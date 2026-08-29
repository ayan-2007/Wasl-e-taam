package com.waslataam.strategy;

import com.waslataam.domain.Donation;
import com.waslataam.domain.Recipient;

import java.util.List;
import java.util.Optional;

public interface MatchingStrategy {
    Optional<Recipient> findBestMatch(Donation donation, List<Recipient> candidates);

    String getName();
}