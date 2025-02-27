package cargo.kityk.wms.order.dto

import groovy.transform.Canonical

@Canonical
class OrderItemCreateDTO {
    Long productId
    Integer quantity
} 