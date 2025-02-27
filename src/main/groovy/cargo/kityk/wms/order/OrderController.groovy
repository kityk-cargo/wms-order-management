package cargo.kityk.wms.order

import cargo.kityk.wms.order.dto.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.PathVariable

import java.time.ZonedDateTime

@RestController
@RequestMapping("/orders")
class OrderController {

    @PostMapping
    ResponseEntity<OrderDTO> createOrder(@RequestBody(required = true) OrderCreateDTO orderCreateDTO) {
        // Mock implementation - in real app, this would save to database
        OrderDTO createdOrder = new OrderDTO(
            id: 1L,
            customerId: orderCreateDTO.customerId,
            orderDate: ZonedDateTime.now(),
            status: "Pending",
            totalAmount: calculateTotalAmount(orderCreateDTO.items),
            items: orderCreateDTO.items.collect { item ->
                new OrderItemDTO(
                    id: 1L,
                    productId: item.productId,
                    quantity: item.quantity,
                    price: 0.0 // In real app, would lookup price from product service
                )
            },
            createdAt: ZonedDateTime.now(),
            updatedAt: ZonedDateTime.now()
        )
        
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED)
    }

    @GetMapping
    ResponseEntity<List<OrderDTO>> getOrders() {
        // Mock implementation - in real app, this would query the database
        List<OrderDTO> orders = []
        return ResponseEntity.ok(orders)
    }

    @GetMapping("/{id}")
    ResponseEntity<OrderDTO> getOrder(@PathVariable("id") Long id) {
        // Mock implementation - in real app, this would query the database
        OrderDTO order = new OrderDTO(
            id: id,
            customerId: 1L,
            orderDate: ZonedDateTime.now(),
            status: "Pending",
            totalAmount: 0.0,
            items: [],
            createdAt: ZonedDateTime.now(),
            updatedAt: ZonedDateTime.now()
        )
        return ResponseEntity.ok(order)
    }

    @PutMapping("/{id}")
    ResponseEntity<OrderDTO> updateOrder(@PathVariable("id") Long id, @RequestBody(required = true) OrderDTO orderDTO) {
        // Mock implementation - in real app, this would update the database
        // Ensure the ID in the path matches the ID in the DTO
        orderDTO.id = id
        orderDTO.updatedAt = ZonedDateTime.now()
        
        return ResponseEntity.ok(orderDTO)
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteOrder(@PathVariable("id") Long id) {
        // Mock implementation - in real app, this would delete from database
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/allocate")
    ResponseEntity<OrderDTO> allocateInventory(@PathVariable("id") Long id) {
        // Mock implementation - in real app, this would update inventory allocation
        OrderDTO order = getOrder(id).getBody()
        order.status = "Allocated"
        order.updatedAt = ZonedDateTime.now()
        
        return ResponseEntity.ok(order)
    }

    @PutMapping("/{id}/status")
    ResponseEntity<OrderDTO> updateOrderStatus(@PathVariable("id") Long id, @RequestBody(required = true) OrderStatusDTO statusDTO) {
        // Mock implementation - in real app, this would update the order status
        OrderDTO order = getOrder(id).getBody()
        order.status = statusDTO.status
        order.updatedAt = ZonedDateTime.now()
        
        return ResponseEntity.ok(order)
    }
    
    // Helper method to calculate total amount
    private BigDecimal calculateTotalAmount(List<OrderItemCreateDTO> items) {
        // In a real implementation, this would lookup product prices
        // For now, just return a dummy value
        return 0.0
    }
}
