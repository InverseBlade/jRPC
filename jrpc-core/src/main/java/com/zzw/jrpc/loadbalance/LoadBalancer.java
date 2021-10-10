package com.zzw.jrpc.loadbalance;

import com.zzw.jrpc.remoting.dto.RpcRequest;

import java.util.List;

public interface LoadBalancer {

    String selectServiceNode(List<String> nodeList, RpcRequest rpcRequest);

    void updateNodeList(List<String> updateList);

}
