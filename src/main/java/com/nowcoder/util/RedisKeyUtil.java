package com.nowcoder.util;

/**
 * @Author Xiao Guo
 * @Date 2023/4/8
 */

public class RedisKeyUtil {
    // 拼接RedisKey的分隔符
    private static final String SPLIT = ":";
    // 前缀
    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    // 统计我收到的赞
    // User为key的前缀
    private static final String PREFIX_USER_LIKE = "like:user";

    // 4.16关注、取消关注
    private static final String PREFIX_FOLLOWEE = "followee"; // 目标
    private static final String PREFIX_FOLLOWER = "follower"; // 粉丝

    // 4.23 优化登录模块
    // 使用Redis存储验证码
    private static final String PREFIX_KAPTCHA = "kaptcha";

    // 使用Redis存储登录凭证
    private static final String PREFIX_TICKET = "ticket";

    // Redis缓存用户信息
    private static final String PREFIX_USER = "user";

    // 7.5 网站数据统计
    // 独立访客(UV，网站访问量) HyperLogLog
    private static final String PREFIX_UV = "uv";
    // 日活跃用户(DAU) Bitmap
    private static final String PREFIX_DAU = "dau";

    // 7.7 热帖排行
    // 帖子的分数
    private static final String PREFIX_POST = "post";


    // 某个实体的赞
    // redisKey的形式----like:entityType:entityId
    // redis数据的（KEY,VALUE）的形式---(like:entityType:entityId,set(UserId))
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // 某个用户的赞
    // redisKey的形式----like:user:userId
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    // 某个用户关注的实体
    // 某个用户关注的实体(实体包括帖子、评论、用户)，按照实体分别存
    // redisKey的形式----followee:userId:entityType
    // redis数据的（KEY,VALUE）的形式---(followee:userId:entityType, Zset(entityId,now)) 分数即为now
    // 有时候需要统计你所关注东西的先后顺序，所以值需要设计成 Zset(entityId,now)
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    // 某个实体拥有的粉丝
    // 某个实体拥有的粉丝（实体包括帖子、评论、用户）
    // redisKey的形式----follower:entityType:entityId
    // redis数据的（KEY,VALUE）的形式---(follower:entityType:entityId, Zset(userId,now)) 分数即为now
    // 有时候需要统计你所关注东西的先后顺序，所以值需要设计成 Zset(userId,now)
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    // 登录验证码
    // owner为验证码用户的临时凭证
    // redisKey的形式----kaptcha:随机字符串
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    // 登录的凭证
    // redisKey
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    // 使用Redis缓存用户信息
    // redisKey
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    // 单日UV
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    // 区间UV（比如某一周的访问量）
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    // 单日活跃用户
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    //区间活跃用户
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    // 帖子分数 post:score->【需要重新计算分数的帖子id集合】
    // 分数有变化的帖子
    public static String getPostScoreKey(){
        return PREFIX_POST + SPLIT + "score";
    }

}
