package cargo.kityk.wms.order.repository;

import cargo.kityk.wms.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    /**
     * Find all order items for a specific order
     * 
     * @param orderId The order ID
     * @return List of order items
     */
    List<OrderItem> findByOrderId(Long orderId);
    
    /**
     * Find all order items for a specific product
     * 
     * @param productId The product ID
     * @return List of order items
     */
    List<OrderItem> findByProductId(Long productId);
    
    /**
     * Count the number of times a product has been ordered
     * 
     * @param productId The product ID
     * @return Count of order items for the product
     */
    long countByProductId(Long productId);
    
    /**
     * Find the total quantity ordered for a specific product
     * 
     * @param productId The product ID
     * @return Total quantity ordered
     */
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.productId = :productId")
    Integer getTotalQuantityForProduct(@Param("productId") Long productId);
    
    /**
     * Delete all order items for a specific order
     * 
     * @param orderId The order ID
     */
    void deleteByOrderId(Long orderId);
} 