package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.PaymentServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 支付服务Web控制器
/**
 * 
 * @author Amos
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/web/pay")
public class WebPayController {

    @Autowired
    private PaymentServiceFeign paymentServiceFeign;

    // ================= 支付管理接口 =================

    /**
     * 创建支付订单
     */
    @PostMapping("/create")
    public ResponseResult createPayment(@RequestBody Map<String, Object> paymentData) {
        return paymentServiceFeign.createPayment(paymentData);
    }

    /**
     * 查询支付订单
     * <p>透传支付中心后台 PayAdminQueryController#tradeDetail（按支付单号/订单号）</p>
     */
    @GetMapping("/detail/{id}")
    public ResponseResult getPaymentDetail(@PathVariable("id") String id) {
        Map<String, Object> param = new java.util.HashMap<>();
        param.put("consumeTradeId", id);
        return paymentServiceFeign.adminTradeDetail(param);
    }

    /**
     * 支付订单详情（按支付单号/订单号，POST）
     * <p>透传支付中心后台 PayAdminQueryController#tradeDetail</p>
     */
    @PostMapping("/detail")
    public ResponseResult paymentDetail(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.adminTradeDetail(param == null ? new java.util.HashMap<>() : param);
    }

    /**
     * 查询支付列表
     * <p>透传支付中心后台 PayAdminQueryController#tradePage（走 hou_pay_consume_trade 真实数据）</p>
     */
    @PostMapping("/list")
    public ResponseResult queryPaymentList(@RequestBody(required = false) Map<String, Object> param) {
        return paymentServiceFeign.adminTradePage(param == null ? new java.util.HashMap<>() : param);
    }

    /**
     * 发起支付
     */
    @PostMapping("/initiate")
    public ResponseResult initiatePayment(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.initiatePayment(param);
    }

    /**
     * 查询支付状态
     */
    @GetMapping("/status/{orderNo}")
    public ResponseResult getPaymentStatus(@PathVariable("orderNo") String orderNo) {
        return paymentServiceFeign.getPaymentStatus(orderNo);
    }

    /**
     * 关闭支付订单
     */
    @PostMapping("/close")
    public ResponseResult closePayment(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.closePayment(param);
    }

    // ================= 退款管理接口=================

    /**
     * 创建退款订单
     */
    @PostMapping("/refund/create")
    public ResponseResult createRefund(@RequestBody Map<String, Object> refundData) {
        return paymentServiceFeign.createRefund(refundData);
    }

    /**
     * 查询退款订单
     * <p>透传支付中心后台 PayAdminQueryController#refundPage 按退款单号过滤</p>
     */
    @GetMapping("/refund/detail/{id}")
    public ResponseResult getRefundDetail(@PathVariable("id") String id) {
        Map<String, Object> param = new java.util.HashMap<>();
        param.put("refundNo", id);
        param.put("pageNum", 1);
        param.put("pageSize", 1);
        return paymentServiceFeign.adminRefundPage(param);
    }

    /**
     * 查询退款列表
     * <p>透传支付中心后台 PayAdminQueryController#refundPage（hou_pay_refund_order 真实数据）</p>
     */
    @PostMapping("/refund/list")
    public ResponseResult queryRefundList(@RequestBody(required = false) Map<String, Object> param) {
        return paymentServiceFeign.adminRefundPage(param == null ? new java.util.HashMap<>() : param);
    }

    /**
     * 发起退款
     */
    @PostMapping("/refund/initiate")
    public ResponseResult initiateRefund(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.initiateRefund(param);
    }

    /**
     * 查询退款状态
     */
    @GetMapping("/refund/status/{refundNo}")
    public ResponseResult getRefundStatus(@PathVariable("refundNo") String refundNo) {
        return paymentServiceFeign.getRefundStatus(refundNo);
    }

    /**
     * uniapp 退款申请别名端点（请求体同 /refund/create）
     */
    @PostMapping("/refund/apply")
    public ResponseResult refundApply(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.createRefund(param);
    }

    /**
     * uniapp 按 refundNo 查询退款详情别名端点。
/**
     * 复用 /refund/list 接口按 refundNo 精准过滤，取首条返回，
     * 避免为移动端重复增加下游接口。
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/refund/detail")
    public ResponseResult refundDetailByNo(@RequestBody Map<String, Object> param) {
        if (param == null || param.get("refundNo") == null) {
            return ResponseResult.fail(400, "refundNo 不能为空");
        }
        Map<String, Object> queryParam = new java.util.HashMap<>();
        queryParam.put("refundNo", param.get("refundNo"));
        queryParam.put("page", 1);
        queryParam.put("size", 1);
        ResponseResult listRes = paymentServiceFeign.queryRefundList(queryParam);
        if (listRes == null || listRes.getData() == null) {
            return listRes;
        }
        Object data = listRes.getData();
        if (data instanceof Map) {
            Object records = ((Map<String, Object>) data).get("records");
            if (records instanceof java.util.List && !((java.util.List<?>) records).isEmpty()) {
                return ResponseResult.success(((java.util.List<?>) records).get(0));
            }
        } else if (data instanceof java.util.List && !((java.util.List<?>) data).isEmpty()) {
            return ResponseResult.success(((java.util.List<?>) data).get(0));
        }
        return ResponseResult.fail(404, "退款单不存在");
    }

    // ================= 账户管理接口 =================

    /**
     * 查询账户余额
     */
    @GetMapping("/account/balance/{userId}")
    public ResponseResult getAccountBalance(@PathVariable("userId") String userId) {
        // 调用 payment 服务的管理端余额分页，按 userId 过滤
        Map<String, Object> param = new java.util.HashMap<>();
        param.put("userId", userId);
        param.put("pageNum", 1);
        param.put("pageSize", 1);
        return paymentServiceFeign.adminBalancePage(param);
    }

    /**
     * 查询交易记录
     */
    @PostMapping("/account/records")
    public ResponseResult getAccountRecords(@RequestBody Map<String, Object> param) {
        // 调用 payment 服务的管理端资金流水分页（hou_pay_consume_record）
        return paymentServiceFeign.adminRecordPage(param);
    }

    /**
     * 查询资产明细
     */
    @PostMapping("/account/details")
    public ResponseResult getAssetDetails(@RequestBody Map<String, Object> param) {
        // 调用 payment 服务的管理端资金流水分页（hou_pay_consume_record）作为账户明细
        return paymentServiceFeign.adminRecordPage(param);
    }

    // ================= 充值管理接口=================

    /**
     * 创建充值订单
     */
    @PostMapping("/recharge/create")
    public ResponseResult createRecharge(@RequestBody Map<String, Object> rechargeData) {
        // 调用 payment 服务的管理端充值创建（hou_pay_consume_deposit / 账户充值）
        return paymentServiceFeign.walletRecharge(rechargeData);
    }

    /**
     * 查询充值记录
     */
    @PostMapping("/recharge/list")
    public ResponseResult getRechargeList(@RequestBody Map<String, Object> param) {
        // 调用 payment 服务的管理端充值记录分页（hou_pay_consume_deposit）
        return paymentServiceFeign.adminDepositPage(param);
    }

    /**
     * 查询充值详情
     */
    @PostMapping("/recharge/detail")
    public ResponseResult getRechargeDetail(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.adminDepositDetail(param);
    }

    // ================= 提现管理接口 =================

    /**
     * 申请提现
     */
    @PostMapping("/withdraw/apply")
    public ResponseResult applyWithdraw(@RequestBody Map<String, Object> withdrawData) {
        return wrapDtoResponse(paymentServiceFeign.withdrawApply(withdrawData));
    }

    /**
     * 查询提现记录
     */
    @PostMapping("/withdraw/list")
    public ResponseResult getWithdrawList(@RequestBody Map<String, Object> param) {
        // 对齐支付中心后台真实分页：hou_pay_consume_withdraw
        return paymentServiceFeign.adminWithdrawPage(param == null ? new java.util.HashMap<>() : param);
    }

    /**
     * 查询提现详情
     */
    @PostMapping("/withdraw/detail")
    public ResponseResult getWithdrawDetail(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.adminWithdrawDetail(param);
    }

    // ================= 支付配置接口（统一对齐到 ChannelController，自研对标实现）=================
    // 历史 PaymentServiceFeign 中 /payment/config/methods、/payment/config/query、/payment/config/update
    // 是早期占位 stub，payment 模块从未实现。现在统一转发到 ChannelController（hou_pay_channel 表），
    // 与「通企付参考蓝本→自研对标实现」的支付架构方向保持一致。

    /**
     * 查询启用的支付方式（公开版渠道列表，C 端无需登录鉴权也可读，仅返回启用 + 公开字段）。
/**
     * <p>对齐到 ChannelController#channelConfigPublic → POST /api/payment/channel/channel/config/public。
     * 入参 payMethod 可选过滤（WECHAT/ALIPAY/UNIONPAY/...）。</p>
     */
    @GetMapping("/config/methods")
    public ResponseResult getPaymentMethods(@RequestParam(value = "payMethod", required = false) String payMethod) {
        Map<String, Object> body = new java.util.HashMap<>();
        if (payMethod != null && !payMethod.isEmpty()) {
            body.put("payMethod", payMethod);
        }
        return paymentServiceFeign.channelConfigPublic(body);
    }

    /**
     * 查询支付配置（自研对标实现）
/**
     * <p>历史 Feign 端点 /payment/config/query 在 payment 模块未实现（早期 stub），
     * 现统一对齐到 ChannelController#channelConfigList → /api/payment/channel/channel/config/list，
     * 使用 hou_pay_channel 表的脱敏分页查询作为「支付配置列表」唯一数据源。
     * 与「通企付参考蓝本→自研对标实现」的支付架构方向保持一致。</p>
     */
    @PostMapping("/config/list")
    public ResponseResult queryPaymentConfig(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.channelConfigList(param);
    }

    /**
     * 新增/更新支付配置（凭证含敏感字段会自动加密落库）。
/**
     * <p>对齐到 ChannelController#channelConfigSave → POST /api/payment/channel/channel/config/save。
     * 请求体支持：id（编辑时传）、channelCode、channelName、provider、payMethod、merchantNo、status、remark、configMap。</p>
     */
    @PostMapping("/config/update")
    public ResponseResult updatePaymentConfig(@RequestBody Map<String, Object> configData) {
        return paymentServiceFeign.channelConfigSave(configData);
    }

    /**
     * 启用/停用支付配置。
/**
     * <p>对齐到 ChannelController#channelConfigToggle → POST /api/payment/channel/channel/config/toggle。
     * 请求体：id、status（0 停用 / 1 启用）。</p>
     */
    @PostMapping("/config/toggle")
    public ResponseResult togglePaymentConfig(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.channelConfigToggle(param);
    }

    /**
     * 删除支付配置（逆向逻辑券证/渠道）。
/**
     * <p>对齐到 ChannelController#channelDelete → POST /api/payment/channel/channel/delete。
     * 请求体：id。</p>
     */
    @PostMapping("/config/delete")
    public ResponseResult deletePaymentConfig(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.channelDelete(param);
    }

    // ================= 统一收银台（Task 0 新增） =================

    /**
     * 收银台下单
     */
    @PostMapping("/cashier/create")
    public ResponseResult cashierCreate(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.cashierCreate(param);
    }

    /**
     * 查询收银台订单状态
/**
     * <p>处于后台主要被 C 端 uniapp 收银台调用，额外叠加 statusCode 字段输出
     * SUCCESS/PENDING/FAIL/CLOSED/REFUND 映射，便于前端状态机判断。</p>
     */
    @PostMapping("/cashier/status")
    public ResponseResult cashierStatus(@RequestBody Map<String, Object> param) {
        ResponseResult result = paymentServiceFeign.cashierStatus(param);
        enrichCashierStatusCode(result);
        return result;
    }

    /**
     * 关闭收银台订单
     */
    @PostMapping("/cashier/close")
    public ResponseResult cashierClose(@RequestBody Map<String, Object> param) {
        ResponseResult result = paymentServiceFeign.cashierClose(param);
        enrichCashierStatusCode(result);
        return result;
    }

    /**
     * 在 cashier/status 、cashier/close 返回体中，为前端额外补充 statusCode 字段。
/**
     * 0/1 → PENDING；2 → SUCCESS；3 → FAIL；4 → CLOSED；5 → REFUND。
     */
    @SuppressWarnings("unchecked")
    private void enrichCashierStatusCode(ResponseResult result) {
        if (result == null || result.getData() == null) return;
        Object data = result.getData();
        if (!(data instanceof Map)) return;
        Map<String, Object> map = (Map<String, Object>) data;
        Object statusObj = map.get("status");
        if (statusObj == null) return;
        int code;
        try {
            code = Integer.parseInt(String.valueOf(statusObj));
        } catch (NumberFormatException ex) {
            return;
        }
        String statusCode;
        switch (code) {
            case 2: statusCode = "SUCCESS"; break;
            case 3: statusCode = "FAIL";    break;
            case 4: statusCode = "CLOSED";  break;
            case 5: statusCode = "REFUND";  break;
            case 0:
            case 1:
            default: statusCode = "PENDING"; break;
        }
        map.put("statusCode", statusCode);
    }

    // ================= 收银台订单（Task 0 新增） =================

    /**
     * 收银台订单列表
     */
    @PostMapping("/order/list")
    public ResponseResult cashierOrderList(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.cashierOrderList(param);
    }

    /**
     * 收银台订单详情
/**
     * 前端 POST body: {orderNo}；payment 下游为 GET /detail/{cashierNo}
     */
    @PostMapping("/order/detail")
    public ResponseResult cashierOrderDetail(@RequestBody Map<String, Object> param) {
        Object key = param == null ? null : param.get("orderNo");
        if (key == null && param != null) {
            key = param.get("cashierNo");
        }
        if (key == null) {
            return ResponseResult.fail(500, "orderNo 必填");
        }
        return paymentServiceFeign.cashierOrderDetail(key.toString());
    }

    // ================= 钱包（Task 0 新增） =================

    /**
     * 钱包信息
     */
    @PostMapping("/wallet/info")
    public ResponseResult walletInfo() {
        return paymentServiceFeign.walletInfo();
    }

    /**
     * 钱包交易流水
     */
    @PostMapping("/wallet/transactions")
    public ResponseResult walletTransactions(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.walletTransactions(param);
    }

    /**
     * 钱包充值
     */
    @PostMapping("/wallet/recharge")
    public ResponseResult walletRecharge(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.walletRecharge(param);
    }

    /**
     * 钱包提现
     */
    @PostMapping("/wallet/withdraw")
    public ResponseResult walletWithdraw(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.walletWithdraw(param);
    }

    // ================= 银行卡（Task 0 新增） =================

    /**
     * 银行卡列表
     */
    @PostMapping("/bankcard/list")
    public ResponseResult bankcardList(@RequestBody(required = false) Map<String, Object> param) {
        return paymentServiceFeign.bankcardList(param == null ? new java.util.HashMap<>() : param);
    }

    /**
     * 绑定银行卡
     */
    @PostMapping("/bankcard/add")
    public ResponseResult bankcardAdd(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.bankcardAdd(param);
    }

    /**
     * 解绑银行卡
     */
    @PostMapping("/bankcard/delete")
    public ResponseResult bankcardDelete(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.bankcardDelete(param);
    }

    // ================= 支付安全（Task 0 新增） =================

    /**
     * 设置支付密码
     */
    @PostMapping("/security/setPassword")
    public ResponseResult securitySetPassword(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.securitySetPassword(param);
    }

    /**
     * 校验支付密码
     */
    @PostMapping("/security/verifyPassword")
    public ResponseResult securityVerifyPassword(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.securityVerifyPassword(param);
    }

    /**
     * 修改支付密码
     */
    @PostMapping("/security/modifyPassword")
    public ResponseResult securityModifyPassword(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.securityModifyPassword(param);
    }

    /**
     * 实名认证提交
     */
    @PostMapping("/security/realName")
    public ResponseResult securityRealName(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.securityRealName(param);
    }

    /**
     * 查询实名认证状态
/**
     * 前端 POST 不带 body；payment 提供 POST /realNameStatus 从 token 解析
     */
    @PostMapping("/security/realNameStatus")
    public ResponseResult securityRealNameStatus(@RequestBody(required = false) Map<String, Object> param) {
        return paymentServiceFeign.securityRealNameStatusByToken(
                param == null ? new java.util.HashMap<>() : param);
    }

    // ================= 短信（Task 0 新增） =================

    /**
     * 发送短信验证码
     */
    @PostMapping("/sms/send")
    public ResponseResult smsSend(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.smsSend(param);
    }

    /**
     * 校验短信验证码
     */
    @PostMapping("/sms/verify")
    public ResponseResult smsVerify(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.smsVerify(param);
    }

    // ================= 平台商户管理（W1 P0 新增） =================

    /**
     * 商户进件
     */
    @PostMapping("/merchant/reg")
    public ResponseResult merchantReg(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.merchantReg(param);
    }

    /**
     * 商户列表（分页）
     */
    @PostMapping("/merchant/list")
    public ResponseResult merchantList(@RequestBody(required = false) Map<String, Object> param) {
        return paymentServiceFeign.merchantList(param == null ? new java.util.HashMap<>() : param);
    }

    /**
     * 商户详情
     */
    @PostMapping("/merchant/detail")
    public ResponseResult merchantDetail(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.merchantDetail(param);
    }

    /**
     * 商户信息更新
     */
    @PostMapping("/merchant/update")
    public ResponseResult merchantUpdate(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.merchantUpdate(param);
    }

    /**
     * 商户状态变更（审核/冻结/解冻/注销）
     */
    @PostMapping("/merchant/changeStatus")
    public ResponseResult merchantChangeStatus(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.merchantChangeStatus(param);
    }

    /**
     * 商户费率配置
     */
    @PostMapping("/merchant/configFee")
    public ResponseResult merchantConfigFee(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.merchantConfigFee(param);
    }

    /**
     * 商户结算账户更新
     */
    @PostMapping("/merchant/updateAccount")
    public ResponseResult merchantUpdateAccount(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.merchantUpdateAccount(param);
    }

    /**
     * 商户产品开通/停用
     */
    @PostMapping("/merchant/openProduct")
    public ResponseResult merchantOpenProduct(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.merchantOpenProduct(param);
    }

    // ================= 支付风控（W2 P0 新增） =================

    /**
     * 风控规则-创建
     */
    @PostMapping("/risk/rule/create")
    public ResponseResult riskRuleCreate(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.riskRuleCreate(param);
    }

    /**
     * 风控规则-更新
     */
    @PostMapping("/risk/rule/update")
    public ResponseResult riskRuleUpdate(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.riskRuleUpdate(param);
    }

    /**
     * 风控规则-启停
     */
    @PostMapping("/risk/rule/changeStatus")
    public ResponseResult riskRuleChangeStatus(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.riskRuleChangeStatus(param);
    }

    /**
     * 风控规则-删除（逻辑删除）
     */
    @PostMapping("/risk/rule/delete")
    public ResponseResult riskRuleDelete(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.riskRuleDelete(param);
    }

    /**
     * 风控规则-分页列表
     */
    @PostMapping("/risk/rule/list")
    public ResponseResult riskRuleList(@RequestBody(required = false) Map<String, Object> param) {
        return paymentServiceFeign.riskRuleList(param == null ? new java.util.HashMap<>() : param);
    }

    /**
     * 风控规则-详情
     */
    @PostMapping("/risk/rule/detail")
    public ResponseResult riskRuleDetail(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.riskRuleDetail(param);
    }

    /**
     * 风控日志-分页列表
     */
    @PostMapping("/risk/log/list")
    public ResponseResult riskLogList(@RequestBody(required = false) Map<String, Object> param) {
        return paymentServiceFeign.riskLogList(param == null ? new java.util.HashMap<>() : param);
    }

    /**
     * 风控日志-详情
     */
    @PostMapping("/risk/log/detail")
    public ResponseResult riskLogDetail(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.riskLogDetail(param);
    }

    /**
     * 风控日志-人工审核
     */
    @PostMapping("/risk/log/audit")
    public ResponseResult riskLogAudit(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.riskLogAudit(param);
    }

    /**
     * 黑白名单-新增
     */
    @PostMapping("/risk/blacklist/add")
    public ResponseResult riskBlacklistAdd(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.riskBlacklistAdd(param);
    }

    /**
     * 黑白名单-分页列表
     */
    @PostMapping("/risk/blacklist/list")
    public ResponseResult riskBlacklistList(@RequestBody(required = false) Map<String, Object> param) {
        return paymentServiceFeign.riskBlacklistList(param == null ? new java.util.HashMap<>() : param);
    }

    /**
     * 黑白名单-移除
     */
    @PostMapping("/risk/blacklist/remove")
    public ResponseResult riskBlacklistRemove(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.riskBlacklistRemove(param);
    }

    /**
     * 风控评估核心入口（在支付/退款/提现前调用）
     */
    @PostMapping("/risk/check")
    public ResponseResult riskCheck(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.riskCheck(param);
    }

    // ================= 渠道路由（W3 P0 新增） =================

    /** 渠道配置-保存 */
    @PostMapping("/channel/channel/save")
    public ResponseResult channelSave(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.channelSave(param);
    }

    /** 渠道配置-状态变更 */
    @PostMapping("/channel/channel/changeStatus")
    public ResponseResult channelChangeStatus(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.channelChangeStatus(param);
    }

    /** 渠道配置-删除 */
    @PostMapping("/channel/channel/delete")
    public ResponseResult channelDelete(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.channelDelete(param);
    }

    /** 渠道配置-分页 */
    @PostMapping("/channel/channel/list")
    public ResponseResult channelList(@RequestBody(required = false) Map<String, Object> param) {
        return paymentServiceFeign.channelList(param == null ? new java.util.HashMap<>() : param);
    }

    /** 渠道配置-详情 */
    @PostMapping("/channel/channel/detail")
    public ResponseResult channelDetail(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.channelDetail(param);
    }

    /** 渠道配置-编码查询 */
    @PostMapping("/channel/channel/getByCode")
    public ResponseResult channelGetByCode(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.channelGetByCode(param);
    }

    /** 路由规则-保存 */
    @PostMapping("/channel/route/save")
    public ResponseResult channelRouteSave(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.channelRouteSave(param);
    }

    /** 路由规则-状态变更 */
    @PostMapping("/channel/route/changeStatus")
    public ResponseResult channelRouteChangeStatus(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.channelRouteChangeStatus(param);
    }

    /** 路由规则-删除 */
    @PostMapping("/channel/route/delete")
    public ResponseResult channelRouteDelete(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.channelRouteDelete(param);
    }

    /** 路由规则-分页 */
    @PostMapping("/channel/route/list")
    public ResponseResult channelRouteList(@RequestBody(required = false) Map<String, Object> param) {
        return paymentServiceFeign.channelRouteList(param == null ? new java.util.HashMap<>() : param);
    }

    /** 路由规则-详情 */
    @PostMapping("/channel/route/detail")
    public ResponseResult channelRouteDetail(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.channelRouteDetail(param);
    }

    /** 路由选择核心入口（风控通过后调用，返回目标渠道与 logNo） */
    @PostMapping("/channel/route/select")
    public ResponseResult channelRouteSelect(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.channelRouteSelect(param);
    }

    /**
     * 退款渠道路由选择（W5+ P2-1）
/**
     * <p>请求体须携带 originChannelCode；bizType 会被PAAS 强制覆盖为 2</p>
     */
    @PostMapping("/channel/route/selectRefund")
    public ResponseResult channelRouteSelectRefund(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.channelRouteSelectRefund(param);
    }

    /** 渠道调用结果上报（用于更新健康度与熔断状态） */
    @PostMapping("/channel/route/report")
    public ResponseResult channelRouteReport(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.channelRouteReport(param);
    }

    /** 渠道调用日志-分页 */
    @PostMapping("/channel/log/list")
    public ResponseResult channelLogList(@RequestBody(required = false) Map<String, Object> param) {
        return paymentServiceFeign.channelLogList(param == null ? new java.util.HashMap<>() : param);
    }

    /** 渠道调用日志-详情 */
    @PostMapping("/channel/log/detail")
    public ResponseResult channelLogDetail(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.channelLogDetail(param);
    }

    /** 补偿任务-注册（掉单时挂单，由定时任务反查） */
    @PostMapping("/channel/compensate/register")
    public ResponseResult channelCompensateRegister(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.channelCompensateRegister(param);
    }

    /** 补偿任务-手动触发单条 */
    @PostMapping("/channel/compensate/executeOne")
    public ResponseResult channelCompensateExecuteOne(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.channelCompensateExecuteOne(param);
    }

    /** 补偿任务-批量到期执行（供定时任务调用） */
    @PostMapping("/channel/compensate/executeDue")
    public ResponseResult channelCompensateExecuteDue(@RequestBody(required = false) Map<String, Object> param) {
        return paymentServiceFeign.channelCompensateExecuteDue(param == null ? new java.util.HashMap<>() : param);
    }

    /** 补偿任务-分页 */
    @PostMapping("/channel/compensate/list")
    public ResponseResult channelCompensateList(@RequestBody(required = false) Map<String, Object> param) {
        return paymentServiceFeign.channelCompensateList(param == null ? new java.util.HashMap<>() : param);
    }

    /** 补偿任务-详情 */
    @PostMapping("/channel/compensate/detail")
    public ResponseResult channelCompensateDetail(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.channelCompensateDetail(param);
    }

    /** 补偿任务-放弃 */
    @PostMapping("/channel/compensate/giveUp")
    public ResponseResult channelCompensateGiveUp(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.channelCompensateGiveUp(param);
    }

    // ================= W4 商户补全（T4 新增） =================

    /** 个人/个体工商户进件 */
    @PostMapping("/merchant/regPerson")
    public ResponseResult merchantRegPerson(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.merchantRegPerson(param);
    }

    /** 商户资质补充 */
    @PostMapping("/merchant/qualSupplement")
    public ResponseResult merchantQualSupplement(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.merchantQualSupplement(param);
    }

    /** 商户子账号列表 */
    @PostMapping("/merchant/subAccount/list")
    public ResponseResult merchantSubAccountList(@RequestBody(required = false) Map<String, Object> param) {
        return paymentServiceFeign.merchantSubAccountList(param == null ? new java.util.HashMap<>() : param);
    }

    // ================= W4 风控人工审核（T7 新增） =================

    /** 风控日志提交人工审核 */
    @PostMapping("/risk/log/submitReview")
    public ResponseResult riskLogSubmitReview(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.riskLogSubmitReview(param);
    }

    /** 风控日志人工审核通过/驳回 */
    @PostMapping("/risk/log/approve")
    public ResponseResult riskLogApprove(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.riskLogApprove(param);
    }

    // ================= 渠道凭证管理（W5 新增） =================

    /** 凭证列表（脱敏） */
    @PostMapping("/channel/config/list")
    public ResponseResult channelConfigList(@RequestBody(required = false) Map<String, Object> param) {
        return paymentServiceFeign.channelConfigList(param == null ? new java.util.HashMap<>() : param);
    }

    /** 凭证详情（解密） */
    @PostMapping("/channel/config/get")
    public ResponseResult channelConfigGet(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.channelConfigGet(param);
    }

    /** 保存凭证（加密落库） */
    @PostMapping("/channel/config/save")
    public ResponseResult channelConfigSave(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.channelConfigSave(param);
    }

    /** 凭证启用/停用 */
    @PostMapping("/channel/config/toggle")
    public ResponseResult channelConfigToggle(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.channelConfigToggle(param);
    }

    /** 连通性测试 */
    @PostMapping("/channel/config/testConnect")
    public ResponseResult channelConfigTestConnect(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.channelConfigTestConnect(param);
    }

    /**
     * 公开版渠道配置（C 端 uniapp 收银台读取启用渠道 + 公开字段）
/**
     * 入参可选 payMethod 进行过滤；不传返回所有启用渠道
     */
    @PostMapping("/config/public")
    public ResponseResult configPublic(@RequestBody(required = false) Map<String, Object> param) {
        return paymentServiceFeign.channelConfigPublic(param == null ? new java.util.HashMap<>() : param);
    }

    /**
     * 钱包余额扣款（C 端收银台真实扣款入口，必传 cashierNo + payPassword）
/**
     * 内部会校验支付密码后复用 CashierServiceImpl.handleBalancePay 完成扣款
     */
    @PostMapping("/wallet/pay")
    public ResponseResult walletPay(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.walletPay(param);
    }

    // ================= 交易管理（TradeController 透传，/web/pay/trade/*） =================

    /** 统一下单（支付管理后台交易下单） */
    @PostMapping("/trade/unifiedOrder")
    public ResponseResult tradeUnifiedOrder(@RequestBody Map<String, Object> param) {
        return wrapDtoResponse(paymentServiceFeign.tradeUnifiedOrder(param));
    }

    /** 统一订单查询 */
    @PostMapping("/trade/queryOrder")
    public ResponseResult tradeQueryOrder(@RequestBody Map<String, Object> param) {
        return wrapDtoResponse(paymentServiceFeign.tradeQueryOrder(param));
    }

    /** 关闭支付订单 */
    @PostMapping("/trade/closeOrder")
    public ResponseResult tradeCloseOrder(@RequestBody Map<String, Object> param) {
        return wrapDtoResponse(paymentServiceFeign.tradeCloseOrder(param));
    }

    /** 统一退款 */
    @PostMapping("/trade/refund")
    public ResponseResult tradeRefund(@RequestBody Map<String, Object> param) {
        return wrapDtoResponse(paymentServiceFeign.tradeRefund(param));
    }

    /** 退款查询 */
    @PostMapping("/trade/queryRefund")
    public ResponseResult tradeQueryRefund(@RequestBody Map<String, Object> param) {
        return wrapDtoResponse(paymentServiceFeign.tradeQueryRefund(param));
    }

    // ================= 会员管理（MemberController 透传，/web/pay/member/*） =================

    /** 企业会员实名开户 */
    @PostMapping("/member/regEnterprise")
    public ResponseResult memberRegEnterprise(@RequestBody Map<String, Object> param) {
        return wrapDtoResponse(paymentServiceFeign.memberRegEnterprise(param));
    }

    /** 个人会员实名及绑卡 */
    @PostMapping("/member/regPerson")
    public ResponseResult memberRegPerson(@RequestBody Map<String, Object> param) {
        return wrapDtoResponse(paymentServiceFeign.memberRegPerson(param));
    }

    /** 会员资料补录 */
    @PostMapping("/member/regSupplement")
    public ResponseResult memberRegSupplement(@RequestBody Map<String, Object> param) {
        return wrapDtoResponse(paymentServiceFeign.memberRegSupplement(param));
    }

    /** 会员信息查询（聚合，走 MemberBiz info/query） */
    @PostMapping("/member/query")
    public ResponseResult memberQuery(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.memberBizInfoQuery(param);
    }

    // ================= 分账管理（DivisionController 透传，/web/pay/division/*） =================

    /** 批量分账 */
    @PostMapping("/division/batch")
    public ResponseResult divisionBatch(@RequestBody Map<String, Object> param) {
        return wrapDtoResponse(paymentServiceFeign.divisionBatch(param));
    }

    /** 分账查询 */
    @PostMapping("/division/query")
    public ResponseResult divisionQuery(@RequestBody Map<String, Object> param) {
        return wrapDtoResponse(paymentServiceFeign.divisionQuery(param));
    }

    /** 分账撤销 */
    @PostMapping("/division/refund")
    public ResponseResult divisionRefund(@RequestBody Map<String, Object> param) {
        return wrapDtoResponse(paymentServiceFeign.divisionRefund(param));
    }

    // ================= 商户进件 / 对账 / 账单 / 余额 / 提现 =================

    /** 商户进件（通企付 MchController） */
    @PostMapping("/mch/reg")
    public ResponseResult mchReg(@RequestBody Map<String, Object> param) {
        return wrapDtoResponse(paymentServiceFeign.mchReg(param));
    }

    /** 商户列表（平台自研商户列表） */
    @PostMapping("/mch/list")
    public ResponseResult mchList(@RequestBody(required = false) Map<String, Object> param) {
        return paymentServiceFeign.merchantList(param == null ? new java.util.HashMap<>() : param);
    }

    /** 对账文件下载 */
    @PostMapping("/statement/download")
    public ResponseResult statementDownload(@RequestBody Map<String, Object> param) {
        return wrapDtoResponse(paymentServiceFeign.statementDownload(param));
    }

    /** 账单导入 */
    @PostMapping("/bill/import")
    public ResponseResult billImport(@RequestBody Map<String, Object> param) {
        return wrapDtoResponse(paymentServiceFeign.billImport(param));
    }

    /** 统一余额查询 */
    @PostMapping("/balance/query")
    public ResponseResult balanceQuery(@RequestBody Map<String, Object> param) {
        return wrapDtoResponse(paymentServiceFeign.balanceQuery(param));
    }

    /**
     * 将支付服务 DTO 响应统一包裹为 ResponseResult。
     * <p>通企付对标 DTO（UnifiedOrderResponse 等）自带 code/msg 字段（"0" 表示成功），
     * 若直接作为 Object 返回会被前端误判；这里统一转换为 {code:200,message,data} 结构。</p>
     */
    @SuppressWarnings("unchecked")
    private ResponseResult wrapDtoResponse(Object value) {
        if (value == null) {
            return ResponseResult.fail(500, "支付服务无响应");
        }
        // 已是统一响应（如熔断 fallback 返回的 ResponseResult）
        if (value instanceof ResponseResult) {
            return (ResponseResult) value;
        }
        if (value instanceof Map) {
            Map<String, Object> m = (Map<String, Object>) value;
            Object code = m.get("code");
            Object msg = m.get("msg");
            boolean isOk = code == null || "0".equals(String.valueOf(code)) || "200".equals(String.valueOf(code));
            if (isOk) {
                return ResponseResult.success(m);
            }
            return ResponseResult.fail(500, msg == null ? "支付服务处理失败" : String.valueOf(msg));
        }
        return ResponseResult.success(value);
    }
}