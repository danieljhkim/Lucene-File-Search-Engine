package com.lucene.ui.views;

import com.lucene.indexer.Indexer;
import com.lucene.searcher.Searcher;
import com.lucene.util.Constants;
import com.lucene.util.FileUtil;
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
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.ByteBuffersDirectory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class SearchView extends BaseView {

    private static final String VIEW_NAME = "SearchView";
    private static final String PREF_INDEXED_DIRS = "indexedDirectories";

    private final DirectoryChooser directoryChooser = new DirectoryChooser();
    private final ListView<String> resultsView = new ListView<>();
    private final Button searchBtn = new Button("Search");
    private final Button indexBtn = new Button("Choose Directory to Index");
    private final Button clearBtn = new Button("Clear");
    private final Button resetPrefsBtn = new Button("Clear Indexes");
    private final ComboBox<String> fileTypeComboBox = new ComboBox<>();
    private final ComboBox<String> indexedDirectoriesComboBox = new ComboBox<>();
    private final Stage primaryStage;
    private final ByteBuffersDirectory inMemoryIndex;
    private final Preferences prefs = Preferences.userNodeForPackage(SearchView.class);

    private String currentDirectory;
    private boolean usingInMemoryIndex = true;
    private Indexer indexer;
    private Searcher searcher;

    public SearchView(Stage primaryStage, LogView logView, ByteBuffersDirectory index) {
        super(logView);
        this.primaryStage = primaryStage;
        this.inMemoryIndex = index;
    }

    public void init() {
        TextField queryField = new TextField();
        directoryChooser.setTitle("Select Directory to Index");
        fileTypeComboBox.setItems(FXCollections.observableArrayList(Constants.SUPPORTED_FILE_TYPES));
        fileTypeComboBox.getSelectionModel().selectFirst();

        indexedDirectoriesComboBox.setPromptText("Indexed Directories");
        loadIndexedDirectories();
        indexedDirectoriesComboBox.setOnAction(event -> switchToSelectedDirectory());
        searchBtn.setOnAction(event -> search(queryField, resultsView));
        indexBtn.setOnAction(event -> selectDirectoryAndIndex());
        clearBtn.setOnAction(event -> clearResults());
        resetPrefsBtn.setOnAction(event -> resetPreferences());

        HBox searchBox = new HBox(10, queryField, searchBtn);
        HBox indexBox = new HBox(10, indexBtn, indexedDirectoriesComboBox);
        HBox clearButtonBox = new HBox(10, clearBtn, resetPrefsBtn);

        VBox searchVBox = new VBox(10,
                new Label("Enter search query:"),
                searchBox,
                new Label("Index new directory or choose already indexed:"),
                indexBox,
                resultsView,
                clearButtonBox
        );
        searchVBox.setPadding(new Insets(20));
        this.getChildren().add(searchVBox);
        initializeInMemoryComponents();
    }

    /**
     * Initializes components for in-memory indexing and searching.
     */
    private void initializeInMemoryComponents() {
        try {
            logView.appendLog(logAppender.debug("Initializing in-memory components..."));
            indexer = new Indexer(inMemoryIndex);
            searcher = new Searcher(inMemoryIndex);
            usingInMemoryIndex = true;
            currentDirectory = null;
            searchBtn.setDisable(false);
        } catch (Exception e) {
            String errorMessage = "Failed to initialize in-memory components: " + e.getMessage();
            logView.appendLog(logAppender.error(errorMessage));
            searchBtn.setDisable(true);
        }
    }

    /**
     * Loads indexed directories from preferences.
     */
    private void loadIndexedDirectories() {
        String savedDirs = prefs.get(PREF_INDEXED_DIRS, "");
        List<String> validDirs = new ArrayList<>();

        if (!savedDirs.isEmpty()) {
            String[] dirs = savedDirs.split(";");
            for (String dir : dirs) {
                if (FileUtil.isValidDirectory(dir) && Searcher.isValidIndexDirectory(dir)) {
                    validDirs.add(dir);
                } else {
                    logView.appendLog(logAppender.warning("Removing invalid or missing index directory: " + dir));
                }
            }
        }

        indexedDirectoriesComboBox.setItems(FXCollections.observableArrayList(validDirs));
    }

    /**
     * Saves indexed directories to preferences.
     */
    private void saveIndexedDirectories() {
        List<String> dirs = indexedDirectoriesComboBox.getItems();
        StringBuilder sb = new StringBuilder();
        for (String dir : dirs) {
            if (sb.length() > 0) {
                sb.append(";");
            }
            sb.append(dir);
        }
        prefs.put(PREF_INDEXED_DIRS, sb.toString());
    }


    private void switchToSelectedDirectory() {
        String selectedPath = indexedDirectoriesComboBox.getSelectionModel().getSelectedItem();
        if (selectedPath == null) {
            return;
        }
        if (!FileUtil.isValidDirectory(selectedPath)) {
            logView.appendLog(logAppender.error("Invalid directory: " + selectedPath));
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Invalid directory"));
            return;
        }
        try {
            if (searcher != null) {
                searcher.close();
            }
            if (indexer != null && !usingInMemoryIndex) {
                indexer.close();
            }
            if (!Searcher.isValidIndexDirectory(selectedPath)) {
                logView.appendLog(logAppender.error("Not a valid Lucene index: " + selectedPath));
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR,
                        "No index exists for the selected directory."));
                return;
            }
            searcher = new Searcher(selectedPath);
            currentDirectory = selectedPath;
            usingInMemoryIndex = false;
            logView.appendLog(logAppender.info("Switched searcher to directory: " + selectedPath));
            searchBtn.setDisable(false);
        } catch (Exception e) {
            logView.appendLog(logAppender.error("Failed to switch searcher: " + e.getMessage()));
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Failed to switch searcher: " + e.getMessage()));
            initializeInMemoryComponents();
        }
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
        if (query.isEmpty()) {
            logView.appendLog(logAppender.warning("Empty search query"));
            return;
        }
        if (searcher == null) {
            logView.appendLog(logAppender.error("Searcher not initialized"));
            showAlert(Alert.AlertType.ERROR, "Search engine not initialized");
            return;
        }
        try {
            ScoreDoc[] hits = searcher.getSearch(query, 10000);
            Platform.runLater(() -> {
                for (ScoreDoc hit : hits) {
                    try {
                        Document doc = searcher.getDocument(hit);
                        resultsView.getItems().add("* Score: " + MathUtil.foundUpToThousandth(hit.score) +
                                "     [" + doc.get("filename") + "]     " + doc.get("path"));
                    } catch (Exception e) {
                        logView.appendLog(logAppender.error("Error retrieving document: " + e.getMessage()));
                    }
                }
                logView.appendLog(logAppender.info("Search complete. Number of hits: " + hits.length));
            });
        } catch (Exception e) {
            String errorMessage = "Search failed: " + e.getMessage();
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, errorMessage));
            logView.appendLog(logAppender.error(errorMessage));
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
        String directoryPath = selectedDirectory.getAbsolutePath();
        String fileType = fileTypeComboBox.getSelectionModel().getSelectedItem();
        try {
            Path dataDir = Paths.get(System.getProperty("user.dir"), "data");
            Files.createDirectories(dataDir);
            Indexer directoryIndexer = null;
            try {
                directoryIndexer = new Indexer(directoryPath);
                directoryIndexer.indexDirectory(directoryPath, fileType);
                // Add to indexed directories if not already there
                if (!indexedDirectoriesComboBox.getItems().contains(directoryPath)) {
                    indexedDirectoriesComboBox.getItems().add(directoryPath);
                    saveIndexedDirectories();
                }
                indexedDirectoriesComboBox.getSelectionModel().select(directoryPath);
                switchToSelectedDirectory();
                
                Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Indexing complete!"));
                logView.appendLog(logAppender.info("Indexing complete for directory: " + directoryPath));
            } finally {
                if (directoryIndexer != null) {
                    directoryIndexer.close();
                }
            }
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
     * Resets all preferences and clears saved directories
     */
    private void resetPreferences() {
        try {
            prefs.remove(PREF_INDEXED_DIRS);
            indexedDirectoriesComboBox.getItems().clear();
            logView.appendLog(logAppender.info("All preferences have been reset"));
            initializeInMemoryComponents();
        } catch (Exception e) {
            logView.appendLog(logAppender.error("Failed to reset preferences: " + e.getMessage()));
            showAlert(Alert.AlertType.ERROR, "Failed to reset preferences");
        }
    }

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

