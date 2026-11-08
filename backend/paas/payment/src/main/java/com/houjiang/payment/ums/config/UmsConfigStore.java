package com.houjiang.payment.ums.config;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

/**
 * 银联企业订单渠道配置读写（hou_ums_corp_config）。
 */
public class UmsConfigStore {

    public record Config(
            long id,
            long orgId,
            String env,
            String groupId,
            String merNo,
            String channelId,
            String verNo,
            String baseUrl,
            String mchPrivateKey,
            String umsPublicKey,
            String notifyUrl,
            boolean enabled
    ) {}

    private final JdbcTemplate jdbc;

    public UmsConfigStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Config get(long orgId) {
        List<Config> list = jdbc.query(
                "SELECT id,org_id,env,group_id,mer_no,channel_id,ver_no,base_url,mch_private_key,ums_public_key,notify_url,enabled "
                        + "FROM hou_ums_corp_config WHERE org_id=? LIMIT 1",
                (rs, i) -> new Config(
                        rs.getLong("id"),
                        rs.getLong("org_id"),
                        rs.getString("env"),
                        rs.getString("group_id"),
                        rs.getString("mer_no"),
                        rs.getString("channel_id"),
                        rs.getString("ver_no"),
                        rs.getString("base_url"),
                        rs.getString("mch_private_key"),
                        rs.getString("ums_public_key"),
                        rs.getString("notify_url"),
                        rs.getInt("enabled") == 1
                ),
                orgId);
        return list.isEmpty() ? null : list.get(0);
    }

    public Config require(long orgId) {
        Config cfg = get(orgId);
        if (cfg == null) {
            throw new IllegalStateException("尚未保存银联渠道配置");
        }
        return cfg;
    }

    public void save(long orgId, Map<String, Object> body) {
        Config existing = get(orgId);
        String env = str(body, "env", existing == null ? "test" : existing.env());
        String groupId = str(body, "groupId", existing == null ? "" : existing.groupId());
        String merNo = str(body, "merNo", existing == null ? "" : existing.merNo());
        String channelId = str(body, "channelId", existing == null ? "043" : existing.channelId());
        String verNo = str(body, "verNo", existing == null ? "100" : existing.verNo());
        String baseUrl = str(body, "baseUrl", existing == null
                ? "https://mobl-test.chinaums.com/channel/Business/UnifyMulti" : existing.baseUrl());
        String mchKey = str(body, "mchPrivateKey", existing == null ? "" : nullToEmpty(existing.mchPrivateKey()));
        String umsKey = str(body, "umsPublicKey", existing == null ? "" : nullToEmpty(existing.umsPublicKey()));
        String notifyUrl = str(body, "notifyUrl", existing == null ? "" : existing.notifyUrl());
        boolean enabled = body.get("enabled") == null
                ? (existing == null || existing.enabled())
                : Boolean.parseBoolean(String.valueOf(body.get("enabled")))
                || "1".equals(String.valueOf(body.get("enabled")));

        if (existing == null) {
            jdbc.update(
                    "INSERT INTO hou_ums_corp_config(org_id,env,group_id,mer_no,channel_id,ver_no,base_url,mch_private_key,ums_public_key,notify_url,enabled) "
                            + "VALUES(?,?,?,?,?,?,?,?,?,?,?)",
                    orgId, env, groupId, merNo, channelId, verNo, baseUrl, mchKey, umsKey, notifyUrl, enabled ? 1 : 0);
        } else {
            jdbc.update(
                    "UPDATE hou_ums_corp_config SET env=?,group_id=?,mer_no=?,channel_id=?,ver_no=?,base_url=?,mch_private_key=?,ums_public_key=?,notify_url=?,enabled=? WHERE org_id=?",
                    env, groupId, merNo, channelId, verNo, baseUrl, mchKey, umsKey, notifyUrl, enabled ? 1 : 0, orgId);
        }
    }

    public Map<String, Object> toSafeMap(Config cfg) {
        if (cfg == null) {
            return Map.of(
                    "env", "test",
                    "groupId", "",
                    "merNo", "",
                    "channelId", "043",
                    "verNo", "100",
                    "baseUrl", "https://mobl-test.chinaums.com/channel/Business/UnifyMulti",
                    "notifyUrl", "",
                    "enabled", false,
                    "hasMchPrivateKey", false,
                    "hasUmsPublicKey", false
            );
        }
        return Map.of(
                "env", nullToEmpty(cfg.env()),
                "groupId", nullToEmpty(cfg.groupId()),
                "merNo", nullToEmpty(cfg.merNo()),
                "channelId", nullToEmpty(cfg.channelId()),
                "verNo", nullToEmpty(cfg.verNo()),
                "baseUrl", nullToEmpty(cfg.baseUrl()),
                "notifyUrl", nullToEmpty(cfg.notifyUrl()),
                "enabled", cfg.enabled(),
                "hasMchPrivateKey", cfg.mchPrivateKey() != null && !cfg.mchPrivateKey().isBlank(),
                "hasUmsPublicKey", cfg.umsPublicKey() != null && !cfg.umsPublicKey().isBlank()
        );
    }

    private static String str(Map<String, Object> m, String key, String def) {
        Object v = m.get(key);
        if (v == null) return def == null ? "" : def;
        String s = String.valueOf(v).trim();
        return s.isEmpty() && def != null ? def : s;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
