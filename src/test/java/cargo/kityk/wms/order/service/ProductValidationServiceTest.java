package cargo.kityk.wms.order.service;

import cargo.kityk.wms.order.exception.InvalidOrderException;
import cargo.kityk.wms.order.exception.OrderManagementException;
import cargo.kityk.wms.order.service.client.InventoryClient;
import cargo.kityk.wms.order.service.client.ProductResponse;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the ProductValidationService
 */
@ExtendWith(MockitoExtension.class)
class ProductValidationServiceTest {

    @Mock
    private InventoryClient inventoryClient;

    @InjectMocks
    private ProductValidationService productValidationService;

    @Test
    @DisplayName("validateProductsExist should not throw exception when all products exist")
    void validateProductsExist_WhenAllProductsExist_ShouldNotThrow() {
        // Arrange
        Long product1Id = 1L;
        Long product2Id = 2L;
        List<Long> productIds = Arrays.asList(product1Id, product2Id);
        
        ProductResponse product1 = new ProductResponse();
        product1.setId(product1Id);
        
        ProductResponse product2 = new ProductResponse();
        product2.setId(product2Id);
        
        when(inventoryClient.getProductById(product1Id)).thenReturn(product1);
        when(inventoryClient.getProductById(product2Id)).thenReturn(product2);
        
        // Act & Assert
        assertDoesNotThrow(() -> productValidationService.validateProductsExist(productIds));
        
        // Verify each product was checked
        verify(inventoryClient).getProductById(product1Id);
        verify(inventoryClient).getProductById(product2Id);
    }
    
    @Test
    @DisplayName("validateProductsExist should throw InvalidOrderException when any product doesn't exist")
    void validateProductsExist_WhenSomeProductsDoNotExist_ShouldThrowInvalidOrderException() {
        // Arrange
        Long existingProductId = 1L;
        Long nonExistingProductId = 2L;
        List<Long> productIds = Arrays.asList(existingProductId, nonExistingProductId);
        
        ProductResponse product = new ProductResponse();
        product.setId(existingProductId);
        
        when(inventoryClient.getProductById(existingProductId)).thenReturn(product);
        when(inventoryClient.getProductById(nonExistingProductId))
            .thenThrow(FeignException.NotFound.class);
        
        // Act & Assert
        InvalidOrderException exception = assertThrows(InvalidOrderException.class, 
            () -> productValidationService.validateProductsExist(productIds));
        
        assertTrue(exception.getMessage().contains(nonExistingProductId.toString()));
        
        // Verify each product was checked
        verify(inventoryClient).getProductById(existingProductId);
        verify(inventoryClient).getProductById(nonExistingProductId);
    }
    
    @Test
    @DisplayName("validateProductsExist should not throw when list is empty")
    void validateProductsExist_WhenListIsEmpty_ShouldNotThrow() {
        // Arrange
        List<Long> emptyList = Collections.emptyList();
        
        // Act & Assert
        assertDoesNotThrow(() -> productValidationService.validateProductsExist(emptyList));
        
        // Verify no calls were made
        verifyNoInteractions(inventoryClient);
    }
    
    @Test
    @DisplayName("validateProductsExist should not throw when product list is null")
    void validateProductsExist_WhenListIsNull_ShouldNotThrow() {
        // Act & Assert
        assertDoesNotThrow(() -> productValidationService.validateProductsExist(null));
        
        // Verify no calls were made
        verifyNoInteractions(inventoryClient);
    }
    
    @Test
    @DisplayName("validateProductsExist should not throw when service is down")
    void validateProductsExist_WhenServiceIsDown_ShouldNotThrow() {
        // Arrange
        Long productId = 1L;
        List<Long> productIds = Collections.singletonList(productId);
        
        when(inventoryClient.getProductById(productId))
            .thenThrow(new RuntimeException("Service unavailable"));
        
        // Act & Assert
        assertThrows(OrderManagementException.class, () -> productValidationService.validateProductsExist(productIds));
        
        // Verify attempt was made
        verify(inventoryClient).getProductById(productId);
    }
} 