package cargo.kityk.wms.order;

import cargo.kityk.wms.order.application.OrderApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;


@SpringBootTest(classes = TestConfiguration.class)
@Import(TestConfiguration.class)
class OrderApplicationTests {

	@Test
	void contextLoads() {
	}

}
