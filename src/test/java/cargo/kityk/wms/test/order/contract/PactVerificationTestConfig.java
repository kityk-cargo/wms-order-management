package cargo.kityk.wms.test.order.contract;

import cargo.kityk.wms.test.order.testconfig.LiquibaseFileConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

/**
 * Test configuration for Pact verification tests.
 * This configuration imports the LiquibaseFileConfig to reuse the database setup.
 * Includes test controllers that can simulate different states for contract tests.
 */
@TestConfiguration
@Import(LiquibaseFileConfig.class)
@Profile("dbIntegrationTest")
public class PactVerificationTestConfig {
    private static final Logger logger = LoggerFactory.getLogger(PactVerificationTestConfig.class);
    
    /**
     * Bean to log that the Pact verification configuration is loaded
     */
    @Bean
    public String pactConfigurationLoaded() {
        logger.info("=== Pact Verification Test Configuration Loaded ===");
        return "pactConfigurationLoaded";
    }
}