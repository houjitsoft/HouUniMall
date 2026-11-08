package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.PaymentServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 自研会员 BFF 控制器（W4-T5）
/**
 *
 * <p>路径前缀：/web/pay/member/biz，对应 paas/payment 的 /api/payment/member/biz/*</p>
 *
 * @author Amos
 * @since 2026-05
 */
@Slf4j
@RestController
@RequestMapping("/web/pay/member/biz")
public class WebMemberBizController {

    @Autowired
    private PaymentServiceFeign paymentServiceFeign;

    /** 会员信息聚合查询（账户+商户+风控+产品） */
    @PostMapping("/info/query")
    public ResponseResult infoQuery(@RequestBody Map<String, Object> param) {
        log.info("[bff/memberBiz] info/query memberNo={}", param == null ? null : param.get("memberNo"));
        return paymentServiceFeign.memberBizInfoQuery(param);
    }

    /** 会员企业信息修改（联动商户实体字段） */
    @PostMapping("/enterprise/modify")
    public ResponseResult enterpriseModify(@RequestBody Map<String, Object> param) {
        log.info("[bff/memberBiz] enterprise/modify memberNo={}", param == null ? null : param.get("memberNo"));
        return paymentServiceFeign.memberBizEnterpriseModify(param);
    }

    /** 会员注册状态查询 */
    @PostMapping("/regStatus")
    public ResponseResult regStatus(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.memberBizRegStatus(param);
    }

    /** 会员产品管理（action=open/close/list） */
    @PostMapping("/product/manage")
    public ResponseResult productManage(@RequestBody Map<String, Object> param) {
        log.info("[bff/memberBiz] product/manage action={}", param == null ? null : param.get("action"));
        return paymentServiceFeign.memberBizProductManage(param);
    }
}
