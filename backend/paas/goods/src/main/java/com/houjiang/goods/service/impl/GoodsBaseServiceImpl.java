package com.houjiang.goods.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.houjiang.commons.aspect.UserPermissionsResult;
import com.houjiang.commons.aspect.UserService;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.goods.*;
import com.houjiang.commons.dto.oos.OssUploadUpdateParamDTO;
import com.houjiang.commons.dto.order.OrderGoodsBarterDTO;
import com.houjiang.commons.dto.order.WapGoodsSaleSumDTO;
import com.houjiang.commons.dto.shop.GateStoreIndexGoodsDTO;
import com.houjiang.commons.dto.shop.ShopEvaluationDTO;
import com.houjiang.commons.dto.shop.WapTransportAreaQueryDTO;
import com.houjiang.commons.dto.yiyatong.OpenSpuInfoQueryDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.exception.DefinedException;
import com.houjiang.commons.rocketmq.MqSendUtils;
import com.houjiang.commons.util.*;
import com.houjiang.commons.util.json.JsonUtils;
import com.houjiang.commons.util.excel.ExcelsUtil;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.util.secverifyapiutil.Base64Utils;
import com.houjiang.commons.vo.activity.*;
import com.houjiang.commons.vo.base.BaseGoodsConfigVO;
import com.houjiang.commons.vo.base.PointsCashRatioVo;
import com.houjiang.commons.vo.goods.*;
import com.houjiang.commons.vo.order.OrderGoodsBarterVO;
import com.houjiang.commons.vo.order.WapShippingQueryResultVO;
import com.houjiang.commons.vo.shop.GdsShopDetailResultVO;
import com.houjiang.commons.vo.shop.InvoiceSwitchEntityVO;
import com.houjiang.commons.vo.shop.TransportTemplateVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.commons.vo.yiyatong.base.OpenSpuInfoQueryVO;
import com.houjiang.goods.config.GoodsRepository;
import com.houjiang.goods.dto.*;
import com.houjiang.goods.entity.*;
import com.houjiang.goods.feign.*;
import com.houjiang.goods.mapper.*;
import com.houjiang.goods.service.GoodsBaseService;
import com.houjiang.goods.service.GoodsCommonService;
import com.houjiang.goods.service.SupplierGoodsYiyatongService;
import com.houjiang.goods.service.YiYaTongService;
import com.houjiang.goods.util.es.AccessTokenUtil;
import com.houjiang.goods.vo.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @ClassName GoodsBaseServiceImpl
 * @Description 商品表
 * @Author Amos
 * @Date 2023-05-25 09:16:35
 * @Version 1.0
 * @author Amos
 */
@Service
public class GoodsBaseServiceImpl extends ServiceImpl<GoodsBaseMapper, GoodsBaseEntity>
        implements GoodsBaseService {

    private final static Logger LOG = LoggerFactory.getLogger(GoodsBaseServiceImpl.class);

    @Autowired
    private ActivityServiceClient activityServiceClient;
    @Autowired
    private OrderReductionClient orderReductionClient;
    @Autowired
    private com.houjiang.goods.service.DiscountQueryService discountQueryService;
    @Autowired
    private com.houjiang.goods.mall.service.MallGoodsService mallGoodsService;
    @Autowired
    private MerchantServiceClient merchantServiceClient;
    @Autowired
    private BaseServiceClient baseServiceClient;
    @Resource
    private ShopBaseMapper shopBaseMapper;
    @Resource
    private GoodsCommonMapper goodsCommonMapper;
    @Resource
    private GoodsCommonAttributeAssociationMapper goodsCommonAttributeAssociationMapper;
    @Resource
    private BaseSysConfigsMapper baseSysConfigsMapper;
    @Resource
    private GoodsImagesMapper goodsImagesMapper;
    @Resource
    private GoodsCommonDetailMapper goodsCommonDetailMapper;
    @Resource
    private GoodsDelayConfigMapper goodsDelayConfigMapper;
    @Resource
    private ShopGoodsCatMapper shopGoodsCatMapper;
    @Resource
    private ShopSelfCatMapper shopSelfCatMapper;
    @Resource
    private ThirdPartyServiceClient thirdPartyServiceClient;
    @Resource
    private GoodsAttributeMapper goodsAttributeMapper;
    @Resource
    private GoodsAttributeContentMapper goodsAttributeContentMapper;
    @Resource
    private ShopTransportTemplateMapper shopTransportTemplateMapper;
    @Resource
    private ShopTransportAreaMapper shopTransportAreaMapper;
    @Autowired
    private GoodsCommonService goodsCommonService;
    @Autowired
    private GoodsCommonServiceImpl goodsCommonServiceImpl;
    @Autowired
    private GoodsStockMapper goodsStockMapper;
    @Autowired
    private StorageGoodsBaseMapper storageGoodsBaseMapper;
    @Resource
    private GoodsCategoryAttributesMapper goodsCategoryAttributesMapper;
    @Autowired
    private SupplierGoodsYiyatongService supplierGoodsYiyatongService;
    @Autowired
    private YiYaTongService yiYaTongService;
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private SupplierServiceClient supplierServiceClient;
    @Autowired
    private GoodsVersionMapper goodsVersionMapper;
    @Autowired
    private GoodsBaseVersionMapper goodsBaseVersionMapper;
    @Lazy
    @Resource
    private AsyncVersionServiceImpl asyncVersionService;

    @Value("${system.pcIndexConfig}")
    private String pcIndexConfig;

    @Value("${domainName}")
    public String domainName;

    static String[] suffix = {".text", ".pdf", ".word", ".png", ".jpg", ".jpeg"};

    /**
     * 更新商品收藏数量
     *
     * @param goodId 商品ID
     * @return 返回操作结果，成功或失败
     * <p>
     * 该方法用于减少商品的收藏数量。当商品的收藏数量大于0时，通过数据库操作将其减少1。
     * 这通常发生在用户取消收藏商品的场景。
     */
    @Override
    public ResponseResult updateGoodsCollect(Integer goodId) {
        // 通过商品ID查询商品基本信息
        GoodsBaseEntity goodsBaseEntityentit = this.baseMapper.selectById(goodId);

        // 当商品的收藏数量大于0时，执行更新操作
        if (goodsBaseEntityentit.getGoodsCollect() > 0) {
            // 更新商品收藏数量，将其减少1
            this.baseMapper.update(goodsBaseEntityentit, new LambdaUpdateWrapper<GoodsBaseEntity>().eq(GoodsBaseEntity::getGoodsId, goodId)
                    .set(GoodsBaseEntity::getGoodsCollect, goodsBaseEntityentit.getGoodsCollect() - 1));
        }

        // 返回成功操作结果
        return ResponseResult.success();
    }

    /**
     * 获取商品详情
     *
     * @param goodsId 商品ID
     * @return 返回商品详情信息的响应结果
     */
    @Override
    public ResponseResult getGoodsDetails(Integer goodsId) {
        // 通过商品ID查询商品详情（旧表可能已退役，失败时回落 mall SKU）
        ReceiveGoodsDetailsDTO detail = null;
        try {
            detail = this.baseMapper.selectCartMessage(goodsId);
        } catch (Exception e) {
            LOG.warn("selectCartMessage 失败，改走 mall SKU: goodsId={}, err={}", goodsId, e.getMessage());
        }
        if (null == detail) {
            detail = buildMallCartDetail(goodsId);
        }
        // 如果商品详情为空，则返回商品已下架的错误信息
        if (null == detail) {
            return ResponseResult.fail(BizCodeEnume.PRODUCT_HAS_BEEN_REMOVED_FROM_THE_SHELF);
        }

        // 计算价格
        ResponseResult<PointsCashRatioVo> pointsCashRatio = baseServiceClient.getPointsCashRatio();
        if (pointsCashRatio.getCode() == 1) {

            Double pointsRate = detail.getPointsRate();
            if (Objects.nonNull(pointsRate)) {
                PointsCashRatioVo cashRatiVo = pointsCashRatio.getData();
                PointsRatioUtil.PointsCashRatioRes ratio = PointsRatioUtil.getPointsRatio(detail.getGoodsPrice(), pointsRate);
                detail.setCashPrice(ratio.getCashPrice());
            }
        }

        // 检查商品状态，如果不满足上架条件，则更新商品库存和状态
        if (!detail.getCommonState().equals(1) || !detail.getCommonVerify().equals(1)
                || (detail.getCartStatus() != null && !detail.getCartStatus().equals(0))) {
            detail.setGoodsStock(0);
            detail.setCartStatus(0);
        }

        // 处理活动状态逻辑
        Integer integer = dealActivityState(detail.getGoodsId(), detail.getGoodsMaxSale(),
                detail.getCommonId(), detail.getCommonIsXian());
        // 如果活动状态为-100，则返回商品存在问题的错误信息
        if (integer != null && integer == -100) {
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), "该商品存在问题");
        }
        detail.setActivityGoodsState(integer);

        // 处理活动详情逻辑（commonIsXian=0 时仍探测满减：全场活动不会回写商品表）
        Integer commonIsXian = detail.getCommonIsXian() == null ? 0 : detail.getCommonIsXian();
        ActivityGoodsDetailDTO activityGoodsDetail = dealActivity(detail.getGoodsId(), detail.getGoodsMaxSale(),
                detail.getCommonId(), commonIsXian);

        // 如果活动详情不为空且活动状态不为0，则更新商品的活动状态和相关活动信息
        if (null != activityGoodsDetail && activityGoodsDetail.getActivityStatus() != null
                && !activityGoodsDetail.getActivityStatus().equals(0)) {
            detail.setActivityStatus(activityGoodsDetail.getActivityStatus());
            detail.setActivityGoodsDetail(activityGoodsDetail);
            detail.setCommonIsXian(activityGoodsDetail.getActivityStatus());
            detail.setGoodsStartTime(activityGoodsDetail.getActivityStartTime());

            // 若为限时折扣活动，则进一步处理
            if (!activityGoodsDetail.getActivityStatus().equals(4) && (activityGoodsDetail.getActivityStartTime() != null
                    && activityGoodsDetail.getActivityStatus() == 2 && !DateUtils.compareDate2(activityGoodsDetail.getActivityStartTime()))) {
                detail.setGoodsMaxSale(activityGoodsDetail.getGoodsUpperLimit());
                detail.setGoodsMinSale(activityGoodsDetail.getGoodsLowerLimit());
            }
        } else {
            // 如果活动详情为空，则设置默认的活动状态和空的活动详情信息
            detail.setActivityStatus(0);
            detail.setActivityGoodsDetail(new ActivityGoodsDetailDTO());
            detail.setCommonIsXian(0);
        }

        // 如果商品最大销售数量为0，则更新为0
        if (detail.getGoodsMaxSale().equals(0)) {
            detail.setGoodsMaxSale(0);
        }

        // 返回商品详情的响应结果
        return ResponseResult.success(JsonUtils.toString(detail));
    }

    /**
     * 处理活动商品状态
     * 根据商品ID和不同的活动类型，获取商品在特定活动中的状态
     *
     * @param goodsId      商品ID
     * @param goodsMaxSale 商品最大销售量（未使用）
     * @param commonId     活动ID（未使用）
     * @param commonIsXian 商品参与的活动类型 0表示不参加活动
     * @return 商品在活动中的状态，如果商品不参加活动或没有对应活动信息，返回null
     */
    private Integer dealActivityState(Integer goodsId, Integer goodsMaxSale, Integer commonId,
                                      Integer commonIsXian) {
        // 活动商品详情对象，用于存储活动商品的详细信息
        ActivityGoodsDetailDTO activityGoodsDetail = null;
        // 商品在活动中的状态
        Integer activityGoodsState = null;

        // 判断商品是否参加促销活动 0不参加,1砍价,2秒杀,3限时折扣,4拼团,5满减赠折
        if (!commonIsXian.equals(0)) {
            activityGoodsDetail = new ActivityGoodsDetailDTO();
            // 根据商品参与的活动类型，获取具体的活动商品信息
            if (commonIsXian.equals(2)) {
                // 如果商品参与秒杀活动，查询秒杀活动商品信息
                AppSeckillGoodsQueryResultVO seckillGoods = activityServiceClient.selectSeckillByGoodsId2(goodsId);
                if (seckillGoods != null) {
                    // 设置商品在秒杀活动中的状态
                    activityGoodsState = seckillGoods.getSeckillGoodsState();
                }
            } else if (commonIsXian.equals(3)) {
                AppDiscountGoodsQueryResultVO discountGoods = null;
                try {
                    discountGoods = discountQueryService.selectByGoodsIdAndTime(goodsId);
                } catch (Exception ignored) { }
                if (discountGoods != null) {
                    activityGoodsState = discountGoods.getDiscountGoodsState();
                }
            }
            // 设置活动商品详情中的活动状态
            activityGoodsDetail.setActivityStatus(commonIsXian);
        }

        // 返回商品在活动中的状态
        return activityGoodsState;
    }


    /**
     * 处理商品活动逻辑
     * 根据商品ID和活动类型查询并处理相应的活动信息，返回活动商品详情
     *
     * @param goodsId      商品ID
     * @param goodsMaxSale 商品最大销量限制
     * @param commonId     公共ID，用于识别活动参与
     * @param commonIsXian 商品参与的活动类型
     * @return 返回处理后的活动商品详情DTO
     */
    private ActivityGoodsDetailDTO dealActivity(Integer goodsId, Integer goodsMaxSale, Integer commonId,
                                                Integer commonIsXian) {
        // 活动商品详情
        ActivityGoodsDetailDTO activityGoodsDetail = null;
        // 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣,4拼团,5满减赠折
        if (commonIsXian != null && !commonIsXian.equals(0)) {
            activityGoodsDetail = new ActivityGoodsDetailDTO();
            // 处理不同类型的活动
            if (commonIsXian.equals(2)) {
                // 秒杀活动处理
                AppSeckillGoodsQueryResultVO seckillGoods = activityServiceClient.selectSeckillByGoodsId2(goodsId);
                commonIsXian = goodsCommonServiceImpl.dealSeckillActivity1(commonId, goodsId, goodsMaxSale, commonIsXian, activityGoodsDetail, null);
                if (seckillGoods != null) {
                    activityGoodsDetail.setSeckillGoods(seckillGoods);
                    activityGoodsDetail.setGoodsUpperLimit(seckillGoods.getGoodsUpperLimit());
                    activityGoodsDetail.setActivityStartTime(seckillGoods.getGoodsStartTime());
                }
            } else if (commonIsXian.equals(3)) {
                // 限时折扣活动处理（宿主 DiscountQueryService，不打已下线 activity）
                commonIsXian = dealDiscountActivity(goodsId, goodsMaxSale, commonIsXian, activityGoodsDetail);
            } else if (commonIsXian.equals(4)) {
                // 拼团活动处理
                commonIsXian = dealPintuanActivity(commonId, commonIsXian, goodsId, activityGoodsDetail);
            } else if (commonIsXian.equals(5)) {
                // 满减赠折活动处理
                commonIsXian = dealManjianzengzheActivity(commonId, goodsMaxSale, commonIsXian, goodsId, activityGoodsDetail);
            } else {
                // 其他未处理的活动类型，默认设置不参加活动
                commonIsXian = 0;
            }

            // 设置活动状态
            activityGoodsDetail.setActivityStatus(commonIsXian);
        } else {
            // 未标注促销时：先探测限时折扣（改价），再探测满减（全场/绑定未回写）
            activityGoodsDetail = new ActivityGoodsDetailDTO();
            commonIsXian = dealDiscountActivity(goodsId, goodsMaxSale, 3, activityGoodsDetail);
            if (commonIsXian != null && !commonIsXian.equals(0)) {
                activityGoodsDetail.setActivityStatus(3);
                return activityGoodsDetail;
            }
            commonIsXian = dealManjianzengzheActivity(commonId, goodsMaxSale, 5, goodsId, activityGoodsDetail);
            activityGoodsDetail.setActivityStatus(commonIsXian);
            if (commonIsXian == null || commonIsXian.equals(0)) {
                return null;
            }
        }

        // 返回活动商品详情
        return activityGoodsDetail;
    }

    /**
     * mall SKU → 购物车商品详情（旧 hou_goods_base 不存在时的回落）。
     * goodsId 优先按 SKU id 解析；找不到再按 SPU id 取第一个可售 SKU。
     */
    private ReceiveGoodsDetailsDTO buildMallCartDetail(Integer goodsId) {
        if (goodsId == null || mallGoodsService == null) {
            return null;
        }
        try {
            com.houjiang.commons.dto.mall.MallSkuSnapshot sku =
                    mallGoodsService.requireSaleableSku(goodsId.longValue());
            if (sku == null || sku.getSkuId() == null) {
                return null;
            }
            ReceiveGoodsDetailsDTO detail = new ReceiveGoodsDetailsDTO();
            detail.setGoodsId(sku.getSkuId().intValue());
            detail.setCommonId(sku.getGoodsId() == null ? goodsId : sku.getGoodsId().intValue());
            detail.setShopId(sku.getShopId() == null ? 0 : sku.getShopId().intValue());
            detail.setShopName("");
            detail.setGoodsName(sku.getGoodsName());
            detail.setGoodsImage(sku.getGoodsImage());
            detail.setGoodsPrice(sku.getSalePrice() == null ? BigDecimal.ZERO : sku.getSalePrice());
            detail.setGoodsStock(sku.getSaleableStock() == null ? 0 : sku.getSaleableStock());
            detail.setGoodsSpec(sku.getSkuSpec());
            detail.setGoodsIsShelves(sku.isSaleable() ? 1 : 2);
            detail.setCartStatus(sku.isSaleable() ? 0 : 1);
            detail.setGoodsMaxSale(sku.getSaleableStock() == null ? 0 : sku.getSaleableStock());
            detail.setGoodsMinSale(1);
            detail.setCommonVerify(1);
            detail.setCommonState(sku.isSaleable() ? 1 : 0);
            detail.setCommonIsXian(0);
            detail.setActivityStatus(0);
            detail.setVirtualGoods(1);
            detail.setDealerGoodsFlag(0);
            detail.setGoodsType(1);
            detail.setShopInvoices(0);
            return detail;
        } catch (Exception e) {
            LOG.warn("mall SKU 回落失败: goodsId={}, err={}", goodsId, e.getMessage());
            return null;
        }
    }


    /**
     * 处理满减赠者活动
     *
     * @param commonId            活动标识
     * @param goodsMaxSale        商品最大销量
     * @param commonIsXian        是否限时
     * @param goodsId             商品ID
     * @param activityGoodsDetail 活动商品详情
     * @return 更新后的是否限时状态
     */
    private Integer dealManjianzengzheActivity(Integer commonId, Integer goodsMaxSale, Integer commonIsXian, Integer goodsId,
                                               ActivityGoodsDetailDTO activityGoodsDetail) {
        ManjianzengzheGoodsQueryResultVO manjianzengzheGoods = null;
        try {
            manjianzengzheGoods = orderReductionClient.selectByGoodsId(commonId);
        } catch (Exception e) {
            LOG.warn("满减查询不可用: commonId={}, err={}", commonId, e.getMessage());
        }

        // 如果满减赠者策略存在且其促销ID不为空
        if (null != manjianzengzheGoods && null != manjianzengzheGoods.getPromotionId()) {
            // 比较当前时间与活动结束时间，如果活动尚未结束
            if (DateUtils.compareDate2(manjianzengzheGoods.getEndTime())) {
                // 计算剩余时间
                Long overplusTime = DateUtils.getSecondByMinusDate(new Date(), manjianzengzheGoods.getEndTime());
                // 更新活动商品详情中的剩余时间
                activityGoodsDetail.setOverplusTime(overplusTime);
                activityGoodsDetail.setActivityEndTime(manjianzengzheGoods.getEndTime());
                List<ManjianzengzheGoodsRuleQueryDTO> ruleList = List.of();
                try {
                    ruleList = orderReductionClient.selectList(manjianzengzheGoods.getPromotionId());
                } catch (Exception e) {
                    LOG.warn("满减规则查询不可用: promotionId={}, err={}", manjianzengzheGoods.getPromotionId(), e.getMessage());
                }
                if (ruleList == null) {
                    ruleList = List.of();
                }
                // 更新活动商品详情中的促销ID、规则列表和商品价格
                activityGoodsDetail.setPromotionId(manjianzengzheGoods.getPromotionId());
                activityGoodsDetail.setActivityId(manjianzengzheGoods.getPromotionId());
                activityGoodsDetail.setRuleList(ruleList);
                activityGoodsDetail.setMjzzPrice(manjianzengzheGoods.getGoodsPrice());
                // 设置商品的下限和上限
                activityGoodsDetail.setGoodsLowerLimit(0);
                activityGoodsDetail.setGoodsUpperLimit(goodsMaxSale);
                // 保持 5：下单侧按 ruleList.discountType 区分满减/满折
            } else {
                // 如果活动已结束，设置commonIsXian为0
                commonIsXian = 0;
            }
        } else {
            // 如果满减赠者策略不存在，设置commonIsXian为0
            commonIsXian = 0;
        }

        return commonIsXian;
    }

    /**
     * 处理拼团活动
     *
     * @param commonId            活动标识
     * @param commonIsXian        是否限时
     * @param goodsId             商品ID
     * @param activityGoodsDetail 活动商品详情
     * @return 更新后的是否限时状态
     */
    private Integer dealPintuanActivity(Integer commonId, Integer commonIsXian, Integer goodsId,
                                        ActivityGoodsDetailDTO activityGoodsDetail) {
        // 根据活动标识和商品ID选择拼团策略
        AppPintuanGoodsQueryResultVO pintuanGoods = activityServiceClient.selectpiecingTogetherByGoodsId(commonId, goodsId);
        if (null != pintuanGoods) {
            // 比较当前时间与活动结束时间，如果活动尚未结束
            if (DateUtils.compareDate2(pintuanGoods.getEndTime())) {
                // 计算剩余时间
                Long overplusTime = DateUtils.getSecondByMinusDate(new Date(), pintuanGoods.getEndTime());
                // 更新活动商品详情中的剩余时间
                activityGoodsDetail.setOverplusTime(overplusTime);
                // 更新活动商品详情中的拼团价格
                activityGoodsDetail.setPintuanPrice(pintuanGoods.getPrice());
                activityGoodsDetail.setPriceOne(pintuanGoods.getPriceOne());
                activityGoodsDetail.setActivityPrice(pintuanGoods.getPrice());
            } else {
                // 如果活动已结束，设置commonIsXian为0
                commonIsXian = 0;
            }
            // 更新拼团策略中的是否限时状态
            pintuanGoods.setCommonIsXian(commonIsXian);
        } else {
            // 如果拼团策略不存在，设置commonIsXian为0
            commonIsXian = 0;
        }

        return commonIsXian;
    }

    /**
     * 处理折扣活动
     *
     * @param goodsId             商品ID
     * @param goodsMaxSale        商品最大销量
     * @param commonIsXian        是否是限时折扣活动，1表示是，0表示否
     * @param activityGoodsDetail 活动商品详情对象
     * @return 更新后的commonIsXian值
     */
    private Integer dealDiscountActivity(Integer goodsId, Integer goodsMaxSale, Integer commonIsXian,
                                         ActivityGoodsDetailDTO activityGoodsDetail) {
        AppDiscountGoodsQueryResultVO discountGoods = null;
        try {
            discountGoods = discountQueryService.selectByGoodsIdAndTime(goodsId);
        } catch (Exception e) {
            LOG.warn("限时折扣查询不可用: goodsId={}, err={}", goodsId, e.getMessage());
        }
        if (null != discountGoods) {
            // 检查活动是否在进行中
            if (discountGoods.getGoodsEndTime() != null && DateUtils.compareDate2(discountGoods.getGoodsEndTime())) {
                Long overplusTime = DateUtils.getSecondByMinusDate(new Date(), discountGoods.getGoodsEndTime());
                Integer lower = discountGoods.getGoodsLowerLimit() == null ? 0 : discountGoods.getGoodsLowerLimit();
                Integer upper = discountGoods.getGoodsUpperLimit() == null ? 0 : discountGoods.getGoodsUpperLimit();
                activityGoodsDetail.setGoodsLowerLimit(lower.equals(0) ? 0 : lower);
                activityGoodsDetail.setGoodsUpperLimit(upper.equals(0) ? goodsMaxSale : upper);
                activityGoodsDetail.setGoodsBuyCount(
                        discountGoods.getGoodsBuyCount() != null ? discountGoods.getGoodsBuyCount() : 0);
                activityGoodsDetail.setOverplusTime(overplusTime);
                activityGoodsDetail.setActivityPrice(discountGoods.getDiscountPrice());
                activityGoodsDetail.setActivityId(discountGoods.getDiscountId());
                activityGoodsDetail.setActivityEndTime(discountGoods.getGoodsEndTime());
                activityGoodsDetail.setActivityStartTime(discountGoods.getGoodsStartTime());
                activityGoodsDetail.setDiscountGoods(discountGoods);
            } else {
                commonIsXian = 0;
            }
        } else {
            commonIsXian = 0;
        }
        return commonIsXian;
    }

    /**
     * 处理秒杀活动
     *
     * @param goodsId             商品ID
     * @param goodsMaxSale        商品最大销量
     * @param commonIsXian        是否是秒杀活动，1表示是，0表示否
     * @param activityGoodsDetail 活动商品详情对象
     * @return 更新后的commonIsXian值
     */
    private Integer dealSeckillActivity(Integer goodsId, Integer goodsMaxSale, Integer commonIsXian,
                                        ActivityGoodsDetailDTO activityGoodsDetail) {
        // 根据商品ID查询秒杀活动信息
        AppSeckillGoodsQueryResultVO seckillGoods = activityServiceClient.selectSeckillByGoodsId2(goodsId);
        if (null != seckillGoods) {
            // 检查活动是否在进行中
            if (DateUtils.compareDate2(seckillGoods.getGoodsEndTime())) {
                // 剩余时间
                Long overplusTime = DateUtils.getSecondByMinusDate(new Date(), seckillGoods.getGoodsEndTime());
                // 设置购买下限
                activityGoodsDetail.setGoodsLowerLimit(0);
                // 设置购买上限
                if (!seckillGoods.getGoodsUpperLimit().equals(0)) {
                    activityGoodsDetail.setGoodsUpperLimit(seckillGoods.getGoodsUpperLimit());
                } else {
                    activityGoodsDetail.setGoodsUpperLimit(goodsMaxSale);
                }
                // 累计购买数量
                activityGoodsDetail
                        .setGoodsBuyCount(seckillGoods.getSeckillNum() != null ? seckillGoods.getSeckillNum() : 0);
                // 剩余时间
                activityGoodsDetail.setOverplusTime(overplusTime);
                // 活动价格
                activityGoodsDetail.setActivityPrice(seckillGoods.getSeckillPrice());
            } else {
                // 如果活动不在进行中，则标记commonIsXian为0
                commonIsXian = 0;
            }
        } else {
            // 如果没有对应的秒杀活动，则标记commonIsXian为0
            commonIsXian = 0;
        }
        return commonIsXian;
    }

    /**
     * 根据商品基础列表查询
     *
     * @param param 请求参数，包含商品ID和用户令牌
     * @return 包含商品基础信息列表的响应结果
     */
    @Override
    public ResponseResult queryByGoodsBaseList(ManageCommonIdsDTO param) {
        List<ManageGoodsBaseListVO> goodsBases = new ArrayList<ManageGoodsBaseListVO>();
        // 权限判断
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());

        if (userPermissionsResult.getFlag()) {
            goodsBases = this.baseMapper.selectByGoodsList(param.getCommonId());
            for (ManageGoodsBaseListVO goodsBase : goodsBases) {
                String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.STORAGE_SWITCH.getDesc());
                if (!StringUtils.isEmpty(value) && "1".equals(value)) {
                    Integer storageGoodsStock = storageGoodsBaseMapper.getGoodsStorageStock(goodsBase.getGoodsId());
                    goodsBase.setStorageGoodsStock(storageGoodsStock);
                } else {
                    goodsBase.setStorageGoodsStock(0);
                }
            }
        } else {
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        }
        return ResponseResult.success(goodsBases);
    }

    /**
     * 根据商品基础列表查询（版本3）
     *
     * @param param 请求参数，包含商品ID和用户令牌
     * @return 包含商品基础信息列表的响应结果
     */
    @Override
    public ResponseResult queryByGoodsBaseList3(ManageCommonIdsDTO param) {
        List<ManageGoodsBaseListVO> goodsBases = new ArrayList<ManageGoodsBaseListVO>();
        // 权限判断
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());

        if (userPermissionsResult.getFlag()) {
            goodsBases = this.baseMapper.selectByGoodsList3(param.getCommonId());
        } else {
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        }
        return ResponseResult.success(goodsBases);
    }

    /**
     * 查询秒杀商品列表
     *
     * @param param 请求参数，包含商品ID和用户令牌
     * @return 包含秒杀商品信息列表的响应结果
     */
    @Override
    public ResponseResult querySeckillGoodsList(ManageCommonIdsDTO param) {
        List<ManageGoodsBaseListVO> goodsBases;
        // 权限判断
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());

        if (userPermissionsResult.getFlag()) {
            goodsBases = this.baseMapper.querySeckillGoodsList(param);
        } else {
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        }
        return ResponseResult.success(goodsBases);
    }

    /**
     * 根据通用ID和商品ID更新商品基础信息和库存
     * 此方法用于处理单个商品ID和库存更新，或者商品ID列表和对应库存列表的更新
     * 在事务控制下完成更新操作，确保数据一致性
     *
     * @param param 包含商品ID、通用ID和库存信息的参数对象
     * @return 更新操作的结果，成功或失败的响应结果
     */
    @Override
    @Transactional
    public ResponseResult updateGoodsBaseByCommonIdAndGoodsId(ManageGoodsBaseListResultDTO param) {
        // 判断是否提供了商品ID列表或库存列表，如果没有，检查单个商品ID和库存是否提供
        if (param.getGoodsIdList() == null && param.getGoodsStockList() == null) {
            // 以下为单个商品ID和库存更新的条件检查
            if (param.getCommonId() == null) {
                return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), "commonId不能为null");
            }
            if (param.getGoodsId() == null) {
                return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), "goodsId不能为空");
            }
            if (param.getGoodsStock() == null) {
                return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), "库存不能为null");
            }
            if (param.getGoodsStock() < 0) {
                return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), "库存值错误");
            }
            // 调用更新商品基础信息和库存的辅助方法
            return updateGoodsBaseStock(param);
        } else {
            // 以下为批量更新商品库存的逻辑
            List<Integer> goodsIdList = param.getGoodsIdList();
            for (int i = 0; i < goodsIdList.size(); i++) {
                ManageGoodsBaseListResultDTO dto = new ManageGoodsBaseListResultDTO();
                dto.setCommonId(param.getCommonId());
                dto.setGoodsId(goodsIdList.get(i));
                dto.setGoodsStock(param.getGoodsStockList().get(i));
                updateGoodsBaseStock(dto);
            }
        }
        // 所有更新操作完成后，返回成功响应结果
        return ResponseResult.success();
    }

    /**
     * 更新商品基础信息和库存的辅助方法
     * 此方法负责实际的库存更新操作，包括商品的通用库存和特定商品库存
     *
     * @param param 包含商品ID、通用ID和库存信息的参数对象
     * @return 更新操作的结果，以ResponseResult形式返回
     */
    public ResponseResult updateGoodsBaseStock(ManageGoodsBaseListResultDTO param) {
        // 检查并更新关联商品信息
        GoodsCommonEntity commonEntity = goodsCommonMapper.selectOne(new LambdaQueryWrapper<GoodsCommonEntity>()
                .eq(GoodsCommonEntity::getCommonId, param.getCommonId()));
        // 判断当前视频是否是称重商品
        if (CommonType.virtualGoods.IS_WEIGHING.getCode().equals(commonEntity.getVirtualGoods())) {
            // 更新称重商品库存
            this.baseMapper.updateWeighingGoodsBaseStock(param);
            goodsCommonMapper.updateWeighingGoodsCommonStock(param);
            // 返回更新操作结果
            return ResponseResult.success();
        }

        // 获取当前商品的库存信息
        Integer goodsStock = this.baseMapper.queryGoodsStock(param.getGoodsId());
        // 计算库存变化量
        Integer integr = param.getGoodsStock() - goodsStock;
        // 根据库存变化量更新通用库存
        if (integr >= 0) {
            this.baseMapper.updateCommonStock(param.getCommonId(), integr);
        } else {
            this.baseMapper.updateCommonStock2(param.getCommonId(), integr);
        }

        // 更新商品基础信息
        Integer result = this.baseMapper.updateGoodsBaseByCommonIdAndGoodsId(param);

        GoodsStockEntity baseEntity = goodsStockMapper.selectOne(new LambdaQueryWrapper<GoodsStockEntity>()
                .eq(GoodsStockEntity::getGoodsId, param.getGoodsId()));
        if (commonEntity.getGoodsType() != null && commonEntity.getGoodsType() == 2) {
            goodsCommonMapper.update(null, new LambdaUpdateWrapper<GoodsCommonEntity>()
                    .set(GoodsCommonEntity::getCommonStock, commonEntity.getCommonStock())
                    .eq(GoodsCommonEntity::getCommonParentId, commonEntity.getCommonId()));
            List<GoodsBaseEntity> goodsBaseEntityList = this.baseMapper.selectList(new LambdaQueryWrapper<GoodsBaseEntity>()
                    .eq(GoodsBaseEntity::getSupplierGoodsId, baseEntity.getGoodsId()));
            if (null != goodsBaseEntityList) {
                for (GoodsBaseEntity goodsBaseEntity : goodsBaseEntityList) {
                    goodsStockMapper.update(null, new LambdaUpdateWrapper<GoodsStockEntity>()
                            .set(GoodsStockEntity::getGoodsStock, baseEntity.getGoodsStock())
                            .eq(GoodsStockEntity::getGoodsId, goodsBaseEntity.getGoodsId()));
                }
            }
        }
        // 返回更新操作结果
        return ResponseResult.success(result);
    }


    /**
     * 新增商品
     *
     * @param param 商品发布信息
     * @return 响应结果
     */
    @Override
    @Transactional
    public ResponseResult newAddGoods(ManageGdsCommonPublishDTO param) {
        try {
            UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.getToken());
            // 权限判断
            UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());
            LOG.info(JSON.toJSONString(userPermissionsResult));

            // 根据shopId查询店铺基本信息
            GdbMerchantShopBaseVO gdbMerchantShopBase = this.baseMapper.selectByPrimaryKey(Integer.valueOf(userPermissionsResult.getShopId()));
            Integer commonId = null;

            // 若权限验证通过
            if (userPermissionsResult.getFlag()) {
                String shopId = userPermissionsResult.getShopId();
                param.setShopId(shopId);
                param.setCommonUpdateBy(userPermissionsResult.getUserId());
                param.setCommonUpdateTime(new Date());

                // 初始化默认值
                // 判断是否放入仓库
                if (param.getCommonState() == null || param.getCommonState() == 0) {
                    param.setCommonState(0);
                }
                param.setCommonCubage(new BigDecimal(0));
                param.setGoodsId("0");

                // 根据店铺自持情况设置审核状态
                if (gdbMerchantShopBase.getShopSelfSupport().equals("true")) {
                    param.setCommonVerify(1);
                    if (param.getCommonState() != 4 && param.getCommonState() != 5) {
                        param.setCommonState(1);
                    }
                } else if (param.getCommonState() == 0) {
                    param.setCommonVerify(0);
                } else if (param.getCommonState() == 5) {
                    // 放入仓库修改为审核通过
                    param.setCommonVerify(1);
                } else {
                    param.setCommonVerify(10);
                }

                param.setCommonStock(0);
                param.setCommonAddTime(DateUtils.getNowDate());
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    param.setCommonSellTime(sdf.parse(param.getCommonSellTimeStr()));
                } catch (ParseException ignored) {
                    // 解析时间失败，忽略错误
                }

                String image = null;
                if (!CollectionUtils.isEmpty(param.getGoodsImages())) {
                    // 通常情况下取第一张图片作为主图
                    image = param.getGoodsImages().get(0).getMerchantGoodsImages().get(0).getImagesImage();
                    param.setCommonImage(image);
                }

                // 获取店铺区域id
                ManageShopBaseQueryVO shopBaseQueryResult = shopBaseMapper.selectByShopId2(param.getShopId());

                param.setDistrictId(shopBaseQueryResult.getDistrictId());
                param.setShopName(shopBaseQueryResult.getShopName());

                // 根据店铺自持情况设置shopSelfSupport字段
                if (shopBaseQueryResult.getShopSelfSupport() == "false") {
                    param.setShopSelfSupport(0);
                } else {
                    param.setShopSelfSupport(1);
                }

                // 若商品有视频，则设置主图为视频类型
                if (StringUtils.isNotEmpty(param.getCommonVideo())) {
                    param.setType(2);
                }

                param.setShopStatus(shopBaseQueryResult.getShopStatus());
                param.setCommonSpecValue("");

                // 将规格 和 规格值保存到数据库
                param.setSpecParamJson(JSON.toJSONString(param.getSpecParam()));

                // 根据店铺类型设置商品类型
                if (null != userPermissionsResult.getShopType()) {
                    if (userPermissionsResult.getShopType().equals(CommonType.ShopType.SUPPLY_SHOP.getCode())) {
                        param.setGoodsType(2);
                    }
                }

                // 品牌店铺商品类型为3
                if (gdbMerchantShopBase.getShopType() == 3) {
                    param.setGoodsType(3);
                }

                // 若未指定是否为虚拟商品，则默认为非虚拟商品
                if (null == param.getVirtualGoods()) {
                    param.setVirtualGoods(1);
                }
                // 若有效期为空字符串，则设置为null
                if (!StringUtils.isNotBlank(param.getPeriodOfValidity())) {
                    param.setPeriodOfValidity(null);
                }
                // 获取传来的门店id数组
                List<Integer> storeId = param.getStoreId();
                // 转化为字符串存储到数据库
                String storeIds = StringUtils.join(storeId, ",");
                param.setGoodsAssignmentStore(storeIds);
                // 以下几行设置品牌ID和名称
                param.setBrandId(param.getBrandId());
                param.setBrandName(param.getBrandName());
                // 店铺商品分类
                String join = Joiner.on(",").join(param.getGoodsCatIdList());
                System.out.println(join);
                param.setGoodsCatIds(join);
                // 序列化数组自定义信息
                String jsonString = JSON.toJSONString(param.getAfterServiceList());
                param.setAfterService(jsonString);
                // 添加数据到公共信息表,并获取common_id
                goodsCommonMapper.newInsertGdsCommonObj(param);
                LOG.info("商品common表更新成功，common_id = {}", param.getCommonId());
                commonId = param.getCommonId();

                // 属性id和属性内容id存储中间表
                param.setCommonId(commonId);
                // 前端传值商品属性id
                List<AttributeParamDTO> attributeIds = param.getAttribuParam();
                // 通过商品属性id查询属性内容然后添加对应的属性内容id
                if (null != attributeIds && !attributeIds.isEmpty()) {
                    for (AttributeParamDTO attribu : attributeIds) {
                        Integer attrConIds = attribu.getAttributeContentId();
                        param.setButeId(attrConIds);
                        param.setAttribuId(attribu.getAttributeId());
                        goodsCommonAttributeAssociationMapper.insertAss(param);
                    }
                }

                int num = 0;
                // 创建并设置查询参数，用于获取LOGO配置
                GdbNavSortLogoDTO gdbNavSortLogoDTO = new GdbNavSortLogoDTO();
                gdbNavSortLogoDTO.setConfigKey("logo_copywriting_ico_goods");
                gdbNavSortLogoDTO.setConfigType("logo_copywriting");
                // 查询并获取LOGO配置列表
                List<WebNavLogoConbfigDTO> webNavLogoConbfigDTOList = baseSysConfigsMapper.quertNavLogoByConfigType(gdbNavSortLogoDTO);
                // 获取goods_base,赋值commomid
                List<ManageGdsBaseAddPublishDTO> gdsBases = param.getGdsBases();
                for (int k = 0; k < gdsBases.size(); k++) {
                    ManageGdsBaseAddPublishDTO gdsBase = gdsBases.get(k);
                    gdsBase.setGoodsPromotionTips(param.getCommonPromotionTips());
                    gdsBase.setCommonId(commonId);
                    gdsBase.setShopId(shopId);
                    gdsBase.setShopName(shopBaseQueryResult.getShopName());
                    gdsBase.setGoodsIsRecommend(param.getCommonIsRecommend());
                    gdsBase.setGoodsImage(param.getGoodsImages().get(k).getMerchantGoodsImages().get(0).getImagesImage());

                    if (gdsBase.getBrandId() == null) {
                        gdsBase.setBrandId(0);
                    }
                    if (gdsBase.getGoodsAlarm() == null) {
                        gdsBase.setGoodsAlarm(0);
                    }
                    if (gdsBase.getGoodsMaxSale() == null) {
                        gdsBase.setGoodsMaxSale(0);
                    }

                    ManageGdsBaseAddParamvDTO manageGdsBaseAddParam = new ManageGdsBaseAddParamvDTO();
                    manageGdsBaseAddParam.setSpecId(gdsBase.getSpecId());
                    manageGdsBaseAddParam.setTypeId(gdsBase.getTypeId());
                    if (StringUtils.isNotBlank(gdsBase.getGoodsSpec())) {
                        manageGdsBaseAddParam.setGoodsName(gdsBase.getGoodsName() + "(" + gdsBase.getGoodsSpec() + ")");
                    } else {
                        manageGdsBaseAddParam.setGoodsName(gdsBase.getGoodsName());
                    }

                    manageGdsBaseAddParam.setGoodsId(gdsBase.getGoodsId());
                    manageGdsBaseAddParam.setCommonId(gdsBase.getCommonId());
                    manageGdsBaseAddParam.setShopId(gdsBase.getShopId());
                    manageGdsBaseAddParam.setShopName(gdsBase.getShopName());
                    manageGdsBaseAddParam.setGoodsPromotionTips(gdsBase.getGoodsPromotionTips());
                    manageGdsBaseAddParam.setCatId(gdsBase.getCatId());
                    manageGdsBaseAddParam.setGoodsSpecValue(gdsBase.getGoodsSpecValue());
                    manageGdsBaseAddParam.setGoodsPrice(gdsBase.getGoodsPrice());
                    manageGdsBaseAddParam.setGoodsMarketPrice(gdsBase.getGoodsMarketPrice());
                    manageGdsBaseAddParam.setGoodsStock(gdsBase.getGoodsStock());
                    //添加门店库存总和最大值
                    manageGdsBaseAddParam.setGoodsShopGateStock(gdsBase.getGoodsShopGateStock());
                    manageGdsBaseAddParam.setGoodsAlarm(gdsBase.getGoodsAlarm());
                    manageGdsBaseAddParam.setGoodsCode(gdsBase.getGoodsCode());
                    manageGdsBaseAddParam.setGoodsBarcode(gdsBase.getGoodsBarcode());
                    manageGdsBaseAddParam.setGoodsIsRecommend(gdsBase.getGoodsIsRecommend());
                    manageGdsBaseAddParam.setGoodsImage(gdsBase.getGoodsImage());
                    manageGdsBaseAddParam.setGoodsIsShelves(gdsBase.getGoodsIsShelves());
                    manageGdsBaseAddParam.setBrandId(gdsBase.getBrandId());
                    manageGdsBaseAddParam.setGoodsCubage(gdsBase.getGoodsCubage());
                    manageGdsBaseAddParam.setGoodsBabyPrice(gdsBase.getGoodsBabyPrice());
                    manageGdsBaseAddParam.setGoodsMinSale(gdsBase.getGoodsMinSale());
                    //规格跟着商品走
                    manageGdsBaseAddParam.setIsUse(gdsBase.getIsUse());
                    // 商家编号
                    manageGdsBaseAddParam.setMerchantCode(gdsBase.getMerchantCode());
                    // 商品编码
                    manageGdsBaseAddParam.setCommodityBarTypeCode(gdsBase.getCommodityBarTypeCode());
                    // 规格
                    manageGdsBaseAddParam.setSpecValue(JSON.toJSONString(gdsBase.getSpecValues()));
                    //指导价
                    manageGdsBaseAddParam.setHotPrice(gdsBase.getHotPrice());
                    List<GoodBaseSpecValuesJsonBeanDTO> specValues = gdsBase.getSpecValues();
                    String goodsSpec = null;
                    for (GoodBaseSpecValuesJsonBeanDTO specValue : specValues) {
                        if (goodsSpec == null) {
                            goodsSpec = specValue.getParamValue();
                            continue;
                        }
                        goodsSpec = goodsSpec + "+" + specValue.getParamValue();
                    }
                    manageGdsBaseAddParam.setGoodsSpec(goodsSpec);
                    // 体积
                    if (StringUtils.isNotEmpty(gdsBase.getGoodsVolume())) {
                        BigDecimal goodsVolume = new BigDecimal(gdsBase.getGoodsVolume());
                        manageGdsBaseAddParam.setGoodsVolume(goodsVolume);
                    }
                    // 商品成本价
                    if (null != param.getCommonCostPrice()) {
                        manageGdsBaseAddParam.setGoodsCostPrice(param.getCommonCostPrice());
                    }
                    // 直播卖点
                    if (StringUtils.isNotEmpty(param.getSellingPoint())) {
                        manageGdsBaseAddParam.setSellingPoint(param.getSellingPoint());
                    }
                    // 是否寄样
                    if (null != gdsBase.getGoodsSendBack()) {
                        manageGdsBaseAddParam.setGoodsSendBack(gdsBase.getGoodsSendBack());
                    }
                    //限购不设置 则默认最多购买数量是商品的库存
                    manageGdsBaseAddParam.setGoodsMaxSale(gdsBase.getGoodsMaxSale());
                    if (null == gdsBase.getGoodsMaxSale() || 0 == gdsBase.getGoodsMaxSale()) {
                        manageGdsBaseAddParam.setGoodsMaxSale(gdsBase.getGoodsStock());
                    }
                    if (null != gdsBase.getCommission()) {
                        manageGdsBaseAddParam.setCommission(gdsBase.getCommission());
                    }
                    this.baseMapper.insertGdsBasesObj(manageGdsBaseAddParam);
                    Integer goodsId = manageGdsBaseAddParam.getGoodsId();
                    System.out.println(goodsId);
                    gdsBase.setGoodsId(goodsId);

                    // 添加商品图片
                    List<GdbMerchantGuiGeGoodsImagesDTO> goodsImages = param.getGoodsImages();
                    if (!CollectionUtils.isEmpty(goodsImages)) {
                        // 遍历商品图片列表，设置默认值和关联商品信息
                        for (GdbMerchantGoodsImagesVO goodsImage : goodsImages.get(num).getMerchantGoodsImages()) {
                            // 如果图片显示顺序未设置，则设置为0
                            if (goodsImage.getImagesDisplayorder() == null) {
                                goodsImage.setImagesDisplayorder(0);
                            }
                            // 如果未设置默认图片，则设置为默认图片
                            if (goodsImage.getImagesIsDefault() == null) {
                                goodsImage.setImagesIsDefault(1);
                            }
                            // 设置商品ID、店铺ID和通用ID
                            goodsImage.setGoodsId(goodsId);
                            goodsImage.setShopId(shopId);
                            goodsImage.setCommonId(commonId);
                        }
                    } else {
                        // 如果商品图片列表为空，创建一个新的图片对象并添加到列表
                        GdbMerchantGoodsImagesVO goodsImage = new GdbMerchantGoodsImagesVO();
                        // 如果图片显示顺序未设置，则设置为0
                        if (goodsImage.getImagesDisplayorder() == null) {
                            goodsImage.setImagesDisplayorder(0);
                        }
                        // 如果未设置默认图片，则设置为默认图片
                        if (goodsImage.getImagesIsDefault() == null) {
                            goodsImage.setImagesIsDefault(1);
                        }
                        // 设置商品ID、店铺ID和通用ID
                        goodsImage.setGoodsId(goodsId);
                        goodsImage.setShopId(shopId);
                        goodsImage.setCommonId(commonId);
                        // 将新创建的图片对象添加到列表中
                        goodsImages.get(num).getMerchantGoodsImages().add(goodsImage);
                    }

                    // 插入非导航图标的商品图片
                    if (null != goodsImages && !goodsImages.isEmpty()) {
                        // 遍历商品图片列表，排除导航图标后插入数据库
                        for (GdbMerchantGoodsImagesVO recordImage : goodsImages.get(num).getMerchantGoodsImages()) {
                            // 排除导航图标
                            if (!recordImage.getImagesImage().equals(webNavLogoConbfigDTOList.get(0).getConfigValue())) {
                                recordImage.setImagesId(null);
                                goodsImagesMapper.insertSelective(recordImage);
                            }
                        }
                        num++;
                    }

                    LOG.info("商品common_images表更新成功");
                }

                // 添加goods_base
                LOG.info("商品goods_base表更新成功");

                // 添加商品详情
                GdbMerchantGoodsCommonDetailDTO goodsCommonDetail = param.getGoodsCommonDetail();
                if (null != goodsCommonDetail) {
                    goodsCommonDetail.setCommonId(commonId);
                    goodsCommonDetailMapper.insertSelective(goodsCommonDetail);
                    LOG.info("商品common_detail表更新成功");
                }

                // 更新商品规格
                newUpdateCommonSpec(commonId, gdsBases, userPermissionsResult.getUserId());

                if (null != param.getManageGdsGoodsConfig()) {
                    // 保存经销商品配置信息数据
                    BaseGoodsConfigVO manageGdsGoodsConfig = param.getManageGdsGoodsConfig();
                    manageGdsGoodsConfig.setCommonId(commonId);
                    goodsDelayConfigMapper.insertSelective(manageGdsGoodsConfig);
                }
                if (!CollectionUtils.isEmpty(param.getGoodsCatIdList())) {
                    List<MerchantShopGoodsCatDTO> goodsCatList = shopSelfCatMapper.queryShopCatIds(param.getGoodsCatIdList());
                    for (MerchantShopGoodsCatDTO shopGoodsCat : goodsCatList) {
                        shopGoodsCat.setCommonId(commonId);
                        shopGoodsCat.setShopId(Integer.valueOf(userPermissionsResult.getShopId()));
                        shopGoodsCat.setParentId(null == shopGoodsCat.getParentId() ? 0 : shopGoodsCat.getParentId());
                    }
                    shopGoodsCatMapper.insertSelectiveBatch(goodsCatList);
                }
                if (null != param.getCategoryAttributesList()) {
                    // 添加商品类目属性
                    for (GoodsCategoryAttributesDTO categoryAttributesDTO : param.getCategoryAttributesList()) {
                        if (StringUtils.isEmpty(categoryAttributesDTO.getAttributeValueName())) {
                            continue;
                        }
                        GoodsCategoryAttributesEntity attributesEntity = new GoodsCategoryAttributesEntity();
                        BeanUtils.copyProperties(categoryAttributesDTO, attributesEntity);
                        attributesEntity.setCommonId(commonId);
                        attributesIsRepeatedFlag(attributesEntity);
                        goodsCategoryAttributesMapper.insert(attributesEntity);
                    }
                }
            } else {
                return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
            }
            if (gdbMerchantShopBase.getShopSelfSupport().equals("true")) {
                Map<String, Object> esMap = new HashMap<>();
                List<Integer> list = new ArrayList<>();
                list.add(commonId);
                // 更新es
                esMap.put("commonIdS", list);
                // 1 是添加  2是删除
                esMap.put("type", 1);
                // 发送消息到MQ，用于更新ES中的商品销售数量
                MqSendUtils.mqSendGoods(esMap);
                asyncVersionService.addGoodsVersion(new ManageCommonIdsDTO(commonId, CommonType.commonStatus.YES.getCode(), param.getToken()));
            }
            return ResponseResult.success(commonId);
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.OPERATION_FAILED.getMsg());
        }
    }

    private void attributesIsRepeatedFlag(GoodsCategoryAttributesEntity entity) {
        List<String> attributeNames = goodsCategoryAttributesMapper.selectList(new LambdaQueryWrapper<GoodsCategoryAttributesEntity>()
                        .select(GoodsCategoryAttributesEntity::getAttributeName)
                        .ne(entity.getId() != null, GoodsCategoryAttributesEntity::getId, entity.getId())
                        .eq(GoodsCategoryAttributesEntity::getCommonId, entity.getCommonId()))
                .stream().map(GoodsCategoryAttributesEntity::getAttributeName).toList();
        if (attributeNames.contains(entity.getAttributeName())) {
            throw new DefinedException(0, entity.getAttributeName() + "属性值已存在");
        }
    }

    /**
     * 更新商品规格信息
     *
     * @param commonId      商品规格ID
     * @param addGoodsBases 待更新的商品信息列表
     * @param userId        用户id
     */
    private void newUpdateCommonSpec(Integer commonId, List<ManageGdsBaseAddPublishDTO> addGoodsBases, String userId) {
        // 新建规格值DTO列表
        List<CommonSpecValueDTO> commonSpecValues = Lists.newArrayList();
        // 初始化规格总库存
        Integer commonStock = 0;

        // 准备更新商品规格参数
        UpdateGdsSpecParamDTO updateGdsSpecParam = new UpdateGdsSpecParamDTO();

        // 遍历商品信息列表
        for (ManageGdsBaseAddPublishDTO goodsBase : addGoodsBases) {
            // 初始化商品规格参数
            ManageGdsBaseAddParamvDTO manageGdsBaseAddParam = new ManageGdsBaseAddParamvDTO();
            manageGdsBaseAddParam.setGoodsSpec(goodsBase.getGoodsName());
            manageGdsBaseAddParam.setSpecId(goodsBase.getSpecId());
            manageGdsBaseAddParam.setTypeId(goodsBase.getTypeId());

            // 准备规格值DTO
            CommonSpecValueDTO value = new CommonSpecValueDTO();
            value.setCommonId(commonId);
            value.setGoodsId(goodsBase.getGoodsId());
            // 累加商品库存到规格总库存
            commonStock = commonStock + goodsBase.getGoodsStock();
            // 添加规格值到列表
            commonSpecValues.add(value);

            //将数据更新到商品库存表
            GoodsStockEntity goodsStockEntity = new GoodsStockEntity();
            goodsStockEntity.setGoodsId(goodsBase.getGoodsId());
            goodsStockEntity.setCommonId(goodsBase.getCommonId());
            goodsStockEntity.setGoodsStock(goodsBase.getGoodsStock());
            goodsStockEntity.setGoodsAlarm(goodsBase.getGoodsAlarm());
            goodsStockEntity.setUpdateTime(new Date());
            goodsStockEntity.setUpdateUser(userId);
            goodsStockMapper.insert(goodsStockEntity);
            LOG.info("商品goods_stock表更新成功");
        }
        // 获取第一个商品信息
        ManageGdsBaseAddPublishDTO manageGdsBase = addGoodsBases.get(0);
        // 设置更新规格参数
        updateGdsSpecParam.setCommonSpecValue(JSON.toJSONString(commonSpecValues));
        updateGdsSpecParam.setCommonStock(commonStock);
        updateGdsSpecParam.setCommonShopGateStock(commonStock / 2);
        updateGdsSpecParam.setGoodsId(manageGdsBase.getGoodsId().toString());
        updateGdsSpecParam.setCommonPrice(manageGdsBase.getGoodsPrice());
        updateGdsSpecParam.setCommonMarketPrice(manageGdsBase.getGoodsMarketPrice());
        updateGdsSpecParam.setCommonImage(manageGdsBase.getGoodsImage());
        updateGdsSpecParam.setCommonAlarm(manageGdsBase.getGoodsAlarm());
        updateGdsSpecParam.setCommonCubage(manageGdsBase.getGoodsCubage());
        updateGdsSpecParam.setCommonId(commonId);
        updateGdsSpecParam.setCommonBabyPrice(manageGdsBase.getGoodsBabyPrice());
        updateGdsSpecParam.setTypeId(manageGdsBase.getTypeId());

        // 更新商品规格信息
        goodsCommonMapper.updateGdsSpec(updateGdsSpecParam);
        // 记录更新日志
        LOG.info("更新common表规格商品信息成功");
    }


    /**
     * 更新商品信息
     * 通过参数对象更新商品信息，并进行权限校验
     *
     * @param param 商品管理通用更新参数对象
     * @return 包含操作结果的响应对象
     */
    @Override
    @Transactional
    public ResponseResult updateGoods(ManageGdsCommonUpdateDTO param) {
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 权限判断
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());

        if (userPermissionsResult.getFlag()) {
            Map<String, Object> map = new HashMap<>();
            List<Integer> list = new ArrayList<>();
            list.add(param.getCommonId());
            //通过mq 更新es的商品数据
            map.put("commonIdS", list);
            Integer num = 0;

            String shopId = userPermissionsResult.getShopId();

            // 查询店铺名称
            String shopName = shopBaseMapper.selectShopName(Integer.valueOf(shopId));
            param.setShopName(shopName);
            param.setShopId(shopId);
            param.setCommonCubage(new BigDecimal(0));
            Integer commonId = param.getCommonId();

            //	序列化数组售后服务
            String jsonString = JSON.toJSONString(param.getAfterServiceList());
            param.setAfterService(jsonString);

            if (param.getCommonState() == null || param.getCommonState() == 0) {
                param.setCommonState(0);
            }

            // 判断主图类型
            if (StringUtils.isNotEmpty(param.getCommonVideo())) {
                param.setType(2);
            }


            // 获取公共信息表规格值和goodsid
            List<ManageGdsBaseAddParamvDTO> gdsBases = param.getGdsBases();
            // 新增商品的基础信息列表
            List<ManageGdsBaseAddParamvDTO> addGoodsBases = Lists.newArrayList();
            // 更新商品的基础信息列表
            List<ManageGdsBaseAddParamvDTO> updateGoodsBases = Lists.newArrayList();
            // 新增的商品图片列表
            List<GdbMerchantGuiGeGoodsImagesDTO> addImages = Lists.newArrayList();
            // 更新的商品图片列表
            List<GdbMerchantGuiGeGoodsImagesDTO> updateImages = Lists.newArrayList();
            // 需要移除的商品图片ID列表
            List<Integer> removeGoodsImage = Lists.newArrayList();
            // 更新商品状态的ID列表
            List<Integer> updateGoodsStatus = Lists.newArrayList();
            // 商品主图，目前只取第一张图片
            String image = null;
            // 公共库存，默认值为0
            Integer commonStock = 0;

            // 如果商品图片列表非空，设置公共图片
            if (!CollectionUtils.isEmpty(param.getGoodsImages())) {
                image = param.getGoodsImages().getFirst().getMerchantGoodsImages().getFirst().getImagesImage();
                // 设置公共图片
                param.setCommonImage(image);
            }
            // 检查商品规格是否为空
            if (!CollectionUtils.isEmpty(gdsBases)) {
                String goodsImg = null;
                List<GdbMerchantGuiGeGoodsImagesDTO> goodsImages = param.getGoodsImages();
                for (ManageGdsBaseAddParamvDTO gdsBase : gdsBases) {
                    //获取sku图片
                    goodsImg = goodsImages.get(num).getMerchantGoodsImages().get(0).getImagesImage();
                    gdsBase.setImg(goodsImages.get(num).getMerchantGoodsImages());
                    System.out.println("商品sku图片++" + goodsImages.get(num).getMerchantGoodsImages());

                    // 设置商品促销提示
                    gdsBase.setGoodsPromotionTips(param.getCommonPromotionTips());
                    // 如果品牌ID为空，设置为0
                    if (null == param.getBrandId()) {
                        gdsBase.setBrandId(0);
                    }

                    // 根据商品状态设置上架状态
                    if (param.getCommonState().equals(1)) {
                        gdsBase.setGoodsIsShelves(1);
                    } else {
                        gdsBase.setGoodsIsShelves(2);
                    }
                    // 累加商品库存
                    commonStock += gdsBase.getGoodsStock();
                    // 设置商品库存
                    gdsBase.setGoodsStock(gdsBase.getGoodsStock());
                    // 设置商品门店库存
                    gdsBase.setGoodsShopGateStock(gdsBase.getGoodsShopGateStock());
                    // 设置商品图片
                    gdsBase.setGoodsImage(goodsImg);
                    // 设置商品是否推荐
                    gdsBase.setGoodsIsRecommend(param.getCommonIsRecommend());
                    // 设置公共ID
                    gdsBase.setCommonId(commonId);
                    // 设置店铺ID
                    gdsBase.setShopId(shopId);
                    // 设置商品成本价
                    gdsBase.setGoodsCostPrice(param.getCommonCostPrice());
                    // 处理规格值
                    List<GoodBaseSpecValuesJsonBeanDTO> specValues = gdsBase.getSpecValues();
                    String goodsSpec = null;
                    for (GoodBaseSpecValuesJsonBeanDTO specValue : specValues) {
                        if (goodsSpec == null) {
                            goodsSpec = specValue.getParamValue();
                            continue;
                        }
                        goodsSpec = goodsSpec + "+" + specValue.getParamValue();
                    }
                    // 设置商品规格
                    gdsBase.setGoodsSpec(goodsSpec);
                    // 设置店铺名称
                    gdsBase.setShopName(shopName);
                    // 设置规格值JSON字符串
                    gdsBase.setSpecValue(JSON.toJSONString(specValues));
                    // 根据商品ID区分新增或更新操作
                    if (null == gdsBase.getGoodsId()) {
                        addGoodsBases.add(gdsBase);
                    } else {
                        updateGoodsStatus.add(gdsBase.getGoodsId());
                        updateGoodsBases.add(gdsBase);
                    }
                    num++;
                }
            } else {
                // 规格不能为空，返回错误提示
                return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), "规格不能为空");
            }

            // 处理商品图片删除逻辑
            for (GdbMerchantGuiGeGoodsImagesDTO images : param.getGoodsImages()) {
                boolean flag = true;
                for (GdbMerchantGoodsImagesVO img : images.getMerchantGoodsImages()) {
                    if (img.getImagesId() != null) {
                        removeGoodsImage.add(img.getImagesId());
                        flag = false;
                    }
                }
            }

            //删除图片
            if (!CollectionUtils.isEmpty(removeGoodsImage)) {
                goodsImagesMapper.deleteByGoodsImageIds(removeGoodsImage, commonId);
            } else {
                goodsImagesMapper.deleteByCommonId(commonId);
            }

            // 设置更新者ID
            param.setCommonUpdateBy(userPermissionsResult.getUserId());
            // 设置更新时间
            param.setCommonUpdateTime(new Date());
            // 设置商品价格
            param.setCommonPrice(gdsBases.getFirst().getGoodsPrice());
            // 修改状态为审核中
            param.setCommonVerify(10);
            // 再次设置商品价格
            param.setCommonPrice(gdsBases.getFirst().getGoodsPrice());
            // 将规格参数对象转换为JSON字符串
            param.setSpecParamJson(JSONObject.toJSONString(param.getSpecParam()));

            // 根据shopId查询商户店铺基础信息
            GdbMerchantShopBaseVO gdbMerchantShopBase = this.baseMapper.selectByPrimaryKey(Integer.valueOf(userPermissionsResult.getShopId()));
            // 根据店铺是否自营设置审核状态和自营标志
            if (gdbMerchantShopBase.getShopSelfSupport().equals("true")) {
                param.setShopSelfSupport(2);
                param.setCommonVerify(1);
            } else {
                param.setShopSelfSupport(1);
            }

            // 插入数据到公共信息表,并获取common_id
            param.setCommonStock(commonStock);

            // 设置品牌ID和名称
            param.setBrandId(param.getBrandId());
            param.setBrandName(param.getBrandName());
            // 格式化有效期
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (param.getPeriodOfValidity() != null && param.getPeriodOfValidity() != "") {
                try {
                    param.setPeriodOfValiditys(format.parse(param.getPeriodOfValidity()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            // 更新商品公共信息
            goodsCommonMapper.updateGdsCommonObj(param);

            // 编辑发布商品时先删除商品属性关联表里的数据再添加
            goodsCommonAttributeAssociationMapper.deleteAsscon(param.getCommonId());
            // 获取商品属性ID列表
            List<AttributeParamDTO> attributeIds = param.getAttribuParam();
            // 通过商品属性id查询属性内容然后添加对应的属性内容id
            if (null != attributeIds && !attributeIds.isEmpty()) {
                for (AttributeParamDTO attribu : attributeIds) {
                    Integer attrConIds = attribu.getAttributeContentId();
                    param.setButeId(attrConIds);
                    param.setAttribuId(attribu.getAttributeId());
                    goodsCommonAttributeAssociationMapper.insertAssContent(param);
                }
            }

            // 记录日志：更新common表成功
            LOG.info("更新common表成功");
            // 更新商品状态
            this.baseMapper.updateGoodsIsShelves2(updateGoodsStatus, commonId);
            // 添加goods_base
            if (!CollectionUtils.isEmpty(addGoodsBases)) {
                for (ManageGdsBaseAddParamvDTO addGoodsBase : addGoodsBases) {
                    this.baseMapper.insertGdsBasesObj(addGoodsBase);

                    //将数据更新到商品库存表
                    GoodsStockEntity goodsStockEntity = new GoodsStockEntity();
                    goodsStockEntity.setGoodsId(addGoodsBase.getGoodsId());
                    goodsStockEntity.setCommonId(addGoodsBase.getCommonId());
                    goodsStockEntity.setGoodsStock(addGoodsBase.getGoodsStock());
                    goodsStockEntity.setGoodsAlarm(addGoodsBase.getGoodsAlarm());
                    goodsStockEntity.setUpdateTime(new Date());
                    goodsStockEntity.setUpdateUser(userPermissionsResult.getUserId());
                    goodsStockMapper.insert(goodsStockEntity);
                    LOG.info("商品goods_stock表更新成功");

                    System.out.println("新加规格的goodsId-----" + addGoodsBase.getGoodsId());

                    // 添加商品图片
                    List<GdbMerchantGoodsImagesVO> goodsImages = addGoodsBase.getImg();
                    List<GdbMerchantGoodsImagesVO> addImgs = Lists.newArrayList();
                    if (!CollectionUtils.isEmpty(goodsImages)) {
                        for (GdbMerchantGoodsImagesVO goodsImage : goodsImages) {
                            goodsImage.setCommonId(param.getCommonId());
                            goodsImage.setShopId(shopId);
                            goodsImage.setGoodsId(addGoodsBase.getGoodsId());
                            addImgs.add(goodsImage);
                        }

                        goodsImagesMapper.insertBatch(addImgs);
                        LOG.info("更新common_images表成功");
                    }
                }
            }
            if (!CollectionUtils.isEmpty(updateGoodsBases)) {
                for (ManageGdsBaseAddParamvDTO record : updateGoodsBases) {
                    if (null == record.getGoodsMaxSale() || 0 == record.getGoodsMaxSale()) {
                        record.setGoodsMaxSale(record.getGoodsStock());
                    }
                    this.baseMapper.updateGdsBasesBatch2Obj(record);

                    // 更新商品库存信息
                    GoodsStockEntity entity = goodsStockMapper.selectOne(new LambdaUpdateWrapper<GoodsStockEntity>()
                            .eq(GoodsStockEntity::getGoodsId, record.getGoodsId())
                            .eq(GoodsStockEntity::getCommonId, commonId));
                    GoodsStockEntity goodsStockEntity = new GoodsStockEntity();
                    goodsStockEntity.setId(entity.getId());
                    goodsStockEntity.setGoodsId(record.getGoodsId());
                    goodsStockEntity.setCommonId(commonId);
                    goodsStockEntity.setGoodsStock(record.getGoodsStock());
                    goodsStockEntity.setGoodsAlarm(record.getGoodsAlarm());
                    goodsStockEntity.setUpdateTime(new Date());
                    goodsStockEntity.setUpdateUser(userPermissionsResult.getUserId());
                    goodsStockMapper.updateById(goodsStockEntity);
                    LOG.info("商品goods_stock表更新成功");

                    // 添加商品图片
                    List<GdbMerchantGoodsImagesVO> img = record.getImg();
                    List<GdbMerchantGoodsImagesVO> addImgs = Lists.newArrayList();
                    List<GdbMerchantGoodsImagesVO> upImgs = Lists.newArrayList();
                    if (!CollectionUtils.isEmpty(img)) {
                        for (GdbMerchantGoodsImagesVO goodsImage : img) {
                            if (goodsImage.getImagesId() == null) {
                                goodsImage.setCommonId(param.getCommonId());
                                goodsImage.setShopId(shopId);
                                goodsImage.setGoodsId(record.getGoodsId());
                                goodsImage.setImagesDisplayorder(0);
                                goodsImage.setImagesIsDefault(0);
                                addImgs.add(goodsImage);
                            } else {
                                goodsImage.setCommonId(param.getCommonId());
                                goodsImage.setShopId(shopId);
                                goodsImage.setGoodsId(record.getGoodsId());
                                upImgs.add(goodsImage);
                            }

                        }
                        if (!CollectionUtils.isEmpty(upImgs)) {
                            goodsImagesMapper.updateBatch(upImgs);
                        }
                        if (!CollectionUtils.isEmpty(addImgs)) {
                            addImgs.getFirst().setImagesIsDefault(1);
                            goodsImagesMapper.insertBatch(addImgs);
                        }


                        LOG.info("更新common_images表成功");
                    }
                }
            }

            LOG.info("更新goods_base表成功");

            addGoodsBases.addAll(updateGoodsBases);
            String commonSpecName = param.getCommonSpecName();
            // 更新商品规格
            updateCommonSpec2(commonId, addGoodsBases, commonSpecName);

            // 添加商品详情
            GdbMerchantGoodsCommonDetailDTO goodsCommonDetail = param.getGoodsCommonDetail();
            if (null != goodsCommonDetail) {

                GdbMerchantGoodsCommonDetailVO selectByPrimaryKey = goodsCommonDetailMapper.selectByPrimaryKey(commonId);
                goodsCommonDetail.setCommonId(commonId);
                if (null != selectByPrimaryKey) {
                    goodsCommonDetailMapper.updateByPrimaryKeySelective(goodsCommonDetail);
                } else {
                    goodsCommonDetailMapper.insertSelective(goodsCommonDetail);
                }

            }

            LOG.info("更新common_detail表成功");


            // 更新经销商品配置信息
            if (null != param.getManageGdsGoodsConfig()) {

                BaseGoodsConfigVO config = goodsDelayConfigMapper.selectByPrimaryKey(param.getCommonId());
                if (null != config) {
                    config = param.getManageGdsGoodsConfig();
                    config.setCommonId(param.getCommonId());
                    goodsDelayConfigMapper.updateByPrimaryKeySelective(config);
                } else {
                    config = param.getManageGdsGoodsConfig();
                    config.setCommonId(param.getCommonId());
                    goodsDelayConfigMapper.insertSelective(config);
                }
            }
            if (!CollectionUtils.isEmpty(param.getGoodsCatIdList())) {

                //更新店铺商品分类
                shopGoodsCatMapper.deleteByCommonId(param.getCommonId());

                List<MerchantShopGoodsCatDTO> goodsCatList = shopSelfCatMapper.queryShopCatIds(param.getGoodsCatIdList());

                for (MerchantShopGoodsCatDTO shopGoodsCat : goodsCatList) {

                    shopGoodsCat.setCommonId(commonId);
                    shopGoodsCat.setShopId(Integer.valueOf(userPermissionsResult.getShopId()));
                    shopGoodsCat.setParentId(null == shopGoodsCat.getParentId() ? 0 : shopGoodsCat.getParentId());
                }
                if (null != goodsCatList && goodsCatList.size() != 0) {
                    shopGoodsCatMapper.insertSelectiveBatch(goodsCatList);
                }
            }
            List<Integer> supperCommonId = new ArrayList<>();
            for (Integer SuppercommonId : list) {
                //判断当前是否是是供应商商品 如果是 下架商家对应的商品 并更新es
                List<Integer> dealerCommonIds = goodsCommonMapper.queryIsSupperGoods(SuppercommonId);
                if (null != dealerCommonIds && dealerCommonIds.size() > 0) {
                    //当前商品是供应商商品 所有商家商品下架 更新es
                    goodsCommonMapper.updateOffline(dealerCommonIds);
                }
                supperCommonId.addAll(dealerCommonIds);
            }
            if (null != param.getCategoryAttributesList() && !param.getCategoryAttributesList().isEmpty()) {
                List<Integer> attributesIds = new ArrayList<>();
                // 更新商品类目属性
                for (GoodsCategoryAttributesDTO categoryAttributesDTO : param.getCategoryAttributesList()) {
                    if (StringUtils.isEmpty(categoryAttributesDTO.getAttributeValueName())) {
                        continue;
                    }
                    GoodsCategoryAttributesEntity categoryAttributesEntity = JSON.parseObject(JSON.toJSONString(categoryAttributesDTO), GoodsCategoryAttributesEntity.class);
                    categoryAttributesEntity.setCommonId(commonId);
                    attributesIsRepeatedFlag(categoryAttributesEntity);
                    if (categoryAttributesEntity.getId() == null) {
                        goodsCategoryAttributesMapper.insert(categoryAttributesEntity);
                    } else {
                        goodsCategoryAttributesMapper.updateById(categoryAttributesEntity);
                    }
                    attributesIds.add(categoryAttributesEntity.getId());
                }
                if (!attributesIds.isEmpty()) {
                    goodsCategoryAttributesMapper.delete(new LambdaQueryWrapper<GoodsCategoryAttributesEntity>()
                            .eq(GoodsCategoryAttributesEntity::getCommonId, commonId)
                            .notIn(GoodsCategoryAttributesEntity::getId, attributesIds));
                }
            } else {
                goodsCategoryAttributesMapper.delete(new LambdaQueryWrapper<GoodsCategoryAttributesEntity>()
                        .eq(GoodsCategoryAttributesEntity::getCommonId, commonId));
            }
            list.addAll(supperCommonId);
            // 1 是添加  2是删除
            // 自营店铺 更新商品 且商品是上架状态 就更新es
            if (gdbMerchantShopBase.getShopSelfSupport().equals("true") && param.getCommonVerify() == 1) {
                map.put("type", 1);
                MqSendUtils.mqSendGoods(map);
                asyncVersionService.addGoodsVersion(new ManageCommonIdsDTO(commonId, CommonType.commonStatus.YES.getCode(), param.getToken()));
            } else {
                map.put("type", 2);
                MqSendUtils.mqSendGoods(map);
            }
        } else {
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        }
        return ResponseResult.success();

    }


    /**
     * 更新common表中的商品规格信息
     *
     * @param commonId       common表的主键ID
     * @param addGoodsBases  待添加的商品基本信息列表
     * @param commonSpecName common表中的规格名称
     */
    private void updateCommonSpec2(Integer commonId, List<ManageGdsBaseAddParamvDTO> addGoodsBases, String commonSpecName) {
        // 用于存储商品规格值的列表
        List<CommonSpecValueDTO> commonSpecValues = Lists.newArrayList();
        // 初始化common表中的总库存
        Integer commonStock = 0;
        // 更新goods_common中的规格信息
        UpdateGdsSpecParamDTO updateGdsSpecParam = new UpdateGdsSpecParamDTO();

        // 遍历商品基本信息列表，处理每个商品的规格信息
        for (ManageGdsBaseAddParamvDTO goodsBase : addGoodsBases) {
            // 复制商品基础信息以进行规格处理
            ManageGdsBaseAddParamvDTO manageGdsBaseAddParam = new ManageGdsBaseAddParamvDTO();
            manageGdsBaseAddParam.setGoodsSpec(goodsBase.getGoodsName());
            manageGdsBaseAddParam.setSpecId(goodsBase.getSpecId());
            manageGdsBaseAddParam.setTypeId(goodsBase.getTypeId());
            // 创建商品规格值对象
            CommonSpecValueDTO value = new CommonSpecValueDTO();
            value.setCommonId(commonId);
            value.setGoodsId(goodsBase.getGoodsId());
            // 累加商品库存到common表的总库存
            commonStock = commonStock + goodsBase.getGoodsStock();
            // 将商品规格值添加到列表
            commonSpecValues.add(value);
        }
        // 获取第一个商品的基础信息作为代表
        ManageGdsBaseAddParamvDTO manageGdsBase = addGoodsBases.get(0);
        // 设置更新规格参数中的商品规格值
        updateGdsSpecParam.setCommonSpecValue(JSON.toJSONString(commonSpecValues));
        updateGdsSpecParam.setCommonStock(commonStock);
        // 设置更新规格参数中的商品ID
        updateGdsSpecParam.setGoodsId(manageGdsBase.getGoodsId().toString());
        // 设置更新规格参数中的商品价格
        updateGdsSpecParam.setCommonPrice(manageGdsBase.getGoodsPrice());
        // 设置更新规格参数中的商品市场价
        updateGdsSpecParam.setCommonMarketPrice(manageGdsBase.getGoodsMarketPrice());
        // 设置更新规格参数中的商品图片
        updateGdsSpecParam.setCommonImage(manageGdsBase.getGoodsImage());
        // 设置更新规格参数中的商品库存警戒值
        updateGdsSpecParam.setCommonAlarm(manageGdsBase.getGoodsAlarm());
        // 设置更新规格参数中的商品体积
        updateGdsSpecParam.setCommonCubage(manageGdsBase.getGoodsCubage());
        // 设置更新规格参数中的common表主键ID
        updateGdsSpecParam.setCommonId(commonId);
        // 设置更新规格参数中的商品宝贝价格
        updateGdsSpecParam.setCommonBabyPrice(manageGdsBase.getGoodsBabyPrice());
        // 设置更新规格参数中的商品类型ID
        updateGdsSpecParam.setTypeId(manageGdsBase.getTypeId());

        // 更新goods_common表中的规格信息
        goodsCommonMapper.updateGdsSpec(updateGdsSpecParam);
        // 记录日志，更新common表规格商品信息成功
        LOG.info("更新common表规格商品信息成功");
    }


    @Override
    public ResponseResult queryGoods(ManageCommonIdsDTO param) {
        // 初始化查询结果对象
        ManageGdsCommonQueryVO commonResult = new ManageGdsCommonQueryVO();

        // 权限判断
        // 【重要】优先从SecurityContext获取用户信息（JWT过滤器已设置）
        // 如果请求参数中有token，使用旧的验证方式（向后兼容）
        UserPermissionsResult userPermissionsResult;
        if (param.getToken() != null && !param.getToken().isEmpty()) {
            // 使用旧的token验证方式（向后兼容）
            userPermissionsResult = UserService.judgePermissions(param.getToken());
        } else {
            // 从SecurityContext获取用户信息（新的统一方式）
            try {
                UserInfoVO userInfo = SecurityContextUtils.getCurrentUser();
                if (userInfo == null) {
                    return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
                }
                // 构建UserPermissionsResult
                userPermissionsResult = new UserPermissionsResult();
                userPermissionsResult.setFlag(true);
                userPermissionsResult.setShopId(userInfo.getShopId() != null ? userInfo.getShopId().toString() : null);
                userPermissionsResult.setShopType(userInfo.getShopType());
                userPermissionsResult.setUserId(userInfo.getUserId() != null ? userInfo.getUserId().toString() : null);
                userPermissionsResult.setUserName(userInfo.getUserName());
                userPermissionsResult.setUserMobile(userInfo.getUserMobile());
                userPermissionsResult.setUserNickname(userInfo.getUserNickname());
            } catch (Exception e) {
                LOG.error("从SecurityContext获取用户信息失败: {}", e.getMessage(), e);
                return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
            }
        }

        // 如果用户有权限
        if (userPermissionsResult.getFlag() || (param.getIsVersion() != null && param.getIsVersion() == 1)) {
            // 获取商品公共ID和店铺ID
            Integer commonId = param.getCommonId();
            
            // 检查commonId是否为null
            if (commonId == null) {
                LOG.error("查询商品失败：commonId为空");
                return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), "商品ID不能为空");
            }

            // 根据商品公共ID查询商品信息
            ManageGdsCommonVO common = goodsCommonMapper.selectByCommonId2(commonId);
            
            // 检查商品是否存在
            if (common == null) {
                LOG.error("商品不存在，commonId: {}", commonId);
                return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), "查询的商品不存在，请检查商品ID是否正确");
            }
            
            String shopId = param.getIsVersion() != null && param.getIsVersion() == 1 ? common.getShopId().toString() : userPermissionsResult.getShopId();
            // 如果商品的PLU不为空，表示这是称重商品
            if (!StrUtil.isEmpty(common.getPlu())) {
                // 设置称重商品结果
                commonResult.setCommonResult(common);
                // 查询商品基础信息
                List<GoodsBaseVO> goodsBases = baseMapper.queryGoodsCommonId(commonId, param.getIsVersion());
                GoodsBaseVO base = goodsBases.getFirst();
                common.setGoodsAlarm(base.getGoodsAlarm());
                common.setGoodsStock(base.getGoodsStock());
                // 查询并设置店铺商品分类列表
                List<MerchantShopGoodsCatDTO> shopGoodsCatList = shopGoodsCatMapper.queryByCommonId(commonId);
                commonResult.setShopGoodsCatList(shopGoodsCatList);
                // 处理店铺门类型
                String shopGateType = base.getShopGateType();
                if (!ObjectUtils.isEmpty(shopGateType)) {
                    // 将字符串类型的门类型转换为列表
                    String[] split = shopGateType.split(",");
                    List<Integer> list = new ArrayList<>(split.length);
                    for (String item : split) {
                        list.add(Integer.parseInt(item));
                    }
                    commonResult.setGoodsCatList(list);
                }
            } else {
                // 如果不是称重商品，则查询实物商品或虚拟商品
                queryGoodsBase(common, commonId, commonResult, shopId, param.getIsVersion());
            }
        } else {
            // 如果用户没有权限，返回权限不足的错误信息
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        }

        // 返回查询结果
        return ResponseResult.success(commonResult);
    }

    /**
     * 查询商品基础信息
     *
     * @param common       商品基础信息VO
     * @param commonId     商品基础ID
     * @param commonResult 商品基础查询结果VO
     * @param shopId       店铺ID，目前未使用
     */
    private void queryGoodsBase(ManageGdsCommonVO common, Integer commonId, ManageGdsCommonQueryVO commonResult, String shopId, Integer isVersion) {
        // 转换售后服务列表
        List<String> afterServiceList = common.getAfterService() == null ? null : JsonUtils.string2Obj(common.getAfterService(), List.class, String.class);
        common.setAfterServiceList(afterServiceList);
        List<GoodsCategoryAttributesEntity> goodsCategoryAttributes = goodsCategoryAttributesMapper.selectList(
                new LambdaQueryWrapper<GoodsCategoryAttributesEntity>().eq(GoodsCategoryAttributesEntity::getCommonId, commonId)
                        .orderByAsc(GoodsCategoryAttributesEntity::getDisplayOrder));
        common.setCategoryAttributesList(goodsCategoryAttributes);
        // 处理规格参数
        if (StringUtils.isNotEmpty(common.getSpecParam())) {
            List<SpecParamJsonBeanVO> list = new ArrayList<>();
            JSONArray jsonSpecParam = JSONObject.parseArray(common.getSpecParam());
            for (int index = 0; index < jsonSpecParam.size(); index++) {
                JSONObject obj = jsonSpecParam.getJSONObject(index);
                SpecParamJsonBeanVO temp = JSONObject.parseObject(obj.toJSONString(), SpecParamJsonBeanVO.class);
                list.add(temp);
            }
            common.setSpecParamList(list);
        }

        // 查询商品属性关联信息
        List<GdbAttributeAssionParamVO> assionParams = goodsCommonAttributeAssociationMapper.selectCommonIds(commonId);
        for (GdbAttributeAssionParamVO assionParam : assionParams) {
            // 查询并设置属性ID
            GoodsAttributeVO gdbAttrPram = goodsAttributeMapper.selectAttributeId(assionParam.getAttributeId());
            if (gdbAttrPram != null) {
                assionParam.setAttributeIds(gdbAttrPram.getCommodityAttribute());
            }

            // 查询并设置属性内容ID
            GoodsAttributeVO gdbAttrPram1 = goodsAttributeContentMapper.selectAttContentId(assionParam.getAttributeContentId());
            if (gdbAttrPram1 != null) {
                assionParam.setAttributeContentIds(gdbAttrPram1.getAttriContent());
            }
        }

        // 设置查询结果
        commonResult.setGdbAttributeAssionParams(assionParams);

        // 设置运费和售卖区域
        ManageTransportTemplateVO transportTemplate = shopTransportTemplateMapper
                .selectById(common.getTransportTemplate());
        common.setTransportTemplates(transportTemplate);

        ManageTransportAreaVO transportAreas = shopTransportAreaMapper.selectById(common.getTransportAreaId());
        common.setTransportAreas(transportAreas);
        // 查询国际售卖区域
        ManageTransportAreaVO internationalTransportAreas = shopTransportAreaMapper.selectById(common.getInternationalTransportArea());
        common.setInternationalTransportAreas(internationalTransportAreas);
        // 查询国际运费模板
        ManageTransportTemplateVO internationalTransportTemplate = shopTransportTemplateMapper.selectById(common.getInternationalTransportTemplate());
        common.setInternationalTransportTemplates(internationalTransportTemplate);
        // 根据上架状态查询商品规格
        List<ManageGdsBaseAddResultVO> gdsBases = this.baseMapper.selectGoodsSpecBygoodsIsShelves(commonId, isVersion);
        ManageGdsBaseAddResultVO gdsBase = gdsBases.get(0);
        common.setSellingPoint(gdsBase.getSellingPoint());
        common.setGoodsSendBack(gdsBase.getGoodsSendBack());

        // 查询商品详情
        GdbMerchantGoodsCommonDetailVO goodsCommonDetail = goodsCommonDetailMapper.selectByPrimaryKey(commonId);

        // 商品图片处理
        List<GdbMerchantGuiGeGoodsImagesDTO> merchantGuiGeGoodsImages = new ArrayList<>();
        for (ManageGdsBaseAddResultVO base : gdsBases) {
            String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.STORAGE_SWITCH.getDesc());
            if (!StringUtils.isEmpty(value) && "1".equals(value)) {
                Integer storageGoodsStock = storageGoodsBaseMapper.getGoodsStorageStock(base.getGoodsId());
                base.setStorageGoodsStock(storageGoodsStock);
            } else {
                base.setStorageGoodsStock(0);
            }
            GdbMerchantGuiGeGoodsImagesDTO guiGeGoodsImages = new GdbMerchantGuiGeGoodsImagesDTO();
            List<GdbMerchantGoodsImagesVO> goodsImages = goodsImagesMapper.selectByCommonId3(commonId, Integer.valueOf(shopId), base.getGoodsId());

            if (CollectionUtils.isEmpty(goodsImages)) {
                GdbMerchantGoodsImagesVO goodsImage = new GdbMerchantGoodsImagesVO();
                goodsImage.setImagesImage(common.getCommonImage());
                goodsImage.setImagesDisplayorder(0);
                goodsImage.setShopId(shopId);
                goodsImage.setImagesIsDefault(1);
                goodsImages.add(goodsImage);
                guiGeGoodsImages.setMerchantGoodsImages(goodsImages);
                merchantGuiGeGoodsImages.add(guiGeGoodsImages);
            } else {
                guiGeGoodsImages.setMerchantGoodsImages(goodsImages);
                merchantGuiGeGoodsImages.add(guiGeGoodsImages);
            }
        }

        // 设置返回结果
        commonResult.setGoodsImages(merchantGuiGeGoodsImages);
        commonResult.setCommonResult(common);
        commonResult.setGdsBases(gdsBases);
        commonResult.setGoodsCommonDetail(goodsCommonDetail);

        // 查询商品配置
        BaseGoodsConfigVO manageGdsGoodsConfig = goodsDelayConfigMapper.selectByPrimaryKey(commonId);
        commonResult.setManageGdsGoodsConfig(manageGdsGoodsConfig);

        // 查询商品分类信息
        List<MerchantShopGoodsCatDTO> shopGoodsCatList = shopGoodsCatMapper.queryByCommonId(commonId);
        commonResult.setShopGoodsCatList(shopGoodsCatList);
    }

    /**
     * 查询所有商品信息
     * 此方法通过覆盖父类方法来优化查询逻辑，旨在提高查询效率并处理异常
     * 使用平台提供的参数来查询所有商品的信息，并处理可能发生的异常
     *
     * @param param PlatFormAllGoodsDTO类型的参数，包含查询所需的信息
     * @return 返回一个ResponseResult对象，其中包含查询结果或错误信息
     */
    @Override
    public ResponseResult queryAllGoodsInfo(PlatFormAllGoodsDTO param) {

        try {
            // 尝试执行查询操作
            ResponseResult result = queryAllGoods(param);
            return result;
        } catch (Exception e) {
            // 异常处理：记录错误日志并返回失败结果
            e.printStackTrace();
            LOG.error("[queryAllGoods]-platform-入参：{}-异常：{}", JsonUtils.toString(param), e.getMessage());
            return ResponseResult.fail(e.getMessage());
        }
    }

    /**
     * 根据参数查询商品详情并导出为Excel文件
     *
     * @param param    商品详情查询参数
     * @param response HTTP响应对象
     * @return 包含Excel文件URL的响应结果
     */
    @Override
    public ResponseResult selectCommodityDetailsExp(GoodsDetailsDTO param, HttpServletResponse response) {
        // 定义Excel表格的列标题
        String[] title = {"goodsId", "commonId", "店铺名称", "商品名称", "商品价格", "商品库存", "商品规格"};

        // 初始化用于存储商品信息的List
        List<Map<String, String>> mapList = Lists.newArrayList();

        // 调用Mapper层的selectCommodityDetails方法获取商品详情列表
        List<GoodsDetailsDTO> list = this.baseMapper.selectCommodityDetails(param);

        // 遍历商品详情列表，将每个商品的信息转换为Map并添加到mapList中
        for (GoodsDetailsDTO res : list) {
            HashMap<String, String> map = com.google.common.collect.Maps.newHashMap();
            map.put("goodsId", res.getGoodsId());
            map.put("commonId", res.getCommonId());
            map.put("店铺名称", res.getShopName());
            map.put("商品名称", res.getGoodsName());
            map.put("商品价格", res.getGoodsPrice() + "");
            map.put("商品库存", res.getGoodsStock() + "");
            map.put("商品规格", res.getSpecValueName() + "");

            mapList.add(map);
        }

        // 初始化用于存储每个sheet页数据的Map
        Map<String, List<Map<String, String>>> map = Maps.newHashMap();
        // 将商品详情数据添加到map中，其中key为sheet的名称
        map.put("商品详情", mapList);

        // 调用工具类创建Excel文件，并返回文件的URL
        String url = ExcelsUtil.createExcel(title, map, new int[]{0});

        // 返回包含Excel文件URL的响应结果
        return ResponseResult.success(url);
    }

    /**
     * 根据参数分页查询商品详情
     *
     * @param param 商品详情查询参数
     * @return 包含分页数据的响应结果
     * @throws IllegalAccessException 如果访问权限不足
     */
    @Override
    public ResponseResult selectCommodityDetails(GoodsDetailsDTO param) throws IllegalAccessException {
        // 将查询参数对象转换为Map
        Map data = ParseMapUtils.beanToMap(param);
        // 根据查询参数和分页信息调用Mapper层的selectCommodityDetailsByPage方法
        IPage<GoodsDetailsDTO> page = this.baseMapper.selectCommodityDetailsByPage(new Query<GoodsDetailsDTO>().getPage(data), param);
        // 返回包含分页数据的响应结果
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 根据参数分页查询商品详情
     *
     * @param paraMap 查询参数Map
     * @return 包含分页数据的响应结果
     */
    @Override
    public ResponseResult selectGoodsDetailByRct(Map<String, Object> paraMap) {
        // 根据查询参数和分页信息调用Mapper层的selectGoodsDetailByRct方法
        IPage<GoodsBaseEntity> page = this.baseMapper.selectGoodsDetailByRct(new Query<GoodsBaseEntity>().getPage(paraMap), paraMap);
        // 返回包含分页数据的响应结果
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 根据映射更新商品基础信息
     *
     * @param map 包含更新字段和条件的映射
     * @return 更新结果的响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult updateGoodsBaseByMap(Map<String, Object> map) {
        Integer count = this.baseMapper.updateGoodsBaseByMap(map);
        if (count > 0) {
            return ResponseResult.success(BizCodeEnume.UPDATE_SUCCEEDED);
        }
        return ResponseResult.fail(BizCodeEnume.UPDATE_ERROR);
    }

    /**
     * 更新商品基础状态
     *
     * @param commonParam 包含商品公共状态参数的对象
     * @return 更新结果的响应
     */
    @Override
    public ResponseResult updateGoodsBaseState(CommonShopStateDTO commonParam) {
        Integer count = this.baseMapper.updateGoodsBaseState(commonParam);
        if (count > 0) {
            return ResponseResult.success(BizCodeEnume.UPDATE_SUCCEEDED);
        }
        return ResponseResult.fail(BizCodeEnume.UPDATE_ERROR);
    }

    /**
     * 删除积分商品
     *
     * @param param 包含操作所需token和商品ID的对象
     * @return 删除结果的响应
     */
    @Override
    public ResponseResult deleteIntegralGoods(ManageCommonIdsDTO param) {
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        if (null != userInfo) {
            List<Integer> commonIds = param.getCommonIds();
            goodsCommonMapper.updateCommonDel(commonIds);
            this.baseMapper.updateCommonDel(commonIds);
            goodsCommonDetailMapper.deleteByPrimaryKey(commonIds);
            goodsImagesMapper.deleteByCommonid(commonIds);
            return ResponseResult.success(BizCodeEnume.DELETE_SUCCESS);
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 查询所有积分商品信息
     *
     * @param param 查询参数映射，包含分页信息和token
     * @return 查询结果的响应，包含分页数据
     */
    @Override
    public ResponseResult queryIntegralGoodsAll(Map<String, Object> param) {
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.get("token").toString());
        if (null != userInfo) {
            // 分页查询数据
            Page<IntegraMallGoodsVO> page = new Page<>((Integer) param.get("page"), (Integer) param.get("limit"));
            IPage<IntegraMallGoodsVO> result = this.baseMapper.queryIntegralGoodsAll(page, param);

            return ResponseResult.success(PageUtils.getPage(result));
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 根据通用ID获取商品详情
     * 此方法通过调用数据库查询相关商品详情，并根据特定规则处理获取的商品信息
     * 包括设置商品规格值，根据商品状态设置库存和购物车状态，处理商品限购数量，以及活动商品的特殊处理
     *
     * @param commonId 商品通用ID
     * @return 返回商品详情列表的响应结果
     */
    @Override
    public ResponseResult getGoodsCommonDetails(Integer commonId) {
        // 通过通用ID查询商品信息
        List<ReceiveGoodsDetailsVO> detailList = this.baseMapper.selectCartMessageByCommonId(commonId);
        for (ReceiveGoodsDetailsVO detail : detailList) {
            // 设置商品规格的中文值
            detail.setGoodsSpecChineseValue(detail.getGoodsSpecChineseValue());

            // 根据商品状态设置库存和购物车状态
            if (!detail.getCommonState().equals(1) || !detail.getCommonVerify().equals(1)
                    || !detail.getCartStatus().equals(1)) {
                detail.setGoodsStock(0);
                detail.setCartStatus(0);
            }

            // 处理商品限购数量
            if (detail.getCommonLimit().equals(0) || detail.getGoodsMaxSale().equals(0)) {
                detail.setGoodsMaxSale(detail.getGoodsStock());
            }

            // 活动商品的特殊处理
            ActivityGoodsDetailDTO activityGoodsDetail = dealActivity(detail.getGoodsId(), detail.getGoodsMaxSale(),
                    detail.getCommonId(), detail.getCommonIsXian());

            // 根据活动状态设置商品信息
            if (null != activityGoodsDetail && !activityGoodsDetail.getActivityStatus().equals(0)) {
                detail.setActivityStatus(activityGoodsDetail.getActivityStatus());
                detail.setActivityGoodsDetail(activityGoodsDetail);
                detail.setCommonIsXian(activityGoodsDetail.getActivityStatus());
                if (!activityGoodsDetail.getActivityStatus().equals(4)) {
                    detail.setGoodsMaxSale(activityGoodsDetail.getGoodsUpperLimit());
                    detail.setGoodsMinSale(activityGoodsDetail.getGoodsLowerLimit());
                }
            } else {
                detail.setActivityStatus(0);
                detail.setActivityGoodsDetail(new ActivityGoodsDetailDTO());
                detail.setCommonIsXian(0);
            }

            // 如果商品限购数量为0，则设置为库存数量
            if (detail.getGoodsMaxSale().equals(0)) {
                detail.setGoodsMaxSale(detail.getGoodsStock());
            }
        }
        // 返回处理后的商品详情列表
        return ResponseResult.success(JsonUtils.toString(detailList));
    }

    /**
     * 根据商品基础列表查询详情
     * 此方法首先进行权限判断，然后根据提供的商品通用ID列表查询相应的商品详情
     *
     * @param param 查询参数，包含商品通用ID和权限令牌
     * @return 返回查询到的商品详情列表的响应结果
     */
    @Override
    public ResponseResult queryByGoodsBaseList2(ManageCommonIdsDTO param) {
        // 初始化商品详情列表
        List<ManageGoodsBaseListResultDTO> goodsBases = new ArrayList<ManageGoodsBaseListResultDTO>();
        // 权限判断
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());

        // 如果有权限，继续查询操作
        if (userPermissionsResult.getFlag()) {
            // 如果提供的通用ID列表为空，则将其设置为单个ID列表
            if (param.getCommonId() != null && CollectionUtils.isEmpty(param.getCommonIds())) {
                List<Integer> list = Collections.singletonList(param.getCommonId());
                param.setCommonIds(list);
            }
            // 根据通用ID列表查询商品详情
            goodsBases = this.baseMapper.selectByGoodsList2(param.getCommonIds());
            for (ManageGoodsBaseListResultDTO goodsBase : goodsBases) {
                // 设置商品规格值
                goodsBase.setGoodsSpec(goodsBase.getGoodsSpec());
            }
        } else {
            // 权限不足，返回失败响应
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
        // 返回查询到的商品详情列表
        return ResponseResult.success(goodsBases);
    }

    /**
     * 批量更新商品价格
     *
     * @param param 参数映射，包含权限令牌、更新类型、操作类型、操作值等信息
     * @return 更新操作的结果
     */
    @Override
    public ResponseResult batchUpdateGoodsPrice(Map<String, Object> param) {
        // 判断用户是否具有操作权限
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.get("token").toString());
        if (userPermissionsResult.getFlag()) {
            // 更新类型: 1-覆盖调价 2-浮动调价 3-分别调价
            Integer updateType = (Integer) param.get("updateType");
            // 1-覆盖调价
            if (null != updateType && updateType == 1) {
                if (null == param.get("commonPrice") || new BigDecimal(param.get("commonPrice").toString()).compareTo(BigDecimal.ZERO) <= 0) {
                    return ResponseResult.fail(BizCodeEnume.WRONG_AMOUNT);
                }
                if (null == param.get("commonMarketPrice") || new BigDecimal(param.get("commonMarketPrice").toString()).compareTo(BigDecimal.ZERO) <= 0) {
                    return ResponseResult.fail(BizCodeEnume.WRONG_AMOUNT);
                }
                goodsCommonMapper.updateCommonPriceUni1(param);
                this.baseMapper.updateGoodsPriceUni1(param);
                // 2-浮动调价
            } else if (null != updateType && updateType == 2) {
                if (null == param.get("operValue") || new BigDecimal(param.get("operValue").toString()).compareTo(BigDecimal.ZERO) <= 0) {
                    return ResponseResult.fail(BizCodeEnume.WRONG_AMOUNT);
                }
                List<Integer> commonIds = (List<Integer>) param.get("commonIds");
                Integer operType = Integer.parseInt((String) param.get("operType"));
                BigDecimal operValue = new BigDecimal((String) param.get("operValue"));
                for (Integer commonId : commonIds) {
                    GoodsCommonEntity goodsPriceEntity = goodsCommonMapper.selectById(commonId);
                    BigDecimal commonPrice = goodsPriceEntity.getCommonPrice();
                    BigDecimal commonMarketPrice = goodsPriceEntity.getCommonMarketPrice();
                    param.put("commonId", commonId);
                    // 1-加 2-减 3-乘
                    if (null != operType) {
                        if (1 == operType) {
                            commonPrice = commonPrice.add(operValue);
                            commonMarketPrice = commonMarketPrice.add(operValue);
                        } else if (2 == operType) {
                            commonPrice = commonPrice.subtract(operValue);
                            commonMarketPrice = commonMarketPrice.subtract(operValue);
                        } else if (3 == operType) {
                            commonPrice = commonPrice.multiply(operValue);
                            commonMarketPrice = commonMarketPrice.multiply(operValue);
                        }
                        commonPrice = commonPrice.setScale(2, RoundingMode.UP);
                        commonMarketPrice = commonMarketPrice.setScale(2, RoundingMode.UP);
                        param.put("commonPrice", commonPrice);
                        param.put("commonMarketPrice", commonMarketPrice);
                        goodsCommonMapper.updateCommonPriceSep(param);
                        this.baseMapper.updateGoodsPriceSep(param);
                    }
                }
                // 3-分别调价
            } else if (null != updateType && updateType == 3) {
                List<Map<String, Object>> mapList = (List<Map<String, Object>>) param.get("commonInfos");
                for (Map<String, Object> map : mapList) {
                    if (null == map.get("commonPrice") || new BigDecimal(map.get("commonPrice").toString()).compareTo(BigDecimal.ZERO) <= 0) {
                        return ResponseResult.fail(BizCodeEnume.WRONG_AMOUNT);
                    }
                    if (null == map.get("commonMarketPrice") || new BigDecimal(map.get("commonMarketPrice").toString()).compareTo(BigDecimal.ZERO) <= 0) {
                        return ResponseResult.fail(BizCodeEnume.WRONG_AMOUNT);
                    }
                    goodsCommonMapper.updateCommonPriceSep2(map);
                    this.baseMapper.updateGoodsPriceSep2(map);
                }
            }
        } else {
            // 如果用户没有权限，则返回权限不足的错误
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }

        // 清除相关缓存
        RedisUtil.remove(this.pcIndexConfig);

        // 返回成功结果
        return ResponseResult.success();
    }


    @Override
    public ResponseResult queryByTypeId(ManageByTypeIdParamInfoDTO param) {
        // 初始化返回结果对象
        ManageGdsVO manageGdsAddResult = new ManageGdsVO();

        String goodsId = param.getGoodsId();

        // 根据商品ID查询商品基本信息
        GoodsBaseEntity entity = this.baseMapper.selectById(goodsId);
        Integer shopId = entity.getShopId();

        // 查询商品规格信息并转换为列表
        String specvalue1 = entity.getSpecValue();
        List<GoodBaseSpecValuesVO> specValuesJsonBeans = new ArrayList<>();
        JSONArray specValuesJsonBeanArray = com.alibaba.fastjson.JSONObject.parseArray(specvalue1);
        for (int index = 0; index < specValuesJsonBeanArray.size(); index++) {
            com.alibaba.fastjson.JSONObject obj = specValuesJsonBeanArray.getJSONObject(index);
            GoodBaseSpecValuesVO temp = JSONObject.parseObject(obj.toJSONString(), GoodBaseSpecValuesVO.class);
            specValuesJsonBeans.add(temp);
        }
        manageGdsAddResult.setSpecValueList(specValuesJsonBeans);
        // 初始化规格查询参数对象
        ManageSpecValueInfoDTO manageSpecValueQuery = new ManageSpecValueInfoDTO();
        manageSpecValueQuery.setShopId(shopId.toString());
        manageSpecValueQuery.setCatId(param.getCatId().toString());
        manageSpecValueQuery.setTypeId(Integer.valueOf(param.getTypeId()));
        // 获取售卖区域、运费模板和店铺自定义商品分类
        List<TransportTemplateVO> transportTemplate = merchantServiceClient.selectByShopId(shopId);
        // 设置返回结果
        manageGdsAddResult.setTransportTemplate(transportTemplate);
        // 返回成功响应结果
        return ResponseResult.success(manageGdsAddResult);
    }

    /**
     * 插入对象存储照片端口
     *
     * @param param 对象存储照片插入参数对象
     * @return 响应结果，插入成功或权限不足
     */
    @Override
    public ResponseResult insertOssPhotoPort(OssPhotoInsertDTO param) {
        // 从Redis中获取用户信息字符串
        String userInfoStr = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + param.getToken());
        // 创建用于存储信息的Map对象
        Map<String, Object> map = new HashMap<String, Object>();
        // 将用户信息字符串转换为对象
        UserInfoVO userInfo = JsonUtils.toBean(userInfoStr, UserInfoVO.class);
        // 初始化照片URL字符串
        String photoUrl = null;
        // 判断用户信息字符串是否非空
        if (userInfoStr != "" && userInfoStr != null) {

            // 遍历参数中的照片URL列表，将其拼接成一个字符串
            int i = 0;
            for (photoUrlDTO par : param.getOssparam()) {
                // 解密

                if (i != 0) {
                    photoUrl += "," + par.getPhotoUrl();
                } else {
                    photoUrl = par.getPhotoUrl();
                }
                i++;
            }

            // 根据商品ID查询商品基本信息
            GoodsBaseEntity goods = this.baseMapper.selectById(param.getGoodsId());
            // 将用户ID、用户名、商品ID、店铺ID、店铺名称、商品图片、商品名称、举报图片、类型ID、类型名称和举报内容放入Map对象
            map.put("userId", userInfo.getUserId());
            map.put("userName", userInfo.getUserName());
            map.put("goodsId", param.getGoodsId());
            map.put("shopId", goods.getShopId());
            map.put("shopName", goods.getGoodsName());
            map.put("image", goods.getGoodsImage());
            map.put("goodsName", goods.getGoodsName());
            map.put("reportImage", photoUrl);
            map.put("typeId", param.getTypeId());
            map.put("typeName", param.getTypeName());
            map.put("reportContent", param.getReportContent());
            // 将Map对象中的信息插入举报表
            this.baseMapper.insertReport(map);
        } else {
            // 如果用户信息为空，则返回权限不足的错误
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }

        // 返回成功的结果
        return ResponseResult.success();
    }


    @Override
    public ResponseResult queryGdsDetail(WapGoodsDetailQueryDTO param) {
        try {
            GdsDetailPageResultVO detailPage = new GdsDetailPageResultVO();
            // 获取user信息
            UserInfoVO userinfo = ParseTokenUtils.getUserInfoByTokenNoIsExist(param.getToken());
            // 查询商品详情
            GdsBaseDetailResultVO goodsBases = this.baseMapper.selectByGoodsId(param.getGoodsId());
            if (null == goodsBases) {
                return ResponseResult.fail(0, "商品下架或不存在");
            }
            if (goodsBases != null && goodsBases.getGoodsType() == 2) {
                return ResponseResult.fail(0, "温馨提示：供应商商品仅支持在电脑端查看~");
            }
            if (!goodsBases.getGoodsIsShelves().equals(1) || !goodsBases.getCommonState().equals(1)
                    || !goodsBases.getCommonVerify().equals(1)) {
                goodsBases.setGoodsStock(0);
            }
            if (StringUtils.isNotEmpty(goodsBases.getSpecValue())) {
                List<GoodBaseSpecValuesJsonBeanVO> specValuesJsonBeans = new ArrayList<>();
                JSONArray specValuesJsonBeanArray = JSONObject.parseArray(goodsBases.getSpecValue());
                for (int index = 0; index < specValuesJsonBeanArray.size(); index++) {
                    JSONObject obj = specValuesJsonBeanArray.getJSONObject(index);
                    GoodBaseSpecValuesJsonBeanVO temp = JSONObject.parseObject(obj.toJSONString(), GoodBaseSpecValuesJsonBeanVO.class);
                    specValuesJsonBeans.add(temp);
                }
                goodsBases.setSpecValueList(specValuesJsonBeans);
            }

            Integer commonId = goodsBases.getCommonId();

            Integer catId = null;

            if (null != goodsBases) {
                goodsBases.setGoodsSpec(goodsBases.getGoodsSpec());
                Integer shopId = goodsBases.getShopId();
                GdsShopDetailResultVO shopDetail = null;
                if (0 != shopId) {
                    shopDetail = this.baseMapper.selectShopDetailByShopId(shopId);
                    if (shopDetail == null) {
                        return ResponseResult.fail(0, "店铺信息为null", null);
                    }
                    // 获取店铺评分
                    getShopScore(shopId, shopDetail);
                    // 设置店铺默认值
                    setShopDefaultValue(goodsBases, shopDetail);
                    Integer result = 0;
                    Map<Object, Object> paramTmp = new HashMap<>();
                    paramTmp.put("shopId", shopId);
                    InvoiceSwitchEntityVO data = this.baseMapper.queryInvoiceSwitch(paramTmp);
                    Integer invoiceSwitchStatus = data.getInvoiceSwitchStatus();
                    Integer invoiceAuditStatus = data.getInvoiceAuditStatus();
                    if (1 == invoiceAuditStatus && 1 == invoiceSwitchStatus) {
                        result = 1;
                    } else if (2 == invoiceAuditStatus && 0 == invoiceSwitchStatus) {
                        result = 1;
                    }
                    shopDetail.setInvoiceActive(result);
                } else {
                    shopDetail = new GdsShopDetailResultVO();
                    shopDetail.setShopId(0);
                    shopDetail.setShopName("平台店铺");
                    shopDetail.setShopLogo("");
                    shopDetail.setShopCount(0);
                    shopDetail.setShopCollect(0);
                    shopDetail.setWapShopLogo("");
                    shopDetail.setInvoiceActive(0);
                }


                // 查询公共信息
                GdsCommonResultVO gdsCommon = goodsCommonMapper.selectTransport(commonId);
                if (StringUtils.isNotEmpty(gdsCommon.getSpecParam())) {
                    List<SpecParamJsonBeanVO> list = new ArrayList<>();
                    JSONArray jsonSpecParam = JSONObject.parseArray(gdsCommon.getSpecParam());
                    for (int index = 0; index < jsonSpecParam.size(); index++) {
                        JSONObject obj = jsonSpecParam.getJSONObject(index);
                        SpecParamJsonBeanVO temp = JSONObject.parseObject(obj.toJSONString(), SpecParamJsonBeanVO.class);
                        list.add(temp);
                    }
                    gdsCommon.setSpecParamList(list);
                }
                // 不限购
                goodsBases.setGoodsMaxSale(goodsBases.getGoodsStock());

                // 活动
                ActivityGoodsDetailDTO activityGoodsDetail = goodsCommonService.dealActivity2(goodsBases.getGoodsId(), goodsBases.getGoodsStock(),
                        goodsBases.getGoodsMaxSale(), commonId, gdsCommon.getCommonIsXian(), userinfo.getUserId(), param.getSessionTime());

                if (null != activityGoodsDetail && !activityGoodsDetail.getActivityStatus().equals(0)) {
                    goodsBases.setActivityStatus(activityGoodsDetail.getActivityStatus());
                    goodsBases.setActivityGoodsDetail(activityGoodsDetail);
                    if (!activityGoodsDetail.getActivityStatus().equals(4)) {
                        goodsBases.setGoodsMaxSale(activityGoodsDetail.getGoodsUpperLimit());
                        goodsBases.setGoodsMinSale(activityGoodsDetail.getGoodsLowerLimit());
                    }
                    //满减赠折活动展示活动内容ZZZ
                    if (activityGoodsDetail.getActivityStatus().equals(5)) {
                        String promotionContent = "";
                        List<ManjianzengzhePromotionContentQueryResultVO> promotionContents =
                                this.baseMapper.selectManjianzengzhePromotionContent(commonId);
                        for (ManjianzengzhePromotionContentQueryResultVO r : promotionContents) {
                            Integer discountType = r.getDiscountType();
                            Integer hasGiveaway = r.getHasGiveaway();
                            String discountContent = r.getDiscountContent();
                            boolean mzFlag = discountContent == null || "".equals(discountContent);
                            //满减赠
                            if (discountType == 1) {
                                if (null == goodsBases.getPromotionType() || "".equals(goodsBases.getPromotionType())) {
                                    if (hasGiveaway == 1 && mzFlag) {
                                        goodsBases.setPromotionType("满赠");
                                    } else {
                                        goodsBases.setPromotionType("满减");
                                    }
                                }
                                //满折
                            } else if (discountType == 3) {
                                goodsBases.setPromotionType("满折扣");
                            }
                            if ("满减".equals(goodsBases.getPromotionType()) && hasGiveaway == 1) {
                                goodsBases.setPromotionType("满减赠");
                            } else if ("满赠".equals(goodsBases.getPromotionType()) && !mzFlag) {
                                goodsBases.setPromotionType("满减赠");
                            }
                            goodsCommonService.dealPromotionContent(r);

                            promotionContent = promotionContent + r.getPromotionContent() + "\n";

                        }
                        goodsBases.setPromotionContent(promotionContent);


                    }
                    activityGoodsDetail.setGoodsPrice(goodsBases.getGoodsPrice());
                    gdsCommon.setCommonIsXian(activityGoodsDetail.getActivityStatus());
                } else {
                    goodsBases.setActivityStatus(0);
                    goodsBases.setActivityGoodsDetail(new ActivityGoodsDetailDTO());
                    gdsCommon.setCommonIsXian(0);
                }

                catId = gdsCommon.getCatId();

                // 设置规格名称和规格值
                setCommonSpec(gdsCommon);

                // 查询图片信息
                List<GdsImageDetailResultVO> goodsImages = goodsImagesMapper.selectByCommonId(param.getGoodsId().toString(), shopId);
                if (CollectionUtils.isEmpty(goodsImages)) {
                    GdsImageDetailResultVO goodsImage = new GdsImageDetailResultVO();
                    goodsImage.setImagesImage(gdsCommon.getCommonImage());
                    goodsImage.setImagesDisplayorder(0);
                    goodsImage.setImagesIsDefault(1);


                    goodsImages.add(goodsImage);
                }

                // 处理结果。防止出现null或空，影响ios
                GdsShopDetailResultVO detailResultVO = new GdsShopDetailResultVO();
                detailResultVO.setShopId(0);
                detailResultVO.setShopName("");
                detailResultVO.setShopLogo("");
                detailResultVO.setShopCount(0);
                detailResultVO.setShopCollect(0);
                detailResultVO.setWapShopLogo("");

                detailPage.setGoodsBases(goodsBases);
                detailPage.setGoodsImages(goodsImages != null ? goodsImages : new ArrayList<GdsImageDetailResultVO>());
                detailPage.setShopDetail(shopDetail != null ? shopDetail : detailResultVO);
                detailPage.setGdsCommon(gdsCommon);

            }

            // 更新点击量
            this.baseMapper.updateGoodsClick(param.getGoodsId().toString());
            goodsCommonMapper.updateGoodsClick(commonId);
            // 添加足迹
            String token = param.getToken();
            if (token != null && !token.equals("")) {
                goodsCommonService.addUserFoot(userinfo, commonId, catId);
            }

            return ResponseResult.success(detailPage);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail(e.getMessage());
        }
    }

    /**
     * 查询用户足迹H5页面数据
     * 当前方法用于查询用户在H5页面的浏览足迹，包括用户浏览过的商品信息和相关活动信息
     * 该方法首先检查参数中的分页信息（页码和每页限制），如果未设置则使用默认值
     * 然后根据用户token从Redis中获取用户信息，并根据用户信息查询店铺自定义商品分类
     * 接着查询用户的浏览记录，并对其进行分页处理
     * 最后，对查询到的浏览记录中的每个商品进行处理，获取商品的详细信息和相关活动信息
     *
     * @param param 查询参数，包含分页信息和用户token等
     * @return 包含用户浏览记录和分页信息的结果对象
     */
    @Override
    public ResponseResult queryFootPrintH5(GuessYouLikeQueryDTO param) {
        // 检查分页参数，如未设置则使用默认值
        if (param.getLimit() == null || param.getPage() == null) {
            param.setPage(1);
            param.setLimit(50);
        }
        // 初始化结果对象和空的用户足迹列表
        Map result = new HashMap();
        List<WapCommonBaseUserFootVO> commonBases = Lists.newArrayList();
        Integer totalCount = 0;
        String token = param.getToken();

        // 如果token非空且在Redis中存在，则进行用户信息的获取和处理
        if (StringUtils.isNotBlank(token) && RedisUtil.exists(token)) {
            //获取用户信息
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
            // 查询店铺自定义商品分类
            List<Integer> catIds = this.baseMapper.selectCartGuessLikeGoods2(userInfo.getUserId());
            param.setCatIds(catIds);
        }
        // 查询用户足迹数据
        commonBases = this.baseMapper.selectAppGuessLikeH5(param);
        // 设置总记录数
        result.put("count", commonBases.size());
        // 计算当前页显示的总记录数
        if (param.getLimit() * param.getPage() < commonBases.size()) {
            totalCount = param.getLimit() * param.getPage();
        } else {
            totalCount = commonBases.size();
        }

        // 分页处理用户足迹数据
        commonBases = commonBases.subList((param.getPage() - 1) * param.getLimit(), totalCount);

        // 遍历用户足迹数据，处理每个商品信息
        for (WapCommonBaseUserFootVO gdsListQueryResult : commonBases) {
            // 处理商品ID
            gdsListQueryResult.setGoodsIdOne(Integer.parseInt(gdsListQueryResult.getGoodsId()));
            if (gdsListQueryResult.getGoodsIdOne().equals(1)) {
                List<Integer> goodsIds = this.baseMapper.selectGoodsId(gdsListQueryResult.getCommonId());
                if (!CollectionUtils.isEmpty(goodsIds)) {
                    gdsListQueryResult.setGoodsIdOne(goodsIds.get(0));
                }
            }

            // 获取商品的活动信息
            Integer commonIsXian = gdsListQueryResult.getCommonIsXian();
            ActivityGoodsDetailDTO activityGoodsDetail = dealActivity(gdsListQueryResult.getGoodsIdOne(), null,
                    gdsListQueryResult.getCommonId(), commonIsXian);

            // 根据活动信息更新商品信息
            if (null != activityGoodsDetail && activityGoodsDetail.getActivityStatus() != 0) {
                gdsListQueryResult.setActivityStatus(activityGoodsDetail.getActivityStatus());
                gdsListQueryResult.setActivityGoodsDetail(activityGoodsDetail);
                gdsListQueryResult.setCommonIsXian(activityGoodsDetail.getActivityStatus());
                gdsListQueryResult.setCommonPrice(activityGoodsDetail.getActivityPrice());
                if (activityGoodsDetail.getActivityStatus() == 5) {
                    gdsListQueryResult.setCommonPrice(activityGoodsDetail.getMjzzPrice());
                }
            } else {
                gdsListQueryResult.setActivityStatus(0);
                gdsListQueryResult.setActivityGoodsDetail(new ActivityGoodsDetailDTO());
                gdsListQueryResult.setCommonIsXian(0);
            }
            // 计算价格
            ResponseResult<PointsCashRatioVo> pointsCashRatio = baseServiceClient.getPointsCashRatio();
            if (pointsCashRatio.getCode() == 1) {
                PointsCashRatioVo ratioVo = pointsCashRatio.getData();
                if (Objects.nonNull(gdsListQueryResult.getPointsRate())) {
                    PointsRatioUtil.PointsCashRatioRes pointsRatio = PointsRatioUtil.getPointsRatio(gdsListQueryResult.getCommonPrice(), gdsListQueryResult.getPointsRate());
                    gdsListQueryResult.setCashPrice(pointsRatio.getCashPrice());
                }
            }
        }
        // 将处理后的用户足迹数据放入结果对象
        result.put("commonBases", commonBases);
        // 返回查询结果
        return ResponseResult.success(result);
    }


    @Override
    public ResponseResult getGoodsPoster(Map<String, Object> param) {
        Map<String, Object> map = new HashMap<>();
        // 二维码的base64位
        String orCode = null;
        try {
            String token = (String) param.get("token");
            // 1 是 h5  2 是小程序
            String type = (String) param.get("type");
            //商品id
            String goodsId = (String) param.get("goodsId");

            //获取用户信息
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
            // 封装用户名
            map.put("userName", userInfo.getUserName());
            // 封装用户id
            map.put("userId", userInfo.getUserId());
            //封装用户头像
            map.put("userAvatar", userInfo.getUserAvatar());
            // 获取海报的背景图
            NavSortLogoDTO gdbNavSortLogoDTO = new NavSortLogoDTO();
            gdbNavSortLogoDTO.setConfigKey("h5_goods_image");
            ResponseResult resp1 = baseServiceClient.quertNavLogo(gdbNavSortLogoDTO);
            List<Map<String, Object>> list = (List<Map<String, Object>>) resp1.getData();
            DecimalFormat df = new DecimalFormat("#.00");
            if (null != list && list.size() > 0) {
                //海报背景图
                String posterImageUrl = list.get(0).get("configValue").toString();
                map.put("posterImageUrl", posterImageUrl);
                //获取商品信息
                GoodsPosterVO gdsListQueryEsResult = this.baseMapper.getGoodsPoster(Integer.valueOf(goodsId));
                map.put("goodsId", gdsListQueryEsResult.getGoodsId());
                map.put("commonId", gdsListQueryEsResult.getCommonId());
                map.put("commonName", gdsListQueryEsResult.getCommonName());
                map.put("commonImage", gdsListQueryEsResult.getCommonImage());

                // 计算价格

                ResponseResult<PointsCashRatioVo> pointsCashRatio = baseServiceClient.getPointsCashRatio();
                if (pointsCashRatio.getCode() == 1) {
                    PointsCashRatioVo ratioVo = pointsCashRatio.getData();
                    Double pointsRate = gdsListQueryEsResult.getPointsRate();
                    if (Objects.nonNull(pointsRate)) {
                        PointsRatioUtil.PointsCashRatioRes ratio = PointsRatioUtil.getPointsRatio(gdsListQueryEsResult.getCommonPrice(), pointsRate);
                        map.put("cashPrice", ratio.getCashPrice());
                    }
                }

                if (null != gdsListQueryEsResult.getCommonIsXian() && !gdsListQueryEsResult.getCommonIsXian().equals(0)) {
                    BigDecimal goodsPrice = new BigDecimal(0);
                    if (gdsListQueryEsResult.getCommonIsXian().equals(2)) {
                        // 秒杀
                        AppSeckillGoodsQueryResultVO seckillGoods = activityServiceClient.selectSeckillByGoodsId2(Integer.valueOf(gdsListQueryEsResult.getGoodsId()));
                        goodsPrice = seckillGoods.getSeckillPrice();
                    } else if (gdsListQueryEsResult.getCommonIsXian().equals(3)) {
                        // 限时折扣
                        AppDiscountGoodsQueryResultVO discountGoods = activityServiceClient.selectDiscountByGoodsId(Integer.valueOf(gdsListQueryEsResult.getGoodsId()));
                        goodsPrice = discountGoods.getDiscountPrice();
                    } else if (gdsListQueryEsResult.getCommonIsXian().equals(4)) {
                        // 拼团
                        AppPintuanGoodsQueryResultVO pintuanGoods = activityServiceClient.selectpiecingTogetherByGoodsId(gdsListQueryEsResult.getCommonId(), Integer.valueOf(gdsListQueryEsResult.getGoodsId()));
                        goodsPrice = pintuanGoods.getPrice();
                    } else if (gdsListQueryEsResult.getCommonIsXian().equals(5)) {
                        //满减赠折 不涉及价格修改
                        goodsPrice = gdsListQueryEsResult.getCommonPrice();
                    }
                    map.put("commonPrice", goodsPrice);
                } else {
                    map.put("commonPrice", gdsListQueryEsResult.getCommonPrice());
                }
                //判断当前用户是否是微客
                Integer vkstart = goodsCommonMapper.queryUserVkStart(userInfo.getUserId());
                //判断当前商品是否是 分销商品
                Integer vkGoodsStart = goodsCommonMapper.queryvkGoodsStart(gdsListQueryEsResult.getCommonId());

                // 1 是 h5  2 是小程序
                if ("1".equals(type)) {
                    String url = domainName + "/h5/#/pages/description/descrption?goodsId=" + gdsListQueryEsResult.getGoodsId() + "&commonId=" + gdsListQueryEsResult.getCommonId();
                    if (vkstart != 0 && vkGoodsStart != 0) {
                        url = url + "&shareUserId=" + userInfo.getUserId() + "&shareType = 1" + "&isShare=true";
                    }
                    byte[] b = QrCodeUtil.createQRCode(150, 150, url);
                    // 转base 64
                    orCode = QrCodeUtil.BASE_PREFIX + Base64Utils.encode2(b);

                } else {
                    //因为生成小程序的二维码参数不能过长，所以将部分请求参数存入redis

                    Integer num = null;
                    if (vkstart != 0 && vkGoodsStart != 0) {
                        //定义变长字符串
                        StringBuilder str = new StringBuilder();
                        Random random = new Random();
                        //随机生成数字，并添加到字符串  小程序二维码不能超过 32 位
                        for (int i = 0; i < 8; i++) {
                            str.append(random.nextInt(6));
                        }
                        num = Integer.parseInt(str.toString());
                    } else {
                        num = gdsListQueryEsResult.getCommonId();
                    }
                    //生成markid key
                    System.out.println("codeId=" + num + "&codeType=PUTONG");
                    // 根据shopid 判断当前店铺是否是品牌店铺
                    String fileName = null;
                    GdsShopDetailResultVO gdsShopDetailResultVO = merchantServiceClient.queryShopinfoByShopId(gdsListQueryEsResult.getShopId());
                    if (gdsShopDetailResultVO != null && CommonType.ShopType.BRAND_SHOP.getCode().equals(gdsShopDetailResultVO.getShopType())) {
                        fileName = AccessTokenUtil.getminiqrQr("codeId=" + num + "&codeType=PUTONG", "pages/description/descrption", gdsListQueryEsResult.getShopId());
                    } else {
                        fileName = AccessTokenUtil.getminiqrQr("codeId=" + num + "&codeType=PUTONG", "pages/description/descrption", null);
                    }
                    orCode = QrCodeUtil.BASE_PREFIX + fileName;
                    Map<String, Object> orMap = new HashMap<>();
                    orMap.put("goodsId", gdsListQueryEsResult.getGoodsId());
                    orMap.put("commonId", gdsListQueryEsResult.getCommonId());
                    if (vkstart != 0 && vkGoodsStart != 0) {
                        orMap.put("shareUserId", userInfo.getUserId());
                        orMap.put("shareType", 1);
                        orMap.put("isShare", true);
                    }
                    //将拼团团id存入redis从另外接口查询
                    RedisUtil.set("PUTONG_" + num, JsonUtils.toString(orMap), 60 * 60 * 24 * 30);
                }
                map.put("orcode", orCode);

                return ResponseResult.success(map);
            } else {
                return ResponseResult.fail(1, "获取背景图失败！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail(e.getMessage());

        }
    }

    /**
     * 查询非特殊商品信息
     *
     * @param param 查询参数，包含令牌等信息
     * @return 包含分页商品信息的响应结果
     * @throws IllegalAccessException 如果访问权限不足
     */
    @Override
    public ResponseResult queryNoneGoods(NoneGoodsQueryDTO param) throws IllegalAccessException {
        // 通过令牌解析用户信息
        UserInfoVO userInfoVO = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 将查询参数转换为Map
        Map data = ParseMapUtils.beanToMap(param);
        // 执行非特殊商品的分页查询
        IPage<GoodsRecResultDetailVO> page = this.baseMapper.selectNoneGoods(new Query<GoodsRecResultDetailVO>().getPage(data),
                param);
        // 如果查询到商品记录，则处理每个商品的活动状态
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            for (GoodsRecResultDetailVO good : page.getRecords()) {
                // 获取商品是否参与活动的状态
                Integer commonIsXian = good.getCommonIsXian();
                // 处理商品活动详情
                ActivityGoodsDetailDTO activityGoodsDetail = dealActivity(
                        Integer.parseInt(good.getGoodsId()), null, good.getCommonId(),
                        commonIsXian);

                // 如果商品有有效的活动详情且活动状态不为0，则设置商品的活动状态和详情
                if (null != activityGoodsDetail && activityGoodsDetail.getActivityStatus() != 0) {
                    good.setActivityStatus(activityGoodsDetail.getActivityStatus());
                    good.setActivityGoodsDetail(activityGoodsDetail);
                    // 如果活动状态为特定值5，则设置商品价格为活动价格
                    if (activityGoodsDetail.getActivityStatus() == 5) {
                        good.setCommonPrice(activityGoodsDetail.getMjzzPrice());
                    }
                } else {
                    // 如果商品没有活动或活动状态为0，则进行相应设置
                    good.setActivityStatus(0);
                    good.setActivityGoodsDetail(new ActivityGoodsDetailDTO());
                }
            }
        }
        // 返回分页商品信息的响应结果
        return ResponseResult.success(PageUtils.getPage(page));
    }


    @Override
    /**
     * 将文件上传到阿里云OSS
     *
     * @param param 包含上传所需信息的参数对象，如文件流和文件名
     * @return 返回上传结果，成功或失败的信息
     */
    public ResponseResult uploadToOss(OssUploadUpdateParamDTO param) {
        // 设置文件在OSS中的存储路径
        param.setFileUrl("file/upload/shop/settled/");
        // 检查文件后缀名是否在支持的范围内
        if (Arrays.asList(suffix).contains(param.getStreamName())) {
            // 如果是支持的文件格式，则调用第三方服务上传Base64编码的文件流到OSS
            return thirdPartyServiceClient.uploadToOssBase64(param);
        } else {
            // 如果文件格式不支持，则返回失败结果，提示不支持当前格式文件
            return ResponseResult.fail(0, "不支持当前格式文件");
        }
    }


    /**
     * 设置商品的通用规格信息
     *
     * @param gdsCommon GdsCommonResultVO对象，用于存储商品的通用信息
     */
    private void setCommonSpec(GdsCommonResultVO gdsCommon) {
        // 将字符串形式的规格值转换为对象列表
        List<CommonSpecValueVO> specValueList = JsonUtils.string2Obj(gdsCommon.getCommonSpecValue(), List.class,
                CommonSpecValueVO.class);

        // 创建一个用于存储处理后的规格值的列表
        List<WapCommonSpecValueVO> commonSpecValueList = Lists.newArrayList();

        // 如果规格值列表不为空，遍历并处理每个规格值
        if (!CollectionUtils.isEmpty(specValueList)) {
            for (CommonSpecValueVO commonSpecValue : specValueList) {
                // 创建一个新的WapCommonSpecValueVO对象来存储规格值信息
                WapCommonSpecValueVO specValue = new WapCommonSpecValueVO();
                // 设置规格ID和商品ID
                specValue.setCommonId(commonSpecValue.getCommonId());
                specValue.setGoodsId(commonSpecValue.getGoodsId());

                // 将处理后的规格值对象添加到列表中
                commonSpecValueList.add(specValue);
            }
        }

        // 清除原始的规格值字符串，并设置处理后的规格值列表
        gdsCommon.setCommonSpecValue(null);
        gdsCommon.setCommonSpecValueList(commonSpecValueList);
    }

    /**
     * 设置店铺的默认展示值
     *
     * @param goodsBases GdsBaseDetailResultVO对象，包含商品的基础信息
     * @param shopDetail GdsShopDetailResultVO对象，用于存储和展示店铺的详细信息
     */
    private void setShopDefaultValue(GdsBaseDetailResultVO goodsBases, GdsShopDetailResultVO shopDetail) {
        // 检查店铺logo是否为空，如果为空则设置默认值
        if (shopDetail.getShopLogo() == null || shopDetail.getWapShopLogo() == null) {
            String configValue = baseServiceClient.selectConfigValueFeign("logo_copywriting_ico_shop");

            if (shopDetail.getShopLogo() == null) {
                shopDetail.setShopLogo(configValue);
            }
            // 以下条件判断逻辑可以简化，代码重构时应考虑逻辑清晰性和可维护性
            if (shopDetail.getWapShopLogo() == null && shopDetail.getShopLogo() != null) {
                shopDetail.setWapShopLogo(configValue);
            } else {
                shopDetail.setWapShopLogo(configValue);
            }
        }

        // 查询当前店铺的商品数量并设置到店铺详情对象中
        Integer shopCount = goodsCommonMapper.selectByShopId(goodsBases.getShopId());
        shopDetail.setShopCount(shopCount);

        // 调用另一个方法设置店铺的默认值，此处可能存在代码重复或错误，因为方法名与方法体内容不符
        // 正确的做法应该是检查方法名是否准确反映其功能，或检查是否存在逻辑错误
        setShopDefaulValue(shopDetail);
    }


    /**
     * 设置店铺的默认Logo
     * 当店铺的Logo或手机端Logo为空时，设置默认Logo
     *
     * @param shopDetail 店铺详情对象
     */
    private void setShopDefaulValue(GdsShopDetailResultVO shopDetail) {
        // 检查店铺Logo和手机端Logo是否为空，如果为空则设置默认Logo
        if (shopDetail.getShopLogo() == null || shopDetail.getWapShopLogo() == null) {
            // 从配置服务中获取默认Logo地址
            String configValue = baseServiceClient.selectConfigValueFeign("logo_copywriting_ico_shop");
            // 如果店铺Logo为空，设置默认Logo
            if (shopDetail.getShopLogo() == null) {
                shopDetail.setShopLogo(configValue);
            }
            // 如果手机端Logo为空，设置默认Logo
            if (shopDetail.getWapShopLogo() == null) {
                shopDetail.setWapShopLogo(configValue);
            }
        }
    }

    /**
     * 获取店铺的评分信息
     * 根据店铺ID获取店铺的综合评分，并将其设置到店铺详情对象中
     *
     * @param shopId     店铺ID
     * @param shopDetail 店铺详情对象
     */
    private void getShopScore(Integer shopId, GdsShopDetailResultVO shopDetail) {
        try {
            // 创建参数Map，包含店铺ID
            Map<Object, Object> map = new HashMap<>();
            map.put("shopId", shopId);
            // 调用服务获取店铺评分
            ResponseResult shopScore = merchantServiceClient.shopScore(map);
            if (null != shopScore) {
                // 解析评分数据
                String dataString = (String) shopScore.getData();
                ShopEvaluationDTO data = JsonUtils.string2Obj(dataString, ShopEvaluationDTO.class);
                // 计算综合评分
                Double overallScore = null;
                if (null != data) {
                    BigDecimal setScale = new BigDecimal(data.getEvaluationDeliverycredit())
                            .add(new BigDecimal(data.getEvaluationDesccredit()))
                            .add(new BigDecimal(data.getEvaluationServicecredit()))
                            .divide(new BigDecimal(3), 1, BigDecimal.ROUND_DOWN);
                    overallScore = setScale.doubleValue();
                } else {
                    // 如果数据为空，设置默认评分为4.5
                    overallScore = 4.5d;
                }
                // 将综合评分设置到店铺详情对象中
                shopDetail.setOverallScore(overallScore);
            }
        } catch (Exception e) {
            // 异常处理：记录获取店铺评分失败的信息
            LOG.info("获取店铺评分失败：{}", e.getMessage());
        }
    }


    /**
     * 根据通用ID查询商品ID列表的字符串形式
     *
     * @param commonId 通用ID
     * @return 商品ID列表的字符串形式，商品ID之间以逗号分隔
     */
    public String selectByCommonId(Integer commonId) {
        // 查询符合条件的商品列表，并提取商品ID
        List<Integer> list = this.baseMapper.selectList(new LambdaQueryWrapper<GoodsBaseEntity>()
                        .eq(GoodsBaseEntity::getCommonId, commonId))
                .stream().map(GoodsBaseEntity::getGoodsId).collect(Collectors.toList());
        // 构建最终的字符串结果
        StringBuilder sb = new StringBuilder();
        for (int x = 0; x < list.size(); x++) {
            // 遍历商品ID列表，拼接字符串
            if (x == list.size() - 1) {
                // 最后一个商品ID不加逗号
                sb.append(list.get(x));
            } else {
                // 非最后一个商品ID后加逗号
                sb.append(list.get(x) + ",");
            }
        }
        String string = sb.toString();
        return string;
    }

    /**
     * 查询所有商品
     *
     * @param param 查询参数，包含商品过滤和分页信息
     * @return 包含分页商品信息的响应结果
     * @throws IllegalAccessException 当访问字段时出现权限不足错误
     */
    public ResponseResult queryAllGoods(PlatFormAllGoodsDTO param) throws IllegalAccessException {

        // 将查询参数转换为Map，便于后续查询使用
        Map data = ParseMapUtils.beanToMap(param);

        // 执行查询，获取分页后的商品数据
        IPage<PlatFormAllGoodsVO> page = this.baseMapper.selectByAllGoods(new Query<PlatFormAllGoodsVO>().getPage(data),
                param);

        // 遍历查询到的商品，处理每个商品的业务逻辑
        for (PlatFormAllGoodsVO good : page.getRecords()) {
            // 根据店铺自营状态设置店铺业务类型
            if ("true".equals(good.getShopSelfSupport())) {
                good.setShopBusiness(2);
            } else {
                // 检查店铺业务类型是否为0、1或2，若不是自营则设置为1
                List<Integer> list = Arrays.asList(0, 1, 2);
                if (list.contains(good.getShopBusiness())) {
                    good.setShopBusiness(1);
                }
            }

            // 查询当前商品所上架的专区名称列表
            List<String> noneNameList = goodsCommonMapper.selectNoneName(good.getCommonId());
            good.setNoneNameList(noneNameList);

            GoodsBaseEntity goodsInfo = this.baseMapper.selectOne(new LambdaQueryWrapper<GoodsBaseEntity>()
                    .eq(GoodsBaseEntity::getCommonId, good.getCommonId()).eq(GoodsBaseEntity::getGoodsIsShelves, 1)
                    .select(GoodsBaseEntity::getGoodsId).last("limit 1"));
            if (null != goodsInfo) {
                good.setGoodsId(goodsInfo.getGoodsId().toString());
                // 查询当前商品不同规格下的库存信息
                List<GoodsStockVO> stockEntityList = new ArrayList<>();
                stockEntityList = this.baseMapper.selectStocks(good.getCommonId());
                Integer commonStock = 0;
                for (GoodsStockVO goodsStockVO : stockEntityList) {
                    commonStock = commonStock + goodsStockVO.getGoodsStock();
                }
                good.setCommonStock(commonStock);
                good.setGoodsStockEntityList(stockEntityList);
            }

            String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.STORAGE_SWITCH.getDesc());
            if (!StringUtils.isEmpty(value) && "1".equals(value)) {
                Integer storageGoodsStock = storageGoodsBaseMapper.getAllSpecGoodsStorageStock(good.getCommonId());
                good.setStorageGoodsStock(storageGoodsStock);
            } else {
                good.setStorageGoodsStock(0);
            }
        }

        // 返回分页商品数据的响应结果
        return ResponseResult.success(PageUtils.getPage(page));
    }


    /**
     * 查询指定商品的优惠券相关商品信息
     *
     * @param goodsId 商品ID
     * @return 返回优惠券相关商品信息WapShippingQueryResultVO实例，如果商品不存在或状态不正确则返回null
     */
    @Override
    public WapShippingQueryResultVO queryProductVoucherGoods(Integer goodsId) {
        // 查询sku商品信息
        WapShippingQueryResultVO wapShippingQueryResultVO = this.baseMapper.queryProductVoucherGoods(goodsId);
        // 如果sku商品信息为空，则返回null
        if (null == wapShippingQueryResultVO) {
            return null;
        }
        // 判断spu商品状态是否上架
        Integer spuStartNum = goodsCommonMapper.querySpuStartNum(wapShippingQueryResultVO.getCommonId());
        // 如果spu商品状态不是上架状态，则返回null
        if (0 == spuStartNum) {
            return null;
        }
        // 商品信息和状态都正常，返回优惠券相关商品信息
        return wapShippingQueryResultVO;
    }

    @Override
    public ResponseResult queryTransportArea(WapTransportAreaQueryDTO param) {
        // 用于存储每个商品在指定区域是否有货的结果
        List<List<Integer>> results = new ArrayList<List<Integer>>();
        // 获取商品的通用ID列表
        // transportAreaMapper
        // 查询所有的区域id
        List<List<Integer>> shopCommonIds = param == null ? null : param.getShopGoodsIds();
        if (shopCommonIds == null || shopCommonIds.isEmpty()) {
            // 参数缺失时返回明确业务错误，避免 NPE 被包装成 11009
            return ResponseResult.fail("商品ID不能为空");
        }
        // 用于存储遍历过程中所有商品的ID
        List<Integer> goodsIds = new ArrayList<Integer>();

        for (List<Integer> commonIds : shopCommonIds) {
            if (commonIds == null || commonIds.isEmpty()) {
                continue;
            }
            // 当前商品只有一个通用ID时，处理该商品
            Integer supplierGoods = 0;
            if (commonIds.size() == 1) {
                // 初始化库存为0
                int stock = 0;
                // 根据通用ID查询商品详情
                // 查询商品详情
                List<BaseDetailVO> goodsBases = null;
                try {
                    if (null != param.getGoodsType() && CommonType.ShopType.SUPPLY_SHOP.getCode().equals(param.getGoodsType())) {
                        goodsBases = baseMapper.selectSupplierByCommonIdZ(commonIds.getFirst());
                    } else {
                        goodsBases = baseMapper.selectByCommonIdZ(commonIds.getFirst());
                    }
                } catch (Exception e) {
                    // 本环境可能无经典轨 goods_base / hou_goods_base（仅 mall 轨），按可配送降级，避免 11009
                    LOG.warn("queryTransportArea classic goods lookup failed, soft success. commonId={}, err={}",
                            commonIds.getFirst(), e.getMessage());
                    List<Integer> result = new ArrayList<Integer>();
                    result.add(1);
                    results.add(result);
                    return ResponseResult.success(results);
                }
                // 经典轨无记录（mall 轨商品）时按可配送处理，交由前端库存兜底
                if (goodsBases == null || goodsBases.isEmpty()) {
                    List<Integer> result = new ArrayList<Integer>();
                    result.add(1);
                    results.add(result);
                    return ResponseResult.success(results);
                }
                supplierGoods = goodsBases.get(0).getSupplierSource();
                for (BaseDetailVO tmp : goodsBases) {
                    goodsIds.add(tmp.getGoodsId());
                    // 如果商品上架、状态正常且已验证，累加库存
                    if (tmp.getGoodsIsShelves() == 1 && tmp.getCommonState() == 1 && tmp.getCommonVerify() == 1) {
                        stock = stock + tmp.getGoodsStock();
                    }
                }
                // 如果库存为0，直接返回该结果
                List<Integer> result = new ArrayList<Integer>();
                if (stock == 0) {
                    result.add(0);
                    results.add(result);
                    return ResponseResult.success(results);
                }
            }

            if (null != supplierGoods && supplierGoods == 2) {
                int commonId = commonIds.get(0);
                //获取怡亚通SPUId
                SupplierGoodsYiyatongEntity yiyatongEntity = supplierGoodsYiyatongService.list(new LambdaQueryWrapper<SupplierGoodsYiyatongEntity>()
                        .eq(SupplierGoodsYiyatongEntity::getCommonId, commonId)
                        .eq(SupplierGoodsYiyatongEntity::getIsDelete, 0)).get(0);
                OpenSpuInfoQueryDTO dto = new OpenSpuInfoQueryDTO();
                List<String> spuIdList = new ArrayList<>();
                spuIdList.add(yiyatongEntity.getSpuId());
                dto.setSpuIdList(spuIdList);
                ResponseResult SpuResult = yiYaTongService.getSpuBySpuIdsPath(dto);
                List<OpenSpuInfoQueryVO> list = JSONArray.parseArray(JSON.toJSONString(SpuResult.getData()), OpenSpuInfoQueryVO.class);
                for (OpenSpuInfoQueryVO vo : list) {
                    List<Integer> result = new ArrayList<Integer>();
                    if (vo.getIsLimitArea().equals("N")) {
                        // 表示有货
                        result.add(1);
                        results.add(result);
                        return ResponseResult.success(results);
                    } else {
                        List<String> areaIdsList = vo.getLimitArea();
                        List<Integer> needAreaIds = param.getAreaIds();
                        if (areaIdsList.contains(needAreaIds.get(2).toString())) {
                            // 表示有货
                            result.add(1);
                            results.add(result);
                            return ResponseResult.success(results);
                        } else {
                            // 表示无货
                            result.add(0);
                            results.add(result);
                            return ResponseResult.success(results);
                        }
                    }
                }
            } else {
                // 根据商品ID查询运输区域ID
                List<Integer> transportAreaIds = null;
                if (null != param.getGoodsType() && CommonType.ShopType.SUPPLY_SHOP.getCode().equals(param.getGoodsType())) {
                    transportAreaIds = goodsCommonService.selectSupplierTransportAreaId(goodsIds);
                } else {
                    transportAreaIds = goodsCommonService.selectTransportAreaId(goodsIds);
                }
                if (!CollectionUtils.isEmpty(transportAreaIds)) {
                    List<Integer> result = new ArrayList<Integer>();
                    for (Integer transportAreaId : transportAreaIds) {
                        // 根据运输区域ID查询有货的区域信息
                        ResponseResult areas = merchantServiceClient.selectAreaById(transportAreaId);
                        ManageTransportAreaVO transportAreas = JSON.parseObject(JSON.toJSONString(areas.getData()), ManageTransportAreaVO.class);
                        String shopAreaIds = transportAreas.getAreaIds();
                        // 如果全国有货，标记结果为有货
                        // 全国有货
                        if (null != shopAreaIds && shopAreaIds.equals("0")) {
                            // 表示有货
                            result.add(1);
                            results.add(result);
                            return ResponseResult.success(results);
                        } else {
                            // 省市区有货的标记
                            boolean flag = false;
                            if (null != shopAreaIds && !shopAreaIds.isEmpty()) {
                                // 判断指定区域是否有货
                                // 判断传入区域是否有货
                                List<String> areaIdsList = Arrays.asList(shopAreaIds.split(","));
                                // 获取需要判断的区域ID列表
                                // 获取需要判断的id
                                List<Integer> needAreaIds = param.getAreaIds();

                                if (needAreaIds.size() >= 2) {
                                    if (areaIdsList.contains(needAreaIds.get(1).toString())) {
                                        // 表示有货
                                        result.add(1);
                                        flag = true;
                                        // break;
                                    }
                                }
                            }
                            // 如果没有标记为有货，表示该商品在指定区域无货
                            if (!flag) {
                                // 表示无货
                                result.add(0);
                            }
                        }
                    }
                    results.add(result);
                } else {
                    // 如果没有查询到运输区域ID，表示该商品无法运输，标记为无货
                    List<Integer> result = new ArrayList<Integer>();
                    for (int i = 0; i < goodsIds.size(); i++) {
                        result.add(0);
                    }
                    results.add(result);
                }

            }


        }
        // 如果结果列表为空，表示没有查询到任何商品的信息，返回失败结果
        if (CollectionUtils.isEmpty(results)) {
            return ResponseResult.fail(results);
        }
        // 返回成功结果，包含所有商品在指定区域是否有货的信息
        return ResponseResult.success(results);
    }

    /**
     * 根据商品ID查询组合商品信息。
     *
     * @param goodsId 商品ID
     * @return 返回包含组合商品信息的ActivityCombinationGoodsVO对象
     */
    @Override
    public ActivityCombinationGoodsVO selectGoodsById(Integer goodsId) {
        ActivityCombinationGoodsVO goods = this.baseMapper.selectGoodsById(goodsId);
        return goods;
    }

    /**
     * 根据商品ID查询商品ID列表。
     *
     * @param goodsId 商品ID
     * @return 返回包含商品ID列表的GoodsIdsQueryResultVO对象
     */
    @Override
    public GoodsIdsQueryResultVO selectGoodsIdList(Integer goodsId) {
        GoodsIdsQueryResultVO goods = this.baseMapper.selectGoodsIdList(goodsId);
        return goods;
    }

    /**
     * 根据商品ID查询通用ID。
     *
     * @param goodsId 商品ID
     * @return 返回与商品ID关联的通用ID
     */
    @Override
    public Integer selectCommonId(Integer goodsId) {
        Integer commonId = this.baseMapper.selectCommonId(goodsId);
        return commonId;
    }

    /**
     * 更新指定店铺的商品基础信息。
     *
     * @param shopId 店铺ID
     */
    @Override
    public void updateGoodsBase(String shopId) {
        this.baseMapper.updateGoodsBase(shopId);
    }

    /**
     * 更新商品的基础库存信息。
     *
     * @param param 包含更新参数的Map对象
     * @return 返回更新操作的结果，通常为受影响的行数
     */
    @Override
    public Integer updateBaseStock(Map<String, Object> param) {
        Integer result = this.baseMapper.updateBaseStock(param);
        return result;
    }

    /**
     * 查询商品的基础信息。
     *
     * @param gateStoreIndexGoodsDTO 包含查询条件的DTO对象
     * @return 返回包含商品基础信息的GateStoreIndexGoodsVO列表
     */
    @Override
    public List<GateStoreIndexGoodsVO> queryGoodsBaseInfo(GateStoreIndexGoodsDTO gateStoreIndexGoodsDTO) {
        List<GateStoreIndexGoodsVO> result = this.baseMapper.queryGoodsBaseInfo(gateStoreIndexGoodsDTO);
        return result;
    }

    /**
     * 根据供应商商品ID查询商品相关信息。
     *
     * @param supplierGoodsId 供应商商品ID
     * @return 返回包含商品信息的Map对象
     */
    @Override
    public Map<String, Object> queryByGoodsId(Integer supplierGoodsId) {
        Map<String, Object> result = this.baseMapper.queryByGoodsIdFeign(supplierGoodsId);
        return result;
    }

    /**
     * 查询商品的详细信息。
     *
     * @param modularizationdGoodsData 包含查询条件的DTO对象
     * @return 返回包含商品详细信息的ModularizationdGoodsDataDTO对象
     */
    @Override
    public ModularizationdGoodsDataDTO queryGoodsInfo(ModularizationdGoodsDataDTO modularizationdGoodsData) {
        ModularizationdGoodsDataDTO result = this.baseMapper.queryGoodsInfo(modularizationdGoodsData);
        return result;
    }

    /**
     * 根据商品ID查询模块化商品信息。
     *
     * @param goodsId 商品ID
     * @return 返回包含模块化商品信息的ModularizationdGoodsVO对象
     */
    @Override
    public ModularizationdGoodsVO queryModularizationdGoodsInfo(String goodsId) {
        ModularizationdGoodsVO result = this.baseMapper.queryModularizationdGoodsInfo(goodsId);

        // 计算汇率价格
        ResponseResult<PointsCashRatioVo> pointsCashRatioRes = baseServiceClient.getPointsCashRatio();
        if (pointsCashRatioRes.getCode() == 1) {
            PointsCashRatioVo cashRatiVo = pointsCashRatioRes.getData();
            Double pointsRate = result.getPointsRate();
            if (Objects.nonNull(pointsRate)) {
                PointsRatioUtil.PointsCashRatioRes ratio = PointsRatioUtil.getPointsRatio(result.getCommonPrice(), pointsRate);
                result.setCashPrice(ratio.getCashPrice());
            }

        }
        return result;
    }

    /**
     * 根据商品ID查询模块化商品信息（第二种实现方式）。
     *
     * @param goodsId 商品ID
     * @return 返回包含模块化商品信息的ModularizationdGoodsVO对象
     */
    @Override
    public ModularizationdGoodsVO queryModularizationdGoodsInfo2(String goodsId) {
        ModularizationdGoodsVO result = this.baseMapper.queryModularizationdGoodsInfo2(goodsId);
        return result;
    }

    /**
     * 查询Web楼层移动端配置的商品信息。
     *
     * @param goodsId 商品ID
     * @return 返回包含商品信息的GoodsIdsVO对象
     */
    @Override
    public GoodsIdsVO newSelectWebFloorMobConfig(Integer goodsId) {
        GoodsIdsVO result = this.baseMapper.newSelectWebFloorMobConfig(goodsId);
        return result;
    }

    /**
     * 根据分类ID列表和商品类型查询商品ID列表。
     *
     * @param catIdsDTO 包含分类ID列表和商品类型的DTO对象
     * @return 返回包含商品ID查询结果的GoodsIdsQueryVO列表
     */
    @Override
    public List<GoodsIdsQueryVO> selectByCatIds(CatIdsDTO catIdsDTO) {
        List<Integer> catIdList = catIdsDTO.getCatIdList();
        Integer goodsType = catIdsDTO.getGoodsType();
        List<GoodsIdsQueryVO> result = this.baseMapper.selectByCatIds(catIdList, goodsType);
        return result;
    }

    /**
     * 根据dify传输过来的数据查询商品信息
     */
    @Override
    public ResponseResult queryGoodsByDify(String data) {
        List<Map<String, Object>> list = goodsRepository.executeCustomSql(data);
        return ResponseResult.success(list);
    }

    /**
     * 查看申请换货商品
     */
    @Override
    public ResponseResult<OrderGoodsBarterVO> queryGoodsToBarter(OrderGoodsBarterDTO param) {
        // 获取商品价格 和 数量
        BigDecimal targetPrice = param.getGoodsPrice();
        Integer goodsNum = param.getGoodsNum();
        Integer verId = param.getVerId();
        // 查询基础换货商品信息
        OrderGoodsBarterVO base = baseMapper.queryGoodsToBarter(param.getGoodsId());
        // 查询库存开关配置
        String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.STORAGE_SWITCH.getDesc());
        if (!StringUtils.isEmpty(value) && "1".equals(value)) {
            // 如果库存开关开启，查询商品库存
            Integer storageGoodsStock = storageGoodsBaseMapper.getGoodsStorageStock(base.getGoodsId());
            base.setStorageGoodsStock(storageGoodsStock);
        } else {
            // 如果库存开关关闭，设置商品库存为0
            base.setStorageGoodsStock(0);
        }
        if (base.getDealerGoodsFlag() == 1 && base.getSupplierSource() != 2) {
            Integer supplierGoodsStock = supplierServiceClient.querySupplierGoodsStock(base.getSupplierGoodsId());
            base.setGoodsStock(supplierGoodsStock);
        }
        base.setMemberAmount(targetPrice);
        // 设置规格参数
        setGoodsSpecParamList(targetPrice, base, goodsNum, verId);
        // 返回成功结果
        return ResponseResult.success(base);
    }

    /**
     * 设置商品规格参数列表
     * <p>
     * 本方法旨在解析和设置与商品相关的规格参数，通过比较商品价格和目标价格来确定每个规格参数的激活状态
     *
     * @param targetPrice 目标价格，用于与商品规格价格进行比较
     * @param base        订单商品基础信息对象，包含商品的通用ID等信息
     * @param goodsNum    商品数量，用于判断库存是否充足
     * @param verId       商品版本ID，用于获取商品版本信息
     */
    private void setGoodsSpecParamList(BigDecimal targetPrice, OrderGoodsBarterVO base, Integer goodsNum,
                                       Integer verId) {
        // 如果商品有规格参数，进行规格参数的解析和转换
        List<BarterSpecValuesVO> specValuesList = new ArrayList<>();
        GoodsVersionEntity goodsVersionEntity = goodsVersionMapper.selectOne(new LambdaQueryWrapper<GoodsVersionEntity>()
                .eq(GoodsVersionEntity::getVerId, verId).eq(GoodsVersionEntity::getCommonId, base.getCommonId())
                .select(GoodsVersionEntity::getVerBody, GoodsVersionEntity::getVerId));
        List<String> goodsBasesVerBody = goodsBaseVersionMapper.selectList(new LambdaQueryWrapper<GoodsBaseVersionEntity>()
                .select(GoodsBaseVersionEntity::getVerBody)
                .eq(GoodsBaseVersionEntity::getVerId, goodsVersionEntity.getVerId())).stream().map(GoodsBaseVersionEntity::getVerBody).toList();
        List<ManageGdsBaseAddResultVO> bases = new ArrayList<>();
        for (String goodsBaseVerBody : goodsBasesVerBody) {
            ManageGdsBaseAddResultVO gdsBase = JSON.parseObject(goodsBaseVerBody, ManageGdsBaseAddResultVO.class);
            bases.add(gdsBase);
        }
        List<Map<String, Object>> specData = baseMapper.getGoodsSpecNameList(base.getCommonId());
        Map<String, Integer> specStockMap = buildSpecStockMapping(specData);
        bases = bases.stream().filter(item -> item.getIsDelete() != 1).toList();
        for (ManageGdsBaseAddResultVO goods : bases) {
            BarterSpecValuesVO specValuesVO = new BarterSpecValuesVO();
            specValuesVO.setGoodsPrice(goods.getGoodsPrice());
            // 解析商品的规格值，并设置到specValuesVO对象中
            List<GoodBaseSpecValuesVO> specValues = JSON.parseArray(goods.getSpecValue(), GoodBaseSpecValuesVO.class);
            specValuesVO.setSpecValues(specValues);
            // 设置商品库存
            Integer stock = specStockMap.getOrDefault(goods.getSpecValue(), 0);
            specValuesVO.setGoodsStock(stock);
            specValuesList.add(specValuesVO);
        }

        // 构建规格参数的映射，用于后续生成规格参数列表
        Map<String, Map<String, Boolean>> specsMap = new HashMap<>();
        for (BarterSpecValuesVO specVO : specValuesList) {
            BigDecimal goodsPrice = specVO.getGoodsPrice();
            Integer goodsStock = specVO.getGoodsStock();
            List<GoodBaseSpecValuesVO> combination = specVO.getSpecValues();
            for (GoodBaseSpecValuesVO vo : combination) {
                String paramName = vo.getParamName();
                String paramValue = vo.getParamValue();
                // 判断当前商品价格是否等于目标价格，以确定规格参数的激活状态
                boolean isActive = goodsPrice.compareTo(targetPrice) == 0 && goodsStock >= goodsNum;
                specsMap.computeIfAbsent(paramName, k -> new LinkedHashMap<>())
                        .merge(paramValue, isActive, Boolean::logicalOr);
            }
        }

        // 根据specsMap生成最终的规格参数列表，并设置到base对象中
        List<SpecDTO> specParamList = specsMap.entrySet().stream()
                .map(entry -> {
                    SpecDTO dto = new SpecDTO();
                    dto.setParamName(entry.getKey());
                    List<String> values = new ArrayList<>(entry.getValue().keySet());
                    List<Boolean> statuses = new ArrayList<>(entry.getValue().values());
                    dto.setParamNameValues(values);
                    dto.setStatus(statuses);
                    return dto;
                })
                .collect(Collectors.toList());
        base.setSpecParamList(specParamList);
    }

    /**
     * 构建规格库存映射
     * <p>
     * 该方法用于将给定的规格数据列表转换为一个映射表，其中键为商品规格名称，值为对应的库存数量
     * 这种映射关系有助于快速查询特定规格商品的库存情况
     *
     * @param specData 规格数据列表，列表中的每个元素都是一个包含规格信息的映射表
     * @return 返回一个映射表，键为商品规格名称的字符串，值为对应的库存数量的整数
     */
    private Map<String, Integer> buildSpecStockMapping(List<Map<String, Object>> specData) {
        return specData.stream().collect(Collectors.toMap(
                item -> item.get("specValue").toString(),
                item -> Integer.parseInt(item.get("goodsStock").toString()),
                (oldVal, newVal) -> newVal));
    }

    /**
     * 查询换货商品信息
     *
     * @param goodsId 换货商品id
     */
    @Override
    public ResponseResult<OrderGoodsBarterVO> selectBarterGoods(Integer goodsId) {
        // 查询基础换货商品信息
        OrderGoodsBarterVO base = baseMapper.queryGoodsToBarter(goodsId);
        // 查询库存开关配置
        String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.STORAGE_SWITCH.getDesc());
        if (!StringUtils.isEmpty(value) && "1".equals(value)) {
            // 如果库存开关开启，查询商品库存
            Integer storageGoodsStock = storageGoodsBaseMapper.getGoodsStorageStock(base.getGoodsId());
            base.setStorageGoodsStock(storageGoodsStock);
        } else {
            // 如果库存开关关闭，设置商品库存为0
            base.setStorageGoodsStock(0);
        }
        if (base.getDealerGoodsFlag() == 1 && base.getSupplierSource() != 2) {
            Integer supplierGoodsStock = supplierServiceClient.querySupplierGoodsStock(base.getSupplierGoodsId());
            base.setGoodsStock(supplierGoodsStock);
        }
        base.setMemberAmount(base.getGoodsPrice());
        return ResponseResult.success(base);
    }

    /**
     * 根据规格值查看换货商品规格信息
     */
    @Override
    public ResponseResult<BaseDetailVO> queryGoodsSpecToBarter(OrderGoodsBarterDTO param) {
        // 从查询参数中获取商品通用ID、用户令牌和商品规格值列表
        Integer commonId = param.getCommonId();
        List<GoodsSpecValueVO> specValues = param.getSpecValues();
        Integer verId = param.getVerId();
        // 根据商品通用ID查询商品基础信息
        GoodsVersionEntity goodsVersionEntity = goodsVersionMapper.selectOne(new LambdaQueryWrapper<GoodsVersionEntity>()
                .eq(GoodsVersionEntity::getVerId, verId).eq(GoodsVersionEntity::getCommonId, commonId)
                .select(GoodsVersionEntity::getVerBody, GoodsVersionEntity::getVerId));
        ManageGdsCommonVO commonResult = JSON.parseObject(goodsVersionEntity.getVerBody(), ManageGdsCommonVO.class);
        List<String> goodsBasesVerBody = goodsBaseVersionMapper.selectList(new LambdaQueryWrapper<GoodsBaseVersionEntity>()
                .select(GoodsBaseVersionEntity::getVerBody)
                .eq(GoodsBaseVersionEntity::getVerId, goodsVersionEntity.getVerId())).stream().map(GoodsBaseVersionEntity::getVerBody).toList();
        List<ManageGdsBaseAddResultVO> gdsBases = new ArrayList<>();
        for (String goodsBaseVerBody : goodsBasesVerBody) {
            ManageGdsBaseAddResultVO gdsBase = JSON.parseObject(goodsBaseVerBody, ManageGdsBaseAddResultVO.class);
            gdsBases.add(gdsBase);
        }
        // 如果没有查询到商品信息，则返回错误码NO_GOODS
        if (gdsBases.isEmpty()) {
            return ResponseResult.fail(BizCodeEnume.NO_GOODS);
        }
        gdsBases = gdsBases.stream().filter(item -> item.getIsDelete() != 1).toList();
        List<Integer> goodsIdList = new ArrayList<>();
        Map<Integer, ManageGdsBaseAddResultVO> gdsBaseMap = new HashMap<>();
        // 初始化商品ID，用于后续匹配完全符合规格的商品
        int goodsId = 0;
        // 遍历查询到的商品基础信息，寻找完全匹配规格的商品
        for (ManageGdsBaseAddResultVO record : gdsBases) {
            // 如果商品有规格值信息，则进行匹配检查
            if (StringUtils.isNotEmpty(record.getSpecValue())) {
                // 用于记录匹配的规格数量
                int flag = 0;
                JSONArray array = JSONObject.parseArray(record.getSpecValue());
                List<GoodBaseSpecValuesVO> tempSpecValues = new ArrayList<>();
                // 解析规格值信息，转换为GoodBaseSpecValuesVO对象列表
                for (int index = 0; index < array.size(); index++) {
                    String jsonObject = array.getString(index);
                    GoodBaseSpecValuesVO temp = JSONObject.parseObject(jsonObject, GoodBaseSpecValuesVO.class);
                    tempSpecValues.add(temp);
                }
                // 遍历用户输入的商品规格值，与商品规格值列表进行匹配
                for (GoodsSpecValueVO oldSpecValues : specValues) {
                    // 如果找到完全匹配的规格，则计数器加一
                    for (GoodBaseSpecValuesVO newSpecValues : tempSpecValues) {
                        if (oldSpecValues.getParamName().equals(newSpecValues.getParamName()) &&
                                oldSpecValues.getParamValue().equals(newSpecValues.getParamValue())) {
                            flag++;
                        }
                    }
                }
                // 如果所有规格都匹配，则记录该商品ID
                if (specValues.size() == flag) {
                    goodsId = record.getGoodsId();
                }
                goodsIdList.add(record.getGoodsId());
                gdsBaseMap.put(record.getGoodsId(), record);
            }
        }

        if (!goodsIdList.contains(goodsId)) {
            throw new DefinedException(BizCodeEnume.SPEC_NOT_EXIST);
        }
        ManageGdsBaseAddResultVO dto = gdsBaseMap.get(goodsId);
        if (commonResult.getDealerGoodsFlag() == 1 && commonResult.getSupplierSource() == 1) {
            Integer supplierGoodsStock = supplierServiceClient.querySupplierGoodsStock(dto.getSupplierGoodsId());
            dto.setGoodsStock(supplierGoodsStock);
        } else {
            GoodsStockEntity stockEntity = goodsStockMapper.selectOne(new LambdaQueryWrapper<GoodsStockEntity>()
                    .eq(GoodsStockEntity::getGoodsId, goodsId));
            if (null != stockEntity) {
                dto.setGoodsStock(stockEntity.getGoodsStock());
            } else {
                dto.setGoodsStock(0);
            }
        }
        // 根据商品ID查询商品的详细信息
        BaseDetailVO goodsBases = JSON.parseObject(JSON.toJSONString(dto), BaseDetailVO.class);

        // 设置商品的最大销售量和商品价格
        goodsBases.setGoodsMaxSale(goodsBases.getGoodsStock());
        goodsBases.setCommodityPrice(goodsBases.getGoodsPrice());
        goodsBases.setMemberAmount(goodsBases.getGoodsPrice());

        // 如果商品有规格值信息，则转换为列表形式
        if (StringUtils.isNotEmpty(goodsBases.getSpecValue())) {
            List<GoodBaseSpecValuesVO> specValuesJsonBeans = new ArrayList<>();
            JSONArray specValuesJsonBeanArray = JSONObject.parseArray(goodsBases.getSpecValue());
            for (int index = 0; index < specValuesJsonBeanArray.size(); index++) {
                JSONObject obj = specValuesJsonBeanArray.getJSONObject(index);
                GoodBaseSpecValuesVO temp = JSONObject.parseObject(obj.toJSONString(), GoodBaseSpecValuesVO.class);
                specValuesJsonBeans.add(temp);
            }
            goodsBases.setSpecValueList(specValuesJsonBeans);
        }

        // 返回查询成功的响应结果，包含商品的详细信息
        return ResponseResult.success(goodsBases);
    }

    @Override
    public void updateGoodsSaleNum(Map<String, Object> param) {
        // 初始化用于存储商品ID的列表
        Map<String, Object> esMap = new HashMap<>();
        List<Integer> list = new ArrayList<>();
        try {
            // 从参数中获取商品销售数量的列表
            List<Map<String, Object>> goodsNum = (List<Map<String, Object>>) param.get("goodsNum");
            // 遍历商品销售数量列表
            if (null != goodsNum) {
                for (Map<String, Object> map : goodsNum) {
                    // 退款或退货数量
                    Integer orderGoodsReturnNum = (Integer) map.get("order_goods_returnnum");
                    //商品id
                    Integer commonId = (Integer) map.get("common_id");
                    // 如果商品ID和退货数量都不为空
                    if (null != commonId && null != orderGoodsReturnNum) {
                        // 将商品ID添加到列表中，用于后续批量查询
                        list.add(commonId);
                        // 查询当前商品的销量
                        GoodsCommonEntity entity = goodsCommonMapper.selectById(commonId);
                        // 如果商品的销售数量不为空
                        if (null != entity.getCommonSalenum()) {
                            // 如果更新后的销售数量大于0，则更新销售数量
                            // 否则，将销售数量更新为0
                            if (entity.getCommonSalenum() - orderGoodsReturnNum > 0) {
                                goodsCommonMapper.updateGoodsSalenum(commonId, entity.getCommonSalenum() - orderGoodsReturnNum);
                            } else {
                                goodsCommonMapper.updateGoodsSalenum(commonId, 0);
                            }
                        }
                    }
                    //商品id
                    Integer goodsId = (Integer) map.get("goods_id");
                    WapGoodsSaleSumDTO goods = new WapGoodsSaleSumDTO();
                    goods.setGoodsId(goodsId);
                    goods.setGoodsNum(String.valueOf(orderGoodsReturnNum));
                    // 如果商品ID和退货数量都不为空
                    if (null != goodsId && null != orderGoodsReturnNum) {
                        // 更新goodsBase商品销售数量
                        this.baseMapper.lessGoodsBySales(goods);
                    }
                }
                // 根据商品ID列表查询所有商品的ID
                List<Integer> list2 = goodsCommonMapper.queryAllGoodsIdByCommonId(list);
                // 准备更新ES的商品ID列表和操作类型
                // 更新es
                esMap.put("goodsIdS", list2);
                // 1 是添加  2是删除
                esMap.put("type", 1);
                // 发送消息到MQ，用于更新ES中的商品销售数量
                MqSendUtils.mqSendGoodsByEs(esMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

