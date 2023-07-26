package com.nowcoder.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

/**
 * @Author Xiao Guo
 * @Date 2023/2/18
 */

@Configuration
public class AlphaConfig {
    // 装配一个第三方的Bean
    @Bean // Bean的名称就是方法名simpleDateFormat
    public SimpleDateFormat simpleDateFormat(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }
}
