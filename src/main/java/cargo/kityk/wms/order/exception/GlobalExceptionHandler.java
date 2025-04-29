package cargo.kityk.wms.order.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for the application.
 * Provides consistent error responses across all controllers using the Common Error Format.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle resource not found exceptions
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<CommonErrorFormat> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        
        logger.warn("Resource not found: {}", ex.getMessage());
        
        String detail = String.format("%s not found with ID: %s", 
            ex.getResourceType(), ex.getResourceId());
        
        CommonErrorFormat errorResponse = CommonErrorFormat.critical(detail);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Handle validation exceptions with proper separation of errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonErrorFormat> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        logger.warn("Validation error: {}", ex.getMessage());
        
        // Get all field errors
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        
        // Create main error message
        String mainErrorDetail = "Multiple validation errors occurred";
        if (fieldErrors.size() == 1) {
            // If there's only one error, use it as the main error
            FieldError error = fieldErrors.get(0);
            mainErrorDetail = String.format("Validation error for field '%s': %s", 
                    error.getField(), error.getDefaultMessage());
        }
        
        // Create the main error response
        CommonErrorFormat errorResponse = CommonErrorFormat.critical(mainErrorDetail);
        
        // If there are multiple errors, add them as other errors
        if (fieldErrors.size() > 1) {
            // Create individual CommonErrorFormat objects for each validation error
            List<CommonErrorFormat> validationErrors = fieldErrors.stream()
                .map(error -> {
                    String errorDetail = String.format("Validation error for field '%s': %s", 
                            error.getField(), error.getDefaultMessage());
                    return CommonErrorFormat.critical(errorDetail);
                })
                .collect(Collectors.toList());
            
            // Add them to the main error
            errorResponse.addOtherErrors(validationErrors);
        }
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle invalid order exceptions
     */
    @ExceptionHandler(InvalidOrderException.class)
    public ResponseEntity<CommonErrorFormat> handleInvalidOrderException(
            InvalidOrderException ex, WebRequest request) {
        
        logger.warn("Invalid order: {}", ex.getMessage());
        
        CommonErrorFormat errorResponse = CommonErrorFormat.critical(ex.getMessage());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle database integrity violations
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<CommonErrorFormat> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, WebRequest request) {
        
        logger.error("Data integrity violation: {}", ex.getMessage());
        
        CommonErrorFormat errorResponse = CommonErrorFormat.critical(
            "A data integrity constraint was violated"
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Catch-all handler for any uncaught exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonErrorFormat> handleGenericException(
            Exception ex, WebRequest request) {
        
        logger.error("Unhandled exception: ", ex);
        
        CommonErrorFormat errorResponse = CommonErrorFormat.critical("Internal server error");
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
} 