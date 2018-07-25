package com.xie.consumer.impl;

import com.alibaba.fastjson.JSON;
import com.xie.beans.User;
import com.mq.xie.client.annotation.Consumer;
import com.mq.xie.client.annotation.Tag;
import com.mq.xie.client.annotation.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Created by xieyang on 18/7/14.
 */
@Service
@Consumer
@Topic(value = "TopicA")
public class ServiceAImpl {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Tag(value = "addUser")
    String addUser2(User user){
        logger.info("server a 消费端  收到addUser: "+ JSON.toJSONString(user));
        return "server b收到添加用户";
    }

    @Tag(value = "addUser2")
    String addUser(User user){
        logger.info("server b消费端  收到addUser2: "+ JSON.toJSONString(user));
       return "server b收到添加用户2";
    }

}
