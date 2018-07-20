 
 **[remoting模块是mq的基础通信模块]**

#### 1.基于netty

#### 2.通信接口 
RemotingService 顶层接口，抽出server及client共有的
  RemotingClient 
  RemotingServer
  NettyRemotingAbstract //抽象层
    NettyRemotingClient //实现
    NettyRemotingServer
  

#### 3 处理命令
RemotingCommand
    