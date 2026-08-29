package com.waslataam.chain;

import com.waslataam.domain.Donation;
import com.waslataam.domain.Location;

public class LocationCheckHandler extends VerificationHandler {
    private static final double MAX_DISTANCE_KM = 100.0;

    @Override
    protected VerificationResult doHandle(Donation donation) {
        Location location = donation.getLocation();
        if (location == null) {
            return VerificationResult.fail("Donation must have a valid location");
        }
        // In a real system, we'd check against recipient locations
        // For now, just validate the location object itself
        if (location.getLatitude() == 0.0 && location.getLongitude() == 0.0) {
            return VerificationResult.fail("Location coordinates appear invalid");
        }
        return VerificationResult.pass();
    }
}