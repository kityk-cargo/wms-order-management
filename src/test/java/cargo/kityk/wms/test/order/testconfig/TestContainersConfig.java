package cargo.kityk.wms.test.order.testconfig;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

/**
 * Centralized TestContainers configuration to be used by all database integration tests.
 * This avoids duplicating the container setup in multiple test classes.
 */
public abstract class TestContainersConfig {

    /**
     * Shared PostgreSQL container for tests
     */
    @Container
    protected static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("wms_test")
            .withUsername("test")
            .withPassword("test");

    /**
     * Configures Spring properties for the database connection
     * 
     * @param registry Dynamic property registry
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }
} 