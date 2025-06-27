package cargo.kityk.wms.order.service.client;

import cargo.kityk.wms.order.dto.StockLockRequest;
import cargo.kityk.wms.order.dto.StockLockResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for interacting with the Inventory Management Service.
 * Used to validate products during order creation and updates.
 */
@FeignClient(name = "inventory-management", url = "${inventory.service.url}")
public interface InventoryClient {
    String BASE_URL = "/api/v1/products";
    String STOCK_URL = "/api/v1/stock";

    /**
     * Check if a product exists by ID.
     * This is used during order creation and updates to validate product existence.
     *
     * @param productId The ID of the product to check
     * @return Product details if it exists
     */
    @GetMapping(BASE_URL + "/{productId}")
    ProductResponse getProductById(@PathVariable("productId") Long productId);

    /**
     * Lock stock for multiple products in a location-agnostic manner.
     * This is used during order creation to reserve inventory items.
     *
     * @param request Stock lock request containing products and quantities to lock
     * @return Stock lock confirmation response
     */
    @PostMapping(STOCK_URL + "/lock")
    StockLockResponse lockStock(@RequestBody StockLockRequest request);
}