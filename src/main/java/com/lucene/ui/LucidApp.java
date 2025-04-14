package com.lucene.ui;

import com.lucene.indexer.Indexer;
import com.lucene.searcher.Searcher;
import com.lucene.ui.views.LogView;
import com.lucene.ui.views.SearchView;
import com.lucene.ui.views.WatchView;
import com.lucene.util.logging.LogAppender;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.apache.lucene.store.ByteBuffersDirectory;

public class LucidApp extends Application {

    private static final String APP_NAME = "LucidSearch";
    private final LogView logView = new LogView();
    public LogAppender logAppender = new LogAppender(APP_NAME);
    private SearchView searchView;
    private WatchView watchView;
    private final ByteBuffersDirectory index = new ByteBuffersDirectory();
    private Indexer indexer;
    private Searcher searcher;

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle(APP_NAME);
        searchView = new SearchView(primaryStage, logView, index);
        this.indexer = searchView.initializeIndexer();
        this.searcher = searchView.initializeSearcher();
        searchView.init();
        watchView = new WatchView(primaryStage, logView, indexer, searcher);

        // --- TabPane setup ---
        TabPane tabPane = new TabPane();
        Tab searchTab = new Tab("Search", searchView);
        searchTab.setClosable(false);
        Tab logsTab = new Tab("Logs", logView);
        logsTab.setClosable(false);
        Tab watchTab = new Tab("Watch", watchView);
        watchTab.setClosable(false);
        tabPane.getTabs().addAll(searchTab, watchTab, logsTab);

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
