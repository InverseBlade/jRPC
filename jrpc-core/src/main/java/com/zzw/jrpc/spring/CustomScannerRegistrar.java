package com.zzw.jrpc.spring;

import com.zzw.jrpc.annotation.JRpcScan;
import com.zzw.jrpc.annotation.JRpcService;
import com.zzw.jrpc.base.factory.SingletonFactory;
import com.zzw.jrpc.remoting.transport.netty.server.NettyRpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.stereotype.Component;

public class CustomScannerRegistrar implements ResourceLoaderAware, ImportBeanDefinitionRegistrar {

    public static final String DEFAULT_BASE_PACKAGE = "com.zzw";
    public static final String BASE_PACKAGE_ATTR_NAME = "basePackage";
    public ResourceLoader resourceLoader;

    public static final Logger log = LoggerFactory.getLogger(CustomScannerRegistrar.class);

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata,
                                        BeanDefinitionRegistry registry) {
        AnnotationAttributes rpcScanAnnotationAttrs = AnnotationAttributes.fromMap(
                annotationMetadata.getAnnotationAttributes(JRpcScan.class.getName())
        );
        String[] rpcScanBasePackages = new String[0];

        if (rpcScanAnnotationAttrs != null) {
            rpcScanBasePackages = rpcScanAnnotationAttrs.getStringArray(BASE_PACKAGE_ATTR_NAME);
        }
        if (rpcScanBasePackages.length == 0) {
            rpcScanBasePackages = new String[]{
                    ((StandardAnnotationMetadata) annotationMetadata)
                            .getIntrospectedClass().getPackage().getName()
            };
        }
        CustomScanner rpcServiceScanner
                = new CustomScanner(registry, JRpcService.class);
        CustomScanner beanScanner
                = new CustomScanner(registry, Component.class);
        if (resourceLoader != null) {
            rpcServiceScanner.setResourceLoader(resourceLoader);
            beanScanner.setResourceLoader(resourceLoader);
        }
        int beanAmount = beanScanner.scan(DEFAULT_BASE_PACKAGE);
        log.info("BeanScanner扫描数量 [{}]", beanAmount);
        int rpcServiceAmount = rpcServiceScanner.scan(rpcScanBasePackages);
        log.info("rpcServiceScanner扫描数量 [{}]", rpcServiceAmount);
        // 若有发布 RpcService 则自动开启 RpcServer
        if (rpcServiceAmount > 0) {
            SingletonFactory.getInstance(NettyRpcServer.class).startAsync();
        }
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }


}
