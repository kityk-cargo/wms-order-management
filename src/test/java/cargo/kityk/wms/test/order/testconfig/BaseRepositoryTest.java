package cargo.kityk.wms.test.order.testconfig;

import cargo.kityk.wms.order.application.OrderApplication;
import cargo.kityk.wms.order.repository.CustomerRepository;
import cargo.kityk.wms.order.repository.OrderItemRepository;
import cargo.kityk.wms.order.repository.OrderRepository;
import cargo.kityk.wms.order.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for repository integration tests providing common setup, cleanup and autowiring.
 * Extends TestContainersConfig to inherit the PostgreSQL container configuration.
 */
@SpringBootTest(classes = OrderApplication.class)
@ActiveProfiles("dbIntegrationTest")
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = TestConfiguration.class))
@Import(LiquibaseFileConfig.class)
@Testcontainers
@Transactional
public abstract class BaseRepositoryTest extends TestContainersConfig {

    @Autowired protected CustomerRepository customerRepository;
    @Autowired protected OrderRepository orderRepository;
    @Autowired protected OrderItemRepository orderItemRepository;
    @Autowired protected PaymentRepository paymentRepository;

    /**
     * Cleans up all test data before each test
     */
    @BeforeEach
    void cleanUpData() {
        // Clean up data
        paymentRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        customerRepository.deleteAll();
    }
} 