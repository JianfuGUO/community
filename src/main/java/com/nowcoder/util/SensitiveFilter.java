package com.nowcoder.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 前缀树敏感词过滤
 *
 * @Author Xiao Guo
 * @Date 2023/3/5
 */
// 交给 Spring 容器来管理
@Component
public class SensitiveFilter {

    // 引入日志文件
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 发现敏感词后替换成的常量
    // 替换符
    private static final String REPLACEMENT = "***";

    // 根节点
    private TrieNode rootNode = new TrieNode();

    // @PostConstruct: 当容器完成了Bean的依赖注入后，就会调用被@PostConstruct注解的方法，用于执行初始化操作。
    @PostConstruct
    public void init() {
        // 从 target 目录下获取这个敏感词文件
        // 将流写在try的小括号中，就不用写finally,会自动配置finally来关流
        try (
                // 字节流
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                // 字节流-->字符流-->缓冲流
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                // 添加到前缀树
                this.addKeyword(keyword);

            }
        } catch (Exception e) {
            logger.error("加载敏感词失败：" + e.getMessage());
        }
    }

    // 将一个敏感词添加到前缀树中去
    private void addKeyword(String keyword) {
        // tempNode相当于一个指针，默认指向根节点
        TrieNode tempNode = rootNode;

        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            // 获取当前节点的子节点
            TrieNode subNode = tempNode.getSubNode(c);

            // 判断之前有没有添加过此节点
            if (subNode == null) {
                // 初始化节点
                subNode = new TrieNode();
                // 将此节点挂在下面
                tempNode.addSubNode(c, subNode);
            }

            // 指向子节点，进入下一轮
            tempNode = subNode;

            // 设置结束标识
            if (i == keyword.length() - 1) {
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 敏感词过滤的主方法
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        // 指针1
        TrieNode tempNode = rootNode;
        // 指针2
        int begin = 0;
        // 指针3
        int position = 0;
        // 结果，变长字符串
        StringBuilder sb = new StringBuilder();

        while (position < text.length()) {
            char c = text.charAt(position);

            // 跳过符号
            if (isSymbol(c)) {
                // 若指针1处于根节点,将此符号计入结果,让指针2向下走一步
                if (tempNode == rootNode) {
                    sb.append(c);
                    begin++;
                }
                // 无论符号在开头或中间，指针3都向下走一步
                position++;
                // 跳过符号，进入下一轮循环
                continue;
            }

            // 不是符号
            // 检查下级节点
            tempNode = tempNode.getSubNode(c);

            if (tempNode == null){
                // 以 begin 开头的字符不是敏感词
                // 将 begin 位置的字符记录下来
                sb.append(text.charAt(begin));
                // 进入下一个位置(指针1、2移位)
                position = ++begin;
                // 指针3归位,重新指向根节点
                tempNode = rootNode;
            }else if (tempNode.isKeywordEnd()){
                // 发现敏感词，将 begin~position 字符串替换掉
                sb.append(REPLACEMENT);
                // 进入下一个位置
                begin = ++position;
                // 指针3归位,重新指向根节点
                tempNode = rootNode;
            }else {
                // 检查下一个字符
                position++;
            }
        }

        // 将最后一批字符计入结果
        sb.append(text.substring(begin));

        return sb.toString();
    }

    // 判断是否为符号
    // 返回值为true 表示为特殊字符
    private boolean isSymbol(Character c) {
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }


    // 前缀树
    // 内部类，定义前缀树的数据结构
    private class TrieNode {

        // 关键词结束标识
        private boolean isKeywordEnd = false;

        // 子节点(key 是下级字符，value 是下级节点)
        // Character 字符
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // 添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        // 获取子节点的办法
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }

}
