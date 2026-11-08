package com.houjiang.payment.controller.api;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.dto.h5cash.H5CashOrderRequest;
import com.houjiang.payment.dto.h5cash.H5CashOrderResponse;
import com.houjiang.payment.entity.H5CashOrderEntity;
import com.houjiang.payment.service.api.H5CashService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 通企付 H5 收银台 Controller
 * 路由前缀：/api/payment/h5cash
 * 统一响应：commons.ResponseResult
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/h5cash")
@RequiredArgsConstructor
public class H5CashController {

    private final H5CashService h5CashService;

    /**
     * 27. H5 收银台下单
     * 调用通企付 /h5Cash/getH5Url 生成 H5 收银台跳转链接
     *
     * @param request H5 收银台下单请求
     * @return 含 H5Url 的响应
     */
    @PostMapping("/getH5Url")
    public ResponseResult<H5CashOrderResponse> getH5Url(@Valid @RequestBody H5CashOrderRequest request) {
        log.info("[H5Cash] getH5Url, mchOrderNo:{}, amount:{}",
                request.getMchOrderNo(), request.getAmount());
        return ResponseResult.success(h5CashService.getH5Url(request));
    }

    /**
     * jumpUrl 回跳结果解析
     * 通企付支付完成后会携带 mchNo/mchOrderNo/payOrderId/amount/state/sign/signType/reqTime 跳转
     * 前端拿到 query 参数透传到本接口，由后端验签并更新本地订单 state
     *
     * @param params jumpUrl query 参数（mchNo/mchOrderNo/payOrderId/amount/state/sign/signType/reqTime/...）
     * @return 最新本地订单实体（含 state=2/4/5）
     */
    @PostMapping("/parseJumpResult")
    public ResponseResult<H5CashOrderEntity> parseJumpResult(@RequestBody Map<String, String> params) {
        log.info("[H5Cash] parseJumpResult, mchOrderNo:{}, state:{}",
                params == null ? null : params.get("mchOrderNo"),
                params == null ? null : params.get("state"));
        return ResponseResult.success(h5CashService.parseJumpResult(params));
    }

    /**
     * 按商户订单号查询订单
     *
     * @param mchNo      商户号（必填）
     * @param mchOrderNo 商户订单号（必填）
     * @return 订单实体（不存在返回 null）
     */
    @GetMapping("/queryByMchOrderNo")
    public ResponseResult<H5CashOrderEntity> queryByMchOrderNo(@RequestParam(value = "mchNo", required = false) String mchNo,
                                                               @RequestParam("mchOrderNo") String mchOrderNo) {
        log.info("[H5Cash] queryByMchOrderNo, mchNo:{}, mchOrderNo:{}", mchNo, mchOrderNo);
        return ResponseResult.success(h5CashService.queryByMchOrderNo(mchNo, mchOrderNo));
    }
}
