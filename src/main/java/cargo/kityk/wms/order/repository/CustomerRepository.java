package cargo.kityk.wms.order.repository;

import cargo.kityk.wms.order.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    /**
     * Find a customer by their email address
     * 
     * @param email The email address to search for
     * @return An Optional containing the customer if found
     */
    Optional<Customer> findByEmail(String email);
    
    /**
     * Check if a customer exists with the given email
     * 
     * @param email The email to check
     * @return true if a customer exists with this email, false otherwise
     */
    boolean existsByEmail(String email);
} 