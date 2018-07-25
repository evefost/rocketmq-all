package com.xie.buz;


import com.mq.xie.client.pojo.SourceEvent;
import com.mq.xie.client.support.MessagePublisher;
import com.xie.beans.User;
import com.xie.producer.InterFaceA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServiceAServiceImpl {



    @Autowired
    private MessagePublisher publisher;



    @Autowired(required = false)
    private InterFaceA interFaceA;

    int i = 0;
    public void noTag(int count) {
        User user = new User();
        user.setAge(count);
        user.setName("发送无tag消息");
        interFaceA.noTag(user);
    }

    public void addUser(int count) {
        User user = new User();
        user.setAge(count);
        user.setName("老王");
        interFaceA.addUser(user);
    }


    public void addUser2(int count) {
        User user = new User();
        user.setAge(count);
        user.setName("老王儿子");
        interFaceA.addUser2(user);
    }


}
