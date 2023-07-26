package com.nowcoder.config;

import com.nowcoder.quartz.AlphaJob;
import com.nowcoder.quartz.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

/**
 * @Author Xiao Guo
 * @Date 2023/5/13
 */
// 配置 -》 数据库 -》 调用
// 作用：配置信息被写到数据库中，以后会直接读取数据库中表的配置信息来执行任务
@Configuration
public class QuartzConfig {

    // FactoryBean可简化bean的实例化过程:
    // 1.Spring通过FactoryBean封装了某些bean的实例化过程
    // 2.将FactoryBean装配到spring容器里后
    // 3.再将FactoryBean注入给其他的bean
    // 4.该bean得到的是FactoryBean管理的对象实例

    // ----------------------------------------------------------------------- //
    // 配置JobDetail  要把数据库里的信息删掉，因为这是一次初始化
    // 注意：把bean注解注掉后如果不把数据库信息删除，则定时任务仍生效，会读取数据库中的信息生	//成【bean】
    // 配置 JobDetail---》任务详情
//     @Bean
    public JobDetailFactoryBean alphaJobDetail(){
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();

        // 设置一些属性
        factoryBean.setJobClass(AlphaJob.class);
        // 设置任务job名称
        factoryBean.setName("alphaJob");
        // 声明job的名字
        factoryBean.setGroup("alphaJobGroup");
        // 声明job是持久的保存,哪怕将来任务的触发器都没有了也保留任务
        factoryBean.setDurability(true);
        // 声明job是可恢复的
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

    // 配置Trigger---》任务触发时机
    // 配置Trigger，trigger依赖 JobDetail，传进来的参数是JobDetail的实例对象
    // 配置Trigger---》（简单，复杂）--- 》(SimpleTriggerFactoryBean, CronTriggerFactoryBean)
//     @Bean
    public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail){

        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        // 声明trigger是对哪个JobDetail的
        factoryBean.setJobDetail(alphaJobDetail);
        // 声明trigger的名字
        factoryBean.setName("alphaTrigger");
        // 声明trigger所在的组名
        factoryBean.setGroup("alphaGroup");
        // 声明多久执行一次任务，单位是毫秒 --3秒
        factoryBean.setRepeatInterval(3000);
        // trigger底层是要存储job的状态，用JobDataMap去存储
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }

    // 刷新帖子分数任务
    // 配置 JobDetail---》任务详情
     @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail(){
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();

        // 设置一些属性
        factoryBean.setJobClass(PostScoreRefreshJob.class);
        // 设置任务job名称
        factoryBean.setName("postScoreRefreshJob");
        // 声明job的组（同一个项目放到同一个组中）
        factoryBean.setGroup("communityJobGroup");
        // 声明job是持久的保存,哪怕将来任务的触发器都没有了也保留任务
        factoryBean.setDurability(true);
        // 声明job是可恢复的
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

    // 刷新帖子分数任务
    // 配置Trigger---》任务触发时机
    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshJobDetail){
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        // 声明trigger是对哪个JobDetail的
        factoryBean.setJobDetail(postScoreRefreshJobDetail);
        // 声明trigger的名字
        factoryBean.setName("postScoreRefreshTrigger");
        // 声明trigger所在的组名
        factoryBean.setGroup("communityTriggerGroup");
        // 声明多久执行一次任务，单位是毫秒 --5min
        factoryBean.setRepeatInterval(1000 * 60 * 5);
//        factoryBean.setRepeatInterval(1000 * 5);
        // trigger底层是要存储job的状态，用JobDataMap去存储
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }
}
