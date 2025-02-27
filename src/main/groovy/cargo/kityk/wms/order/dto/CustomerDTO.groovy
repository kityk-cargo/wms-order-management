package cargo.kityk.wms.order.dto

import groovy.transform.Canonical
import io.swagger.v3.oas.annotations.media.Schema

@Canonical
class CustomerDTO {
    @Schema(description = "Customer ID", example = "1")
    Long id
    
    @Schema(description = "Customer name", example = "John Smith")
    String name
    
    @Schema(description = "Customer email address", example = "john.smith@example.com")
    String email
    
    @Schema(description = "Customer phone number", example = "+1-555-123-4567")
    String phone
    
    @Schema(description = "Customer shipping address", example = "123 Main St, Springfield, IL 62701")
    String address
} 