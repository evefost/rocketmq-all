package com.mq.xie.client.annotation;

import java.lang.annotation.*;

/**
 * 标识为事件消息，需引用transaction 包才支持
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
@Documented
public @interface TransMsg {

}
