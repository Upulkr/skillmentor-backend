package com.skillmentor.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception for when a resource is not found.
 * 
 * @ResponseStatus(HttpStatus.NOT_FOUND) tells Spring
 *                                       to automatically return a 404 status
 *                                       code when this
 *                                       exception is thrown.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
