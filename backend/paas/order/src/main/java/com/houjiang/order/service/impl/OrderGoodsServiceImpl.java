package com.houjiang.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.base.BaseExpressDTO;
import com.houjiang.commons.dto.goods.DiscountGoodsListVO;
import com.houjiang.commons.dto.order.OrderGoodsBarterDTO;
import com.houjiang.commons.dto.vop.JingDongReturnGoodsDTO;
import com.houjiang.commons.dto.vop.JingDongReturnOrderDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.json.JsonUtils;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.kuaidiniao.KdniaoTrackQueryAPI;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.activity.AppSeckillGoodsQueryResultVO;
import com.houjiang.commons.vo.goods.BaseDetailVO;
import com.houjiang.commons.vo.goods.DiscountGoodsInfoVO;
import com.houjiang.commons.vo.goods.GoodsDetailsListVO;
import com.houjiang.commons.vo.goods.SeckillGoodsInfoVO;
import com.houjiang.commons.vo.job.AcquiescenceVO;
import com.houjiang.commons.vo.order.OrderGoodsBarterVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.order.dto.QueryLogisticsMessageDTO;
import com.houjiang.order.entity.OrderBaseEntity;
import com.houjiang.order.entity.OrderGoodsEntity;
import com.houjiang.order.feign.BaseServiceClient;
import com.houjiang.order.feign.GoodServiceClient;
import com.houjiang.order.mapper.OrderBaseMapper;
import com.houjiang.order.mapper.OrderGoodsMapper;
import com.houjiang.order.mapper.OrderShipmentMapper;
import com.houjiang.order.service.OrderGoodsService;
import com.houjiang.order.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName OrderGoodsServiceImpl
 * @Description
 * @Author Amos
 * @Date 2023/6/21 10:40
 * @Version 1.0
 * @author Amos
 */
@Service
public class OrderGoodsServiceImpl extends ServiceImpl<OrderGoodsMapper, OrderGoodsEntity>
        implements OrderGoodsService {

    @Autowired
    private BaseServiceClient baseServiceClient;

    @Autowired
    private OrderShipmentMapper orderShipmentMapper;

    @Autowired
    private OrderBaseMapper orderBaseMapper;

    @Autowired
    private GoodServiceClient goodServiceClient;

    private final Logger logger = LoggerFactory.getLogger(OrderGoodsServiceImpl.class);

    /**
     * 根据快递单号，快递公司id，收货人 查询物流信息
     */
    @Override
    public ResponseResult queryLogisticsByCode(QueryLogisticsMessageDTO dto) {
        LogisticsMessageVO shippinginfo = new LogisticsMessageVO();
        shippinginfo.setOrderId(dto.getOrderId());
        shippinginfo.setOrderShippingCode(dto.getOrderShippingCode());
        shippinginfo.setOrderShippingExpressId(dto.getOrderShippingExpressId());
        // 物流信息
        String logisticCode = shippinginfo.getOrderShippingCode();
        logger.info("---------开始查询运单号为{}的物流信息", logisticCode);
        Map<String, String> resultMap = getShipperCode(shippinginfo, dto.getOrderShippingMobile());
        //获取快递鸟的配置信息
        KdniaoTrackQueryAPI.AppKey = resultMap.get("AppKey");
        KdniaoTrackQueryAPI.EBusinessID = resultMap.get("EBussinessID");
        KdniaoTrackQueryAPI.instructCode = String.valueOf(resultMap.get("instructCode"));
        //获取快递的简称
        String shipperCode = resultMap.get("shipperCode");
        // 创建一个HashMap用于存储请求数据
        Map<String, Object> requestDataMap = new HashMap<>();
        // 将物流公司的编码放入请求数据中
        requestDataMap.put("ShipperCode", shipperCode);
        // 将物流单号放入请求数据中
        requestDataMap.put("LogisticCode", logisticCode);

        // 如果是顺丰快递，则需要提供客户名称,一般用手机号后四位表示
        if (logisticCode.startsWith("SF")) {
            String CustomerName = resultMap.get("CustomerName");
            requestDataMap.put("CustomerName", CustomerName);
        }
        // 调用快递鸟的接口获取物流信息（地图版）
        String returnStr = null;
        try {
            returnStr = KdniaoTrackQueryAPI.getOrderTracesAndMap(requestDataMap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        logger.info("---------查询运单号为{}的物流信息结束,物流信息：{}", logisticCode, returnStr);
        Map<String, Object> shippingMap = new HashMap<>();
        shippingMap.put("shippingInfo", returnStr);
        shippingMap.put("shippingCode", logisticCode);
        shippingMap.put("expressName", resultMap.get("expressName"));
        return ResponseResult.success(shippingMap);
    }


    /**
     * 根据订单号查询物流信息。
     *
     * @param map 包含订单号等信息的参数映射。
     * @return 查询结果的响应对象。
     */
    @Override
    public ResponseResult queryLogistics(Map<Object, Object> map) {
        // 从参数映射中获取订单号
        String no = (String) map.get("no");
        // 如果订单号为空，则返回失败响应
        if (StringUtils.isEmpty(no)) {
            return ResponseResult.fail(BizCodeEnume.ORDER_NUMBER_IS_EMPTY);
        }

        // 根据订单号查询物流消息列表
        // 查询订单信息
        List<LogisticsMessageVO> shippingList = orderShipmentMapper.queryLogisticsMessage(no);
        // 初始化存储物流信息的列表
        // 物流信息
        List<Map<String, Object>> returnStrs = new ArrayList<>();
        // 如果物流消息列表不为空，则遍历处理每个物流消息
        if (null != shippingList && !shippingList.isEmpty()) {
            for (LogisticsMessageVO shippinginfo : shippingList) {
                // 解析商品ID列表和数量列表
                List<Integer> goodsIds = JSON.parseArray(shippinginfo.getGoodsIds(), Integer.class);
                List<Integer> goodsNums = JSON.parseArray(shippinginfo.getGoodsNums(), Integer.class);
                // 根据商品ID列表和订单号查询商品信息
                List<ShippingGoodsMessageVO> sgm = orderBaseMapper.queryGoodsMessage(goodsIds, no,null);
                // 将商品数量匹配到对应的商品信息中
                for (int i = 0; i < sgm.size(); i++) {
                    sgm.get(i).setShipmentNum(goodsNums.get(i));
                }
                // 初始化单个物流信息的映射
                Map<String, Object> shippingMap = new HashMap<>();
                String shippingMessage = shippinginfo.getOrderShippingMessage();
                // 自主配送 不查询快递鸟
                if(CommonType.shipmentType.LOGISTICS_DELIVERY.getCode().equals(shippinginfo.getShipmentType())) {
                    String logisticCode = shippinginfo.getOrderShippingCode();
                    // 查询物流公司的ID对应的物流名称
                    Integer expId = shippinginfo.getOrderShippingExpressId();
                    // 查询物流名称
                    ResponseResult express = baseServiceClient.getBaseExpressInfoById(expId);
                    String expName = JSON.parseObject(JSON.toJSONString(express.getData()), BaseExpressDTO.class).getExpressName();
                    shippinginfo.setShippingName(expName);
                    // 获取物流公司的编码、消息等信息，并封装到物流映射中
                    String returnStr = null;
                    try {
                        String shippingCode = shippinginfo.getOrderShippingCode();
                        logger.info("---------开始查询运单号为{}的物流信息", shippingCode);
                        Map<String, String> resultMap = getShipperCode(shippinginfo, "");
                        //获取快递鸟的配置信息
                        KdniaoTrackQueryAPI.AppKey = resultMap.get("AppKey");
                        KdniaoTrackQueryAPI.EBusinessID = resultMap.get("EBussinessID");
                        KdniaoTrackQueryAPI.instructCode = String.valueOf(resultMap.get("instructCode"));
                        //获取快递的简称
                        String shipperCode = resultMap.get("shipperCode");
                        // 创建一个HashMap用于存储请求数据
                        Map<String, Object> requestDataMap = new HashMap<>();
                        // 将物流公司的编码放入请求数据中
                        requestDataMap.put("ShipperCode", shipperCode);
                        // 将物流单号放入请求数据中
                        requestDataMap.put("LogisticCode", logisticCode);
                        // 收货详细地址
                        requestDataMap.put("Receiver", resultMap.get("receiver"));
                        // 发货地址
                        requestDataMap.put("Sender", shippinginfo.getSenderAddress());
                        System.out.println("shippinginfo.getSenderAddress()" + shippinginfo.toString());
                        // 如果是顺丰快递，则需要提供客户名称,一般用手机号后四位表示
                        if (logisticCode.startsWith("SF")) {
                            String CustomerName = resultMap.get("CustomerName");
                            requestDataMap.put("CustomerName", CustomerName);
                        }
                        // 根据物流公司编码查询物流轨迹信息
                        returnStr = KdniaoTrackQueryAPI.getOrderTracesAndMap(requestDataMap);
                        logger.info("---------查询运单号为{}的物流信息结束,物流信息：{}", shippingCode, returnStr);
                        // 将物流信息填充到映射中
                        shippingMap.put("shippingInfo", returnStr);
                        shippingMap.put("shippingCode", shippingCode);
                        shippingMap.put("shippingMessage", shippingMessage);
                        shippingMap.put("expressName", resultMap.get("expressName"));
                    } catch (Exception e) {
                        // 如果查询物流信息过程中出现异常，则返回失败响应
                        logger.info("queryLogisticsErrorResults:" + e.getMessage());
                        return ResponseResult.fail(e.getMessage());
                    }
                }else{
                    shippingMap.put("shippingMessage", shippingMessage);
                    shippingMap.put("deliveryPerson",shippinginfo.getDeliveryPerson());
                    shippingMap.put("deliveryTelephone",shippinginfo.getDeliveryTelephone());
                    shippingMap.put("deliveryCertificate",shippinginfo.getDeliveryCertificate());
                }
                shippingMap.put("shipmentType",shippinginfo.getShipmentType());
                shippingMap.put("orderShippingTime", shippinginfo.getOrderShippingTime());
                shippingMap.put("goodsList", sgm);
                returnStrs.add(shippingMap);
            }
        }
        // 返回查询成功的响应，包含所有物流信息
        return ResponseResult.success(returnStrs);
    }

    /**
     * 查询退货商品信息
     *
     * @param map 查询参数的映射表，可能包含分页信息和商品相关的查询条件。
     * @return 返回查询结果，包装在ResponseResult对象中，其中包含分页信息。
     */
    @Override
    public ResponseResult queryReturnGoods(Map<String, Object> map) {
        // 根据查询条件和分页信息，查询商品列表
        IPage<GoodsDetailsListVO> page = this.baseMapper.queryGoodsList2(
                new Query<GoodsDetailsListVO>().getPage(map), map);

        // 将查询结果包装在PageUtils中，进一步包装在ResponseResult中返回
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 根据订单ID分页查询商品详情，并获取赠品信息。
     *
     * @param map 包含订单ID和其他查询参数的映射。
     * @return 返回查询结果，包括商品详情和赠品信息。
     */
    @Override
    public ResponseResult selectByPage(Map<String, Object> map) {
        // 初始化返回的数据映射
        HashMap<String, Object> returnMap = new HashMap<>();
        // 从传入的map中提取订单ID
        String orderId = String.valueOf(map.get("orderId"));

        // 根据订单ID查询商品列表，分页信息来自map
        IPage<GoodsDetailsListVO> page=this.baseMapper.queryGoodsListByPage(new Query<GoodsDetailsListVO>().getPage(map),
                map);

        // 查询该订单的赠品信息
        //如果订单包含赠品 查询
        String giveawayInfo = this.baseMapper.queryOrderZengpinInfo(orderId);

        // 初始化赠品列表
        List<ManJianZengPinVO> list = new ArrayList<>();
        // 如果赠品信息不为空，则将其转换为对象列表
        if(null != giveawayInfo && !"".equals(giveawayInfo)) {
            list = JsonUtils.string2Obj(giveawayInfo, List.class, ManJianZengPinVO.class);
        }

        // 查询该订单是否有过改价记录
        //查询当前订单是否改价 如果 改价必须全单退
        Integer orderNum = this.baseMapper.queryOrderUpdatePrice(map);
        // 根据查询结果，标记订单是否改过价
        if(orderNum == 0 ){
            returnMap.put("updatePrice",false);
        }else{
            returnMap.put("updatePrice",true);
        }

        // 将分页处理后的商品详情和赠品信息添加到返回的映射中
        returnMap.put("orderGoods",PageUtils.getPage(page));
        returnMap.put("giveawayInfo",list);

        // 返回查询结果
        return ResponseResult.success(returnMap);
    }

    /**
     * 根据传入的条件查询商品销售排名信息。
     *
     * @param map 包含查询条件和令牌信息的Map对象。
     * @return 返回查询结果的ResponseResult对象。
     */
    @Override
    public ResponseResult querySalesRanking(Map<String, Object> map) {
        // 从map中获取token
        String token = (String) map.get("token");
        // 根据token解析用户信息
        UserInfoVO info = ParseTokenUtils.getPlatformUser(token);
        // 检查用户信息是否有效
        if (info != null) {
            try {
                // 从map中获取查询类型
                Integer type = (Integer) map.get("type");
                List<GoodsParaVO> goodsList = null;
                // 根据查询类型查询相应的商品排名信息
                if (type == 1) {
                    goodsList = this.baseMapper.queryAllGoods();
                } else if (type == 2) {
                    goodsList = this.baseMapper.queryTodayGoods();
                } else if (type == 3) {
                    goodsList = this.baseMapper.queryWeekGoods();
                } else if (type == 4) {
                    goodsList = this.baseMapper.queryMonthGoods();
                } else {
                    goodsList = this.baseMapper.queryYearGoods();
                }
                // 返回查询结果
                return ResponseResult.success(goodsList);
            } catch (Exception e) {
                // 查询过程中发生异常，返回系统错误信息
                return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(), BizCodeEnume.SYSTEM_ERROR.getMsg());
            }
        } else {
            // 用户信息无效，返回令牌无效信息
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
    }

    /**
     * 根据订单号查询商品信息。
     *
     * @param orderId 订单号。
     * @return 返回查询到的商品信息列表。
     */
    @Override
    public List<AcquiescenceVO> queryGoodsInfoByjob(String orderId) {
        // 根据订单号查询订单商品信息
        List<OrderGoodsEntity> list = this.baseMapper.selectList(new LambdaQueryWrapper<OrderGoodsEntity>()
                .eq(OrderGoodsEntity::getOrderId, orderId));
        List<AcquiescenceVO> voList = new ArrayList<>();
        // 如果查询结果不为空
        if (!CollectionUtils.isEmpty(list)) {
            for (OrderGoodsEntity entity : list) {
                AcquiescenceVO vo = new AcquiescenceVO();
                // 将商品信息填充到VO对象中
                vo.setGoodsId(entity.getGoodsId());
                vo.setCommonId(entity.getCommonId());
                vo.setGoodsName(entity.getGoodsName());
                vo.setGoodsSpec(entity.getOrderSpecChineseValue());
                voList.add(vo);
            }
        }
        return voList;
    }

    /**
     * 根据订单号查询订单的发货商品信息。
     *
     * @param param 包含token和orderId的参数映射表。
     * @return 返回查询到的发货商品详情列表。
     */
    @Override
    public ResponseResult queryOrderShipmentGoods(Map<String, Object> param) {
        // 从参数映射表中提取token
        String token = (String) param.get("token");
        // 使用token解析出用户信息
        UserInfoVO info = ParseTokenUtils.getPlatformUser(token);

        // 根据订单号和店铺ID查询订单的商品详情列表
        List<GoodsDetailsVO> list = this.baseMapper.queryOrders(param.get("orderId").toString(),Integer.valueOf(info.getShopId()));

        // 返回查询结果，如果成功，则将商品详情列表作为数据部分返回
        return ResponseResult.success(list);
    }

    /**
     * 根据物流消息体获取运输商代码及相关信息。
     *
     * @param shippinginfo 物流消息体，包含订单物流相关信息。
     * @return 返回一个Map，包含运输商代码、运输商名称、快递鸟配置信息及收件人后四位姓名。
     *         这些信息用于后续的物流查询和处理。
     */
    public Map<String, String> getShipperCode(LogisticsMessageVO shippinginfo, String userMobile) {
        // 初始化一个Map用于存储运输商代码和相关配置信息
        Map map = new HashMap();

        // 获取订单物流信息中的运输商ID
        // 获取快递简称
        Integer orderShippingExpressId = shippinginfo.getOrderShippingExpressId();
        // 根据运输商ID调用基础服务客户端获取运输商详细信息
        ResponseResult shipperResp = baseServiceClient.getBaseExpressInfoById(orderShippingExpressId);
        // 解析运输商信息中的拼音和名称
        Map<String, String> shipperCode = JSONObject.parseObject(JSON.toJSONString(shipperResp.getData()), Map.class);

        // 将运输商的拼音和名称存入map中
        map.put("shipperCode", shipperCode.get("expressPinyin"));
        map.put("expressName", shipperCode.get("expressName"));

        // 获取快递鸟的配置信息
        // 获取快递鸟的配置
        Map<String, String> expressQueryByKdniao = (Map<String, String>) baseServiceClient.queryConfigFeign().getData();
        // 将快递鸟的配置信息存入map中
        map.put("AppKey", expressQueryByKdniao.get("appkey"));
        map.put("EBussinessID", expressQueryByKdniao.get("ebussinessid"));
        map.put("instructCode", expressQueryByKdniao.get("instructCode"));

        // 根据订单ID查询订单基本信息
        // 获取收件人手机号的后4位
        String orderReceiverContact;
        if (!StringUtils.isEmpty(userMobile)) {
            orderReceiverContact = userMobile;
        }else {
            OrderBaseEntity orderBase = orderBaseMapper.selectOne(new LambdaQueryWrapper<OrderBaseEntity>()
                    .eq(OrderBaseEntity::getOrderId, shippinginfo.getOrderId()));
            // 提取收件人姓名的后四位作为客户名称
            orderReceiverContact = orderBase.getOrderReceiverContact();
            String receiver = orderBase.getOrderReceiverAddress();
            map.put("receiver", receiver);
        }
        String customerName = orderReceiverContact.substring(orderReceiverContact.length() - 4);
        // 将客户名称存入map中
        map.put("customerName", customerName);

        // 返回包含运输商代码和配置信息的map
        return map;
    }

    /**
     * 查询供应商订单发货商品
     *
     * @param param
     * @return
     */
    @Override
    public ResponseResult querySupplierOrderShipmentGoods(Map<String, Object> param) {
        // 从map中获取订单ID字符串
        List<GoodsOrderDetailsVO> list = this.baseMapper.querySupplierOrderShipmentGoods(param.get("orderId").toString());
        // 返回查询结果
        return ResponseResult.success(list);
    }

    /**
     * 通过Feign客户端查询订单商品数量
     * 主要用于远程调用，可能涉及到与其他微服务的交互
     *
     * @param param 查询参数，包含可能影响查询结果的各种条件
     * @return 返回查询到的订单商品数量
     */
    @Override
    public Integer queryOrderGoodsCountFeign(Map<String, Object> param) {
        Integer count = this.baseMapper.queryOrderGoodsCountFeign(param);
        return count;
    }

    /**
     * 查询订单商品数量
     * 主要用于本地查询，不涉及远程调用
     *
     * @param param 查询参数，包含可能影响查询结果的各种条件
     * @return 返回查询到的订单商品数量
     */
    @Override
    public Integer queryOrderGoodsCount(Map<String, Object> param) {
        Integer count = this.baseMapper.queryOrderGoodsCount(param);
        return count;
    }

    /**
     * 查询折扣商品信息
     * 该方法可能涉及到复杂的商品折扣计算逻辑
     *
     * @param discountGoodsListResult 折扣商品列表结果对象，包含查询条件和结果容器
     * @return 返回填充了折扣商品信息的结果对象
     */
    @Override
    public DiscountGoodsInfoVO queryDiscountGoodsInfo2(DiscountGoodsListVO discountGoodsListResult) {
        DiscountGoodsInfoVO discountGoodsInfoVO = this.baseMapper.queryDiscountGoodsInfo2(discountGoodsListResult);
        return discountGoodsInfoVO;
    }

    /**
     * 查询购买了折扣商品的用户列表
     * 该方法主要用于营销活动，识别谁购买了特定的折扣商品
     *
     * @param discountGoodsListResult 折扣商品列表结果对象，包含查询条件
     * @return 返回购买了折扣商品的用户ID列表
     */
    @Override
    public List<String> queryBuyerUsers(DiscountGoodsListVO discountGoodsListResult) {
        List<String> list = this.baseMapper.queryBuyerUsers(discountGoodsListResult);
        return list;
    }

    /**
     * 查询满足特定条件的订单数量
     * 该方法可能用于统计分析，特别是针对较旧订单的分析
     *
     * @param paramMap 查询参数映射，包含可能影响查询结果的条件
     * @return 返回满足条件的订单数量
     */
    @Override
    public Integer queryCountForOlder(Map<String, Object> paramMap) {
        Integer count = this.baseMapper.queryCountForOlder(paramMap);
        return count;
    }

    /**
     * 查询用户订单数量
     * 该方法主要用于用户行为分析或者用户个性化推荐
     *
     * @param paramMap 查询参数映射，包含用户相关的查询条件
     * @return 返回用户订单的数量
     */
    @Override
    public Integer queryUserOrderCount(Map<String, Object> paramMap) {
        Integer count = this.baseMapper.queryUserOrderCount(paramMap);
        return count;
    }

    /**
     * 查询秒杀商品信息
     * 该方法可能涉及到秒杀活动的特殊逻辑处理
     *
     * @param appSeckillGoodsResult 秒杀商品查询结果对象，包含查询条件和结果容器
     * @return 返回填充了秒杀商品信息的结果对象
     */
    @Override
    public SeckillGoodsInfoVO querySeckillGoodsInfo2(AppSeckillGoodsQueryResultVO appSeckillGoodsResult) {
        SeckillGoodsInfoVO seckillGoodsInfoVO =  this.baseMapper.querySeckillGoodsInfo2(appSeckillGoodsResult);
        return seckillGoodsInfoVO;
    }

    /**
     * 查询订单中的换购商品信息
     * 本方法通过订单ID和商品ID查询特定商品的换购信息，并调用商品服务进行进一步处理
     *
     * @param dto OrderGoodsBarterDTO对象，包含订单ID和商品ID，用于查询特定商品的换购信息
     * @return 返回包含换购商品信息的ResponseResult对象
     */
    @Override
    public ResponseResult<OrderGoodsBarterVO> queryOrderBarterGoods(OrderGoodsBarterDTO dto) {
        // 根据订单ID和商品ID查询商品的换购相关信息
        OrderGoodsEntity entity = baseMapper.selectOne(new LambdaQueryWrapper<OrderGoodsEntity>()
                .select(OrderGoodsEntity::getGoodsId,OrderGoodsEntity::getCommonId,
                        OrderGoodsEntity::getGoodsPrice,OrderGoodsEntity::getVerId)
                .eq(OrderGoodsEntity::getOrderId,dto.getOrderId())
                .eq(OrderGoodsEntity::getGoodsId,dto.getGoodsId()));

        // 将查询到的商品价格和通用ID设置到DTO中，以便后续调用商品服务
        dto.setGoodsPrice(BigDecimal.valueOf(entity.getGoodsPrice()));
        dto.setVerId(entity.getVerId());
        dto.setCommonId(entity.getCommonId());

        // 调用商品服务查询换购商品信息
        ResponseResult<OrderGoodsBarterVO> voResult = goodServiceClient.queryGoodsToBarter(dto);
        OrderGoodsBarterVO vo = voResult.getData();
        vo.setGoodsTaxPrice(BigDecimal.valueOf(entity.getGoodsPrice()));
        vo.setGoodsWeiPrice(BigDecimal.valueOf(entity.getGoodsPrice()));
        return ResponseResult.success(vo);
    }

    /**
     * 通过规格值查询订单换货商品规格信息
     *
     * @param dto 包含订单商品以物易物查询条件的数据传输对象
     * @return 返回查询结果的响应对象
     */
    @Override
    public ResponseResult<BaseDetailVO> queryOrderBarterGoodsSpec(OrderGoodsBarterDTO dto) {
        // 根据订单ID和商品ID查询商品的换购相关信息
        OrderGoodsEntity entity = baseMapper.selectOne(new LambdaQueryWrapper<OrderGoodsEntity>()
                .select(OrderGoodsEntity::getGoodsId,OrderGoodsEntity::getCommonId,
                        OrderGoodsEntity::getGoodsPrice,OrderGoodsEntity::getVerId)
                .eq(OrderGoodsEntity::getOrderId,dto.getOrderId())
                .eq(OrderGoodsEntity::getGoodsId,dto.getGoodsId()));

        // 将查询到的商品价格和通用ID设置到DTO中，以便后续调用商品服务
        dto.setGoodsPrice(BigDecimal.valueOf(entity.getGoodsPrice()));
        dto.setVerId(entity.getVerId());
        dto.setCommonId(entity.getCommonId());

        // 调用商品服务查询换购商品信息
        return goodServiceClient.queryGoodsSpecToBarter(dto);
    }

    /**
     * 将京东售后申请号存入库
     * @param jdOrderId
     * @param jdReturenOrderId
     * @return
     */
    @Override
    public ResponseResult updateJDReturnId(String jdOrderId, String jdReturenOrderId) {
        this.baseMapper.update(null,new LambdaUpdateWrapper<OrderGoodsEntity>()
                .set(OrderGoodsEntity::getJdReturenOrderId,jdReturenOrderId)
                .eq(OrderGoodsEntity::getJdOrderId,jdOrderId))
        ;
        return ResponseResult.success();
    }

    /**
     * 批量查询订单下商品售后权益
     * @param jingDongReturnOrderDTO
     * @return
     */
    @Override
    public ResponseResult queryJdWarrantyRights(JingDongReturnOrderDTO jingDongReturnOrderDTO) {
        // 根据商城订单号 查询京东订单号 及 京东商品skuId
        List<JingDongReturnGoodsDTO> jingDongReturnGoodsDTOS =  this.baseMapper.queryJdOrderGoodsInfo(jingDongReturnOrderDTO);
        ResponseResult responseResult = goodServiceClient.queryJdWarrantyRights(jingDongReturnGoodsDTOS);
        return responseResult;
    }
}
