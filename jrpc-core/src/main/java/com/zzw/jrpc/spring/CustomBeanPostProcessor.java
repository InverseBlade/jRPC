package com.zzw.jrpc.spring;

import com.zzw.jrpc.annotation.JRpcReference;
import com.zzw.jrpc.annotation.JRpcService;
import com.zzw.jrpc.base.factory.SingletonFactory;
import com.zzw.jrpc.config.RpcServiceConfig;
import com.zzw.jrpc.provider.ServiceProvider;
import com.zzw.jrpc.provider.impl.ZkServiceProviderImpl;
import com.zzw.jrpc.proxy.RpcClientProxy;
import com.zzw.jrpc.remoting.transport.RpcTransport;
import com.zzw.jrpc.remoting.transport.netty.client.NettyRpcClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Component
@Slf4j
public class CustomBeanPostProcessor implements BeanPostProcessor {

    private final ServiceProvider serviceProvider;
    private final RpcTransport rpcTransport;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        //log.info("Triggered: postProcessBeforeInitialization [{}] [{}]", bean, beanName);
        if (bean.getClass().isAnnotationPresent(JRpcService.class)) {
            log.info("[{}] is annotated with [{}]", bean.getClass().getName(), JRpcService.class.getCanonicalName());
            JRpcService jRpcService = bean.getClass().getAnnotation(JRpcService.class);
            RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
            rpcServiceConfig.setGroup(jRpcService.group());
            rpcServiceConfig.setVersion(jRpcService.version());
            rpcServiceConfig.setService(bean);
            //
            serviceProvider.publishService(rpcServiceConfig);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            JRpcReference jRpcReference = field.getAnnotation(JRpcReference.class);
            if (jRpcReference != null) {
                RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
                rpcServiceConfig.setGroup(jRpcReference.group());
                rpcServiceConfig.setVersion(jRpcReference.version());

                Object serviceProxy = new RpcClientProxy(rpcTransport, rpcServiceConfig)
                        .getProxy(field.getType());
                field.setAccessible(true);
                try {
                    field.set(bean, serviceProxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }

    public CustomBeanPostProcessor() {
        serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
        rpcTransport = SingletonFactory.getInstance(NettyRpcClient.class);
    }

}
