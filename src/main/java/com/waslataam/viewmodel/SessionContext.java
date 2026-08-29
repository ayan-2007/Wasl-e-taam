package com.waslataam.viewmodel;

import com.waslataam.domain.User;
import com.waslataam.domain.UserRole;

import javafx.beans.property.*;

public class SessionContext {
    private static final SessionContext INSTANCE = new SessionContext();
    private final ObjectProperty<User> currentUser = new SimpleObjectProperty<>();
    private final ObjectProperty<UserRole> currentRole = new SimpleObjectProperty<>();

    public ObjectProperty<User> currentUserProperty() {
        return currentUser;
    }

    public User getCurrentUser() {
        return currentUser.get();
    }

    public void setCurrentUser(User user) {
        currentUser.set(user);
        if (user != null) {
            currentRole.set(user.getRole());
        } else {
            currentRole.set(null);
        }
    }

    public ObjectProperty<UserRole> currentRoleProperty() {
        return currentRole;
    }

    public UserRole getCurrentRole() {
        return currentRole.get();
    }

    public boolean isLoggedIn() {
        return currentUser.get() != null;
    }

    public void clear() {
        currentUser.set(null);
        currentRole.set(null);
    }

    public boolean hasRole(UserRole role) {
        return currentRole.get() == role;
    }

    public static SessionContext getInstance() {
        return INSTANCE;
    }
}