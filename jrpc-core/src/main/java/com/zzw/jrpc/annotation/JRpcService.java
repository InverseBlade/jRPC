package com.zzw.jrpc.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface JRpcService {
    String version() default "";

    String group() default "";
}
