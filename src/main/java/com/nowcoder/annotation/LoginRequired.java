package com.nowcoder.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 注解书写在方法之上，用于描述方法
@Target(ElementType.METHOD)
// 声明注解的有效时机（程序运行时有效）
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {
}
