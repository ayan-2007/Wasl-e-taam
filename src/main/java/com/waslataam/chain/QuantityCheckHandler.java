package com.waslataam.chain;

import com.waslataam.domain.Donation;

public class QuantityCheckHandler extends VerificationHandler {
    private static final double MIN_QUANTITY = 0.1;
    private static final double MAX_QUANTITY = 10000.0;

    @Override
    protected VerificationResult doHandle(Donation donation) {
        if (donation.getQuantity() < MIN_QUANTITY) {
            return VerificationResult.fail("Quantity must be at least " + MIN_QUANTITY);
        }
        if (donation.getQuantity() > MAX_QUANTITY) {
            return VerificationResult.fail("Quantity cannot exceed " + MAX_QUANTITY);
        }
        return VerificationResult.pass();
    }
}