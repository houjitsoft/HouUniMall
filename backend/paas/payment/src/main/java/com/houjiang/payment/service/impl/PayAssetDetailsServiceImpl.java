package com.houjiang.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.excel.ExcelsUtil;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.order.OrderBalanceRechargeVO;
import com.houjiang.commons.vo.order.OrderBaseVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.entity.PayAssetDetailsEntity;
import com.houjiang.payment.entity.PayConsumeWithdrawEntity;
import com.houjiang.payment.feign.OrderServiceClient;
import com.houjiang.payment.mapper.PayAssetDetailsMapper;
import com.houjiang.payment.mapper.PayConsumeWithdrawMapper;
import com.houjiang.payment.service.PayAssetDetailsService;
import com.houjiang.payment.vo.AssetInfoVO;
import com.houjiang.payment.vo.AssetPlatformQueryExpVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Amos
 */
@Service
public class PayAssetDetailsServiceImpl extends ServiceImpl<PayAssetDetailsMapper, PayAssetDetailsEntity>
    implements PayAssetDetailsService{

    @Autowired
    private PayConsumeWithdrawMapper payConsumeWithdrawMapper;
    @Autowired
    private OrderServiceClient orderServiceClient;

    final static Logger log = LoggerFactory.getLogger(PayAssetDetailsService.class);
    @Override
    public ResponseResult queryAssetList(Map<String, Object> map) {
        Integer returnFlag = 0;
        String token = (String) map.get("token");
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (isExistToken) {
            try {
                if (null != map.get("startTime") && null != map.get("finishTime")) {
                    if (!"".equals(map.get("startTime")) && map.get("startTime").equals(map.get("finishTime"))) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Calendar cal = Calendar.getInstance();
                        Date fdate = sdf.parse((String) map.get("finishTime"));
                        cal.setTime(fdate);
                        cal.add(Calendar.DATE, 1);
                        fdate = cal.getTime();
                        map.put("finishTime", sdf.format(fdate));

                    }
                }
                // 获取user信息
                UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
                if (ui == null) {
                    return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
                }
                String userId = ui.getUserId();
                map.put("userId", userId);
                //分页查询数据
                IPage<AssetInfoVO> page = this.baseMapper.queryAssetList(new Query<AssetInfoVO>().getPage(map), map);

                for (AssetInfoVO asset : page.getRecords()) {
                    Integer detailType = asset.getDetailType();
                    String orderId = asset.getOrderId();
                    //通过是否R开头以区分充值单号和订单号
                    if (null != orderId && !orderId.startsWith("R") && !orderId.startsWith("W") && !orderId.startsWith("T")
                            && !orderId.startsWith("P") && !orderId.startsWith("C") && !orderId.startsWith("YE")) {

                        Map<String, Object> orderInfoEntity = orderServiceClient.queryOrderInfo(orderId);
                        if(null == orderInfoEntity){
                            continue;
                        }
                        asset.setOrderStatus(Integer.valueOf(orderInfoEntity.get("orderStatus").toString()));
                        int virtualGoods = Integer.parseInt(orderInfoEntity.get("virtualGoods").toString());
                        int orderIsShopTake = Integer.parseInt(orderInfoEntity.get("orderIsShopTake").toString());
                        if (1 == orderIsShopTake) {
                            asset.setOrderType(3);
                        } else {
                            if (0 == virtualGoods) {
                                asset.setOrderType(2);
                                asset.setPastDue(Integer.valueOf(orderInfoEntity.get("pastDue").toString()));
                                asset.setIsCancel(Integer.valueOf(orderInfoEntity.get("isCancel").toString()));
                            } else {
                                asset.setOrderType(1);
                            }
                        }
                        int returnStatus = (int) orderInfoEntity.get("orderReturnStatus");
                        int refundStatus = (int) orderInfoEntity.get("orderRefundStatus");
                        asset.setReturnStatus(returnStatus);
                        asset.setRefundStatus(refundStatus);
                        if (CommonType.orderReturnStatus.RETURN_COMPLETED.getCode() == returnStatus || CommonType.orderRefundStatus.REFUND_COMPLETED.getCode() == refundStatus) {
                            BigDecimal returnAmount = new BigDecimal(orderInfoEntity.get("orderRefundAmount").toString());
                            BigDecimal orderAmount = new BigDecimal(orderInfoEntity.get("orderPaymentAmount").toString());
                            if (orderAmount.compareTo(returnAmount) > 0) {
                                returnFlag = 2;
                            } else if ((orderAmount.compareTo(returnAmount) == 0)) {
                                returnFlag = 1;
                            }
                        }
                        asset.setReturnFlag(returnFlag);
                    }
                    //若为余额提现，需要补充相关信息
                    if (CommonType.detailType.WITHDRAWAL_OF_BALANCE.getCode().equals(detailType)) {
                        PayConsumeWithdrawEntity bankInfoEntity = payConsumeWithdrawMapper.selectOne(new LambdaUpdateWrapper<PayConsumeWithdrawEntity>()
                                .eq(PayConsumeWithdrawEntity::getOrderid, orderId));
                        if (null != bankInfoEntity) {
                            asset.setAuditStatus(bankInfoEntity.getIsSucceed());
                            asset.setAuditComment(bankInfoEntity.getRemark());
                            asset.setReceiverName(bankInfoEntity.getCardname());
                            asset.setReceiverBankName(bankInfoEntity.getBank());
                            asset.setReceiverBankNo(bankInfoEntity.getCardno());
                            if (null != bankInfoEntity.getIsSucceed() && CommonType.isSucceed.NO_PASS.getCode().equals(bankInfoEntity.getIsSucceed())) {
                                returnFlag = 1;
                                asset.setReturnFlag(returnFlag);
                            }
                        }else{
                            asset.setAuditStatus(CommonType.isSucceed.PASS_THROUGH.getCode());
                        }
                    }
                    // 是余额充值
                    if (CommonType.detailType.BALANCE_RECHARGE.getCode().equals(detailType)) {
                        // 个人余额充值
                        if (null != orderId && orderId.startsWith("YE")) {
                            OrderBalanceRechargeVO orderBalanceRechargeVO = orderServiceClient.queryRechargeOrderById(orderId);
                            if(CommonType.balanceRechargeOrderStart.WAITING_PAY.getCode().equals(orderBalanceRechargeVO.getRechargeOrderStart())){
                                asset.setAuditStatusDesc(CommonType.balanceRechargeOrderStart.WAITING_PAY.getDesc());
                            }else if(CommonType.balanceRechargeOrderStart.WAITING_AUDIT.getCode().equals(orderBalanceRechargeVO.getRechargeOrderStart())){
                                asset.setAuditStatusDesc(CommonType.balanceRechargeOrderStart.WAITING_AUDIT.getDesc());
                            }else if(CommonType.balanceRechargeOrderStart.AUDIT_FAIL.getCode().equals(orderBalanceRechargeVO.getRechargeOrderStart())){
                                asset.setAuditStatusDesc(CommonType.balanceRechargeOrderStart.AUDIT_FAIL.getDesc());
                            }else{
                                asset.setAuditStatusDesc(CommonType.balanceRechargeOrderStart.FINISH.getDesc());
                            }
                        }else {
                            asset.setAuditStatusDesc(CommonType.balanceRechargeOrderStart.FINISH.getDesc());
                        }
                    }
                }
                return ResponseResult.success(PageUtils.getPage(page));
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseResult.fail(e.getMessage());
            }
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
    }

    @Override
    public ResponseResult queryAssetDetail(Map<Object, Object> map) {
        ResponseResult rr = new ResponseResult();
        //退款标识:0-默认；1-已退款；2-部分退款
        Integer returnFlag = 0;
        if (null == map.get("detailId") || "".equals(map.get("detailId"))) {
            return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER);
        }
        Integer detailId = Integer.valueOf(map.get("detailId").toString());

        try {
            AssetInfoVO assetInfoEntity = this.baseMapper.queryAssetDetail(detailId);
            //明细类型:0-默认;1-余额充值;2-订单退款;3-提现失败;4-余额提现;5-余额转账;6-购物消费;7-购物卡充值;8-余额扣款
            Integer detailType = assetInfoEntity.getDetailType();
            String orderId = assetInfoEntity.getOrderId();
            //如果存在订单号，查询退货退款状态以及是否全部退款
            if (null != orderId && !orderId.startsWith("R") && !orderId.startsWith("W")&& !orderId.startsWith("T")
                    && !orderId.startsWith("PLUS") && !orderId.startsWith("C") && !orderId.startsWith("ZZ") && !orderId.startsWith("YE")) {
                Map<String, Object> orderInfoEntity = orderServiceClient.queryOrderInfo(orderId);
                assetInfoEntity.setOrderStatus((Integer) orderInfoEntity.get("orderStatus"));
                assetInfoEntity.setPaymentNo((String) orderInfoEntity.get("paymentNo"));
                Integer virtualGoods = (Integer) orderInfoEntity.get("virtualGoods");
                Integer orderIsShopTake = (Integer) orderInfoEntity.get("orderIsShopTake");

                if (null != orderIsShopTake && 1 == orderIsShopTake) {
                    assetInfoEntity.setOrderType(3);
                } else {
                    if (null != virtualGoods && 0 == virtualGoods) {
                        assetInfoEntity.setOrderType(2);
                        assetInfoEntity.setPastDue((Integer) orderInfoEntity.get("pastDue"));
                        assetInfoEntity.setIsCancel(Integer.valueOf(String.valueOf(orderInfoEntity.get("isCancel"))));

                    } else {
                        assetInfoEntity.setOrderType(1);
                    }
                }
                int returnStatus = (int) orderInfoEntity.get("orderReturnStatus");
                int refundStatus = (int) orderInfoEntity.get("orderRefundStatus");

                assetInfoEntity.setReturnStatus(returnStatus);
                assetInfoEntity.setRefundStatus(refundStatus);
                if (2 == returnStatus || 2 == refundStatus) {
                    BigDecimal returnAmount = (BigDecimal) orderInfoEntity.get("orderRefundAmount");
                    BigDecimal orderAmount = (BigDecimal) orderInfoEntity.get("orderPaymentAmount");
                    if (orderAmount.compareTo(returnAmount) > 0) {
                        returnFlag = 2;
                    } else if ((orderAmount.compareTo(returnAmount) == 0)) {
                        returnFlag = 1;
                    }
                }
                assetInfoEntity.setReturnFlag(returnFlag);
            } else {

                assetInfoEntity.setPaymentNo(assetInfoEntity.getOrderId());
                assetInfoEntity.setOrderId(null);
            }
            //若为余额提现，需要补充相关信息
            if (CommonType.detailType.WITHDRAWAL_OF_BALANCE.getCode().equals(detailType)) {
                PayConsumeWithdrawEntity bankInfoEntity = payConsumeWithdrawMapper.selectOne(new LambdaUpdateWrapper<PayConsumeWithdrawEntity>()
                        .eq(PayConsumeWithdrawEntity::getOrderid, orderId));
                if (null != bankInfoEntity) {
                    assetInfoEntity.setAuditStatus(bankInfoEntity.getIsSucceed());
                    assetInfoEntity.setAuditComment(bankInfoEntity.getRemark());
                    assetInfoEntity.setReceiverName(bankInfoEntity.getCardname());
                    assetInfoEntity.setReceiverBankName(bankInfoEntity.getBank());
                    assetInfoEntity.setReceiverBankNo(bankInfoEntity.getCardno());

                    if (null != bankInfoEntity.getIsSucceed() && CommonType.isSucceed.NO_PASS.getCode().equals(bankInfoEntity.getIsSucceed())) {
                        returnFlag = 1;
                        assetInfoEntity.setReturnFlag(returnFlag);
                    }
                }else{
                    assetInfoEntity.setAuditStatus(CommonType.isSucceed.PASS_THROUGH.getCode());
                }
            }
            // 是余额充值
            if (CommonType.detailType.BALANCE_RECHARGE.getCode().equals(detailType)) {
                // 个人余额充值
                if (null != orderId && orderId.startsWith("YE")) {
                    OrderBalanceRechargeVO orderBalanceRechargeVO = orderServiceClient.queryRechargeOrderById(orderId);
                    if(CommonType.balanceRechargeOrderStart.WAITING_PAY.getCode().equals(orderBalanceRechargeVO.getRechargeOrderStart())){
                        assetInfoEntity.setAuditStatusDesc(CommonType.balanceRechargeOrderStart.WAITING_PAY.getDesc());
                    }else if(CommonType.balanceRechargeOrderStart.WAITING_AUDIT.getCode().equals(orderBalanceRechargeVO.getRechargeOrderStart())){
                        assetInfoEntity.setAuditStatusDesc(CommonType.balanceRechargeOrderStart.WAITING_AUDIT.getDesc());
                    }else if(CommonType.balanceRechargeOrderStart.AUDIT_FAIL.getCode().equals(orderBalanceRechargeVO.getRechargeOrderStart())){
                        assetInfoEntity.setAuditStatusDesc(CommonType.balanceRechargeOrderStart.AUDIT_FAIL.getDesc());
                    }else{
                        assetInfoEntity.setAuditStatusDesc(CommonType.balanceRechargeOrderStart.FINISH.getDesc());
                    }
                }else {
                    assetInfoEntity.setAuditStatusDesc(CommonType.balanceRechargeOrderStart.FINISH.getDesc());
                }
            }
            return ResponseResult.success(assetInfoEntity);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail(e.getMessage());
        }
    }

    @Override
    public ResponseResult queryPlatformAssetList(Map<String, Object> map) {

        Integer returnFlag = 0;
        try {
            IPage<PayAssetDetailsEntity> dataList =this.baseMapper.selectPage(new Query<PayAssetDetailsEntity>().getPage(map),
                    new LambdaQueryWrapper<PayAssetDetailsEntity>()
                       .eq(map.get("assetType")!=null, PayAssetDetailsEntity::getAssetType, map.get("assetType"))
                       .eq(map.get("payType")!=null, PayAssetDetailsEntity::getPayType, map.get("payType"))
                       .eq(map.get("userId")!=null && !"".equals(map.get("userId")), PayAssetDetailsEntity::getUserId, map.get("userId"))
                       .ge(map.get("startTime") != null && !"".equals(map.get("startTime")), PayAssetDetailsEntity::getCreateTime, map.get("startTime"))
                       .le(map.get("finishTime") != null && !"".equals(map.get("finishTime")), PayAssetDetailsEntity::getCreateTime, map.get("finishTime"))
                       .orderByDesc(PayAssetDetailsEntity::getCreateTime));

            for(PayAssetDetailsEntity asset:dataList.getRecords()) {
                String orderId = asset.getOrderId();
                Integer detailType = asset.getDetailType();
                //通过是否R开头以区分充值单号和订单号
                //若为购物消费，需要补充虚拟订单、事物订单和门店自提订单状态
                if (null != orderId && !orderId.startsWith("R") && !orderId.startsWith("W")&& !orderId.startsWith("T") && !orderId.startsWith("PLU") && !orderId.startsWith("ZZ")) {
                    ResponseResult orderInfoResult = orderServiceClient.selectOrderInfoById(orderId);
                    OrderBaseVO orderInfoEntity= JSONObject.parseObject(JSON.toJSONString(orderInfoResult.getData()),OrderBaseVO.class);
                    asset.setOrderStatus(orderInfoEntity.getOrderStatus());
                    Integer virtualGoods = orderInfoEntity.getVirtualGoods();
                    Integer orderIsShopTake = orderInfoEntity.getOrderIsShopTake();
                    if(null != orderIsShopTake && 1 == orderIsShopTake){
                        asset.setOrderType(3);
                    }else{
                        if(null != virtualGoods && 0 == virtualGoods){
                            asset.setOrderType(2);
                            asset.setPastDue(orderInfoEntity.getPastDue());
                            asset.setIsCancel(orderInfoEntity.getIsCancel());
                        }else{
                            asset.setOrderType(1);
                        }
                    }

                    int returnStatus = Integer.valueOf(orderInfoEntity.getOrderReturnStatus());
                    int refundStatus = Integer.valueOf(orderInfoEntity.getOrderRefundStatus());
                    asset.setReturnStatus(returnStatus);
                    asset.setRefundStatus(refundStatus);
                    if (2 == returnStatus || 2 == refundStatus) {
                        BigDecimal returnAmount = BigDecimal.valueOf(orderInfoEntity.getOrderRefundAmount());
                        BigDecimal orderAmount = orderInfoEntity.getOrderPaymentAmount();
                        if (orderAmount.compareTo(returnAmount) > 0) {
                            returnFlag = 2;
                        } else if ((orderAmount.compareTo(returnAmount) == 0)) {
                            returnFlag = 1;
                        }
                    }
                    asset.setReturnFlag(returnFlag);
                }

                //若为余额提现，需要补充相关信息
                if(null != detailType && 4 == detailType){
                    PayConsumeWithdrawEntity bankInfoEntity = payConsumeWithdrawMapper.selectOne(new LambdaUpdateWrapper<PayConsumeWithdrawEntity>()
                            .eq(PayConsumeWithdrawEntity::getOrderid, orderId));
                    if(null != bankInfoEntity){
                        asset.setAuditStatus(bankInfoEntity.getIsSucceed());
                        if(null != bankInfoEntity.getIsSucceed() && 4 == bankInfoEntity.getIsSucceed()){
                            returnFlag = 1;
                            asset.setReturnFlag(returnFlag);
                        }
                    }


                }

            }
            return ResponseResult.success(PageUtils.getPage(dataList));

        } catch (Exception e) {
            log.info("queryList：" + e.getMessage());
            return ResponseResult.fail(e.getMessage());
        }
    }

    @Override
    public ResponseResult exportPlatformAssetList(Map<Object, Object> map) {

        List<AssetPlatformQueryExpVO> dataList = new ArrayList<>();
        dataList = this.baseMapper.queryExpPlatformAssetList(map);
        String[] headers = {"序号","会员昵称","联系方式","收支类型","收支金额","交易对象/账户","订单号/充值单号","创建时间"};
        List<Map<String, String>> list = Lists.newArrayList();
        int n = 0;
        for (AssetPlatformQueryExpVO res : dataList) {
            n++;

            if(n > 10000){
                //导出数量过大，请联系运维
                return ResponseResult.fail(BizCodeEnume.EXPORT_QUANTITY_IS_TOO_LARGE);
            }
            HashMap<String, String> dataMap = com.google.common.collect.Maps.newHashMap();
            //收支类型:0-默认;1-余额充值;2-订单退款;3-提现失败;4-余额提现;5-余额转账;6-购物消费;7-购物卡充值;8-余额扣款
            if (res.getDetailType() != null) {
                switch (res.getDetailType() + "") {
                    case "1":
                        res.setDetailType("余额充值");
                        break;
                    case "2":
                        res.setDetailType("订单退款");
                        break;
                    case "3":
                        res.setDetailType("提现失败");
                        break;
                    case "4":
                        res.setDetailType("余额提现");
                        break;
                    case "5":
                        res.setDetailType("余额转账");
                        break;
                    case "6":
                        res.setDetailType("购物消费");
                        break;
                    case "7":
                        res.setDetailType("购物卡充值");
                        break;
                    case "8":
                        res.setDetailType("余额扣款");
                        break;
                    default:
                        res.setDetailType("默认");
                        break;
                }

            }

            dataMap.put("序号", n+"");
            dataMap.put("会员昵称", res.getUserNickname());
            dataMap.put("联系方式", res.getUserMobile());
            dataMap.put("收支类型", res.getDetailType());
            dataMap.put("收支金额", res.getAssetAmount()+"");
            dataMap.put("交易对象/账户", res.getAssetDirection());
            dataMap.put("订单号/充值单号", res.getOrderId());
            dataMap.put("创建时间", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(res.getCreateTime()));

            list.add(dataMap);
        }

        // excel文件名
        Map<String, List<Map<String, String>>> tmpMap = Maps.newHashMap();
        tmpMap.put("资产明细", list);
        String url = ExcelsUtil.createExcel(headers, tmpMap, new int[] { 0 });

        return ResponseResult.success(url);
    }

    @Override
    public void insertDetail(Map<Object, Object> map) {
        this.baseMapper.insertDetail(map);
    }
}




