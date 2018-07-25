package com.xie;

import com.mq.xie.client.annotation.EnableScanTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication()
@EnableAsync
@EnableScanTopic
public class ServerBApp {
    // 入口
    public static void main(String[] args) {
        System.setProperty("rocketmq.Client.LogLevel","DEBUG");
        SpringApplication.run(ServerBApp.class, args);
    }


}
