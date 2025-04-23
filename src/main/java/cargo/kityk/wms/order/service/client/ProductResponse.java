package cargo.kityk.wms.order.service.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * DTO representing a product response from the inventory management service.
 * Contains only the fields needed for product validation in the order service.
 */
@Data
@NoArgsConstructor
public class ProductResponse {
    private Long id;
    private String sku;
    private String name;
    private String category;
    private String description;
    
    @JsonProperty("created_at")
    private ZonedDateTime createdAt;
    
    @JsonProperty("updated_at")
    private ZonedDateTime updatedAt;
} 