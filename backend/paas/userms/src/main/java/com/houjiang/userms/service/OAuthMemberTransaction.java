package com.houjiang.userms.service;

import java.security.SecureRandom;
import com.houjiang.userms.dto.OAuthIdentity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** 单独的 Spring 事务代理：异常必须传播给外层，提交/回滚完成后才能重查赢家。 */
@Service
public class OAuthMemberTransaction {
    private static final SecureRandom USER_IDS = new SecureRandom();
    private final JdbcTemplate jdbc;
    private final String prefix;
    private final String legacyWechatAppId;

    public OAuthMemberTransaction(JdbcTemplate jdbc,
            @Value("${database.prefix.userms:}") String prefix,
            @Value("${userms.oauth.legacy-wechat-app-id:}") String legacyWechatAppId) {
        if (!prefix.matches("([a-zA-Z0-9_]+\\.)?")) throw new IllegalArgumentException("无效 userms 数据库前缀");
        this.jdbc = jdbc;
        this.prefix = prefix;
        this.legacyWechatAppId = legacyWechatAppId;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public Map<String, Object> getOrCreate(OAuthIdentity identity) {
        Map<String, Object> existing = find(identity);
        if (existing != null) return existing;

        String legacyId = null;
        String userId = null;
        if ("wechat".equals(identity.channel())) {
            List<Map<String, Object>> legacy = jdbc.queryForList("SELECT bind_id, bind_type, user_id, bind_openid, bind_unionid FROM "
                    + prefix + "hou_user_bind_connect WHERE bind_id = ?", "weixin_" + identity.openid());
            if (!legacy.isEmpty()) {
                // 旧键没有 AppID，必须由运维确认归属；错误类型、歧义数据不能当作已验证身份。
                if (!identity.providerAppId().equals(legacyWechatAppId) || legacy.size() != 1) {
                    throw new IllegalStateException("历史微信绑定归属未确认或存在歧义");
                }
                Map<String, Object> binding = legacy.getFirst();
                if (((Number) binding.get("bind_type")).intValue() != 2
                        || !Objects.equals(binding.get("bind_openid"), identity.openid())) {
                    throw new IllegalStateException("历史微信绑定类型或 openid 不一致");
                }
                verifyUnion(binding.get("bind_unionid"), identity.unionid());
                legacyId = binding.get("bind_id").toString();
                userId = binding.get("user_id").toString();
                requireActiveUser(userId);
            }
        }
        boolean created = false;
        if (userId == null) {
            if (!identity.allowCreate()) throw new IllegalStateException("OAuth 身份尚未绑定会员");
            // 绑定表 user_id 只有 18 位；数据库主键兜底拒绝极低概率的随机碰撞。
            userId = Long.toString(USER_IDS.nextLong(100_000_000_000_000_000L, 1_000_000_000_000_000_000L));
            // 显式 NULL，不能让 user_mobile 的历史默认值 '1' 生效；不创建后台权限记录。
            jdbc.update("INSERT INTO " + prefix + "hou_user_info (user_id,user_mobile,password,international_dialing_code,"
                            + "user_name,user_state,user_integral,user_count_login,user_is_new,user_nickname,create_time,"
                            + "is_delete,is_business,is_admin,follower_num,follow_num,member_create_time,register_terminal) "
                            + "VALUES (?,NULL,'','+86',?,1,0,0,1,?,CURRENT_TIMESTAMP,0,0,0,0,0,CURRENT_TIMESTAMP,?)",
                    userId, "oauth_" + userId, identity.nickname(), identity.registerTerminal());
            jdbc.update("INSERT INTO " + prefix + "hou_user_info_detail "
                            + "(user_id,user_avatar,user_gender,user_reg_time,user_mobile_verify,user_parent_id,user_platform,validate_long_term) "
                            + "VALUES (?,?,0,?,0,'0',1,0)",
                    userId, identity.avatar(), System.currentTimeMillis() / 1000);
            created = true;
        }
        jdbc.update("INSERT INTO " + prefix + "hou_user_oauth_identity "
                        + "(channel,provider_app_id,openid,unionid,user_id,legacy_bind_id) VALUES (?,?,?,?,?,?)",
                identity.channel(), identity.providerAppId(), identity.openid(), identity.unionid(), userId, legacyId);
        if (legacyId == null) {
            jdbc.update("INSERT INTO " + prefix + "hou_user_bind_connect "
                            + "(bind_id,bind_type,user_id,bind_nickname,bind_avator,bind_openid,bind_unionid,is_auto_login) "
                            + "VALUES (?,?,?,?,?,?,?,1)",
                    bindId(identity), identity.bindType(), userId, identity.nickname(), identity.avatar(),
                    identity.openid(), identity.unionid());
        }
        return Map.of("userId", userId, "created", created);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Map<String, Object> find(OAuthIdentity identity) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT user_id,unionid,legacy_bind_id FROM " + prefix
                        + "hou_user_oauth_identity WHERE channel=? AND provider_app_id=? AND openid=?",
                identity.channel(), identity.providerAppId(), identity.openid());
        if (rows.isEmpty()) return null;
        Map<String, Object> row = rows.getFirst();
        verifyUnion(row.get("unionid"), identity.unionid());
        String userId = row.get("user_id").toString();
        String bindingId = row.get("legacy_bind_id") == null ? bindId(identity) : row.get("legacy_bind_id").toString();
        List<Map<String, Object>> bindings = jdbc.queryForList("SELECT user_id,bind_openid,bind_unionid FROM "
                        + prefix + "hou_user_bind_connect WHERE bind_id=? AND bind_type=?",
                bindingId, identity.bindType());
        if (bindings.size() != 1 || !userId.equals(bindings.getFirst().get("user_id").toString())
                || !identity.openid().equals(bindings.getFirst().get("bind_openid"))) {
            throw new IllegalStateException("OAuth 身份与真实绑定不一致");
        }
        verifyUnion(bindings.getFirst().get("bind_unionid"), identity.unionid());
        requireActiveUser(userId);
        return Map.of("userId", userId, "created", false);
    }

    private void requireActiveUser(String userId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM " + prefix
                + "hou_user_info WHERE user_id=? AND is_delete=0 AND user_state IN (1,2)", Integer.class, userId);
        if (count == null || count != 1) throw new IllegalStateException("绑定会员不存在或已停用");
    }

    private static void verifyUnion(Object stored, String supplied) {
        // 不依赖 unionid 查人，不跨 AppID 合并，不覆盖已有 unionid。
        if (stored != null && !stored.toString().isEmpty() && supplied != null && !stored.equals(supplied)) {
            throw new IllegalStateException("OAuth unionid 与已有绑定冲突");
        }
    }

    static String bindId(OAuthIdentity identity) {
        try {
            String key = identity.channel() + "\n" + identity.providerAppId() + "\n" + identity.openid();
            return "oauth_" + HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(key.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
