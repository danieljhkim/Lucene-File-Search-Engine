import com.danieljhkim.indexer.Indexer;
import com.danieljhkim.searcher.Searcher;
import com.danieljhkim.watcher.FileWatcher;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.store.ByteBuffersDirectory;

import java.util.logging.Logger;

public class LuceneTest {

    private static final Logger logger = Logger.getLogger(LuceneTest.class.getName());

    public static void main(String[] args) throws Exception {
        String filepath = "/Users/daniel/repos/java/LucidSearch/src/main/resources";
        ByteBuffersDirectory index = new ByteBuffersDirectory();

        Analyzer analyzer = new StandardAnalyzer();
        Indexer indexer = new Indexer(index, analyzer);
        indexer.indexDirectory(filepath);

        Searcher searcher = new Searcher(index);
//        searcher.search("Lucene", 10);

        FileWatcher watcher = new FileWatcher(indexer, searcher, filepath, "Lucene");
        Thread watcherThread = new Thread(watcher);
        watcherThread.setDaemon(true);
        watcherThread.start();
        watcherThread.join();

        searcher.close();
        indexer.close();
        logger.info("Application finished.");
    }
}