package com.nowcoder;

import com.nowcoder.dao.DiscussPostMapper;
import com.nowcoder.dao.LoginTicketMapper;
import com.nowcoder.dao.MessageMapper;
import com.nowcoder.dao.UserMapper;
import com.nowcoder.entity.DiscussPost;
import com.nowcoder.entity.LoginTicket;
import com.nowcoder.entity.Message;
import com.nowcoder.entity.User;
import org.junit.jupiter.api.Test;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;

/**
 * @Author Xiao Guo
 * @Date 2023/2/19
 */

@SpringBootTest
// 启用SpringBoot的那个正常运行的类作为配置类来运行项目
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MessageMapper messageMapper;

    // .yml 配置文件的某一行属性文字
    @Value("${spring.datasource.url}")
    private String url;

    /**
     * UserMapper接口方法的测试
     */
    // 测试查询方法
    @Test
    public void testSelectUser() {
        // 根据 id 查询
        User user1 = userMapper.selectById(101);
        System.out.println(user1);

        // 根据 username 查询
        User user2 = userMapper.selectByName("liubei");
        System.out.println(user2);

        // 根据 email 查询
        User user3 = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user3);
    }

    // 测试 insert（增加）的方法
    @Test
    public void testInsertUser() {
        User user = new User();
        user.setUsername("test");
        user.setPassword("1234");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://images.nowcoder.com/head/101t.png");
        user.setCreateTime(new Date());

        // 返回插入影响的行数
        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    // 测试 update(修改) 方法
    @Test
    public void updateUser() {
        int rows1 = userMapper.updateStatus(150, 1);
        System.out.println(rows1);

        int rows2 = userMapper.updateHeader(150, "http://images.nowcoder.com/head/102t.png");
        System.out.println(rows2);

        int rows3 = userMapper.updatePassword(150, "hello");
        System.out.println(rows3);

    }

    /**
     * DiscussPostMapper接口方法测试
     */
    @Test
    public void testSelectPosts() {
        // selectDiscussPosts方法
        List<DiscussPost> lists = discussPostMapper.selectDiscussPosts(0, 0, 10,0);
        for (DiscussPost list : lists) {
            System.out.println(list);
        }

        int rows = discussPostMapper.selectDiscussPostRow(0);
        System.out.println(rows);
    }

    // 测试获取 .yml 配置文件的某一行属性文字
    @Test
    public void testYml() {
        System.out.println(url);
    }

    /**
     * LoginTicketMapper 的测试方法
     */
    @Test
    public void testinsertLoginTicket() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));

        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectLoginTicket() {
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);

        // 测试修改的方法
        loginTicketMapper.updateStatus("abc", 0);
        LoginTicket loginTicket1 = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket1);

    }

    /**
     * 测试 MessageMapper 的方法
     */
    @Test
    public void testMessageMapper() {
        // 查询所有私信
        List<Message> list = messageMapper.selectConversation(111, 0, 20);
        for (Message message : list) {
            System.out.println(message);
        }

        // 查询当前用户的会话数量
        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);

        // 查询某个会话所包含的私信列表
        list = messageMapper.selectLetters("111_112", 0, 10);
        for (Message message : list) {
            System.out.println(message);
        }

        // 查询某个会话所包含的私信数量
        int count1 = messageMapper.selectLetterCount("111_112");
        System.out.println(count1);

        // 查询未读私信的数量
        int count2 = messageMapper.selectLetterUnreadCount(131, "111_131");
        System.out.println(count2);
    }

}
