package com.nowcoder.event;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.entity.DiscussPost;
import com.nowcoder.entity.Event;
import com.nowcoder.entity.Message;
import com.nowcoder.service.DiscussPostService;
import com.nowcoder.service.ElasticSearchService;
import com.nowcoder.service.MessageService;
import com.nowcoder.util.CommunityConstant;
import com.nowcoder.util.CommunityUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 * @Author Xiao Guo
 * @Date 2023/4/22
 */
// 消费者
// 消费三个主题的数据
@Component
public class EventConsumer implements CommunityConstant {

    // 记录日志
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    // 私信
    @Autowired
    private MessageService messageService;

    // 先从MySQL数据库中查询
    @Autowired
    private DiscussPostService discussPostService;

    // 再存入到 ElasticSearch 服务器中
    @Autowired
    private ElasticSearchService elasticSearchService;

    // wk运行命令
    @Value("${wk.image.command}")
    private String wkImageCommand;

    // wk长图片存放路径
    @Value("${wk.image.storage}")
    private String wkImageStorage;

    // 线程池(开启线程自动处理后台程序，执行定时任务)
    @Autowired(required = false)
    private ThreadPoolTaskScheduler taskScheduler;

    // 标识用户身份
    @Value("${qiniu.key.access}")
    private String accessKey;

    // 上传具体内容为内容加密
    @Value("${qiniu.key.secret}")
    private String secretKey;

    // 长图空间名称
    @Value("${qiniu.bucket.share.name}")
    private String shareBucketName;

    // 消息监听的主题
    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_FOLLOW, TOPIC_LIKE})
    public void handleCommentMessages(ConsumerRecord record) {

        if (record == null || record.value() == null) {
            logger.error("消息的内容为空!");
            return;
        }

        // 把发送过来的JSON数据转化为Event实体对象
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);

        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }

        // 发送站内对象
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        // 系统通知页面的内容
        // 拼出如下内容---用户nowcoder评论了你的帖子,点击查看!
        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());

        // event对象里面的额外数据
        if (!event.getData().isEmpty()) {
            // foreach方式遍历map集合
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                // (key,value)
                content.put(entry.getKey(), entry.getValue());
            }
        }

        // 将Map数据以JSON字符串的形式存储起来
        message.setContent(JSONObject.toJSONString(content));

        // 存取message 数据到 message 表中
        // 消费者将数据最终存储到 MySQL数据库的message表中
        messageService.addMessage(message);
    }

    // 消费发帖事件
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record) {
        // 先进行判断
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空!");
            return;
        }

        // 把发送过来的JSON数据转化为Event实体对象
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);

        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }

        // 先从MySQL数据库中查询
        DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
        // 再存入到 ElasticSearch 服务器中
        elasticSearchService.saveDiscussPost(post);

    }

    // 消费删帖事件
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record) {
        // 先进行判断
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空!");
            return;
        }

        // 把发送过来的JSON数据转化为Event实体对象
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);

        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }

        // DiscussPostRepository继承原始的 ElasticsearchRepository 接口，并指明（泛型，主键类型）
        // SpringData会自动为它生成基本增删改查方法（内置了很多方法，可以像数据库一样增删改查）
        // 调用 ElasticSearch
        elasticSearchService.deleteDiscussPost(event.getEntityId());

    }

    // 消费生成长图片事件
    @KafkaListener(topics = {TOPIC_SHARE})
    public void handleShareMessage(ConsumerRecord record) {
        // 先进行判断
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空!");
            return;
        }

        // 把发送过来的JSON数据转化为Event实体对象
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);

        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }

        // 从 map 集合里面取数据
        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");

        // htmlUrl为html访问路径
        // cmd:（wkhtmltoimage.exe所在路径，图片质量，访问的html的地址，html转换为图片后存入的位置和图片名称）
        // 》d:/soft/wkhtmltopdf/bin/wkhtmltoimage --quality 75 https://wkhtmltopdf.org/downloads.html d:/codeJava/workspace3/wk-images/eea6709a9eba40a2aaaf957571e11594.png
        String cmd = wkImageCommand + " --quality 75 "
                + htmlUrl + " " + wkImageStorage + "/" + fileName + suffix;

        try {
            // java（java语言访问）生成长图
            Runtime.getRuntime().exec(cmd); // 比较耗时
            logger.info("生成长图成功：" + cmd); // 先执行完毕
        } catch (IOException e) {
            logger.error("生成长图失败：" + e.getMessage());
        }

        // 定时器等待 Runtime.getRuntime().exec(cmd);
        // 命令执行完毕之后再进行上传到七牛云服务器

        // 启用定时器,监视该图片,一旦生成了,则上传至七牛云.开启新的线程
        UploadTask task = new UploadTask(fileName, suffix);

        // 触发定时器的执行（返回结果），设置调度器的任务与执行频率。（不断执行线程里面重写的run方法）
        ScheduledFuture<?> future = taskScheduler.scheduleAtFixedRate(task, 500);// 参数:(任务，500ms)

        // 将 future 属性及时赋值给匿名内部类，以便后续使用。
        task.setFuture(future);

    }

    // 匿名类
    // 开启新的线程
    class UploadTask implements Runnable {

        // 文件名称
        private String fileName;

        // 文件后缀
        private String suffix;

        // 启动任务的返回值，它可用来停止定时器
        private ScheduledFuture<?> future;

        // 开始时间
        private long startTime; // 超过30s就自动停止线程

        // 上传次数
        private int uploadTimes; // 一般上传一次就能成功，上传3次还未成功：七牛云服务器挂掉或者Network有问题

        // 类的构造器
        public UploadTask(String fileName, String suffix) {
            this.fileName = fileName;
            this.suffix = suffix;
            this.startTime = System.currentTimeMillis();
        }

        // 设置属性
        public void setFuture(ScheduledFuture<?> future) {
            this.future = future;
        }

        // 重写 run 方法
        @Override
        public void run() {

            // 上传失败---生成图片超过30s还未完成
            if (System.currentTimeMillis() - startTime > 30000) {
                logger.error("执行时间过长,终止任务:" + fileName);
                // 停止线程
                future.cancel(true);

                return;
            }

            // 上传失败---上传次数超过3次还未完成（网络原因）
            if (uploadTimes >= 3) {
                logger.error("上传次数过多,终止任务:" + fileName);
                future.cancel(true);
                return;
            }

            // 本地存放长图的完整路径
            String path = wkImageStorage + "/" + fileName + suffix;
            File file = new File(path);

            if (file.exists()) {
                logger.info(String.format("开始第%d次上传[%s].", ++uploadTimes, fileName));
                // 设置响应信息
                StringMap policy = new StringMap();
                policy.put("returnBody", CommunityUtil.getJSONString(0));
                // 生成上传凭证
                Auth auth = Auth.create(accessKey, secretKey);
                String uploadToken = auth.uploadToken(shareBucketName, fileName, 3600, policy);
                // 指定上传机房
                UploadManager manager = new UploadManager(new Configuration(Zone.zone0()));
                try {
                    // 开始上传图片
                    Response response = manager.put(
                            path, fileName, uploadToken, null, "image/" + suffix, false);
                    // 处理响应结果
                    JSONObject json = JSONObject.parseObject(response.bodyString());
                    if (json == null || json.get("code") == null || !json.get("code").toString().equals("0")) {
                        logger.info(String.format("第%d次上传失败[%s].", uploadTimes, fileName));
                    } else {
                        logger.info(String.format("第%d次上传成功[%s].", uploadTimes, fileName));
                        future.cancel(true);
                    }
                } catch (QiniuException e) {
                    logger.info(String.format("第%d次上传失败[%s].", uploadTimes, fileName));
                }
            } else {
                logger.info("等待图片生成[" + fileName + "].");
            }
        }
    }
}
