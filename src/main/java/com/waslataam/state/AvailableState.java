package com.waslataam.state;

import com.waslataam.domain.Donation;
import com.waslataam.domain.DonationStatus;
import com.waslataam.domain.exceptions.IllegalStateTransitionException;

public class AvailableState implements DonationState {
    @Override
    public DonationStatus getStatus() {
        return DonationStatus.AVAILABLE;
    }

    @Override
    public void claim(Donation donation, UUID recipientId) throws IllegalStateTransitionException {
        donation.setRecipientId(recipientId);
        donation.setState(new ClaimedState());
    }

    @Override
    public void cancel(Donation donation) throws IllegalStateTransitionException {
        donation.setState(new RejectedState());
    }

    @Override
    public void pickUp(Donation donation, UUID volunteerId) throws IllegalStateTransitionException {
        throw new IllegalStateTransitionException("Cannot pick up a donation that hasn't been claimed");
    }

    @Override
    public void deliver(Donation donation) throws IllegalStateTransitionException {
        throw new IllegalStateTransitionException("Cannot deliver a donation that hasn't been claimed");
    }

    @Override
    public void expire(Donation donation) throws IllegalStateTransitionException {
        donation.setState(new ExpiredState());
    }

    @Override
    public void reject(Donation donation, String reason) throws IllegalStateTransitionException {
        donation.setState(new RejectedState());
    }
}