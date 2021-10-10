package com.zzw.jrpc.provider.impl;

import com.zzw.jrpc.base.enums.RpcErrorMsgEnum;
import com.zzw.jrpc.base.exception.RpcException;
import com.zzw.jrpc.config.RpcServiceConfig;
import com.zzw.jrpc.provider.ServiceProvider;
import com.zzw.jrpc.registry.ServiceRegistry;
import com.zzw.jrpc.registry.zk.ZkServiceRegistryImpl;
import com.zzw.jrpc.remoting.transport.netty.server.NettyRpcServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ZkServiceProviderImpl implements ServiceProvider {

    private final Map<String, Object> serviceMap;
    private final ServiceRegistry serviceRegistry;

    public ZkServiceProviderImpl() {
        serviceMap = new ConcurrentHashMap<>();
        serviceRegistry = new ZkServiceRegistryImpl();
    }

    @Override
    public Object getService(String rpcServiceName) {
        Object service = serviceMap.get(rpcServiceName);
        if (null == service) {
            throw new RpcException(RpcErrorMsgEnum.SERVICE_NOT_EXIST);
        }
        return service;
    }

    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            // Add Service to Provider
            String serviceName = rpcServiceConfig.getServiceName();
            if (serviceMap.containsKey(serviceName)) {
                return;
            }
            serviceMap.put(serviceName, rpcServiceConfig.getService());
            log.info("Add service: {} and interfaces:{}",
                    serviceName, rpcServiceConfig.getService().getClass().getInterfaces());
            //
            serviceRegistry.registerService(serviceName, new InetSocketAddress(host, NettyRpcServer.PORT));
        } catch (UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }
    }

    @Override
    public void removeService(String rpcServiceName) {

    }
}
