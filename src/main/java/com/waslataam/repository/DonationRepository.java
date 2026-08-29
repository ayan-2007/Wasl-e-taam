package com.waslataam.repository;

import com.waslataam.domain.Donation;
import com.waslataam.domain.DonationStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DonationRepository {
    void save(Donation donation);
    Optional<Donation> findById(UUID id);
    List<Donation> findByStatus(DonationStatus status);
    List<Donation> findByDonorId(UUID donorId);
    List<Donation> findByRecipientId(UUID recipientId);
    List<Donation> findAll();
    void delete(UUID id);
    void update(Donation donation);
}