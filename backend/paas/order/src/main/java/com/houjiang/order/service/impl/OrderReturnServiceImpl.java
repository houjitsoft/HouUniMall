package com.houjiang.order.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.houjiang.commons.aspect.UserPermissionsResult;
import com.houjiang.commons.aspect.UserService;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.base.MemberMessageListIntegralDTO;
import com.houjiang.commons.dto.base.UpMemberClassDTO;
import com.houjiang.commons.dto.goods.ManageShopAddressDTO;
import com.houjiang.commons.dto.goods.StorageInventoryFlowGoodsDTO;
import com.houjiang.commons.dto.vop.JingDongReturnGoodsDTO;
import com.houjiang.commons.dto.vop.JingDongReturnOrderDTO;
import com.houjiang.commons.dto.order.RefundParamDTO;
import com.houjiang.commons.dto.order.WapGoodsSaleSumDTO;
import com.houjiang.commons.dto.payment.AccountPeriodParamDTO;
import com.houjiang.commons.dto.payment.RefundAccountParamDTO;
import com.houjiang.commons.dto.shop.ShopSalesStatisticsDTO;
import com.houjiang.commons.dto.userms.UserInfoDTO;
import com.houjiang.commons.dto.vop.OrderAddresDTO;
import com.houjiang.commons.dto.yiyatong.ApplyRefundDTO;
import com.houjiang.commons.dto.yiyatong.ApplyReturnGoodsDTO;
import com.houjiang.commons.dto.yiyatong.OrderDetailDTO;
import com.houjiang.commons.errorcode.domain.PermissionErrorCode;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.exception.DefinedException;
import com.houjiang.commons.rocketmq.MqSendUtils;
import com.houjiang.commons.util.*;
import com.houjiang.commons.util.json.JsonUtils;
import com.houjiang.commons.util.excel.ExcelsUtil;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.base.PlatformoperationVO;
import com.houjiang.commons.vo.goods.GoodsDetailsListVO;
import com.houjiang.commons.vo.goods.WapGoodsSaleSumUpdateVO;
import com.houjiang.commons.vo.order.OrderBaseVO;
import com.houjiang.commons.vo.order.OrderCommissionVO;
import com.houjiang.commons.vo.order.OrderGoodsBarterVO;
import com.houjiang.commons.vo.payment.AccountShopBillDetailsVO;
import com.houjiang.commons.vo.shop.AccountPeriodVO;
import com.houjiang.commons.vo.shop.ManageShopAddressQueryVO;
import com.houjiang.commons.vo.shop.RetentionShopInfoVO;
import com.houjiang.commons.vo.shop.ShopUserMessageResultVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.commons.vo.yiyatong.base.OrderReturnGoodsVO;
import com.houjiang.commons.vo.yiyatong.base.ResellerOrderDetailVO;
import com.houjiang.commons.vo.yiyatong.base.ResellerOrderItemVO;
import com.houjiang.order.dto.*;
import com.houjiang.order.entity.*;
import com.houjiang.order.feign.*;
import com.houjiang.order.mapper.*;
import com.houjiang.order.service.OrderReturnService;
import com.houjiang.order.service.OrderSupplierBaseService;
import com.houjiang.order.utils.DateUtil;
import com.houjiang.order.vo.*;
import io.seata.core.context.RootContext;
import io.seata.core.exception.TransactionException;
import io.seata.spring.annotation.GlobalTransactional;
import io.seata.tm.api.GlobalTransactionContext;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.houjiang.order.service.impl.OrderBaseServiceImpl.setStorageInventoryFlow;
import static com.houjiang.order.service.impl.OrderBaseServiceImpl.setStorageInventoryFlow2;

/**
 * @author Amos
 */
@Service
public class OrderReturnServiceImpl extends ServiceImpl<OrderReturnMapper, OrderReturnEntity>
        implements OrderReturnService {
    private static final Logger LOG = LoggerFactory.getLogger(OrderReturnServiceImpl.class);
    @Resource
    private OrderBaseMapper orderBaseMapper;
    @Resource
    private OrderGoodsMapper orderGoodsMapper;
    @Resource
    private OrderReturnPlatformMapper orderReturnPlatformMapper;
    @Resource
    private OrderComplaintsMapper orderComplaintsMapper;
    @Resource
    private OrderCommissionMapper orderCommissionMapper;

    @Autowired
    private UserServiceClient userServiceClient;
    @Autowired
    private PayMentServiceClient payMentServiceClient;
    @Autowired
    private BaseServiceClient baseServiceClient;
    @Autowired
    private ThirdPartyServiceClient thirdPartyServiceClient;
    @Autowired
    private GoodServiceClient goodServiceClient;
    @Autowired
    private MerchantServiceClient merchantServiceClient;
    @Autowired
    private OrderShipmentMapper orderShipmentMapper;
    @Resource
    private OrderYiyatongMapper orderYiyatongMapper;
    @Resource
    private SupplierClient supplierClient;
    @Resource
    private OrderDealerInfoMapper orderDealerInfoMapper;
    @Autowired
    private OrderSupplierBaseMapper orderSupplierBaseMapper;
    @Autowired
    private OrderSupplierBaseService orderSupplierBaseService;

    @Resource
    private MallDistributionClient mallDistributionClient;

    @org.springframework.beans.factory.annotation.Value("${houcloud.plugin.internal-job-token:}")
    private String internalJobToken;

    /**
     * 二开售后创建网关（before*）：退款 / 退货 / 换货申请前回调 {@code AfterSaleCreateExtension}，只读、可中断。
     */
    @Autowired
    private com.houjiang.order.plugin.AfterSaleCreateGateway afterSaleCreateGateway;

    /**
     * 支付退款成功后通知分销等比冲减（旁路）。offline 场景直接调 {@link #notifyDistributionRefund}.
     */
    private boolean waitShipmentsRefundAndNotify(RefundParamDTO refundPara, String orderId, Object returnCash) {
        boolean ok = payMentServiceClient.waitShipmentsRefund(refundPara);
        if (ok) {
            notifyDistributionRefund(orderId, returnCash);
        }
        return ok;
    }

    private void notifyDistributionRefund(String orderId, Object returnCashObj) {
        BigDecimal returnCash = toMoney(returnCashObj);
        boolean full = false;
        try {
            OrderBaseEntity base = orderBaseMapper.selectById(orderId);
            if (base != null && base.getOrderPaymentAmount() != null && returnCash != null
                    && returnCash.compareTo(base.getOrderPaymentAmount()) >= 0) {
                full = true;
            }
        } catch (Exception ignored) {
            // ignore
        }
        com.houjiang.order.utils.DistributionNotifyHelper.notifyCancel(
                mallDistributionClient, internalJobToken, orderId, returnCash, full);
    }

    private static BigDecimal toMoney(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        try {
            return new BigDecimal(value.toString());
        } catch (Exception e) {
            return null;
        }
    }
    @Resource
    private OrderPaymentVocherMapper orderPaymentVocherMapper;
    @Resource
    private OrderBarterMapper orderBarterMapper;
    @Resource
    private OrderBarterServiceImpl orderBarterService;
    /**
     * 处理退款申请。
     * 使用全局事务确保退款申请操作的原子性。
     *
     * @param map 包含退款相关信息的映射，包括令牌和退款原因等。
     * @return 返回处理结果，成功或失败及原因。
     * @throws TransactionException 如果发生事务异常。
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public ResponseResult applyRefund(Map<String, String> map) throws TransactionException {
        try {
            // 从请求映射中获取令牌信息
            String token = map.get("token");

            // 检查退款原因ID和原因是否为空或未设置，防止无效的退款申请
            if ("".equals(map.get("returnReasonId")) || "".equals(map.get("returnReason"))
                    || map.get("returnReasonId") == null || map.get("returnReason") == null) {

                return ResponseResult.fail(BizCodeEnume.PLEASE_COMPLETE_THE_REFUND_INFORMATION);
            }

            // 通过令牌获取用户信息，用于后续的退款操作
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);

            // 调用具体业务方法处理退款申请，并返回处理结果
            ResponseResult result = applyRefund(map, userInfo);
            return result;

        } catch (Exception e) {
            // 发生异常时，回滚全局事务，并返回失败信息
            throw new DefinedException(e.getMessage());
        }

    }


    /**
     * 处理申请退货的请求。
     * <p>
     * 本方法通过解析传入的token，获取用户信息，并调用applyReturn方法处理退货申请。
     * 在处理过程中，会结合用户信息对退货申请进行相应的处理逻辑。
     *
     * @param map 包含请求参数的映射，其中必须包含token信息。
     * @return 返回处理结果的ResponseResult对象。
     * @throws TransactionException 如果处理过程中发生事务异常，则抛出此异常。
     */
    @Override
    public ResponseResult applyReturn(Map<String, String> map) throws TransactionException {
        // 从请求参数映射中获取token
        String token = map.get("token");
        // 使用token解析获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        // 调用applyReturn方法处理退货申请，传入请求参数映射和用户信息
        ResponseResult result = applyReturn(map, userInfo);
        // 返回处理结果
        return result;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public ResponseResult applyReturnMore(ApplyReturnOrderDTO applyReturnOrders) throws TransactionException {
        try {
            String token = applyReturnOrders.getToken();
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
            if (userInfo == null) {
                return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
            }
            if (applyReturnOrders.getReturnReasonId() == null || applyReturnOrders.getReturnReasonId().isEmpty()) {
                return ResponseResult.fail(BizCodeEnume.RETURN_REASON_CANNOT_BE_NULL);
            }
            List<OrderBaseVO> orderBases = orderBaseMapper.getOrderBaseById(applyReturnOrders.getOrderId());
            OrderBaseVO orderBase = orderBases.getFirst();
            if (!orderBase.getOrderReturnStatus().equals(CommonType.orderReturnStatus.NO_RETURNS.getCode().toString())) {
                if (orderBase.getOrderReturnStatus().equals(CommonType.orderReturnStatus.RETURN_COMPLETED.getCode().toString())) {
                    //当前订单退款中,请勿重复操作
                    return ResponseResult.fail(BizCodeEnume.ORDER_STATUS_CHANGE);
                } else {
                    Integer returnState = Integer.parseInt(orderBase.getOrderReturnStatus());
                    if (!returnState.equals(CommonType.returnState.NEW_LAUNCH_PENDING_SELLER_REVIEW.getCode()) &&
                            !returnState.equals(CommonType.returnState.SELLER_FAILED_THE_REVIEW.getCode()) &&
                            !returnState.equals(CommonType.returnState.USER_CANCELS_REFUND_CLOSED.getCode()) &&
                            !returnState.equals(CommonType.returnState.USER_CANCELS_RETURN_CLOSED.getCode())) {
                        return ResponseResult.fail(BizCodeEnume.ORDER_STATUS_CHANGE);
                    }
                }
            }
            List<Integer> orderStatusList = Arrays.asList(3, 4, 5, 6, 8, 9, 11, 12);
            if (!orderStatusList.contains(orderBase.getOrderStatus())) {
                return ResponseResult.fail(BizCodeEnume.ORDER_STATUS_CHANGE);
            }
            // 如果订单售后时间小于当前时间订单不可退
            if (orderBase.getVirtualGoods() != 0 && null != orderBase.getOrderLastReturnTime()
                    && DateUtils.compareDate(orderBase.getOrderLastReturnTime(), DateUtils.getNowDate())) {
                return ResponseResult.fail(BizCodeEnume.GOODS_BEYOND_THE_AFTER_SALES_PERIOD);
            }
            String orderId = applyReturnOrders.getOrderId();

            //清空平台介入信息
            orderReturnPlatformMapper.delete(new LambdaQueryWrapper<OrderReturnPlatformEntity>()
                    .eq(OrderReturnPlatformEntity::getOrderId, String.valueOf(applyReturnOrders.getOrderId())));

            // 去userms模块查找用户信息 "sellerId","sellerName","shopId","userId"
            Map<String, String> getSerllerInfoMap = new HashMap<>();
            getSerllerInfoMap.put("shopId", orderBase.getShopId().toString());
            Map<String, String> sellerInfo = (Map<String, String>) userServiceClient.getSellerInfo(getSerllerInfoMap).getData();
            Integer orderGoodsNum = 0;
            // 根据商品己算退货价格
            List<ApplyReturnGoodDTO> applyReturnGoods = applyReturnOrders.getApplyReturnGoods();

            //判断当前订单是否全部退货
            Boolean huoBaoreturn = true;
            Double returnCash = 0.00;
            //售后红包金额
            Double returnRptCash = 0.00;
            //退还运费
            Double returnShippingFee = 0.00;
            //供应商 退还的 经销等级折扣
            BigDecimal orderDiscountFee = new BigDecimal(0);
            //查询运费
            List<OrderGoodsEntity> goodsNumList = orderGoodsMapper.selectList(new LambdaQueryWrapper<OrderGoodsEntity>()
                    .eq(OrderGoodsEntity::getOrderId, orderId));
            Double shopDealerCommission = 0.00;
            Integer two = 0;
            //由于要展示退货详情所以商品表撤销是没清状态(所以二次申请要清空状态)
            Map<String, Object> orderGoodsMap1 = new HashMap<String, Object>();
            orderGoodsMap1.put("goodsRefundStatus", 0);
            orderGoodsMap1.put("goodsReturnStatus", 0);
            orderGoodsMap1.put("orderId", orderId);
            baseMapper.updateOrderGoodsById(orderGoodsMap1);
            for (ApplyReturnGoodDTO applyReturnGood : applyReturnGoods) {
                // 查询商品购买数量
                String orderGoodsId = applyReturnGood.getOrderGoodsId();

                OrderGoodsEntity orderPurchaseQuantity = orderGoodsMapper.selectOne(new LambdaQueryWrapper<OrderGoodsEntity>()
                        .eq(OrderGoodsEntity::getOrderId, orderId)
                        .eq(OrderGoodsEntity::getGoodsId, orderGoodsId));

                returnRptCash += orderPurchaseQuantity.getOrderRptPrice();
                returnShippingFee += orderPurchaseQuantity.getShippingFee();

                Integer orderGoodsNumTemp = Integer.valueOf(orderPurchaseQuantity.getOrderGoodsNum());
                Integer orderReturnGoodsNum = Integer.valueOf(applyReturnGood.getOrderGoodsNum());
                if (!orderGoodsNumTemp.equals(orderReturnGoodsNum)) {
                    huoBaoreturn = false;
                }
                boolean dropShippingFlag = orderPurchaseQuantity.getDropShipping() != null && orderPurchaseQuantity.getDropShipping() == 1;
                Double dealerReturnCommission = 0.00;
                Double goodsReturnPrice = 0.00;
                // 判断：如果全部退则退还此商品实付，否则按比例退还
                if (applyReturnGood.getOrderGoodsNum().equals(orderPurchaseQuantity.getOrderGoodsNum())) {
                    // 1.1 退回资金 = 应付金额（商品实际支付金额 + 运费- 促销）'
                    returnCash += orderPurchaseQuantity.getOrderGoodsPaymentAmount().doubleValue() - orderPurchaseQuantity.getShippingFee();
                    //全部退 退经销等级折扣
                    orderDiscountFee = orderBase.getDirectsellerDiscount();
                    if (dropShippingFlag) {
                        dealerReturnCommission = orderPurchaseQuantity.getDealerCommission();
                        shopDealerCommission = shopDealerCommission + dealerReturnCommission;
                    }
                    goodsReturnPrice = orderPurchaseQuantity.getOrderGoodsAmount() - orderPurchaseQuantity.getVoucherPrice();
                } else {
                    if (null != orderBase.getOrderType() && 2 == orderBase.getOrderType()) {
                        // 退货金额 = ((实付金额-运费)/商品总金额)*退货商品价格*退货商品数量
                        returnCash += (orderBase.getOrderGoodsAmount().doubleValue() / Double.parseDouble(orderPurchaseQuantity.getOrderGoodsNum())) * Double.parseDouble(applyReturnGood.getOrderGoodsNum());

                    } else {
                        // 退货金额 = ((实付金额-运费)/商品总金额)*退货商品价格*退货商品数量
                        returnCash += ((orderPurchaseQuantity.getOrderGoodsPaymentAmount().doubleValue() - orderPurchaseQuantity.getShippingFee()) / Double.parseDouble(orderPurchaseQuantity.getOrderGoodsNum())) * Double.parseDouble(applyReturnGood.getOrderGoodsNum());
                        dealerReturnCommission = (orderPurchaseQuantity.getDealerCommission() / Integer.parseInt(orderPurchaseQuantity.getOrderGoodsNum()) * Integer.parseInt(applyReturnGood.getOrderGoodsNum()));
                        goodsReturnPrice = (orderPurchaseQuantity.getOrderGoodsAmount() - orderPurchaseQuantity.getVoucherPrice())
                                / Integer.parseInt(orderPurchaseQuantity.getOrderGoodsNum()) * Integer.parseInt(applyReturnGood.getOrderGoodsNum());
                        shopDealerCommission = shopDealerCommission + dealerReturnCommission;
                    }
                    if (!"0".equals(orderPurchaseQuantity.getOrderGoodsNum())) {
                        if (null != orderBase.getDirectsellerDiscount()) {
                            orderDiscountFee = (orderBase.getDirectsellerDiscount().divide(new BigDecimal(orderPurchaseQuantity.getOrderGoodsNum()), 2, RoundingMode.HALF_UP)).multiply(new BigDecimal(applyReturnGood.getOrderGoodsNum()));
                        }
                    }
                }
                orderGoodsNum += Integer.parseInt(applyReturnGood.getOrderGoodsNum());
                two += orderGoodsMapper.update(null, new LambdaUpdateWrapper<OrderGoodsEntity>()
                        .eq(OrderGoodsEntity::getOrderId, applyReturnOrders.getOrderId())
                        .eq(OrderGoodsEntity::getGoodsId, applyReturnGood.getOrderGoodsId())
                        .set(OrderGoodsEntity::getOrderGoodsReturnnum, applyReturnGood.getOrderGoodsNum())
                        .set(OrderGoodsEntity::getDealerReturnCommission, dealerReturnCommission)
                        .set(OrderGoodsEntity::getGoodsReturnPrice, goodsReturnPrice)
                        // 订单状态 与order同步
                        .set(OrderGoodsEntity::getOrderGoodsStatus, CommonType.orderStatus.CONFIRM_RECEIPT.getCode())
                        .set(OrderGoodsEntity::getGoodsReturnStatus, CommonType.orderReturnStatus.RETURNING_GOODS.getCode())
                        .set(OrderGoodsEntity::getGoodsRefundStatus, CommonType.orderRefundStatus.NO_REFUND.getCode()));
            }
            //如果huoBaoreturn 为true 证明当前订单 退货的商品数量与订单数量一致 （但也可能是一个订单里面的一个商品全部数量退货，另外的商品不退货，所以要判断是否是所有商品全部退货）
            if (!huoBaoreturn) {
                //判断订单是否是全部商品 全部退货
                Integer num = goodsNumList.size();
                //如果是订单全部退货 就退红包（修改红包状态未使用，在商家退货确认收货接口修改），退红包 就在结算时写入红包金额
                if (num.equals(applyReturnGoods.size())) {
                    //如果进来就证明该订单不是是全部退货就不退红包，不将红包金额写入结算  不退还 经销等级折扣
                    returnRptCash = 0.00;
                    returnShippingFee = 0.00;
                }
            }
            // 调整退货金额，确保两位小数
            if(applyReturnOrders.getIsSupplier() == null || applyReturnOrders.getIsSupplier() == 0) {
                returnCash = applyReturnOrders.getReturnCash();
            }
            if (null == returnCash) {
                return ResponseResult.fail(BizCodeEnume.WRONG_AMOUNT);
            }
            BigDecimal tmp = new BigDecimal(returnCash);
            if (tmp.compareTo(new BigDecimal(0)) <= 0) {
                return ResponseResult.fail(BizCodeEnume.WRONG_AMOUNT);
            }
            returnCash = tmp.setScale(2, RoundingMode.HALF_UP).doubleValue();

            Integer one = orderBaseMapper.update(null, new LambdaUpdateWrapper<OrderBaseEntity>()
                    .eq(OrderBaseEntity::getOrderId, applyReturnOrders.getOrderId())
                    .set(OrderBaseEntity::getOrderStatus, CommonType.orderStatus.CONFIRM_RECEIPT.getCode())
                    .set(OrderBaseEntity::getOrderReturnStatus, CommonType.orderReturnStatus.RETURNING_GOODS.getCode())
                    .set(OrderBaseEntity::getOrderRefundStatus, CommonType.orderRefundStatus.NO_REFUND.getCode())
                    .set(OrderBaseEntity::getOrderFinishedTime, DateUtils.getNowDate())
                    .set(OrderBaseEntity::getReturnMethodType, CommonType.returnMethodType.RETURN.getCode())
                    .set(OrderBaseEntity::getDealerReturnCommission, shopDealerCommission)
                    // 1.1 退回资金
                    .set(OrderBaseEntity::getOrderRefundAmount, returnCash)
                    // 插入买家id，为资金回退做准备
                    .set(OrderBaseEntity::getSellerUserId, sellerInfo.get("userId")));

            Map<String, Object> orderReturnMap = new HashMap<>();
            Integer isExistData = Math.toIntExact(baseMapper.selectCount(new LambdaQueryWrapper<OrderReturnEntity>()
                    .eq(OrderReturnEntity::getOrderId, applyReturnOrders.getOrderId())));

            baseMapper.updateOdbOrderReturn(applyReturnOrders.getOrderId());
            List<String> voucher = new ArrayList<>();
            if (applyReturnOrders.getReturnVoucher() != null) {
                voucher = applyReturnOrders.getReturnVoucher();
                String returnVoucher = JSON.toJSONString(voucher);
                orderReturnMap.put("returnVoucher", returnVoucher);
            }
            orderReturnMap.put("orderId", applyReturnOrders.getOrderId());
            orderReturnMap.put("returnState", 1);
            orderReturnMap.put("returnShopHandle", 1);
            // 1为退款申请,2为退货申请
            orderReturnMap.put("returnType", 2);
            orderReturnMap.put("returnCash", returnCash);
            orderReturnMap.put("returnMethodStatus", 3);
            orderReturnMap.put("returnReasonId", applyReturnOrders.getReturnReasonId());
            orderReturnMap.put("returnReason", applyReturnOrders.getReturnReason());
            orderReturnMap.put("returnMessage", applyReturnOrders.getReturnMessage());
            orderReturnMap.put("orderGoodsNum", orderGoodsNum);
            orderReturnMap.put("orderDiscountFee", orderDiscountFee);
            // 订单总额，包含运费
            orderReturnMap.put("orderAmount", orderBase.getOrderGoodsAmount().doubleValue() + orderBase.getOrderShippingFee());
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

            if (isExistData == 1) {
                OrderReturnStatusDTO returnStatus = baseMapper.queryOrderReturnData(applyReturnOrders.getOrderId());
                // 存在更新数据
                //在卖家没介入之前可以一直修改但不改失效时间
                orderReturnMap.put("historicalReturnStatus", "");
                if (returnStatus.getReturnState() != 1) {
                    SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Calendar calendar = Calendar.getInstance();
                    String nowDate = DateUtils.getTime();
                    Date date = null;
                    try {
                        date = formatter1.parse(nowDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    calendar.setTime(date);
                    calendar.add(Calendar.DATE, 1);
                    //处理时效时间
                    Date calendarTime = calendar.getTime();
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
                baseMapper.updateOrderReturn(orderReturnMap);

            } else {
                // 退多件暂时不存
                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                String dateString = formatter.format(new Date());
                String returnCode = "TD-" + userInfo.getUserId() + "-" + orderBase.getShopId() + "-" + dateString + "-"
                        + "0001";
                // 退货编号
                orderReturnMap.put("returnCode", returnCode);

                SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Calendar calendar = Calendar.getInstance();
                String nowDate = DateUtils.getTime();
                Date date = null;
                try {
                    date = formatter1.parse(nowDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                calendar.setTime(date);
                calendar.add(Calendar.DATE, 1);
                //处理时效时间
                Date calendarTime = calendar.getTime();
                orderReturnMap.put("returnAddTime", nowDate);
                orderReturnMap.put("expirationTime", formatter1.format(calendarTime));


                Integer three = baseMapper.returnMoney(orderReturnMap);

                if (1 != one || 0 == two || 1 != three) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return ResponseResult.fail(BizCodeEnume.APPLICATION_FAILED);
                }

                // 使用feign在其他库插入数据
                Map<String, String> userRecordMap = new HashMap<String, String>();
                userRecordMap.put("orderId", applyReturnOrders.getOrderId());
                userRecordMap.put("userId", userInfo.getUserId());
                userRecordMap.put("userNickname", userInfo.getUserName());
                // 1。1将总金额改为 退款金额
                userRecordMap.put("recordMoney", returnCash.toString());
                userRecordMap.put("recordDate", DateUtils.getTime());
                userRecordMap.put("recordYear", DateUtils.getYear());
                userRecordMap.put("recordMonth", DateUtils.getMonth());
                userRecordMap.put("recordDay", DateUtils.getDay());
                userRecordMap.put("recordTitle", "退款");
                userRecordMap.put("recordDesc", "订单号：" + applyReturnOrders.getOrderId());
                userRecordMap.put("recordTime", DateUtils.getTime());
                userRecordMap.put("tradeTypeId", "5");
                userRecordMap.put("userType", "1");
                userRecordMap.put("recordStatus", "8");
                userRecordMap.put("recordPayorder", orderBase.getPaymentNumber());
                userRecordMap.put("recordPaytime", DateUtils.getTime());

                Map<String, String> sellerRecordMap = new HashMap<String, String>();
                sellerRecordMap.put("orderId", applyReturnOrders.getOrderId());
                sellerRecordMap.put("userId", sellerInfo.get("userId"));
                sellerRecordMap.put("userNickname", sellerInfo.get("sellerName"));
                // 1。1将总金额改为 退款金额
                sellerRecordMap.put("recordMoney", returnCash.toString());
                sellerRecordMap.put("recordDate", DateUtils.getTime());
                sellerRecordMap.put("recordYear", DateUtils.getYear());
                sellerRecordMap.put("recordMonth", DateUtils.getMonth());
                sellerRecordMap.put("recordDay", DateUtils.getDay());
                sellerRecordMap.put("recordTitle", "退款");
                sellerRecordMap.put("recordDesc", "订单号：" + applyReturnOrders.getOrderId());
                sellerRecordMap.put("recordTime", DateUtils.getTime());
                sellerRecordMap.put("tradeTypeId", "5");
                sellerRecordMap.put("userType", "2");
                sellerRecordMap.put("recordStatus", "8");
                sellerRecordMap.put("recordPayorder", orderBase.getPaymentNumber());
                sellerRecordMap.put("recordPaytime", DateUtils.getTime());

                // 发送消息模板
                Map<String, Object> messageMap = new HashMap<>();
                List<String> messageList = new ArrayList<>();
                messageList.add(applyReturnOrders.getOrderId());
                messageMap.put("list", messageList);
                messageMap.put("orderId", applyReturnOrders.getOrderId());
                String returnGoodsName = baseMapper.queryReturnGoodsNameByOrderId(applyReturnOrders.getOrderId());
                messageMap.put("goodsName", returnGoodsName);
                messageMap.put("userId", orderBase.getSellerUserId());
                messageMap.put("sellerId", orderBase.getSellerUserId());
                messageMap.put("sellerName", orderBase.getSellerUserName());
                messageMap.put("messageType", 1);
                messageMap.put("shopId", orderBase.getShopId());
                messageMap.put("messageTitle", "退货通知");
                messageMap.put("templateId", CommonType.messageTemplate.CODE_I.getCode());
                baseServiceClient.sendMessageTemplate(messageMap);

                Map<String, Map<String, String>> returnMap = new HashMap<String, Map<String, String>>();
                returnMap.put("user", userRecordMap);
                returnMap.put("seller", sellerRecordMap);
                // 使用feign在其他库插入数据
                ResponseResult torf = payMentServiceClient.insertRecord(returnMap);
                if (CommonStatusEnum.SUCCESS.getCode() != torf.getCode()) {
                    GlobalTransactionContext.reload(RootContext.getXID()).rollback();
                    return ResponseResult.fail(BizCodeEnume.APPLICATION_FAILED);
                }
            }
            if (!applyReturnGoods.isEmpty()) {
                for (ApplyReturnGoodDTO goods : applyReturnGoods) {
                    OrderDealerInfoEntity dealerInfoEntity = orderDealerInfoMapper.selectOne(new LambdaQueryWrapper<OrderDealerInfoEntity>()
                            .eq(OrderDealerInfoEntity::getOrderId, applyReturnOrders.getOrderId())
                            .eq(OrderDealerInfoEntity::getGoodsId, goods.getOrderGoodsId()));
                    if (null != dealerInfoEntity) {
                        BigDecimal totalTaxInclusiveAmount = dealerInfoEntity.getTotalTaxInclusiveAmount().subtract(dealerInfoEntity.getDealerDiscountAmout());
                        List<ApplyReturnGoodDTO> applyReturnSonGoods = new ArrayList<>();

                        ApplyReturnGoodDTO dto = new ApplyReturnGoodDTO();
                        //经销商品id
                        dto.setOrderGoodsId(dealerInfoEntity.getSupplierGoodsId().toString());
                        //经销商品退货数量
                        dto.setOrderGoodsNum(goods.getOrderGoodsNum());
                        //经销商品图片
                        dto.setOrderGoodsPic(goods.getOrderGoodsPic());
                        //经销商品价格
                        dto.setOrderGoodsPrice(totalTaxInclusiveAmount.toString());
                        applyReturnSonGoods.add(dto);
                        //经销商品集合
                        applyReturnOrders.setApplyReturnGoods(applyReturnSonGoods);
                        //经销订单
                        applyReturnOrders.setOrderId(dealerInfoEntity.getOriginalOrder());
                        //经销订单金额
                        applyReturnOrders.setReturnCash(Double.valueOf(totalTaxInclusiveAmount.toString()));
                        applyReturnOrders.setIsSupplier(1);
                        applyReturnMore(applyReturnOrders);
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
                    GlobalTransactionContext.reload(RootContext.getXID()).rollback();
                    throw new DefinedException(result);
                }

                //获取售后单号
                OrderReturnGoodsVO orderReturnGoodsVO = JSONObject.parseObject(JSONObject.toJSONString(result.getData()), OrderReturnGoodsVO.class);
                LOG.info("orderId:" + orderYiyatongEntity.getOrderId() + "-怡亚通售后单号-" + orderReturnGoodsVO.getReturnSn());
                orderYiyatongEntity.setReturnSn(orderReturnGoodsVO.getReturnSn());
                //保存售后单号
                orderYiyatongMapper.updateById(orderYiyatongEntity);
            } else if (orderBase.getOrderSource() == CommonType.SupplierSource.JING_DONG.getCode()) {
                // 判断是京东订单
                // 封装商城退单商品信息
                JingDongReturnOrderDTO jingDongReturnOrderDTO = new JingDongReturnOrderDTO();
                jingDongReturnOrderDTO.setOrderId(applyReturnOrders.getOrderId());
                jingDongReturnOrderDTO.setReturnReason(applyReturnOrders.getReturnReason());
                jingDongReturnOrderDTO.setReturnMethodType(CommonType.returnMethodType.RETURN.getCode());
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
        } catch (Exception e) {
            e.printStackTrace();
            // 回滚
            GlobalTransactionContext.reload(RootContext.getXID()).rollback();
            return ResponseResult.fail(e.getMessage());
        }
        return ResponseResult.success(BizCodeEnume.APPLICATION_SUCCESS.getMsg());
    }

    @Override
    public ResponseResult onlyReturnMoney(Map<String, Object> map) throws TransactionException {
        // 从map中获取token字符串
        String token = map.get("token").toString();
        // 从Redis中获取存储的用户信息字符串，键名由用户token前缀和token拼接而成
        String userInfoString = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + token);
        // 将用户信息字符串反序列化为UserInfoDTO对象
        UserInfoDTO UserInfoDTO = JSON.parseObject(userInfoString, UserInfoDTO.class);
        // 从UserInfoDTO中提取出UserInfoVO对象，用于后续处理
        UserInfoVO userInfo = UserInfoDTO.getUser();
        // 调用onlyReturnMoney方法处理业务逻辑，并传入map和userInfo，返回处理结果
        ResponseResult result = onlyReturnMoney(map, userInfo);
        // 返回处理结果
        return result;

    }

    @Override
    public ResponseResult listReturnOrderByRT(Map<String, Object> paraMap) {
        // 从参数映射中获取token
        String token = (String) paraMap.get("token");
        // 根据token解析用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        // 如果用户信息为空，则返回非法token的错误响应
        if (userInfo == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
        }
        // 将用户ID存入参数映射中
        paraMap.put("userId", userInfo.getUserId());
        // 初始化用户ID列表，用于后续查询
        List<String> userIdList = new ArrayList<>();
        // 将用户ID添加到列表中
        userIdList.add(userInfo.getUserId());
        // 将用户ID列表存入参数映射中
        // 新增企业成员信息
        paraMap.put("userIdList", userIdList);
        // 根据参数映射查询退货订单信息
        List<GoodsDetailsVO> goodsDetails;
        IPage<ReturnOrderVO> page = baseMapper.getBlogByType(new Query<ReturnOrderVO>().getPage(paraMap),
                paraMap);

        // 遍历查询结果，处理每个退货订单
        for (ReturnOrderVO returnOrder : page.getRecords()) {
            // 根据订单ID查询退货平台相关信息
            OrderReturnPlatformEntity pl = orderReturnPlatformMapper.selectOne(new LambdaQueryWrapper<OrderReturnPlatformEntity>()
                    .eq(OrderReturnPlatformEntity::getOrderId, returnOrder.getOrderId()));
            // 如果平台信息存在，则设置退货状态和干预状态
            if (pl != null && !returnOrder.getReturnMethodStatus().equals(CommonType.returnMethodType.BARTER.getCode())) {
                returnOrder.setReturnState("9");
                returnOrder.setInterventionState(pl.getInterventionState());
            }
            // 根据退货类型查询商品详情
            if ("1".equals(returnOrder.getReturnType())) {
                goodsDetails = orderGoodsMapper.listGoodsDetails(returnOrder.getOrderId());
                // 设置商品详情
                returnOrder.setGoodsDetails(goodsDetails);
            } else {
                // 初始化选择映射，用于查询退货商品详情
                // 退货，只有一种商品情况
                Map<String, String> selectMap = new HashMap<>();
                // 设置订单ID
                selectMap.put("orderId", returnOrder.getOrderId());
                goodsDetails = orderGoodsMapper.listReturnGoods(selectMap);
                // 如果是换货订单，为每个商品添加换货商品详情
                if (returnOrder.getReturnMethodStatus().equals(CommonType.returnMethodType.BARTER.getCode())) {
                    for (GoodsDetailsVO goods : goodsDetails) {
                        if (goods.getBarterGoodsId() != null) {
                            ResponseResult<OrderGoodsBarterVO> barterGoods = goodServiceClient.selectBarterGoods(goods.getBarterGoodsId());
                            goods.setBarterGoods(barterGoods.getData());
                        }
                    }
                }
                // 设置商品详情
                returnOrder.setGoodsDetails(goodsDetails);
            }
            List<String> returnPaymentVoucherList = JSON.parseArray(returnOrder.getReturnPaymentVoucher(), String.class);
            returnOrder.setReturnPaymentVoucherList(returnPaymentVoucherList);
        }
        // 返回处理后的分页结果
        return ResponseResult.success(PageUtils.getPage(page));

    }

    @Override
    public ResponseResult returnDetail(Map<String, String> map) {
        String token = map.get("token");
        @SuppressWarnings("unused")
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        SellerOrderDetailVO sellerOrderDetail = baseMapper.getSellerOrderDetailById(map.get("returnCode"), userInfo.getShopId());
        if (sellerOrderDetail.getReturnVoucher() != null) {
            List<String> voucher = JSON.parseArray(sellerOrderDetail.getReturnVoucher(), String.class);
            sellerOrderDetail.setReturnVouchers(voucher);
        }
        Map<String, Object> expressMap = new HashMap<>(1);
        expressMap.put("token", token);
        //查找平台所有的快递公司
        ResponseResult expressQueryResults = baseServiceClient.queryExpressList(expressMap);
        if (null != expressQueryResults.getData()) {
            List<LinkedHashMap<Object, Object>> list = (List<LinkedHashMap<Object, Object>>) expressQueryResults.getData();
            for (LinkedHashMap<Object, Object> result : list) {
                if (null != result.get("expressId") && null != sellerOrderDetail.getReturnPostCode()) {
                    if (result.get("expressId").toString().equals(sellerOrderDetail.getReturnPostCode())) {
                        sellerOrderDetail.setReturnPostName((String) result.get("expressName"));
                    }
                }
            }

        }
        //如果是供应商的单
        if (null != sellerOrderDetail.getOrderType() && 2 == sellerOrderDetail.getOrderType()) {
            sellerOrderDetail.setReturnCash(sellerOrderDetail.getReturnCash());
        }

        Map<String, String> selectMap = new HashMap<String, String>();
        selectMap.put("orderId", sellerOrderDetail.getOrderId());
        selectMap.put("shopId", userInfo.getShopId());
        Integer methodStatus = sellerOrderDetail.getOrderType();
        List<GoodsDetailsVO> listReturnGoods = null;
        if (null != methodStatus) {
            if (methodStatus == 2) {
                listReturnGoods = orderGoodsMapper.listReturnGoods(selectMap);
            } else {
                listReturnGoods = orderGoodsMapper.listReturnGoods1(selectMap);
            }
        }
        sellerOrderDetail.setGoodsDetails(listReturnGoods);

        //判断是否是代发货订单
        if (null != sellerOrderDetail.getDropShipping() && 1 == sellerOrderDetail.getDropShipping() && userInfo.getShopType() == 2) {
            if (null != sellerOrderDetail.getSupperReturnPrice()) {
                sellerOrderDetail.setReturnCash(sellerOrderDetail.getSupperReturnPrice().doubleValue());
                sellerOrderDetail.setOrderPaymentAmount(sellerOrderDetail.getSupperPaymentAmount().toString());
            }
            if (null != sellerOrderDetail.getSupperRefundPrice()) {
                sellerOrderDetail.setReturnCash(sellerOrderDetail.getSupperRefundPrice().doubleValue());
                sellerOrderDetail.setOrderPaymentAmount(sellerOrderDetail.getSupperPaymentAmount().toString());
            }
            List<GoodsDetailsVO> goodsDetails = sellerOrderDetail.getGoodsDetails();
            if (!CollectionUtils.isEmpty(goodsDetails)) {
                for (GoodsDetailsVO goodsDetail : goodsDetails) {
                    goodsDetail.setOrderGoodsPaymentAmount(sellerOrderDetail.getSupperPaymentAmount().doubleValue());
                }
            }
        }

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
            sellerOrderDetail.setStepState(4);
        }
        //退款关闭（新增的）
        if (sellerOrderDetail.getReturnState() == 8) {
            sellerOrderDetail.setStepState(8);
        }
        //平台介入（新增的）
        if (sellerOrderDetail.getReturnState() == 9) {
            sellerOrderDetail.setStepState(9);
        }
        //验收不通过（新增的）
        if (sellerOrderDetail.getReturnState() == 10) {
            sellerOrderDetail.setStepState(10);
        }
        //待买家退货（新增的）
        if (sellerOrderDetail.getReturnState() == 11) {
            sellerOrderDetail.setStepState(11);
        }
        //待验收（新增的）
        if (sellerOrderDetail.getReturnState() == 12) {
            sellerOrderDetail.setStepState(12);
        }
        return ResponseResult.success(sellerOrderDetail);
    }

    @Override
    public ResponseResult refundDetail(Map<String, String> map) {
        // 从传入的map中获取token，用于后续验证和获取用户信息
        String token = map.get("token");
        // 根据token解析获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        // 从map中获取返回码，用于根据不同的返回码处理不同的业务逻辑
        String returnCode = map.get("returnCode");
        // 根据返回码和店铺ID查询卖家订单详情
        SellerOrderDetailVO sellerOrderDetail = baseMapper.getSellerOrderDetailById(returnCode, userInfo.getShopId());
        // 根据订单的退货状态设置订单流程步骤状态
        // 如果订单退货状态为1，表示退货中，设置步骤状态为1
        if (sellerOrderDetail.getReturnState() == 1) {
            sellerOrderDetail.setStepState(1);
        }
        // 如果订单退货状态为3或4，表示退货已完成或已拒绝，设置步骤状态为2
        if (sellerOrderDetail.getReturnState() == 3 || sellerOrderDetail.getReturnState() == 4) {
            sellerOrderDetail.setStepState(2);
        }
        // 如果订单退货状态为5，表示退货申请已取消，设置步骤状态为3
        if (sellerOrderDetail.getReturnState() == 5) {
            sellerOrderDetail.setStepState(3);
        }
        // 返回处理后的卖家订单详情，标记为成功响应
        return ResponseResult.success(sellerOrderDetail);

    }

    @Override
    public ResponseResult returnShipping(Map<String, String> paraMap) {
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(paraMap.get("token"));
        if (null == userInfo) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());
        }
        Map<String, Object> orderGoodsMap = new HashMap<>();
        String orderId = baseMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>()
                .eq(OrderReturnEntity::getReturnCode, paraMap.get("returnCode"))).getOrderId();
        //查询退款详情
        ReturnOrderDetailVO returnOrderDetail = baseMapper.queryOrderReturnDetail(orderId);
        if (Integer.parseInt(returnOrderDetail.getReturnState()) == 10) {
            Integer resubmissionsNumber = returnOrderDetail.getResubmissionsNumber() + 1;
            orderGoodsMap.put("resubmissionsNumber", resubmissionsNumber);
        }

        orderGoodsMap.put("returnCode", paraMap.get("returnCode"));
        // 退货物流单号
        orderGoodsMap.put("returnShippingCode", paraMap.get("returnShippingCode"));
        // 联系电话
        orderGoodsMap.put("returnMobile", paraMap.get("returnMobile"));
        // 物流id
        orderGoodsMap.put("returnPostCode", paraMap.get("returnPostCode"));
        orderGoodsMap.put("returnShopHandle", 6);
        // 订单状态
        orderGoodsMap.put("returnState", "6");
        orderGoodsMap.put("callStatus", 0);
        Calendar instance = Calendar.getInstance();
        instance.setTime(new Date());
        instance.add(Calendar.DATE, 7);
        Date instanceTime = instance.getTime();
        orderGoodsMap.put("expirationTime", instanceTime);

        Integer torf = baseMapper.updateOrderReturnById(orderGoodsMap);
        // 判断是否是京东订单
        if(returnOrderDetail.getOrderSource() == CommonType.SupplierSource.JING_DONG.getCode()){
            // 封装商城退单商品信息
            JingDongReturnOrderDTO jingDongReturnOrderDTO = new JingDongReturnOrderDTO();
            jingDongReturnOrderDTO.setReturnShippingCode(paraMap.get("returnShippingCode"));
            //查询快递公司名字
            ResponseResult express = baseServiceClient.getBaseExpressInfoById(Integer.valueOf(paraMap.get("returnPostCode")));
            Map shipperCode = JSON.parseObject(JSON.toJSONString(express.getData()), Map.class);
            jingDongReturnOrderDTO.setExpressName(shipperCode.get("expressName").toString());
            // 根据订单号查询京东售后单信息
            List<JingDongReturnGoodsDTO> jingDongReturnGoodsDTOS = orderGoodsMapper.queryJdReturnOrderGoodsInfo(orderId);
            jingDongReturnOrderDTO.setGoodsList(jingDongReturnGoodsDTOS);
            // 调用京东接口 填写运单信息
            goodServiceClient.updateJdShippingInfo(jingDongReturnOrderDTO);

        }


        if (1 == torf) {
            return ResponseResult.success();
        }
        return ResponseResult.fail(BizCodeEnume.FILLING_LOGISTICS_ERROR.getCode(), BizCodeEnume.FILLING_LOGISTICS_ERROR.getMsg());
    }

    /** 申请售后时，不更改orderstatus */
    @Transactional
    public ResponseResult onlyReturnMoney(Map<String, Object> paraMap, UserInfoVO userInfo) {
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            List<OrderBaseVO> orderBases = orderBaseMapper.getOrderBaseById(paraMap.get("orderId").toString());
            OrderBaseVO orderBase = orderBases.get(0);
            //商家未操作之前，用户是可以操作的
            if (!orderBase.getOrderRefundStatus().equals(CommonType.orderRefundStatus.NO_REFUND.getCode().toString())) {
                if (orderBase.getOrderRefundStatus().equals(CommonType.orderRefundStatus.REFUND_COMPLETED.getCode().toString())) {
                    //当前订单退款中,请勿重复操作
                    return ResponseResult.fail(BizCodeEnume.ORDER_STATUS_CHANGE);
                } else {
                    Integer returnState = Integer.parseInt(orderBase.getOrderRefundStatus());
                    if (!returnState.equals(CommonType.returnState.NEW_LAUNCH_PENDING_SELLER_REVIEW.getCode()) &&
                            !returnState.equals(CommonType.returnState.SELLER_FAILED_THE_REVIEW.getCode()) &&
                            !returnState.equals(CommonType.returnState.USER_CANCELS_REFUND_CLOSED.getCode()) &&
                            !returnState.equals(CommonType.returnState.USER_CANCELS_RETURN_CLOSED.getCode())) {
                        return ResponseResult.fail(BizCodeEnume.ORDER_STATUS_CHANGE);
                    }
                }
            }
            List<Integer> orderStatusList = Arrays.asList(3, 4, 5, 6, 8, 9, 11, 12);
            if (!orderStatusList.contains(orderBase.getOrderStatus())) {
                return ResponseResult.fail(BizCodeEnume.ORDER_STATUS_CHANGE);
            }
            // 实付金额
            double orderPaymentMmount = orderBase.getOrderPaymentAmount().doubleValue();
            // 退款金额
            double returnCash = Double.parseDouble(paraMap.get("returnCash").toString());
            //售后商品id
            //售后商品id
            List<ApplyReturnGoodDTO> returnGoods = new ArrayList<>();
            List goodsList = (List) paraMap.get("applyReturnGoods");
            if (goodsList.getFirst() instanceof Integer) {
                // 如果列表项为Integer类型，表示是商品ID列表
                List<Integer> goodsIds = (List) paraMap.get("applyReturnGoods");
                for (Integer id : goodsIds) {
                    ApplyReturnGoodDTO dto = new ApplyReturnGoodDTO();
                    // 查询该商品的购买数量
                    OrderGoodsEntity orderGoods = orderGoodsMapper.selectOne(new LambdaQueryWrapper<OrderGoodsEntity>()
                            .select(OrderGoodsEntity::getOrderGoodsNum,OrderGoodsEntity::getDealerCommission,OrderGoodsEntity::getOrderGoodsAmount,OrderGoodsEntity::getVoucherPrice)
                            .eq(OrderGoodsEntity::getOrderId,String.valueOf(paraMap.get("orderId")))
                            .eq(OrderGoodsEntity::getGoodsId, id));
                    dto.setOrderGoodsId(String.valueOf(id));
                    dto.setOrderGoodsNum(String.valueOf(orderGoods.getOrderGoodsNum()));
                    dto.setDealerCommission(BigDecimal.valueOf(orderGoods.getDealerCommission()));
                    dto.setOrderGoodsAmount(orderGoods.getOrderGoodsAmount() - orderGoods.getVoucherPrice());
                    returnGoods.add(dto);
                }
            } else {
                // 否则，直接将列表转换为ApplyReturnGoodDTO列表
                returnGoods = JSON.parseArray(JSON.toJSONString(paraMap.get("applyReturnGoods")), ApplyReturnGoodDTO.class);
            }
            // 如果订单售后时间小于当前时间订单不可退
            if (null != orderBase.getOrderLastReturnTime() && DateUtils.compareDate(orderBase.getOrderLastReturnTime(), DateUtils.getNowDate())) {
                return ResponseResult.fail(BizCodeEnume.ORDER_AFTER_TIME_EXCEED);
            }
            // 如果退款金额大于实付金额，取实付金额
            if (returnCash > orderPaymentMmount) {
                returnCash = orderPaymentMmount;
            }
            // 二开售后校验（before*）：退款金额核定前回调 AfterSaleCreateExtension，只读、可中断、可调整核定金额。
            // ctx.abort → 抛 PluginAbortException(7001)；回读 approveAmount 并夹在 [0, 实付]（夹取在网关内完成）。
            {
                Map<String, Object> afterSaleParams = new HashMap<>(paraMap);
                com.houjiang.order.plugin.AfterSaleCreateGateway.Result afterSaleResult =
                        afterSaleCreateGateway.beforeCreate(orderBase, returnGoods,
                                com.houjiang.order.plugin.AfterSaleCreateGateway.TYPE_REFUND,
                                null, BigDecimal.valueOf(orderPaymentMmount),
                                BigDecimal.valueOf(returnCash),
                                paraMap.get("returnReason") == null ? null : String.valueOf(paraMap.get("returnReason")),
                                afterSaleParams);
                returnCash = afterSaleResult.approveAmount().doubleValue();
            }
            //清空平台介入信息
            orderReturnPlatformMapper.delete(new LambdaQueryWrapper<OrderReturnPlatformEntity>()
                    .eq(OrderReturnPlatformEntity::getOrderId, String.valueOf(paraMap.get("orderId"))));

            // 去userms模块查找用户信息 "sellerId","sellerName","shopId","userId"
            Map<String, String> getSerllerInfoMap = new HashMap<>();
            getSerllerInfoMap.put("shopId", orderBase.getShopId().toString());
            Map<String, String> sellerInfo = (Map<String, String>) userServiceClient.getSellerInfo(getSerllerInfoMap).getData();

            Map<String, Object> orderBaseMap = new HashMap<>();
            orderBaseMap.put("orderStatus", 6);
            orderBaseMap.put("orderRefundStatus", 1);
            orderBaseMap.put("orderFinishedTime", DateUtils.getNowDate());
            orderBaseMap.put("orderId", paraMap.get("orderId"));
            orderBaseMap.put("orderRefundAmount", returnCash);
            orderBaseMap.put("returnMethodType", 2);
            // 填写卖家id，为资金回退做准备
            orderBaseMap.put("sellerUserId", sellerInfo.get("userId"));

            Integer one = orderBaseMapper.update(null, new LambdaUpdateWrapper<OrderBaseEntity>()
                    .eq(OrderBaseEntity::getOrderId, paraMap.get("orderId"))
                    .set(OrderBaseEntity::getOrderStatus, CommonType.orderStatus.CONFIRM_RECEIPT.getCode())
                    .set(OrderBaseEntity::getOrderRefundStatus, CommonType.orderRefundStatus.REFUNDING.getCode())
                    .set(OrderBaseEntity::getOrderFinishedTime, DateUtils.getNowDate())
                    .set(OrderBaseEntity::getReturnMethodType, CommonType.returnMethodType.ONLY_REFUND.getCode())
                    // 1.1 退回资金
                    .set(OrderBaseEntity::getOrderRefundAmount, returnCash)
                    // 插入买家id，为资金回退做准备
                    .set(OrderBaseEntity::getSellerUserId, sellerInfo.get("userId")));

            //由于要展示退货详情所以商品表撤销是没清状态(所以二次申请要清空状态)
            Map<String, Object> orderGoodsMap1 = new HashMap<String, Object>();
            orderGoodsMap1.put("goodsRefundStatus", 0);
            orderGoodsMap1.put("goodsReturnStatus", 0);
            orderGoodsMap1.put("orderId", paraMap.get("orderId"));
            baseMapper.updateOrderGoodsById(orderGoodsMap1);

            Map<String, Object> orderGoodsMap = new HashMap<String, Object>();
            Integer two = null;
            if (returnGoods != null && !returnGoods.isEmpty()) {
                for (ApplyReturnGoodDTO returnGood : returnGoods) {
                    orderGoodsMap.put("goodsRefundStatus", 1);
                    orderGoodsMap.put("goodsReturnStatus", 0);
                    orderGoodsMap.put("orderGoodsStatus", 6);
                    orderGoodsMap.put("orderId", paraMap.get("orderId"));
                    orderGoodsMap.put("goodsId", returnGood.getOrderGoodsId());
                    orderGoodsMap.put("orderGoodsReturnnum", returnGood.getOrderGoodsNum());
                    orderGoodsMap.put("dealerCommission", returnGood.getDealerCommission());
                    orderGoodsMap.put("goodsReturnPrice", returnGood.getOrderGoodsAmount());
                    two = baseMapper.updateOrderGoodsById1(orderGoodsMap);
                }
            }
            Map<String, Object> orderReturnMap = new HashMap<>();
            Integer isExistData = Math.toIntExact(baseMapper.selectCount(new LambdaQueryWrapper<OrderReturnEntity>()
                    .eq(OrderReturnEntity::getOrderId, paraMap.get("orderId"))));

            if (paraMap.get("returnVoucher") != null) {
                List<String> complaintsImages = (List<String>) paraMap.get("returnVoucher");
                String returnVoucher = JSON.toJSONString(complaintsImages);
                orderReturnMap.put("returnVoucher", returnVoucher);
            }
            orderReturnMap.put("orderId", paraMap.get("orderId"));
            orderReturnMap.put("returnState", 1);
            orderReturnMap.put("returnShopHandle", 1);
            orderReturnMap.put("returnMethodStatus", 2);
            // 1为退款申请,2为退货申请
            orderReturnMap.put("returnType", 1);
            orderReturnMap.put("returnReasonId", paraMap.get("returnReasonId"));
            orderReturnMap.put("returnReason", paraMap.get("returnReason"));
            orderReturnMap.put("returnMessage", paraMap.get("returnMessage"));
            orderReturnMap.put("returnCash", returnCash);
            orderReturnMap.put("returnRptCash", orderBase.getOrderRptPrice());
            orderReturnMap.put("buyerUserId", userInfo.getUserId());
            orderReturnMap.put("shopId", orderBase.getShopId());
            orderReturnMap.put("shopName", orderBase.getShopName());
            orderReturnMap.put("buyerUserAccount", userInfo.getUserName());
            orderReturnMap.put("orderAmount", orderBase.getOrderGoodsAmount().doubleValue() + orderBase.getOrderShippingFee());
            orderReturnMap.put("orderGoodsId", "0");
            orderReturnMap.put("orderGoodsName", "0");
            orderReturnMap.put("orderGoodsPrice", "0");
            orderReturnMap.put("orderGoodsNum", "0");
            orderReturnMap.put("orderGoodsPic", "0");
            orderReturnMap.put("returnAddrId", 0);
            orderReturnMap.put("returnPostCode", 0);
            orderReturnMap.put("sellerUserId", sellerInfo.get("userId"));

            baseMapper.updateOdbOrderReturn(String.valueOf(paraMap.get("orderId")));
            if (isExistData == 1) {
                OrderReturnStatusDTO returnStatus = baseMapper.queryOrderReturnData(String.valueOf(paraMap.get("orderId")));
                // 存在更新数据
                //在卖家没介入之前可以一直修改但不改失效时间
                orderReturnMap.put("historicalReturnStatus", "");
                if (returnStatus.getReturnState() != 1) {
                    SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Calendar calendar = Calendar.getInstance();
                    String nowDate = DateUtils.getTime();
                    Date date = null;
                    try {
                        date = formatter1.parse(nowDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    calendar.setTime(date);
                    calendar.add(Calendar.DATE, 1);
                    //处理时效时间
                    Date calendarTime = calendar.getTime();
                    orderReturnMap.put("returnAddTime", nowDate);
                    orderReturnMap.put("expirationTime", formatter1.format(calendarTime));
                    Integer returnChangeNum = returnStatus.getReturnChangeNum() + 1;
                    orderReturnMap.put("returnChangeNum", returnChangeNum);
                }
                // 更新申请退款记录
                boolean isUpdateReturnSuccess = baseMapper.updateOrderReturn(orderReturnMap);
                System.out.println("-----isUpdateReturnSUccess-----" + isUpdateReturnSuccess);
                // 发送消息模板
                Map<String, Object> messageMap = new HashMap<>();
                List<String> messageList = new ArrayList<>();
                messageList.add(paraMap.get("orderId").toString());
                messageMap.put("list", messageList);
                messageMap.put("orderId", paraMap.get("orderId"));
                String returnGoodsName = baseMapper.queryReturnGoodsNameByOrderId(paraMap.get("orderId").toString());
                messageMap.put("goodsName", returnGoodsName);
                messageMap.put("userId", orderBase.getSellerUserId());
                messageMap.put("sellerId", orderBase.getSellerUserId());
                messageMap.put("sellerName", orderBase.getSellerUserName());
                messageMap.put("messageType", 1);
                messageMap.put("shopId", orderBase.getShopId());
                messageMap.put("messageTitle", "退款通知");
                messageMap.put("templateId", CommonType.messageTemplate.CODE_H.getCode());
                baseServiceClient.sendMessageTemplate(messageMap);
            } else {
                // 根据商品己算退货价格
                orderReturnMap.put("orderId", paraMap.get("orderId"));

                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                String dateString = formatter.format(new Date());
                Date addTime = new Date();
                Calendar instance = Calendar.getInstance();
                instance.setTime(addTime);
                instance.add(Calendar.DATE, 1);
                Date expirationTime = instance.getTime();

                String returnCode = "TD-" + userInfo.getUserId() + "-" + orderBase.getShopId() + "-" + dateString + "-"
                        + "0001";
                // 退货编号
                orderReturnMap.put("returnCode", returnCode);

                orderReturnMap.put("returnAddTime", df.format(addTime));
                orderReturnMap.put("expirationTime", df.format(expirationTime));

                // 填写卖家id，为资金回退做准备
                orderReturnMap.put("sellerUserId", sellerInfo.get("userId"));
                orderReturnMap.put("returnMobile", orderBase.getOrderReceiverContact());
                Integer three = baseMapper.insertOrderReturn(orderReturnMap);

                if (1 != one || 0 == two || 1 != three) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return ResponseResult.fail();
                }

                // 使用feign在其他库插入数据
                Map<String, String> userRecordMap = new HashMap<String, String>();
                userRecordMap.put("orderId", String.valueOf(paraMap.get("orderId")));
                userRecordMap.put("userId", userInfo.getUserId());
                userRecordMap.put("userNickname", userInfo.getUserName());
                userRecordMap.put("recordMoney", String.valueOf(returnCash));
                userRecordMap.put("recordDate", DateUtils.getTime());
                userRecordMap.put("recordYear", DateUtils.getYear());
                userRecordMap.put("recordMonth", DateUtils.getMonth());
                userRecordMap.put("recordDay", DateUtils.getDay());
                userRecordMap.put("recordTitle", "退款");
                userRecordMap.put("recordDesc", "订单号：" + paraMap.get("orderId"));
                userRecordMap.put("recordTime", DateUtils.getTime());
                userRecordMap.put("tradeTypeId", "5");
                userRecordMap.put("userType", "1");
                userRecordMap.put("recordStatus", "8");
                userRecordMap.put("recordPayorder", orderBase.getPaymentNumber());
                userRecordMap.put("recordPaytime", DateUtils.getTime());

                Map<String, String> sellerRecordMap = new HashMap<String, String>();
                sellerRecordMap.put("orderId", String.valueOf(paraMap.get("orderId")));
                sellerRecordMap.put("userId", sellerInfo.get("userId"));
                sellerRecordMap.put("userNickname", sellerInfo.get("sellerName"));
                sellerRecordMap.put("recordMoney", String.valueOf(returnCash));
                sellerRecordMap.put("recordDate", DateUtils.getTime());
                sellerRecordMap.put("recordYear", DateUtils.getYear());
                sellerRecordMap.put("recordMonth", DateUtils.getMonth());
                sellerRecordMap.put("recordDay", DateUtils.getDay());
                sellerRecordMap.put("recordTitle", "退款");
                sellerRecordMap.put("recordDesc", "订单号：" + paraMap.get("orderId"));
                sellerRecordMap.put("recordTime", DateUtils.getTime());
                sellerRecordMap.put("tradeTypeId", "5");
                sellerRecordMap.put("userType", "2");
                sellerRecordMap.put("recordStatus", "8");
                sellerRecordMap.put("recordPayorder", orderBase.getPaymentNumber());
                sellerRecordMap.put("recordPaytime", DateUtils.getTime());


                // 发送消息模板
                Map<String, Object> messageMap = new HashMap<>();
                List<String> messageList = new ArrayList<>();
                messageList.add(paraMap.get("orderId").toString());
                messageMap.put("list", messageList);
                messageMap.put("orderId", paraMap.get("orderId"));
                String returnGoodsName = baseMapper.queryReturnGoodsNameByOrderId(paraMap.get("orderId").toString());
                messageMap.put("goodsName", returnGoodsName);
                messageMap.put("userId", orderBase.getSellerUserId());
                messageMap.put("sellerId", orderBase.getSellerUserId());
                messageMap.put("sellerName", orderBase.getSellerUserName());
                messageMap.put("messageType", 1);
                messageMap.put("shopId", orderBase.getShopId());
                messageMap.put("messageTitle", "退款通知");
                messageMap.put("templateId", CommonType.messageTemplate.CODE_H.getCode());
                baseServiceClient.sendMessageTemplate(messageMap);

                Map<String, Map<String, String>> returnMap = new HashMap<String, Map<String, String>>();
                returnMap.put("user", userRecordMap);
                returnMap.put("seller", sellerRecordMap);
                ResponseResult torf = payMentServiceClient.insertRecord(returnMap);
                if (CommonStatusEnum.SUCCESS.getCode() != torf.getCode()) {
                    throw new DefinedException(torf);
                }

                if (returnGoods != null && !returnGoods.isEmpty()) {
                    for (ApplyReturnGoodDTO goods : returnGoods) {
                        OrderDealerInfoEntity dealerInfoEntity = orderDealerInfoMapper.selectOne(new LambdaQueryWrapper<OrderDealerInfoEntity>()
                                .eq(OrderDealerInfoEntity::getOrderId, paraMap.get("orderId"))
                                .eq(OrderDealerInfoEntity::getGoodsId, goods.getOrderGoodsId()));
                        if (null != dealerInfoEntity) {
                            List<Integer> goodsIds = new ArrayList<>();
                            paraMap.put("orderId", dealerInfoEntity.getOriginalOrder());
                            goodsIds.add(dealerInfoEntity.getSupplierGoodsId());
                            paraMap.put("applyReturnGoods", goodsIds);
                            onlyReturnMoney(paraMap, userInfo);
                        }
                    }
                }
                //判断是否是怡亚通订单
                if (orderBase.getOrderSource() == 2) {
                    //查询怡亚通订单信息
                    OrderYiyatongEntity orderYiyatongEntity = orderYiyatongMapper.selectOne(new LambdaQueryWrapper<OrderYiyatongEntity>()
                            .eq(OrderYiyatongEntity::getOrderId, paraMap.get("orderId")));
                    ApplyRefundDTO applyRefundDTO = new ApplyRefundDTO();
                    applyRefundDTO.setOrderSn(orderYiyatongEntity.getOrderSn());
                    applyRefundDTO.setReason(paraMap.get("returnReason").toString());
                    applyRefundDTO.setRefundReasonId(paraMap.get("returnReasonId").toString());
                    applyRefundDTO.setReturnType(1);
                    //查询订单详情,获取规格id和商品数
                    OrderDetailDTO dto = new OrderDetailDTO();
                    dto.setOrderSn(orderYiyatongEntity.getOrderSn());
                    ResponseResult orderResult = goodServiceClient.getOrderDetail(dto);
                    ResellerOrderDetailVO vo = JSONObject.parseObject(JSONObject.toJSONString(orderResult.getData()), ResellerOrderDetailVO.class);
                    List<ResellerOrderItemVO> orderGoodsList = vo.getGoodsList();
                    List<ApplyReturnGoodsDTO> ReturnGoodsList = new ArrayList<>();
                    if (orderGoodsList.size() > 0) {
                        for (ResellerOrderItemVO item : orderGoodsList) {
                            ApplyReturnGoodsDTO applyReturnGoodsDTO = new ApplyReturnGoodsDTO();
                            applyReturnGoodsDTO.setGoodsSpecId(item.getGoodsSpecId());
                            applyReturnGoodsDTO.setGoodsQuantity(item.getReturnGoodsNum());
                            applyReturnGoodsDTO.setRefundAmount(new BigDecimal(item.getReturnGoodsNum()).multiply(item.getPerPrice()));
                            ReturnGoodsList.add(applyReturnGoodsDTO);
                        }
                        applyRefundDTO.setGoodsList(ReturnGoodsList);
                    }
                    //申请怡亚通售后
                    ResponseResult result = goodServiceClient.applyRefund(applyRefundDTO);
                    if (CommonStatusEnum.SUCCESS.getCode() != result.getCode()) {
                        GlobalTransactionContext.reload(RootContext.getXID()).rollback();
                        throw new DefinedException(result);
                    }
                    //获取售后单号
                    OrderReturnGoodsVO orderReturnGoodsVO = JSONObject.parseObject(JSONObject.toJSONString(result.getData()), OrderReturnGoodsVO.class);
                    orderYiyatongEntity.setReturnSn(orderReturnGoodsVO.getReturnSn());
                    //保存售后单号
                    orderYiyatongMapper.updateById(orderYiyatongEntity);
                }
            }
        } catch (Exception e) {
            throw new DefinedException(e.getMessage());
        }

        return ResponseResult.success(BizCodeEnume.APPLICATION_SUCCESS);
    }

    /**
     * 1.1 将退款价格改为去掉优惠价格（红包，代金券）
     * 1.2 申请售后时不修改 orderstatus
     *
     * @param paraMap
     * @param userInfo
     * @return
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public ResponseResult applyReturn(Map<String, String> paraMap, UserInfoVO userInfo) throws TransactionException {
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // 将double类型数据转为两位小数
            DecimalFormat decimalFormat = new DecimalFormat("#.00");
            List<OrderBaseVO> orderBases = orderBaseMapper.getOrderBaseById(paraMap.get("orderId"));
            OrderBaseVO orderBase = orderBases.get(0);
            if (!orderBase.getOrderReturnStatus().equals(CommonType.orderReturnStatus.NO_RETURNS.getCode().toString())) {
                //当前订单退货中,请勿重复操作
                return ResponseResult.fail(BizCodeEnume.CURRENT_ORDER_RETURN_IN_PROGRESS);
            }
            // 如果订单售后时间小于当前时间订单不可退
            if (null != orderBase.getOrderLastReturnTime() && DateUtils.compareDate(orderBase.getOrderLastReturnTime(), DateUtils.getNowDate())) {
                return ResponseResult.fail(BizCodeEnume.ORDER_AFTER_TIME_EXCEED);
            }
            List<Integer> orderStatusList = Arrays.asList(3, 4, 5, 6, 8, 9, 11, 12);
            if (!orderStatusList.contains(orderBase.getOrderStatus())) {
                return ResponseResult.fail(BizCodeEnume.ORDER_STATUS_CHANGE);
            }
            // 去userms模块查找用户信息 "sellerId","sellerName","shopId","userId"
            Map<String, String> getSerllerInfoMap = new HashMap<>();
            getSerllerInfoMap.put("shopId", orderBase.getShopId().toString());
            Map<String, String> sellerInfo = (Map<String, String>) userServiceClient.getSellerInfo(getSerllerInfoMap).getData();

            // 根据商品己算退货价格，减去红包和代金券价格
            Double returnCash = Double.parseDouble(paraMap.get("orderGoodsNum"))
                    * Double.parseDouble(paraMap.get("orderGoodsPrice"));
            Double proportional = returnCash / orderBase.getOrderGoodsAmount().doubleValue();
            returnCash = orderBase.getOrderPaymentAmount().doubleValue() * proportional;

            Integer one = orderBaseMapper.update(null, new LambdaUpdateWrapper<OrderBaseEntity>()
                    .eq(OrderBaseEntity::getOrderId, paraMap.get("orderId"))
                    .set(OrderBaseEntity::getOrderReturnStatus, CommonType.orderReturnStatus.RETURNING_GOODS.getCode())
                    .set(OrderBaseEntity::getOrderRefundAmount, orderBase.getOrderPaymentAmount())
                    // 插入买家id，为资金回退做准备
                    .set(OrderBaseEntity::getSellerUserId, sellerInfo.get("userId"))
                    .set(OrderBaseEntity::getOrderFinishedTime, DateUtils.getNowDate()));


            Integer two = orderGoodsMapper.update(null, new LambdaUpdateWrapper<OrderGoodsEntity>()
                    .eq(OrderGoodsEntity::getOrderId, paraMap.get("orderId"))
                    .set(OrderGoodsEntity::getOrderGoodsReturnnum, paraMap.get("orderGoodsNum"))
                    .set(OrderGoodsEntity::getGoodsRefundStatus, CommonType.orderRefundStatus.REFUNDING.getCode()));
            //查询订单商品数量
            List<OrderGoodsEntity> goodsNumList = orderGoodsMapper.selectList(new LambdaQueryWrapper<OrderGoodsEntity>()
                    .eq(OrderGoodsEntity::getOrderId, paraMap.get("orderId")));
            if (null != goodsNumList) {
                for (OrderGoodsEntity orderGoods : goodsNumList) {
                    Integer goodsId = orderGoods.getGoodsId();
                    Integer goodsNum = Integer.parseInt(orderGoods.getOrderGoodsNum());
                    //修改订单退货数量
                    orderGoodsMapper.update(null, new LambdaUpdateWrapper<OrderGoodsEntity>()
                            .eq(OrderGoodsEntity::getGoodsId, goodsId)
                            .eq(OrderGoodsEntity::getOrderId, paraMap.get("orderId"))
                            .set(OrderGoodsEntity::getOrderGoodsReturnnum, goodsNum));
                }

            }
            Map<String, Object> orderReturnMap = new HashMap<>();

            orderReturnMap.put("orderId", paraMap.get("orderId"));
            // 订单总额
            orderReturnMap.put("orderAmount", orderBase.getOrderGoodsAmount());
            // ，不包含运费
            orderReturnMap.put("orderGoodsId", paraMap.get("orderGoodsId"));
            orderReturnMap.put("orderGoodsName", paraMap.get("orderGoodsName"));
            orderReturnMap.put("orderGoodsPrice", paraMap.get("orderGoodsPrice"));
            orderReturnMap.put("orderGoodsNum", paraMap.get("orderGoodsNum"));
            orderReturnMap.put("orderGoodsPic", paraMap.get("orderGoodsPic"));

            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String dateString = formatter.format(new Date());
            String returnCode = "TD-" + userInfo.getUserId() + "-" + orderBase.getShopId() + "-" + dateString + "-"
                    + "0001";
            // 退货编号
            orderReturnMap.put("returnCode", returnCode);
            // 1为退款申请,2为退货申请
            orderReturnMap.put("returnType", "2");
            orderReturnMap.put("buyerUserId", userInfo.getUserId());
            orderReturnMap.put("shopId", orderBase.getShopId());
            orderReturnMap.put("shopName", orderBase.getShopName());
            orderReturnMap.put("buyerUserAccount", userInfo.getUserName());
            orderReturnMap.put("returnAddTime", df.format(new Date()));
            orderReturnMap.put("returnReasonId", paraMap.get("returnReasonId"));
            orderReturnMap.put("returnReason", paraMap.get("returnReason"));
            orderReturnMap.put("returnMessage", paraMap.get("returnMessage"));
            orderReturnMap.put("returnAddrId", 0);
            orderReturnMap.put("returnPostCode", 0);
            // 1-新发起等待卖家审核 2-卖家审核通过 3-卖家审核不通过
            orderReturnMap.put("returnState", 1);
            // 4-卖家收到货物 5-平台审核通过',
            orderReturnMap.put("returnCash", returnCash);
            // 待写红包分担金额
            orderReturnMap.put("returnRptCash",
                    decimalFormat.format(returnCash / orderBase.getOrderGoodsAmount().doubleValue() * orderBase.getOrderRptPrice()));
            orderReturnMap.put("returnShopHandle", 1);
            // 插入买家id，为资金回退做准备
            orderReturnMap.put("sellerUserId", sellerInfo.get("sellerId"));

            int three = insertOrderReturn(paraMap.get("orderId"), orderBase.getOrderGoodsAmount().doubleValue(), paraMap.get("orderGoodsId"), paraMap.get("orderGoodsName"), paraMap.get("orderGoodsPrice"), paraMap.get("orderGoodsNum"), paraMap.get("orderGoodsPic"),
                    "2", userInfo, orderBase, sellerInfo,
                    paraMap.get("returnReasonId"), paraMap.get("returnReason"), paraMap.get("returnMessage"),
                    returnCash, returnCash / orderBase.getOrderGoodsAmount().doubleValue() * orderBase.getOrderRptPrice(), null);

            if (1 != one || 0 == two || 1 != three) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return ResponseResult.fail();
            }


            // 发送消息模板
            Map<String, Object> messageMap = new HashMap<>();
            List<String> messageList = new ArrayList<>();
            messageList.add(paraMap.get("orderId"));
            messageMap.put("list", messageList);
            messageMap.put("orderId", paraMap.get("orderId"));
            String returnGoodsName = baseMapper.queryReturnGoodsNameByOrderId(paraMap.get("orderId"));
            messageMap.put("goodsName", returnGoodsName);
            messageMap.put("userId", orderBase.getSellerUserId());
            messageMap.put("sellerId", orderBase.getSellerUserId());
            messageMap.put("sellerName", orderBase.getSellerUserName());
            messageMap.put("messageType", 1);
            messageMap.put("shopId", orderBase.getShopId());
            messageMap.put("messageTitle", "退货通知");
            messageMap.put("templateId", CommonType.messageTemplate.CODE_I.getCode());
            baseServiceClient.sendMessageTemplate(messageMap);
            // 使用feign在其他库插入数据
            ResponseResult torf = insertRecord(paraMap.get("orderId"), userInfo, String.valueOf(returnCash), orderBase.getPaymentNumber(), sellerInfo);

            if (CommonStatusEnum.SUCCESS.getCode() != torf.getCode()) {
                GlobalTransactionContext.reload(RootContext.getXID()).rollback();
                return ResponseResult.fail();
            }
        } catch (Exception e) {
            GlobalTransactionContext.reload(RootContext.getXID()).rollback();
            return ResponseResult.fail(e.getMessage());
        }
        return ResponseResult.success(BizCodeEnume.APPLICATION_SUCCESS);
    }

    private int insertOrderReturn(String orderId, double orderAmount, String orderGoodsId,
                                  String orderGoodsName, String orderGoodsPrice, String orderGoodsNum, String orderGoodsPic,
                                  String returnType,
                                  UserInfoVO userInfo, OrderBaseVO orderBase, Map<String, String> sellerInfo,
                                  String returnReasonId, String returnReason, String returnMessage,
                                  double returnCash, double returnRptCash, String returnShippingFee) {
        Map<String, Object> orderReturnMap = new HashMap<String, Object>();
        // 不存在创建数据
        orderReturnMap.put("orderId", orderId);
        // 订单总额
        orderReturnMap.put("orderAmount", orderAmount);
        orderReturnMap.put("orderGoodsId", orderGoodsId);
        orderReturnMap.put("orderGoodsName", orderGoodsName);
        orderReturnMap.put("orderGoodsPrice", orderGoodsPrice);
        orderReturnMap.put("orderGoodsNum", orderGoodsNum);
        orderReturnMap.put("orderGoodsPic", orderGoodsPic);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateString = formatter.format(new Date());
        String returnCode = "TD-" + userInfo.getUserId() + "-" + orderBase.getShopId() + "-" + dateString + "-"
                + "0001";
        // 退货编号
        orderReturnMap.put("returnCode", returnCode);
        // 1为退款申请,2为退货申请
        orderReturnMap.put("returnType", returnType);

        orderReturnMap.put("buyerUserId", userInfo.getUserId());
        orderReturnMap.put("shopId", orderBase.getShopId());
        orderReturnMap.put("shopName", orderBase.getShopName());
        orderReturnMap.put("buyerUserAccount", userInfo.getUserName());
        orderReturnMap.put("returnAddTime", DateUtils.getNowDate());
        orderReturnMap.put("returnReasonId", returnReasonId);
        orderReturnMap.put("returnReason", returnReason);
        orderReturnMap.put("returnMessage", returnMessage);

        orderReturnMap.put("returnAddrId", 0);
        orderReturnMap.put("returnPostCode", 0);
        // 1-新发起等待卖家审核 2-卖家审核通过 3-卖家审核不通过
        orderReturnMap.put("returnState", 1);
        orderReturnMap.put("returnShopHandle", 1);

        // 1.1 退回资金 = 应付金额（商品实际支付金额 + 运费）' - 运费
        orderReturnMap.put("returnCash", returnCash);
        orderReturnMap.put("returnRptCash", returnRptCash);
        orderReturnMap.put("returnShippingFee", returnShippingFee);

        // 插入买家id，为资金回退做准备
        orderReturnMap.put("sellerUserId", sellerInfo.get("userId"));
        orderReturnMap.put("returnMobile", orderBase.getOrderReceiverContact());
        Integer three = baseMapper.insertOrderReturn(orderReturnMap);
        return three;
    }

    /**
     * 插入用户和商家的退款记录。
     * 该方法用于在用户和商家之间发生退款行为时，分别在他们的记录中插入相应的退款记录。
     * 这些记录包括退款金额、订单号、退款时间等信息，以供后续的查询和统计使用。
     *
     * @param orderId        订单号，用于关联退款记录和原始订单。
     * @param userInfo       用户信息，包含用户ID和用户名，用于在用户记录中标识退款用户。
     * @param recordMoney    退款金额，记录退款的货币金额。
     * @param recordPayorder 退款订单号，用于唯一标识退款订单。
     * @param sellerInfo     商家信息，包含商家ID和商家名称，用于在商家记录中标识退款商家。
     * @return ResponseResult     返回操作的结果，包括成功与否和可能的错误信息。
     */
    private ResponseResult insertRecord(String orderId, UserInfoVO userInfo, String recordMoney, String recordPayorder, Map<String, String> sellerInfo) {
        // 构建用户退款记录的Map，包含所有必要的退款信息。
        // 使用feign在其他库插入数据
        Map<String, String> userRecordMap = new HashMap<>();
        // 用户相关的退款记录信息设置
        userRecordMap.put("orderId", orderId);
        userRecordMap.put("userId", userInfo.getUserId());
        userRecordMap.put("userNickname", userInfo.getUserName());
        userRecordMap.put("recordMoney", recordMoney);
        userRecordMap.put("recordDate", DateUtils.getNowDateToString());
        userRecordMap.put("recordYear", DateUtils.getYear());
        userRecordMap.put("recordMonth", DateUtils.getMonth());
        userRecordMap.put("recordDay", DateUtils.getDay());
        userRecordMap.put("recordTitle", "退款");
        userRecordMap.put("recordDesc", "订单号：" + orderId);
        userRecordMap.put("recordTime", DateUtils.getNowDateToString());
        userRecordMap.put("tradeTypeId", "5");
        userRecordMap.put("userType", "1");
        userRecordMap.put("recordStatus", "8");
        userRecordMap.put("recordPayorder", recordPayorder);
        userRecordMap.put("recordPaytime", DateUtils.getNowDateToString());

        // 构建商家退款记录的Map，包含所有必要的退款信息。
        Map<String, String> sellerRecordMap = new HashMap<>();
        // 商家相关的退款记录信息设置
        sellerRecordMap.put("orderId", orderId);
        sellerRecordMap.put("userId", sellerInfo.get("userId"));
        sellerRecordMap.put("userNickname", sellerInfo.get("sellerName"));
        sellerRecordMap.put("recordMoney", recordMoney);
        sellerRecordMap.put("recordDate", DateUtils.getNowDateToString());
        sellerRecordMap.put("recordYear", DateUtils.getYear());
        sellerRecordMap.put("recordMonth", DateUtils.getMonth());
        sellerRecordMap.put("recordDay", DateUtils.getDay());
        sellerRecordMap.put("recordTitle", "退款");
        sellerRecordMap.put("recordDesc", "订单号：" + orderId);
        sellerRecordMap.put("recordTime", DateUtils.getNowDateToString());
        sellerRecordMap.put("tradeTypeId", "5");
        sellerRecordMap.put("userType", "2");
        sellerRecordMap.put("recordStatus", "8");
        sellerRecordMap.put("recordPayorder", recordPayorder);
        sellerRecordMap.put("recordPaytime", DateUtils.getNowDateToString());

        // 将用户和商家的退款记录放入一个Map中，以便一次性插入到数据库中。
        Map<String, Map<String, String>> returnMap = new HashMap<String, Map<String, String>>();
        returnMap.put("user", userRecordMap);
        returnMap.put("seller", sellerRecordMap);

        // 调用支付服务客户端的insertRecord方法，插入退款记录，并返回操作结果。
        ResponseResult result = payMentServiceClient.insertRecord(returnMap);
        return result;
    }


    /**
     * 1.1 将退款时钱是商品总价 改为去掉优惠信息价格 申请退款
     * 1.2 申请售后时不更新orderstatus
     *
     * @param paraMap
     * @param userInfo
     * @return
     */
    public ResponseResult applyRefund(Map<String, String> paraMap, UserInfoVO userInfo) {
        Integer returnState = baseMapper.getReturnStateByOrderId(paraMap.get("orderId"));
        // 检查返回状态是否为非处理状态，以防止订单状态被错误地更改
        if (returnState != null && !returnState.equals(CommonType.returnState.NEW_LAUNCH_PENDING_SELLER_REVIEW.getCode())
                && !returnState.equals(CommonType.returnState.SELLER_FAILED_THE_REVIEW.getCode())
                && !returnState.equals(CommonType.returnState.USER_CANCELS_REFUND_CLOSED.getCode())
                && !returnState.equals(CommonType.returnState.USER_CANCELS_RETURN_CLOSED.getCode())) {
            // 如果返回状态不正确，则返回失败响应，提示订单状态更改
            return ResponseResult.fail(BizCodeEnume.ORDER_STATUS_CHANGE);
        }

        // 根据订单ID查询订单基本信息
        List<OrderBaseVO> orderBases = orderBaseMapper.getOrderBaseById(paraMap.get("orderId"));
        // 获取查询结果列表中的第一个订单对象
        OrderBaseVO orderBase = orderBases.getFirst();

        // 定义允许更改订单状态的状态码列表
        List<Integer> orderStatusList = Arrays.asList(3, 4, 5, 6, 8, 9, 11, 12);

        // 检查当前订单状态是否在允许更改的状态列表中
        if (!orderStatusList.contains(orderBase.getOrderStatus())) {
            // 如果订单状态不允许更改，则返回失败响应，提示订单状态更改
            return ResponseResult.fail(BizCodeEnume.ORDER_STATUS_CHANGE);
        }

        if (orderBase.getPayCode() == 14 && userInfo.getIsEmployee() == 1) {
            ResponseResult result = userServiceClient.queryUserInfoByUserId(userInfo.getCreateUser());
            userInfo = JSON.parseObject((String) result.getData(), UserInfoVO.class);
        }
        Integer orderStatus = orderBase.getOrderStatus();
        //虚拟商品 0 是 1 否
        Integer virtualGoods = orderBase.getVirtualGoods();

        //代发货状态下的 虚拟订单  和 自提订单 可以申请退款 //非门店自提的实物订单在商家发货前可以申请退款
        if (null != orderStatus && 4 == orderStatus && (null == orderBase.getVirtualGoods() || 1 == orderBase.getVirtualGoods()) && (2 == orderBase.getOrderIsShopTake())) {
            return ResponseResult.fail();
        }
        //虚拟订单
        if (null != virtualGoods && 0 == virtualGoods) {
            boolean virtualFlag = checkVirtualGoods(paraMap, orderStatus);
            if (!virtualFlag) {
                return ResponseResult.fail(0, "该虚拟商品已过期,无法退款");
            }
        }

        //清空平台介入信息
        orderReturnPlatformMapper.delete(new LambdaQueryWrapper<OrderReturnPlatformEntity>().eq(OrderReturnPlatformEntity::getOrderId, String.valueOf(paraMap.get("orderId"))));

        // 去userms模块查找用户信息 "sellerId","sellerName","shopId","userId"
        Map<String, String> getSerllerInfoMap = new HashMap<>();
        getSerllerInfoMap.put("shopId", orderBase.getShopId().toString());
        Map<String, String> sellerInfo = (Map<String, String>) userServiceClient.getSellerInfo(getSerllerInfoMap).getData();


        Integer one = orderBaseMapper.update(null, new LambdaUpdateWrapper<OrderBaseEntity>()
                .eq(OrderBaseEntity::getOrderId, paraMap.get("orderId"))
                .set(OrderBaseEntity::getOrderRefundStatus, CommonType.orderRefundStatus.REFUNDING.getCode())
                // 1.1 退回资金 = 应付金额（商品实际支付金额 + 运费）' - 运费
                .set(OrderBaseEntity::getOrderRefundAmount, orderBase.getOrderPaymentAmount())
                // 插入买家id，为资金回退做准备
                .set(OrderBaseEntity::getSellerUserId, sellerInfo.get("userId")));


        Integer two = orderGoodsMapper.update(null, new LambdaUpdateWrapper<OrderGoodsEntity>()
                .eq(OrderGoodsEntity::getOrderId, paraMap.get("orderId"))
                .set(OrderGoodsEntity::getOrderGoodsStatus, CommonType.orderStatus.CONFIRM_RECEIPT.getCode())
                .set(OrderGoodsEntity::getGoodsRefundStatus, CommonType.orderRefundStatus.REFUNDING.getCode()));
        Integer orderGoodsNum = 0;
        //查询订单商品数量
        List<OrderGoodsEntity> goodsNumList = orderGoodsMapper.selectList(new LambdaQueryWrapper<OrderGoodsEntity>()
                .eq(OrderGoodsEntity::getOrderId, paraMap.get("orderId")));

        if (null != goodsNumList) {
            for (OrderGoodsEntity orderGoods : goodsNumList) {
                Integer goodsId = orderGoods.getGoodsId();
                Integer goodsNum = Integer.parseInt(orderGoods.getOrderGoodsNum());
                orderGoodsNum += goodsNum;
                //修改订单退款数量
                orderGoodsMapper.update(null, new LambdaUpdateWrapper<OrderGoodsEntity>()
                        .eq(OrderGoodsEntity::getGoodsId, goodsId)
                        .eq(OrderGoodsEntity::getOrderId, paraMap.get("orderId"))
                        .set(OrderGoodsEntity::getDealerReturnCommission, orderGoods.getDealerCommission())
                        .set(OrderGoodsEntity::getOrderGoodsReturnnum, goodsNum));
            }
        }

        // 查询是否存在该条数据
        Map<String, Object> orderReturnMap = new HashMap<>();
        Integer isExistData = Math.toIntExact(baseMapper.selectCount(new LambdaQueryWrapper<OrderReturnEntity>()
                .eq(OrderReturnEntity::getOrderId, paraMap.get("orderId"))));
        baseMapper.updateOdbOrderReturn(paraMap.get("orderId"));

        orderReturnMap.put("orderId", paraMap.get("orderId"));
        orderReturnMap.put("returnState", 1);
        orderReturnMap.put("returnShopHandle", 1);
        orderReturnMap.put("returnType", 1);
        orderReturnMap.put("returnMethodStatus", 1);
        orderReturnMap.put("returnReasonId", paraMap.get("returnReasonId"));
        orderReturnMap.put("returnReason", paraMap.get("returnReason"));
        orderReturnMap.put("returnMessage", paraMap.get("returnMessage"));
        orderReturnMap.put("returnCash", orderBase.getOrderPaymentAmount());
        // 订单总额
        orderReturnMap.put("orderAmount", orderBase.getOrderGoodsAmount());
        orderReturnMap.put("orderGoodsId", 0);
        orderReturnMap.put("orderGoodsName", 0);
        orderReturnMap.put("orderGoodsPrice", 0);
        orderReturnMap.put("orderGoodsNum", orderGoodsNum);
        orderReturnMap.put("orderGoodsPic", 0);
        orderReturnMap.put("buyerUserId", userInfo.getUserId());
        orderReturnMap.put("shopId", orderBase.getShopId());
        orderReturnMap.put("shopName", orderBase.getShopName());
        orderReturnMap.put("buyerUserAccount", userInfo.getUserName());
        orderReturnMap.put("returnAddrId", 0);
        orderReturnMap.put("returnPostCode", 0);
        // 4-卖家收到货物 5-平台审核通过',
        BigDecimal orderPaymentAmount = orderBase.getOrderPaymentAmount();
        // 1.1 退回资金 = 应付金额（商品实际支付金额 + 运费）' - 运费
        orderReturnMap.put("returnCash", orderPaymentAmount);
        orderReturnMap.put("returnRptCash", orderBase.getOrderRptPrice());
        orderReturnMap.put("returnShopHandle", 1);
        orderReturnMap.put("returnMethodStatus", 1);
        orderReturnMap.put("returnMobile", orderBase.getOrderReceiverContact());

        // 插入买家id，为资金回退做准备
        orderReturnMap.put("sellerUserId", sellerInfo.get("userId"));
        SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //创建时间
        Calendar calendar = Calendar.getInstance();
        String nowDate = DateUtils.getTime();
        Date date = null;
        try {
            date = formatter1.parse(nowDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 1);
        //处理时效时间
        Date calendarTime = calendar.getTime();
        orderReturnMap.put("returnAddTime", nowDate);

        if (isExistData == 1) {
            OrderReturnStatusDTO returnStatus = baseMapper.queryOrderReturnData(paraMap.get("orderId"));
            // 存在更新数据
            //在卖家没介入之前可以一直修改但不改失效时间
            orderReturnMap.put("historicalReturnStatus", "");
            if (null != returnStatus.getReturnState() && returnStatus.getReturnState() != 1 && returnStatus.getReturnState() != 15) {
                Integer returnChangeNum = returnStatus.getReturnChangeNum() + 1;
                orderReturnMap.put("returnChangeNum", returnChangeNum);
                orderReturnMap.put("expirationTime", formatter1.format(calendarTime));
            } else if (returnStatus.getReturnState() == 15) {
                orderReturnMap.put("expirationTime", formatter1.format(calendarTime));
            }

            // 更新申请退款记录
            baseMapper.updateOrderReturn(orderReturnMap);

        } else {
            //处理时效时间
            orderReturnMap.put("expirationTime", formatter1.format(calendarTime));
            // 不存在创建数据
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String dateString = formatter.format(new Date());
            String returnCode = "TD-" + userInfo.getUserId() + "-" + orderBase.getShopId() + "-" + dateString + "-"
                    + "0001";
            // 退货编号
            orderReturnMap.put("returnCode", returnCode);
            Integer three = baseMapper.insertOrderReturn(orderReturnMap);
            // two有可能为2
            if (1 != one || 0 == two || 1 != three) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return ResponseResult.fail();
            }

            // 发送消息模板
            Map<String, Object> messageMap = new HashMap<>();
            List<String> messageList = new ArrayList<>();
            messageList.add(paraMap.get("orderId"));
            messageMap.put("list", messageList);
            messageMap.put("orderId", paraMap.get("orderId"));
            String returnGoodsName = baseMapper.queryReturnGoodsNameByOrderId(paraMap.get("orderId"));
            messageMap.put("goodsName", returnGoodsName);
            messageMap.put("userId", orderBase.getSellerUserId());
            messageMap.put("sellerId", orderBase.getSellerUserId());
            messageMap.put("sellerName", orderBase.getSellerUserName());
            messageMap.put("messageType", 1);
            messageMap.put("shopId", orderBase.getShopId());
            messageMap.put("messageTitle", "退款通知");
            messageMap.put("templateId", CommonType.messageTemplate.CODE_H.getCode());
            baseServiceClient.sendMessageTemplate(messageMap);


            Map<String, Map<String, String>> returnMap = setOrderReturnInfo(paraMap, userInfo, orderBase, orderPaymentAmount, sellerInfo);
            ResponseResult tort = payMentServiceClient.insertRecord(returnMap);
            if (CommonStatusEnum.SUCCESS.getCode() != tort.getCode()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return ResponseResult.fail();
            }
        }
        List<OrderBaseEntity> originalOrderList = orderBaseMapper.selectList(new LambdaQueryWrapper<OrderBaseEntity>()
                .eq(OrderBaseEntity::getOriginalOrder, paraMap.get("orderId")));

        if (!originalOrderList.isEmpty()) {
            for (OrderBaseEntity entity : originalOrderList) {
                paraMap.put("orderId", entity.getOrderId());
                ResponseResult result = applyRefund(paraMap, userInfo);
                if (CommonStatusEnum.SUCCESS.getCode() != result.getCode()) {
                    throw new DefinedException(result);
                }
            }
        }

        //判断是否是怡亚通订单
        if (orderBase.getOrderSource() == CommonType.SupplierSource.YI_YA_TONG.getCode()) {
            //查询怡亚通订单信息
            OrderYiyatongEntity orderYiyatongEntity = orderYiyatongMapper.selectOne(new LambdaQueryWrapper<OrderYiyatongEntity>()
                    .eq(OrderYiyatongEntity::getOrderId, paraMap.get("orderId")));
            ApplyRefundDTO applyRefundDTO = new ApplyRefundDTO();
            applyRefundDTO.setOrderSn(orderYiyatongEntity.getOrderSn());
            applyRefundDTO.setReason(paraMap.get("returnReason"));
            applyRefundDTO.setRefundReasonId(paraMap.get("returnReasonId"));
            applyRefundDTO.setReturnType(1);
            //查询订单详情,获取规格id和商品数
            OrderDetailDTO dto = new OrderDetailDTO();
            dto.setOrderSn(orderYiyatongEntity.getOrderSn());
            ResponseResult orderResult = goodServiceClient.getOrderDetail(dto);
            ResellerOrderDetailVO vo = JSONObject.parseObject(JSONObject.toJSONString(orderResult.getData()), ResellerOrderDetailVO.class);
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
            orderYiyatongEntity.setReturnSn(orderReturnGoodsVO.getReturnSn());
            //保存售后单号
            orderYiyatongMapper.updateById(orderYiyatongEntity);
        } else if (orderBase.getOrderSource() == CommonType.SupplierSource.JING_DONG.getCode()) {
            // 判断是是京东订单
            // 因为是代发货 申请退款 直接调用京东申请取消订单
            ResponseResult responseResult = goodServiceClient.cancelJingDongOrder(paraMap.get("returnReason"), orderBase.getJdOrderId());
            if(responseResult.getCode() == CommonStatusEnum.SUCCESS.getCode() && responseResult.getData().equals(true)){
                return ResponseResult.success();
            } else{
              return ResponseResult.fail("京东订单取消异常");
            }

        }
        return ResponseResult.success();

    }

    /**
     * @return boolean
     * @Author Amos
     * @Description 查看虚拟订单是否可以退款，可以退款返回true,不能退款返回flase
     * @Date 14:34 2022/2/24
     * @Param [orderBase, paraMap]
     **/
    private boolean checkVirtualGoods(Map<String, String> paraMap, Integer orderStatus) {
        Map<Object, Object> map = orderGoodsMapper.getVitualInfo(paraMap.get("orderId"));
        //是否过了有效期 是:true,否:false
        boolean dueFlag = false;
        if (null != map.get("period_of_validity")) {
            Date date = DateUtils.parseCNDate(map.get("period_of_validity").toString().replace("T", " "));
            Date nowDate = new Date();
            Calendar calDue = Calendar.getInstance();
            Calendar calNow = Calendar.getInstance();
            calDue.setTime(date);
            calNow.setTime(nowDate);
            if (calNow.after(calDue)) {
                dueFlag = true;
            }
        }
        //过了有效期
        if (dueFlag) {
            return null != map.get("past_due") && 0 == (Integer) map.get("past_due");
            //没过有效期
        } else {
            return orderStatus >= 2 && orderStatus < 6;
        }
    }


    /**
     * 设置fegin参数
     *
     * @param paraMap
     * @param userInfo
     * @param orderBase
     * @param orderPaymentAmount
     * @param sellerInfo
     * @return
     */
    private Map<String, Map<String, String>> setOrderReturnInfo(Map<String, String> paraMap, UserInfoVO userInfo,
                                                                OrderBaseVO orderBase, BigDecimal orderPaymentAmount,
                                                                Map<String, String> sellerInfo) {
        // 使用feign在其他库插入数据
        // 创建一个HashMap用于存储用户记录的相关信息
        Map<String, String> userRecordMap = new HashMap<>();
        // 订单ID
        userRecordMap.put("orderId", paraMap.get("orderId"));
        // 用户ID
        userRecordMap.put("userId", userInfo.getUserId());
        // 用户昵称
        userRecordMap.put("userNickname", userInfo.getUserName());
        // 订单退款金额
        userRecordMap.put("recordMoney", String.valueOf(orderPaymentAmount));
        // 记录时间
        userRecordMap.put("recordDate", DateUtils.getTime());
        // 记录的年份
        userRecordMap.put("recordYear", DateUtils.getYear());
        // 记录的月份
        userRecordMap.put("recordMonth", DateUtils.getMonth());
        // 记录的日期
        userRecordMap.put("recordDay", DateUtils.getDay());
        // 记录标题，此处为"退款"
        userRecordMap.put("recordTitle", "退款");
        // 记录描述，包含订单号
        userRecordMap.put("recordDesc", "订单号：" + paraMap.get("orderId"));
        // 记录操作时间
        userRecordMap.put("recordTime", DateUtils.getTime());
        // 交易类型ID，此处假设为"5"
        userRecordMap.put("tradeTypeId", "5");
        // 用户类型，此处假设为"1"
        userRecordMap.put("userType", "1");
        // 记录状态，此处假设为"8"，代表退款状态
        userRecordMap.put("recordStatus", "8");
        // 订单支付单号
        userRecordMap.put("recordPayorder", orderBase.getPaymentNumber());
        // 付款时间
        userRecordMap.put("recordPaytime", DateUtils.getTime());
        // 创建一个映射，用于存储卖家的记录信息
        Map<String, String> sellerRecordMap = new HashMap<>();
        // 记录订单ID
        sellerRecordMap.put("orderId", paraMap.get("orderId"));
        // 记录卖家的用户ID
        sellerRecordMap.put("userId", sellerInfo.get("userId"));
        // 记录卖家的昵称
        sellerRecordMap.put("userNickname", sellerInfo.get("sellerName"));
        // 记录退款金额，以负数表示
        sellerRecordMap.put("recordMoney", "-" + orderPaymentAmount);
        // 记录操作时间
        sellerRecordMap.put("recordDate", DateUtils.getTime());
        // 记录操作的年份
        sellerRecordMap.put("recordYear", DateUtils.getYear());
        // 记录操作的月份
        sellerRecordMap.put("recordMonth", DateUtils.getMonth());
        // 记录操作的日期
        sellerRecordMap.put("recordDay", DateUtils.getDay());
        // 设置记录标题为“退款”
        sellerRecordMap.put("recordTitle", "退款");
        // 设置记录描述，包含订单号
        sellerRecordMap.put("recordDesc", "订单号：" + paraMap.get("orderId"));
        // 记录操作时间
        sellerRecordMap.put("recordTime", DateUtils.getTime());
        // 设置交易类型ID为5，代表退款
        sellerRecordMap.put("tradeTypeId", "5");
        // 设置用户类型为2，代表卖家
        sellerRecordMap.put("userType", "2");
        // 设置记录状态为8，代表退款完成
        sellerRecordMap.put("recordStatus", "8");
        // 记录订单的支付编号
        sellerRecordMap.put("recordPayorder", orderBase.getPaymentNumber());
        // 记录退款时间
        sellerRecordMap.put("recordPaytime", DateUtils.getTime());

        // 创建一个映射，用于存储买家和卖家的记录信息
        Map<String, Map<String, String>> returnMap = new HashMap<>();
        // 将买家记录映射放入returnMap
        returnMap.put("user", userRecordMap);
        // 将卖家记录映射放入returnMap
        returnMap.put("seller", sellerRecordMap);
        // 返回包含买家和卖家记录信息的映射
        return returnMap;

    }


    /**
     * 根据订单ID查询退单详情，并补充退单地址和退单凭证信息。
     *
     * @param map 包含token和orderId的Map对象。
     * @return 返回退单详情的ResponseResult对象，如果查询失败或订单无效，则返回失败结果。
     */
    @Override
    public ResponseResult selectordereturn(Map<String, Object> map) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        // 如果用户信息为空，返回非法token的错误信息
        if (null == userInfo) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
        }
        // 获取订单ID
        String orderId = String.valueOf(map.get("orderId"));
        // 查询退单详情
        //查询退款详情
        ReturnOrderDetailVO returnOrderDetail = baseMapper.queryOrderReturnDetail(orderId);
        // 如果退单详情存在
        if (returnOrderDetail != null) {
            // 线下支付售后显示平台退款凭证和用户支付凭证
            if (returnOrderDetail.getReturnPaymentVoucher() != null) {
                String re = returnOrderDetail.getReturnPaymentVoucher();
                List<String> returnPaymentVoucherList = JSON.parseArray(re, String.class);
                returnOrderDetail.setReturnPaymentVoucherList(returnPaymentVoucherList);
                OrderPaymentVocherEntity paymentVoucherEntity = orderPaymentVocherMapper.selectOne(new LambdaQueryWrapper<OrderPaymentVocherEntity>()
                        .eq(OrderPaymentVocherEntity::getOrderId, orderId));
                String paymentVoucher = paymentVoucherEntity.getOrderPaymentVoucherImgs();
                List<String> orderPaymentVoucher = JSON.parseArray(paymentVoucher, String.class);
                returnOrderDetail.setOrderPaymentVoucherList(orderPaymentVoucher);
            }
            // 如果退单地址为空，查询并补充退单地址
            if (null == returnOrderDetail.getReturnAddr()) {
                //赋值该订单发货地址
                String address = baseMapper.queryOrderAddress(returnOrderDetail.getOrderId());
                returnOrderDetail.setReturnAddr(address);
            }
            // 检查商家拒绝退款次数是否超过3次
            //获取商家拒绝次数
            if (RedisUtil.exists(HouConfig.NUMBER_OF_TIMES_MERCHANTS_REFUSE_REFUNDS + "-" + orderId)) {
                Integer number = Integer.valueOf(RedisUtil.getNoExpire(HouConfig.NUMBER_OF_TIMES_MERCHANTS_REFUSE_REFUNDS + "-" + orderId));
                // 根据拒绝次数设置标志
                if (number >= 3) {
                    //拒绝是否超过3次
                    returnOrderDetail.setNumberOfTimesMerchantsRefuseRefunds(CommonType.commonStatus.YES.getCode());
                } else {
                    returnOrderDetail.setNumberOfTimesMerchantsRefuseRefunds(CommonType.commonStatus.NO.getCode());
                }
            } else {
                returnOrderDetail.setNumberOfTimesMerchantsRefuseRefunds(CommonType.commonStatus.NO.getCode());
            }
            // 如果存在退单凭证，解析并补充退单凭证列表，同时清空退单凭证字段
            if (returnOrderDetail.getReturnVoucher() != null) {
                String returnVoucher = returnOrderDetail.getReturnVoucher();
                List<String> images = JSONArray.parseArray(returnVoucher, String.class);
                returnOrderDetail.setReturnVouchers(images);
                returnOrderDetail.setReturnVoucher(null);
            }
        } else {
            // 如果退单详情不存在，尝试查询订单基本信息
            OrderBaseEntity entity = orderBaseMapper.selectById(orderId);
            // 如果订单存在且为虚拟商品且已过期，则返回订单过期不可退款的信息
            if (null != entity) {
                Date now = new Date();
                if (entity.getVirtualGoods().equals(CommonType.commonStatus.NO.getCode())) {
                    int result = now.compareTo(entity.getPeriodOfValidity());
                    if(result > 0){
                        return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INVENTED_ORDER_IS_OVERDUE.getMsg() + "不可退款");
                    }
                }
            }
            // 如果退单详情和订单都不存在，返回订单售后信息为空的错误信息
            return ResponseResult.fail(BizCodeEnume.ORDER_AFTER_SALES_NULL);
        }
        // 查询订单商品详情并补充到退单详情中
        //查询售后商品
        List<GoodsDetailsListVO> orderGoods = orderGoodsMapper.queryGoodsList1(orderId);
        if (returnOrderDetail.getReturnMethodStatus().equals(CommonType.returnMethodType.BARTER.getCode())) {
            for (GoodsDetailsListVO goods : orderGoods) {
                if(goods.getBarterGoodsId() != null) {
                    ResponseResult<OrderGoodsBarterVO> barterGoods = goodServiceClient.selectBarterGoods(goods.getBarterGoodsId());
                    goods.setBarterGoods(barterGoods.getData());
                }
            }
        }
        returnOrderDetail.setOrderGoodsDetail(orderGoods);
        List<BarterShippingInfoVO> barterShippingInfoList = orderBarterMapper.selectBarterShippingInfoList(returnOrderDetail.getReturnCode());
        if(!barterShippingInfoList.isEmpty()){
            barterShippingInfoList.forEach(barterShippingInfoVO -> {
                if(StringUtils.isNotBlank(barterShippingInfoVO.getBarterPostName())){
                    barterShippingInfoVO.setExpressName(barterShippingInfoVO.getBarterPostName());
                }else{
                    //查询快递公司名字
                    ResponseResult express = baseServiceClient.getBaseExpressInfoById(barterShippingInfoVO.getBarterPostCode());
                    Map shipperCode = JSON.parseObject(JSON.toJSONString(express.getData()), Map.class);
                    barterShippingInfoVO.setExpressName(shipperCode.get("expressName")+"");
                }
            });
            returnOrderDetail.setBarterShippingInfoList(barterShippingInfoList);
        }
        //查询平台介入
        OrderReturnPlatformVO orderReturnPlatform = orderReturnPlatformMapper.selectOrderReturnPlatform(orderId);
        if (orderReturnPlatform != null) {
            // 设置订单ID
            orderReturnPlatform.setOrderId(returnOrderDetail.getOrderId());
            // 更新退单详情中的平台介入状态
            returnOrderDetail.setInterventionState(orderReturnPlatform.getInterventionState());
            // 处理买家和卖家的凭证，将字符串形式的图片地址转换为列表
            String buyer_voucher = orderReturnPlatform.getBuyerVoucher();
            String seller_voucher = orderReturnPlatform.getSellerVoucher();
            if (seller_voucher != null) {
                List<String> image = JSONArray.parseArray(seller_voucher, String.class);
                orderReturnPlatform.setSellerVouchers(image);
                orderReturnPlatform.setSellerVoucher(null);
            }
            if (buyer_voucher != null) {
                List<String> image = JSONArray.parseArray(buyer_voucher, String.class);
                orderReturnPlatform.setBuyerVouchers(image);
                orderReturnPlatform.setBuyerVoucher(null);
            }
            // 设置退单详情
            returnOrderDetail.setOrderReturnPlatform(orderReturnPlatform);
        }
        // 返回成功的退单详情信息
        return ResponseResult.success(returnOrderDetail);
    }

    /**
     * 撤销介入操作
     *
     * @param map 包含订单相关信息的Map对象，其中必须包含订单ID和返回代码
     * @return 返回操作结果，成功或失败
     */
    @Override
    public ResponseResult revokeIntervention(Map<String, Object> map) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        // 若用户信息为空，则返回非法令牌错误信息
        if (null == userInfo) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
        }
        // 初始化订单商品信息Map
        Map<String, Object> orderGoodsMap = new HashMap<String, Object>();
        // 从map中获取订单ID字符串
        String orderId = String.valueOf(map.get("orderId"));
        // 根据订单ID查询退货详情
        ReturnOrderDetailVO returnOrderDetail = baseMapper.queryOrderReturnDetail(orderId);
        // 获取退货方式状态
        Integer methodStatus = returnOrderDetail.getReturnMethodStatus();
        // 根据返回代码查询退货状态
        Integer returnState = baseMapper.getReturnStateByReturnCode((String) map.get("returnCode"));
        // 若当前退货状态不满足条件，则返回订单状态变更错误信息
        if (methodStatus != 4 && returnState != null && !returnState.equals(CommonType.returnState.NEW_LAUNCH_PENDING_SELLER_REVIEW.getCode())
                && !returnState.equals(CommonType.returnState.SELLER_FAILED_THE_REVIEW.getCode())
                && !returnState.equals(CommonType.returnState.ACCEPTANCE_NOT_PASSED.getCode())
                && !returnState.equals(CommonType.returnState.USER_CANCELS_REFUND_CLOSED.getCode())
                && !returnState.equals(CommonType.returnState.USER_CANCELS_RETURN_CLOSED.getCode())) {
            return ResponseResult.fail(BizCodeEnume.ORDER_STATUS_CHANGE);
        }
        // 初始化订单退货平台信息对象
        OrderReturnPlatformVO orderReturnPlatform = new OrderReturnPlatformVO();
        // 获取当前时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        Date Application_time = calendar.getTime();
        // 设置申请时间
        orderReturnPlatform.setOrderId(orderId);
        // 设置买家撤销相关参数
        orderGoodsMap.put("returnCode", map.get("returnCode"));
        orderGoodsMap.put("historicalReturnStatus", returnOrderDetail.getReturnState());
        orderGoodsMap.put("returnShopHandle", 0);
        // 根据退货方式状态设置退货状态
        //退款方式:1.退款 2.仅退款 3退货退款
        if (methodStatus == 1 || methodStatus == 2) {
            //用户撤销退款关闭
            orderGoodsMap.put("returnState", 15);
        } else if (methodStatus == 3) {
            orderGoodsMap.put("returnState", 16);
        } else if (methodStatus == 4) {
            orderGoodsMap.put("returnState", 20);
            ReturnOrderDTO dto = new ReturnOrderDTO();
            dto.setOrderId(orderId);
            List<WapGoodsSaleSumDTO> returnGoods = orderGoodsMapper.queryReturnGoodsToSale(dto);
            goodServiceClient.updateGoodsStock(returnGoods);
        }
        // 设置撤销数量和关闭时间
        orderGoodsMap.put("revokeNum", 1);
        orderGoodsMap.put("closeTime", new Date());
        // 初始化更新订单和订单商品的Map
        Map<String, Object> updateMap = new HashMap<>();
        Map<String, Object> updateGoodsMap = new HashMap<>();
        // 设置订单相关更新参数
        Integer torfOne = null;
        Integer torfTwo = null;
        updateMap.put("orderId", orderId);
        updateMap.put("orderRefundStatus", "0");
        updateMap.put("orderReturnStatus", "0");
        updateMap.put("orderBarterStatus", "0");
        // 设置订单商品相关更新参数
        updateGoodsMap.put("orderId", orderId);
        updateGoodsMap.put("goodsReturnStatus", 0);
        updateGoodsMap.put("goodsRefundStatus", 0);
        updateGoodsMap.put("goodsBarterStatus", 0);
        // 添加商家拒绝退款时间
        String businessTime = String.valueOf(System.currentTimeMillis());
        String businessRefundTime = DateUtils.stampToDate(businessTime);
        // 更新订单基础信息
        torfOne = orderBaseMapper.updateOrderBaseById(updateMap);
        orderGoodsMapper.updateOrderGoodsById2(updateGoodsMap);
        // 更新订单记录状态
        Map<String, String> feignMap = new HashMap<>();
        feignMap.put("orderId", orderId);
        feignMap.put("recordStatus", "7");
        payMentServiceClient.updateRefuse(feignMap);
        // 是京东的订单要调用接口 对京东的售后单进行撤销
        if(returnOrderDetail.getOrderSource() == CommonType.SupplierSource.JING_DONG.getCode()){
            // 根据商城订单订单号查询 京东订单号 和 售后单号
            List<JingDongReturnOrderDTO> jingDongReturnOrderDTOS = orderGoodsMapper.queryJdReturnOrderInfo(orderId);
            for (JingDongReturnOrderDTO jingDongReturnOrderDTO : jingDongReturnOrderDTOS) {
                goodServiceClient.cancelJdAfterSaleOrder(jingDongReturnOrderDTO.getJdOrderId(),jingDongReturnOrderDTO.getJdReturenOrderId());
            }
        }
        // 若订单基础信息更新不成功，则回滚事务并返回失败信息
        if (1 != torfOne) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseResult.fail();
        }
        // 更新订单退货信息
        Integer torf = baseMapper.updateOrderReturnById(orderGoodsMap);
        // 若更新成功，则返回成功信息，否则返回失败信息
        if (torf > 0) {
            return ResponseResult.success();
        }
        return ResponseResult.fail();
    }

    /**
     * 根据订单ID查询退单平台介入信息。
     *
     * @param map 包含token和orderId的Map对象。
     * @return 返回退单平台介入详情的ResponseResult对象。
     */
    @Override
    public ResponseResult selectordereturnplatform(Map<String, Object> map) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        // 若用户信息为空，则返回token非法的错误信息
        if (null == userInfo) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());
        }
        // 获取订单ID
        String orderId = String.valueOf(map.get("orderId"));
        // 查询退单详情
        // 查询退款详情
        ReturnOrderDetailVO returnOrderDetail = baseMapper.queryOrderReturnDetail(orderId);
        // 若退单详情不为空，处理退单凭证信息
        if (returnOrderDetail != null) {
            // 如果存在退单凭证，将其解析为图片列表
            if (returnOrderDetail.getReturnVoucher() != null) {
                String returnVoucher = returnOrderDetail.getReturnVoucher();
                List<String> imgs = JSONArray.parseArray(returnVoucher, String.class);
                returnOrderDetail.setReturnVouchers(imgs);
                returnOrderDetail.setReturnVoucher(null);
            }
        }
        // 如果退单详情中的退货地址或地址名称为空，则查询并补充地址信息
        if (null == returnOrderDetail.getReturnAddr() || null == returnOrderDetail.getReturnAddrName()) {
            returnOrderDetail.setReturnAddr(baseMapper.queryOrderAddress(returnOrderDetail.getOrderId()));
            returnOrderDetail.setReturnAddrName(baseMapper.queryOrderAddrName(returnOrderDetail.getOrderId()));
        }
        // 查询退单中的商品详情
        // 查询售后商品
        List<GoodsDetailsListVO> orderGoods = orderGoodsMapper.queryGoodsList1(orderId);
        returnOrderDetail.setOrderGoodsDetail(orderGoods);
        // 查询平台介入的退单信息
        // 查询平台介入
        OrderReturnPlatformVO orderReturnPlatform = orderReturnPlatformMapper.selectOrderReturnPlatform(orderId);
        // 如果平台介入信息为空，则初始化该对象
        if (orderReturnPlatform == null) {
            orderReturnPlatform = new OrderReturnPlatformVO();
        }
        // 设置订单ID，并从退单详情中获取介入状态
        orderReturnPlatform.setOrderId(returnOrderDetail.getOrderId());
        returnOrderDetail.setInterventionState(orderReturnPlatform.getInterventionState());
        // 处理买家和卖家的凭证信息，将其解析为图片列表
        String buyer_voucher = orderReturnPlatform.getBuyerVoucher();
        String seller_voucher = orderReturnPlatform.getSellerVoucher();
        if (seller_voucher != null) {
            List<String> image = JSONArray.parseArray(seller_voucher, String.class);
            orderReturnPlatform.setSellerVouchers(image);
            orderReturnPlatform.setSellerVoucher(null);
        }
        if (buyer_voucher != null) {
            List<String> image = JSONArray.parseArray(buyer_voucher, String.class);
            orderReturnPlatform.setBuyerVouchers(image);
            orderReturnPlatform.setBuyerVoucher(null);
        }
        // 将退单详情和平台介入信息绑定在一起
        orderReturnPlatform.setReturnOrderDetail(returnOrderDetail);
        // 返回处理后的平台介入退单信息
        return ResponseResult.success(orderReturnPlatform);
    }

    /**
     * 根据售后订单状态查询售后方式
     *
     * @param map 包含token和orderId的Map对象
     * @return 返回售后方式的结果对象
     */
    @Override
    public ResponseResult checkAfterSales(Map<String, Object> map) {
        // 初始化返回结果对象
        ResponseResult result = new ResponseResult();
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        // 如果用户信息为空，返回token非法的错误结果
        if (null == userInfo) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());
        }
        // 根据订单ID查询售后订单详情
        ReturnOrderDetailVO returnOrderDetail = baseMapper.queryOrderReturnDetail(String.valueOf(map.get("orderId")));
        // 如果售后订单详情不为空
        if (returnOrderDetail != null) {
            // 根据售后订单的状态，设置售后方式的结果
            Integer methodStatus = returnOrderDetail.getReturnMethodStatus();
            if (methodStatus == 2 && !CommonType.returnState.USER_CANCELS_REFUND_CLOSED.getCode().equals(Integer.parseInt(returnOrderDetail.getReturnState()))) {
                result.setData(CommonType.afterSalesMethods.ONLY_REFUND.getCode());
                result.setMessage(CommonType.afterSalesMethods.ONLY_REFUND.getDesc());
            } else if (methodStatus == 3 && !CommonType.returnState.USER_CANCELS_RETURN_CLOSED.getCode().equals(Integer.parseInt(returnOrderDetail.getReturnState())))  {
                result.setData(CommonType.afterSalesMethods.REFUND.getCode());
                result.setMessage(CommonType.afterSalesMethods.REFUND.getDesc());
            } else if (methodStatus == 1 && !CommonType.returnState.USER_CANCELS_REFUND_CLOSED.getCode().equals(Integer.parseInt(returnOrderDetail.getReturnState())))  {
                result.setData(CommonType.afterSalesMethods.RETURN.getCode());
                result.setMessage(CommonType.afterSalesMethods.RETURN.getDesc());
            }else{
                result.setData(CommonType.afterSalesMethods.NO_AFTER_SALES.getCode());
                result.setMessage(CommonType.afterSalesMethods.NO_AFTER_SALES.getDesc());
            }
            // 如果售后订单详情为空，表示没有售后记录，设置相应的结果
        } else {
            result.setData(CommonType.afterSalesMethods.NO_AFTER_SALES.getCode());
            result.setMessage(CommonType.afterSalesMethods.NO_AFTER_SALES.getDesc());
        }
        // 设置操作成功的状态码
        result.setCode(CommonStatusEnum.SUCCESS.getCode());
        // 返回售后方式的结果对象
        return result;
    }

    /**
     * @Description: 买家取消平台介入
     * @Param: [map]
     * @Return: com.yf.ShoppingCart.web.entity.ResultResp
     */
    @Override
    public ResponseResult revokePlatformIntervention(Map<String, Object> map) {
        ResponseResult resultResp = new ResponseResult();
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        if (null == userInfo) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());
        }
        Map<String, Object> orderGoodsMap = new HashMap<>();
        String orderId = String.valueOf(map.get("orderId"));
        //查询平台介入
        OrderReturnPlatformVO orderReturnPlatform1 = orderReturnPlatformMapper.selectOrderReturnPlatform(orderId);

        ReturnOrderDetailVO returnOrderDetail = baseMapper.queryOrderReturnDetail(orderId);
        Integer methodStatus = returnOrderDetail.getReturnMethodStatus();

        OrderReturnPlatformVO orderReturnPlatform = new OrderReturnPlatformVO();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        Date Application_time = calendar.getTime();
        calendar.add(Calendar.DATE, 1);
        Date calendarTime = calendar.getTime();
        orderReturnPlatform.setOrderId(orderId);

        // 买家撤销
        orderGoodsMap.put("returnCode", map.get("returnCode"));
        orderGoodsMap.put("historicalReturnStatus", orderReturnPlatform1.getReturnState());

        orderGoodsMap.put("returnShopHandle", 0);
        if (methodStatus == 1 || methodStatus == 2) {
            orderGoodsMap.put("returnState", 15);
        } else if (methodStatus == 3) {
            orderGoodsMap.put("returnState", 16);
        }

        orderGoodsMap.put("closeTime", new Date());


        Map<String, Object> updateMap = new HashMap<>();
        Map<String, Object> updateGoodsMap = new HashMap<>();
        Integer torfOne = null;
        Integer torfTwo = null;
        updateMap.put("orderId", orderId);
        // 原值：2，现改为0，用户可持续申请
        updateMap.put("orderRefundStatus", "0");
        // 原值：2，现改为0，用户可持续申请
        updateMap.put("orderReturnStatus", "0");

        updateGoodsMap.put("orderId", orderId);

        updateGoodsMap.put("goodsReturnStatus", 0);
        updateGoodsMap.put("goodsRefundStatus", 0);
        //添加商家拒绝退款时间
        String businessTime = String.valueOf(System.currentTimeMillis());
        String businessRefundTime = DateUtils.stampToDate(businessTime);

        //更新平台介入状态
        orderReturnPlatformMapper.update(null, new LambdaUpdateWrapper<OrderReturnPlatformEntity>()
                .set(OrderReturnPlatformEntity::getInterventionState, CommonType.interventionState.BUYER_WITHDRAWAL.getCode())
                .set(OrderReturnPlatformEntity::getProcessingResults, CommonType.processingResults.USER_REVOCATION.getCode())
                .set(OrderReturnPlatformEntity::getProcessingTime, new Date())
                .eq(OrderReturnPlatformEntity::getOrderId, orderId));

        torfOne = orderBaseMapper.updateOrderBaseById(updateMap);
        //torfTwo = returnMoneyDao.updateOrderGoodsById(updateGoodsMap);

        //更新record表状态
        Map<String, String> feignMap = new HashMap<>();
        feignMap.put("orderId", orderId);
        // 原值：7
        feignMap.put("recordStatus", "7");
        payMentServiceClient.updateRefuse(feignMap);

        //if (1 != torfOne ||  torfTwo<1) {
        if (1 != torfOne) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            resultResp.setCode(2);
            return resultResp;
        }
        Integer torf = baseMapper.updateOrderReturnById(orderGoodsMap);
        if (torf > 0) {
            resultResp.setCode(1);
        }
        return resultResp;
    }

    @Override
    public ResponseResult calculatePrice(ApplyReturnOrderDTO applyReturnOrders) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(applyReturnOrders.getToken());
        // 如果用户信息为空，则返回token非法的错误信息
        if (null == userInfo) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());
        }
        // 检查订单id是否为空，如果为空，则返回错误信息
        if (StringUtils.isEmpty(applyReturnOrders.getOrderId())) {
            return ResponseResult.fail(2, "订单id不能为空");
        }
        // 根据订单id查询订单基本信息
        List<OrderBaseVO> orderBases = orderBaseMapper.getOrderBaseById(applyReturnOrders.getOrderId());
        // 获取查询到的订单列表中的第一个订单
        OrderBaseVO orderBase = orderBases.get(0);
        // 计算订单的折扣率
        BigDecimal discount = orderBase.getOrderPaymentAmount().divide(orderBase.getOrderGoodsAmount(), 2, RoundingMode.HALF_UP);

        // 检查虚拟商品是否为0以及是否超过最后退货时间，如果超过，则返回超出售后期限的错误信息
        // 如果订单售后时间小于当前时间订单不可退
        if (orderBase.getVirtualGoods() != 0 && null != orderBase.getOrderLastReturnTime()
                && DateUtils.compareDate(orderBase.getOrderLastReturnTime(), DateUtils.getNowDate())) {
            return ResponseResult.fail(BizCodeEnume.GOODS_BEYOND_THE_AFTER_SALES_PERIOD);
        }
        // 获取订单id
        String orderId = applyReturnOrders.getOrderId();

        // 获取订单所属店铺id
        Integer shopId = orderBase.getShopId();
        // 初始化订单商品数量
        Integer orderGoodsNum = 0;
        // 获取申请退货的商品列表
        // 根据商品己算退货价格
        List<ApplyReturnGoodDTO> applyReturnGoods = applyReturnOrders.getApplyReturnGoods();


        //判断当前订单是否全部退货
        Boolean huoBaoreturn = true;
        Double returnCash = 0.00;
        // 退货时商品总价格
        Double goodsAmount = 0.00;
        //售后红包金额
        Double returnRptCash = 0.00;
        //退还运费
        Double returnShippingFee = 0.00;
        //运费
        Double shippingFee = 0.00;
        //查询运费
        List<Double> fees = orderGoodsMapper.selectList(new LambdaQueryWrapper<OrderGoodsEntity>()
                        .eq(OrderGoodsEntity::getOrderId, orderId))
                .stream().map(OrderGoodsEntity::getShippingFee).collect(Collectors.toList());
        // 售后商品 + 售后促销 / 总商品 + 总促销（红包+代金券）
        for (ApplyReturnGoodDTO applyReturnGood : applyReturnGoods) {
            // 查询商品购买数量
            String orderGoodsId = applyReturnGood.getOrderGoodsId();

            ApplyReturnGoodDTO orderPurchaseQuantity = orderGoodsMapper.queryOrderPurchaseQuantity(orderId, orderGoodsId);
            if (orderPurchaseQuantity == null) {
                //应用退货为null，则进入下一次循环
                continue;
            }
            // 计算商品的退款金额
            // 将申请退换货的商品数量和单价转换为double类型，并相乘累加到商品的退款金额中
            goodsAmount += Double.parseDouble(applyReturnGood.getOrderGoodsNum()) * Double.parseDouble(applyReturnGood.getOrderGoodsPrice());

            // 初始化一个Map用于存储订单和商品的ID信息
            Map<String, String> getGoodsMap = new HashMap<>();
            // 将退换货订单的ID和商品的ID存入Map中
            getGoodsMap.put("orderId", applyReturnOrders.getOrderId());
            getGoodsMap.put("goodsId", applyReturnGood.getOrderGoodsId());

            // 根据订单和商品的ID信息，查询该商品的折扣信息
            OrderGoodsDiscountDTO orderGoodsDiscount = orderGoodsMapper.getGoodsDiscount(getGoodsMap);

            // 初始化一个临时变量，用于存储退换货商品的实际数量
            Integer orderGoodsNumTemp = 0;

            if (orderGoodsDiscount != null) {
                //商品价格加上售后促销价格
                goodsAmount += orderGoodsDiscount.getOrderRptPrice() + orderGoodsDiscount.getVoucherPrice();
                returnRptCash += orderGoodsDiscount.getOrderRptPrice();
                orderGoodsNumTemp = orderGoodsDiscount.getOrderGoodsNum().intValue();
            }
            Integer orderReturnGoodsNum = Integer.valueOf(applyReturnGood.getOrderGoodsNum());
            if (!orderGoodsNumTemp.equals(orderReturnGoodsNum)) {
                huoBaoreturn = false;
            }
            //计算运费
            if (null != orderPurchaseQuantity.getTotalGoodsShippingFee()) {
                shippingFee += orderPurchaseQuantity.getTotalGoodsShippingFee();
            }
//            shippingFee+=orderPurchaseQuantity.getShippingFee();

            // 判断：如果全部退则退还此商品实付，否则按比例退还
            if (applyReturnGood.getOrderGoodsNum().equals(orderPurchaseQuantity.getOrderGoodsNum())) {
                // 1.1 退回资金 = 应付金额（商品实际支付金额 + 运费- 促销）'(带运费)

                Double totalGoodsShippingFee = 0.0;
                if (null != orderPurchaseQuantity.getTotalGoodsShippingFee()) {
                    totalGoodsShippingFee = orderPurchaseQuantity.getTotalGoodsShippingFee();
                }
                returnCash += orderPurchaseQuantity.getOrderGoodsPaymentAmount() - orderPurchaseQuantity.getShippingFee() + totalGoodsShippingFee;
                // returnCash=new BigDecimal(returnCash).multiply(discount).doubleValue();

            } else {
                if (null != orderBase.getOrderType() && 2 == orderBase.getOrderType()) {
                    // 退货金额 = ((实付金额-运费)/商品总金额)*退货商品价格*退货商品数量
                    returnCash += (orderBase.getOrderGoodsAmount().doubleValue() / Double.parseDouble(orderPurchaseQuantity.getOrderGoodsNum())) * Double.parseDouble(applyReturnGood.getOrderGoodsNum());
                } else {
                    // 退货金额 = ((实付金额-运费)/商品总金额)*退货商品价格*退货商品数量+(运费)
                    Double totalGoodsShippingFee = 0.0;
                    if (null != orderPurchaseQuantity.getTotalGoodsShippingFee()) {
                        totalGoodsShippingFee = orderPurchaseQuantity.getTotalGoodsShippingFee();
                    }
                    returnCash += ((orderPurchaseQuantity.getOrderGoodsPaymentAmount() - orderPurchaseQuantity.getShippingFee()) / Double.parseDouble(orderPurchaseQuantity.getOrderGoodsNum())) * Double.parseDouble(applyReturnGood.getOrderGoodsNum()) + totalGoodsShippingFee;
                }
            }
            //如果是 供应商代发货的订单 查询供应商的商品价格 计算退单金额
            //因为 同步是商品不能加购物车 所以 退货数量 和 退货商品价格是唯一  只会有一个商品
            //if (null != orderBase.getDropShipping() && 1 == orderBase.getDropShipping()) {
            //    //获取供应商的商品id
            //    Integer commonId = orderBaseMapper.queryCommonId(applyReturnOrders.getOrderId(), applyReturnGood.getOrderGoodsId());
            //    for (OrderBaseVO base : orderBases) {
            //        if (base.getSupperCommonId().equals(commonId)) {
            //            BigDecimal supperReturnPrice = new BigDecimal(0);
            //            supperReturnPrice = base.getSupperGoodsPrice().multiply(new BigDecimal(applyReturnGood.getOrderGoodsNum()));
            //            //将退单的供应商 退款金额存入数据库
            //            orderSupplierBaseService.update(null, new LambdaUpdateWrapper<OrderSupplierBaseEntity>()
            //                    .set(OrderSupplierBaseEntity::getOrderReturnPrice, supperReturnPrice)
            //                    .eq(OrderSupplierBaseEntity::getOrderId, applyReturnOrders.getOrderId())
            //                    .eq(OrderSupplierBaseEntity::getCommonId, base.getSupperCommonId()));
            //        }
            //    }
            //
            //}
        }
        //如果huoBaoreturn 为true 证明当前订单 退货的商品数量与订单数量一致 （但也可能是一个订单里面的一个商品全部数量退货，另外的商品不退货，所以要判断是否是所有商品全部退货）
        if (!huoBaoreturn) {
            //判断订单是否是全部商品 全部退货
            Integer num = Math.toIntExact(orderGoodsMapper.selectCount(new LambdaQueryWrapper<OrderGoodsEntity>()
                    .eq(OrderGoodsEntity::getOrderId, orderId)));
            //如果是订单全部退货 就退红包（修改红包状态未使用，在商家退货确认收货接口修改），退红包 就在结算时写入红包金额
            if (num.equals(applyReturnGoods.size())) {
                //如果进来就证明该订单不是是全部退货就不退红包，不将红包金额写入结算  不退还 经销等级折扣
                returnRptCash = 0.00;
                returnShippingFee = 0.00;
            }
        }
        //退货金额
        BigDecimal returnCashBig;
        if (orderBase.getCombinationId() != null && orderBase.getCombinationId() != 0) {
            returnCashBig = orderBase.getOrderPaymentAmount();
        } else {
            returnCashBig = new BigDecimal(returnCash).setScale(2, RoundingMode.HALF_UP);
        }
        Map map = new HashMap<>();
        map.put("returnCash", returnCashBig);
        map.put("shippingFee", shippingFee);
        map.put("orderShippingFee", orderBase.getOrderShippingFee());
        return ResponseResult.success(map);
    }

    /**
     * 查询退款订单列表。
     *
     * @param paraMap 查询参数集合，包含分页信息和过滤条件。
     * @return 返回查询结果，包含分页信息和退款订单详情列表。
     */
    @Override
    public ResponseResult listRefundOrder(Map<String, Object> paraMap) {
        if(null != paraMap.get("returnState")){
            paraMap.put("returnState",paraMap.get("returnState").toString());
        }
        // 根据查询条件和分页信息查询退款订单详情
        IPage<ReturnOrderDetailVO> page = baseMapper.listRefundOrder(new Query<ReturnOrderDetailVO>().getPage(paraMap), paraMap);

        // 获取脱敏开关的配置值
        String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.DESENSITIZATION_SWITCH.getDesc());

        // 如果脱敏开关打开且查询结果不为空，则对查询结果进行脱敏处理
        if (StrUtil.isNotEmpty(value) && value.equals(CommonType.commonStatus.YES.getCode().toString()) && !page.getRecords().isEmpty()) {
            for (ReturnOrderDetailVO vo : page.getRecords()) {
                // 脱敏处理后覆盖原对象属性
                BeanUtils.copyProperties(DesensitizationUtil.Desensitization(vo, ReturnOrderDetailVO.class), vo);
            }
        }

        // 返回处理后的查询结果
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 查询平台退单详情信息。
     * 通过传入的参数映射对象查询退单详情，并补充退单的商品详情信息。
     * 如果退单处理状态为商家拒绝且退单状态为已完成，则将退单状态更新为已拒绝。
     * 返回包含退单详细信息的成功响应结果。
     *
     * @param paraMap 参数映射对象，包含查询退单详情所需的键值对参数。
     * @return 成功的响应结果，其中包含退单的详细信息。
     */
    @Override
    public ResponseResult PlatformReturnDetail(Map<String, Object> paraMap) {
        // 通过参数映射对象查询退单详情信息
        ReturnOrderDetailVO returnDetail = baseMapper.refundDetail(paraMap);

        String orderId = returnDetail.getOrderId();
        // 初始化用于查询退单商品详情的参数映射对象
        Map<String, String> selectMap = new HashMap<String, String>();
        // 设置查询参数为退单订单号
        selectMap.put("orderId", orderId);

        // 根据订单号查询退单商品详情列表
        List<GoodsDetailsVO> listReturnGoods = baseMapper.listReturnGoods(selectMap);
        // 将查询到的退单商品详情列表设置到退单详情对象中
        returnDetail.setGoodsDetails(listReturnGoods);

        // 如果退单由商家拒绝且退单状态为已完成，则将退单状态更新为已拒绝
        if (returnDetail.getReturnShopHandle() == 3 && "5".equals(returnDetail.getReturnState())) {
            returnDetail.setReturnState("3");
        }

        // 线下付款查询支付凭证
        if (returnDetail.getPaymentCode() == 2) {
            String returnPaymentVoucherStr = baseMapper.selectReturnPaymentVoucher(orderId);
            List<String> returnPaymentVoucher = JSON.parseArray(returnPaymentVoucherStr, String.class);
            returnDetail.setReturnPaymentVoucherList(returnPaymentVoucher);
            OrderPaymentVocherEntity paymentVoucherEntity = orderPaymentVocherMapper.selectOne(new LambdaQueryWrapper<OrderPaymentVocherEntity>()
                    .eq(OrderPaymentVocherEntity::getOrderId, orderId));
            String paymentVoucher = paymentVoucherEntity.getOrderPaymentVoucherImgs();
            List<String> orderPaymentVoucher = JSON.parseArray(paymentVoucher, String.class);
            returnDetail.setOrderPaymentVoucherList(orderPaymentVoucher);
        }
        // 返回包含退单详细信息的成功响应结果
        return ResponseResult.success(returnDetail);
    }

    @Override
    public ResponseResult listRefundOrderExcel(Map<String, String> paraMap, HttpServletResponse response) {
        UserInfoVO info = ParseTokenUtils.getPlatformUser(paraMap.get("token"));
        if (null == info) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String finishTime = dateformat.format(new Date());
        List<ReturnOrderDetailVO> result = baseMapper.listrefundorderExp(paraMap);

        String[] headers = {"退单编号", "退单金额", "佣金金额", "申请原因", "申请时间", "商家处理备注", "商家处理时间", "订单编号",
                "买家", "商家", "付款时间", "退款状态"};
        List<Map<String, String>> listExcel = Lists.newArrayList();
        String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.DESENSITIZATION_SWITCH.getDesc());
        for (ReturnOrderDetailVO res : result) {
            if (StrUtil.isNotEmpty(value) && value.equals(CommonType.commonStatus.YES.getCode().toString())) {
                BeanUtils.copyProperties(DesensitizationUtil.Desensitization(res, ReturnOrderDetailVO.class), res);
            }
            HashMap<String, String> map = Maps.newHashMap();
            if (res.getReturnState() != null) {
                switch (res.getReturnState()) {
                    case "0":
                        res.setReturnState("卖家拒绝 ");
                        break;
                    case "1":
                        res.setReturnState("新发起等待卖家审核");
                        break;
                    case "2":
                        res.setReturnState("卖家审核通过  ");
                        break;
                    case "3":
                        res.setReturnState("卖家审核不通过 ");
                        break;
                    case "4":
                        res.setReturnState("卖家收到货物 ");
                        break;
                    case "5":
                        res.setReturnState("平台审核通过");
                        break;
                    case "6":
                        res.setReturnState("买家已填物流 ");
                        break;
                    case "7":
                        res.setReturnState("平台拒绝");
                        break;
                    case "8":
                        res.setReturnState("退款关闭");
                        break;
                    case "9":
                        res.setReturnState("平台介入中");
                        break;
                    case "15":
                        res.setReturnState("用户撤销退款关闭");
                        break;

                }
            }
            map.put("退单编号", res.getReturnCode());
            map.put("退单金额", res.getReturnCash().toString());
            map.put("佣金金额", res.getReturnCommisionFee());
            map.put("申请原因", res.getReturnReason());
            map.put("申请时间", res.getReturnAddTime());
            map.put("商家处理备注", res.getReturnShopMessage());
            map.put("商家处理时间", res.getReturnShopTime());
            map.put("订单编号", res.getOrderId());
            map.put("买家", res.getBuyerUserAccount());
            map.put("商家", res.getShopName());
            map.put("付款时间", res.getPaymentTime());
            map.put("退款状态", res.getReturnState());
            listExcel.add(map);
        }
        // excel文件名
        Map<String, List<Map<String, String>>> map = Maps
                .newHashMap();
        map.put("退款列表", listExcel);
        String url = ExcelsUtil.createExcel(headers, map, new int[]{0});
        return ResponseResult.success(url);
    }

    /**
     * 查询订单提醒配送端口信息。
     *
     * @param param 包含订单提醒配送信息的DTO对象。
     * @return 返回查询结果的ResponseResult对象。
     * @Override
     */
    @Override
    public ResponseResult queryOrderRemindDeliveryPort(OrderRemindDeliveryDTO param) {
        // 通过token获取用户信息。
        UserInfoVO info = ParseTokenUtils.getPlatformUser(param.getToken());
        if (info != null) {
            Map map = null;
            try {
                // 将参数对象转换为Map，用于后续的查询条件构建。
                map = ParseMapUtils.beanToMap(param);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            // 根据Map中的查询条件，查询订单提醒配送信息。
            IPage<OrderRemindDeliveryVO> page = baseMapper.queryOrderRemindDeliveryPort(new Query<OrderRemindDeliveryVO>().getPage(map), param);
            // 获取脱敏开关的配置值。
            String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.DESENSITIZATION_SWITCH.getDesc());
            // 判断是否需要进行脱敏处理，并对查询结果进行脱敏。
            if (StrUtil.isNotEmpty(value) && value.equals(CommonType.commonStatus.YES.getCode().toString()) && !page.getRecords().isEmpty()) {
                for (OrderRemindDeliveryVO vo : page.getRecords()) {
                    BeanUtils.copyProperties(DesensitizationUtil.Desensitization(vo, OrderRemindDeliveryVO.class), vo);
                }
            }
            // 返回查询结果。
            return ResponseResult.success(PageUtils.getPage(page));
        } else {
            // 如果用户信息为空，返回认证失败的响应结果。
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
    }

    /**
     * 提交订单发货提醒接口。
     * 通过订单ID等参数，查询订单商品详情，并根据商品所属商家的信息，推送发货提醒至第三方服务。
     *
     * @param param 请求参数，包含订单ID、买家用户名、时间差等信息。
     * @return 发货提醒的结果，成功或失败。
     */
    @Override
    public ResponseResult OrderRemindDeliveryPort(Map<String, String> param) {
        // 获取订单ID
        String orderId = param.get("orderId");
        // 获取买家用户名
        String orderReceiverName = param.get("buyerUserName");
        // 获取时间差
        String timeDiff = param.get("timeDiff");

        // 通过订单ID查询商品详情
        List<GoodsDetailsVO> parm = baseMapper.queryGoodsDetail(orderId);
        for (GoodsDetailsVO goodsDetailsVO : parm) {
            // 查询商品所属商家的信息
            ShopUserMessageResultVO shopUserMessageResult = merchantServiceClient.queryUserId(goodsDetailsVO.getDeliverShopId());
            // 检查商家是否配置了电话，未配置则不支持发送短信提醒
            if (shopUserMessageResult == null || shopUserMessageResult.getShopTel() == null || shopUserMessageResult.getShopTel().equals("")) {
                return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER.getCode(), "商家未配置手机号，不支持发送短信");
            } else {
                // 构建发送给第三方服务的参数，包含商家电话、买家用户名、时间差和订单ID
                Map<String, String> map = new HashMap<>();
                map.put("orderSellerContact", shopUserMessageResult.getShopTel());
                map.put("orderReceiverName", orderReceiverName);
                map.put("timeDiff", timeDiff);
                map.put("orderId", orderId);
                // 推送发货提醒至第三方服务
                thirdPartyServiceClient.pushTheDelivery(map);
            }
        }
        // 发送成功
        return ResponseResult.success(BizCodeEnume.SENDING_SUCCEEDED.getMsg());
    }


    @Override
    public ResponseResult queryOrderRemindDeliveryPortExcel(OrderRemindDeliveryDTO param) {
        List<OrderRemindDeliveryVO> result = null;
        UserInfoVO info = ParseTokenUtils.getPlatformUser(param.getToken());
        if (info != null) {

            result = baseMapper.OrderRemindDeliveryPortExp(param);
            String[] headers = {"已付款时间(h)", "订单编号", "商品名称", "买家账号", "收件人姓名", "支付时间", "店铺名称", "店铺id", "店铺联系方式"};
            if (null != result && result.size() > 0) {
                String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.DESENSITIZATION_SWITCH.getDesc());
                List<Map<String, String>> list = Lists.newArrayList();
                for (OrderRemindDeliveryVO orderRemindDeliveryResult : result) {
                    if (StrUtil.isNotEmpty(value) && value.equals(CommonType.commonStatus.YES.getCode().toString())) {
                        BeanUtils.copyProperties(DesensitizationUtil.Desensitization(orderRemindDeliveryResult, OrderRemindDeliveryVO.class), orderRemindDeliveryResult);
                    }
                    HashMap<String, String> map = com.google.common.collect.Maps.newHashMap();
                    map.put("已付款时间(h)", String.valueOf(orderRemindDeliveryResult.getTimeDiff()));
                    if (StringUtils.isEmpty(orderRemindDeliveryResult.getOrderId())) {
                        map.put("订单编号", "无");
                    } else {
                        map.put("订单编号", orderRemindDeliveryResult.getOrderId());
                    }
                    if (StringUtils.isEmpty(orderRemindDeliveryResult.getGoodsName())) {
                        map.put("商品名称", "无");
                    } else {
                        map.put("商品名称", orderRemindDeliveryResult.getGoodsName());
                    }
                    if (StringUtils.isEmpty(orderRemindDeliveryResult.getBuyerUserName())) {
                        map.put("买家账号", "无");
                    } else {
                        map.put("买家账号", orderRemindDeliveryResult.getBuyerUserName());
                    }
                    if (StringUtils.isEmpty(orderRemindDeliveryResult.getOrderReceiverName())) {
                        map.put("收件人姓名", "无");
                    } else {
                        map.put("收件人姓名", orderRemindDeliveryResult.getOrderReceiverName());
                    }
                    if (StringUtils.isEmpty(orderRemindDeliveryResult.getPaymentTime())) {
                        map.put("支付时间", "无");
                    } else {
                        map.put("支付时间", orderRemindDeliveryResult.getPaymentTime());
                    }
                    if (StringUtils.isEmpty(orderRemindDeliveryResult.getShopName())) {
                        map.put("店铺名称", "无");
                    } else {
                        map.put("店铺名称", orderRemindDeliveryResult.getShopName());
                    }
                    if (StringUtils.isEmpty(orderRemindDeliveryResult.getShopId())) {
                        map.put("店铺id", "无");
                    } else {
                        map.put("店铺id", orderRemindDeliveryResult.getShopId());
                    }
                    if (StringUtils.isEmpty(orderRemindDeliveryResult.getOrderSellerContact())) {
                        map.put("店铺联系方式", "无");
                    } else {
                        map.put("店铺联系方式", orderRemindDeliveryResult.getOrderSellerContact());
                    }
                    list.add(map);
                }
                // excel文件名
                Map<String, List<Map<String, String>>> map = Maps
                        .newHashMap();
                map.put("提醒发货列表", list);
                String url = ExcelsUtil.createExcel(headers, map, new int[]{0});
                return ResponseResult.success(url);
            } else {
                return ResponseResult.success(BizCodeEnume.SELECT_DATA_NULL.getCode(), BizCodeEnume.SELECT_DATA_NULL.getMsg());
            }
        } else {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
    }

    /**
     * 查询退货退款订单列表。
     *
     * @param paraMap 查询参数集合，包含分页信息和过滤条件。
     * @return 返回查询结果，包含分页信息和退货退款订单详情列表。
     */
    @Override
    public ResponseResult listReturnOrder(Map<String, Object> paraMap) {
        // 根据查询条件和分页信息查询退货退款订单详情
        IPage<ReturnOrderDetailVO> page = baseMapper.listReturnRefund(new Query<ReturnOrderDetailVO>().getPage(paraMap), paraMap);

        // 获取脱敏开关的配置值
        String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.DESENSITIZATION_SWITCH.getDesc());

        // 如果脱敏开关打开，并且查询结果不为空，则对查询结果进行脱敏处理
        if (StrUtil.isNotEmpty(value) && value.equals(CommonType.commonStatus.YES.getCode().toString()) && !page.getRecords().isEmpty()) {
            for (ReturnOrderDetailVO vo : page.getRecords()) {
                BeanUtils.copyProperties(DesensitizationUtil.Desensitization(vo, ReturnOrderDetailVO.class), vo);
            }
        }

        // 返回处理后的查询结果
        return ResponseResult.success(PageUtils.getPage(page));
    }

    @Override
    public ResponseResult listrefundorderExcel(Map<String, String> paraMap, HttpServletResponse response) {
        UserInfoVO info = ParseTokenUtils.getUserInfoByToken(paraMap.get("token"));
        if (null == info) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        List<ReturnOrderDetailVO> result = baseMapper.listReturnRefundExcel(paraMap);
        String[] headers = new String[]{"退单编号", "退单金额", "佣金金额", "申请原因", "申请时间", "商家处理备注", "商家处理时间", "订单编号", "卖家",
                "商家", "付款时间", "退货状态"};
        if (result != null && result.size() > 0) {
            String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.DESENSITIZATION_SWITCH.getDesc());
            List<Map<String, String>> list = Lists.newArrayList();
            for (ReturnOrderDetailVO res : result) {
                if (StrUtil.isNotEmpty(value) && value.equals(CommonType.commonStatus.YES.getCode().toString())) {
                    BeanUtils.copyProperties(DesensitizationUtil.Desensitization(res, ReturnOrderDetailVO.class), res);
                }
                HashMap<String, String> map = Maps.newHashMap();
                if (res.getReturnState() != null) {
                    switch (res.getReturnState()) {
                        case "0":
                            res.setReturnState("卖家拒绝 ");
                            break;
                        case "1":
                            res.setReturnState("新发起等待卖家审核");
                            break;
                        case "2":
                            res.setReturnState("卖家审核通过  ");
                            break;
                        case "3":
                            res.setReturnState("卖家审核不通过 ");
                            break;
                        case "4":
                            res.setReturnState("卖家收到货物 ");
                            break;
                        case "5":
                            res.setReturnState("平台审核通过");
                            break;
                        case "6":
                            res.setReturnState("买家已填物流 ");
                            break;
                        case "7":
                            res.setReturnState("平台拒绝");
                            break;
                        case "8":
                            res.setReturnState("退款关闭");
                            break;
                        case "9":
                            res.setReturnState("平台介入");
                            break;
                        case "10":
                            res.setReturnState("验收不通过");
                            break;
                        case "11":
                            res.setReturnState("待买家退货");
                            break;
                        case "12":
                            res.setReturnState("待验收");
                            break;
                        case "13":
                            res.setReturnState("退货关闭");
                            break;
                        case "15":
                            res.setReturnState("用户撤销退款关闭");
                            break;
                        case "16":
                            res.setReturnState("用户撤销退货关闭");
                            break;
                        case "17":
                            res.setReturnState("平台退款中");
                            break;
                    }
                }
                map.put("退单编号", res.getReturnCode());
                map.put("退单金额", res.getReturnCash().toString());
                map.put("佣金金额", res.getReturnCommisionFee());
                map.put("申请原因", res.getReturnReason());
                map.put("申请时间", res.getReturnAddTime());
                map.put("商家处理备注", res.getReturnShopMessage());
                map.put("商家处理时间", res.getReturnShopTime());
                map.put("订单编号", res.getOrderId());
                map.put("买家", res.getBuyerUserAccount());
                map.put("商家", res.getShopName());
                map.put("付款时间", res.getPaymentTime());
                map.put("退款状态", res.getReturnState());
                list.add(map);
            }
            // excel文件名
            Map<String, List<Map<String, String>>> map = Maps
                    .newHashMap();
            map.put("合并数据", list);
            String url = ExcelsUtil.createExcel(headers, map, new int[]{0});
            return ResponseResult.success(url);
        } else {
            return ResponseResult.success(BizCodeEnume.SELECT_DATA_NULL.getCode(), BizCodeEnume.SELECT_DATA_NULL.getMsg());
        }
    }

    /**
     * 查询退货行销报告。
     * 根据传入的查询参数，获取退货行销报告的分页数据，并进行敏感信息脱敏处理。
     *
     * @param param 退货行销报告的查询条件。
     * @return 包含退货行销报告数据的响应结果。
     * @throws IllegalAccessException 如果访问字段失败。
     */
    @Override
    public ResponseResult queryReturnsaleReport(ReturnReportQueryDTO param) throws IllegalAccessException {
        // 如果查询参数为空，则创建一个新的查询参数对象。
        if (null == param) {
            param = new ReturnReportQueryDTO();
        }

        // 将查询参数对象转换为Map，用于后续的查询。
        Map data = ParseMapUtils.beanToMap(param);

        // 执行退货行销报告的查询，获取分页数据。
        IPage<ReturnReportQueryseleVO> page = baseMapper.queryReturnsaleReport(new Query<ReturnReportQueryseleVO>().getPage(data),
                param);

        // 获取敏感信息处理开关的配置值。
        String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.DESENSITIZATION_SWITCH.getDesc());

        // 对查询结果中的每一项，根据敏感信息处理开关，进行敏感信息脱敏处理。
        for (ReturnReportQueryseleVO res : page.getRecords()) {
            if (StrUtil.isNotEmpty(value) && value.equals(CommonType.commonStatus.YES.getCode().toString())) {
                BeanUtils.copyProperties(DesensitizationUtil.Desensitization(res, ReturnReportQueryseleVO.class), res);
            }
            // 根据订单号查询商品详情，并设置到退货行销报告数据中。
            List<GoodsDetailsVO> parm = baseMapper.queryGoodsDetail(res.getOrderNumber());
            res.setGoods(parm);
        }

        // 返回处理后的退货行销报告数据。
        return ResponseResult.success(PageUtils.getPage(page));
    }

    @Override
    public ResponseResult queryReturnsaleReportExcel(ReturnReportQueryDTO param) {
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        if (null != userInfo) {
            String[] title = {"订单号", "退款金额", "退货编号", "店铺名称", "买家名称", "买家账号", "支付单号", "第三方支付平台交易号", "应付金额", "支付方式", "退款状态",
                    "退款类型", "添加时间", "商家处理时间", "退款完成时间", "商品名称", "商品价格", "商品数量", "退货数量", "物流单号", "第三方支付金额", "充值卡余额支付"};
            List<Map<String, String>> list = Lists.newArrayList();
            //分页查询数据
            List<ReturnsaleReportVO> returnReportQueryseleResults = baseMapper.queryReturnsaleReportExcel(param);
            if (returnReportQueryseleResults.size() > 10000) {
                //导出数量过大，请联系运维
                return ResponseResult.fail(BizCodeEnume.EXPORT_QUANTITY_IS_TOO_LARGE);
            }
            String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.DESENSITIZATION_SWITCH.getDesc());
            for (ReturnsaleReportVO res : returnReportQueryseleResults) {
                // 订单状态 1.待付款 2.已付款 3.待发货 4.已发货 5.已签收 6.确认收货 7.取消 8.退款中 9.已退款 11.待自提',
                if (StrUtil.isNotEmpty(value) && value.equals(CommonType.commonStatus.YES.getCode().toString())) {
                    BeanUtils.copyProperties(DesensitizationUtil.Desensitization(res, ReturnsaleReportVO.class), res);
                }
                if (res.getPayCode() != null) {
                    switch (res.getPayCode()) {
                        // 0 无 1-余额支付，2-支付宝支付，3-微信支付，4-通联支付,5-健康账户余额支付',
                        case "0":
                            res.setPayCode("无");
                            break;
                        case "1":
                            res.setPayCode("余额支付");
                            break;
                        case "2":
                            res.setPayCode("支付宝支付");
                            break;
                        case "3":
                            res.setPayCode("微信支付");
                            break;
                        case "6":
                            res.setPayCode("充值卡支付");
                            break;
                        case "12":
                            res.setPayCode("线下支付");
                            break;
                        case "13":
                            res.setPayCode("账期支付");
                            break;
                        case "14":
                        case "15":
                            res.setPayCode("主管支付");
                            break;
                    }

                }
                if (res.getReturnType() != null) {
                    switch (res.getReturnType()) {
                        // 1-退款申请 2-退货申请 3-虚拟退款'
                        case "1":
                            res.setReturnType("退款申请");
                            break;
                        case "2":
                            res.setReturnType("退货申请 ");
                            break;
                        case "3":
                            res.setReturnType("虚拟退款");
                            break;
                    }
                }
                if (res.getReturnState() != null) {

                    switch (res.getReturnState()) {
                        case "1":
                            res.setReturnState("新发起等待卖家审核 ");
                            break;
                        case "2":
                            res.setReturnState("卖家审核通过 ");
                            break;
                        case "3":
                            res.setReturnState("卖家审核不通过");
                            break;
                        case "4":
                            res.setReturnState("卖家收到货物");
                            break;
                        case "5":
                            res.setReturnState("平台审核通过");
                            break;
                        case "6":
                            res.setReturnState("待卖家验收");
                            break;
                        case "8":
                            res.setReturnState("退款关闭");
                            break;
                        case "9":
                            res.setReturnState("平台介入中");
                            break;
                        case "13":
                            res.setReturnState(" 退货关闭");
                            break;
                        case "15":
                            res.setReturnState("用户撤销退款关闭");
                            break;
                        case "16":
                            res.setReturnState("用户撤销退货关闭");
                            break;
                    }
                }
                HashMap<String, String> map = Maps.newHashMap();
                map.put("订单号", res.getOrderId());
                map.put("退款金额", res.getReturnCash() + "");
                map.put("退货编号", res.getReturnCode());
                map.put("店铺名称", res.getShopName());
                map.put("买家名称", res.getBuyerUserName());
                map.put("买家账号", res.getBuyerUserAccount());
                map.put("支付单号", res.getPaymentNumber());
                map.put("第三方支付平台交易号", res.getPaymentOtherNumber() == null ? "" : res.getPaymentOtherNumber() + "");
                map.put("应付金额", res.getOrderPaymentAmount() + "");
                map.put("支付方式", res.getPayCode());
                map.put("退款状态", res.getReturnState());
                map.put("退款类型", res.getReturnType());
                map.put("添加时间", res.getReturnAddTime() + "");
                map.put("商家处理时间", res.getReturnShopTime() == null ? "" : res.getReturnShopTime() + "");
                map.put("退款完成时间", res.getReturnFinishTime() == null ? "" : res.getReturnFinishTime() + "");
                map.put("商品名称", res.getGoodsName());
                map.put("商品价格", res.getGoodsPrice());
                map.put("商品数量", res.getOrderGoodsNum() + "");
                map.put("退货数量", res.getOrderGoodsReturnnum() + "");
                map.put("物流单号", res.getOrderShippingCode());
                map.put("第三方支付金额", res.getTradeThirdPartyAmount() + "");
                map.put("充值卡余额支付", res.getTradePaymentRechargeCard() + "");
                list.add(map);
            }
            Map<String, List<Map<String, String>>> map = Maps.newHashMap();
            map.put("售后服务", list);
            String url = ExcelsUtil.createExcel(title, map, new int[]{0});
            return ResponseResult.success(url);
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 查询商家退款订单列表。
     *
     * @param map 包含查询参数的映射，其中包含token用于用户身份验证。
     * @return 返回查询结果的ResponseResult对象。
     * @throws ParseException 如果解析token失败。
     */
    @Override
    public ResponseResult listRefund(Map<String, Object> map) throws ParseException {
        // 从映射中获取并转换token，用于用户身份验证
        String token = map.get("token").toString();
        // 根据token解析用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        // 设置查询的订单时间
        setOrderTime(map);
        // 将商家ID添加到查询参数中
        map.put("shopId", userInfo.getShopId());
        // 如果商家类型存在，则将其添加到查询参数中
        if (userInfo.getShopType() != null) {
            map.put("shopType", userInfo.getShopType().toString());
        }
        // 将查询类型设置为退款
        // 退款
        map.put("returnType", "1");

        // 创建分页对象，并从映射中获取页码和每页记录数
        Page<SellerOrderDetailVO> page = new Page<>((Integer) map.get("page"), (Integer) map.get("limit"));
        // 根据查询参数查询退款订单
        IPage<SellerOrderDetailVO> sellerOrderDetails = baseMapper.listRefund(page, map);
        // 如果查询结果存在
        if (null != sellerOrderDetails) {
            HashMap refundMap = new HashMap();
            // 遍历查询结果中的每个订单
            for (SellerOrderDetailVO sellerOrderDetail : sellerOrderDetails.getRecords()) {
                // 设置订单ID和商家ID，用于查询退款商品详情
                String orderId = sellerOrderDetail.getOrderId();
                refundMap.put("orderId", orderId);
                refundMap.put("shopId", userInfo.getShopId());
                // 查询退款商品详情并设置到订单详情中
                List<GoodsDetailsListVO> listReturnGoods = baseMapper.listReturnGoods1(refundMap);
                sellerOrderDetail.setOrderGoodsDetail(listReturnGoods);
                // 清空退款映射，以便下一次循环使用
                refundMap.clear();
            }
        }
        // 返回处理后的查询结果
        return ResponseResult.success(PageUtils.getPage(sellerOrderDetails));
    }


    /**
     * 根据订单时间设置查询范围。
     *
     * @param map 一个包含订单信息的Map，其中可能包含订单时间的键值对。
     * @throws ParseException 如果解析日期时发生错误。
     *                        <p>
     *                        此方法根据传入的map中指定的订单时间，设置查询的开始和结束时间。
     *                        它支持查询当天、明天和三个月后的日期范围。
     *                        通过将开始和结束时间的字符串形式放入map中，供其他部分的代码使用。
     */
    private void setOrderTime(Map<String, Object> map) throws ParseException {
        // 检查map中是否包含订单时间信息
        if (map.get("orderTime") != null) {
            // 将订单时间从map中取出并转换为Integer类型
            Integer orderTime = (Integer) map.get("orderTime");
            // 创建一个日期格式化对象，用于将日期转换为字符串
            SimpleDateFormat df = new SimpleDateFormat(DateUtils.YYYY_MM_DD_HH_MM_SS);
            // 根据订单时间的值，设置不同的查询日期范围
            if (orderTime == 1) {
                // 查询当天的日期范围
                map.put("returnAddTime", DateUtils.getDayStartTime2());
                map.put("returnFinishTime", DateUtils.getDayEndTime2());
            } else if (orderTime == 2) {
                // 查询明天的日期范围
                map.put("returnAddTime", df.format(DateUtils.addDays(df.parse(DateUtils.getDayStartTime2()), 1)));
                map.put("returnFinishTime", df.format(DateUtils.addDays(df.parse(DateUtils.getDayEndTime2()), 1)));
            } else if (orderTime == 3) {
                // 查询三个月后的日期范围
                map.put("returnAddTime", df.format(DateUtils.addMonth(df.parse(DateUtils.getDayStartTime2()), 3)));
                map.put("returnFinishTime", df.format(DateUtils.addMonth(df.parse(DateUtils.getDayEndTime2()), 3)));
            }
        }
    }


    /**
     * 处理卖家审核的请求。
     *
     * @param map 包含审核相关信息的映射表，其中必须包含token和returnCode。
     * @return 根据审核结果返回相应的响应结果。
     * @throws TransactionException 如果交易过程中出现异常。
     */
    @Override
    public ResponseResult forSellerApprove(Map<String, String> map) throws TransactionException {
        // 从请求映射中获取token
        String token = map.get("token");
        // 通过token解析获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        // 检查用户是否有权限进行审核，如果没有shopId则表示无权限
        if (null == userInfo.getShopId()) {
            // 裸码 10086 已认领为 AUTH 域 30801「登录失败，请重新登录」，
            // 前端据此清 token 跳登录页 —— 没权限会被误当成没登录。改用 PERMISSION 域 31005。
            return ResponseResult.fail(PermissionErrorCode.PERMISSION_ACCESS_DENIED.code(),
                    PermissionErrorCode.PERMISSION_ACCESS_DENIED.meta().userMessage());
        }
        // 根据返回码查询退货状态
        Integer returnState = baseMapper.getReturnStateByReturnCode(map.get("returnCode"));
        // 如果当前状态不是待卖家审核，则返回错误信息
        if (returnState != null && !returnState.equals(CommonType.returnState.NEW_LAUNCH_PENDING_SELLER_REVIEW.getCode())) {
            return ResponseResult.fail(BizCodeEnume.ORDER_STATUS_CHANGE);
        }
        // 将用户和店铺相关信息添加到映射中，用于后续处理
        map.put("shopId", userInfo.getShopId());
        map.put("userId", userInfo.getUserId());
        map.put("shopType", String.valueOf(userInfo.getShopType()));
        map.put("orderOperator", userInfo.getUserName());
        // 调用布尔方法确认卖家审核结果
        Boolean torf = forSellerApproveBoolean(map);
        // 根据审核结果返回相应的响应
        if (!torf) {
            return ResponseResult.fail(2, "审核失败");
        }
        return ResponseResult.success(1, "审核成功");
    }

    /**
     * 京东取消订单 商城端退款
     * @param jdOrderIds
     * @return
     * @throws TransactionException
     */
    @Override
    public ResponseResult jingdongForSellerApprove(List<String> jdOrderIds) throws TransactionException {
        for (String jdOrderId : jdOrderIds) {
            // 根据京东订单号查询 商城订单信息
            ReturnOrderVO returnOrderVO = baseMapper.queryReturnOrderByJDOrderId(jdOrderId);
            Map<String, String> map = new HashMap<>();

            // 将用户和店铺相关信息添加到映射中，用于后续处理
            map.put("shopId", returnOrderVO.getShopId());
            map.put("userId", returnOrderVO.getSellerUserId());
            map.put("shopType", returnOrderVO.getShopType().toString());
            map.put("orderOperator", returnOrderVO.getSellerUserName());
            map.put("returnCode", returnOrderVO.getReturnCode());
            map.put("type", "1");
            map.put("returnShopMessage", "京东退款");
            map.put("orderId", returnOrderVO.getOrderId());
            // 调用布尔方法确认卖家审核结果
            Boolean torf = forSellerApproveBoolean(map);
            // 根据审核结果返回相应的响应
            if (!torf) {
                return ResponseResult.fail(2, "审核失败");
            }
            return ResponseResult.success(1, "审核成功");
        }
        return ResponseResult.success(1, "审核成功");
    }

    public void returnGoodsNum(String orderId, OrderBaseEntity entity){
        //获取goodsid commonid 购买数量
        List<WapGoodsSaleSumDTO> wapGood = orderGoodsMapper.queryGoodsNewType(orderId);
        // 如果是供应商订单 且不是供应商代发订单就 处理供应商商品库存
        if (CommonType.orderType.SUPPLY_TYPE.getCode().equals(entity.getOrderType()) && CommonType.dropShipping.DROPSHIPPING_NOT.getCode().equals(entity.getDropShipping())) {
            //退款成功后 修改库存
            oriOrderReturnMethod(orderId);
        } else {
            goodServiceClient.updateGoodsStock(wapGood);
            //退款成功后 修改库存(子订单)
            oriOrderReturnMethod(orderId);
        }
    }

    public void oriOrderReturnMethod(String orderId) {
        //子订单
        List<OrderBaseEntity> originalOrderList = orderBaseMapper.selectList(new LambdaQueryWrapper<OrderBaseEntity>()
                .eq(OrderBaseEntity::getOriginalOrder, orderId));
        if (!originalOrderList.isEmpty()) {
            for (OrderBaseEntity entity1 : originalOrderList) {
                //退款成功后 修改库存
                //获取goodsid commonid 购买数量
                List<WapGoodsSaleSumDTO> wapGood = orderGoodsMapper.queryGoodsNewType(entity1.getOrderId());
                //
                supplierClient.updateGoodsStock(wapGood);
                for (WapGoodsSaleSumDTO goods : wapGood) {
                    if (goods.getCommonId() != null && goods.getCommonId() == 0) {
                        continue;
                    }
                    Integer salenum = orderGoodsMapper.selectGoodsSalenum(goods);
                    Integer goodsNum = Integer.parseInt(goods.getGoodsNum());
                    if (salenum > goodsNum) {
                        salenum = salenum - goodsNum;
                    }

                    //遍历集合减去销量
                    orderGoodsMapper.lessGoodsBySales(goods.getGoodsId(), salenum);
                }

            }
        }
    }

    public Boolean forSellerApproveBoolean(Map<String, String> paraMap) throws TransactionException {
        Map<String, Object> orderGoodsMap = new HashMap<>();
        Map<String, Object> orderBaseMap = new HashMap<>();
        Map<String, Object> assetDetailMap = new HashMap<>();
        orderGoodsMap.put("orderOperator", paraMap.get("orderOperator"));
        OrderReturnEntity orderReturnEntity = baseMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>()
                .eq(OrderReturnEntity::getReturnCode, paraMap.get("returnCode")));
        String orderId = orderReturnEntity.getOrderId();
        OrderBaseEntity entity = orderBaseMapper.selectById(orderId);
        Integer orderStatus = baseMapper.queryOrderStatus(orderId);
        Integer virtualGoods = 1;
        //获取退货数量
        Map<String, Object> goodsNumMap = new HashMap<>();
        List<Map<String, Object>> goodsNum = baseMapper.queryBuyNumByorderId(orderId);
        goodsNumMap.put("goodsNum", goodsNum);
        //买家id
        String buyerUserId = goodsNum.get(0).get("buyer_user_id").toString();
        //判断是否是虚拟订单 且没有被核销
        Map<String, Object> map = baseMapper.selectisVirtualOrderByTime(orderId);
        if (null != map) {
            virtualGoods = 0;
            //如果该虚拟订单已过期 且过期不退款直接返回false
            Integer result = baseMapper.queryisVirtualOrderByTime(orderId);
            if (0 != result) {
                return false;
            }
        }

        //查询用户售后商品
        List<Integer> returnGoods = baseMapper.queryReturnGoods(orderId, paraMap.get("shopId"));
        String returnGoodsName = baseMapper.queryReturnGoodsNameByOrderId(orderId);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("orderId", orderId);
        paramMap.put("goodsName", returnGoodsName);
        List<String> messageList = new ArrayList<>();
        messageList.add(orderId);
        // 商家同意
        if (1 == Integer.parseInt(paraMap.get("type"))) {
            BigDecimal returnCash;
            if (orderStatus == 6) {
                //用户收到货物退款
                orderGoodsMap.put("returnCode", paraMap.get("returnCode"));
                orderGoodsMap.put("returnShopHandle", 2);
                //4-卖家收到货物
                orderGoodsMap.put("returnState", 2);
                orderGoodsMap.put("returnShopMessage", paraMap.get("returnShopMessage"));
                orderGoodsMap.put("returnShopTime", new Date());
                orderGoodsMap.put("returnFinishTime", TimeStampUtil.getNowDate());

                //	 1.2修改商家同意订单状态为 6
                orderBaseMap.put("orderId", orderId);
                //9.已退款
                if (entity.getPaymentCode() == 2) {
                    orderBaseMap.put("orderStatus", "28");
                } else {
                    orderBaseMap.put("orderStatus", "9");
                }
                //2是退款完成
                orderBaseMap.put("orderRefundStatus", "2");
                //2是退款完成
                orderBaseMap.put("orderReturnStatus", "0");
                Integer torf = baseMapper.updateOrderBaseById(orderBaseMap);

                Map<String, Object> updateGoodsMap = new HashMap<>();
                updateGoodsMap.put("orderId", orderId);
                updateGoodsMap.put("goodsRefundStatus", 2);
                updateGoodsMap.put("goodsReturnStatus", 0);
                for (Integer returnGood : returnGoods) {
                    updateGoodsMap.put("goodsId", returnGood);
                    baseMapper.updateOrderGoodsById1(updateGoodsMap);
                }


                String uOrderId = baseMapper.queryPaymentNumber(orderId);
                returnCash = baseMapper.queryReturnCash(orderId);

                List<Integer> orderTradeStatus = baseMapper.queryOrderRecordStatus(uOrderId);
                RefundParamDTO refundPara = new RefundParamDTO();
                boolean isRefundSuccess = false;
                if (torf == 1) {
                    refundPara = baseMapper.queryRefundPara(orderId);
                    refundPara.setOrderTradeStatus(orderTradeStatus);
                    refundPara.setReturnCash(returnCash);
                    refundPara.setShopId(Integer.valueOf(paraMap.get("shopId")));
                    refundPara.setShopType(Integer.valueOf(paraMap.get("shopType")));
                    isRefundSuccess = waitShipmentsRefundAndNotify(refundPara, orderId, returnCash);
                    //退款更新商品销量
                    updateGoodsSalenum(goodsNumMap);
                }
                if (!isRefundSuccess) {
                    // 回滚
                    GlobalTransactionContext.reload(RootContext.getXID()).rollback();
                    return false;
                }
                if (1 != torf) {
                    // 回滚
                    GlobalTransactionContext.reload(RootContext.getXID()).rollback();
                    return false;
                }
                //退还红包优惠卷
                returnred(orderId, returnCash, returnGoods);

                //退还积分
                refundpoints(orderId, returnCash);
                //已发货退款  且是普通商品 不能是虚拟商品
            } else if (orderStatus == 4 && virtualGoods == 1) {
                orderGoodsMap.put("returnCode", paraMap.get("returnCode"));
                orderGoodsMap.put("returnShopHandle", 2);
                orderGoodsMap.put("returnState", 2);
                orderGoodsMap.put("returnShopMessage", paraMap.get("returnShopMessage"));
                orderGoodsMap.put("returnShopTime", new Date());

                //	 1.2修改商家同意订单状态为 6
                orderBaseMap.put("orderId", orderId);
                if (entity.getPaymentCode() == 2) {
                    orderBaseMap.put("orderStatus", "28");
                } else {
                    orderBaseMap.put("orderStatus", "9");
                }
                orderBaseMap.put("orderRefundStatus", "2");
                orderBaseMap.put("orderFinishedTime", TimeStampUtil.getNowDate());
                Integer torf = baseMapper.updateOrderBaseById(orderBaseMap);
                Map<String, Object> updateGoodsMap = new HashMap<>();
                updateGoodsMap.put("orderId", orderId);
                updateGoodsMap.put("goodsRefundStatus", 2);
                baseMapper.updateOrderGoodsById(updateGoodsMap);

                String uOrderId = baseMapper.queryPaymentNumber(orderId);
                returnCash = baseMapper.queryReturnCash(orderId);
                List<Integer> orderTradeStatus = baseMapper.queryOrderRecordStatus(uOrderId);
                RefundParamDTO refundPara = new RefundParamDTO();
                boolean isRefundSuccess = false;
                if (torf == 1) {
                    refundPara = baseMapper.queryRefundPara(orderId);
                    refundPara.setOrderTradeStatus(orderTradeStatus);
                    refundPara.setReturnCash(returnCash);
                    refundPara.setShopId(Integer.valueOf(paraMap.get("shopId")));
                    refundPara.setShopType(Integer.valueOf(paraMap.get("shopType")));
                    isRefundSuccess = waitShipmentsRefundAndNotify(refundPara, orderId, returnCash);
                    //退款更新商品销量
                    updateGoodsSalenum(goodsNumMap);
                }
                if (!isRefundSuccess) {
                    // 回滚
                    GlobalTransactionContext.reload(RootContext.getXID()).rollback();
                    return false;
                }

                if (1 != torf) {
                    // 回滚
                    GlobalTransactionContext.reload(RootContext.getXID()).rollback();
                    return false;
                }
            } else if (orderStatus == 3 || virtualGoods == 0) {
                //未发货退款  虚拟商品订单可以进来
                orderGoodsMap.put("returnCode", paraMap.get("returnCode"));
                orderGoodsMap.put("returnShopHandle", 2);
                //卖家审核通过
                orderGoodsMap.put("returnState", 2);
                orderGoodsMap.put("returnShopMessage", paraMap.get("returnShopMessage"));
                orderGoodsMap.put("returnShopTime", new Date());
                orderGoodsMap.put("returnFinishTime", TimeStampUtil.getNowDate());

                //	 1.2修改商家同意订单状态为 6
                orderBaseMap.put("orderId", orderId);
                //已退款
                if (entity.getPaymentCode() == 2) {
                    orderBaseMap.put("orderStatus", "28");
                } else {
                    orderBaseMap.put("orderStatus", "9");
                }
                orderBaseMap.put("orderRefundStatus", "2");
                orderBaseMap.put("orderReturnStatus", "0");
                orderBaseMap.put("orderFinishedTime", TimeStampUtil.getNowDate());
                Integer torf = baseMapper.updateOrderBaseById(orderBaseMap);
                Map<String, Object> updateGoodsMap = new HashMap<>();
                updateGoodsMap.put("orderId", orderId);
                updateGoodsMap.put("goodsRefundStatus", 2);
                updateGoodsMap.put("goodsReturnStatus", 0);
                baseMapper.updateOrderGoodsById(updateGoodsMap);

                String uOrderId = baseMapper.queryPaymentNumber(orderId);
                returnCash = baseMapper.queryReturnCash(orderId);

                List<Integer> orderTradeStatus = baseMapper.queryOrderRecordStatus(uOrderId);
                RefundParamDTO refundPara = new RefundParamDTO();
                boolean isRefundSuccess = false;
                if (torf == 1) {
                    refundPara = baseMapper.queryRefundPara(orderId);
                    refundPara.setOrderTradeStatus(orderTradeStatus);
                    refundPara.setReturnCash(returnCash);
                    refundPara.setShopId(Integer.valueOf(paraMap.get("shopId")));
                    refundPara.setShopType(Integer.valueOf(paraMap.get("shopType")));
                    System.out.println("+++++++" + refundPara);
//                    updateGoodsSalenum(goodsNumMap);
                    if (refundPara.getPayCode() != 12) {
                        isRefundSuccess = waitShipmentsRefundAndNotify(refundPara, orderId, returnCash);
                    } else {
                        isRefundSuccess = true;
                        notifyDistributionRefund(orderId, returnCash);
                    }
                }
                if (!isRefundSuccess) {
                    // 回滚
                    GlobalTransactionContext.reload(RootContext.getXID()).rollback();
                    return false;
                }
                if (1 != torf) {
                    // 回滚
                    GlobalTransactionContext.reload(RootContext.getXID()).rollback();
                    return false;
                }
                returnGoodsNum(orderId,entity);
                //退款成功后 修改库存
                //获取goodsid commonid 购买数量
                List<WapGoodsSaleSumUpdateVO> wapGood = baseMapper.queryGoodsXinXi(orderId);
                for (WapGoodsSaleSumUpdateVO wapGoodsSaleSumUpdate : wapGood) {
                    // 如果当前订单是参加秒杀活动 需要回退库存到秒杀活动中
                    if (null != wapGoodsSaleSumUpdate.getPromotionId() && wapGoodsSaleSumUpdate.getPromotionId().equals(CommonType.promotionStatus.SECKILL.getCode())) {
                        baseMapper.updateSeckillStockByGoodsId(wapGoodsSaleSumUpdate);
                    } else {
                        baseMapper.updateCommonStockByGoodsId(wapGoodsSaleSumUpdate);
                        baseMapper.updateGoodsStockByGoodsId(wapGoodsSaleSumUpdate);
                    }

                    //遍历集合减去销量
                    baseMapper.lessgoodsBySales(wapGoodsSaleSumUpdate);
                }

                //退还红包和代金卷
                returnred(orderId, returnCash, returnGoods);
            } else {
                return false;
            }
            // 售后分销佣金处理
            commissionReturn(orderId, returnGoods);

            // 由于退款 是全订单退款 所以退红包
            baseMapper.updateHongbaoStart(orderId);
            // 处理子单佣金
            updateSonOrderCommissionFee(orderId);
            if (entity.getPaymentCode() != 2) {
                //插入我的余额明细表
                assetDetailMap.put("orderId", orderId);
                assetDetailMap.put("detailType", 2);
                assetDetailMap.put("assetType", 1);
                assetDetailMap.put("assetAmount", returnCash);
                AssetDirectionAndPayTypeInfoDTO assetInfo = baseMapper.getAssetDirectionAndPayTypeInfo(orderId);
                if (null != assetInfo) {
                    assetDetailMap.put("assetDirection", assetInfo.getAssetDirection());
                    assetDetailMap.put("payType", assetInfo.getPayType());
                    assetDetailMap.put("userId", assetInfo.getUserId());
                    baseMapper.insertDetail(assetDetailMap);
                }
            }
            //判断是否是虚拟订单 且没有被核销
            if (null != map) {
                virtualGoods = 0;
                //是否核销 0是 1否 2已过期
                Integer isCancel = Integer.parseInt(map.get("isCancel").toString());
                if (null != isCancel && 0 != isCancel) {
                    //如果该虚拟订单已过期 且过期不退款直接返回false
                    Integer result = baseMapper.queryisVirtualOrderByTime(orderId);
                    if (0 != result) {
                        return false;
                    }
                    boolean returnFlag = false;
                    //未核销且在有效期内
                    if (1 == isCancel) {
                        Integer result1 = baseMapper.queryisVirtualOrderByTime1(orderId);
                        if (null != result1 && result1 > 0) {
                            returnFlag = true;
                        }
                        //过期且可过期退款
                    } else if (2 == isCancel) {
                        Integer result2 = baseMapper.queryisVirtualOrderByTime2(orderId);
                        if (null != result2 && result2 > 0) {
                            //如果该虚拟订单已过期 且过期允许退款就扣除积分
                            returnFlag = true;
                            if (entity.getPaymentCode() != 2) {
                                Map<String, Object> tmpMap = baseMapper.queryAddpointsLogByOrderId(orderId);
                                if (null != tmpMap) {
                                    MemberMessageListIntegralDTO updateMemberMessageListIntegral = new MemberMessageListIntegralDTO();
                                    ArrayList<String> list = new ArrayList<>();
                                    list.add((String) tmpMap.get("user_id"));
                                    updateMemberMessageListIntegral.setUserIdList(list);
                                    updateMemberMessageListIntegral.setIntegral((Integer) tmpMap.get("points_log_points"));
                                    //1是增加积分 0 是扣除积分
                                    updateMemberMessageListIntegral.setType(0);
                                    feignUpdateMemberIntegral(updateMemberMessageListIntegral);
                                    //添加积分修改记录
                                    UserPointLogParamDTO userPointLog = new UserPointLogParamDTO();
                                    userPointLog.setPointsLogType(2);
                                    userPointLog.setClassId(9);
                                    userPointLog.setPointsLogTime(new Date());
                                    userPointLog.setPointsLogDesc("商品退款");
                                    userPointLog.setPointsLogFlag("reg");
                                    userPointLog.setUserId(tmpMap.get("user_id").toString());
                                    userPointLog.setPointsLogPoints((Integer) tmpMap.get("points_log_points"));
                                    userPointLog.setPointsLogOperate("商品退款");
                                    userPointLog.setOrderId(paraMap.get("orderId"));
                                    this.baseMapper.feginAddpointsLog(userPointLog);
                                }
                            }
                        }
                    }
                    if (returnFlag) {
                        Map<String, Object> dataMap = this.baseMapper.queryOrderReturnInfoById(orderId);
                        dataMap.put("orderId", orderId);
                        if (returnCash != null && entity.getOrderPaymentAmount().compareTo(returnCash) > 0) {
                            BigDecimal lv = orderReturnEntity.getReturnCash().divide(entity.getOrderPaymentAmount(), 2, RoundingMode.HALF_UP);
                            if (entity.getOrderCommissionFee() != null) {
                                dataMap.put("orderCommissionFee", BigDecimal.valueOf(entity.getOrderCommissionFee()).multiply(lv).setScale(2, RoundingMode.HALF_UP));
                            }
                            if (entity.getBaseMemberDiscount() != null) {
                                dataMap.put("baseMemberDiscount", BigDecimal.valueOf(entity.getBaseMemberDiscount()).multiply(lv).setScale(2, RoundingMode.HALF_UP));
                            }
                            if (entity.getDistributionDiscount() != null) {
                                dataMap.put("distributionDiscount", entity.getDistributionDiscount().multiply(lv).setScale(2, RoundingMode.HALF_UP));
                            }
                        }
                        this.baseMapper.updateOrderReturnInfoById(dataMap);
                    }
                }
            }
            //普通订单,退回积分
            if (virtualGoods == 1) {
                Map<String, Object> dataMap = this.baseMapper.queryOrderReturnInfoById(orderId);
                BigDecimal goodsAmount = BigDecimalUtils.setTwoScale(entity.getOrderPaymentAmount().subtract(entity.getOrderShippingFee()));
                if (goodsAmount.compareTo(orderReturnEntity.getReturnCash()) > 0) {
                    BigDecimal orderCommissionFee = BigDecimal.ZERO;
                    List<OrderGoodsVO> returnGoodsList = orderGoodsMapper.queryOrderGoodsByOrderId(orderId);
                    for (OrderGoodsVO orderGoodsVO : returnGoodsList) {
                        if (Objects.equals(orderGoodsVO.getOrderGoodsNum(), orderGoodsVO.getOrderGoodsReturnNum())) {
                            orderCommissionFee = orderCommissionFee.add(orderGoodsVO.getOrderGoodsCommission());
                        }else{
                            BigDecimal oneGoodsCommission = orderGoodsVO.getOrderGoodsCommission().divide(BigDecimal.valueOf(orderGoodsVO.getOrderGoodsNum()), 2, RoundingMode.HALF_UP);
                            BigDecimal returnGoodsCommission = oneGoodsCommission.multiply(BigDecimal.valueOf(orderGoodsVO.getOrderGoodsReturnNum()));
                            orderCommissionFee = orderCommissionFee.add(returnGoodsCommission);
                        }
                    }
                    dataMap.put("orderCommissionFee", orderCommissionFee.setScale(2, RoundingMode.HALF_UP));
                    BigDecimal lv = orderReturnEntity.getReturnCash().divide(goodsAmount, 2, RoundingMode.HALF_UP);
                    if (entity.getBaseMemberDiscount() != null) {
                        dataMap.put("baseMemberDiscount", BigDecimal.valueOf(entity.getBaseMemberDiscount()).multiply(lv).setScale(2, RoundingMode.HALF_UP));
                    }
                    if (entity.getDistributionDiscount() != null) {
                        dataMap.put("distributionDiscount", entity.getDistributionDiscount().multiply(lv).setScale(2, RoundingMode.HALF_UP));
                    }
                }else {
                    dataMap.put("orderCommissionFee", entity.getOrderCommissionFee());
                    dataMap.put("baseMemberDiscount", entity.getBaseMemberDiscount());
                    dataMap.put("distributionDiscount", entity.getDistributionDiscount());
                }
                dataMap.put("orderId", orderId);
                this.baseMapper.updateOrderBasePingTaiCommission(orderId, new BigDecimal(dataMap.get("orderCommissionFee").toString()));
                this.baseMapper.updateOrderReturnInfoById(dataMap);
            }
            //退款完成时间
            orderGoodsMap.put("returnFinishTime", LocalDateTime.now());
            //罚扣质保金
            charging(orderId, returnGoods, paraMap.get("userId"), Integer.valueOf(paraMap.get("shopId")));

            //退回plus折扣额度
            try {
                plusdis(orderId, buyerUserId, returnGoods);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            // 设置消息模板信息
            paramMap.put("templateId", CommonType.messageTemplate.CODE_D.getCode());
            paramMap.put("userId", entity.getBuyerUserId());
            paramMap.put("list", messageList);
            baseServiceClient.sendMessageTemplate(paramMap);
        } else {
            // 商家不同意
            orderGoodsMap.put("returnCode", paraMap.get("returnCode"));
            orderGoodsMap.put("returnShopHandle", 0);
            orderGoodsMap.put("returnState", 3);
            orderGoodsMap.put("returnShopMessage", paraMap.get("returnShopMessage"));
            orderGoodsMap.put("returnShopTime", new Date());
            Calendar instance = Calendar.getInstance();
            instance.setTime(new Date());
            instance.add(Calendar.DATE, 2);
            Date expiration_time = instance.getTime();
            orderGoodsMap.put("expirationTime", expiration_time);

            Map<String, Object> updateMap = new HashMap<String, Object>();
            Map<String, Object> updateGoodsMap = new HashMap<String, Object>();
            Integer torfOne = null;
            Integer torfTwo = null;
            updateMap.put("orderId", orderId);
            // 原值：2，现改为0，用户可持续申请
            updateMap.put("orderRefundStatus", "3");
            // 原值：2，现改为0，用户可持续申请
            updateMap.put("orderReturnStatus", "0");

            updateGoodsMap.put("orderId", orderId);

            updateGoodsMap.put("goodsReturnStatus", 0);
            updateGoodsMap.put("goodsRefundStatus", 3);
            for (Integer returnGood : returnGoods) {
                updateGoodsMap.put("goodsId", returnGood);
                torfTwo = this.baseMapper.updateOrderGoodsById1(updateGoodsMap);
            }

            //添加商家拒绝退款时间
            String businessTime = String.valueOf(System.currentTimeMillis());
            String businessRefundTime = TimeStampUtil.stampToDate(businessTime);

            updateGoodsMap.put("businessRefundTime", businessRefundTime);

            torfOne = this.baseMapper.updateOrderBaseById(updateMap);

            //更新record表状态
            Map<String, String> feignMap = new HashMap<>();
            feignMap.put("orderId", orderId);
            // 原值：7
            feignMap.put("recordStatus", "8");
            this.baseMapper.updateRefuse(feignMap);

            // 设置消息模板信息
            paramMap.put("templateId", CommonType.messageTemplate.CODE_N.getCode());
            paramMap.put("userId", entity.getBuyerUserId());
            paramMap.put("returnShopMessage", paraMap.get("returnShopMessage"));
            messageList.add(paraMap.get("returnShopMessage"));
            paramMap.put("list", messageList);
            baseServiceClient.sendMessageTemplate(paramMap);

            if (1 != torfOne || torfTwo < 1) {
                GlobalTransactionContext.reload(RootContext.getXID()).rollback();
                return false;
            }
        }
        //1:同意,0:拒绝
        if (1 == Integer.parseInt(paraMap.get("type"))) {
            // 线下支付订单需平台退款
            orderGoodsMap.put("returnState", entity.getPaymentCode() == 2 ? CommonType.returnState.WAITING_FOR_PLATFORM_REFUND.getCode()
                    : CommonType.returnState.APPROVED_BY_THE_SELLER.getCode());
        } else {
            orderGoodsMap.put("returnState", CommonType.returnState.SELLER_FAILED_THE_REVIEW.getCode());
        }
        Integer torf = this.baseMapper.updateOrderReturnById(orderGoodsMap);
        if (1 == torf) {
            //子订单
            List<OrderBaseEntity> originalOrderList = orderBaseMapper.selectList(new LambdaQueryWrapper<OrderBaseEntity>()
                    .eq(OrderBaseEntity::getOriginalOrder, orderId));
            if (!originalOrderList.isEmpty()) {
                for (OrderBaseEntity entity1 : originalOrderList) {
                    //更新子订单退款详情信息
                    OrderReturnEntity orderSonReturnEntity = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>()
                            .eq(OrderReturnEntity::getOrderId, entity1.getOrderId()));
                    if (null != orderSonReturnEntity) {
                        //子订单退货编号
                        orderGoodsMap.put("returnCode", orderSonReturnEntity.getReturnCode());
//                        //1:同意,0:拒绝
//                        orderGoodsMap.put("returnState", 1 == Integer.parseInt(param.get("type")) ?
//                                CommonType.returnState.APPROVED_BY_THE_SELLER.getCode() : CommonType.returnState.SELLER_FAILED_THE_REVIEW.getCode());
                        // 更新子订单退款详情信息。order_return
                        this.baseMapper.updateOrderReturnById(orderGoodsMap);
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 处理子单 退单佣金
     *
     * @param orderId
     */
    public void updateSonOrderCommissionFee(String orderId) {
        // 查询当前订单下的子单
        List<OrderGoodsVO> sonOrderList = orderBaseMapper.querySonOrderList(orderId);
        if (null != sonOrderList && !sonOrderList.isEmpty()) {
            for (OrderGoodsVO sonOrderVO : sonOrderList) {
                // 判断当前 子订单是否有退款
                String sonReturnCode = orderBaseMapper.querySonReturnCode(sonOrderVO.getOrderId());
                if (StringUtils.isNotBlank(sonReturnCode)) {
                    BigDecimal returnSonOrderCommissionFee = new BigDecimal(0);
                    // 当前子单总的佣金
                    BigDecimal sonOrderCommissionFee = sonOrderVO.getOrderCommissionFee();
                    // 计算要退的佣金
                    List<OrderGoodsVO> sonOrderGoods = orderBaseMapper.querySonOrderGoodsList(sonOrderVO.getOrderId());
                    for (OrderGoodsVO sonOrderGood : sonOrderGoods) {
                        if (null != sonOrderGood.getOrderGoodsNum() && null != sonOrderGood.getOrderGoodsReturnNum()
                            &&  sonOrderGood.getOrderGoodsNum().equals(sonOrderGood.getOrderGoodsReturnNum())) {
                            returnSonOrderCommissionFee = returnSonOrderCommissionFee.add(sonOrderGood.getOrderGoodsCommission());
                        } else if (sonOrderGood.getOrderGoodsReturnNum() != 0) {
                            returnSonOrderCommissionFee = returnSonOrderCommissionFee.add(sonOrderGood.getOrderGoodsCommission()
                                    .divide(new BigDecimal(sonOrderGood.getOrderGoodsNum()), 2, BigDecimal.ROUND_HALF_UP)
                                    .multiply(new BigDecimal(sonOrderGood.getOrderGoodsReturnNum()))
                            );
                        }
                    }
                    // 佣金存入库中
                    orderBaseMapper.updateReturnOrderCommissionFe(sonOrderVO.getOrderId(), returnSonOrderCommissionFee);
                    this.baseMapper.updateReturnOrderCommissionFe(sonOrderVO.getOrderId(), returnSonOrderCommissionFee);
                }
            }
        }
    }

    public void plusdis(String orderId, String userId, List goodsId) throws ParseException {
        //退还该订单plus折扣额度
        Map map1 = orderCommissionMapper.selectplusdis(orderId, goodsId);
        String goodsPlusDiscount = String.valueOf(map1.get("goods_plus_discount"));
        goodsPlusDiscount = goodsPlusDiscount == "null" ? "0" : goodsPlusDiscount;
        BigDecimal bigDecimal = new BigDecimal(goodsPlusDiscount);

        //下单时间
        Date createTime = DateUtils.parseCNDate(map1.get("order_goods_time").toString());
        //查看会员是否过期
        PlusMemberEntityVO plusMemberEntity = orderBaseMapper.selectplusmember(userId);
        if (plusMemberEntity != null) {
            DateFormat dft = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
            //已用额度
            BigDecimal usedAmount = plusMemberEntity.getUsedAmount();

            //任务执行时间
            Date star = dft.parse(dft.format(plusMemberEntity.getThaliStarttime()));

            Date endDay = dft.parse(dft.format(new Date()));
            //当前时间与其他时间相差的毫秒数
            Long diff = endDay.getTime() - star.getTime();
            BigDecimal divide = BigDecimal.valueOf(diff).divide
                    (BigDecimal.valueOf(60 * 60 * 1000 * 24), 2, RoundingMode.HALF_UP);
            int value = divide.divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP).intValue();
            //计算订单是否用返回折扣
            //开始时间
            Calendar instance = Calendar.getInstance();
            instance.setTime(plusMemberEntity.getThaliStarttime());
            instance.add(Calendar.DATE, 30 * value);
            //开始时间
            Date starttime = instance.getTime();
            instance.add(Calendar.DATE, 30);
            //结束时间
            Date endtime = instance.getTime();

            boolean b = DateUtils.belongCalendar(createTime, starttime, endtime);
            if (b) {
                //在时间段  退还plus会员折扣
                orderBaseMapper.updateplusmember(userId, bigDecimal);
            }
        }
    }

    @Override
    public ResponseResult returnorder(Map<String, Object> paraMap) throws ParseException {
        String token = paraMap.get("token").toString();
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        setOrderTime(paraMap);
        paraMap.put("shopId", userInfo.getShopId());
        paraMap.put("returnType", "2");
        if (userInfo.getShopType() != null) {
            paraMap.put("shopType", userInfo.getShopType().toString());
        }

        Page<ReturnOrderVO> page = new Page<>((Integer) paraMap.get("page"), (Integer) paraMap.get("limit"));
        IPage<ReturnOrderVO> returnRefundList = this.baseMapper.listReturnOrders(page, paraMap);
        for (ReturnOrderVO returnOrder : returnRefundList.getRecords()) {
            // 退货，可能又多件商品
            Map<String, String> selectMap = new HashMap<String, String>();
            List<GoodsDetailsVO> goodsDetails = new ArrayList<GoodsDetailsVO>();
            selectMap.put("orderId", returnOrder.getOrderId());
            //判断是否为商家，0供应商，1经销商
            if (Integer.parseInt(paraMap.get("type").toString()) != 1) {
                selectMap.put("shopId", userInfo.getShopId());
            }
            goodsDetails = this.baseMapper.listReturnGoods(selectMap);
            returnOrder.setGoodsDetails(goodsDetails);
        }
        return ResponseResult.success(PageUtils.getPage(returnRefundList));
    }

    /**
     * 处理卖家审核的请求。
     *
     * @param map 包含请求所需信息的Map对象，其中包括token和returnCode等关键信息。
     * @return 根据处理结果返回相应的ResponseResult对象，包含审核是否成功的状态和消息。
     */
    @Override
    public ResponseResult retForSellerApprove(Map<String, Object> map) {
        // 从请求参数中获取token
        String token = map.get("token").toString();
        // 使用token解析获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        // 检查用户是否有店铺权限，如果没有则返回权限不足的错误
        if (null == userInfo.getShopId()) {
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
        // 根据返回码查询退货状态
        Integer returnState = this.baseMapper.getReturnStateByReturnCode((String) map.get("returnCode"));
        // 如果当前退货状态不是待卖家审核，则返回订单状态改变的错误
        if (returnState != null && !returnState.equals(CommonType.returnState.NEW_LAUNCH_PENDING_SELLER_REVIEW.getCode())) {
            return ResponseResult.fail(BizCodeEnume.ORDER_STATUS_CHANGE);
        }
        // 将用户信息添加到请求参数map中
        map.put("orderOperator", userInfo.getUserName());

        // 调用方法检查是否满足卖家审核的条件
        Boolean torf = retForSellerApproveBoolean(map);
        // 如果条件不满足，则返回审核失败的错误
        if (!torf) {
            return ResponseResult.fail(2, "审核失败");
        }
        // 如果条件满足，则返回审核成功的消息
        return ResponseResult.success(1, "审核成功");

    }
    /**
     * 京东售后单 退换货 处理
     *
     * @param map 包含请求所需信息的Map对象，其中包括token和returnCode等关键信息。
     * @return 根据处理结果返回相应的ResponseResult对象，包含审核是否成功的状态和消息。
     */
    @Override
    public ResponseResult jingDongReturnAudit(Map<String, Object> map) {

        Integer type = Integer.valueOf(map.get("type").toString());
        List<JingDongReturnOrderDTO> list = JsonUtils.string2Obj(JsonUtils.toString(map.get("orderIdList")), List.class, JingDongReturnOrderDTO.class);
        for (JingDongReturnOrderDTO jingDongReturnOrderDTO : list) {
            // 根据京东退单申请单号查询 商城订单号
            String orderId = baseMapper.queryOrderIdByJDReturnOrderId(jingDongReturnOrderDTO.getJdReturenOrderId());
            // 根据京东订单号查询 商城订单信息
            ReturnOrderVO returnOrderVO = baseMapper.queryReturnOrderByJDOrderId(orderId);
            // 根据京东售后申请单号 京东订单号 调用京东接口 查询寄回地址
            Map<String, String> stringStringMap = goodServiceClient.queryJdReturnAddres(jingDongReturnOrderDTO.getJdOrderId(), jingDongReturnOrderDTO.getJdReturenOrderId());

            if(returnOrderVO.getReturnMethodStatus() == CommonType.returnMethodType.RETURN.getCode()){
                //退货
                Map<String, Object> paraMap = new HashMap<>();
                paraMap.put("orderOperator",returnOrderVO.getSellerUserName());
                paraMap.put("returnCode",returnOrderVO.getReturnCode());
                paraMap.put("returnAddrId","");
                paraMap.put("type",type);
                paraMap.put("returnAddr","");
                if(type == 0){
                    paraMap.put("returnShopMessage","审核失败");
                }else{
                    paraMap.put("returnShopMessage","审核通过");
                }
                if(null != stringStringMap){
                    paraMap.put("returnRealName",stringStringMap.get("rurernUserName") != null ? stringStringMap.get("rurernUserName") : "");
                    paraMap.put("returnNumber",stringStringMap.get("rurernPhone") != null ? stringStringMap.get("rurernPhone") : "");
                    paraMap.put("returnAddr",stringStringMap.get("rurernAddress") != null ? stringStringMap.get("rurernAddress") : "");
                }
                retForSellerApproveBoolean(paraMap);

            }else if(returnOrderVO.getReturnMethodStatus() == CommonType.returnMethodType.BARTER.getCode()){
                // 换货
                ReturnOrderDTO returnOrderDTO = new ReturnOrderDTO();
                returnOrderDTO.setOrderOperator(returnOrderVO.getSellerUserName());
                returnOrderDTO.setOrderId(returnOrderVO.getOrderId());
                returnOrderDTO.setReturnCode(returnOrderVO.getReturnCode());
                returnOrderDTO.setType(type);
                if(type == 0){
                    returnOrderDTO.setReturnShopMessage("审核失败");
                }else{
                    returnOrderDTO.setReturnShopMessage("审核通过");
                }
                if(null != stringStringMap){
                    returnOrderDTO.setReturnRealName(stringStringMap.get("rurernUserName") != null ? stringStringMap.get("rurernUserName") : "");
                    returnOrderDTO.setReturnNumber(stringStringMap.get("rurernPhone") != null ? stringStringMap.get("rurernPhone") : "");
                    returnOrderDTO.setReturnAddr(stringStringMap.get("rurernAddress") != null ? stringStringMap.get("rurernAddress") : "");
                }
                orderBarterService.approveBarter(returnOrderDTO);
            }
        }
        return ResponseResult.success(1, "审核成功");

    }
    @Transactional
    public Boolean retForSellerApproveBoolean(Map<String, Object> paraMap) {
        Map<String, Object> orderGoodsMap = new HashMap<>();
        orderGoodsMap.put("orderOperator", paraMap.get("orderOperator"));
        ManageShopAddressQueryVO returnAddr = null;
        String orderId = this.baseMapper.getOrderIdByReturnCode(paraMap.get("returnCode").toString());
        BigDecimal returnCash = this.baseMapper.queryReturnCash(orderId);
        if (paraMap.get("returnAddrId") != null && paraMap.get("returnAddrId") != "") {
            returnAddr = queryReturnShop(Integer.valueOf(paraMap.get("returnAddrId").toString()), paraMap.get("token").toString());
        }
        OrderBaseEntity orderBaseEntity = orderBaseMapper.selectById(orderId);
        String returnGoodsName = this.baseMapper.queryReturnGoodsNameByOrderId(orderId);
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", orderId);
        map.put("goodsName", returnGoodsName);
        List<String> list = new ArrayList<>();
        list.add(orderId);
        map.put("userId", orderBaseEntity.getBuyerUserId());
        map.put("templateId", CommonType.messageTemplate.CODE_E.getCode());
        if(null !=  paraMap.get("returnRealName") ){
            orderGoodsMap.put("returnRealName", paraMap.get("returnRealName"));
        }
        if(null !=  paraMap.get("returnNumber") ){
            orderGoodsMap.put("returnNumber", paraMap.get("returnNumber"));
        }
        // 商家同意
        if (1 == Integer.parseInt(paraMap.get("type").toString())) {
            orderGoodsMap.put("returnCode", paraMap.get("returnCode"));
            orderGoodsMap.put("returnShopHandle", 2);
            orderGoodsMap.put("returnState", 2);
            orderGoodsMap.put("returnAddrId", paraMap.get("returnAddrId"));
            orderGoodsMap.put("returnAddr", paraMap.get("returnAddr"));
            orderGoodsMap.put("returnAddrName", paraMap.get("returnAddr"));
            orderGoodsMap.put("returnShopMessage", paraMap.get("returnShopMessage"));
            orderGoodsMap.put("returnShopTime", new Date());
            Calendar instance = Calendar.getInstance();
            instance.setTime(new Date());
            instance.add(Calendar.DATE, 7);
            Date expirationTime = instance.getTime();
            if (returnAddr != null) {
                orderGoodsMap.put("returnRealName", returnAddr.getShippingAddressContact());
                orderGoodsMap.put("returnNumber", returnAddr.getShippingAddressPhone());
            }
            orderGoodsMap.put("expirationTime", expirationTime);
            list.add("同意");
            map.put("result", "同意");
            map.put("status", "商家已同意退货，请尽快填写退货物流信息");
        } else {
            // 商家不同意
            orderGoodsMap.put("returnCode", paraMap.get("returnCode"));
            orderGoodsMap.put("returnShopHandle", 3);
            orderGoodsMap.put("returnState", 3);
            orderGoodsMap.put("returnShopMessage", paraMap.get("returnShopMessage"));
            orderGoodsMap.put("returnAddrId", paraMap.get("returnAddrId"));
            orderGoodsMap.put("returnAddr", paraMap.get("returnAddr"));
            orderGoodsMap.put("returnAddrName", paraMap.get("returnAddr"));
            orderGoodsMap.put("returnShopTime", new Date());
            orderGoodsMap.put("orderId", orderId);
            Calendar instance = Calendar.getInstance();
            instance.setTime(new Date());
            instance.add(Calendar.DATE, 2);
            Date expirationTime = instance.getTime();
            if (returnAddr != null) {
                orderGoodsMap.put("returnRealName", returnAddr.getShippingAddressContact());
                orderGoodsMap.put("returnNumber", returnAddr.getShippingAddressPhone());
            }

            orderGoodsMap.put("expirationTime", expirationTime);
            this.baseMapper.updateOrderBaseById1(orderGoodsMap);

            Map<String, Object> updateMap = new HashMap<>();
            updateMap.put("orderId", orderId);
            updateMap.put("orderRefundStatus", "0");
            updateMap.put("orderReturnStatus", "3");
            Integer torf = this.baseMapper.updateOrderBaseById(updateMap);
            if (1 != torf) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return false;
            }
            list.add("不同意");
            map.put("result", "不同意");
            map.put("status", "您提交的退货订单，商家未同意退货");
            // 更新子订单状态
            List<OrderBaseEntity> orderBaseEntities = orderBaseMapper.selectList(new LambdaQueryWrapper<OrderBaseEntity>()
                    .eq(OrderBaseEntity::getOriginalOrder, orderId));
            for (OrderBaseEntity entity : orderBaseEntities) {
                OrderReturnEntity orderReturnEntity = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>()
                        .eq(OrderReturnEntity::getOrderId, entity.getOrderId()));
                if (null != orderReturnEntity) {
                    // 更新订单基础信息为拒绝退货状态
                    orderGoodsMap.put("returnCode", orderReturnEntity.getReturnCode());
                    this.baseMapper.updateOrderBaseById1(orderGoodsMap);
                }
                updateMap.put("orderId", entity.getOrderId());
                orderBaseMapper.updateOrderBaseById(updateMap);
            }
        }

        map.put("list", list);
        baseServiceClient.sendMessageTemplate(map);

        Integer torf = this.baseMapper.updateOrderReturnById(orderGoodsMap);
        if (torf > 0) {
            // 更新子订单状态
            List<OrderBaseEntity> orderBaseEntities = orderBaseMapper.selectList(new LambdaQueryWrapper<OrderBaseEntity>()
                    .eq(OrderBaseEntity::getOriginalOrder, orderId));
            for (OrderBaseEntity entity : orderBaseEntities) {
                OrderReturnEntity orderReturnEntity = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>()
                        .eq(OrderReturnEntity::getOrderId, entity.getOrderId()));
                if (null != orderReturnEntity) {
                    // 更新订单基础信息为拒绝退货状态
                    orderGoodsMap.put("returnCode", orderReturnEntity.getReturnCode());
                    this.baseMapper.updateOrderReturnById(orderGoodsMap);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 处理商家接收信息的请求。
     * 该方法首先验证用户令牌以获取用户信息，然后检查用户是否有权限操作，
     * 接着将用户相关信息添加到请求参数中，最后调用其他方法处理请求并返回结果。
     *
     * @param map 请求参数的映射表，包含令牌信息。
     * @return 根据处理结果生成的响应结果对象。
     */
    @Override
    public ResponseResult retForSellerReceive(Map<String, String> map) throws TransactionException {
        if(null != map.get("token") ){
            // 从请求参数中获取令牌
            String token = map.get("token");
            // 使用令牌解析用户信息
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
            // 检查用户是否有店铺，如果没有则返回权限不足的错误
            if (null == userInfo.getShopId()) {
                return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
            }
            // 将用户的店铺ID、店铺类型和用户ID添加到请求参数中
            map.put("shopId", userInfo.getShopId());
            map.put("shopType", String.valueOf(userInfo.getShopType()));
            map.put("userId", String.valueOf(userInfo.getUserId()));
        }else{
            if(map.get("shopId") == null || map.get("shopType") == null || map.get("userId") == null ){
                return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
            }
        }
        // 调用其他方法处理业务逻辑，并根据结果返回相应的响应
        Boolean torf = retForSellerReceiveBoolean(map,false);
        if (!torf) {
            return ResponseResult.fail(2, "审核失败");
        }
        return ResponseResult.success(1, "审核成功");
    }
    /**
     * 京东 退货 换货 确认收货后 商品质检审核 通过 不通过
     * @param map
     * @return
     * @throws TransactionException
     */
    @Override
    public ResponseResult jingDongQualityInspectionReceive(Map<String, Object> map) throws TransactionException {
        Integer type = Integer.valueOf(map.get("type").toString());
        List<JingDongReturnOrderDTO> list = JsonUtils.string2Obj(JsonUtils.toString(map.get("orderIdList")), List.class, JingDongReturnOrderDTO.class);
        for (JingDongReturnOrderDTO jingDongReturnOrderDTO : list) {
            // 根据京东退单申请单号查询 商城订单号
            String orderId = baseMapper.queryOrderIdByJDReturnOrderId(jingDongReturnOrderDTO.getJdReturenOrderId());
            // 根据京东订单号查询 商城订单信息
            ReturnOrderVO returnOrderVO = baseMapper.queryReturnOrderByJDOrderId(orderId);

            if(returnOrderVO.getReturnMethodStatus() == CommonType.returnMethodType.RETURN.getCode()){
                //退货
                Map<String, String> paraMap = new HashMap<>();
                paraMap.put("returnCode",returnOrderVO.getReturnCode());
                paraMap.put("type",type.toString());
                if(type == 0){
                    paraMap.put("returnShopMessage","审核失败");
                }else{
                    paraMap.put("returnShopMessage","审核通过");
                }
                paraMap.put("shopId",returnOrderVO.getShopId());
                paraMap.put("shopType",returnOrderVO.getShopType().toString());
                paraMap.put("userId",returnOrderVO.getSellerUserId());

                retForSellerReceiveBoolean(paraMap,false);

            }else if(returnOrderVO.getReturnMethodStatus() == CommonType.returnMethodType.BARTER.getCode()){
                // 换货
                ReturnOrderDTO returnOrderDTO = new ReturnOrderDTO();
                returnOrderDTO.setShopId(Integer.valueOf(returnOrderVO.getShopId()));
                returnOrderDTO.setShopType(returnOrderVO.getShopType());
                returnOrderDTO.setUserName(returnOrderVO.getSellerUserName());
                returnOrderDTO.setUserId(returnOrderVO.getSellerUserId());
                returnOrderDTO.setReturnCode(returnOrderVO.getReturnCode());
                returnOrderDTO.setType(type);
                if(type == 0){
                    returnOrderDTO.setReturnShopMessage("审核失败");
                }else{
                    returnOrderDTO.setReturnShopMessage("审核通过");
                }
                orderBarterService.shopConfirmReceipt(returnOrderDTO);
            }
        }
        return ResponseResult.success(1, "审核成功");
    }

    @Override
    public ResponseResult selectshopordereturn(Map<String, Object> map) {
        String orderId = String.valueOf(map.get("orderId"));
        SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //查询退款详情
        ReturnOrderDetailVO returnOrderDetail = this.baseMapper.queryOrderReturnDetail(orderId);
        if (returnOrderDetail != null) {
            if (returnOrderDetail.getReturnVoucher() != null) {
                String returnVoucher = returnOrderDetail.getReturnVoucher();
                List<String> imgs = JSONArray.parseArray(returnVoucher, String.class);
                returnOrderDetail.setReturnVouchers(imgs);
                returnOrderDetail.setReturnVoucher(null);
            }
            // 线下付款查询支付凭证
            if (returnOrderDetail.getPaymentCode() == 2) {
                String returnPaymentVoucherStr = baseMapper.selectReturnPaymentVoucher(orderId);
                List<String> returnPaymentVoucher = JSON.parseArray(returnPaymentVoucherStr, String.class);
                returnOrderDetail.setReturnPaymentVoucherList(returnPaymentVoucher);
                OrderPaymentVocherEntity paymentVoucherEntity = orderPaymentVocherMapper.selectOne(new LambdaQueryWrapper<OrderPaymentVocherEntity>()
                        .eq(OrderPaymentVocherEntity::getOrderId, orderId));
                String paymentVoucher = paymentVoucherEntity.getOrderPaymentVoucherImgs();
                List<String> orderPaymentVoucher = JSON.parseArray(paymentVoucher, String.class);
                returnOrderDetail.setOrderPaymentVoucherList(orderPaymentVoucher);
            }
        }
        //查询售后商品
        List<GoodsDetailsListVO> orderGoods = orderGoodsMapper.queryGoodsList1(orderId);
        returnOrderDetail.setOrderGoodsDetail(orderGoods);
        //查询平台介入
        OrderReturnPlatformVO orderReturnPlatform = orderReturnPlatformMapper.selectOrderReturnPlatform(orderId);

        if (orderReturnPlatform != null) {
            String buyer_voucher = orderReturnPlatform.getBuyerVoucher();
            String seller_voucher = orderReturnPlatform.getSellerVoucher();
            if (seller_voucher != null) {
                List<String> imgs = JSONArray.parseArray(seller_voucher, String.class);
                orderReturnPlatform.setSellerVouchers(imgs);
                orderReturnPlatform.setSellerVoucher(null);
            }
            if (buyer_voucher != null) {
                List<String> imgs = JSONArray.parseArray(buyer_voucher, String.class);
                orderReturnPlatform.setBuyerVouchers(imgs);
                orderReturnPlatform.setBuyerVoucher(null);
            }
            orderReturnPlatform.setReturnOrderDetail(returnOrderDetail);
            return ResponseResult.success(orderReturnPlatform);
        }
        if (orderGoods != null && !orderGoods.isEmpty()) {
            return ResponseResult.success(returnOrderDetail);
        }
        return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
    }

    /**
     * 插入店铺干预信息，并更新订单退货状态。
     *
     * @param map 包含订单相关信息的Map对象。
     * @return 如果操作成功，返回成功的响应结果；如果操作失败，返回失败的响应结果。
     */
    @Override
    public ResponseResult insertShopIntervention(Map<String, Object> map) {
        // 从map中获取订单ID，并转换为字符串
        String orderId = String.valueOf(map.get("orderId"));
        // 创建订单退货平台信息对象
        OrderReturnPlatformVO orderReturnPlatform = new OrderReturnPlatformVO();
        // 获取当前时间作为申请时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        Date Application_time = calendar.getTime();
        // 设置处理日期为当前日期后的第四天
        calendar.add(Calendar.DATE, 4);
        Date calendarTime = calendar.getTime();
        // 设置订单ID
        orderReturnPlatform.setOrderId(orderId);
        // 从map中获取投诉图片列表，并转换为JSON字符串
        List<String> complaintsImages = (List<String>) map.get("sellerVoucher");
        String seller_voucher = JSON.toJSONString(complaintsImages);
        // 设置商家凭证
        orderReturnPlatform.setSellerVoucher(seller_voucher);
        // 从map中获取商家备注，并设置
        orderReturnPlatform.setSellerRemarks(String.valueOf(map.get("sellerRemarks")));
        // 设置过期时间
        orderReturnPlatform.setExpriationTime(calendarTime);
        // 设置干预状态为1，表示已干预
        orderReturnPlatform.setInterventionState(1);
        // 更新订单退货店铺信息
        Integer integer = this.baseMapper.updateOrderReturnShop(orderReturnPlatform);
        // 更新订单退货状态
        this.baseMapper.updateOrderReturnState(orderId, calendarTime);
        // 发送消息模板
        Map<String, Object> messageMap = new HashMap<>();
        List<String> messageList = new ArrayList<>();
        messageList.add(orderId);
        messageMap.put("list", messageList);
        messageMap.put("orderId", orderId);
        messageMap.put("templateId", CommonType.messageTemplate.CODE_M.getCode());
        baseServiceClient.sendMessageTemplate(messageMap);
        // 如果更新记录数大于0，表示操作成功
        if (integer > 0) {
            return ResponseResult.success();
        }
        // 否则，操作失败
        return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
    }

    /**
     * 处理退货订单的回调函数。
     *
     * @param map 包含退货订单相关信息的Map对象。
     * @return 返回处理结果的ResponseResult对象。
     */
    @Override
    public ResponseResult callReturnOrder(Map<String, Object> map) {
        // 初始化用于存放订单退货信息的Map对象
        Map<String, Object> orderGoodsMap = new HashMap<String, Object>();
        // 从传入的map中获取token并转换为字符串
        String token = String.valueOf(map.get("token"));
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        // 如果用户信息为空，则返回TOKEN_IS_INVALID错误码
        if (userInfo == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 将退货相关代码和状态放入orderGoodsMap
        orderGoodsMap.put("returnCode", map.get("returnCode"));
        orderGoodsMap.put("returnShopHandle", 2);
        orderGoodsMap.put("returnState", 2);
        orderGoodsMap.put("callStatus", 1);

        // 从传入的map中获取订单ID并转换为字符串
        String orderId = String.valueOf(map.get("orderId"));
        // 初始化用于更新订单信息的Map对象
        Map<String, Object> updateMap = new HashMap<String, Object>();
        // 设置订单ID和退货状态
        updateMap.put("orderId", orderId);
        updateMap.put("orderReturnStatus", "1");
        // 更新订单基础信息，返回影响行数
        Integer torf = this.baseMapper.updateOrderBaseById(updateMap);

        // 初始化用于更新订单商品信息的Map对象
        Map<String, Object> updateGoodsMap = new HashMap<String, Object>();
        // 设置订单ID和商品退货状态
        updateGoodsMap.put("orderId", orderId);
        updateGoodsMap.put("goodsReturnStatus", 1);
        // 更新订单商品信息
        this.baseMapper.updateOrderGoodsById2(updateGoodsMap);
        // 更新记录表的状态
        //更新record表状态
        Map<String, String> feignMap = new HashMap<>();
        feignMap.put("orderId", orderId);
        feignMap.put("recordStatus", "8");
        this.baseMapper.updateRecordRefuse(feignMap);

        // 如果更新订单基础信息的影响行数不为1，则回滚事务并返回失败信息
        if (1 != torf) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseResult.fail(2, "唤起失败");
        }

        // 更新订单退货信息，返回影响行数
        Integer torf1 = this.baseMapper.updateOrderReturnById(orderGoodsMap);
        // 如果更新订单退货信息成功，则返回成功结果
        if (1 == torf1) {
            return ResponseResult.success();
        }
        // 如果更新订单退货信息失败，则返回失败信息
        return ResponseResult.fail(2, "唤起失败");
    }

    @Override
    public ResponseResult shopReturnShipping(Map<String, String> paraMap) throws TransactionException {
        String token = String.valueOf(paraMap.get("token"));
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        if (userInfo == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        Map<String, Object> orderGoodsMap = new HashMap<String, Object>();
        Map<String, Object> orderBaseMap = new HashMap<String, Object>();
        // 商家确认收货该状态为9已退款
        String orderId = this.baseMapper.getOrderIdByReturnCode(paraMap.get("returnCode"));
        // 根据订单ID更新订单基础信息，设置订单为退货状态
        OrderBaseEntity baseEntity = orderBaseMapper.selectById(orderId);
        // 卖家填写物流信息
        orderGoodsMap.put("returnCode", paraMap.get("returnCode"));
        // 退货物流单号
        orderGoodsMap.put("returnShippingCode", paraMap.get("returnShippingCode"));
        // 联系电话
        orderGoodsMap.put("returnMobile", paraMap.get("returnMobile"));
        // 物流id
        orderGoodsMap.put("returnPostCode", paraMap.get("returnPostCode"));
        orderGoodsMap.put("returnShopHandle", 4);
        // 订单状态
        // 如果是线下付款订单，则设置订单状态为待平台退款
        if (baseEntity.getPaymentCode() == 2) {
            orderGoodsMap.put("returnState", "17");
        } else {
            orderGoodsMap.put("returnState", "4");
        }
        // 更改唤醒状态
        orderGoodsMap.put("callStatus", 0);
        String logistics = String.valueOf(System.currentTimeMillis());
        String fillLogisticsTime = TimeStampUtil.stampToDate(logistics);
        orderGoodsMap.put("fillLogisticsTime", fillLogisticsTime);

        orderGoodsMap.put("returnShopTime", new Date());
        orderGoodsMap.put("returnShopMessage", paraMap.get("returnShopMessage"));
        orderGoodsMap.put("returnFinishTime", new Date());

        //查询用户售后商品
        List<Integer> returnGoods = this.baseMapper.queryReturnGoods(orderId, userInfo.getShopId());

        orderBaseMap.put("orderId", orderId);
        if (baseEntity.getPaymentCode() == 2) {
            orderBaseMap.put("orderStatus", "28");
        } else {
            orderBaseMap.put("orderStatus", "9");
        }
        orderBaseMap.put("orderReturnStatus", "2");
        orderBaseMap.put("orderRefundStatus", "0");
        Integer torf = this.baseMapper.updateOrderBaseById(orderBaseMap);
        Map<String, Object> updateGoodsMap = new HashMap<String, Object>();

        updateGoodsMap.put("orderId", orderId);
        updateGoodsMap.put("goodsReturnStatus", 2);
        updateGoodsMap.put("goodsRefundStatus", 0);
        for (Integer returnGood : returnGoods) {
            updateGoodsMap.put("goodsId", returnGood);
            this.baseMapper.updateOrderGoodsById1(updateGoodsMap);
        }

        String uOrderId = this.baseMapper.queryPaymentNumber(orderId);
        OrderBaseEntity orderBaseEntity = orderBaseMapper.selectById(orderId);
        BigDecimal returnCash = this.baseMapper.queryReturnCash(orderId);

        List<Integer> orderTradeStatus = this.baseMapper.queryOrderRecordStatus(uOrderId);
        RefundParamDTO refundPara = new RefundParamDTO();
        boolean isRefundSuccess = false;
        if (torf == 1) {
            refundPara = this.baseMapper.queryRefundPara(orderId);
            refundPara.setOrderTradeStatus(orderTradeStatus);
            refundPara.setReturnCash(returnCash);
            isRefundSuccess = waitShipmentsRefundAndNotify(refundPara, orderId, returnCash);

        }
        if (!isRefundSuccess) {
            // 回滚
            GlobalTransactionContext.reload(RootContext.getXID()).rollback();
            return ResponseResult.fail(2, "填写物流失败");
        }

        if (1 != torf) {
            GlobalTransactionContext.reload(RootContext.getXID()).rollback();
            return ResponseResult.fail(2, "填写物流失败");
        }

        // 分销佣金订单处理
        commissionReturn(orderId, returnGoods);
        //判断当前订单是否全部退货
        Boolean huoBaoreturn = true;
        //平台退货佣金计算  会员折扣计算
        List<OrderPingtaiCommissionDTO> orderPingtaiCommissions = this.baseMapper.queryOrderPingTaiCommission(orderId);
        BigDecimal countPingTaiCommission = new BigDecimal(0);
        BigDecimal huiYuanZhekou = new BigDecimal(0);
        for (OrderPingtaiCommissionDTO orderPingtaiCommission : orderPingtaiCommissions) {
            // 总的平台抽佣/下单数量 * 退单数量
            System.out.println(new BigDecimal(orderPingtaiCommission.getGoodsNum()));

            //计算会员折扣
            if (!orderPingtaiCommission.getGoodsNum().equals(orderPingtaiCommission.getGoodsReturnNum())) {
                huoBaoreturn = false;
            }

        }
        //根据退款金额退多少佣金
        BigDecimal goodsAmount = orderBaseEntity.getOrderPaymentAmount().subtract(orderBaseEntity.getOrderShippingFee());
        if (goodsAmount.compareTo(returnCash) > 0) {
            List<OrderGoodsVO> returnGoodsList = orderGoodsMapper.queryOrderGoodsByOrderId(orderId);
            for (OrderGoodsVO orderGoodsVO : returnGoodsList) {
                if (Objects.equals(orderGoodsVO.getOrderGoodsNum(), orderGoodsVO.getOrderGoodsReturnNum())) {
                    countPingTaiCommission = countPingTaiCommission.add(orderGoodsVO.getOrderGoodsCommission());
                    huiYuanZhekou = huiYuanZhekou.add(orderGoodsVO.getGoodsMemberDiscount());
                } else {
                    BigDecimal oneGoodsCommission = orderGoodsVO.getOrderGoodsCommission().divide(BigDecimal.valueOf(orderGoodsVO.getOrderGoodsNum()), 2, RoundingMode.HALF_UP);
                    BigDecimal returnGoodsCommission = oneGoodsCommission.multiply(BigDecimal.valueOf(orderGoodsVO.getOrderGoodsReturnNum()));
                    countPingTaiCommission = countPingTaiCommission.add(returnGoodsCommission);

                    BigDecimal oneGoodsMemberDiscount = orderGoodsVO.getGoodsMemberDiscount().divide(BigDecimal.valueOf(orderGoodsVO.getOrderGoodsNum()), 2, RoundingMode.HALF_UP);
                    BigDecimal returnGoodsMemberDiscount = oneGoodsMemberDiscount.multiply(BigDecimal.valueOf(orderGoodsVO.getOrderGoodsReturnNum()));
                    huiYuanZhekou = huiYuanZhekou.add(returnGoodsMemberDiscount);
                }
            }
        } else {
            countPingTaiCommission = BigDecimal.valueOf(orderBaseEntity.getOrderCommissionFee());
            huiYuanZhekou = BigDecimal.valueOf(orderBaseEntity.getBaseMemberDiscount());
        }
        // 处理子单佣金
        updateSonOrderCommissionFee(orderId);
        //根据订单号查询支付单号 因为有可能多个订单公用一个支付单号 如果是多个订单共用用一个支付单号 要判断除当前退单外的其他订单是否是全退
        String paymentNumber = this.baseMapper.queryOrderPaymentNumber(orderId);
        //如果多个订单公用一个支付单号 根据当前订单号 和 支付单号 查询是否有其他订单没有全退货
        Integer notOrderNum = this.baseMapper.queryNotRetuenOrder(orderId, paymentNumber);
        if (0 <= notOrderNum) {
            //说明有其他订单没有完全退货 不退红包 优惠券
            huoBaoreturn = false;
        }
        //退货红包 优惠券
        if (huoBaoreturn) {
            this.baseMapper.updateHongbaoStart(orderId);
            this.baseMapper.updateYouHuiquanStart(orderId);
        }
        this.baseMapper.updateOrderBasePingTaiCommission(orderId, countPingTaiCommission);
        //更新退款表 平台退还佣金 退还会员折扣
        this.baseMapper.updateOrderReturnPingTaiCommission(orderId, countPingTaiCommission, huiYuanZhekou);
        // 代发货订单 经销商和供应商佣金处理
        Integer i = 1;
        BigDecimal reutrnOrderGoodsAmount = new BigDecimal(0);
        for (Integer returnGood : returnGoods) {
            Map<String, Object> updateGoodsReturnCommissionMap = new HashMap<>();
            updateGoodsReturnCommissionMap.put("orderId", orderId);
            OrderGoodsEntity orderGoodsEntity = orderGoodsMapper.selectOne(new LambdaQueryWrapper<OrderGoodsEntity>()
                    .eq(OrderGoodsEntity::getOrderId, orderId)
                    .eq(OrderGoodsEntity::getGoodsId, returnGood));
            if (orderBaseEntity.getDealerCommission() != null && orderBaseEntity.getDealerCommission() > 0){
                if (orderGoodsEntity.getDealerCommission() != null && orderGoodsEntity.getDealerCommission() > 0){
                    if(orderGoodsEntity.getOrderGoodsReturnnum().equals(orderGoodsEntity.getOrderGoodsNum())){
                        updateGoodsReturnCommissionMap.put("dealerReturnCommission", orderGoodsEntity.getDealerCommission());
                    }else{
                        BigDecimal dealerReturnCommission = BigDecimal.valueOf(orderGoodsEntity.getDealerCommission() * Integer.valueOf(orderGoodsEntity.getOrderGoodsReturnnum())
                                / Integer.parseInt(orderGoodsEntity.getOrderGoodsNum())).setScale(2, RoundingMode.HALF_UP);
                        updateGoodsReturnCommissionMap.put("dealerReturnCommission", dealerReturnCommission);
                    }
                }
            }
            BigDecimal orderGoodsAmount = orderGoodsEntity.getOrderGoodsPaymentAmount();
            if(i.equals(returnGoods.size())){
                updateGoodsReturnCommissionMap.put("goodsReturnPrice", returnCash.subtract(reutrnOrderGoodsAmount));
            }else{
                BigDecimal lv = orderGoodsAmount.divide(returnCash, 2, RoundingMode.HALF_UP);
                BigDecimal amount = (orderGoodsAmount).multiply(lv).setScale(2, RoundingMode.HALF_UP);
                reutrnOrderGoodsAmount = reutrnOrderGoodsAmount.add(amount);
                updateGoodsReturnCommissionMap.put("goodsReturnPrice", amount);
                i++;
            }
            updateGoodsReturnCommissionMap.put("goodsId", returnGood);
            this.baseMapper.updateOrderGoodsById1(updateGoodsReturnCommissionMap);
        }
        // 仓库id
        Integer storageId = orderShipmentMapper.queryStorageId(orderId);
        List<StorageInventoryFlowGoodsDTO> storageGoodsList = new ArrayList<>();
        Map<String, Object> goodsNumMap = new HashMap<>();
        List<Map<String, Object>> goodsNum = this.baseMapper.queryBuyNumByorderId(orderId);
        goodsNumMap.put("goodsNum", goodsNum);
        //退款更新商品销量
        updateGoodsSalenum(goodsNumMap);
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
        Integer inventoryFlowType = CommonType.inventoryFlowType.PUT_STORAGE.getCode();
        Integer inventoryFlowTypeDetail = CommonType.inventoryFlowTypeDetail.RETURN_TO_INVENTORY_AND_STORAGE.getCode();
        setStorageInventoryFlow(storageId, orderId, inventoryFlowType, inventoryFlowTypeDetail, storageGoodsList,
                goodServiceClient, baseServiceClient, null, token);
        //根据orderid 查询应该回退的积分和userid
        Map<String, Object> map = this.baseMapper.queryAddpointsLogByOrderId(orderId);
        //ZZZ 新增积分按比例扣除逻辑
        //订单实付金额
        BigDecimal order_payment_amount = this.baseMapper.queryOrderAmount(orderId);
        //订单商品总价（不含运费）
        BigDecimal order_goods_amount = this.baseMapper.queryOrderGoodsAmount(orderId);
        //退货商品总价(不含运费）
        BigDecimal returnAmount = BigDecimal.ZERO;
        //退货积分系数
        BigDecimal coef = BigDecimal.ZERO;
        List<OrderReturnPointsDTO> orderReturnPointsList = this.baseMapper.queryOrderReturnInfo(orderId);
        if (null != orderReturnPointsList && orderReturnPointsList.size() > 0) {
            for (OrderReturnPointsDTO orp : orderReturnPointsList) {
                BigDecimal goodsPrice = null != orp ? orp.getGoodsPrice() : BigDecimal.ZERO;
                Integer goodsReturnNum = null != orp ? orp.getOrderGoodsReturnnum() : 0;
                returnAmount = returnAmount.add(goodsPrice.multiply(new BigDecimal(goodsReturnNum)));
            }
        }
        //实际退款金额(用于计算退还积分)
        returnAmount = returnAmount.multiply(order_payment_amount).divide(order_goods_amount, RoundingMode.HALF_UP);
        //实际退款积分ZZZ
        BigDecimal tmp = BigDecimal.ZERO;
        if (null != map && null != map.get("points_log_points")) {
            tmp = returnAmount.multiply(new BigDecimal((Integer) map.get("points_log_points")).divide(order_payment_amount, RoundingMode.HALF_UP));
            Integer tmpInt = tmp.intValue();
            map.put("points_log_points", tmpInt);
            if (baseEntity.getPaymentCode() != 2) {
                //插入我的余额明细表
                Map<String, Object> assetDetailMap = new HashMap<>();
                assetDetailMap.put("orderId", orderId);
                assetDetailMap.put("detailType", 2);
                assetDetailMap.put("assetType", 1);
                assetDetailMap.put("assetAmount", returnCash);
                AssetDirectionAndPayTypeInfoDTO assetInfo = this.baseMapper.getAssetDirectionAndPayTypeInfo(orderId);
                if (null != assetInfo) {
                    assetDetailMap.put("assetDirection", assetInfo.getAssetDirection());
                    assetDetailMap.put("payType", assetInfo.getPayType());
                    assetDetailMap.put("userId", assetInfo.getUserId());
                    this.baseMapper.insertDetail(assetDetailMap);
                }

                //修改会员积分和会员等级
                MemberMessageListIntegralDTO updateMemberMessageListIntegral = new MemberMessageListIntegralDTO();
                ArrayList<String> list = new ArrayList<>();
                list.add((String) map.get("user_id"));
                updateMemberMessageListIntegral.setUserIdList(list);
                updateMemberMessageListIntegral.setIntegral((Integer) map.get("points_log_points"));
                //1是增加积分 0 是扣除积分
                updateMemberMessageListIntegral.setType(0);
                feignUpdateMemberIntegral(updateMemberMessageListIntegral);
                //添加积分修改记录
                UserPointLogParamDTO userPointLog = new UserPointLogParamDTO();
                userPointLog.setPointsLogType(2);
                userPointLog.setClassId(9);
                userPointLog.setPointsLogTime(new Date());
                userPointLog.setPointsLogDesc("商品退货");
                userPointLog.setPointsLogFlag("reg");
                userPointLog.setUserId((String) map.get("user_id"));
                userPointLog.setPointsLogPoints((Integer) map.get("points_log_points"));
                userPointLog.setPointsLogOperate("商品退货");
                userPointLog.setOrderId(orderId);
                this.baseMapper.feginAddpointsLog(userPointLog);
            }
            //罚扣质保金
            charging(orderId, returnGoods, userInfo.getUserId(), Integer.valueOf(userInfo.getShopId()));
        }


        // 发送消息模板
        String returnGoodsName = this.baseMapper.queryReturnGoodsNameByOrderId(orderId);
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("orderId", orderId);
        messageMap.put("goodsName", returnGoodsName);
        List<String> messageList = new ArrayList<>();
        messageList.add(orderId);
        messageMap.put("userId", baseEntity.getBuyerUserId());
        messageMap.put("templateId", CommonType.messageTemplate.CODE_E.getCode());
        messageList.add("已");
        messageMap.put("list", messageList);
        messageMap.put("result", "已");
        messageMap.put("status", "您提交的退货订单，商家已退货");
        baseServiceClient.sendMessageTemplate(messageMap);

        Integer torf1 = this.baseMapper.updateOrderReturnById(orderGoodsMap);
        if (1 == torf1) {
            List<OrderBaseEntity> originalOrderBaseList = orderBaseMapper.selectList(new LambdaQueryWrapper<OrderBaseEntity>()
                    .eq(OrderBaseEntity::getOriginalOrder, orderId));

            if (!originalOrderBaseList.isEmpty()) {
                for (OrderBaseEntity entity : originalOrderBaseList) {
                    OrderReturnEntity orderReturnEntity = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>()
                            .eq(OrderReturnEntity::getOrderId, entity.getOrderId()));
                    if (null != orderReturnEntity) {
                        paraMap.put("returnCode", orderReturnEntity.getReturnCode());
                        shopReturnShipping(paraMap);
                    }
                }
            }

            return ResponseResult.success(1, "填写物流成功");
        }
        // 回滚
        GlobalTransactionContext.reload(RootContext.getXID()).rollback();
        return ResponseResult.fail(2, "填写物流失败");
    }

    /**
     * 平台查询线下支付售后订单
     */
    @Override
    public ResponseResult queryWaitingForPlatformRefund(Map<String, Object> paraMap) {
        // 计算当前页的起始位置
        Integer currentPage = ((Integer) (paraMap.get("page")) - 1)
                * (Integer) paraMap.get("limit");
        // 更新paraMap中的page参数为当前页的起始位置
        paraMap.put("page", currentPage);
        paraMap.put("returnType", "0");
        paraMap.put("returnState", "17");
        // 根据更新后的参数查询退款订单详情列表
        IPage<ReturnOrderDetailVO> returnOrderPage = this.baseMapper.listReturnRefund(new Query<ReturnOrderDetailVO>().getPage(paraMap),paraMap);
        List<ReturnOrderDetailVO> returnOrderDetails = returnOrderPage.getRecords();
        // 如果查询结果不为空，对每条订单详情进行脱敏处理
        if (!returnOrderDetails.isEmpty()) {
            for (ReturnOrderDetailVO detailVO : returnOrderDetails) {
                BeanUtils.copyProperties(DesensitizationUtil.Desensitization(detailVO, ReturnOrderDetailVO.class), detailVO);
            }
        }
        // 构建返回结果映射，包含退款订单列表和总记录数
        Map<String, Object> returnMap = new HashMap<>();
        returnMap.put("returnOrderDetails", returnOrderDetails);
        returnMap.put("totalCount", returnOrderPage.getTotal());

        // 返回查询成功的响应结果，包含退款订单列表和总记录数
        return ResponseResult.success(returnMap);
    }

    /**
     * 平台导出线下支付售后订单
     */
    @Override
    public ResponseResult queryWaitingForPlatformRefundExp(Map<String, String> paraMap) {
        paraMap.put("returnType", "0");
        paraMap.put("returnState", "17");
        // 格式化当前时间作为文件名的一部分，用于区分不同的导出文件
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String finishTime = dateformat.format(new Date());

        // 根据查询参数查询退款订单详情信息
        List<ReturnOrderDetailVO> conResult = this.baseMapper.listReturnRefundExcel(paraMap);

        // 如果查询结果超过10000条，视为导出失败，防止数据量过大导致导出效率低下或内存溢出
        if (conResult.size() > 10000) {
            return ResponseResult.fail(BizCodeEnume.ORDER_EXP_FAIL.getCode()).setMessage(BizCodeEnume.ORDER_EXP_FAIL.getMsg());
        }

        // 定义导出的Excel表格的列标题
        String[] headers = {"买家账号", "退单编号", "订单编号", "售后状态", "退单金额（元）", "买家申请原因", "付款时间", "申请时间",
                "商家处理备注", "商家处理时间", "店铺名称", "操作人", "操作时间"};

        // 初始化数据集，用于存储格式化后的查询结果
        List<Map<String, String>> dataset = Lists.newArrayList();

        // 遍历查询结果，对每条数据进行格式化处理，并添加到数据集中
        for (ReturnOrderDetailVO res : conResult) {
            // 对敏感信息进行脱敏处理
            BeanUtils.copyProperties(DesensitizationUtil.Desensitization(res, ReturnOrderDetailVO.class), res);


            // 创建一个映射，用于存储当前数据行的各个字段值
            Map<String, String> map = Maps.newHashMap();

            // 将当前数据行的各个字段值添加到映射中，为Excel表格的一行数据
            map.put("买家账号", res.getBuyerUserAccount());
            map.put("退单编号", res.getReturnCode());
            map.put("订单编号", res.getOrderId());
            map.put("售后状态", "待平台退款");
            map.put("退单金额（元）", res.getReturnCash() + "");
            map.put("买家申请原因", res.getReturnReason());
            map.put("付款时间", dateformat.format(res.getPaymentTime()));
            map.put("申请时间", dateformat.format(res.getReturnAddTime()));
            map.put("商家处理备注", res.getReturnShopMessage());
            map.put("商家处理时间", dateformat.format(res.getReturnShopTime()));
            map.put("店铺名称", res.getShopName());
            map.put("操作人", res.getOrderOperator());
            map.put("操作时间", res.getOrderOperatorTime());
            dataset.add(map);
        }

        // 根据当前时间生成唯一的文件名，用于导出的CSV文件
        // excel文件名
        String fileName = "refund" + finishTime + ".csv";

        // 将数据集包装成一个Excel表格，存储到mapExcel中，用于后续的导出
        // excel文件名
        Map<String, List<Map<String, String>>> mapExcel = Maps.newHashMap();
        mapExcel.put(fileName, dataset);

        // 调用Excel工具类，根据数据集和列标题生成Excel文件，并返回文件的下载链接
        String url = ExcelsUtil.createExcel(headers, mapExcel, new int[]{0});

        // 返回导出结果，包含生成的Excel文件的下载链接
        return ResponseResult.success(url);
    }

    /**
     * 线下支付订单 平台上传退款支付凭证
     */
    @Override
    public ResponseResult waitingForPlatformRefund(WaitPlatformRefundDTO dto) {
        String orderId = dto.getOrderId();
        String paymentVoucher = JSON.toJSONString(dto.getReturnPaymentVoucherList());
        if (!StringUtils.isEmpty(paymentVoucher)) {
            OrderBaseEntity orderBaseEntity = orderBaseMapper.selectOne(new LambdaQueryWrapper<OrderBaseEntity>()
                    .eq(OrderBaseEntity::getOrderId, orderId));
            OrderReturnEntity returnEntity = baseMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>()
                    .eq(OrderReturnEntity::getOrderId, orderId));
            if (returnEntity.getReturnState() != 17) {
                return ResponseResult.fail(BizCodeEnume.ORDER_AFTER_SALES_NOT_WAIT);
            }
            //插入我的余额明细表
            Map<String, Object> assetDetailMap = new HashMap<>();
            assetDetailMap.put("orderId", orderId);
            assetDetailMap.put("detailType", 2);
            assetDetailMap.put("assetType", 1);
            assetDetailMap.put("assetAmount", returnEntity.getReturnCash());
            AssetDirectionAndPayTypeInfoDTO assetInfo = this.baseMapper.getAssetDirectionAndPayTypeInfo(orderId);
            if (null != assetInfo) {
                assetDetailMap.put("assetDirection", assetInfo.getAssetDirection());
                assetDetailMap.put("payType", assetInfo.getPayType());
                assetDetailMap.put("userId", assetInfo.getUserId());
                this.baseMapper.insertDetail(assetDetailMap);
            }
            //扣减积分
            //修改会员积分和会员等级
            //根据orderid 查询应该回退的积分和userid
            Map<String, Object> tmpMap = baseMapper.queryAddpointsLogByOrderId(orderId);
            if (null != tmpMap) {
                MemberMessageListIntegralDTO updateMemberMessageListIntegral = new MemberMessageListIntegralDTO();
                ArrayList<String> list = new ArrayList<>();
                list.add((String) tmpMap.get("user_id"));
                BigDecimal jifen = BigDecimal.valueOf(orderBaseEntity.getOrderRefundAmount()).multiply(new BigDecimal((int) tmpMap.get("points_log_points")));
                long l = jifen.setScale(0, RoundingMode.HALF_UP).longValue();
                updateMemberMessageListIntegral.setUserIdList(list);
                updateMemberMessageListIntegral.setIntegral((int) l);
                //1是增加积分 0 是扣除积分
                updateMemberMessageListIntegral.setType(0);
                feignUpdateMemberIntegral(updateMemberMessageListIntegral);
                //添加积分修改记录
                UserPointLogParamDTO userPointLog = new UserPointLogParamDTO();
                userPointLog.setPointsLogType(2);
                userPointLog.setClassId(9);
                userPointLog.setPointsLogTime(new Date());
                userPointLog.setPointsLogDesc("商品退款");
                userPointLog.setPointsLogFlag("reg");
                userPointLog.setUserId(tmpMap.get("user_id").toString());
                userPointLog.setPointsLogPoints((int) l);
                userPointLog.setPointsLogOperate("商品退款");
                userPointLog.setOrderId(orderId);
                this.baseMapper.feginAddpointsLog(userPointLog);
            }
            // 更新退款状态和退款凭证 当为退款退货时，退款状态为商家确认收货，否则为商家同意退款。
            Integer returnState = returnEntity.getReturnMethodStatus() == 3 ? 4 : 2;
            baseMapper.update(null, new LambdaUpdateWrapper<OrderReturnEntity>()
                    .set(OrderReturnEntity::getReturnState, returnState)
                    .set(OrderReturnEntity::getReturnPaymentVoucher, paymentVoucher)
                    .eq(OrderReturnEntity::getOrderId, orderId));
            // 更新订单状态为退款完成
            orderBaseMapper.update(null, new LambdaUpdateWrapper<OrderBaseEntity>()
                    .set(OrderBaseEntity::getOrderStatus, 9)
                    .eq(OrderBaseEntity::getOrderId, orderId));

            // 更新子单状态
            List<OrderBaseEntity> originalOrderBaseList = orderBaseMapper.selectList(new LambdaQueryWrapper<OrderBaseEntity>()
                    .eq(OrderBaseEntity::getOriginalOrder, orderId));

            if (!originalOrderBaseList.isEmpty()) {
                for (OrderBaseEntity entity : originalOrderBaseList) {
                    OrderReturnEntity orderReturnEntity = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>()
                            .eq(OrderReturnEntity::getOrderId, entity.getOrderId()));
                    if (null != orderReturnEntity) {
                        dto.setOrderId(entity.getOrderId());
                        waitingForPlatformRefund(dto);
                    }
                }
            }
            return ResponseResult.success();
        }
        return ResponseResult.fail(BizCodeEnume.PAYMENT_VOUCHER_NULL);
    }

    @Override
    public Boolean updateTimeOutReturn() {
        Map<String, Object> orderGoodsMap = new HashMap<String, Object>();
        Map<String, Object> orderBaseMap = new HashMap<String, Object>();
        Map<String, Object> assetDetailMap = new HashMap<>();
        ArrayList<Object> arrayList = new ArrayList<>();
        List<OrderReturnEntity> orderIds = this.baseMapper.queryTimeOutReturn();
        if (orderIds != null && orderIds.size() > 0) {
            for (OrderReturnEntity map1 : orderIds) {
                try {
                    Boolean is = true;
                    //查询店铺类型
                    Integer selectshoptype = this.baseMapper.selectshoptype(map1.getShopId());

                    String orderId = map1.getOrderId();
                    String returnCode = map1.getReturnCode();
                    OrderBaseEntity orderBaseEntity = orderBaseMapper.selectById(orderId);
                    OrderReturnEntity orderReturnEntity = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>().eq(OrderReturnEntity::getOrderId, orderId));
                    OrderBaseEntity baseEntity = orderBaseMapper.selectById(orderId);
                    Integer orderStatus = orderBaseEntity.getOrderStatus();
                    Integer virtualGoods = 1;
                    //获取退货数量
                    Map<String, Object> goodsNumMap = new HashMap<>();
                    List<Map<String, Object>> goodsNum = this.baseMapper.queryBuyNumByorderId(orderId);
                    goodsNumMap.put("goodsNum", goodsNum);
                    //判断是否是虚拟订单 且没有被核销
                    OrderBaseEntity map = orderBaseMapper.selectOne(new LambdaQueryWrapper<OrderBaseEntity>()
                            .eq(OrderBaseEntity::getOrderId, orderId)
                            .eq(OrderBaseEntity::getVirtualGoods, 0)
                            .in(OrderBaseEntity::getIsCancel, 1, 2));
                    if (null != map) {
                        virtualGoods = 0;
                        //如果该虚拟订单已过期 且过期不退款直接返回false
                        Integer result = orderBaseMapper.queryisVirtualOrderByTime(orderId);
                        if (0 != result) {
                            updateVirtualGoodsOrder(orderId,baseEntity.getShopId(),orderReturnEntity.getReturnCode());
                            return false;
                        }
                    }
                    // 线下付款设置退款状态为待平台退款状态
                    if (orderBaseEntity.getPaymentCode() == 2) {
                        orderGoodsMap.put("returnState", 17);
                    } else {
                        orderGoodsMap.put("returnState", 2);
                    }
                    BigDecimal returnCash = new BigDecimal(0);
                    //查询用户售后商品
                    List<Integer> returnGoods = this.baseMapper.queryReturnGoods(orderId, null);
                    //用户收到货物退款
                    if (orderStatus == 6) {
                        orderGoodsMap.put("returnCode", returnCode);
                        // orderGoodsMap.put("orderId", paraMap.get("orderId"));
                        orderGoodsMap.put("returnShopHandle", 2);


                        //	 1.2修改商家同意订单状态为 6
                        orderBaseMap.put("orderId", orderId);
                        //9.已退款
                        orderBaseMap.put("orderStatus", "9");
                        //2是退款完成
                        orderBaseMap.put("orderRefundStatus", "2");
                        //2是退款完成
                        orderBaseMap.put("orderReturnStatus", "0");
                        Integer torf = orderBaseMapper.updateOrderBaseById(orderBaseMap);

                        for (Integer returnGood : returnGoods) {
                            orderGoodsMapper.update(null, new LambdaUpdateWrapper<OrderGoodsEntity>()
                                    .eq(OrderGoodsEntity::getOrderId, orderId)
                                    .eq(OrderGoodsEntity::getGoodsId, returnGood)
                                    .set(OrderGoodsEntity::getGoodsRefundStatus, CommonType.orderRefundStatus.REFUND_COMPLETED.getCode())
                                    .set(OrderGoodsEntity::getGoodsReturnStatus, CommonType.orderReturnStatus.NO_RETURNS.getCode()));
                        }
                        String uOrderId = orderBaseEntity.getPaymentNumber();
                        returnCash = orderReturnEntity.getReturnCash();

                        List<Integer> orderTradeStatus = this.baseMapper.queryOrderRecordStatus(uOrderId);
                        ;
                        RefundParamDTO refundPara = new RefundParamDTO();
                        boolean isRefundSuccess = false;
                        if (torf == 1) {
                            refundPara = this.baseMapper.queryRefundPara(orderId);
                            refundPara.setOrderTradeStatus(orderTradeStatus);
                            refundPara.setReturnCash(returnCash);
                            refundPara.setShopId(map1.getShopId());
                            refundPara.setShopType(selectshoptype);
                            isRefundSuccess = waitShipmentsRefundAndNotify(refundPara, orderId, returnCash);
                            //退款更新商品销量
                            updateGoodsSalenum(goodsNumMap);
                        }
                        if (!isRefundSuccess) {
                            // 回滚
                            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                            return false;
                        }
                        if (1 != torf) {
                            // 回滚
                            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                            return false;
                        }
                        //未发货退款  虚拟商品订单可以进来
                    } else if (orderStatus == 3 || virtualGoods == 0) {
                        orderGoodsMap.put("returnCode", returnCode);
                        // orderGoodsMap.put("orderId", paraMap.get("orderId"));
                        orderGoodsMap.put("returnShopHandle", 2);
                        orderGoodsMap.put("returnFinishTime", DateUtils.getNowDate());

                        //	 1.2修改商家同意订单状态为 6
                        orderBaseMap.put("orderId", orderId);
                        //已退款
                        orderBaseMap.put("orderStatus", "9");
                        orderBaseMap.put("orderRefundStatus", "2");
                        orderBaseMap.put("orderReturnStatus", "0");
                        orderBaseMap.put("orderFinishedTime", DateUtils.getNowDate());
                        Integer torf = this.baseMapper.updateOrderBaseById(orderBaseMap);
                        Map<String, Object> updateGoodsMap = new HashMap<String, Object>();
                        updateGoodsMap.put("orderId", orderId);
                        updateGoodsMap.put("goodsRefundStatus", 2);
                        updateGoodsMap.put("goodsReturnStatus", 0);
                        this.baseMapper.updateOrderGoodsById(updateGoodsMap);

                        String uOrderId = this.baseMapper.queryPaymentNumber(orderId);
                        returnCash = this.baseMapper.queryReturnCash(orderId);

                        List<Integer> orderTradeStatus = this.baseMapper.queryOrderRecordStatus(uOrderId);
                        RefundParamDTO refundPara = new RefundParamDTO();
                        boolean isRefundSuccess = false;
                        if (torf == 1) {
                            refundPara = this.baseMapper.queryRefundPara(orderId);
                            refundPara.setOrderTradeStatus(orderTradeStatus);
                            refundPara.setReturnCash(returnCash);
                            refundPara.setShopId(map1.getShopId());
                            refundPara.setShopType(selectshoptype);
                            updateGoodsSalenum(goodsNumMap);
                            if (refundPara.getPayCode() != 12) {
                                isRefundSuccess = waitShipmentsRefundAndNotify(refundPara, orderId, returnCash);
                            } else {
                                isRefundSuccess = true;
                                notifyDistributionRefund(orderId, returnCash);
                            }
                        }
                        if (!isRefundSuccess) {
                            // 回滚
                            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                            return false;
                        }
                        if (1 != torf) {
                            // 回滚
                            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                            return false;
                        }
                        //退款成功后 修改库存
                        //获取goodsid commonid 购买数量
                        List<WapGoodsSaleSumUpdateVO> wapGood = this.baseMapper.queryGoodsXinXi(orderId);
                        //封装数据
                        List<WapGoodsSaleSumDTO> wapGoodData = new ArrayList<>();
                        if (!CollectionUtils.isEmpty(wapGood)) {
                            wapGoodData = wapGood.stream().map(entity -> {
                                WapGoodsSaleSumDTO catResult = new WapGoodsSaleSumDTO();

                                BeanUtils.copyProperties(entity, catResult);
                                return catResult;
                            }).collect(Collectors.toList());
                        }
                        goodServiceClient.updateGoodsStock(wapGoodData);
                        for (WapGoodsSaleSumUpdateVO goods : wapGood) {
                            //遍历集合减去销量
                            this.baseMapper.lessgoodsBySales(goods);
                        }
                        //查询红包和代金卷
                        WapGoodsSaleSumUpdateVO wap = this.baseMapper.queryRedByEnvelope(orderId);
                        if (null != wap) {
                            if (null != wap.getVoucherCode()) {
                                //退还代金卷
                                this.baseMapper.updateCouponStuate(wap.getVoucherCode());
                            }
                            if (null != wap.getRedpacketCode()) {
                                this.baseMapper.updateRedPageStuate(wap.getRedpacketCode());
                            }
                        }
                    } else {
                        is = false;
                    }
                    if (is) {
                        // 售后分销佣金处理
                        commissionReturn(orderId, returnGoods);
                        // 代发货订单 经销商和供应商佣金处理
                        Integer i = 1;
                        BigDecimal reutrnOrderGoodsAmount = new BigDecimal(0);
                        for (Integer returnGood : returnGoods) {
                            Map<String, Object> updateGoodsReturnCommissionMap = new HashMap<>();
                            updateGoodsReturnCommissionMap.put("orderId", orderId);
                            OrderGoodsEntity orderGoodsEntity = orderGoodsMapper.selectOne(new LambdaQueryWrapper<OrderGoodsEntity>()
                                    .eq(OrderGoodsEntity::getOrderId, orderId)
                                    .eq(OrderGoodsEntity::getGoodsId, returnGood));
                            if (orderBaseEntity.getDealerCommission() != null && orderBaseEntity.getDealerCommission() > 0){
                                if (orderGoodsEntity.getDealerCommission() != null && orderGoodsEntity.getDealerCommission() > 0){
                                    if(orderGoodsEntity.getOrderGoodsReturnnum().equals(orderGoodsEntity.getOrderGoodsNum())){
                                        updateGoodsReturnCommissionMap.put("dealerReturnCommission", orderGoodsEntity.getDealerCommission());
                                    }else{
                                        BigDecimal dealerReturnCommission = BigDecimal.valueOf(orderGoodsEntity.getDealerCommission() * Integer.valueOf(orderGoodsEntity.getOrderGoodsReturnnum())
                                                / Integer.parseInt(orderGoodsEntity.getOrderGoodsNum())).setScale(2, RoundingMode.HALF_UP);
                                        updateGoodsReturnCommissionMap.put("dealerReturnCommission", dealerReturnCommission);
                                    }
                                }
                            }
                            BigDecimal orderGoodsAmount = orderGoodsEntity.getOrderGoodsPaymentAmount();
                            if(i.equals(returnGoods.size())){
                                updateGoodsReturnCommissionMap.put("goodsReturnPrice", returnCash.subtract(reutrnOrderGoodsAmount));
                            }else{
                                BigDecimal lv = orderGoodsAmount.divide(returnCash, 2, RoundingMode.HALF_UP);
                                BigDecimal amount = (orderGoodsAmount).multiply(lv).setScale(2, RoundingMode.HALF_UP);
                                reutrnOrderGoodsAmount = reutrnOrderGoodsAmount.add(amount);
                                updateGoodsReturnCommissionMap.put("goodsReturnPrice", amount);
                                i++;
                            }
                            updateGoodsReturnCommissionMap.put("goodsId", returnGood);
                            this.baseMapper.updateOrderGoodsById1(updateGoodsReturnCommissionMap);
                        }
                        // 由于退款 是全订单退款 所以退红包
                        this.baseMapper.updateHongbaoStart(orderId);
                        //插入我的余额明细表
                        assetDetailMap.put("orderId", orderId);
                        assetDetailMap.put("detailType", 2);
                        assetDetailMap.put("assetType", 1);
                        assetDetailMap.put("assetAmount", returnCash);
                        AssetDirectionAndPayTypeInfoDTO assetInfo = this.baseMapper.getAssetDirectionAndPayTypeInfo(orderId);
                        if (null != assetInfo) {
                            assetDetailMap.put("assetDirection", assetInfo.getAssetDirection());
                            assetDetailMap.put("payType", assetInfo.getPayType());
                            assetDetailMap.put("userId", assetInfo.getUserId());
                            this.baseMapper.insertDetail(assetDetailMap);
                        //判断是否是虚拟订单 且没有被核销
                        }
                        if (null != map) {
                            virtualGoods = 0;
                            //是否核销 0是 1否 2已过期
                            Integer isCancel = map.getIsCancel();
                            if (null != isCancel && 0 != isCancel) {
                                //如果该虚拟订单已过期 且过期不退款直接返回false
                                Integer result = this.baseMapper.queryisVirtualOrderByTime(orderId);
                                if (0 != result) {
                                    return false;
                                }
                                boolean returnFlag = false;
                                //未核销且在有效期内
                                if (1 == isCancel) {
                                    Integer result1 = this.baseMapper.queryisVirtualOrderByTime1(orderId);
                                    if (null != result1 && result1 > 0) {
                                        returnFlag = true;
                                    }
                                    //过期且可过期退款
                                } else if (2 == isCancel) {
                                    Integer result2 = this.baseMapper.queryisVirtualOrderByTime2(orderId);
                                    if (null != result2 && result2 > 0) {
                                        returnFlag = true;
                                        //退还虚拟订单佣金
                                        //如果该虚拟订单已过期 且过期允许退款就扣除积分
                                        //扣减积分
                                        //修改会员积分和会员等级
                                        //根据orderid 查询应该回退的积分和userid
                                        Map<String, Object> tmpMap = this.baseMapper.queryAddpointsLogByOrderId(orderId);
                                        if (null != tmpMap) {
                                            MemberMessageListIntegralDTO updateMemberMessageListIntegral = new MemberMessageListIntegralDTO();
                                            ArrayList<String> list = new ArrayList<>();
                                            list.add((String) tmpMap.get("user_id"));
                                            updateMemberMessageListIntegral.setUserIdList(list);
                                            updateMemberMessageListIntegral.setIntegral((Integer) tmpMap.get("points_log_points"));
                                            //1是增加积分 0 是扣除积分
                                            updateMemberMessageListIntegral.setType(0);
                                            feignUpdateMemberIntegral(updateMemberMessageListIntegral);
                                            //添加积分修改记录
                                            UserPointLogParamDTO userPointLog = new UserPointLogParamDTO();
                                            userPointLog.setPointsLogType(2);
                                            userPointLog.setClassId(9);
                                            userPointLog.setPointsLogTime(new Date());
                                            userPointLog.setPointsLogDesc("商品退款");
                                            userPointLog.setPointsLogFlag("reg");
                                            userPointLog.setUserId(tmpMap.get("user_id").toString());
                                            userPointLog.setPointsLogPoints((Integer) tmpMap.get("points_log_points"));
                                            userPointLog.setPointsLogOperate("商品退款");
                                            userPointLog.setOrderId(orderId);
                                            this.baseMapper.feginAddpointsLog(userPointLog);
                                        }

                                    }
                                }
                                if (returnFlag) {
                                    Map<String, Object> dataMap = this.baseMapper.queryOrderReturnInfoById(orderId);
                                    if (baseEntity.getOrderPaymentAmount().compareTo(returnCash) > 0) {
                                        BigDecimal lv = orderReturnEntity.getReturnCash().divide(baseEntity.getOrderPaymentAmount(), 2, RoundingMode.HALF_UP);
                                        if (baseEntity.getOrderCommissionFee() != null) {
                                            dataMap.put("orderCommissionFee", BigDecimal.valueOf(baseEntity.getOrderCommissionFee()).multiply(lv).setScale(2, RoundingMode.HALF_UP));
                                        }
                                        if (baseEntity.getBaseMemberDiscount() != null) {
                                            dataMap.put("baseMemberDiscount", BigDecimal.valueOf(baseEntity.getBaseMemberDiscount()).multiply(lv).setScale(2, RoundingMode.HALF_UP));
                                        }
                                        if (baseEntity.getDistributionDiscount() != null) {
                                            dataMap.put("distributionDiscount", baseEntity.getDistributionDiscount().multiply(lv).setScale(2, RoundingMode.HALF_UP));
                                        }
                                    }
                                    dataMap.put("orderId", orderId);
                                    this.baseMapper.updateOrderReturnInfoById(dataMap);
                                }
                            }
                        }
                        Integer torf = this.baseMapper.updateOrderReturnById(orderGoodsMap);
                    }
                } catch (Exception e) {
                    arrayList.add(map1.getReturnCode());
                }
            }
        }
        return true;
    }
    public void updateVirtualGoodsOrder(String orderId,Integer shopId,String returnCode){
        List<Integer> returnGoods = baseMapper.queryReturnGoods(orderId, shopId.toString());
        Map<String, Object> updateGoodsMap = new HashMap<String, Object>();
        updateGoodsMap.put("orderId", orderId);
        updateGoodsMap.put("goodsReturnStatus", 0);
        updateGoodsMap.put("goodsRefundStatus", 3);
        for (Integer returnGood : returnGoods) {
            updateGoodsMap.put("goodsId", returnGood);
            this.baseMapper.updateOrderGoodsById1(updateGoodsMap);
        }
        Map<String, Object> updateMap = new HashMap<String, Object>();
        updateMap.put("orderId", orderId);
        updateMap.put("orderRefundStatus", "3");
        updateMap.put("orderReturnStatus", "0");
        this.baseMapper.updateOrderBaseById(updateMap);
        //更新record表状态
        Map<String, String> feignMap = new HashMap<>();
        feignMap.put("orderId", orderId);
        // 原值：7
        feignMap.put("recordStatus", "8");
        this.baseMapper.updateRefuse(feignMap);
        Map<String, Object> orderGoodsMap = new HashMap<>();
        orderGoodsMap.put("returnCode", returnCode);
        orderGoodsMap.put("returnShopHandle", 0);
        orderGoodsMap.put("returnState", 3);
        orderGoodsMap.put("returnShopTime", new Date());
        Calendar instance = Calendar.getInstance();
        instance.setTime(new Date());
        instance.add(Calendar.DATE, 2);
        Date expiration_time = instance.getTime();
        orderGoodsMap.put("expirationTime", expiration_time);
        orderGoodsMap.put("returnState", CommonType.returnState.SELLER_FAILED_THE_REVIEW.getCode());
        this.baseMapper.updateOrderReturnById(orderGoodsMap);
    }
    @Override
    public Boolean updateTimeOutReturnGoods() {
        //商家超时未同意退货订单
        List<ReturnOrderDetailsVO> orderDetails = this.baseMapper.queryTimeOutReturnGoods();
        if (orderDetails != null && orderDetails.size() > 0) {
            Map<String, Object> orderGoodsMap = new HashMap<String, Object>();
            for (ReturnOrderDetailsVO orderDetail : orderDetails) {
                try {
                    String returnCode = orderDetail.getReturnCode();
                    orderGoodsMap.put("returnCode", returnCode);
                    //平台5
                    orderGoodsMap.put("returnShopHandle", 2);
                    orderGoodsMap.put("returnState", 2);
                    //orderGoodsMap.put("returnAddrId", paraMap.get("returnAddrId"));
                    orderGoodsMap.put("returnAddr", orderDetail.getOrderSellerAddress());
                    orderGoodsMap.put("returnAddrName", orderDetail.getOrderSellerAddress());
                    orderGoodsMap.put("returnRealName", orderDetail.getOrderSellerName());
                    orderGoodsMap.put("returnNumber", orderDetail.getOrderSellerContact());
//				orderGoodsMap.put("returnShopMessage", paraMap.get("returnShopMessage"));
//				orderGoodsMap.put("returnShopTime", new Date());
                    Calendar instance = Calendar.getInstance();
                    instance.setTime(new Date());
                    instance.add(Calendar.DATE, 7);
                    Date expiration_time = instance.getTime();

                    orderGoodsMap.put("expirationTime", expiration_time);
                    Integer torf = this.baseMapper.updateOrderReturnById(orderGoodsMap);
                    orderGoodsMap.clear();
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }

        //商家超时未收货退货订单
        List<ReturnOrderDetailsVO> timeoutorder = this.baseMapper.queryTimeOutReturnGoods1();
        if (timeoutorder != null && timeoutorder.size() > 0) {
            Map<String, Object> orderGoodsMap = new HashMap<String, Object>();
            Map<String, Object> orderBaseMap = new HashMap<String, Object>();
            for (ReturnOrderDetailsVO orderDetail : timeoutorder) {
                try {
                    //查询用户售后商品
                    List<Integer> returnGoods = this.baseMapper.queryReturnGoods(orderDetail.getOrderId(), null);
                    Integer selectshoptype = this.baseMapper.selectshoptype(orderDetail.getShopId());
                    String returnCode = orderDetail.getReturnCode();
                    orderGoodsMap.put("returnCode", returnCode);
                    //4
                    orderGoodsMap.put("returnShopHandle", 4);
                    // 线下付款设置退款状态为待平台退款状态
                    if (orderDetail.getPaymentCode() == 2) {
                        orderGoodsMap.put("returnState", 17);
                    } else {
                        orderGoodsMap.put("returnState", 4);
                    }
                    orderGoodsMap.put("returnShopTime", new Date());
                    //orderGoodsMap.put("returnShopMessage", paraMap.get("returnShopMessage"));

                    //	 商家确认收货该状态为9已退款
                    OrderReturnEntity entity = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>()
                            .eq(OrderReturnEntity::getReturnCode, returnCode));
                    orderBaseMap.put("orderId", entity.getOrderId());
                    orderBaseMap.put("orderStatus", CommonType.orderStatus.REFUNDED.getCode());
                    orderBaseMap.put("orderReturnStatus", CommonType.orderReturnStatus.RETURN_COMPLETED.getCode());
                    orderBaseMap.put("orderRefundStatus", CommonType.orderRefundStatus.NO_REFUND.getCode());
                    Integer torf = orderBaseMapper.updateOrderBaseById(orderBaseMap);
                    Map<String, Object> updateGoodsMap = new HashMap<String, Object>();
                    updateGoodsMap.put("orderId", entity.getOrderId());
                    updateGoodsMap.put("goodsReturnStatus", 2);
                    updateGoodsMap.put("goodsRefundStatus", 0);
                    orderGoodsMapper.updateOrderGoodsById2(updateGoodsMap);
                    String uOrderId = this.baseMapper.queryPaymentNumber(entity.getOrderId());
                    OrderBaseEntity orderBaseEntity = orderBaseMapper.selectById(entity.getOrderId());
                    BigDecimal returnCash = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>()
                            .eq(OrderReturnEntity::getOrderId, entity.getOrderId())).getReturnCash();


                    List<Integer> orderTradeStatus = this.baseMapper.queryOrderRecordStatus(uOrderId);
                    RefundParamDTO refundPara = new RefundParamDTO();
                    boolean isRefundSuccess = false;
                    if (torf == 1) {
                        refundPara = this.baseMapper.queryRefundPara(entity.getOrderId());
                        refundPara.setOrderTradeStatus(orderTradeStatus);
                        refundPara.setReturnCash(returnCash);
                        refundPara.setShopId(orderDetail.getShopId());
                        refundPara.setShopType(selectshoptype);
                        isRefundSuccess = waitShipmentsRefundAndNotify(refundPara, entity.getOrderId(), returnCash);

                    }
                    if (!isRefundSuccess) {
                        // 回滚
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        return false;
                    }

                    if (1 != torf) {
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        return false;
                    }

                    // 分销佣金订单处理
                    commissionReturn(entity.getOrderId(), returnGoods);
                    //判断当前订单是否全部退货
                    Boolean huoBaoreturn = true;
                    //平台退货佣金计算  会员折扣计算
                    List<OrderPingtaiCommissionDTO> orderPingtaiCommissions = this.baseMapper.queryOrderPingTaiCommission(entity.getOrderId());
                    BigDecimal countPingTaiCommission = new BigDecimal(0);
                    BigDecimal huiYuanZhekou = new BigDecimal(0);
                    for (OrderPingtaiCommissionDTO orderPingtaiCommission : orderPingtaiCommissions) {
                        // 总的平台抽佣/下单数量 * 退单数量
                        System.out.println(new BigDecimal(orderPingtaiCommission.getGoodsNum()));
                        //下单数量 等于 退单数量 平台抽佣全退

                        if (!orderPingtaiCommission.getGoodsNum().equals(orderPingtaiCommission.getGoodsReturnNum())) {
                            huoBaoreturn = false;
                        }
                        //退还同步商品的 佣金
                        if (null != orderPingtaiCommission.getDropShipping() && 1 == orderPingtaiCommission.getDropShipping()) {
                            List<OrderSupplierBaseEntity> orderSupplierOrderBaseEntitys = orderSupplierBaseService.getBaseMapper().selectList(
                                    new LambdaQueryWrapper<OrderSupplierBaseEntity>()
                                    .eq(OrderSupplierBaseEntity::getOrderId,entity.getOrderId()));
                            for (OrderSupplierBaseEntity orderSupplierBaseEntity : orderSupplierOrderBaseEntitys) {
                                BigDecimal supperPrice = orderSupplierBaseEntity.getSupperCommission();
                                supperPrice = supperPrice.divide(new BigDecimal(orderPingtaiCommission.getGoodsNum()),2, RoundingMode.HALF_UP);
                                BigDecimal returnSupperPrice = supperPrice.multiply(new BigDecimal(orderPingtaiCommission.getGoodsReturnNum())).setScale(2, RoundingMode.HALF_UP);

                                orderSupplierBaseService.update(null,new LambdaUpdateWrapper<OrderSupplierBaseEntity>()
                                        .eq(OrderSupplierBaseEntity::getOrderId,entity.getOrderId())
                                        .eq(OrderSupplierBaseEntity::getGoodsId,orderSupplierBaseEntity.getGoodsId())
                                        .set(OrderSupplierBaseEntity::getSupperReturnCommission,returnSupperPrice));
                            }
                        }
                    }
                    //根据退款金额退多少佣金
                    BigDecimal goodsAmount = orderBaseEntity.getOrderPaymentAmount().subtract(orderBaseEntity.getOrderShippingFee());
                    if (goodsAmount.compareTo(returnCash) > 0) {
                        List<OrderGoodsVO> returnGoodsList = orderGoodsMapper.queryOrderGoodsByOrderId(entity.getOrderId());
                        for (OrderGoodsVO orderGoodsVO : returnGoodsList) {
                            if (Objects.equals(orderGoodsVO.getOrderGoodsNum(), orderGoodsVO.getOrderGoodsReturnNum())) {
                                countPingTaiCommission = countPingTaiCommission.add(orderGoodsVO.getOrderGoodsCommission());
                                huiYuanZhekou = huiYuanZhekou.add(orderGoodsVO.getGoodsMemberDiscount());
                            } else {
                                BigDecimal oneGoodsCommission = orderGoodsVO.getOrderGoodsCommission().divide(BigDecimal.valueOf(orderGoodsVO.getOrderGoodsNum()), 2, RoundingMode.HALF_UP);
                                BigDecimal returnGoodsCommission = oneGoodsCommission.multiply(BigDecimal.valueOf(orderGoodsVO.getOrderGoodsReturnNum()));
                                countPingTaiCommission = countPingTaiCommission.add(returnGoodsCommission);

                                BigDecimal oneGoodsMemberDiscount = orderGoodsVO.getGoodsMemberDiscount().divide(BigDecimal.valueOf(orderGoodsVO.getOrderGoodsNum()), 2, RoundingMode.HALF_UP);
                                BigDecimal returnGoodsMemberDiscount = oneGoodsMemberDiscount.multiply(BigDecimal.valueOf(orderGoodsVO.getOrderGoodsReturnNum()));
                                huiYuanZhekou = huiYuanZhekou.add(returnGoodsMemberDiscount);
                            }
                        }
                    } else {
                        countPingTaiCommission = BigDecimal.valueOf(orderBaseEntity.getOrderCommissionFee());
                        huiYuanZhekou = BigDecimal.valueOf(orderBaseEntity.getBaseMemberDiscount());
                    }
                    //根据订单号查询支付单号 因为有可能多个订单公用一个支付单号 如果是多个订单共用用一个支付单号 要判断除当前退单外的其他订单是否是全退
                    String paymentNumber = this.baseMapper.queryOrderPaymentNumber(entity.getOrderId());
                    //如果多个订单公用一个支付单号 根据当前订单号 和 支付单号 查询是否有其他订单没有全退货
                    Integer notOrderNum = this.baseMapper.queryNotRetuenOrder(entity.getOrderId(), paymentNumber);
                    if (0 <= notOrderNum) {
                        //说明有其他订单没有完全退货 不退红包 优惠券
                        huoBaoreturn = false;
                    }
                    //退货红包 优惠券
                    if (huoBaoreturn) {
                        this.baseMapper.updateHongbaoStart(entity.getOrderId());
                        this.baseMapper.updateYouHuiquanStart(entity.getOrderId());
                    }
                    this.baseMapper.updateOrderBasePingTaiCommission(entity.getOrderId(), countPingTaiCommission);
                    //更新退款表 平台退还佣金 退还会员折扣
                    this.baseMapper.updateOrderReturnPingTaiCommission(entity.getOrderId(), countPingTaiCommission, huiYuanZhekou);
                    // 代发货订单 经销商和供应商佣金处理
                    Integer i = 1;
                    BigDecimal reutrnOrderGoodsAmount = new BigDecimal(0);
                    for (Integer returnGood : returnGoods) {
                        Map<String, Object> updateGoodsReturnCommissionMap = new HashMap<>();
                        updateGoodsReturnCommissionMap.put("orderId", entity.getOrderId());
                        OrderGoodsEntity orderGoodsEntity = orderGoodsMapper.selectOne(new LambdaQueryWrapper<OrderGoodsEntity>()
                                .eq(OrderGoodsEntity::getOrderId, entity.getOrderId())
                                .eq(OrderGoodsEntity::getGoodsId, returnGood));
                        if (orderBaseEntity.getDealerCommission() != null && orderBaseEntity.getDealerCommission() > 0){
                            if (orderGoodsEntity.getDealerCommission() != null && orderGoodsEntity.getDealerCommission() > 0){
                                if(orderGoodsEntity.getOrderGoodsReturnnum().equals(orderGoodsEntity.getOrderGoodsNum())){
                                    updateGoodsReturnCommissionMap.put("dealerReturnCommission", orderGoodsEntity.getDealerCommission());
                                }else{
                                    BigDecimal dealerReturnCommission = BigDecimal.valueOf(orderGoodsEntity.getDealerCommission() * Integer.valueOf(orderGoodsEntity.getOrderGoodsReturnnum())
                                            / Integer.parseInt(orderGoodsEntity.getOrderGoodsNum())).setScale(2, RoundingMode.HALF_UP);
                                    updateGoodsReturnCommissionMap.put("dealerReturnCommission", dealerReturnCommission);
                                }
                            }
                        }
                        BigDecimal orderGoodsAmount = orderGoodsEntity.getOrderGoodsPaymentAmount();
                        if(i.equals(returnGoods.size())){
                            updateGoodsReturnCommissionMap.put("goodsReturnPrice", returnCash.subtract(reutrnOrderGoodsAmount));
                        }else{
                            BigDecimal lv = orderGoodsAmount.divide(returnCash, 2, RoundingMode.HALF_UP);
                            BigDecimal amount = (orderGoodsAmount).multiply(lv).setScale(2, RoundingMode.HALF_UP);
                            reutrnOrderGoodsAmount = reutrnOrderGoodsAmount.add(amount);
                            updateGoodsReturnCommissionMap.put("goodsReturnPrice", amount);
                            i++;
                        }
                        updateGoodsReturnCommissionMap.put("goodsId", returnGood);
                        this.baseMapper.updateOrderGoodsById1(updateGoodsReturnCommissionMap);
                    }

                    Integer storageId = orderShipmentMapper.queryStorageId(entity.getOrderId());
                    storageId = storageId != null ? storageId : 0;
                    List<StorageInventoryFlowGoodsDTO> storageGoodsList = new ArrayList<>();
                    Map<String, Object> goodsNumMap = new HashMap<>();
                    List<Map<String, Object>> goodsNum = this.baseMapper.queryBuyNumByorderId(entity.getOrderId());
                    goodsNumMap.put("goodsNum", goodsNum);
                    //退款更新商品销量
                    updateGoodsSalenum(goodsNumMap);
                    for (Map<String, Object> map : goodsNum) {
                        StorageInventoryFlowGoodsDTO storageInventoryFlowGoodsDTO = new StorageInventoryFlowGoodsDTO();
                        storageInventoryFlowGoodsDTO.setGoodsId(Integer.parseInt(map.get("goods_id").toString()));
                        storageInventoryFlowGoodsDTO.setGoodsNum(Integer.parseInt(map.get("order_goods_returnnum").toString()));
                        storageInventoryFlowGoodsDTO.setBuyNum(Integer.parseInt(map.get("order_goods_returnnum").toString()));
                        storageGoodsList.add(storageInventoryFlowGoodsDTO);
                    }
                    Integer inventoryFlowType = CommonType.inventoryFlowType.PUT_STORAGE.getCode();
                    Integer inventoryFlowTypeDetail = CommonType.inventoryFlowTypeDetail.RETURN_TO_INVENTORY_AND_STORAGE.getCode();
                    setStorageInventoryFlow(storageId, entity.getOrderId(), inventoryFlowType, inventoryFlowTypeDetail, storageGoodsList,
                            goodServiceClient, baseServiceClient, entity.getShopId(), "");
                    //根据orderid 查询应该回退的积分和userid
                    Map<String, Object> map = this.baseMapper.queryAddpointsLogByOrderId(entity.getOrderId());
                    if (map == null) {
                        map = new HashMap<String, Object>();
                    }
                    //ZZZ 新增积分按比例扣除逻辑
                    //订单实付金额
                    BigDecimal order_payment_amount = this.baseMapper.queryOrderAmount(entity.getOrderId());
                    //订单商品总价（不含运费）
                    BigDecimal order_goods_amount = this.baseMapper.queryOrderGoodsAmount(entity.getOrderId());
                    //退货商品总价(不含运费）
                    BigDecimal returnAmount = BigDecimal.ZERO;
                    //退货积分系数
                    BigDecimal coef = BigDecimal.ZERO;
                    List<OrderReturnPointsDTO> orderReturnPointsList = this.baseMapper.queryOrderReturnInfo(entity.getOrderId());
                    if (null != orderReturnPointsList && orderReturnPointsList.size() > 0) {
                        for (OrderReturnPointsDTO orp : orderReturnPointsList) {
                            BigDecimal goodsPrice = null != orp ? orp.getGoodsPrice() : BigDecimal.ZERO;
                            Integer goodsReturnNum = null != orp ? orp.getOrderGoodsReturnnum() : 0;
                            returnAmount = returnAmount.add(goodsPrice.multiply(new BigDecimal(goodsReturnNum)));
                        }
                    }
                    //实际退款金额(用于计算退还积分)
                    returnAmount = returnAmount.multiply(order_payment_amount).divide(order_goods_amount, RoundingMode.HALF_UP);
                    //实际退款积分ZZZ
                    BigDecimal tmp = BigDecimal.ZERO;
                    if (null != map && null != map.get("points_log_points")) {
                        tmp = returnAmount.multiply(new BigDecimal((Integer) map.get("points_log_points"))).divide(order_payment_amount, RoundingMode.HALF_UP);
                    }
                    Integer tmpInt = tmp.intValue();
                    map.put("points_log_points", tmpInt);

                    //插入我的余额明细表
                    Map<String, Object> assetDetailMap = new HashMap<>();
                    assetDetailMap.put("orderId", entity.getOrderId());
                    assetDetailMap.put("detailType", 2);
                    assetDetailMap.put("assetType", 1);
                    assetDetailMap.put("assetAmount", returnCash);
                    AssetDirectionAndPayTypeInfoDTO assetInfo = this.baseMapper.getAssetDirectionAndPayTypeInfo(entity.getOrderId());
                    if (null != assetInfo) {
                        assetDetailMap.put("assetDirection", assetInfo.getAssetDirection());
                        assetDetailMap.put("payType", assetInfo.getPayType());
                        assetDetailMap.put("userId", assetInfo.getUserId());
                        this.baseMapper.insertDetail(assetDetailMap);
                    }

                    //修改会员积分和会员等级
                    MemberMessageListIntegralDTO updateMemberMessageListIntegral = new MemberMessageListIntegralDTO();
                    ArrayList<String> list = new ArrayList<>();
                    list.add((String) map.get("user_id"));
                    updateMemberMessageListIntegral.setUserIdList(list);
                    updateMemberMessageListIntegral.setIntegral((Integer) map.get("points_log_points"));
                    //1是增加积分 0 是扣除积分
                    updateMemberMessageListIntegral.setType(0);
                    feignUpdateMemberIntegral(updateMemberMessageListIntegral);
                    //添加积分修改记录
                    UserPointLogParamDTO userPointLog = new UserPointLogParamDTO();
                    userPointLog.setPointsLogType(2);
                    userPointLog.setClassId(9);
                    userPointLog.setPointsLogTime(new Date());
                    userPointLog.setPointsLogDesc("商品退货");
                    userPointLog.setPointsLogFlag("reg");
                    userPointLog.setUserId(map.get("user_id").toString());
                    userPointLog.setPointsLogPoints((Integer) map.get("points_log_points"));
                    userPointLog.setPointsLogOperate("商品退货");
                    userPointLog.setOrderId(entity.getOrderId());
                    this.baseMapper.feginAddpointsLog(userPointLog);

                    orderGoodsMap.put("returnFinishTime", new Date());
                    this.baseMapper.updateOrderReturnById(orderGoodsMap);
                    orderGoodsMap.clear();
                    orderBaseMap.clear();
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
        return true;
    }

    /**
     * 更新超时未处理的退货订单状态。
     *
     * @return 更新操作是否成功的标志。
     */
    @Override
    public Boolean updateTimeOutBuyReturn() {
        // 通过基础Mapper查询超时未处理的退货订单详情
        List<ReturnOrderDetailVO> returnOrderDetails = this.baseMapper.updateTimeOutBuyReturn();
        // 检查查询结果是否为空或不为空且包含元素
        if (returnOrderDetails != null && returnOrderDetails.size() > 0) {
            // 初始化一个Map用于存储订单和商品的更新信息
            Map orderGoodsMap = new HashMap();
            // 遍历查询到的每个退货订单详情
            for (ReturnOrderDetailVO returnOrderDetail : returnOrderDetails) {
                try {
                    // 提取退货单号和订单号
                    String returnCode = returnOrderDetail.getReturnCode();
                    String orderId = returnOrderDetail.getOrderId();
                    // 设置订单和商品的处理状态为超时关闭，并记录关闭时间
                    // 超时关闭
                    orderGoodsMap.put("returnCode", returnCode);
                    orderGoodsMap.put("returnShopHandle", 8);
                    orderGoodsMap.put("returnState", 8);
                    orderGoodsMap.put("closeTime", new Date());
                    // 初始化订单和商品的更新Map
                    Map<String, Object> updateMap = new HashMap<String, Object>();
                    Map<String, Object> updateGoodsMap = new HashMap<String, Object>();
                    // 设置订单的更新信息，将订单的退款状态和商品的退货状态重置为待处理状态
                    Integer torfOne = null;
                    Integer torfTwo = null;
                    updateMap.put("orderId", orderId);
                    // 原值：2，现改为0，用户可持续申请
                    updateMap.put("orderRefundStatus", "0");
                    // 原值：2，现改为0，用户可持续申请
                    updateMap.put("goodsReturnStatus", "0");

                    updateGoodsMap.put("orderId", orderId);

                    updateGoodsMap.put("goodsReturnStatus", 0);
                    updateGoodsMap.put("goodsRefundStatus", 0);
                    // 记录商家拒绝退款的时间
                    String businessTime = String.valueOf(System.currentTimeMillis());
                    String businessRefundTime = DateUtils.stampToDate(businessTime);

                    updateGoodsMap.put("businessRefundTime", businessRefundTime);

                    // 更新订单基础信息，并检查更新是否成功
                    torfOne = orderBaseMapper.updateOrderBaseById(updateMap);
                    // 如果更新不成功，则回滚事务并返回更新失败
                    if (1 != torfOne) {
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        return false;
                    }
                    // 更新订单退货信息，并清理订单和商品的更新信息
                    Integer torf = this.baseMapper.updateOrderReturnById(orderGoodsMap);
                    orderGoodsMap.clear();
                } catch (NoTransactionException e) {
                    System.out.println(e);
                }
            }
        }
        // 如果所有订单更新都成功，则返回更新成功标志
        return true;
    }

    /**
     * 更新退货超时未处理的订单状态。
     *
     * @return Boolean 返回操作是否成功的标志。
     */
    @Override
    public Boolean updatebuyTimeOutReturn() {
        // 通过基础Mapper查询需要更新的退货订单详情
        List<ReturnOrderDetailVO> orderDetails = this.baseMapper.updatebuyTimeOutReturn();
        // 检查查询结果是否为空，不为空则进行更新操作
        if (orderDetails != null && orderDetails.size() > 0) {
            try {
                // 初始化用于存储订单和商品更新信息的Map
                Map orderGoodsMap = new HashMap();
                Map updateGoodsMap = new HashMap();
                // 遍历订单详情列表
                for (ReturnOrderDetailVO orderDetail : orderDetails) {
                    // 提取订单详情中的退货编码和订单编号
                    String returnCode = orderDetail.getReturnCode();
                    String orderId = orderDetail.getOrderId();
                    // 设置订单更新信息，包括退货处理状态和过期时间
                    orderGoodsMap.put("returnCode", returnCode);
                    orderGoodsMap.put("returnShopHandle", 13);
                    orderGoodsMap.put("returnState", 13);
                    orderGoodsMap.put("orderId", orderId);
                    // 设置订单过期时间为当前时间后一天
                    Calendar instance = Calendar.getInstance();
                    instance.setTime(new Date());
                    instance.add(Calendar.DATE, 1);
                    Date expiration_time = instance.getTime();
                    orderGoodsMap.put("expirationTime", expiration_time);
                    // 更新订单基础信息
                    this.baseMapper.updateOrderBaseById1(orderGoodsMap);

                    // 设置商品更新信息，包括退货状态和退款状态
                    updateGoodsMap.put("orderId", orderId);
                    updateGoodsMap.put("goodsReturnStatus", 0);
                    updateGoodsMap.put("goodsRefundStatus", 0);
                    updateGoodsMap.put("goodsBarterStatus", 0);
                    baseMapper.updateOrderGoodsById(updateGoodsMap);

                    // 此处注释掉的商品更新操作可能需要根据实际业务逻辑进行调整

                    // 更新订单中的商品退货状态和退款状态
                    Map<String, Object> updateMap = new HashMap<String, Object>();
                    updateMap.put("orderId", orderId);
                    updateMap.put("orderRefundStatus", "0");
                    updateMap.put("orderReturnStatus", "0");
                    // 执行订单基础信息的更新操作，并检查更新是否成功
                    Integer torf = orderBaseMapper.updateOrderBaseById(updateMap);
                    if (1 != torf) {
                        // 如果更新不成功，则设置事务回滚并返回失败标志
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        return false;
                    }
                }

                // 更新退货信息，并清理临时Map对象
                Integer torf = this.baseMapper.updateOrderReturnById(orderGoodsMap);
                orderGoodsMap.clear();
                updateGoodsMap.clear();
            } catch (NoTransactionException e) {
                System.out.println(e);
            }
        }
        // 如果所有操作都成功，则返回成功标志
        return true;
    }


    public Boolean retForSellerReceiveBoolean(Map<String, String> paraMap,boolean isPlatform) throws TransactionException {
        Map<String, Object> orderGoodsMap = new HashMap<>();
        Map<String, Object> orderBaseMap = new HashMap<>();

        //	 商家确认收货该状态为9已退款
        String orderId = this.baseMapper.getOrderIdByReturnCode(paraMap.get("returnCode"));
        String returnGoodsName = this.baseMapper.queryReturnGoodsNameByOrderId(orderId);
        OrderBaseEntity orderBaseEntity = orderBaseMapper.selectById(orderId);
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("orderId", orderId);
        messageMap.put("goodsName", returnGoodsName);
        List<String> messageList = new ArrayList<>();
        messageList.add(orderId);
        messageMap.put("userId", orderBaseEntity.getBuyerUserId());
        messageMap.put("templateId", CommonType.messageTemplate.CODE_E.getCode());
        paraMap.put("userName",orderBaseEntity.getSellerUserName());
        // 商家同意
        if (1 == Integer.parseInt(paraMap.get("type"))) {
            Integer storageId;
            String storageIdStr = paraMap.get("storageId");
            if (StringUtils.isEmpty(storageIdStr)) {
                storageId = baseMapper.queryStorageId(orderId);
                if (storageId == null) {
                    storageId = orderShipmentMapper.queryStorageId(orderId);
                }
            } else {
                storageId = Integer.parseInt(storageIdStr);
            }
            List<StorageInventoryFlowGoodsDTO> storageGoodsList = new ArrayList<>();
            orderGoodsMap.put("returnCode", paraMap.get("returnCode"));
            orderGoodsMap.put("returnShopHandle", isPlatform ? 4 : 2);
            // 如果是线下付款订单，则设置订单状态为待平台退款
            if (orderBaseEntity.getPaymentCode() == 2) {
                orderGoodsMap.put("returnState", 17);
            } else {
                orderGoodsMap.put("returnState", 4);
            }
            orderGoodsMap.put("returnShopTime", new Date());
            orderGoodsMap.put("returnShopMessage", paraMap.get("returnShopMessage"));
            orderGoodsMap.put("returnFinishTime", new Date());
            orderGoodsMap.put("storageId", storageId);


            //查询用户售后商品
            List<Integer> returnGoods = this.baseMapper.queryReturnGoods(orderId, paraMap.get("shopId"));

            //所有决定都在商家，不需要管供应商是否收到货
//           // 查询是否所有商品的供应商都收到货
            Integer torf = 0;
//            Integer integer = this.baseMapper.queryOrderGoodsReturnStatus(orderId);
//            if(integer == 0){
            orderBaseMap.put("orderId", orderId);
            if (orderBaseEntity.getPaymentCode() == 2) {
                orderBaseMap.put("orderStatus", "28");
            } else {
                orderBaseMap.put("orderStatus", "9");
            }
            orderBaseMap.put("orderReturnStatus", isPlatform ? "4" : "2");
            orderBaseMap.put("orderRefundStatus", "0");
            torf = this.baseMapper.updateOrderBaseById(orderBaseMap);
//            }

            String uOrderId = this.baseMapper.queryPaymentNumber(orderId);
            BigDecimal returnCash = this.baseMapper.queryReturnCash(orderId);

            List<Integer> orderTradeStatus = this.baseMapper.queryOrderRecordStatus(uOrderId);
            boolean isRefundSuccess = false;
            if (torf == 1) {
                RefundParamDTO refundPara = this.baseMapper.queryRefundPara(orderId);
                refundPara.setOrderTradeStatus(orderTradeStatus);
                refundPara.setReturnCash(returnCash);
                refundPara.setShopType(Integer.valueOf(paraMap.get("shopType")));
                refundPara.setShopId(Integer.valueOf(paraMap.get("shopId")));
                isRefundSuccess = waitShipmentsRefundAndNotify(refundPara, orderId, returnCash);

            }
            if (!isRefundSuccess) {
                // 回滚
                GlobalTransactionContext.reload(RootContext.getXID()).rollback();
                return false;
            }

            if (1 != torf) {
                GlobalTransactionContext.reload(RootContext.getXID()).rollback();
                return false;
            }

            // 分销佣金订单处理
            commissionReturn(orderId, returnGoods);
            //平台退货佣金计算  会员折扣计算
            BigDecimal huiYuanZhekou = new BigDecimal(0);
            BigDecimal countPingTaiCommission = new BigDecimal(0);
            //根据退款金额退多少佣金
            BigDecimal goodsAmount = orderBaseEntity.getOrderPaymentAmount().subtract(orderBaseEntity.getOrderShippingFee());
            if (goodsAmount.compareTo(returnCash) > 0) {
                List<OrderGoodsVO> returnGoodsList = orderGoodsMapper.queryOrderGoodsByOrderId(orderId);
                for (OrderGoodsVO orderGoodsVO : returnGoodsList) {
                    if (Objects.equals(orderGoodsVO.getOrderGoodsNum(), orderGoodsVO.getOrderGoodsReturnNum())) {
                        countPingTaiCommission = countPingTaiCommission.add(orderGoodsVO.getOrderGoodsCommission());
                        huiYuanZhekou = huiYuanZhekou.add(orderGoodsVO.getGoodsMemberDiscount());
                    } else {
                        BigDecimal oneGoodsCommission = orderGoodsVO.getOrderGoodsCommission().divide(BigDecimal.valueOf(orderGoodsVO.getOrderGoodsNum()), 2, RoundingMode.HALF_UP);
                        BigDecimal returnGoodsCommission = oneGoodsCommission.multiply(BigDecimal.valueOf(orderGoodsVO.getOrderGoodsReturnNum()));
                        countPingTaiCommission = countPingTaiCommission.add(returnGoodsCommission);

                        BigDecimal oneGoodsMemberDiscount = orderGoodsVO.getGoodsMemberDiscount().divide(BigDecimal.valueOf(orderGoodsVO.getOrderGoodsNum()), 2, RoundingMode.HALF_UP);
                        BigDecimal returnGoodsMemberDiscount = oneGoodsMemberDiscount.multiply(BigDecimal.valueOf(orderGoodsVO.getOrderGoodsReturnNum()));
                        huiYuanZhekou = huiYuanZhekou.add(returnGoodsMemberDiscount);
                    }
                }
            } else {
                countPingTaiCommission = BigDecimal.valueOf(orderBaseEntity.getOrderCommissionFee());
                huiYuanZhekou = BigDecimal.valueOf(orderBaseEntity.getBaseMemberDiscount());
            }
            // 更新订单商品信息为退款状态order_goods
            // 代发货订单 经销商和供应商佣金处理
            Map<String, Object> updateGoodsMap = new HashMap<>();
            updateGoodsMap.put("orderId", orderId);
            updateGoodsMap.put("goodsReturnStatus", 2);
            updateGoodsMap.put("goodsRefundStatus", 0);
            Integer i = 1;
            BigDecimal reutrnOrderGoodsAmount = new BigDecimal(0);
            for (Integer returnGood : returnGoods) {
                OrderGoodsEntity orderGoodsEntity = orderGoodsMapper.selectOne(new LambdaQueryWrapper<OrderGoodsEntity>()
                        .eq(OrderGoodsEntity::getOrderId, orderId)
                        .eq(OrderGoodsEntity::getGoodsId, returnGood));
                if (orderBaseEntity.getDealerCommission() != null && orderBaseEntity.getDealerCommission() > 0){
                    if (orderGoodsEntity.getDealerCommission() != null && orderGoodsEntity.getDealerCommission() > 0){
                        if(orderGoodsEntity.getOrderGoodsReturnnum().equals(orderGoodsEntity.getOrderGoodsNum())){
                            updateGoodsMap.put("dealerReturnCommission", orderGoodsEntity.getDealerCommission());
                        }else{
                            BigDecimal dealerReturnCommission = BigDecimal.valueOf(orderGoodsEntity.getDealerCommission() * Integer.valueOf(orderGoodsEntity.getOrderGoodsReturnnum())
                                    / Integer.parseInt(orderGoodsEntity.getOrderGoodsNum())).setScale(2, RoundingMode.HALF_UP);
                            updateGoodsMap.put("dealerReturnCommission", dealerReturnCommission);
                        }
                    }
                }
                BigDecimal orderGoodsAmount = orderGoodsEntity.getOrderGoodsPaymentAmount();
                if(i.equals(returnGoods.size())){
                    updateGoodsMap.put("goodsReturnPrice", returnCash.subtract(reutrnOrderGoodsAmount));
                }else{
                    BigDecimal lv = orderGoodsAmount.divide(returnCash, 2, RoundingMode.HALF_UP);
                    BigDecimal amount = (orderGoodsAmount).multiply(lv).setScale(2, RoundingMode.HALF_UP);
                    reutrnOrderGoodsAmount = reutrnOrderGoodsAmount.add(amount);
                    updateGoodsMap.put("goodsReturnPrice", amount);
                    i++;
                }
                updateGoodsMap.put("goodsId", returnGood);
                this.baseMapper.updateOrderGoodsById1(updateGoodsMap);
            }
            //退还红包优惠卷
            returnred(orderId, returnCash, returnGoods);

            this.baseMapper.updateOrderBasePingTaiCommission(orderId, countPingTaiCommission);
            //更新退款表 平台退还佣金 退还会员折扣
            this.baseMapper.updateOrderReturnPingTaiCommission(orderId, countPingTaiCommission, huiYuanZhekou);
            // 处理子单佣金
            updateSonOrderCommissionFee(orderId);
            Map<String, Object> goodsNumMap = new HashMap<>();
            List<Map<String, Object>> goodsNum = this.baseMapper.queryBuyNumByorderId(orderId);
            //买家id
            String buyerUserId = goodsNum.get(0).get("buyer_user_id").toString();
            goodsNumMap.put("goodsNum", goodsNum);
            //退款更新商品销量
            updateGoodsSalenum(goodsNumMap);
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
            //根据orderid 查询应该回退的积分和userid
            Map<String, Object> map = this.baseMapper.queryAddpointsLogByOrderId(orderId);
            //ZZZ 新增积分按比例扣除逻辑
            //订单实付金额
            BigDecimal order_payment_amount = this.baseMapper.queryOrderAmount(orderId);
            //订单退款金额
            BigDecimal returnAmount = returnCash;
            BigDecimal tmp = BigDecimal.ZERO;
            if (null != map) {
                if (null != map.get("points_log_points")) {
                    tmp = returnAmount.multiply(new BigDecimal((Integer) map.get("points_log_points")).divide(order_payment_amount, RoundingMode.HALF_UP));
                }
                Integer tmpInt = tmp.intValue();
                map.put("points_log_points", tmpInt);
                if (orderBaseEntity.getPaymentCode() != 2) {
                    //插入我的余额明细表
                    Map<String, Object> assetDetailMap = new HashMap<>();
                    assetDetailMap.put("orderId", orderId);
                    assetDetailMap.put("detailType", 2);
                    assetDetailMap.put("assetType", 1);
                    assetDetailMap.put("assetAmount", returnCash);
                    AssetDirectionAndPayTypeInfoDTO assetInfo = this.baseMapper.getAssetDirectionAndPayTypeInfo(orderId);
                    if (null != assetInfo) {
                        assetDetailMap.put("assetDirection", assetInfo.getAssetDirection());
                        assetDetailMap.put("payType", assetInfo.getPayType());
                        assetDetailMap.put("userId", assetInfo.getUserId());
                        this.baseMapper.insertDetail(assetDetailMap);
                    }

                    //修改会员积分和会员等级
                    MemberMessageListIntegralDTO updateMemberMessageListIntegral = new MemberMessageListIntegralDTO();
                    ArrayList<String> list = new ArrayList<>();
                    list.add(String.valueOf(map.get("user_id")));
                    updateMemberMessageListIntegral.setUserIdList(list);
                    updateMemberMessageListIntegral.setIntegral((Integer) map.get("points_log_points"));
                    //1是增加积分 0 是扣除积分
                    updateMemberMessageListIntegral.setType(0);
                    feignUpdateMemberIntegral(updateMemberMessageListIntegral);
                    //添加积分修改记录
                    UserPointLogParamDTO userPointLog = new UserPointLogParamDTO();
                    userPointLog.setPointsLogType(2);
                    userPointLog.setClassId(9);
                    userPointLog.setPointsLogTime(new Date());
                    userPointLog.setPointsLogDesc("商品退货");
                    userPointLog.setPointsLogFlag("reg");
                    userPointLog.setUserId(map.get("user_id").toString());
                    userPointLog.setPointsLogPoints((Integer) map.get("points_log_points"));
                    userPointLog.setPointsLogOperate("商品退货");
                    userPointLog.setOrderId(orderId);
                    this.baseMapper.feginAddpointsLog(userPointLog);
                }
                //罚扣质保金
                charging(orderId, returnGoods, paraMap.get("userId"), Integer.valueOf(paraMap.get("shopId")));
                //退回plus折扣额度
                try {
                    plusdis(orderId, buyerUserId, returnGoods);
                } catch (ParseException ignored) {}
            }
            messageList.add("已");
            messageMap.put("list", messageList);
            messageMap.put("result", "已");
            messageMap.put("status", "您提交的退货订单，商家已退货");
            //发送站内信
            baseServiceClient.sendMessageTemplate(messageMap);
            Integer inventoryFlowType = CommonType.inventoryFlowType.PUT_STORAGE.getCode();
            Integer inventoryFlowTypeDetail = CommonType.inventoryFlowTypeDetail.RETURN_TO_INVENTORY_AND_STORAGE.getCode();
            setStorageInventoryFlow2(storageId, orderId, inventoryFlowType, inventoryFlowTypeDetail, storageGoodsList,
                    goodServiceClient, baseServiceClient, orderBaseEntity.getShopId(), paraMap);
            //所有决定都在商家，不需要管供应商是否收到货
            //判断是否所有商品都已被供应商收到货
//            if(integer == 0) {
            Integer torfs = this.baseMapper.updateOrderReturnById(orderGoodsMap);
            if (1 == torfs) {
                List<OrderBaseEntity> originalOrderBaseList = orderBaseMapper.selectList(new LambdaQueryWrapper<OrderBaseEntity>()
                        .eq(OrderBaseEntity::getOriginalOrder, orderId));

                if (!originalOrderBaseList.isEmpty()) {
                    for (OrderBaseEntity entity : originalOrderBaseList) {
                        OrderReturnEntity orderReturnEntity = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>()
                                .eq(OrderReturnEntity::getOrderId, entity.getOrderId()));
                        if (null != orderReturnEntity) {
                            paraMap.put("returnCode", orderReturnEntity.getReturnCode());
                            retForSellerReceive(paraMap);
                        }
                    }
                }
                return true;
            }
            return false;
//            }
//            return true;
        }

        // 商家不同意
        orderGoodsMap.put("returnCode", paraMap.get("returnCode"));
        orderGoodsMap.put("returnShopMessage", paraMap.get("returnShopMessage"));
        orderGoodsMap.put("returnShopHandle", paraMap.get("returnState") != null ? paraMap.get("returnState") : 10);
        orderGoodsMap.put("returnState", paraMap.get("returnState") != null ? paraMap.get("returnState") : 10);
        orderGoodsMap.put("returnShopTime", new Date());
        orderGoodsMap.put("closeTime", new Date());
        Calendar instance = Calendar.getInstance();
        instance.setTime(new Date());
        instance.add(Calendar.DATE, 2);
        Date expirationTime = instance.getTime();
        orderGoodsMap.put("expirationTime", expirationTime);

        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("orderId", orderId);
        updateMap.put("orderReturnStatus", "3");
        updateMap.put("orderRefundStatus", "0");
        Integer torf = this.baseMapper.updateOrderBaseById(updateMap);
        Map<String, Object> updateGoodsMap = new HashMap<>();

        //查询用户售后商品
        List<Integer> returnGoods = this.baseMapper.queryReturnGoods(orderId, paraMap.get("shopId"));

        updateGoodsMap.put("orderId", orderId);
        updateGoodsMap.put("goodsReturnStatus", 3);
        updateGoodsMap.put("orderRefundStatus", 0);
        for (Integer returnGood : returnGoods) {
            updateGoodsMap.put("goodsId", returnGood);
            this.baseMapper.updateOrderGoodsById1(updateGoodsMap);
        }
        //更新record表状态
        Map<String, String> feignMap = new HashMap<>();
        feignMap.put("orderId", orderId);
        feignMap.put("recordStatus", "7");
        this.baseMapper.updateRefuse(feignMap);

        messageList.add("不同意");
        messageMap.put("list", messageList);
        messageMap.put("result", "不同意");
        messageMap.put("status", "您提交的退货订单，商家未同意退货");
        baseServiceClient.sendMessageTemplate(messageMap);

        if (1 != torf) {
            GlobalTransactionContext.reload(RootContext.getXID()).rollback();
            return false;
        }
        Integer torfs = this.baseMapper.updateOrderReturnById(orderGoodsMap);
        if (torfs > 0) {
            // 根据只订单退货编码查询主订单id
            // String orderId = this.baseMapper.getOrderIdByReturnCode(paraMap.get("returnCode"));
            // 根据主订单id获取子订单ID
            List<OrderBaseEntity> list = orderBaseMapper.selectList(new LambdaQueryWrapper<OrderBaseEntity>()
                    .eq(OrderBaseEntity::getOriginalOrder, orderId));

            if (!list.isEmpty()) {
                for (OrderBaseEntity entity : list) {
                    OrderReturnEntity orderReturnEntity = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>()
                            .eq(OrderReturnEntity::getOrderId, entity.getOrderId()));
                    if (null != orderReturnEntity) {
                        paraMap.put("returnCode", orderReturnEntity.getReturnCode());
                        retForSellerReceive(paraMap);
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 处理订单佣金返回逻辑。
     *
     * @param orderId 订单ID，用于查询订单佣金信息。
     */
    public void commissionReturn(String orderId, List<Integer> returnGoods) {
        try {
            for (Integer returnGood : returnGoods) {
                // 根据订单ID查询订单佣金信息。
                OrderCommissionVO orderCommission = orderCommissionMapper.selectByPrimaryKey(orderId, returnGood);
                // 如果订单佣金信息存在，则进行后续处理。
                if (null != orderCommission) {
                    OrderGoodsEntity orderGoods = orderGoodsMapper.selectReturnGoods(orderId, returnGood);
                    // 退款的商品金额-排除运费
                    BigDecimal returnMoney = getReturnMoney(orderGoods, orderCommission);
                    // 计算员应返回的佣金信息。
                    OrderCommissionVO updateOrderCommission = getOrderCommissionVO(orderId, returnMoney, orderCommission, returnGood);
                    // 更新订单佣金信息。
                    orderCommissionMapper.updateByPrimaryKeySelective(updateOrderCommission);
                }
            }
            // 操作成功。
        } catch (Exception e) {
            // 操作失败。
        }
    }

    /**
     * 计算退货应返回的金额
     * <p>
     * 此方法用于准确计算在订单退货或退款时应返回给用户的金额考虑到可能存在的商品数量差异，
     * 其计算逻辑分为两种情况：一是退货数量与订单中商品的总数量相同时，直接返回订单价格；
     * 二是在存在数量差异时，根据比例计算应返回的金额
     *
     * @param orderGoods      订单退货实体，包含退货数量等信息
     * @param orderCommission 订单佣金信息，包含订单价格和商品总数量等信息
     * @return 计算得出的应返回金额，保留两位小数
     */
    private static BigDecimal getReturnMoney(OrderGoodsEntity orderGoods, OrderCommissionVO orderCommission) {
        // 定义应返回的金额变量
        BigDecimal returnMoney;
        // 当退货数量与订单中的商品总数量相等时，直接返回订单价格
        if (Objects.equals(orderGoods.getOrderGoodsNum(), orderCommission.getGoodsNum().toString())) {
            returnMoney = orderCommission.getOrderPrice();
        } else {
            // 当存在数量差异时，按比例计算应返回的金额
            // 首先根据总价格和总数量计算单个商品的价格，然后根据退货数量计算应返回的总金额，并保留两位小数
            returnMoney = orderCommission.getOrderPrice().divide(new BigDecimal(orderCommission.getGoodsNum()), 2, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(orderGoods.getOrderGoodsNum())).setScale(2, RoundingMode.HALF_UP);
        }
        return returnMoney;
    }

    /**
     * 计算并更新订单中推广员和平台的佣金返还金额
     *
     * @param orderId         订单ID
     * @param returnMoney     需要返还的金额
     * @param orderCommission 订单佣金信息
     * @return 更新后的订单佣金信息对象
     */
    private static OrderCommissionVO getOrderCommissionVO(String orderId, BigDecimal returnMoney, OrderCommissionVO orderCommission, Integer returnGood) {
        // 计算一级推广员应返回的佣金。
        BigDecimal returnOneTgCommission = returnMoney.divide(orderCommission.getOrderPrice(), 2, RoundingMode.HALF_UP)
                .multiply(orderCommission.getOneTgCommission()).setScale(2, RoundingMode.HALF_UP);
        // 计算二级推广员应返回的佣金。
        BigDecimal returnTwoTgCommission = returnMoney.divide(orderCommission.getOrderPrice(), 2, RoundingMode.HALF_UP)
                .multiply(orderCommission.getTwoTgCommission()).setScale(2, RoundingMode.HALF_UP);
        // 计算平台应返回的佣金。
        BigDecimal returnFxCommission = returnMoney.divide(orderCommission.getOrderPrice(), 2, RoundingMode.HALF_UP)
                .multiply(orderCommission.getFxCommission()).setScale(2, RoundingMode.HALF_UP);
        // 构造更新后的订单佣金信息。
        return new OrderCommissionVO(orderId, CommonType.CommissionOrderStatus.RETURN.getCode(), returnMoney,
                returnOneTgCommission, returnTwoTgCommission, returnFxCommission, returnGood);
    }
    /**
     * 处理超时未处理的退货申请，将其状态更新为已退货。
     *
     * @return 更新操作是否成功的标志，成功返回true，失败返回false。
     */
    @Override
    public Boolean updatebuyTimeOutReturnGoods() {
        // 从数据库查询所有超时未处理的退货申请详情
        List<ReturnOrderDetailVO> orderDetails = this.baseMapper.updatebuyTimeOutReturnGoods();
        // 检查查询结果是否为空，不为空则继续处理
        if (orderDetails != null && orderDetails.size() > 0) {
            // 初始化用于存储订单商品信息的映射表
            Map orderGoodsMap = new HashMap();
            // 初始化用于存储商品更新信息的映射表
            Map updateGoodsMap = new HashMap();
            // 遍历每个退货申请详情
            for (ReturnOrderDetailVO orderDetail : orderDetails) {
                try {
                    // 提取退货申请编码和订单编号
                    String returnCode = orderDetail.getReturnCode();
                    String orderId = orderDetail.getOrderId();
                    // 设置订单商品信息映射表的值，包括退货处理状态和过期时间等
                    orderGoodsMap.put("returnCode", returnCode);
                    orderGoodsMap.put("returnShopHandle", 13);
                    orderGoodsMap.put("returnState", 13);
                    orderGoodsMap.put("callStatus", 1);
                    orderGoodsMap.put("orderId", orderId);
                    // 设置订单过期时间为当前时间加一天
                    Calendar instance = Calendar.getInstance();
                    instance.setTime(new Date());
                    instance.add(Calendar.DATE, 1);
                    Date expiration_time = instance.getTime();
                    orderGoodsMap.put("expirationTime", expiration_time);
                    // 更新订单基础信息
                    this.baseMapper.updateOrderBaseById1(orderGoodsMap);

                    // 设置商品更新信息映射表的值，包括商品退货状态和退款状态
                    updateGoodsMap.put("orderId", orderId);
                    updateGoodsMap.put("goodsReturnStatus", 0);
                    updateGoodsMap.put("goodsRefundStatus", 0);
                    updateGoodsMap.put("goodsBarterStatus", 0);
                    baseMapper.updateOrderGoodsById(updateGoodsMap);
                    // 此处注释掉的代码可能是之前尝试更新商品信息的逻辑

                    // 初始化用于更新订单状态的映射表
                    Map<String, Object> updateMap = new HashMap<String, Object>();
                    updateMap.put("orderId", orderId);
                    updateMap.put("orderRefundStatus", "0");
                    updateMap.put("orderReturnStatus", "0");
                    // 更新订单的基础状态
                    Integer torf = this.baseMapper.updateOrderBaseById(updateMap);
                    // 如果更新不成功，则回滚事务并返回失败标志
                    if (1 != torf) {
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        return false;
                    }
                    // 更新订单的退货信息
                    Integer torf1 = this.baseMapper.updateOrderReturnById(orderGoodsMap);
                    // 清空订单商品信息和商品更新信息的映射表，以便处理下一个订单
                    orderGoodsMap.clear();
                    updateGoodsMap.clear();
                } catch (NoTransactionException e) {
                    System.out.println(e);
                }
            }
        }
        // 如果所有订单更新成功，则返回成功标志
        return true;
    }

    @Override
    public String queryRefundOrderNum(String orderId) {
        String refundOrderNum = this.baseMapper.queryRefundOrderNum(orderId);
        return refundOrderNum;
    }

    @Override
    public BigDecimal querShopReturnCash(ShopSalesStatisticsDTO pojo) {
        BigDecimal shopReturnCash = this.baseMapper.querShopReturnCash(pojo);
        return shopReturnCash;
    }

    /**
     * 通过SSL POST方法发送请求。
     *
     * @param url      请求的URL地址。
     * @param data     请求的数据。
     * @param certPath 客户端证书路径。
     * @param certPass 证书密码。
     * @return 服务器响应的内容。
     */
    public static String postSSL(String url, String data, String certPath, String certPass) {
        // 默认的用户代理字符串，模拟浏览器发送请求。
        String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.146 Safari/537.36";
        HttpsURLConnection conn = null;
        OutputStream out = null;
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            // 初始化客户端证书存储，加载证书。
            KeyStore clientStore = KeyStore.getInstance("PKCS12");
            clientStore.load(new FileInputStream(certPath), certPass.toCharArray());
            // 初始化密钥管理工厂，使用加载的证书。
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(clientStore, certPass.toCharArray());
            // 初始化SSL上下文，用于创建HTTPS连接。
            KeyManager[] kms = kmf.getKeyManagers();
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kms, null, new SecureRandom());
            // 设置默认的SSLSocketFactory，用于HTTPS连接。
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            // 创建URL对象并打开连接。
            URL _url = new URL(url);
            conn = (HttpsURLConnection) _url.openConnection();
            // 设置连接超时和读取超时时间。
            conn.setConnectTimeout(25000);
            conn.setReadTimeout(25000);
            // 设置请求方法为POST。
            conn.setRequestMethod("POST");
            // 允许输出和输入，即发送数据和接收响应。
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 设置请求头信息。
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("User-Agent", DEFAULT_USER_AGENT);
            // 连接服务器。
            conn.connect();
            // 发送请求数据。
            out = conn.getOutputStream();
            out.write(data.getBytes(Charset.forName("UTF-8")));
            out.flush();
            // 读取响应数据。
            inputStream = conn.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            // 返回响应内容。
            return sb.toString();
        } catch (Exception e) {
            // 抛出运行时异常，以便调用方可以捕获并处理。
            throw new RuntimeException(e);
        } finally {
            // 关闭资源。
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(inputStream);
            if (conn != null) {
                conn.disconnect();
            }
        }
    }


    public Boolean updateRefundAccountPeriod(RefundAccountParamDTO param) {
        //退回账期可用额度
        int row = this.baseMapper.updateRefundAccountPeriod(param);
        if (row <= 0) {
            return false;
        }
        //查找账单信息
        AccountShopBillDetailsVO detail = this.baseMapper.selectDetailsByPeriodId(param.getAccountingPeriodId());
        //更改账单状态
        if (detail.getRemainingAccounts().compareTo(BigDecimal.ZERO) == 0) {
            //全部还款
            this.baseMapper.editAccountStatus(param.getAccountingPeriodId(), 2);
        } else if (detail.getRemainingAccounts().compareTo(BigDecimal.ZERO) == 1 && detail.getAccountStatus() != 3) {
            //部分还款且不逾期
            this.baseMapper.editAccountStatus(param.getAccountingPeriodId(), 1);
        }
        //修改账期会员的使用额度跟可用额度
        //查找账期会员的账期信息
        AccountPeriodVO account = this.baseMapper.getAccountPeriodByIds(detail.getShopId(), detail.getUserId());
        BigDecimal amountUsed = account.getAmountUsed().subtract(param.getRefundPrice());
        BigDecimal availableAmount = BigDecimal.ZERO;
        if (account.getAccountIsLimit() != 0) {
            availableAmount = account.getAvailableAmount().add(param.getRefundPrice());
        }
        BigDecimal monthAmountUsed = account.getMonthAmountUsed();
        Boolean isMonth = DateUtil.isMonth(new Date(), detail.getStartTime());
        if (isMonth) {
            monthAmountUsed = account.getMonthAmountUsed().subtract(param.getRefundPrice());
        }
        AccountPeriodParamDTO periodParam = new AccountPeriodParamDTO(detail.getShopId(), detail.getUserId(), amountUsed, availableAmount, monthAmountUsed);
        //更新账期信息
        this.baseMapper.updateAccountPeriod(periodParam);
        return true;
    }

    public void updateGoodsSalenum(Map<String, Object> param) {
        Map<String, Object> esMap = new HashMap<>();
        List<Integer> list = new ArrayList<>();
        try {
            List<Map<String, Object>> goodsNum = (List<Map<String, Object>>) param.get("goodsNum");
            if (null != goodsNum) {
                for (Map<String, Object> map : goodsNum) {
                    //退款 或 退货数量
                    Integer orderGoodsReturnNum = (Integer) map.get("order_goods_returnnum");
                    //商品id
                    Integer commonId = (Integer) map.get("common_id");
                    if (null != commonId && null != orderGoodsReturnNum) {
                        list.add(commonId);
                        //查询当前商品的销量
                        Integer salenum = this.baseMapper.queryCommonNameSalenum(commonId);
                        if (null != salenum) {
                            if (salenum - orderGoodsReturnNum > 0) {
                                this.baseMapper.updateGoodsSalenum(commonId, salenum - orderGoodsReturnNum);
                            } else {
                                this.baseMapper.updateGoodsSalenum(commonId, 0);
                            }
                        }
                    }
                    //更新es
                    map.put("commonIdS", list);
                    // 1 是添加  2是删除
                    map.put("type", 1);
                    MqSendUtils.mqSendGoods(esMap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void feignUpdateMemberIntegral(MemberMessageListIntegralDTO param) {
        try {
            //获取所有用户id
            List<String> userIdList = param.getUserIdList();
            Integer userIntegral = null;
            Map<String, Object> map = new HashMap<>();
            //type 1是增加积分 0 是扣除积分
            if (param.getType() == 1) {
                //便利 添加积分
                for (String userId : userIdList) {
                    //获取当前用户的积分
                    userIntegral = this.baseMapper.queryMemberMessageListIntegral(userId);
                    Integer temp = userIntegral + param.getIntegral();
                    map.put("userid", userId);
                    map.put("integral", temp);
                    this.baseMapper.updateMemberMessageListIntegral(map);
                }
            } else {
                //便利 扣除积分
                for (String userId : userIdList) {
                    //获取当前用户的积分
                    userIntegral = this.baseMapper.queryMemberMessageListIntegral(userId);
                    //如果当前用户的积分 小于 等于 平台扣除的积分直接赋值为 0
                    Integer temp = userIntegral - param.getIntegral();
                    if (temp <= 0) {
                        map.put("userid", userId);
                        map.put("integral", 0);
                    } else {
                        map.put("userid", userId);
                        map.put("integral", temp);
                    }
                    this.baseMapper.updateMemberMessageListIntegral(map);
                }
            }
            //积分修改完成 修改会员等级
            for (String userId : userIdList) {
                //获取当前用户的积分
                userIntegral = this.baseMapper.queryMemberMessageListIntegral(userId);
                MembershipGradeDTO membershipGrade = this.baseMapper.queryMembershipGradeByGradeRule(userIntegral);
                UpMemberClassDTO upMemberClassDto = new UpMemberClassDTO();
                upMemberClassDto.setMemberClassId(membershipGrade.getId());
                upMemberClassDto.setMemberId(userId);
                this.baseMapper.updateMemeberClass(upMemberClassDto);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据返回地址ID查询店铺地址详情。
     *
     * @param returnAddrId 返回地址ID，用于查询具体的店铺地址信息。
     * @param token        用户权限令牌，用于验证用户是否有权限查看店铺地址。
     * @return ManageShopAddressQueryVO 对象，包含查询到的店铺地址详情。
     */
    public ManageShopAddressQueryVO queryReturnShop(Integer returnAddrId, String token) {
        // 初始化店铺地址查询结果对象
        ManageShopAddressQueryVO shopAddress = new ManageShopAddressQueryVO();

        // 判断用户是否有权限查看店铺地址
        // 权限判断
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(token);

        // 如果用户无权限，则直接返回空的店铺地址查询结果
        if (!userPermissionsResult.getFlag()) {
            return shopAddress;
        }

        // 根据返回地址ID查询店铺地址详情
        shopAddress = this.baseMapper.selectShopAddressDetail(returnAddrId);

        // 设置店铺地址的名称
        // 设置店铺地址名称
        setShopAddressName(shopAddress);

        // 返回查询到的店铺地址详情
        return shopAddress;
    }

    /**
     * 设置店铺发货地址的名称
     *
     * @param shopAddress
     */
    private void setShopAddressName(ManageShopAddressQueryVO shopAddress) {
        // 初始化一个列表用于存储区域ID
        List<Integer> districtIds = Lists.newArrayList();
        // 添加省、市、区的ID到列表中
        districtIds.add(shopAddress.getShippingAddressProvinceId());
        districtIds.add(shopAddress.getShippingAddressCityId());
        districtIds.add(shopAddress.getShippingAddressAreaId());
        // 根据区域ID列表查询对应的区域名称
        List<String> districtNames = this.baseMapper.selectByIdList(districtIds);
        // 初始化用于存储完整区域名称的字符串
        String shippingAddressArea = "";
        // 遍历区域名称列表，拼接成完整的区域名称
        for (String districtName : districtNames) {
            shippingAddressArea = shippingAddressArea + districtName;
        }
        // 更新购物地址中的区域信息为完整的区域名称
        shopAddress.setShippingAddressArea(shippingAddressArea);

    }

    /** 售后成功  扣除手续费 */
    public void charging(String orderId, List goodsId, String userId, Integer shopId) {
        PlatformoperationVO platformoperation = new PlatformoperationVO();
        //获取商家质保金余额
        Integer selectshop = this.baseMapper.selectshop(shopId);
        if(null != selectshop){
            //查询平台管理规范
            String configValue = baseServiceClient.selectConfigValueFeign("platform_operation_specifications");
            if (configValue != null) {
                platformoperation = JSONObject.parseObject(configValue, PlatformoperationVO.class);
            }
            //是否开启退费手续费
            Integer configEnable = platformoperation.getConfigEnable();
            //扣除手续费总额
            BigDecimal total = BigDecimal.valueOf(0);
            //开启售后手续费
            if (configEnable == 1) {
                //退费通用比例
                BigDecimal universalRatio = BigDecimal.valueOf(platformoperation.getUniversalRatio()).divide(BigDecimal.valueOf(100));
                //查询商品分类
                List<Map> selectgoodscat = this.baseMapper.selectgoodscat(goodsId);
                if (!CollectionUtils.isEmpty(selectgoodscat)) {
                    for (Map map : selectgoodscat) {
                        Integer id = Integer.valueOf(String.valueOf(map.get("goods_id")));
                        Integer catId = Integer.valueOf(String.valueOf(map.get("cat_id")));
                        //查询商品支付金额
                        //商品实付金额
                        BigDecimal selectgoodsprice = orderGoodsMapper.selectgoodsprice(id, orderId);
                        //退费比例
                        Object refund_proportion = map.get("refund_proportion");
                        if (org.springframework.util.StringUtils.isEmpty(refund_proportion)) {
                            //计算退费金额
                            total = total.add(selectgoodsprice.multiply(universalRatio)
                                    .setScale(2, RoundingMode.HALF_UP));

                        } else {
                            BigDecimal refundProportion = BigDecimal.valueOf(Long.parseLong(String.valueOf(map.get("refund_proportion")))).divide(BigDecimal.valueOf(100));
                            //计算退费金额
                            total = total.add(selectgoodsprice.multiply(refundProportion)
                                    .setScale(2, RoundingMode.HALF_UP));
                        }
                    }

                    BigDecimal bigDecimal = new BigDecimal(selectshop);
                    if (bigDecimal.compareTo(new BigDecimal(0)) <= 0 || bigDecimal.compareTo(total) < 0) {
                        //质保金余额不足 暂不赔付
                        insertcom(total, orderId, 0, shopId, goodsId);
                    } else {
                        //罚扣质保金
                        insertcom(total, orderId, 1, shopId, goodsId);
                    }
                    updateshopmoney(shopId, total, userId);

                }
            }
        }
    }

    /** 插入罚扣质保金记录 */
    @Transactional
    public void insertcom(BigDecimal multiply, String orderId, Integer status, Integer shopId, List goodsId) {
        // 初始化一个Map用于存储投诉记录的信息
        HashMap<String, Object> map = new HashMap<>();

        // 构造投诉单号，以"TS"开头，加上日期和5位随机数字
        //生成投诉单号
        StringBuffer id = new StringBuffer("TS");
        DateFormat dFormat3 = new SimpleDateFormat("yyyyMMdd");
        String format = dFormat3.format(new Date());
        Random random = new Random();
        String verificationCode = String.valueOf(random.nextInt(9) + 1);
        for (int i = 0; i < 5; i++) {
            verificationCode += random.nextInt(10);
        }
        id.append(format).append(verificationCode);

        // 将投诉单号、买家ID、投诉状态等信息放入map中
        map.put("complaintsId", id.toString());
        //买家id
        map.put("userId", null);
        map.put("complaintsStatus", 1);
        //退费金额
        map.put("compensationAmount", multiply);
        //是否赔付
        map.put("compensationStatus", status);
        map.put("orderId", orderId);
        map.put("complaintsType", 5);
        map.put("fundDestination", 2);
        map.put("shopId", shopId);

        // 将商品ID列表转换为JSON字符串并放入map中
        String s = JSON.toJSONString(goodsId);
        map.put("complaintsGoodsid", s);
        map.put("createTime", DateUtils.getNowDate());

        // 调用orderComplaintsMapper的addcomplaints方法插入投诉记录
        //插入质保金罚扣记录
        orderComplaintsMapper.addcomplaints(map);
    }

    /** 罚扣质保金 */
    @Transactional
    public Integer updateshopmoney(Integer shopId, BigDecimal amount, String userId) {
        RetentionShopInfoVO selectretention = orderComplaintsMapper.selectretention(shopId);
        //质保金余额-
        BigDecimal payPrice = selectretention.getRetentionMoneyBalance().subtract(amount);

        //质保金可提现
        BigDecimal payPrice1 = payPrice.subtract(selectretention.getRetentionMoneyLimit());
        if (payPrice1.compareTo(new BigDecimal(0)) == -1) {
            payPrice1 = new BigDecimal(0);
        }

        //待补缴
        BigDecimal payPrice2 = new BigDecimal(0);
        if (payPrice.compareTo(selectretention.getRetentionMoneyLimit()) < 0) {
            //待补缴金额
            payPrice2 = selectretention.getRetentionMoneyLimit().subtract(payPrice);
            if (payPrice2.compareTo(new BigDecimal(0)) == -1) {
                payPrice2 = new BigDecimal(0);
            }
        }

        //累计赔付金额
        BigDecimal payPrice3 = selectretention.getAccumulatedCompensation().add(amount);
        HashMap<Object, Object> paymap = new HashMap<>();
        paymap.put("payPrice", payPrice);
        paymap.put("payPrice1", payPrice1);
        paymap.put("payPrice2", payPrice2);
        paymap.put("payPrice3", payPrice3);
        paymap.put("shopId", shopId);
        if (payPrice.compareTo(selectretention.getRetentionMoneyLimit()) >= 0) {
            paymap.put("retentionStatus", 1);
        } else if (payPrice.compareTo(new BigDecimal(0)) >= 0 && payPrice2.compareTo(new BigDecimal(0)) > 0) {
            //发送站内信
            String content = "亲爱的卖家您好，根据平台质保金规则您需补缴" + payPrice2 + " 元质保金，请登录商家后台查询。";
            orderComplaintsMapper.insertSysMessage1("", userId, "", 5,
                    "补缴质保金", content, String.valueOf(shopId));
            paymap.put("retentionStatus", 2);
        } else if (payPrice.compareTo(new BigDecimal(0)) == -1) {
            //发送站内信
            String content = "亲爱的卖家您好，根据平台质保金规则您需补缴" + payPrice2 + " 元质保金，请登录商家后台查询。";
            orderComplaintsMapper.insertSysMessage1("", userId, "", 5,
                    "补缴质保金", content, String.valueOf(shopId));
            //质保金状态
            paymap.put("paymentTime", new Date());
            paymap.put("retentionStatus", 3);
        }
        //更新shopbase表
        if (selectretention.getRetentionStatus() == 3) {
            paymap.put("paymentTime", null);
        }
        return orderComplaintsMapper.updateshopbase(paymap);
    }

    /** 退还优惠卷红包 */
    public void returnred(String orderId, BigDecimal returnCash, List<Integer> returnGoods) {
        try {
            Map map1 = orderBaseMapper.queryorderpaymentamount(orderId);
            //订单实付金额
            BigDecimal orderpay = new BigDecimal(String.valueOf(map1.get("order_payment_amount")));
            //订单邮费
            BigDecimal orderShippingFee = new BigDecimal(String.valueOf(map1.get("order_shipping_fee")));
            //查询红包和代金卷
            WapGoodsSaleSumUpdateVO wap = orderBaseMapper.queryRedByEnvelope(orderId);

            BigDecimal payGoods = orderGoodsMapper.selectPayGoods(orderId, returnGoods);
            BigDecimal feeGoods = orderGoodsMapper.selectFeeGoods(orderId, returnGoods);
            if (returnCash.compareTo(payGoods.subtract(feeGoods)) >= 0) {
                for (Integer returnGood : returnGoods) {
                    Integer voucherId = orderGoodsMapper.selectOne(new LambdaQueryWrapper<OrderGoodsEntity>()
                            .eq(OrderGoodsEntity::getOrderId, orderId)
                            .eq(OrderGoodsEntity::getGoodsId, returnGood)).getVoucherId();
                    if (voucherId != null && !voucherId.toString().equals("0")) {
                        this.baseMapper.updateCouponStuate(voucherId.toString());
                    }
                }
            }

            if (null != wap) {
                //订单总金额 - 运费
                if (returnCash.compareTo(orderpay.subtract(orderShippingFee)) >= 0) {
                    if (!org.springframework.util.StringUtils.isEmpty(wap.getVoucherCode())) {
                        String[] voucherNoList = wap.getVoucherCode().split("-");
                        for (String voucherId : voucherNoList) {
                            //退还代金卷
                            this.baseMapper.updateCouponStuate(voucherId);
                        }
                    }
                }
                if (returnCash.compareTo(orderpay.subtract(orderShippingFee)) >= 0) {
                    //全退返还红包
                    if (!org.springframework.util.StringUtils.isEmpty(wap.getRedpacketCode())) {
                        //根据订单号查询支付单号 因为有可能多个订单公用一个支付单号 如果是多个订单共用用一个支付单号 要判断除当前退单外的其他订单是否是全退
                        String paymentNumber = this.baseMapper.queryOrderPaymentNumber(orderId);
                        //如果多个订单公用一个支付单号 根据当前订单号 和 支付单号 查询是否有其他订单没有全退货
                        List orderIds = this.baseMapper.queryOrderId(orderId, paymentNumber);
                        if (CollectionUtils.isEmpty(orderIds)) {
                            //无其他订单 退还红包
                            this.baseMapper.updateRedPageStuate(wap.getRedpacketCode());
                        } else {
                            //查询其他订单是否全额退款
                            List<Map> maps = this.baseMapper.queryOrderreturn(orderIds);
                            boolean s = true;
                            if (!CollectionUtils.isEmpty(maps)) {
                                for (Map map2 : maps) {
                                    //判断其他订单是否全额退款
                                    //实付金额
                                    BigDecimal paymentAmount = new BigDecimal(String.valueOf(map2.get("order_payment_amount")));
                                    //退款金额
                                    BigDecimal returnCash1 = new BigDecimal(String.valueOf(map2.get("return_cash")));
                                    if (paymentAmount.compareTo(returnCash1) > 0) {
                                        s = false;
                                        break;
                                    }
                                }

                                if (s) {
                                    //退还红包
                                    this.baseMapper.updateRedPageStuate(wap.getRedpacketCode());
                                }
                            }
                        }

                    }
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    /** 计算用户退还积分 */
    public void refundpoints(String orderId, BigDecimal returnCash) {
        //根据orderid 查询应该回退的积分和userid
        Map<String, Object> map = this.baseMapper.queryAddpointsLogByOrderId(orderId);

        //ZZZ 新增积分按比例扣除逻辑
        //订单实付金额
        BigDecimal order_payment_amount = this.baseMapper.queryOrderAmount(orderId);
        //订单退款金额
        BigDecimal returnAmount = returnCash;

        BigDecimal tmp = BigDecimal.ZERO;
        if (null != map && null != map.get("points_log_points")) {
            tmp = returnAmount.multiply(new BigDecimal((Integer) map.get("points_log_points")).divide(order_payment_amount, RoundingMode.HALF_UP));
        }
        Integer tmpInt = tmp.intValue();
        map.put("points_log_points", tmpInt);

        //修改会员积分和会员等级
        MemberMessageListIntegralDTO updateMemberMessageListIntegral = new MemberMessageListIntegralDTO();
        ArrayList<String> list = new ArrayList<>();
        list.add((String) map.get("user_id"));
        updateMemberMessageListIntegral.setUserIdList(list);
        updateMemberMessageListIntegral.setIntegral((Integer) map.get("points_log_points"));
        //1是增加积分 0 是扣除积分
        updateMemberMessageListIntegral.setType(0);
        feignUpdateMemberIntegral(updateMemberMessageListIntegral);
        //添加积分修改记录
        UserPointLogParamDTO userPointLog = new UserPointLogParamDTO();
        userPointLog.setPointsLogType(2);
        userPointLog.setClassId(9);
        userPointLog.setPointsLogTime(new Date());
        userPointLog.setPointsLogDesc("商品仅退款");
        userPointLog.setPointsLogFlag("reg");
        userPointLog.setUserId(map.get("user_id").toString());
        userPointLog.setPointsLogPoints((Integer) map.get("points_log_points"));
        userPointLog.setPointsLogOperate("商品仅退款");
        userPointLog.setOrderId(orderId);
        this.baseMapper.feginAddpointsLog(userPointLog);
    }
}




