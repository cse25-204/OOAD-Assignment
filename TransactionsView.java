package view;

import controller.AppContext;
import controller.TransactionController;
import dao.DataAccessException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import model.Account;
import model.BankException;
import model.Transaction;
import util.MoneyUtil;

public class TransactionsView implements Refreshable {

    @FXML private ChoiceBox<Account> accountBox;
    @FXML private Label balanceLabel;
    @FXML private TextField amountField;
    @FXML private Button withdrawButton;
    @FXML private Button interestButton;
    @FXML private TableView<Transaction> historyTable;
    @FXML private TableColumn<Transaction, String> dateCol;
    @FXML private TableColumn<Transaction, String> typeCol;
    @FXML private TableColumn<Transaction, String> amountCol;
    @FXML private TableColumn<Transaction, String> balanceAfterCol;
    @FXML private TableColumn<Transaction, String> descriptionCol;

    private AppContext context;

    @FXML
    private void initialize() {
        dateCol.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getTimestamp().toString().replace('T', ' ')));
        typeCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getType().toString()));
        amountCol.setCellValueFactory(cell ->
                new SimpleStringProperty(MoneyUtil.format(cell.getValue().getAmount())));
        balanceAfterCol.setCellValueFactory(cell ->
                new SimpleStringProperty(MoneyUtil.format(cell.getValue().getBalanceAfter())));
        descriptionCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getDescription()));
        accountBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showAccount(newValue));
    }

    void setContext(AppContext context) {
        this.context = context;
        boolean teller = context.session().getCurrentUser().isTeller();
        interestButton.setVisible(teller);
        interestButton.setManaged(teller);
        refresh();
    }

    @FXML
    private void onDeposit() {
        runTransaction(true);
    }

    @FXML
    private void onWithdraw() {
        runTransaction(false);
    }

    private void runTransaction(boolean deposit) {
        Account account = accountBox.getValue();
        if (account == null) {
            UiUtil.showError(historyTable.getScene().getWindow(), "Choose an account first.");
            return;
        }
        try {
            if (deposit) {
                context.transactions().deposit(account.getAccountNumber(), amountField.getText());
            } else {
                context.transactions().withdraw(account.getAccountNumber(), amountField.getText());
            }
            amountField.clear();
            refresh();
        } catch (BankException | DataAccessException e) {
            UiUtil.showError(historyTable.getScene().getWindow(), e.getMessage());
        }
    }

    @FXML
    private void onRunInterest() {
        try {
            TransactionController.InterestRunSummary summary =
                    context.transactions().payMonthlyInterest();
            UiUtil.showInfo(historyTable.getScene().getWindow(), summary.toString());
            refresh();
        } catch (BankException | DataAccessException e) {
            UiUtil.showError(historyTable.getScene().getWindow(), e.getMessage());
        }
    }

    private void showAccount(Account account) {
        if (account == null) {
            balanceLabel.setText("");
            withdrawButton.setDisable(true);
            historyTable.getItems().clear();
            return;
        }
        balanceLabel.setText(MoneyUtil.format(account.getBalance()));
        withdrawButton.setDisable(!account.allowsWithdrawals());
        try {
            List<Transaction> history =
                    context.transactions().getHistory(account.getAccountNumber());
            List<Transaction> newestFirst = new ArrayList<>(history);
            Collections.reverse(newestFirst);
            historyTable.setItems(FXCollections.observableArrayList(newestFirst));
        } catch (BankException | DataAccessException e) {
            UiUtil.showError(historyTable.getScene().getWindow(), e.getMessage());
        }
    }

    @Override
    public void refresh() {
        if (context == null) {
            return;
        }
        try {
            String selectedNumber = accountBox.getValue() == null
                    ? null : accountBox.getValue().getAccountNumber();
            List<Account> accounts = context.accounts().getVisibleAccounts();
            accountBox.setItems(FXCollections.observableArrayList(accounts));
            Account selected = null;
            for (Account account : accounts) {
                if (account.getAccountNumber().equals(selectedNumber)) {
                    selected = account;
                    break;
                }
            }
            if (selected == null && !accounts.isEmpty()) {
                selected = accounts.get(0);
            }
            accountBox.setValue(selected);
            showAccount(selected);
        } catch (BankException | DataAccessException e) {
            UiUtil.showError(historyTable.getScene().getWindow(), e.getMessage());
        }
    }
}
