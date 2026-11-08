package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 支付服务Feign熔断器
/**
 * 
 * @description 支付服务降级处理
 * @author Amos
 * @date 2024-12-20
 */
@Slf4j
@Component
public class PaymentServiceFeignFallback implements PaymentServiceFeign {

    private static final String FALLBACK_MESSAGE = "支付服务暂时不可用，请稍后重试";

    @Override
    public ResponseResult callPaymentAPI(String path) {
        log.error("支付服务调用失败，path: {}", path);
        return ResponseResult.fail(FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult callPaymentAPIWithBody(String path, Map<String, Object> params) {
        log.error("支付服务调用失败，path: {}", path);
        return ResponseResult.fail(FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult createPayment(Map<String, Object> paymentData) {
        log.error("创建支付订单失败");
        return ResponseResult.fail(FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult getPaymentDetail(Long id) {
        log.error("查询支付订单失败，id: {}", id);
        return ResponseResult.fail(FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult queryPaymentList(Map<String, Object> param) {
        log.error("查询支付列表失败");
        return ResponseResult.fail(FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult initiatePayment(Map<String, Object> param) {
        log.error("发起支付失败");
        return ResponseResult.fail(FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult paymentCallback(Map<String, Object> callbackData) {
        log.error("支付回调处理失败");
        return ResponseResult.fail(FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult getPaymentStatus(String orderNo) {
        log.error("查询支付状态失败，orderNo: {}", orderNo);
        return ResponseResult.fail(FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult closePayment(Map<String, Object> param) {
        log.error("关闭支付订单失败");
        return ResponseResult.fail(FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult createRefund(Map<String, Object> refundData) {
        log.error("创建退款订单失败");
        return ResponseResult.fail(FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult getRefundDetail(Long id) {
        log.error("查询退款订单失败，id: {}", id);
        return ResponseResult.fail(FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult queryRefundList(Map<String, Object> param) {
        log.error("查询退款列表失败");
        return ResponseResult.fail(FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult initiateRefund(Map<String, Object> param) {
        log.error("发起退款失败");
        return ResponseResult.fail(FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult refundCallback(Map<String, Object> callbackData) {
        log.error("退款回调处理失败");
        return ResponseResult.fail(FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult getRefundStatus(String refundNo) {
        log.error("查询退款状态失败，refundNo: {}", refundNo);
        return ResponseResult.fail(FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult withdrawDepositList(Map<String, Object> param) {
        log.error("查询提现订单列表失败");
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult getPaymentMethods() {
        log.error("查询支付方式失败");
        return ResponseResult.fail(FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult queryPaymentConfig(Map<String, Object> param) {
        log.error("查询支付配置失败");
        return ResponseResult.fail(FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult updatePaymentConfig(Map<String, Object> configData) {
        log.error("更新支付配置失败");
        return ResponseResult.fail(FALLBACK_MESSAGE);
    }

    /** ================= 统一收银台（Task 0） ================= */

    @Override
    public ResponseResult cashierCreate(Map<String, Object> param) {
        log.error("收银台下单失败");
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult cashierStatus(Map<String, Object> param) {
        log.error("查询收银台订单状态失败");
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult cashierClose(Map<String, Object> param) {
        log.error("关闭收银台订单失败");
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult cashierOrderList(Map<String, Object> param) {
        log.error("查询收银台订单列表失败");
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult cashierOrderDetail(String cashierNo) {
        log.error("查询收银台订单详情失败, cashierNo={}", cashierNo);
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult bankcardList(Map<String, Object> param) {
        log.error("查询银行卡列表失败");
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult bankcardAdd(Map<String, Object> param) {
        log.error("绑定银行卡失败");
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult bankcardDelete(Map<String, Object> param) {
        log.error("解绑银行卡失败");
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult securitySetPassword(Map<String, Object> param) {
        log.error("设置支付密码失败");
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult securityVerifyPassword(Map<String, Object> param) {
        log.error("校验支付密码失败");
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult securityModifyPassword(Map<String, Object> param) {
        log.error("修改支付密码失败");
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult securityRealName(Map<String, Object> param) {
        log.error("实名认证提交失败");
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult securityRealNameStatus(String userId) {
        log.error("查询实名认证状态失败, userId={}", userId);
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult securityRealNameStatusByToken(Map<String, Object> param) {
        log.error("查询实名认证状态（token）失败");
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult smsSend(Map<String, Object> param) {
        log.error("发送短信验证码失败");
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult smsVerify(Map<String, Object> param) {
        log.error("校验短信验证码失败");
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    /** ================= 钱包（Task 0） ================= */

    @Override
    public ResponseResult walletInfo() {
        log.error("查询钱包信息失败");
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult walletTransactions(Map<String, Object> param) {
        log.error("查询钱包交易流水失败");
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult walletRecharge(Map<String, Object> param) {
        log.error("钱包充值失败");
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult walletWithdraw(Map<String, Object> param) {
        log.error("钱包提现失败");
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    /** ================= 平台商户管理（W1 P0 新增） ================= */

    @Override
    public ResponseResult merchantReg(Map<String, Object> param) {
        log.error("商户进件失败");
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult merchantList(Map<String, Object> param) {
        log.error("商户列表查询失败");
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult merchantDetail(Map<String, Object> param) {
        log.error("商户详情查询失败");
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult merchantUpdate(Map<String, Object> param) {
        log.error("商户信息更新失败");
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult merchantChangeStatus(Map<String, Object> param) {
        log.error("商户状态变更失败");
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult merchantConfigFee(Map<String, Object> param) {
        log.error("商户费率配置失败");
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult merchantUpdateAccount(Map<String, Object> param) {
        log.error("商户结算账户更新失败");
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult merchantOpenProduct(Map<String, Object> param) {
        log.error("商户产品开通失败");
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    /** ================= 支付风控 W2 P0 ================= */

    @Override public ResponseResult riskRuleCreate(Map<String, Object> param)       { log.error("风控规则创建失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult riskRuleUpdate(Map<String, Object> param)       { log.error("风控规则更新失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult riskRuleChangeStatus(Map<String, Object> param) { log.error("风控规则状态变更失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult riskRuleDelete(Map<String, Object> param)       { log.error("风控规则删除失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult riskRuleList(Map<String, Object> param)         { log.error("风控规则分页失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult riskRuleDetail(Map<String, Object> param)       { log.error("风控规则详情失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult riskLogList(Map<String, Object> param)          { log.error("风控日志分页失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult riskLogDetail(Map<String, Object> param)        { log.error("风控日志详情失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult riskLogAudit(Map<String, Object> param)         { log.error("风控日志审核失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult riskBlacklistAdd(Map<String, Object> param)     { log.error("黑白名单新增失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult riskBlacklistList(Map<String, Object> param)    { log.error("黑白名单分页失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult riskBlacklistRemove(Map<String, Object> param)  { log.error("黑白名单移除失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult riskCheck(Map<String, Object> param)            { log.error("风控评估失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }

    @Override
    public ResponseResult merchantQualPage(Map<String, Object> param) {
        log.error("商户资质分页查询失败");
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult merchantQualDetail(Long id, Boolean plainSensitive) {
        log.error("商户资质详情查询失败");
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult merchantQualSave(Map<String, Object> param) {
        log.error("商户资质保存失败");
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult merchantQualImport(Map<String, Object> param) {
        log.error("商户资质智能入库失败");
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult merchantQualToggle(Map<String, Object> param) {
        log.error("商户资质启用/停用失败");
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    @Override
    public ResponseResult merchantQualDelete(Map<String, Object> param) {
        log.error("商户资质删除失败");
        return ResponseResult.fail(500, FALLBACK_MESSAGE);
    }

    /** ================= 渠道路由（W3 P0 新增） ================= */

    @Override public ResponseResult channelSave(Map<String, Object> param)                 { log.error("渠道保存失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult channelChangeStatus(Map<String, Object> param)         { log.error("渠道状态变更失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult channelDelete(Map<String, Object> param)               { log.error("渠道删除失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult channelList(Map<String, Object> param)                 { log.error("渠道分页失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult channelDetail(Map<String, Object> param)               { log.error("渠道详情失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult channelGetByCode(Map<String, Object> param)            { log.error("渠道编码查询失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult channelConfigList(Map<String, Object> param)          { log.error("渠道凭证列表失败"); return ResponseResult.fail(503, FALLBACK_MESSAGE); }
    @Override public ResponseResult channelConfigGet(Map<String, Object> param)           { log.error("渠道凭证详情失败"); return ResponseResult.fail(503, FALLBACK_MESSAGE); }
    @Override public ResponseResult channelConfigSave(Map<String, Object> param)          { log.error("渠道凭证保存失败"); return ResponseResult.fail(503, FALLBACK_MESSAGE); }
    @Override public ResponseResult channelConfigToggle(Map<String, Object> param)        { log.error("渠道凭证启停失败"); return ResponseResult.fail(503, FALLBACK_MESSAGE); }
    @Override public ResponseResult channelConfigTestConnect(Map<String, Object> param)   { log.error("渠道连通性测试失败"); return ResponseResult.fail(503, FALLBACK_MESSAGE); }
    @Override public ResponseResult channelConfigPublic(Map<String, Object> param)        { log.error("渠道公开配置查询失败"); return ResponseResult.fail(503, FALLBACK_MESSAGE); }
    @Override public ResponseResult walletPay(Map<String, Object> param)                  { log.error("钱包余额扣款失败"); return ResponseResult.fail(503, FALLBACK_MESSAGE); }
    @Override public ResponseResult channelRouteSave(Map<String, Object> param)            { log.error("路由规则保存失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult channelRouteChangeStatus(Map<String, Object> param)    { log.error("路由规则状态变更失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult channelRouteDelete(Map<String, Object> param)          { log.error("路由规则删除失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult channelRouteList(Map<String, Object> param)            { log.error("路由规则分页失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult channelRouteDetail(Map<String, Object> param)          { log.error("路由规则详情失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult channelRouteSelect(Map<String, Object> param)          { log.error("路由选择失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
        @Override public ResponseResult channelRouteSelectRefund(Map<String, Object> param)    { log.error("退款路由选择失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult channelRouteReport(Map<String, Object> param)          { log.error("渠道结果上报失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult channelLogList(Map<String, Object> param)              { log.error("调用日志分页失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult channelLogDetail(Map<String, Object> param)            { log.error("调用日志详情失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult channelCompensateRegister(Map<String, Object> param)   { log.error("补偿任务注册失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult channelCompensateExecuteOne(Map<String, Object> param) { log.error("补偿任务执行失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult channelCompensateExecuteDue(Map<String, Object> param) { log.error("补偿任务批量执行失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult channelCompensateList(Map<String, Object> param)       { log.error("补偿任务分页失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult channelCompensateDetail(Map<String, Object> param)     { log.error("补偿任务详情失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult channelCompensateGiveUp(Map<String, Object> param)     { log.error("补偿任务放弃失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }

    /** ================= 通企付 H5 收银台（H5Cash P0）================= */

    @Override public ResponseResult h5CashGetH5Url(Map<String, Object> param)               { log.error("H5收银台下单失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult h5CashParseJumpResult(Map<String, String> params)       { log.error("H5收银台jumpUrl解析失败"); return ResponseResult.fail(500, FALLBACK_MESSAGE); }
    @Override public ResponseResult h5CashQueryByMchOrderNo(String mchNo, String mchOrderNo){ log.error("H5收银台订单查询失败, mchNo:{}, mchOrderNo:{}", mchNo, mchOrderNo); return ResponseResult.fail(500, FALLBACK_MESSAGE); }

    /** ================= W4 对账清算（T3）================= */

    @Override public ResponseResult reconPullChannelBill(Map<String, Object> param) { log.error("渠道对账单拉取失败"); return ResponseResult.fail(503, "支付服务暂时不可用：渠道对账单拉取失败，请检查 payment 服务状态"); }
    @Override public ResponseResult reconDiffList(Map<String, Object> param)        { log.error("对账差异分页失败"); return ResponseResult.fail(503, "支付服务暂时不可用：对账差异分页失败，请检查 payment 服务状态"); }
    @Override public ResponseResult reconDiffHandle(Map<String, Object> param)      { log.error("对账差异处理失败"); return ResponseResult.fail(503, "支付服务暂时不可用：对账差异处理失败，请检查 payment 服务状态"); }
    @Override public ResponseResult reconReportGen(Map<String, Object> param)       { log.error("对账报告生成失败"); return ResponseResult.fail(503, "支付服务暂时不可用：对账报告生成失败，请检查 payment 服务状态"); }
    @Override public ResponseResult reconDiffCheck(Map<String, Object> param)        { log.error("对账核对失败"); return ResponseResult.fail(503, "支付服务暂时不可用：对账核对失败，请检查 payment 服务状态"); }
    @Override public ResponseResult reconReportList(Map<String, Object> param)       { log.error("对账报表分页失败"); return ResponseResult.fail(503, "支付服务暂时不可用：对账报表分页失败，请检查 payment 服务状态"); }
    @Override public ResponseResult reconTrigger(Map<String, Object> param)          { log.error("手动触发对账失败"); return ResponseResult.fail(503, "支付服务暂时不可用：手动触发对账失败，请检查 payment 服务状态"); }

    /** ================= W4 商户补全（T4）================= */

    @Override public ResponseResult merchantRegPerson(Map<String, Object> param)        { log.error("个人/个体工商户进件失败"); return ResponseResult.fail(503, "支付服务暂时不可用：商户进件失败，请检查 payment 服务状态"); }
    @Override public ResponseResult merchantQualSupplement(Map<String, Object> param)   { log.error("商户资质补充失败"); return ResponseResult.fail(503, "支付服务暂时不可用：商户资质补充失败，请检查 payment 服务状态"); }
    @Override public ResponseResult merchantSubAccountList(Map<String, Object> param)   { log.error("商户子账号列表失败"); return ResponseResult.fail(503, "支付服务暂时不可用：商户子账号列表失败，请检查 payment 服务状态"); }

    /** ================= W4 自研会员（T5）================= */

    @Override public ResponseResult memberBizInfoQuery(Map<String, Object> param)         { log.error("会员信息聚合查询失败"); return ResponseResult.fail(503, "支付服务暂时不可用：会员信息查询失败，请检查 payment 服务状态"); }
    @Override public ResponseResult memberBizEnterpriseModify(Map<String, Object> param)  { log.error("会员企业信息修改失败"); return ResponseResult.fail(503, "支付服务暂时不可用：会员企业信息修改失败，请检查 payment 服务状态"); }
    @Override public ResponseResult memberBizRegStatus(Map<String, Object> param)         { log.error("会员注册状态查询失败"); return ResponseResult.fail(503, "支付服务暂时不可用：会员注册状态查询失败，请检查 payment 服务状态"); }
    @Override public ResponseResult memberBizProductManage(Map<String, Object> param)     { log.error("会员产品管理失败"); return ResponseResult.fail(503, "支付服务暂时不可用：会员产品管理失败，请检查 payment 服务状态"); }

    /** ================= W4 自研分账（T6）================= */

    @Override public ResponseResult divisionBizReceiverBind(Map<String, Object> param)    { log.error("分账接收方绑定失败"); return ResponseResult.fail(503, "支付服务暂时不可用：分账接收方绑定失败，请检查 payment 服务状态"); }
    @Override public ResponseResult divisionBizReceiverList(Map<String, Object> param)    { log.error("分账接收方列表失败"); return ResponseResult.fail(503, "支付服务暂时不可用：分账接收方列表失败，请检查 payment 服务状态"); }
    @Override public ResponseResult divisionBizReceiverUnbind(Map<String, Object> param)  { log.error("分账接收方解绑失败"); return ResponseResult.fail(503, "支付服务暂时不可用：分账接收方解绑失败，请检查 payment 服务状态"); }
    @Override public ResponseResult divisionBizRuleEngineCalc(Map<String, Object> param)  { log.error("分账规则引擎计算失败"); return ResponseResult.fail(503, "支付服务暂时不可用：分账规则引擎计算失败，请检查 payment 服务状态"); }

    /** ================= W4 风控人工审核（T7）================= */

    @Override public ResponseResult riskLogSubmitReview(Map<String, Object> param) { log.error("风控日志提交人工审核失败"); return ResponseResult.fail(503, "支付服务暂时不可用：风控提交审核失败，请检查 payment 服务状态"); }
    @Override public ResponseResult riskLogApprove(Map<String, Object> param)       { log.error("风控日志人工审核失败"); return ResponseResult.fail(503, "支付服务暂时不可用：风控审核失败，请检查 payment 服务状态"); }

    /** ================= 支付统计（Stats）================= */

    @Override public ResponseResult statsOverview(Map<String, Object> param)     { log.error("统计概览查询失败"); return ResponseResult.fail(503, "支付服务暂时不可用：统计概览查询失败"); }
    @Override public ResponseResult statsTrend(Map<String, Object> param)        { log.error("收入趋势查询失败"); return ResponseResult.fail(503, "支付服务暂时不可用：收入趋势查询失败"); }
    @Override public ResponseResult statsChannelDist(Map<String, Object> param)   { log.error("渠道分布查询失败"); return ResponseResult.fail(503, "支付服务暂时不可用：渠道分布查询失败"); }
    @Override public ResponseResult statsMerchantRank(Map<String, Object> param)  { log.error("商户排行查询失败"); return ResponseResult.fail(503, "支付服务暂时不可用：商户排行查询失败"); }

    /** ================= 支付中心后台查询（PayAdminQueryController） ================= */

    @Override public ResponseResult adminTradePage(Map<String, Object> param)    { log.error("支付订单分页查询失败"); return ResponseResult.fail(503, "支付服务暂时不可用：支付订单查询失败"); }
    @Override public ResponseResult adminTradeDetail(Map<String, Object> param)  { log.error("支付订单详情查询失败"); return ResponseResult.fail(503, "支付服务暂时不可用：支付订单详情查询失败"); }
    @Override public ResponseResult adminRefundPage(Map<String, Object> param)   { log.error("退款单分页查询失败"); return ResponseResult.fail(503, "支付服务暂时不可用：退款单查询失败"); }
    @Override public ResponseResult adminDepositPage(Map<String, Object> param)  { log.error("充值记录分页查询失败"); return ResponseResult.fail(503, "支付服务暂时不可用：充值记录查询失败"); }
    @Override public ResponseResult adminDepositDetail(Map<String, Object> param) { log.error("充值记录详情查询失败"); return ResponseResult.fail(503, "支付服务暂时不可用：充值记录详情查询失败"); }
    @Override public ResponseResult adminWithdrawPage(Map<String, Object> param) { log.error("提现记录分页查询失败"); return ResponseResult.fail(503, "支付服务暂时不可用：提现记录查询失败"); }
    @Override public ResponseResult adminWithdrawDetail(Map<String, Object> param) { log.error("提现记录详情查询失败"); return ResponseResult.fail(503, "支付服务暂时不可用：提现记录详情查询失败"); }
    @Override public ResponseResult adminRecordPage(Map<String, Object> param)   { log.error("资金流水分页查询失败"); return ResponseResult.fail(503, "支付服务暂时不可用：资金流水查询失败"); }
    @Override public ResponseResult adminBalancePage(Map<String, Object> param)  { log.error("账户余额分页查询失败"); return ResponseResult.fail(503, "支付服务暂时不可用：账户余额查询失败"); }

    /** ================= 交易管理（TradeController） ================= */

    @Override public Object tradeUnifiedOrder(Map<String, Object> param) { log.error("统一下单失败"); return ResponseResult.fail(503, "支付服务暂时不可用：统一下单失败"); }
    @Override public Object tradeQueryOrder(Map<String, Object> param)   { log.error("订单查询失败"); return ResponseResult.fail(503, "支付服务暂时不可用：订单查询失败"); }
    @Override public Object tradeCloseOrder(Map<String, Object> param)   { log.error("关闭订单失败"); return ResponseResult.fail(503, "支付服务暂时不可用：关闭订单失败"); }
    @Override public Object tradeRefund(Map<String, Object> param)       { log.error("统一退款失败"); return ResponseResult.fail(503, "支付服务暂时不可用：统一退款失败"); }
    @Override public Object tradeQueryRefund(Map<String, Object> param)  { log.error("退款查询失败"); return ResponseResult.fail(503, "支付服务暂时不可用：退款查询失败"); }

    /** ================= 会员管理（MemberController） ================= */

    @Override public Object memberRegEnterprise(Map<String, Object> param) { log.error("企业会员开户失败"); return ResponseResult.fail(503, "支付服务暂时不可用：企业会员开户失败"); }
    @Override public Object memberRegPerson(Map<String, Object> param)     { log.error("个人会员开户失败"); return ResponseResult.fail(503, "支付服务暂时不可用：个人会员开户失败"); }
    @Override public Object memberRegSupplement(Map<String, Object> param) { log.error("会员资料补录失败"); return ResponseResult.fail(503, "支付服务暂时不可用：会员资料补录失败"); }

    /** ================= 分账管理（DivisionController） ================= */

    @Override public Object divisionBatch(Map<String, Object> param)   { log.error("批量分账失败"); return ResponseResult.fail(503, "支付服务暂时不可用：批量分账失败"); }
    @Override public Object divisionQuery(Map<String, Object> param)   { log.error("分账查询失败"); return ResponseResult.fail(503, "支付服务暂时不可用：分账查询失败"); }
    @Override public Object divisionRefund(Map<String, Object> param)  { log.error("分账撤销失败"); return ResponseResult.fail(503, "支付服务暂时不可用：分账撤销失败"); }

    /** ================= 商户进件 / 对账 / 账单 / 余额 / 提现 ================= */

    @Override public Object mchReg(Map<String, Object> param)           { log.error("商户进件失败"); return ResponseResult.fail(503, "支付服务暂时不可用：商户进件失败"); }
    @Override public Object statementDownload(Map<String, Object> param) { log.error("对账文件下载失败"); return ResponseResult.fail(503, "支付服务暂时不可用：对账文件下载失败"); }
    @Override public Object billImport(Map<String, Object> param)       { log.error("账单导入失败"); return ResponseResult.fail(503, "支付服务暂时不可用：账单导入失败"); }
    @Override public Object balanceQuery(Map<String, Object> param)     { log.error("余额查询失败"); return ResponseResult.fail(503, "支付服务暂时不可用：余额查询失败"); }
    @Override public Object withdrawApply(Map<String, Object> param)    { log.error("提现申请失败"); return ResponseResult.fail(503, "支付服务暂时不可用：提现申请失败"); }
}
