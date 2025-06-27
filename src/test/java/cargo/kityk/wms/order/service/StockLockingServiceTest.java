package cargo.kityk.wms.order.service;

import cargo.kityk.wms.order.dto.OrderItemCreateDTO;
import cargo.kityk.wms.order.dto.StockLockRequest;
import cargo.kityk.wms.order.dto.StockLockResponse;
import cargo.kityk.wms.order.exception.OrderManagementException;
import cargo.kityk.wms.order.service.client.InventoryClient;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StockLockingService.
 * Tests stock locking functionality for order creation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Stock Locking Service Tests")
class StockLockingServiceTest {

    @Mock
    private InventoryClient inventoryClient;

    @InjectMocks
    private StockLockingService stockLockingService;

    private List<OrderItemCreateDTO> orderItems;
    private StockLockResponse successResponse;
    private StockLockResponse failureResponse;

    @BeforeEach
    void setUp() {
        // Arrange: Set up test data
        orderItems = Arrays.asList(
            OrderItemCreateDTO.builder()
                .productId(1L)
                .quantity(5)
                .build(),
            OrderItemCreateDTO.builder()
                .productId(2L)
                .quantity(3)
                .build()
        );

        successResponse = StockLockResponse.builder()
            .success(true)
            .message("Stock locked successfully")
            .build();

        failureResponse = StockLockResponse.builder()
            .success(false)
            .message("Insufficient stock available")
            .build();
    }

    @Test
    @DisplayName("Should successfully lock stock for valid order items")
    void testLockStockForOrder_Success() {
        // Arrange
        when(inventoryClient.lockStock(any(StockLockRequest.class)))
            .thenReturn(successResponse);

        // Act & Assert
        assertDoesNotThrow(() -> stockLockingService.lockStockForOrder(orderItems));

        // Verify
        verify(inventoryClient, times(1)).lockStock(any(StockLockRequest.class));
    }

    @Test
    @DisplayName("Should handle successful response with false success flag")
    void testLockStockForOrder_FailureResponse() {
        // Arrange
        when(inventoryClient.lockStock(any(StockLockRequest.class)))
            .thenReturn(failureResponse);

        // Act & Assert
        OrderManagementException exception = assertThrows(OrderManagementException.class,
            () -> stockLockingService.lockStockForOrder(orderItems));

        // Verify exception details
        assertEquals("Stock locking failed: Insufficient stock available", exception.getMessage());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.getStatus());
        assertEquals("critical", exception.getCriticality());
        
        // Verify recovery suggestion is in otherErrors
        assertNotNull(exception.getErrorFormat().getOtherErrors());
        assertEquals(1, exception.getErrorFormat().getOtherErrors().size());
        assertEquals("Recovery suggestion: Check inventory availability and try again", 
                     exception.getErrorFormat().getOtherErrors().get(0).getDetail());

        verify(inventoryClient, times(1)).lockStock(any(StockLockRequest.class));
    }

    @Test
    @DisplayName("Should handle 422 UnprocessableEntity exception from inventory service")
    void testLockStockForOrder_UnprocessableEntity() {
        // Arrange
        FeignException.UnprocessableEntity feignException = 
            mock(FeignException.UnprocessableEntity.class);
        when(inventoryClient.lockStock(any(StockLockRequest.class)))
            .thenThrow(feignException);

        // Act & Assert
        OrderManagementException exception = assertThrows(OrderManagementException.class,
            () -> stockLockingService.lockStockForOrder(orderItems));

        // Verify exception details
        assertEquals("Insufficient stock for order", exception.getMessage());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.getStatus());
        assertEquals("critical", exception.getCriticality());
        assertSame(feignException, exception.getCause());
        
        // Verify recovery suggestion is in otherErrors
        assertNotNull(exception.getErrorFormat().getOtherErrors());
        assertEquals(1, exception.getErrorFormat().getOtherErrors().size());
        assertEquals("Recovery suggestion: Reduce quantities or wait for inventory to be restocked", 
                     exception.getErrorFormat().getOtherErrors().get(0).getDetail());

        verify(inventoryClient, times(1)).lockStock(any(StockLockRequest.class));
    }

    @Test
    @DisplayName("Should handle generic exception from inventory service")
    void testLockStockForOrder_GenericException() {
        // Arrange
        RuntimeException genericException = new RuntimeException("Service unavailable");
        when(inventoryClient.lockStock(any(StockLockRequest.class)))
            .thenThrow(genericException);

        // Act & Assert
        OrderManagementException exception = assertThrows(OrderManagementException.class,
            () -> stockLockingService.lockStockForOrder(orderItems));

        // Verify exception details
        assertEquals("Error locking stock", exception.getMessage());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatus());
        assertEquals("critical", exception.getCriticality());
        assertSame(genericException, exception.getCause());
        
        // Verify recovery suggestion is in otherErrors
        assertNotNull(exception.getErrorFormat().getOtherErrors());
        assertEquals(1, exception.getErrorFormat().getOtherErrors().size());
        assertEquals("Recovery suggestion: The inventory service is currently unavailable. Please try again later.", 
                     exception.getErrorFormat().getOtherErrors().get(0).getDetail());

        verify(inventoryClient, times(1)).lockStock(any(StockLockRequest.class));
    }

    @Test
    @DisplayName("Should handle empty order items list gracefully")
    void testLockStockForOrder_EmptyList() {
        // Arrange
        List<OrderItemCreateDTO> emptyList = Collections.emptyList();

        // Act & Assert
        assertDoesNotThrow(() -> stockLockingService.lockStockForOrder(emptyList));

        // Verify that inventory client is not called
        verify(inventoryClient, never()).lockStock(any(StockLockRequest.class));
    }

    @Test
    @DisplayName("Should handle null order items list gracefully")
    void testLockStockForOrder_NullList() {
        // Act & Assert
        assertDoesNotThrow(() -> stockLockingService.lockStockForOrder(null));

        // Verify that inventory client is not called
        verify(inventoryClient, never()).lockStock(any(StockLockRequest.class));
    }

    @Test
    @DisplayName("Should correctly map order items to stock lock items")
    void testLockStockForOrder_CorrectMapping() {
        // Arrange
        when(inventoryClient.lockStock(any(StockLockRequest.class)))
            .thenReturn(successResponse);

        // Act
        stockLockingService.lockStockForOrder(orderItems);

        // Verify the request structure
        verify(inventoryClient).lockStock(argThat(request -> {
            assertEquals(2, request.getItems().size());
            
            // Verify first item
            assertEquals(1L, request.getItems().get(0).getProductId());
            assertEquals(5, request.getItems().get(0).getQuantity());
            
            // Verify second item
            assertEquals(2L, request.getItems().get(1).getProductId());
            assertEquals(3, request.getItems().get(1).getQuantity());
            
            return true;
        }));
    }
} 