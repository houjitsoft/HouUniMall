package com.houjiang.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.constant.OrderConstant;
import com.houjiang.commons.dto.order.ConsumeRecordByBusinessDTO;
import com.houjiang.commons.dto.order.ConsumeRecordParaDTO;
import com.houjiang.commons.dto.order.ConsumeTradeParaDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.exception.DefinedException;
import com.houjiang.commons.util.*;
import com.houjiang.commons.vo.activity.VoucherBaseVO;
import com.houjiang.commons.vo.base.DealerApplyInfoVO;
import com.houjiang.commons.vo.goods.WapGoodsSaleSumUpdateVO;
import com.houjiang.commons.vo.order.*;
import com.houjiang.commons.vo.shop.ShippingListVO;
import com.houjiang.commons.vo.shop.ShopMemberVO;
import com.houjiang.commons.vo.shop.ShopParaVO;
import com.houjiang.commons.vo.shop.ShopUserMessageResultVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.order.feign.*;
import com.houjiang.order.mapper.OrderBaseMapper;
import com.houjiang.order.mapper.OrderGoodsMapper;
import com.houjiang.order.mapper.OrderInvoiceInfoMapper;
import com.houjiang.order.mapper.OrderSupplierCreateMapper;
import com.houjiang.order.service.OrderSupplierCreateServer;
import com.houjiang.order.vo.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName OrderSupplierCreateServerImpl
 * @Description 供应商 创建订单 计算价格
 * @Author Amos
 * @Date 2023/7/14 15:24
 * @Version 1.0
 * @author Amos
 */
@Slf4j
@Service
public class OrderSupplierCreateServerImpl implements OrderSupplierCreateServer {

    @Resource
    private OrderSupplierCreateMapper orderSupplierCreateMapper;

    @Resource
    private OrderInvoiceInfoMapper orderInvoiceInfoMapper;

    @Resource
    private OrderGoodsMapper orderGoodsMapper;

    @Resource
    private OrderBaseMapper orderBaseMapper;

    @Autowired
    private PayMentServiceClient payMentServiceClient;

    @Autowired
    private GoodServiceClient goodServiceClient;

    @Autowired
    private MerchantServiceClient merchantServiceClient;

    @Autowired
    private ActivityServiceClient activityServiceClient;

    @Autowired
    private SupplierClient supplierClient;

    private final static Logger log = LoggerFactory.getLogger(OrderSupplierCreateServerImpl.class);

    @Override
    public ResponseResult calculatePrice(String message) {
        // 1.校验计算运费
        CommonParameterVO commonParam = JSON.parseObject(message, CommonParameterVO.class);
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(commonParam.getToken());
        ResponseResult resultResp = goodsShipping(message);
        if (resultResp.getCode() != (CommonStatusEnum.SUCCESS.getCode())) {
            return resultResp;
        }
        // 2.获取商品
        String goodsDetails = resultResp.getData().toString();
        // json转对象
        List<WapShopShippingQueryResultVO> wapList = JSON.parseObject(goodsDetails, new TypeReference<>() {
        });
        // 3.计算订单价格
        SupplyOrderPriceVO supplyOrderPrice = new SupplyOrderPriceVO();
        // 3.1 获取所有供应商店铺的店铺折扣
        Map<Integer, DealerApplyInfoVO> dealerApplyInfoMap = new HashMap<>(8);
        List<Integer> supplyShopIdList = wapList.stream().map(WapShopShippingQueryResultVO::getShopId).collect(Collectors.toList());
        DealerShopInfoVO dealerShopInfo = new DealerShopInfoVO(userInfo.getShopId(), supplyShopIdList);
        List<DealerApplyInfoVO> dealerApplyInfoList = orderSupplierCreateMapper.queryDealerApplyListByShop(dealerShopInfo.getDealerShopId(), dealerShopInfo.getSupplyShopIdList());
        if (null != dealerApplyInfoList && !dealerApplyInfoList.isEmpty()) {
            dealerApplyInfoMap = dealerApplyInfoList.stream().collect(Collectors.toMap(DealerApplyInfoVO::getSupplierShopId, Function.identity()));
        }
        // 店铺发票信息
        List<StoreInformationVO> storeInformationList = commonParam.getStoreInformationList();
        Map<Integer, StoreInformationVO> storeInformationMap = storeInformationList.stream().collect(Collectors.toMap(StoreInformationVO::getShopId, Function.identity()));

        List<ShippingListVO> shippingList = new ArrayList<>();
        ShopParaVO sp = new ShopParaVO();
        // 根据店铺循环
        for (WapShopShippingQueryResultVO shopShippingQueryResult : wapList) {
            // 当前店铺优惠比例
            BigDecimal discount = BigDecimal.ZERO;
            if (null != dealerApplyInfoMap.get(shopShippingQueryResult.getShopId())) {
                discount = new BigDecimal(dealerApplyInfoMap.get(shopShippingQueryResult.getShopId()).getDealerDiscount()).divide(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_UP);
            }
            List<WapShippingQueryResultVO> goodsBaseList = new ArrayList<>();

            // 存放shopId&&每家店铺运费
            ShippingListVO sl = new ShippingListVO();
            sl.setShopId(shopShippingQueryResult.getShopId());
            sl.setShopName(shopShippingQueryResult.getShopName());
            sl.setInvoiceId(storeInformationMap.get(shopShippingQueryResult.getShopId()).getInvoiceId());
            sl.setBuyerMessage(shopShippingQueryResult.getBuyerMessage());
            sl.setDistrictId(shopShippingQueryResult.getDistrictId());
            sl.setShopDistributorId(shopShippingQueryResult.getShopId());
            sl.setShopSelfSupport(shopShippingQueryResult.getShopSelfSupport());
            // 店铺默认发货时长 72 小时
            BigDecimal receivingTime = new BigDecimal(72);
            sl.setReceivingTime(receivingTime);

            // 每家店铺总价格(不含运费与优惠价格)
            BigDecimal shopGoodsTotalPrice = new BigDecimal(0);
            // 店铺总折扣
            BigDecimal shopGoodsTotalDiscount = BigDecimal.ZERO;
            // 商品数量
            Integer goodsNo = 0;
            // 获取商品计算商品价格
            for (WapShopShippingResultVO shopShippingResult : shopShippingQueryResult.getGoodsBaseList()) {
                List<WapShippingQueryResultVO> wsqrList = shopShippingResult.getGoodsBaseList();
                for (WapShippingQueryResultVO shippingQueryResult : wsqrList) {
                    // 限购校验
                    resultResp = goodsSaleCheck(shippingQueryResult, userInfo);
                    // 付款价格(不包含运费)
                    BigDecimal price = shippingQueryResult.getGoodsPrice().multiply(new BigDecimal(shippingQueryResult.getGoodsNum()));
                    shippingQueryResult.setGoodsPaymentPrice(price);
                    // 商品优惠金额
                    shippingQueryResult.setGoodsDisountPrice(price.multiply(discount).setScale(2, BigDecimal.ROUND_HALF_UP));
                    goodsNo = goodsNo + shippingQueryResult.getGoodsNum();
                    shopGoodsTotalPrice = shopGoodsTotalPrice.add(price);
                    shopGoodsTotalDiscount = shopGoodsTotalDiscount.add(shippingQueryResult.getGoodsDisountPrice());
                }
                goodsBaseList.addAll(wsqrList);
            }
            sl.setDistributionDiscount(discount.multiply(new BigDecimal(100).setScale(2, RoundingMode.HALF_UP)));
            sl.setWsqrList(goodsBaseList);

            sl.setGoodsNo(goodsNo);
            // 店铺运费
            sl.setShipping(shopShippingQueryResult.getShippingFee());
            // 店铺商品总金额
            sl.setShopPrice(shopGoodsTotalPrice);
            // 店铺折扣
            sl.setShopDiscount(shopGoodsTotalDiscount);
            // 店铺订单总金额
            sl.setShopTotalPrice(shopGoodsTotalPrice.add(shopShippingQueryResult.getShippingFee()));
            shippingList.add(sl);

            supplyOrderPrice.setTotalShippingPrice(shopShippingQueryResult.getShippingFee());
            supplyOrderPrice.setGoodsTotalPrice(shopGoodsTotalPrice);
            supplyOrderPrice.setTotalDisountPrice(sl.getShopDiscount());
        }
        sp.setShippingList(shippingList);
        // 订单总金额
        sp.setTotalPrice(supplyOrderPrice.getOrderPaymentPrice());
        // 总运费
        sp.setTotalShipping(supplyOrderPrice.getTotalShippingPrice());
        // 总折扣
        sp.setShopTotalDiscount(supplyOrderPrice.getTotalDisountPrice());
        sp.setOrderDiscountType(0);
        // 商品总金额
        sp.setAllShopTotalPrice(supplyOrderPrice.getGoodsTotalPrice());

        if (resultResp.getCode() == CommonStatusEnum.SUCCESS.getCode()) {
            resultResp.setData(sp);
            return resultResp;
        }

        if (supplyOrderPrice.getOrderPaymentPrice().compareTo(BigDecimal.ZERO) <= 0) {
            resultResp.setData(sp);
            resultResp.setMessage(BizCodeEnume.SYSTEM_ERROR.getMsg());
            resultResp.setCode(BizCodeEnume.SYSTEM_ERROR.getCode());
            return resultResp;
        }
        return ResponseResult.success(sp);
    }

    @Override
    public ResponseResult createOrders(String para, HttpServletRequest request) {
        try {
            List<String> orderList = new ArrayList<>();
            CommonParameterVO cp = JSON.parseObject(para, new TypeReference<>() {
            });
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(cp.getToken());
            if (null == cp.getBuyerType()) {
                // 查询店铺是否是自营
                Boolean shopSelfSupport = merchantServiceClient.queryShopSelfSupport(Integer.valueOf(userInfo.getShopId()));
                //经销商购买要判断经营类目是否符合
                //根据店铺id 查询 店铺的经营类目
                List<Integer> shopGoodsCatIds = merchantServiceClient.queryShopGoodsCatIds(Integer.valueOf(userInfo.getShopId()));
                // 自营店铺不校验经营类目
                if (!shopSelfSupport && null == shopGoodsCatIds) {
                    return ResponseResult.fail(BizCodeEnume.SHOP_BUSINESS_CATEGORY_NOT_NULL);
                }
                for (Integer goodsId : cp.getGoodsIds()) {
                    // 查询商品经营类目
                    Integer goodsCatId = merchantServiceClient.queryGoodsCatByGoodsId(goodsId);
                    // 自营店铺不校验经营类目
                    if (!shopSelfSupport && !shopGoodsCatIds.contains(goodsCatId)) {
                        return ResponseResult.fail(BizCodeEnume.BUSINESS_CATEGORT_NOT_MATCH);
                    }
                }
            }
            //判断库存
            ResponseResult goodsStockresult = verifyGoodsStock(cp);
            if (goodsStockresult.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
                return goodsStockresult;
            }
            //计算运费
            ResponseResult result = merchantServiceClient.querySupplierShipping(para);
            if (result.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
                return result;
            }
            //获取商品详情
            String goodsDetails = result.getData().toString();
            List<WapShopShippingQueryResultVO> wapList = JSON.parseObject(goodsDetails, new TypeReference<>() {
            });
            String liveId = request.getHeader("liveId");
            String accountId = request.getHeader("accountId");
            // 收货人姓名
            String orderReceiverName = cp.getOrderReceiverName();
            // 收货人地址
            String orderReceiverAddress = cp.getOrderReceiverAddress();
            // 收货人联系方式
            String orderReceiverContact = cp.getOrderReceiverContact();
            // 收货人省Id
            Integer orderReceiverProvinceId = cp.getOrderReceiverProvinceId();
            // 收货人市Id
            Integer orderReceiverCityId = cp.getOrderReceiverCityId();
            // 收货人区Id
            Integer orderReceiverAreaId = cp.getOrderReceiverAreaId();

            // 商户订单号
            String paymentNumber = "U" + DateUtils.getTimeStamp() + DateUtils.randomLengthByTwo();
            // 计算金额 获取会员折扣
            ResponseResult calculateResult = SupplierCalculatePrice(para);
            if (calculateResult.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
                return calculateResult;
            }
            //获取店铺入参
            ShopParaVO detailedPrice = JSON.parseObject(JSON.toJSONString(calculateResult.getData()), ShopParaVO.class);
            List<StoreInformationVO> storeInformationList = cp.getStoreInformationList();
            Map<String, InvoiceEntityInfoVO> invoiceInfoMap = new HashMap<>();
            for (int i = 0; i < wapList.size(); i++) {
                ShippingListVO shippingList = detailedPrice.getShippingList().get(i);
                WapShopShippingQueryResultVO si = wapList.get(i);
                StoreInformationVO storeInformation = storeInformationList.get(i);
                String shopIdTmp = storeInformation.getShopId() + "";
                InvoiceEntityInfoVO ieiTmp = storeInformation.getInvoiceInfo();
                invoiceInfoMap.put(shopIdTmp, ieiTmp);
                // 存放ConsumeTrade参数
                ConsumeTradeParaDTO ctp = new ConsumeTradeParaDTO();
                // 存放ConsumeRecord参数
                ConsumeRecordParaDTO crp = new ConsumeRecordParaDTO();
                // 存放卖家信息
                ConsumeRecordByBusinessDTO crb = new ConsumeRecordByBusinessDTO();
                //平台佣金
                BigDecimal orderGoodsCommission = new BigDecimal(0);
                //平台佣金
                BigDecimal orderBaseCommission = new BigDecimal(0);
                //会员折扣  供应商暂不涉及会员折扣直接赋值为0
                BigDecimal goodsMemberDiscount = new BigDecimal(0);
                //会员折扣
                BigDecimal baseMemberDiscount = new BigDecimal(0);
                String orderDate = DateUtils.getYear() + "-" + DateUtils.getMonth() + "-" + DateUtils.getDay();
                List<Integer> goodsIdList;
                // 发票信息
                String invoiceName = null;
                //此处增加新开票功能
                String invoiceKey = si.getShopId() + "";
                // 订单编号
                String orderId;
                if (userInfo.getUserId().length() <= 8) {
                    orderId = DateUtils.nowtime() + userInfo.getUserId() + DateUtils.randomLengthByTwo();
                } else {
                    String oldUserId = userInfo.getUserId();
                    String newUserId = oldUserId.substring(0, 4) + oldUserId.substring(oldUserId.length() - 4);
                    orderId = DateUtils.nowtime() + newUserId + DateUtils.randomLengthByTwo();
                }
                InvoiceEntityInfoVO invoiceInfo = invoiceInfoMap.get(invoiceKey);
                if (null != invoiceInfo) {
                    invoiceInfo.setOrderId(orderId);
                    invoiceInfo.setUserId(userInfo.getUserId());
                    invoiceInfo.setInvoiceStatus(1);
                    orderInvoiceInfoMapper.addInvoiceInfo(invoiceInfo);
                }
                int index = 0;
                Integer dropShipping = 2;
                if (null != si.getOriginalOrder()) {
                    dropShipping = 1;
                }
                // 保存商品销售数量信息列表
                List<WapGoodsSaleSumUpdateVO> goodsSaleInfoList = new ArrayList<>();
                List<String> goodsNameList = new ArrayList<>();
                for (int n = 0; n < si.getGoodsBaseList().size(); n++) {
                    List<WapShippingQueryResultVO> goodsBaseList = si.getGoodsBaseList().get(n).getGoodsBaseList();
                    for (WapShippingQueryResultVO goodsBase : goodsBaseList) {
                        BigDecimal orderGoodsPaymentAmount = shippingList.getGoodsAmountList().get(index);
                        //计算平台抽佣
                        if (null != goodsBase.getCatId()) {
                            //查询分类抽佣比例
                            BigDecimal catCommission = orderGoodsMapper.queryCatCommission(goodsBase.getCatId());
                            if (null == catCommission) {
                                catCommission = new BigDecimal(0);
                            }
                            //计算平台抽佣
                            catCommission = catCommission.divide(new BigDecimal(100), 2, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
                            orderGoodsCommission = orderGoodsPaymentAmount.subtract(goodsBase.getGoodsShippingFee()).multiply(catCommission).setScale(2, RoundingMode.HALF_UP);
                            orderBaseCommission = orderBaseCommission.add(orderGoodsCommission).setScale(2, RoundingMode.HALF_UP);
                        }
                        // 付款价格(不包含运费)
                        BigDecimal price = goodsBase.getGoodsPrice().multiply(new BigDecimal(goodsBase.getGoodsNum()));
                        goodsBase.setGoodsPaymentPrice(price);
                        // 商品优惠金额
                        BigDecimal goodsDisountPrice = goodsBase.getGoodsDisountPrice() == null ? BigDecimal.ZERO : goodsBase.getGoodsDisountPrice();
                        goodsBase.setGoodsDisountPrice(price.multiply(goodsDisountPrice).setScale(2, RoundingMode.HALF_UP));
                        orderGoodsMapper.insertSupplierByOrderGoods(orderId, si.getShopId(),
                                si.getShopName(), si.getShopId(), si.getShopName(), dropShipping,
                                goodsBase.getGoodsId(), goodsBase.getCommonId(), userInfo.getUserId(),
                                goodsBase.getGoodsName(), goodsBase.getCatId(), goodsBase.getGoodsSpec(), goodsBase.getGoodsPrice(), goodsBase.getGoodsNum(),
                                goodsBase.getGoodsImage(), goodsBase.getGoodsShippingFee(), goodsBase.getGoodsPaymentPrice(), BigDecimal.ZERO,
                                orderGoodsPaymentAmount, 1, 0, BigDecimal.ZERO,
                                BigDecimal.ZERO, BigDecimal.ZERO, 0, 0,
                                DateUtils.getNowDateToString(), 0, 0, 0, 0, BigDecimal.ZERO, BigDecimal.ZERO, orderGoodsCommission, goodsMemberDiscount, goodsBase.getVerId());

                        // 保存商品销量信息
                        WapGoodsSaleSumUpdateVO goodsSaleInfo = saveGoodsSale(goodsBase, cp.getToken(), cp.getTreasureOrderShopId());
                        goodsSaleInfoList.add(goodsSaleInfo);
                        goodsNameList.add(goodsBase.getGoodsName());
                        if (null != storeInformation.getOriginalOrder()) {
                            orderBaseMapper.updateOrderDealerGoods(orderId, goodsBase.getGoodsId(), storeInformation.getOriginalOrder());
                        } else {
                            // 根据 gooid 查询当前商品是否是代发商品
                            OrderDealerGoodsVO orderDealerGoodsVO = orderBaseMapper.queryOrderSupplierGoods(goodsBase.getGoodsId());
                            // 如果是 查询供应商商品含税金额 查询店铺经销折扣 封装经销折扣数据 存入数据库
                            if (null != orderDealerGoodsVO) {
                                orderDealerGoodsVO.setOrderId(orderId);
                                orderDealerGoodsVO.setTotalTaxInclusiveAmount(
                                        orderDealerGoodsVO.getGoodsPrice().multiply(new BigDecimal(goodsBase.getGoodsNum())).setScale(2, RoundingMode.HALF_UP));
                                // 根据店铺id  供应商店铺id 查询经销折扣
                                BigDecimal dealerDiscount = orderBaseMapper.queryDealerDiscountByUserId(userInfo.getShopId(), orderDealerGoodsVO.getSupplierShopId());

                                if (null != dealerDiscount && dealerDiscount.compareTo(new BigDecimal(100)) < 0) {
                                    orderDealerGoodsVO.setDealerDiscount(dealerDiscount.multiply(new BigDecimal("0.01")));
                                    orderDealerGoodsVO.setDealerDiscountAmout(
                                            orderDealerGoodsVO.getTotalTaxInclusiveAmount().subtract(
                                                    (orderDealerGoodsVO.getTotalTaxInclusiveAmount().multiply(orderDealerGoodsVO.getDealerDiscount()).setScale(2, RoundingMode.HALF_UP))
                                            ));
                                } else {
                                    orderDealerGoodsVO.setDealerDiscount(BigDecimal.ZERO);
                                    orderDealerGoodsVO.setDealerDiscountAmout(BigDecimal.ZERO);
                                }
                                // 插入数据库
                                orderBaseMapper.insertByOrderDealerGoods(orderDealerGoodsVO);
                            }
                        }
                        index++;
                    }
                }
                //订单来源 0:自营订单,1:代发平台供应链订单,2:代发怡亚通供应链订单
                Integer orderSource = 0;
                //进货编号
                String purchaseNumber = "JH" + DateUtils.formateYearAndDay(new Date()) + PayToolUtil.buildRandom(6);
                String originalOrder = si.getOriginalOrder();
                // 订单状态
                Integer orderStatus = 1;
                if (cp.getPaymentCode() == 3) {
                    orderStatus = 3;
                }
                // 添加订单
                ShopUserMessageResultVO shopUserMessageResult = orderSupplierCreateMapper.queryUserId(shippingList.getShopId());
                shippingList.setShopDiscount(shippingList.getShopDiscount() == null ? BigDecimal.ZERO : shippingList.getShopDiscount());
                //订单金额
                BigDecimal orderpay = shippingList.getShopTotalPrice().subtract(shippingList.getShopDiscount());
                Integer shopId = storeInformation.getSupplierShopId() != null ? storeInformation.getShopId() : storeInformation.getShopId();
                orderBaseMapper.insertSupplierByOrderBase(orderId, shopId, null, shopUserMessageResult.getShopName(),
                        userInfo.getUserId(), userInfo.getUserName(), shopUserMessageResult.getUserId(), shopUserMessageResult.getUserName(), orderDate, DateUtils.getNowDateToString(), orderReceiverName,
                        orderReceiverAddress, orderReceiverContact, orderReceiverProvinceId, orderReceiverCityId,
                        orderReceiverAreaId, paymentNumber, invoiceName, shippingList.getInvoiceId(), shippingList.getShopPrice(),
                        orderpay, shippingList.getShopDiscount(), shippingList.getShipping(), 0,
                        shippingList.getBuyerMessage(), orderStatus, 0, BigDecimal.ZERO, null, cp.getPlatForm(),
                        null, BigDecimal.ZERO, BigDecimal.ZERO, null, BigDecimal.ZERO,
                        BigDecimal.ZERO, shippingList.getDistrictId(), cp.getTreasureOrderShopId(),
                        0, 0, null, 0, liveId, accountId, 1, null, null,
                        shippingList.getShopDistributorId(), CommonType.orderType.SUPPLY_TYPE.getCode(), shippingList.getShopId(), shippingList.getShopName(),
                        BigDecimal.ZERO, 0, 1, null, 2, dropShipping, BigDecimal.ZERO, BigDecimal.ZERO, orderBaseCommission, baseMemberDiscount, shippingList.getShopDiscount(),
                        purchaseNumber, originalOrder, orderSource, cp.getPaymentCode());

                Integer count = orderBaseMapper.checkInvoiceSwitchStatus(shippingList.getShopId());
                if (count > 0) {
                    if (!orderList.contains(orderId)) {
                        orderList.add(orderId);
                    }
                }
                // consumeRecord所需参数(商家订单)
                crb.setSellId(shopUserMessageResult.getUserId());
                // 订单号
                crb.setOrderId(orderId);
                crb.setUserName(shopUserMessageResult.getUserName());
                // 支付号
                crb.setPaymentNumber(paymentNumber);
                crb.setTotalPrice(shippingList.getShopTotalPrice());
                // 存放goodsnameConsumeRecord
                crb.setGoodsName(goodsNameList);
                payMentServiceClient.insertConsumeRecordByBusiness(crb);
                // fegin调用插入数据ConsumeRecord(买家订单)
                crp.setUserId(userInfo.getUserId());
                // 订单号
                crp.setOrderId(orderId);
                crp.setUserName(userInfo.getUserName());
                // 支付号
                crp.setPaymentNumber(paymentNumber);
                crp.setTotalPrice(shippingList.getShopTotalPrice());
                // 存放goodsnameConsumeRecord
                crp.setGoodsName(goodsNameList);
                payMentServiceClient.insertConsumeRecord(crp);
                // consumeTrade所需参数
                ctp.setUserId(userInfo.getUserId());
                // 订单号
                ctp.setOrderId(orderId);
                ctp.setSellerId(shopUserMessageResult.getUserId());
                // 支付号
                ctp.setPaymentNumber(paymentNumber);
                // 商品价格
                ctp.setTotalPrice(shippingList.getShopTotalPrice());
                // 优惠价格
                ctp.setTradeDiscount(shippingList.getShopDiscount());
                ctp.setGoodsName(goodsNameList);
                // fegin调用插入数据ConsumeTrade
                payMentServiceClient.insertConsumeTrade(ctp);
                //更新库存
                supplierClient.updateSupplierSubStockAndAddSaleNum(JSON.toJSONString(goodsSaleInfoList));

                RedisUtil.set(HouConfig.HOU_SALENUM + paymentNumber, JSON.toJSONString(goodsSaleInfoList), 86400);
                RedisUtil.set(HouConfig.HOU_SALENUM + orderId, JSON.toJSONString(goodsSaleInfoList), 86400);
                RedisUtil.set(HouConfig.HOU_SHOP_IS_SUPPORT + orderId, shippingList.getShopSelfSupport().toString(), 86400);
                // 存放付款金额
                RedisUtil.set(HouConfig.HOU_USERAMOUNTACTUALLYPAID + "--" + cp.getToken() + "--" + userInfo.getUserId(), detailedPrice.getTotalPrice().toString(), 1200);
                // 保存商家电话信息，在支付完成后发送发货提醒会用到
                RedisUtil.set(HouConfig.HOU_SELLERPHONE + "--" + orderId, shopUserMessageResult.getContactsPhone(), 86400);

                // 下单成功，删除商品
                goodsIdList = shippingList.getGoodsIdList();
                orderBaseMapper.batchSupplierDeleteGoods(userInfo.getUserId(), goodsIdList);
            }
            OrderInfoVO info = new OrderInfoVO();
            info.setPaymentNumber(paymentNumber);
            info.setOrderIdList(orderList);
            return ResponseResult.success(info);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail(0, "下单失败");
        }
    }

    /**
     * 根据传入的消息计算商品运费信息并进行校验。
     * 该方法首先尝试查询商品的运费信息。如果查询成功，直接返回查询结果。
     * 如果查询失败，根据返回的错误码提供不同的错误信息。
     * 特别地，如果错误码表示该销售区域的商品已售罄，则返回相应的售罄信息。
     * 其他情况下，返回表示未获取到信息的错误消息。
     *
     * @param message 包含商品信息和配送区域的消息字符串。
     * @return 运费查询结果。如果查询成功，包含运费详情；如果查询失败，包含相应的错误信息。
     */
    private ResponseResult goodsShipping(String message) {
        // 查询商品的运费信息
        ResponseResult rr = merchantServiceClient.queryShipping(message);
        // 如果查询成功，直接返回查询结果
        if (rr.getCode() == (CommonStatusEnum.SUCCESS.getCode())) {
            return rr;
        } else {
            // 如果查询失败，根据错误码返回不同的错误信息
            if (rr.getCode() == (BizCodeEnume.PRODUCTS_OUT_OF_STOCK_IN_THIS_SALES_AREA.getCode())) {
                // 销售区域商品已售罄的情况
                return ResponseResult.fail(BizCodeEnume.PRODUCTS_OUT_OF_STOCK_IN_THIS_SALES_AREA.getMsg());
            } else {
                // 其他查询失败的情况
                return ResponseResult.fail(BizCodeEnume.NO_INFORMATION_OBTAINED.getMsg());
            }
        }
    }

    /**
     * 商品限购校验
     *
     * @param shippingQueryResult
     * @param userInfo
     * @return
     */
    private ResponseResult goodsSaleCheck(WapShippingQueryResultVO shippingQueryResult, UserInfoVO userInfo) {
        Integer goodsMaxSale = shippingQueryResult.getGoodsMaxSale();
        Integer goodsStock = shippingQueryResult.getGoodsStock();
        Integer goodsNumber = shippingQueryResult.getGoodsNum();
        // 商品限购判断
        if (shippingQueryResult.getGoodsMaxSale().intValue() != 0) {
            // 判断商品是否有最大限购数量
            if (goodsNumber > goodsMaxSale || goodsNumber > goodsStock) {
                return ResponseResult.fail(0, "您购买的" + shippingQueryResult.getGoodsName() + BizCodeEnume.GOODS_EXCEEDS_THE_MAXIMUM_PURCHASE_LIMIT.getMsg());
            }
        } else {
            // 商品无最大限购数量时，以商品库存为基准进行判断
            goodsMaxSale = goodsStock;
            if (goodsNumber > goodsMaxSale || goodsNumber > goodsStock) {
                return ResponseResult.fail(0, "您购买的" + shippingQueryResult.getGoodsName() + BizCodeEnume.GOODS_EXCEEDS_THE_MAXIMUM_PURCHASE_LIMIT.getMsg());
            }
        }
        if (shippingQueryResult.getCommonLimit().intValue() == 1) {
            // 判断是否是限购商品
            //限购次数
            Integer restrictionTimes = shippingQueryResult.getRestrictionTimes();
            //限购时间
            Integer restrictionHours = shippingQueryResult.getRestrictionHours();
            //下单数量
            Integer purchasingGoods = orderSupplierCreateMapper.queryPurchasingGoods(userInfo.getUserId(), shippingQueryResult.getGoodsId(), restrictionHours);
            Integer purchasingGoodsStatusNum = orderSupplierCreateMapper.queryPurchasingGoodsStatusNum(userInfo.getUserId(), shippingQueryResult.getGoodsId());
            if (restrictionTimes == (purchasingGoods + purchasingGoodsStatusNum)) {
                return ResponseResult.fail(0, "您购买的" + shippingQueryResult.getGoodsName() + "商品规定" + restrictionHours + "天之内只能下单" + restrictionTimes + "次，请勿购买！");
            }
        }
        return ResponseResult.success();
    }

    /**
     * 保存商品销售数据。
     * 创建一个WapGoodsSaleSumUpdateVO对象，用于更新商品的销售汇总信息。
     *
     * @param goodsBase           商品基础信息，包含商品ID、销量等。
     * @param token               用户的令牌，用于身份验证。
     * @param treasureOrderShopId 聚宝订单中的店铺ID。
     * @return 返回一个WapGoodsSaleSumUpdateVO对象，包含更新后的商品销售信息。
     */
    private WapGoodsSaleSumUpdateVO saveGoodsSale(WapShippingQueryResultVO goodsBase, String token, Integer treasureOrderShopId) {
        // 创建WapGoodsSaleSumUpdateVO对象，用于存储待更新的商品销售汇总信息
        // 存放商品信息，增加销量
        WapGoodsSaleSumUpdateVO wssu = new WapGoodsSaleSumUpdateVO();

        // 初始化商品类型为0，表示普通商品
        Integer goodsType = 0;

        // 设置WapGoodsSaleSumUpdateVO对象的属性，包括用户令牌、商品ID、购买数量、商品通用ID、商品类型和聚宝订单店铺ID
        wssu.setToken(token);
        wssu.setGoodsId(goodsBase.getGoodsId());
        wssu.setGoodsNum(goodsBase.getGoodsNum());
        wssu.setCommonId(goodsBase.getCommonId());
        wssu.setGoodsType(goodsType);
        wssu.setTreasureOrderShopId(treasureOrderShopId);

        // 返回更新后的商品销售汇总信息
        return wssu;
    }


    /**
     * 校验商品库存是否充足。
     *
     * @param cp 公共参数对象，包含是否查询库存的标志以及商品存储信息列表。
     * @return 如果库存充足，返回成功响应；如果库存不足，返回相应的错误响应。
     */
    public ResponseResult verifyGoodsStock(CommonParameterVO cp) {
        // 遍历每个店铺的商品信息
        for (StoreInformationVO str1 : cp.getStoreInformationList()) {
            List<GoodsInformationVO> goodsList = str1.getGoodsBaseList();
            // 遍历每个商品
            for (GoodsInformationVO str2 : goodsList) {
                // 根据商品ID查询商品库存信息
                Map<String, Object> resmap = orderBaseMapper.queryBygoodsId(str2.getGoodsId());
                // 如果查询到库存信息
                if (null != resmap) {
                    // 获取商品库存数量
                    // 库存
                    Integer goodsStock = Integer.parseInt(resmap.get("goodsStock").toString());
                    // 日志记录商品库存信息
                    log.info("stock库存=================>{}", goodsStock);
                    // 检查商品购买数量是否超过库存数量
                    if (str2.getGoodsNum() > goodsStock) {
                        // 如果超过库存数量，返回库存不足的错误响应
                        // 部分商品购买数量超过库存数量
                        return ResponseResult.fail(BizCodeEnume.EXCEEDING_INVENTORY_QUANTITY);
                    }
                }
            }
        }
        // 如果所有商品库存检查通过，返回成功响应
        return ResponseResult.success();
    }

    /**
     * 判断商品是否是一键代发,并创建供应商订单
     *
     * @return
     */
    public ResponseResult createDealerGoodsOrder(List<String> orderList, String params, HttpServletRequest request) {
        List<Integer> goodsIds = new ArrayList<>();
        List<StoreInformationVO> storeInformationList = new ArrayList<>();
        for (String orderId : orderList) {
            //根据订单号查询一件代发商品
            // 一键代发查询依赖 goods_base.supplier_shop_id 等字段，本库没有（代发链路未启用），
            // 查询失败按「该单无代发商品」处理，不能让整单回滚。
            List<DealerGoodsFlagVO> dealerOrderList;
            try {
                dealerOrderList = orderBaseMapper.queryDealerOrderList(orderId);
            } catch (Exception dealerEx) {
                log.warn("一键代发查询不可用，按无代发商品处理：{}", dealerEx.toString());
                dealerOrderList = new ArrayList<>();
            }
            if (!dealerOrderList.isEmpty()) {
                Map<Integer, List<DealerGoodsFlagVO>> groupedByCategory = dealerOrderList.stream()
                        .collect(Collectors.groupingBy(DealerGoodsFlagVO::getSupplierShopId));

                for (Map.Entry<Integer, List<DealerGoodsFlagVO>> entry : groupedByCategory.entrySet()) {
                    Integer supplierShopId = entry.getKey();
                    List<DealerGoodsFlagVO> items = entry.getValue();
                    //店铺参数
                    StoreInformationVO shopStoreInformationDTO = new StoreInformationVO();
                    shopStoreInformationDTO.setUseRebateType(0);
                    shopStoreInformationDTO.setCouponId("0");
                    shopStoreInformationDTO.setInvoiceId(0);
                    shopStoreInformationDTO.setShopId(items.getFirst().getShopId());
                    shopStoreInformationDTO.setSupplierShopId(items.getFirst().getSupplierShopId());
                    shopStoreInformationDTO.setOriginalOrder(orderId);
                    List<GoodsInformationVO> goodsBaseList = new ArrayList<>();
                    for (DealerGoodsFlagVO item : items) {
                        goodsIds.add(item.getGoodsId());
                        GoodsInformationVO dto = new GoodsInformationVO();
                        dto.setGoodsId(item.getGoodsId());
                        dto.setGoodsName(item.getGoodsName());
                        dto.setGoodsNum(item.getGoodsNum());
                        goodsBaseList.add(dto);

                    }
                    shopStoreInformationDTO.setGoodsBaseList(goodsBaseList);
                    storeInformationList.add(shopStoreInformationDTO);
                }
            }

        }
        if (!storeInformationList.isEmpty() && !goodsIds.isEmpty()) {
            CommonParameterVO cp = JSON.parseObject(params, new TypeReference<>() {
            });
            cp.setStoreInformationList(storeInformationList);
            cp.setGoodsIds(goodsIds);
            cp.setBuyerType(1);

            params = JSON.toJSONString(cp);
            //创建供应商订单
            ResponseResult result = createOrders(params, request);
            if (result.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
                return result;
            }

        }

        return ResponseResult.success();
    }

    @Override
    public ResponseResult SupplierCalculatePrice(String message) {
        CommonParameterVO commonParam = JSON.parseObject(message, CommonParameterVO.class);
        ResponseResult result = new ResponseResult();
        //查运费
        result = merchantServiceClient.querySupplierShipping(message);
        if (result.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            if (result.getCode() == BizCodeEnume.PRODUCTS_OUT_OF_STOCK_IN_THIS_SALES_AREA.getCode()) {
                //该商品所售区域暂时无货！
                return ResponseResult.fail(BizCodeEnume.PRODUCTS_OUT_OF_STOCK_IN_THIS_SALES_AREA);
            }
            if (result.getCode() == BizCodeEnume.GOODS_OFFLINE.getCode()) {
                //该商品下架！
                return ResponseResult.fail(BizCodeEnume.GOODS_OFFLINE);
            }
            return ResponseResult.fail(BizCodeEnume.NO_INFORMATION_OBTAINED);
        } else {
            String goodsDetails = result.getData().toString();
            // json转对象
            List<WapShopShippingQueryResultVO> wapList = JSON.parseObject(goodsDetails, new TypeReference<>() {
            });
            ShopParaVO sp = new ShopParaVO();
            //商品价格
            BigDecimal goodPrice = new BigDecimal(0);
            //是否有自营店铺
            Boolean isHasSelfShop = false;
            //会员等级名称
            List<String> gradeNames = new ArrayList<>();
            //会员折扣金额
            BigDecimal hiuYuanZhekou = new BigDecimal(0);
            // 总优惠券金额
            sp.setCouponTotalPrice(BigDecimal.ZERO);
            // 总价(实付金额+运费-扣除红包消费券之后的价格)
            BigDecimal totalPrice = new BigDecimal(0);
            // 所有店铺价格(不含运费与优惠价格)
            BigDecimal allShopTotalPrice = new BigDecimal(0);
            // 跨店满减总商品价格
            BigDecimal allowancePrice = BigDecimal.ZERO;
            // 多品红包总商品价格
            BigDecimal allredPacketPrice = BigDecimal.ZERO;
            // 总运费
            BigDecimal totalShipping = new BigDecimal(0);
            // 商品总价
            BigDecimal goodsTotalPrice = new BigDecimal(0);
            List<ShippingListVO> shippingList = new ArrayList<>();

            //总满减优惠金额
            sp.setTotalManjianDiscount(BigDecimal.ZERO);
            //总满折优惠金额
            sp.setTotalManzheDiscount(BigDecimal.ZERO);
            String token = commonParam.getToken();
            UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(commonParam.getToken());
            String userIdStr = "";
            if (token != null && !token.isEmpty()) {
                // 获取用户id
                userIdStr = ui.getUserId();
            }
            // 平台优惠券金额
            BigDecimal platformVoucherPrice;
            List<Integer> avaliableGoods;
            BigDecimal redPacketPrice = new BigDecimal(0);
            // 红包编码
            String redpacketId = null;
            // 平台优惠券id
            Integer platFormVoucherId = null;
            // 单品红包商品id(舍弃，不存值)
            String goodsCatLimit = null;

            List<Integer> goodsIdList = new ArrayList<>();
            // 店铺红包id
            String shopLimit = null;
            // 红包使用状态
            Integer redPacketStatus = null;
            Integer redpacketTType = null;
            List<String> voucherIdList = new ArrayList<>();
            try {
                //优惠券id
                platFormVoucherId = commonParam.getVoucherId();
                Map<String, Object> voucherTIdMap = new HashMap<>();
                if (platFormVoucherId != null && !"0".equals(platFormVoucherId.toString())) {
                    String platformCouponCode = platFormVoucherId.toString();
                    // 当参数不为空时，则使用平台优惠券
                    log.info("-----Enter-----");
                    voucherTIdMap.put("voucherId", platformCouponCode);
                    // 获取代金券信息
                    VoucherBaseVO voucher = activityServiceClient.getVoucherDetail(voucherTIdMap);
                    // 获取平台优惠券id
                    sp.setPlatformVoucherCode(platformCouponCode);
                    // 获取优惠券价格
                    platformVoucherPrice = voucher.getVoucherPrice();
                    sp.setPlatformVoucherPrice(platformVoucherPrice);
                    // 参加平台优惠券商品id
                    avaliableGoods = voucher.getAvaliableGoods();
                    sp.setAvaliableGoods(avaliableGoods);
                } else {
                    platformVoucherPrice = new BigDecimal(0);
                    sp.setPlatformVoucherPrice(platformVoucherPrice);
                    avaliableGoods = null;
                    sp.setAvaliableGoods(avaliableGoods);
                }
            } catch (Exception e) {
                log.info("-----voucherTIdIsNull-----" + e.getMessage());
                return ResponseResult.fail(e.getMessage());
            }
            // 3.1 获取所有供应商店铺的店铺折扣
            Map<Integer, DealerApplyInfoVO> dealerApplyInfoMap = new HashMap<>(8);
            List<Integer> supplyShopIdList = wapList.stream().map(WapShopShippingQueryResultVO::getShopId).collect(Collectors.toList());
            DealerShopInfoVO dealerShopInfo = new DealerShopInfoVO(ui.getShopId(), supplyShopIdList);
            List<DealerApplyInfoVO> dealerApplyInfoList = orderSupplierCreateMapper.queryDealerApplyListByShop(dealerShopInfo.getDealerShopId(), dealerShopInfo.getSupplyShopIdList());
            if (null != dealerApplyInfoList && !dealerApplyInfoList.isEmpty()) {
                dealerApplyInfoMap = dealerApplyInfoList.stream().collect(Collectors.toMap(DealerApplyInfoVO::getSupplierShopId, Function.identity()));
            }
            // 根据店铺循环 店铺循环开始，s1存放每个店铺的信息
            Integer discountInt = 0;
            BigDecimal discount = null;
            for (WapShopShippingQueryResultVO WapShopShippingQueryResultVO : wapList) {
                // 当前店铺优惠比例
                if (null != dealerApplyInfoMap.get(WapShopShippingQueryResultVO.getShopId())) {
                    discount = new BigDecimal(dealerApplyInfoMap.get(WapShopShippingQueryResultVO.getShopId()).getDealerDiscount()).divide(new BigDecimal(100), 2, BigDecimal.ROUND_UP);
                }
                // 存放shopId&&每家店铺运费
                ShippingListVO sl = new ShippingListVO();
                // 各店铺总价(不含运费与优惠价格)
                BigDecimal shopPrice = BigDecimal.ZERO;
                // 参加红包活动总价
                BigDecimal itemShopPrice = BigDecimal.ZERO;
                // 参加购物津贴活动总价
                BigDecimal allowanceShopPrice = BigDecimal.ZERO;
                // 店铺运费
                BigDecimal shippingFee = WapShopShippingQueryResultVO.getShippingFee();
                //数据处理
                sl = getShippingList(WapShopShippingQueryResultVO, shopLimit, goodsIdList, goodsCatLimit, redpacketTType);
                sl.setIsHasSelfShop(false);
                // 从wapList中获取当前循环项的店铺ID
                Integer shopId = WapShopShippingQueryResultVO.getShopId();
                Integer dalerShopId = WapShopShippingQueryResultVO.getDealerShopId();
                String originalOrder = WapShopShippingQueryResultVO.getOriginalOrder();

                // 初始化ShopMemberVO对象，用于存储店铺会员信息
                ShopMemberVO shopMember = new ShopMemberVO();

                // 设置店铺ID，来源于当前循环项
                shopMember.setShopId(String.valueOf(shopId));

                // 设置会员的用户ID
                shopMember.setUserId(userIdStr);

                // 初始化店铺折扣价、未税价和税价
                BigDecimal shopZheKouPrice = new BigDecimal(0);
                BigDecimal shopWeiShuiPrice = new BigDecimal(0);
                BigDecimal shopTaxPrice = new BigDecimal(0);

                // 每家店铺总价格
                BigDecimal shopTotalPrice = new BigDecimal(0);
                // 商品数量
                Integer goodsNo = 0;
                // 参加多品红包商品数量
                Integer goodsQuantity = 0;
                List<Integer> goodsIds = new ArrayList<>();
                List<BigDecimal> list = new ArrayList<>();
                List<BigDecimal> goodsAmountList = new ArrayList<>();
                List<BigDecimal> goodsPriceList = new ArrayList<>();
                List<BigDecimal> taxIncludedPriceList = new ArrayList<>();
                // 获取商品
                //ZZZ:获取每个店铺下的所有商品
                List<WapShopShippingResultVO> wssrList = WapShopShippingQueryResultVO.getGoodsBaseList();
                Integer index = 0;
                //ZZZ:遍历店铺商品
                for (WapShopShippingResultVO shopShippingVO : wssrList) {
                    //ZZZ：获取不同包裹底下的所有商品
                    List<WapShippingQueryResultVO> wsqrList = shopShippingVO.getGoodsBaseList();

                    //ZZZ:遍历包裹商品
                    for (WapShippingQueryResultVO wapShippingVO : wsqrList) {
                        // 付款价格(不包含运费)
                        BigDecimal allPrice = wapShippingVO.getGoodsPrice().multiply(new BigDecimal(wapShippingVO.getGoodsNum()));
                        wapShippingVO.setGoodsPaymentPrice(allPrice);
                        // 商品优惠金额
                        if(discount != null) {
                            wapShippingVO.setGoodsDisountPrice(allPrice.multiply(discount).setScale(2, BigDecimal.ROUND_HALF_UP));
                        }else {
                            wapShippingVO.setGoodsDisountPrice(BigDecimal.ZERO);
                        }
                        // 初始化商品价格为0，用于后续计算总价格
                        BigDecimal Prices = BigDecimal.ZERO;

                        // 将商品ID添加到商品ID列表中，用于后续处理
                        goodsIds.add(wapShippingVO.getGoodsId());

                        // 设置商品ID列表到购物车项中
                        sl.setGoodsIdList(goodsIds);

                        // 初始化商品数量，用于后续计算总数量
                        Integer goodsNumber;

                        // 初始化通用类型，如果为null则默认为1
                        Integer commonType = wapShippingVO.getCommonType() == null ? 1 : wapShippingVO.getCommonType();

                        if (commonType == 1) {

                            // 会员价阶梯价
                            if ((commonParam.getIsInquiry() == null || commonParam.getIsInquiry() != 1)) {
                                //商品价格
                                goodPrice = wapShippingVO.getGoodsPrice();
                                if (sp.getShopMoprice() == null) {
                                    sp.setShopMoprice(0.00);
                                }
                                BigDecimal taxInclusiveAmount = wapShippingVO.getGoodsPrice();

                                taxIncludedPriceList.add(taxInclusiveAmount.setScale(2, RoundingMode.HALF_UP));
                                sl.setTaxIncludedPriceList(taxIncludedPriceList);
                                // 将经销折扣价设置为商品单价
                                if (originalOrder != null) {
                                    discountInt = orderBaseMapper.getPriceBySupplierShopAndGoodsShopId(shopId, dalerShopId);
                                } else {
                                    discountInt = orderBaseMapper.getPriceBySupplierShopAndGoodsAndUserId(shopId,ui.getUserId());
                                }
                                if(discountInt != null && discountInt!=0){
                                    BigDecimal discountDivide = new BigDecimal(discountInt).divide(new BigDecimal(100));
                                    BigDecimal zk=new BigDecimal(1).add(discountDivide);
                                    goodPrice= wapShippingVO.getGoodsPrice().multiply(discountDivide);
                                }
                                wapShippingVO.setGoodsPrice(goodPrice);
                                List<Integer> hyORstep = getHyOrStep(sl, wapShippingVO);
                                sl.setStepOrHyList(hyORstep);

                                goodsPriceList.add(goodPrice);
                                sl.setGoodsPriceList(goodsPriceList);
                                sp.setHyGoodPrice(goodPrice);
                                list.add(sp.getHyGoodPrice());
                                sl.setHyPriceList(list);
                                // 商品小计
                                goodsAmountList.add(sp.getHyGoodPrice().multiply(BigDecimal.valueOf(wapShippingVO.getGoodsNum())));
                                sl.setGoodsAmountList(goodsAmountList);
                                hiuYuanZhekou = hiuYuanZhekou.add(BigDecimal.ZERO);
                                shopZheKouPrice = shopZheKouPrice.add(BigDecimal.ZERO);

                            }
                            String goodsId = wapShippingVO.getGoodsId().toString();
                            goodsNumber = wapShippingVO.getGoodsNum();
                            BigDecimal goodsNum = new BigDecimal(goodsNumber);
                            if (Prices.compareTo(new BigDecimal(0)) == 0) {
                                Prices = wapShippingVO.getGoodsPrice();
                            }
                            // 付款价格(不包含运费)
                            BigDecimal price = Prices;
                            log.info("-----goodsId-----" + goodsId);
                            // 不属于参加多品红包的店铺
                            sl.setItemShopId("");
                            if (platFormVoucherId != null && platFormVoucherId != 0
                                    && avaliableGoods.contains(Integer.parseInt(goodsId))) {
                                allowancePrice = allowancePrice.add(price);
                                allowanceShopPrice = allowanceShopPrice.add(price);
                                sl.setAllowanceShopId(shopId.toString());
                            } else {
                                sl.setAllowanceShopId("");
                            }
                            shopWeiShuiPrice = shopWeiShuiPrice.add(price);

                            if ((commonParam.getIsInquiry() == null || commonParam.getIsInquiry() != 1)) {
                                price = price.multiply(goodsNum);
                                goodsTotalPrice = goodsTotalPrice.add(price);
                                shopTotalPrice = shopTotalPrice.add(price);
                                shopPrice = shopPrice.add(price);
                            }
                            goodsNo = goodsNo + goodsNumber;
                            sl.setGoodsQuantity(goodsQuantity);
                            sl.setGoodsNo(goodsNo);

                        }
                        index++;
                    }
                }
                sl.setShopTaxPrice(shopTaxPrice.setScale(2, RoundingMode.HALF_UP));
                // 设置本月店铺水费价格，用于累加计算店铺水费
                sl.setShopWeiShuiPrice(shopWeiShuiPrice.setScale(2, RoundingMode.HALF_UP));
                // 设置本月店铺折扣价格，用于计算折扣后价格
                sl.setShopZheKouPrice(shopZheKouPrice.setScale(2, RoundingMode.HALF_UP));
                // 如果店铺水费价格为空，则默认为0，避免后续计算出现空指针异常
                if (sp.getShopWeiShuiPrice() == null) {
                    sp.setShopWeiShuiPrice(BigDecimal.ZERO);
                }
                // 累加计算店铺水费，确保水费的准确性
                sp.setShopWeiShuiPrice(sp.getShopWeiShuiPrice().add(sl.getShopWeiShuiPrice()));
                // 如果店铺税费为空，则默认为0，避免后续计算出现空指针异常
                if (sp.getShuiFei() == null) {
                    sp.setShuiFei(BigDecimal.ZERO);
                }
                // 累加计算店铺税费，确保税费的准确性
                sp.setShuiFei(sp.getShuiFei().add(sl.getShopTaxPrice()).setScale(2, RoundingMode.HALF_UP));

                // 设置商品的店铺价格
                sl.setShopPrice(shopPrice.setScale(2, RoundingMode.HALF_UP));
                // 设置商品的单品店铺价格
                sl.setItemShopPrice(itemShopPrice.setScale(2, RoundingMode.HALF_UP));
                // 设置商品的优惠券抵扣后的店铺价格
                sl.setAllowanceShopPrice(allowanceShopPrice.setScale(2, RoundingMode.HALF_UP));

                // 判断是否满足包邮条件，若不满足则累加运费
                if (shopTotalPrice.compareTo(new BigDecimal(WapShopShippingQueryResultVO.getShopFreeShipping())) < 0 || WapShopShippingQueryResultVO.getShopFreeShipping().compareTo(BigInteger.ZERO) == 0) {
                    totalShipping = totalShipping.add(shippingFee);
                    goodsTotalPrice = goodsTotalPrice.add(shippingFee);
                    shopTotalPrice = shopTotalPrice.add(shippingFee);
                } else {
                    // 满足包邮条件，设置运费为0
                    sl.setShipping(new BigDecimal(0));
                }

                // 更新商品的店铺总价
                sl.setShopTotalPrice(shopTotalPrice);

                // 将商品的运费信息添加到运费列表
                shippingList.add(sl);

                // 所有店铺总价
                allShopTotalPrice = allShopTotalPrice.add(shopPrice);
            }
            // 店铺总商品总金额
            totalPrice = totalPrice.add(goodsTotalPrice);
            // 更新总价，减去优惠券金额、平台代金券金额、满减优惠和满折优惠
            totalPrice = totalPrice.subtract(sp.getCouponTotalPrice()).subtract(platformVoucherPrice)
                    .subtract(sp.getTotalManjianDiscount()).subtract(sp.getTotalManzheDiscount());
            // 更新所有店铺的总价，同样减去优惠券金额、平台代金券金额、满减优惠和满折优惠
            allShopTotalPrice = allShopTotalPrice.subtract(sp.getCouponTotalPrice()).subtract(platformVoucherPrice)
                    .subtract(sp.getTotalManjianDiscount()).subtract(sp.getTotalManzheDiscount());

            if (totalPrice.compareTo(BigDecimal.ZERO) <= 0) {
                //订单金额错误，请勿操作！
                return ResponseResult.fail(BizCodeEnume.ORDER_AMOUNT_ERROR);
            }
            // 优惠方式
            Integer orderDiscountType = null;
            if (discount != null) {
                // 经销商折扣
                orderDiscountType = 6;
            } else {
                // 当不存在优惠券和红包，或活动已结束时，选择折扣类型为0
                orderDiscountType = 0;
            }
            // 设置会员价保留两位小数
            sp.setHyGoodPrice(sp.getHyGoodPrice() != null ? sp.getHyGoodPrice().setScale(2, RoundingMode.HALF_UP) : sp.getHyGoodPrice());
            // 设置商品是否包含自有店铺商品的标记
            sp.setIsHasSelfShop(isHasSelfShop);
            // 设置店铺折扣价格
            sp.setShopZheKouPrice(hiuYuanZhekou.setScale(2, RoundingMode.HALF_UP));
            // 设置等级名称列表
            sp.setGradeNames(gradeNames);
            // 设置总价
            sp.setTotalPrice(totalPrice.setScale(2, RoundingMode.HALF_UP));
            // 设置红包ID
            sp.setRedpacketId(redpacketId);
            // 设置配送列表
            sp.setShippingList(shippingList);
            // 设置总配送费用
            sp.setTotalShipping(totalShipping);
            // 设置优惠券ID列表
            sp.setVoucherIdList(voucherIdList);
            // 设置红包价格
            sp.setRedPacketPrice(redPacketPrice);
            // 设置优惠金额
            sp.setAllowancePrice(allowancePrice);
            // 设置红包状态
            sp.setRedPacketStatus(redPacketStatus);
            // 设置所有红包价格
            sp.setAllredPacketPrice(allredPacketPrice);
            // 设置订单折扣类型
            sp.setOrderDiscountType(orderDiscountType);
            // 设置所有店铺的总价
            sp.setAllShopTotalPrice(allShopTotalPrice.setScale(2, RoundingMode.HALF_UP));

            result.setCode(1);
            result.setData(sp);
        }
        return result;
    }

    private List<Integer> getHyOrStep(ShippingListVO sl, WapShippingQueryResultVO wapShippingVO) {
        List<Integer> hyORstep = new ArrayList<>();
        if (sl.getStepOrHyList() != null) {
            hyORstep = sl.getStepOrHyList();
        }
        if (wapShippingVO.getGoodsStock() == 0) {
            hyORstep.add(0);
        } else {
            hyORstep.add(1);
        }
        return hyORstep;
    }

    private ShippingListVO getShippingList(WapShopShippingQueryResultVO wapList, String shopLimit, List<Integer> goodsIdList, String goodsCatLimit, Integer redpacketTType) {

        // 存放shopId&&每家店铺运费
        ShippingListVO sl = new ShippingListVO();
        Integer shopId = wapList.getShopId();
        // 店铺运费
        BigDecimal shippingFee = wapList.getShippingFee();
        sl.setShopId(shopId);
        // 存放店铺红包id
        // 设置店铺限制，用于控制每个店铺的最大购买量或其它限制条件
        sl.setShopLimit(shopLimit);
        // 设置配送费用，用于计算订单的总费用，包括商品价格和配送费用
        sl.setShipping(shippingFee);
        // 设置商品ID列表，表示用户选择购买的商品集合
        sl.setGoodsIdList(goodsIdList);

        // 存放单品红包id
        sl.setGoodsCatLimit(goodsCatLimit);
        sl.setRedpacketTType(redpacketTType);
        //店铺满减金额
        sl.setShopManjianDiscount(BigDecimal.ZERO);
        //店铺满折金额
        sl.setShopManzheDiscount(BigDecimal.ZERO);
        BigDecimal receivingTime = new BigDecimal(72);
        sl.setReceivingTime(receivingTime);
        sl.setShopName(wapList.getShopName());
        sl.setBuyerMessage(wapList.getBuyerMessage());
        sl.setDistrictId(wapList.getDistrictId());
        sl.setShopDistributorId(wapList.getShopId());
        sl.setShopSelfSupport(wapList.getShopSelfSupport());
        return sl;
    }

    /**
     * 创建订单
     *
     * @param params  包含创建订单所需信息的参数字符串
     * @param request 用户的请求对象，可用于获取额外的请求信息
     * @return 返回订单创建结果，成功则返回带有订单信息的ResponseResult，失败则返回错误信息的ResponseResult
     */
    @Override
    public ResponseResult supplierCreateOrders(String params, HttpServletRequest request) {
        // 获取Redis锁，以保证订单创建的原子性
        RedisLock redisLock = null;
        try (Jedis jedis = RedisUtil.getJedis()) {
            // 解析请求参数
            CommonParameterVO cp = JSON.parseObject(params, new TypeReference<>() {
            });
            // 通过token获取用户信息
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(cp.getToken());
            /**
             * 下单类型:1.普通用户购买,2:经销商购买
             */
            if (null == cp.getBuyerType()) {
                // 查询店铺是否是自营
                Boolean shopSelfSupport = orderBaseMapper.queryShopSelfSupport(Integer.valueOf(userInfo.getShopId()));
                //经销商购买要判断经营类目是否符合
                //根据店铺id 查询 店铺的经营类目
                List<Integer> shopGoodsCatIds = orderBaseMapper.queryShopGoodsCatIds(Integer.valueOf(userInfo.getShopId()));
                // 自营店铺不校验经营类目
                if (!shopSelfSupport && null == shopGoodsCatIds) {
                    return ResponseResult.fail(BizCodeEnume.SHOP_BUSINESS_CATEGORY_NOT_NULL.getCode(), BizCodeEnume.SHOP_BUSINESS_CATEGORY_NOT_NULL.getMsg());
                }
                for (Integer goodsId : cp.getGoodsIds()) {
                    // 查询商品经营类目
                    Integer goodsCatId = orderBaseMapper.queryGoodsCatByGoodsId(goodsId);
                    // 自营店铺不校验经营类目
                    if (!shopSelfSupport && !shopGoodsCatIds.contains(goodsCatId)) {
                        return ResponseResult.fail(BizCodeEnume.SHOP_BUSINESS_CATEGORY_DISCREPANCY.getCode(), BizCodeEnume.SHOP_BUSINESS_CATEGORY_DISCREPANCY.getMsg());
                    }
                }
            }

            // 生成Redis锁，防止高并发情况下服务器处理异常
            redisLock = new RedisLock(jedis, OrderConstant.ORDER_SUPPLIER_CREATE, 10000);
            // 尝试订单锁，若失败则直接返回操作失败
            if (redisLock.lock()) {
                // 创建订单
                ResponseResult result = createOrders(params, request);
                // 若订单创建失败，则回滚全局事务
                if (result != null && result.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
                    throw new DefinedException(result);
                }
                return result;
            }
            return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
        } catch (Exception e) {
            // 处理线程中断异常
            return ResponseResult.fail(e.getMessage());
        } finally {
            // 释放Redis锁
            if (redisLock != null) {
                redisLock.unLock();
            }
        }
    }

}
