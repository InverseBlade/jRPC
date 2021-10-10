package com.zzw.jrpc.loadbalance;

import com.zzw.jrpc.remoting.dto.RpcRequest;

import java.util.List;

public abstract class AbstractLoadBalancer implements LoadBalancer {

    @Override
    public String selectServiceNode(List<String> nodeList, RpcRequest rpcRequest) {
        if (nodeList == null || nodeList.size() == 0) {
            return null;
        }
        if (nodeList.size() == 1) {
            return nodeList.get(0);
        }
        return doSelect(nodeList, rpcRequest);
    }

    protected abstract String doSelect(List<String> nodeList, RpcRequest rpcRequest);
}
