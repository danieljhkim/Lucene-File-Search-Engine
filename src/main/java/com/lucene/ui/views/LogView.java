package com.lucene.ui.views;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

import java.util.ArrayList;
import java.util.List;

public class LogView extends BaseView {

    private static final String VIEW_NAME = "LogView";
    private static final String CLEAR_LOGS = "Clear Logs";
    public List<String> logs = new ArrayList<>();
    private TextArea logTextArea;

    public LogView() {
        BorderPane pane = createLogView();
        this.getChildren().add(pane);
    }

    public BorderPane createLogView() {
        logTextArea = new TextArea();
        logTextArea.setEditable(false);
        logTextArea.setWrapText(true);
        Button refreshBtn = new Button(CLEAR_LOGS);
        refreshBtn.setOnAction(e -> clearLogs());

        BorderPane pane = new BorderPane();
        pane.setCenter(logTextArea);
        pane.setBottom(refreshBtn);
        pane.setPadding(new Insets(10));

        return pane;
    }

    public void clearLogs() {
        logs.clear();
        logTextArea.clear();
    }

    public void appendLog(String log) {
        logs.add(log);
        logTextArea.appendText(log + "\n");
    }
}
