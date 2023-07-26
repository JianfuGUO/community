package com.nowcoder.controller;

import com.nowcoder.entity.DiscussPost;
import com.nowcoder.entity.Page;
import com.nowcoder.service.ElasticSearchService;
import com.nowcoder.service.LikeService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author Xiao Guo
 * @Date 2023/5/4
 */
@Controller // Spring 容器来管理
public class SearchController implements CommunityConstant {

    // ES 查询
    @Autowired
    private ElasticSearchService elasticSearchService;

    // user 相关信息
    @Autowired
    private UserService userService;

    // 点赞数量信息
    @Autowired
    private LikeService likeService;

    // 搜索帖子
    // 路径传参 search？keyword=xxx
    @GetMapping(path = "/search")
    // 默认返回 .html 类型数据
    public String search(String keyword, Page page, Model model) {
        // 我们写的getCurrent是从1开始的
        org.springframework.data.domain.Page<DiscussPost> searchResult =
                elasticSearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());

        // 聚合数据
        List<Map<String, Object>> discussPosts = new ArrayList<>();

        if (searchResult != null) {
            // 遍历存储数据及额外信息
            for (DiscussPost post : searchResult) {
                // 每次实例化一个新的map
                Map<String, Object> map = new HashMap<>();
                // 帖子
                map.put("post", post);
                // 作者
                map.put("user", userService.findUserById(post.getUserId()));
                // 点赞数量
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));

                // 往集合里面的存数据
                discussPosts.add(map);
            }
        }

        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("keyword", keyword);

        // 分页信息
        page.setPath("/search?keyword=" + keyword);
        page.setRows(searchResult == null ? 0 : (int) searchResult.getTotalElements());

        return "/site/search";
    }

}
