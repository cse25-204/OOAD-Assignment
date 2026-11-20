package view;

import controller.AppContext;
import dao.DataAccessException;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import model.BankException;
import model.CustomerType;

public class RegisterView {

    @FXML private ChoiceBox<CustomerType> typeBox;
    @FXML private Label firstLabel;
    @FXML private Label secondLabel;
    @FXML private Label idLabel;
    @FXML private TextField firstField;
    @FXML private TextField secondField;
    @FXML private TextField idField;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;

    private AppContext context;
    private Runnable onRegistered;

    static void show(AppContext context, Window owner, Runnable onRegistered) {
        ViewLoader.Loaded<RegisterView> loaded = ViewLoader.load("Register.fxml");
        loaded.controller.context = context;
        loaded.controller.onRegistered = onRegistered;

        Stage stage = new Stage();
        stage.setTitle("Register customer");
        if (owner != null) {
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
        } else {
            stage.initModality(Modality.APPLICATION_MODAL);
        }
        Scene scene = new Scene(loaded.root);
        ViewLoader.applyStyle(scene);
        stage.setScene(scene);
        stage.showAndWait();
    }

    @FXML
    private void initialize() {
        typeBox.getItems().setAll(CustomerType.values());
        typeBox.setValue(CustomerType.INDIVIDUAL);
        typeBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> updateLabels(newValue));
        updateLabels(CustomerType.INDIVIDUAL);
    }

    private void updateLabels(CustomerType type) {
        if (type == CustomerType.COMPANY) {
            firstLabel.setText("Company name");
            secondLabel.setText("Registration number");
            secondField.setPromptText("Registration number");
            idLabel.setVisible(false);
            idLabel.setManaged(false);
            idField.setVisible(false);
            idField.setManaged(false);
        } else {
            firstLabel.setText("First name");
            secondLabel.setText("Surname");
            secondField.setPromptText("Surname");
            idLabel.setText("ID number (Omang)");
            idLabel.setVisible(true);
            idLabel.setManaged(true);
            idField.setVisible(true);
            idField.setManaged(true);
        }
    }

    @FXML
    private void onRegister() {
        try {
            CustomerType type = typeBox.getValue();
            if (type == CustomerType.COMPANY) {
                context.customers().registerCompany(
                        firstField.getText(), secondField.getText(), addressField.getText(),
                        phoneField.getText(), emailField.getText(), usernameField.getText(),
                        passwordField.getText(), confirmField.getText());
            } else {
                context.customers().registerIndividual(
                        firstField.getText(), secondField.getText(), idField.getText(),
                        addressField.getText(), phoneField.getText(), emailField.getText(),
                        usernameField.getText(), passwordField.getText(), confirmField.getText());
            }
            Window window = ((Node) typeBox).getScene().getWindow();
            UiUtil.showInfo(window, "Customer registered successfully.");
            if (onRegistered != null) {
                onRegistered.run();
            }
            window.hide();
        } catch (BankException | DataAccessException e) {
            UiUtil.showError(((Node) typeBox).getScene().getWindow(), e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        ((Node) typeBox).getScene().getWindow().hide();
    }
}
