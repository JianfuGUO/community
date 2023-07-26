package com.nowcoder.dao;

import com.nowcoder.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

// 在接口类上添加了@Mapper，在编译之后会生成相应的接口实现类。
// 使用注解让 Spring 容器来管理此接口的是实现类，才能自动装配
// 操作 discuss_post 表
@Mapper
public interface DiscussPostMapper {
    // 分页查询：offset为起始行数，limit为显示的条数
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit,int orderMode);

    // @Param("userId")给参数取别名，参数名过长时，方便书写sql
    // 如果只有一个参数，并且在<if>（动态sql）里使用，则必须加别名
    int selectDiscussPostRow(@Param("userId") int userId);

    // AJAX请求，实现发布帖子的功能
    // 增加帖子
    int insertDiscussPost(DiscussPost discussPost);

    // 帖子详情
    DiscussPost selectDiscussPostById(int id);

    // 新增评论后，需更改评论数量
    int updateCommentCount(int id, int commentCount);

    // 更改帖子类型
    // 置顶操作，修改帖子Type
    int updateType(int id, int type);

    // 更改帖子状态
    // 加精、删除操作，修改帖子的status
    int updateStatus(int id, int status);

    // 更新帖子分数
    int updateScore(int id,double score);

}
