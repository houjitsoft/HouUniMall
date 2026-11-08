package com.houjiang.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.base.MemberMessageListIntegralDTO;
import com.houjiang.commons.dto.goods.StorageInventoryFlowGoodsDTO;
import com.houjiang.commons.dto.order.RefundParamDTO;
import com.houjiang.commons.dto.order.WapGoodsSaleSumDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.DateUtils;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.TimeStampUtil;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.vo.goods.GoodsDetailsListVO;
import com.houjiang.commons.vo.goods.WapGoodsSaleSumUpdateVO;
import com.houjiang.commons.vo.order.OrderGoodsBarterVO;
import com.houjiang.commons.vo.shop.GdsShopDetailResultVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.order.dto.*;
import com.houjiang.order.entity.*;
import com.houjiang.order.feign.BaseServiceClient;
// import com.houjiang.order.feign.BusinessServiceClient; // TODO: business 服务已删除
import com.houjiang.order.feign.GoodServiceClient;
import com.houjiang.order.feign.MerchantServiceClient;
import com.houjiang.order.feign.PayMentServiceClient;
import com.houjiang.order.mapper.*;
import com.houjiang.order.service.OrderCommissionService;
import com.houjiang.order.service.OrderReturnPlatformService;
import com.houjiang.order.service.OrderSupplierBaseService;
import com.houjiang.order.vo.OrderGoodsVO;
import com.houjiang.order.vo.OrderReturnPlatformVO;
import com.houjiang.order.vo.ReturnOrderDetailVO;
import com.houjiang.order.vo.ReturnOrderDetailsVO;
import io.seata.core.context.RootContext;
import io.seata.core.exception.TransactionException;
import io.seata.tm.api.GlobalTransactionContext;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static com.houjiang.order.service.impl.OrderBaseServiceImpl.setStorageInventoryFlow;

/**
 * @author Amos
 */
@Service
public class OrderReturnPlatformServiceImpl extends ServiceImpl<OrderReturnPlatformMapper, OrderReturnPlatformEntity>
    implements OrderReturnPlatformService{

    @Resource
    private OrderReturnMapper orderReturnMapper;

    @Resource
    private OrderBaseMapper orderBaseMapper;

    @Resource
    private OrderGoodsMapper orderGoodsMapper;
    @Resource

    @Autowired
    private MerchantServiceClient merchantServiceClient;

    @Autowired
    private PayMentServiceClient payMentServiceClient;

    @Autowired
    private GoodServiceClient goodServiceClient;

    @Autowired
    private OrderReturnServiceImpl orderReturnServiceImpl;

    @Autowired
    private OrderBaseServiceImpl orderBaseService;

    @Autowired
    private OrderSupplierBaseService orderSupplierBaseService;

    @Autowired
    private OrderCommissionService orderCommissionService;

    @Resource
    private OrderCommissionMapper orderCommissionMapper;

    @Autowired
    private BaseServiceClient baseServiceClient;

    @Override
    public ResponseResult insertPlatformIntervention(Map<String, Object> map) {
        // 通过token获取用户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        // 如果用户信息为空，则返回token非法的错误信息
        if (null == ui) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());
        }
        Integer integer = 0;
        // 从map中获取订单号，并转换为字符串类型
        String orderId = String.valueOf(map.get("orderId"));
        // 根据订单号查询退款详情
        //查询退款详情
        ReturnOrderDetailVO returnOrderDetail = orderReturnMapper.queryOrderReturnDetail(orderId);
        // 获取当前时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        Date Application_time = calendar.getTime();
        // 时间加1天，用于计算过期时间
        calendar.add(Calendar.DATE, 1);
        Date calendarTime = calendar.getTime();
        // 如果退款详情不为空，则进行后续处理
        //平台介入
        if (returnOrderDetail != null) {
            // 创建平台介入的退款信息对象
            OrderReturnPlatformVO orderReturnPlatform = new OrderReturnPlatformVO();
            // 根据订单号查询已存在的平台介入退款信息
            OrderReturnPlatformVO orderReturn = this.baseMapper.selectOrderReturnPlatform(orderId);
            // 如果存在，则处理更新情况
            if (orderReturn != null) {
                // 处理买家凭证图片信息
                List<String> complaintsImages = (List<String>) map.get("buyerVoucher");
                String buyer_voucher = JSON.toJSONString(complaintsImages);
                orderReturnPlatform.setBuyerVoucher(buyer_voucher);
                // 设置其他订单相关信息
                orderReturnPlatform.setOrderId(orderId);
                orderReturnPlatform.setBuyerRemarks(String.valueOf(map.get("buyerRemarks")));
                // 根据当前退款状态设置平台介入后的退款状态
                if (orderReturn.getReturnState() == 3 || orderReturn.getReturnState() == 10) {
                    orderReturnPlatform.setReturnState(Integer.parseInt(returnOrderDetail.getReturnState()));
                } else {
                    orderReturnPlatform.setReturnState(null);
                }
                orderReturnPlatform.setReturnMethodType(returnOrderDetail.getReturnMethodStatus());
                // 如果当前介入状态为处理中或已同意，则设置过期时间和申请时间，并更新退款状态
                if (orderReturn.getInterventionState() == 3 || orderReturn.getInterventionState() == 2) {
                    orderReturnPlatform.setExpriationTime(calendarTime);
                    orderReturnPlatform.setApplicationTime(Application_time);
                    orderReturnMapper.updateOrderReturnState(orderId, calendarTime);
                }
                // 设置平台介入状态为待处理
                orderReturnPlatform.setInterventionState(0);
                // 更新平台介入的退款信息
                integer = this.baseMapper.updateOrderReturnBuyerVoucher(orderReturnPlatform);
            } else {
                // 如果不存在，则处理插入新记录的情况
                // 处理买家凭证图片信息
                List<String> complaintsImages = (List<String>) map.get("buyerVoucher");
                String buyer_voucher = JSON.toJSONString(complaintsImages);
                // 设置相关订单和退款信息
                orderReturnPlatform.setOrderId(returnOrderDetail.getOrderId());
                orderReturnPlatform.setReturnCode(returnOrderDetail.getReturnCode());
                orderReturnPlatform.setShopName(returnOrderDetail.getShopName());
                orderReturnPlatform.setShopId(returnOrderDetail.getShopId());
                orderReturnPlatform.setSellerUserId(returnOrderDetail.getShopId());
                orderReturnPlatform.setBuyerUserId(returnOrderDetail.getBuyerUserId());
                orderReturnPlatform.setReturnState(Integer.parseInt(returnOrderDetail.getReturnState()));
                orderReturnPlatform.setInterventionState(0);
                orderReturnPlatform.setApplicationTime(Application_time);
                orderReturnPlatform.setExpriationTime(calendarTime);
                orderReturnPlatform.setBuyerVoucher(buyer_voucher);
                orderReturnPlatform.setBuyerRemarks(String.valueOf(map.get("buyerRemarks")));
                orderReturnPlatform.setReturnMethodType(returnOrderDetail.getReturnMethodStatus());
                // 插入新的平台介入退款信息
                integer = this.baseMapper.insertOrderReturnPlatform(orderReturnPlatform);

                // 更新退款状态
                orderReturnMapper.updateOrderReturnState(orderId, calendarTime);
            }
        }
        // 如果有记录被更新或插入，则继续处理订单状态和平台介入状态的更新
        if (integer > 0) {
            // 更新订单的退款方式状态
            orderBaseMapper.updateOrderBaseReturnMethodStatus(returnOrderDetail.getReturnMethodStatus(), orderId);
            // 更新售后单状态为平台介入
            //更改售后单状态为平台介入
            Integer integer1 = orderReturnMapper.update(null,new LambdaUpdateWrapper<OrderReturnEntity>()
                                .set(OrderReturnEntity::getReturnState, CommonType.returnState.PLATFORM_INTERVENTION.getCode())
                                .eq(OrderReturnEntity::getOrderId,orderId));
            // 如果更新成功，则返回成功结果
            if (integer1 > 0) {
                return ResponseResult.success(returnOrderDetail.getReturnMethodStatus());
            }
        }
        // 如果以上操作有任何失败，则返回失败结果
        return ResponseResult.fail(returnOrderDetail == null ? null :returnOrderDetail.getReturnMethodStatus());

    }

    /**
     * 根据订单号检查是否有平台介入的退货申请。
     *
     * @param map 包含订单号的Map对象。
     * @return ResponseResult 对象，表示查询结果。
     *         如果有平台介入的退货申请，返回包含相关信息的成功响应。
     *         如果没有平台介入的退货申请，返回包含相关信息的成功响应。
     */
    @Override
    public ResponseResult checkplatform(Map<String, Object> map) {
        // 从传入的map中获取订单号，并转换为字符串类型
        String orderId = String.valueOf(map.get("orderId"));

        // 查询是否有平台介入的退货申请，使用LamdaQueryWrapper构造查询条件
        // 查询平台介入
        Integer orderReturnPlatform = Math.toIntExact(this.baseMapper.selectCount(new LambdaQueryWrapper<OrderReturnPlatformEntity>()
                .eq(OrderReturnPlatformEntity::getOrderId, orderId)));

        // 根据查询结果判断是否有平台介入，并返回相应的ResponseResult对象
        if (orderReturnPlatform > 0) {
            // 有平台介入
            return ResponseResult.success(1, "有平台介入信息", 1);
        } else {
            // 无平台介入
            return ResponseResult.success(1, "无平台介入信息", 2);
        }
    }

    @Override
    public ResponseResult queryPlatformIntervention(Map<String, Object> map) {
        // 从map中获取token，用于验证用户身份
        String token = (String) map.get("token");
        // 根据token解析并获取平台用户信息
        UserInfoVO userInfoVO = ParseTokenUtils.getPlatformUser(token);
        // 如果用户信息为空，则返回token无效的错误信息
        if (userInfoVO == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }

        // 从map中获取orderId，用于后续查询订单退款详情
        String orderId = String.valueOf(map.get("orderId"));
        // 根据orderId查询平台介入的订单退款信息
        // 查询平台介入
        OrderReturnPlatformVO orderReturnPlatform = this.baseMapper.selectOrderReturnPlatform(orderId);
        // 如果查询结果不为空，则计算店铺评分
        if (orderReturnPlatform != null) {
            //计算店铺评分
            Map<Object, Object> map1 = new HashMap();
            map1.put("shopId", orderReturnPlatform.getShopId());
            ResponseResult shopScore = merchantServiceClient.shopScore(map1);
            Map data = (Map) shopScore.getData();
            Double evaluationDeliverycredit = (Double) data.get("evaluationDeliverycredit");
            Double evaluation_desccredit = (Double) data.get("evaluationDesccredit");
            Double evaluation_servicecredit = (Double) data.get("evaluationServicecredit");
            Double shopRating = (evaluation_servicecredit + evaluation_desccredit + evaluationDeliverycredit) / 3;
            BigDecimal tmp = new BigDecimal(shopRating);
            shopRating = tmp.setScale(1, RoundingMode.HALF_UP).doubleValue();
            orderReturnPlatform.setShopRating(String.valueOf(shopRating));

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
        }


        // 根据orderId查询订单退款详情
        ReturnOrderDetailVO returnOrderDetail = this.baseMapper.queryOrderReturnDetail(orderId);
        // 根据orderId查询售后商品详情
        List<GoodsDetailsListVO> orderGoods = this.baseMapper.queryGoodsList1(orderId);
        // 查询该店铺的历史维权次数，并设置到orderReturnPlatform对象中
        Integer integer = this.baseMapper.queryShopIdByCount(returnOrderDetail.getShopId());
        orderReturnPlatform.setHistoricalCount(integer);
        // 如果查询到售后商品详情不为空，则将详情设置到返回对象中，并返回成功结果
        if (orderGoods != null && !orderGoods.isEmpty()) {
            if(returnOrderDetail.getReturnMethodStatus() == 4){
                for (GoodsDetailsListVO goods : orderGoods) {
                    // 调用服务客户端查询换货商品信息，并设置到商品对象中
                    ResponseResult<OrderGoodsBarterVO> barterGoods = goodServiceClient.selectBarterGoods(goods.getBarterGoodsId());
                    goods.setBarterGoods(barterGoods.getData());
                }
            }
            returnOrderDetail.setOrderGoodsDetail(orderGoods);
            orderReturnPlatform.setReturnOrderDetail(returnOrderDetail);
            return ResponseResult.success(orderReturnPlatform);
        }
        // 如果没有查询到售后商品详情，则返回失败结果
        return ResponseResult.fail(2, "失败");
    }

    /**
     * 处理平台审批请求。 退款
     *
     * @param map 包含审批请求相关数据的Map，其中必须包含"token"键。
     * @return 根据审批结果返回相应的ResponseResult对象。
     * @see ResponseResult
     * @see BizCodeEnume
     */
    @Override
    public ResponseResult forPlatformApprove(Map<String, String> map) throws TransactionException  {
        // 从请求参数中获取用户令牌
        String token = map.get("token");
        // 根据令牌解析用户信息，如果令牌无效，返回失败结果
        UserInfoVO userInfoVO = ParseTokenUtils.getPlatformUser(token);
        if (userInfoVO == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 调用具体业务方法处理审批逻辑，返回审批结果（布尔值）
        Boolean torf = forPlatformApproveBoolean(map);
        // 如果审批失败，返回失败结果
        if (!torf) {
            return ResponseResult.fail(2,"申请退款失败");
        }
        // 审批成功，返回成功结果
        return ResponseResult.success(1,"申请退款成功");
    }

    @Override
    public ResponseResult returnforPlatformApprove(Map<String, String> map) throws TransactionException {
        // 从传入的map中获取token，用于后续的用户信息解析
        String token = map.get("token");
        // 解析token，获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(token);
        // 如果用户信息为空，表示token无效，返回相应的错误响应
        if (userInfo == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        OrderReturnEntity returnEntity = orderReturnMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>()
                .eq(OrderReturnEntity::getReturnCode, map.get("returnCode")));
        GdsShopDetailResultVO gdsShopDetailResultVO = merchantServiceClient.queryShopinfoByShopId(returnEntity.getShopId());
        map.put("userId", returnEntity.getSellerUserId());
        map.put("shopId", returnEntity.getShopId().toString());
        map.put("shopType", gdsShopDetailResultVO != null ? gdsShopDetailResultVO.getShopType().toString() : "0");
        map.put("returnState","13");

        // 调用forPlatformApproveBoolean方法，根据业务逻辑判断是否通过审核
        Boolean torf = orderReturnServiceImpl.retForSellerReceiveBoolean(map,true);
        OrderReturnPlatformVO platform = new OrderReturnPlatformVO();
        //修改平台介入表
        platform.setProcessingResults(Integer.valueOf(map.get("type")));
        platform.setInterventionState(2);
        platform.setProcessingTime(new Date());
        platform.setOrderId(map.get("orderId"));
        this.baseMapper.examineIntervention(platform);

        // 如果审核不通过，返回相应的错误响应
        if (!torf) {
            return ResponseResult.fail(2,"申核失败");
        }
        // 审核通过，返回成功响应
        return ResponseResult.success(1,"申核成功");

    }

    @Override
    public ResponseResult queryReturnPlatform(OrderReturnPlatformListDTO map) {
        // 尝试处理分页查询退货平台信息的请求
        try {
            // 设置分页默认值，防止前端未传参导致NPE
            Integer page = map.getPage() != null ? map.getPage() : 1;
            Integer limit = map.getLimit() != null ? map.getLimit() : 10;
            // 初始化分页参数
            Page<OrderReturnPlatformListDTO> pageObj = new Page<>(page, limit);
            // 执行查询操作，返回分页后的退货平台信息
            IPage<OrderReturnPlatformListDTO> selectreturnplatform = this.baseMapper.selectreturnplatform(pageObj, map);
            // 构建并返回查询结果的成功响应
            return ResponseResult.success(PageUtils.getPage(selectreturnplatform));
        } catch (Exception e) {
            // 捕获查询过程中可能出现的异常，返回异常信息和错误代码
            return ResponseResult.fail(2, e.getMessage());
        }

    }

    /**
     * 根据订单号检查平台介入信息。
     *
     * @param map 包含订单号等信息的参数映射。
     * @return 根据查询结果返回不同的响应结果。
     *         如果平台介入信息存在，返回成功响应并包含相关信息。
     *         如果平台介入信息不存在，返回失败响应。
     */
    @Override
    public ResponseResult checkPlatformInfo(Map<String, Object> map) {
        // 从参数映射中获取订单号
        String orderId = String.valueOf(map.get("orderId"));

        // 查询指定订单是否由平台介入处理
        //查询平台介入
        Integer orderReturnPlatform = this.baseMapper.selectOrderReturnPlatform1(orderId);

        // 如果查询结果大于0，说明有平台介入，返回相应的成功响应
        if (orderReturnPlatform > 0){
            return ResponseResult.success(1,"有平台介入信息",1);
        }
        // 如果查询结果为0，说明无平台介入，返回相应的失败响应
        return ResponseResult.fail(2,"无平台介入信息",2);
    }

    public Boolean forPlatformApproveBoolean(Map<String, String> paraMap)throws TransactionException {
        Map<String, Object> orderGoodsMap = new HashMap<String, Object>();
        Map<String, Object> orderBaseMap = new HashMap<String, Object>();
        Map<String, Object> assetDetailMap = new HashMap<>();
        OrderReturnPlatformVO platform = new OrderReturnPlatformVO();
//        String orderId = orderReturnMapper.getOrderIdByReturnCode(paraMap.get("returnCode"));
        OrderReturnEntity returnEntity = orderReturnMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>()
                .eq(OrderReturnEntity::getReturnCode, paraMap.get("returnCode")));
        String orderId = returnEntity.getOrderId();
        OrderBaseEntity entity = orderBaseMapper.selectById(orderId);
        paraMap.put("userId", returnEntity.getSellerUserId());
        paraMap.put("shopId", returnEntity.getShopId().toString());
        Integer orderStatus = orderReturnMapper.queryOrderStatus(orderId);
        Integer virtualGoods = 1;
        //获取退货数量
        Map<String, Object> goodsNumMap = new HashMap<>();
        List<Map<String, Object>> goodsNum = orderReturnMapper.queryBuyNumByorderId(orderId);
        goodsNumMap.put("goodsNum", goodsNum);
        String buyerUserId = goodsNum.get(0).get("buyer_user_id").toString();
        //判断是否是虚拟订单 且没有被核销
        Map<String, Object> map = orderReturnMapper.selectisVirtualOrderByTime(orderId);
        if (null != map) {
            virtualGoods = 0;
            //如果该虚拟订单已过期 且过期不退款直接返回false
            Integer result = orderReturnMapper.queryisVirtualOrderByTime(orderId);
            if( 0 != result){
                return false;
            }
        }
        String returnGoodsName = orderReturnMapper.queryReturnGoodsNameByOrderId(orderId);
        // 发送消息模板
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("orderId", paraMap.get("orderId"));
        paramMap.put("goodsName", returnGoodsName);
        List<String> msglist = new ArrayList<>();
        msglist.add(paraMap.get("orderId"));
        paramMap.put("list", msglist);
        int type = Integer.parseInt(paraMap.get("type"));
        // 平台同意
        if (1 == Integer.parseInt(paraMap.get("type"))) {
            //查询用户售后商品
            List<Integer> returnGoods = this.baseMapper.queryReturnGoods(orderId);
            BigDecimal returnCash;
            //用户收到货物退款
            if(orderStatus == 6){
                orderGoodsMap.put("returnCode", paraMap.get("returnCode"));
                orderGoodsMap.put("returnShopHandle", 2);
                //4-卖家收到货物
                orderGoodsMap.put("returnState", 2);
                orderGoodsMap.put("returnShopMessage", paraMap.get("returnShopMessage"));
                orderGoodsMap.put("returnFinishTime", TimeStampUtil.getNowDate());

                //	 1.2修改商家同意订单状态为 6
                orderBaseMap.put("orderId",orderId);
                //9.已退款
                orderBaseMap.put("orderStatus","9");
                //2是退款完成
                orderBaseMap.put("orderRefundStatus","2");
                //2是退款完成
                orderBaseMap.put("orderReturnStatus","0");
                Integer torf = orderReturnMapper.updateOrderBaseById(orderBaseMap);

                //修改平台介入表
                platform.setProcessingResults(type);
                platform.setInterventionState(2);
                platform.setProcessingTime(new Date());
                platform.setOrderId(orderId);
                this.baseMapper.examineIntervention(platform);

                Map<String, Object> updateGoodsMap = new HashMap<String, Object>();
                updateGoodsMap.put("orderId",orderId);
                updateGoodsMap.put("goodsRefundStatus",2);
                updateGoodsMap.put("goodsReturnStatus",0);
                updateGoodsMap.put("orderGoodsStatus",9);
                orderReturnMapper.updateOrderGoodsById(updateGoodsMap);

                String uOrderId = orderReturnMapper.queryPaymentNumber(orderId);
                returnCash = orderReturnMapper.queryReturnCash(orderId);

                List<Integer> orderTradeStatus = orderReturnMapper.queryOrderRecordStatus(uOrderId);
                RefundParamDTO refundPara = new RefundParamDTO();
                boolean isRefundSuccess = false;
                if(torf == 1){
                    refundPara = orderReturnMapper.queryRefundPara(orderId);
                    refundPara.setOrderTradeStatus(orderTradeStatus);
                    refundPara.setReturnCash(returnCash);
                    isRefundSuccess = payMentServiceClient.waitShipmentsRefund(refundPara);
                    //退款更新商品销量
                    orderReturnServiceImpl.updateGoodsSalenum(goodsNumMap);
                }
                if(!isRefundSuccess){
                    // 回滚
                    GlobalTransactionContext.reload(RootContext.getXID()).rollback();
                    return false;
                }
                if (1 != torf ) {
                    // 回滚
                    GlobalTransactionContext.reload(RootContext.getXID()).rollback();
                    return false;
                }
                //退还积分
                orderReturnServiceImpl.refundpoints(orderId,returnCash);

                //退还红包优惠卷
                orderReturnServiceImpl.returnred(orderId,returnCash,returnGoods);
                //已发货退款  且是普通商品 不能是虚拟商品
            }else if(orderStatus == 4 && virtualGoods == 1){
                orderGoodsMap.put("returnCode", paraMap.get("returnCode"));
                orderGoodsMap.put("returnShopHandle", 2);
                orderGoodsMap.put("returnState", 2);
                orderGoodsMap.put("returnShopMessage", paraMap.get("returnShopMessage"));
                orderGoodsMap.put("returnShopTime", new Date());

                //	 1.2修改商家同意订单状态为 6
                orderBaseMap.put("orderId",orderId);
                orderBaseMap.put("orderStatus","9");
                orderBaseMap.put("orderRefundStatus","2");
                orderBaseMap.put("orderReturnStatus","0");
                Integer torf = orderReturnMapper.updateOrderBaseById(orderBaseMap);
                Map<String, Object> updateGoodsMap = new HashMap<String, Object>();
                updateGoodsMap.put("orderId",orderId);
                updateGoodsMap.put("goodsRefundStatus",2);
                orderReturnMapper.updateOrderGoodsById(updateGoodsMap);

                String uOrderId = orderReturnMapper.queryPaymentNumber(orderId);
                returnCash = orderReturnMapper.queryReturnCash(orderId);
                List<Integer> orderTradeStatus = orderReturnMapper.queryOrderRecordStatus(uOrderId);
                RefundParamDTO refundPara = new RefundParamDTO();
                boolean isRefundSuccess = false;
                if(torf == 1){
                    refundPara = orderReturnMapper.queryRefundPara(orderId);
                    refundPara.setOrderTradeStatus(orderTradeStatus);
                    refundPara.setReturnCash(returnCash);
                    isRefundSuccess = payMentServiceClient.waitShipmentsRefund(refundPara);
                    //退款更新商品销量
                    orderReturnServiceImpl.updateGoodsSalenum(goodsNumMap);
                }
                if(!isRefundSuccess){
                    // 回滚
                    GlobalTransactionContext.reload(RootContext.getXID()).rollback();
                    return false;
                }

                if (1 != torf ) {
                    // 回滚
                    GlobalTransactionContext.reload(RootContext.getXID()).rollback();
                    return false;
                }
                //未发货退款  虚拟商品订单可以进来
            }else if(orderStatus == 3 || virtualGoods == 0){
                orderGoodsMap.put("returnCode", paraMap.get("returnCode"));
                //平台同意买家审核通过
                orderGoodsMap.put("returnState", 2);
                orderGoodsMap.put("returnFinishTime", TimeStampUtil.getNowDate());

                //	 1.2修改平台同意订单状态为 9
                orderBaseMap.put("orderId",orderId);
                //已退款
                orderBaseMap.put("orderStatus","9");
                orderBaseMap.put("orderRefundStatus","2");
                orderBaseMap.put("orderReturnStatus","0");
                orderBaseMap.put("orderFinishedTime",TimeStampUtil.getNowDate());
                Integer torf = orderReturnMapper.updateOrderBaseById(orderBaseMap);
                Map<String, Object> updateGoodsMap = new HashMap<String, Object>();
                updateGoodsMap.put("orderId",orderId);
                updateGoodsMap.put("goodsRefundStatus",2);
                updateGoodsMap.put("goodsReturnStatus",0);
                updateGoodsMap.put("orderGoodsStatus",9);
                orderReturnMapper.updateOrderGoodsById(updateGoodsMap);

                //修改平台介入表
                platform.setProcessingResults(type);
                platform.setInterventionState(2);
                platform.setProcessingTime(new Date());
                platform.setOrderId(orderId);
                this.baseMapper.examineIntervention(platform);

                String uOrderId = orderReturnMapper.queryPaymentNumber(orderId);
                returnCash = orderReturnMapper.queryReturnCash(orderId);

                List<Integer> orderTradeStatus = orderReturnMapper.queryOrderRecordStatus(uOrderId);
                RefundParamDTO refundPara = new RefundParamDTO();
                boolean isRefundSuccess = false;
                if(torf == 1){
                    refundPara = orderReturnMapper.queryRefundPara(orderId);
                    refundPara.setOrderTradeStatus(orderTradeStatus);
                    refundPara.setReturnCash(returnCash);
                    orderReturnServiceImpl.updateGoodsSalenum(goodsNumMap);
                    if(refundPara.getPayCode() != 12) {
                        isRefundSuccess = payMentServiceClient.waitShipmentsRefund(refundPara);
                    }else{
                        isRefundSuccess = true;
                    }
                }
                if(!isRefundSuccess){
                    // 回滚
                    GlobalTransactionContext.reload(RootContext.getXID()).rollback();
                    return false;
                }
                if (1 != torf ) {
                    // 回滚
                    GlobalTransactionContext.reload(RootContext.getXID()).rollback();
                    return false;
                }
                //退款成功后 修改库存
                //获取goodsid commonid 购买数量
                List<WapGoodsSaleSumUpdateVO> wapGood = orderReturnMapper.queryGoodsXinXi(orderId);
                for (WapGoodsSaleSumUpdateVO wapGoodsSaleSumUpdate : wapGood) {
                    orderReturnMapper.updateCommonStockByGoodsId(wapGoodsSaleSumUpdate);
                    orderReturnMapper.updateGoodsStockByGoodsId(wapGoodsSaleSumUpdate);

                    //遍历集合减去销量
                    orderReturnMapper.lessgoodsBySales(wapGoodsSaleSumUpdate);
                }
                //退还红包优惠卷
                orderReturnServiceImpl.returnred(orderId,returnCash,returnGoods);
            }else{
                return false;
            }
            // 售后分销佣金处理
            orderReturnServiceImpl.commissionReturn(orderId, returnGoods);
            // 代发货订单 经销商和供应商佣金处理
            Integer i = 1;
            BigDecimal reutrnOrderGoodsAmount = new BigDecimal(0);
            for (Integer returnGood : returnGoods) {
                Map<String, Object> updateGoodsReturnCommissionMap = new HashMap<>();
                updateGoodsReturnCommissionMap.put("orderId", orderId);
                OrderGoodsEntity orderGoodsEntity = orderGoodsMapper.selectOne(new LambdaQueryWrapper<OrderGoodsEntity>()
                        .eq(OrderGoodsEntity::getOrderId, orderId)
                        .eq(OrderGoodsEntity::getGoodsId, returnGood));
                if (entity.getDealerCommission() != null && entity.getDealerCommission() > 0){
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
                orderReturnMapper.updateOrderGoodsById1(updateGoodsReturnCommissionMap);
            }

            // 由于退款 是全订单退款 所以退红包
            orderReturnMapper.updateHongbaoStart(orderId);


            //插入我的余额明细表
            assetDetailMap.put("orderId",orderId);
            assetDetailMap.put("detailType",2);
            assetDetailMap.put("assetType",1);
            assetDetailMap.put("assetAmount",returnCash);
            AssetDirectionAndPayTypeInfoDTO assetInfo = orderReturnMapper.getAssetDirectionAndPayTypeInfo(orderId);
            if(null != assetInfo){
                assetDetailMap.put("assetDirection",assetInfo.getAssetDirection());
                assetDetailMap.put("payType",assetInfo.getPayType());
                assetDetailMap.put("userId",assetInfo.getUserId());
                orderReturnMapper.insertDetail(assetDetailMap);
            //判断是否是虚拟订单 且没有被核销
            }
            if(null  != map){
                virtualGoods = 0;
                //是否核销 0是 1否 2已过期
                Integer isCancel = Integer.parseInt(map.get("isCancel").toString());
                if(null != isCancel && 0 != isCancel) {
                    //如果该虚拟订单已过期 且过期不退款直接返回false
                    Integer result = orderReturnMapper.queryisVirtualOrderByTime(orderId);
                    if (0 != result) {
                        return false;
                    }
                    boolean returnFlag = false;
                    //未核销且在有效期内
                    if( 1 == isCancel){
                        Integer result1 = orderReturnMapper.queryisVirtualOrderByTime1(orderId);
                        if(null != result1 && result1 >0){
                            returnFlag = true;
                        }
                        //过期且可过期退款
                    }else if( 2 == isCancel){
                        Integer result2 = orderReturnMapper.queryisVirtualOrderByTime2(orderId);
                        if(null != result2 && result2 >0){
                            returnFlag = true;
                            //退还虚拟订单佣金
                            //如果该虚拟订单已过期 且过期允许退款就扣除积分
                            //扣减积分
                            //修改会员积分和会员等级
                            //根据orderid 查询应该回退的积分和userid
                            Map<String, Object> tmpMap = orderReturnMapper.queryAddpointsLogByOrderId(orderId);
                            if (null != tmpMap) {
                                MemberMessageListIntegralDTO updateMemberMessageListIntegral = new MemberMessageListIntegralDTO();
                                ArrayList<String> list = new ArrayList<>();
                                list.add((String) tmpMap.get("user_id"));
                                updateMemberMessageListIntegral.setUserIdList(list);
                                updateMemberMessageListIntegral.setIntegral((Integer) tmpMap.get("points_log_points"));
                                //1是增加积分 0 是扣除积分
                                updateMemberMessageListIntegral.setType(0);
                                orderReturnServiceImpl.feignUpdateMemberIntegral(updateMemberMessageListIntegral);
                                //添加积分修改记录
                                UserPointLogParamDTO userPointLog = new UserPointLogParamDTO();
                                userPointLog.setPointsLogType(2);
                                userPointLog.setClassId(9);
                                userPointLog.setPointsLogTime(new Date());
                                userPointLog.setPointsLogDesc("商品退款");
                                userPointLog.setPointsLogFlag("reg");
                                userPointLog.setUserId((String) tmpMap.get("user_id"));
                                userPointLog.setPointsLogPoints((Integer) tmpMap.get("points_log_points"));
                                userPointLog.setPointsLogOperate("商品退款");
                                userPointLog.setOrderId(paraMap.get("orderId"));
                                orderReturnMapper.feginAddpointsLog(userPointLog);
                            }

                        }
                    }
                    if(returnFlag){
                        Map<String, Object> dataMap = orderReturnMapper.queryOrderReturnInfoById(orderId);
                        if (entity.getOrderPaymentAmount().compareTo(returnCash) > 0) {
                            BigDecimal lv = returnEntity.getReturnCash().divide(entity.getOrderPaymentAmount(), 2, RoundingMode.HALF_UP);
                            if(entity.getOrderCommissionFee() != null) {
                                dataMap.put("orderCommissionFee", BigDecimal.valueOf(entity.getOrderCommissionFee()).multiply(lv).setScale(2, RoundingMode.HALF_UP));
                            }
                            if(entity.getBaseMemberDiscount() != null) {
                                dataMap.put("baseMemberDiscount", BigDecimal.valueOf(entity.getBaseMemberDiscount()).multiply(lv).setScale(2, RoundingMode.HALF_UP));
                            }
                            if(entity.getDistributionDiscount() != null) {
                                dataMap.put("distributionDiscount", entity.getDistributionDiscount().multiply(lv).setScale(2, RoundingMode.HALF_UP));
                            }}
                        dataMap.put("orderId",orderId);
                        orderReturnMapper.updateOrderReturnInfoById(dataMap);
                    }

                }
            }
            //罚扣质保金
            orderReturnServiceImpl.charging(orderId,returnGoods,paraMap.get("userId"),
                    Integer.valueOf(paraMap.get("shopId")));

            //退回plus折扣额度
            try {
                orderBaseService.plusdis(orderId,buyerUserId,returnGoods);
            } catch (ParseException e) {

            }
            // 设置消息模板信息
            paramMap.put("templateId", CommonType.messageTemplate.CODE_D.getCode());
            paramMap.put("userId", entity.getBuyerUserId());

            baseServiceClient.sendMessageTemplate(paramMap);

        } else {
            // 平台不同意
            orderGoodsMap.put("returnCode", paraMap.get("returnCode"));
            if(3 == returnEntity.getReturnMethodStatus()){
                orderGoodsMap.put("returnState", 13);
            }else{
                orderGoodsMap.put("returnState", 8);
            }
            orderGoodsMap.put("closeTime", new Date());
            Map<String, Object> updateMap = new HashMap<String, Object>();
            Integer torfOne = null;
            Integer torfTwo = null;
            updateMap.put("orderId", orderId);
            // 原值：2，现改为0，用户可持续申请
            updateMap.put("orderRefundStatus", "0");
            if(orderStatus == 6) {
                updateMap.put("orderStatus", 6);
            }else if(orderStatus == 3 || virtualGoods == 0){
                updateMap.put("orderStatus", 3);
            }
            updateMap.put("orderReturnStatus",0);
            updateMap.put("orderRefundStatus",0);
            //添加商家拒绝退款时间
            String businessTime =String.valueOf(System.currentTimeMillis());
            String businessRefundTime=TimeStampUtil.stampToDate(businessTime);
            torfOne = orderReturnMapper.updateOrderBaseById(updateMap);

            //修改平台介入表
            platform.setProcessingResults(type);
            platform.setInterventionState(2);
            platform.setProcessingTime(new Date());
            platform.setOrderId(orderId);
            this.baseMapper.examineIntervention(platform);

            // 设置消息模板信息
            ResourceInfoDTO resourceInfo = this.baseMapper.getReturnInfo(paraMap.get("returnCode"));
            paramMap.put("templateId", CommonType.messageTemplate.CODE_N.getCode());
            paramMap.put("userId", resourceInfo.getBuyerUserId());
            baseServiceClient.sendMessageTemplate(paramMap);

            //更新record表状态
            Map<String,String> feignMap = new HashMap<>();
            feignMap.put("orderId",orderId);
            // 原值：7
            feignMap.put("recordStatus","8");
            payMentServiceClient.updateRefuse(feignMap);
            if (1 != torfOne ) {
                GlobalTransactionContext.reload(RootContext.getXID()).rollback();
                return false;
            }
        }
        Integer torf = orderReturnMapper.updateOrderReturnById(orderGoodsMap);
        if (1 == torf) {
            return true;
        }
        return false;
    }

    @Override
    public Boolean platformUpdate() {
        try {
            // 查询需要更新过期时间的退货订单详情
            List<ReturnOrderDetailVO> returnOrderDetails = orderReturnMapper.updateplatform1();
            // 获取当前时间的Calendar实例
            Calendar instance = Calendar.getInstance();
            // 如果退货订单详情不为空
            if (!CollectionUtils.isEmpty(returnOrderDetails)){
                // 遍历每个退货订单详情
                for (ReturnOrderDetailVO returnOrderDetail : returnOrderDetails) {
                    // 设置Calendar的时间为订单的过期时间
                    instance.setTime(returnOrderDetail.getExpirationTime());
                    // 将过期时间增加7天
                    instance.add(Calendar.DATE,7);
    
                    // 更新订单的过期时间
                    orderReturnMapper.updateplatform(returnOrderDetail.getOrderId(),instance.getTime());
                }

            }
        } catch (Exception e) {
            System.out.println(e);
        }
        // 返回true，表示更新操作已完成
        return true;

    }

    @Override
    public Boolean updateTimeOutReturnplatform() {
        //退货
        List<ReturnOrderDetailsVO> orderDetails = this.baseMapper.updateTimeOutReturnplatform();
        shoptimeout(orderDetails);

        //退款
        List<ReturnOrderDetailsVO> detailList =  this.baseMapper.updateTimeOutReturnfundplatform();
        Map<String, Object> orderGoodsMap = new HashMap<String, Object>();
        Map<String, Object> orderBaseMap = new HashMap<String, Object>();
        Map<String,Object> assetDetailMap = new HashMap<>();
        if (detailList != null && detailList.size() > 0 ){
            for (ReturnOrderDetailsVO map1 : detailList) {
                try {
                    String orderId = map1.getOrderId();
                    String returnCode = map1.getReturnCode();
                    OrderBaseEntity orderBaseEntity=orderBaseMapper.selectById(orderId);
                    Integer orderStatus = orderBaseEntity.getOrderStatus();
                    Integer virtualGoods = 1;
                    //获取退货数量
                    Map<String,Object> goodsNumMap = new HashMap<>();
                    List<Map<String,Object>> goodsNum =  orderReturnMapper.queryBuyNumByorderId(orderId);
                    goodsNumMap.put("goodsNum",goodsNum);
                    //判断是否是虚拟订单 且没有被核销
                    Map<String, Object> map = orderReturnMapper.selectisVirtualOrderByTime(orderId);
                    if(null  != map){
                        virtualGoods = 0;
                        //如果该虚拟订单已过期 且过期不退款直接返回false
                        Integer result = orderBaseMapper.queryisVirtualOrderByTime(orderId);
                        if( 0 != result){
                            return false;
                        }
                    }
                    BigDecimal returnCash;
                    //查询用户售后商品
                    List<Integer> returnGoods = this.baseMapper.queryReturnGoods(orderId);
                    if(orderStatus == 6){
                        //用户收到货物退款
                        orderGoodsMap.put("returnCode", returnCode);
                        orderGoodsMap.put("returnShopHandle", 2);
                        orderGoodsMap.put("returnState", 2);


                        //	 1.2修改商家同意订单状态为 6
                        orderBaseMap.put("orderId",orderId);
                        //9.已退款
                        orderBaseMap.put("orderStatus","9");
                        //2是退款完成
                        orderBaseMap.put("orderRefundStatus","2");
                        //2是退款完成
                        orderBaseMap.put("orderReturnStatus","0");
                        Integer torf = orderBaseMapper.updateOrderBaseById(orderBaseMap);

                        Map<String, Object> updateGoodsMap = new HashMap<String, Object>();
                        updateGoodsMap.put("orderId",orderId);
                        updateGoodsMap.put("goodsRefundStatus",2);
                        updateGoodsMap.put("goodsReturnStatus",0);
                        orderReturnMapper.updateOrderGoodsById(updateGoodsMap);

                        String uOrderId = orderBaseEntity.getPaymentNumber();
                        returnCash= orderReturnMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>()
                                .eq(OrderReturnEntity::getOrderId,orderId)).getReturnCash();

                        List<Integer> orderTradeStatus =orderReturnMapper.queryOrderRecordStatus(orderBaseEntity.getPaymentNumber());
                        RefundParamDTO refundPara = new RefundParamDTO();
                        boolean isRefundSuccess = false;
                        if(torf == 1){
                            refundPara = orderReturnMapper.queryRefundPara(orderId);
                            refundPara.setOrderTradeStatus(orderTradeStatus);
                            refundPara.setReturnCash(returnCash);
                            isRefundSuccess = payMentServiceClient.waitShipmentsRefund(refundPara);
                            //退款更新商品销量
                            orderReturnServiceImpl.updateGoodsSalenum(goodsNumMap);
                        }
                        if(!isRefundSuccess){
                            // 回滚
                            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                            return false;
                        }
                    }else if(orderStatus == 3 || virtualGoods == 0){
                        //未发货退款  虚拟商品订单可以进来
                        orderGoodsMap.put("returnCode", returnCode);
                        orderGoodsMap.put("returnShopHandle", 2);
                        //卖家审核通过
                        orderGoodsMap.put("returnState", 2);
                        orderGoodsMap.put("returnFinishTime", DateUtils.getNowDate());

                        //	 1.2修改商家同意订单状态为 6
                        orderBaseMap.put("orderId",orderId);
                        //已退款
                        orderBaseMap.put("orderStatus", CommonType.orderStatus.REFUNDED.getCode());
                        orderBaseMap.put("orderRefundStatus","2");
                        orderBaseMap.put("orderReturnStatus","0");
                        orderBaseMap.put("orderFinishedTime",DateUtils.getNowDate());
                        Integer torf = orderBaseMapper.updateOrderBaseById(orderBaseMap);
                        Map<String, Object> updateGoodsMap = new HashMap<String, Object>();
                        updateGoodsMap.put("orderId",orderId);
                        updateGoodsMap.put("goodsRefundStatus",2);
                        updateGoodsMap.put("goodsReturnStatus",0);
                        orderReturnMapper.updateOrderGoodsById(updateGoodsMap);

                        returnCash= orderReturnMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>()
                                .eq(OrderReturnEntity::getOrderId,orderId)).getReturnCash();

                        List<Integer> orderTradeStatus =orderReturnMapper.queryOrderRecordStatus(orderBaseEntity.getPaymentNumber());
                        RefundParamDTO refundPara = new RefundParamDTO();
                        boolean isRefundSuccess = false;
                        if(torf == 1){
                            refundPara = orderReturnMapper.queryRefundPara(orderId);
                            refundPara.setOrderTradeStatus(orderTradeStatus);
                            refundPara.setReturnCash(returnCash);
                            orderReturnServiceImpl.updateGoodsSalenum(goodsNumMap);
                            if(refundPara.getPayCode() != 12) {
                                isRefundSuccess = payMentServiceClient.waitShipmentsRefund(refundPara);
                            }else{
                                isRefundSuccess = true;
                            }
                        }
                        if(!isRefundSuccess){
                            // 回滚
                            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                            return false;
                        }
                        //退款成功后 修改库存
                        //获取goodsid commonid 购买数量
                        List<WapGoodsSaleSumUpdateVO> wapGood = orderReturnMapper.queryGoodsXinXi(orderId);
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
                            orderReturnMapper.lessgoodsBySales(goods);
                        }
                        //查询红包和代金卷
                        if (null != orderBaseEntity.getVoucherCode()) {
                            //退还代金卷
                            orderReturnMapper.updateCouponStuate(orderBaseEntity.getVoucherCode());
                        }
                        if (null != orderBaseEntity.getRedpacketCode()) {
                            orderReturnMapper.updateRedPageStuate(orderBaseEntity.getRedpacketCode());
                        }
                    }else{
                        return false;
                    }
                    // 售后分销佣金处理
                    orderReturnServiceImpl.commissionReturn(orderId, returnGoods);

                    //插入我的余额明细表
                    assetDetailMap.put("orderId", orderId);
                    assetDetailMap.put("detailType", 2);
                    assetDetailMap.put("assetType", 1);
                    assetDetailMap.put("assetAmount", returnCash);
                    AssetDirectionAndPayTypeInfoDTO assetInfo = orderReturnMapper.getAssetDirectionAndPayTypeInfo(orderId);
                    if (null != assetInfo) {
                        assetDetailMap.put("assetDirection", assetInfo.getAssetDirection());
                        assetDetailMap.put("payType", assetInfo.getPayType());
                        assetDetailMap.put("userId", assetInfo.getUserId());
                        orderReturnMapper.insertDetail(assetDetailMap);
                    //判断是否是虚拟订单 且没有被核销
                    }
                    if(null  != map){
                        virtualGoods = 0;
                        //是否核销 0是 1否 2已过期
                        Integer isCancel = Integer.parseInt(map.get("isCancel").toString());
                        if(null != isCancel && 0 != isCancel) {
                            //如果该虚拟订单已过期 且过期不退款直接返回false
                            Integer result = orderBaseMapper.queryisVirtualOrderByTime(orderId);
                            if (0 != result) {
                                return false;
                            }
                            boolean returnFlag = false;
                            //未核销且在有效期内
                            if( 1 == isCancel){
                                Integer result1 = orderReturnMapper.queryisVirtualOrderByTime1(orderId);
                                if(null != result1 && result1 >0){
                                    returnFlag = true;
                                }
                                //过期且可过期退款
                            }else if( 2 == isCancel){
                                Integer result2 = orderReturnMapper.queryisVirtualOrderByTime2(orderId);
                                if(null != result2 && result2 >0){
                                    returnFlag = true;
                                    //退还虚拟订单佣金
                                    //如果该虚拟订单已过期 且过期允许退款就扣除积分
                                    //扣减积分
                                    //修改会员积分和会员等级
                                    //根据orderid 查询应该回退的积分和userid
                                    Map<String, Object> tmpMap = orderReturnMapper.queryAddpointsLogByOrderId(orderId);
                                    if (null != tmpMap) {
                                        MemberMessageListIntegralDTO updateMemberMessageListIntegral = new MemberMessageListIntegralDTO();
                                        ArrayList<String> list = new ArrayList<>();
                                        list.add(tmpMap.get("user_id").toString());
                                        updateMemberMessageListIntegral.setUserIdList(list);
                                        updateMemberMessageListIntegral.setIntegral((Integer) tmpMap.get("points_log_points"));
                                        //1是增加积分 0 是扣除积分
                                        updateMemberMessageListIntegral.setType(0);
                                        orderReturnServiceImpl.feignUpdateMemberIntegral(updateMemberMessageListIntegral);
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
                                        orderReturnMapper.feginAddpointsLog(userPointLog);
                                    }

                                }
                            }
                            if(returnFlag){
                                Map<String, Object> dataMap = orderReturnMapper.queryOrderReturnInfoById(orderId);
                                if (orderBaseEntity.getOrderPaymentAmount().compareTo(returnCash) > 0) {
                                    BigDecimal lv = returnCash.divide(orderBaseEntity.getOrderPaymentAmount(), 2, RoundingMode.HALF_UP);
                                    if(orderBaseEntity.getOrderCommissionFee() != null) {
                                        dataMap.put("orderCommissionFee", BigDecimal.valueOf(orderBaseEntity.getOrderCommissionFee()).multiply(lv).setScale(2, RoundingMode.HALF_UP));
                                    }
                                    if(orderBaseEntity.getBaseMemberDiscount() != null) {
                                        dataMap.put("baseMemberDiscount", BigDecimal.valueOf(orderBaseEntity.getBaseMemberDiscount()).multiply(lv).setScale(2, RoundingMode.HALF_UP));
                                    }
                                    if(orderBaseEntity.getDistributionDiscount() != null) {
                                        dataMap.put("distributionDiscount", orderBaseEntity.getDistributionDiscount().multiply(lv).setScale(2, RoundingMode.HALF_UP));
                                    }}
                                dataMap.put("orderId",orderId);
                                orderReturnMapper.updateOrderReturnInfoById(dataMap);
                            }
                        }
                    }
                    Integer torf = orderReturnMapper.updateOrderReturnById(orderGoodsMap);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
        return true;
    }


    public Boolean shoptimeout(List<ReturnOrderDetailsVO> orderDetails){
        if (orderDetails != null && orderDetails.size() > 0){
            Map<String, Object> orderGoodsMap = new HashMap<String, Object>();
            Map<String, Object> orderBaseMap = new HashMap<String, Object>();
            for (ReturnOrderDetailsVO orderDetail : orderDetails) {
                try {
                    String returnCode = orderDetail.getReturnCode();
                    //查询用户售后商品
                    List<Integer> returnGoods = this.baseMapper.queryReturnGoods(orderDetail.getOrderId());
                    if (orderDetail.getReturnState() == 3){
                        orderGoodsMap.put("returnCode", returnCode);
                        //平台5
                        orderGoodsMap.put("returnShopHandle", 2);
                        orderGoodsMap.put("returnState", 2);

                        orderGoodsMap.put("returnAddr", orderDetail.getOrderSellerAddress());
                        orderGoodsMap.put("returnRealName", orderDetail.getOrderSellerName());
                        orderGoodsMap.put("returnNumber", orderDetail.getOrderSellerContact());

                        Calendar instance = Calendar.getInstance();
                        instance.setTime(new Date());
                        instance.add(Calendar.DATE,7);
                        Date expiration_time = instance.getTime();

                        orderGoodsMap.put("expirationTime", expiration_time);
                        orderReturnMapper.updateOrderReturnById(orderGoodsMap);
                        orderGoodsMap.clear();
                    }else if(orderDetail.getReturnState() == 10){
                        orderGoodsMap.put("returnCode", returnCode);
                        orderGoodsMap.put("returnShopHandle", 4);
                        orderGoodsMap.put("returnState", 4);
                        orderGoodsMap.put("returnShopTime", new Date());

                        //	 商家确认收货该状态为9已退款
                        String orderId = orderReturnMapper.getOrderIdByReturnCode(returnCode);
                        OrderBaseEntity orderBaseEntity=orderBaseMapper.selectById(orderId);
                        orderBaseMap.put("orderId",orderId);
                        orderBaseMap.put("orderStatus","9");
                        orderBaseMap.put("orderReturnStatus","2");
                        Integer torf = orderBaseMapper.updateOrderBaseById(orderBaseMap);
                        Map<String, Object> updateGoodsMap = new HashMap<String, Object>();
                        updateGoodsMap.put("orderId",orderId);
                        updateGoodsMap.put("goodsReturnStatus",2);
                        orderGoodsMapper.updateOrderGoodsById2(updateGoodsMap);
                        String uOrderId = orderReturnMapper.queryPaymentNumber(orderId);
                        BigDecimal returnCash =orderReturnMapper.queryReturnCash(orderId);

                        List<Integer> orderTradeStatus = orderReturnMapper.queryOrderRecordStatus(uOrderId);
                        RefundParamDTO refundPara = new RefundParamDTO();
                        boolean isRefundSuccess = false;
                        if(torf == 1){
                            refundPara = orderReturnMapper.queryRefundPara(orderId);
                            refundPara.setOrderTradeStatus(orderTradeStatus);
                            refundPara.setReturnCash(returnCash);
                            isRefundSuccess = payMentServiceClient.waitShipmentsRefund(refundPara);

                        }
                        if(!isRefundSuccess){
                            // 回滚
                            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                            return false;
                        }

                        if (1 != torf ) {
                            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                            return false;
                        }

                        // 分销佣金订单处理
                        orderReturnServiceImpl.commissionReturn(orderId, returnGoods);
                        //判断当前订单是否全部退货
                        Boolean huoBaoreturn = true;
                        //平台退货佣金计算  会员折扣计算
                        List<OrderPingtaiCommissionDTO> orderPingtaiCommissions = orderReturnMapper.queryOrderPingTaiCommission(orderId);
                        BigDecimal countPingTaiCommission = new BigDecimal(0);
                        BigDecimal huiYuanZhekou = new BigDecimal(0);
                        for (OrderPingtaiCommissionDTO orderPingtaiCommission : orderPingtaiCommissions) {
                            //下单数量 等于 退单数量 平台抽佣全退

                            if(!orderPingtaiCommission.getGoodsNum().equals(orderPingtaiCommission.getGoodsReturnNum())){
                                huoBaoreturn = false;
                            }
                            //退还同步商品的 佣金
                            if(null != orderPingtaiCommission.getDropShipping() && 1 == orderPingtaiCommission.getDropShipping()) {
                                List<OrderSupplierBaseEntity> orderSupplierOrderBaseEntitys = orderSupplierBaseService.getBaseMapper().selectList(
                                        new LambdaQueryWrapper<OrderSupplierBaseEntity>()
                                                .eq(OrderSupplierBaseEntity::getOrderId, orderId));
                                for (OrderSupplierBaseEntity orderSupplierBaseEntity : orderSupplierOrderBaseEntitys) {
                                    BigDecimal supperPrice = orderSupplierBaseEntity.getSupperCommission();
                                    supperPrice = supperPrice.divide(new BigDecimal(orderPingtaiCommission.getGoodsNum()), 2, RoundingMode.HALF_UP);
                                    BigDecimal returnSupperPrice = supperPrice.multiply(new BigDecimal(orderPingtaiCommission.getGoodsReturnNum())).setScale(2, RoundingMode.HALF_UP);

                                    orderSupplierBaseService.update(null, new LambdaUpdateWrapper<OrderSupplierBaseEntity>()
                                            .eq(OrderSupplierBaseEntity::getOrderId, orderId)
                                            .eq(OrderSupplierBaseEntity::getGoodsId, orderSupplierBaseEntity.getGoodsId())
                                            .set(OrderSupplierBaseEntity::getSupperReturnCommission, returnSupperPrice));
                                }
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


                        //根据订单号查询支付单号 因为有可能多个订单公用一个支付单号 如果是多个订单共用用一个支付单号 要判断除当前退单外的其他订单是否是全退
                        //如果多个订单公用一个支付单号 根据当前订单号 和 支付单号 查询是否有其他订单没有全退货
                        Integer notOrderNum = orderReturnMapper.queryNotRetuenOrder(orderId, uOrderId);
                        if(0 <= notOrderNum){
                            //说明有其他订单没有完全退货 不退红包 优惠券
                            huoBaoreturn = false;
                        }
                        //退货红包 优惠券
                        if(huoBaoreturn){
                            orderReturnMapper.updateHongbaoStart(orderId);
                            orderReturnMapper.updateYouHuiquanStart(orderId);
                        }
                        orderBaseMapper.update(null,new LambdaUpdateWrapper<OrderBaseEntity>()
                                .eq(OrderBaseEntity::getOrderId,orderId)
                                .set(OrderBaseEntity::getOrderCommissionReturnFee,countPingTaiCommission));
                        //更新退款表 平台退还佣金 退还会员折扣
                        orderReturnMapper.update(null,new LambdaUpdateWrapper<OrderReturnEntity>()
                                .eq(OrderReturnEntity::getOrderId,orderId)
                                .set(OrderReturnEntity::getReturnCommisionFee,countPingTaiCommission)
                                .set(OrderReturnEntity::getReturnMemberDiscount,huiYuanZhekou));
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
                            orderReturnMapper.updateOrderGoodsById1(updateGoodsReturnCommissionMap);
                        }

                        //扣除分销佣金
                        OrderCommissionEntity mapFx = orderCommissionService.getById(orderId);
                        Map<String,Object> objectMap=new HashMap<>();
                        objectMap.put("userId",mapFx.getShareUserId().toString());
                        objectMap.put("var2",mapFx.getReturnOneTgCommission());
                        objectMap.put("var3",mapFx.getReturnFxCommission());
                        if(null != mapFx){
                            this.baseMapper.updateVkCommissionReturn(objectMap);
                        }

                        Integer storageId = orderDetail.getStorageId() != null ? orderDetail.getStorageId() : 0;
                        List<StorageInventoryFlowGoodsDTO> storageGoodsList = new ArrayList<>();
                        Map<String,Object> goodsNumMap = new HashMap<>();
                        List<Map<String,Object>> goodsNum = orderReturnMapper.queryBuyNumByorderId(orderId);
                        goodsNumMap.put("goodsNum",goodsNum);
                        //退款更新商品销量
                        orderReturnServiceImpl.updateGoodsSalenum(goodsNumMap);
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
                                goodServiceClient ,baseServiceClient, orderBaseEntity.getShopId(), "");
                        //根据orderid 查询应该回退的积分和userid
                        //根据orderid 查询应该回退的积分和userid
                        Map<String, Object> map = orderReturnMapper.queryAddpointsLogByOrderId(orderId);

                        //ZZZ 新增积分按比例扣除逻辑
                        //订单实付金额
                        BigDecimal orderPaymentAmount = orderBaseEntity.getOrderPaymentAmount();
                        //订单商品总价（不含运费）
                        BigDecimal orderGoodsAmount = orderBaseEntity.getOrderGoodsAmount();
                        //退货商品总价(不含运费）
                        BigDecimal returnAmount = BigDecimal.ZERO;
                        //退货积分系数
                        BigDecimal coef = BigDecimal.ZERO;
                        List<OrderReturnPointsDTO> orderReturnPointsList = orderReturnMapper.queryOrderReturnInfo(orderId);
                        if (null != orderReturnPointsList && orderReturnPointsList.size() > 0) {
                            for (OrderReturnPointsDTO orp : orderReturnPointsList) {
                                BigDecimal goodsPrice = null != orp ? orp.getGoodsPrice() : BigDecimal.ZERO;
                                Integer goodsReturnNum = null != orp ? orp.getOrderGoodsReturnnum() : 0;
                                returnAmount = returnAmount.add(goodsPrice.multiply(new BigDecimal(goodsReturnNum)));
                            }
                        }
                        //实际退款金额(用于计算退还积分)
                        returnAmount = returnAmount.multiply(orderPaymentAmount).divide(orderGoodsAmount, RoundingMode.HALF_UP);
                        //实际退款积分ZZZ
                        BigDecimal tmp = BigDecimal.ZERO;
                        if (null != map && null != map.get("points_log_points")) {
                            tmp = returnAmount.multiply(new BigDecimal((Integer) map.get("points_log_points"))).divide(orderPaymentAmount, RoundingMode.HALF_UP);
                        }
                        Integer tmpInt = tmp.intValue();
                        map.put("points_log_points", tmpInt);
                        //插入我的余额明细表
                        setAssDetails(orderId,returnCash);



                        //修改会员积分和会员等级
                        MemberMessageListIntegralDTO updateMemberMessageListIntegral = new MemberMessageListIntegralDTO();
                        ArrayList<String> list = new ArrayList<>();
                        list.add(map.get("user_id").toString());
                        updateMemberMessageListIntegral.setUserIdList(list);
                        updateMemberMessageListIntegral.setIntegral((Integer) map.get("points_log_points"));
                        //1是增加积分 0 是扣除积分
                        updateMemberMessageListIntegral.setType(0);
                        orderReturnServiceImpl.feignUpdateMemberIntegral(updateMemberMessageListIntegral);
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
                        orderReturnMapper.feginAddpointsLog(userPointLog);

                        orderGoodsMap.put("returnFinishTime",new Date());
                        orderReturnMapper.updateOrderReturnById(orderGoodsMap);
                        orderGoodsMap.clear();
                        orderBaseMap.clear();
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }

            }
        }
        return true;
    }


/**
 * 设置退货详情信息。
 * 该方法用于根据退货订单ID和退回金额，构建并存储资产详情信息。
 * 如果找到了相应的资产方向和支付类型信息，则将这些信息一并存储。
 *
 * @param orderId 退货订单ID，用于查询和标记资产详情信息。
 * @param returnCash 退回的现金金额，表示此次退货涉及的资产变化量。
 */
public void setAssDetails(String orderId, BigDecimal returnCash) {
    // 初始化一个Map用于存储资产详情的各项信息
    Map<String, Object> assetDetailMap = new HashMap<>();
    // 设置订单ID、详情类型、资产类型和资产金额
    // 插入我的余额明细表
    assetDetailMap.put("orderId", orderId);
    assetDetailMap.put("detailType", 2);
    assetDetailMap.put("assetType", 1);
    assetDetailMap.put("assetAmount", returnCash);

    // 查询退货订单的资产方向、支付类型和用户ID信息
    AssetDirectionAndPayTypeInfoDTO assetInfo = orderReturnMapper.getAssetDirectionAndPayTypeInfo(orderId);

    // 再次设置资产金额，确保信息的准确性
    assetDetailMap.put("assetAmount", returnCash);
    // 如果查询到了资产信息，则将其添加到assetDetailMap中，并插入资产详情记录
    if (null != assetInfo) {
        assetDetailMap.put("assetDirection", assetInfo.getAssetDirection());
        assetDetailMap.put("payType", assetInfo.getPayType());
        assetDetailMap.put("userId", assetInfo.getUserId());
        // 插入资产详情信息到数据库
        orderReturnMapper.insertDetail(assetDetailMap);
    }
}
}




