package cargo.kityk.wms.order.dto;

import cargo.kityk.wms.order.dto.base.BaseDBEntityDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor()
public class OrderDTO extends BaseDBEntityDTO {
    @NotNull(message = "Customer ID cannot be null")
    @Min(1)
    @Schema(description = "Customer ID", example = "1")
    private Long customerId;
    
    @Valid
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
    
    @Pattern(regexp = "^(Pending|Allocated|Processing|Shipped|Delivered|Cancelled)$", 
             message = "Invalid order status. Must be one of: Pending, Allocated, Processing, Shipped, Delivered, Cancelled")
    @Schema(description = "Current order status", example = "Processing", 
           allowableValues = {"Pending", "Allocated", "Processing", "Shipped", "Delivered", "Cancelled"})
    private String status;
    
    @DecimalMin(value = "0.0", inclusive = true)
    @Schema(description = "Total order amount", example = "149.95")
    private BigDecimal totalAmount;
    
    @Valid
    @ArraySchema(
        schema = @Schema(implementation = OrderItemDTO.class),
        arraySchema = @Schema(description = "Items in the order")
    )
    private List<OrderItemDTO> items = new ArrayList<>();
    
    @DecimalMin(value = "0.0", inclusive = true)
    @Schema(description = "Order subtotal", example = "199.98")
    private BigDecimal subtotal;
    
    @DecimalMin(value = "0.0", inclusive = true)
    @Schema(description = "Order tax amount", example = "16.00")
    private BigDecimal tax;
    
    @DecimalMin(value = "0.0", inclusive = true)
    @Schema(description = "Order shipping cost", example = "9.99")
    private BigDecimal shippingCost;
    
    @Schema(description = "Requested delivery date", example = "2023-01-20T12:00:00")
    private LocalDateTime requestedDeliveryDate;
    
    @Schema(description = "Actual delivery date", example = "2023-01-20T11:30:00")
    private LocalDateTime deliveredAt;
    
    @Size(max = 1000)
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