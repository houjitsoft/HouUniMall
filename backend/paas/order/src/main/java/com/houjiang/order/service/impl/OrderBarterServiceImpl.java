package com.houjiang.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.base.BaseExpressDTO;
import com.houjiang.commons.dto.goods.ManageShopAddressDTO;
import com.houjiang.commons.dto.goods.StorageInventoryFlowGoodsDTO;
import com.houjiang.commons.dto.order.ManageShopAddressDetailDTO;
import com.houjiang.commons.dto.order.OrderDropGoodsDTO;
import com.houjiang.commons.dto.order.WapGoodsSaleSumDTO;
import com.houjiang.commons.dto.vop.JingDongReturnGoodsDTO;
import com.houjiang.commons.dto.vop.JingDongReturnOrderDTO;
import com.houjiang.commons.dto.vop.OrderAddresDTO;
import com.houjiang.commons.dto.yiyatong.ApplyRefundDTO;
import com.houjiang.commons.dto.yiyatong.ApplyReturnGoodsDTO;
import com.houjiang.commons.dto.yiyatong.OrderDetailDTO;
import com.houjiang.commons.dto.yiyatong.SendReturnGoodsDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.exception.DefinedException;
import com.houjiang.commons.util.*;
import com.houjiang.commons.util.json.JsonUtils;
import com.houjiang.commons.util.excel.ExcelsUtil;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.vo.order.OrderBaseVO;
import com.houjiang.commons.vo.order.OrderGoodsBarterVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.commons.vo.yiyatong.base.OrderReturnGoodsVO;
import com.houjiang.commons.vo.yiyatong.base.ResellerOrderDetailVO;
import com.houjiang.commons.vo.yiyatong.base.ResellerOrderItemVO;
import com.houjiang.order.dto.*;
import com.houjiang.order.entity.*;
import com.houjiang.order.feign.*;
import com.houjiang.order.mapper.*;
import com.houjiang.order.service.OrderBarterService;
import com.houjiang.order.vo.*;
import io.seata.core.context.RootContext;
import io.seata.tm.api.GlobalTransactionContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.houjiang.order.service.impl.OrderBaseServiceImpl.setStorageInventoryFlow;
import static com.houjiang.order.service.impl.OrderBaseServiceImpl.setStorageInventoryFlow2;

/**
 * 订单换货表(OrderBarter)表服务实现类
 *
 * @author Amos
 * @since 2025-05-07 09:36:29
 */
@Slf4j
@Service
public class OrderBarterServiceImpl extends ServiceImpl<OrderBarterMapper, OrderBarterEntity>
        implements OrderBarterService {

    @Resource
    private OrderBaseMapper orderBaseMapper;

    @Resource
    private OrderGoodsMapper orderGoodsMapper;

    @Resource
    private OrderReturnMapper orderReturnMapper;

    @Resource
    private OrderYiyatongMapper orderYiyatongMapper;

    @Resource
    private MerchantServiceClient merchantServiceClient;

    @Resource
    private UserServiceClient userServiceClient;

    @Resource
    private BaseServiceClient baseServiceClient;

    @Resource
    private OrderPaymentVocherMapper orderPaymentVocherMapper;

    @Resource
    private GoodServiceClient goodServiceClient;

    @Resource
    private OrderReturnPlatformMapper orderReturnPlatformMapper;

    @Resource
    private OrderDealerInfoMapper orderDealerInfoMapper;

    @Resource
    private SupplierClient supplierClient;

    @Resource
    private OrderShipmentMapper orderShipmentMapper;

    /**
     * 申请换货处理方法
     *
     * @param applyReturnOrders 用户提交的换货申请信息
     * @return 返回处理结果
     */
    @Override
    public ResponseResult applyForBarter(ApplyReturnOrderDTO applyReturnOrders) {
        // 获取当前用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(applyReturnOrders.getToken());

        // 校验退货原因是否为空
        if (applyReturnOrders.getReturnReasonId() == null || applyReturnOrders.getReturnReasonId().isEmpty()) {
            return ResponseResult.fail(BizCodeEnume.RETURN_REASON_CANNOT_BE_NULL);
        }

        // 查询当前订单的退货状态，如果已经处于退货中，则不允许再次申请
        List<Integer> returnStateList = Arrays.asList(3,20);
        OrderReturnStatusDTO returnDTO = orderReturnMapper.queryOrderReturnData(applyReturnOrders.getOrderId());
        if (null != returnDTO && null != returnDTO.getReturnState() &&
                (returnDTO.getReturnState() > 1 && !returnStateList.contains(returnDTO.getReturnState()))) {
            return ResponseResult.fail(BizCodeEnume.ORDER_STATUS_CHANGE);
        }

        // 根据订单ID查询订单基本信息
        List<OrderBaseVO> orderBaseList = orderBaseMapper.getOrderBaseById(applyReturnOrders.getOrderId());
        OrderBaseVO orderBase = orderBaseList.getFirst();
        // 如果订单售后时间小于当前时间订单不可退
        if (orderBase.getVirtualGoods() != 0 && null != orderBase.getOrderLastReturnTime() &&
                DateUtils.compareDate(orderBase.getOrderLastReturnTime(), DateUtils.getNowDate())) {
            return ResponseResult.fail(BizCodeEnume.GOODS_BEYOND_THE_AFTER_SALES_PERIOD);
        }

        String orderId = applyReturnOrders.getOrderId();

        // 清空平台介入信息
        orderReturnPlatformMapper.deletePlatform(String.valueOf(applyReturnOrders.getOrderId()));

        // 去userms模块查找用户信息 "sellerId","sellerName","shopId","userId"
        Map<String, String> getSerllerInfoMap = new HashMap<>();
        getSerllerInfoMap.put("shopId", String.valueOf(orderBase.getShopId()));
        Map<String, String> sellerInfo = (Map<String, String>) userServiceClient.getSellerInfo(getSerllerInfoMap).getData();

        // 计算退货总件数
        int orderGoodsNum = 0;

        // 根据商品己算退货价格
        List<ApplyReturnGoodDTO> applyReturnGoods = applyReturnOrders.getApplyReturnGoods();

        // 判断当前订单是否全部退货
        Double returnCash = 0.00;
        // 售后红包金额
        Double returnRptCash = 0.00;
        // 退还运费
        Double returnShippingFee = 0.00;
        // 供应商 退还的 经销等级折扣
        BigDecimal orderDiscountFee = new BigDecimal(0);

        BigDecimal shopDealerCommission = new BigDecimal(0);
        int two = 0;
        // 由于要展示退货详情所以商品表撤销是没清状态(所以二次申请要清空状态)
        Map<String, Object> orderGoodsMap1 = new HashMap<>();
        orderGoodsMap1.put("goodsRefundStatus", 0);
        orderGoodsMap1.put("goodsReturnStatus", 0);
        orderGoodsMap1.put("goodsBarterStatus", 0);
        orderGoodsMap1.put("orderId", orderId);
        orderReturnMapper.updateOrderGoodsById(orderGoodsMap1);

        List<WapGoodsSaleSumDTO> wapGoodsStockSumList = new  ArrayList<>();
        for (ApplyReturnGoodDTO applyReturnGood : applyReturnGoods) {
            OrderGoodsEntity orderGoodsEntity = orderGoodsMapper.selectOne(new LambdaQueryWrapper<OrderGoodsEntity>()
                    .select(OrderGoodsEntity::getOrderGoodsReturnnum, OrderGoodsEntity::getBarterGoodsId)
                    .eq(OrderGoodsEntity::getOrderId, applyReturnOrders.getOrderId())
                    .eq(OrderGoodsEntity::getGoodsId, applyReturnGood.getOrderGoodsId()));
            Integer barterGoodsId = applyReturnGood.getBarterGoodsId();
            Integer oldBarterGoodsId = orderGoodsEntity.getBarterGoodsId() == null ? 0 : orderGoodsEntity.getBarterGoodsId();
            Integer oldGoodsReturnNum = orderGoodsEntity.getOrderGoodsReturnnum() == null ? 0 : Integer.parseInt(orderGoodsEntity.getOrderGoodsReturnnum());
            Integer barterGoodsNum = Integer.valueOf(applyReturnGood.getOrderGoodsNum());
            // 获取商品库存
            Integer goodsStock;
            if(applyReturnOrders.getIsSupplier() == null || applyReturnOrders.getIsSupplier() != 1) {
                goodsStock = goodServiceClient.queryGoodsStock(barterGoodsId);
            }else{
                goodsStock = supplierClient.queryGoodsStock(barterGoodsId);
            }
            if(oldBarterGoodsId.equals(barterGoodsId)){
                if((goodsStock + oldGoodsReturnNum) < barterGoodsNum){
                    throw new DefinedException(BizCodeEnume.STOCK_INSUFFICIENT);
                }
                WapGoodsSaleSumDTO dto = new WapGoodsSaleSumDTO();
                dto.setGoodsId(barterGoodsId);
                dto.setGoodsNum(String.valueOf(barterGoodsNum - oldGoodsReturnNum));
                wapGoodsStockSumList.add(dto);
            }else{
                if(oldBarterGoodsId != 0) {
                    // 将之前换货的商品库存回滚
                    WapGoodsSaleSumDTO oldDto = new WapGoodsSaleSumDTO();
                    oldDto.setGoodsId(oldBarterGoodsId);
                    oldDto.setGoodsNum(String.valueOf(-oldGoodsReturnNum));
                    wapGoodsStockSumList.add(oldDto);
                }
                if(goodsStock < barterGoodsNum){
                    throw new DefinedException(BizCodeEnume.STOCK_INSUFFICIENT);
                }
                WapGoodsSaleSumDTO dto = new WapGoodsSaleSumDTO();
                dto.setGoodsId(barterGoodsId);
                dto.setGoodsNum(String.valueOf(barterGoodsNum));
                wapGoodsStockSumList.add(dto);
            }

            // 查询商品购买数量
            orderGoodsNum += Integer.parseInt(applyReturnGood.getOrderGoodsNum());
            two += orderGoodsMapper.update(null, new LambdaUpdateWrapper<OrderGoodsEntity>()
                    .eq(OrderGoodsEntity::getOrderId, applyReturnOrders.getOrderId())
                    .eq(OrderGoodsEntity::getGoodsId, applyReturnGood.getOrderGoodsId())
                    .set(OrderGoodsEntity::getOrderGoodsReturnnum, barterGoodsNum)
                    .set(OrderGoodsEntity::getBarterGoodsId, applyReturnGood.getBarterGoodsId())
                    // 订单状态 与order同步
                    .set(OrderGoodsEntity::getOrderGoodsStatus, CommonType.orderStatus.CONFIRM_RECEIPT.getCode())
                    .set(OrderGoodsEntity::getGoodsBarterStatus, CommonType.orderBarterStatus.BARTERING.getCode()));
        }

        // 更新订单状态为退货中
        int one = orderBaseMapper.update(null, new LambdaUpdateWrapper<OrderBaseEntity>()
                .eq(OrderBaseEntity::getOrderId, applyReturnOrders.getOrderId())
                .set(OrderBaseEntity::getOrderStatus, CommonType.orderStatus.CONFIRM_RECEIPT.getCode())
                .set(OrderBaseEntity::getOrderBarterStatus, CommonType.orderBarterStatus.BARTERING.getCode())
                .set(OrderBaseEntity::getOrderFinishedTime, DateUtils.getNowDate())
                .set(OrderBaseEntity::getReturnMethodType, CommonType.returnMethodType.BARTER.getCode())
                .set(OrderBaseEntity::getDealerReturnCommission, shopDealerCommission)
                // 1.1 退回资金
                .set(OrderBaseEntity::getOrderRefundAmount, returnCash)
                // 插入买家id，为资金回退做准备
                .set(OrderBaseEntity::getSellerUserId, sellerInfo.get("userId")));

        Map<String, Object> orderReturnMap = new HashMap<>();
        int isExistData = orderReturnMapper.selectCount(new LambdaQueryWrapper<OrderReturnEntity>()
                .eq(OrderReturnEntity::getOrderId, applyReturnOrders.getOrderId())).intValue();

        orderReturnMapper.updateOdbOrderReturn(applyReturnOrders.getOrderId());
        List<String> voucher = new ArrayList<>();
        if (applyReturnOrders.getReturnVoucher() != null) {
            voucher = applyReturnOrders.getReturnVoucher();
            String returnVoucher = JSON.toJSONString(voucher);
            orderReturnMap.put("returnVoucher", returnVoucher);
        }
        orderReturnMap.put("orderId", applyReturnOrders.getOrderId());
        orderReturnMap.put("returnState", 1);
        orderReturnMap.put("returnShopHandle", 1);
        // 1为退款申请,2为退货申请,3为换货申请
        orderReturnMap.put("returnType", 3);
        orderReturnMap.put("returnCash", returnCash);
        orderReturnMap.put("returnMethodStatus", 4);
        orderReturnMap.put("returnReasonId", applyReturnOrders.getReturnReasonId());
        orderReturnMap.put("returnReason", applyReturnOrders.getReturnReason());
        orderReturnMap.put("returnMessage", applyReturnOrders.getReturnMessage());
        orderReturnMap.put("orderGoodsNum", orderGoodsNum);
        orderReturnMap.put("orderDiscountFee", orderDiscountFee);
        // 订单总额，包含运费
        orderReturnMap.put("orderAmount", orderBase.getOrderGoodsAmount().add(BigDecimal.valueOf(orderBase.getOrderShippingFee())));
        orderReturnMap.put("orderGoodsId", 0);
        orderReturnMap.put("orderGoodsName", 0);
        orderReturnMap.put("orderGoodsPrice", 0);
        orderReturnMap.put("orderGoodsPic", 0);
        orderReturnMap.put("buyerUserId", userInfo.getUserId());
        orderReturnMap.put("shopId", orderBase.getShopId());
        orderReturnMap.put("shopName", orderBase.getShopName());
        orderReturnMap.put("buyerUserAccount", userInfo.getUserName());
        orderReturnMap.put("returnAddrId", 0);
        orderReturnMap.put("returnPostCode", 0);
        // 待写退回红包金额
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        orderReturnMap.put("returnRptCash",
                decimalFormat.format(returnRptCash));
        orderReturnMap.put("returnShippingFee",
                decimalFormat.format(returnShippingFee));
        orderReturnMap.put("returnShopHandle", 1);
        // 插入买家id，为资金回退做准备
        orderReturnMap.put("sellerUserId", sellerInfo.get("userId"));
        orderReturnMap.put("returnMobile", orderBase.getOrderReceiverContact());
        String nowDate = DateUtils.getTime();
        SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateUtils.getNowDateToDf());
        calendar.add(Calendar.DATE, 1);
        // 处理时效时间
        Date calendarTime = calendar.getTime();
        String returnCode;
        if (isExistData == 1) {
            OrderReturnStatusDTO returnStatus = orderReturnMapper.queryOrderReturnData(applyReturnOrders.getOrderId());
            // 在卖家没介入之前可以一直修改但不改失效时间
            orderReturnMap.put("historicalReturnStatus", "");
            if (returnStatus.getReturnState() != 1) {
                orderReturnMap.put("returnAddTime", nowDate);
                orderReturnMap.put("expirationTime", formatter1.format(calendarTime));
                if (returnStatus.getReturnState() == 3) {
                    Integer returnChangeNum = returnStatus.getReturnChangeNum() + 1;
                    orderReturnMap.put("returnChangeNum", returnChangeNum);
                } else if (returnStatus.getReturnState() == 10) {
                    Integer resubmissionsNumber = Integer.valueOf(returnStatus.getResubmissionsNumber() + 1);
                    orderReturnMap.put("resubmissionsNumber", resubmissionsNumber);
                }
            }
            // 更新申请退款记录
            orderReturnMapper.updateOrderReturn(orderReturnMap);
            returnCode = returnStatus.getReturnCode();
        } else {
            // 退多件暂时不存
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String dateString = formatter.format(new Date());
            if (applyReturnOrders.getIsSupplier() == null || applyReturnOrders.getIsSupplier() == 0) {
                returnCode = "TD-" + userInfo.getUserId() + "-" + orderBase.getShopId() + "-" + dateString + "-"
                        + "0001";
            } else {
                returnCode = "TD-" + userInfo.getUserId() + "-" + orderBase.getDeliverShopId() + "-" + dateString + "-"
                        + "0001";
            }
            // 退货编号
            orderReturnMap.put("returnCode", returnCode);
            orderReturnMap.put("returnAddTime", nowDate);
            orderReturnMap.put("expirationTime", formatter1.format(calendarTime));

            Integer three = orderReturnMapper.returnMoney(orderReturnMap);
            if (1 != one || 0 == two || 1 != three) {
                throw new DefinedException(BizCodeEnume.APPLICATION_FAILED);
            }
        }

        // 发送消息模板
        Map<String, Object> messageMap = new HashMap<>();
        List<String> messageList = new ArrayList<>();
        messageList.add(applyReturnOrders.getOrderId());
        messageMap.put("list", messageList);
        messageMap.put("orderId", applyReturnOrders.getOrderId());
        messageMap.put("returnCode", returnCode);
        String returnGoodsName = orderReturnMapper.queryReturnGoodsNameByOrderId(applyReturnOrders.getOrderId());
        messageMap.put("goodsName", returnGoodsName);
        messageMap.put("userId", orderBase.getSellerUserId());
        messageMap.put("sellerId", orderBase.getSellerUserId());
        messageMap.put("sellerName", orderBase.getSellerUserName());
        messageMap.put("messageType", 1);
        messageMap.put("shopId", orderBase.getShopId());
        messageMap.put("messageTitle", "换货通知");
        messageMap.put("templateId", CommonType.messageTemplate.CODE_V.getCode());
        baseServiceClient.sendMessageTemplate(messageMap);

        // 申请换货减少库存
        if(applyReturnOrders.getIsSupplier() == null || applyReturnOrders.getIsSupplier() != 1) {
            goodServiceClient.updateSubStockAndAddSaleNum(JSON.toJSONString(wapGoodsStockSumList));
        }else{
            supplierClient.updateSupplierSubStockAndAddSaleNum(JSON.toJSONString(wapGoodsStockSumList));
        }

        if (!applyReturnGoods.isEmpty()) {
            Map<String, List<ApplyReturnGoodDTO>> orderMap = new HashMap<>();
            for (ApplyReturnGoodDTO goods : applyReturnGoods) {
                OrderDealerInfoEntity dealerInfoEntity = orderDealerInfoMapper.selectOne(new LambdaQueryWrapper<OrderDealerInfoEntity>()
                        .eq(OrderDealerInfoEntity::getOrderId, applyReturnOrders.getOrderId())
                        .eq(OrderDealerInfoEntity::getGoodsId, goods.getOrderGoodsId()));
                OrderDropGoodsDTO barterDealerInfoEntity = supplierClient.queryDropGoodsInfo(String.valueOf(goods.getBarterGoodsId()));
                if (null != dealerInfoEntity && null != barterDealerInfoEntity) {
                    List<ApplyReturnGoodDTO> applyReturnSonGoods = new ArrayList<>();
                    if (orderMap.get(dealerInfoEntity.getOriginalOrder()) != null) {
                        applyReturnSonGoods = orderMap.get(dealerInfoEntity.getOriginalOrder());
                    }
                    //经销商品id
                    goods.setOrderGoodsId(dealerInfoEntity.getSupplierGoodsId().toString());
                    //经销商品价格
                    goods.setOrderGoodsPrice(returnCash.toString());
                    //经销换货商品id
                    goods.setBarterGoodsId(barterDealerInfoEntity.getSupplyGoodsId());
                    applyReturnSonGoods.add(goods);
                    orderMap.put(dealerInfoEntity.getOriginalOrder(), applyReturnSonGoods);
                }
            }
            if (!orderMap.isEmpty()) {
                for (Map.Entry<String, List<ApplyReturnGoodDTO>> entry : orderMap.entrySet()) {
                    System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
                    //经销商品集合
                    applyReturnOrders.setApplyReturnGoods(entry.getValue());
                    //经销订单
                    applyReturnOrders.setOrderId(entry.getKey());
                    //经销订单金额
                    applyReturnOrders.setReturnCash(returnCash);
                    applyReturnOrders.setIsSupplier(1);
                    applyForBarter(applyReturnOrders);
                }
            }
        }

        //判断是否是怡亚通订单
        if (orderBase.getOrderSource() == CommonType.SupplierSource.YI_YA_TONG.getCode()) {
            //查询怡亚通订单信息
            OrderYiyatongEntity orderYiyatongEntity = orderYiyatongMapper.selectOne(new LambdaQueryWrapper<OrderYiyatongEntity>()
                    .eq(OrderYiyatongEntity::getOrderId, applyReturnOrders.getOrderId()));
            ApplyRefundDTO applyRefundDTO = new ApplyRefundDTO();
            applyRefundDTO.setOrderSn(orderYiyatongEntity.getOrderSn());
            applyRefundDTO.setReason(applyReturnOrders.getReturnReason());
            applyRefundDTO.setRefundReasonId(applyReturnOrders.getReturnReasonId());
            applyRefundDTO.setReturnType(2);
            //查询订单详情,获取规格id和商品数
            OrderDetailDTO dto = new OrderDetailDTO();
            dto.setOrderSn(orderYiyatongEntity.getOrderSn());
            ResponseResult orderResult = goodServiceClient.getOrderDetail(dto);
            ResellerOrderDetailVO vo = JSONObject.parseObject(JSON.toJSONString(orderResult.getData()), ResellerOrderDetailVO.class);
            List<ResellerOrderItemVO> orderGoodsList = vo.getGoodsList();
            List<ApplyReturnGoodsDTO> goodsList = new ArrayList<>();
            if (!orderGoodsList.isEmpty()) {
                for (ResellerOrderItemVO item : orderGoodsList) {
                    ApplyReturnGoodsDTO applyReturnGoodsDTO = new ApplyReturnGoodsDTO();
                    applyReturnGoodsDTO.setGoodsSpecId(item.getGoodsSpecId());
                    applyReturnGoodsDTO.setGoodsQuantity(item.getReturnGoodsNum());
                    applyReturnGoodsDTO.setRefundAmount(new BigDecimal(item.getReturnGoodsNum()).multiply(item.getPerPrice()));
                    goodsList.add(applyReturnGoodsDTO);
                }
                applyRefundDTO.setGoodsList(goodsList);
            }
            //申请怡亚通售后
            ResponseResult result = goodServiceClient.applyRefund(applyRefundDTO);
            if (CommonStatusEnum.SUCCESS.getCode() != result.getCode()) {
                throw new DefinedException(result);
            }

            //获取售后单号
            OrderReturnGoodsVO orderReturnGoodsVO = JSONObject.parseObject(JSONObject.toJSONString(result.getData()), OrderReturnGoodsVO.class);
            log.info("orderId:{}-怡亚通售后单号-{}", orderYiyatongEntity.getOrderId(), orderReturnGoodsVO.getReturnSn());
            orderYiyatongEntity.setReturnSn(orderReturnGoodsVO.getReturnSn());
            //保存售后单号
            orderYiyatongMapper.updateById(orderYiyatongEntity);
        } else if (orderBase.getOrderSource() == CommonType.SupplierSource.JING_DONG.getCode()) {
            // 判断是京东订单
            // 封装商城退单商品信息
            JingDongReturnOrderDTO jingDongReturnOrderDTO = new JingDongReturnOrderDTO();
            jingDongReturnOrderDTO.setOrderId(applyReturnOrders.getOrderId());
            jingDongReturnOrderDTO.setReturnReason(applyReturnOrders.getReturnReason());
            jingDongReturnOrderDTO.setReturnMethodType(CommonType.returnMethodType.BARTER.getCode());
            jingDongReturnOrderDTO.setVoucher(voucher);
            List<JingDongReturnGoodsDTO> jingDongReturnGoodsDTOList = new ArrayList<>();
            for (ApplyReturnGoodDTO applyReturnGood : applyReturnGoods) {
                JingDongReturnGoodsDTO  jingDongReturnGoodsDTO = new JingDongReturnGoodsDTO();
                jingDongReturnGoodsDTO.setGoodsNum(Integer.valueOf(applyReturnGood.getOrderGoodsNum()));
                jingDongReturnGoodsDTO.setGoodsId(Integer.valueOf(applyReturnGood.getOrderGoodsId()));
                // 根据商城 商品id和订单号 查询京东订单号
                String jdOrderId = orderGoodsMapper.queryJdOrderId(applyReturnOrders.getOrderId(),applyReturnGood.getOrderGoodsId());
                jingDongReturnGoodsDTO.setJdOrderId(jdOrderId);
                jingDongReturnGoodsDTOList.add(jingDongReturnGoodsDTO);
            }
            // 封装 收件地址
            OrderAddresDTO pickupAddress = new OrderAddresDTO();
            pickupAddress.setUserName(orderBase.getOrderReceiverName());
            pickupAddress.setUserContact(orderBase.getOrderReceiverContact());
            pickupAddress.setAddress(orderBase.getOrderReceiverAddress());
            // 解析并设置详细的收货地址
            String[] address = orderBase.getOrderReceiverAddress().split(" ");
            String load = address[address.length - 1].replace("\\t", "").replace("\\n", "");
            pickupAddress.setAddressDetail(load);
            jingDongReturnOrderDTO.setPickupAddress(pickupAddress);
            // 封装 返件地址 换货是使用
            jingDongReturnOrderDTO.setReturnAddress(pickupAddress);

            // 调用京东 生成售后单
            goodServiceClient.createJdAfterSaleOrder(jingDongReturnOrderDTO);
        }
        return ResponseResult.success();
    }

    /**
     * 查询换货订单
     */
    @Override
    public ResponseResult<PageUtils<ReturnOrderVO>> selectBarterOrder(ReturnOrderDTO dto) {
        // 将用户信息转换为需要的格式
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(dto.getToken());
        // 设置订单查询的时间范围
        setOrderTime(dto);
        // 将用户店铺ID和退货类型添加到查询参数中
        dto.setShopId(Integer.valueOf(userInfo.getShopId()));
        // 如果存在店铺类型，则将其添加到查询参数中
        if (userInfo.getShopType() != null) {
            dto.setShopType(userInfo.getShopType());
        }
        // 设置分页默认值，防止前端未传参导致NPE
        Integer page = dto.getPage() != null ? dto.getPage() : 1;
        Integer limit = dto.getLimit() != null ? dto.getLimit() : 10;
        // 分页查询退款列表
        Page<ReturnOrderVO> pageObj = new Page<>(page, limit);
        // 根据查询参数查询退货订单列表
        IPage<ReturnOrderVO> returnRefundIPage = this.baseMapper.selectBarterOrderList(pageObj, dto);
        // 遍历查询结果，对每个退货订单进行加工处理
        return setBarterGoods(returnRefundIPage, dto.getShopId(), dto.getShopType(), userInfo);
    }

    /**
     * 根据传入的订单时间类型，设置订单的起止时间。
     *
     * @param dto 包含订单相关信息的映射，其中"orderStartTime"键用于指定订单时间类型。
     *            根据不同的订单时间类型，更新映射中的"orderStartTime"和"orderEndTime"的值。
     */
    private void setOrderTime(ReturnOrderDTO dto) {
        // 检查map中是否包含订单时间信息
        if (dto.getOrderTime() != null) {
            try {
                // 将订单时间从map中取出并转换为Integer类型
                int orderTime = dto.getOrderTime();
                // 创建一个日期格式化对象，用于将日期转换为字符串
                SimpleDateFormat df = new SimpleDateFormat(DateUtils.YYYY_MM_DD_HH_MM_SS);
                // 根据订单时间的值，设置不同的查询日期范围
                if (orderTime == 1) {
                    // 查询当天的日期范围
                    dto.setReturnAddTime(DateUtils.getDayStartTime2());
                    dto.setReturnFinishTime(DateUtils.getDayStrEndTime2());
                } else if (orderTime == 2) {
                    // 查询明天的日期范围
                    dto.setReturnAddTime(df.format(DateUtils.addDays(df.parse(DateUtils.getDayStartTime2()), 1)));
                    dto.setReturnFinishTime(df.format(DateUtils.addDays(df.parse(DateUtils.getDayStrEndTime2()), 1)));
                } else if (orderTime == 3) {
                    // 查询三个月后的日期范围
                    dto.setReturnAddTime(df.format(DateUtils.addMonth(df.parse(DateUtils.getDayStartTime2()), 3)));
                    dto.setReturnFinishTime(df.format(DateUtils.addMonth(df.parse(DateUtils.getDayStrEndTime2()), 3)));
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 查询换货订单详情
     */
    @Override
    public ResponseResult queryBarterOrderDetails(ReturnOrderDTO dto) {
        // 获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(dto.getToken());
        // 获取token用于后续请求
        String token = dto.getToken();
        // 根据returnCode从数据库中获取退货详情
        SellerOrderDetailVO sellerOrderDetail = baseMapper.queryBarterOrderDetails(dto.getReturnCode());
        // 如果存在退货凭证，将其解析为列表
        if (sellerOrderDetail.getReturnVoucher() != null) {
            List<String> voucher = JSON.parseArray(sellerOrderDetail.getReturnVoucher(), String.class);
            sellerOrderDetail.setReturnVouchers(voucher);
        }
        // 构建查询快递公司的请求参数
        Map<String, Object> expressMap = new HashMap<>(1);
        expressMap.put("token", token);
        // 调用服务查询快递公司列表
        ResponseResult expressQueryResults = baseServiceClient.queryExpressList(expressMap);
        // 如果查询结果存在，则遍历查找与退货邮编匹配的快递公司名称
        if (null != expressQueryResults.getData()) {
            List<LinkedHashMap<Object, Object>> list = (List<LinkedHashMap<Object, Object>>) expressQueryResults.getData();
            for (LinkedHashMap<Object, Object> result : list) {
                // 如果找到匹配的快递公司，保存其名称
                if (null != result.get("expressId") && null != sellerOrderDetail.getReturnPostCode()) {
                    if (result.get("expressId").toString().equals(sellerOrderDetail.getReturnPostCode())) {
                        sellerOrderDetail.setReturnPostName((String) result.get("expressName"));
                    }
                }
            }
        }
        // 如果是代发货订单，根据不同的情况更新退货退款金额
        if (null != sellerOrderDetail.getDropShipping() && 1 == sellerOrderDetail.getDropShipping()) {
            if (null != sellerOrderDetail.getSupperReturnPrice()) {
                sellerOrderDetail.setOrderPaymentAmount(sellerOrderDetail.getSupperPaymentAmount().toString());
            }
            if (null != sellerOrderDetail.getSupperRefundPrice()) {
                sellerOrderDetail.setOrderPaymentAmount(sellerOrderDetail.getSupperPaymentAmount().toString());
            }
        }
        // 根据订单ID构建查询退货商品的参数
        Map<String, String> selectMap = new HashMap<>();
        selectMap.put("orderId", sellerOrderDetail.getOrderId());
        selectMap.put("shopId", userInfo.getShopId());
        selectMap.put("shopType", userInfo.getShopType().toString());
        // 查询退货商品详情
        List<GoodsDetailsVO> listReturnGoods = orderGoodsMapper.listReturnGoods1(selectMap);
        // 如果是换货订单，为每个商品添加换货商品详情
        if (sellerOrderDetail.getReturnMethodStatus().equals(CommonType.returnMethodType.BARTER.getCode())) {
            for (GoodsDetailsVO goods : listReturnGoods) {
                ResponseResult<OrderGoodsBarterVO> barterGoods;
                if(userInfo.getShopType() == 1) {
                    // 调用服务客户端查询换货商品信息，并设置到商品对象中
                    barterGoods = goodServiceClient.selectBarterGoods(goods.getBarterGoodsId());
                }else {
                    // 调用服务客户端查询换货商品信息，并设置到商品对象中
                    barterGoods = supplierClient.selectBarterGoods(goods.getBarterGoodsId());
                }
                goods.setBarterGoods(barterGoods.getData());
            }
        }
        // 设置退货商品详情
        sellerOrderDetail.setGoodsDetails(listReturnGoods);
        // 设置换货物流信息
        setBarterOrderShippingList(null, sellerOrderDetail);
        List<BarterShippingInfoVO> barterShippingInfoList = baseMapper.selectBarterShippingInfoList(sellerOrderDetail.getReturnCode());
        if (!barterShippingInfoList.isEmpty()) {
            barterShippingInfoList.forEach(barterShippingInfoVO -> {
                //查询快递公司名字
                ResponseResult express = baseServiceClient.getBaseExpressInfoById(barterShippingInfoVO.getBarterPostCode());
                Map shipperCode = JSON.parseObject(JSON.toJSONString(express.getData()), Map.class);
                barterShippingInfoVO.setExpressName(shipperCode.get("expressName") + "");
            });
            sellerOrderDetail.setBarterShippingInfoList(barterShippingInfoList);
        }
        // 根据退货状态更新步骤状态
        if (sellerOrderDetail.getReturnState() == 1) {
            sellerOrderDetail.setStepState(1);
        }
        if (sellerOrderDetail.getReturnState() == 2 || sellerOrderDetail.getReturnState() == 3) {
            sellerOrderDetail.setStepState(2);
        }
        if (sellerOrderDetail.getReturnState() == 6) {
            // 卖家收到货物
            sellerOrderDetail.setStepState(3);
        }
        if (sellerOrderDetail.getReturnState() == 4) {
            if (sellerOrderDetail.getPaymentCode() == 2) {
                sellerOrderDetail.setStepState(5);
            } else {
                sellerOrderDetail.setStepState(4);
            }
        }
        // 退款关闭（新增的）
        if (sellerOrderDetail.getReturnState() == 8) {
            sellerOrderDetail.setStepState(8);
        }
        // 平台介入（新增的）
        if (sellerOrderDetail.getReturnState() == 9) {
            sellerOrderDetail.setStepState(9);
        }
        // 验收不通过（新增的）
        if (sellerOrderDetail.getReturnState() == 10) {
            sellerOrderDetail.setStepState(2);
        }
        // 待买家退货（新增的）
        if (sellerOrderDetail.getReturnState() == 11) {
            sellerOrderDetail.setStepState(11);
        }
        // 待验收（新增的）
        if (sellerOrderDetail.getReturnState() == 12) {
            sellerOrderDetail.setStepState(12);
        }
        // 线下支付订单平台待退款（新增的）
        if (sellerOrderDetail.getReturnState() == 17 && sellerOrderDetail.getPaymentCode() == 2) {
            sellerOrderDetail.setStepState(4);
        }
        // 换货买家确认收货 换货买家确认收货 换货部分发出换货物流 买家部分确认收货 换货关闭
        if (Arrays.asList(18, 19, 21, 22, 23).contains(sellerOrderDetail.getReturnState())) {
            sellerOrderDetail.setStepState(4);
        }
        // 返回处理后的退货详情
        return ResponseResult.success(sellerOrderDetail);
    }
    /**
     * 商家处理换货申请
     * 商家同意换货与否，type=1 同意，type=0 不同意
     */
    @Override
    public ResponseResult approveBarterAudit(ReturnOrderDTO dto) {
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(dto.getToken());
        dto.setOrderOperator(ui.getUserName());
        return approveBarter(dto);
    }
    /**
     * 商家处理换货申请
     * 商家同意换货与否，type=1 同意，type=0 不同意
     */
    public ResponseResult approveBarter(ReturnOrderDTO dto) {
        // 初始化订单商品信息映射
        Map<String, Object> orderGoodsMap = new HashMap<>();
        // 根据退货码查询退货状态
        Integer barterState = baseMapper.getBarterStateByReturnCode(dto.getReturnCode());
        // 如果当前退货状态不是待商家审核，则返回错误，表明状态变更无效
        if (barterState != null && !barterState.equals(CommonType.returnState.NEW_LAUNCH_PENDING_SELLER_REVIEW.getCode())) {
            return ResponseResult.fail(BizCodeEnume.ORDER_STATUS_CHANGE);
        }
        ResponseResult returnAddr = null;

        orderGoodsMap.put("orderOperator", dto.getOrderOperator());
        // 根据退货码查询订单号
        String orderId = orderReturnMapper.getOrderIdByReturnCode(dto.getReturnCode());
        OrderBaseEntity orderBaseEntity = orderBaseMapper.selectOne(new LambdaQueryWrapper<OrderBaseEntity>()
                .select(OrderBaseEntity::getBuyerUserId)
                .eq(OrderBaseEntity::getOrderId, orderId));
        // 如果提供了退货地址ID，则尝试获取详细的退货地址信息
        if (dto.getReturnAddrId() != null) {
            ManageShopAddressDetailDTO param = new ManageShopAddressDetailDTO();
            param.setShippingAddressId(dto.getReturnAddrId());
            returnAddr = merchantServiceClient.queryShopAddressDetail(param);
        }
        // 如果提供了退货地址ID，则尝试获取详细的退货地址信息
        if (dto.getStorageId() != null) {
            orderGoodsMap.put("storageId", dto.getStorageId());
            Map<String, Object> returnAddrData = goodServiceClient.queryStorageDetail(String.valueOf(dto.getStorageId())).getData();
            if (returnAddrData != null) {
                orderGoodsMap.put("returnRealName", returnAddrData.get("storageContacts"));
                orderGoodsMap.put("returnNumber", returnAddrData.get("storageMobile"));
            }
        }
        // 如果获取到退货地址信息，则填充到订单商品信息映射中
        if (returnAddr != null) {
            Map returnAddrData = (Map) returnAddr.getData();
            orderGoodsMap.put("returnRealName", returnAddrData.get("shippingAddressContact"));
            orderGoodsMap.put("returnNumber", returnAddrData.get("shippingAddressPhone"));
        }
        String returnGoodsName = orderReturnMapper.queryReturnGoodsNameByOrderId(orderId);
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", orderId);
        map.put("returnCode", dto.getReturnCode());
        map.put("goodsName", returnGoodsName);
        List<String> list = new ArrayList<>();
        list.add(orderId);
        map.put("userId", orderBaseEntity.getBuyerUserId());
        map.put("templateId", CommonType.messageTemplate.CODE_P.getCode());
        Map<String, Object> updateMap = new HashMap<>();
        if(StringUtils.isNotBlank(dto.getReturnRealName())){
            orderGoodsMap.put("returnRealName", dto.getReturnRealName());
        }
        if(StringUtils.isNotBlank(dto.getReturnNumber())){
            orderGoodsMap.put("returnNumber", dto.getReturnNumber());
        }
        // 商家同意退货
        if (1 == dto.getType()) {
            // 商家同意 设置退货处理相关的信息
            orderGoodsMap.put("returnCode", dto.getReturnCode());
            orderGoodsMap.put("returnShopHandle", 2);
            orderGoodsMap.put("returnState", 2);
            orderGoodsMap.put("returnAddrId", dto.getReturnAddrId());
            orderGoodsMap.put("returnAddr", dto.getReturnAddr());
            orderGoodsMap.put("returnAddrName", dto.getReturnAddrName());
            orderGoodsMap.put("returnShopMessage", dto.getReturnShopMessage());
            orderGoodsMap.put("returnShopTime", new Date());
            // 设置退货处理的过期时间
            Calendar instance = Calendar.getInstance();
            instance.setTime(new Date());
            instance.add(Calendar.DATE, 7);
            Date expiration_time = instance.getTime();
            orderGoodsMap.put("expirationTime", expiration_time);
            list.add("同意");
            map.put("result", "同意");
            map.put("status", "商家已同意换货，请尽快填写换货物流信息");

            updateMap.put("orderId", orderId);
            updateMap.put("orderRefundStatus", "0");
            updateMap.put("orderReturnStatus", "0");
            updateMap.put("orderBarterStatus", "1");
            // 更新订单状态
            Integer torOne = orderBaseMapper.updateOrderBaseById(updateMap);
            // 如果更新失败，则回滚事务
            if (1 != torOne) {
                // 回滚
                throw new DefinedException(BizCodeEnume.APPROVE_FAILED);
            }
        } else {
            list.add("不同意");
            map.put("result", "不同意");
            map.put("status", "您提交的换货订单，商家未同意换货");
            // 商家拒绝退货
            // 商家不同意
            orderGoodsMap.put("returnCode", dto.getReturnCode());
            orderGoodsMap.put("returnShopHandle", 3);
            orderGoodsMap.put("returnState", 3);
            orderGoodsMap.put("returnShopMessage", dto.getReturnShopMessage());
            orderGoodsMap.put("returnAddrId", dto.getReturnAddrId());
            orderGoodsMap.put("returnAddr", dto.getReturnAddr());
            orderGoodsMap.put("returnAddrName", dto.getReturnAddrName());
            orderGoodsMap.put("returnShopTime", new Date());
            orderGoodsMap.put("orderId", orderId);
            // 设置拒绝退货的过期时间
            Calendar instance = Calendar.getInstance();
            instance.setTime(new Date());
            instance.add(Calendar.DATE, 2);
            Date expiration_time = instance.getTime();
            orderGoodsMap.put("expirationTime", expiration_time);
            // 更新订单基础信息为拒绝退货状态
            orderReturnMapper.updateOrderBaseById1(orderGoodsMap);

            updateMap.put("orderId", orderId);
            updateMap.put("orderRefundStatus", "0");
            updateMap.put("orderReturnStatus", "0");
            updateMap.put("orderBarterStatus", "3");
            // 更新订单状态
            Integer torOne = orderBaseMapper.updateOrderBaseById(updateMap);
            // 如果更新失败，则回滚事务
            if (1 != torOne) {
                // 回滚
                throw new DefinedException(BizCodeEnume.APPROVE_FAILED);
            }
            dto.setOrderId(orderId);
            List<WapGoodsSaleSumDTO> returnGoods = orderGoodsMapper.queryReturnGoodsToSale(dto);
            goodServiceClient.updateGoodsStock(returnGoods);
        }
        // 发送消息
        map.put("list", list);
        baseServiceClient.sendMessageTemplate(map);
        // 更新退货信息
        Integer flag = orderReturnMapper.updateOrderReturnById(orderGoodsMap);
        // 如果更新成功，则返回成功结果，否则回滚事务并返回失败结果
        if (flag > 0) {
            // 更新子订单状态
            List<OrderBaseEntity> orderBaseEntities = orderBaseMapper.selectList(new LambdaQueryWrapper<OrderBaseEntity>()
                    .eq(OrderBaseEntity::getOriginalOrder, orderId));
            for (OrderBaseEntity entity : orderBaseEntities) {
                OrderReturnEntity orderReturnEntity = orderReturnMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>()
                        .eq(OrderReturnEntity::getOrderId, entity.getOrderId()));
                if (null != orderReturnEntity) {
                    // 更新订单基础信息为拒绝退货状态
                    orderGoodsMap.put("returnCode", orderReturnEntity.getReturnCode());
                    orderReturnMapper.updateOrderReturnById(orderGoodsMap);
                }
                updateMap.put("orderId", entity.getOrderId());
                orderBaseMapper.updateOrderBaseById(updateMap);
                if(1 != dto.getType()) {
                    dto.setOrderId(entity.getOrderId());
                    List<WapGoodsSaleSumDTO> returnGoods = orderGoodsMapper.queryReturnGoodsToSale(dto);
                    goodServiceClient.updateGoodsStock(returnGoods);
                }
            }
            return ResponseResult.success();
        }
        // 回滚
        throw new DefinedException(BizCodeEnume.APPROVE_FAILED);
    }

    /**
     * 换货买家填写物流信息
     */
    @Override
    public ResponseResult barterShipping(ReturnOrderDTO dto) {
        Map<String, Object> orderGoodsMap = new HashMap<>();
        // 根据退货编码查询订单ID
        String orderId = orderReturnMapper.queryreturnCodeByOrderId(dto.getReturnCode());
        // 根据订单ID查询退货详情
        //查询退款详情
        ReturnOrderDetailVO returnOrderDetail = orderReturnMapper.queryOrderReturnDetail(orderId);
        // 如果退货状态为10（待处理），则更新重发次数
        if (Integer.parseInt(returnOrderDetail.getReturnState()) == 10) {
            Integer resubmissionsNumber = returnOrderDetail.getResubmissionsNumber() + 1;
            orderGoodsMap.put("resubmissionsNumber", resubmissionsNumber);
        }
        // 查询物流名称
        ResponseResult express = baseServiceClient.getBaseExpressInfoById(dto.getReturnPostCode());
        String expName = JSON.parseObject(JSON.toJSONString(express.getData()), BaseExpressDTO.class).getExpressName();
        // 将退货相关的信息放入orderGoodsMap中，用于后续更新数据库
        orderGoodsMap.put("returnCode", dto.getReturnCode());
        // 退货物流单号
        orderGoodsMap.put("returnShippingCode", dto.getReturnShippingCode());
        // 联系电话
        orderGoodsMap.put("returnMobile", dto.getReturnMobile());
        // 物流id
        orderGoodsMap.put("returnPostCode", dto.getReturnPostCode());
        // 设置退货处理状态和退货商店处理意见
        orderGoodsMap.put("returnShopHandle", 6);
        // 订单状态
        orderGoodsMap.put("returnState", "6");
        orderGoodsMap.put("callStatus", 0);
        // 设置退货处理的过期时间，为当前时间后7天
        Calendar instance = Calendar.getInstance();
        instance.setTime(new Date());
        instance.add(Calendar.DATE, 7);
        Date instanceTime = instance.getTime();
        orderGoodsMap.put("expirationTime", instanceTime);
        // 更新退货信息，并根据更新结果返回相应的响应
        Integer torf = orderReturnMapper.updateOrderReturnById(orderGoodsMap);
        if (1 == torf) {
            //根据订单id查子订单
            List<OrderBaseEntity> orderBaseEntities = orderBaseMapper.selectList(new LambdaQueryWrapper<OrderBaseEntity>()
                    .eq(OrderBaseEntity::getOriginalOrder, orderId));
            if (!orderBaseEntities.isEmpty()) {
                for (OrderBaseEntity entity : orderBaseEntities) {
                    //子订单退款记录
                    OrderReturnEntity orderReturnEntity = orderReturnMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>()
                            .eq(OrderReturnEntity::getOrderId, entity.getOrderId()));
                    if (null != orderReturnEntity) {
                        orderGoodsMap.put("returnCode", orderReturnEntity.getReturnCode());
                        // 更新子订单退货信息，并根据更新结果返回相应的响应
                        orderReturnMapper.updateOrderReturnById(orderGoodsMap);
                    }
                }
            }
            //查询怡亚通订单信息
            OrderYiyatongEntity orderYiyatongEntity = orderYiyatongMapper.selectOne(new LambdaQueryWrapper<OrderYiyatongEntity>()
                    .eq(OrderYiyatongEntity::getOrderId, orderId));
            if (null != orderYiyatongEntity) {
                SendReturnGoodsDTO sendReturnGoodsDTO = new SendReturnGoodsDTO();
                sendReturnGoodsDTO.setReturnSn(orderYiyatongEntity.getReturnSn());
                sendReturnGoodsDTO.setDeliveryCorpName(expName);
                sendReturnGoodsDTO.setDeliverySn(dto.getReturnShippingCode());
                ResponseResult yiyatongResult = goodServiceClient.sendReturnGoods(sendReturnGoodsDTO);
                if (yiyatongResult.getCode() == CommonStatusEnum.FAIL.getCode()) {
                    return yiyatongResult;
                }
            }
            // 判断是否是京东订单
            if(returnOrderDetail.getOrderSource() == CommonType.SupplierSource.JING_DONG.getCode()){
                // 封装商城退单商品信息
                JingDongReturnOrderDTO jingDongReturnOrderDTO = new JingDongReturnOrderDTO();
                jingDongReturnOrderDTO.setReturnShippingCode(dto.getReturnShippingCode());
                //查询快递公司名字
                jingDongReturnOrderDTO.setExpressName(expName);
                // 根据订单号查询京东售后单信息
                List<JingDongReturnGoodsDTO> jingDongReturnGoodsDTOS = orderGoodsMapper.queryJdReturnOrderGoodsInfo(orderId);
                jingDongReturnOrderDTO.setGoodsList(jingDongReturnGoodsDTOS);
                // 调用京东接口 填写运单信息
                goodServiceClient.updateJdShippingInfo(jingDongReturnOrderDTO);
            }

            return ResponseResult.success();
        }
        return ResponseResult.fail(BizCodeEnume.FILLING_LOGISTICS_ERROR);
    }

    /**
     * 换货-买家未填写物流信息时商家确认收货
     */
    @Override
    public ResponseResult barterShopShipping(ReturnOrderDTO dto) {
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(dto.getToken());
        // 更新paraMap，添加shopId和shopType用于后续操作
        dto.setShopId(Integer.valueOf(ui.getShopId()));
        dto.setShopType(ui.getShopType());

        // 根据返回码获取订单ID
        String orderId = orderReturnMapper.getOrderIdByReturnCode(dto.getReturnCode());
        // 初始化订单商品和基础信息的映射
        Map<String, Object> orderGoodsMap = new HashMap<>();

        setConfirmReceipt(orderGoodsMap, dto, orderId);
        Integer torf = orderReturnMapper.updateOrderReturnById(orderGoodsMap);
        if (1 == torf) {
            // 根据主订单id获取子订单ID
            List<OrderBaseEntity> list = orderBaseMapper.selectList(new LambdaQueryWrapper<OrderBaseEntity>()
                    .eq(OrderBaseEntity::getOriginalOrder, orderId));

            if (!list.isEmpty()) {
                for (OrderBaseEntity entity : list) {
                    OrderReturnEntity orderReturnEntity = orderReturnMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>()
                            .eq(OrderReturnEntity::getOrderId, entity.getOrderId()));
                    if (null != orderReturnEntity) {
                        dto.setReturnCode(orderReturnEntity.getReturnCode());
                        barterShopShipping(dto);
                    }
                }
            }
            return ResponseResult.success();
        }
        throw new DefinedException(BizCodeEnume.APPROVE_FAILED);
    }
    private void setConfirmReceipt2(Map<String, Object> orderGoodsMap, ReturnOrderDTO dto, String orderId) {
        // 商家同意收货处理
        Integer storageId = orderReturnMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>()
                .eq(OrderReturnEntity::getOrderId, orderId)).getStorageId();
        List<StorageInventoryFlowGoodsDTO> storageGoodsList = new ArrayList<>();
        // 设置订单商品的返回状态及相关信息
        orderGoodsMap.put("returnCode", dto.getReturnCode());
        orderGoodsMap.put("returnShopHandle", 4);
        orderGoodsMap.put("returnState", 4);
        orderGoodsMap.put("returnShopTime", new Date());
        orderGoodsMap.put("returnShopMessage", dto.getReturnShopMessage());
        orderGoodsMap.put("returnFinishTime", new Date());
        orderGoodsMap.put("storageId", storageId);

        Map<String, Object> goodsNumMap = new HashMap<>();
        List<Map<String, Object>> goodsNum = orderGoodsMapper.queryBuyNumByorderId(orderId);
        goodsNumMap.put("goodsNum", goodsNum);
        //退款更新商品销量
        goodServiceClient.updateGoodsSaleNum(goodsNumMap);
        for (Map<String, Object> map : goodsNum) {
            StorageInventoryFlowGoodsDTO storageInventoryFlowGoodsDTO = new StorageInventoryFlowGoodsDTO();
            storageInventoryFlowGoodsDTO.setGoodsId(Integer.parseInt(map.get("goods_id").toString()));
            storageInventoryFlowGoodsDTO.setGoodsNum(Integer.parseInt(map.get("order_goods_returnnum").toString()));
            storageInventoryFlowGoodsDTO.setBuyNum(Integer.parseInt(map.get("order_goods_returnnum").toString()));
            storageGoodsList.add(storageInventoryFlowGoodsDTO);
        }
        Map<String, String> map = new HashMap<>();
        map.put("shopId",dto.getShopId().toString());
        map.put("userId",dto.getUserId());
        map.put("userName",dto.getUserName());
        Integer inventoryFlowType = CommonType.inventoryFlowType.PUT_STORAGE.getCode();
        Integer inventoryFlowTypeDetail = CommonType.inventoryFlowTypeDetail.RETURN_TO_INVENTORY_AND_STORAGE.getCode();
        setStorageInventoryFlow2(storageId, orderId, inventoryFlowType, inventoryFlowTypeDetail, storageGoodsList,
                goodServiceClient, baseServiceClient, null, map);
        //子订单
        List<OrderBaseEntity> originalOrderList = orderBaseMapper.selectList(new LambdaQueryWrapper<OrderBaseEntity>()
                .eq(OrderBaseEntity::getOriginalOrder, orderId));
        updateOriginalOrderGoodsSale(originalOrderList);
        //退款完成时间
        orderGoodsMap.put("returnFinishTime", LocalDateTime.now());
    }

    private void setConfirmReceipt(Map<String, Object> orderGoodsMap, ReturnOrderDTO dto, String orderId) {
        // 商家同意收货处理
        Integer storageId = orderReturnMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>()
                .eq(OrderReturnEntity::getOrderId, orderId)).getStorageId();
        List<StorageInventoryFlowGoodsDTO> storageGoodsList = new ArrayList<>();
        // 设置订单商品的返回状态及相关信息
        orderGoodsMap.put("returnCode", dto.getReturnCode());
        orderGoodsMap.put("returnShopHandle", 4);
        orderGoodsMap.put("returnState", 4);
        orderGoodsMap.put("returnShopTime", new Date());
        orderGoodsMap.put("returnShopMessage", dto.getReturnShopMessage());
        orderGoodsMap.put("returnFinishTime", new Date());
        orderGoodsMap.put("storageId", storageId);

        Map<String, Object> goodsNumMap = new HashMap<>();
        List<Map<String, Object>> goodsNum = orderGoodsMapper.queryBuyNumByorderId(orderId);
        goodsNumMap.put("goodsNum", goodsNum);
        //退款更新商品销量
        goodServiceClient.updateGoodsSaleNum(goodsNumMap);
        for (Map<String, Object> map : goodsNum) {
            StorageInventoryFlowGoodsDTO storageInventoryFlowGoodsDTO = new StorageInventoryFlowGoodsDTO();
            storageInventoryFlowGoodsDTO.setGoodsId(Integer.parseInt(map.get("goods_id").toString()));
            storageInventoryFlowGoodsDTO.setGoodsNum(Integer.parseInt(map.get("order_goods_returnnum").toString()));
            storageInventoryFlowGoodsDTO.setBuyNum(Integer.parseInt(map.get("order_goods_returnnum").toString()));
            storageGoodsList.add(storageInventoryFlowGoodsDTO);
        }
        Integer inventoryFlowType = CommonType.inventoryFlowType.PUT_STORAGE.getCode();
        Integer inventoryFlowTypeDetail = CommonType.inventoryFlowTypeDetail.RETURN_TO_INVENTORY_AND_STORAGE.getCode();
        setStorageInventoryFlow(storageId, orderId, inventoryFlowType, inventoryFlowTypeDetail, storageGoodsList,
                goodServiceClient, baseServiceClient, null, dto.getToken());
        //子订单
        List<OrderBaseEntity> originalOrderList = orderBaseMapper.selectList(new LambdaQueryWrapper<OrderBaseEntity>()
                .eq(OrderBaseEntity::getOriginalOrder, orderId));
        updateOriginalOrderGoodsSale(originalOrderList);
        //退款完成时间
        orderGoodsMap.put("returnFinishTime", LocalDateTime.now());
    }

    /**
     * 换货-商家确认收货
     */
    @Override
    public ResponseResult shopConfirmReceipt(ReturnOrderDTO dto) {
        if (dto.getIsJob() == null || !dto.getIsJob()) {
            if(null != dto.getToken()){
                UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(dto.getToken());
                // 更新paraMap，添加shopId和shopType用于后续操作
                dto.setShopId(Integer.valueOf(ui.getShopId()));
                dto.setShopType(ui.getShopType());
                dto.setUserId(ui.getUserId());
                dto.setUserName(ui.getUserName());
            }
        }

        // 根据返回码获取订单ID
        String orderId = orderReturnMapper.getOrderIdByReturnCode(dto.getReturnCode());
        // 初始化订单商品和基础信息的映射
        Map<String, Object> orderGoodsMap = new HashMap<>();

        // 商家同意收货处理
        if (1 == dto.getType()) {
            setConfirmReceipt2(orderGoodsMap, dto, orderId);
        } else {
            // 商家不同意
            orderGoodsMap.put("returnCode", dto.getReturnCode());
            orderGoodsMap.put("returnShopMessage", dto.getReturnShopMessage());
            orderGoodsMap.put("returnShopHandle", 10);
            orderGoodsMap.put("returnState", 10);
            orderGoodsMap.put("returnShopTime", new Date());
            Calendar instance = Calendar.getInstance();
            instance.setTime(new Date());
            instance.add(Calendar.DATE, 2);
            Date expirationTime = instance.getTime();
            orderGoodsMap.put("expirationTime", expirationTime);

            // 更新订单基础信息为拒绝收货状态
            Map<String, Object> updateMap = new HashMap<>();
            updateMap.put("orderId", orderId);
            updateMap.put("orderReturnStatus", "0");
            updateMap.put("orderRefundStatus", "0");
            updateMap.put("orderBarterStatus", "3");
            // 更新订单商品信息为拒绝收货状态
            orderBaseMapper.updateOrderBaseById(updateMap);
            Map<String, Object> updateGoodsMap = new HashMap<>();

            //查询用户售后商品
            List<Integer> returnGoods = orderGoodsMapper.queryReturnGoods(orderId);

            updateGoodsMap.put("orderId", orderId);
            updateGoodsMap.put("goodsReturnStatus", 0);
            updateGoodsMap.put("orderRefundStatus", 0);
            updateGoodsMap.put("goodsBarterStatus", 3);
            for (Integer returnGood : returnGoods) {
                updateGoodsMap.put("goodsId", returnGood);
                orderReturnMapper.updateOrderGoodsById1(updateGoodsMap);
            }
        }
        Integer torf = orderReturnMapper.updateOrderReturnById(orderGoodsMap);
        if (1 == torf) {
            // 根据主订单id获取子订单ID
            List<OrderBaseEntity> list = orderBaseMapper.selectList(new LambdaQueryWrapper<OrderBaseEntity>()
                    .eq(OrderBaseEntity::getOriginalOrder, orderId));

            if (!list.isEmpty()) {
                for (OrderBaseEntity entity : list) {
                    OrderReturnEntity orderReturnEntity = orderReturnMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>()
                            .eq(OrderReturnEntity::getOrderId, entity.getOrderId()));
                    if (null != orderReturnEntity) {
                        dto.setReturnCode(orderReturnEntity.getReturnCode());
                        shopConfirmReceipt(dto);
                    }
                }
            }
            return ResponseResult.success();
        }
        // 回滚
        throw new DefinedException(BizCodeEnume.APPROVE_FAILED);
    }

    /**
     * 换货-商家发起换货物流
     * 当商家需要发起换货物流时调用此方法，处理换货订单的创建和状态更新
     *
     * @param dto 包含换货订单信息的DTO
     * @return 返回操作结果
     */
    @Override
    public ResponseResult shopInitiateLogistics(ReturnOrderDTO dto) {
        // 获取当前商家信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(dto.getToken());
        // 计算商家ID
        Integer shopId = calculateShopId(dto, ui);

        // 查询退换货订单信息
        OrderReturnEntity orderReturnEntity = orderReturnMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>()
                .select(OrderReturnEntity::getOrderId, OrderReturnEntity::getOrderReturnId)
                .eq(OrderReturnEntity::getReturnCode, dto.getReturnCode()));
        // 获取订单ID
        String orderId = orderReturnEntity.getOrderId();

        // 创建换货订单实体
        OrderBarterEntity orderBarterEntity = getOrderBarterEntity(dto, orderReturnEntity, shopId);
        // 插入换货订单信息到数据库
        baseMapper.insert(orderBarterEntity);

        // 查询符合条件的商品ID列表
        List<Integer> goodsIdList = orderGoodsMapper.selectList(new LambdaQueryWrapper<OrderGoodsEntity>()
                .select(OrderGoodsEntity::getGoodsId)
                .eq(OrderGoodsEntity::getOrderId, orderId)
                .eq(OrderGoodsEntity::getShopId, shopId)
                .eq(ui.getShopType() == 1,OrderGoodsEntity::getDeliverShopId, shopId)
                .eq(ui.getShopType() == 2 && dto.getShopType() != null && dto.getShopType() == 1,
                        OrderGoodsEntity::getDropShipping, 1)
                .in(OrderGoodsEntity::getGoodsBarterStatus, Arrays.asList(1, 3))).stream().map(OrderGoodsEntity::getGoodsId).toList();

        // 更新订单商品的换货订单ID
        orderGoodsMapper.update(null, new LambdaUpdateWrapper<OrderGoodsEntity>()
                .set(OrderGoodsEntity::getOrderBarterId, orderBarterEntity.getOrderBarterId())
                .eq(OrderGoodsEntity::getOrderId, orderId)
                .in(OrderGoodsEntity::getGoodsId, goodsIdList));

        // 查询该订单下所有商品的数量
        Integer allGoodsCount = orderGoodsMapper.queryOrderReturnGoodsCount(orderId);
        // 查询该订单下已换货商品的数量
        Integer barterGoodsCount = Math.toIntExact(orderGoodsMapper.selectCount(new LambdaQueryWrapper<OrderGoodsEntity>()
                .eq(OrderGoodsEntity::getOrderId, orderId)
                .ne(OrderGoodsEntity::getGoodsBarterStatus, CommonType.orderBarterStatus.NO_BARTER.getCode())
                .isNotNull(OrderGoodsEntity::getOrderBarterId)));

        // 更新退换货订单的状态
        orderReturnMapper.update(null, new LambdaUpdateWrapper<OrderReturnEntity>()
                .set(OrderReturnEntity::getReturnState, Objects.equals(allGoodsCount, barterGoodsCount) ?
                        CommonType.returnState.SEND_BARTER_LOGISTICS.getCode() : CommonType.returnState.SEND_PART_BARTER_LOGISTICS.getCode())
                .eq(OrderReturnEntity::getReturnCode, dto.getReturnCode()));

        // 如果当前商家类型为供应商家且DTO中的商家类型为空，则同步更新商家订单
        if (Objects.equals(ui.getShopType(), CommonType.ShopType.SUPPLY_SHOP.getCode()) && dto.getShopType() == null) {
            // 查询原始订单信息
            OrderBaseEntity orderBaseEntity = orderBaseMapper.selectOne(new LambdaQueryWrapper<OrderBaseEntity>()
                    .select(OrderBaseEntity::getOriginalOrder)
                    .eq(OrderBaseEntity::getOrderId, orderId));

            // 查询商家的退换货订单信息
            OrderReturnEntity orderReturnShopEntity = orderReturnMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>()
                    .eq(OrderReturnEntity::getOrderId, orderBaseEntity.getOriginalOrder()));

            // 更新DTO信息
            dto.setReturnCode(orderReturnShopEntity.getReturnCode());
            dto.setShopType(CommonType.ShopType.COMMON_SHOP.getCode());
            dto.setShopId(orderReturnShopEntity.getShopId());

            // 递归调用当前方法，处理商家的换货物流
            shopInitiateLogistics(dto);
        }

        // 返回成功结果
        return ResponseResult.success();
    }
    /**
     * 换货-京东发起换货物流
     * 当商家需要发起换货物流时调用此方法，处理换货订单的创建和状态更新
     *
     * @param map 包含换货订单信息的DTO
     * @return 返回操作结果
     */
    @Override
    public ResponseResult jingDongShopInitiateLogistics(Map<String,Object> map) {
        List<JingDongReturnOrderDTO> list = JsonUtils.string2Obj(JsonUtils.toString(map.get("orderIdList")), List.class, JingDongReturnOrderDTO.class);
        for (JingDongReturnOrderDTO jingDongReturnOrderDTO : list) {
            // 根据京东退单申请单号查询 商城订单号
            String orderId = orderReturnMapper.queryOrderIdByJDReturnOrderId(jingDongReturnOrderDTO.getJdReturenOrderId());
            // 根据京东订单号查询 商城订单信息
            ReturnOrderVO returnOrderVO = orderReturnMapper.queryReturnOrderByJDOrderId(orderId);

            ReturnOrderDTO dto = new ReturnOrderDTO();
            dto.setOrderId(returnOrderVO.getOrderId());
            dto.setReturnCode(returnOrderVO.getReturnCode());
            dto.setReturnShippingCode(jingDongReturnOrderDTO.getReturnShippingCode());
            dto.setReturnPostCode(0);
            dto.setBarterPostName(jingDongReturnOrderDTO.getExpressName());
            dto.setReturnNumber(returnOrderVO.getOrderReceiverContact());


            Integer shopId = dto.getShopId();

            // 查询退换货订单信息
            OrderReturnEntity orderReturnEntity = orderReturnMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>()
                    .select(OrderReturnEntity::getOrderId, OrderReturnEntity::getOrderReturnId)
                    .eq(OrderReturnEntity::getReturnCode, dto.getReturnCode()));

            // 创建换货订单实体
            OrderBarterEntity orderBarterEntity = getOrderBarterEntity(dto, orderReturnEntity, shopId);
            // 插入换货订单信息到数据库
            baseMapper.insert(orderBarterEntity);

            // 查询符合条件的商品ID列表
            List<Integer> goodsIdList = orderGoodsMapper.selectList(new LambdaQueryWrapper<OrderGoodsEntity>()
                    .select(OrderGoodsEntity::getGoodsId)
                    .eq(OrderGoodsEntity::getOrderId, orderId)
                    .eq(OrderGoodsEntity::getShopId, shopId)
                    .in(OrderGoodsEntity::getGoodsBarterStatus, Arrays.asList(1, 3))).stream().map(OrderGoodsEntity::getGoodsId).toList();

            // 更新订单商品的换货订单ID
            orderGoodsMapper.update(null, new LambdaUpdateWrapper<OrderGoodsEntity>()
                    .set(OrderGoodsEntity::getOrderBarterId, orderBarterEntity.getOrderBarterId())
                    .eq(OrderGoodsEntity::getOrderId, orderId)
                    .in(OrderGoodsEntity::getGoodsId, goodsIdList));

            // 查询该订单下所有商品的数量
            Integer allGoodsCount = orderGoodsMapper.queryOrderReturnGoodsCount(orderId);
            // 查询该订单下已换货商品的数量
            Integer barterGoodsCount = Math.toIntExact(orderGoodsMapper.selectCount(new LambdaQueryWrapper<OrderGoodsEntity>()
                    .eq(OrderGoodsEntity::getOrderId, orderId)
                    .ne(OrderGoodsEntity::getGoodsBarterStatus, CommonType.orderBarterStatus.NO_BARTER.getCode())
                    .isNotNull(OrderGoodsEntity::getOrderBarterId)));

            // 更新退换货订单的状态
            orderReturnMapper.update(null, new LambdaUpdateWrapper<OrderReturnEntity>()
                    .set(OrderReturnEntity::getReturnState, Objects.equals(allGoodsCount, barterGoodsCount) ?
                            CommonType.returnState.SEND_BARTER_LOGISTICS.getCode() : CommonType.returnState.SEND_PART_BARTER_LOGISTICS.getCode())
                    .eq(OrderReturnEntity::getReturnCode, dto.getReturnCode()));

        }
        // 返回成功结果
        return ResponseResult.success();
    }

    /**
     * 计算并返回店铺ID
     * 此方法用于决定是使用ReturnOrderDTO中的店铺ID还是UserInfoVO中的店铺ID
     * 它基于ReturnOrderDTO中的店铺类型是否为普通店铺来做出决定
     *
     * @param dto ReturnOrderDTO对象，包含退货订单的相关信息
     * @param ui  UserInfoVO对象，包含用户信息
     * @return 根据条件返回相应的店铺ID
     */
    private Integer calculateShopId(ReturnOrderDTO dto, UserInfoVO ui) {
        // 根据dto中的店铺类型是否为普通店铺，决定返回哪个店铺ID
        return (dto.getShopType() != null && dto.getShopType().equals(CommonType.ShopType.COMMON_SHOP.getCode()))
                ? dto.getShopId()
                : Integer.parseInt(ui.getShopId());
    }

    /**
     * 根据退货订单DTO和订单退货实体创建并返回一个订单置换实体
     * 此方法用于处理订单置换逻辑，将退货订单的相关信息转换为订单置换实体，以便进行后续的置换处理
     *
     * @param dto               退货订单DTO，包含用户提交的退货相关信息
     * @param orderReturnEntity 订单退货实体，包含订单退货的详细信息
     * @param shopId            店铺ID，标识店铺
     * @return 返回一个初始化后的OrderBarterEntity对象，表示一个订单置换实体
     */
    private static OrderBarterEntity getOrderBarterEntity(ReturnOrderDTO dto, OrderReturnEntity orderReturnEntity, Integer shopId) {
        // 创建一个新的订单置换实体对象
        OrderBarterEntity orderBarterEntity = new OrderBarterEntity();

        // 设置订单ID
        orderBarterEntity.setOrderId(orderReturnEntity.getOrderId());
        // 设置店铺ID
        orderBarterEntity.setShopId(shopId);
        // 设置订单退货ID
        orderBarterEntity.setOrderReturnId(orderReturnEntity.getOrderReturnId());
        // 设置置换货品的物流编码
        orderBarterEntity.setBarterShippingCode(dto.getReturnShippingCode());
        // 设置置换货品的邮编
        orderBarterEntity.setBarterPostCode(dto.getReturnPostCode());
        // 换货公司名称
        orderBarterEntity.setBarterPostName(dto.getBarterPostName());
        // 设置置换货品的手机号
        orderBarterEntity.setBarterMobile(dto.getReturnMobile());
        // 设置退货编码
        orderBarterEntity.setReturnCode(dto.getReturnCode());
        // 设置创建时间
        orderBarterEntity.setCreateTime(DateUtils.getNowDateToDf());
        // 设置置换状态为已发送置换物流
        orderBarterEntity.setBarterStatus(CommonType.barterStatus.SEND_BARTER_LOGISTICS.getCode());

        // 返回初始化后的订单置换实体对象
        return orderBarterEntity;
    }

    /**
     * 换货-买家确认收货
     */
    @Override
    public ResponseResult buyerBarterConfirmReceipt(ReturnOrderDTO dto) {
        // 根据退换货代码查询订单信息
        OrderReturnEntity orderReturnEntity = orderReturnMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>()
                .select(OrderReturnEntity::getOrderId, OrderReturnEntity::getReturnCode)
                .eq(OrderReturnEntity::getReturnCode, dto.getReturnCode()));

        // 更新换货状态为买家确认收货，并设置配送时间
        baseMapper.update(null, new LambdaUpdateWrapper<OrderBarterEntity>()
                .set(OrderBarterEntity::getBarterStatus, CommonType.barterStatus.BUYER_BARTER_CONFIRM_RECEIPT.getCode())
                .set(OrderBarterEntity::getDeliveryTime, DateUtils.getNowDateToDf())
                .in(OrderBarterEntity::getOrderBarterId, dto.getOrderBarterIdList()));

        // 查询用户售后商品
        dto.setOrderId(orderReturnEntity.getOrderId());
        List<WapGoodsSaleSumDTO> returnGoods = orderGoodsMapper.queryReturnGoodsToSale(dto);
        List<Integer> returnGoodsId = returnGoods.stream().map(WapGoodsSaleSumDTO::getGoodsId).collect(Collectors.toList());
        // 更新商品换货状态为换货完成
        orderGoodsMapper.update(null, new LambdaUpdateWrapper<OrderGoodsEntity>()
                .set(OrderGoodsEntity::getGoodsBarterStatus, CommonType.orderBarterStatus.BARTER_COMPLETED.getCode())
                .in(OrderGoodsEntity::getGoodsId, returnGoodsId)
                .eq(OrderGoodsEntity::getOrderId, orderReturnEntity.getOrderId()));

        // 查询订单中所有商品的数量
        Integer allGoodsCount = orderGoodsMapper.queryOrderReturnGoodsCount(orderReturnEntity.getOrderId());
        // 计算已完成换货的商品数量
        Integer barterGoodsCount = Math.toIntExact(orderGoodsMapper.selectCount(new LambdaQueryWrapper<OrderGoodsEntity>()
                .eq(OrderGoodsEntity::getOrderId, orderReturnEntity.getOrderId())
                .eq(OrderGoodsEntity::getGoodsBarterStatus, CommonType.orderBarterStatus.BARTER_COMPLETED.getCode())));

        // 更新订单退换货状态，根据是否所有商品都已完成换货
        orderReturnMapper.update(null, new LambdaUpdateWrapper<OrderReturnEntity>()
                .set(OrderReturnEntity::getReturnState, Objects.equals(allGoodsCount, barterGoodsCount) ?
                        CommonType.returnState.BUYER_BARTER_CONFIRM_RECEIPT.getCode() : CommonType.returnState.BUYER_PART_BARTER_CONFIRM_RECEIPT.getCode())
                .set(allGoodsCount.equals(barterGoodsCount), OrderReturnEntity::getReturnFinishTime, DateUtils.getNowDateToDf())
                .eq(OrderReturnEntity::getReturnCode, dto.getReturnCode()));

        // 如果所有商品都已完成换货，更新订单状态为换货完成
        if (allGoodsCount.equals(barterGoodsCount)) {
            orderBaseMapper.update(null, new LambdaUpdateWrapper<OrderBaseEntity>()
                    .set(OrderBaseEntity::getOrderBarterStatus, CommonType.orderBarterStatus.BARTER_COMPLETED.getCode())
                    .eq(OrderBaseEntity::getOrderId, orderReturnEntity.getOrderId()));
        }

        // 同步更新子订单
        List<OrderBaseEntity> orderBaseEntities = orderBaseMapper.selectList(new LambdaQueryWrapper<OrderBaseEntity>()
                .eq(OrderBaseEntity::getOriginalOrder, orderReturnEntity.getOrderId()));
        if (!orderBaseEntities.isEmpty()) {
            for (OrderBaseEntity entity : orderBaseEntities) {
                OrderReturnEntity orderReturnSupplierEntity = orderReturnMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>()
                        .eq(OrderReturnEntity::getOrderId, entity.getOrderId()));
                if (null != orderReturnSupplierEntity) {
                    List<Integer> orderBarterIdList = baseMapper.selectList(new LambdaQueryWrapper<OrderBarterEntity>()
                                    .eq(OrderBarterEntity::getOrderId, entity.getOrderId())
                                    .eq(OrderBarterEntity::getBarterStatus, CommonType.barterStatus.SEND_BARTER_LOGISTICS.getCode())
                                    .select(OrderBarterEntity::getOrderBarterId))
                            .stream().map(OrderBarterEntity::getOrderBarterId).toList();
                    if (!orderBarterIdList.isEmpty()) {
                        dto.setOrderBarterIdList(orderBarterIdList);
                        dto.setOrderId(entity.getOrderId());
                        dto.setReturnCode(orderReturnSupplierEntity.getReturnCode());
                        buyerBarterConfirmReceipt(dto);
                    }
                }
            }
        }
        OrderBaseEntity orderBase = orderBaseMapper.selectOne(new LambdaQueryWrapper<OrderBaseEntity>()
                .eq(OrderBaseEntity::getOrderId, orderReturnEntity.getOrderId()));
        // 判断是否是京东订单
        if(orderBase.getOrderSource() == CommonType.SupplierSource.JING_DONG.getCode()){
            List<JingDongReturnGoodsDTO> jingDongReturnGoodsDTOS = orderGoodsMapper.queryJdReturnOrderGoodsInfo(orderBase.getOrderId());
            // 根据 JdReturenOrderId 分组
            Map<String, List<JingDongReturnGoodsDTO>> groupedByJdReturenOrderId = jingDongReturnGoodsDTOS.stream()
                    .collect(Collectors.groupingBy(JingDongReturnGoodsDTO::getJdReturenOrderId));
            // 处理分组后的数据
            for (Map.Entry<String, List<JingDongReturnGoodsDTO>> entry : groupedByJdReturenOrderId.entrySet()) {
                List<JingDongReturnGoodsDTO> items = entry.getValue();
                for (JingDongReturnGoodsDTO item : items) {
                    // 调用京东 京东 确认售后单
                    goodServiceClient.confirmJingDongRetunrOrde(item.getOrderId(),item.getJdReturenOrderId());
                }
            }
        }

        return ResponseResult.success();
    }

    /**
     * 换货-平台查询换货订单列表
     */
    @Override
    public ResponseResult selectBarterOrderListToPlatform(ReturnOrderDTO dto) {
        // 根据更新后的参数查询换货订单详情
        Page<ReturnOrderDetailVO> page = new Page<>(dto.getPage(), dto.getLimit());
        IPage<ReturnOrderDetailVO> result = this.baseMapper.selectBarterOrderListToPlatform(page, dto);
        // 如果查询结果不为空，对每个订单进行脱敏处理
        if (!result.getRecords().isEmpty()) {
            for (ReturnOrderDetailVO detailVO : result.getRecords()) {
                BeanUtils.copyProperties(DesensitizationUtil.Desensitization(detailVO, ReturnOrderDetailVO.class), detailVO);
            }
        }
        // 返回成功的响应结果，其中包含退货订单详情和总数
        return ResponseResult.success(PageUtils.getPage(result));
    }

    @Override
    public ResponseResult exportBarterOrderList(ReturnOrderDTO dto) {
        Page<ReturnOrderDetailVO> page = new Page<>(dto.getPage(), dto.getLimit());
        IPage<ReturnOrderDetailVO> result = this.baseMapper.selectBarterOrderListToPlatform(page, dto);
        List<ReturnOrderDetailVO> list = result.getRecords();
        // 定义导出文件的列标题
        String[] headers = {"买家账号", "换单编号", "订单编号", "换货状态", "申请原因", "申请时间", "涉及商品", "商家处理备注",
                "商家处理时间", "店铺名称", "操作人", "操作时间"};

        // 初始化数据集，用于存储格式化后的订单数据
        List<Map<String, String>> dataset = Lists.newArrayList();

        // 遍历查询结果，格式化数据并添加到数据集中
        for (ReturnOrderDetailVO res : list) {
            // 对敏感信息进行脱敏处理
            BeanUtils.copyProperties(DesensitizationUtil.Desensitization(res, ReturnOrderDetailVO.class), res);

            // 根据退货状态码转换为易读的退货状态文本
            String returnState = getReturnState(res.getReturnState());

            // 构建单行数据的映射表，并添加到数据集中
            Map<String, String> map = Maps.newHashMap();
            map.put("买家账号", res.getBuyerUserAccount());
            map.put("账单编号", res.getReturnCode());
            map.put("订单编号", res.getOrderId());
            map.put("退货状态", returnState);
            map.put("申请原因", res.getReturnReason());
            map.put("申请时间", res.getReturnAddTime());
            map.put("涉及商品", res.getOrderGoodsNum());
            map.put("商家处理备注", res.getReturnShopMessage());
            map.put("商家处理时间", res.getReturnShopTime());
            map.put("店铺名称", res.getShopName());
            map.put("操作人", res.getOrderOperator());
            map.put("操作时间", res.getOrderOperatorTime());

            dataset.add(map);
        }

        // 构建导出文件名
        String fileName = "salesReturn" + DateUtils.dateTimeNow() + ".csv";

        // 将数据集包装为Excel文件格式
        Map<String, List<Map<String, String>>> mapExcel = Maps.newHashMap();
        mapExcel.put(fileName, dataset);
        String url = ExcelsUtil.createExcel(headers, mapExcel, new int[]{0});

        // 返回包含导出文件URL的响应结果
        return ResponseResult.success(url);
    }

    /**
     * 换货-查询换货订单详情
     */
    @Override
    public ResponseResult barterOrderDetail(ReturnOrderDTO dto) {
        // 通过baseMapper查询退货详情
        ReturnOrderDetailVO returnOrderDetail = baseMapper.selectBarterOrderDetail(dto);
        String orderId = returnOrderDetail.getOrderId();
        // 创建一个Map用于查询退货商品详情
        Map<String, String> selectMap = new HashMap<>();
        selectMap.put("orderId", orderId);
        // 查询退货商品详情
        List<GoodsDetailsVO> listReturnGoods = orderGoodsMapper.listReturnGoods(selectMap);
        if (Objects.equals(returnOrderDetail.getReturnMethodStatus(), CommonType.returnMethodType.BARTER.getCode())) {
            for (GoodsDetailsVO goods : listReturnGoods) {
                if (goods.getBarterGoodsId() != null) {
                    ResponseResult<OrderGoodsBarterVO> barterGoods = goodServiceClient.selectBarterGoods(goods.getBarterGoodsId());
                    goods.setBarterGoods(barterGoods.getData());
                }
            }
        }
        // 设置退货商品详情
        returnOrderDetail.setGoodsDetails(listReturnGoods);
        // 如果退货处理状态为3（即店铺已处理）且退货状态为5（即待平台处理），则将退货状态修改为3（已处理）
        if (returnOrderDetail.getReturnShopHandle() == 3 && "5".equals(returnOrderDetail.getReturnState())) {
            returnOrderDetail.setReturnState("3");
        }
        // 线下付款查询支付凭证
        if (returnOrderDetail.getPaymentCode() == 2) {
            String returnPaymentVoucherStr = orderReturnMapper.selectReturnPaymentVoucher(orderId);
            List<String> returnPaymentVoucher = JSON.parseArray(returnPaymentVoucherStr, String.class);
            returnOrderDetail.setReturnPaymentVoucherList(returnPaymentVoucher);
            OrderPaymentVocherEntity paymentVoucherEntity = orderPaymentVocherMapper.selectOne(new LambdaQueryWrapper<OrderPaymentVocherEntity>()
                    .eq(OrderPaymentVocherEntity::getOrderId, orderId));
            String paymentVoucher = paymentVoucherEntity.getOrderPaymentVoucherImgs();
            List<String> orderPaymentVoucher = JSON.parseArray(paymentVoucher, String.class);
            returnOrderDetail.setOrderPaymentVoucherList(orderPaymentVoucher);
        }
        // 设置换货物流信息
        setBarterOrderShippingList(returnOrderDetail, null);
        // 返回成功结果，并附带退货详情
        return ResponseResult.success(returnOrderDetail);
    }

    /**
     * 换货-查询换货订单列表
     */
    public void setBarterOrderShippingList(ReturnOrderDetailVO returnOrderDetailVO, SellerOrderDetailVO sellerOrderDetailVO) {
        String returnCode = returnOrderDetailVO == null ? sellerOrderDetailVO.getReturnCode() : returnOrderDetailVO.getReturnCode();
        List<BarterShippingInfoVO> barterShippingInfoList = baseMapper.selectBarterShippingInfoList(returnCode);
        if (!barterShippingInfoList.isEmpty()) {
            barterShippingInfoList.forEach(barterShippingInfoVO -> {
                //查询快递公司名字
                ResponseResult express = baseServiceClient.getBaseExpressInfoById(barterShippingInfoVO.getBarterPostCode());
                Map shipperCode = JSON.parseObject(JSON.toJSONString(express.getData()), Map.class);
                barterShippingInfoVO.setExpressName(shipperCode.get("expressName") + "");
            });
            if (returnOrderDetailVO != null) {
                returnOrderDetailVO.setBarterShippingInfoList(barterShippingInfoList);
            } else {
                sellerOrderDetailVO.setBarterShippingInfoList(barterShippingInfoList);
            }
        }
    }

    /**
     * 换货-平台介入同意退货与否，则 type=1 同意，则type=0不同意
     */
    @Override
    public ResponseResult platformApprove(ReturnOrderDTO dto) {
        UserInfoVO userInfo = null;
        Boolean isJob = dto.getIsJob() == null || !dto.getIsJob();
        // 解析用户信息
        if(isJob) {
            userInfo = ParseTokenUtils.getUserInfoByToken(dto.getToken());
            // 如果用户信息为空，则返回错误信息
            if (null == userInfo) {
                return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
            }
        }
        OrderReturnPlatformVO platform = new OrderReturnPlatformVO();
        int type = dto.getType();
        String orderId = dto.getOrderId();
        //查询平台介入
        OrderReturnPlatformVO orderReturnPlatform = orderReturnPlatformMapper.selectOrderReturnPlatform(orderId);
        //判断是卖家拒绝收货还是商家拒绝退货申请
        if (orderReturnPlatform.getReturnState() == 3) {
            Map<String, Object> orderGoodsMap = new HashMap<>();
            orderGoodsMap.put("orderOperator", isJob ? userInfo.getUserAccount() : "admin");

            // 平台同意买家
            if (1 == type) {
                orderGoodsMap.put("returnCode", dto.getReturnCode());
                orderGoodsMap.put("returnShopHandle", 2);
                orderGoodsMap.put("returnState", 2);
                Calendar instance = Calendar.getInstance();
                instance.setTime(new Date());
                instance.add(Calendar.DATE, 7);
                Date expirationTime = instance.getTime();

                //更新平台介入状态
                platform.setProcessingResults(type);
                platform.setInterventionState(2);
                platform.setProcessingTime(new Date());
                platform.setOrderId(orderId);
                orderReturnPlatformMapper.examineIntervention(platform);

                orderGoodsMap.put("expirationTime", expirationTime);
            } else {
                // 平台不同意
                orderGoodsMap.put("returnCode", dto.getReturnCode());
                orderGoodsMap.put("returnShopHandle", 13);
                orderGoodsMap.put("returnState", 13);
                orderGoodsMap.put("returnShopMessage", dto.getReturnShopMessage());
                orderGoodsMap.put("returnShopTime", new Date());
                orderGoodsMap.put("closeTime", new Date());
                orderGoodsMap.put("orderId", orderId);
                Calendar instance = Calendar.getInstance();
                instance.setTime(new Date());
                instance.add(Calendar.DATE, 1);
                Date expiration_time = instance.getTime();
                orderGoodsMap.put("expirationTime", expiration_time);
                orderReturnMapper.updateOrderBaseById1(orderGoodsMap);

                //更新平台介入状态
                platform.setProcessingResults(type);
                platform.setInterventionState(2);
                platform.setProcessingTime(new Date());
                platform.setOrderId(orderId);
                orderReturnPlatformMapper.examineIntervention(platform);

                Map<String, Object> updateMap = new HashMap<>();
                updateMap.put("orderId", orderId);
                updateMap.put("orderRefundStatus", "0");
                updateMap.put("orderReturnStatus", "0");
                updateMap.put("orderBarterStatus", "0");
                Integer torf = orderBaseMapper.updateOrderBaseById(updateMap);

                if (1 != torf) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return ResponseResult.fail();
                }

                dto.setOrderId(orderId);
                List<WapGoodsSaleSumDTO> returnGoods = orderGoodsMapper.queryReturnGoodsToSale(dto);
                goodServiceClient.updateGoodsStock(returnGoods);
            }

            Integer torf = orderReturnMapper.updateOrderReturnById(orderGoodsMap);
            return getResponseResult(dto, orderId, torf);
        } else {
            Map<String, Object> orderGoodsMap = new HashMap<>();
            // 平台同意
            if (1 == type) {
                Integer storageId = orderShipmentMapper.queryStorageId(orderId);
                List<StorageInventoryFlowGoodsDTO> storageGoodsList = new ArrayList<>();
                orderGoodsMap.put("returnCode", dto.getReturnCode());
                orderGoodsMap.put("returnShopHandle", 4);
                orderGoodsMap.put("returnState", 4);
                orderGoodsMap.put("returnShopTime", new Date());
                orderGoodsMap.put("returnFinishTime", new Date());
                orderGoodsMap.put("returnShopMessage", dto.getReturnShopMessage());
                orderGoodsMap.put("storageId", storageId);

                //更新平台介入状态
                platform.setProcessingResults(type);
                platform.setInterventionState(2);
                platform.setProcessingTime(new Date());
                platform.setOrderId(orderId);
                orderReturnPlatformMapper.examineIntervention(platform);

                OrderBaseEntity orderBaseEntity = orderBaseMapper.selectById(orderId);

                Map<String, Object> goodsNumMap = new HashMap<>();
                List<Map<String, Object>> goodsNum = orderGoodsMapper.queryBuyNumByorderId(orderId);
                goodsNumMap.put("goodsNum", goodsNum);
                //退款更新商品销量
                goodServiceClient.updateGoodsSaleNum(goodsNumMap);

                for (Map<String, Object> map : goodsNum) {
                    StorageInventoryFlowGoodsDTO storageInventoryFlowGoodsDTO = new StorageInventoryFlowGoodsDTO();
                    storageInventoryFlowGoodsDTO.setGoodsId(Integer.parseInt(map.get("goods_id").toString()));
                    storageInventoryFlowGoodsDTO.setGoodsNum(Integer.parseInt(map.get("order_goods_returnnum").toString()));
                    storageInventoryFlowGoodsDTO.setBuyNum(Integer.parseInt(map.get("order_goods_returnnum").toString()));
                    storageGoodsList.add(storageInventoryFlowGoodsDTO);
                }

                //子订单
                List<OrderBaseEntity> originalOrderList = orderBaseMapper.selectList(new LambdaQueryWrapper<OrderBaseEntity>()
                        .eq(OrderBaseEntity::getOriginalOrder, orderId));
                updateOriginalOrderGoodsSale(originalOrderList);

                Integer inventoryFlowType = CommonType.inventoryFlowType.PUT_STORAGE.getCode();
                Integer inventoryFlowTypeDetail = CommonType.inventoryFlowTypeDetail.RETURN_TO_INVENTORY_AND_STORAGE.getCode();
                setStorageInventoryFlow(storageId, orderId, inventoryFlowType, inventoryFlowTypeDetail, storageGoodsList,
                        goodServiceClient, baseServiceClient, orderBaseEntity.getShopId(),dto.getToken());
            } else {
                // 平台不同意
                orderGoodsMap.put("returnCode", dto.getReturnCode());
                orderGoodsMap.put("returnShopMessage", dto.getReturnShopMessage());
                orderGoodsMap.put("returnShopHandle", 13);
                orderGoodsMap.put("returnState", 13);
                orderGoodsMap.put("returnShopTime", new Date());
                orderGoodsMap.put("closeTime", new Date());

                //更新平台介入状态
                platform.setProcessingResults(type);
                platform.setInterventionState(2);
                platform.setProcessingTime(new Date());
                platform.setOrderId(orderId);
                orderReturnPlatformMapper.examineIntervention(platform);

                Map<String, Object> updateMap = new HashMap<>();
                updateMap.put("orderId", orderId);
                updateMap.put("orderReturnStatus", "0");
                updateMap.put("orderRefundStatus", "0");
                updateMap.put("orderBarterStatus", "0");
                Integer torf = orderBaseMapper.updateOrderBaseById(updateMap);
                //查询用户售后商品
                List<Integer> returnGoods = orderGoodsMapper.queryReturnGoods(orderId);
                Map<String, Object> updateGoodsMap = new HashMap<>();
                updateGoodsMap.put("orderId", orderId);
                updateGoodsMap.put("goodsReturnStatus", 0);
                updateGoodsMap.put("goodsRefundStatus", 0);
                updateGoodsMap.put("goodsBarterStatus", 0);
                for (Integer returnGood : returnGoods) {
                    updateGoodsMap.put("goodsId", returnGood);
                    orderReturnMapper.updateOrderGoodsById1(updateGoodsMap);
                }
            }
            orderGoodsMap.put("returnFinishTime", new Date());
            Integer torf = orderReturnMapper.updateOrderReturnById(orderGoodsMap);
            return getResponseResult(dto, orderId, torf);
        }
    }

    private void updateOriginalOrderGoodsSale(List<OrderBaseEntity> originalOrderList) {
        if (!originalOrderList.isEmpty()) {
            for (OrderBaseEntity entity1 : originalOrderList) {
                //获取子订单退货数量
                Map<String, Object> originalgoodsNumMap = new HashMap<>();
                List<Map<String, Object>> sonGoodsNum = orderGoodsMapper.queryBuyNumByorderId(entity1.getOrderId());
                originalgoodsNumMap.put("goodsNum", sonGoodsNum);
                // 更新子订单商品销量
                supplierClient.updateGoodsSaleNum(originalgoodsNumMap);
            }
        }
    }

    /**
     * 根据返回订单DTO、订单ID和一个标志获取响应结果
     * 此方法主要用于处理退货订单的逻辑，根据torf标志决定是否同步更新子订单
     *
     * @param dto     返回订单DTO，包含退货相关信息
     * @param orderId 订单ID，用于查询相关订单
     * @param torf    标志位，1表示需要同步更新子订单，其他值表示不需要
     * @return ResponseResult 包含处理结果的响应对象
     */
    private ResponseResult getResponseResult(ReturnOrderDTO dto, String orderId, Integer torf) {
        // 根据torf标志决定是否需要同步更新子订单
        if (1 == torf) {
            //同步更新子订单
            // 查询所有原始订单ID等于当前订单ID的子订单
            List<OrderBaseEntity> orderBaseEntities = orderBaseMapper.selectList(new LambdaQueryWrapper<OrderBaseEntity>()
                    .eq(OrderBaseEntity::getOriginalOrder, orderId));
            // 如果查询到的子订单列表不为空，则遍历处理每个子订单
            if (!orderBaseEntities.isEmpty()) {
                for (OrderBaseEntity entity : orderBaseEntities) {
                    // 查询当前子订单对应的退货信息
                    OrderReturnEntity orderReturnEntity = orderReturnMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>()
                            .eq(OrderReturnEntity::getOrderId, entity.getOrderId()));
                    // 如果当前子订单的退货信息存在，则更新DTO并调用平台审核方法
                    if (null != orderReturnEntity) {
                        dto.setReturnCode(orderReturnEntity.getReturnCode());
                        dto.setOrderId(entity.getOrderId());
                        platformApprove(dto);
                    }
                }
            }
            // 退货订单处理成功，返回成功响应
            return ResponseResult.success();
        }
        // 如果torf标志不为1，表示不需要同步更新子订单，返回失败响应
        return ResponseResult.fail();
    }

    /**
     * 换货-查询换货代发明细列表
     */
    @Override
    public ResponseResult<PageUtils<ReturnOrderVO>> selectBarterDropShippingList(ReturnOrderDTO dto) {
        // 将用户信息转换为需要的格式
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(dto.getToken());
        // 将用户店铺ID和退货类型添加到查询参数中
        dto.setShopId(Integer.valueOf(userInfo.getShopId()));
        Page<ReturnOrderVO> page = new Page<>(dto.getPage(), dto.getLimit());
        IPage<ReturnOrderVO> resultPage = baseMapper.selectBarterDropShippingList(page, dto);
        return setBarterGoods(resultPage, Integer.valueOf(userInfo.getShopId()), 2, userInfo);
    }

    /**
     * 设置换货商品信息
     *
     * @param resultPage 分页查询结果，包含换货订单信息
     * @param shopId     商店ID，用于查询换货商品
     * @param shopType   商店类型，用于区分不同的商店
     * @param userInfo   用户信息，暂未在方法中使用，但可能用于权限或用户相关操作
     * @return 返回包含换货商品信息的响应结果
     */
    private ResponseResult<PageUtils<ReturnOrderVO>> setBarterGoods(IPage<ReturnOrderVO> resultPage, Integer shopId, Integer shopType, UserInfoVO userInfo) {
        // 遍历查询结果中的每个换货订单
        for (ReturnOrderVO returnOrder : resultPage.getRecords()) {
            //查询订单换货商品
            Map<String, String> selectMap = new HashMap<>();
            selectMap.put("orderId", returnOrder.getOrderId());
            selectMap.put("shopId", shopId.toString());
            selectMap.put("shopType", shopType.toString());
            selectMap.put("isBarter", "1");
            List<GoodsOrderDetailsVO> goodsDetails = orderGoodsMapper.returnGoodsList(selectMap);

            // 如果订单的换货状态匹配，则进一步处理换货商品
            if (Objects.equals(returnOrder.getReturnMethodStatus(), CommonType.returnMethodType.BARTER.getCode())) {
                // 为每个换货商品设置换货商品详细信息
                for (GoodsOrderDetailsVO goods : goodsDetails) {
                    if (goods.getBarterGoodsId() != null) {
                        ResponseResult<OrderGoodsBarterVO> barterGoods;
                        if(shopType == 1) {
                            // 调用服务客户端查询换货商品信息，并设置到商品对象中
                            barterGoods = goodServiceClient.selectBarterGoods(goods.getBarterGoodsId());
                        }else {
                            // 调用服务客户端查询换货商品信息，并设置到商品对象中
                            barterGoods = supplierClient.selectBarterGoods(goods.getBarterGoodsId());
                        }
                        goods.setBarterGoods(barterGoods.getData());
                    }
                }
            }
            // 将处理后的换货商品列表设置回换货订单对象
            returnOrder.setGoodsOrderDetailsVO(goodsDetails);
        }
        // 返回包含分页信息的响应结果
        return ResponseResult.success(PageUtils.getPage(resultPage));
    }

    @Override
    public ResponseResult exportBarterDropShippingList(ReturnOrderDTO dto) {
        ResponseResult<PageUtils<ReturnOrderVO>> resultPage = selectBarterDropShippingList(dto);
        List<ReturnOrderVO> list = resultPage.getData().getList();
        // 定义导出文件的列标题
        String[] headers = {"换货编号", "供应商", "换货商品", "换货规格", "换货数量", "新商品", "新规格", "换货状态", "换货申请时间",
                "换货申请原因", "换货说明", "是否已寄回", "备注"};

        // 初始化数据集，用于存储格式化后的订单数据
        List<Map<String, String>> dataset = Lists.newArrayList();

        // 遍历查询结果，格式化数据并添加到数据集中
        for (ReturnOrderVO res : list) {
            String returnState = getReturnState(res.getReturnState());
            for (GoodsOrderDetailsVO goods : res.getGoodsOrderDetailsVO()) {
                // 构建单行数据的映射表，并添加到数据集中
                Map<String, String> map = Maps.newHashMap();
                map.put("换货编号", res.getReturnCode());
                map.put("供应商", res.getDeliverShopName());
                map.put("换货商品", goods.getGoodsName());
                map.put("换货规格", goods.getGoodsSpec());
                map.put("换货数量", goods.getOrderGoodsReturnnum());
                map.put("新商品", goods.getBarterGoods().getGoodsName());
                map.put("新规格", goods.getBarterGoods().getGoodsSpec());
                map.put("换货状态", returnState);
                map.put("换货申请时间", res.getReturnAddTime());
                map.put("换货申请原因", res.getReturnReason());
                map.put("换货说明", res.getReturnMessage());
                map.put("是否已寄回", res.getIsSendBack() == 1 ? "已寄回" : "未寄回");
                map.put("备注", res.getSendBackNotes());
                dataset.add(map);
            }
        }

        // 构建导出文件名
        String fileName = "barterDropShipping" + DateUtils.dateTimeNow() + ".csv";

        // 将数据集包装为Excel文件格式
        Map<String, List<Map<String, String>>> mapExcel = Maps.newHashMap();
        mapExcel.put(fileName, dataset);
        String url = ExcelsUtil.createExcel(headers, mapExcel, new int[]{0, 7, 8, 9, 10, 11});

        // 返回包含导出文件URL的响应结果
        return ResponseResult.success(url);
    }

    private String getReturnState(String returnState) {
        // 根据退货状态码转换为易读的退货状态文本
        if (StringUtils.isNotEmpty(returnState)) {
            switch (returnState) {
                case "0":
                    returnState = "卖家拒绝 ";
                    break;
                case "1":
                    returnState = "新发起等待卖家审核";
                    break;
                case "2":
                    returnState = "卖家审核通过  ";
                    break;
                case "3":
                    returnState = "卖家审核不通过 ";
                    break;
                case "4":
                    returnState = "卖家收到货物 ";
                    break;
                case "5":
                    returnState = "平台审核通过";
                    break;
                case "6":
                    returnState = "买家已填物流 ";
                    break;
                case "7":
                    returnState = "平台拒绝";
                    break;
                case "8":
                    returnState = "退款关闭";
                    break;
                case "9":
                    returnState = "平台介入";
                    break;
                case "10":
                    returnState = "验收不通过";
                    break;
                case "11":
                    returnState = "待买家退货";
                    break;
                case "12":
                    returnState = "待验收";
                    break;
                case "13":
                    returnState = "退货关闭";
                    break;
                case "18":
                    returnState = "卖家发出换货物流";
                    break;
                case "19":
                    returnState = "买家确认收货";
                    break;
                case "20":
                    returnState = "用户撤销换货关闭";
                    break;
                case "21":
                    returnState = "部分发出换货物流";
                    break;
                case "22":
                    returnState = "买家部分确认收货";
                    break;
                case "23":
                    returnState = "换货关闭";
                    break;
            }
        }
        return returnState;
    }

    /**
     * 发送退货订单回滴答仓
     * 此方法处理退货订单的发送逻辑，将订单状态更新为已发送回滴答仓
     * 如果订单有子订单，也会同步更新子订单的状态
     *
     * @param dto 包含退货订单ID列表的传输对象
     * @return 返回执行结果的成功响应
     */
    @Override
    public ResponseResult sendBackDropShipping(ReturnOrderDTO dto) {
        // 获取订单ID列表
        List<String> orderIdList = dto.getOrderIdList();
        // 遍历每个订单ID
        for (String orderId : orderIdList) {
            // 更新订单退货状态为已发送回滴答仓
            orderReturnMapper.update(null, new LambdaUpdateWrapper<OrderReturnEntity>()
                    .set(OrderReturnEntity::getIsSendBack, CommonType.commonState.YES.getCode())
                    .eq(OrderReturnEntity::getOrderId, orderId));
            //同步更新子订单
            // 查询是否有子订单
            List<OrderBaseEntity> orderBaseEntities = orderBaseMapper.selectList(new LambdaQueryWrapper<OrderBaseEntity>()
                    .eq(OrderBaseEntity::getOriginalOrder, orderId));
            // 如果存在子订单
            if (!orderBaseEntities.isEmpty()) {
                // 遍历每个子订单
                for (OrderBaseEntity entity : orderBaseEntities) {
                    // 查询子订单的退货信息
                    OrderReturnEntity orderReturnEntity = orderReturnMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>()
                            .eq(OrderReturnEntity::getOrderId, entity.getOrderId()));
                    // 如果子订单的退货信息存在
                    if (null != orderReturnEntity) {
                        // 更新DTO的订单ID列表为当前子订单ID
                        dto.setOrderIdList(Collections.singletonList(entity.getOrderId()));
                        // 递归调用发送退货订单回滴答仓
                        sendBackDropShipping(dto);
                    }
                }
            }
        }
        // 返回成功响应
        return ResponseResult.success();
    }

    /**
     * job-定时任务处理
     */
    @Override
    public void handleBarterOrderToJob() {
        // 超过48小时商家未处理售后换货申请
        List<handleBarterOrderDTO> list = baseMapper.handleBarterOrderApplyToJob();
        if (!list.isEmpty()) {
            for (handleBarterOrderDTO handleDTO : list) {
                ManageShopAddressDTO addressDTO = merchantServiceClient.queryShippingAddressByShopId(handleDTO.getShopId());
                ReturnOrderDTO dto = new ReturnOrderDTO();
                dto.setReturnCode(handleDTO.getReturnCode());
                dto.setType(1);
                dto.setReturnShopMessage("商家超时未处理,自动同意-->买家填写物流");
                if (addressDTO != null) {
                dto.setReturnAddrId(addressDTO.getShippingAddressId());
                dto.setReturnAddr(addressDTO.getShippingAddressAddress());
                dto.setReturnAddrName(addressDTO.getShippingAddressArea());
                dto.setReturnMobile(addressDTO.getShippingAddressPhone());
                }
                approveBarter(dto);
            }
        }
        // 超过24小时卖家未处理售后换货订单
        List<handleBarterOrderDTO> barterOrderDTOList = baseMapper.handleBarterOrderToJob();
        if (!barterOrderDTOList.isEmpty()) {
            for (handleBarterOrderDTO handleDTO : barterOrderDTOList) {
                ReturnOrderDTO dto = new ReturnOrderDTO();
                dto.setIsJob(true);
                dto.setOrderId(handleDTO.getOrderId());
                dto.setReturnCode(handleDTO.getReturnCode());
                dto.setType(1);
                dto.setShopId(handleDTO.getShopId());
                dto.setShopType(CommonType.ShopType.COMMON_SHOP.getCode());
                dto.setReturnShopMessage("商家超时未处理,自动同意-->待卖家发出换货物流");
                shopConfirmReceipt(dto);
            }
        }
        // 超出售后期更改状态为换货关闭
        List<String> barterOrderList = baseMapper.afterSalesOutOrderToJob();
        if (!barterOrderList.isEmpty()) {
            orderReturnMapper.update(null, new LambdaUpdateWrapper<OrderReturnEntity>()
                    .set(OrderReturnEntity::getReturnState, CommonType.returnState.BARTER_CLOSED.getCode())
                    .in(OrderReturnEntity::getOrderId, barterOrderList));
        }
        // 商家同意换货但买家未在有效期内填写物流信息 状态 2 / 商家拒绝退货后买家超时未处理 状态 3 10
        List<String> noShippingOrderList = baseMapper.buyTimeOutReturnGoodsToJob();
        if (!noShippingOrderList.isEmpty()) {
            List<String> originalOrderIds = orderBaseMapper.selectList(new LambdaQueryWrapper<OrderBaseEntity>()
                    .select(OrderBaseEntity::getOrderId)
                    .in(OrderBaseEntity::getOriginalOrder, noShippingOrderList)).stream().map(OrderBaseEntity::getOrderId).toList();
            if (!originalOrderIds.isEmpty()) {
                noShippingOrderList.addAll(originalOrderIds);
            }
            // 设置订单过期时间为当前时间加一天。
            Calendar instance = Calendar.getInstance();
            instance.setTime(new Date());
            instance.add(Calendar.DATE, 1);
            Date expirationTime = instance.getTime();
            orderReturnMapper.update(null, new LambdaUpdateWrapper<OrderReturnEntity>()
                    .set(OrderReturnEntity::getReturnState, CommonType.returnState.BARTER_CLOSED.getCode())
                    .set(OrderReturnEntity::getReturnShopHandle, CommonType.returnState.BARTER_CLOSED.getCode())
                    .set(OrderReturnEntity::getExpirationTime, expirationTime)
                    .set(OrderReturnEntity::getCallStatus, CommonType.commonStatus.YES.getCode())
                    .in(OrderReturnEntity::getOrderId, noShippingOrderList));
            orderBaseMapper.update(null, new LambdaUpdateWrapper<OrderBaseEntity>()
                    .set(OrderBaseEntity::getOrderBarterStatus, CommonType.orderBarterStatus.NO_BARTER.getCode())
                    .in(OrderBaseEntity::getOrderId, noShippingOrderList));
            orderGoodsMapper.update(null, new LambdaUpdateWrapper<OrderGoodsEntity>()
                    .set(OrderGoodsEntity::getGoodsBarterStatus, CommonType.orderBarterStatus.NO_BARTER.getCode())
                    .in(OrderGoodsEntity::getOrderId, noShippingOrderList));
        }
        // 申请平台介入卖家超时未处理
        List<handleBarterOrderDTO> platformApproveList = baseMapper.platformApproveToJob();
        if (!platformApproveList.isEmpty()) {
            for (handleBarterOrderDTO handleDTO : platformApproveList) {
                ReturnOrderDTO dto = new ReturnOrderDTO();
                dto.setIsJob(true);
                dto.setOrderId(handleDTO.getOrderId());
                dto.setReturnCode(handleDTO.getReturnCode());
                dto.setType(1);
                dto.setShopId(handleDTO.getShopId());
                dto.setShopType(CommonType.ShopType.COMMON_SHOP.getCode());
                dto.setReturnShopMessage("商家超时未处理,自动同意");
                platformApprove(dto);
            }
        }
        // 超过7天用户未售后换货物流订单
        List<OrderBarterEntity> barterOrderShippingList = baseMapper.selectList(new LambdaQueryWrapper<OrderBarterEntity>()
                .select(OrderBarterEntity::getOrderBarterId, OrderBarterEntity::getOrderId, OrderBarterEntity::getReturnCode)
                .eq(OrderBarterEntity::getBarterStatus, CommonType.barterStatus.SEND_BARTER_LOGISTICS.getCode())
                .lt(OrderBarterEntity::getCreateTime, DateUtils.addDays(new Date(), -7)));
        if (!barterOrderShippingList.isEmpty()) {
            List<Integer> orderBarterIdList = barterOrderShippingList.stream().map(OrderBarterEntity::getOrderBarterId).toList();
            List<String> orderIdList = barterOrderShippingList.stream().map(OrderBarterEntity::getOrderId).toList();
            // 更新换货状态为买家确认收货，并设置配送时间
            baseMapper.update(null, new LambdaUpdateWrapper<OrderBarterEntity>()
                    .set(OrderBarterEntity::getBarterStatus, CommonType.barterStatus.BUYER_BARTER_CONFIRM_RECEIPT.getCode())
                    .set(OrderBarterEntity::getDeliveryTime, DateUtils.getNowDateToDf())
                    .in(OrderBarterEntity::getOrderBarterId, orderBarterIdList));
            for (OrderBarterEntity entity : barterOrderShippingList) {
                ReturnOrderDTO dto = new ReturnOrderDTO();
                dto.setOrderId(entity.getOrderId());
                dto.setReturnCode(entity.getReturnCode());
                dto.setOrderBarterIdList(Collections.singletonList(entity.getOrderBarterId()));

                // 查询订单中所有商品的数量
                Integer allGoodsCount = orderGoodsMapper.queryOrderReturnGoodsCount(entity.getOrderId());
                // 计算已完成换货的商品数量
                Integer barterGoodsCount = Math.toIntExact(baseMapper.selectCount(new LambdaQueryWrapper<OrderBarterEntity>()
                        .eq(OrderBarterEntity::getOrderId, entity.getOrderId())
                        .eq(OrderBarterEntity::getBarterStatus, CommonType.barterStatus.BUYER_BARTER_CONFIRM_RECEIPT.getCode())));

                // 更新订单退换货状态，根据是否所有商品都已完成换货
                orderReturnMapper.update(null, new LambdaUpdateWrapper<OrderReturnEntity>()
                        .set(OrderReturnEntity::getReturnState, Objects.equals(allGoodsCount, barterGoodsCount) ?
                                CommonType.returnState.BUYER_BARTER_CONFIRM_RECEIPT.getCode() : CommonType.returnState.BUYER_PART_BARTER_CONFIRM_RECEIPT.getCode())
                        .set(allGoodsCount.equals(barterGoodsCount), OrderReturnEntity::getReturnFinishTime, DateUtils.getNowDateToDf())
                        .eq(OrderReturnEntity::getReturnCode, dto.getReturnCode()));

                // 如果所有商品都已完成换货，更新订单状态为换货完成
                if (allGoodsCount.equals(barterGoodsCount)) {
                    orderBaseMapper.update(null, new LambdaUpdateWrapper<OrderBaseEntity>()
                            .set(OrderBaseEntity::getOrderBarterStatus, CommonType.orderBarterStatus.BARTER_COMPLETED.getCode())
                            .eq(OrderBaseEntity::getOrderId, entity.getOrderId()));
                }

                // 查询用户售后商品
                List<WapGoodsSaleSumDTO> returnGoods = orderGoodsMapper.queryReturnGoodsToSale(dto);
                goodServiceClient.updateSubStockAndAddSaleNum(JSON.toJSONString(returnGoods));

            }
            List<Integer> returnGoodsId = orderGoodsMapper.selectList(new LambdaQueryWrapper<OrderGoodsEntity>()
                    .select(OrderGoodsEntity::getGoodsId)
                    .in(OrderGoodsEntity::getOrderBarterId, orderBarterIdList)).stream().map(OrderGoodsEntity::getGoodsId).collect(Collectors.toList());
            // 更新商品换货状态为换货完成
            orderGoodsMapper.update(null, new LambdaUpdateWrapper<OrderGoodsEntity>()
                    .set(OrderGoodsEntity::getGoodsBarterStatus, CommonType.orderBarterStatus.BARTER_COMPLETED.getCode())
                    .in(OrderGoodsEntity::getGoodsId, returnGoodsId)
                    .in(OrderGoodsEntity::getOrderId, orderIdList));
        }
    }
}
