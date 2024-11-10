import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        String[] topics = {"ball", "goal"};

        String outputFile = "Graph.txt";
        for (int i = 0; i < topics.length; i++) {
            outputFile = topics[i] + "-" + outputFile;
        }
        WikiCrawler w = new WikiCrawler("/wiki/Tennis", topics, 50, outputFile);
        w.crawl();
    }
}
