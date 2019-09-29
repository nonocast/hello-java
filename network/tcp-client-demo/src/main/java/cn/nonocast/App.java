package cn.nonocast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
  private static final Logger logger = LoggerFactory.getLogger(App.class);
  private RRU2889Client client;

  public App() {
    this.client = RRU2889Client.create();
    this.client.setTagListener((tags)-> {
      logger.debug("callback.tags: {}", tags);
    });
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    new App().config().open().hold().close();
  }

  private App hold() throws IOException {
    new InputStreamReader(System.in).read();
    return this;
  }

  public App open() throws InterruptedException {
    this.client.open();
    return this;
  }

  public App close() {
    this.client.close();
    return this;
  }

  private App config() throws IOException {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    try (InputStream input = loader.getResourceAsStream("config.properties")) {
      Properties prop = new Properties();
      prop.load(input);
      logger.debug("server.address: {}", prop.getProperty("server.address"));
      logger.debug("server.port: {}", prop.getProperty("server.port"));

      String address = prop.getProperty("server.address");
      int port = Integer.parseInt(prop.getProperty("server.port"));
      this.client.setEndpoint(new InetSocketAddress(address, port));
    }
    return this;
  }
}
