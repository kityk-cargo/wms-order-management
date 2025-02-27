package cargo.kityk.wms.order.dto

import groovy.transform.Canonical
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.ArraySchema

@Canonical
class OrderCreateDTO {
    @Schema(description = "Customer ID placing the order", example = "1")
    Long customerId
    
    @ArraySchema(
        schema = @Schema(implementation = OrderItemCreateDTO.class),
        arraySchema = @Schema(description = "Items to be ordered")
    )
    List<OrderItemCreateDTO> items = []
} 