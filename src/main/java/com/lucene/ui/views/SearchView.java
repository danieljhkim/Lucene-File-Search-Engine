package com.lucene.ui.views;

import com.lucene.indexer.Indexer;
import com.lucene.searcher.Searcher;
import com.lucene.util.Constants;
import com.lucene.util.MathUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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
    private final DirectoryChooser directoryChooser = new DirectoryChooser();
    private final ListView<String> resultsView = new ListView<>();
    private final Button searchBtn = new Button("Search");
    private final Button indexBtn = new Button("Choose Directory & Index");
    private final Button clearBtn = new Button("Clear");
    private final ComboBox<String> fileTypeComboBox = new ComboBox<>();
    private final Stage primaryStage;
    public String selectedDirectory;
    private Indexer indexer;
    private Searcher searcher;
    private final ByteBuffersDirectory index;

    public SearchView(Stage primaryStage, LogView logView, ByteBuffersDirectory index) {
        super(logView);
        this.primaryStage = primaryStage;
        this.index = index;
    }

    public void init() {

        TextField queryField = new TextField();
        directoryChooser.setTitle("Select Directory to Index");

        fileTypeComboBox.setItems(FXCollections.observableArrayList(Constants.SUPPORTED_FILE_TYPES));
        fileTypeComboBox.getSelectionModel().selectFirst();
        searchBtn.setOnAction(event -> search(queryField, resultsView));
        indexBtn.setOnAction(event -> selectDirectoryAndIndex());
        clearBtn.setOnAction(event -> clearResults());
        HBox buttonBox = new HBox(10, fileTypeComboBox, indexBtn);

        VBox searchVBox = new VBox(10,
                new Label("Enter search query:"),
                queryField,
                searchBtn,
                new Label("Select file type:"),
                buttonBox,
                resultsView,
                clearBtn
        );
        searchVBox.setPadding(new Insets(20));

        this.getChildren().add(searchVBox);
    }

    /**
     * Initializes the Searcher.
     *
     * @return Searcher if successfully initialized, null if not.
     */
    public Searcher initializeSearcher() {
        try {
            logView.appendLog(logAppender.debug("Initializing Searcher..."));
            searcher = new Searcher(index);
            searchBtn.setDisable(false);
            return searcher;
        } catch (IndexNotFoundException e) {
            String errorMessage = "Index not found. Please index a directory first.";
            logView.appendLog(logAppender.error(errorMessage));
        } catch (Exception e) {
            String errorMessage = "Failed to initialize Searcher: " + e.getMessage();
            showAlert(Alert.AlertType.ERROR, errorMessage);
            logView.appendLog(logAppender.error(errorMessage));
        }
        searchBtn.setDisable(true);
        return null;
    }

    public Indexer initializeIndexer() {
        try {
            logView.appendLog(logAppender.debug("Initializing Indexer..."));
            indexer = new Indexer(index);
            return indexer;
        } catch (Exception e) {
            String errorMessage = "Failed to initialize Indexer: " + e.getMessage();
            showAlert(Alert.AlertType.ERROR, errorMessage);
            logView.appendLog(logAppender.error(errorMessage));
        }
        return null;
    }

    /**
     * Performs a search with the given query and updates the results view.
     */
    private void search(TextField queryField, ListView<String> resultsView) {
        if (queryField == null || resultsView == null) {
            return;
        }
        clearResults();
        String query = queryField.getText().trim();
        if (!query.isEmpty() && searcher != null) {
            resultsView.getItems().clear();
            try {
                ScoreDoc[] hits = searcher.getSearch(query, 10000);
                for (ScoreDoc hit : hits) {
                    Document doc = searcher.getDocument(hit);
                    resultsView.getItems().add("* Score: " + MathUtil.foundUpToThousandth(hit.score) + "     [" + doc.get("filename") + "]     " + doc.get("path"));
                }
                logView.appendLog(logAppender.info("Search complete. Number of hits: " + hits.length));
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
        if (selectedDirectory == null) {
            logView.appendLog(logAppender.warning("No directory selected."));
            return;
        }
        this.selectedDirectory = selectedDirectory.getAbsolutePath();
        String fileType = fileTypeComboBox.getSelectionModel().getSelectedItem();
        try {
            indexer.indexDirectory(selectedDirectory.getAbsolutePath(), fileType);
            Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Indexing complete!"));
            logView.appendLog(logAppender.info("Indexing complete for directory: " + selectedDirectory.getAbsolutePath()));
            initializeSearcher();
        } catch (Exception e) {
            String errorMessage = "Failed to index directory: " + e.getMessage();
            logView.appendLog(logAppender.error(errorMessage));
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, errorMessage));
        }

    }

    public void clearResults() {
        resultsView.getItems().clear();
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
