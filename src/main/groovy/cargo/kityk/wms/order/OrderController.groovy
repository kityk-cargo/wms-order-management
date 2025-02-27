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

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag

import java.time.ZonedDateTime

@RestController
@RequestMapping("/orders")
@Tag(name = "Order Management", description = "APIs for managing customer orders")
class OrderController {

    @PostMapping
    @Operation(
        summary = "Create a new order",
        description = "Creates a new order with the specified items",
        responses = [
            @ApiResponse(
                responseCode = "201", 
                description = "Order created successfully",
                content = @Content(schema = @Schema(implementation = OrderDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid order data")
        ]
    )
    ResponseEntity<OrderDTO> createOrder(
        @RequestBody(required = true) OrderCreateDTO orderCreateDTO
    ) {
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
    @Operation(
        summary = "Get all orders",
        description = "Returns a list of all orders in the system",
        responses = [
            @ApiResponse(
                responseCode = "200", 
                description = "List of orders retrieved successfully",
                content = @Content(schema = @Schema(implementation = OrderDTO.class))
            )
        ]
    )
    ResponseEntity<List<OrderDTO>> getOrders() {
        // Mock implementation - in real app, this would query the database
        List<OrderDTO> orders = []
        return ResponseEntity.ok(orders)
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get order by ID",
        description = "Returns a specific order by its ID",
        responses = [
            @ApiResponse(
                responseCode = "200", 
                description = "Order retrieved successfully",
                content = @Content(schema = @Schema(implementation = OrderDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Order not found")
        ]
    )
    ResponseEntity<OrderDTO> getOrder(
        @Parameter(description = "ID of the order to retrieve") 
        @PathVariable("id") Long id
    ) {
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
    @Operation(
        summary = "Update an order",
        description = "Updates an existing order with new information",
        responses = [
            @ApiResponse(
                responseCode = "200", 
                description = "Order updated successfully",
                content = @Content(schema = @Schema(implementation = OrderDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid order data"),
            @ApiResponse(responseCode = "404", description = "Order not found")
        ]
    )
    ResponseEntity<OrderDTO> updateOrder(
        @Parameter(description = "ID of the order to update") 
        @PathVariable("id") Long id, 
        @RequestBody(required = true) OrderDTO orderDTO
    ) {
        // Mock implementation - in real app, this would update the database
        // Ensure the ID in the path matches the ID in the DTO
        orderDTO.id = id
        orderDTO.updatedAt = ZonedDateTime.now()
        
        return ResponseEntity.ok(orderDTO)
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete an order",
        description = "Deletes an order from the system",
        responses = [
            @ApiResponse(responseCode = "204", description = "Order deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found")
        ]
    )
    ResponseEntity<Void> deleteOrder(
        @Parameter(description = "ID of the order to delete") 
        @PathVariable("id") Long id
    ) {
        // Mock implementation - in real app, this would delete from database
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/allocate")
    @Operation(
        summary = "Allocate inventory for an order",
        description = "Allocates inventory items for the specified order",
        responses = [
            @ApiResponse(
                responseCode = "200", 
                description = "Inventory allocated successfully",
                content = @Content(schema = @Schema(implementation = OrderDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "400", description = "Insufficient inventory")
        ]
    )
    ResponseEntity<OrderDTO> allocateInventory(
        @Parameter(description = "ID of the order to allocate inventory for") 
        @PathVariable("id") Long id
    ) {
        // Mock implementation - in real app, this would update inventory allocation
        OrderDTO order = getOrder(id).getBody()
        order.status = "Allocated"
        order.updatedAt = ZonedDateTime.now()
        
        return ResponseEntity.ok(order)
    }

    @PutMapping("/{id}/status")
    @Operation(
        summary = "Update order status",
        description = "Updates the status of an existing order",
        responses = [
            @ApiResponse(
                responseCode = "200", 
                description = "Order status updated successfully",
                content = @Content(schema = @Schema(implementation = OrderDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid status"),
            @ApiResponse(responseCode = "404", description = "Order not found")
        ]
    )
    ResponseEntity<OrderDTO> updateOrderStatus(
        @Parameter(description = "ID of the order to update status for") 
        @PathVariable("id") Long id, 
        @RequestBody(required = true) OrderStatusDTO statusDTO
    ) {
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
