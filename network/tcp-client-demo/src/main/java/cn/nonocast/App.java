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
  private RRU2889Client client2;

  public App() {
    this.client = RRU2889Client.create("netty");
    this.client.setTagListener((tags) -> {
      logger.debug("callback.tags({}): {}", tags.size(), tags);
    });

    this.client2 = RRU2889Client.create("netty");
    this.client2.setTagListener((tags) -> {
      logger.debug("callback.tags({}): {}", tags.size(), tags);
    });
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    new App().config(args).open().hold().close();
  }

  private App hold() throws IOException {
    new InputStreamReader(System.in).read();
    return this;
  }

  public App open() throws InterruptedException {
    this.client.open();
    this.client2.open();
    return this;
  }

  public App close() {
    this.client.close();
    this.client2.close();
    return this;
  }

  private App config(String[] args) throws IOException {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    try (InputStream input = loader.getResourceAsStream("config.properties")) {
      Properties prop = new Properties();
      prop.load(input);
      String address = prop.getProperty("server.address");
      int port = Integer.parseInt(prop.getProperty("server.port"));
      this.client.setHost(address);
      this.client.setPort(port);
    }

    // 如果命令行有参数，覆盖配置文件参数
    if (args.length > 0) {
      try {
        int port = Integer.parseInt(args[0]);
        this.client.setPort(port);
      } catch (NumberFormatException e) {
        // ignore
      }
    }

    logger.debug("server.address: {}", this.client.getHost());
    logger.debug("server.port: {}", this.client.getPort());

    this.client2.setHost(this.client.getHost());
    this.client2.setPort(12007);

    return this;
  }
}
