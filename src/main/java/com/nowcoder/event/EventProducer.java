package com.nowcoder.event;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.entity.Event;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @Author Xiao Guo
 * @Date 2023/4/22
 */

// 生产者
@Component
public class EventProducer {

    // Spring 已经整合 Kafka
    // KafkaTemplate是Spring Kafka提供的一个用于发送Kafka消息的高级模板类。
    // KafkaTemplate封装了生产者API，提供了一个简单的API用于向Kafka主题发送消息。
    @Autowired(required = false)
    private KafkaTemplate kafkaTemplate;

    // 处理事件
    public void fireEvent(Event event) {
        // 将事件发布到指定的主题
        // send（主题，内容（内容为JSON格式））
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));

    }


}
