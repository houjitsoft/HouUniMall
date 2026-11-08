package com.houjiang.gift.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 满赠规则。
 *
 * @param thresholdAmount 门槛金额（订单商品金额，含）
 * @param giftMode        FIXED 固定赠品 / CHOOSE 赠品池多选一
 * @param maxGiftKinds    CHOOSE 模式下每单最多可选几种
 */
public record GiftRule(
        long id,
        String ruleName,
        String scopeType,
        List<String> scopeIds,
        BigDecimal thresholdAmount,
        String giftMode,
        int maxGiftKinds,
        int status,
        int priority,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String remark,
        List<GiftItem> items) {

    public boolean enabled() {
        return status == 1;
    }
}
