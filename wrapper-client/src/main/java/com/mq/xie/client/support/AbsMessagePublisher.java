package com.mq.xie.client.support;

import com.mq.xie.client.config.RocketmqProperties;
import com.mq.xie.client.pojo.SourceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.UUID;


public abstract class AbsMessagePublisher implements MessagePublisher {

   protected final Logger logger = LoggerFactory.getLogger(AbsMessagePublisher.class);

   @Autowired
   protected RocketmqProperties rocketmqProperties;

   @Override
  public final void publishEvent(SourceEvent sourceEvent){
      doBeforePublish(sourceEvent);
      doSend(sourceEvent);
      doAfterPublish(sourceEvent);
   }

   protected abstract void doSend(SourceEvent sourceEvent);


   protected void  doBeforePublish(SourceEvent sourceEvent){

      String msgId = UUID.randomUUID().toString();
      if (StringUtils.isEmpty(sourceEvent.getEventId())) {
         sourceEvent.setEventId(msgId);
      }
      if (StringUtils.isEmpty(sourceEvent.getTopic())) {
          //设置默认主题
         sourceEvent.setTopic(rocketmqProperties.getTopic());
      }
   }

   protected void  doAfterPublish(SourceEvent sourceEvent){

   }


}
