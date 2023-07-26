package com.nowcoder.service;

import com.nowcoder.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.entity.DiscussPost;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Xiao Guo
 * @Date 2023/5/4
 */
@Service // 交给Spring容器管理
public class ElasticSearchService {

    // 往 ES 服务器中存数据
    @Autowired
    private DiscussPostRepository discussPostRepository;

    // 高亮显示
    @Autowired(required = false)
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    // 往 ES 服务器中添加帖子
    // 增
    // 改（改是重新 save 一下即可 ）
    public void saveDiscussPost(DiscussPost discussPost){
        discussPostRepository.save(discussPost);
    }

    // 删
    public void deleteDiscussPost(int id){
        discussPostRepository.deleteById(id);
    }

    // 搜索方法
    // 返回 Spring 提供的 Page 类型
    // 方法参数：[搜索关键词，当前页，显示的条数]
    public Page<DiscussPost> searchDiscussPost(String keyword, int current, int limit){
        // 查询条件
        NativeSearchQueryBuilder query = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // 字段匹配设置
        boolQueryBuilder.should(QueryBuilders.matchQuery("title", keyword));
        boolQueryBuilder.should(QueryBuilders.matchQuery("content", keyword));
        query.withQuery(boolQueryBuilder);

        // 排序方式设置
        query.withSorts(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSorts(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSorts(SortBuilders.fieldSort("createTime").order(SortOrder.DESC));

        // 高亮设置
        query.withHighlightFields(new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>"));

        // 分页设置
        PageRequest pageRequest = PageRequest.of(current, limit);
        query.withPageable(pageRequest);

        SearchHits<DiscussPost> searchHits = elasticsearchRestTemplate.search(query.build(), DiscussPost.class);

        // 将命中的数据进行处理，包装成DiscussPost放入list内
        List<DiscussPost> list = new ArrayList<>();

        // 总的命中数
        long totalHits = searchHits.getTotalHits();
        System.out.println(totalHits);

        List<SearchHit<DiscussPost>> searchHits1 = searchHits.getSearchHits();

        for (SearchHit<DiscussPost> hit : searchHits1) {
            DiscussPost post = hit.getContent();
//            System.out.println(content);

            // 处理高亮数据
            List<String> title = hit.getHighlightFields().get("title");
            if (title != null && !title.isEmpty()) {
                post.setTitle(title.get(0));
            }
            List<String> content = hit.getHighlightFields().get("content");
            if (content != null && !content.isEmpty()) {
                post.setContent(content.get(0));
            }

            list.add(post);
        }

        // 将 List 转换为 Page 格式的数据
        // 其中，list是命中的数据列表，totalHits是命中的总数，page和size表示当前页码和每页数据量，PageRequest表示一个分页请求对象。
        // 最终通过PageImpl来构造一个Page对象。
        PageImpl<DiscussPost> page  = new PageImpl<>(list, pageRequest, totalHits);

        return page;
    }
}
