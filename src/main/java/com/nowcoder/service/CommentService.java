package com.nowcoder.service;

import com.nowcoder.dao.CommentMapper;
import com.nowcoder.entity.Comment;
import com.nowcoder.util.CommunityConstant;
import com.nowcoder.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @Author Xiao Guo
 * @Date 2023/3/8
 */

@Service
public class CommentService implements CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;

    // 敏感词过滤
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    // 分页查询
    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    // 统计总的条目数
    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    // 添加评论数目
    // 包含两次 DML 操作，使用事务来管理
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if (comment == null){
            throw new IllegalArgumentException("参数不能为空！");
        }

        // 添加评论
        // 过滤标签
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        // 过滤敏感词
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        // 添加到数据库中
        int rows = commentMapper.insertComment(comment);

        // 更新帖子的评论数量
        if (comment.getEntityType() == ENTITY_TYPE_POST){
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());

            // 评论数改为最新值
            discussPostService.updateCommentCount(comment.getEntityId(),count);
        }

        // 返回影响的行数
        return rows;
    }

    // 根据 id 查询一条 comment
    public Comment findCommentById(int id){
        return commentMapper.selectCommentById(id);
    }
}
