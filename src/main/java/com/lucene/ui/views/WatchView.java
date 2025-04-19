package com.lucene.ui.views;

import com.lucene.indexer.Indexer;
import com.lucene.model.WatchResult;
import com.lucene.searcher.Searcher;
import com.lucene.util.logging.CustomLogger;
import com.lucene.watcher.ChangeWatcher;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.logging.Logger;

public class WatchView extends BaseView {

    private static final Logger logger = CustomLogger.getLogger(WatchView.class.getName());

    private static final String VIEW_NAME = "WatchView";
    private final DirectoryChooser directoryChooser = new DirectoryChooser();
    private final Stage primaryStage;
    private final Indexer indexer;
    private final Searcher searcher;
    private final ListView<WatchResult> results = new ListView<>();
    public String selectedDirectory;
    private Button startButton;

    public WatchView(Stage primaryStage, LogView logView, Indexer indexer, Searcher searcher) {
        super(logView);
        this.indexer = indexer;
        this.searcher = searcher;
        this.primaryStage = primaryStage;
        this.setStyle("-fx-padding: 10; -fx-spacing: 10;");
        init();
    }

    public void init() {
        startButton = new Button("Initiate Watch");
        Button clearButton = new Button("Clear");
        Button selectDirButton = new Button("Select Directory");
        selectDirButton.setOnAction(event -> selectDirectoryToWatchChange());
        clearButton.setOnAction(e -> clearOutput());
        HBox buttonBox = new HBox(10, selectDirButton, startButton);
        this.getChildren().addAll(new Label("Monitor Directory for Changes"), buttonBox, results, clearButton);
    }

    public void initWatch(String directoryPath) {
        this.startButton.setOnAction(e -> initiateChangeWatcher(directoryPath));
    }


    private void initiateChangeWatcher(String directoryPath) {
        logView.appendLog(logAppender.info("Watching directory: " + directoryPath));
        try {
            ChangeWatcher watcher = new ChangeWatcher(indexer, searcher, directoryPath, this::displayChangeResults);
            Thread watcherThread = new Thread(watcher);
            watcherThread.setDaemon(true);
            watcherThread.start();
        } catch (Exception ex) {
            logView.appendLog(logAppender.error("Failed to start watcher: " + ex.getMessage()));
        }
    }

    private void selectDirectoryToWatchChange() {
        File selectedDirectory = directoryChooser.showDialog(primaryStage);
        if (selectedDirectory == null) {
            logView.appendLog(logAppender.warning("No directory selected."));
            return;
        }
        this.selectedDirectory = selectedDirectory.getAbsolutePath();
        logView.appendLog(logAppender.info("Selected directory: " + this.selectedDirectory));
        try {
            initWatch(this.selectedDirectory);
        } catch (Exception e) {
            String errorMessage = "Failed to watch directory: " + e.getMessage();
            logView.appendLog(logAppender.error(errorMessage));
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, errorMessage));
        }
    }

    public void clearOutput() {
        results.getItems().clear();
    }


    public void displayChangeResults(WatchResult result) {
        logger.info("change event: " + result.printChangeEvent());
        results.getItems().add(result);
    }

}

