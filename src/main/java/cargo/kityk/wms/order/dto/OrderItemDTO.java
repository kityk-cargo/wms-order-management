package cargo.kityk.wms.order.dto;

import cargo.kityk.wms.order.dto.base.BaseOrderItemDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderItemDTO extends BaseOrderItemDTO {
    @Schema(description = "Order item ID", example = "1")
    private Long id;
    
    @NotNull
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    @Schema(description = "Price per unit at time of order", example = "29.99")
    private BigDecimal price;
}