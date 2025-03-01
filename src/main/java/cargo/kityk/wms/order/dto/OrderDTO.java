package cargo.kityk.wms.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    @Schema(description = "Order ID", example = "1")
    private Long id;
    
    @Schema(description = "Order reference number", example = "ORD-2023-12345")
    private String orderReference;
    
    @Schema(description = "Customer ID", example = "1")
    private Long customerId;
    
    @Schema(description = "Customer information")
    private CustomerDTO customer;
    
    @Schema(description = "Shipping address", example = "123 Main St")
    private String shippingAddress;
    
    @Schema(description = "Shipping city", example = "New York")
    private String shippingCity;
    
    @Schema(description = "Shipping state/province", example = "NY")
    private String shippingState;
    
    @Schema(description = "Shipping zip/postal code", example = "10001")
    private String shippingZipCode;
    
    @Schema(description = "Shipping country", example = "USA")
    private String shippingCountry;
    
    @Schema(description = "Date and time when order was placed", example = "2023-07-15T10:30:00Z")
    private ZonedDateTime orderDate;
    
    @Schema(description = "Current order status", example = "Processing", 
           allowableValues = {"Pending", "Allocated", "Processing", "Shipped", "Delivered", "Cancelled"})
    private String status;
    
    @Schema(description = "Order status details")
    private OrderStatusDTO statusDetails;
    
    @Schema(description = "Total order amount", example = "149.95")
    private BigDecimal totalAmount;
    
    @ArraySchema(
        schema = @Schema(implementation = OrderItemDTO.class),
        arraySchema = @Schema(description = "Items in the order")
    )
    private List<OrderItemDTO> items = new ArrayList<>();
    
    @Schema(description = "Order subtotal", example = "199.98")
    private BigDecimal subtotal;
    
    @Schema(description = "Order tax amount", example = "16.00")
    private BigDecimal tax;
    
    @Schema(description = "Order shipping cost", example = "9.99")
    private BigDecimal shippingCost;
    
    @Schema(description = "Date and time when order record was created", example = "2023-07-15T10:30:00Z")
    private ZonedDateTime createdAt;
    
    @Schema(description = "Date and time when order record was last updated", example = "2023-07-15T10:30:00Z")
    private ZonedDateTime updatedAt;
    
    @Schema(description = "Requested delivery date", example = "2023-01-20T12:00:00")
    private LocalDateTime requestedDeliveryDate;
    
    @Schema(description = "Actual delivery date", example = "2023-01-20T11:30:00")
    private LocalDateTime deliveredAt;
    
    @Schema(description = "Order notes", example = "Please leave package at the door")
    private String notes;
    
    // Defensive getter for items
    public List<OrderItemDTO> getItems() {
        return items != null ? new ArrayList<>(items) : new ArrayList<>();
    }
    
    // Defensive setter for items
    public void setItems(List<OrderItemDTO> items) {
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
    }
} 