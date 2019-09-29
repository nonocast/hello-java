package cn.nonocast;

import java.net.SocketAddress;
import java.util.List;

public abstract class RRU2889Client {
  protected SocketAddress endpoint;
  protected TagListener listener;

  public interface TagListener {
    void onTags(List<String> tags);
  }

  static public RRU2889Client create() {
    return new RRU2889ClientNettyImpl();
  }

  public void open() {
    throw new UnsupportedOperationException();
  }

  public void close() {
    throw new UnsupportedOperationException();
  }

  public SocketAddress getEndpoint() {
    return this.endpoint;
  }

  public void setEndpoint(SocketAddress endpoint) {
    this.endpoint = endpoint;
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