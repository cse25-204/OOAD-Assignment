package model;

/**
 * Abstract base class for anyone who banks with SM BANKING.
 * A customer is either an individual or a company (inheritance + abstraction).
 */
public abstract class Customer {

    private final String customerId;
    private String address;
    private String phone;
    private String email;

    protected Customer(String customerId, String address, String phone, String email) {
        this.customerId = customerId;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /** Individual or company. */
    public abstract CustomerType getCustomerType();

    /** Full name of the person, or the company's trading name. */
    public abstract String getDisplayName();

    @Override
    public String toString() {
        return customerId + " - " + getDisplayName();
    }
}
