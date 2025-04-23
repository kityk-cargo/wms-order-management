package cargo.kityk.wms.order.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to enable Feign clients in the application.
 * Scans for interfaces annotated with @FeignClient in the specified package.
 */
@Configuration
@EnableFeignClients(basePackages = "cargo.kityk.wms.order.service.client")
public class FeignClientConfig {
} 