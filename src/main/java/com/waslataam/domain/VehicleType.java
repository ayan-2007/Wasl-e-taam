package com.waslataam.domain;

public enum VehicleType {
    BICYCLE("Bicycle"),
    MOTORCYCLE("Motorcycle"),
    CAR("Car"),
    VAN("Van"),
    TRUCK("Truck");

    private final String displayName;

    VehicleType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getCapacityKg() {
        return switch (this) {
            case BICYCLE -> 20;
            case MOTORCYCLE -> 30;
            case CAR -> 100;
            case VAN -> 500;
            case TRUCK -> 2000;
        };
    }
}