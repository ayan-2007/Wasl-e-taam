package com.waslataam.domain.exceptions;

public class IllegalStateTransitionException extends RuntimeException {
    public IllegalStateTransitionException(String message) {
        super(message);
    }

    public IllegalStateTransitionException(String message, Throwable cause) {
        super(message, cause);
    }
}