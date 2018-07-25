package com.mq.xie.client.support;

import com.mq.xie.client.pojo.MessageWraper;

import java.util.concurrent.CountDownLatch;

public interface InnerMessageSender {


    void sendTransactionEvent(MessageWraper messageWraper);

    void sendTransactionEvent(MessageWraper messageWraper, CountDownLatch cdh);

}
