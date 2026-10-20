package com.waslataam.state;

import com.waslataam.domain.Donation;
import com.waslataam.domain.DonationStatus;
import com.waslataam.domain.exceptions.IllegalStateTransitionException;

import java.util.UUID;

public class InTransitState implements DonationState {
    @Override
    public DonationStatus getStatus() {
        return DonationStatus.IN_TRANSIT;
    }

    @Override
    public void claim(Donation donation, UUID recipientId) throws IllegalStateTransitionException {
        throw new IllegalStateTransitionException("Cannot claim a donation that is in transit");
    }

    @Override
    public void cancel(Donation donation) throws IllegalStateTransitionException {
        throw new IllegalStateTransitionException("Cannot cancel a donation that is in transit");
    }

    @Override
    public void pickUp(Donation donation, UUID volunteerId) throws IllegalStateTransitionException {
        throw new IllegalStateTransitionException("Donation is already picked up");
    }

    @Override
    public void deliver(Donation donation) throws IllegalStateTransitionException {
        donation.setState(new DeliveredState());
    }

    @Override
    public void expire(Donation donation) throws IllegalStateTransitionException {
        throw new IllegalStateTransitionException("Cannot expire a donation that is in transit");
    }

    @Override
    public void reject(Donation donation, String reason) throws IllegalStateTransitionException {
        throw new IllegalStateTransitionException("Cannot reject a donation that is in transit");
    }
}