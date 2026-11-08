package com.houjiang.payment.controller.api;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.entity.PayMemberProductEntity;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.service.api.MemberBizService;
import com.houjiang.payment.util.ApiTokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 自研会员业务 Controller（W4 P0）。
 *
 * 路径前缀 /api/payment/member/biz/，与历史 MemberController（通企付适配）物理隔离。
 *
 * 接口清单（4 个）：
 *   /info/query           会员聚合信息
 *   /enterprise/modify    企业会员信息修改
 *   /regStatus            注册状态查询
 *   /product/manage       会员产品 open/close/list
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/member/biz")
@RequiredArgsConstructor
public class MemberBizController {

    private final MemberBizService memberBizService;

    /** 1. 会员聚合信息 */
    @PostMapping("/info/query")
    public ResponseResult<Map<String, Object>> infoQuery(@RequestBody Map<String, Object> req) {
        try {
            return ResponseResult.success(memberBizService.infoQuery(req));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        } catch (Exception ex) {
            log.error("[member/biz/info] error", ex);
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    /** 2. 企业会员信息修改 */
    @PostMapping("/enterprise/modify")
    public ResponseResult<Boolean> enterpriseModify(@RequestBody Map<String, Object> req,
                                                    HttpServletRequest http) {
        String userId = ApiTokenUtils.resolveUserId(http);
        if (!StringUtils.hasText(userId)) return ResponseResult.fail(401, "用户未登录");
        try {
            return ResponseResult.success(memberBizService.enterpriseModify(req, userId));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    /** 3. 会员注册状态查询 */
    @PostMapping("/regStatus")
    public ResponseResult<Integer> regStatus(@RequestBody Map<String, Object> req) {
        try {
            return ResponseResult.success(memberBizService.regStatus(req));
        } catch (Exception ex) {
            log.error("[member/biz/regStatus] error", ex);
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    /** 4. 会员产品管理 */
    @PostMapping("/product/manage")
    public ResponseResult<List<PayMemberProductEntity>> productManage(@RequestBody Map<String, Object> req,
                                                                     HttpServletRequest http) {
        String userId = ApiTokenUtils.resolveUserId(http);
        if (!StringUtils.hasText(userId)) return ResponseResult.fail(401, "用户未登录");
        try {
            return ResponseResult.success(memberBizService.productManage(req, userId));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        }
    }
}
