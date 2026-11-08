package com.houjiang.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 砍价成交价助手（对应插件 {@code order-bargain}）。
 *
 * <p><b>为什么在 order 核心</b>：用户砍到底价后必须按成交价下单，而成交价只能由
 * {@code calculatePrice} 决定（{@code createOrders} 复用同一方法）。插件只能给前端展示。
 *
 * <p><b>生效口径（严格）</b>：
 * <ul>
 *   <li>只认<b>本人</b>的砍价单：{@code user_id} 必须匹配，不允许拿别人的砍价单结算；</li>
 *   <li>只认<b>已砍到底价</b>（{@code status = 'REACHED'}）且<b>未过期</b>、<b>未下单</b>的砍价单；</li>
 *   <li>只对该砍价单绑定的那个商品生效（{@code activity.goods_id = goodsId}）；</li>
 *   <li>成交价取 {@code current_price}，且必须低于原价才生效。</li>
 * </ul>
 *
 * <p>下单成功后由前端调插件 {@code /front/mark-ordered} 把砍价单置为 {@code ORDERED}，
 * 因此同一个砍价单只能成交一次。
 *
 * <p>任何异常 / 缺表 / 脏数据一律返回 {@code null}，调用方保持原价。
 * 开关：{@code houcloud.pricing.bargain-enabled}（默认 true）。
 *
 * @author Amos
 */
@Component
public class BargainPricePricingHelper {

    private static final Logger LOG = LoggerFactory.getLogger(BargainPricePricingHelper.class);

    private static final String INSTANCE_TABLE = "hou_plugin_order_bargain_instance";
    private static final String ACTIVITY_TABLE = "hou_plugin_order_bargain_activity";

    private final JdbcTemplate jdbc;

    @Value("${houcloud.pricing.bargain-enabled:true}")
    private boolean enabled = true;

    public BargainPricePricingHelper(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * 该用户在该商品上是否有可成交的砍价单。
     *
     * @param userId  下单用户
     * @param goodsId 商品 ID
     * @param originUnitPrice 商品原单价
     * @return 砍价成交单价；无可用砍价单 / 开关关闭 / 异常返回 {@code null}
     */
    public BigDecimal unitPrice(Long userId, String goodsId, BigDecimal originUnitPrice) {
        if (!enabled || userId == null || goodsId == null || goodsId.isBlank()
                || originUnitPrice == null || originUnitPrice.signum() <= 0) {
            return null;
        }
        try {
            String sql = "SELECT i.current_price FROM " + INSTANCE_TABLE + " i "
                    + "JOIN " + ACTIVITY_TABLE + " a ON a.id = i.activity_id "
                    + "WHERE i.user_id = ? AND i.status = 'REACHED' "
                    + "  AND i.expire_time IS NOT NULL AND i.expire_time >= NOW() "
                    + "  AND a.goods_id = ? "
                    + "ORDER BY i.id DESC LIMIT 1";
            List<Map<String, Object>> rows = jdbc.queryForList(sql, String.valueOf(userId), goodsId.trim());
            if (rows.isEmpty()) {
                return null;
            }
            Object raw = rows.get(0).get("current_price");
            if (raw == null) {
                return null;
            }
            BigDecimal price = raw instanceof BigDecimal d ? d : new BigDecimal(String.valueOf(raw));
            if (price.signum() <= 0 || price.compareTo(originUnitPrice) >= 0) {
                return null;
            }
            LOG.info("[砍价] 命中成交价：userId={}, goodsId={}, 原价={}, 成交价={}", userId, goodsId, originUnitPrice, price);
            return price;
        } catch (Exception e) {
            LOG.warn("[砍价] 查询失败，按原价处理：userId={}, goodsId={}, err={}", userId, goodsId, e.toString());
            return null;
        }
    }
}
