package com.houjiang.payment.service;

import com.houjiang.payment.constant.CashierOrderStatusEnum;
import com.houjiang.payment.entity.PayCashierOrderEntity;
import com.houjiang.payment.entity.PayChannelCompensateEntity;
import com.houjiang.payment.mapper.PayCashierOrderMapper;
import com.houjiang.payment.mapper.PayChannelCompensateMapper;
import com.houjiang.payment.service.api.impl.CompensateServiceImpl;
import com.houjiang.payment.service.cashier.CashierBizNotifyService;
import org.junit.jupiter.api.Test;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CompensateServiceTest {

    @Test
    void payNotifyTaskResendsOnlyForSuccessfulCashierOrder() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), "CompensateServiceTest"),
                PayChannelCompensateEntity.class);
        PayChannelCompensateMapper compensateMapper = mock(PayChannelCompensateMapper.class);
        PayCashierOrderMapper cashierMapper = mock(PayCashierOrderMapper.class);
        CashierBizNotifyService notifyService = mock(CashierBizNotifyService.class);
        PayChannelCompensateEntity task = new PayChannelCompensateEntity();
        task.setId(1L);
        task.setTaskNo("CT1");
        task.setBizNo("PAY_NOTIFY:CS007");
        task.setTaskStatus(1);
        task.setQueryCount(0);
        task.setMaxQuery(10);
        when(compensateMapper.selectById(1L)).thenReturn(task);
        when(compensateMapper.update(any(), any())).thenReturn(1);
        PayCashierOrderEntity order = new PayCashierOrderEntity();
        order.setCashierNo("CS007");
        order.setStatus(CashierOrderStatusEnum.SUCCESS.getCode());
        when(cashierMapper.selectByCashierNo("CS007")).thenReturn(order);
        when(notifyService.notifyPaySuccess(order)).thenReturn(true);
        CompensateServiceImpl service = new CompensateServiceImpl(
                compensateMapper, cashierMapper, notifyService);

        assertTrue(service.executeOne(1L));

        verify(notifyService).notifyPaySuccess(order);
    }
}
