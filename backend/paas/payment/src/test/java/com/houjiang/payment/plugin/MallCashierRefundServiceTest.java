package com.houjiang.payment.plugin;

import com.houjiang.commons.dto.payment.MallRefundRequest;
import com.houjiang.payment.channel.biz.ChannelPayStrategy;
import com.houjiang.payment.channel.biz.PayChannelStrategyFactory;
import com.houjiang.payment.entity.PayCashierOrderEntity;
import com.houjiang.payment.entity.PayRefundOrderEntity;
import com.houjiang.payment.mapper.PayCashierOrderMapper;
import com.houjiang.payment.mapper.PayRefundOrderMapper;
import com.houjiang.payment.service.cashier.BalanceRefundService;
import com.houjiang.payment.service.cashier.MallCashierRefundService;
import com.houjiang.plugin.api.extension.payment.RefundCreateExtension;
import com.houjiang.plugin.engine.config.PluginEngineProperties;
import com.houjiang.plugin.engine.registry.ExtensionInvoker;
import com.houjiang.plugin.engine.registry.ExtensionRegistry;
import com.houjiang.plugin.runtime.PluginAbortException;
import com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 退款链路真实插入点验证：{@code MallCashierRefundService#refund} 在写库（insertIfAbsent）前调用
 * {@link RefundCreateGateway}。
 *
 * <ul>
 *   <li>插件 abort → 抛 {@link PluginAbortException}，<b>退款单不被创建</b>（insertIfAbsent 零调用）；</li>
 *   <li>插件改写金额 → 领域夹取后写库金额被采纳。</li>
 * </ul>
 */
class MallCashierRefundServiceTest {

    private static final BigDecimal PAID = new BigDecimal("100.00");

    private static ExtensionGatewaySupport support(RefundCreateExtension... extensions) {
        PluginEngineProperties properties = new PluginEngineProperties();
        properties.setExtensionTimeoutMs(0);
        ExtensionRegistry registry = new ExtensionRegistry();
        int priority = 0;
        for (RefundCreateExtension extension : extensions) {
            registry.register(RefundCreateExtension.class, extension, "custom-" + priority, priority, "official");
            priority += 10;
        }
        return new ExtensionGatewaySupport(new ExtensionInvoker(registry, properties));
    }

    private static PayCashierOrderEntity successCashier() {
        PayCashierOrderEntity cashier = new PayCashierOrderEntity();
        cashier.setCashierNo("C1");
        cashier.setMallOrderNo("MALL1");
        cashier.setPayMethod("WECHAT");
        cashier.setAmount(PAID);
        cashier.setStatus(2);
        cashier.setUserId("U1");
        return cashier;
    }

    private MallCashierRefundService service(ExtensionGatewaySupport support,
                                            PayCashierOrderMapper cashierMapper,
                                            PayRefundOrderMapper refundMapper) {
        PayChannelStrategyFactory factory = mock(PayChannelStrategyFactory.class);
        ChannelPayStrategy strategy = mock(ChannelPayStrategy.class);
        when(factory.require(anyString())).thenReturn(strategy);
        when(strategy.refund(anyMap())).thenReturn(Map.of("refundOrderNo", "CH-1"));
        return new MallCashierRefundService(cashierMapper, refundMapper, factory,
                mock(BalanceRefundService.class), new RefundCreateGateway(support));
    }

    private static MallRefundRequest request(BigDecimal amount) {
        MallRefundRequest request = new MallRefundRequest();
        request.setRefundNo("R1");
        request.setCashierNo("C1");
        request.setMallOrderNo("MALL1");
        request.setAmount(amount);
        request.setReason("用户申请");
        return request;
    }

    @Test
    void 插件abort时退款单不被创建() {
        PayCashierOrderMapper cashierMapper = mock(PayCashierOrderMapper.class);
        PayRefundOrderMapper refundMapper = mock(PayRefundOrderMapper.class);
        when(cashierMapper.selectByCashierNo("C1")).thenReturn(successCashier());
        when(refundMapper.selectByRefundNo("R1")).thenReturn(null);
        MallCashierRefundService service = service(support(ctx -> ctx.abort("风控拦截")), cashierMapper, refundMapper);

        PluginAbortException ex = assertThrows(PluginAbortException.class,
                () -> service.refund(request(new BigDecimal("30.00"))));

        assertEquals("RefundCreate", ex.getPoint());
        verify(refundMapper, never()).insertIfAbsent(any(PayRefundOrderEntity.class));
        verify(refundMapper, never()).updateById(any(PayRefundOrderEntity.class));
    }

    @Test
    void 插件下修金额后写库金额被采纳() {
        PayCashierOrderMapper cashierMapper = mock(PayCashierOrderMapper.class);
        PayRefundOrderMapper refundMapper = mock(PayRefundOrderMapper.class);
        when(cashierMapper.selectByCashierNo("C1")).thenReturn(successCashier());
        when(refundMapper.selectByRefundNo("R1")).thenReturn(null);
        when(refundMapper.insertIfAbsent(any(PayRefundOrderEntity.class))).thenReturn(1);
        when(refundMapper.updateById(any(PayRefundOrderEntity.class))).thenReturn(1);
        MallCashierRefundService service = service(
                support(ctx -> ctx.refundAmount = new BigDecimal("12.34")), cashierMapper, refundMapper);

        PayRefundOrderEntity refund = service.refund(request(new BigDecimal("30.00")));

        ArgumentCaptor<PayRefundOrderEntity> captor = ArgumentCaptor.forClass(PayRefundOrderEntity.class);
        verify(refundMapper, times(1)).insertIfAbsent(captor.capture());
        assertEquals(0, new BigDecimal("12.34").compareTo(captor.getValue().getAmount()));
        assertEquals("SUCCESS", refund.getStatus());
    }
}
