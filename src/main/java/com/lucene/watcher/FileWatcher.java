package com.lucene.watcher;

import com.lucene.indexer.Indexer;
import com.lucene.model.WatchResult;
import com.lucene.searcher.Searcher;
import org.apache.lucene.store.ByteBuffersDirectory;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.function.Consumer;

public class FileWatcher extends Watcher<List<WatchResult>> {
    Searcher searcher;
    Indexer indexer;
    String searchWord;
    ByteBuffersDirectory index = new ByteBuffersDirectory();

    public FileWatcher(String dirPath, String searchWord, Consumer<List<WatchResult>> outputFunc) throws IOException {
        super(dirPath, outputFunc);
        this.indexer = new Indexer(index);
        this.searcher = new Searcher(dirPath);
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
                    String fileName = changed.getFileName().toString().toLowerCase();
                    int idx = fileName.lastIndexOf(".");
                    String fileExt = (idx > 0) ? fileName.substring(idx + 1) : "INVALID";
                    if ((kind == StandardWatchEventKinds.ENTRY_CREATE || kind == StandardWatchEventKinds.ENTRY_MODIFY)) {
                        indexer.indexFile(changed);
                        try {
                            List<WatchResult> res = searcher.search(searchWord, 100);
                            outputFunc.accept(res);
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