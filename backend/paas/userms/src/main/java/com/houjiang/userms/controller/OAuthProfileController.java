package com.houjiang.userms.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.SecurityContextUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/** 仅通过当前 JWT 身份读取本人资料，不接受调用方指定用户。 */
@RestController
public class OAuthProfileController {
    private final JdbcTemplate jdbc;
    private final String prefix;

    public OAuthProfileController(JdbcTemplate jdbc, @Value("${database.prefix.userms:}") String prefix) {
        if (!prefix.matches("([a-zA-Z0-9_]+\\.)?")) throw new IllegalArgumentException("无效 userms 数据库前缀");
        this.jdbc = jdbc;
        this.prefix = prefix;
    }

    @GetMapping("/user/ms/oauth/profile")
    public ResponseResult<Map<String, Object>> profile() {
        String userId = SecurityContextUtils.getCurrentUserId();
        if (userId == null || userId.isBlank()) return ResponseResult.fail(401, "请先登录");
        var rows = jdbc.query("SELECT user_id,user_nickname,user_mobile,register_terminal,create_time FROM "
                        + prefix + "hou_user_info WHERE user_id=? AND is_delete=0 AND user_state IN (1,2)",
                (rs, index) -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("userId", rs.getString("user_id"));
                    row.put("userNickname", rs.getString("user_nickname"));
                    row.put("userMobile", rs.getString("user_mobile"));
                    row.put("registerTerminal", rs.getString("register_terminal"));
                    var time = rs.getTimestamp("create_time");
                    row.put("registerTime", time == null ? null : time.toLocalDateTime().toString());
                    return row;
                }, userId);
        if (rows.size() != 1) return ResponseResult.fail(404, "当前用户不存在或已停用");
        return ResponseResult.success(rows.getFirst());
    }
}
