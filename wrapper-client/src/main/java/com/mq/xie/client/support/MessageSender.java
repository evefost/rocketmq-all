package com.mq.xie.client.support;

import com.alibaba.fastjson.JSON;
import com.mq.xie.client.pojo.MessageWraper;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;


public class MessageSender implements InnerMessageSender {

    private Logger logger = LoggerFactory.getLogger(MessageSender.class);

    private DefaultMQProducer defaultProducer;

    public MessageSender (DefaultMQProducer defaultProducer){
        this.defaultProducer = defaultProducer;
    }


    @Override
    public void sendTransactionEvent(MessageWraper messageWraper){
        sendTransactionEvent(messageWraper,null);
    }

    @Override
    public void sendTransactionEvent(MessageWraper messageWraper, CountDownLatch cdh) {
        logger.info("消息将发往broker");
        Message msg = new Message(messageWraper.getTopic(),
                messageWraper.getTag(),
                messageWraper.getMsgId(),
                JSON.toJSONString(messageWraper).getBytes());
        try {
            if(messageWraper.getOrderId()!=null){
                defaultProducer.send(msg,
                        (mqs, msg1, arg) -> {
                            String id = (String) arg;
                            int index = Math.abs(id.hashCode()) % mqs.size();
                            return mqs.get(index);
                        },
                        messageWraper.getOrderId(),
                        new SendCallback() {
                            @Override
                            public void onSuccess(SendResult sendResult) {
                                if(cdh!=null){
                                    cdh.countDown();
                                }
                            }
                            @Override
                            public void onException(Throwable e) {

                                if(cdh!=null){
                                    cdh.countDown();
                                }
                            }
                        });
            }else {
                defaultProducer.send(msg, new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        if (cdh != null) {
                            cdh.countDown();
                        }
                    }

                    @Override
                    public void onException(Throwable e) {
                        if (cdh != null) {
                            cdh.countDown();
                        }
                    }
                });
            }
        } catch (Exception e) {
            if(cdh!=null){
                cdh.countDown();
            }
            logger.error("发送事务消息失败:{}\n {}", JSON.toJSONString(messageWraper),e);
        } finally {


        }
    }


}
