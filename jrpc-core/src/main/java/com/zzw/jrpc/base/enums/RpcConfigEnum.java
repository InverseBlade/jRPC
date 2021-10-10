package com.zzw.jrpc.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RpcConfigEnum {

    JRPC_CONFIG_PATH("jrpc.properties"),
    ZK_ADDRESS("jrpc.zookeeper.address"),
    RPC_SERVER_PORT("jrpc.server.port");

    private final String propertyValue;
}
