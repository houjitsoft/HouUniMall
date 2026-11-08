package com.houjiang.gift.domain;

/**
 * 赠品池里的一项。
 *
 * @param giftQty    每单赠送数量
 * @param stockTotal 总库存，0 表示不限
 * @param stockUsed  已赠出
 */
public record GiftItem(
        long id,
        long ruleId,
        String goodsId,
        String skuId,
        String giftTitle,
        int giftQty,
        int stockTotal,
        int stockUsed,
        int sort) {

    /** 剩余可赠数量；不限库存时返回 {@link Integer#MAX_VALUE}。 */
    public int remaining() {
        if (stockTotal <= 0) {
            return Integer.MAX_VALUE;
        }
        return Math.max(0, stockTotal - stockUsed);
    }

    public boolean inStock() {
        return remaining() >= giftQty;
    }
}
