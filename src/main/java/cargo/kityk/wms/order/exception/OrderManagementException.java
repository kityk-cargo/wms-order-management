package cargo.kityk.wms.order.exception;

import org.springframework.http.HttpStatus;

/**
 * Root exception for all order management service exceptions.
 * This serves as the base class for all custom exceptions in the order management domain.
 * 
 * It provides:
 * - HTTP status code handling
 * - Error criticality levels
 * - Recovery suggestion messages
 */
public class OrderManagementException extends RuntimeException {
    
    private final HttpStatus status;
    private final String criticality;
    private final String recoverySuggestion;
    
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
        this.criticality = criticality;
        this.recoverySuggestion = recoverySuggestion;
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
        this.criticality = criticality;
        this.recoverySuggestion = recoverySuggestion;
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
        return criticality;
    }
    
    /**
     * Get recovery suggestion if available
     * 
     * @return Recovery suggestion or null
     */
    public String getRecoverySuggestion() {
        return recoverySuggestion;
    }
} 