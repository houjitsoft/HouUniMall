package com.houjiang.payment.service.impl;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.stripe.Stripe;
import com.stripe.StripeClient;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.*;
import com.stripe.param.checkout.SessionCreateParams;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.base.MemberMessageListIntegralDTO;
import com.houjiang.commons.dto.oos.OssUploadUpdateParamDTO;
import com.houjiang.commons.dto.order.WapGoodsSaleSumDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.*;
import com.houjiang.commons.util.secverifyapiutil.Base64Utils;
import com.houjiang.commons.vo.base.BaseCurrencySymbolVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.dto.GoodsDTO;
import com.houjiang.payment.dto.RecordDTO;
import com.houjiang.payment.dto.StripeOrder;
import com.houjiang.payment.feign.*;
import com.houjiang.payment.mapper.PayConsumeRecordMapper;
import com.houjiang.payment.mapper.PayConsumeTradeBackMapper;
import com.houjiang.payment.mapper.PayConsumeTradeMapper;
import com.houjiang.payment.mapper.PayUserResourceMapper;
import com.houjiang.payment.service.StripePaymentService;
import com.houjiang.payment.vo.ComsumeRecordVO;
import com.houjiang.payment.vo.RecordVO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName StripePaymentServiceImpl
 * @Author Amos
 * @Date 2024/12/18 13:01
 * @Version 1.0
 * @author Amos
 */
@Service
public class StripePaymentServiceImpl implements StripePaymentService {

    final Logger log = LoggerFactory.getLogger(StripePaymentServiceImpl.class);

    @Value("${stripe.key}")
    private String stripeKey;
    @Value("${stripe.sectret}")
    private String stripeSectret;
    @Value("${stripe.webhooksKey}")
    private String stripeWebhooksKey;
    @Value("${stripe.merchantName}")
    private String merchantName;
    @Value("${domainName}")
    public String domainName;

    /** PC */
    public String SUCCESS_URL = "/ucenter/index.html#/order/list";
    /** h5 */
    public String SUCCESS_URL_H5 = "/h5/#/member/orderList/orderList";

    private StripeClient client = null;

    @Autowired
    private UsermsServiceClient usermsServiceClient;
    @Autowired
    private PayConsumeTradeBackMapper payConsumeTradeBackMapper;
    @Autowired
    private PayConsumeRecordMapper payConsumeRecordMapper;
    @Autowired
    private PayUserResourceMapper payUserResourceMapper;
    @Autowired
    private GoodServiceClient goodServiceClient;
    @Autowired
    private PayConsumeTradeMapper payConsumeTradeMapper;
    @Autowired
    private ActivityServiceClient activityServiceClient;
    @Autowired
    private OrderServiceClient orderServiceClient;
    @Autowired
    private ThirdPartyServiceClient thirdPartyServiceClient;
    @Autowired
    private BaseServiceClient baseServiceClient;


    public void init(){
        client = StripeClient.builder()
                .setConnectTimeout(30 * 1000)
                .setReadTimeout(80 * 1000)
                .setApiKey(stripeSectret)
                .build();
    }
    @Override
    public ResponseResult pay(Map<Object, Object> para) {
        //获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(para.get("token").toString());
        if (null != userInfo) {
            //订单号
            String orderId = (String) para.get("orderId");

            //用户id
            String userId = userInfo.getUserId();
            //订单支付金额
            BigDecimal totalPrice = new BigDecimal(0);
            //商品名称
            String goodsName = null;
            //积分订单支付金额
            Integer amountAotalIntegral = null;
            //积分商品 0  是  1 否
            Integer integralGoods = CommonType.goodsType.GOODS_TYPE_ORDINARY.getCode();
            //判断当前订单号是否是 支付单号 或 是订单号
            if (MathUtil.isInteger(orderId)) {
                GoodsDTO queryGoodsPara = payConsumeTradeBackMapper.queryGoodsPriceAndName(orderId, userId);
                totalPrice = queryGoodsPara.getTradePaymentAmount();
                goodsName = queryGoodsPara.getTradeDesc();
                //获取积分商品订单的积分
                Integer totalIntegral = queryGoodsPara.getTotalIntegral();
                //积分商品 0  是  1 否
                integralGoods = queryGoodsPara.getIntegralGoods();
                amountAotalIntegral = totalIntegral;
            } else {
                RecordVO rp = payConsumeRecordMapper.queryRecordPara(orderId);
                totalPrice = rp.getRecordMoney();
                goodsName = rp.getRecordDesc();

                // 获取积分商品的积分
                String totalIntegral = RedisUtil
                        .get(HouConfig.HOU_USERINTEGRALYPAID + "--" + para.get("token") + "--" + userId);
                if (null != totalIntegral) {
                    //amountAotalIntegral 不为null 证明是积分订单
                    amountAotalIntegral = Integer.valueOf(totalIntegral);
                    //积分商品 0  是  1 否
                    integralGoods = CommonType.goodsType.GOODS_TYPE_INTEGRAL.getCode();
                }
            }
            //如果是积分商品判断 当前用户积分是否支持购买
            if (null != integralGoods && integralGoods == 0) {
                //获取用户积分
                Integer userIntegral = usermsServiceClient.getMemberIntegralByUserId(userId);
                Integer frozenIntegra = usermsServiceClient.getMemberFrozenIntegraIntegralByUserId(userId);
                userIntegral = userIntegral - frozenIntegra;
                if(userIntegral < 0){
                    userIntegral = 0;
                }
                if (null != userIntegral && amountAotalIntegral > userIntegral) {
                    return ResponseResult.fail(BizCodeEnume.PAY_INTEGRAL_ERROR.getCode(), BizCodeEnume.PAY_INTEGRAL_ERROR.getMsg());
                }
            }
            init();
            // 构建支付参数
            StripeOrder stripeOrder = new StripeOrder();
            stripeOrder.setBody(goodsName);
            stripeOrder.setSubject("购物");
            stripeOrder.setOrderId(orderId);
            Integer type = Integer.valueOf(para.get("type").toString());
            if(null != para.get("currencyCode") && StringUtil.isNotBlank(para.get("currencyCode").toString())){
                // 货币符号
                stripeOrder.setCurrencyCode(para.get("currencyCode").toString());
                // 计算汇率
                ResponseResult result = baseServiceClient.getByCurrencyDetection(para.get("currencyCode").toString());
                if(result.getCode()  == CommonStatusEnum.SUCCESS.getCode()){
                    // 汇率
                    BaseCurrencySymbolVO baseCurrencySymbolVO = JSON.parseObject(JSON.toJSONString(result.getData()), BaseCurrencySymbolVO.class);
                    BigDecimal exchangeRate = baseCurrencySymbolVO.getExchangeRate();
                    totalPrice = totalPrice.multiply(exchangeRate).setScale(2,BigDecimal.ROUND_HALF_UP);
                }
            }else{
                stripeOrder.setCurrencyCode("USD");
            }
            totalPrice = totalPrice.multiply(new BigDecimal(100));
            stripeOrder.setPrice(totalPrice);
            // 创建支付
            String payUrl = orderInfo(stripeOrder,type);
            return ResponseResult.success(payUrl);
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());
    }

    /**
     * 产品
     * @param stripeOrder
     * @return
     * @throws StripeException
     */
    private Product getProduct(StripeOrder stripeOrder) throws StripeException {
        ProductSearchParams searchParams =
                ProductSearchParams.builder()
                        .setQuery("active:'true' AND name:'" + stripeOrder.getSubject()
                                + "' AND description:'" + stripeOrder.getBody() + "'")
                        .setLimit(1L)
                        .build();
        StripeSearchResult<Product> result = client.products().search(searchParams);
        Product product;
        if (result != null && !result.getData().isEmpty()) {
            product = result.getData().stream().findFirst().get();
        } else {
            //创建产品 https://stripe.com/docs/api/products/create
            ProductCreateParams params = ProductCreateParams.builder()
                    .setDescription(stripeOrder.getBody())
                    .setName(stripeOrder.getSubject()).build();
            product = client.products().create(params);
        }
        return product;
    }
    /**
     * 价格
     */
    private Price getPrice(StripeOrder stripeOrder, String productId) throws StripeException {
        Long unitAmount = stripeOrder.getPrice().longValue();
        String lookupKey = unitAmount + stripeOrder.getCurrencyCode() + productId;
        PriceSearchParams params =
                PriceSearchParams.builder()
                        .setQuery("active:'true' AND product:'" + productId
                                + "' AND currency:'" + stripeOrder.getCurrencyCode()
                                + "' AND lookup_key:'" + lookupKey + "'")
                        .build();
        StripeSearchResult<Price> result = client.prices().search(params);
        Price price;
        if (result != null && !result.getData().isEmpty()) {
            price = result.getData().stream().findFirst().get();
        } else {
            //创建价格 https://stripe.com/docs/api/prices/create
//          PriceCreateParams.Recurring recurring = PriceCreateParams.Recurring.builder()
//                    .setInterval(PriceCreateParams.Recurring.Interval.MONTH).build();
            PriceCreateParams priceCreateParams = PriceCreateParams.builder()
                    .setCurrency(stripeOrder.getCurrencyCode())
                    .setProduct(productId)
                    .setLookupKey(lookupKey)
                    .setUnitAmount(unitAmount)
//                    .setRecurring(recurring)
                    .build();
            price = client.prices().create(priceCreateParams);
        }
        return price;
    }
    /**
     * 创建支付
     */
    public String orderInfo(StripeOrder stripeOrder,Integer type) {
        try {
            Product product = getProduct(stripeOrder);
            Price price = getPrice(stripeOrder, product.getId());
            String successUrl = domainName;
            // 1 是pc  2是 h5
            if(1 == type){
                successUrl = successUrl + SUCCESS_URL;
            }else{
                successUrl = successUrl + SUCCESS_URL_H5;
            }
            //创建支付信息 得到url
            SessionCreateParams sessionCreateParams = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
//                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.ALIPAY)
//                    .setCustomer("47852") // 关联客户
//                    .setCustomerEmail("1801454629@qq.com") // 客户电子邮件地址
//                    .setClientReferenceId("U45567895262") // 自定义订单参考 ID
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(successUrl)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPrice(price.getId())
                                    .build())
                    .putMetadata("outTradeNo", stripeOrder.getOrderId())
                    .putMetadata("currencyCode", stripeOrder.getCurrencyCode())
                    .build();
            Session session = client.checkout().sessions().create(sessionCreateParams);
            System.out.println(session.getUrl());
            return session.getUrl();
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public ResponseResult payApp(Map<Object, Object> para) throws StripeException {
        //获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(para.get("token").toString());
        if (null != userInfo) {
            //订单号
            String orderId = (String) para.get("orderId");

            //用户id
            String userId = userInfo.getUserId();
            //订单支付金额
            BigDecimal totalPrice = new BigDecimal(0);
            //商品名称
            String goodsName = null;
            //积分订单支付金额
            Integer amountAotalIntegral = null;
            //积分商品 0  是  1 否
            Integer integralGoods = CommonType.goodsType.GOODS_TYPE_ORDINARY.getCode();
            //判断当前订单号是否是 支付单号 或 是订单号
            if (MathUtil.isInteger(orderId)) {
                GoodsDTO queryGoodsPara = payConsumeTradeBackMapper.queryGoodsPriceAndName(orderId, userId);
                totalPrice = queryGoodsPara.getTradePaymentAmount();
                goodsName = queryGoodsPara.getTradeDesc();
                //获取积分商品订单的积分
                Integer totalIntegral = queryGoodsPara.getTotalIntegral();
                //积分商品 0  是  1 否
                integralGoods = queryGoodsPara.getIntegralGoods();
                amountAotalIntegral = totalIntegral;
            } else {
                RecordVO rp = payConsumeRecordMapper.queryRecordPara(orderId);
                totalPrice = rp.getRecordMoney();
                goodsName = rp.getRecordDesc();

                // 获取积分商品的积分
                String totalIntegral = RedisUtil
                        .get(HouConfig.HOU_USERINTEGRALYPAID + "--" + para.get("token") + "--" + userId);
                if (null != totalIntegral) {
                    //amountAotalIntegral 不为null 证明是积分订单
                    amountAotalIntegral = Integer.valueOf(totalIntegral);
                    //积分商品 0  是  1 否
                    integralGoods = CommonType.goodsType.GOODS_TYPE_INTEGRAL.getCode();
                }
            }
            //如果是积分商品判断 当前用户积分是否支持购买
            if (null != integralGoods && integralGoods == 0) {
                //获取用户积分
                Integer userIntegral = usermsServiceClient.getMemberIntegralByUserId(userId);
                Integer frozenIntegra = usermsServiceClient.getMemberFrozenIntegraIntegralByUserId(userId);
                userIntegral = userIntegral - frozenIntegra;
                if(userIntegral < 0){
                    userIntegral = 0;
                }
                if (null != userIntegral && amountAotalIntegral > userIntegral) {
                    return ResponseResult.fail(BizCodeEnume.PAY_INTEGRAL_ERROR.getCode(), BizCodeEnume.PAY_INTEGRAL_ERROR.getMsg());
                }
            }
            init();
            // 构建支付参数
            StripeOrder stripeOrder = new StripeOrder();
            stripeOrder.setBody(goodsName);
            stripeOrder.setSubject("购物");
            stripeOrder.setOrderId(orderId);

            if(null != para.get("currencyCode") && StringUtil.isNotBlank(para.get("currencyCode").toString())){
                 // 货币符号
                 stripeOrder.setCurrencyCode(para.get("currencyCode").toString());
                 // 计算汇率
                 ResponseResult result = baseServiceClient.getByCurrencyDetection(para.get("currencyCode").toString());
                 if(result.getCode()  == CommonStatusEnum.SUCCESS.getCode()){
                     // 汇率
                     BaseCurrencySymbolVO baseCurrencySymbolVO = JSON.parseObject(JSON.toJSONString(result.getData()), BaseCurrencySymbolVO.class);
                     BigDecimal exchangeRate = baseCurrencySymbolVO.getExchangeRate();
                     totalPrice = totalPrice.multiply(exchangeRate).setScale(2,BigDecimal.ROUND_HALF_UP);
                 }
            }else{
                stripeOrder.setCurrencyCode("USD");
            }
            totalPrice = totalPrice.multiply(new BigDecimal(100));
            stripeOrder.setPrice(totalPrice);
            // 创建支付
            Map<String, Object> map = create(stripeOrder);
            map.put("orderId", orderId);
            return ResponseResult.success(map);
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());
    }
    public  Map<String, Object> create(StripeOrder stripeOrder) throws StripeException {
        try {
            //账号私钥
            Stripe.apiKey = stripeSectret;

            // 创建付款对象
            PaymentIntentCreateParams params =
                    PaymentIntentCreateParams
                            .builder()
                            .setAmount(stripeOrder.getPrice().longValue())
                            .setCurrency(stripeOrder.getCurrencyCode())
                            .setAutomaticPaymentMethods(
                                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                            )
                            .putMetadata("outTradeNo", stripeOrder.getOrderId())
                            .putMetadata("currencyCode", stripeOrder.getCurrencyCode())
                            .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            System.out.println(JSONObject.toJSONString(paymentIntent));


            Map<String, Object> map = new JSONObject();
            map.put("clientSecret", paymentIntent.getClientSecret());
            map.put("publishKey", stripeKey);
            map.put("merchantName", merchantName);
            System.out.println("订单号：" + paymentIntent.getId());
            return map;

        } catch (StripeException e) {
            throw new RuntimeException("failed to create checkout by payment intent: " + e.getMessage());
        }
    }
    @Override
    public ResponseEntity<String> stripeNotifyUrl(HttpServletRequest request) throws IOException, SignatureVerificationException {
        String payload = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        String sigHeader = request.getHeader("Stripe-Signature");

        // 验证签名
        Event event = Webhook.constructEvent(payload, sigHeader, stripeWebhooksKey);

        // 反序列化事件数据
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;
        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        } else {
            log.error("Deserialization failed due to an API version mismatch.");
            return new ResponseEntity<>("Deserialization failed", HttpStatus.BAD_REQUEST);
        }
        String stripeOutTradeNo = "";
        String orderId = "";
        if("checkout.session.completed".equals(event.getType())){
            // 通过支付链接 支付完成
            Session session = (Session) stripeObject;
            stripeOutTradeNo = session.getPaymentIntent();
            Map<String, String> metaData = session.getMetadata();
            if(null != metaData){
                // 自定义订单号
                orderId = session.getMetadata().get("outTradeNo");
                // 货币符号
                String currencyCode = session.getMetadata().get("currencyCode");
                if(null != orderId && null != currencyCode ){
                    notifyPay(orderId, stripeOutTradeNo);
                    ResponseResult result = baseServiceClient.getByCurrencyDetection(currencyCode);
                    if(result.getCode()  == CommonStatusEnum.SUCCESS.getCode()){
                        BaseCurrencySymbolVO baseCurrencySymbolVO = JSON.parseObject(JSON.toJSONString(result.getData()), BaseCurrencySymbolVO.class);
                        // 将货币符号和汇率存入订单表
                        orderServiceClient.updateCurrencyAndExchangeRate(orderId,baseCurrencySymbolVO.getCurrencyCode(),baseCurrencySymbolVO.getExchangeRate(),baseCurrencySymbolVO.getCurrencySymbol());
                    }
                    log.info("支付成功 checkout.session， 订单号为：{}", stripeOutTradeNo);
                }
            }
        }else if("payment_intent.succeeded".equals(event.getType())){
            PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
            //自定义传入的参数
            Map<String, String> metaData = paymentIntent.getMetadata();
            if(null != metaData){
                // 获取 Stripe 订单号
                String stripeOrderId = paymentIntent.getId();
                //自定义订单号
                orderId = metaData.get("outTradeNo");
                //货币符号
                String currencyCode = metaData.get("currencyCode");
                if(null != orderId && null != currencyCode ){
                    notifyPay(orderId, stripeOrderId);
                    ResponseResult result = baseServiceClient.getByCurrencyDetection(currencyCode);
                    if(result.getCode()  == CommonStatusEnum.SUCCESS.getCode()){
                        BaseCurrencySymbolVO baseCurrencySymbolVO = JSON.parseObject(JSON.toJSONString(result.getData()), BaseCurrencySymbolVO.class);
                        // 将货币符号和汇率存入订单表
                        orderServiceClient.updateCurrencyAndExchangeRate(orderId,baseCurrencySymbolVO.getCurrencyCode(),baseCurrencySymbolVO.getExchangeRate(),baseCurrencySymbolVO.getCurrencySymbol());
                    }
                    log.info("支付成功 payment_intent， 订单号为：{}", stripeOutTradeNo);
                }
            }
        }else{
            log.info("event.getType()" + event.getType());
        }
        // 返回200 OK
        return ResponseEntity.ok().build();

    }

    public void notifyPay(String orderId,String stripeOutTradeNo) {
        // 判断订单在当前环境是否存在
        Integer orderNum = orderServiceClient.queryOrderNumExist(orderId);
        if(orderNum == 0){
            return;
        }
        usermsServiceClient.setVKInfo(orderId);
        // 更改销量
        List<WapGoodsSaleSumDTO> wapGood = orderServiceClient.queryGoodsByOrderId(orderId);
        goodServiceClient.updateGoodsSalenum(wapGood);
        // 订单号是纯数字
        if (MathUtil.isInteger(orderId)) {
            //进入回调纯数字订单逻辑处理
            orderIsInteger(orderId, stripeOutTradeNo);
        } else {
            //进入非纯数字订单逻辑处理
            orderIsNotInteger(orderId, stripeOutTradeNo);
        }
    }

    @Override
    public ResponseResult stripeReturnAmount(Map<Object, Object> para) {
        //账号私钥
        Stripe.apiKey = stripeSectret;
        // 退单金额
        BigDecimal returnPrice = new BigDecimal(para.get("returnAmount").toString());
        returnPrice = returnPrice.multiply(new BigDecimal(100));
        // 第三方订单号
        String tradeNo = para.get("tradeNo").toString();
//        String currencyCode = para.get("currencyCode").toString();
        try {
            RefundCreateParams createParams = RefundCreateParams.builder()
                    // 退款金额(以最小货币单位为单位，例如美分)
                    .setAmount(returnPrice.longValue())
//                    .setCurrency(currencyCode)//货币类型
                    //订单ID
                    .setPaymentIntent(tradeNo)
                    .build();

            Refund refund = Refund.create(createParams);
            log.info("退款ID", refund.getId());
            return ResponseResult.success();
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

    }



    /**
     * 当支付单号为纯数字订单
     *
     * @param outTradeNo
     * @param tradeNo
     */
    public void orderIsInteger(String outTradeNo, String tradeNo) {
        Map<String, String> orderIdIsNum = new HashMap<>();
        //虚拟订单 0是 1否
        Integer virtualGoods = null;
        //门店订单 1 是 2否
        Integer orderIsShopTake = null;
        // 是否同城配送2-否 1-是
        Integer isDelivery = null;
        RecordDTO queryGoodsPara = payConsumeTradeBackMapper.queryRecordPriceAndName(outTradeNo);
        //判断订单是否已支付成功
        if (!queryGoodsPara.getOrderStateId().equals(6)) {

            payConsumeTradeMapper.updateConsumeTradePaymentMoney(outTradeNo, 3);
            orderIdIsNum.put("orderId", outTradeNo);
            //订单号是数字
            orderIdIsNum.put("isNum", "0");

            //获取非数字订单
            String recordPayorder = orderServiceClient.queryAssetInfo(outTradeNo, "").get(0).getPaymentNumber();
            //修改拼团的支付状态
            Map<String, Object> pinTuanMap = new HashMap<>();
            pinTuanMap.put("orderId", recordPayorder);
            activityServiceClient.updatePinTuanPayStart(pinTuanMap);

            //判断是否是积分商品订单 并修改用户积分
            updateUserJIfen(outTradeNo);
            //判断是否是虚拟订单
            Map<String, Integer> judgeOrderMap = this.virtualWxExchangeCode(orderIdIsNum);
            virtualGoods = judgeOrderMap.get("virtualGoods");
            orderIsShopTake = judgeOrderMap.get("orderIsShopTake");
            isDelivery = judgeOrderMap.get("isDelivery");
            Map<Object, Object> map = new HashMap<Object, Object>();
            // 支付方式
            Integer payCode = CommonType.payType.PAY_TYPE_STRIPE.getCode();
            // 待发货
            Integer orderStatus = CommonType.orderStatus.TO_BE_SHIPPED.getCode();
            //虚拟订单 0 是 1 否
            if (null != virtualGoods && virtualGoods.equals(CommonType.orderType.IS_VIRTUAL.getCode())) {
                //如果是虚拟订单就将订单状态修改为 已发货
                orderStatus = CommonType.orderStatus.SHIPPED.getCode();
            }
            //门店订单 1 是 2 否 且不是同城配送
            if (null != orderIsShopTake && orderIsShopTake.equals(CommonType.orderType.IS_SHOP_TAKE.getCode()) && CommonType.DeliveryOrderStart.not_is_delivery.getCode().equals(isDelivery)) {
                //如果是门店订单就将订单状态修改为 已发货
                orderStatus = CommonType.orderStatus.SHIPPED.getCode();
            }
            // 支付方式
            Integer paymentId = CommonType.payType.ONLINE_PAY.getCode();
            // 支付方式名称
            String paymentName = "在线支付";
            // 支付时间
            String paymentTime = DateUtils.getNowDateToString();
            // 第三方支付号
            String paymentOtherNumber = tradeNo;
            // 支付方式
            map.put("payCode", payCode);
            map.put("paymentId", paymentId);
            // 商户订单号
            map.put("paymentNumber", outTradeNo);
            map.put("orderStatus", orderStatus);
            map.put("paymentName", paymentName);
            map.put("paymentTime", paymentTime);
            map.put("paymentOtherNumber", paymentOtherNumber);
            orderServiceClient.HFiveUpdateOrderBaseByPayment(map);
            // 更改支付表状态ConsumeRecord
            Integer recordStatus = 3;
            Map<String, Object> mapPureDigital = new HashMap<>();
            mapPureDigital.put("recordStatus", recordStatus);
            mapPureDigital.put("paymentTime", paymentTime);
            mapPureDigital.put("outTradeNo", outTradeNo);
            payConsumeRecordMapper.updateComsumeRecordByPureDigital(mapPureDigital);
            // 发送feign,提交下单通知
            Map<String, Object> messageMap = new HashMap<String, Object>();
            // 支付号
            messageMap.put("orderId", outTradeNo);
            messageMap.put("paymentTime", paymentTime);
            // 发送下单通知
            orderServiceClient.orderToInform(messageMap);
            // 更改ConsumeTrade
            Integer orderStateId = 3;
            String closeTime = DateUtils.getNowDateToString();

            Map<String, Object> mapDigital = new HashMap<>();
            mapDigital.put("orderStateId", orderStateId);
            mapDigital.put("paymentTime", paymentTime);
            mapDigital.put("closeTime", closeTime);
            mapDigital.put("outTradeNo", outTradeNo);
            payConsumeTradeBackMapper.updateConsumeTradeByPureDigital(mapDigital);
        }
    }
    /**
     * 当支付单号不是纯数字订单
     *
     * @param outTradeNo
     * @param tradeNo
     */
    public void orderIsNotInteger(String outTradeNo, String tradeNo) {
        Map<String, String> orderIdIsNum = new HashMap<>();
        //虚拟订单 0是 1否
        Integer virtualGoods = null;
        //门店订单 1 是 2否
        Integer orderIsShopTake = null;
        // 订单号不是纯数字Record表查询
        List<Integer> recodeStatus = payConsumeRecordMapper.queryRecodeStatus(outTradeNo);
        orderIdIsNum.put("orderId", outTradeNo);
        //订单号不是是数字
        orderIdIsNum.put("isNum", "1");

        //修改拼团的支付状态
        Map<String, Object> pinTuanMap = new HashMap<>();
        pinTuanMap.put("orderId", outTradeNo);
        activityServiceClient.updatePinTuanPayStart(pinTuanMap);

        //判断是否是积分商品订单 并修改用户积分
        updateUserJIfen(outTradeNo);
        //判断是否是虚拟订单
        Map<String, Integer> judgeOrderMap = this.virtualWxExchangeCode(orderIdIsNum);
        virtualGoods = judgeOrderMap.get("virtualGoods");
        orderIsShopTake = judgeOrderMap.get("orderIsShopTake");
        // 判断订单状态是否改变
        for (int i = 0; i < recodeStatus.size(); i++) {
            String orderId = null;
            if (recodeStatus.get(i) == 1) {
                List<ComsumeRecordVO> payConsumeRecord = payConsumeRecordMapper.queryorderNo(outTradeNo);
                BigDecimal money = new BigDecimal(0);
                for (int j = 0; j < payConsumeRecord.size(); j++) {
                    money = money.add(payConsumeRecord.get(j).getRecordMoney());
                }

                 Map<Object, Object> map = new HashMap<Object, Object>();
                 // 支付方式
                 Integer payCode = CommonType.payType.PAY_TYPE_STRIPE.getCode();
                 // 待发货
                 Integer orderStatus = 3;
                 //虚拟订单 0 是 1 否
                 if (null != virtualGoods && 0 == virtualGoods) {
                     //如果是虚拟订单就将订单状态修改为 已发货
                     orderStatus = 4;
                 }
                 //门店订单 0 是 1 否
                 if (null != orderIsShopTake && 0 == orderIsShopTake) {
                     //如果是门店订单就将订单状态修改为 已发货
                     orderStatus = 4;
                 }
                 // 支付方式
                 Integer paymentId = 1;
                 // 支付方式名称
                 String paymentName = "在线支付";
                 // 支付时间
                 String paymentTime = DateUtils.getNowDateToString();
                 // 第三方支付号
                 String paymentOtherNumber = tradeNo;
                 // 支付方式
                 map.put("payCode", payCode);
                 map.put("paymentId", paymentId);
                 // 商户订单号
                 map.put("paymentNumber", outTradeNo);
                 map.put("orderStatus", orderStatus);
                 map.put("paymentName", paymentName);
                 map.put("paymentTime", paymentTime);
                 map.put("paymentOtherNumber", paymentOtherNumber);
                 orderServiceClient.HFiveUpdateOrderBaseByPayment(map);
                 // 更改支付表状态ConsumeRecord
                 Integer recordStatus = 3;
                 Map<String, Object> mapPureDigital = new HashMap<>();
                 mapPureDigital.put("recordStatus", recordStatus);
                 mapPureDigital.put("paymentTime", paymentTime);
                 mapPureDigital.put("outTradeNo", outTradeNo);
                 payConsumeRecordMapper.updateComsumeRecord(mapPureDigital);
                 // 更改ConsumeTrade
                 Integer orderStateId = 3;
                 String closeTime = DateUtils.getNowDateToString();
                 Map<String, Object> mapDigital = new HashMap<>();
                 mapDigital.put("orderStateId", orderStateId);
                 mapDigital.put("paymentTime", paymentTime);
                 mapDigital.put("closeTime", closeTime);
                 mapDigital.put("outTradeNo", outTradeNo);
                 payConsumeTradeBackMapper.updateConsumeTrade(mapDigital);


                 // 发送feign,提交下单通知
                 Map<String, Object> messageMap = new HashMap<String, Object>();
                 // 支付号
                 messageMap.put("orderId", outTradeNo);
                 messageMap.put("paymentTime", paymentTime);
                 // 发送下单通知
                 orderServiceClient.orderToInform(messageMap);

            }
        }
    }
    /**
     * 如果是积分订单就 修改用户积分
     *
     * @param orderId
     */
    public void updateUserJIfen(String orderId) {
        Integer amountAotalIntegral = null;
        //积分商品 0  是  1 否
        Integer integralGoods = 1;
        //是纯数字
        RecordDTO queryGoodsPara = null;
        if (MathUtil.isInteger(orderId)) {
            queryGoodsPara = payConsumeTradeBackMapper.queryRecordPriceAndNameByTradeId(orderId);
        //不是纯数字
        } else {
            queryGoodsPara = payConsumeTradeBackMapper.queryRecordPriceAndName(orderId);
        }
        if (null == queryGoodsPara) {
            return;
        }
        GoodsDTO queryGoodsPara2 = payConsumeTradeBackMapper.queryGoodsPriceAndName(queryGoodsPara.getOrderId(), queryGoodsPara.getBuyerId());
        if (null == queryGoodsPara2 && null != queryGoodsPara2.getTotalIntegral()) {
            return;
        }
        //获取积分商品订单的积分
        Integer totalIntegral = queryGoodsPara2.getTotalIntegral();
        //积分商品 0  是  1 否
        integralGoods = queryGoodsPara2.getIntegralGoods();
        amountAotalIntegral = totalIntegral;
        if (null != integralGoods && integralGoods == 0) {
            //获取用户积分
            Integer userIntegral = usermsServiceClient.getMemberIntegralByUserId(queryGoodsPara.getBuyerId());
            if (null != userIntegral && amountAotalIntegral <= userIntegral) {
                //扣除积分
                //修改会员积分和会员等级
                MemberMessageListIntegralDTO updateMemberMessageListIntegral = new MemberMessageListIntegralDTO();
                ArrayList<String> list = new ArrayList<>();
                list.add(queryGoodsPara.getBuyerId());
                updateMemberMessageListIntegral.setUserIdList(list);
                updateMemberMessageListIntegral.setIntegral(amountAotalIntegral);
                //1是增加积分 0 是扣除积分
                updateMemberMessageListIntegral.setType(CommonType.commonStatus.NO.getCode());
                usermsServiceClient.updateMemberIntegral(updateMemberMessageListIntegral);
                //添加积分修改记录
                Map<String, Object> pointParam = new HashMap<>();
                pointParam.put("PointsLogType", CommonType.pointsLogType.CONSUME.getCode());
                pointParam.put("ClassId", CommonType.pointType.POINTS_EXCHANGE_GOODS.getCode());
                pointParam.put("PointsLogTime", new Date());
                pointParam.put("PointsLogDesc", CommonType.pointType.POINTS_EXCHANGE_GOODS.getDesc());
                pointParam.put("PointsLogFlag", "reg");
                pointParam.put("UserId", queryGoodsPara.getBuyerId());
                pointParam.put("PointsLogPoints", amountAotalIntegral);
                pointParam.put("PointsLogOperate", CommonType.pointType.POINTS_EXCHANGE_GOODS.getDesc());
                usermsServiceClient.insertPointLogByMap(pointParam);
            }
        }
    }
    /**
     * 判断微信小程序支付的订单是否为虚拟订单或自提订单，并为虚拟订单生成兑换码。
     *
     * @param orderIdIsNum 包含订单ID和其他信息的映射表。
     * @return 返回一个映射表，其中包含虚拟商品标识、订单提取方式和配送方式等信息。
     */
    public Map<String, Integer> virtualWxExchangeCode(Map<String, String> orderIdIsNum) {
        // 初始化变量，用于存储虚拟商品标识、订单ID、兑换码URL、兑换码、订单提取方式和配送方式等信息。
        Integer virtualGoods = 1;
        String orderId = null;
        String redeemCodeUrl = null;
        String code = null;
        Integer orderIsShopTake = 2;
        // 是否同城配送2-否 1-是
        Integer isDelivery = null;
        // 创建一个映射表，用于存储判断结果。
        Map<String, Integer> judgeOrderMap = new HashMap<>();
        try {
            // 调用服务检查订单是否为虚拟订单。
            ResponseResult res = orderServiceClient.isVirtualOrder(orderIdIsNum);
            if (null != res.getData()) {
                // 从返回的数据中提取订单信息。
                Map<String, Object> map = (Map<String, Object>) res.getData();
                virtualGoods = Integer.valueOf(map.get("virtualGoods").toString());
                orderId = (String) map.get("orderId");
                orderIsShopTake = Integer.valueOf(map.get("orderIsShopTake").toString());
                isDelivery = Integer.valueOf(map.get("isDelivery").toString());
            }

            // 如果订单是虚拟订单或自提订单，则生成兑换码。
            // 虚拟订单 0 是 1 否
            if (null != virtualGoods && 0 == virtualGoods || null != orderIsShopTake && 1 == orderIsShopTake) {
                // 生成兑换码的唯一标识。
                SimpleDateFormat df = new SimpleDateFormat("ddHHmmss");
                String timeStamp = df.format(new Date());
                String temp = String.valueOf(Math.round((Math.random() + 1) * 1000));
                code = temp + timeStamp;
                // 生成兑换码的二维码并上传至OSS。
                byte[] b = QrCodeUtil.createQRCode(150, 150, code);
                String encode = Base64Utils.encode2(b);
                OssUploadUpdateParamDTO param = new OssUploadUpdateParamDTO();
                param.setBase64Str(encode);
                param.setStreamName(".png");
                ResponseResult responseResult = thirdPartyServiceClient.uploadToOssBase64(param);
                // 保存兑换码信息到数据库。
                redeemCodeUrl = (String) responseResult.getData();
                Map<String, Object> param2 = new HashMap<>();
                param2.put("order", orderId);
                param2.put("redeemCode", code);
                param2.put("redeemCodeUrl", redeemCodeUrl);
                orderServiceClient.addRedeemCode(param2);
            }

            // 将判断结果存入映射表中返回。
            judgeOrderMap.put("virtualGoods", virtualGoods);
            judgeOrderMap.put("orderIsShopTake", orderIsShopTake);
            judgeOrderMap.put("isDelivery", isDelivery);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return judgeOrderMap;
    }
}
