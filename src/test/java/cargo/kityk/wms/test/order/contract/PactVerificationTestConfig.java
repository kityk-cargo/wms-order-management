package cargo.kityk.wms.test.order.contract;

import cargo.kityk.wms.test.order.testconfig.LiquibaseFileConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.TestPropertySource;

/**
 * Test configuration for Pact verification tests.
 * This configuration imports the LiquibaseFileConfig to reuse the database setup.
 * Includes test controllers that can simulate different states for contract tests.
 */
@TestConfiguration
@Import(LiquibaseFileConfig.class)
@Profile("dbIntegrationTest")
@TestPropertySource(properties = {
    "spring.datasource.url=${container.jdbc.url:jdbc:postgresql://localhost:5432/order_management_test}",
    "spring.datasource.username=${container.jdbc.username:test}",
    "spring.datasource.password=${container.jdbc.password:test}",
    "spring.datasource.driver-class-name=${container.jdbc.driver:org.postgresql.Driver}",
    "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.jpa.show-sql=true",
    "logging.level.org.springframework=INFO",
    "logging.level.au.com.dius.pact=DEBUG",
    "logging.level.cargo.kityk.wms=DEBUG"
})
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