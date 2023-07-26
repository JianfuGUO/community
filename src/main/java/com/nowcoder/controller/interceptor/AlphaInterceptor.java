package com.nowcoder.controller.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author Xiao Guo
 * @Date 2023/3/2
 */

//    自定义拦截器，实现 HandlerInterceptor 接口
//    配置路径

@Component
public class AlphaInterceptor implements HandlerInterceptor {

    // 引入日志，方便演示重写方法的执行顺序
    private static final Logger  logger = LoggerFactory.getLogger(AlphaInterceptor.class);

    // 在 Controller 之前执行
    // 拦截请求，此方法中 request 和 response 请求和响应对象都给你，处理你自己的定义的逻辑和响应
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 日志级别设置为debug
        logger.debug("preHandle:" + handler.toString());
        // return ture: 后续接着执行
        // return false: 停止执行
        return true;
    }

    // 在 Controller 之后执行
    // 比上面多了 modelAndView 变量，主要的业务逻辑已经实现，现在处理模板引擎
    // 在渲染模板引擎之前执行
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        logger.debug("postHandle:" + handler.toString());
    }

    // 在模板引擎之后执行
    // 在 TemplateEngine 之后执行
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        logger.debug("afterCompletion:" + handler.toString());
    }
}
