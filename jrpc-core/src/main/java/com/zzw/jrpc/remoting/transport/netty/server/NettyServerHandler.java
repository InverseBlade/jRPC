package com.zzw.jrpc.remoting.transport.netty.server;

import com.zzw.jrpc.base.enums.RpcResponseCodeEnum;
import com.zzw.jrpc.base.factory.SingletonFactory;
import com.zzw.jrpc.remoting.constants.RpcConstants;
import com.zzw.jrpc.remoting.dto.RpcMessage;
import com.zzw.jrpc.remoting.dto.RpcRequest;
import com.zzw.jrpc.remoting.dto.RpcResponse;
import com.zzw.jrpc.remoting.handler.RpcRequestHandler;
import com.zzw.jrpc.serialize.ProtostuffSerializer;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcMessage> {

    private final RpcRequestHandler rpcRequestHandler;

    public NettyServerHandler() {
        rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage rpcMessage) throws Exception {
        log.info("server receive msg: [{}] ", rpcMessage);
        byte msgType = rpcMessage.getMessageType();
        RpcMessage responseRpcMsg = new RpcMessage();

        if (msgType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
            responseRpcMsg.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
            responseRpcMsg.setData(RpcConstants.PONG);
        } else {
            RpcRequest rpcRequest = (RpcRequest) rpcMessage.getData();
            // Invoke Target Service Method
            Object result = rpcRequestHandler.handle(rpcRequest);
            log.info("server get result: [{}]", result);
            responseRpcMsg.setMessageType(RpcConstants.RESPONSE_TYPE);
            if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
                responseRpcMsg.setData(rpcResponse);
            } else {
                RpcResponse<Object> rpcResponse = RpcResponse.fail(RpcResponseCodeEnum.FAIL);
                responseRpcMsg.setData(rpcResponse);
                log.error("not writeable now, message has been dropped.");
            }
        }
        log.info("Size of ResponseRpcMsg=[{}] KB", new ProtostuffSerializer().serialize(responseRpcMsg).length * 1.0 / 1024);
        ctx.writeAndFlush(responseRpcMsg).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("idle check happen, so close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
