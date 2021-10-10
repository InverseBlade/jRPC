package com.zzw.jrpc.remoting.transport.netty.server;

import com.zzw.jrpc.base.enums.RpcConfigEnum;
import com.zzw.jrpc.base.factory.SingletonFactory;
import com.zzw.jrpc.base.utils.PropertiesFileUtil;
import com.zzw.jrpc.base.utils.RuntimeUtil;
import com.zzw.jrpc.config.CustomShutdownHook;
import com.zzw.jrpc.config.RpcServiceConfig;
import com.zzw.jrpc.provider.ServiceProvider;
import com.zzw.jrpc.provider.impl.ZkServiceProviderImpl;
import com.zzw.jrpc.remoting.transport.netty.codec.RpcMessageDecoder;
import com.zzw.jrpc.remoting.transport.netty.codec.RpcMessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyRpcServer {

    public static final int PORT;

    private final ServiceProvider serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);

    public void registerService(RpcServiceConfig serviceConfig) {
        serviceProvider.publishService(serviceConfig);
    }

    static {
        Properties config = PropertiesFileUtil.readPropertiesFile(RpcConfigEnum.JRPC_CONFIG_PATH.getPropertyValue());
        String portStr = config.getProperty(RpcConfigEnum.RPC_SERVER_PORT.getPropertyValue(), "9995");
        PORT = Integer.parseInt(portStr);
    }

    public void startAsync() {
        new Thread(this::startServer).start();
    }

    @SneakyThrows
    public void startServer() {
        log.info("Starting Netty RPC Server...");
        CustomShutdownHook.getInstance().clearAll();
        String host = InetAddress.getLocalHost().getHostAddress();
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(
                RuntimeUtil.cpus() * 2
        );
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline p = socketChannel.pipeline();
                            p.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS))
                                    .addLast(new RpcMessageDecoder())
                                    .addLast(new RpcMessageEncoder())
                                    .addLast(serviceHandlerGroup, new NettyServerHandler());
                        }
                    });
            ChannelFuture future = bootstrap.bind(host, PORT).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("occur exception when start server: ", e);
        } finally {
            log.error("shutdown bossGroup ans workerGroup");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            serviceHandlerGroup.shutdownGracefully();
        }
    }


}











