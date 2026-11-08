package com.houjiang.payment.service.cashier;

import com.houjiang.commons.dto.payment.MallRefundRequest;
import com.houjiang.payment.channel.biz.PayChannelStrategyFactory;
import com.houjiang.payment.entity.PayCashierOrderEntity;
import com.houjiang.payment.entity.PayRefundOrderEntity;
import com.houjiang.payment.mapper.PayCashierOrderMapper;
import com.houjiang.payment.mapper.PayRefundOrderMapper;
import com.houjiang.payment.plugin.RefundCreateGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Amos
 */
@RequiredArgsConstructor
@Service
public class MallCashierRefundService {

    private final PayCashierOrderMapper cashierOrderMapper;
    private final PayRefundOrderMapper refundOrderMapper;
    private final PayChannelStrategyFactory strategyFactory;
    private final BalanceRefundService balanceRefundService;
    private final RefundCreateGateway refundCreateGateway;

    public PayRefundOrderEntity refund(MallRefundRequest request) {
        validate(request);
        PayRefundOrderEntity existing = refundOrderMapper.selectByRefundNo(request.getRefundNo());
        if (existing != null) return existing;
        PayCashierOrderEntity cashier = cashierOrderMapper.selectByCashierNo(request.getCashierNo());
        if (cashier == null || !request.getMallOrderNo().equals(cashier.getMallOrderNo())) {
            throw new IllegalStateException("收银台订单不存在或商城订单号不匹配");
        }
        if (cashier.getStatus() == null || cashier.getStatus() != 2) {
            throw new IllegalStateException("只有支付成功的订单可以退款");
        }
        if (request.getAmount().compareTo(cashier.getAmount()) > 0) {
            throw new IllegalArgumentException("退款金额超过支付金额");
        }
        // before* 扩展点：退款单写库 / 渠道退款之前做校验与金额核定。
        // 插件 abort 时抛 PluginAbortException，退款单不会被创建；插件可下修退款金额（夹取在 [0, 实付]）。
        String refundWay = "BALANCE".equalsIgnoreCase(cashier.getPayMethod()) ? "BALANCE" : "ORIGINAL";
        RefundCreateGateway.Decision decision = refundCreateGateway.beforeRefund(
                request.getMallOrderNo(), cashier.getAmount(), request.getAmount(), refundWay,
                request.getReason(), null);
        request.setAmount(decision.refundAmount());
        if ("BALANCE".equalsIgnoreCase(cashier.getPayMethod())) {
            return balanceRefundService.refund(request.getRefundNo(), request.getCashierNo(),
                    request.getMallOrderNo(), cashier.getUserId(), request.getAmount(), request.getReason());
        }

        Date now = new Date();
        PayRefundOrderEntity refund = new PayRefundOrderEntity();
        refund.setRefundNo(request.getRefundNo());
        refund.setCashierNo(request.getCashierNo());
        refund.setMallOrderNo(request.getMallOrderNo());
        refund.setAmount(request.getAmount());
        refund.setReason(request.getReason());
        refund.setStatus("PROCESSING");
        refund.setCreateTime(now);
        refund.setUpdateTime(now);
        if (refundOrderMapper.insertIfAbsent(refund) != 1) {
            return refundOrderMapper.selectByRefundNo(request.getRefundNo());
        }
        try {
            Map<String, Object> channelRequest = new LinkedHashMap<>();
            channelRequest.put("channelOrderNo", cashier.getCashierNo());
            channelRequest.put("outTradeNo", cashier.getCashierNo());
            channelRequest.put("refundNo", request.getRefundNo());
            channelRequest.put("refundAmount", request.getAmount());
            channelRequest.put("totalAmount", cashier.getAmount());
            channelRequest.put("refundReason", request.getReason());
            String strategyCode = "WECHAT".equalsIgnoreCase(cashier.getPayMethod())
                    ? "WECHAT_V3" : cashier.getPayMethod();
            Map<String, Object> result = strategyFactory.require(strategyCode).refund(channelRequest);
            refund.setChannelRefundNo(text(result.get("refundOrderNo")));
            refund.setStatus("SUCCESS");
            refund.setSuccessTime(new Date());
        } catch (RuntimeException ex) {
            refund.setStatus("FAILED");
            refund.setFailureReason(ex.getMessage());
            throw ex;
        } finally {
            refund.setUpdateTime(new Date());
            refundOrderMapper.updateById(refund);
        }
        return refund;
    }

    private void validate(MallRefundRequest request) {
        if (request == null || request.getRefundNo() == null || request.getRefundNo().isBlank()
                || request.getCashierNo() == null || request.getCashierNo().isBlank()
                || request.getMallOrderNo() == null || request.getMallOrderNo().isBlank()
                || request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("退款参数不完整");
        }
    }

    private String text(Object value) {
        return value == null ? null : value.toString();
    }
}
