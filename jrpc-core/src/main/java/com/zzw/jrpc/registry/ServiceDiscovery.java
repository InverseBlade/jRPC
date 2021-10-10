package com.zzw.jrpc.registry;

import com.zzw.jrpc.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;

public interface ServiceDiscovery {

    InetSocketAddress lookupService(RpcRequest rpcRequest);

}
