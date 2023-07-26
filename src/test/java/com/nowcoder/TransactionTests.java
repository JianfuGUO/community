package com.nowcoder;

import com.nowcoder.service.AlphaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * @Author Xiao Guo
 * @Date 2023/3/8
 */

@SpringBootTest
// 启用SpringBoot的那个正常运行的类作为配置类来运行项目
@ContextConfiguration(classes = CommunityApplication.class)
public class TransactionTests {

    @Autowired
    private AlphaService alphaService;

    // 测试声明式事务
    @Test
    public void testSave1(){
        Object obj = alphaService.save1();
        System.out.println(obj);
    }

    // 测试编程式事务
    @Test
    public void testSave2(){
        Object obj = alphaService.save2();
        System.out.println(obj);
    }

}
