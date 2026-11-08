package com.houjiang.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.util.DateUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.StringUtils;
import com.houjiang.commons.vo.fx.GoodsVkSetVO;
import com.houjiang.commons.vo.fx.VKUserInfoVO;
import com.houjiang.commons.vo.order.OrderCommissionVO;
import com.houjiang.commons.vo.order.WapShippingQueryResultVO;
import com.houjiang.commons.vo.order.WapShopShippingQueryResultVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.order.feign.BaseServiceClient;
import com.houjiang.order.feign.UserServiceClient;
import com.houjiang.order.service.FXService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Amos
 * @date 2021-06-0510:29
 * @deprecated : 分销
 **/
@Service
public class FXServiceImpl implements FXService {
    private static final Logger logger = LoggerFactory.getLogger(FXServiceImpl.class);

    @Resource
    private BaseServiceClient baseServiceClient;
    @Resource
    private UserServiceClient userServiceClient;

    /**
     * 根据订单信息和微客相关信息，计算并添加订单佣金信息。
     *
     * @param shippingQueryResult     物流查询结果，包含订单的相关物流信息。
     * @param shopShippingQueryResult 商家物流查询结果，包含商家订单的物流信息。
     * @param userInfo                用户信息，用于查询微客绑定的上级信息。
     * @param shareUserId             分享者的用户ID，用于查询分享者的微客信息。
     * @param orderId                 订单ID，用于关联订单和佣金信息。
     * @param orderPrice              订单价格，用于计算佣金。
     * @param fxType                  分销类型，用于确定佣金计算规则。
     * @param vkShopId                微客商家ID，用于查询商家的佣金设置。
     * @return 返回订单佣金信息VO，如果不需要计算佣金则返回null。
     */
    @Override
    public OrderCommissionVO addOrderCommission(WapShippingQueryResultVO shippingQueryResult, WapShopShippingQueryResultVO shopShippingQueryResult, UserInfoVO userInfo, String shareUserId, String orderId, BigDecimal orderPrice, Integer fxType, Integer vkShopId) {
        // 查询是否开启了微客推广和分佣业绩归属设置
        ResponseResult resultResp = baseServiceClient.queryGoodsVkSet();
        GoodsVkSetVO goodsVkSet = JSON.parseObject(JSON.toJSONString(resultResp.getData()), GoodsVkSetVO.class);
        if (goodsVkSet.getVkState() != 1 || shippingQueryResult.getFxGoods() != 1) {
            return null;
        }

        // 查询当前用户绑定的微客上级信息
        Map<String, String> map2 = new HashMap<>(8);
        map2.put("userId", userInfo.getUserId());
        resultResp = userServiceClient.queryVKUserInfo(map2);
        VKUserInfoVO userInviter = JSON.parseObject(JSON.toJSONString(resultResp.getData()), VKUserInfoVO.class);

        // 检查佣金分配规则和订单是否满足计算佣金的条件
        this.commissionCheck(goodsVkSet, shippingQueryResult, shareUserId, userInviter, fxType, userInfo.getUserId());

        // 如果订单没有设置任何佣金比例，则不计算佣金
        if (shippingQueryResult.getOneTgRate().equals("0") && shippingQueryResult.getTwoTgRate().equals("0") &&
                shippingQueryResult.getFxRate().equals("0")) {
            //走到这里说明该商品不是分销商品
            return null;
        }

        // 查询分享者的微客信息
        VKUserInfoVO shareUserInfo = new VKUserInfoVO();
        if (!StringUtils.isEmpty(shareUserId)) {
            Map<String, String> map = new HashMap<>(8);
            map.put("userId", shareUserId);
            resultResp = userServiceClient.queryVKUserInfo(map);
            shareUserInfo = JSON.parseObject(JSON.toJSONString(resultResp.getData()), VKUserInfoVO.class);
            // 如果设置了仅计算邀请关系的佣金，则清空分享者的微客信息
            if (!shareUserInfo.getUserVkFlag()) {
                shareUserInfo.setUserId("");
                shareUserInfo.setUserName("");
                shareUserInfo.setUserMobile("");
            }
        }

        // 如果存在分享者微客信息、或存在邀请关系的微客信息，则创建并返回订单佣金信息
        if (!StringUtils.isEmpty(shareUserInfo.getUserId()) || !StringUtils.isEmpty(userInviter.getOneInviter()) ||
                !StringUtils.isEmpty(userInviter.getTwoInviter())) {
            // 3.创建微客订单
            return this.buildOrderCommission(shippingQueryResult, shopShippingQueryResult, shareUserId, userInfo, orderId, orderPrice, userInviter, shareUserInfo, vkShopId);
        }

        return null;
    }

    /**
     * 根据商品的佣金设置和微客状态，调整佣金比例并清理邀请人信息。
     * 当商品设置了微客状态和佣金配置时，根据不同的佣金配置和微客标记，调整一级、二级推广佣金和分享佣金的比例，并可能清除一级和二级邀请人信息。
     * 如果商品未开启微客推广或未设置佣金配置，则将所有佣金比例设置为0。
     *
     * @param goodsVkSet          商品的微客状态和佣金配置信息。
     * @param shippingQueryResult 运费模板查询结果，包含佣金比例信息。
     * @param shareUserId         分享人的用户ID。
     * @param vkUserInfoVO        微客用户信息，包含一级和二级微客标记。
     * @param fxType              购买类型，用于判断是否通过分享页购买。
     */
    private void commissionCheck(GoodsVkSetVO goodsVkSet, WapShippingQueryResultVO shippingQueryResult,
                                 String shareUserId, VKUserInfoVO vkUserInfoVO, Integer fxType,String userId) {
        // 当商品开启微客状态且设置了佣金配置时，进入条件判断
        if (goodsVkSet.getVkState() == 1 && goodsVkSet.getCommissionConfig() != 0) {
            // 如果只开启分享人分佣，则将一级、二级推广佣金比例设置为0，并清除一级、二级邀请人信息
            if (goodsVkSet.getCommissionConfig() == 2) {
                shippingQueryResult.setOneTgRate("0");
                shippingQueryResult.setTwoTgRate("0");
                clearOneInviter(vkUserInfoVO);
                clearTwoInviter(vkUserInfoVO);
            }
            // 如果只开启推广人分佣，则将分享佣金比例设置为0
            if (goodsVkSet.getCommissionConfig() == 1) {
                shippingQueryResult.setFxRate("0");
            }
            // 如果不是通过分享页购买，则将分享佣金比例设置为0
            if (fxType == 2) {
                shippingQueryResult.setFxRate("0");
            }
            if(userId.equals(shareUserId)){
                shippingQueryResult.setOneTgRate("0");
                shippingQueryResult.setTwoTgRate("0");
                clearOneInviter(vkUserInfoVO);
                clearTwoInviter(vkUserInfoVO);
            }
        } else {
            // 当微客推广关闭或未设置佣金配置时，将所有佣金比例设置为0
            shippingQueryResult.setOneTgRate("0");
            shippingQueryResult.setTwoTgRate("0");
            shippingQueryResult.setFxRate("0");
        }
    }


    /**
     * 清除微客的一级推广者信息。
     * <p>
     * 该方法用于重置微客信息对象中与一级推广者相关的信息字段，确保后续操作不会受到之前一级推广者信息的影响。
     * 这些字段包括一级推广者的用户名、用户手机号以及是否设置了一级推广者的标志。
     *
     * @param vkUserInfoVO 微客信息对象，包含微客的一级推广者信息。
     */
    private void clearOneInviter(VKUserInfoVO vkUserInfoVO) {
        // 清空一级推广者ID
        vkUserInfoVO.setOneInviter("");
        // 清空一级推广者用户名
        vkUserInfoVO.setOneUserName("");
        // 清空一级推广者用户手机号
        vkUserInfoVO.setOneUserMobile("");
        // 将是否设置了一级推广者的标志设为false
        vkUserInfoVO.setOneVkFlag(false);
    }


    /**
     * 清除二级推广者信息。
     * <p>
     * 该方法用于重置微客信息对象中的二级推广者相关属性，将其设置为默认值或空值。
     * 这样做的目的是在处理新的推广关系或清除现有推广关系时，确保对象状态的一致性和准确性。
     *
     * @param vkUserInfoVO 微客信息对象，包含微客的详细信息。
     *                     该对象的二级推广者相关信息将被清除。
     */
    private void clearTwoInviter(VKUserInfoVO vkUserInfoVO) {
        // 清空二级推广者的ID
        vkUserInfoVO.setTwoInviter("");
        // 清空二级推广者的用户名
        vkUserInfoVO.setTwoUserName("");
        // 清空二级推广者的用户手机号
        vkUserInfoVO.setTwoUserMobile("");
        // 将二级推广者标记为未设置，即false
        vkUserInfoVO.setTwoVkFlag(false);
    }

    @Override
    public WapShippingQueryResultVO selectIsVkShippingInfo(WapShippingQueryResultVO shippingQueryResult, UserInfoVO userInfo, String shareUserId, Integer fxType) {
        // 查询是否开启了微客推广和分佣业绩归属设置
        ResponseResult resultResp = baseServiceClient.queryGoodsVkSet();
        GoodsVkSetVO goodsVkSet = JSON.parseObject(JSON.toJSONString(resultResp.getData()), GoodsVkSetVO.class);
        if (goodsVkSet.getVkState() != 1 || shippingQueryResult.getFxGoods() != 1) {
            return null;
        }

        // 查询当前用户绑定的微客上级信息
        Map<String, String> map2 = new HashMap<>(8);
        map2.put("userId", userInfo.getUserId());
        resultResp = userServiceClient.queryVKUserInfo(map2);
        VKUserInfoVO userInviter = JSON.parseObject(JSON.toJSONString(resultResp.getData()), VKUserInfoVO.class);

        // 检查佣金分配规则和订单是否满足计算佣金的条件
        this.commissionCheck(goodsVkSet, shippingQueryResult, shareUserId, userInviter, fxType, userInfo.getUserId());

        // 如果订单没有设置任何佣金比例，则不计算佣金
        if (shippingQueryResult.getOneTgRate().equals("0") && shippingQueryResult.getTwoTgRate().equals("0") &&
                shippingQueryResult.getFxRate().equals("0")) {
            //走到这里说明该商品不是分销商品
            return null;
        }
        return shippingQueryResult;
    }


    /**
     * 根据订单信息和用户信息构建订单分佣信息。
     * 这个方法详细说明了如何从各种输入参数中提取信息，并填充到OrderCommissionVO对象中，
     * 用于后续的订单分佣计算和记录。
     *
     * @param shippingQueryResult     商品信息，包含商品ID、名称、价格等。
     * @param shopShippingQueryResult 店铺信息，主要用到店铺ID。
     * @param shareUserId             分享者的用户ID，用于记录订单的分享关系。
     * @param userInfo                下单用户的个人信息，包含用户ID、用户名等。
     * @param OrderId                 订单ID，用于标识唯一的订单。
     * @param orderPrice              订单价格，用于计算分佣金额。
     * @param vkUserInfoVO            下单用户对应的微客信息，包含微客的一级、二级邀请人信息。
     * @param shareUserInfo           分享用户的微客信息，用于记录分享关系。
     * @return OrderCommissionVO 对象，包含了订单分佣的所有相关信息。
     */
    public OrderCommissionVO buildOrderCommission(WapShippingQueryResultVO shippingQueryResult, WapShopShippingQueryResultVO shopShippingQueryResult,
                                                  String shareUserId, UserInfoVO userInfo, String OrderId, BigDecimal orderPrice,
                                                  VKUserInfoVO vkUserInfoVO, VKUserInfoVO shareUserInfo, Integer vkShopId) {
        // 创建OrderCommissionVO实例，用于存储订单分佣信息
        OrderCommissionVO orderCommission = new OrderCommissionVO();

        // 填充订单基本信息，如订单ID、商品ID、商品名称等
        orderCommission.setOrderId(OrderId);
        orderCommission.setGoodsId(shippingQueryResult.getGoodsId());
        orderCommission.setGoodsName(shippingQueryResult.getGoodsName());
        orderCommission.setGoodsPrice(shippingQueryResult.getGoodsPrice());
        orderCommission.setGoodsNum(shippingQueryResult.getGoodsNum());
        orderCommission.setGoodsImage(shippingQueryResult.getGoodsImage());
        orderCommission.setCatId(shippingQueryResult.getCatId());

        // 填充店铺信息，如店铺ID
        orderCommission.setShopId(shopShippingQueryResult.getShopId());
        orderCommission.setVkShopId(vkShopId);

        // 填充分享者和被分享者的相关信息，如用户ID、用户名等
        orderCommission.setShareUserId(shareUserInfo.getUserId());
        orderCommission.setShareUserName(shareUserInfo.getUserName());
        orderCommission.setShareUserMobile(shareUserInfo.getUserMobile());

        // 填充微客信息，如一级、二级邀请人的ID、用户名等
        orderCommission.setTgOneUserId(vkUserInfoVO.getOneInviter());
        orderCommission.setTgOneUserName(vkUserInfoVO.getOneUserName());
        orderCommission.setTgOneUserMobile(vkUserInfoVO.getOneUserMobile());
        orderCommission.setTgTwoUserId(vkUserInfoVO.getTwoInviter());
        orderCommission.setTgTwoUserName(vkUserInfoVO.getTwoUserName());
        orderCommission.setTgTwoUserMobile(vkUserInfoVO.getTwoUserMobile());

        // 填充购买者信息，如用户ID、用户名、头像等
        orderCommission.setBuyUserId(userInfo.getUserId());
        orderCommission.setBuyUserName(userInfo.getUserName());
        orderCommission.setBuyUserMobile(userInfo.getUserMobile());
        orderCommission.setBuyUserAvatar(userInfo.getUserAvatar());
        orderCommission.setBuyUserTime(userInfo.getUserLastloginTime());

        // 设置订单状态、价格等相关信息
        orderCommission.setOrderStatus(CommonType.CommissionOrderStatus.UN_PAY.getCode());
        orderCommission.setOrderPrice(orderPrice);
        orderCommission.setOneTgRate(shippingQueryResult.getOneTgRate());
        orderCommission.setTwoTgRate(shippingQueryResult.getTwoTgRate());
        orderCommission.setFxRate(shippingQueryResult.getFxRate());

        // 计算并设置订单的支付时间
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        orderCommission.setPayTime(dateFormat.format(now));

        // 根据佣金比例计算实际佣金金额，并设置到订单分佣信息中
        double oneTg = Double.parseDouble(shippingQueryResult.getOneTgRate()) / 100;
        double twoTg = Double.parseDouble(shippingQueryResult.getTwoTgRate()) / 100;
        double fx = Double.parseDouble(shippingQueryResult.getFxRate()) / 100;
        orderCommission.setOneTgCommission(orderPrice.multiply(new BigDecimal(oneTg).setScale(2, RoundingMode.HALF_UP)));
        if(StringUtils.isNotEmpty(orderCommission.getTgTwoUserId())){
            orderCommission.setTwoTgCommission(orderPrice.multiply(new BigDecimal(twoTg).setScale(2, RoundingMode.HALF_UP)));
        }else{
            orderCommission.setTwoTgCommission(new BigDecimal(0));
        }
        orderCommission.setFxCommission(orderPrice.multiply(new BigDecimal(fx).setScale(2, RoundingMode.HALF_UP)));

        // 设置订单分佣类型和绑定类型
        orderCommission.setCommissionType(1);
        if (shareUserId != null && vkUserInfoVO.getOneInviter() != null && shareUserId.equals(vkUserInfoVO.getOneInviter())) {
            orderCommission.setBindType(1);
        } else {
            orderCommission.setBindType(2);
        }

        // 记录创建时间
        orderCommission.setCreateTime(DateUtils.getNowDateToString());

        // 设置绑定的用户ID，根据分享者ID和微客信息决定
        orderCommission.setBindUserId(StringUtils.isEmpty(shareUserId) ? vkUserInfoVO.getOneInviter() : shareUserId);

        // 返回构建完成的订单分佣信息
        return orderCommission;
    }


}
