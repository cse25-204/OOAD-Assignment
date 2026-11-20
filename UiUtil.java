package view;

import javafx.scene.control.Alert;
import javafx.stage.Window;

final class UiUtil {

    private UiUtil() {
    }

    static void showError(Window owner, String message) {
        show(owner, Alert.AlertType.ERROR, "Error", message);
    }

    static void showInfo(Window owner, String message) {
        show(owner, Alert.AlertType.INFORMATION, "SM BANKING", message);
    }

    private static void show(Window owner, Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
