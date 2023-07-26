package com.nowcoder.controller;

import com.nowcoder.service.DataService;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

/**
 * @Author Xiao Guo
 * @Date 2023/5/12
 */
@Controller // 展现数据
public class DataController {

    // 进行 UV 和 DAU 数据统计
    @Autowired
    private DataService dataService;

    // 打开统计页面
    @RequestMapping(path = "/data", method = {RequestMethod.GET, RequestMethod.POST})
    public String getDataPage() {

        return "/site/admin/data";
    }

    // 统计网站UV
    @PostMapping(path = "/data/uv")
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date end, Model model) {

        // UV 统计结果
        long uv = dataService.calculateUV(start, end);
        model.addAttribute("uvResult", uv);
        model.addAttribute("uvStartDate", start);
        model.addAttribute("uvEndDate", end);

        // 返回页面模板给 DispatcherServlet，后续由DispatcherServlet来处理
        // return "/site/admin/data";

        // 转发：forward表示当前此方法只能将模板处理一半，需要另外一个方法继续处理请求（一个同级别的请求而不是模板）
        // 转发是一次请求
        return "forward:/data";
    }

    // 统计活跃用户
    @PostMapping(path = "/data/dau")
    public String getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                         @DateTimeFormat(pattern = "yyyy-MM-dd") Date end, Model model) {
        // dau 统计结果
        long dau = dataService.calculateDAU(start, end);

        model.addAttribute("dauResult", dau);
        model.addAttribute("dauStartDate", start);
        model.addAttribute("dauEndDate", end);

        // 转发
        return "forward:/data";
    }
}
