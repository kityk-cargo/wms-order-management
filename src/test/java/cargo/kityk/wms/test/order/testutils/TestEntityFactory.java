package cargo.kityk.wms.test.order.testutils;

import cargo.kityk.wms.order.entity.Customer;
import cargo.kityk.wms.order.entity.Order;
import cargo.kityk.wms.order.entity.OrderItem;
import cargo.kityk.wms.order.entity.Payment;
import cargo.kityk.wms.order.repository.CustomerRepository;
import cargo.kityk.wms.order.repository.OrderItemRepository;
import cargo.kityk.wms.order.repository.OrderRepository;
import cargo.kityk.wms.order.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static cargo.kityk.wms.test.order.testconfig.TestConstants.*;

/**
 * Factory for creating test entities.
 * Provides methods for both repository integration tests (with persistence)
 * and service unit tests (without persistence).
 */
public class TestEntityFactory {
    
    /**
     * Creates a test customer without persisting it
     *
     * @param id The customer ID to set
     * @return Customer entity
     */
    public static Customer createCustomer(Long id) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setName(CUSTOMER_NAME);
        customer.setEmail(TEST_EMAIL);
        customer.setPhone(CUSTOMER_PHONE);
        customer.setAddress(CUSTOMER_ADDRESS);
        return customer;
    }
    
    /**
     * Creates a test customer with the specified email without persisting it
     *
     * @param id The customer ID
     * @param email The email to set
     * @return Customer entity
     */
    public static Customer createCustomer(Long id, String email) {
        Customer customer = createCustomer(id);
        customer.setEmail(email);
        return customer;
    }
    
    /**
     * Creates a test customer in the database
     *
     * @param customerRepository Repository to save the customer
     * @param email Email for the customer
     * @return Saved customer entity
     */
    public static Customer createPersistedCustomer(CustomerRepository customerRepository, String email) {
        Customer customer = new Customer();
        customer.setName(CUSTOMER_NAME);
        customer.setEmail(email);
        customer.setPhone(CUSTOMER_PHONE);
        customer.setAddress(CUSTOMER_ADDRESS);
        return customerRepository.save(customer);
    }
    
    /**
     * Creates a test customer in the database with the default test email
     *
     * @param customerRepository Repository to save the customer
     * @return Saved customer entity
     */
    public static Customer createPersistedCustomer(CustomerRepository customerRepository) {
        return createPersistedCustomer(customerRepository, TEST_EMAIL);
    }
    
    /**
     * Creates a basic order without persisting it
     *
     * @param id Order ID to set
     * @param customer Customer for the order
     * @param status Order status
     * @return Order entity
     */
    public static Order createBasicOrder(Long id, Customer customer, String status) {
        ZonedDateTime testTime = ZonedDateTime.now();
        Order order = new Order();
        order.setId(id);
        order.setCustomer(customer);
        order.setStatus(status);
        order.setOrderDate(testTime);
        order.setTotalAmount(BigDecimal.ZERO);
        order.setItems(new ArrayList<>());
        order.setCreatedAt(testTime);
        order.setUpdatedAt(testTime);
        return order;
    }
    
    /**
     * Creates an order entity in the database
     *
     * @param orderRepository Repository to save the order
     * @param customer Customer for the order
     * @param status Order status
     * @param amount Order total amount
     * @return Saved order entity
     */
    public static Order createPersistedOrder(OrderRepository orderRepository, 
                                            Customer customer, 
                                            String status, 
                                            BigDecimal amount) {
        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderDate(ZonedDateTime.now());
        order.setStatus(status);
        order.setTotalAmount(amount);
        return orderRepository.save(order);
    }
    
    /**
     * Creates an order item without persisting it
     *
     * @param order Parent order
     * @param productId Product ID
     * @param quantity Item quantity
     * @return OrderItem entity
     */
    public static OrderItem createOrderItem(Order order, Long productId, int quantity) {
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setPrice(ITEM_PRICE);
        return item;
    }
    
    /**
     * Creates an order item in the database
     *
     * @param orderItemRepository Repository to save the item
     * @param order Parent order
     * @param productId Product ID
     * @param quantity Item quantity
     * @param price Item price
     * @return Saved order item
     */
    public static OrderItem createPersistedOrderItem(OrderItemRepository orderItemRepository,
                                                    Order order, 
                                                    Long productId, 
                                                    int quantity, 
                                                    BigDecimal price) {
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setPrice(price);
        
        // Establish bidirectional relationship
        order.addOrderItem(item);
        
        return orderItemRepository.save(item);
    }
    
    /**
     * Creates a payment in the database
     *
     * @param paymentRepository Repository to save the payment
     * @param order Associated order
     * @param amount Payment amount
     * @param method Payment method
     * @param status Payment status
     * @return Saved payment entity
     */
    public static Payment createPersistedPayment(PaymentRepository paymentRepository,
                                                Order order, 
                                                BigDecimal amount, 
                                                String method, 
                                                String status) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentDate(ZonedDateTime.now());
        payment.setAmount(amount);
        payment.setPaymentMethod(method);
        payment.setTransactionId("txn_" + System.currentTimeMillis());
        payment.setStatus(status);
        
        // Establish bidirectional relationship
        order.addPayment(payment);
        
        return paymentRepository.save(payment);
    }
    
    /**
     * Adds a specified number of items to an order and updates the total amount (no persistence)
     *
     * @param order Order to update
     * @param itemCount Number of items to add
     */
    public static void addItemsToOrder(Order order, int itemCount) {
        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        
        for (int i = 0; i < itemCount; i++) {
            Long productId = PRODUCT_ID + i;
            int quantity = i + 1;
            OrderItem item = createOrderItem(order, productId, quantity);
            items.add(item);
            
            BigDecimal itemTotal = ITEM_PRICE.multiply(new BigDecimal(quantity));
            total = total.add(itemTotal);
        }
        
        order.setItems(items);
        order.setTotalAmount(total);
    }
} 