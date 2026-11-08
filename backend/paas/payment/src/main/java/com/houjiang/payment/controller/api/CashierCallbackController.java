package com.houjiang.payment.controller.api;

import com.houjiang.payment.service.cashier.CashierCallbackService;
import com.houjiang.payment.service.cashier.WxJsapiPayHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 支付渠道异步通知入口
 *
 * <p>当前支持：
 * <ul>
 *   <li>POST /api/payment/callback/wx/jsapi 微信 JSAPI 通知（XML 报文）</li>
 *   <li>POST /api/payment/callback/stripe/notify Stripe Webhook（JSON 报文）</li>
 * </ul>
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/callback")
@RequiredArgsConstructor
public class CashierCallbackController {

    private final CashierCallbackService cashierCallbackService;
    private final WxJsapiPayHandler wxJsapiPayHandler;

    /**
     * 微信 JSAPI 支付异步通知
     * 微信会以 XML 格式 POST 到本接口，须返回 XML（SUCCESS/FAIL）
     *
     * @param rawXml 微信回传的 XML 原文
     * @return 微信期望的应答 XML
     */
    @PostMapping(value = "/wx/jsapi",
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE, MediaType.ALL_VALUE},
            produces = MediaType.APPLICATION_XML_VALUE)
    public String wxJsapiNotify(@RequestBody String rawXml) {
        log.info("[wxJsapiNotify] recv xml length={}", rawXml == null ? 0 : rawXml.length());
        try {
            Map<String, String> map = WxJsapiPayHandler.parseXml(rawXml);
            return cashierCallbackService.handleWxJsapiNotify(rawXml, map);
        } catch (Exception e) {
            log.error("[wxJsapiNotify] error, err={}", e.getMessage(), e);
            return wxJsapiPayHandler.buildNotifyResponse(false, "server error");
        }
    }

    /**
     * Stripe Webhook 异步通知。
     * Dashboard 应配置为：{domain}/api/payment/callback/stripe/notify
     */
    @PostMapping(value = "/stripe/notify",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.ALL_VALUE})
    public ResponseEntity<String> stripeNotify(@RequestBody String payload,
                                               @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {
        log.info("[stripeNotify] recv payload length={}, hasSig={}",
                payload == null ? 0 : payload.length(), sigHeader != null);
        try {
            String result = cashierCallbackService.handleStripeNotify(payload, sigHeader);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn("[stripeNotify] bad request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("[stripeNotify] error, err={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("server error");
        }
    }
}
