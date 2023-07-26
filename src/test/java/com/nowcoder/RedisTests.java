package com.nowcoder;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * @Author Xiao Guo
 * @Date 2023/4/7
 */

@SpringBootTest
// 启用SpringBoot的那个正常运行的类作为配置类来运行项目
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTests {

    // 注入配置的 RedisTemplate
    @Autowired
    private RedisTemplate redisTemplate;

    // 测试String 操作——（String,String）
    @Test
    public void testStrings() {
        String redisKey = "test:count";

        // 存数据
        redisTemplate.opsForValue().set(redisKey, 1);

        // 取数据并操作
        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));

    }

    // 测试hash结构——（String,hash形式的数据）
    @Test
    public void testHashes() {
        String redisKey = "test:user";

        redisTemplate.opsForHash().put(redisKey, "id", 1);
        redisTemplate.opsForHash().put(redisKey, "username", "zhangsan");

        System.out.println(redisTemplate.opsForHash().get(redisKey, "id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey, "username"));
    }

    // 访问列表——（String,Lists）
    @Test
    public void testLists() {
        String redisKey = "test:ids";

        // 列表左侧操作数据
        redisTemplate.opsForList().leftPush(redisKey, 101);
        redisTemplate.opsForList().leftPush(redisKey, 102);
        redisTemplate.opsForList().leftPush(redisKey, 103);

        // 取数据
        System.out.println("获取对应key的集合长度:" + redisTemplate.opsForList().size(redisKey));
        // key为redisKey，索引为0处的value
        System.out.println(redisTemplate.opsForList().index(redisKey, 0));
        // key为redisKey，索引为0-2范围的value
        System.out.println(redisTemplate.opsForList().range(redisKey, 0, 2));

        // 从列表左侧弹出元素
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
    }

    // 访问Set集合数据——（String, Set集合）
    // 无序、不重复、无索引
    @Test
    public void testSets() {
        String redisKey = "test:teachers";

        redisTemplate.opsForSet().add(redisKey, "刘备", "关羽", "张飞", "赵云", "诸葛亮");

        System.out.println("获取对应key的集合长度:" + redisTemplate.opsForSet().size(redisKey));
        // 随机弹出一个值
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        // 统计Set集合中的数据
        System.out.println(redisTemplate.opsForSet().members(redisKey)); // [赵云, 刘备, 张飞, 关羽]

    }

    // 访问SortedSet集合数据——（String, Set集合）
    // 有序、不重复、无索引
    // SortedSet中的每一个元素都带有一个score属性，可以基于score属性对元素排序
    @Test
    public void testSortedSets() {
        String redisKey = "test:students";

        // 存值
        redisTemplate.opsForZSet().add(redisKey, "唐僧", 80);
        redisTemplate.opsForZSet().add(redisKey, "悟空", 90);
        redisTemplate.opsForZSet().add(redisKey, "八戒", 50);
        redisTemplate.opsForZSet().add(redisKey, "沙僧", 70);
        redisTemplate.opsForZSet().add(redisKey, "白龙马", 60);

        // ZCARD key：获取sorted set中的元素个数
        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));
        // ZSCORE key member : 获取sorted set中的指定元素的score值
        System.out.println(redisTemplate.opsForZSet().score(redisKey, "八戒"));
        // 返回排名索引
        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey, "八戒"));
        // ZRANGE key min max：按照score排序后，获取指定排名范围内的元素
        System.out.println(redisTemplate.opsForZSet().reverseRange(redisKey, 0, 2));
    }

    // 测试redis通用命令
    @Test
    public void testGenneralCommand() {
        // 删除某个key
        redisTemplate.delete("test:user");

        // 判断某个key是否存在
        System.out.println(redisTemplate.hasKey("test:user"));

        // 设置key的过期时间
        redisTemplate.expire("test:students", 10, TimeUnit.SECONDS);
    }

    // 绑定redisKey，不用每次都写
    @Test
    public void testBoundOperations() {
        // key
        String redisKey = "test:count";

        // 绑定redisKey，不用每次都写
        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);
        // 原始的值
        System.out.println(operations.get());
        operations.increment();
        operations.increment();
        operations.increment();
        System.out.println(operations.get());
    }

    // 编程式事务(整个代码结构是关键)
    @Test
    public void testTransactional() {
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                // key
                String redisKey = "test:tx";

                // 开启事务
                operations.multi();

                // 添加数据
                operations.opsForSet().add(redisKey, "zhangsan");
                operations.opsForSet().add(redisKey, "lisi");
                operations.opsForSet().add(redisKey, "wangwu");
                // redis事务中查询无结果
                System.out.println(operations.opsForSet().members(redisKey));

                // 提交事务
                return operations.exec();
            }
        });

        System.out.println(obj);
    }

    // 统计20万个重复数据的独立总数
    @Test
    public void testHyperLogLog() {
        String redisKey = "redis:hll:01";

        for (int i = 1; i <= 100000; i++) {
            // 往 redis 里面存数据
            redisTemplate.opsForHyperLogLog().add(redisKey, i);
        }

        for (int i = 1; i <= 100000; i++) {
            int r = (int) (Math.random() * 100000 + 1);
            // 往 redis 里面存数据
            redisTemplate.opsForHyperLogLog().add(redisKey, i);
        }

        // 数据个数
        Long size = redisTemplate.opsForHyperLogLog().size(redisKey);
        System.out.println(size); // 99553
    }

    // 将3组数据合并, 再统计合并后的重复数据的独立总数.
    @Test
    public void testHyperLogLogUnion() {
        String redisKey2 = "test:hll:02";
        for (int i = 1; i <= 10000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey2, i);
        }

        String redisKey3 = "test:hll:03";
        for (int i = 5001; i <= 15000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey3, i);
        }

        String redisKey4 = "test:hll:04";
        for (int i = 10001; i <= 20000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey4, i);
        }

        // 30000条数据，重复了10000条
        // 合并的统计结果存放到新的redisKey里面
        String unionKey = "test:hhl:union";
        // 参数：（新的redisKey，要合并的key1，要合并的key2，要合并的key3）
        redisTemplate.opsForHyperLogLog().union(unionKey, redisKey2, redisKey3, redisKey4);

        // 数据个数
        Long size = redisTemplate.opsForHyperLogLog().size(unionKey);
        System.out.println(size); // 19833
    }

    // 统计一组数据的布尔值
    @Test
    public void testBitMap() {
        String redisKey = "test:bm:01";

        // 记录
        // 索引为1的位置存true
        redisTemplate.opsForValue().setBit(redisKey, 1, true);
        // 索引为4的位置存true
        redisTemplate.opsForValue().setBit(redisKey, 4, true);
        // 索引为7的位置存true
        redisTemplate.opsForValue().setBit(redisKey, 7, true);

        // 查询单个索引位置的bit值
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2));

        // 统计
        // 通过redis底层的连接进行访问(redis底层的逻辑)
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                // 传入的参数未bit数组，故需要将字符串转为byte数组
                return connection.bitCount(redisKey.getBytes());
            }
        });

        System.out.println(obj);
    }

    // 统计3组数据的布尔值, 并对这3组数据做OR运算.
    @Test
    public void testBitMapOperation() {
        String redisKey2 = "test:bm:02";

        // opsForValue:操作字符串，类似 String
        // 索引为0的位置存true
        // 0、1、2
        redisTemplate.opsForValue().setBit(redisKey2, 0, true);
        redisTemplate.opsForValue().setBit(redisKey2, 1, true);
        redisTemplate.opsForValue().setBit(redisKey2, 2, true);

        String redisKey3 = "test:bm:03";
        // 2、3、4
        redisTemplate.opsForValue().setBit(redisKey3, 2, true);
        redisTemplate.opsForValue().setBit(redisKey3, 3, true);
        redisTemplate.opsForValue().setBit(redisKey3, 4, true);

        String redisKey4 = "test:bm:04";
        // 4、5、6
        redisTemplate.opsForValue().setBit(redisKey4, 4, true);
        redisTemplate.opsForValue().setBit(redisKey4, 5, true);
        redisTemplate.opsForValue().setBit(redisKey4, 6, true);

        // 对三个key的数据进行or运算合并，新合并的 redisKey
        String redisKey = "test:bm:or";
        // 9个数据，实际未重复的为0-6共7个bit数据
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(), redisKey2.getBytes(), redisKey3.getBytes(), redisKey4.getBytes());

                return connection.bitCount(redisKey.getBytes());
            }
        });

        System.out.println(obj);

        System.out.println(redisTemplate.opsForValue().getBit(redisKey,0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,2));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,3));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,4));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,5));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,6));
    }

}
