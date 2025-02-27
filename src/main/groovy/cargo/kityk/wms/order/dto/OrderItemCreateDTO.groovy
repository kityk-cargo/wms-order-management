package cargo.kityk.wms.order.dto

import groovy.transform.Canonical
import io.swagger.v3.oas.annotations.media.Schema

@Canonical
class OrderItemCreateDTO {
    @Schema(description = "Product ID to order", example = "1")
    Long productId
    
    @Schema(description = "Quantity of product to order", example = "5")
    Integer quantity
} 