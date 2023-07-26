package com.nowcoder.service;

import com.nowcoder.dao.AlphaDao;
import com.nowcoder.dao.DiscussPostMapper;
import com.nowcoder.dao.UserMapper;
import com.nowcoder.entity.DiscussPost;
import com.nowcoder.entity.User;
import com.nowcoder.util.CommunityUtil;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

/**
 * @Author Xiao Guo
 * @Date 2023/2/18
 */

// 演示容器对Bean的管理
@Service
//@Scope("singleton") // 默认单例，不用写
//@Scope("prototype") // 多例
public class AlphaService {

    // 日志工具
    private static final Logger logger = LoggerFactory.getLogger(AlphaService.class);
    // service 依赖 Dao
    @Autowired
    @Qualifier("alphaHilbernate") // 按照Bean的名称注入（针对接口含有多个实现类的处理情况）
    private AlphaDao alphaDao;

    // 演示事务：增加用户和发送帖子
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    // Spring 容器自动创建，自动装配
    @Autowired(required = false)
    private TransactionTemplate transactionTemplate;

    public AlphaService() {
        System.out.println("实例化AlphaService");
    }

    // 初始化方法，构造器之后调用
    @PostConstruct
    public void init() {
        System.out.println("初始化AlphaService");
    }

    // 销毁方法，销毁对象之前调用
    @PreDestroy
    public void destroy() {
        System.out.println("销毁AlphaService");
    }

    // 模拟查询业务
    public String find() {
        return alphaDao.select();
    }

    // 演示事务的原子性
    // 声明式业务
    // Spring声明式事务@Transactional
    // 事务隔离级别isolation和事务传播行为Propagation
    // 事务传播行为Propagation:业务方法之间相互调用，每个业务方法可能都含有事务，事务传播行为就是解决事务交叉的问题
    // a方法调用b方法，a相对于b为外部事务，b为当前事务
    // REQUIRED(0)：支持当前事务(外部事务)，如果不存在则创建新事务
    // REQUIRES_NEW(3)：创建一个新事务，并且暂停当前事务(外部事务)
    // NESTED(6)：如果当前存在事务(外部事务),则嵌套在该事务中执行(独立的提交和回滚)，否则就会REQUIRED一样.
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public Object save1() {
        // 新增用户
        User user = new User();
        user.setUsername("alpha");
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
        user.setEmail("alpha@qq.com");
        user.setHeaderUrl("http://image.nowcoder.com/head/99t.png");
        user.setCreateTime(new Date());
        // 执行 DML 语句（增删改）默认自动提交，马上生效
        userMapper.insertUser(user);

        // 新增帖子
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle("Hello");
        post.setContent("新人报道!");
        post.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(post);

        // 人为报错，事务回滚，前面两条插入的语句未启作用
        // 将字符串转换为整数
        Integer.valueOf("abc");

        return "ok";
    }

    // 编程式事务
    public Object save2() {
        // 设置隔离级别
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        // 设置事务传播行为
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return transactionTemplate.execute(new TransactionCallback<Object>() {
            // 回调方法，由transactionTemplate底层自动调
            @Override
            public Object doInTransaction(TransactionStatus status) {
                // 实现自己的业务逻辑，会对此部分的代码进行事务管理
                // 新增用户
                User user = new User();
                user.setUsername("beta");
                user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
                user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
                user.setEmail("beta@qq.com");
                user.setHeaderUrl("http://image.nowcoder.com/head/999t.png");
                user.setCreateTime(new Date());
                // 执行 DML 语句（增删改）默认自动提交，马上生效
                userMapper.insertUser(user);

                // 新增帖子
                DiscussPost post = new DiscussPost();
                post.setUserId(user.getId());
                post.setTitle("你好");
                post.setContent("新人报道!");
                post.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(post);

                // 人为报错，事务回滚，前面两条插入的语句未启作用
                // 将字符串转换为整数
                Integer.valueOf("abc");

                return "ok";
            }
        });
    }

    // 让改方法在多线程的环境下，被异步调用
    // 普通线程池的使用
    @Async
    public void execute1() {
        logger.debug("execute1");
    }

    // 定时任务线程池的使用
    // 方法上加上@Scheduled，任何程序执行，该方法都会自动被调用
//    @Scheduled(initialDelay = 10000, fixedDelay = 1000) // 延迟10秒后执行，执行间隔是1秒
//    public void execute2() {
//        logger.debug("execute2");
//    }
}
