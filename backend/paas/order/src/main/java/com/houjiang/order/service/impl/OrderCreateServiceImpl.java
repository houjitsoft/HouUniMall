package com.houjiang.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.goods.ActivityGoodsDetailDTO;
import com.houjiang.commons.dto.goods.ManjianzengzheGoodsRuleQueryDTO;
import com.houjiang.commons.dto.job.ExpireMarkIdUpdateDTO;
import com.houjiang.commons.dto.oos.OssUploadUpdateParamDTO;
import com.houjiang.commons.dto.order.ConsumeRecordByBusinessDTO;
import com.houjiang.commons.dto.order.ConsumeRecordParaDTO;
import com.houjiang.commons.dto.order.ConsumeTradeParaDTO;
import com.houjiang.commons.dto.shop.ShopUserDTO;
import com.houjiang.commons.dto.yiyatong.FreightCalculateDTO;
import com.houjiang.commons.dto.yiyatong.FreightSpuInfoDTO;
import com.houjiang.commons.dto.yiyatong.StockItemDTO;
import com.houjiang.commons.dto.yiyatong.StockQueryDTO;
import com.houjiang.commons.errorcode.domain.GoodsErrorCode;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.exception.DefinedException;
import com.houjiang.commons.rocketmq.MqSendUtils;
import com.houjiang.commons.util.*;
import com.houjiang.commons.util.json.JsonUtils;
import com.houjiang.commons.util.secverifyapiutil.AesEncryptUtil;
import com.houjiang.commons.util.secverifyapiutil.Base64Utils;
import com.houjiang.commons.vo.activity.*;
import com.houjiang.commons.vo.base.PointsCashRatioVo;
import com.houjiang.commons.vo.goods.GdbMerchantGoodsCommonVO;
import com.houjiang.commons.vo.goods.WapGoodsSaleSumUpdateVO;
import com.houjiang.commons.vo.order.*;
import com.houjiang.commons.vo.shop.GdsShopDetailResultVO;
import com.houjiang.commons.vo.shop.ShippingListVO;
import com.houjiang.commons.vo.shop.ShopParaVO;
import com.houjiang.commons.vo.shop.ShopUserMessageResultVO;
import com.houjiang.commons.vo.third.ThirdShopVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.commons.vo.yiyatong.base.SkuStockVO;
import com.houjiang.order.dto.*;
import com.houjiang.order.entity.OrderBaseEntity;
import com.houjiang.order.entity.OrderGoodsEntity;
import com.houjiang.order.feign.*;
import com.houjiang.order.plugin.LegacyActivityPromotionCalcExtension;
import com.houjiang.plugin.api.capability.coupon.CouponQueryCapability;
import com.houjiang.plugin.api.capability.coupon.CouponQueryCapability.CouponInfo;
import com.houjiang.plugin.runtime.PromotionCalcGateway;
import com.houjiang.order.plugin.FreightCalcGateway;
import com.houjiang.order.plugin.OrderCreateAfterGateway;
import com.houjiang.order.plugin.OrderCreateBeforeGateway;
import com.houjiang.order.mapper.*;
import com.houjiang.order.service.FXService;
import com.houjiang.order.service.OrderCreateService;
import com.houjiang.order.vo.OrderDealerGoodsVO;
import com.houjiang.order.vo.OrderInfoVO;
import io.seata.core.context.RootContext;
import io.seata.core.exception.TransactionException;
import io.seata.tm.api.GlobalTransactionContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;

/**
 * @ClassName OrderCreateServiceImpl
 * @Description 创建订单 计算价格
 * @Author Amos
 * @Date 2023-05-26 13:28:35
 * @Version 1.0
 * @author Amos
 */
@Slf4j
@Service
public class OrderCreateServiceImpl implements OrderCreateService {

    private static final Logger LOG = LoggerFactory.getLogger(OrderCreateService.class);
    @Autowired
    private GoodServiceClient goodServiceClient;
    /** 阶梯价算价助手：批量下单按数量区间改单价，未命中保持原价。 */
    @Autowired
    private com.houjiang.order.service.TierPricePricingHelper tierPricePricingHelper;
    /** 砍价成交价助手：本人已砍到底价的商品按成交价成交，未命中保持原价。 */
    @Autowired
    private com.houjiang.order.service.BargainPricePricingHelper bargainPricePricingHelper;
    @Autowired
    private MerchantServiceClient merchantServiceClient;
    @Autowired
    private UserServiceClient userServiceClient;
    @Autowired
    private ActivityServiceClient activityServiceClient;
    @Autowired
    private com.houjiang.order.service.ReductionPolicyQueryService reductionPolicyQueryService;
    /**
     * 促销算价门面：下单算价改为面向 {@code PromotionCalcExtension} 扩展点，
     * 不再直接依赖 activity 服务。当前由 {@link LegacyActivityPromotionCalcExtension}
     * 接管调用，行为与原来一致；活动逐个插件化后由插件实现替换。
     */
    @Autowired
    private PromotionCalcGateway promotionCalcGateway;
    /**
     * 券查询能力：券状态校验不是算价，故单独成契约（见
     * {@link com.houjiang.plugin.api.capability.coupon.CouponQueryCapability}）。
     * 当前由 {@link LegacyActivityCouponQueryCapability} 接管，行为与直接调 activity 一致。
     */
    @Autowired
    private CouponQueryCapability couponQueryCapability;
    /**
     * 二开运费网关：官方基础运费算完后回调 {@code FreightCalcExtension}，回读并写回最终运费。
     */
    @Autowired
    private FreightCalcGateway freightCalcGateway;
    /**
     * 二开下单前网关（before*）：订单入参组装后、写库前回调 {@code OrderCreateBeforeExtension}，可中断。
     */
    @Autowired
    private OrderCreateBeforeGateway orderCreateBeforeGateway;
    /**
     * 二开下单后网关（after*）：订单落库后回调 {@code OrderCreateAfterExtension}，可补充结果 / 同步外部系统。
     */
    @Autowired
    private OrderCreateAfterGateway orderCreateAfterGateway;
    @Autowired
    private BaseServiceClient baseServiceClient;
    @Autowired
    private OrderInvoiceInfoMapper orderInvoiceInfoMapper;
    @Autowired
    private OrderGoodsMapper orderGoodsMapper;
    @Autowired
    private OrderBaseMapper orderBaseMapper;
    @Autowired
    private FXService fxService;
    @Autowired
    private PayMentServiceClient payMentServiceClient;
    @Autowired
    private OrderUserCartMapper orderUserCartMapper;
    @Autowired
    private OrderCommissionMapper orderCommissionMapper;
    @Autowired
    private ThirdPartyServiceClient thirdPartyServiceClient;
    @Autowired
    private OrderSupplierCreateServerImpl supplierCreateServer;

    /**
     * 商城分销内部事件客户端（下单 → 商城登记分销订单，用于「预估收益 / 推广订单」展示）。
     * 与确认收货处的完成通知是同一个客户端的两条链路，失败仅记日志，绝不影响下单主流程。
     */
    @Resource
    private MallDistributionClient mallDistributionClient;

    /** 内部任务令牌，与商城侧 houcloud.plugin.internal-job-token 保持一致 */
    @Value("${houcloud.plugin.internal-job-token:}")
    private String internalJobToken;

    @Override
    public ResponseResult calculatePrice(String message) {
        LOG.info("message{}", message);
        CommonParameterVO cp = JSON.parseObject(message, new TypeReference<>() {
        });
        ResponseResult rr = merchantServiceClient.queryShipping(message);
        if (rr.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return rr;
        }
        if (rr.getData() == null) {
            return ResponseResult.fail(BizCodeEnume.GOODS_NOT_EXIST);
        }
        // 用 JSON 序列化再解析，避免 Feign/Map toString 非 JSON 导致解析异常
        List<WapShopShippingQueryResultVO> wapList = JSON.parseObject(JSON.toJSONString(rr.getData()),
                new TypeReference<List<WapShopShippingQueryResultVO>>() {
                });
        if (CollUtil.isEmpty(wapList)) {
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), "运费商品信息为空，请刷新后重试");
        }
        // 总优惠券金额
        BigDecimal couponTotalPrice = new BigDecimal(0);
        // 总价(实付金额+运费-扣除红包消费券之后的价格)
        BigDecimal totalPrice = new BigDecimal(0);
        // 所有店铺价格(不含运费与优惠价格)
        BigDecimal allShopTotalPrice = new BigDecimal(0);
        // 跨店满减总商品价格
        BigDecimal allowancePrice = BigDecimal.ZERO;
        // 总运费
        BigDecimal totalShipping = new BigDecimal(0);
        // 商品总价
        BigDecimal goodsTotalPrice = new BigDecimal(0);
        // 会员折扣总优惠
        BigDecimal hiuYuanZhekoutotal = new BigDecimal(0);

        BigDecimal totalCashPrice = new BigDecimal(0);
        // 商品优惠方式
        Integer activityStatus = null;
        //HashSet<Integer> mjzzIds = new HashSet<Integer>();// 满减赠折活动ID
        // 满减赠折活动ID
        HashSet<ManjianzengzheDTO> mjzzIds = new HashSet<>();
        // 满减赠折每个活动累计金额
        Map<Integer, BigDecimal> mjzzAmounts = new HashMap<>();
        // 满折件数
        Map<Integer, Integer> mzNum = new HashMap<>();
        // 满减赠折每个活动的规则
        Map<Integer, Object> mjzzRules = new HashMap<>();
        Integer judgeStatus = null;
        List<ShippingListVO> shippingList = new ArrayList<>();
        ShopParaVO sp = new ShopParaVO();
        boolean isCombination = !StrUtil.isEmpty(cp.getCombinationId());
        CombinationVO combinationVO = new CombinationVO();
        if (isCombination) {
            combinationVO = activityServiceClient.queryCombinationInfoById(cp.getCombinationId());
            sp.setCombinationType(combinationVO.getCombinationType());
        }
        //满减赠折优惠总金额初始化，默认值为0
        //总满减优惠金额
        sp.setTotalManjianDiscount(BigDecimal.ZERO);
        //总满折优惠金额
        sp.setTotalManzheDiscount(BigDecimal.ZERO);
        //总优惠券金额
        sp.setCouponTotalPrice(BigDecimal.ZERO);

        // 运费判断
        if (cp.getFee() != null && cp.getFee().compareTo(new BigDecimal(0)) < 0) {
            return ResponseResult.fail(BizCodeEnume.WRONG_FEE);
        }

        // 最终配送费
        BigDecimal fee = new BigDecimal(0);
        if (null != cp.getFee()) {
            fee = cp.getFee();
        }
        // 配送费
        BigDecimal pick = new BigDecimal(0);
        if (null != cp.getPick()) {
            pick = cp.getPick();
        }
        String token = cp.getToken();
        String userId = null;
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        if (userInfo == null || StrUtil.isBlank(userInfo.getUserId())) {
            return ResponseResult.fail(BizCodeEnume.USER_IS_NOT_LOGGED_IN);
        }
        userId = userInfo.getUserId();

        //查询用户是否开通plus会员 并且是否有折扣权益
        BigDecimal usedamout = new BigDecimal(0);
        ResponseResult selectplusmember = userServiceClient.queryUserPlusInfo(userId);
        //plus会员折扣
        BigDecimal plusDiscount = null;
        //plus会员剩余可用额度
        BigDecimal plusResidualLimit = null;
        if (selectplusmember != null && selectplusmember.getCode() == 1
                && selectplusmember.getData() instanceof Map<?, ?> plusRaw) {
            @SuppressWarnings("unchecked")
            Map<String, Object> userPlusInfo = (Map<String, Object>) plusRaw;
            if (userPlusInfo.get("discount") != null) {
                plusDiscount = new BigDecimal(userPlusInfo.get("discount").toString());
            }
            if (userPlusInfo.get("residualLimit") != null) {
                plusResidualLimit = new BigDecimal(userPlusInfo.get("residualLimit").toString());
                usedamout = plusResidualLimit;
            }
        }

        //pans 是否是积分商品
        if (null != cp.getIntegralGoods() && 0 == cp.getIntegralGoods()) {
            //获取店铺列表
            List<StoreInformationVO> storeInformationList = cp.getStoreInformationList();
            for (StoreInformationVO storeInformation : storeInformationList) {
                //获取商品列表
                List<GoodsInformationVO> goodsBaseList = storeInformation.getGoodsBaseList();
                for (GoodsInformationVO goodsInformation : goodsBaseList) {
                    Integer goodsId = goodsInformation.getGoodsId();
                    Map<String, Object> map = new HashMap<>();
                    map.put("goodsId", goodsId);
                    //根据goodsId查询 积分商品的价格 所需积分 库存
                    ResponseResult resp = goodServiceClient.queryIntegralGoodsBygoodsId(map);
                    if (resp == null || !(resp.getData() instanceof Map<?, ?>)) {
                        return ResponseResult.fail(BizCodeEnume.GOODS_NOT_EXIST);
                    }
                    @SuppressWarnings("unchecked")
                    Map<String, Object> resmap = (Map<String, Object>) resp.getData();
                    //库存
                    Integer goodsStock = (Integer) resmap.get("goodsStock");
                    //所需积分
                    Integer needIntegral = (Integer) resmap.get("needIntegral");
                    //价格
                    Double goodsPrice = (Double) resmap.get("goodsPrice");
                    Double pointsRate = (Double) resmap.get("pointsRate");
                    // 计算嘉币价格
                    BigDecimal cashPrice = (BigDecimal) resmap.get("cashPrice");
                    BigDecimal goodsPrice2 = new BigDecimal(goodsPrice == null ? 0 : goodsPrice);
                    if (null != goodsInformation.getGoodsNum() && goodsInformation.getGoodsNum() <= goodsStock) {
                        //积分商品不为null qie不为0
                        if (null != goodsPrice2 && (goodsPrice2.compareTo(new BigDecimal(0)) == 1)) {
                            //购买的商品数量乘于商品价格
                            sp.setTotalPrice(goodsPrice2.multiply(new BigDecimal(goodsInformation.getGoodsNum())));
                        }


                        if (null != cashPrice) {
                            sp.setTotalCashPrice(cashPrice.multiply(new BigDecimal(goodsInformation.getGoodsNum())));
                        }

                        //购买的商品数量乘于购买所需的积分
                        sp.setTotalIntegral(needIntegral * goodsInformation.getGoodsNum());
                        // 存放shopId&&每家店铺运费
                        ShippingListVO sl = new ShippingListVO();
                        sl.setShopId(0);
                        sl.setGoodsNo(1);
                        // 存放店铺红包id
                        sl.setShopLimit(null);
                        sl.setShipping(new BigDecimal(0));
                        sl.setGoodsIdList(null);
                        // 存放单品红包id
                        sl.setGoodsCatLimit(null);
                        sl.setRedpacketTType(null);
                        sl.setGoodsQuantity(0);
                        sl.setShopPrice(new BigDecimal(goodsPrice).multiply(new BigDecimal(goodsInformation.getGoodsNum())));
                        sl.setReceivingTime(new BigDecimal(72));
                        sl.setCouponPrice(new BigDecimal(0));
                        sl.setAllowanceShopId("");
                        sl.setItemShopId(null);
                        sl.setGoodsNo(1);
                        sl.setItemShopPrice(new BigDecimal(0));
                        sl.setAllowanceShopPrice(new BigDecimal("0"));
                        sl.setShopTotalPrice(new BigDecimal(goodsPrice));
                        shippingList.add(sl);
                        sp.setShippingList(shippingList);
                        rr.setCode(1);
                        rr.setData(sp);
                        rr.setMessage("");
                    } else {
                        rr.setCode(2);
                        rr.setMessage("库存不足");
                    }
                    return rr;
                }
            }
        }
        // 获取红包Id,并封装Map,在向malls请求数据
        Map<String, String> redPacketIdMap = new HashMap<>();
        // 红包id
        Integer redPacketNo = cp.getRedPacketId();
        // 优惠券id
        String voucherId = null;
        // 平台优惠券金额
        BigDecimal platformVoucherPrice = new BigDecimal(0);
        List<Integer> avaliableGoods = new ArrayList<>();
        Integer couponNo = null;
        BigDecimal redPacketPrice = new BigDecimal(0);
        BigDecimal couponPrice = new BigDecimal(0);
        // 红包订单限额
        BigDecimal orderLimit = new BigDecimal(0);
        // 红包编码
        String redpacketId = null;
        // 平台优惠券id
        Integer platFormVoucherId = null;
        // 店铺红包id
        String shopLimit = null;
        // 红包使用状态
        Integer redPacketStatus = null;
        // 优惠券使用状态
        Integer voucherStatus = null;
        Integer redpacketTType = null;

        // 计算plus会员折扣用
        BigDecimal plusgoodsprice = new BigDecimal(0);
        // 计算plus会员总折扣
        BigDecimal plusgoodstotal = new BigDecimal(0);

        List<String> voucherIdList = new ArrayList<>();

        if (null != redPacketNo && redPacketNo != 0 && !isCombination) {
            String redPacketId = redPacketNo.toString();
            redPacketIdMap.put("redpacketId", redPacketId);
            // 创建接收bean
            HouRedpacketBaseVO redPacket = activityServiceClient.getRedpDetail(redPacketIdMap);
            if (null != redPacket && 2 == redPacket.getRedpacketState()) {
                return ResponseResult.fail(BizCodeEnume.RED_ENVELOPE_USED);
            }
            if (null != redPacket && 3 == redPacket.getRedpacketState()) {
                return ResponseResult.fail(BizCodeEnume.RED_ENVELOPE_EXPIRE);
            }
            if (null != redPacket && redPacket.getRedpacketEndDate().compareTo(DateUtils.getNowDateToDf()) < 0) {
                return ResponseResult.fail(BizCodeEnume.RED_ENVELOPE_EXPIRE);
            }
            if (null != redPacket && userId.equals(redPacket.getRedpacketOwnerId())) {
                redpacketTType = redPacket.getRedpacketTType();
                // 获取店铺红包id
                shopLimit = redPacket.getShopLimit();
                // 红包使用状态
                redPacketStatus = redPacket.getRedpacketState();
                // 使用红包订单限额
                Integer redpacketTOrderlimit = redPacket.getRedpacketTOrderlimit();
                // 类型转换
                orderLimit = new BigDecimal(redpacketTOrderlimit.toString());

                // 获取红包价格
                redPacketPrice = new BigDecimal(redPacket.getRedpacketPrice());
                // 获取红包编码
                redpacketId = redPacket.getRedpacketId().toString();
            } else {
                redPacketNo = 0;
            }
        }
        try {
            platFormVoucherId = cp.getVoucherId();
            Map<String, Object> voucherTIdMap = new HashMap<>();
            if (platFormVoucherId != null && 0 != platFormVoucherId && !isCombination) {
                String platFormCouponsId = platFormVoucherId.toString();
                // 当参数不为空时，则使用平台优惠券
                System.out.println("-----Enter-----");
                // 改走券查询能力（当前由遗留适配器接管，行为与直接调 activity 一致）
                CouponInfo coupon = couponQueryCapability.findByVoucherId(platFormCouponsId).orElse(null);
                if (null != coupon && Integer.valueOf(2).equals(coupon.state())) {
                    return ResponseResult.fail(BizCodeEnume.COUPON_USED);
                }
                if (null != coupon && Integer.valueOf(3).equals(coupon.state())) {
                    return ResponseResult.fail(BizCodeEnume.COUPON_EXPIRE);
                }
                if (null != coupon && Integer.valueOf(4).equals(coupon.state())) {
                    return ResponseResult.fail(BizCodeEnume.COUPON_RECYCLE);
                }
                if (null != coupon && null != coupon.endDate()
                        && coupon.endDate().compareTo(DateUtils.getNowDateToDf()) < 0) {
                    return ResponseResult.fail(BizCodeEnume.COUPON_EXPIRE);
                }
                if (null != coupon && userId.equals(coupon.ownerId())) {
                    // 获取平台优惠券id
                    sp.setPlatformVoucherCode(platFormCouponsId);
                    // 获取优惠券价格
                    platformVoucherPrice = coupon.price();
                    sp.setPlatformVoucherPrice(platformVoucherPrice);
                    // 参加平台优惠券商品id
                    avaliableGoods = coupon.availableGoods();
                    sp.setAvaliableGoods(avaliableGoods);
                } else {
                    platformVoucherPrice = new BigDecimal(0);
                    sp.setPlatformVoucherPrice(platformVoucherPrice);
                    avaliableGoods = null;
                    sp.setAvaliableGoods(avaliableGoods);
                }
            } else {
                platformVoucherPrice = new BigDecimal(0);
                sp.setPlatformVoucherPrice(platformVoucherPrice);
                avaliableGoods = null;
                sp.setAvaliableGoods(avaliableGoods);
            }
        } catch (Exception e) {
            System.out.println("-----voucherTIdIsNull-----" + e.getMessage());
        }
        Boolean vkFlag = StringUtils.isNotEmpty(cp.getShareUserId()) && !Objects.equals(cp.getShareUserId(), "0");
        List<BigDecimal> memberDiscountList = new ArrayList<>();
        List<BigDecimal> plusDiscountList = new ArrayList<>();
        // 根据店铺循环
        //ZZZ:店铺循环开始，s1存放每个店铺的信息
        for (int i = 0; i < wapList.size(); i++) {
            // 存放shopId&&每家店铺运费
            ShippingListVO sl = new ShippingListVO();
            // 各店铺总价(不含运费与优惠价格)
            BigDecimal shopPrice = new BigDecimal(0);
            // 参加红包活动总价
            BigDecimal itemShopPrice = BigDecimal.ZERO;
            // 参加购物津贴活动总价
            BigDecimal allowanceShopPrice = BigDecimal.ZERO;
            Integer shopId = wapList.get(i).getShopId();
            // 店铺运费
            BigDecimal shippingFee = wapList.get(i).getShippingFee();
            sl.setShopId(shopId);
            // 存放店铺红包id
            sl.setShopLimit(shopLimit);
            sl.setShipping(shippingFee);
            sl.setRedpacketTType(redpacketTType);
            //店铺满减金额
            sl.setShopManjianDiscount(BigDecimal.ZERO);
            //店铺满折金额
            sl.setShopManzheDiscount(BigDecimal.ZERO);

            // 每家店铺总价格
            BigDecimal shopTotalPrice = new BigDecimal(0);
            // 商品数量
            Integer goodsNo = 0;
            // 参加多品红包商品数量
            Integer goodsQuantity = 0;
            List<BigDecimal> goodsAmountList = new ArrayList<>();
            List<Integer> hyORstep = new ArrayList<>();
            //优惠券商品
            List<GoodsInformationVO> goodsBaseList = new ArrayList<>();
            // 获取商品
            //ZZZ:获取每个店铺下的所有商品
            List<WapShopShippingResultVO> wssrList = wapList.get(i).getGoodsBaseList();
            if (CollUtil.isEmpty(wssrList)) {
                return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), "店铺商品运费信息为空，请刷新后重试");
            }
            //ZZZ:遍历店铺商品
            for (int n = 0; n < wssrList.size(); n++) {
                //ZZZ：获取不同包裹底下的所有商品
                List<WapShippingQueryResultVO> wsqrList = wssrList.get(n).getGoodsBaseList();
                if (CollUtil.isEmpty(wsqrList)) {
                    return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), "包裹商品信息为空，请刷新后重试");
                }

                //ZZZ:遍历包裹商品
                for (WapShippingQueryResultVO wapShippingQueryResultVO : wsqrList) {
                    BigDecimal plusMemberDiscount = BigDecimal.ZERO;
                    Integer commodityId = wapShippingQueryResultVO.getGoodsId();
                    Integer goodsStock = wapShippingQueryResultVO.getGoodsStock() == null
                            ? 0 : wapShippingQueryResultVO.getGoodsStock();
                    Integer goodsNumber = wapShippingQueryResultVO.getGoodsNum() == null
                            ? 0 : wapShippingQueryResultVO.getGoodsNum();
                    BigDecimal goodsPrice = BigDecimal.ZERO;
                    activityStatus = wapShippingQueryResultVO.getActivityStatus() == null
                            ? 0 : wapShippingQueryResultVO.getActivityStatus();
                    ActivityGoodsDetailDTO agd = wapShippingQueryResultVO.getActivityGoodsDetail();
                    // 运费结果未带促销时，本进程探测生效满减并挂上，避免 mall 路径静默丢优惠
                    if ((activityStatus == 0 || agd == null || agd.getPromotionId() == null)
                            && wapShippingQueryResultVO.getCommonId() != null) {
                        try {
                            var mj = reductionPolicyQueryService.selectByGoodsId(wapShippingQueryResultVO.getCommonId());
                            if (mj != null && mj.getPromotionId() != null) {
                                var rules = reductionPolicyQueryService.selectList(mj.getPromotionId());
                                if (agd == null) {
                                    agd = new ActivityGoodsDetailDTO();
                                }
                                agd.setPromotionId(mj.getPromotionId());
                                agd.setActivityId(mj.getPromotionId());
                                agd.setActivityStatus(5);
                                agd.setRuleList(rules);
                                agd.setActivityEndTime(mj.getEndTime());
                                wapShippingQueryResultVO.setActivityGoodsDetail(agd);
                                wapShippingQueryResultVO.setActivityStatus(5);
                                activityStatus = 5;
                            }
                        } catch (Exception e) {
                            LOG.warn("满减探测失败 commonId={}: {}", wapShippingQueryResultVO.getCommonId(), e.toString());
                        }
                    }
                    if (wapShippingQueryResultVO.getDealerGoodsPrice() == null) {
                        wapShippingQueryResultVO.setDealerGoodsPrice(BigDecimal.ZERO);
                    }
                    // 查询店铺发货时长（连接池抖动时不阻断计价）
                    BigDecimal shippingTime = null;
                    try {
                        shippingTime = orderBaseMapper.queryShippingTimeByGoods(shopId, wapShippingQueryResultVO.getGoodsId());
                    } catch (Exception e) {
                        LOG.warn("queryShippingTimeByGoods failed shopId={} goodsId={}: {}",
                                shopId, wapShippingQueryResultVO.getGoodsId(), e.toString());
                    }
                    if (shippingTime != null) {
                        BigDecimal receivingTime = shippingTime.setScale(0, RoundingMode.HALF_UP);
                        sl.setReceivingTime(receivingTime);
                    } else {
                        BigDecimal receivingTime = new BigDecimal(72);
                        sl.setReceivingTime(receivingTime);
                    }
                    if (isCombination) {
                        Map<String, Object> combinationMap = new HashMap<>();
                        // 组合商品计算
                        setCombinationInfo(sl, combinationVO, wapShippingQueryResultVO, sp, goodsAmountList,
                                goodsNo, goodsQuantity, shopTotalPrice, goodsTotalPrice, shopPrice, combinationMap, cp,
                                memberDiscountList, plusDiscountList, plusgoodsprice, hiuYuanZhekoutotal,
                                selectplusmember, plusDiscount, usedamout, plusgoodstotal);
                        shopTotalPrice = (BigDecimal) combinationMap.get("shopTotalPrice");
                        goodsTotalPrice = (BigDecimal) combinationMap.get("goodsTotalPrice");
                        shopPrice = (BigDecimal) combinationMap.get("shopPrice");
                        goodsPrice = (BigDecimal) combinationMap.get("goodsPrice");
                        hiuYuanZhekoutotal = (BigDecimal) combinationMap.get("hiuYuanZhekoutotal");
                        usedamout = (BigDecimal) combinationMap.get("usedamout");
                        plusgoodstotal = (BigDecimal) combinationMap.get("plusgoodstotal");
                        plusgoodsprice = (BigDecimal) combinationMap.get("plusgoodsprice");
                    } else if (activityStatus == 2) {
                        hyORstep.add(2);
                        // 秒杀：改走促销算价扩展点（当前由遗留适配器接管，行为与直接调 activity 一致）
                        PromotionCalcGateway.Result seckillPromo = promotionCalcGateway.calc(
                                LegacyActivityPromotionCalcExtension.SECKILL,
                                null,
                                java.util.List.of(),
                                wapShippingQueryResultVO.getGoodsPrice()
                                        .multiply(new BigDecimal(wapShippingQueryResultVO.getGoodsNum())),
                                buildPromoExtra(token, wapShippingQueryResultVO.getGoodsId(), null,
                                        wapShippingQueryResultVO.getGoodsPrice(),
                                        agd.getActivityPrice(),
                                        wapShippingQueryResultVO.getGoodsNum()));
                        if (seckillPromo.aborted()) {
                            rr.setCode(3);
                            rr.setMessage(seckillPromo.abortReason());
                            return rr;
                        }
                        goodsPrice = (BigDecimal) seckillPromo.promotionDetail()
                                .get(LegacyActivityPromotionCalcExtension.DETAIL_UNIT_PRICE);
                        // 秒杀
                        String goodsId = wapShippingQueryResultVO.getGoodsId().toString();
                        goodsNumber = wapShippingQueryResultVO.getGoodsNum();
                        BigDecimal goodsNum = new BigDecimal(goodsNumber);
                        // 付款价格(不包含运费)
                        BigDecimal price = goodsPrice.multiply(goodsNum);
                        if (platFormVoucherId != null && platFormVoucherId != 0
                                && avaliableGoods.contains(Integer.parseInt(goodsId))) {
                            allowancePrice = allowancePrice.add(price);
                            allowanceShopPrice = allowanceShopPrice.add(price);
                            sl.setAllowanceShopId(shopId.toString());
                        } else {
                            sl.setAllowanceShopId("");
                        }
                        // 商品小计
                        goodsAmountList.add(price);
                        sl.setGoodsAmountList(goodsAmountList);
                        //plus会员折扣价
                        plusgoodsprice = price;
                        shopTotalPrice = shopTotalPrice.add(price);
                        goodsTotalPrice = goodsTotalPrice.add(price);
                        shopPrice = shopPrice.add(price);
                        goodsNo = goodsNo + goodsNumber;
                        sl.setGoodsQuantity(goodsQuantity);
                        sl.setGoodsNo(goodsNo);
                    } else if (activityStatus == 3) {
                        hyORstep.add(3);
                        // 限时折扣：改走促销算价扩展点（当前由遗留适配器接管，行为与直接调 activity 一致）
                        PromotionCalcGateway.Result discountPromo = promotionCalcGateway.calc(
                                LegacyActivityPromotionCalcExtension.DISCOUNT,
                                null,
                                java.util.List.of(),
                                wapShippingQueryResultVO.getGoodsPrice()
                                        .multiply(new BigDecimal(wapShippingQueryResultVO.getGoodsNum())),
                                buildPromoExtra(token, wapShippingQueryResultVO.getGoodsId(),
                                        wapShippingQueryResultVO.getCommonId(),
                                        wapShippingQueryResultVO.getGoodsPrice(),
                                        agd.getActivityPrice(),
                                        wapShippingQueryResultVO.getGoodsNum()));
                        if (discountPromo.aborted()) {
                            rr.setCode(3);
                            rr.setMessage(discountPromo.abortReason());
                            return rr;
                        }
                        goodsPrice = (BigDecimal) discountPromo.promotionDetail()
                                .get(LegacyActivityPromotionCalcExtension.DETAIL_UNIT_PRICE);
                        // 限时折扣
                        String goodsId = wapShippingQueryResultVO.getGoodsId().toString();
                        goodsNumber = wapShippingQueryResultVO.getGoodsNum();
                        BigDecimal goodsNum = new BigDecimal(goodsNumber);
                        // 付款价格(不包含运费)
                        BigDecimal price = goodsPrice.multiply(goodsNum);
                        if (platFormVoucherId != null && platFormVoucherId != 0
                                && avaliableGoods.contains(Integer.parseInt(goodsId))) {
                            allowancePrice = allowancePrice.add(price);
                            allowanceShopPrice = allowanceShopPrice.add(price);
                            sl.setAllowanceShopId(shopId.toString());
                        } else {
                            sl.setAllowanceShopId("");
                        }
                        // 商品小计
                        goodsAmountList.add(price);
                        sl.setGoodsAmountList(goodsAmountList);
                        //plus会员折扣价
                        plusgoodsprice = price;
                        shopTotalPrice = shopTotalPrice.add(price);
                        goodsTotalPrice = goodsTotalPrice.add(price);
                        shopPrice = shopPrice.add(price);
                        goodsNo = goodsNo + goodsNumber;
                        sl.setGoodsQuantity(goodsQuantity);
                        sl.setGoodsNo(goodsNo);
                    } else if (activityStatus == 4 && cp.getPlatForm() != null && cp.getPlatForm() != 1) {
                        hyORstep.add(4);
                        // 拼团
                        if (agd.getType() == 0) {
                            goodsPrice = agd.getPintuanPrice();
                        } else {
                            goodsPrice = agd.getPriceOne();
                        }
                        goodsNumber = wapShippingQueryResultVO.getGoodsNum();
                        BigDecimal goodsNum = new BigDecimal(goodsNumber);
                        // 付款价格(不包含运费)
                        BigDecimal price = goodsPrice.multiply(goodsNum);
                        shopTotalPrice = shopTotalPrice.add(price);
                        // 商品小计
                        goodsAmountList.add(price);
                        sl.setGoodsAmountList(goodsAmountList);
                        //plus会员折扣价
                        plusgoodsprice = price;
                        goodsTotalPrice = goodsTotalPrice.add(price);
                        shopPrice = shopPrice.add(price);
                        goodsNo = goodsNo + goodsNumber;
                        sl.setGoodsNo(goodsNo);
                    } else if (activityStatus == 5) {
                        hyORstep.add(5);
                        // 满减赠折
                        goodsPrice = wapShippingQueryResultVO.getGoodsPrice();
                        goodsNumber = wapShippingQueryResultVO.getGoodsNum();
                        BigDecimal goodsNum = new BigDecimal(goodsNumber);
                        // 付款价格(不包含运费)
                        BigDecimal price = goodsPrice.multiply(goodsNum);
                        //满减赠折活动ID
                        Integer promotionId = agd.getPromotionId();
                        //满减赠折活动规则
                        List<ManjianzengzheGoodsRuleQueryDTO> ruleList = agd.getRuleList();
                        ManjianzengzheDTO dto = new ManjianzengzheDTO();
                        List<Integer> indexList = new ArrayList<>();
                        indexList.add(n);
                        List<Integer> goodsIdList = new ArrayList<>();
                        goodsIdList.add(wapShippingQueryResultVO.getGoodsId());
                        List<BigDecimal> goodsPriceList = new ArrayList<>();
                        goodsPriceList.add(price);
                        dto.setGoodsId(wapShippingQueryResultVO.getGoodsId());
                        dto.setPromotionId(promotionId);
                        dto.setIndexList(indexList);
                        dto.setIndex(n);
                        dto.setGoodsIdList(goodsIdList);
                        dto.setGoodsPriceList(goodsPriceList);
                        if (!mjzzIds.isEmpty()) {
                            List<Integer> mjIdList = mjzzIds.stream().map(ManjianzengzheDTO::getPromotionId).collect(Collectors.toList());
                            if (!mjIdList.contains(promotionId)) {
                                // 收集满减赠折活动ID
                                mjzzIds.add(dto);
                            } else {
                                HashSet<ManjianzengzheDTO> newSet = new HashSet<>();
                                for (ManjianzengzheDTO mjDto : mjzzIds) {
                                    if (!mjDto.getPromotionId().equals(promotionId)) {
                                        newSet.add(mjDto);
                                    } else {
                                        indexList.addAll(mjDto.getIndexList());
                                        dto.setIndexList(indexList);
                                        goodsIdList.addAll(mjDto.getGoodsIdList());
                                        dto.setGoodsIdList(goodsIdList);
                                        goodsPriceList.addAll(mjDto.getGoodsPriceList());
                                        dto.setGoodsPriceList(goodsPriceList);
                                    }
                                }
                                newSet.add(dto);
                                mjzzIds = newSet;
                            }
                        } else {
                            // 收集满减赠折活动ID
                            mjzzIds.add(dto);
                        }
                        // 收集满减赠折每个活动的累计金额
                        if (null == mjzzAmounts.get(promotionId)) {
                            mjzzAmounts.put(promotionId, price);
                        } else {
                            BigDecimal tmp = mjzzAmounts.get(promotionId).add(price);
                            mjzzAmounts.put(promotionId, tmp);
                        }
                        //收集满减赠折每个活动的活动规则
                        mjzzRules.putIfAbsent(promotionId, ruleList);
                        //收集满赠活动商品数量
                        if (3 == ruleList.get(0).getDiscountType()) {
                            if (null == mzNum.get(promotionId)) {
                                mzNum.put(promotionId, goodsNumber);
                            } else {
                                Integer tmpNum = mzNum.get(promotionId) + goodsNumber;
                                mzNum.put(promotionId, tmpNum);
                            }
                        }
                        // 商品小计
                        goodsAmountList.add(price);
                        sl.setGoodsAmountList(goodsAmountList);
                        //plus会员折扣价
                        plusgoodsprice = price;
                        shopTotalPrice = shopTotalPrice.add(price);
                        goodsTotalPrice = goodsTotalPrice.add(price);
                        shopPrice = shopPrice.add(price);
                        goodsNo = goodsNo + goodsNumber;
                        sl.setGoodsNo(goodsNo);
                    } else {
                        hyORstep.add(0);
                        String goodsId = wapShippingQueryResultVO.getGoodsId().toString();
                        goodsNumber = wapShippingQueryResultVO.getGoodsNum();
                        BigDecimal goodsNum = new BigDecimal(goodsNumber);
                        goodsPrice = wapShippingQueryResultVO.getGoodsPrice();
                        // 砍价优先于阶梯价：本人把这个商品砍到底价时按成交价成交（一人一单，下单后由前端置 ORDERED）
                        BigDecimal bargainUnitPrice = bargainPricePricingHelper.unitPrice(
                                StrUtil.isBlank(userId) ? null : Long.valueOf(userId), goodsId, goodsPrice);
                        if (bargainUnitPrice != null) {
                            goodsPrice = bargainUnitPrice;
                        } else {
                            // 阶梯价：数量命中区间时按阶梯单价成交（未命中/异常保持原价，不影响算价主链路）
                            BigDecimal tierUnitPrice = tierPricePricingHelper.unitPrice(
                                    goodsId, goodsNum.intValue(), goodsPrice);
                            if (tierUnitPrice != null) {
                                goodsPrice = tierUnitPrice;
                            }
                        }
                        // 付款价格(不包含运费)
                        BigDecimal price = goodsPrice.multiply(goodsNum);
                        if (platFormVoucherId != null && platFormVoucherId != 0
                                && avaliableGoods.contains(Integer.parseInt(goodsId))) {
                            allowancePrice = allowancePrice.add(price);
                            allowanceShopPrice = allowanceShopPrice.add(price);
                            sl.setAllowanceShopId(shopId.toString());
                        } else {
                            sl.setAllowanceShopId("");
                        }
                        // 商品小计
                        goodsAmountList.add(price);
                        sl.setGoodsAmountList(goodsAmountList);
                        //plus会员折扣价
                        plusgoodsprice = price;
                        goodsTotalPrice = goodsTotalPrice.add(price);
                        shopTotalPrice = shopTotalPrice.add(price);
                        shopPrice = shopPrice.add(price);
                        goodsNo = goodsNo + goodsNumber;
                        sl.setGoodsQuantity(goodsQuantity);
                        sl.setGoodsNo(goodsNo);
                        // 计算嘉币、劲豆
                        if (null != wapShippingQueryResultVO.getCashPrice()) {
                            totalCashPrice = totalCashPrice.add(wapShippingQueryResultVO.getCashPrice().multiply(goodsNum));
                        }
                    }

                    BigDecimal memberDiscountByVoucher = BigDecimal.ZERO;
                    BigDecimal plusDiscountByVoucher = BigDecimal.ZERO;
                    BigDecimal memberDiscount = goodsPrice;
                    if (!isCombination || combinationVO.getCombinationType() == 2) {
                        //总商品价格减去总运费  得出总的商品价格
                        if (null != cp.getMemberClassId() && 0 != cp.getMemberClassId()) {
                            //商品价格                        //会员折扣
                            BigDecimal hiuYuanZhekou = plusgoodsprice;
                            //根据会员的会员等级id 查询出会员折扣
                            String gradeDiscount = resolveGradeDiscount(cp.getMemberClassId());

                            memberDiscount = memberDiscount.subtract(memberDiscount.multiply(new BigDecimal(gradeDiscount)).setScale(2, RoundingMode.HALF_UP));
                            //计算出使用会员折扣，折扣了多少钱
                            hiuYuanZhekou = hiuYuanZhekou.subtract(hiuYuanZhekou.multiply(new BigDecimal(gradeDiscount)).setScale(2, RoundingMode.HALF_UP));
                            memberDiscountByVoucher = hiuYuanZhekou.divide(new BigDecimal(goodsNumber), 2, RoundingMode.HALF_UP);
                            memberDiscountList.add(hiuYuanZhekou);
                            hiuYuanZhekoutotal = hiuYuanZhekoutotal.add(hiuYuanZhekou);
                            plusgoodsprice = plusgoodsprice.subtract(hiuYuanZhekou);
                        } else {
                            memberDiscountList.add(BigDecimal.ZERO);
                        }
                        plusMemberDiscount = memberDiscount;
                        //计算plus会员折扣金额
                        //查询是否为plus商品
                        if (selectplusmember.getCode() == 1 && (cp.getPlatForm() != null && cp.getPlatForm() == 1)) {
                            Integer integer = goodServiceClient.queryGoodsIsPlus(wapShippingQueryResultVO.getCommonId());
                            if (integer == 1) {
                                //plus会员折扣
                                BigDecimal data = plusDiscount;
                                data = new BigDecimal(1).subtract(data);
                                //plus商品会员折扣金额
                                BigDecimal multiply = plusgoodsprice.multiply(data);
                                //判断可用额度
                                if (usedamout.compareTo(new BigDecimal(-1)) != 0) {
                                    //剩余额度不足
                                    if (multiply.compareTo(usedamout) > 0) {
                                        multiply = usedamout;
                                    }
                                    plusMemberDiscount = multiply.divide(BigDecimal.valueOf(goodsNumber), 2, RoundingMode.HALF_UP);
                                    if (plusMemberDiscount.multiply(BigDecimal.valueOf(goodsNumber)).compareTo(multiply) != 0) {
                                        multiply = plusMemberDiscount.multiply(BigDecimal.valueOf(goodsNumber));
                                    }
                                    usedamout = usedamout.subtract(multiply);
                                }
                                BigDecimal add = plusgoodstotal.add(multiply);
                                plusDiscountByVoucher = plusMemberDiscount.divide(new BigDecimal(goodsNumber), 2, RoundingMode.HALF_UP);
                                ;
                                plusDiscountList.add(plusMemberDiscount);
                                //plus会员总折扣
                                plusgoodstotal = add.setScale(2, RoundingMode.HALF_UP);
                            } else {
                                plusDiscountList.add(BigDecimal.ZERO);
                            }
                        } else {
                            plusDiscountList.add(BigDecimal.ZERO);
                        }
                    }
                    // 优惠券商品
                    if (cp.getIsAutoChoose() != null && cp.getIsAutoChoose() == 1) {
                        GoodsInformationVO voucherGoods = new GoodsInformationVO();
                        voucherGoods.setGoodsId(commodityId);
                        voucherGoods.setGoodsNum(goodsNumber);
                        voucherGoods.setGoodsPrice(Double.valueOf(goodsPrice.subtract(memberDiscountByVoucher).subtract(plusDiscountByVoucher).setScale(2, RoundingMode.HALF_UP).toString()));
                        goodsBaseList.add(voucherGoods);
                    }

                    // 1 是通过分享页下单  2 是通过 商城直接下单
                    int fxType = 1;
                    if (!vkFlag) {
                        fxType = 2;
                    }
                    //创建分销佣金订单
                    WapShippingQueryResultVO orderCommission = fxService.selectIsVkShippingInfo(wapShippingQueryResultVO, userInfo, cp.getShareUserId(), fxType);
                    if (null != orderCommission) {
                        sp.setVkFlag(true);
                    }
                    // 判断商品是否有最大库存数量
                    if (goodsNumber > goodsStock) {
                        judgeStatus = 1;
                        rr.setCode(2);
                        rr.setData(null);
                        rr.setMessage("当前商品数量超过库存数量!");
                    } else {
                        judgeStatus = 0;
                    }
                }
            }
            // 先计算会员折扣再计算优惠券（无券时 couponId 可能为 null，按未使用处理）
            String voucherNo = cp.getStoreInformationList().get(i).getCouponId();
            if (StrUtil.isBlank(voucherNo)) {
                voucherNo = "0";
            }
            if (cp.getIsAutoChoose() != null && cp.getIsAutoChoose() == 1) {
                Map<String, Object> voucherPara = new HashMap<>();
                voucherPara.put("token", cp.getToken());
                voucherPara.put("platForm", cp.getPlatForm());
                voucherPara.put("shopId", shopId);
                voucherPara.put("goodsInfo", goodsBaseList);
                // 优惠券服务（activity）不可用时降级为「本次不使用优惠券」，不能让整个结算算价失败。
                // 本机曾因未部署 activity 服务导致 calculatePrice 稳定 20008，结算页永远打不开。
                ResponseResult voucherResp;
                try {
                    voucherResp = activityServiceClient.selectAvailableVcBase(voucherPara);
                } catch (Exception couponEx) {
                    LOG.warn("优惠券服务不可用，本次按未使用优惠券算价：{}", couponEx.toString());
                    voucherResp = null;
                }
                List<HouVoucherBaseVO> voucherBaseVOList = voucherResp == null || voucherResp.getData() == null
                        ? null
                        : JSON.parseArray(JSON.toJSONString(voucherResp.getData()), HouVoucherBaseVO.class);
                if (CollUtil.isNotEmpty(voucherBaseVOList) && voucherBaseVOList.get(0) != null
                        && StrUtil.isNotBlank(voucherBaseVOList.get(0).getVoucherNo())) {
                    voucherNo = voucherBaseVOList.get(0).getVoucherNo();
                    sl.setVoucherTitle(voucherBaseVOList.get(0).getVoucherTitle());
                }
            }
            if (!"0".equals(voucherNo) && StrUtil.isNotBlank(voucherNo)) {
                String[] voucherNoList = voucherNo.split("-");
                for (String no : voucherNoList) {
                    // 获取代金券Id,并封装Map,在向malls请求数据
                    Map<String, Object> couponIdMap = new HashMap<>();
                    couponNo = Integer.parseInt(no);
                    String couponId = couponNo.toString();
                    couponIdMap.put("voucherId", couponId);
                    // 创建接收bean
                    VoucherBaseVO voucher = activityServiceClient.getVoucherDetail(couponIdMap);
                    // 获取优惠券价格
                    couponPrice = couponPrice.add(voucher.getVoucherPrice());
                    // 优惠券使用状态
                    voucherStatus = voucher.getVoucherState();
                    sl.setVoucherTitle(voucherNoList.length > 1 ? "组合优惠" : voucher.getVoucherTitle());
                }
                sl.setCouponPrice(couponPrice);
                sl.setVoucherStatus(voucherStatus);
            } else {
                couponNo = 0;
                couponPrice = new BigDecimal(0);
                sl.setCouponPrice(couponPrice);
            }
            sl.setVoucherId(voucherNo);
            couponTotalPrice = couponTotalPrice.add(couponPrice);
            // 存放优惠券集合
            voucherIdList.add(voucherNo);
            //如果是 自提商品运费为0
            if (null != cp.getOrderIsShopTake() && 1 == cp.getOrderIsShopTake()) {
                shippingFee = new BigDecimal(0);
            }
            totalShipping = totalShipping.add(shippingFee);
            goodsTotalPrice = goodsTotalPrice.add(shippingFee);
//            totalCashPrice = totalCashPrice.add(shippingFee);
            // 运费可以抵扣嘉币/劲豆，默认抵扣，可以选择不抵用
            sl.setPromotionIdList(hyORstep);
            try {
                //ZZZ处理每家店铺的满减赠折优惠内容
                DealMjzzPromotion(sp, sl, mjzzIds, mjzzAmounts, mjzzRules, mzNum);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //店铺金额 等于店铺商品金额之和 加 运费 减优惠券
            BigDecimal shopPriceTotal = shopPrice.add(sl.getShipping()).subtract(sl.getCouponPrice());
            sl.setMemberDiscountList(memberDiscountList);
            sl.setPlusDiscountList(plusDiscountList);
            sl.setShopPrice(shopPrice);
            sl.setShopPriceTotal(shopPriceTotal);
            sl.setItemShopPrice(itemShopPrice);
            sl.setAllowanceShopPrice(allowanceShopPrice);
            sl.setShopTotalPrice(shopTotalPrice.add(shippingFee).subtract(couponPrice).add(fee));
            sl.setTotalCashPrice(totalCashPrice);
            shippingList.add(sl);
            // 所有店铺总价
            allShopTotalPrice = allShopTotalPrice.add(shopPrice);
            //每个店铺结束清理满减赠折优惠信息
            mjzzRules.clear();
            mjzzAmounts.clear();
            mjzzIds.clear();
        //ZZZ:店铺循环结束
        }

        // 店铺总商品总金额
        totalPrice = totalPrice.add(goodsTotalPrice);
        totalPrice = totalPrice.subtract(hiuYuanZhekoutotal).subtract(plusgoodstotal);
        sp.setMemberDiscountAmount(hiuYuanZhekoutotal);
        totalPrice = totalPrice.subtract(couponTotalPrice).subtract(platformVoucherPrice)
                .subtract(sp.getTotalManjianDiscount()).subtract(sp.getTotalManzheDiscount());
        sp.setNoRedpacketAmount(totalPrice);
        if (redPacketNo != 0) {
            // 判断实付金额是否可以使用红包
            if (totalPrice.compareTo(orderLimit) > 0 || totalPrice.compareTo(orderLimit) == 0) {
                // 去除红包金额
                totalPrice = totalPrice.subtract(redPacketPrice);
            } else {
                rr.setCode(2);
                rr.setMessage("此订单不符合红包或优惠券使用条件，请确认！");
                return rr;
            }
        }
        // 优惠方式
        Integer orderDiscountType = null;
        if (couponNo != 0 && redPacketNo != 0 && activityStatus == 0) {
            orderDiscountType = 5;
        } else if (couponNo != 0) {
            orderDiscountType = 1;
        } else if (redPacketNo != 0) {
            orderDiscountType = 2;
        } else {
            orderDiscountType = 0;
        }
        if (totalPrice.compareTo(new BigDecimal(0)) < 0) {
            totalPrice = new BigDecimal(0);
        }

        sp.setTotalCashPrice(totalCashPrice);

        // 如果前端选择使用积分抵扣运费则把运费加到积分总额上
        if (Integer.valueOf(1).equals(cp.getUsePointsForShipping())) {
            // 运费可以抵扣嘉币/劲豆，默认抵扣，可以选择不抵用
        } else {
            sp.setTotalCashPrice(totalCashPrice.add(totalShipping));
        }

        // 更新订单总价格，包括费用
        sp.setTotalPrice(totalPrice.add(fee));
        // 设置选择的领取方式
        sp.setPick(pick);
        // 设置红包ID
        sp.setRedpacketId(redpacketId);
        // 设置配送列表
        sp.setShippingList(shippingList);
        // 设置配送总价格
        sp.setTotalShipping(totalShipping);
        // 设置优惠券ID列表
        sp.setVoucherIdList(voucherIdList);
        // 设置红包价格
        sp.setRedPacketPrice(redPacketPrice);
        // 设置代金券价格
        sp.setAllowancePrice(allowancePrice);
        // 设置红包状态
        sp.setRedPacketStatus(redPacketStatus);
        // 设置优惠券总价格
        sp.setCouponTotalPrice(couponTotalPrice);
        // 设置订单折扣类型
        sp.setOrderDiscountType(orderDiscountType);
        // 设置所有店铺的总价格
        sp.setAllShopTotalPrice(allShopTotalPrice);
        // 设置加购商品总数
        sp.setPlusgoodstotal(plusgoodstotal);
        // 根据会员等级，设置使用的优惠金额
        if (selectplusmember.getCode() == 1) {
            sp.setPlususedamount(plusResidualLimit);
        }

        if (judgeStatus != 1) {
            rr.setCode(1);
            rr.setData(sp);
        } else {
            rr.setData(sp);
        }

        return rr;
    }

    /**
     * 设置组合商品信息
     *
     * @param sl                 物流信息载体
     * @param combinationVO      组合信息
     * @param wapShippingVO      用户选择的商品信息
     * @param sp                 商铺参数信息
     * @param goodsAmountList    商品金额列表
     * @param goodsNo            商品编号累加器
     * @param goodsQuantity      商品数量
     * @param shopTotalPrice     商铺总价
     * @param goodsTotalPrice    商品总价
     * @param shopPrice          商品单价
     * @param combinationMap     组合商品的映射表
     * @param commonParam        公共参数
     * @param memberDiscountList 会员折扣列表
     * @param plusDiscountList   Plus会员折扣列表
     * @param plusgoodsprice     商品原价
     * @param hiuYuanZhekoutotal 折扣总额
     * @param selectplusmember   Plus会员信息
     * @param plusDiscount       Plus折扣
     * @param usedamout          已使用的优惠金额
     * @param plusgoodstotal     Plus会员总折扣金额
     */
    private void setCombinationInfo(ShippingListVO sl, CombinationVO combinationVO, WapShippingQueryResultVO wapShippingVO,
                                    ShopParaVO sp, List<BigDecimal> goodsAmountList, Integer goodsNo, Integer goodsQuantity,
                                    BigDecimal shopTotalPrice, BigDecimal goodsTotalPrice, BigDecimal shopPrice,
                                    Map<String, Object> combinationMap, CommonParameterVO commonParam, List<BigDecimal> memberDiscountList,
                                    List<BigDecimal> plusDiscountList, BigDecimal plusgoodsprice,
                                    BigDecimal hiuYuanZhekoutotal, ResponseResult selectplusmember, BigDecimal plusDiscount,
                                    BigDecimal usedamout, BigDecimal plusgoodstotal) {
        // 判断组合活动类型，1表示固定组合，其他表示搭配组合
        // 组合活动类型 true固定组合 false搭配组合
        boolean type = combinationVO.getCombinationType() == 1;

        // 根据活动类型获取商品数量和单价
        BigDecimal goodsNum = new BigDecimal(wapShippingVO.getGoodsNum());
        CombinationGoodsInfoVO goodsInfoVO = activityServiceClient.queryCombinationGoodsInfoByIds(combinationVO.getCombinationId(), wapShippingVO.getGoodsId());
        BigDecimal goodsPrice = type ? wapShippingVO.getGoodsPrice() : goodsInfoVO.getMatchingPrices();

        // 计算商品金额并更新列表
        // 付款价格(不包含运费)
        BigDecimal price = goodsPrice.multiply(goodsNum);
        // 商品小计
        goodsAmountList.add(price);
        sl.setGoodsAmountList(goodsAmountList);

        // 初始化或更新物流信息中的商品数量和编号累加器
        sl.setAllowanceShopId("");
        if (sp.getShopMoprice() == null) {
            sp.setShopMoprice(0.0);
        }

        goodsNo = goodsNo + wapShippingVO.getGoodsNum();
        sl.setGoodsQuantity(goodsQuantity);
        sl.setGoodsNo(goodsNo);

        // 处理固定组合商品的逻辑
        if (type) {
            // 计算组合商品的总价和单价
            BigDecimal combinationPrice = combinationVO.getCombinationPrice().multiply(new BigDecimal(commonParam.getCombinationGoodsNum())).setScale(2, RoundingMode.HALF_UP);
            shopTotalPrice = combinationPrice;
            goodsTotalPrice = combinationPrice;
            shopPrice = combinationPrice;
            sl.setGoodsCombinationPrice(combinationVO.getCombinationPrice());

            // 计算会员折扣
            BigDecimal memberDiscount = goodsTotalPrice;
            if (commonParam.getMemberClassId() != null && commonParam.getMemberClassId() != 0) {
                BigDecimal hiuYuanZhekou = plusgoodsprice;
                Double gradeDiscount = resolveGradeDiscountDouble(commonParam.getMemberClassId());
                memberDiscount = memberDiscount.subtract(memberDiscount.multiply(new BigDecimal(gradeDiscount)).setScale(2, RoundingMode.HALF_UP));
                memberDiscountList = Collections.singletonList(memberDiscount);
                hiuYuanZhekoutotal = memberDiscount;
                plusgoodsprice = plusgoodsprice.subtract(hiuYuanZhekou);
            } else {
                memberDiscountList.add(BigDecimal.ZERO);
            }

            // 处理Plus会员折扣逻辑
            BigDecimal plusMemberDiscount = memberDiscount;
            if (selectplusmember.getCode() == 1 && commonParam.getPlatForm() != null && commonParam.getPlatForm() == 1) {
                Integer integer = goodServiceClient.queryGoodsIsPlus(wapShippingVO.getCommonId());
                if (integer == 1) {
                    BigDecimal data = plusDiscount;
                    data = new BigDecimal(1).subtract(data);
                    BigDecimal multiply = plusgoodsprice.multiply(data);

                    // 判断可用优惠额度
                    if (usedamout.compareTo(new BigDecimal(-1)) != 0) {
                        if (multiply.compareTo(usedamout) > 0) {
                            multiply = usedamout;
                        }
                        plusMemberDiscount = multiply.divide(goodsNum, 2, RoundingMode.HALF_UP);
                        multiply = plusMemberDiscount.multiply(goodsNum);
                        usedamout = usedamout.subtract(multiply);
                    }

                    BigDecimal add = plusgoodstotal.add(multiply);
                    plusDiscountList.add(plusMemberDiscount);
                    plusgoodstotal = add.setScale(2, RoundingMode.HALF_UP);
                } else {
                    plusDiscountList.add(BigDecimal.ZERO);
                }
            } else {
                plusDiscountList.add(BigDecimal.ZERO);
            }

            // 更新组合商品映射表
            combinationMap.put("hiuYuanZhekoutotal", hiuYuanZhekoutotal);
            combinationMap.put("usedamout", usedamout);
            combinationMap.put("plusgoodstotal", plusgoodstotal);
            combinationMap.put("plusgoodsprice", BigDecimal.ZERO);
        } else {
            // 更新非固定组合商品的总价和单价
            shopTotalPrice = shopTotalPrice.add(price);
            goodsTotalPrice = goodsTotalPrice.add(price);
            shopPrice = shopPrice.add(price);
            combinationMap.put("hiuYuanZhekoutotal", hiuYuanZhekoutotal);
            combinationMap.put("usedamout", BigDecimal.ZERO);
            combinationMap.put("plusgoodstotal", BigDecimal.ZERO);
            combinationMap.put("plusgoodsprice", price);
        }

        // 更新组合商品映射表中的商品单价和总价
        combinationMap.put("goodsPrice", goodsPrice);
        combinationMap.put("shopTotalPrice", shopTotalPrice);
        combinationMap.put("goodsTotalPrice", goodsTotalPrice);
        combinationMap.put("shopPrice", shopPrice);
    }


    /**
     * 创建订单的方法，该方法在处理订单创建时涉及到事务的处理和分布式锁的使用，以确保订单创建的原子性和一致性。
     *
     * @param para    包含订单创建所需参数的字符串。
     * @param request HTTP请求对象，可能包含额外的请求信息。
     * @return 返回订单创建的结果，成功或失败。
     * @throws TransactionException 如果交易过程中出现异常，会抛出此异常。
     */
    @Override
    public ResponseResult createOrders(String para, HttpServletRequest request) throws TransactionException {
        ResponseResult result = appCreateOrders(para, request);
        // 如果订单创建失败，则回滚事务。
        if (result.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            GlobalTransactionContext.reload(RootContext.getXID()).rollback();
            throw new DefinedException(result);
        }
        return result;
    }

    public ResponseResult appCreateOrders(String para, HttpServletRequest request) {
        //解密请求参数（para 本身就是密文字符串，必须走 desEncryptRaw：
        // desEncrypt(Object) 会再 toJSONString 包一层双引号，导致 Base64 解码失败 → 下单 403）
        para = AesEncryptUtil.desEncryptRaw(para);
        LOG.info("para---{}", para);
        List<String> orderList = new ArrayList<>();
        Long orderCancelTime = null;
        Integer orderCancelMinute = null;
        Integer goodsPlusDis = 0;
        String orderId = null;
        //判断商品是否是 虚拟商品
        Map<String, Object> respData = new HashMap<>();
        try {
            CommonParameterVO cp = JSON.parseObject(para, new TypeReference<>() {
            });
            //判断商品来源,并判断区域是否有货
            ResponseResult checkSkuStockResult = checkSkuStock(cp);
            if (checkSkuStockResult.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
                return checkSkuStockResult;
            }
            //按照商品来源拆单
            para = parseOrderDataByCatId(para);
            cp = JSON.parseObject(para, new TypeReference<>() {
            });
            //计算怡亚通商品运费
            Map<String, String> ShippingFeeMap = checkShippingFee(cp);
            List<StoreInformationVO> storeInformationList = cp.getStoreInformationList();
            Map<Integer, Integer> goodsFlagMap = new HashMap<>();

            for (StoreInformationVO storeInformation : storeInformationList) {
                // 获取购物车的店铺id和商品信息
                List<GoodsInformationVO> goodsList = storeInformation.getGoodsBaseList();
                for (GoodsInformationVO goodsInformation : goodsList) {
                    goodsFlagMap.put(goodsInformation.getGoodsId(), goodsInformation.getType());
                }

            }
            //超时时间：10秒，
            UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(cp.getToken());
            boolean isCombination = !StrUtil.isEmpty(cp.getCombinationId());
            CombinationVO combinationVO = new CombinationVO();
            if (isCombination) {
                combinationVO = activityServiceClient.queryCombinationInfoById(cp.getCombinationId());
            }
            BigDecimal usedamout = new BigDecimal(0);
            //查询用户是否开通plus会员 并且是否有折扣权益
            ResponseResult selectplusmember = userServiceClient.queryUserPlusInfo(ui.getUserId());
            //plus会员剩余可用额度
            BigDecimal plusResidualLimit = null;
            if (selectplusmember.getCode() == 1) {
                Map<String, Object> userPlusInfo = (Map<String, Object>) selectplusmember.getData();
                //剩余可用额度
                plusResidualLimit = new BigDecimal(userPlusInfo.get("residualLimit").toString());
                usedamout = plusResidualLimit;
            }

            /**
             * 订单信息
             */
            CreateOrderDTO dto = getData(ui, cp);
            dto.setCustomerTypeName(ui.getCustomerTypeName());
            dto.setCustomerType(ui.getCustomerType());
            ThirdShopVO thirdShopVO = ui.getThirdShopVO();
            if (BeanUtil.isNotEmpty(thirdShopVO)) {
                dto.setOutShopId(thirdShopVO.getId());
            }
            //查询商品信息 售卖区域 运费信息 参加活动的活动信息等
            ResponseResult rr = merchantServiceClient.queryShipping(para);
            LOG.info("查询商品信息 售卖区域 运费信息 参加活动的活动信息等{}", rr);
            if (rr.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
                return rr;
            } else {
                // 与 calculatePrice 同一口径：Feign 返回的 data 多为 List<Map>，
                // 直接 toString() 得到的是 Map 的 Java 字面量（{shopId=1, ...}）而不是 JSON，
                // fastjson 会在 `=` 处报 illegal input。必须先 toJSONString 再解析。
                List<WapShopShippingQueryResultVO> wapList = JSON.parseObject(
                        JSON.toJSONString(rr.getData()), new TypeReference<>() {
                        });
                // 获取计算价格
                rr = calculatePrice(para);
                if (rr.getCode() == 2) {
                    return rr;
                }
                ShopParaVO detailedPrice = (ShopParaVO) rr.getData();

                //价格信息
                PriceDTO priceDTO = getPriceData(detailedPrice);
                if (null != cp.getIntegralGoods() && cp.getIntegralGoods() == 0) {
                    priceDTO.setIntegralGoods(0);
                    //根据用户id查询用户积分
                    Integer userIntegral = userServiceClient.getMemberIntegralByUserId(ui.getUserId());
                    // 查询用户冻结积分
                    Integer frozenIntegra = userServiceClient.getMemberFrozenIntegraIntegralByUserId(ui.getUserId());
                    userIntegral = userIntegral - frozenIntegra;
                    if (userIntegral < 0) {
                        userIntegral = 0;
                    }
                    if (null != userIntegral && priceDTO.getTotalIntegral() > userIntegral) {
                        return ResponseResult.fail(2, "你的积分不足" + priceDTO.getTotalIntegral());
                    }
                }
                List<Integer> goodsIdList = new ArrayList<>();
                // 存放ConsumeTrade参数
                ConsumeTradeParaDTO ctp = new ConsumeTradeParaDTO();
                // 存放ConsumeRecord参数
                ConsumeRecordParaDTO crp = new ConsumeRecordParaDTO();
                // 存放卖家信息
                ConsumeRecordByBusinessDTO crb = new ConsumeRecordByBusinessDTO();

                if (priceDTO.getRedPacketStatus() != null && priceDTO.getRedPacketStatus() == 2) {
                    //红包已使用，请重新选择！
                    return ResponseResult.fail(BizCodeEnume.RED_ENVELOPE_USED);
                }
                String paymentNumber = "U" + DateUtils.getTimeStamp() + DateUtils.randomLengthByTwo();
                List<OrderCommissionVO> orderCommissionList = new ArrayList<>();
                Integer sizeA = null;
                Integer sizeB = null;
                Integer sizeC = null;
                //详情信息
                SubclassDTO subclassDTO = new SubclassDTO();
                for (int i = 0; i < wapList.size(); i++) {
                    ShippingListVO shippingListDTO = detailedPrice.getShippingList().get(i);
                    Integer shopId = wapList.get(i).getShopId();
                    sizeA = wapList.size();
                    String shopIdByStr = shopId.toString();
                    // 是否是京东商品
                    Boolean jingDongGoods = false;
                    if (ui.getShopId() != null && ui.getShopId().equals(shopId.toString())) {
                        //请勿购买自家商品！
                        return ResponseResult.fail(BizCodeEnume.DO_NOT_PURCHASE_YOUR_OWN_PRODUCTS);
                    }
                    StoreInformationVO storeInfoList = cp.getStoreInformationList().get(i);
                    subclassDTO = getSubData(detailedPrice.getShippingList().get(i), storeInfoList, ui, paymentNumber);
                    if (subclassDTO.getVoucherStatus() != null && subclassDTO.getVoucherStatus() == 2) {
                        //优惠券已使用，请重新选择！
                        return ResponseResult.fail(BizCodeEnume.COUPON_USED);
                    }
                    orderId = subclassDTO.getOrderId();
                    HashMap<Object, Object> hashMap = new HashMap<>();
                    // 店铺所在地
                    Integer districtId = wapList.get(i).getDistrictId();
                    //此处增加新开票功能
                    InvoiceEntityInfoVO invoiceInfo = storeInfoList.getInvoiceInfo();
                    if (null != invoiceInfo) {
                        invoiceInfo.setOrderId(subclassDTO.getOrderId());
                        invoiceInfo.setUserId(ui.getUserId());
                        invoiceInfo.setInvoiceStatus(1);
                        orderInvoiceInfoMapper.addInvoiceInfo(invoiceInfo);
                        subclassDTO.setInvoiceStatus(invoiceInfo.getInvoiceStatus());
                        subclassDTO.setInvoiceId(invoiceInfo.getInvoiceId());
                    }
                    if (!isCombination) {
                        // 红包店铺拆分
                        if (priceDTO.getRedpacketId() != null && subclassDTO.getRedpacketTType() == 4) {
                            // 使用的红包是店铺红包
                            subclassDTO.setRedPacketSumMoney(priceDTO.getRedPacketPrice());
                        } else if (priceDTO.getRedpacketId() != null && (subclassDTO.getRedpacketTType() == 3 || subclassDTO.getRedpacketTType() == 5)
                                && subclassDTO.getItemShopId().equals(shopIdByStr)) {
                            // 使用的红包是多品红包
                            if (i == wapList.size() - 1) {
                                subclassDTO.setRedPacketSumMoney(priceDTO.getRedPacketPrice().subtract(priceDTO.getRedPacketSplitPrice()));
                            } else {
                                // 单个店铺参加活动价格
                                BigDecimal itemShopPrice = detailedPrice.getShippingList().get(i).getItemShopPrice();
                                BigDecimal allredPacketPrice = detailedPrice.getAllredPacketPrice();
                                subclassDTO.setRedPacketSumMoney(itemShopPrice.divide(allredPacketPrice, 2, RoundingMode.HALF_UP)
                                        .multiply(priceDTO.getRedPacketPrice()).setScale(2, RoundingMode.HALF_UP));
                                priceDTO.setRedPacketSplitPrice(priceDTO.getRedPacketSplitPrice().add(subclassDTO.getRedPacketSumMoney()));
                            }
                        } else {
                            if (priceDTO.getRedpacketId() != null && (subclassDTO.getRedpacketTType() == 1 || subclassDTO.getRedpacketTType() == 2
                                    || subclassDTO.getRedpacketTType() == 6 || subclassDTO.getRedpacketTType() == 7)) {
                                // 使用的红包是普通红包
                                // 红包比例计算拆分
                                if (i == wapList.size() - 1 && "1".equals(subclassDTO.getShopLimit())) {
                                    // 计算最后一家店铺红包金额
                                    BigDecimal lastRedPacketPrice = priceDTO.getRedPacketPrice().subtract(priceDTO.getOrderRptPrice());
                                    subclassDTO.setRedPacketSumMoney(lastRedPacketPrice);
                                    priceDTO.setOrderRptPrice(lastRedPacketPrice);
                                } else {
                                    // 单个店铺总价
                                    BigDecimal shopPrice = detailedPrice.getShippingList().get(i).getShopPrice();
                                    BigDecimal allShopPrice = detailedPrice.getAllShopTotalPrice();
                                    subclassDTO.setRedPacketSumMoney(shopPrice.divide(allShopPrice, 2, RoundingMode.HALF_UP)
                                            .multiply(priceDTO.getRedPacketPrice()).setScale(2, RoundingMode.HALF_UP));
                                    priceDTO.setOrderRptPrice(priceDTO.getOrderRptPrice().add(subclassDTO.getRedPacketSumMoney()));
                                }
                            }
                        }

                        // 平台优惠券店铺拆分
                        if (priceDTO.getPlatformVoucherCode() != null && subclassDTO.getAllowanceShopId().equals(shopIdByStr)) {
                            if (i == wapList.size() - 1) {
                                // 计算最后一家拆分金额
                                subclassDTO.setPlatformVoucherShopPrice(priceDTO.getPlatformVoucherPrice().subtract(priceDTO.getPlatformVoucherSplitPrice()));
                            } else {
                                // 店铺参加活动价格
                                BigDecimal allowanceShopPrice = detailedPrice.getShippingList().get(i).getAllowanceShopPrice();
                                BigDecimal allowancePrice = detailedPrice.getAllowancePrice();
                                subclassDTO.setPlatformVoucherShopPrice(allowanceShopPrice
                                        .divide(allowancePrice, 2, RoundingMode.HALF_UP).multiply(priceDTO.getPlatformVoucherPrice())
                                        .setScale(2, RoundingMode.HALF_UP));
                                priceDTO.setPlatformVoucherSplitPrice(priceDTO.getPlatformVoucherSplitPrice().add(subclassDTO.getPlatformVoucherShopPrice()));
                            }
                        }
                    }
                    String shopName = wapList.get(i).getShopName();
                    String buyerMessage = wapList.get(i).getBuyerMessage();
                    // 店铺运费
                    BigDecimal shippingFee = wapList.get(i).getShippingFee();
                    String orderGoodsTime = null;
                    boolean flag = false;
                    boolean giftFlag = false;
                    Boolean vkFlag = StringUtils.isNotEmpty(cp.getShareUserId()) && !Objects.equals(cp.getShareUserId(), "0");
                    List<WapShopShippingResultVO> wssrList = wapList.get(i).getGoodsBaseList();
                    if (CollUtil.isEmpty(wssrList)) {
                        return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), "店铺商品运费信息为空，请刷新后重试");
                    }
                    BigDecimal totalDealerCommission = BigDecimal.ZERO;
                    BigDecimal totalSupplyCommission = BigDecimal.ZERO;
                    List<String> voucherNoList = new ArrayList<>();
                    if (priceDTO.getVoucherIdList() != null) {
                        voucherNoList = Arrays.asList(priceDTO.getVoucherIdList().get(i).split("-"));
                    }
                    List<String> voucherIdList = new ArrayList<>(voucherNoList);
                    int index = 0;
                    for (int n = 0; n < wssrList.size(); n++) {
                        // 运费同一模板循环
                        sizeB = wssrList.size();
                        List<WapShippingQueryResultVO> wsqrList = wssrList.get(n).getGoodsBaseList();
                        if (CollUtil.isEmpty(wsqrList)) {
                            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), "包裹商品信息为空，请刷新后重试");
                        }
                        for (int j = 0; j < wsqrList.size(); j++) {
                            // 供应商待发货订单
                            if (priceDTO.getDropShipping() == 2) {
                                // 查询商品是否是 代发商品
                                Integer dealerGoodsFlagNum = orderBaseMapper.queryDealerGoodsFlagNum(wsqrList.get(j).getGoodsId());
                                if (0 < dealerGoodsFlagNum) {
                                    priceDTO.setDropShipping(1);
                                }
                            }
                            WapShippingQueryResultVO resultVO = wsqrList.get(j);
                            if (resultVO.getDealerGoodsPrice() == null) {
                                resultVO.setDealerGoodsPrice(BigDecimal.ZERO);
                            }
                            subclassDTO.setSupplyShopId(wsqrList.get(j).getSupplyShopId());
                            subclassDTO.setSupplyShopName(wsqrList.get(j).getSupplyShopName());
                            subclassDTO.setOrderSupplyPrice(subclassDTO.getOrderSupplyPrice().add(wsqrList.get(j).getDealerGoodsPrice().multiply(new BigDecimal(wsqrList.get(j).getGoodsNum()))));
                            if (1 == wssrList.size()) {
                                if (1 == wsqrList.size()) {
                                    flag = true;
                                }
                            }
                            sizeC = wsqrList.size();
                            // 商品循环
                            //商品总邮费
                            BigDecimal totalGoodsShippingFee = wsqrList.get(j).getTotalGoodsShippingFee();
                            if (totalGoodsShippingFee == null) {
                                totalGoodsShippingFee = BigDecimal.ZERO;
                            }
                            Integer activityStatus = wsqrList.get(j).getActivityStatus();
                            ActivityGoodsDetailDTO agd = wsqrList.get(j).getActivityGoodsDetail();
                            // 记录每次红包拆分金额
                            BigDecimal couponSumMoney = new BigDecimal(0);
                            //活动类型 0 普通点单 2 秒杀 3 限时折扣 4拼团 5 满减赠 6 满减折 7固定组合 8搭配组合
                            Integer promotionId = 0;
                            //活动id
                            Integer goodsPromotionId = 0;
                            Integer goodsId = null;
                            Integer commonId = null;
                            String goodsName = null;
                            // 获取满减优惠金额
                            BigDecimal manjianDiscount = BigDecimal.ZERO;
                            // 获取满折优惠金额
                            BigDecimal manzheDiscount = BigDecimal.ZERO;
                            Integer catId = null;
                            String goodsSpec = null;
                            String goodsImage = null;
                            Integer goodsNum = null;
                            BigDecimal goodsAmount = new BigDecimal(0);
                            BigDecimal goodsPrice = new BigDecimal(0);
                            BigDecimal goodsShippingFee = new BigDecimal(0);
                            BigDecimal activityUnitPrice = BigDecimal.ZERO;
                            // 判断活动状态
                            //秒杀
                            if (activityStatus == 2 && !isCombination) {
                                // 规格值Id
                                goodsId = wsqrList.get(j).getGoodsId();
                                // 存放goodsId
                                goodsIdList.add(goodsId);
                                // commonId
                                commonId = wsqrList.get(j).getCommonId();
                                // 商品名
                                goodsName = wsqrList.get(j).getGoodsName();
                                // 上级类目Id
                                catId = wsqrList.get(j).getCatId();
                                // 规格值
                                goodsSpec = wsqrList.get(j).getGoodsSpec();
                                // 商品图
                                goodsImage = wsqrList.get(j).getGoodsImage();
                                // 购买数量
                                goodsNum = wsqrList.get(j).getGoodsNum();
                                // 商品数量转换类型
                                goodsAmount = new BigDecimal(goodsNum);
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("token", cp.getToken());
                                map.put("goodsId", wsqrList.get(j).getGoodsId());
                                ResponseResult checkLimit = activityServiceClient.seckillCheckLimit(map);
                                // 商品价格
                                if (checkLimit.getCode() == 1) {
                                    goodsPrice = agd.getActivityPrice();
                                } else if (checkLimit.getCode() == 2) {
                                    goodsPrice = wsqrList.get(j).getGoodsPrice();
                                } else {
                                    return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
                                }
                                activityUnitPrice = goodsPrice;
                                // 单件商品运费价格
                                goodsShippingFee = wsqrList.get(j).getGoodsShippingFee();
                                // 付款价格(不含运费)
                                priceDTO.setOrderGoodsAmount(goodsPrice.multiply(goodsAmount));
                                subclassDTO.setGroupbuyStatus(0);
                                subclassDTO.setOrderPromotionId(2);
                                promotionId = 2;
                                if (null != agd.getActivityStartStatus() && agd.getActivityStartStatus() == 1) {
                                    goodsPromotionId = agd.getSeckillGoods().getSeckillId();
                                }
                            //限时折扣
                            } else if (activityStatus == 3 && !isCombination) {
                                // 规格值Id
                                goodsId = wsqrList.get(j).getGoodsId();
                                // 存放goodsId
                                goodsIdList.add(goodsId);
                                // commonId
                                commonId = wsqrList.get(j).getCommonId();
                                // 商品名
                                goodsName = wsqrList.get(j).getGoodsName();
                                // 上级类目Id
                                catId = wsqrList.get(j).getCatId();
                                // 规格值
                                goodsSpec = wsqrList.get(j).getGoodsSpec();
                                // 商品图
                                goodsImage = wsqrList.get(j).getGoodsImage();
                                // 购买数量
                                goodsNum = wsqrList.get(j).getGoodsNum();
                                // 商品数量转换类型
                                goodsAmount = new BigDecimal(goodsNum);
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("token", cp.getToken());
                                map.put("goodsId", wsqrList.get(j).getGoodsId());
                                map.put("goodsNum", goodsNum);
                                ResponseResult checkLimit = activityServiceClient.discountCheckLimit(map);
                                // 商品价格
                                if (checkLimit.getCode() == 1) {
                                    goodsPrice = agd.getActivityPrice();
                                } else if (checkLimit.getCode() == 2) {
                                    return checkLimit;
                                    // goodsPrice = wsqrList.get(j).getGoodsPrice();
                                } else {
                                    return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
                                }
                                activityUnitPrice = goodsPrice;
                                // 单件商品运费价格
                                goodsShippingFee = wsqrList.get(j).getGoodsShippingFee();
                                // 付款价格(不含运费)
                                priceDTO.setOrderGoodsAmount(goodsPrice.multiply(goodsAmount));
                                subclassDTO.setGroupbuyStatus(0);
                                subclassDTO.setOrderPromotionId(3);
                                promotionId = 3;
                                if (null != agd.getActivityStartStatus() && agd.getActivityStartStatus() == 1) {
                                    goodsPromotionId = agd.getDiscountGoods().getDiscountId();
                                }
                            //拼团
                            } else if (activityStatus == 4 && cp.getPlatForm() != null && cp.getPlatForm() != 1 && !isCombination) {
                                // 规格值Id
                                goodsId = wsqrList.get(j).getGoodsId();
                                subclassDTO.setGroupBookingGoodsId(goodsId);
                                // 存放goodsId
                                goodsIdList.add(goodsId);
                                // commonId
                                commonId = wsqrList.get(j).getCommonId();
                                subclassDTO.setPinCommonId(commonId);
                                // 商品名
                                goodsName = wsqrList.get(j).getGoodsName();
                                // 上级类目Id
                                catId = wsqrList.get(j).getCatId();
                                // 规格值
                                goodsSpec = wsqrList.get(j).getGoodsSpec();
                                // 商品图
                                goodsImage = wsqrList.get(j).getGoodsImage();
                                // 购买数量
                                goodsNum = wsqrList.get(j).getGoodsNum();
                                // 商品数量转换类型
                                goodsAmount = new BigDecimal(goodsNum);
                                subclassDTO.setType(agd.getType());
                                subclassDTO.setMarkId(agd.getMarkId());
                                if (subclassDTO.getType() == 1) {
                                    // 单独购买，不参与拼团
                                    subclassDTO.setGroupbuyStatus(0);
                                    subclassDTO.setOrderPromotionId(0);
                                    goodsPrice = agd.getPriceOne();
                                } else {
                                    // 商品价格
                                    goodsPrice = agd.getPintuanPrice();
                                    // goods表活动值
                                    promotionId = 4;
                                    // 参与拼团
                                    if (subclassDTO.getMarkId() == null || subclassDTO.getMarkId().isEmpty()) {
                                        // 判断是否是新开团
                                        subclassDTO.setOrderBuyId((DateUtils.timeToStamp()).toString()
                                                + DateUtils.randomLengthByTwo());
                                        subclassDTO.setMarkId(subclassDTO.getOrderBuyId());
                                        subclassDTO.setIsMark(1);
                                    } else {
                                        // 参加已有拼团
                                        subclassDTO.setIsMark(2);
                                        subclassDTO.setOrderBuyId(subclassDTO.getMarkId());
                                    }
                                    subclassDTO.setGroupbuyStatus(1);
                                    subclassDTO.setOrderPromotionId(4);
                                }
                                activityUnitPrice = goodsPrice;
                                // 单件商品运费价格
                                goodsShippingFee = wsqrList.get(j).getGoodsShippingFee();
                                // 付款价格(不含运费)
                                priceDTO.setOrderGoodsAmount(goodsPrice.multiply(goodsAmount));
                            //满减赠折活动
                            } else if (activityStatus == 5 && !isCombination) {
                                // 规格值Id
                                goodsId = wsqrList.get(j).getGoodsId();
                                // 存放goodsId
                                goodsIdList.add(goodsId);
                                // commonId
                                commonId = wsqrList.get(j).getCommonId();
                                // 商品名
                                goodsName = wsqrList.get(j).getGoodsName();
                                // 上级类目Id
                                catId = wsqrList.get(j).getCatId();
                                // 规格值
                                goodsSpec = wsqrList.get(j).getGoodsSpec();
                                // 商品图
                                goodsImage = wsqrList.get(j).getGoodsImage();
                                // 购买数量
                                goodsNum = wsqrList.get(j).getGoodsNum();
                                // 商品数量转换类型
                                goodsAmount = new BigDecimal(goodsNum);
                                goodsPrice = wsqrList.get(j).getGoodsPrice();
                                // 商品价格
                                ;
                                // 单件商品运费价格
                                goodsShippingFee = wsqrList.get(j).getGoodsShippingFee();
                                // 付款价格(不含运费)
                                priceDTO.setOrderGoodsAmount(goodsPrice.multiply(goodsAmount));
                                subclassDTO.setGroupbuyStatus(0);
                                subclassDTO.setOrderPromotionId(5);
                                promotionId = 5;
                                for (ShippingListVO sl : priceDTO.getShippingLists()) {
                                    if (sl.getShopId().equals(shopId)) {
                                        // 满减优惠金额
                                        manjianDiscount = sl.getShopManjianDiscount();
                                        // 满折优惠金额
                                        manzheDiscount = sl.getShopManzheDiscount();
                                        priceDTO.setManjianDiscountBase(priceDTO.getManzheDiscountBase().add(manjianDiscount));
                                        priceDTO.setManzheDiscountBase(priceDTO.getManzheDiscountBase().add(manzheDiscount));
                                    }
                                }
                            } else {
                                // 规格值Id
                                goodsId = wsqrList.get(j).getGoodsId();
                                // 存放goodsId
                                goodsIdList.add(goodsId);
                                // commonId
                                commonId = wsqrList.get(j).getCommonId();
                                // 商品名
                                goodsName = wsqrList.get(j).getGoodsName();
                                // 上级类目Id
                                catId = wsqrList.get(j).getCatId();
                                // 规格值
                                goodsSpec = wsqrList.get(j).getGoodsSpec();
                                // 商品图
                                goodsImage = wsqrList.get(j).getGoodsImage();
                                // 购买数量
                                goodsNum = wsqrList.get(j).getGoodsNum();
                                // 商品数量转换类型
                                goodsAmount = new BigDecimal(goodsNum);
                                // 商品价格
                                goodsPrice = wsqrList.get(j).getGoodsPrice();
                                // 插件价：砍价成交价优先，其次数量阶梯价（未命中/异常一律保持原价）
                                // 与 calculatePrice 同一口径，否则会出现「详情页 179.10、成交 199」的价差。
                                Long pluginUserId = null;
                                try {
                                    pluginUserId = Long.valueOf(ui.getUserId());
                                } catch (Exception ignored) {
                                    // userId 非数字时按未登录处理，插件价不生效
                                }
                                String pluginGoodsId = String.valueOf(goodsId);
                                BigDecimal pluginUnitPrice = bargainPricePricingHelper.unitPrice(
                                        pluginUserId, pluginGoodsId, goodsPrice);
                                if (pluginUnitPrice == null) {
                                    pluginUnitPrice = tierPricePricingHelper.unitPrice(pluginGoodsId, goodsNum, goodsPrice);
                                }
                                if (pluginUnitPrice != null) {
                                    goodsPrice = pluginUnitPrice;
                                }
                                // 单件商品运费价格
                                goodsShippingFee = wsqrList.get(j).getGoodsShippingFee();
                                // 付款价格(不含运费)
                                priceDTO.setOrderGoodsAmount(goodsPrice.multiply(goodsAmount));
                                subclassDTO.setGroupbuyStatus(0);
                                subclassDTO.setOrderPromotionId(0);
                            }
                            //查询积分商品所需 的积分
                            Integer isLimit = orderBaseMapper.getIntegralGoodsIsLimit(goodsId);
                            if (cp.getIntegralGoods() != null && null != isLimit && isLimit > 0) {
                                Integer buyCount = orderGoodsMapper.queryBuyCountByGoodsId(goodsId, ui.getUserId());
                                buyCount = buyCount == null ? 0 : buyCount;
                                if (buyCount + goodsNum > isLimit) {
                                    return ResponseResult.fail(BizCodeEnume.GOODS_EXCEEDS_THE_MAXIMUM_PURCHASE_LIMIT);
                                }
                            }
                            //限时秒杀
                            if (activityStatus == 2 && !isCombination) {
                                Integer buyType = goodsFlagMap.get(goodsId);
                                //若为限时折扣商品且想要用优惠价购买，则需要判断限购条件
                                if (null != buyType && 0 == buyType) {
                                    CheckSeckillLimitResultVO data = activityServiceClient.querySeckillInfoByGoodsId(goodsId);
                                    Integer count = null;
                                    if (null != data) {
                                        Integer goodsUpperLimit = data.getGoodsUpperLimit();
                                        if (null == orderCancelMinute) {
                                            orderCancelMinute = data.getCancelTime();
                                        } else if (orderCancelMinute > data.getCancelTime()) {
                                            orderCancelMinute = data.getCancelTime();
                                        }
                                        if (0 != goodsUpperLimit) {
                                            Map<String, Object> param = new HashMap<>();
                                            param.put("goodsStartTime", data.getGoodsStartTime());
                                            param.put("goodsEndTime", data.getGoodsEndTime());
                                            param.put("goodsId", goodsId);
                                            param.put("commonId", commonId);
                                            param.put("seckillId", data.getSeckillId());
                                            param.put("userId", ui.getUserId());
                                            count = orderGoodsMapper.queryOrderGoodsCount(param);
                                            count = count == null ? 0 : count;
                                            count = count + goodsAmount.intValue();
                                            if (count > goodsUpperLimit) {
                                                return ResponseResult.fail(0, "商品:「" + data.getGoodsName() + "」此次活动内只能秒杀下单" + goodsUpperLimit + "件");
                                            }
                                        }

                                    }
                                }
                            }

                            //限时折扣
                            if (activityStatus == 3 && !isCombination) {
                                Integer buyType = goodsFlagMap.get(goodsId);
                                //若为限时折扣商品且想要用优惠价购买，则需要判断限购条件
                                if (null != buyType && 0 == buyType) {
                                    CheckDiscountLimitResultVO data = activityServiceClient.queryDiscountInfoByGoodsId(goodsId);
                                    Integer count = null;
                                    if (null != data) {
                                        Integer goodsUpperLimit = data.getGoodsUpperLimit();
                                        Integer discountUpperTime = data.getDiscountUpperTime();
                                        if (0 != goodsUpperLimit && 0 != discountUpperTime) {
                                            Calendar cal = Calendar.getInstance();
                                            Map<String, Object> param = new HashMap<>();
                                            Date fdate = new Date();
                                            cal.setTime(fdate);
                                            cal.add(Calendar.DATE, -discountUpperTime);
                                            fdate = cal.getTime();
                                            param.put("fDate", fdate);
                                            param.put("goodsId", goodsId);
                                            param.put("commonId", commonId);
                                            param.put("discountId", data.getDiscountId());
                                            param.put("userId", ui.getUserId());
                                            count = orderGoodsMapper.queryOrderGoodsCountByDiscount(param);
                                            count = count == null ? 0 : count;
                                            count = count + goodsAmount.intValue();
                                            if (count > goodsUpperLimit) {
                                                return ResponseResult.fail(0, "商品:「" + data.getGoodsName() + "」" + discountUpperTime + "天内只能优惠价下单" + goodsUpperLimit + "件");
                                            }
                                        }
                                    }
                                }
                            }
                            //如果是门店商品 门店用户不能购买自己的商品门店同步是商品
                            ResponseResult OrderIsShopTakeResponse = checkOrderIsShopTake(cp, ui, wsqrList.get(j).getCommonId());
                            if (OrderIsShopTakeResponse.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
                                return OrderIsShopTakeResponse;
                            }

                            // 判断markid是否为null
                            // agd（活动商品详情）只在拼团/活动分支里赋值，普通商品此处必为 null，
                            // 不判空会直接 NPE 把整单打回「操作失败」（本次实测踩到）。
                            if (agd != null && agd.getMarkId() != null && subclassDTO.getMarkId() != null
                                    && !subclassDTO.getMarkId().isEmpty()) {
                                ResponseResult resultResp = activityServiceClient.querySurplusNum(subclassDTO.getMarkId());
                                if (resultResp.getCode() == 1) {
                                    Integer numberRemaining = (Integer) resultResp.getData();
                                    if (numberRemaining == 0) {
                                        return ResponseResult.fail(0, "当前拼团人数已满，请重新选择！");
                                    }
                                }
                            }

                            // 订单状态
                            Integer orderGoodsStatus = 1;
                            // 评价状态
                            Integer orderGoodsEvaluationStatus = 0;
                            // 退款状态
                            Integer goodsReturnStatus = 0;
                            // 退货状态
                            Integer goodsRefundStatus = 0;
                            // 下单时间
                            orderGoodsTime = DateUtils.getNowDateToString();
                            // 是否发货
                            Integer orderGoodsIdDeliver = 0;
                            // goods表红包抵扣金额
                            BigDecimal goodsRtpPrice = BigDecimal.ZERO;
                            // 判断是否是京东商品
                            //查询商品来源
                            Integer goodsSource = orderBaseMapper.queryGoodsSource(goodsId);
                            if (null != goodsSource && CommonType.SupplierSource.JING_DONG.getCode().equals(goodsSource)) {
                                jingDongGoods = true;
                            }
                            Integer voucherId = 0;
                            if (!isCombination) {
                                if (null != priceDTO.getVoucherIdList() && !priceDTO.getVoucherIdList().isEmpty()) {
                                    // 红包&优惠券都使用
                                    // 优惠券比例计算拆分
                                    if (flag) {
                                        // 单个店铺总价
                                        BigDecimal shopPrice = detailedPrice.getShippingList().get(i).getShopPrice();
                                        // 计算最后一件商品优惠券拆分金额
                                        // 存入最后一个商品的优惠券抵扣金额
                                        // 一件商品不拆分
                                        couponSumMoney = subclassDTO.getCouponPrice();
                                    } else {
                                        // 获取代金券Id,并封装Map,在向malls请求数据
                                        Map<String, Object> couponIdMap = new HashMap<>();
                                        if (voucherNoList.size() > 1) {
                                            for (String couponId : voucherIdList) {
                                                couponIdMap.put("voucherId", couponId);
                                                // 创建接收bean
                                                VoucherBaseVO voucher = activityServiceClient.getVoucherDetail(couponIdMap);
                                                if (voucher.getAvaliableGoods().contains(goodsId) &&
                                                        BigDecimal.valueOf(voucher.getVoucherLimit()).compareTo(priceDTO.getOrderGoodsAmount()) <= 0) {
                                                    // 获取优惠券价格
                                                    couponSumMoney = voucher.getVoucherPrice();
                                                    voucherId = voucher.getVoucherId();
                                                    voucherIdList.remove(voucher.getVoucherId().toString());
                                                    break;
                                                }
                                            }
                                        } else if (voucherNoList.size() == 1) {
                                            if (!"0".equals(voucherNoList.get(0))) {
                                                couponIdMap.put("voucherId", voucherNoList.get(0));
                                                VoucherBaseVO voucher = activityServiceClient.getVoucherDetail(couponIdMap);
                                                if (voucher.getIsShopVoucher() == 2) {
                                                    if (voucher.getAvaliableGoods().contains(goodsId) &&
                                                            BigDecimal.valueOf(voucher.getVoucherLimit()).compareTo(priceDTO.getOrderGoodsAmount()) <= 0) {
                                                        // 获取优惠券价格
                                                        couponSumMoney = voucher.getVoucherPrice();
                                                        voucherId = voucher.getVoucherId();
                                                    }
                                                } else {
                                                    if (sizeA == i + 1 && sizeB == n + 1 && sizeC == j + 1) {
                                                        // 计算最后一件商品优惠券拆分金额
                                                        couponSumMoney = subclassDTO.getCouponPrice().subtract(subclassDTO.getOrderCptPrice());
                                                    } else {
                                                        // 单个店铺总价
                                                        BigDecimal shopPrice = detailedPrice.getShippingList().get(i).getShopPrice();
                                                        couponSumMoney = priceDTO.getOrderGoodsAmount()
                                                                .multiply(subclassDTO.getCouponPrice())
                                                                .divide(shopPrice, 2, RoundingMode.HALF_UP)
                                                                .setScale(2, RoundingMode.HALF_UP);
                                                        // 累加金额
                                                        subclassDTO.setOrderCptPrice(subclassDTO.getOrderCptPrice().add(couponSumMoney));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if (priceDTO.getRedpacketId() != null) {
                                    // 判断使用红包类型是否是店铺红包
                                    if ((subclassDTO.getRedpacketTType() == 3 || subclassDTO.getRedpacketTType() == 5)
                                            && subclassDTO.getItemShopId().equals(shopIdByStr)) {
                                        if (subclassDTO.getGoodsQuantity() == 1) {
                                            // 判断商品个数
                                            goodsRtpPrice = subclassDTO.getRedPacketSumMoney();
                                            // 优惠活动中平台承担的金额
                                            subclassDTO.setOrderGoodsPlatformBear(subclassDTO.getRedPacketSumMoney());
                                        } else {
                                            if (sizeA == i + 1 && sizeB == n + 1 && sizeC == j + 1) {
                                                goodsRtpPrice = subclassDTO.getRedPacketSumMoney().subtract(subclassDTO.getOrderGoodsRptPrice());
                                                subclassDTO.setOrderGoodsPlatformBear(subclassDTO.getRedPacketSumMoney().subtract(subclassDTO.getOrderGoodsAddPlatformBear()));
                                            } else {
                                                BigDecimal itemShopPrice = detailedPrice.getShippingList().get(i)
                                                        .getItemShopPrice();
                                                subclassDTO.setOrderGoodsPlatformBear(goodsPrice.multiply(goodsAmount)
                                                        .multiply(subclassDTO.getRedPacketSumMoney()
                                                                .divide(itemShopPrice, 2, RoundingMode.HALF_UP)
                                                                // 拆分比例价格);
                                                                .setScale(2, RoundingMode.HALF_UP)));
                                                // 累加价格
                                                subclassDTO.setOrderGoodsAddPlatformBear(subclassDTO.getOrderGoodsAddPlatformBear().add(subclassDTO.getOrderGoodsPlatformBear()));
                                                // 赋值红包价格
                                                goodsRtpPrice = subclassDTO.getOrderGoodsPlatformBear();
                                                // 累加红包拆分价格
                                                subclassDTO.setOrderGoodsRptPrice(subclassDTO.getOrderGoodsRptPrice().add(goodsRtpPrice));

                                            }
                                        }
                                    } else if (subclassDTO.getRedpacketTType() == 1 || subclassDTO.getRedpacketTType() == 2
                                            || subclassDTO.getRedpacketTType() == 6 || subclassDTO.getRedpacketTType() == 7) {
                                        // 普通红包比例拆分
                                        if (flag) {
                                            // 1件商品
                                            goodsRtpPrice = subclassDTO.getRedPacketSumMoney();
                                            subclassDTO.setOrderGoodsPlatformBear(subclassDTO.getRedPacketSumMoney());
                                        } else {
                                            // 多件商品,判断最后一件
                                            if (sizeA == i + 1 && sizeB == n + 1 && sizeC == j + 1) {
                                                goodsRtpPrice = subclassDTO.getRedPacketSumMoney().subtract(subclassDTO.getOrderGoodsRptPrice());
                                                subclassDTO.setOrderGoodsPlatformBear(subclassDTO.getRedPacketSumMoney().subtract(subclassDTO.getOrderGoodsAddPlatformBear()));
                                            } else {
                                                // 单个店铺总价
                                                BigDecimal shopPrice = detailedPrice.getShippingList().get(i).getShopPrice();
                                                // 拆分至每家店铺的红包再次拆分至每一个商品
                                                subclassDTO.setOrderGoodsPlatformBear(priceDTO.getOrderGoodsAmount()
                                                        .divide(shopPrice, 2, RoundingMode.HALF_UP)
                                                        .multiply(subclassDTO.getRedPacketSumMoney()).setScale(2, RoundingMode.HALF_UP));
                                                goodsRtpPrice = subclassDTO.getOrderGoodsPlatformBear();
                                                subclassDTO.setOrderGoodsRptPrice(subclassDTO.getOrderGoodsRptPrice().add(goodsRtpPrice));
                                                subclassDTO.setOrderGoodsAddPlatformBear(subclassDTO.getOrderGoodsAddPlatformBear().add(subclassDTO.getOrderGoodsPlatformBear()));
//
                                            }
                                        }
                                    }
                                }
                            }
                            // 优惠券&红包都不使用
                            if (goodsShippingFee == null) {
                                goodsShippingFee = BigDecimal.ZERO;
                            }

                            if (!CollectionUtils.isEmpty(priceDTO.getAvaliableGoods()) && priceDTO.getAvaliableGoods().contains(goodsId)) {
                                if (sizeA == i + 1 && sizeB == n + 1 && sizeC == j + 1) {
                                    subclassDTO.setPlatformVoucherRptPrice(subclassDTO.getPlatformVoucherShopPrice().subtract(subclassDTO.getPlatformVoucherRptAddPrice()));
                                } else {
                                    // 购物津贴拆分至goods
                                    BigDecimal allowanceShopPrice = detailedPrice.getShippingList().get(i)
                                            .getAllowanceShopPrice();
                                    subclassDTO.setPlatformVoucherRptPrice(goodsPrice.multiply(goodsAmount)
                                            .divide(allowanceShopPrice, 2, RoundingMode.HALF_UP)
                                            .multiply(subclassDTO.getPlatformVoucherShopPrice()).setScale(2, RoundingMode.HALF_UP));
                                    subclassDTO.setPlatformVoucherRptAddPrice(subclassDTO.getPlatformVoucherRptAddPrice().add(subclassDTO.getPlatformVoucherRptPrice()));

                                }
                            }

                            if (isCombination && combinationVO.getCombinationType() == 2) {
                                CombinationGoodsInfoVO goodsInfoVO = activityServiceClient.queryCombinationGoodsInfoByIds(combinationVO.getCombinationId(), goodsId);
                                goodsPrice = goodsInfoVO.getMatchingPrices();
                                priceDTO.setOrderGoodsAmount(goodsPrice.multiply(goodsAmount));
                            }
                            BigDecimal price = goodsPrice.multiply(goodsAmount);
                            //商品减去会员折扣好价格  用于计算plus会员价格
                            BigDecimal plusprice = new BigDecimal(0);
                            subclassDTO.setPaymentRealPrice(price.add(goodsShippingFee));
                            // 总优惠金额
                            BigDecimal orderDiscountFee = new BigDecimal(0);
                            Double gradeDiscount = 0.0;
                            //会员折扣
                            if (null != cp.getMemberClassId() && 0 != cp.getMemberClassId()) {
                                gradeDiscount = resolveGradeDiscountDouble(cp.getMemberClassId());

                                if (isCombination && combinationVO.getCombinationType() == 1) {
                                    price = combinationVO.getCombinationPrice().multiply(goodsAmount);
                                    subclassDTO.setGoodsMemberDiscount(price
                                            .subtract(goodsShippingFee).subtract(((price.subtract(goodsShippingFee))
                                                    .multiply(new BigDecimal(gradeDiscount)).setScale(2, RoundingMode.HALF_UP))));
                                } else {
                                    subclassDTO.setGoodsMemberDiscount(subclassDTO.getPaymentRealPrice()
                                            .subtract(goodsShippingFee).subtract(((subclassDTO.getPaymentRealPrice().subtract(goodsShippingFee))
                                                    .multiply(new BigDecimal(gradeDiscount)).setScale(2, RoundingMode.HALF_UP))));
                                }
                                orderDiscountFee = subclassDTO.getGoodsMemberDiscount().add(orderDiscountFee);
                                if (isCombination && combinationVO.getCombinationType() == 1) {
                                    if (index == 0) {
                                        subclassDTO.setBaseMemberDiscount(subclassDTO.getBaseMemberDiscount().add(subclassDTO.getGoodsMemberDiscount()));
                                    }
                                } else {
                                    subclassDTO.setBaseMemberDiscount(subclassDTO.getBaseMemberDiscount().add(subclassDTO.getGoodsMemberDiscount()));
                                }


                                //商品减去会员折扣好价格  用于计算plus会员价格
                                plusprice = subclassDTO.getPaymentRealPrice().subtract(goodsShippingFee).subtract(subclassDTO.getGoodsMemberDiscount());
                                subclassDTO.setPaymentRealPrice(subclassDTO.getPaymentRealPrice().subtract(subclassDTO.getGoodsMemberDiscount()));

                            }

                            //plus会员优惠金额
                            //查询是否为plus商品
                            if (selectplusmember.getCode() == 1) {
                                Integer integer = goodServiceClient.queryGoodsIsPlus(wsqrList.get(j).getCommonId());
                                if (integer == 1) {
                                    priceDTO.setPlusOrder(1);
                                    //plus会员折扣
                                    Map<String, Object> map = (Map<String, Object>) selectplusmember.getData();
                                    BigDecimal data = new BigDecimal(String.valueOf(map.get("discount")));
                                    data = new BigDecimal(1).subtract(data);
                                    //plus商品  会员折扣金额
                                    priceDTO.setPlusgoodsprice(plusprice.multiply(data).setScale(2, RoundingMode.HALF_UP));

                                    //判断可用额度
                                    if (usedamout.compareTo(new BigDecimal(-1)) != 0) {
                                        if (priceDTO.getPlusgoodsprice().compareTo(usedamout) > 0) {
                                            priceDTO.setPlusgoodsprice(usedamout);
                                        }
                                        usedamout = usedamout.subtract(priceDTO.getPlusgoodsprice());
                                    }
                                    //总优惠金额
                                    priceDTO.setPlusgoodstotal(priceDTO.getPlusgoodstotal().add(priceDTO.getPlusgoodsprice()));
                                    //支付金额
                                    subclassDTO.setPaymentRealPrice(subclassDTO.getPaymentRealPrice().subtract(priceDTO.getPlusgoodsprice()).setScale(2, RoundingMode.HALF_UP));
                                    //将plus会员折扣金额加入会员金额
                                    subclassDTO.setBaseMemberDiscount(subclassDTO.getBaseMemberDiscount().add(priceDTO.getPlusgoodsprice()));

                                    goodsPlusDis = 1;

                                    //更新plus信息表已用额度
                                    hashMap.put("userId", ui.getUserId());
                                    hashMap.put("usedAmout", priceDTO.getPlusgoodsprice());
                                    hashMap.put("totalAmount", priceDTO.getPlusgoodsprice());
                                    userServiceClient.updateUserPluslimit(hashMap);
                                }
                            }

                            subclassDTO.setPaymentRealPrice(subclassDTO.getPaymentRealPrice().subtract(couponSumMoney).subtract(subclassDTO.getOrderGoodsPlatformBear())
                                    .subtract(subclassDTO.getPlatformVoucherRptPrice()).setScale(2, RoundingMode.HALF_UP));

                            if (subclassDTO.getPaymentRealPrice().compareTo(new BigDecimal(0)) < 0) {
                                subclassDTO.setPaymentRealPrice(new BigDecimal(0));

                            }

                            //查询分类抽佣比例
                            BigDecimal catCommission = orderGoodsMapper.queryCatCommission(catId);
                            if (null == catCommission) {
                                catCommission = new BigDecimal(0);
                            }
                            //计算 商家抽佣
                            catCommission = catCommission.divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);

                            if (subclassDTO.getCatCommission() == null || subclassDTO.getCatCommission().compareTo(catCommission) > 0) {
                                subclassDTO.setCatCommission(catCommission);
                            }
                            //计算平台抽佣
                            subclassDTO.setOrderGoodsCommission((subclassDTO.getPaymentRealPrice().subtract(goodsShippingFee)).multiply(catCommission)
                                    .setScale(2, RoundingMode.HALF_UP));
                            subclassDTO.setOrderBaseCommission(subclassDTO.getOrderBaseCommission()
                                    .add(subclassDTO.getOrderGoodsCommission())
                                    .setScale(2, RoundingMode.HALF_UP));

                            manjianDiscount = manjianDiscount.setScale(2, RoundingMode.HALF_UP);
                            manzheDiscount = manzheDiscount.setScale(2, RoundingMode.HALF_UP);
                            // 每家店铺商品总价(不包含运费，优惠价格)
                            BigDecimal shopPrice = detailedPrice.getShippingList().get(i).getShopPrice();
                            // 满减活动 + 会员折扣
                            orderDiscountFee = orderDiscountFee.add(manjianDiscount).add(manzheDiscount);
                            subclassDTO.setOrderBasePaymentAmount(shopPrice.add(shippingFee).subtract(priceDTO.getOrderRptPrice()).subtract(orderDiscountFee));

                            if (subclassDTO.getOrderBasePaymentAmount().compareTo(new BigDecimal(0)) < 0) {
                                subclassDTO.setOrderBasePaymentAmount(new BigDecimal(0));
                            }


                            if (null != cp.getVirtualGoods() && 0 == cp.getVirtualGoods()) {
                                Map<String, String> map = new HashMap<>();
                                map.put("goodsId", String.valueOf(goodsId));
                                ResponseResult resp = goodServiceClient.queryVirtualgoods(map);
                                respData = (Map<String, Object>) resp.getData();
                            }

                            // 计算商品经销佣金和供应商佣金
                            double dealer = wsqrList.get(j).getDealerCommission() == null ? 0 : Double.parseDouble(wsqrList.get(j).getDealerCommission()) / 100;
                            double supply = wsqrList.get(j).getSupplyCommission() == null ? 0 : Double.parseDouble(wsqrList.get(j).getSupplyCommission()) / 100;
                            BigDecimal dealerCommission = subclassDTO.getPaymentRealPrice().multiply(new BigDecimal(dealer).setScale(2, RoundingMode.HALF_UP));
                            BigDecimal supplyCommission = subclassDTO.getPaymentRealPrice().multiply(new BigDecimal(supply).setScale(2, RoundingMode.HALF_UP));
                            totalDealerCommission = totalDealerCommission.add(dealerCommission);
                            totalSupplyCommission = totalSupplyCommission.add(supplyCommission);

                            //查询积分商品所需 的积分
                            Integer needIntegral = orderBaseMapper.getGoodsIntegral(goodsId);
                            if (null != needIntegral) {
                                needIntegral = 0;
                            }
                            if (shippingListDTO.getPromotionIdList() != null && !shippingListDTO.getPromotionIdList().isEmpty()) {
                                promotionId = shippingListDTO.getPromotionIdList().get(n);
                            }
                            BigDecimal fullReductionDiscount = null;
                            BigDecimal fullFoldingDiscount = null;
                            BigDecimal paymentRealPrice = subclassDTO.getPaymentRealPrice();
                            if (shippingListDTO.getManjianMap() != null) {
                                fullReductionDiscount = shippingListDTO.getManjianMap().get(goodsId);
                                paymentRealPrice = paymentRealPrice.subtract(fullReductionDiscount == null ? BigDecimal.ZERO : fullReductionDiscount);
                            }
                            if (shippingListDTO.getManjianMap() != null) {
                                fullFoldingDiscount = shippingListDTO.getManzheMap().get(goodsId);
                                paymentRealPrice = paymentRealPrice.subtract(fullFoldingDiscount == null ? BigDecimal.ZERO : fullFoldingDiscount);
                            }
                            if (isCombination) {
                                promotionId = combinationVO.getCombinationType() == 1 ? 7 : 8;
                            }
                            paymentRealPrice = paymentRealPrice.setScale(2, RoundingMode.HALF_UP);
                            //怡亚通商品运费
                            if (null != ShippingFeeMap && null != ShippingFeeMap.get(resultVO.getCommonId().toString())) {
                                // 商品运费
                                goodsShippingFee = new BigDecimal(ShippingFeeMap.get(resultVO.getCommonId().toString()));
                                // 总商品运费
                                totalGoodsShippingFee = new BigDecimal(ShippingFeeMap.get(resultVO.getCommonId().toString()));
                            }
                            // 创建订单(orderGoods)

                            // 计算嘉币、劲豆折扣
                            String discount = resolveGradeDiscount(cp.getMemberClassId());

                            Double pointsRate = resultVO.getPointsRate();


                            BigDecimal cashPrice = resultVO.getCashPrice();

                            subclassDTO.setGoodsMemberDiscount(detailedPrice.getMemberDiscountAmount());

                            boolean isInsertByOrderGoodsSuccess = orderGoodsMapper.insertByOrderGoods(totalGoodsShippingFee, orderId, shopId, shopName, subclassDTO.getSupplyShopId(), subclassDTO.getSupplyShopName(), shopId.equals(subclassDTO.getSupplyShopId()) ? 2 : 1, goodsId, commonId, ui.getUserId(), goodsName, catId, goodsSpec, goodsPrice, goodsNum, goodsImage, goodsShippingFee, priceDTO.getOrderGoodsAmount(), subclassDTO.getOrderGoodsPlatformBear(), paymentRealPrice, orderGoodsStatus, orderGoodsEvaluationStatus, goodsRtpPrice, couponSumMoney, subclassDTO.getPlatformVoucherRptPrice(), goodsReturnStatus, goodsRefundStatus, orderGoodsTime, orderGoodsIdDeliver, promotionId, subclassDTO.getSmallOrder(), needIntegral, dealerCommission, supplyCommission, subclassDTO.getOrderGoodsCommission(), subclassDTO.getGoodsMemberDiscount(), orderDiscountFee, priceDTO.getPlusgoodsprice(), goodsPlusDis, fullReductionDiscount, fullFoldingDiscount, activityUnitPrice, goodsPromotionId, voucherId, resultVO.getVerId(), pointsRate, cashPrice);

                            // 根据 gooid 查询当前商品是否是代发商品
                            // 代发商品查询依赖 goods_base.supplier_goods_id / supplier_shop_id，
                            // 本库商品表没有这两个字段（代发链路未启用）。查询失败按「非代发商品」处理，
                            // 不能因为一个可选的经销折扣把整张订单打回「操作失败」。
                            OrderDealerGoodsVO orderDealerGoodsVO = null;
                            try {
                                orderDealerGoodsVO = orderBaseMapper.queryOrderDealerGoods(goodsId);
                            } catch (Exception dealerEx) {
                                LOG.warn("代发商品查询不可用，按非代发商品处理：{}", dealerEx.toString());
                            }
                            // 如果是 查询供应商商品含税金额 查询店铺经销折扣 封装经销折扣数据 存入数据库
                            if (null != orderDealerGoodsVO) {
                                orderDealerGoodsVO.setOrderId(orderId);
                                BigDecimal supplierTaxInclusiveAmount = orderBaseMapper.querySupplierTaxInclusiveAmount(orderDealerGoodsVO.getSupplierGoodsId());
                                orderDealerGoodsVO.setGoodsPrice(supplierTaxInclusiveAmount);
                                orderDealerGoodsVO.setTotalTaxInclusiveAmount(
                                        orderDealerGoodsVO.getGoodsPrice().multiply(new BigDecimal(goodsNum)).setScale(2, RoundingMode.HALF_UP));
                                // 根据店铺id  供应商店铺id 查询经销折扣
                                BigDecimal dealerDiscount = orderBaseMapper.queryDealerDiscount(orderDealerGoodsVO.getShopId(), orderDealerGoodsVO.getSupplierShopId());

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

                            // 存放goodsnameConsumeTrade
                            List<String> goodsNameList = new ArrayList<>();
                            goodsNameList.add(goodsName);
                            ctp.setGoodsName(goodsNameList);
                            // 存放goodsnameConsumeRecord
                            crp.setGoodsName(goodsNameList);
                            // 存放卖家商品名
                            crb.setGoodsName(goodsNameList);
                            if (!isInsertByOrderGoodsSuccess) {
                                return ResponseResult.fail(0, "创建订单失败！");
                            }
                            if (cp.getIsGift() == 1 && resultVO.getIsGiftProducts() == 1) {
                                giftFlag = true;
                            }
                            // 1 是通过分享页下单  2 是通过 商城直接下单
                            int fxType = 1;
                            if (!vkFlag) {
                                fxType = 2;
                            }
                            //通过推广页购买商品 并且商品是分销商品
                            if (resultVO.getFxGoods().equals(CommonType.commonStatus.YES.getCode()) && (!isCombination ||
                                    combinationVO.getCombinationType() == 2) && !giftFlag) {
                                BigDecimal orderGoodsPrice = paymentRealPrice.subtract(goodsShippingFee).setScale(2, RoundingMode.HALF_UP);
                                resultVO.setGoodsPrice(orderGoodsPrice);
                                //创建分销佣金订单
                                OrderCommissionVO orderCommission = fxService.addOrderCommission(resultVO, wapList.get(i), ui, cp.getShareUserId(), orderId, orderGoodsPrice, fxType, cp.getVkShopId());
                                if (vkFlag) {
                                    if (null != orderCommission) {
                                        //分享
                                        orderCommission.setFxTg(1);
                                        orderCommissionList.add(orderCommission);
                                    }
                                } else {
                                    //微客通过正常页面购买分销商品
                                    if (null != orderCommission) {
                                        //推广
                                        orderCommission.setFxTg(2);
                                        orderCommissionList.add(orderCommission);
                                    }
                                }
                            }
                            index++;
                        }
                    }

                    if (null != priceDTO.getTotalIntegral()) {
                        //存放积分商品兑换所需的积分
                        RedisUtil.set(HouConfig.HOU_USERINTEGRALYPAID + "--" + cp.getToken() + "--" + ui.getUserId(),
                                priceDTO.getTotalIntegral().toString(), 1200);
                    }
                    if (null == priceDTO.getTotalPrice()) {
                        priceDTO.setTotalPrice(new BigDecimal(0));
                    }
                    // 存放付款金额
                    RedisUtil.set(HouConfig.HOU_USERAMOUNTACTUALLYPAID + "--" + cp.getToken() + "--" + ui.getUserId(),
                            priceDTO.getTotalPrice().toString(), 1200);
                    String orderDate = DateUtils.getYear() + "-" + DateUtils.getMonth() + "-"
                            + DateUtils.getDay();
                    // 买家评价状态
                    // 订单状态
                    // 每家店铺商品总价(不包含运费，优惠价格)
                    BigDecimal shopPrice = detailedPrice.getShippingList().get(i).getShopPrice();
                    // 总优惠金额
                    BigDecimal orderDiscountFee = new BigDecimal(0);
                    int orderType = CommonType.orderType.GOODS_TYPE.getCode();
                    subclassDTO.setOrderBasePaymentAmount(
                            shopPrice.add(shippingFee).subtract(subclassDTO.getPlatformVoucherShopPrice())
                                    .subtract(subclassDTO.getRedPacketSumMoney())
                                    .subtract(subclassDTO.getCouponPrice())
                                    .subtract(shippingListDTO.getShopManjianDiscount() == null ? BigDecimal.ZERO : shippingListDTO.getShopManjianDiscount())
                                    .subtract(shippingListDTO.getShopManzheDiscount() == null ? BigDecimal.ZERO : shippingListDTO.getShopManzheDiscount())
                                    .subtract(subclassDTO.getBaseMemberDiscount())
                    );

                    //使用无门槛红包订单金额小于0
                    if (subclassDTO.getOrderBasePaymentAmount().compareTo(new BigDecimal(0)) < 0) {
                        subclassDTO.setOrderBasePaymentAmount(new BigDecimal(0));
                    }
                    // 总优惠金额
                    orderDiscountFee = shopPrice.add(shippingFee)
                            .subtract(subclassDTO.getOrderBasePaymentAmount())
                            .subtract(priceDTO.getManjianDiscountBase())
                            .subtract(priceDTO.getManzheDiscountBase())
                            .subtract(subclassDTO.getBaseMemberDiscount());

                    ShopUserMessageResultVO shopUserMessageResult = merchantServiceClient.queryUserId(shopId);
                    if (null != shopUserMessageResult || null != cp.getIntegralGoods() && cp.getIntegralGoods() == 0) {
                        // 二开下单前校验（before*）：订单入参组装后、写库前回调 OrderCreateBeforeExtension，可中断。
                        // ctx.abort → 抛 PluginAbortException(7001)，由 appCreateOrders 捕获返回失败 → createOrders 回滚事务。
                        {
                            Map<String, Object> beforeParams = new HashMap<>();
                            beforeParams.put("shopId", shopId);
                            beforeParams.put("orderId", subclassDTO.getOrderId());
                            orderCreateBeforeGateway.validateBefore(subclassDTO, beforeParams);
                        }
                        //orderBase新增订单信息
                        ResponseResult result = insertOrderBase(cp, dto, priceDTO, ui, subclassDTO, shopUserMessageResult, ctp, crp, crb, vkFlag, shopId, shopName, orderGoodsTime,
                                shopPrice, orderDiscountFee, shippingFee, buyerMessage, districtId, request, respData, totalDealerCommission, totalSupplyCommission,
                                StringUtils.isEmpty(priceDTO.getVoucherIdList()) ? "0" : priceDTO.getVoucherIdList().get(i), orderList, orderCancelMinute, shippingListDTO,
                                isCombination, combinationVO, giftFlag, ShippingFeeMap, storeInfoList, jingDongGoods);
                        if (result.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
                            GlobalTransactionContext.reload(RootContext.getXID()).rollback();
                            return ResponseResult.fail(0, "生成订单失败！");
                        }
                        if (result.getData() != null) {
                            orderCancelTime = Long.valueOf(result.getData().toString());
                        }
                        // 二开下单后（after*）：订单落库成功后回调 OrderCreateAfterExtension，二开可向 respData 补充字段
                        // （如外部系统单号）。after* 不回写已落库订单，故障由骨架按降级策略兜底。
                        orderCreateAfterGateway.afterCreate(subclassDTO, result, respData);
                    }
                    if (null != cp.getGoodsCat() && cp.getGoodsCat()) {
                        // 下单成功，删除购物车商品
                        orderUserCartMapper.batchDeleteGoods(ui.getUserId(), goodsIdList, cp.getShopGateId());
                    }
                    orderId = subclassDTO.getOrderId();
                }
                // 批量插入分佣订单
                if (!CollectionUtils.isEmpty(orderCommissionList)) {
                    orderCommissionMapper.insertBatch(orderCommissionList);
                }

                // 更新红包优惠券状态
                Map<String, Object> statusMap = new HashMap<>();
                statusMap.put("redpacketId", priceDTO.getRedpacketId());
                statusMap.put("platformVoucherCode", priceDTO.getPlatformVoucherCode());
                statusMap.put("voucherId", priceDTO.getVoucherIdList());
                statusMap.put("type", "1");
                statusMap.put("orderId", orderId);
                // 活动销量回写是下单后的附加动作：activity 服务未部署时不能把整单打回。
                ResponseResult salesResp;
                try {
                    salesResp = activityServiceClient.updateSalesPrm(statusMap);
                } catch (Exception salesEx) {
                    LOG.warn("活动销量回写不可用（activity 服务缺失），跳过：{}", salesEx.toString());
                    salesResp = null;
                }
                if (salesResp != null && salesResp.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
                    return salesResp;
                }

                // 拼团传值
                JoinPintuanParamVO jpp = new JoinPintuanParamVO();
                if (subclassDTO.getType() != null && 1 != subclassDTO.getType()) {
                    jpp.setType(subclassDTO.getType());
                    jpp.setToken(cp.getToken());
                    jpp.setMarkId(subclassDTO.getMarkId());
                    jpp.setUserId(ui.getUserId());
                    jpp.setIsMark(subclassDTO.getIsMark());
                    jpp.setGoodsId(subclassDTO.getGroupBookingGoodsId());
                    jpp.setCommonId(subclassDTO.getPinCommonId());
                    // 迁 Mall 收银台：拼团名额按 order_id 登记，成团改由支付成功回调按已付款人数判定
                    jpp.setOrderId(orderId);
                    ResponseResult getReturnPara = activityServiceClient.addJoinPintuan(jpp);
                    Integer code = getReturnPara.getCode();
                    if (code == 2) {
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        return ResponseResult.fail(0, "拼团失败！");
                    }
                }
                OrderInfoVO info = new OrderInfoVO();
                // 纯积分订单
                if (null != cp.getIntegralGoods() && cp.getIntegralGoods() == 0) {
                    if (detailedPrice.getTotalPrice() == null) {
                        Map<Object, Object> map = new HashMap<>();
                        map.put("token", cp.getToken());
                        map.put("orderIdList", subclassDTO.getPaymentNumber());
                        map.put("type", 12);
                        rr = payMentServiceClient.payMentUnifyInlet(map);
                        if (rr.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
                            return rr;
                        }
                        info.setIsIntegral(CommonType.commonStatus.YES.getCode());
                    } else {
                        info.setIsIntegral(CommonType.commonStatus.NO.getCode());
                    }
                }
                //更新库存
                // 存放商品信息，增加销量
                List<WapGoodsSaleSumUpdateVO> goodsDetailsList = new ArrayList<>();
                List<StoreInformationVO> arrStoreList = cp.getStoreInformationList();
                for (StoreInformationVO str1 : arrStoreList) {
                    List<GoodsInformationVO> goodsList = str1.getGoodsBaseList();
                    for (GoodsInformationVO str2 : goodsList) {
                        // 存放商品信息，增加销量
                        WapGoodsSaleSumUpdateVO wssu = new WapGoodsSaleSumUpdateVO();
                        if (null != cp.getOrderIsShopTake() && cp.getOrderIsShopTake() == 1) {
                            wssu.setShopGateId(cp.getShopGateId());
                        }
                        wssu.setToken(cp.getToken());
                        wssu.setGoodsId(str2.getGoodsId());
                        wssu.setGoodsNum(str2.getGoodsNum());
                        wssu.setGoodsType(0);
                        wssu.setTreasureOrderShopId(dto.getTreasureOrderShopId());
                        goodsDetailsList.add(wssu);
                        // 更新redis商品库存
                        String goodsStock = RedisUtil.get(HouConfig.GOODS_STOCK + str2.getGoodsId());
                        if (goodsStock != null && !StringUtils.isEmpty(goodsStock)) {
                            RedisUtil.set(HouConfig.GOODS_STOCK + str2.getGoodsId(), String.valueOf(Integer.parseInt(goodsStock) - str2.getGoodsNum()));
                        }
                    }
                }
                // list 转 String
                String saleSum = JSON.toJSONString(goodsDetailsList);
                if (null != cp.getOrderIsShopTake() && cp.getOrderIsShopTake() == 1) {
                    Map<String, String> param = new HashMap<>();
                    param.put("saleSum", saleSum);
                    param.put("type", "1");
                    MqSendUtils.mqSendGoodsStockByOrder(param);
                    merchantServiceClient.updateShopGateStock(saleSum);
                } else {
                    Map<String, String> param = new HashMap<>();
                    param.put("saleSum", saleSum);
                    param.put("type", "2");
                    MqSendUtils.mqSendGoodsStockByOrder(param);
                    //goodServiceClient.updateSaleSum(param);
                }
                info.setPaymentNumber(subclassDTO.getPaymentNumber());
                info.setOrderIdList(orderList);
                info.setOrderCancelTime(orderCancelTime);
                List<String> newOrderList = new ArrayList<>();
                for (String s : orderList) {
                    String orderSource = orderBaseMapper.selectOrderSource(s);
                    //排除怡亚通  京东 订单,不创建代发订单
                    if (!orderSource.equals(CommonType.SupplierSource.YI_YA_TONG.getCode().toString())
                            && !orderSource.equals(CommonType.SupplierSource.JING_DONG.getCode().toString())) {
                        newOrderList.add(s);
                    }
                }
                if (!newOrderList.isEmpty()) {
                    //代发订单创建
                    ResponseResult dealerResult = supplierCreateServer.createDealerGoodsOrder(newOrderList, para, request);
                    if (dealerResult.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
                        return ResponseResult.fail();
                    }
                }
                return ResponseResult.success(info);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail(e.getMessage());
        }

    }

    /**
     * 检查订单是否由门店自提。
     *
     * @param cp       公共参数对象，包含订单是否由门店自提的信息。
     * @param ui       用户信息对象，用于获取用户ID。
     * @param commonId 商品的通用ID，用于查询商品自提状态。
     * @return 如果订单由门店自提且用户尝试购买自己的商品，则返回错误响应；否则返回成功响应。
     */
    public ResponseResult checkOrderIsShopTake(CommonParameterVO cp, UserInfoVO ui, Integer commonId) {
        // 检查是否选择了门店自提
        if (null != cp.getOrderIsShopTake() && 1 == cp.getOrderIsShopTake()) {
            // 构建查询参数Map，包含用户ID
            // 判断判断当前用户是否开通门店  且门店id是多少
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("userId", ui.getUserId());

            // 调用业务服务查询门店自提点ID
            ResponseResult responseResult = merchantServiceClient.queryShopGateId(paramMap);
            // 如果查询成功
            if (responseResult.getCode() == CommonStatusEnum.SUCCESS.getCode()) {
                // 根据商品通用ID和门店自提点ID查询商品自提开始时间
                Integer shopTakeStart = orderGoodsMapper.queryShopTakeGoodsStart(commonId, responseResult.getData().toString());
                // 如果商品自提开始时间不为空且大于0，表示商品尚不可自提
                if (null != shopTakeStart && shopTakeStart > 0) {
                    // 返回错误响应，提示用户不能购买自己的商品
                    return ResponseResult.fail(BizCodeEnume.DO_NOT_PURCHASE_YOUR_OWN_PRODUCTS);
                }
            }
        }
        // 如果以上条件不满足，返回成功响应
        return ResponseResult.success();
    }

    /**
     * 处理满减赠折优惠
     *
     * @param sp
     * @param sl
     * @param mjzzIds
     * @param mjzzAmounts
     * @param mjzzRules
     */

    private void DealMjzzPromotion(ShopParaVO sp, ShippingListVO sl, HashSet<ManjianzengzheDTO> mjzzIds, Map<Integer, BigDecimal> mjzzAmounts, Map<Integer, Object> mjzzRules, Map<Integer, Integer> mzNum) throws Exception {
        for (ManjianzengzheDTO dto : mjzzIds) {
            Integer pid = dto.getPromotionId();
            //对每个活动进行处理
            List<ManjianzengzheGoodsRuleQueryDTO> ruleList =
                    (List<ManjianzengzheGoodsRuleQueryDTO>) mjzzRules.get(pid);
            BigDecimal price = mjzzAmounts.get(pid);
            //满足金额-满减满赠
            HashSet<BigDecimal> meetAmountList = new HashSet<BigDecimal>();
            //符合条件的最大满足金额-满减满赠
            BigDecimal meetAmountMax = BigDecimal.ZERO;
            //单个活动的满折商品数量
            Integer meetNum = null == mzNum.get(pid) ? 0 : mzNum.get(pid);
            BigDecimal meetNumBD = new BigDecimal(meetNum);

            //折扣内容
            Map<BigDecimal, ManjianzengzheGoodsRuleQueryDTO> ruleMap = new HashMap<>();
            String mainDiscountContent = null;
            //折扣类型 1-满减赠 3-满折
            Integer mainDiscountType = 0;

            for (ManjianzengzheGoodsRuleQueryDTO rule : ruleList) {
                BigDecimal meetAmount = rule.getMeetAmount();
                meetAmountList.add(meetAmount);
                ruleMap.put(meetAmount, rule);
                //一个活动对应的只能是一种折扣类型
                mainDiscountType = rule.getDiscountType();

            }
            //获取最大满足金额/数量
            for (BigDecimal meetAm : meetAmountList) {
                //满减赠活动 meetAmountMax为最大满足金额
                if (1 == mainDiscountType) {
                    if (price.compareTo(meetAm) == 0) {
                        meetAmountMax = meetAm;
                        break;
                    } else if (price.compareTo(meetAm) > 0) {
                        if (meetAm.compareTo(meetAmountMax) > 0) {
                            meetAmountMax = meetAm;
                        }
                    }
                    //满折活动 meetAmountMax为最大满足数量
                } else if (3 == mainDiscountType) {
                    if (meetNumBD.compareTo(meetAm) == 0) {
                        meetAmountMax = meetAm;
                        break;
                    } else if (meetNumBD.compareTo(meetAm) > 0) {
                        if (meetAm.compareTo(meetAmountMax) > 0) {
                            meetAmountMax = meetAm;
                        }
                    }
                }
            }
            BigDecimal mjzzDiscount = BigDecimal.ZERO;
            //获取折扣内容
            ManjianzengzheGoodsRuleQueryDTO mjzzRule = ruleMap.get(meetAmountMax);
            if (null != mjzzRule) {
                mainDiscountType = mjzzRule.getDiscountType();
                mainDiscountContent = mjzzRule.getDiscountContent();
                Integer hasGiveaway = mjzzRule.getHasGiveaway();
                //满减
                if (1 == mainDiscountType) {
                    if (null != mainDiscountContent && !mainDiscountContent.isEmpty()) {
                        BigDecimal mjDiscount = new BigDecimal(mainDiscountContent);
                        mjzzDiscount = mjDiscount.setScale(2, RoundingMode.HALF_UP);
                        sl.setShopManjianDiscount(sl.getShopManjianDiscount().add(mjzzDiscount));
                        sp.setTotalManjianDiscount(sp.getTotalManjianDiscount().add(mjzzDiscount));
                    }
                //满折
                } else if (3 == mainDiscountType) {
                    if (null != mainDiscountContent && !mainDiscountContent.isEmpty()) {
                        BigDecimal mzDiscount = BigDecimal.valueOf(0.1 * Double.parseDouble(mainDiscountContent)).setScale(2, RoundingMode.HALF_UP);
                        mjzzDiscount = price.subtract(price.multiply(mzDiscount)).setScale(2, RoundingMode.HALF_UP);
                        sl.setShopManzheDiscount(sl.getShopManzheDiscount().add(mjzzDiscount));
                        sp.setTotalManzheDiscount(sp.getTotalManzheDiscount().add(mjzzDiscount));
                        sl.getPromotionIdList().set(dto.getIndex(), 6);
                    }
                }
                List<BigDecimal> goodsAmountList = sl.getGoodsAmountList();
                int size = dto.getIndexList().size();
                Map<Integer, BigDecimal> manjianMap = new HashMap<>();
                Map<Integer, BigDecimal> manzheMap = new HashMap<>();
                for (int i = 0; i < size; i++) {
                    int index = dto.getIndexList().get(i);
                    if (mjzzDiscount.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal scale = mjzzDiscount.divide(price, 6, RoundingMode.HALF_EVEN);
                        sl.setScale(scale);
                        if (null != mainDiscountContent && !mainDiscountContent.isEmpty()) {
                            BigDecimal amountScale = 3 == mainDiscountType ? scale.setScale(2, RoundingMode.HALF_UP) : scale;
                            BigDecimal amount = sl.getGoodsAmountList().get(index);
                            BigDecimal goodsAmount = amount.multiply(BigDecimal.ONE.subtract(amountScale)).setScale(2, RoundingMode.HALF_UP);
                            goodsAmountList.set(index, goodsAmount);
                            sl.setGoodsAmountList(goodsAmountList);
                            BigDecimal discount = dto.getGoodsPriceList().get(i).divide(price, 2, RoundingMode.HALF_UP).multiply(mjzzDiscount);
                            //满减
                            if (1 == mainDiscountType) {
                                manjianMap.put(dto.getGoodsIdList().get(i), discount);
                            //满折
                            } else if (3 == mainDiscountType) {
                                manzheMap.put(dto.getGoodsIdList().get(i), discount);
                            }
                        }
                    }
                }
                sl.setManjianMap(manjianMap);
                sl.setManzheMap(manzheMap);
                //满赠
                if (1 == hasGiveaway) {
                    String giveawayName = mjzzRule.getGiveawayName();
                    String giveawayImage = mjzzRule.getGiveawayImage();
                    String spId = sl.getShopId().toString();
                    Map<String, String> giveawayMap = new HashMap<>();
                    giveawayMap.put("goodsId", dto.getGoodsId().toString());
                    giveawayMap.put("giveawayGoodsId", mjzzRule.getGoodsId().toString());
                    String giveawayId = mjzzRule.getGiveawayId().toString();
                    giveawayMap.put("giveawayId", giveawayId);
                    giveawayMap.put("giveawayName", giveawayName);
                    giveawayMap.put("giveawayImage", giveawayImage);
                    if (null == sl.getShopGiveawayList()) {
                        List<Map<String, String>> giveawayList = new ArrayList<>();
                        sl.setShopGiveawayList(giveawayList);
                    }
                    sl.getShopGiveawayList().add(giveawayMap);
                    if (null == sp.getGiveawayList()) {
                        List<Map<String, String>> totalGiveawayList = new ArrayList<>();
                        sp.setGiveawayList(totalGiveawayList);
                    }
                    Map<String, String> totalGiveawayMap = new HashMap<>(giveawayMap);
                    totalGiveawayMap.put("shopId", spId);
                    sp.getGiveawayList().add(totalGiveawayMap);
                }
            }
        }

    }

    /**
     * 查看拼团状态
     *
     * @param para
     * @return
     */
    public boolean checkGroupBookingStatus(ExpireMarkIdUpdateDTO para) {
        boolean flag = false;
        List<String> orderIdList = para.getOrderIdList();
        Integer status = para.getStatus();
        try {
            if (status == 1) {
                // 拼团成功，改变订单状态 当所有的单支付成功 才去改拼团状态 此处注释
//                boolean isUpdateBaseStatus = orderBaseMapper.updateGroupBookingBaseStatus(orderIdList);
                flag = true;
            } else {
                // 拼团失败
                boolean isUpdateBaseStatus = false;
                boolean isUpdateGoodsStatus = false;
                // U支付号
                for (String orderId : orderIdList) {
                    String cancelTime = DateUtils.getNowDateToString();
                    String payNumber = orderBaseMapper.queryPayNumber(orderId);
                    isUpdateBaseStatus = orderBaseMapper.updateGroupBookingBaseOrderStatus(orderId, cancelTime);
                    isUpdateGoodsStatus = orderBaseMapper.updateGroupBookingGoodsOrderStatus(payNumber);
                }
                if (isUpdateBaseStatus && isUpdateGoodsStatus) {
                    flag = true;
                }
            }
        } catch (Exception e) {
        }
        return flag;
    }


    public CreateOrderDTO getData(UserInfoVO ui, CommonParameterVO cp) {
        CreateOrderDTO dto = new CreateOrderDTO();

        Integer orderIsShopTake = 2;
        if (cp != null && cp.getOrderIsShopTake() != null) {
            orderIsShopTake = cp.getOrderIsShopTake();
        }
        Integer payMentCode = cp.getPaymentCode();
        if (null == payMentCode) {
            payMentCode = 1;
        }


        Integer treasureOrderShopId = cp.getTreasureOrderShopId();
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
        //判断商品是否是虚拟商品
        if (null != cp.getVirtualGoods() && 0 == cp.getVirtualGoods()) {
            orderReceiverName = ui.getUserName();
            orderReceiverContact = ui.getUserMobile();
            //虚拟订单不需要 收货地址
            orderReceiverAddress = "";
            orderReceiverProvinceId = 0;
            orderReceiverCityId = 0;
            orderReceiverAreaId = 0;
        }
        //判断商品是否是门店商品
        if (null != cp.getOrderIsShopTake() && 1 == cp.getOrderIsShopTake() && cp.getIsDelivery() == 2) {
            //虚拟订单不需要 收货地址
            orderReceiverAddress = "";
            orderReceiverProvinceId = 0;
            orderReceiverCityId = 0;
            orderReceiverAreaId = 0;
        }
        // 订单来源
        dto.setOrderFromNum(cp.getPlatForm());
        dto.setGoodsIds(cp.getGoodsIds());
        dto.setOrderIsShopTake(orderIsShopTake);
        dto.setPayMentCode(payMentCode);
        dto.setTreasureOrderShopId(treasureOrderShopId);
        dto.setOrderReceiverName(orderReceiverName);
        dto.setOrderReceiverAddress(orderReceiverAddress);
        dto.setOrderReceiverContact(orderReceiverContact);
        dto.setOrderReceiverProvinceId(orderReceiverProvinceId);
        dto.setOrderReceiverCityId(orderReceiverCityId);
        dto.setOrderReceiverAreaId(orderReceiverAreaId);
        dto.setFee(cp.getFee());
        dto.setIsDelivery(cp.getIsDelivery());
        dto.setPick(cp.getPick());
        return dto;

    }


    public PriceDTO getPriceData(ShopParaVO detailedPrice) {
        PriceDTO dto = new PriceDTO();

        // 红包编码
        String redpacketId = detailedPrice.getRedpacketId();
        // 平台优惠券code
        String platformVoucherCode = detailedPrice.getPlatformVoucherCode();
        // 平台优惠券金额
        BigDecimal platformVoucherPrice = detailedPrice.getPlatformVoucherPrice();
        // 参与平台优惠券商品id
        List<Integer> avaliableGoods = detailedPrice.getAvaliableGoods();
        // 获取实付总价
        BigDecimal totalPrice = detailedPrice.getTotalPrice();
        // 优惠券id集合
        List<String> voucherIdList = detailedPrice.getVoucherIdList();
        // 获取红包金额
        BigDecimal redPacketPrice = detailedPrice.getRedPacketPrice();
        // 优惠方式
        Integer orderDiscountType = detailedPrice.getOrderDiscountType();
        // 获取总优惠券金额
        BigDecimal couponTotalPrice = detailedPrice.getCouponTotalPrice();
        List<ShippingListVO> shippingLists = detailedPrice.getShippingList();
        // 获取满减优惠金额
        BigDecimal manjianDiscountBase = BigDecimal.ZERO;
        // 获取满折优惠金额
        BigDecimal manzheDiscountBase = BigDecimal.ZERO;
        //获取赠品清单
        List<Map<String, String>> totalGiveawayList = detailedPrice.getGiveawayList();
        //店铺赠品清单
        List<Map<String, String>> shopGiveawayList = new ArrayList<Map<String, String>>();
        //赠品信息
        String giveawayInfo = "";
        Integer redPacketStatus = detailedPrice.getRedPacketStatus();
        BigDecimal tradeDiscount = BigDecimal.ZERO;
        //积分商品所需积分之和
        Integer totalIntegral = detailedPrice.getTotalIntegral();
        //积分商品 0 是 1否
        Integer integralGoods = 1;

        //plus商品优惠金额
        //plus商品优惠金额
        BigDecimal plusgoodsprice = new BigDecimal(0);
        //plus商品优惠总金额
        BigDecimal plusgoodstotal = new BigDecimal(0);
        //是否为plus订单
        Integer plusOrder = 0;
        // 供应商待发货订单
        Integer dropShipping = 2;


        if (null != couponTotalPrice && redPacketPrice != null) {
            // 优惠价格
            tradeDiscount = couponTotalPrice.add(redPacketPrice);
        }

        // 商品总价(不包含运费及优惠金额)
        BigDecimal orderGoodsAmount = new BigDecimal(0);
        // 红包拆分累加金额
        BigDecimal orderRptPrice = new BigDecimal(0);
        // 购物津贴拆分价格
        BigDecimal platformVoucherSplitPrice = BigDecimal.ZERO;
        // 红包
        BigDecimal redPacketSplitPrice = BigDecimal.ZERO;


        dto.setRedpacketId(redpacketId);
        dto.setPlatformVoucherCode(platformVoucherCode);
        dto.setPlatformVoucherPrice(platformVoucherPrice);
        dto.setAvaliableGoods(avaliableGoods);
        dto.setTotalPrice(totalPrice);
        dto.setVoucherIdList(voucherIdList);
        dto.setRedPacketPrice(redPacketPrice);
        dto.setOrderDiscountType(orderDiscountType);
        dto.setCouponTotalPrice(couponTotalPrice);
        dto.setShippingLists(shippingLists);
        dto.setManjianDiscountBase(manjianDiscountBase);
        dto.setManzheDiscountBase(manzheDiscountBase);
        dto.setTotalGiveawayList(totalGiveawayList);
        dto.setShopGiveawayList(shopGiveawayList);
        dto.setGiveawayInfo(giveawayInfo);
        dto.setRedPacketStatus(redPacketStatus);
        dto.setTradeDiscount(tradeDiscount);
        dto.setTotalIntegral(totalIntegral);
        dto.setIntegralGoods(integralGoods);
        dto.setPlusgoodsprice(plusgoodsprice);
        dto.setPlusgoodstotal(plusgoodstotal);
        dto.setPlusOrder(plusOrder);
        dto.setDropShipping(dropShipping);

        dto.setOrderGoodsAmount(orderGoodsAmount);
        dto.setOrderRptPrice(orderRptPrice);
        dto.setPlatformVoucherSplitPrice(platformVoucherSplitPrice);
        dto.setRedPacketSplitPrice(redPacketSplitPrice);
        dto.setPointsRate(detailedPrice.getPointsRate());
        dto.setTotalCashPrice(detailedPrice.getTotalCashPrice());
        return dto;
    }


    public SubclassDTO getSubData(ShippingListVO vo, StoreInformationVO svo, UserInfoVO ui, String paymentNumber) {
        SubclassDTO dto = new SubclassDTO();

        // 订单编号
        String orderId = null;
        // 卖家id
        String sellerId = null;
        // 拼团commonid
        Integer pinCommonId = null;
        String markId = null;
        Integer type = null;
        // 是否是团长
        Integer isMark = null;
        Integer groupBookingGoodsId = null;
        Integer sizeA = null;
        Integer sizeB = null;
        Integer sizeC = null;

        BigDecimal orderSupplyPrice = BigDecimal.ZERO;
        Integer supplyShopId = null;
        String supplyShopName = null;

        //平台佣金
        BigDecimal orderGoodsCommission = new BigDecimal(0);
        //平台佣金
        BigDecimal orderBaseCommission = new BigDecimal(0);
        //会员折扣
        BigDecimal goodsMemberDiscount = new BigDecimal(0);
        //会员折扣
        BigDecimal baseMemberDiscount = new BigDecimal(0);

        Integer smallOrder = 0;
        // 是否参加活动值
        Integer orderPromotionId = 0;
        // 拼团状态值
        Integer groupbuyStatus = null;
        // markid
        String orderBuyId = null;
        // 每家店铺商品个数
        Integer goodsQuantity = vo.getGoodsQuantity();

        Integer redpacketTType = vo.getRedpacketTType();
        // 单品红包所属shopid
        String itemShopId = vo.getItemShopId();
        // 跨店满减所属shopid
        String allowanceShopId = vo.getAllowanceShopId();
        // 获取店铺红包id
        String shopLimit = vo.getShopLimit();
        // 优惠券使用状态
        Integer voucherStatus = vo.getVoucherStatus();

        // 代金券拆分累加金额
        BigDecimal orderCptPrice = new BigDecimal(0);
        // goods表实付价格(含运费，去除红包拆分价格)
        BigDecimal paymentRealPrice = new BigDecimal(0);
        // 商品红包拆分累加金额
        BigDecimal orderGoodsRptPrice = new BigDecimal(0);
        // base表实付金额
        BigDecimal orderBasePaymentAmount = new BigDecimal(0);
        // 记录店铺每次红包拆分金额
        BigDecimal redPacketSumMoney = new BigDecimal(0);
        // 平台优惠券店铺拆分
        BigDecimal platformVoucherShopPrice = new BigDecimal(0);
        // 优惠活动中平台承担的金额
        BigDecimal orderGoodsPlatformBear = BigDecimal.ZERO;
        BigDecimal orderGoodsAddPlatformBear = BigDecimal.ZERO;
        // 平台优惠券拆分
        BigDecimal platformVoucherRptPrice = new BigDecimal(0);
        BigDecimal platformVoucherRptAddPrice = BigDecimal.ZERO;

        // 每家店铺使用优惠券价格
        BigDecimal couponPrice = vo.getCouponPrice();
        // 记录商品每次红包拆分金额
        BigDecimal redPacketGoodsSumMoney = new BigDecimal(0);
        //生成订单编号
        if (ui.getUserId().length() <= 8) {
            // 订单编号
            orderId = DateUtils.nowtime() + ui.getUserId() + DateUtils.randomLengthByTwo();
        } else {
            String oldUserId = ui.getUserId();
            String newUserId = oldUserId.substring(0, 4)
                    + oldUserId.substring(oldUserId.length() - 4);
            // 订单编号
            orderId = DateUtils.nowtime() + newUserId + DateUtils.randomLengthByTwo();
        }


        // 商户订单号
        dto.setPaymentNumber(paymentNumber);
        dto.setOrderId(orderId);
        dto.setSellerId(sellerId);
        dto.setPinCommonId(pinCommonId);
        dto.setMarkId(markId);
        dto.setType(type);
        dto.setIsMark(isMark);
        dto.setGroupBookingGoodsId(groupBookingGoodsId);
        dto.setSizeA(sizeA);
        dto.setSizeB(sizeB);
        dto.setSizeC(sizeC);

        dto.setOrderSupplyPrice(orderSupplyPrice);
        dto.setSupplyShopId(supplyShopId);
        dto.setSupplyShopName(supplyShopName);

        dto.setOrderGoodsCommission(orderGoodsCommission);
        dto.setOrderBaseCommission(orderBaseCommission);
        dto.setGoodsMemberDiscount(goodsMemberDiscount);
        dto.setBaseMemberDiscount(baseMemberDiscount);
        dto.setSmallOrder(smallOrder);
        dto.setOrderPromotionId(orderPromotionId);
        dto.setGroupbuyStatus(groupbuyStatus);
        dto.setOrderBuyId(orderBuyId);
        dto.setGoodsQuantity(goodsQuantity);

        dto.setRedpacketTType(redpacketTType);
        dto.setItemShopId(itemShopId);
        dto.setAllowanceShopId(allowanceShopId);
        dto.setShopLimit(shopLimit);
        dto.setVoucherStatus(voucherStatus);
        dto.setOrderCptPrice(orderCptPrice);
        dto.setPaymentRealPrice(paymentRealPrice);
        dto.setOrderGoodsRptPrice(orderGoodsRptPrice);
        dto.setOrderBasePaymentAmount(orderBasePaymentAmount);
        dto.setRedPacketSumMoney(redPacketSumMoney);
        dto.setPlatformVoucherShopPrice(platformVoucherShopPrice);
        dto.setOrderGoodsPlatformBear(orderGoodsPlatformBear);
        dto.setOrderGoodsAddPlatformBear(orderGoodsAddPlatformBear);
        dto.setPlatformVoucherRptPrice(platformVoucherRptPrice);
        dto.setPlatformVoucherRptAddPrice(platformVoucherRptAddPrice);
        dto.setCouponPrice(couponPrice);
        dto.setRedPacketGoodsSumMoney(redPacketGoodsSumMoney);
        dto.setOrderId(orderId);

        return dto;
    }


    public ResponseResult insertOrderBase(CommonParameterVO cp, CreateOrderDTO dto, PriceDTO priceDTO, UserInfoVO ui,
                                          SubclassDTO subclassDTO, ShopUserMessageResultVO shopUserMessageResult,
                                          ConsumeTradeParaDTO ctp, ConsumeRecordParaDTO crp, ConsumeRecordByBusinessDTO crb,
                                          Boolean vkFlag, Integer shopId, String shopName, String orderGoodsTime, BigDecimal shopPrice,
                                          BigDecimal orderDiscountFee, BigDecimal shippingFee, String buyerMessage, Integer districtId,
                                          HttpServletRequest request, Map<String, Object> respData, BigDecimal totalDealerCommission,
                                          BigDecimal totalSupplyCommission, String voucherIds, List<String> orderList,
                                          Integer orderCancelMinute, ShippingListVO shippingListDTO, Boolean isCombination,
                                          CombinationVO combinationVO, Boolean giftFlag, Map<String, String> ShippingFeeMap,
                                          StoreInformationVO storeInfoList, Boolean jingDongGoods) throws ParseException {
        //获取赠品清单
        List<Map<String, String>> totalGiveawayList = priceDTO.getTotalGiveawayList();
        //店铺赠品清单
        List<Map<String, String>> shopGiveawayList = new ArrayList<>();
        Long orderCancelTime = null;
        Integer orderGateShopId = null;
        // 订单状态
        Integer orderStatus = 1;
        String sellerUserName = null;
        String sellerPhone = null;
        if (null != cp.getIntegralGoods() && cp.getIntegralGoods() == 0) {
            sellerUserName = "平台商铺";
            subclassDTO.setSellerId("1");
            sellerPhone = "";
        } else {
            subclassDTO.setSellerId(shopUserMessageResult.getUserId());
            sellerUserName = shopUserMessageResult.getUserName();
            sellerPhone = shopUserMessageResult.getContactsPhone();
        }
        RedisUtil.set(HouConfig.HOU_SELLERPHONE + "--" + subclassDTO.getOrderId(), sellerPhone, 86400);
        String voucherId = "";
        if (!"0".equals(voucherIds)) {
            voucherId = voucherIds;
        }
        // 创建订单(orderBase)
        if (null != cp.getOrderIsShopTake()) {
            dto.setOrderIsShopTake(cp.getOrderIsShopTake());
        }
        if (null != cp.getShopGateId()) {
            orderGateShopId = cp.getShopGateId();
        }
        String shareUserId = "0";
        if (vkFlag) {
            shareUserId = null == cp.getShareUserId() ? "0" : cp.getShareUserId();
        }
        if (dto.getPayMentCode() == 3) {
            orderStatus = 3;
        }
        //查询店铺赠品 ZZZ
        if (null != totalGiveawayList) {
            for (Map<String, String> giveawayMap : totalGiveawayList) {
                if (null != giveawayMap && null != shopId) {
                    if (null != giveawayMap.get("shopId") && (shopId.toString()).equals(giveawayMap.get("shopId"))) {
                        giveawayMap.remove("shopId");
                        shopGiveawayList.add(giveawayMap);
                    }
                }
            }
            // list 转 String
            priceDTO.setGiveawayInfo(JsonUtils.toString(shopGiveawayList));
            shopGiveawayList.clear();
        }

        // 计算嘉币、劲豆折扣
        String discount = resolveGradeDiscount(cp.getMemberClassId());
        //封装参数
        Map<String, Object> map = getStringObjectMapBase(dto, priceDTO, subclassDTO, ui, shopId, orderGateShopId, shopName, shareUserId, sellerUserName,
                orderGoodsTime, shopPrice, orderDiscountFee, shippingFee, buyerMessage, orderStatus, voucherId, districtId,
                request, respData, totalDealerCommission, totalSupplyCommission, isCombination, combinationVO, giftFlag, ShippingFeeMap,
                storeInfoList, discount);

        OrderBaseEntity entity = ParseMapUtils.parseMap2Object(map, OrderBaseEntity.class);
        entity.setOrderGateShopId(orderGateShopId);
        entity.setDeliveryChainId(orderGateShopId);
        entity.setCountryName(cp.getCountryName());
        entity.setInternationalDialingCode(cp.getInternationalDialingCode());
        entity.setCurrencySymbol(cp.getCurrencySymbol());
        entity.setExchangeRate(cp.getExchangeRate());
        entity.setCurrencyCode(cp.getCurrencyCode());
        entity.setCountryId(cp.getCountryId());
        entity.setCustomerType(dto.getCustomerType());
        entity.setOutShopId(dto.getOutShopId());
        if (jingDongGoods) {
            entity.setOrderSource(CommonType.SupplierSource.JING_DONG.getCode());
        }
        LOG.info("插入订单基础信息:{}", entity);
        int isInsertByorderBaseSuccess = orderBaseMapper.insert(entity);
        // 分销：下单即登记分销订单（未完成状态，只作预估收益展示，不产生佣金）
        notifyDistributionCreated(entity);
        // 插入店铺会员信息
        ShopUserDTO shopUserDTO = new ShopUserDTO();
        shopUserDTO.setShopId(shopId.toString());
        shopUserDTO.setUserId(ui.getUserId());
        merchantServiceClient.addShopUser(shopUserDTO);
        // 存放付款金额
        RedisUtil.set(HouConfig.HOU_USERAMOUNTACTUALLYPAID + "--" + cp.getToken() + "--" + ui.getUserId(),
                entity.getOrderPaymentAmount().toString(), 1200);
        String orderDate = DateUtils.getYear() + "-" + DateUtils.getMonth() + "-"
                + DateUtils.getDay();
        // consumeRecord所需参数(商家订单)
        crb.setSellId(subclassDTO.getSellerId());
        // 订单号
        crb.setOrderId(subclassDTO.getOrderId());
        crb.setUserName(sellerUserName);
        // 支付号
        crb.setPaymentNumber(subclassDTO.getPaymentNumber());
        crb.setTotalPrice(subclassDTO.getOrderBasePaymentAmount());
        // 嘉币数据
        crb.setOrderCashPayment(entity.getOrderCashPayment() != null
                ? entity.getOrderCashPayment() : entity.getOrderPaymentAmount());
        crb.setCustomerType(entity.getCustomerType());
        payMentServiceClient.insertConsumeRecordByBusiness(crb);
        // fegin调用插入数据ConsumeRecord(买家订单)
        crp.setUserId(ui.getUserId());
        // 订单号
        crp.setOrderId(subclassDTO.getOrderId());
        crp.setUserName(ui.getUserName());
        // 支付号
        crp.setPaymentNumber(subclassDTO.getPaymentNumber());
        crp.setTotalPrice(subclassDTO.getOrderBasePaymentAmount());
        // 嘉币数据
        crp.setOrderCashPayment(entity.getOrderCashPayment() != null
                ? entity.getOrderCashPayment() : entity.getOrderPaymentAmount());
        crp.setCustomerType(entity.getCustomerType());
        payMentServiceClient.insertConsumeRecord(crp);
        // consumeTrade所需参数
        ctp.setUserId(ui.getUserId());
        // 订单号
        ctp.setOrderId(subclassDTO.getOrderId());
        ctp.setSellerId(subclassDTO.getSellerId());
        // 支付号
        ctp.setPaymentNumber(subclassDTO.getPaymentNumber());
        // 商品价格
        ctp.setTotalPrice(subclassDTO.getOrderBasePaymentAmount());
        // 优惠价格
        ctp.setTradeDiscount(priceDTO.getTradeDiscount());
        //兑换积分商品所需的积分
        ctp.setTotalIntegral(priceDTO.getTotalIntegral());
        ctp.setIntegralGoods(priceDTO.getIntegralGoods());
        // 嘉币数据
        ctp.setOrderCashPayment(entity.getOrderCashPayment() != null
                ? entity.getOrderCashPayment() : entity.getOrderPaymentAmount());
        ctp.setCustomerType(entity.getCustomerType());
        // fegin调用插入数据ConsumeTrade
        payMentServiceClient.insertConsumeTrade(ctp);
        orderList.add(subclassDTO.getOrderId());
        Integer count = orderBaseMapper.checkInvoiceSwitchStatus(shopId);
        if (count > 0) {
            if (!orderList.contains(subclassDTO.getOrderId())) {
                orderList.add(subclassDTO.getOrderId());
            }
        }

        if (isInsertByorderBaseSuccess > 0) {
            // 更改赠品库存
            if (shippingListDTO.getShopGiveawayList() != null && !shippingListDTO.getShopGiveawayList().isEmpty()) {
                List<Integer> list = shippingListDTO.getShopGiveawayList().stream().map(item -> Integer.parseInt(item.get("giveawayId"))).collect(Collectors.toList());
                Map<String, String> param = new HashMap<>();
                param.put("list", JSON.toJSONString(list));
                param.put("type", "3");
                MqSendUtils.mqSendGoodsStockByOrder(param);
                //goodServiceClient.updateGiveawayStock(list);
            }
            // 更改秒杀取消时间
            if (null != orderCancelMinute) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date orderTimeTmp = simpleDateFormat.parse(orderGoodsTime);
                Calendar cal = Calendar.getInstance();
                cal.setTime(orderTimeTmp);
                cal.add(Calendar.MINUTE, orderCancelMinute);
                orderTimeTmp = cal.getTime();
                orderCancelTime = orderTimeTmp.getTime() / 1000;
                Date seckillCancelTime = simpleDateFormat.parse(simpleDateFormat.format(orderTimeTmp));
                Map<String, Object> orderMap = new HashMap<>();
                orderMap.put("orderId", subclassDTO.getOrderId());
                orderMap.put("seckillCancelTime", seckillCancelTime);
                orderBaseMapper.updateSeckillCancelTime(orderMap);
            }
            return ResponseResult.success("创建成功", orderCancelTime);
        } else {
            return ResponseResult.fail(orderCancelTime);
        }
    }

    private Map<String, Object> getStringObjectMapBase(CreateOrderDTO dto, PriceDTO priceDTO, SubclassDTO subclassDTO, UserInfoVO ui,
                                                       Integer shopId, Integer orderGateShopId, String shopName, String shareUserId,
                                                       String sellerUserName, String orderGoodsTime, BigDecimal shopPrice,
                                                       BigDecimal orderDiscountFee, BigDecimal shippingFee, String buyerMessage,
                                                       Integer orderStatus, String voucherId, Integer districtId,
                                                       HttpServletRequest request, Map<String, Object> respData,
                                                       BigDecimal totalDealerCommission, BigDecimal totalSupplyCommission,
                                                       Boolean isCombination, CombinationVO combinationVO, Boolean giftFlag,
                                                       Map<String, String> ShippingFeeMap, StoreInformationVO storeInfoList, String discount) {

        if (Objects.isNull(discount)) {
            discount = "1";
        }
        if ("用家".equals(dto.getCustomerTypeName()) || "批发".equals(dto.getCustomerTypeName())) {
            dto.setPayMentCode(1);
        }

        //获取当前订单下怡亚通商品的运费
        BigDecimal orderShippingFee = new BigDecimal("0");
        //订单来源 0:自营订单,1:代发平台供应链订单,2:代发怡亚通供应链订单
        Integer orderSource = 0;
        if (null != ShippingFeeMap && !ShippingFeeMap.isEmpty()) {
            List<String> commonIds = orderBaseMapper.selectCommonIdsByOrderId(subclassDTO.getOrderId());
            for (String commonId : commonIds) {
                //判断当前订单下的商品,是否有怡亚通代发的商品
                if (null != ShippingFeeMap.get(commonId)) {
                    orderSource = 2;
                    orderShippingFee = orderShippingFee.add(new BigDecimal(ShippingFeeMap.get(commonId)));
                }
            }
        }

        Map<String, Object> map = new HashMap<>();
        //0 是虚拟订单 1 不是虚拟订单
        Integer virtualGoods = 1;
        String periodOfValidity = null;
        Integer pastDue = null;
        //是否同城配送2-否 1-是
        Integer isDelivery = 2;
        if (respData.containsKey("virtualGoods")) {
            virtualGoods = (Integer) respData.get("virtualGoods");
        }
        if (respData.containsKey("periodOfValidity")) {

            periodOfValidity = (String) respData.get("periodOfValidity");
        }
        if (respData.containsKey("pastDue")) {

            pastDue = (Integer) respData.get("pastDue");
        }
        // 最终配送费
        BigDecimal fee = new BigDecimal(0);
        if (null != dto.getFee()) {
            fee = dto.getFee();
        }
        // 配送费
        BigDecimal pick = new BigDecimal(0);
        if (null != dto.getPick()) {
            pick = dto.getPick();
        }
        if (null != dto.getIsDelivery()) {
            isDelivery = dto.getIsDelivery();
        }
        map.put("orderSource", orderSource);
        map.put("orderId", subclassDTO.getOrderId());
        map.put("shopId", shopId);
        map.put("orderGateShopId ", orderGateShopId);
        map.put("shopName", shopName);
        map.put("buyerUserId", ui.getUserId());
        map.put("buyerUserName", ui.getUserName());
        map.put("sellerUserId", subclassDTO.getSellerId());
        map.put("sellerUserName", sellerUserName);
        map.put("orderDate", DateUtils.getYear() + "-" + DateUtils.getMonth() + "-" + DateUtils.getDay());
        map.put("orderCreateTime", orderGoodsTime);
        map.put("orderReceiverName", dto.getOrderReceiverName());
        map.put("orderReceiverAddress", dto.getOrderReceiverAddress());
        map.put("orderReceiverContact", dto.getOrderReceiverContact());
        map.put("orderReceiverProvinceId", dto.getOrderReceiverProvinceId());
        map.put("orderReceiverCityId", dto.getOrderReceiverCityId());
        map.put("orderReceiverAreaId", dto.getOrderReceiverAreaId());
        map.put("paymentNumber", subclassDTO.getPaymentNumber());
        map.put("invoiceStatus", subclassDTO.getInvoiceStatus());
        map.put("invoiceId", subclassDTO.getInvoiceId());
        map.put("orderGoodsAmount", shopPrice);
        // 嘉币金额

        List<Integer> goodsIds = dto.getGoodsIds();
        LOG.info("嘉币商品goodsId集合：{}", goodsIds);
        // 嘉币商品信息是可选增强（pointsRate 为空即无嘉币），查询失败不能阻断下单。
        // goods 服务在本环境按 @TableName("hou_goods_common") 查实体，而库表是 hou_goods_common，
        // 该接口稳定返回 20602；这里降级为「无嘉币商品」，不抛给调用方。
        List<GoodsCommonDTO> goodsCommonDTOS = null;
        try {
            goodsCommonDTOS = goodServiceClient.selectGoodsByIds(goodsIds);
        } catch (Exception goodsEx) {
            LOG.warn("嘉币商品信息查询不可用，按无嘉币商品下单：{}", goodsEx.toString());
        }
        if (CollUtil.isNotEmpty(goodsCommonDTOS)) {
            BigDecimal orderCashPayment = new BigDecimal(0);
            for (GoodsCommonDTO commonDTO : goodsCommonDTOS) {
                Double pointsRate = commonDTO.getPointsRate();
                if (pointsRate != null) {

                    ResponseResult<PointsCashRatioVo> pointsCashRatio = baseServiceClient.getPointsCashRatio();
                    if (pointsCashRatio.getCode() == 1) {
                        PointsCashRatioVo cashRatiVo = pointsCashRatio.getData();
                        BigDecimal commonPrice = commonDTO.getCommonPrice();

                        PointsRatioUtil.PointsCashRatioRes ratio = PointsRatioUtil.getPointsRatio(commonPrice, pointsRate);

                        LOG.info("嘉币商品{},原价：{},计算后金额：{}", commonDTO.getGoodsId(), commonDTO.getCommonPrice(), ratio);

                        orderCashPayment = orderCashPayment.add(ratio.getCashPrice());
                        LOG.info("嘉币商品{}的现金金额：{}", commonDTO.getGoodsId(), ratio.getCashPrice());

                        dto.setPayMentCode(1);
                    }
                }
            }
            map.put("orderCashPayment", priceDTO.getTotalCashPrice());
        }

        //订单实付价加配送费
        map.put("orderPaymentAmount", subclassDTO.getOrderBasePaymentAmount().add(fee));
        map.put("orderDiscountFee", orderDiscountFee.add(subclassDTO.getBaseMemberDiscount()));
        map.put("orderShippingFee", shippingFee);
        map.put("orderBuyerEvaluationStatus", 0);
        map.put("orderMessage", buyerMessage);
        map.put("orderStatus", orderStatus);
        map.put("orderDiscountType", priceDTO.getOrderDiscountType());
        map.put("voucherPrice", subclassDTO.getCouponPrice());
        map.put("voucherCode", voucherId);
        map.put("orderFrom", dto.getOrderFromNum());
        map.put("redpacketCode", priceDTO.getRedpacketId());
        map.put("redpacketPrice", priceDTO.getRedPacketPrice());
        map.put("orderRptPrice", subclassDTO.getRedPacketSumMoney());
        if (subclassDTO.getRedPacketSumMoney() != null && subclassDTO.getOrderBasePaymentAmount().compareTo(BigDecimal.ZERO) == 0) {
            map.put("orderRptPrice", shopPrice.subtract(subclassDTO.getBaseMemberDiscount().add(subclassDTO.getCouponPrice())));
        }
        map.put("platformCouponCode", priceDTO.getPlatformVoucherCode());
        map.put("platformCouponPrice", priceDTO.getPlatformVoucherPrice());
        map.put("platformCouponRptPrice", subclassDTO.getPlatformVoucherShopPrice());
        map.put("districtId", districtId);
        map.put("treasureOrderShopId", dto.getTreasureOrderShopId());
        map.put("orderPromotionId", subclassDTO.getOrderPromotionId());
        map.put("groupbuyStatus", subclassDTO.getGroupbuyStatus());
        map.put("orderBuyId", subclassDTO.getOrderBuyId());
        map.put("smallOrder", subclassDTO.getSmallOrder());
        map.put("liveId", request.getHeader("liveId"));
        map.put("accountId", request.getHeader("accountId"));
        map.put("virtualGoods", virtualGoods);
        map.put("periodOfValidity", periodOfValidity);
        map.put("pastDue", pastDue);
        map.put("shopDistributorId", 0);
        map.put("orderType", CommonType.orderType.GOODS_TYPE.getCode());
        map.put("deliverShopId", subclassDTO.getSupplyShopId());
        map.put("deliverShopName", subclassDTO.getSupplyShopName());
        map.put("orderSupplyAmount", subclassDTO.getOrderSupplyPrice());
        map.put("vkShareUserId", shareUserId);
        map.put("integralGoods", priceDTO.getIntegralGoods());
        map.put("totalIntegral", priceDTO.getTotalIntegral());
        map.put("orderIsShopTake", dto.getOrderIsShopTake());
        map.put("dropShipping", priceDTO.getDropShipping());
        map.put("dealerCommission", totalDealerCommission);
        map.put("supplyCommission", totalSupplyCommission);
        map.put("payMentCode", dto.getPayMentCode());
        map.put("manjianDiscount", priceDTO.getManjianDiscountBase());
        map.put("manzheDiscount", priceDTO.getManzheDiscountBase());
        map.put("giveawayInfo", priceDTO.getGiveawayInfo());
        map.put("plusOrder", priceDTO.getPlusOrder());
        if (isCombination && combinationVO.getCombinationType() == 1) {
            map.put("orderCommissionFee", subclassDTO.getOrderBasePaymentAmount().multiply(subclassDTO.getCatCommission()).setScale(2, RoundingMode.HALF_UP));
        } else {
            map.put("orderCommissionFee", subclassDTO.getOrderBaseCommission());
        }
        map.put("baseMemberDiscount", subclassDTO.getBaseMemberDiscount());
        map.put("deliveryCharge", pick);
        map.put("isDelivery", isDelivery);
        map.put("endfree", fee);
        if (isCombination) {
            map.put("combinationId", combinationVO.getCombinationId());
            map.put("combinationType", combinationVO.getCombinationType());
            map.put("combinationPrice", combinationVO.getCombinationPrice());
        } else {
            map.put("combinationId", 0);
            map.put("combinationType", 0);
            map.put("combinationPrice", 0);
        }
        map.put("isGiftOrder", giftFlag ? 1 : 0);
        //进货编号
        String purchaseNumber = "JH" + DateUtils.formateYearAndDay(new Date()) + PayToolUtil.buildRandom(6);
        map.put("purchaseNumber", purchaseNumber);
        map.put("originalOrder", storeInfoList.getOriginalOrder());

        return map;


    }

    @Override
    public ResponseResult productVoucherCreateOrders(CreateProductVoucherOrdersDTO para) throws TransactionException {
        ResponseResult result = null;
        // 获取Jedis实例，用于与Redis进行通信。
        // 获取jedis实例
        Jedis jedis = RedisUtil.getJedis();
        RedisLock redisLock = null;
        try {
            // 通过token获取用户信息。
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(para.getToken());
            // 创建RedisLock实例，用于实现分布式锁。
            redisLock = new RedisLock(jedis, "user-createOrders-" + userInfo.getUserId() + para.getGoodsId(), 10000);

            // 尝试获取分布式锁，如果成功则执行订单创建逻辑。
            // 用户锁
            if (redisLock.lock()) {
                result = createProductVoucherOrders(para);
                // 如果订单创建失败，则回滚事务。
                if (result.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
                    GlobalTransactionContext.reload(RootContext.getXID()).rollback();
                }
                return result;
            }
            // 如果无法获取分布式锁，则表示订单创建失败。
            return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
        } catch (InterruptedException | TransactionException e) {
            // 记录异常信息，并回滚事务。
            e.printStackTrace();
            GlobalTransactionContext.reload(RootContext.getXID()).rollback();
            return ResponseResult.fail(e.getMessage());
        } finally {
            // 释放分布式锁。
            redisLock.unLock();
            // 关闭Jedis实例。
            jedis.close();
        }
    }

    public ResponseResult createProductVoucherOrders(CreateProductVoucherOrdersDTO voucherOrdersDTO) {
        // 通过token获取用户信息。
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(voucherOrdersDTO.getToken());
        // 根据条件查询 兑换券是否有效 兑换券是否属于当前用户 是否适用当前商品
        ResponseResult responseResult = activityServiceClient.queryProductVoucherBaseId(userInfo.getUserId(), voucherOrdersDTO.getCommonId(), voucherOrdersDTO.getVoucherTId());
        if (responseResult.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return responseResult;
        }

        // 查询商品基本信息 判断商品有效性
        GdsShopDetailResultVO gdsShopDetailResultVO = merchantServiceClient.queryShopinfoByShopId(Integer.valueOf(voucherOrdersDTO.getShopId()));
        // 生成支付单号
        String paymentNumber = "U" + DateUtils.getTimeStamp() + DateUtils.randomLengthByTwo();
        // 生成订单编号
        WapShippingQueryResultVO goodsResultVO = goodServiceClient.queryProductVoucherGoods(voucherOrdersDTO.getGoodsId());
        if (null == goodsResultVO) {
            return ResponseResult.fail(BizCodeEnume.GOODS_OFFLINE.getCode(), BizCodeEnume.GOODS_OFFLINE.getMsg());
        }
        // 判断商品库存是否满足
        if (goodsResultVO.getGoodsStock() < 1) {
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_INVENTORY.getCode(), BizCodeEnume.INSUFFICIENT_INVENTORY.getMsg());
        }
        // 查询商品Spu信息
        GdbMerchantGoodsCommonVO goodsCommonVO = goodServiceClient.queryProductVoucherGoodsCommon(voucherOrdersDTO.getCommonId());
        // 查询店铺信息
        String orderId = null;
        //生成订单编号
        if (userInfo.getUserId().length() <= 8) {
            // 订单编号
            orderId = DateUtils.nowtime() + userInfo.getUserId() + DateUtils.randomLengthByTwo();
        } else {
            String oldUserId = userInfo.getUserId();
            String newUserId = oldUserId.substring(0, 4)
                    + oldUserId.substring(oldUserId.length() - 4);
            // 订单编号
            orderId = DateUtils.nowtime() + newUserId + DateUtils.randomLengthByTwo();
        }
        // 商品金额
        BigDecimal orderGoodsAmout = goodsResultVO.getGoodsPrice() == null ? BigDecimal.ZERO : goodsResultVO.getGoodsPrice();
        // 优惠 金额
        BigDecimal orderGoodsDiscountFee = orderGoodsAmout;
        // 实付金额
        BigDecimal orderGoodsPaymentAmount = new BigDecimal(0);
        // 优惠活动中平台承担的金额
        BigDecimal orderGoodsPlatformBear = new BigDecimal(0);
        // 兑换券Id
        Integer voucherId = Integer.valueOf(responseResult.getData().toString());
        // 兑换券金额
        BigDecimal voucherPrice = orderGoodsAmout;
        // 运费
        BigDecimal shippingFee = new BigDecimal(0);


        // 封装orderGoods 数据  插入库中
        ResponseResult isInsertByorderGoodsSuccess = insertProductVoucherOrderGoods(goodsResultVO, gdsShopDetailResultVO, orderId, orderGoodsAmout,
                orderGoodsDiscountFee, orderGoodsPaymentAmount, orderGoodsPlatformBear, voucherPrice, shippingFee, voucherId, userInfo);
        if (isInsertByorderGoodsSuccess.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return isInsertByorderGoodsSuccess;
        }
        //虚拟商品兑换码
        String redeemCode = null;
        // 虚拟商品兑换二维码url
        String redeemCodeUrl = null;

        // 判断是否是虚拟订 如果是生成 兑换码
        if (CommonType.virtualGoods.IS_VIRTUAL.getCode().equals(goodsCommonVO.getVirtualGoods())) {
            // 生成兑换码的唯一标识。
            SimpleDateFormat df = new SimpleDateFormat("ddHHmmss");
            String timeStamp = df.format(new Date());
            String temp = String.valueOf(Math.round((Math.random() + 1) * 1000));
            redeemCode = temp + timeStamp;
            // 生成 二维码
            redeemCodeUrl = createRedeemCodeUrl(redeemCode);
        }
        // 封装 orderBase 数据 插入库中
        ResponseResult isInsertByorderBaseSuccess = insertProductVoucherOrderBase(goodsResultVO, goodsCommonVO, gdsShopDetailResultVO, voucherOrdersDTO, orderId, orderGoodsAmout,
                orderGoodsDiscountFee, orderGoodsPaymentAmount, voucherPrice, shippingFee, voucherId, userInfo, redeemCode, redeemCodeUrl, paymentNumber);
        if (isInsertByorderBaseSuccess.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return isInsertByorderBaseSuccess;
        }
        // 插入payment服务中数据
        ConsumeTradeParaDTO ctp = new ConsumeTradeParaDTO();
        // 存放ConsumeRecord参数
        ConsumeRecordParaDTO crp = new ConsumeRecordParaDTO();
        // 存放卖家信息
        ConsumeRecordByBusinessDTO crb = new ConsumeRecordByBusinessDTO();

        // 存放goodsnameConsumeTrade
        List<String> goodsNameList = new ArrayList<>();
        goodsNameList.add(goodsResultVO.getGoodsName());
        ctp.setGoodsName(goodsNameList);
        // 存放goodsnameConsumeRecord
        crp.setGoodsName(goodsNameList);
        // 存放卖家商品名
        crb.setGoodsName(goodsNameList);

        // consumeRecord所需参数(商家订单)
        crb.setSellId(gdsShopDetailResultVO.getUserId());
        // 订单号
        crb.setOrderId(orderId);
        crb.setUserName(gdsShopDetailResultVO.getUserName());
        // 支付号
        crb.setPaymentNumber(paymentNumber);
        crb.setTotalPrice(orderGoodsPaymentAmount);
        crb.setPayCode(CommonType.payType.PAY_TYPE_PRODUCT_VOUCHER.getCode());
        payMentServiceClient.insertConsumeRecordByBusiness(crb);
        // fegin调用插入数据ConsumeRecord(买家订单)
        crp.setUserId(userInfo.getUserId());
        // 订单号
        crp.setOrderId(orderId);
        crp.setUserName(userInfo.getUserName());
        // 支付号
        crp.setPaymentNumber(paymentNumber);
        crp.setTotalPrice(orderGoodsPaymentAmount);
        crp.setPayCode(CommonType.payType.PAY_TYPE_PRODUCT_VOUCHER.getCode());
        payMentServiceClient.insertConsumeRecord(crp);


        ctp.setUserId(userInfo.getUserId());
        // 订单号
        ctp.setOrderId(orderId);
        ctp.setSellerId(gdsShopDetailResultVO.getUserId());
        // 支付号
        ctp.setPaymentNumber(paymentNumber);
        // 商品价格
        ctp.setTotalPrice(orderGoodsPaymentAmount);
        // 优惠价格
        ctp.setTradeDiscount(orderGoodsDiscountFee);
        //兑换积分商品所需的积分
        ctp.setTotalIntegral(0);
        ctp.setIntegralGoods(CommonType.orderType.NOT_INTEGRA.getCode());
        // fegin调用插入数据ConsumeTrade
        payMentServiceClient.insertConsumeTrade(ctp);
        // 修改对应商品库存 增加销量
        Map<String, Object> statusMap = new HashMap<>();
        statusMap.put("voucherId", voucherId);
        statusMap.put("orderId", orderId);
        statusMap.put("goodsId", voucherOrdersDTO.getGoodsId());
        statusMap.put("commonId", voucherOrdersDTO.getCommonId());
        statusMap.put("goodsNum", 1);
        goodServiceClient.updateProductVoucherGoodsStock(statusMap);
        // 修改兑换券状态
        ResponseResult salesResp;
        try {
            salesResp = activityServiceClient.updateProductVoucher(statusMap);
        } catch (Exception voucherEx) {
            LOG.warn("券状态回写不可用（activity 服务缺失），跳过：{}", voucherEx.toString());
            salesResp = null;
        }
        if (salesResp.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return salesResp;
        }
        return ResponseResult.success();
    }

    /**
     * 插入产品优惠券订单基础信息
     * <p>
     * 该方法主要用于在订单提交过程中，插入订单的基础信息，包括订单ID、买家信息、卖家信息、订单状态等
     * 它是订单提交流程中的一步，负责将与优惠券相关的产品订单基础数据保存到数据库中
     *
     * @param goodsResultVO           商品详情信息，包含商品ID等
     * @param goodsCommonVO           商品通用信息，包含是否为虚拟商品、有效期等
     * @param gdsShopDetailResultVO   店铺详情信息，包含店铺ID、店铺名称等
     * @param voucherOrdersDTO        优惠券订单数据传输对象，包含收货人信息等
     * @param orderId                 订单ID，用于关联到特定订单
     * @param orderGoodsAmout         订单商品总金额
     * @param orderGoodsDiscountFee   订单商品折扣金额
     * @param orderGoodsPaymentAmount 订单商品实际支付金额
     * @param voucherPrice            优惠券价格
     * @param shippingFee             运费
     * @param voucherId               优惠券ID
     * @param userInfo                用户信息，包含用户ID、用户名等
     * @param redeemCode              兑换码
     * @param redeemCodeUrl           兑换码网址
     * @param paymentNumber           支付编号
     * @return 返回响应结果，成功或失败
     */
    private ResponseResult insertProductVoucherOrderBase(WapShippingQueryResultVO goodsResultVO, GdbMerchantGoodsCommonVO goodsCommonVO,
                                                         GdsShopDetailResultVO gdsShopDetailResultVO, CreateProductVoucherOrdersDTO voucherOrdersDTO, String orderId,
                                                         BigDecimal orderGoodsAmout, BigDecimal orderGoodsDiscountFee, BigDecimal orderGoodsPaymentAmount,
                                                         BigDecimal voucherPrice, BigDecimal shippingFee, Integer voucherId, UserInfoVO userInfo,
                                                         String redeemCode, String redeemCodeUrl, String paymentNumber) {
        // 初始化订单基础实体对象，并设置基础属性
        OrderBaseEntity orderBaseEntity = new OrderBaseEntity();
        orderBaseEntity.setOrderId(orderId);
        orderBaseEntity.setShopId(gdsShopDetailResultVO.getShopId());
        orderBaseEntity.setShopName(gdsShopDetailResultVO.getShopName());
        orderBaseEntity.setDeliverShopId(gdsShopDetailResultVO.getShopId());
        orderBaseEntity.setDeliverShopName(gdsShopDetailResultVO.getShopName());
        orderBaseEntity.setBuyerUserId(userInfo.getUserId());
        orderBaseEntity.setBuyerUserName(userInfo.getUserName());
        orderBaseEntity.setSellerUserId(gdsShopDetailResultVO.getUserId());
        orderBaseEntity.setSellerUserName(gdsShopDetailResultVO.getUserName());
        orderBaseEntity.setOrderDate(new Date());
        orderBaseEntity.setOrderCreateTime(new Date());
        orderBaseEntity.setOrderReceiverName(voucherOrdersDTO.getOrderReceiverName());
        orderBaseEntity.setOrderReceiverAddress(voucherOrdersDTO.getOrderReceiverAddress());
        orderBaseEntity.setOrderReceiverContact(voucherOrdersDTO.getOrderReceiverContact());
        orderBaseEntity.setOrderReceiverProvinceId(voucherOrdersDTO.getOrderReceiverProvinceId());
        orderBaseEntity.setOrderReceiverCityId(voucherOrdersDTO.getOrderReceiverCityId());
        orderBaseEntity.setOrderReceiverAreaId(voucherOrdersDTO.getOrderReceiverAreaId());
        orderBaseEntity.setPaymentId(CommonType.payType.ONLINE_PAY.getCode().toString());
        orderBaseEntity.setPaymentName(CommonType.payType.ONLINE_PAY.getDesc());
        orderBaseEntity.setPaymentTime(new Date());
        orderBaseEntity.setPaymentNumber(paymentNumber);
        orderBaseEntity.setPayCode(CommonType.payType.PAY_TYPE_PRODUCT_VOUCHER.getCode());
        orderBaseEntity.setOrderGoodsAmount(orderGoodsAmout);
        orderBaseEntity.setOrderPaymentAmount(new BigDecimal(0.00));
        orderBaseEntity.setOrderDiscountFee(orderGoodsDiscountFee);
        orderBaseEntity.setOrderShippingFee(shippingFee);
        if (CommonType.virtualGoods.IS_VIRTUAL.getCode().equals(goodsCommonVO.getVirtualGoods())) {
            orderBaseEntity.setOrderStatus(CommonType.orderStatus.SHIPPED.getCode());
        } else {
            orderBaseEntity.setOrderStatus(CommonType.orderStatus.TO_BE_SHIPPED.getCode());
        }
        orderBaseEntity.setVoucherPrice(voucherPrice.doubleValue());
        orderBaseEntity.setOrderFrom(Integer.valueOf(voucherOrdersDTO.getPlatForm()));
        orderBaseEntity.setOrderCommissionFee(0.00);
        orderBaseEntity.setDistrictId(gdsShopDetailResultVO.getDistrictId());
        orderBaseEntity.setPaymentCode(CommonType.payType.ONLINE_PAY.getCode());
        orderBaseEntity.setVirtualGoods(goodsCommonVO.getVirtualGoods());
        orderBaseEntity.setPeriodOfValidity(goodsCommonVO.getPeriodOfValidity() != null ? DateUtils.stringToTime(goodsCommonVO.getPeriodOfValidity()) : null);
        orderBaseEntity.setPastDue(goodsCommonVO.getPastDue());
        orderBaseEntity.setRedeemCode(redeemCode);
        orderBaseEntity.setRedeemCodeUrl(redeemCodeUrl);
        orderBaseEntity.setIntegralGoods(CommonType.orderType.NOT_INTEGRA.getCode());
        orderBaseEntity.setOrderIsShopTake(CommonType.orderType.NOT_SHOP_TAKE.getCode());
        orderBaseEntity.setOrderType(CommonType.orderType.GOODS_TYPE.getCode());
        //2表示非代发
        orderBaseEntity.setDropShipping(2);
        orderBaseEntity.setOrderSupplyAmount(0.00);
        orderBaseEntity.setManjianDiscount(new BigDecimal(0.00));
        orderBaseEntity.setManzheDiscount(new BigDecimal(0.00));
        orderBaseEntity.setBaseMemberDiscount(0.00);
        orderBaseEntity.setRedpacketPrice(0.00);
        orderBaseEntity.setRedpacketPrice(0.00);
        orderBaseEntity.setDeliveryCharge("0");
        orderBaseEntity.setCombinationId(0);
        orderBaseEntity.setCombinationType(0);
        orderBaseEntity.setCombinationPrice(new BigDecimal(0.00));
        // 执行插入操作
        int isInsertByorderBaseSuccess = orderBaseMapper.insert(orderBaseEntity);
        // 判断插入是否成功
        if (isInsertByorderBaseSuccess > 0) {
            return ResponseResult.success();
        } else {
            return ResponseResult.fail();
        }
    }

    /**
     * 订单落库后通知商城分销模块：登记分销订单（未完成状态）。
     *
     * <p>与「确认收货 → 生成佣金」是两个独立时点：本方法只做登记，让分销员在
     * 「预估收益 / 推广订单」里立刻看到自己带来的单；真正的佣金要等确认收货。
     *
     * <p>约束同完成通知：商城侧按 orderId 幂等；异常一律吞掉只记日志——分销是旁路，
     * 不能因为分销失败让用户下不了单。
     *
     * @param order 已落库的订单（含 shopId / buyerUserId / vkShareUserId / 实付金额）
     */
    private void notifyDistributionCreated(OrderBaseEntity order) {
        com.houjiang.order.utils.DistributionNotifyHelper.notifyCreated(
                mallDistributionClient, internalJobToken, orderGoodsMapper, order);
    }

    /**
     * 插入产品优惠券订单商品信息
     * <p>
     * 该方法主要用于在订单提交过程中，将参与优惠券活动的商品信息插入到订单商品信息表中
     * 它包含了商品的基本信息、价格、折扣、优惠券等相关信息的插入逻辑
     *
     * @param goodsResultVO           商品详情信息，包含商品ID、商品名称等
     * @param gdsShopDetailResultVO   店铺详情信息，包含店铺ID、店铺名称等
     * @param orderId                 订单ID，用于关联商品信息到特定订单
     * @param orderGoodsAmout         订单商品总金额
     * @param orderGoodsDiscountFee   订单商品折扣金额
     * @param orderGoodsPaymentAmount 订单商品实际支付金额
     * @param orderGoodsPlatformBear  平台承担的金额
     * @param voucherPrice            优惠券价格
     * @param shippingFee             运费
     * @param voucherId               优惠券ID
     * @param userInfo                用户信息，包含用户ID等
     * @return 返回响应结果，成功或失败
     */
    private ResponseResult insertProductVoucherOrderGoods(WapShippingQueryResultVO goodsResultVO, GdsShopDetailResultVO gdsShopDetailResultVO,
                                                          String orderId, BigDecimal orderGoodsAmout, BigDecimal orderGoodsDiscountFee,
                                                          BigDecimal orderGoodsPaymentAmount, BigDecimal orderGoodsPlatformBear, BigDecimal voucherPrice,
                                                          BigDecimal shippingFee, Integer voucherId, UserInfoVO userInfo) {
        // 初始化订单商品实体对象，并设置基础属性
        OrderGoodsEntity orderGoodsEntity = new OrderGoodsEntity();
        orderGoodsEntity.setOrderId(orderId);
        orderGoodsEntity.setShopId(gdsShopDetailResultVO.getShopId());
        orderGoodsEntity.setShopName(gdsShopDetailResultVO.getShopName());
        orderGoodsEntity.setDeliverShopId(gdsShopDetailResultVO.getShopId());
        orderGoodsEntity.setDeliverShopName(gdsShopDetailResultVO.getShopName());
        // 2表示非代发
        orderGoodsEntity.setDropShipping(2);
        orderGoodsEntity.setGoodsId(goodsResultVO.getGoodsId());
        orderGoodsEntity.setCommonId(goodsResultVO.getCommonId());
        orderGoodsEntity.setBuyerUserId(userInfo.getUserId());
        orderGoodsEntity.setGoodsName(goodsResultVO.getGoodsName());
        orderGoodsEntity.setGoodsClassId(goodsResultVO.getCatId());
        orderGoodsEntity.setOrderSpecChineseValue(goodsResultVO.getGoodsSpec());
        orderGoodsEntity.setGoodsPrice(goodsResultVO.getGoodsPrice().doubleValue());
        // 商品数量设为1
        orderGoodsEntity.setOrderGoodsNum("1");
        orderGoodsEntity.setGoodsImage(goodsResultVO.getGoodsImage());
        orderGoodsEntity.setOrderGoodsAmount(orderGoodsAmout.doubleValue());
        orderGoodsEntity.setOrderGoodsPlatformBear(orderGoodsPlatformBear.doubleValue());
        orderGoodsEntity.setOrderGoodsDiscountFee(orderGoodsDiscountFee.doubleValue());
        orderGoodsEntity.setOrderGoodsPaymentAmount(orderGoodsPaymentAmount);
        //佣金设为0
        orderGoodsEntity.setOrderGoodsCommission(0.00);
        //订单商品状态设为待发货
        orderGoodsEntity.setOrderGoodsStatus(CommonType.orderStatus.TO_BE_SHIPPED.getCode());
        //评价状态初始化为0
        orderGoodsEntity.setOrderGoodsEvaluationStatus(0);
        orderGoodsEntity.setVoucherPrice(voucherPrice.doubleValue());
        //订单返现价格设为0
        orderGoodsEntity.setOrderRptPrice(0.00);
        //商品退货状态初始化为0
        orderGoodsEntity.setGoodsReturnStatus(0);
        //商品退款状态初始化为0
        orderGoodsEntity.setGoodsRefundStatus(0);
        //设置商品下单时间
        orderGoodsEntity.setOrderGoodsTime(new Date());
        //运费设为0
        orderGoodsEntity.setShippingFee(shippingFee.doubleValue());
        //会员折扣金额设为0
        orderGoodsEntity.setGoodsMemberDiscount(0.00);
        //总商品运费设为0
        orderGoodsEntity.setTotalGoodsShippingFee(shippingFee.doubleValue());
        //商品加价折扣设为0
        orderGoodsEntity.setGoodsPlusDiscount(0.00);
        //商品加价状态初始化为0
        orderGoodsEntity.setGoodsPlusDis(0);
        //活动单价设为0
        orderGoodsEntity.setActivityUnitPrice(new BigDecimal(0.00));
        orderGoodsEntity.setVoucherId(voucherId);
        //执行插入操作
        int isInsertByorderGoodsSuccess = orderGoodsMapper.insert(orderGoodsEntity);
        //判断插入是否成功
        if (isInsertByorderGoodsSuccess > 0) {
            //插入成功，返回成功响应
            return ResponseResult.success();
        } else {
            //插入失败，返回失败响应
            return ResponseResult.fail();
        }
    }

    public String createRedeemCodeUrl(String code) {
        // 生成兑换码的二维码并上传至OSS。
        byte[] b = QrCodeUtil.createQRCode(150, 150, code);
        String encode = Base64Utils.encode2(b);
        OssUploadUpdateParamDTO param = new OssUploadUpdateParamDTO();
        param.setBase64Str(encode);
        param.setStreamName(".png");
        ResponseResult responseResult = thirdPartyServiceClient.uploadToOssBase64(param);
        String redeemCodeUrl = (String) responseResult.getData();
        return redeemCodeUrl;
    }

    private String parseOrderDataByCatId(String para) {
        CommonParameterVO cp = JSON.parseObject(para, new TypeReference<CommonParameterVO>() {
        });
        List<StoreInformationVO> storeInformationList = cp.getStoreInformationList();
        LOG.info("下单参数转换前=====" + JSON.toJSONString(storeInformationList));
        List<StoreInformationVO> storeInformationList1 = new ArrayList<>();
        for (StoreInformationVO dto : storeInformationList) {

            Map<Integer, Integer> map = new HashMap<>();
            Map<Integer, String> list = new HashMap<>();
            for (GoodsInformationVO goods : dto.getGoodsBaseList()) {
                //查询商品来源
                Integer goodsSource = orderBaseMapper.queryGoodsSource(goods.getGoodsId());
                goodsSource = (goodsSource == null
                        || (goodsSource != CommonType.SupplierSource.YI_YA_TONG.getCode()
                        && goodsSource != CommonType.SupplierSource.JING_DONG.getCode())
                ) ? 1 : goodsSource;
                if (map.get(goodsSource) != null) {
                    List<GoodsInformationVO> newstoreInformationList = JSON.parseArray(list.get(goodsSource), GoodsInformationVO.class);
                    newstoreInformationList.add(goods);
                    list.put(goodsSource, JSON.toJSONString(newstoreInformationList));
                } else {
                    List<GoodsInformationVO> goodsBaseList = new ArrayList<>();
                    // 怡亚通商品单独拆成一个订单
                    if (goodsSource == CommonType.SupplierSource.YI_YA_TONG.getCode()) {
                        map.put(CommonType.SupplierSource.YI_YA_TONG.getCode(), goods.getGoodsId());
                    } else if (goodsSource == CommonType.SupplierSource.JING_DONG.getCode()) {
                        // 京东商品单独拆成一个订单
                        map.put(CommonType.SupplierSource.JING_DONG.getCode(), goods.getGoodsId());
                    } else {
                        map.put(1, goods.getGoodsId());
                    }
                    goodsBaseList.add(goods);
                    list.put(goodsSource, JSON.toJSONString(goodsBaseList));
                }
            }

            for (String value : list.values()) {
                List<GoodsInformationVO> newstoreInformationList = JSON.parseArray(value, GoodsInformationVO.class);
                StoreInformationVO dto1 = new StoreInformationVO();
                dto1.setShopId(dto.getShopId());
                dto1.setCouponId(dto.getCouponId());
                dto1.setBuyerMessage(dto.getBuyerMessage());
                dto1.setGoodsBaseList(newstoreInformationList);
                dto1.setInvoiceInfo(dto.getInvoiceInfo());
                storeInformationList1.add(dto1);
            }
        }

        LOG.info("下单参数转换后=====" + JSON.toJSONString(storeInformationList1));


        cp.setStoreInformationList(storeInformationList1);

        String str = JSON.toJSONString(cp);

        return str;

    }

    private ResponseResult checkSkuStock(CommonParameterVO cp) {
        if (null != cp.getOrderIsShopTake() && 1 == cp.getOrderIsShopTake()) {
            // 自提订单不做校验
            return ResponseResult.success();
        }
        if (StringUtils.isEmpty(cp.getOrderReceiverName()) || StringUtils.isEmpty(cp.getOrderReceiverAddress()) ||
                StringUtils.isEmpty(cp.getOrderReceiverContact()) || cp.getOrderReceiverProvinceId() == null ||
                cp.getOrderReceiverCityId() == null || cp.getOrderReceiverAreaId() == null) {
            return ResponseResult.fail(BizCodeEnume.ORDER_RECEIVER_INFO_EMPTY);
        }
        //查询区域编码
        String areaRegionCode = baseServiceClient.queryDistrictCode(cp.getOrderReceiverAreaId().toString());
        List<Integer> goodsId = new ArrayList<>();
        List<StoreInformationVO> storeInformationList = cp.getStoreInformationList();
        for (StoreInformationVO storeInformation : storeInformationList) {
            List<GoodsInformationVO> goodsBaseList = storeInformation.getGoodsBaseList();
            //查询销售区域参数
            StockQueryDTO dto = new StockQueryDTO();
            dto.setAreaRegionCode(areaRegionCode);

            for (GoodsInformationVO goodsBase : goodsBaseList) {
                StockItemDTO stockItemDTO = new StockItemDTO();
                //查询商品来源
                Integer goodsSource = orderBaseMapper.queryGoodsSource(goodsBase.getGoodsId());
                //怡亚通
                if (goodsSource != null && goodsSource == CommonType.SupplierSource.YI_YA_TONG.getCode() && null != areaRegionCode) {
                    List<StockItemDTO> itemDTOList = new ArrayList<>();
                    String skuId = orderBaseMapper.querySkuId(goodsBase.getGoodsId(), storeInformation.getShopId());
                    stockItemDTO.setSkuId(skuId);
                    stockItemDTO.setSkuNum(goodsBase.getGoodsNum().toString());
                    itemDTOList.add(stockItemDTO);
                    dto.setItemDTOList(itemDTOList);

                    ResponseResult result = goodServiceClient.checkSkuStock(dto);
                    SkuStockVO relultVo = JSONObject.parseObject(JSON.toJSONString(result.getData()), SkuStockVO.class);
                    if (relultVo.getSailState().equals("0") || relultVo.getStockState().equals("0")) {
                        //无货商品id
                        goodsId.add(goodsBase.getGoodsId());

                    }
                } else if (goodsSource != null && goodsSource == CommonType.SupplierSource.JING_DONG.getCode()) {
                    // 商品来源是京东
                    String address = baseServiceClient.getAddressString(cp.getOrderReceiverProvinceId(), cp.getOrderReceiverCityId(), cp.getOrderReceiverAreaId()).getData().toString();
                    address = address + cp.getOrderReceiverContact();
                    // 通过地址id 查询地址名称
                    Map<String, Object> map = new HashMap<>();
                    map.put("goodsId", goodsBase.getGoodsId());
                    map.put("address", address);
                    ResponseResult responseResult1 = goodServiceClient.queryGoodsJingDongExistStock(map);
                    if (responseResult1.getCode() == CommonStatusEnum.SUCCESS.getCode() && responseResult1.getData().equals(false)) {
                        goodsId.add(goodsBase.getGoodsId());
                    }
                }
            }
            //怡亚通 / 京东：只要有一件商品无货就整体失败
            if (!goodsId.isEmpty()) {
                // 旧写法 ResponseResult.fail(goodsId) 走的是 fail(T data) 重载，
                // 码值恒为 CommonStatusEnum.FAIL（0），无货商品 ID 只塞在 data 里 ——
                // 也就是说这个「无货」场景**从来没有定义过码值**，前端 uniapp / pc 的
                // `code === 15030` 分支从来没命中过，用户只看到一个通用失败提示，
                // 不知道是哪件商品没货。改用统一新码 40801 GOODS_OUT_OF_STOCK
                // （无货语义历史上没有对应旧码，故不认领任何旧码），商品 ID 仍放 data。
                return ResponseResult.fail(GoodsErrorCode.GOODS_OUT_OF_STOCK.code(),
                        GoodsErrorCode.GOODS_OUT_OF_STOCK.meta().userMessage(), goodsId);
            }
        }
        return ResponseResult.success();

    }

    private Map<String, String> checkShippingFee(CommonParameterVO cp) {
        List<StoreInformationVO> storeInformationList = cp.getStoreInformationList();
        //查询收货城市ID（二级区域编码)
        String areaRegionCode = baseServiceClient.queryDistrictCode(cp.getOrderReceiverCityId().toString());
        Map<String, String> totalShippingFeeMap = new HashMap<>();
        //查询有多少个SPU以及商品总数
        for (StoreInformationVO storeInformation : storeInformationList) {
            FreightCalculateDTO dto = new FreightCalculateDTO();
            dto.setCityId(areaRegionCode);
            List<FreightSpuInfoDTO> spuInfoList = new ArrayList<>();
            List<GoodsInformationVO> goodsBaseList = storeInformation.getGoodsBaseList();
            Map<String, String> shippingFeeMap = new HashMap<>();
            for (GoodsInformationVO goodsBase : goodsBaseList) {
                //查询商品来源
                Integer goodsSource = orderBaseMapper.queryGoodsSource(goodsBase.getGoodsId());
                //怡亚通
                if (goodsSource != null && goodsSource == 2) {
                    //获取每个商品的SpuId
                    String spuId = orderBaseMapper.querySpuId(goodsBase.getGoodsId(), storeInformation.getShopId());
                    if (null == shippingFeeMap.get(spuId)) {
                        shippingFeeMap.put(spuId, goodsBase.getGoodsNum().toString());
                    } else {
                        String goodsNum = shippingFeeMap.get(spuId);
                        Integer total = goodsBase.getGoodsNum() + Integer.parseInt(goodsNum);
                        shippingFeeMap.put(spuId, total.toString());
                    }
                }
            }
            //计算每个spu的运费
            for (String spuId : shippingFeeMap.keySet()) {
                String commonId = orderBaseMapper.queryCommonIdAndShopId(spuId, storeInformation.getShopId());
                FreightSpuInfoDTO freightSpuInfoDTO = new FreightSpuInfoDTO();
                freightSpuInfoDTO.setQuantity(Integer.parseInt(shippingFeeMap.get(spuId)));
                freightSpuInfoDTO.setSpuId(spuId);
                spuInfoList.add(freightSpuInfoDTO);
                dto.setSpuInfoList(spuInfoList);
                ResponseResult result = goodServiceClient.calculate(dto);
                String shippingFee = JSONObject.parseObject(JSON.toJSONString(result.getData()), String.class);
                // 二开运费（after*）：官方基础运费算完后回调 FreightCalcExtension，回读最终运费并写回最终运费，
                // 领域夹取（运费 >= 0）在网关内完成。解析失败回落官方运费，绝不阻断下单。
                try {
                    BigDecimal officialFreight = (shippingFee == null || shippingFee.isBlank())
                            ? BigDecimal.ZERO : new BigDecimal(shippingFee);
                    FreightCalcGateway.Result freightResult =
                            freightCalcGateway.calc(null, null, areaRegionCode, officialFreight);
                    if (freightResult.freightFee() != null) {
                        shippingFee = freightResult.freightFee().toPlainString();
                    }
                } catch (NumberFormatException e) {
                    LOG.warn("运费解析失败，回落官方运费：commonId={}, raw={}", commonId, shippingFee);
                }
                totalShippingFeeMap.put(commonId, shippingFee);
            }
        }
        return totalShippingFeeMap;

    }

    /**
     * 会员折扣倍率；无等级或查询失败时返回 1（原价）。
     */
    private String resolveGradeDiscount(Integer memberClassId) {
        if (memberClassId == null || memberClassId == 0) {
            return "1";
        }
        try {
            ResponseResult resp = baseServiceClient.getMemberNameByClassId(memberClassId.toString());
            if (resp != null && resp.getData() instanceof Map<?, ?> rawMap) {
                Object gd = rawMap.get("gradeDiscount");
                if (gd != null && StrUtil.isNotBlank(String.valueOf(gd))) {
                    return String.valueOf(gd);
                }
            }
        } catch (Exception e) {
            LOG.warn("getMemberNameByClassId failed memberClassId={}: {}", memberClassId, e.toString());
        }
        return "1";
    }

    private Double resolveGradeDiscountDouble(Integer memberClassId) {
        try {
            return Double.valueOf(resolveGradeDiscount(memberClassId));
        } catch (Exception e) {
            return 1.0d;
        }
    }

    /**
     * 组装促销算价的透传参数。
     *
     * <p>活动插件化后这些参数由插件自行按需声明；过渡期由
     * {@link LegacyActivityPromotionCalcExtension} 读取，用于调用现有 activity 校验接口。
     */
    private Map<String, Object> buildPromoExtra(String token,
                                                Object goodsId,
                                                Object commonId,
                                                BigDecimal normalPrice,
                                                BigDecimal activityPrice,
                                                Integer quantity) {
        Map<String, Object> extra = new HashMap<>();
        extra.put(LegacyActivityPromotionCalcExtension.EXTRA_TOKEN, token);
        extra.put(LegacyActivityPromotionCalcExtension.EXTRA_GOODS_ID, goodsId);
        if (commonId != null) {
            extra.put(LegacyActivityPromotionCalcExtension.EXTRA_COMMON_ID, commonId);
        }
        extra.put(LegacyActivityPromotionCalcExtension.EXTRA_NORMAL_PRICE, normalPrice);
        extra.put(LegacyActivityPromotionCalcExtension.EXTRA_ACTIVITY_PRICE, activityPrice);
        extra.put(LegacyActivityPromotionCalcExtension.EXTRA_QUANTITY, quantity);
        return extra;
    }
}
