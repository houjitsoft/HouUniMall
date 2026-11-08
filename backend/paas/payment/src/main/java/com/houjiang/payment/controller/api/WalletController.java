package com.houjiang.payment.controller.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.PasswordUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.dto.cashier.CashierStatusResponse;
import com.houjiang.payment.entity.PayConsumeRecordEntity;
import com.houjiang.payment.entity.PaySecurityEntity;
import com.houjiang.payment.entity.PayUserResourceEntity;
import com.houjiang.payment.mapper.PayConsumeRecordMapper;
import com.houjiang.payment.mapper.PaySecurityMapper;
import com.houjiang.payment.mapper.PayUserResourceMapper;
import com.houjiang.payment.service.api.CashierService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 钱包 Controller（Task 0 骨架）
 * 对应前端 /api/pay.js 的 getWalletInfo/getTransactionList/rechargeApply/withdrawApply
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final PayUserResourceMapper payUserResourceMapper;
    private final PayConsumeRecordMapper payConsumeRecordMapper;
    private final PaySecurityMapper paySecurityMapper;
    private final CashierService cashierService;

    /**
     * 钱包信息：返回用户余额、冻结、充值卡三项
     */
    @PostMapping("/info")
    public ResponseResult<Map<String, Object>> info(HttpServletRequest http) {
        String userId = resolveUserId(http);
        if (!StringUtils.hasText(userId)) {
            return ResponseResult.fail(401, "用户未登录");
        }
        PayUserResourceEntity entity = payUserResourceMapper.selectById(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("balance", entity == null || entity.getUserMoney() == null ? BigDecimal.ZERO : entity.getUserMoney());
        data.put("frozen", entity == null || entity.getUserMoneyFrozen() == null ? BigDecimal.ZERO : entity.getUserMoneyFrozen());
        data.put("rechargeCard", entity == null || entity.getUserRechargeCard() == null ? BigDecimal.ZERO : entity.getUserRechargeCard());
        return ResponseResult.success(data);
    }

    /**
     * 钱包交易记录分页
     * @param param {page,size,type,tradeTypeId}
     */
    @PostMapping("/transactions")
    public ResponseResult<IPage<PayConsumeRecordEntity>> transactions(@RequestBody Map<String, Object> param,
                                                                     HttpServletRequest http) {
        String userId = resolveUserId(http);
        if (!StringUtils.hasText(userId)) {
            return ResponseResult.fail(401, "用户未登录");
        }
        long pageNum = param.get("page") == null ? 1L : Long.parseLong(param.get("page").toString());
        long pageSize = param.get("size") == null ? 20L : Long.parseLong(param.get("size").toString());
        Integer tradeTypeId = param.get("tradeTypeId") == null ? null : Integer.valueOf(param.get("tradeTypeId").toString());
        IPage<PayConsumeRecordEntity> p = payConsumeRecordMapper.selectPage(
                new Page<>(pageNum, pageSize),
                Wrappers.<PayConsumeRecordEntity>lambdaQuery()
                        .eq(PayConsumeRecordEntity::getUserId, userId)
                        .eq(PayConsumeRecordEntity::getRecordDelete, 0)
                        .eq(tradeTypeId != null, PayConsumeRecordEntity::getTradeTypeId, tradeTypeId)
                        .orderByDesc(PayConsumeRecordEntity::getRecordTime));
        return ResponseResult.success(p);
    }

    /**
     * 钱包充值（Task 0 占位：真实渠道对接放在后续 Task；建议前端直接走 /cashier/create）
     */
    @PostMapping("/recharge")
    public ResponseResult<Map<String, Object>> recharge(@RequestBody Map<String, Object> param,
                                                        HttpServletRequest http) {
        String userId = resolveUserId(http);
        log.info("[wallet/recharge] userId={}, amount={}, payMethod={}",
                userId, param.get("amount"), param.get("payMethod"));
        Map<String, Object> tip = new HashMap<>();
        tip.put("userId", userId);
        tip.put("tip", "请通过 /web/pay/cashier/create 发起充值下单（Task 0 骨架未接入真实渠道充值通道）");
        return ResponseResult.fail(501, "充值通道尚未开通，请通过收银台下单", tip);
    }

    /**
     * 钱包提现（Task 0 占位：实际对接放在后续 Task；这里不执行扣款）
     */
    @PostMapping("/withdraw")
    public ResponseResult<Map<String, Object>> withdraw(@RequestBody Map<String, Object> param,
                                                        HttpServletRequest http) {
        String userId = resolveUserId(http);
        log.info("[wallet/withdraw] userId={}, amount={}, bankCardId={}",
                userId, param.get("amount"), param.get("bankCardId"));
        Map<String, Object> tip = new HashMap<>();
        tip.put("userId", userId);
        tip.put("tip", "提现通道尚未开通（Task 0 骨架）");
        return ResponseResult.fail(501, "提现通道尚未开通", tip);
    }

    /**
     * 钱包余额扣款（C 端收银台“余额支付”入口）
     * 入参：{ cashierNo, payPassword }
     * 流程：JWT解出 userId → BCrypt 校验支付密码 → 调用 cashierService.payByBalance 完成扣款。
     */
    @PostMapping("/pay")
    public ResponseResult<Map<String, Object>> walletPay(@RequestBody Map<String, Object> body,
                                                         HttpServletRequest http) {
        String userId = resolveUserId(http);
        if (!StringUtils.hasText(userId)) {
            return ResponseResult.fail(401, "用户未登录");
        }
        if (body == null) {
            return ResponseResult.fail(500, "参数不能为空");
        }
        String cashierNo = (String) body.get("cashierNo");
        String payPassword = (String) body.get("payPassword");
        if (!StringUtils.hasText(cashierNo)) {
            return ResponseResult.fail(500, "cashierNo 必填");
        }
        if (!StringUtils.hasText(payPassword)) {
            return ResponseResult.fail(500, "payPassword 必填");
        }
        // 1) 校验支付密码
        PaySecurityEntity sec = paySecurityMapper.selectOne(
                Wrappers.<PaySecurityEntity>lambdaQuery().eq(PaySecurityEntity::getUserId, userId));
        if (sec == null || !StringUtils.hasText(sec.getPayPassword())) {
            return ResponseResult.fail(500, "尚未设置支付密码");
        }
        if (sec.getPwdErrorCount() != null && sec.getPwdErrorCount() >= 5) {
            return ResponseResult.fail(500, "密码错误次数超限，请稍后再试");
        }
        boolean ok = Boolean.TRUE.equals(PasswordUtils.BCryptPasswordMatches(payPassword, sec.getPayPassword()));
        if (!ok) {
            sec.setPwdErrorCount((sec.getPwdErrorCount() == null ? 0 : sec.getPwdErrorCount()) + 1);
            sec.setUpdateTime(new Date());
            paySecurityMapper.updateById(sec);
            return ResponseResult.fail(500, "支付密码错误");
        }
        sec.setPwdErrorCount(0);
        sec.setUpdateTime(new Date());
        paySecurityMapper.updateById(sec);
        // 2) 调用扣款
        try {
            CashierStatusResponse resp = cashierService.payByBalance(cashierNo, userId);
            Map<String, Object> data = new HashMap<>();
            data.put("cashierNo", resp.getCashierNo());
            data.put("payOrderId", resp.getPayOrderId());
            data.put("status", resp.getStatus());
            data.put("statusDesc", resp.getStatusDesc());
            data.put("amount", resp.getAmount());
            data.put("successTime", resp.getSuccessTime());
            return ResponseResult.success(data);
        } catch (IllegalStateException | IllegalArgumentException ex) {
            log.warn("[wallet/pay] biz error: cashierNo={}, userId={}, msg={}",
                    cashierNo, userId, ex.getMessage());
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    /** ================= 私有方法 ================= */

    private String resolveUserId(HttpServletRequest http) {
        String token = http.getHeader("Authorization");
        if (!StringUtils.hasText(token)) {
            token = http.getHeader("token");
        }
        if (!StringUtils.hasText(token)) {
            return null;
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        try {
            UserInfoVO user = ParseTokenUtils.getUserInfoByToken(token);
            return user == null ? null : user.getUserId();
        } catch (Exception e) {
            log.warn("[wallet] token 解析失败: {}", e.getMessage());
            return null;
        }
    }
}
