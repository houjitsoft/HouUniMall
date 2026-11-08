package com.houjiang.payment.service.impl;


import com.houjiang.commons.dto.base.MemberMessageListIntegralDTO;
import com.houjiang.commons.dto.oos.OssUploadUpdateParamDTO;
import com.houjiang.commons.dto.userms.UserPointLogDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.*;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.feign.ActivityServiceClient;
import com.houjiang.payment.feign.OrderServiceClient;
import com.houjiang.payment.feign.ThirdPartyServiceClient;
import com.houjiang.payment.feign.UsermsServiceClient;
import com.houjiang.payment.mapper.PayConsumeRecordMapper;
import com.houjiang.payment.service.PaymentBalanceService;
import com.houjiang.payment.vo.QueryGoodsVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class PaymentBalanceServiceImpl implements PaymentBalanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentBalanceServiceImpl.class);
    @Autowired
    private PayConsumeRecordMapper recordMapper;

    @Autowired
    private UsermsServiceClient usermsServiceClient;

    @Autowired
    private OrderServiceClient orderServiceClient;

    @Autowired
    private ThirdPartyServiceClient thirdPartyServiceClient;

    @Autowired
    private ActivityServiceClient activityServiceClient;

    @Override
    public ResponseResult checkPaymentVocher(Map<Object, Object> map) {
        Map<String,String> orderIdIsNum = new HashMap<>();
        //虚拟订单 0 是 1 否
        Integer virtualGoods =null;
        //门店字体订单
        Integer orderIsShopTake = null;
        // 获取支付订单编号，并改变状态
        // 接收付款订单编号
        String orderId = (String) map.get("orderIdList");
        Map<String,Object> userMap = orderServiceClient.queryBuyUserId(orderId);
        String userId = userMap.get("buyerUserId").toString();
        orderIdIsNum.put("orderId",orderId);
        BigDecimal amountActuallyPaids = new BigDecimal(0);
        Integer amountAotalIntegral = null;
        //积分商品 0  是  1 否
        Integer integralGoods = 1;
        // 判断纯数字订单
        if (PayToolUtil.isInteger(orderId)) {
            QueryGoodsVO queryGoodsPara = recordMapper.queryGoodsPriceAndName(orderId, userId);
            BigDecimal paymentAmount = queryGoodsPara.getTradePaymentAmount();
            //获取积分商品订单的积分
            Integer totalIntegral = queryGoodsPara.getTotalIntegral();
            //积分商品 0  是  1 否
            integralGoods = queryGoodsPara.getIntegralGoods();
            amountAotalIntegral = totalIntegral;
            amountActuallyPaids = paymentAmount;
            //订单号是数字
            orderIdIsNum.put("isNum","0");
        }
        if(null != integralGoods && integralGoods == 0){
            //获取用户积分
            Integer userIntegral = usermsServiceClient.getMemberIntegralByUserId(userId);
            if(null != userIntegral && amountAotalIntegral <= userIntegral){
                //扣除积分
                //修改会员积分和会员等级
                MemberMessageListIntegralDTO updateMemberMessageListIntegral = new MemberMessageListIntegralDTO();
                ArrayList<String> list = new ArrayList<>();
                list.add(userId);
                updateMemberMessageListIntegral.setUserIdList(list);
                updateMemberMessageListIntegral.setIntegral(amountAotalIntegral);
                //1是增加积分 0 是扣除积分
                updateMemberMessageListIntegral.setType(0);
                usermsServiceClient.updateMemberIntegral(updateMemberMessageListIntegral);
                // 创建用户积分日志DTO对象，用于记录用户的积分操作信息
                UserPointLogDTO userPointLog = new UserPointLogDTO();

                // 设置积分日志的类型为兑换，这里使用数字2代表兑换类型
                userPointLog.setPointsLogType(2);

                // 设置积分操作所关联的班级ID，这里假设为7
                userPointLog.setClassId(7);

                // 设置积分日志的时间为当前时间
                userPointLog.setPointsLogTime(new Date());

                // 设置积分日志的描述为“积分兑换商品”，记录积分的具体使用场景
                userPointLog.setPointsLogDesc("积分兑换商品");

                // 设置积分日志的标记为“reg”，可能用于区分不同来源或类型的积分操作
                userPointLog.setPointsLogFlag("reg");

                // 设置积分操作所影响的用户ID
                userPointLog.setUserId(userId);

                // 设置本次积分操作的积分数量
                userPointLog.setPointsLogPoints(amountAotalIntegral);

                // 设置积分操作的类型为“积分兑换商品”，与描述相呼应，强调操作的性质
                userPointLog.setPointsLogOperate("积分兑换商品");

                // 设置本次积分操作关联的订单ID，用于追踪和审计
                userPointLog.setOrderId(orderId);

                // 这里通过usermsServiceClient的feginAddpointsLog方法，将积分日志信息提交到远端系统进行处理
                usermsServiceClient.feginAddpointsLog(userPointLog);

            }else{
                //用户积分不足
                return ResponseResult.fail(BizCodeEnume.PAY_INTEGRAL_ERROR);
            }
        }
        //判断是否是虚拟订单
        Map<String, Integer> judgeOrderMap = this.virtualWxExchangeCode(orderIdIsNum);
        virtualGoods = judgeOrderMap.get("virtualGoods");
        orderIsShopTake = judgeOrderMap.get("orderIsShopTake");
        // 调用方法,更改状态
        // 存放值
        Map<Object, Object> statusMap = new HashMap<Object, Object>();
        // 是纯数字订单号，存放纯数字订单
        // 存储订单编号
        statusMap.put("orderId", orderId);
        // 存储用户ID
        statusMap.put("userId", userId);
        // 存储用户名，从用户映射中获取买家用户名
        statusMap.put("userName", userMap.get("buyerUserName").toString());
        // 设置订单类型为12
        statusMap.put("type", 12);
        // 存储实际支付金额
        statusMap.put("paymentAmount", amountActuallyPaids);
        // 存储总积分金额
        statusMap.put("amountAotalIntegral", amountAotalIntegral);

        //虚拟订单 0 是 1 否
        if(null != virtualGoods &&  0 == virtualGoods ){
            statusMap.put("virtualGoods",virtualGoods);
        }
        //虚拟订单 0 是 1 否
        if(null != orderIsShopTake &&  1 == orderIsShopTake ){
            statusMap.put("orderIsShopTake",orderIsShopTake);
        }
        changeStatus(statusMap);
        return ResponseResult.success();
    }

    /**
     * 更改状态
     *
     * @param map
     */
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Map<Object, Object> map) {
        // 从map中获取订单ID，用于后续订单处理
        String orderId = (String) map.get("orderId");
        // 从map中获取订单类型，用于区分不同类型的订单
        Integer type = (Integer) map.get("type");
        // 从map中获取虚拟商品的数量，用于统计订单中的虚拟商品总量
        Integer virtualGoods = (Integer) map.get("virtualGoods");
        // 从map中获取订单是否由店铺自取的标志，用于安排配送或自取
        Integer orderIsShopTake = (Integer) map.get("orderIsShopTake");
        // 从map中获取订单总积分金额，用于计算订单的积分优惠
        Integer amountAotalIntegral = (Integer) map.get("amountAotalIntegral");
        // 获取支付时间，用于记录订单的支付时间点
        // 支付时间
        String paymentTime = DateUtils.getTime();
        // 从map中获取用户支付后的剩余金额，用于计算用户的余额
        BigDecimal money = (BigDecimal) map.get("afterUserMoney");

        if (null == money) {
            money = new BigDecimal(0);
        }
        String userId = null;
        String userName = null;
        if(null == map.get("token")){
            userId = map.get("userId").toString();
            userName = map.get("userName").toString();
        }else{
            UserInfoVO ui = ParseTokenUtils.getUserInfoByToken((String) map.get("token"));
            userId = ui.getUserId();
            userName = ui.getUserName();
        }

        try {
            // 发送feign,提交下单通知
            Map<String, Object> messageMap = new HashMap<String, Object>();
            messageMap.put("userId", userId);
            messageMap.put("userName", userName);
            // 支付号
            messageMap.put("orderId", orderId);
            messageMap.put("paymentTime", paymentTime);
            // 发送下单通知
            ResponseResult responseResult = orderServiceClient.orderToInform(messageMap);
            if (responseResult.getCode() == 1) {
                LOGGER.info("-----OrderToInformUpdateSuccess-----");
            } else {
                LOGGER.info("-----OrderToInformUpdateFail-----");
            }

            // 更新trade&record状态
            LOGGER.info("StartUpdateTrade&RecordStatusTheFirstTime:" + DateUtils.getTime());
            boolean isUpdateRecordSuccess = false;
            boolean isUpdateRecordBySellerSuccess = false;
            Map<String, Object> mapRecord = new HashMap<>();
            mapRecord.put("orderId", orderId);
            mapRecord.put("money", money);
            mapRecord.put("userId", userId);
            mapRecord.put("paymentTime", paymentTime);
            mapRecord.put("amountAotalIntegral", amountAotalIntegral);
            if (PayToolUtil.isInteger(orderId)) {
                // 纯数字订单号
                // 用户，付款方
                isUpdateRecordSuccess = recordMapper.updateRecordByInt(mapRecord);
                // 商家，收款方
                isUpdateRecordBySellerSuccess = recordMapper.updateRecordBySellerInt(orderId, paymentTime);
                //获取非数字订单
                String record_payorder = orderServiceClient.queryUpayByOrderId(orderId);
                //修改拼团的支付状态
                Map<String,Object> pinTuanMap = new HashMap<>();
                pinTuanMap.put("orderId",record_payorder);
                activityServiceClient.updatePinTuanPayStart(pinTuanMap);
            } else {
                // 非纯数字订单号
                // 用户，付款方
                isUpdateRecordSuccess = recordMapper.updateRecord(mapRecord);
                // 商家，收款方
                isUpdateRecordBySellerSuccess = recordMapper.updateRecordBySeller(orderId, paymentTime);
                //修改拼团状态
                //修改拼团的支付状态
                Map<String,Object> pinTuanMap = new HashMap<>();
                pinTuanMap.put("orderId",orderId);
                activityServiceClient.updatePinTuanPayStart(pinTuanMap);
            }
            if (isUpdateRecordSuccess && isUpdateRecordBySellerSuccess) {
                LOGGER.info("-----UpdateTrade&RecordStatusSuccess-----");
                // 发送feign请求，更改payment状态
                Map<String, Object> paymentMap = new HashMap<String, Object>();
                // 支付方式
                Integer paymentId = 1;
                // 支付方式名称
                String paymentName = "在线支付";
                if (type==12) {
                    paymentId = 2;
                    paymentName = "线下支付";
                }
                // 分辨余额,购物卡,蜂币
                paymentMap.put("type", type);
                paymentMap.put("payOrderId", orderId);
                paymentMap.put("paymentId", paymentId);
                paymentMap.put("paymentName", paymentName);
                paymentMap.put("paymentTime", paymentTime);
                paymentMap.put("virtualGoods",virtualGoods);
                paymentMap.put("orderIsShopTake",orderIsShopTake);
                ResponseResult isUpdateOrderBase = orderServiceClient.UpdateOrderBaseByPayment(paymentMap);
                boolean is = new Boolean(String.valueOf(isUpdateOrderBase.getData())).booleanValue();
                if (is) {
                    LOGGER.info("-----UpdateOrderBaseByPaymentSuccess-----");
                } else {
                    LOGGER.info("-----UpdateOrderBaseByPaymentFail-----");
                }
            } else {
                LOGGER.info("-----UpdateTrade&RecordStatusFail-----");
            }
            LOGGER.info("EndUpdateTrade&RecordStatusTheFirstTime:" + DateUtils.getNowDateToDf());
        } catch (Exception e) {
            LOGGER.info("changeStatusErrorResults:" + e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        } finally {
            // 发送feign请求，更改payment状态
            Map<String, Object> paymentMap = new HashMap<String, Object>();
            // 支付方式
            Integer paymentId = 1;
            // 支付方式名称
            String paymentName = "在线支付";
            if (type==12) {
                paymentId = 2;
                paymentName = "线下支付";
            }
            // 分辨余额与购物卡
            paymentMap.put("type", type);
            paymentMap.put("payOrderId", orderId);
            paymentMap.put("paymentId", paymentId);
            paymentMap.put("paymentName", paymentName);
            paymentMap.put("paymentTime", paymentTime);
            paymentMap.put("virtualGoods",virtualGoods);
            paymentMap.put("orderIsShopTake",orderIsShopTake);
            orderServiceClient.UpdateOrderBaseByPayment(paymentMap);
        }
        return;
    }

    /**
     * 判断微信小程序支付的订单是否为虚拟订单或自提订单，并为虚拟订单生成兑换码。
     *
     * @param orderIdIsNum 包含订单号和是否为数字的Map对象。
     * @return 返回一个Map对象，其中包含虚拟商品标识和订单提取方式标识。
     */
    public Map<String,Integer> virtualWxExchangeCode(Map<String,String> orderIdIsNum){
        // 初始化变量，用于存储虚拟商品标识、订单号、兑换码URL、兑换码和订单是否为自提店提取方式
        Integer virtualGoods = 1;
        String  orderId = null;
        String redeemCodeUrl =null;
        String code= null;
        Integer orderIsShopTake = 2;
        // 初始化用于返回的结果Map，包含虚拟商品和订单提取方式信息
        Map<String, Integer> judgeOrderMap = new HashMap<>();
        try {
            // 调用服务判断订单是否为虚拟订单
            ResponseResult res = orderServiceClient.isVirtualOrder(orderIdIsNum);
            if (null != res.getData()) {
                // 从返回结果中提取订单相关信息
                Map<String,Object> map = (Map<String,Object>)res.getData();
                virtualGoods = (Integer) map.get("virtualGoods");
                orderId = (String)map.get("orderId");
                orderIsShopTake = (Integer) map.get("orderIsShopTake");
            }
            // 如果订单为虚拟订单或自提订单，则生成兑换码
            //虚拟订单 0 是 1 否
            if(null != virtualGoods &&  0 == virtualGoods || null != orderIsShopTake && 1 == orderIsShopTake){
                // 生成兑换码的唯一标识
                SimpleDateFormat df = new SimpleDateFormat("ddHHmmss");
                String timeStamp = df.format(new Date());
                String temp = String.valueOf(Math.round((Math.random()+1) * 1000));
                code = temp+timeStamp;

                // 生成兑换码的二维码图片并上传至OSS
                ResponseResult responseResult = QrCodeUtil.GenerateQRCode(code);
                redeemCodeUrl = String.valueOf(thirdPartyServiceClient.uploadToOssBase64((OssUploadUpdateParamDTO) responseResult.getData()).getData());
                // 构建包含兑换码信息的参数Map，用于添加兑换码到订单
                Map<String,Object> param = new HashMap<>();
                param.put("order",orderId);
                param.put("redeemCode",code);
                param.put("redeemCodeUrl",redeemCodeUrl);
                // 为订单添加兑换码
                //为虚拟拟订单添加兑换码
                orderServiceClient.addRedeemCode(param);
            }

            // 将虚拟商品和订单提取方式信息添加到返回的Map中
            judgeOrderMap.put("virtualGoods",virtualGoods);
            judgeOrderMap.put("orderIsShopTake",orderIsShopTake);
        } catch (Exception e) {
            // 异常处理，打印堆栈跟踪
            e.printStackTrace();
        }
        // 返回包含虚拟商品和订单提取方式信息的Map
        return judgeOrderMap;
    }

}
