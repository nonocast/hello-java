package cn.nonocast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
  private static final Logger logger = LoggerFactory.getLogger(App.class);
  private Socket client;
  private SocketAddress endpoint;
  private PrintWriter out;
  private BufferedReader in;

  public static void main(String[] args) throws UnknownHostException, IOException {
    App app = new App().config().runLoop();
    
    // wait
    new InputStreamReader(System.in).read();

    app.close();
  }

  private App runLoop() throws UnknownHostException, IOException {
    logger.debug("application start.");
    this.client = new Socket();
    this.client.connect(this.endpoint);
    logger.debug(client.toString());

    // InputStream inputStream = this.client.getInputStream();
    // int len = inputStream.available();
    // byte[] buffer = new byte[len];
    // logger.debug("available: {}", len);
    // int result = inputStream.read(buffer, 0, len+10);
    // logger.debug("read byte count: {}", result);
    // logger.debug("body: {}", buffer);
    // logger.debug("{}", new String(buffer, StandardCharsets.UTF_8));


    DataInputStream wrappedInputStream = new DataInputStream(new BufferedInputStream(this.client.getInputStream()));
    char c = (char)wrappedInputStream.readByte();
    logger.debug("char: {}", c);


    // OutputStream outputStream = this.client.getOutputStream();

    // this.out = new PrintWriter(this.client.getOutputStream(), true);
    // this.in = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
    // this.send("ni hao");

    return this;
  }

  private App close() {
    logger.debug("closing");

    if (this.client != null) {
      try {
        this.client.close();
      } catch (IOException error) {
        // ignore
      }
    }
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
      this.endpoint = new InetSocketAddress(address, port);
    }
    return this;
  }

  public String send(String msg) throws IOException {
    out.println(msg);
    String resp = this.in.readLine();
    return resp;
  }
}
