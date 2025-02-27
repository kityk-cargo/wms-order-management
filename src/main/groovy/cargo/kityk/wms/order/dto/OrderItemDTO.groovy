package cargo.kityk.wms.order.dto

import groovy.transform.Canonical
import io.swagger.v3.oas.annotations.media.Schema

@Canonical
class OrderItemDTO {
    @Schema(description = "Order item ID", example = "1")
    Long id
    
    @Schema(description = "Product ID", example = "1")
    Long productId
    
    @Schema(description = "Quantity of product ordered", example = "5")
    Integer quantity
    
    @Schema(description = "Price per unit at time of order", example = "29.99")
    BigDecimal price
} 