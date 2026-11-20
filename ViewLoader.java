package view;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

final class ViewLoader {

    private static final double WINDOW_WIDTH = 1100;
    private static final double WINDOW_HEIGHT = 700;
    private static final Duration TRANSITION_DURATION = Duration.millis(160);

    static final class Loaded<T> {
        final Parent root;
        final T controller;

        Loaded(Parent root, T controller) {
            this.root = root;
            this.controller = controller;
        }
    }

    private ViewLoader() {
    }

    static <T> Loaded<T> load(String fileName) {
        try {
            FXMLLoader loader = new FXMLLoader(locate(fileName));
            Parent root = loader.load();
            T controller = loader.getController();
            return new Loaded<>(root, controller);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load " + fileName + ": " + e.getMessage(), e);
        }
    }

    static void applyStyle(Scene scene) {
        scene.getStylesheets().add(locate("style.css").toExternalForm());
    }

    static void show(Stage stage, Parent root, String title) {
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        double sceneWidth = Math.min(WINDOW_WIDTH, Math.max(320, screen.getWidth() - 48));
        double sceneHeight = Math.min(WINDOW_HEIGHT, Math.max(300, screen.getHeight() - 80));
        Scene nextScene = new Scene(root, sceneWidth, sceneHeight);
        applyStyle(nextScene);
        stage.setTitle(title);
        stage.setMinWidth(Math.min(900, sceneWidth));
        stage.setMinHeight(Math.min(600, sceneHeight));

        Scene currentScene = stage.getScene();
        if (currentScene == null) {
            root.setOpacity(0);
            stage.setScene(nextScene);
            stage.centerOnScreen();
            stage.show();
            fade(root, 0, 1).play();
            return;
        }

        FadeTransition fadeOut = fade(currentScene.getRoot(), 1, 0);
        fadeOut.setOnFinished(event -> {
            root.setOpacity(0);
            stage.setScene(nextScene);
            fade(root, 0, 1).play();
        });
        fadeOut.play();
    }

    private static FadeTransition fade(Parent root, double from, double to) {
        FadeTransition transition = new FadeTransition(TRANSITION_DURATION, root);
        transition.setFromValue(from);
        transition.setToValue(to);
        return transition;
    }

    private static URL locate(String fileName) {
        URL resource = ViewLoader.class.getResource("/fxml/" + fileName);
        if (resource != null) {
            return resource;
        }

        File file = new File("fxml", fileName);
        if (!file.isFile()) {
            throw new IllegalStateException("Cannot find fxml\\" + fileName
                    + ". Run the program from the project folder.");
        }
        try {
            return file.toURI().toURL();
        } catch (IOException e) {
            throw new IllegalStateException("Could not open " + fileName + ": " + e.getMessage(), e);
        }
    }
}
