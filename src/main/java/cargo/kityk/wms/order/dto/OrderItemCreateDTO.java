package cargo.kityk.wms.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemCreateDTO {
    @Schema(description = "Product ID to order", example = "1")
    private Long productId;
    
    @Schema(description = "Quantity of product to order", example = "5")
    private Integer quantity;
} 