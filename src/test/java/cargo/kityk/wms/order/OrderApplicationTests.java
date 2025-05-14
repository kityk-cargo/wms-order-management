package cargo.kityk.wms.order;

import cargo.kityk.wms.order.application.OrderApplication;
import cargo.kityk.wms.test.order.testconfig.UnitTestConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;


@SpringBootTest(classes = OrderApplication.class)
@Import(UnitTestConfiguration.class)
class OrderApplicationTests {

	@Test
	@DisplayName("Context is loaded (application startup test)")
	void contextLoads() {
	}

}
