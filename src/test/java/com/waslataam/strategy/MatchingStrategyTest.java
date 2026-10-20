package com.waslataam.strategy;

import com.waslataam.domain.Donation;
import com.waslataam.domain.DonationType;
import com.waslataam.domain.Location;
import com.waslataam.domain.PerishableDonation;
import com.waslataam.domain.Recipient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MatchingStrategyTest {
    private Donation donation;
    private Recipient recipient1;
    private Recipient recipient2;
    private Recipient recipient3;
    private UUID donorId;

    @BeforeEach
    void setUp() {
        donorId = UUID.randomUUID();
        
        Location donationLoc = new Location(24.8607, 67.0011, "Karachi", "Karachi");
        donation = new PerishableDonation(
                UUID.randomUUID(), 50.0, "kg", donorId,
                LocalDateTime.now().plusDays(1), "2-8°C"
        );
        donation.setLocation(donationLoc);

        // Recipient 1: Near, low capacity
        Location loc1 = new Location(24.9000, 67.0500, "Karachi East", "Karachi");
        recipient1 = new Recipient(UUID.randomUUID(), "Recipient 1", "r1@test.com", loc1, 100, 0.9);

        // Recipient 2: Far, high capacity
        Location loc2 = new Location(31.5204, 74.3587, "Lahore", "Lahore");
        recipient2 = new Recipient(UUID.randomUUID(), "Recipient 2", "r2@test.com", loc2, 1000, 0.8);

        // Recipient 3: Medium distance, medium capacity
        Location loc3 = new Location(25.0000, 67.1000, "Karachi North", "Karachi");
        recipient3 = new Recipient(UUID.randomUUID(), "Recipient 3", "r3@test.com", loc3, 500, 0.95);
    }

    @Test
    void testNearestFirstStrategy() {
        NearestFirstStrategy strategy = new NearestFirstStrategy();
        var result = strategy.findBestMatch(donation, List.of(recipient1, recipient2, recipient3));
        
        assertTrue(result.isPresent());
        assertEquals(recipient1.getId(), result.get().getId());
    }

    @Test
    void testExpiryFirstStrategy() {
        ExpiryFirstStrategy strategy = new ExpiryFirstStrategy();
        var result = strategy.findBestMatch(donation, List.of(recipient1, recipient2, recipient3));
        
        // All recipients can accept, so it should pick the first one (recipient1)
        assertTrue(result.isPresent());
    }

    @Test
    void testCapacityBasedStrategy() {
        CapacityBasedStrategy strategy = new CapacityBasedStrategy();
        var result = strategy.findBestMatch(donation, List.of(recipient1, recipient2, recipient3));
        
        assertTrue(result.isPresent());
        // Recipient 2 has highest remaining capacity (1000 - 0 = 1000)
        assertEquals(recipient2.getId(), result.get().getId());
    }

    @Test
    void testNoMatchingRecipient() {
        NearestFirstStrategy strategy = new NearestFirstStrategy();
        
        // Create recipient with insufficient capacity
        Location loc = new Location(24.8607, 67.0011, "Karachi", "Karachi");
        Recipient smallRecipient = new Recipient(UUID.randomUUID(), "Small", "small@test.com", loc, 10, 0.9);
        
        var result = strategy.findBestMatch(donation, List.of(smallRecipient));
        assertFalse(result.isPresent());
    }

    @Test
    void testInactiveRecipientFiltered() {
        NearestFirstStrategy strategy = new NearestFirstStrategy();
        
        Location loc = new Location(24.8607, 67.0011, "Karachi", "Karachi");
        Recipient inactive = new Recipient(UUID.randomUUID(), "Inactive", "inactive@test.com", loc, 1000, 0.9);
        inactive.setActive(false);
        
        var result = strategy.findBestMatch(donation, List.of(inactive, recipient1));
        assertTrue(result.isPresent());
        assertEquals(recipient1.getId(), result.get().getId());
    }
}