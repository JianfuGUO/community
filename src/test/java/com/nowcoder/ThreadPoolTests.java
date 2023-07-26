package com.nowcoder;

import com.nowcoder.service.AlphaService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Author Xiao Guo
 * @Date 2023/5/13
 */

@SpringBootTest
// 启用SpringBoot的那个正常运行的类作为配置类来运行项目
@ContextConfiguration(classes = CommunityApplication.class)
public class ThreadPoolTests {

    // 使用 logger 来记录日志，输出一些内容，可获取 Thread 包括id一些具体信息
    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolTests.class);

    // ---------1.JDK 普通的线程池--------------------- //
    // 使用 Executors 类的静态方法来创建ExecutorService对象
    // 线程池初始化以后里面复用这5个已创建好的线程池
    private ExecutorService executorService = Executors.newFixedThreadPool(5); // 实例化

    // ---------2.JDK可执行定时任务的线程池--------------- //
    // 使用 Executors 类的静态方法来创建ExecutorService对象
    // 线程池初始化以后里面复用这5个已创建好的线程池
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);// 实例化

    // ---------3. Spring普通线程池------------------- //
    @Autowired(required = false)
    private ThreadPoolTaskExecutor taskExecutor;

    // ---------4.Spring可执行定时任务的线程池------------ //
    @Autowired(required = false)
    private ThreadPoolTaskScheduler taskScheduler;

    // ---------5.Spring普通线程池、定时任务线程池的简化使用----------------- //
    @Autowired
    private AlphaService alphaService;

    // 封装一个让线程sleep的方法，解决 junit包下Test方法执行完线程立马就终止，利用sleep方法让其阻塞一会儿
    // m 毫秒
    private void sleep(long m){
        try {
            Thread.sleep(m);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 演示
    // 1.JDK普通线程池
    @Test
    public void testExecutorService(){
        // 线程池需要你给其一任务，对应会安排一个线程来执行
        // 使用线程池要传一个Runnable对象
        // 创建了一个实现了 Runnable 接口的匿名内部类对象
        // 这段代码创建的对象是实现了 Runnable 接口的类的实例。
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("hello ExecutorService");
            }
        };

        for (int i = 0; i < 10; i++) {
            executorService.submit(task);
        }

        // 当前线程等待10秒
        sleep(10000);
    }

    // 2.JDK定时任务的线程池(可设置时间间隔，不断进行自动重复执行)
    @Test
    public void testScheduledExecutorService(){
        // 线程池需要你给其一任务，对应会安排一个线程来执行
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("hello ScheduledExecutorService");
            }
        };

        // 以固定的周期去执行
        // 10s后执行，每隔1s执行一次，知道30秒之后这个test方法结束
        scheduledExecutorService.scheduleAtFixedRate(task,10000,1000, TimeUnit.SECONDS);

        // 当前线程等待30秒
        sleep(30000);
    }

    // 3.Spring 普通线程池
    // 可配置核心线程数、最大线程数、缓冲队列的大小，比JDK自带的线程池好用
    @Test
    public void testThreadPoolTaskExecutor(){
        // 线程池需要你给其一任务，对应会安排一个线程来执行
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("hello ThreadPoolTaskExecutor");
            }
        };

        for (int i = 0; i < 10; i++) {
            taskExecutor.submit(task);
        }

        // 当前线程等待10秒
        sleep(10000);
    }

    //  4.Spring 定时任务线程池
    @Test
    public void testThreadPoolTaskScheduler(){
        // 线程池需要你给其一任务，对应会安排一个线程来执行
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("hello ThreadPoolTaskScheduler");
            }
        };

        // 以固定的周期去执行
        // 当前时间10s后执行，每隔1s执行一次，知道30秒之后这个test方法结束
        Date startTime = new Date(System.currentTimeMillis() + 10000);
        taskScheduler.scheduleAtFixedRate(task,startTime,1000);

        // 当前线程等待30秒
        sleep(30000);
    }

    // 5.Spring普通线程池的简化使用
    @Test
    public void testThreadPoolTaskExecutorSimple(){
        for (int i = 0; i < 10; i++) {
            // Spring 底层会以多线程的方式调用此方法
            alphaService.execute1();
        }

        // 当前线程等待20秒
        sleep(20000);
    }

    // 6.Spring可执行定时任务线程池的简化使用
    // 任何程序执行，该方法都会自动被调用
    @Test
    public void testThreadPoolTaskSchedulerSimple(){
        // 当前线程等待30秒
        sleep(30000);
    }
}
