package com.houjiang.gift.repository;

import com.houjiang.gift.domain.GiftItem;
import com.houjiang.gift.domain.GiftRule;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 满赠数据访问（JDBC）。
 */
@Repository
public class GiftRepository {

    private static final String T_RULE = "hou_plugin_order_gift_rule";
    private static final String T_ITEM = "hou_plugin_order_gift_item";
    private static final String T_RECORD = "hou_plugin_order_gift_record";

    private static final RowMapper<GiftItem> ITEM_MAPPER = (rs, i) -> new GiftItem(
            rs.getLong("id"),
            rs.getLong("rule_id"),
            rs.getString("goods_id"),
            rs.getString("sku_id"),
            rs.getString("gift_title"),
            rs.getInt("gift_qty"),
            rs.getInt("stock_total"),
            rs.getInt("stock_used"),
            rs.getInt("sort"));

    private static final RowMapper<GiftRule> RULE_MAPPER = (rs, i) -> new GiftRule(
            rs.getLong("id"),
            rs.getString("rule_name"),
            rs.getString("scope_type"),
            split(rs.getString("scope_ids")),
            rs.getBigDecimal("threshold_amount"),
            rs.getString("gift_mode"),
            rs.getInt("max_gift_kinds"),
            rs.getInt("status"),
            rs.getInt("priority"),
            toLocal(rs.getTimestamp("start_time")),
            toLocal(rs.getTimestamp("end_time")),
            rs.getString("remark"),
            List.of());

    private final JdbcTemplate jdbc;

    public GiftRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<GiftRule> findAll() {
        List<GiftRule> rules = jdbc.query("SELECT * FROM " + T_RULE + " ORDER BY priority DESC, id DESC", RULE_MAPPER);
        List<GiftRule> result = new ArrayList<>(rules.size());
        for (GiftRule rule : rules) {
            result.add(new GiftRule(rule.id(), rule.ruleName(), rule.scopeType(), rule.scopeIds(),
                    rule.thresholdAmount(), rule.giftMode(), rule.maxGiftKinds(), rule.status(), rule.priority(),
                    rule.startTime(), rule.endTime(), rule.remark(), itemsOf(rule.id())));
        }
        return result;
    }

    public List<GiftItem> itemsOf(long ruleId) {
        return jdbc.query("SELECT * FROM " + T_ITEM + " WHERE rule_id = ? ORDER BY sort ASC, id ASC",
                ITEM_MAPPER, ruleId);
    }

    public long save(GiftRule rule) {
        String scopeIds = rule.scopeIds() == null || rule.scopeIds().isEmpty() ? null : String.join(",", rule.scopeIds());
        if (rule.id() > 0) {
            jdbc.update("UPDATE " + T_RULE + " SET rule_name = ?, scope_type = ?, scope_ids = ?, threshold_amount = ?,"
                            + " gift_mode = ?, max_gift_kinds = ?, status = ?, priority = ?, start_time = ?,"
                            + " end_time = ?, remark = ?, update_time = NOW() WHERE id = ?",
                    rule.ruleName(), rule.scopeType(), scopeIds, rule.thresholdAmount(), rule.giftMode(),
                    rule.maxGiftKinds(), rule.status(), rule.priority(), rule.startTime(), rule.endTime(),
                    rule.remark(), rule.id());
            jdbc.update("DELETE FROM " + T_ITEM + " WHERE rule_id = ?", rule.id());
            insertItems(rule.id(), rule.items());
            return rule.id();
        }
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO " + T_RULE + " (rule_name, scope_type, scope_ids, threshold_amount, gift_mode,"
                            + " max_gift_kinds, status, priority, start_time, end_time, remark, create_time, update_time)"
                            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, rule.ruleName());
            ps.setString(2, rule.scopeType());
            ps.setString(3, scopeIds);
            ps.setBigDecimal(4, rule.thresholdAmount() == null ? BigDecimal.ZERO : rule.thresholdAmount());
            ps.setString(5, rule.giftMode());
            ps.setInt(6, rule.maxGiftKinds());
            ps.setInt(7, rule.status());
            ps.setInt(8, rule.priority());
            ps.setTimestamp(9, toTimestamp(rule.startTime()));
            ps.setTimestamp(10, toTimestamp(rule.endTime()));
            ps.setString(11, rule.remark());
            return ps;
        }, keyHolder);
        long id = keyHolder.getKey() == null ? 0L : keyHolder.getKey().longValue();
        insertItems(id, rule.items());
        return id;
    }

    private void insertItems(long ruleId, List<GiftItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        List<Object[]> args = new ArrayList<>();
        int sort = 0;
        for (GiftItem item : items) {
            args.add(new Object[]{ruleId, item.goodsId(), item.skuId(), item.giftTitle(),
                    item.giftQty(), item.stockTotal(), item.stockUsed(), ++sort});
        }
        jdbc.batchUpdate("INSERT INTO " + T_ITEM
                + " (rule_id, goods_id, sku_id, gift_title, gift_qty, stock_total, stock_used, sort, create_time)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())", args);
    }

    public void delete(long id) {
        jdbc.update("DELETE FROM " + T_ITEM + " WHERE rule_id = ?", id);
        jdbc.update("DELETE FROM " + T_RULE + " WHERE id = ?", id);
    }

    /** 赠出登记：扣库存 + 写记录（同一事务由调用方保证）。 */
    /**
     * 把赠品写进订单主表的 {@code giveaway_info}（按 payment_number 归组的多张子单一起写）。
     * 表或字段不存在时静默返回 0 —— 赠品登记不因宿主表结构差异而失败。
     */
    public int updateOrderGiveawayInfo(String paymentNumber, String giveawayText) {
        try {
            return jdbc.update("UPDATE hou_order_base SET giveaway_info = ? WHERE payment_number = ?",
                    giveawayText, paymentNumber);
        } catch (Exception e) {
            return 0;
        }
    }

    /** 同一订单同一赠品是否已登记（幂等判据，唯一键 order_no + item_id）。 */
    public Map<String, Object> recordOf(String orderNo, long itemId) {
        List<Map<String, Object>> rows = jdbc.query(
                "SELECT * FROM " + T_RECORD + " WHERE order_no = ? AND item_id = ?",
                (rs, i) -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("orderNo", rs.getString("order_no"));
                    row.put("itemId", rs.getLong("item_id"));
                    row.put("giftTitle", rs.getString("gift_title"));
                    row.put("giftQty", rs.getInt("gift_qty"));
                    return row;
                }, orderNo, itemId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public void recordGift(String orderNo, String userId, long ruleId, GiftItem item, int qty) {
        jdbc.update("INSERT INTO " + T_RECORD
                        + " (order_no, user_id, rule_id, item_id, goods_id, gift_title, gift_qty, status, create_time)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, 1, NOW())",
                orderNo, userId, ruleId, item.id(), item.goodsId(), item.giftTitle(), qty);
        if (item.stockTotal() > 0) {
            jdbc.update("UPDATE " + T_ITEM + " SET stock_used = stock_used + ? WHERE id = ?", qty, item.id());
        }
    }

    public List<Map<String, Object>> pageRecords(String orderNo, String userId, int offset, int limit) {
        StringBuilder sql = new StringBuilder("SELECT * FROM " + T_RECORD + " WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        if (orderNo != null && !orderNo.isBlank()) {
            sql.append(" AND order_no = ?");
            args.add(orderNo.trim());
        }
        if (userId != null && !userId.isBlank()) {
            sql.append(" AND user_id = ?");
            args.add(userId.trim());
        }
        sql.append(" ORDER BY id DESC LIMIT ? OFFSET ?");
        args.add(Math.max(1, limit));
        args.add(Math.max(0, offset));
        return jdbc.query(sql.toString(), (rs, i) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", rs.getLong("id"));
            row.put("orderNo", rs.getString("order_no"));
            row.put("userId", rs.getString("user_id"));
            row.put("ruleId", rs.getLong("rule_id"));
            row.put("goodsId", rs.getString("goods_id"));
            row.put("giftTitle", rs.getString("gift_title"));
            row.put("giftQty", rs.getInt("gift_qty"));
            row.put("status", rs.getInt("status"));
            row.put("createTime", rs.getTimestamp("create_time") == null
                    ? null : rs.getTimestamp("create_time").toLocalDateTime().toString());
            return row;
        }, args.toArray());
    }

    public long countRecords(String orderNo, String userId) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM " + T_RECORD + " WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        if (orderNo != null && !orderNo.isBlank()) {
            sql.append(" AND order_no = ?");
            args.add(orderNo.trim());
        }
        if (userId != null && !userId.isBlank()) {
            sql.append(" AND user_id = ?");
            args.add(userId.trim());
        }
        Long total = jdbc.queryForObject(sql.toString(), Long.class, args.toArray());
        return total == null ? 0L : total;
    }

    public Map<String, Object> stats() {
        Map<String, Object> result = new LinkedHashMap<>();
        Long rules = jdbc.queryForObject("SELECT COUNT(*) FROM " + T_RULE, Long.class);
        Long enabled = jdbc.queryForObject("SELECT COUNT(*) FROM " + T_RULE + " WHERE status = 1", Long.class);
        Long items = jdbc.queryForObject("SELECT COUNT(*) FROM " + T_ITEM, Long.class);
        Long records = jdbc.queryForObject("SELECT COUNT(*) FROM " + T_RECORD + " WHERE status = 1", Long.class);
        result.put("ruleCount", rules == null ? 0L : rules);
        result.put("enabledCount", enabled == null ? 0L : enabled);
        result.put("itemCount", items == null ? 0L : items);
        result.put("recordCount", records == null ? 0L : records);
        return result;
    }

    private static List<String> split(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return Arrays.stream(text.split(",")).map(String::trim).filter(item -> !item.isEmpty()).toList();
    }

    private static LocalDateTime toLocal(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }

    private static Timestamp toTimestamp(LocalDateTime time) {
        return time == null ? null : Timestamp.valueOf(time);
    }
}
