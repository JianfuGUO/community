package com.nowcoder.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @Author Xiao Guo
 * @Date 2023/2/25
 */

public class CommunityUtil {

    // 生成随机字符串
    public static String generateUUID() {
        // 将所有-替换为空格
        return UUID.randomUUID().toString().replace("-", "");
    }

    // MD5加密
    // hello ---> abc123def456 加密过程不可逆，但加密密文唯一
    // hello + 3ed48(随机字符串) ---> abc123def456dwsd
    public static String md5(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }

        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    // ----------------------------业务数据存储在 json 里面------------------------------------ //
    // 编号、提示、业务数据
    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        // 遍历 map 集合存取业务数据
        if (map != null) {
            // map 的 key 集合
            for (String key : map.keySet()) {
                // 按 key 取 value
                json.put(key, map.get(key));
            }
        }

        // 将 json 对象转换成 json 字符串
        return json.toJSONString();
    }

    // 对此方法进行方法重载
    public static String getJSONString(int code, String msg) {
        // 调用上面的方法
        return getJSONString(code, msg, null);
    }

    // 对此方法进行方法重载
    public static String getJSONString(int code) {
        // 调用上面的方法
        return getJSONString(code, null, null);
    }

    public static void main(String[] args) {
        Map<String,Object> map = new HashMap<>();
        map.put("name","zhangsan");
        map.put("age",25);

        System.out.println(getJSONString(0,"ok",map));
    }
}
