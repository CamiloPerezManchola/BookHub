package usco.edu.co.BookHub.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger =
        LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 404 - Resource not found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(
            ResourceNotFoundException ex) {

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", ex.getMessage()));
    }

    // 400 - Invalid DTO fields
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.putIfAbsent(
                error.getField(),
                error.getDefaultMessage()
            )
        );

        return ResponseEntity
            .badRequest()
            .body(Map.of(
                "error", "Validation failed",
                "details", errors
            ));
    }

    // 400 - Invalid parameters
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(
            ConstraintViolationException ex) {

        return ResponseEntity
            .badRequest()
            .body(Map.of(
                "error", "Invalid request parameters."
            ));
    }

    // 400 - Invalid JSON
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleInvalidJson(
            HttpMessageNotReadableException ex) {

        return ResponseEntity
            .badRequest()
            .body(Map.of(
                "error", "Invalid or malformed request body."
            ));
    }

    // 400 - Invalid arguments (legacy handling)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(
            IllegalArgumentException ex) {

        return ResponseEntity
            .badRequest()
            .body(Map.of(
                "error", ex.getMessage()
            ));
    }

    // 409 - Duplicate resources
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<?> handleDuplicate(
            DuplicateResourceException ex) {

        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(Map.of(
                "error", ex.getMessage()
            ));
    }

    // 409 - Database integrity violation
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrity(
            DataIntegrityViolationException ex) {

        logger.warn("Database integrity violation", ex);

        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(Map.of(
                "error", "The operation conflicts with existing data."
            ));
    }

    // 403 - Forbidden
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(
            AccessDeniedException ex) {

        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(Map.of(
                "error", "You don't have permission to perform this action."
            ));
    }

    // 500 - Unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralException(
            Exception ex) {

        logger.error("Unexpected server error", ex);

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of(
                "error", "An unexpected error occurred."
            ));
    }
}