package com.xie.producer;


import com.xie.beans.User;
import com.mq.xie.client.annotation.Producer;
import com.mq.xie.client.annotation.Tag;
import com.mq.xie.client.annotation.Topic;
import com.mq.xie.client.annotation.TransMsg;

@Producer
@Topic("TopicA")
public interface InterFaceA {


      String noTag(User user);
    @Tag(value = "addUser")
    String addUser(User user);


    @Tag(value = "addUser2")
    @TransMsg
    void addUser2(User user);
}
