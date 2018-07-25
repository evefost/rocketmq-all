package com.mq.xie.client.support;


import com.alibaba.fastjson.JSON;
import com.mq.xie.client.pojo.MessageWraper;
import com.mq.xie.client.pojo.TargetEvent;
import com.mq.xie.client.config.RocketmqProperties;
import com.mq.xie.client.support.scan.ConsumerAutoInvoker;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;


@Slf4j
public class ComsumerMessageListener implements MessageListenerConcurrently {

    @Autowired
    protected RocketmqProperties properties;

    @Autowired
    protected ApplicationEventPublisher publisher;

    @Autowired(required = false)
    protected ConsumerAutoInvoker invoker;

//    public ComsumerMessageListener(ConsumerAutoInvoker invoker){
//        this.invoker = invoker;
//    }

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        MessageExt msg = msgs.get(0);
        try {
            log.info("收到非事务性消息:{}:{} ",msg.getMsgId(),msg.getTopic(),msg.getTags());
            MessageWraper messageWraper = paserMessage(msg, MessageWraper.class);
            return doAfterParseMessage(msg,messageWraper);
        } catch (Exception e) {
            log.error("消费消息失败：", e);
            if (msg.getReconsumeTimes() < properties.getConsumerRetryCount()) {
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
            try {
            } catch (Exception e1) {
                log.error("save error info:", e1);
            }
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

    protected ConsumeConcurrentlyStatus doAfterParseMessage(MessageExt msg,MessageWraper messageWraper) throws Exception {
        boolean autonInvoke = false;
        if(invoker != null){
            autonInvoke =invoker.invoke(messageWraper);
        }
        if(!autonInvoke){
            this.publisher.publishEvent(new TargetEvent(msg,messageWraper));
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

    public final  <T extends MessageWraper> T    paserMessage(MessageExt msg,Class<T> resultClazz) throws Exception {
        if (msg == null) {
            throw new Exception("消息为空");
        }
        String msgContent = new String(msg.getBody(), "utf-8");
        if (StringUtils.isBlank(msgContent)) {
            throw new Exception("消息为空");
        }
        T messageWraper = JSON.parseObject(msgContent, resultClazz);
        messageWraper.setMsgId(msg.getKeys());
        return messageWraper;
    }


}
