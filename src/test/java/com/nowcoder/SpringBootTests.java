package com.nowcoder;

import com.nowcoder.entity.DiscussPost;
import com.nowcoder.service.DiscussPostService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.Assert;

import java.util.Date;


/**
 * @Author Xiao Guo
 * @Date 2023/5/30
 */
@SpringBootTest
// 启用SpringBoot的那个正常运行的类作为配置类来运行项目
@ContextConfiguration(classes = CommunityApplication.class)
public class SpringBootTests {

    @Autowired
    private DiscussPostService discussPostService;

    // 成员变量来存储数据
    private DiscussPost data;

    // 修饰的是静态方法
    // ①所有测试方法执行前的操作，需要执行的代码块或方法。 ②静态方法，只会执行一次
    // 通常用来初始化数据
    @BeforeAll
    public static void beforeClass() {
        System.out.println("beforeClass");
    }

    // 修饰的是静态方法
    // ①所有测试方法执行之后的操作，需要执行的代码块或方法。 ②静态方法，只会执行一次
    // 通常用来销毁数据
    @AfterAll
    public static void afterClass() {
        System.out.println("afterClass");
    }

    // 修饰的是实例方法
    // ①用于标记在每个测试方法执行之前需要执行的代码块或方法。②该注解的方法在每个测试方法执行前都会执行。
    @BeforeEach
    public void before() {
        System.out.println("before");

        // 初始化测试数据
        data = new DiscussPost();
        data.setUserId(112);
        data.setTitle("Test Title");
        data.setContent("Test Content");
        data.setCreateTime(new Date());

        discussPostService.addDiscussPost(data);

    }

    // 实例方法
    // ①用于标记在每个测试方法执行之后需要执行的代码块或方法。②该注解的方法在每个测试方法执行后都会执行。
    @AfterEach
    public void after() {
        System.out.println("after");

        // 删除测试数据
        // 不是真的删除，而是修改帖子的状态
        discussPostService.updateStatus(data.getId(), 2);
    }

    // 测试方法
    @Test
    public void test1() {
        System.out.println("test1");
    }

    // 测试方法
    @Test
    public void test2() {
        System.out.println("test2");
    }

    @Test
    public void testFindById() {
        DiscussPost post = discussPostService.findDiscussPostById(data.getId());

        // 使用断言语句来判断测试方法的正确性
        // 查到的结果过和初始化的数据是否一致
        // 使用断言Assertions
        // 这些方法可以在测试方法中使用，用于对实际结果进行断言验证，确保测试的正确性。
        // 如果断言失败，将会抛出AssertionError异常，并提供相应的错误信息。
        Assertions.assertNotNull(post); // 验证对象是否为null。

        // 对象已经重写toString方法，可以使用断言来判断两个对象的toString()方法返回的字符串是否相等
//        Assertions.assertEquals(data, post); // 验证两个值是否相等。
        Assertions.assertEquals(data.getTitle(), post.getTitle());
        Assertions.assertEquals(data.getContent(), post.getContent());
    }

    @Test
    public void testUpdateScore() {
        // 修改分数
        int rows = discussPostService.updateScore(data.getId(), 2000.00);
        Assertions.assertEquals(1, rows);

        DiscussPost post = discussPostService.findDiscussPostById(data.getId());
        // 使用断言进行浮点数比较，精确到小数点后两位。
        Assertions.assertEquals(2000.00, post.getScore(), 2);
    }

}
