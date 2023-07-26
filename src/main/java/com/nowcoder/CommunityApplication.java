package com.nowcoder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

//Spring Boot的核心注解，它用于标记一个主类（Main Class），表示这是一个Spring Boot应用程序的入口点。
@SpringBootApplication
public class CommunityApplication {

    // 在初始化阶段，@PostConstruct修饰的方法会在在属性赋值完成之后自动执行该方法
    @PostConstruct
    public void init() {
        // 解决netty启动冲突问题
        // see Netty4Utils.setAvailableProcessors()
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

    public static void main(String[] args) {
        SpringApplication.run(CommunityApplication.class, args);
    }

}
