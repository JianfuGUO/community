package com.nowcoder.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * @Author Xiao Guo
 * @Date 2023/2/24
 */

@Component
public class MailClient {
    // 日志信息
    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);

    // @Autowired(required=false)：表示忽略当前要注入的bean，如果有直接注入，没有跳过，不会报错。
    // @Autowired(required = false)
    @Resource
    private JavaMailSender MailSender;

    // 从 .yml 配置文件里面获取某一行的属性值
    @Value("${spring.mail.username}")
    private String from;

    public void sendMail(String to,String subject,String content){

        try {
            // 创建模板对象
            MimeMessage message = MailSender.createMimeMessage();

            // 帮助构建模板里面的内容
            MimeMessageHelper helper = new MimeMessageHelper(message);
            // 设置发件人信息
            helper.setFrom(from);
            // 收件人
            helper.setTo(to);
            // 主题
            helper.setSubject(subject);
            // true: html类型的文本
            // false: 普通文本
            helper.setText(content,true);

            // 发送邮件
            MailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            logger.error("发送邮件失败" + e.getMessage());
        }


    }






}
