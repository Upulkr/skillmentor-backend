package com.skillmentor.exception;

import com.skillmentor.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * GLOBAL EXCEPTION HANDLER WITH PROPER LOGGING
 *
 * @Slf4j - Automatically creates 'log' field
 *
 * This handler addresses ALL 4 W's:
 * 1. WHAT - What error occurred?
 * 2. WHERE - Which endpoint/class?
 * 3. WHEN - What time?
 * 4. WHY - Root cause/reason?
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * VALIDATION ERROR HANDLER (400 Bad Request)
     *
     * Example:
     * User sends: POST /api/v1/sessions/enroll with mentorId=null
     * Error: @NotNull validation fails
     * This handler catches it
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request
    ) {
        // Extract validation errors
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.put(
                        error.getField(),
                        error.getDefaultMessage()
                ));

        // Get request details
        String requestPath = request.getDescription(false).replace("uri=", "");

        /**
         * LOG THE ERROR - Answers all 4 W's
         *
         * WHAT: "Validation failed"
         * WHERE: Log the endpoint path and class
         * WHEN: Log the current timestamp
         * WHY: Include the field errors
         */
        log.warn(
                "VALIDATION ERROR | " +
                        "WHAT: Validation failed on request | " +
                        "WHERE: {} | " +
                        "WHEN: {} | " +
                        "WHY: {}",
                requestPath,
                LocalDateTime.now(),
                fieldErrors
        );

        // Build error response
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message("Validation failed")
                .path(requestPath)
                .errors(fieldErrors)
                .traceId("ERR_" + UUID.randomUUID())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * RESOURCE NOT FOUND HANDLER (404)
     *
     * Example:
     * User requests: GET /api/v1/mentors/999
     * Mentor with ID 999 doesn't exist
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            WebRequest request
    ) {
        String requestPath = request.getDescription(false).replace("uri=", "");

        /**
         * LOG WITH 4 W's
         *
         * WHAT: "Resource not found"
         * WHERE: Path and class name
         * WHEN: Timestamp
         * WHY: Exception message explains why (e.g., "Mentor not found")
         */
        log.warn(
                "RESOURCE NOT FOUND | " +
                        "WHAT: Resource lookup failed | " +
                        "WHERE: {} | " +
                        "WHEN: {} | " +
                        "WHY: {} | " +
                        "TraceID: {}",
                requestPath,
                LocalDateTime.now(),
                ex.getMessage(),
                UUID.randomUUID()
        );

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .path(requestPath)
                .traceId("ERR_" + UUID.randomUUID())
                .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    /**
     * DOUBLE BOOKING EXCEPTION HANDLER (409 Conflict)
     *
     * Example:
     * Student tries to book mentor at already-booked time
     */
    @ExceptionHandler(DoubleBookingException.class)
    public ResponseEntity<ErrorResponse> handleDoubleBookingException(
            DoubleBookingException ex,
            WebRequest request
    ) {
        String requestPath = request.getDescription(false).replace("uri=", "");

        /**
         * LOG WITH 4 W's AND EXTRA CONTEXT
         *
         * This is a critical business error, so we log more details
         */
        log.error(
                "DOUBLE BOOKING ATTEMPT | " +
                        "WHAT: Mentor availability conflict | " +
                        "WHERE: {} | " +
                        "WHEN: {} | " +
                        "WHY: {} | " +
                        "SEVERITY: CRITICAL | " +
                        "TraceID: {}",
                requestPath,
                LocalDateTime.now(),
                ex.getMessage(),
                UUID.randomUUID()
        );

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Conflict")
                .message(ex.getMessage())
                .path(requestPath)
                .traceId("ERR_" + UUID.randomUUID())
                .build();

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    /**
     * UNAUTHORIZED HANDLER (403 Forbidden)
     *
     * Example:
     * Student tries to access /admin/create-subject
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
            UnauthorizedException ex,
            WebRequest request
    ) {
        String requestPath = request.getDescription(false).replace("uri=", "");

        /**
         * LOG SECURITY ISSUE - Log at WARN level
         *
         * Security violations should always be logged
         * They indicate potential security attacks
         */
        log.warn(
                "UNAUTHORIZED ACCESS ATTEMPT | " +
                        "WHAT: Permission denied | " +
                        "WHERE: {} | " +
                        "WHEN: {} | " +
                        "WHY: {} | " +
                        "SECURITY_LEVEL: HIGH",
                requestPath,
                LocalDateTime.now(),
                ex.getMessage()
        );

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Forbidden")
                .message(ex.getMessage())
                .path(requestPath)
                .traceId("ERR_" + UUID.randomUUID())
                .build();

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(errorResponse);
    }

    /**
     * INVALID STATE TRANSITION HANDLER (400)
     *
     * Example:
     * Session is COMPLETED, admin tries to confirm payment again
     */
    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStateTransitionException(
            InvalidStateTransitionException ex,
            WebRequest request
    ) {
        String requestPath = request.getDescription(false).replace("uri=", "");

        /**
         * LOG BUSINESS LOGIC ERROR
         *
         * This indicates incorrect workflow
         * Should be logged for monitoring
         */
        log.warn(
                "INVALID STATE TRANSITION | " +
                        "WHAT: Workflow violation | " +
                        "WHERE: {} | " +
                        "WHEN: {} | " +
                        "WHY: {} | " +
                        "ACTION: Review workflow logic",
                requestPath,
                LocalDateTime.now(),
                ex.getMessage()
        );

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .path(requestPath)
                .traceId("ERR_" + UUID.randomUUID())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * CATCH-ALL EXCEPTION HANDLER (500)
     *
     * Any unexpected error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            WebRequest request
    ) {
        String requestPath = request.getDescription(false).replace("uri=", "");
        String traceId = "ERR_" + UUID.randomUUID();

        /**
         * LOG CRITICAL ERROR WITH FULL STACK TRACE
         *
         * WHAT: Unexpected error
         * WHERE: Class name, method name
         * WHEN: Current timestamp
         * WHY: Full stack trace for debugging
         */
        log.error(
                "UNEXPECTED SERVER ERROR | " +
                        "WHAT: Unhandled exception occurred | " +
                        "WHERE: {} | " +
                        "WHEN: {} | " +
                        "WHY: {} | " +
                        "STACK_TRACE_ID: {} | " +
                        "SEVERITY: CRITICAL | " +
                        "ACTION: Review logs immediately",
                requestPath,
                LocalDateTime.now(),
                ex.getMessage(),
                traceId,
                ex  // This logs the full stack trace
        );

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("Something went wrong. Please try again later.")
                .path(requestPath)
                .traceId(traceId)
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }
}
