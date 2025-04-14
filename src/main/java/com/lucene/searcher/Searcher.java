package com.lucene.searcher;

import com.lucene.model.WatchResult;
import com.lucene.util.logging.CustomLogger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Searcher {

    private static final Logger logger = CustomLogger.getLogger(Searcher.class.getName());

    private final ByteBuffersDirectory index;
    private final StandardAnalyzer analyzer;
    private DirectoryReader reader;
    private IndexSearcher searcher;

    public Searcher(ByteBuffersDirectory index) throws IOException {
        this.index = index;
        this.analyzer = new StandardAnalyzer();
        this.reader = DirectoryReader.open(this.index);
        this.searcher = new IndexSearcher(this.reader);
    }

    public Searcher(ByteBuffersDirectory index, StandardAnalyzer analyzer) throws IOException {
        this.index = index;
        this.analyzer = analyzer;
        this.reader = DirectoryReader.open(this.index);
        this.searcher = new IndexSearcher(this.reader);
    }


    public void refresh() throws IOException {
        DirectoryReader newReader = DirectoryReader.openIfChanged(this.reader);
        if (newReader != null) {
            this.reader.close();
            this.reader = newReader;
            this.searcher = new IndexSearcher(this.reader);
            logger.info("IndexReader refreshed with updated content.");
        }
    }

    public List<WatchResult> search(String queryString, int maxResults) throws Exception {
        // Refresh the reader before searching
        refresh();
        List<WatchResult> wresults = new ArrayList<>();
        Query query = new QueryParser("content", analyzer).parse(queryString);
        TopDocs topDocs = searcher.search(query, maxResults);
        logger.info("Total Hits: " + topDocs.totalHits.value);
        for (ScoreDoc sd : topDocs.scoreDocs) {
            Document doc = searcher.doc(sd.doc);
            if (doc == null) {
                logger.warning("Document not found for scoreDoc: " + sd.doc);
                continue;
            }
            WatchResult wresult = new WatchResult.Builder()
                    .fileName(doc.get("filename"))
                    .filePath(doc.get("path"))
                    .content(docPreview(doc.get("content")))
                    .score(sd.score)
                    .build();
            wresults.add(wresult);
        }
        return wresults;
    }

    public ScoreDoc[] getSearch(String queryString, int maxResults) throws Exception {
        refresh();
        Query query = new QueryParser("content", analyzer).parse(queryString);
        TopDocs topDocs = searcher.search(query, maxResults);
        return topDocs.scoreDocs;
    }

    public Document getDocument(ScoreDoc hit) throws Exception {
        return searcher.doc(hit.doc);
    }

    public void close() throws IOException {
        reader.close();
    }

    private String docPreview(String content) {
        return content.length() > 150 ? content.substring(0, 150) + "..." : content;
    }
}