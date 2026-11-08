package com.houjiang.merchant.service.impl.mall;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.entity.mall.MallRefund;
import com.houjiang.merchant.common.enums.RefundEnum;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.mall.MallRefundMapper;
import com.houjiang.merchant.service.mall.IMallRefundLogService;
import com.houjiang.merchant.validate.mall.MallRefundReRefundValidate;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MallRefundPlatformScopeTest {

    @Test
    void nullShopIdKeepsPlatformRefundQueriesUnscoped() {
        QueryWrapper<MallRefund> platform = MallRefundServiceImpl.applyShopScope(new QueryWrapper<MallRefund>(), null);
        QueryWrapper<MallRefund> merchant = MallRefundServiceImpl.applyShopScope(new QueryWrapper<MallRefund>(), 8);

        assertFalse(platform.getSqlSegment().contains("shop_id"), platform.getSqlSegment());
        assertTrue(merchant.getSqlSegment().contains("shop_id"), merchant.getSqlSegment());
    }

    @Test
    void waitingAmountContainsOnlyWaitingRefunds() {
        MallRefundMapper mapper = mock(MallRefundMapper.class);
        MallRefundServiceImpl service = new MallRefundServiceImpl();
        ReflectionTestUtils.setField(service, "mallRefundMapper", mapper);
        when(mapper.sum(eq("money"), any())).thenReturn(BigDecimal.TEN);

        service.getRefundWaitMoneyByShopId(null);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<QueryWrapper<MallRefund>> wrapper = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).sum(eq("money"), wrapper.capture());
        wrapper.getValue().getSqlSegment();
        Map<String, Object> values = wrapper.getValue().getParamNameValuePairs();
        assertTrue(values.containsValue(RefundEnum.STATUS_WAIT_RETURN.getCode()), values.toString());
        assertFalse(values.containsValue(RefundEnum.STATUS_FAILED.getCode()), values.toString());
    }

    @Test
    void retryUsesAtomicFailedToWaitingTransition() {
        MallRefundMapper mapper = mock(MallRefundMapper.class);
        IMallRefundLogService logService = mock(IMallRefundLogService.class);
        MallRefundServiceImpl service = new MallRefundServiceImpl();
        ReflectionTestUtils.setField(service, "mallRefundMapper", mapper);
        ReflectionTestUtils.setField(service, "iMallRefundLogService", logService);

        MallRefund refund = new MallRefund();
        refund.setId(9);
        refund.setShopId(3);
        refund.setStatus(RefundEnum.STATUS_FAILED.getCode());
        refund.setMoney(BigDecimal.ONE);
        when(mapper.selectOne(any())).thenReturn(refund);
        when(mapper.update(any(), any())).thenReturn(0);

        MallRefundReRefundValidate validate = new MallRefundReRefundValidate();
        validate.setId(9);
        validate.setShopId(3);

        assertThrows(OperateException.class, () -> service.reRefund(validate));
        verify(mapper).update(eq(null), any());
        verify(mapper, never()).updateById(any(MallRefund.class));
        verify(logService, never()).createLog(any());
    }
}
