package com.zzw.jrpc.remoting.transport.netty.client;

import com.zzw.jrpc.base.factory.SingletonFactory;
import com.zzw.jrpc.registry.ServiceDiscovery;
import com.zzw.jrpc.registry.zk.ZkServiceDiscoveryImpl;
import com.zzw.jrpc.remoting.constants.RpcConstants;
import com.zzw.jrpc.remoting.dto.RpcMessage;
import com.zzw.jrpc.remoting.dto.RpcRequest;
import com.zzw.jrpc.remoting.dto.RpcResponse;
import com.zzw.jrpc.remoting.transport.RpcTransport;
import com.zzw.jrpc.remoting.transport.netty.codec.RpcMessageDecoder;
import com.zzw.jrpc.remoting.transport.netty.codec.RpcMessageEncoder;
import com.zzw.jrpc.serialize.DefaultSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

@Slf4j
public class NettyRpcClient implements RpcTransport {

    private final Map<String, Channel> channelMap = new ConcurrentHashMap<>();//<InetAddr, Channel>

    private final ServiceDiscovery serviceDiscovery;
    private final UnprocessedRequests unprocessedRequests;
    private final Bootstrap bootstrap;

    @SneakyThrows
    @Override
    public CompletableFuture<RpcResponse<Object>> sendRpcRequest(RpcRequest rpcRequest) {
        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();
        InetSocketAddress addr = serviceDiscovery.lookupService(rpcRequest);
        Channel channel = getChannel(addr);

        if (channel != null && channel.isActive()) {
            unprocessedRequests.set(rpcRequest.getRequestId(), resultFuture);

            RpcMessage rpcMsg = new RpcMessage();
            rpcMsg.setMessageType(RpcConstants.REQUEST_TYPE);
            rpcMsg.setData(rpcRequest);

            log.info("Size of RequestRpcMsg=[{}] KB",
                    new DefaultSerializer().serialize(rpcMsg).length * 1.0 / 1024);

            channel.writeAndFlush(rpcMsg).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("client send message: [{}]", rpcMsg);
                } else {
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    unprocessedRequests.remove(rpcRequest.getRequestId());
                    log.error("Client Send Fail: ", future.cause());
                }
            });
        } else {
            resultFuture.completeExceptionally(new IllegalStateException());
            throw new IllegalStateException();
        }
        return resultFuture;
    }

    public Channel getChannel(InetSocketAddress addr) {
        String key = addr.toString();
        Channel channel = null;

        if (channelMap.containsKey(key)) {
            channel = channelMap.get(key);
            if (channel == null || !channel.isActive() || !channel.isOpen()) {
                channelMap.remove(key);
                channel = null;
            }
        }
        // do connect
        channel = channelMap.computeIfAbsent(key, new Function<String, Channel>() {
            @Override
            public Channel apply(String s) {
                return doConnect(addr);
            }
        });
        //
        return channel;
    }

    @SneakyThrows
    public Channel doConnect(InetSocketAddress addr) {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(addr).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                completableFuture.complete(future.channel());
            } else {
                throw new IllegalStateException();
            }
        });
        try {
            return completableFuture.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.error("Connect to server [{}] timeout!", addr.getAddress().getHostAddress());
        }
        return null;
    }

    public NettyRpcClient() {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pip = ch.pipeline();
                        pip.addLast(new IdleStateHandler(0, 20, 0, TimeUnit.SECONDS))
                                .addLast(new RpcMessageEncoder())
                                .addLast(new RpcMessageDecoder())
                                .addLast(new NettyRpcClientHandler());
                    }
                });
        serviceDiscovery = SingletonFactory.getInstance(ZkServiceDiscoveryImpl.class);
        unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
    }
}
