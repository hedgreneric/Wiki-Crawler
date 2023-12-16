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

        for (int i = 0; i < keywords.length; i++) {
            keywords[i] = keywords[i].toLowerCase();
        }
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
            String url = edge.child();
            if (visited.contains(url)) continue;
            visited.add(url);

            Document doc = Jsoup.connect(BASE_URL + url).get();
            Elements links = doc.select("div.mw-parser-output > p a[href~=/wiki/[^:#]*$]");
            String boydText = doc.body().text();

            for (Element link : links) {
                if (!link.attr("abs:href").contains(BASE_URL)) {
                    continue;
                }
                String href = link.attr("href");
                if (!visited.contains(href) && !disallowSet.contains(href)) {
                    queue.add(new Edge(href, url));

                }
            }

            if (!url.equals(seedUrl) && isRelevant(boydText)) {
                numRelevant++;
                printWriter.println(edge.parent() + " " + url);
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

    /*
    Seeing if the page is relevant to the list of keywords by checking if any of the keywords are in the body of that
    page
     */
    public boolean isRelevant (String bodyText) {
        bodyText = bodyText.toLowerCase();
        for (String word : keywords) {
            if (bodyText.contains(word)) {
                return true;
            }
        }
        return false;
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

    public record Edge(String child, String parent) {
    }
}