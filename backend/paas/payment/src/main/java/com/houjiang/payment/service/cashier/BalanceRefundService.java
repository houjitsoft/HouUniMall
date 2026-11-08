package com.houjiang.payment.service.cashier;

import com.houjiang.payment.entity.PayRefundOrderEntity;
import com.houjiang.payment.mapper.PayRefundOrderMapper;
import com.houjiang.payment.mapper.PayUserResourceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Amos
 */
@RequiredArgsConstructor
@Service
public class BalanceRefundService {

    private final PayRefundOrderMapper refundOrderMapper;
    private final PayUserResourceMapper userResourceMapper;

    @Transactional(rollbackFor = Exception.class)
    public PayRefundOrderEntity refund(String refundNo, String cashierNo, String mallOrderNo,
                                       String userId, BigDecimal amount, String reason) {
        PayRefundOrderEntity existing = refundOrderMapper.selectByRefundNo(refundNo);
        if (existing != null) return existing;

        Date now = new Date();
        PayRefundOrderEntity refund = new PayRefundOrderEntity();
        refund.setRefundNo(refundNo);
        refund.setCashierNo(cashierNo);
        refund.setMallOrderNo(mallOrderNo);
        refund.setAmount(amount);
        refund.setReason(reason);
        refund.setStatus("PROCESSING");
        refund.setCreateTime(now);
        refund.setUpdateTime(now);
        if (refundOrderMapper.insertIfAbsent(refund) != 1) {
            return refundOrderMapper.selectByRefundNo(refundNo);
        }
        if (userResourceMapper.creditBalance(userId, amount) != 1) {
            throw new IllegalStateException("Balance refund account does not exist");
        }
        refund.setStatus("SUCCESS");
        refund.setSuccessTime(new Date());
        refund.setUpdateTime(refund.getSuccessTime());
        if (refundOrderMapper.updateById(refund) != 1) {
            throw new IllegalStateException("Balance refund status update failed");
        }
        return refund;
    }
}
