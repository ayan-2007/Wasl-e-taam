package com.waslataam.chain;

import com.waslataam.domain.Donation;
import com.waslataam.domain.DonationType;
import com.waslataam.domain.Location;
import com.waslataam.domain.PerishableDonation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VerificationChainTest {
    private VerificationHandler chain;
    private UUID donorId;

    @BeforeEach
    void setUp() {
        donorId = UUID.randomUUID();
        
        // Build chain: Expiry -> Quantity -> Location -> Approval
        VerificationHandler expiryCheck = new ExpiryCheckHandler();
        VerificationHandler quantityCheck = new QuantityCheckHandler();
        VerificationHandler locationCheck = new LocationCheckHandler();
        VerificationHandler approval = new ApprovalHandler();
        
        expiryCheck.setNext(quantityCheck).setNext(locationCheck).setNext(approval);
        chain = expiryCheck;
    }

    @Test
    void testValidDonationPasses() {
        Donation donation = new PerishableDonation(
                UUID.randomUUID(), 10.0, "kg", donorId,
                LocalDateTime.now().plusDays(1), "2-8°C"
        );
        donation.setLocation(new Location(24.8607, 67.0011, "Karachi", "Karachi"));
        
        VerificationResult result = chain.handle(donation);
        assertTrue(result.isPassed());
    }

    @Test
    void testExpiredDonationFails() {
        Donation donation = new PerishableDonation(
                UUID.randomUUID(), 10.0, "kg", donorId,
                LocalDateTime.now().minusDays(1), "2-8°C" // Expired
        );
        donation.setLocation(new Location(24.8607, 67.0011, "Karachi", "Karachi"));
        
        VerificationResult result = chain.handle(donation);
        assertFalse(result.isPassed());
        assertTrue(result.getReason().contains("expired"));
    }

    @Test
    void testZeroQuantityFails() {
        Donation donation = new PerishableDonation(
                UUID.randomUUID(), 0.0, "kg", donorId, // Zero quantity
                LocalDateTime.now().plusDays(1), "2-8°C"
        );
        donation.setLocation(new Location(24.8607, 67.0011, "Karachi", "Karachi"));
        
        VerificationResult result = chain.handle(donation);
        assertFalse(result.isPassed());
        assertTrue(result.getReason().contains("Quantity must be at least"));
    }

    @Test
    void testNegativeQuantityFails() {
        Donation donation = new PerishableDonation(
                UUID.randomUUID(), -5.0, "kg", donorId,
                LocalDateTime.now().plusDays(1), "2-8°C"
        );
        donation.setLocation(new Location(24.8607, 67.0011, "Karachi", "Karachi"));
        
        VerificationResult result = chain.handle(donation);
        assertFalse(result.isPassed());
    }

    @Test
    void testExcessiveQuantityFails() {
        Donation donation = new PerishableDonation(
                UUID.randomUUID(), 15000.0, "kg", donorId, // Over max
                LocalDateTime.now().plusDays(1), "2-8°C"
        );
        donation.setLocation(new Location(24.8607, 67.0011, "Karachi", "Karachi"));
        
        VerificationResult result = chain.handle(donation);
        assertFalse(result.isPassed());
        assertTrue(result.getReason().contains("cannot exceed"));
    }

    @Test
    void testInvalidLocationFails() {
        Donation donation = new PerishableDonation(
                UUID.randomUUID(), 10.0, "kg", donorId,
                LocalDateTime.now().plusDays(1), "2-8°C"
        );
        // Location with zero coordinates (invalid)
        donation.setLocation(new Location(0.0, 0.0, "Invalid", "Invalid"));
        
        VerificationResult result = chain.handle(donation);
        assertFalse(result.isPassed());
        assertTrue(result.getReason().contains("invalid"));
    }

    @Test
    void testChainStopsAtFirstFailure() {
        // Expired donation should fail at first handler (ExpiryCheck)
        Donation donation = new PerishableDonation(
                UUID.randomUUID(), -5.0, "kg", donorId, // Also negative quantity
                LocalDateTime.now().minusDays(1), "2-8°C"
        );
        donation.setLocation(new Location(24.8607, 67.0011, "Karachi", "Karachi"));
        
        VerificationResult result = chain.handle(donation);
        assertFalse(result.isPassed());
        // Should fail at expiry check (first in chain), not quantity check
        assertTrue(result.getReason().contains("expired"));
    }
}