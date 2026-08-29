package com.waslataam.domain;

public enum UserRole {
    DONOR("Donor"),
    RECIPIENT("Recipient"),
    VOLUNTEER("Volunteer"),
    ADMIN("Admin");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}