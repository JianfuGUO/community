package com.nowcoder.controller;

import com.nowcoder.annotation.LoginRequired;
import com.nowcoder.entity.User;
import com.nowcoder.service.FollowService;
import com.nowcoder.service.LikeService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.CommunityConstant;
import com.nowcoder.util.CommunityUtil;
import com.nowcoder.util.HostHolder;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.sun.org.apache.xpath.internal.operations.Mod;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 账号设置
 *
 * @Author Xiao Guo
 * @Date 2023/3/4
 */

@Controller
@RequestMapping(path = "/user")
public class UserController implements CommunityConstant {
    // 引入日志
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    // 从配置文件 .yml 里面获取名称
    // 上传路径
    @Value("${community.path.upload}")
    private String uploadPath;

    // 注入域名
    @Value("${community.path.domain}")
    private String domain;

    // 注入配置文件里面的项目访问路径属性
    @Value("${server.servlet.context-path}")
    private String contextPath;

    // 注入业务逻辑层 service
    @Autowired
    private UserService userService;

    // 获取当前用户是谁
    @Autowired
    private HostHolder hostHolder;

    // 获取点赞数量
    @Autowired
    private LikeService likeService;

    // 获取关注相关信息
    @Autowired
    private FollowService followService;

    // 注入 properties 配置文件里面自定义的配置参数名
    // 身份有关
    @Value("${qiniu.key.access}")
    private String accessKey;

    // 内容加密有关
    @Value("${qiniu.key.secret}")
    private String secretKey;

    // 上传空间
    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${quniu.bucket.header.url}")  //七牛云对外暴露的图片链接，不是上传的url
    private String headerBucketUrl;

    // 自定义注解设置拦截器限制访问
    @LoginRequired
    // 个人头像里面的点击账号设置，使用此 controller 跳转到该页面，提交要修改的头像等数据
    @GetMapping(path = "/setting")
    // 默认返回值类型为 html
    public String getSettingPage(Model model) {
        // 上传文件名称
        // 每次图像名称不一致，防止同名覆盖和服务器缓存，无法记录原始数据
        String fileName = CommunityUtil.generateUUID();

        // 设置响应信息
        // 七牛云规定的响应代码
        StringMap policy = new StringMap(); // 本质为map集合
        // 客户端采取异步的方式传递信息，返回JSON字符串，是否成功
        policy.put("returnBody", CommunityUtil.getJSONString(0));

        // 生成上传凭证
        Auth auth = Auth.create(accessKey, secretKey);
        // 参数：（上传空间，文件名，过期时间3600秒，响应信息）
        String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);

        model.addAttribute("uploadToken", uploadToken);
        model.addAttribute("fileName", fileName);

        return "/site/setting";
    }

    // 更新头像路径
    // 头像上传到七牛云服务器之后，更新头像访问路径为七牛云的访问路径
    @PostMapping(path = "/header/url")
    @ResponseBody
    public String updateHeaderUrl(String fileName) {

        // 判断是否为空
        if (StringUtils.isBlank(fileName)) {
            return CommunityUtil.getJSONString(1, "文件名不能为空");
        }

        // 头像图片的访问路径
        String url = headerBucketUrl + "/" + fileName;
        userService.updateHeader(hostHolder.getUser().getId(), url);

        return CommunityUtil.getJSONString(0);
    }

    /**
     * 废弃
     *
     * @param headerImage
     * @param model
     * @return
     */
    // 自定义注解设置拦截器限制访问
    @LoginRequired
    // 处理上传头像的逻辑
    // 上传的时候表单的提交方式必须为 post
    // 使用的参数为 SpringMVC 为我们提供的 MultipartFile 类
    // 声明变量类型 MultipartFile、Model 之后 SpringMVC自动注入
    @PostMapping(path = "/upload")
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            // 给页面添加提示信息
            model.addAttribute("error", "您还没有选择图片！");
            // 返回提交头像的页面
            return "/site/setting";
        }

        // 上传文件
        // 存取时未防止图片的名称重复，不使用原始上传的文件名，使用==”随即名称 + . + 原始图像的后缀名“==
        // 获取原始的文件名
        String fileName = headerImage.getOriginalFilename();

        // 截取文件的后缀名
        // fileName.lastIndexOf(".") 会找到文件名中最后一个 "." 出现的位置
        // substring() 方法从该位置开始截取字符串直到字符串末尾，就得到了文件类型后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            // 给页面添加提示信息
            model.addAttribute("error", "文件的格式不正确！");
            // 返回提交头像的页面
            return "/site/setting";
        }

        // 生成随机的文件名
        String filename = CommunityUtil.generateUUID() + suffix;
        // 确定文件存放的路径
        File dest = new File(uploadPath + "/" + filename);
        try {
            // 存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            // 记录日志
            logger.error("上传文件失败：" + e.getMessage());
            // 抛出异常，打断程序
            throw new RuntimeException("上传文件失败，服务器发生异常！", e);
        }

        // 更新当前用户的头像路径（web访问路径）
        // http://localhost:8080/community/user/header/xxx.png
        // 获取当前用户
        User user = hostHolder.getUser();
        // 当前用户的头像路径（web访问路径）
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(), headerUrl);

        // 从定向到首页的访问路径，相当于重新发起一次请求
        return "redirect:/index";
    }

    /**
     * 废弃
     *
     * @param filename
     * @param response
     */
    // 获取头像
    // 在 index.html 里面，<img th:src="${loginUser.headerUrl}" class="rounded-circle" style="width:30px;"/>
    // 浏览器会发送一个的 web 路径的请求给服务器获取资源
    // 所以在使用th:src加载外部资源文件时，浏览器会像普通的HTML标签一样，发送一个新请求获取该资源文件。
    @GetMapping(path = "/header/{filename}")
    public void getHeader(@PathVariable("filename") String filename, HttpServletResponse response) {

        // 自定义的输入流
        FileInputStream fis = null;

        // 服务器中图像存放路径
        filename = uploadPath + "/" + filename;
        // 文件后缀
        String suffix = filename.substring(filename.lastIndexOf(".") + 1);
        // 给浏览器响应图片
        response.setContentType("image/" + suffix);

        try {
            // 二进制（字节流）
            // 获取输出流
            ServletOutputStream os = response.getOutputStream();
            // 获取输入流
            fis = new FileInputStream(filename);
            // 一组一组地读取
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                // 输出数据
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败：" + e.getMessage());
        } finally {
            // 关闭输入流
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 自定义注解设置拦截器限制访问
    @LoginRequired
    // 修改密码
    // Thymeleaf中，只有在表单提交时带有相应名称的参数才会被自动绑定到 Controller 中的方法参数
    // 当表单属性名与Controller请求路径中的方法参数名相同时，Spring MVC可以自动绑定请求参数。
    @PostMapping(path = "/updatePassword")
    public String updatePassword(String oldPassword, String newPassword, String confirmPassword,
                                 Model model, @CookieValue("ticket") String ticket) {
        if (oldPassword == null) {
            model.addAttribute("oldPasswordMsg", "请输入原始密码！");
            // 返回至修改页面
            return "site/setting";
        }

        if (newPassword == null) {
            model.addAttribute("newPasswordMsg", "请输入新密码！");
            // 返回至修改页面
            return "site/setting";
        }

        if (confirmPassword == null) {
            model.addAttribute("confirmPasswordMsg", "请输入新密码！");
            // 返回至修改页面
            return "site/setting";
        }

        // 获取当前线程的 user 对象
        User user = hostHolder.getUser();

        // 判断密码的逻辑
        // 输入的原密码错误
        if (!CommunityUtil.md5(oldPassword + user.getSalt()).equals(user.getPassword())) {
            model.addAttribute("oldPasswordMsg", "输入的原密码错误！");
            // 返回至修改页面
            return "site/setting";
        }

        // 两次输入的密码不一致
        if (!confirmPassword.equals(newPassword)) {
            model.addAttribute("confirmPasswordMsg", "两次输入的密码不一致！");
            // 返回至修改页面
            return "site/setting";
        }

        // 修改密码
        userService.updatePassword(user.getId(), CommunityUtil.md5(newPassword + user.getSalt()));

        // 退出登录
        userService.logout(ticket);

        // 重新登录
        return "redirect:/login";
    }

    // 个人主页
    @GetMapping(path = "/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }

        // 给页面发送用户信息
        model.addAttribute("user", user);
        // 点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        // 给页面发送点赞信息
        model.addAttribute("likeCount", likeCount);

        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);

        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);

        // 是否已关注
        boolean hasFollowed = false;

        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        // 返回模板 .html
        return "site/profile";
    }

}
