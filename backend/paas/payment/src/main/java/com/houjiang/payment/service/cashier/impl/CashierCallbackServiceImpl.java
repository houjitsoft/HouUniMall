package com.houjiang.payment.service.cashier.impl;

import com.houjiang.payment.channel.biz.impl.StripePayStrategy;
import com.houjiang.payment.constant.CashierOrderStatusEnum;
import com.houjiang.payment.dto.channel.CompensateRegisterRequest;
import com.houjiang.payment.entity.PayCallbackLogEntity;
import com.houjiang.payment.entity.PayCashierOrderEntity;
import com.houjiang.payment.mapper.PayCallbackLogMapper;
import com.houjiang.payment.mapper.PayCashierOrderMapper;
import com.houjiang.payment.plugin.PayCallbackGateway;
import com.houjiang.payment.plugin.PromotionPaySettleGateway;
import com.houjiang.payment.service.api.CompensateService;
import com.houjiang.payment.service.cashier.CashierBizNotifyService;
import com.houjiang.payment.service.cashier.CashierCallbackService;
import com.houjiang.payment.service.cashier.WxJsapiPayHandler;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 收银台异步回调处理实现
 *
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CashierCallbackServiceImpl implements CashierCallbackService {

    private final PayCashierOrderMapper cashierOrderMapper;
    private final PayCallbackLogMapper callbackLogMapper;
    private final WxJsapiPayHandler wxJsapiPayHandler;
    private final CashierBizNotifyService bizNotifyService;
    private final CompensateService compensateService;
    private final StripePayStrategy stripePayStrategy;
    private final PayCallbackGateway payCallbackGateway;
    private final PromotionPaySettleGateway promotionPaySettleGateway;

    private static final String CHANNEL_WX_JSAPI = "WX_JSAPI";
    private static final String CHANNEL_STRIPE = "STRIPE";
    private static final Integer SIGN_VERIFY_PASS = 1;
    private static final Integer SIGN_VERIFY_FAIL = 2;
    private static final Integer PROCESS_SUCCESS = 1;
    private static final Integer PROCESS_FAIL = 2;
    private static final Integer PROCESS_DUPLICATE = 3;

    @Override
    public String handleWxJsapiNotify(String rawXml, Map<String, String> notifyMap) {
        PayCallbackLogEntity logRow = newLog(CHANNEL_WX_JSAPI, rawXml, notifyMap);

        if (notifyMap == null || notifyMap.isEmpty()) {
            logRow.setErrMsg("empty notify");
            logRow.setProcessStatus(PROCESS_FAIL);
            saveLog(logRow);
            return wxJsapiPayHandler.buildNotifyResponse(false, "empty notify");
        }
        String returnCode = notifyMap.get("return_code");
        String resultCode = notifyMap.get("result_code");
        if (!"SUCCESS".equals(returnCode) || !"SUCCESS".equals(resultCode)) {
            logRow.setErrMsg("channel not success: return=" + returnCode + ", result=" + resultCode);
            logRow.setProcessStatus(PROCESS_FAIL);
            saveLog(logRow);
            return wxJsapiPayHandler.buildNotifyResponse(false, "channel not success");
        }

        boolean signOk = wxJsapiPayHandler.verifyNotify(notifyMap);
        logRow.setSignVerify(signOk ? SIGN_VERIFY_PASS : SIGN_VERIFY_FAIL);
        if (!signOk) {
            logRow.setErrMsg("sign verify fail");
            logRow.setProcessStatus(PROCESS_FAIL);
            saveLog(logRow);
            return wxJsapiPayHandler.buildNotifyResponse(false, "sign verify fail");
        }

        String cashierNo = notifyMap.get("out_trade_no");
        String transactionId = notifyMap.get("transaction_id");
        logRow.setCashierNo(cashierNo);
        logRow.setPayOrderId(transactionId);

        PayCashierOrderEntity order = cashierOrderMapper.selectByCashierNo(cashierNo);
        if (order == null) {
            logRow.setErrMsg("cashier order not found: " + cashierNo);
            logRow.setProcessStatus(PROCESS_FAIL);
            saveLog(logRow);
            return wxJsapiPayHandler.buildNotifyResponse(false, "order not found");
        }
        logRow.setMchOrderNo(order.getMchOrderNo());

        if (CashierOrderStatusEnum.SUCCESS.getCode() == order.getStatus()) {
            logRow.setProcessStatus(PROCESS_DUPLICATE);
            logRow.setErrMsg("order already SUCCESS, idempotent skip");
            saveLog(logRow);
            log.info("[wxJsapiCallback] idempotent skip, cashierNo={}", cashierNo);
            return wxJsapiPayHandler.buildNotifyResponse(true, "OK");
        }

        try {
            int notifyFee = Integer.parseInt(notifyMap.getOrDefault("total_fee", "0"));
            int orderFee = order.getAmount().multiply(new BigDecimal(100))
                    .setScale(0, RoundingMode.HALF_UP).intValueExact();
            if (notifyFee != orderFee) {
                logRow.setErrMsg("amount mismatch, notify=" + notifyFee + ", order=" + orderFee);
                logRow.setProcessStatus(PROCESS_FAIL);
                saveLog(logRow);
                return wxJsapiPayHandler.buildNotifyResponse(false, "amount mismatch");
            }
        } catch (NumberFormatException e) {
            logRow.setErrMsg("total_fee parse fail: " + notifyMap.get("total_fee"));
            logRow.setProcessStatus(PROCESS_FAIL);
            saveLog(logRow);
            return wxJsapiPayHandler.buildNotifyResponse(false, "invalid total_fee");
        }

        if (!updateOrderSuccess(order, transactionId)) {
            logRow.setProcessStatus(PROCESS_DUPLICATE);
            logRow.setErrMsg("concurrent callback already processed");
            saveLog(logRow);
            return wxJsapiPayHandler.buildNotifyResponse(true, "OK");
        }

        boolean bizOk = false;
        String bizErr = null;
        try {
            bizOk = bizNotifyService.notifyPaySuccess(order);
        } catch (Exception e) {
            bizErr = e.getMessage();
            log.warn("[wxJsapiCallback] biz hook error, cashierNo={}, err={}", cashierNo, bizErr);
        }

        logRow.setProcessStatus(PROCESS_SUCCESS);
        logRow.setResponseBody("SUCCESS");
        if (!bizOk) {
            logRow.setErrMsg("biz hook not ok" + (bizErr == null ? "" : (": " + bizErr)));
            registerNotifyCompensation(order);
        }
        saveLog(logRow);
        log.info("[wxJsapiCallback] process ok, cashierNo={}, payOrderId={}, bizOk={}",
                cashierNo, transactionId, bizOk);
        // 支付首次确认成功（重放已在上面 SUCCESS 分支 early-return），触发 after* 扩展点。
        dispatchPaymentSuccess(order, notifyMap);
        return wxJsapiPayHandler.buildNotifyResponse(true, "OK");
    }

    @Override
    public String handleStripeNotify(String payload, String sigHeader) {
        PayCallbackLogEntity logRow = new PayCallbackLogEntity();
        logRow.setChannel(CHANNEL_STRIPE);
        logRow.setEventType("PAY");
        logRow.setReqBody(payload);
        logRow.setSignVerify(0);
        logRow.setProcessStatus(0);
        logRow.setReceiveTime(new Date());

        if (!StringUtils.hasText(payload) || !StringUtils.hasText(sigHeader)) {
            logRow.setErrMsg("empty payload or signature");
            logRow.setProcessStatus(PROCESS_FAIL);
            saveLog(logRow);
            throw new IllegalArgumentException("empty payload or signature");
        }

        String webhookSecret = stripePayStrategy.resolveWebhookSecret(null);
        if (!StringUtils.hasText(webhookSecret)) {
            logRow.setErrMsg("stripe webhook secret not configured");
            logRow.setProcessStatus(PROCESS_FAIL);
            saveLog(logRow);
            throw new IllegalStateException("stripe webhook secret not configured");
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            logRow.setSignVerify(SIGN_VERIFY_PASS);
        } catch (SignatureVerificationException e) {
            logRow.setSignVerify(SIGN_VERIFY_FAIL);
            logRow.setErrMsg("sign verify fail: " + e.getMessage());
            logRow.setProcessStatus(PROCESS_FAIL);
            saveLog(logRow);
            throw new IllegalArgumentException("stripe signature verification failed");
        }

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = deserializer.getObject().orElse(null);
        if (stripeObject == null) {
            logRow.setErrMsg("deserialize stripe object failed, type=" + event.getType());
            logRow.setProcessStatus(PROCESS_FAIL);
            saveLog(logRow);
            throw new IllegalStateException("deserialize stripe object failed");
        }

        String cashierNo = null;
        String payOrderId = null;
        Long amountTotal = null;

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) stripeObject;
            Map<String, String> metadata = session.getMetadata();
            cashierNo = metadata == null ? null : metadata.get("outTradeNo");
            payOrderId = StringUtils.hasText(session.getPaymentIntent())
                    ? session.getPaymentIntent() : session.getId();
            amountTotal = session.getAmountTotal();
        } else if ("payment_intent.succeeded".equals(event.getType())) {
            PaymentIntent intent = (PaymentIntent) stripeObject;
            Map<String, String> metadata = intent.getMetadata();
            cashierNo = metadata == null ? null : metadata.get("outTradeNo");
            payOrderId = intent.getId();
            amountTotal = intent.getAmountReceived() != null ? intent.getAmountReceived() : intent.getAmount();
        } else {
            logRow.setProcessStatus(PROCESS_SUCCESS);
            logRow.setErrMsg("ignored event type: " + event.getType());
            logRow.setResponseBody("ignored");
            saveLog(logRow);
            log.info("[stripeCallback] ignore event type={}", event.getType());
            return "ok";
        }

        logRow.setCashierNo(cashierNo);
        logRow.setPayOrderId(payOrderId);

        if (!StringUtils.hasText(cashierNo)) {
            logRow.setErrMsg("missing outTradeNo in metadata");
            logRow.setProcessStatus(PROCESS_FAIL);
            saveLog(logRow);
            throw new IllegalArgumentException("missing outTradeNo");
        }

        PayCashierOrderEntity order = cashierOrderMapper.selectByCashierNo(cashierNo);
        if (order == null) {
            logRow.setErrMsg("cashier order not found, maybe legacy order: " + cashierNo);
            logRow.setProcessStatus(PROCESS_SUCCESS);
            logRow.setResponseBody("legacy_or_unknown");
            saveLog(logRow);
            log.warn("[stripeCallback] cashier order not found, cashierNo={}", cashierNo);
            return "ok";
        }
        logRow.setMchOrderNo(order.getMchOrderNo());

        if (CashierOrderStatusEnum.SUCCESS.getCode() == order.getStatus()) {
            logRow.setProcessStatus(PROCESS_DUPLICATE);
            logRow.setErrMsg("order already SUCCESS, idempotent skip");
            saveLog(logRow);
            return "ok";
        }

        if (amountTotal != null) {
            long orderFee = order.getAmount().multiply(new BigDecimal(100))
                    .setScale(0, RoundingMode.HALF_UP).longValueExact();
            if (amountTotal != orderFee) {
                logRow.setErrMsg("amount mismatch, notify=" + amountTotal + ", order=" + orderFee);
                logRow.setProcessStatus(PROCESS_FAIL);
                saveLog(logRow);
                throw new IllegalArgumentException("amount mismatch");
            }
        }

        if (!updateOrderSuccess(order, payOrderId)) {
            logRow.setProcessStatus(PROCESS_DUPLICATE);
            logRow.setErrMsg("concurrent callback already processed");
            saveLog(logRow);
            return "ok";
        }

        boolean bizOk = false;
        String bizErr = null;
        try {
            bizOk = bizNotifyService.notifyPaySuccess(order);
        } catch (Exception e) {
            bizErr = e.getMessage();
            log.warn("[stripeCallback] biz hook error, cashierNo={}, err={}", cashierNo, bizErr);
        }

        logRow.setProcessStatus(PROCESS_SUCCESS);
        logRow.setResponseBody("ok");
        if (!bizOk) {
            logRow.setErrMsg("biz hook not ok" + (bizErr == null ? "" : (": " + bizErr)));
            registerNotifyCompensation(order);
        }
        saveLog(logRow);
        log.info("[stripeCallback] process ok, cashierNo={}, payOrderId={}, bizOk={}",
                cashierNo, payOrderId, bizOk);
        // 支付首次确认成功（重放已在上面 SUCCESS 分支 early-return），触发 after* 扩展点。
        dispatchPaymentSuccess(order, payload);
        return "ok";
    }

    /**
     * 支付首次确认成功后，把支付事实交给二开扩展点（{@code after*} 语义）。
     *
     * <p>仅在本方法被调用时点（即订单刚刚由非 SUCCESS 迁移到 SUCCESS）才执行，
     * 因此同一渠道回调重放不会二次触发。插件故障由 {@code ExtensionGatewaySupport} 隔离；
     * 插件显式 {@code abort} 会抛业务异常（fail-closed），不影响已有回调流水落库。
     */
    private void dispatchPaymentSuccess(PayCashierOrderEntity order, Object callbackPayload) {
        String tradeNo = order.getPayOrderId();
        String channel = order.getPayMethod();
        payCallbackGateway.afterPayCallback(tradeNo, channel, "SUCCESS", callbackPayload);

        String orderNo = StringUtils.hasText(order.getMallOrderNo())
                ? order.getMallOrderNo() : order.getMchOrderNo();
        String paidTime = order.getSuccessTime() == null ? null
                : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(order.getSuccessTime());
        Map<String, Object> extra = new HashMap<>(4);
        extra.put("cashierNo", order.getCashierNo());
        extra.put("mchOrderNo", order.getMchOrderNo());
        promotionPaySettleGateway.settle(orderNo, null, channel, tradeNo,
                order.getAmount(), paidTime, extra);
    }

    private void registerNotifyCompensation(PayCashierOrderEntity order) {
        try {
            CompensateRegisterRequest request = new CompensateRegisterRequest();
            request.setBizNo("PAY_NOTIFY:" + order.getCashierNo());
            request.setBizType(1);
            request.setChannelCode(order.getPayMethod());
            request.setMaxQuery(10);
            request.setDelaySeconds(30);
            compensateService.register(request);
        } catch (Exception e) {
            log.error("[cashierCallback] register notify compensation failed, cashierNo={}",
                    order.getCashierNo(), e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    protected boolean updateOrderSuccess(PayCashierOrderEntity order, String payOrderId) {
        Date successTime = new Date();
        int changed = cashierOrderMapper.markPaymentSuccess(
                order.getCashierNo(), order.getStatus(), CashierOrderStatusEnum.SUCCESS.getCode(),
                payOrderId, successTime);
        if (changed != 1) return false;
        order.setStatus(CashierOrderStatusEnum.SUCCESS.getCode());
        order.setPayOrderId(payOrderId);
        order.setSuccessTime(successTime);
        order.setUpdateTime(new Date());
        return true;
    }

    private PayCallbackLogEntity newLog(String channel, String rawXml, Map<String, String> map) {
        PayCallbackLogEntity log = new PayCallbackLogEntity();
        log.setChannel(channel);
        log.setEventType("PAY");
        log.setReqBody(rawXml);
        log.setSignVerify(0);
        log.setProcessStatus(0);
        log.setReceiveTime(new Date());
        if (map != null) {
            log.setCashierNo(map.get("out_trade_no"));
            log.setPayOrderId(map.get("transaction_id"));
        }
        return log;
    }

    private void saveLog(PayCallbackLogEntity entity) {
        try {
            entity.setProcessTime(new Date());
            callbackLogMapper.insert(entity);
        } catch (Exception e) {
            log.warn("[cashierCallback] save log fail: {}", e.getMessage());
        }
    }
}
