package org.kirhgoff.lastocrawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by Kirill Lastovirya on 17/01/16.
 */
public class Crawler {
  private final URL startUrl;

  //page -> set of referrers
  private final ConcurrentMap<Anchor, Set<Anchor>> processed = new ConcurrentHashMap<>();
  private final ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(4);

  private static final List<String> stopWords = Arrays.asList(
      "http", "mailto", "javascript", "#"
  );

  public static void main(String[] args) throws IOException, InterruptedException {
    Crawler crawler = new Crawler("http://anima-pro.ru/");
    crawler.go();
    crawler.generateReport("/tmp/lastocrawler.csv");
  }

  public Crawler(String startUrlString) throws MalformedURLException {
    this.startUrl = new URL(startUrlString);
  }

  private void go() throws IOException, InterruptedException {
    executor.execute(new CrawlerTask(new Anchor(startUrl.toString())));
    while (executor.getActiveCount() != 0) {
      Thread.sleep(1000);
    }
    executor.shutdownNow();
  }

  private void generateReport(String fileName) throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append("Source;BrokenLink\n");
    processed.entrySet().forEach(entry -> {
      Anchor anchor = entry.getKey();
      Set<Anchor> referrers = entry.getValue();
      if (anchor.isBroken()) {
        for (Anchor reference : referrers) {
          sb.append(reference.getUrl())
            .append(";")
            .append(anchor.getUrl())
            .append("\n");
        }
        }
    });

    FileWriter fileWriter = new FileWriter(fileName);
    String string = sb.toString();
    fileWriter.write(string, 0, string.length());
    fileWriter.flush();
    fileWriter.close();

    System.out.println("Created report:" + fileName);
  }

  private class CrawlerTask implements Runnable {
    private final Anchor anchor;
    private URL url = null;

    CrawlerTask(Anchor anchor) {
      this.anchor = anchor;
    }

    @Override
    public void run() {
      System.out.println(processed.size() + " running task " + anchor.getUrl());
      try {
        url = new URL(anchor.getUrl());
        url.openConnection();

        if (anchor.isExternal()) return;

        String contents = readUrl(url);
        List<String> urls = parseAndFindAnchors(contents);
        for (String childUrl : urls) {
          childUrl = normalize(childUrl, url);
          Anchor childAnchor = new Anchor(childUrl, isExternal(childUrl, url));
          processed.compute(childAnchor, (k, v) -> {
            if (v == null) {
              v = new HashSet<>();
              //Not really atomic, could create duplicate tasks
              executor.execute(new CrawlerTask(childAnchor));
            }
            v.add(anchor);
            return v;
          });
        }
      } catch (IOException e) {
        anchor.setIsBroken(true);
      }
    }

    private String readUrl(URL url) throws IOException {
      StringBuilder sb = new StringBuilder();
      try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
        String inputLine;
        while ((inputLine = in.readLine()) != null)
          sb.append(inputLine);
      }

      return sb.toString();
    }

    private List<String> parseAndFindAnchors(String pageContents) {
      Document document = Jsoup.parse(pageContents, "UTF-8");
      Elements links = document.select("a[href]");
      List<String> result = new LinkedList<>();
      for (int i = 0; i < links.size(); i++) {
        result.add(links.eq(i).attr("href"));
      }
      return result;
    }

    private String normalize(String urlString, URL referrer) {
      if (urlString.startsWith("/")) {
        urlString = referrer.getProtocol() + "://" + referrer.getHost() + urlString;
      } else if (!startsFromStopWord(urlString)) {
        urlString = referrer.getProtocol() + "://" + referrer.getHost() + "/" + referrer.getPath().replaceFirst("/", "") + urlString;
      }

      return urlString;
    }

    private boolean startsFromStopWord(String urlString) {
      return stopWords.stream().filter(urlString::startsWith).findAny().isPresent();
    }

    private boolean isExternal(String childUrl, URL url) {
      return !childUrl.contains(url.getHost());
    }
  }


}
