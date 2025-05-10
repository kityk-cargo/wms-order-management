package cargo.kityk.wms.order.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends OrderManagementException {
    
    private final String resourceType;
    private final Object resourceId;
    
    /**
     * Create a new resource not found exception
     * 
     * @param resourceType Type of resource (e.g., "Order", "Customer")
     * @param resourceId Identifier of the resource
     */
    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super(
            String.format("%s not found with ID: %s", resourceType, resourceId),
            HttpStatus.NOT_FOUND,
            "critical",
            String.format("Check if the %s ID exists or create a new %s", resourceType.toLowerCase(), resourceType.toLowerCase())
        );
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    /**
     * Get the type of resource that was not found
     * 
     * @return Resource type
     */
    public String getResourceType() {
        return resourceType;
    }
    
    /**
     * Get the identifier of the resource that was not found
     * 
     * @return Resource identifier
     */
    public Object getResourceId() {
        return resourceId;
    }
} 