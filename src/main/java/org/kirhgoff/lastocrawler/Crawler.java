package org.kirhgoff.lastocrawler;

import com.google.common.base.Joiner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by khrapusha on 17/01/16.
 */
public class Crawler {
  public static void main(String[] args) throws IOException {
    Crawler crawler = new Crawler();
    crawler.go("http://www.anima-pro.ru");
  }

  public Crawler() {
  }

  private void go(String startUrl) throws IOException {
    String pageContents = readUrl(startUrl);
    List<String> urls = parseAndFindAnchors(pageContents);
    System.out.println(Joiner.on("\n").join(urls));
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

  private String readUrl(String startUrl) throws IOException {
    URL url = new URL(startUrl);

    StringBuilder sb = new StringBuilder();
    try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
      String inputLine;
      while ((inputLine = in.readLine()) != null)
        sb.append(inputLine);
    }

    return sb.toString();
  }

}
