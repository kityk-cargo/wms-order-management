package cargo.kityk.wms.order.service;

import cargo.kityk.wms.order.dto.OrderCreateDTO;
import cargo.kityk.wms.order.dto.OrderDTO;
import cargo.kityk.wms.order.dto.OrderItemCreateDTO;
import cargo.kityk.wms.order.dto.OrderItemDTO;
import cargo.kityk.wms.order.entity.Customer;
import cargo.kityk.wms.order.entity.Order;
import cargo.kityk.wms.order.entity.OrderItem;
import cargo.kityk.wms.order.repository.CustomerRepository;
import cargo.kityk.wms.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    
    @Autowired
    public OrderService(OrderRepository orderRepository, CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
    }
    
    /**
     * Creates a new order from the provided order data
     * 
     * @param orderCreateDTO Order creation data
     * @return Created order as DTO
     */
    @Transactional
    public OrderDTO createOrder(OrderCreateDTO orderCreateDTO) {
        // Validate customer exists
        Customer customer = customerRepository.findById(orderCreateDTO.getCustomerId())
            .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + orderCreateDTO.getCustomerId()));
            
        // Create new order
        Order newOrder = Order.builder()
            .customer(customer)
            .orderDate(ZonedDateTime.now())
            .status("Pending")
            .totalAmount(BigDecimal.ZERO) // Will be calculated based on items
            .items(new ArrayList<>())
            .build();
            
        // Add order items
        if (orderCreateDTO.getItems() != null && !orderCreateDTO.getItems().isEmpty()) {
            BigDecimal totalAmount = BigDecimal.ZERO;
            
            for (OrderItemCreateDTO itemDTO : orderCreateDTO.getItems()) {
                // In a real app, we would get the product price from a product service
                BigDecimal itemPrice = new BigDecimal("29.99"); // Simulated price
                BigDecimal itemTotal = itemPrice.multiply(new BigDecimal(itemDTO.getQuantity()));
                
                OrderItem item = OrderItem.builder()
                    .order(newOrder)
                    .productId(itemDTO.getProductId())
                    .quantity(itemDTO.getQuantity())
                    .price(itemPrice)
                    .build();
                
                newOrder.addOrderItem(item);
                totalAmount = totalAmount.add(itemTotal);
            }
            
            newOrder.setTotalAmount(totalAmount);
        }
        
        // Save the order
        Order savedOrder = orderRepository.save(newOrder);
        
        // Convert to DTO and return
        return mapOrderToDTO(savedOrder);
    }
    
    /**
     * Retrieves an order by ID
     * 
     * @param orderId Order ID
     * @return Order as DTO or null if not found
     */
    public OrderDTO getOrder(Long orderId) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        return orderOptional.map(this::mapOrderToDTO).orElse(null);
    }
    
    /**
     * Updates an existing order
     * 
     * @param orderId ID of order to update
     * @param orderDTO Updated order data
     * @return Updated order as DTO
     */
    @Transactional
    public OrderDTO updateOrder(Long orderId, OrderDTO orderDTO) {
        Order existingOrder = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
            
        // Update basic fields
        if (orderDTO.getStatus() != null) {
            existingOrder.setStatus(orderDTO.getStatus());
        }
        
        if (orderDTO.getShippingAddress() != null) {
            // In a real app, we would update shipping details
        }
        
        // In a real application, you would handle updating items, handling payments, etc.
        
        // Save updated order
        Order updatedOrder = orderRepository.save(existingOrder);
        
        return mapOrderToDTO(updatedOrder);
    }
    
    /**
     * Deletes an order by ID
     * 
     * @param orderId ID of order to delete
     */
    @Transactional
    public void deleteOrder(Long orderId) {
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