package cargo.kityk.wms.test.order.testconfig;

import java.math.BigDecimal;

/**
 * Centralized constants for tests to avoid duplication across test classes.
 */
public final class TestConstants {
    // Customer constants
    public static final Long CUSTOMER_ID = 1L;
    public static final String TEST_EMAIL = "test@example.com";
    public static final String CUSTOMER_NAME = "Test Customer";
    public static final String CUSTOMER_PHONE = "123-456-7890";
    public static final String CUSTOMER_ADDRESS = "123 Test Street";
    
    // Order constants
    public static final Long ORDER_ID = 1L;
    public static final BigDecimal ORDER_AMOUNT = new BigDecimal("100.00");
    
    // Order status constants
    public static final String PENDING_STATUS = "Pending";
    public static final String PROCESSING_STATUS = "Processing";
    public static final String SHIPPED_STATUS = "Shipped";
    
    // Product constants
    public static final Long PRODUCT_ID = 1L;
    public static final Long PRODUCT_ID_2 = 2L;
    public static final BigDecimal ITEM_PRICE = new BigDecimal("29.99");
    
    // Payment constants
    public static final String PAYMENT_METHOD = "Credit Card";
    public static final String PAYMENT_STATUS = "Completed";
    public static final BigDecimal PAYMENT_AMOUNT = new BigDecimal("100.00");
    
    private TestConstants() {
        // Private constructor to prevent instantiation
    }
} 