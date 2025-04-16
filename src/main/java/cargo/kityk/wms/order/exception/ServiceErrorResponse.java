package cargo.kityk.wms.order.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Standardized error response for the API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceErrorResponse {

    private final String error;
    private final int status;
    private final LocalDateTime timestamp;

    //todo: I won't touch it now since the common error format is in the pipeline
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Object> details;

    public ServiceErrorResponse(String error, int status) {
        this.error = error;
        this.status = status;
        this.timestamp = LocalDateTime.now();
        this.details = new HashMap<>();
    }

    public String getError() {
        return error;
    }

    public int getStatus() {
        return status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public Map<String, Object> getDetails() {
        return details;
    }
    
    public void addDetail(String key, Object value) {
        if (this.details == null) {
            this.details = new HashMap<>();
        }
        this.details.put(key, value);
    }
} 