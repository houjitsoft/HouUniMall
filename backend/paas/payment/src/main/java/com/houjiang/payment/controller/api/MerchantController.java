package com.houjiang.payment.controller.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.dto.mch.MerchantAccountUpdateRequest;
import com.houjiang.payment.dto.mch.MerchantDetailVO;
import com.houjiang.payment.dto.mch.MerchantFeeConfigRequest;
import com.houjiang.payment.dto.mch.MerchantListRequest;
import com.houjiang.payment.dto.mch.MerchantProductOpenRequest;
import com.houjiang.payment.dto.mch.MerchantRegRequest;
import com.houjiang.payment.dto.mch.MerchantStatusChangeRequest;
import com.houjiang.payment.dto.mch.MerchantUpdateRequest;
import com.houjiang.payment.entity.PayMerchantEntity;
import com.houjiang.payment.entity.PayMerchantFeeEntity;
import com.houjiang.payment.entity.PayMerchantProductEntity;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.service.api.MerchantService;
import com.houjiang.payment.util.ApiTokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 平台商户管理 Controller（W1 P0 任务）
 *
 * 对标通企付商户入驻/管理能力，接口面向平台运营/代理商
 *
 * 1. /reg         商户进件
 * 2. /list        商户列表（分页）
 * 3. /detail      商户详情（聚合产品+费率）
 * 4. /update      商户信息更新
 * 5. /changeStatus 商户状态变更（审核/冻结/注销）
 * 6. /configFee   商户费率配置
 * 7. /updateAccount 商户结算账户更新
 * 8. /openProduct 商户产品开通/停用
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/merchant")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    /**
     * 1. 商户进件
     */
    @PostMapping("/reg")
    public ResponseResult<PayMerchantEntity> reg(@Valid @RequestBody MerchantRegRequest request,
                                                 HttpServletRequest http) {
        String userId = ApiTokenUtils.resolveUserId(http);
        if (!StringUtils.hasText(userId)) {
            return ResponseResult.fail(401, "用户未登录");
        }
        try {
            PayMerchantEntity e = merchantService.register(request, userId);
            return ResponseResult.success(e);
        } catch (PaymentException ex) {
            log.warn("[merchant/reg] fail: {}", ex.getMessage());
            return ResponseResult.fail(500, ex.getMessage());
        } catch (Exception ex) {
            log.error("[merchant/reg] error", ex);
            return ResponseResult.fail(500, "商户进件失败: " + ex.getMessage());
        }
    }

    /**
     * 2. 商户列表
     */
    @PostMapping("/list")
    public ResponseResult<IPage<PayMerchantEntity>> list(@RequestBody(required = false) MerchantListRequest request) {
        if (request == null) request = new MerchantListRequest();
        return ResponseResult.success(merchantService.list(request));
    }

    /**
     * 3. 商户详情
     */
    @PostMapping("/detail")
    public ResponseResult<MerchantDetailVO> detail(@RequestBody MerchantListRequest request) {
        if (request == null || !StringUtils.hasText(request.getMchNo())) {
            return ResponseResult.fail(500, "商户号不能为空");
        }
        try {
            return ResponseResult.success(merchantService.detail(request.getMchNo()));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    /**
     * 4. 商户信息更新
     */
    @PostMapping("/update")
    public ResponseResult<Boolean> update(@Valid @RequestBody MerchantUpdateRequest request,
                                          HttpServletRequest http) {
        String userId = ApiTokenUtils.resolveUserId(http);
        if (!StringUtils.hasText(userId)) {
            return ResponseResult.fail(401, "用户未登录");
        }
        try {
            return ResponseResult.success(merchantService.update(request, userId));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    /**
     * 5. 商户状态变更（审核/冻结/解冻/注销）
     */
    @PostMapping("/changeStatus")
    public ResponseResult<Boolean> changeStatus(@Valid @RequestBody MerchantStatusChangeRequest request,
                                                HttpServletRequest http) {
        String userId = ApiTokenUtils.resolveUserId(http);
        if (!StringUtils.hasText(userId)) {
            return ResponseResult.fail(401, "用户未登录");
        }
        try {
            return ResponseResult.success(merchantService.changeStatus(request, userId));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    /**
     * 6. 商户费率配置
     */
    @PostMapping("/configFee")
    public ResponseResult<PayMerchantFeeEntity> configFee(@Valid @RequestBody MerchantFeeConfigRequest request,
                                                          HttpServletRequest http) {
        String userId = ApiTokenUtils.resolveUserId(http);
        if (!StringUtils.hasText(userId)) {
            return ResponseResult.fail(401, "用户未登录");
        }
        try {
            return ResponseResult.success(merchantService.configFee(request, userId));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    /**
     * 7. 商户结算账户更新
     */
    @PostMapping("/updateAccount")
    public ResponseResult<Boolean> updateAccount(@Valid @RequestBody MerchantAccountUpdateRequest request,
                                                 HttpServletRequest http) {
        String userId = ApiTokenUtils.resolveUserId(http);
        if (!StringUtils.hasText(userId)) {
            return ResponseResult.fail(401, "用户未登录");
        }
        try {
            return ResponseResult.success(merchantService.updateSettleAccount(request, userId));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    /**
     * 8. 商户产品开通/停用
     */
    @PostMapping("/openProduct")
    public ResponseResult<PayMerchantProductEntity> openProduct(@Valid @RequestBody MerchantProductOpenRequest request,
                                                                HttpServletRequest http) {
        String userId = ApiTokenUtils.resolveUserId(http);
        if (!StringUtils.hasText(userId)) {
            return ResponseResult.fail(401, "用户未登录");
        }
        try {
            return ResponseResult.success(merchantService.openProduct(request, userId));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    // ============================ W4 自研补全 3 接口 ============================

    /**
     * 9. 个人商户进件（mchType=3 个人 / 2 个体工商户）
     */
    @PostMapping("/regPerson")
    public ResponseResult<PayMerchantEntity> regPerson(@RequestBody Map<String, Object> req,
                                                       HttpServletRequest http) {
        String userId = ApiTokenUtils.resolveUserId(http);
        if (!StringUtils.hasText(userId)) return ResponseResult.fail(401, "用户未登录");
        try {
            return ResponseResult.success(merchantService.regPerson(req, userId));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        } catch (Exception ex) {
            log.error("[merchant/regPerson] error", ex);
            return ResponseResult.fail(500, "个人商户进件失败: " + ex.getMessage());
        }
    }

    /**
     * 10. 资质补件
     */
    @PostMapping("/qualSupplement")
    public ResponseResult<Boolean> qualSupplement(@RequestBody Map<String, Object> req,
                                                  HttpServletRequest http) {
        String userId = ApiTokenUtils.resolveUserId(http);
        if (!StringUtils.hasText(userId)) return ResponseResult.fail(401, "用户未登录");
        try {
            return ResponseResult.success(merchantService.qualSupplement(req, userId));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    /**
     * 11. 子商户/子账户列表
     */
    @PostMapping("/subAccount/list")
    public ResponseResult<List<Map<String, Object>>> subAccountList(@RequestBody Map<String, Object> req) {
        try {
            return ResponseResult.success(merchantService.subAccountList(req));
        } catch (Exception ex) {
            log.error("[merchant/subAccount/list] error", ex);
            return ResponseResult.fail(500, ex.getMessage());
        }
    }
}
