package cargo.kityk.wms.order.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Common Error Format")
class CommonErrorFormatTest {

    @Test
    @DisplayName("Should create a critical error with proper attributes")
    void shouldCreateCriticalError() {
        // Arrange
        String errorDetail = "Critical error message";
        
        // Act
        CommonErrorFormat error = CommonErrorFormat.critical(errorDetail);
        
        // Assert
        assertEquals("critical", error.getCriticality());
        assertEquals(errorDetail, error.getDetail());
        assertNotNull(error.getId());
        assertTrue(error.getId().length() > 0);
    }
    
    @Test
    @DisplayName("Should create a non-critical error with proper attributes")
    void shouldCreateNonCriticalError() {
        // Arrange
        String errorDetail = "Non-critical error message";
        
        // Act
        CommonErrorFormat error = CommonErrorFormat.nonCritical(errorDetail);
        
        // Assert
        assertEquals("non-critical", error.getCriticality());
        assertEquals(errorDetail, error.getDetail());
        assertNotNull(error.getId());
    }
    
    @Test
    @DisplayName("Should create an error with unknown criticality")
    void shouldCreateUnknownCriticalityError() {
        // Arrange
        String errorDetail = "Unknown criticality error message";
        
        // Act
        CommonErrorFormat error = CommonErrorFormat.unknown(errorDetail);
        
        // Assert
        assertEquals("unknown", error.getCriticality());
        assertEquals(errorDetail, error.getDetail());
        assertNotNull(error.getId());
    }
    
    @Test
    @DisplayName("Should generate unique IDs for each error instance")
    void shouldGenerateUniqueIdsForEachError() {
        // Arrange
        String errorDetail = "Error message";
        
        // Act
        CommonErrorFormat error1 = CommonErrorFormat.critical(errorDetail);
        CommonErrorFormat error2 = CommonErrorFormat.critical(errorDetail);
        
        // Assert
        assertNotEquals(error1.getId(), error2.getId());
    }
    
    @Test
    @DisplayName("Should add a single nested error to the otherErrors list")
    void shouldAddSingleOtherError() {
        // Arrange
        CommonErrorFormat mainError = CommonErrorFormat.critical("Main error");
        CommonErrorFormat otherError = CommonErrorFormat.critical("Other error");
        
        // Act
        mainError.addOtherError(otherError);
        
        // Assert
        assertNotNull(mainError.getOtherErrors());
        assertEquals(1, mainError.getOtherErrors().size());
        assertEquals(otherError.getDetail(), mainError.getOtherErrors().get(0).getDetail());
    }
    
    @Test
    @DisplayName("Should add multiple nested errors to the otherErrors list")
    void shouldAddMultipleOtherErrors() {
        // Arrange
        CommonErrorFormat mainError = CommonErrorFormat.critical("Main error");
        CommonErrorFormat otherError1 = CommonErrorFormat.critical("Other error 1");
        CommonErrorFormat otherError2 = CommonErrorFormat.critical("Other error 2");
        List<CommonErrorFormat> otherErrors = Arrays.asList(otherError1, otherError2);
        
        // Act
        mainError.addOtherErrors(otherErrors);
        
        // Assert
        assertNotNull(mainError.getOtherErrors());
        assertEquals(2, mainError.getOtherErrors().size());
        assertEquals(otherError1.getDetail(), mainError.getOtherErrors().get(0).getDetail());
        assertEquals(otherError2.getDetail(), mainError.getOtherErrors().get(1).getDetail());
    }
    
    @Test
    @DisplayName("Should handle null or empty collections for otherErrors")
    void shouldHandleNullOrEmptyOtherErrors() {
        // Arrange
        CommonErrorFormat mainError = CommonErrorFormat.critical("Main error");
        
        // Act
        mainError.addOtherErrors(null);
        
        // Assert
        assertNull(mainError.getOtherErrors());
        
        // Act again with empty list
        mainError.addOtherErrors(Arrays.asList());
        
        // Assert again
        assertNull(mainError.getOtherErrors());
    }
} 