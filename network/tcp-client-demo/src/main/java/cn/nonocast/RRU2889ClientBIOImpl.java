package cn.nonocast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBufUtil;

public class RRU2889ClientBIOImpl extends RRU2889Client {
  private static final Logger logger = LoggerFactory.getLogger(RRU2889ClientBIOImpl.class);
  private Timer timer;
  private Socket client;
  private Thread clientReceiveThread;

  public RRU2889ClientBIOImpl() {
    timer = new Timer();
  }

  @Override
  public void open() {
    logger.debug("bio: open");
    this.setupTimer();
  }

  @Override
  public void close() {
    logger.debug("BIO: close");
    this.closeTimer();
    this.closeSocket();
  }

  private void setupSocket() throws IOException {
    logger.debug("setupSocket()");

    this.client = new Socket();
    this.client.connect(new InetSocketAddress(this.host, this.port));

    this.clientReceiveThread = new Thread(() -> {
      logger.debug("bio: thread");
      byte[] buffer = new byte[255];
      InputStream inputStream = null;

      try {
        inputStream = this.client.getInputStream();
      } catch (IOException e) {
        logger.debug(e.getMessage());
      }

      while (true) {
        try {
          int len = inputStream.read(buffer);
          if (len < 1) {
            break;
          }

          String p = ByteBufUtil.hexDump(buffer, 0, len);
          logger.info("receive len({}): {}", len, p);
          this.parsePacket(p);
        } catch (IOException e) {
          // socket closed, exit thead
          break;
        }
      }

      logger.debug("thread exited");
    });
    clientReceiveThread.start();
  }

  /**
   * 15 00 01 03 01 01 0C E2 00 00 19 59 11 01 72 14 40 93 52 48 D8 C0
   * 只需要处理status[4]为3的packet
   */
  private void parsePacket(String body) {
    List<String> tags = new ArrayList<>();
    logger.debug("body size: {}", body.length());

    if (body.length() % 44 == 0) {

      for (int i = 0; i < body.length() / 44; ++i) {
        String p = body.substring(i * 44, i * 44 + 44);
        tags.add(p.substring(14, 14 + 24));
      }
    }

    if (tags.size() > 0) {
      logger.debug("tag size: {}", tags.size());
      this.fireTagNotify(tags);
    }

  }

  private void closeSocket() {
    logger.debug("closeSocket()");
    if (this.client != null) {
      try {
        this.client.close();
      } catch (IOException e) {
        // ignore
      } finally {
        this.client = null;
      }
    }
  }

  private void setupTimer() {
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        // logger.debug("bio: ticker");
        if (RRU2889ClientBIOImpl.this.client == null) {
          try {
            RRU2889ClientBIOImpl.this.setupSocket();
          } catch (IOException e) {
            logger.debug("bio.ticker connect FAILED, close socket");
            RRU2889ClientBIOImpl.this.closeSocket();
          }
        } else {
          byte[] buf = new byte[] { (byte) 0x00, (byte) 0xff };
          try {
            RRU2889ClientBIOImpl.this.send(buf);
          } catch (IOException e) {
            logger.debug("bio.ticker send FAILED, close socket");
            RRU2889ClientBIOImpl.this.closeSocket();
          }
        }
      }
    }, 0, 2000);
  }

  private void closeTimer() {
    if (timer != null) {
      timer.cancel();
      timer.purge();
      timer = null;
    }
  }

  public void send(byte[] buf) throws IOException {
    OutputStream outputStream = this.client.getOutputStream();

    byte[] buffer = new byte[] { (byte) 0x06, (byte) 0x00, (byte) 0x01, (byte) 0x04, (byte) 0xff, (byte) 0xd4,
        (byte) 0x39 };
    outputStream.write(buffer);
  }
}