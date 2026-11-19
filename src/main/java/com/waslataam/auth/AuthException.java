package com.waslataam.auth;

/**
 * A user-facing authentication failure. The message is safe to show in the UI;
 * it never contains a password or hash.
 */
public class AuthException extends Exception {

    public enum Reason {
        VALIDATION,
        EMAIL_TAKEN,
        INVALID_CREDENTIALS,
        LOCKED,
        ADMIN_RESTRICTED,
        ACCOUNT_DISABLED
    }

    private final Reason reason;

    public AuthException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
