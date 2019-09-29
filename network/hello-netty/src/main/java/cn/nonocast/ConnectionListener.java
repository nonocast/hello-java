package cn.nonocast;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;

public class ConnectionListener implements ChannelFutureListener {
  private static final Logger logger = LoggerFactory.getLogger(ConnectionListener.class);

  private App app;

  public ConnectionListener(App app) {
    this.app = app;
  }

  @Override
  public void operationComplete(ChannelFuture future) throws Exception {
    if (!future.isSuccess()) {
      logger.debug("reconnect");
      final EventLoop loop = future.channel().eventLoop();
      loop.schedule(new Runnable() {
        @Override
        public void run() {
          app.createBootstrap(new Bootstrap(), loop);
        }
      }, 1L, TimeUnit.SECONDS);
    }
  }
}