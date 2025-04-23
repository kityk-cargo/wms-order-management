package cargo.kityk.wms.order.service.client;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactBuilder;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pact consumer tests for the Inventory Service client.
 * This defines the contract that the Order Management service (consumer)
 * expects from the Inventory Management service (provider).
 */
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "wms-inventory-management")
@Tag("pact")
public class InventoryServicePactTest {

    private static final Long EXISTING_PRODUCT_ID = 42L;
    private static final Long NONEXISTENT_PRODUCT_ID = 999L;

    @Pact(consumer = "wms-order-management")
    public V4Pact existingProductPact(PactBuilder builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .usingLegacyDsl()
                .given("a product with ID " + EXISTING_PRODUCT_ID + " exists")
                .uponReceiving("a request for an existing product")
                .path("/api/products/" + EXISTING_PRODUCT_ID)
                .method("GET")
                .willRespondWith()
                .status(200)
                .headers(headers)
                .body("""
                {
                    "id": 42,
                    "sku": "EL-9999-01X",
                    "name": "Super LED Panel 60W",
                    "category": "Lighting Equipment",
                    "description": "A high-end LED panel for industrial use",
                    "created_at": "2023-05-05T09:00:00Z",
                    "updated_at": "2023-05-06T10:00:00Z"
                }
            """)
                .toPact(V4Pact.class);
    }

    @Pact(consumer = "wms-order-management")
    public V4Pact nonexistentProductPact(PactBuilder builder) {
        return builder
                .usingLegacyDsl()
                .given("a product with ID " + NONEXISTENT_PRODUCT_ID + " does not exist")
                .uponReceiving("a request for a non-existent product")
                .path("/api/products/" + NONEXISTENT_PRODUCT_ID)
                .method("GET")
                .willRespondWith()
                .status(404)
                .body("""
                {
                    "detail": "Product not found"
                }
            """)
                .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "existingProductPact")
    void testGetExistingProduct(MockServer mockServer) {
        RestTemplate restTemplate = new RestTemplate();
        String url = mockServer.getUrl() + "/api/products/" + EXISTING_PRODUCT_ID;

        ProductResponse response = restTemplate.getForObject(url, ProductResponse.class);

        assertNotNull(response);
        assertEquals(EXISTING_PRODUCT_ID, response.getId());
        assertEquals("EL-9999-01X", response.getSku());
        assertEquals("Super LED Panel 60W", response.getName());
        assertEquals("Lighting Equipment", response.getCategory());
    }

    @Test
    @PactTestFor(pactMethod = "nonexistentProductPact")
    void testGetNonexistentProduct(MockServer mockServer) {
        RestTemplate restTemplate = new RestTemplate();
        String url = mockServer.getUrl() + "/api/products/" + NONEXISTENT_PRODUCT_ID;

        assertThrows(HttpClientErrorException.NotFound.class, () -> {
            restTemplate.getForObject(url, ProductResponse.class);
        });
    }
}
