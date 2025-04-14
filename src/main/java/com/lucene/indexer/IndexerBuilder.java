package com.lucene.indexer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.ByteBuffersDirectory;

import java.io.IOException;

public class IndexerBuilder {

    private ByteBuffersDirectory index;
    private Analyzer analyzer;

    public IndexerBuilder setByteBuffersDirectory(ByteBuffersDirectory index) {
        this.index = index;
        return this;
    }

    public IndexerBuilder setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
        return this;
    }

    public Indexer build() throws IOException {
        return new Indexer(index, analyzer);
    }
}
