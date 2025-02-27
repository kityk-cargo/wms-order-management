package cargo.kityk.wms.order.dto

import groovy.transform.Canonical
import io.swagger.v3.oas.annotations.media.Schema

@Canonical
class OrderStatusDTO {
    @Schema(description = "Order status to update to", example = "Processing",
           allowableValues = ["Pending", "Allocated", "Processing", "Shipped", "Delivered", "Cancelled"])
    String status
} 