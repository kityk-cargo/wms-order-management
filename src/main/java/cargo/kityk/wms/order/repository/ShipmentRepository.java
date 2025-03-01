package cargo.kityk.wms.order.repository;

import cargo.kityk.wms.order.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    
    /**
     * Find all shipments for a specific order
     * 
     * @param orderId The order ID
     * @return List of shipments
     */
    List<Shipment> findByOrderId(Long orderId);
    
    /**
     * Find shipment by tracking number
     * 
     * @param trackingNumber The tracking number
     * @return Optional shipment
     */
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    
    /**
     * Find shipments by status
     * 
     * @param status The shipment status
     * @return List of shipments with the specified status
     */
    List<Shipment> findByStatus(String status);
    
    /**
     * Find shipments by carrier
     * 
     * @param carrier The carrier name
     * @return List of shipments with the specified carrier
     */
    List<Shipment> findByCarrier(String carrier);
    
    /**
     * Find shipments created on a specific date
     * 
     * @param startDate Start of the date range
     * @param endDate End of the date range
     * @return List of shipments created in the date range
     */
    List<Shipment> findByShipmentDateBetween(ZonedDateTime startDate, ZonedDateTime endDate);
    
    /**
     * Count shipments by status
     * 
     * @param status The shipment status
     * @return Count of shipments with the specified status
     */
    long countByStatus(String status);
} 