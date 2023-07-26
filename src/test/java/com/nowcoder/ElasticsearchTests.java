package com.nowcoder;

import com.nowcoder.dao.DiscussPostMapper;
import com.nowcoder.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.entity.DiscussPost;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Xiao Guo
 * @Date 2023/5/1
 */
@SpringBootTest
// 启用SpringBoot的那个正常运行的类作为配置类来运行项目
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTests {

    // 从 MySQL 中取数据
    @Autowired
    private DiscussPostMapper discussPostMapper;

    // 往 Elasticsearch 中存数据、查数据
    @Autowired
    private DiscussPostRepository discussPostRepository;

    // 有些特殊情况
    // 默认情况下必须要求依赖对象存在，如果要允许null值，可以设置它的required=false
    @Autowired(required = false)
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    // 往 Elasticsearch 服务器中添加数据
    // 测试前要开启 zookeeper 和 kafka 和 Elasticsearch 三个服务
    @Test
    public void testInsert() {
        // 一次插入一条数据
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
    }

    // 一次插入多条数据
    @Test
    public void testInsertList() {
        // 根据 user 来插入数据
        // 参数（userId，起始数，终止数）
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(133, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134, 0, 100,0));
    }

    // 修改一条数据
    @Test
    public void testUpdate() {
        // 先从MySQL里面查询
        DiscussPost post = discussPostMapper.selectDiscussPostById(231);
        // 修改内容
        post.setContent("我是新人，使劲灌水！");
        // 存入elasticsearch中(MySQL中的数据未修改)
        discussPostRepository.save(post);
    }

    // 删除所有数据
    @Test
    public void testDelete() {
//        discussPostRepository.deleteById(231); // 删除一条数据
        discussPostRepository.deleteAll();          // 删除整个索引
    }

    /**
     * elasticsearch最核心的搜索功能
     */
    //单条件查询
    @Test
    void queryOne() {
        // 查询ES中items索引中,title字段包含"游戏"关键字的数据
        Iterable<DiscussPost> items = discussPostRepository.queryDiscussPostsByTitleMatches("互联网寒冬");
        // 遍历每一条数据
        items.forEach(item -> System.out.println(item));
    }

    /**
     * 实现 ElasticsearchRepository 接口操作 ElasticSearch
     */
    @Test
    public void testSearchByRepository() {
        int pageNum = 1;   // 要查询的页码
        int pageSize = 10;  // 每页包含的数据条数
        Page<DiscussPost> page = discussPostRepository
                .findByTitleMatchesOrContentMatchesOrderByTypeDescCreateTimeDesc(
                        "互联网寒冬", "互联网寒冬", PageRequest.of(pageNum - 1, pageSize));
        page.forEach(item -> System.out.println(item));
        // page对象中包含的分页和信息:
        System.out.println("总页数:" + page.getTotalPages());
        System.out.println("总条数:" + page.getTotalElements());
        System.out.println("当前页:" + (page.getNumber() + 1));
        System.out.println("每页条数:" + page.getSize());
        System.out.println("是否为首页:" + page.isFirst());
        System.out.println("是否为末页:" + page.isLast());

    }

    /**
     * 使用 elasticsearchTemplate 来操作 ElasticSearch
     */
    @Test
    public void testSearchByTemplate() {

        // 查询条件
        NativeSearchQueryBuilder query = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // 字段匹配设置
        boolQueryBuilder.should(QueryBuilders.matchQuery("title", "互联网寒冬"));
        boolQueryBuilder.should(QueryBuilders.matchQuery("content", "互联网寒冬"));
        query.withQuery(boolQueryBuilder);

        // 排序方式设置
        query.withSorts(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSorts(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSorts(SortBuilders.fieldSort("createTime").order(SortOrder.DESC));

        // 高亮设置
        query.withHighlightFields(new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>"));

        // 分页设置
        PageRequest pageRequest = PageRequest.of(0, 10);
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
        System.out.println(page.getTotalElements());
        System.out.println(page.getTotalPages());
        System.out.println(page.getNumber());
        System.out.println(page.getSize());
        for (DiscussPost post : page) {
            System.out.println(post);
        }
    }


}
