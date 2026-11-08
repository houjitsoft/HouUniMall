package com.houjiang.payment.feign;

import com.houjiang.commons.dto.payment.AccountPeriodParamDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.shop.AccountPeriodVO;
import com.houjiang.commons.vo.shop.ShopBankMessageVO;
import com.houjiang.commons.vo.shop.ShopUserMessageResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商户服务Feign客户端
 * 替代原 BusinessServiceClient，指向新的 merchant 微服务
 * @author Amos
 */
@FeignClient("merchant")
public interface MerchantServiceClient {

    /**
     * 更新分销小店销量
     * 该接口已由 merchant 的 MerchantInternalController 提供（/merchant/internal/**）。
     * 注意：接口存在不等于已实现——账期与质保金充值在 2026-09-19 前是空实现（质保金直接返回成功但未落账），
     * 现已补为真实落库，缺少必填参数会返回失败。
     *
     * @param orderId 订单ID
     */
    @PostMapping("/merchant/internal/updateVkShopSaleNum")
    void updateVkShopSaleNum(@RequestParam("orderId") String orderId);

    /**
     * 质保金充值记录
     * 该接口已由 merchant 的 MerchantInternalController 提供（/merchant/internal/**）。
     * 注意：接口存在不等于已实现——账期与质保金充值在 2026-09-19 前是空实现（质保金直接返回成功但未落账），
     * 现已补为真实落库，缺少必填参数会返回失败。
     *
     * @param map 充值参数
     * @return 返回操作结果
     */
    @PostMapping("/merchant/internal/recharge")
    ResponseResult recharge(@RequestBody Map<Object, Object> map);

    /**
     * 根据店铺ID查询店铺用户信息
     * 该接口已由 merchant 的 MerchantInternalController 提供（/merchant/internal/**）。
     * 注意：接口存在不等于已实现——账期与质保金充值在 2026-09-19 前是空实现（质保金直接返回成功但未落账），
     * 现已补为真实落库，缺少必填参数会返回失败。
     *
     * @param shopId 店铺ID
     * @return 返回店铺用户消息结果
     */
    @GetMapping("/merchant/internal/queryUserId")
    ShopUserMessageResultVO queryUserId(@RequestParam("shopId") Integer shopId);

    /**
     * 根据店铺ID获取店铺银行信息
     * 该接口已由 merchant 的 MerchantInternalController 提供（/merchant/internal/**）。
     * 注意：接口存在不等于已实现——账期与质保金充值在 2026-09-19 前是空实现（质保金直接返回成功但未落账），
     * 现已补为真实落库，缺少必填参数会返回失败。
     *
     * @param shopId 店铺ID
     * @return 返回店铺银行信息
     */
    @GetMapping("/merchant/internal/getShopBankMessage")
    ShopBankMessageVO getShopBankMessage(@RequestParam("shopId") String shopId);

    /**
     * 根据店铺ID和用户ID获取账期信息
     * 该接口已由 merchant 的 MerchantInternalController 提供（/merchant/internal/**）。
     * 注意：接口存在不等于已实现——账期与质保金充值在 2026-09-19 前是空实现（质保金直接返回成功但未落账），
     * 现已补为真实落库，缺少必填参数会返回失败。
     *
     * @param shopId 店铺ID
     * @param userId 用户ID
     * @return 返回账期信息
     */
    @GetMapping("/merchant/internal/getAccountPeriodByIds")
    AccountPeriodVO getAccountPeriodByIds(@RequestParam("shopId") Integer shopId, @RequestParam("userId") String userId);

    /**
     * 更新账期信息
     * 该接口已由 merchant 的 MerchantInternalController 提供（/merchant/internal/**）。
     * 注意：接口存在不等于已实现——账期与质保金充值在 2026-09-19 前是空实现（质保金直接返回成功但未落账），
     * 现已补为真实落库，缺少必填参数会返回失败。
     *
     * @param periodParam 账期参数
     */
    @PostMapping("/merchant/internal/updateAccountPeriod")
    void updateAccountPeriod(@RequestBody AccountPeriodParamDTO periodParam);
}
