package com.xie.buz;



import com.mq.xie.client.pojo.SourceEvent;
import com.mq.xie.client.support.MessagePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class ServiceB {

    private Logger logger = LoggerFactory.getLogger(ServiceB.class);


    @Autowired
    private ServiceC serviceC;
    @Autowired
    private MessagePublisher publisher;
    Random r = new Random();


    public void doAdd()  {
        //do something
        logger.info("执行B服务===================");
        //保存事务信息
        SourceEvent doAdd = new SourceEvent(this);
        doAdd.setTopic("TopicTest1");//
        doAdd.setTag("TagA");
        doAdd.setData("这是b的测试内容");

        //publisher.publishEvent(doAdd);
//        if(r.nextBoolean()){
//            logger.info("B抛出异常============");
//            throw new RuntimeException("B故意抛出异常");
//        }
    }

}
