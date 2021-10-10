package com.zzw.jrpc.registry.zk;

import com.zzw.jrpc.registry.ServiceRegistry;
import com.zzw.jrpc.registry.zk.util.CuratorUtils;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;

public class ZkServiceRegistryImpl implements ServiceRegistry {
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress addr) {
        CuratorFramework zkClient = CuratorUtils.getZkClient();
//        System.out.format("toString: %s\n HostString:%s\n HostName:%s\n", addr, addr.getHostName(), addr.getHostString());
//        System.out.format("%s\n", addr.getAddress().getHostAddress());
        String hostAndPort = addr.getAddress().getHostAddress() + ":" + addr.getPort();
        System.out.println("hostAndPort: " + hostAndPort);
        String serviceNodePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + "/" + hostAndPort;
        CuratorUtils.createTemporaryNode(zkClient, serviceNodePath);
    }
}
