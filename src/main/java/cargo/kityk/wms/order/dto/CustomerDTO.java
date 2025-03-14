package cargo.kityk.wms.order.dto;

import cargo.kityk.wms.order.dto.base.BaseDBEntityDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class CustomerDTO extends BaseDBEntityDTO {
    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Schema(description = "Customer name", example = "John Smith")
    private String name;
    
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    @Schema(description = "Customer email address", example = "john.smith@example.com")
    private String email;
    
    @Pattern(regexp = "^\\+?[0-9\\-\\s]+$", message = "Invalid phone number format")
    @Schema(description = "Customer phone number", example = "+1-555-123-4567")
    private String phone;
    
    @NotBlank(message = "Address cannot be blank")
    @Schema(description = "Customer shipping address", example = "123 Main St, Springfield, IL 62701")
    private String address;
}