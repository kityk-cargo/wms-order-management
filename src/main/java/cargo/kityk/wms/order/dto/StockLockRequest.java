package cargo.kityk.wms.order.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for requesting stock locks for multiple products.
 * The inventory service will attempt to lock the requested quantities
 * in a location-agnostic manner.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockLockRequest {
    
    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    @ArraySchema(
        schema = @Schema(implementation = StockLockItemDTO.class),
        arraySchema = @Schema(description = "List of products and quantities to lock")
    )
    private List<StockLockItemDTO> items;
} 