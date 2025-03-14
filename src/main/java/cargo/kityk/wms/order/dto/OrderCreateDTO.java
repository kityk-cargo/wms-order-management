package cargo.kityk.wms.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateDTO {
    @NotNull(message = "Customer ID cannot be null")
    @Min(value = 1, message = "Customer ID must be greater than 0")
    @Schema(description = "Customer ID placing the order", example = "1")
    private Long customerId;
    
    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    @ArraySchema(
        schema = @Schema(implementation = OrderItemCreateDTO.class),
        arraySchema = @Schema(description = "Items to be ordered")
    )
    private List<OrderItemCreateDTO> items = new ArrayList<>();
    
    // Defensive getter for items
    public List<OrderItemCreateDTO> getItems() {
        return items != null ? new ArrayList<>(items) : new ArrayList<>();
    }
    
    // Defensive setter for items
    public void setItems(List<OrderItemCreateDTO> items) {
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
    }
}