package com.zzw.jrpc.remoting.transport.netty.codec;

import com.zzw.jrpc.remoting.dto.RpcMessage;
import com.zzw.jrpc.serialize.DefaultSerializer;
import com.zzw.jrpc.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {

    private static final Serializer serializer = new DefaultSerializer();

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage rpcMessage, ByteBuf out) throws Exception {
        try {
            byte[] bytes = serializer.serialize(rpcMessage);
            out.writeBytes(bytes);
        } catch (Exception e) {
            log.error("Encode request error! ", e);
        }
    }
}
