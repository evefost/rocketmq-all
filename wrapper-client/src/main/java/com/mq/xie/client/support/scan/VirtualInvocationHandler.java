package com.mq.xie.client.support.scan;

import com.alibaba.fastjson.JSON;
import com.mq.xie.client.support.AbsMessagePublisher;
import com.mq.xie.client.pojo.SourceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * Created by xieyang on 18/7/14.
 */
public class VirtualInvocationHandler implements InvocationHandler {


    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private VirtualFactoryBean factoryBean;

    private ApplicationContext applicationContext;

    private AbsMessagePublisher publisher;

    private Map<Method, MethodInfo> methodInfos;



    public VirtualInvocationHandler(ApplicationContext applicationContext, Map<Method, MethodInfo> methodInfos) {
        this.applicationContext = applicationContext;
        this.publisher = applicationContext.getBean(AbsMessagePublisher.class);
        this.methodInfos =methodInfos;

    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if ("equals".equals(method.getName())) {
            try {
                Object
                        otherHandler =
                        args.length > 0 && args[0] != null ? Proxy.getInvocationHandler(args[0]) : null;
                return equals(otherHandler);
            } catch (IllegalArgumentException e) {
                return false;
            }
        } else if ("hashCode".equals(method.getName())) {
            return method.hashCode();
        } else if ("toString".equals(method.getName())) {
            return method.toString();
        }
        try {
            MethodInfo methodInfo = methodInfos.get(method);
            String key = methodInfo.getTopic()+":"+methodInfo.getTag();
            logger.debug("发布消息{}: {}", key ,JSON.toJSONString(args));
            SourceEvent sourceEvent = new SourceEvent(this);
            sourceEvent.setTopic(methodInfo.getTopic());
            sourceEvent.setTag(methodInfo.getTag());
            sourceEvent.setTrans(methodInfo.isTrans());
            if(args != null && args.length==1){
                sourceEvent.setData(args[0]);
                if(sourceEvent.isTrans()){
                    publisher.publishTransEvent(sourceEvent);
                }else {
                    publisher.publishEvent(sourceEvent);
                }
            }else {
                throw new RuntimeException("parameters are not support");
            }
            Class<?> returnType = method.getReturnType();
            if (void.class == returnType) {
                return null;
            }
            return null;
        } finally {

        }
    }
}
