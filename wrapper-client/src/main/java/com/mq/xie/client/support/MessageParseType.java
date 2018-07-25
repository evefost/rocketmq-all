package com.mq.xie.client.support;

import com.mq.xie.client.pojo.MessageWraper;

public interface MessageParseType {

     <T extends MessageWraper> Class<T> getMessageType();
}
