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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Kirill Lastovirya on 17/01/16.
 */
public class Crawler {
  private final String secondDomain;
  private final URL startUrl;

  //page -> set of referrers
  private final AtomicInteger counter = new AtomicInteger();
  private final ConcurrentMap<Anchor, Set<Anchor>> processed = new ConcurrentHashMap<>();
  private final ExecutorService executor = Executors.newCachedThreadPool();

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
    this.secondDomain = startUrl.getHost().substring("www.".length());
//    ((ThreadPoolExecutor)executor).setRejectedExecutionHandler((r, exec) -> {
//      try {
//        exec.getQueue().put(r);
//      } catch (InterruptedException e) {
//        throw new RuntimeException("Interrupted while submitting task", e);
//      }
//    });
  }

  private void go() throws IOException, InterruptedException {
    try {
      Anchor root = new Anchor(startUrl.toString());
      executor.execute(new CrawlerTask(root));
      //TODO
    } finally {
//      executor.shutdown();
//      executor.awaitTermination(5, TimeUnit.MINUTES);
    }
    Thread.sleep(10000);
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
  }

  private class CrawlerTask implements Runnable {
    private final Anchor anchor;
    private URL url = null;

    CrawlerTask(Anchor anchor) {
      this.anchor = anchor;
    }

    @Override
    public void run() {
      System.out.println("Running task number " + counter.incrementAndGet());
      try {
        if (processed.containsKey(anchor)) return;

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
            }
            v.add(anchor);
            return v;
          });
          //Not really atomic, could create duplicate tasks
          executor.execute(new CrawlerTask(childAnchor));
        }
      } catch (IOException e) {
        anchor.setIsBroken(true);
      } finally {
        counter.decrementAndGet();
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
        urlString = referrer.getProtocol() + "://" + referrer.getHost() + "/" + referrer.getPath() + "/" + urlString;
      }

      if (urlString.contains(startUrl.getHost())) {
        urlString = urlString.replace(secondDomain, startUrl.getHost());
      }
      return urlString;
    }

    private boolean startsFromStopWord(String urlString) {
      return stopWords.stream().filter(urlString::startsWith).findAny().isPresent();
    }

    private boolean isExternal(String childUrl, URL url) {
      return childUrl.contains(url.getHost());
    }
  }


}
