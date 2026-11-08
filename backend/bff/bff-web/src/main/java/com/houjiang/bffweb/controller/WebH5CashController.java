package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.PaymentServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 通企付 H5 收银台 BFF 控制器
/**
 *
 * <p>路径前缀：/web/payment/h5cash，统一通过 PaymentServiceFeign 透传到 paas/payment 服务。
 * 对应 paas 路由：/api/payment/h5cash/*</p>
 *
 * @author Amos
 * @since 2026-05
 */
@Slf4j
@RestController
@RequestMapping("/web/payment/h5cash")
public class WebH5CashController {

    @Autowired
    private PaymentServiceFeign paymentServiceFeign;

    /**
     * 27. H5 收银台下单
/**
     * 前端通过本接口获取 H5Url，再跳转到通企付收银台
     *
     * @param param 下单参数（mchNo/mchOrderNo/amount/body/notifyUrl/jumpUrl/...）
     * @return 含 H5Url 的统一响应
     */
    @PostMapping("/getH5Url")
    public ResponseResult getH5Url(@RequestBody Map<String, Object> param) {
        log.info("[bff/h5cash] getH5Url mchOrderNo={}", param == null ? null : param.get("mchOrderNo"));
        return paymentServiceFeign.h5CashGetH5Url(param);
    }

    /**
     * jumpUrl 回跳参数解析
/**
     * 通企付支付完成后会携带 mchNo/mchOrderNo/payOrderId/amount/state/sign/signType/reqTime 跳转回前端，
     * 前端把 query 参数透传到本接口，由 paas 完成验签并更新本地订单 state
     *
     * @param params jumpUrl query 参数透传 Map
     * @return 最新本地订单实体（含 state）
     */
    @PostMapping("/parseJumpResult")
    public ResponseResult parseJumpResult(@RequestBody Map<String, String> params) {
        log.info("[bff/h5cash] parseJumpResult mchOrderNo={}, state={}",
                params == null ? null : params.get("mchOrderNo"),
                params == null ? null : params.get("state"));
        return paymentServiceFeign.h5CashParseJumpResult(params);
    }

    /**
     * 按商户订单号查询 H5 收银台订单
/**
     *
     * @param mchNo      商户号
     * @param mchOrderNo 商户订单号
     * @return 订单实体（不存在 data=null）
     */
    @GetMapping("/queryByMchOrderNo")
    public ResponseResult queryByMchOrderNo(@RequestParam(value = "mchNo", required = false) String mchNo,
                                            @RequestParam("mchOrderNo") String mchOrderNo) {
        log.info("[bff/h5cash] queryByMchOrderNo mchNo={}, mchOrderNo={}", mchNo, mchOrderNo);
        return paymentServiceFeign.h5CashQueryByMchOrderNo(mchNo, mchOrderNo);
    }
}
