package com.zzw.jrpc.proxy;

import com.zzw.jrpc.base.enums.RpcErrorMsgEnum;
import com.zzw.jrpc.base.enums.RpcResponseCodeEnum;
import com.zzw.jrpc.base.exception.RpcException;
import com.zzw.jrpc.config.RpcServiceConfig;
import com.zzw.jrpc.remoting.dto.RpcRequest;
import com.zzw.jrpc.remoting.dto.RpcResponse;
import com.zzw.jrpc.remoting.transport.RpcTransport;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class RpcClientProxy implements InvocationHandler {

    public static final String INTERFACE_NAME = "interfaceName";

    private final RpcTransport rpcTransport;
    private final RpcServiceConfig rpcServiceConfig;

    public RpcClientProxy(RpcTransport rpcTransport) {
        this.rpcTransport = rpcTransport;
        rpcServiceConfig = new RpcServiceConfig();
    }

    public RpcClientProxy(RpcTransport rpcTransport, RpcServiceConfig serviceConfig) {
        this.rpcTransport = rpcTransport;
        rpcServiceConfig = serviceConfig;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("invoked method: [{}]", method.getName());
        RpcRequest rpcRequest = new RpcRequest();

        rpcRequest.setRequestId(UUID.randomUUID().toString());
        rpcRequest.setInterfaceName(method.getDeclaringClass().getName());
        rpcRequest.setGroup(rpcServiceConfig.getGroup());
        rpcRequest.setVersion(rpcServiceConfig.getVersion());
        rpcRequest.setMethodName(method.getName());
        rpcRequest.setParamTypes(method.getParameterTypes());
        rpcRequest.setParameters(args);
        //
        RpcResponse<Object> rpcResponse = null;
        CompletableFuture<RpcResponse<Object>> future = null;
        // 重试策略
        for (int i = 0; i < 3; i++) {
            try {
                future = rpcTransport.sendRpcRequest(rpcRequest);
                rpcResponse = future.get(7, TimeUnit.SECONDS);
                break;
            } catch (TimeoutException | IllegalStateException e) {
                log.error("Network TimeOut! retries=[{}]", i);
                if (future != null) {
                    future.cancel(true);
                }
                rpcRequest.setRequestId(UUID.randomUUID().toString());
            }
        }
        //
        check(rpcResponse, rpcRequest);
        return rpcResponse.getData();
    }

    public <T> T getProxy(Class<T> clazz) {
        return clazz.cast(Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this));
    }

    public void check(RpcResponse<Object> response, RpcRequest request) {
        if (response == null) {
            throw new RpcException(
                    RpcErrorMsgEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + request.getInterfaceName());
        }
        if (!request.getRequestId().equals(response.getRequestId())) {
            throw new RpcException(
                    RpcErrorMsgEnum.REQUEST_NOT_MATCH_RESPONSE, INTERFACE_NAME + ":" + request.getInterfaceName()
            );
        }
        if (response.getCode() != null && !response.getCode().equals(RpcResponseCodeEnum.SUCCESS.getCode())) {
            throw new RpcException(
                    RpcErrorMsgEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + request.getInterfaceName()
            );
        }
    }

}

















