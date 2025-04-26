package com.lucene.indexer;

import com.lucene.util.Constants;
import com.lucene.util.FileUtil;
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
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class Indexer {

    private static final Logger logger = CustomLogger.getLogger(Indexer.class.getName());
    private final Directory directory;
    private final Analyzer analyzer;
    private final IndexWriter writer;
    private final String sourceDirectoryPath;

    public Indexer(ByteBuffersDirectory index) throws IOException {
        this.directory = index;
        this.analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        this.writer = new IndexWriter(this.directory, config);
        this.sourceDirectoryPath = null;
    }

    public Indexer(ByteBuffersDirectory index, Analyzer analyzer) throws IOException {
        this.directory = index;
        this.analyzer = analyzer;
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        this.writer = new IndexWriter(directory, config);
        this.sourceDirectoryPath = null;
    }
    
    /**
     * Create an indexer for a given source directory.
     * The index will be stored in "LucidSearch/data/[encoded-directory-name]"
     */
    public Indexer(String sourceDirectoryPath) throws IOException {
        this.sourceDirectoryPath = sourceDirectoryPath;
        this.analyzer = new StandardAnalyzer();
        Path indexPath = FileUtil.getIndexPath(sourceDirectoryPath);
        Files.createDirectories(indexPath);
        
        logger.info("Creating index at: " + indexPath);
        this.directory = FSDirectory.open(indexPath);
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        // Use CREATE_OR_APPEND to preserve any existing index
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        this.writer = new IndexWriter(directory, config);
    }

    public void indexDirectory(String directoryPath, String fileType) throws IOException {
        Path docDir = Paths.get(directoryPath);
        if (!Files.isDirectory(docDir)) {
            throw new IOException(directoryPath + " is not a valid directory");
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(docDir)) {
            for (Path file : stream) {
                if (!Files.isDirectory(file)) {
                    String fileName = file.getFileName().toString().toLowerCase();
                    int idx = fileName.lastIndexOf(".");
                    String fileExt = (idx > 0) ? fileName.substring(idx + 1) : "INVALID";
                    if (Constants.FILE_TYPES_SET.contains(fileExt)) {
                        logger.info("File extension: " + file);
                        if (fileType.equals("all")) {
                            indexFile(file);
                        } else {
                            if (file.toString().endsWith(fileType)) {
                                logger.info("Indexing file: " + file.getFileName());
                                indexFile(file);
                            }
                        }
                    }
                } else {
                    logger.info("**Exploring subdirectory (DFS time)**: " + file);
                    indexDirectory(file.toString(), fileType);
                }
            }
        }
    }

    public void indexFile(Path filePath) throws IOException {
        String content = Files.readString(filePath);

        Document doc = new Document();
        doc.add(new TextField("filename", filePath.getFileName().toString(), Field.Store.YES));
        doc.add(new TextField("content", content, Field.Store.YES));
        doc.add(new TextField("path", filePath.toAbsolutePath().toString(), Field.Store.YES));
        writer.updateDocument(new Term("filename", filePath.getFileName().toString()), doc);
        writer.commit();
        logger.info("Indexed: " + filePath.getFileName());
    }

    public void close() throws IOException {
        writer.close();
        directory.close();
    }

}
