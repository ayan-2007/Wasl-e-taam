package com.waslataam.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Donor extends User {
    private final String organizationName;
    private final List<UUID> donationHistory;

    public Donor(UUID id, String name, String email, Location location, String organizationName) {
        super(id, name, email, location, UserRole.DONOR);
        this.organizationName = organizationName;
        this.donationHistory = new ArrayList<>();
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public List<UUID> getDonationHistory() {
        return new ArrayList<>(donationHistory);
    }

    public void addDonation(UUID donationId) {
        donationHistory.add(donationId);
    }

    @Override
    public String toString() {
        return "Donor{" +
                "organizationName='" + organizationName + '\'' +
                ", donationCount=" + donationHistory.size() +
                "} " + super.toString();
    }
}