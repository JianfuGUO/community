package com.nowcoder.controller;

import com.nowcoder.entity.Event;
import com.nowcoder.entity.User;
import com.nowcoder.event.EventProducer;
import com.nowcoder.service.LikeService;
import com.nowcoder.util.CommunityConstant;
import com.nowcoder.util.CommunityUtil;
import com.nowcoder.util.HostHolder;
import com.nowcoder.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;

/**
 * @Author Xiao Guo
 * @Date 2023/4/8
 */

@Controller
public class LikeController implements CommunityConstant {

    // 注入likeService
    @Autowired
    private LikeService likeService;

    // 获取当前用户
    @Autowired
    private HostHolder hostHolder;

    // 注入 Producer 生产者
    @Autowired
    private EventProducer eventProducer;

    // 操作 redis 数据库
    @Autowired
    private RedisTemplate redisTemplate;

    // 处理异步请求
    @PostMapping(path = "/like")
    @ResponseBody // 加上这个指定返回值为String, 默认返回html类型数据
    public String like(int entityType, int entityId, int entityUserId, int postId) {
        // 获取当前用户
        User user = hostHolder.getUser();

        // 点赞
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        // 查询某实体点赞的数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        // 查询某人对某实体的点赞状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);

        // 将数据结果返回给页面
        // Json类型的数据
        HashMap<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        // 触发点赞事件
        // 点赞触发事件，不点赞发发送系统通知
        if (likeStatus == 1) {
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);  //postId对于点赞的是帖子而言，这个数据是冗余的

            // 触发事件(将事件发布到指定的主题)
            eventProducer.fireEvent(event);
        }

        // 判断如果是对帖子进行点赞，才加分
        if (entityType == ENTITY_TYPE_POST) {
            // 计算帖子分数
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, postId);
        }


        // 返回Json数据
        return CommunityUtil.getJSONString(0, null, map);


    }
}
