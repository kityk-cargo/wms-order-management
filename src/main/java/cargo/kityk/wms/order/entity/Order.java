package cargo.kityk.wms.order.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", schema = "wms_schema")
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
    
    public Order() { }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    
    public ZonedDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(ZonedDateTime orderDate) { this.orderDate = orderDate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    
    public List<Shipment> getShipments() { return shipments; }
    public void setShipments(List<Shipment> shipments) { this.shipments = shipments; }
    
    public List<Payment> getPayments() { return payments; }
    public void setPayments(List<Payment> payments) { this.payments = payments; }
    
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
    
    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Helper methods for associations
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order other = (Order) o;
        // Use customer's email and orderDate as the business key.
        String thisEmail = (customer != null) ? customer.getEmail() : null;
        String otherEmail = (other.customer != null) ? other.customer.getEmail() : null;
        return thisEmail != null &&
               thisEmail.equals(otherEmail) &&
               orderDate != null &&
               orderDate.equals(other.orderDate);
    }

    @Override
    public int hashCode() {
        int result = (customer != null && customer.getEmail() != null) ? customer.getEmail().hashCode() : 0;
        result = 31 * result + (orderDate != null ? orderDate.hashCode() : 0);
        return result;
    }
    
    @Override
    public String toString() {
        return "Order{" +
               "id=" + id +
               ", orderDate=" + orderDate +
               ", status='" + status + '\'' +
               ", totalAmount=" + totalAmount +
               '}';
    }
}