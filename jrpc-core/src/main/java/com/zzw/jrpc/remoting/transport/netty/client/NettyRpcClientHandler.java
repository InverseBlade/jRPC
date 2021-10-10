package com.zzw.jrpc.remoting.transport.netty.client;

import com.zzw.jrpc.base.factory.SingletonFactory;
import com.zzw.jrpc.remoting.constants.RpcConstants;
import com.zzw.jrpc.remoting.dto.RpcMessage;
import com.zzw.jrpc.remoting.dto.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class NettyRpcClientHandler extends SimpleChannelInboundHandler<RpcMessage> {

    private final NettyRpcClient nettyRpcClient;
    private final UnprocessedRequests unprocessedRequests;

    public NettyRpcClientHandler() {
        nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
        unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
    }

    @Override
    @SneakyThrows
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcMessage rpcMessage) throws Exception {
        log.info("client receive msg [{}]", rpcMessage);
        if (rpcMessage.getMessageType() == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
            log.info("heart [{}]", rpcMessage.getData());
        } else {
            RpcResponse<Object> rpcResponse = (RpcResponse<Object>) rpcMessage.getData();
            unprocessedRequests.complete(rpcResponse);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state.equals(IdleState.WRITER_IDLE)) {
                log.info("write idle happen [{}]", ctx.channel().remoteAddress());
                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                RpcMessage rpcMsg = new RpcMessage();
                rpcMsg.setMessageType(RpcConstants.HEARTBEAT_REQUEST_TYPE);
                rpcMsg.setData(RpcConstants.PING);
                channel.writeAndFlush(rpcMsg).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("client catch exception: ", cause);
        cause.printStackTrace();
        ctx.close();
    }
}
