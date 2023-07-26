package com.nowcoder;

import com.nowcoder.entity.DiscussPost;
import com.nowcoder.service.DiscussPostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;

/**
 * @Author Xiao Guo
 * @Date 2023/5/28
 */
@SpringBootTest
// 启用SpringBoot的那个正常运行的类作为配置类来运行项目
@ContextConfiguration(classes = CommunityApplication.class)
public class CaffeineTests {

    // 测试方法进行测试
    @Autowired
    private DiscussPostService discussPostService;

    // 初始化数据，数据量大，进行压力测试效果才明显
    @Test
    public void initDataForTest() {
        // 插入30万条数据
        for (int i = 0; i < 300000; i++) {
            DiscussPost post = new DiscussPost();
            post.setUserId(111);
            post.setTitle("互联网求职暖春计划");
            post.setContent("今年的就业形势，确实不容乐观。过了个年，仿佛跳水一般，整个讨论区哀鸿遍野！19届真的没人要了吗？！18届被优化真的没有出路了吗？！大家的“哀嚎”与“悲惨遭遇”牵动了每日潜伏于讨论区的牛客小哥哥小姐姐们的心，于是牛客决定：是时候为大家做点什么了！为了帮助大家度过“寒冬”，牛客网特别联合60+家企业，开启互联网求职暖春计划，面向18届&19届，拯救0 offer！");
            post.setCreateTime(new Date());
            post.setScore(Math.random() * 2000);
            discussPostService.addDiscussPost(post);
        }
    }

    // 查询测试
    @Test
    public void testCache(){
        // 第一次缓存中没有数据，从数据库DB中查询，查询到数据后存入缓存中
        System.out.println(discussPostService.findDiscussPosts(0,0,10,1));
        // 第二次、第三次缓存中就有数据
        System.out.println(discussPostService.findDiscussPosts(0,0,10,1));
        System.out.println(discussPostService.findDiscussPosts(0,0,10,1));
        // 查询帖子列表，按照默认的方式，不走缓存走DB数据库
        System.out.println(discussPostService.findDiscussPosts(0,0,10,0));
    }

}
