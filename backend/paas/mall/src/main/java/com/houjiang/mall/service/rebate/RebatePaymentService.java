package com.houjiang.mall.service.rebate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.rebate.RebatePayment;
import com.houjiang.mall.mapper.rebate.RebatePaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 返利发放服务类
 *
 * <p>发放状态：0 待发放 / 1 发放中 / 2 已发放 / 3 发放失败。
 * 2 是终态；3 允许在修好通道后重试；1 是并发保护位。
 *
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RebatePaymentService extends ServiceImpl<RebatePaymentMapper, RebatePayment> {

    /** 待发放 */
    public static final int STATUS_PENDING = 0;

    /** 发放中（占位，防并发重复出款） */
    public static final int STATUS_PAYING = 1;

    /** 已发放（终态） */
    public static final int STATUS_PAID = 2;

    /** 发放失败（可重试） */
    public static final int STATUS_FAILED = 3;

    private final RebatePaymentChannelRouter rebatePaymentChannelRouter;

    /**
     * 分页查询返利发放
     * 
     * @param params 查询参数
     * @return 分页数据
     */
    public IPage<RebatePayment> pageList(Map<String, Object> params) {
        Integer pageNum = (Integer) params.getOrDefault("pageNum", 1);
        Integer pageSize = (Integer) params.getOrDefault("pageSize", 10);
        String paymentNo = (String) params.get("paymentNo");
        Integer paymentMethod = params.get("paymentMethod") != null ? Integer.parseInt(params.get("paymentMethod").toString()) : null;
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;
        Long shopId = params.get("shopId") != null ? Long.parseLong(params.get("shopId").toString()) : null;
        Long userId = params.get("userId") != null ? Long.parseLong(params.get("userId").toString()) : null;

        LambdaQueryWrapper<RebatePayment> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(paymentNo), RebatePayment::getPaymentNo, paymentNo)
               .eq(paymentMethod != null, RebatePayment::getPaymentMethod, paymentMethod)
               .eq(status != null, RebatePayment::getStatus, status)
               .eq(shopId != null, RebatePayment::getShopId, shopId)
               .eq(userId != null, RebatePayment::getUserId, userId)
               .orderByDesc(RebatePayment::getCreateTime);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 创建返利发放
     * 
     * @param payment 发放信息
     * @return 是否成功
     */
    public Boolean createPayment(RebatePayment payment) {
        payment.setPaymentNo(generatePaymentNo());
        // 待发放
        payment.setStatus(STATUS_PENDING);
        payment.setCreateTime(LocalDateTime.now());
        payment.setUpdateTime(LocalDateTime.now());
        return save(payment);
    }

    /**
     * 执行发放
     * 
     * @param id 发放ID
     * @return 是否成功
     */
    public Boolean execute(Long id) {
        RebatePayment payment = getById(id);
        if (payment == null) {
            log.warn("[返利发放] 发放单不存在 id={}", id);
            return false;
        }

        Integer status = payment.getStatus();
        if (status != null && (status == STATUS_PAYING || status == STATUS_PAID)) {
            // 发放中说明有并发在跑，已发放是终态，两者都不能再出一次钱
            log.warn("[返利发放] 发放单 paymentNo={} 当前状态 {} 不允许重复执行", payment.getPaymentNo(), status);
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        // 抢锁：只有 待发放/发放失败 能推进到 发放中，条件更新保证并发下只放行一个线程
        boolean locked = lambdaUpdate()
                .set(RebatePayment::getStatus, STATUS_PAYING)
                .set(RebatePayment::getUpdateTime, now)
                .eq(RebatePayment::getId, id)
                .in(RebatePayment::getStatus, STATUS_PENDING, STATUS_FAILED)
                .update();
        if (!locked) {
            log.warn("[返利发放] 发放单 paymentNo={} 已被其它请求推进，本次跳过", payment.getPaymentNo());
            return false;
        }
        payment.setStatus(STATUS_PAYING);
        payment.setUpdateTime(now);

        RebatePaymentChannel.Result result;
        try {
            result = rebatePaymentChannelRouter.route().pay(payment);
        } catch (Exception e) {
            log.error("[返利发放] 调用发放通道异常 paymentNo={}", payment.getPaymentNo(), e);
            result = RebatePaymentChannel.Result.fail("发放通道异常：" + e.getMessage());
        }

        // 成功且带上游流水号才认；空流水号一律按失败处理，杜绝无凭据的成功记录
        if (result == null || !result.isSuccess() || !StringUtils.hasText(result.getTransactionNo())) {
            String reason = result == null ? "通道未返回结果" : result.getMessage();
            payment.setStatus(STATUS_FAILED);
            payment.setFailReason(reason);
            log.warn("[返利发放] 发放未成功 paymentNo={}, reason={}", payment.getPaymentNo(), reason);
        } else {
            payment.setStatus(STATUS_PAID);
            payment.setPaymentTime(now);
            payment.setTransactionNo(result.getTransactionNo());
            payment.setFailReason(null);
            log.info("[返利发放] 发放成功 paymentNo={}, transactionNo={}",
                    payment.getPaymentNo(), result.getTransactionNo());
        }
        payment.setUpdateTime(LocalDateTime.now());
        return updateById(payment);
    }

    /**
     * 标记发放失败
     * 
     * @param id 发放ID
     * @param failReason 失败原因
     * @return 是否成功
     */
    public Boolean markFailed(Long id, String failReason) {
        RebatePayment payment = new RebatePayment();
        payment.setId(id);
        // 发放失败
        payment.setStatus(STATUS_FAILED);
        payment.setFailReason(failReason);
        payment.setUpdateTime(LocalDateTime.now());
        return updateById(payment);
    }

    /**
     * 生成发放单号
     * 
     * @return 发放单号
     */
    private String generatePaymentNo() {
        return "PAY" + System.currentTimeMillis();
    }
}
