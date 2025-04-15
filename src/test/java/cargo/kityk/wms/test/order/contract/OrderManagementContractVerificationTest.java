package cargo.kityk.wms.test.order.contract;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.StateChangeAction;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import cargo.kityk.wms.order.application.OrderApplication;
import cargo.kityk.wms.order.entity.Customer;
import cargo.kityk.wms.order.entity.Order;
import cargo.kityk.wms.order.entity.OrderItem;
import cargo.kityk.wms.order.repository.CustomerRepository;
import cargo.kityk.wms.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Pact contract verification test for Order Management service.
 * This test verifies that the service implementation meets the expectations
 * defined in the consumer contracts.
 */
@Provider("wms_order_management")
@PactFolder("../wms-contracts/pact/rest/wms_order_management")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {OrderApplication.class, PactVerificationTestConfig.class}
)
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"test", "dbIntegrationTest"})
@Testcontainers
@Tag("pact")
public class OrderManagementContractVerificationTest {

    private static final Logger logger = LoggerFactory.getLogger(OrderManagementContractVerificationTest.class);

    @LocalServerPort
    private int port;

    // Static PostgreSQL container that will be shared for all tests
    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("order_management_test")
            .withUsername("test")
            .withPassword("test");

    @MockitoSpyBean
    private OrderRepository orderRepository;
    
    @Autowired
    private CustomerRepository customerRepository;

    /**
     * Set up dynamic properties for the database connection from TestContainers
     */
    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("container.jdbc.url", postgreSQLContainer::getJdbcUrl);
        registry.add("container.jdbc.username", postgreSQLContainer::getUsername);
        registry.add("container.jdbc.password", postgreSQLContainer::getPassword);
        registry.add("container.jdbc.driver", () -> "org.postgresql.Driver");
    }

    @BeforeEach
    void setUp(PactVerificationContext context) {
        logger.info("Setting up Pact verification test on port {}", port);
        
        // Check default Pact folder
        Path defaultPactPath = Paths.get("../wms-contracts/pact/rest/wms_order_management");
        if (Files.exists(defaultPactPath)) {
            logger.info("Default Pact folder exists at: {}", defaultPactPath.toAbsolutePath());
        } else {
            logger.warn("Default Pact folder does not exist at: {}", defaultPactPath.toAbsolutePath());
        }
        
        // Check if we need to use an alternative pact file location
        String pactFolderOverride = System.getProperty("pact.folder.path");
        logger.info("Pact folder override from system property: {}", pactFolderOverride);
        
        if (pactFolderOverride != null && !pactFolderOverride.isEmpty()) {
            Path overridePath = Paths.get(pactFolderOverride);
            if (Files.exists(overridePath) && Files.isDirectory(overridePath)) {
                logger.info("Using override Pact folder: {}", overridePath.toAbsolutePath());
                // Set the root directory for Pact files using system property
                System.setProperty("pact.rootDir", pactFolderOverride);
            } else {
                logger.error("Override Pact folder does not exist or is not a directory: {}", overridePath.toAbsolutePath());
            }
        }
        
        // Set up the test target - where the provider is running
        logger.info("Setting target to localhost:{}", port);
        context.setTarget(new HttpTestTarget("localhost", port));
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPacts(PactVerificationContext context) {
        logger.info("Verifying Pact interactions");
        context.verifyInteraction();
    }

    /**
     * State handler for setting up the "orders exist" state
     * This would initialize test data required for specific contract tests
     */
    @State(value = "orders exist", action = StateChangeAction.SETUP)
    public void setupOrdersExist() {
        logger.info("Setting up provider state: 'orders exist'");
        
        // Mock repository to return a list of orders when findAll() is called
        List<Order> orders = new ArrayList<>();
        orders.add(createTestOrder(1L, 1L));
        orders.add(createTestOrder(2L, 2L));
        
        Mockito.when(orderRepository.findAll()).thenReturn(orders);
    }

    /**
     * State handler for cleaning up test data if necessary
     */
    @State(value = "orders exist", action = StateChangeAction.TEARDOWN)
    public void tearDownOrdersExist() {
        logger.info("Tearing down provider state: 'orders exist'");
        Mockito.reset(orderRepository);
    }
    
    /**
     * State handler for simulating server error scenario
     */
    @State("server is experiencing issues")
    public void setupServerError() {
        logger.info("Setting up provider state: 'server is experiencing issues'");
        Mockito.when(orderRepository.findAll()).thenThrow(new NullPointerException());

    }

    /**
     * Cleanup for server error state
     */
    @State(value = "server is experiencing issues", action = StateChangeAction.TEARDOWN)
    public void tearDownServerError() {
        logger.info("Tearing down provider state: 'server is experiencing issues'");
        Mockito.reset(orderRepository);
    }

    /**
     * State handler for non-existent order
     */
    @State("order with ID 9999 does not exist")
    public void setupNonExistentOrder() {
        logger.info("Setting up provider state: 'order with ID 9999 does not exist'");
    }

    /**
     * State handler for existing order with ID 1
     */
    @State("order with ID 1 exists")
    public void setupOrderExists() {
        logger.info("Setting up provider state: 'order with ID 1 exists'");
        Order order = createTestOrder(1L, 1L);
        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        Mockito.doNothing().when(orderRepository).deleteById(1L);
    }
    
    /**
     * Creates a test order with the specified IDs
     */
    private Order createTestOrder(Long orderId, Long productId) {
        // Create customer
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Test Customer");
        customer.setEmail("test@example.com");
        
        // Create order
        Order order = new Order();
        order.setId(orderId);
        order.setCustomer(customer);
        order.setOrderDate(ZonedDateTime.parse("2015-08-06T16:53:10+01:00"));
        order.setStatus("Pending");
        order.setCreatedAt(ZonedDateTime.parse("2015-08-06T16:53:10+01:00"));
        order.setUpdatedAt(ZonedDateTime.parse("2015-08-06T16:53:10+01:00"));
        order.setItems(new ArrayList<>());
        order.setTotalAmount(new BigDecimal("99.99"));
        
        // Create order item
        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setOrder(order);
        item.setProductId(productId);
        item.setQuantity(1);
        item.setPrice(new BigDecimal("19.99"));
        
        // Add item to order
        order.addOrderItem(item);
        
        return order;
    }

    /**
     * State handler for order creation
     */
    @State("can create a new order")
    public void setupOrderCreation() {
        logger.info("Setting up provider state: 'can create a new order'");
        // Ensure a customer exists for order creation
        ensureCustomerExists(1L);
    }
    
    /**
     * Helper method to ensure a customer exists
     */
    private void ensureCustomerExists(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            Customer customer = new Customer();
            customer.setId(customerId);
            customer.setName("Test Customer");
            customer.setEmail("test@example.com");
            customerRepository.save(customer);
            logger.info("Created customer with ID {}", customerId);
        }
    }

    /**
     * State handler for non-existent product
     */
    @State("product with ID 9999 does not exist")
    public void setupNonExistentProduct() {
        logger.info("Setting up provider state: 'product with ID 9999 does not exist'");
        // This is handled by the ProductNotFoundException in the OrderService
    }

    @State("order validation will fail")
    public void setupFailedOrderValidation() {
        logger.info("Setting up provider state: 'order validation will fail'");
        // This is handled by the ProductNotFoundException in the OrderService
    }
} 