package com.nowcoder.controller;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.entity.Message;
import com.nowcoder.entity.Page;
import com.nowcoder.entity.User;
import com.nowcoder.service.MessageService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.CommunityConstant;
import com.nowcoder.util.CommunityUtil;
import com.nowcoder.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

/**
 * @Author Xiao Guo
 * @Date 2023/3/10
 */
@Controller
public class MessageController implements CommunityConstant {

    // 私信
    @Autowired
    private MessageService messageService;

    // 获取当前用户
    @Autowired
    private HostHolder hostHolder;

    // 查询用户相关信息
    @Autowired
    private UserService userService;

    // 私信列表
    @GetMapping(path = "/letter/list")
    public String getLetterList(Model model, Page page) {

        // 报错信息
//         Integer.valueOf("abc");

        // 获取当前用户
        User user = hostHolder.getUser();

        // 分页信息
        page.setLimit(5);
        // 分页查询路径
        page.setPath("/letter/list");
        // 数据总数（用于计算总页数）
        page.setRows(messageService.findConversationCount(user.getId()));

        // 会话列表
        List<Message> conversationList = messageService.findConversations(
                user.getId(), page.getOffset(), page.getLimit());

        // 页面需要多个信息，使用 Map 来封装，---------------> 返回给页面的数据
        List<Map<String, Object>> conversations = new ArrayList<>();

        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                // 会话信息
                map.put("conversation", message);
                // 查询某个会话所包含的私信数量
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
                // 查询未读私信的数量
                map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                // 显示对话者的头像
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));

                conversations.add(map);
            }
        }

        // 将数据存放到模板中
        model.addAttribute("conversations", conversations);

        // 查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        // 系统通知
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);


        return "site/letter";
    }

    // 私信详情
    // Page为分页作用
    @GetMapping(path = "/letter/detail/{conversationId}")
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model) {
        // 分页信息
        page.setLimit(5);
        // 分页查询路径
        page.setPath("/letter/detail/" + conversationId);
        // 数据总数（用于计算总页数）
        page.setRows(messageService.findLetterCount(conversationId));

        // 私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        // 返回给页面的数据
        List<Map<String, Object>> letters = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", message);
                map.put("fromUser", userService.findUserById(message.getFromId()));

                letters.add(map);
            }
        }

        // 数据传送给模板
        model.addAttribute("letters", letters);

        // 私信目标
        model.addAttribute("target", getLetterTarget(conversationId));

        // 设置已读
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "/site/letter-detail";

    }

    // 分割 conversation_id
    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }
    }

    // 提取集合中未读的信息
    private List<Integer> getLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();

        if (letterList != null) {
            for (Message message : letterList) {
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }

        return ids;
    }

    // 新增私信
    // ajax 异步请求
    @PostMapping(path = "/letter/send")
    @ResponseBody // 返回JSON字符串给浏览器
    public String sendLetter(String toName, String content) {

        // 报错信息
//        Integer.valueOf("abc");

        User target = userService.findUserByName(toName);

        if (target == null) {
            // 返回 JSON 数据
            return CommunityUtil.getJSONString(1, "目标用户不存在！");
        }

        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        // conversation_id 字段 111_119(id 小的在前面，大的在后面)
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());

        // 新增私信
        messageService.addMessage(message);

        // 返回数据
        return CommunityUtil.getJSONString(0);

    }

    // 通知列表
    @GetMapping(path = "/notice/list")
    // 默认返回html类型数据
    // 声明变量类型 Model（模板） 之后 SpringMVC自动注入
    public String getNoticeList(Model model) {
        // 获取当前用户
        User user = hostHolder.getUser();

        // 查询评论类通知
        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        // 添加一些额外的补充数据信息
        if (message != null) {
            Map<String, Object> messageVO = new HashMap<>();
            messageVO.put("message", message);

            // 额外信息
            // 私信：内容是一句话，直接可以
            // 通知：内容是JSON字符串，需要将其还原成对象，再去应用
            // MySQL数据库Message表中，存储的content字段的内容：
            // {&quot;entityType&quot;:1,&quot;entityId&quot;:234,&quot;postId&quot;:234,&quot;userId&quot;:156}
            // 其中&quot----> " 引号，转义字符
            String content = HtmlUtils.htmlUnescape(message.getContent()); // 去掉转义字符
            // 转化为对象
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            // 私信的数量
            // 查询某个主题所包含的通知列表
            int count = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("count", count);

            // 未读的数量
            // 查询未读的通知的数量
            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("unread", unread);

            model.addAttribute("commentNotice", messageVO);
        }
        // 将数据传送给模板

        // 查询点赞类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
        // 添加一些额外的补充数据信息
        if (message != null) {
            Map<String, Object> messageVO = new HashMap<>();
            messageVO.put("message", message);

            // 额外信息
            // 私信：内容是一句话，直接可以
            // 通知：内容是JSON字符串，需要将其还原成对象，再去应用
            // MySQL数据库Message表中，存储的content字段的内容：
            // {&quot;entityType&quot;:1,&quot;entityId&quot;:234,&quot;postId&quot;:234,&quot;userId&quot;:156}
            // 其中&quot----> " 引号，转义字符
            String content = HtmlUtils.htmlUnescape(message.getContent()); // 去掉转义字符
            // 转化为对象
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            // 私信的数量
            // 查询某个主题所包含的通知列表
            int count = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            messageVO.put("count", count);

            // 未读的数量
            // 查询未读的通知的数量
            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            messageVO.put("unread", unread);

            model.addAttribute("likeNotice", messageVO);
        }
        // 将数据传送给模板


        // 查询关注类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        // 添加一些额外的补充数据信息
        if (message != null) {
            Map<String, Object> messageVO = new HashMap<>();

            messageVO.put("message", message);

            // 额外信息
            // 私信：内容是一句话，直接可以
            // 通知：内容是JSON字符串，需要将其还原成对象，再去应用
            // MySQL数据库Message表中，存储的content字段的内容：
            // {&quot;entityType&quot;:1,&quot;entityId&quot;:234,&quot;postId&quot;:234,&quot;userId&quot;:156}
            // 其中&quot----> " 引号，转义字符
            String content = HtmlUtils.htmlUnescape(message.getContent()); // 去掉转义字符 使用HtmlUtils.htmlUnescape函数可以将这些转义字符转换回它们原来的字符。
            // 转化为对象
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));

            // 私信的数量
            // 查询某个主题所包含的通知列表
            int count = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("count", count);

            // 未读的数量
            // 查询未读的通知的数量
            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("unread", unread);

            model.addAttribute("followNotice", messageVO);
        }
        // 将数据传送给模板


        // 查询未读消息数量
        // 朋友私信
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        // 系统通知
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/notice";
    }

    // 通知详情
    // 查询某个主题所包含的通知列表(支持分页)
    @GetMapping(path = "/notice/detail/{topic}")
    // 默认返回html类型数据
    // 声明变量类型 Page（自定义的分页类） 之后 SpringMVC自动注入
    // 声明变量类型 Model（模板） 之后 SpringMVC自动注入
    public String getNoticeDetail(@PathVariable("topic") String topic, Page page, Model model) {
        // 获取当前用户
        User user = hostHolder.getUser();

        // 分页查询参数设置
        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        // 通知本身的数据
        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        // 额外的数据
        // 聚合起来
        List<Map<String, Object>> noticeVoList = new ArrayList<>();
        if (noticeList != null) {
            // 遍历list，一个一个进行处理
            for (Message notice : noticeList) {
                Map<String, Object> map = new HashMap<>();

                // 通知
                map.put("notice", notice);

                // 额外封装的具体信息
                // 内容
                // 使用HtmlUtils.htmlUnescape函数可以将这些转义字符转换回它们原来的字符。
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.findUserById((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));
                // 通知作者
                map.put("fromUser", userService.findUserById(notice.getFromId()));

                noticeVoList.add(map);
            }
        }

        // 将数据传送给模板
        model.addAttribute("notices", noticeVoList);

        // 设置已读
        List<Integer> ids = getLetterIds(noticeList);
        if (!ids.isEmpty()){
            messageService.readMessage(ids);
        }

        // 返回模板
        return "/site/notice-detail";
    }
}
