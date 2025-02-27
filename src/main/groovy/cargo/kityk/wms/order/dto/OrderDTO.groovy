package cargo.kityk.wms.order.dto

import groovy.transform.Canonical
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.ArraySchema
import java.time.ZonedDateTime
import java.util.ArrayList

@Canonical
class OrderDTO {
    @Schema(description = "Order ID", example = "1")
    Long id
    
    @Schema(description = "Customer ID", example = "1")
    Long customerId
    
    @Schema(description = "Customer information")
    CustomerDTO customer
    
    @Schema(description = "Date and time when order was placed", example = "2023-07-15T10:30:00Z")
    ZonedDateTime orderDate
    
    @Schema(description = "Current order status", example = "Processing", allowableValues = ["Pending", "Allocated", "Processing", "Shipped", "Delivered", "Cancelled"])
    String status
    
    @Schema(description = "Total order amount", example = "149.95")
    BigDecimal totalAmount
    
    @ArraySchema(
        schema = @Schema(implementation = OrderItemDTO.class),
        arraySchema = @Schema(description = "Items in the order")
    )
    private List<OrderItemDTO> items = []
    
    @Schema(description = "Date and time when order record was created", example = "2023-07-15T10:30:00Z")
    ZonedDateTime createdAt
    
    @Schema(description = "Date and time when order record was last updated", example = "2023-07-15T10:30:00Z")
    ZonedDateTime updatedAt
    
    // Defensive getter for items
    List<OrderItemDTO> getItems() {
        return items ? new ArrayList<>(items) : []
    }
    
    // Defensive setter for items
    void setItems(List<OrderItemDTO> items) {
        this.items = items ? new ArrayList<>(items) : []
    }
} 