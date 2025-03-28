package com.lucene.indexer;

import com.lucene.util.logging.CustomLogger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.ByteBuffersDirectory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class Indexer {

    private static final Logger logger = CustomLogger.getLogger(Indexer.class.getName());

    private final ByteBuffersDirectory index;
    private final Analyzer analyzer;
    private final IndexWriter writer;

    public Indexer(ByteBuffersDirectory index) throws IOException {
        this.index = index;
        this.analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        this.writer = new IndexWriter(this.index, config);
    }

    public Indexer(ByteBuffersDirectory index, Analyzer analyzer) throws IOException {
        this.index = index;
        this.analyzer = analyzer;
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        this.writer = new IndexWriter(index, config);
    }

    public void indexDirectory(String directoryPath) throws IOException {
        Path docDir = Paths.get(directoryPath);
        if (!Files.isDirectory(docDir)) {
            throw new IOException(directoryPath + " is not a valid directory");
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(docDir, "*.txt")) {
            for (Path file : stream) {
                indexFile(file);
            }
        }
    }

    public void indexFile(Path filePath) throws IOException {
        String content = Files.readString(filePath);

        Document doc = new Document();
        doc.add(new TextField("filename", filePath.getFileName().toString(), Field.Store.YES));
        doc.add(new TextField("content", content, Field.Store.YES));

        // Use updateDocument to replace older versions with same filename
        writer.updateDocument(new Term("filename", filePath.getFileName().toString()), doc);
        writer.commit();
        logger.info("Indexed: " + filePath.getFileName());
    }

    public void close() throws IOException {
        writer.close();
    }
}
