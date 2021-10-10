package com.zzw.jrpc.remoting.dto;

import java.io.Serializable;

public class RpcMessage implements Serializable {

    private byte messageType;

    private int requestId;

    private Object data;

    public RpcMessage() {
    }

    public RpcMessage(byte messageType, int requestId, Object data) {
        this.messageType = messageType;
        this.requestId = requestId;
        this.data = data;
    }

    public byte getMessageType() {
        return messageType;
    }

    public void setMessageType(byte messageType) {
        this.messageType = messageType;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
