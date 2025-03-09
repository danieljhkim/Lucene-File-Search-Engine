package com.danieljhkim.searcher;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;

public class Searcher {

    private final ByteBuffersDirectory index;
    private final StandardAnalyzer analyzer;

    public Searcher(ByteBuffersDirectory index) {
        this.index = index;
        this.analyzer = new StandardAnalyzer();
    }

    public void search(String queryString, int maxResults) throws Exception {
        Query query = new QueryParser("content", analyzer).parse(queryString);
        IndexSearcher searcher;

        try (var reader = DirectoryReader.open(index)) {
            searcher = new IndexSearcher(reader);
            TopDocs topDocs = searcher.search(query, maxResults);

            System.out.println("Total Hits: " + topDocs.totalHits.value);
            for (ScoreDoc sd : topDocs.scoreDocs) {
                Document doc = searcher.doc(sd.doc);
                System.out.println("Document: " + doc.get("filename"));
                System.out.println("Content: " + docPreview(doc.get("content")));
                System.out.println("Score: " + sd.score);
                System.out.println("----");
            }
        }
    }

    public ScoreDoc[] getSearch(String queryString, int maxResults) throws Exception {
        Query query = new QueryParser("content", analyzer).parse(queryString);
        IndexSearcher searcher;
        try (var reader = DirectoryReader.open(index)) {
            searcher = new IndexSearcher(reader);
            TopDocs topDocs = searcher.search(query, maxResults);
            return topDocs.scoreDocs;
        }
    }

    public Document getDocument(ScoreDoc hit) throws Exception {
        try (var reader = DirectoryReader.open(index)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            return searcher.doc(hit.doc);
        }
    }

    private String docPreview(String content) {
        return content.length() > 150 ? content.substring(0, 150) + "..." : content;
    }


}