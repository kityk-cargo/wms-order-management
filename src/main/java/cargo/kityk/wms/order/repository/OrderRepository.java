package cargo.kityk.wms.order.repository;

import cargo.kityk.wms.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * Find all orders for a specific customer
     * 
     * @param customerId The customer ID
     * @return List of orders for the customer
     */
    List<Order> findByCustomerId(Long customerId);
    
    /**
     * Find all orders for a specific customer with pagination
     * 
     * @param customerId The customer ID
     * @param pageable Pagination information
     * @return Page of orders for the customer
     */
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);
    
    /**
     * Find orders by status
     * 
     * @param status The order status to filter by
     * @return List of orders with the specified status
     */
    List<Order> findByStatus(String status);
    
    /**
     * Find orders by status with pagination
     * 
     * @param status The order status to filter by
     * @param pageable Pagination information
     * @return Page of orders with the specified status
     */
    Page<Order> findByStatus(String status, Pageable pageable);
    
    /**
     * Find orders created between two dates
     * 
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return List of orders created in the date range
     */
    List<Order> findByOrderDateBetween(ZonedDateTime startDate, ZonedDateTime endDate);
    
    /**
     * Find orders for a customer with a specific status
     * 
     * @param customerId The customer ID
     * @param status The order status
     * @return List of matching orders
     */
    List<Order> findByCustomerIdAndStatus(Long customerId, String status);
    
    /**
     * Count orders by status
     * 
     * @param status The order status
     * @return Count of orders with the specified status
     */
    long countByStatus(String status);
    
    /**
     * Find recent orders with a limit
     * 
     * @param limit Maximum number of orders to return
     * @return List of recent orders
     */
    @Query(value = "SELECT o FROM Order o ORDER BY o.orderDate DESC")
    List<Order> findRecentOrders(Pageable pageable);
    
    /**
     * Search orders by customer name (case insensitive)
     * 
     * @param customerName The customer name to search for
     * @return List of matching orders
     */
    @Query("SELECT o FROM Order o JOIN o.customer c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :customerName, '%'))")
    List<Order> searchByCustomerName(@Param("customerName") String customerName);
} 