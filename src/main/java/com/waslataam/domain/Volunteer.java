package com.waslataam.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Volunteer extends User {
    private final VehicleType vehicleType;
    private boolean isAvailable;
    private final List<UUID> assignedPickups;

    public Volunteer(UUID id, String name, String email, Location location, VehicleType vehicleType) {
        super(id, name, email, location, UserRole.VOLUNTEER);
        this.vehicleType = vehicleType;
        this.isAvailable = true;
        this.assignedPickups = new ArrayList<>();
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public List<UUID> getAssignedPickups() {
        return new ArrayList<>(assignedPickups);
    }

    public void assignPickup(UUID donationId) {
        assignedPickups.add(donationId);
        isAvailable = false;
    }

    public void completePickup(UUID donationId) {
        assignedPickups.remove(donationId);
        if (assignedPickups.isEmpty()) {
            isAvailable = true;
        }
    }

    public int getVehicleCapacityKg() {
        return vehicleType.getCapacityKg();
    }

    @Override
    public String toString() {
        return "Volunteer{" +
                "vehicleType=" + vehicleType +
                ", isAvailable=" + isAvailable +
                ", assignedPickups=" + assignedPickups.size() +
                "} " + super.toString();
    }
}