package cargo.kityk.wms.order;

import cargo.kityk.wms.order.application.OrderApplication;
import cargo.kityk.wms.order.dto.*;
import cargo.kityk.wms.test.order.testconfig.UnitTestConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(classes = OrderApplication.class)
@Import(UnitTestConfiguration.class)
@AutoConfigureMockMvc
public class OrderControllerTests {

    private static final String BASE_URL = "/api/v1/orders";
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should create a new order and return 201 Created status")
    void testCreateOrder() throws Exception {
        // Create test data using our DTO builders
        OrderItemCreateDTO orderItem = OrderItemCreateDTO.builder()
                .productId(1L)
                .quantity(2)
                .build();
                
        OrderCreateDTO orderCreate = OrderCreateDTO.builder()
                .customerId(1L)
                .items(Collections.singletonList(orderItem))
                .build();
                
        String jsonContent = objectMapper.writeValueAsString(orderCreate);
        
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("Pending"));
    }

    @Test
    @DisplayName("Should return an empty array of orders with 200 OK status")
    void testGetOrders() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Should return a specific order by ID with 200 OK status")
    void testGetOrder() throws Exception {
        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("Should update an existing order and return 200 OK status")
    void testUpdateOrder() throws Exception {
        // Create test data using our DTO builders
        OrderItemDTO orderItem = OrderItemDTO.builder()
                .id(1L)
                .productId(1L)
                .quantity(2)
                .price(new BigDecimal("49.99"))
                .build();
                
        OrderDTO orderDTO = OrderDTO.builder()
                .id(1L)
                .customerId(1L)
                .status("Processing")
                .totalAmount(new BigDecimal("99.99"))
                .items(Collections.singletonList(orderItem))
                .build();
                
        String jsonContent = objectMapper.writeValueAsString(orderDTO);
        
        mockMvc.perform(put(BASE_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Processing"));
    }

    @Test
    @DisplayName("Should delete an order and return 204 No Content status")
    void testDeleteOrder() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should allocate inventory for an order and return 200 OK status")
    void testAllocateInventory() throws Exception {
        mockMvc.perform(post(BASE_URL + "/1/allocate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Allocated"));
    }

    @Test
    @DisplayName("Should update order status and return 200 OK status")
    void testUpdateOrderStatus() throws Exception {
        // Create test data using our DTO builder
        OrderStatusDTO statusDTO = OrderStatusDTO.builder()
                .status("Processing")
                .build();
                
        String jsonContent = objectMapper.writeValueAsString(statusDTO);
        
        mockMvc.perform(put(BASE_URL + "/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Processing"));
    }
}
