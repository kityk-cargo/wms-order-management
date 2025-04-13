package cargo.kityk.wms.order.contract;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Controller to handle setting up provider states for Pact contract testing.
 * This controller is used by the Pact Gradle Plugin to set up provider states.
 * Only active when the 'pact.verifier.publishResults' property is set.
 */
@RestController
@ConditionalOnProperty(value = "pact.verifier.publishResults")
public class PactStateController {

    private static final Logger log = LoggerFactory.getLogger(PactStateController.class);
    
    private final PactStateService pactStateService;

    @Autowired
    public PactStateController(PactStateService pactStateService) {
        this.pactStateService = pactStateService;
    }

    /**
     * Endpoint to handle provider state setup requests from Pact.
     * The Pact Gradle Plugin sends state in request body.
     * 
     * @param body Map containing the 'state' to set up
     */
    @PostMapping("/pact-states")
    public void providerState(@RequestBody Map<String, Object> body) {
        String state = (String) body.get("state");
        log.info("Setting up provider state: {}", state);
        
        switch (state) {
            case "orders exist":
                pactStateService.setupOrdersExist();
                break;
            case "order with ID 1 exists":
                pactStateService.setupOrderWithIdExists(1);
                break;
            case "order with ID 9999 does not exist":
                pactStateService.setupOrderWithIdDoesNotExist(9999);
                break;
            case "can create a new order":
                pactStateService.setupCanCreateNewOrder();
                break;
            case "server is experiencing issues":
                pactStateService.setupServerIssues();
                break;
            default:
                log.warn("Unknown provider state: {}", state);
        }
    }
} 