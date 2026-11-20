package dao;

import model.Customer;

import java.util.List;
import java.util.Optional;
/** CRUD operations for customers. */
public interface CustomerDAO {

    void save(Customer customer);                    // Create

    Optional<Customer> findById(String customerId);  // Read

    List<Customer> findAll();                        // Read

    List<Customer> search(String text);              // Read (id, name, phone, email)

    void update(Customer customer);                  // Update (contact details)

    void delete(String customerId);                  // Delete

    int count();

    /** Next free customer id, e.g. C00011. */
    String nextCustomerId();
}
