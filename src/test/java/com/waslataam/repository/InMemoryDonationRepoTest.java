package com.waslataam.repository;

import com.waslataam.domain.Donation;
import com.waslataam.domain.DonationStatus;
import com.waslataam.domain.DonationType;
import com.waslataam.domain.PerishableDonation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryDonationRepoTest {
    private InMemoryDonationRepo repo;
    private UUID donorId;
    private UUID recipientId;

    @BeforeEach
    void setUp() {
        repo = new InMemoryDonationRepo();
        donorId = UUID.randomUUID();
        recipientId = UUID.randomUUID();
    }

    @Test
    void testSaveAndFindById() {
        Donation donation = new PerishableDonation(
                UUID.randomUUID(), 10.0, "kg", donorId,
                LocalDateTime.now().plusDays(1), "2-8°C"
        );
        
        repo.save(donation);
        
        Optional<Donation> found = repo.findById(donation.getId());
        assertTrue(found.isPresent());
        assertEquals(donation.getId(), found.get().getId());
    }

    @Test
    void testFindByStatus() {
        Donation d1 = new PerishableDonation(UUID.randomUUID(), 10.0, "kg", donorId, LocalDateTime.now().plusDays(1), "2-8°C");
        d1.setState(new com.waslataam.state.AvailableState());
        
        Donation d2 = new PerishableDonation(UUID.randomUUID(), 20.0, "kg", donorId, LocalDateTime.now().plusDays(1), "2-8°C");
        d2.setState(new com.waslataam.state.AvailableState());
        
        Donation d3 = new PerishableDonation(UUID.randomUUID(), 30.0, "kg", donorId, LocalDateTime.now().plusDays(1), "2-8°C");
        d3.setState(new com.waslataam.state.ClaimedState());
        d3.claim(recipientId);
        
        repo.save(d1);
        repo.save(d2);
        repo.save(d3);
        
        List<Donation> available = repo.findByStatus(DonationStatus.AVAILABLE);
        assertEquals(2, available.size());
        
        List<Donation> claimed = repo.findByStatus(DonationStatus.CLAIMED);
        assertEquals(1, claimed.size());
    }

    @Test
    void testFindByDonorId() {
        Donation d1 = new PerishableDonation(UUID.randomUUID(), 10.0, "kg", donorId, LocalDateTime.now().plusDays(1), "2-8°C");
        Donation d2 = new PerishableDonation(UUID.randomUUID(), 20.0, "kg", donorId, LocalDateTime.now().plusDays(1), "2-8°C");
        
        UUID otherDonor = UUID.randomUUID();
        Donation d3 = new PerishableDonation(UUID.randomUUID(), 30.0, "kg", otherDonor, LocalDateTime.now().plusDays(1), "2-8°C");
        
        repo.save(d1);
        repo.save(d2);
        repo.save(d3);
        
        List<Donation> donorDonations = repo.findByDonorId(donorId);
        assertEquals(2, donorDonations.size());
    }

    @Test
    void testFindByRecipientId() {
        Donation d1 = new PerishableDonation(UUID.randomUUID(), 10.0, "kg", donorId, LocalDateTime.now().plusDays(1), "2-8°C");
        d1.setState(new com.waslataam.state.AvailableState());
        
        Donation d2 = new PerishableDonation(UUID.randomUUID(), 20.0, "kg", donorId, LocalDateTime.now().plusDays(1), "2-8°C");
        d2.setState(new com.waslataam.state.ClaimedState());
        d2.claim(recipientId);
        
        repo.save(d1);
        repo.save(d2);
        
        List<Donation> recipientDonations = repo.findByRecipientId(recipientId);
        assertEquals(1, recipientDonations.size());
    }

    @Test
    void testFindAll() {
        repo.save(new PerishableDonation(UUID.randomUUID(), 10.0, "kg", donorId, LocalDateTime.now().plusDays(1), "2-8°C"));
        repo.save(new PerishableDonation(UUID.randomUUID(), 20.0, "kg", donorId, LocalDateTime.now().plusDays(1), "2-8°C"));
        
        List<Donation> all = repo.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void testUpdate() {
        Donation donation = new PerishableDonation(UUID.randomUUID(), 10.0, "kg", donorId, LocalDateTime.now().plusDays(1), "2-8°C");
        donation.setState(new com.waslataam.state.AvailableState());
        repo.save(donation);
        
        donation.setState(new com.waslataam.state.ClaimedState());
        donation.claim(recipientId);
        repo.update(donation);
        
        Optional<Donation> found = repo.findById(donation.getId());
        assertTrue(found.isPresent());
        assertEquals(DonationStatus.CLAIMED, found.get().getStatus());
    }

    @Test
    void testDelete() {
        Donation donation = new PerishableDonation(UUID.randomUUID(), 10.0, "kg", donorId, LocalDateTime.now().plusDays(1), "2-8°C");
        repo.save(donation);
        
        assertTrue(repo.findById(donation.getId()).isPresent());
        
        repo.delete(donation.getId());
        
        assertFalse(repo.findById(donation.getId()).isPresent());
    }
}