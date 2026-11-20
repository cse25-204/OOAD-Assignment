package controller;

import dao.AccountDAO;
import dao.CustomerDAO;
import dao.UserDAO;
import model.Bank;
import model.BankException;
import model.CompanyCustomer;
import model.Customer;
import model.IndividualCustomer;
import model.Role;
import model.User;
import util.Validator;

import java.util.List;
/** Customer registration and maintenance. Validates all input before it reaches the model. */
public class CustomerController {

    private final CustomerDAO customers;
    private final AccountDAO accounts;
    private final UserDAO users;
    private final Session session;

    public CustomerController(CustomerDAO customers, AccountDAO accounts, UserDAO users, Session session) {
        this.customers = customers;
        this.accounts = accounts;
        this.users = users;
        this.session = session;
    }

    /** Registers a person and creates their login. Anyone may register (no login needed). */
    public Customer registerIndividual(String firstName, String surname, String idNumber,
                                       String address, String phone, String email,
                                       String username, String password, String confirmPassword)
            throws BankException {
        String first = Validator.requireName(firstName, "First name");
        String last = Validator.requireName(surname, "Surname");
        String id = Validator.requireNotBlank(idNumber, "ID number");
        String addr = Validator.requireNotBlank(address, "Address");
        String ph = Validator.requirePhone(phone);
        String mail = Validator.requireEmail(email);
        String user = Validator.requireUsername(username);
        Validator.requirePassword(password, confirmPassword);
        ensureUsernameFree(user);

        Customer customer = new IndividualCustomer(customers.nextCustomerId(), first, last, id, addr, ph, mail);
        customers.save(customer);
        users.save(User.create(user, password, Role.CUSTOMER, customer.getCustomerId()));
        return customer;
    }

    /** Registers a company or organisation and creates its login. */
    public Customer registerCompany(String companyName, String registrationNumber,
                                    String address, String phone, String email,
                                    String username, String password, String confirmPassword)
            throws BankException {
        String name = Validator.requireNotBlank(companyName, "Company name");
        String reg = Validator.requireNotBlank(registrationNumber, "Registration number");
        String addr = Validator.requireNotBlank(address, "Address");
        String ph = Validator.requirePhone(phone);
        String mail = Validator.requireEmail(email);
        String user = Validator.requireUsername(username);
        Validator.requirePassword(password, confirmPassword);
        ensureUsernameFree(user);

        Customer customer = new CompanyCustomer(customers.nextCustomerId(), name, reg, addr, ph, mail);
        customers.save(customer);
        users.save(User.create(user, password, Role.CUSTOMER, customer.getCustomerId()));
        return customer;
    }

    /** Teller only: every customer, optionally filtered by a search text. */
    public List<Customer> findCustomers(String searchText) throws BankException {
        session.requireTeller();
        if (searchText == null || searchText.trim().isEmpty()) {
            return customers.findAll();
        }
        return customers.search(searchText);
    }

    /** The customer record of whoever is logged in (customers only). */
    public Customer getMyCustomer() throws BankException {
        User user = session.requireLogin();
        if (user.isTeller()) {
            throw new BankException("Bank staff do not have a customer record.");
        }
        return customers.findById(user.getCustomerId())
                .orElseThrow(() -> new BankException("Customer record not found."));
    }

    public Customer updateContactDetails(String customerId, String address, String phone, String email)
            throws BankException {
        session.requireAccessToCustomer(customerId);
        Customer customer = customers.findById(customerId)
                .orElseThrow(() -> new BankException("Customer " + customerId + " not found."));
        customer.setAddress(Validator.requireNotBlank(address, "Address"));
        customer.setPhone(Validator.requirePhone(phone));
        customer.setEmail(Validator.requireEmail(email));
        customers.update(customer);
        return customer;
    }

    /** Teller only. A customer who still holds accounts cannot be deleted. */
    public void deleteCustomer(String customerId) throws BankException {
        session.requireTeller();
        if (!accounts.findByCustomer(customerId).isEmpty()) {
            throw new BankException("Customer " + customerId
                    + " still holds accounts and cannot be deleted.");
        }
        customers.delete(customerId);
    }

    private void ensureUsernameFree(String username) throws BankException {
        if (users.existsByUsername(username)) {
            throw new BankException("The username '" + username + "' is already taken.");
        }
    }
}
