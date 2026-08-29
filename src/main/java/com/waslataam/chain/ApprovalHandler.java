package com.waslataam.chain;

import com.waslataam.domain.Donation;

public class ApprovalHandler extends VerificationHandler {
    @Override
    protected VerificationResult doHandle(Donation donation) {
        // In a real system, this might check donor reputation, require admin approval for large donations, etc.
        // For now, auto-approve everything that passes previous checks
        return VerificationResult.pass();
    }
}