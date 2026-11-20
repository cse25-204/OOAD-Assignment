import controller.AppContext;
import controller.DefaultTellerSetup;
import dao.DatabaseManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import view.LoginView;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        try {
            AppContext ctx = AppContext.forJdbc(DatabaseManager.DEFAULT_URL);
            DefaultTellerSetup.ensureDefaultTeller(ctx);
            LoginView.show(ctx, stage);
        } catch (RuntimeException e) {
            e.printStackTrace();
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR,
                    "SM BANKING could not start the database:\n" + e.getMessage(),
                    javafx.scene.control.ButtonType.OK);
            alert.showAndWait();
            Platform.exit();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
