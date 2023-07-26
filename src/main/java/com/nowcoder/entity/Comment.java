package com.nowcoder.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author Xiao Guo
 * @Date 2023/3/8
 */

// @Data：相当于上面这些注解的作用，自动生成get、set、toString、equals、equals和无参构造方法
// @Builder：自动生成set流，从而就不用写一大堆的setting方法设置对象属性了
//@NoArgsConstructor：自动生成无参构造方法
// @AllArgsConstructor：自动生成全参构造方法
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    private int id;
    private int userId;
    private int entityType;
    private int entityId;
    private int targetId;
    private String content;
    private int status;
    private Date createTime;
}
