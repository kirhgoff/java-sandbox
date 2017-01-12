package org.kirhgoff.parser;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.awt.Color.*;

/**
 * Created by klastovirya on 5/1/17.
 */
public class AccessLogParser {
  private static final String postfix = "2016-12-21";
  private static final String fileName = "/Users/klastovirya/Investigations/2017-01-05-slave-configs/outages/localhost_access_log.2016-12-21.txt.1";
  private static final String outputFolder = "/Users/klastovirya/Investigations/2017-01-05-slave-configs/outages/tables";
  private static final Pattern pattern = Pattern.compile("([0-9\\.]+) - - \\[([0-9A-Z-a-z\\/\\:]+).* ([0-9]+)");
  private static final LocalDateTime start = LocalDateTime.of(2016, 12, 15, 10, 0);
  private static final LocalDateTime end = LocalDateTime.of(2016, 12, 15, 14, 0);

  private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss");
  private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

  public static void main(String[] args) throws IOException {
    long startedAt = System.currentTimeMillis();
    //read file into stream, try-with-resources
    final Collection<Record> records = new LinkedList<>();
    try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
      stream.forEach(line -> {
        if (!line.contains("replication")) return;

        Matcher m = pattern.matcher(line);
        m.find();

        records.add(new Record(
            m.group(1),
            LocalDateTime.parse(m.group(2), dateFormatter),
            Long.valueOf(m.group(3)),
            1L
        ));
      });

      System.out.println("Records " + records.size());
    } catch (IOException e) {
      e.printStackTrace();
    }

    Collection<Record> reduced = reduce(records);
    Table<LocalDateTime, String, Record> table = makeTable(reduced);

    List<String> ips = reduced.stream().map(Record::getIp).distinct().sorted().collect(Collectors.toList());
    List<LocalDateTime> times = reduced.stream().map(Record::getDateTime).distinct().sorted().collect(Collectors.toList());


    save(postfix + "_sizes" + ".csv", table, Record::getSize, ips, times);
    save(postfix + "_counts" + ".csv", table, Record::getCount, ips, times);
    //saveGraph(table, ips, times);

    System.out.println("Read at " + (System.currentTimeMillis() - startedAt));
  }

  private static void save(String reportName, Table<LocalDateTime, String, Record> table, Function<Record, Long> function, List<String> ips, List<LocalDateTime> times) throws IOException {
    StringBuilder sb = new StringBuilder ();

    makeHeader(ips, sb);

    for (LocalDateTime dateTime : times) {
      sb.append(dateTime.format(timeFormatter));
      for (String ip : ips) {
        sb.append("; ");
        Record record = table.get(dateTime, ip);
        Long value;
        if (record != null) {
          value = function.apply(record);
          sb.append(value);
        }
      }
      sb.append("\n");
    }

    Path targetPath = Paths.get(outputFolder + "/" + reportName);
    Files.write(targetPath, sb.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
 }

 private static void saveGraph(Table<LocalDateTime, String, Record> table, List<String> ips, List<LocalDateTime> times) throws IOException {
   Stack<Color> colors = new Stack<>();
   colors.addAll(Arrays.asList(red, blue, green, magenta, orange, black, gray, lightGray, darkGray, pink));
   XYChart chart = new XYChart(2024, 800);
   chart.setTitle("size");
   chart.setXAxisTitle("time");
   chart.setXAxisTitle("requests");
   for (String ip : ips) {
     XYSeries series = chart.addSeries(ip, convert(times), extract(table, ip, times));
     series.setLineColor(new Color(colors.pop().getRGB()));
   }
   BitmapEncoder.saveBitmap(chart, outputFolder + "/" + "count.png", BitmapEncoder.BitmapFormat.PNG);
 }

  private static List<Long> convert(List<LocalDateTime> times) {
    LocalDateTime zero = times.get(0);
    return times.stream().map(t -> zero.until(t, ChronoUnit.MINUTES)).collect(Collectors.toList());
  }

  private static List<? extends Number> extract(Table<LocalDateTime, String, Record> table, String ip, List<LocalDateTime> times) {
    Map<LocalDateTime, Record> map = table.column(ip);
    List<Long> values =  new LinkedList<>();
    for (LocalDateTime time : times) {
      Record record = map.get(time);
      Long count = record != null ? record.getCount() : 0L;
      values.add(count);
    }
    return values;
  }



  private static void makeHeader(List<String> ips, StringBuilder sb) {
    sb.append("Time");
    for (String ip : ips) {
      sb.append("; ").append(ip);
    }
    sb.append("\n");
  }

  private static Table<LocalDateTime, String, Record> makeTable(Collection<Record> records) {
    Table<LocalDateTime, String, Record> table = HashBasedTable.create();
    for (Record record : records) {
      table.put(record.getDateTime(), record.getIp(), record);
    }
    return table;
  }

  private static Collection<Record> reduce(Collection<Record> records) {
    Map<String, Record> map = new HashMap<>();
    for (Record record : records) {
      LocalDateTime dateTime = record.getDateTime();
//      if (dateTime.isBefore(start) || dateTime.isAfter(end)) {
//        continue;
//      }

      String bucketKey = record.getBucketKey();
      map.merge(bucketKey, record, (r1, r2)
          -> new Record(r1.ip, r1.dateTime, r1.size + r2.size, r1.count + r2.count));
    }
    return map.values();
  }

  private static class Record {
    final private String ip;
    final private LocalDateTime dateTime;
    final private Long size;
    final private Long count;
    final private String bucketKey;

    Record (String ip, LocalDateTime dateTime, Long size, Long count) {
      this.ip = ip;
      this.dateTime = dateTime;
      this.size = size;
      this.count = count;
      this.bucketKey = ip + "@" + dateTime.format(timeFormatter);
    }

    public String getIp() {
      return ip;
    }

    public LocalDateTime getDateTime() {
      return dateTime;
    }

    public Long getSize() {
      return size;
    }

    public Long getCount() {
      return count;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Record record = (Record) o;

      if (ip != null ? !ip.equals(record.ip) : record.ip != null) return false;
      return dateTime != null ? dateTime.equals(record.dateTime) : record.dateTime == null;
    }

    @Override
    public int hashCode() {
      int result = ip != null ? ip.hashCode() : 0;
      result = 31 * result + (dateTime != null ? dateTime.hashCode() : 0);
      return result;
    }

    public String getBucketKey() {
      return bucketKey;
    }
  }
}
