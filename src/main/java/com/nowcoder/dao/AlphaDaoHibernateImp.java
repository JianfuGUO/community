package com.nowcoder.dao;

import org.springframework.stereotype.Repository;

/**
 * @Author Xiao Guo
 * @Date 2023/2/18
 */

// 主程序所在包下
// 加上四个注解中的一个
@Repository("alphaHilbernate") // Bean的名称，默认名字为类名首字母小写
public class AlphaDaoHibernateImp implements AlphaDao{
    @Override
    public String select() {
        return "Hibernate";
    }
}
