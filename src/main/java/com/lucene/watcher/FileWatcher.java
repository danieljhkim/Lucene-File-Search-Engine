package com.lucene.watcher;

import com.lucene.indexer.Indexer;
import com.lucene.searcher.Searcher;
import com.lucene.util.logging.CustomLogger;

import java.io.IOException;
import java.nio.file.*;
import java.util.logging.Logger;

public class FileWatcher implements Runnable {

    private static final Logger logger = CustomLogger.getLogger(FileWatcher.class.getName());

    private final Path dir;
    private final Indexer indexer;
    private final Searcher searcher;
    private final int maxResults = 10;
    private final String searchWord;

    public FileWatcher(Indexer indexer, Searcher searcher, String dirPath, String searchWord) throws IOException {
        this.dir = Paths.get(dirPath);
        this.indexer = indexer;
        this.searcher = searcher;
        this.searchWord = searchWord;
    }

    @Override
    public void run() {
        try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
            dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
            logger.info("Watching directory: " + dir.toAbsolutePath());

            WatchKey key;
            while ((key = watcher.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path changed = dir.resolve((Path) event.context());
                    logger.info("Event detected: " + kind.name() + " - " + changed);

                    if ((kind == StandardWatchEventKinds.ENTRY_CREATE || kind == StandardWatchEventKinds.ENTRY_MODIFY)
                            && changed.toString().endsWith(".txt")) {
                        indexer.indexFile(changed);
                        try {
                            searcher.search(searchWord, maxResults);
                        } catch (Exception e) {
                            logger.severe("Error during search: " + e.getMessage());
                        }
                    }
                }
                key.reset();
            }
        } catch (InterruptedException | IOException e) {
            logger.severe("Error in FileWatcher: " + e.getMessage());
        }
    }
}