package cargo.kityk.wms.order.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Common Error Format per system specifications.
 * See: wms-main/architectural intent/common formats/error/Common Error Format.md
 * 
 * Only three fields are required:
 * - criticality: Indicates whether the process was stopped without a valid response
 * - id: A UUID for tracing the error
 * - detail: The main error message
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard error response format")
@Slf4j
@Getter
public class CommonErrorFormat {

    /**
     * Indicates whether the process requested was stopped by SERVER without valid response because of the error
     */
    @Schema(description = "Error severity level", example = "critical", 
            allowableValues = {"critical", "non-critical", "unknown"})
    private final String criticality;
    
    /**
     * An id of the error, used for tracing it through the system in the logs etc
     */
    @Schema(description = "Unique error identifier for tracing", example = "123e4567-e89b-12d3-a456-426614174000")
    private final String id;
    
    /**
     * The main error message
     */
    @Schema(description = "Main error message", example = "Order not found")
    private final String detail;
    
    /**
     * Optional array of additional errors in the same format
     */
    @Schema(description = "List of other errors in the same format")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<CommonErrorFormat> otherErrors;
    
    public CommonErrorFormat(String criticality, String detail) {
        this.criticality = criticality;
        this.id = UUID.randomUUID().toString();
        this.detail = detail;
        log.warn("COMMON_ERROR_ID={} message={}", this.id, this.detail);
    }
    
    /**
     * Create a new error format with an existing ID
     * This constructor should only be used when preserving an error ID from another error
     * 
     * @param criticality Error criticality level
     * @param detail Error detail message
     * @param existingId The existing error ID to preserve
     */
    public CommonErrorFormat(String criticality, String detail, String existingId) {
        this.criticality = criticality;
        this.id = existingId;
        this.detail = detail;
    }
    
    /**
     * Factory method for critical errors
     */
    public static CommonErrorFormat critical(String detail) {
        return new CommonErrorFormat("critical", detail);
    }
    
    /**
     * Factory method for non-critical errors
     */
    public static CommonErrorFormat nonCritical(String detail) {
        return new CommonErrorFormat("non-critical", detail);
    }
    
    /**
     * Factory method for errors with unknown criticality
     */
    public static CommonErrorFormat unknown(String detail) {
        return new CommonErrorFormat("unknown", detail);
    }

    /**
     * Adds a nested error to the otherErrors list
     * @param error The error to add
     */
    public void addOtherError(CommonErrorFormat error) {
        if (otherErrors == null) {
            otherErrors = new ArrayList<>();
        }
        otherErrors.add(error);
    }

    /**
     * Add multiple other errors at once
     * @param errors The errors to add
     */
    public void addOtherErrors(List<CommonErrorFormat> errors) {
        if (errors == null || errors.isEmpty()) {
            return;
        }
        
        if (otherErrors == null) {
            otherErrors = new ArrayList<>();
        }
        otherErrors.addAll(errors);
    }
    
    public List<CommonErrorFormat> getOtherErrors() {
        return otherErrors != null ? Collections.unmodifiableList(otherErrors) : null;
    }
} 