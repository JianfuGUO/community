package com.nowcoder.actuator;

import com.nowcoder.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 一个类就是一个自定义端点
 *
 * @Author Xiao Guo
 * @Date 2023/5/30
 */
// 监控数据库是否正常连接
@Component
@Endpoint(id = "database") // 端点的名称，浏览器的访问路径 http://localhost:8080/community/actuator/database
public class DatabaseEndpoint {

    // 实例化一个Logger工具
    private static final Logger logger = LoggerFactory.getLogger(DatabaseEndpoint.class);

    // 数据库连接池
    @Autowired(required = false)
    private DataSource dataSource;

    // 获取数据库的连接信息
    @ReadOperation // 用于标记一个方法作为 Actuator 端点的读取操作，表示这个端点只能用get请求访问
    public String checkConnection() {
        try {
            // 获取数据库的连接信息
            Connection conn = dataSource.getConnection();

            // 关闭数据库连接
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            return CommunityUtil.getJSONString(0, "获取连接成功！");

        } catch (SQLException e) {

            logger.error("获取连接失败：" + e.getMessage());
            return CommunityUtil.getJSONString(1, "获取连接失败！");
        }

    }

}
