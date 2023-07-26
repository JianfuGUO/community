package com.nowcoder.controller.interceptor;

import com.nowcoder.entity.User;
import com.nowcoder.service.MessageService;
import com.nowcoder.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @Author Xiao Guo
 * @Date 2023/4/30
 */
@Component // 交给容器来处理
public class MessageInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    // 未读消息数量
    @Autowired
    private MessageService messageService;

    // 在 Controller 之后执行
    // 比上面多了 modelAndView 变量，主要的业务逻辑已经实现，现在处理模板引擎
    // 在渲染模板引擎之前执行
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 获取当前用户
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            // 所有的私信数量
            int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
            // 所有的通知数量
            int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);

            // 总的未读数量
            modelAndView.addObject("allUnreadCount", letterUnreadCount + noticeUnreadCount);
        }
    }
}
