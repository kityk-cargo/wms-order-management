package cargo.kityk.wms.order.service;

import cargo.kityk.wms.order.dto.OrderCreateDTO;
import cargo.kityk.wms.order.dto.OrderDTO;
import cargo.kityk.wms.order.dto.OrderItemCreateDTO;
import cargo.kityk.wms.order.dto.OrderItemDTO;
import cargo.kityk.wms.order.entity.Order;
import cargo.kityk.wms.order.entity.Customer;
import cargo.kityk.wms.order.entity.OrderItem;
import cargo.kityk.wms.order.repository.OrderRepository;
import cargo.kityk.wms.order.repository.CustomerRepository;
import cargo.kityk.wms.order.exception.ResourceNotFoundException;
import cargo.kityk.wms.order.service.StockLockingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import static cargo.kityk.wms.test.order.testconfig.TestConstants.*;
import static cargo.kityk.wms.test.order.testutils.TestEntityFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Order Service Tests")
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private CustomerRepository customerRepository;

        @Mock
    private ProductValidationService productValidationService;
    
    @Mock
    private StockLockingService stockLockingService;
    
    @InjectMocks
    private OrderService orderService;
    
    // Test fixtures
    private Customer testCustomer;
    private ZonedDateTime testTime;
    private OrderCreateDTO orderCreateDTO;
    private Order testOrder;
    
    @BeforeEach
    void setUp() {
        testTime = ZonedDateTime.now();
        testCustomer = createCustomer(CUSTOMER_ID);
        
        // Initialize empty objects that will be customized in individual tests
        orderCreateDTO = OrderCreateDTO.builder().customerId(CUSTOMER_ID).items(new ArrayList<>()).build();
        testOrder = createBasicOrder(ORDER_ID, testCustomer, PROCESSING_STATUS);
        
        // Set up default behavior for stock locking to succeed
        // This ensures existing tests continue to pass unless specifically testing stock locking failures
        // Using lenient() to avoid unnecessary stubbing exceptions for tests that don't call stock locking
        lenient().doNothing().when(stockLockingService).lockStockForOrder(any());
    }
    
    @Nested
    @DisplayName("Order Creation Operations")
    class CreateOrderTests {
        @Test
        @DisplayName("Should create and return the order with all details")
        void testCreateOrder_Success() {
            // Arrange
            OrderItemCreateDTO itemDTO = OrderItemCreateDTO.builder()
                    .productId(PRODUCT_ID)
                    .quantity(2)
                    .build();
            orderCreateDTO.setItems(Collections.singletonList(itemDTO));
            
            Order newOrder = createBasicOrder(ORDER_ID, testCustomer, PENDING_STATUS);
            
            // Create order item with quantity=2 to match our expected calculation (29.99 * 2 = 59.98)
            OrderItem orderItem = createOrderItem(newOrder, PRODUCT_ID, 2);
            newOrder.setItems(Collections.singletonList(orderItem));
            
            // Set the total amount to match what the service would calculate (29.99 * 2 = 59.98)
            newOrder.setTotalAmount(new BigDecimal("59.98"));
            
            when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(testCustomer));
            when(orderRepository.save(any(Order.class))).thenReturn(newOrder);
    
            // Act
            OrderDTO result = orderService.createOrder(orderCreateDTO);
    
            // Assert
            assertNotNull(result);
            assertEquals(ORDER_ID, result.getId());
            assertEquals(CUSTOMER_ID, result.getCustomerId());
            assertEquals(PENDING_STATUS, result.getStatus());
            assertEquals(new BigDecimal("59.98"), result.getTotalAmount());
            
            // Verify item mapping
            assertEquals(1, result.getItems().size());
            OrderItemDTO resultItem = result.getItems().getFirst();
            assertEquals(PRODUCT_ID, resultItem.getProductId());
            assertEquals(2, resultItem.getQuantity());
            assertEquals(ITEM_PRICE, resultItem.getPrice());
            
            verify(customerRepository).findById(CUSTOMER_ID);
            verify(orderRepository).save(any(Order.class));
        }
        
        @Test
        @DisplayName("Should throw exception when customer not found")
        void testCreateOrder_CustomerNotFound() {
            // Arrange
            Long nonExistentCustomerId = 999L;
            orderCreateDTO.setCustomerId(nonExistentCustomerId);
            
            when(customerRepository.findById(nonExistentCustomerId)).thenReturn(Optional.empty());
    
            // Act & Assert
            RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.createOrder(orderCreateDTO)
            );
            
            assertTrue(exception.getMessage().contains("Customer not found"));
            verify(customerRepository).findById(nonExistentCustomerId);
            verify(orderRepository, never()).save(any(Order.class));
        }
        
        @Test
        @DisplayName("Should throw exception when creating order with empty item list")
        void testCreateOrder_EmptyItems() {
            // Arrange
            when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(testCustomer));

            // Act & Assert
            cargo.kityk.wms.order.exception.InvalidOrderException exception = assertThrows(
                cargo.kityk.wms.order.exception.InvalidOrderException.class,
                () -> orderService.createOrder(orderCreateDTO)
            );
            assertEquals("Order with empty item list is not a valid order to create", exception.getMessage());
            verify(customerRepository).findById(CUSTOMER_ID);
            verify(orderRepository, never()).save(any(Order.class));
        }
    }

    @Nested
    @DisplayName("Order Retrieval Operations")
    class GetOrderTests {
        @Test
        @DisplayName("Should return complete order details when found")
        void testGetOrder_Success() {
            // Arrange
            addItemsToOrder(testOrder, 2);
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(testOrder));
    
            // Act
            OrderDTO result = orderService.getOrder(ORDER_ID);
    
            // Assert
            assertNotNull(result);
            assertEquals(ORDER_ID, result.getId());
            assertEquals(CUSTOMER_ID, result.getCustomerId());
            assertEquals(PROCESSING_STATUS, result.getStatus());
            assertEquals(testOrder.getTotalAmount(), result.getTotalAmount());
            assertTrue(testTime.minusNanos(1).isBefore(result.getOrderDate()));
            assertTrue(testTime.minusNanos(1).isBefore(result.getCreatedAt()));
            assertTrue(testTime.minusNanos(1).isBefore(result.getUpdatedAt()));
            assertTrue(testTime.plusMinutes(1).isAfter(result.getOrderDate()));
            assertTrue(testTime.plusMinutes(1).isAfter(result.getCreatedAt()));
            assertTrue(testTime.plusMinutes(1).isAfter(result.getUpdatedAt()));
            
            // Verify items
            assertEquals(2, result.getItems().size());
            assertEquals(PRODUCT_ID, result.getItems().get(0).getProductId());
            assertEquals(1, result.getItems().get(0).getQuantity());
            assertEquals(PRODUCT_ID + 1, result.getItems().get(1).getProductId());
            assertEquals(2, result.getItems().get(1).getQuantity());
        }
        
        @Test
        @DisplayName("Should throw ResourceNotFoundException when order not found")
        void testGetOrder_NotFound() {
            // Arrange
            Long nonExistentOrderId = 999L;
            when(orderRepository.findById(nonExistentOrderId)).thenReturn(Optional.empty());
    
            // Act & Assert
            assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.getOrder(nonExistentOrderId)
            );
            
            verify(orderRepository).findById(nonExistentOrderId);
        }
    }
    
    @Nested
    @DisplayName("Order Update Operations")
    class UpdateOrderTests {
        @Test
        @DisplayName("Should update and return the order with all modified fields")
        void testUpdateOrder_Success() {
            // Arrange
            OrderDTO updateOrderDTO = OrderDTO.builder()
                    .id(ORDER_ID)
                    .customerId(CUSTOMER_ID)
                    .status(SHIPPED_STATUS)
                    .build();
                    
            testOrder.setTotalAmount(new BigDecimal("100.00"));
            Order updatedOrder = createBasicOrder(ORDER_ID, testCustomer, SHIPPED_STATUS);
            updatedOrder.setTotalAmount(new BigDecimal("100.00"));
    
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);
    
            // Act
            OrderDTO result = orderService.updateOrder(ORDER_ID, updateOrderDTO);
    
            // Assert
            assertNotNull(result);
            assertEquals(ORDER_ID, result.getId());
            assertEquals(SHIPPED_STATUS, result.getStatus());
            assertEquals(new BigDecimal("100.00"), result.getTotalAmount());
            
            verify(orderRepository).findById(ORDER_ID);
            verify(orderRepository).save(testOrder);
        }
        
        @Test
        @DisplayName("Should throw exception when order not found")
        void testUpdateOrder_NotFound() {
            // Arrange
            Long nonExistentOrderId = 999L;
            OrderDTO updateOrderDTO = OrderDTO.builder()
                    .id(nonExistentOrderId)
                    .status(SHIPPED_STATUS)
                    .build();
                    
            when(orderRepository.findById(nonExistentOrderId)).thenReturn(Optional.empty());
    
            // Act & Assert
            RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.updateOrder(nonExistentOrderId, updateOrderDTO)
            );
            
            assertTrue(exception.getMessage().contains("Order not found"));
            verify(orderRepository).findById(nonExistentOrderId);
            verify(orderRepository, never()).save(any(Order.class));
        }
    }
    
    @Nested
    @DisplayName("Order Deletion and Listing Operations")
    class DeleteAndListOrderTests {
        @Test
        @DisplayName("Should throw ResourceNotFoundException when order not found")
        void testDeleteOrder_NotFound() {
            // Arrange
            Long nonExistentOrderId = 999L;
    
            // Act & Assert
            assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.deleteOrder(nonExistentOrderId)
            );

            verify(orderRepository, never()).delete(any(Order.class));
        }
        
        @Test
        @DisplayName("Should successfully delete the order")
        void testDeleteOrder_Success() {
            // Arrange
            when(orderRepository.existsById(ORDER_ID)).thenReturn(true);
    
            // Act
            orderService.deleteOrder(ORDER_ID);
    
            // Assert
            verify(orderRepository).existsById(ORDER_ID);
            verify(orderRepository).deleteById(ORDER_ID);
        }
        
        @Test
        @DisplayName("Should return all orders with details")
        void testGetAllOrders_Success() {
            // Arrange
            Order order1 = createBasicOrder(ORDER_ID, testCustomer, PROCESSING_STATUS);
            order1.setTotalAmount(new BigDecimal("100.00"));
            
            Order order2 = createBasicOrder(2L, testCustomer, SHIPPED_STATUS);
            order2.setTotalAmount(new BigDecimal("200.00"));
                    
            List<Order> orders = Arrays.asList(order1, order2);
            when(orderRepository.findAll()).thenReturn(orders);
    
            // Act
            List<OrderDTO> results = orderService.getAllOrders();
    
            // Assert
            assertNotNull(results);
            assertEquals(2, results.size());
            
            // First order assertions
            OrderDTO firstOrder = results.getFirst();
            assertEquals(ORDER_ID, firstOrder.getId());
            assertEquals(PROCESSING_STATUS, firstOrder.getStatus());
            assertEquals(new BigDecimal("100.00"), firstOrder.getTotalAmount());
            
            // Second order assertions
            OrderDTO secondOrder = results.get(1);
            assertEquals(2L, secondOrder.getId());
            assertEquals(SHIPPED_STATUS, secondOrder.getStatus());
            assertEquals(new BigDecimal("200.00"), secondOrder.getTotalAmount());
            
            verify(orderRepository).findAll();
        }
        
        @Test
        @DisplayName("Should return empty list when no orders exist")
        void testGetAllOrders_Empty() {
            // Arrange
            when(orderRepository.findAll()).thenReturn(new ArrayList<>());
    
            // Act
            List<OrderDTO> results = orderService.getAllOrders();
    
            // Assert
            assertNotNull(results);
            assertTrue(results.isEmpty());
            
            verify(orderRepository).findAll();
        }
    }
}