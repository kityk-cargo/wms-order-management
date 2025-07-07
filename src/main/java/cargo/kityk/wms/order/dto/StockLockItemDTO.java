package cargo.kityk.wms.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a single item in a stock lock request.
 * Used to request locking of a specific quantity of a product.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockLockItemDTO {
    
    @NotNull(message = "Product ID cannot be null")
    @Min(value = 1, message = "Product ID must be greater than 0")
    @Schema(description = "ID of the product to lock", example = "1")
    private Long productId;
    
    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be greater than 0")
    @Schema(description = "Quantity of the product to lock", example = "5")
    private Integer quantity;
} 