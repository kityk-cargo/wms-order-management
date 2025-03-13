package cargo.kityk.wms.order.openapi;

import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.MergedAnnotation;

import static org.springframework.context.annotation.AnnotationConfigUtils.AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME;

@SpringBootApplication
@Profile("openapi")
@EnableAutoConfiguration(exclude = {
    org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
    org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class
})
@ComponentScan(basePackages = {"cargo.kityk.wms.order.controller", "cargo.kityk.wms.order.dto", "cargo.kityk.wms.order.config"})
public class OpenApiGeneratorApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(OpenApiGeneratorApplication.class);
        app.setWebApplicationType(WebApplicationType.SERVLET);
        app.setAllowBeanDefinitionOverriding(true);
        app.run(args);
    }

    /**
     * The name is needed here because this bean is already loaded with such name at ComponentScan processing time.
     * We reload this bean with our custom processor to turn off mandatory autowiring.
     * @return
     */
    @Bean(name = AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME)
    @Primary
    public static AutowiredAnnotationBeanPostProcessor autowiredAnnotationBeanPostProcessor() {
        return new CustomAutowiredAnnotationBeanPostProcessor();
    }

    /**
     * Custom AutowiredAnnotationBeanPostProcessor that turns off autowiring by forcing
     * all @Autowired annotations to act as if required=false.
     */
    private static class CustomAutowiredAnnotationBeanPostProcessor extends AutowiredAnnotationBeanPostProcessor {
        @Override
        protected boolean determineRequiredStatus(MergedAnnotation<?> ann) {
            return false;
        }
    }
}
