package com.nowcoder;

import org.junit.jupiter.api.Test;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.context.ContextConfiguration;

/**
 * @Author Xiao Guo
 * @Date 2023/5/13
 */
@SpringBootTest
// 启用SpringBoot的那个正常运行的类作为配置类来运行项目
@ContextConfiguration(classes = CommunityApplication.class)
public class QuartzTests {

    // 注入调度器
    @Autowired(required = false)
    private Scheduler scheduler;

    @Test
    public void testDeleteJob(){
        try {
            // 把数据库里的alphaJob的定时任务信息删掉
            boolean result = scheduler.deleteJob(new JobKey("alphaJob", "alphaJobGroup"));
            System.out.println(result);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    // 删除 Spring Quartz 已有的配置信息
    @Test
    public void testPostScoreRefreshJob(){
        try {
            // 把数据库里的alphaJob的定时任务信息删掉
            boolean result = scheduler.deleteJob(new JobKey("postScoreRefreshJob", "communityJobGroup"));
            System.out.println(result);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
