package cargo.kityk.wms.test.order.testconfig;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;

import javax.sql.DataSource;

@Configuration
@Profile("dbIntegrationTest")
public class LiquibaseFileConfig {

    @Bean
    public SpringLiquibase liquibase(DataSource dataSource, ResourceLoader resourceLoader) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setResourceLoader(resourceLoader);
        
        // Use classpath resource for TestContainers environment
        liquibase.setChangeLog("classpath:db/changelog-test.xml");
        
        return liquibase;
    }
}
