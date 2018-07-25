package com.mq.xie.client.pojo;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.ApplicationEvent;

@Data
@EqualsAndHashCode(callSuper=false)
public class TargetEvent extends ApplicationEvent {
    private static final long serialVersionUID = -4468405250074063206L;

    private String topic;
    private String tag;
    private String data;
    private String eventId;
    private String transactionId;

    public TargetEvent(Object source,MessageWraper wraper) {
        super(source);
        this.topic = wraper.getTopic();
        this.tag = wraper.getTag();
        this.data = wraper.getData();
        this.eventId=wraper.getMsgId();
        this.transactionId=wraper.getTransactionId();
    }

    public <T> T getData(Class<T> clazz){
        return JSON.parseObject(data,clazz);
    }
}