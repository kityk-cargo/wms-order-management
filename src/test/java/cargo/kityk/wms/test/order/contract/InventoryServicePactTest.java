package cargo.kityk.wms.test.order.contract;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.LambdaDsl;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import cargo.kityk.wms.order.application.OrderApplication;
import cargo.kityk.wms.order.service.client.InventoryClient;
import cargo.kityk.wms.order.service.client.ProductResponse;
import cargo.kityk.wms.test.order.testconfig.UnitTestConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.Map;

import static cargo.kityk.wms.order.service.client.InventoryClient.BASE_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pact consumer tests for the Inventory Service client.
 * This defines the contract that the Order Management service (consumer)
 * expects from the Inventory Management service (provider).
 */
@ExtendWith({PactConsumerTestExt.class, SpringExtension.class})
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        "inventory.service.url=http://localhost:9999"  // Fixed port for Pact mock server
    },
    classes = {
        OrderApplication.class
    }
)
@Import(UnitTestConfiguration.class)
@ActiveProfiles("test")
@PactTestFor(providerName = "wms_inventory_management", pactVersion = PactSpecVersion.V3)
@MockServerConfig(hostInterface = "localhost", port = "9999")
@Tag("pact")
@DisplayName("Inventory Service Contract Tests")
public class InventoryServicePactTest {

    private static final Long EXISTING_PRODUCT_ID = 1L;
    private static final Long NONEXISTENT_PRODUCT_ID = 9999L;
    
    @Autowired
    private InventoryClient inventoryClient;
    
    @BeforeAll
    public static void setup() {
        // Force overwriting of pact files to avoid merge conflicts between V3 and V4 pacts
        System.setProperty("pact.writer.overwrite", "true");
    }

    @Pact(consumer = "wms_order_management")
    @DisplayName("Pact for retrieving an existing product from inventory")
    public RequestResponsePact existingProductPact(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        DslPart responseBody = LambdaDsl.newJsonBody(body -> {
            body.numberValue("id", EXISTING_PRODUCT_ID);
            body.stringType("sku", "EL-9999-01X");
            body.stringType("name", "Super LED Panel 60W");
            body.stringType("category", "Lighting Equipment");
            body.stringType("description", "A high-end LED panel for industrial use");
            body.stringMatcher("created_at", "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z", "2023-05-05T09:00:00Z");
            body.stringMatcher("updated_at", "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z", "2023-05-06T10:00:00Z");
        }).build();

        return builder
                .given("product with ID " + EXISTING_PRODUCT_ID + " exists")
                .uponReceiving("a request for an existing product")
                .path(BASE_URL + "/" + EXISTING_PRODUCT_ID)
                .method("GET")
                .willRespondWith()
                .status(200)
                .headers(headers)
                .body(responseBody)
                .toPact();
    }

    @Pact(consumer = "wms_order_management")
    @DisplayName("Pact for requesting a non-existent product from inventory")
    public RequestResponsePact nonexistentProductPact(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        DslPart errorResponseBody = new PactDslJsonBody()
                .stringValue("criticality", "critical")
                .stringMatcher("id", "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", "550e8400-e29b-41d4-a716-446655440000")
                .stringValue("detail", "Product not found");

        return builder
                .given("product with ID " + NONEXISTENT_PRODUCT_ID + " does not exist")
                .uponReceiving("a request for a non-existent product")
                .path(BASE_URL + "/" + NONEXISTENT_PRODUCT_ID)
                .method("GET")
                .willRespondWith()
                .status(404)
                .body(errorResponseBody)
                .headers(headers)
                .toPact();
    }
    

    //todo the following tests are less shit now. However is there a way to test how we actualkly USE the client? Or is it overkill?
    @Test
    @PactTestFor(pactMethod = "existingProductPact")
    @DisplayName("Should successfully retrieve an existing product from inventory")
    void testGetExistingProduct() {
        ProductResponse response = inventoryClient.getProductById(EXISTING_PRODUCT_ID);

        assertNotNull(response);
        assertEquals(EXISTING_PRODUCT_ID, response.getId());
        // Using less strict assertions to match our matcher-based Pact definition
        assertNotNull(response.getSku());
        assertNotNull(response.getName());
        assertNotNull(response.getCategory());
    }

    @Test
    @PactTestFor(pactMethod = "nonexistentProductPact")
    @DisplayName("Should receive a 404 error when requesting a non-existent product")
    void testGetNonexistentProduct() {
        assertThrows(feign.FeignException.NotFound.class, () -> {
            inventoryClient.getProductById(NONEXISTENT_PRODUCT_ID);
        });
    }
}
