package com.zzw.jrpc.base.utils;

public class Tools {

    public static String genRpcServiceName(String interfaceName, String version, String group) {
        return interfaceName + group + version;
    }

}
