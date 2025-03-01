package cargo.kityk.wms.order

import groovy.transform.CompileStatic
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.GetMapping

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag

@CompileStatic
@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "Health check endpoints")
class HealthController {

    @GetMapping
    @Operation(
        summary = "Health check",
        description = "Returns health status of the service",
        responses = [
            @ApiResponse(responseCode = "200", description = "Service is healthy")
        ]
    )
    Map<String, String> healthCheck() {
        return [status: "UP"]
    }
} 