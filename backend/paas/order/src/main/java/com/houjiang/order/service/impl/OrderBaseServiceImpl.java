package com.houjiang.order.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.houjiang.commons.config.ThreadPoolConfig;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.PageUserTokenBeanDTO;
import com.houjiang.commons.dto.base.BaseExpressDTO;
import com.houjiang.commons.dto.base.MemberMessageListIntegralDTO;
import com.houjiang.commons.dto.goods.ReceiveGoodsDetailsDTO;
import com.houjiang.commons.dto.goods.StorageInventoryFlowDTO;
import com.houjiang.commons.dto.goods.StorageInventoryFlowGoodsDTO;
import com.houjiang.commons.dto.goods.SynchronousGoodsDTO;
import com.houjiang.commons.dto.job.ExpireMarkIdUpdateDTO;
import com.houjiang.commons.dto.order.AddInventoryDTO;
import com.houjiang.commons.dto.order.PlatFormMemberSelfQueryDTO;
import com.houjiang.commons.dto.order.RefundParamDTO;
import com.houjiang.commons.dto.order.WapGoodsSaleSumDTO;
import com.houjiang.commons.dto.payment.AssetTmpInfoDTO;
import com.houjiang.commons.dto.payment.MallRefundRequest;
import com.houjiang.commons.dto.payment.RefundAccountParamDTO;
import com.houjiang.commons.dto.shop.ShopUserDTO;
import com.houjiang.commons.dto.supplier.MerchantGoodsBaseDTO;
import com.houjiang.commons.dto.supplier.SupplierSyncGoodsDTO;
import com.houjiang.commons.dto.userms.BusinessUserInfoDTO;
import com.houjiang.commons.dto.userms.PlusEquityDTO;
import com.houjiang.commons.dto.userms.UserInfoDTO;
import com.houjiang.commons.dto.vop.OrderGoodsJdVopDTO;
import com.houjiang.commons.dto.yiyatong.*;
import com.houjiang.commons.errorcode.domain.PermissionErrorCode;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.exception.BizCoreException;
import com.houjiang.commons.rocketmq.MqSendUtils;
import com.houjiang.commons.util.*;
import com.houjiang.commons.util.json.JsonUtils;
import com.houjiang.commons.util.excel.ExcelsUtil;
import com.houjiang.commons.util.kuaidiniao.KdniaoSubscribeAPI;
import com.houjiang.commons.util.kuaidiniao.KdniaoTrackQueryAPI;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.base.BaseCurrencySymbolVO;
import com.houjiang.commons.vo.base.PointsCashRatioVo;
import com.houjiang.commons.vo.goods.GoodsDetailsListVO;
import com.houjiang.commons.vo.goods.PurchaseOrderVO;
import com.houjiang.commons.vo.goods.StorageVO;
import com.houjiang.commons.vo.im.ImConfigVO;
import com.houjiang.commons.vo.job.AcquiescenceVO;
import com.houjiang.commons.vo.order.*;
import com.houjiang.commons.vo.payment.AccountShopBillDetailsVO;
import com.houjiang.commons.vo.shop.AppShopBaseSalesInfoVO;
import com.houjiang.commons.vo.shop.ShopStampVO;
import com.houjiang.commons.vo.shop.ShopUserVO;
import com.houjiang.commons.vo.user.UserInfoOrderDetailVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.commons.vo.yiyatong.base.OrderExpressItemVO;
import com.houjiang.commons.vo.yiyatong.base.OrderExpressVO;
import com.houjiang.commons.vo.yiyatong.base.OrderReturnGoodsVO;
import com.houjiang.commons.vo.yiyatong.base.SubmitOrderVO;
import com.houjiang.order.dto.*;
import com.houjiang.order.entity.*;
import com.houjiang.order.feign.*;
import com.houjiang.order.mapper.*;
import com.houjiang.order.service.OrderBaseService;
import com.houjiang.order.service.OrderReturnService;
import com.houjiang.order.utils.AccessTokenUtil;
import com.houjiang.order.utils.PageInfo;
import com.houjiang.order.vo.*;
import io.seata.core.context.RootContext;
import io.seata.core.exception.TransactionException;
import io.seata.spring.annotation.GlobalTransactional;
import io.seata.tm.TransactionManagerHolder;
import io.seata.tm.api.GlobalTransactionContext;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.houjiang.commons.util.TokenUtils.checkToken;

/**
 * @ClassName OrderBaseServiceImpl
 * @Description 订单详细信息
 * @Author Amos
 * @Date 2023-06-06 15:00:49
 * @Version 1.0
 * @author Amos
 */
@Service
public class OrderBaseServiceImpl extends ServiceImpl<OrderBaseMapper, OrderBaseEntity>
        implements OrderBaseService {

    private final Logger logger = LoggerFactory.getLogger(OrderBaseServiceImpl.class);
    /** 引入线程池 */
    private static final ExecutorService executorService = ThreadPoolConfig.executorService;

    /**
     * 商城分销内部事件客户端（订单完成 → 商城生成佣金）。
     * 只在确认收货成功时通知，失败仅记日志，绝不影响收货主流程。
     */
    @Resource
    private MallDistributionClient mallDistributionClient;

    /** 内部任务令牌，与商城侧 houcloud.plugin.internal-job-token 保持一致 */
    @Value("${houcloud.plugin.internal-job-token:}")
    private String internalJobToken;
    /**
     * 京东支付方式
     */
    @Value("${jingdong.paymentType:0}")
    private Integer PAYMENT_TYPE;
    @Resource
    private OrderGoodsMapper orderGoodsMapper;
    @Resource
    private OrderCommissionMapper orderCommissionMapper;
    @Resource
    private OrderReturnMapper orderReturnMapper;
    @Resource
    private OrderGoodsEvaluationMapper orderGoodsEvaluationMapper;
    @Resource
    private OrderInvoiceInfoMapper orderInvoiceInfoMapper;
    @Autowired
    private OrderShipmentMapper orderShipmentMapper;
    @Autowired
    private ActivityServiceClient activityServiceClient;
    @Autowired
    private GoodServiceClient goodServiceClient;
    @Autowired
    private PayMentServiceClient payMentServiceClient;
    @Autowired
    private com.houjiang.order.service.OrderInventoryLifecycleService inventoryLifecycleService;
    @Autowired
    private UserServiceClient userServiceClient;
    @Autowired
    private BaseServiceClient baseServiceClient;
    @Autowired
    private MerchantServiceClient merchantServiceClient;
    @Autowired
    private OrderYiyatongMapper orderYiyatongMapper;
    @Autowired
    private ThirdPartyServiceClient thirdPartyServiceClient;
    @Autowired
    private OrderReturnService orderReturnService;
    @Resource
    private OrderBaseMapper orderBaseMapper;
    @Resource
    private OrderPaymentVocherMapper orderPaymentVocherMapper;
    @Autowired
    private RestTemplate restTemplate;
    @Resource
    private OrderDealerInfoMapper orderDealerInfoMapper;
    @Autowired
    private SupplierClient supplierClient;
    final Logger log = LoggerFactory.getLogger(OrderBaseServiceImpl.class);

    @Override
    public ResponseResult queryAmountPaid(Map<String, String> map) {
        // 初始化支付详情对象和支付金额
        PayDetailsVO payDetails = null;
        BigDecimal paymentAmount = new BigDecimal(0);
        // 从map中尝试获取支付编号
        String paymentNumber = null;
        try {
            paymentNumber = map.get("paymentNumber");
            // 如果支付编号为空，则直接返回null
            if (StringUtils.isEmpty(paymentNumber)) {
                return null;
            }
            // 判断支付编号的开头字符，以决定使用哪种查询方法
            String substring = paymentNumber.substring(0, 1);
            if ("U".equals(substring)) {
                payDetails = this.baseMapper.querypaymentAmount(paymentNumber);
            } else {
                payDetails = this.baseMapper.querypaymentByOrder(paymentNumber);
            }
            // 查询订单的取消时间，并设置到支付详情对象中
            Long orderCancelTime = queryOrderCancelTime(map);
            payDetails.setOrderCancelTime(orderCancelTime);
        } catch (Exception e) {
            // 捕获查询过程中可能出现的异常，并打印堆栈跟踪
            e.printStackTrace();
        }
        // 返回查询结果，如果查询成功，则返回支付详情对象；否则返回null
        return ResponseResult.success(payDetails);
    }

    private Long queryOrderCancelTime(Map<String, String> map) {
        Long retoverplusTime = null;
        String orderId = String.valueOf(map.get("orderId"));
        //根据orderId查询订单
        OrderListVO orderList = this.baseMapper.queryOrderCreateTime(orderId);
        Integer status = orderList == null ? 0 : orderList.getOrderStatus();
        List<GoodsDetailsListVO> goodsDetailsList = this.baseMapper.queryGoodsList(orderId);
        // 商品总价
        BigDecimal paymentPrice = new BigDecimal(0);
        for (int j = 0; j < goodsDetailsList.size(); j++) {
            //若为待付款订单，对限时秒杀商品添加距离付款时间
            if (1 == status) {
                Integer goodsId = goodsDetailsList.get(j).getGoodsId();
                Integer seckillId = this.baseMapper.queryCountForSeckillGoods(goodsId);
                if (null != seckillId && seckillId > 0) {
                    //取消时间 单位分钟
                    Integer cancelTime = this.baseMapper.queryCancelTime(seckillId);
                    if (null != cancelTime && cancelTime > 0) {
//                        String orderCreateTime = orderList.getOrderCreateTime();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        try {
                            /* 获取订单的创建时间 */
                            Date orderCreateTimeDate = orderList.getOrderCreateTime();

                            /* 创建一个Calendar实例用于时间操作 */
                            Calendar cal = Calendar.getInstance();

                            /* 设置Calendar的时间为订单的创建时间 */
                            cal.setTime(orderCreateTimeDate);

                            /* 根据取消时间添加分钟数到订单创建时间，计算订单的预计取消时间 */
                            cal.add(Calendar.MINUTE, cancelTime);
                            Date orderCancelTime = cal.getTime();

                            /* 检查当前时间是否在订单预计取消时间之前 */
                            if (new Date().before(orderCancelTime)) {
                                /* 获取订单预计取消时间的毫秒数 */
                                Long overplusTime = orderCancelTime.getTime();

                                /* 如果retoverplusTime为空或者当前订单的过期时间更短，则更新retoverplusTime */
                                if (retoverplusTime == null) {
                                    retoverplusTime = overplusTime;
                                } else if (retoverplusTime > overplusTime) {
                                    retoverplusTime = overplusTime;
                                }
                            }

                            /* 捕获并处理异常 */
                        } catch (Exception e) {
                        }

                    }
                }
            }
        }
        return retoverplusTime;
    }

    /**
     * 根据用户ID查询积分订单的数量。
     *
     * @param map 包含查询参数的映射表，其中必须包含用户ID。
     * @return 返回积分订单数量的响应结果。
     */
    @Override
    public ResponseResult queryintegraOrdersCount(Map<String, Object> map) {
        // 通过用户ID查询积分订单的数量
        Integer count = this.baseMapper.queryintegraOrdersCount(map.get("userId").toString());
        // 返回查询结果的成功响应
        return ResponseResult.success(count);
    }

    /**
     * 根据订单号或支付号查询店铺ID的数量。
     * 该方法用于查询与给定的订单号或支付号相关的店铺ID的数量。它通过Map参数接收订单号或支付号，
     * 并利用LambdaQueryWrapper构造查询条件，最后返回查询结果的数量。
     * 注意，这里查询的是店铺ID的数量，而不是具体的店铺ID，因为业务逻辑可能只需要知道是否有匹配的店铺ID。
     *
     * @param map 包含订单号或支付号的键值对参数集合。
     *            键应该为"orderId"或"paymentNumber"，值为对应的订单号或支付号。
     * @return 返回匹配的店铺ID的数量。如果没有匹配的记录，返回0。
     */
    @Override
    public Integer queryShopIdByOrderIdNum(Map<String, Object> map) {
        // 使用LambdaQueryWrapper构造查询条件，查询与给定订单号或支付号匹配的记录数量，限制查询结果为1条
        Integer shopIdCount = Math.toIntExact(this.baseMapper.selectCount(new LambdaQueryWrapper<OrderBaseEntity>()
                .eq(OrderBaseEntity::getOrderId, map.get("orderId")).or().eq(OrderBaseEntity::getPaymentNumber, map.get("orderId"))
                .last("limit 1")));
        // 返回查询结果的数量
        return shopIdCount;
    }


    /**
     * 根据订单信息查询店铺ID。
     * <p>
     * 该方法通过调用底层的baseMapper，使用LambdaQueryWrapper构造查询条件，查询满足条件的第一个OrderBaseEntity对象。
     * 查询条件为订单ID（orderId）或支付编号（paymentNumber）与传入的订单信息匹配。
     * 主要用于在业务操作中根据订单相关的信息定位到具体的店铺ID。
     *
     * @param map 包含订单信息的Map对象，其中应包含订单ID或支付编号等关键信息。
     * @return 返回包含查询结果的ResponseResult对象，如果查询成功，则结果中的data字段为查询到的OrderBaseEntity对象。
     */
    @Override
    public ResponseResult queryShopIdByOrderInfo(Map<String, Object> map) {
        // 根据传入的订单信息（orderId或paymentNumber）查询订单数据库，返回第一个匹配的结果
        OrderBaseEntity entity = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderBaseEntity>()
                .eq(OrderBaseEntity::getOrderId, map.get("orderId")).or().eq(OrderBaseEntity::getPaymentNumber, map.get("orderId"))
                .last("limit 1"));
        // 构造并返回查询成功的响应结果，其中包含查询到的OrderBaseEntity对象
        return ResponseResult.success(entity);
    }

    /**
     * 根据订单ID查询商品数量。
     * <p>
     * 通过调用BaseMapper的queryGoodNumByOrderId方法，使用传入的订单ID查询对应订单的商品数量。
     * 这里的Map参数用于传递查询条件，其中的关键字"orderId"对应于查询订单ID的条件。
     *
     * @param map 一个包含查询条件的Map对象，其中必须包含键"orderId"以指定订单ID。
     * @return 返回查询到的商品数量，如果未查询到则返回null。
     */
    @Override
    public Integer queryGoodNumByOrderId(Map<String, Object> map) {
        // 通过订单ID查询商品数量
        Integer num = this.baseMapper.queryGoodNumByOrderId(map.get("orderId").toString());
        return num;
    }

    @Override
    public ResponseResult messageToRemind(Map<Object, Object> map) {
        // 从map中获取token字符串
        String token = (String) map.get("token");
        // 检查Redis中是否存在以token前缀的键，判断用户token是否有效
        boolean isExist = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (isExist) {
            // 统一解析器能正确读取 RedisTemplate 序列化的会话值。
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
            // 从UserInfoVO中获取用户ID
            String userId = userInfo.getUserId();
            // 根据用户ID查询待处理订单基础数量
            MessageRemindNumVO mrn = this.baseMapper.selectOrderBaseNum(userId);

            if (null != mrn) {
                // 根据用户ID查询待处理退款售后数量，并设置到MessageRemindNumVO对象中
                Integer returnNum = this.baseMapper.selectOrderReturnNum(userId);
                mrn.setRefundAndAfterSale(returnNum);
            }
            // 返回成功响应，包含MessageRemindNumVO对象
            return ResponseResult.success(mrn);
        }
        // 如果token无效，返回失败响应，错误码为TOKEN_IS_INVALID
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);

    }

    @Override
    public ResponseResult deleteOrder(Map<String, Object> map) {
        // 通过token获取用户信息，用于后续操作的授权和身份验证
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());

        // 从用户信息中提取用户ID，用于后续操作与用户的关联
        String userId = userInfo.getUserId();

        // 从请求参数中获取订单ID，用于查询具体的订单信息
        String orderId = (String) map.get("orderId");

        // 检查是否有直接传入的用户ID，如果有，则使用传入的用户ID，否则使用之前获取的用户ID
        userId = map.get("userId") == null ? userId : (String) map.get("userId");

        // 根据订单ID查询订单基本信息，用于进一步处理订单状态等信息
        OrderBaseEntity orderBaseEntity = this.baseMapper.selectById(orderId);

        // 获取订单的状态，用于判断订单当前所处的状态，如是否已支付、已发货等
        Integer orderStatus = orderBaseEntity.getOrderStatus();

        // 判断订单
        if (orderStatus == 1 || orderStatus == 6 || orderStatus == 7 || orderStatus == 9) {
            // 根据用户ID和订单ID更新订单商品的删除状态为已删除
            orderGoodsMapper.update(null, new LambdaUpdateWrapper<OrderGoodsEntity>()
                    .eq(OrderGoodsEntity::getBuyerUserId, userId)
                    .eq(OrderGoodsEntity::getOrderId, orderId)
                    .set(OrderGoodsEntity::getOrderDel, 1));

            // 根据用户ID和订单ID更新订单基础信息的删除状态为已删除
            this.baseMapper.update(null, new LambdaUpdateWrapper<OrderBaseEntity>()
                    .eq(OrderBaseEntity::getBuyerUserId, userId)
                    .eq(OrderBaseEntity::getOrderId, orderId)
                    .set(OrderBaseEntity::getOrderDel, 1));

        }
        return ResponseResult.success();
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public ResponseResult cancelTheOrder(Map<Object, Object> map) throws TransactionException {
        try {
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
            String userId = map.get("userId") != null ? (String) map.get("userId") : userInfo.getUserId();
            String orderid = map.get("orderId").toString();
            // 订单取消者身份
            Integer orderCancelIdentity = Integer.valueOf(map.get("orderCancelIdentity").toString());
            // 1：买家
            // 2：卖家
            // 3：系统
            List<String> orderIdList = new ArrayList<>();
            orderIdList.add(orderid);
            // 爆品取消时需求跟绑定商品一起取消
            OrderBaseEntity orderPayInfo = this.baseMapper.selectById(orderid);

            if (orderPayInfo.getOrderType().intValue() == CommonType.orderType.HOT_GOODS_TYPE.getCode()) {
                List<OrderBaseEntity> orderPayInfoList = this.baseMapper.selectList(new LambdaQueryWrapper<OrderBaseEntity>()
                        .eq(OrderBaseEntity::getPaymentNumber, orderPayInfo.getPaymentNumber()));
                orderIdList = orderPayInfoList.stream().map(OrderBaseEntity::getOrderId).collect(Collectors.toList());
            }

            for (String orderId : orderIdList) {
                // 订单取消状态
                Integer orderStatus = CommonType.orderStatus.CANCEL.getCode();
                OrderBaseEntity orderBaseEntity = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderBaseEntity>()
                        .eq(OrderBaseEntity::getBuyerUserId, userId).eq(OrderBaseEntity::getOrderId, orderId));
                // 查询该订单是否已取消
                Integer status = orderBaseEntity.getOrderStatus();
                if (orderStatus.equals(status)) {
                    //该订单已取消，请勿重复操作！
                    return ResponseResult.fail(BizCodeEnume.ORDER_CANCELED);

                } else {
                    //获取非数字订单
                    OrderBaseEntity order = this.baseMapper.selectById(orderid);
                    String record_payorder = order.getPaymentNumber();
                    //取消订单 删除当前用户的拼团信息
                    Map<String, Object> pinTuanMap = new HashMap<>();
                    pinTuanMap.put("orderId", record_payorder);
                    activityServiceClient.deletePinTuanInfo(pinTuanMap);

                    List goodsId = new ArrayList();
                    // 查询商品信息中商品id,商品数量
                    List<AddInventoryDTO> aie = this.baseMapper.queryGoodsDetails(orderId);
                    for (AddInventoryDTO addInventoryDTO : aie) {
                        goodsId.add(addInventoryDTO.getGoodsId());
                    }
                    // 订单取消原因
                    String orderCancelReason = (String) map.get("orderCancelReason");
                    // 订单取消时间
                    Date orderCancelDate = DateUtils.getNowDate();
                    /**
                     * 更新订单基础信息，并检查更新是否成功。
                     * 使用LambdaUpdateWrapper构造更新条件，针对特定订单进行状态及其他相关信息的更新。
                     * 更新成功与否通过比较更新影响行数与预设成功代码进行判断。
                     */
                    boolean isUpdateSuccess = this.baseMapper.update(null, new LambdaUpdateWrapper<OrderBaseEntity>()
                            .eq(OrderBaseEntity::getOrderId, orderPayInfo.getOrderId())
                            .set(OrderBaseEntity::getOrderStatus, orderStatus)
                            .set(OrderBaseEntity::getOrderCancelIdentity, orderCancelIdentity)
                            .set(OrderBaseEntity::getOrderCancelReason, orderCancelReason)
                            .set(OrderBaseEntity::getOrderCancelDate, orderCancelDate)) == CommonStatusEnum.SUCCESS.getCode();

                    /**
                     * 更新订单商品信息，并检查更新是否成功。
                     * 使用LambdaUpdateWrapper构造更新条件，针对特定买家和订单进行商品状态的更新。
                     * 更新成功与否通过比较更新影响行数与预设成功代码进行判断。
                     */
                    boolean isUpdateGoodsSuccess = orderGoodsMapper.update(null, new LambdaUpdateWrapper<OrderGoodsEntity>()
                            .eq(OrderGoodsEntity::getBuyerUserId, userId)
                            .eq(OrderGoodsEntity::getOrderId, orderId)
                            .set(OrderGoodsEntity::getOrderGoodsStatus, orderStatus)) == CommonStatusEnum.SUCCESS.getCode();


                    //取消佣金订单
                    orderCommissionMapper.update(null, new LambdaUpdateWrapper<OrderCommissionEntity>()
                            .eq(OrderCommissionEntity::getOrderId, orderId)
                            .set(OrderCommissionEntity::getOrderStatus, CommonType.CommissionOrderStatus.CANCEL.getCode()));

                    //取消付款状态
                    payMentServiceClient.updateCancelStatus(orderId);
                    //退还plus会员折扣
                    plusdis(orderId, userId, goodsId);
                    // 分销：取消订单整单作废待结算佣金（旁路，失败不影响取消）
                    notifyDistributionCancelled(orderId, orderBaseEntity.getOrderPaymentAmount(), true);
                }
                // 查询支付单号
                String payMentNumber = orderPayInfo.getPaymentNumber();
                String redpacketId = orderPayInfo.getRedpacketCode();
                String voucherCode = orderPayInfo.getVoucherCode();
                //取消订单成功后 修改库存
                //获取goodsid commonid 购买数量
                List<OrderGoodsEntity> orderGoodsEntityList = orderGoodsMapper.selectList(new LambdaQueryWrapper<OrderGoodsEntity>()
                        .eq(OrderGoodsEntity::getOrderId, orderId));
                //封装数据
                List<WapGoodsSaleSumDTO> wapGood = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(orderGoodsEntityList)) {
                    wapGood = orderGoodsEntityList.stream().map(entity -> {
                        WapGoodsSaleSumDTO catResult = new WapGoodsSaleSumDTO();
                        catResult.setGoodsNum(entity.getOrderGoodsNum());
                        BeanUtils.copyProperties(entity, catResult);
                        return catResult;
                    }).collect(Collectors.toList());
                }

                //修改商品库存
                goodServiceClient.updateGoodsStock(wapGood);
                //退还红包
                if (redpacketId != null) {
                    // 查询订单状态
                    List<Integer> orderStatusList = this.baseMapper.selectList(new LambdaQueryWrapper<OrderBaseEntity>()
                                    .eq(OrderBaseEntity::getPaymentNumber, payMentNumber))
                            .stream().map(OrderBaseEntity::getOrderStatus).collect(Collectors.toList());
                    Map<String, Object> codeMap = new HashMap<String, Object>();
                    codeMap.put("redpacketId", redpacketId);
                    codeMap.put("type", "0");
                    Set set = new HashSet(orderStatusList);
                    if (set.size() == 1) {
                        activityServiceClient.updateSalesPrm(codeMap);
                        if (voucherCode != null) {
                            List<String> voucherCodeList = Arrays.asList(voucherCode.split(","));
                            Map<String, Object> voucherCodeMap = new HashMap<String, Object>();
                            voucherCodeMap.put("voucherId", voucherCodeList);
                            voucherCodeMap.put("type", "0");
                            activityServiceClient.updateSalesPrm(voucherCodeMap);
                        }
                    }
                } else {
                    if (!StringUtils.isEmpty(voucherCode)) {
                        List<String> voucherCodeList = Arrays.asList(voucherCode.split(","));
                        Map<String, Object> voucherCodeMap = new HashMap<String, Object>();
                        voucherCodeMap.put("voucherId", voucherCodeList);
                        voucherCodeMap.put("type", "0");
                        activityServiceClient.updateSalesPrm(voucherCodeMap);
                    }
                }
                // 判断是否是线下支付 如果是 取消订单同时 付款凭证审核改为 审核驳回
                if(CommonType.payType.OFFLINE_PAY.getCode().equals(orderBaseEntity.getPaymentCode())){
                    this.baseMapper.cancelOrderUpdatePaymentVocher(orderId);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            // 回滚
            GlobalTransactionContext.reload(RootContext.getXID()).rollback();
            return ResponseResult.fail(e.getMessage());
        }
        return ResponseResult.success();
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public ResponseResult confirmTheGoods(Map<Object, Object> map) throws TransactionException {
        ResponseResult result = new ResponseResult();
        String token = (String) map.get("token");
        boolean isExist = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (isExist) {
            try {
                // String转object
                UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
                if (ui == null) {
                    return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
                }
                String userId = map.get("userId") == null ? ui.getUserId() : map.get("userId").toString();
                String orderId = (String) map.get("orderId");
                //订单信息
                OrderBaseEntity orderBaseEntity = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderBaseEntity>()
                        .eq(OrderBaseEntity::getBuyerUserId, userId).eq(OrderBaseEntity::getOrderId, orderId));
                //查询当前订单是否有经销订单
                List<OrderBaseEntity> orderList=this.baseMapper.selectList(new LambdaQueryWrapper<OrderBaseEntity>()
                        .eq(OrderBaseEntity::getOriginalOrder, orderId));

                Date orderFinishedTime = DateUtils.getNowDate();
                Integer orderStatus = CommonType.orderStatus.CONFIRM_RECEIPT.getCode();
                // 获取最晚收货时间
                Integer orderReceiverDate = DateUtils.timeToStamp(orderBaseEntity.getOrderReceiverDate());
                // 获取当前时间
                Integer dateTime = DateUtils.timeToStamp();
                // 订单金额
                String sellerId = orderBaseEntity.getSellerUserId();
                BigDecimal money = orderBaseEntity.getOrderPaymentAmount();
                Map<String, Object> moneyMap = new HashMap<>();
                moneyMap.put("sellerId", sellerId);
                moneyMap.put("money", money);

                String sellerMobile = orderBaseEntity.getOrderSellerContact();
                // 判断最晚收货时间与当前时间
                if (dateTime.equals(orderReceiverDate) || dateTime > orderReceiverDate) {
                    //默认收货时间(goods)
                    boolean isSuccessByGoodsForUpdate = this.baseMapper.updateByOrderGoodsForDefaultFinish(orderId, orderStatus);
                    boolean isSuccessByBaseForUpdate = this.baseMapper.updateByOrderBaseForDefaultFinish(orderId, orderStatus);
                    if (isSuccessByGoodsForUpdate && isSuccessByBaseForUpdate) {
                        //计算售后最后时间
                        String configValue = baseServiceClient.selectConfigValueFeign(CommonType.configKey.AFTER_SALES_TIME.getDesc());
                        Date orderLastReturnTime = DateUtils.addDays(DateUtils.getNowDateToDf(), Integer.parseInt(configValue));
                        //更新订单最后售后时间
                        this.baseMapper.updateByOrderBaseorderLastReturnTime(orderId, orderLastReturnTime);
                        result.setCode(CommonStatusEnum.SUCCESS.getCode());
                        result.setMessage(BizCodeEnume.DEFAULT_RECEIPT_SUCCEEDED.getMsg());
                        // 分佣订单售后时间更新
                        baseServiceClient.updateOrderAfterSale(orderId);
                        //如果有子订单,更新子订单状态
                        if(!orderList.isEmpty()){
                            for (OrderBaseEntity entity:orderList){
                                // 更新订单商品和订单状态为确认收货
                                this.baseMapper.updateByOrderGoodsForDefaultFinish(entity.getOrderId(), orderStatus);
                                this.baseMapper.updateByOrderBaseForDefaultFinish(entity.getOrderId(), orderStatus);
                                //更新订单最后售后时间
                                this.baseMapper.updateByOrderBaseorderLastReturnTime(entity.getOrderId(), orderLastReturnTime);
                                payMentServiceClient.changeConsumeRecord(entity.getOrderId());
                            }
                        }
                    } else {
                        result.setCode(CommonStatusEnum.FAIL.getCode());
                        result.setMessage(BizCodeEnume.DEFAULT_RECEIPT_FAILED.getMsg());
                    }
                } else {
                    boolean isSuccessByGoodsForUpdate = orderGoodsMapper.update(null, new LambdaUpdateWrapper<OrderGoodsEntity>()
                            .eq(OrderGoodsEntity::getOrderId, orderId)
                            .set(OrderGoodsEntity::getOrderGoodsStatus, orderStatus)
                            .set(OrderGoodsEntity::getOrderGoodsFinishTime, orderFinishedTime)) > CommonStatusEnum.FAIL.getCode();

                    boolean isSuccessByBaseForUpdate = this.baseMapper.update(null, new LambdaUpdateWrapper<OrderBaseEntity>()
                            .eq(OrderBaseEntity::getOrderId, orderId)
                            .set(OrderBaseEntity::getOrderStatus, orderStatus)
                            .set(OrderBaseEntity::getOrderFinishedTime, orderFinishedTime)) > CommonStatusEnum.FAIL.getCode();

                    if (isSuccessByGoodsForUpdate && isSuccessByBaseForUpdate) {
                        //计算售后最后时间
                        String configValue = baseServiceClient.selectConfigValueFeign(CommonType.configKey.AFTER_SALES_TIME.getDesc());
                        Date orderLastReturnTime = DateUtils.addDays(DateUtils.getNowDateToDf(), Integer.parseInt(configValue));
                        //更新订单最后售后时间
                        this.baseMapper.updateByOrderBaseorderLastReturnTime(orderId, orderLastReturnTime);
                        //收货成功
                        result.setCode(CommonStatusEnum.SUCCESS.getCode());
                        result.setMessage(BizCodeEnume.GOODS_RECEIVED_SUCCESSFULLY.getMsg());
                        // 分佣订单售后时间更新
                        baseServiceClient.updateOrderAfterSale(orderId);
                        // 收货成功改变订单状态
                        payMentServiceClient.changeConsumeRecord(orderId);
                        // 收货成功，将金额存向卖家冻结金额
                        payMentServiceClient.addMoney(moneyMap);
                        //如果有子订单,更新子订单状态
                        if(!orderList.isEmpty()){
                            for (OrderBaseEntity entity:orderList){
                                // 如果未超过最晚收货时间，则更新订单商品和订单状态为确认收货，并计算并更新订单的最后退货时间
                                boolean isSuccessByOriginalOrder = orderGoodsMapper.update(null, new LambdaUpdateWrapper<OrderGoodsEntity>()
                                        .eq(OrderGoodsEntity::getOrderId, entity.getOrderId())
                                        .set(OrderGoodsEntity::getOrderGoodsStatus, orderStatus)
                                        .set(OrderGoodsEntity::getOrderGoodsFinishTime, orderFinishedTime)) > CommonStatusEnum.FAIL.getCode();

                                boolean isSuccessByOriginalGoods = this.baseMapper.update(null, new LambdaUpdateWrapper<OrderBaseEntity>()
                                        .eq(OrderBaseEntity::getOrderId, entity.getOrderId())
                                        .set(OrderBaseEntity::getOrderStatus, orderStatus)
                                        .set(OrderBaseEntity::getOrderFinishedTime, orderFinishedTime)) > CommonStatusEnum.FAIL.getCode();

                                //更新订单最后售后时间
                                this.baseMapper.updateByOrderBaseorderLastReturnTime(entity.getOrderId(), orderLastReturnTime);

                                // 收货成功改变订单状态
                                payMentServiceClient.changeConsumeRecord(entity.getOrderId());
                            }
                        }
                    } else {
                        result.setCode(CommonStatusEnum.FAIL.getCode());
                        result.setMessage(BizCodeEnume.GOODS_RECEIVED_FAILED.getMsg());
                    }
                }
                if (result.getCode() != CommonStatusEnum.FAIL.getCode() && (null == orderBaseEntity.getIntegralGoods() || 1 == orderBaseEntity.getIntegralGoods())) {
                    //查询积分规则
                    ResponseResult resultResp = baseServiceClient.queryIntegralRules();
                    Map<String, Integer> integralRules = (Map<String, Integer>) resultResp.getData();
                    //修改会员积分和会员等级
                    MemberMessageListIntegralDTO updateMemberMessageListIntegral = new MemberMessageListIntegralDTO();
                    //积比例
                    Integer ruleConsume = integralRules.get("ruleConsume");
                    if (ui.getPlusMember() != null && ui.getPlusMember() == 1) {
                        //查询用户是否开通plus会员 并且是否有折扣权益
                        ResponseResult selectplusmember = selectplusmemberjifen(userId);
                        String s = String.valueOf(selectplusmember.getData());
                        if (s != null && !s.isEmpty() && !"null".equals(s)) {
                            //倍率
                            ruleConsume = Integer.valueOf(s);
                        }
                    }
                    BigDecimal jifen = money.multiply(new BigDecimal(ruleConsume));
                    BigDecimal l = jifen.setScale(0, RoundingMode.HALF_UP);
                    Integer points = Integer.parseInt(l.toString());
                    ArrayList<String> list = new ArrayList<>();
                    list.add(userId);
                    updateMemberMessageListIntegral.setUserIdList(list);
                    updateMemberMessageListIntegral.setIntegral(points);
                    //1是增加积分 0 是扣除积分
                    updateMemberMessageListIntegral.setType(CommonType.commonStatus.YES.getCode());
                    userServiceClient.updateMemberIntegral(updateMemberMessageListIntegral);
                    //添加积分修改记录
                    Map<String, Object> pointParam = new HashMap<>();
                    pointParam.put("PointsLogType", CommonType.pointsLogType.GAIN.getCode());
                    pointParam.put("ClassId", CommonType.pointType.PURCHASE.getCode());
                    pointParam.put("PointsLogTime", DateUtils.getTime());
                    pointParam.put("PointsLogDesc", CommonType.pointType.PURCHASE.getDesc());
                    pointParam.put("PointsLogFlag", "reg");
                    pointParam.put("UserId", userId);
                    pointParam.put("orderId", orderId);
                    pointParam.put("PointsLogPoints", points);
                    pointParam.put("PointsLogOperate", CommonType.pointType.PURCHASE.getDesc());
                    pointParam.put("proportion", ruleConsume);
                    userServiceClient.insertPointLogByMap(pointParam);
                }
                if (result.getCode() != CommonStatusEnum.FAIL.getCode()){
                    // 分销：确认收货即订单完成 → 通知商城按分销关系生成佣金（幂等）
                    notifyDistributionFinished(orderBaseEntity);
                    //查询怡亚通订单信息
                    OrderYiyatongEntity orderYiyatongEntity = orderYiyatongMapper.selectOne(new LambdaQueryWrapper<OrderYiyatongEntity>()
                            .eq(OrderYiyatongEntity::getOrderId, orderId));
                    if(null!=orderYiyatongEntity){
                        ConfirmReceiveDTO dto=new ConfirmReceiveDTO();
                        dto.setOrderSn(orderYiyatongEntity.getOrderSn());
                        ResponseResult yiyatongResult=goodServiceClient.confirmReceive(dto);
                        if (yiyatongResult.getCode() != CommonStatusEnum.SUCCESS.getCode()){
                            return yiyatongResult;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                GlobalTransactionContext.reload(RootContext.getXID()).rollback();
                return ResponseResult.fail(e.getMessage());
            }
        } else {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        return result;
    }

    /**
     * 订单完成（确认收货）后通知商城分销模块：登记分销订单并生成佣金。
     *
     * <p>设计约束：
     * <ul>
     *   <li>只做「通知」，商城侧按 orderId 幂等处理，重复确认收货不会重复分佣；</li>
     *   <li>任何异常都被吞掉并记日志——收货是主流程，分销是旁路，不能因为分销失败
     *       让用户收不了货；</li>
     *   <li>未配置内部令牌时仍会调用（商城白名单放行），令牌缺失只影响审计不阻断链路。</li>
     * </ul>
     *
     * @param order 订单（含 shopId / buyerUserId / vkShareUserId / 实付金额）
     */
    private void notifyDistributionFinished(OrderBaseEntity order) {
        com.houjiang.order.utils.DistributionNotifyHelper.notifyFinished(
                mallDistributionClient, internalJobToken, orderGoodsMapper, order);
    }

    private void notifyDistributionCancelled(Object orderId, BigDecimal refundAmount, boolean fullInvalidate) {
        com.houjiang.order.utils.DistributionNotifyHelper.notifyCancel(
                mallDistributionClient, internalJobToken, orderId, refundAmount, fullInvalidate);
    }
    public ResponseResult selectplusmemberjifen(String userId) {
        // 根据用户ID查询Plus会员实体信息
        PlusMemberEntityVO map1 = this.baseMapper.selectplusmember(userId);
        if (map1 != null) {
            // 初始化权益内容字符串
            String string = null;

            // 获取会员权益信息
            String selectplusthali = map1.getPlusEquity();
            // 如果权益信息为空或仅包含空格，则返回权益为空的错误信息
            if (selectplusthali == null || "".equals(selectplusthali)) {
                return ResponseResult.fail(2, "权益为空", null);
            }
            // 将权益信息按逗号分隔，并转换为整数列表
            String[] split = selectplusthali.split(",");
            List<Integer> appIdList = Stream.of(split).map(Integer::parseInt).collect(Collectors.toList());
            // 根据权益ID列表查询权益详情
            List<PlusEquityDTO> entities = JSON.parseArray(JSONObject.toJSONString(userServiceClient.selectplusequitys(appIdList).getData()), PlusEquityDTO.class);
            // 如果权益详情为空或列表长度为0，则返回权益错误的错误信息
            if (entities == null || !(entities.size() > 0)) {
                return ResponseResult.fail(2, "权益错误", null);
            }
            // 遍历权益详情列表，查找权益类型为5的权益，并提取权益值
            for (PlusEquityDTO selectplusequity : entities) {
                if (selectplusequity.getEquityType() == 5) {
                    String equityValue = selectplusequity.getEquityValue();
                    // 根据冒号分隔权益值，获取权益具体内容
                    String[] strings = equityValue.split(":");
                    string = strings[1];
                }
            }
            // 返回成功结果，包含权益值
            return ResponseResult.success(1, "", string);
        }
        // 如果未查询到会员实体信息，则返回失败结果
        return ResponseResult.fail();

    }

    /**
     * 延长收货时间的逻辑处理。
     *
     * @param map 包含令牌和订单ID的映射表。
     * @return 返回处理结果。
     */
    @Override
    public ResponseResult delayToReceiveGoods(Map<Object, Object> map) {
        ResponseResult result = new ResponseResult();
        // 从映射表中获取令牌
        String token = (String) map.get("token");
        // 检查令牌在Redis中的存在性
        boolean isExist = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (isExist) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                UserInfoDTO ui = new UserInfoDTO();
                // 从Redis中获取用户信息
                // 获取user信息
                String userinfo = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + token);
                // 将字符串反序列化为UserInfoDTO对象
                // String转object
                ui = mapper.readValue(userinfo, UserInfoDTO.class);
                String userId = ui.getUser().getUserId();
                String orderId = (String) map.get("orderId");
                // 根据用户ID和订单ID查询订单信息
                // 订单信息
                OrderBaseEntity orderBaseEntity = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderBaseEntity>()
                        .eq(OrderBaseEntity::getBuyerUserId, userId).eq(OrderBaseEntity::getOrderId, orderId));
                Integer isAlreadyDelay = orderBaseEntity.getOrderDelayStatus();
                Date time = orderBaseEntity.getOrderReceiverDate();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String delayTime = sdf.format(time);
                // 如果订单已经延迟过，则提示用户
                if (isAlreadyDelay == 1) {
                    result.setMessage("该订单已延长至" + delayTime + "，请勿重复操作！");
                } else {
                    // 通过基线服务客户端调用Feign接口，获取配置值，该配置用于延长交付时间
                    String configValue = baseServiceClient.selectConfigValueFeign(CommonType.configKey.EXTEND_THE_DELIVERY_TIME.getDesc());
                    // 获取订单的接收日期
                    Date orderReceiverDate = orderBaseEntity.getOrderReceiverDate();
                    // 根据配置的延长交付时间值，计算出订单的最终交付日期
                    orderReceiverDate = DateUtils.addDays(orderReceiverDate, Integer.parseInt(configValue));
                    // 尝试更新订单的延迟状态
                    boolean isUpdateToDelayToReceiveGoods = this.baseMapper.updateDelayTime(userId, orderId, orderReceiverDate);
                    if (isUpdateToDelayToReceiveGoods) {
                        // 更新成功
                        result.setCode(CommonStatusEnum.SUCCESS.getCode());
                        result.setMessage("已成功为您延迟" + configValue + "天收货，请注意自动收货时间！");
                    } else {
                        // 更新失败
                        result.setCode(CommonStatusEnum.FAIL.getCode());
                        result.setMessage("订单延长出错！");
                    }
                }
                return result;
            } catch (Exception e) {
                result.setCode(500);
                log.info("DelayToReceiveGoodsErrorResults:" + e.getMessage());
                // 返回失败结果
                return ResponseResult.fail(e.getMessage());
            }
        } else {
            // 令牌无效
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
    }

    @Override
    public ResponseResult enquireSoldOrders(Map<String, Object> map) {
        String token = (String) map.get("token");
        boolean isExist = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (isExist) {
            try {
                UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
                String userId = map.get("userId") == null ? ui.getUserId() : (String) map.get("userId");
                String orderId = (String) map.get("orderId");
                // 查询订单信息
                SoldOrderByCommonalityDetailsVO sobcd = this.baseMapper.soldOrderByCommonalityDetails(userId, orderId);
                if (null != sobcd.getGiveawayInfo() && !sobcd.getGiveawayInfo().isEmpty()) {
                    List<FullReductionGiftsVO> list = JsonUtils.string2Obj(sobcd.getGiveawayInfo(), List.class, FullReductionGiftsVO.class);
                    sobcd.setZengpinInfo(list);
                }
                //查询当前订单是否改价 如果 改价必须全单退
                Integer orderNum = orderGoodsMapper.queryOrderUpdatePrice(map);
                if (orderNum == 0) {
                    sobcd.setUpdatePrice(false);
                } else {
                    if (!StringUtils.isPositiveNumber(sobcd.getOrderAdjustFee())) {
                        sobcd.setOrderAdjustFee(String.valueOf(Double.parseDouble(sobcd.getOrderAdjustFee()) * -1));
                    }
                    sobcd.setUpdatePrice(true);
                }

                if (ui != null) {
                    sobcd.setUserInfo(ui);
                }
                //判断是否是门店自提订单，是则远程调用查询门店信息
                if (sobcd.getOrderIsShopTake() != null && sobcd.getOrderIsShopTake() == 1) {
                    if (sobcd.getOrderGateShopId() != null) {
                        //远程调用查询门店信息
                        Map<String, Object> param = new HashMap<>();
                        param.put("shopGateId", sobcd.getOrderGateShopId());
                        ResponseResult resultResp = merchantServiceClient.queryGateDetail(param);
                        GateDetailDTO gateDetailDTO = JSONObject.parseObject(JSONObject.toJSONString(resultResp.getData()), GateDetailDTO.class);
                        if (gateDetailDTO != null) {
                            sobcd.setGateDetailDTO(gateDetailDTO);
                        }
                    }
                }
                //骑手id 不为空 时查询 骑手信息
                if (!StringUtils.isEmpty(sobcd.getDistributorId()) && !"0".equals(sobcd.getDistributorId())) {
                    Map<String, Object> riderInfo = this.baseMapper.queryRiderInfo(sobcd.getDistributorId());
                    sobcd.setRiderName(null != riderInfo && riderInfo.get("riderName") != null ? riderInfo.get("riderName").toString() : null);
                    sobcd.setRiderTel(null != riderInfo && riderInfo.get("riderTel") != null ? riderInfo.get("riderTel").toString() : null);
                }
                BigDecimal vPrice = new BigDecimal(0);
                // 优惠券总金额
                BigDecimal voucherTotalPrice = new BigDecimal(0);
                // 单一店铺优惠券金额
                BigDecimal voucherPrice = sobcd.getVoucherPrice();
                if (voucherPrice == null) {
                    vPrice = new BigDecimal(0);
                    voucherTotalPrice = voucherTotalPrice.add(vPrice);
                } else {
                    voucherTotalPrice = voucherTotalPrice.add(voucherPrice);
                }

                if (null != sobcd.getOrderBuyId()) {
                    ResponseResult orderPintuanMarkEntity = activityServiceClient.querySurplusNum(sobcd.getOrderBuyId());
                    if (!ObjectUtils.isEmpty(orderPintuanMarkEntity.getData())) {
                        Integer numberesultemaining = Integer.valueOf(orderPintuanMarkEntity.getData().toString());
                        sobcd.setNumberRemaining(numberesultemaining);
                    }
                }
                // 配送公司id
                Integer orderShippingExpressId = sobcd.getOrderShippingExpressId();
                if(orderShippingExpressId != null) {
                    // 获取快递信息
                    ResponseResult expressResult = baseServiceClient.getBaseExpressInfoById(orderShippingExpressId);
                    if (expressResult.getData() != null) {
                        Map<String, Object> orderShippingExpressName = (Map<String, Object>) expressResult.getData();
                        sobcd.setOrderShippingExpressName(orderShippingExpressName.get("expressName").toString());
                    }
                }
                // 红包拆分价格
                BigDecimal orderesultptPrice = sobcd.getOrderRptPrice();
                // 红包
                sobcd.setRedpacketPrice(orderesultptPrice);
                Integer orderesultefundStatus = sobcd.getOrderRefundStatus();
                Integer orderesulteturnStatus = sobcd.getOrderReturnStatus();
                // 订单状态
                Integer normalStatus = sobcd.getOrderStatus();
                //虚拟订单 0 是 1 否
                Integer virtualGoods = sobcd.getVirtualGoods();
                // 1.线上支付  2.线下支付 3.货到付款
                Integer paymentCode = sobcd.getPaymentCode();
                if (null != paymentCode && 2 == paymentCode) {
                    //查询线下支付的审核状态
                    OrderPaymentVocherEntity PaymentVocherPo = orderPaymentVocherMapper.selectOne(new LambdaQueryWrapper<OrderPaymentVocherEntity>()
                            .eq(OrderPaymentVocherEntity::getOrderId, orderId));
                    if (null != PaymentVocherPo) {
                        sobcd.setReviewStatus(PaymentVocherPo.getReviewStatus());
                    }
                }
                // 涉及退款退货订单状态
                Integer status = null;
                if (normalStatus.equals(CommonType.orderStatus.TO_BE_SHIPPED.getCode()) || (null != virtualGoods && 0 == virtualGoods && CommonType.orderStatus.SHIPPED.getCode().equals(normalStatus))) {
                    // 未发货状态
                    if (orderesultefundStatus.equals(CommonType.orderRefundStatus.REFUNDING.getCode())) {
                        // 未发货退款中
                        status = 22;
                    } else if (orderesultefundStatus.equals(CommonType.orderRefundStatus.REFUND_COMPLETED.getCode())) {
                        // 商家拒绝或审核通过，继续发货，但不可再次申请退款
                        status = 26;
                    } else if (orderesultefundStatus.equals(CommonType.orderRefundStatus.NO_REFUND.getCode()) && null != virtualGoods && 0 == virtualGoods) {
                        status = normalStatus;
                    } else {
                        status = 3;
                    }
                } else if (normalStatus.equals(CommonType.orderStatus.CONFIRM_RECEIPT.getCode())) {
                    // 已完成状态
                    if (orderesultefundStatus.equals(CommonType.orderRefundStatus.REFUNDING.getCode()) || orderesultefundStatus.equals(CommonType.orderRefundStatus.REFUND_FAILED.getCode())) {
                        // 收货退款中
                        status = 23;
                    } else if (orderesultefundStatus.equals(CommonType.orderRefundStatus.REFUND_COMPLETED.getCode())) {
                        status = 25;
                    } else if (orderesultefundStatus.equals(CommonType.orderRefundStatus.NO_REFUND.getCode()) && orderesulteturnStatus.equals(CommonType.orderReturnStatus.NO_RETURNS.getCode())) {
                        status = 6;
                    } else if (orderesulteturnStatus.equals(CommonType.orderReturnStatus.RETURNING_GOODS.getCode())) {
                        // 收货退货中
                        status = 24;
                    } else if (orderesulteturnStatus.equals(CommonType.orderReturnStatus.RETURN_COMPLETED.getCode())) {
                        status = 25;
                    } else {
                        status = 6;
                    }
                    if (null != sobcd.getOrderLastReturnTime() && DateUtils.compareDate(sobcd.getOrderLastReturnTime(), DateUtils.getNowDateToString())
                            && 1 == virtualGoods) {
                        // 订单超过完成时间不可售后
                        status = 27;
                    }
                } else {
                    status = normalStatus;
                }
                // 存放最新状态
                sobcd.setOrderStatus(status);

                // 查询商品信息
                List<SoldOrderByGoodsDetailsVO> soldOrderByGoodsDetailsList = new ArrayList<>();
                List<SoldOrderByGoodsDetailsVO> sobgdList = this.baseMapper.soldOrderByGoodsDetails(userId, orderId);
                for (SoldOrderByGoodsDetailsVO soldOrderByGoodsDetails : sobgdList) {
                    // 退货状态
                    Integer goodsRefundStatus = soldOrderByGoodsDetails.getGoodsRefundStatus();
                    // 退款状态
                    Integer goodsReturnStatus = soldOrderByGoodsDetails.getGoodsReturnStatus();
                    if (goodsRefundStatus.equals(CommonType.orderRefundStatus.NO_REFUND.getCode())) {
                        // 不涉及退货(申请售后)
                        soldOrderByGoodsDetails.setGoodsStatus(0);
                    } else if (goodsRefundStatus.equals(CommonType.orderRefundStatus.REFUNDING.getCode())) {
                        // 仅退货(退货进行中)
                        soldOrderByGoodsDetails.setGoodsStatus(1);
                    } else if (goodsRefundStatus.equals(CommonType.orderRefundStatus.REFUND_COMPLETED.getCode())) {
                        // 仅退货(退货完成)
                        soldOrderByGoodsDetails.setGoodsStatus(2);
                    } else if (goodsReturnStatus.equals(CommonType.orderReturnStatus.NO_RETURNS.getCode())) {
                        // 不涉及退款(申请售后)
                        soldOrderByGoodsDetails.setGoodsStatus(0);
                    } else if (goodsReturnStatus.equals(CommonType.orderReturnStatus.RETURNING_GOODS.getCode())) {
                        // 仅退款(退款进行中)
                        soldOrderByGoodsDetails.setGoodsStatus(3);
                    } else if (goodsReturnStatus.equals(CommonType.orderReturnStatus.RETURN_COMPLETED.getCode())) {
                        // 仅退款(退款完成)
                        soldOrderByGoodsDetails.setGoodsStatus(4);
                    }
                    soldOrderByGoodsDetailsList.add(soldOrderByGoodsDetails);
                    sobcd.setSoldOrderByGoodsDetailsList(soldOrderByGoodsDetailsList);
                    //若为未付款秒杀订单，显示剩余支付时间
                    if (1 == status) {
                        Date seckillCancelTime = soldOrderByGoodsDetails.getSeckillCancelTime();
                        if (null != seckillCancelTime) {
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            try {
                                if (new Date().before(seckillCancelTime)) {
                                    Long overplusTime = seckillCancelTime.getTime();
                                    if (sobcd.getSeckillExpireTime() == null) {
                                        sobcd.setSeckillExpireTime(overplusTime);
                                    } else if (sobcd.getSeckillExpireTime() > overplusTime) {
                                        sobcd.setSeckillExpireTime(overplusTime);
                                    }

                                }
                            } catch (Exception e) {
                                log.error(e.getMessage());
                            }
                        }
                    }
                }

                // 根据订单ID查询退货信息
                OrderReturnEntity entity = orderReturnMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>()
                        .eq(OrderReturnEntity::getOrderId, orderId));
                // 如果退货信息存在，则设置相应的退货物流信息
                if (entity != null) {
                    sobcd.setReturnShippingCode(entity.getReturnShippingCode());
                    sobcd.setReturnPostId(entity.getReturnPostCode());
                }
                // 调用基础服务客户端，查询物流列表
                ResponseResult res = baseServiceClient.queryExpressList(map);
                // 如果查询结果存在，则遍历列表以查找与退货物流ID匹配的物流信息
                if (null != res.getData()) {
                    List<LinkedHashMap<Object, Object>> list = (List<LinkedHashMap<Object, Object>>) res.getData();
                    for (LinkedHashMap<Object, Object> expresult : list) {
                        // 如果物流ID存在且与退货物流ID匹配，则设置物流名称
                        if (null != expresult.get("expressId") && null != sobcd.getReturnPostId()) {
                            if (expresult.get("expressId").toString().equals(sobcd.getReturnPostId().toString())) {
                                sobcd.setReturnPostName((String) expresult.get("expressName"));
                            }
                        }
                    }
                }

                // 判断集合是否为空
                if (sobcd == null) {
                    // 为空输出信息
                    return ResponseResult.fail(BizCodeEnume.SELECT_DATA_NULL);
                } else {
                    return ResponseResult.success(sobcd);
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.info("EnquireSoldOrdersErrorResults:" + e.getMessage());
                return ResponseResult.fail(e.getMessage());
            }
        } else {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
    }

    /**
     * 添加评论信息
     * 使用全局事务确保评论和评分的一致性
     *
     * @param cl 评论列表数据传输对象，包含评论内容和相关订单、商品信息
     * @return 添加评论后的响应结果
     * @throws TransactionException 如果交易异常
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public ResponseResult addComments(CommentsListDTO cl) throws TransactionException {
        try {
            ResponseResult result = new ResponseResult();
            String token = cl.getToken();
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
            if (userInfo != null) {
                String userId = userInfo.getUserId();
                String userName = userInfo.getUserName();
                String userAvatar = userInfo.getUserAvatar();
                String orderId = cl.getOrderId();
                Integer shopId = cl.getShopId();
                String resultType = null;
                Date createTime = DateUtils.getNowDate();
                // 评论显示状态
                Integer status = 1;
                String goodsName = null;
                String goodsSpec = null;
                Integer evaluationFrom = cl.getEvaluationFrom();
                // 描述相符评分
                Integer evaluationDesccredit = cl.getEvaluationDesccredit();
                // 服务态度评分
                Integer evaluationServicecredit = cl.getEvaluationServicecredit();
                // 发货速度评分
                Integer evaluationDeliverycredit = cl.getEvaluationDeliverycredit();
                // 查找店铺名
                OrderBaseEntity orderBaseEntity = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderBaseEntity>()
                        .eq(OrderBaseEntity::getBuyerUserId, userId).eq(OrderBaseEntity::getOrderId, orderId));
                String shopName = null;
                if (null != orderBaseEntity) {
                    shopName = orderBaseEntity.getShopName();
                }

                if (cl.getGoodsList().isEmpty()) {
                    // 店铺评分
                    Map<String, Object> param = new HashMap<>();
                    param.put("shopId", shopId);
                    param.put("shopName", shopName);
                    param.put("userId", userId);
                    param.put("userName", userName);
                    param.put("orderId", orderId);
                    param.put("evaluationDesccredit", evaluationDesccredit);
                    param.put("evaluationServicecredit", evaluationServicecredit);
                    param.put("evaluationDeliverycredit", evaluationDeliverycredit);
                    param.put("evaluationCreateTime", createTime);

                    result = merchantServiceClient.saveShopEvaluationInfo(param);
                    return result;
                } else {
                    for (int i = 0; i < cl.getGoodsList().size(); i++) {
                        Integer commonId = cl.getGoodsList().get(i).getCommonId();
                        Integer goodsId = cl.getGoodsList().get(i).getGoodsId();
                        // 是否匿名
                        Integer isAnonymous = cl.getIsanonymous();
                        String feedBack = cl.getGoodsList().get(i).getFeedback();
                        Integer score = cl.getGoodsList().get(i).getScores();
                        switch (score) {
                            case 5:
                                resultType = "good";
                                break;
                            case 4:
                                resultType = "good";
                                break;
                            case 3:
                                resultType = "neutral";
                                break;
                            case 2:
                                resultType = "bad";
                                break;
                            case 1:
                                resultType = "bad";
                                break;
                        }
                        List<String> imagesList = cl.getGoodsList().get(i).getImageList();
                        String images = StringUtils.join(imagesList, ",");
                        List<OrderGoodsEntity> goodsDetails = orderGoodsMapper.selectList(new LambdaQueryWrapper<OrderGoodsEntity>()
                                .eq(OrderGoodsEntity::getOrderId, orderId)
                                .eq(OrderGoodsEntity::getGoodsId, goodsId));

                        goodsName = goodsDetails.get(0).getGoodsName();
                        goodsSpec = goodsDetails.get(0).getOrderSpecChineseValue();

                        Map<String, Object> mapGoods = new HashMap<>();
                        mapGoods.put("userId", userId);
                        mapGoods.put("userName", userName);
                        mapGoods.put("userAvatar", userAvatar);
                        mapGoods.put("orderId", orderId);
                        mapGoods.put("shopId", shopId);
                        mapGoods.put("shopName", shopName);
                        mapGoods.put("commonId", commonId);
                        mapGoods.put("goodsId", goodsId);
                        mapGoods.put("goodsName", goodsName);
                        mapGoods.put("goodsSpec", goodsSpec);
                        mapGoods.put("score", score);
                        mapGoods.put("result", resultType);
                        mapGoods.put("feedBack", feedBack);
                        mapGoods.put("isAnonymous", isAnonymous);
                        mapGoods.put("createTime", createTime);
                        mapGoods.put("status", status);
                        mapGoods.put("images", images);
                        mapGoods.put("evaluationFrom", evaluationFrom);
                        boolean isInsertByGoods = orderGoodsEvaluationMapper.commentsInsertByGoods(mapGoods);
                        Map<String, Object> map = new HashMap<>();
                        map.put("commonId", commonId);
                        goodServiceClient.updateCommentCount(map);
                        List<Integer> list = new ArrayList<>();
                        list.add(commonId);
                        map.put("commonIdS", list);
                        // 1 是添加  2是删除
                        map.put("type", 1);
                        executorService.submit(() -> {
                            //发送消息到MQ
                            MqSendUtils.mqSendGoods(map);
                        });

                        // 商品评价
                        if (isInsertByGoods) {
                            result.setMessage(CommonStatusEnum.SUCCESS.getValue());
                            // 评价成功，改变状态
                            this.baseMapper.update(null, new LambdaUpdateWrapper<OrderBaseEntity>()
                                    .eq(OrderBaseEntity::getBuyerUserId, userId)
                                    .eq(OrderBaseEntity::getOrderId, orderId)
                                    .set(OrderBaseEntity::getOrderBuyerEvaluationStatus, 1));

                            orderGoodsMapper.update(null, new LambdaUpdateWrapper<OrderGoodsEntity>()
                                    .eq(OrderGoodsEntity::getBuyerUserId, userId)
                                    .eq(OrderGoodsEntity::getOrderId, orderId)
                                    .set(OrderGoodsEntity::getOrderGoodsEvaluationStatus, 1));

                        } else {
                            result.setMessage(CommonStatusEnum.FAIL.getValue());
                        }
                    }
                    // 店铺评分
                    Map<String, Object> param = new HashMap<>();
                    param.put("shopId", shopId);
                    param.put("shopName", shopName);
                    param.put("userId", userId);
                    param.put("userName", userName);
                    param.put("orderId", orderId);
                    param.put("evaluationDesccredit", evaluationDesccredit);
                    param.put("evaluationServicecredit", evaluationServicecredit);
                    param.put("evaluationDeliverycredit", evaluationDeliverycredit);
                    param.put("evaluationCreateTime", createTime);

                    result = merchantServiceClient.saveShopEvaluationInfo(param);
                }
            } else {
                return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            // 回滚
            GlobalTransactionContext.reload(RootContext.getXID()).rollback();
            return ResponseResult.fail(e.getMessage());
        }
    }

    /**
     * 根据用户token查询订单统计数据
     *
     * @param map 包含用户token等信息的映射
     * @return 返回订单统计结果
     */
    @Override
    public ResponseResult ordersTotal(Map<Object, Object> map) {
        ResponseResult rr = new ResponseResult();
        BusinessManageOrderTotalVO omot = new BusinessManageOrderTotalVO();
        // 从map中获取用户token
        String token = (String) map.get("token");
        // 检查用户token是否存在（使用新key前缀 HOU_USER_TOKEN_USER，与auth服务保持一致）
        boolean isExist = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (isExist) {
            try {
                UserInfoVO ui = new UserInfoVO();
                // 从Redis中获取用户信息
                ui = ParseTokenUtils.getUserInfoByToken(token);
                // 当前用户没有 shopId（如云端 SaaS 管理后台用户）：订单中心的统计 SQL 强依赖 shop_id，
                // 数据模型层面也没有 org_id 维度。这里不再粗暴抛「权限不足」，改为返回全 0 的统计占位，
                // 避免前端首页卡片报错或弹错误 toast。
                if (null == ui || null == ui.getShopId() || ui.getShopId().isEmpty()) {
                    omot.setWaitingForPayment(0);
                    omot.setWaitForTheDelivery(0);
                    omot.setShipped(0);
                    omot.setOffTheStocks(0);
                    omot.setSalesReturn(0);
                    omot.setRefund(0);
                    omot.setAfterSales(0);
                    omot.setEvaluate(0);
                    rr.setCode(1);
                    rr.setData(omot);
                    return rr;
                }
                // 获取店铺ID
                Integer shopId = Integer.valueOf(ui.getShopId());
                // shopGateId 在云端管理后台同样可能为空，做兜底
                Integer shopGateId = (ui.getShopGateId() != null && !ui.getShopGateId().isEmpty())
                        ? Integer.valueOf(ui.getShopGateId()) : null;
                // 初始化订单来源，默认为2
                Integer orderSource = 2;
                // 如果店铺类型为2，订单来源改为1
                if (null != ui.getShopType() && 2 == ui.getShopType()) {
                    //查询待发货订单
                    orderSource = 1;
                }
                // 分别查询不同状态的订单数量
                // 待付款
                Integer WaitingForPayment = this.baseMapper.WaitingForPaymentTotal(shopGateId,shopId, orderSource);
                // 待发货
                Integer WaitForTheDelivery = this.baseMapper.WaitForTheDeliveryTotal(shopGateId,shopId, orderSource);
                // 已发货
                Integer Shipped = this.baseMapper.shippedTotal(shopGateId,shopId, orderSource);
                // 已完成
                Integer OffTheStocks = this.baseMapper.OffTheStocksTotal(shopGateId,shopId, orderSource);
                // 退货订单
                Integer SalesReturn = this.baseMapper.SalesReturnTotal(shopGateId,shopId, orderSource);
                // 退款订单
                Integer Refund = this.baseMapper.RefundTotal2(shopGateId,shopId, orderSource);
                // 设置订单统计数据到返回对象
                omot.setWaitingForPayment(WaitingForPayment);
                omot.setWaitForTheDelivery(WaitForTheDelivery);
                omot.setShipped(Shipped);
                omot.setOffTheStocks(OffTheStocks);
                omot.setSalesReturn(SalesReturn);
                omot.setRefund(Refund);
                // 设置成功代码和数据到返回结果
                rr.setCode(1);
                rr.setData(omot);
            } catch (Exception e) {
                rr.setCode(500);
                log.info("OrdersTotalErrorResults:" + e.getMessage());
            }
        } else {
            // 如果token不存在，设置失败代码和消息
            rr.setCode(CommonStatusEnum.FAIL.getCode());
            rr.setMessage(BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        return rr;
    }

    @Override
    public ResponseResult shopAppOrdersTotal(Map<Object, Object> map) {
        // 通过token获取用户信息，用于身份验证
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        // 如果用户信息为空，表示身份验证失败，返回相应的错误信息
        if (ui == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 解析用户所属店铺的ID
        Integer shopId = Integer.parseInt(ui.getShopId());
        Integer shopGateId = Integer.valueOf(ui.getShopGateId());
        // 初始化订单统计对象
        BusinessManageOrderTotalVO omot = new BusinessManageOrderTotalVO();
        // 默认订单来源为2，代表普通店铺
        Integer orderSource = 2;
        // 如果店铺类型为2（特殊店铺），则订单来源改为1
        if (null != ui.getShopType() && 2 == ui.getShopType()) {
            //查询待发货订单
            orderSource = 1;
        }
        // 分别查询各类订单的数量
        // 待付款
        Integer WaitingForPayment = this.baseMapper.WaitingForPaymentTotal(shopGateId,shopId, orderSource);
        // 待发货
        Integer WaitForTheDelivery = this.baseMapper.WaitForTheDeliveryTotal(shopGateId,shopId, orderSource);
        // 退货订单
        Integer SalesReturn = this.baseMapper.SalesReturnTotal(shopGateId,shopId, orderSource);
        // 退款订单
        Integer Refund = this.baseMapper.RefundTotal2(shopGateId,shopId, orderSource);
        // 待评价订单
        Integer evaluate = this.baseMapper.evaluateOrder(shopGateId,shopId);
        // 将查询到的订单数量填充到订单统计对象中
        omot.setWaitingForPayment(WaitingForPayment);
        omot.setWaitForTheDelivery(WaitForTheDelivery);
        omot.setSalesReturn(SalesReturn);
        omot.setRefund(Refund);
        // 计算售后服务订单总数（退货+退款）
        omot.setAfterSales(SalesReturn + Refund);
        omot.setEvaluate(evaluate);
        // 返回订单统计结果
        return ResponseResult.success(omot);
    }

    /**
     * 根据用户token查询销售排名信息。
     *
     * @param map 包含token等信息的参数映射。
     * @return 返回查询结果的响应对象。
     */
    @Override
    public ResponseResult salesNo(Map<Object, Object> map) {
        ResponseResult rr = new ResponseResult();
        // 从映射中获取token
        String token = (String) map.get("token");
        // 检查token在Redis中的存在性
        boolean isExist = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (isExist) {
            try {
                UserInfoVO ui = new UserInfoVO();
                // 从Redis中获取用户信息
                ui = ParseTokenUtils.getUserInfoByToken(token);
                if(null == ui || null == ui.getShopId()){
                    rr.setCode(CommonStatusEnum.FAIL.getCode());
                    rr.setMessage(BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
                    return rr;
                }
                Integer shopId = Integer.valueOf(ui.getShopId());
                Integer shopGateId = Integer.valueOf(ui.getShopGateId());
                // 查询销售排名信息
                List<SalesRankingVO> sr = this.baseMapper.querySalesRanking(shopGateId,shopId);
                // 设置响应结果
                if (sr != null) {
                    rr.setCode(CommonStatusEnum.SUCCESS.getCode());
                    rr.setData(sr);
                    rr.setMessage(BizCodeEnume.OPERATION_SUCCESSFUL.getMsg());
                } else {
                    rr.setCode(CommonStatusEnum.FAIL.getCode());
                    rr.setMessage(BizCodeEnume.OPERATION_FAILED.getMsg());
                }
            } catch (Exception e) {
                rr.setCode(500);
                log.info("SalesNoErrorResults:" + e.getMessage());
            }
        } else {
            // 如果token不存在，设置相应的错误响应
            rr.setCode(CommonStatusEnum.FAIL.getCode());
            rr.setMessage(BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        return rr;
    }

    /**
     * 根据传入的映射对象计算销售情况。
     * 此方法主要用于根据用户的token和商店类型来查询特定商店的销售数据。
     *
     * @param map 映射对象，包含token和shopType等信息。
     * @return 返回计算结果的响应对象。
     */
    @Override
    public ResponseResult calculatedSales(Map<Object, Object> map) {
        ResponseResult rr = new ResponseResult();
        // 从map中获取token和shopType
        String token = (String) map.get("token");
        // 检查token在Redis中的存在性
        boolean isExist = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (isExist) {
            try {
                UserInfoVO ui = new UserInfoVO();
                // 从Redis中获取用户信息
                ui = ParseTokenUtils.getUserInfoByToken(token);
                Integer shopType = ui.getShopType();
                Integer shopId = Integer.valueOf(ui.getShopId());
                Integer shopGateId = Integer.valueOf(ui.getShopGateId());
                // 根据shopId和shopType查询销售数据
                // 销售额列表-今日,本周,本月
                List<SaleRoomVO> srList = this.baseMapper.querySaleByShopId(shopGateId,shopId, shopType);
                srList.forEach(s -> {
                    s.setOrderTotalPrice(s.getOrderTotalPrice() == null ? BigDecimal.ZERO : s.getOrderTotalPrice());
                    s.setSalesVolumePrice(s.getSalesVolumePrice() == null ? BigDecimal.ZERO : s.getSalesVolumePrice());
                });
                return ResponseResult.success(srList);
            } catch (Exception e) {
                rr.setCode(500);
                log.info("CalculatedSalesErrorResults:" + e.getMessage());
            }
        } else {
            // 如果token不存在，则返回失败信息
            rr.setCode(CommonStatusEnum.FAIL.getCode());
            rr.setMessage(BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        return rr;
    }


    @Override
    public ResponseResult queryVirtualOrderByEvaluation(Map<String, Object> map) {
        String token = (String) map.get("token");
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
        String userId = ui.getUserId();
        // 待评价
        Integer waitingEvaluation = 0;
        // 已完成
        Integer offTheStocks = 6;
        IPage<OrderListVO> orderList;
        Map<String, Object> mapOrder = new HashMap<>();

        mapOrder.put("waitingEvaluation", waitingEvaluation);
        mapOrder.put("offTheStocks", offTheStocks);
        if (null == map.get("virtualGoods") || (Integer) map.get("virtualGoods") == 0) {
            mapOrder.put("userId", userId);
            //查询待评价虚拟订单商品信息
            orderList = this.baseMapper.queryWaitingEvaluationVirtualOrder(new Query<OrderListVO>().getPage(map), mapOrder);
        } else {
            String buyUserId = map.get("buyUserId") == null ? "0" : map.get("buyUserId").toString();
            List<String> userIdList = getEmployees(ui, userId, buyUserId);
            mapOrder.put("userIdList", userIdList);
            //查询待评价非虚拟订单商品信息
            orderList = this.baseMapper.queryWaitingEvaluationOrder(new Query<OrderListVO>().getPage(map), mapOrder);
        }
        BigDecimal paymentPrice = new BigDecimal(0);
        for (int i = 0; i < orderList.getRecords().size(); i++) {
            Integer goodsNum = 0;
            String orderId = orderList.getRecords().get(i).getOrderId();
            // 查询待评价商品信息
            List<GoodsDetailsListVO> goodsDetailsList = this.baseMapper.queryGoodsList(orderId);
            orderList.getRecords().get(i).setGoodsDetailsList(goodsDetailsList);
            // 退款状态
            Integer orderRefundStatus = orderList.getRecords().get(i).getOrderRefundStatus();
            // 退货状态
            Integer orderReturnStatus = orderList.getRecords().get(i).getOrderReturnStatus();
            // 换货状态
            Integer orderBarterStatus = orderList.getRecords().get(i).getOrderBarterStatus();
            // 订单状态
            Integer normalStatus = orderList.getRecords().get(i).getOrderStatus();
            // 虚拟订单
            Integer virtualGoods = orderList.getRecords().get(i).getVirtualGoods();
            // 涉及退款退货订单状态
            Integer status = null;
            if (normalStatus.equals(CommonType.orderStatus.TO_BE_SHIPPED.getCode()) ||
                    (virtualGoods != null && 0 == virtualGoods && 4 == normalStatus)) {
                // 未发货状态
                if (orderRefundStatus.equals(CommonType.orderRefundStatus.REFUNDING.getCode())) {
                    // 未发货退款中
                    status = 22;
                } else if (orderRefundStatus.equals(CommonType.orderRefundStatus.REFUND_COMPLETED.getCode())) {
                    // 商家拒绝或审核通过，继续发货，但不可再次申请退款
                    status = 26;
                } else if (orderRefundStatus.equals(CommonType.orderRefundStatus.NO_REFUND.getCode())) {
                    status = 3;
                }
            } else if (normalStatus.equals(CommonType.orderStatus.CONFIRM_RECEIPT.getCode())) {
                // 已完成状态
                if (orderRefundStatus.equals(CommonType.orderRefundStatus.REFUNDING.getCode())) {
                    // 收货退款中
                    status = 23;
                } else if (orderRefundStatus.equals(CommonType.orderRefundStatus.REFUND_COMPLETED.getCode())) {
                    status = 25;
                } else if (orderRefundStatus.equals(CommonType.orderRefundStatus.NO_REFUND.getCode())
                        && orderReturnStatus.equals(CommonType.orderReturnStatus.NO_RETURNS.getCode())
                        && orderBarterStatus.equals(CommonType.orderBarterStatus.NO_BARTER.getCode())) {
                    status = 6;
                } else if (orderReturnStatus.equals(CommonType.orderReturnStatus.RETURNING_GOODS.getCode())) {
                    // 收货退货中
                    status = 24;
                } else if (orderReturnStatus.equals(CommonType.orderReturnStatus.RETURN_COMPLETED.getCode())) {
                    status = 25;
                } else if(orderBarterStatus.equals(CommonType.orderBarterStatus.BARTERING.getCode()) ||
                        orderBarterStatus.equals(CommonType.orderBarterStatus.BARTER_FAILED.getCode())){
                    // 换货中
                    status = 29;
                } else if(orderBarterStatus.equals(CommonType.orderBarterStatus.BARTER_COMPLETED.getCode())){
                    // 换货完成
                    status = 30;
                }
            } else {
                status = normalStatus;
            }
            // 存放最新状态
            orderList.getRecords().get(i).setOrderStatus(status);
            for (GoodsDetailsListVO goodsDetailsListVO : goodsDetailsList) {
                // 计算共有几件商品
                Integer goodsnum = goodsDetailsListVO.getOrderGoodsNum();
                goodsNum += goodsnum;
                orderList.getRecords().get(i).setGoodsNum(goodsNum);

                // 计算商品总价
                // 单个商品价格
                BigDecimal orderGoodsAmount = goodsDetailsListVO.getOrderGoodsAmount();
                paymentPrice = paymentPrice.add(orderGoodsAmount).setScale(2, RoundingMode.UP);
            }
            // 退款实际价格
            orderList.getRecords().get(i).setRefundRealPrice(orderList.getRecords().get(i).getOrderGoodsAmount());
            // 总付款价格
            orderList.getRecords().get(i).setShopTotalPrice(orderList.getRecords().get(i).getOrderPaymentAmount());
            orderList.getRecords().get(i).setOrderTotalPrice(orderList.getRecords().get(i).getOrderPaymentAmount());
        }
        return ResponseResult.success(PageUtils.getPage(orderList));
    }

    @Override
    public ResponseResult queryIntegralOrders(Map<String, Object> map) {
        String token = (String) map.get("token");
        boolean isExist = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (isExist) {
            String userId = null;
            try {
                ObjectMapper mapper = new ObjectMapper();
                // 获取user信息
                String userinfo = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + token);
                // String转object
                UserInfoDTO dto = mapper.readValue(userinfo, UserInfoDTO.class);
                UserInfoVO ui = dto.getUser();
                // 发送人id
                userId = ui.getUserId();
                // 订单状态
                Integer orderStatus = Integer.valueOf(map.get("orderStatus").toString());
                Integer orderStatusNum = null;
                if (orderStatus == 1) {
                } else if (orderStatus == 2) {
                    // 待付款
                    orderStatusNum = 1;
                } else if (orderStatus == 3) {
                    // 待发货
                    orderStatusNum = 3;
                } else if (orderStatus == 4) {
                    // 待收货
                    orderStatusNum = 4;
                } else if (orderStatus == 5) {
                    // 已完成
                    orderStatusNum = 6;
                }
                IPage<OrderBaseEntity> page = this.page(new Query<OrderBaseEntity>().getPage(map),
                        new LambdaQueryWrapper<OrderBaseEntity>().eq(OrderBaseEntity::getBuyerUserId, userId)
                                .eq(OrderBaseEntity::getIntegralGoods, 0)
                                .eq(OrderBaseEntity::getOrderDel, 0)
                                .eq(orderStatusNum != null && orderStatusNum == 3, OrderBaseEntity::getOrderStatus, orderStatusNum)
                                .ne(orderStatusNum != null && orderStatusNum == 3, OrderBaseEntity::getGroupbuyStatus, 1)
                                .eq(orderStatusNum != null && orderStatusNum != 3, OrderBaseEntity::getOrderStatus, orderStatusNum)
                                .orderByDesc(OrderBaseEntity::getOrderCreateTime));
                // 商品总价
                BigDecimal paymentPrice = new BigDecimal(0);
                for (int i = 0; i < page.getRecords().size(); i++) {
                    Integer goodsNum = 0;
                    String orderId = page.getRecords().get(i).getOrderId();
                    List<GoodsDetailsListVO> goodsDetailsList = orderGoodsMapper.queryGoodsList(orderId);
                    page.getRecords().get(i).setGoodsDetailsList(goodsDetailsList);
                    // 订单状态
                    Integer normalStatus = page.getRecords().get(i).getOrderStatus();
                    // 涉及退款退货订单状态
                    Integer status = null;
                    status = normalStatus;
                    // 存放最新状态
                    page.getRecords().get(i).setOrderStatus(status);
                    for (int j = 0; j < goodsDetailsList.size(); j++) {
                        // 计算共有几件商品
                        Integer goodsnum = goodsDetailsList.get(j).getOrderGoodsNum();
                        goodsNum += goodsnum;
                        page.getRecords().get(i).setGoodsNum(goodsNum);

                        // 计算商品总价
                        // 单个商品价格
                        BigDecimal orderGoodsAmount = goodsDetailsList.get(j).getOrderGoodsAmount();
                        paymentPrice = paymentPrice.add(orderGoodsAmount).setScale(2, BigDecimal.ROUND_UP);

                        //根据SPU获取商品状态   -- 商品状态 0下架，1正常，10违规（禁售）,2店铺下架商品下架,3商品删除
                        Integer commonId = goodsDetailsList.get(j).getCommonId();
                        Integer goodsid = goodsDetailsList.get(j).getGoodsId();
                        ResponseResult rr2 = goodServiceClient.getGoodsDetails(goodsid);
                        if (rr2.getCode() != 1) {
                            return ResponseResult.fail(BizCodeEnume.NO_INFORMATION_OBTAINED);
                        } else {
                            String goodsDetails = rr2.getData().toString();
                            // json转对象
                            ReceiveGoodsDetailsDTO receiveGoodsDetails = JsonUtils.string2Obj(goodsDetails,
                                    ReceiveGoodsDetailsDTO.class);
                            goodsDetailsList.get(j).setGoodsIntegral(receiveGoodsDetails.getNeedIntegral());
                        }
                        Integer commonState = this.baseMapper.selCommonStateBySPU(commonId);
                        goodsDetailsList.get(j).setCommonState(commonState);
                    }
                    System.out.println("-----groupBuyStatus----" + page.getRecords().get(i).getOrderBuyId());
                    /**
                     * 检查订单是否具有拼团标记信息
                     * 如果订单的购买ID不为空，那么该订单可能参与了拼团活动。此段代码的目的是查询并更新该订单的拼团剩余数量。
                     * 这对于展示订单的当前拼团状态（如还有多少名额剩余）至关重要。
                     */
                    if (page.getRecords().get(i).getOrderBuyId() != null) {

                        /* 准备查询拼团标记信息的参数 */
                        Map<String, Object> param = new HashMap<>();
                        param.put("id", page.getRecords().get(i).getOrderBuyId());

                        /* 调用服务查询拼团标记信息 */
                        ResponseResult orderPintuanMarkEntity = activityServiceClient.selectPiecingTogetherMarkInfo(param);

                        /* 如果查询结果不为空，则处理查询到的拼团信息 */
                        if (!ObjectUtils.isEmpty(orderPintuanMarkEntity.getData())) {
                            /* 将查询结果转换为Map，以便于处理 */
                            Map<String, Object> ptMap = ParseMapUtils.beanToMap(orderPintuanMarkEntity.getData());
                            /* 提取拼团剩余数量，并转换为整型 */
                            Integer numberesultemaining = Integer.valueOf(ptMap.get("surplusNum").toString());
                            /* 更新订单的剩余数量 */
                            page.getRecords().get(i).setNumberRemaining(numberesultemaining);
                        }
                    }

                    // 每家店铺总计价格
                    page.getRecords().get(i).setShopOrderTotalPrice(paymentPrice);
                    // 退款实际价格
                    page.getRecords().get(i).setRefundRealPrice(page.getRecords().get(i).getOrderGoodsAmount());
                    // 总付款价格
                    page.getRecords().get(i).setOrderTotalPrice(page.getRecords().get(i).getOrderPaymentAmount());
                }

                return ResponseResult.success(PageUtils.getPage(page));
            } catch (Exception e) {
                log.info("queryOrdersErrorResults:" + e.getMessage());
                return ResponseResult.fail(e.getMessage());
            }
        } else {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
    }

    @Override
    public ResponseResult queryIntegralOrderByEvaluation(Map<String, Object> map) {
        String token = (String) map.get("token");

        boolean isExist = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (isExist) {
            String userId = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USERID + token);
            // 已完成
            Integer offTheStocks = 6;
            String timeEnd = (String) map.get("timeEnd");
            String timeStart = (String) map.get("timeStart");
            String orderNo = (String) map.get("orderNo");

            Map<String, Object> mapOrder = new HashMap<>();
            mapOrder.put("userId", userId);
            mapOrder.put("offTheStocks", offTheStocks);
            mapOrder.put("timeStart", timeStart);
            mapOrder.put("timeEnd", timeEnd);
            mapOrder.put("orderNo", orderNo);
            // 查询待评价订单商品信息
            IPage<OrderListVO> page = this.baseMapper.queryIntegralWaitingEvaluationOrder(new Query<OrderListVO>().getPage(map), mapOrder);
            BigDecimal paymentPrice = new BigDecimal(0);
            for (int i = 0; i < page.getRecords().size(); i++) {
                Integer goodsNum = 0;
                String orderId = page.getRecords().get(i).getOrderId();
                // 查询待评价商品信息
                List<GoodsDetailsListVO> goodsDetailsList = orderGoodsMapper.queryGoodsList(orderId);
                page.getRecords().get(i).setGoodsDetailsList(goodsDetailsList);
                // 退款状态
                Integer orderRefundStatus = page.getRecords().get(i).getOrderRefundStatus();
                // 退货状态
                Integer orderReturnStatus = page.getRecords().get(i).getOrderReturnStatus();
                // 订单状态
                Integer normalStatus = page.getRecords().get(i).getOrderStatus();
                // 涉及退款退货订单状态
                Integer status = null;
                if (normalStatus.equals(CommonType.orderStatus.TO_BE_SHIPPED.getCode())) {
                    // 未发货状态
                    if (orderRefundStatus.equals(CommonType.orderRefundStatus.REFUNDING.getCode())) {
                        // 未发货退款中
                        status = 22;
                    } else if (orderRefundStatus.equals(CommonType.orderRefundStatus.REFUND_COMPLETED.getCode())) {
                        // 商家拒绝或审核通过，继续发货，但不可再次申请退款
                        status = 26;
                    } else if (orderRefundStatus.equals(CommonType.orderRefundStatus.NO_REFUND.getCode())) {
                        status = 3;
                    }
                } else if (normalStatus.equals(CommonType.orderStatus.CONFIRM_RECEIPT.getCode())) {
                    // 已完成状态
                    if (orderRefundStatus.equals(CommonType.orderRefundStatus.REFUNDING.getCode())) {
                        // 收货退款中
                        status = 23;
                    } else if (orderRefundStatus.equals(CommonType.orderRefundStatus.REFUND_COMPLETED.getCode())) {
                        status = 25;
                    } else if (orderRefundStatus.equals(CommonType.orderRefundStatus.NO_REFUND.getCode()) && orderReturnStatus.equals(CommonType.orderReturnStatus.NO_RETURNS.getCode())) {
                        status = 6;
                    } else if (orderReturnStatus.equals(CommonType.orderReturnStatus.RETURNING_GOODS.getCode())) {
                        // 收货退货中
                        status = 24;
                    } else if (orderReturnStatus.equals(CommonType.orderReturnStatus.RETURN_COMPLETED.getCode())) {
                        status = 25;
                    }
                    if (null != page.getRecords().get(i).getOrderLastReturnTime() && DateUtils.compareDate(page.getRecords().get(i).getOrderLastReturnTime(), DateUtils.getNowDateToString())) {
                        // 订单超过完成时间不可售后
                        status = 27;
                    }
                } else {
                    status = normalStatus;
                }
                // 存放最新状态
                page.getRecords().get(i).setOrderStatus(status);
                for (int j = 0; j < goodsDetailsList.size(); j++) {
                    // 计算共有几件商品
                    Integer goodsnum = goodsDetailsList.get(j).getOrderGoodsNum();
                    goodsNum += goodsnum;
                    page.getRecords().get(i).setGoodsNum(goodsNum);

                    // 计算商品总价
                    // 单个商品价格
                    BigDecimal orderGoodsAmount = goodsDetailsList.get(j).getOrderGoodsAmount();
                    paymentPrice = paymentPrice.add(orderGoodsAmount).setScale(2, BigDecimal.ROUND_UP);
                }
                // 退款实际价格
                page.getRecords().get(i).setRefundRealPrice(page.getRecords().get(i).getOrderGoodsAmount());
                // 总付款价格
                page.getRecords().get(i).setOrderTotalPrice(page.getRecords().get(i).getOrderPaymentAmount());
            }
            return ResponseResult.success(PageUtils.getPage(page));
        } else {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
    }

    /**
     * 根据传入的参数查询订单发票列表。
     *
     * @param param 查询参数，包含token和其他过滤条件。
     * @return 返回查询结果的ResponseResult对象。
     */
    @Override
    public ResponseResult queryOrderInvoiceList(Map<String, Object> param) {
        // 从参数中提取token
        String token = (String) param.get("token");
        // 根据token解析用户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
        // 如果用户信息为空，则返回非法token的错误信息
        if (ui == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
        } else {
            // 初始化用户ID列表
            List<String> userIdList = new ArrayList<>();
            // 如果参数中包含buyUserId，则将其加入列表，否则将当前用户ID加入列表
            if (param.get("buyUserId") != null) {
                String buyUserId = (String) param.get("buyUserId");
                userIdList.add(buyUserId);
            } else {
                userIdList.add(ui.getUserId());
//                // 根据当前用户ID查询业务信息
//                Map<String, Object> detail = this.baseMapper.queryBusiness(ui.getUserId(), null);
//                // 如果查询结果存在，且当前用户不是员工，且是商家，则进一步查询员工列表
//                if (detail != null && ui.getIsEmployee() != null && ui.getIsEmployee() != 1 && ui.getIsBusiness() == 1) {
//                    List<String> list = this.baseMapper.queryEmployeesList(detail.get("businessId").toString());
//                    // 如果当前用户是员工列表中的一员，则使用员工列表作为查询条件
//                    if (list.contains(ui.getUserId())) {
//                        userIdList = list;
//                    }
//                }
            }
            // 将用户ID列表加入查询参数
            param.put("userIdList", userIdList);
        }
        try {
            // 根据查询参数分页查询订单商品信息
            IPage<OrderBaseEntity> page = this.baseMapper.queryOrderGoodsInfo(new Query<OrderBaseEntity>().getPage(param),
                    param);
            // 遍历查询结果，丰富每个订单的信息
            for (OrderBaseEntity order : page.getRecords()) {
                // 为每个订单查询商品详情
                param.put("orderId", order.getOrderId());
                List<OrderGoodsEntity> goodsInfo = orderGoodsMapper.selectList(new LambdaQueryWrapper<OrderGoodsEntity>()
                        .eq(OrderGoodsEntity::getOrderId, order.getOrderId()));
                order.setGoodsList(goodsInfo);
                // 根据订单查询发票信息
                param.put("invoiceId", order.getInvoiceId());
                OrderInvoiceInfoEntity invoice = orderInvoiceInfoMapper.selectById(order.getInvoiceId());
                if (invoice != null) {
                    order.setInvoiceType(invoice.getInvoiceType());
                }
                // 根据订单所属店铺查询发票开关和审核状态
                Map<String, Object> tmp = this.baseMapper.getInvoiceSwitchInfo(order.getShopId());
                if (null != tmp) {
                    order.setInvoiceAuditStatus((Integer) tmp.get("invoiceAuditStatus"));
                    // 根据发票审核状态和开关状态设置订单的发票开关状态
                    Integer invoiceSwitchStatus = (Integer) tmp.get("invoiceSwitchStatus");
                    Integer invoiceAuditStatus = (Integer) tmp.get("invoiceAuditStatus");
                    if (1 == invoiceAuditStatus && 1 == invoiceSwitchStatus) {
                        order.setInvoiceSwitchStatus(1);
                    } else if (0 == invoiceAuditStatus && 0 == invoiceSwitchStatus) {
                        order.setInvoiceSwitchStatus(1);
                    } else if (2 == invoiceAuditStatus && 0 == invoiceSwitchStatus) {
                        order.setInvoiceSwitchStatus(1);
                    } else {
                        order.setInvoiceSwitchStatus(0);
                    }
                } else {
                    // 如果查询结果为空，则设置默认的发票状态
                    order.setInvoiceSwitchStatus(0);
                    order.setInvoiceAuditStatus(3);
                }
                // 根据订单完成时间判断订单是否超期
                Date orderFinishTime = order.getOrderFinishedTime();
                order.setIsOutDate(0);
                if (null != orderFinishTime) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(orderFinishTime);
                    cal.add(Calendar.DATE, 90);
                    Date countDate = cal.getTime();
                    if (countDate.compareTo(new Date()) < 0) {
                        order.setIsOutDate(1);
                    }
                }
            }
            // 返回成功的响应结果，包含分页处理后的订单列表
            return ResponseResult.success(PageUtils.getPage(page));
        } catch (Exception e) {
            // 如果查询过程中发生异常，则返回失败的响应结果，包含异常信息
            return ResponseResult.fail(e.getMessage());
        }
    }

    /**
     * 根据销售参数进行销售交易查询。
     *
     * @param sellingPara 销售输入参数DTO，包含查询条件和分页信息。
     * @return 返回查询结果的响应对象。
     */
    @Override
    public ResponseResult orderSellingTransaction(SellingInputDTO sellingPara) {
        // 根据token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(sellingPara.getToken());
        // 如果用户信息为空，返回令牌无效的错误响应
        if (userInfo == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }

        // 尝试将输入参数转换为Map，用于后续的查询条件构造
        // 分页
        // 将对象转换为map
        Map data = null;
        try {
            data = ParseMapUtils.beanToMap(sellingPara);
        } catch (IllegalAccessException e) {
        }

        // 根据转换后的Map和销售参数查询新的销售订单信息
        // 分页查询数据
        IPage<SellingOrderVO> page = this.baseMapper.selectSellingOrderNew(new Query<SellingOrderVO>().getPage(data), sellingPara);
        // 查询销售订单的总金额
        BigDecimal orderMoney = this.baseMapper.querySellingOrderMoneynNew(sellingPara);
        List<SellingOrderVO> sellingList = page.getRecords();

        // 如果查询结果不为空，对查询结果进行处理
        if (CollectionUtils.isNotEmpty(sellingList)) {
            // 用于标记订单的退款状态
            Integer returnFlag = 0;

            // 获取脱敏开关的配置值
            String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.DESENSITIZATION_SWITCH.getDesc());
            for (SellingOrderVO sellingOrderPara : sellingList) {
                // 根据脱敏开关的配置，对订单信息进行脱敏处理
                if (StrUtil.isNotEmpty(value) && value.equals(CommonType.commonStatus.YES.getCode().toString())) {
                    BeanUtils.copyProperties(DesensitizationUtil.Desensitization(sellingOrderPara, SellingOrderVO.class), sellingOrderPara);
                }

                // 根据订单的属性，设置订单的类型
                Integer virtualGoods = sellingOrderPara.getVirtualGoods();
                Integer orderIsShopTake = sellingOrderPara.getOrderIsShopTake();
                if (null != orderIsShopTake && 1 == orderIsShopTake) {
                    sellingOrderPara.setOrderType(3);
                } else {
                    if (null != virtualGoods && 0 == virtualGoods) {
                        sellingOrderPara.setOrderType(2);
                        sellingOrderPara.setPastDue(sellingOrderPara.getPastDue());
                        sellingOrderPara.setIsCancel(sellingOrderPara.getIsCancel());
                    } else {
                        sellingOrderPara.setOrderType(1);
                    }
                }

                // 根据订单的退货和退款状态，计算订单的退款标志
                Integer returnStatus = Integer.valueOf(sellingOrderPara.getOrderReturnStatus());
                Integer refundStatus = Integer.valueOf(sellingOrderPara.getOrderRefundStatus());
                if (2 == returnStatus || 2 == refundStatus) {
                    BigDecimal returnAmount = sellingOrderPara.getOrderRefundAmount();
                    BigDecimal orderAmount = sellingOrderPara.getOrderPaymentAmount();
                    if (orderAmount.compareTo(returnAmount) > 0) {
                        returnFlag = 2;
                    } else if ((orderAmount.compareTo(returnAmount) == 0)) {
                        returnFlag = 1;
                    }
                }
                sellingOrderPara.setReturnFlag(returnFlag);

                // 第三方支付金额赋值 2-支付宝支付，3-微信支付
                if ("2".equals(sellingOrderPara.getPayCode()) || "3".equals(sellingOrderPara.getPayCode())) {
                    sellingOrderPara.setTradeThirdPartyAmount(sellingOrderPara.getOrderPaymentAmount());
                } else if ("6".equals(sellingOrderPara.getPayCode())) {
                    sellingOrderPara.setTradePaymentRechargeCard(sellingOrderPara.getOrderPaymentAmount());
                }

                // 标记订单是否存在退款
                if (refundStatus != 0 || returnFlag != 0) {
                    sellingOrderPara.setIsExistReturn("1");
                } else {
                    sellingOrderPara.setIsExistReturn("0");
                }
            }
        }

        // 构造返回结果，包含订单总金额和分页后的订单数据
        Map<String, Object> Map = new HashMap<>();
        Map.put("orderMoney", orderMoney);
        Map.put("orderData", PageUtils.getPage(page));

        // 返回处理后的查询结果
        return ResponseResult.success(Map);
    }

    @Override
    public ResponseResult orderSellingTransactionExcel(SellingInputDTO sellingPara) {
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(sellingPara.getToken());
        if (null == userInfo) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }

        String[] headers = {"订单编号", "买家账号", "订单来源", "下单时间", "订单金额(元)", "支付金额(元)", "订单状态", "收件人姓名", "联系方式",
                "收货地址", "是否存在退货退款", "支付单号", "支付方式", "支付方式名称", "充值卡余额支付", "第三方支付金额", "支付时间", "发货时间",
                "发货物流单号", "退款金额", "订单完成时间", "拼团状态", "是否评价", "店铺ID", "店铺名称", "买家ID"};
        List<Map<String, String>> list = Lists.newArrayList();
        List<SellingOrderVO> records = this.baseMapper.orderSellingDaoTransactionExp(sellingPara);
        String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.DESENSITIZATION_SWITCH.getDesc());
        for (SellingOrderVO res : records) {
            if (StrUtil.isNotEmpty(value) && value.equals(CommonType.commonStatus.YES.getCode().toString())) {
                BeanUtils.copyProperties(DesensitizationUtil.Desensitization(res, SellingOrderVO.class), res);
            }
            HashMap<String, String> map = com.google.common.collect.Maps.newHashMap();
            if (res.getOrderStatus() != null) {

                switch (res.getOrderStatus()) {
                    case "1":
                        res.setOrderStatus("待付款 ");
                        break;
                    case "2":
                        res.setOrderStatus("已付款");
                        break;
                    case "3":
                        res.setOrderStatus("待发货  ");
                        break;
                    case "4":
                        res.setOrderStatus("已发货 ");
                        break;
                    case "5":
                        res.setOrderStatus("已签收 ");
                        break;
                    case "6":
                        res.setOrderStatus("确认收货");
                        break;
                    case "7":
                        res.setOrderStatus("取消 ");
                        break;
                    case "8":
                        res.setOrderStatus("退款中");
                        break;
                    case "9":
                        res.setOrderStatus("已退款 ");
                        break;
                    case "11":
                        res.setOrderStatus("待自提");
                        break;
                    case "12":
                        res.setOrderStatus("已过期");
                        break;
                }
                if (res.getOrderReturnStatus().equals("2") && res.getOrderStatus().equals("已退款 ")) {
                    res.setOrderStatus("已退货 ");
                }
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
                    case "4":
                        res.setPayCode("通联支付");
                        break;
                    case "5":
                        res.setPayCode("健康账户余额支付 ");
                        break;
                    case "6":
                        res.setPayCode("充值卡余额支付 ");
                        break;
                    case "7":
                        res.setPayCode("充值卡+支付宝支付 ");
                        break;
                    case "8":
                        res.setPayCode("充值卡+微信 ");
                        break;
                }
            }

            if (res.getOrderFrom() != null) {

                switch (res.getOrderFrom()) {
                    //1PC 2wap微信浏览器 3安卓 5ios 6微信小程序 7wap浏览器
                    case "1":
                        res.setOrderFrom("pc");
                        break;
                    case "2":
                        res.setOrderFrom("wap微信浏览器");
                        break;
                    case "3":
                        res.setOrderFrom("安卓");
                        break;
                    case "5":
                        res.setOrderFrom("ios");
                        break;
                    case "6":
                        res.setOrderFrom("微信小程序");
                        break;
                    case "7":
                        res.setOrderFrom("wap浏览器");
                        break;
                }
            }

            if (res.getOrderRefundStatus() != null) {

                switch (res.getOrderRefundStatus()) {
                    // 0是无退款,1是退款中,2是退款完成',
                    case "0":
                        res.setOrderRefundStatus("无退款");
                        break;
                    case "1":
                        res.setOrderRefundStatus("退款中 ");
                        res.setOrderStatus("退款中 ");
                        break;
                    case "2":
                        res.setOrderRefundStatus("退款完成");
                        res.setOrderStatus("退款完成 ");
                        break;

                }
            }

            if (res.getOrderReturnStatus() != null) {

                switch (res.getOrderReturnStatus()) {
                    // 0是无退款,1是退款中,2是退款完成',
                    case "0":
                        res.setOrderReturnStatus("无退货");
                        break;
                    case "1":
                        res.setOrderReturnStatus("退货中 ");
                        res.setOrderStatus("退货中 ");
                        break;
                    case "2":
                        res.setOrderReturnStatus("退货完成");
                        res.setOrderStatus("退货完成 ");
                        break;
                }
            }
            if (res.getIsExistReturn() != null) {
                switch (res.getIsExistReturn()) {
                    case "0":
                        res.setIsExistReturn("否");
                        break;
                    case "1":
                        res.setIsExistReturn("是 ");
                        break;
                }
            }
            if (res.getOrderBuyerEvaluationStatus() != null) {
                switch (res.getOrderBuyerEvaluationStatus()) {
                    case "0":
                        res.setOrderBuyerEvaluationStatus("未评价");
                        break;
                    case "1":
                        res.setOrderBuyerEvaluationStatus("已评价 ");
                        break;
                }
            }
            if (!"".equals(res.getGroupbuyStatus()) && res.getGroupbuyStatus() != null) {
                switch (res.getGroupbuyStatus()) {
                    case "0":
                        res.setGroupbuyStatus("非拼团");
                        break;
                    case "1":
                        res.setGroupbuyStatus("拼团中");
                        break;
                    case "2":
                        res.setGroupbuyStatus("已成功");
                        break;
                    case "3":
                        res.setGroupbuyStatus("未成功");
                        break;
                }

            }
            map.put("订单编号", res.getOrderId());
            map.put("买家账号", res.getBuyerUserName());
            if ("".equals(res.getOrderFrom()) || res.getOrderFrom() == null) {
                map.put("订单来源", "无");
            } else {
                map.put("订单来源", res.getOrderFrom());
            }
            map.put("下单时间", res.getOrderCreateTime());
            map.put("订单金额(元)", res.getOrderAmount() + "");
            map.put("支付金额(元)", res.getOrderPaymentAmount() + "");
            if ("".equals(res.getOrderStatus()) || res.getOrderStatus() == null) {
                map.put("订单状态", "无");
            } else {
                map.put("订单状态", res.getOrderStatus());
            }
            if ("".equals(res.getOrderReceiverName()) || res.getOrderReceiverName() == null) {
                map.put("收件人姓名", "无");
            } else {
                map.put("收件人姓名", res.getOrderReceiverName());
            }
            map.put("联系方式", res.getOrderReceiverContact());
            map.put("收货地址", res.getOrderReceiverAddress());
            map.put("是否存在退货退款", res.getIsExistReturn());
            map.put("支付单号", res.getPaymentNumber());
            if ("".equals(res.getPaymentName()) || res.getPaymentName() == null) {
                map.put("支付方式", "无");
            } else {
                map.put("支付方式", res.getPaymentName());
            }
            if ("".equals(res.getPayCode()) || res.getPayCode() == null) {
                map.put("支付方式名称", "无");

            } else {
                map.put("支付方式名称", res.getPayCode());
            }
            map.put("充值卡余额支付", res.getTradePaymentRechargeCard() + "");
            map.put("第三方支付金额", res.getTradeThirdPartyAmount() + "");
            if ("".equals(res.getPaymentTime()) || res.getPaymentTime() == null) {
                map.put("支付时间", "无");

            } else {
                map.put("支付时间", res.getPaymentTime());
            }
            if ("".equals(res.getOrderShippingTime()) || res.getOrderShippingTime() == null) {
                map.put("发货时间", "无");
            } else {
                map.put("发货时间", res.getOrderShippingTime());
            }
            if ("".equals(res.getOrderShippingCode()) || res.getOrderShippingCode() == null) {
                map.put("发货物流单号", "无");

            } else {
                map.put("发货物流单号", res.getOrderShippingCode());
            }

            if (res.getOrderRefundAmount() == null) {
                res.setOrderRefundAmount(new BigDecimal(0));
            }
            map.put("退款金额", res.getOrderRefundAmount() + "");
            if ("".equals(res.getOrderFinishedTime()) || res.getOrderFinishedTime() == null) {
                map.put("订单完成时间", "无");

            } else {
                map.put("订单完成时间", res.getOrderFinishedTime());
            }
            map.put("拼团状态", res.getGroupbuyStatus());
            map.put("是否评价", res.getOrderBuyerEvaluationStatus());
            map.put("店铺ID", res.getShopId() + "");
            map.put("店铺名称", res.getShopName());
            map.put("买家ID", res.getBuyerUserId() + "");
            list.add(map);
        }

        // excel文件名
        Map<String, List<Map<String, String>>> map = Maps
                .newHashMap();
        map.put("订单明细", list);
        String url = ExcelsUtil.createExcel(headers, map, new int[]{0});

        return ResponseResult.success(url);
    }

    /**
     * 根据订单详情进行平台后台操作。
     *
     * @param buyingPara 订单相关的参数集合，包含token和orderId等信息。
     * @return 返回操作结果，包含订单详情和操作状态等信息。
     */
    @Override
    public ResponseResult PlatFormBackGroundByOrderDetails(Map<String, Object> buyingPara) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(String.valueOf(buyingPara.get("token")));
        // 校验token是否有效
        if (userInfo == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 获取订单ID
        String orderId = (String) buyingPara.get("orderId");
        // 校验订单ID是否为空
        if (StringUtils.isEmpty(orderId)) {
            return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER);
        } else {
            // 根据订单ID查询订单详情和商品信息
            OrderbaseDetailsparaVO param = this.baseMapper.queryorderDetailparam(orderId);
            List<GoodsAllByidVO> res = this.baseMapper.getGoodsAllByIdparam(orderId);
            // 初始化返回标志，用于判断订单是否可退款
            Integer returnFlag = 0;
            // 处理虚拟商品和实际商品的订单类型区分
            Integer virtualGoods = param.getVirtualGoods();
            Integer orderIsShopTake = param.getOrderIsShopTake();
            if (null != orderIsShopTake && 1 == orderIsShopTake) {
                param.setOrderType(3);
            } else {
                if (null != virtualGoods && 0 == virtualGoods) {
                    param.setOrderType(2);
                    param.setPastDue(param.getPastDue());
                    param.setIsCancel(param.getIsCancel());
                } else {
                    param.setOrderType(1);
                }
            }
            // 更新订单的退货和退款状态
            int returnStatus = param.getOrderReturnStatus();
            int refundStatus = param.getOrderRefundStatus();
            param.setReturnStatus(returnStatus);
            param.setRefundStatus(refundStatus);
            // 判断订单是否可退款并设置返回标志
            if (2 == returnStatus || 2 == refundStatus) {
                BigDecimal returnAmount = param.getOrderRefundAmount();
                BigDecimal orderAmount = BigDecimal.valueOf(param.getOrderPaymentAmount());
                if (orderAmount.compareTo(returnAmount) > 0) {
                    returnFlag = 2;
                } else if ((orderAmount.compareTo(returnAmount) == 0)) {
                    returnFlag = 1;
                }
            }
            // 初始化商品总数、平台优惠、商家优惠和预计积分等信息
            // 商品总件数
            Integer goodsNum = 0;
            // 平台总优惠
            BigDecimal totalPlatformDiscount = new BigDecimal(0);
            // 商家总优惠
            BigDecimal totalMerchantDiscount = new BigDecimal(0);
            // 预计获得的积分
            Integer pointsObtained = 0;
            // 查询积分规则
            ResponseResult resultResp = baseServiceClient.queryIntegralRules();
            Map<String, Integer> integralRules = (Map<String, Integer>) resultResp.getData();
            // 设置返回标志
            param.setReturnFlag(returnFlag);
            // 处理商品的退款状态和计算总优惠等信息
            for (GoodsAllByidVO pa : res) {
                if (pa.getGoodsRefundStatus() == 0) {
                    pa.setIsNoretrun(0);
                } else {
                    pa.setIsNoretrun(1);
                }
                if (CommonType.orderReturnStatus.RETURN_COMPLETED.getCode().equals(pa.getGoodsReturnStatus() )) {
                    pa.setGoodsRefundStatus(CommonType.orderRefundStatus.REFUND_COMPLETED.getCode());
                }
                goodsNum = goodsNum + pa.getOrderGoodsNum();
                // 计算平台和商家的总优惠
                if (null != pa.getVoucherPrice()) {
                    totalMerchantDiscount = totalPlatformDiscount.add(pa.getVoucherPrice());
                }
                if (null != pa.getOrderRptPrice()) {
                    totalPlatformDiscount = totalMerchantDiscount.add(pa.getOrderRptPrice());
                }
                if (null != pa.getGoodsPlusDiscount()) {
                    totalPlatformDiscount = totalMerchantDiscount.add(pa.getGoodsPlusDiscount());
                }
                if (null != pa.getGoodsMemberDiscount()) {
                    totalPlatformDiscount = totalMerchantDiscount.add(pa.getGoodsMemberDiscount());
                }
            }
            // 通过订单ID查询实际获得的积分
            pointsObtained = userServiceClient.queryPointsByOrderId(param.getOrderId());
            // 设置商品总数、总优惠等信息
            param.setGoodsNum(goodsNum);
            if (param.getCombinationType() != null && param.getCombinationType() == 1) {
                param.setTotalPlatformDiscount(param.getOrderDiscountFee());
                param.setTotalMerchantDiscount(BigDecimal.ZERO);
            } else {
                param.setTotalPlatformDiscount(totalPlatformDiscount);
                param.setTotalMerchantDiscount(totalMerchantDiscount);
            }
            // 判断订单状态，根据积分规则计算预计获得的积分
            List<Integer> statusList = new ArrayList<>(Arrays.asList(1, 3, 4, 7));
            if (statusList.contains(param.getOrderStatus())) {
                if (null != integralRules.get("ruleConsume")) {
                    double pointsObtained2 = param.getOrderPaymentAmount() * integralRules.get("ruleConsume");
                    BigDecimal bd = new BigDecimal(pointsObtained2).setScale(0, RoundingMode.HALF_UP);
                    pointsObtained = Integer.parseInt(bd.toString());
                    if (param.getPlusOrder() != null && param.getPlusOrder() == 1) {
                        pointsObtained = userServiceClient.queryPointsByOrderId(param.getOrderId());
                    }
                }
            }
            // 设置预计获得的积分
            param.setPointsObtained(pointsObtained);
            // 设置商品信息
            param.setGoodsAllByidParam(res);
            // 查询订单的支付凭证信息
            // 获取支付凭证信息
            OdbPaymentVocherVO odbPaymentVocher = this.baseMapper.queryPaymentVocher(orderId);
            if (null != odbPaymentVocher) {
                param.setOdbPaymentVocherPO(odbPaymentVocher);
            }
            // 查询发票信息
            // 根据参数查询卖家发票详情
            SellerOrderInvoiceDetailVO sellerOrderInvoiceDetailEntity = orderInvoiceInfoMapper.querySellerInvoiceDetail(orderId);
            param.setSellerOrderInvoiceDetailVO(sellerOrderInvoiceDetailEntity);

            // 返回处理后的订单详情
            return ResponseResult.success(param);
        }
    }

    /**
     * 根据销售参数查询销售后台数据。
     * 该方法主要用于平台后台根据销售参数查询相关的销售订单信息，并进行数据的脱敏处理。
     * 同时，计算订单的总金额，并对查询结果进行分页处理，最终返回查询结果和订单总金额。
     *
     * @param sellingPara 销售参数输入对象，包含查询所需的条件信息。
     * @return 返回查询结果的响应对象，其中包含订单总金额和分页处理后的订单数据。
     */
    @Override
    public ResponseResult PlatFormBackGroundBySelling(SellingInputDTO sellingPara) {
        // 通过token获取用户信息，用于权限验证
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(sellingPara.getToken());
        // 如果用户信息为空，表示token无效，返回失败结果
        if (userInfo == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        Map data = null;
        try {
            // 将销售参数对象转换为Map，用于后续的查询条件构建
            data = ParseMapUtils.beanToMap(sellingPara);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        // 根据转换后的Map和销售参数查询销售订单，返回分页结果
        IPage<SellingOrderVO> page = this.baseMapper.selectSellingOrder(new Query<SellingOrderVO>().getPage(data), sellingPara);
        // 获取是否开启脱敏处理的配置值
        String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.DESENSITIZATION_SWITCH.getDesc());
        // 如果开启脱敏处理，则对查询结果进行脱敏处理
        if (StrUtil.isNotEmpty(value) && value.equals(CommonType.commonStatus.YES.getCode().toString())) {
            if (!page.getRecords().isEmpty()) {
                for (SellingOrderVO sellingOrderVO : page.getRecords()) {
                    BeanUtils.copyProperties(DesensitizationUtil.Desensitization(sellingOrderVO, SellingOrderVO.class), sellingOrderVO);
                }
            }
        }
        // 计算销售订单的总金额
        BigDecimal orderMoney = this.baseMapper.querySellingOrderMoney(sellingPara);
        // 构建返回结果的Map，包含订单总金额和分页处理后的订单数据
        Map<String, Object> map = new HashMap<>();
        map.put("orderMoney", orderMoney);
        map.put("orderData", PageUtils.getPage(page));
        // 返回查询结果的响应对象
        return ResponseResult.success(map);

    }

    @Override
    public ResponseResult PlatFormBackGroundBySellingExp(SellingInputDTO sellingPara) {

        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(sellingPara.getToken());
        if (userInfo == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        Page<SellingOrderVO> page = new Page<>(0, -1);
        String[] headers = {"订单编号", "订单来源", "审核状态", "买家账号", "下单时间", "订单金额(元)", "支付金额(元)",
                "收件人姓名", "联系方式", "收货地址", "支付单号", "店铺名称"};
        List<Map<String, String>> list = Lists.newArrayList();
        IPage<SellingOrderVO> result = this.baseMapper.selectSellingOrder(page, sellingPara);
        String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.DESENSITIZATION_SWITCH.getDesc());
        for (SellingOrderVO res : result.getRecords()) {
            if (StrUtil.isNotEmpty(value) && value.equals(CommonType.commonStatus.YES.getCode().toString())) {
                BeanUtils.copyProperties(DesensitizationUtil.Desensitization(res, SellingOrderVO.class), res);
            }
            HashMap<String, String> map = com.google.common.collect.Maps.newHashMap();
            if (res.getOrderFrom() != null) {
                switch (res.getOrderFrom()) {
                    //1PC 2wap微信浏览器 3安卓 5ios 6微信小程序 7wap浏览器
                    case "1":
                        res.setOrderFrom("pc");
                        break;
                    case "2":
                        res.setOrderFrom("wap微信浏览器");
                        break;
                    case "3":
                        res.setOrderFrom("安卓");
                        break;
                    case "5":
                        res.setOrderFrom("ios");
                        break;
                    case "6":
                        res.setOrderFrom("微信小程序");
                        break;
                    case "7":
                        res.setOrderFrom("wap浏览器");
                        break;
                }
            }
            String reviewStr = null;
            if (res.getReviewStatus() != null) {
                switch (res.getReviewStatus()) {
                    case 1:
                        reviewStr = "待审核";
                        break;
                    case 2:
                        reviewStr = "审核已通过";
                        break;
                    case 3:
                        reviewStr = "审核已驳回";
                        break;
                }
            } else {
                reviewStr = "待上传支付凭证";
            }
            map.put("订单编号", res.getOrderId());
            if ("".equals(res.getOrderFrom()) || res.getOrderFrom() == null) {
                map.put("订单来源", "无");
            } else {
                map.put("订单来源", res.getOrderFrom());
            }
            map.put("审核状态", reviewStr);
            map.put("买家账号", res.getBuyerUserName());
            if (res.getOrderRefundAmount() == null) {
                res.setOrderRefundAmount(new BigDecimal(0));
            }
            map.put("下单时间", res.getOrderCreateTime());
            map.put("订单金额(元)", res.getOrderAmount() + "");
            map.put("支付金额(元)", res.getOrderPaymentAmount() + "");
            if ("".equals(res.getOrderReceiverName()) || res.getOrderReceiverName() == null) {
                map.put("收件人姓名", "无");
            } else {
                map.put("收件人姓名", res.getOrderReceiverName());
            }
            map.put("联系方式", res.getOrderReceiverContact());
            map.put("收货地址", res.getOrderReceiverAddress().trim());
            map.put("支付单号", res.getPaymentNumber());
            map.put("店铺名称", res.getShopName());

            list.add(map);
        }

        // excel文件名
        Map<String, List<Map<String, String>>> map = Maps.newHashMap();
        map.put("合并数据", list);
        String url = ExcelsUtil.createExcel(headers, map, new int[]{0});
        return ResponseResult.success(url);
    }

    /**
     * 根据订单ID查询订单完成时间，并判断该订单是否在规定的时间范围内可以申请售后服务。
     *
     * @param paraMap 包含订单ID等参数的映射。
     * @return 如果订单存在且完成时间在规定范围内，则返回可以申请售后服务的成功消息；否则返回失败消息。
     * @throws ParseException 如果解析日期出错。
     */
    @Override
    public ResponseResult overTimeReturnOrder(Map<String, String> paraMap) throws ParseException {
        // 根据订单ID查询订单状态为已收货且订单完成时间不为空的订单的完成时间
        Date orderFinishTime = this.baseMapper.selectById(new LambdaQueryWrapper<OrderBaseEntity>()
                .eq(OrderBaseEntity::getOrderStatus, CommonType.orderStatus.CONFIRM_RECEIPT.getCode())
                .isNotNull(OrderBaseEntity::getOrderFinishedTime)
                .eq(OrderBaseEntity::getOrderId, paraMap.get("orderId"))).getOrderFinishedTime();

        // 如果订单完成时间为空，则返回失败消息
        if (orderFinishTime == null) {
            return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION);
        }

        // 获取当前日期
        Date date2 = DateUtils.getNowDateToDf();
        // 判断订单完成时间是否在最近一周内
        boolean latestWeek = DateUtils.isLatestWeek(orderFinishTime, date2);

        // 如果订单完成时间在最近一周内，则返回可以申请售后服务的成功消息；否则返回失败消息
        if (latestWeek) {
            return ResponseResult.success(BizCodeEnume.NOT_EXCEEDING_THE_TIME_AFTER_SALE);
        } else {
            return ResponseResult.fail(BizCodeEnume.ORDER_AFTER_TIME_EXCEED);
        }
    }

    /**
     * 根据订单信息查询物流信息。
     *
     * @param map 包含查询所需信息的Map，包括token（用户令牌）、no（运单号）、orderId（订单号）和shipmentId（物流配置ID）。
     * @return 返回查询物流信息的结果，如果查询失败，会包含失败原因。
     */
    @Override
    public ResponseResult queryLogistics2(Map<Object, Object> map) {
        // 从请求参数中获取用户令牌
        String token = map.get("token").toString();
        // 根据令牌解析用户信息
        UserInfoVO userInfoVO = ParseTokenUtils.getUserInfoByToken(token);
        // 如果用户信息无效，则返回令牌无效的错误信息
        if (userInfoVO == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 初始化物流信息Map和返回结果List
        // 物流信息
        Map<String, String> shippingMap = new HashMap<>();
        List<Map<String, String>> returnStrs = new ArrayList<>();
        // 物流信息
        String returnStr = null;
        try {
            // 从请求参数中获取运单号、订单号和物流配置ID
            String shippingCode = map.get("no").toString();
            String orderId = map.get("orderId").toString();
            Integer shipmentId = Integer.valueOf(map.get("shipmentId").toString());
            String logisticCode = map.get("shippingCode").toString();
            OrderShipmentEntity orderShipmentEntity = orderShipmentMapper.selectOne(new LambdaQueryWrapper<OrderShipmentEntity>()
                    .eq(OrderShipmentEntity::getOrderId, orderId)
                    .eq(OrderShipmentEntity::getShipmentId, shipmentId));
            // 根据订单号查询物流消息
            List<LogisticsMessageVO> shippingList = this.baseMapper.queryLogisticsMessage(orderId);
            // 日志记录查询开始
            log.info("---------开始查询运单号为{}的物流信息", shippingCode);
            // 根据物流配置ID获取物流商代码等信息
            Map<String, String> resultMap = getShipperCode2(shipmentId);
            // 设置快递鸟API的配置信息
            //获取快递鸟的配置信息
            KdniaoTrackQueryAPI.AppKey = resultMap.get("AppKey");
            KdniaoTrackQueryAPI.EBusinessID = resultMap.get("EBussinessID");
            KdniaoTrackQueryAPI.instructCode = String.valueOf(resultMap.get("instructCode"));
            // 创建一个HashMap用于存储请求数据
            Map<String, Object> requestDataMap = new HashMap<>();
            //获取快递的简称
            String shipperCode = resultMap.get("shipperCode");
            // 将物流公司的编码放入请求数据中
            requestDataMap.put("ShipperCode", shipperCode);
            // 将物流单号放入请求数据中
            requestDataMap.put("LogisticCode", logisticCode);
            // 如果是顺丰快递，则需要提供客户名称,一般用手机号后四位表示
            if (logisticCode.startsWith("SF")) {
                String CustomerName = resultMap.get("CustomerName");
                requestDataMap.put("CustomerName", CustomerName);
            }
            // 收货详细地址
            requestDataMap.put("Receiver", resultMap.get("receiver"));
            // 发货地址
            requestDataMap.put("Sender", orderShipmentEntity.getSenderAddress());
            // 调用快递鸟的接口获取物流信息（地图版）
            returnStr = KdniaoTrackQueryAPI.getOrderTracesAndMap(requestDataMap);
            // 如果查询结果不为空，则处理并添加到返回结果中
            if (StringUtils.isNotEmpty(returnStr)) {
                // 构建物流信息Map，并添加到返回结果List中
                shippingMap.put("shippingInfo", returnStr);
                shippingMap.put("expressName", resultMap.get("expressName"));
                shippingMap.put("shippingCode", shippingCode);
                shippingMap.put("orderShippingMessage", shippingList.get(0).getOrderShippingMessage());
                shippingMap.put("orderReceiverName", shippingList.get(0).getOrderReceiverName());
                shippingMap.put("orderReceiverAddress", shippingList.get(0).getOrderReceiverAddress());
                shippingMap.put("orderReceiverContact", shippingList.get(0).getOrderReceiverContact());
                returnStrs.add(shippingMap);
            }
            // 日志记录查询结束及查询结果
            log.info("---------查询运单号为{}的物流信息结束,物流信息：{}", shippingCode, returnStr);
        } catch (Exception e) {
            // 如果查询过程中发生异常，则返回包含异常信息的错误结果
            return ResponseResult.fail(e.getMessage());
        }
        // 返回查询成功的物流信息结果
        return ResponseResult.success(returnStrs);
    }

    /**
     * 根据订单号查询支付凭证图片。
     *
     * @param param 包含订单号的请求参数对象。
     * @return 如果查询成功，返回包含支付凭证信息的响应结果；如果查询失败或未找到支付凭证，返回失败的响应结果。
     */
    @Override
    public ResponseResult querkVocherImages(QukVocherImagesDTO param) {
        // 根据订单号查询支付凭证信息
        OdbPaymentVocherVO PaymentVocherPo = this.baseMapper.queryPaymentVocher(param.getOrderId());

        // 如果支付凭证信息存在，则返回成功的响应结果，否则返回失败的响应结果
        if (null != PaymentVocherPo) {
            return ResponseResult.success(PaymentVocherPo);
        } else {
            return ResponseResult.fail();
        }
    }

    /**
     * 根据传入的优惠券图片信息检查支付凭证的有效性，并根据检查结果更新支付凭证的状态。
     *
     * @param param 包含优惠券图片信息和订单ID的DTO对象。
     * @return 如果更新成功，返回成功的响应结果；如果更新失败或未更新，返回失败的响应结果。
     */
    @Override
    public ResponseResult checkPaymentVocher(QukVocherImagesDTO param) {
        // 初始化一个Map用于存储待检查的支付凭证信息
        HashMap<Object, Object> map = new HashMap<>();
        // 初始化更新计数器
        Integer integer = 0;
        // 校验订单ID是否为数字，如果是，则根据订单ID查询对应的支付编号。
        String paymentNumber = param.getOrderId();
        if (MathUtil.isNumeric(paymentNumber)) {
            OrderBaseEntity entity = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderBaseEntity>()
                    .eq(OrderBaseEntity::getOrderId, paymentNumber));
            paymentNumber = entity.getPaymentNumber();
        }
        // 根据支付编号查询所有相关订单。
        List<OrderBaseEntity> list = this.baseMapper.selectList(new LambdaQueryWrapper<OrderBaseEntity>()
                .eq(OrderBaseEntity::getPaymentNumber, paymentNumber));
        // 如果优惠券审查状态为2，表示需要检查支付凭证的有效性
        if (param.getReviewStatus() == 2) {
            // 对于审核通过的订单，逐个更新订单状态并检查付款凭证的有效性。
            for (OrderBaseEntity orderBaseEntity : list) {
                if (orderBaseEntity.getOrderStatus() == 7) {
                    return ResponseResult.fail(BizCodeEnume.ORDER_STATUS_CHANGE);
                }
                param.setOrderId(orderBaseEntity.getOrderId());
                map.put("orderIdList", param.getOrderId());
                ResponseResult results = payMentServiceClient.checkPaymentVocher(map);
                // 如果付款凭证有效，则更新订单的付款凭证信息。
                if (results.getCode() == 1) {
                    integer = this.baseMapper.updatePaymentVocher(param);
                    if (orderBaseEntity.getOrderType() == 2) {
                        SupplierSyncGoodsDTO dto = new SupplierSyncGoodsDTO();
                        List<SynchronousGoodsDTO> goodsList = baseMapper.queryGoodsByOrderId(param.getOrderId());
                        dto.setGoodsList(goodsList);
                        dto.setToken(param.getToken());
                        //同步供应商商品
                        supplierClient.synchronousGoods(dto);
                    }
                }
                // 更新分销小店销量
                merchantServiceClient.updateVkShopSaleNum(orderBaseEntity.getOrderId());
                // 设置微客
                userServiceClient.setVKInfo(orderBaseEntity.getOrderId());
            }
        } else {
            for (OrderBaseEntity orderBaseEntity : list) {
                if (orderBaseEntity.getOrderStatus() == 7) {
                    return ResponseResult.fail(BizCodeEnume.ORDER_STATUS_CHANGE);
                }
                param.setOrderId(orderBaseEntity.getOrderId());
                integer = this.baseMapper.updatePaymentVocher(param);
            }
        }

        // 如果更新计数器大于0，表示更新成功，返回成功的响应结果；否则，返回失败的响应结果
        if (integer > 0) {
            return ResponseResult.success();
        }
        return ResponseResult.fail();
    }

    /**
     * 根据订单ID判断订单是否为虚拟订单。
     *
     * @param orderIdIsNum 包含订单ID和判断标志的映射表，其中"orderId"表示订单ID，"isNum"表示判断标志（0表示数字订单，1表示非数字订单）。
     * @return 返回一个ResponseResult对象，其中包含查询结果。
     */
    @Override
    public ResponseResult isVirtualOrder(Map<String, String> orderIdIsNum) {
        // 从映射表中获取订单ID和判断标志
        String orderid = orderIdIsNum.get("orderId");
        String isNum = orderIdIsNum.get("isNum");
        Map<String, Object> map = null;

        // 根据判断标志确定查询参数，查询订单是否为虚拟订单
        //0 是数字订单 1 不是数字订单
        if ("0".equals(isNum)) {
            map = this.baseMapper.queryisVirtualOrder(orderid, 0);
        } else {
            map = this.baseMapper.queryisVirtualOrder(orderid, 1);
        }

        // 返回查询结果
        return ResponseResult.success(map);
    }

    /**
     * 添加兑换码信息。
     *
     * @param param 兑换码及相关信息的映射表。
     * @return 如果添加成功，返回成功的响应结果；否则返回失败的响应结果。
     */
    @Override
    public ResponseResult addRedeemCode(Map<String, Object> param) {
        // 调用BaseMapper的addRedeemCode方法尝试添加兑换码
        Integer integer = this.baseMapper.addRedeemCode(param);
        // 判断添加结果，如果添加数量大于0，则视为添加成功
        if (integer > 0) {
            return ResponseResult.success();
        }
        // 如果添加数量不大于0，则视为添加失败
        return ResponseResult.fail();
    }

    /**
     * 根据支付号通知商家有新的订单。
     *
     * @param map 包含支付号和支付时间的Map。
     * @return 返回处理结果的ResponseResult对象。
     */
    @Override
    public ResponseResult orderToInform(Map<String, Object> map) {
        // 从map中获取支付号和支付时间
        // 支付号
        String payNumber = (String) map.get("orderId");
        String paymentTime = (String) map.get("paymentTime");
        boolean isInsertSuccess = false;
        Map<String, Object> messageMap = new HashMap<>();
        List<String> messageList = new ArrayList<>();
        messageMap.put("messageTitle", "下单通知");
        messageMap.put("messageType", 1);
        messageMap.put("templateId", CommonType.messageTemplate.CODE_G.getCode());
        messageMap.put("messageCreateTime", paymentTime);

        // 判断支付号是否为整数，用于区分处理方式
        if (MathUtil.isInteger(payNumber)) {
            // 根据数字支付号查询订单并发送通知
            OrderBaseEntity orderBaseEntity = this.baseMapper.selectById(payNumber);
            messageList.add(orderBaseEntity.getOrderId());
            messageMap.put("list", messageList);
            messageMap.put("userId", orderBaseEntity.getSellerUserId());
            messageMap.put("orderId", orderBaseEntity.getOrderId());
            messageMap.put("sellerId", orderBaseEntity.getSellerUserId());
            messageMap.put("sellerName", orderBaseEntity.getSellerUserName());
            messageMap.put("shopId", orderBaseEntity.getShopId());
            baseServiceClient.sendMessageTemplate(messageMap);
        } else {
            // 当支付号不是整数时，通过支付号查询订单相关列表信息
            List<OrderToInformParaVO> orderToInformList = this.baseMapper.queryBasePara(payNumber);
            // 遍历列表，对每个订单插入系统消息
            for (OrderToInformParaVO orderBaseEntity : orderToInformList) {
                messageList.add(orderBaseEntity.getOrderId());
                messageMap.put("list", messageList);
                messageMap.put("userId", orderBaseEntity.getSellerUserId());
                messageMap.put("orderId", orderBaseEntity.getOrderId());
                messageMap.put("sellerId", orderBaseEntity.getSellerUserId());
                messageMap.put("sellerName", orderBaseEntity.getSellerUserName());
                messageMap.put("shopId", orderBaseEntity.getShopId());
                baseServiceClient.sendMessageTemplate(messageMap);
            }
        }

        // 根据消息插入结果返回处理结果
        if (isInsertSuccess) {
            return ResponseResult.success();
        } else {
            return ResponseResult.fail();
        }
    }


    /**
     * 根据订单号查询UPay的支付结果。
     * <p>
     * 本方法通过调用底层的baseMapper，使用订单号查询UPay的支付情况。
     * 它封装了对数据库的操作，对外提供了一个简洁的接口来获取支付结果。
     *
     * @param order_id 订单号，用于查询UPay的支付结果。
     * @return ResponseResult 对象，包含查询结果的成功与否及具体信息。
     */
    @Override
    public ResponseResult queryUpayByOrderId(String order_id) {
        // 调用baseMapper的queryUpayByOrderId方法，根据订单号查询支付结果
        String s = this.baseMapper.queryUpayByOrderId(order_id);
        // 构造并返回一个表示查询成功的结果对象，其中包含查询结果的信息
        return ResponseResult.success(s);
    }

    @Override
    public ResponseResult PlatFormOfflineOrderEXcel(SellingInputDTO sellingInputDTO) throws ParseException {
        String userInfoStr = RedisUtil.get(HouConfig.HOU_PLATFORM_TOKEN_USER + sellingInputDTO.getToken());
        if (null == userInfoStr || userInfoStr.equals("")) {
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }

        Integer type = Integer.valueOf(sellingInputDTO.getType());
        String allOrderType = null;
        Integer otherOrderType = null;
        // 判断状态
        if (type == 1) {
            // 全部订单
            allOrderType = "' '";
        } else if (type == 2) {
            // 待付款订单
            otherOrderType = 1;
        } else if (type == 3) {
            // 已付款待发货订单
            otherOrderType = 3;
        } else if (type == 4) {
            // 已发货订单
            otherOrderType = 4;
        } else if (type == 5) {
            // 已完成订单
            otherOrderType = 6;
        } else if (type == 6) {
            // 已取消订单
            otherOrderType = 7;
        } else if (type == 7) {
            otherOrderType = 0;
        } else if (type == 8) {
            // 待审核上传支付凭证
            otherOrderType = 11;
        } else if (type == 9) {
            // 已审核上传支付凭证
            otherOrderType = 12;
        } else if (type == 10) {
            // 已驳回上传支付凭证
            otherOrderType = 13;
        } else if (type == 11) {
//				otherOrderType=14;//释放这一行，即查询出所有已上传支付凭证的线下订单
        }


        Map<Object, Object> mapobject = new HashMap<Object, Object>();
        mapobject.put("orderId", sellingInputDTO.getOrderId());
        mapobject.put("buyerUserName", sellingInputDTO.getBuyerUserName() == null ? "" : sellingInputDTO.getBuyerUserName());
        mapobject.put("shopName", sellingInputDTO.getShopName() == null ? "" : sellingInputDTO.getShopName());
        mapobject.put("paymentNo", sellingInputDTO.getPaymentNo());
        mapobject.put("allOrderType", allOrderType);
        mapobject.put("otherOrderType", otherOrderType);
        mapobject.put("airTime", sellingInputDTO.getStartTime());
        mapobject.put("finishTime", sellingInputDTO.getEndTime());
        //省id
        mapobject.put("provinceId", sellingInputDTO.getProvinceId());
        //市Id
        mapobject.put("cityId", sellingInputDTO.getDistrictCityId());
        //区id
        mapobject.put("districtId", sellingInputDTO.getAreaId());


        List<SellingOrderVO> sellingList = this.baseMapper.PlatFormOfflineOrderEXcel(mapobject);
        String[] headers = {"订单编号", "订单来源", "审核状态", "买家账号", "下单时间", "订单金额(元)", "支付金额(元)", "收货人名称", "联系方式", "收货地址", "支付单号",
                "店铺名称"};
        List<Map<String, String>> list = Lists.newArrayList();
        ResponseResult resp = new ResponseResult();
        for (SellingOrderVO res : sellingList) {
            HashMap<String, String> map = Maps.newHashMap();
            map.put("订单编号", res.getOrderId());
            if (res.getOrderFrom() != null) {

                switch (res.getOrderFrom()) {
                    //1PC 2wap微信浏览器 3安卓 5ios 6微信小程序 7wap浏览器
                    case "1":
                        res.setOrderFrom("pc");
                        break;
                    case "2":
                        res.setOrderFrom("wap微信浏览器");
                        break;
                    case "3":
                        res.setOrderFrom("安卓");
                        break;
                    case "5":
                        res.setOrderFrom("ios");
                        break;
                    case "6":
                        res.setOrderFrom("微信小程序");
                        break;
                    case "7":
                        res.setOrderFrom("wap浏览器");
                        break;
                }
            }
            map.put("订单来源", res.getOrderFrom());
            if (res.getReviewStatus() != null) {

                switch (res.getReviewStatus()) {
                    //审核状态  0 待上传   1 待审核 2 已审核 3 审核驳回
                    case 0:
                        map.put("审核状态", "待上传");
                        break;
                    case 1:
                        map.put("审核状态", "待审核");
                        break;
                    case 2:
                        map.put("审核状态", "已审核");
                        break;
                    case 3:
                        map.put("审核状态", "审核驳回");
                        break;
                }
            } else {
                map.put("审核状态", "无");
            }
            map.put("买家账号", res.getBuyerUserName());
            map.put("下单时间", res.getOrderCreateTime());
            map.put("订单金额(元)", res.getOrderAmount() + "");
            map.put("支付金额(元)", res.getOrderPaymentAmount() + "");
            map.put("收货人名称", res.getOrderReceiverName());
            map.put("收货地址", res.getOrderReceiverAddress());
            map.put("联系方式", res.getOrderReceiverContact());
            map.put("支付单号", res.getPaymentNumber());
            map.put("店铺名称", res.getShopName());
            list.add(map);
        }
        // excel文件名
        Map<String, List<Map<String, String>>> map = Maps
                .newHashMap();
        map.put("合并数据", list);
        String url = ExcelsUtil.createExcel(headers, map, new int[]{0});
        resp.setCode(1);
        resp.setData(url);
        return resp;
    }

    @Override
    public ResponseResult PlatFormBackGroundBySellingNew(SellingInputNewDTO sellingPara) throws ParseException, IllegalAccessException {

        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(sellingPara.getToken());

        if (null != userInfo) {
            //实物订单
            if (null != sellingPara.getOrderType() && 1 == sellingPara.getOrderType()) {
                //虚拟订单 0 是 1 否
                sellingPara.setVirtualGoods(1);
                //门店自提订单 1是 2否
                sellingPara.setOrderIsShopTake(2);
                //虚拟订单（不包含门店虚拟订单）
            } else if (null != sellingPara.getOrderType() && 2 == sellingPara.getOrderType()) {
                //虚拟订单 0 是 1 否
                sellingPara.setVirtualGoods(0);
                //门店自提订单 1是 2否
                sellingPara.setOrderIsShopTake(2);
                //门店订单（包含门店虚拟订单）
            } else if (null != sellingPara.getOrderType() && 3 == sellingPara.getOrderType()) {
                //门店自提订单 1是 2否
                sellingPara.setOrderIsShopTake(1);
                // 是否同城配送2-否 1-是
                sellingPara.setIsDelivery(2);
            } else if (null != sellingPara.getOrderType() && 4 == sellingPara.getOrderType()) {
                //门店自提订单 1是 2否
                sellingPara.setOrderIsShopTake(1);
                // 是否同城配送2-否 1-是
                sellingPara.setIsDelivery(1);
            }

            if (null != sellingPara.getOrderStatus() && sellingPara.getOrderStatus().equals(3)) {
                // 已付款待发货订单
                sellingPara.setReturnStatus(0);
                sellingPara.setRefundStatus(0);
            } else if (null != sellingPara.getOrderStatus() && sellingPara.getOrderStatus().equals(4)) {
                // 已发货订单
                sellingPara.setReturnStatus(0);
                sellingPara.setRefundStatus(0);
            } else if (null != sellingPara.getOrderStatus() && sellingPara.getOrderStatus().equals(6)) {
                // 已完成订单
                sellingPara.setReturnStatus(0);
                sellingPara.setRefundStatus(0);
            } else if (null != sellingPara.getOrderStatus() && sellingPara.getOrderStatus().equals(8)) {
                // 退货中
                sellingPara.setOrderStatus(null);
                sellingPara.setReturnStatus(1);
            } else if (null != sellingPara.getOrderStatus() && sellingPara.getOrderStatus().equals(9)) {
                // 退款中
                sellingPara.setOrderStatus(null);
                sellingPara.setRefundStatus(1);
            } else if (null != sellingPara.getOrderStatus() && sellingPara.getOrderStatus().equals(10)) {
                // 退款完成
                sellingPara.setOrderStatus(null);
                sellingPara.setRefundStatus(2);
            } else if (null != sellingPara.getOrderStatus() && sellingPara.getOrderStatus().equals(11)) {
                // 退货完成
                sellingPara.setOrderStatus(null);
                sellingPara.setReturnStatus(2);
            } else if (null != sellingPara.getOrderStatus() && sellingPara.getOrderStatus().equals(12)) {
                sellingPara.setPickup(null);
                if(2 == sellingPara.getOrderType()){
                    //虚拟单已过期
                    sellingPara.setOrderStatus(12);
                }else{
                    // 待接单
                    sellingPara.setOrderStatus(3);
                }
            } else if (null != sellingPara.getOrderStatus() && sellingPara.getOrderStatus().equals(13)) {
                // 待取货
                sellingPara.setOrderStatus(3);
                sellingPara.setPickup(0);
            } else if (null != sellingPara.getOrderStatus() && sellingPara.getOrderStatus().equals(14)) {
                // 待配送
                sellingPara.setOrderStatus(3);
                sellingPara.setPickup(1);
            }
            if (sellingPara.getPayCode() != null && sellingPara.getPayCode().equals(7)) {
                sellingPara.setPayCode(null);
                sellingPara.setPaymentCode(2);
            } else if (sellingPara.getPayCode() != null && sellingPara.getPayCode().equals(8)) {
                sellingPara.setPayCode(null);
                sellingPara.setPaymentCode(3);
            }
            Map data = ParseMapUtils.beanToMap(sellingPara);
            IPage<SellingOrderVO> page = this.baseMapper.selectSellingOrderNew2(new Query<SellingOrderVO>().getPage(data),
                    sellingPara);
            String orderMoney = this.baseMapper.querySellingOrderMoneynNew2(sellingPara);
            if (orderMoney == null) {
                orderMoney = "0";
            }
            Integer returnFlag = 0;
            String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.DESENSITIZATION_SWITCH.getDesc());
            if (null != page.getRecords() && page.getRecords().size() > 0) {
                for (SellingOrderVO sellingOrderPara : page.getRecords()) {
                    if (StrUtil.isNotEmpty(value) && value.equals(CommonType.commonStatus.YES.getCode().toString())) {
                        BeanUtils.copyProperties(DesensitizationUtil.Desensitization(sellingOrderPara, SellingOrderVO.class), sellingOrderPara);
                    }
                    Integer virtualGoods = sellingOrderPara.getVirtualGoods();
                    Integer orderIsShopTake = sellingOrderPara.getOrderIsShopTake();
                    Integer isDelivery = sellingOrderPara.getIsDelivery();
                    if (null != orderIsShopTake && 1 == orderIsShopTake) {
                        sellingOrderPara.setOrderType(3);
                    } else {
                        if (null != virtualGoods && 0 == virtualGoods) {
                            sellingOrderPara.setOrderType(2);
                            sellingOrderPara.setPastDue(sellingOrderPara.getPastDue());
                            sellingOrderPara.setIsCancel(sellingOrderPara.getIsCancel());
                        } else {
                            sellingOrderPara.setOrderType(1);
                        }
                    }
                    if (null != isDelivery && isDelivery == 1) {
                        sellingOrderPara.setOrderType(4);
                    }
                    Integer returnStatus = Integer.valueOf(sellingOrderPara.getOrderReturnStatus());
                    Integer refundStatus = Integer.valueOf(sellingOrderPara.getOrderRefundStatus());
                    if (2 == returnStatus || 2 == refundStatus) {
                        BigDecimal returnAmount = sellingOrderPara.getOrderRefundAmount();
                        BigDecimal orderAmount = sellingOrderPara.getOrderPaymentAmount();
                        if (orderAmount.compareTo(returnAmount) > 0) {
                            returnFlag = 2;
                        } else if ((orderAmount.compareTo(returnAmount) == 0)) {
                            returnFlag = 1;
                        }
                    }
                    sellingOrderPara.setReturnFlag(returnFlag);
                    //第三方支付金额赋值 2-支付宝支付，3-微信支付
                    if ("2".equals(sellingOrderPara.getPayCode()) || "3".equals(sellingOrderPara.getPayCode())) {
                        sellingOrderPara.setTradeThirdPartyAmount(sellingOrderPara.getOrderPaymentAmount());
                    } else if ("6".equals(sellingOrderPara.getPayCode())) {
                        sellingOrderPara.setTradePaymentRechargeCard(sellingOrderPara.getOrderPaymentAmount());
                    }
                    if (1 == returnStatus || 2 == returnStatus || 1 == refundStatus || 2 == refundStatus) {
                        sellingOrderPara.setIsExistReturn("1");
                    } else {
                        sellingOrderPara.setIsExistReturn("0");
                    }
                }

            }
            HashMap<String, Object> map = new HashMap<>();
            map.put("orderMoney", orderMoney);
            map.put("data", PageUtils.getPage(page));
            return ResponseResult.success(map);
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
    }

    @Override
    public ResponseResult platFormOnlineOrdersEXcel(SellingInputNewDTO sellingPara) {
        UserInfoVO userInfoVO = ParseTokenUtils.getPlatformUser(sellingPara.getToken());
        if (null == userInfoVO) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        if (sellingPara.getPayCode() != null && sellingPara.getPayCode().equals(7)) {
            sellingPara.setPayCode(null);
            sellingPara.setPaymentCode(2);
        } else if (sellingPara.getPayCode() != null && sellingPara.getPayCode().equals(8)) {
            sellingPara.setPayCode(null);
            sellingPara.setPaymentCode(3);
        }
        String[] headers = {"订单编号", "订单来源", "付款来源", "买家账号", "下单时间", "订单金额(元)", "支付金额(元)", "订单状态",
                "收件人姓名", "联系方式", "收货地址", "是否存在退货退款", "支付单号", "第三方支付单号", "支付方式", "充值卡余额支付",
                "第三方支付金额", "支付时间", "发货物流单号", "退款金额", "订单完成时间", "是否拼团", "是否评价", "店铺名称"};
        List<Map<String, String>> list = Lists.newArrayList();

        List<SellingOrderVO> result = this.baseMapper.selectSellingOrderNewEXcel(sellingPara);

        for (SellingOrderVO res : result) {
            HashMap<String, String> map = com.google.common.collect.Maps.newHashMap();
            //第三方支付金额赋值 2-支付宝支付，3-微信支付
            if ("2".equals(res.getPayCode()) || "3".equals(res.getPayCode())) {
                res.setTradeThirdPartyAmount(res.getOrderPaymentAmount());
            } else if ("6".equals(res.getPayCode())) {
                res.setTradePaymentRechargeCard(res.getOrderPaymentAmount());
            }
            if (res.getOrderStatus() != null) {
                switch (res.getOrderStatus()) {
                    case "1":
                        res.setOrderStatus("待付款 ");
                        break;
                    case "2":
                        res.setOrderStatus("已付款");
                        break;
                    case "3":
                        res.setOrderStatus("待发货  ");
                        break;
                    case "4":
                        res.setOrderStatus("已发货 ");
                        break;
                    case "5":
                        res.setOrderStatus("已签收 ");
                        break;
                    case "6":
                        res.setOrderStatus("确认收货");
                        break;
                    case "7":
                        res.setOrderStatus("取消 ");
                        break;
                    case "8":
                        res.setOrderStatus("退款中");
                        break;
                    case "9":
                        res.setOrderStatus("已退款 ");
                        break;
                    case "11":
                        res.setOrderStatus("待自提");
                        break;
                    case "12":
                        res.setOrderStatus("已过期");
                        break;
                }
                if (res.getOrderReturnStatus().equals("2") && res.getOrderStatus().equals("已退款 ")) {
                    res.setOrderStatus("已退货 ");
                }
            }
            if (res.getPayCode() != null) {

                switch (res.getPayCode()) {
                    // 支付方式1-余额支付，2-支付宝支付，3-微信支付，6-购物卡支付，12 线下支付 16-paypal支付 17-0元兑换券 18 Stripe 支付
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
                        res.setPayCode("充值卡余额支付 ");
                        break;
                    case "12":
                        res.setPayCode("线下支付");
                        break;
                    case "16":
                        res.setPayCode("Paypal支付");
                        break;
                    case "17":
                        res.setPayCode("0元兑换券");
                        break;
                    case "18":
                        res.setPayCode("Stripe 支付");
                        break;
                }
            }

            if (res.getOrderFrom() != null) {

                switch (res.getOrderFrom()) {
                    //1PC 2wap微信浏览器 3安卓 5ios 6微信小程序 7wap浏览器
                    case "1":
                        res.setOrderFrom("pc");
                        break;
                    case "2":
                        res.setOrderFrom("wap微信浏览器");
                        break;
                    case "3":
                        res.setOrderFrom("安卓");
                        break;
                    case "5":
                        res.setOrderFrom("ios");
                        break;
                    case "6":
                        res.setOrderFrom("微信小程序");
                        break;
                    case "7":
                        res.setOrderFrom("wap浏览器");
                        break;
                }
            }

            if (res.getOrderRefundStatus() != null) {

                switch (res.getOrderRefundStatus()) {
                    // 0是无退款,1是退款中,2是退款完成',
                    case "0":
                        res.setOrderRefundStatus("无退款");
                        break;
                    case "1":
                        res.setOrderRefundStatus("退款中 ");
                        res.setOrderStatus("退款中 ");
                        break;
                    case "2":
                        res.setOrderRefundStatus("退款完成");
                        res.setOrderStatus("退款完成 ");
                        break;

                }
            }

            if (res.getOrderReturnStatus() != null) {

                switch (res.getOrderReturnStatus()) {
                    // 0是无退款,1是退款中,2是退款完成',
                    case "0":
                        res.setOrderReturnStatus("无退货");
                        break;
                    case "1":
                        res.setOrderReturnStatus("退货中 ");
                        res.setOrderStatus("退货中 ");
                        break;
                    case "2":
                        res.setOrderReturnStatus("退货完成");
                        res.setOrderStatus("退货完成 ");
                        break;
                }
            }
            if (res.getIsExistReturn() != null) {
                switch (res.getIsExistReturn()) {
                    case "0":
                        res.setIsExistReturn("否");
                        break;
                    case "1":
                        res.setIsExistReturn("是 ");
                        break;
                }
            }
            if (res.getOrderBuyerEvaluationStatus() != null) {
                switch (res.getOrderBuyerEvaluationStatus()) {
                    case "0":
                        res.setOrderBuyerEvaluationStatus("未评价");
                        break;
                    case "1":
                        res.setOrderBuyerEvaluationStatus("已评价 ");
                        break;
                }
            }
            if (!"".equals(res.getGroupbuyStatus()) && res.getGroupbuyStatus() != null) {
                switch (res.getGroupbuyStatus()) {
                    case "0":
                        res.setGroupbuyStatus("非拼团");
                        break;
                    case "1":
                        res.setGroupbuyStatus("拼团中");
                        break;
                    case "2":
                        res.setGroupbuyStatus("已成功");
                        break;
                    case "3":
                        res.setGroupbuyStatus("未成功");
                        break;
                }

            }
            map.put("订单编号", res.getOrderId());
            map.put("订单来源", res.getOrderFrom() == null ? "无" : res.getOrderFrom());
            map.put("付款来源", res.getPayCode());
            map.put("买家账号", res.getBuyerUserName());
            map.put("下单时间", res.getOrderCreateTime());
            map.put("订单金额(元)", res.getOrderAmount() + "");
            map.put("支付金额(元)", res.getOrderPaymentAmount() + "");
            map.put("订单状态", res.getOrderStatus() == null ? "无" : res.getOrderStatus());
            map.put("收件人姓名", res.getOrderReceiverName() == null ? "无" : res.getOrderReceiverName());
            map.put("联系方式", res.getOrderReceiverContact());
            map.put("收货地址", res.getOrderReceiverAddress());
            map.put("是否存在退货退款", res.getIsExistReturn());
            map.put("支付单号", res.getPaymentNumber());
            map.put("第三方单号", res.getPaymentOtherNumber());
            map.put("支付方式", res.getPaymentName() == null ? "无" : res.getPaymentName());
            map.put("充值卡余额支付", res.getTradePaymentRechargeCard() + "");
            map.put("第三方支付金额", res.getTradeThirdPartyAmount() + "");
            map.put("支付时间", res.getPaymentTime() == null ? "无" : res.getPaymentTime());
            map.put("发货物流单号", res.getOrderShippingCode() == null ? "无" : res.getOrderShippingCode());
            map.put("退款金额", res.getOrderRefundAmount() == null ? "0.00" : res.getOrderRefundAmount().toString());
            map.put("订单完成时间", res.getOrderFinishedTime() == null ? "无" : res.getOrderFinishedTime());
            map.put("是否拼团", res.getGroupbuyStatus());
            map.put("是否评价", res.getOrderBuyerEvaluationStatus());
            map.put("店铺名称", res.getShopName());
            list.add(map);
        }

        // excel文件名
        Map<String, List<Map<String, String>>> map = Maps
                .newHashMap();
        map.put("全部订单", list);
        String url = ExcelsUtil.createExcel(headers, map, new int[]{0});
        return ResponseResult.success(url);
    }

    /**
     * 根据订单ID查询订单信息。
     * <p>
     * 本方法通过调用底层Mapper接口，使用给定的订单ID查询订单实体。随后，将查询到的实体对象的信息
     * 复制到一个订单VO对象中，以供前端使用。最后，将包含订单VO的响应结果返回。
     *
     * @param OrderId 订单的唯一标识ID。
     * @return 包含查询到的订单信息的响应结果对象。
     */
    @Override
    public ResponseResult selectOrderInfoById(String OrderId) {
        // 根据OrderId查询订单实体
        OrderBaseEntity entity = this.baseMapper.selectById(OrderId);
        // 创建一个订单VO对象，用于封装查询结果
        OrderBaseVO vo = new OrderBaseVO();
        // 将查询到的订单实体属性复制到订单VO对象中
        BeanUtils.copyProperties(entity, vo);
        // 构造并返回包含查询到的订单信息的成功响应结果
        return ResponseResult.success(vo);
    }

    /**
     * 根据传入的用户信息和特定条件，获取员工列表。
     * 此方法用于确定一个订单的处理人员列表，这取决于购买用户的ID是否存在。
     * 如果购买用户ID存在且不为"0"，则列表中只包含购买用户ID。
     * 如果购买用户ID不存在或为"0"，则列表中只包含当前用户ID。
     * 这种设计用于在不同的业务场景下，将订单分配给不同的处理人员。
     *
     * @param ui        用户信息，此处未在方法逻辑中直接使用，但作为方法参数的一部分，可能在未来的扩展中使用。
     * @param userId    当前用户的ID，用于在购买用户ID不存在或为"0"时，确定订单的处理人员。
     * @param buyUserId 购买用户的ID，如果存在且不为"0"，则用于确定订单的处理人员。
     * @return 返回一个员工ID的列表，列表中可能包含一个或零个ID，具体取决于参数的值。
     */
    private List<String> getEmployees(UserInfoVO ui, String userId, String buyUserId) {
        List<String> userIdList = new ArrayList<>();
        // 根据buyUserId的值决定是否添加到userIdList中
        if (buyUserId != null && !"0".equals(buyUserId)) {
            userIdList.add(buyUserId);
        } else {
            userIdList.add(userId);
        }
        return userIdList;
    }


    @Override
    public ResponseResult UpdateOrderBaseByPayment(Map<String, Object> map) {
        // 支付订单号
        String paymentNumbers = (String) map.get("payOrderId");
        Integer type = Integer.valueOf(map.get("type").toString());
        Integer payCode = null;
        switch (type) {
            case 1:
                payCode = 1;
                break;
            case 2:
                payCode = 6;
                break;
            case 3:
                payCode = 9;
                break;
            case 6:
                //微信支付
                payCode = 3;
                break;
            case 12:
                //线下支付 上传凭证
                payCode = 12;
                break;
        }
        boolean isUpdateBaseSuccess = false;
        boolean isUpdateGoodsSuccess = false;
        boolean flag = false;
        // 支付方式
        Integer paymentId = (Integer) map.get("paymentId");
        // 支付方式名称
        String paymentName = (String) map.get("paymentName");
        // 支付时间
        String paymentTime = (String) map.get("paymentTime");

        // 支付订单信息
        List<String> orderIdList = new ArrayList<>();
        //虚拟订单 0 是 1 否
        Integer virtualGoods = (Integer) map.get("virtualGoods");
        //门店订单 1 是 2 否
        Integer orderIsShopTake = (Integer) map.get("orderIsShopTake");
        // 门店订单 是否同城配送2-否 1-是
        Integer isDelivery = (Integer) map.get("isDelivery");
        //3.待发货 4.已发货
        Integer orderStatus = 3;
        //虚拟订单 0 是 1 否
        if (null != virtualGoods && 0 == virtualGoods) {
            orderStatus = 4;
        }
        //门店订单 1 是 2 否 是否同城配送2-否 1-是  是门店订单且不是同城配送 才改订单状态为4
        if (null != orderIsShopTake && 1 == orderIsShopTake && isDelivery == 2) {
            orderStatus = 4;
        }
        // 构建订单更新信息
        Map<String, Object> mapPayment = new HashMap<>();
        mapPayment.put("orderStatus", orderStatus);
        mapPayment.put("paymentId", paymentId);
        mapPayment.put("paymentName", paymentName);
        mapPayment.put("paymentTime", paymentTime);
        mapPayment.put("paymentNumbers", paymentNumbers);
        mapPayment.put("payCode", payCode);
        if (MathUtil.isInteger(paymentNumbers)) {
            isUpdateBaseSuccess = this.baseMapper.updateOrderBaseByPaymentByPureDigital(orderStatus, paymentId, paymentName, paymentTime,
                    paymentNumbers, payCode);

            orderIdList.add(paymentNumbers);
            isUpdateGoodsSuccess = orderGoodsMapper.updateOrderGoodsByStatusByPureDigital(orderStatus, paymentNumbers);
            flag = isUpdateBaseSuccess && isUpdateGoodsSuccess;
        } else {
            isUpdateBaseSuccess = this.baseMapper.updateOrderBaseByPayment(orderStatus, payCode, paymentId, paymentName, paymentTime,
                    paymentNumbers);
            // 获取实际支付单号
            orderIdList = this.baseMapper.queryActualOrderId(paymentNumbers);
            for (String orderNumber : orderIdList) {
                // 实际订单号
                isUpdateGoodsSuccess = orderGoodsMapper.updateOrderGoodsByStatus(orderStatus, orderNumber);
            }
            flag = isUpdateBaseSuccess && isUpdateGoodsSuccess;
        }
        // 根据订单更新是否成功返回结果
        if (flag) {
            //查询当前订单是否有一件代发的子订单
            List<String> orders = this.baseMapper.querySubOrderId(paymentNumbers);
            if (!orders.isEmpty()) {
                for (String str : orders) {
                    //改子订单状态
                    mapPayment.put("paymentNumbers", str);
                    this.baseMapper.updateOrderBaseByPaymentBySub(mapPayment);
                    orderIdList.add(str);
                    orderGoodsMapper.update(null, new LambdaUpdateWrapper<OrderGoodsEntity>()
                            .eq(OrderGoodsEntity::getOrderId, str)
                            .set(OrderGoodsEntity::getOrderGoodsStatus, orderStatus));
                }
            }
            return ResponseResult.success(true);
        } else {
            return ResponseResult.fail(false);
        }
    }

    /**
     * 根据支付信息更新订单基础信息和商品状态。
     *
     * @param map 包含支付相关信息的Map对象。
     * @return boolean 更新操作是否成功的标志。
     */
    @Override
    public boolean fiveUpdateOrderBaseByPayment(Map<Object, Object> map) {
        // 从map中提取支付相关的字段
        // 商户订单号
        String paymentNumber = (String) map.get("paymentNumber");
        boolean flag = false;
        Integer payCode = (Integer) map.get("payCode");
        Integer orderStatus = (Integer) map.get("orderStatus");
        // 支付方式
        Integer paymentId = (Integer) map.get("paymentId");
        // 支付方式名称
        String paymentName = (String) map.get("paymentName");
        // 支付时间
        String paymentTime = (String) map.get("paymentTime");
        // 第三方交易流水号
        String paymentOtherNumber = (String) map.get("paymentOtherNumber");
        String orderFrom = (String) map.get("orderFrom");

        // 准备用于更新订单基础信息的参数
        // 支付订单信息
        List<String> orderIdList = new ArrayList<>();
        Map<String, Object> mapDigital = new HashMap<>();
        mapDigital.put("orderStatus", orderStatus);
        mapDigital.put("paymentId", paymentId);
        mapDigital.put("paymentName", paymentName);
        mapDigital.put("paymentTime", paymentTime);
        mapDigital.put("paymentOtherNumber", paymentOtherNumber);
        mapDigital.put("paymentNumber", paymentNumber);
        mapDigital.put("payCode", payCode);
        //参数不定
        mapDigital.put("orderFrom", orderFrom);

        // 判断paymentNumber是否为纯数字，以确定使用哪种更新策略
        if (MathUtil.isInteger(paymentNumber)) {
            // 是纯数字，则为实际支付单号
            boolean isBaseSuccess = false;
            if (orderFrom != null) {
                isBaseSuccess = this.baseMapper.updateBaseByPureDigital(mapDigital);
            } else {
                isBaseSuccess = this.baseMapper.fiveUpdateOrderBaseToPaymentByPureDigital(mapDigital);
            }
            boolean isGoodsSuccess = this.baseMapper.fiveUpdateOrderGoodsToGoodsStatusByPureDigital(orderStatus, paymentNumber);

            orderIdList.add(paymentNumber);
            flag = isBaseSuccess && isGoodsSuccess;
        } else {
            // 如果不是纯数字，则为支付单号
            boolean isBaseSuccess = false;
            boolean isGoodsSuccess = false;
            if (orderFrom != null) {
                isBaseSuccess = this.baseMapper.updateBaseToPayment(mapDigital);
            } else {
                isBaseSuccess = this.baseMapper.fiveUpdateOrderBaseToPayment(mapDigital);
            }
            orderIdList = this.baseMapper.queryOrderIdForPayNumber(paymentNumber);
            for (String orderNumber : orderIdList) {
                isGoodsSuccess = this.baseMapper.fiveUpdateOrderGoodsToGoodsStatus(orderStatus, orderNumber);
            }
            flag = isBaseSuccess && isGoodsSuccess;
        }

        return flag;
    }

    /**
     * 得到快递信息
     *
     * @param shipmentId
     * @return
     */
    public Map<String, String> getShipperCode2(Integer shipmentId) {
        Map map = new HashMap();
        Map result = orderGoodsMapper.getShipperCode2(shipmentId);
        //获取快递简称
        Integer orderShippingExpressId = Integer.parseInt(result.get("orderShippingExpressId").toString());
        ResponseResult shipperResp = baseServiceClient.getBaseExpressInfoById(orderShippingExpressId);
        Map<String, String> shipperCode = JSONObject.parseObject(JSON.toJSONString(shipperResp.getData()), Map.class);
        map.put("shipperCode", shipperCode.get("expressPinyin"));
        map.put("expressName", shipperCode.get("expressName"));
        //获取快递鸟的配置
        ResponseResult expressResp = baseServiceClient.queryConfigFeign();
        BaseKdniaoConfigDTO expressQueryByKdNiao = JSON.parseObject(JSON.toJSONString(expressResp.getData()), BaseKdniaoConfigDTO.class);
        map.put("AppKey", expressQueryByKdNiao.getAppKey());
        map.put("EBussinessID", expressQueryByKdNiao.getEBussinessID());
        map.put("instructCode", expressQueryByKdNiao.getInstructCode());
        //获取收件人手机号的后4位
        String orderReceiverContact = (String) result.get("orderReceiverContact");
        String customerName = orderReceiverContact.substring(orderReceiverContact.length() - 4);
        map.put("customerName", customerName);
        String receiver = result.get("orderReceiverAddress").toString();
        map.put("receiver", receiver);
        return map;
    }

    /**
     * 根据商家订单ID查询订单发票信息。
     * 此方法用于查询商家订单的发票列表，包括订单的详细信息和相关的商品信息。
     *
     * @param param 查询参数，包含token等必要信息。
     * @return 返回查询结果，包含订单发票信息和分页信息。
     */
    @Override
    public ResponseResult querySellerOrderInvoiceList(Map<String, Object> param) {
        // 从参数中提取token
        String token = (String) param.get("token");
        // 根据token解析用户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);

        // 尝试将商家ID转换为整型
        String shopId = ui.getShopId();
        // 如果商家ID为空，则返回权限不足的错误信息
        if (StringUtils.isEmpty(shopId)) {
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
        // 校验token的有效性
        ResponseResult result = checkToken(token);
        // 如果token校验失败，则直接返回错误结果
        if (result.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return result;
        } else {
            // 如果token校验成功，将用户ID添加到查询参数中
            param.put("userId", result.getData().toString());
            param.put("shopId", shopId);
        }
        try {
            // 查询订单发票信息，包括分页信息
            IPage<SellerOrderInvoiceVO> page = this.baseMapper.querySellerOrderInvoiceInfo(new Query<SellerOrderInvoiceVO>().getPage(param), param);
            // 遍历查询结果，为每个订单查询商品详情，并补充发票类型信息
            for (SellerOrderInvoiceVO order : page.getRecords()) {
                // 为查询商品详情，将订单ID添加到参数中
                param.put("orderId", order.getOrderId());
                // 查询订单相关的商品详情
                List<OrderGoodsDetailVO> goodsInfo = orderBaseMapper.queryOrderGoodsInfo2(param);
                // 将商品详情设置到订单对象中
                order.setGoodsList(goodsInfo);
                // 如果订单有发票ID，查询并设置发票类型
                param.put("invoiceId", order.getInvoiceId());
                if (null != order.getInvoiceId()) {
                    Integer invoiceType = orderBaseMapper.getInvoiceType(param);
                    order.setInvoiceType(invoiceType);
                }
            }
            // 构建成功返回结果，包含分页处理后的订单发票信息
            return ResponseResult.success(PageUtils.getPage(page));
        } catch (Exception e) {
            // 记录异常信息，并返回失败结果
            log.error(e.getMessage());
            return ResponseResult.fail(e.getMessage());
        }
    }

    /**
     * 已售订单查询
     *
     * @param map
     * @return
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    @Override
    public ResponseResult soldOrders(Map<String, Object> map) {
        ResponseResult rr = new ResponseResult();
        String token = (String) map.get("token");
        boolean isExist = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (isExist) {
            try {
                UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
                Integer shopId = Integer.valueOf(ui.getShopId());
                if (null == ui.getShopId()) {
                    rr.setCode(CommonStatusEnum.FAIL.getCode());
                    rr.setMessage(BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
                    return rr;
                }
                setOrderTime(map);
                String userName = (String) map.get("userName");
                String orderNum = (String) map.get("orderNum");
                String goodsName = (String) map.get("goodsName");
                String orderStartTime = (String) map.get("orderStartTime");
                String orderEndTime = (String) map.get("orderEndTime");
                Integer status = Integer.parseInt(map.get("status") + "");
                String allOrderStatus = null;
                Integer otherOrderStatus = null;
                //退货状态
                Integer orderReturnStatus1 = null;
                //退款状态
                Integer orderRefundStatus1 = null;
                if (status == 1) {
                    allOrderStatus = "' '";
                } else if (status == 2) {
                    // 待付款
                    otherOrderStatus = 1;
                    orderReturnStatus1 = 0;
                    orderRefundStatus1 = 0;
                } else if (status == 3) {
                    // 已付款
                    otherOrderStatus = 3;
                    orderReturnStatus1 = 0;
                    orderRefundStatus1 = 0;
                } else if (status == 4) {
                    // 已发货
                    otherOrderStatus = 4;
                    orderReturnStatus1 = 0;
                    orderRefundStatus1 = 0;
                } else if (status == 5) {
                    // 已完成
                    otherOrderStatus = 6;
                    orderReturnStatus1 = 0;
                    orderRefundStatus1 = 0;
                } else if (status == 6) {
                    // 已取消
                    otherOrderStatus = 7;
                    orderReturnStatus1 = 0;
                    orderRefundStatus1 = 0;
                } else if (status == 7) {
                    // 退货中
                    otherOrderStatus = null;
                    orderReturnStatus1 = 1;
                } else if (status == 8) {
                    // 退款中
                    otherOrderStatus = null;
                    orderRefundStatus1 = 1;
                } else if (status == 9) {
                    // 退款完成
                    otherOrderStatus = null;
                    orderRefundStatus1 = 2;
                } else if (status == 10) {
                    // 退货完成
                    otherOrderStatus = null;
                    orderReturnStatus1 = 2;
                } else if (status == 16) {
                    otherOrderStatus = 16;
                    orderReturnStatus1 = 0;
                    orderRefundStatus1 = 0;
                }
                Integer dropShipping = null;
                if (null != map.get("dropShipping")) {
                    dropShipping = Integer.parseInt(map.get("dropShipping") + "");
                }

                map.put("shopId", shopId);
                map.put("allOrderStatus", allOrderStatus);
                map.put("otherOrderStatus", otherOrderStatus);
                map.put("orderReturnStatus", orderReturnStatus1);
                map.put("orderRefundStatus", orderRefundStatus1);
                map.put("userName", userName);
                map.put("orderNum", orderNum);
                map.put("goodsName", goodsName);
                map.put("orderStartTime", orderStartTime);
                map.put("orderEndTime", orderEndTime);
                map.put("dropShipping", dropShipping);
                map.put("orderSource", 2);

                IPage<OrderDetailsVO> page = this.baseMapper.queryOrderBase(new Query<OrderDetailsVO>().getPage(map),
                        map);


                for (int i = 0; i < page.getRecords().size(); i++) {
                    String orderId = page.getRecords().get(i).getOrderId();
                    // 退款状态
                    Integer orderRefundStatus = page.getRecords().get(i).getOrderRefundStatus();
                    // 退货状态
                    Integer orderReturnStatus = page.getRecords().get(i).getOrderReturnStatus();
                    // 订单状态
                    Integer normalStatus = page.getRecords().get(i).getOrderStatus();
                    // 涉及退款退货订单状态
                    Integer Orderstatus = page.getRecords().get(i).getOrderStatus();
                    //满减赠赠品信息
                    String giveawayInfo = page.getRecords().get(i).getGiveawayInfo();

                    List<ManJianZengPinVO> list;
                    if (null != giveawayInfo && !"".equals(giveawayInfo)) {
                        list = JsonUtils.string2Obj(giveawayInfo, List.class, ManJianZengPinVO.class);
                        page.getRecords().get(i).setZengpinInfo(list);
                    }

                    if (normalStatus == 3) {
                        // 未发货状态
                        if (orderRefundStatus == 1) {
                            // 未发货退款中
                            Orderstatus = 22;
                        } else if (orderRefundStatus == 2) {
                            // 商家拒绝或审核通过，继续发货，但不可再次申请退款
                            Orderstatus = 26;
                        } else if (orderRefundStatus == 0) {
                            Orderstatus = 3;
                        }
                    } else if (normalStatus == 6) {
                        // 已完成状态
                        if (orderRefundStatus == 1) {
                            // 收货退款中
                            Orderstatus = 23;
                        } else if (orderRefundStatus == 2) {
                            Orderstatus = 25;
                        } else if (orderRefundStatus == 0 && orderReturnStatus == 0) {
                            Orderstatus = 6;
                        } else if (orderReturnStatus == 1) {
                            // 收货退货中
                            Orderstatus = 24;
                        } else if (orderReturnStatus == 2) {
                            Orderstatus = 25;
                        } else if (orderReturnStatus == 0 && orderRefundStatus == 0) {
                            Orderstatus = 6;
                        }
                    }
                    // 存放最新状态
                    page.getRecords().get(i).setOrderStatus(Orderstatus);
                    List<GoodsDetailsVO> queryOrderDetails = orderBaseMapper.queryOrders(orderId);
                    if (queryOrderDetails != null) {
                        page.getRecords().get(i).setGoodsDetails(queryOrderDetails);
                    } else {
                        rr.setCode(CommonStatusEnum.FAIL.getCode());
                        rr.setMessage(BizCodeEnume.ORDER_IS_NULL.getMsg());
                    }
                }

                return ResponseResult.success(PageUtils.getPage(page));
            } catch (Exception e) {
                rr.setCode(500);
                e.printStackTrace();
                log.info("SoldOrdersErrorResults:" + e.getMessage());
            }
        } else {
            rr.setCode(CommonStatusEnum.FAIL.getCode());
            rr.setMessage(BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        return rr;
    }

    /**
     * 设置订单查询的时间范围。
     * 根据传入的map中"orderStartTime"的值，确定查询的起始和结束时间。
     * 如果"orderStartTime"为1，查询当天的订单；
     * 如果为2，查询前一天的订单；
     * 如果为3，查询前三个月的订单；
     * 其他情况，清空查询时间范围。
     *
     * @param map 用于传递和存储订单查询条件的Map对象。
     */
    private void setOrderTime(Map<String, Object> map) {
        // 检查map中是否包含了"orderStartTime"键
        if (map.get("orderStartTime") != null) {
            try {
                // 将"orderStartTime"的值转换为Integer类型
                Integer orderTime = (Integer) map.get("orderStartTime");
                // 创建日期格式化对象，用于日期的字符串和日期对象之间的转换
                SimpleDateFormat df = new SimpleDateFormat(DateUtils.YYYY_MM_DD_HH_MM_SS);
                // 根据不同的orderTime值，设置查询的起始和结束时间
                if (orderTime == 1) {
                    // 查询当天的订单，设置起始和结束时间为当天的0点和23点59分59秒
                    map.put("orderStartTime", DateUtils.getDayStartTime2());
                    map.put("orderEndTime", DateUtils.getDayEndTime2());
                } else if (orderTime == 2) {
                    // 查询前一天的订单，设置起始和结束时间为前一天的0点和23点59分59秒
                    map.put("orderStartTime", df.format(DateUtils.addDays(df.parse(DateUtils.getDayStartTime2()), -1)));
                    map.put("orderEndTime", df.format(DateUtils.addDays(df.parse(DateUtils.getDayEndTime2()), -1)));
                } else if (orderTime == 3) {
                    // 查询前三个月的订单，设置起始和结束时间为三个月前的0点和23点59分59秒
                    map.put("orderStartTime", df.format(DateUtils.addMonth(df.parse(DateUtils.getDayStartTime2()), -3)));
                    map.put("orderEndTime", df.format(DateUtils.addMonth(df.parse(DateUtils.getDayEndTime2()), -3)));
                } else {
                    // 其他情况，清空查询的时间范围
                    map.put("orderStartTime", "");
                    map.put("orderEndTime", "");
                }
            } catch (Exception ignored) {
                // 忽略日期转换过程中可能抛出的异常
            }
        }
    }


    @Override
    public ResponseResult allOrders(Map<String, Object> map) throws ParseException {
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        Integer shopId = Integer.valueOf(ui.getShopId());
        if (null == ui.getShopId()) {
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
        setOrderTime(map);
        Integer status = Integer.parseInt(map.get("status") + "");
        String allOrderStatus = null;
        Integer otherOrderStatus = null;
        //退货状态
        Integer orderReturnStatus1 = null;
        //退款状态
        Integer orderRefundStatus1 = null;
        if (status == 1) {
            allOrderStatus = "' '";
        } else if (status == 2) {
            // 待付款
            otherOrderStatus = 1;
            orderReturnStatus1 = 0;
            orderRefundStatus1 = 0;
        } else if (status == 3) {
            // 已付款
            otherOrderStatus = 3;
            orderReturnStatus1 = 0;
            orderRefundStatus1 = 0;
        } else if (status == 4) {
            // 已发货
            otherOrderStatus = 4;
            orderReturnStatus1 = 0;
            orderRefundStatus1 = 0;
        } else if (status == 5) {
            // 已完成
            otherOrderStatus = 6;
            orderReturnStatus1 = 0;
            orderRefundStatus1 = 0;
        } else if (status == 6) {
            // 已取消
            otherOrderStatus = 7;
            orderReturnStatus1 = 0;
            orderRefundStatus1 = 0;
        } else if (status == 7) {
            // 退货中
            orderReturnStatus1 = 1;
        } else if (status == 8) {
            // 退款中
            orderRefundStatus1 = 1;
        } else if (status == 9) {
            // 退款完成
            orderRefundStatus1 = 2;
        } else if (status == 10) {
            // 退货完成
            orderReturnStatus1 = 2;
        }
        Integer dropShipping = null;
        if (null != map.get("dropShipping")) {
            dropShipping = Integer.parseInt(map.get("dropShipping") + "");
        }

        map.put("shopId", shopId);
        map.put("allOrderStatus", allOrderStatus);
        map.put("otherOrderStatus", otherOrderStatus);
        map.put("orderReturnStatus", orderReturnStatus1);
        map.put("orderRefundStatus", orderRefundStatus1);
        map.put("userName", map.get("userName"));
        map.put("orderNum", map.get("orderNum"));
        map.put("goodsName", map.get("goodsName"));
        map.put("orderStartTime", map.get("orderStartTime"));
        map.put("orderEndTime", map.get("orderEndTime"));
        map.put("orderType", CommonType.orderType.GOODS_TYPE.getCode());
        map.put("dropShipping", dropShipping);
        map.put("orderSource", 2);
        IPage<OrderDetailsVO> page = this.baseMapper.queryAllOrderBase(new Query<OrderDetailsVO>().getPage(map),
                map);
        for (int i = 0; i < page.getRecords().size(); i++) {
            String orderId = page.getRecords().get(i).getOrderId();
            // 退款状态
            Integer orderRefundStatus = page.getRecords().get(i).getOrderRefundStatus();
            // 退货状态
            Integer orderReturnStatus = page.getRecords().get(i).getOrderReturnStatus();
            // 订单状态
            Integer normalStatus = page.getRecords().get(i).getOrderStatus();
            // 涉及退款退货订单状态
            Integer Orderstatus = null;
            //满减赠赠品信息
            String giveawayInfo = page.getRecords().get(i).getGiveawayInfo();

            List<ManJianZengPinVO> list;
            if (null != giveawayInfo && !"".equals(giveawayInfo)) {
                list = JsonUtils.string2Obj(giveawayInfo, List.class, ManJianZengPinVO.class);
                page.getRecords().get(i).setZengpinInfo(list);
            }

            if (normalStatus == 3) {
                // 未发货状态
                if (orderRefundStatus == 1) {
                    // 未发货退款中
                    Orderstatus = 22;
                } else if (orderRefundStatus == 2) {
                    // 商家拒绝或审核通过，继续发货，但不可再次申请退款
                    Orderstatus = 26;
                } else if (orderRefundStatus == 0) {
                    Orderstatus = 3;
                }
            } else if (normalStatus == 6) {
                // 已完成状态
                if (orderRefundStatus == 1) {
                    // 收货退款中
                    Orderstatus = 23;
                } else if (orderRefundStatus == 2) {
                    Orderstatus = 25;
                } else if (orderRefundStatus == 0 && orderReturnStatus == 0) {
                    Orderstatus = 6;
                } else if (orderReturnStatus == 1) {
                    // 收货退货中
                    Orderstatus = 24;
                } else if (orderReturnStatus == 2) {
                    Orderstatus = 25;
                }
            } else {
                Orderstatus = normalStatus;
            }
            // 存放最新状态
            page.getRecords().get(i).setOrderStatus(Orderstatus);
            List<GoodsDetailsVO> queryOrderDetails = orderBaseMapper.queryOrders(orderId);
            if (queryOrderDetails != null) {
                page.getRecords().get(i).setGoodsDetails(queryOrderDetails);
            } else {
                return ResponseResult.fail(BizCodeEnume.ORDER_IS_NULL);
            }
        }
        return ResponseResult.success(PageUtils.getPage(page));
    }


    /**
     * 假删除订单。
     * <p>
     * 该方法通过传入的token验证用户身份，并尝试对指定订单进行假删除操作。假删除意味着订单并非真正从数据库中删除，
     * 而是通过某种方式标记为已删除。这种方法通常用于避免真正删除订单带来的问题，如数据不可恢复等。
     * </p>
     *
     * @param map 包含令牌(token)和订单号(orderId)的映射表。
     * @return 返回操作结果，成功或失败，并附带相应的错误信息。
     */
    @Override
    @Transactional
    public ResponseResult pseudoDelete(Map<String, Object> map) {
        // 从请求参数中获取令牌信息
        String token = (String) map.get("token");
        // 通过令牌获取用户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
        // 验证令牌有效性
        if (ui != null) {
            // 从请求参数中获取订单号
            String orderId = (String) map.get("orderId");
            // 调用Mapper进行假删除操作
            boolean isSuccess = orderBaseMapper.pseudoDelete(orderId);
            // 根据操作结果返回相应的成功或失败信息
            if (isSuccess) {
                return ResponseResult.success(BizCodeEnume.DELETE_SUCCESS);
            } else {
                return ResponseResult.fail(BizCodeEnume.DELETE_ERROR);
            }
        } else {
            // 如果令牌无效，返回相应的错误信息
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
    }


    /**
     * 计算平台每日营业额
     * 该方法用于根据传入的token信息，查询并返回平台每日的营业额数据。
     *
     * @param map 包含token等信息的参数映射表。
     * @return 返回每日营业额数据的成功响应结果，如果token无效或查询过程中出现异常，则返回相应的错误响应结果。
     */
    @Override
    public ResponseResult dailyTurnoverOfThePlatform(Map<String, Object> map) {
        // 从参数映射中获取token信息
        String token = (String) map.get("token");

        // 根据token解析用户信息，验证token的有效性
        UserInfoVO info = ParseTokenUtils.getPlatformUser(token);
        if (info != null) {
            try {
                // 初始化今日销售额为0
                BigDecimal sales = new BigDecimal(0);
                // 查询今日销售额
                sales = orderBaseMapper.queryTodaySales();
                // 对查询到的销售额进行四舍五入处理，确保精度
                if (null != sales) {
                    sales = sales.setScale(2, RoundingMode.HALF_UP);
                } else {
                    // 如果查询结果为null，则重置销售额为0
                    sales = new BigDecimal(0);
                }
                // 返回查询成功的响应结果，包含今日销售额
                return ResponseResult.success(sales);
            } catch (Exception e) {
                // 在查询过程中发生异常时，返回系统错误的响应结果
                return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(), BizCodeEnume.SYSTEM_ERROR.getMsg());
            }
        } else {
            // 当token无效时，返回token无效的响应结果
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
    }

    /**
     * 根据订单详情进行业务管理。
     * 该方法主要用于根据传入的订单号查询订单详情，并根据订单状态和退款退货状态进行相应的逻辑处理，
     * 包括订单支付方式的名称设置、赠品信息的解析、订单状态的更新等。
     *
     * @param map 包含订单相关信息的Map对象，其中包含token、orderNum等关键信息。
     * @return 返回处理结果的ResponseResult对象。
     */
    @Override
    @SuppressWarnings("unused")
    public ResponseResult businessManageByOrderDetails(Map<String, Object> map) {
        // 初始化返回结果对象
        ResponseResult rr = new ResponseResult();
        // 从map中获取token
        String token = (String) map.get("token");
        // 检查token是否存在
        boolean isExist = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (isExist) {
            // 通过token获取平台用户信息
            UserInfoVO info = ParseTokenUtils.getPlatformUser(token);
            // 通过token获取店铺用户信息
            ResponseResult res = ParseTokenUtils.getShopUserInfo(token);
            // 如果获取店铺用户信息失败，则直接返回失败结果
            if (res.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
                return res;
            }
            // 从map中获取订单号
            String orderNum = (String) map.get("orderNum");
            // 根据订单号查询订单详情
            MerchantsOrdersDetailsVO mod = orderBaseMapper.queryByOrderDetails(orderNum);
            // 如果订单存在物流信息，则查询物流信息
            if (mod.getOrderShippingExpressId() != null && mod.getOrderShippingExpressId() != 0) {
                mod.setOrderShippingExpress(baseServiceClient.queryBaseExpressById(mod.getOrderShippingExpressId()));
            }
            // 处理订单的退货状态、正常状态、赠品信息、支付方式等
            // 退货状态
            Integer orderReturnStatus = mod.getOrderReturnStatus();
            // 订单状态
            Integer normalStatus = mod.getOrderStatus();
            //满减赠赠品信息
            String giveawayInfo = mod.getGiveawayInfo();
            //支付方式
            Integer paymentCode = mod.getPaymentCode();
            if (paymentCode != null) {
                // 根据支付方式设置支付名称
                if (paymentCode == 1) {
                    mod.setPaymentName("线上支付");
                } else if (paymentCode == 2) {
                    mod.setPaymentName("线下支付");
                } else if (paymentCode == 3) {
                    mod.setPaymentName("货到付款");
                }
            }
            // 解析赠品信息
            List<ManJianZengPinVO> list;
            if (null != giveawayInfo && !giveawayInfo.isEmpty()) {
                list = JsonUtils.string2Obj(giveawayInfo, List.class, ManJianZengPinVO.class);
                mod.setZengpinInfo(list);
            }
            // 处理涉及退款退货的订单状态
            Integer Orderstatus = null;
            Integer orderRefundStatus = null;
            // 退款状态
            orderRefundStatus = mod.getOrderRefundStatus();
            // 根据订单的正常状态和退款退货状态，确定订单的最终状态
            if (normalStatus == 3) {
                // 未发货状态
                if (orderRefundStatus != null && orderRefundStatus == 1) {
                    // 未发货退款中
                    Orderstatus = 22;
                } else if (orderRefundStatus != null && orderRefundStatus == 2) {
                    // 商家拒绝或审核通过，继续发货，但不可再次申请退款
                    Orderstatus = 26;
                } else if (orderRefundStatus != null && orderRefundStatus == 0) {
                    Orderstatus = 3;
                }
            } else if (normalStatus == 6) {
                // 已完成状态
                if (orderRefundStatus != null && orderRefundStatus == 1) {
                    // 收货退款中
                    Orderstatus = 23;
                } else if (orderRefundStatus != null && orderRefundStatus == 2) {
                    Orderstatus = 25;
                } else if (orderReturnStatus == 0 && orderReturnStatus == 0) {
                    Orderstatus = 6;
                } else if (orderReturnStatus != null && orderReturnStatus == 1) {
                    // 收货退货中
                    Orderstatus = 24;
                } else if (orderReturnStatus != null && orderReturnStatus == 2) {
                    Orderstatus = 25;
                } else if (orderRefundStatus != null && orderReturnStatus == 0 && orderRefundStatus == 0) {
                    Orderstatus = 6;
                }
            } else {
                Orderstatus = normalStatus;
            }
            // 更新订单状态
            // 存放最新状态
            mod.setOrderStatus(Orderstatus);
            // 查询新订单详情
            List<AddOrderDetailsVO> aod = orderBaseMapper.queryNewOrderDetails(orderNum, info.getShopId());
            // 查询商品分类的抽佣比例
            //查询商品分类的抽佣比例
            if (null != aod && !aod.isEmpty()) {
                for (AddOrderDetailsVO addOrderDetails : aod) {
                    BigDecimal bili = orderBaseMapper.queryCatChouYong(addOrderDetails.getGoodsId());
                    addOrderDetails.setChouYongBili(null == bili ? new BigDecimal(0) : bili);
                }
            }
            // 设置发票状态
            mod.setInvoiceState(StrUtil.isEmptyIfStr(mod.getOrderInvoiceId()) ? 1 : 4);
            // 查询退货订单详情
            List<ReturnOrderDetailsVO> rod = orderBaseMapper.queryReturnOrderDetails(orderNum);
            // 设置订单详情和退货订单详情
            mod.setAddOrderDetails(aod);
            mod.setReturnOrderDetails(rod);
            // 返回处理成功的结果
            return ResponseResult.success(mod);
        } else {
            // 如果token无效，返回失败结果
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
    }

    /**
     * 根据订单详情进行业务管理。
     * 该方法主要用于根据传入的订单号查询订单详情，并根据订单状态和退款退货状态进行相应的逻辑处理，
     * 包括订单支付方式的名称设置、赠品信息的解析、订单状态的更新等。
     *
     * @param map 包含订单相关信息的Map对象，其中包含token、orderNum等关键信息。
     * @return 返回处理结果的ResponseResult对象。
     */
    @Override
    public ResponseResult queryPrintOrderDetails(Map<String, Object> map) {
        // 从map中获取token
        String token = (String) map.get("token");
        // 检查token是否存在
        boolean isExist = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (isExist) {
            // 通过token获取平台用户信息
            UserInfoVO info = ParseTokenUtils.getPlatformUser(token);
            // 通过token获取店铺用户信息
            ResponseResult res = ParseTokenUtils.getShopUserInfo(token);
            // 如果获取店铺用户信息失败，则直接返回失败结果
            if (res.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
                return res;
            }
            // 从map中获取订单号
            String orderNum = (String) map.get("orderNum");
            // 根据订单号查询订单详情
            MerchantsOrdersDetailsVO mod = orderBaseMapper.queryByOrderDetails(orderNum);
            // 如果订单存在物流信息，则查询物流信息
            if (mod.getOrderShippingExpressId() != null && mod.getOrderShippingExpressId() != 0) {
                mod.setOrderShippingExpress(baseServiceClient.queryBaseExpressById(mod.getOrderShippingExpressId()));
            }
            //支付方式
            Integer paymentCode = mod.getPaymentCode();
            if (paymentCode != null) {
                // 根据支付方式设置支付名称
                if (paymentCode == 1) {
                    mod.setPaymentName("线上支付");
                } else if (paymentCode == 2) {
                    mod.setPaymentName("线下支付");
                } else if (paymentCode == 3) {
                    mod.setPaymentName("货到付款");
                }
            }

            // 查询新订单详情
            List<AddOrderDetailsVO> aod = orderBaseMapper.queryNewOrderDetails(orderNum, info.getShopId());
            // 设置订单详情和退货订单详情
            mod.setAddOrderDetails(aod);
            // 查询店铺印章
            Map<String, Object> shopMap = new HashMap<>();
            shopMap.put("shopId", info.getShopId());
            ResponseResult responseResult = merchantServiceClient.queryShopStampByShopId(shopMap);
            if (null != responseResult && responseResult.getCode() == CommonStatusEnum.SUCCESS.getCode()) {
                ShopStampVO shopStampVO = JSONObject.parseObject(JSONObject.toJSONString(responseResult.getData()), ShopStampVO.class);
                if (null != shopStampVO) {
                    mod.setShopStamp(shopStampVO.getShopStamp());
                    mod.setShopStampMsg(shopStampVO.getShopStampMsg());
                }
            }
            // 返回处理成功的结果
            return ResponseResult.success(mod);
        } else {
            // 如果token无效，返回失败结果
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
    }

    /**
     * 根据红包ID获取红包订单信息。
     *
     * @param paraMap 包含请求参数的映射表，其中必须包含红包ID（redpacketId）。
     * @return 返回包含红包订单信息的ResponseResult对象。
     * 如果查询成功，ResponseResult中的data字段将包含红包订单列表；
     * 如果查询失败或参数不合法，将返回相应的错误信息。
     */
    @Override
    public ResponseResult getRedPacketOrder(Map<String, String> paraMap) {
        // 使用LambdaQueryWrapper查询符合条件的红包订单。
        // 查询条件为：红包代码等于paraMap中get("redpacketId")的值。
        List<OrderBaseEntity> list = this.baseMapper.selectList(new LambdaQueryWrapper<OrderBaseEntity>()
                .eq(OrderBaseEntity::getRedpacketCode, paraMap.get("redpacketId").toString()));

        // 返回查询结果，将订单列表封装在ResponseResult对象中。
        // 如果查询成功，list将被包含在ResponseResult的data字段中。
        return ResponseResult.success(list);
    }

    @Override
    public ResponseResult queryPlatFormMemberSelfPard(PlatFormMemberSelfQueryDTO param) throws IllegalAccessException {
        Map<String, Object> result = new HashMap<>();
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        if (null != userInfo) {
            if (param.getOrderStatus().equals("1")) {
                // 全部订单
                param.setOrderStatus("");
            } else if (param.getOrderStatus().equals("2")) {
                // 待付款订单
                param.setOrderStatus("1");
            } else if (param.getOrderStatus().equals("3")) {
                // 已付款待发货订单
                param.setOrderStatus("3");
            } else if (param.getOrderStatus().equals("4")) {
                // 已发货订单
                param.setOrderStatus("4");
            } else if (param.getOrderStatus().equals("5")) {
                // 已完成订单
                param.setOrderStatus("6");
            } else if (param.getOrderStatus().equals("6")) {
                // 已取消订单
                param.setOrderStatus("7");
            } else if (param.getOrderStatus().equals("7")) {
                param.setOrderStatus("0");
            }
            if (param.getOrderId() == "") {
                if (param.getAirTime() == "" && param.getFinishTime() == "" && param.getBuyerUserName() == "") {
                    System.out.println(1);
                    SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String finishTime = dateformat.format(new Date());
                    Calendar c = Calendar.getInstance();
                    // 过去三个月
                    c.setTime(new Date());
                    c.add(Calendar.MONTH, -1);
                    Date m3 = c.getTime();
                    String startTime = dateformat.format(m3);
                    param.setAirTime(startTime);
                    param.setFinishTime(finishTime);

                }

            }
            Map data = ParseMapUtils.beanToMap(param);
            IPage<PlatFormMemberSelfQueryVO> page = this.baseMapper.queryPlatFormMemberSelfPard(new Query<PlatFormMemberSelfQueryVO>().getPage(data),
                    param);
            String totMoney = this.baseMapper.queryMemberSelfMoney(param);
            if (totMoney == null) {
                totMoney = "0";
            }
            result.put("totalAmount", totMoney);
            for (PlatFormMemberSelfQueryVO he : page.getRecords()) {
                if (he.getOrderRefundStatus() != null || he.getOrderReturnStatus() != null) {
                    if ((he.getOrderRefundStatus() + "状态").equals("0状态")
                            && (he.getOrderReturnStatus() + "状态").equals("0状态")) {
                        // 1无退款退货
                        he.setIsNotreFundReturn("1");

                    } else {
                        // 2有退款退货
                        he.setIsNotreFundReturn("2");
                    }
                } else {
                    // 1无退款退货
                    he.setIsNotreFundReturn("1");

                }
            }
            result.put("page", PageUtils.getPage(page));
        } else {
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
        return ResponseResult.success(result);
    }

    /**
     * 处理发货相关业务逻辑。
     *
     * @param map 包含请求参数的映射，其中关键参数包括token用于用户身份验证，
     *            orderShippingCode用于指定订单的发货编码。
     * @return 返回处理结果，如果验证失败或发货编码为空，则返回相应的错误信息，
     * 否则调用piLiangFahuoShipments方法进行具体的发货处理。
     * @throws TransactionException 如果业务操作过程中发生事务异常，则抛出此异常。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult shipments(Map<Object, Object> map) throws TransactionException {
        // 从请求参数映射中提取token字符串
        String token = map.get("token").toString();
        // 使用token解析用户信息，用于身份验证
        UserInfoVO rr = ParseTokenUtils.getPlatformUser(token);
        // 如果解析失败，即token无效，返回相应的错误结果
        if (rr == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }

        // 如果验证通过，调用具体发货方法进行处理，这里传入的commonStatus.NO.getCode()可能表示未发货的状态
        return piLiangFahuoShipments(map, CommonType.commonStatus.NO.getCode());
    }

    /**
     * 删除积分订单
     *
     * @param map 包含令牌和订单ID的映射表
     * @return 删除操作的结果
     * 如果令牌有效且订单存在，则删除订单并返回成功响应
     * 如果令牌无效，则返回令牌非法的错误响应
     */
    @Override
    public ResponseResult deleteIntegralOrder(Map<Object, Object> map) {
        // 通过令牌获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(map.get("token").toString());

        // 检查用户信息是否存在
        if (null != userInfo) {
            // 使用LambdaQueryWrapper构建查询条件，根据订单ID和积分商品状态删除订单
            this.baseMapper.delete(new LambdaQueryWrapper<OrderBaseEntity>()
                    .eq(OrderBaseEntity::getOrderId, map.get("orderId").toString())
                    .eq(OrderBaseEntity::getIntegralGoods, CommonType.commonStatus.NO.getCode()));

            // 返回操作成功的响应
            return ResponseResult.success();
        }

        // 如果令牌无效，返回错误响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 查询月销售报告
     * 根据传入的MonthReportDTO参数，查询相应的月销售报告，并对查询结果进行处理和封装。
     * 如果参数不完整，会根据当前时间设置查询的时间范围。
     * 根据订单的不同属性，对查询结果中的每个订单记录进行分类和标记。
     *
     * @param param 月销售报告查询参数，包含订单号、支付时间范围等信息。
     * @return 返回处理后的月销售报告结果。
     */
    @Override
    public ResponseResult queryMonthsaleReport(MonthReportDTO param) {
        // 如果参数为空，则创建一个新的MonthReportDTO对象
        if (null == param) {
            param = new MonthReportDTO();
        }
        // 如果没有指定订单号，但指定了支付时间范围，则根据当前时间设置查询的时间范围
        if (param.getOrderId() == "") {
            if (param.getPaymentTimeStart() == "" && param.getPaymentTimeEnd() == "") {
                // 格式化当前时间
                SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String finishTime = dateformat.format(new Date());
                // 设置查询的开始时间为当前时间的前三个月
                Calendar c = Calendar.getInstance();
                c.setTime(new Date());
                c.add(Calendar.MONTH, -1);
                Date m3 = c.getTime();
                String startTime = dateformat.format(m3);

                param.setOrderCreateTimeEnd(finishTime);
                param.setOrderCreateTimeStart(startTime);
            }
        }
        // 根据token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        // 如果用户信息有效
        if (null != userInfo) {
            try {
                // 将查询参数转换为Map格式
                //将对象转换为map
                Map data = ParseMapUtils.beanToMap(param);
                // 执行查询操作
                //分页查询数据
                IPage<MonthReportSalesVO> page = this.baseMapper.queryMonthsaleReport(new Query<MonthReportSalesVO>().getPage(data), param);
                List<MonthReportSalesVO> records = page.getRecords();
                // 初始化标记，用于标识是否有退货或退款的订单
                Integer returnFlag = 0;
                // 遍历查询结果，对每个订单进行处理
                for (MonthReportSalesVO record : records) {
                    // 根据订单号查询订单详细信息
                    OrderBaseEntity orderInfoEntity = this.baseMapper.selectById(record.getOrderId());
                    // 根据订单的不同属性，设置订单类型
                    Integer virtualGoods = orderInfoEntity.getVirtualGoods();
                    Integer orderIsShopTake = orderInfoEntity.getOrderIsShopTake();
                    if (null != orderIsShopTake && 1 == orderIsShopTake) {
                        record.setOrderType(3);
                    } else {
                        if (null != virtualGoods && 0 == virtualGoods) {
                            record.setOrderType(2);
                            record.setPastDue(orderInfoEntity.getPastDue());
                            record.setIsCancel(orderInfoEntity.getIsCancel());
                        } else {
                            record.setOrderType(1);
                        }
                    }
                    // 设置退货和退款状态
                    String returnStatus = orderInfoEntity.getOrderReturnStatus();
                    String refundStatus = orderInfoEntity.getOrderRefundStatus();
                    record.setReturnStatus(returnStatus);
                    record.setRefundStatus(refundStatus);
                    // 根据退货和退款金额，设置订单的退货标记
                    if (returnStatus.equals("2") || refundStatus.equals("2")) {
                        BigDecimal returnAmount = BigDecimal.valueOf(orderInfoEntity.getOrderRefundAmount());
                        BigDecimal orderAmount = orderInfoEntity.getOrderPaymentAmount();
                        if (orderAmount.compareTo(returnAmount) > 0) {
                            returnFlag = 2;
                        } else if ((orderAmount.compareTo(returnAmount) == 0)) {
                            returnFlag = 1;
                        }
                    }
                    record.setReturnFlag(returnFlag);
                    // 查询订单的商品详情，并设置到订单记录中
                    List<GoodsDetailsVO> parm = this.baseMapper.queryGoodsDetail(record.getOrderId());
                    record.setGoods(parm);
                    // 根据支付方式，设置订单的支付类型
                    if (null != record.getPayCode() && "6".equals(record.getPayCode())) {
                        //购物卡支付
                        record.setTradePaymentRechargeCard(record.getOrderPaymentAmount());
                    } else if ((null != record.getPayCode())
                            && ("3".equals(record.getPayCode()) || "2".equals(record.getPayCode()))) {
                        //第三方支付
                        record.setTradeThirdPartyAmount(record.getOrderPaymentAmount());
                    }
                    // 如果支付方式为支付宝或微信支付，设置支付代码为15
                    if (null != orderInfoEntity.getPaymentCode() && orderInfoEntity.getPaymentCode() == 3) {
                        record.setPayCode("15");
                    }
                }
                // 返回处理后的查询结果
                return ResponseResult.success(PageUtils.getPage(page));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        // 如果用户信息无效，返回失败结果
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    @Override
    public ResponseResult queryMonthsaleReportExcel(MonthReportDTO param) {
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (null != userInfo) {
            List<MonthReportSalesVO> page = this.baseMapper.queryMonthsaleReportExcel(param);
            String[] title = {"订单号", "支付时间", "卖家店铺名称", "运费", "红包面额", "应付金额", "订单状态", "支付方式", "买家名称", "订单完成时间", "支付单号",
                    "第三方支付平台交易号", "订单物流", "商品物流", "订单来源", "商品名", "商品价格", "商品数量", "退货数量", "渠道号", "充值卡余额支付", "第三方支付金额"};

            List<Map<String, String>> list = Lists.newArrayList();

            for (MonthReportSalesVO res : page) {
                HashMap<String, String> map = com.google.common.collect.Maps.newHashMap();
                // 订单状态 1.待付款 2.已付款 3.待发货 4.已发货 5.已签收 6.确认收货 7.取消 8.退款中 9.已退款 11.待自提',
                if (res.getOrderStatus() != null) {

                    switch (res.getOrderStatus()) {
                        case "1":
                            res.setOrderStatus("待付款 ");
                            break;
                        case "2":
                            res.setOrderStatus("已付款");
                            break;
                        case "3":
                            res.setOrderStatus("待发货  ");
                            break;
                        case "4":
                            res.setOrderStatus("已发货 ");
                            break;
                        case "5":
                            res.setOrderStatus("已签收 ");
                            break;
                        case "6":
                            res.setOrderStatus("确认收货");
                            break;
                        case "7":
                            res.setOrderStatus("取消 ");
                            break;
                        case "8":
                            res.setOrderStatus("退款中");
                            break;
                        case "9":
                            res.setOrderStatus("已退款 ");
                            break;
                        case "11":
                            res.setOrderStatus("待自提");
                            break;
                        case "12":
                            res.setOrderStatus("已过期");
                            break;
                    }
                }
                if (res.getPayCode() != null) {

                    switch (res.getPayCode()) {
                        // 支付方式1-余额支付，2-支付宝支付，3-微信支付，6-购物卡支付，12 线下支付 16-paypal支付 17-0元兑换券 18 Stripe 支付
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
                            res.setPayCode("充值卡余额支付 ");
                            break;
                        case "12":
                            res.setPayCode("线下支付");
                            break;
                        case "16":
                            res.setPayCode("Paypal支付");
                            break;
                        case "17":
                            res.setPayCode("0元兑换券");
                            break;
                        case "18":
                            res.setPayCode("Stripe 支付");
                            break;
                    }
                }

                if (res.getOrderFrom() != null) {

                    switch (res.getOrderFrom()) {
                        case "1":
                            res.setOrderFrom("pc");
                            break;
                        case "2":
                            res.setOrderFrom("wap");
                            break;
                        case "3":
                            res.setOrderFrom("安卓");
                            break;
                        case "5":
                            res.setOrderFrom("iOS");
                            break;
                        case "6":
                            res.setOrderFrom("小程序");
                            break;
                    }

                }
                map.put("订单号", res.getOrderId() + "");
                map.put("支付时间", res.getPaymentTime() == null ? "" : format.format(res.getPaymentTime()));
                map.put("卖家店铺名称", res.getShopName() + "");
                map.put("运费", res.getOrderShippingFee() + "");
                map.put("红包面额", res.getRedpacketPrice() + "");
                map.put("应付金额", res.getOrderPaymentAmount() + "");
                map.put("订单状态", res.getOrderStatus() + "");
                map.put("支付方式", res.getPayCode() + "");
                map.put("买家名称", res.getBuyerUserName() + "");
                map.put("订单完成时间", res.getOrderFinishedTime() == null ? "" : format.format(res.getOrderFinishedTime()));
                map.put("支付单号", res.getPaymentNumber() + "");
                map.put("第三方支付平台交易号", res.getPaymentOtherNumber() == null ? "" : String.valueOf(res.getPaymentOtherNumber()));
                map.put("订单物流", res.getOrderShippingCode() == null ? "" : String.valueOf(res.getOrderShippingCode()));
                map.put("订单来源", res.getOrderFrom() + "");
                map.put("商品名", res.getGoodsName() == null ? "" : String.valueOf(res.getGoodsName()));
                map.put("商品价格", res.getGoodsPrice() == null ? "" : String.valueOf(res.getGoodsPrice()));
                map.put("商品数量", res.getOrderGoodsNum() == null ? "" : String.valueOf(res.getOrderGoodsNum()));
                map.put("退货数量", res.getOrderGoodsReturnnum() == null ? "" : String.valueOf(res.getOrderGoodsReturnnum()));
                map.put("充值卡余额支付", res.getTradePaymentRechargeCard() + "");
                map.put("第三方支付金额", res.getTradeThirdPartyAmount() + "");
                list.add(map);
            }
            // excel文件名
            Map<String, List<Map<String, String>>> map = Maps
                    .newHashMap();
            map.put("交易明细", list);
            String url = ExcelsUtil.createExcel(title, map,
                    new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14});

            return ResponseResult.success(url);
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    @Override
    public ResponseResult queryRealOrderSelfPardExcel(RealOrderSelfDTO param) {
        if (null == param) {
            param = new RealOrderSelfDTO();
        }
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());

        if (null == userInfo || userInfo.equals("")) {
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String finishTime = dateformat.format(new Date());
        if (param.getOrderId() == "") {
            if (param.getAirTime() == "" && param.getFinishTime() == "" && param.getBuyerUserName() == "") {
                Calendar c = Calendar.getInstance();
                // 过去三个月
                c.setTime(new Date());
                c.add(Calendar.MONTH, -3);
                Date m3 = c.getTime();
                String startTime = dateformat.format(m3);
                param.setStartTime(startTime);
                param.setEndTime(finishTime);

            }
        }
        if (param.getOrderStatus().equals("1")) {
            // 全部订单
            param.setOrderStatus("");
        } else if (param.getOrderStatus().equals("2")) {
            // 待付款订单
            param.setOrderStatus("1");
        } else if (param.getOrderStatus().equals("3")) {
            // 已付款待发货订单
            param.setOrderStatus("3");
            param.setOrderReturnStatus("0");
            param.setOrderRefundStatus("0");
        } else if (param.getOrderStatus().equals("4")) {
            // 已发货订单
            param.setOrderStatus("4");
            param.setOrderReturnStatus("0");
            param.setOrderRefundStatus("0");
        } else if (param.getOrderStatus().equals("5")) {
            // 已完成订单
            param.setOrderStatus("6");
            param.setOrderReturnStatus("0");
            param.setOrderRefundStatus("0");
        } else if (param.getOrderStatus().equals("6")) {
            // 已取消订单
            param.setOrderStatus("7");
        } else if (param.getOrderStatus().equals("7")) {
            param.setOrderStatus("");
            param.setOrderReturnStatus("1");
        } else if (param.getOrderStatus().equals("8")) {
            param.setOrderStatus("");
            param.setOrderRefundStatus("1");
        } else if (param.getOrderStatus().equals("9")) {
            param.setOrderStatus("");
            param.setOrderRefundStatus("2");
        } else if (param.getOrderStatus().equals("10")) {
            param.setOrderStatus("");
            param.setOrderReturnStatus("2");
        }
        List<PlatFormMemberSelfQueryVO> page = this.baseMapper.queryRealOrderSelfPardExccel(param);
        if (page.size() > 30000) {
            //导出数量过大，请联系运维
            return ResponseResult.fail(BizCodeEnume.EXPORT_QUANTITY_IS_TOO_LARGE);
        }
        String[] headers = {"订单编号", "订单来源", "收货人名称", "下单时间", "商品金额", "支付金额",
                "代金券", "运费", "订单状态", "活动ID", "退款状态", "退货状态",
                "是否存在退货退款", "支付单号", "支付方式名称", "支付时间", "发货物流单号", "退款金额", "订单完成时间",
                "是否评价", "店铺id", "店铺名", "买家id", "买家名", "充值卡余额支付", "第三方支付金额", "支付方式"};
        List<Map<String, String>> list = Lists.newArrayList();
        String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.DESENSITIZATION_SWITCH.getDesc());
        for (PlatFormMemberSelfQueryVO res : page) {
            if (StrUtil.isNotEmpty(value) && value.equals(CommonType.commonStatus.YES.getCode().toString())) {
                BeanUtils.copyProperties(DesensitizationUtil.Desensitization(res, PlatFormMemberSelfQueryVO.class), res);
            }
            HashMap<String, String> map = Maps.newHashMap();
            if (res.getOrderStatus() != null) {
                switch (res.getOrderStatus()) {
                    case "1":
                        res.setOrderStatus("待付款 ");
                        break;
                    case "2":
                        res.setOrderStatus("已付款");
                        break;
                    case "3":
                        res.setOrderStatus("待发货  ");
                        break;
                    case "4":
                        res.setOrderStatus("已发货 ");
                        break;
                    case "5":
                        res.setOrderStatus("已签收 ");
                        break;
                    case "6":
                        res.setOrderStatus("确认收货");
                        break;
                    case "7":
                        res.setOrderStatus("取消 ");
                        break;
                    case "8":
                        res.setOrderStatus("退款中");
                        break;
                    case "9":
                        res.setOrderStatus("已退款 ");
                        break;
                    case "11":
                        res.setOrderStatus("待自提");
                        break;

                }
            }
            if (res.getOrderFrom() != null) {

                switch (res.getOrderFrom()) {
                    //1PC 2wap微信浏览器 3安卓 5ios 6微信小程序 7wap浏览器
                    case "1":
                        res.setOrderFrom("pc");
                        break;
                    case "2":
                        res.setOrderFrom("wap微信浏览器");
                        break;
                    case "3":
                        res.setOrderFrom("安卓");
                        break;
                    case "5":
                        res.setOrderFrom("ios");
                        break;
                    case "6":
                        res.setOrderFrom("微信小程序");
                        break;
                    case "7":
                        res.setOrderFrom("wap浏览器");
                        break;
                }
            }
            if (null != res.getOrderReturnStatus() && res.getOrderRefundStatus() != null) {
                if ("0".equals(res.getOrderReturnStatus()) && "0".equals(res.getOrderRefundStatus())) {
                    res.setIsNotreFundReturn("否");
                } else {
                    res.setIsNotreFundReturn("是");
                }
            } else {
                res.setIsNotreFundReturn("否");
            }
            if (res.getOrderRefundStatus() != null) {

                switch (res.getOrderRefundStatus()) {
                    // 0是无退款,1是退款中,2是退款完成',
                    case "0":
                        res.setOrderRefundStatus("无退款");
                        break;
                    case "1":
                        res.setOrderRefundStatus("退款中 ");
                        res.setOrderStatus("退款中 ");
                        break;
                    case "2":
                        res.setOrderRefundStatus("退款完成");
                        res.setOrderStatus("退款完成 ");
                        break;

                }
            }
            if (res.getOrderReturnStatus() != null) {
                switch (res.getOrderReturnStatus()) {
                    // 0是无退款,1是退款中,2是退款完成',
                    case "0":
                        res.setOrderReturnStatus("无退货");
                        break;
                    case "1":
                        res.setOrderReturnStatus("退货中 ");
                        res.setOrderStatus("退货中 ");
                        break;
                    case "2":
                        res.setOrderReturnStatus("退货完成");
                        res.setOrderStatus("退货完成 ");
                        break;

                }
            }

            if (res.getPayCode() != null) {

                switch (res.getPayCode()) {
                    // 支付方式1-余额支付，2-支付宝支付，3-微信支付，6-购物卡支付，12 线下支付 16-paypal支付 17-0元兑换券 18 Stripe 支付
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
                        res.setPayCode("充值卡余额支付 ");
                        break;
                    case "12":
                        res.setPayCode("线下支付");
                        break;
                    case "16":
                        res.setPayCode("Paypal支付");
                        break;
                    case "17":
                        res.setPayCode("0元兑换券");
                        break;
                    case "18":
                        res.setPayCode("Stripe 支付");
                        break;
                }
            }
            // 1.线上支付  2.线下支付 3.货到付款
            if (CommonType.payType.GOODS_ARRIVED_PAY.getCode().equals(res.getPaymentCode())) {
                res.setPayCode(CommonType.payType.GOODS_ARRIVED_PAY.getDesc());
            }
            if (res.getOrderBuyerEvaluationStatus() != null) {
                switch (res.getOrderBuyerEvaluationStatus()) {
                    case "0":
                        res.setOrderBuyerEvaluationStatus("未评价");
                        break;
                    case "1":
                        res.setOrderBuyerEvaluationStatus("已评价 ");
                        break;

                }
            }
            map.put("订单编号", res.getOrderId());
            map.put("订单来源", res.getOrderFrom());
            map.put("收货人名称", res.getOrderReceiverName());
            map.put("下单时间", res.getOrderCreateTime().toString());
            map.put("商品金额", res.getOrderGoodsAmount().toString());
            map.put("支付金额", res.getOrderPaymentAmount().toString());
            map.put("代金券", res.getVoucherPrice().toString());
            map.put("运费", res.getOrderShippingFee().toString());
            map.put("订单状态", res.getOrderStatus());
            map.put("活动ID", res.getOrderPromotionId().toString());
            map.put("退款状态", res.getOrderRefundStatus());
            map.put("退货状态", res.getOrderReturnStatus());
            map.put("是否存在退货退款", res.getIsNotreFundReturn());
            map.put("支付单号", res.getPaymentNumber());
            map.put("支付方式名称", res.getPaymentName());
            map.put("支付时间", res.getPaymentTime() != null ? res.getPaymentTime().toString() : "");
            map.put("发货物流单号", res.getOrderShippingCode());
            map.put("退款金额", res.getOrderRefundAmount() != null ? res.getOrderRefundAmount().toString() : "0");
            map.put("订单完成时间", res.getOrderFinishedTime() != null ? res.getOrderFinishedTime().toString() : "");
            map.put("是否评价", res.getOrderBuyerEvaluationStatus());
            map.put("店铺id", res.getShopId().toString());
            map.put("店铺名", res.getShopName());
            map.put("买家id", res.getBuyerUserId());
            map.put("买家名", res.getBuyerUserName());
            map.put("充值卡余额支付", res.getTradePaymentRechargeCard() != null ? res.getTradePaymentRechargeCard().toString() : "0");
            map.put("第三方支付金额", res.getTradeThirdPartyAmount() != null ? res.getTradeThirdPartyAmount().toString() : "0");
            map.put("支付方式", res.getPayCode());

            list.add(map);
        }

        // excel文件名
        Map<String, List<Map<String, String>>> map = Maps.newHashMap();
        map.put("合并数据", list);
        String url = ExcelsUtil.createExcel(headers, map, new int[]{0});
        return ResponseResult.success(url);
    }

    /**
     * 根据条件查询用户的实际订单信息。
     *
     * @param param 查询条件对象，包含订单状态等过滤条件。
     * @return 返回查询结果的对象，包含分页信息和订单列表。
     * @throws IllegalAccessException 当访问字段失败时抛出此异常。
     */
    @Override
    public ResponseResult queryRealOrderSelfPard(RealOrderSelfDTO param) throws IllegalAccessException {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        // 如果用户信息为空，返回令牌无效的响应
        if (userInfo == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 根据传入的订单状态进行逻辑处理，修正查询条件
        if (param.getOrderStatus().equals("1")) {
            // 全部订单
            param.setOrderStatus("");
        } else if (param.getOrderStatus().equals("2")) {
            // 待付款订单
            param.setOrderStatus("1");
        } else if (param.getOrderStatus().equals("3")) {
            // 已付款待发货订单
            param.setOrderStatus("3");
            param.setOrderReturnStatus("0");
            param.setOrderRefundStatus("0");
        } else if (param.getOrderStatus().equals("4")) {
            // 已发货订单
            param.setOrderStatus("4");
            param.setOrderReturnStatus("0");
            param.setOrderRefundStatus("0");
        } else if (param.getOrderStatus().equals("5")) {
            // 已完成订单
            param.setOrderStatus("6");
            param.setOrderReturnStatus("0");
            param.setOrderRefundStatus("0");
        } else if (param.getOrderStatus().equals("6")) {
            // 已取消订单
            param.setOrderStatus("7");
        } else if (param.getOrderStatus().equals("7")) {
            param.setOrderStatus("");
            param.setOrderReturnStatus("1");
        } else if (param.getOrderStatus().equals("8")) {
            param.setOrderStatus("");
            param.setOrderRefundStatus("1");
        } else if (param.getOrderStatus().equals("9")) {
            param.setOrderStatus("");
            param.setOrderRefundStatus("2");
        } else if (param.getOrderStatus().equals("10")) {
            param.setOrderStatus("");
            param.setOrderReturnStatus("2");
        }

        Integer returnFlag = 0;
        Map data = ParseMapUtils.beanToMap(param);
        IPage<PlatFormMemberSelfQueryVO> page = this.baseMapper.queryRealOrderSelfPard(new Query<PlatFormMemberSelfQueryVO>().getPage(data), param);
        String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.DESENSITIZATION_SWITCH.getDesc());
        for (PlatFormMemberSelfQueryVO r : page.getRecords()) {
            // 根据配置，是否进行数据脱敏处理
            if (StrUtil.isNotEmpty(value) && value.equals(CommonType.commonStatus.YES.getCode().toString())) {
                BeanUtils.copyProperties(DesensitizationUtil.Desensitization(r, PlatFormMemberSelfQueryVO.class), r);
            }
            // 处理订单的额外信息，如订单类型、退款状态等
            OrderBaseEntity orderInfoEntity = this.baseMapper.selectById(r.getOrderId());
            Integer virtualGoods = orderInfoEntity.getVirtualGoods();
            Integer orderIsShopTake = orderInfoEntity.getOrderIsShopTake();
            if (null != orderIsShopTake && 1 == orderIsShopTake) {
                r.setOrderType(3);
            } else {
                if (null != virtualGoods && 0 == virtualGoods) {
                    r.setOrderType(2);
                    r.setPastDue(orderInfoEntity.getPastDue());
                    r.setIsCancel(orderInfoEntity.getIsCancel());
                } else {
                    r.setOrderType(1);
                }
            }
            String returnStatus = orderInfoEntity.getOrderReturnStatus();
            String refundStatus = orderInfoEntity.getOrderRefundStatus();
            r.setReturnStatus(Integer.valueOf(returnStatus));
            r.setRefundStatus(Integer.valueOf(refundStatus));
            // 根据退款状态计算订单的退款标志
            if (returnStatus.equals("2") || refundStatus.equals("2")) {
                BigDecimal returnAmount = new BigDecimal(orderInfoEntity.getOrderRefundAmount());
                BigDecimal orderAmount = orderInfoEntity.getOrderPaymentAmount();
                if (orderAmount.compareTo(returnAmount) > 0) {
                    returnFlag = 2;
                } else if ((orderAmount.compareTo(returnAmount) == 0)) {
                    returnFlag = 1;
                }
            }
            r.setReturnFlag(returnFlag);
            // 根据支付方式设置订单的支付金额
            if (null != r.getPayCode() && "1".equals(r.getPayCode())) {
                //余额支付
                r.setTradePaymentMoney(r.getOrderPaymentAmount());
            } else if (null != r.getPayCode() && "6".equals(r.getPayCode())) {
                //购物卡支付
                r.setTradePaymentRechargeCard(r.getOrderPaymentAmount());
            } else if ((null != r.getPayCode()) && ("3".equals(r.getPayCode()) || "2".equals(r.getPayCode()))) {
                //第三方支付
                r.setTradeThirdPartyAmount(r.getOrderPaymentAmount());
            }
            r.setOrderAmountNoShippingFee(new BigDecimal(r.getOrderPaymentAmount()).subtract(new BigDecimal(r.getOrderShippingFee())).setScale(2, RoundingMode.HALF_UP));
        }
        // 返回处理后的分页查询结果
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 根据Map更新订单基础信息。
     * 使用事务确保操作的原子性。
     *
     * @param map 包含更新信息的Map，键值对表示要更新的字段及其新值。
     * @return 如果更新成功，返回成功的响应结果；如果更新失败，返回失败的响应结果。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult updateByMap(Map<String, Object> map) {
        // 执行更新操作
        Integer count = this.baseMapper.updateOrderBaseByMap(map);
        // 如果更新的行数大于0，视为更新成功
        if (count > 0) {
            return ResponseResult.success(BizCodeEnume.UPDATE_SUCCEEDED);
        }
        // 更新失败，返回失败的响应结果
        return ResponseResult.fail(BizCodeEnume.UPDATE_ERROR);
    }

    /**
     * 根据Map查询返现债券详情。
     *
     * @param map 包含查询条件的Map，至少包含shopId用于查询。
     * @return 返回包含查询结果的响应结果，其中包含未下架商品的数量和其他相关信息。
     */
    @Override
    public ResponseResult queryReturnBondDetail(Map<String, Object> map) {
        // 从map中提取shopId
        Integer shopId = Integer.valueOf(map.get("shopId").toString());
        // 查询该店铺未下架商品的数量
        // 未下架数量
        Integer onshelves = this.baseMapper.selectGoodsStatus(shopId);
        // 将未下架商品数量添加到查询条件的map中
        map.put("onshelves", onshelves);
        // 设置未发货和未结算的商品信息到map中，用于进一步查询和展示
        // 未发货/未结算
        map = this.baseMapper.setNoSendGoods(shopId);

        // 返回包含查询结果的响应结果
        return ResponseResult.success(map);
    }

    @Override
    public ResponseResult updateShopByOrderInfo(Map<String, Object> map) {
        String shopId = map.get("shopId").toString();
        List<OrderBaseEntity> orderBaseEntityList = this.baseMapper.selectList(new LambdaQueryWrapper<OrderBaseEntity>()
                .eq(OrderBaseEntity::getShopId, shopId));
        Boolean flag = false;
        if (CollectionUtils.isNotEmpty(orderBaseEntityList)) {
            for (OrderBaseEntity entity : orderBaseEntityList) {
                //订单未完成不能删除,否则可以删除
                //订单待付款
                if (entity.getOrderStatus().equals(CommonType.orderStatus.TO_BE_PAID.getCode())) {
                    flag = true;
                }
                //订单取消
                if (entity.getOrderStatus().equals(CommonType.orderStatus.CANCEL.getCode())) {
                    flag = true;
                }
                //订单已退款并且超过7天售后期
                if (entity.getOrderStatus().equals(CommonType.orderStatus.REFUNDED.getCode())
                        && (DateUtils.getBetweenDays(entity.getOrderFinishedTime(), new Date()) > 7)) {
                    flag = true;
                }
                //非虚拟订单
                if (entity.getVirtualGoods().equals(CommonType.commonStatus.YES.getCode())) {
                    //订单完成并且超过7天售后期
                    if (entity.getOrderStatus().equals(CommonType.orderStatus.CONFIRM_RECEIPT.getCode())
                            && (DateUtils.getBetweenDays(entity.getOrderFinishedTime(), new Date()) > 7)) {
                        flag = true;
                    }
                } else {
                    //虚拟订单
                    //虚拟订单已过期
                    if (!DateUtils.compareDate2(entity.getPeriodOfValidity()) && (entity.getPastDue().equals(0))) {
                        flag = true;
                    }
                    //虚拟订单已校验
                    if (entity.getIsCancel().equals(CommonType.commonStatus.NO.getCode())) {
                        flag = true;
                    }
                }
                //订单删除
                if (null != entity.getOrderDel() && entity.getOrderDel().equals(CommonType.commonStatus.YES.getCode())) {
                    flag = true;
                }
                if (!flag) {
                    break;
                }
            }
            if (flag) {
                return ResponseResult.success();
            } else {
                return ResponseResult.fail();
            }
        }
        return ResponseResult.success();
    }

    /**
     * 根据传入的参数查询兑换列表。
     *
     * @param param 包含查询参数的Map对象，其中必须包含token信息用于用户身份验证。
     * @return 返回查询结果的ResponseResult对象。如果用户信息有效，返回查询到的兑换列表；
     * 如果用户信息无效或查询过程中发生异常，则返回错误信息。
     */
    @Override
    public ResponseResult queryExchangeList(Map<String, Object> param) {
        // 通过token获取用户信息，用于权限验证
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.get("token").toString());
        if (null != userInfo) {
            try {
                // 将查询参数Map转换为Bean对象，方便后续处理
                Map data = ParseMapUtils.beanToMap(param);
                // 执行分页查询
                IPage<OrderBaseEntity> page = this.baseMapper.queryExchangeList(new Query<OrderBaseEntity>().getPage(data), param);

                // 返回查询结果
                return ResponseResult.success(PageUtils.getPage(page));
            } catch (IllegalAccessException e) {
                // 查询过程中发生异常，打印堆栈信息
                e.printStackTrace();
            }
        }
        // 用户信息验证失败或查询异常，返回错误码表示token非法
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 发货
     *
     * @param map
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult piLiangFahuoShipments(Map<Object, Object> map, Integer isPiLiang) throws TransactionException {
        ResponseResult rr = new ResponseResult();
        String token = (String) map.get("token");
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByTokenNoIsExist(token);
        log.info("-----para-----" + map.toString());
        String orderId = (String) map.get("orderId");
        //校验订单是否退款
        Integer count = Math.toIntExact(this.baseMapper.selectCount(new LambdaQueryWrapper<OrderBaseEntity>()
                .eq(OrderBaseEntity::getOrderId, orderId)
                .eq(OrderBaseEntity::getOrderReturnStatus, CommonType.orderReturnStatus.NO_RETURNS.getCode())
                .eq(OrderBaseEntity::getOrderRefundStatus, CommonType.orderRefundStatus.NO_REFUND.getCode())));
        if (count == 0) {
            return ResponseResult.fail(BizCodeEnume.ORDER_HAVE_REFUND_RETURN);
        }
        boolean isUpdateSuccessByOrderGoods = false;
        boolean isUpdateSuccessByOrderBase;
        try {
            // 商家所用发货地址id
            Integer shippingAddressId = null;
            // 发货人姓名
            String orderSellerName = (String) map.get("orderSellerName");
            // 发货人地址
            String orderSellerAddress = (String) map.get("orderSellerAddress");
            // 发货人联系方式
            String orderSellerContact = (String) map.get("orderSellerContact");
            // 配送时间
            String orderShippingTime = DateUtils.getTime();
            // 配送公司ID
            Integer orderShippingExpressId = 0;
            // 物流单号
            String orderShippingCode = null;
            // 1 物流配送 2 自主配送
            Integer shipmentType = map.get("shipmentType") != null ? Integer.parseInt(map.get("shipmentType").toString()) : 1;
            //配送人
            String deliveryPerson = null;
            //配送联系方式
            String deliveryTelephone = null;
            //配送凭证
            String deliveryCertificate = null;
            // shipmentType 1 物流配送 2 自主配送
            if (CommonType.shipmentType.LOGISTICS_DELIVERY.getCode().equals(shipmentType)) {
                orderShippingExpressId = Integer.parseInt(map.get("orderShippingExpressId").toString());
                orderShippingCode = (String) map.get("orderShippingCode");
                shippingAddressId = StringUtils.isEmpty(map.get("shippingAddressId").toString())
                        ? 0 : Integer.parseInt(map.get("shippingAddressId").toString());
            } else {
                deliveryPerson = (String) map.get("deliveryPerson");
                deliveryTelephone = (String) map.get("deliveryTelephone");
                deliveryCertificate = (String) map.get("deliveryCertificate");
            }
            // 仓库id
            String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.STORAGE_SWITCH.getDesc());
            if(!StringUtils.isEmpty(value) && "1".equals(value) &&  CommonType.ShopType.COMMON_SHOP.getCode().equals(userInfo.getShopType())){
                if(map.get("storageId") == null || StringUtils.isEmpty(map.get("storageId").toString())){
                    return ResponseResult.fail(BizCodeEnume.STORAGE_ID_NOT_EXIST);
                }
            }
            Integer storageId = map.get("storageId") != null && !StringUtils.isEmpty(map.get("storageId").toString())
                    ? Integer.parseInt(map.get("storageId").toString()) : 0;
            List<StorageInventoryFlowGoodsDTO> storageGoodsList = new ArrayList<>();
            // 商品id
            List<?> goodsIdsObj = (List<?>) map.get("goodsIds");
            List<Integer> goodsIds = goodsIdsObj.stream()
                    .map(obj -> Integer.parseInt(obj + "")).collect(Collectors.toList());
            // 商品数量
            List<Integer> goodsNums = (List<Integer>) map.get("goodsNums");
            // 查询订单信息 如状态，退货退款状态，收货人手机号
            OrderBaseEntity qos = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderBaseEntity>()
                    .eq(OrderBaseEntity::getOrderId, orderId));
            String receiverPhone = qos.getOrderReceiverContact();
            BaseExpressDTO shipperCode = null;
            //物流配送
            if (CommonType.shipmentType.LOGISTICS_DELIVERY.getCode().equals(shipmentType)) {
                ResponseResult express = baseServiceClient.getBaseExpressInfoById(orderShippingExpressId);
                shipperCode = JSONObject.parseObject(JSON.toJSONString(express.getData()), BaseExpressDTO.class);
            }

            // 物流发货备注
            String orderShippingMessage = map.get("orderShippingMessage") == null ?
                    "" : (String) map.get("orderShippingMessage");
            Integer orderStatus = 4;
            // 商品订单是否已经发货。1-是，0-否，默认0(orderGoods)
            Integer orderGoodsIdDeliver = 1;
            Map<String, Object> mapGoods = new HashMap<>();
            mapGoods.put("orderId", orderId);
            mapGoods.put("orderStatus", orderStatus);
            //updateByOrderBase 参数去除
            mapGoods.put("orderGoodsIdDeliver", orderGoodsIdDeliver);
            mapGoods.put("orderSellerName", orderSellerName);
            mapGoods.put("orderSellerAddress", orderSellerAddress);
            mapGoods.put("orderSellerContact", orderSellerContact);
            mapGoods.put("orderShippingTime", orderShippingTime);
            mapGoods.put("orderShippingExpressId", orderShippingExpressId);
            mapGoods.put("orderShippingCode", orderShippingCode);
            mapGoods.put("orderShippingMessage", orderShippingMessage);
            if (qos.getOrderStatus() == 3 && qos.getOrderRefundStatus().equals("1")) {
                rr.setCode(BizCodeEnume.ORDER_AFTER_SALES.getCode());
                rr.setMessage(BizCodeEnume.ORDER_AFTER_SALES.getMsg());
            } else if (qos.getOrderStatus() == 6 && qos.getOrderRefundStatus().equals("2")) {
                rr.setCode(BizCodeEnume.ORDER_AFTER_SALES_COMPLETION.getCode());
                rr.setMessage(BizCodeEnume.ORDER_AFTER_SALES_COMPLETION.getMsg());
            } else {
                int index = 0;
                //查询当前订单已发货的商品数量
                Integer goodsNumCount = this.baseMapper.selectOrderFahuoGoodsNum(orderId, goodsIds);
                if (null == goodsNumCount) {
                    goodsNumCount = 0;
                }
                String goodsName = "";
                for (Integer goodsId : goodsIds) {
                    StorageInventoryFlowGoodsDTO storageInventoryFlowGoodsDTO = new StorageInventoryFlowGoodsDTO();
                    storageInventoryFlowGoodsDTO.setGoodsId(goodsId);
                    storageInventoryFlowGoodsDTO.setGoodsNum(goodsNums.get(index));
                    storageInventoryFlowGoodsDTO.setBuyNum(goodsNums.get(index));
                    storageGoodsList.add(storageInventoryFlowGoodsDTO);
                    StringBuilder sb = new StringBuilder();
                    OrderGoodsEntity orderGoods = orderGoodsMapper.selectOne(new LambdaQueryWrapper<OrderGoodsEntity>()
                            .eq(OrderGoodsEntity::getOrderId, orderId)
                            .eq(OrderGoodsEntity::getGoodsId, goodsId));
                    goodsName = orderGoods.getGoodsName();
                    String goodsShippingCode = orderGoods.getOrderShippingCode();
                    if (StrUtil.isNotEmpty(goodsShippingCode)) {
                        String[] goodsShippingCodeList = goodsShippingCode.split(",");
                        if (goodsShippingCodeList.length > 1) {
                            for (String code : goodsShippingCodeList) {
                                if (code.equals(goodsShippingCode)) {
                                    sb.append(goodsShippingCode).append(",");
                                    continue;
                                }
                                sb.append(code).append(",");
                            }
                            Integer lastNum = sb.toString().lastIndexOf(",");
                            goodsShippingCode = (sb).substring(0, lastNum);
                        } else {
                            goodsShippingCode = orderShippingCode;
                        }
                    } else {
                        goodsShippingCode = orderShippingCode;
                    }
                    Integer goodsNum = goodsNums.get(index) + orderGoods.getShipmentQuantity();
                    if (goodsNum > Integer.parseInt(orderGoods.getOrderGoodsNum())) {
                        return ResponseResult.fail(BizCodeEnume.SHIPMENT_QUANTITY_GT_GOODS__NUM);
                    }
                    mapGoods.put("shipmentQuantity", goodsNum);
                    if (goodsNum >= Integer.parseInt(orderGoods.getOrderGoodsNum())) {
                        mapGoods.put("shipmentStatus", 2);
                    } else {
                        mapGoods.put("shipmentStatus", 1);
                    }
                    goodsNumCount += goodsNum;
                    mapGoods.put("orderGoodsId", orderGoods.getOrderGoodsId());
                    mapGoods.put("goodsShippingCode", goodsShippingCode);
                    isUpdateSuccessByOrderGoods = orderGoodsMapper.updateByOrderGoods(mapGoods);
                    index++;
                }
                //查询当前订单总的商品数量
                Integer orderGoodsNum = this.baseMapper.selectOrderGoodsNum(orderId);
                if (!goodsNumCount.equals(orderGoodsNum)) {
                    //部分发货
                    mapGoods.put("orderStatus", 16);
                }
                String orderGoodsIds = JSON.toJSONString(goodsIds);
                String orderGoodsNums = JSON.toJSONString(goodsNums);
                OrderShipmentEntity shipmentEntity = new OrderShipmentEntity(orderId, orderGoodsIds, orderGoodsNums, DateUtils.getNowDateToDf(),
                        orderShippingExpressId, orderShippingCode, orderShippingMessage,
                        shipmentType, deliveryPerson, deliveryTelephone, deliveryCertificate, storageId, orderSellerAddress);
                Boolean isInsertShipment = orderShipmentMapper.insert(shipmentEntity) > 0;
                //后台配置的自动确认收货时间
                String configValue = baseServiceClient.selectConfigValueFeign(CommonType.configKey.AUTOMATIC_DELIVERY_TIME.getDesc());
                Integer automaticDeliveryTime = Integer.parseInt(configValue);
                mapGoods.put("automaticDeliveryTime", automaticDeliveryTime);
                isUpdateSuccessByOrderBase = this.baseMapper.updateByOrderBase(mapGoods);
                if (isUpdateSuccessByOrderGoods && isUpdateSuccessByOrderBase && isInsertShipment) {
                    // 分佣订单售后时间更新
                    ResponseResult res = updateOrderAfterSale(orderId, goodsIds);
                    if (res.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
                        GlobalTransactionContext.reload(RootContext.getXID()).rollback();
                        return res;
                    }
                    rr.setCode(CommonStatusEnum.SUCCESS.getCode());
                    rr.setMessage("发货成功！");
                    payMentServiceClient.changeConsumeRecord(orderId);
                    if (CommonType.commonStatus.YES.getCode().equals(isPiLiang)) {
                        // 信息
                        rr = merchantServiceClient.queryShippingAddress(shippingAddressId);
                        String senderAddress = (String) rr.getData();
                        String requestData = sendKdniaoMessage(senderAddress, qos, orderShippingCode,
                                orderSellerName, orderSellerContact, orderSellerAddress, shipperCode);
                        String results = KdniaoSubscribeAPI.orderTracesSubByJson(requestData);
                        System.out.println("-----LogisticsTrackingReturnResults-----" + results);
                    }
                    //发货成功短信提醒
                    try {
                        //获取店铺名称
                        String shopName = qos.getShopName();
                        //设置快递名称
                        String ShippingExpressName = shipperCode.getExpressName();
                        Map<String, Object> paraMap = new HashMap<>();
                        List<String> messageList = new ArrayList<>();
                        messageList.add(shopName);
                        messageList.add(orderShippingCode);
                        messageList.add(ShippingExpressName);
                        paraMap.put("list", messageList);
                        paraMap.put("mobile", receiverPhone);
                        paraMap.put("orderId", qos.getOrderId());
                        paraMap.put("userId", qos.getBuyerUserId());
                        paraMap.put("shopName", shopName);
                        paraMap.put("trackingNumber", orderShippingCode);
                        paraMap.put("expressCompany", ShippingExpressName);
                        paraMap.put("goodsName", goodsName);
                        paraMap.put("goodsNum", goodsNumCount);
                        paraMap.put("templateId", CommonType.messageTemplate.CODE_O.getCode());
                        baseServiceClient.sendMessageTemplate(paraMap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Integer type = CommonType.inventoryFlowType.OUT_STORAGE.getCode();
                    Integer typeDetails = CommonType.inventoryFlowTypeDetail.SALES_OUTBOUND.getCode();
                    setStorageInventoryFlow(storageId, orderId, type, typeDetails, storageGoodsList, goodServiceClient, baseServiceClient, qos.getShopId(), map.get("token").toString());
                } else {
                    rr.setCode(BizCodeEnume.UPDATE_ERROR.getCode());
                    rr.setMessage(BizCodeEnume.UPDATE_ERROR.getMsg());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            rr.setCode(BizCodeEnume.SYSTEM_ERROR.getCode());
            rr.setMessage(BizCodeEnume.SYSTEM_ERROR.getMsg());
            log.info("ShipmentsErrorResults:" + e.getMessage());
            GlobalTransactionContext.reload(RootContext.getXID()).rollback();
        }
        return rr;
    }

    /**
     * 设置库存流水信息
     * 根据配置开关和存储ID，记录库存流水，包括入库和出库情况
     *
     * @param storageId         存储位置ID
     * @param orderId           订单ID，用于备注
     * @param type              操作类型，如入库或出库
     * @param typeDetails       操作类型详情
     * @param storageGoodsList  存储货物列表
     * @param goodServiceClient 调用商品服务客户端
     * @param baseServiceClient 调用基础服务客户端
     * @param shopId            店铺ID，可选参数
     */
    public static void setStorageInventoryFlow(Integer storageId, String orderId, Integer type, Integer typeDetails,
                                               List<StorageInventoryFlowGoodsDTO> storageGoodsList, GoodServiceClient goodServiceClient,
                                               BaseServiceClient baseServiceClient, Integer shopId, String token) {
        // 检查库存操作开关是否打开，以及存储ID是否有效
        String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.STORAGE_SWITCH.getDesc());
        if ("1".equals(value) && storageId != 0) {
            StorageInventoryFlowDTO storageInventoryFlowDTO = new StorageInventoryFlowDTO();
            // 根据操作类型设置入库或出库的具体信息
            if (type.equals(CommonType.inventoryFlowType.PUT_STORAGE.getCode())) {
                storageInventoryFlowDTO.setPutStorageId(storageId);
                storageInventoryFlowDTO.setStartTime(DateUtils.getNowDate());
            } else {
                storageInventoryFlowDTO.setOutStorageId(storageId);
                storageInventoryFlowDTO.setEndTime(DateUtils.getNowDate());
            }
            // 设置店铺ID，如果提供的话
            if (shopId != null) {
                storageInventoryFlowDTO.setShopId(shopId);
            }
            // 设置其他基本信息
            storageInventoryFlowDTO.setType(type);
            storageInventoryFlowDTO.setTypeDetails(typeDetails);
            storageInventoryFlowDTO.setNotes("订单号：" + orderId);
            storageInventoryFlowDTO.setGoodsList(storageGoodsList);
            storageInventoryFlowDTO.setToken(token);
            // 调用服务保存库存流水信息
            goodServiceClient.insertStorageInventoryFlow(storageInventoryFlowDTO);
        }
    }
    /**
     * 设置库存流水信息
     * 根据配置开关和存储ID，记录库存流水，包括入库和出库情况
     *
     * @param storageId         存储位置ID
     * @param orderId           订单ID，用于备注
     * @param type              操作类型，如入库或出库
     * @param typeDetails       操作类型详情
     * @param storageGoodsList  存储货物列表
     * @param goodServiceClient 调用商品服务客户端
     * @param baseServiceClient 调用基础服务客户端
     * @param shopId            店铺ID，可选参数
     */
    public static void setStorageInventoryFlow2(Integer storageId, String orderId, Integer type, Integer typeDetails,
                                               List<StorageInventoryFlowGoodsDTO> storageGoodsList, GoodServiceClient goodServiceClient,
                                               BaseServiceClient baseServiceClient, Integer shopId, Map<String, String> map) {
        // 检查库存操作开关是否打开，以及存储ID是否有效
        String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.STORAGE_SWITCH.getDesc());
        if ("1".equals(value) && storageId != 0) {
            StorageInventoryFlowDTO storageInventoryFlowDTO = new StorageInventoryFlowDTO();
            // 根据操作类型设置入库或出库的具体信息
            if (type.equals(CommonType.inventoryFlowType.PUT_STORAGE.getCode())) {
                storageInventoryFlowDTO.setPutStorageId(storageId);
                storageInventoryFlowDTO.setStartTime(DateUtils.getNowDate());
            } else {
                storageInventoryFlowDTO.setOutStorageId(storageId);
                storageInventoryFlowDTO.setEndTime(DateUtils.getNowDate());
            }
            // 设置店铺ID，如果提供的话
            if (shopId != null) {
                storageInventoryFlowDTO.setShopId(shopId);
            }
            // 设置其他基本信息
            storageInventoryFlowDTO.setType(type);
            storageInventoryFlowDTO.setTypeDetails(typeDetails);
            storageInventoryFlowDTO.setNotes("订单号：" + orderId);
            storageInventoryFlowDTO.setGoodsList(storageGoodsList);
            storageInventoryFlowDTO.setShopId(Integer.valueOf(map.get("shopId")));
            storageInventoryFlowDTO.setUserId(map.get("userId"));
            storageInventoryFlowDTO.setUserName(map.get("userName"));
            // 调用服务保存库存流水信息
            goodServiceClient.insertStorageInventoryFlow(storageInventoryFlowDTO);
        }
    }
    /**
     * 更新售后订单的状态。
     * 根据售后订单ID查询相应的售后佣金信息，如果存在，则调用外部服务查询商品的售后设置，并更新售后订单的处理时间。
     * 此函数旨在处理售后订单的后续操作，确保订单状态与商品的售后政策保持一致。
     *
     * @param orderId 售后订单ID，用于查询和更新售后订单信息。
     * @return 返回处理结果，如果成功更新，则返回成功的响应结果；如果发生异常，则返回操作失败的响应结果。
     */
    public ResponseResult updateOrderAfterSale(String orderId, List<Integer> goodsIdList) {
        try {
            for (Integer goodsId : goodsIdList) {
                // 根据订单ID查询售后佣金信息
                OrderCommissionVO orderCommission = orderCommissionMapper.selectByPrimaryKey(orderId, goodsId);
                // 如果售后佣金信息存在
                if (null != orderCommission) {
                    // 调用基础服务客户端，查询商品的售后设置
                    String configValue = baseServiceClient.selectConfigValueFeign(CommonType.configKey.AFTER_SALES_TIME.getDesc());
                    // 更新售后订单的处理时间，处理时间为商品售后天数后的日期
                    this.baseMapper.updateOrderAfterSaleTime(orderId, DateUtils.getAfterDate(Integer.parseInt(configValue)));
                }
            }
            // 返回操作成功的响应结果
            return ResponseResult.success();
        } catch (Exception e) {
            // 记录异常信息
            log.error(e.getMessage());
            // 返回操作失败的响应结果，错误码为OPERATION_FAILED
            return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
        }
    }

    /**
     * 根据给定的参数拼接快递鸟的请求数据。
     * 该方法用于构建发送给快递鸟API的请求字符串，包含发件人和收件人的详细信息。
     *
     * @param senderAddress 发件人地址信息的JSON字符串。
     * @param qos 订单基础信息实体，包含买家和卖家的相关信息。
     * @param orderShippingCode 订单的物流编码。
     * @param orderSellerName 卖家的名称。
     * @param orderSellerContact 卖家的联系方式。
     * @param orderSellerAddress 卖家的地址。
     * @param shipperCode 快递公司的编码信息。
     * @return 返回构建好的请求数据字符串。
     */
    /**
     * 快递鸟传值拼接
     */
    public String sendKdniaoMessage(String senderAddress, OrderBaseEntity qos, String orderShippingCode,
                                    String orderSellerName, String orderSellerContact, String orderSellerAddress, BaseExpressDTO shipperCode) {
        // 初始化请求数据字符串
        String requestData = null;
        try {
            // 解析发件人地址信息
            MerchantsAddressDTO merchantsAddress = JSON.parseObject(senderAddress, MerchantsAddressDTO.class);
            // 初始化订阅信息对象
            SubscriptionVO sp = new SubscriptionVO();
            // 设置快递公司编码
            sp.setShipperCode(shipperCode.getExpressPinyin());
            // 设置物流编码
            sp.setLogisticCode(orderShippingCode);
            // 初始化发件人参数对象
            SenderDTO senderPara = new SenderDTO();
            // 设置发件人信息
            senderPara.setName(orderSellerName);
            senderPara.setMobile(orderSellerContact);
            senderPara.setAddress(orderSellerAddress);
            senderPara.setProvinceName(merchantsAddress.getShippingAddressProvince());
            senderPara.setCityName(merchantsAddress.getShippingAddressCity());
            senderPara.setExpAreaName(merchantsAddress.getShippingAddressArea());
            // 初始化收件人信息
            // 推送参数
            String buyerUserId = qos.getBuyerUserId();
            Map<String, Integer> districtMap = new HashMap<String, Integer>();
            districtMap.put("provinceId", qos.getOrderReceiverProvinceId());
            districtMap.put("cityId", qos.getOrderReceiverCityId());
            districtMap.put("areaId", qos.getOrderReceiverAreaId());
            List<String> region = baseServiceClient.queryRegionName(districtMap);
            ReceiverDTO rp = new ReceiverDTO();
            rp.setName(qos.getOrderReceiverName());
            rp.setMobile(qos.getOrderReceiverContact());
            rp.setAddress(qos.getOrderReceiverAddress());
            rp.setProvinceName(region.get(0));
            rp.setCityName(region.get(1));
            rp.setExpAreaName(region.get(2));
            // 设置收件人参数
            sp.setSenderPara(senderPara);
            sp.setReceiverPara(rp);
            // 将订阅信息转换为字符串形式作为请求数据
            requestData = sp.toString();
        } catch (Exception e) {
            // 记录异常信息
            log.info("-----sendKdniaoMessage-----" + e.getMessage());
        }
        // 返回请求数据字符串
        return requestData;
    }

    /**
     * 根据传入的参数计算平台的总销售额。
     * 通过传入的token验证用户身份，根据用户选择的统计类型查询相应的销售数据。
     * 销售数据分为全部、当日、本周、本月和年度。
     * 返回计算后的总销售额。
     *
     * @param map 包含token和type的Map对象，token用于用户身份验证，type指定统计类型。
     * @return 返回计算后的总销售额，如果验证失败或查询过程中出现异常，则返回相应的错误信息。
     */
    @Override
    public ResponseResult totalTurnoverOfThePlatform(Map<String, Object> map) {
        // 从map中获取token，用于用户身份验证
        String token = (String) map.get("token");
        // 根据token验证用户身份
        UserInfoVO info = ParseTokenUtils.getPlatformUser(token);
        if (info != null) {
            try {
                // 从map中获取统计类型
                // 类型:1、全部，2、当日，3、本周，4、本月，5、年度
                Integer type = (Integer) map.get("type");
                // 初始化销售额为0
                BigDecimal money = new BigDecimal(0);
                // 根据不同的统计类型查询相应的销售数据
                if (type == 1) {
                    money = this.baseMapper.queryAllSales();
                } else if (type == 2) {
                    money = this.baseMapper.queryTodaySales();
                } else if (type == 3) {
                    money = this.baseMapper.queryWeekSales();
                } else if (type == 4) {
                    money = this.baseMapper.queryMonthSales();
                } else {
                    money = this.baseMapper.queryYearSales();
                }
                // 对销售额进行四舍五入，保留两位小数
                money = money.setScale(2, RoundingMode.HALF_UP);
                // 返回查询成功的响应结果，包含总销售额
                return ResponseResult.success(money);
            } catch (Exception e) {
                // 如果查询过程中出现异常，返回系统错误的响应结果
                return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(), BizCodeEnume.SYSTEM_ERROR.getMsg());
            }
        } else {
            // 如果token验证失败，返回token无效的响应结果
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
    }

    /**
     * 根据用户权限查询不同类型的店铺排名信息。
     *
     * @param map 包含查询类型和用户令牌的信息。
     * @return 返回查询结果，如果查询失败或令牌无效，则返回相应的错误信息。
     */
    @Override
    public ResponseResult storeRanking(Map<String, Object> map) {
        // 从请求参数中获取用户令牌
        String token = (String) map.get("token");

        // 根据令牌解析用户信息，检查令牌是否有效
        UserInfoVO info = ParseTokenUtils.getPlatformUser(token);
        if (info != null) {
            try {
                // 从请求参数中获取查询类型
                Integer type = (Integer) map.get("type");
                List<StoreParaVO> storeList = null;

                // 根据查询类型查询不同的店铺排名列表
                if (type == 1) {
                    storeList = this.baseMapper.queryAllStore();
                } else if (type == 2) {
                    storeList = this.baseMapper.queryTodayStore();
                } else if (type == 3) {
                    storeList = this.baseMapper.queryWeekStore();
                } else if (type == 4) {
                    storeList = this.baseMapper.queryMonthStore();
                } else {
                    storeList = this.baseMapper.queryYearStore();
                }

                // 返回查询成功的响应结果，包含店铺排名列表
                return ResponseResult.success(storeList);
            } catch (Exception e) {
                // 如果查询过程中发生异常，返回系统错误的响应结果
                return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(), BizCodeEnume.SYSTEM_ERROR.getMsg());
            }
        } else {
            // 如果令牌无效，返回相应的错误响应结果
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
    }

    /**
     * 根据用户令牌获取物流信息。
     *
     * @param map 包含令牌和其他请求参数的映射。
     * @return 返回订单的物流信息。
     */
    @Override
    public ResponseResult logisticsInformation(Map<String, Object> map) {
        // 通过令牌获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        // 如果用户信息为空，表示令牌无效，返回错误响应
        if (null == userInfo) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        // 提取用户ID，并将其添加到请求参数映射中
        String userId = userInfo.getUserId();
        map.put("userId", userId);

        // 根据更新后的请求参数映射查询订单列表及相关信息
        IPage<OrderListVO> page = this.baseMapper.selByOrderBase(new Query<OrderListVO>().getPage(map), map);
        List<OrderListVO> orderList = page.getRecords();
        // 遍历订单列表，计算每个订单的商品总数和总价
        for (int i = 0; i < orderList.size(); i++) {
            Integer goodsNum = 0;
            BigDecimal goodsTotalPrice = new BigDecimal(0);
            String orderId = orderList.get(i).getOrderId();

            // 根据订单ID查询商品详情列表
            List<GoodsDetailsListVO> goodsDetailsList = this.baseMapper.queryGoodsList1(orderId);
            orderList.get(i).setGoodsDetailsList(goodsDetailsList);

            // 遍历商品详情列表，计算订单的商品总数和总价
            for (int j = 0; j < goodsDetailsList.size(); j++) {
                // 计算共有几件商品
                Integer goodsnum = goodsDetailsList.get(j).getOrderGoodsNum();
                goodsNum += goodsnum;
                orderList.get(i).setGoodsNum(goodsNum);
                // 计算商品总价
                BigDecimal goodsprice = goodsDetailsList.get(j).getOrderGoodsAmount();
                goodsTotalPrice = goodsTotalPrice.add(goodsprice);
                orderList.get(i).setOrderTotalPrice(goodsTotalPrice);
            }
        }
        // 返回成功响应，包含分页信息
        return ResponseResult.success(PageUtils.getPage(page));
    }


    @Override
    public ResponseResult queryGateOrders(Map<String, Object> map) {
        String token = (String) map.get("token");
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
        if (null != ui) {
            String userId = null;
            try {
                // 发送人id
                userId = ui.getUserId();
                // 订单状态
                Integer orderStatus = (Integer) map.get("orderStatus");
                /*
				订单状态 1.待付款 2.已付款 3.待发货 4.已发货 5.已签收 6.确认收货 7.取消 8.退款中 9.已退款 11.待自提
				 */
                if (orderStatus == 2) {
                    // 待付款
                    map.put("orderStatusNum", 1);
                } else if (orderStatus == 4) {
                    //
                    map.put("orderStatusNum", 4);
                } else if (orderStatus == 5) {
                    // 已完成
                    map.put("orderStatusNum", 6);
                }
                map.put("userId", userId);

                IPage<OrderListVO> page = this.baseMapper.queryGateOrdersList(new Query<OrderListVO>().getPage(map), map);
                List<OrderListVO> orderList = page.getRecords();
                // 商品总价
                BigDecimal paymentPrice = new BigDecimal(0);
                for (int i = 0; i < orderList.size(); i++) {
                    Integer goodsNum = 0;
                    String orderId = orderList.get(i).getOrderId();
                    //feign调用根据门店ID查询门店名称
                    if (orderList.get(i).getOrderGateShopId() != null) {
                        Map<String, String> resultGateName = merchantServiceClient.queryGateStoreName(orderList.get(i).getOrderGateShopId());
                        orderList.get(i).setOrderGateName(resultGateName.get("shopGateName"));
                    }
                    List<GoodsDetailsListVO> goodsDetailsList = orderGoodsMapper.queryGoodsList(orderId);
                    orderList.get(i).setGoodsDetailsList(goodsDetailsList);
                    // 订单状态
                    Integer normalStatus = orderList.get(i).getOrderStatus();
                    // 1.待付款  4.已发货  6.确认收货 7.取消 8.退款中 9.已退款
                    Integer status = normalStatus;
                    // 存放最新状态
                    orderList.get(i).setOrderStatus(status);
                    for (int j = 0; j < goodsDetailsList.size(); j++) {
                        // 计算共有几件商品
                        Integer goodsnum = goodsDetailsList.get(j).getOrderGoodsNum();
                        goodsNum += goodsnum;
                        orderList.get(i).setGoodsNum(goodsNum);

                        // 计算商品总价
                        // 单个商品价格
                        BigDecimal orderGoodsAmount = goodsDetailsList.get(j).getOrderGoodsAmount();
                        paymentPrice = paymentPrice.add(orderGoodsAmount).setScale(2, BigDecimal.ROUND_UP);

                    }
                    // 每家店铺总计价格
                    orderList.get(i).setShopOrderTotalPrice(paymentPrice);
                    // 退款实际价格
                    orderList.get(i).setRefundRealPrice(orderList.get(i).getOrderGoodsAmount());
                    // 总付款价格
                    orderList.get(i).setOrderTotalPrice(orderList.get(i).getOrderPaymentAmount());

                }
                page.setRecords(orderList);
                return ResponseResult.success(PageUtils.getPage(page));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(), BizCodeEnume.SYSTEM_ERROR.getMsg());
    }

    @Override
    public ResponseResult queryGateDeliveryOrders(Map<String, Object> map) {
        String token = (String) map.get("token");
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
        if (null != ui) {
            String userId = null;
            try {
                // 发送人id
                userId = ui.getUserId();
                // 订单状态
                Integer orderStatus = (Integer) map.get("orderStatus");

                if (orderStatus == 1) {
                    // 全部
                    map.put("orderStatusNum", null);
                } else if (orderStatus == 2) {
                    // 待付款
                    map.put("orderStatusNum", 1);
                } else {
                    map.put("orderStatusNum", orderStatus);
                }
                map.put("userId", userId);

                IPage<OrderListVO> page = this.baseMapper.queryGateDeliveryOrders(new Query<OrderListVO>().getPage(map), map);
                List<OrderListVO> orderList = page.getRecords();
                // 商品总价
                BigDecimal paymentPrice = new BigDecimal(0);
                for (int i = 0; i < orderList.size(); i++) {
                    Integer goodsNum = 0;
                    String orderId = orderList.get(i).getOrderId();
                    //feign调用根据门店ID查询门店名称
                    if (orderList.get(i).getOrderGateShopId() != null) {
                        Map<String, String> resultGateName = merchantServiceClient.queryGateStoreName(orderList.get(i).getOrderGateShopId());
                        orderList.get(i).setOrderGateName(resultGateName.get("shopGateName"));
                        orderList.get(i).setLongitude(resultGateName.get("longitude"));
                        orderList.get(i).setLatitude(resultGateName.get("latitude"));
                    }
                    List<GoodsDetailsListVO> goodsDetailsList = orderGoodsMapper.queryGoodsList(orderId);
                    orderList.get(i).setGoodsDetailsList(goodsDetailsList);
                    // 订单状态
                    Integer normalStatus = orderList.get(i).getOrderStatus();
                    // 1.待付款  4.已发货  6.确认收货 7.取消 8.退款中 9.已退款
                    Integer status = normalStatus;
                    // 存放最新状态
                    orderList.get(i).setOrderStatus(status);
                    for (int j = 0; j < goodsDetailsList.size(); j++) {
                        // 计算共有几件商品
                        Integer goodsnum = goodsDetailsList.get(j).getOrderGoodsNum();
                        goodsNum += goodsnum;
                        orderList.get(i).setGoodsNum(goodsNum);

                        // 计算商品总价
                        // 单个商品价格
                        BigDecimal orderGoodsAmount = goodsDetailsList.get(j).getOrderGoodsAmount();
                        paymentPrice = paymentPrice.add(orderGoodsAmount).setScale(2, BigDecimal.ROUND_UP);

                    }
                    // 每家店铺总计价格
                    orderList.get(i).setShopOrderTotalPrice(paymentPrice);
                    // 退款实际价格
                    orderList.get(i).setRefundRealPrice(orderList.get(i).getOrderGoodsAmount());
                    // 总付款价格
                    orderList.get(i).setOrderTotalPrice(orderList.get(i).getOrderPaymentAmount());

                }
                page.setRecords(orderList);
                return ResponseResult.success(PageUtils.getPage(page));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(), BizCodeEnume.SYSTEM_ERROR.getMsg());
    }

    /**
     * 根据用户token查询门禁交付单数量。
     *
     * @param map 包含用户token等信息的参数映射。
     * @return 返回查询结果的响应对象。
     */
    @Override
    public ResponseResult queryGateDeliveryOrdersNum(Map<String, Object> map) {
        // 从参数映射中获取用户token
        String token = (String) map.get("token");
        // 根据token解析用户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
        // 如果用户信息存在
        if (null != ui) {
            // 初始化门禁交付单数量统计对象
            GateDeliveryOrdersNumVO gateDeliveryOrdersNumVO = new GateDeliveryOrdersNumVO();
            // 统计待付款单数
            Integer waitPay = this.baseMapper.queryGateDeliveryOrdersNum(ui.getUserId(), CommonType.orderStatus.TO_BE_PAID.getCode(), null);
            // 统计待接单单数
            Integer waitOrder = this.baseMapper.queryGateDeliveryOrdersNum(ui.getUserId(), CommonType.orderStatus.TO_BE_SHIPPED.getCode(), null);
            // 统计待取货单数
            Integer waitPick = this.baseMapper.queryGateDeliveryOrdersNum(ui.getUserId(), CommonType.orderStatus.TO_BE_SHIPPED.getCode(), CommonType.DeliveryOrderStart.NOT_PICKUP.getCode());
            // 统计待配送单数
            Integer waitPickup = this.baseMapper.queryGateDeliveryOrdersNum(ui.getUserId(), CommonType.orderStatus.TO_BE_SHIPPED.getCode(), CommonType.DeliveryOrderStart.PICKUP.getCode());
            // 统计配送中单数
            Integer waitDelivery = this.baseMapper.queryGateDeliveryOrdersNum(ui.getUserId(), CommonType.orderStatus.SHIPPED.getCode(), null);
            // 统计已完成单数
            Integer waitComplete = this.baseMapper.queryGateDeliveryOrdersNum(ui.getUserId(), CommonType.orderStatus.CONFIRM_RECEIPT.getCode(), null);
            // 统计售后单数
            Integer waitAfterSale = this.baseMapper.queryGateDeliveryOrdersNum(ui.getUserId(), CommonType.orderStatus.REFUNDED.getCode(), null);
            // 设置统计结果到门禁交付单数量统计对象
            gateDeliveryOrdersNumVO.setWaitPay(waitPay);
            gateDeliveryOrdersNumVO.setWaitOrder(waitOrder);
            gateDeliveryOrdersNumVO.setWaitPick(waitPick);
            gateDeliveryOrdersNumVO.setWaitPickup(waitPickup);
            gateDeliveryOrdersNumVO.setWaitDelivery(waitDelivery);
            gateDeliveryOrdersNumVO.setWaitComplete(waitComplete);
            gateDeliveryOrdersNumVO.setWaitAfterSale(waitAfterSale);
            // 返回成功响应，包含门禁交付单数量统计对象
            return ResponseResult.success(gateDeliveryOrdersNumVO);
        }
        // 如果用户信息不存在，返回失败响应
        return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(), BizCodeEnume.SYSTEM_ERROR.getMsg());
    }

    @Override
    public ResponseResult queryOrders(Map<String, Object> map) {
        String token = (String) map.get("token");
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
        if (null != ui) {
            try {
                // 发送人id
                String userId = ui.getUserId();
                // 订单状态
                Integer orderStatus = (Integer) map.get("orderStatus");
                Integer virtualGoods = (Integer) map.get("virtualGoods");
                Integer orderStatusNum = null;
                // orderStatus 为空表示调用方不按状态筛选（查全部）。必须先判空：
                // Integer 与 int 字面量比较会自动拆箱，null 时抛 NullPointerException，
                // 被外层 catch 吞掉后表现为 10012「系统异常，请联系客服」。
                if (orderStatus == null || orderStatus == 1) {
                } else if (orderStatus == 2) {
                    // 待付款
                    orderStatusNum = 1;
                } else if (orderStatus == 3) {
                    // 待发货
                    orderStatusNum = 3;
                } else if (orderStatus == 4) {
                    // 待收货
                    orderStatusNum = 4;
                } else if (orderStatus == 5) {
                    // 已取消
                    orderStatusNum = 7;
                } else if (orderStatus == 6) {
                    // 已取消
                    orderStatusNum = 16;
                }
                map.put("orderStatusNum", orderStatusNum);
                map.put("userId", userId);

                IPage<OrderListVO> page = this.baseMapper.queryOrderListByPhone(new Query<OrderListVO>().getPage(map), map);
                List<OrderListVO> orderList = page.getRecords();
                // 商品总价
                BigDecimal paymentPrice = new BigDecimal(0);
                setKdniaoConfig();
                for (OrderListVO orderListVO : orderList) {
                    //判断是否可以再申请售后
                    Integer platformInterventionId = orderListVO.getPlatformInterventionId();
                    Integer interventionState = orderListVO.getInterventionState();
                    Integer processingResults = orderListVO.getProcessingResults();
                    Integer returnMethodType = orderListVO.getReturnMethodType();
                    if (platformInterventionId != null && interventionState == 2 && processingResults == 0 && returnMethodType == 1) {
                        orderListVO.setPlatformAudit(1);
                    } else if (platformInterventionId != null && interventionState == 2 && processingResults == 0 && (returnMethodType == 2 || returnMethodType == 3)) {
                        orderListVO.setPlatformAudit(2);
                    } else {
                        orderListVO.setPlatformAudit(0);
                    }
                    Integer goodsNum = 0;
                    String orderId = orderListVO.getOrderId();
                    List<GoodsDetailsListVO> goodsDetailsList = orderGoodsMapper.queryGoodsList(orderId);
                    orderListVO.setGoodsDetailsList(goodsDetailsList);
                    // 退款状态
                    Integer orderRefundStatus = orderListVO.getOrderRefundStatus();
                    // 退货状态
                    Integer orderReturnStatus = orderListVO.getOrderReturnStatus();
                    // 换货状态
                    Integer orderBarterStatus = orderListVO.getOrderBarterStatus();
                    // 订单状态
                    Integer normalStatus = orderListVO.getOrderStatus();
                    // 1.线上支付  2.线下支付 3.货到付款
                    Integer paymentCode = orderListVO.getPaymentCode();
                    Integer status = null;
                    if (normalStatus.equals(CommonType.orderStatus.TO_BE_SHIPPED.getCode()) || (null != virtualGoods && 0 == virtualGoods && CommonType.orderStatus.SHIPPED.getCode().equals(normalStatus))) {
                        // 未发货状态
                        if (orderRefundStatus.equals(CommonType.orderRefundStatus.REFUNDING.getCode()) || orderRefundStatus.equals(CommonType.orderRefundStatus.REFUND_FAILED.getCode())) {
                            // 未发货退款中
                            status = 22;
                        } else if (orderRefundStatus.equals(CommonType.orderRefundStatus.REFUND_COMPLETED.getCode())) {
                            // 商家拒绝或审核通过，继续发货，但不可再次申请退款
                            status = 26;
                        } else if (orderRefundStatus.equals(CommonType.orderRefundStatus.NO_REFUND.getCode()) && null != virtualGoods && 0 == virtualGoods) {
                            status = normalStatus;
                        } else {
                            status = 3;
                        }
                    } else if (normalStatus.equals(CommonType.orderStatus.CONFIRM_RECEIPT.getCode())) {
                        // 已完成状态
                        if (orderRefundStatus.equals(CommonType.orderRefundStatus.REFUNDING.getCode()) || orderRefundStatus.equals(CommonType.orderRefundStatus.REFUND_FAILED.getCode())) {
                            // 收货退款中
                            status = 23;
                        } else if (orderRefundStatus.equals(CommonType.orderRefundStatus.REFUND_COMPLETED.getCode())) {
                            status = 25;
                        } else if (orderRefundStatus.equals(CommonType.orderRefundStatus.NO_REFUND.getCode()) &&
                                orderReturnStatus.equals(CommonType.orderReturnStatus.NO_RETURNS.getCode()) &&
                                orderBarterStatus.equals(CommonType.orderBarterStatus.NO_BARTER.getCode())) {
                            status = 6;
                        } else if (orderReturnStatus.equals(CommonType.orderReturnStatus.RETURNING_GOODS.getCode()) || orderReturnStatus.equals(CommonType.orderReturnStatus.RETURN_FAILED.getCode())) {
                            // 收货退货中
                            status = 24;
                        } else if (orderReturnStatus.equals(CommonType.orderReturnStatus.RETURN_COMPLETED.getCode())) {
                            status = 25;
                        } else if (orderReturnStatus.equals(CommonType.orderReturnStatus.RETURN_COMPLETED.getCode())) {
                            status = 25;
                        } else if(orderBarterStatus.equals(CommonType.orderBarterStatus.BARTERING.getCode()) ||
                                orderBarterStatus.equals(CommonType.orderBarterStatus.BARTER_FAILED.getCode())){
                            // 换货中
                            status = 29;
                        } else if(orderBarterStatus.equals(CommonType.orderBarterStatus.BARTER_COMPLETED.getCode())){
                            // 换货完成
                            status = 30;
                        }else {
                            status = 6;
                        }
                        if (null != orderListVO.getOrderLastReturnTime() && DateUtils.compareDate(orderListVO.getOrderLastReturnTime(), DateUtils.getNowDateToString())) {
                            // 订单超过完成时间不可售后
                            status = 27;
                        }
                    } else {
                        status = normalStatus;
                    }
                    // 存放最新状态
                    orderListVO.setOrderStatus(status);
                    if (null != paymentCode && 2 == paymentCode) {
                        //查询线下支付的审核状态
                        OdbPaymentVocherVO PaymentVocherPo = orderPaymentVocherMapper.queryPaymentVocher(orderId);
                        if (null != PaymentVocherPo) {
                            orderListVO.setReviewStatus(PaymentVocherPo.getReviewStatus());
                        }
                    }
                    for (GoodsDetailsListVO goodsDetailsListVO : goodsDetailsList) {
                        // 计算共有几件商品
                        Integer goodsnum = goodsDetailsListVO.getOrderGoodsNum();
                        goodsNum += goodsnum;
                        orderListVO.setGoodsNum(goodsNum);
                        //若为待付款订单，对限时秒杀商品添加距离付款时间
                        if (1 == status) {
                            Integer goodsId = goodsDetailsListVO.getGoodsId();
                            Integer seckillId = this.baseMapper.queryCountForSeckillGoods(goodsId);
                            if (null != seckillId && seckillId > 0) {
                                //取消时间 单位分钟
                                Integer cancelTime = this.baseMapper.queryCancelTime(seckillId);
                                if (null != cancelTime && cancelTime > 0) {
                                    try {
                                        Date orderCreateTimeDate = orderListVO.getOrderCreateTime();
                                        Calendar cal = Calendar.getInstance();
                                        cal.setTime(orderCreateTimeDate);
                                        cal.add(Calendar.MINUTE, cancelTime);
                                        Date orderCancelTime = cal.getTime();
                                        if (new Date().before(orderCancelTime)) {
                                            Long overplusTime = orderCancelTime.getTime();
                                            if (orderListVO.getOrderCancelTime() == null) {
                                                orderListVO.setOrderCancelTime(overplusTime);
                                            } else if (orderListVO.getOrderCancelTime() > overplusTime) {
                                                orderListVO.setOrderCancelTime(overplusTime);
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                        // 获取订单的最新物流信息
                        List<Integer> statusList = Arrays.asList(4, 16);
                        if (goodsDetailsListVO.getShipmentStatus() != 0 && statusList.contains(normalStatus) && Integer.valueOf(orderListVO.getOrderSource()) != CommonType.SupplierSource.JING_DONG.getCode()) {
                            Map<String, String> delivery = setDeliveryOrder(orderId, goodsDetailsListVO.getGoodsId(),
                                    orderListVO.getOrderReceiverContact(), orderListVO.getOrderReceiverAddress());
                            goodsDetailsListVO.setDelivery(delivery);
                        }
                        // 计算商品总价
                        // 单个商品价格
                        BigDecimal orderGoodsAmount = goodsDetailsListVO.getOrderGoodsAmount();
                        paymentPrice = paymentPrice.add(orderGoodsAmount).setScale(2, RoundingMode.UP);
                    }
                    /**
                     * 如果订单购买ID不为空，则查询拼团标记实体
                     * 这里解释了为什么需要查询拼团标记实体，即为了获取拼团剩余数量信息
                     */
                    if (orderListVO.getOrderBuyId() != null) {
                        /**
                         * 调用activityServiceClient的querySurplusNum方法，传入订单购买ID
                         * 该方法用于查询指定拼团的剩余数量
                         */
                        ResponseResult orderPintuanMarkEntity = activityServiceClient.querySurplusNum(orderListVO.getOrderBuyId());
                        /**
                         * 检查查询结果是否为空，非空则将数据转换为整型，并设置到orderListVO对象中
                         * 这里解释了为什么要进行空检查，因为查询结果可能为空，需要避免空指针异常
                         */
                        if (!ObjectUtils.isEmpty(orderPintuanMarkEntity.getData())) {
                            Integer numberesultemaining = Integer.valueOf(orderPintuanMarkEntity.getData().toString());
                            orderListVO.setNumberRemaining(numberesultemaining);
                        }
                    }

                    // 每家店铺总计价格
                    orderListVO.setShopOrderTotalPrice(paymentPrice);
                    // 退款实际价格
                    // 总付款价格
                    orderListVO.setRefundRealPrice(orderListVO.getOrderGoodsAmount());

                    orderListVO.setOrderTotalPrice(orderListVO.getOrderPaymentAmount());

                }
                page.setRecords(orderList);
                return ResponseResult.success(PageUtils.getPage(page));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(), BizCodeEnume.SYSTEM_ERROR.getMsg());
    }


    /**
     * 设置快递鸟配置
     * 本方法通过调用基础服务客户端查询配置接口，获取快递鸟API的配置信息，并将其设置到快递鸟查询API中
     */
    private void setKdniaoConfig(){
        //获取快递鸟的配置
        ResponseResult expressResp = baseServiceClient.queryConfigFeign();
        // 将查询到的配置数据转换为快递鸟配置DTO对象
        BaseKdniaoConfigDTO expressQueryByKdNiao = JSON.parseObject(JSON.toJSONString(expressResp.getData()), BaseKdniaoConfigDTO.class);
        // 设置快递鸟查询API的配置参数
        KdniaoTrackQueryAPI.AppKey = expressQueryByKdNiao.getAppKey();
        KdniaoTrackQueryAPI.EBusinessID = expressQueryByKdNiao.getEBussinessID();
        KdniaoTrackQueryAPI.instructCode = expressQueryByKdNiao.getInstructCode().toString();
    }

    /**
     * 设置配送订单信息并查询物流跟踪数据
     *
     * @param orderId 订单ID
     * @param orderReceiverContact 收件人联系方式
     * @param orderReceiverAddress 收件人地址
     * @return 包含物流信息的映射
     */
    private Map<String, String> setDeliveryOrder(String orderId, Integer goodsId, String orderReceiverContact, String orderReceiverAddress){
        Map<String, String> delivery;
        // 查询与订单ID关联的所有运输实体
        List<OrderShipmentEntity> shipmentList = orderShipmentMapper.selectListByGoodsId(orderId, goodsId.toString());
        List<String> list = new ArrayList<>();
        for (OrderShipmentEntity entity : shipmentList) {
            //获取快递简称
            ResponseResult shipperResp = baseServiceClient.getBaseExpressInfoById(entity.getOrderShippingExpressId());
            Map<String, String> shipperInfo = JSONObject.parseObject(JSON.toJSONString(shipperResp.getData()), Map.class);
            Map<String, Object> requestDataMap = new HashMap<>();
            //获取快递的简称
            String shipperCode = shipperInfo.get("expressPinyin");
            // 将物流公司的编码放入请求数据中
            requestDataMap.put("ShipperCode", shipperCode);
            // 将物流单号放入请求数据中
            requestDataMap.put("LogisticCode", entity.getOrderShippingCode());
            // 如果是顺丰快递，则需要提供客户名称,一般用手机号后四位表示
            if (entity.getOrderShippingCode().startsWith("SF")) {
                //获取收件人手机号的后4位
                String CustomerName = orderReceiverContact.substring(orderReceiverContact.length() - 4);
                requestDataMap.put("CustomerName", CustomerName);
            }
            // 收货详细地址
            requestDataMap.put("Receiver", orderReceiverAddress);
            // 发货地址
            requestDataMap.put("Sender", entity.getSenderAddress());
            try {
                // 调用快递鸟的接口获取物流信息（地图版）
                String returnStr = KdniaoTrackQueryAPI.getOrderTracesAndMap(requestDataMap);
                list.add(returnStr);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 处理并获取最新的物流信息
        delivery = KdniaoTrackQueryAPI.latestLogistics(list.toString());
        return delivery;
    }

    @Override
    public ResponseResult queryOrderByEvaluation(Map<String, Object> map) {
        String token = (String) map.get("token");
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
        if (null != ui) {
            String userId = ui.getUserId();
            // 待评价
            Integer waitingEvaluation = 0;
            // 已完成
            Integer offTheStocks = 6;
            map.put("userId", userId);
            map.put("waitingEvaluation", waitingEvaluation);
            map.put("offTheStocks", offTheStocks);
            IPage<OrderListVO> page = this.baseMapper.queryWaitingEvaluationOrder(new Query<OrderListVO>().getPage(map), map);
            List<OrderListVO> orderList = page.getRecords();
            BigDecimal paymentPrice = new BigDecimal(0);
            for (int i = 0; i < orderList.size(); i++) {
                Integer goodsNum = 0;
                String orderId = orderList.get(i).getOrderId();
                // 查询待评价商品信息
                List<GoodsDetailsListVO> goodsDetailsList = orderGoodsMapper.queryGoodsList(orderId);
                orderList.get(i).setGoodsDetailsList(goodsDetailsList);
                // 退款状态
                Integer orderRefundStatus = orderList.get(i).getOrderRefundStatus();
                // 退货状态
                Integer orderReturnStatus = orderList.get(i).getOrderReturnStatus();
                // 订单状态
                Integer normalStatus = orderList.get(i).getOrderStatus();
                // 涉及退款退货订单状态
                Integer status = null;
                if (normalStatus == 3) {
                    // 未发货状态
                    if (orderRefundStatus == 1) {
                        // 未发货退款中
                        status = 22;
                    } else if (orderRefundStatus == 2) {
                        // 商家拒绝或审核通过，继续发货，但不可再次申请退款
                        status = 26;
                    } else if (orderRefundStatus == 0) {
                        status = 3;
                    }
                } else if (normalStatus == 6) {
                    // 已完成状态
                    if (orderRefundStatus == 1) {
                        // 收货退款中
                        status = 23;
                    } else if (orderRefundStatus == 2) {
                        status = 25;
                    } else if (orderRefundStatus == 0 && orderReturnStatus == 0) {
                        status = 6;
                    } else if (orderReturnStatus == 1) {
                        // 收货退货中
                        status = 24;
                    } else if (orderReturnStatus == 2) {
                        status = 25;
                    } else if (orderReturnStatus == 0 && orderRefundStatus == 0) {
                        status = 6;
                    }
                } else {
                    status = normalStatus;
                }
                // 存放最新状态
                orderList.get(i).setOrderStatus(status);
                for (int j = 0; j < goodsDetailsList.size(); j++) {
                    // 计算共有几件商品
                    Integer goodsnum = goodsDetailsList.get(j).getOrderGoodsNum();
                    goodsNum += goodsnum;
                    orderList.get(i).setGoodsNum(goodsNum);

                    // 计算商品总价
                    // 单个商品价格
                    BigDecimal orderGoodsAmount = goodsDetailsList.get(j).getOrderGoodsAmount();
                    paymentPrice = paymentPrice.add(orderGoodsAmount).setScale(2, BigDecimal.ROUND_UP);

                }
                // 退款实际价格
                orderList.get(i).setRefundRealPrice(orderList.get(i).getOrderGoodsAmount());
                // 总付款价格
                orderList.get(i).setOrderTotalPrice(orderList.get(i).getOrderPaymentAmount());
            }
            page.setRecords(orderList);
            return ResponseResult.success(PageUtils.getPage(page));
        } else {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
    }

    /**
     * 根据传入的参数查询虚拟订单信息。
     *
     * @param map 包含查询条件和其他必要信息的Map对象。
     * @return 返回查询结果的ResponseResult对象。
     */
    @Override
    public ResponseResult virtualOrders(Map<String, Object> map) {
        // 从map中获取token
        String token = (String) map.get("token");
        // 检查token是否存在
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (!isExistToken) {
            // 如果token不存在，返回失败结果
            return ResponseResult.fail(10086, "token失效，请重新获取！");
        }
        try {
            // 通过token获取用户信息
            UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
            // 将用户信息中的shopId转换为Integer类型
            Integer shopId = Integer.valueOf(ui.getShopId());
            // 初始化订单取消状态
            Integer isCancel = null;
            // 设置订单时间
            setOrderTime(map);
            // 如果isCancel存在于map中，将其转换为Integer类型
            if (map.get("isCancel") != null) {
                isCancel = Integer.parseInt(map.get("isCancel").toString());
            }
            // 从map中获取其他查询条件
            String userName = (String) map.get("userName");
            String orderNum = (String) map.get("orderNum");
            String goodsName = (String) map.get("goodsName");
            String orderStartTime = (String) map.get("orderStartTime");
            String orderEndTime = (String) map.get("orderEndTime");
            // 将map中的status转换为Integer类型
            Integer status = Integer.parseInt(map.get("status") + "");
            // 根据status设置不同的查询条件
            String allOrderStatus = null;
            Integer otherOrderStatus = null;
            if (status == 1) {
                allOrderStatus = "' '";
            } else if (status == 2) {
                // 待付款
                otherOrderStatus = 1;
            } else if (status == 3) {
                // 已付款
                otherOrderStatus = 3;
            } else if (status == 4) {
                // 已发货
                otherOrderStatus = 4;
            } else if (status == 5) {
                // 已完成
                otherOrderStatus = 6;
            } else if (status == 6) {
                // 已取消
                otherOrderStatus = 7;
            } else if (status == 12) {
                // 虚拟订单 已过期
                otherOrderStatus = 12;
            }
            // 创建分页对象
            Page<OrderDetailsVO> page = new Page<>(Integer.parseInt(map.get("page") + ""), Integer.parseInt(map.get("limit") + ""));
            // 更新map中的查询条件
            map.put("shopId", shopId);
            map.put("virtualGoods", 0);
            map.put("isCancel", isCancel);
            map.put("allOrderStatus", allOrderStatus);
            map.put("otherOrderStatus", otherOrderStatus);
            map.put("userName", userName);
            map.put("orderNum", orderNum);
            map.put("goodsName", goodsName);
            map.put("orderStartTime", orderStartTime);
            map.put("orderEndTime", orderEndTime);
            // 根据更新后的查询条件查询虚拟订单基础信息
            IPage<OrderDetailsVO> orderDetails = this.baseMapper.queryVirtualOrderBase(page, map);
            // 处理查询结果，设置订单的最新状态
            for (OrderDetailsVO record : orderDetails.getRecords()) {
                String orderId = record.getOrderId();
                // 退款状态
                Integer orderRefundStatus = record.getOrderRefundStatus();
                // 退货状态
                Integer orderReturnStatus = record.getOrderReturnStatus();
                // 订单状态
                Integer normalStatus = record.getOrderStatus();
                // 计算订单的最新状态
                Integer Orderstatus = null;
                if (normalStatus == 3) {
                    // 未发货状态
                    if (orderRefundStatus == 1) {
                        // 未发货退款中
                        Orderstatus = 22;
                    } else if (orderRefundStatus == 2) {
                        // 商家拒绝或审核通过，继续发货，但不可再次申请退款
                        Orderstatus = 26;
                    } else if (orderRefundStatus == 0) {
                        Orderstatus = 3;
                    }
                } else if (normalStatus == 6) {
                    // 已完成状态
                    if (orderRefundStatus == 1) {
                        // 收货退款中
                        Orderstatus = 23;
                    } else if (orderRefundStatus == 2) {
                        Orderstatus = 25;
                    } else if (orderReturnStatus == 1) {
                        // 收货退货中
                        Orderstatus = 24;
                    } else {
                        Orderstatus = normalStatus;
                    }
                } else {
                    Orderstatus = normalStatus;
                }
                // 更新订单状态
                record.setOrderStatus(Orderstatus);
                // 根据订单ID查询商品详情
                List<GoodsDetailsVO> queryOrderDetails = this.baseMapper.queryOrders(orderId);
                // 如果商品详情为空，返回失败结果
                if (queryOrderDetails == null) {
                    return ResponseResult.fail(2, "订单为空！");
                }
                // 设置订单的商品详情
                record.setGoodsDetails(queryOrderDetails);
            }
            // 返回查询结果
            return ResponseResult.success(PageUtils.getPage(orderDetails));
        } catch (Exception e) {
            // 如果查询过程中出现异常，返回失败结果
            return ResponseResult.fail(500, "SoldOrdersErrorResults:" + e.getMessage());
        }
    }
    /**
     * 查询所有虚拟订单 导出
     *
     * @param map 包含查询条件和其他必要信息的Map对象。
     * @return 返回查询结果的ResponseResult对象。
     */
    @Override
    public ResponseResult virtualOrdersExp(Map<String, Object> map) {
        // 从map中获取token
        String token = (String) map.get("token");
        // 检查token是否存在
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (!isExistToken) {
            // 如果token不存在，返回失败结果
            return ResponseResult.fail(10086, "token失效，请重新获取！");
        }
        try {
            // 通过token获取用户信息
            UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
            // 将用户信息中的shopId转换为Integer类型
            Integer shopId = Integer.valueOf(ui.getShopId());
            // 初始化订单取消状态
            Integer isCancel = null;
            // 设置订单时间
            setOrderTime(map);
            // 如果isCancel存在于map中，将其转换为Integer类型
            if (map.get("isCancel") != null) {
                isCancel = Integer.parseInt(map.get("isCancel").toString());
            }
            // 从map中获取其他查询条件
            String userName = (String) map.get("userName");
            String orderNum = (String) map.get("orderNum");
            String goodsName = (String) map.get("goodsName");
            String orderStartTime = (String) map.get("orderStartTime");
            String orderEndTime = (String) map.get("orderEndTime");
            // 将map中的status转换为Integer类型
            Integer status = Integer.parseInt(map.get("status") + "");
            // 根据status设置不同的查询条件
            String allOrderStatus = null;
            Integer otherOrderStatus = null;
            if (status == 1) {
                allOrderStatus = "' '";
            } else if (status == 2) {
                // 待付款
                otherOrderStatus = 1;
            } else if (status == 3) {
                // 已付款
                otherOrderStatus = 3;
            } else if (status == 4) {
                // 已发货
                otherOrderStatus = 4;
            } else if (status == 5) {
                // 已完成
                otherOrderStatus = 6;
            } else if (status == 6) {
                // 已取消
                otherOrderStatus = 7;
            } else if (status == 12) {
                // 虚拟订单 已过期
                otherOrderStatus = 12;
            }
            // 更新map中的查询条件
            map.put("shopId", shopId);
            map.put("virtualGoods", 0);
            map.put("isCancel", isCancel);
            map.put("allOrderStatus", allOrderStatus);
            map.put("otherOrderStatus", otherOrderStatus);
            map.put("userName", userName);
            map.put("orderNum", orderNum);
            map.put("goodsName", goodsName);
            map.put("orderStartTime", orderStartTime);
            map.put("orderEndTime", orderEndTime);
            // 根据更新后的查询条件查询虚拟订单基础信息
            List<OrderExportVO> orderDetails = this.baseMapper.queryVirtualOrderBaseExp(map);
            String[] title = {"订单号", "订单状态", "买家昵称",  "实付款", "商品名称",
                    "商家编码", "销售属性", "单价", "数量", "优惠", "下单时间", "付款时间", "买家留言"};
            List<Map<String, String>> list = Lists.newArrayList();
            // 处理查询结果，设置订单的最新状态
            for (OrderExportVO orderExportVO : orderDetails) {
                Integer orderStatus = orderExportVO.getOrderStatus();
                Integer orderRefundStatus = orderExportVO.getOrderRefundStatus();
                String statusName = null;
                if (orderStatus == 1) {
                    statusName = "待付款";
                } else if (orderStatus == 4) {
                    if(orderRefundStatus == 1){
                        statusName = "退款申请中";
                    }else if(orderRefundStatus == 2){
                        statusName = "退款完成";
                    }else if(orderRefundStatus == 3){
                        statusName = "退款不通过";
                    }else if(orderRefundStatus == 0 && orderExportVO.getIsCancel() == 2 ){
                        statusName = "待核销";
                    }else{
                        statusName = "待核销";
                    }
                } else if (orderStatus == 6) {
                    statusName = "已核销";
                } else if (orderStatus == 7) {
                    statusName = "已取消";
                } else if (orderStatus == 8) {
                    statusName = "退款中";
                } else if (orderStatus == 9) {
                    statusName = "已退款";
                }else if (orderStatus == 12) {
                    statusName = "已过期";
                }
                HashMap<String, String> mPut = Maps.newHashMap();
                mPut.put("订单号", orderExportVO.getOrderId());
                mPut.put("订单状态",statusName);
                mPut.put("买家昵称", orderExportVO.getBuyerUserName());
                mPut.put("实付款", orderExportVO.getOrderPaymentAmount().toString());
                mPut.put("商品名称", orderExportVO.getGoodsName());
                mPut.put("商家编码", orderExportVO.getShopId());
                mPut.put("销售属性", orderExportVO.getOrderSpecChineseValue());
                mPut.put("单价", orderExportVO.getGoodsPrice().toString());
                mPut.put("数量", orderExportVO.getOrderGoodsNum().toString());
                mPut.put("优惠", orderExportVO.getOrderDiscountFee() == null ? "0" : orderExportVO.getOrderDiscountFee().toString());
                mPut.put("下单时间", orderExportVO.getOrderCreateTime());
                mPut.put("付款时间", orderExportVO.getPaymentTime());
                mPut.put("买家留言", orderExportVO.getOrderMessage());
                list.add(mPut);
            }
            Map<String, List<Map<String, String>>> m = Maps.newHashMap();
            m.put("虚拟订单", list);
            String data = ExcelsUtil.createExcel(title, m, new int[]{0});
            return ResponseResult.success(data);
        } catch (Exception e) {
            // 如果查询过程中出现异常，返回失败结果
            return ResponseResult.fail(500, "SoldOrdersErrorResults:" + e.getMessage());
        }
    }
    /**
     * 根据用户token和订单状态查询门禁订单。
     *
     * @param map 包含token、订单状态等查询参数的映射。
     * @return 返回查询结果的响应对象。
     */
    @Override
    public ResponseResult queryGateShopOrders(Map<String, Object> map) {
        // 从映射中获取token
        String token = (String) map.get("token");
        // 检查token在Redis中的存在性
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        // 如果token不存在，则返回失败响应
        if (!isExistToken) {
            return ResponseResult.fail(10086, "token失效，请重新获取！");
        }

        // 使用token获取用户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
        // 从映射中获取订单状态
        // 订单状态
        Integer orderStatus = (Integer) map.get("orderStatus");
        // 初始化订单状态编号
        Integer orderStatusNum = null;
        // 根据订单状态设置订单状态编号
        if (orderStatus == 1) {
            // 全部订单
        } else if (orderStatus == 2) {
            // 待付款
            orderStatusNum = 1;
        } else if (orderStatus == 3) {
            // 待自提
            orderStatusNum = 4;
        } else if (orderStatus == 4) {
            // 以自提
            orderStatusNum = 6;
        }
        // 将订单状态编号和用户自提点ID添加到映射中
        map.put("orderStatusNum", orderStatusNum);
        map.put("orderGateShopId", ui.getShopGateId());
        // 创建分页对象
        Page<GateShopOrdersVO> page = new Page<>((Integer) map.get("page"), (Integer) map.get("limit"));
        // 执行门禁订单查询
        IPage<GateShopOrdersVO> orderList = this.baseMapper.queryGateShopOrders(page, map);
        // 构建并返回成功响应，包含分页信息
        return ResponseResult.success(PageUtils.getPage(orderList));
    }

    /**
     * 商家取消订单接口
     * 通过传入的token和核销码，检查商家是否有权限取消订单，并查询订单的核销状态。
     *
     * @param map 包含token和核销码的Map对象
     * @return 根据操作结果返回相应的响应结果
     */
    @Override
    public ResponseResult webGateShopCancelOrder(Map<String, Object> map) {
        // 从map中获取token
        String token = (String) map.get("token");
        // 检查token在Redis中的存在性，判断token是否失效
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (!isExistToken) {
            return ResponseResult.fail(10086, "token失效，请重新获取！");
        }
        // 从map中获取核销码
        String redeemCode = (String) map.get("redeemCode");

        // 通过token获取用户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
        try {
            // 检查商家是否有权限取消订单
            if (null == ui.getShopGateId()) {
                // 裸码 10086 已认领为 AUTH 域 30801「登录失败，请重新登录」，
                // 前端据此清 token 跳登录页 —— 没权限会被误当成没登录。改用 PERMISSION 域 31005。
                return ResponseResult.fail(PermissionErrorCode.PERMISSION_ACCESS_DENIED.code(),
                        PermissionErrorCode.PERMISSION_ACCESS_DENIED.meta().userMessage());
            }
            // 根据核销码和商家ID查询订单详情
            Map<String, Object> stringObjectMap = this.baseMapper.queryGateShopCancelOrderDetails(redeemCode, ui.getShopGateId());
            // 检查查询到的订单详情是否为空
            if (null == stringObjectMap) {
                return ResponseResult.fail(2, "抱歉，该核销码不是本店的！");
            }
            // 根据订单的核销状态返回相应的结果
            // 是否核销  0是 1否 2已过期
            if (1 == (Long) stringObjectMap.get("is_cancel")) {
                return ResponseResult.success("未核销", stringObjectMap);
            } else if (0 == (Long) stringObjectMap.get("is_cancel")) {
                return ResponseResult.success("订单已核销", stringObjectMap);
            } else if (2 == (Long) stringObjectMap.get("is_cancel")) {
                return ResponseResult.success("订单过期", stringObjectMap);
            }
            return ResponseResult.fail(500, "系统错误", stringObjectMap);
        } catch (Exception e) {
            e.printStackTrace();
            // 捕获异常，返回失败结果并包含异常信息
            return ResponseResult.fail(500, e.getMessage());
        }
    }


    /**
     * 取消门禁店铺订单。
     *
     * @param map 包含订单相关信息的Map对象，其中必须包含token和redeemCode。
     * @return 返回操作结果，成功或失败以及相应的信息。
     */
    @Override
    public ResponseResult cancelGateShopOrder(Map<String, Object> map) {
        // 从map中获取token
        String token = (String) map.get("token");
        // 检查token在Redis中的存在性
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        // 如果token不存在，则返回失败结果
        if (!isExistToken) {
            return ResponseResult.fail(10086, "token失效，请重新获取！");
        }
        // 通过token获取用户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
        try {
            // 检查用户是否有权限操作
            if (null == ui.getShopGateId()) {
                // 裸码 10086（登录语义）→ PERMISSION 域 31005，理由同上
                return ResponseResult.fail(PermissionErrorCode.PERMISSION_ACCESS_DENIED.code(),
                        PermissionErrorCode.PERMISSION_ACCESS_DENIED.meta().userMessage());
            }
            // 获取当前时间，用于记录订单取消时间
            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            // 从map中获取核销码
            String redeemCode = (String) map.get("redeemCode");
            // 根据核销码和店铺门禁ID查询订单详情
            OrderDetailsVO orderDetails = this.baseMapper.queryGateShopOrderDetails(redeemCode, ui.getShopGateId());
            // 如果订单正在退款中，返回提示信息，无法核销
            if (null != orderDetails.getOrderRefundStatus() && 1 == orderDetails.getOrderRefundStatus()) {
                return ResponseResult.success("此订单退款中 无法核销！",2);
            }
            // 如果订单已退款完成，返回提示信息，无法核销
            if (null != orderDetails.getOrderRefundStatus() && 2 == orderDetails.getOrderRefundStatus()) {
                return ResponseResult.success("此订单退款完成 无法核销！",2);
            }
            // 如果订单已经取消，则返回失败结果
            if (orderDetails.getIsCancel() == 0) {
                return ResponseResult.success("此订单已经核销！",2);
            }
            // 更新订单为取消状态，并记录取消时间
            Integer integer = this.baseMapper.updateGateShopOrderCancel(redeemCode, currentTime, orderDetails.getOrderId(), ui.getShopGateId());
            // 如果更新成功，则返回成功结果
            if (integer > 0) {
                return ResponseResult.success(1);
            }
            // 如果更新失败，则返回失败结果
            return ResponseResult.success( "操作失败",2);
        } catch (Exception e) {
            // 记录异常信息
            log.info("JsonProcessingExceptionError:" + e.getMessage());
            // 返回异常信息对应的失败结果
            return ResponseResult.fail(500, e.getMessage());
        }
    }


    /**
     * 根据传入的参数查询虚拟门禁店铺订单。
     *
     * @param map 包含查询参数的映射，包括token、status、page和limit等。
     * @return 返回查询结果的ResponseResult对象。
     */
    @Override
    public ResponseResult queryVirtualGateShopOrders(Map<String, Object> map) {
        // 从map中获取token并转换为字符串
        String token = String.valueOf(map.get("token"));
        // 使用token解析获取用户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
        // 如果用户信息为空，则返回token失效的错误信息
        if (ui == null) {
            return ResponseResult.fail(10086, "token失效，请重新获取！");
        }
        try {
            // 检查用户是否有店铺，如果没有则返回无权限的错误信息
            if (null == ui.getShopId()) {
                // 裸码 10086（登录语义）→ PERMISSION 域 31005，理由同上
                return ResponseResult.fail(PermissionErrorCode.PERMISSION_ACCESS_DENIED.code(),
                        PermissionErrorCode.PERMISSION_ACCESS_DENIED.meta().userMessage());
            }
            // 从map中获取订单状态，并根据状态映射为系统认可的状态值
            // 订单状态
            Integer status = (Integer) map.get("status");
            Integer otherOrderStatus = null;
            if (status == 1) {
            } else if (status == 2) {
                // 待付款
                otherOrderStatus = 1;
            } else if (status == 3) {
                // 已付款
                otherOrderStatus = 3;
            } else if (status == 4) {
                // 已发货
                otherOrderStatus = 4;
            } else if (status == 5) {
                // 已完成
                otherOrderStatus = 6;
            } else if (status == 6) {
                // 已取消
                otherOrderStatus = 7;
            }
            // 将处理后的状态值和其他必要参数放入map中
            map.put("otherOrderStatus", otherOrderStatus);
            map.put("orderGateShopId", ui.getShopGateId());
            // 创建分页对象
            Page<VirtualGateShopOrderVO> page = new Page<>(Integer.parseInt(map.get("page") + ""),
                    Integer.parseInt(map.get("limit") + ""));
            // 调用baseMapper查询订单列表
            IPage<VirtualGateShopOrderVO> orderList = this.baseMapper.queryVirtualGateShopOrders(page, map);
            // 将查询结果包装成ResponseResult对象返回
            return ResponseResult.success(PageUtils.getPage(orderList));
        } catch (Exception e) {
            // 记录异常信息，并返回服务器错误的ResponseResult对象
            log.info("SoldOrdersErrorResults:" + e.getMessage());
            return ResponseResult.fail(500, e.getMessage());
        }
    }

    @Override
    public ResponseResult shopGateTakeOrder(Map<String, Object> map) {
        String token = (String) map.get("token");
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (!isExistToken) {
            return ResponseResult.fail(10086, "token失效，请重新获取！");
        }
        try {
            UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
            Integer shopId = Integer.valueOf(ui.getShopId());
            Integer status = Integer.parseInt(map.get("status") + "");
            Integer isDelivery = null;
            if (map.get("isDelivery") != null) {
                isDelivery = Integer.parseInt(map.get("isDelivery") + "");
            }
            boolean isDeliveryFlag = isDelivery != null && isDelivery == 1;
            String allOrderStatus = null;
            Integer otherOrderStatus = null;

            if (status == 1) {
                allOrderStatus = "' '";
                if (isDeliveryFlag) {
                    map.put("distributorId", 0);
                }
            } else if (status == 2) {
                // 待付款
                otherOrderStatus = 1;
                map.put("orderReturnStatus", 0);
                map.put("orderRefundStatus", 0);
                if (isDeliveryFlag) {
                    map.put("pickup", 0);
                    map.put("distributorId", 0);
                }
            } else if (status == 3) {
                // 已付款
                otherOrderStatus = 3;
                map.put("orderReturnStatus", 0);
                map.put("orderRefundStatus", 0);
                if (isDeliveryFlag) {
                    // 待接单
                    map.put("pickup", 0);
                    map.put("distributorId", 0);
                }
            } else if (status == 4) {
                // 已发货
                otherOrderStatus = 4;
                map.put("orderReturnStatus", 0);
                map.put("orderRefundStatus", 0);
                if (isDeliveryFlag) {
                    // 待取货
                    otherOrderStatus = 3;
                    map.put("pickup", 0);
                    map.put("distributorId", 1);
                }
            } else if (status == 5) {
                // 已完成
                otherOrderStatus = 6;
                map.put("orderReturnStatus", 0);
                map.put("orderRefundStatus", 0);
                if (isDeliveryFlag) {
                    // 待配送
                    otherOrderStatus = 3;
                    map.put("pickup", 1);
                    map.put("distributorId", 1);
                }
            } else if (status == 6) {
                // 已取消
                otherOrderStatus = 7;
                map.put("orderReturnStatus", 0);
                map.put("orderRefundStatus", 0);
                if (isDeliveryFlag) {
                    // 配送中
                    otherOrderStatus = 4;
                    map.put("pickup", 1);
                    map.put("distributorId", 1);
                }
            } else if (status == 7) {
                // 退货中
                map.put("orderReturnStatus", 1);
                if (isDeliveryFlag) {
                    // 已完成
                    otherOrderStatus = 6;
                    map.put("orderReturnStatus", 0);
                    map.put("orderRefundStatus", 0);
                    map.put("pickup", 1);
                    map.put("distributorId", 1);
                }
            } else if (status == 8) {
                // 退款中
                map.put("orderRefundStatus", 1);
            } else if (status == 9) {
                // 退款完成
                map.put("orderRefundStatus", 2);
            } else if (status == 10) {
                // 退货完成
                map.put("orderReturnStatus", 2);
            }
            if (isDeliveryFlag) {
                map.put("orderIsShopTake", null);
            } else {
                map.put("orderIsShopTake", 1);
            }
            map.put("shopId", shopId);
            map.put("allOrderStatus", allOrderStatus);
            map.put("otherOrderStatus", otherOrderStatus);
            Page<OrderDetailsVO> page = new Page<>(Integer.parseInt(map.get("page") + ""), Integer.parseInt(map.get("limit") + ""));
            IPage<OrderDetailsVO> orderDetails = this.baseMapper.queryGateShopTakeOrderBase(page, map);
            for (int i = 0; i < orderDetails.getRecords().size(); i++) {
                String orderId = orderDetails.getRecords().get(i).getOrderId();
                // 退款状态
                Integer orderRefundStatus = orderDetails.getRecords().get(i).getOrderRefundStatus();
                // 退货状态
                Integer orderReturnStatus = orderDetails.getRecords().get(i).getOrderReturnStatus();
                // 订单状态
                Integer normalStatus = orderDetails.getRecords().get(i).getOrderStatus();
                // 涉及退款退货订单状态
                Integer Orderstatus = null;
                if (normalStatus == 3) {
                    // 未发货状态
                    if (orderRefundStatus == 1) {
                        // 未发货退款中
                        Orderstatus = 22;
                    } else if (orderRefundStatus == 2) {
                        // 商家拒绝或审核通过，继续发货，但不可再次申请退款
                        Orderstatus = 26;
                    } else if (orderRefundStatus == 0) {
                        Orderstatus = 3;
                    }
                } else if (normalStatus == 6) {
                    // 已完成状态
                    if (orderRefundStatus == 1) {
                        // 收货退款中
                        Orderstatus = 23;
                    } else if (orderRefundStatus == 2) {
                        Orderstatus = 25;
                    } else if (orderRefundStatus == 0 && orderReturnStatus == 0) {
                        Orderstatus = 6;
                    } else if (orderReturnStatus == 1) {
                        // 收货退货中
                        Orderstatus = 24;
                    } else if (orderReturnStatus == 2) {
                        Orderstatus = 25;
                    } else if (orderReturnStatus == 0 && orderRefundStatus == 0) {
                        Orderstatus = 6;
                    }
                } else {
                    Orderstatus = normalStatus;
                }
                // 存放最新状态
                orderDetails.getRecords().get(i).setOrderStatus(Orderstatus);
                List<GoodsDetailsVO> queryOrderDetails = this.baseMapper.queryOrders(orderId);
                if (queryOrderDetails == null) {
                    return ResponseResult.fail(2, "订单为空！");
                }
                orderDetails.getRecords().get(i).setGoodsDetails(queryOrderDetails);
            }
            return ResponseResult.success(PageUtils.getPage(orderDetails));
        } catch (Exception e) {
            log.info("SoldOrdersErrorResults:" + e.getMessage());
            return ResponseResult.fail(500, e.getMessage());
        }
    }
    /**
     * 查询门店订单 导出
     * @param map
     * @return
     */
    @Override
    public ResponseResult shopGateTakeOrdersExp(Map<String, Object> map) {
        String token = (String) map.get("token");
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (!isExistToken) {
            return ResponseResult.fail(10086, "token失效，请重新获取！");
        }
        try {
            UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
            Integer shopId = Integer.valueOf(ui.getShopId());
            Integer status = Integer.parseInt(map.get("status") + "");
            Integer isDelivery = null;
            if (map.get("isDelivery") != null) {
                isDelivery = Integer.parseInt(map.get("isDelivery") + "");
            }
            boolean isDeliveryFlag = isDelivery != null && isDelivery == 1;
            String allOrderStatus = null;
            Integer otherOrderStatus = null;

            if (status == 1) {
                allOrderStatus = "' '";
                if (isDeliveryFlag) {
                    map.put("distributorId", 0);
                }
            } else if (status == 2) {
                // 待付款
                otherOrderStatus = 1;
                map.put("orderReturnStatus", 0);
                map.put("orderRefundStatus", 0);
                if (isDeliveryFlag) {
                    map.put("pickup", 0);
                    map.put("distributorId", 0);
                }
            } else if (status == 3) {
                // 已付款
                otherOrderStatus = 3;
                map.put("orderReturnStatus", 0);
                map.put("orderRefundStatus", 0);
                if (isDeliveryFlag) {
                    // 待接单
                    map.put("pickup", 0);
                    map.put("distributorId", 0);
                }
            } else if (status == 4) {
                // 已发货
                otherOrderStatus = 4;
                map.put("orderReturnStatus", 0);
                map.put("orderRefundStatus", 0);
                if (isDeliveryFlag) {
                    // 待取货
                    otherOrderStatus = 3;
                    map.put("pickup", 0);
                    map.put("distributorId", 1);
                }
            } else if (status == 5) {
                // 已完成
                otherOrderStatus = 6;
                map.put("orderReturnStatus", 0);
                map.put("orderRefundStatus", 0);
                if (isDeliveryFlag) {
                    // 待配送
                    otherOrderStatus = 3;
                    map.put("pickup", 1);
                    map.put("distributorId", 1);
                }
            } else if (status == 6) {
                // 已取消
                otherOrderStatus = 7;
                map.put("orderReturnStatus", 0);
                map.put("orderRefundStatus", 0);
                if (isDeliveryFlag) {
                    // 配送中
                    otherOrderStatus = 4;
                    map.put("pickup", 1);
                    map.put("distributorId", 1);
                }
            } else if (status == 7) {
                // 退货中
                map.put("orderReturnStatus", 1);
                if (isDeliveryFlag) {
                    // 已完成
                    otherOrderStatus = 6;
                    map.put("orderReturnStatus", 0);
                    map.put("orderRefundStatus", 0);
                    map.put("pickup", 1);
                    map.put("distributorId", 1);
                }
            } else if (status == 8) {
                // 退款中
                map.put("orderRefundStatus", 1);
            } else if (status == 9) {
                // 退款完成
                map.put("orderRefundStatus", 2);
            } else if (status == 10) {
                // 退货完成
                map.put("orderReturnStatus", 2);
            }
            if (isDeliveryFlag) {
                map.put("orderIsShopTake", null);
            } else {
                map.put("orderIsShopTake", 1);
            }
            map.put("shopId", shopId);
            map.put("allOrderStatus", allOrderStatus);
            map.put("otherOrderStatus", otherOrderStatus);
            List<OrderExportVO> orderDetails = this.baseMapper.queryGateShopTakeOrderBaseExp(map);
            String[] title = {"订单号", "门店名称","订单状态", "买家昵称",  "实付款", "商品名称",
                    "商家编码", "销售属性", "单价", "数量", "优惠", "下单时间", "付款时间", "买家留言"};
            List<Map<String, String>> list = Lists.newArrayList();
            // 处理查询结果，设置订单的最新状态
            for (OrderExportVO orderExportVO : orderDetails) {
                Integer orderStatus = orderExportVO.getOrderStatus();
                Integer orderRefundStatus = orderExportVO.getOrderRefundStatus();
                String statusName = null;
                if (orderStatus == 1) {
                    statusName = "待付款";
                } else if (orderStatus == 4) {
                    if(orderRefundStatus == 1){
                        statusName = "退款申请中";
                    }else if(orderRefundStatus == 2){
                        statusName = "退款完成";
                    }else if(orderRefundStatus == 3){
                        statusName = "退款不通过";
                    }else{
                        statusName = "待自提";
                    }
                } else if (orderStatus == 6) {
                    if(orderRefundStatus == 1){
                        statusName = "退款申请中";
                    }else if(orderRefundStatus == 2){
                        statusName = "退款完成";
                    }else if(orderRefundStatus == 3){
                        statusName = "退款不通过";
                    }else{
                        statusName = "已自提";
                    }
                } else if (orderStatus == 7) {
                    statusName = "已取消";
                } else if (orderStatus == 8) {
                    statusName = "退款中";
                } else if (orderStatus == 9) {
                    statusName = "已退款";
                }
                HashMap<String, String> mPut = Maps.newHashMap();
                mPut.put("订单号", orderExportVO.getOrderId());
                mPut.put("门店名称", orderExportVO.getShopGateName());
                mPut.put("订单状态",statusName);
                mPut.put("买家昵称", orderExportVO.getBuyerUserName());
                mPut.put("实付款", orderExportVO.getOrderPaymentAmount().toString());
                mPut.put("商品名称", orderExportVO.getGoodsName());
                mPut.put("商家编码", orderExportVO.getShopId());
                mPut.put("销售属性", orderExportVO.getOrderSpecChineseValue());
                mPut.put("单价", orderExportVO.getGoodsPrice().toString());
                mPut.put("数量", orderExportVO.getOrderGoodsNum().toString());
                mPut.put("优惠", orderExportVO.getOrderDiscountFee() == null ? "0" : orderExportVO.getOrderDiscountFee().toString());
                mPut.put("下单时间", orderExportVO.getOrderCreateTime());
                mPut.put("付款时间", orderExportVO.getPaymentTime());
                mPut.put("买家留言", orderExportVO.getOrderMessage());
                list.add(mPut);
            }
            Map<String, List<Map<String, String>>> m = Maps.newHashMap();
            m.put("门店订单", list);
            String data = ExcelsUtil.createExcel(title, m, new int[]{0});
            return ResponseResult.success(data);
        } catch (Exception e) {
            log.info("SoldOrdersErrorResults:" + e.getMessage());
            return ResponseResult.fail(500, e.getMessage());
        }
    }
    /**
     * 根据核销码查询取消订单详情。
     *
     * @param paramMap 请求参数的映射，包含token和redeemCode。
     * @return 返回查询结果的响应对象。
     */
    @Override
    public ResponseResult queryCancelOrderDetails(Map<String, String> paramMap) {
        // 从请求参数中获取token和redeemCode
        String token = paramMap.get("token");
        String redeemCode = paramMap.get("redeemCode");

        // 检查token在Redis中的存在性，判断token是否失效
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (!isExistToken) {
            return ResponseResult.fail(10086, "token失效，请重新获取！");
        }

        // 通过token获取用户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);

        // 根据核销码、用户ID和店铺gateId查询取消订单详情
        Map<String, Object> stringObjectMap = this.baseMapper.queryCancelOrderDetails(redeemCode, ui.getUserId(), ui.getShopGateId());
        // 检查查询结果是否为空，为空表示该核销码不属于本店
        if (null == stringObjectMap) {
            return ResponseResult.fail(2, "抱歉，该核销码不是本店的！");
        }

        // 从查询结果中获取有效期字符串
        String periodOfValidity = (String) stringObjectMap.get("period_of_validity");
        boolean flag = false;
        // 判断订单是否过期
        if (null != periodOfValidity) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                if (new Date().after(dateFormat.parse(periodOfValidity))) {
                    flag = true;
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                log.error(e.getMessage());
            }
        }

        // 根据订单状态返回相应的成功响应，包括订单过期、未核销和已核销的情况
        // 是否核销  0是 1否 2已过期
        if (flag) {
            return ResponseResult.success(1, "订单过期", stringObjectMap);
        } else if (1 == (Long) stringObjectMap.get("is_cancel")) {
            return ResponseResult.success(1, "未核销", stringObjectMap);
        } else if (0 == (Long) stringObjectMap.get("is_cancel")) {
            return ResponseResult.success(1, "订单已核销", stringObjectMap);
        } else if (2 == (Long) stringObjectMap.get("is_cancel")) {
            return ResponseResult.success(1, "订单过期", stringObjectMap);
        }

        // 如果以上所有条件都不满足，返回系统错误的响应
        return ResponseResult.fail(500, "系统错误", stringObjectMap);
    }

    @Override
    public ResponseResult cancelOrder(Map<String, Object> map) {
        // 从请求参数中获取token
        String token = (String) map.get("token");
        // 检查token在Redis中是否存在，如果不存在则返回token失效的错误信息
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (!isExistToken) {
            return ResponseResult.fail(10086, "token失效，请重新获取！");
        }
        // 通过token获取用户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
        try {
            // 获取当前时间，用于记录订单核销时间
            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            // 从请求参数中获取兑换码
            String redeemCode = (String) map.get("redeemCode");
            // 根据兑换码和店铺ID查询订单详情
            OrderDetailsVO orderDetails = this.baseMapper.queryOrderDetails(redeemCode, ui.getShopId());
            if(!DateUtils.compareDate2(orderDetails.getPeriodOfValidity())){
                return ResponseResult.fail(2, "已过期 无法核销！");
            }
            // 如果订单正在退款中，返回提示信息，无法核销
            if (null != orderDetails.getOrderRefundStatus() && 1 == orderDetails.getOrderRefundStatus()) {
                return ResponseResult.fail(2, "此订单退款中 无法核销！");
            }
            // 如果订单已退款完成，返回提示信息，无法核销
            if (null != orderDetails.getOrderRefundStatus() && 2 == orderDetails.getOrderRefundStatus()) {
                return ResponseResult.fail(2, "此订单退款完成 无法核销！");
            }
            // 如果订单已取消（已核销），返回提示信息
            if (orderDetails.getIsCancel() == 0) {
                return ResponseResult.fail(2, "此订单已经核销！");
            }
            // 更新订单为取消状态，并记录核销时间
            Integer integer = this.baseMapper.updateOrderCancel(redeemCode, currentTime, orderDetails.getOrderId(), ui.getShopId());
            // 如果更新成功，返回操作成功的提示信息
            if (integer > 0) {
                return ResponseResult.success(1, "操作成功");
            }
            // 如果更新失败，返回操作失败的提示信息
            return ResponseResult.fail(2, "操作失败");
        } catch (Exception e) {
            // 记录异常信息
            log.info("JsonProcessingExceptionError:" + e.getMessage());
            // 返回异常信息对应的错误码和错误信息
            return ResponseResult.fail(500, e.getMessage());
        }

    }

    /**
     * 取消业务订单。
     *
     * @param map 包含订单相关信息的Map对象。
     * @return 根据操作结果返回相应的响应结果。
     * @throws TransactionException 如果交易过程中出现异常。
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public ResponseResult businessCancelOrder(Map<String, Object> map) throws TransactionException {
        // 从请求参数中获取用户令牌
        String token = (String) map.get("token");
        // 根据令牌解析用户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
        // 如果用户信息为空，则返回令牌无效的响应
        if (ui == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        try {
            // 当前时间，用于更新订单状态的时间记录
            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            // 从请求参数中获取兑换码
            String redeemCode = (String) map.get("redeemCode");
            // 根据兑换码和店铺ID查询订单详情
            OrderBaseEntity orderDetails = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderBaseEntity>()
                    .eq(OrderBaseEntity::getRedeemCode, redeemCode)
                    .eq(OrderBaseEntity::getShopId, ui.getShopId()));
            // 如果订单存在且已取消，则返回订单已取消的响应
            if (null != orderDetails && 2 == orderDetails.getIsCancel()) {
                return ResponseResult.fail(BizCodeEnume.ORDER_EXPIRE_CANNOT_BE_CANCELLED);
            }
            // 如果订单存在且处于退款中状态，则返回订单无法取消的响应
            if (null != orderDetails.getOrderRefundStatus() && CommonType.orderRefundStatus.REFUNDING.getCode().equals(orderDetails.getOrderRefundStatus())) {
                return ResponseResult.fail(BizCodeEnume.ORDER_REFUND_CANNOT_BE_CANCELLED);
            }
            // 如果订单存在且已退款完成，则返回订单无法取消的响应
            if (null != orderDetails.getOrderRefundStatus() && CommonType.orderRefundStatus.REFUND_COMPLETED.getCode().equals(orderDetails.getOrderRefundStatus())) {
                return ResponseResult.fail(BizCodeEnume.ORDER_REFUND_COMPLETED);
            }
            // 如果订单存在且已核销，则返回订单已核销的响应
            if (orderDetails.getIsCancel() == 0) {
                return ResponseResult.fail(CommonType.isCancel.NO.getDesc());
            }
            // 更新订单状态为已确认收货、取消日期为当前时间、取消状态为未取消、完成时间为当前时间、最后退货时间为当前时间
            Integer integer = this.baseMapper.update(null, new LambdaUpdateWrapper<OrderBaseEntity>()
                    .eq(OrderBaseEntity::getRedeemCode, redeemCode)
                    .eq(OrderBaseEntity::getShopId, ui.getShopId())
                    .eq(OrderBaseEntity::getOrderId, orderDetails.getOrderId())
                    .set(OrderBaseEntity::getOrderStatus, CommonType.orderStatus.CONFIRM_RECEIPT.getCode())
                    .set(OrderBaseEntity::getCancelDate, currentTime)
                    .set(OrderBaseEntity::getIsCancel, CommonType.isCancel.NO.getCode())
                    .set(OrderBaseEntity::getOrderFinishedTime, LocalDateTime.now())
                    .set(OrderBaseEntity::getOrderLastReturnTime, LocalDateTime.now()));
            // 如果更新成功，则进行积分相关操作
            if (integer > 0) {
                // 查询积分规则
                ResponseResult resultResp = baseServiceClient.queryIntegralRules();
                Map<String, Integer> integralRules = (Map<String, Integer>) resultResp.getData();
                // 计算积分值并进行会员积分更新操作
                BigDecimal jifen = orderDetails.getOrderPaymentAmount().multiply(new BigDecimal(integralRules.get("ruleConsume")));
                long l = jifen.setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
                ArrayList<String> list = new ArrayList<>();
                list.add(orderDetails.getBuyerUserId());
                MemberMessageListIntegralDTO updateMemberMessageListIntegral = new MemberMessageListIntegralDTO();
                updateMemberMessageListIntegral.setUserIdList(list);
                updateMemberMessageListIntegral.setIntegral((int) l);
                updateMemberMessageListIntegral.setType(CommonType.commonStatus.YES.getCode());
                userServiceClient.updateMemberIntegral(updateMemberMessageListIntegral);
                // 添加积分修改记录
                Map<String, Object> pointParam = new HashMap<>();
                pointParam.put("PointsLogType", CommonType.pointsLogType.GAIN.getCode());
                pointParam.put("ClassId", CommonType.pointType.PURCHASE.getCode());
                pointParam.put("PointsLogTime", DateUtils.getTime());
                pointParam.put("PointsLogDesc", CommonType.pointType.PURCHASE.getDesc());
                pointParam.put("PointsLogFlag", "reg");
                pointParam.put("UserId", orderDetails.getBuyerUserId());
                pointParam.put("orderId", orderDetails.getOrderId());
                pointParam.put("PointsLogPoints", (int) l);
                pointParam.put("PointsLogOperate", CommonType.pointType.PURCHASE.getDesc());
                pointParam.put("proportion", integralRules.get("ruleConsume"));
                userServiceClient.insertPointLogByMap(pointParam);
                // 返回操作成功的响应
                return ResponseResult.success();
            } else {
                // 如果更新失败，则返回操作失败的响应
                return ResponseResult.fail();
            }
        } catch (Exception e) {
            // 记录异常信息
            log.info("Exception:" + e.getMessage());
            // 回滚全局事务
            GlobalTransactionContext.reload(RootContext.getXID()).rollback();
            // 返回操作失败的响应，附带异常信息
            return ResponseResult.fail(e.getMessage());
        }
    }

    @Override
    public ResponseResult changeShipments(Map<Object, Object> map) {
        String token = (String) map.get("token");
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (!isExistToken) {
            return ResponseResult.fail(10086, "token失效，请重新获取！");
        }
        StringBuilder sb = new StringBuilder();
        String orderId = (String) map.get("orderId");
        Integer shipmentId = (Integer) map.get("shipmentId");
        // 物流单号
        String orderShippingCode = map.get("orderShippingCode") == null ? "" : (String) map.get("orderShippingCode");
        // 配送公司ID
        Integer orderShippingExpressId = map.get("orderShippingExpressId") == null ? null : (Integer) map.get("orderShippingExpressId");
        // 物流发货备注
        String orderShippingMessage = (String) map.get("orderShippingMessage");
        String deliveryPerson = map.get("deliveryPerson") == null ? "" : (String) map.get("deliveryPerson");
        String deliveryTelephone = map.get("deliveryTelephone") == null ? "" : (String) map.get("deliveryTelephone");
        String deliveryCertificate = map.get("deliveryCertificate") == null ? "" : (String) map.get("deliveryCertificate");
        OrderBaseEntity orderBase = this.baseMapper.selectById(orderId);
        OrderShipmentEntity shipmentEntity = orderShipmentMapper.selectOne(new LambdaQueryWrapper<OrderShipmentEntity>()
                .eq(OrderShipmentEntity::getShipmentId, shipmentId));
        List<Integer> goodsIds = JSON.parseArray(shipmentEntity.getGoodsIds(), Integer.class);
        boolean isChangeShipmentsByBaseSuccess = false;
        boolean isChangeShipmentsByGoodsSuccess = goodsIds.isEmpty();
        String shippingCode = orderBase.getOrderShippingCode();
        if (!StringUtils.isEmpty(shippingCode) && shippingCode.split(",").length > 1) {
            String[] shippingCodes = shippingCode.split(",");
            for (String code : shippingCodes) {
                if (code.equals(orderShippingCode)) {
                    sb.append(orderShippingCode).append(",");
                    continue;
                }
                sb.append(code).append(",");
            }
            Integer lastNum = sb.toString().lastIndexOf(",");
            shippingCode = (sb).substring(0, lastNum);
        } else {
            shippingCode = orderShippingCode;
        }
        for (Integer goodsId : goodsIds) {
            StringBuilder str = new StringBuilder();
            OrderGoodsEntity orderGoods = orderGoodsMapper.selectOne(new LambdaQueryWrapper<OrderGoodsEntity>()
                    .eq(OrderGoodsEntity::getOrderId, orderId)
                    .eq(OrderGoodsEntity::getGoodsId, goodsId));
            String goodsShippingCode = orderGoods.getOrderShippingCode();
            if (!StringUtils.isEmpty(goodsShippingCode) && goodsShippingCode.split(",").length > 1) {
                String[] goodsShippingCodeList = goodsShippingCode.split(",");
                for (String code : goodsShippingCodeList) {
                    if (code.equals(orderShippingCode)) {
                        str.append(orderShippingCode).append(",");
                        continue;
                    }
                    str.append(code).append(",");
                }
                Integer lastNum = str.toString().lastIndexOf(",");
                goodsShippingCode = (str).substring(0, lastNum);
            } else {
                goodsShippingCode = orderShippingCode;
            }
            if (orderShippingExpressId == null) {
                isChangeShipmentsByGoodsSuccess = orderGoodsMapper.updateChangeShipmentsByGoodsId(orderGoods.getOrderGoodsId(), goodsShippingCode, orderShippingMessage);
            } else {
                isChangeShipmentsByGoodsSuccess = orderGoodsMapper.updateChangeAllShipmentsByGoodsId(orderGoods.getOrderGoodsId(),
                        orderShippingExpressId, orderShippingCode, orderShippingMessage);
            }
        }
        if (orderShippingExpressId == null) {
            isChangeShipmentsByBaseSuccess = this.baseMapper.updateChangeShipmentsByBase(orderId, shippingCode, orderShippingMessage);
        } else {
            isChangeShipmentsByBaseSuccess = this.baseMapper.updateChangeAllShipmentsByBase(orderId, orderShippingExpressId, shippingCode, orderShippingMessage);
        }
        orderShipmentMapper.update(null, new LambdaUpdateWrapper<OrderShipmentEntity>()
                .set(!StringUtils.isEmpty(orderShippingCode), OrderShipmentEntity::getOrderShippingCode, orderShippingCode)
                .set(orderShippingExpressId != null, OrderShipmentEntity::getOrderShippingExpressId, orderShippingExpressId)
                .set(!StringUtils.isEmpty(orderShippingMessage), OrderShipmentEntity::getOrderShippingMessage, orderShippingMessage)
                .set(!StringUtils.isEmpty(deliveryPerson), OrderShipmentEntity::getDeliveryPerson, deliveryPerson)
                .set(!StringUtils.isEmpty(deliveryTelephone), OrderShipmentEntity::getDeliveryTelephone, deliveryTelephone)
                .set(!StringUtils.isEmpty(deliveryCertificate), OrderShipmentEntity::getDeliveryCertificate, deliveryCertificate)
                .eq(OrderShipmentEntity::getShipmentId, shipmentId));
        if (isChangeShipmentsByBaseSuccess && isChangeShipmentsByGoodsSuccess) {
            return ResponseResult.success(1, "更改成功！");
        }
        return ResponseResult.fail(2, "更改失败！");
    }

    @Override
    public ResponseResult queryMemberOrderDetail(Map<String, Object> param) {
        // 初始化返回标志为0，表示默认没有退换货情况
        Integer returnFlag = 0;
        // 从参数中获取token字符串
        String token = (String) param.get("token");
        // 检查token是否存在，判断用户是否已登录
        if (RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token)) {
            // 根据用户条件查询订单信息列表
            IPage<UserInfoOrderDetailVO> page = this.baseMapper.queryUserorderInfoList(new Query<UserInfoOrderDetailVO>().getPage(param),
                    param);

            // 遍历查询到的订单记录
            for (UserInfoOrderDetailVO r : page.getRecords()) {
                // 获取当前订单ID
                String orderId = r.getOrderId();
                // 根据订单ID查询订单基本信息
                OrderBaseEntity orderInfoEntity = this.baseMapper.selectById(orderId);
                // 根据订单属性设置订单类型
                Integer virtualGoods = orderInfoEntity.getVirtualGoods();
                Integer orderIsShopTake = orderInfoEntity.getOrderIsShopTake();
                if (null != orderIsShopTake && 1 == orderIsShopTake) {
                    r.setOrderType(3);
                } else {
                    if (null != virtualGoods && 0 == virtualGoods) {
                        r.setOrderType(2);
                        r.setPastDue(orderInfoEntity.getPastDue());
                        r.setIsCancel(orderInfoEntity.getIsCancel());
                    } else {
                        r.setOrderType(1);
                    }
                }
                // 设置订单的退换货状态和退款状态
                int returnStatus = Integer.valueOf(orderInfoEntity.getOrderReturnStatus());
                int refundStatus = Integer.valueOf(orderInfoEntity.getOrderRefundStatus());
                r.setReturnStatus(returnStatus);
                r.setRefundStatus(refundStatus);
                // 根据退换货金额和订单金额判断退换货标志
                if (2 == returnStatus || 2 == refundStatus) {
                    BigDecimal returnAmount = new BigDecimal(orderInfoEntity.getOrderRefundAmount());
                    BigDecimal orderAmount = orderInfoEntity.getOrderPaymentAmount();
                    if (orderAmount.compareTo(returnAmount) > 0) {
                        returnFlag = 2;
                    } else if ((orderAmount.compareTo(returnAmount) == 0)) {
                        returnFlag = 1;
                    }
                }
                // 设置退换货标志到订单详情
                r.setReturnFlag(returnFlag);
            }


            // 返回成功的响应结果，包含分页处理后的订单列表
            return ResponseResult.success(PageUtils.getPage(page));
        } else {
            // 如果token不存在，表示用户未登录，返回权限不足的错误信息
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }

    }

    @Override
    public ResponseResult separateShipments(ShipmentsDTO param) throws TransactionException {
        String token = param.getToken();
        ResponseResult rr = ParseTokenUtils.getShopUserInfo(token);
        if (rr.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return rr;
        }
        String orderId = param.getOrderId();
        // 查询订单信息 如状态，退货退款状态，收货人手机号
        OrderBaseEntity obe = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderBaseEntity>()
                .eq(OrderBaseEntity::getOrderId, orderId));
        String receiverPhone = obe.getOrderReceiverContact();
        if (obe.getOrderStatus() == 3 && obe.getOrderRefundStatus().equals("1")) {
            return ResponseResult.fail(BizCodeEnume.ORDER_AFTER_SALES.getCode(), BizCodeEnume.ORDER_AFTER_SALES.getMsg());
        } else if (obe.getOrderStatus() == 6 && obe.getOrderRefundStatus().equals("2")) {
            return ResponseResult.fail(BizCodeEnume.ORDER_AFTER_SALES_COMPLETION.getCode(), BizCodeEnume.ORDER_AFTER_SALES_COMPLETION.getMsg());
        }
        List<ShipmentsGoodsDTO> spList = param.getGoodsDetails();
        if (spList.size() == 1) {
            Map<Object, Object> map = new HashMap<Object, Object>();
            map.put("token", token);
            map.put("orderId", orderId);
            map.put("shippingAddressId", spList.get(0).getShippingAddressId());
            map.put("orderSellerName", spList.get(0).getOrderSellerName());
            map.put("orderSellerAddress", spList.get(0).getOrderSellerAddress());
            map.put("orderSellerContact", spList.get(0).getOrderSellerContact());
            map.put("orderShippingExpressId", spList.get(0).getOrderShippingExpressId());
            map.put("orderShippingCode", spList.get(0).getOrderShippingCode());
            map.put("orderShippingMessage", spList.get(0).getOrderShippingMessage());
            shipments(map);
        }
        // 配送时间
        String orderShippingTime = DateUtils.getTime();
        StringBuilder sb = new StringBuilder();
        boolean isUpdateSuccessByOrderGoods = false;
        Integer orderStatus = 4;
        String orderSellerName = null;
        String orderSellerAddress = null;
        String orderSellerContact = null;
        Integer orderShippingExpressId = null;
        String orderShippingMessage = null;
        String orderShippingCode = null;
        // 商品订单是否已经发货。1-是，0-否，默认0(orderGoods)
        Integer orderGoodsIdDeliver = 1;
        for (ShipmentsGoodsDTO shipmentsGoodsPara : spList) {
            if ("".equals(shipmentsGoodsPara.getOrderShippingCode().trim())) {
                return ResponseResult.fail(BizCodeEnume.SHOPPING_CODE_IS_NULL);
            }
            // 商品id
            Integer goodsId = shipmentsGoodsPara.getGoodsId();
            // 发货人姓名
            orderSellerName = shipmentsGoodsPara.getOrderSellerName();
            // 发货人地址
            orderSellerAddress = shipmentsGoodsPara.getOrderSellerAddress();
            // 发货人联系方式
            orderSellerContact = shipmentsGoodsPara.getOrderSellerContact();
            // 配送公司ID
            orderShippingExpressId = shipmentsGoodsPara.getOrderShippingExpressId();
            // 物流发货备注
            orderShippingMessage = shipmentsGoodsPara.getOrderShippingMessage();
            // 物流单号
            orderShippingCode = shipmentsGoodsPara.getOrderShippingCode();
            sb.append(shipmentsGoodsPara.getOrderShippingCode()).append(",");

            if (orderShippingExpressId == 47) {
                // 获取手机号后四位
                String phoneNumber = receiverPhone.substring(7, 11);
                // 顺丰快递物流号
                orderShippingCode = orderShippingCode + ":" + phoneNumber;
            }
            Map<String, Object> mapGoods = new HashMap<>();
            mapGoods.put("orderId", orderId);
            mapGoods.put("goodsId", goodsId);
            mapGoods.put("orderStatus", orderStatus);
            mapGoods.put("orderGoodsIdDeliver", orderGoodsIdDeliver);
            mapGoods.put("orderSellerName", orderSellerName);
            mapGoods.put("orderSellerAddress", orderSellerAddress);
            mapGoods.put("orderSellerContact", orderSellerContact);
            mapGoods.put("orderShippingTime", orderShippingTime);
            mapGoods.put("orderShippingExpressId", orderShippingExpressId);
            mapGoods.put("orderShippingCode", orderShippingCode);
            mapGoods.put("orderShippingMessage", orderShippingMessage);
            //发货 order_goods
            isUpdateSuccessByOrderGoods = orderGoodsMapper.updateSeparateShipmentsByOrderGoods(mapGoods);
        }
        Integer lastNum = sb.toString().lastIndexOf(",");
        String allShippingCode = (sb).substring(0, lastNum);
        Map<String, Object> mapBase = new HashMap<>();
        mapBase.put("orderId", orderId);
        mapBase.put("orderStatus", orderStatus);
        mapBase.put("orderSellerName", orderSellerName);
        mapBase.put("orderSellerAddress", orderSellerAddress);
        mapBase.put("orderSellerContact", orderSellerContact);
        mapBase.put("orderShippingTime", orderShippingTime);
        mapBase.put("orderShippingExpressId", orderShippingExpressId);
        mapBase.put("allShippingCode", allShippingCode);
        mapBase.put("orderShippingMessage", orderShippingMessage);
        //后台配置的自动确认收货时间
        String configValue = baseServiceClient.selectConfigValueFeign(CommonType.configKey.AUTOMATIC_DELIVERY_TIME.getDesc());
        Integer automaticDeliveryTime = Integer.parseInt(configValue);
        mapBase.put("automaticDeliveryTime", automaticDeliveryTime);
        //更改 order_base
        boolean isUpdateSuccessByOrderBase = this.baseMapper.updateSeparateShipmentsByOrderBase(mapBase);
        if (isUpdateSuccessByOrderGoods && isUpdateSuccessByOrderBase) {
            payMentServiceClient.changeConsumeRecord(orderId);
            UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
            this.baseMapper.updateOrderMessageProcessingStatus(ui.getShopId(), orderId);
            //获取店铺名称
            String shopName = obe.getShopName();

            //发送站内信通知
            PlatformMessageVO platformMessage = new PlatformMessageVO();
            platformMessage.setCreateTime(new Date());
            //买家id
            platformMessage.setUserId(obe.getBuyerUserId());
            platformMessage.setMessageUserName(obe.getBuyerUserName());
            platformMessage.setMessageContent("你的订单" + orderId + "已发货");
            platformMessage.setMessageTitle("订单发货提醒");
            platformMessage.setCreater(shopName);
            this.baseMapper.insertMessage(platformMessage);

            return ResponseResult.success();
        }
        return ResponseResult.fail(BizCodeEnume.UPDATE_ERROR);
    }

    @Override
    public ResponseResult updateAddressInformation(Map<Object, Object> map) {
        String token = map.get("token").toString();
        ResponseResult rr = ParseTokenUtils.getShopUserInfo(token);
        if (rr.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return rr;
        }
        String orderId = (String) map.get("orderId");
        String orderReceiverName = (String) map.get("orderReceiverName");
        String orderReceiverAddress = (String) map.get("orderReceiverAddress");
        String orderReceiverContact = (String) map.get("orderReceiverContact");
        Map<String, Object> mapInfo = new HashMap<>();
        mapInfo.put("orderId", orderId);
        mapInfo.put("orderReceiverName", orderReceiverName);
        mapInfo.put("orderReceiverAddress", orderReceiverAddress);
        mapInfo.put("orderReceiverContact", orderReceiverContact);
        boolean changeAddressInformation = this.baseMapper.updateAddressInformation(mapInfo);
        if (changeAddressInformation) {
            return ResponseResult.success(BizCodeEnume.UPDATE_SUCCEEDED);
        }
        return ResponseResult.fail(BizCodeEnume.UPDATE_ERROR);
    }

    @Override
    public ResponseResult queryAddressInformation(Map<Object, Object> map) {
        // 从传入的map中获取订单ID
        String orderId = (String) map.get("orderId");
        // 根据订单ID查询订单基本信息
        OrderBaseEntity entity = this.baseMapper.selectById(orderId);
        if (entity != null) {
            // 调用基础服务客户端，根据订单收货地址的省、市、
            String address = null;
            ResponseResult addressString = baseServiceClient.getAddressString(entity.getOrderReceiverProvinceId(), entity.getOrderReceiverCityId(), entity.getOrderReceiverAreaId());
            if(null != addressString.getData()){
                address = addressString.getData().toString();
            }else {
                address = entity.getOrderReceiverAddress();

            }
            // 将获取到的地址信息更新到订单实体的收货详情字段
            entity.setOrderReceiverDetails(address);
            // 返回成功响应，包含更新后的订单实体
            return ResponseResult.success(entity);
        }
        // 如果订单实体为空，返回失败响应，指示订单不存在
        return ResponseResult.fail(BizCodeEnume.ORDER_IS_NULL.getCode(), BizCodeEnume.ORDER_IS_NULL.getMsg());

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult importShipmentsOrderExport(MultipartFile file, String token) throws TransactionException {
        // 解析excel数据
        ResponseResult res = ExcelsUtil.readExcel(file);
        if (res.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return res;
        }
        List<Map<Object, Object>> mapList = (List<Map<Object, Object>>) res.getData();
        if (!mapList.isEmpty()) {
            for (Map<Object, Object> map : mapList) {
                if (null == map.get("shippingAddressId") || "".equals(map.get("shippingAddressId").toString())) {
                    TransactionManagerHolder.get().rollback(RootContext.getXID());
                    return ResponseResult.fail(BizCodeEnume.SHIPPING_ADDRESS_IS_NULL);
                }
                if (null == map.get("orderSellerName") || "".equals(map.get("orderSellerName").toString())) {
                    TransactionManagerHolder.get().rollback(RootContext.getXID());
                    return ResponseResult.fail(BizCodeEnume.CONSIGNOR_NAME_IS_NULL);
                }
                if (null == map.get("orderSellerAddress") || "".equals(map.get("orderSellerAddress").toString())) {
                    TransactionManagerHolder.get().rollback(RootContext.getXID());
                    return ResponseResult.fail(BizCodeEnume.CONSIGNOR_ADDRESS_IS_NULL);
                }
                if (null == map.get("orderSellerContact") || "".equals(map.get("orderSellerContact").toString())) {
                    TransactionManagerHolder.get().rollback(RootContext.getXID());
                    return ResponseResult.fail(BizCodeEnume.CONSIGNOR_TEL_IS_NULL);
                }
                if (null == map.get("orderShippingExpressId") || "".equals(map.get("orderShippingExpressId").toString())) {
                    TransactionManagerHolder.get().rollback(RootContext.getXID());
                    return ResponseResult.fail(BizCodeEnume.EXPRESS_ID_IS_NULL);
                }
                if (null == map.get("goodsIds") || "".equals(map.get("goodsIds").toString())) {
                    TransactionManagerHolder.get().rollback(RootContext.getXID());
                    return ResponseResult.fail(BizCodeEnume.GOODS_ID_IS_NULL);
                }
                if (null == map.get("goodsNums") || "".equals(map.get("goodsNums").toString())) {
                    TransactionManagerHolder.get().rollback(RootContext.getXID());
                    return ResponseResult.fail(BizCodeEnume.GOODS_NUM_IS_NULL);
                }
                if (null == map.get("orderShippingCode") || "".equals(map.get("orderShippingCode").toString())) {
                    TransactionManagerHolder.get().rollback(RootContext.getXID());
                    return ResponseResult.fail(BizCodeEnume.ORDER_AFTER_SALES_COMPLETION);
                }
                // 批量发货 赋值物流配送
                map.put("shipmentType", CommonType.shipmentType.LOGISTICS_DELIVERY.getCode());
                map.put("token", token);
                ResponseResult resp = piLiangFahuoShipments(map, CommonType.commonStatus.YES.getCode());
                if (CommonStatusEnum.SUCCESS.getCode() != resp.getCode()) {
                    return resp;
                }
            }
            return ResponseResult.success(CommonStatusEnum.SUCCESS.getCode(), "发货成功");
        }
        return ResponseResult.fail(2, "excel文件错误");
    }

    @Override
    public ResponseResult batchShipmentsOrderExport(Map<String, Object> map, HttpServletResponse response) {
        String token = map.get("token").toString();
        UserInfoVO userInfoVO = ParseTokenUtils.getUserInfoByToken(token);
        if (userInfoVO == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        Integer id = 1;
        try {
            //批量 获取选中的orderIds  获取订单信息 下载表格
            List<ShipmentsOrderDetailDTO> orderList = this.baseMapper.batchShipmentsOrderExport(map);
            String[] title = {"ID", "订单号", "订单创建时间", "支付单号", "支付方式", "支付时间", "商品名称", "采购数量", "已发货数量",
                    "配送公司ID", "配送公司名字", "物流单号", "本次发货量", "收货人姓名", "收货人详细地址", "收货人联系方式",
                    "订单应付金额", "商品Id", "商品单价", "发货人名称", "发货人地址", "发货人手机号", "发货地址id"};
            List<Map<String, String>> list = Lists.newArrayList();
            for (ShipmentsOrderDetailDTO shipmentsOrderDetailDTO : orderList) {
                Integer paymentCode = shipmentsOrderDetailDTO.getPaymentCode();
                String paymentCodeName = null;
                if (paymentCode == 1) {
                    paymentCodeName = "余额支付";
                } else if (paymentCode == 2) {
                    paymentCodeName = "支付宝支付";
                } else if (paymentCode == 3) {
                    paymentCodeName = "微信支付";
                } else if (paymentCode == 4) {
                    paymentCodeName = "通联支付";
                } else if (paymentCode == 5) {
                    paymentCodeName = "健康账户余额支付";
                } else if (paymentCode == 6) {
                    paymentCodeName = "购物卡支付";
                } else if (paymentCode == 7) {
                    paymentCodeName = "充值卡+支付宝";
                } else if (paymentCode == 8) {
                    paymentCodeName = "充值卡+微信";
                } else if (paymentCode == 9) {
                    paymentCodeName = "蜂币支付";
                } else if (paymentCode == 10) {
                    paymentCodeName = "蜂币+支付宝";
                } else if (paymentCode == 11) {
                    paymentCodeName = "蜂币+微信";
                } else if (paymentCode == 12) {
                    paymentCodeName = "线下支付（上传凭证)";
                } else {
                    paymentCodeName = "未知";
                }
                shipmentsOrderDetailDTO.setPaymentCodeName(paymentCodeName);

                HashMap<String, String> mPut = Maps.newHashMap();
                mPut.put("ID", String.valueOf(id));
                mPut.put("订单号", shipmentsOrderDetailDTO.getOrderId());
                mPut.put("订单创建时间", shipmentsOrderDetailDTO.getOrderCreateTime());
                mPut.put("支付单号", shipmentsOrderDetailDTO.getPaymentNumber());
                mPut.put("支付方式", shipmentsOrderDetailDTO.getPaymentCodeName());
                mPut.put("支付时间", shipmentsOrderDetailDTO.getPaymentTime());
                mPut.put("配送公司ID", map.get("expressId").toString());
                mPut.put("配送公司名字", map.get("expressName").toString());
                mPut.put("物流单号", null);
                mPut.put("收货人姓名", shipmentsOrderDetailDTO.getOrderReceiverName());
                mPut.put("收货人详细地址", shipmentsOrderDetailDTO.getOrderReceiverAddress());
                mPut.put("收货人联系方式", shipmentsOrderDetailDTO.getOrderReceiverContact());
                mPut.put("订单应付金额", shipmentsOrderDetailDTO.getOrderPaymentAmount().toString());
                mPut.put("商品Id", shipmentsOrderDetailDTO.getGoodsId().toString());
                mPut.put("商品名称", shipmentsOrderDetailDTO.getGoodsName());
                mPut.put("采购数量", shipmentsOrderDetailDTO.getOrderGoodsNum().toString());
                mPut.put("已发货数量", shipmentsOrderDetailDTO.getShipmentQuantity().toString());
                mPut.put("本次发货量", null);
                mPut.put("商品单价", shipmentsOrderDetailDTO.getGoodsPrice().toString());
                mPut.put("发货人名称", map.get("orderSellerName").toString());
                mPut.put("发货人地址", map.get("orderSellerAddress").toString());
                mPut.put("发货人手机号", map.get("orderSellerContact").toString());
                mPut.put("发货地址id", map.get("shippingAddressId").toString());

                list.add(mPut);
                id++;
            }
            Workbook workBook = ExcelsUtil.createWorkBook();
            Sheet sheet = ExcelsUtil.createSheet(workBook, "待发货订单");
            ExcelsUtil.writeSheet(sheet, title, list);

            String fileName = String.valueOf(System.currentTimeMillis() / 1000);
            String base64Str = ExcelsUtil.getFileBase64URLString(workBook, fileName);
            return ResponseResult.success(base64Str);
        } catch (Exception e) {
            log.info("orderExportErrorResults:" + e.getMessage());
            return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED.getCode(), BizCodeEnume.OPERATION_FAILED.getMsg());
        }
    }

    @Override
    public ResponseResult storageBatchShipmentsOrderExport(Map<String, Object> map, HttpServletResponse response) {
        String token = map.get("token").toString();
        UserInfoVO userInfoVO = ParseTokenUtils.getUserInfoByToken(token);
        if (userInfoVO == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        Integer id = 1;
        // 查询仓储是否开启
        String storageStart = baseServiceClient.selectConfigValueByKey(CommonType.configKey.STORAGE_SWITCH.getDesc());
        List<String> titleList = new ArrayList<>(Arrays.asList("ID", "订单号", "订单创建时间", "支付单号", "支付方式", "支付时间", "商品名称", "采购数量", "已发货数量",
                "配送公司ID", "配送公司名字", "物流单号", "本次发货量", "收货人姓名", "收货人详细地址", "收货人联系方式",
                "订单应付金额", "商品Id", "商品单价", "发货人名称", "发货人地址", "发货人手机号", "发货地址id"));
        if (null != storageStart && "1".equals(storageStart)) {
            titleList.add("仓库");
            titleList.add("仓库id");
        }
        String[] title = titleList.stream().toArray(String[]::new);

        //批量 获取选中的orderIds  获取订单信息 下载表格
        List<ShipmentsOrderDetailDTO> orderList = this.baseMapper.batchShipmentsOrderExport(map);

        List<List<StorageVO>> storageList = new ArrayList<>();
        List<Map<String, String>> list = Lists.newArrayList();
        List<String> goodsNameList = new ArrayList<>();
        for (ShipmentsOrderDetailDTO shipmentsOrderDetailDTO : orderList) {
            Integer paymentCode = shipmentsOrderDetailDTO.getPaymentCode();
            String paymentCodeName = null;
            if (paymentCode == 1) {
                paymentCodeName = "余额支付";
            } else if (paymentCode == 2) {
                paymentCodeName = "支付宝支付";
            } else if (paymentCode == 3) {
                paymentCodeName = "微信支付";
            } else if (paymentCode == 4) {
                paymentCodeName = "通联支付";
            } else if (paymentCode == 5) {
                paymentCodeName = "健康账户余额支付";
            } else if (paymentCode == 6) {
                paymentCodeName = "购物卡支付";
            } else if (paymentCode == 7) {
                paymentCodeName = "充值卡+支付宝";
            } else if (paymentCode == 8) {
                paymentCodeName = "充值卡+微信";
            } else if (paymentCode == 9) {
                paymentCodeName = "蜂币支付";
            } else if (paymentCode == 10) {
                paymentCodeName = "蜂币+支付宝";
            } else if (paymentCode == 11) {
                paymentCodeName = "蜂币+微信";
            } else if (paymentCode == 12) {
                paymentCodeName = "线下支付（上传凭证)";
            } else {
                paymentCodeName = "未知";
            }
            shipmentsOrderDetailDTO.setPaymentCodeName(paymentCodeName);
            HashMap<String, String> mPut = Maps.newHashMap();
            mPut.put("ID", String.valueOf(id));
            mPut.put("订单号", shipmentsOrderDetailDTO.getOrderId());
            mPut.put("订单创建时间", shipmentsOrderDetailDTO.getOrderCreateTime());
            mPut.put("支付单号", shipmentsOrderDetailDTO.getPaymentNumber());
            mPut.put("支付方式", shipmentsOrderDetailDTO.getPaymentCodeName());
            mPut.put("支付时间", shipmentsOrderDetailDTO.getPaymentTime());
            mPut.put("配送公司ID", map.get("expressId").toString());
            mPut.put("配送公司名字", map.get("expressName").toString());
            mPut.put("物流单号", null);
            mPut.put("收货人姓名", shipmentsOrderDetailDTO.getOrderReceiverName());
            mPut.put("收货人详细地址", shipmentsOrderDetailDTO.getOrderReceiverAddress());
            mPut.put("收货人联系方式", shipmentsOrderDetailDTO.getOrderReceiverContact());
            mPut.put("订单应付金额", shipmentsOrderDetailDTO.getOrderPaymentAmount().toString());
            mPut.put("商品Id", shipmentsOrderDetailDTO.getGoodsId().toString());
            mPut.put("商品名称", shipmentsOrderDetailDTO.getGoodsName());
            mPut.put("采购数量", shipmentsOrderDetailDTO.getOrderGoodsNum().toString());
            mPut.put("已发货数量", shipmentsOrderDetailDTO.getShipmentQuantity().toString());
            mPut.put("本次发货量", null);
            mPut.put("商品单价", shipmentsOrderDetailDTO.getGoodsPrice().toString());
            mPut.put("发货人名称", map.get("orderSellerName").toString());
            mPut.put("发货人地址", map.get("orderSellerAddress").toString());
            mPut.put("发货人手机号", map.get("orderSellerContact").toString());
            mPut.put("发货地址id", map.get("shippingAddressId").toString());
            list.add(mPut);
            id++;

            if (null != storageStart && "1".equals(storageStart)) {
                // 如果开启仓储 要查询订单的发货订单的仓库信息
                // 根据商品id 查询商品的仓储信息
                ResponseResult responseResult = goodServiceClient.queryGoodsStorageByGoodsId(shipmentsOrderDetailDTO.getGoodsId());
                if (null != responseResult && responseResult.getCode() == CommonStatusEnum.SUCCESS.getCode()) {
                    List<StorageVO> tmpInfo = JSON.parseArray(JSON.toJSONString(responseResult.getData()), StorageVO.class);
                    storageList.add(tmpInfo);
                } else {
                    goodsNameList.add(shipmentsOrderDetailDTO.getGoodsName());
                }
            }
        }
        if (null != storageStart && "1".equals(storageStart) && goodsNameList.size() > 0) {
            String result = String.join(", ", goodsNameList);
            return ResponseResult.fail(BizCodeEnume.GOODS_NOT_STOCK_INFO.getCode(), BizCodeEnume.GOODS_NOT_STOCK_INFO.getMsg() + result);
        }
        // 这里替换为新的excel工具类 用来实现级联的关系
        try (Workbook workBook = ExcelsUtil.createWorkBook()) {
            Sheet sheet = ExcelsUtil.createSheet(workBook, "待发货订单");
            ExcelsUtil.writeSheet(sheet, title, list);
            if (null != storageStart && "1".equals(storageStart)) {
                // 设置仓库信息关联下拉列表关系 key为选择列 value为映射列
                ExcelsUtil.createLinkageValidation(
                        workBook,
                        storageList,
                        new String[]{"仓库", "仓库id"},
                        true,
                        sheet,
                        list.size(),
                        'X',
                        'Y'
                );
            }

            // 返回文件URL
//            String fileUrl = ExcelsUtil.getFileUrl(workBook);

            String fileName = String.valueOf(System.currentTimeMillis() / 1000);
            String base64Str = ExcelsUtil.getFileBase64URLString(workBook, fileName);
            return ResponseResult.success(base64Str);
        } catch (Exception e) {
            log.info("orderExportErrorResults:" + e.getMessage());
            return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED.getCode(), BizCodeEnume.OPERATION_FAILED.getMsg());
        }
    }

    /**
     * 根据商家信息查询未发货的订单。
     * <p>
     * 本方法用于接收商家的查询请求，通过商家的令牌获取商家信息，然后查询对应商家未发货的订单。
     * 如果获取商家信息失败，则直接返回错误结果；否则，根据商家ID查询订单，并返回查询结果。
     *
     * @param param 查询参数，包含商家令牌、页码和每页数量等信息。
     * @return 返回查询结果，包含分页信息和订单列表。
     */
    @Override
    public ResponseResult queryUnshippedOrder(RetrunQueryPrintDTO param) {
        // 通过令牌获取商家信息
        ResponseResult rr = ParseTokenUtils.getShopUserInfo(param.getToken());
        // 如果获取商家信息失败，直接返回错误结果
        if (rr.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return rr;
        }
        // 设置分页默认值，防止前端未传参导致NPE
        Integer page = param.getPage() != null ? param.getPage() : 1;
        Integer limit = param.getLimit() != null ? param.getLimit() : 10;
        // 初始化分页参数
        //根据店铺查询 未发货的订单
        Page<ManageOrderbaseListVO> pageObj = new Page<>(page, limit);
        // 根据商家ID和分页参数查询未发货的订单
        IPage<ManageOrderbaseListVO> list = this.baseMapper.selectOrderbaseByShopIdParam(pageObj, param);
        // 返回查询结果
        return ResponseResult.success(PageUtils.getPage(list));
    }

    @Override
    public ResponseResult shippingOrderExport(Map<String, Object> map, HttpServletResponse response) {
        String token = map.get("token").toString();
        ResponseResult rr = ParseTokenUtils.getShopUserInfo(token);
        if (rr.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return rr;
        }
        UserInfoVO ui = JSON.parseObject(JSON.toJSONString(rr.getData()), UserInfoVO.class);
        String shopId = ui.getShopId();
        // 买家
        String userName = (String) map.get("userName");
        // 订单号
        String orderNum = (String) map.get("orderNum");
        // 下单开始时间
        String orderStartTime = (String) map.get("orderStartTime");
        // 下单结束时间
        String orderEndTime = (String) map.get("orderEndTime");
        // 发货状态
        Integer status = Integer.parseInt(map.get("status").toString());
        Map<String, Object> mapExport = new HashMap<>();
        mapExport.put("shopId", shopId);
        mapExport.put("status", status);
        mapExport.put("userName", userName);
        mapExport.put("orderNum", orderNum);
        mapExport.put("orderStartTime", orderStartTime);
        mapExport.put("orderEndTime", orderEndTime);
        List<SoldOrderExportDTO> orderList = this.baseMapper.queryShippingExportParams(mapExport);
        String[] title = {"订单号", "买家昵称", "收件人姓名", "手机", "电话", "省", "市", "区", "地址", "邮编", "运费", "实付款", "商品名称",
                "商家编码", "销售属性", "单价", "数量", "优惠", "下单时间", "付款时间", "买家留言", "卖家备注"};
        List<Map<String, String>> list = Lists.newArrayList();
        for (SoldOrderExportDTO soldOrderExportPara : orderList) {
            Integer orderStatus = soldOrderExportPara.getOrderStatus();
            String statusName = null;
            if (orderStatus == 1) {
                statusName = "待付款";
            } else if (orderStatus == 2) {
                statusName = "待配货";
            } else if (orderStatus == 3) {
                statusName = "待发货";
            } else if (orderStatus == 4) {
                statusName = "已发货";
            } else if (orderStatus == 5) {
                statusName = "已签收";
            } else if (orderStatus == 6) {
                statusName = "已完成";
            } else if (orderStatus == 7) {
                statusName = "已取消";
            } else if (orderStatus == 8) {
                statusName = "退款中";
            } else if (orderStatus == 9) {
                statusName = "退款完成";
            }
            soldOrderExportPara.setOrderStatusName(statusName);
            HashMap<String, String> mPut = Maps.newHashMap();
            mPut.put("订单号", soldOrderExportPara.getOrderId());
            mPut.put("买家昵称", soldOrderExportPara.getBuyerUserName());
            mPut.put("收件人姓名", soldOrderExportPara.getOrderReceiverName());
            mPut.put("手机", soldOrderExportPara.getOrderReceiverContact());
            mPut.put("电话", null);
            mPut.put("省", soldOrderExportPara.getProvince());
            mPut.put("市", soldOrderExportPara.getCity());
            mPut.put("区", soldOrderExportPara.getArea());
            mPut.put("地址", soldOrderExportPara.getOrderReceiverAddress());
            mPut.put("邮编", null);

            mPut.put("运费", soldOrderExportPara.getOrderShippingFee().toString());
            mPut.put("实付款", soldOrderExportPara.getOrderPaymentAmount().toString());
            mPut.put("商品名称", soldOrderExportPara.getGoodsName());
            mPut.put("商家编码", soldOrderExportPara.getShopId());
            mPut.put("销售属性", soldOrderExportPara.getOrderSpecChineseValue());
            mPut.put("单价", soldOrderExportPara.getGoodsPrice().toString());
            mPut.put("数量", soldOrderExportPara.getOrderGoodsNum().toString());
            mPut.put("优惠", soldOrderExportPara.getOrderDiscountFee().toString());

            mPut.put("下单时间", soldOrderExportPara.getOrderCreateTime());
            mPut.put("付款时间", soldOrderExportPara.getPaymentTime());
            mPut.put("买家留言", soldOrderExportPara.getOrderMessage());
            mPut.put("卖家备注", soldOrderExportPara.getOrderSellerMessage());
            list.add(mPut);
        }
        Map<String, List<Map<String, String>>> m = Maps.newHashMap();
        m.put("合并数据", list);
        String url = ExcelsUtil.createExcel(title, m, new int[]{0});/* 此处数组为需要合并的列，可能有的需求是只需要某些列里面相同内容合并 */
        return ResponseResult.success(url);
    }

    @Override
    public ResponseResult queryRecycleBin(Map<String, Object> map) {
        // 从请求参数中获取token
        String token = (String) map.get("token");
        // 检查token在Redis中的存在性，确保用户会话的有效性
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        // 如果token不存在，则返回错误响应，提示用户重新获取token
        if (!isExistToken) {
            return ResponseResult.fail(10086, "token失效，请重新获取！");
        }
        try {
            // 从请求参数中获取orderId
            String orderId = (String) map.get("orderId");
            // 如果orderId为空或为空字符串，则返回错误响应，提示orderId不能为空
            if (orderId == null || orderId == "") {
                return ResponseResult.fail(2, "orderId不能为null");
            }
            // 根据orderId查询回收站中的商品详情
            List<GoodsDetailsVO> goodsDetails = this.baseMapper.queryRecycleBin(orderId);
            // 返回查询结果的成功响应
            return ResponseResult.success(goodsDetails);
        } catch (Exception e) {
            // 捕获查询过程中的异常，打印异常堆栈，并返回错误响应，包含异常信息
            e.printStackTrace();
            return ResponseResult.fail(500, e.getMessage());
        }

    }

    @Override
    public ResponseResult queryLogisticsMessage(Map<String, Object> map) throws Exception {
        String token = (String) map.get("token");
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (!isExistToken) {
            return ResponseResult.fail(10086, "token失效，请重新获取！");
        }
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
        if (null == ui.getShopId()) {
            // 裸码 10086（登录语义）→ PERMISSION 域 31005，理由同上
            return ResponseResult.fail(PermissionErrorCode.PERMISSION_ACCESS_DENIED.code(),
                    PermissionErrorCode.PERMISSION_ACCESS_DENIED.meta().userMessage());
        }

        //定义返回对象
        List<LogisticsMessageVO> shippingLists = new ArrayList<>();
        String orderId = (String) map.get("orderId");
        OrderBaseEntity entity = this.baseMapper.selectById(orderId);
        List<LogisticsMessageVO> shippingList = orderShipmentMapper.queryLogisticsMessage(orderId);

        OrderBaseEntity orderBase = orderBaseMapper.selectOne(new LambdaQueryWrapper<OrderBaseEntity>()
                .eq(OrderBaseEntity::getOrderId, orderId));

        for (LogisticsMessageVO logisticsMessageVO : shippingList) {
            List<Integer> goodsIds = JSON.parseArray(logisticsMessageVO.getGoodsIds(), Integer.class);
            List<Integer> goodsNums = JSON.parseArray(logisticsMessageVO.getGoodsNums(), Integer.class);
            List<ShippingGoodsMessageVO> sgm = new ArrayList<>();
            if (entity.getOrderType() == 2) {
                sgm = this.baseMapper.querySupplierGoodsMessage(goodsIds, orderId);
            } else {
                sgm = this.baseMapper.queryGoodsMessage(goodsIds, orderId, Integer.valueOf(ui.getShopId()));
            }
            for (int i = 0; i < sgm.size(); i++) {
                sgm.get(i).setShipmentNum(goodsNums.get(i));
            }
            // 自主配送 不查询快递鸟
            if (CommonType.shipmentType.LOGISTICS_DELIVERY.getCode().equals(logisticsMessageVO.getShipmentType())) {
                if(orderBase.getOrderSource() == CommonType.SupplierSource.JING_DONG.getCode()){
                    // 判断是否是京东的订单
                    Map<String, Object> stringObjectMap = goodServiceClient.queryJingDongLogistics(orderBase.getJdOrderId());
                    logisticsMessageVO.setShippingName(stringObjectMap.get("deliveryCarrier").toString());
                    logisticsMessageVO.setOrderShippingCode( stringObjectMap.get("deliveryOrderId").toString());
                    logisticsMessageVO.setGoodsMessage(sgm);
                    logisticsMessageVO.setShippingInfo(JSON.toJSONString(stringObjectMap.get("logisticsList")));

                }else{
                    Integer expId = logisticsMessageVO.getOrderShippingExpressId();
                    String logisticCode = logisticsMessageVO.getOrderShippingCode();
                    // 查询物流名称
                    String expName = this.baseMapper.queryExpressNameById(expId);
                    logisticsMessageVO.setShippingName(expName);
                    logisticsMessageVO.setGoodsMessage(sgm);
                    String shippingCode = logisticsMessageVO.getOrderShippingCode();
                    logger.info("---------开始查询运单号为{}的物流信息", shippingCode);
                    Map<String, String> resultMap = getShipperCode2(logisticsMessageVO.getShipmentId());
                    if (expId != 0) {
                        logisticsMessageVO.setShippingName(resultMap.get("expressName"));
                    }
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
                    requestDataMap.put("Sender", logisticsMessageVO.getSenderAddress());
                    // 如果是顺丰快递，则需要提供客户名称,一般用手机号后四位表示
                    if (logisticCode.startsWith("SF")) {
                        String CustomerName = resultMap.get("CustomerName");
                        requestDataMap.put("CustomerName", CustomerName);
                    }

                    String returnStr = KdniaoTrackQueryAPI.getOrderTracesAndMap(requestDataMap);
                    if (StringUtils.isNotEmpty(returnStr)) {
                        logisticsMessageVO.setShippingInfo(returnStr);
                    }
                    logger.info("---------查询运单号为{}的物流信息结束,物流信息：{}", shippingCode, returnStr);
                }
            }
            //如果对应供应商的商品未发货，即商品信息为空,删除对应信息
            if (!CollectionUtils.isEmpty(sgm)) {
                shippingLists.add(logisticsMessageVO);
            }
        }
        return ResponseResult.success(shippingLists);
    }

    /**
     * 提醒用户支付的接口。
     *
     * @param map 包含用户token和订单ID的信息。
     * @return 返回支付提醒的结果。
     */
    @Override
    public ResponseResult remindPay(Map<String, Object> map) {
        // 从请求参数中获取用户token
        String token = (String) map.get("token");
        // 检查token在Redis中的存在性，判断用户会话是否有效
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (!isExistToken) {
            // 如果token无效，返回失败结果并提示用户重新获取token
            return ResponseResult.fail(10086, "token失效，请重新获取！");
        }
        // 使用token获取用户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
        // 检查用户是否有权限操作，即是否有店铺ID
        if (null == ui.getShopId()) {
            // 如果无权限，返回失败结果并提示用户没有权限
            // 裸码 10086（登录语义）→ PERMISSION 域 31005，理由同上
            return ResponseResult.fail(PermissionErrorCode.PERMISSION_ACCESS_DENIED.code(),
                    PermissionErrorCode.PERMISSION_ACCESS_DENIED.meta().userMessage());
        }

        // 从请求参数中获取订单ID
        String orderId = (String) map.get("orderId");
        // 根据订单ID查询用户的电话号码等信息
        Map<String, String> maps = this.baseMapper.queryUserPhone2(orderId);
        // 调用第三方服务，发送发货成功提醒短信给用户
        // 商家端发货成功提醒短信
        thirdPartyServiceClient.goodsObligation(maps);
        // 构建站内信信息，提醒用户订单未支付
        // 发送站内信通知
        PlatformMessageVO platformMessage = new PlatformMessageVO();
        platformMessage.setCreateTime(new Date());
        // 买家id
        platformMessage.setUserId(maps.get("buyerUserId"));
        platformMessage.setMessageUserName(maps.get("buyer_user_name"));
        platformMessage.setMessageContent("待支付提醒：您刚购买的商品订单" + orderId + "未支付成功，订单即将取消，请尽快支付~");
        platformMessage.setMessageTitle("待支付提醒");
        platformMessage.setCreater(String.valueOf(maps.get("shopName")));
        // 将站内信信息插入数据库
        this.baseMapper.insertMessage(platformMessage);
        // 返回成功结果
        return ResponseResult.success();
    }

    /**
     * 根据用户令牌查询订单发货信息。
     *
     * @param map 包含查询参数的映射，包括用户令牌、用户名、订单号、订单状态等。
     * @return 返回查询结果的响应对象，包含发货信息列表和分页信息。
     * @throws ParseException 如果解析令牌失败抛出此异常。
     */
    @Override
    public ResponseResult shipmentsOrder(Map<String, Object> map) throws ParseException {
        // 从请求参数中提取用户令牌
        String token = map.get("token").toString();
        // 根据令牌解析用户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
        // 如果用户信息为空，表示令牌无效，返回错误响应
        if (ui == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }

        // 定义查询开始时间和服务名常量
        String Seller_Trade_Deliver = "Seller_Trade_Deliver";
        // 将用户信息中的店铺ID转换为整型
        Integer shopId = Integer.valueOf(ui.getShopId());
        // 检查用户是否有权限查询
        if (containLimits(ui, Seller_Trade_Deliver)) {
            // 设置订单查询的时间范围
            setOrderTime(map);
            // 从请求参数中提取用户名、订单号、订单状态等信息
            // 买家
            String userName = (String) map.get("userName");
            // 订单号
            String orderNum = (String) map.get("orderNum");
            // 下单开始时间
            String orderStartTime = Objects.equals(map.get("orderStartTime"), "") ? null : (String) map.get("orderStartTime");
            // 下单结束时间
            String orderEndTime = Objects.equals(map.get("orderEndTime"), "") ? null : (String) map.get("orderEndTime");
            // 发货状态
            Integer status = Integer.parseInt(map.get("status") + "");
            // 根据订单状态准备查询条件
            String allOrderStatus = null;
            Integer otherOrderStatus = null;
            if (status == 1) {
                allOrderStatus = "1";
            } else {
                otherOrderStatus = status;
            }
            // 分页参数
            Integer currentPage = Integer.parseInt(map.get("page") + "");
            Integer pageSize = Integer.parseInt(map.get("limit") + "");
            String plusOrder = String.valueOf(map.get("plusOrder"));
            // 初始化分页对象
            Page<ShipmentsByOrderBaseVO> page = new Page<>(currentPage, pageSize);
            // 准备查询条件的映射
            Map<String, Object> mapBase = new HashMap<>();
            mapBase.put("shopId", shopId);
            mapBase.put("allOrderStatus", allOrderStatus);
            mapBase.put("otherOrderStatus", otherOrderStatus);
            mapBase.put("userName", userName);
            mapBase.put("orderNum", orderNum);
            mapBase.put("orderStartTime", orderStartTime);
            mapBase.put("orderEndTime", orderEndTime);
            mapBase.put("orderSource", 2);
            mapBase.put("plusOrder", plusOrder);
            // 执行查询
            IPage<ShipmentsByOrderBaseVO> orderBaseIPage = this.baseMapper.queryShipmentsOrderBase(page, mapBase);
            List<ShipmentsByOrderBaseVO> orderDetails = orderBaseIPage.getRecords();
            // 设置发货信息的详细字段
            // 设置待发货订单的信息
            boolean flag = setShippedOrderDetails(orderDetails, shopId);
            // 如果设置成功，返回查询结果，否则返回订单为空的错误响应
            if (flag) {
                return ResponseResult.success(PageUtils.getPage(orderBaseIPage));
            }
            return ResponseResult.fail(BizCodeEnume.ORDER_IS_NULL.getCode(), BizCodeEnume.ORDER_IS_NULL.getMsg());
        }
        // 如果用户无权限查询，返回权限不足的错误响应
        return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
    }

    @Override
    public ResponseResult reduction(Map<String, Object> map) {
        // 通过token获取用户信息，验证token的有效性
        UserInfoVO userInfoVO = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        // 如果用户信息为空，表示token无效，返回相应的错误结果
        if (userInfoVO == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 获取订单号，准备进行订单状态更新
        String orderId = (String) map.get("orderId");
        // 更新订单的隐藏状态，将状态设置为不可见
        boolean isSuccess = this.baseMapper.updateShopHidden(CommonType.commonStatus.NO.getCode(), orderId);
        // 如果更新成功，返回成功结果
        if (isSuccess) {
            return ResponseResult.success();
        }
        // 如果更新失败，返回失败结果
        return ResponseResult.fail(BizCodeEnume.UPDATE_ERROR);

    }

    /**
     * 查询回收站中的订单页面详情。
     *
     * @param map 包含请求参数的映射，其中必须包含token以用于用户身份验证。
     * @return 返回查询结果的响应对象。
     */
    @Override
    public ResponseResult queryRecycleBinPageDetails(Map<String, Object> map) {
        // 通过token获取用户信息，用于后续的权限验证。
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        // 如果用户信息为空，表示token无效，返回相应的错误响应。
        if (ui == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
        }
        // 初始化分页信息。
        PageInfo pi = new PageInfo();
        // 将用户信息中的店铺ID转换为整型，用于后续的查询条件。
        Integer shopId = Integer.valueOf(ui.getShopId());
        // 将店铺ID添加到查询参数映射中，作为查询条件之一。
        // 条件参数
        map.put("shopId", shopId);
        // 根据查询参数进行订单详情的查询。
        // 注意：这里的查询逻辑可能需要根据实际业务需求进行调整。
        IPage<OrderDetailsVO> page = this.baseMapper.queryRecycleBinByOrderBase(new Query<OrderDetailsVO>().getPage(map), map);
        // 遍历查询结果，为每个订单加载商品详情。
        for (int i = 0; i < page.getRecords().size(); i++) {
            String orderId = page.getRecords().get(i).getOrderId();
            // 根据订单ID和店铺ID查询订单的商品详情。
            List<GoodsDetailsVO> queryOrderDetails = orderGoodsMapper.queryOrders(orderId, shopId);
            // 如果商品详情查询成功，将其设置到订单详情中。
            if (queryOrderDetails != null) {
                page.getRecords().get(i).setGoodsDetails(queryOrderDetails);
            } else {
                // 如果商品详情查询失败，表示订单数据存在问题，返回相应的错误响应。
                return ResponseResult.fail(BizCodeEnume.ORDER_IS_NULL.getCode(), BizCodeEnume.ORDER_IS_NULL.getMsg());
            }
        }
        // 构建成功响应，并返回分页处理后的查询结果。
        return ResponseResult.success(PageUtils.getPage(page));
    }


    @Override
    public ResponseResult recycleBinExcel(Map<String, Object> map) {
        String token = (String) map.get("token");
        boolean isExist = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (isExist) {
            try {
                UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
                if (null == ui.getShopId()) {
                    return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
                }
                Integer shopId = Integer.valueOf(ui.getShopId());
                String Seller_Trade_Order = "Seller_Trade_Order";
                if (containLimits(ui, Seller_Trade_Order)) {
                    String username = (String) map.get("userName");
                    String userName = null;
                    if (username == "") {
                        userName = "";
                    } else {
                        userName = "'" + username + "'";
                    }

                    Integer virtualGoods = (Integer) map.get("virtualGoods");
                    String ordernum = (String) map.get("orderNum");
                    String orderNum = null;
                    if (ordernum == "") {
                        orderNum = "";
                    } else {
                        orderNum = "'" + ordernum + "'";
                    }
                    String goodsname = (String) map.get("goodsName");
                    String goodsName = null;
                    if (goodsname == "") {
                        goodsName = "";
                    } else {
                        goodsName = "'" + goodsname + "'";
                    }
                    String orderstarttime = (String) map.get("orderStartTime");
                    String orderStartTime = null;
                    if (orderstarttime == "") {
                        orderStartTime = "";
                    } else {
                        orderStartTime = "'" + orderstarttime + "'";
                    }
                    String orderendtime = (String) map.get("orderEndTime");
                    String orderEndTime = null;
                    if (orderendtime == "") {
                        orderEndTime = "";
                    } else {
                        orderEndTime = "'" + orderendtime + "'";
                    }
                    String allOrderStatus = null;
                    Integer status = Integer.parseInt(map.get("status") + "");

                    List<OrderDetailsVO> orderList = this.baseMapper.queryRecycleBinByOrderBaseExcel(virtualGoods, shopId, allOrderStatus,
                            status, userName, orderNum, goodsName, orderStartTime, orderEndTime);

                    String[] title = {"订单号", "下单时间", "买家", "订单金额", "交易状态"};
                    List<Map<String, String>> list = Lists.newArrayList();
                    for (int i = 0; i < orderList.size(); i++) {
                        Integer orderStatus = orderList.get(i).getOrderStatus();
                        String statusName = null;
                        if (orderStatus == 1) {
                            statusName = "待付款";
                        } else if (orderStatus == 2) {
                            statusName = "待配货";
                        } else if (orderStatus == 3) {
                            statusName = "待发货";
                        } else if (orderStatus == 4) {
                            statusName = "已发货";
                        } else if (orderStatus == 5) {
                            statusName = "已签收";
                        } else if (orderStatus == 6) {
                            statusName = "已完成";
                        } else if (orderStatus == 7) {
                            statusName = "已取消";
                        } else if (orderStatus == 8) {
                            statusName = "退款中";
                        } else if (orderStatus == 9) {
                            statusName = "退款完成";
                        }

                        HashMap<String, String> mPut = Maps.newHashMap();
                        mPut.put("订单号", orderList.get(i).getOrderId());
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String format2 = format.format(orderList.get(i).getOrderCreateTime());
                        mPut.put("下单时间", format2);
                        mPut.put("买家", orderList.get(i).getBuyerUserName());
                        mPut.put("订单金额", new BigDecimal(orderList.get(i).getOrderPaymentAmount()).toString());
                        mPut.put("交易状态", statusName);
//
                        list.add(mPut);
                    }
                    Map<String, List<Map<String, String>>> m = Maps.newHashMap();
                    m.put("合并数据", list);
                    String url = ExcelsUtil.createExcel(title, m, new int[]{0});/* 此处数组为需要合并的列，可能有的需求是只需要某些列里面相同内容合并 */

                    return ResponseResult.success(url);
                } else {
                    return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
                }
            } catch (Exception e) {

                e.printStackTrace();
                log.info("RecycleBinPageByOrderDetailsErrorResults:" + e.getMessage());
                return ResponseResult.fail(e.getMessage());
            }
        } else {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }

    }

    @Override
    public ResponseResult queryLogisticsName(Map<Object, Object> map) {
        String token = map.get("token").toString();
        UserInfoVO userInfoVO = ParseTokenUtils.getUserInfoByToken(token);
        if (userInfoVO == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        String orderId = (String) map.get("orderId");
        // 查询买家收货地址
        OrderBaseEntity qln = this.baseMapper.selectById(orderId);
        Integer expressId = qln.getOrderShippingExpressId();
        if (expressId != null) {
            //查询快递公司名字
            ResponseResult express = baseServiceClient.getBaseExpressInfoById(expressId);
            if (null != express && null != express.getData() && StringUtils.isNotEmpty(express.getData().toString())) {
                Map<String, String> shipperCode = JSONObject.parseObject(JSON.toJSONString(express.getData()), Map.class);
                qln.setOrderShippingExpress(shipperCode.get("expressName"));
            } else {
                qln.setOrderShippingExpress(null);
            }
        }
        return ResponseResult.success(qln);
    }

    @Override
    @Transactional
    public ResponseResult changeShoppingPrice(Map<String, Object> map) {
        String token = (String) map.get("token");
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (!isExistToken) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
        if (null == ui.getShopId()) {
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
        String orderId = (String) map.get("orderId");
        List<Map<String, Object>> list = (List) map.get("list");
        if (null != list && list.size() == 0) {
            return ResponseResult.success();
        }
        //订单商品佣金总额
        BigDecimal orderGoodsCommission = new BigDecimal(0);
        //订单基础佣金总额
        BigDecimal orderBaseCommission = new BigDecimal(0);
        orderBaseCommission = orderGoodsMapper.queryOrderCommission(orderId, list);
        //查询当前订单除去 改价商品外的 商品佣金和
        if (null == orderBaseCommission) {
            orderBaseCommission = new BigDecimal(0);
        }
        for (Map<String, Object> res : list) {
            // 增加或减少的金额
            String money = (String) res.get("money");
            if (!MathUtil.isMoney(money)) {
                return ResponseResult.fail(2, "输入的格式有误,请按要求填写！");
            }
            // 类型转换
            BigDecimal price = new BigDecimal(money);
            // 金额正负1:+  2:-
            Integer type = (Integer) res.get("type");
            // 商品id
            Integer goodsId = (Integer) res.get("goodsId");
            // 查询该订单总价
            OrderGoodsEntity goodsEntity = orderGoodsMapper.selectOne(new LambdaQueryWrapper<OrderGoodsEntity>()
                    .eq(OrderGoodsEntity::getOrderId, orderId).eq(OrderGoodsEntity::getGoodsId, goodsId));
            OrderBaseEntity baseEntity = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderBaseEntity>()
                    .eq(OrderBaseEntity::getOrderId, orderId));
            goodsEntity.setOrderGoodsAdjustBeforePrice(goodsEntity.getOrderGoodsAdjustBeforePrice() == null ? goodsEntity.getOrderGoodsPaymentAmount() : goodsEntity.getOrderGoodsAdjustBeforePrice());
            // 存储改价之后的金额
            BigDecimal calculateAfterMoney = new BigDecimal(0);
            BigDecimal newPrice = BigDecimal.ZERO;
            String adjustPrice = null;
            String adjustPriceBase = null;
            Integer isChangePrice = null;
            BigDecimal goodsPaymentPrice = this.baseMapper.queryGoodsPaymentPrice(orderId, goodsId);
            // 查询订单是否存在改价
            BigDecimal beforeAdjustPrice = this.baseMapper.queryBeforeAdjustPrice(orderId, goodsId);
            BigDecimal beforeAdjustBasePrice = this.baseMapper.queryOrderBaseBeforeAdjustPrice(orderId);
            if (null == beforeAdjustPrice) {
                isChangePrice = 0;
                beforeAdjustPrice = new BigDecimal(0);
            }
            if (null == beforeAdjustBasePrice) {
                beforeAdjustBasePrice = new BigDecimal(0);
            } else {
                beforeAdjustBasePrice = beforeAdjustBasePrice.subtract(beforeAdjustPrice);
            }
            BigDecimal totalPrice = baseEntity.getOrderPaymentAmount().subtract(goodsEntity.getOrderGoodsPaymentAmount())
                    .add(goodsEntity.getOrderGoodsAdjustBeforePrice());
            goodsPaymentPrice = goodsEntity.getOrderGoodsAdjustBeforePrice() == null ? goodsPaymentPrice : goodsEntity.getOrderGoodsAdjustBeforePrice();
            newPrice = price;
            // 根据goodsId 查询平台分类抽佣比例
            BigDecimal catCommission = orderGoodsMapper.queryCatCommissionByGoodsId(goodsId);
            catCommission = catCommission.divide(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP);
            if (type == 1) {
                calculateAfterMoney = totalPrice.add(price);
                beforeAdjustBasePrice = beforeAdjustBasePrice.add(price);
                adjustPrice = "" + newPrice;
                adjustPriceBase = "" + beforeAdjustBasePrice;
                price = goodsEntity.getOrderGoodsAdjustBeforePrice().add(price);
                // 计算佣金
                orderGoodsCommission = price.multiply(catCommission).setScale(2, RoundingMode.HALF_UP);
                // 更新goods实付金额
                this.baseMapper.updateOrderGoodsPaymentAmountAdd(orderId, price, adjustPrice, goodsId, isChangePrice, orderGoodsCommission);
            } else {
                if (goodsPaymentPrice.compareTo(price) < 0) {
                    return ResponseResult.fail(10060, "请注意，改价金额不得大于实付金额！");
                } else {
                    calculateAfterMoney = totalPrice.subtract(price);
                    beforeAdjustBasePrice = beforeAdjustBasePrice.subtract(price);
                    adjustPrice = "-" + newPrice;
                    adjustPriceBase = beforeAdjustBasePrice.toString();
                    price = goodsEntity.getOrderGoodsAdjustBeforePrice().subtract(price);
                    // 计算佣金
                    orderGoodsCommission = price.multiply(catCommission).setScale(2, RoundingMode.HALF_UP);
                    // 更新goods实付金额
                    this.baseMapper.updateOrderGoodsPaymentAmountSubtract(orderId, price, adjustPrice, goodsId, isChangePrice, orderGoodsCommission);
                }
            }
            orderBaseCommission = orderBaseCommission.add(orderGoodsCommission);
            // 更新base实付金额
            this.baseMapper.updateOrderBasePaymentAmount(orderId, calculateAfterMoney, adjustPriceBase, orderBaseCommission);
            AdjustPriceDTO ap = new AdjustPriceDTO();
            ap.setOrderId(orderId);
            ap.setCalculateAfterMoney(calculateAfterMoney);
            // 更新record表与trade表实付金额
            this.updateOrderPaymentAmount(ap);
        }
        return ResponseResult.success();
    }

    /**
     * 取消店铺核销记录接口。
     * 该方法用于根据用户令牌取消店铺的核销记录。如果用户令牌有效，将根据是否指定了核销状态来查询相应的核销记录，
     * 并在返回的结果中包含用户名称。
     *
     * @param page 包含分页信息和用户令牌的页面对象。
     * @return 返回查询结果的响应对象。
     */
    @Override
    public ResponseResult cancelShopGateRecord(PageUserTokenBeanDTO page) {
        // 初始化一个空的IPage对象用于存储查询结果
        IPage<QueryCancelRecordResultVO> result = new Page<>();
        // 检查用户令牌是否存在于Redis中
        boolean isExist = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + page.getToken());
        if (isExist) {
            try {
                // 通过令牌获取用户信息，并设置店铺ID到page对象中
                UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(page.getToken());
                page.setShopId(ui.getShopId());
                // 将page对象转换为Map，以传递给查询方法
                Map data = ParseMapUtils.beanToMap(page);
                // 根据page对象中的isCancel值来决定查询已核销还是未核销的记录
                // IsCance 为null 查询全部  IsCance 0 查询已核销  1  未核销
                if (null == page.getIsCancel()) {
                    result = this.baseMapper.queryCancelShopGateRecord2(new Query<QueryCancelRecordResultVO>().getPage(data), page);
                } else {
                    result = this.baseMapper.queryCancelShopGateRecord(new Query<QueryCancelRecordResultVO>().getPage(data), page);
                }
                // 遍历查询结果，设置每个记录的用户名
                for (QueryCancelRecordResultVO vo : result.getRecords()) {
                    vo.setUserName(ui.getUserName());
                }
                // 返回查询结果的成功响应
                return ResponseResult.success(PageUtils.getPage(result));
            } catch (IllegalAccessException e) {
                // 返回异常信息的失败响应
                return ResponseResult.fail(e.getMessage());
            }
        } else {
            // 返回令牌无效的失败响应
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
    }

    /**
     * 根据用户token查询虚拟商品购买用户的分页信息。
     *
     * @param page 包含分页信息和用户token的DTO对象。
     * @return 返回查询结果的ResponseResult对象。
     */
    @Override
    public ResponseResult queryVirtualGoodsUser(PageUserTokenBeanDTO page) {
        // 初始化结果列表和用户ID列表
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<String> userids = new ArrayList<>();
        Integer totalCount = null;
        // 初始化一个Map用于最终的返回结果
        Map<String, Object> map = new HashMap<>();
        // 检查用户token是否存在于Redis中
        boolean isExist = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + page.getToken());
        if (isExist) {
            // 从Redis中获取用户信息
            // 获取user信息
            String userinfo = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + page.getToken());
            UserInfoDTO ui = JSON.parseObject(userinfo, UserInfoDTO.class);
            UserInfoVO userInfo = ui.getUser();
            // 设置页码和店铺ID
            page.setPage((page.getPage() - 1) * page.getLimit());
            page.setShopId(userInfo.getShopId());
            // 根据IsCancel的值选择查询方法
            //IsCance 为null 查询全部  IsCance 0 查询已核销  1  未核销
            if (null == page.getIsCancel()) {
                userids = this.baseMapper.queryVirtualGoodsUser2(page);
                totalCount = this.baseMapper.queryVirtualGoodsUserCount2(page);
            } else {
                userids = this.baseMapper.queryVirtualGoodsUser(page);
                totalCount = this.baseMapper.queryVirtualGoodsUserCount(page);
            }
            // 遍历用户ID列表，查询并添加用户详细信息到结果列表
            if (null != userids) {
                for (String userid : userids) {
                    Map<String, Object> resutlMap = this.baseMapper.selectMembershipUserById(userid);
                    if (null != resutlMap) {
                        resultList.add(resutlMap);
                    }
                }
                // 将总数和结果列表添加到最终的返回Map中
                map.put("totalCount", totalCount);
                map.put("userInfo", resultList);
                // 返回成功结果
                return ResponseResult.success(map);
            }
            // 如果没有查询到数据，返回失败结果
            return ResponseResult.fail(2, "未查询到数据信息");
        }
        // 如果用户token无效，返回失败结果
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
    }

    /**
     * 取消记录的接口。
     * 用于查询用户的核销记录，支持查询已核销和未核销的记录。
     *
     * @param page 分页参数，包含用户令牌、页码、每页数量等信息。
     * @return 根据查询结果生成的响应结果对象，包含查询记录的总数和具体记录列表。
     */
    @Override
    public ResponseResult cancelRecord(PageUserTokenBeanDTO page) {
        // 检查用户令牌是否存在于Redis中
        boolean isExist = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + page.getToken());
        if (isExist) {
            // 从Redis中获取用户信息
            // 获取user信息
            String userinfo = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + page.getToken());
            UserInfoDTO ui = JSON.parseObject(userinfo, UserInfoDTO.class);
            UserInfoVO userInfo = ui.getUser();

            // 设置分页默认值，防止前端未传参导致NPE
            Integer pageNum = page.getPage() != null ? page.getPage() : 1;
            Integer limit = page.getLimit() != null ? page.getLimit() : 10;
            // 初始化分页参数
            //IsCance 为null 查询全部  IsCance 0 查询已核销  1  未核销
            Page<QueryCancelRecordResultVO> pageQuery = new Page<>(pageNum, limit);

            // 初始化查询结果对象
            IPage<QueryCancelRecordResultVO> results = null;
            // 构建查询条件
            Map<String, Object> map = new HashMap<>();
            map.put("userId", page.getUserId());
            map.put("shopId", userInfo.getShopId());

            // 根据是否指定了核销状态，选择不同的查询方法
            if (null == page.getIsCancel()) {
                results = this.baseMapper.queryCancelRecord2(pageQuery, map);
            } else {
                map.put("isCancel", page.getIsCancel());
                results = this.baseMapper.queryCancelRecord(pageQuery, map);
            }

            // 给查询结果中的每条记录补充用户名信息
            for (QueryCancelRecordResultVO result : results.getRecords()) {
                result.setUserName(userInfo.getUserName());
            }

            // 构建最终的响应数据
            Map<String, Object> map2 = new HashMap<>();
            map2.put("totalCount", results.getTotal());
            map2.put("results", results.getRecords());

            // 返回查询成功的响应结果
            return ResponseResult.success(map2);
        }

        // 如果用户令牌无效，返回相应的错误响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
    }

    @Override
    public ResponseResult queryOrdersByPC(Map<String, Object> map) {
        PageInfo pi = new PageInfo();
        String token = (String) map.get("token");
        // 获取user信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        if (userInfo != null) {
            String userId = userInfo.getUserId();
            // 订单状态
            // 前端可能不传 orderStatus（买家端「全部订单」），直接 parseInt(map.get(...) + "")
            // 会得到 parseInt("null") 抛 NumberFormatException，整个列表接口 20010。缺省按 0=全部。
            Object orderStatusRaw = map.get("orderStatus");
            Integer orderStatus = (orderStatusRaw == null || orderStatusRaw.toString().trim().isEmpty())
                    ? 0 : Integer.parseInt(orderStatusRaw.toString().trim());
            //订单编号
            String orderIds = (String) map.get("orderNo");
            //开始时间
            String orderStartTime = (String) map.get("timeStart");
            //结束时间
            String orderEndTime = (String) map.get("timeEnd");
            //支付编码
            Object payCodeRaw = map.get("payCode");
            Integer payCode = (payCodeRaw == null || payCodeRaw.toString().trim().isEmpty())
                    ? null : Integer.parseInt(payCodeRaw.toString().trim());
            Integer virtualGoods = null;
            if (null != map.get("virtualGoods")) {
                virtualGoods = (Integer) map.get("virtualGoods");
            }
            Integer integralGoods = null;
            if (null != map.get("integralGoods")) {
                integralGoods = (Integer) map.get("integralGoods");
            }

            Integer allOrders = null;
            if (orderStatus.equals(CommonType.orderStatus.TO_BE_PAID.getCode())) {
                // 全部订单
                allOrders = 1;
            }
            // 平台超管（hou_user_info.is_admin=1 → 登录态 permissions=["*"]）调用本接口时
            // 是管理端行为：① 不按登录用户过滤（可看全平台订单）；② 允许 mall 代理直传
            // orderStatusNum（枚举码 1/3/4/6/7，对应后台订单 tab），绕过下方 PC 买家端
            // 的 2/3/4/5/6 语义映射（2026-10-30 后台订单列表打通）。
            boolean platformAdmin = ParseTokenUtils.isPlatformAdmin(token);
            Integer orderStatusNum;
            if (platformAdmin && null != map.get("orderStatusNum")) {
                orderStatusNum = Integer.parseInt(map.get("orderStatusNum").toString().trim());
                allOrders = null;
            } else {
                orderStatusNum = getOrderStatusNum(orderStatus);
            }

            Map<String, String> businessUseMap = new HashMap<>();
            businessUseMap.put("userId", userId);
            //根据用户id查企业用户信息
            ResponseResult businessResult = userServiceClient.getBusinessInfoByMap(businessUseMap);
            BusinessUserInfoDTO detail = null;
            if (businessResult.getCode() == 1) {
                detail = JSON.parseObject(JSON.toJSONString(businessResult.getData()), BusinessUserInfoDTO.class);
            }

            //查询用户订单详情
            pi = getOrderList(map, integralGoods, orderIds, allOrders, orderStatusNum,
                    orderStartTime, orderEndTime, virtualGoods, payCode,
                    detail, userId, platformAdmin);
            return ResponseResult.success(pi);
        } else {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
    }
    @Override
    public ResponseResult soldOrdersExp(Map<String, Object> map) {
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        if (ui == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
        }
        Integer shopId = Integer.valueOf(ui.getShopId());
        setOrderTime(map);
        String userName = (String) map.get("userName");
        String orderNum = (String) map.get("orderNum");
        String goodsName = (String) map.get("goodsName");
        String orderStartTime = (String) map.get("orderStartTime");
        String orderEndTime = (String) map.get("orderEndTime");
        Integer status = Integer.parseInt(map.get("status") + "");
        String allOrderStatus = null;
        Integer otherOrderStatus = null;
        //退货状态
        Integer orderReturnStatus1 = null;
        //退款状态
        Integer orderRefundStatus1 = null;
        if (status == 1) {
            allOrderStatus = "' '";
        } else if (status == 2) {
            // 待付款
            otherOrderStatus = 1;
            orderReturnStatus1 = 0;
            orderRefundStatus1 = 0;
        } else if (status == 3) {
            // 已付款
            otherOrderStatus = 3;
            orderReturnStatus1 = 0;
            orderRefundStatus1 = 0;
        } else if (status == 4) {
            // 已发货
            otherOrderStatus = 4;
            orderReturnStatus1 = 0;
            orderRefundStatus1 = 0;
        } else if (status == 5) {
            // 已完成
            otherOrderStatus = 6;
            orderReturnStatus1 = 0;
            orderRefundStatus1 = 0;
        } else if (status == 6) {
            // 已取消
            otherOrderStatus = 7;
            orderReturnStatus1 = 0;
            orderRefundStatus1 = 0;
        } else if (status == 7) {
            // 退货中
            otherOrderStatus = null;
            orderReturnStatus1 = 1;
        } else if (status == 8) {
            // 退款中
            otherOrderStatus = null;
            orderRefundStatus1 = 1;
        } else if (status == 9) {
            // 退款完成
            otherOrderStatus = null;
            orderRefundStatus1 = 2;
        } else if (status == 10) {
            // 退货完成
            otherOrderStatus = null;
            orderReturnStatus1 = 2;
        } else if (status == 16) {
            otherOrderStatus = 16;
            orderReturnStatus1 = 0;
            orderRefundStatus1 = 0;
        }
        Integer dropShipping = null;
        if (null != map.get("dropShipping")) {
            dropShipping = Integer.parseInt(map.get("dropShipping") + "");
        }

        map.put("shopId", shopId);
        map.put("allOrderStatus", allOrderStatus);
        map.put("otherOrderStatus", otherOrderStatus);
        map.put("orderReturnStatus", orderReturnStatus1);
        map.put("orderRefundStatus", orderRefundStatus1);
        map.put("userName", userName);
        map.put("orderNum", orderNum);
        map.put("goodsName", goodsName);
        map.put("orderStartTime", orderStartTime);
        map.put("orderEndTime", orderEndTime);
        map.put("dropShipping", dropShipping);
        map.put("orderSource", 2);

        List<SoldOrderExportDTO> orderList = this.baseMapper.queryOrderBaseOfShopList(map);

        String[] title = {"订单号", "订单状态", "买家昵称", "收件人姓名", "手机",   "运费", "实付款", "商品名称",
                "商家编码", "销售属性", "单价", "数量", "优惠", "下单时间", "付款时间", "买家留言", "卖家备注"};
        List<Map<String, String>> list = Lists.newArrayList();
        for (SoldOrderExportDTO soldOrderExportDTO : orderList) {
            Integer orderStatus = soldOrderExportDTO.getOrderStatus();
            Integer orderRefundStatus = soldOrderExportDTO.getOrderRefundStatus();
            Integer orderReturnStatus = soldOrderExportDTO.getOrderReturnStatus();
            String statusName = null;
            if (orderStatus == 1) {
                statusName = "待付款";
            } else if (orderStatus == 2) {
                statusName = "待配货";
            } else if (orderStatus == 3) {
                if(orderRefundStatus == 1){
                    statusName = "退款申请中";
                }else if(orderRefundStatus == 2){
                    statusName = "退款完成";
                }else if(orderRefundStatus == 3){
                    statusName = "退款不通过";
                }else{
                    statusName = "待发货";
                }
            } else if (orderStatus == 4) {
                statusName = "已发货";
            } else if (orderStatus == 5) {
                statusName = "已签收";
            } else if (orderStatus == 6) {
                if(orderReturnStatus == 1){
                    statusName = "退货申请中";
                }else if(orderReturnStatus == 2){
                    statusName = "退货完成";
                }else if(orderReturnStatus == 3){
                    statusName = "退货不通过";
                }else{
                    statusName = "已完成";
                }
            } else if (orderStatus == 7) {
                statusName = "已取消";
            } else if (orderStatus == 8) {
                statusName = "退款中";
            } else if (orderStatus == 9) {
                if(orderRefundStatus == 2){
                    statusName = "退款完成";
                }else{
                    statusName = "退货完成";
                }
            }else if (orderStatus == 16) {
                statusName = "部分发货";

            }
            HashMap<String, String> mPut = Maps.newHashMap();
            mPut.put("订单号", soldOrderExportDTO.getOrderId());
            mPut.put("订单状态", statusName);
            mPut.put("买家昵称", soldOrderExportDTO.getBuyerUserName());
            mPut.put("收件人姓名", soldOrderExportDTO.getOrderReceiverName());
            mPut.put("手机", soldOrderExportDTO.getOrderReceiverContact());
            mPut.put("运费", soldOrderExportDTO.getOrderShippingFee() == null ? "0" : soldOrderExportDTO.getOrderShippingFee().toString());
            mPut.put("实付款", soldOrderExportDTO.getOrderPaymentAmount().toString());
            mPut.put("商品名称", soldOrderExportDTO.getGoodsName());
            mPut.put("商家编码", soldOrderExportDTO.getShopId());
            mPut.put("销售属性", soldOrderExportDTO.getOrderSpecChineseValue());
            mPut.put("单价", soldOrderExportDTO.getGoodsPrice().toString());
            mPut.put("数量", soldOrderExportDTO.getOrderGoodsNum().toString());
            mPut.put("优惠", soldOrderExportDTO.getOrderDiscountFee() == null ? "0" : soldOrderExportDTO.getOrderDiscountFee().toString());
            mPut.put("下单时间", soldOrderExportDTO.getOrderCreateTime());
            mPut.put("付款时间", soldOrderExportDTO.getPaymentTime());
            mPut.put("买家留言", soldOrderExportDTO.getOrderMessage());
            mPut.put("卖家备注", soldOrderExportDTO.getOrderSellerMessage());
            list.add(mPut);
        }
        Map<String, List<Map<String, String>>> m = Maps.newHashMap();
        m.put("合并数据", list);
        String url = ExcelsUtil.createExcel(title, m, new int[]{0});
        return ResponseResult.success(url);
    }

    @Override
    public ResponseResult checkodd(Map<String, Object> map) {
        //判断是订单号还是投诉单号
        String oddNumbers = String.valueOf(map.get("oddNumbers"));
        Integer shopId = (Integer) map.get("shopId");
        OrderStatusVO selectorderid = null;
        if (oddNumbers != null && oddNumbers != "" && PayToolUtil.isInteger(oddNumbers)) {
            //纯数字订单
            selectorderid = this.baseMapper.selectorderid(oddNumbers, shopId);
            if (selectorderid != null) {
                Integer normalStatus = selectorderid.getOrderStatus();
                Integer orderRefundStatus = selectorderid.getOrderRefundStatus();
                Integer orderReturnStatus = selectorderid.getOrderReturnStatus();
                Integer status = selectorderid.getOrderStatus();
                if (normalStatus == 3) {
                    // 未发货状态
                    if (orderRefundStatus == 1 || orderRefundStatus == 3) {
                        // 未发货退款中
                        status = 22;
                    } else if (orderRefundStatus == 2) {
                        // 商家拒绝或审核通过，继续发货，但不可再次申请退款
                        status = 26;
                    } else if (orderRefundStatus == 0) {
                        status = normalStatus;
                    } else {
                        status = 3;
                    }
                } else if (normalStatus == 6) {
                    // 已完成状态
                    if (orderRefundStatus == 1 || orderRefundStatus == 3) {
                        // 收货退款中
                        status = 22;
                    } else if (orderRefundStatus == 2) {
                        //收货退款完成
                        status = 26;
                    } else if (orderRefundStatus == 0 && orderReturnStatus == 0) {
                        //无退货退款
                        status = 6;
                    } else if (orderReturnStatus == 1 || orderReturnStatus == 3) {
                        //} else if (orderReturnStatus == 1 ) {
                        // 收货退货中
                        status = 24;
                    } else if (orderReturnStatus == 2) {
                        //退货完成
                        status = 25;
                    } else if (orderReturnStatus == 0 && orderRefundStatus == 0) {
                        status = 6;
                    } else {
                        status = 6;
                    }
                } else {
                    status = normalStatus;
                }
                selectorderid.setOrderStatus(status);
            }
        } else {
            selectorderid = this.baseMapper.selectcomplaintsid(oddNumbers, shopId);
        }
        if (selectorderid == null) {
            return ResponseResult.fail(0, "订单号/投诉单号不存在", selectorderid);
        }
        return ResponseResult.success("订单号/投诉单号存在", selectorderid);
    }

    /**
     * 查询未评价的订单。
     * <p>
     * 本方法用于调用底层Mapper，查询所有尚未进行评价的订单信息。返回这些订单的详细情况，
     * 以便用户或者系统能够了解哪些订单尚未完成评价流程。通过这种方式，可以鼓励用户
     * 完成订单评价，提高平台的互动性和数据完整性。
     *
     * @return ResponseResult 包含未评价订单信息的成功响应结果。
     */
    @Override
    public ResponseResult queryNotEvaluatedOrder() {
        // 调用底层Mapper，查询所有未评价的订单
        List<AcquiescenceVO> acquiescenceVOList = this.baseMapper.queryNotEvaluatedOrder();
        // 将查询结果包装进成功响应结果中返回
        return ResponseResult.success(acquiescenceVOList);
    }


    private PageInfo getOrderList(Map<String, Object> map, Integer integralGoods, String orderIds,
                                  Integer allOrders, Integer orderStatusNum, String orderStartTime,
                                  String orderEndTime, Integer virtualGoods, Integer payCode,
                                  BusinessUserInfoDTO detail, String userId, boolean platformAdmin) {
        PageInfo pi = new PageInfo();
        Map<String, Object> mapOrder = new HashMap<>();
        // 平台超管查全平台订单，不按登录用户过滤；买家端保持仅本人。
        // 注意：不能放空 List（queryOrderList 的 <foreach> 会吐出悬空 `in` 导致 SQL 语法错），
        // 必须整个键不传，让 <if test="param.userIdList != null"> 直接不拼该条件。
        if (!platformAdmin) {
            List<String> userIds = new ArrayList<>();
            userIds.add(userId);
            mapOrder.put("userIdList", userIds);
        }
        mapOrder.put("integralGoods", integralGoods);
        mapOrder.put("orderIds", orderIds);
        mapOrder.put("allOrders", allOrders);
        mapOrder.put("orderStatusNum", orderStatusNum);
        mapOrder.put("orderStartTime", orderStartTime);
        mapOrder.put("orderEndTime", orderEndTime);
        mapOrder.put("virtualGoods", virtualGoods);
        mapOrder.put("orderType", null);
        mapOrder.put("payCode", payCode);
        //查询用户订单详情
        IPage<OrderListVO> page = this.baseMapper.queryOrderList(new Query<OrderListVO>().getPage(map), mapOrder);

        List<String> gradeNameList = new ArrayList<>();
        List<Integer> hyZheKouList = new ArrayList<>();
        for (int i = 0; i < page.getRecords().size(); i++) {
            Date endTime = page.getRecords().get(i).getSeckillCancelTime() == null ?
                    DateUtils.addDays(page.getRecords().get(i).getOrderCreateTime(), 1) : page.getRecords().get(i).getSeckillCancelTime();
            page.getRecords().get(i).setOrderEndTime(endTime);
            String giveawayInfo = page.getRecords().get(i).getGiveawayInfo();
            List<FullReductionGiftsVO> list = new ArrayList<>();
            if (null != giveawayInfo && !giveawayInfo.isEmpty()) {
                list = JsonUtils.string2Obj(giveawayInfo, List.class, FullReductionGiftsVO.class);
            }
            page.getRecords().get(i).setZengpinInfo(list);
            //判断是否可以再申请售后
            Integer platformInterventionId = page.getRecords().get(i).getPlatformInterventionId();
            Integer interventionState = page.getRecords().get(i).getInterventionState();
            Integer processingResults = page.getRecords().get(i).getProcessingResults();
            Integer returnMethodType = page.getRecords().get(i).getReturnMethodType();
            if (platformInterventionId != null && interventionState == 2 && processingResults == 0 && returnMethodType == 1) {
                page.getRecords().get(i).setPlatformAudit(1);
            } else if (platformInterventionId != null && interventionState == 2 && processingResults == 0 && (returnMethodType == 2 || returnMethodType == 3)) {
                page.getRecords().get(i).setPlatformAudit(2);
            } else {
                page.getRecords().get(i).setPlatformAudit(0);
            }
            Integer goodsNum = 0;
            //退款状态
            Integer orderRefundStatus = page.getRecords().get(i).getOrderRefundStatus();
            //退货状态
            Integer orderReturnStatus = page.getRecords().get(i).getOrderReturnStatus();
            // 换货状态
            Integer orderBarterStatus = page.getRecords().get(i).getOrderBarterStatus();
            //是否虚拟商品
            Integer isVirtualGoods = page.getRecords().get(i).getVirtualGoods();
            Integer isPastDue = page.getRecords().get(i).getPastDue();
            //判断虚拟商品是否过期且允许退款
            if (isVirtualGoods.equals(CommonType.commonStatus.NO.getCode())
                    && !DateUtils.compareDate2(Objects.requireNonNull(DateUtils.parseCNDate(page.getRecords().get(i).getPeriodOfValidity())))
                    && isPastDue.equals(CommonType.commonStatus.YES.getCode())) {
                page.getRecords().get(i).setExpire(CommonType.commonStatus.YES.getCode());
            }

            // 订单状态
            Integer normalStatus = page.getRecords().get(i).getOrderStatus();
            // 涉及退款退货订单状态
            Integer status = getStatus(normalStatus, virtualGoods, orderRefundStatus, orderReturnStatus, page.getRecords().get(i).getOrderLastReturnTime(),orderBarterStatus);
            // 存放最新状态
            page.getRecords().get(i).setOrderStatus(status);
            //获取账号开户行，
            OrderInvoiceInfoEntity invoice = orderInvoiceInfoMapper.selectOne(new LambdaQueryWrapper<OrderInvoiceInfoEntity>()
                    .eq(OrderInvoiceInfoEntity::getOrderId, page.getRecords().get(i).getOrderId()));
            if (invoice != null) {
                page.getRecords().get(i).setBankAccount(invoice.getBankAccount());
                page.getRecords().get(i).setOpeningBank(invoice.getOpeningBank());
            } else {
                page.getRecords().get(i).setBankAccount("-");
                page.getRecords().get(i).setOpeningBank("-");
            }
            if (detail != null) {
                String business = detail.getCompanyName();
                if (business != null && !business.isEmpty()) {
                    page.getRecords().get(i).setBusinessName(business);
                } else {
                    page.getRecords().get(i).setBusinessName(page.getRecords().get(i).getBuyerUserName());
                }
            }
            String orderId = page.getRecords().get(i).getOrderId();
            //根据订单号查商品列表
            List<GoodsDetailsListVO> goodsDetailsList = orderGoodsMapper.queryGoodsListLimit(orderId);
            Integer goodsCount = Math.toIntExact(orderGoodsMapper.selectCount(new LambdaQueryWrapper<OrderGoodsEntity>()
                    .eq(OrderGoodsEntity::getOrderId, orderId)
                    .eq(OrderGoodsEntity::getOrderDel, CommonType.orderDel.NOT_DELETE.getCode())));
            page.getRecords().get(i).setGoodsDetailsCount(Math.max(goodsCount - goodsDetailsList.size(), 0));
            // 商品总价
            BigDecimal paymentPrice = new BigDecimal(0);
            Integer offSelfCount = 0;
            for (GoodsDetailsListVO goodsDetailsListVO : goodsDetailsList) {
                // 计算共有几件商品
                Integer goodsnum = goodsDetailsListVO.getOrderGoodsNum();
                goodsNum += goodsnum;
                page.getRecords().get(i).setGoodsNum(goodsNum);
                // 单个商品价格
                BigDecimal orderGoodsAmount = goodsDetailsListVO.getOrderGoodsAmount();
                paymentPrice = paymentPrice.add(orderGoodsAmount).setScale(2, RoundingMode.UP);
            }
            // 再来一单
            page.getRecords().get(i).setIsAnotherOrder(offSelfCount.equals(goodsCount));
            // 商品列表
            page.getRecords().get(i).setGoodsDetailsList(goodsDetailsList);
            // 退款实际价格
            page.getRecords().get(i).setRefundRealPrice(page.getRecords().get(i).getOrderPaymentAmount());
            // 总付款价格
            page.getRecords().get(i).setOrderTotalPrice(page.getRecords().get(i).getOrderPaymentAmount());
            // 向redis存入订单状态
            Integer statusNum = page.getRecords().get(i).getOrderStatus();
            String statusNumber = statusNum.toString();
            if (!RedisUtil.exists(HouConfig.HOU_ORDERSTATUS + "--" + orderId)) {
                RedisUtil.set(HouConfig.HOU_ORDERSTATUS + "--" + orderId, statusNumber, 3000);
            } else {
                RedisUtil.remove(HouConfig.HOU_ORDERSTATUS + "--" + orderId);
                RedisUtil.set(HouConfig.HOU_ORDERSTATUS + "--" + orderId, statusNumber, 3000);
            }
            // 根据店铺id查询im启用状态
            ResponseResult result = baseServiceClient.detectingImStatus(CommonType.imConfigKey.CUDSTOMER_SERVICE_TYPE.getDesc() + "_" + page.getRecords().get(i).getShopId());
            if (result.getCode() == CommonStatusEnum.SUCCESS.getCode() && result.getData() != null) {
                ImConfigVO imConfigVO = JSON.parseObject(JSON.toJSONString(result.getData()), ImConfigVO.class);
                page.getRecords().get(i).setImConfigVO(imConfigVO);
            }
        }
        pi.setPage(PageUtils.getPage(page));
        pi.setGradeNameList(gradeNameList);
        pi.setHyZheKouList(hyZheKouList);
        return pi;
    }

    private Integer getStatus(Integer normalStatus, Integer virtualGoods, Integer orderRefundStatus,
                              Integer orderReturnStatus, String orderLastReturnTim, Integer orderBarterStatus) {
        // 涉及退款退货订单状态
        Integer status = null;
        orderRefundStatus = orderRefundStatus == null ? 0 : orderRefundStatus;
        orderReturnStatus = orderReturnStatus == null ? 0 : orderReturnStatus;
        orderBarterStatus = orderBarterStatus == null ? 0 : orderBarterStatus;
        if (normalStatus.equals(CommonType.orderStatus.TO_BE_SHIPPED.getCode()) || (null != virtualGoods && 0 == virtualGoods && CommonType.orderStatus.SHIPPED.getCode().equals(normalStatus))) {
            // 未发货状态
            if (orderRefundStatus.equals(CommonType.orderRefundStatus.REFUNDING.getCode()) || orderRefundStatus.equals(CommonType.orderRefundStatus.REFUND_FAILED.getCode())) {
                // 未发货退款中
                status = 22;
            } else if (orderRefundStatus.equals(CommonType.orderRefundStatus.REFUND_COMPLETED.getCode())) {
                // 商家拒绝或审核通过，继续发货，但不可再次申请退款
                status = 26;
            } else if (orderRefundStatus.equals(CommonType.orderRefundStatus.NO_REFUND.getCode()) && null != virtualGoods && 0 == virtualGoods) {
                status = normalStatus;
            } else {
                status = 3;
            }
        } else if (normalStatus.equals(CommonType.orderStatus.CONFIRM_RECEIPT.getCode())) {
            // 已完成状态
            if (orderRefundStatus.equals(CommonType.orderRefundStatus.REFUNDING.getCode()) || orderRefundStatus.equals(CommonType.orderRefundStatus.REFUND_FAILED.getCode())) {
                // 收货退款中
                status = 23;
            } else if (orderRefundStatus.equals(CommonType.orderRefundStatus.REFUND_COMPLETED.getCode())) {
                status = 25;
            } else if (orderRefundStatus.equals(CommonType.orderRefundStatus.NO_REFUND.getCode()) &&
                    orderReturnStatus.equals(CommonType.orderReturnStatus.NO_RETURNS.getCode()) &&
                    orderBarterStatus.equals(CommonType.orderBarterStatus.NO_BARTER.getCode())) {
                status = 6;
            } else if (orderReturnStatus.equals(CommonType.orderReturnStatus.RETURNING_GOODS.getCode()) || orderReturnStatus.equals(CommonType.orderReturnStatus.RETURN_FAILED.getCode())) {
                // 收货退货中
                status = 24;
            } else if (orderReturnStatus.equals(CommonType.orderReturnStatus.RETURN_COMPLETED.getCode())) {
                status = 25;
            } else if(orderBarterStatus.equals(CommonType.orderBarterStatus.BARTERING.getCode()) ||
                    orderBarterStatus.equals(CommonType.orderBarterStatus.BARTER_FAILED.getCode())){
                // 换货中
                status = 29;
            } else if(orderBarterStatus.equals(CommonType.orderBarterStatus.BARTER_COMPLETED.getCode())){
                // 换货完成
                status = 30;
            }else {
                status = 6;
            }
            if (null != orderLastReturnTim && TimeStampUtil.compareDate(orderLastReturnTim, TimeStampUtil.getNowDate())) {
                // 订单超过完成时间不可售后
                status = 27;
            }
        } else {
            status = normalStatus;
        }

        return status;
    }


    /**
     * 根据购买用户ID和当前用户ID，获取用户ID列表。
     * <p>
     * 此方法用于确定一个用户ID列表，该列表要么包含购买用户ID，要么包含当前用户ID。
     * 当购买用户ID不为空时，列表中包含购买用户ID；否则，列表中包含当前用户ID。
     * 这对于需要根据用户ID执行后续操作的场景非常有用，例如，在购物平台中根据用户ID进行订单查询或权限验证。
     *
     * @param buyUserId 购买用户ID，可能为null。
     * @param userId    当前用户ID，不会为null。
     * @param detail    业务用户信息DTO（数据传输对象），未在方法逻辑中使用，可能为null。
     * @return 用户ID列表，包含购买用户ID或当前用户ID。
     */
    private List<String> getUserIdList(String buyUserId, String userId, BusinessUserInfoDTO detail) {
        // 初始化用户ID列表
        List<String> userIdList = new ArrayList<>();
        // 判断购买用户ID是否为空
        if (buyUserId != null) {
            // 如果不为空，添加购买用户ID到列表
            userIdList.add(buyUserId);
        } else {
            // 如果为空，添加当前用户ID到列表
            userIdList.add(userId);
        }
        // 返回用户ID列表
        return userIdList;
    }


    private Integer getOrderStatusNum(Integer orderStatus) {
        Integer orderStatusNum = null;
        if (orderStatus.equals(CommonType.orderStatus.PAID.getCode())) {
            // 待付款
            orderStatusNum = CommonType.orderStatus.TO_BE_PAID.getCode();
        } else if (orderStatus.equals(CommonType.orderStatus.TO_BE_SHIPPED.getCode())) {
            // 待发货
            orderStatusNum = CommonType.orderStatus.TO_BE_SHIPPED.getCode();
        } else if (orderStatus.equals(CommonType.orderStatus.SHIPPED.getCode())) {
            // 待收货
            orderStatusNum = CommonType.orderStatus.SHIPPED.getCode();
        } else if (orderStatus.equals(CommonType.orderStatus.SIGNED_FOR.getCode())) {
            // 已取消
            orderStatusNum = CommonType.orderStatus.CANCEL.getCode();
        } else if (orderStatus == 6) {
            // 部分发货
            orderStatusNum = CommonType.orderStatus.PARTIAL_SHIPMENT.getCode();
        }
        return orderStatusNum;
    }


    /**
     * 调整订单支付金额。
     * 该方法用于根据提供的调整价格信息，更新订单的实付金额。它分别尝试更新订单记录和交易的支付金额，
     * 并只有在两者都成功更新时，才认为调整成功。
     *
     * @param ap 调整价格的DTO（数据传输对象），包含订单ID和调整后的金额。
     * @return 如果订单记录和交易的支付金额都成功更新，则返回true；否则返回false。
     */
    public boolean updateOrderPaymentAmount(AdjustPriceDTO ap) {
        // 获取订单ID和调整后的金额
        String orderId = ap.getOrderId();
        BigDecimal calculateAfterMoney = ap.getCalculateAfterMoney();

        // 尝试更新订单记录的支付金额
        boolean isUpdateRecord = this.baseMapper.updateOrderPaymentAmountByRecord(orderId, calculateAfterMoney);
        // 尝试更新订单交易的支付金额
        boolean isUpdateTrade = this.baseMapper.updateOrderPaymentAmountByTrade(orderId, calculateAfterMoney);

        // 如果订单记录和交易的支付金额都成功更新，则返回true，否则返回false
        if (isUpdateRecord && isUpdateTrade) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 设置待发货订单信息
     *
     * @param orderDetails
     * @return
     */
    private boolean setShippedOrderDetails(List<ShipmentsByOrderBaseVO> orderDetails, Integer shopId) {
        for (ShipmentsByOrderBaseVO orderDetail : orderDetails) {
            String orderId = orderDetail.getOrderId();
            // 退款状态
            Integer orderRefundStatus = orderDetail.getOrderRefundStatus();
            // 退货状态
            Integer orderReturnStatus = orderDetail.getOrderReturnStatus();
            // 换货状态
            Integer orderBarterStatus = orderDetail.getOrderBarterStatus();
            // 订单状态
            Integer normalStatus = orderDetail.getOrderStatus();
            // 涉及退款退货订单状态
            Integer rStatus = null;
            //满减赠品信息
            String giveawayInfo = orderDetail.getGiveawayInfo();
            List<ManJianZengPinVO> list;
            if (null != giveawayInfo && !giveawayInfo.isEmpty()) {
                list = JsonUtils.string2Obj(giveawayInfo, List.class, ManJianZengPinVO.class);
                orderDetail.setZengpinInfo(list);
            }
            if (normalStatus == 3) {
                // 未发货状态
                if (orderRefundStatus == 1) {
                    // 未发货退款中
                    rStatus = 22;
                } else if (orderRefundStatus == 2) {
                    // 商家拒绝或审核通过，继续发货，但不可再次申请退款
                    rStatus = 26;
                } else if (orderRefundStatus == 0) {
                    rStatus = 3;
                }
            } else if (normalStatus == 6) {
                // 已完成状态
                if (orderRefundStatus == 1) {
                    // 收货退款中
                    rStatus = 23;
                } else if (orderRefundStatus == 2) {
                    rStatus = 25;
                } else if (orderRefundStatus == 0 && orderReturnStatus == 0 && orderBarterStatus == 0) {
                    rStatus = 6;
                } else if (orderReturnStatus == 1) {
                    // 收货退货中
                    rStatus = 24;
                } else if (orderReturnStatus == 2) {
                    rStatus = 25;
                } else if(orderBarterStatus.equals(CommonType.orderBarterStatus.BARTERING.getCode()) ||
                        orderBarterStatus.equals(CommonType.orderBarterStatus.BARTER_FAILED.getCode())){
                    // 换货中
                    rStatus = 29;
                } else if(orderBarterStatus.equals(CommonType.orderBarterStatus.BARTER_COMPLETED.getCode())){
                    // 换货完成
                    rStatus = 30;
                } else {
                    rStatus = 6;
                }
            } else {
                rStatus = normalStatus;
            }
            // 存放最新状态
            orderDetail.setOrderStatus(rStatus);
            List<ShipmentsByOrderGoodsDetailsVO> queryOrderDetails = this.orderGoodsMapper.shipmentsByOrderGoods(orderId, shopId);
            if (queryOrderDetails != null) {
                for (ShipmentsByOrderGoodsDetailsVO detail : queryOrderDetails) {
                    if (detail.getPaymentTime() != null && detail.getDeliveryTime() != null) {
                        detail.setLatestDeliveryTime(DateUtils.addDays(detail.getPaymentTime(), detail.getDeliveryTime()));
                    }
                }
                if(orderDetail.getOrderStatus() == 16) {
                    // 统计未发货的商品数量（shipmentStatus == 0表示未发货）
                    long unshippedCount = queryOrderDetails.stream()
                            .filter(detail -> detail.getShipmentStatus() == 0)
                            .count();
                    if (unshippedCount == queryOrderDetails.size()) {
                        orderDetail.setOrderStatus(3);
                    }
                }
                orderDetail.setShipmentsByOrderGoodsDetails(queryOrderDetails);
            }
        }
        return true;
    }


    /**
     * 判断权限
     *
     * @param userInfo
     * @param limit
     * @return
     */
    public Boolean containLimits(UserInfoVO userInfo, String limit) {
        // 检查用户是否是管理员
        Integer adminInteger = userInfo.getSellerIsAdmin();
        if (1 == adminInteger) {
            return true;
        }

        // 如果用户不是管理员，检查他们是否具有指定的权限限制
        String limitsString = userInfo.getLimits();
        if (null == limitsString) {
            return false;
        }
        return true;
    }

    /**
     * 根据用户token查询会员用户订单详情。
     *
     * @param map 包含查询参数的映射，其中必须包含token信息。
     * @return 返回查询结果的响应对象。
     */
    @Override
    public ResponseResult queryMemberUserOrderDetail(Map<String, Object> map) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(map.get("token").toString());
        // 如果用户信息存在
        if (null != userInfo) {
            // 初始化返回标志
            Integer returnFlag = 0;
            // 执行分页查询
            // 分页查询数据
            IPage<UserInfoOrderDetailVO> page = this.baseMapper.queryMemberUserOrderDetail(new Query<UserInfoOrderDetailVO>().getPage(map), map);
            // 遍历查询结果
            for (UserInfoOrderDetailVO r : page.getRecords()) {
                // 获取订单号
                String orderId = r.getOrderId();
                // 根据订单号查询订单基本信息
                OrderBaseEntity orderInfoEntity = this.baseMapper.selectById(orderId);
                // 根据订单属性设置订单类型
                Integer virtualGoods = orderInfoEntity.getVirtualGoods();
                Integer orderIsShopTake = orderInfoEntity.getOrderIsShopTake();
                if (null != orderIsShopTake && 1 == orderIsShopTake) {
                    r.setOrderType(3);
                } else {
                    if (null != virtualGoods && 0 == virtualGoods) {
                        r.setOrderType(2);
                        r.setPastDue(orderInfoEntity.getPastDue());
                        r.setIsCancel(orderInfoEntity.getIsCancel());
                    } else {
                        r.setOrderType(1);
                    }
                }
                // 设置订单的退货状态和退款状态
                int returnStatus = Integer.valueOf(orderInfoEntity.getOrderReturnStatus());
                int refundStatus = Integer.valueOf(orderInfoEntity.getOrderRefundStatus());
                r.setReturnStatus(returnStatus);
                r.setRefundStatus(refundStatus);
                // 根据退货金额和订单金额计算返回标志
                if (2 == returnStatus || 2 == refundStatus) {
                    BigDecimal returnAmount = new BigDecimal(orderInfoEntity.getOrderRefundAmount());
                    BigDecimal orderAmount = orderInfoEntity.getOrderPaymentAmount();
                    if (orderAmount.compareTo(returnAmount) > 0) {
                        returnFlag = 2;
                    } else if ((orderAmount.compareTo(returnAmount) == 0)) {
                        returnFlag = 1;
                    }
                }
                r.setReturnFlag(returnFlag);
            }
            // 返回查询成功的响应结果
            return ResponseResult.success(PageUtils.getPage(page));
        }
        // 如果用户信息不存在，返回token非法的响应结果
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
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
        PlusMemberEntityVO plusMemberEntity = this.baseMapper.selectplusmember(userId);
        if (plusMemberEntity != null) {
            DateFormat dft = new SimpleDateFormat("yyyy-MM-dd HH:00:00");

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
                this.baseMapper.updateplusmember(userId, bigDecimal);
            }
        }
    }

    @Override
    public ResponseResult gateShopQueryDeliveryOrders(GateQueryDeliveryOrdersDTO param) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
        if (null != userInfo) {
            // 根据不同的订单状态，设置新的订单状态和相关属性
            if (param.getOrderStatus() == 1) {
                // 逻辑处理：订单状态为1时的处理逻辑
                param.setOrderStatus(null);
                param.setPickup(null);
                param.setDistributorId(0);
                param.setShopAffirm(null);
            } else if (param.getOrderStatus() == 2) {
                // 逻辑处理：订单状态为2时的处理逻辑
                param.setOrderStatus(1);
                param.setPickup(0);
                param.setDistributorId(0);
                param.setShopAffirm(null);
            } else if (param.getOrderStatus() == 3) {
                // 逻辑处理：订单状态为3时的处理逻辑
                param.setOrderStatus(3);
                param.setPickup(0);
                param.setDistributorId(0);
                param.setShopAffirm(1);
                param.setType(0);
            } else if (param.getOrderStatus() == 4) {
                // 逻辑处理：订单状态为4时的处理逻辑
                param.setOrderStatus(3);
                param.setPickup(0);
                param.setDistributorId(1);
                param.setShopAffirm(null);
            } else if (param.getOrderStatus() == 5) {
                // 逻辑处理：订单状态为5时的处理逻辑
                param.setOrderStatus(3);
                param.setPickup(1);
                param.setDistributorId(1);
                param.setShopAffirm(null);
            } else if (param.getOrderStatus() == 6) {
                // 逻辑处理：订单状态为6时的处理逻辑
                param.setOrderStatus(4);
                param.setPickup(1);
                param.setDistributorId(1);
                param.setShopAffirm(null);
            } else if (param.getOrderStatus() == 7) {
                // 逻辑处理：订单状态为7时的处理逻辑
                param.setOrderStatus(6);
                param.setPickup(1);
                param.setDistributorId(1);
                param.setShopAffirm(null);
            } else if (param.getOrderStatus() == 8) {
                // 逻辑处理：订单状态为8时的处理逻辑
                //  param.setOrderStatus(null);
                param.setOrderStatus(8);
                param.setPickup(null);
                param.setDistributorId(0);
                param.setShopAffirm(0);
            } else if (param.getOrderStatus() == 9) {
                // 逻辑处理：订单状态为9时的处理逻辑
                param.setOrderStatus(null);
                param.setPickup(null);
                param.setDistributorId(0);
                param.setOrderRefundStatus(1);
            }
            // 设置用户关联的店铺门禁ID
            param.setShopGateId(userInfo.getShopGateId());
            // 设置分页默认值，防止前端未传参导致NPE
            Integer page = param.getPage() != null ? param.getPage() : 1;
            Integer limit = param.getLimit() != null ? param.getLimit() : 10;
            // 创建分页对象
            Page<QueryCancelRecordResultVO> pageQuery = new Page<>(page, limit);
            // 调用Mapper进行查询，并返回分页结果
            IPage<GateQueryDeliveryOrdersVO> commonsResults = orderBaseMapper.gateShopQueryDeliveryOrders(pageQuery, param);
            // 返回查询结果
            return ResponseResult.success(PageUtils.getPage(commonsResults));
        }
        // 如果token无效，返回错误响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());

    }

    /**
     * 根据订单ID查询门市配送订单详情。
     *
     * @param param 查询参数，包含订单ID。
     * @return 返回查询到的订单详情信息。
     * @throws BizCoreException 如果订单不存在，则抛出业务核心异常。
     */
    @Override
    public ResponseResult gateShopQueryDeliveryOrdersDetail(GateQueryDeliveryOrdersDTO param) {
        // 根据订单ID查询门市配送订单详情
        GateShopDeliveryOrdersDetailVO mod = orderBaseMapper.gateShopQueryDeliveryOrdersDetail(param.getOrderId());
        // 检查订单是否存在，如果不存在则抛出异常
        if (mod == null) {
            throw new BizCoreException("订单不存在");
        }
        // 根据订单ID查询订单商品详情列表
        List<OrderDetailGoodsItemVO> itemVoList = orderGoodsMapper.getOrderGoodsItem(param.getOrderId());
        // 如果订单商品详情列表不为空，遍历列表
        if (CollUtil.isNotEmpty(itemVoList)) {
            itemVoList.forEach(houOrderDetailGoodsItemVo -> {
                // 如果商品规格中文描述不为空，更新商品名称为商品名称加上规格中文描述
                if (StrUtil.isNotBlank(houOrderDetailGoodsItemVo.getOrderSpecChineseValue())) {
                    houOrderDetailGoodsItemVo.setGoodsName(houOrderDetailGoodsItemVo.getGoodsName() + "(" + houOrderDetailGoodsItemVo.getOrderSpecChineseValue() + ")");
                }
            });
        }
        // 将订单商品详情列表设置到订单详情对象中
        mod.setGoodsVoList(itemVoList);
        // 返回查询成功的响应结果，包含订单详情对象
        return ResponseResult.success(mod);
    }

    /**
     * 商家确认收货接口。
     * 该方法用于商家在系统中确认特定订单的收货。它首先验证用户令牌的有效性，然后根据订单的状态检查是否可以进行收货确认。
     * 如果订单处于退款中或已退款状态，则不允许确认收货。否则，更新订单状态为已收货，并返回操作成功的响应。
     *
     * @param param 包含订单ID和用户令牌信息的请求参数。
     * @return 如果操作成功，返回一个成功的响应结果；如果令牌无效或订单状态不允许确认收货，则返回相应的错误响应。
     * @throws BizCoreException 如果订单处于退款中或已退款状态，则抛出此异常。
     */
    @Override
    public ResponseResult shopAffirm(GateQueryDeliveryOrdersDTO param) {
        // 通过令牌获取用户信息，用于验证用户身份。
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());

        // 验证用户信息是否有效。
        if (null != userInfo) {
            // 根据订单ID查询订单详情，用于检查订单状态。
            GateShopDeliveryOrdersDetailVO mod = orderBaseMapper.gateShopQueryDeliveryOrdersDetail(param.getOrderId());

            // 检查订单是否处于退款中或已退款状态，不允许确认收货。
            if (mod.getOrderRefundStatus() == 1) {
                throw new BizCoreException("退款中,不能确认！");
            }
            if (mod.getOrderRefundStatus() == 2) {
                throw new BizCoreException("已退款,不能确认！");
            }

            // 如果订单状态允许，更新订单为已收货状态。
            orderBaseMapper.shopAffirm(param.getOrderId());
            // 返回操作成功的响应。
            return ResponseResult.success();
        }

        // 如果用户信息验证失败，返回令牌无效的错误响应。
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
    }

    /**
     * 查询用户的消费总额和最近消费时间
     *
     * @param shopUserDTO 包含用户信息的数据传输对象，用于查询消费信息
     * @return ShopUserVO 返回包含用户消费总额和最近消费时间的值对象
     */
    @Override
    public ShopUserVO queryShopUserTotalExpenditure(ShopUserDTO shopUserDTO) {
        // 创建一个用户价值对象来存储查询结果
        ShopUserVO shopUserVO = new ShopUserVO();

        // 查询用户的消费总额
        BigDecimal totalExpenditure = orderBaseMapper.queryUserTotalExpenditure(shopUserDTO);
        // 将消费总额设置到用户价值对象中
        shopUserVO.setTotalExpenditure(totalExpenditure);

        // 查询用户的最近消费时间
        String latestConsumptionTime = orderBaseMapper.queryUserLatestConsumptionTime(shopUserDTO);
        // 将最近消费时间设置到用户价值对象中
        shopUserVO.setLatestConsumptionTime(latestConsumptionTime);

        // 返回包含用户消费总额和最近消费时间的用户价值对象
        return shopUserVO;
    }

    /**
     * 查询店铺用户的 historical orders.
     * 通过用户信息和指定条件查询用户的历史订单信息.
     *
     * @param shopUserDTO 包含token和查询条件的用户信息对象
     * @return 包含分页查询结果的响应结果对象
     */
    @Override
    public ResponseResult queryShopUserHistoricalOrders(ShopUserDTO shopUserDTO) {
        // 通过token获取用户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(shopUserDTO.getToken());
        // 如果用户信息为空，表示token非法
        if (null == ui) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());
        }
        // 设置店铺ID
        shopUserDTO.setShopId(ui.getShopId());
        // 将查询条件对象转换为map
        Map data = null;
        try {
            data = ParseMapUtils.beanToMap(shopUserDTO);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        // 执行查询并返回结果
        IPage<ShopUserHistoricalOrdersVO> orderList = this.baseMapper.queryShopUserHistoricalOrders(new Query<ShopUserVO>().getPage(data), shopUserDTO);
        return ResponseResult.success(PageUtils.getPage(orderList));
    }

    /**
     * 根据库存流水商品信息查询采购订单详情
     * 此方法解释了如何根据给定的库存信息来获取相关的采购订单详细信息它首先尝试从数据库中查询订单详情，
     * 如果没有找到，则返回一个空的采购订单对象和一个空的商品列表如果找到了订单信息，则进一步设置
     * 库存流水号和与该订单相关联的商品列表
     *
     * @param param 包含库存信息的DTO用于查询采购订单
     * @return PurchaseOrderVO 采购订单的视图对象，包含订单详情和商品列表
     */
    @Override
    public PurchaseOrderVO queryPurchaseOrderGoods(StorageInventoryFlowGoodsDTO param) {
        // 查询采购订单详细信息
        PurchaseOrderVO purchaseOrderVO = baseMapper.queryPurchaseOrderDetails(param);
        if (purchaseOrderVO == null) {
            // 如果没有找到订单信息，创建一个新的采购订单视图对象并设置一个空的商品列表
            purchaseOrderVO = new PurchaseOrderVO();
            purchaseOrderVO.setGoodsList(new ArrayList<>());
        } else {
            // 如果找到了订单信息，设置库存流水号和商品列表
            purchaseOrderVO.setInventoryFlowNum(orderGoodsMapper.selectInventoryFlowNum(purchaseOrderVO.getOrderId()));
            purchaseOrderVO.setGoodsList(orderGoodsMapper.queryPurchaseOrderGoodsList(purchaseOrderVO.getOrderId()));
        }
        return purchaseOrderVO;
    }

    /**
     * 查询用户消费金额和下单数量通过Feign客户端调用此方法允许传递包含多个查询参数的映射对象，
     * 以便灵活地执行查询
     *
     * @param map 包含查询参数的映射对象，例如用户ID、开始时间、结束时间等
     * @return 包含用户消费金额和下单数量的映射对象
     */
    @Override
    public Map<String, Object> queryUserConsumption(Map<String, String> map) {
        // 调用基础映射器查询用户消费信息
        Map<String, Object> result = this.baseMapper.queryUserConsumption(map);
        return result;
    }

    /**
     * 查询用户订单信息
     * <p>
     * 该方法通过调用基础映射器查询用户在即时通讯（IM）系统中的订单信息，并返回查询结果
     * 主要用于处理来自IM系统的用户订单查询请求，提供了一个接口级别的封装
     *
     * @param map 包含查询参数的映射，如用户ID、订单时间范围等，用于指定查询条件
     * @return 返回一个ResponseResult对象，其中包含查询结果的分页信息
     */
    @Override
    public ResponseResult imQueryUserOrderInfo(Map<String, Object> map) {
        // 调用基础映射器查询用户消费信息
        IPage<ImQueryUserOrderInfoDTO> result = this.baseMapper.imQueryUserOrderInfo(new Query<ImQueryUserOrderInfoDTO>().getPage(map), map);
        // 使用PageUtils工具将查询结果封装到ResponseResult对象中并返回
        return ResponseResult.success(PageUtils.getPage(result));
    }

    /**
     * 发货
     *
     * @param shipmentsListDTO
     * @return
     */
    @Override
    public ResponseResult yiyatongShipments(ShipmentsListDTO shipmentsListDTO) throws TransactionException {
        ResponseResult result = new ResponseResult();
        if (shipmentsListDTO.getShipmentsGoodsDTOList().size() > 0) {
            for (ShipmentsYiYaTongDTO shipmentsYiYaTongDTO : shipmentsListDTO.getShipmentsGoodsDTOList()) {
                String orderId = orderYiyatongMapper.selectOne(new LambdaQueryWrapper<OrderYiyatongEntity>()
                        .eq(OrderYiyatongEntity::getOrderSn, shipmentsYiYaTongDTO.getOrderSn())).getOrderId();
                if (null != orderId) {
                    OrderBaseEntity orderBaseEntity = this.baseMapper.selectById(orderId);
                    String token = RedisUtil.get(HouConfig.HOU_SHOP_TOKEN_SHOP + orderBaseEntity.getShopId());
                    shipmentsListDTO.setToken(token);
                    ResponseResult rr = ParseTokenUtils.getShopUserInfo(token);
                    UserInfoVO ui = JSON.parseObject(JSON.toJSONString(rr.getData()), UserInfoVO.class);
                    result = yiyatongShipments(shipmentsListDTO, CommonType.commonStatus.NO.getCode(), ui);
                }

            }

        }
        return result;
    }

    /**
     * 发货
     *
     * @param shipmentsListDTO
     * @return
     */
    public ResponseResult yiyatongShipments(ShipmentsListDTO shipmentsListDTO, Integer isPiLiang, UserInfoVO ui) throws
            TransactionException {
        Integer shopId = Integer.valueOf(ui.getShopId());
        for (ShipmentsYiYaTongDTO vo : shipmentsListDTO.getShipmentsGoodsDTOList()) {
            //查询怡亚通订单信息
            OrderYiyatongEntity orderYiyatongEntity = orderYiyatongMapper.selectOne(new LambdaQueryWrapper<OrderYiyatongEntity>()
                    .eq(OrderYiyatongEntity::getOrderSn, vo.getOrderSn()));
            OrderExpressListByOsDTO dto = new OrderExpressListByOsDTO();
            dto.setOrderSn(vo.getOrderSn());
            dto.setDeliverySn(vo.getDeliverySn());
            ResponseResult result = goodServiceClient.getOrderExpressListByOs(dto);
            List<OrderExpressVO> list = JSONObject.parseArray(JSON.toJSONString(result.getData()), OrderExpressVO.class);
            for (OrderExpressVO expressVO : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("orderId", orderYiyatongEntity.getOrderId());
                map.put("orderSellerName", expressVO.getSendName());
                map.put("orderSellerAddress", expressVO.getSendAddr());
                map.put("orderSellerContact", expressVO.getSendMobile());
                map.put("orderShippingCode", expressVO.getDeliverySn());
                map.put("expressName", expressVO.getExpressName());
                map.put("expressPinyin", vo.getDeliveryCorpSn());
                String orderShippingExpressId = this.baseMapper.queryOrderShippingExpressId(expressVO.getExpressName());
                if (null != orderShippingExpressId) {
                    map.put("orderShippingExpressId", orderShippingExpressId);
                } else {
                    BaseExpressDTO baseExpress = new BaseExpressDTO();
                    baseExpress.setExpressName(expressVO.getExpressName());
                    baseExpress.setExpressPinyin(vo.getDeliveryCorpSn());
                    this.baseMapper.insertBaseExpress(baseExpress);
                    orderShippingExpressId = String.valueOf(baseExpress.getId());
                    map.put("orderShippingExpressId", orderShippingExpressId);
                }


                List<String> skuIdList = expressVO.getOrderExpressItemVOList().stream().map(OrderExpressItemVO::getGoodsSpecId).collect(Collectors.toList());
                List<Integer> goodsIds = this.baseMapper.selectGoodsIdBySkuIdList(skuIdList, shopId);
                map.put("goodsIds", goodsIds);
                List<String> goodsNums = expressVO.getOrderExpressItemVOList().stream().map(OrderExpressItemVO::getSendGoodsQuantity).collect(Collectors.toList());
                if (goodsNums.size() > 0) {
                    List<Integer> goods = new ArrayList<>();
                    for (String str : goodsNums) {
                        goods.add(Integer.parseInt(str));
                    }
                    map.put("goodsNums", goods);
                }
                map.put("shipmentType", "1");
                map.put("token", shipmentsListDTO.getToken());
                map.put("shopId", ui.getShopId());
                //发货
                result = yiyatongShipmentsMethod(map, isPiLiang);
                if (result.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
                    return result;
                }
            }

        }

        return ResponseResult.success();
    }

    /**
     * 发货
     *
     * @param map
     * @return
     */
    public ResponseResult yiyatongShipmentsMethod(Map<String, Object> map, Integer isPiLiang) throws
            TransactionException {
        ResponseResult rr = new ResponseResult();
        log.info("-----para-----" + map.toString());
        String orderId = (String) map.get("orderId");
        String token = (String) map.get("token");
        String shopId = (String) map.get("shopId");
        //校验订单是否退款
        Integer count = this.baseMapper.checkOrderIsRefund(orderId);
        if (count == 0) {
            return ResponseResult.fail(BizCodeEnume.ORDER_HAVE_REFUND_RETURN);
        }
        boolean isUpdateSuccessByOrderGoods = false;
        boolean isUpdateSuccessByOrderBase;
        try {
            // 发货人姓名
            String orderSellerName = (String) map.get("orderSellerName");
            // 发货人地址
            String orderSellerAddress = (String) map.get("orderSellerAddress");
            // 发货人联系方式
            String orderSellerContact = (String) map.get("orderSellerContact");
            // 配送时间
            String orderShippingTime = DateUtils.getTime();
            //快递公司名称
            String expressName = (String) map.get("expressName");
            //快递公司编码
            String expressPinyin = (String) map.get("expressPinyin");
            Integer orderShippingExpressId = Integer.parseInt(map.get("orderShippingExpressId").toString());
            // 物流单号
            String orderShippingCode = null;
            // 1 物流配送 2 自主配送
            Integer shipmentType = Integer.valueOf(map.get("shipmentType").toString());
            //配送人
            String deliveryPerson = null;
            //配送联系方式
            String deliveryTelephone = null;
            //配送凭证
            String deliveryCertificate = null;
            // shipmentType 1 物流配送 2 自主配送
            if (Objects.equals(CommonType.shipmentType.LOGISTICS_DELIVERY.getCode(), shipmentType)) {

                orderShippingCode = (String) map.get("orderShippingCode");

            } else {
                deliveryPerson = (String) map.get("deliveryPerson");
                deliveryTelephone = (String) map.get("deliveryTelephone");
                deliveryCertificate = (String) map.get("deliveryCertificate");
            }
            Integer storageId = map.get("storageId") != null ? Integer.parseInt(map.get("storageId").toString()) : 0;
            List<StorageInventoryFlowGoodsDTO> storageGoodsList = new ArrayList<>();
            // 商品id
            List<Integer> goodsIds = (List<Integer>) map.get("goodsIds");
            // 商品数量
            List<Integer> goodsNums = (List<Integer>) map.get("goodsNums");
            // 查询订单信息 如状态，退货退款状态，收货人手机号
            OrderBaseEntity qos = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderBaseEntity>()
                    .eq(OrderBaseEntity::getOrderId, orderId));
            String receiverPhone = qos.getOrderReceiverContact();

            // 物流发货备注
            String orderShippingMessage = map.get("orderShippingMessage") == null ?
                    "" : (String) map.get("orderShippingMessage");
            Integer orderStatus = 4;
            // 商品订单是否已经发货。1-是，0-否，默认0(orderGoods)
            Integer orderGoodsIdDeliver = 1;
            Map<String, Object> mapGoods = new HashMap<>();
            mapGoods.put("orderId", orderId);
            mapGoods.put("orderStatus", orderStatus);
            //updateByOrderBase 参数去除
            mapGoods.put("orderGoodsIdDeliver", orderGoodsIdDeliver);
            mapGoods.put("orderSellerName", orderSellerName);
            mapGoods.put("orderSellerAddress", orderSellerAddress);
            mapGoods.put("orderSellerContact", orderSellerContact);
            mapGoods.put("orderShippingTime", orderShippingTime);
//            mapGoods.put("orderShippingExpressId", orderShippingExpressId);
            mapGoods.put("orderShippingCode", orderShippingCode);
            mapGoods.put("orderShippingMessage", orderShippingMessage);
            if (qos.getOrderStatus() == 3 && qos.getOrderRefundStatus().equals("1")) {
                rr.setCode(BizCodeEnume.ORDER_AFTER_SALES.getCode());
                rr.setMessage(BizCodeEnume.ORDER_AFTER_SALES.getMsg());
            } else if (qos.getOrderStatus() == 6 && qos.getOrderRefundStatus().equals("2")) {
                rr.setCode(BizCodeEnume.ORDER_AFTER_SALES_COMPLETION.getCode());
                rr.setMessage(BizCodeEnume.ORDER_AFTER_SALES_COMPLETION.getMsg());
            } else {
                int index = 0;
                Integer goodsNumCount = 0;
                //微信小程序请求订单参数
                Map<String, Object> mapWxData = new HashMap<>();
                //微信小程序订单物流信息列表
                List<Map<String, Object>> shippingList = new ArrayList<>();
                // 获取订单已发货商品总数量
                Integer shipmentQuantity = this.baseMapper.selectOrderShipmentQuantity(orderId);
                String goodsName = "";
                for (Integer goodsId : goodsIds) {
                    StorageInventoryFlowGoodsDTO storageInventoryFlowGoodsDTO = new StorageInventoryFlowGoodsDTO();
                    storageInventoryFlowGoodsDTO.setGoodsId(goodsId);
                    storageInventoryFlowGoodsDTO.setGoodsNum(goodsNums.get(index));
                    storageInventoryFlowGoodsDTO.setBuyNum(goodsNums.get(index));
                    storageGoodsList.add(storageInventoryFlowGoodsDTO);
                    StringBuilder sb = new StringBuilder();
                    OrderGoodsEntity orderGoods = orderGoodsMapper.selectOne(new LambdaQueryWrapper<OrderGoodsEntity>()
                            .eq(OrderGoodsEntity::getOrderId, orderId)
                            .eq(OrderGoodsEntity::getGoodsId, goodsId));
                    goodsName = orderGoods.getGoodsName();
                    String goodsShippingCode = orderGoods.getOrderShippingCode();
                    if (StrUtil.isNotEmpty(goodsShippingCode)) {
                        String[] goodsShippingCodeList = goodsShippingCode.split(",");
                        if (goodsShippingCodeList.length > 1) {
                            for (String code : goodsShippingCodeList) {
                                if (code.equals(goodsShippingCode)) {
                                    sb.append(goodsShippingCode).append(",");
                                    continue;
                                }
                                sb.append(code).append(",");
                            }
                            int lastNum = sb.toString().lastIndexOf(",");
                            goodsShippingCode = (sb).substring(0, lastNum);
                        } else {
                            goodsShippingCode = orderShippingCode;
                        }
                    } else {
                        goodsShippingCode = orderShippingCode;
                    }
                    int goodsNum = goodsNums.get(index) + orderGoods.getShipmentQuantity();
                    if (goodsNum > Integer.parseInt(orderGoods.getOrderGoodsNum())) {
                        return ResponseResult.fail(BizCodeEnume.SHIPMENT_QUANTITY_GT_GOODS__NUM);
                    }
                    mapGoods.put("shipmentQuantity", goodsNum);
                    if (goodsNum >= Integer.parseInt(orderGoods.getOrderGoodsNum())) {
                        mapGoods.put("shipmentStatus", 2);
                    } else {
                        mapGoods.put("shipmentStatus", 1);
                    }
                    goodsNumCount += goodsNums.get(index);
                    mapGoods.put("orderGoodsId", orderGoods.getOrderGoodsId());
                    mapGoods.put("goodsShippingCode", goodsShippingCode);
                    isUpdateSuccessByOrderGoods = orderGoodsMapper.updateByOrderGoods(mapGoods);
                    index++;
                    // 必须是物流配送
                    if (Objects.equals(CommonType.shipmentType.LOGISTICS_DELIVERY.getCode(), shipmentType)) {
                        //微信小程序订单物流信息参数
                        Map<String, Object> shipping = new HashMap<>();
                        //物流单号，物流快递发货时必填
                        shipping.put("tracking_no", orderShippingCode);
                        //物流公司编码，快递公司ID，物流快递发货时必填
                        shipping.put("express_company", expressPinyin);
                        //商品信息，商品名称+商品数量
                        shipping.put("item_desc", orderGoods.getGoodsName() + "*" + orderGoods.getOrderGoodsNum());
                        shippingList.add(shipping);
                    }
                }
                //设置微信小程序订单参数：1、UNIFIED_DELIVERY（统一发货）
                mapWxData.put("delivery_mode", 1);
                Integer orderGoodsNum = this.baseMapper.selectOrderGoodsNum(orderId);
                // 获取订单商品总数量
                Integer shipmentGoodsNum = goodsNumCount;
                goodsNumCount += shipmentQuantity;
                if (!goodsNumCount.equals(orderGoodsNum)) {
                    //部分发货
                    mapGoods.put("orderStatus", 16);

                    //设置微信小程序订单参数：2、SPLIT_DELIVERY（分拆发货）
                    mapWxData.put("delivery_mode", 2);
                }
                // 判断当前单是否是 经销单
                if (Integer.parseInt(mapGoods.get("orderStatus").toString()) == 4
                        && Objects.equals(CommonType.dropShipping.DROPSHIPPING_YES.getCode(), qos.getDropShipping())) {
                    // 查询当前有几个子单
                    // 查询当前单的所有子单是否都发货完成
                    List<OrderBaseEntity> sonOrder = this.baseMapper.querySonOrderShipment(qos.getOrderId());
                    if (null != sonOrder && !sonOrder.isEmpty()) {
                        for (OrderBaseEntity orderBaseEntity : sonOrder) {
                            if (!Objects.equals(orderBaseEntity.getOrderStatus(), CommonType.orderStatus.SHIPPED.getCode())) {
                                //部分发货
                                mapGoods.put("orderStatus", 16);
                                break;
                            }
                        }
                    }
                }
                String orderGoodsIds = JSON.toJSONString(goodsIds);
                String orderGoodsNums = JSON.toJSONString(goodsNums);
                OrderShipmentEntity shipmentEntity = new OrderShipmentEntity(orderId, orderGoodsIds, orderGoodsNums,
                        DateUtils.getNowDateToDf(), orderShippingExpressId, orderShippingCode, orderShippingMessage,
                        shipmentType, deliveryPerson, deliveryTelephone, deliveryCertificate, storageId);
                boolean isInsertShipment = orderShipmentMapper.insert(shipmentEntity) > 0;
                isUpdateSuccessByOrderBase = this.baseMapper.updateByOrderBase(mapGoods);
                if (isUpdateSuccessByOrderGoods && isUpdateSuccessByOrderBase && isInsertShipment) {
                    rr.setCode(CommonStatusEnum.SUCCESS.getCode());
                    rr.setMessage("发货成功！");

                    //判断来源是否为小程序   是物流配送
                    if (qos.getOrderFrom() == 3 && Objects.equals(CommonType.shipmentType.LOGISTICS_DELIVERY.getCode(), shipmentType)) {
                        //微信小程序订单参数
                        WeChatVO result = getWeChatVO(qos, mapWxData, shippingList);
                        //判断微信小程序的订单添加是否成功
                        if (result.getErrcode() != 0) {
                            rr.setCode(result.getErrcode());
                            rr.setMessage(result.getErrmsg());
                            GlobalTransactionContext.reload(RootContext.getXID()).rollback();
                            return rr;
                        }
                    }

                    payMentServiceClient.changeConsumeRecord(orderId);
                    //发货成功短信提醒
                    try {
                        //获取店铺名称
                        String shopName = qos.getShopName();
                        //设置快递名称
                        String ShippingExpressName = expressName;
                        Map<String, Object> paraMap = new HashMap<>();
                        List<String> messageList = new ArrayList<>();
                        messageList.add(shopName);
                        messageList.add(orderShippingCode);
                        messageList.add(ShippingExpressName);
                        paraMap.put("list", messageList);
                        paraMap.put("mobile", receiverPhone);
                        paraMap.put("orderId", qos.getOrderId());
                        paraMap.put("userId", qos.getBuyerUserId());
                        paraMap.put("shopName", shopName);
                        paraMap.put("trackingNumber", orderShippingCode);
                        paraMap.put("expressCompany", ShippingExpressName);
                        paraMap.put("goodsName", goodsName);
                        paraMap.put("goodsNum", shipmentGoodsNum);
                        paraMap.put("templateId", CommonType.messageTemplate.CODE_O.getCode());
                        baseServiceClient.sendMessageTemplate(paraMap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Integer type = CommonType.inventoryFlowType.OUT_STORAGE.getCode();
                    Integer typeDetails = CommonType.inventoryFlowTypeDetail.SALES_OUTBOUND.getCode();
                    setStorageInventoryFlow(storageId, orderId, type, typeDetails, storageGoodsList, goodServiceClient, baseServiceClient, Integer.valueOf(shopId), token);
                } else {
                    rr.setCode(BizCodeEnume.UPDATE_ERROR.getCode());
                    rr.setMessage(BizCodeEnume.UPDATE_ERROR.getMsg());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            rr.setCode(BizCodeEnume.SYSTEM_ERROR.getCode());
            rr.setMessage(BizCodeEnume.SYSTEM_ERROR.getMsg());
            log.info("ShipmentsErrorResults:" + e.getMessage());
            GlobalTransactionContext.reload(RootContext.getXID()).rollback();
        }
        return rr;
    }

    /**
     * 处理微信小程序订单
     *
     * @param obe
     * @param mapWxData
     * @param shippingList
     * @return
     */
    private WeChatVO getWeChatVO(OrderBaseEntity obe, Map<String, Object> mapWxData, List<Map<String, Object>> shippingList) {
        //微信小程序订单参数
        Map<String, Object> orderKey = new HashMap<>();
        //枚举值1，使用下单商户号和商户侧单号；枚举值2，使用微信支付单号
        orderKey.put("order_number_type", 2);
        //支付下单商户的商户号，由微信支付生成并下发。
        orderKey.put("mchid", AccessTokenUtil.queryAppMCHId());
        //原支付交易对应的微信订单号
        orderKey.put("transaction_id", obe.getPaymentOtherNumber());

        //配置订单参数
        mapWxData.put("order_key", orderKey);
        //物流模式，发货方式枚举值：1、实体物流配送采用快递公司进行实体物流配送形式 2、同城配送 3、虚拟商品，虚拟商品，例如话费充值，点卡等，无实体配送形式 4、用户自提
        mapWxData.put("logistics_type", 1);

        //物流信息参数
        mapWxData.put("shipping_list", shippingList);
        //上传时间，用于标识请求的先后顺序 示例值: `2022-12-15T13:29:35.120+08:00`
        mapWxData.put("upload_time", DateUtils.getSpecialNowDate());

        //查询订单买家的openId
        String openId = userServiceClient.queryBindInfoByuserId(obe.getBuyerUserId());
        //微信小程序支付者参数-openId
        Map<String, Object> payer = new HashMap<>();
        payer.put("openid", openId);
        //支付者，支付者信息
        mapWxData.put("payer", payer);

        //设置参数
        String accessToken = AccessTokenUtil.queryAccessToken();
        String host = CommonType.wxApiUrl.API_WECHAT_QQ_URL.getDesc();
        String addPath = CommonType.wxApiUrl.WX_SHIPPING_INFO.getDesc() + accessToken;
        return restTemplate.postForObject(host + addPath, mapWxData, WeChatVO.class);
    }

    /**
     * 怡亚通售后
     *
     * @param ReturnListDTO
     * @return
     */
    @Override
    public ResponseResult yiyatongReturn(ReturnListDTO ReturnListDTO) throws TransactionException {
        for (ReturnYiYaTongInfoDTO vo : ReturnListDTO.getReturnList()) {
            //查询怡亚通订单信息
            OrderYiyatongEntity orderYiyatongEntity = orderYiyatongMapper.selectOne(new LambdaQueryWrapper<OrderYiyatongEntity>()
                    .eq(OrderYiyatongEntity::getOrderSn, vo.getOrderSn()));
            CancelRefundDTO dto = new CancelRefundDTO();
            dto.setReturnSn(vo.getReturnSn());
            //根据售后单号,查询怡亚通售后详情
            ResponseResult result = goodServiceClient.orderReturnGoodsDetail(dto);
            if (result.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
                return result;
            }
            OrderReturnGoodsVO orderReturnGoodsVO = JSONObject.parseObject(JSON.toJSONString(result.getData()), OrderReturnGoodsVO.class);
            //退货退款单号
            OrderReturnEntity orderReturnEntity = orderReturnMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>()
                    .select(OrderReturnEntity::getReturnCode)
                    .eq(OrderReturnEntity::getOrderId, orderYiyatongEntity.getOrderId()));
            //订单详情
            OrderBaseEntity orderBaseEntity = this.baseMapper.selectById(orderYiyatongEntity.getOrderId());
            //获取店铺token
            String token = RedisUtil.get(HouConfig.HOU_SHOP_TOKEN_SHOP + orderBaseEntity.getShopId());
            /* 售后状态
             售后状态(
            一：退款
            1,待卖家审核
            2,卖家拒绝退款：
            3,退款成功：
            二：退货退款
            4,卖家拒绝退货退款：
            5,待买家退货：
            6,买家已退货，待卖家收货：
            7,买家已退货，卖家拒绝收货：
            8,卖家已收货,待确认退款：
            9,退货退款成功：
            三：换货
            10,卖家拒绝换货：
            11,买家已退货，待卖家换货：
            12,卖家已换货，待买家收货：
            13,换货成功：
            14,已关闭：
            四：仅退款,五：部分退款
            15,卖家同意退款,
            16：卖家同意退款,
            17：卖家拒绝退款，
            18：退款成功)*/
            if (orderReturnGoodsVO.getReturnStatus() == 2 || orderReturnGoodsVO.getReturnStatus() == 17) {
                Map<String, String> param = new HashMap<>();
                param.put("type", "0");
                param.put("returnCode", orderReturnEntity.getReturnCode());
                param.put("returnShopMessage", orderReturnGoodsVO.getSupplierReason());
                param.put("token", token);
                orderReturnService.forSellerApprove(param);
            }
            if (orderReturnGoodsVO.getReturnStatus() == 3 || orderReturnGoodsVO.getReturnStatus() == 15 || orderReturnGoodsVO.getReturnStatus() == 16) {
                Map<String, String> param = new HashMap<>();
                param.put("type", "1");
                param.put("returnCode", orderReturnEntity.getReturnCode());
                param.put("returnShopMessage", orderReturnGoodsVO.getSupplierReason());
                param.put("token", token);
                orderReturnService.forSellerApprove(param);
            }
            //卖家拒绝退货退款
            if (orderReturnGoodsVO.getReturnStatus() == 4) {
                Map<String, String> param = new HashMap<>();
                param.put("type", "0");
                param.put("returnCode", orderReturnEntity.getReturnCode());
                param.put("returnShopMessage", orderReturnGoodsVO.getSupplierReason());
                param.put("returnAddr", orderReturnGoodsVO.getUserAddress());
                param.put("token", token);
                orderReturnService.forSellerApprove(param);
            }
            // 卖家同意,待买家退货：
            if (orderReturnGoodsVO.getReturnStatus() == 5) {
                Map<String, String> param = new HashMap<>();
                param.put("type", "1");
                param.put("returnCode", orderReturnEntity.getReturnCode());
                param.put("returnShopMessage", orderReturnGoodsVO.getSupplierReason());
                param.put("returnAddr", orderReturnGoodsVO.getUserAddress());
                param.put("token", token);
                orderReturnService.forSellerApprove(param);
            }
            //买家已退货，卖家拒绝收货：
            if (orderReturnGoodsVO.getReturnStatus() == 7) {
                Map<String, String> param = new HashMap<>();
                param.put("type", "0");
                param.put("returnCode", orderReturnEntity.getReturnCode());
                param.put("returnShopMessage", orderReturnGoodsVO.getSupplierReason());
                param.put("token", token);
                orderReturnService.retForSellerReceive(param);
            }
            //卖家已收货,待确认退款：
            if (orderReturnGoodsVO.getReturnStatus() == 8) {
                Map<String, String> param = new HashMap<>();
                param.put("type", "1");
                param.put("returnCode", orderReturnEntity.getReturnCode());
                param.put("returnShopMessage", orderReturnGoodsVO.getSupplierReason());
                param.put("token", token);
                orderReturnService.retForSellerReceive(param);
            }
        }
        return ResponseResult.success();
    }

    /**
     * 发货
     *
     * @param map
     * @return
     */
    @Override
    public ResponseResult supplierShipments(Map<String, Object> map) throws TransactionException {
        String token = map.get("token").toString();
        ResponseResult rr = ParseTokenUtils.getShopUserInfo(token);
        if (rr.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return rr;
        }
//        map.put("role",1);
        return supplierShipmentsMthod(map, CommonType.commonStatus.NO.getCode());
    }

    /**
     * 发货
     *
     * @param map
     * @return
     */
    public ResponseResult supplierShipmentsMthod(Map<String, Object> map, Integer isPiLiang) throws
            TransactionException {
        ResponseResult rr = new ResponseResult();
        log.info("-----para-----" + map.toString());
        String orderId = (String) map.get("orderId");
        //校验订单是否退款
        Integer count = this.baseMapper.checkOrderIsRefund(orderId);
        if (count == 0) {
            return ResponseResult.fail(BizCodeEnume.ORDER_HAVE_REFUND_RETURN);
        }
        boolean isUpdateSuccessByOrderGoods = false;
        boolean isUpdateSuccessByOrderBase;
        try {
            // 商家所用发货地址id
            Integer shippingAddressId = null;
            // 发货人姓名
            String orderSellerName = (String) map.get("orderSellerName");
            // 发货人地址
            String orderSellerAddress = (String) map.get("orderSellerAddress");
            // 发货人联系方式
            String orderSellerContact = (String) map.get("orderSellerContact");
            // 配送时间
            String orderShippingTime = DateUtils.getTime();
            // 配送公司ID
            Integer orderShippingExpressId = null;
            // 物流单号
            String orderShippingCode = null;
            // 1 物流配送 2 自主配送
            Integer shipmentType = Integer.valueOf(map.get("shipmentType").toString());
            //配送人
            String deliveryPerson = null;
            //配送联系方式
            String deliveryTelephone = null;
            //配送凭证
            String deliveryCertificate = null;
            // shipmentType 1 物流配送 2 自主配送
            if (Objects.equals(CommonType.shipmentType.LOGISTICS_DELIVERY.getCode(), shipmentType)) {
                orderShippingExpressId = Integer.parseInt(map.get("orderShippingExpressId").toString());
                orderShippingCode = (String) map.get("orderShippingCode");
                shippingAddressId = Integer.parseInt(map.get("shippingAddressId").toString());
            } else {
                deliveryPerson = (String) map.get("deliveryPerson");
                deliveryTelephone = (String) map.get("deliveryTelephone");
                deliveryCertificate = (String) map.get("deliveryCertificate");
            }

            // 商品id
            List<Integer> goodsIds = (List<Integer>) map.get("goodsIds");
            // 经销商品id
            List<Integer> dealerGoodsIds = new ArrayList<>();
            // 商品数量
            List<Integer> goodsNums = (List<Integer>) map.get("goodsNums");
            // 查询订单信息 如状态，退货退款状态，收货人手机号
            OrderBaseEntity qos = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderBaseEntity>()
                    .eq(OrderBaseEntity::getOrderId, orderId));
            String receiverPhone = qos.getOrderReceiverContact();
            BaseExpressDTO shipperCode = null;
            //物流配送
            if (CommonType.shipmentType.LOGISTICS_DELIVERY.getCode().equals(shipmentType)) {
                ResponseResult express = baseServiceClient.getBaseExpressInfoById(orderShippingExpressId);
                shipperCode = JSONObject.parseObject(JSON.toJSONString(express.getData()), BaseExpressDTO.class);
            }
            // 物流发货备注
            String orderShippingMessage = map.get("orderShippingMessage") == null ?
                    "" : (String) map.get("orderShippingMessage");
            Integer orderStatus = 4;
            // 商品订单是否已经发货。1-是，0-否，默认0(orderGoods)
            Integer orderGoodsIdDeliver = 1;
            Map<String, Object> mapGoods = new HashMap<>();
            mapGoods.put("orderId", orderId);
            mapGoods.put("orderStatus", orderStatus);
            //updateByOrderBase 参数去除
            mapGoods.put("orderGoodsIdDeliver", orderGoodsIdDeliver);
            mapGoods.put("orderSellerName", orderSellerName);
            mapGoods.put("orderSellerAddress", orderSellerAddress);
            mapGoods.put("orderSellerContact", orderSellerContact);
            mapGoods.put("orderShippingTime", orderShippingTime);
            mapGoods.put("orderShippingExpressId", orderShippingExpressId);
            mapGoods.put("orderShippingCode", orderShippingCode);
            mapGoods.put("orderShippingMessage", orderShippingMessage);
            //后台配置的自动确认收货时间
            String configValue = baseServiceClient.selectConfigValueFeign(CommonType.configKey.AUTOMATIC_DELIVERY_TIME.getDesc());
            Integer automaticDeliveryTime = Integer.parseInt(configValue);
            mapGoods.put("automaticDeliveryTime", automaticDeliveryTime);
            if (qos.getOrderStatus() == 3 && qos.getOrderRefundStatus().equals("1")) {
                rr.setCode(BizCodeEnume.ORDER_AFTER_SALES.getCode());
                rr.setMessage(BizCodeEnume.ORDER_AFTER_SALES.getMsg());
            } else if (qos.getOrderStatus() == 6 && qos.getOrderRefundStatus().equals("2")) {
                rr.setCode(BizCodeEnume.ORDER_AFTER_SALES_COMPLETION.getCode());
                rr.setMessage(BizCodeEnume.ORDER_AFTER_SALES_COMPLETION.getMsg());
            } else {
                int index = 0;
                Integer goodsNumCount = 0;
                //微信小程序请求订单参数
                Map<String, Object> mapWxData = new HashMap<>();
                //微信小程序订单物流信息列表
                List<Map<String, Object>> shippingList = new ArrayList<>();
                // 获取订单已发货商品总数量
                Integer shipmentQuantity = this.baseMapper.selectOrderShipmentQuantity(orderId);
                String goodsName = "";
                for (Integer goodsId : goodsIds) {
                    StringBuilder sb = new StringBuilder();
                    OrderGoodsEntity orderGoods = orderGoodsMapper.selectOne(new LambdaQueryWrapper<OrderGoodsEntity>()
                            .eq(OrderGoodsEntity::getOrderId, orderId)
                            .eq(OrderGoodsEntity::getGoodsId, goodsId));
                    goodsName = orderGoods.getGoodsName();
                    String goodsShippingCode = orderGoods.getOrderShippingCode();
                    if (StrUtil.isNotEmpty(goodsShippingCode)) {
                        String[] goodsShippingCodeList = goodsShippingCode.split(",");
                        if (goodsShippingCodeList.length > 1) {
                            for (String code : goodsShippingCodeList) {
                                if (code.equals(goodsShippingCode)) {
                                    sb.append(goodsShippingCode).append(",");
                                    continue;
                                }
                                sb.append(code).append(",");
                            }
                            int lastNum = sb.toString().lastIndexOf(",");
                            goodsShippingCode = (sb).substring(0, lastNum);
                        } else {
                            goodsShippingCode = orderShippingCode;
                        }
                    } else {
                        goodsShippingCode = orderShippingCode;
                    }
                    int goodsNum = goodsNums.get(index) + orderGoods.getShipmentQuantity();
                    if (goodsNum > Integer.parseInt(orderGoods.getOrderGoodsNum())) {
                        return ResponseResult.fail(BizCodeEnume.SHIPMENT_QUANTITY_GT_GOODS__NUM);
                    }
                    mapGoods.put("shipmentQuantity", goodsNum);
                    if (goodsNum >= Integer.parseInt(orderGoods.getOrderGoodsNum())) {
                        mapGoods.put("shipmentStatus", 2);
                    } else {
                        mapGoods.put("shipmentStatus", 1);
                    }
                    goodsNumCount += goodsNums.get(index);
                    mapGoods.put("orderGoodsId", orderGoods.getOrderGoodsId());
                    mapGoods.put("goodsShippingCode", goodsShippingCode);
                    isUpdateSuccessByOrderGoods = orderGoodsMapper.updateByOrderGoods(mapGoods);
                    // 换取经销商品的订单id
                    if (null != qos.getOriginalOrder()) {
                        // 查询经销商shopId
                        Integer dealerShopId = this.baseMapper.queryDealserShopId(qos.getOriginalOrder());
                        // 查询经销商品id
                        Integer dealerGoodsId = orderGoodsMapper.queryDealerGoodsId(dealerShopId, goodsId);
                        orderGoodsMapper.updateByDealerOrderGoods(qos.getOriginalOrder(), dealerGoodsId, mapGoods);
                        dealerGoodsIds.add(dealerGoodsId);
                    }
                    index++;
                    // 必须是物流配送
                    if (Objects.equals(CommonType.shipmentType.LOGISTICS_DELIVERY.getCode(), shipmentType)) {
                        //微信小程序订单物流信息参数
                        Map<String, Object> shipping = new HashMap<>();
                        //物流单号，物流快递发货时必填
                        shipping.put("tracking_no", orderShippingCode);
                        //物流公司编码，快递公司ID，物流快递发货时必填
                        shipping.put("express_company", shipperCode.getExpressPinyin());
                        //商品信息，商品名称+商品数量
                        shipping.put("item_desc", orderGoods.getGoodsName() + "*" + orderGoods.getOrderGoodsNum());
                        shippingList.add(shipping);
                    }
                }
                //设置微信小程序订单参数：1、UNIFIED_DELIVERY（统一发货）
                mapWxData.put("delivery_mode", 1);
                Integer orderGoodsNum = this.baseMapper.selectOrderGoodsNum(orderId);
                // 获取订单商品总数量
                Integer shipmentGoodsNum = goodsNumCount;
                goodsNumCount += shipmentQuantity;
                if (!goodsNumCount.equals(orderGoodsNum)) {
                    //部分发货
                    mapGoods.put("orderStatus", 16);

                    //设置微信小程序订单参数：2、SPLIT_DELIVERY（分拆发货）
                    mapWxData.put("delivery_mode", 2);
                }
                String orderGoodsIds = JSON.toJSONString(goodsIds);
                String orderGoodsNums = JSON.toJSONString(goodsNums);
                OrderShipmentEntity shipmentEntity = new OrderShipmentEntity(orderId, orderGoodsIds, orderGoodsNums,
                        DateUtils.getNowDateToDf(), orderShippingExpressId, orderShippingCode, orderShippingMessage,
                        shipmentType, deliveryPerson, deliveryTelephone, deliveryCertificate, 0);
                Boolean isInsertShipment = orderShipmentMapper.insert(shipmentEntity) > 0;
                if (null != qos.getOriginalOrder()) {
                    String fathOrderGoodsIds = JSON.toJSONString(dealerGoodsIds);
                    // 插入主单返货数据
                    OrderShipmentEntity fathSshipmentEntity = new OrderShipmentEntity(qos.getOriginalOrder(), fathOrderGoodsIds, orderGoodsNums,
                            DateUtils.getNowDateToDf(), orderShippingExpressId, orderShippingCode, orderShippingMessage,
                            shipmentType, deliveryPerson, deliveryTelephone, deliveryCertificate, 0, orderSellerAddress);
                    orderShipmentMapper.insert(fathSshipmentEntity);
                }
                isUpdateSuccessByOrderBase = this.baseMapper.updateByOrderBase(mapGoods);
                if (isUpdateSuccessByOrderGoods && isUpdateSuccessByOrderBase && isInsertShipment) {
                    rr.setCode(CommonStatusEnum.SUCCESS.getCode());
                    rr.setMessage("发货成功！");

                    //判断来源是否为小程序   是物流配送
                    if (qos.getOrderFrom() == 3 && CommonType.shipmentType.LOGISTICS_DELIVERY.getCode().equals(shipmentType)) {
                        //微信小程序订单参数
                        WeChatVO result = getWeChatVO(qos, mapWxData, shippingList);
                        //判断微信小程序的订单添加是否成功
                        if (result.getErrcode() != 0) {
                            rr.setCode(result.getErrcode());
                            rr.setMessage(result.getErrmsg());
                            GlobalTransactionContext.reload(RootContext.getXID()).rollback();
                            return rr;
                        }
                    }

                    payMentServiceClient.changeConsumeRecord(orderId);
                    // 必须是物流配送
                    if (CommonType.commonStatus.YES.getCode().equals(isPiLiang) && CommonType.shipmentType.LOGISTICS_DELIVERY.getCode().equals(shipmentType)) {
                        // 信息
                        rr = merchantServiceClient.queryShippingAddress(shippingAddressId);
                        String senderAddress = (String) rr.getData();
                        String requestData = sendKdniaoMessage(senderAddress, qos, orderShippingCode,
                                orderSellerName, orderSellerContact, orderSellerAddress, shipperCode);
                        String results = KdniaoSubscribeAPI.orderTracesSubByJson(requestData);
                        System.out.println("-----LogisticsTrackingReturnResults-----" + results);
                    }
                    //发货成功短信提醒
                    try {
                        //获取店铺名称
                        String shopName = qos.getShopName();
                        //设置快递名称
                        String ShippingExpressName = shipperCode.getExpressName();
                        Map<String, Object> paraMap = new HashMap<>();
                        List<String> messageList = new ArrayList<>();
                        messageList.add(shopName);
                        messageList.add(orderShippingCode);
                        messageList.add(ShippingExpressName);
                        paraMap.put("list", messageList);
                        paraMap.put("mobile", receiverPhone);
                        paraMap.put("orderId", qos.getOrderId());
                        paraMap.put("userId", qos.getBuyerUserId());
                        paraMap.put("shopName", shopName);
                        paraMap.put("trackingNumber", orderShippingCode);
                        paraMap.put("expressCompany", ShippingExpressName);
                        paraMap.put("goodsName", goodsName);
                        paraMap.put("goodsNum", shipmentGoodsNum);
                        paraMap.put("templateId", CommonType.messageTemplate.CODE_O.getCode());
                        baseServiceClient.sendMessageTemplate(paraMap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //供应商商品全部发货完成,并且是经销订单
                    if (null != qos.getOriginalOrder()) {
                        Boolean shipmentsStart = false;
                        // 查询主订单,除了待发货商品以外,是否全部发货完成
                        List<ShipmentsByOrderGoodsDetailsVO> queryOrderDetails = this.orderGoodsMapper.shipmentsByOrderGoodsDetails(qos.getOriginalOrder());
                        if (queryOrderDetails.isEmpty() && Integer.parseInt(mapGoods.get("orderStatus").toString()) == 4) {
                            shipmentsStart = true;
                        }

                        // 查询除了当前外的子单是否已发货完成
                        List<ShipmentsByOrderGoodsDetailsVO> querySonOrderDetails = this.orderGoodsMapper.querySonOrderShipmentsStart(qos.getOriginalOrder(), qos.getOrderId());
                        if (querySonOrderDetails != null && !querySonOrderDetails.isEmpty()) {
                            for (ShipmentsByOrderGoodsDetailsVO querySonOrderDetail : querySonOrderDetails) {
                                if (querySonOrderDetail.getShipmentStatus() != 2) {
                                    // 证明其他子单中有商品未完全发货成功
                                    shipmentsStart = false;
                                    break;
                                }
                            }
                        }
                        // 修改主单状态
                        if (shipmentsStart) {
                            mapGoods.put("orderStatus", 4);
                        } else {
                            mapGoods.put("orderStatus", 16);
                        }
                        this.baseMapper.updateByDealerOrderBase(qos.getOriginalOrder(), mapGoods);
                    }
                } else {
                    rr.setCode(BizCodeEnume.UPDATE_ERROR.getCode());
                    rr.setMessage(BizCodeEnume.UPDATE_ERROR.getMsg());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            rr.setCode(BizCodeEnume.SYSTEM_ERROR.getCode());
            rr.setMessage(BizCodeEnume.SYSTEM_ERROR.getMsg());
            log.info("ShipmentsErrorResults:" + e.getMessage());
            GlobalTransactionContext.reload(RootContext.getXID()).rollback();
        }
        return rr;
    }

    @Override
    public ResponseResult yiyatongSoldOrders(Map<String, Object> map) {
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        Integer shopId = Integer.valueOf(ui.getShopId());
        //条件参数
        map.put("shopId", shopId);
        //自定义sql:
        IPage<YiYaTongOrderVO> page = this.baseMapper.queryYiYaTongOrderByPage(new Query<Map<String, Object>>().getPage(map),
                map);


        return ResponseResult.success(PageUtils.getPage(page));
    }

    @Override
    public ResponseResult yiyatongSoldOrdersExp(Map<String, Object> map) {
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        Integer shopId = Integer.valueOf(ui.getShopId());
        //条件参数
        map.put("shopId", shopId);
        map.put("code", CommonType.orderType.GOODS_TYPE.getCode());
        List<YiYaTongOrderVO> orderList = this.baseMapper.queryYiYaTongOrderByPage(map);

        String[] title = {"订单编号", "订单状态", "买家名称", "收件人姓名", "手机", "收货地址", "运费", "实付款", "商品名称",
                "商品协议价", "商品销售价", "数量", "优惠", "下单时间", "付款时间", "完成时间", "售后类型", "售后时间", "售后完成时间"};
        List<Map<String, String>> list = Lists.newArrayList();
        for (YiYaTongOrderVO soldOrderExportDTO : orderList) {
            Integer orderStatus = soldOrderExportDTO.getOrderStatus();
            String statusName = null;
            if (orderStatus == 1) {
                statusName = "待付款";
            } else if (orderStatus == 2) {
                statusName = "待配货";
            } else if (orderStatus == 3) {
                statusName = "待发货";
            } else if (orderStatus == 4) {
                statusName = "已发货";
            } else if (orderStatus == 5) {
                statusName = "已签收";
            } else if (orderStatus == 6) {
                statusName = "已完成";
            } else if (orderStatus == 7) {
                statusName = "已取消";
            } else if (orderStatus == 8) {
                statusName = "退款中";
            } else if (orderStatus == 9) {
                statusName = "退款完成";
            }
            String returnName = null;
            if (orderStatus == 1) {
                returnName = "退款";

            } else if (orderStatus == 2) {
                returnName = "仅退款";
            } else if (orderStatus == 3) {
                returnName = "退货退款";
            }
            HashMap<String, String> mPut = Maps.newHashMap();
            mPut.put("订单编号", soldOrderExportDTO.getOrderId());
            mPut.put("订单状态", statusName);
            mPut.put("买家名称", soldOrderExportDTO.getBuyerUserName());
            mPut.put("收件人姓名", soldOrderExportDTO.getOrderReceiverName());
            mPut.put("手机", soldOrderExportDTO.getOrderReceiverContact());
            mPut.put("收货地址", soldOrderExportDTO.getOrderReceiverAddress());
            mPut.put("运费", soldOrderExportDTO.getShippingFee().toString());
            mPut.put("实付款", soldOrderExportDTO.getOrderGoodsPaymentAmount().toString());
            mPut.put("商品名称", soldOrderExportDTO.getGoodsName());
            mPut.put("商品协议价", soldOrderExportDTO.getGoodsPrice().toString());
            mPut.put("商品销售价", soldOrderExportDTO.getMsrp().toString());
            mPut.put("数量", soldOrderExportDTO.getOrderGoodsNum().toString());
            mPut.put("优惠", soldOrderExportDTO.getOrderGoodsDiscountFee() == null ? "0" : soldOrderExportDTO.getOrderGoodsDiscountFee().toString());
            mPut.put("下单时间", soldOrderExportDTO.getOrderCreateTime());
            mPut.put("付款时间", soldOrderExportDTO.getPaymentTime());
            mPut.put("完成时间", soldOrderExportDTO.getOrderFinishedTime());
            mPut.put("售后类型", returnName);
            mPut.put("售后时间", soldOrderExportDTO.getReturnCreateTime());
            mPut.put("售后完成时间", soldOrderExportDTO.getReturnFinishTime());
            list.add(mPut);
        }
        Map<String, List<Map<String, String>>> m = Maps.newHashMap();
        m.put("怡亚通订单明细数据", list);
        String url = ExcelsUtil.createExcel(title, m, new int[]{0});
        return ResponseResult.success(url);
    }
    /**
     * 供应商 已售订单
     *
     * @param map
     * @return
     */
    @Override
    public ResponseResult soldSupplierOrders(Map<String, Object> map) {
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        PageInfo pi = new PageInfo();
        Integer shopId = Integer.valueOf(ui.getShopId());
        //条件参数
        map.put("shopId", shopId);
        IPage<OrderDetailsVO> page = this.baseMapper.queryOrderBaseOfSupplierShop(new Query<OrderDetailsVO>().getPage(map), map);
        // 设置订单信息
        ResponseResult setResult = setSupplierOrderDetails(page.getRecords());
        if (CommonStatusEnum.SUCCESS.getCode() != setResult.getCode()) {
            return setResult;
        }
        return ResponseResult.success(PageUtils.getPage(page));
    }
    /**
     * 供应商 已售订单 导出
     *
     * @param map
     * @return
     */
    @Override
    public ResponseResult soldSupplierOrdersExp(Map<String, Object> map) {
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        if(null != ui){
            Integer shopId = Integer.valueOf(ui.getShopId());
            //条件参数
            map.put("shopId", shopId);
            List<OrderExportVO> orderDetails = this.baseMapper.soldSupplierOrdersExp(map);
            String[] title = {"订单号","订单状态", "买家昵称",  "实付款", "商品名称",
                    "商家编码", "销售属性", "单价", "数量", "优惠", "下单时间", "付款时间", "买家留言"};
            List<Map<String, String>> list = Lists.newArrayList();
            // 处理查询结果，设置订单的最新状态
            for (OrderExportVO orderExportVO : orderDetails) {
                Integer orderStatus = orderExportVO.getOrderStatus();
                Integer orderRefundStatus = orderExportVO.getOrderRefundStatus();
                Integer orderReturnStatus = orderExportVO.getOrderReturnStatus();
                String statusName = null;
                if (orderStatus == 1) {
                    statusName = "待付款";
                } else if (orderStatus == 2) {
                    statusName = "待配货";
                } else if (orderStatus == 3) {
                    if(orderRefundStatus == 1){
                        statusName = "退款申请中";
                    }else if(orderRefundStatus == 2){
                        statusName = "退款完成";
                    }else if(orderRefundStatus == 3){
                        statusName = "退款不通过";
                    }else{
                        statusName = "待发货";
                    }
                } else if (orderStatus == 4) {
                    statusName = "已发货";
                } else if (orderStatus == 5) {
                    statusName = "已签收";
                } else if (orderStatus == 6) {
                    if(orderReturnStatus == 1){
                        statusName = "退货申请中";
                    }else if(orderReturnStatus == 2){
                        statusName = "退货完成";
                    }else if(orderReturnStatus == 3){
                        statusName = "退货不通过";
                    }else{
                        statusName = "已完成";
                    }
                } else if (orderStatus == 7) {
                    statusName = "已取消";
                } else if (orderStatus == 8) {
                    statusName = "退款中";
                } else if (orderStatus == 9) {
                    if(orderRefundStatus == 2){
                        statusName = "退款完成";
                    }else{
                        statusName = "退货完成";
                    }
                }else if (orderStatus == 16) {
                    statusName = "部分发货";

                }
                HashMap<String, String> mPut = Maps.newHashMap();
                mPut.put("订单号", orderExportVO.getOrderId());
                mPut.put("订单状态",statusName);
                mPut.put("买家昵称", orderExportVO.getBuyerUserName());
                mPut.put("实付款", orderExportVO.getOrderPaymentAmount().toString());
                mPut.put("商品名称", orderExportVO.getGoodsName());
                mPut.put("商家编码", orderExportVO.getShopId());
                mPut.put("销售属性", orderExportVO.getOrderSpecChineseValue());
                mPut.put("单价", orderExportVO.getGoodsPrice().toString());
                mPut.put("数量", orderExportVO.getOrderGoodsNum().toString());
                mPut.put("优惠", orderExportVO.getOrderDiscountFee() == null ? "0" : orderExportVO.getOrderDiscountFee().toString());
                mPut.put("下单时间", orderExportVO.getOrderCreateTime());
                mPut.put("付款时间", orderExportVO.getPaymentTime());
                mPut.put("买家留言", orderExportVO.getOrderMessage());
                list.add(mPut);
            }
            Map<String, List<Map<String, String>>> m = Maps.newHashMap();
            m.put("供应商已售订单", list);
            String data = ExcelsUtil.createExcel(title, m, new int[]{0});
            return ResponseResult.success(data);
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());

    }

    /**
     * 设置订单信息
     *
     * @param orderDetails 订单详情对象
     * @return 订单详情
     */
    private ResponseResult setSupplierOrderDetails(List<OrderDetailsVO> orderDetails) {
        int count = 0;
        Map<String, String> map = new HashMap<>();
        for (OrderDetailsVO orderDetail : orderDetails) {
            String orderId = orderDetail.getOrderId();
            // 退款状态
            Integer orderRefundStatus = orderDetail.getOrderRefundStatus();
            // 退货状态
            Integer orderReturnStatus = orderDetail.getOrderReturnStatus();
            // 订单状态
            Integer normalStatus = orderDetail.getOrderStatus();
            // 涉及退款退货订单状态
            Integer Orderstatus = null;
            //获取账号开户行，
            InvoiceBankDTO invoice = orderInvoiceInfoMapper.queryInvoiceInfo(orderId);
            if (invoice != null) {
                orderDetail.setBankAccount(invoice.getBankAccount());
                orderDetail.setOpeningBank(invoice.getOpeningBank());
            } else {
                orderDetail.setBankAccount("-");
                orderDetail.setOpeningBank("-");
            }

            if (normalStatus == 3) {
                // 未发货状态
                if (orderRefundStatus == 1) {
                    // 未发货退款中
                    Orderstatus = 22;
                } else if (orderRefundStatus == 2) {
                    // 商家拒绝或审核通过，继续发货，但不可再次申请退款
                    Orderstatus = 26;
                } else if (orderRefundStatus == 0) {
                    Orderstatus = 3;
                }
            } else if (normalStatus == 6) {
                // 已完成状态
                if (orderRefundStatus == 1) {
                    // 收货退款中
                    Orderstatus = 23;
                } else if (orderRefundStatus == 2) {
                    Orderstatus = 25;
                } else if (orderRefundStatus == 0 && orderReturnStatus == 0) {
                    Orderstatus = 6;
                } else if (orderReturnStatus == 1) {
                    // 收货退货中
                    Orderstatus = 24;
                } else if (orderReturnStatus == 2) {
                    Orderstatus = 25;
                }
            } else {
                Orderstatus = normalStatus;
            }
            BigDecimal dealerDiscountAmout = new BigDecimal(0);
            // 存放最新状态
            orderDetail.setOrderStatus(Orderstatus);
            List<GoodsDetailsVO> queryOrderDetails = orderGoodsMapper.selectSupplierOrderDetails(orderId);
            if (queryOrderDetails != null) {
                for (GoodsDetailsVO queryOrderDetail : queryOrderDetails) {
                    if (null == orderDetail.getSubOrderNo()) {
                        // 查询供应商经销折扣
                        OrderDealerInfoEntity orderDealerInfoEntity = orderDealerInfoMapper.queryOrderGoodsDealerDiscount(orderId, queryOrderDetail.getGoodsId());
                        if (null != orderDealerInfoEntity) {
                            dealerDiscountAmout = dealerDiscountAmout.add(orderDealerInfoEntity.getDealerDiscountAmout());
                            if (null != orderDealerInfoEntity.getDealerDiscount() && orderDealerInfoEntity.getDealerDiscount().compareTo(BigDecimal.ZERO) > 0) {
                                queryOrderDetail.setGoodsPrice(String.valueOf(orderDealerInfoEntity.getTaxInclusiveAmount().multiply(orderDealerInfoEntity.getDealerDiscount()).setScale(2, RoundingMode.HALF_UP)));
                            } else {
                                queryOrderDetail.setGoodsPrice(String.valueOf(orderDealerInfoEntity.getTaxInclusiveAmount()));
                            }
                            queryOrderDetail.setDealerDiscount(orderDealerInfoEntity.getDealerDiscount());
                        }
                    } else {
                        // 查询供应商经销折扣
                        OrderDealerInfoEntity orderDealerInfoEntity = orderDealerInfoMapper.queryOrderGoodsDealerDiscountBySubOrderNo(orderDetail.getSubOrderNo(), queryOrderDetail.getGoodsId());
                        if (null != orderDealerInfoEntity) {
                            dealerDiscountAmout = dealerDiscountAmout.add(orderDealerInfoEntity.getDealerDiscountAmout());
                            if (null != orderDealerInfoEntity.getDealerDiscount() && orderDealerInfoEntity.getDealerDiscount().compareTo(BigDecimal.ZERO) > 0) {
                                queryOrderDetail.setGoodsPrice(String.valueOf(orderDealerInfoEntity.getTaxInclusiveAmount().multiply(orderDealerInfoEntity.getDealerDiscount()).setScale(2, RoundingMode.HALF_UP)));
                            } else {
                                queryOrderDetail.setGoodsPrice(String.valueOf(orderDealerInfoEntity.getTaxInclusiveAmount()));
                            }
                            queryOrderDetail.setDealerDiscount(orderDealerInfoEntity.getDealerDiscount());
                        }
                    }
                }
                orderDetail.setDealerDiscountAmout(dealerDiscountAmout);
                orderDetail.setGoodsDetails(queryOrderDetails);
            } else {
                return ResponseResult.fail(BizCodeEnume.ORDER_IS_NULL);
            }
            if (CommonType.dropShipping.DROPSHIPPING_YES.getCode().equals(orderDetail.getDropShipping())) {
                // 当前订单是 子单 查询 主单 及商家信息
                String dealerShopName = this.baseMapper.queryDealerShopName(orderDetail.getSubOrderNo());
                orderDetail.setDealerShopName(dealerShopName);
                orderDetail.setFathOrderNo(orderDetail.getSubOrderNo());
            }
        }
        return ResponseResult.success();
    }

    @Override
    public ResponseResult shipmentsSupplierOrder(Map<Object, Object> map) {
        String token = map.get("token").toString();
        ResponseResult rr = ParseTokenUtils.getShopUserInfo(token);
        if (rr.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return rr;
        }
        UserInfoVO ui = JSON.parseObject(JSON.toJSONString(rr.getData()), UserInfoVO.class);
        String Seller_Trade_Deliver = "Seller_Trade_Deliver";
        Integer shopId = Integer.valueOf(ui.getShopId());
        if (containLimits(ui, Seller_Trade_Deliver)) {
            // 买家
            String userName = (String) map.get("userName");
            // 订单号
            String orderNum = (String) map.get("orderNum");
            // 下单开始时间
            String orderStartTime = (String) map.get("orderStartTime");
            // 下单结束时间
            String orderEndTime = (String) map.get("orderEndTime");
            // 发货状态
            Integer status = Integer.parseInt(map.get("status") + "");
            Integer currentPage = Integer.parseInt(map.get("page") + "");
            Integer pageSize = Integer.parseInt(map.get("limit") + "");
            Page<ShipmentsByOrderBaseVO> page = new Page<>(currentPage, pageSize);
            Map<String, Object> mapBase = new HashMap<>();
            mapBase.put("shopId", shopId);
            mapBase.put("status", status);
            mapBase.put("userName", userName);
            mapBase.put("orderNum", orderNum);
            mapBase.put("orderStartTime", orderStartTime);
            mapBase.put("orderEndTime", orderEndTime);
            mapBase.put("orderSource", 2);
            IPage<ShipmentsByOrderBaseVO> orderBaseIPage = this.baseMapper.queryShipmentsSupplierOrderBase(page, mapBase);
            List<ShipmentsByOrderBaseVO> orderDetails = orderBaseIPage.getRecords();
            if (!orderDetails.isEmpty()) {
                for (ShipmentsByOrderBaseVO vo : orderBaseIPage.getRecords()) {
                    if (Objects.equals(CommonType.dropShipping.DROPSHIPPING_YES.getCode(), vo.getDropShipping())) {
                        //查询 经销商店铺名称
                        String dealerShopName = this.baseMapper.queryDealerShopName(vo.getOriginalOrder());
                        vo.setDealerShopName(dealerShopName);
                    }
                }
            }
            //设置待发货订单的信息
            boolean flag = setShippedSupplierOrderDetails(orderDetails);
            if (flag) {
                return ResponseResult.success(PageUtils.getPage(orderBaseIPage));
            }
            return ResponseResult.fail(BizCodeEnume.ORDER_IS_NULL);
        }
        return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
    }

    /**
     * 设置待发货订单信息
     *
     * @param orderDetails
     * @return
     */
    private boolean setShippedSupplierOrderDetails(List<ShipmentsByOrderBaseVO> orderDetails) {
        for (ShipmentsByOrderBaseVO orderDetail : orderDetails) {
            //满减赠品信息
            String giveawayInfo = orderDetail.getGiveawayInfo();
            List<ManJianZengPinVO> list;
            if (null != giveawayInfo && !giveawayInfo.isEmpty()) {
                list = JsonUtils.string2Obj(giveawayInfo, List.class, ManJianZengPinVO.class);
                orderDetail.setZengpinInfo(list);
            }
            String orderId = orderDetail.getOrderId();
            // 退款状态
            Integer orderRefundStatus = orderDetail.getOrderRefundStatus();
            // 退货状态
            Integer orderReturnStatus = orderDetail.getOrderReturnStatus();
            // 订单状态
            Integer normalStatus = orderDetail.getOrderStatus();
            // 涉及退款退货订单状态
            Integer rStatus = null;
            if (normalStatus == 3) {
                // 未发货状态
                if (orderRefundStatus == 1) {
                    // 未发货退款中
                    rStatus = 22;
                } else if (orderRefundStatus == 2) {
                    // 商家拒绝或审核通过，继续发货，但不可再次申请退款
                    rStatus = 26;
                } else if (orderRefundStatus == 0) {
                    rStatus = 3;
                }
            } else if (normalStatus == 6) {
                // 已完成状态
                if (orderRefundStatus == 1) {
                    // 收货退款中
                    rStatus = 23;
                } else if (orderRefundStatus == 2) {
                    rStatus = 25;
                } else if (orderRefundStatus == 0 && orderReturnStatus == 0) {
                    rStatus = 6;
                } else if (orderReturnStatus == 1) {
                    // 收货退货中
                    rStatus = 24;
                } else if (orderReturnStatus == 2) {
                    rStatus = 25;
                } else {
                    rStatus = 6;
                }
            } else {
                rStatus = normalStatus;
            }
            // 存放最新状态
            orderDetail.setOrderStatus(rStatus);
            List<ShipmentsByOrderGoodsDetailsVO> queryOrderDetails = this.orderGoodsMapper.shipmentsBySupplierOrderGoods(orderId);
            if (queryOrderDetails != null) {
                orderDetail.setShipmentsByOrderGoodsDetails(queryOrderDetails);
            }
        }
        return true;
    }

    @Override
    public ResponseResult queInvoiceOrder(queryInvoiceOrderDTO params) throws IllegalAccessException {
        // 通过token获取用户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(params.getToken());
        Map<String, Object> data = ParseMapUtils.beanToMap(params);
        data.put("userId", ui.getUserId());
        IPage<queryInvoiceOrderVO> page = this.baseMapper.queInvoiceOrder(new Query<queryInvoiceOrderVO>().getPage(data),
                data);
        for (queryInvoiceOrderVO vo : page.getRecords()) {
            List<queryPurchaseGoodsDetailsVO> goodsDetailsList = this.baseMapper.queryGoodsDetailsList(vo.getOrderId());
            for (queryPurchaseGoodsDetailsVO good : goodsDetailsList) {
                good.setOrderStatus(vo.getOrderStatus());
                good.setCommonState(vo.getCommonState());

            }
            vo.setGoodsDetailsList(goodsDetailsList);
        }
        return ResponseResult.success(PageUtils.getPage(page));
    }

    @Override
    public ResponseResult queryPurchaseOrder(queryPurchaseOrderDTO params) throws IllegalAccessException {
        // 通过token获取用户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(params.getToken());
        Map<String, Object> data = ParseMapUtils.beanToMap(params);
        data.put("userId", ui.getUserId());
        IPage<queryPurchaseOrderVO> page = this.baseMapper.queryPurchaseOrder(new Query<queryPurchaseOrderVO>().getPage(data),
                data);
        for (queryPurchaseOrderVO vo : page.getRecords()) {
            OrderReturnEntity orderReturnEntity = orderReturnMapper.selectOne(new LambdaQueryWrapper<OrderReturnEntity>()
                    .eq(OrderReturnEntity::getOrderId, vo.getOrderId()));
            if (null != orderReturnEntity) {
                vo.setReturnState(orderReturnEntity.getReturnState());
                vo.setReturnMethodType(orderReturnEntity.getReturnMethodStatus());
            }
            // 换货状态
            Integer orderBarterStatus = vo.getOrderBarterStatus();
            Integer status = getStatus(Integer.parseInt(vo.getOrderStatus()), vo.getVirtualGoods(), vo.getOrderRefundStatus(), vo.getOrderReturnStatus(), vo.getOrderLastReturnTime(), orderBarterStatus);
            BigDecimal dealerDiscountAmout = new BigDecimal(0);
            List<queryPurchaseGoodsDetailsVO> goodsDetailsList = this.baseMapper.queryGoodsDetailsList(vo.getOrderId());
            for (queryPurchaseGoodsDetailsVO good : goodsDetailsList) {
                good.setOrderStatus(vo.getOrderStatus());
                good.setCommonState(vo.getCommonState());
                // 查询供应商经销折扣
                OrderDealerInfoEntity orderDealerInfoEntity = orderDealerInfoMapper.queryOrderGoodsDealerDiscount(vo.getOrderId(), good.getGoodsId());
                if (null != orderDealerInfoEntity) {
                    dealerDiscountAmout = dealerDiscountAmout.add(orderDealerInfoEntity.getDealerDiscountAmout());
                    good.setTotalTaxInclusiveAmount(orderDealerInfoEntity.getTotalTaxInclusiveAmount().subtract(orderDealerInfoEntity.getDealerDiscountAmout()));
                }
            }
            vo.setOrderStatus(status.toString());
            vo.setDealerDiscountAmout(dealerDiscountAmout);
            vo.setGoodsDetailsList(goodsDetailsList);
        }
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 取消订单。
     * 该方法处理订单的取消逻辑，包括但不限于：退款、更新订单状态、取消佣金订单、更新付款状态、修改商品库存、退还红包等。
     *
     * @param map 包含订单相关数据的映射，如用户ID、订单ID、订单取消身份和原因等。
     * @return 返回处理结果的响应对象。
     */
    @Override
    @Transactional
    public ResponseResult cancelSupplierTheOrder(Map<Object, Object> map) {
        ResponseResult rr = new ResponseResult();
        try {
            // 获取当前操作用户的信息
            // 获取user信息
            UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
            // 默认使用当前用户ID，如果map中提供了userId则使用提供的ID
            String userId = map.get("userId") != null ? (String) map.get("userId") : ui.getUserId();
            // 获取订单ID
            String orderid = map.get("orderId").toString();
            // 获取订单取消身份，1：买家；2：卖家；3：系统
            // 订单取消者身份
            Integer orderCancelIdentity = 1;

            // 处理订单取消逻辑
            List<String> orderIdList = new ArrayList<>();
            orderIdList.add(orderid);
            // 根据订单类型或支付方式，处理关联的订单列表
            OrderBaseEntity orderPayInfo = this.baseMapper.selectById(orderid);
            if (orderPayInfo.getOrderType().intValue() == CommonType.orderType.HOT_GOODS_TYPE.getCode() || orderPayInfo.getPaymentCode() == 2) {
                List<OrderBaseEntity> orderPayInfoList = this.baseMapper.selectList(new LambdaQueryWrapper<OrderBaseEntity>()
                        .eq(OrderBaseEntity::getPaymentNumber, orderPayInfo.getPaymentNumber()));
                orderIdList = orderPayInfoList.stream().map(OrderBaseEntity::getOrderId).collect(Collectors.toList());
            }

            // 遍历订单列表，执行取消操作
            for (String orderId : orderIdList) {
                // 更新订单状态为已取消
                // 订单取消状态
                Integer orderStatus = CommonType.orderStatus.CANCEL.getCode();
                // 获取订单实例
                OrderBaseEntity order = this.baseMapper.selectById(orderId);
                if (order.getPaymentCode() == 2) {
                    orderPaymentVocherMapper.update(null, new LambdaUpdateWrapper<OrderPaymentVocherEntity>()
                            .set(OrderPaymentVocherEntity::getReviewStatus, 4)
                            .set(OrderPaymentVocherEntity::getReviewMessage, "订单已取消")
                            .set(OrderPaymentVocherEntity::getReviewTime, DateUtils.getNowDateToDf())
                            .eq(OrderPaymentVocherEntity::getOrderId, orderId));
                }
                // 更新订单信息
                // 订单取消原因
                String orderCancelReason = (String) map.get("orderCancelReason");
                // 订单取消时间
                Date orderCancelDate = DateUtils.getNowDate();
                boolean isUpdateSuccess = this.baseMapper.update(null, new LambdaUpdateWrapper<OrderBaseEntity>()
                        .eq(OrderBaseEntity::getOrderId, orderId)
                        .set(OrderBaseEntity::getOrderStatus, orderStatus)
                        .set(OrderBaseEntity::getOrderCancelIdentity, orderCancelIdentity)
                        .set(OrderBaseEntity::getOrderCancelReason, orderCancelReason)
                        .set(OrderBaseEntity::getOrderCancelDate, orderCancelDate)) == CommonStatusEnum.SUCCESS.getCode();

                // 更新订单商品状态
                boolean isUpdateGoodsSuccess = orderGoodsMapper.update(null, new LambdaUpdateWrapper<OrderGoodsEntity>()
                        .eq(OrderGoodsEntity::getBuyerUserId, userId)
                        .eq(OrderGoodsEntity::getOrderId, orderId)
                        .set(OrderGoodsEntity::getOrderGoodsStatus, orderStatus)) == CommonStatusEnum.SUCCESS.getCode();
            }
            // 修改库存
            // 查询支付单号
            List<OrderGoodsEntity> orderGoodsEntityList = orderGoodsMapper.selectList(new LambdaQueryWrapper<OrderGoodsEntity>()
                    .eq(OrderGoodsEntity::getOrderId, orderid));
            List<WapGoodsSaleSumDTO> wapGood = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(orderGoodsEntityList)) {
                wapGood = orderGoodsEntityList.stream().map(entity -> {
                    WapGoodsSaleSumDTO catResult = new WapGoodsSaleSumDTO();
                    catResult.setGoodsNum(entity.getOrderGoodsNum());
                    BeanUtils.copyProperties(entity, catResult);
                    return catResult;
                }).collect(Collectors.toList());
            }
            supplierClient.updateGoodsStock(wapGood);
        } catch (Exception e) {
            log.error(e.getMessage());
            // 手动回滚事务
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseResult.fail(e.getMessage());
        }
        return ResponseResult.success();
    }

    /**
     * 查询退货行列表页面信息。
     *
     * @param map 包含查询条件的映射表，如页码和订单ID。
     * @return 返回查询结果的响应对象，包含退货行列表和是否改价的信息。
     */
    @Override
    public ResponseResult queryReturnGoodsPage(Map<String, Object> map) {
        // 从映射表中获取订单ID字符串
        String orderId = String.valueOf(map.get("orderId"));
        // 创建分页对象，并从映射表中获取页码和每页数量
        Page<GoodsDetailsListVO> page = new Page<>((Integer) map.get("page"), (Integer) map.get("limit"));
        // 根据订单ID和分页信息查询退货行列表
        IPage<GoodsDetailsListVO> orderGoodsPage = orderGoodsMapper.queryGoodsListByReturn(page, orderId);
        // 创建一个映射表用于存放查询结果
        Map<String, Object> returnMap = new HashMap<>();
        // 查询该订单是否有过改价操作
        // 查询当前订单是否改价 如果 改价必须全单退
        Integer orderNum = this.baseMapper.queryOrderUpdatePrice(map);
        // 根据查询结果，设置订单是否改价的信息
        if (orderNum == 0) {
            returnMap.put("updatePrice", false);
        } else {
            returnMap.put("updatePrice", true);
        }
        // 查询订单基础信息，以获取赠品信息
        OrderBaseEntity baseEntity = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderBaseEntity>()
                .eq(OrderBaseEntity::getOrderId, orderId));
        // 获取赠品信息字符串
        String giveawayInfo = baseEntity.getGiveawayInfo();
        // 解析赠品信息字符串为对象列表
        List<FullReductionGiftsVO> list = new ArrayList<>();
        if (null != giveawayInfo && !giveawayInfo.isEmpty()) {
            list = JsonUtils.string2Obj(giveawayInfo, List.class, FullReductionGiftsVO.class);
        }
        for(GoodsDetailsListVO vo : orderGoodsPage.getRecords()){
            MerchantGoodsBaseDTO goodsMap = goodServiceClient.queryByGoodsId(vo.getGoodsId());
            vo.setGoodsIsShelves(goodsMap.getGoodsIsShelves());
        }
        // 将赠品信息和退货行列表信息添加到结果映射表中
        returnMap.put("giveawayInfo", list);
        returnMap.put("data", PageUtils.getPage(orderGoodsPage));
        // 返回处理后的查询结果
        return ResponseResult.success(returnMap);
    }

    /**
     * 商家查询子单
     *
     * @param map
     * @return
     */
    @Override
    public ResponseResult querySonOrders(Map<String, Object> map) {
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        PageInfo pi = new PageInfo();
        //条件参数
        IPage<OrderDetailsVO> page = this.baseMapper.querySonSupplierOrders(new Query<OrderDetailsVO>().getPage(map), map);
        // 设置订单信息
        ResponseResult setResult = setSupplierOrderDetails(page.getRecords());
        if (CommonStatusEnum.SUCCESS.getCode() != setResult.getCode()) {
            return setResult;
        }

        List<String> gradeNameList = new ArrayList<>();
        List<Integer> hyZheKouList = new ArrayList<>();
        for (OrderDetailsVO orderDetail : page.getRecords()) {
            //满减赠品信息
            String giveawayInfo = orderDetail.getGiveawayInfo();
            List<ManJianZengPinVO> list;
            if (null != giveawayInfo && !"".equals(giveawayInfo)) {
                list = JsonUtils.string2Obj(giveawayInfo, List.class, ManJianZengPinVO.class);
                orderDetail.setZengpinInfo(list);
            }
        }

        pi.setPage(PageUtils.getPage(page));
        pi.setGradeNameList(gradeNameList);
        pi.setHyZheKouList(hyZheKouList);
        return ResponseResult.success(pi);
    }

    /**
     * 更新订单的团购状态
     * 此方法接收一个订单ID列表，并将这些订单的团购状态进行更新
     * 主要用于处理团购活动中的订单状态更新，以确保订单信息的准确性
     *
     * @param orderIdlist 订单ID列表，用于指定需要更新团购状态的订单
     */
    @Override
    public void updateOrderGroupbuyStatus(List<String> orderIdlist) {
        this.baseMapper.updateOrderGroupbuyStatus(orderIdlist);
    }

    /**
     * 查询指定店铺的订单数量
     * 通过店铺ID来查询该店铺的订单数量，以便了解店铺的销售情况
     *
     * @param shopId 店铺ID，用于指定查询的店铺
     * @return 店铺的订单数量
     */
    @Override
    public Integer selectShopOrder(Integer shopId) {
        Integer result=this.baseMapper.selectShopOrder(shopId);
        return  result;
    }

    /**
     * 获取未处理的订单数量
     * 根据传入的参数映射，查询符合一定条件的未处理订单数量
     * 主要用于监控和处理订单系统中的未处理订单
     *
     * @param param 包含查询条件的映射表
     * @return 未处理的订单数量
     */
    @Override
    public Integer getUndealedOrderCount(Map<String, Object> param) {
        Integer result=this.baseMapper.getUndealedOrderCount(param);
        return  result;
    }

    /**
     * 查询指定日期范围内的支付金额
     * 根据开始时间和结束时间，查询指定店铺在该时间段内的支付金额
     * 用于财务报表或销售分析，以帮助商家了解特定时期的销售业绩
     *
     * @param startTimeStr 开始时间字符串，指定查询的起始时间
     * @param endTimeStr 结束时间字符串，指定查询的结束时间
     * @param shopId 店铺ID，用于指定查询的店铺
     * @return 指定日期范围内的支付金额
     */
    @Override
    public BigDecimal queryPayMentByDate(String startTimeStr, String endTimeStr, String shopId) {
        BigDecimal result=this.baseMapper.queryPayMentByDate(startTimeStr,endTimeStr,shopId);
        return  result;
    }

    /**
     * 查询指定日期范围内的支付订单数量
     * 类似于查询支付金额，但专注于订单数量，以提供关于销售量的信息
     *
     * @param startTimeStr 开始时间字符串，指定查询的起始时间
     * @param endTimeStr 结束时间字符串，指定查询的结束时间
     * @param shopId 店铺ID，用于指定查询的店铺
     * @return 指定日期范围内的支付订单数量
     */
    @Override
    public Integer queryPayCountByDate(String startTimeStr, String endTimeStr, String shopId) {
        Integer result=this.baseMapper.queryPayCountByDate(startTimeStr,endTimeStr,shopId);
        return  result;
    }

    /**
     * 查询店铺的基础销售信息
     * 通过店铺ID和日期参数，查询该店铺在指定日期的基础销售信息
     * 主要用于商家通过APP查看店铺每日销售概况，包括支付金额等信息
     *
     * @param shopId 店铺ID，用于指定查询的店铺
     * @param today 日期字符串，指定查询的日期
     * @return 店铺的基础销售信息列表
     */
    @Override
    public List<AppShopBaseSalesInfoVO> queryShopBasePayPriceByApp(String shopId, String today) {
        List<AppShopBaseSalesInfoVO> result=this.baseMapper.queryShopBasePayPriceByApp(shopId,today);
        return  result;
    }

    /**
     * 查询订单信息
     * 通过订单ID查询与订单相关的所有信息
     *
     * @param orderId 订单ID，用于查询订单信息
     * @return 包含订单信息的Map对象
     */
    @Override
    public Map<String, Object> queryOrderInfo(String orderId) {
        Map<String, Object> result=this.baseMapper.queryOrderInfoFeign(orderId);
        return result;
    }

    /**
     * 查询购买用户ID
     * 通过订单ID查询购买该订单的用户ID
     *
     * @param orderId 订单ID，用于查询购买用户ID
     * @return 包含用户ID的Map对象
     */
    @Override
    public Map<String, Object> queryBuyUserId(String orderId) {
        Map<String, Object> result=this.baseMapper.queryBuyUserId(orderId);
        return result;
    }

    /**
     * 更新退款账户期间
     * 更新与退款相关的账户信息，主要用于更新退款状态或期间
     *
     * @param param 包含退款账户参数的DTO对象
     * @return 受影响的行数，用于确认更新操作是否成功
     */
    @Override
    public int updateRefundAccountPeriod(RefundAccountParamDTO param) {
        int row = this.baseMapper.updateRefundAccountPeriod(param);
        return row;
    }

    /**
     * 编辑账户状态
     * 通过会计期间ID更新账户状态，用于管理账户的活动状态
     *
     * @param accountingPeriodId 会计期间ID，用于指定要编辑的账户
     * @param accountStatus 账户状态，用于更新账户的当前状态
     */
    @Override
    public void editAccountStatus(String accountingPeriodId, Integer accountStatus) {
        this.baseMapper.editAccountStatus(accountingPeriodId,accountStatus);
    }

    /**
     * 查询订单信息（Feign调用版）
     * 通过订单ID查询订单信息，专为Feign客户端调用设计
     *
     * @param orderId 订单ID，用于查询订单信息
     * @return 订单基本信息的VO对象
     */
    @Override
    public OrderBaseVO queryOrderInfoFegin(String orderId) {
        OrderBaseVO result=this.baseMapper.queryOrderInfoFegin(orderId);
        return result;
    }

    /**
     * 查询会计期间ID
     * 通过订单ID查询对应的会计期间ID，用于关联财务信息
     *
     * @param orderId 订单ID，用于查询会计期间ID
     * @return 对应的会计期间ID字符串
     */
    @Override
    public String queryAccountingPeriodId(String orderId) {
        String result=this.baseMapper.queryAccountingPeriodId(orderId);
        return result;
    }

    /**
     * 根据会计期间ID选择账单详情
     * @param accountingPeriodId 会计期间ID
     * @return 账单详情对象
     */
    @Override
    public AccountShopBillDetailsVO selectDetailsByPeriodId(String accountingPeriodId) {
        AccountShopBillDetailsVO result=this.baseMapper.selectDetailsByPeriodId(accountingPeriodId);
        return result;
    }

    /**
     * 根据订单ID查询珍宝阁店铺ID
     * @param orderId 订单ID
     * @return 店铺ID
     */
    @Override
    public String queryTreasureShopIdByInt(String orderId) {
        String shopid= this.baseMapper.queryTreasureShopIdByInt(orderId);
        return  shopid;
    }

    /**
     * 查询订单ID对应的信息
     * @param orderId 订单ID
     * @return 订单信息字符串
     */
    @Override
    public String queryOrderId(String orderId) {
        String result=this.baseMapper.queryOrderId(orderId);
        return  result;
    }

    /**
     * 根据订单ID查询订单信息和支付编号
     * @param orderId 订单ID
     * @return 包含订单信息和支付编号的列表
     */
    @Override
    public List<Map<String, Object>> queryOrderIdAndPaymentNumber(String orderId) {
        List<Map<String, Object>> list=this.baseMapper.queryOrderIdAndPaymentNumber(orderId);
        return list;
    }

    /**
     * 插入易牙通订单信息
     * @param submitOrderListVO 提交订单列表视图对象
     * @return 插入结果
     */
    @Override
    public Integer insertYiYaTongOrder(SubmitOrderVO submitOrderListVO) {
        Integer result=this.baseMapper.insertYiYaTongOrder(submitOrderListVO);
        return  result;
    }

    /**
     * 查询资产信息
     * @param orderId 订单ID
     * @param paymentNumber 支付编号
     * @return 资产临时信息DTO列表
     */
    @Override
    public List<AssetTmpInfoDTO> queryAssetInfo(String orderId, String paymentNumber) {
        List<AssetTmpInfoDTO> list=this.baseMapper.queryAssetInfo(orderId,paymentNumber);
        return  list;
    }

    /**
     * 根据订单基础信息查询买家用户ID
     * @param orderId 订单ID
     * @return 买家用户ID
     */
    @Override
    public String queryBuyerUserIdByOrderBase(String orderId) {
        String  result=this.baseMapper.queryBuyerUserIdByOrderBase(orderId);
        return  result;
    }

    /**
     * 根据订单ID查询商品销售汇总信息
     * @param orderId 订单ID
     * @return 商品销售汇总DTO列表
     */
    @Override
    public List<WapGoodsSaleSumDTO> queryGoodsByOrderId(String orderId) {
        List<WapGoodsSaleSumDTO> list=this.baseMapper.queryGoodsByOrderIdFeign(orderId);
        return  list;
    }

    /**
     * 查询订单编号是否已存在
     *
     * @param orderId 订单编号
     * @return 返回存在次数，如果大于0则表示存在
     */
    @Override
    public Integer queryOrderNumExist(String orderId) {
        return this.baseMapper.queryOrderNumExist(orderId);
    }
    /**
     * 将货币符号和汇率存入订单表
     * @param orderId
     * @param currencyCode
     * @param exchangeRate
     * @return
     */
    @Override
    public ResponseResult updateCurrencyAndExchangeRate(String orderId, String currencyCode, BigDecimal exchangeRate,String currencySymbol) {
        baseMapper.updateCurrencyAndExchangeRate(orderId,currencyCode,exchangeRate,currencySymbol);
        return ResponseResult.success();
    }
    /**
     * 根据订单查询货币信息
     * @param orderId
     * @return
     */
    @Override
    public BaseCurrencySymbolVO queryOrderCurrencyInfo(String orderId) {
        return this.baseMapper.queryOrderCurrencyInfo(orderId);
    }

    @Override
    public boolean checkGroupBookingStatus(ExpireMarkIdUpdateDTO para) {
        boolean flag = false;
        List<String> orderIdList = para.getOrderIdList();
        Integer status = para.getStatus();
        try {
            if (status == 1) {
                // 拼团成功，改变订单状态
                boolean isUpdateBaseStatus = baseMapper.updateGroupBookingBaseStatus(orderIdList);
                log.info("-----isUpdateBaseStatus-----" + isUpdateBaseStatus);
                flag = true;
            } else {
                // 拼团失败：走新 Mall 收银台逐笔退款（order_id 语义）+ 按订单状态幂等。
                // 仅对「已付款且未取消」的订单退款：退款成功后订单置 order_status=7（取消），
                // 重复触发时这些订单已不在已付款态，天然跳过，避免重复退款。
                if (orderIdList != null && !orderIdList.isEmpty()) {
                    for (String orderId : orderIdList) {
                        OrderBaseEntity order = baseMapper.selectMallPaymentOrder(orderId);
                        if (order == null || order.getOrderId() == null) {
                            continue;
                        }
                        if (!Integer.valueOf(CommonType.orderStatus.PAID.getCode()).equals(order.getOrderStatus())) {
                            // 非已付款态（未支付或已取消/退款）跳过，幂等保护
                            continue;
                        }
                        String cashierNo = order.getPaymentNumber();
                        BigDecimal amount = order.getOrderPaymentAmount();
                        if (cashierNo == null || cashierNo.isBlank()
                                || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                            log.warn("拼团失败退款缺收银台单号或金额 orderId={}", orderId);
                            continue;
                        }
                        MallRefundRequest refundRequest = new MallRefundRequest();
                        refundRequest.setRefundNo("PT_RT_" + order.getOrderId());
                        refundRequest.setCashierNo(cashierNo);
                        refundRequest.setMallOrderNo(order.getOrderId());
                        refundRequest.setAmount(amount);
                        refundRequest.setReason("拼团失败自动退款");
                        ResponseResult refundResp = payMentServiceClient.refund(refundRequest);
                        if (refundResp == null || refundResp.getCode() != 1) {
                            log.warn("拼团失败退款失败 orderId={} resp={}", order.getOrderId(), refundResp);
                            continue;
                        }
                        String cancelTime = DateUtils.getNowDateToString();
                        boolean isUpdateBaseStatus = baseMapper.updateGroupBookingBaseOrderStatus(order.getOrderId(), cancelTime);
                        log.info("-----拼团失败退款后取消订单 orderId={} isUpdateBaseStatus={}", order.getOrderId(), isUpdateBaseStatus);
                        boolean isUpdateGoodsStatus = baseMapper.updateGroupBookingGoodsOrderStatus(order.getOrderId());
                        log.info("-----拼团失败退款后关单商品 orderId={} isUpdateGoodsStatus={}", order.getOrderId(), isUpdateGoodsStatus);
                        // 回补已实扣的可售库存（commit→return 状态机，幂等：仅处理 COMMITTED 预约）
                        try {
                            inventoryLifecycleService.returnToStock(order.getOrderId(), true);
                        } catch (Exception ex) {
                            log.warn("拼团失败退款回补库存异常 orderId={} err={}", order.getOrderId(), ex.getMessage());
                        }
                        flag = true;
                    }
                }
            }
        } catch (Exception e) {
            log.info("checkGroupBookingStatusErrorResults:" + e.getMessage());
        }
        return flag;
    }

    /**
     * 保存京东订单号
     * @param orderId
     * @param jingDongOrderId
     * @return
     */
    @Override
    public ResponseResult updateOrderJingDongOrderId(String orderId, String jingDongOrderId) {
        orderGoodsMapper.updateOrderJingDongOrderId(orderId,jingDongOrderId);
        this.baseMapper.updateOrderJingDongOrderId(orderId,jingDongOrderId);
        return ResponseResult.success();
    }

    /**
     * 根据京东订单号 修改我方订单状态为 已发货
     * @param jdOrderIds
     * @return
     */
    @Override
    public ResponseResult updateOrderStatusByJDOrderId(List<String> jdOrderIds) {
        for (String jdOrderId : jdOrderIds) {
            // 调用京东接口查询物流轨迹
            Map<String, Object> stringObjectMap = goodServiceClient.queryJingDongLogistics(jdOrderId);
            if(null == stringObjectMap){
                return ResponseResult.fail("京东物流查询异常");
            }
            List<OrderGoodsEntity> orderGoodsEntities = orderGoodsMapper.selectList(null,new LambdaQueryWrapper<OrderGoodsEntity>()
                    .eq(OrderGoodsEntity::getJdOrderId,jdOrderId));
            String orderId = orderGoodsEntities.get(0).getOrderId();
            // 查询订单信息 如状态，退货退款状态，收货人手机号
            OrderBaseEntity qos = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderBaseEntity>()
                    .eq(OrderBaseEntity::getOrderId, orderId));
            // 已发货
            Integer orderStatus = 4;
            // 商品订单是否已经发货。1-是，0-否，默认0(orderGoods)
            Integer orderGoodsIdDeliver = 1;
            String orderSellerName = "京东";
            // 配送公司ID
            Integer orderShippingExpressId = 0;
            // 配送信息
            String orderShippingMessage = "";
            //发货人地址
            String orderSellerAddress = "";
            // 物流单号
            String orderShippingCode = stringObjectMap.get("deliveryOrderId").toString();
            // 物流公司
            String deliveryCarrier = stringObjectMap.get("deliveryCarrier").toString();
            // 配送时间
            String orderShippingTime = DateUtils.getTime();
            //配送人
            String deliveryPerson = null;
            //配送联系方式
            String deliveryTelephone = null;
            //配送凭证
            String deliveryCertificate = null;
            Integer storageId = 0;
            // 1 物流配送 2 自主配送
            Integer shipmentType = CommonType.shipmentType.LOGISTICS_DELIVERY.getCode();
            List<Integer> goodsIds = new ArrayList<>();
            List<String> goodsNums = new ArrayList<>();
             List<Integer> goodsIdList = orderGoodsEntities.stream()
             .map(OrderGoodsEntity::getGoodsId)
             .collect(Collectors.toList());
            Integer goodsNumCount = this.baseMapper.selectOrderFahuoGoodsNum(orderGoodsEntities.get(0).getOrderId(), goodsIdList);
            if (null == goodsNumCount) {
                goodsNumCount = 0;
            }
            Map<String, Object> mapGoods = new HashMap<>();
            mapGoods.put("orderId",orderGoodsEntities.get(0).getOrderId());
            mapGoods.put("orderStatus", orderStatus);
            mapGoods.put("orderGoodsIdDeliver", orderGoodsIdDeliver);
            mapGoods.put("orderSellerName", orderSellerName);
            mapGoods.put("orderShippingTime", orderShippingTime);
            mapGoods.put("orderShippingCode", orderShippingCode);
            mapGoods.put("orderShippingExpressId", orderShippingExpressId);
            for (OrderGoodsEntity orderGoodsEntity : orderGoodsEntities) {
                mapGoods.put("shipmentQuantity", orderGoodsEntity.getOrderGoodsNum());
                mapGoods.put("shipmentStatus", 2);
                mapGoods.put("orderGoodsId", orderGoodsEntity.getOrderGoodsId());
                mapGoods.put("goodsShippingCode", orderShippingCode);
                goodsIds.add(orderGoodsEntity.getGoodsId());
                goodsNums.add(orderGoodsEntity.getOrderGoodsNum());
                goodsNumCount += Integer.valueOf(orderGoodsEntity.getOrderGoodsNum());
                orderGoodsMapper.updateByOrderGoods(mapGoods);
            }
            //查询当前订单总的商品数量
            Integer orderGoodsNum = this.baseMapper.selectOrderGoodsNum(orderId);
            if (!goodsNumCount.equals(orderGoodsNum)) {
                //部分发货
                mapGoods.put("orderStatus", 16);
            }
            String orderGoodsIds = JSON.toJSONString(goodsIds);
            String orderGoodsNums = JSON.toJSONString(goodsNums);
            OrderShipmentEntity shipmentEntity = new OrderShipmentEntity(orderId, orderGoodsIds, orderGoodsNums, DateUtils.getNowDateToDf(),
                    orderShippingExpressId, orderShippingCode, orderShippingMessage,
                    shipmentType, deliveryPerson, deliveryTelephone, deliveryCertificate, storageId, orderSellerAddress);
           orderShipmentMapper.insert(shipmentEntity);
            //后台配置的自动确认收货时间
            String configValue = baseServiceClient.selectConfigValueFeign(CommonType.configKey.AUTOMATIC_DELIVERY_TIME.getDesc());
            Integer automaticDeliveryTime = Integer.parseInt(configValue);
            mapGoods.put("automaticDeliveryTime", automaticDeliveryTime);
            this.baseMapper.updateByOrderBase(mapGoods);
            payMentServiceClient.changeConsumeRecord(orderId);
            try {
                //获取店铺名称
                String shopName = qos.getShopName();
                //设置快递名称
                Map<String, Object> paraMap = new HashMap<>();
                List<String> messageList = new ArrayList<>();
                messageList.add(shopName);
                messageList.add(orderShippingCode);
                messageList.add(deliveryCarrier);
                paraMap.put("list", messageList);
                paraMap.put("mobile", qos.getOrderReceiverContact());
                paraMap.put("orderId", qos.getOrderId());
                paraMap.put("userId", qos.getBuyerUserId());
                paraMap.put("shopName", shopName);
                paraMap.put("trackingNumber", orderShippingCode);
                paraMap.put("expressCompany", deliveryCarrier);
                paraMap.put("goodsName", orderGoodsEntities.get(0).getGoodsName());
                paraMap.put("goodsNum", goodsNumCount);
                paraMap.put("templateId", CommonType.messageTemplate.CODE_O.getCode());
                baseServiceClient.sendMessageTemplate(paraMap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ResponseResult.success();
    }

    /**
     * 将京东子订单号保存入库中
     * @param orderGoodsJdVopDTOS
     * @return
     */
    @Override
    public ResponseResult updateJongDongSonOrder(List<OrderGoodsJdVopDTO> orderGoodsJdVopDTOS) {
        for (OrderGoodsJdVopDTO orderGoodsJdVopDTO : orderGoodsJdVopDTOS) {
            orderGoodsMapper.updateJongDongSonOrder(orderGoodsJdVopDTO);
        }
        return ResponseResult.success();
    }

    /**
     * 京东已售订单
     * @param map
     * @return
     */
    @Override
    public ResponseResult jingDongSoldOrders(Map<String, Object> map) {
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        Integer shopId = Integer.valueOf(ui.getShopId());
        //条件参数
        map.put("shopId", shopId);
        IPage<YiYaTongOrderVO> page = this.baseMapper.queryJingdDngOrderByPage(new Query<Map<String, Object>>().getPage(map),
                map);


        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 京东已售订单 导出
     * @param map
     * @return
     */
    @Override
    public ResponseResult jingDongSoldOrdersExp(Map<String, Object> map) {
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        Integer shopId = Integer.valueOf(ui.getShopId());
        //条件参数
        map.put("shopId", shopId);
        List<YiYaTongOrderVO> orderList = this.baseMapper.queryJingDongOrderByPageExel(map);

        String[] title = {"订单编号", "订单状态", "买家名称", "收件人姓名", "手机", "收货地址", "运费", "实付款", "商品名称",
                "商品协议价", "商品销售价", "数量", "优惠", "下单时间", "付款时间", "完成时间", "售后类型", "售后时间", "售后完成时间"};
        List<Map<String, String>> list = Lists.newArrayList();
        for (YiYaTongOrderVO soldOrderExportDTO : orderList) {
            Integer orderStatus = soldOrderExportDTO.getOrderStatus();
            String statusName = null;
            if (orderStatus == 1) {
                statusName = "待付款";
            } else if (orderStatus == 2) {
                statusName = "待配货";
            } else if (orderStatus == 3) {
                statusName = "待发货";
            } else if (orderStatus == 4) {
                statusName = "已发货";
            } else if (orderStatus == 5) {
                statusName = "已签收";
            } else if (orderStatus == 6) {
                statusName = "已完成";
            } else if (orderStatus == 7) {
                statusName = "已取消";
            } else if (orderStatus == 8) {
                statusName = "退款中";
            } else if (orderStatus == 9) {
                statusName = "退款完成";
            }
            String returnName = null;
            if (orderStatus == 1) {
                returnName = "退款";

            } else if (orderStatus == 2) {
                returnName = "仅退款";
            } else if (orderStatus == 3) {
                returnName = "退货退款";
            }
            HashMap<String, String> mPut = Maps.newHashMap();
            mPut.put("订单编号", soldOrderExportDTO.getOrderId());
            mPut.put("订单状态", statusName);
            mPut.put("买家名称", soldOrderExportDTO.getBuyerUserName());
            mPut.put("收件人姓名", soldOrderExportDTO.getOrderReceiverName());
            mPut.put("手机", soldOrderExportDTO.getOrderReceiverContact());
            mPut.put("收货地址", soldOrderExportDTO.getOrderReceiverAddress());
            mPut.put("运费", soldOrderExportDTO.getShippingFee().toString());
            mPut.put("实付款", soldOrderExportDTO.getOrderGoodsPaymentAmount().toString());
            mPut.put("商品名称", soldOrderExportDTO.getGoodsName());
            mPut.put("商品协议价", soldOrderExportDTO.getGoodsPrice().toString());
            mPut.put("商品销售价", soldOrderExportDTO.getMsrp().toString());
            mPut.put("数量", soldOrderExportDTO.getOrderGoodsNum().toString());
            mPut.put("优惠", soldOrderExportDTO.getOrderGoodsDiscountFee() == null ? "0" : soldOrderExportDTO.getOrderGoodsDiscountFee().toString());
            mPut.put("下单时间", soldOrderExportDTO.getOrderCreateTime());
            mPut.put("付款时间", soldOrderExportDTO.getPaymentTime());
            mPut.put("完成时间", soldOrderExportDTO.getOrderFinishedTime());
            mPut.put("售后类型", returnName);
            mPut.put("售后时间", soldOrderExportDTO.getReturnCreateTime());
            mPut.put("售后完成时间", soldOrderExportDTO.getReturnFinishTime());
            list.add(mPut);
        }
        Map<String, List<Map<String, String>>> m = Maps.newHashMap();
        m.put("京东订单明细数据", list);
        String url = ExcelsUtil.createExcel(title, m, new int[]{0});
        return ResponseResult.success(url);
    }

    /**
     * 判断京东余额支付足够支付
     * @param orderId
     * @param orderAmount
     * @return
     */
    @Override
    public ResponseResult queryJDBalanceIsEnough(String orderId, BigDecimal orderAmount) {
        OrderBaseEntity qos = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderBaseEntity>()
                .eq(OrderBaseEntity::getOrderId, orderId)
                .or().
                eq(OrderBaseEntity::getPaymentNumber,orderId)
                .last("limit 1"));
        // 判断订单是否是京东订单
        if(qos.getOrderSource() == CommonType.SupplierSource.JING_DONG.getCode()){
            //枚举值 1：货到付款 4：预存款 5：公司转账 101：京东金采 102：商城金采(一般不适用，仅限确认开通商城账期的特殊情况使用，请与业务确认后使用) 20为混合支付
            if(PAYMENT_TYPE == 4 ){
                ResponseResult responseResult = goodServiceClient.queryJongDongBalance();
                if(responseResult.getCode() ==  BizCodeEnume.REGISTRATION_SUCCESS.getCode() && responseResult.getData() != null ){
                    JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString( responseResult.getData()));
                    BigDecimal jdAmount = jsonObject.getJSONObject("balanceInfo").getBigDecimal("remainLimit");
                    if(jdAmount.compareTo(orderAmount) >= 0){
                        return ResponseResult.success(true);
                    }else{
                        return ResponseResult.success(false);
                    }
                }else{
                    return ResponseResult.success(false);
                }
            }
        }
        return ResponseResult.success(true);
    }
}
