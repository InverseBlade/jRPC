package com.zzw.jrpc.remoting.dto;

import com.zzw.jrpc.base.enums.RpcResponseCodeEnum;

import java.io.Serializable;

public class RpcResponse<T> implements Serializable {

    private String requestId;

    private Integer code;

    private String msg;

    private T data;

    public static <T> RpcResponse<T> success(T data, String requestId) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setRequestId(requestId);
        response.setData(data);
        response.setCode(RpcResponseCodeEnum.SUCCESS.getCode());
        response.setMsg(RpcResponseCodeEnum.SUCCESS.getMsg());
        return response;
    }

    public static <T> RpcResponse<T> fail(RpcResponseCodeEnum code) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(RpcResponseCodeEnum.FAIL.getCode());
        response.setMsg(RpcResponseCodeEnum.FAIL.getMsg());
        return response;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
