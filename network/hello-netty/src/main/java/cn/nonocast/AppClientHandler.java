package cn.nonocast;

import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;

@Sharable
public class AppClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
  private static final Logger logger = LoggerFactory.getLogger(AppClientHandler.class);
  private App app;

  public AppClientHandler(App app) {
    this.app = app;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) {

  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
    String body = ByteBufUtil.hexDump(in);
    logger.debug("receive({}): {}", in.readableBytes(), body);

    if (in.readableBytes() == 21) {
      logger.debug(">>> tag: {}", body.substring(12, 12 + 24));
    }
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if (evt instanceof IdleStateEvent) {
      IdleStateEvent idleState = (IdleStateEvent) evt;
      if (idleState.state() == IdleState.ALL_IDLE) {
        ByteBuf ping = Unpooled.copiedBuffer(new byte[] { (byte) 0x04, (byte) 0xff, (byte) 0x21, (byte) 0x19, (byte) 0x95 });
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