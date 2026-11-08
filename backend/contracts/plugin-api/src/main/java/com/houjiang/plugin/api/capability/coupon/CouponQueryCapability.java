package com.houjiang.plugin.api.capability.coupon;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 券查询能力：按券 ID 取回券信息（状态 / 有效期 / 归属 / 面额 / 可用商品）。
 *
 * <p><b>为什么单独成契约</b>：order 原本用 {@code @FeignClient("activity")} 的
 * {@code getVoucherDetail} 取券，但这类调用是<b>券状态校验</b>（是否用过 / 过期 / 回收 /
 * 归属本人），不是算价——它返回的是不同的业务错误码，而 {@code PromotionCalcExtension}
 * 的语义是「输入原价、输出优惠额」。把状态校验塞进算价扩展点，会把「券已使用」
 * 错误地降级成「无优惠」，制造金额问题。故按定稿 §3.2「Core 面向能力编程」单独定义。
 *
 * <p><b>职责边界</b>：本能力只负责<b>取回券信息</b>，是否可用由 Core 按业务规则判定
 * （状态、有效期、归属）。这样券规则变更仍由 Core 掌握，插件只做数据供给。
 *
 * <p>未安装券插件时返回 {@link Optional#empty()}，由 Core 走降级分支。
 *
 * @author Amos
 */
public interface CouponQueryCapability {

    /**
     * @param voucherId 券 ID
     * @return 券信息；未找到或插件未安装时为空
     */
    Optional<CouponInfo> findByVoucherId(String voucherId);

    /**
     * 券信息快照。
     *
     * @param voucherId       券 ID
     * @param state           券状态：1 未使用 / 2 已使用 / 3 已过期 / 4 已回收
     * @param endDate         有效期截止
     * @param ownerId         归属用户 ID
     * @param price           券面额
     * @param availableGoods  可用商品 ID；为空表示不限商品
     */
    record CouponInfo(String voucherId,
                      Integer state,
                      Date endDate,
                      String ownerId,
                      BigDecimal price,
                      List<Integer> availableGoods) {
    }
}
