package com.waslataam.command;

import com.waslataam.domain.Donation;
import com.waslataam.domain.exceptions.IllegalStateTransitionException;

import java.util.UUID;

public interface DonationCommand {
    void execute() throws IllegalStateTransitionException;

    void undo() throws IllegalStateTransitionException;

    String describe();

    UUID getDonationId();

    UUID getActorId();
}