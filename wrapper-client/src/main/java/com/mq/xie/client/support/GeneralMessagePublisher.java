package com.mq.xie.client.support;

import com.mq.xie.client.pojo.MessageWraper;
import com.mq.xie.client.pojo.SourceEvent;
import org.springframework.beans.factory.annotation.Value;


public class GeneralMessagePublisher extends AbsMessagePublisher {

    private InnerMessageSender messageSender;

    public GeneralMessagePublisher(InnerMessageSender messageSender) {
        this.messageSender = messageSender;
    }

    @Override
    protected void doSend(SourceEvent sourceEvent) {
        logger.info("发送非事务消息{}:{}", sourceEvent.getTopic(), sourceEvent.getTag());
        MessageWraper wapper = new MessageWraper(sourceEvent);
        messageSender.sendTransactionEvent(wapper);
    }


    @Override
    public void publishTransEvent(SourceEvent sourceEvent) {
        throw new UnsupportedOperationException("不支事务消息");
    }
}
