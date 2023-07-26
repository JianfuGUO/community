package com.nowcoder.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author Xiao Guo
 * @Date 2023/3/4
 */
// 使用普通类，静态的方法
public class CookieUtil {

    // 从 request 对象中获取 指定 cookie 里面的值
    public static String getValue(HttpServletRequest request, String name) {
        if (request == null || name == null) {
            throw new IllegalArgumentException("参数为空！");
        }

        // 从 request 对象获取 cookie 数组
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            // 遍历 cookie 数组
            for (Cookie cookie : cookies) {
                // 查找名称为 ticket 的 cookie
                if (cookie.getName().equals(name)) {
                    // 返回值
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
