package com.waslataam.command;

import com.waslataam.domain.DonationStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuditTrail {
    private final List<AuditEntry> entries = new ArrayList<>();

    public void log(UUID actorId, String actorRole, String action, UUID donationId, DonationStatus previousStatus, DonationStatus newStatus) {
        AuditEntry entry = new AuditEntry(
                LocalDateTime.now(),
                actorId,
                actorRole,
                action,
                donationId,
                previousStatus,
                newStatus
        );
        entries.add(entry);
    }

    public List<AuditEntry> getEntries() {
        return new ArrayList<>(entries);
    }

    public List<AuditEntry> getEntriesSince(LocalDateTime since) {
        return entries.stream()
                .filter(e -> e.timestamp().isAfter(since))
                .toList();
    }

    public List<AuditEntry> getEntriesByActor(UUID actorId) {
        return entries.stream()
                .filter(e -> e.actorId().equals(actorId))
                .toList();
    }

    public List<AuditEntry> getEntriesByDonation(UUID donationId) {
        return entries.stream()
                .filter(e -> e.donationId().equals(donationId))
                .toList();
    }

    public record AuditEntry(
            LocalDateTime timestamp,
            UUID actorId,
            String actorRole,
            String action,
            UUID donationId,
            DonationStatus previousStatus,
            DonationStatus newStatus
    ) {}
}