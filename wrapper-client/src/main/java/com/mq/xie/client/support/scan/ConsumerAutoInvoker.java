package com.mq.xie.client.support.scan;

import com.alibaba.fastjson.JSON;
import com.mq.xie.client.pojo.MessageWraper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


public class ConsumerAutoInvoker implements ApplicationContextAware ,InitializingBean{

    protected final Logger logger = LoggerFactory.getLogger(ConsumerAutoInvoker.class);

    private Map<Class,Object> subscribleBeans = new HashMap<>();

    @Autowired
    private VirtualPointInfo consumerInfo;

    private ApplicationContext applicationContext;


    public boolean invoke(MessageWraper messageWraper) throws InvocationTargetException, IllegalAccessException {

        if (consumerInfo == null) {
            logger.warn("not scan consumer info");
            return false;
        }
        String key = StringUtils.isEmpty(messageWraper.getTag()) ? messageWraper.getTopic() : messageWraper.getTopic() + ":" + messageWraper.getTag();
        MethodInfo methodInfo = consumerInfo.getMethodInfo(key);
        if (methodInfo != null) {
            logger.info("{} 自动匹配到业务代码", key);
            Object targetBean = subscribleBeans.get(methodInfo.getTargetClass());
            Method targetMethod = methodInfo.getMethod();
            Class<?>[] parameterTypes = targetMethod.getParameterTypes();
            Object arg0 = JSON.parseObject(messageWraper.getData(), parameterTypes[0]);
            Object[] args = new Object[]{arg0};
            targetMethod.invoke(targetBean, args);
            return true;
        } else {
            logger.info("{} 未自动匹配到业务代码", key);
            return false;
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, MethodInfo> methodInfoMap = consumerInfo.getMethodInfoMap();
        if(methodInfoMap != null && methodInfoMap.size()>0){
            methodInfoMap.forEach((topic, methodInfo) -> {
                Object targetBean = applicationContext.getBean(methodInfo.getTargetClass());
                subscribleBeans.put(methodInfo.getTargetClass(),targetBean);
            });
        }
    }
}
