package cargo.kityk.wms.order.dto

import groovy.transform.Canonical
import java.time.ZonedDateTime

@Canonical
class OrderDTO {
    Long id
    Long customerId
    CustomerDTO customer
    ZonedDateTime orderDate
    String status
    BigDecimal totalAmount
    List<OrderItemDTO> items = []
    ZonedDateTime createdAt
    ZonedDateTime updatedAt
} 