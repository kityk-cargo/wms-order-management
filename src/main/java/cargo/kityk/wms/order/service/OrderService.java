package cargo.kityk.wms.order.service;

import cargo.kityk.wms.order.dto.OrderCreateDTO;
import cargo.kityk.wms.order.dto.OrderDTO;
import cargo.kityk.wms.order.dto.OrderItemCreateDTO;
import cargo.kityk.wms.order.dto.OrderItemDTO;
import cargo.kityk.wms.order.entity.Customer;
import cargo.kityk.wms.order.entity.Order;
import cargo.kityk.wms.order.entity.OrderItem;
import cargo.kityk.wms.order.exception.ResourceNotFoundException;
import cargo.kityk.wms.order.repository.CustomerRepository;
import cargo.kityk.wms.order.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductValidationService productValidationService;
    private final StockLockingService stockLockingService;
    
    @Autowired
    public OrderService(OrderRepository orderRepository, 
                       CustomerRepository customerRepository,
                       ProductValidationService productValidationService,
                       StockLockingService stockLockingService) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productValidationService = productValidationService;
        this.stockLockingService = stockLockingService;
    }
    
    /**
     * Creates a new order from the provided order data
     * 
     * @param orderCreateDTO Order creation data
     * @return Created order as DTO
     * @throws ResourceNotFoundException if customer not found
     */
    @Transactional
    public OrderDTO createOrder(OrderCreateDTO orderCreateDTO) {
        // Validate customer exists
        Customer customer = customerRepository.findById(orderCreateDTO.getCustomerId())
            .orElseThrow(() -> new ResourceNotFoundException("Customer", orderCreateDTO.getCustomerId()));
            
        // Validate that the order contains at least one item
        if (orderCreateDTO.getItems() == null || orderCreateDTO.getItems().isEmpty()) {
            String errorId = java.util.UUID.randomUUID().toString();
            log.warn("ORDER_VALIDATION_ERROR_ID={} message=Order with empty item list is not a valid order to create,", errorId);
            throw new cargo.kityk.wms.order.exception.InvalidOrderException("Order with empty item list is not a valid order to create");
        }
            
        // Create new order using setters instead of builder
        Order newOrder = new Order();
        newOrder.setCustomer(customer);
        newOrder.setOrderDate(ZonedDateTime.now());
        newOrder.setStatus("Pending");
        newOrder.setTotalAmount(BigDecimal.ZERO);
        newOrder.setItems(new ArrayList<>());
            
        // Add order items
        if (orderCreateDTO.getItems() != null && !orderCreateDTO.getItems().isEmpty()) {
            // Extract product IDs for validation
            List<Long> productIds = orderCreateDTO.getItems().stream()
                .map(OrderItemCreateDTO::getProductId)
                .collect(Collectors.toList());
                
            // Validate all products exist in inventory
            productValidationService.validateProductsExist(productIds);
            
            BigDecimal totalAmount = BigDecimal.ZERO;
            
            for (OrderItemCreateDTO itemDTO : orderCreateDTO.getItems()) {
                // todo: here I have the price, but it is not in the product on order creation. Just doing it to spice up the UI/should be done on
                // todo: the later stages, tbd -- probably on packing/sending to port as part of order finalization using current prices.
                BigDecimal itemPrice = BigDecimal.valueOf(1 + Math.random() * 999).setScale(2, RoundingMode.HALF_UP);
                BigDecimal itemTotal = itemPrice.multiply(new BigDecimal(itemDTO.getQuantity()));
                
                // Build order item using setters
                OrderItem item = new OrderItem();
                item.setOrder(newOrder);
                item.setProductId(itemDTO.getProductId());
                item.setQuantity(itemDTO.getQuantity());
                item.setPrice(itemPrice);
                
                newOrder.addOrderItem(item);
                totalAmount = totalAmount.add(itemTotal);
            }
            
            newOrder.setTotalAmount(totalAmount);
        }
        
        // Save the order
        Order savedOrder = orderRepository.save(newOrder);
        
        // Attempt to lock stock for the order items
        try {
            stockLockingService.lockStockForOrder(orderCreateDTO.getItems());
            log.info("Successfully locked stock for order ID: {}", savedOrder.getId());
        } catch (Exception e) {
            log.error("Failed to lock stock for order ID: {}. Error: {}", savedOrder.getId(), e.getMessage());
            // Update order status to indicate stock lock error
            savedOrder.setStatus("Stock Lock Error");
            savedOrder = orderRepository.save(savedOrder);
            log.warn("Order ID: {} status updated to 'Stock Lock Error' due to stock locking failure", savedOrder.getId());
        }
        
        // Convert to DTO and return
        return mapOrderToDTO(savedOrder);
    }
    
    /**
     * Retrieves an order by ID
     * 
     * @param orderId Order ID
     * @return Order as DTO
     * @throws ResourceNotFoundException if order not found
     */
    public OrderDTO getOrder(Long orderId) {
        return orderRepository.findById(orderId)
            .map(this::mapOrderToDTO)
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
    }
    
    /**
     * Updates an existing order
     * 
     * @param orderId ID of order to update
     * @param orderDTO Updated order data
     * @return Updated order as DTO
     * @throws ResourceNotFoundException if order not found
     */
    @Transactional
    public OrderDTO updateOrder(Long orderId, OrderDTO orderDTO) {
        Order existingOrder = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
            
        // Update basic fields
        if (orderDTO.getStatus() != null) {
            existingOrder.setStatus(orderDTO.getStatus());
        }
        
        if (orderDTO.getShippingAddress() != null) {
            //todo shipping address? Add to DB/remove from DTO
        }
        
        // Validate products if items are being updated
        if (orderDTO.getItems() != null && !orderDTO.getItems().isEmpty()) {
            List<Long> productIds = orderDTO.getItems().stream()
                .map(OrderItemDTO::getProductId)
                .collect(Collectors.toList());
                
            // Validate all products exist in inventory
            productValidationService.validateProductsExist(productIds);
            
            // todo update order items/should be addressed when the update flow is there  -- will be done with update flow
        }
        
        // Save updated order
        Order updatedOrder = orderRepository.save(existingOrder);
        
        return mapOrderToDTO(updatedOrder);
    }
    
    /**
     * Deletes an order by ID
     * 
     * @param orderId ID of order to delete
     * @throws ResourceNotFoundException if order not found
     */
    @Transactional
    public void deleteOrder(Long orderId) {
        // Check if order exists before deleting
        if (!orderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException("Order", orderId);
        }
        orderRepository.deleteById(orderId);
    }
    
    /**
     * Lists all orders
     * 
     * @return List of all orders as DTOs
     */
    public List<OrderDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
            .map(this::mapOrderToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Maps Order entity to OrderDTO
     */
    private OrderDTO mapOrderToDTO(Order order) {
        if (order == null) {
            return null;
        }
        
        List<OrderItemDTO> itemDTOs = new ArrayList<>();
        
        if (order.getItems() != null) {
            itemDTOs = order.getItems().stream()
                .map(item -> OrderItemDTO.builder()
                    .id(item.getId())
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .price(item.getPrice())
                    .build())
                .collect(Collectors.toList());
        }
        
        return OrderDTO.builder()
            .id(order.getId())
            .customerId(order.getCustomer().getId())
            .orderDate(order.getOrderDate())
            .status(order.getStatus())
            .totalAmount(order.getTotalAmount())
            .items(itemDTOs)
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .build();
    }
}