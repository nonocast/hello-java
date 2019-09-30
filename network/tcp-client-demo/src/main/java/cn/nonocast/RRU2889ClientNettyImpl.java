package cn.nonocast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

public class RRU2889ClientNettyImpl extends RRU2889Client {
  private static final Logger logger = LoggerFactory.getLogger(RRU2889ClientNettyImpl.class);
  private EventLoopGroup group;

  class AppClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private RRU2889ClientNettyImpl app;
    private List<String> cache;

    public AppClientHandler(RRU2889ClientNettyImpl app) {
      this.app = app;
      this.cache = new ArrayList<>();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
      String body = ByteBufUtil.hexDump(in);
      logger.debug("receive({}): {}", in.readableBytes(), body);

      if (in.readableBytes() == 21) {
        String tag = body.substring(12, 12 + 24);
        this.cache.add(tag);
      }

      if (in.readableBytes() == 7 && this.cache.size() > 0) {
        this.app.fireTagNotify(this.cache);
        this.cache.clear();
      }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
      if (evt instanceof IdleStateEvent) {
        IdleStateEvent idleState = (IdleStateEvent) evt;
        if (idleState.state() == IdleState.ALL_IDLE) {
          ByteBuf ping = Unpooled.copiedBuffer(
              new byte[] { (byte) 0x06, (byte) 0x00, (byte) 0x01, (byte) 0x04, (byte) 0xff, (byte) 0xd4, (byte) 0x39 });
          ctx.writeAndFlush(ping);
        }
      }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
      final EventLoop eventLoop = ctx.channel().eventLoop();
      eventLoop.schedule(new Runnable() {
        @Override
        public void run() {
          AppClientHandler.this.app.createBootstrap(new Bootstrap(), eventLoop);
        }
      }, 1L, TimeUnit.SECONDS);
      super.channelInactive(ctx);
    }
  }

  public class ConnectionListener implements ChannelFutureListener {
    private RRU2889ClientNettyImpl app;

    public ConnectionListener(RRU2889ClientNettyImpl app) {
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

  @Override
  public void open() {
    this.group = new NioEventLoopGroup();
    this.createBootstrap(new Bootstrap(), group);
  }

  @Override
  public void close() {
    if (this.group != null) {
      try {
        this.group.shutdownGracefully().sync();
      } catch (Exception e) {
        // ignore
      }
    }
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
          ch.pipeline().addLast(new AppClientHandler(RRU2889ClientNettyImpl.this));
        }
      });
      bootstrap.remoteAddress(this.host, this.port);
      bootstrap.connect().addListener(new ConnectionListener(RRU2889ClientNettyImpl.this));
    }
    return bootstrap;
  }

}