package com.nowcoder.config;

import com.nowcoder.util.CommunityConstant;
import com.nowcoder.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @Author Xiao Guo
 * @Date 2023/5/9
 */
@Configuration // 配置类
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

    // 忽略的资源路径(resources包下面的静态资源如如html、css、图片等)
    @Override
    public void configure(WebSecurity web) throws Exception {
        // 忽略静态资源的访问
        web.ignoring().antMatchers("/resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 授权
        http.authorizeRequests()
                // 这些访问路径都需要权限
                .antMatchers(
                        "/user/setting",
                        "/user/upload",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow"
                )
                // 以下任意权限
                .hasAnyAuthority(
                        AUTHORITY_USER,
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR
                )
                // -------------------------------------------------------------- //
                // 置顶、加精、删除的权限设置
                // ①置顶、加精需要 AUTHORITY_MODERATOR 权限
                .antMatchers(
                        "/discuss/top",
                        "/discuss/wonderful"
                )
                .hasAnyAuthority(
                        AUTHORITY_MODERATOR
                )
                // 删除  需要  AUTHORITY_ADMIN
                .antMatchers(
                        "/discuss/delete",
                        "/data/**",
                        "/actuator/**"
                )
                .hasAnyAuthority(
                        AUTHORITY_ADMIN
                )
                // 上述请求路径外其他任意请求都允许
                .anyRequest().permitAll()
                // 不利用Security进行csrf检查，不生成token凭证。
                .and().csrf().disable();

        // 权限不够时的处理
        // 普通请求返回页面 -----> exceptionHandling().accessDeniedPage("/denied");
        http.exceptionHandling()
                // 普通请求与异步请求，普通请求返回html页面，异步请求返回JSON数据
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    // 没有登陆
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
                        // 普通请求时发现未登录，重定向到登录页面，强制你登录
                        // 异步请求时发现未登录，返回JSON字符串，给一个页面提示
                        String xRequestedWith = request.getHeader("x-requested-with");

                        // 相等时为异步请求
                        if ("XMLHttpRequest".equals(xRequestedWith)) {

                            response.setContentType("application/plain;charset=utf-8");
                            // 获取字符流
                            PrintWriter writer = response.getWriter();
                            // 调用工具类输出JSON字符串
                            writer.write(CommunityUtil.getJSONString(403, "你还没有登录哦!"));
                        } else {
                            // 同步请求
                            // 重定向到 login 登陆页面
                            response.sendRedirect(request.getContextPath() + "/login");
                        }

                    }
                })
                .accessDeniedHandler(new AccessDeniedHandler() {
                    // 权限不足
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {

                        // 普通请求时发现未登录，重定向到登录页面，强制你登录
                        // 异步请求时发现未登录，返回JSON字符串，给一个页面提示
                        String xRequestedWith = request.getHeader("x-requested-with");

                        // 相等时为异步请求
                        if ("XMLHttpRequest".equals(xRequestedWith)) {

                            response.setContentType("application/plain;charset=utf-8");
                            // 获取字符流
                            PrintWriter writer = response.getWriter();
                            // 调用工具类输出JSON字符串
                            writer.write(CommunityUtil.getJSONString(403, "你没有访问此功能的权限!"));
                        } else {
                            // 同步请求
                            // 重定向到没有权限的页面
                            response.sendRedirect(request.getContextPath() + "/denied");
                        }
                    }
                });

        // Security 底层默认会拦截/logout请求,进行退出处理.
        // 覆盖它默认的逻辑,才能执行我们自己的退出代码.
        http.logout().logoutUrl("/securitylogout");

    }
}
