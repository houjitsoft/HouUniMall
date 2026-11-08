package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.PaymentServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商户资质管理 BFF 控制器
/**
 *
 * <p>16 终端 × 多支付渠道的商户资质登记/查询/编辑/启停/删除。
 * 路径以 /web/payment/merchantQual 开头，统一通过 PaymentServiceFeign 透传到 paas/payment 服务。</p>
 *
 * @author Amos
 * @since 2026-05
 */
@RestController
@RequestMapping("/web/payment/merchantQual")
public class WebMerchantQualController {

    @Autowired
    private PaymentServiceFeign paymentServiceFeign;

    /**
     * 商户资质分页查询
     */
    @PostMapping("/page")
    public ResponseResult page(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.merchantQualPage(param);
    }

    /**
     * 商户资质详情
/**
     *
     * @param id              主键 ID
     * @param plainSensitive  是否解密返回敏感字段（编辑回填时传 true）
     */
    @GetMapping("/detail/{id}")
    public ResponseResult detail(@PathVariable("id") Long id,
                                 @RequestParam(value = "plainSensitive", required = false) Boolean plainSensitive) {
        return paymentServiceFeign.merchantQualDetail(id, plainSensitive);
    }

    /**
     * 保存商户资质（id 为空 = 新增；id 非空 = 更新）
     */
    @PostMapping("/save")
    public ResponseResult save(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.merchantQualSave(param);
    }

    /**
     * 启用/停用商户资质（status: 0=停用 1=启用）
     */
    @PostMapping("/toggle")
    public ResponseResult toggle(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.merchantQualToggle(param);
    }

    /**
     * 删除商户资质（逻辑删除）
     */
    @PostMapping("/delete")
    public ResponseResult delete(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.merchantQualDelete(param);
    }
}
