package com.nowcoder.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Author Xiao Guo
 * @Date 2023/5/13
 */
@Configuration
@EnableScheduling // 启用定时任务
@EnableAsync
public class ThreadPoolConfig {

}
