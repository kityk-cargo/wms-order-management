package cargo.kityk.wms.order.service;

import cargo.kityk.wms.order.exception.CommonErrorFormat;
import cargo.kityk.wms.order.exception.InvalidOrderException;
import cargo.kityk.wms.order.exception.OrderManagementException;
import cargo.kityk.wms.order.exception.ResourceNotFoundException;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Observability Tests")
public class ObservabilityTest {

    private ListAppender<ILoggingEvent> logAppender;
    
    // Pattern to match our ELK-friendly log format
    private final Pattern errorIdPattern = Pattern.compile("(?<type>[A-Z_]+)_ID=(?<id>[\\w-]+)\\s+message=(?<message>.+)");
    
    @BeforeEach
    void setUp() {
        // Set up log capture
        logAppender = new ListAppender<>();
        logAppender.start();
        
        // Configure loggers to use our appender
        ((Logger) LoggerFactory.getLogger(OrderManagementException.class)).addAppender(logAppender);
        ((Logger) LoggerFactory.getLogger(CommonErrorFormat.class)).addAppender(logAppender);
    }
    
    @AfterEach
    void tearDown() {
        // Remove and stop the appender
        ((Logger) LoggerFactory.getLogger(OrderManagementException.class)).detachAppender(logAppender);
        ((Logger) LoggerFactory.getLogger(CommonErrorFormat.class)).detachAppender(logAppender);
        logAppender.stop();
    }
    
    @Test
    @DisplayName("Should generate error ID and log in ELK-friendly format when creating CommonErrorFormat")
    void testCommonErrorFormatLogging() {
        // Arrange
        String errorDetail = "Test error detail";
        
        // Act
        CommonErrorFormat errorFormat = new CommonErrorFormat("critical", errorDetail);
        
        // Assert
        // 1. Verify log was created
        List<ILoggingEvent> logs = logAppender.list;
        assertEquals(1, logs.size(), "Should have exactly one log entry");
        
        // 2. Verify log format is ELK-friendly
        String logMessage = logs.get(0).getFormattedMessage();
        assertTrue(logMessage.contains("COMMON_ERROR_ID="), "Log should contain error ID marker");
        assertTrue(logMessage.contains(" message="), "Log should contain message marker");
        assertTrue(logMessage.contains(errorDetail), "Log should contain the error detail");
        
        // 3. Parse and verify the error ID and message
        Matcher matcher = errorIdPattern.matcher(logMessage);
        assertTrue(matcher.find(), "Log format should match expected pattern");
        assertEquals("COMMON_ERROR", matcher.group("type"), "Error type should be COMMON_ERROR");
        assertEquals(errorFormat.getId(), matcher.group("id"), "Error ID in log should match object ID");
        assertEquals(errorDetail, matcher.group("message"), "Error message should match input detail");
    }
    
    @Test
    @DisplayName("Should generate error ID and log when creating OrderManagementException")
    void testOrderManagementExceptionLogging() {
        // Arrange & Act
        InvalidOrderException exception = new InvalidOrderException("Test invalid order");
        
        // Assert
        // 1. Verify error ID is generated
        assertNotNull(exception.getErrorId(), "Error ID should be non-null");
        assertTrue(exception.getErrorId().matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"), 
                "Error ID should be a valid UUID");
                
        // 2. Verify error format is created
        assertNotNull(exception.getErrorFormat(), "Error format should be non-null");
        assertEquals(exception.getMessage(), exception.getErrorFormat().getDetail(), "Message should match");
    }
    
    @Test
    @DisplayName("Should preserve error ID when exceptions are chained")
    void testErrorIdPreservationInExceptionChain() {
        // Arrange - Create a base exception with auto-generated ID
        ResourceNotFoundException baseException = new ResourceNotFoundException("Product", 123L);
        String originalErrorId = baseException.getErrorId();
        
        // Act - Create a chained exception
        OrderManagementException chainedException = new OrderManagementException(
            "Chained exception", 
            baseException,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "critical",
            "This is a test"
        );
        
        // Assert
        assertEquals(originalErrorId, chainedException.getErrorId(), 
                "Error ID should be preserved in exception chain");
    }
    
    @Test
    @DisplayName("Should log SEVERE warning when wrapping OrderManagementException")
    void testSevereWarningLogWhenWrappingExceptions() {
        // Arrange
        ResourceNotFoundException baseException = new ResourceNotFoundException("Product", 123L);
        String originalErrorId = baseException.getErrorId();
        
        // Act - Create a chained exception
        OrderManagementException chainedException = new OrderManagementException(
            "Chained exception", 
            baseException,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "critical",
            "This is a test"
        );
        
        // Assert
        // 1. Verify error ID is preserved
        assertEquals(originalErrorId, chainedException.getErrorId(), 
                "Error ID should be preserved in exception chain");
        
        // 2. Verify SEVERE log was created
        List<ILoggingEvent> logs = logAppender.list;
        boolean foundSevereLog = false;
        
        for (ILoggingEvent log : logs) {
            if (log.getLevel().toString().equals("ERROR") && 
                log.getFormattedMessage().contains("SEVERE") &&
                log.getFormattedMessage().contains("INSTANCE OF THE INTERNAL EXCEPTION WAS RE-WRAPPED")) {
                foundSevereLog = true;
                break;
            }
        }
        
        assertTrue(foundSevereLog, "Should log SEVERE warning when an OrderManagementException is re-wrapped");
    }
} 