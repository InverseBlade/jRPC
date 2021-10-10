package com.zzw.jrpc.registry;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public interface ServiceRegistry {

    public void registerService(String rpcServiceName, InetSocketAddress addr);

}
