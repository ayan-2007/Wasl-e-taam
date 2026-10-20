package com.waslataam.observer;

import com.waslataam.command.AuditTrail;
import com.waslataam.domain.Donation;
import com.waslataam.domain.DonationStatus;
import com.waslataam.domain.DonationType;
import com.waslataam.domain.PerishableDonation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ObserverTest {
    private AuditTrail auditTrail;
    private AuditLogger auditLogger;
    private Donation donation;
    private UUID donorId;
    private UUID recipientId;

    @BeforeEach
    void setUp() {
        auditTrail = new AuditTrail();
        auditLogger = new AuditLogger(auditTrail);
        donorId = UUID.randomUUID();
        recipientId = UUID.randomUUID();
        
        donation = new PerishableDonation(
                UUID.randomUUID(), 10.0, "kg", donorId,
                LocalDateTime.now().plusDays(1), "2-8°C"
        );
        donation.setLocation(new com.waslataam.domain.Location(24.8607, 67.0011, "Karachi", "Karachi"));
    }

    @Test
    void testAuditLoggerRecordsDonationPosted() {
        donation.setState(new com.waslataam.state.AvailableState());
        SystemEvent event = new SystemEvent(
                SystemEvent.Type.DONATION_POSTED, donation, donorId, "DONOR", DonationStatus.PENDING_REVIEW
        );
        
        auditLogger.onDonationPosted(event);
        
        var entries = auditTrail.getEntries();
        assertEquals(1, entries.size());
        assertEquals("POST_DONATION", entries.get(0).action());
        assertEquals(DonationStatus.PENDING_REVIEW, entries.get(0).previousStatus());
        assertEquals(DonationStatus.AVAILABLE, entries.get(0).newStatus());
    }

    @Test
    void testAuditLoggerRecordsClaim() {
        donation.setState(new com.waslataam.state.AvailableState());
        donation.claim(recipientId);
        
        SystemEvent event = new SystemEvent(
                SystemEvent.Type.DONATION_CLAIMED, donation, recipientId, "RECIPIENT", DonationStatus.AVAILABLE
        );
        
        auditLogger.onDonationClaimed(event);
        
        var entries = auditTrail.getEntries();
        assertEquals(1, entries.size());
        assertEquals("CLAIM_DONATION", entries.get(0).action());
        assertEquals(DonationStatus.AVAILABLE, entries.get(0).previousStatus());
        assertEquals(DonationStatus.CLAIMED, entries.get(0).newStatus());
    }

    @Test
    void testAuditLoggerRecordsDelivery() {
        donation.setState(new com.waslataam.state.AvailableState());
        donation.claim(recipientId);
        donation.pickUp(UUID.randomUUID());
        donation.deliver();
        
        SystemEvent event = new SystemEvent(
                SystemEvent.Type.DONATION_DELIVERED, donation, UUID.randomUUID(), "VOLUNTEER", DonationStatus.IN_TRANSIT
        );
        
        auditLogger.onDonationDelivered(event);
        
        var entries = auditTrail.getEntries();
        assertEquals(1, entries.size());
        assertEquals("DELIVER_DONATION", entries.get(0).action());
        assertEquals(DonationStatus.IN_TRANSIT, entries.get(0).previousStatus());
        assertEquals(DonationStatus.DELIVERED, entries.get(0).newStatus());
    }

    @Test
    void testMultipleEventsRecorded() {
        // Post
        auditLogger.onDonationPosted(new SystemEvent(
                SystemEvent.Type.DONATION_POSTED, donation, donorId, "DONOR", DonationStatus.PENDING_REVIEW
        ));
        
        // Claim
        donation.setState(new com.waslataam.state.AvailableState());
        auditLogger.onDonationClaimed(new SystemEvent(
                SystemEvent.Type.DONATION_CLAIMED, donation, recipientId, "RECIPIENT", DonationStatus.AVAILABLE
        ));
        
        // Deliver
        donation.claim(recipientId);
        donation.pickUp(UUID.randomUUID());
        auditLogger.onDonationDelivered(new SystemEvent(
                SystemEvent.Type.DONATION_DELIVERED, donation, UUID.randomUUID(), "VOLUNTEER", DonationStatus.IN_TRANSIT
        ));
        
        var entries = auditTrail.getEntries();
        assertEquals(3, entries.size());
    }

    @Test
    void testAuditTrailFiltering() {
        auditLogger.onDonationPosted(new SystemEvent(
                SystemEvent.Type.DONATION_POSTED, donation, donorId, "DONOR", DonationStatus.PENDING_REVIEW
        ));
        
        auditLogger.onDonationClaimed(new SystemEvent(
                SystemEvent.Type.DONATION_CLAIMED, donation, recipientId, "RECIPIENT", DonationStatus.AVAILABLE
        ));
        
        var byActor = auditTrail.getEntriesByActor(donorId);
        assertEquals(1, byActor.size());
        
        var byDonation = auditTrail.getEntriesByDonation(donation.getId());
        assertEquals(2, byDonation.size());
    }
}