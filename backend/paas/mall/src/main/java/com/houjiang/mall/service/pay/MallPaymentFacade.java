package com.houjiang.mall.service.pay;

import com.houjiang.commons.dto.payment.MallCashierCreateRequest;
import com.houjiang.commons.dto.payment.MallCashierCreateResponse;
import com.houjiang.commons.dto.payment.MallCashierStatusResponse;
import com.houjiang.commons.dto.payment.MallRefundRequest;
import com.houjiang.commons.exception.DefinedException;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.dto.pay.MallPayCreateParam;
import com.houjiang.mall.feign.OrderPaasFeign;
import com.houjiang.mall.service.setting.SettingPayWayService;
import com.houjiang.mall.vo.setting.SettingPaymentMethodVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@RequiredArgsConstructor
@Service
public class MallPaymentFacade {

    private final OrderPaasFeign orderFeign;
    private final SettingPayWayService payWayService;

    public MallCashierCreateResponse create(MallPayCreateParam param, String userId) {
        MallCashierCreateRequest request = new MallCashierCreateRequest();
        request.setMallOrderNo(param.getMallOrderNo());
        request.setRequestNo("PAY-" + param.getMallOrderNo() + "-1");
        request.setUserId(userId);
        request.setPayMethod(param.getPayMethod());
        request.setTerminal(param.getTerminal());
        request.setChannelCode(param.getChannelCode());
        request.setOpenId(param.getOpenId());
        request.setClientIp(param.getClientIp());
        request.setReturnUrl(param.getReturnUrl());
        return requireData(orderFeign.createMallCashier(request));
    }

    public MallCashierStatusResponse status(String cashierNo, String userId) {
        return requireData(orderFeign.mallCashierStatus(cashierNo, userId));
    }

    public Map<Integer, List<SettingPaymentMethodVo>> ways(Integer scene) {
        Map<Integer, List<SettingPaymentMethodVo>> all = payWayService.list();
        return scene == null ? all : Map.of(scene, all.getOrDefault(scene, List.of()));
    }

    public Object refund(MallRefundRequest request, String userId) {
        return requireData(orderFeign.refundMallPayment(request, userId));
    }

    public String balance(String userId) {
        return requireData(orderFeign.mallBalance(userId));
    }

    private <T> T requireData(ResponseResult<T> response) {
        if (response == null || response.getCode() != 1) {
            throw new DefinedException(response == null ? "下游服务无响应" : response.getMessage());
        }
        return response.getData();
    }
}
