package com.houjiang.payment.channel.client;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Amos
 */
@Component
public class WechatClientFactory {

    /**
     * 创建
     */
    private final Map<String, WechatClient> clients = new ConcurrentHashMap<>();

    /**
     * 创建
     * @param credential 凭证
     * @return 处理结果
     */
    public WechatClient create(Map<String, Object> credential) {
        String cacheKey = value(credential, "mchId") + ":" + value(credential, "certSerialNo");
        /**
         * @param createClient 创建客户端
         */
        return clients.computeIfAbsent(cacheKey, ignored -> createClient(credential));
    }

    /**
     * evict
     * @param credential 凭证
     */
    public void evict(Map<String, Object> credential) {
        clients.remove(value(credential, "mchId") + ":" + value(credential, "certSerialNo"));
    }

    /**
     * 创建客户端
     * @param credential 凭证
     * @return 处理结果
     */
    private WechatClient createClient(Map<String, Object> credential) {
        Config config = new RSAAutoCertificateConfig.Builder()
                .merchantId(value(credential, "mchId"))
                .privateKey(value(credential, "privateKey"))
                .merchantSerialNumber(value(credential, "certSerialNo"))
                .apiV3Key(value(credential, "apiV3Key"))
                .build();
        return new OfficialWechatClient(config, value(credential, "appId"), value(credential, "mchId"));
    }

    /**
     * value
     * @param data 数据
     * @param key 键
     * @return 处理结果
     */
    private String value(Map<String, Object> data, String key) {
        Object value = data.get(key);
        /**
         * @param String string
         */
        if (value == null || String.valueOf(value).isBlank()) {
            /**
             * @param key 键
             */
            throw new IllegalArgumentException("Missing WeChat credential: " + key);
        }
        return String.valueOf(value);
    }

    public interface WechatClient {
        /**
         * @param request 请求
         * @return 处理结果
         */
        String nativePrepay(Map<String, Object> request);
        /**
         * @param request 请求
         * @return 处理结果
         */
        Map<String, String> jsapiPrepay(Map<String, Object> request);
        /**
         * @param outTradeNo out交易编号
         * @return 处理结果
         */
        Map<String, Object> query(String outTradeNo);
        /**
         * @param request 请求
         * @return 处理结果
         */
        Map<String, Object> refund(Map<String, Object> request);
    }

    private static final class OfficialWechatClient implements WechatClient {
        private final String appId;
        private final String mchId;
        private final NativePayService nativeService;
        private final JsapiServiceExtension jsapiService;
        private final RefundService refundService;

        /**
         * 待补充说明
         * @param config 配置
         * @param appId app主键
         * @param mchId mch主键
         * @return 处理结果
         */
        private OfficialWechatClient(Config config, String appId, String mchId) {
            this.appId = appId;
            this.mchId = mchId;
            this.nativeService = new NativePayService.Builder().config(config).build();
            this.jsapiService = new JsapiServiceExtension.Builder().config(config).build();
            this.refundService = new RefundService.Builder().config(config).build();
        }

        /**
         * @param data 数据
         * @return 处理结果
         */
        @Override
        public String nativePrepay(Map<String, Object> data) {
            com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest request =
                    new com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest();
            request.setAppid(appId);
            request.setMchid(mchId);
            request.setDescription(text(data, "subject"));
            request.setOutTradeNo(text(data, "outTradeNo"));
            request.setNotifyUrl(text(data, "notifyUrl"));
            com.wechat.pay.java.service.payments.nativepay.model.Amount amount =
                    new com.wechat.pay.java.service.payments.nativepay.model.Amount();
            amount.setTotal(Math.toIntExact(cents(data.get("amount"))));
            amount.setCurrency("CNY");
            request.setAmount(amount);
            PrepayResponse response = nativeService.prepay(request);
            return response.getCodeUrl();
        }

        /**
         * @param data 数据
         * @return 处理结果
         */
        @Override
        public Map<String, String> jsapiPrepay(Map<String, Object> data) {
            com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest request =
                    new com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest();
            request.setAppid(appId);
            request.setMchid(mchId);
            request.setDescription(text(data, "subject"));
            request.setOutTradeNo(text(data, "outTradeNo"));
            request.setNotifyUrl(text(data, "notifyUrl"));
            com.wechat.pay.java.service.payments.jsapi.model.Amount amount =
                    new com.wechat.pay.java.service.payments.jsapi.model.Amount();
            amount.setTotal(Math.toIntExact(cents(data.get("amount"))));
            amount.setCurrency("CNY");
            request.setAmount(amount);
            Payer payer = new Payer();
            payer.setOpenid(text(data, "openId"));
            request.setPayer(payer);
            PrepayWithRequestPaymentResponse response = jsapiService.prepayWithRequestPayment(request);
            Map<String, String> result = new LinkedHashMap<>();
            result.put("appId", response.getAppId());
            result.put("timeStamp", response.getTimeStamp());
            result.put("nonceStr", response.getNonceStr());
            result.put("package", response.getPackageVal());
            result.put("signType", response.getSignType());
            result.put("paySign", response.getPaySign());
            return result;
        }

        /**
         * @param outTradeNo out交易编号
         * @return 处理结果
         */
        @Override
        public Map<String, Object> query(String outTradeNo) {
            QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
            request.setMchid(mchId);
            request.setOutTradeNo(outTradeNo);
            Transaction transaction = nativeService.queryOrderByOutTradeNo(request);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("tradeState", transaction.getTradeState() == null ? null : transaction.getTradeState().name());
            result.put("transactionId", transaction.getTransactionId());
            result.put("successTime", transaction.getSuccessTime());
            result.put("tradeStateDesc", transaction.getTradeStateDesc());
            return result;
        }

        /**
         * @param data 数据
         * @return 处理结果
         */
        @Override
        public Map<String, Object> refund(Map<String, Object> data) {
            CreateRequest request = new CreateRequest();
            request.setOutTradeNo(text(data, "channelOrderNo"));
            request.setOutRefundNo(text(data, "refundNo"));
            request.setReason(optionalText(data, "refundReason"));
            request.setNotifyUrl(optionalText(data, "notifyUrl"));
            AmountReq amount = new AmountReq();
            amount.setRefund(cents(data.get("refundAmount")));
            amount.setTotal(cents(data.get("totalAmount")));
            amount.setCurrency("CNY");
            request.setAmount(amount);
            Refund refund = refundService.create(request);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("status", refund.getStatus() == null ? null : refund.getStatus().name());
            result.put("refundId", refund.getRefundId());
            result.put("successTime", refund.getSuccessTime());
            return result;
        }

        /**
         * cents
         * @param value value
         * @return 处理结果
         */
        private static Long cents(Object value) {
            /**
             * @param null null
             */
            if (value == null) throw new IllegalArgumentException("amount is required");
            return new BigDecimal(String.valueOf(value)).movePointRight(2)
                    .setScale(0, RoundingMode.HALF_UP).longValueExact();
        }

        /**
         * text
         * @param data 数据
         * @param key 键
         * @return 处理结果
         */
        private static String text(Map<String, Object> data, String key) {
            String value = optionalText(data, key);
            /**
             * @param value value
             */
            if (value == null || value.isBlank()) throw new IllegalArgumentException(key + " is required");
            return value;
        }

        /**
         * optionaltext
         * @param data 数据
         * @param key 键
         * @return 处理结果
         */
        private static String optionalText(Map<String, Object> data, String key) {
            Object value = data.get(key);
            return value == null ? null : String.valueOf(value);
        }
    }
}
