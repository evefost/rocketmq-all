package com.xie.buz;


import com.mq.xie.client.pojo.SourceEvent;
import com.mq.xie.client.support.MessagePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class ServiceC {

    private Logger logger = LoggerFactory.getLogger(ServiceC.class);


    Random r = new Random();

    @Autowired
    private MessagePublisher publisher;


    public void doAdd()  {
        //do something
        logger.info("执行C服务===================");
        //保存事务信息
        SourceEvent doAdd = new SourceEvent(this);
        doAdd.setTopic("TopicTest1");//
        doAdd.setTag("TagA");
        doAdd.setData("这是c的测试内容");
        //publisher.publishEvent(doAdd);
//        if(r.nextBoolean()){
//            logger.info("C抛出异常============");
//            throw new RuntimeException("C故意抛出异常");
//        }
    }

}
