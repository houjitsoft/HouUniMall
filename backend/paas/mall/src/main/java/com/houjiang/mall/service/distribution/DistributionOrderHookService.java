package com.houjiang.mall.service.distribution;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.mall.entity.distribution.DistributionOrder;
import com.houjiang.mall.entity.distribution.DistributionSetting;
import com.houjiang.mall.entity.distribution.Distributor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分销订单事件挂钩服务（订单侧 → 商城侧）
 *
 * <p>订单服务在「下单」「确认收货/完成」「退款/关闭」三个时点，通过
 * {@code /internal/distribution/order/*} 把订单事件推过来，本服务负责把订单落进
 * {@code hou_mall_distribution_order}，并在订单完成时生成佣金（走
 * {@link DistributionSettleService}，天然幂等）。
 *
 * <p>状态口径与 {@link DistributionSettleService#ORDER_STATUS_FINISHED} 保持一致：
 * <ul>
 *   <li>1 = 已下单（未完成，只作「预估收益」展示，不产生佣金）</li>
 *   <li>3 = 已完成（确认收货，触发生成佣金 → pending）</li>
 *   <li>4 = 已失效（退款/关闭，已生成的待结算佣金作废）</li>
 * </ul>
 *
 * @author Amos
 */
@Slf4j
@Service
public class DistributionOrderHookService {

    /** 已下单（未完成） */
    public static final int STATUS_CREATED = 1;
    /** 已完成（确认收货） */
    public static final int STATUS_FINISHED = 3;
    /** 已失效（退款 / 关闭） */
    public static final int STATUS_INVALID = 4;

    private final DistributionSettingService distributionSettingService;
    private final DistributionOrderService distributionOrderService;
    private final DistributorService distributorService;
    private final DistributionSettleService distributionSettleService;

    public DistributionOrderHookService(DistributionSettingService distributionSettingService,
                                        DistributionOrderService distributionOrderService,
                                        DistributorService distributorService,
                                        DistributionSettleService distributionSettleService) {
        this.distributionSettingService = distributionSettingService;
        this.distributionOrderService = distributionOrderService;
        this.distributorService = distributorService;
        this.distributionSettleService = distributionSettleService;
    }

    /**
     * 下单事件：登记分销订单（幂等，按 orderId 去重）
     *
     * @param params orderId / orderNo / shopId / buyerId / shareUserId / orderAmount / goodsLines
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> onCreate(Map<String, Object> params) {
        Long orderId = toLong(params.get("orderId"));
        if (orderId == null) {
            return fail("orderId 不能为空");
        }
        Long shopId = toLong(params.get("shopId"));
        DistributionSetting setting = distributionSettingService.getSetting(shopId);
        if (setting.getEnabled() == null || setting.getEnabled() != 1) {
            return skip("分销未开启");
        }

        Long buyerId = toLong(params.get("buyerId"));
        Long shareUserId = toLong(params.get("shareUserId"));
        Distributor distributor = resolveDistributor(setting, shareUserId, buyerId);
        if (distributor == null) {
            return skip("无有效分销关系");
        }

        String goodsSnapshot = toGoodsSnapshot(params.get("goodsLines"));

        DistributionOrder exist = getByOrderId(orderId);
        if (exist != null) {
            // 已存在：只补金额/分销员/商品快照，不回退状态
            BigDecimal amount = toDecimal(params.get("orderAmount"));
            boolean dirty = false;
            if (amount != null && amount.compareTo(nz(exist.getOrderAmount())) != 0) {
                exist.setOrderAmount(amount);
                dirty = true;
            }
            if (exist.getDistributorId() == null) {
                exist.setDistributorId(distributor.getId());
                dirty = true;
            }
            if (StringUtils.hasText(goodsSnapshot) && !goodsSnapshot.equals(exist.getGoodsSnapshot())) {
                exist.setGoodsSnapshot(goodsSnapshot);
                dirty = true;
            }
            if (dirty) {
                exist.setUpdateTime(LocalDateTime.now());
                distributionOrderService.updateById(exist);
            }
            Map<String, Object> res = new HashMap<>();
            res.put("ok", true);
            res.put("duplicate", true);
            res.put("distributionOrderId", exist.getId());
            res.put("distributorId", exist.getDistributorId());
            return res;
        }

        DistributionOrder order = new DistributionOrder();
        order.setShopId(shopId == null ? 0L : shopId);
        order.setOrderId(orderId);
        order.setOrderNo(str(params.get("orderNo")));
        order.setDistributorId(distributor.getId());
        order.setBuyerId(buyerId);
        order.setOrderAmount(toDecimal(params.get("orderAmount")));
        order.setGoodsSnapshot(goodsSnapshot);
        order.setRefundedAmount(BigDecimal.ZERO);
        order.setOrderStatus(STATUS_CREATED);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        distributionOrderService.save(order);

        Map<String, Object> res = new HashMap<>();
        res.put("ok", true);
        res.put("duplicate", false);
        res.put("distributionOrderId", order.getId());
        res.put("distributorId", order.getDistributorId());
        return res;
    }

    /**
     * 订单完成事件：置为已完成并生成佣金
     *
     * <p>若订单尚未登记（下单事件没推过来 / 历史订单），先用事件携带的订单信息补登记，
     * 再走完成流程——这样订单侧只需要在确认收货这一个时点调用即可，不必两处挂钩。
     *
     * @param params orderId（必填）+ 补登记所需的 orderNo/shopId/buyerId/shareUserId/orderAmount/goodsLines
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> onFinish(Map<String, Object> params) {
        Long orderId = params == null ? null : toLong(params.get("orderId"));
        if (orderId == null) {
            return fail("orderId 不能为空");
        }
        DistributionOrder order = getByOrderId(orderId);
        if (order == null) {
            Map<String, Object> created = onCreate(params);
            if (!Boolean.TRUE.equals(created.get("ok"))) {
                Map<String, Object> res = new HashMap<>();
                res.put("ok", false);
                res.put("skipped", true);
                res.put("reason", "无有效分销关系（" + created.get("reason") + "）");
                return res;
            }
            order = getByOrderId(orderId);
            if (order == null) {
                return skip("分销订单登记后仍查不到");
            }
        } else {
            // 完成时补齐/刷新商品快照与金额
            String goodsSnapshot = toGoodsSnapshot(params == null ? null : params.get("goodsLines"));
            boolean dirty = false;
            if (StringUtils.hasText(goodsSnapshot)) {
                order.setGoodsSnapshot(goodsSnapshot);
                dirty = true;
            }
            BigDecimal amount = params == null ? null : toDecimal(params.get("orderAmount"));
            if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0
                    && amount.compareTo(nz(order.getOrderAmount())) != 0) {
                order.setOrderAmount(amount);
                dirty = true;
            }
            if (dirty) {
                order.setUpdateTime(LocalDateTime.now());
                distributionOrderService.updateById(order);
            }
        }
        if (order.getOrderStatus() != null && order.getOrderStatus() == STATUS_INVALID) {
            return skip("订单已失效");
        }
        order.setOrderStatus(STATUS_FINISHED);
        order.setUpdateTime(LocalDateTime.now());
        distributionOrderService.updateById(order);

        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        result.putAll(distributionSettleService.generateForOrder(orderId));
        return result;
    }

    /**
     * 订单失效 / 退款事件：按退款金额等比冲减待结算佣金；无金额则整单作废。
     *
     * @param params orderId（必填）+ refundAmount（可选）+ fullInvalidate（可选，默认无金额时 true）
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> onCancel(Map<String, Object> params) {
        Long orderId = params == null ? null : toLong(params.get("orderId"));
        if (orderId == null) {
            return fail("orderId 不能为空");
        }
        DistributionOrder order = getByOrderId(orderId);
        if (order == null) {
            return skip("未登记的分销订单");
        }

        BigDecimal refundAmount = params.get("refundAmount") == null ? null : toDecimal(params.get("refundAmount"));
        boolean fullInvalidate = params.get("fullInvalidate") != null
                && (Boolean.TRUE.equals(params.get("fullInvalidate"))
                || "1".equals(String.valueOf(params.get("fullInvalidate")))
                || "true".equalsIgnoreCase(String.valueOf(params.get("fullInvalidate"))));
        if (refundAmount == null && !fullInvalidate) {
            // 兼容旧调用：只传 orderId → 整单作废
            fullInvalidate = true;
        }

        Map<String, Object> clawback = distributionSettleService.clawbackByOrder(orderId, refundAmount, fullInvalidate);
        boolean wipe = Boolean.TRUE.equals(clawback.get("fullInvalidate"));
        if (wipe) {
            order.setOrderStatus(STATUS_INVALID);
            order.setUpdateTime(LocalDateTime.now());
            distributionOrderService.updateById(order);
        } else {
            // 部分退款：保持已完成，刷新 refunded 已由 settle 写入
            order = getByOrderId(orderId);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        result.putAll(clawback);
        if (order != null) {
            result.put("orderStatus", order.getOrderStatus());
            result.put("refundedAmount", order.getRefundedAmount());
        }
        return result;
    }

    /** 兼容旧签名 */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> onCancel(Long orderId) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("fullInvalidate", true);
        return onCancel(params);
    }

    /**
     * 清理「下单预登记但未完成」的孤儿分销订单。
     *
     * <p>下单时就会登记分销订单（用于「预估收益 / 推广订单」展示），但用户可能一直不付款、
     * 或下单事务回滚导致商城侧留下一条永远等不到的记录。这里把超过
     * {@code staleDays} 天仍处于「已下单」且未产生任何佣金的记录逻辑删除，避免脏数据
     * 污染客户统计与业绩看板。
     *
     * @param staleDays 超过多少天未完成即视为孤儿（默认 7 天）
     * @return 清理条数
     */
    @Transactional(rollbackFor = Exception.class)
    public int cleanupStaleOrders(int staleDays) {
        int days = staleDays <= 0 ? 7 : staleDays;
        LocalDateTime before = LocalDateTime.now().minusDays(days);
        List<DistributionOrder> stale = distributionOrderService.lambdaQuery()
                .eq(DistributionOrder::getOrderStatus, STATUS_CREATED)
                .eq(DistributionOrder::getDeleted, 0)
                .lt(DistributionOrder::getCreateTime, before)
                .list();
        int removed = 0;
        for (DistributionOrder item : stale) {
            long count = distributionSettleService.countRecordsByOrder(item.getOrderId());
            if (count > 0) {
                // 已产生佣金的不动，交给正常的失效流程处理
                continue;
            }
            // 注意：deleted 是 MyBatis-Plus 逻辑删除字段，手动 set 后 updateById 不会写进去，
            // 必须走 removeById（由 MP 改写成 UPDATE ... SET deleted = 1）
            distributionOrderService.removeById(item.getId());
            removed++;
        }
        if (removed > 0) {
            log.info("分销孤儿订单清理: 清理 {} 条（超过 {} 天未完成）", removed, days);
        }
        return removed;
    }

    /**
     * 解析本单的一级受益人，结合 {@code bindMode}：
     * <ul>
     *   <li>bindMode=1 永久绑定：买家一旦有过有效分销归属，后续即便换分享人也不改绑；</li>
     *   <li>bindMode=2 保护期后可换绑：有分享人时以分享人为准；无分享人时在保护期内沿用最近归属。</li>
     * </ul>
     * 其它回落：自购返佣给上级；否则无有效分销关系。
     */
    private Distributor resolveDistributor(DistributionSetting setting, Long shareUserId, Long buyerId) {
        int bindMode = setting.getBindMode() == null ? 1 : setting.getBindMode();
        Distributor shared = null;
        if (shareUserId != null && shareUserId > 0) {
            shared = findActiveByUserId(setting.getShopId(), shareUserId);
        }

        if (buyerId != null && buyerId > 0) {
            // 永久绑定：已有历史归属则锁定，不再被新分享人改绑
            if (bindMode == 1) {
                Distributor permanent = resolveExistingBind(buyerId, null);
                if (permanent != null) {
                    return permanent;
                }
            } else {
                // 保护期模式：无新分享人时，保护期内沿用最近归属
                if (shared == null) {
                    Distributor protectedBind = resolveExistingBind(buyerId, setting.getBindProtectDays());
                    if (protectedBind != null) {
                        return protectedBind;
                    }
                }
            }
        }

        if (shared != null) {
            return shared;
        }

        if (buyerId != null && buyerId > 0) {
            Distributor self = findActiveByUserId(setting.getShopId(), buyerId);
            if (self != null && self.getParentId() != null && self.getParentId() > 0) {
                Distributor parent = distributorService.getById(self.getParentId());
                if (parent != null && parent.getStatus() != null && parent.getStatus() == 1) {
                    return parent;
                }
            }
        }
        return null;
    }

    /**
     * 查买家已有分销归属。
     *
     * @param protectDays null=不限时间（永久绑定）；&gt;0=仅取保护期内最近一笔；&lt;=0=不查
     */
    private Distributor resolveExistingBind(Long buyerId, Integer protectDays) {
        if (protectDays != null && protectDays <= 0) {
            return null;
        }
        var query = distributionOrderService.lambdaQuery()
                .eq(DistributionOrder::getBuyerId, buyerId)
                .ne(DistributionOrder::getOrderStatus, STATUS_INVALID)
                .eq(DistributionOrder::getDeleted, 0);
        if (protectDays != null) {
            query.ge(DistributionOrder::getCreateTime, LocalDateTime.now().minusDays(protectDays));
        }
        DistributionOrder recent = query.orderByDesc(DistributionOrder::getId).last("limit 1").one();
        if (recent == null || recent.getDistributorId() == null) {
            return null;
        }
        Distributor d = distributorService.getById(recent.getDistributorId());
        if (d == null || d.getStatus() == null || d.getStatus() != 1) {
            return null;
        }
        return d;
    }

    private Distributor findActiveByUserId(Long shopId, Long userId) {
        LambdaQueryWrapper<Distributor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Distributor::getUserId, userId);
        wrapper.eq(Distributor::getStatus, 1);
        wrapper.eq(Distributor::getDeleted, 0);
        wrapper.last("limit 1");
        if (shopId != null && shopId > 0) {
            wrapper.eq(Distributor::getShopId, shopId);
        }
        return distributorService.getOne(wrapper);
    }

    private DistributionOrder getByOrderId(Long orderId) {
        LambdaQueryWrapper<DistributionOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DistributionOrder::getOrderId, orderId);
        wrapper.eq(DistributionOrder::getDeleted, 0);
        wrapper.last("limit 1");
        return distributionOrderService.getOne(wrapper);
    }

    private static String toGoodsSnapshot(Object goodsLines) {
        if (goodsLines == null) {
            return null;
        }
        if (goodsLines instanceof String s) {
            return StringUtils.hasText(s) ? s : null;
        }
        try {
            return JSON.toJSONString(goodsLines);
        } catch (Exception e) {
            return null;
        }
    }

    private static BigDecimal toDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private static Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String str(Object value) {
        return value == null ? null : value.toString();
    }

    private static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static Map<String, Object> skip(String reason) {
        Map<String, Object> res = new HashMap<>();
        res.put("ok", false);
        res.put("skipped", true);
        res.put("reason", reason);
        return res;
    }

    private static Map<String, Object> fail(String reason) {
        Map<String, Object> res = new HashMap<>();
        res.put("ok", false);
        res.put("reason", reason);
        return res;
    }
}
