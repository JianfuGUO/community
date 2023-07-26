package com.nowcoder.service;

import com.nowcoder.dao.LoginTicketMapper;
import com.nowcoder.dao.UserMapper;
import com.nowcoder.entity.LoginTicket;
import com.nowcoder.entity.User;
import com.nowcoder.util.CommunityConstant;
import com.nowcoder.util.CommunityUtil;
import com.nowcoder.util.MailClient;
import com.nowcoder.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author Xiao Guo
 * @Date 2023/2/24
 */
@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    // 注入邮件客户端
    @Autowired
    private MailClient mailClient;

    // 注入模板引擎
    @Autowired(required = false)
    private TemplateEngine templateEngine;

    // 注入登陆页面的 mapper
//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    // 高度封装的“RedisTemplate”类，操作数据进行redis库的存取
    @Autowired
    private RedisTemplate redisTemplate;

    // .yml 配置文件里面的域名
    @Value("${community.path.domain}")
    private String domain;

    // .yml 配置文件里面的项目访问路径名
    @Value("${server.servlet.context-path}")
    private String contextPath;

    // 根据 id 查用户信息
    public User findUserById(int id) {
        // MySQL 中查询
//        return userMapper.selectById(id);

        // 优先从 redis 里面查询
        User user = getCache(id);

        // redis 里面没有时
        if (user == null) {
            // 从 MySQL 中查询之后再存入到 redis 中
            user = initCache(id);
        }

        return user;
    }

    // 注册时的业务逻辑
    public Map<String, Object> register(User user) {
        // 使用 map 集合存储多种情况的信息
        HashMap<String, Object> map = new HashMap<>();

        // 空值处理
        if (user == null) {
            // 非法参数
            throw new IllegalArgumentException("参数不能为空！");
        }

        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }

        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }

        // 验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在！");
            return map;
        }

        // 验证邮箱
        User u2 = userMapper.selectByEmail(user.getEmail());
        if (u2 != null) {
            map.put("emailMsg", "该邮箱已被注册！");
            return map;
        }

        // 注册用户
        // 生成5位随机字符串
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        // 用加密的密码覆盖之前的密码
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        // 普通用户
        user.setType(0);
        // 未激活
        user.setStatus(0);
        // 设置激活码
        user.setActivationCode(CommunityUtil.generateUUID());
        // 随机头像
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        // 注册时间
        user.setCreateTime(new Date());
        // 添加到数据库中
        userMapper.insertUser(user);

        // 发送激活邮件
        // 存储动态变量(thymeleaf包下)
        Context context = new Context();
        // /mail/activate.html文件里面需要的动态变量值
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);

        // 模板路径和内容
        // 邮箱里面的html页面信息
        String content = templateEngine.process("/mail/activation", context);
        System.out.println(content);

        // 发送邮件
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    // 激活账号
    // http://localhost:8080/community/activation/101/code
    // <a href="http://localhost:8080/community/activation/154/40051b8e17554263bea9dad54973d58d">此链接</a>,
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            // 重复激活
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            // 修改激活状态
            userMapper.updateStatus(userId, 1);

            // 清除缓存
            clearCache(userId);

            return ACTIVATION_SUCCESS;
        } else {
            // 激活失败
            return ACTIVATION_FAILURE;
        }
    }

    // 登录的业务逻辑 ------- 模拟 session/cookie的功能
    // 多种情况的返回结果,返回给页面
    // 传入的 password 需要使用 MD5加密，再与数据库中存入的密码比对
    // expiredSeconds 为过期的秒数
    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        // 账号不能为空
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }

        // 密码不能为空
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }

        // 验证账号合法性
        // 验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在！");
            return map;
        }

        // 验证状态（是否激活，未激活不能使用）
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活！");
        }

        // 验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        // String 为引用类型，
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确！");
            return map;
        }

        //  生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        // 设置激活状态
        loginTicket.setStatus(0);
        // 设置过期时间
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        // 插入登陆信息
//        loginTicketMapper.insertLoginTicket(loginTicket);

        // 插入登陆信息，存到redis中
        // redisKey
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        // redis会将loginTicket对象序列化为Json字符串存储起来
        redisTemplate.opsForValue().set(redisKey, loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    // 退出
    public void logout(String ticket) {
        // 1 表示无效
//        loginTicketMapper.updateStatus(ticket, 1);

        // redisKey
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        // 从 redis 里面取出数据，并改变状态为1
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        // 改变类的属性值
        loginTicket.setStatus(1);
        // 存回到 redis 中
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }

    // 查询凭证
    public LoginTicket findLoginTicket(String ticket) {
//        return loginTicketMapper.selectByTicket(ticket);

        // 从 redis 里面查询
        // redisKey
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    // 更改用户的头像路径
    public int updateHeader(int userId, String headerUrl) {
//        return userMapper.updateHeader(userId, headerUrl);

        // 更新行数
        int rows = userMapper.updateHeader(userId, headerUrl);
        // 清除缓存
        clearCache(userId);
        return rows;
    }

    // 修改用户密码
    public void updatePassword(int userId, String password) {
        userMapper.updatePassword(userId, password);
    }

    // 根据名称查询User
    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

    // 使用Redis缓存用户信息
    // 1.优先从缓存中取值
    // redisKey
    private User getCache(int userId) {
        // redisKey
        String redisKey = RedisKeyUtil.getUserKey(userId);

        // 从 redis 里面查询值并退出
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    // 2.取不到，初始化缓存数据，设置存3600s，即一个小时
    // 数据来源于 mysql
    private User initCache(int userId) {
        // 从 MySQL 中将数据查询到
        User user = userMapper.selectById(userId);

        // 获取 redisKey
        String redisKey = RedisKeyUtil.getUserKey(userId);

        // 将从  MySQL 中查询到的数据
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);

        return user;
    }

    // 3.数据变更，清除缓存数据
    private void clearCache(int userId) {
        // 获取 redisKey
        String redisKey = RedisKeyUtil.getUserKey(userId);

        // 删除 redisKey 即删除数据
        redisTemplate.delete(redisKey);
    }

    // 权限（用户user可能包含多个权限，使用集合来封装）
    // 使用此方法判断user对象包含的权限
    public Collection<? extends GrantedAuthority> getAuthorities(int userId){
        User user = this.findUserById(userId);

        // 权限集合
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {
                switch (user.getType()){
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}
