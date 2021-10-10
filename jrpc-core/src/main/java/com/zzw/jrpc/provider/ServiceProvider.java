package com.zzw.jrpc.provider;

import com.zzw.jrpc.config.RpcServiceConfig;

public interface ServiceProvider {

    Object getService(String rpcServiceName);

    void publishService(RpcServiceConfig rpcServiceConfig);

    void removeService(String rpcServiceName);
}
