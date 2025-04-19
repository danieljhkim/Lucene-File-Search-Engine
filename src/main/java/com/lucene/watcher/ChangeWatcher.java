package com.lucene.watcher;

import com.lucene.indexer.Indexer;
import com.lucene.model.WatchResult;
import com.lucene.searcher.Searcher;
import com.lucene.util.Constants;
import javafx.application.Platform;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ChangeWatcher extends Watcher<WatchResult> implements Runnable {

    private final Map<WatchKey, Path> watchKeyToPathMap = new HashMap<>();
    private final WatchService watchService;
    private volatile boolean running = true;

    public ChangeWatcher(Indexer indexer, Searcher searcher, String dirPath, Consumer<WatchResult> outputFunc) throws IOException {
        super(indexer, searcher, dirPath, outputFunc);
        this.watchService = FileSystems.getDefault().newWatchService();
        registerDirectory(dir);
    }

    @Override
    public void run() {
        try {
            while (running) {
                WatchKey key;
                try {
                    key = watchService.take(); // Blocking call
                } catch (InterruptedException e) {
                    logger.warning("Watch service interrupted: " + e.getMessage());
                    Thread.currentThread().interrupt();
                    break;
                }

                Path dir = watchKeyToPathMap.get(key);
                if (dir == null) {
                    logger.warning("WatchKey not recognized");
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        logger.warning("Event overflow occurred");
                        continue;
                    }
                    Path name = ((WatchEvent<Path>) event).context();
                    Path changedPath = dir.resolve(name);
                    logger.info("Event detected: " + kind.name() + " - " + changedPath);

                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        try {
                            if (Files.isDirectory(changedPath)) {
                                registerDirectory(changedPath);
                            }
                        } catch (IOException e) {
                            logger.warning("Failed to register new directory: " + e.getMessage());
                        }
                    }

                    processEvent(kind, changedPath);
                }

                boolean valid = key.reset();
                if (!valid) {
                    watchKeyToPathMap.remove(key);
                    logger.warning("Directory no longer accessible: " + dir);
                    if (watchKeyToPathMap.isEmpty()) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.severe("Error in ChangeWatcher: " + e.getMessage());
        } finally {
            try {
                watchService.close();
            } catch (IOException e) {
                logger.warning("Failed to close watch service: " + e.getMessage());
            }
        }
    }

    private void registerDirectory(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            return;
        }
        WatchKey key = directory.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE);

        watchKeyToPathMap.put(key, directory);
        logger.info("Watching directory: " + directory.toAbsolutePath());
        Files.list(directory)
                .filter(Files::isDirectory)
                .forEach(subdir -> {
                    try {
                        registerDirectory(subdir);
                    } catch (IOException e) {
                        logger.warning("Failed to register subdirectory: " + subdir + " - " + e.getMessage());
                    }
                });
    }

    public void stop() {
        running = false;
    }

    private void processEvent(WatchEvent.Kind<?> kind, Path changedPath) {
        String fileName = changedPath.getFileName().toString().toLowerCase();

        if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
            if (!Files.isDirectory(changedPath)) {
                notifyChange(fileName, changedPath, kind.name());
            }
            return;
        }
        if (Files.isDirectory(changedPath)) {
            return;
        }
        if (kind == StandardWatchEventKinds.ENTRY_CREATE || kind == StandardWatchEventKinds.ENTRY_MODIFY) {
            if (!isValidFileType(fileName)) {
                logger.warning("Unsupported file type: " + fileName);
                return;
            }
            notifyChange(fileName, changedPath, kind.name());
        }
    }

    private boolean isValidFileType(String fileName) {
        int idx = fileName.lastIndexOf(".");
        if (idx <= 0) {
            return false;
        }
        String fileExt = fileName.substring(idx + 1);
        return Constants.FILE_TYPES_SET.contains(fileExt);
    }

    private void notifyChange(String fileName, Path changedPath, String eventType) {
        try {
            WatchResult result = new WatchResult.Builder()
                    .fileName(fileName)
                    .filePath(changedPath.toAbsolutePath().toString())
                    .eventType(eventType)
                    .build();

            logger.info("File change detected: " + result.printChangeEvent());
            Platform.runLater(() -> outputFunc.accept(result));
        } catch (Exception e) {
            logger.severe("Error creating change notification: " + e.getMessage());
        }
    }
}