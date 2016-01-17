package org.kirhgoff.lastocrawler;

/**
 * Created by khrapusha on 17/01/16.
 */
public class Anchor {
  private String url;

  public Anchor(String url) {
    this.url = url;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Anchor anchor = (Anchor) o;

    return url != null ? url.equals(anchor.url) : anchor.url == null;

  }

  @Override
  public int hashCode() {
    return url != null ? url.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "Anchor{" +
        "url='" + url + '\'' +
        '}';
  }

  public String getUrl() {
    return url;
  }
}
