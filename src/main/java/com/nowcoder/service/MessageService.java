package com.nowcoder.service;

import com.nowcoder.dao.MessageMapper;
import com.nowcoder.entity.Message;
import com.nowcoder.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * 调用 dao层的接口方法，返回相应的返回值
 *
 * @Author Xiao Guo
 * @Date 2023/3/10
 */

@Service
public class MessageService {

    // 私信列表
    @Autowired
    private MessageMapper messageMapper;

    // 过滤敏感词
    @Autowired
    private SensitiveFilter sensitiveFilter;


    public List<Message> findConversations(int userId, int offset, int limit) {
        return messageMapper.selectConversation(userId, offset, limit);
    }

    public int findConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    public List<Message> findLetters(String conversationId, int offset, int limit) {
        return messageMapper.selectLetters(conversationId, offset, limit);
    }

    public int findLetterCount(String conversationId) {
        return messageMapper.selectLetterCount(conversationId);
    }

    public int findLetterUnreadCount(int userId, String conversationId) {
        return messageMapper.selectLetterUnreadCount(userId, conversationId);
    }

    // 添加私信
    public int addMessage(Message message) {
        // 过滤标签
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        // 过滤敏感词
        message.setContent(sensitiveFilter.filter(message.getContent()));

        // 添加数据
        return messageMapper.insertMessage(message);
    }

    // 读取消息，最终反馈的是改变状态（返回影响的行数）
    public int readMessage(List<Integer> ids) {
        return messageMapper.updateStatus(ids, 1);
    }

    // 通知列表
    // 查询某个主题下最新的通知
    public Message findLatestNotice(int userId, String topic) {
        return messageMapper.selectLatestNotice(userId, topic);
    }

    // 查询某个主题所包含的通知列表
    public int findNoticeCount(int userId, String topic) {
        return messageMapper.selectNoticeCount(userId, topic);
    }

    // 查询未读的通知的数量
    public int findNoticeUnreadCount(int userId, String topic) {
        return messageMapper.selectNoticeUnreadCount(userId, topic);
    }

    // 通知详情
    // 查询某个主题所包含的通知列表(支持分页)
    public List<Message> findNotices(int userId, String topic, int offset, int limit) {
        return messageMapper.selectNotices(userId, topic, offset, limit);
    }

}
