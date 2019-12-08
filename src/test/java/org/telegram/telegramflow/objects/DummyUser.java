package org.telegram.telegramflow.objects;

public class DummyUser extends AbstractUser {

    private DummyRole role;

    @Override
    public TelegramRole getRole() {
        return role;
    }

    @Override
    public void setRole(TelegramRole role) {
        this.role = (DummyRole) role;
    }
}