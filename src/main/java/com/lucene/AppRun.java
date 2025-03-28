package com.lucene;

import com.lucene.indexer.Indexer;
import com.lucene.searcher.Searcher;
import com.lucene.util.logging.CustomLogger;
import com.lucene.watcher.FileWatcher;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.store.ByteBuffersDirectory;

import java.util.logging.Logger;

public class AppRun {

    private static final Logger logger = CustomLogger.getLogger(AppRun.class.getName());

    public static void main(String[] args) throws Exception {
        System.out.println("Starting Lucene application...");
        start();
    }

    public static void start() {
        try {
            String filepath = "/Users/daniel/repos/java/LucidSearch/src/main/resources";
            ByteBuffersDirectory index = new ByteBuffersDirectory();

            Analyzer analyzer = new StandardAnalyzer();
            Indexer indexer = new Indexer(index, analyzer);
            indexer.indexDirectory(filepath);

            Searcher searcher = new Searcher(index);
            FileWatcher watcher = new FileWatcher(indexer, searcher, filepath, "Lucene");
            Thread watcherThread = new Thread(watcher);
            watcherThread.setDaemon(true);
            watcherThread.start();
            watcherThread.join();

            searcher.close();
            indexer.close();
            logger.info("Application finished.");
        } catch (Exception e) {
            logger.severe("Error initializing Lucene: " + e.getMessage());
        }

    }
}
