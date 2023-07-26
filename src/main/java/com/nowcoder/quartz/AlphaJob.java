package com.nowcoder.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @Author Xiao Guo
 * @Date 2023/5/13
 */
// 实现 Job 接口
public class AlphaJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // 当前线程名称 + "任务"
        System.out.println(Thread.currentThread().getName() + ": execute a quartz job.");
    }
}
