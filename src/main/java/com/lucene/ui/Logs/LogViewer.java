package com.lucene.ui.Logs;

import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class LogViewer extends VBox {

    private final TextArea logTextArea;

    public LogViewer() {
        logTextArea = new TextArea();
        logTextArea.setEditable(false);
        logTextArea.setWrapText(true);
        Button refreshBtn = new Button("Clear Logs");
        refreshBtn.setOnAction(e -> clearLogs());

        BorderPane pane = new BorderPane();
        pane.setCenter(logTextArea);
        pane.setBottom(refreshBtn);

        this.getChildren().add(pane);
    }

    public void clearLogs() {
        logTextArea.clear();
    }

    public void appendLog(String log) {
        logTextArea.appendText(log + "\n");
    }
}
