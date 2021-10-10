package com.zzw.jrpc.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface JRpcReference {

    String version() default "";

    String group() default "";

}
