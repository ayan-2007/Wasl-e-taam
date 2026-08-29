package com.waslataam.command;

import com.waslataam.domain.Donation;
import com.waslataam.domain.DonationStatus;
import com.waslataam.domain.DonationType;
import com.waslataam.domain.PerishableDonation;
import com.waslataam.domain.exceptions.IllegalStateTransitionException;
import com.waslataam.repository.DonationRepository;
import com.waslataam.repository.InMemoryDonationRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CommandTest {
    private DonationRepository donationRepository;
    private Donation donation;
    private UUID donorId;
    private UUID recipientId;
    private UUID volunteerId;

    @BeforeEach
    void setUp() {
        donationRepository = new InMemoryDonationRepo();
        donorId = UUID.randomUUID();
        recipientId = UUID.randomUUID();
        volunteerId = UUID.randomUUID();
        
        donation = new PerishableDonation(
                UUID.randomUUID(), 25.0, "kg", donorId,
                LocalDateTime.now().plusDays(1), "2-8°C"
        );
        donation.setState(new com.waslataam.state.AvailableState());
        donationRepository.save(donation);
    }

    @Test
    void testClaimDonationCommand() {
        ClaimDonationCommand cmd = new ClaimDonationCommand(donationRepository, donation.getId(), recipientId);
        
        assertEquals(DonationStatus.AVAILABLE, donation.getStatus());
        cmd.execute();
        assertEquals(DonationStatus.CLAIMED, donation.getStatus());
        assertEquals(recipientId, donation.getRecipientId());
        
        // Test undo
        cmd.undo();
        assertEquals(DonationStatus.AVAILABLE, donation.getStatus());
        assertNull(donation.getRecipientId());
    }

    @Test
    void testCancelClaimCommand() {
        // First claim
        donation.claim(recipientId);
        donationRepository.update(donation);
        
        CancelClaimCommand cmd = new CancelClaimCommand(donationRepository, donation.getId(), recipientId);
        cmd.execute();
        
        assertEquals(DonationStatus.AVAILABLE, donation.getStatus());
        
        // Test undo
        cmd.undo();
        assertEquals(DonationStatus.CLAIMED, donation.getStatus());
    }

    @Test
    void testMarkPickedUpCommand() {
        donation.claim(recipientId);
        donationRepository.update(donation);
        
        MarkPickedUpCommand cmd = new MarkPickedUpCommand(donationRepository, donation.getId(), volunteerId);
        cmd.execute();
        
        assertEquals(DonationStatus.IN_TRANSIT, donation.getStatus());
        assertEquals(volunteerId, donation.getVolunteerId());
        
        // Test undo
        cmd.undo();
        assertEquals(DonationStatus.CLAIMED, donation.getStatus());
    }

    @Test
    void testMarkDeliveredCommand() {
        donation.claim(recipientId);
        donation.pickUp(volunteerId);
        donationRepository.update(donation);
        
        MarkDeliveredCommand cmd = new MarkDeliveredCommand(donationRepository, donation.getId(), volunteerId);
        cmd.execute();
        
        assertEquals(DonationStatus.DELIVERED, donation.getStatus());
        
        // Test undo
        cmd.undo();
        assertEquals(DonationStatus.IN_TRANSIT, donation.getStatus());
    }

    @Test
    void testCommandInvoker() {
        CommandInvoker invoker = new CommandInvoker();
        
        ClaimDonationCommand cmd = new ClaimDonationCommand(donationRepository, donation.getId(), recipientId);
        invoker.execute(cmd);
        
        assertEquals(DonationStatus.CLAIMED, donation.getStatus());
        assertTrue(invoker.canUndo());
        
        invoker.undo();
        assertEquals(DonationStatus.AVAILABLE, donation.getStatus());
        assertFalse(invoker.canUndo());
    }

    @Test
    void testAuditTrail() {
        AuditTrail auditTrail = new AuditTrail();
        UUID actorId = UUID.randomUUID();
        
        auditTrail.log(actorId, "RECIPIENT", "CLAIM_DONATION", donation.getId(), 
                DonationStatus.AVAILABLE, DonationStatus.CLAIMED);
        
        var entries = auditTrail.getEntries();
        assertEquals(1, entries.size());
        assertEquals("CLAIM_DONATION", entries.get(0).action());
        assertEquals(DonationStatus.AVAILABLE, entries.get(0).previousStatus());
        assertEquals(DonationStatus.CLAIMED, entries.get(0).newStatus());
    }
}