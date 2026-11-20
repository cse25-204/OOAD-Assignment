package dao;

import model.CompanyCustomer;
import model.Customer;
import model.CustomerType;
import model.IndividualCustomer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
/** JDBC implementation of CustomerDAO. */
public class JdbcCustomerDAO implements CustomerDAO {

    private static final String COLUMNS =
        "customer_id, customer_type, first_name, surname, id_number, company_name, "
        + "registration_number, address, phone, email";

    private final DatabaseManager db;

    public JdbcCustomerDAO(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public void save(Customer customer) {
        String sql = "INSERT INTO customers (" + COLUMNS + ") VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, customer.getCustomerId());
            ps.setString(2, customer.getCustomerType().name());
            if (customer instanceof IndividualCustomer ind) {
                ps.setString(3, ind.getFirstName());
                ps.setString(4, ind.getSurname());
                ps.setString(5, ind.getIdNumber());
                ps.setNull(6, Types.VARCHAR);
                ps.setNull(7, Types.VARCHAR);
            } else if (customer instanceof CompanyCustomer co) {
                ps.setNull(3, Types.VARCHAR);
                ps.setNull(4, Types.VARCHAR);
                ps.setNull(5, Types.VARCHAR);
                ps.setString(6, co.getCompanyName());
                ps.setString(7, co.getRegistrationNumber());
            }
            ps.setString(8, customer.getAddress());
            ps.setString(9, customer.getPhone());
            ps.setString(10, customer.getEmail());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not save customer " + customer.getCustomerId()
                    + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Customer> findById(String customerId) {
        String sql = "SELECT " + COLUMNS + " FROM customers WHERE customer_id = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read customer " + customerId + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Customer> findAll() {
        return query("SELECT " + COLUMNS + " FROM customers ORDER BY customer_id", null);
    }

    @Override
    public List<Customer> search(String text) {
        String like = "%" + text.trim().toLowerCase() + "%";
        String sql = "SELECT " + COLUMNS + " FROM customers WHERE lower(customer_id) LIKE ?"
                + " OR lower(coalesce(first_name,'') || ' ' || coalesce(surname,'')) LIKE ?"
                + " OR lower(coalesce(company_name,'')) LIKE ?"
                + " OR lower(phone) LIKE ? OR lower(email) LIKE ? ORDER BY customer_id";
        return query(sql, like);
    }

    private List<Customer> query(String sql, String likeParam) {
        List<Customer> result = new ArrayList<>();
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (likeParam != null) {
                for (int i = 1; i <= 5; i++) {
                    ps.setString(i, likeParam);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read customers: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public void update(Customer customer) {
        String sql = "UPDATE customers SET address = ?, phone = ?, email = ? WHERE customer_id = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, customer.getAddress());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getCustomerId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not update customer " + customer.getCustomerId()
                    + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String customerId) {
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM customers WHERE customer_id = ?")) {
            ps.setString(1, customerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not delete customer " + customerId + ": " + e.getMessage(), e);
        }
    }

    @Override
    public int count() {
        return DaoSupport.count(db, "SELECT COUNT(*) FROM customers");
    }

    @Override
    public String nextCustomerId() {
        long max = DaoSupport.scalarLong(db,
            "SELECT COALESCE(MAX(CAST(SUBSTR(customer_id, 2) AS INTEGER)), 0) FROM customers");
        return String.format("C%05d", max + 1);
    }

    private Customer map(ResultSet rs) throws SQLException {
        CustomerType type = CustomerType.valueOf(rs.getString("customer_type"));
        String id = rs.getString("customer_id");
        String address = rs.getString("address");
        String phone = rs.getString("phone");
        String email = rs.getString("email");
        if (type == CustomerType.INDIVIDUAL) {
            return new IndividualCustomer(id, rs.getString("first_name"), rs.getString("surname"),
                    rs.getString("id_number"), address, phone, email);
        }
        return new CompanyCustomer(id, rs.getString("company_name"), rs.getString("registration_number"),
                address, phone, email);
    }
}
