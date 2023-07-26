package com.nowcoder.controller.interceptor;

import com.nowcoder.entity.User;
import com.nowcoder.service.DataService;
import com.nowcoder.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author Xiao Guo
 * @Date 2023/5/12
 */
@Component // 使用拦截器来对每次访问进行统计
public class DataInterceptor implements HandlerInterceptor {

    // 记录 UV
    @Autowired
    private DataService dataService;

    // 活跃用户记录 id
    @Autowired
    private HostHolder hostHolder;

    // 在 Controller 之前执行
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 统计 UV
        String ip = request.getRemoteHost(); // 获取当前 ip
        dataService.recordUV(ip);

        // 统计 DAU
        User user = hostHolder.getUser(); // 获取当前用户
        if (user != null){
            dataService.recordDAU(user.getId());
        }
        // 请求继续执行
        return true;
    }
}
