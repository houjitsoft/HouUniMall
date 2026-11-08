package com.houjiang.goods.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Amos
 */
@Repository
public class GoodsRepository {

    private static final Logger logger = LoggerFactory.getLogger(GoodsRepository.class);

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public GoodsRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 执行自定义SQL查询，并返回结果集。
     *
     * @param sql SQL查询语句
     * @return 查询结果集
     */
    public List<Map<String, Object>> executeCustomSql(String sql) {
        String selectStr = new String(Base64.getDecoder().decode(sql.trim()));
        System.out.println(selectStr);
        // 正则表达式匹配 SQL 内容
        String regex = "```sql\\n([\\s\\S]*?)\\n```";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(selectStr);
        // 查找并输出 SQL 内容
        if (matcher.find()) {
            // 提取 SQL 内容并去除前后空白
            selectStr = matcher.group(1).trim();
        } else {
            logger.info("SQL语句不能为空");
            return new ArrayList<>();
        }
        // 清理SQL语句，去除注释和转义字符，并转换为小写
        String cleanedSql = selectStr.trim().replaceAll("[\";]", "").toLowerCase();
        // 输入校验
        if (!isOnlySelect(cleanedSql)) {
            logger.warn("SQL语句为空或者不是查询语句，无法执行查询");
            return new ArrayList<>();
        }

        try {
            // 记录SQL执行日志（仅用于调试，生产环境需注意敏感信息）
            logger.debug("执行SQL: {}", cleanedSql);

            // 使用NamedParameterJdbcTemplate执行查询
            return jdbcTemplate.queryForList(cleanedSql,new MapSqlParameterSource());

        } catch (Exception e) {
            // 异常处理
            logger.error("执行SQL时发生错误: {}", cleanedSql, e);
            return new ArrayList<>();
        }
    }

    /**
     * 检查SQL查询语句是否仅包含SELECT操作
     * 此方法用于验证给定的SQL查询语句是否仅包含SELECT操作，而不包含其他可能对数据库造成修改的操作
     * 它通过检查SQL语句中是否包含预定义的关键词来实现这一点
     *
     * @param sql 待验证的SQL查询语句
     * @return 如果SQL查询语句仅包含SELECT操作，则返回true；否则返回false
     */
    private boolean isOnlySelect(String sql) {
        // 输入校验：如果SQL语句为空或只包含空白字符，则返回false
        if (sql == null || sql.trim().isEmpty()) {
            return false;
        }
        // 定义非法操作的正则表达式（精确匹配单词边界）
        String[] keywords = {"\\bmaster\\b", "\\btruncate\\b", "\\binsert\\b",
                "\\bdelete\\b", "\\bupdate\\b", "\\bdeclare\\b",
                "\\balter\\b", "\\bdrop\\b"};
        // 定义一组关键词，如果SQL语句中包含这些关键词，则表明它不仅仅包含SELECT操作
        for (String keyword : keywords) {
            Pattern pattern = Pattern.compile(keyword);
            if (pattern.matcher(sql).find()) {
                System.out.println("检测到非法关键词: " + keyword.replace("\\b", ""));
                return false;
            }
        }
        return sql.startsWith("select");
    }

}