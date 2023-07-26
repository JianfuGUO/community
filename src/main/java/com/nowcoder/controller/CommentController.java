package com.nowcoder.controller;

import com.nowcoder.entity.Comment;
import com.nowcoder.entity.DiscussPost;
import com.nowcoder.entity.Event;
import com.nowcoder.event.EventProducer;
import com.nowcoder.service.CommentService;
import com.nowcoder.service.DiscussPostService;
import com.nowcoder.util.CommunityConstant;
import com.nowcoder.util.HostHolder;
import com.nowcoder.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

/**
 * @Author Xiao Guo
 * @Date 2023/3/9
 */

@Controller
@RequestMapping(path = "/comment")
public class CommentController implements CommunityConstant {

    // 评论相关业务
    @Autowired
    private CommentService commentService;

    // 获取当前用户
    @Autowired
    private HostHolder hostHolder;

    // 注入 Producer 生产者
    @Autowired
    private EventProducer eventProducer;

    // 帖子查询
    @Autowired
    private DiscussPostService discussPostService;

    // 注入RedisTemplate实现对redis数据库的各种操作
    @Autowired
    private RedisTemplate redisTemplate;

    // 返回到帖子页面
    @PostMapping(path = "/add/{discussPostId}")
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        // 页面传过来的信息主要是内容
        comment.setUserId(hostHolder.getUser().getId());
        // 设置默认 status
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        // 添加评论
        commentService.addComment(comment);

        // 触发评论事件
        // set返回值变更为Event的效果
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", discussPostId);

        // 人的操作：点赞\回复\关注
        // 事件发生在哪个实体上
        // 实体的作者根据实体的类型而不同
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }

        // 触发事件(将事件发布到指定的主题)
        eventProducer.fireEvent(event);

        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            // 触发发帖事件
            event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            // 触发一个事件，通知相关的监听器进行处理。
            // eventProducer是事件生产者，fireEvent方法是触发事件的方法，将事件对象event传入
            eventProducer.fireEvent(event);

            // 计算帖子分数
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, discussPostId);

        }

        // 重定向到帖子详情的页面
        return "redirect:/discuss/detail/" + discussPostId;
    }
}
