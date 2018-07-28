**生产端、消费端**
MQAdmin //顶层接口，创建topic,获取topic 消息等
    MQProducer 生产端接口
        DefaultMQProducer  //生产默认的实现
        TransactionMQProducer //不再支持
    MQConsumer 消费端接口,定义一些发送接口
        MQPullConsumer
            DefaultMQPullConsumer //拉取消息实的
        MQPushConsumer
            DefaultMQPushConsumer 官方建议使用的推送实现
=====================================================
MQConsumerInner
    DefaultMQPushConsumerImpl 消费推送实现
    DefaultMQPullConsumerImpl 消费拉取实现
MQClientInstance 核心处理类：producer 与consumer共用
