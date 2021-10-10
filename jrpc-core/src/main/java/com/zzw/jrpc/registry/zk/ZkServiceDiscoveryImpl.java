package com.zzw.jrpc.registry.zk;

import com.zzw.jrpc.base.enums.RpcErrorMsgEnum;
import com.zzw.jrpc.loadbalance.LoadBalancer;
import com.zzw.jrpc.loadbalance.impl.RoundRobinBalancer;
import com.zzw.jrpc.registry.ServiceDiscovery;
import com.zzw.jrpc.registry.zk.util.CuratorUtils;
import com.zzw.jrpc.remoting.dto.RpcRequest;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;

public class ZkServiceDiscoveryImpl implements ServiceDiscovery {

    private final LoadBalancer loadBalancer;

    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        String rpcServiceName = rpcRequest.getRpcServiceName();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> nodeList = CuratorUtils.getChildNodes(zkClient, rpcServiceName, loadBalancer);
        String targetNode = loadBalancer.selectServiceNode(nodeList, rpcRequest);
        if (targetNode == null) {
            throw new RuntimeException(RpcErrorMsgEnum.SERVICE_NOT_EXIST.getMsg());
        }
        String[] ipAndPortStr = targetNode.split("[:]");
        String IP = ipAndPortStr[0];
        int port = Integer.parseInt(ipAndPortStr[1]);
        return new InetSocketAddress(IP, port);
    }

    public ZkServiceDiscoveryImpl() {
        loadBalancer = new RoundRobinBalancer();
    }
}
