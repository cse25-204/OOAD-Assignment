package view;

import controller.AppContext;
import dao.DataAccessException;
import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Window;
import javafx.beans.property.SimpleStringProperty;
import model.BankException;
import model.Customer;

public class CustomersView implements Refreshable {

    @FXML private TextField searchField;
    @FXML private TableView<Customer> table;
    @FXML private TableColumn<Customer, String> idCol;
    @FXML private TableColumn<Customer, String> typeCol;
    @FXML private TableColumn<Customer, String> nameCol;
    @FXML private TableColumn<Customer, String> addressCol;
    @FXML private TableColumn<Customer, String> phoneCol;
    @FXML private TableColumn<Customer, String> emailCol;

    private AppContext context;

    @FXML
    private void initialize() {
        idCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getCustomerId()));
        typeCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getCustomerType().toString()));
        nameCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getDisplayName()));
        addressCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getAddress()));
        phoneCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getPhone()));
        emailCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getEmail()));
    }

    void setContext(AppContext context) {
        this.context = context;
        refresh();
    }

    @FXML
    private void onSearch() {
        refresh();
    }

    @FXML
    private void onShowAll() {
        searchField.clear();
        refresh();
    }

    @FXML
    private void onRegister() {
        RegisterView.show(context, table.getScene().getWindow(), this::refresh);
    }

    @FXML
    private void onUpdate() {
        Customer customer = table.getSelectionModel().getSelectedItem();
        if (customer == null) {
            UiUtil.showError(table.getScene().getWindow(), "Select a customer first.");
            return;
        }
        String address = prompt("Address", customer.getAddress());
        if (address == null) {
            return;
        }
        String phone = prompt("Phone", customer.getPhone());
        if (phone == null) {
            return;
        }
        String email = prompt("Email", customer.getEmail());
        if (email == null) {
            return;
        }
        try {
            context.customers().updateContactDetails(customer.getCustomerId(), address, phone, email);
            refresh();
        } catch (BankException | DataAccessException e) {
            UiUtil.showError(table.getScene().getWindow(), e.getMessage());
        }
    }

    @FXML
    private void onDelete() {
        Customer customer = table.getSelectionModel().getSelectedItem();
        if (customer == null) {
            UiUtil.showError(table.getScene().getWindow(), "Select a customer first.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(table.getScene().getWindow());
        alert.setHeaderText(null);
        alert.setContentText("Delete " + customer.getDisplayName() + "?");
        Optional<ButtonType> answer = alert.showAndWait();
        if (answer.isPresent() && answer.get() == ButtonType.OK) {
            try {
                context.customers().deleteCustomer(customer.getCustomerId());
                refresh();
            } catch (BankException | DataAccessException e) {
                UiUtil.showError(table.getScene().getWindow(), e.getMessage());
            }
        }
    }

    private String prompt(String label, String value) {
        TextInputDialog dialog = new TextInputDialog(value);
        Window owner = table.getScene().getWindow();
        dialog.initOwner(owner);
        dialog.setTitle("Update customer");
        dialog.setHeaderText(null);
        dialog.setContentText(label + ":");
        Optional<String> answer = dialog.showAndWait();
        return answer.orElse(null);
    }

    @Override
    public void refresh() {
        if (context == null) {
            return;
        }
        try {
            List<Customer> customers = context.customers().findCustomers(searchField.getText());
            table.setItems(FXCollections.observableArrayList(customers));
        } catch (BankException | DataAccessException e) {
            UiUtil.showError(table.getScene().getWindow(), e.getMessage());
        }
    }
}
