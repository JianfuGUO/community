package com.nowcoder.quartz;

import com.nowcoder.entity.DiscussPost;
import com.nowcoder.service.DiscussPostService;
import com.nowcoder.service.ElasticSearchService;
import com.nowcoder.service.LikeService;
import com.nowcoder.util.CommunityConstant;
import com.nowcoder.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author Xiao Guo
 * @Date 2023/5/18
 */

// 使用Quartz实现定时任务
public class PostScoreRefreshJob implements Job, CommunityConstant {

    // 定时任务关键节点记录日志
    // slf4j
    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    // 计算数据的来源为 redis
    @Autowired
    private RedisTemplate redisTemplate;

    // 判断是否加精、评论数量、点赞数量以及需要将数据同步到 elasticsearch 搜索引擎中，注入相关Bean
    // 评论 + 加精
    @Autowired
    private DiscussPostService discussPostService;

    // 点赞
    @Autowired
    private LikeService likeService;

    // 同步数据到 Elasticsearch 搜索引擎中
    @Autowired
    private ElasticSearchService elasticSearchService;

    // 牛客纪元
    private static final Date epoch;

    // 静态代码块里面初始化
    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化牛客纪元失败！", e);
        }
    }


    // alt + enter 实现接口方法或者导包
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // 获取 redisKey
        String redisKey = RedisKeyUtil.getPostScoreKey();
        // 每个 key 都要进行反复的操作
        // 针对Set的相关操作
        // BoundSetOperations就是一个绑定key的对象，我们可以通过这个对象来进行与key相关的操作。
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        // 判断redis的key里面有无数据
        if (operations.size() == 0) {
            logger.info("[任务取消] 没有需要刷新的帖子!");
            return;
        }

        logger.info("[任务开始] 正在刷新帖子分数: " + operations.size());

        // redis 中含有数据就算score
        while (operations.size() > 0) {
            // operations是一个集合，每次弹出一个值集合就少一个值
            this.refresh((Integer) operations.pop());
        }

        logger.info("[任务结束] 帖子分数刷新完毕！");

    }

    private void refresh(int postId) {
        // 查询帖子
        DiscussPost post = discussPostService.findDiscussPostById(postId);

        if (post == null) {
            logger.error("该帖子不存在: id = " + postId);
            return;
        }

        // 分数 score 计算公式：
        // log(精华分 + 评论数*10 + 点赞数*2 + 收藏数*2) + (发布时间 – 牛客纪元)
        // 是否加精
        boolean wonderful = post.getStatus() == 1;
        // 评论数量
        int commentCount = post.getCommentCount();
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

        // 计算权重
        // 加精：true为75分，false为0分
        // 时间默认为 ms
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        // 分数 = 帖子权重 + 距离天数
        double score = Math.log10(Math.max(w, 1))
                + (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);

        // 更新帖子分数(MySQL数据库中)
        discussPostService.updateScore(postId, score);
        // 同步搜索数据(ElasticSearch中)
        post.setScore(score);
        elasticSearchService.saveDiscussPost(post);

    }
}
