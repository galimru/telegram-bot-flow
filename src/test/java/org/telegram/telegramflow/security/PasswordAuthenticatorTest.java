package org.telegram.telegramflow.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegramflow.dummy.DummyRole;
import org.telegram.telegramflow.dummy.DummyUser;
import org.telegram.telegramflow.exceptions.AuthenticationException;
import org.telegram.telegramflow.objects.*;
import org.telegram.telegramflow.services.MessageProvider;
import org.telegram.telegramflow.services.TelegramBot;
import org.telegram.telegramflow.services.UserService;
import utils.JsonUtil;

import javax.annotation.Nonnull;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PasswordAuthenticatorTest {

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private UserService userService;

    @Mock
    private MessageProvider messageProvider;

    @Captor
    private ArgumentCaptor<SendMessage> sendMessageCaptor;

    private Authenticator authenticator;

    @Before
    public void setup() {
        authenticator = new PasswordAuthenticator() {
            @Override
            protected TelegramRole login(@Nonnull Credentials credentials) throws AuthenticationException {
                if (credentials.getLogin().equals("admin")
                        && credentials.getPassword().equals("admin")) {
                    return DummyRole.ADMIN;
                }
                throw new AuthenticationException("Login or password incorrect");
            }
        };
        authenticator.setTelegramBot(telegramBot);
        authenticator.setUserService(userService);
        authenticator.setMessageProvider(messageProvider);
    }

    @Test
    public void shouldSendAuthorizeMessageForUnauthorizedUser() throws IOException, TelegramApiException {
        when(userService.find(any())).thenReturn(null);
        when(userService.create()).thenReturn(new DummyUser());
        when(messageProvider.getMessage(any())).thenAnswer(i -> i.getArguments()[0]);

        Update update = JsonUtil.fromFile("/json/update_start.json");
        try {
            authenticator.authorize(update);
            fail("Expected AuthenticationException");
        } catch (AuthenticationException e) {
            assertNotNull(e);
        }

        verify(telegramBot).execute(sendMessageCaptor.capture());
        assertEquals("authentication.authorizeMessage", sendMessageCaptor.getValue().getText());
    }

    @Test
    public void shouldSendLoginRequestAfterPressingAuthorize() throws IOException, TelegramApiException {
        when(userService.create()).thenReturn(new DummyUser());
        when(messageProvider.getMessage(any())).thenAnswer(i -> i.getArguments()[0]);

        Update update = JsonUtil.fromFile("/json/update_press_authorize.json");
        try {
            authenticator.authorize(update);
            fail("Expected AuthenticationException");
        } catch (AuthenticationException e) {
            assertNotNull(e);
        }

        verify(telegramBot).execute(sendMessageCaptor.capture());
        assertEquals("authentication.loginMessage", sendMessageCaptor.getValue().getText());
    }

    @Test
    public void shouldSendPasswordRequestAfterSendingLogin() throws IOException, TelegramApiException {
        when(userService.find(any())).thenAnswer(i -> {
            TelegramUser user = new DummyUser();
            user.setAuthState(AuthState.AUTHORIZATION);
            return user;
        });
        when(messageProvider.getMessage(any())).thenAnswer(i -> i.getArguments()[0]);

        // send login
        Update update = JsonUtil.fromFile("/json/update_text_admin.json");
        try {
            authenticator.authorize(update);
            fail("Expected AuthenticationException");
        } catch (AuthenticationException e) {
            assertNotNull(e);
        }

        verify(telegramBot).execute(sendMessageCaptor.capture());
        assertEquals("authentication.passwordMessage", sendMessageCaptor.getValue().getText());
    }

    @Test
    public void shouldAuthorizeAfterSendingCorrectPassword() throws IOException, TelegramApiException, AuthenticationException {
        when(userService.find(any())).thenAnswer(i -> {
            TelegramUser user = new DummyUser();
            user.setAuthState(AuthState.AUTHORIZATION);
            return user;
        });
        when(messageProvider.getMessage(any())).thenAnswer(i -> i.getArguments()[0]);

        // send login
        Update update = JsonUtil.fromFile("/json/update_text_admin.json");
        try {
            authenticator.authorize(update);
            fail("Expected AuthenticationException");
        } catch (AuthenticationException e) {
            assertNotNull(e);
        }

        // send password
        update = JsonUtil.fromFile("/json/update_text_admin.json");

        TelegramUser authorizedUser = authenticator.authorize(update);
        assertNotNull(authorizedUser);
    }

    @Test
    public void shouldSendRestrictedMessageAfterSendingIncorrectPassword() throws IOException, TelegramApiException, AuthenticationException {
        when(userService.find(any())).thenAnswer(i -> {
            TelegramUser user = new DummyUser();
            user.setAuthState(AuthState.AUTHORIZATION);
            return user;
        });
        when(messageProvider.getMessage(any())).thenAnswer(i -> i.getArguments()[0]);

        // send login
        Update update = JsonUtil.fromFile("/json/update_text_admin.json");
        try {
            authenticator.authorize(update);
            fail("Expected AuthenticationException");
        } catch (AuthenticationException e) {
            assertNotNull(e);
        }

        // send incorrect password
        update = JsonUtil.fromFile("/json/update_text_test.json");
        try {
            authenticator.authorize(update);
            fail("Expected AuthenticationException");
        } catch (AuthenticationException e) {
            assertNotNull(e);
        }

        verify(telegramBot, times(2)).execute(sendMessageCaptor.capture());
        assertEquals("authentication.restrictedMessage", sendMessageCaptor.getValue().getText());
    }

    @Test
    public void shouldReturnUserForAuthorizedUser() throws IOException, TelegramApiException, AuthenticationException {
        when(userService.find(any())).thenReturn(null);
        when(userService.create()).then(i -> {
            TelegramUser user = new DummyUser();
            user.setAuthState(AuthState.AUTHORIZED);
            user.setRole(DummyRole.USER);
            return user;
        });

        Update update = JsonUtil.fromFile("/json/update_start.json");

        TelegramUser authorizedUser = authenticator.authorize(update);

        assertNotNull(authorizedUser);
    }
}