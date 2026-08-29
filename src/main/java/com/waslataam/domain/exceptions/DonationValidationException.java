package com.waslataam.domain.exceptions;

public class DonationValidationException extends RuntimeException {
    public DonationValidationException(String message) {
        super(message);
    }

    public DonationValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}