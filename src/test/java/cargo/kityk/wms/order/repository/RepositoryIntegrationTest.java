package cargo.kityk.wms.order.repository;

import cargo.kityk.wms.order.entity.Customer;
import cargo.kityk.wms.order.entity.Order;
import cargo.kityk.wms.order.entity.OrderItem;
import cargo.kityk.wms.order.entity.Payment;
import cargo.kityk.wms.test.order.testconfig.BaseRepositoryTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static cargo.kityk.wms.test.order.testconfig.TestConstants.*;
import static cargo.kityk.wms.test.order.testutils.TestEntityFactory.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Repository Integration Tests")
public class RepositoryIntegrationTest extends BaseRepositoryTest {

    private Customer testCustomer;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        // Create test customer
        testCustomer = createPersistedCustomer(customerRepository);

        // Create test order
        testOrder = createPersistedOrder(orderRepository, testCustomer, PENDING_STATUS, ORDER_AMOUNT);
    }

    @Nested
    @DisplayName("Customer Repository Tests")
    class CustomerRepositoryTests {

        @Test
        @DisplayName("Should find customer by email when customer exists")
        void findByEmail_WhenCustomerExists_ShouldReturnCustomer() {
            // Arrange - setup is done in the @BeforeEach method

            // Act
            Optional<Customer> result = customerRepository.findByEmail(TEST_EMAIL);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(CUSTOMER_NAME, result.get().getName());
            assertEquals(TEST_EMAIL, result.get().getEmail());
        }

        @Test
        @DisplayName("Should return empty when finding customer by non-existent email")
        void findByEmail_WhenCustomerDoesNotExist_ShouldReturnEmpty() {
            // Arrange
            String nonExistentEmail = "nonexistent@example.com";

            // Act
            Optional<Customer> result = customerRepository.findByEmail(nonExistentEmail);

            // Assert
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should confirm email exists when customer with email exists")
        void existsByEmail_WhenEmailExists_ShouldReturnTrue() {
            // Act
            boolean result = customerRepository.existsByEmail(TEST_EMAIL);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("Should confirm email doesn't exist when no customer has that email")
        void existsByEmail_WhenEmailDoesNotExist_ShouldReturnFalse() {
            // Act
            boolean result = customerRepository.existsByEmail("nonexistent@example.com");

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("Should create, read, update and delete customer")
        void crudOperations_ShouldWorkCorrectly() {
            // Arrange - Create
            Customer newCustomer = new Customer();
            newCustomer.setName("CRUD Test Customer");
            newCustomer.setEmail("crud@example.com");
            newCustomer.setPhone("987-654-3210");
            newCustomer.setAddress("456 CRUD Street");

            // Act - Create
            Customer savedCustomer = customerRepository.save(newCustomer);

            // Assert - Create
            assertNotNull(savedCustomer.getId());
            assertEquals("CRUD Test Customer", savedCustomer.getName());

            // Act - Read
            Optional<Customer> foundCustomer = customerRepository.findById(savedCustomer.getId());

            // Assert - Read
            assertTrue(foundCustomer.isPresent());
            assertEquals("crud@example.com", foundCustomer.get().getEmail());

            // Act - Update
            foundCustomer.get().setName("Updated CRUD Customer");
            Customer updatedCustomer = customerRepository.save(foundCustomer.get());

            // Assert - Update
            assertEquals("Updated CRUD Customer", updatedCustomer.getName());
            assertEquals(savedCustomer.getId(), updatedCustomer.getId());

            // Act - Delete
            customerRepository.delete(updatedCustomer);

            // Assert - Delete
            assertFalse(customerRepository.existsById(updatedCustomer.getId()));
        }
    }

    @Nested
    @DisplayName("Order Repository Tests")
    class OrderRepositoryTests {

        @Test
        @DisplayName("Should find orders by customer ID when orders exist")
        void findByCustomerId_WhenOrdersExist_ShouldReturnOrders() {
            // Act
            List<Order> results = orderRepository.findByCustomerId(testCustomer.getId());

            // Assert
            assertFalse(results.isEmpty());
            assertEquals(1, results.size());
            assertEquals(testCustomer.getId(), results.get(0).getCustomer().getId());
            assertEquals(PENDING_STATUS, results.get(0).getStatus());
        }

        @Test
        @DisplayName("Should return empty list when finding orders for non-existent customer")
        void findByCustomerId_WhenCustomerDoesNotExist_ShouldReturnEmptyList() {
            // Arrange
            Long nonExistentCustomerId = 9999L;

            // Act
            List<Order> results = orderRepository.findByCustomerId(nonExistentCustomerId);

            // Assert
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("Should find orders by status when orders with that status exist")
        void findByStatus_WhenOrdersExist_ShouldReturnOrders() {
            // Act
            List<Order> results = orderRepository.findByStatus(PENDING_STATUS);

            // Assert
            assertFalse(results.isEmpty());
            assertEquals(PENDING_STATUS, results.get(0).getStatus());
        }

        @Test
        @DisplayName("Should return empty list when finding orders with non-existent status")
        void findByStatus_WhenStatusDoesNotExist_ShouldReturnEmptyList() {
            // Act
            List<Order> results = orderRepository.findByStatus("NonExistentStatus");

            // Assert
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("Should return paged results when finding orders by customer ID")
        void findByCustomerId_WithPageable_ShouldReturnPagedResults() {
            // Arrange
            PageRequest pageRequest = PageRequest.of(0, 10);

            // Act
            Page<Order> pagedResult = orderRepository.findByCustomerId(testCustomer.getId(), pageRequest);

            // Assert
            assertEquals(1, pagedResult.getTotalElements());
            assertEquals(0, pagedResult.getNumber());
            assertEquals(10, pagedResult.getSize());
        }

        @Test
        @DisplayName("Should find orders by date range when orders exist in that range")
        void findByOrderDateBetween_WhenOrdersExist_ShouldReturnOrders() {
            // Arrange
            ZonedDateTime yesterday = ZonedDateTime.now().minusDays(1);
            ZonedDateTime tomorrow = ZonedDateTime.now().plusDays(1);

            // Act
            List<Order> results = orderRepository.findByOrderDateBetween(yesterday, tomorrow);

            // Assert
            assertFalse(results.isEmpty());
            assertEquals(testOrder.getId(), results.get(0).getId());
        }

        @Test
        @DisplayName("Should return empty list when finding orders in a date range with no orders")
        void findByOrderDateBetween_WhenNoOrdersInRange_ShouldReturnEmptyList() {
            // Arrange
            ZonedDateTime twoDaysAgo = ZonedDateTime.now().minusDays(2);
            ZonedDateTime oneDayAgo = ZonedDateTime.now().minusDays(1).minusHours(1);

            // Act
            List<Order> results = orderRepository.findByOrderDateBetween(twoDaysAgo, oneDayAgo);

            // Assert
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("Should count orders by status when orders with that status exist")
        void countByStatus_WhenOrdersExist_ShouldReturnCorrectCount() {
            // Act
            long count = orderRepository.countByStatus(PENDING_STATUS);

            // Assert
            assertEquals(1, count);
        }

        @Test
        @DisplayName("Should return zero when counting orders with non-existent status")
        void countByStatus_WhenStatusDoesNotExist_ShouldReturnZero() {
            // Act
            long count = orderRepository.countByStatus("NonExistentStatus");

            // Assert
            assertEquals(0, count);
        }

        @Test
        @DisplayName("Should find orders by customer ID and status when such orders exist")
        void findByCustomerIdAndStatus_WhenOrdersExist_ShouldReturnOrders() {
            // Act
            List<Order> results = orderRepository.findByCustomerIdAndStatus(
                testCustomer.getId(), PENDING_STATUS);

            // Assert
            assertFalse(results.isEmpty());
            assertEquals(1, results.size());
            assertEquals(testCustomer.getId(), results.get(0).getCustomer().getId());
            assertEquals(PENDING_STATUS, results.get(0).getStatus());
        }

        @Test
        @DisplayName("Should return empty list when no orders match customer ID and status")
        void findByCustomerIdAndStatus_WhenNoMatch_ShouldReturnEmptyList() {
            // Act
            List<Order> results = orderRepository.findByCustomerIdAndStatus(
                testCustomer.getId(), "NonExistentStatus");

            // Assert
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("Should perform complete CRUD operations on Order entity")
        void crudOperations_ShouldWorkCorrectly() {
            // Arrange - Create
            Customer customer = createPersistedCustomer(customerRepository, "CRUDTEST@example.com");
            Order newOrder = new Order();
            newOrder.setCustomer(customer);
            newOrder.setOrderDate(ZonedDateTime.now());
            newOrder.setStatus("Created");
            newOrder.setTotalAmount(new BigDecimal("250.00"));

            // Act - Create
            Order savedOrder = orderRepository.save(newOrder);

            // Assert - Create
            assertNotNull(savedOrder.getId());
            assertEquals("Created", savedOrder.getStatus());
            assertEquals(new BigDecimal("250.00"), savedOrder.getTotalAmount());
            assertEquals(customer.getId(), savedOrder.getCustomer().getId());

            // Act - Read
            Optional<Order> foundOrder = orderRepository.findById(savedOrder.getId());

            // Assert - Read
            assertTrue(foundOrder.isPresent());
            assertEquals("Created", foundOrder.get().getStatus());
            assertEquals(new BigDecimal("250.00"), foundOrder.get().getTotalAmount());

            // Act - Update
            foundOrder.get().setStatus("Processed");
            foundOrder.get().setTotalAmount(new BigDecimal("300.00"));
            Order updatedOrder = orderRepository.save(foundOrder.get());

            // Assert - Update
            assertEquals("Processed", updatedOrder.getStatus());
            assertEquals(new BigDecimal("300.00"), updatedOrder.getTotalAmount());

            // Verify in database that update persisted
            Optional<Order> verifyOrder = orderRepository.findById(updatedOrder.getId());
            assertTrue(verifyOrder.isPresent());
            assertEquals("Processed", verifyOrder.get().getStatus());
            assertEquals(new BigDecimal("300.00"), verifyOrder.get().getTotalAmount());

            // Act - Delete
            orderRepository.delete(updatedOrder);

            // Assert - Delete
            assertFalse(orderRepository.existsById(updatedOrder.getId()));
        }
    }

    @Nested
    @DisplayName("Order Item Repository Tests")
    class OrderItemRepositoryTests {

        private OrderItem testOrderItem;

        @BeforeEach
        void setUpOrderItem() {
            testOrderItem = createPersistedOrderItem(orderItemRepository, testOrder, PRODUCT_ID, 2, ITEM_PRICE);
        }

        @Test
        @DisplayName("Should find order items by order ID when items exist")
        void findByOrderId_WhenItemsExist_ShouldReturnItems() {
            // Act
            List<OrderItem> results = orderItemRepository.findByOrderId(testOrder.getId());

            // Assert
            assertFalse(results.isEmpty());
            assertEquals(1, results.size());
            assertEquals(2, results.get(0).getQuantity());
            assertEquals(PRODUCT_ID, results.get(0).getProductId());
            assertEquals(ITEM_PRICE, results.get(0).getPrice());
        }

        @Test
        @DisplayName("Should return empty list when finding items for non-existent order")
        void findByOrderId_WhenOrderDoesNotExist_ShouldReturnEmptyList() {
            // Act
            List<OrderItem> results = orderItemRepository.findByOrderId(9999L);

            // Assert
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("Should find order items by product ID when items exist")
        void findByProductId_WhenItemsExist_ShouldReturnItems() {
            // Act
            List<OrderItem> results = orderItemRepository.findByProductId(PRODUCT_ID);

            // Assert
            assertFalse(results.isEmpty());
            assertEquals(PRODUCT_ID, results.get(0).getProductId());
        }

        @Test
        @DisplayName("Should return empty list when finding items for non-existent product")
        void findByProductId_WhenProductDoesNotExist_ShouldReturnEmptyList() {
            // Act
            List<OrderItem> results = orderItemRepository.findByProductId(9999L);

            // Assert
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("Should count items by product ID when items exist")
        void countByProductId_WhenItemsExist_ShouldReturnCorrectCount() {
            // Act
            long count = orderItemRepository.countByProductId(PRODUCT_ID);

            // Assert
            assertEquals(1, count);
        }

        @Test
        @DisplayName("Should return zero when counting items for non-existent product")
        void countByProductId_WhenProductDoesNotExist_ShouldReturnZero() {
            // Act
            long count = orderItemRepository.countByProductId(9999L);

            // Assert
            assertEquals(0, count);
        }

        @Test
        @DisplayName("Should get total quantity for product when items exist")
        void getTotalQuantityForProduct_WhenItemsExist_ShouldReturnTotal() {
            // Arrange
            createPersistedOrderItem(orderItemRepository, testOrder, PRODUCT_ID, 3, new BigDecimal("60.00")); // Add another item with 3 quantity

            // Act
            Integer total = orderItemRepository.getTotalQuantityForProduct(PRODUCT_ID);

            // Assert
            assertEquals(5, total); // 2 + 3 = 5
        }

        @Test
        @DisplayName("Should return null or zero when getting total quantity for non-existent product")
        void getTotalQuantityForProduct_WhenProductDoesNotExist_ShouldReturnNullOrZero() {
            // Act
            Integer total = orderItemRepository.getTotalQuantityForProduct(9999L);

            // Assert
            if (total != null) {
                assertEquals(0, total);
            } else {
                assertNull(total);
            }
        }

        @Test
        @DisplayName("Should perform complete CRUD operations on OrderItem entity")
        void crudOperations_ShouldWorkCorrectly() {
            // Arrange - Create
            OrderItem newItem = new OrderItem();
            newItem.setOrder(testOrder);
            newItem.setProductId(PRODUCT_ID_2);
            newItem.setQuantity(5);
            newItem.setPrice(new BigDecimal("49.99"));

            // Act - Create
            OrderItem savedItem = orderItemRepository.save(newItem);

            // Assert - Create
            assertNotNull(savedItem.getId());
            assertEquals(PRODUCT_ID_2, savedItem.getProductId());
            assertEquals(5, savedItem.getQuantity());
            assertEquals(new BigDecimal("49.99"), savedItem.getPrice());

            // Act - Read
            Optional<OrderItem> foundItem = orderItemRepository.findById(savedItem.getId());

            // Assert - Read
            assertTrue(foundItem.isPresent());
            assertEquals(PRODUCT_ID_2, foundItem.get().getProductId());
            assertEquals(5, foundItem.get().getQuantity());

            // Act - Update
            foundItem.get().setQuantity(10);
            OrderItem updatedItem = orderItemRepository.save(foundItem.get());

            // Assert - Update
            assertEquals(10, updatedItem.getQuantity());

            // Verify in database
            Optional<OrderItem> verifyItem = orderItemRepository.findById(updatedItem.getId());
            assertTrue(verifyItem.isPresent());
            assertEquals(10, verifyItem.get().getQuantity());

            // Act - Delete
            orderItemRepository.delete(updatedItem);

            // Assert - Delete
            assertFalse(orderItemRepository.existsById(updatedItem.getId()));
        }

        @Test
        @DisplayName("Should cascade delete order items when order is deleted")
        void cascadeDelete_WhenOrderDeleted_ShouldDeleteOrderItems() {
            // Arrange
            Customer customer = createPersistedCustomer(customerRepository, "cascade@example.com");
            Order cascadeOrder = createPersistedOrder(orderRepository, customer, PENDING_STATUS, ORDER_AMOUNT);
            OrderItem cascadeItem = createPersistedOrderItem(orderItemRepository, cascadeOrder, PRODUCT_ID_2, 3, new BigDecimal("33.33"));

            // First verify the item exists
            assertTrue(orderItemRepository.existsById(cascadeItem.getId()));

            // Act - Delete the parent order
            orderRepository.delete(cascadeOrder);

            // Assert - Verify the item was cascade deleted
            assertFalse(orderItemRepository.existsById(cascadeItem.getId()));
        }
    }

    @Nested
    @DisplayName("Payment Repository Tests")
    class PaymentRepositoryTests {

        private Payment testPayment;

        @BeforeEach
        void setUpPayment() {
            testPayment = createPersistedPayment(paymentRepository, testOrder, PAYMENT_AMOUNT, PAYMENT_METHOD, PAYMENT_STATUS);
        }

        @Test
        @DisplayName("Should find payments by order ID when payments exist")
        void findByOrderId_WhenPaymentsExist_ShouldReturnPayments() {
            // Act
            List<Payment> results = paymentRepository.findByOrderId(testOrder.getId());

            // Assert
            assertFalse(results.isEmpty());
            assertEquals(1, results.size());
            assertEquals(PAYMENT_AMOUNT, results.get(0).getAmount());
            assertEquals(PAYMENT_METHOD, results.get(0).getPaymentMethod());
            assertEquals(PAYMENT_STATUS, results.get(0).getStatus());
        }

        @Test
        @DisplayName("Should return empty list when finding payments for non-existent order")
        void findByOrderId_WhenOrderDoesNotExist_ShouldReturnEmptyList() {
            // Act
            List<Payment> results = paymentRepository.findByOrderId(9999L);

            // Assert
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("Should find payment by transaction ID when payment exists")
        void findByTransactionId_WhenPaymentExists_ShouldReturnPayment() {
            // Arrange
            String transactionId = testPayment.getTransactionId();

            // Act
            Optional<Payment> result = paymentRepository.findByTransactionId(transactionId);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(testOrder.getId(), result.get().getOrder().getId());
            assertEquals(PAYMENT_AMOUNT, result.get().getAmount());
        }

        @Test
        @DisplayName("Should return empty when finding payment by non-existent transaction ID")
        void findByTransactionId_WhenTransactionIdDoesNotExist_ShouldReturnEmpty() {
            // Act
            Optional<Payment> result = paymentRepository.findByTransactionId("non_existent_txn");

            // Assert
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should find payments by status when payments with that status exist")
        void findByStatus_WhenPaymentsExist_ShouldReturnPayments() {
            // Act
            List<Payment> results = paymentRepository.findByStatus(PAYMENT_STATUS);

            // Assert
            assertFalse(results.isEmpty());
            assertEquals(PAYMENT_STATUS, results.get(0).getStatus());
        }

        @Test
        @DisplayName("Should return empty list when finding payments with non-existent status")
        void findByStatus_WhenStatusDoesNotExist_ShouldReturnEmptyList() {
            // Act
            List<Payment> results = paymentRepository.findByStatus("NonExistentStatus");

            // Assert
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("Should find payments by payment method when payments with that method exist")
        void findByPaymentMethod_WhenPaymentsExist_ShouldReturnPayments() {
            // Act
            List<Payment> results = paymentRepository.findByPaymentMethod(PAYMENT_METHOD);

            // Assert
            assertFalse(results.isEmpty());
            assertEquals(PAYMENT_METHOD, results.get(0).getPaymentMethod());
        }

        @Test
        @DisplayName("Should return empty list when finding payments with non-existent payment method")
        void findByPaymentMethod_WhenMethodDoesNotExist_ShouldReturnEmptyList() {
            // Act
            List<Payment> results = paymentRepository.findByPaymentMethod("Bitcoin");

            // Assert
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("Should get total payments for order when payments exist")
        void getTotalPaymentsForOrder_WhenPaymentsExist_ShouldReturnTotal() {
            // Arrange
            createPersistedPayment(paymentRepository, testOrder, new BigDecimal("50.00"), "PayPal", PAYMENT_STATUS);

            // Act
            BigDecimal total = paymentRepository.getTotalPaymentsForOrder(testOrder.getId());

            // Assert
            assertEquals(new BigDecimal("150.00"), total); // 100 + 50 = 150
        }

        @Test
        @DisplayName("Should return zero or null when getting total for order with no payments")
        void getTotalPaymentsForOrder_WhenNoPayments_ShouldReturnZeroOrNull() {
            // Arrange
            Customer customer = createPersistedCustomer(customerRepository, "nopayments@example.com");
            Order orderWithoutPayments = createPersistedOrder(orderRepository, customer, PENDING_STATUS, ORDER_AMOUNT);

            // Act
            BigDecimal total = paymentRepository.getTotalPaymentsForOrder(orderWithoutPayments.getId());

            // Assert
            if (total != null) {
                assertEquals(BigDecimal.ZERO, total);
            } else {
                assertNull(total);
            }
        }

        @Test
        @DisplayName("Should find payments by date range when payments exist in that range")
        void findByPaymentDateBetween_WhenPaymentsExist_ShouldReturnPayments() {
            // Arrange
            ZonedDateTime yesterday = ZonedDateTime.now().minusDays(1);
            ZonedDateTime tomorrow = ZonedDateTime.now().plusDays(1);

            // Act
            List<Payment> results = paymentRepository.findByPaymentDateBetween(yesterday, tomorrow);

            // Assert
            assertFalse(results.isEmpty());
            assertEquals(testPayment.getId(), results.get(0).getId());
        }

        @Test
        @DisplayName("Should return empty list when finding payments in a date range with no payments")
        void findByPaymentDateBetween_WhenNoPaymentsInRange_ShouldReturnEmptyList() {
            // Arrange
            ZonedDateTime twoDaysAgo = ZonedDateTime.now().minusDays(2);
            ZonedDateTime oneDayAgo = ZonedDateTime.now().minusDays(1).minusHours(1);

            // Act
            List<Payment> results = paymentRepository.findByPaymentDateBetween(twoDaysAgo, oneDayAgo);

            // Assert
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("Should perform complete CRUD operations on Payment entity")
        void crudOperations_ShouldWorkCorrectly() {
            // Arrange - Create
            String transactionId = "txn_test_" + System.currentTimeMillis();
            Payment newPayment = new Payment();
            newPayment.setOrder(testOrder);
            newPayment.setPaymentDate(ZonedDateTime.now());
            newPayment.setAmount(new BigDecimal("199.99"));
            newPayment.setPaymentMethod("PayPal");
            newPayment.setTransactionId(transactionId);
            newPayment.setStatus("Pending");

            // Act - Create
            Payment savedPayment = paymentRepository.save(newPayment);

            // Assert - Create
            assertNotNull(savedPayment.getId());
            assertEquals(transactionId, savedPayment.getTransactionId());
            assertEquals("Pending", savedPayment.getStatus());
            assertEquals(new BigDecimal("199.99"), savedPayment.getAmount());
            assertEquals("PayPal", savedPayment.getPaymentMethod());
            assertEquals(testOrder.getId(), savedPayment.getOrder().getId());

            // Act - Read
            Optional<Payment> foundPayment = paymentRepository.findById(savedPayment.getId());

            // Assert - Read
            assertTrue(foundPayment.isPresent());
            assertEquals(transactionId, foundPayment.get().getTransactionId());
            assertEquals("Pending", foundPayment.get().getStatus());

            // Act - Update
            foundPayment.get().setStatus("Completed");
            foundPayment.get().setAmount(new BigDecimal("200.00"));
            Payment updatedPayment = paymentRepository.save(foundPayment.get());

            // Assert - Update
            assertEquals("Completed", updatedPayment.getStatus());
            assertEquals(new BigDecimal("200.00"), updatedPayment.getAmount());

            // Verify in database
            Optional<Payment> verifyPayment = paymentRepository.findById(updatedPayment.getId());
            assertTrue(verifyPayment.isPresent());
            assertEquals("Completed", verifyPayment.get().getStatus());
            assertEquals(new BigDecimal("200.00"), verifyPayment.get().getAmount());

            // Act - Delete
            paymentRepository.delete(updatedPayment);

            // Assert - Delete
            assertFalse(paymentRepository.existsById(updatedPayment.getId()));
        }

        @Test
        @DisplayName("Should cascade delete payments when order is deleted")
        void cascadeDelete_WhenOrderDeleted_ShouldDeletePayments() {
            // Arrange
            Customer customer = createPersistedCustomer(customerRepository, "payment-cascade@example.com");
            Order cascadeOrder = createPersistedOrder(orderRepository, customer, PENDING_STATUS, ORDER_AMOUNT);
            Payment cascadePayment = createPersistedPayment(paymentRepository, cascadeOrder, new BigDecimal("75.00"), "Stripe", "Authorized");

            // First verify payment exists
            assertTrue(paymentRepository.existsById(cascadePayment.getId()));

            // Act - Delete the parent order
            orderRepository.delete(cascadeOrder);

            // Assert - Verify payment was cascade deleted
            assertFalse(paymentRepository.existsById(cascadePayment.getId()));
        }
    }
}
