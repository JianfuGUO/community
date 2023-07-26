package com.nowcoder.controller;

import com.nowcoder.service.AlphaService;
import com.nowcoder.util.CommunityConstant;
import com.nowcoder.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * 演示一些 demo
 *
 * @Author Xiao Guo
 * @Date 2023/2/18
 */

@Controller
@RequestMapping("/alpha")//一级访问路径
public class AlphaController {

    // controller 依赖于 service
    @Autowired
    private AlphaService alphaService;

    @RequestMapping("/hello")//二级访问路径
    @ResponseBody // 直接返回数据给浏览器
    public String sayHello() {
        return "hello Spring Boot";
    }

    @RequestMapping("/data")//二级访问路径
    @ResponseBody // 直接返回数据给浏览器
    // 模拟查询请求
    public String getData() {
        return alphaService.find();
    }

    // 底层的处理请求和响应的方式
    @RequestMapping("/http")
    // 声明了这两个参数之后，Spring MVC 中的 DispatcherServlet（中央控制器）自动把request和response对象传给你
    public void http(HttpServletRequest request, HttpServletResponse response) {
        // 获取请求数据
        // 获取请求方式
        System.out.println(request.getMethod());
        // 获取请求路径
        System.out.println(request.getServletPath());
        // 获取所有的请求行(返回的是一个很老的迭代器)
        Enumeration<String> enumeration = request.getHeaderNames();
        // 遍历此迭代器
        while (enumeration.hasMoreElements()) {
            // key
            String key = enumeration.nextElement();
            // 获取 value
            String value = request.getHeader(key);
            System.out.println(key + ":" + value);
        }
        // 获取请求参数
        // ?code=123&name=zhangsan
        System.out.println(request.getParameter("code"));
        System.out.println(request.getParameter("name"));


        // 返回响应数据
        // 设置返回数据的类型
        response.setContentType("text/html;charset=utf-8");

        try {
            // 获取响应的输出流
            PrintWriter writer = response.getWriter();
            writer.write("<h1>牛客网</h1>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // -------------------------------------处理请求------------------------------------------//
    // 简便的处理请求
    // （1）接收请求数据
    // GET请求:从浏览器获取数据
    // ① ？拼参数： /students?current=1&limit=20
    @RequestMapping(path = "/students", method = RequestMethod.GET) // 限制请求方式为GET
    @ResponseBody //简单返回字符串
    // 没有参数就默认值
    public String getStudents(
            @RequestParam(name = "current", required = false, defaultValue = "1") int current,
            @RequestParam(name = "limit", required = false, defaultValue = "1") int limit) {
        System.out.println(current);
        System.out.println(limit);
        return "some students";
    }

    // ②参数为请求路径的一部分： /student/123
    @RequestMapping(path = "/student/{id}", method = RequestMethod.GET) // 限制请求方式为GET
    @ResponseBody //简单返回字符串
    // 没有参数就默认值
    public String getStudent(@PathVariable("id") int id) {
        System.out.println(id);
        return "a student";
    }

    // ---------------------------测试时先访问表单http://localhost:8080/community/html/student.html-------------------------------------------- //
    // POST请求：向服务器提交数据
    @RequestMapping(path = "/student", method = RequestMethod.POST)
    @ResponseBody // 直接给浏览器返回数据
    public String saveStudent(String name, int age) {
        System.out.println(name);
        System.out.println(age);
        return "success";
    }
    // ---------------------------------------------------------------------------- //


    // -------------------------------------响应数据的处理--------------------------------------- //
    // 响应数据的处理
    // 响应html数据（动态数据）
    // 方式一：
    @RequestMapping(path = "/teacher", method = RequestMethod.GET)
    // 不书写@ResponseBody则返回的是html数据
    // 返回值的类型是ModelAndView(两份数据)
    public ModelAndView getTeacher() {
        ModelAndView modelAndView = new ModelAndView();
        // 模板里面含有多少变量，就add多少数据
        modelAndView.addObject("name", "张三");
        modelAndView.addObject("age", "23");

        // 设置模板的路径和名字(view相当于view.html)
        modelAndView.setViewName("/demo/view");
        return modelAndView;
    }

    // 响应html数据（动态数据）
    // 方式二：(简化方式一)
    @RequestMapping(path = "/school", method = RequestMethod.GET)
    // 返回值为String (返回值为View的路径)
    // DispatcherServlet调用此方法时将model对象传给你
    public String getSchool(Model model) {
        model.addAttribute("name", "东南大学");
        model.addAttribute("age", 120);
        return "/demo/view";
    }

    // 响应JSON数据（异步请求）
    // Java对象 -> JSON字符串 -> JS对象(JavaScript 浏览器端)
    // 返回1个Json对象
    @RequestMapping(path = "/emp", method = RequestMethod.GET)
    @ResponseBody // 加上这个指定返回值为String,默认返回html类型数据
    public Map<String, Object> getEmp() {
        Map<String, Object> emp = new HashMap<>();
        emp.put("name", "张三");
        emp.put("age", 32);
        emp.put("salary", 8000.00);

        return emp;
    }

    // 返回多个Json对象
    @RequestMapping(path = "/emps", method = RequestMethod.GET)
    @ResponseBody // 加上这个指定返回值为String, 默认返回html类型数据
    public List<Map<String, Object>> getEmps() {
        List<Map<String, Object>> list = new ArrayList<>();

        Map<String, Object> emp1 = new HashMap<>();
        emp1.put("name", "张三");
        emp1.put("age", 32);
        emp1.put("salary", 8000.00);
        list.add(emp1);

        Map<String, Object> emp2 = new HashMap<>();
        emp2.put("name", "李四");
        emp2.put("age", 28);
        emp2.put("salary", 6000.00);
        list.add(emp2);

        Map<String, Object> emp3 = new HashMap<>();
        emp3.put("name", "王五");
        emp3.put("age", 50);
        emp3.put("salary", 18000.00);
        list.add(emp3);

        return list;
    }

    // Cookie 示例
    @RequestMapping(path = "/cookie/set", method = RequestMethod.GET)
    @ResponseBody // 加上这个指定返回值为String, 默认返回html类型数据
    public String setCookie(HttpServletResponse response) {
        // 创建 Cookie
        // 以恶搞 Cookie 对象只能存一对 key-value，而且没有无参构造器
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());
        // 设置生效方法
        // 指定哪些路径有效（不指定则所有请求路径都会携带Cookie 数据，影响网络资源占用）
        // 此路径及其子路径有效
        cookie.setPath("/community/alpha");
        // 设置 Cookie 的生存时间(单位秒)
        cookie.setMaxAge(60 * 10); //10 分钟
        // 发送 Cookie
        response.addCookie(cookie);

        return "set cookie";
    }

    // 测试生成 cookie之后，浏览器再次访问服务器会将 cookie 发送给服务器
    // 注意有效路径而且此次cookie数据是在 request Headers 里面
    @RequestMapping(path = "/cookie/get", method = RequestMethod.GET)
    @ResponseBody // 加上这个指定返回值为String, 默认返回html类型数据
    // @CookieValue("code")指定获取哪一个Cookie,@CookieValue(key)
    public String getCookie(@CookieValue("code") String code) {
        System.out.println(code);
        return "get cookie";
    }

    // session 示例
    @RequestMapping(path = "/session/set", method = RequestMethod.GET)
    @ResponseBody
    // 声明变量类型之后 SpringMVC自动注入
    public String setSession(HttpSession session) {
        // session 一直存在于服务端，存什么数据都行
        session.setAttribute("id", 1);
        session.setAttribute("name", "Test");
        return "set session";
    }

    @RequestMapping(path = "/session/get", method = RequestMethod.GET)
    @ResponseBody
    // 声明变量类型 HttpSession 之后 SpringMVC自动注入
    public String getSession(HttpSession session) {
        // session 一直存在于服务端，存什么数据都行
        System.out.println(session.getAttribute("id"));
        System.out.println(session.getAttribute("name"));
        return "get session";
    }


    // ajax 示例
    @PostMapping(path = "/ajax")
    @ResponseBody // 返回JSON字符串给浏览器
    public String testAjax(String name,int age) {
        System.out.println(name);
        System.out.println(age);
        // 返回 json 字符串
        return CommunityUtil.getJSONString(0,"操作成功！");
    }


}