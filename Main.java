import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        String[] topics = {"tennis", "grand slam"};
        WikiCrawler w = new WikiCrawler("/wiki/Tennis", topics, 100, "WikiTennisGraph.txt");
        w.crawl();
    }
}
