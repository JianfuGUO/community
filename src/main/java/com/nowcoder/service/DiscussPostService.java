package com.nowcoder.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nowcoder.dao.DiscussPostMapper;
import com.nowcoder.entity.DiscussPost;
import com.nowcoder.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 业务层
 *
 * @Author Xiao Guo
 * @Date 2023/2/24
 */
@Service
public class DiscussPostService {

    // 日志工具
    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    // caffeine实现本地缓存
    // 注入配置文件里面caffeine自定义参数
    // 缓存中数据对象的最大数量
    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    // 缓存数据的过期时间
    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    // Caffeine核心接口: Cache, 子接口为LoadingCache, AsyncLoadingCache
    // 帖子列表缓存----所有的缓存都一样都是利用key来缓存value的形式----（key,value）
    private LoadingCache<String, List<DiscussPost>> postListCache;

    // 帖子总数缓存
    private LoadingCache<Integer, Integer> postRowsCache;

    // 类在调用的时候，唯一一次初始化时初始化缓存
    // init()方法被标记了@PostConstruct注解，当DiscussPostService对象创建后，init()方法会被自动调用，执行初始化操作。
    @PostConstruct
    public void init() {
        // <---初始化缓存的方式都是固定的--->
        // 初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                // 缓存对象的最大数量
                .maximumSize(maxSize)
                // 缓存过期时间
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() { // 匿名接口来实现

                    // 当尝试从缓存中取数据的时候，Caffeine首先看缓存中有没有，有的话就返回，没有的话就得知道怎么查这个数据并把它装入到缓存中；
                    // 故需要提供一个查询数据库得到数据、得到初始化数据的办法
                    @Override
                    public @Nullable List<DiscussPost> load(String key) throws Exception {
                        // 判断参数是否正常
                        if (key == null || key.length() == 0) {
                            throw new IllegalArgumentException("参数错误！");
                        }

                        // 解析key,（offset + ":" + limit）为key
                        String[] params = key.split(":");
                        if (params == null || params.length != 2) {
                            throw new IllegalArgumentException("参数错误！");
                        }

                        // 字符串转int
                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);

                        // 二级缓存:Redis -> mysql

                        // 只想缓存热门帖子（orderMode==1）&& 访问首页的时候（传递的参数中不传递，userId==0）
                        return discussPostMapper.selectDiscussPosts(0, offset, limit, 1);
                    }
                });

        // 初始化帖子总数缓存
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds,TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public @Nullable Integer load(Integer key) throws Exception {

                        logger.debug("load post rows from DB.");
                        return discussPostMapper.selectDiscussPostRow(key);
                    }
                });

    }

    // 把 dao 层的接口注入进来
    @Autowired
    private DiscussPostMapper discussPostMapper;

    // 注入敏感词过滤工具类
    @Autowired
    private SensitiveFilter sensitiveFilter;

    // 查询一页数据
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode) {
        // 访问首页，按照默认时间方式会调用此方法；按照热门帖子的方式，也会调用此方法；用户查看自己发布的帖子的时候也会调用此方法
        // 但只想缓存热门帖子（orderMode==1）&& 访问首页的时候（传递的参数中不传递，userId==0）
        if (userId == 0 && orderMode == 1) {
            // 启用缓存，（offset + ":" + limit）为key
            // 从缓存中返回结果
            return postListCache.get(offset + ":" + limit);
        }

        // 访问数据库查询数据
        logger.debug("load post list from DB.");
        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
    }

    // 查询行数
    public int findDiscussPostRows(int userId) {
        // 访问首页的时候（传递的参数中不传递，userId==0）
        if (userId == 0) {
            // 使用缓存，从缓存中返回结果
            return postRowsCache.get(userId);
        }

        // 访问数据库查询数据
        logger.debug("load post rows from DB.");
        return discussPostMapper.selectDiscussPostRow(userId);
    }

    // 添加帖子
    public int addDiscussPost(DiscussPost post) {
        if (post == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        // 转义 HTML 标签
        // HtmlUtils 为 SpringMVC 自带的工具类
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        // 过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));

        // 插入数据
        return discussPostMapper.insertDiscussPost(post);
    }

    // 查看帖子详情
    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    // 添加评论后修改表中的品论数量
    public int updateCommentCount(int id, int commentCount) {
        return discussPostMapper.updateCommentCount(id, commentCount);
    }

    // 更改帖子类型
    public int updateType(int id, int type) {
        return discussPostMapper.updateType(id, type);
    }

    // 更改帖子状态
    public int updateStatus(int id, int status) {
        return discussPostMapper.updateStatus(id, status);
    }

    // 更改帖子分数
    public int updateScore(int id, double score) {
        return discussPostMapper.updateScore(id, score);
    }
}
