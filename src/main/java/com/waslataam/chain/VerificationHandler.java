package com.waslataam.chain;

import com.waslataam.domain.Donation;

public abstract class VerificationHandler {
    private VerificationHandler next;

    public VerificationHandler setNext(VerificationHandler next) {
        this.next = next;
        return next;
    }

    public final VerificationResult handle(Donation donation) {
        VerificationResult result = doHandle(donation);
        if (!result.isPassed() || next == null) {
            return result;
        }
        return next.handle(donation);
    }

    protected abstract VerificationResult doHandle(Donation donation);
}