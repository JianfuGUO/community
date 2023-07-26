package com.nowcoder.dao;

import com.nowcoder.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

// 在接口类上添加了@Mapper，在编译之后会生成相应的接口实现类。
// 使用注解让 Spring 容器来管理此接口的是实现类，才能自动装配
// MyBatis会自动为该接口生成一个代理对象，开发者可以直接使用该代理对象来操作数据库

/**
 * 使用纯注解开发的方式
 */

@Mapper
@Deprecated // 声明这个组件不建议使用了
public interface LoginTicketMapper {

    // 添加数据，方法参数类型为实体类，返回值为影响的行数
    @Insert("INSERT INTO login_ticket(USER_ID, TICKET, STATUS, EXPIRED) values(#{userId},#{ticket},#{status},#{expired})")
    // 设置主键自增
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    // 查询数据，方法参数为ticket,返回值类型为实体类
    @Select("select id,user_id,ticket,status,expired from login_ticket where ticket=#{ticket}")
    LoginTicket selectByTicket(String ticket);

    // 修改状态，更改状态表示失效或者删除
    // 注解里面的动态sql，有报错提示但是不影响
/*    @Update("<script> "
            + "UPDATE login_ticket set status = #{status} where ticket=#{ticket} "
            + "<if test=\"ticket!=null\">"
            + "and 1=1"
            + "</if>"
            + "</script>"
    )*/
    @Update("UPDATE login_ticket set status = #{status} where ticket=#{ticket}")
    int updateStatus(String ticket, int status);
}
