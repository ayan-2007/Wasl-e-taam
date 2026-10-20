package com.waslataam.state;

import com.waslataam.domain.Donation;
import com.waslataam.domain.DonationStatus;
import com.waslataam.domain.DonationType;
import com.waslataam.domain.PerishableDonation;
import com.waslataam.domain.exceptions.IllegalStateTransitionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DonationStateTest {
    private Donation donation;
    private UUID donorId;
    private UUID recipientId;
    private UUID volunteerId;

    @BeforeEach
    void setUp() {
        donorId = UUID.randomUUID();
        recipientId = UUID.randomUUID();
        volunteerId = UUID.randomUUID();
        
        donation = new PerishableDonation(
                UUID.randomUUID(), 10.0, "kg", donorId,
                LocalDateTime.now().plusDays(1), "2-8°C"
        );
    }

    @Test
    void testPendingReviewToAvailable() {
        // Initially in PENDING_REVIEW
        assertEquals(DonationStatus.PENDING_REVIEW, donation.getStatus());
        
        // Reject it (simulating approval flow)
        donation.reject("Test");
        assertEquals(DonationStatus.REJECTED, donation.getStatus());
    }

    @Test
    void testAvailableToClaimed() {
        donation.setState(new AvailableState());
        assertEquals(DonationStatus.AVAILABLE, donation.getStatus());
        
        donation.claim(recipientId);
        assertEquals(DonationStatus.CLAIMED, donation.getStatus());
        assertEquals(recipientId, donation.getRecipientId());
    }

    @Test
    void testClaimedToAvailable() {
        donation.setState(new AvailableState());
        donation.claim(recipientId);
        assertEquals(DonationStatus.CLAIMED, donation.getStatus());
        
        donation.cancelClaim();
        assertEquals(DonationStatus.AVAILABLE, donation.getStatus());
        assertNull(donation.getRecipientId());
    }

    @Test
    void testClaimedToInTransit() {
        donation.setState(new AvailableState());
        donation.claim(recipientId);
        
        donation.pickUp(volunteerId);
        assertEquals(DonationStatus.IN_TRANSIT, donation.getStatus());
        assertEquals(volunteerId, donation.getVolunteerId());
    }

    @Test
    void testInTransitToDelivered() {
        donation.setState(new AvailableState());
        donation.claim(recipientId);
        donation.pickUp(volunteerId);
        
        donation.deliver();
        assertEquals(DonationStatus.DELIVERED, donation.getStatus());
    }

    @Test
    void testAvailableToExpired() {
        donation.setState(new AvailableState());
        donation.expire();
        assertEquals(DonationStatus.EXPIRED, donation.getStatus());
    }

    @Test
    void testIllegalTransitions() {
        // Can't claim from PENDING_REVIEW
        assertThrows(IllegalStateTransitionException.class, () -> donation.claim(recipientId));
        
        // Can't pick up from AVAILABLE
        donation.setState(new AvailableState());
        assertThrows(IllegalStateTransitionException.class, () -> donation.pickUp(volunteerId));
        
        // Can't deliver from CLAIMED
        donation.claim(recipientId);
        assertThrows(IllegalStateTransitionException.class, () -> donation.deliver());
        
        // Can't claim DELIVERED
        donation.setState(new AvailableState());
        donation.claim(recipientId);
        donation.pickUp(volunteerId);
        donation.deliver();
        assertThrows(IllegalStateTransitionException.class, () -> donation.claim(recipientId));
    }
}