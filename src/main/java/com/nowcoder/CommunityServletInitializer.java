package com.nowcoder;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * @Author Xiao Guo
 * @Date 2023/6/3
 */

// tomcat启动时默认加载此类，运行CommunityApplication.java主类（Main Class），表示这是一个Spring Boot应用程序的入口点。
public class CommunityServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(CommunityApplication.class);
    }
}
