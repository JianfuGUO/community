package com.nowcoder.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author Xiao Guo
 * @Date 2023/3/15
 */

@Component
@Aspect
public class ServiceAspect {

    // 实例化 logger
    private static final Logger logger = LoggerFactory.getLogger(ServiceAspect.class);

    // 切点
    // 新的功能
    // 切点：返回值(一切返回值) 包 类(所有) 方法名(所有) 参数
    @Pointcut("execution(* com.nowcoder.service.*.*(..))")
    public void pointcut() {

    }

    // 前置通知，记录日志
    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        // 日志记录格式用户[1.2.3.4（ip）],在[xxx(时间)],访问了[com.nowcoder.community.service.xxx()（方法）]
        // 获取 request 对象
        // ①在使用RequestContextHolder.getRequestAttributes() 方法获取请求上下文对象时，返回值类型是 RequestAttributes，而不是 ServletRequestAttributes。
        // ②RequestAttributes 是一个接口，定义了获取和设置请求属性的方法，而 ServletRequestAttributes 则是 RequestAttributes 接口的实现类，
        // 它继承了 RequestAttributes 接口，并提供了许多额外的方法，用于获取与 HTTP 请求相关的对象，例如 HttpServletRequest、HttpSession 和 ServletContext 等。
        // ③如果不进行强制转换，则只能使用 RequestAttributes 接口中定义的方法，无法使用 ServletRequestAttributes 提供的额外方法。
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null){
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getRemoteHost();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        // 类名 + 方法
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        // %s --- 占位符
        logger.info(String.format("用户[%s],在[%s],访问了[%s].", ip, now, target));
    }
}
