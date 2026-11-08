package com.houjiang.gift.service;

import com.houjiang.gift.domain.GiftItem;
import com.houjiang.gift.domain.GiftRule;
import com.houjiang.gift.repository.GiftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 满赠业务：规则维护、可赠判定、赠出登记。
 *
 * <p><b>边界</b>：判定与登记都是「供调用方使用」的接口——插件不写订单表、不改订单金额，
 * 结算页决定要不要把赠品加进购物车/订单，下单成功后调用 {@code /front/claim} 登记赠出并扣赠品库存。
 */
@Service
public class GiftService {

    private final GiftRepository repository;

    public GiftService(GiftRepository repository) {
        this.repository = repository;
    }

    public List<GiftRule> list() {
        return repository.findAll();
    }

    public Map<String, Object> stats() {
        return repository.stats();
    }

    public long save(GiftRule rule) {
        if (rule.ruleName() == null || rule.ruleName().isBlank()) {
            throw new IllegalArgumentException("规则名称不能为空");
        }
        String scope = rule.scopeType() == null ? "ALL" : rule.scopeType().toUpperCase();
        if (!List.of("ALL", "CATEGORY", "GOODS").contains(scope)) {
            throw new IllegalArgumentException("生效范围只能是 ALL / CATEGORY / GOODS");
        }
        String mode = rule.giftMode() == null ? "FIXED" : rule.giftMode().toUpperCase();
        if (!List.of("FIXED", "CHOOSE").contains(mode)) {
            throw new IllegalArgumentException("赠品方式只能是 FIXED / CHOOSE");
        }
        if (rule.thresholdAmount() == null || rule.thresholdAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("门槛金额必须大于 0");
        }
        if (rule.items() == null || rule.items().isEmpty()) {
            throw new IllegalArgumentException("至少配置一个赠品");
        }
        for (GiftItem item : rule.items()) {
            if (item.goodsId() == null || item.goodsId().isBlank()) {
                throw new IllegalArgumentException("赠品商品 ID 不能为空");
            }
            if (item.giftTitle() == null || item.giftTitle().isBlank()) {
                throw new IllegalArgumentException("赠品名称不能为空");
            }
            if (item.giftQty() <= 0) {
                throw new IllegalArgumentException("赠送数量必须大于 0");
            }
        }
        return repository.save(new GiftRule(rule.id(), rule.ruleName(), scope, rule.scopeIds(),
                rule.thresholdAmount(), mode, Math.max(1, rule.maxGiftKinds()), rule.status(), rule.priority(),
                rule.startTime(), rule.endTime(), rule.remark(), rule.items()));
    }

    public void delete(long id) {
        repository.delete(id);
    }

    /**
     * 可赠判定：按订单商品金额与商品/类目集合匹配规则，返回命中的规则与可选赠品。
     */
    public Map<String, Object> evaluate(BigDecimal amount, List<String> goodsIds, List<String> categoryIds) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("订单金额不能为负数");
        }
        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("matched", false);
        result.put("amount", amount);

        for (GiftRule rule : repository.findAll()) {
            if (!rule.enabled() || !inWindow(rule, now)) {
                continue;
            }
            if (amount.compareTo(rule.thresholdAmount()) < 0 || !scopeHit(rule, goodsIds, categoryIds)) {
                continue;
            }
            List<Map<String, Object>> available = new ArrayList<>();
            for (GiftItem item : rule.items()) {
                if (!item.inStock()) {
                    continue;
                }
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("itemId", item.id());
                row.put("goodsId", item.goodsId());
                row.put("skuId", item.skuId());
                row.put("title", item.giftTitle());
                row.put("giftQty", item.giftQty());
                row.put("remaining", item.stockTotal() <= 0 ? -1 : item.remaining());
                available.add(row);
            }
            if (available.isEmpty()) {
                continue;
            }
            result.put("matched", true);
            result.put("ruleId", rule.id());
            result.put("ruleName", rule.ruleName());
            result.put("thresholdAmount", rule.thresholdAmount());
            result.put("giftMode", rule.giftMode());
            result.put("maxGiftKinds", rule.maxGiftKinds());
            result.put("gifts", available);
            return result;
        }
        result.put("gifts", List.of());
        return result;
    }

    /** 赠出登记：订单创建成功后调用，幂等键 (orderNo, itemId)。 */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> claim(String orderNo, String userId, long ruleId, long itemId, int qty) {
        if (orderNo == null || orderNo.isBlank()) {
            throw new IllegalArgumentException("订单号不能为空");
        }
        if (qty <= 0) {
            throw new IllegalArgumentException("赠送数量必须大于 0");
        }
        GiftItem target = null;
        for (GiftItem item : repository.itemsOf(ruleId)) {
            if (item.id() == itemId) {
                target = item;
                break;
            }
        }
        if (target == null) {
            throw new IllegalArgumentException("赠品不存在或不属于该规则");
        }
        if (!target.inStock()) {
            throw new IllegalStateException("赠品库存不足");
        }
        // 幂等：同一订单同一赠品只登记一次，重复调用直接返回已有记录（不报错、不重复扣库存）
        Map<String, Object> exists = repository.recordOf(orderNo, itemId);
        if (exists != null) {
            // 幂等命中：记录已存在，但订单主表的 giveaway_info 可能还没写（比如上次登记时订单行尚未落库），这里补写
            String existingText = "赠 " + exists.get("giftTitle") + " ×" + exists.get("giftQty");
            exists.put("orderGiveawayUpdated", repository.updateOrderGiveawayInfo(orderNo, existingText));
            exists.put("alreadyClaimed", true);
            return exists;
        }
        try {
            repository.recordGift(orderNo, userId, ruleId, target, qty);
        } catch (org.springframework.dao.DuplicateKeyException duplicate) {
            Map<String, Object> again = repository.recordOf(orderNo, itemId);
            if (again != null) {
                again.put("alreadyClaimed", true);
                return again;
            }
            throw duplicate;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderNo", orderNo);
        result.put("itemId", itemId);
        result.put("giftTitle", target.giftTitle());
        result.put("giftQty", qty);
        // 赠品写进订单主表 giveaway_info：买家在订单详情、商家在发货单都能看到「赠 xxx ×N」
        String giveawayText = "赠 " + target.giftTitle() + " ×" + qty;
        int rows = repository.updateOrderGiveawayInfo(orderNo, giveawayText);
        result.put("orderGiveawayUpdated", rows);
        return result;
    }

    public Map<String, Object> pageRecords(String orderNo, String userId, int page, int size) {
        int safePage = Math.max(1, page);
        int safeSize = Math.min(Math.max(1, size), 200);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", repository.countRecords(orderNo, userId));
        result.put("list", repository.pageRecords(orderNo, userId, (safePage - 1) * safeSize, safeSize));
        result.put("page", safePage);
        result.put("size", safeSize);
        return result;
    }

    private boolean inWindow(GiftRule rule, LocalDateTime now) {
        if (rule.startTime() != null && now.isBefore(rule.startTime())) {
            return false;
        }
        return rule.endTime() == null || !now.isAfter(rule.endTime());
    }

    private boolean scopeHit(GiftRule rule, List<String> goodsIds, List<String> categoryIds) {
        return switch (rule.scopeType()) {
            case "GOODS" -> goodsIds != null && goodsIds.stream().anyMatch(rule.scopeIds()::contains);
            case "CATEGORY" -> categoryIds != null && categoryIds.stream().anyMatch(rule.scopeIds()::contains);
            default -> true;
        };
    }
}
