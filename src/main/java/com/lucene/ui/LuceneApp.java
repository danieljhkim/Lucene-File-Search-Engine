package com.lucene.ui;

import com.lucene.ui.views.LogView;
import com.lucene.ui.views.SearchView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class LuceneApp extends Application {

    private static final String APP_NAME = "LucidSearch";
    private final LogView logView = new LogView();
    private SearchView searchView;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle(APP_NAME);
        searchView = new SearchView(primaryStage, logView);
        searchView.init();

        // --- TabPane setup ---
        TabPane tabPane = new TabPane();
        Tab searchTab = new Tab("Search", searchView);
        searchTab.setClosable(false);
        Tab logsTab = new Tab("Logs", logView);
        logsTab.setClosable(false);
        tabPane.getTabs().addAll(searchTab, logsTab);

        primaryStage.setScene(new Scene(tabPane, 600, 400));
        primaryStage.show();
    }

    /**
     * Clean up resources on application exit.
     */
    @Override
    public void stop() throws Exception {
        if (searchView != null) {
            searchView.close();
        }
        super.stop();
    }
}
