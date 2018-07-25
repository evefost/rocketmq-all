package com.mq.xie.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

import static com.mq.xie.client.config.RocketmqProperties.PREFIX;


@Data
@ConfigurationProperties(PREFIX)
public class RocketmqProperties {

    public static final String PREFIX = "spring.extend.mq";

    public static final String DEFAULT_TOPIC = "default_topic";

    /**
     * mq nameserver 集群地址，用“;”分割地址
     */
    private String serverAddr;

    /**
     * 实例名称
     */
    private String instanceName;

    private String clientIP;


    /**
     * 生产者默认主题 default_topic，可设定
     */
    private String topic = DEFAULT_TOPIC;

    /**
     * 消费者订阅主题,格式:topic1:tag1,topic2:tag2
     */
    private List<String> subscribe;
    /**
     * 消息消费重试次数，从0开始
     */
    private int consumerRetryCount;

}
