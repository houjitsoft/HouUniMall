package com.houjiang.payment.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.dto.merchantQual.MerchantQualQueryRequest;
import com.houjiang.payment.dto.merchantQual.MerchantQualSaveRequest;
import com.houjiang.payment.dto.merchantQual.MerchantQualVO;
import com.houjiang.payment.security.PaymentOrganizationResolver;
import com.houjiang.payment.service.PayMerchantQualService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 商户资质管理 Controller（管理后台）
 * <p>
 * 路径前缀：/api/payment/admin/merchantQual
 * 通过 bff-web /web/payment/merchantQual/** 透传到这里。
 *
 * @author Amos
 * @since 2026-05-13
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/admin/merchantQual")
@RequiredArgsConstructor
public class PayMerchantQualController {

    private final PayMerchantQualService merchantQualService;
    private final PaymentOrganizationResolver organizationResolver;

    /**
     * 分页查询
     */
    @PostMapping("/page")
    public ResponseResult<IPage<MerchantQualVO>> page(@RequestBody MerchantQualQueryRequest request) {
        return ResponseResult.success(merchantQualService.page(request));
    }

    /**
     * 详情（默认敏感字段脱敏；plainSensitive=true 解密返回明文，用于编辑回填）
     */
    @GetMapping("/{id}")
    public ResponseResult<MerchantQualVO> detail(@PathVariable("id") Long id,
                                                 @RequestParam(value = "plainSensitive", required = false, defaultValue = "false") boolean plainSensitive) {
        MerchantQualVO vo = merchantQualService.detail(id, plainSensitive);
        if (vo == null) {
            return ResponseResult.fail(404, "资质不存在");
        }
        return ResponseResult.success(vo);
    }

    /**
     * 新增 / 编辑
     */
    @PostMapping("/save")
    public ResponseResult<Long> save(@RequestBody MerchantQualSaveRequest request) {
        try {
            Long id = merchantQualService.save(request);
            return ResponseResult.success(id);
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("[merchantQual/save] error", e);
            return ResponseResult.fail(500, "保存失败：" + e.getMessage());
        }
    }

    /** 智能入库专用：组织、创建人和禁用状态均由服务端决定。 */
    @PostMapping("/import")
    public ResponseResult<Long> intelligentImport(@RequestBody MerchantQualSaveRequest request) {
        long tenantId = organizationResolver.requireCurrentTenantId();
        String creatorId = organizationResolver.requireCurrentUserId();
        return ResponseResult.success(merchantQualService.importDisabled(request, tenantId, creatorId));
    }

    /**
     * 启用 / 禁用
     */
    @PostMapping("/toggle")
    public ResponseResult<Boolean> toggle(@RequestBody Map<String, Object> body) {
        if (body == null || body.get("id") == null || body.get("status") == null) {
            return ResponseResult.fail(400, "id/status 必填");
        }
        Long id = Long.valueOf(body.get("id").toString());
        Integer status = Integer.valueOf(body.get("status").toString());
        return ResponseResult.success(merchantQualService.toggleStatus(id, status));
    }

    /**
     * 逻辑删除
     */
    @PostMapping("/delete")
    public ResponseResult<Boolean> delete(@RequestBody Map<String, Object> body) {
        if (body == null || body.get("id") == null) {
            return ResponseResult.fail(400, "id 必填");
        }
        Long id = Long.valueOf(body.get("id").toString());
        return ResponseResult.success(merchantQualService.delete(id));
    }
}
