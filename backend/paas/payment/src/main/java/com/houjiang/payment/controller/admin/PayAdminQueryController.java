package com.houjiang.payment.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.entity.*;
import com.houjiang.payment.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 支付平台运营查询 Controller（管理端）
 *
 * <p>为支付中心后台（/payment/dashboard 各列表页）提供真实数据分页查询：
 * 支付订单、退款单、充值记录、提现记录、资金流水、账户余额等。
 * 数据源为 MyBatis-Plus 直查，避免依赖历史 stub 端点。</p>
 *
 * <p>BFF 透传路径：/web/payment/admin/**</p>
 *
 * @author Amos
 * @since 2026-06
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/admin")
@RequiredArgsConstructor
public class PayAdminQueryController {

    private final PayConsumeTradeMapper tradeMapper;
    private final PayRefundOrderMapper refundOrderMapper;
    private final PayConsumeDepositMapper depositMapper;
    private final PayConsumeWithdrawMapper withdrawMapper;
    private final PayConsumeRecordMapper recordMapper;
    private final PayUserResourceMapper userResourceMapper;

    // ==================== 支付订单（hou_pay_consume_trade） ====================

    /**
     * 支付订单分页
     * 参数：page/pageNum、size/pageSize、orderId、consumeTradeId、orderStateId、paymentChannelId、startDate、endDate
     */
    @PostMapping("/trade/page")
    public ResponseResult<IPage<PayConsumeTradeEntity>> tradePage(@RequestBody(required = false) Map<String, Object> param) {
        if (param == null) param = new java.util.HashMap<>();
        long pageNum = longVal(param, "pageNum", "page", 1);
        long pageSize = longVal(param, "pageSize", "size", 10);
        LambdaQueryWrapper<PayConsumeTradeEntity> w = Wrappers.lambdaQuery();
        String orderId = strVal(param, "orderId");
        if (StringUtils.hasText(orderId)) w.eq(PayConsumeTradeEntity::getOrderId, orderId);
        String consumeTradeId = strVal(param, "consumeTradeId");
        if (StringUtils.hasText(consumeTradeId)) w.eq(PayConsumeTradeEntity::getConsumeTradeId, consumeTradeId);
        Integer state = intVal(param, "orderStateId");
        if (state != null) w.eq(PayConsumeTradeEntity::getOrderStateId, state);
        Integer channel = intVal(param, "paymentChannelId");
        if (channel != null) w.eq(PayConsumeTradeEntity::getPaymentChannelId, channel);
        String startDate = strVal(param, "startDate");
        String endDate = strVal(param, "endDate");
        if (StringUtils.hasText(startDate)) w.ge(PayConsumeTradeEntity::getTradeCreateTime, startDate);
        if (StringUtils.hasText(endDate)) w.le(PayConsumeTradeEntity::getTradeCreateTime, endDate);
        w.orderByDesc(PayConsumeTradeEntity::getTradeCreateTime);
        IPage<PayConsumeTradeEntity> page = tradeMapper.selectPage(new Page<>(pageNum, pageSize), w);
        return ResponseResult.success(page);
    }

    /**
     * 支付订单详情
     */
    @PostMapping("/trade/detail")
    public ResponseResult<PayConsumeTradeEntity> tradeDetail(@RequestBody Map<String, Object> param) {
        String consumeTradeId = strVal(param, "consumeTradeId");
        String orderId = strVal(param, "orderId");
        PayConsumeTradeEntity one = null;
        if (StringUtils.hasText(consumeTradeId)) {
            one = tradeMapper.selectOne(Wrappers.<PayConsumeTradeEntity>lambdaQuery()
                    .eq(PayConsumeTradeEntity::getConsumeTradeId, consumeTradeId).last("limit 1"));
        } else if (StringUtils.hasText(orderId)) {
            one = tradeMapper.selectOne(Wrappers.<PayConsumeTradeEntity>lambdaQuery()
                    .eq(PayConsumeTradeEntity::getOrderId, orderId).last("limit 1"));
        }
        if (one == null) return ResponseResult.fail(404, "支付订单不存在");
        return ResponseResult.success(one);
    }

    // ==================== 退款单（hou_pay_refund_order） ====================

    /**
     * 退款单分页
     * 参数：page/pageNum、size/pageSize、refundNo、cashierNo、mallOrderNo|orderNo、status、startDate、endDate
     */
    @PostMapping("/refund/page")
    public ResponseResult<IPage<PayRefundOrderEntity>> refundPage(@RequestBody(required = false) Map<String, Object> param) {
        if (param == null) param = new java.util.HashMap<>();
        long pageNum = longVal(param, "pageNum", "page", 1);
        long pageSize = longVal(param, "pageSize", "size", 10);
        LambdaQueryWrapper<PayRefundOrderEntity> w = Wrappers.lambdaQuery();
        String refundNo = strVal(param, "refundNo");
        if (StringUtils.hasText(refundNo)) w.eq(PayRefundOrderEntity::getRefundNo, refundNo);
        String cashierNo = strVal(param, "cashierNo");
        if (StringUtils.hasText(cashierNo)) w.eq(PayRefundOrderEntity::getCashierNo, cashierNo);
        String mallOrderNo = firstStr(param, "mallOrderNo", "orderNo");
        if (StringUtils.hasText(mallOrderNo)) w.eq(PayRefundOrderEntity::getMallOrderNo, mallOrderNo);
        String status = strVal(param, "status");
        if (StringUtils.hasText(status)) w.eq(PayRefundOrderEntity::getStatus, status);
        String startDate = strVal(param, "startDate");
        String endDate = strVal(param, "endDate");
        if (StringUtils.hasText(startDate)) w.ge(PayRefundOrderEntity::getCreateTime, startDate);
        if (StringUtils.hasText(endDate)) w.le(PayRefundOrderEntity::getCreateTime, endDate + " 23:59:59");
        w.orderByDesc(PayRefundOrderEntity::getCreateTime);
        IPage<PayRefundOrderEntity> page = refundOrderMapper.selectPage(new Page<>(pageNum, pageSize), w);
        return ResponseResult.success(page);
    }

    // ==================== 充值记录（hou_pay_consume_deposit） ====================

    /**
     * 充值记录分页
     * 参数：page/pageNum、size/pageSize、depositTradeNo|rechargeNo、depositBuyerId|userId、
     * depositPayChannel|channel、depositTradeStatus|status、startDate、endDate
     */
    @PostMapping("/deposit/page")
    public ResponseResult<IPage<PayConsumeDepositEntity>> depositPage(@RequestBody(required = false) Map<String, Object> param) {
        if (param == null) param = new java.util.HashMap<>();
        long pageNum = longVal(param, "pageNum", "page", 1);
        long pageSize = longVal(param, "pageSize", "size", 10);
        LambdaQueryWrapper<PayConsumeDepositEntity> w = Wrappers.lambdaQuery();
        String tradeNo = firstStr(param, "depositTradeNo", "rechargeNo");
        if (StringUtils.hasText(tradeNo)) w.eq(PayConsumeDepositEntity::getDepositTradeNo, tradeNo);
        String buyerId = firstStr(param, "depositBuyerId", "userId");
        if (StringUtils.hasText(buyerId)) w.eq(PayConsumeDepositEntity::getDepositBuyerId, buyerId);
        Integer channel = firstInt(param, "depositPayChannel", "channel");
        if (channel != null) w.eq(PayConsumeDepositEntity::getDepositPayChannel, channel);
        // 前端 status: 1待支付 2已支付；实体 depositTradeStatus 为 Boolean
        Boolean status = boolVal(param, "depositTradeStatus");
        if (status == null) {
            Integer statusCode = intVal(param, "status");
            if (statusCode != null) {
                if (statusCode == 2) status = Boolean.TRUE;
                else if (statusCode == 1) status = Boolean.FALSE;
            }
        }
        if (status != null) w.eq(PayConsumeDepositEntity::getDepositTradeStatus, status);
        String startDate = strVal(param, "startDate");
        String endDate = strVal(param, "endDate");
        if (StringUtils.hasText(startDate)) w.ge(PayConsumeDepositEntity::getDepositGmtCreate, startDate);
        if (StringUtils.hasText(endDate)) w.le(PayConsumeDepositEntity::getDepositGmtCreate, endDate + " 23:59:59");
        w.orderByDesc(PayConsumeDepositEntity::getDepositGmtCreate);
        IPage<PayConsumeDepositEntity> page = depositMapper.selectPage(new Page<>(pageNum, pageSize), w);
        return ResponseResult.success(page);
    }

    /**
     * 充值记录详情
     * 参数：id（主键）或 depositTradeNo
     */
    @PostMapping("/deposit/detail")
    public ResponseResult<PayConsumeDepositEntity> depositDetail(@RequestBody Map<String, Object> param) {
        Long id = param == null ? null : longObjVal(param, "id");
        if (id != null) {
            PayConsumeDepositEntity one = depositMapper.selectById(id);
            if (one != null) return ResponseResult.success(one);
        }
        String tradeNo = strVal(param, "depositTradeNo");
        if (StringUtils.hasText(tradeNo)) {
            PayConsumeDepositEntity one = depositMapper.selectOne(Wrappers.<PayConsumeDepositEntity>lambdaQuery()
                    .eq(PayConsumeDepositEntity::getDepositTradeNo, tradeNo).last("limit 1"));
            if (one != null) return ResponseResult.success(one);
        }
        return ResponseResult.fail(404, "充值记录不存在");
    }

    // ==================== 提现记录（hou_pay_consume_withdraw） ====================

    /**
     * 提现记录分页
     * 参数：page/pageNum、size/pageSize、payUid|userId、orderid|withdrawNo、isSucceed|status、startDate、endDate
     */
    @PostMapping("/withdraw/page")
    public ResponseResult<IPage<PayConsumeWithdrawEntity>> withdrawPage(@RequestBody(required = false) Map<String, Object> param) {
        if (param == null) param = new java.util.HashMap<>();
        long pageNum = longVal(param, "pageNum", "page", 1);
        long pageSize = longVal(param, "pageSize", "size", 10);
        LambdaQueryWrapper<PayConsumeWithdrawEntity> w = Wrappers.lambdaQuery();
        String payUid = firstStr(param, "payUid", "userId");
        if (StringUtils.hasText(payUid)) w.eq(PayConsumeWithdrawEntity::getPayUid, payUid);
        String orderid = firstStr(param, "orderid", "withdrawNo");
        if (StringUtils.hasText(orderid)) w.eq(PayConsumeWithdrawEntity::getOrderid, orderid);
        Integer succeed = intVal(param, "isSucceed");
        if (succeed == null) {
            // 前端枚举：1待审核 3处理中 4成功 5失败 → 库字段 isSucceed：0/3/2/4
            Integer uiStatus = intVal(param, "status");
            if (uiStatus != null) {
                switch (uiStatus) {
                    case 1 -> succeed = 0;
                    case 3 -> succeed = 3;
                    case 4 -> succeed = 2;
                    case 5, 6 -> succeed = 4;
                    default -> { /* 忽略未映射的 UI 状态 */ }
                }
            }
        }
        if (succeed != null) w.eq(PayConsumeWithdrawEntity::getIsSucceed, succeed);
        String startDate = strVal(param, "startDate");
        String endDate = strVal(param, "endDate");
        if (StringUtils.hasText(startDate)) w.ge(PayConsumeWithdrawEntity::getAddTime, startDate);
        if (StringUtils.hasText(endDate)) w.le(PayConsumeWithdrawEntity::getAddTime, endDate + " 23:59:59");
        w.orderByDesc(PayConsumeWithdrawEntity::getAddTime);
        IPage<PayConsumeWithdrawEntity> page = withdrawMapper.selectPage(new Page<>(pageNum, pageSize), w);
        return ResponseResult.success(page);
    }

    /**
     * 提现记录详情
     * 参数：id（主键）或 orderid（提现订单号）
     */
    @PostMapping("/withdraw/detail")
    public ResponseResult<PayConsumeWithdrawEntity> withdrawDetail(@RequestBody Map<String, Object> param) {
        Integer id = param == null ? null : intVal(param, "id");
        if (id != null) {
            PayConsumeWithdrawEntity one = withdrawMapper.selectById(id);
            if (one != null) return ResponseResult.success(one);
        }
        String orderid = strVal(param, "orderid");
        if (StringUtils.hasText(orderid)) {
            PayConsumeWithdrawEntity one = withdrawMapper.selectOne(Wrappers.<PayConsumeWithdrawEntity>lambdaQuery()
                    .eq(PayConsumeWithdrawEntity::getOrderid, orderid).last("limit 1"));
            if (one != null) return ResponseResult.success(one);
        }
        return ResponseResult.fail(404, "提现记录不存在");
    }

    // ==================== 资金流水（hou_pay_consume_record） ====================

    /**
     * 资金流水分页
     * 参数：page/pageNum、size/pageSize、userId、recordPayorder、tradeTypeId、startDate、endDate
     */
    @PostMapping("/record/page")
    public ResponseResult<IPage<PayConsumeRecordEntity>> recordPage(@RequestBody(required = false) Map<String, Object> param) {
        if (param == null) param = new java.util.HashMap<>();
        long pageNum = longVal(param, "pageNum", "page", 1);
        long pageSize = longVal(param, "pageSize", "size", 10);
        LambdaQueryWrapper<PayConsumeRecordEntity> w = Wrappers.lambdaQuery();
        String userId = strVal(param, "userId");
        if (StringUtils.hasText(userId)) w.eq(PayConsumeRecordEntity::getUserId, userId);
        String payorder = strVal(param, "recordPayorder");
        if (StringUtils.hasText(payorder)) w.eq(PayConsumeRecordEntity::getRecordPayorder, payorder);
        Integer tradeTypeId = intVal(param, "tradeTypeId");
        if (tradeTypeId != null) w.eq(PayConsumeRecordEntity::getTradeTypeId, tradeTypeId);
        String startDate = strVal(param, "startDate");
        String endDate = strVal(param, "endDate");
        if (StringUtils.hasText(startDate)) w.ge(PayConsumeRecordEntity::getRecordTime, startDate);
        if (StringUtils.hasText(endDate)) w.le(PayConsumeRecordEntity::getRecordTime, endDate);
        w.orderByDesc(PayConsumeRecordEntity::getRecordTime);
        IPage<PayConsumeRecordEntity> page = recordMapper.selectPage(new Page<>(pageNum, pageSize), w);
        return ResponseResult.success(page);
    }

    // ==================== 账户余额（hou_pay_user_resource） ====================

    /**
     * 账户余额分页
     * 参数：page/pageNum、size/pageSize、userId
     */
    @PostMapping("/balance/page")
    public ResponseResult<IPage<PayUserResourceEntity>> balancePage(@RequestBody(required = false) Map<String, Object> param) {
        if (param == null) param = new java.util.HashMap<>();
        long pageNum = longVal(param, "pageNum", "page", 1);
        long pageSize = longVal(param, "pageSize", "size", 10);
        LambdaQueryWrapper<PayUserResourceEntity> w = Wrappers.lambdaQuery();
        String userId = strVal(param, "userId");
        if (StringUtils.hasText(userId)) w.eq(PayUserResourceEntity::getUserId, userId);
        w.orderByDesc(PayUserResourceEntity::getTs);
        IPage<PayUserResourceEntity> page = userResourceMapper.selectPage(new Page<>(pageNum, pageSize), w);
        return ResponseResult.success(page);
    }

    // ==================== 辅助 ====================

    private static String strVal(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return null;
        String s = v.toString().trim();
        return s.isEmpty() ? null : s;
    }

    /** 按优先级取第一个非空字符串（兼容前端别名） */
    private static String firstStr(Map<String, Object> m, String... keys) {
        if (keys == null) return null;
        for (String key : keys) {
            String v = strVal(m, key);
            if (StringUtils.hasText(v)) return v;
        }
        return null;
    }

    private static Integer intVal(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v == null || "".equals(v.toString().trim())) return null;
        try { return Integer.valueOf(v.toString().trim()); } catch (NumberFormatException e) { return null; }
    }

    private static Integer firstInt(Map<String, Object> m, String... keys) {
        if (keys == null) return null;
        for (String key : keys) {
            Integer v = intVal(m, key);
            if (v != null) return v;
        }
        return null;
    }

    private static Boolean boolVal(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return null;
        if (v instanceof Boolean) return (Boolean) v;
        return "1".equals(v.toString()) || "true".equalsIgnoreCase(v.toString());
    }

    private static Long longObjVal(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return null;
        try { return Long.valueOf(v.toString()); } catch (NumberFormatException e) { return null; }
    }

    private static long longVal(Map<String, Object> m, String key, String alias, long def) {
        Object v = m.get(key);
        if (v == null) v = m.get(alias);
        if (v == null) return def;
        try { return Long.parseLong(v.toString()); } catch (NumberFormatException e) { return def; }
    }
}
