package com.lucene.watcher;

import com.lucene.indexer.Indexer;
import com.lucene.model.WatchResult;
import com.lucene.searcher.Searcher;
import com.lucene.util.logging.CustomLogger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

public abstract class Watcher<T> implements Runnable {
    static final Logger logger = CustomLogger.getLogger(Watcher.class.getName());

    Path dir;
    Indexer indexer;
    Searcher searcher;
    Consumer<T> outputFunc;
    String searchWord;

    public Watcher(Indexer indexer, Searcher searcher, String dirPath, Consumer<WatchResult> outputFunc) throws IOException {
        this.dir = Paths.get(dirPath);
        this.indexer = indexer;
        this.searcher = searcher;
        this.outputFunc = (Consumer<T>) outputFunc;
    }

    public Watcher(Indexer indexer, Searcher searcher, String dirPath, Consumer<List<WatchResult>> outputFunc, String searchWord) throws IOException {
        this.dir = Paths.get(dirPath);
        this.indexer = indexer;
        this.searcher = searcher;
        this.searchWord = searchWord;
        this.outputFunc = (Consumer<T>) outputFunc;
    }

    abstract public void run();

}
