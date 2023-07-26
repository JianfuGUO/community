package com.nowcoder.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author Xiao Guo
 * @Date 2023/2/19
 */

// @Data：相当于上面这些注解的作用，自动生成get、set、toString、equals、equals和无参构造方法
// @Builder：自动生成set流，从而就不用写一大堆的setting方法设置对象属性了
//@NoArgsConstructor：自动生成无参构造方法
// @AllArgsConstructor：自动生成全参构造方法
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private int id;
    private String username;
    private String password;
    private String salt;
    private String email;
    private int type;
    private int status;
    private String activationCode;
    private String headerUrl;
    private Date createTime;
}
