package cargo.kityk.wms.order.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for interacting with the Inventory Management Service.
 * Used to validate products during order creation and updates.
 */
@FeignClient(name = "inventory-management", url = "${inventory.service.url}")
public interface InventoryClient {
    String BASE_URL = "/api/v1/products";

    /**
     * Check if a product exists by ID.
     * This is used during order creation and updates to validate product existence.
     *
     * @param productId The ID of the product to check
     * @return Product details if it exists
     */
    @GetMapping(BASE_URL + "/{productId}")
    ProductResponse getProductById(@PathVariable("productId") Long productId);
}