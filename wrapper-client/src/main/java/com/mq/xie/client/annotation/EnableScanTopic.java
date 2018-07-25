package com.mq.xie.client.annotation;

import com.mq.xie.client.support.scan.VirtualBeanRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(VirtualBeanRegistrar.class)
public @interface EnableScanTopic {

    /**
     * 生产接口包
     * @return
     */
    String[] producerPackages() default {};

    /**
     * 消费接口包
     * @return
     */
    String[] consumerPackages() default {};
}
