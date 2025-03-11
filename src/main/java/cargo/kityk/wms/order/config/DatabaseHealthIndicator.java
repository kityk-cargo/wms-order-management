package cargo.kityk.wms.order.config;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Component that monitors database connectivity and provides reconnection capabilities.
 * This uses HikariCP's built-in reconnection mechanisms but adds additional monitoring.
 */
@Component
public class DatabaseHealthIndicator {
    private static final Logger log = LoggerFactory.getLogger(DatabaseHealthIndicator.class);
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private HikariPoolMXBean poolProxy;
    
    @PostConstruct
    public void init() {
        if (dataSource instanceof HikariDataSource) {
            try {
                HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
                this.poolProxy = hikariDataSource.getHikariPoolMXBean();
                log.info("Database connection pool monitoring initialized");
            } catch (Exception e) {
                log.warn("Could not initialize pool monitoring: {}", e.getMessage());
            }
        } else {
            log.warn("DataSource is not a HikariDataSource, advanced monitoring unavailable");
        }
    }
    
    /**
     * Regularly checks database connectivity and logs pool statistics
     */
    @Scheduled(fixedRate = 60000) // Check every minute
    public void monitorDatabaseConnection() {
        try {
            // Check if we can execute a simple query
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            
            // If successful, log pool statistics
            if (poolProxy != null) {
                log.debug("DB Pool Stats - Active: {}, Idle: {}, Total: {}, Waiting: {}",
                    poolProxy.getActiveConnections(),
                    poolProxy.getIdleConnections(),
                    poolProxy.getTotalConnections(),
                    poolProxy.getThreadsAwaitingConnection());
            }
        } catch (DataAccessException e) {
            log.warn("Database connectivity check failed: {}", e.getMessage());
            
            // No need to manually attempt reconnection as HikariCP will handle this
            // We just log the issue for monitoring purposes
            if (poolProxy != null) {
                log.info("Connection pool status during failure - Active: {}, Idle: {}, Total: {}, Waiting: {}",
                    poolProxy.getActiveConnections(),
                    poolProxy.getIdleConnections(),
                    poolProxy.getTotalConnections(),
                    poolProxy.getThreadsAwaitingConnection());
            }
        }
    }
    
    /**
     * Used by health checks to verify database connectivity
     * @return true if database is available, false otherwise
     */
    public boolean isDatabaseHealthy() {
        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(3)) {  // 3-second timeout
                return true;
            }
        } catch (SQLException e) {
            log.warn("Database health check failed: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Gets the current pool statistics as a human-readable string
     * @return connection pool statistics
     */
    public String getPoolStatistics() {
        if (poolProxy != null) {
            return String.format("Active: %d, Idle: %d, Total: %d, Waiting Threads: %d",
                poolProxy.getActiveConnections(),
                poolProxy.getIdleConnections(),
                poolProxy.getTotalConnections(),
                poolProxy.getThreadsAwaitingConnection());
        }
        return "Pool statistics unavailable";
    }
}
