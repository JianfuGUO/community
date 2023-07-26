package com.nowcoder.dao;

import com.nowcoder.entity.User;
import org.apache.ibatis.annotations.Mapper;

// 在接口类上添加了@Mapper，在编译之后会生成相应的接口实现类。
// 使用注解让 Spring 容器来管理此接口的是实现类，才能自动装配
@Mapper
public interface UserMapper {

    User selectById(int id);

    User selectByName(String username);

    User selectByEmail(String email);

    // 返回插入的行数
    int insertUser(User user);

    // 返回修改的条数（修改了几行）
    int updateStatus(int id, int status);

    int updateHeader(int id, String headerUrl);

    int updatePassword(int id, String password);
}
