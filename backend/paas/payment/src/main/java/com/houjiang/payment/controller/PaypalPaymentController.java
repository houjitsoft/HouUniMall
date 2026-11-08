package com.houjiang.payment.controller;

import com.alibaba.fastjson.JSON;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.MathUtil;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.base.BaseCurrencySymbolVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.common.PayPalUtils;
import com.houjiang.payment.common.PaypalPaymentIntent;
import com.houjiang.payment.common.PaypalPaymentMethod;
import com.houjiang.payment.dto.GoodsDTO;
import com.houjiang.payment.feign.BaseServiceClient;
import com.houjiang.payment.feign.OrderServiceClient;
import com.houjiang.payment.feign.UsermsServiceClient;
import com.houjiang.payment.mapper.PayConsumeRecordMapper;
import com.houjiang.payment.mapper.PayConsumeTradeBackMapper;
import com.houjiang.payment.mapper.PayConsumeTradeMapper;
import com.houjiang.payment.service.PaypalService;
import com.houjiang.payment.vo.RecordVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName PaypalPaymentController
 * @Description paypal支付控制台
 * @Author Amos
 * @Date 2024/4/24 17:35
 * @Version 1.0
 * @author Amos
 */
@RefreshScope
@RestController
@RequestMapping("/")
public class PaypalPaymentController {

    public static final String PAYPAL_SUCCESS_URL = "pay/success";

    public static final String PAYPALAPP_SUCCESS_URL = "payApp/success";

    public static final String PAYPAL_CANCEL_URL = "pay/cancel";

    /** PC/H5跳转地址 */
    public String SUCCESS_URL = "";
    /** app订单id */
    public Map<String,String> mapAppOrderId = new HashMap<>();
    /** PC/H5订单id */
    public Map<String,String> mapPcOrderId = new HashMap<>();

    private Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    private PayConsumeTradeBackMapper payConsumeTradeBackMapper;
    @Autowired
    private PayConsumeRecordMapper payConsumeRecordMapper;
    @Autowired
    private BaseServiceClient baseServiceClient;
    @Autowired
    private OrderServiceClient orderServiceClient;
    @Resource
    private PaypalService paypalService;

    @Value("${paypal.client.app}")
    private String clientId;

    @Value("${paypal.client.secret}")
    private String clientSecret;

    @Value("${paypal.client.mode}")
    private String mode;

    @Value("${domainName}")
    public String domainName;


    /**
     * 开始交易App
     * @param request
     * @param orderId
     * @param token
     * @return
     */
    @PostMapping("/payApp")
    public ResponseResult payApp(HttpServletRequest request,
                                 @RequestParam("orderId") String orderId,
                                 @RequestParam("token") String token,
                                 @RequestParam("currencyCode") String currencyCode) {
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        if (null == userInfo) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        //用户id
        String userId = userInfo.getUserId();
        //订单支付金额
        BigDecimal totalPrice = new BigDecimal(0);
        //判断当前订单号是否是 支付单号 或 是订单号
        if (MathUtil.isInteger(orderId)) {
            GoodsDTO queryGoodsPara = payConsumeTradeBackMapper.queryGoodsPriceAndName(orderId, userId);
            totalPrice = queryGoodsPara.getTradePaymentAmount();
        } else {
            RecordVO rp = payConsumeRecordMapper.queryRecordPara(orderId);
            totalPrice = rp.getRecordMoney();
        }
        if("CNY".equals(currencyCode)){
            //如果是人民币 就转 美元支付
            currencyCode = "USD";
        }
        // 计算汇率
        ResponseResult result = baseServiceClient.getByCurrencyDetection(currencyCode);
        if(result.getCode()  == CommonStatusEnum.SUCCESS.getCode()){
            // 汇率
            BaseCurrencySymbolVO baseCurrencySymbolVO = JSON.parseObject(JSON.toJSONString(result.getData()), BaseCurrencySymbolVO.class);
            BigDecimal exchangeRate = baseCurrencySymbolVO.getExchangeRate();
            totalPrice = totalPrice.multiply(exchangeRate).setScale(2,BigDecimal.ROUND_HALF_UP);
        }
        String cancelUrl = domainName+"/payment/" + PAYPAL_CANCEL_URL;
        String successUrl = domainName+"/payment/" + PAYPALAPP_SUCCESS_URL;
        Order payment = paypalService.createPaymentApp(
                totalPrice,
                currencyCode,
                cancelUrl,
                successUrl,
                mode,
                clientId,
                clientSecret);
        String tokenId = payment.id();
        mapAppOrderId.put(tokenId,orderId);
        log.info("orderId is {}",tokenId);
        String status = payment.status();
        if (status.equals("CREATED")) {
            List<LinkDescription> links = payment.links();
            for (LinkDescription linkDescription : links) {
                if (linkDescription.rel().equals("approve")) {
                    Map<String,String > map = new HashMap<>();
                    map.put("orderId",tokenId);
                    map.put("url",linkDescription.href());
                    map.put("mode",mode);
                    map.put("clientId",clientId);
                    map.put("currency","USD");
                    return ResponseResult.success(map);
                }
            }
        }
        return ResponseResult.success();
    }

    /**
     * 交易成功,并执行交易(相当于提交事务)APP
     * @param token
     * @return
     */
    @GetMapping("/payApp/success")
    public ResponseResult successPayApp(@RequestParam("token") String token,
                                        @RequestParam("currencyCode") String currencyCode) {
        log.info("token is {}",token);
        //捕获订单 进行支付
        HttpResponse<Order> response = null;
        OrdersCaptureRequest ordersCaptureRequest = new OrdersCaptureRequest(token);
        ordersCaptureRequest.requestBody(new OrderRequest());
        PayPalUtils payPalClient = new PayPalUtils();
        try {
            //环境判定sanbox 或 live
            response = payPalClient.client(mode, clientId, clientSecret).execute(ordersCaptureRequest);
        } catch (IOException e) {
            log.error("调用paypal扣款失败，失败原因 {}", e.getMessage());
        }
        for (PurchaseUnit purchaseUnit : response.result().purchaseUnits()) {
            for (Capture capture : purchaseUnit.payments().captures()) {
                if ("COMPLETED".equals(capture.status())) {
                    String orderId = mapAppOrderId.get(token);
                    log.info("orderId is {}",orderId);
                    log.info("支付成功ID：" + capture.id());
                    paypalService.notifyPay(orderId,capture.id());
                    if("CNY".equals(currencyCode)){
                        //如果是人民币 就转 美元支付
                        currencyCode = "USD";
                    }
                    ResponseResult result = baseServiceClient.getByCurrencyDetection(currencyCode);
                    if(result.getCode()  == CommonStatusEnum.SUCCESS.getCode()){
                        BaseCurrencySymbolVO baseCurrencySymbolVO = JSON.parseObject(JSON.toJSONString(result.getData()), BaseCurrencySymbolVO.class);
                        // 将货币符号和汇率存入订单表
                        orderServiceClient.updateCurrencyAndExchangeRate(orderId,baseCurrencySymbolVO.getCurrencyCode(),baseCurrencySymbolVO.getExchangeRate(),baseCurrencySymbolVO.getCurrencySymbol());

                    }
                    return ResponseResult.success();
                }
            }
        }
        return ResponseResult.success();
    }

    /**
     * 开始交易
     * @param request
     * @param url
     * @param orderId
     * @param token
     * @return
     */
    @PostMapping("/pay")
    public ResponseResult pay(HttpServletRequest request,
                              @RequestParam("url") String url,
                              @RequestParam("orderId") String orderId,
                              @RequestParam("token") String token,
                              @RequestParam("currencyCode") String currencyCode) {
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        if (null == userInfo) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        //用户id
        String userId = userInfo.getUserId();
        //订单支付金额
        BigDecimal totalPrice = new BigDecimal(0);
        //判断当前订单号是否是 支付单号 或 是订单号
        if (MathUtil.isInteger(orderId)) {
            GoodsDTO queryGoodsPara = payConsumeTradeBackMapper.queryGoodsPriceAndName(orderId, userId);
            totalPrice = queryGoodsPara.getTradePaymentAmount();
        } else {
            RecordVO rp = payConsumeRecordMapper.queryRecordPara(orderId);
            totalPrice = rp.getRecordMoney();
        }
        if("CNY".equals(currencyCode)){
            //如果是人民币 就转 美元支付
            currencyCode = "USD";
        }
        // 计算汇率
        ResponseResult result = baseServiceClient.getByCurrencyDetection(currencyCode);
        if(result.getCode()  == CommonStatusEnum.SUCCESS.getCode()){
            // 汇率
            BaseCurrencySymbolVO baseCurrencySymbolVO = JSON.parseObject(JSON.toJSONString(result.getData()), BaseCurrencySymbolVO.class);
            BigDecimal exchangeRate = baseCurrencySymbolVO.getExchangeRate();
            totalPrice = totalPrice.multiply(exchangeRate).setScale(2,BigDecimal.ROUND_HALF_UP);
        }
        String cancelUrl = domainName+"/payment/" + PAYPAL_CANCEL_URL;
        String successUrl = domainName+"/payment/" + PAYPAL_SUCCESS_URL;
        SUCCESS_URL = url;
        try {
            Payment payment = paypalService.createPayment(
                    totalPrice,
                    currencyCode,
                    PaypalPaymentMethod.paypal,
                    PaypalPaymentIntent.sale,
                    "payment description",
                    cancelUrl,
                    successUrl);
            mapPcOrderId.put(payment.getId(),orderId);
            mapPcOrderId.put(payment.getId()+"CURRENCYCODE",currencyCode);
            for (Links links : payment.getLinks()) {
                if (links.getRel().equals("approval_url")) {
                    // 输出跳转到paypal支付页面的网站，eg：https://www.sandbox.paypal.com/cgi-bin/webscr?cmd=_express-checkout&token=EC-8HL7026676482401S
                    log.info("links is {}", links.getHref());
                    return ResponseResult.success(links.getHref());
                }
            }
        } catch (PayPalRESTException e) {
            // PayPalRESTException是PayPal的REST API客户端中的一个异常类，它用于处理与PayPal REST API相关的异常情况
            // 当使用 PayPal 的 REST API 进行付款处理、交易查询、退款请求等操作时，如果发生错误或异常情况，PayPalRestException 将被抛出，以便开发者可以捕获并处理这些异常
            log.error(e.getMessage());
        }
        return ResponseResult.success();
    }

    /**
     * 交易取消
     *
     * @return
     */
    @GetMapping("/pay/cancel")
    public ResponseResult cancelPay(HttpServletResponse response) {
        try {
            response.sendRedirect(SUCCESS_URL.replace("%23","#"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseResult.success();
    }

    /**
     * 交易成功,并执行交易(相当于提交事务)
     *
     * @param paymentId
     * @param payerId   一定是PayerID，PayPal通常使用"PayerID"（ID和P都大小写）作为参数名称
     * @return
     */
    @GetMapping("/pay/success")
    public ResponseResult successPay(@RequestParam("paymentId") String paymentId,
                                     @RequestParam("PayerID") String payerId,
                                     HttpServletResponse response) {
        try {

            Payment payment = paypalService.executePayment(paymentId, payerId);
            if (payment.getState().equals("approved")) {
                log.info("支付成功Payment：" + payment);
                String orderId = mapPcOrderId.get(payment.getId());
                String currencyCode = mapPcOrderId.get(payment.getId()+"CURRENCYCODE");
                log.info("orderId is {}",orderId);
                log.info("currencyCode is {}",currencyCode);
                // 交易ID，transactionId
                String transactionId= "";
                if (payment.getIntent().equals(PaypalPaymentIntent.authorize.toString())) {
                    transactionId = payment.getTransactions().get(0).getRelatedResources().get(0).getAuthorization().getId();
                } else if (payment.getIntent().equals(PaypalPaymentIntent.sale.toString())) {
                    transactionId = payment.getTransactions().get(0).getRelatedResources().get(0).getSale().getId();
                } else if (payment.getIntent().equals(PaypalPaymentIntent.order.toString())) {
                    transactionId = payment.getTransactions().get(0).getRelatedResources().get(0).getOrder().getId();
                }
                log.info("PDT通知：交易成功回调");
                log.info("交易订单Id：{}", transactionId);

                paypalService.notifyPay(orderId,transactionId);
                if("CNY".equals(currencyCode)){
                    //如果是人民币 就转 美元支付
                    currencyCode = "USD";
                }
                ResponseResult result = baseServiceClient.getByCurrencyDetection(currencyCode);
                if(result.getCode()  == CommonStatusEnum.SUCCESS.getCode()){
                    BaseCurrencySymbolVO baseCurrencySymbolVO = JSON.parseObject(JSON.toJSONString(result.getData()), BaseCurrencySymbolVO.class);
                    // 将货币符号和汇率存入订单表
                    orderServiceClient.updateCurrencyAndExchangeRate(orderId,baseCurrencySymbolVO.getCurrencyCode(),baseCurrencySymbolVO.getExchangeRate(),baseCurrencySymbolVO.getCurrencySymbol());

                }

                try {
                    response.sendRedirect(SUCCESS_URL.replace("%23","#"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return ResponseResult.success();
            }

        } catch (PayPalRESTException e) {
            log.error(e.getMessage());
        }
        try {
            response.sendRedirect(SUCCESS_URL.replace("%23","#"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseResult.success();
    }

    /**
     * 交易退款
     *
     * @return
     */
    @PostMapping("/pay/refundSale")
    public boolean refundSale(@RequestParam("tradeNo") String tradeNo,@RequestParam("money") BigDecimal money,@RequestParam("currencyCode")String currencyCode) {
        return paypalService.refundSale(tradeNo,money,currencyCode);
    }

}
