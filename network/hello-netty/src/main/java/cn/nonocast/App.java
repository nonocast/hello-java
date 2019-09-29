package cn.nonocast;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

public class App {
  private static final Logger logger = LoggerFactory.getLogger(App.class);
  private EventLoopGroup group;
  private String host;
  private int port;

  public App() {
    this.host = "127.0.0.1";
    this.port = 12009;
  }

  private App open() throws Exception {
    this.group = new NioEventLoopGroup();
    this.createBootstrap(new Bootstrap(), group);
    return this;
  }

  public Bootstrap createBootstrap(Bootstrap bootstrap, EventLoopGroup group) {
    if (bootstrap != null) {
      bootstrap.group(group);
      bootstrap.channel(NioSocketChannel.class);
      bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
      bootstrap.handler(new ChannelInitializer<SocketChannel>() {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
          ch.pipeline().addFirst(new IdleStateHandler(0, 0, 2, TimeUnit.SECONDS));
          ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(255, 0, 1, 0, 1));
          ch.pipeline().addLast(new AppClientHandler(App.this));
        }
      });
      bootstrap.remoteAddress(this.host, this.port);
      bootstrap.connect().addListener(new ConnectionListener(App.this));
    }
    return bootstrap;
  }

  public void close() {
    if (this.group != null) {
      try {
        this.group.shutdownGracefully().sync();
      } catch (Exception e) {
        // ignore
      }
    }
  }

  public App hold() throws IOException {
    new InputStreamReader(System.in).read();
    return this;
  }

  public static void main(String[] args) throws Exception {
    new App().open().hold().close();
  }
}
