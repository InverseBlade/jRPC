package com.zzw.demo.service.impl;

import com.zzw.demo.service.EchoService;
import com.zzw.jrpc.annotation.JRpcService;
import com.zzw.jrpc.remoting.transport.netty.server.NettyRpcServer;

import java.net.InetAddress;
import java.net.UnknownHostException;

@JRpcService
public class EchoServiceImpl implements EchoService {
    @Override
    public String hello(String msg) {
        String host = "unknown";
        long pid = -1;
        try {
            host = InetAddress.getLocalHost().getHostAddress();
            pid = NettyRpcServer.PORT;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return "Echo msg from Provider[" + host + "&port=" + pid + "]: " + msg;
    }
}
