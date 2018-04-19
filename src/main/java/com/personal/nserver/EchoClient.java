package com.personal.nserver;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.*;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class EchoClient {

    protected final HashedWheelTimer timer = new HashedWheelTimer();
    private static final ByteBuf HEARTBEAT_SEQUENCE = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Heartbeat",
            CharsetUtil.UTF_8));
    private final ConnectorIdleStateTrigger idleStateTrigger = new ConnectorIdleStateTrigger();

    public void connect(String host,int port) throws Exception {

        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();

            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO));


            final ConnectionWatchdog watchdog = new ConnectionWatchdog(b, timer, port, host, true) {
                public ChannelHandler[] handlers() {
                    return new ChannelHandler[]{
                            this,
                            new IdleStateHandler(0, 12, 0, TimeUnit.SECONDS),
//                            new DelimiterBasedFrameDecoder(1024, Unpooled.copiedBuffer("$_".getBytes())),
                            new StringEncoder(),
                            idleStateTrigger,
                            new StringDecoder(),
                            new ClientHandler()
                    };
                }
            };

            ChannelFuture future;
            try {
                synchronized (b) {//进行连接
                    b.handler(new ChannelInitializer<Channel>() {
                        //初始化channel
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline().addLast(watchdog.handlers());
                        }
                    });
                    future = b.connect(host, port);
                }

                // 以下代码在synchronized同步块外面是安全的
                future.sync();
            } catch (Throwable t) {
                throw new Exception("connects to  fails", t);
            }

            future.channel().closeFuture().sync();
        }finally {
            group.shutdownGracefully();
        }
    }


    class ClientHandler extends ChannelInboundHandlerAdapter{

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            String message = (String) msg;
            System.out.println(message);

            ReferenceCountUtil.release(msg);
        }



        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("激活时间是：" + new Date());
            System.out.println("ClientHandler channelActive");
            ctx.writeAndFlush(new Gson().toJson(clientInfo));
            ctx.fireChannelActive();
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
            System.out.println("停止时间是：" + new Date());
            System.out.println("ClientHandler channelInactive");
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            ctx.fireExceptionCaught(cause);
        }


    }

    private String createJsonStrInfo(){
        ChannelInfo channelInfo = new ChannelInfo();
        channelInfo.setPhoneModel("safasrtertwretw54wert");
        channelInfo.setJobNember("q123");
        channelInfo.setSerialNumber("54wer5we2r1wqer54qwer2fdh1ghj74ty");
        return new Gson().toJson(channelInfo);
    }

    @ChannelHandler.Sharable
    public class ConnectorIdleStateTrigger extends ChannelInboundHandlerAdapter {
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleState state = ((IdleStateEvent) evt).state();
                if (state == IdleState.WRITER_IDLE) {
                    // write heartbeat to server
                    ctx.writeAndFlush(new Gson().toJson(clientInfo));
                }
            } else {
                super.userEventTriggered(ctx, evt);
            }
        }
    }

    @ChannelHandler.Sharable
    abstract class ConnectionWatchdog extends ChannelInboundHandlerAdapter implements TimerTask, ChannelHandlerHolder {
        private final Bootstrap bootstrap;
        private final Timer timer;
        private final int port;

        private final String host;

        private volatile boolean reconnect = true;
        private int attempts;

        public ConnectionWatchdog(Bootstrap bootstrap, Timer timer, int port, String host, boolean reconnect) {
            this.bootstrap = bootstrap;
            this.timer = timer;
            this.port = port;
            this.host = host;
            this.reconnect = reconnect;
        }

        /**
         * channel链路每次active的时候，将其连接的次数重新置0
         */
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("当前链路已经激活了，重连尝试次数重新置为0");
            attempts = 0;
            ctx.fireChannelActive();
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("链接关闭");
            if (reconnect) {
                System.out.println("链接关闭，将进行重连");
                if (attempts < 1000) {
                    attempts++;
                }  else return;         //重连的间隔时间会越来越长
                int timeout = 2 << attempts;
                timer.newTimeout(this, timeout, TimeUnit.SECONDS);
            }
            ctx.fireChannelInactive();
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            System.out.println("run watchDog");
            ChannelFuture future;
            //bootstrap已经初始化好了，只需要将handler填入就可以了
            synchronized (bootstrap) {
                bootstrap.handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(handlers());
                    }
                });
                future = bootstrap.connect(host, port);
            }
            //future对象
            future.addListener((ChannelFutureListener) f -> {
                boolean succeed = f.isSuccess();
                //如果重连失败，则调用ChannelInactive方法，再次出发重连事件，一直尝试12次，如果失败则不再重连
                if (!succeed) {
                    System.out.println("重连失败");
                    f.channel().pipeline().fireChannelInactive();
                } else {
                    System.out.println("重连成功");
                }
            });
        }
    }

    private ChannelInfo clientInfo;

    public void setClientInfo(ChannelInfo info){
        clientInfo = info;
    }


    public static void main(String[] args){
        try {
            new EchoClient().connect("127.0.0.1",8080);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("连接异常");
        }
    }
}
