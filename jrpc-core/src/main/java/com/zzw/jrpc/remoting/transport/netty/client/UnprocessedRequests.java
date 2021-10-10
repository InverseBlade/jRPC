package com.zzw.jrpc.remoting.transport.netty.client;

import com.zzw.jrpc.remoting.dto.RpcResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class UnprocessedRequests {

    private static final Map<String, CompletableFuture<RpcResponse<Object>>> table = new ConcurrentHashMap<>();

    public void set(String requestId, CompletableFuture<RpcResponse<Object>> future) {
        table.put(requestId, future);
    }

    public void complete(RpcResponse<Object> rpcResponse) {
        CompletableFuture<RpcResponse<Object>> future = table.remove(rpcResponse.getRequestId());
        if (future == null) {
            throw new IllegalStateException();
        } else {
            future.complete(rpcResponse);
        }
    }

}
