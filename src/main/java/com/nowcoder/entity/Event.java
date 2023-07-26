package com.nowcoder.entity;


import java.util.HashMap;
import java.util.Map;

/**
 * @Author Xiao Guo
 * @Date 2023/4/22
 */


public class Event {

    // 主题（事件类型）
    private String topic;

    // 事件由谁发出的
    private int userId;

    // 人的操作：点赞\回复\关注
    // 事件发生在哪个实体上
    private int entityType;
    private int entityId;
    // 实体的作者
    private int entityUserId;

    // 额外的一些信息
    private Map<String, Object> data = new HashMap<>();

    public String getTopic() {
        return topic;
    }

    // 修改 set 方法,改为含有返回值的（返回值为该事件类本身）
    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityUserId() {
        return entityUserId;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    // Map进行特殊处理
    public Map<String, Object> getData() {
        return data;
    }

    public Event setData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }
}
