package com.danieljhkim.watcher;

import com.danieljhkim.indexer.Indexer;
import org.apache.lucene.store.ByteBuffersDirectory;

import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcher implements Runnable {

    private final Path dir;
    private final Indexer indexer;

    public FileWatcher(ByteBuffersDirectory index, String dirPath) {
        this.dir = Paths.get(dirPath);
        this.indexer = new Indexer(index);
    }

    @Override
    public void run() {
        try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
            dir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
            System.out.println("Watching directory: " + dir.toAbsolutePath());

            WatchKey key;
            while ((key = watcher.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path changed = dir.resolve((Path) event.context());

                    System.out.println("Event detected: " + kind.name() + " - " + changed);

                    // Re-index whenever a .txt file is added or modified
                    if (kind == ENTRY_CREATE || kind == ENTRY_MODIFY) {
                        if (changed.toString().endsWith(".txt")) {
                            indexer.indexDirectory(dir.toString());
                        }
                    }

                }
                key.reset();
            }
        } catch (InterruptedException | IOException e) {
            System.err.println("Error in FileWatcher: " + e.getMessage());
        }
    }

}