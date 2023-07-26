package com.nowcoder.service;

import com.nowcoder.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * 点赞
 *
 * @Author Xiao Guo
 * @Date 2023/4/8
 */
@Service
public class LikeService {

    // 高度封装的“RedisTemplate”类，操作数据进行redis库的存取
    @Autowired
    private RedisTemplate redisTemplate;

    // 点赞
    // 用户 点赞的实体 实体的id
    public void like(int userId, int entityType, int entityId, int entityUserId) {
//        // redis数据库里面的key
//        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
//
//        // 第1次点赞，第2次取消点赞。
//        Boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
//        if (isMember) {
//            // Set集合有，表明点过赞，则取消点赞
//            redisTemplate.opsForSet().remove(entityLikeKey, userId);
//        } else {
//            // Set集合没有，进行点赞
//            // add(K key, V… var2):向key中批量添加值
//            redisTemplate.opsForSet().add(entityLikeKey, userId);
//        }

        // 保证事务性，一次点赞两处增加，编程式事务。
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                // redis数据库里面的key
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);

                // 查询要在事务之前，不能放在事务里面，否则事务提交之后才执行。
                Boolean isMember = operations.opsForSet().isMember(entityLikeKey, userId);

                //开启事务
                operations.multi();

                if (isMember) {
                    // 取消点赞
                    operations.opsForSet().remove(entityLikeKey, userId);
                    // 数量减一
                    operations.opsForValue().decrement(userLikeKey);
                } else {
                    // 点赞
                    operations.opsForSet().add(entityLikeKey, userId);
                    // 数量加一
                    operations.opsForValue().increment(userLikeKey);
                }

                // 提交事务
                return operations.exec();
            }
        });
    }

    // 查询某实体点赞的数量
    // 根据redis的key来查询值Set集合的元素个数
    public long findEntityLikeCount(int entityType, int entityId) {
        // redis数据库里面的key
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);

        // 根据redis的key来查询数量
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    // 查询某人对某实体的点赞状态
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        // redis数据库里面的key
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);

        // members(K key)：获取key中的值
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }

    // 查询某个用户获得的赞
    public int findUserLikeCount(int userId) {
        // redis数据库里面的key
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);

        // redis由key来获取值
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);

        return count == null ? 0 : count.intValue();
    }
}
