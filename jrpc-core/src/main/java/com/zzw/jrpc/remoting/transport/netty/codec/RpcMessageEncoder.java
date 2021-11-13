package com.zzw.jrpc.remoting.transport.netty.codec;

import com.zzw.jrpc.remoting.constants.RpcConstants;
import com.zzw.jrpc.remoting.dto.RpcMessage;
import com.zzw.jrpc.serialize.ProtostuffSerializer;
import com.zzw.jrpc.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/*
Protocol Form:
magic(4B), type(1B), len(4B), req_id(4B)
body(?B)
 */
@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {

    private final static AtomicInteger idGenerator = new AtomicInteger(0);

    private static final Serializer serializer = new ProtostuffSerializer();

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage rpcMessage, ByteBuf out) throws Exception {
        try {
            out.writeBytes(RpcConstants.MAGIC_NUMBER);
            out.writeByte(rpcMessage.getMessageType());
            out.writerIndex(out.writerIndex() + 4);
            out.writeInt(idGenerator.getAndIncrement());

            int fullLength = RpcConstants.HEADER_LENGTH;
            byte[] bytes = null;
            if (rpcMessage.getMessageType() != RpcConstants.HEARTBEAT_REQUEST_TYPE &&
                    rpcMessage.getMessageType() != RpcConstants.HEARTBEAT_RESPONSE_TYPE &&
                    rpcMessage.getData() != null) {
                bytes = serializer.serialize(rpcMessage.getData());
                fullLength += bytes.length;
            }
            if (bytes != null) {
                out.writeBytes(bytes);
            }
            int index = out.writerIndex();
            out.writerIndex(index - fullLength + RpcConstants.MAGIC_NUMBER.length + 1);
            out.writeInt(fullLength);
            out.writerIndex(index);
        } catch (Exception e) {
            log.error("Encode request error! ", e);
            throw e;
        }
    }
}
