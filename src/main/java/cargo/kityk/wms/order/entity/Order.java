package cargo.kityk.wms.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", schema = "wms_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @Column(name = "order_date", nullable = false)
    private ZonedDateTime orderDate;
    
    @Column(nullable = false)
    private String status;
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Shipment> shipments = new ArrayList<>();
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();
    
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
    
    // Helper methods to maintain bidirectional relationships
    public void addOrderItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
    
    public void removeOrderItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }
    
    public void addShipment(Shipment shipment) {
        shipments.add(shipment);
        shipment.setOrder(this);
    }
    
    public void removeShipment(Shipment shipment) {
        shipments.remove(shipment);
        shipment.setOrder(null);
    }
    
    public void addPayment(Payment payment) {
        payments.add(payment);
        payment.setOrder(this);
    }
    
    public void removePayment(Payment payment) {
        payments.remove(payment);
        payment.setOrder(null);
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = ZonedDateTime.now();
        if (orderDate == null) {
            orderDate = ZonedDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }
} 