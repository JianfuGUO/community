package com.nowcoder.service;

import com.nowcoder.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @Author Xiao Guo
 * @Date 2023/5/11
 */
@Service // 交给 Spring 容器管理
public class DataService {

    // 操作 redis 数据库
    @Autowired
    private RedisTemplate redisTemplate;

    // 对日期进行格式化
    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

    // 将指定的IP计入UV
    public void recordUV(String ip) {
        // redis 数据库的 key
        String redisKey = RedisKeyUtil.getUVKey(df.format(new Date()));
        // 存数据到 redis 中
        redisTemplate.opsForHyperLogLog().add(redisKey, ip);
    }

    // 统计指定日期范围内的UV
    // 数据可能比较大
    public long calculateUV(Date start, Date end) {
        // 任何一个参数为空则抛异常
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        // 整理该日期范围内的key
        List<String> keyList = new ArrayList<>();

        // 实例化一个 Calendar，该对象表示当前时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        // 循环时间获取 redisKey
        while (!calendar.getTime().after(end)) {
            // 获取当前时间
            String key = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
            keyList.add(key);
            calendar.add(Calendar.DATE, 1);
        }

        // 合并这些数据
        // redisKey
        String redisKey = RedisKeyUtil.getUVKey(df.format(start), df.format(end));
        // 存入 redis 中--------参数（新的redisKey,要合并的key1,要合并的key2,要合并的key3）
        redisTemplate.opsForHyperLogLog().union(redisKey, keyList.toArray());

        // 返回统计结果
        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }

    // 将指定用户计入DAU
    public void recordDAU(int userId) {
        // redis 数据库的 key
        String redisKey = RedisKeyUtil.getDAUKey(df.format(new Date()));
        // 操作 Bitmap 这种数据结合,将索引为 usrId 的 bit 位置置为 true
        redisTemplate.opsForValue().setBit(redisKey, userId, true);
    }

    // 统计指定日期范围内的DAU
    public long calculateDAU(Date start, Date end) {
        // 参数不能为空
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        // 整理该日期范围内的key
        // Bitmap 的数据进行or运算合并，传入的RedisKey的形式为byte[]的格式，需将String转换为字符串数组
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            // 获取当前时间对应的 redisKey
            String key = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
            // RedisKey由String 格式转换为 byte[] 格式
            keyList.add(key.getBytes());
            // 日期加1
            calendar.add(Calendar.DATE, 1);
        }

        // 进行 OR 运算
        // 使用 Redis 底层的连接来实现
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                // 要存入 redis 数据库新的 key
                String redisKey = RedisKeyUtil.getDAUKey(df.format(start), df.format(end));

                // 进行 or 操作
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(), keyList.toArray(new byte[0][0]));

                // 合并后数据的个数
                return connection.bitCount(redisKey.getBytes());
            }
        });

    }
}
