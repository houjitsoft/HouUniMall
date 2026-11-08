package com.houjiang.mall.service.store;

import com.houjiang.commons.util.PasswordUtils;
import com.houjiang.commons.util.SnowflakeIdWorker;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 门店主数据服务。
 *
 * <p>商家是经营主体，门店是商家下属的经营与履约节点；配送和 POS 开关均属于门店。</p>
 */
@Service
@RequiredArgsConstructor
public class MallStoreService {

    private static final List<String> TOGGLE_FIELDS = List.of(
            "business_status", "rider_delivery_enabled", "pos_enabled");
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^1\\d{10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+$");

    private final JdbcTemplate jdbcTemplate;

    public Map<String, Object> page(Map<String, Object> params) {
        int page = positiveInt(params.get("page"), 1);
        int size = Math.min(positiveInt(params.get("size"), 15), 100);
        List<Object> arguments = new ArrayList<>();
        StringBuilder where = new StringBuilder(" WHERE s.deleted = 0 AND COALESCE(m.delete_time, 0) = 0");

        appendLike(where, arguments, "s.store_name", params.get("storeName"));
        appendLike(where, arguments, "m.name", params.get("merchantName"));
        appendEqual(where, arguments, "s.merchant_id", params.get("merchantId"));
        appendEqual(where, arguments, "s.business_status", params.get("businessStatus"));
        appendEqual(where, arguments, "s.rider_delivery_enabled", params.get("riderDeliveryEnabled"));
        appendEqual(where, arguments, "s.pos_enabled", params.get("posEnabled"));

        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM hou_mall_store s "
                        + "JOIN hou_merchant_mall_shop m ON m.id = s.merchant_id" + where,
                Long.class,
                arguments.toArray());

        List<Object> pageArguments = new ArrayList<>(arguments);
        pageArguments.add(size);
        pageArguments.add((page - 1) * size);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT s.id, s.merchant_id AS merchantId, m.name AS merchantName, "
                        + "s.store_no AS storeNo, s.store_name AS storeName, "
                        + "s.contact_person AS contactPerson, s.contact_phone AS contactPhone, "
                        + "s.province, s.city, s.district, s.address, "
                        + "s.business_status AS businessStatus, "
                        + "s.rider_delivery_enabled AS riderDeliveryEnabled, "
                        + "s.pos_enabled AS posEnabled, s.create_time AS createTime "
                        + "FROM hou_mall_store s "
                        + "JOIN hou_merchant_mall_shop m ON m.id = s.merchant_id"
                        + where + " ORDER BY s.id DESC LIMIT ? OFFSET ?",
                pageArguments.toArray());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", rows);
        result.put("total", total == null ? 0L : total);
        result.put("current", page);
        result.put("size", size);
        return result;
    }

    public List<Map<String, Object>> merchantOptions() {
        return jdbcTemplate.queryForList(
                "SELECT id, name FROM hou_merchant_mall_shop "
                        + "WHERE COALESCE(delete_time, 0) = 0 AND shop_status = 1 ORDER BY id");
    }

    public List<Map<String, Object>> accountOptions(Map<String, Object> params) {
        String keyword = text(params.get("keyword"));
        List<Object> arguments = new ArrayList<>();
        String keywordSql = "";
        if (StringUtils.hasText(keyword)) {
            keywordSql = " AND (u.user_name LIKE ? OR u.user_mobile LIKE ? OR u.user_nickname LIKE ?)";
            String like = "%" + keyword + "%";
            arguments.add(like);
            arguments.add(like);
            arguments.add(like);
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT u.user_id AS userId, u.user_name AS userName, u.user_mobile AS userMobile, "
                        + "u.user_nickname AS userNickname, a.merchant_id AS grantedMerchantId, "
                        + "m.name AS grantedMerchantName "
                        + "FROM hou_user_info u "
                        + "LEFT JOIN hou_mall_merchant_account a ON a.user_id = u.user_id "
                        + "LEFT JOIN hou_merchant_mall_shop m ON m.id = a.merchant_id "
                        + "WHERE COALESCE(u.is_delete, 0) = 0 AND u.user_state IN (1, 2) "
                        + "AND u.password IS NOT NULL AND u.password <> ''"
                        + keywordSql + " ORDER BY u.user_id DESC LIMIT 30",
                arguments.toArray());

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String loginAccount = preferredLoginAccount(row);
            if (!StringUtils.hasText(loginAccount)) continue;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("userId", String.valueOf(row.get("userId")));
            item.put("loginAccount", loginAccount);
            item.put("displayName", firstText(row.get("userNickname"), row.get("userName"), loginAccount));
            item.put("accountType", EMAIL_PATTERN.matcher(loginAccount).matches() ? "email" : "mobile");
            item.put("grantedMerchantId", row.get("grantedMerchantId"));
            item.put("grantedMerchantName", row.get("grantedMerchantName"));
            result.add(item);
        }
        return result;
    }

    @Transactional
    public Map<String, Object> create(Map<String, Object> params) {
        long merchantId = requiredLong(params.get("merchantId"), "请选择所属商家");
        String storeName = requiredText(params.get("storeName"), "请输入门店名称");
        Long merchantCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM hou_merchant_mall_shop "
                        + "WHERE id = ? AND COALESCE(delete_time, 0) = 0",
                Long.class,
                merchantId);
        if (merchantCount == null || merchantCount == 0) {
            throw new IllegalArgumentException("所属商家不存在");
        }

        AccountGrant accountGrant = resolveAndGrantAccount(params, merchantId);

        String storeNo = text(params.get("storeNo"));
        if (!StringUtils.hasText(storeNo)) {
            storeNo = "STORE-" + UUID.randomUUID().toString().replace("-", "")
                    .substring(0, 12).toUpperCase();
        }
        jdbcTemplate.update(
                "INSERT INTO hou_mall_store "
                        + "(merchant_id, store_no, store_name, contact_person, contact_phone, province, city, district, address, "
                        + "business_status, rider_delivery_enabled, pos_enabled) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                merchantId,
                storeNo,
                storeName,
                nullableText(params.get("contactPerson")),
                nullableText(params.get("contactPhone")),
                nullableText(params.get("province")),
                nullableText(params.get("city")),
                nullableText(params.get("district")),
                nullableText(params.get("address")),
                booleanInt(params.get("businessStatus"), 1),
                booleanInt(params.get("riderDeliveryEnabled"), 0),
                booleanInt(params.get("posEnabled"), 0));
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("storeId", id == null ? 0L : id);
        result.put("userId", accountGrant.userId());
        result.put("loginAccount", accountGrant.loginAccount());
        result.put("accountCreated", accountGrant.accountCreated());
        return result;
    }

    private AccountGrant resolveAndGrantAccount(Map<String, Object> params, long merchantId) {
        String mode = requiredText(params.get("accountMode"), "请选择账号来源");
        if ("existing".equals(mode)) {
            long userId = requiredLong(params.get("platformUserId"), "请选择已有账号");
            Map<String, Object> account = findActiveAccount(userId);
            if (account == null) throw new IllegalArgumentException("所选账号不存在或已停用");
            String loginAccount = preferredLoginAccount(account);
            if (!StringUtils.hasText(loginAccount)) {
                throw new IllegalArgumentException("所选账号没有可用的手机号或邮箱");
            }
        grantMerchantPermission(userId,
                firstText(account.get("userNickname"), account.get("userName"), loginAccount),
                merchantId);
            return new AccountGrant(String.valueOf(userId), loginAccount, false);
        }
        if (!"new".equals(mode)) throw new IllegalArgumentException("账号来源不正确");

        String identifier = requiredText(params.get("accountIdentifier"), "请输入手机号或邮箱");
        if (EMAIL_PATTERN.matcher(identifier).matches()) identifier = identifier.toLowerCase();
        if (!MOBILE_PATTERN.matcher(identifier).matches() && !EMAIL_PATTERN.matcher(identifier).matches()) {
            throw new IllegalArgumentException("请输入正确的手机号或邮箱");
        }
        String password = requiredText(params.get("accountPassword"), "请设置初始密码");
        String passwordAgain = requiredText(params.get("accountPasswordAgain"), "请再次输入初始密码");
        if (password.length() < 6 || password.length() > 32) {
            throw new IllegalArgumentException("初始密码必须为6至32个字符");
        }
        if (!password.equals(passwordAgain)) throw new IllegalArgumentException("两次输入的密码不一致");
        Long duplicate = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM hou_user_info WHERE COALESCE(is_delete, 0) = 0 "
                        + "AND (user_mobile = ? OR user_name = ?)",
                Long.class, identifier, identifier);
        if (duplicate != null && duplicate > 0) {
            throw new IllegalArgumentException("该手机号或邮箱已注册，请选择已有账号");
        }

        String userId = SnowflakeIdWorker.getSnowId();
        String passwordHash = PasswordUtils.BCryptPasswordEncoder(password);
        jdbcTemplate.update(
                "INSERT INTO hou_user_info "
                        + "(user_id, user_mobile, password, international_dialing_code, user_name, user_state, "
                        + "user_integral, user_count_login, user_is_new, user_nickname, create_time, "
                        + "is_business, shop_id, follower_num, follow_num, register_terminal, is_delete, is_admin) "
                        + "VALUES (?, ?, ?, '+86', ?, 1, 0, 0, 1, ?, NOW(), 1, ?, 0, 0, 'houcloud', 0, 0)",
                userId, MOBILE_PATTERN.matcher(identifier).matches() ? identifier : null,
                passwordHash, identifier, identifier, merchantId);
        jdbcTemplate.update(
                "INSERT INTO hou_user_info_detail "
                        + "(user_id, user_reg_time, user_reg_ip, user_mobile_verify, user_parent_id, user_platform) "
                        + "VALUES (?, UNIX_TIMESTAMP(), '127.0.0.1', 1, '0', 5)",
                userId);
        grantMerchantPermission(Long.parseLong(userId), identifier, merchantId);
        return new AccountGrant(userId, identifier, true);
    }

    private Map<String, Object> findActiveAccount(long userId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT user_id AS userId, user_name AS userName, user_mobile AS userMobile, "
                        + "user_nickname AS userNickname, password FROM hou_user_info "
                        + "WHERE user_id = ? AND COALESCE(is_delete, 0) = 0 "
                        + "AND user_state IN (1, 2) AND password IS NOT NULL AND password <> '' LIMIT 1",
                userId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private void grantMerchantPermission(long userId, String displayName, long merchantId) {
        List<Map<String, Object>> grants = jdbcTemplate.queryForList(
                "SELECT merchant_id AS merchantId, admin_id AS adminId "
                        + "FROM hou_mall_merchant_account WHERE user_id = ? LIMIT 1",
                userId);
        if (!grants.isEmpty()) {
            long grantedMerchantId = ((Number) grants.get(0).get("merchantId")).longValue();
            if (grantedMerchantId != merchantId) {
                throw new IllegalArgumentException("该账号已属于其他商家，不能重复授权");
            }
            jdbcTemplate.update("UPDATE hou_user_info SET is_business = 1, shop_id = ? WHERE user_id = ?",
                    merchantId, userId);
            return;
        }
        Long existingAdmin = jdbcTemplate.query(
                "SELECT id FROM hou_merchant_mall_shop_admin WHERE delete_time = 0 "
                        + "AND shop_id = ? AND user_id = ? LIMIT 1",
                ps -> {
                    ps.setLong(1, merchantId);
                    ps.setLong(2, userId);
                },
                rs -> rs.next() ? rs.getLong("id") : null);
        if (existingAdmin != null) return;
        if (false) {
            throw new IllegalArgumentException("该登录账号已被商家中心使用");
        }
        long now = System.currentTimeMillis() / 1000;
        jdbcTemplate.update(
                "INSERT INTO hou_merchant_mall_shop_admin "
                        + "(create_time, update_time, delete_time, shop_id, super, name, face, status, "
                        + "login_all, user_id, login_last_time, login_last_ip) "
                + "VALUES (?, ?, 0, ?, 1, ?, '/api/static/backend_avatar.png', 1, 0, ?, 0, '')",
                now, now, merchantId, displayName, userId);
        Long adminId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        jdbcTemplate.update(
                "INSERT INTO hou_mall_merchant_account (merchant_id, user_id, admin_id) VALUES (?, ?, ?)",
                merchantId, userId, adminId);
    }

    private String preferredLoginAccount(Map<String, Object> account) {
        String mobile = text(account.get("userMobile"));
        String username = text(account.get("userName"));
        if (MOBILE_PATTERN.matcher(mobile).matches() || EMAIL_PATTERN.matcher(mobile).matches()) return mobile;
        if (MOBILE_PATTERN.matcher(username).matches() || EMAIL_PATTERN.matcher(username).matches()) return username;
        return "";
    }

    private String firstText(Object... values) {
        for (Object value : values) {
            String current = text(value);
            if (StringUtils.hasText(current)) return current;
        }
        return "";
    }

    private record AccountGrant(String userId, String loginAccount, boolean accountCreated) {}

    public boolean updateToggle(Map<String, Object> params) {
        long id = requiredLong(params.get("id"), "门店ID不能为空");
        String field = text(params.get("field"));
        if (!TOGGLE_FIELDS.contains(field)) {
            throw new IllegalArgumentException("不支持的门店状态字段");
        }
        int enabled = booleanInt(params.get("enabled"), 0);
        return jdbcTemplate.update(
                "UPDATE hou_mall_store SET " + field + " = ? WHERE id = ? AND deleted = 0",
                enabled,
                id) > 0;
    }

    private void appendLike(StringBuilder where, List<Object> arguments, String column, Object value) {
        String text = text(value);
        if (StringUtils.hasText(text)) {
            where.append(" AND ").append(column).append(" LIKE ?");
            arguments.add("%" + text + "%");
        }
    }

    private void appendEqual(StringBuilder where, List<Object> arguments, String column, Object value) {
        if (value != null && StringUtils.hasText(String.valueOf(value))) {
            where.append(" AND ").append(column).append(" = ?");
            arguments.add(Integer.parseInt(String.valueOf(value)));
        }
    }

    private int positiveInt(Object value, int fallback) {
        try {
            int parsed = Integer.parseInt(String.valueOf(value));
            return parsed > 0 ? parsed : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private int booleanInt(Object value, int fallback) {
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            return fallback;
        }
        String normalized = String.valueOf(value).trim();
        return "1".equals(normalized) || "true".equalsIgnoreCase(normalized) ? 1 : 0;
    }

    private long requiredLong(Object value, String message) {
        try {
            long parsed = Long.parseLong(String.valueOf(value));
            if (parsed > 0) return parsed;
        } catch (Exception ignored) {
            // 统一转为业务校验错误。
        }
        throw new IllegalArgumentException(message);
    }

    private String requiredText(Object value, String message) {
        String result = text(value);
        if (!StringUtils.hasText(result)) throw new IllegalArgumentException(message);
        return result;
    }

    private String nullableText(Object value) {
        String result = text(value);
        return StringUtils.hasText(result) ? result : null;
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
