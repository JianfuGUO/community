package com.nowcoder.controller;

import com.nowcoder.entity.Event;
import com.nowcoder.entity.Page;
import com.nowcoder.entity.User;
import com.nowcoder.event.EventProducer;
import com.nowcoder.service.FollowService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.CommunityConstant;
import com.nowcoder.util.CommunityUtil;
import com.nowcoder.util.HostHolder;
import com.nowcoder.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @Author Xiao Guo
 * @Date 2023/4/9
 */

@Controller
public class FollowController implements CommunityConstant {

    // 关注 + 取关
    @Autowired
    private FollowService followService;

    // 获取当前用户
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    // 注入 Producer 生产者
    @Autowired
    private EventProducer eventProducer;

    // 关注
    @PostMapping(path = "/follow")
    @ResponseBody // 异步请求
    public String follow(int entityType, int entityId) {
        // 获取当前用户
        User user = hostHolder.getUser();

        // 关注
        followService.follow(user.getId(), entityType, entityId);


        // 触发关注事件
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId); // 不需要postId

        eventProducer.fireEvent(event);

        // 异步请求返回Json数据给页面
        return CommunityUtil.getJSONString(0, "已关注！");
    }

    // 取消关注
    @PostMapping(path = "/unfollow")
    @ResponseBody // 异步请求
    public String unfollow(int entityType, int entityId) {
        // 获取当前用户
        User user = hostHolder.getUser();

        // 关注
        followService.unfollow(user.getId(), entityType, entityId);

        // 异步请求返回Json数据给页面
        return CommunityUtil.getJSONString(0, "已取消关注！");
    }

    // 查询用户关注的实体
    @GetMapping(path = "/followees/{userId}")
    // 默认返回html类型数据
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model) {
        // 根据 id 查询 user
        User user = userService.findUserById(userId);

        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }

        model.addAttribute("user", user);

        // 分页查询
        page.setLimit(5);
        // 查询路径（用于复用分页链接）
        page.setPath("/followees" + userId);
        // 数据总数（用于计算总页数）
        page.setRows((int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER));

        List<Map<String, Object>> userList = followService.findFollowees(userId, page.getOffset(), page.getLimit());

        if (userList != null) {
            // 遍历 List 集合
            for (Map<String, Object> map : userList) {
                // 从 map 中获取 user
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));

            }
        }

        model.addAttribute("users",userList);

        return "/site/followee";
    }

    // 查询用户的粉丝
    @GetMapping(path = "/followers/{userId}")
    // 默认返回html类型数据
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model) {
        // 根据 id 查询 user
        User user = userService.findUserById(userId);

        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }

        model.addAttribute("user", user);

        // 分页查询
        page.setLimit(5);
        // 查询路径（用于复用分页链接）
        page.setPath("/followers" + userId);
        // 数据总数（用于计算总页数）
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER,userId));

        List<Map<String, Object>> userList = followService.findFollowers(userId, page.getOffset(), page.getLimit());

        if (userList != null) {
            // 遍历 List 集合
            for (Map<String, Object> map : userList) {
                // 从 map 中获取 user
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));

            }
        }

        model.addAttribute("users",userList);

        return "/site/follower";
    }

    // 判断该用户的关注状态
    private boolean hasFollowed(int userId) {
        if (hostHolder.getUser() == null) {
            return false;
        }

        // 判断当前用户是否关注过
        return followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
    }
}
