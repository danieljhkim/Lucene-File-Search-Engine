package com.lucene.ui.views;

import com.lucene.model.WatchResult;
import com.lucene.util.logging.CustomLogger;
import com.lucene.watcher.ChangeWatcher;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class WatchView extends BaseView {

    private static final Logger logger = CustomLogger.getLogger(WatchView.class.getName());

    private static final String VIEW_NAME = "WatchView";
    private final DirectoryChooser directoryChooser = new DirectoryChooser();
    private final Stage primaryStage;
    private final ListView<WatchResult> results = new ListView<>();

    public String selectedDirectory;
    private Button startButton;
    private Button stopButton;
    private ChangeWatcher activeWatcher;
    private Thread watcherThread;


    public WatchView(Stage primaryStage, LogView logView) {
        super(logView);
        this.primaryStage = primaryStage;
        this.setStyle("-fx-padding: 10; -fx-spacing: 10;");
        init();
    }

    public void init() {
        startButton = new Button("Start Watching");
        stopButton = new Button("Stop Watching");
        stopButton.setDisable(true);
        Button clearButton = new Button("Clear");
        Button selectDirButton = new Button("Select Directory");

        selectDirButton.setOnAction(event -> selectDirectoryToWatchChange());
        startButton.setOnAction(event -> {
            if (selectedDirectory != null) {
                initiateChangeWatcher(selectedDirectory);
            } else {
                logView.appendLog(logAppender.warning("No directory selected to watch."));
                showAlert(Alert.AlertType.WARNING, "Please select a directory to watch first.");
            }
        });

        stopButton.setOnAction(event -> stopWatcher());
        clearButton.setOnAction(e -> clearOutput());

        HBox buttonBox = new HBox(10, selectDirButton, startButton, stopButton);
        this.getChildren().addAll(new Label("Monitor Directory for Changes"), buttonBox, results, clearButton);
    }

    private void initiateChangeWatcher(String directoryPath) {
        if (!Files.isDirectory(Paths.get(directoryPath))) {
            String errorMsg = "The selected path is not a valid directory: " + directoryPath;
            logView.appendLog(logAppender.error(errorMsg));
            showAlert(Alert.AlertType.ERROR, errorMsg);
            return;
        }

        stopWatcher();
        logView.appendLog(logAppender.info("Watching directory: " + directoryPath));
        try {
            activeWatcher = new ChangeWatcher(directoryPath, this::displayChangeResults);
            watcherThread = new Thread(activeWatcher, "DirectoryWatcherThread");
            watcherThread.setDaemon(true);
            watcherThread.start();
            startButton.setDisable(true);
            stopButton.setDisable(false);
            logView.appendLog(logAppender.info("Watcher started"));
        } catch (Exception ex) {
            logView.appendLog(logAppender.error("Failed to start watcher: " + ex.getMessage()));
            showAlert(Alert.AlertType.ERROR, "Failed to start directory watcher: " + ex.getMessage());
        }
    }

    private void stopWatcher() {
        if (activeWatcher != null && activeWatcher.isRunning()) {
            activeWatcher.stop();
            logView.appendLog(logAppender.info("Watcher stopped"));
        }

        if (watcherThread != null && watcherThread.isAlive()) {
            try {
                watcherThread.interrupt();
                watcherThread.join(1000);  // Wait for thread to terminate, timeout after 1 second
            } catch (InterruptedException e) {
                logger.warning("Interrupted while stopping watcher thread");
            }
        }
        activeWatcher = null;
        watcherThread = null;
        startButton.setDisable(false);
        stopButton.setDisable(true);
    }

    private void selectDirectoryToWatchChange() {
        File selectedDir = directoryChooser.showDialog(primaryStage);
        if (selectedDir == null) {
            logView.appendLog(logAppender.warning("No directory selected."));
            return;
        }
        this.selectedDirectory = selectedDir.getAbsolutePath();
        logView.appendLog(logAppender.info("Selected directory: " + this.selectedDirectory));
    }

    public void clearOutput() {
        results.getItems().clear();
    }

    public void displayChangeResults(WatchResult result) {
        logger.info("Change event: " + result.printChangeEvent());
        results.getItems().add(0, result); //TODO: use deque for optimization
    }
}
