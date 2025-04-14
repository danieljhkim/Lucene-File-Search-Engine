package com.lucene.searcher;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.store.ByteBuffersDirectory;

import java.io.IOException;

public class SearcherBuilder {
    private ByteBuffersDirectory index;
    private StandardAnalyzer analyzer;

    public SearcherBuilder setIndex(ByteBuffersDirectory index) {
        this.index = index;
        return this;
    }

    public SearcherBuilder setAnalyzer(StandardAnalyzer analyzer) {
        this.analyzer = analyzer;
        return this;
    }

    public Searcher build() throws IOException {
        return new Searcher(index, analyzer);
    }
}
    