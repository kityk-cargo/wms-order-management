package cargo.kityk.wms.order.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.Min;
import java.time.ZonedDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseDBEntityDTO {
    @Min(1)
    @Schema(description = "Entity ID", example = "1")
    private Long id;
    
    @Schema(description = "Date and time when record was created", example = "2023-07-15T10:30:00Z")
    private ZonedDateTime createdAt;
    
    @Schema(description = "Date and time when record was last updated", example = "2023-07-15T10:30:00Z")
    private ZonedDateTime updatedAt;
}
