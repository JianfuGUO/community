package com.nowcoder;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;

/**
 * @Author Xiao Guo
 * @Date 2023/4/22
 */

@SpringBootTest
// 启用SpringBoot的那个正常运行的类作为配置类来运行项目
@ContextConfiguration(classes = CommunityApplication.class)
public class KafkaTests {

    // 注入生产者
    @Autowired
    private KafkaProducer kafkaProducer;

    @Test
    public void testKafka() {
        // 主动调用生产者发送消息
        kafkaProducer.sendMessage("test", "hello");
        kafkaProducer.sendMessage("test", "hi");
        kafkaProducer.sendMessage("test", "你好");
        kafkaProducer.sendMessage("test", "在吗");

        // alt + enter 处理异常
        try {
            Thread.sleep(1000 * 10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}

// 生产者---主动的发消息，主动调用方法
@Component // 组件，交给 Spring 容器
class KafkaProducer {

    // Spring 已经整合 Kafka
    // KafkaTemplate是Spring Kafka提供的一个用于发送Kafka消息的高级模板类。
    // KafkaTemplate封装了生产者API，提供了一个简单的API用于向Kafka主题发送消息。
    @Autowired(required = false)
    private KafkaTemplate kafkaTemplate;

    // 发送消息
    // 参数： (主题，消息内容)
    public void sendMessage(String topic, String content) {
        kafkaTemplate.send(topic, content);
    }
}

// 消费者---被动地处理消息，自动去调方法
@Component
class KafkaConsumer {

    // 消息监听的主题
    @KafkaListener(topics = {"test"})
    public void handleMessage(ConsumerRecord record) {
        // 读取消息
        System.out.println(record.value());
    }
}









