package com.waslataam.observer;

import com.waslataam.domain.Donation;
import com.waslataam.domain.DonationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class SystemEvent {
    public enum Type {
        DONATION_POSTED,
        DONATION_CLAIMED,
        DONATION_CANCELLED,
        DONATION_PICKED_UP,
        DONATION_DELIVERED,
        DONATION_EXPIRED,
        DONATION_REJECTED
    }

    private final Type type;
    private final Donation donation;
    private final UUID actorId;
    private final String actorRole;
    private final DonationStatus previousStatus;
    private final LocalDateTime timestamp;

    public SystemEvent(Type type, Donation donation, UUID actorId, String actorRole, DonationStatus previousStatus) {
        this.type = type;
        this.donation = donation;
        this.actorId = actorId;
        this.actorRole = actorRole;
        this.previousStatus = previousStatus;
        this.timestamp = LocalDateTime.now();
    }

    public Type getType() {
        return type;
    }

    public Donation getDonation() {
        return donation;
    }

    public UUID getActorId() {
        return actorId;
    }

    public String getActorRole() {
        return actorRole;
    }

    public DonationStatus getPreviousStatus() {
        return previousStatus;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public DonationStatus getCurrentStatus() {
        return donation.getStatus();
    }
}