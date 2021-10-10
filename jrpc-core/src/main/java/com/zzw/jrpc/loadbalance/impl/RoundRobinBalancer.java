package com.zzw.jrpc.loadbalance.impl;

import com.zzw.jrpc.loadbalance.AbstractLoadBalancer;
import com.zzw.jrpc.remoting.dto.RpcRequest;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinBalancer extends AbstractLoadBalancer {

    private final List<String> nodeList = new CopyOnWriteArrayList<>();
    private final AtomicInteger cur = new AtomicInteger(0);

    public void updateNodeList(List<String> updateList) {
        nodeList.clear();
        nodeList.addAll(updateList);
    }

    @Override
    protected String doSelect(List<String> list, RpcRequest rpcRequest) {
        int index = cur.getAndIncrement() % nodeList.size();
        checkBound();
        return nodeList.get(index);
    }

    private void checkBound() {
        int oldVal = cur.get();
        if (oldVal < nodeList.size()) return;
        do {
            oldVal = cur.get();
        } while (!cur.compareAndSet(oldVal, oldVal % nodeList.size()));
    }

    public RoundRobinBalancer() {
    }

    public RoundRobinBalancer(List<String> nodeList) {
        this.nodeList.addAll(nodeList);
    }
}
