package com.waslataam.chain;

import com.waslataam.domain.Donation;

public class ExpiryCheckHandler extends VerificationHandler {
    @Override
    protected VerificationResult doHandle(Donation donation) {
        if (donation.isExpired()) {
            return VerificationResult.fail("Donation has already expired");
        }
        return VerificationResult.pass();
    }
}