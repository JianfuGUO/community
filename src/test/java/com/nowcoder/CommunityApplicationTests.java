package com.nowcoder;

import com.nowcoder.dao.AlphaDao;
import com.nowcoder.entity.DiscussPost;
import com.nowcoder.service.AlphaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import java.text.SimpleDateFormat;
import java.util.Date;


@SpringBootTest
// 启用SpringBoot的那个正常运行的类作为配置类来运行项目
@ContextConfiguration(classes = CommunityApplication.class)
// 实现该接口来获取Spring容器
class CommunityApplicationTests implements ApplicationContextAware {
    // Spring容器
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    // 编写测试方法来测试容器
    @Test
    public void testApplicationContext() {
        // 打印的是类名+@hashcode值
        System.out.println(applicationContext);

        // 从容器中获取 Dao 实现类（Bean）-- 按照接口或者实现类的类型都行
        AlphaDao alphaDao = applicationContext.getBean(AlphaDao.class);
        System.out.println(alphaDao.select());

        // 获取Bean：按照Bean的名称
        AlphaDao alphaHibernate = applicationContext.getBean("alphaHilbernate", AlphaDao.class);
        System.out.println(alphaHibernate.select());
    }

    // 测试Spring容器管理Bean的方式
    @Test
    public void testBeanManagement(){
        // 通过容器获取Bean
        AlphaService alphaService = applicationContext.getBean(AlphaService.class);
        System.out.println(alphaService);

        AlphaService alphaService2 = applicationContext.getBean(AlphaService.class);
        System.out.println(alphaService2);
    }

    // 测试装配一个第三方Bean
    @Test
    public void testBeanConfig(){
        // 通过容器获取Bean
        SimpleDateFormat simpleDateFormat = applicationContext.getBean(SimpleDateFormat.class);

        // 使用当前的对象实例化当日期
        System.out.println(simpleDateFormat.format(new Date()));
    }

    // 演示依赖注入
    @Autowired
    @Qualifier("alphaHilbernate") // 接口含有多个实现类，根据名称进行注入（过滤）
    private AlphaDao alphaDao;

    @Autowired
    private AlphaService alphaService;

    @Autowired
    private SimpleDateFormat simpleDateFormat;

    // 演示依赖注入
    @Test
    public void testDependencyInjection(){
        System.out.println(alphaDao);
        System.out.println(alphaService);
        System.out.println(simpleDateFormat);
    }

    // 测试类的默认属性
    @Test
    public void testDiscussPost(){
        DiscussPost post = new DiscussPost();
        System.out.println(post);
    }


}
