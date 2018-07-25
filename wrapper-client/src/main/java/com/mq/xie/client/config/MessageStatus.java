package com.mq.xie.client.config;

public enum MessageStatus {

    NOT_SEND(0),
    SEND_SUCCESS(1),
    SEND_FAIL(2),
    SEND_OVER_FAIL(3)

    ;

    private Integer value;

    MessageStatus(Integer value){
        this.value = value;
    }

    public static MessageStatus getMessageStatusByValue(Integer value){
        MessageStatus[] messageStatuses = MessageStatus.values();
        for (MessageStatus messageStatus : messageStatuses){
            if(messageStatus.value==value){
                return messageStatus;
            }
        }
        return null;
    }

    public Integer getValue(){
        return value;
    }

}
