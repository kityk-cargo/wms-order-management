package cargo.kityk.wms.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing the response from a stock lock operation.
 * Contains confirmation that the stock has been successfully locked.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockLockResponse {
    
    @Schema(description = "Confirmation message", example = "Stock locked successfully")
    private String message;
    
    @Schema(description = "Success status", example = "true")
    private boolean success;
} 