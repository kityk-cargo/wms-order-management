package cargo.kityk.wms.order.controller

import cargo.kityk.wms.order.repository.CustomerRepository
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.tags.Tag

@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "Kubernetes health check endpoints")
@CompileStatic
class K8sChecksController {

    @Autowired
    private JdbcTemplate jdbcTemplate
    
    @Autowired
    private CustomerRepository customerRepository

    @GetMapping(value = "/liveness", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Liveness probe",
        description = "Determines if the application is running. Used by Kubernetes to know when to restart the container.",
        responses = [
            @ApiResponse(
                responseCode = "200", 
                description = "Application is alive",
                content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(responseCode = "500", description = "Application is not functioning correctly")
        ]
    )
    ResponseEntity<Map<String, Object>> livenessCheck() {
        // Explicitly type the map for static type checking
        Map<String, Object> response = [
            status: 'UP', 
            timestamp: new Date()
        ]
        return ResponseEntity.ok(response)
    }

    @GetMapping(value = "/readiness", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Readiness probe",
        description = "Determines if the service is ready to receive traffic, including database availability.",
        responses = [
            @ApiResponse(
                responseCode = "200", 
                description = "Service is ready to receive traffic",
                content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                responseCode = "503", 
                description = "Service is not ready (e.g., database unavailable)",
                content = @Content(mediaType = "application/json")
            )
        ]
    )
    ResponseEntity<Map<String, Object>> readinessCheck() {
        // Explicitly define map types
        Map<String, Object> response = [
            status: 'UP',
            timestamp: new Date()
        ]
        
        // Create components map separately
        Map<String, Object> components = [:]
        response.put('components', components)
        
        // Check database connection
        boolean dbStatus = isDatabaseAvailable()
        components.put('database', [status: dbStatus ? 'UP' : 'DOWN'])
        
        // Return appropriate response based on status
        if (dbStatus) {
            return ResponseEntity.ok(response)
        } else {
            response.put('status', 'DOWN')
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response)
        }
    }

    @GetMapping(value = "/startup", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Startup probe",
        description = "Determines if the application has started correctly.",
        responses = [
            @ApiResponse(
                responseCode = "200", 
                description = "Application has started successfully",
                content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                responseCode = "503", 
                description = "Application is still starting up",
                content = @Content(mediaType = "application/json")
            )
        ]
    )
    ResponseEntity<Map<String, Object>> startupCheck() {
        Map<String, Object> response = [
            status: 'UP',
            timestamp: new Date()
        ]
        
        Map<String, Object> components = [:]
        response.put('components', components)
        
        boolean dbStatus = isDatabaseAvailable()
        components.put('database', [status: dbStatus ? 'UP' : 'DOWN'])
        
        if (dbStatus) {
            return ResponseEntity.ok(response)
        } else {
            response.put('status', 'DOWN')
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response)
        }
    }
    
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Overall health check",
        description = "Returns comprehensive health status of the service",
        responses = [
            @ApiResponse(
                responseCode = "200", 
                description = "Service is healthy",
                content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                responseCode = "503", 
                description = "Service has issues",
                content = @Content(mediaType = "application/json")
            )
        ]
    )
    ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> status = [
            status: 'UP',
            timestamp: new Date()
        ]
        
        Map<String, Object> components = [:]
        status.put('components', components)
        
        Map<String, Object> dbConnectionStatus = checkDatabaseConnection()
        components.put('database', dbConnectionStatus)
        
        if (dbConnectionStatus.get('status') == 'DOWN') {
            status.put('status', 'DOWN')
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(status)
        } else {
            return ResponseEntity.ok(status)
        }
    }
    
    private Map<String, Object> checkDatabaseConnection() {
        Map<String, Object> status = [status: 'DOWN']
        Map<String, Object> details = [:]
        status.put('details', details)
        
        try {
            // Create a type-safe timing function
            long startTime = System.currentTimeMillis()
            Integer pingResult = jdbcTemplate.queryForObject('SELECT 1', Integer)
            long pingTime = System.currentTimeMillis() - startTime
            
            startTime = System.currentTimeMillis()
            long countResult = customerRepository.count()
            long countTime = System.currentTimeMillis() - startTime
            
            // Update the status map - Use String concatenation instead of GString interpolation
            status.put('status', 'UP')
            details.put('pingTime', pingTime.toString() + "ms")
            details.put('countTime', countTime.toString() + "ms")
            details.put('recordCount', countResult)
            
            return status
        } catch (DataAccessException exception) {
            // Handle exception details
            details.put('error', exception.message)
            details.put('errorType', exception.class.simpleName)
            if (exception.cause != null) {
                details.put('cause', exception.cause.message)
            }
            details.put('timestamp', new Date())
            
            return status
        }
    }
    
    private boolean isDatabaseAvailable() {
        try {
            Map<String, Object> connectionStatus = checkDatabaseConnection()
            return connectionStatus.get('status') == 'UP'
        } catch (Exception e) {
            return false
        }
    }
}