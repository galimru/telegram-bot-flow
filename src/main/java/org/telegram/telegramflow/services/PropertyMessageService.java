package org.telegram.telegramflow.services;

import org.telegram.telegramflow.api.MessageService;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Properties;

public class PropertyMessageService implements MessageService {

    private final static String MESSAGES_FILENAME = "/messages.properties";

    private Properties properties;

    @Override
    public void initialize() throws IOException {
        InputStream is = getClass().getResourceAsStream(MESSAGES_FILENAME);
        properties = new Properties();
        Reader reader = new InputStreamReader(is, Charset.defaultCharset());
        properties.load(reader);
    }

    @Nonnull
    @Override
    public String getMessage(@Nonnull String key) {
        Objects.requireNonNull(key, "key is null");

        return properties.getProperty(key, key);
    }

    @Nonnull
    @Override
    public String formatMessage(@Nonnull String key, Object... args) {
        Objects.requireNonNull(key, "key is null");

        return String.format(properties.getProperty(key, key), args);
    }
}
