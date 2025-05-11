package cargo.kityk.wms.order.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

/**
 * Root exception for all order management service exceptions.
 * This serves as the base class for all custom exceptions in the order management domain.
 * 
 * It provides:
 * - HTTP status code handling
 * - Error criticality levels
 * - Recovery suggestion messages
 * - Error IDs for tracing through logs
 */
@Slf4j
public class OrderManagementException extends RuntimeException {
    
    private final HttpStatus status;
    private final CommonErrorFormat errorFormat;
    
    /**
     * Create a new order management exception
     * 
     * @param message Error message
     * @param status HTTP status code to return
     * @param criticality Error criticality level (critical, non-critical, unknown)
     * @param recoverySuggestion Optional suggestion for recovery
     */
    public OrderManagementException(String message, HttpStatus status, String criticality, String recoverySuggestion) {
        super(message);
        this.status = status;
        this.errorFormat = new CommonErrorFormat(criticality, message);
        
        if (recoverySuggestion != null && !recoverySuggestion.isEmpty()) {
            CommonErrorFormat recovery = new CommonErrorFormat("non-critical", 
                "Recovery suggestion: " + recoverySuggestion);
            this.errorFormat.addOtherError(recovery);
        }
    }
    
    /**
     * Create a new order management exception
     * 
     * @param message Error message
     * @param cause Root cause exception
     * @param status HTTP status code to return
     * @param criticality Error criticality level (critical, non-critical, unknown)
     * @param recoverySuggestion Optional suggestion for recovery
     */
    public OrderManagementException(String message, Throwable cause, HttpStatus status, String criticality, String recoverySuggestion) {
        super(message, cause);
        this.status = status;
        
        // If the cause is also an OrderManagementException, preserve its error ID
        if (cause instanceof OrderManagementException) {
            //todo re-form this log and monitor this as a potential issue -- must not happen ideally
            log.error("SEVERE: INSTANCE OF THE INTERNAL EXCEPTION WAS RE-WRAPPED WHICH MUST NOT HAPPEN", cause);
            OrderManagementException orderEx = (OrderManagementException) cause;
            this.errorFormat = new CommonErrorFormat(criticality, message, orderEx.getErrorId());
        } else {
            // Create a new error format with a new ID
            this.errorFormat = new CommonErrorFormat(criticality, message);
        }
        
        if (recoverySuggestion != null && !recoverySuggestion.isEmpty()) {
            CommonErrorFormat recovery = new CommonErrorFormat("non-critical", 
                "Recovery suggestion: " + recoverySuggestion);
            this.errorFormat.addOtherError(recovery);
        }
    }
    
    /**
     * Get HTTP status that should be returned
     * 
     * @return HTTP status
     */
    public HttpStatus getStatus() {
        return status;
    }
    
    /**
     * Get error criticality (critical, non-critical, unknown)
     * 
     * @return Error criticality
     */
    public String getCriticality() {
        return errorFormat.getCriticality();
    }
    
    /**
     * Get the error ID for tracing
     * 
     * @return Error ID as a string
     */
    public String getErrorId() {
        return errorFormat.getId();
    }
    
    /**
     * Get the underlying error format
     * 
     * @return CommonErrorFormat
     */
    public CommonErrorFormat getErrorFormat() {
        return errorFormat;
    }
} 