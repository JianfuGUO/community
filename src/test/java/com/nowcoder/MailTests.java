package com.nowcoder;

import com.nowcoder.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;

/**
 * @Author Xiao Guo
 * @Date 2023/2/24
 */

@SpringBootTest
// 启用SpringBoot的那个正常运行的类作为配置类来运行项目
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {
    @Autowired
    private MailClient mailClient;

    // @Autowired(required = false)
    @Resource
    private TemplateEngine templateEngine;

    // 测试普通邮件
    @Test
    public void testTextMail(){
        mailClient.sendMail("1210905939@qq.com","TEST","hello Friday");
    }

    // 测试 html 邮件
    @Test
    public void testHtmlMail(){
        // 存储动态变量
        Context context = new Context();
        context.setVariable("username","Friday");

        // 模板路径和内容
        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);

        // 发送邮件
        mailClient.sendMail("1210905939@qq.com","HTML",content);
    }
}
