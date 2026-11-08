package com.houjiang.order.plugin;

import com.houjiang.commons.vo.activity.VoucherBaseVO;
import com.houjiang.order.feign.ActivityServiceClient;
import com.houjiang.plugin.api.capability.coupon.CouponQueryCapability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 券查询能力的遗留实现：内部调现有 {@code @FeignClient("activity")} 的
 * {@code getVoucherDetail}，把 {@link VoucherBaseVO} 映射成契约 {@link CouponInfo}。
 *
 * <p>与 {@link LegacyActivityPromotionCalcExtension} 同为过渡实现：活动逐个插件化后，
 * 由券插件自身提供本能力（可 standalone 部署，经 {@code PluginHostApi} 远程调用），届时删除。
 *
 * <p><b>故障语义</b>：取不到券时返回空由 Core 判定；但调用本身失败（服务不可达）必须抛出——
 * 静默当作「券不存在」会让用户以为券有效却没抵扣，或反过来绕过校验。
 *
 * @author Amos
 */
public class LegacyActivityCouponQueryCapability implements CouponQueryCapability {

    private static final Logger log = LoggerFactory.getLogger(LegacyActivityCouponQueryCapability.class);

    private final ActivityServiceClient activityServiceClient;

    public LegacyActivityCouponQueryCapability(ActivityServiceClient activityServiceClient) {
        this.activityServiceClient = activityServiceClient;
    }

    @Override
    public Optional<CouponInfo> findByVoucherId(String voucherId) {
        if (voucherId == null || voucherId.isBlank()) {
            return Optional.empty();
        }
        Map<String, Object> param = new HashMap<>();
        param.put("voucherId", voucherId);
        VoucherBaseVO voucher = activityServiceClient.getVoucherDetail(param);
        if (voucher == null) {
            return Optional.empty();
        }
        return Optional.of(new CouponInfo(
                voucherId,
                voucher.getVoucherState(),
                voucher.getVoucherEndDate(),
                voucher.getVoucherOwnerId(),
                voucher.getVoucherPrice(),
                voucher.getAvaliableGoods()));
    }
}
