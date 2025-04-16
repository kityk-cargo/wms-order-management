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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
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
     * @throws ResourceNotFoundException if customer not found
     */
    @Transactional
    public OrderDTO createOrder(OrderCreateDTO orderCreateDTO) {
        // Validate customer exists
        Customer customer = customerRepository.findById(orderCreateDTO.getCustomerId())
            .orElseThrow(() -> new ResourceNotFoundException("Customer", orderCreateDTO.getCustomerId()));
            
        // Create new order using setters instead of builder
        Order newOrder = new Order();
        newOrder.setCustomer(customer);
        newOrder.setOrderDate(ZonedDateTime.now());
        newOrder.setStatus("Pending");
        newOrder.setTotalAmount(BigDecimal.ZERO);
        newOrder.setItems(new ArrayList<>());
            
        // Add order items
        if (orderCreateDTO.getItems() != null && !orderCreateDTO.getItems().isEmpty()) {
            BigDecimal totalAmount = BigDecimal.ZERO;
            
            for (OrderItemCreateDTO itemDTO : orderCreateDTO.getItems()) {
                // In a real app, we would get the product price from a product service
                BigDecimal itemPrice = new BigDecimal("29.99"); // Simulated price
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