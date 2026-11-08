package com.houjiang.payment.service.impl;

import com.paypal.api.payments.*;
import com.paypal.api.payments.Payer;
import com.paypal.base.rest.PayPalRESTException;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import com.paypal.orders.Order;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.base.MemberMessageListIntegralDTO;
import com.houjiang.commons.dto.oos.OssUploadUpdateParamDTO;
import com.houjiang.commons.dto.order.WapGoodsSaleSumDTO;
import com.houjiang.commons.util.*;
import com.houjiang.payment.common.PayPalUtils;
import com.houjiang.payment.common.PaypalPaymentIntent;
import com.houjiang.payment.common.PaypalPaymentMethod;
import com.houjiang.payment.dto.PayConsumeRecordDTO;
import com.houjiang.payment.dto.QueryGoodsParaDTO;
import com.houjiang.payment.dto.QueryRecordParaDTO;
import com.houjiang.payment.feign.*;
import com.houjiang.payment.mapper.PayConsumeRecordMapper;
import com.houjiang.payment.mapper.PayConsumeTradeMapper;
import com.houjiang.payment.service.PaypalService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName PaypalServiceImpl
 * @Description Paypal支付实现
 * @Author Amos
 * @Date 2024/4/24 17:54
 * @Version 1.0
 * @author Amos
 */
@Service
public class PaypalServiceImpl implements PaypalService {

    final static Logger log = LoggerFactory.getLogger(PaypalServiceImpl.class);

    @Autowired
    private GoodServiceClient goodServiceClient;

    @Autowired
    private OrderServiceClient orderServiceClient;

    @Autowired
    private ActivityServiceClient activityServiceClient;

    @Resource
    private PayConsumeTradeMapper payConsumeTradeMapper;

    @Resource
    private PayConsumeRecordMapper payConsumeRecordMapper;

    @Autowired
    private UsermsServiceClient usermsServiceClient;

    @Autowired
    private ThirdPartyServiceClient thirdPartyServiceClient;

    @Autowired
    private  PayPalUtils payPalUtils;
    @Override
    public Order createPaymentApp(BigDecimal total,
                                  String currency,
                                  String cancelUrl,
                                  String successUrl,
                                  String mode,
                                  String clientId,
                                  String clientSecret){
        // 设置环境沙盒或生产
        PayPalHttpClient client = payPalUtils.client(mode, clientId, clientSecret);
        // 配置请求参数
        OrderRequest orderRequest = new OrderRequest();
        // 设置支付意图为“CAPTURE”，表示在结账时将立即扣款
        orderRequest.checkoutPaymentIntent("CAPTURE");

        // 初始化购买单元列表
        List<PurchaseUnitRequest> purchaseUnits = new ArrayList<>();
        // 创建一个购买单元，设置货币代码和总金额
        purchaseUnits.add(new PurchaseUnitRequest().amountWithBreakdown(new AmountWithBreakdown().currencyCode(currency).value(total.toString())));
        // 将购买单元列表设置到订单请求中
        orderRequest.purchaseUnits(purchaseUnits);

        // 设置应用上下文，包括成功和取消URL
        orderRequest.applicationContext(new ApplicationContext().returnUrl(successUrl).cancelUrl(cancelUrl));

        // 创建订单创建请求，并设置订单请求体
        OrdersCreateRequest request = new OrdersCreateRequest().requestBody(orderRequest);

        HttpResponse<Order> response = null;
        try {
            // 执行订单创建请求
            response = client.execute(request);
        } catch (IOException e) {
            // 记录IO异常信息
            log.error(e.getMessage());
        }

        // 获取订单响应结果
        Order order = response.result();
        // 记录订单ID信息
        log.info("orderId is {}",order);

        // 返回订单对象
        return order;

    }

    /**
     * 创建PayPal支付。
     * 该方法通过PayPal的REST API创建一个支付对象，包括交易金额、货币类型、支付方式、支付意图、交易描述、取消URL和成功URL。
     * 创建的支付对象将包含一个交易和一个支付者的信息，并设置好回调URL。
     * 最后，通过调用PayPal API来创建这个支付对象。
     *
     * @param total 支付的总金额。
     * @param currency 交易的货币类型。
     * @param method 支付方式，使用PaypalPaymentMethod枚举定义。
     * @param intent 支付意图，使用PaypalPaymentIntent枚举定义。
     * @param description 交易的描述信息。
     * @param cancelUrl 用户取消支付时重定向的URL。
     * @param successUrl 用户完成支付时重定向的URL。
     * @return 创建的Payment对象，包含PayPal支付的所有相关信息。
     * @throws PayPalRESTException 如果与PayPal API的交互出现错误，将抛出此异常。
     */
    @Override
    public Payment createPayment(
            BigDecimal total,
            String currency,
            PaypalPaymentMethod method,
            PaypalPaymentIntent intent,
            String description,
            String cancelUrl,
            String successUrl) throws PayPalRESTException {
        // 设置交易金额和货币类型
        //设置金额和单位对象
        Amount amount = new Amount();
        amount.setCurrency(currency);
        amount.setTotal(String.format("%.2f", total));

        // 设置交易详细信息，包括交易描述和金额
        //设置具体的交易对象
        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);

        // 创建交易列表，并添加刚才创建的交易
        //交易集合-可以添加多个交易对象
        List<Transaction> transactions = new ArrayList<Transaction>();
        transactions.add(transaction);

        // 设置支付者信息，包括支付方式
        Payer payer = new Payer();
        payer.setPaymentMethod(method.toString());

        // 创建支付对象，设置支付意图、支付者和交易信息
        Payment payment = new Payment();
        payment.setIntent(intent.toString());
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        // 设置回调URLs
        //设置反馈url
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl);

        // 将回调URLs设置到支付对象中
        //加入反馈对象
        payment.setRedirectUrls(redirectUrls);

        // 调用PayPal API创建支付对象，并返回创建的支付对象
        //加入认证并创建交易
        return payment.create(payPalUtils.apiContext());
    }


    /**
     * 执行支付操作。
     * 根据给定的支付ID和支付者ID，对支付进行执行。此方法封装了支付的执行流程，首先根据支付ID查找支付，
     * 然后使用支付者ID对支付进行执行。
     *
     * @param paymentId 支付的唯一标识ID。用于查找特定的支付操作。
     * @param payerId   支付者的唯一标识ID。用于指定支付的发起者。
     * @return PaymentExecution 对象，包含支付执行的结果。
     * @throws PayPalRESTException 如果执行支付过程中遇到任何问题，将抛出此异常。
     */
    @Override
    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        // 根据支付ID初始化Payment对象，并设置支付的唯一标识ID。
        Payment payment = new Payment();
        payment.setId(paymentId);

        // 根据支付者ID初始化PaymentExecution对象，并设置支付者的唯一标识ID。
        PaymentExecution paymentExecute = new PaymentExecution();
        paymentExecute.setPayerId(payerId);

        // 执行支付操作，传入支付对象和支付执行对象，返回支付结果。
        return payment.execute(payPalUtils.apiContext(), paymentExecute);
    }


    /**
     * 退款函数，针对指定的交易订单进行退款操作。
     *
     * @param tradeNo 交易订单号，用于唯一标识一个交易。
     * @param money 需要退款的金额，以BigDecimal形式表示，确保精确度。
     * @return 返回一个布尔值，表示退款操作是否成功。true表示成功，false表示失败。
     */
    @Override
    public boolean refundSale(String tradeNo, BigDecimal money,String currencyCode ) {
        // 初始化标志位为true，表示默认退款操作是成功的。
        boolean flag = true;

        // 创建退款请求对象，并设置退款金额和原因。
        RefundRequest refund = new RefundRequest();
        Amount amount = new Amount();
        amount.setCurrency(currencyCode);
        amount.setTotal(money.toString());
        refund.setAmount(amount);
        refund.setReason("超时退款");

        try {
            // 创建销售对象，设置交易订单号，并调用退款方法尝试进行退款。
            Sale sale = new Sale();
            sale.setId(tradeNo);
            DetailedRefund res = sale.refund(payPalUtils.apiContext(), refund);
            // 记录退款结果信息到日志。
            log.info("对象："+res);
            return flag;
        } catch (PayPalRESTException e) {
            // 如果退款过程中发生异常，记录错误日志，并将标志位设置为false。
            log.error("failed to refund saleId=" + tradeNo, e);
            flag = false;
        }
        return flag;
    }


    @Override
    public void notifyPay(String out_trade_no,String transaction_id) {
        // 更改销量
        List<WapGoodsSaleSumDTO> wapGood = orderServiceClient.queryGoodsByOrderId(out_trade_no);
        goodServiceClient.updateGoodsSalenum(wapGood);
        Map<String, String> orderIdIsNum = new HashMap<>();
        //虚拟订单 0是 1否
        Integer virtualGoods = null;
        //门店订单 1 是 2否
        Integer orderIsShopTake = null;
        // 判断纯数字订单，实际支付订单
        if (PayToolUtil.isInteger(out_trade_no)) {
            QueryRecordParaDTO queryGoodsPara = payConsumeTradeMapper.queryRecordPriceAndName(out_trade_no);
            if (null != queryGoodsPara && !queryGoodsPara.getOrderStateId().equals(6)) {
                log.info("----判断成功----");
                payConsumeTradeMapper.updateConsumeTradePaymentMoney(out_trade_no, 3);
                orderIdIsNum.put("orderId", out_trade_no);
                //订单号是数字
                orderIdIsNum.put("isNum", "0");

                //获取非数字订单
                String recordPayorder = orderServiceClient.queryAssetInfo(out_trade_no, "").getFirst().getPaymentNumber();
                //修改拼团的支付状态
                Map<String, Object> pinTuanMap = new HashMap<>();
                pinTuanMap.put("orderId", recordPayorder);
                activityServiceClient.updatePinTuanPayStart(pinTuanMap);

                //判断是否是积分商品订单 扣除积分
                updateUserJIfen(out_trade_no);
                //判断是否是虚拟订单
                Map<String, Integer> judgeOrderMap = this.virtualWxExchangeCode(orderIdIsNum);
                virtualGoods = judgeOrderMap.get("virtualGoods");
                orderIsShopTake = judgeOrderMap.get("orderIsShopTake");
                Map<Object, Object> map = new HashMap<Object, Object>();
                Integer payCode = 16;
                // 待发货
                Integer orderStatus = 3;
                //门店订单 0 是 1 否
                if (null != orderIsShopTake && 1 == orderIsShopTake) {
                    //如果是门店订单就将订单状态修改为 已发货
                    orderStatus = 4;
                }
                //虚拟订单 0 是 1 否
                if (null != virtualGoods && 0 == virtualGoods) {
                    //如果是虚拟订单就将订单状态修改为 已发货
                    orderStatus = 4;
                }
                // 支付方式
                Integer paymentId = 1;
                // 支付方式名称
                String paymentName = "在线支付";
                // 支付时间
                String paymentTime = DateUtils.getTime();
                // 第三方支付号
                String paymentOtherNumber = transaction_id;
                String orderFrom = RedisUtil.get(HouConfig.ORDERFORM + out_trade_no);
                map.put("payCode", payCode);
                map.put("orderFrom", orderFrom);
                map.put("paymentId", paymentId);
                // 商户订单号
                map.put("paymentNumber", out_trade_no);
                map.put("orderStatus", orderStatus);
                map.put("paymentName", paymentName);
                map.put("paymentTime", paymentTime);
                map.put("paymentOtherNumber", paymentOtherNumber);
                orderServiceClient.HFiveUpdateOrderBaseByPayment(map);
                // 更改支付表状态ConsumeRecord
                Integer recordStatus = 3;
                Map<String, Object> recordMap = new HashMap<>();
                recordMap.put("recordStatus", recordStatus);
                recordMap.put("paymentTime", paymentTime);
                recordMap.put("outTradeNo", out_trade_no);
                payConsumeRecordMapper.updateComsumeRecordByPureDigital(recordMap);
                // 更改ConsumeTrade
                Integer orderStateId = 3;
                // 支付方式
                Integer paymentChannelId = 4;
                String closeTime = DateUtils.getTime();
                payConsumeTradeMapper.updateConsumeTradeByPureDigital(orderStateId,
                        paymentTime, closeTime, out_trade_no, paymentChannelId);
                // 发送feign,提交下单通知
                Map<String, Object> messageMap = new HashMap<String, Object>();
                // 支付号
                messageMap.put("orderId", out_trade_no);
                messageMap.put("paymentTime", paymentTime);
                // 发送下单通知
                orderServiceClient.orderToInform(messageMap);
            }
        } else {
            orderIdIsNum.put("orderId", out_trade_no);
            //订单号不是数字
            orderIdIsNum.put("isNum", "1");
            //判断是否是虚拟订单
            Map<String, Integer> judgeOrderMap = this.virtualWxExchangeCode(orderIdIsNum);
            virtualGoods = judgeOrderMap.get("virtualGoods");
            // 支付订单号
            orderIsShopTake = judgeOrderMap.get("orderIsShopTake");
            List<PayConsumeRecordDTO> payConsumeRecord = payConsumeRecordMapper.queryOrderNo(out_trade_no);
            List<Integer> allRecordStatus = payConsumeRecordMapper.queryAllRecordStatus(out_trade_no);
            BigDecimal money = new BigDecimal(0);
            for (int i = 0; i < payConsumeRecord.size(); i++) {
                money = money.add(payConsumeRecord.get(i).getRecordMoney());
            }
            if (allRecordStatus.contains(1)) {
                payConsumeTradeMapper.updateConsumeTradePaymentMoney(out_trade_no, 3);
                Map<Object, Object> map = new HashMap<Object, Object>();
                Integer payCode = 16;
                // 待发货
                Integer orderStatus = 3;

                //修改拼团的支付状态
                Map<String, Object> pinTuanMap = new HashMap<>();
                pinTuanMap.put("orderId", out_trade_no);
                activityServiceClient.updatePinTuanPayStart(pinTuanMap);

                //判断是否是积分商品订单 扣除积分
                updateUserJIfen(out_trade_no);
                //门店订单 0 是 1 否
                if (null != orderIsShopTake && 1 == orderIsShopTake) {
                    //如果是门店订单就将订单状态修改为 已发货
                    orderStatus = 4;
                }
                //虚拟订单 0 是 1 否
                if (null != virtualGoods && 0 == virtualGoods) {
                    //如果是虚拟订单就将订单状态修改为 已发货
                    orderStatus = 4;
                }
                // 支付方式
                Integer paymentId = 1;
                // 支付方式名称
                String paymentName = "在线支付";
                // 支付时间
                String paymentTime = DateUtils.getTime();
                // 第三方支付号
                String paymentOtherNumber = transaction_id;
                String orderFrom = RedisUtil.get(HouConfig.ORDERFORM + out_trade_no);
                map.put("payCode", payCode);
                map.put("orderFrom", orderFrom);
                map.put("paymentId", paymentId);
                // 商户订单号
                map.put("paymentNumber", out_trade_no);
                map.put("orderStatus", orderStatus);
                map.put("paymentName", paymentName);
                map.put("paymentTime", paymentTime);
                map.put("paymentOtherNumber", paymentOtherNumber);
                orderServiceClient.HFiveUpdateOrderBaseByPayment(map);
                // 更改支付表状态ConsumeRecord
                Integer recordStatus = 3;
                Map<String, Object> recordMap = new HashMap<>();
                recordMap.put("recordStatus", recordStatus);
                recordMap.put("paymentTime", paymentTime);
                recordMap.put("outTradeNo", out_trade_no);
                payConsumeRecordMapper.updateComsumeRecord(recordMap);
                // 更改ConsumeTrade
                Integer orderStateId = 3;
                // 支付方式
                Integer paymentChannelId = 4;
                String closeTime = DateUtils.getTime();
                payConsumeTradeMapper.updateConsumeTrade(orderStateId, paymentTime,
                        closeTime, out_trade_no, paymentChannelId);
                // 发送feign,提交下单通知
                Map<String, Object> messageMap = new HashMap<String, Object>();
                // 支付号
                messageMap.put("orderId", out_trade_no);
                messageMap.put("paymentTime", paymentTime);
                // 发送下单通知
                orderServiceClient.orderToInform(messageMap);
            }
        }

    }

    /**
     * 如果是积分订单就 修改用户积分
     */
    public void updateUserJIfen(String orderId) {
        Integer amountAotalIntegral = null;
        //积分商品 0  是  1 否
        Integer integralGoods = 1;
        //是纯数字
        QueryRecordParaDTO queryGoodsPara = null;
        if (PayToolUtil.isInteger(orderId)) {
            queryGoodsPara = payConsumeTradeMapper.queryRecordPriceAndName(orderId);
        } else {
            //不是纯数字
            queryGoodsPara = payConsumeTradeMapper.queryRecordPriceAndName(orderId);
        }
        if (null == queryGoodsPara) {
            return;
        }
        QueryGoodsParaDTO queryGoodsParaDTO2 = payConsumeTradeMapper.queryGoodsOrderAndUser(queryGoodsPara.getOrderId(), queryGoodsPara.getBuyerId());
        if (null == queryGoodsParaDTO2 && null != queryGoodsParaDTO2.getTotalIntegral()) {
            return;
        }
        //获取积分商品订单的积分
        Integer totalIntegral = queryGoodsParaDTO2.getTotalIntegral();
        //积分商品 0  是  1 否
        integralGoods = queryGoodsParaDTO2.getIntegralGoods();
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
                updateMemberMessageListIntegral.setType(0);
                usermsServiceClient.updateMemberIntegral(updateMemberMessageListIntegral);
                //添加积分修改记录
                Map<String, Object> pointParam = new HashMap<>();
                pointParam.put("PointsLogType", CommonType.pointsLogType.CONSUME.getCode());
                pointParam.put("ClassId", CommonType.pointType.POINTS_EXCHANGE_VOUCHER.getCode());
                pointParam.put("PointsLogTime", DateUtils.getTime());
                pointParam.put("PointsLogDesc", CommonType.pointType.POINTS_EXCHANGE_GOODS.getDesc());
                pointParam.put("PointsLogFlag", "reg");
                pointParam.put("UserId", queryGoodsPara.getBuyerId());
                pointParam.put("PointsLogPoints", amountAotalIntegral);
                pointParam.put("PointsLogOperate", CommonType.pointType.POINTS_EXCHANGE_GOODS.getDesc());
                pointParam.put("OrderId", orderId);
                usermsServiceClient.insertPointLogByMap(pointParam);
            }
        }
    }

    /**
     * 判断微信小程序支付的订单是否为虚拟订单或自提订单，并为虚拟订单生成兑换码。
     *
     * @param orderIdIsNum 一个包含订单ID的Map，ID可能是数字或字符串。
     * @return 返回一个Map，包含订单类型和是否为自提订单的信息。
     */
    public Map<String, Integer> virtualWxExchangeCode(Map<String, String> orderIdIsNum) {
        // 初始化变量，用于存储虚拟商品标识、订单ID、兑换码URL、兑换码和是否为自提订单的标识
        Integer virtualGoods = 1;
        String orderId = null;
        String redeemCodeUrl = null;
        String code = null;
        Integer orderIsShopTake = 2;
        // 初始化用于返回结果的Map
        Map<String, Integer> judgeOrderMap = new HashMap<>();
        try {
            // 调用服务判断订单是否为虚拟订单
            ResponseResult res = orderServiceClient.isVirtualOrder(orderIdIsNum);
            if (null != res.getData()) {
                // 从返回结果中提取订单信息
                Map<String, Object> map = (Map<String, Object>) res.getData();
                virtualGoods = (Integer) map.get("virtualGoods");
                orderId = (String) map.get("orderId");
                orderIsShopTake = (Integer) map.get("orderIsShopTake");
            }
            // 如果订单是虚拟订单或自提订单，则生成兑换码
            // 虚拟订单 0 是 1 否
            if (null != virtualGoods && 0 == virtualGoods || null != orderIsShopTake && 1 == orderIsShopTake) {
                // 生成兑换码的唯一标识
                SimpleDateFormat df = new SimpleDateFormat("ddHHmmss");
                String timeStamp = df.format(new Date());
                String temp = String.valueOf(Math.round((Math.random() + 1) * 1000));
                code = temp + timeStamp;
                // 生成兑换码的二维码图片
                ResponseResult resultResp = QrCodeUtil.GenerateQRCode(code);
                redeemCodeUrl = String.valueOf(thirdPartyServiceClient.uploadToOssBase64((OssUploadUpdateParamDTO) resultResp.getData()).getData());
                // 构建包含兑换码信息的参数Map，用于添加兑换码到订单
                Map<String, Object> param = new HashMap<>();
                param.put("order", orderId);
                param.put("redeemCode", code);
                param.put("redeemCodeUrl", redeemCodeUrl);
                // 为订单添加兑换码
                orderServiceClient.addRedeemCode(param);
            }
            // 将虚拟商品标识和是否为自提订单的标识添加到返回的Map中
            judgeOrderMap.put("virtualGoods", virtualGoods);
            judgeOrderMap.put("orderIsShopTake", orderIsShopTake);
        } catch (Exception e) {
            // 打印异常堆栈信息
            e.printStackTrace();
        }
        // 返回包含订单信息的Map
        return judgeOrderMap;
    }


}
