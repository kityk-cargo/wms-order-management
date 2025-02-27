package cargo.kityk.wms.order.dto

import groovy.transform.Canonical

@Canonical
class OrderItemDTO {
    Long id
    Long productId
    Integer quantity
    BigDecimal price
} 