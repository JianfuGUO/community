package com.nowcoder.dao.elasticsearch;

import com.nowcoder.entity.DiscussPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository // Spring数据库访问层注解，@Mapper是MyBatis专有接口
// 继承原始的 ElasticsearchRepository 接口，并指明（泛型，主键类型）
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost, Integer> {
    // DiscussPostRepository接口要继承SpringData提供的ElasticsearchRepository父接口
    // 一旦继承,当前接口就会被识别为连接ES的持久层类,SpringData会自动为它生成基本增删改查方法（内置了很多方法，可以像数据库一样增删改查）
    // ElasticsearchRepository<[关联的实体类名称],[实体类主键类型]>

    // findBy开头不需要实体类型
    // queryDiscussPosts开头需要实体类型
    Page<DiscussPost> findByTitleMatchesOrContentMatchesOrderByTypeDescCreateTimeDesc
            (String title, String content, Pageable pageable);

    Iterable<DiscussPost> queryDiscussPostsByTitleMatches(String title);
}
