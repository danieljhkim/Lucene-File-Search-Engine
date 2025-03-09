import com.danieljhkim.indexer.Indexer;
import com.danieljhkim.searcher.Searcher;
import com.danieljhkim.watcher.FileWatcher;
import org.apache.lucene.store.ByteBuffersDirectory;

public class LuceneTest {

    public static void main(String[] args) throws Exception {
        String filepath = "";
        ByteBuffersDirectory index = new ByteBuffersDirectory();

        Indexer indexer = new Indexer(index);
        indexer.indexDirectory(filepath);

        Searcher searcher = new Searcher(index);
        searcher.search("Lucene", 10);

        Thread watcherThread = new Thread(new FileWatcher(index, filepath));
        watcherThread.setDaemon(true);
        watcherThread.start();
        watcherThread.join();
    }
}
