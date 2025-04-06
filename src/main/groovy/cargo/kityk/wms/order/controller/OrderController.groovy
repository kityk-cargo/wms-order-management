package cargo.kityk.wms.order.controller

import cargo.kityk.wms.order.dto.*
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
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

import jakarta.validation.Valid
import java.time.LocalDateTime
import java.time.ZonedDateTime

import cargo.kityk.wms.order.service.OrderService

@CompileStatic
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Order Management", description = "APIs for managing customer orders")
class OrderController {

    @Autowired
    private OrderService orderService

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
        @Valid @RequestBody(required = true) OrderCreateDTO orderCreateDTO
    ) {
        OrderDTO createdOrder = orderService.createOrder(orderCreateDTO)
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
        List<OrderDTO> orders = orderService.getAllOrders()
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
        OrderDTO order = orderService.getOrder(id)
        if (order != null) { //todo throw exception?
            return ResponseEntity.ok(order)
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
        }
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
        @Valid @RequestBody(required = true) OrderDTO orderDTO
    ) {
        OrderDTO updatedOrder = orderService.updateOrder(id, orderDTO)
        return ResponseEntity.ok(updatedOrder)
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
        orderService.deleteOrder(id)
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
        @Valid @RequestBody(required = true) OrderStatusDTO statusDTO
    ) {
        // Mock implementation - in real app, this would update the order status
        OrderDTO order = getOrder(id).getBody()
        order.status = statusDTO.status
        order.updatedAt = ZonedDateTime.now()
        
        return ResponseEntity.ok(order)
    }
}
