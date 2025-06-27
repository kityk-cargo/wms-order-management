package cargo.kityk.wms.order.service;

import cargo.kityk.wms.order.dto.OrderItemCreateDTO;
import cargo.kityk.wms.order.dto.StockLockItemDTO;
import cargo.kityk.wms.order.dto.StockLockRequest;
import cargo.kityk.wms.order.dto.StockLockResponse;
import cargo.kityk.wms.order.exception.OrderManagementException;
import cargo.kityk.wms.order.service.client.InventoryClient;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsible for locking stock during order creation.
 * Uses the inventory client to lock stock for order items.
 */
@Service
public class StockLockingService {
    private static final Logger logger = LoggerFactory.getLogger(StockLockingService.class);
    
    private final InventoryClient inventoryClient;
    
    @Autowired
    public StockLockingService(InventoryClient inventoryClient) {
        this.inventoryClient = inventoryClient;
    }
    
    /**
     * Locks stock for all items in the order.
     * 
     * @param orderItems List of order items to lock stock for
     * @throws OrderManagementException if stock locking fails
     */
    public void lockStockForOrder(List<OrderItemCreateDTO> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            logger.warn("Empty order items list provided for stock locking");
            return;
        }
        
        // Convert order items to stock lock items
        List<StockLockItemDTO> stockLockItems = orderItems.stream()
                .map(item -> StockLockItemDTO.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());
        
        StockLockRequest request = StockLockRequest.builder()
                .items(stockLockItems)
                .build();
        
        try {
            StockLockResponse response = inventoryClient.lockStock(request);
            if (response.isSuccess()) {
                logger.info("Successfully locked stock for {} items", stockLockItems.size());
            } else {
                String errorMessage = "Stock locking failed: " + response.getMessage();
                logger.error(errorMessage);
                throw new OrderManagementException(errorMessage, HttpStatus.UNPROCESSABLE_ENTITY, "critical",
                        "Check inventory availability and try again");
            }
        } catch (OrderManagementException e) {
            // Rethrow OrderManagementException (from response.isSuccess() == false case)
            throw e;
        } catch (FeignException.UnprocessableEntity e) {
            logger.error("Insufficient stock for order items: {}", stockLockItems);
            throw new OrderManagementException("Insufficient stock for order", e,
                    HttpStatus.UNPROCESSABLE_ENTITY, "critical",
                    "Reduce quantities or wait for inventory to be restocked");
        } catch (Exception e) {
            logger.error("Error locking stock for order items {}: {}", stockLockItems, e.getMessage());
            throw new OrderManagementException("Error locking stock", e,
                    HttpStatus.SERVICE_UNAVAILABLE, "critical",
                    "The inventory service is currently unavailable. Please try again later.");
        }
    }
} 