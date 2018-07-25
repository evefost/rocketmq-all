package com.mq.xie.client.pojo;

import com.alibaba.fastjson.JSON;
import com.mq.xie.client.config.MessageStatus;

import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.UUID;

import static com.mq.xie.client.config.MessageStatus.NOT_SEND;


public class MessageWraper implements Serializable {

    public MessageWraper(){
        this.status = NOT_SEND;
        this.sendErrTimes = 0;
        this.sendTime = new Date();
        try {
            InetAddress addr = Inet4Address.getLocalHost();
            String ip = addr.getHostAddress().toString();
            this.sendIp = ip;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public MessageWraper(String topic,String tag,String data){
        this.topic = topic;
        this.tag = tag;
        this.data = data;
        this.msgId = UUID.randomUUID().toString();
        this.status = NOT_SEND;
        this.sendErrTimes = 0;
        this.sendTime = new Date();
        try {
            InetAddress addr = Inet4Address.getLocalHost();
            String ip = addr.getHostAddress().toString();
            this.sendIp = ip;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public MessageWraper(MessageWraper messageWraper){
        this.transactionId = messageWraper.transactionId;
        this.msgId = messageWraper.msgId;
        this.topic = messageWraper.topic;
        this.tag = messageWraper.tag;
        this.status = messageWraper.status;
        this.data = messageWraper.data;
        this.sendErrTimes = messageWraper.sendErrTimes;
        this.sendTime = messageWraper.sendTime;
        this.sendIp = messageWraper.sendIp;
        this.orderId = messageWraper.getOrderId();
    }

    public MessageWraper(SourceEvent sourceEvent){
        this.transactionId = sourceEvent.getTransactionId();
        this.msgId = sourceEvent.getEventId();
        this.topic = sourceEvent.getTopic();
        this.tag = sourceEvent.getTag();
        this.data = JSON.toJSONString(sourceEvent.getData());
        this.status = NOT_SEND;
        this.sendErrTimes = 0;
        this.sendTime = new Date();
        this.orderId = sourceEvent.getOrderId();
        try {
            InetAddress addr = Inet4Address.getLocalHost();
            String ip = addr.getHostAddress().toString();
            this.sendIp = ip;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    /**
     * 父级id,标识为同一次发起的事务
     */
    protected String transactionId;

    protected String msgId;

    protected String topic;

    protected String tag;

    protected MessageStatus status=NOT_SEND;

    protected Integer sendErrTimes=0;

    protected String data;

    protected Date sendTime;

    protected String sendIp;

    protected String orderId;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Integer getStatus() {
        return status.getValue();
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public String getMsgId() {
        return msgId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public Integer getSendErrTimes() {
        return sendErrTimes;
    }

    public void setSendErrTimes(Integer sendErrTimes) {
        this.sendErrTimes = sendErrTimes;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public String getSendIp() {
        return sendIp;
    }

    public void setSendIp(String sendIp) {
        this.sendIp = sendIp;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
