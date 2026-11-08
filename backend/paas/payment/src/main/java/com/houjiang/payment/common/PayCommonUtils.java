package com.houjiang.payment.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.base.MemberMessageListIntegralDTO;
import com.houjiang.commons.dto.order.WapGoodsSaleSumDTO;
import com.houjiang.commons.dto.payment.AssetTmpInfoDTO;
import com.houjiang.commons.dto.yiyatong.GoodsDTO;
import com.houjiang.commons.dto.yiyatong.RegionByCodeOpenDTO;
import com.houjiang.commons.dto.yiyatong.SubmitOrderDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.*;
import com.houjiang.commons.vo.order.OrderBaseVO;
import com.houjiang.commons.vo.order.PointsPayReqVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.commons.vo.yiyatong.base.RegionVO;
import com.houjiang.commons.vo.yiyatong.base.SubmitOrderVO;
import com.houjiang.payment.entity.PayConsumeRecordEntity;
import com.houjiang.payment.entity.PayUserBaseEntity;
import com.houjiang.payment.entity.PayUserResourceEntity;
import com.houjiang.payment.feign.*;
import com.houjiang.payment.mapper.PayAssetDetailsMapper;
import com.houjiang.payment.mapper.PayConsumeRecordMapper;
import com.houjiang.payment.mapper.PayUserBaseMapper;
import com.houjiang.payment.mapper.PayUserResourceMapper;
import com.houjiang.payment.service.PayUserResourceService;
import com.houjiang.payment.service.impl.ALiPayServiceImpl;
import com.houjiang.payment.vo.CallBackVO;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author Amos
 * @date 2023/4/27 10:41
 */
@Component
public class PayCommonUtils {

    @Resource
    private PayUserBaseMapper payUserBaseMapper;

    @Autowired
    private UsermsServiceClient usermsServiceClient;

    @Autowired
    private PayUserResourceService payUserResourceService;

    @Autowired
    private ALiPayServiceImpl aLiPayService;

    @Resource
    private PayUserResourceMapper payUserResourceMapper;

    @Autowired
    private OrderServiceClient orderServiceClient;

    @Autowired
    private ActivityServiceClient activityServiceClient;

    @Resource
    private PayConsumeRecordMapper payConsumeRecordMapper;

    @Resource
    private PayAssetDetailsMapper payAssetDetailsMapper;

    @Autowired
    private MerchantServiceClient merchantServiceClient;

    @Autowired
    private GoodServiceClient goodServiceClient;

    final Logger log = LoggerFactory.getLogger(PayCommonUtils.class);

    /**
     * 验证支付密码
     *
     * @param passwd
     * @param userInfo
     * @return
     */
    public ResponseResult getPwdExist(String passwd, UserInfoVO userInfo) {
        Integer isExist = 0;
        PayUserBaseEntity entity = payUserBaseMapper.selectById(userInfo.getUserId());
        // 验证支付密码
        String pwdExist = entity.getUserPayPasswd();
        if ("".equals(pwdExist)) {
            //未设置密码，请前往密码设置！
            return ResponseResult.fail(BizCodeEnume.PASSWORD_NOT_SET);
        } else if (passwd == null || "".equals(passwd) || !PasswordUtils.BCryptPasswordMatches(passwd, pwdExist)) {
            if (null != userInfo.getIsEmployee() && userInfo.getIsEmployee() == 1) {
                //密码错误，可联系企业管理员修改。
                return ResponseResult.fail(BizCodeEnume.PASSWORD_ERROR_ONE);
            } else {
                //密码错误，可以选择密码重置！
                return ResponseResult.fail(BizCodeEnume.PASSWORD_ERROR_TWO);
            }
        } else {
            isExist = 1;
            return ResponseResult.success(isExist);
        }
    }


    /**
     * 用户积分相关操作
     *
     * @param integralGoods
     * @param userId
     * @param amountAotalIntegral
     * @param orderId
     * @return
     */
    public ResponseResult getUserIntegral(Integer integralGoods, String userId, Integer amountAotalIntegral, String orderId) {
        if (null != integralGoods && integralGoods == 0) {
            //获取用户积分
            Integer userIntegral = usermsServiceClient.getMemberIntegralByUserId(userId);
            Integer frozenIntegra = usermsServiceClient.getMemberFrozenIntegraIntegralByUserId(userId);
            userIntegral = userIntegral - frozenIntegra;
            if (userIntegral < 0) {
                userIntegral = 0;
            }
            if (null != userIntegral && amountAotalIntegral <= userIntegral) {
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
                //添加积分修改记录
                Map<String, Object> pointParam = new HashMap<>();
                pointParam.put("PointsLogType", CommonType.pointsLogType.CONSUME.getCode());
                pointParam.put("ClassId", CommonType.pointType.POINTS_EXCHANGE_VOUCHER.getCode());
                pointParam.put("PointsLogTime", LocalDateTime.now());
                pointParam.put("PointsLogDesc", CommonType.pointType.POINTS_EXCHANGE_GOODS.getDesc());
                pointParam.put("PointsLogFlag", "reg");
                pointParam.put("UserId", userId);
                pointParam.put("PointsLogPoints", amountAotalIntegral);
                pointParam.put("PointsLogOperate", CommonType.pointType.POINTS_EXCHANGE_GOODS.getDesc());
                if (null != orderId) {
                    pointParam.put("orderId", orderId);
                }
                usermsServiceClient.insertPointLogByMap(pointParam);
                return ResponseResult.success();
            } else {
                //账户积分不足
                return ResponseResult.fail(BizCodeEnume.PAY_INTEGRAL_ERROR);
            }
        }
        return ResponseResult.success();
    }

    /**
     * 支付
     *
     * @param updateMoney
     * @param token
     * @param userId
     * @param orderId
     * @param type
     * @param virtualGoods
     * @param orderIsShopTake
     * @param amountActuallyPaids
     * @param amountAotalIntegral
     * @param orderIdIsNum
     * @param callBackParaList
     * @param integralGoods
     * @param assetDetailMap
     * @param dataList
     * @return
     */
    public ResponseResult getPay(boolean updateMoney, String token, String userId, String orderId, String type,
                                 Integer virtualGoods, Integer orderIsShopTake, BigDecimal amountActuallyPaids,
                                 Integer amountAotalIntegral, Map<String, String> orderIdIsNum, List<CallBackVO> callBackParaList,
                                 Integer integralGoods, Map<Object, Object> assetDetailMap, List<AssetTmpInfoDTO> dataList) {
        if (updateMoney) {
            PayUserResourceEntity payUserResourceEntity = payUserResourceService.getById(userId);
            //删除支付成功后积分订单的所需积分
            RedisUtil.remove(HouConfig.HOU_USERINTEGRALYPAID + "--" + token + "--" + userId);
            RedisUtil.set(HouConfig.DETERMINEWHETHERTOPAY + "-" + orderId + "-" + userId + "-" + type,
                    orderId, 86400);
            // 查询账户购物卡余额
            BigDecimal afterUserGiftCardMoney = new BigDecimal(0);
            if ("1".equals(type)) {
                afterUserGiftCardMoney = payUserResourceEntity.getUserMoney();
            } else if ("2".equals(type)) {
                afterUserGiftCardMoney = payUserResourceEntity.getUserRechargeCard();
            }

            //判断是否是虚拟订单
            Map<String, Integer> judgeOrderMap = aLiPayService.virtualWxExchangeCode(orderIdIsNum);
            virtualGoods = judgeOrderMap.get("virtualGoods");
            //存放值
            orderIsShopTake = judgeOrderMap.get("orderIsShopTake");
            Map<Object, Object> statusMap = new HashMap<Object, Object>();
            // 是纯数字订单号，存放纯数字订单
            statusMap.put("afterUserMoney", afterUserGiftCardMoney);
            statusMap.put("orderId", orderId);
            statusMap.put("token", token);
            statusMap.put("type", type);
            statusMap.put("paymentAmount", amountActuallyPaids);
            statusMap.put("amountAotalIntegral", amountAotalIntegral);
            //虚拟订单 0 是 1 否
            if (null != virtualGoods && 0 == virtualGoods) {
                statusMap.put("virtualGoods", virtualGoods);
            }
            //门店订单 0 是 1 否
            if (null != orderIsShopTake && 1 == orderIsShopTake) {
                statusMap.put("orderIsShopTake", orderIsShopTake);
                statusMap.put("isDelivery", judgeOrderMap.get("isDelivery"));
            }

            // 更改销量
            List<WapGoodsSaleSumDTO> wapGood = orderServiceClient.queryGoodsByOrderId(orderId);
            goodServiceClient.updateGoodsSalenum(wapGood);
            // 更新分销小店销量
            merchantServiceClient.updateVkShopSaleNum(orderId);
            // 更改订单状态
            changeStatus(statusMap);
            // 设置微客
            setVKInfo(orderId);
            if ("1".equals(type) || "2".equals(type) || "12".equals(type)) {
                if (null != integralGoods && integralGoods == 0) {
                    if (amountActuallyPaids.compareTo(new BigDecimal(0)) > 0) {
                        assetDetailMap.put("assetDirection", "积分订单");
                        assetDetailMap.put("payType", Integer.valueOf(type));
                        assetDetailMap.put("userId", userId);
                        assetDetailMap.put("detailType", 6);
                        assetDetailMap.put("assetType", 2);
                        assetDetailMap.put("assetAmount", amountActuallyPaids);
                        assetDetailMap.put("orderId", dataList.get(0).getOrderId());
                        payAssetDetailsMapper.insertDetail(assetDetailMap);
                    }
                } else {

                    for (AssetTmpInfoDTO assetInfoEntity : dataList) {
                        assetDetailMap.put("assetDirection", assetInfoEntity.getShopName());
                        assetDetailMap.put("payType", Integer.valueOf(type));
                        assetDetailMap.put("userId", userId);
                        assetDetailMap.put("detailType", 6);
                        assetDetailMap.put("assetType", 2);
                        assetDetailMap.put("assetAmount", assetInfoEntity.getOrderPaymentAmount());
                        assetDetailMap.put("orderId", assetInfoEntity.getOrderId());
                        payAssetDetailsMapper.insertDetail(assetDetailMap);
                    }
                }
            }
            //
            List<Map<String, Object>> orderMap = payUserBaseMapper.selectOrderMap(orderId);
            for (Map<String, Object> map : orderMap) {
                String orderIdStr = map.get("orderId").toString();
                String orderSource = map.get("orderSource").toString();
                String buyerUserName = map.get("buyerUserName").toString();
                String orderReceiverName = map.get("orderReceiverName").toString();
                String orderReceiverAddress = map.get("orderReceiverAddress").toString();
                String orderReceiverContact = map.get("orderReceiverContact").toString();
                // 是怡亚通的订单
                if (orderSource.equals(CommonType.SupplierSource.YI_YA_TONG.getCode().toString())) {
                    String districtCode = map.get("provinceCode").toString();
                    String cityCode = map.get("cityCode").toString();
                    String areaCode = map.get("areaCode").toString();
                    //怡亚通代发订单
                    SubmitOrderDTO dto = new SubmitOrderDTO();
                    dto.setOrderSource("1");
                    dto.setBuyerMemo(buyerUserName);
                    dto.setShipName(orderReceiverName);
                    dto.setShipAddress(orderReceiverAddress);
                    dto.setShipMobile(orderReceiverContact);

                    List<String> provinceCodeList = Arrays.asList("500000", "810000", "820000");
                    //重庆地址单独做处理,查二级
                    if (provinceCodeList.contains(districtCode) && !StringUtils.isEmpty(areaCode)) {
                        RegionByCodeOpenDTO regionByCodeOpenDTO = new RegionByCodeOpenDTO();
                        regionByCodeOpenDTO.setRegionCode(areaCode);
                        ResponseResult codeResult = goodServiceClient.getRegionByCodeOpen(regionByCodeOpenDTO);
                        List<RegionVO> list = JSONObject.parseArray(JSON.toJSONString(codeResult.getData()), RegionVO.class);
                        if (!list.isEmpty()) {
                            for (RegionVO regionVO : list) {
                                //获取区级 "regionFullCodes": "50024301,500243,500200,500000",
                                String regionFullCodes = regionVO.getRegionFullCodes();
                                String[] parts = regionFullCodes.split(",");
                                System.out.println("parts" + Arrays.toString(parts));
                                // 检查数组长度是否足够
                                if (parts.length > 2) {
                                    if (parts[1].equals(areaCode)) {
                                        cityCode = parts[2];
                                        break;
                                    }
                                } else {
                                    if (parts[0].equals(areaCode)) {
                                        cityCode = parts[0];
                                        areaCode = "";
                                        break;
                                    }
                                }
                            }
                            System.out.println("cityCode    " + cityCode + "   areaCode    " + areaCode);
                        }
                    }
                    String shipAreaCode = districtCode + "," + cityCode;
                    if (!StringUtils.isEmpty(areaCode)) {
                        shipAreaCode = shipAreaCode + "," + areaCode;
                    }
                    dto.setShipAreaCode(shipAreaCode);

                    List<GoodsDTO> goodsList = payUserBaseMapper.selectOrderGoodsList(orderIdStr);
                    dto.setGoodsList(goodsList);

                    ResponseResult result = goodServiceClient.submitOrder(dto);
                    log.info("--怡亚通创建订单---返回数据---" + JSON.toJSONString(result.getData()));
                    if (result.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
                        return result;
                    }
                    List<SubmitOrderVO> list = JSON.parseArray(JSON.toJSONString(result.getData()), SubmitOrderVO.class);
                    if (!list.isEmpty()) {
                        for (SubmitOrderVO submitOrderListVO : list) {
                            //保存怡亚通订单信息
                            submitOrderListVO.setOrderId(orderIdStr);
                            orderServiceClient.insertYiYaTongOrder(submitOrderListVO);
                        }
                    }
                } else if (orderSource.equals(CommonType.SupplierSource.JING_DONG.getCode().toString())) {
                    // 是京东的订单
                    goodServiceClient.sendOrderToJingDong(orderIdStr);
                }
            }
            //支付成功
            return ResponseResult.success(BizCodeEnume.PAYMENT_SUCCESSFUL.getMsg());
        } else {
            //支付失败
            return ResponseResult.fail(BizCodeEnume.PAYMENT_FAILED);
        }
    }

    /**
     * 支付
     *
     * @param userId
     * @param orderId
     * @param type
     * @param amountActuallyPaids
     * @param assetDetailMap
     * @return
     */
    public ResponseResult pointsPay(String token,
                                    BigDecimal afterUserGiftCardMoney,
                                    String paymentName,
                                    String userId,
                                    String orderId,
                                    Integer type,
                                    BigDecimal amountActuallyPaids,
                                    Map<Object, Object> assetDetailMap) {

        // 更改销量
        List<WapGoodsSaleSumDTO> wapGood = orderServiceClient.queryGoodsByOrderId(orderId);
        goodServiceClient.updateGoodsSalenum(wapGood);
        // 更新分销小店销量
        merchantServiceClient.updateVkShopSaleNum(orderId);
        // 更新用户，付款方
        payConsumeRecordMapper.update(null, new LambdaUpdateWrapper<PayConsumeRecordEntity>()
                .set(PayConsumeRecordEntity::getRecordStatus, 3)
                .set(PayConsumeRecordEntity::getRecordAfterMoney, afterUserGiftCardMoney)
                .set(PayConsumeRecordEntity::getUserId, userId)
                .set(PayConsumeRecordEntity::getPlusOrMinus, 0)
                .set(PayConsumeRecordEntity::getRecordPaytime, new Date())
                .set(PayConsumeRecordEntity::getTotalIntegral, amountActuallyPaids)
                .eq(PayConsumeRecordEntity::getOrderId, orderId));
        // 商家，收款方
        payConsumeRecordMapper.update(null, new LambdaUpdateWrapper<PayConsumeRecordEntity>()
                .set(PayConsumeRecordEntity::getRecordStatus, 3)
                .set(PayConsumeRecordEntity::getPlusOrMinus, 1)
                .set(PayConsumeRecordEntity::getRecordPaytime, new Date())
                .eq(PayConsumeRecordEntity::getOrderId, orderId));

        // 发送feign请求，更改payment状态

        PointsPayReqVO vo = new PointsPayReqVO();
        vo.setOrderId(orderId);
        vo.setPayCode(type);
        vo.setPaymentName(paymentName);
        vo.setOrderStatus(3);
        orderServiceClient.updateOrderStatus(vo);

        if (amountActuallyPaids.compareTo(new BigDecimal(0)) > 0) {
            assetDetailMap.put("assetDirection", "积分订单");
            assetDetailMap.put("payType", Integer.valueOf(type));
            assetDetailMap.put("userId", userId);
            assetDetailMap.put("detailType", 6);
            assetDetailMap.put("assetType", 2);
            assetDetailMap.put("assetAmount", amountActuallyPaids);
            assetDetailMap.put("orderId", orderId);
            payAssetDetailsMapper.insertDetail(assetDetailMap);
        }

        //
        List<Map<String, Object>> orderMap = payUserBaseMapper.selectOrderMap(orderId);
        for (Map<String, Object> map : orderMap) {
            String orderIdStr = map.get("orderId").toString();
            String orderSource = map.get("orderSource").toString();
            String buyerUserName = map.get("buyerUserName").toString();
            String orderReceiverName = map.get("orderReceiverName").toString();
            String orderReceiverAddress = map.get("orderReceiverAddress").toString();
            String orderReceiverContact = map.get("orderReceiverContact").toString();
            // 是怡亚通的订单
            if (orderSource.equals(CommonType.SupplierSource.YI_YA_TONG.getCode().toString())) {
                String districtCode = map.get("provinceCode").toString();
                String cityCode = map.get("cityCode").toString();
                String areaCode = map.get("areaCode").toString();
                //怡亚通代发订单
                SubmitOrderDTO dto = new SubmitOrderDTO();
                dto.setOrderSource("1");
                dto.setBuyerMemo(buyerUserName);
                dto.setShipName(orderReceiverName);
                dto.setShipAddress(orderReceiverAddress);
                dto.setShipMobile(orderReceiverContact);

                List<String> provinceCodeList = Arrays.asList("500000", "810000", "820000");
                //重庆地址单独做处理,查二级
                if (provinceCodeList.contains(districtCode) && !StringUtils.isEmpty(areaCode)) {
                    RegionByCodeOpenDTO regionByCodeOpenDTO = new RegionByCodeOpenDTO();
                    regionByCodeOpenDTO.setRegionCode(areaCode);
                    ResponseResult codeResult = goodServiceClient.getRegionByCodeOpen(regionByCodeOpenDTO);
                    List<RegionVO> list = JSONObject.parseArray(JSON.toJSONString(codeResult.getData()), RegionVO.class);
                    if (!list.isEmpty()) {
                        for (RegionVO regionVO : list) {
                            //获取区级 "regionFullCodes": "50024301,500243,500200,500000",
                            String regionFullCodes = regionVO.getRegionFullCodes();
                            String[] parts = regionFullCodes.split(",");
                            log.info("嘉币、劲豆parts" + Arrays.toString(parts));
                            // 检查数组长度是否足够
                            if (parts.length > 2) {
                                if (parts[1].equals(areaCode)) {
                                    cityCode = parts[2];
                                    break;
                                }
                            } else {
                                if (parts[0].equals(areaCode)) {
                                    cityCode = parts[0];
                                    areaCode = "";
                                    break;
                                }
                            }
                        }
                        log.info("嘉币、劲豆cityCode    " + cityCode + "   areaCode    " + areaCode);
                    }
                }
                String shipAreaCode = districtCode + "," + cityCode;
                if (!StringUtils.isEmpty(areaCode)) {
                    shipAreaCode = shipAreaCode + "," + areaCode;
                }
                dto.setShipAreaCode(shipAreaCode);

                List<GoodsDTO> goodsList = payUserBaseMapper.selectOrderGoodsList(orderIdStr);
                dto.setGoodsList(goodsList);

                ResponseResult result = goodServiceClient.submitOrder(dto);
                log.info("--怡亚通创建订单---返回数据---" + JSON.toJSONString(result.getData()));
                if (result.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
                    return result;
                }
                List<SubmitOrderVO> list = JSON.parseArray(JSON.toJSONString(result.getData()), SubmitOrderVO.class);
                if (!list.isEmpty()) {
                    for (SubmitOrderVO submitOrderListVO : list) {
                        //保存怡亚通订单信息
                        submitOrderListVO.setOrderId(orderIdStr);
                        orderServiceClient.insertYiYaTongOrder(submitOrderListVO);
                    }
                }
            } else if (orderSource.equals(CommonType.SupplierSource.JING_DONG.getCode().toString())) {
                // 是京东的订单
                goodServiceClient.sendOrderToJingDong(orderIdStr);
            }
        }
        //支付成功
        return ResponseResult.success(BizCodeEnume.PAYMENT_SUCCESSFUL.getMsg());
    }

    public void setVKInfo(String orderId) {
        usermsServiceClient.setVKInfo(orderId);
    }

    /**
     * 更改状态
     *
     * @param map
     */
    public void changeStatus(Map<Object, Object> map) {
        String orderId = (String) map.get("orderId");
        String type = map.get("type").toString();
        Integer virtualGoods = (Integer) map.get("virtualGoods");
        Integer orderIsShopTake = (Integer) map.get("orderIsShopTake");
        // 是否同城配送2-否 1-是
        Integer isDelivery = (Integer) map.get("isDelivery");
        Integer amountAotalIntegral = (Integer) map.get("amountAotalIntegral");
        // 支付时间
        String paymentTime = DateUtils.getNowDateToString();
        BigDecimal money = (BigDecimal) map.get("afterUserMoney");
        // 存放金额
        if (null == money) {
            money = new BigDecimal(0);
        }
        // 获取user信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        String userId = ui.getUserId();
        String userName = ui.getUserName();
        String buyerUserId = orderServiceClient.queryBuyerUserIdByOrderBase(orderId);
        try {
            // 发送feign,提交下单通知
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("userId", userId);
            messageMap.put("userName", userName);
            // 支付号
            messageMap.put("orderId", orderId);
            messageMap.put("paymentTime", paymentTime);
            // 发送下单通知
            boolean isUpdateOrderToInform = orderServiceClient.orderToInform(messageMap).getCode() == CommonStatusEnum.SUCCESS.getCode();
            if (isUpdateOrderToInform) {
                log.info("-----OrderToInformUpdateSuccess-----");
            } else {
                log.info("-----OrderToInformUpdateFail-----");
            }

            // 更新trade&record状态
            log.info("StartUpdateTrade&RecordStatusTheFirstTime:" + DateUtils.getNowDate());
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
                isUpdateRecordSuccess = payConsumeRecordMapper.updateRecordByInt(mapRecord);
                // 商家，收款方
                isUpdateRecordBySellerSuccess = payConsumeRecordMapper.updateRecordBySellerInt(orderId, paymentTime);
                //获取非数字订单
                String recordPayorder = orderServiceClient.queryAssetInfo(orderId, "").getFirst().getPaymentNumber();
                //修改拼团的支付状态
                Map<String, Object> pinTuanMap = new HashMap<>();
                pinTuanMap.put("orderId", recordPayorder);
                // 拼团状态更新属增强逻辑：未部署 activity 服务时不能连累正常订单支付
                try {
                    activityServiceClient.updatePinTuanPayStart(pinTuanMap);
                } catch (Exception ex) {
                    log.warn("更新拼团支付状态失败（activity 服务不可用），忽略: {}", ex.getMessage());
                }
            } else {
                // 非纯数字订单号
                // 用户，付款方
                isUpdateRecordSuccess = payConsumeRecordMapper.updateRecord(mapRecord);
                // 商家，收款方
                isUpdateRecordBySellerSuccess = payConsumeRecordMapper.updateRecordBySeller(orderId, paymentTime);
                //修改拼团状态
                //修改拼团的支付状态
                Map<String, Object> pinTuanMap = new HashMap<>();
                pinTuanMap.put("orderId", orderId);
                // 拼团状态更新属增强逻辑：未部署 activity 服务时不能连累正常订单支付
                try {
                    activityServiceClient.updatePinTuanPayStart(pinTuanMap);
                } catch (Exception ex) {
                    log.warn("更新拼团支付状态失败（activity 服务不可用），忽略: {}", ex.getMessage());
                }
            }
            if (isUpdateRecordSuccess && isUpdateRecordBySellerSuccess) {
                log.info("-----UpdateTrade&RecordStatusSuccess-----");
                // 发送feign请求，更改payment状态
                Map<String, Object> paymentMap = new HashMap<>();
                // 支付方式
                Integer paymentId = 1;
                // 支付方式名称
                String paymentName = "在线支付";
                if ("12".equals(type)) {
                    paymentId = 2;
                    paymentName = "线下支付";
                } else if ("1".equals(type) && !buyerUserId.equals(ui.getUserId())) {
                    paymentId = 14;
                    paymentName = "余额支付";
                } else if ("2".equals(type) && !buyerUserId.equals(ui.getUserId())) {
                    paymentId = 15;
                    paymentName = "购物卡支付";
                }
                // 分辨余额,购物卡,蜂币
                paymentMap.put("type", type);
                paymentMap.put("payOrderId", orderId);
                paymentMap.put("paymentId", paymentId);
                paymentMap.put("paymentName", paymentName);
                paymentMap.put("paymentTime", paymentTime);
                paymentMap.put("virtualGoods", virtualGoods);
                paymentMap.put("orderIsShopTake", orderIsShopTake);
                paymentMap.put("isDelivery", isDelivery);
                orderServiceClient.UpdateOrderBaseByPayment(paymentMap);
            }
        } catch (Exception e) {
            // 这里以前直接 currentTransactionStatus()，在 Seata 全局事务下没有本地
            // TransactionStatus → 抛 NoTransactionException，把真正的失败原因（例如
            // 未部署 activity 服务的 Feign 异常）盖掉，前端只看到「操作失败」。
            log.error("更改订单状态失败: {}", e.getMessage(), e);
            try {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            } catch (Exception ignored) {
                log.warn("当前无本地事务上下文，跳过 setRollbackOnly");
            }
        } finally {
        }
    }

    public ResponseResult<List<AssetTmpInfoDTO>> getDeteails(String orderId, String paymentNumber) {
        //新资产中心-余额够买普通商品
        List<AssetTmpInfoDTO> dataList = null;
        // 判断纯数字订单
        if (PayToolUtil.isInteger(orderId)) {
            dataList = orderServiceClient.queryAssetInfo(orderId, "");
        } else {
            dataList = orderServiceClient.queryAssetInfo("", paymentNumber);
        }
        if (!dataList.isEmpty()) {
//            if (!DateUtils.getDateSpecific(new Date(), dataList.get(0).getOrderEndTime())) {
//                //该订单已超时，不能支付！
//                return ResponseResult.fail(BizCodeEnume.ORDER_HAS_TIMED_OUT_CANNOT_BE_PAID);
//            }
            return ResponseResult.success(dataList);
        } else {
            //未找到该订单！
            return ResponseResult.fail(BizCodeEnume.ORDER_IS_NULL);
        }
    }

    /**
     * 获取订单信息
     *
     * @param orderId
     * @param paymentNumber
     * @return
     */
    public OrderBaseVO getOrderBase(String orderId, String paymentNumber) {
        ResponseResult<OrderBaseVO> orderBase = orderServiceClient.getOrderBase(orderId, paymentNumber);
        if (orderBase.getCode() == 1) {
            return orderBase.getData();
        }
        return null;
    }

    /**
     * 质保金充值
     *
     * @return
     */
    public ResponseResult getetentionPay(Map<Object, Object> assetDetailMap, Map<Object, Object> map) {

        payAssetDetailsMapper.insertDetail(assetDetailMap);

        //插入记录信息
        ResponseResult recharge = merchantServiceClient.recharge(map);
        if (recharge.getCode() != 1) {
            // 回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return recharge;
    }
}
