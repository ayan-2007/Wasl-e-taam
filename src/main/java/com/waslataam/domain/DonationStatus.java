package com.waslataam.domain;

public enum DonationStatus {
    PENDING_REVIEW("Pending Review"),
    AVAILABLE("Available"),
    CLAIMED("Claimed"),
    IN_TRANSIT("In Transit"),
    DELIVERED("Delivered"),
    EXPIRED("Expired"),
    REJECTED("Rejected");

    private final String displayName;

    DonationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActive() {
        return this == AVAILABLE || this == CLAIMED || this == IN_TRANSIT;
    }

    public boolean isTerminal() {
        return this == DELIVERED || this == EXPIRED || this == REJECTED;
    }
}