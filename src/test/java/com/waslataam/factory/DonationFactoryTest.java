package com.waslataam.factory;

import com.waslataam.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DonationFactoryTest {
    private DonationFactory factory;
    private UUID donorId;
    private Location location;

    @BeforeEach
    void setUp() {
        factory = new DonationFactory();
        donorId = UUID.randomUUID();
        location = new Location(24.8607, 67.0011, "Karachi", "Karachi");
    }

    @Test
    void testCreatePerishableDonation() {
        DonationParams params = DonationParams.builder()
                .type(DonationType.PERISHABLE)
                .quantity(10.0)
                .unit("kg")
                .donorId(donorId)
                .location(location)
                .expiryTime(LocalDateTime.now().plusDays(1))
                .temperatureRequirement("2-8°C")
                .build();

        Donation donation = factory.create(params);
        
        assertNotNull(donation);
        assertInstanceOf(PerishableDonation.class, donation);
        assertEquals(DonationType.PERISHABLE, donation.getType());
        assertEquals(10.0, donation.getQuantity());
        assertEquals("kg", donation.getUnit());
        assertEquals(donorId, donation.getDonorId());
        
        PerishableDonation pd = (PerishableDonation) donation;
        assertEquals("2-8°C", pd.getTemperatureRequirement());
    }

    @Test
    void testCreateDryFoodDonation() {
        DonationParams params = DonationParams.builder()
                .type(DonationType.DRY_FOOD)
                .quantity(100.0)
                .unit("kg")
                .donorId(donorId)
                .location(location)
                .shelfLifeDays(365)
                .build();

        Donation donation = factory.create(params);
        
        assertNotNull(donation);
        assertInstanceOf(DryFoodDonation.class, donation);
        assertEquals(DonationType.DRY_FOOD, donation.getType());
        
        DryFoodDonation dfd = (DryFoodDonation) donation;
        assertEquals(365, dfd.getShelfLifeDays());
    }

    @Test
    void testCreatePreparedMealDonation() {
        DonationParams params = DonationParams.builder()
                .type(DonationType.PREPARED_MEAL)
                .quantity(50.0)
                .unit("meals")
                .donorId(donorId)
                .location(location)
                .servings(50)
                .cookedAt(LocalDateTime.now().minusHours(2))
                .build();

        Donation donation = factory.create(params);
        
        assertNotNull(donation);
        assertInstanceOf(PreparedMealDonation.class, donation);
        assertEquals(DonationType.PREPARED_MEAL, donation.getType());
        
        PreparedMealDonation pmd = (PreparedMealDonation) donation;
        assertEquals(50, pmd.getServings());
    }

    @Test
    void testPerishableMissingExpiryThrows() {
        DonationParams params = DonationParams.builder()
                .type(DonationType.PERISHABLE)
                .quantity(10.0)
                .unit("kg")
                .donorId(donorId)
                .location(location)
                .temperatureRequirement("2-8°C")
                .build();

        assertThrows(IllegalArgumentException.class, () -> factory.create(params));
    }

    @Test
    void testPerishableMissingTempThrows() {
        DonationParams params = DonationParams.builder()
                .type(DonationType.PERISHABLE)
                .quantity(10.0)
                .unit("kg")
                .donorId(donorId)
                .location(location)
                .expiryTime(LocalDateTime.now().plusDays(1))
                .build();

        assertThrows(IllegalArgumentException.class, () -> factory.create(params));
    }

    @Test
    void testDryFoodInvalidShelfLifeThrows() {
        DonationParams params = DonationParams.builder()
                .type(DonationType.DRY_FOOD)
                .quantity(10.0)
                .unit("kg")
                .donorId(donorId)
                .location(location)
                .shelfLifeDays(0)
                .build();

        assertThrows(IllegalArgumentException.class, () -> factory.create(params));
    }

    @Test
    void testPreparedMealInvalidServingsThrows() {
        DonationParams params = DonationParams.builder()
                .type(DonationType.PREPARED_MEAL)
                .quantity(10.0)
                .unit("meals")
                .donorId(donorId)
                .location(location)
                .servings(0)
                .cookedAt(LocalDateTime.now().minusHours(1))
                .build();

        assertThrows(IllegalArgumentException.class, () -> factory.create(params));
    }
}