package com.lucene.watcher;

import com.lucene.indexer.Indexer;
import com.lucene.model.WatchResult;
import com.lucene.searcher.Searcher;
import com.lucene.util.Constants;
import com.lucene.util.logging.CustomLogger;

import java.io.IOException;
import java.nio.file.*;
import java.util.function.Consumer;

public class ChangeWatcher extends Watcher<WatchResult> implements Runnable {

    public ChangeWatcher(Indexer indexer, Searcher searcher, String dirPath, Consumer<WatchResult> outputFunc) throws IOException {
        super(indexer, searcher, dirPath, outputFunc);
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

                    if ((kind == StandardWatchEventKinds.ENTRY_CREATE || kind == StandardWatchEventKinds.ENTRY_MODIFY)) {
                        String fileName = changed.getFileName().toString().toLowerCase();
                        int idx = fileName.lastIndexOf(".");
                        String fileExt = (idx > 0) ? fileName.substring(idx + 1) : "INVALID";
                        if (!Constants.FILE_TYPES_SET.contains(fileExt)) {
                            logger.warning("Unsupported file type: " + fileExt);
                            continue;
                        }
                        // indexer.indexFile(changed);
                        try {
                            WatchResult wresult = new WatchResult.Builder()
                                    .fileName(fileName)
                                    .filePath(changed.toAbsolutePath().toString())
                                    .eventType(kind.name())
                                    .build();
                            logger.info("File indexed: " + wresult);
                            outputFunc.accept(wresult);
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