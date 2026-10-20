package com.waslataam.state;

import com.waslataam.domain.Donation;
import com.waslataam.domain.DonationStatus;
import com.waslataam.domain.exceptions.IllegalStateTransitionException;

import java.util.UUID;

public class DeliveredState implements DonationState {
    @Override
    public DonationStatus getStatus() {
        return DonationStatus.DELIVERED;
    }

    @Override
    public void claim(Donation donation, UUID recipientId) throws IllegalStateTransitionException {
        throw new IllegalStateTransitionException("Cannot claim a delivered donation");
    }

    @Override
    public void cancel(Donation donation) throws IllegalStateTransitionException {
        throw new IllegalStateTransitionException("Cannot cancel a delivered donation");
    }

    @Override
    public void pickUp(Donation donation, UUID volunteerId) throws IllegalStateTransitionException {
        throw new IllegalStateTransitionException("Cannot pick up a delivered donation");
    }

    @Override
    public void deliver(Donation donation) throws IllegalStateTransitionException {
        throw new IllegalStateTransitionException("Donation is already delivered");
    }

    @Override
    public void expire(Donation donation) throws IllegalStateTransitionException {
        throw new IllegalStateTransitionException("Cannot expire a delivered donation");
    }

    @Override
    public void reject(Donation donation, String reason) throws IllegalStateTransitionException {
        throw new IllegalStateTransitionException("Cannot reject a delivered donation");
    }
}