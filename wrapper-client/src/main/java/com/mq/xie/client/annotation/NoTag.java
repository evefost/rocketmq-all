package com.mq.xie.client.annotation;

import java.lang.annotation.*;

/**
 * 标认无tag方法订阅主题
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
@Documented
public @interface NoTag {

}
