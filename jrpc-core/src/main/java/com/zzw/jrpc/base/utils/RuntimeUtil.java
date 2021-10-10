package com.zzw.jrpc.base.utils;

public class RuntimeUtil {

    public static int cpus() {
        return Runtime.getRuntime().availableProcessors();
    }

}
