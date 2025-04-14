package cargo.kityk.wms.test.order.contract;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.StateChangeAction;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import cargo.kityk.wms.order.application.OrderApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
     * State handler for setting up the "order exists" state
     * This would initialize test data required for specific contract tests
     */
    @State(value = "orders exist", action = StateChangeAction.SETUP)
    public void setupOrderExists() {
        logger.info("Setting up provider state: 'order exists'");
        // Code to set up the state "order exists"
        // For example, initialize database with required order data
    }

    /**
     * State handler for cleaning up test data if necessary
     */
    @State(value = "orders exist", action = StateChangeAction.TEARDOWN)
    public void tearDownOrderExists() {
        logger.info("Tearing down provider state: 'order exists'");
        // Code to clean up after the test if needed
    }
} 