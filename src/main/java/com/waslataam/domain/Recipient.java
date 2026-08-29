package com.waslataam.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Recipient extends User {
    private final int capacityKg;
    private int currentLoadKg;
    private final double reliabilityScore;
    private final List<UUID> claimedDonations;
    private final List<UUID> completedDonations;

    public Recipient(UUID id, String name, String email, Location location, int capacityKg, double reliabilityScore) {
        super(id, name, email, location, UserRole.RECIPIENT);
        if (capacityKg <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        if (reliabilityScore < 0.0 || reliabilityScore > 1.0) {
            throw new IllegalArgumentException("Reliability score must be between 0.0 and 1.0");
        }
        this.capacityKg = capacityKg;
        this.currentLoadKg = 0;
        this.reliabilityScore = reliabilityScore;
        this.claimedDonations = new ArrayList<>();
        this.completedDonations = new ArrayList<>();
    }

    public int getCapacityKg() {
        return capacityKg;
    }

    public int getCurrentLoadKg() {
        return currentLoadKg;
    }

    public int getRemainingCapacityKg() {
        return capacityKg - currentLoadKg;
    }

    public double getReliabilityScore() {
        return reliabilityScore;
    }

    public List<UUID> getClaimedDonations() {
        return new ArrayList<>(claimedDonations);
    }

    public List<UUID> getCompletedDonations() {
        return new ArrayList<>(completedDonations);
    }

    public boolean canAccept(int quantityKg) {
        return currentLoadKg + quantityKg <= capacityKg;
    }

    public void addClaimedDonation(UUID donationId) {
        claimedDonations.add(donationId);
    }

    public void removeClaimedDonation(UUID donationId) {
        claimedDonations.remove(donationId);
    }

    public void completeDonation(UUID donationId, int quantityKg) {
        claimedDonations.remove(donationId);
        completedDonations.add(donationId);
        currentLoadKg = Math.max(0, currentLoadKg - quantityKg);
    }

    public void increaseLoad(int quantityKg) {
        currentLoadKg += quantityKg;
    }

    @Override
    public String toString() {
        return "Recipient{" +
                "capacityKg=" + capacityKg +
                ", currentLoadKg=" + currentLoadKg +
                ", reliabilityScore=" + reliabilityScore +
                "} " + super.toString();
    }
}