package com.zzw.jrpc.base.enums;

public enum RpcErrorMsgEnum {

    CLIENT_CONNECT_SERVER_FAILURE("客户端连接服务端失败"),
    SERVICE_INVOCATION_FAILURE("服务调用失败"),
    SERVICE_NOT_EXIST("指定服务不存在"),
    SERVICE_NOT_IMPLEMENT_ANY_INTERFACE("服务未实现任何接口"),
    REQUEST_NOT_MATCH_RESPONSE("请求和返回的响应不匹配");

    private final String msg;

    RpcErrorMsgEnum(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
