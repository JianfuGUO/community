package com.nowcoder.controller.advice;

import com.nowcoder.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @Author Xiao Guo
 * @Date 2023/3/14
 */

// Controller 通知，配置类
// annotations = Controller.class：直接扫描所有的Bean，限制扫描范围，只扫描带有 @Controller 注解的 Bean
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    // 注入记录日志的工具
    // self4j
    private static final Logger logger = LoggerFactory.getLogger(Exception.class);

    // 表示处理所有异常的方法
    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {

        // 异常概括
        logger.error("服务器发生异常：" + e.getMessage());
        // 异常栈的详细信息
        for (StackTraceElement element : e.getStackTrace()) {
            logger.error(element.toString());
        }

        // 异步请求还是普通请求------> 利用request判断
        // 普通请求，访问网页，重定向到提示页面
        // 异步请求，返回 JSON 数据
        
        // 获取请求方式
        String header = request.getHeader("x-requested-with");

        // 如果是异步请求的方式
        if ("XMLHttpRequest".equals(header)) {
            // 普通字符串
            response.setContentType("application/plain;charset=utf-8");
            // 输出流
            PrintWriter writer = response.getWriter();
            // 调用工具类将普通字符串转JSON字符串
            writer.write(CommunityUtil.getJSONString(1,"服务器异常！"));
        }else {
            // 普通请求，重定向到错误页面
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
}
