package com.nowcoder.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * @Author Xiao Guo
 * @Date 2023/5/19
 */
// 在服务启动的时候创建图片存放路径
@Configuration
public class WkConfig {

    // 注入日志工具
    private static final Logger logger = LoggerFactory.getLogger(WkConfig.class);

    // 从配置文件中注入图片文件的保存路径
    @Value("${wk.image.storage}")
    private String wkImageStorage;

    // 初始化的方法
    // 在下面的示例中，init()方法被标记了@PostConstruct注解，当WkConfig对象创建后，init()方法会被自动调用，执行初始化操作。
    @PostConstruct
    public void init(){
        // 创建WK图片目录
        File file = new File(wkImageStorage);
        if (!file.exists()){
            file.mkdir();
            logger.info("创建wk图片目录：" + wkImageStorage);
        }

    }
}
