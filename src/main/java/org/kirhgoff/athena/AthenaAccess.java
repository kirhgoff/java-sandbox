package org.kirhgoff.athena;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class AthenaAccess {
  private static final String JDBC_DRIVER = "com.amazonaws.athena.jdbc.AthenaDriver";
  private static final String DB_URL = "jdbc:awsathena://athena.us-west-2.amazonaws.com:443";
  //private static final String DB_URL = "jdbc:awsathena://athena.ap-southeast-2.amazonaws.com:443";

  //private static final String SQL = "SELECT * FROM mixpanel_data.mixpanel limit 10;";
  private static final String SQL = "show tables in mixpanel_data";
  public static final String STAGING_DIR = "s3://goo-persist/mixpanel/";

  public static void main(String[] args) {
    Connection connection = null;
    Statement statement = null;
    try {
      Class.forName(JDBC_DRIVER);
      connection = DriverManager.getConnection(DB_URL, createProperties());
      statement = connection.createStatement();
      ResultSet rs = statement.executeQuery(SQL);

      while (rs.next()) {
        String event = rs.getString(0);
        System.out.print("EVENT: " + event);
      }
      rs.close();
      statement.close();
      connection.close();

    } catch (Exception exception) {
      exception.printStackTrace();
    } finally {
      safelyClose(statement);
      safelyClose(connection);
    }

  }

  private static Properties createProperties() {
    Properties properties = new Properties();
    properties.put("aws_credentials_provider_class",EnvironmentCredentialsProvider.class.getName());
    properties.put("s3_staging_dir", STAGING_DIR);
    properties.put("log_level", "ALL");
    properties.put("log_path", "./logs/athena.log");
    System.out.println("Props are: " + properties);
    return properties;
  }

  private static void safelyClose(AutoCloseable closeable) {
    try {
      if (closeable != null) closeable.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static class EnvironmentCredentialsProvider implements AWSCredentialsProvider {
    public EnvironmentCredentialsProvider() {}

    @Override
    public AWSCredentials getCredentials() {
      return new AWSCredentials() {
        @Override
        public String getAWSAccessKeyId() {
          return System.getenv("AWS_ACCESS_KEY_ID");
        }

        @Override
        public String getAWSSecretKey() {
          return System.getenv("AWS_SECRET_ACCESS_KEY");
        }
      };
    }

    @Override
    public void refresh() {}
  }
}
