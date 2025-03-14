package cargo.kityk.wms.order.dto;

import cargo.kityk.wms.order.dto.base.BaseOrderItemDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderItemCreateDTO extends BaseOrderItemDTO {
    // All fields are inherited from BaseOrderItemDTO
}