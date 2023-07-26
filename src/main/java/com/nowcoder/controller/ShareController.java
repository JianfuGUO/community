package com.nowcoder.controller;

import com.nowcoder.entity.Event;
import com.nowcoder.event.EventProducer;
import com.nowcoder.util.CommunityConstant;
import com.nowcoder.util.CommunityUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author Xiao Guo
 * @Date 2023/5/19
 */
@Controller
public class ShareController implements CommunityConstant {

    // 实例化一个 logger
    private static final Logger logger = LoggerFactory.getLogger(ShareController.class);

    // 异步方式生成长图片
    // 使用事件绑定的形式，利用kafka消息队列来实现
    @Autowired
    private EventProducer eventProducer;

    // 域名
    @Value("${community.path.domain}")
    private String domain;

    // 项目访问路径
    @Value("${server.servlet.context-path}")
    private String contextPath;

    // 图片存放位置
    @Value("${wk.image.storage}")
    private String wkImageStorage;

    // 注入share空间（截长图）
    @Value("${qiniu.bucket.share.url}")
    private String shareBucketUrl;

    /**
     * 根据 wk工具和 htmlUrl 访问路径，生成长图
     *
     * @param htmlUrl
     * @return
     */
    @GetMapping(path = "/share") // ① ？拼参数： /students?current=1&limit=20
    @ResponseBody // 异步请求，返回JSON数据
    public String share(@RequestParam(name = "htmlUrl") String htmlUrl) {
        // 图片文件名随机
        String fileName = CommunityUtil.generateUUID();

        // 异步方式生成长图片
        Event event = new Event()
                // 设置主题
                .setTopic(TOPIC_SHARE)
                // 携带参数
                .setData("htmlUrl", htmlUrl)
                .setData("fileName", fileName)
                // 后缀
                .setData("suffix", ".png");

        // 触发事件
        eventProducer.fireEvent(event);

        // 返回访问给页面的数据：
        // 查看图片的访问路径
        Map<String, Object> map = new HashMap<>();
//        map.put("shareUrl", domain + contextPath + "/share/image/" + fileName);
        map.put("shareUrl", shareBucketUrl + "/" + fileName);

        return CommunityUtil.getJSONString(0, null, map);
    }

    // 废弃--（从七牛云获取图片）
    /**
     * 获取长图，输出给 HTML 页面
     */
    @GetMapping(path = "/share/image/{fileName}") // ②参数为请求路径的一部分： /student/123
    public void getShareImage(@PathVariable("fileName") String fileName, HttpServletResponse response) {

        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("文件名不能为空!");
        }

        // 设置响应的内容类型为PNG图像
        response.setContentType("image/png");

        // 输出图片到本地
        File file = new File(wkImageStorage + "/" + fileName + ".png");

        try {
            // 获取用于向客户端发送数据的输出流对象。在Java Servlet中，可以使用该输出流将数据发送到客户端。
            OutputStream os = response.getOutputStream();

            // 输入流对象，可以从文件中读取数据。
            FileInputStream fis = new FileInputStream(file);
            // 缓冲区
            byte[] buffer = new byte[1024];
            int b = 0;

            // 通过循环读取文件输入流并写入输出流，可以实现逐块地将文件内容发送给客户端。
            while ((b = fis.read(buffer)) != -1) {
                // 循环读取文件输入流中的数据，将读取的字节数保存在变量b中
                // 将读取到的数据通过输出流写入到HTTP响应的输出流中
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("获取长图失败：" + e.getMessage());
        }

    }
}
