package com.waslataam.repository;

import com.waslataam.domain.Donation;
import com.waslataam.domain.DonationStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class InMemoryDonationRepo implements DonationRepository {
    private final ConcurrentMap<UUID, Donation> donations = new ConcurrentHashMap<>();

    @Override
    public void save(Donation donation) {
        donations.put(donation.getId(), donation);
    }

    @Override
    public Optional<Donation> findById(UUID id) {
        return Optional.ofNullable(donations.get(id));
    }

    @Override
    public List<Donation> findByStatus(DonationStatus status) {
        return donations.values().stream()
                .filter(d -> d.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public List<Donation> findByDonorId(UUID donorId) {
        return donations.values().stream()
                .filter(d -> d.getDonorId().equals(donorId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Donation> findByRecipientId(UUID recipientId) {
        return donations.values().stream()
                .filter(d -> d.getRecipientId() != null && d.getRecipientId().equals(recipientId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Donation> findAll() {
        return new ArrayList<>(donations.values());
    }

    @Override
    public void delete(UUID id) {
        donations.remove(id);
    }

    @Override
    public void update(Donation donation) {
        if (donations.containsKey(donation.getId())) {
            donations.put(donation.getId(), donation);
        }
    }
}