package com.houjiang.order.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.goods.ActivityGoodsDetailDTO;
import com.houjiang.commons.dto.goods.ReceiveGoodsDetailsDTO;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.*;
import com.houjiang.commons.util.json.JsonUtils;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.activity.AppSeckillGoodsQueryResultVO;
import com.houjiang.commons.vo.activity.ManjianzengzhePromotionContentQueryResultVO;
import com.houjiang.commons.vo.goods.ReceiveGoodsDetailsVO;
import com.houjiang.commons.vo.order.ShoppingCartVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.order.dto.EntityObjectsDTO;
import com.houjiang.order.entity.OrderUserCartEntity;
import com.houjiang.order.feign.*;
import com.houjiang.order.mapper.OrderUserCartMapper;
import com.houjiang.order.service.OrderUserCartService;
import io.seata.core.context.RootContext;
import io.seata.core.exception.TransactionException;
import io.seata.spring.annotation.GlobalTransactional;
import io.seata.tm.api.GlobalTransactionContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * @ClassName OrderUserCartServiceImpl
 * @Description 购物车表
 * @Author Amos
 * @Date 2023-05-25 13:39:42
 * @Version 1.0
 * @author Amos
 */
@Service
public class OrderUserCartServiceImpl extends ServiceImpl<OrderUserCartMapper, OrderUserCartEntity>
        implements OrderUserCartService {
    @Autowired
    private MerchantServiceClient merchantServiceClient;
    @Autowired
    private BaseServiceClient baseServiceClient;
    @Autowired
    private UserServiceClient userServiceClient;
    @Autowired
    private GoodServiceClient goodServiceClient;
    @Autowired
    private ActivityServiceClient activityServiceClient;
    @Autowired
    private com.houjiang.order.service.ReductionPolicyQueryService reductionPolicyQueryService;
    /**
     * 二开购物车校验网关（before*）：加购 / 改数量 / 结算前回调 {@code CartValidateExtension}，只读、可中断。
     */
    @Autowired
    private com.houjiang.order.plugin.CartValidateGateway cartValidateGateway;

    final Logger log = LoggerFactory.getLogger(OrderUserCartServiceImpl.class);

    @Override
    public Integer selectCurrentCartQuantity(String userId, Integer goodsId) {
        return this.baseMapper.selectCurrentCartQuantity(userId, goodsId);
    }

    /**
     * 根据商品ID修改商品数量。
     * 该方法主要用于处理用户购物车中商品数量的变更，包括库存检查和数量更新。
     *
     * @param map 包含请求参数的映射，其中重要参数有：
     *            - token: 用户令牌，用于获取用户信息。
     *            - goodsId: 商品ID，指定需要修改数量的商品。
     *            - goodsNum: 商品数量，指定商品的新数量。
     *            - shopGateId: 门店ID，可选，用于特定门店库存的检查。
     * @return 返回操作结果，包括成功与否和错误信息。
     */
    @Override
    public ResponseResult changeGoodsNum(Map<String, Object> map) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        if (null != userInfo) {
            // 获取商品ID
            Integer goodsId = (Integer) map.get("goodsId");
            // 初始化门店ID，默认为null
            Integer shopGateId = null;
            // 如果请求中包含门店ID且不为空，则赋值给shopGateId
            if(null != map.get("shopGateId") && StringUtils.isNotBlank(map.get("shopGateId").toString())){
                shopGateId = Integer.valueOf(map.get("shopGateId").toString());
            }
            try {
                // 调用业务服务获取商品详情
                ResponseResult rr = merchantServiceClient.getGoodsDetails(goodsId);
                // 检查商品详情获取是否成功
                if (rr.getCode() != 1) {
                    return ResponseResult.fail(0, "未获取到信息！");
                } else {
                    // 将商品详情的JSON字符串转换为对象
                    String goodsDetails = null;
                    // json转对象
                    ReceiveGoodsDetailsVO receiveGoodsDetails = goodsDetails != null ? JsonUtils.string2Obj(goodsDetails, ReceiveGoodsDetailsVO.class) : null;
                    // 从商品详情中获取库存和销售限制相关的信息
                    Integer goodsStock = receiveGoodsDetails.getGoodsStock();
                    Integer goodsMaxSale = receiveGoodsDetails.getGoodsMaxSale();
                    Integer commonLimit = receiveGoodsDetails.getCommonLimit();
                    Integer goodsMinSale = receiveGoodsDetails.getGoodsMinSale();
                    // 获取请求中的商品数量
                    Integer goodsNum = (Integer) map.get("goodsNum");
                    // 如果有门店ID，使用门店库存替代商品总库存
                    if(null!= shopGateId){
                        //当门店id不为空需要 查询门店的商品库存
                        Integer gateShopStock = this.baseMapper.selectpGateShopStock(shopGateId, goodsId);
                        if(null == gateShopStock){
                            gateShopStock = 0;
                        }
                        goodsStock = gateShopStock;
                    }
                    // 如果商品有限制数量，检查商品数量是否在允许范围内
                    if (commonLimit != null && commonLimit != 0) {
                        // 如果商品数量不在允许范围内，直接返回失败
                        if (goodsNum > goodsMaxSale || goodsNum < goodsMinSale) {
                        } else {
                            // 如果商品数量在允许范围内，检查库存
                            if (goodsNum > goodsStock) {
                                return ResponseResult.fail(BizCodeEnume.USER_CART_STOCK_ERROR.getCode(), BizCodeEnume.USER_CART_STOCK_ERROR.getMsg());
                            } else {
                                // 更新商品数量
                                Boolean IsSuccess = this.baseMapper.changeGoodsNum(userInfo.getUserId(), goodsId, goodsNum,shopGateId);
                                // 根据更新结果返回相应的成功或失败信息
                                if (IsSuccess) {
                                    rr.setCode(1);
                                    return ResponseResult.success();
                                } else {
                                    rr.setCode(2);
                                    return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(), BizCodeEnume.SYSTEM_ERROR.getMsg());
                                }
                            }
                        }
                    } else {
                        // 如果商品没有限制数量，直接检查库存
                        if (goodsNum > goodsStock) {
                            rr.setCode(2);
                            return ResponseResult.fail(BizCodeEnume.USER_CART_STOCK_ERROR.getCode(), BizCodeEnume.USER_CART_STOCK_ERROR.getMsg());
                        } else {
                            // 更新商品数量
                            Boolean IsSuccess = this.baseMapper.changeGoodsNum(userInfo.getUserId(), goodsId, goodsNum,shopGateId);
                            // 根据更新结果返回相应的成功或失败信息
                            if (IsSuccess) {
                                rr.setCode(1);
                                return ResponseResult.success();
                            } else {
                                rr.setCode(2);
                                return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(), BizCodeEnume.SYSTEM_ERROR.getMsg());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // 捕获异常，不做处理，返回系统错误信息
                e.printStackTrace();
            }
        } else {
            // 如果用户信息获取失败，返回令牌非法的错误信息
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());
        }
        // 如果执行到此处，表示操作失败，返回系统错误信息
        return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(), BizCodeEnume.SYSTEM_ERROR.getMsg());
    }

    /**
     * 批量删除商品接口。
     *
     * @param map 请求参数映射表，包含token、goodsId列表和可选的shopGateId。
     * @return 删除操作的结果，成功或失败及其原因。
     */
    @Override
    public ResponseResult batchDeleteGoods(Map<String, Object> map) {
        // 通过token获取用户信息，用于后续的权限验证。
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        // 如果用户信息为空，则返回认证失败的响应结果。
        if (null == userInfo) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());
        }
        try {
            // 提取用户ID和待删除商品的ID列表。
            String userId = userInfo.getUserId();
            @SuppressWarnings("unchecked")
            List<Integer> goodsIdList = (List<Integer>) map.get("goodsId");
            // 可选的店铺门牌ID，用于限定删除范围。
            Integer shopGateId = null;
            if(null != map.get("shopGateId") && StringUtils.isNotBlank(map.get("shopGateId").toString())){
                shopGateId = Integer.valueOf(map.get("shopGateId").toString());
            }
            // 调用底层Mapper，执行批量删除操作。
            this.baseMapper.batchDeleteGoods(userId, goodsIdList,shopGateId);

        } catch (Exception e) {
            // 捕获并处理异常，此处省略具体处理逻辑。
        }
        // 如果删除操作没有明显失败，则返回成功的响应结果。
        return ResponseResult.success();
    }

    @Override
    public ResponseResult queryShoppingCart(Map<String, Object> map) {
        //plus状态查询
        Integer selectplus = (Integer) baseServiceClient.queryMerchantPlusRule().getData();

        String token = (String) map.get("token");
        try {
            String userId = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USERID + token);
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
            Integer shopGateId = null;
            if(null != map.get("shopGateId") && StringUtils.isNotBlank(map.get("shopGateId").toString())){
                shopGateId = Integer.valueOf(map.get("shopGateId").toString());
            }
            //查询用户购物车
            List<OrderUserCartEntity> cartList = this.baseMapper.queryUserShoppingCart(userId/*,goodsType*/,CommonType.shoppingCartStatus.NORMAL.getCode(),shopGateId);
            //查询店铺名和店铺id
            List<EntityObjectsDTO> cartListByShopName = this.baseMapper.queryShoppingCartByShopName(userId/*, goodsType*/);
            Map<String, Object> resultMap = new HashMap<>();
            Integer numberOfValidProducts = 0;
            List<EntityObjectsDTO> cartListByShopNameList = new ArrayList<>();
            List<OrderUserCartEntity> userXiaJiaCartList = new ArrayList<>();
            //遍历店铺
            for (EntityObjectsDTO entityObjects : cartListByShopName) {
                Integer goodsNum = 0;
                Integer shopInvoices = null;
                Integer plus_goods = null;
                Integer shopIdTmp = entityObjects.getShopId();
                Integer result = 0;
                Map<String, Object> paramTmp = new HashMap<>();
                paramTmp.put("shopId", shopIdTmp);
                //查询发票功能是否开启
                ResponseResult data = merchantServiceClient.queryShopInvoiceSwitchStatus(paramTmp);
                entityObjects.setInvoiceActive((Integer) data.getData());
                List<OrderUserCartEntity> userShoppingCartList = new ArrayList<>();
                //遍历用户购物车
                for (OrderUserCartEntity userShoppingCart : cartList) {
                    //设置会员折扣
                    userShoppingCart.setPlusMember(userInfo.getPlusMember());
                    userShoppingCart.setPlusDiscount(userInfo.getPlusDiscount());
                    if (entityObjects.getShopId().toString().equals(userShoppingCart.getShopId().toString())) {
                        Integer goodsId = userShoppingCart.getGoodsId();
                        Integer goodsStatus = null;
                        Integer goodsStock = null;
                        Integer goodsMaxSale = null;
                        Integer goodsMinSale = null;
                        Integer activityStatus = null;
                        Integer activityGoodsState = null;
                        Integer toBuyMaxNum = null;
                        Integer purchased = null;
                        BigDecimal goodsPrice = new BigDecimal(0);
                        String pic = null;
                        //查询商品详细信息
                        ResponseResult goodResult = goodServiceClient.getGoodsDetails(goodsId);
                        if (goodResult.getCode() != CommonType.commonStatus.YES.getCode()) {
                            return ResponseResult.fail(BizCodeEnume.NO_INFORMATION_OBTAINED.getCode());
                        } else {
                            String goodsDetails = goodResult.getData().toString();
                            // json转对象
                            ReceiveGoodsDetailsDTO receiveGoodsDetails = JsonUtils.string2Obj(goodsDetails,
                                    ReceiveGoodsDetailsDTO.class);
                            activityStatus = receiveGoodsDetails.getActivityStatus();
                            activityGoodsState = receiveGoodsDetails.getActivityGoodsState();
                            ActivityGoodsDetailDTO agd = receiveGoodsDetails.getActivityGoodsDetail();
                            if(null!= shopGateId){
                                //当门店id不为空需要 查询门店的商品库存
                                Integer gateShopStock = this.baseMapper.selectpGateShopStock(shopGateId, goodsId);
                                if(null == gateShopStock){
                                    gateShopStock = 0;
                                }
                                receiveGoodsDetails.setGoodsStock(gateShopStock);
                            }
                            if (activityStatus == 1) {

                            } else if (activityStatus == 2 && receiveGoodsDetails.getActivityGoodsState() == 1) {
                                HashMap<String, Object> map1 = new HashMap<>();
                                map1.put("token", token);
                                map1.put("goodsId", goodsId);
                                map1.put("commonId", receiveGoodsDetails.getCommonId());
                                Integer seckillPurchased = 0;
                                try {
                                    ResponseResult resp = activityServiceClient.seckillCheckLimit(map1);
                                    if (resp != null && resp.getData() != null) {
                                        seckillPurchased = (Integer) resp.getData();
                                    }
                                } catch (Exception e) {
                                    log.warn("秒杀限购校验不可用（促销插件未安装或服务异常），按未购买计入: {}", e.getMessage());
                                }
                                purchased = seckillPurchased;
                                goodsPrice = agd.getActivityPrice();
                                pic = receiveGoodsDetails.getGoodsImage();
                                shopInvoices = receiveGoodsDetails.getShopInvoices();
                                goodsStatus = receiveGoodsDetails.getCartStatus();
                                goodsStock = receiveGoodsDetails.getGoodsStock();
                                goodsMaxSale = receiveGoodsDetails.getGoodsMaxSale();
                                goodsMinSale = receiveGoodsDetails.getGoodsMinSale();

                            } else if (activityStatus == 3 && receiveGoodsDetails.getActivityGoodsState() == 1) {
                                HashMap<String, Object> map1 = new HashMap<>();
                                map1.put("token", token);
                                map1.put("goodsId", goodsId);
                                map1.put("commonId", receiveGoodsDetails.getCommonId());
                                Integer discountPurchased = 0;
                                try {
                                    ResponseResult resp = activityServiceClient.discountCheckLimit(map1);
                                    if (resp != null && resp.getData() != null) {
                                        discountPurchased = (Integer) resp.getData();
                                    }
                                } catch (Exception e) {
                                    log.warn("限时折扣限购校验不可用（促销插件未安装或服务异常），按未购买计入: {}", e.getMessage());
                                }
                                purchased = discountPurchased;
                                goodsPrice = agd.getActivityPrice();
                                pic = receiveGoodsDetails.getGoodsImage();
                                shopInvoices = receiveGoodsDetails.getShopInvoices();
                                goodsStatus = receiveGoodsDetails.getCartStatus();
                                goodsStock = receiveGoodsDetails.getGoodsStock();
                                goodsMaxSale = receiveGoodsDetails.getGoodsMaxSale();
                                goodsMinSale = receiveGoodsDetails.getGoodsMinSale();
                            } else {
                                if (activityStatus == 5 || activityStatus == null || activityStatus == 0) {
                                    List<ManjianzengzhePromotionContentQueryResultVO> promotionContents;
                                    try {
                                        promotionContents = reductionPolicyQueryService.selectContent(receiveGoodsDetails.getCommonId());
                                    } catch (Exception e) {
                                        // 促销为可选商业插件：查不到规则即无优惠，按原价成交，不阻断下单（最终版 §3.2）
                                        log.warn("满减满折规则查询不可用（促销插件未安装或服务异常），按无优惠原价处理: {}", e.getMessage());
                                        promotionContents = new java.util.ArrayList<>();
                                    }
                                    if (promotionContents == null) {
                                        promotionContents = new java.util.ArrayList<>();
                                    }
                                    if (!promotionContents.isEmpty() && (activityStatus == null || activityStatus == 0)) {
                                        // 商品表未标促销时挂上满减态，保证结算累计满减/满折
                                        activityStatus = 5;
                                    }
                                }
                                pic = receiveGoodsDetails.getGoodsImage();
                                goodsPrice = receiveGoodsDetails.getGoodsPrice();
                                shopInvoices = receiveGoodsDetails.getShopInvoices();
                                goodsStatus = receiveGoodsDetails.getCartStatus();
                                goodsStock = receiveGoodsDetails.getGoodsStock();
                                goodsMaxSale = receiveGoodsDetails.getGoodsMaxSale();
                            }
                            plus_goods = receiveGoodsDetails.getPlusGoods();
                            toBuyMaxNum = dealActivityInfo(token, goodsId, activityStatus, goodsMaxSale, goodsStock);
                        }
                        ReceiveGoodsDetailsDTO newReceiveGoodsDetails = JsonUtils.string2Obj(goodResult.getData().toString(),
                                ReceiveGoodsDetailsDTO.class);

                        userShoppingCart.setDealerGoodsFlag(null == newReceiveGoodsDetails.getDealerGoodsFlag() ? 0 : newReceiveGoodsDetails.getDealerGoodsFlag());
                        // 设置最大售卖数量、库存及商品上下架状态
                        userShoppingCart.setGoodsImage(pic);
                        userShoppingCart.setGoodsStock(goodsStock);
                        userShoppingCart.setGoodsPrice(Double.parseDouble(goodsPrice.toString()));
                        userShoppingCart.setGoodsIsShelves(goodsStatus);
                        userShoppingCart.setGoodsMaxSale(goodsMaxSale);
                        userShoppingCart.setGoodsName(newReceiveGoodsDetails.getGoodsName());
                        userShoppingCart.setCommonIsXian(activityStatus);
                        userShoppingCart.setGoodsMinSale(goodsMinSale);
                        userShoppingCart.setActivityGoodsState(activityGoodsState);
                        userShoppingCart.setPurchased(purchased);
                        userShoppingCart.setPlusMember(userInfo.getPlusMember());
                        userShoppingCart.setPlusDiscount(userInfo.getPlusDiscount());
                        userShoppingCart.setPlusGoods(plus_goods);
                        userShoppingCart.setGoodsStartTime(newReceiveGoodsDetails.getGoodsStartTime());
                        userShoppingCart.setGoodsIsShelves(newReceiveGoodsDetails.getGoodsIsShelves());
                        userShoppingCart.setPointsRate(newReceiveGoodsDetails.getPointsRate());
                        userShoppingCart.setCashPrice(newReceiveGoodsDetails.getCashPrice());
                        if (2 == activityStatus || 3 == activityStatus) {
                            userShoppingCart.setToBuyMaxNum(toBuyMaxNum);
                        }
                        userShoppingCart.setActivityGoodsDetail(newReceiveGoodsDetails.getActivityGoodsDetail());
                        Integer num = userShoppingCart.getGoodsNum();
                        BigDecimal goodsNumber = new BigDecimal(num);
                        BigDecimal totalPrice = goodsNumber.multiply(goodsPrice);
                        userShoppingCart.setGoodsTotalPrice(totalPrice);

                        // 计算共有几件商品
                        Integer goodsnum = userShoppingCart.getGoodsNum();
                        goodsNum += goodsnum;
                        entityObjects.setGoodsNum(goodsNum);
                        userShoppingCart.setGoodsSpecChineseValue(userShoppingCart.getGoodsSpecChineseValue());
                        if (userShoppingCart.getGoodsIsShelves() == 1 && newReceiveGoodsDetails.getCommonState() != 10
                                && userShoppingCart.getGoodsStock() > 0 && newReceiveGoodsDetails.getCommonStock() > 0) {
                            userShoppingCartList.add(userShoppingCart);
                            numberOfValidProducts++;
                        } else {
                            userXiaJiaCartList.add(userShoppingCart);
                        }
                    }
                }
                entityObjects.setShopInvoices(shopInvoices);
                entityObjects.setPlusStatus(selectplus);
                entityObjects.setGoodsDetailsList(userShoppingCartList);
                if (!userShoppingCartList.isEmpty()) {
                    cartListByShopNameList.add(entityObjects);
                }
            }
            resultMap.put("numberOfValidProducts", numberOfValidProducts);
            resultMap.put("list", cartListByShopNameList);
            resultMap.put("invalidProductList", userXiaJiaCartList);
            return ResponseResult.success(resultMap);

        } catch (Exception e) {
            e.printStackTrace();
            log.info("queryShoppingCartErrorResults:" + e.getMessage());
            return ResponseResult.fail(e.getMessage());
        }
    }

/**
 * 通过购物车删除无效商品。
 * 该方法用于根据用户的购物车信息，检查并删除用户购物车中已下架、售罄或无法购买的商品。
 * @param map 包含用户token等信息的映射对象。
 * @return 返回操作结果，成功或失败及原因。
 */
@Override
public ResponseResult deleteInvalidProductByCart(Map<String, Object> map) {
    // 从传入的map中获取用户token
    String token = (String) map.get("token");
    try {
        // 通过token获取用户ID
        String userId = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USERID + token);
        // 初始化用户信息
        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(userId);
        // 调用userServiceClient查询用户信息
        ResponseResult userInfoResult = userServiceClient.queryUserInfo(vo);
        // 查询用户的购物车商品列表，不包括已删除和异常状态的商品
        List<OrderUserCartEntity> cartList = this.baseMapper.selectList(new LambdaQueryWrapper<OrderUserCartEntity>()
                .eq(OrderUserCartEntity::getUserId, userId)
                .eq(OrderUserCartEntity::getCartStatus, CommonType.shoppingCartStatus.NORMAL.getCode())
                .orderByDesc(OrderUserCartEntity::getInsertCartTime));
        // 根据用户ID查询购物车中每个商品对应的店铺名称
        List<EntityObjectsDTO> cartListByShopName = this.baseMapper.queryShoppingCartByShopName(userId);
        // 用于存储需要删除的商品ID
        List<Integer> goodIdList = new ArrayList<>();
        // 遍历所有店铺
        for (EntityObjectsDTO entityObjects : cartListByShopName) {
            // 遍历用户购物车中的所有商品
            for (OrderUserCartEntity userShoppingCart : cartList) {
                // 如果购物车中的商品所属店铺与当前遍历的店铺匹配
                if (entityObjects.getShopId().toString().equals(userShoppingCart.getShopId().toString())) {
                    Integer goodsId = userShoppingCart.getGoodsId();
                    // 调用goodServiceClient获取商品详情
                    ResponseResult goodResult = goodServiceClient.getGoodsDetails(goodsId);
                    // 如果获取商品详情失败，则返回失败结果
                    if (goodResult.getCode() != CommonType.commonStatus.YES.getCode()) {
                        return ResponseResult.fail(BizCodeEnume.NO_INFORMATION_OBTAINED.getCode());
                    }
                    // 解析商品详情JSON字符串
                    String goodsDetails = goodResult.getData().toString();
                    ReceiveGoodsDetailsDTO receiveGoodsDetails = JsonUtils.string2Obj(goodsDetails, ReceiveGoodsDetailsDTO.class);
                    // 如果解析失败或商品已下架、售罄或库存为0，则将商品ID加入到待删除列表
                    if (receiveGoodsDetails == null) {
                        return ResponseResult.fail(BizCodeEnume.NO_GOODS);
                    }
                    if (receiveGoodsDetails.getGoodsIsShelves() != 1 || receiveGoodsDetails.getCommonState() == 10
                            || receiveGoodsDetails.getGoodsStock() <= 0 || receiveGoodsDetails.getCommonStock() <= 0) {
                        goodIdList.add(goodsId);
                    }
                }
            }
        }
        // 如果map中包含shopGateId且不为空，则获取shopGateId用于后续删除操作
        Integer shopGateId = null;
        if(null != map.get("shopGateId") && StringUtils.isNotBlank(map.get("shopGateId").toString())){
            shopGateId = Integer.valueOf(map.get("shopGateId").toString());
        }
        // 批量删除购物车中无效的商品
        this.baseMapper.batchDeleteGoods(userId, goodIdList,shopGateId);
        // 返回操作成功结果
        return ResponseResult.success();
    } catch (Exception e) {
        // 打印异常信息
        e.printStackTrace();
        log.info("deleteInvalidProductByCart:" + e.getMessage());
        // 返回操作失败结果，包含异常信息
        return ResponseResult.fail(e.getMessage());
    }
}


    /**
     * 在购物车中添加商品。
     * 使用全局事务确保操作的原子性。
     *
     * @param map 包含添加购物车所需信息的映射。
     * @return 添加购物车操作的结果。
     * @throws TransactionException 如果操作过程中发生事务异常。
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public ResponseResult insertShoppingCart(Map<String, Object> map) throws TransactionException {
        // 从请求映射中提取用户令牌
        String token = (String) map.get("token");

        try {
            // 使用令牌获取用户信息
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
            // 提取用户ID
            String userId = userInfo.getUserId();
            //判断是否为品牌店铺用户
            UserInfoVO vo = new UserInfoVO();
            vo.setUserId(userId);
            // 调用远程服务查询用户详细信息
            ResponseResult userInfoResult = userServiceClient.queryUserInfo(vo);
            // 从用户信息结果中提取用户映射信息
            Map<String, Object> userMap = (Map<String, Object>) userInfoResult.getData();
            // 提取商家店铺ID，优先使用用户信息中的店铺ID
            String sellerShopId = userInfo.getShopId();
            // 如果用户映射不为空，则使用用户映射中的店铺ID
            if (userMap != null) {
                sellerShopId = String.valueOf(userMap.get("shopId"));
            }
            // 提取请求中的店铺入口ID，如果存在且不为空
            Integer shopGateId = null;
            if(null != map.get("shopGateId") && StringUtils.isNotBlank(map.get("shopGateId").toString())){
                shopGateId = Integer.valueOf(map.get("shopGateId").toString());
            }
            // 提取请求中的商品ID和数量
            Integer goodsId = Integer.parseInt(map.get("goodsId").toString());
            Integer goodsNum = Integer.parseInt(map.get("goodsNum").toString());
            // 二开购物车校验（before*）：加购前回调 CartValidateExtension，只读、可中断（限购 / 不可售地区等）。
            // ctx.abort → 抛 PluginAbortException(7001)，由全局异常处理器按业务错误码回传。params 透传原始请求参数。
            {
                List<Object> validateItems = new ArrayList<>();
                validateItems.add(goodsId);
                cartValidateGateway.validate(null, validateItems,
                        com.houjiang.order.plugin.CartValidateGateway.ACTION_ADD, map);
            }
            // 调用方法添加商品到购物车
            return addShoppingCart(userId, goodsId, goodsNum, sellerShopId,shopGateId);
        } catch (Exception e) {
            // 打印异常堆栈跟踪
            e.printStackTrace();
            // 重新加载全局事务ID并回滚事务
            // 回滚
            GlobalTransactionContext.reload(RootContext.getXID()).rollback();
            // 返回操作失败的结果，附带异常信息
            return ResponseResult.fail(e.getMessage());
        }

    }


    /**
     * 获取购物车列表
     *
     * @param param 购物车信息的VO对象，包含分页和过滤条件等
     * @return 返回购物车列表的响应结果
     * @throws IllegalAccessException 当访问字段失败时抛出此异常
     */
    @Override
    public ResponseResult getShoppongCartList(ShoppingCartVO param) throws IllegalAccessException {
        // 将购物车VO对象转换为Map，用于后续的查询过滤
        Map data = ParseMapUtils.beanToMap(param);

        // 根据传入的参数和分页信息，从数据库中查询购物车列表
        IPage<ShoppingCartVO> page = this.baseMapper.getShoppongCartList(new Query<ShoppingCartVO>().getPage(data),
                param);

        // 获取脱敏开关的配置值，用于判断是否需要对查询结果进行脱敏处理
        String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.DESENSITIZATION_SWITCH.getDesc());

        // 如果脱敏开关打开，并且查询结果不为空，则对查询结果进行脱敏处理
        if (StrUtil.isNotEmpty(value) && value.equals(CommonType.commonStatus.YES.getCode().toString())) {
            if (!page.getRecords().isEmpty()) {
                for (ShoppingCartVO cartVO : page.getRecords()) {
                    // 脱敏处理后，将结果覆盖原对象属性
                    BeanUtils.copyProperties(DesensitizationUtil.Desensitization(cartVO, ShoppingCartVO.class), cartVO);
                }
            }
        }

        // 返回处理后的分页结果
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 根据传入的Map参数更新订单用户车辆信息。
     * <p>
     * 本方法通过调用BaseMapper的updateOrderUserCarByMap方法，使用Map中的键值对来更新订单用户车辆表中的相应记录。
     * 如果更新成功（即影响的行数大于0），则返回表示更新成功的响应结果；否则，返回表示更新失败的响应结果。
     * </p>
     *
     * @param map 包含更新订单用户车辆信息所需的键值对的Map对象。
     * @return 如果更新成功，返回包含成功信息的响应结果；如果更新失败，返回包含失败信息的响应结果。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult updateOrderUserCarByMap(Map<String, Object> map) {
        // 调用BaseMapper的updateOrderUserCarByMap方法来更新订单用户车辆信息
        Integer count = this.baseMapper.updateOrderUserCarByMap(map);
        // 判断更新操作影响的行数，如果大于0，则认为更新成功
        if (count > 0) {
            // 返回表示更新成功的响应结果
            return ResponseResult.success(BizCodeEnume.UPDATE_SUCCEEDED);
        }
        // 如果更新操作影响的行数不大于0，则认为更新失败
        return ResponseResult.fail(BizCodeEnume.UPDATE_ERROR);
    }

    @Override
    public ResponseResult insertShoppingCartBatch(Map<String, Object> map) {
        String token = (String) map.get("token");
        boolean isExist = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (isExist) {
            try {
                UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
                String userId = ui.getUserId();
                Integer commonId = Integer.parseInt(map.get("commonId").toString());
                Integer goodsNum = (Integer) map.get("goodsNum");
                // 加入购物车时间
                String insertCartTime = DateUtils.getNowDateToString();
                ResponseResult result = goodServiceClient.getGoodsCommonDetails(commonId);
                if (result.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
                    return result;
                } else {
                    String goodsDetails = result.getData().toString();
                    // json转对象
                    List<ReceiveGoodsDetailsVO> receiveGoodsDetailList = JSON.parseArray(goodsDetails, ReceiveGoodsDetailsVO.class);
                    for (ReceiveGoodsDetailsVO receiveGoodsDetails : receiveGoodsDetailList) {
                        Integer shopId = receiveGoodsDetails.getShopId();
                        String shopName = receiveGoodsDetails.getShopName();
                        String goodsName = receiveGoodsDetails.getGoodsName();
                        String goodsImage = receiveGoodsDetails.getGoodsImage();
                        BigDecimal goodsPrice = receiveGoodsDetails.getGoodsPrice();
                        Integer goodsStock = receiveGoodsDetails.getGoodsStock();
                        String goodsSpecChineseValue = receiveGoodsDetails.getGoodsSpecChineseValue();
                        Integer cartStatus = receiveGoodsDetails.getCartStatus();
                        Integer goodsStatus = null;
                        if (cartStatus == 1) {
                            goodsStatus = 0;
                        } else if (cartStatus == 2) {
                            goodsStatus = 1;
                        } else if (cartStatus == 0) {
                            goodsStatus = 2;
                        }
                        // 判断购物车是否存在该商品(查询goodsId)
                        OrderUserCartEntity entity = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderUserCartEntity>()
                                .eq(OrderUserCartEntity::getUserId, userId)
                                .eq(OrderUserCartEntity::getGoodsId, receiveGoodsDetails.getGoodsId())
                                .eq(OrderUserCartEntity::getCartStatus, CommonType.shoppingCartStatus.NORMAL.getCode()));
                        Integer queryGoodsId = entity.getGoodsId();
                        Integer goodsMaxSale = receiveGoodsDetails.getGoodsMaxSale();
                        if (receiveGoodsDetails.getGoodsId().equals(queryGoodsId)) {
                            if (goodsNum > goodsMaxSale || goodsNum > goodsStock) {
                                this.baseMapper.update(null, new LambdaUpdateWrapper<OrderUserCartEntity>()
                                        .eq(OrderUserCartEntity::getUserId, userId)
                                        .eq(OrderUserCartEntity::getGoodsId, receiveGoodsDetails.getGoodsId())
                                        .eq(OrderUserCartEntity::getCartStatus, CommonType.shoppingCartStatus.NORMAL.getCode())
                                        .set(OrderUserCartEntity::getGoodsNum, goodsMaxSale)
                                        .set(OrderUserCartEntity::getInsertCartTime, insertCartTime));
                                return ResponseResult.fail(BizCodeEnume.TREASURES_IS_OUT_OF_RANGE);
                            } else {
                                Integer cartGoodsNum = entity.getGoodsNum();
                                if (cartGoodsNum > goodsMaxSale) {
                                    this.baseMapper.update(null, new LambdaUpdateWrapper<OrderUserCartEntity>()
                                            .eq(OrderUserCartEntity::getUserId, userId)
                                            .eq(OrderUserCartEntity::getGoodsId, receiveGoodsDetails.getGoodsId())
                                            .eq(OrderUserCartEntity::getCartStatus, CommonType.shoppingCartStatus.NORMAL.getCode())
                                            .set(OrderUserCartEntity::getGoodsNum, goodsMaxSale)
                                            .set(OrderUserCartEntity::getInsertCartTime, insertCartTime));
                                    return ResponseResult.fail(BizCodeEnume.TREASURES_IS_OUT_OF_RANGE);
                                } else {
                                    // 增加商品数量
                                    // 增加商品数量
                                    Boolean IsSuccess = this.baseMapper.updateGoodsNum(userId, receiveGoodsDetails.getGoodsId(), goodsNum, insertCartTime);
                                    if (IsSuccess) {
                                        return ResponseResult.fail(BizCodeEnume.OPERATION_SUCCESSFUL);
                                    } else {
                                        return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
                                    }
                                }
                            }
                        } else {
                            OrderUserCartEntity data = new OrderUserCartEntity();
                            data.setUserId(userId);
                            data.setShopId(shopId);
                            data.setShopName(shopName);
                            data.setCommonId(commonId);
                            data.setGoodsId(receiveGoodsDetails.getGoodsId());
                            data.setGoodsName(goodsName);
                            data.setGoodsImage(goodsImage);
                            data.setGoodsNum(goodsNum);
                            data.setGoodsPrice(goodsPrice.doubleValue());
                            data.setGoodsStock(goodsStock);
                            data.setGoodsSpecChineseValue(goodsSpecChineseValue);
                            data.setInsertCartTime(DateUtils.getNowDate());
                            data.setCartStatus(goodsStatus);
                            data.setGoodsType(receiveGoodsDetails.getGoodsType());
                            // 插入购物车
                            boolean IsSuccess = this.baseMapper.insert(data) > 0;
                            if (IsSuccess) {
                                return ResponseResult.fail(BizCodeEnume.OPERATION_SUCCESSFUL);
                            } else {
                                return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
                            }
                        }
                    }
                }
                return ResponseResult.fail(BizCodeEnume.OPERATION_SUCCESSFUL);
            } catch (Exception e) {
                log.info("insertShoppingCartErrorResults:" + e.getMessage());
                return ResponseResult.fail(e.getMessage());
            }
        } else {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
    }

    /**
     * 减少购物车中商品的数量。
     *
     * @param map 包含购物车商品相关信息的映射表，包括商品ID、用户令牌等。
     * @return 返回操作结果，成功或失败及原因。
     */
    @Override
    public ResponseResult reduceGoodsNum(Map<String, Object> map) {

        // 从映射表中获取用户令牌
        String token = (String) map.get("token");
        try {
            // 根据令牌解析用户信息，检查令牌是否有效
            UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
            if (ui == null) {
                // 如果用户信息为空，则返回用户不存在的错误信息
                return ResponseResult.fail(BizCodeEnume.USER_DOES_NOT_EXIST);
            }
            // 初始化用户令牌bean
            // 获取用户权限
            UserTokenBeanDTO userTokenBean = new UserTokenBeanDTO();
            userTokenBean.setToken(token);
            // 从用户信息中获取用户ID
            String userId = ui.getUserId();
            // 从映射表中获取商品ID
            Integer goodsId = (Integer) map.get("goodsId");
            // 调用商品服务获取商品详情
            ResponseResult goodResult = goodServiceClient.getGoodsDetails(goodsId);
            if (goodResult.getCode() != 1) {
                // 如果获取商品详情失败，则返回未获取到信息的错误信息
                return ResponseResult.fail(BizCodeEnume.NO_INFORMATION_OBTAINED);
            }
            // 将商品详情的JSON字符串转换为对象
            String goodsDetails = goodResult.getData().toString();
            // json转对象
            ReceiveGoodsDetailsDTO receiveGoodsDetails = JsonUtils.string2Obj(goodsDetails,
                    ReceiveGoodsDetailsDTO.class);
            // 获取商品的最小销售数量
            Integer goodsMinSale = receiveGoodsDetails.getGoodsMinSale();
            // 查询购物车中该商品的数量
            // 判断购物车是否存在该商品(查询goodsId)
            OrderUserCartEntity entity = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderUserCartEntity>()
                    .eq(OrderUserCartEntity::getUserId, userId)
                    .eq(OrderUserCartEntity::getGoodsId, receiveGoodsDetails.getGoodsId())
                    .eq(OrderUserCartEntity::getCartStatus, CommonType.shoppingCartStatus.NORMAL.getCode()));
            // 计算新的商品数量，如果映射表中未提供数量，则默认减少1
            Integer goodsNum = map.get("goodsNum") == null ? entity.getGoodsNum() - 1 : (Integer) map.get("goodsNum");
            if (goodsMinSale != null && goodsMinSale != 0 && entity.getGoodsNum() < goodsMinSale) {
                // 如果商品数量小于最小销售数量，则返回错误信息
                // 宝贝数量小于范围
                return ResponseResult.fail(BizCodeEnume.TREASURES_IS_LESS_THAN_THE_RANGE);
            }
            // 更新购物车中商品的数量
            Boolean isSuccess = this.baseMapper.update(null, new LambdaUpdateWrapper<OrderUserCartEntity>()
                    .eq(OrderUserCartEntity::getUserId, userId)
                    .eq(OrderUserCartEntity::getGoodsId, goodsId)
                    .eq(OrderUserCartEntity::getCartStatus, 0)
                    .set(OrderUserCartEntity::getGoodsNum, goodsNum)) > 0;
            if (isSuccess) {
                // 如果更新成功，则返回成功信息
                return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL.getMsg());
            } else {
                // 如果更新失败，则返回失败信息
                return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
            }

        } catch (Exception e) {
            // 如果操作中出现异常，则返回异常信息
            e.printStackTrace();
            return ResponseResult.fail(e.getMessage());

        }
    }

    @Override
    public ResponseResult addGoodsNum(Map<String, Object> map) {
        String token = (String) map.get("token");
        boolean isExist = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (isExist) {
            try {

                UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
                String userId = ui.getUserId();
                Integer goodsId = (Integer) map.get("goodsId");
                ResponseResult goodResult = goodServiceClient.getGoodsDetails(goodsId);
                if (goodResult.getCode() != 1) {
                    return ResponseResult.fail(BizCodeEnume.NO_INFORMATION_OBTAINED);
                } else {
                    String goodsDetails = goodResult.getData().toString();
                    // json转对象
                    ReceiveGoodsDetailsDTO receiveGoodsDetails = JsonUtils.string2Obj(goodsDetails,
                            ReceiveGoodsDetailsDTO.class);
                    // 最大限购数
                    Integer goodsMaxSale = receiveGoodsDetails.getGoodsMaxSale();
                    // 库存
                    Integer goodsStock = receiveGoodsDetails.getGoodsStock();
                    //当前购物车数量
                    Integer goodsNum = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderUserCartEntity>()
                            .eq(OrderUserCartEntity::getUserId, userId).eq(OrderUserCartEntity::getGoodsId, goodsId)
                            .eq(OrderUserCartEntity::getCartStatus, 0)).getGoodsNum();
                    if(Objects.equals(receiveGoodsDetails.getCommonIsXian(), CommonType.promotionStatus.SECKILL.getCode())){
                        AppSeckillGoodsQueryResultVO seckillGoodsVO = receiveGoodsDetails.getActivityGoodsDetail().getSeckillGoods();
                        Integer seckillNum = seckillGoodsVO.getSeckillNum();
                        Integer commonNum = this.baseMapper.selectCommonNumByUserId(userId,receiveGoodsDetails.getCommonId());
                        if(commonNum + 1 > seckillNum){
                            return ResponseResult.fail(BizCodeEnume.ACTIVITY_LIMIT_PURCHASE_LIMIT);
                        }
                    }

                    if (goodsMaxSale == 0) {
                        if ((1 + goodsNum) > goodsStock || goodsNum >= goodsStock) {
                            return ResponseResult.fail(BizCodeEnume.TREASURES_IS_OUT_OF_RANGE);
                        } else {
                            Boolean IsSuccess = this.baseMapper.update(null, new LambdaUpdateWrapper<OrderUserCartEntity>()
                                    .eq(OrderUserCartEntity::getUserId, userId)
                                    .eq(OrderUserCartEntity::getGoodsId, goodsId)
                                    .eq(OrderUserCartEntity::getCartStatus, 0)
                                    .set(OrderUserCartEntity::getGoodsNum, goodsNum + 1)) > 0;
                            if (IsSuccess) {
                                return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
                            } else {
                                return ResponseResult.fail(BizCodeEnume.OPERATION_SUCCESSFUL);
                            }
                        }
                    } else {
                        if ((1 + goodsNum) > goodsStock || goodsNum >= goodsMaxSale || goodsNum >= goodsStock || ((1 + goodsNum) > goodsMaxSale)) {
                            return ResponseResult.fail(BizCodeEnume.TREASURES_IS_OUT_OF_RANGE);

                        } else {
                            Boolean IsSuccess = this.baseMapper.update(null, new LambdaUpdateWrapper<OrderUserCartEntity>()
                                    .eq(OrderUserCartEntity::getUserId, userId)
                                    .eq(OrderUserCartEntity::getGoodsId, goodsId)
                                    .eq(OrderUserCartEntity::getCartStatus, 0)
                                    .set(OrderUserCartEntity::getGoodsNum, goodsNum)) > 0;
                            if (IsSuccess) {
                                return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
                            } else {
                                return ResponseResult.fail(BizCodeEnume.OPERATION_SUCCESSFUL);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.info("addGoodsNumErrorResults:" + e.getMessage());
                return ResponseResult.fail(e.getMessage());
            }
        } else {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);

        }

    }

    @Override
    public void deleteUserCat(String shopId) {
        this.baseMapper.deleteUserCat(shopId);
    }

    /**
     * 添加商品到购物车
     *
     * @param userId 用户ID
     * @param goodsId 商品ID
     * @param goodsNum 商品数量
     * @param sellerShopId 商家店铺ID
     * @param shopGateId 门店ID，用于区分库存
     * @return 添加结果
     * @throws JsonProcessingException
     */
    private ResponseResult addShoppingCart(String userId, Integer goodsId, Integer goodsNum, String sellerShopId,Integer shopGateId) throws JsonProcessingException {
        // 获取当前时间，作为添加购物车的时间戳
        // 加入购物车时间
        Date insertCartTime = DateUtils.getNowDate();
        // 查询商品详情
        // 查询商品详情
        ResponseResult goodsDetailsResult = goodServiceClient.getGoodsDetails(goodsId);

        // 检查商品查询结果，如果不成功，则直接返回查询结果
        if (goodsDetailsResult.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return goodsDetailsResult;
        } else {
            // 将商品详情的JSON字符串转换为对象
            String goodsDetails = goodsDetailsResult.getData().toString();
            // json转对象
            ReceiveGoodsDetailsDTO receiveGoodsDetails = JsonUtils.string2Obj(goodsDetails,
                    ReceiveGoodsDetailsDTO.class);

            // 检查商品是否为虚拟商品，虚拟商品不能添加到购物车
            Integer virtualGoods = receiveGoodsDetails.getVirtualGoods();
            if (null != virtualGoods && 0 == virtualGoods) {
                return ResponseResult.fail(BizCodeEnume.VIRTUAL_PRODUCT_CANNOT_BE_ADDED_TO_SHOPPING_CART);
            }

            // 获取商品相关属性，用于后续的判断和操作
            Integer shopId = receiveGoodsDetails.getShopId();
            String shopName = receiveGoodsDetails.getShopName();
            Integer commonId = receiveGoodsDetails.getCommonId();
            String goodsName = receiveGoodsDetails.getGoodsName();
            String goodsImage = receiveGoodsDetails.getGoodsImage();
            BigDecimal goodsPrice = receiveGoodsDetails.getGoodsPrice();
            Integer goodsStock = receiveGoodsDetails.getGoodsStock();
            String goodsSpecChineseValue = receiveGoodsDetails.getGoodsSpec();

            // 如果有门店ID，需要查询门店的库存情况
            if(null!= shopGateId){
                //当门店id不为空需要 查询门店的商品库存
                Integer gateShopStock = this.baseMapper.selectpGateShopStock(shopGateId, goodsId);
                if(null == gateShopStock){
                    gateShopStock = 0;
                }
                goodsStock = gateShopStock;
            }


            // 检查是否不能购买自己的商品
            if (sellerShopId != null && sellerShopId.toString().equals(shopId.toString())) {
                return ResponseResult.fail(BizCodeEnume.DO_NOT_PURCHASE_YOUR_OWN_PRODUCTS);
            }

            // 检查购物车中是否已存在该商品，如果存在，则更新数量；如果不存在，则添加新商品到购物车
            // 判断购物车是否存在该商品(查询goodsId)
            OrderUserCartEntity userCartEntity = this.baseMapper.queryShoppingCartGoodsExist(userId, goodsId,shopGateId,CommonType.shoppingCartStatus.NORMAL.getCode());
            Integer goodsMaxSale = receiveGoodsDetails.getGoodsMaxSale();
            //购物车现有数量
            Integer cartGoodsNum = 0;
            if (userCartEntity != null && goodsId.equals(userCartEntity.getGoodsId())) {
                cartGoodsNum = userCartEntity.getGoodsNum();
                // 根据限购和库存情况，更新购物车中的商品数量
                //goodsMaxSale为0是不限购商品
                if (goodsMaxSale == 0) {
                    if (goodsNum > goodsStock || (goodsNum + cartGoodsNum) > goodsStock) {
                        return ResponseResult.fail(BizCodeEnume.TREASURES_IS_OUT_OF_RANGE);
                    } else {
                        userCartEntity.setGoodsNum(userCartEntity.getGoodsNum() + goodsNum);
                        userCartEntity.setInsertCartTime(insertCartTime);
                        // 增加商品数量
                        int flag = this.baseMapper.updateShoppingCartGoodsNum(userCartEntity,shopGateId);
                        Boolean isSuccess = flag > 0;
                        goodsNum = goodsNum + userCartEntity.getGoodsNum();
                        return cartChangNumRedis(userId, goodsNum, goodsId, isSuccess);
                    }
                } else {
                    if (goodsNum > goodsMaxSale || goodsNum > goodsStock || (cartGoodsNum + goodsNum) > goodsMaxSale || (cartGoodsNum + goodsNum) > goodsStock) {
                        return ResponseResult.fail(BizCodeEnume.TREASURES_IS_OUT_OF_RANGE);
                    } else {
                        userCartEntity.setGoodsNum(userCartEntity.getGoodsNum() + goodsNum);
                        userCartEntity.setInsertCartTime(insertCartTime);
                        // 增加商品数量
                        int flag = this.baseMapper.updateShoppingCartGoodsNum(userCartEntity,shopGateId);
                        Boolean isSuccess = flag > 0;
                        goodsNum = goodsNum + userCartEntity.getGoodsNum();
                        return cartChangNumRedis(userId, goodsNum, goodsId, isSuccess);
                    }
                }
            } else {
                // 根据限购和库存情况，添加新商品到购物车
                //不限购
                if (goodsMaxSale == 0) {
                    if (goodsNum > goodsStock) {
                        return ResponseResult.fail(BizCodeEnume.TREASURES_IS_OUT_OF_RANGE);
                    } else {
                        // 插入购物车
                        boolean isSuccess = insertUserCart(userId, shopId, shopName, commonId, goodsId, goodsName,
                                goodsImage, goodsNum, goodsPrice, goodsStock, goodsSpecChineseValue, insertCartTime,
                                receiveGoodsDetails.getGoodsType(),shopGateId);
                        return insertCartIsSuccess(userId, isSuccess);
                    }
                } else {
                    //限购
                    if (goodsNum > goodsMaxSale || goodsNum > goodsStock) {
                        return ResponseResult.fail(BizCodeEnume.TREASURES_IS_OUT_OF_RANGE);
                    } else {
                        // 插入购物车
                        boolean isSuccess = insertUserCart(userId, shopId, shopName, commonId, goodsId, goodsName, goodsImage, goodsNum, goodsPrice, goodsStock, goodsSpecChineseValue, insertCartTime,receiveGoodsDetails.getGoodsType(),shopGateId);
                        return insertCartIsSuccess(userId, isSuccess);
                    }
                }
            }
        }
    }


    /**
     * 改变购物车商品数量并更新缓存
     *
     * @param userId
     * @param goodsNum
     * @param queryGoodsId
     * @param isSuccess
     * @throws JsonProcessingException
     */
    private ResponseResult cartChangNumRedis(String userId, Integer goodsNum, Integer queryGoodsId, Boolean isSuccess) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        if (isSuccess) {
//            String jsonString = RedisUtil.get("unimall_cart_userId_" + userId);
//            if (jsonString != null) {
//                Integer length = 0;
//                Integer index = 0;
//                JavaType javaType = mapper.getTypeFactory().constructParametricType(ArrayList.class, EntityObjectsDTO.class);
//                List<EntityObjectsDTO> cartListByShopNameList = mapper.readValue(jsonString, javaType);
//                for (EntityObjectsDTO entityObjects : cartListByShopNameList) {
//                    List<UserShoppingCartDTO> goodsList = (List<UserShoppingCartDTO>) entityObjects.getGoodsDetailsList();
//                    //直接用上面的强制数据会报错，因为强制的时候转坏的
//                    //可以把list转成json
//                    String str = JSON.toJSONString(goodsList);
//                    //再把json转成list
//                    goodsList = JSONObject.parseArray(str, UserShoppingCartDTO.class);
//                    if (length == 1) {
//                        cartListByShopNameList.get(index).setGoodsDetailsList(goodsList);
//                        index++;
//                        continue;
//                    }
//                    for (UserShoppingCartDTO userShoppingCart : goodsList) {
//                        if (userShoppingCart.getGoodsId() != null && queryGoodsId.equals(userShoppingCart.getGoodsId())) {
//                            userShoppingCart.setGoodsNum(goodsNum);
//                        }
//                    }
//                    cartListByShopNameList.get(index).setGoodsDetailsList(goodsList);
//                    index++;
//                }
//                RedisUtil.set("unimall_cart_userId_" + userId, JsonUtils.toString(cartListByShopNameList), 60 * 60 * 24);
//            }
            return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL.getMsg());
        } else {
            return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
        }
    }

    /**
     * 根据用户ID和购物车添加结果，处理购物车数据的逻辑。
     * 如果添加成功，从Redis中移除相应的购物车数据；如果添加失败，返回失败结果。
     *
     * @param userId 用户ID，用于标识购物车数据的归属。
     * @param isSuccess 添加购物车是否成功，用于决定执行的操作。
     * @return 返回处理结果，成功时返回成功结果，失败时返回失败结果。
     */
    private ResponseResult insertCartIsSuccess(String userId, boolean isSuccess) {
        // 如果购物车添加成功
        if (isSuccess) {
            // 从Redis中移除用户的购物车数据
            RedisUtil.remove("unimall_cart_userId_" + userId);
            // 返回购物车添加成功的响应结果
            return ResponseResult.success(CommonStatusEnum.SUCCESS.getValue());

        } else {
            // 返回购物车添加失败的响应结果
            return ResponseResult.fail(CommonStatusEnum.FAIL.getValue());
        }
    }

    /**
     * 插入用户购物车信息。
     *
     * 该方法用于将用户选择的商品信息插入到购物车中。在用户添加商品到购物车时调用，
     * 它会创建一个新的OrderUserCartEntity实体，并设置所有必要的属性，然后调用baseMapper的insert方法将其插入数据库。
     *
     * @param userId 用户ID，用于关联购物车信息和用户。
     * @param shopId 商店ID，指示商品所属的商店。
     * @param shopName 商店名称，提供商品所属商店的名称。
     * @param commonId 商品通用ID，可能用于关联商品的详细信息。
     * @param goodsId 商品ID，唯一标识商品。
     * @param goodsName 商品名称，描述添加到购物车的商品。
     * @param goodsImage 商品图片，提供商品的可视化表示。
     * @param goodsNum 商品数量，指示添加到购物车的商品数量。
     * @param goodsPrice 商品价格，指示商品的单价。
     * @param goodsStock 商品库存，指示商品的当前库存数量。
     * @param goodsSpecChineseValue 商品规格中文名称，描述商品的具体规格。
     * @param insertCartTime 添加到购物车的时间，记录用户添加商品到购物车的时刻。
     * @param goodsType 商品类型，指示商品的分类或类型。
     * @param shopGateId 商店入口ID，可能用于标识用户进入商店的途径或渠道。
     * @return 插入操作的成功状态，返回true表示插入成功，false表示插入失败。
     */
    private Boolean insertUserCart(String userId, Integer shopId, String shopName, Integer commonId,
                                       Integer goodsId, String goodsName, String goodsImage, Integer goodsNum, BigDecimal goodsPrice,
                                       Integer goodsStock, String goodsSpecChineseValue, Date insertCartTime, Integer goodsType,Integer shopGateId) {

        // 创建一个新的OrderUserCartEntity实例，用于存储购物车信息。
        OrderUserCartEntity insertUserCartEntity = new OrderUserCartEntity();
        // 设置购物车信息的各项属性，包括用户ID、商店ID、商品ID等。
        insertUserCartEntity.setUserId(userId);
        insertUserCartEntity.setShopId(shopId);
        insertUserCartEntity.setShopName(shopName);
        insertUserCartEntity.setCommonId(commonId);
        insertUserCartEntity.setGoodsId(goodsId);
        insertUserCartEntity.setGoodsName(goodsName);
        insertUserCartEntity.setGoodsImage(goodsImage);
        insertUserCartEntity.setGoodsNum(goodsNum);
        insertUserCartEntity.setGoodsPrice(Double.parseDouble(goodsPrice.toString()));
        insertUserCartEntity.setGoodsStock(goodsStock);
        insertUserCartEntity.setGoodsSpecChineseValue(goodsSpecChineseValue);
        insertUserCartEntity.setInsertCartTime(insertCartTime);
        insertUserCartEntity.setCartStatus(CommonType.shoppingCartStatus.NORMAL.getCode());
        insertUserCartEntity.setGoodsType(goodsType);
        insertUserCartEntity.setShopGateId(shopGateId);

        // 调用baseMapper的insert方法，尝试插入新的购物车信息。
        int flag = this.baseMapper.insert(insertUserCartEntity);
        // 根据插入操作的结果判断是否成功，返回相应的布尔值。
        return flag == CommonStatusEnum.SUCCESS.getCode();

    }

    /**
     * 根据商品ID和活动状态处理活动信息，确定最大购买数量。
     *
     * @param token 用户令牌，用于权限验证。
     * @param goodsId 商品ID，用于查询商品信息。
     * @param activityStatus 活动状态，2表示秒杀活动，3表示限时折扣活动。
     * @param goodsMaxSale 商品单次最大销售数量。
     * @param goodsStock 商品库存数量。
     * @return 返回用户在当前活动中可以购买的最大数量。如果活动结束后或库存不足，返回0。
     */
    private Integer dealActivityInfo(String token, Integer goodsId, Integer activityStatus, Integer goodsMaxSale, Integer goodsStock) {
        // 初始化最大购买数量为0
        Integer toBuyMaxNum = 0;

        // 初始化请求参数
        Map<String, Object> param = new HashMap<>();

        // 根据活动状态处理不同的活动
        if (2 == activityStatus) {
            // 秒杀活动处理
            param.put("token", token);
            param.put("goodsId", goodsId);
            // 默认：不限购（code=1 + data=null），由库存兜底，保证基础下单不被阻断
            Integer code = 1;
            Object data = null;
            try {
                ResponseResult resp = activityServiceClient.seckillCheckLimit(param);
                if (resp != null) {
                    code = resp.getCode();
                    data = resp.getData();
                }
            } catch (Exception e) {
                // 促销为可选商业插件：未安装或服务不可用时按「不限购」放行（最终版目录架构 §3.2）
                log.warn("秒杀限购校验不可用（促销插件未安装或服务异常），按不限购放行，受库存约束: {}", e.getMessage());
            }

            // 根据返回的代码和数据，确定最大购买数量
            //code 1 data null 不限购
            if (1 == code && data == null) {
                // 未限制购买数量，返回商品库存数量
                toBuyMaxNum = goodsStock;
                //code 2 data count 已达购买上限 data null 活动结束
            } else if (2 == code) {
                // 活动已结束或购买数量已达到上限，返回0
                toBuyMaxNum = 0;
                //code 1 data count 未达上限，可购买
            } else if (1 == code && null != data) {
                // 返回剩余可购买数量
                toBuyMaxNum = goodsMaxSale - (Integer) data;
            }
        } else if (3 == activityStatus) {
            // 限时折扣活动处理
            param.put("token", token);
            param.put("goodsId", goodsId);
            // 默认：不限购（code=1 + data=null），由库存兜底，保证基础下单不被阻断
            Integer code = 1;
            Object data = null;
            try {
                ResponseResult resp = activityServiceClient.discountCheckLimit(param);
                if (resp != null) {
                    code = resp.getCode();
                    data = resp.getData();
                }
            } catch (Exception e) {
                // 促销为可选商业插件：未安装或服务不可用时按「不限购」放行（最终版目录架构 §3.2）
                log.warn("限时折扣限购校验不可用（促销插件未安装或服务异常），按不限购放行，受库存约束: {}", e.getMessage());
            }

            // 根据返回的代码和数据，确定最大购买数量
            //code 1 data null 不限购
            if (1 == code && data == null) {
                // 未限制购买数量，返回商品库存数量
                toBuyMaxNum = goodsStock;
                //code 2 data count 已达购买上限 data null 活动结束
            } else if (2 == code) {
                // 活动已结束或购买数量已达到上限，返回0
                toBuyMaxNum = 0;
                //code 1 data count 未达上限，可购买
            } else if (1 == code && null != data) {
                // 返回剩余可购买数量
                toBuyMaxNum = goodsMaxSale - (Integer) data;
            }
        }

        // 返回最大购买数量
        return toBuyMaxNum;
    }
}

