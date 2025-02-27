package cargo.kityk.wms.order.dto

import groovy.transform.Canonical

@Canonical
class OrderCreateDTO {
    Long customerId
    List<OrderItemCreateDTO> items = []
} 