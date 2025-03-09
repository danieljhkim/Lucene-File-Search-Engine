package com.danieljhkim.indexer;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.ByteBuffersDirectory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Indexer {

    private final ByteBuffersDirectory index;
    private final StandardAnalyzer analyzer;

    public Indexer(ByteBuffersDirectory index) {
        this.index = index;
        this.analyzer = new StandardAnalyzer();
    }

    public void indexDirectory(String directoryPath) throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        try (IndexWriter writer = new IndexWriter(index, config)) {
            Path docDir = Paths.get(directoryPath);
            if (!Files.isDirectory(docDir)) {
                throw new IOException(directoryPath + " is not a valid directory");
            }

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(docDir, "*.txt")) {
                for (Path file : stream) {
                    indexFile(writer, file);
                }
            }
        }
    }

    private void indexFile(IndexWriter writer, Path filePath) throws IOException {
        String content = Files.readString(filePath);

        Document doc = new Document();
        doc.add(new TextField("filename", filePath.getFileName().toString(), Field.Store.YES));
        doc.add(new TextField("content", content, Field.Store.YES));

        writer.addDocument(doc);
        System.out.println("Indexed: " + filePath.getFileName());
    }
}