package com.nowcoder.controller.interceptor;

import com.nowcoder.annotation.LoginRequired;
import com.nowcoder.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 自定义拦截器
 *
 * @Author Xiao Guo
 * @Date 2023/3/5
 */

@Component
public class LoginRequiredIntercepter implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    // controller 之前执行
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 拦截的是否为方法还是其他例如静态资源等
        if (handler instanceof HandlerMethod) {
            // 初始为 object 类型
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            // 获取 method 对象
            Method method = handlerMethod.getMethod();
            // 获取方法上的注解
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            // 方法需要登陆但是user为Null，即未登录
            if (loginRequired != null && hostHolder.getUser() == null) {

                // 利用 response 重定向到登录页面
                response.sendRedirect(request.getContextPath() + "/login");

                return false;
            }
        }

        // 保证后续接着执行
        return true;
    }
}
