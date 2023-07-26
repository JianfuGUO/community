package com.nowcoder.controller;

import com.nowcoder.entity.DiscussPost;
import com.nowcoder.entity.Page;
import com.nowcoder.entity.User;
import com.nowcoder.service.DiscussPostService;
import com.nowcoder.service.LikeService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 表现层
 *
 * @Author Xiao Guo
 * @Date 2023/2/24
 */
@Controller
public class HomeController implements CommunityConstant {

    // 注入业务层 Service 的 Bean
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    // 查询赞的数量
    @Autowired
    private LikeService likeService;

    // 根路径的处理
    @GetMapping(path = "/")
    public String root(){
        // 重定向到/index访问路径
        return "forward:/index";
    }

    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page,
                               @RequestParam(name = "orderMode", defaultValue = "0") int orderMode) {
        //方法调用前，SpringMVC 会自动实例化 Model 和 Page,并将 Page 注入 Model
        // 所以，在thymeleaf中可以直接访问Page对象中的数据.
        // 给 Page 设置一些默认值
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index?orderMode=" + orderMode);

        // 分页查询的一页数据
        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit(), orderMode);

        // 页的数据不含user的具体信息，构造Map集合存储完整信息
        List<Map<String, Object>> discussPosts = new ArrayList<>();

        if (list != null) {
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);

                // 查询帖子赞的数量
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("orderMode", orderMode);
        return "/index";
    }

    /**
     * 统一异常处理
     * controller发生异常，统一处理日志，记录完日志后访问 500.png 页面（人为重定向过去）
     */
    @GetMapping(path = "/error")
    public String getErrorPage() {
        return "/error/500";
    }

    // 拒绝访问时的提示页面
    @RequestMapping(path = "/denied", method = RequestMethod.GET)
    public String getDeniedPage() {
        return "/error/404";
    }
}
