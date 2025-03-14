package cargo.kityk.wms.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusDTO {
    @NotBlank(message = "Status cannot be blank")
    @Pattern(regexp = "^(Pending|Allocated|Processing|Shipped|Delivered|Cancelled)$", 
             message = "Invalid order status. Must be one of: Pending, Allocated, Processing, Shipped, Delivered, Cancelled")
    @Schema(description = "Order status to update to", example = "Processing",
           allowableValues = {"Pending", "Allocated", "Processing", "Shipped", "Delivered", "Cancelled"})
    private String status;
}