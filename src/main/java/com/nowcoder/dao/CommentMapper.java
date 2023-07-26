package com.nowcoder.dao;

import com.nowcoder.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

// 在接口类上添加了@Mapper，在编译之后会生成相应的接口实现类。
// 使用注解让 Spring 容器来管理此接口的是实现类，才能自动装配
@Mapper
public interface CommentMapper {

    // 分页查询（按实体类型，评论有针对帖子、个人、评论的评论等诸多类型）
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    // 查询整个的条目数好计算分页查询的总页数
    int selectCountByEntity(int entityType, int entityId);

    // 添加评论
    int insertComment(Comment comment);

    // 根据 id 查询一条 comment
    Comment selectCommentById(int id);
}
