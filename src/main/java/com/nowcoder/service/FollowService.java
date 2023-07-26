package com.nowcoder.service;

import com.nowcoder.entity.User;
import com.nowcoder.util.CommunityConstant;
import com.nowcoder.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author Xiao Guo
 * @Date 2023/4/9
 */

@Service
public class FollowService implements CommunityConstant {

    // 高度封装的“RedisTemplate”类，操作数据进行redis库的存取
    @Autowired
    private RedisTemplate redisTemplate;

    // 使用 userService 来查询用户的相关信息
    @Autowired
    private UserService userService;

    // redis存取数据时一次存两种数据：关注目标、粉丝
    // 一项业务包含两次存储，使用redis事务进行管理
    // 关注
    public void follow(int userId, int entityType, int entityId) {

        // 编程式事务
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {

                // redis的key
                // 某个用户关注的实体(实体包括帖子、用户)
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                // 某个实体拥有的粉丝（实体包括帖子、用户）
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                // 开启事务
                operations.multi();

                // 关注（存数据）
                // ZSet()里面存储数据的格式——(key,value,score)
                operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                operations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());

                return operations.exec(); // 执行事务
            }
        });
    }

    // 取关
    public void unfollow(int userId, int entityType, int entityId) {

        // 编程式事务
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {

                // redis的key
                // 某个用户关注的实体(实体包括帖子、用户)
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                // 某个实体拥有的粉丝（实体包括帖子、用户）
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                // 开启事务
                operations.multi();

                // 取消关注（删除数据）
                // 删除指定key-value的数据
                // ZSet().remove(key, value); // Score信息不用管
                operations.opsForZSet().remove(followeeKey, entityId);
                operations.opsForZSet().remove(followerKey, userId);

                return operations.exec(); // 执行事务
            }
        });
    }

    // 查询关注的实体的数量
    public long findFolloweeCount(int userId, int entityType) {
        // redis的 Key
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        // 返回 value 的个数
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    // 查询实体的粉丝的数量
    public long findFollowerCount(int entityType, int entityId) {
        // redis的 Key
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        // 返回 value 的个数
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    // 查询当前用户是否已关注此实体
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        // 目标的redis的key
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        // 查询 ZSet里面有无分数（有--已关注；null--未关注）
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    // 查询用户的具体
    // 查询某用户关注的人(Type为人已确定)
    // 返回给页面你的是user对象、时间等多种数据
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit) {
        // redis的key
        // entityType为用户
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);

        // 按时间倒序查询（目标用户的id值）
        // redis 里面的分页查询，其实应该是把redis里面的数据全部查询出来，offset
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);

        if (targetIds == null) {
            return null;
        }

        // 将 id 和用户的具体信息存储到集合中
        // 通过 userId 获取更为详细的值存储到集合中
        List<Map<String, Object>> list = new ArrayList<>();
        // 遍历 targetIds 里面的 id 值获取更为详细的信息
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            // userService 中根据 id 来查询用户
            User user = userService.findUserById(targetId);
            map.put("user", user);
            // 用户关注的时间
            // redis数据的（KEY,VALUE）的形式---(followee:userId:entityType, Zset(entityId,now)) 分数即为now
            // redisTemplate.opsForZSet().score(redis的key, 值的key)------得到值的value
            Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
            // 小数转化为时间
            map.put("followTime", new Date(score.longValue()));

            // list里面存一个又一个 map
            list.add(map);
        }

        return list;
    }

    // 查询某用户的粉丝
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit) {
        // redis的key
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
        // 返回集合---redis范围查询
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);

        if (targetIds == null) {
            return null;
        }

        // 将 id 和用户的具体信息存储到集合中
        // 通过 userId 获取更为详细的值存储到集合中
        List<Map<String, Object>> list = new ArrayList<>();
        // 遍历 targetIds 里面的 id 值获取更为详细的信息
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            // userService 中根据 id 来查询用户
            User user = userService.findUserById(targetId);
            map.put("user", user);
            // 用户关注的时间
            // redis数据的（KEY,VALUE）的形式---(followee:userId:entityType, Zset(entityId,now)) 分数即为now
            // redisTemplate.opsForZSet().score(redis的key, 值的key)------得到值的value
            Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
            // 小数转化为时间
            map.put("followTime", new Date(score.longValue()));

            // list里面存一个又一个 map
            list.add(map);
        }
        return list;

    }
}
