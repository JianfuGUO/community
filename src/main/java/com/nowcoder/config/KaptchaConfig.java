package com.nowcoder.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * 验证码生成工具配置类
 * @Author Xiao Guo
 * @Date 2023/2/26
 */

@Configuration
public class KaptchaConfig {

    // 装配一个第三方的Bean
    // Spring 容器来管理
    @Bean
    public Producer kaptchaProducer(){
        // 配置文件信息
        Properties properties = new Properties();
        // 图片宽
        properties.setProperty("kaptcha.image.width", "100");
        // 图片高
        properties.setProperty("kaptcha.image.height", "40");
        // 字体大小
        properties.setProperty("kaptcha.textproducer.font.size", "32");
        //字体颜色
        properties.setProperty("kaptcha.textproducer.font.color", "black");
        //文本集合，验证码值从此集合中获取
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        //验证码长度
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        //干扰实现类(防止机器暴力破解)
        properties.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.NoNoise");

        // Producer 接口的实现类
        DefaultKaptcha kaptcha = new DefaultKaptcha();
        // 配置信息
        Config config = new Config(properties);
        kaptcha.setConfig(config);
        return kaptcha;
    }

}
