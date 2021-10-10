package com.zzw.jrpc.loadbalance.impl;

import com.zzw.jrpc.loadbalance.AbstractLoadBalancer;
import com.zzw.jrpc.remoting.dto.RpcRequest;

import java.util.List;
import java.util.Random;

public class RandomLoadBalancer extends AbstractLoadBalancer {
    @Override
    protected String doSelect(List<String> nodeList, RpcRequest rpcRequest) {
        Random random = new Random(System.currentTimeMillis());
        int size = nodeList.size();
        int index = random.nextInt(size * 2) % size;
        return nodeList.get(index);
    }

    @Override
    public void updateNodeList(List<String> updateList) {

    }
}
