package cn.nonocast;

import java.net.SocketAddress;
import java.util.List;

public abstract class RRU2889Client {
  protected String host;
  protected int port;
  protected TagListener listener;

  public interface TagListener {
    void onTags(List<String> tags);
  }

  static public RRU2889Client create(String type) {
    RRU2889Client result = null;

    switch (type.toLowerCase()) {
    case "bio": {
      result = new RRU2889ClientBIOImpl();
      break;
    }
    case "netty": {
      result = new RRU2889ClientNettyImpl();
      break;
    }
    default:
      break;
    }
    return result;
  }

  public abstract void open();

  public abstract void close();

  public String getHost() {
    return this.host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return this.port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public TagListener getTagListener() {
    return this.listener;
  }

  public RRU2889Client setTagListener(TagListener listener) {
    this.listener = listener;
    return this;
  }

  public RRU2889Client removeListener() {
    this.listener = null;
    return this;
  }

  protected void fireTagNotify(List<String> tags) {
    if (this.listener != null) {
      this.listener.onTags(tags);
    }
  }
}