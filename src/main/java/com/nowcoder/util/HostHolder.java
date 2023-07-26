package com.nowcoder.util;

import com.nowcoder.entity.User;
import org.springframework.stereotype.Component;

/**
 * 使用容器来管理该对象
 * 持有用户信息，用于代替 session 对象
 *
 * @Author Xiao Guo
 * @Date 2023/3/4
 */
@Component
public class HostHolder {

    // （考虑并发的情况，线程隔离）,使用 ThreadLocal 来存值
    private ThreadLocal<User> users = new ThreadLocal<>();

    // 存值
    public void setUser(User user) {
        // ThreadLocal 存值
        users.set(user);
    }

    // 取值
    public User getUser() {
        // ThreadLocal 取值
        return users.get();
    }

    // 清理
    public void clear(){
        users.remove();
    }

}
