package com.skillmentor.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception for invalid state transitions.
 * Thrown when an action is attempted on a resource that is not in the correct
 * state.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidStateTransitionException extends RuntimeException {
    public InvalidStateTransitionException(String message) {
        super(message);
    }
}
