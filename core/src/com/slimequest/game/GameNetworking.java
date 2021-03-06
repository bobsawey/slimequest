package com.slimequest.game;

import com.slimequest.shared.GameNetworkEvent;

import java.nio.charset.Charset;
import java.util.logging.Logger;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.compression.SnappyFrameDecoder;
import io.netty.handler.codec.compression.SnappyFrameEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * Created by jacob on 9/11/16.
 */

public class GameNetworking extends Thread {
    private Channel channel;
    private boolean shutdown;

    public void close() {
        if (channel != null) {
            channel.close();
        }

        shutdown = true;
        channel = null;
        Game.networking = null;
    }

    @Override
    public void run() {
        Game.networking = this;

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        Game.connecting = true;

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                    cause.printStackTrace();

                    Game.connectionError = true;
                }

                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    Game.connectionError = false;

                    ch.pipeline()
                            .addLast(new SnappyFrameDecoder())
                            .addLast(new SnappyFrameEncoder())
                            .addLast(new StringDecoder(Charset.forName("UTF-8")))
                            .addLast(new StringEncoder(Charset.forName("UTF-8")))
                            .addLast(new ClientHandler());
                }
            });

            // Start the client.
            channel = b.connect(Game.serverAddress, Game.serverPort).sync().channel();

            Game.connecting = false;

            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Game.connectionError = true;
            workerGroup.shutdownGracefully();

        if (!shutdown) try {
                Thread.sleep(1000);

                run();
            } catch (InterruptedException e) {
                e.printStackTrace();

                run();
            }
        }
    }

    public void send(GameNetworkEvent event) {
        if (channel != null && channel.isOpen()) {
            channel.writeAndFlush(event.json());
        }
    }
}
