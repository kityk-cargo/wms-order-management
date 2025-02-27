package cargo.kityk.wms.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(
    exclude = {
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class
        // Don't exclude the main application class
    }
)
@ComponentScan(basePackages = "cargo.kityk.wms") // Ensure we scan all components
public class OpenApiGeneratorApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(OpenApiGeneratorApplication.class);
        app.setWebApplicationType(WebApplicationType.SERVLET);
        app.run(args);
    }
}
