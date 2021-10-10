package com.zzw.jrpc.annotation;

import com.zzw.jrpc.spring.CustomScannerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Import({CustomScannerRegistrar.class})
@Documented
public @interface JRpcScan {
    String[] basePackage();
}
