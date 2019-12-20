package com.tfswx.my_receive;

import com.tfswx.my_receive.utils.KillPort;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@MapperScan("com.tfswx.my_receive.mapper")
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class MyReceiveApplication {


    public static void main(String[] args) {
        //检查程序端口占用，如果被占用杀掉进程
        new KillPort().check(9528);
        SpringApplication app= new SpringApplication(MyReceiveApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);

    }
}
