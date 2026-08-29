package com.waslataam.domain;

public enum DonationType {
    PERISHABLE("Perishable Food"),
    DRY_FOOD("Dry Food"),
    PREPARED_MEAL("Prepared Meal");

    private final String displayName;

    DonationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}