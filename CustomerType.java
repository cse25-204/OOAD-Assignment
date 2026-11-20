package model;

public enum CustomerType {
    INDIVIDUAL("Individual"),
    COMPANY("Company");

    private final String label;

    CustomerType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
