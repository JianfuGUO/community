package com.nowcoder.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.entity.User;
import com.nowcoder.service.UserService;
import com.nowcoder.util.CommunityConstant;
import com.nowcoder.util.CommunityUtil;
import com.nowcoder.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 注册登录功能
 *
 * @Author Xiao Guo
 * @Date 2023/2/24
 */
@Controller
// 实现此接口来使用七定义的常量
public class LoginController implements CommunityConstant {

    // 创建日志对象
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    // 注入注册账号的 Service 层
    @Autowired
    private UserService userService;

    // 注入生成的验证码工具
    @Autowired
    private Producer kaptchaProducer;

    // 注入配置文件里面的项目访问路径属性
    @Value("${server.servlet.context-path}")
    private String contextPath;

    // 高度封装的“RedisTemplate”类，操作数据进行redis库的存取
    @Autowired
    private RedisTemplate redisTemplate;

    // @GetMapping("/register")
    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    // @GetMapping("/login")
    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    //@PostMapping("/register")
    // -------------------- 提交表单为Post请求方式--------------------------------------//
    @RequestMapping(path = "/register", method = RequestMethod.POST)
    // Model:存储数据，携带给模板
    // User:声明一个User对象，接收提交表单的数据，MVC会自动注入User对象里面与表单名称一直的变量
    public String register(Model model, User user) {

        Map<String, Object> map = userService.register(user);

        // map 为空，表示注册成功
        if (map == null || map.isEmpty()) {
            // /site/operate-result.html 里面的提示信息
            model.addAttribute("msg", "注册成功,我们已经向您的邮箱发送了一封激活邮件，请尽快激活!");
            // /site/operate-result.html 最后内置有自动跳转的js语句，最终跳转到首页
            model.addAttribute("target", "/index");
            // 此次访问要渲染的模板
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));

            // 返回注册页面
            return "/site/register";
        }
    }

    // 点击邮件里面的超链接，浏览器回发送一个新的请求
    // ---------------------新的请求，新的页面------------------------------//
    // http://localhost:8080/community/activation/101/code
    // @PathVariable:从路径中获取参数
    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {

        int result = userService.activation(userId, code);

        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功，您的账号已经可以正常使用了!");
            model.addAttribute("target", "/login");
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作，该账号已经激活过了!");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败，您提供的激活码不正确!");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    // 生成验证码
    // ----- 给浏览器响应的是特殊的东西，不是字符串，也不是网页，需要自己利用 response 对象给浏览器手动输出 ----- //
    // 生成验证码之后，服务端需要将其记住，在多个请求要使用
    // 声明变量类型 HttpServletResponse、HttpSession 之后 SpringMVC自动注入
    @GetMapping(path = "/kaptcha")
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/) {
        // 生成验证码
        String text = kaptchaProducer.createText();
        // 生成图片
        BufferedImage image = kaptchaProducer.createImage(text);

        // 将验证码存入 session
        // session.setAttribute("kaptcha", text);

        // 验证码的归属
        String kaptchaOwner = CommunityUtil.generateUUID();
        // 需要将这个凭证发送给客户端，客户端也需要这个凭证
        // 创建 cookie 对象 （key, value）
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        // cookie的存活时间 s
        cookie.setMaxAge(60);
        // cookie的有效路径----整个项目的访问路径
        cookie.setPath(contextPath);
        // 将其发送到客户端
        response.addCookie(cookie);
        
        // 将验证码存入redis
        // 先获取redisKey
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        // 存值-----redisValue的形式---String
        redisTemplate.opsForValue().set(redisKey,text,60, TimeUnit.SECONDS);


        // 将图片输出给浏览器
        response.setContentType("image/png");
        try {
            // 获取输出流
            OutputStream os = response.getOutputStream();
            // 输出图片
            ImageIO.write(image, "png", os);
            // response 由 SpringMVC 来维护，自动会关闭流
        } catch (IOException e) {
            logger.error("响应验证码失败：" + e.getMessage());
        }
    }

    // 5.开发登录、退出功能
    // 表单提交，Post请求
    // @CookieValue表示从request里面携带的cookie中取出对应key的值
    @PostMapping(path = "/login")
    public String login(String username, String password, String code, boolean rememberme,
                        Model model, /*HttpSession session,*/ HttpServletResponse response,
                        @CookieValue("kaptchaOwner") String kaptchaOwner) {
        // 从session里面去除验证码
        // String kaptcha = (String) session.getAttribute("kaptcha");

        // 从redis里面取值
        String kaptcha = null;

        if (StringUtils.isNotBlank(kaptchaOwner)){
            // 获取 redis 的 key
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            // 根据 redis 的 key 获取 value 值
            // Object 类转换成 String
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }

        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确！");
            // 默认返回html页面
            return "site/login";
        }

        // 检查账号、密码(service 层已处理)
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        // service 层的业务逻辑
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")){

            // cookie 的 key-value 必须都是字符串
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            // cookie 的有效路径
            // 指定哪些路径有效（不指定则所有请求路径都会携带Cookie 数据，影响网络资源占用）
            // 此路径及其子路径有效
            cookie.setPath(contextPath);
            // 设置 cookie 的有效时间
            cookie.setMaxAge(expiredSeconds);
            // 将cookie 响应给页面
            response.addCookie(cookie);

            // 重定向到首页
            return "redirect:/index";
        }else {

            // 当map里面没有这个键时，获取到的为null，不影响
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            // 返回到登陆页面
            return "/site/login";
        }
    }

    // 退出登录
    @GetMapping(path = "/logout")
    // @CookieValue 指定具体的 Cookie 名称。
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);

        // ------------------------------ //
        // 退出登录，将保存权限的逻辑也进行清理
        SecurityContextHolder.clearContext();

        return "redirect:/login";
    }

}
