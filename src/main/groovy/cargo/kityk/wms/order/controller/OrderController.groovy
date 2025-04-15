package cargo.kityk.wms.order.controller

import cargo.kityk.wms.order.dto.*
import cargo.kityk.wms.order.exception.ServiceErrorResponse
import cargo.kityk.wms.order.service.OrderService
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
            @ApiResponse(
                responseCode = "400", 
                description = "Invalid order data",
                content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "404", 
                description = "Product not found",
                content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class))
            )
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
            ),
            @ApiResponse(
                responseCode = "500", 
                description = "Internal server error",
                content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class))
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
            @ApiResponse(
                responseCode = "404", 
                description = "Order not found",
                content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class))
            )
        ]
    )
    ResponseEntity<OrderDTO> getOrder(
        @Parameter(description = "ID of the order to retrieve") 
        @PathVariable("id") Long id
    ) {
        OrderDTO order = orderService.getOrder(id)
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
            @ApiResponse(
                responseCode = "400", 
                description = "Invalid order data",
                content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "404", 
                description = "Order not found",
                content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class))
            )
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
            @ApiResponse(
                responseCode = "404", 
                description = "Order not found",
                content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class))
            )
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
            @ApiResponse(
                responseCode = "404", 
                description = "Order not found",
                content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "400", 
                description = "Insufficient inventory",
                content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class))
            )
        ]
    )
    ResponseEntity<OrderDTO> allocateInventory(
        @Parameter(description = "ID of the order to allocate inventory for") 
        @PathVariable("id") Long id
    ) {
        // Get the order - this will throw ResourceNotFoundException if not found
        OrderDTO order = orderService.getOrder(id)
        
        // Mock implementation - in real app, this would update inventory allocation
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
            @ApiResponse(
                responseCode = "400", 
                description = "Invalid status",
                content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "404", 
                description = "Order not found",
                content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class))
            )
        ]
    )
    ResponseEntity<OrderDTO> updateOrderStatus(
        @Parameter(description = "ID of the order to update status for") 
        @PathVariable("id") Long id, 
        @Valid @RequestBody(required = true) OrderStatusDTO statusDTO
    ) {
        // Get the order - this will throw ResourceNotFoundException if not found
        OrderDTO order = orderService.getOrder(id)
        
        // Mock implementation - in real app, this would update the order status
        order.status = statusDTO.status
        order.updatedAt = ZonedDateTime.now()
        
        return ResponseEntity.ok(order)
    }
}
