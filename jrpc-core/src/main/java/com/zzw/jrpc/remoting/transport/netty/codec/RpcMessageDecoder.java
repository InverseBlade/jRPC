package com.zzw.jrpc.remoting.transport.netty.codec;

import com.zzw.jrpc.remoting.dto.RpcMessage;
import com.zzw.jrpc.serialize.DefaultSerializer;
import com.zzw.jrpc.serialize.ProtostuffSerializer;
import com.zzw.jrpc.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class RpcMessageDecoder extends ByteToMessageDecoder {

    private static final Serializer serializer = new ProtostuffSerializer();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf out, List<Object> list) throws Exception {
        byte[] bytes = new byte[out.readableBytes()];
        out.readBytes(bytes);
        RpcMessage rpcMessage = serializer.deSerialize(bytes, RpcMessage.class);
        list.add(rpcMessage);
    }
}
