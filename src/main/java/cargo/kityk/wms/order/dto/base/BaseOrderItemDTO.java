package cargo.kityk.wms.order.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseOrderItemDTO {
    @NotNull
    @Min(1)
    @Schema(description = "Product ID", example = "1")
    private Long productId;
    
    @NotNull
    @Min(1)
    @Schema(description = "Quantity of product", example = "5")
    private Integer quantity;
}
