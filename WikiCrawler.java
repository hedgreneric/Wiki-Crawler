import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/*
@param seedUrl - relative address of the seed ur
@param keywords - words/phrases that describe a topic
@param max - max number of sites to be crawled
@param fileName - name of the file that the graph will be written to
 */
public class WikiCrawler {
    String seedUrl;
    String[] keywords;
    int max;
    String fileName;
    Set<String> disallowSet;
    private static final String BASE_URL = "https://en.wikipedia.org";

    WikiCrawler(String seedUrl, String[] keywords, int max, String fileName) throws FileNotFoundException {
        this.seedUrl = seedUrl;
        this.keywords = keywords;
        this.max = max;
        this.fileName = fileName;

        robotsDisallowed();
    }

    public void crawl() throws IOException {
        Set<String> visited = new HashSet<>();
        Queue<Edge> queue = new LinkedList<>();
        int numRelevant = 0;
        queue.add(new Edge(seedUrl, null));

        PrintWriter printWriter = new PrintWriter(fileName);
        printWriter.println(max);

        while (!queue.isEmpty() && numRelevant < max) {
            Edge edge = queue.poll();
            String url = edge.getChild();
            visited.add(url);

            Document doc = Jsoup.connect(BASE_URL + url).get();
            Elements links = doc.select("div.mw-parser-output > p a[href~=/wiki/[^:#]*$]");

            for (Element link : links) {
                String href = link.attr("href");
                if (!visited.contains(href) && !disallowSet.contains(href)) {
                    queue.add(new Edge(href, url));
                }
            }

            // TODO create a relevance method.
            if (!url.equals(seedUrl) && relevance() >= 1) {
                numRelevant++;
                printWriter.println(edge.getParent() + " " + url);
            }

            if (visited.size() % 10 == 0) { // Sleep for 1 second every 10 requests
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        printWriter.close();
    }

    public double relevance () {
        return 1;
    }

    public void robotsDisallowed () throws FileNotFoundException {
        File file = new File("./robots.txt");
        Scanner scan = new Scanner(file);
        disallowSet = new HashSet<>();
        String line;

        while (scan.hasNextLine()) {
            line = scan.nextLine();
            if (line.contains("Disallow")) {
                line = line.replaceFirst("Disallow: ", "");
                if (!line.contains("#") && !line.contains(":")) {
                    disallowSet.add(line);
                }
            }
        }
    }

    public static class Edge {
        private final String child;
        private final String parent;

        public Edge(String child, String parent) {
            this.child = child;
            this.parent = parent;
        }

        public String getChild() {
            return child;
        }

        public String getParent() {
            return parent;
        }
    }
}