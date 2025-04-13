package com.lucene.ui.views;

import com.lucene.util.logging.LogAppender;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

abstract class BaseView extends VBox {

    private static final String VIEW_NAME = "BaseView";
    public Stage primaryStage;
    public LogView logView;
    public LogAppender logAppender = new LogAppender(VIEW_NAME);

    public BaseView() {
        super();
    }

    public BaseView(LogView logView) {
        this.logView = logView;
    }

    public BaseView(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Provides a preview of the given content.
     */
    public String preview(String content) {
        final int previewLength = 100;
        return content.length() > previewLength ? content.substring(0, previewLength) + "..." : content;
    }

    /**
     * Displays an alert dialog with the specified type and message.
     */
    public void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message);
        alert.showAndWait();
    }
}
