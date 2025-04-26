package com.lucene.ui;

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
    private final ByteBuffersDirectory index = new ByteBuffersDirectory();
    public LogAppender logAppender = new LogAppender(APP_NAME);
    private SearchView searchView;

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle(APP_NAME);
        searchView = new SearchView(primaryStage, logView, index);
        searchView.init();
        WatchView watchView = new WatchView(primaryStage, logView);

        // --- TabPane setup ---
        TabPane tabPane = new TabPane();
        Tab searchTab = new Tab("Search", searchView);
        searchTab.setClosable(false);
        Tab logsTab = new Tab("Logs", logView);
        logsTab.setClosable(false);
        Tab watchTab = new Tab("Monitor", watchView);
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
