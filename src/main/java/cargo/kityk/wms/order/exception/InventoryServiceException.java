package cargo.kityk.wms.order.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when there's an issue communicating with the inventory service.
 * This could be due to network issues, service unavailability, or invalid responses.
 */
public class InventoryServiceException extends OrderManagementException {

    
    /**
     * Create a new inventory service exception with a custom HTTP status and recovery suggestion
     * 
     * @param message Error message
     * @param status HTTP status code to return
     * @param recoverySuggestion Suggestion for recovery
     */
    public InventoryServiceException(String message, HttpStatus status, String recoverySuggestion) {
        super(message, status, "critical", recoverySuggestion);
    }

    /**
     * Factory method for product not found in inventory
     * 
     * @param productId ID of the product that wasn't found
     * @return InventoryServiceException
     */
    public static InventoryServiceException productNotFound(Long productId) {
        return new InventoryServiceException(
            String.format("Product with ID %d not found in inventory", productId),
            HttpStatus.BAD_REQUEST,
            "Check that the product ID is correct and exists in the inventory"
        );
    }
    
    /**
     * Factory method for insufficient inventory
     * 
     * @param productId ID of the product with insufficient inventory
     * @param requested Quantity requested
     * @param available Quantity available
     * @return InventoryServiceException
     */
    public static InventoryServiceException insufficientInventory(Long productId, int requested, int available) {
        return new InventoryServiceException(
            String.format("Insufficient inventory for product %d. Requested: %d, Available: %d", 
                productId, requested, available),
            HttpStatus.CONFLICT,
            "Reduce the quantity ordered or wait for inventory to be restocked"
        );
    }
} 