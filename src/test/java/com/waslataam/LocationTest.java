package com.waslataam.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LocationTest {
    @Test
    void testValidLocation() {
        Location loc = new Location(24.8607, 67.0011, "Karachi", "Karachi");
        assertEquals(24.8607, loc.getLatitude());
        assertEquals(67.0011, loc.getLongitude());
        assertEquals("Karachi", loc.getAddress());
    }

    @Test
    void testInvalidLatitude() {
        assertThrows(IllegalArgumentException.class, () -> new Location(91, 0, "Test", "Test"));
        assertThrows(IllegalArgumentException.class, () -> new Location(-91, 0, "Test", "Test"));
    }

    @Test
    void testInvalidLongitude() {
        assertThrows(IllegalArgumentException.class, () -> new Location(0, 181, "Test", "Test"));
        assertThrows(IllegalArgumentException.class, () -> new Location(0, -181, "Test", "Test"));
    }

    @Test
    void testDistanceTo() {
        Location karachi = new Location(24.8607, 67.0011, "Karachi", "Karachi");
        Location lahore = new Location(31.5204, 74.3587, "Lahore", "Lahore");
        
        double distance = karachi.distanceTo(lahore);
        assertTrue(distance > 1000 && distance < 1500); // Approx 1025 km
    }

    @Test
    void testDistanceToNull() {
        Location loc = new Location(0, 0, "Test", "Test");
        assertEquals(Double.MAX_VALUE, loc.distanceTo(null));
    }
}