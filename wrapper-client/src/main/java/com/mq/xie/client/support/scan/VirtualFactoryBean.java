package com.mq.xie.client.support.scan;

import com.mq.xie.client.annotation.Tag;
import com.mq.xie.client.annotation.Topic;
import com.mq.xie.client.annotation.TransMsg;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

class VirtualFactoryBean implements FactoryBean<Object>, InitializingBean, ApplicationContextAware, EnvironmentAware {

    private ApplicationContext applicationContext;

    protected Environment environment;

    private Class<?> type;

    private String name;

    private String topic;


    private VirtualPointInfo producerInfo;

    private  VirtualPointInfo consumerInfo;


    public Object getObject() throws Exception {
        //创建代理
        //1.准备一些材料，把method 与topic ,tags 的关系存起来
        Method[] declaredMethods = type.getMethods();
        Map<Method,MethodInfo> methodMethodInfoMap = new HashMap<Method,MethodInfo>();
        String classTopic = topic;
        for(Method method:declaredMethods){
            MethodInfo methodInfo = new MethodInfo();
            methodInfo.setTargetClass(type);
            methodInfo.setMethod(method);
            Topic methodTopicAnno = method.getAnnotation(Topic.class);
            String methodTopic = null;
            if(methodTopicAnno != null){
                methodTopic = methodTopicAnno.value();
            }
            String targetTopic = methodTopic != null?methodTopic:classTopic;
            targetTopic = resolve(targetTopic);
            methodInfo.setTopic(targetTopic);
            Tag annotation = method.getAnnotation(Tag.class);
            if(annotation != null){
                String tag = annotation.value();
                tag = resolve(tag);
                methodInfo.setTag(tag);
            }
            methodInfo.setTrans(method.isAnnotationPresent(TransMsg.class));
            methodMethodInfoMap.put(method,methodInfo);
        }

        //2.调用代理接口时，通过方法签名去匹配上面的信息
        Object proxy = Proxy.newProxyInstance(VirtualFactoryBean.class.getClassLoader(), new Class[]{type}, new VirtualInvocationHandler(applicationContext,methodMethodInfoMap));
        return proxy;
    }



    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public VirtualPointInfo getProducerInfo() {
        return producerInfo;
    }

    public void setProducerInfo(VirtualPointInfo producerInfo) {
        this.producerInfo = producerInfo;
    }

    public VirtualPointInfo getConsumerInfo() {
        return consumerInfo;
    }

    public void setConsumerInfo(VirtualPointInfo consumerInfo) {
        this.consumerInfo = consumerInfo;
    }

    public Class<?> getObjectType() {
        return this.type;
    }

    public boolean isSingleton() {
        return true;
    }

    public void afterPropertiesSet() throws Exception {

    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public String toString() {
        return new StringBuilder("MyTestFactoryBean{")
                .append("type=").append(type).append(", ")
                .append("name='").append(name).append("', ")
                .append("topic='").append(topic).append("', ")
                .append("}").toString();
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    private String resolve(String value) {
        if (StringUtils.hasText(value)) {
            return this.environment.resolvePlaceholders(value);
        }
        return value;
    }
}
