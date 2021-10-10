package com.zzw.jrpc.remoting.constants;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class RpcConstants {

    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    public static final byte REQUEST_TYPE = 0x1;
    public static final byte RESPONSE_TYPE = 0x2;

    public static final byte HEARTBEAT_REQUEST_TYPE = 0x3;
    public static final byte HEARTBEAT_RESPONSE_TYPE = 0x4;
    public static final String PING = "ping";
    public static final String PONG = "pong";

}
