package com.mq.xie.client.support;

import com.mq.xie.client.pojo.SourceEvent;


public interface MessagePublisher {

    /**
     * 普通非事务性消息
     * @param sourceEvent
     */
    void publishEvent(SourceEvent sourceEvent);

    /**
     * 事务性消
     * @param sourceEvent
     */
    void publishTransEvent(SourceEvent sourceEvent);

}
