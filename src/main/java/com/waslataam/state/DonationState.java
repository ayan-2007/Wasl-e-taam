package com.waslataam.state;

import com.waslataam.domain.Donation;
import com.waslataam.domain.DonationStatus;
import com.waslataam.domain.exceptions.IllegalStateTransitionException;

import java.util.UUID;

public interface DonationState {
    DonationStatus getStatus();

    void claim(Donation donation, UUID recipientId) throws IllegalStateTransitionException;

    void cancel(Donation donation) throws IllegalStateTransitionException;

    void pickUp(Donation donation, UUID volunteerId) throws IllegalStateTransitionException;

    void deliver(Donation donation) throws IllegalStateTransitionException;

    void expire(Donation donation) throws IllegalStateTransitionException;

    void reject(Donation donation, String reason) throws IllegalStateTransitionException;
}