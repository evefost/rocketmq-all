package com.mq.xie.client.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.ApplicationEvent;


@Data
@EqualsAndHashCode(callSuper = false)
public class SourceEvent<T> extends ApplicationEvent {

    private String transactionId;
    private String eventId;
    private String topic;
    private String tag;
    private T data;

    //需要保持顺序消费的需要提供相同的orderId
    private String orderId;

    private boolean isTrans;

    public boolean isTrans() {
        return isTrans;
    }

    public void setTrans(boolean trans) {
        isTrans = trans;
    }

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public SourceEvent(Object source) {
        super(source);
    }

    /**
     * 默认配置主题
     * @param source
     * @param data
     */
    public SourceEvent(Object source, T data) {
        super(source);
        this.data = data;
    }

    public SourceEvent(Object source, String tag, T data) {
        super(source);
        this.tag = tag;
        this.data = data;
    }

    public SourceEvent(Object source,String topic, String tag, T data) {
        super(source);
        this.topic = topic;
        this.tag = tag;
        this.data = data;
    }

    public SourceEvent(Object source, String tag, T data, String orderId) {
        super(source);
        this.tag = tag;
        this.data = data;
        this.orderId = orderId;
    }

}
