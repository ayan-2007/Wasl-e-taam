package com.waslataam.state;

import com.waslataam.domain.Donation;
import com.waslataam.domain.DonationStatus;
import com.waslataam.domain.exceptions.IllegalStateTransitionException;

public class RejectedState implements DonationState {
    @Override
    public DonationStatus getStatus() {
        return DonationStatus.REJECTED;
    }

    @Override
    public void claim(Donation donation, UUID recipientId) throws IllegalStateTransitionException {
        throw new IllegalStateTransitionException("Cannot claim a rejected donation");
    }

    @Override
    public void cancel(Donation donation) throws IllegalStateTransitionException {
        throw new IllegalStateTransitionException("Cannot cancel a rejected donation");
    }

    @Override
    public void pickUp(Donation donation, UUID volunteerId) throws IllegalStateTransitionException {
        throw new IllegalStateTransitionException("Cannot pick up a rejected donation");
    }

    @Override
    public void deliver(Donation donation) throws IllegalStateTransitionException {
        throw new IllegalStateTransitionException("Cannot deliver a rejected donation");
    }

    @Override
    public void expire(Donation donation) throws IllegalStateTransitionException {
        throw new IllegalStateTransitionException("Cannot expire a rejected donation");
    }

    @Override
    public void reject(Donation donation, String reason) throws IllegalStateTransitionException {
        throw new IllegalStateTransitionException("Donation is already rejected");
    }
}