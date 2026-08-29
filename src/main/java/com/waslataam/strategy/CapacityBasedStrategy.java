package com.waslataam.strategy;

import com.waslataam.domain.Donation;
import com.waslataam.domain.Recipient;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class CapacityBasedStrategy implements MatchingStrategy {
    @Override
    public Optional<Recipient> findBestMatch(Donation donation, List<Recipient> candidates) {
        return candidates.stream()
                .filter(r -> r.canAccept((int) donation.getQuantity()))
                .filter(r -> r.isActive())
                .max(Comparator.comparingInt(Recipient::getRemainingCapacityKg));
    }

    @Override
    public String getName() {
        return "Capacity Based";
    }
}