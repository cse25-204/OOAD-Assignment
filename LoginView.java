package view;

import controller.AppContext;
import dao.DataAccessException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.BankException;

public class LoginView {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private AppContext context;
    private Stage stage;

    public static void show(AppContext context, Stage stage) {
        ViewLoader.Loaded<LoginView> loaded = ViewLoader.load("Login.fxml");
        loaded.controller.context = context;
        loaded.controller.stage = stage;
        ViewLoader.show(stage, loaded.root, "SM BANKING");
    }

    @FXML
    private void onLogin() {
        try {
            context.auth().login(usernameField.getText(), passwordField.getText());
            DashboardView.show(context, stage);
        } catch (BankException | DataAccessException e) {
            messageLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void onRegister() {
        RegisterView.show(context, stage, null);
    }
}
