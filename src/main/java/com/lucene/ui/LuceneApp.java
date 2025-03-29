package com.lucene.ui;

import com.lucene.indexer.Indexer;
import com.lucene.searcher.Searcher;
import com.lucene.ui.Logs.LogViewer;
import com.lucene.util.logging.LogAppender;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.ByteBuffersDirectory;

import java.io.File;

public class LuceneApp extends Application {

    private static final String APP_NAME = "LucidSearch";

    private final LogAppender logAppender = new LogAppender(APP_NAME);
    private final ByteBuffersDirectory index = new ByteBuffersDirectory();
    private final LogViewer logViewer = new LogViewer();

    private Indexer indexer;
    private Searcher searcher;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("LucidSearch");

        // --- Search UI components ---
        TextField queryField = new TextField();
        Button searchBtn = new Button("Search");
        Button indexBtn = new Button("Choose Directory & Index");
        ListView<String> resultsView = new ListView<>();
        logViewer.setPadding(new Insets(20));

        try {
            indexer = new Indexer(index);
        } catch (Exception e) {
            String errorMessage = "Failed to initialize Indexer: " + e.getMessage();
            showAlert(Alert.AlertType.ERROR, errorMessage);
            logViewer.appendLog(logAppender.error(errorMessage));
            return;
        }

        initializeSearcher(searchBtn);
        searchBtn.setOnAction(event -> search(queryField, resultsView));
        indexBtn.setOnAction(event -> selectDirectoryAndIndex(primaryStage, searchBtn));

        VBox searchVBox = new VBox(10, new Label("Enter search query:"), queryField, searchBtn, indexBtn, resultsView);
        searchVBox.setPadding(new Insets(20));

        // --- TabPane setup ---
        TabPane tabPane = new TabPane();
        Tab searchTab = new Tab("Search", searchVBox);
        searchTab.setClosable(false);
        Tab logsTab = new Tab("Logs", logViewer);
        logsTab.setClosable(false);
        tabPane.getTabs().addAll(searchTab, logsTab);

        primaryStage.setScene(new Scene(tabPane, 600, 400));
        primaryStage.show();
    }

    /**
     * Initializes the Searcher.
     *
     * @param searchBtn Button to disable on failure.
     * @return true if successfully initialized, false if not.
     */
    private boolean initializeSearcher(Button searchBtn) {
        try {
            searcher = new Searcher(index);
            searchBtn.setDisable(false);
            return true;
        } catch (IndexNotFoundException e) {
            String errorMessage = "Index not found. Please index a directory first.";
            logViewer.appendLog(logAppender.error(errorMessage));
        } catch (Exception e) {
            String errorMessage = "Failed to initialize Searcher: " + e.getMessage();
            showAlert(Alert.AlertType.ERROR, errorMessage);
            logViewer.appendLog(logAppender.error(errorMessage));
        }
        searchBtn.setDisable(true);
        return false;
    }

    /**
     * Performs a search with the given query and updates the results view.
     */
    private void search(TextField queryField, ListView<String> resultsView) {
        String query = queryField.getText().trim();
        if (!query.isEmpty() && searcher != null) {
            resultsView.getItems().clear();
            try {
                ScoreDoc[] hits = searcher.getSearch(query, 10);
                for (ScoreDoc hit : hits) {
                    Document doc = searcher.getDocument(hit);
                    resultsView.getItems().add(doc.get("filename") + ": " + preview(doc.get("content")));
                }
            } catch (Exception e) {
                String errorMessage = "Search failed: " + e.getMessage();
                showAlert(Alert.AlertType.ERROR, errorMessage);
                logViewer.appendLog(logAppender.error(errorMessage));
            }
        }
    }

    /**
     * Opens a directory chooser to select a directory and indexes it.
     */
    private void selectDirectoryAndIndex(Stage stage, Button searchBtn) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose Documents Directory");
        File selectedDirectory = directoryChooser.showDialog(stage);

        if (selectedDirectory != null) {
            try {
                indexer.indexDirectory(selectedDirectory.getAbsolutePath());
                Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Indexing complete!"));
                logViewer.appendLog(logAppender.info("Indexing complete for directory: " + selectedDirectory.getAbsolutePath()));
                initializeSearcher(searchBtn);
            } catch (Exception e) {
                String errorMessage = "Failed to index directory: " + e.getMessage();
                logViewer.appendLog(logAppender.error(errorMessage));
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, errorMessage));
            }
        }
    }

    /**
     * Provides a preview of the given content.
     */
    private String preview(String content) {
        final int previewLength = 150;
        return content.length() > previewLength ? content.substring(0, previewLength) + "..." : content;
    }

    /**
     * Displays an alert dialog with the specified type and message.
     */
    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message);
        alert.showAndWait();
    }

    /**
     * Clean up resources on application exit.
     */
    @Override
    public void stop() throws Exception {
        if (searcher != null) {
            searcher.close();
        }
        if (indexer != null) {
            indexer.close();
        }
        super.stop();
    }
}
