package cargo.kityk.wms.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an order is invalid.
 * For example, when it contains products that don't exist in inventory.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidOrderException extends OrderManagementException {

    /**
     * Create a new invalid order exception
     * 
     * @param message Error message
     */
    public InvalidOrderException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "critical", "Correct the order data and try again");
    }
} 