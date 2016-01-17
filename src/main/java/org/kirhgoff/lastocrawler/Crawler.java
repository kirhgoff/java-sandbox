package org.kirhgoff.lastocrawler;

import com.google.common.base.Joiner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by khrapusha on 17/01/16.
 */
public class Crawler {
  private final String secondDomain;
  private final URL startUrl;
  private final BlockingQueue<Anchor> queue = new LinkedBlockingQueue<>();
  private final Set<Anchor> processed = new HashSet<>();

  private final Set<String> wontProcess = new HashSet<>();
  private final Set<Anchor> brokenLinkSource = new HashSet<>();

  private static final List<String> stopWords = Arrays.asList(
      "http", "mailto", "javascript", "#"
  );

  public static void main(String[] args) throws IOException, InterruptedException {
    Crawler crawler = new Crawler("http://www.anima-pro.ru/");
    crawler.go();
    crawler.generateReport("/tmp/lastocrawler.csv");
  }

  public Crawler(String startUrlString) throws MalformedURLException {
    this.startUrl = new URL(startUrlString);
    this.secondDomain = startUrl.getHost().substring("www.".length());
  }

  private void go() throws IOException, InterruptedException {
    Anchor root = new Anchor(startUrl.toString());
    queue.add(root);
    while (!queue.isEmpty()) {
      Anchor current = queue.take();
      processed.add(current);

      String pageContents = readUrl(current);
      List<String> urls = parseAndFindAnchors(pageContents);

      urls.stream().forEach(u -> processUrl(u, current));

      System.out.println("Queue " + queue.size() + ", processed " + processed.size());
    }
  }

  private void processUrl(String url, Anchor source) {
    Anchor newAnchor = null;
    url = normalize(url);
    if (isLocalUrl(url)) {
      newAnchor = new Anchor(url);
    } else {
      //Outer link
      if (url.startsWith("http")) {
        checkAvailability(url, source);
      }
    }

    if (newAnchor != null) {
      if (!processed.contains(newAnchor) && !queue.contains(newAnchor)) {
        queue.add(newAnchor);
      } else {
        updateReferences(newAnchor, source);
      }
    }
  }

  private void updateReferences(Anchor newAnchor, Anchor source) {
    updateReference(newAnchor, source, processed);
    updateReference(newAnchor, source, queue);
    updateReference(newAnchor, source, brokenLinkSource);
  }

  private void updateReference(Anchor anchor, Anchor referencedBy, Collection<Anchor> processed) {
    Optional<Anchor> option = processed.stream().filter(a -> a.equals(anchor)).findAny();
    if (option.isPresent()) {
      option.get().addReference(referencedBy);
    }
  }

  private void checkAvailability(String urlString, Anchor source) {
    try {
      URL url = new URL(urlString);
      url.openConnection();
    } catch (Exception e) {
      Anchor temp = new Anchor(urlString);
      temp.addReference(source);
      brokenLinkSource.add(temp);
    }
  }

  private boolean isLocalUrl(String urlString) {
    try {
      URL url = new URL(urlString);
      return startUrl.getHost().equals(url.getHost());
    } catch (MalformedURLException e) {
      System.out.println("Cannot process url:" + urlString);
      wontProcess.add(urlString);
      return false;
    }
  }

  private String normalize(String urlString) {
    if (urlString.startsWith("/")) {
      urlString = startUrl.getProtocol() + "://" + startUrl.getHost() + urlString;
    } else if (!stopWords.stream().filter(urlString::startsWith).findAny().isPresent()) {
      urlString = startUrl.getProtocol() + "://" + startUrl.getHost() + "/" + urlString;
    }

    //Hack
    if (!urlString.contains(startUrl.getHost())) {
      urlString = urlString.replace(secondDomain, startUrl.getHost());
    }
    return urlString;
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

  private String readUrl(Anchor anchor) throws MalformedURLException {
    URL url = new URL(anchor.getUrl());

    StringBuilder sb = new StringBuilder();
    try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
      String inputLine;
      while ((inputLine = in.readLine()) != null)
        sb.append(inputLine);
    } catch (IOException e) {
      System.out.println("Broken link: " + anchor.getUrl());
      brokenLinkSource.add(anchor);
    }

    return sb.toString();
  }

  private void generateReport(String fileName) throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append("Source;BrokenLink\n");
    for (Anchor anchor : brokenLinkSource) {
      for (Anchor reference : anchor.getReferences()) {
        sb.append(reference.getUrl())
            .append(";")
            .append(anchor.getUrl())
            .append("\n");
      }
    }

    FileWriter fileWriter = new FileWriter(fileName);
    String string = sb.toString();
    fileWriter.write(string, 0, string.length());
    fileWriter.flush();
    fileWriter.close();
  }

}
