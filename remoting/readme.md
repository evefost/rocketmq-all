 
 **[remoting模块是mq的基础通信模块]**

#### 1.基于netty

#### 2.通信接口 
RemotingService 顶层接口，抽出server及client共有的
  RemotingClient 
  RemotingServer
  NettyRemotingAbstract 
    //抽取server与 client 共有的实现，如限流，请求处器处理，响应处理
    NettyRemotingClient 
    NettyRemotingServer
  

#### 3 处理命令
RemotingCommand
    