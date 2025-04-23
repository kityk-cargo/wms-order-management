package cargo.kityk.wms.order.service;

import cargo.kityk.wms.order.exception.InvalidOrderException;
import cargo.kityk.wms.order.service.client.InventoryClient;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service responsible for validating products in orders.
 * Uses the inventory client to check if products exist in the inventory.
 */
@Service
public class ProductValidationService {
    private static final Logger logger = LoggerFactory.getLogger(ProductValidationService.class);
    
    private final InventoryClient inventoryClient;
    
    @Autowired
    public ProductValidationService(InventoryClient inventoryClient) {
        this.inventoryClient = inventoryClient;
    }
    
    /**
     * Validates that all products in the given list exist in the inventory.
     * 
     * @param productIds List of product IDs to validate
     * @throws InvalidOrderException if any product does not exist
     */
    public void validateProductsExist(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            logger.warn("Empty product list provided for validation");
            return;
        }
        
        Set<Long> invalidProducts = productIds.stream()
                .filter(this::isProductInvalid)
                .collect(Collectors.toSet());
        
        if (!invalidProducts.isEmpty()) {
            String errorMessage = String.format(
                    "The following products do not exist in inventory: %s", 
                    invalidProducts
            );
            logger.error(errorMessage);
            throw new InvalidOrderException(errorMessage);
        }
        
        logger.info("All products validated successfully: {}", productIds);
    }
    
    /**
     * Checks if a single product exists in the inventory.
     * 
     * @param productId ID of the product to check
     * @return true if the product exists, false otherwise
     */
    private boolean isProductInvalid(Long productId) {
        try {
            inventoryClient.getProductById(productId);
            return false; // Product exists
        } catch (FeignException.NotFound e) {
            logger.warn("Product not found in inventory: {}", productId);
            return true; // Product does not exist
        } catch (Exception e) {
            // In case of other errors (network, service down, etc.), log but don't fail validation
            // This is to prevent orders from failing when inventory service is temporarily unavailable //todo massive rework
            logger.error("Error validating product {}: {}", productId, e.getMessage());
            return false;
        }
    }
} 