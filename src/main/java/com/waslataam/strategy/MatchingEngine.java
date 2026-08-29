package com.waslataam.strategy;

import com.waslataam.domain.Donation;
import com.waslataam.domain.Recipient;
import com.waslataam.repository.UserRepository;

import java.util.List;
import java.util.Optional;

public class MatchingEngine {
    private MatchingStrategy currentStrategy;
    private final UserRepository userRepository;

    public MatchingEngine(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.currentStrategy = new NearestFirstStrategy(); // Default strategy
    }

    public void setStrategy(MatchingStrategy strategy) {
        this.currentStrategy = strategy;
    }

    public MatchingStrategy getCurrentStrategy() {
        return currentStrategy;
    }

    public Optional<Recipient> findBestMatch(Donation donation) {
        List<Recipient> candidates = userRepository.findByRole(com.waslataam.domain.UserRole.RECIPIENT)
                .stream()
                .map(r -> (Recipient) r)
                .toList();
        return currentStrategy.findBestMatch(donation, candidates);
    }

    public List<String> getAvailableStrategies() {
        return List.of("Nearest First", "Expiry First", "Capacity Based");
    }
}