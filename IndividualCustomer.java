package model;

/** A private person (firstname, surname, national ID number). */
public class IndividualCustomer extends Customer {

    private final String firstName;
    private final String surname;
    private final String idNumber;

    public IndividualCustomer(String customerId, String firstName, String surname, String idNumber,
                              String address, String phone, String email) {
        super(customerId, address, phone, email);
        this.firstName = firstName;
        this.surname = surname;
        this.idNumber = idNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getSurname() {
        return surname;
    }

    public String getIdNumber() {
        return idNumber;
    }

    @Override
    public CustomerType getCustomerType() {
        return CustomerType.INDIVIDUAL;
    }

    @Override
    public String getDisplayName() {
        return firstName + " " + surname;
    }
}
