package com.mq.xie.client.config;

import com.mq.xie.client.support.*;
import com.mq.xie.client.support.scan.VirtualPointInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.*;

import static com.mq.xie.client.config.RocketmqProperties.PREFIX;
import static com.mq.xie.client.support.scan.VirtualBeanRegistrar.CONSUMER_INFO;


@Configuration
@EnableConfigurationProperties({RocketmqProperties.class})
@ConditionalOnProperty(prefix = PREFIX, value = "serverAddr")
@Slf4j
public class RocketmqAutoConfiguration  {



    /**
     * 生产者配置
     */
    @ConditionalOnProperty(prefix = PREFIX, value = "producer.enable", matchIfMissing = true)
    public static class ProducerConfiguration {

        @Autowired
        private RocketmqProperties properties;
        {
            System.setProperty("rocketmq.client.log.loadconfig","false");
        }

        @Bean
        public DefaultMQProducer defaultProducer() throws MQClientException {
            /**
             * 一个应用创建一个Producer，由应用来维护此对象，可以设置为全局对象或者单例<br>
             * 注意：ProducerGroupName需要由应用来保证唯一<br>
             * ProducerGroup这个概念发送普通的消息时，作用不大，但是发送分布式事务消息时，比较关键，
             * 因为服务器会回查这个Group下的任意一个Producer
             */
            DefaultMQProducer producer = new DefaultMQProducer(properties.getInstanceName());
            producer.setNamesrvAddr(properties.getServerAddr());
            producer.setInstanceName(properties.getInstanceName());
            producer.setVipChannelEnabled(false);

            /**
             * Producer对象在使用之前必须要调用start初始化，初始化一次即可<br>
             * 注意：切记不可以在每次发送消息时，都调用start方法
             */
            producer.start();
            System.out.println("RocketMq defaultProducer Started.");
            return producer;
        }
        @Bean
        public InnerMessageSender getInnerMessageSender(DefaultMQProducer defaultProducer) {
            return new MessageSender(defaultProducer);
        }

        @Bean
        public MessagePublisher getMessagePublisher(InnerMessageSender messageSender) {
            return new GeneralMessagePublisher(messageSender);
        }
    }


    /**
     * 消费者配置
     */
    @ConditionalOnProperty(prefix = PREFIX, value = "consumer.enable", matchIfMissing = true)
    public static class ConsumerConfiguration implements InitializingBean, ApplicationContextAware{

        ApplicationContext applicationContext;

        @Autowired
        private RocketmqProperties properties;

        @Autowired(required = false)
        private DefaultMQPushConsumer consumer;

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            this.applicationContext = applicationContext;
        }

        /**
         * 初始化rocketmq消息监听方式的消费者
         */
        @Bean
        public DefaultMQPushConsumer pushConsumer(MessageListenerConcurrently messageListenerConcurrently) throws MQClientException {

            /**
             * 一个应用创建一个Consumer，由应用来维护此对象，可以设置为全局对象或者单例<br>
             * 注意：ConsumerGroupName需要由应用来保证唯一
             */
            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(properties.getInstanceName());
            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
            consumer.setNamesrvAddr(properties.getServerAddr());
            consumer.setInstanceName(properties.getInstanceName());
            consumer.setConsumeMessageBatchMaxSize(1);//设置批量消费，以提升消费吞吐量，默认是1
            //设置成广播模式，也就是发布订阅模式，消息会发给Consume Group中的每一个消费者进行消费
            //consumer.setMessageModel(MessageModel.BROADCASTING);
            consumer.registerMessageListener(messageListenerConcurrently);
            return consumer;
        }


        @Bean
        public MessageListenerConcurrently getConsumerListener() {
            return new ComsumerMessageListener();
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            if (consumer != null) {
                VirtualPointInfo consumerInfo = null;
                try {
                    consumerInfo = (VirtualPointInfo) applicationContext.getBean(CONSUMER_INFO);
                } catch (NoSuchBeanDefinitionException ex) {
                    log.warn("未开启自动扫描主题");
                }
                List<String> subscribeList = properties.getSubscribe();
                Map<String, List<String>> allTopTags = new HashMap<>();
                if (subscribeList != null && !subscribeList.isEmpty()) {
                    for (String sunscribe : subscribeList) {
                        String[] topicTags = sunscribe.split(":");
                        String topic = topicTags[0];
                        String tags = "*";
                        if(topicTags.length==2){
                            tags = topicTags[1];
                        }
                        String[] stags = tags.split("\\|\\|");
                        List<String> topicTagList = new ArrayList<>();
                        for (String st : stags) {
                            topicTagList.add(st);
                        }
                        allTopTags.put(topic, topicTagList);
                    }
                }
                checkSubcsribeUseStatus(allTopTags);
                //合拼主题
                if (consumerInfo != null && !consumerInfo.getTopicTags().isEmpty()) {
                    Map<String, List<String>> topicTags = consumerInfo.getTopicTags();
                    topicTags.forEach((topic, tags) -> {
                        List<String> tTags = allTopTags.get(topic);
                        if (tTags == null) {
                            allTopTags.put(topic, tags);
                        } else {
                            tags.stream().forEach(tag -> {
                                if (!tTags.contains(tag)) {
                                    tTags.add(tag);
                                }
                            });
                        }
                    });

                }
                if (allTopTags.isEmpty()) {
                    log.warn("消费端已开启，但未订阅任何主题");
                } else {
                    log.info("消费者将订阅的以下主题...");
                    allTopTags.forEach((topic, tags) -> {
                        try {
                            String tempTags = null;
                            if (tags.contains("*")) {
                                tempTags = "*";
                            } else {
                                StringBuilder sb = new StringBuilder();
                                tags.stream().forEach(tag -> {
                                    sb.append(tag).append("||");
                                });
                                tempTags = sb.substring(0, sb.lastIndexOf("||")).toString();
                            }
                            log.info("{}:{}", topic, tempTags);
                            consumer.subscribe(topic, tempTags);
                        } catch (MQClientException e) {
                            e.printStackTrace();
                        }
                    });
                }

                try {
                    consumer.start();
                } catch (Exception e) {
                    log.error("RocketMq pushConsumer Start failure!!!.");
                    e.printStackTrace();
                }
                log.info("RocketMq pushConsumer Started.");
            }
        }

        private void checkSubcsribeUseStatus(Map<String, List<String>> allTopTags) {
            if (applicationContext instanceof BeanDefinitionRegistry) {
                BeanDefinitionRegistry factory = (BeanDefinitionRegistry) applicationContext;

                ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(factory);
                AnnotationTypeFilter annotationTypeFilter = new AnnotationTypeFilter(
                        Component.class);
                scanner.addIncludeFilter(annotationTypeFilter);
                scanner.setIncludeAnnotationConfig(false);
                Set<BeanDefinition> candidateComponents = scanner
                        .findCandidateComponents("com.xhg");
                ClassLoader classLoader = applicationContext.getClassLoader();
                if (candidateComponents.size() > 0) {
                    for (BeanDefinition candidateComponent : candidateComponents) {
                        try {
                            Class<?> aClass = classLoader.loadClass(candidateComponent.getBeanClassName());
                            Method[] methods = aClass.getDeclaredMethods();
                            for (Method method : methods) {
                                if (method.isAnnotationPresent(EventListener.class)) {
                                    EventListener annotation = method.getAnnotation(EventListener.class);
                                    String condition = annotation.condition();
                                    if (!StringUtils.isEmpty(condition)) {
                                        String[] split = condition.split("==");
                                        String[] split1 = split[1].split("&&");
                                        String topic = split1[0].replace("'", "").trim();
                                        String tag = split[2].replace("'", "").trim();
                                        List<String> subscibeTagList = allTopTags.get(topic);
                                        if (subscibeTagList == null) {
                                            log.warn("{} 没有被订阅，可能收不到消息", topic + ":" + tag);
                                        } else {
                                            if (!subscibeTagList.contains(tag)) {
                                                log.warn("{} 有业务处理，但还没有订阅该主题目", topic + ":" + tag);
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }




}
