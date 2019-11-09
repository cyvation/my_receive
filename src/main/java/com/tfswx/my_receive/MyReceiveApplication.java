package com.tfswx.my_receive;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import static com.tfswx.my_receive.utils.FileUtil.getIpAddress;

@MapperScan("com.tfswx.my_receive.mapper")
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class MyReceiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyReceiveApplication.class, args);
        System.out.println("文书卷宗目的端启动成功\n" +
                "配置信息查看访问： http://"+getIpAddress()+":5173");
    }
}
