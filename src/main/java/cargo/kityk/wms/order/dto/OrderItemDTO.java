package cargo.kityk.wms.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    @Schema(description = "Order item ID", example = "1")
    private Long id;
    
    @Schema(description = "Product ID", example = "1")
    private Long productId;
    
    @Schema(description = "Quantity of product ordered", example = "5")
    private Integer quantity;
    
    @Schema(description = "Price per unit at time of order", example = "29.99")
    private BigDecimal price;
} 