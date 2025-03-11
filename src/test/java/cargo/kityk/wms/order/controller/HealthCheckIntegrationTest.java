package cargo.kityk.wms.order.controller;

import cargo.kityk.wms.order.application.OrderApplication;
import cargo.kityk.wms.order.repository.CustomerRepository;
import cargo.kityk.wms.test.order.testconfig.LiquibaseFileConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = OrderApplication.class)
@ActiveProfiles("dbIntegrationTest")
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = TestConfiguration.class))
@Import(LiquibaseFileConfig.class)
@Testcontainers
@AutoConfigureMockMvc
@DisplayName("Health Check Integration Tests")
public class HealthCheckIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("wms_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("With Database Available")
    class WithDatabaseAvailable {

        @Test
        @DisplayName("Liveness probe should always return UP")
        void livenessProbe_ShouldAlwaysReturnUp() throws Exception {
            mockMvc.perform(get("/health/liveness")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", equalTo("UP")))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Readiness probe should return UP when database is available")
        void readinessProbe_WithDbUp_ShouldReturnUp() throws Exception {
            mockMvc.perform(get("/health/readiness")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", equalTo("UP")))
                    .andExpect(jsonPath("$.components.database.status", equalTo("UP")));
        }

        @Test
        @DisplayName("Startup probe should return UP when database is available")
        void startupProbe_WithDbUp_ShouldReturnUp() throws Exception {
            mockMvc.perform(get("/health/startup")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", equalTo("UP")))
                    .andExpect(jsonPath("$.components.database.status", equalTo("UP")));
        }

        @Test
        @DisplayName("Health check should return detailed status when database is available")
        void healthCheck_WithDbUp_ShouldReturnDetailedStatus() throws Exception {
            mockMvc.perform(get("/health")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", equalTo("UP")))
                    .andExpect(jsonPath("$.components.database.status", equalTo("UP")))
                    .andExpect(jsonPath("$.components.database.details.pingTime").exists())
                    .andExpect(jsonPath("$.components.database.details.countTime").exists())
                    .andExpect(jsonPath("$.components.database.details.recordCount").exists());
        }
    }

    @Nested
    @DisplayName("With Database Unavailable")
    @SpringBootTest(classes = OrderApplication.class)
    @AutoConfigureMockMvc
    @TestPropertySource(properties = {
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=none"
    })
    @ImportAutoConfiguration(exclude = {
        LiquibaseAutoConfiguration.class
    })
    class WithDatabaseUnavailable {
        
        @Autowired
        private MockMvc mockMvc;
        
        @MockitoBean
        private JdbcTemplate jdbcTemplate;
        
        @MockitoBean
        private CustomerRepository customerRepository;

        @Test
        @DisplayName("Liveness probe should return UP even when database is down")
        void livenessProbe_WithDbDown_ShouldReturnUp() throws Exception {
            simulateDatabaseFailure();
            
            mockMvc.perform(get("/health/liveness")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", equalTo("UP")));
        }

        @Test
        @DisplayName("Readiness probe should return DOWN when database is unavailable")
        void readinessProbe_WithDbDown_ShouldReturnDown() throws Exception {
            simulateDatabaseFailure();
            
            mockMvc.perform(get("/health/readiness")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.status", equalTo("DOWN")))
                    .andExpect(jsonPath("$.components.database.status", equalTo("DOWN")));
        }

        @Test
        @DisplayName("Startup probe should return DOWN when database is unavailable")
        void startupProbe_WithDbDown_ShouldReturnDown() throws Exception {
            simulateDatabaseFailure();
            
            mockMvc.perform(get("/health/startup")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.status", equalTo("DOWN")))
                    .andExpect(jsonPath("$.components.database.status", equalTo("DOWN")));
        }

        @Test
        @DisplayName("Health check should return detailed status with DOWN when database is unavailable")
        void healthCheck_WithDbDown_ShouldReturnDetailedStatus() throws Exception {
            simulateDatabaseFailure();
            
            mockMvc.perform(get("/health")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.status", equalTo("DOWN")))
                    .andExpect(jsonPath("$.components.database.status", equalTo("DOWN")))
                    .andExpect(jsonPath("$.components.database.details.error").exists())
                    .andExpect(jsonPath("$.components.database.details.errorType").exists());
        }
        
        private void simulateDatabaseFailure() {
            String errorMessage = "Database connection failed";
            DataAccessResourceFailureException exception = new DataAccessResourceFailureException(errorMessage);

            when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class)))
                .thenThrow(exception);
            when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class)))
                .thenThrow(exception);
            when(customerRepository.count())
                .thenThrow(exception);
        }
    }
}
