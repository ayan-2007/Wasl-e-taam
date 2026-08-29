package com.waslataam.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public abstract class Donation {
    private final UUID id;
    private final DonationType type;
    private final double quantity;
    private final String unit;
    private final UUID donorId;
    private UUID recipientId;
    private UUID volunteerId;
    private final LocalDateTime postedAt;
    private LocalDateTime updatedAt;
    private DonationState state;

    protected Donation(UUID id, DonationType type, double quantity, String unit, UUID donorId) {
        this.id = id;
        this.type = type;
        this.quantity = quantity;
        this.unit = unit;
        this.donorId = donorId;
        this.postedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.state = new PendingReviewState();
    }

    public UUID getId() {
        return id;
    }

    public DonationType getType() {
        return type;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public UUID getDonorId() {
        return donorId;
    }

    public UUID getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(UUID recipientId) {
        this.recipientId = recipientId;
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getVolunteerId() {
        return volunteerId;
    }

    public void setVolunteerId(UUID volunteerId) {
        this.volunteerId = volunteerId;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getPostedAt() {
        return postedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public DonationStatus getStatus() {
        return state.getStatus();
    }

    public DonationState getState() {
        return state;
    }

    public void setState(DonationState state) {
        this.state = state;
        this.updatedAt = LocalDateTime.now();
    }

    // State delegation methods
    public void claim(UUID recipientId) {
        state.claim(this, recipientId);
    }

    public void cancelClaim() {
        state.cancel(this);
    }

    public void pickUp(UUID volunteerId) {
        state.pickUp(this, volunteerId);
    }

    public void deliver() {
        state.deliver(this);
    }

    public void expire() {
        state.expire(this);
    }

    public void reject(String reason) {
        state.reject(this, reason);
    }

    public abstract LocalDateTime getExpiryTime();

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(getExpiryTime());
    }

    public boolean isUrgent() {
        return LocalDateTime.now().plusHours(2).isAfter(getExpiryTime());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Donation donation = (Donation) o;
        return Objects.equals(id, donation.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Donation{" +
                "id=" + id +
                ", type=" + type +
                ", quantity=" + quantity + " " + unit +
                ", status=" + getStatus() +
                ", donorId=" + donorId +
                ", postedAt=" + postedAt +
                '}';
    }
}