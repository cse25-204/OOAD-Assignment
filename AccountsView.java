package view;

import controller.AppContext;
import dao.DataAccessException;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import model.Account;
import model.AccountType;
import model.Bank;
import model.BankException;
import model.Customer;
import model.InterestBearing;
import util.MoneyUtil;

public class AccountsView implements Refreshable {

    @FXML private Label customerLabel;
    @FXML private ChoiceBox<Customer> customerBox;
    @FXML private ChoiceBox<AccountType> typeBox;
    @FXML private ChoiceBox<String> branchBox;
    @FXML private TextField openingField;
    @FXML private TextField employerField;
    @FXML private TextField employerAddressField;
    @FXML private Label ruleLabel;
    @FXML private Label tableTitle;
    @FXML private TableView<Account> table;
    @FXML private TableColumn<Account, String> numberCol;
    @FXML private TableColumn<Account, String> typeCol;
    @FXML private TableColumn<Account, String> holderCol;
    @FXML private TableColumn<Account, String> branchCol;
    @FXML private TableColumn<Account, String> balanceCol;
    @FXML private TableColumn<Account, String> openedCol;
    @FXML private TableColumn<Account, String> interestCol;

    private AppContext context;

    @FXML
    private void initialize() {
        typeBox.getItems().setAll(AccountType.values());
        typeBox.getSelectionModel().selectFirst();
        branchBox.getItems().setAll(Bank.BRANCHES);
        branchBox.getSelectionModel().selectFirst();
        typeBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> updateTypeFields(newValue));
        setColumns();
        updateTypeFields(typeBox.getValue());
    }

    void setContext(AppContext context) {
        this.context = context;
        boolean teller = context.session().getCurrentUser().isTeller();
        customerLabel.setVisible(teller);
        customerLabel.setManaged(teller);
        customerBox.setVisible(teller);
        customerBox.setManaged(teller);
        refresh();
    }

    private void setColumns() {
        numberCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getAccountNumber()));
        typeCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getType().getLabel()));
        holderCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getOwner().getDisplayName()));
        branchCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getBranch()));
        balanceCol.setCellValueFactory(cell ->
                new SimpleStringProperty(MoneyUtil.format(cell.getValue().getBalance())));
        openedCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getDateOpened().toString()));
        interestCol.setCellValueFactory(cell -> {
            Account account = cell.getValue();
            String rate = account instanceof InterestBearing
                    ? ((InterestBearing) account).describeRate() : "None";
            return new SimpleStringProperty(rate);
        });
    }

    private void updateTypeFields(AccountType type) {
        boolean cheque = type == AccountType.CHEQUE;
        employerField.setDisable(!cheque);
        employerAddressField.setDisable(!cheque);
        ruleLabel.setText(type == AccountType.INVESTMENT
                ? "Investment accounts require an opening deposit of at least BWP 500."
                : cheque ? "Cheque accounts require the employer's name and address."
                : "Savings accounts do not allow withdrawals.");
    }

    @FXML
    private void onOpenAccount() {
        try {
            boolean teller = context.session().getCurrentUser().isTeller();
            Customer customer = customerBox.getValue();
            if (teller && customer == null) {
                UiUtil.showError(table.getScene().getWindow(), "Choose a customer.");
                return;
            }
            Account account = context.accounts().openAccount(
                    typeBox.getValue(),
                    teller ? customer.getCustomerId() : null,
                    branchBox.getValue(),
                    openingField.getText(),
                    employerField.getText(),
                    employerAddressField.getText());
            UiUtil.showInfo(table.getScene().getWindow(), "Opened " + account.getAccountNumber());
            openingField.clear();
            employerField.clear();
            employerAddressField.clear();
            refresh();
        } catch (BankException | DataAccessException e) {
            UiUtil.showError(table.getScene().getWindow(), e.getMessage());
        }
    }

    @Override
    public void refresh() {
        if (context == null) {
            return;
        }
        try {
            List<Account> accounts = context.accounts().getVisibleAccounts();
            table.setItems(FXCollections.observableArrayList(accounts));
            tableTitle.setText("Accounts (" + accounts.size() + ")");
            if (context.session().getCurrentUser().isTeller()) {
                customerBox.setItems(FXCollections.observableArrayList(
                        context.customers().findCustomers("")));
                if (customerBox.getValue() == null) {
                    customerBox.getSelectionModel().selectFirst();
                }
            }
        } catch (BankException | DataAccessException e) {
            UiUtil.showError(table.getScene().getWindow(), e.getMessage());
        }
    }
}
