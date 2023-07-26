package com.nowcoder.config;

import com.nowcoder.controller.interceptor.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author Xiao Guo
 * @Date 2023/3/3
 */

// 之前的配置类里面主要是为装配第三方的Bean
// @Bean
// 配置拦截器要求实现一个接口，而不是简单装配一个 Bean
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // 注入拦截器
    @Autowired
    private AlphaInterceptor alphaInterceptor;

    // 注入 LoginTicketIntercepter 拦截器
    @Autowired
    private LoginTicketIntercepter loginTicketIntercepter;

    // 注入检查登录状态
//    @Autowired
//    private LoginRequiredIntercepter loginRequiredIntercepter;

    // 注入统计UV和DAU的拦截器
    @Autowired
    private DataInterceptor dataInterceptor;

    // 注入总的未读数量拦截器
    @Autowired
    private MessageInterceptor messageInterceptor;

    // 重写注册接口的方法
    // 排除静态资源
    // /**/*.css表示所有访问路径下的.css文件访问路径都过滤掉
    // 指定拦截路径 addPathPatterns，不指定就是拦截所有路径，排除上面静态资源的路径
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(alphaInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg")
                .addPathPatterns("/register", "/login");

        // 配置 LoginTicketIntercepter 拦截器
        // 所有请求路径都拦截
        registry.addInterceptor(loginTicketIntercepter)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");

        // 配置 loginRequiredIntercepter 拦截器
        // 所有请求路径都拦截
//        registry.addInterceptor(loginRequiredIntercepter)
//                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");

        // 配置 messageInterceptor 拦截器
        // 所有请求路径都拦截
        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");

        // 配置 dataInterceptor 拦截器
        // 所有请求路径都拦截，静态资源不拦截
        registry.addInterceptor(dataInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
    }
}
