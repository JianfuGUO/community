package com.nowcoder.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.Objects;

/**
 * @Author Xiao Guo
 * @Date 2023/2/22
 */

// @Data：相当于上面这些注解的作用，自动生成get、set、toString、equals、equals和无参构造方法
// @Builder：自动生成set流，从而就不用写一大堆的setting方法设置对象属性了
//@NoArgsConstructor：自动生成无参构造方法
// @AllArgsConstructor：自动生成全参构造方法
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "discusspost", shards = 6, replicas = 3) // (索引，分片，副本)
public class DiscussPost {

    // 实体中的属性与索引中的字段的对应关系
    @Id // 主键
    private int id;

    @Field(type = FieldType.Integer) // 接口的常量
    // userId为外键，关联user的名称
    private int userId;

    // 重点是 title 和 content 来个要搜索的字段
    // 内容示例：互联网校招
    // ik_max_word:存储的时候拆分出较多的单词
    // ik_smart：搜索时拆分出较少的单词
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart") // (文本，存储的解析器，搜索的解析器)
    private String title;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart") // (文本，存储的解析器，搜索的解析器)
    private String content;

    @Field(type = FieldType.Integer) // 接口的常量
    private int type;

    @Field(type = FieldType.Integer) // 接口的常量
    private int status;

    @Field(type = FieldType.Date) // 接口的常量
    private Date createTime;

    @Field(type = FieldType.Integer) // 接口的常量
    private int commentCount;

    @Field(type = FieldType.Double) // 接口的常量
    private double score;
}
