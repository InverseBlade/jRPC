package com.zzw.jrpc.base.exception;

import com.zzw.jrpc.base.enums.RpcErrorMsgEnum;

public class RpcException extends RuntimeException {
    public RpcException(RpcErrorMsgEnum msgEnum, String detail) {
        super(msgEnum.getMsg() + ":" + detail);
    }

    public RpcException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public RpcException(RpcErrorMsgEnum msgEnum) {
        super(msgEnum.getMsg());
    }
}
