package com.nowcoder.controller.interceptor;

import com.nowcoder.entity.LoginTicket;
import com.nowcoder.entity.User;
import com.nowcoder.service.UserService;
import com.nowcoder.util.CookieUtil;
import com.nowcoder.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @Author Xiao Guo
 * @Date 2023/3/4
 */
//    自定义拦截器，实现 HandlerInterceptor 接口
//    配置路径

@Component
public class LoginTicketIntercepter implements HandlerInterceptor {

    // 注入 service
    @Autowired
    private UserService userService;

    // 注入 ThreadLocal
    @Autowired
    private HostHolder hostHolder;

    // 在请求开始之初将 user 用户存储在线程对应的对象 ThreadLocal 里面
    // 方法参数由接口定义的，不能使用 @CookieValue 直接根据 Cookie 的 key 来获取值
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从 cookie 中获取登录凭证
        String ticket = CookieUtil.getValue(request, "ticket");

        if (ticket != null) {
            // 查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            // 检查凭证是否有效
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                // 根据 longin_ticket 表中的 UserId 来查询 User 的完整信息
                User user = userService.findUserById(loginTicket.getUserId());
                // 在本次请求中持有用户（考虑并发的情况，线程隔离）
                hostHolder.setUser(user);

                // --------------------------------------------------- //
                // 登录之后判断用户的权限
                // 构建用户认证的结果,并存入SecurityContext,以便于Security进行授权.
                // 三个参数：①principal: 主要信息 ②credentials: 证书 ③authorities: 权限
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user, user.getPassword(), userService.getAuthorities(user.getId()));
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }
        }

        // 否则后续方法接着执行
        return true;
    }

    // 在模板渲染之前，将user信息传递给 model
    // 拦截器此方法恰好在请求之后、模板 Template 渲染之前执行，且包含 ModelAndView 对象
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // ThreadLocal 里面获取当前线程里面的 user
        User user = hostHolder.getUser();

        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);
        }
    }

    // 在模板渲染结束之后，将 ThreadLocal 数据清除掉
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();

        // ----------------------------------- //
        // 请求结束，将保存权限的逻辑也进行清理
//        SecurityContextHolder.clearContext();

    }
}
