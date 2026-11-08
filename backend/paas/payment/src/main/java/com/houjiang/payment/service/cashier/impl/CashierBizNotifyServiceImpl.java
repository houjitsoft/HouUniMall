package com.houjiang.payment.service.cashier.impl;

import com.alibaba.fastjson.JSONObject;
import com.houjiang.payment.entity.PayCashierOrderEntity;
import com.houjiang.payment.service.cashier.CashierBizNotifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.houjiang.commons.http.OutboundHttpClientFactory;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 收银台业务回调默认实现（HTTP 透传）
 *
 * <p>读取订单 {@code notifyUrl}，将支付成功事件转发到业务方 HTTP 端点。
 * 若订单未携带 notifyUrl，或业务方返回非 2xx，直接返回 false 让上层处理（记录 callback_log）。
 * 此实现不抛异常，保障 callback 入口流程不被阻塞。
 *
 * @author Amos
 */
@Slf4j
@Service
public class CashierBizNotifyServiceImpl implements CashierBizNotifyService {

    private final RestTemplate restTemplate = OutboundHttpClientFactory.restTemplateDefault();

    /**
     * 支付成功通知业务方
     * @param order 收银台订单
     * @return true=业务方 2xx 响应
     */
    @Override
    public boolean notifyPaySuccess(PayCashierOrderEntity order) {
        if (order == null) {
            return false;
        }
        String notifyUrl = order.getNotifyUrl();
        if (!StringUtils.hasText(notifyUrl)) {
            log.info("[bizNotify] skip, notifyUrl empty, cashierNo={}", order.getCashierNo());
            return true;
        }
        try {
            JSONObject body = new JSONObject();
            body.put("cashierNo", order.getCashierNo());
            body.put("mchOrderNo", order.getMchOrderNo());
            body.put("mallOrderNo", StringUtils.hasText(order.getMallOrderNo())
                    ? order.getMallOrderNo() : order.getMchOrderNo());
            body.put("requestNo", order.getRequestNo());
            body.put("payOrderId", order.getPayOrderId());
            body.put("channelOrderNo", order.getPayOrderId());
            body.put("payMethod", order.getPayMethod());
            body.put("terminal", order.getTerminal());
            body.put("amount", order.getAmount());
            body.put("status", order.getStatus());
            body.put("successTime", order.getSuccessTime());
            if (order.getSuccessTime() != null) {
                LocalDateTime paidTime = LocalDateTime.ofInstant(
                        order.getSuccessTime().toInstant(), ZoneId.systemDefault());
                body.put("paidTime", paidTime.toString());
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(body.toJSONString(), headers);

            ResponseEntity<String> resp = restTemplate.postForEntity(notifyUrl, entity, String.class);
            boolean ok = resp.getStatusCode().is2xxSuccessful();
            log.info("[bizNotify] post {}, cashierNo={}, status={}, body={}",
                    notifyUrl, order.getCashierNo(), resp.getStatusCode(), resp.getBody());
            return ok;
        } catch (Exception e) {
            log.warn("[bizNotify] fail, cashierNo={}, notifyUrl={}, err={}",
                    order.getCashierNo(), notifyUrl, e.getMessage());
            return false;
        }
    }
}
