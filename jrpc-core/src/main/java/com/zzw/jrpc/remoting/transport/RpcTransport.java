package com.zzw.jrpc.remoting.transport;

import com.zzw.jrpc.remoting.dto.RpcRequest;
import com.zzw.jrpc.remoting.dto.RpcResponse;

import java.util.concurrent.CompletableFuture;

public interface RpcTransport {

    CompletableFuture<RpcResponse<Object>> sendRpcRequest(RpcRequest rpcRequest);

}
