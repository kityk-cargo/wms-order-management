package cargo.kityk.wms.order.repository;

import cargo.kityk.wms.order.application.OrderApplication;
import cargo.kityk.wms.order.entity.Customer;
import cargo.kityk.wms.order.entity.Order;
import cargo.kityk.wms.order.entity.OrderItem;
import cargo.kityk.wms.order.entity.Payment;
import cargo.kityk.wms.test.order.testconfig.LiquibaseFileConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = OrderApplication.class)
@ActiveProfiles("dbIntegrationTest")
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = TestConfiguration.class))
@Import(LiquibaseFileConfig.class)
@Testcontainers
@Transactional
public class RepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("wms_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");

    }

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private Customer testCustomer;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        // Clean up data
        paymentRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        customerRepository.deleteAll();

        // Create test customer
        testCustomer = Customer.builder()
                .name("Test Customer")
                .email("test@example.com")
                .phone("123-456-7890")
                .address("123 Test Street")
                .build();
        customerRepository.save(testCustomer) ;

        // Create test order
        testOrder = Order.builder()
                .customer(testCustomer)
                .orderDate(ZonedDateTime.now())
                .status("Pending")
                .totalAmount(new BigDecimal("100.00"))
                .build();
        orderRepository.save(testOrder);
    }

    @Test
    void testCustomerRepository() {
        // Test findByEmail
        Optional<Customer> foundCustomer = customerRepository.findByEmail("test@example.com");
        assertTrue(foundCustomer.isPresent());
        assertEquals("Test Customer", foundCustomer.get().getName());

        // Test existsByEmail
        assertTrue(customerRepository.existsByEmail("test@example.com"));
        assertFalse(customerRepository.existsByEmail("nonexistent@example.com"));

        // Test basic CRUD
        Customer newCustomer = Customer.builder()
                .name("Another Customer")
                .email("another@example.com")
                .build();
        Customer savedCustomer = customerRepository.save(newCustomer);
        assertNotNull(savedCustomer.getId());

        customerRepository.deleteById(savedCustomer.getId());
        assertFalse(customerRepository.findById(savedCustomer.getId()).isPresent());
    }

    @Test
    void testOrderRepository() {
        // Test findByCustomerId
        List<Order> customerOrders = orderRepository.findByCustomerId(testCustomer.getId());
        assertFalse(customerOrders.isEmpty());
        assertEquals(testCustomer.getId(), customerOrders.get(0).getCustomer().getId());

        // Test findByStatus
        List<Order> pendingOrders = orderRepository.findByStatus("Pending");
        assertFalse(pendingOrders.isEmpty());

        // Test paged results
        var pagedResult = orderRepository.findByCustomerId(testCustomer.getId(), PageRequest.of(0, 10));
        assertEquals(1, pagedResult.getTotalElements());

        // Test findByOrderDateBetween
        ZonedDateTime yesterday = ZonedDateTime.now().minusDays(1);
        ZonedDateTime tomorrow = ZonedDateTime.now().plusDays(1);
        List<Order> recentOrders = orderRepository.findByOrderDateBetween(yesterday, tomorrow);
        assertFalse(recentOrders.isEmpty());

        // Test countByStatus
        long pendingCount = orderRepository.countByStatus("Pending");
        assertTrue(pendingCount > 0);

        // Test findByCustomerIdAndStatus
        List<Order> customerPendingOrders = orderRepository.findByCustomerIdAndStatus(
                testCustomer.getId(), "Pending");
        assertFalse(customerPendingOrders.isEmpty());
    }

    @Test
    void testOrderItemRepository() {
        // Create test order item
        OrderItem item = OrderItem.builder()
                .order(testOrder)
                .productId(1L)
                .quantity(2)
                .price(new BigDecimal("50.00"))
                .build();
        orderItemRepository.save(item);

        // Test findByOrderId
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(testOrder.getId());
        assertFalse(orderItems.isEmpty());
        assertEquals(1, orderItems.size());
        assertEquals(2, orderItems.get(0).getQuantity());

        // Test findByProductId
        List<OrderItem> productItems = orderItemRepository.findByProductId(1L);
        assertFalse(productItems.isEmpty());

        // Test countByProductId
        long productCount = orderItemRepository.countByProductId(1L);
        assertEquals(1, productCount);

        // Test getTotalQuantityForProduct
        Integer totalQuantity = orderItemRepository.getTotalQuantityForProduct(1L);
        assertEquals(2, totalQuantity);
    }

    @Test
    void testPaymentRepository() {
        // Create test payment
        Payment payment = Payment.builder()
                .order(testOrder)
                .paymentDate(ZonedDateTime.now())
                .amount(new BigDecimal("100.00"))
                .paymentMethod("Credit Card")
                .transactionId("txn_123456")
                .status("Completed")
                .build();
        paymentRepository.save(payment);

        // Test findByOrderId
        List<Payment> payments = paymentRepository.findByOrderId(testOrder.getId());
        assertFalse(payments.isEmpty());
        assertEquals(1, payments.size());

        // Test findByTransactionId
        Optional<Payment> foundPayment = paymentRepository.findByTransactionId("txn_123456");
        assertTrue(foundPayment.isPresent());

        // Test findByStatus
        List<Payment> completedPayments = paymentRepository.findByStatus("Completed");
        assertFalse(completedPayments.isEmpty());

        // Test findByPaymentMethod
        List<Payment> creditCardPayments = paymentRepository.findByPaymentMethod("Credit Card");
        assertFalse(creditCardPayments.isEmpty());

        // Test getTotalPaymentsForOrder
        BigDecimal totalPayments = paymentRepository.getTotalPaymentsForOrder(testOrder.getId());
        assertEquals(new BigDecimal("100.00"), totalPayments);

        // Test findByPaymentDateBetween
        ZonedDateTime yesterday = ZonedDateTime.now().minusDays(1);
        ZonedDateTime tomorrow = ZonedDateTime.now().plusDays(1);
        List<Payment> recentPayments = paymentRepository.findByPaymentDateBetween(yesterday, tomorrow);
        assertFalse(recentPayments.isEmpty());
    }
}
