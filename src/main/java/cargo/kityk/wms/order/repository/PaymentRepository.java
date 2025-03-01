package cargo.kityk.wms.order.repository;

import cargo.kityk.wms.order.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    /**
     * Find all payments for a specific order
     * 
     * @param orderId The order ID
     * @return List of payments
     */
    List<Payment> findByOrderId(Long orderId);
    
    /**
     * Find payment by transaction ID
     * 
     * @param transactionId The transaction ID
     * @return Optional payment
     */
    Optional<Payment> findByTransactionId(String transactionId);
    
    /**
     * Find payments by status
     * 
     * @param status The payment status
     * @return List of payments with the specified status
     */
    List<Payment> findByStatus(String status);
    
    /**
     * Find payments by payment method
     * 
     * @param paymentMethod The payment method
     * @return List of payments with the specified payment method
     */
    List<Payment> findByPaymentMethod(String paymentMethod);
    
    /**
     * Find payments created on a specific date
     * 
     * @param startDate Start of the date range
     * @param endDate End of the date range
     * @return List of payments created in the date range
     */
    List<Payment> findByPaymentDateBetween(ZonedDateTime startDate, ZonedDateTime endDate);
    
    /**
     * Calculate the total amount of payments for an order
     * 
     * @param orderId The order ID
     * @return Total payment amount
     */
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.order.id = :orderId AND p.status = 'Completed'")
    BigDecimal getTotalPaymentsForOrder(@Param("orderId") Long orderId);
    
    /**
     * Count payments by status
     * 
     * @param status The payment status
     * @return Count of payments with the specified status
     */
    long countByStatus(String status);
} 