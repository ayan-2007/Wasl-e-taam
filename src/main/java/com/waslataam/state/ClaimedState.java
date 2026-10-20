package com.waslataam.state;

import com.waslataam.domain.Donation;
import com.waslataam.domain.DonationStatus;
import com.waslataam.domain.exceptions.IllegalStateTransitionException;

import java.util.UUID;

public class ClaimedState implements DonationState {
    @Override
    public DonationStatus getStatus() {
        return DonationStatus.CLAIMED;
    }

    @Override
    public void claim(Donation donation, UUID recipientId) throws IllegalStateTransitionException {
        throw new IllegalStateTransitionException("Donation is already claimed");
    }

    @Override
    public void cancel(Donation donation) throws IllegalStateTransitionException {
        donation.setRecipientId(null);
        donation.setState(new AvailableState());
    }

    @Override
    public void pickUp(Donation donation, UUID volunteerId) throws IllegalStateTransitionException {
        donation.setVolunteerId(volunteerId);
        donation.setState(new InTransitState());
    }

    @Override
    public void deliver(Donation donation) throws IllegalStateTransitionException {
        throw new IllegalStateTransitionException("Cannot deliver a donation that hasn't been picked up");
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