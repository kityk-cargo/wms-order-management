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
public class OrderStatusDTO {
    @Schema(description = "Order status to update to", example = "Processing",
           allowableValues = {"Pending", "Allocated", "Processing", "Shipped", "Delivered", "Cancelled"})
    private String status;
} 