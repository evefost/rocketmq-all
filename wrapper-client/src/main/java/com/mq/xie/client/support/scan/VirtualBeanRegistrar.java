package com.mq.xie.client.support.scan;

import com.alibaba.fastjson.JSON;
import com.mq.xie.client.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.*;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

import static com.google.common.collect.Sets.newHashSet;


public class VirtualBeanRegistrar implements ImportBeanDefinitionRegistrar,
        ResourceLoaderAware, BeanClassLoaderAware, EnvironmentAware {
    protected final Logger logger = LoggerFactory.getLogger(VirtualBeanRegistrar.class);

    public static final String CONSUMER_INFO = "consumerInfo";

    private final VirtualPointInfo producerInfo = new VirtualPointInfo();

    private final VirtualPointInfo consumerInfo = new VirtualPointInfo();

    public VirtualPointInfo getProducerInfo() {
        return producerInfo;
    }

    public VirtualPointInfo getConsumerInfo() {
        return consumerInfo;
    }

    private static final Set<String> baseTypes = newHashSet(
            "int",
            "date",
            "string",
            "double",
            "float",
            "boolean",
            "byte",
            "object",
            "long",
            "date-time",
            "file",
            "biginteger",
            "bigdecimal");

    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    protected ResourceLoader resourceLoader;

    protected ClassLoader classLoader;

    protected Environment environment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        logger.info("自动扫描主题信息...");
        Set<String> basePackages = getBasePackages(metadata);
        String producerEnable = environment.getProperty("spring.extend.mq.producer.enable","true");
        String consumerEnable = environment.getProperty("spring.extend.mq.consumer.enable","true");
        boolean pdenable = Boolean.parseBoolean(producerEnable);
        boolean csenable = Boolean.parseBoolean(consumerEnable);
        if(pdenable == false && csenable ==false){
            logger.warn("生产端消费端都没有启用,生产端虚拟接口将无法注入,可能导致项目启动失败");
        }
        if(pdenable) {
            try {
                registerVirtualApis(basePackages, registry);
            } catch (ClassNotFoundException e) {
                logger.error("扫描虚拟接口失败:{}",e);
            }
        }
        //注入consumer信息
        GenericBeanDefinition consumerBd = new GenericBeanDefinition();
        consumerBd.setBeanClass(VirtualPointInfo.class);
        MutablePropertyValues propertyValues = new MutablePropertyValues();
        consumerBd.setPropertyValues(propertyValues);
        propertyValues.add("methodInfoMap", consumerInfo.getMethodInfoMap());
        propertyValues.add("topicTags", consumerInfo.getTopicTags());
        registry.registerBeanDefinition(CONSUMER_INFO, consumerBd);
        if(csenable){
            try {
                scanConsumers(basePackages);
                GenericBeanDefinition invokerBd = new GenericBeanDefinition();
                invokerBd.setBeanClass(ConsumerAutoInvoker.class);
                MutablePropertyValues invokerpropertyValues = new MutablePropertyValues();
                invokerBd.setPropertyValues(invokerpropertyValues);
                registry.registerBeanDefinition("autoInvoker", invokerBd);
            } catch (ClassNotFoundException e) {
                logger.error("扫描订阅接口失败:{}",e);
            }
        }
    }


    protected Set<String> getBasePackages(AnnotationMetadata importingClassMetadata) {
        Map<String, Object> attributes = importingClassMetadata
                .getAnnotationAttributes(EnableScanTopic.class.getCanonicalName());

        Set<String> basePackages = new HashSet<>();
        for (String pkg : (String[]) attributes.get("producerPackages")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }

        for (String pkg : (String[]) attributes.get("consumerPackages")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }
        if (basePackages.isEmpty()) {
            basePackages.add(
                    ClassUtils.getPackageName(importingClassMetadata.getClassName()));
        }
        return basePackages;
    }

    private void registerVirtualApis(Set<String> basePackages,
                                     BeanDefinitionRegistry registry) throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner = getScanner();
        scanner.setResourceLoader(this.resourceLoader);
        AnnotationTypeFilter annotationTypeFilter = new AnnotationTypeFilter(
                Producer.class);
        scanner.addIncludeFilter(annotationTypeFilter);


        for (String basePackage : basePackages) {
            Set<BeanDefinition> candidateComponents = scanner
                    .findCandidateComponents(basePackage);
            for (BeanDefinition candidateComponent : candidateComponents) {
                if (candidateComponent instanceof AnnotatedBeanDefinition) {
                    // verify annotated class is an interface
                    AnnotatedBeanDefinition beanDefinition = (AnnotatedBeanDefinition) candidateComponent;
                    AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();
                    String name = "virtualApi$" + candidateComponent.getBeanClassName();
                    registerVirtualApi(registry, name, annotationMetadata);
                }
            }
        }
        logger.info("当前应用扫描到生产topics " + JSON.toJSONString(producerInfo.getTopicTags()));
    }


    protected void registerVirtualApi(BeanDefinitionRegistry registry, String name,
                                      AnnotationMetadata annotationMetadata) throws ClassNotFoundException {
        String beanName = name;
        logger.debug("即将创建的实例名:" + beanName);
        String beanClassName = annotationMetadata.getClassName();
        parseVirtualInfo(beanClassName, producerInfo,true);
        Map<String, Object> attritutes = annotationMetadata.getAnnotationAttributes(Topic.class.getCanonicalName());
        BeanDefinitionBuilder definition = BeanDefinitionBuilder
                .genericBeanDefinition(VirtualFactoryBean.class);
        definition.addPropertyValue("name", name);
        definition.addPropertyValue("type", beanClassName);
        definition.addPropertyValue("topic", attritutes.get("value"));
        definition.addPropertyValue("producerInfo", producerInfo);
        definition.addPropertyValue("consumerInfo", consumerInfo);

        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        AbstractBeanDefinition beanDefinition = definition.getBeanDefinition();
        beanDefinition.setPrimary(false);
        BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, beanName,
                new String[]{});
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
    }


    public void scanConsumers(Set<String> basePackages) throws ClassNotFoundException {
        //扫描指定的包，过滤出只打topic 及 tag标签的接口或类
        ClassPathScanningCandidateComponentProvider scanner = getScanner();
        scanner.setResourceLoader(this.resourceLoader);
        AnnotationTypeFilter inFilter = new AnnotationTypeFilter(
                Consumer.class);
        scanner.addIncludeFilter(inFilter);

        for (String basePackage : basePackages) {
            Set<BeanDefinition> candidateComponents = scanner
                    .findCandidateComponents(basePackage);
            for (BeanDefinition candidateComponent : candidateComponents) {
                if (candidateComponent instanceof AnnotatedBeanDefinition) {
                    String beanClassName = candidateComponent.getBeanClassName();
                    parseVirtualInfo(beanClassName, consumerInfo,false);
                }
            }

        }
        logger.info("当前应用扫描到消费者topics " + JSON.toJSONString(consumerInfo.getTopicTags()));
    }


    private void parseVirtualInfo(String beanClassName, VirtualPointInfo pointInfo,boolean isProducer) throws ClassNotFoundException {

        Class<?> targetClass = classLoader.loadClass(beanClassName);
        Topic classTopicAnno = targetClass.getAnnotation(Topic.class);
        String classTopic = null;
        if(classTopicAnno != null){
            classTopic = classTopicAnno.value();
        }
        Method[] methods = null;
        if (targetClass.isInterface()) {
            methods = targetClass.getMethods();
        } else {
            methods = targetClass.getDeclaredMethods();
        }
        for (Method method : methods) {
            method.setAccessible(true);
            Topic methodTopicAnno = method.getAnnotation(Topic.class);
            Tag tagAnno = method.getAnnotation(Tag.class);
            String methodTopic = null;
            if(methodTopicAnno !=null){
                methodTopic = methodTopicAnno.value();
            }
            String targetTopic = methodTopic==null?classTopic:methodTopic;
            if(StringUtils.isEmpty(targetTopic)){
                throw new RuntimeException("主题不能为空"+targetClass.getName()+"."+method.getName());
            }
            targetTopic = resolve(targetTopic);
            MethodInfo methodInfo = new MethodInfo();
            String key = null;
            //消费端
            if(!isProducer && !StringUtils.isEmpty(methodTopic) && tagAnno == null){
                //该方法仅订阅了主题
                key = methodTopic;
                MethodInfo rs = pointInfo.getMethodInfo(key);
                if(rs != null){
                    String erromsg = targetClass.getName() + "." + method.getName()+"&&"+rs.getTargetClass().getName()+"."+rs.getMethod().getName();
                    throw new RuntimeException("消费端有两或以上仅订阅主题且主题相同的方法 " + erromsg);
                }
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes != null && parameterTypes.length > 1) {
                    throw new RuntimeException("消息参数仅支持单个且为非基本类型" + targetClass.getName() + "." + method.getName());
                } else if (parameterTypes != null && parameterTypes.length == 1 && isBaseType(parameterTypes[0].getSimpleName())) {
                    throw new RuntimeException("消息参数仅支持单个且为非基本类型" + targetClass.getName() + "." + method.getName());
                }
                methodInfo.setTag(null);
                pointInfo.putMethodInfo(key, methodInfo);
                List<String> tags = pointInfo.getTags(targetTopic);
                if (tags == null) {
                    tags = new ArrayList<>();
                }
                tags.add("*");
                pointInfo.getTopicTags().put(targetTopic, tags);
            }
            //生产端
            if(isProducer && tagAnno == null){
                key = targetTopic;
                MethodInfo rs = pointInfo.getMethodInfo(key);
                if(rs != null){
                    String erromsg = targetClass.getName() + "." + method.getName()+"&&"+rs.getTargetClass().getName()+"."+rs.getMethod().getName();
                    throw new RuntimeException("虚拟接口有两或以上相同主题 " + erromsg);
                }
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes != null && parameterTypes.length > 1) {
                    throw new RuntimeException("消息参数仅支持单个且为非基本类型" + targetClass.getName() + "." + method.getName());
                } else if (parameterTypes != null && parameterTypes.length == 1 && isBaseType(parameterTypes[0].getSimpleName())) {
                    throw new RuntimeException("消息参数仅支持单个且为非基本类型" + targetClass.getName() + "." + method.getName());
                }
                methodInfo.setTag(null);
                pointInfo.putMethodInfo(key, methodInfo);
            }
            methodInfo.setTargetClass(targetClass);
            methodInfo.setMethod(method);
            methodInfo.setTopic(targetTopic);

            if (tagAnno != null) {
                String  tag = tagAnno.value();
                tag = resolve(tag);
                key = targetTopic + ":" + tag;

                MethodInfo rs = pointInfo.getMethodInfo(key);
                if (rs != null) {
                    String erromsg = rs.getTargetClass().getName() + " & " + targetClass.getName();
                    throw new RuntimeException("  topic&&tag: " + key + " aready exist in " + erromsg);
                }
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes != null && parameterTypes.length > 1) {
                    throw new RuntimeException("消息参数仅支持单个且为非基本类型" + targetClass.getName() + "." + method.getName());
                } else if (parameterTypes != null && parameterTypes.length == 1 && isBaseType(parameterTypes[0].getSimpleName())) {
                    throw new RuntimeException("消息参数仅支持单个且为非基本类型" + targetClass.getName() + "." + method.getName());
                }
                methodInfo.setTag(tag);
                pointInfo.putMethodInfo(key, methodInfo);

                List<String> tags = pointInfo.getTags(targetTopic);
                if (tags == null) {
                    tags = new ArrayList<>();
                }
                tags.add(tag);
                pointInfo.getTopicTags().put(targetTopic, tags);
            }
        }

    }

    protected ClassPathScanningCandidateComponentProvider getScanner() {
        return new ClassPathScanningCandidateComponentProvider(false, this.environment) {

            @Override
            protected boolean isCandidateComponent(
                    AnnotatedBeanDefinition beanDefinition) {
                if (beanDefinition.getMetadata().isIndependent()) {
                    // TODO until SPR-11711 will be resolved
                    if (beanDefinition.getMetadata().isInterface()
                            && beanDefinition.getMetadata()
                            .getInterfaceNames().length == 1
                            && Annotation.class.getName().equals(beanDefinition
                            .getMetadata().getInterfaceNames()[0])) {
                        try {
                            Class<?> target = ClassUtils.forName(
                                    beanDefinition.getMetadata().getClassName(),
                                    VirtualBeanRegistrar.this.classLoader);
                            return !target.isAnnotation();
                        } catch (Exception ex) {
                            this.logger.error(
                                    "Could not load target class: "
                                            + beanDefinition.getMetadata().getClassName(),
                                    ex);

                        }
                    }
                    return true;
                }
                return false;

            }
        };
    }
    public static boolean isBaseType(String typeName) {
        return baseTypes.contains(typeName.toLowerCase());
    }

    private String resolve(String value) {
        if (StringUtils.hasText(value)) {
            return this.environment.resolvePlaceholders(value);
        }
        return value;
    }


}
