package com.nowcoder.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * @Author Xiao Guo
 * @Date 2023/3/14
 */

//@Component // Spring 管理的组件
//@Aspect // 切面组件
public class AlphaAspect {

    // 切点
    // 新的功能
    // 切点：返回值(一切返回值) 包 类(所有) 方法名(所有) 参数
    @Pointcut("execution(* com.nowcoder.service.*.*(..))")
    public void pointcut() {

    }

    // 五种通知
    // 切点之前添加方法
    @Before("pointcut()")
    public void before(){
        System.out.println("before");
    }

    // 切点之后添加方法
    @After("pointcut()")
    public void after(){
        System.out.println("after");
    }

    // 返回值之后
    @AfterReturning("pointcut()")
    public void afterReturning(){
        System.out.println("afterReturning");
    }

    // 抛出异常之后
    @AfterThrowing("pointcut()")
    public void afterThrowing(){
        System.out.println("afterThrowing");
    }

    // 前后都植入
    // 要有返回值
    // ProceedingJoinPoint 为切入点位置
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        // 之前植入的方法
        System.out.println("around before");
        Object obj = proceedingJoinPoint.proceed();
        // 之后植入的方法
        System.out.println("around after");
        return obj;
    }

}
