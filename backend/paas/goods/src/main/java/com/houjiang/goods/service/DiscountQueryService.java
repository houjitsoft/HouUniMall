package com.houjiang.goods.service;

import com.houjiang.commons.vo.activity.AppDiscountGoodsQueryResultVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 限时折扣只读查询（宿主侧）：兼容旧 {@code activity/discountGoods/*} Feign，
 * 不依赖已下线 activity 服务，直接读折扣表。
 */
@Service
public class DiscountQueryService {

    private final JdbcTemplate jdbc;

    public DiscountQueryService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public AppDiscountGoodsQueryResultVO selectByGoodsId(Integer goodsId) {
        if (goodsId == null) {
            return null;
        }
        String sql = """
                SELECT discount_id, discount_goods_id, discount_price, goods_start_time, goods_end_time,
                       goods_lower_limit, goods_upper_limit, discount_upper_time, pre_start_time,
                       discount_goods_state, discount_amount
                FROM hou_addon_activity_discount_goods
                WHERE discount_goods_state IN (1, 4)
                  AND (goods_id = ? OR common_id = ?)
                ORDER BY goods_end_time DESC
                LIMIT 1
                """;
        List<Map<String, Object>> rows = jdbc.queryForList(sql, goodsId, goodsId);
        return rows.isEmpty() ? null : mapRow(rows.get(0));
    }

    public AppDiscountGoodsQueryResultVO selectByGoodsIdAndTime(Integer goodsId) {
        if (goodsId == null) {
            return null;
        }
        String sql = """
                SELECT discount_id, discount_goods_id, discount_price, goods_start_time, goods_end_time,
                       goods_lower_limit, goods_upper_limit, discount_upper_time, pre_start_time,
                       discount_goods_state, discount_amount
                FROM hou_addon_activity_discount_goods
                WHERE discount_goods_state = 1
                  AND NOW() BETWEEN goods_start_time AND goods_end_time
                  AND (goods_id = ? OR common_id = ?)
                ORDER BY discount_goods_id DESC
                LIMIT 1
                """;
        List<Map<String, Object>> rows = jdbc.queryForList(sql, goodsId, goodsId);
        if (!rows.isEmpty()) {
            return mapRow(rows.get(0));
        }
        return selectByGoodsId(goodsId);
    }

    private AppDiscountGoodsQueryResultVO mapRow(Map<String, Object> row) {
        AppDiscountGoodsQueryResultVO vo = new AppDiscountGoodsQueryResultVO();
        vo.setDiscountId(asInt(row.get("discount_id")));
        vo.setDiscountGoodsId(asInt(row.get("discount_goods_id")));
        vo.setDiscountPrice(asDecimal(row.get("discount_price")));
        vo.setGoodsStartTime(asDate(row.get("goods_start_time")));
        vo.setGoodsEndTime(asDate(row.get("goods_end_time")));
        vo.setGoodsLowerLimit(asInt(row.get("goods_lower_limit")));
        vo.setGoodsUpperLimit(asInt(row.get("goods_upper_limit")));
        vo.setDiscountUpperTime(asInt(row.get("discount_upper_time")));
        vo.setPreStartTime(asDate(row.get("pre_start_time")));
        vo.setDiscountGoodsState(asInt(row.get("discount_goods_state")));
        vo.setDiscountAmount(asDecimal(row.get("discount_amount")));
        return vo;
    }

    private static Integer asInt(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.intValue();
        try { return Integer.valueOf(String.valueOf(v)); } catch (Exception e) { return null; }
    }

    private static BigDecimal asDecimal(Object v) {
        if (v == null) return null;
        if (v instanceof BigDecimal b) return b;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try { return new BigDecimal(String.valueOf(v)); } catch (Exception e) { return null; }
    }

    private static Date asDate(Object v) {
        if (v == null) return null;
        if (v instanceof Timestamp ts) return new Date(ts.getTime());
        if (v instanceof Date d) return d;
        return null;
    }
}
