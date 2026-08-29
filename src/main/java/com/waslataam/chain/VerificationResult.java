package com.waslataam.chain;

public class VerificationResult {
    private final boolean passed;
    private final String reason;

    private VerificationResult(boolean passed, String reason) {
        this.passed = passed;
        this.reason = reason;
    }

    public static VerificationResult pass() {
        return new VerificationResult(true, null);
    }

    public static VerificationResult fail(String reason) {
        return new VerificationResult(false, reason);
    }

    public boolean isPassed() {
        return passed;
    }

    public String getReason() {
        return reason;
    }
}