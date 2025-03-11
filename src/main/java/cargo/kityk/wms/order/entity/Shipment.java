package cargo.kityk.wms.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.ZonedDateTime;

@Entity
@Table(name = "shipments", schema = "wms_schema")
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Column(name = "shipment_date")
    private ZonedDateTime shipmentDate;
    
    private String carrier;
    
    @Column(name = "tracking_number")
    private String trackingNumber;
    
    @Column(nullable = false)
    private String status;
    
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
    
    public Shipment() { }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    
    public ZonedDateTime getShipmentDate() { return shipmentDate; }
    public void setShipmentDate(ZonedDateTime shipmentDate) { this.shipmentDate = shipmentDate; }
    
    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }
    
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
    
    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = ZonedDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Shipment that = (Shipment) o;
        // Use trackingNumber as the immutable business key
        return trackingNumber != null && trackingNumber.equals(that.trackingNumber);
    }

    @Override
    public int hashCode() {
        return trackingNumber != null ? trackingNumber.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "Shipment{" +
               "id=" + id +
               ", carrier='" + carrier + '\'' +
               ", trackingNumber='" + trackingNumber + '\'' +
               ", status='" + status + '\'' +
               '}';
    }
}