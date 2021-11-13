package com.zzw.jrpc.remoting.transport.netty.codec;

import com.zzw.jrpc.remoting.constants.RpcConstants;
import com.zzw.jrpc.remoting.dto.RpcMessage;
import com.zzw.jrpc.remoting.dto.RpcRequest;
import com.zzw.jrpc.remoting.dto.RpcResponse;
import com.zzw.jrpc.serialize.ProtostuffSerializer;
import com.zzw.jrpc.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

/*
Protocol Form:
magic(4B), type(1B), len(4B), req_id(4B)
body(?B)
 */

@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {

    private static final Serializer serializer = new ProtostuffSerializer();

    public RpcMessageDecoder() {
        super(Integer.MAX_VALUE, RpcConstants.MAGIC_NUMBER.length + 1,
                4, -9, 0);
    }

    private RpcMessage decodeFrame(ByteBuf in) {
        checkMagicNumber(in);

        RpcMessage rpcMessage = new RpcMessage();
        rpcMessage.setMessageType(in.readByte());
        int fullLength = in.readInt();
        rpcMessage.setRequestId(in.readInt());

        if (rpcMessage.getMessageType() == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
            rpcMessage.setData(RpcConstants.PONG);
            return rpcMessage;
        }
        if (rpcMessage.getMessageType() == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
            rpcMessage.setData(RpcConstants.PING);
            return rpcMessage;
        }
        int bodyLength = fullLength - RpcConstants.HEADER_LENGTH;

        if (bodyLength > 0) {
            byte[] bytes = new byte[bodyLength];
            in.readBytes(bytes);
            if (rpcMessage.getMessageType() == RpcConstants.REQUEST_TYPE) {
                rpcMessage.setData(serializer.deSerialize(bytes, RpcRequest.class));
            } else if (rpcMessage.getMessageType() == RpcConstants.RESPONSE_TYPE) {
                rpcMessage.setData(serializer.deSerialize(bytes, RpcResponse.class));
            }
        }
        return rpcMessage;
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        if (decoded instanceof ByteBuf) {
            ByteBuf frame = (ByteBuf) decoded;
            if (frame.readableBytes() >= RpcConstants.HEADER_LENGTH) {
                try {
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("Decode Frame Error: ", e);
                    throw e;
                } finally {
                    frame.release();
                }
            }
        }
        return decoded;
    }

    private void checkMagicNumber(ByteBuf in) {
        byte[] magic = new byte[RpcConstants.MAGIC_NUMBER.length];
        in.readBytes(magic);
        for (int i = 0; i < RpcConstants.MAGIC_NUMBER.length; i++) {
            if (magic[i] != RpcConstants.MAGIC_NUMBER[i]) {
                throw new IllegalStateException("Decode Frame Error: Illegal Magic Number!");
            }
        }
    }
}


















