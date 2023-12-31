package com.nowcoder;

import com.nowcoder.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * @Author Xiao Guo
 * @Date 2023/3/5
 */
@SpringBootTest
// 启用SpringBoot的那个正常运行的类作为配置类来运行项目
@ContextConfiguration(classes = CommunityApplication.class)
public class SensativeTest {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitiveFilter() {
        String text = "这里可以赌博，可以嫖娼，可以开票，可以吸毒，哈哈哈！！";
        text = sensitiveFilter.filter(text);
        System.out.println(text);
        text = "这里可以▲赌▲博▲，可以▲嫖▲娼▲，可以▲开▲票▲，可以▲吸▲毒▲，哈哈哈！！";
        text=sensitiveFilter.filter(text);
        System.out.println(text);
    }
}
