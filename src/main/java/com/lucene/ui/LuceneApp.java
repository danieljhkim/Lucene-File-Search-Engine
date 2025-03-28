package com.lucene.ui;

import com.lucene.indexer.Indexer;
import com.lucene.searcher.Searcher;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.ByteBuffersDirectory;

import java.io.File;

public class LuceneApp extends Application {

    private static String directoryToIndex = "";
    private final ByteBuffersDirectory index = new ByteBuffersDirectory();
    private final Indexer indexer;
    private final Searcher searcher;

    public LuceneApp() {
        try {
            indexer = new Indexer(index);
            searcher = new Searcher(index);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Indexer and Searcher", e);
        }
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            directoryToIndex = args[0];
        }
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("LucidSearch");

        TextField queryField = new TextField();
        Button searchBtn = new Button("Search");
        Button indexBtn = new Button("Choose Directory & Index");
        ListView<String> resultsView = new ListView<>();

        // If a directory was specified at launch, index it immediately.
        if (directoryToIndex != null) {
            try {
                indexer.indexDirectory(directoryToIndex);
                showAlert(Alert.AlertType.INFORMATION, "Indexing complete for: " + directoryToIndex);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Indexing failed: " + e.getMessage());
            }
        }

        searchBtn.setOnAction(event -> {
            String query = queryField.getText();
            if (!query.isBlank()) {
                resultsView.getItems().clear();
                try {
                    ScoreDoc[] hits = searcher.getSearch(query, 10);
                    for (ScoreDoc hit : hits) {
                        Document doc = searcher.getDocument(hit);
                        resultsView.getItems().add(doc.get("filename")
                                + ": " + preview(doc.get("content")));
                    }
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Search failed: " + e.getMessage());
                }
            }
        });

        indexBtn.setOnAction(event -> selectDirectoryAndIndex(primaryStage));

        VBox vbox = new VBox(10, new Label("Enter search query:"), queryField, searchBtn, indexBtn, resultsView);
        vbox.setPadding(new Insets(20));

        primaryStage.setScene(new Scene(vbox, 600, 400));
        primaryStage.show();
    }

    private void selectDirectoryAndIndex(Stage stage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose Documents Directory");
        File selectedDirectory = directoryChooser.showDialog(stage);

        if (selectedDirectory != null) {
            try {
                indexer.indexDirectory(selectedDirectory.getAbsolutePath());
                Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Indexing complete!"));
            } catch (Exception e) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Indexing failed: " + e.getMessage()));
            }
        }
    }

    private String preview(String content) {
        return content.length() > 150 ? content.substring(0, 150) + "..." : content;
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message);
        alert.showAndWait();
    }
}