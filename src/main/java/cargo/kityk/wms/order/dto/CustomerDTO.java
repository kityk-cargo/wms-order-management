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
public class CustomerDTO {
    @Schema(description = "Customer ID", example = "1")
    private Long id;
    
    @Schema(description = "Customer name", example = "John Smith")
    private String name;
    
    @Schema(description = "Customer email address", example = "john.smith@example.com")
    private String email;
    
    @Schema(description = "Customer phone number", example = "+1-555-123-4567")
    private String phone;
    
    @Schema(description = "Customer shipping address", example = "123 Main St, Springfield, IL 62701")
    private String address;
} 