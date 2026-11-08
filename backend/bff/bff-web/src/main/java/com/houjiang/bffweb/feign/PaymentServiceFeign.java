package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 支付服务Feign客户端
/**
 * 
 * @description 调用paas中台支付服务(payment)的接口
 * @author Amos
 * @date 2024-12-20
 */
@FeignClient(name = "payment", path = "/", fallback = PaymentServiceFeignFallback.class)
public interface PaymentServiceFeign {

    // ================= 通用API调用接口 =================
    
    /**
     * 通用API调用方法(无参数)
/**
     * @param path API路径
     * @return 响应结果
     */
    @GetMapping("/{path}")
    ResponseResult callPaymentAPI(@PathVariable("path") String path);
    
    /**
     * 通用API调用方法(有参数)
/**
     * @param path API路径
     * @param params 请求参数
     * @return 响应结果
     */
    @PostMapping("/{path}")
    ResponseResult callPaymentAPIWithBody(@PathVariable("path") String path, @RequestBody Map<String, Object> params);

    // ================= 支付管理接口 =================
    
    /**
     * 创建支付订单
     */
    @PostMapping("/payment/pay/create")
    ResponseResult createPayment(@RequestBody Map<String, Object> paymentData);
    
    /**
     * 查询支付订单
     */
    @GetMapping("/payment/pay/{id}")
    ResponseResult getPaymentDetail(@PathVariable("id") Long id);
    
    /**
     * 查询支付列表
     */
    @PostMapping("/payment/pay/query")
    ResponseResult queryPaymentList(@RequestBody Map<String, Object> param);
    
    /**
     * 发起支付
     */
    @PostMapping("/payment/pay/initiate")
    ResponseResult initiatePayment(@RequestBody Map<String, Object> param);
    
    /**
     * 支付回调
     */
    @PostMapping("/payment/pay/callback")
    ResponseResult paymentCallback(@RequestBody Map<String, Object> callbackData);
    
    /**
     * 查询支付状态
     */
    @GetMapping("/payment/pay/status/{orderNo}")
    ResponseResult getPaymentStatus(@PathVariable("orderNo") String orderNo);
    
    /**
     * 关闭支付订单
     */
    @PostMapping("/payment/pay/close")
    ResponseResult closePayment(@RequestBody Map<String, Object> param);
    
    // ================= 退款管理接口 ==================
    
    /**
     * 创建退款订单
     */
    @PostMapping("/payment/refund/create")
    ResponseResult createRefund(@RequestBody Map<String, Object> refundData);
    
    /**
     * 查询退款订单
     */
    @GetMapping("/payment/refund/{id}")
    ResponseResult getRefundDetail(@PathVariable("id") Long id);
    
    /**
     * 查询退款列表
     */
    @PostMapping("/payment/refund/query")
    ResponseResult queryRefundList(@RequestBody Map<String, Object> param);
    
    /**
     * 发起退款
     */
    @PostMapping("/payment/refund/initiate")
    ResponseResult initiateRefund(@RequestBody Map<String, Object> param);
    
    /**
     * 退款回调
     */
    @PostMapping("/payment/refund/callback")
    ResponseResult refundCallback(@RequestBody Map<String, Object> callbackData);
    
    /**
     * 查询退款状态
     */
    @GetMapping("/payment/refund/status/{refundNo}")
    ResponseResult getRefundStatus(@PathVariable("refundNo") String refundNo);
    
    // ================= 提现管理接口 =================

    /**
     * 平台查询提现订单列表
/**
     * @param param 分页和查询条件
     * @return 提现记录列表
     */
    @PostMapping("/pay/consumeWithdraw/platform/withdrawDepositList")
    ResponseResult withdrawDepositList(@RequestBody Map<String, Object> param);

    // ================= 支付配置接口 =================
    
    /**
     * 查询支付方式
     */
    @GetMapping("/payment/config/methods")
    ResponseResult getPaymentMethods();
    
    /**
     * 查询支付配置
     */
    @PostMapping("/payment/config/query")
    ResponseResult queryPaymentConfig(@RequestBody Map<String, Object> param);
    
    /**
     * 更新支付配置
     */
    @PostMapping("/payment/config/update")
    ResponseResult updatePaymentConfig(@RequestBody Map<String, Object> configData);

    // ================= 统一收银台（Task 0 新增） =================

    /**
     * 统一收银台下单
     */
    @PostMapping("/api/payment/cashier/create")
    ResponseResult cashierCreate(@RequestBody Map<String, Object> param);

    /**
     * 查询收银台订单状态（POST）
     */
    @PostMapping("/api/payment/cashier/status")
    ResponseResult cashierStatus(@RequestBody Map<String, Object> param);

    /**
     * 关闭收银台订单
     */
    @PostMapping("/api/payment/cashier/close")
    ResponseResult cashierClose(@RequestBody Map<String, Object> param);

    // ================= 收银台订单查询（Task 0 新增） =================

    /**
     * 收银台订单列表
     */
    @PostMapping("/api/payment/order/list")
    ResponseResult cashierOrderList(@RequestBody Map<String, Object> param);

    /**
     * 收银台订单详情
     */
    @GetMapping("/api/payment/order/detail/{cashierNo}")
    ResponseResult cashierOrderDetail(@PathVariable("cashierNo") String cashierNo);

    // ================= 银行卡（Task 0 新增） =================

    /**
     * 银行卡列表
     */
    @PostMapping("/api/payment/bankcard/list")
    ResponseResult bankcardList(@RequestBody Map<String, Object> param);

    /**
     * 绑定银行卡
     */
    @PostMapping("/api/payment/bankcard/add")
    ResponseResult bankcardAdd(@RequestBody Map<String, Object> param);

    /**
     * 解绑银行卡
     */
    @PostMapping("/api/payment/bankcard/delete")
    ResponseResult bankcardDelete(@RequestBody Map<String, Object> param);

    // ================= 支付安全（Task 0 新增） =================

    /**
     * 设置支付密码
     */
    @PostMapping("/api/payment/security/setPassword")
    ResponseResult securitySetPassword(@RequestBody Map<String, Object> param);

    /**
     * 校验支付密码
     */
    @PostMapping("/api/payment/security/verifyPassword")
    ResponseResult securityVerifyPassword(@RequestBody Map<String, Object> param);

    /**
     * 修改支付密码
     */
    @PostMapping("/api/payment/security/modifyPassword")
    ResponseResult securityModifyPassword(@RequestBody Map<String, Object> param);

    /**
     * 实名认证提交
     */
    @PostMapping("/api/payment/security/realName")
    ResponseResult securityRealName(@RequestBody Map<String, Object> param);

    /**
     * 查询实名认证状态
     */
    @GetMapping("/api/payment/security/realNameStatus/{userId}")
    ResponseResult securityRealNameStatus(@PathVariable("userId") String userId);

    /**
     * 查询实名认证状态（POST：从 Header token 解析）
     */
    @PostMapping("/api/payment/security/realNameStatus")
    ResponseResult securityRealNameStatusByToken(@RequestBody Map<String, Object> param);

    // ================= 短信（Task 0 新增） =================

    /**
     * 发送短信验证码
     */
    @PostMapping("/api/payment/sms/send")
    ResponseResult smsSend(@RequestBody Map<String, Object> param);

    /**
     * 校验短信验证码
     */
    @PostMapping("/api/payment/sms/verify")
    ResponseResult smsVerify(@RequestBody Map<String, Object> param);

    // ================= 钱包（Task 0 新增） =================

    /**
     * 钱包信息
     */
    @PostMapping("/api/payment/wallet/info")
    ResponseResult walletInfo();

    /**
     * 钱包交易流水
     */
    @PostMapping("/api/payment/wallet/transactions")
    ResponseResult walletTransactions(@RequestBody Map<String, Object> param);

    /**
     * 钱包充值
     */
    @PostMapping("/api/payment/wallet/recharge")
    ResponseResult walletRecharge(@RequestBody Map<String, Object> param);

    /**
     * 钱包提现
     */
    @PostMapping("/api/payment/wallet/withdraw")
    ResponseResult walletWithdraw(@RequestBody Map<String, Object> param);

    // ================= 平台商户管理（W1 P0 新增） =================

    /**
     * 商户进件
     */
    @PostMapping("/api/payment/merchant/reg")
    ResponseResult merchantReg(@RequestBody Map<String, Object> param);

    /**
     * 商户列表（分页）
     */
    @PostMapping("/api/payment/merchant/list")
    ResponseResult merchantList(@RequestBody Map<String, Object> param);

    /**
     * 商户详情
     */
    @PostMapping("/api/payment/merchant/detail")
    ResponseResult merchantDetail(@RequestBody Map<String, Object> param);

    /**
     * 商户信息更新
     */
    @PostMapping("/api/payment/merchant/update")
    ResponseResult merchantUpdate(@RequestBody Map<String, Object> param);

    /**
     * 商户状态变更
     */
    @PostMapping("/api/payment/merchant/changeStatus")
    ResponseResult merchantChangeStatus(@RequestBody Map<String, Object> param);

    /**
     * 商户费率配置
     */
    @PostMapping("/api/payment/merchant/configFee")
    ResponseResult merchantConfigFee(@RequestBody Map<String, Object> param);

    /**
     * 商户结算账户更新
     */
    @PostMapping("/api/payment/merchant/updateAccount")
    ResponseResult merchantUpdateAccount(@RequestBody Map<String, Object> param);

    /**
     * 商户产品开通/停用
     */
    @PostMapping("/api/payment/merchant/openProduct")
    ResponseResult merchantOpenProduct(@RequestBody Map<String, Object> param);

    // ================= 支付风控（W2 P0 新增） =================

    /** 规则创建 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/risk/rule/create")
    ResponseResult riskRuleCreate(@RequestBody Map<String, Object> param);

    /** 规则更新 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/risk/rule/update")
    ResponseResult riskRuleUpdate(@RequestBody Map<String, Object> param);

    /** 规则状态变更 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/risk/rule/changeStatus")
    ResponseResult riskRuleChangeStatus(@RequestBody Map<String, Object> param);

    /** 规则删除 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/risk/rule/delete")
    ResponseResult riskRuleDelete(@RequestBody Map<String, Object> param);

    /** 规则分页 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/risk/rule/list")
    ResponseResult riskRuleList(@RequestBody Map<String, Object> param);

    /** 规则详情 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/risk/rule/detail")
    ResponseResult riskRuleDetail(@RequestBody Map<String, Object> param);

    /** 日志分页 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/risk/log/list")
    ResponseResult riskLogList(@RequestBody Map<String, Object> param);

    /** 日志详情 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/risk/log/detail")
    ResponseResult riskLogDetail(@RequestBody Map<String, Object> param);

    /** 日志审核 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/risk/log/audit")
    ResponseResult riskLogAudit(@RequestBody Map<String, Object> param);

    /** 黑白名单新增；当前领域实现会立即启用，智能入库不得调用。 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/risk/blacklist/add")
    ResponseResult riskBlacklistAdd(@RequestBody Map<String, Object> param);

    /** 黑白名单分页 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/risk/blacklist/list")
    ResponseResult riskBlacklistList(@RequestBody Map<String, Object> param);

    /** 黑白名单移除 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/risk/blacklist/remove")
    ResponseResult riskBlacklistRemove(@RequestBody Map<String, Object> param);

    /** 风控评估核心入口 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/risk/check")
    ResponseResult riskCheck(@RequestBody Map<String, Object> param);

    // ================= 商户资质管理（16终端 × 多渠道，新增） =================

    /**
     * 商户资质分页
     */
    @PostMapping("/api/payment/admin/merchantQual/page")
    ResponseResult merchantQualPage(@RequestBody Map<String, Object> param);

    /**
     * 商户资质详情（plainSensitive=true 返回明文敏感字段）
     */
    @GetMapping("/api/payment/admin/merchantQual/{id}")
    ResponseResult merchantQualDetail(@PathVariable("id") Long id,
                                      @RequestParam(value = "plainSensitive", required = false) Boolean plainSensitive);

    /** 保存商户资质；智能入库必须过滤敏感字段并固定 status=0。 */
    @PostMapping("/api/payment/admin/merchantQual/save")
    ResponseResult merchantQualSave(@RequestBody Map<String, Object> param);

    @PostMapping("/api/payment/admin/merchantQual/import")
    ResponseResult merchantQualImport(@RequestBody Map<String, Object> param);

    /**
     * 启用/停用商户资质
     */
    @PostMapping("/api/payment/admin/merchantQual/toggle")
    ResponseResult merchantQualToggle(@RequestBody Map<String, Object> param);

    /**
     * 删除（逻辑删除）商户资质
     */
    @PostMapping("/api/payment/admin/merchantQual/delete")
    ResponseResult merchantQualDelete(@RequestBody Map<String, Object> param);

    // ================= 渠道路由（W3 P0 新增） =================

    /** 渠道保存 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/channel/channel/save")
    ResponseResult channelSave(@RequestBody Map<String, Object> param);

    /** 渠道状态变更 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/channel/channel/changeStatus")
    ResponseResult channelChangeStatus(@RequestBody Map<String, Object> param);

    /** 渠道删除 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/channel/channel/delete")
    ResponseResult channelDelete(@RequestBody Map<String, Object> param);

    /** 渠道分页 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/channel/channel/list")
    ResponseResult channelList(@RequestBody Map<String, Object> param);

    /** 渠道详情 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/channel/channel/detail")
    ResponseResult channelDetail(@RequestBody Map<String, Object> param);

    /** 渠道根据编码查询 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/channel/channel/getByCode")
    ResponseResult channelGetByCode(@RequestBody Map<String, Object> param);

    // ================= 渠道凭证管理（W5 新增） =================

    /** 凭证列表（脱敏） */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/channel/channel/config/list")
    ResponseResult channelConfigList(@RequestBody Map<String, Object> param);

    /** 凭证详情（解密） */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/channel/channel/config/get")
    ResponseResult channelConfigGet(@RequestBody Map<String, Object> param);

    /** 保存凭证（加密落库） */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/channel/channel/config/save")
    ResponseResult channelConfigSave(@RequestBody Map<String, Object> param);

    /** 凭证启用/停用 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/channel/channel/config/toggle")
    ResponseResult channelConfigToggle(@RequestBody Map<String, Object> param);

    /** 凭证连通性测试 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/channel/channel/config/testConnect")
    ResponseResult channelConfigTestConnect(@RequestBody Map<String, Object> param);

    /** 公开版渠道配置（C 端收银台读取启用渠道 + 公开字段，剔除敏感凭证） */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/channel/channel/config/public")
    ResponseResult channelConfigPublic(@RequestBody Map<String, Object> param);

    /** 钱包余额扣款（C 端收银台真实扣款入口） */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/wallet/pay")
    ResponseResult walletPay(@RequestBody Map<String, Object> param);

    /** 路由规则保存 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/channel/route/save")
    ResponseResult channelRouteSave(@RequestBody Map<String, Object> param);

    /** 路由规则状态变更 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/channel/route/changeStatus")
    ResponseResult channelRouteChangeStatus(@RequestBody Map<String, Object> param);

    /** 路由规则删除 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/channel/route/delete")
    ResponseResult channelRouteDelete(@RequestBody Map<String, Object> param);

    /** 路由规则分页 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/channel/route/list")
    ResponseResult channelRouteList(@RequestBody Map<String, Object> param);

    /** 路由规则详情 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/channel/route/detail")
    ResponseResult channelRouteDetail(@RequestBody Map<String, Object> param);

    /** 路由选择核心入口 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/channel/route/select")
    ResponseResult channelRouteSelect(@RequestBody Map<String, Object> param);

    /** 退款渠道路由选择（W5+ P2-1） */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/channel/route/selectRefund")
    ResponseResult channelRouteSelectRefund(@RequestBody Map<String, Object> param);

    /** 渠道调用结果上报 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/channel/route/report")
    ResponseResult channelRouteReport(@RequestBody Map<String, Object> param);

    /** 调用日志分页 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/channel/log/list")
    ResponseResult channelLogList(@RequestBody Map<String, Object> param);

    /** 调用日志详情 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/channel/log/detail")
    ResponseResult channelLogDetail(@RequestBody Map<String, Object> param);

    /** 补偿任务注册 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/channel/compensate/register")
    ResponseResult channelCompensateRegister(@RequestBody Map<String, Object> param);

    /** 补偿任务手动执行 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/channel/compensate/executeOne")
    ResponseResult channelCompensateExecuteOne(@RequestBody Map<String, Object> param);

    /** 补偿任务到期批量执行 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/channel/compensate/executeDue")
    ResponseResult channelCompensateExecuteDue(@RequestBody Map<String, Object> param);

    /** 补偿任务分页 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/channel/compensate/list")
    ResponseResult channelCompensateList(@RequestBody Map<String, Object> param);

    /** 补偿任务详情 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/channel/compensate/detail")
    ResponseResult channelCompensateDetail(@RequestBody Map<String, Object> param);

    /** 补偿任务放弃 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/channel/compensate/giveUp")
    ResponseResult channelCompensateGiveUp(@RequestBody Map<String, Object> param);

    // ================= 通企付 H5 收银台（H5Cash P0 新增）=================

    /** H5 收银台下单（27. /h5Cash/getH5Url）*/
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/h5cash/getH5Url")
    ResponseResult h5CashGetH5Url(@RequestBody Map<String, Object> param);

    /** jumpUrl 回跳结果解析（带验签与 state 流转）*/
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/api/payment/h5cash/parseJumpResult")
    ResponseResult h5CashParseJumpResult(@RequestBody Map<String, String> params);

    /** 按商户订单号查询 H5 收银台订单 */
    /**
     * TODO 补充方法说明
/**
     * @param "mchNo" TODO
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/api/payment/h5cash/queryByMchOrderNo")
    ResponseResult h5CashQueryByMchOrderNo(@RequestParam(value = "mchNo", required = false) String mchNo,
                                           @RequestParam("mchOrderNo") String mchOrderNo);

    // ================= W4 对账清算（T3 新增） =================

    /** 渠道对账单拉取 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/recon/pullChannelBill")
    ResponseResult reconPullChannelBill(@RequestBody Map<String, Object> param);

    /** 对账差异分页 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/recon/diffList")
    ResponseResult reconDiffList(@RequestBody Map<String, Object> param);

    /** 对账差异处理 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/recon/diffHandle")
    ResponseResult reconDiffHandle(@RequestBody Map<String, Object> param);

    /** 对账报告生成 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/recon/reportGen")
    ResponseResult reconReportGen(@RequestBody Map<String, Object> param);

    /** 对账核对（比对本地订单与渠道账单） */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/recon/diffCheck")
    ResponseResult reconDiffCheck(@RequestBody Map<String, Object> param);

    /** 对账报表分页查询 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/recon/reportList")
    ResponseResult reconReportList(@RequestBody Map<String, Object> param);

    /** 手动触发对账（拉取+核对） */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/recon/trigger")
    ResponseResult reconTrigger(@RequestBody Map<String, Object> param);

    // ================= W4 商户补全（T4 新增） =================

    /** 个人/个体工商户进件 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/merchant/regPerson")
    ResponseResult merchantRegPerson(@RequestBody Map<String, Object> param);

    /** 商户资质补充 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/merchant/qualSupplement")
    ResponseResult merchantQualSupplement(@RequestBody Map<String, Object> param);

    /** 商户子账号列表 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/merchant/subAccount/list")
    ResponseResult merchantSubAccountList(@RequestBody Map<String, Object> param);

    // ================= W4 自研会员（T5 新增） =================

    /** 会员信息聚合查询 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/member/biz/info/query")
    ResponseResult memberBizInfoQuery(@RequestBody Map<String, Object> param);

    /** 会员企业信息修改 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/member/biz/enterprise/modify")
    ResponseResult memberBizEnterpriseModify(@RequestBody Map<String, Object> param);

    /** 会员注册状态 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/member/biz/regStatus")
    ResponseResult memberBizRegStatus(@RequestBody Map<String, Object> param);

    /** 会员产品管理（open/close/list） */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/member/biz/product/manage")
    ResponseResult memberBizProductManage(@RequestBody Map<String, Object> param);

    // ================= W4 自研分账（T6 新增） =================

    /** 分账接收方绑定 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/division/biz/receiver/bind")
    ResponseResult divisionBizReceiverBind(@RequestBody Map<String, Object> param);

    /** 分账接收方列表 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/division/biz/receiver/list")
    ResponseResult divisionBizReceiverList(@RequestBody Map<String, Object> param);

    /** 分账接收方解绑 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/division/biz/receiver/unbind")
    ResponseResult divisionBizReceiverUnbind(@RequestBody Map<String, Object> param);

    /** 分账规则引擎计算 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/division/biz/ruleEngine/calc")
    ResponseResult divisionBizRuleEngineCalc(@RequestBody Map<String, Object> param);

    // ================= W4 风控人工审核（T7 新增） =================

    /** 风控日志提交人工审核 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/risk/log/submitReview")
    ResponseResult riskLogSubmitReview(@RequestBody Map<String, Object> param);

    /** 风控日志人工审核通过/驳回 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/risk/log/approve")
    ResponseResult riskLogApprove(@RequestBody Map<String, Object> param);

    // ================= 支付统计（Stats 新增） =================

    /** 统计概览 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/stats/overview")
    ResponseResult statsOverview(@RequestBody Map<String, Object> param);

    /** 收入趋势 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/stats/trend")
    ResponseResult statsTrend(@RequestBody Map<String, Object> param);

    /** 渠道分布 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/stats/channelDist")
    ResponseResult statsChannelDist(@RequestBody Map<String, Object> param);

    /** 商户 TOP N 交易排行 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/api/payment/stats/merchantRank")
    ResponseResult statsMerchantRank(@RequestBody Map<String, Object> param);

    // ================= 支付中心后台查询（PayAdminQueryController 新增） =================

    /** 支付订单分页 */
    @PostMapping("/api/payment/admin/trade/page")
    ResponseResult adminTradePage(@RequestBody Map<String, Object> param);

    /** 支付订单详情 */
    @PostMapping("/api/payment/admin/trade/detail")
    ResponseResult adminTradeDetail(@RequestBody Map<String, Object> param);

    /** 退款单分页 */
    @PostMapping("/api/payment/admin/refund/page")
    ResponseResult adminRefundPage(@RequestBody Map<String, Object> param);

    /** 充值记录分页 */
    @PostMapping("/api/payment/admin/deposit/page")
    ResponseResult adminDepositPage(@RequestBody Map<String, Object> param);

    /** 充值记录详情 */
    @PostMapping("/api/payment/admin/deposit/detail")
    ResponseResult adminDepositDetail(@RequestBody Map<String, Object> param);

    /** 提现记录分页 */
    @PostMapping("/api/payment/admin/withdraw/page")
    ResponseResult adminWithdrawPage(@RequestBody Map<String, Object> param);

    /** 提现记录详情 */
    @PostMapping("/api/payment/admin/withdraw/detail")
    ResponseResult adminWithdrawDetail(@RequestBody Map<String, Object> param);

    /** 资金流水分页 */
    @PostMapping("/api/payment/admin/record/page")
    ResponseResult adminRecordPage(@RequestBody Map<String, Object> param);

    /** 账户余额分页 */
    @PostMapping("/api/payment/admin/balance/page")
    ResponseResult adminBalancePage(@RequestBody Map<String, Object> param);

    // ================= 交易管理（TradeController 透传，返回 DTO 由 BFF 包裹） =================

    /** 统一下单 */
    @PostMapping("/api/payment/trade/unifiedOrder")
    Object tradeUnifiedOrder(@RequestBody Map<String, Object> param);

    /** 订单查询 */
    @PostMapping("/api/payment/trade/queryOrder")
    Object tradeQueryOrder(@RequestBody Map<String, Object> param);

    /** 关闭订单 */
    @PostMapping("/api/payment/trade/closeOrder")
    Object tradeCloseOrder(@RequestBody Map<String, Object> param);

    /** 统一退款 */
    @PostMapping("/api/payment/trade/refund")
    Object tradeRefund(@RequestBody Map<String, Object> param);

    /** 退款查询 */
    @PostMapping("/api/payment/trade/queryRefund")
    Object tradeQueryRefund(@RequestBody Map<String, Object> param);

    // ================= 会员管理（MemberController 透传，返回 DTO 由 BFF 包裹） =================

    /** 企业会员实名开户 */
    @PostMapping("/api/payment/member/regenterprise")
    Object memberRegEnterprise(@RequestBody Map<String, Object> param);

    /** 个人会员实名及绑卡 */
    @PostMapping("/api/payment/member/regperson")
    Object memberRegPerson(@RequestBody Map<String, Object> param);

    /** 会员资料补录 */
    @PostMapping("/api/payment/member/regsupplement")
    Object memberRegSupplement(@RequestBody Map<String, Object> param);

    // ================= 分账管理（DivisionController 透传，返回 DTO 由 BFF 包裹） =================

    /** 批量分账 */
    @PostMapping("/api/payment/division/batch")
    Object divisionBatch(@RequestBody Map<String, Object> param);

    /** 分账查询 */
    @PostMapping("/api/payment/division/query")
    Object divisionQuery(@RequestBody Map<String, Object> param);

    /** 分账撤销 */
    @PostMapping("/api/payment/division/refund")
    Object divisionRefund(@RequestBody Map<String, Object> param);

    // ================= 商户进件 / 对账文件 / 账单 / 余额 / 提现（返回 DTO 由 BFF 包裹） =================

    /** 商户进件（通企付） */
    @PostMapping("/api/payment/mch/reg")
    Object mchReg(@RequestBody Map<String, Object> param);

    /** 对账文件下载 */
    @PostMapping("/api/payment/statement/download")
    Object statementDownload(@RequestBody Map<String, Object> param);

    /** 账单导入 */
    @PostMapping("/api/payment/bill/import")
    Object billImport(@RequestBody Map<String, Object> param);

    /** 统一余额查询 */
    @PostMapping("/api/payment/balance/query")
    Object balanceQuery(@RequestBody Map<String, Object> param);

    /** 提现申请（通企付） */
    @PostMapping("/api/payment/withdraw/apply")
    Object withdrawApply(@RequestBody Map<String, Object> param);
}
