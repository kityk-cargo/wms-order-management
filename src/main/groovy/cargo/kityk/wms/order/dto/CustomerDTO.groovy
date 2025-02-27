package cargo.kityk.wms.order.dto

import groovy.transform.Canonical

@Canonical
class CustomerDTO {
    Long id
    String name
    String email
    String phone
    String address
} 