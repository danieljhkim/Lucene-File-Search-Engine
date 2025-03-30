package com.lucene.ui.views;

import com.lucene.indexer.Indexer;
import com.lucene.searcher.Searcher;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.ByteBuffersDirectory;

import java.io.File;

public class SearchView extends BaseView {

    private static final String VIEW_NAME = "SearchView";
    private final ByteBuffersDirectory index = new ByteBuffersDirectory();
    private final DirectoryChooser directoryChooser = new DirectoryChooser();
    private final ListView<String> resultsView = new ListView<>();
    private final Button searchBtn = new Button("Search");
    private final Button indexBtn = new Button("Choose Directory & Index");
    private final Stage primaryStage;
    private Indexer indexer;
    private Searcher searcher;

    public SearchView(Stage primaryStage, LogView logView) {
        super(logView);
        this.primaryStage = primaryStage;
    }

    public void init() {
        TextField queryField = new TextField();
        directoryChooser.setTitle("Select Directory to Index");

        initializeIndexer();
        initializeSearcher();

        searchBtn.setOnAction(event -> search(queryField, resultsView));
        indexBtn.setOnAction(event -> selectDirectoryAndIndex());

        VBox searchVBox = new VBox(10, new Label("Enter search query:"), queryField, searchBtn, indexBtn, resultsView);
        searchVBox.setPadding(new Insets(20));
        
        this.getChildren().add(searchVBox);
    }

    /**
     * Initializes the Searcher.
     *
     * @return true if successfully initialized, false if not.
     */
    private boolean initializeSearcher() {
        try {
            searcher = new Searcher(index);
            searchBtn.setDisable(false);
            return true;
        } catch (IndexNotFoundException e) {
            String errorMessage = "Index not found. Please index a directory first.";
            logView.appendLog(logAppender.error(errorMessage));
        } catch (Exception e) {
            String errorMessage = "Failed to initialize Searcher: " + e.getMessage();
            showAlert(Alert.AlertType.ERROR, errorMessage);
            logView.appendLog(logAppender.error(errorMessage));
        }
        searchBtn.setDisable(true);
        return false;
    }

    private boolean initializeIndexer() {
        try {
            indexer = new Indexer(index);
            return true;
        } catch (Exception e) {
            String errorMessage = "Failed to initialize Indexer: " + e.getMessage();
            showAlert(Alert.AlertType.ERROR, errorMessage);
            logView.appendLog(logAppender.error(errorMessage));
        }
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
                logView.appendLog(logAppender.error(errorMessage));
            }
        }
    }

    /**
     * Opens a directory chooser to select a directory and indexes it.
     */
    private void selectDirectoryAndIndex() {
        File selectedDirectory = directoryChooser.showDialog(primaryStage);
        if (selectedDirectory != null) {
            try {
                indexer.indexDirectory(selectedDirectory.getAbsolutePath());
                Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Indexing complete!"));
                logView.appendLog(logAppender.info("Indexing complete for directory: " + selectedDirectory.getAbsolutePath()));
                initializeSearcher();
            } catch (Exception e) {
                String errorMessage = "Failed to index directory: " + e.getMessage();
                logView.appendLog(logAppender.error(errorMessage));
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, errorMessage));
            }
        }
    }

    /**
     * Closes the indexer and searcher.
     */
    public void close() {
        try {
            if (indexer != null) {
                indexer.close();
            }
            if (searcher != null) {
                searcher.close();
            }
        } catch (Exception e) {
            String errorMessage = "Failed to close resources: " + e.getMessage();
            logView.appendLog(logAppender.error(errorMessage));
        }
    }
}
