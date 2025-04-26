package com.lucene.watcher;

import com.lucene.util.logging.CustomLogger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Abstract base class for all watcher implementations.
 *
 * @param <T> The type of results this watcher produces
 */
public abstract class Watcher<T> implements Runnable {

    protected static final Logger logger = CustomLogger.getLogger(Watcher.class.getName());
    protected final Path dir;
    protected final Consumer<T> outputFunc;
    protected volatile boolean running = true;

    protected Watcher(String dirPath, Consumer<T> outputFunc) {
        this.dir = Paths.get(dirPath);
        this.outputFunc = outputFunc;
    }

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }
}
