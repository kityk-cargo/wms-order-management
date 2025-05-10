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
     * Handle all OrderManagementException types
     * This provides a single entry point for all domain-specific exceptions
     */
    @ExceptionHandler(OrderManagementException.class)
    public ResponseEntity<CommonErrorFormat> handleOrderManagementException(
            OrderManagementException ex, WebRequest request) {
        
        // Log different levels based on HTTP status
        if (ex.getStatus().is5xxServerError()) {
            logger.error("Order management error: {}", ex.getMessage(), ex);
        } else {
            logger.warn("Order management error: {}", ex.getMessage());
        }
        
        // Create error response with proper criticality
        CommonErrorFormat errorResponse = new CommonErrorFormat(ex.getCriticality(), ex.getMessage());
        
        // Add recovery suggestion if available
        if (ex.getRecoverySuggestion() != null && !ex.getRecoverySuggestion().isEmpty()) {
            CommonErrorFormat recoverySuggestion = CommonErrorFormat.nonCritical(
                    "Recovery suggestion: " + ex.getRecoverySuggestion());
            errorResponse.addOtherError(recoverySuggestion);
        }
        
        // Add specific handling for resource not found exception
        if (ex instanceof ResourceNotFoundException) {
            ResourceNotFoundException rnfEx = (ResourceNotFoundException) ex;
            String detail = String.format("%s not found with ID: %s", 
                rnfEx.getResourceType(), rnfEx.getResourceId());
            
            // Optional: Add more detailed information to error response
            if (errorResponse.getOtherErrors() == null) {
                CommonErrorFormat resourceDetail = CommonErrorFormat.nonCritical(detail);
                errorResponse.addOtherError(resourceDetail);
            }
        }
        
        return new ResponseEntity<>(errorResponse, ex.getStatus());
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
        
        // Add recovery suggestion
        CommonErrorFormat recoverySuggestion = CommonErrorFormat.nonCritical(
                "Recovery suggestion: Please check your input and correct the validation errors");
        errorResponse.addOtherError(recoverySuggestion);
        
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
     * Handle database integrity violations
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<CommonErrorFormat> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, WebRequest request) {
        
        logger.error("Data integrity violation: {}", ex.getMessage());
        
        CommonErrorFormat errorResponse = CommonErrorFormat.critical(
            "A data integrity constraint was violated"
        );
        
        // Add recovery suggestion
        CommonErrorFormat recoverySuggestion = CommonErrorFormat.nonCritical(
                "Recovery suggestion: Check that your data doesn't violate any unique constraints");
        errorResponse.addOtherError(recoverySuggestion);
        
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
        
        // Add recovery suggestion
        CommonErrorFormat recoverySuggestion = CommonErrorFormat.nonCritical(
                "Recovery suggestion: Please contact support if the problem persists");
        errorResponse.addOtherError(recoverySuggestion);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
} 