package com.houjiang.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * 阶梯价算价助手（对应插件 {@code goods-tierprice}）。
 *
 * <p><b>为什么放在 order 核心</b>：阶梯价必须影响<b>实付金额</b>，而实付由 order 的
 * {@code calculatePrice} 统一计算（{@code createOrders} 也复用它，见 OrderCreateServiceImpl:1257）。
 * 插件只能算价给前端看，改不了这一处。因此这里只做一件最小的事：按
 * 「商品 + 数量」查插件的规则表，返回命中区间的单价；调用方没有任何副作用地替换单价。
 *
 * <p><b>安全边界</b>：
 * <ul>
 *   <li>只读插件表，不写任何数据；</li>
 *   <li>任何异常、缺表、脏数据一律返回 {@code null}，调用方保持原价，<b>绝不让算价失败</b>；</li>
 *   <li>只支持 {@code ALL} 与 {@code GOODS} 两种范围；{@code CATEGORY} 不做（order 侧拿不到可信类目，
 *       按命中处理会误伤整店商品）；</li>
 *   <li>算出的单价必须<b>严格低于</b>原价才生效，否则返回 null。</li>
 * </ul>
 *
 * <p>开关：{@code houcloud.pricing.tier-enabled}（默认 true）。置 false 即完全回到原价逻辑，
 * 不需要回滚镜像。
 *
 * @author Amos
 */
@Component
public class TierPricePricingHelper {

    private static final Logger LOG = LoggerFactory.getLogger(TierPricePricingHelper.class);

    private static final String RULE_TABLE = "hou_plugin_goods_tierprice_rule";
    private static final String TIER_TABLE = "hou_plugin_goods_tierprice_tier";

    private final JdbcTemplate jdbc;

    /** 阶梯价总开关；置 false 时 unitPrice 恒返回 null。 */
    @Value("${houcloud.pricing.tier-enabled:true}")
    private boolean enabled = true;

    public TierPricePricingHelper(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * 计算该商品在该数量下命中的阶梯单价。
     *
     * @param goodsId        商品 ID（mall 侧 goodsId）
     * @param qty            购买数量
     * @param originUnitPrice 商品原单价
     * @return 命中的阶梯单价；未命中 / 开关关闭 / 任何异常返回 {@code null}
     */
    public BigDecimal unitPrice(String goodsId, int qty, BigDecimal originUnitPrice) {
        if (!enabled || goodsId == null || goodsId.isBlank() || qty <= 0
                || originUnitPrice == null || originUnitPrice.signum() <= 0) {
            return null;
        }
        try {
            String sql = "SELECT t.price_type, t.discount_rate, t.fixed_price "
                    + "FROM " + RULE_TABLE + " r JOIN " + TIER_TABLE + " t ON t.rule_id = r.id "
                    + "WHERE r.status = 1 "
                    + "  AND (r.start_time IS NULL OR r.start_time <= NOW()) "
                    + "  AND (r.end_time IS NULL OR r.end_time >= NOW()) "
                    + "  AND (r.scope_type = 'ALL' "
                    + "       OR (r.scope_type = 'GOODS' AND FIND_IN_SET(?, r.scope_ids))) "
                    + "  AND t.min_qty <= ? AND (t.max_qty IS NULL OR t.max_qty >= ?) "
                    + "ORDER BY r.priority DESC, r.id DESC, t.min_qty DESC LIMIT 1";
            List<Map<String, Object>> rows = jdbc.queryForList(sql, goodsId.trim(), qty, qty);
            if (rows.isEmpty()) {
                return null;
            }
            Map<String, Object> row = rows.get(0);
            String priceType = String.valueOf(row.get("price_type"));
            BigDecimal unit;
            if ("FIXED".equalsIgnoreCase(priceType)) {
                unit = toDecimal(row.get("fixed_price"));
            } else {
                BigDecimal rate = toDecimal(row.get("discount_rate"));
                unit = rate == null ? null : originUnitPrice.multiply(rate);
            }
            if (unit == null) {
                return null;
            }
            unit = unit.setScale(2, RoundingMode.HALF_UP);
            if (unit.signum() <= 0 || unit.compareTo(originUnitPrice) >= 0) {
                // 折扣不打折（如 1.0）或价格算高：视为未命中，保持原价
                return null;
            }
            LOG.info("[阶梯价] 命中：goodsId={}, qty={}, 原价={}, 阶梯单价={}", goodsId, qty, originUnitPrice, unit);
            return unit;
        } catch (Exception e) {
            // 表不存在 / 字段变更 / 连接异常：一律不干预算价
            LOG.warn("[阶梯价] 计算失败，按原价处理：goodsId={}, qty={}, err={}", goodsId, qty, e.toString());
            return null;
        }
    }

    private static BigDecimal toDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal d) {
            return d;
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
