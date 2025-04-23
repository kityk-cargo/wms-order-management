package cargo.kityk.wms.order.service;

import cargo.kityk.wms.order.dto.OrderCreateDTO;
import cargo.kityk.wms.order.dto.OrderDTO;
import cargo.kityk.wms.order.dto.OrderItemCreateDTO;
import cargo.kityk.wms.order.entity.Customer;
import cargo.kityk.wms.order.exception.InvalidOrderException;
import cargo.kityk.wms.order.repository.CustomerRepository;
import cargo.kityk.wms.order.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * Tests focusing on product validation in the OrderService
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceProductValidationTest {

    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private CustomerRepository customerRepository;
    
    @Mock
    private ProductValidationService productValidationService;
    
    @InjectMocks
    private OrderService orderService;
    
    @Test
    @DisplayName("createOrder should validate products")
    void createOrder_ShouldValidateProducts() {
        // Arrange
        Long customerId = 1L;
        Customer customer = new Customer();
        customer.setId(customerId);
        
        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setCustomerId(customerId);
        orderCreateDTO.setItems(Arrays.asList(
            createOrderItemDTO(101L, 2),
            createOrderItemDTO(102L, 1)
        ));
        
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        doNothing().when(productValidationService).validateProductsExist(anyList());
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        OrderDTO result = orderService.createOrder(orderCreateDTO);
        
        // Assert
        assertNotNull(result);
    }
    
    @Test
    @DisplayName("createOrder should throw InvalidOrderException when product validation fails")
    void createOrder_WhenProductValidationFails_ShouldThrowInvalidOrderException() {
        // Arrange
        Long customerId = 1L;
        Customer customer = new Customer();
        customer.setId(customerId);
        
        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setCustomerId(customerId);
        orderCreateDTO.setItems(Arrays.asList(
            createOrderItemDTO(101L, 2),
            createOrderItemDTO(999L, 1) // Non-existent product
        ));
        
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        doThrow(new InvalidOrderException("Product 999 does not exist"))
            .when(productValidationService).validateProductsExist(anyList());
        
        // Act & Assert
        assertThrows(InvalidOrderException.class, () -> orderService.createOrder(orderCreateDTO));
    }
    
    /**
     * Helper method to create an OrderItemCreateDTO
     */
    private OrderItemCreateDTO createOrderItemDTO(Long productId, int quantity) {
        OrderItemCreateDTO itemDTO = new OrderItemCreateDTO();
        itemDTO.setProductId(productId);
        itemDTO.setQuantity(quantity);
        return itemDTO;
    }
} 