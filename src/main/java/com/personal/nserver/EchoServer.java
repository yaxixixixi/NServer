package com.personal.nserver;

import com.google.gson.Gson;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EchoServer {
    public static final List<ChannelInfo> channels = new ArrayList<>();
    private final AcceptorIdleStateTrigger idleStateTrigger = new AcceptorIdleStateTrigger();
    private OnNewEventListener l;
    private long readTimeout;

    public static void main(String[] args) {
        new EchoServer().startEchoServer(8080);
    }

    public void startEchoServer(int port) {
        try {
            bind(port);
        } catch (Exception e) {
            e.printStackTrace();
            String msg = "socket server init failure!";
            System.out.println(msg);
            onServerFilure(e, msg);
        }
    }

    public void bind(int port) throws Exception {

        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .localAddress(new InetSocketAddress(port))
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChildSocketChannel())
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture f = b.bind(port).sync();

            f.addListener(fu -> {
                if (fu.isSuccess()) {
                    onServerSuccess("server init:server create successful.");
                } else {
                    onServerFilure(null, "server init:server create failure .");
                }
            });

            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
            onServerFilure(null, "shutdown:server destroyed.");
        }
    }

    private void onServerFilure(Exception e, String errorMsg) {
        if (l != null)
            l.onServerFilure(e, errorMsg);
    }

    private void onServerSuccess(String msg) {
        if (l != null)
            l.onServerSuccess(msg);
    }

    public void setCallback(OnNewEventListener l) {
        this.l = l;
    }

    public void setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
    }

    private void onMessage(ChannelHandlerContext ctx,String msg) {
        if (l != null)
            l.onMessage(ctx,msg);
    }

    private void onChannelError(Exception e, String errorMsg) {
        if (l != null)
            l.onChannelError(e, errorMsg);
    }


    private void onChannelsChanged(ChannelInfo info,Operate operate) {
        if (l != null) {
            l.onChannelsChanged(info,operate);
        }
    }

    /**
     * 当一个通道的心跳超时，删除保存的通道，断开该通道的连接，通知ui更新连接中的通道信息
     *
     * @param ctx
     */
    private void onChannelReadTimeOut(ChannelHandlerContext ctx) {
        // TODO: 2018/4/17
        //remove channelInfo from channels
        channels.forEach(channelInfo -> {
            if (channelInfo.getCtx() == ctx) {
                onMessage(ctx,"read timeout");
                channels.remove(channelInfo);
            }
        });

        //update ui  close -> inactive ->onChannelsChanged

    }

    public class ChildSocketChannel extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
//            ch.pipeline().addLast(new LineBasedFrameDecoder(1024));//基于\n进行拆解
//            ByteBuf delimiter = Unpooled.copiedBuffer("$_".getBytes());
//            ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1024,delimiter));//基于特殊字符进行拆解
            //1024 maxFrameLength 表示单条消息最大长度，如果达到长度后还没有找到分隔符，抛出TooLongFrameException
            ch.pipeline().addLast(new IdleStateHandler(readTimeout, 0, 0, TimeUnit.SECONDS));
//            DelimiterBasedFrameDecoder decoder = new DelimiterBasedFrameDecoder(1024, Unpooled.copiedBuffer("$_".getBytes()));
//            ch.pipeline().addLast(decoder);
            ch.pipeline().addLast(idleStateTrigger);
            ch.pipeline().addLast(new StringDecoder());
            ch.pipeline().addLast(new StringEncoder());
            ch.pipeline().addLast(new ServerHandler());
        }
    }

    class ServerHandler extends ChannelInboundHandlerAdapter {
        private ChannelInfo lInfo;

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            ChannelInfo channelInfo = new ChannelInfo();
            channelInfo.setCtx(ctx);
            lInfo = channelInfo;//save an instance
            channels.add(channelInfo);
            onChannelsChanged(channelInfo,Operate.ADD);
            System.out.println("one Client which ... has established .");
            onMessage(ctx, ":one Client has established .");

        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
            channels.forEach(info -> {
                if (info.getCtx() == ctx) {
                    onChannelsChanged(info,Operate.REMOVE);
                }
            });
            channels.removeIf(info ->info.getCtx() == ctx);
            lInfo = null;

            onMessage(ctx,":Client got disconnected.");
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            String req = (String) msg;

            if (req.startsWith("{")) {
                ChannelInfo json = new Gson().fromJson(req, ChannelInfo.class);
                json.setCtx(ctx);
                if (!json.equals(lInfo)){
                    channels.forEach(info -> {
                        if (info.getCtx() == ctx) {
                            info.setJobNember(json.getJobNember());
                            info.setPhoneModel(json.getPhoneModel());
                            info.setSerialNumber(json.getSerialNumber());
                            System.out.println("gengxin ui*********************");
                            onChannelsChanged(info,Operate.UPDATE);
                        }
                    });
                }

            }
            onMessage(ctx, req);

        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            onMessage(ctx,cause.toString());
            ctx.close();
        }

    }

    @ChannelHandler.Sharable
    public class AcceptorIdleStateTrigger extends ChannelInboundHandlerAdapter {

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleState state = ((IdleStateEvent) evt).state();
                if (state == IdleState.READER_IDLE) {
                    System.out.println("read timeout...");
                    onChannelReadTimeOut(ctx);
                    onMessage(ctx,"read timeout...");
                    //break that channel
                    ctx.close();
//                    throw new Exception("read timeout...");
                }
            } else {
                super.userEventTriggered(ctx, evt);
            }
        }
    }
}
