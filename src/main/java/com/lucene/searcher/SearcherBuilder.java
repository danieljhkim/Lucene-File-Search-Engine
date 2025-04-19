package com.lucene.searcher;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.store.ByteBuffersDirectory;

import java.io.IOException;

public class SearcherBuilder {

    private static SearcherBuilder instance;

    private ByteBuffersDirectory index;
    private StandardAnalyzer analyzer;

    private SearcherBuilder() {}

    public static synchronized SearcherBuilder getInstance() {
        if (instance == null) {
            instance = new SearcherBuilder();
        }
        return instance;
    }

    public SearcherBuilder setIndex(ByteBuffersDirectory index) {
        this.index = index;
        return this;
    }

    public SearcherBuilder setAnalyzer(StandardAnalyzer analyzer) {
        this.analyzer = analyzer;
        return this;
    }

    public Searcher build() throws IOException {
        if (index == null || analyzer == null) {
            throw new IllegalStateException("Index and Analyzer must be set before building the Searcher.");
        }
        return new Searcher(index, analyzer);
    }
}