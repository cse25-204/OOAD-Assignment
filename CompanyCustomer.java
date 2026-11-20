package model;

/** A company or organisation that banks with SM BANKING. */
public class CompanyCustomer extends Customer {

    private final String companyName;
    private final String registrationNumber;

    public CompanyCustomer(String customerId, String companyName, String registrationNumber,
                           String address, String phone, String email) {
        super(customerId, address, phone, email);
        this.companyName = companyName;
        this.registrationNumber = registrationNumber;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    @Override
    public CustomerType getCustomerType() {
        return CustomerType.COMPANY;
    }

    @Override
    public String getDisplayName() {
        return companyName;
    }
}
