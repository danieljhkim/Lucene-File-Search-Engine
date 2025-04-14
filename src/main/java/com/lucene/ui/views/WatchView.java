package com.lucene.ui.views;

import com.lucene.indexer.Indexer;
import com.lucene.model.WatchResult;
import com.lucene.searcher.Searcher;
import com.lucene.util.logging.CustomLogger;
import com.lucene.watcher.ChangeWatcher;
import com.lucene.watcher.FileWatcher;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class WatchView extends BaseView {

    private static final Logger logger = CustomLogger.getLogger(WatchView.class.getName());

    private static final String VIEW_NAME = "WatchView";
    private final DirectoryChooser directoryChooser = new DirectoryChooser();
    private final Stage primaryStage;
    private final TextField searchField;
    private final TextArea outputArea;
    private final Indexer indexer;
    private final Searcher searcher;
    private final Button startButton;
    private final Button clearButton;
    private final Button selectDirButton;
    private final List<WatchResult> results = new ArrayList<>();
    public String selectedDirectory;

    public WatchView(Stage primaryStage, LogView logView, Indexer indexer, Searcher searcher) {
        super(logView);
        this.indexer = indexer;
        this.searcher = searcher;
        this.primaryStage = primaryStage;
        this.setStyle("-fx-padding: 10; -fx-spacing: 10;");
        searchField = new TextField();
        searchField.setPromptText("Enter search keyword");
        startButton = new Button("Watch");
        clearButton = new Button("Clear");
        selectDirButton = new Button("Select Directory");
        selectDirButton.setOnAction(event -> selectDirectoryToWatchChange());
        clearButton.setOnAction(e -> clearOutput());
        outputArea = new TextArea();
        outputArea.setEditable(false);
        HBox buttonBox = new HBox(10, selectDirButton, startButton);
        this.getChildren().addAll(new Label("Watch Directory"), buttonBox, outputArea);
    }

    public void initWatch(String directoryPath) {
        this.startButton.setOnAction(e -> initiateChangeWatcher(directoryPath));
    }

    private void initiateKeyWordWatcher(String directoryPath, String keyword) {
        try {
            FileWatcher watcher = new FileWatcher(indexer, searcher, directoryPath, keyword, this::displaySearchResults);
            Thread watcherThread = new Thread(watcher);
            watcherThread.setDaemon(true);
            watcherThread.start();
        } catch (Exception ex) {
            // TODO: handle exception
        }
    }

    private void initiateChangeWatcher(String directoryPath) {
        try {
            ChangeWatcher watcher = new ChangeWatcher(indexer, searcher, directoryPath, this::displayChangeResults);
            Thread watcherThread = new Thread(watcher);
            watcherThread.setDaemon(true);
            watcherThread.start();
        } catch (Exception ex) {
            // TODO: handle exception
        }
    }

    private void selectDirectoryToWatchChange() {
        File selectedDirectory = directoryChooser.showDialog(primaryStage);
        if (selectedDirectory == null) {
            logView.appendLog(logAppender.warning("No directory selected."));
            return;
        }
        this.selectedDirectory = selectedDirectory.getAbsolutePath();
        try {
            initWatch(this.selectedDirectory);
        } catch (Exception e) {
            String errorMessage = "Failed to watch directory: " + e.getMessage();
            logView.appendLog(logAppender.error(errorMessage));
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, errorMessage));
        }

    }

    public void clearOutput() {
        outputArea.clear();
        results.clear();
    }

    public void displaySearchResults(List<WatchResult> results) {
        for (WatchResult result : results) {
            outputArea.appendText(result.toString());
            outputArea.appendText("--------------------------------------------------------\n");
        }

    }

    public void displayChangeResults(WatchResult result) {
        logger.info("change event: " + result.printChangeEvent());
        results.add(result);
        outputArea.appendText(result.printChangeEvent());
        outputArea.appendText("--------------------------------------------------------\n");
    }

}

