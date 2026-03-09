package com.skillmentor.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception for double-booking conflicts.
 * Returns 409 Conflict.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DoubleBookingException extends RuntimeException {
    public DoubleBookingException(String message) {
        super(message);
    }
}
