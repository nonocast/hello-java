package cn.nonocast;

import java.io.UnsupportedEncodingException;
import java.net.SocketAddress;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

public class RRU2889ClientNettyImpl extends RRU2889Client {
  private static final Logger logger = LoggerFactory.getLogger(RRU2889Client.class);

  private Channel channel;
  private Timer timer;

  class Adapter extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
      ByteBuf buf = (ByteBuf) msg;
      logger.debug("{}", buf.toString());
      logger.debug("{}", ByteBufUtil.hexDump(buf));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
      cause.printStackTrace();
      ctx.close();
    }
  }

  public void open() {
    // 创建基于Reactor的NIO线程组
    EventLoopGroup workerGroup = new NioEventLoopGroup();

    try {
      Bootstrap b = (new Bootstrap().group(workerGroup).channel(NioSocketChannel.class)
          .option(ChannelOption.SO_KEEPALIVE, true).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
              ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
              ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(255, 0, 1, 0, 1));
              ch.pipeline().addLast(new Adapter());
            }
          }));

      // Start the client.
      // sync表示转为同步，可以理解为await
      ChannelFuture f = b.connect(this.endpoint).sync();
      this.channel = f.channel();

      setupTimer();

      // Wait until the connection is closed.
      this.channel.closeFuture().sync();
    } catch (InterruptedException e) {
      logger.debug(e.getMessage());
    } finally {
      logger.debug("shutdownGracefully");
      workerGroup.shutdownGracefully();
    }
  }

  private void setupTimer() {
    this.timer = new HashedWheelTimer();
    this.timer.newTimeout(new TimerTask() {
      @Override
      public void run(Timeout timeout) throws Exception {
        logger.debug(">>>");
        RRU2889ClientNettyImpl.this
            .send(new byte[] { (byte) 0x04, (byte) 0xff, (byte) 0x21, (byte) 0x19, (byte) 0x95 });
        RRU2889ClientNettyImpl.this.timer.newTimeout(this, 2, TimeUnit.SECONDS);
      }
    }, 2, TimeUnit.SECONDS);
  }

  private void closeTimer() {
    if (this.timer != null) {
      this.timer.stop();
      this.timer = null;
    }
  }

  public void send(byte[] data) {
    ByteBuf firstMessage = Unpooled.buffer();
    firstMessage.writeBytes(data);
    this.channel.writeAndFlush(firstMessage);
  }

}