package com.nowcoder.controller;

import com.nowcoder.entity.*;
import com.nowcoder.event.EventProducer;
import com.nowcoder.service.CommentService;
import com.nowcoder.service.DiscussPostService;
import com.nowcoder.service.LikeService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.CommunityConstant;
import com.nowcoder.util.CommunityUtil;
import com.nowcoder.util.HostHolder;
import com.nowcoder.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.ParameterResolutionDelegate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;


/**
 * 新增帖子等功能
 *
 * @Author Xiao Guo
 * @Date 2023/3/5
 */

@Controller
// 类访问的路径
@RequestMapping(path = "/discuss")
public class DisscussPostController implements CommunityConstant {

    // 帖子相关的 service
    @Autowired
    private DiscussPostService discussPostService;

    // 获取当前用户
    @Autowired
    private HostHolder hostHolder;

    // 注入 UserService 查询 user 相关的信息
    @Autowired
    private UserService userService;

    // 注入帖子详情里面显示评论的业务
    @Autowired
    private CommentService commentService;

    // 注入点赞的业务
    @Autowired
    private LikeService likeService;

    // 注入消息队列的生产者
    @Autowired
    private EventProducer eventProducer;

    // 注入RedisTemplate实现对redis数据库的各种操作
    @Autowired
    private RedisTemplate redisTemplate;

    // 异步请求
    @PostMapping(path = "/add")
    // 响应数据类型为字符串
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        // 获取当前用户
        User user = hostHolder.getUser();

        if (user == null) {
            // 403 表示没有权限
            return CommunityUtil.getJSONString(403, "您还没有登录！");
        }

        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        // 其他属性使用默认值
        discussPostService.addDiscussPost(post);

        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        // 触发一个事件，通知相关的监听器进行处理。
        // eventProducer是事件生产者，fireEvent方法是触发事件的方法，将事件对象event传入
        eventProducer.fireEvent(event);

        // -----计算帖子分数------------- //
        // 将有分数变化的帖子存放入 redis 缓存数据库中。（发新帖、加精、评论、点赞帖子时）
        // 获取 redisKey
        String redisKey = RedisKeyUtil.getPostScoreKey();
        // 将帖子 id 存放到 redis 数据库中，使用set数据结构去重
        redisTemplate.opsForSet().add(redisKey,post.getId());


        // 报错的情况将来统一处理
        return CommunityUtil.getJSONString(0, "发布成功！");
    }

    // 查看帖子详情
    // 根据id查询通常习惯将id放在路径里面
    // 不书写 @ResponseBody 注解，默认返回html页面
    // page是我们自己定义的一个实体类，接收和整理分页条件
    // 实体类型，JavaBean，申明在参数中 SpringMVC最终都会将Bean 存入到 Model 中
    @GetMapping(path = "/detail/{disscussPostId}")
    public String getDisscussPost(@PathVariable("disscussPostId") int disscussPostId, Model model, Page page) {
        // 从 discuss_post 表中获取帖子
        DiscussPost post = discussPostService.findDiscussPostById(disscussPostId);
        model.addAttribute("post", post);
        // 从 user 表中根据 discuss_post 表的 user_id 来获取 user 相关的信息
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        // ENTITY_TYPE_POST为帖子，表示对帖子点赞状态的查询
        // 获取点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, disscussPostId);
        // 发送给模板
        model.addAttribute("likeCount", likeCount);
        // 用户的点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, disscussPostId);
        // 发送给模板
        model.addAttribute("likeStatus", likeStatus);


        // 查评论的分页信息
        // 分页查询封装的 Page 类
        // 每页显示上限
        page.setLimit(5);
        // 设置查询路径
        page.setPath("/discuss/detail/" + disscussPostId);
        // 设置总的数目，方便统计由多少页
        page.setRows(post.getCommentCount());

        // 进行分页查询
        // 评论：给帖子的评论
        // 恢复：给评论的评论
        // 评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        // comment表里面有些数据id需转化为详细的数据
        // 构造 map 来处理显示数据
        // 评论 Vo 列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                // 用 map 来处理显示对象
                // 评论 VO
                Map<String, Object> commentVoMap = new HashMap<>();
                // 评论
                commentVoMap.put("comment", comment);
                // 作者
                commentVoMap.put("user", userService.findUserById(comment.getUserId()));

                // ENTITY_TYPE_COMMENT为评论，表示对评论帖子点赞状态的查询
                // 获取点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVoMap.put("likeCount", likeCount);
                // 用户的点赞状态
                likeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVoMap.put("likeStatus", likeStatus);


                // 回复列表(评论的评论)
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                // 回复VO列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVoMap = new HashMap<>();
                        // 回复
                        replyVoMap.put("reply", reply);
                        // 作者
                        replyVoMap.put("user", userService.findUserById(reply.getUserId()));
                        // 回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVoMap.put("target", target);

                        // 获取点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVoMap.put("likeCount", likeCount);
                        // 用户的点赞状态
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVoMap.put("likeStatus", likeStatus);

                        replyVoList.add(replyVoMap);
                    }
                }

                // 回复嵌套在评论里面
                commentVoMap.put("replys", replyVoList);

                // 回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVoMap.put("replyCount", replyCount);

                commentVoList.add(commentVoMap);
            }
        }

        // 将数据存到模板
        model.addAttribute("comments", commentVoList);

        return "/site/discuss-detail";
    }

    // 置顶
    // 使用异步请求（点完提交按钮后，不整体刷新页面）
    @PostMapping(path = "/top")
    @ResponseBody // 加上这个指定返回值为String, 默认返回html类型数据
    public String setTop(int id) {
        // 0-普通; 1-置顶
        discussPostService.updateType(id, 1);

        // 将更新的帖子数据同步到 Elasticsearch 中，保证每次搜寻到的为最新的数据
        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        // 触发一个事件，通知相关的监听器进行处理。
        // eventProducer是事件生产者，fireEvent方法是触发事件的方法，将事件对象event传入
        eventProducer.fireEvent(event);

        // 消费者监听了 TOPIC_PUBLISH 主题，后续自动处理消息

        // 给一个成功的消息提示
        return CommunityUtil.getJSONString(0);
    }

    // 加精
    // 使用异步请求（点完提交按钮后，不整体刷新页面）
    @PostMapping(path = "/wonderful")
    @ResponseBody // 加上这个指定返回值为String, 默认返回html类型数据
    public String setWonderful(int id) {
        // 0-正常; 1-精华; 2-拉黑;
        discussPostService.updateStatus(id, 1);

        // 将更新的帖子数据同步到 Elasticsearch 中，保证每次搜寻到的为最新的数据
        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        // 触发一个事件，通知相关的监听器进行处理。
        // eventProducer是事件生产者，fireEvent方法是触发事件的方法，将事件对象event传入
        eventProducer.fireEvent(event);

        // 消费者监听了 TOPIC_PUBLISH 主题，后续自动处理消息

        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, id);

        // 给一个成功的消息提示
        return CommunityUtil.getJSONString(0);
    }

    // 删除
    // 使用异步请求（点完提交按钮后，不整体刷新页面）
    @PostMapping(path = "/delete")
    @ResponseBody // 加上这个指定返回值为String, 默认返回html类型数据
    public String setDelete(int id) {
        // 0-正常; 1-精华; 2-拉黑;
        discussPostService.updateStatus(id, 2);

        // 触发删帖事件
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        // 触发一个事件，通知相关的监听器进行处理。
        // eventProducer是事件生产者，fireEvent方法是触发事件的方法，将事件对象event传入
        eventProducer.fireEvent(event);

        // 消费者监听了 TOPIC_DELETE 主题，后续自动处理消息

        // 给一个成功的消息提示
        return CommunityUtil.getJSONString(0);
    }


}
