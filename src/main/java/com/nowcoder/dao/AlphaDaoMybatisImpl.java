package com.nowcoder.dao;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

/**
 * @Author Xiao Guo
 * @Date 2023/2/18
 */

@Repository
@Primary //加上此注解，Bean会被优先加载到容器中，有更高的优先级
public class AlphaDaoMybatisImpl implements AlphaDao{
    @Override
    public String select() {
        return "Mybatis";
    }
}
