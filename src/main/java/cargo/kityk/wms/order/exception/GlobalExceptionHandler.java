package cargo.kityk.wms.order.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * Global exception handler for the application.
 * Provides consistent error responses across all controllers.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle resource not found exceptions
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ServiceErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        
        logger.warn("Resource not found: {}", ex.getMessage());
        
        ServiceErrorResponse errorResponse = new ServiceErrorResponse(
            String.format("%s not found", ex.getResourceType()), 
            HttpStatus.NOT_FOUND.value()
        );
        
        if (ex.getResourceId() != null) {
            errorResponse.addDetail(ex.getResourceType().toLowerCase() + "Id", ex.getResourceId());
        }
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Handle validation exceptions
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ServiceErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        logger.warn("Validation error: {}", ex.getMessage());
        
        ServiceErrorResponse errorResponse = new ServiceErrorResponse(
            "Validation error", 
            HttpStatus.BAD_REQUEST.value()
        );
        
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errorResponse.addDetail(error.getField(), error.getDefaultMessage());
        });
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle database integrity violations
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ServiceErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, WebRequest request) {
        
        logger.error("Data integrity violation: {}", ex.getMessage());
        
        ServiceErrorResponse errorResponse = new ServiceErrorResponse(
            "Data integrity violation", 
            HttpStatus.BAD_REQUEST.value()
        );
        
        // Don't expose internal database details
        // Instead, provide a generic message
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Catch-all handler for any uncaught exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ServiceErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {
        
        logger.error("Unhandled exception: ", ex);
        
        ServiceErrorResponse errorResponse = new ServiceErrorResponse(
            "Internal server error", 
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
} 