package com.houjiang.order.service;

import com.houjiang.commons.dto.goods.ManjianzengzheGoodsRuleQueryDTO;
import com.houjiang.commons.vo.activity.ManjianzengzheGoodsQueryResultVO;
import com.houjiang.commons.vo.activity.ManjianzengzhePromotionContentQueryResultVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 满减查询（宿主侧只读桥）：兼容旧 {@code activity/reductionPolicyBase/*} Feign，
 * 不依赖已退役的 {@code hou_goods_base/hou_goods_common}，直接读满减表 + mall 商品。
 *
 * <p>与插件 jar 内 mapper 语义保持一致；插件管理端走 PF4J 路由，下单/商品详情走本服务。
 */
@Service
public class ReductionPolicyQueryService {

    private final JdbcTemplate jdbc;

    public ReductionPolicyQueryService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public ManjianzengzheGoodsQueryResultVO selectByGoodsId(Integer commonId) {
        if (commonId == null) {
            return null;
        }
        String sql = """
                SELECT a.promotion_id, a.start_time, a.end_time,
                       COALESCE(b.goods_id, ?) AS goods_id
                FROM hou_addon_activity_reduction_policy_base a
                LEFT JOIN hou_addon_activity_reduction_policy_goods b
                  ON a.promotion_id = b.promotion_id AND b.common_id = ?
                WHERE a.promotion_status = 1
                  AND NOW() BETWEEN a.start_time AND a.end_time
                  AND (
                    b.common_id IS NOT NULL
                    OR NOT EXISTS (
                      SELECT 1 FROM hou_addon_activity_reduction_policy_goods gx
                      WHERE gx.promotion_id = a.promotion_id
                    )
                  )
                ORDER BY CASE WHEN b.common_id IS NOT NULL THEN 0 ELSE 1 END, a.promotion_id DESC
                LIMIT 1
                """;
        List<Map<String, Object>> rows = jdbc.queryForList(sql, commonId, commonId);
        if (rows.isEmpty()) {
            return null;
        }
        Map<String, Object> row = rows.get(0);
        ManjianzengzheGoodsQueryResultVO vo = new ManjianzengzheGoodsQueryResultVO();
        vo.setPromotionId(asInt(row.get("promotion_id")));
        vo.setStartTime(asDate(row.get("start_time")));
        vo.setEndTime(asDate(row.get("end_time")));
        vo.setGoodsId(asInt(row.get("goods_id")));
        vo.setCommonIsXian(5);
        return vo;
    }

    public List<ManjianzengzheGoodsRuleQueryDTO> selectList(Integer promotionId) {
        if (promotionId == null) {
            return List.of();
        }
        String sql = """
                SELECT a.promotion_id, a.has_giveaway, a.giveaway_id, a.meet_amount,
                       a.discount_content, a.discount_type, mg.name AS giveaway_name, mg.image AS giveaway_image
                FROM hou_addon_activity_reduction_policy_rule a
                LEFT JOIN hou_mall_goods mg
                  ON a.giveaway_id = mg.id AND a.giveaway_id IS NOT NULL AND a.giveaway_id <> 0
                WHERE a.promotion_id = ?
                ORDER BY a.meet_amount ASC
                """;
        List<Map<String, Object>> rows = jdbc.queryForList(sql, promotionId);
        List<ManjianzengzheGoodsRuleQueryDTO> list = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            ManjianzengzheGoodsRuleQueryDTO dto = new ManjianzengzheGoodsRuleQueryDTO();
            dto.setPromotionId(asInt(row.get("promotion_id")));
            dto.setHasGiveaway(asInt(row.get("has_giveaway")));
            dto.setGiveawayId(asInt(row.get("giveaway_id")));
            dto.setMeetAmount(asDecimal(row.get("meet_amount")));
            dto.setDiscountContent(row.get("discount_content") == null ? null : String.valueOf(row.get("discount_content")));
            dto.setDiscountType(asInt(row.get("discount_type")));
            dto.setGoodsId(asInt(row.get("giveaway_id")));
            dto.setGiveawayName(row.get("giveaway_name") == null ? null : String.valueOf(row.get("giveaway_name")));
            dto.setGiveawayImage(row.get("giveaway_image") == null ? null : String.valueOf(row.get("giveaway_image")));
            list.add(dto);
        }
        return list;
    }

    public List<ManjianzengzhePromotionContentQueryResultVO> selectContent(Integer commonId) {
        if (commonId == null) {
            return List.of();
        }
        String sql = """
                SELECT a.promotion_content, a.discount_type, a.has_giveaway,
                       a.discount_content, a.meet_amount, a.giveaway_id
                FROM hou_addon_activity_reduction_policy_rule a
                INNER JOIN hou_addon_activity_reduction_policy_base c
                  ON a.promotion_id = c.promotion_id
                WHERE c.promotion_status = 1
                  AND NOW() BETWEEN c.start_time AND c.end_time
                  AND (
                    EXISTS (
                      SELECT 1 FROM hou_addon_activity_reduction_policy_goods b
                      WHERE b.promotion_id = c.promotion_id AND b.common_id = ?
                    )
                    OR NOT EXISTS (
                      SELECT 1 FROM hou_addon_activity_reduction_policy_goods gx
                      WHERE gx.promotion_id = c.promotion_id
                    )
                  )
                """;
        List<Map<String, Object>> rows = jdbc.queryForList(sql, commonId);
        List<ManjianzengzhePromotionContentQueryResultVO> list = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            ManjianzengzhePromotionContentQueryResultVO vo = new ManjianzengzhePromotionContentQueryResultVO();
            vo.setPromotionContent(row.get("promotion_content") == null ? null : String.valueOf(row.get("promotion_content")));
            vo.setDiscountType(asInt(row.get("discount_type")));
            vo.setHasGiveaway(asInt(row.get("has_giveaway")));
            vo.setDiscountContent(row.get("discount_content") == null ? null : String.valueOf(row.get("discount_content")));
            vo.setMeetAmount(asDecimal(row.get("meet_amount")));
            vo.setGiveawayId(asInt(row.get("giveaway_id")));
            list.add(vo);
        }
        return list;
    }

    private static Integer asInt(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.valueOf(String.valueOf(value));
        } catch (Exception ignored) {
            return null;
        }
    }

    private static BigDecimal asDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Date asDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Timestamp timestamp) {
            return new Date(timestamp.getTime());
        }
        if (value instanceof Date date) {
            return date;
        }
        if (value instanceof java.time.LocalDateTime localDateTime) {
            return Date.from(localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant());
        }
        if (value instanceof java.time.Instant instant) {
            return Date.from(instant);
        }
        return null;
    }
}
