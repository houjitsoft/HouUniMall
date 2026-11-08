package com.houjiang.goods.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.houjiang.commons.aspect.UserPermissionsResult;
import com.houjiang.commons.aspect.UserService;
import com.houjiang.commons.config.ThreadPoolConfig;
import com.houjiang.commons.util.SecurityContextUtils;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.base.DealerGoodsUserInfoDTO;
import com.houjiang.commons.dto.goods.*;
import com.houjiang.commons.dto.oos.OssUploadUpdateParamDTO;
import com.houjiang.commons.dto.order.WapGoodsSaleSumDTO;
import com.houjiang.commons.dto.shop.ManageShopVideoDTO;
import com.houjiang.commons.dto.shop.ShopIndexDTO;
import com.houjiang.commons.dto.shop.WebShopListQueryEsDTO;
import com.houjiang.commons.dto.supplier.SupplierGoodsIsShelvesDTO;
import com.houjiang.commons.dto.userms.UserInfoDTO;
import com.houjiang.commons.dto.yiyatong.*;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.exception.DefinedException;
import com.houjiang.commons.rocketmq.MqSendUtils;
import com.houjiang.commons.util.*;
import com.houjiang.commons.util.json.JsonUtils;
import com.houjiang.commons.util.excel.Constants;
import com.houjiang.commons.util.excel.ExcelsUtil;
import com.houjiang.commons.util.querypage.PageHelper;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.activity.*;
import com.houjiang.commons.vo.base.*;
import com.houjiang.commons.vo.goods.*;
import com.houjiang.commons.vo.goodscat.GoodsCatSearchChildVO;
import com.houjiang.commons.vo.goodscat.WebCatMenuVO;
import com.houjiang.commons.vo.order.DealerOrderGoodsVO;
import com.houjiang.commons.vo.order.WapShippingQueryResultVO;
import com.houjiang.commons.vo.shop.*;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.commons.vo.user.UserPermissionsVO;
import com.houjiang.commons.vo.yiyatong.ProductSelectionCenteGoodsVO;
import com.houjiang.commons.vo.yiyatong.base.OpenSkuInfoBySpuIdVO;
import com.houjiang.commons.vo.yiyatong.base.PropertysVO;
import com.houjiang.commons.vo.yiyatong.base.SkuSpecVO;
import com.houjiang.goods.dto.*;
import com.houjiang.goods.entity.*;
import com.houjiang.goods.feign.*;
import com.houjiang.goods.mapper.*;
import com.houjiang.goods.service.GoodsCommonService;
import com.houjiang.goods.service.GoodsStockService;
import com.houjiang.goods.service.SupplierGoodsYiyatongService;
import com.houjiang.goods.service.YiYaTongService;
import com.houjiang.goods.vo.*;
import com.houjiang.goods.vo.VoucherMessageVO;
import jakarta.annotation.Resource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * @ClassName GoodsCommonServiceImpl
 * @Description 商品表
 * @Author Amos
 * @Date 2023-05-25 09:16:35
 * @Version 1.0
 * @author Amos
 */
@Service
public class GoodsCommonServiceImpl extends ServiceImpl<GoodsCommonMapper, GoodsCommonEntity>
        implements GoodsCommonService {
    private static final String PATTERN = "\n\t(.*?)</p>";
    /** 引入线程池 */
    private static final ExecutorService executorService = ThreadPoolConfig.executorService;

    @Autowired
    private BaseServiceClient baseServiceClient;
    @Autowired
    private MerchantServiceClient merchantServiceClient;
    @Autowired
    private OrderServiceClient orderServiceClient;
    @Autowired
    private ActivityServiceClient activityServiceClient;
    @Autowired
    private UserServiceClient userServiceClient;
    @Resource
    private GoodsImagesMapper goodsImagesMapper;
    @Resource
    private GoodsBaseMapper goodsBaseMapper;
    @Resource
    private GoodsUserFootprintMapper goodsUserFootprintMapper;
    @Resource
    private GoodsCommonDetailMapper goodsCommonDetailMapper;
    @Resource
    private GoodsCommonAttributeAssociationMapper goodsCommonAttributeAssociationMapper;
    @Resource
    private GoodsAttributeMapper goodsAttributeMapper;
    @Resource
    private GoodsAttributeContentMapper goodsAttributeContentMapper;
    @Resource
    private GoodsCatMapper goodsCatMapper;
    @Resource
    private GoodsCommonMapper goodsCommonMapper;
    @Resource
    private StorageGoodsBaseMapper storageGoodsBaseMapper;
    @Resource
    private ActivityNoneGoodsMapper activityNoneGoodsMapper;
    @Resource
    private GoodsStockService goodsStockService;
    @Resource
    private GoodsCategoryAttributesMapper goodsCategoryAttributesMapper;
    @Autowired
    private ThirdPartyServiceClient thirdPartyServiceClient;
    @Autowired
    private YiYaTongService yiYaTongService;

    @Autowired
    private SupplierGoodsYiyatongService supplierGoodsYiyatongService;

    /**
     * 二开取价网关：官方价（含千客千价）算完后回调 {@code PriceCalcExtension}，写回最终价。
     */
    @Autowired
    private com.houjiang.goods.plugin.PriceCalcGateway priceCalcGateway;
    /**
     * 二开库存预占网关：库存扣减前回调 {@code StockLockExtension}，可中断（限购 / 外部可用量校验）。
     */
    @Autowired
    private com.houjiang.goods.plugin.StockLockGateway stockLockGateway;
    /**
     * 二开库存变动网关：库存扣减落库后回调 {@code StockChangeExtension}，用于同步外部仓储。
     */
    @Autowired
    private com.houjiang.goods.plugin.StockChangeGateway stockChangeGateway;

    @Autowired
    private YiyatongMsgCallbackMapper msgCallbackMapper;

    @Resource
    private SupplierServiceClient supplierServiceClient;

    @Lazy
    @Resource
    private AsyncVersionServiceImpl asyncVersionService;

    @Autowired
    private GoodsJingdongDropshippingMapper goodsJingdongDropshippingMapper;

    @Autowired
    private SupplierGoodsYiyatongMapper supplierGoodsYiyatongMapper;

    @Autowired
    private GoodsDealerPutawayRecordMapper goodsDealerPutawayRecordMapper;
    private static final Logger logger = LoggerFactory.getLogger(GoodsCommonServiceImpl.class);

    /**
     * 查询商品详情
     *
     * @param param 商品详情查询参数，包含商品ID等信息
     * @return 返回包含商品详情的响应结果
     */
    @Override
    public ResponseResult queryGoodsDetail(GoodsDetailsDTO param) {
        //查看plus状态
        Integer selectplus = (Integer) baseServiceClient.queryMerchantPlusRule().getData();

        GdsDetailPageResultVO detailPage = new GdsDetailPageResultVO();
        //获取用户信息
        ObjectMapper mapper = new ObjectMapper();
        // 获取user信息
        UserInfoVO userinfo = ParseTokenUtils.getUserInfoByTokenNoIsExist(param.getToken());
        String userId = null;
        if (null != userinfo) {
            userId = userinfo.getUserId();
            // 插入用户访问记录
            UserAccess userAccess = new UserAccess();
            userAccess.setUserId(userinfo.getUserId());
            userAccess.setUserName(userinfo.getUserName());
            userAccess.setGoodsId(param.getGoodsId());
            baseMapper.addUserAccessData(userAccess);
        }


        // 查询商品详情
        GdsBaseDetailResultVO goodsBases = baseMapper.selectByGoodsId(param.getGoodsId());

        // 千客千价：根据用户与商品计算个性化价格
        if (goodsBases != null && goodsBases.getGoodsPrice() != null) {
            try {
                Map<String, Object> priceParam = new HashMap<>();
                priceParam.put("goodsId", goodsBases.getGoodsId());
                // 当前实现先按商品维度个性化定价，SKU 维度后续按需扩展
                priceParam.put("skuId", null);
                priceParam.put("userId", userId);
                priceParam.put("basePrice", goodsBases.getGoodsPrice());
                ResponseResult<Map<String, Object>> priceResult = activityServiceClient.queryThousandsGuestsPersonalPrice(priceParam);
                if (priceResult != null && priceResult.getCode() == CommonStatusEnum.SUCCESS.getCode()
                        && priceResult.getData() != null && priceResult.getData().get("personalPrice") != null) {
                    Object personalPriceObj = priceResult.getData().get("personalPrice");
                    BigDecimal personalPrice = new BigDecimal(String.valueOf(personalPriceObj));
                    // 使用个性化价格覆盖商品基础价格
                    goodsBases.setGoodsPrice(personalPrice);
                }
            } catch (Exception e) {
                // 千客千价定价失败时，不影响原有详情逻辑，直接使用基础价格
                log.error("queryGoodsDetail thousandsGuestsPrice error, goodsId=" + param.getGoodsId() + ", error: " + e.getMessage());
            }
        }

        // 计算汇率价格
        ResponseResult<PointsCashRatioVo> pointsCashRatioRes = baseServiceClient.getPointsCashRatio();
        if (pointsCashRatioRes.getCode() == 1) {
            PointsCashRatioVo pointsCashRatio = pointsCashRatioRes.getData();
            BigDecimal goodsPrice = goodsBases.getGoodsPrice();

            // 根据设置比例，计算商品价格
            Double pointsRate = goodsBases.getPointsRate();

            if (Objects.nonNull(pointsRate)) {
                PointsRatioUtil.PointsCashRatioRes ratio = PointsRatioUtil.getPointsRatio(goodsPrice, pointsRate);

                // 设置现金金额
                goodsBases.setCashPrice(ratio.getCashPrice());
            }
        }


        if (null == goodsBases) {
            return ResponseResult.fail(2, "商品不存在");
        }
        // 二开取价（after*）：官方价（含千客千价）算完、写库前回调 PriceCalcExtension，回读最终价并写回。
        // 领域夹取（最终价 ∈ [0, 原价]）由网关完成，二开返回越界值不会污染主链路。
        if (goodsBases.getGoodsId() != null && goodsBases.getGoodsPrice() != null) {
            Map<String, Object> priceExtra = new HashMap<>();
            priceExtra.put("userId", userId);
            com.houjiang.goods.plugin.PriceCalcGateway.Result priceResult = priceCalcGateway.calc(
                    goodsBases.getGoodsId().longValue(), goodsBases.getGoodsPrice(), priceExtra);
            if (priceResult.finalPrice() != null) {
                goodsBases.setGoodsPrice(priceResult.finalPrice());
            }
        }
        RedisUtil.set(HouConfig.GOODS_STOCK + goodsBases.getGoodsId(), String.valueOf(goodsBases.getGoodsStock()));
        //设置plus折扣
        if (userinfo != null) {
            goodsBases.setPlusMember(userinfo.getPlusMember());
            goodsBases.setPlusDiscount(userinfo.getPlusDiscount());
        }

        // 如果商品的最大销售量设置为0，则将其设置为库存量
        if (goodsBases.getGoodsMaxSale().equals(0)) {
            goodsBases.setGoodsMaxSale(goodsBases.getGoodsStock());
        }

        // 如果商品未上架、状态不为在售或未通过审核，则将库存量设为0
        if (!goodsBases.getGoodsIsShelves().equals(1) || !goodsBases.getCommonState().equals(1)
                || !goodsBases.getCommonVerify().equals(1)) {
            goodsBases.setGoodsStock(0);
        }

        // 如果商品的规格值不为空，则解析规格值并将其转换为对象列表
        if (StringUtils.isNotEmpty(goodsBases.getSpecValue())) {
            List<GoodBaseSpecValuesJsonBeanVO> specValuesJsonBeans = new ArrayList<>();
            JSONArray specValuesJsonBeanArray = com.alibaba.fastjson.JSONObject.parseArray(goodsBases.getSpecValue());
            for (int index = 0; index < specValuesJsonBeanArray.size(); index++) {
                com.alibaba.fastjson.JSONObject obj = specValuesJsonBeanArray.getJSONObject(index);
                GoodBaseSpecValuesJsonBeanVO temp = JSONObject.parseObject(obj.toJSONString(), GoodBaseSpecValuesJsonBeanVO.class);
                specValuesJsonBeans.add(temp);
            }
            goodsBases.setSpecValueList(specValuesJsonBeans);
        }

        // 获取商品的基本信息ID和店铺ID，用于后续查询发票开关状态
        Integer commonId = goodsBases.getCommonId();
        Integer shopId = goodsBases.getShopId();

        // 初始化查询结果变量
        Integer result = 0;

        // 准备参数查询店铺的发票开关状态
        Map<String, Object> paramTmp = new HashMap<>();
        paramTmp.put("shopId", shopId);

        // 调用业务服务客户端查询店铺的发票开关状态
        InvoiceSwitchEntityVO data = merchantServiceClient.queryShopInvoiceSwitch(paramTmp);

        // 根据查询结果设置商品的发票状态
        if (null != data) {
            Integer invoiceSwitchStatus = data.getInvoiceSwitchStatus();
            Integer invoiceAuditStatus = data.getInvoiceAuditStatus();
            if (1 == invoiceAuditStatus && 1 == invoiceSwitchStatus) {
                result = 1;
            }
        }


        Integer catId = null;

        if (null != goodsBases) {
            detailPage.setShopGate(0);
            GdsShopDetailResultVO shopDetail = merchantServiceClient.queryShopinfoByShopId(shopId);
            //判断是否为该商铺的门店账号
            if (null != userinfo) {
                Map<String, String> map = new HashMap<>();
                map.put("userMobile", userinfo.getUserMobile());
                Integer phoneShopId = merchantServiceClient.queryUserIsGate(map);
                if (phoneShopId != null) {
                    if (phoneShopId.equals(shopDetail.getShopId())) {
                        detailPage.setShopGate(1);
                    }
                }
            }

            if (shopDetail != null) {
                // 设置店铺默认值
                shopDetail.setInvoiceActive(result);
                setShopDefaultValue(goodsBases, shopDetail);
            }
            // 查询公共信息
            GdsCommonResultVO gdsCommon = baseMapper.selectTransport(commonId);
            //转换依靠下列两行即可		售后服务List
            List<String> afterServiceList = ObjectChangeList.castObjToList(JSON.parse(gdsCommon.getAfterService()), String.class);
            List<GoodsCategoryAttributesEntity> goodsCategoryAttributes = goodsCategoryAttributesMapper.selectList(
                    new LambdaQueryWrapper<GoodsCategoryAttributesEntity>().eq(GoodsCategoryAttributesEntity::getCommonId, commonId)
                            .orderByAsc(GoodsCategoryAttributesEntity::getDisplayOrder));
            gdsCommon.setCategoryAttributesList(goodsCategoryAttributes);
            gdsCommon.setAfterServiceList(afterServiceList);
            if (StringUtils.isNotEmpty(gdsCommon.getSpecParam())) {
                List<SpecParamJsonBeanVO> list = new ArrayList<>();
                JSONArray jsonSpecParam = com.alibaba.fastjson.JSONObject.parseArray(gdsCommon.getSpecParam());
                for (int index = 0; index < jsonSpecParam.size(); index++) {
                    com.alibaba.fastjson.JSONObject obj = jsonSpecParam.getJSONObject(index);
                    SpecParamJsonBeanVO temp = JSONObject.parseObject(obj.toJSONString(), SpecParamJsonBeanVO.class);
                    list.add(temp);
                }
                gdsCommon.setSpecParamList(list);
            }
            //如果商品未参加活动商品原价就是市场价，否则商品活动价即为商品销售价 原商品销售价赋值为商品市场价
            if (null != goodsBases.getCommonIsXian() && 0 != goodsBases.getCommonIsXian()) {
                goodsBases.setGoodsMarketPrice(goodsBases.getGoodsPrice());
            }
            // 活动
            ActivityGoodsDetailDTO activityGoodsDetail = dealActivity2(goodsBases.getGoodsId(), goodsBases.getGoodsStock(),
                    goodsBases.getGoodsMaxSale(), commonId, goodsBases.getCommonIsXian(), userId, param.getSessionTime());

            if (null != activityGoodsDetail && !activityGoodsDetail.getActivityStatus().equals(0)) {
                goodsBases.setActivityStatus(activityGoodsDetail.getActivityStatus());
                goodsBases.setActivityGoodsDetailDTO(activityGoodsDetail);
                goodsBases.setCurrentCartQuantity(activityGoodsDetail.getCurrentCartQuantity());
                if (!activityGoodsDetail.getActivityStatus().equals(4)) {
                    goodsBases.setGoodsMaxSale(activityGoodsDetail.getGoodsUpperLimit());
                    goodsBases.setGoodsMinSale(activityGoodsDetail.getGoodsLowerLimit());
                }
                //满减赠折活动展示活动内容ZZZ
                if (activityGoodsDetail.getActivityStatus().equals(5)) {
                    String promotionContent = "";
                    List<ManjianzengzhePromotionContentQueryResultVO> promotionContents = activityServiceClient.selectReductionPolicyPromotionContent(commonId);
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
                            goodsBases.setCommonIsXian(6);
                            goodsBases.setActivityStatus(6);
                            goodsBases.setPromotionType("满折扣");
                        }
                        if ("满减".equals(goodsBases.getPromotionType()) && hasGiveaway == 1) {
                            goodsBases.setPromotionType("满减赠");
                        } else if ("满赠".equals(goodsBases.getPromotionType()) && !mzFlag) {
                            goodsBases.setPromotionType("满减赠");
                        }
                        dealPromotionContent(r);

                        promotionContent = promotionContent + r.getPromotionContent() + "\n";

                    }
                    goodsBases.setPromotionContent(promotionContent);
                }
            }
            catId = gdsCommon.getCatId();
            // 查询门店商品库存
            if (null != param.getShopGateId()) {
                Integer shopGateGoodsStock = baseMapper.queryGoodsGateStock(goodsBases.getGoodsId(), param.getShopGateId().toString());
                if (shopGateGoodsStock != null) {
                    goodsBases.setGoodsStock(shopGateGoodsStock);
                } else {
                    goodsBases.setGoodsStock(0);
                }
            }
            // 查询图片信息
            List<GdsImageDetailResultVO> goodsImages = goodsImagesMapper.selectByCommonId(param.getGoodsId(),
                    shopId);
            if (CollectionUtils.isEmpty(goodsImages)) {
                GdsImageDetailResultVO goodsImage = new GdsImageDetailResultVO();
                goodsImage.setImagesImage(gdsCommon.getCommonImage());
                goodsImage.setImagesDisplayorder(0);
                goodsImage.setImagesIsDefault(1);
                goodsImages.add(goodsImage);
            }
            // 处理结果
            detailPage.setGoodsBases(goodsBases);
            detailPage.setGoodsImages(goodsImages);
            detailPage.setShopDetail(shopDetail);
            detailPage.setGdsCommon(gdsCommon);

            // 处理供应商商品
            if (null != goodsBases.getGoodsType() && goodsBases.getGoodsType() == 2) {
                BaseGoodsConfigVO manageGdsGoodsConfig = goodsBaseMapper.queryDelayGoodsConfig(goodsBases.getCommonId());
                detailPage.setManageGdsGoodsConfig(manageGdsGoodsConfig);
                if (manageGdsGoodsConfig != null) {
                    if (null == userinfo) {
                        goodsBases.setOperateType2(manageGdsGoodsConfig);
                    } else {
                        if (manageGdsGoodsConfig.getSellCheck() == 1) {
                            // 需要经销审核
                            DealerGoodsUserInfoDTO goodsUserInfo = new DealerGoodsUserInfoDTO(goodsBases.getShopId(), userinfo.getShopId());
                            goodsUserInfo.setDealerStatus(CommonType.supplierGoodsStatus.AUTH_PASS.getCode());
                            DealerApplyInfoVO dealerApplyInfo = baseServiceClient.queryDealerApplyByShop(goodsUserInfo);
                            if (null == dealerApplyInfo) {
                                goodsBases.setOperateType(CommonType.supplierGoodsStatus.APPLY.getCode());
                            }
                        }
                        if (null == goodsBases.getOperateType()) {
                            // 一键上架商品判断商品是否已经销
                            if (manageGdsGoodsConfig.getSellDeliver() == 1) {
//                                Integer dealerGoodsIdCount = goodsBaseMapper.queryGoodsDealerInfo(userinfo.getShopId(), goodsBases.getGoodsId());
                                DealerPutawayRecordVO putawayRecord = new DealerPutawayRecordVO(goodsBases.getCommonId(), null, userinfo.getShopId(), goodsBases.getShopId(), CommonType.putawayState.NORMAL.getCode(), CommonType.supplierGoodsStatus.PUTAWAY.getCode());
                                putawayRecord = baseMapper.queryPutawayRecord(putawayRecord);
                                if (null != putawayRecord) {
                                    goodsBases.setOperateType(CommonType.supplierGoodsStatus.HAVA_GOODS.getCode());
                                } else {
                                    goodsBases.setOperateType(CommonType.supplierGoodsStatus.PUTAWAY.getCode());
                                }
                            } else {
                                goodsBases.setOperateType(CommonType.supplierGoodsStatus.BUY.getCode());
                            }
                        }
                    }
                }
            }
        }
        detailPage.setPlusStatus(selectplus);

        // 更新点击量
        goodsBaseMapper.updateGoodsClick(param.getGoodsId());
        baseMapper.updateGoodsClick(commonId);

        // 添加足迹
        if (null != userinfo) {
            merchantServiceClient.setShopViewVolume(userinfo.getUserId(), shopId, 1, goodsBases.getGoodsId());
            addUserFoot(userinfo, commonId, catId);
        }

        return ResponseResult.success(detailPage);
    }

    /**
     * 添加用户足迹
     *
     * @param userinfo 商品信息
     * @param commonId 商品ID
     * @param catId    商品类别ID
     */
    @Override
    public void addUserFoot(UserInfoVO userinfo, Integer commonId, Integer catId) {
        WapUserFootPrintAddParamDTO userFoot = new WapUserFootPrintAddParamDTO();
        // 获取用户id
        String userId = userinfo.getUserId();
        // 查询商品是否已添加到足迹
        UserFootprintDTO selectByUserId = goodsUserFootprintMapper.selectByUserId(userId, commonId);
        if (selectByUserId == null) {
            userFoot.setUserId(userId);
            userFoot.setCatId(catId);
            userFoot.setFootprintDate(DateUtils.getDate());
            userFoot.setFootprintTime(DateUtils.getNowDateToString());
            userFoot.setCommonId(commonId);
            goodsUserFootprintMapper.addUserFoot(userFoot);
        } else {
            selectByUserId.setFootprintDate(DateUtils.getDate());
            selectByUserId.setFootprintTime(DateUtils.getNowDateToString());
            goodsUserFootprintMapper.updateUserFoot(selectByUserId);
        }
    }

    /**
     * 设置店铺默认值
     *
     * @param goodsBases 商品基础信息
     * @param shopDetail 店铺详细信息
     */
    private void setShopDefaultValue(GdsBaseDetailResultVO goodsBases, GdsShopDetailResultVO shopDetail) {
        // 为店铺logo设置默认值
        if (shopDetail.getShopLogo() == null || shopDetail.getWapShopLogo() == null) {
            String configValue = baseServiceClient.selectConfigValueFeign("logo_copywriting_ico_shop");

            if (shopDetail.getShopLogo() == null) {
                shopDetail.setShopLogo(configValue);
            }
            if (shopDetail.getWapShopLogo() == null) {
                shopDetail.setWapShopLogo(configValue);
            }
        }
        // 计算店铺总体评分
        Map<String, BigDecimal> shopEvaluation = baseMapper.selectShopEvaluation(shopDetail.getShopId());
        BigDecimal evaluation = BigDecimal.ZERO;
        for (BigDecimal num : shopEvaluation.values()) {
            // 将每个元素累加到sum变量中
            evaluation = evaluation.add(num);
        }
        shopDetail.setOverallScore(Double.parseDouble(evaluation.divide(new BigDecimal(3), 2, RoundingMode.HALF_UP).toString()));
        // 查询商品数量
        Integer shopCount = baseMapper.selectByShopId(goodsBases.getShopId());
        shopDetail.setShopCount(shopCount);
    }

    /**
     * 处理活动商品详情
     * 根据商品参与的不同活动类型，设置相应的活动商品详情信息
     *
     * @param goodsId      商品ID
     * @param goodsStock   商品库存
     * @param goodsMaxSale 商品最大销售量
     * @param commonId     活动ID
     * @param commonIsXian 商品参与的活动类型 0不参加,1砍价,2秒杀,3限时折扣,4拼团,5满减赠折
     * @param userId       用户ID
     * @param sessionTime  会话时间
     * @return 返回处理后的活动商品详情DTO
     */
    @Override
    public ActivityGoodsDetailDTO dealActivity2(Integer goodsId, Integer goodsStock, Integer goodsMaxSale, Integer commonId, Integer commonIsXian, String userId, Date sessionTime) {
        // 活动商品详情
        ActivityGoodsDetailDTO activityGoodsDetail = null;
        // 当前购物车中的商品数量
        Integer currentCartQuantity = 0;

        // 判断商品是否参加促销活动
        if (!commonIsXian.equals(0)) {
            activityGoodsDetail = new ActivityGoodsDetailDTO();

            // 如果商品参与秒杀活动
            if (commonIsXian.equals(2)) {
                if (null != userId) {
                    // 查询当前商品在购物车的数量
                    currentCartQuantity = orderServiceClient.selectCurrentCartQuantity(userId, goodsId);
                }
                // 处理秒杀活动
                commonIsXian = dealSeckillActivity1(commonId, goodsId, goodsStock, commonIsXian, activityGoodsDetail, sessionTime);
            }
            // 如果商品参与限时折扣活动
            else if (commonIsXian.equals(3)) {
                if (null != userId) {
                    // 查询当前商品在购物车的数量
                    currentCartQuantity = orderServiceClient.selectCurrentCartQuantity(userId, goodsId);
                }
                // 处理限时折扣活动
                commonIsXian = dealDiscountActivity(goodsId, goodsMaxSale, commonIsXian, activityGoodsDetail);
            }
            // 如果商品参与拼团活动
            else if (commonIsXian.equals(4)) {
                // 处理拼团活动
                commonIsXian = dealPintuanActivity(commonId, commonIsXian, goodsId, activityGoodsDetail);
            }
            // 如果商品参与满减赠折活动
            else if (commonIsXian.equals(5)) {
                // 处理满减赠折活动
                commonIsXian = dealManjianzengzheActivity(commonId, goodsMaxSale, commonIsXian, goodsId, activityGoodsDetail);
            }

            // 设置活动状态和购物车中的商品数量到活动商品详情DTO
            activityGoodsDetail.setActivityStatus(commonIsXian);
            activityGoodsDetail.setCurrentCartQuantity(currentCartQuantity);
        }

        // 返回处理后的活动商品详情DTO
        return activityGoodsDetail;
    }


    /**
     * 处理秒杀活动详情
     *
     * @param commonId            活动通用ID
     * @param goodsId             商品ID
     * @param goodsStock          商品库存
     * @param commonIsXian        是否为常见限时活动
     * @param activityGoodsDetail 活动商品详情DTO
     * @param sessionTime         会话时间
     * @return 更新后的commonIsXian值
     */
    public Integer dealSeckillActivity1(Integer commonId, Integer goodsId, Integer goodsStock, Integer commonIsXian,
                                        ActivityGoodsDetailDTO activityGoodsDetail, Date sessionTime) {
        // 创建映射以存储查询参数
        Map<String, Object> map = new HashMap<>();
        map.put("commonId", commonId);
        map.put("goodsId", goodsId);
        map.put("sessionTime", sessionTime);
        // 通过参数查询秒杀商品信息
        AppSeckillGoodsQueryResultVO seckillGoods = activityServiceClient.selectSeckillByGoodsId(map);

        // 如果查询到秒杀商品信息
        if (null != seckillGoods) {
            // 检查活动是否在进行中
            if (DateUtils.compareDate2(seckillGoods.getGoodsEndTime())) {
                // 计算活动剩余时间
                Long overplusTime = DateUtils.getSecondByMinusDate(new Date(), seckillGoods.getGoodsEndTime());
                activityGoodsDetail.setActivityEndTime(seckillGoods.getGoodsEndTime());

                // 判断活动是否已开始
                if (DateUtils.compareDate2(seckillGoods.getGoodsStartTime())) {
                    activityGoodsDetail.setActivityStartStatus(0);
                    overplusTime = DateUtils.getSecondByMinusDate(new Date(), seckillGoods.getGoodsStartTime());
                } else {
                    activityGoodsDetail.setActivityStartStatus(1);
                }
                // 设置价格下限
                activityGoodsDetail.setGoodsLowerLimit(0);
                // 设置价格上限
                if (!seckillGoods.getGoodsUpperLimit().equals(0)) {
                    activityGoodsDetail.setGoodsUpperLimit(seckillGoods.getGoodsUpperLimit());
                } else {
                    activityGoodsDetail.setGoodsUpperLimit(0);
                }

                // 更新商品库存为秒杀活动中的较小值
                if (seckillGoods.getSeckillNum() != null && seckillGoods.getSeckillNum() < goodsStock) {
                    activityGoodsDetail.setGoodsStock(seckillGoods.getSeckillNum());
                } else {
                    activityGoodsDetail.setGoodsStock(goodsStock);
                }

                // 设置活动相关详情
                activityGoodsDetail.setCommonStock(seckillGoods.getSeckillNums() != null ? seckillGoods.getSeckillNums() : 0);
                activityGoodsDetail.setDiscountTitle(seckillGoods.getSeckillTitle());
                activityGoodsDetail.setOverplusTime(overplusTime);
                activityGoodsDetail.setActivityPrice(seckillGoods.getSeckillPrice());
                activityGoodsDetail.setActivityStartTime(seckillGoods.getGoodsStartTime());
                activityGoodsDetail.setActivityId(seckillGoods.getSeckillId());
            } else {
                // 如果活动已结束，设置commonIsXian为0
                commonIsXian = 0;
            }
        } else {
            // 如果没有查询到秒杀商品信息，设置commonIsXian为0
            commonIsXian = 0;
        }
        return commonIsXian;
    }

    /**
     * 处理折扣活动详情
     *
     * @param goodsId             商品ID
     * @param goodsMaxSale        商品最大销量
     * @param commonIsXian        是否为常见折扣活动
     * @param activityGoodsDetail 活动商品详情DTO
     * @return 更新后的commonIsXian值
     */
    private Integer dealDiscountActivity(Integer goodsId, Integer goodsMaxSale, Integer commonIsXian,
                                         ActivityGoodsDetailDTO activityGoodsDetail) {
        // 通过商品ID查询折扣商品信息
        AppDiscountGoodsQueryResultVO discountGoods = activityServiceClient.selectDiscountByGoodsId(goodsId);
        Long overplusTime = null;

        // 如果查询到折扣商品信息
        if (null != discountGoods) {
            // 检查活动是否在进行中
            if (DateUtils.compareDate2(discountGoods.getGoodsEndTime())) {
                activityGoodsDetail.setActivityEndTime(discountGoods.getGoodsEndTime());
                activityGoodsDetail.setActivityStartStatus(discountGoods.getDiscountGoodsState());
                activityGoodsDetail.setDiscountAmount(discountGoods.getDiscountAmount());
                activityGoodsDetail.setDiscountTitle(discountGoods.getDiscountTitle());
                activityGoodsDetail.setActivityStartTime(discountGoods.getGoodsStartTime());
                activityGoodsDetail.setActivityId(discountGoods.getDiscountId());

                // 根据活动状态计算剩余时间
                if (null != discountGoods.getDiscountGoodsState() && 1 == discountGoods.getDiscountGoodsState()) {
                    overplusTime = DateUtils.getSecondByMinusDate(new Date(), discountGoods.getGoodsEndTime());
                } else if (null != discountGoods.getDiscountGoodsState() && 4 == discountGoods.getDiscountGoodsState()) {
                    overplusTime = DateUtils.getSecondByMinusDate(new Date(), discountGoods.getGoodsStartTime());
                }

                // 设置价格下限
                if (!discountGoods.getGoodsLowerLimit().equals(0)) {
                    activityGoodsDetail.setGoodsLowerLimit(discountGoods.getGoodsLowerLimit());
                } else {
                    activityGoodsDetail.setGoodsLowerLimit(0);
                }

                // 设置折扣上限时间
                if (!discountGoods.getDiscountUpperTime().equals(0)) {
                    activityGoodsDetail.setDiscountUpperTime(discountGoods.getDiscountUpperTime());
                } else {
                    activityGoodsDetail.setDiscountUpperTime(0);
                }

                // 设置价格上限
                if (!discountGoods.getGoodsUpperLimit().equals(0)) {
                    activityGoodsDetail.setGoodsUpperLimit(discountGoods.getGoodsUpperLimit());
                } else {
                    activityGoodsDetail.setGoodsUpperLimit(0);
                }

                // 更新商品购买数量
                activityGoodsDetail.setGoodsBuyCount(discountGoods.getGoodsBuyCount() != null ? discountGoods.getGoodsBuyCount() : 0);
                activityGoodsDetail.setOverplusTime(overplusTime);
                activityGoodsDetail.setActivityPrice(discountGoods.getDiscountPrice());
            } else {
                // 如果活动已结束，设置commonIsXian为0
                commonIsXian = 0;
            }
        } else {
            // 如果没有查询到折扣商品信息，设置commonIsXian为0
            commonIsXian = 0;
        }
        return commonIsXian;
    }

    /**
     * 处理拼团活动
     *
     * @param commonId            活动标识ID
     * @param commonIsXian        是否参与拼团的标识，1表示参与，0表示不参与
     * @param goodsId             商品ID
     * @param activityGoodsDetail 商品活动详情对象
     * @return 更新后的commonIsXian值
     */
    private Integer dealPintuanActivity(Integer commonId, Integer commonIsXian, Integer goodsId,
                                        ActivityGoodsDetailDTO activityGoodsDetail) {
        // 通过服务客户端根据商品ID获取拼团商品信息
        AppPintuanGoodsQueryResultVO pintuanGoods = activityServiceClient.selectpiecingTogetherByGoodsId(commonId, goodsId);
        if (pintuanGoods != null) {
            // 比较当前时间与活动结束时间，判断活动是否仍在进行
            if (DateUtils.compareDate2(pintuanGoods.getEndTime())) {
                // 计算剩余时间
                Long overplusTime = DateUtils.getSecondByMinusDate(new Date(), pintuanGoods.getEndTime());
                // 设置活动结束时间、剩余时间、拼团价格等信息到活动商品详情中
                activityGoodsDetail.setActivityEndTime(pintuanGoods.getEndTime());
                activityGoodsDetail.setActivityStartTime(pintuanGoods.getStartTime());
                activityGoodsDetail.setOverplusTime(overplusTime);
                activityGoodsDetail.setPintuanPrice(pintuanGoods.getPrice());
                activityGoodsDetail.setPriceOne(pintuanGoods.getPriceOne());
                activityGoodsDetail.setActivityPrice(pintuanGoods.getPrice());
                activityGoodsDetail.setActivityId(pintuanGoods.getPintuanId());
                activityGoodsDetail.setActivityStartStatus(pintuanGoods.getStatus());
            } else {
                // 如果活动已结束，设置commonIsXian为0表示不参与拼团
                commonIsXian = 0;
            }
            // 更新拼团商品的参与状态标识
            pintuanGoods.setCommonIsXian(commonIsXian);
        } else {
            // 如果没有对应的拼团商品信息，设置commonIsXian为0表示不参与拼团
            commonIsXian = 0;
        }

        return commonIsXian;
    }

    /**
     * 处理满减赠者活动
     *
     * @param commonId            活动标识ID
     * @param goodsMaxSale        商品最大销售量
     * @param commonIsXian        是否参与满减赠者的标识，1表示参与，0表示不参与
     * @param goodsId             商品ID
     * @param activityGoodsDetail 商品活动详情对象
     * @return 更新后的commonIsXian值
     */
    private Integer dealManjianzengzheActivity(Integer commonId, Integer goodsMaxSale, Integer commonIsXian, Integer goodsId,
                                               ActivityGoodsDetailDTO activityGoodsDetail) {
        // 通过服务客户端根据商品ID获取满减赠者商品信息
        ManjianzengzheGoodsQueryResultVO manjianzengzheGoods = activityServiceClient.selectReductionPolicyByGoodsId(commonId);
        if (manjianzengzheGoods != null && manjianzengzheGoods.getPromotionId() != null) {
            // 比较当前时间与活动结束时间，判断活动是否仍在进行
            if (DateUtils.compareDate2(manjianzengzheGoods.getEndTime())) {
                // 计算剩余时间
                Long overplusTime = DateUtils.getSecondByMinusDate(new Date(), manjianzengzheGoods.getEndTime());
                // 设置活动结束时间、剩余时间到活动商品详情中
                activityGoodsDetail.setActivityEndTime(manjianzengzheGoods.getEndTime());
                activityGoodsDetail.setOverplusTime(overplusTime);
                // 通过服务客户端根据促销ID获取满减赠者规则详情列表
                List<ManjianzengzheGoodsRuleQueryDTO> ruleList = activityServiceClient.selectReductionPolicyRuleDetailList(manjianzengzheGoods.getPromotionId());
                // 设置促销ID、规则列表等信息到活动商品详情中
                activityGoodsDetail.setPromotionId(manjianzengzheGoods.getPromotionId());
                activityGoodsDetail.setRuleList(ruleList);
                activityGoodsDetail.setMjzzPrice(manjianzengzheGoods.getGoodsPrice());
                activityGoodsDetail.setGoodsLowerLimit(0);
                activityGoodsDetail.setGoodsUpperLimit(goodsMaxSale);
                activityGoodsDetail.setActivityId(manjianzengzheGoods.getPromotionId());

            } else {
                // 如果活动已结束，设置commonIsXian为0表示不参与满减赠者
                commonIsXian = 0;
            }
        } else {
            // 如果没有对应的满减赠者商品信息，设置commonIsXian为0表示不参与满减赠者
            commonIsXian = 0;
        }

        return commonIsXian;
    }

    /**
     * 生成活动内容
     * 1. 若活动类型为满减,discountType=1:
     * 1.1 若hasGiveaway=0,无赠品:
     * 活动内容为:单笔满¥meetAmount，立减现金¥discountContent
     * 1.2 若hasGiveaway=1,有赠品:
     * 1.2.1 discountContent不为空:
     * 活动内容为:单笔满¥meetAmount，立减现金¥discountContent,送礼品giveawayName
     * 1.2.2 discountContent为空:
     * 活动内容为:单笔满¥meetAmount，送礼品giveawayName
     * 2. 若活动类型为满折,discountType=3:
     * 活动内容为:单笔订单满meetAmount件，立减折扣discountContent%
     *
     * @param r
     * @return
     */
    @Override
    public void dealPromotionContent(ManjianzengzhePromotionContentQueryResultVO r) {
        Integer discountType = r.getDiscountType();
        BigDecimal meetAmount = r.getMeetAmount();
        Integer hasGiveaway = r.getHasGiveaway();
        String discountContent = r.getDiscountContent();
        Integer giveawayId = r.getGiveawayId();
        StringBuilder promotionContent = new StringBuilder();
        //满减赠活动
        if (null != discountType && 1 == discountType) {
            //有赠品
            if (null != hasGiveaway && 1 == hasGiveaway) {
                GdsDetailsCommonQueryResultVO queryResult = baseMapper.selectByCommonId(giveawayId);
                String giveawayName = queryResult.getCommonName();
                //满减内容不为空
                //活动内容:单笔满¥meetAmount，立减现金¥discountContent,送礼品giveawayName
                if (null != discountContent && !"".equals(discountContent)) {
                    promotionContent.append("满");
                    promotionContent.append(meetAmount);
                    promotionContent.append("元减");
                    promotionContent.append(discountContent);
                    promotionContent.append("，另赠");
                    promotionContent.append(giveawayName);
                    promotionContent.append("，赠完即止");
                    //满减内容为空
                    //活动内容为:单笔满¥meetAmount，送礼品giveawayName
                } else {
                    promotionContent.append("满");
                    promotionContent.append(meetAmount);
                    promotionContent.append("元，赠");
                    promotionContent.append(giveawayName);
                    promotionContent.append("，赠完即止");
                }
                //无赠品
                //活动内容为:单笔满¥meetAmount，立减现金¥discountContent
            } else {
                promotionContent.append("满");
                promotionContent.append(meetAmount);
                promotionContent.append("元减");
                promotionContent.append(discountContent);
            }
            //满折活动
            //活动内容为:单笔订单满meetAmount件，立减折扣discountContent%
        } else if (null != discountType && 3 == discountType) {
            promotionContent.append("单笔满");
            promotionContent.append(meetAmount);
            promotionContent.append("件，打");
            promotionContent.append(discountContent);
            promotionContent.append("折");

        }
        r.setPromotionContent(promotionContent.toString());

    }

    /**
     * 查询用户可能喜欢的商品信息
     *
     * @param param 用户喜欢商品的查询参数
     * @return 包含用户喜欢的商品分页信息的响应结果
     */
    @Override
    public ResponseResult queryGuessLikeQuery(WepGuessLikeGoodsParamDTO param) {
        PageUtils page = null;
        try {
            // 根据商品ID查询分类ID和其他相关信息
            Map<String, Object> stringIntegerMap = baseMapper.selectCatIdByGoodsId(param.getGoodsId());
            param.setCatId(((Long) stringIntegerMap.get("catId")).intValue());
            Integer integralGoods = (Integer) stringIntegerMap.get("integralGoods");

            // 设置商品类型，如果存在
            if (null != stringIntegerMap.get("goodsType")) {
                param.setGoodsType(Integer.valueOf(stringIntegerMap.get("goodsType").toString()));
            }
            param.setIntegralGoods(integralGoods);

            // 将参数对象转换为Map
            Map data = ParseMapUtils.beanToMap(param);

            // 分页查询数据
            IPage<GdsListQueryResultVO> listQueryResultVOIPage = baseMapper.selectLikeGoodsByCatId(new Query<GdsListQueryResultVO>().getPage(data), param);
            page = PageUtils.getPage(listQueryResultVOIPage);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return ResponseResult.success(page);
    }

    /**
     * 查询商品详细描述信息
     *
     * @param commonsId 商品的公共ID
     * @return 包含商品详细描述信息的响应结果
     */
    @Override
    public ResponseResult queryGdsDetailDesc(Integer commonsId) {
        // 通过主键查询商品详细信息
        GdbMerchantGoodsCommonDetailVO detail = goodsCommonDetailMapper.selectByPrimaryKey(commonsId);

        // 查询商品的详细描述信息并设置品牌名称
        CommonDetailsDescVO gdsCommon = baseMapper.selectCommonDetailsDesc(commonsId);
        if (null != gdsCommon) {
            detail.setBrandName(gdsCommon.getBrandName());
        }

        // 查询商品的关联参数信息
        List<GdbAttributeAssionParamVO> assionParams = goodsCommonAttributeAssociationMapper.selectCommonIds(commonsId);
        for (GdbAttributeAssionParamVO assionParam : assionParams) {
            // 根据属性ID查询商品属性信息并设置
            GoodsAttributeVO gdbAttrPram = goodsAttributeMapper.selectAttributeId(assionParam.getAttributeId());
            if (null != gdbAttrPram) {
                assionParam.setAttributeIds(gdbAttrPram.getCommodityAttribute());
            }

            // 根据属性内容ID查询商品属性内容信息并设置
            GoodsAttributeVO gdbAttrPram1 = goodsAttributeContentMapper.selectAttContentId(assionParam.getAttributeContentId());
            if (null != gdbAttrPram1) {
                assionParam.setAttributeContentIds(gdbAttrPram1.getAttriContent());
            }
        }

        // 检查并处理商品详细信息的Body字段
        if (null == detail) {
            detail = new GdbMerchantGoodsCommonDetailVO();
        } else {
            String s = detail.getCommonBody();
            Pattern p = Pattern.compile(PATTERN);
            Matcher m = p.matcher(s);
            while (m.find()) {
                detail.setCommonBody(m.group(1));
            }
        }

        // 设置商品的关联参数信息
        detail.setAttributeParam(assionParams);
        return ResponseResult.success(detail);
    }

    /**
     * 查询推荐商品
     *
     * @param param 查询参数对象，包含推荐商品的查询条件
     * @return 包含推荐商品信息的响应结果
     */
    @Override
    public ResponseResult queryRecommendGoods(WebIndexRecommendQueryParamDTO param) {
        return ResponseResult.success(baseMapper.queryRecommendGoods(param));
    }

    /**
     * 查询指定commonId的商品起售库存信息
     *
     * @param commonId 商品的commonId
     * @return 包含商品起售库存信息的列表
     */
    @Override
    public List<GdsBaseDetailResultVO> queryGoodsStartStock(Integer commonId) {
        return baseMapper.queryGoodsStartStock(commonId);
    }

    /**
     * 更新商品库存
     *
     * @param wapGood 商品销售汇总数据列表
     * @return 表示操作结果的响应对象
     */
    @Override
    public ResponseResult updateGoodsStock(List<WapGoodsSaleSumDTO> wapGood) {
        // 遍历每个商品的销售汇总数据，进行库存更新
        for (WapGoodsSaleSumDTO wapGoodsSaleSumUpdate : wapGood) {

            // 根据commonId获取商品实体
            GoodsCommonEntity entity = baseMapper.selectById(wapGoodsSaleSumUpdate.getCommonId());

            // 获取商品数量，如果为null则默认为0
            int goodsNum = wapGoodsSaleSumUpdate.getGoodsNum() == null ? 0 : Integer.parseInt(wapGoodsSaleSumUpdate.getGoodsNum());

            // 更新商品的通用库存
            baseMapper.update(null, new LambdaUpdateWrapper<GoodsCommonEntity>()
                    .eq(GoodsCommonEntity::getCommonId, entity.getCommonId())
                    .set(GoodsCommonEntity::getCommonStock, entity.getCommonStock() + goodsNum));

            // 根据goodsId获取商品基础信息
            GoodsStockEntity baseEntity = goodsStockService.getOne(new LambdaUpdateWrapper<GoodsStockEntity>()
                    .eq(GoodsStockEntity::getGoodsId, wapGoodsSaleSumUpdate.getGoodsId())
                    .eq(GoodsStockEntity::getCommonId, wapGoodsSaleSumUpdate.getCommonId()));

            // 更新商品的基础库存
            goodsStockService.getBaseMapper().update(baseEntity, new LambdaUpdateWrapper<GoodsStockEntity>()
                    .eq(GoodsStockEntity::getGoodsId, baseEntity.getGoodsId())
                    .set(GoodsStockEntity::getGoodsStock, baseEntity.getGoodsStock() + goodsNum));

            // 查询商品是否参与秒杀活动
            Integer seckillId = goodsBaseMapper.querySeckillInfo(wapGoodsSaleSumUpdate.getGoodsId());
            if (null != seckillId && seckillId > 0) {
                // 准备更新秒杀商品销售数据的参数
                Map<String, Object> paramMap = new HashMap<>();
                Integer goodsId = wapGoodsSaleSumUpdate.getGoodsId();
                paramMap.put("seckillId", seckillId);
                paramMap.put("goodsId", goodsId);
                paramMap.put("goodsNum", goodsNum);
                // 更新秒杀商品的销售数据
                baseMapper.updateSaleForSeckill2(paramMap);
            }

            // 查询当前商品是否是供应商待发货商品
            Map<String, Object> dealerGoods = baseMapper.querydealerGoodsFlag(wapGoodsSaleSumUpdate.getGoodsId());
            if (null != dealerGoods) {
                // 是商家同步供应商的商品，也要修改供应商端的商品库存
                WapGoodsSaleSumUpdateVO supperGoods = baseMapper.querySupperGoodsInfo(dealerGoods);
                if (null != supperGoods) {
                    // 更新供应商商品库存
                    supperGoods.setGoodsNum(goodsNum);
                    goodsBaseMapper.updateAddStock(supperGoods);
                    baseMapper.updateAddStock(supperGoods);
                }
            }
        }
        // 返回成功操作结果
        return ResponseResult.success();
    }

    /**
     * 更新商品销量
     *
     * @param wapGood 商品销售汇总数据列表
     * @return 更新操作的结果
     */
    @Override
    public ResponseResult updateGoodsSalenum(List<WapGoodsSaleSumDTO> wapGood) {
        // 遍历商品销售汇总数据列表，更新每种商品的销量
        for (WapGoodsSaleSumDTO wapGoodsSaleSumUpdate : wapGood) {

            // 根据商品ID获取商品基本信息
            GoodsCommonEntity entity = baseMapper.selectById(wapGoodsSaleSumUpdate.getCommonId());

            // 获取商品的销售数量，如果为null则默认为0
            int goodsNum = wapGoodsSaleSumUpdate.getGoodsNum() == null ? 0 : Integer.parseInt(wapGoodsSaleSumUpdate.getGoodsNum());

            // 更新商品基本信息中的销量
            baseMapper.update(null, new LambdaUpdateWrapper<GoodsCommonEntity>()
                    .eq(GoodsCommonEntity::getCommonId, entity.getCommonId())
                    .set(GoodsCommonEntity::getCommonSalenum, entity.getCommonSalenum() + goodsNum));

            // 根据商品ID获取商品的基础信息
            GoodsBaseEntity baseEntity = goodsBaseMapper.selectById(wapGoodsSaleSumUpdate.getGoodsId());

            // 更新商品基础信息中的销量
            goodsBaseMapper.update(baseEntity, new LambdaUpdateWrapper<GoodsBaseEntity>()
                    .eq(GoodsBaseEntity::getGoodsId, baseEntity.getGoodsId())
                    .set(GoodsBaseEntity::getGoodsSalenum, baseEntity.getGoodsSalenum() + goodsNum));
        }
        // 返回成功结果
        return ResponseResult.success();
    }

    /**
     * 根据商品ID列表查询运输区域ID列表
     *
     * @param goodsIds 商品ID列表
     * @return 运输区域ID列表
     */
    @Override
    public List<Integer> selectTransportAreaId(List<Integer> goodsIds) {
        // 调用数据库查询方法，返回运输区域ID列表
        return baseMapper.selectTransportAreaId(goodsIds);
    }

    /**
     * 查询当前商品的 国外售卖区域规则
     *
     * @param goodsIds
     * @return
     */
    @Override
    public List<Integer> selectInternationalTransportAreaId(List<Integer> goodsIds) {
        // 调用数据库查询方法，返回运输区域ID列表
        return baseMapper.selectInternationalTransportAreaId(goodsIds);
    }

    /**
     * 更新商品收藏信息
     *
     * @param paraMap 参数映射，包含需要更新的商品收藏信息
     * @return 更新操作的结果
     */
    @Override
    public ResponseResult updateGoodsCollect(Map<String, String> paraMap) {
        // 更新商品基础信息中的收藏状态
        goodsBaseMapper.updateGoodsCollect(paraMap);
        // 更新商品基本信息中的收藏状态
        baseMapper.updateGoodsCollect(paraMap);
        // 返回成功结果
        return ResponseResult.success();
    }

    /**
     * 根据商品ID查询积分商品信息
     *
     * @param param 查询参数，包含需要查询的积分商品信息
     * @return 查询操作的结果，包含积分商品信息
     */
    @Override
    public ResponseResult queryIntegralGoodsBygoodsId(Map<String, Object> param) {
        // 调用数据库查询方法，根据商品ID查询积分商品信息
        Map<String, Object> map = baseMapper.queryIntegralGoodsBygoodsId(param);
        Object pointsRate = map.get("pointsRate");
        if (null != pointsRate) {
            Double rate = (Double) pointsRate;
            ResponseResult<PointsCashRatioVo> pointsCashRatio = baseServiceClient.getPointsCashRatio();
            if (pointsCashRatio.getCode() == 1) {
                BigDecimal goodsPrice = (BigDecimal) map.get("goods_price");
                PointsCashRatioVo ratioVo = pointsCashRatio.getData();
                PointsRatioUtil.PointsCashRatioRes pointsRatio = PointsRatioUtil.getPointsRatio(goodsPrice, rate);
                map.put("cashPrice", pointsRatio.getCashPrice());
            }
        }
        // 返回成功结果，包含积分商品信息
        return ResponseResult.success(map);
    }

    /**
     * 根据通用ID查询商品是否为加价商品
     *
     * @param commonId 商品的通用ID
     * @return 查询结果，1表示是加价商品，0表示不是
     */
    @Override
    public Integer queryGoodsIsPlus(Integer commonId) {
        // 调用数据库查询方法，根据通用ID查询商品是否为加价商品
        return baseMapper.queryGoodsIsPlus(commonId);
    }

    /**
     * 查询虚拟商品详情
     *
     * @param param 包含商品ID的参数映射
     * @return 包含虚拟商品详情的响应结果
     */
    @Override
    public ResponseResult queryVirtualgoods(Map<String, String> param) {
        // 通过商品ID查询接收商品的详细信息
        Map<String, Object> receiveGoodsDetails = baseMapper.queryVirtualgoods(param.get("goodsId"));
        // 返回查询到的商品详情
        return ResponseResult.success(receiveGoodsDetails);
    }

    /**
     * 更新商品的销售量和库存
     *
     * @param goodsDetails 包含商品销售汇总更新信息的字符串
     * @return 包含更新结果的响应结果
     */
    @Override
    public ResponseResult updateSaleAndStock(String goodsDetails) {
        // 解析字符串中的商品销售汇总更新信息列表
        List<WapGoodsSaleSumUpdateVO> result = JSON.parseArray(goodsDetails, WapGoodsSaleSumUpdateVO.class);
        for (WapGoodsSaleSumUpdateVO wapGoodsSaleSumUpdate : result) {
            // 根据goodsId查询具体商品信息
            GoodsStockEntity goodsStockEntity = goodsStockService.getOne(new LambdaUpdateWrapper<GoodsStockEntity>()
                    .eq(GoodsStockEntity::getGoodsId, wapGoodsSaleSumUpdate.getGoodsId()));
            // 根据commonId查询通用商品信息
            GoodsCommonEntity entity = baseMapper.selectById(goodsStockEntity.getCommonId());
            // 获取商品数量，如果为null则默认为0
            int num = wapGoodsSaleSumUpdate.getGoodsNum() == null ? 0 : wapGoodsSaleSumUpdate.getGoodsNum();
            // 获取通用库存，如果小于等于0则默认为0
            Integer commonStock = entity.getCommonStock() > 0 ? entity.getCommonStock() : 0;
            // 获取商品库存，如果小于等于0则默认为0
            Integer goodsStock = goodsStockEntity.getGoodsStock() > 0 ? goodsStockEntity.getGoodsStock() : 0;
            // 二开库存预占（before*）：库存扣减前回调 StockLockExtension，只读、可中断（限购 / 外部可用量校验）。
            // ctx.abort → 抛 PluginAbortException(7001)，由全局异常处理器按业务错误码回传，事务回滚。
            {
                Map<String, Object> stockParams = new HashMap<>();
                stockParams.put("commonId", goodsStockEntity.getCommonId());
                stockLockGateway.lock(
                        wapGoodsSaleSumUpdate.getGoodsId() == null ? null : wapGoodsSaleSumUpdate.getGoodsId().longValue(),
                        num,
                        com.houjiang.goods.plugin.StockLockGateway.TYPE_LOCK,
                        goodsStockEntity.getGoodsStock(),
                        null,
                        stockParams);
            }
            // 如果商品未被询价或商品有库存，则可以进行库存更新
            if (goodsStockEntity.getGoodsStock() > 0) {
                // 更新通用库存，如果更新后的库存大于等于0
                if ((entity.getCommonStock() - num) >= 0) {
                    commonStock = entity.getCommonStock() - num;
                }
                // 更新商品库存，如果更新后的库存大于等于0
                if ((goodsStockEntity.getGoodsStock() - num) >= 0) {
                    goodsStock = goodsStockEntity.getGoodsStock() - num;
                }
            }
            // 更新通用商品的库存信息
            baseMapper.update(null, new LambdaUpdateWrapper<GoodsCommonEntity>()
                    .eq(GoodsCommonEntity::getCommonId, entity.getCommonId())
                    .set(GoodsCommonEntity::getCommonStock, commonStock));
            // 更新商品库存表中的库存信息
            goodsStockEntity.setGoodsStock(goodsStock);
            goodsStockEntity.setUpdateTime(new Date());
            goodsStockService.updateById(goodsStockEntity);
            // 二开库存变动（after*）：扣减落库后回调 StockChangeExtension，用于同步外部仓储 / 通知（不回写、不中断）。
            stockChangeGateway.onChange(
                    wapGoodsSaleSumUpdate.getGoodsId() == null ? null : wapGoodsSaleSumUpdate.getGoodsId().longValue(),
                    -num,
                    com.houjiang.goods.plugin.StockChangeGateway.TYPE_OUT,
                    goodsStockEntity);
            // 若为限时秒杀，需要更新秒杀库存
            Integer seckillId = goodsBaseMapper.querySeckillInfo(wapGoodsSaleSumUpdate.getGoodsId());
            if (null != seckillId && seckillId > 0) {
                Map<String, Object> paramMap = new HashMap<>();
                Integer goodsId = wapGoodsSaleSumUpdate.getGoodsId();
                Integer goodsNum = wapGoodsSaleSumUpdate.getGoodsNum();
                paramMap.put("seckillId", seckillId);
                paramMap.put("goodsId", goodsId);
                paramMap.put("goodsNum", goodsNum);
                Integer seckillNum = activityServiceClient.querySeckillNum(paramMap);
                if (seckillNum - goodsNum >= 0) {
                    activityServiceClient.updateSaleForSeckill(paramMap);
                } else {
                    // 如果秒杀商品库存不足，设置当前事务为回滚
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return ResponseResult.fail("秒杀商品库存不足");
                }
            }
            // 查询当前商品是否是供应商待发货商品，如果是则同时更新商家和供应商的库存
            Map<String, Object> dealerGoods = baseMapper.querydealerGoodsFlag(wapGoodsSaleSumUpdate.getGoodsId());
            if (null != dealerGoods) {
                WapGoodsSaleSumUpdateVO supperGoods = baseMapper.querySupperGoodsInfo(dealerGoods);
                if (null != supperGoods) {
                    supperGoods.setGoodsNum(wapGoodsSaleSumUpdate.getGoodsNum());
                    goodsBaseMapper.updateStock(supperGoods);
                    baseMapper.updateStock(supperGoods);
                }
            }
            // 异步处理操作，使用单线程执行器
            Map<String, Object> map = new HashMap<>();
            List<Integer> list = new ArrayList<>();
            list.add(goodsStockEntity.getCommonId());
            map.put("commonIdS", list);
            map.put("type", 1);
            executorService.submit(() -> {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                System.err.println("当前时间：" + dateFormat.format(new Date()));
                MqSendUtils.mqSendGoods(map);
                System.err.println("当前时间：" + dateFormat.format(new Date()));
            });
        }
        // 返回更新库存成功的响应结果
        return ResponseResult.success(1, "更新库存", null);
    }

    /**
     * 更新产品优惠券商品库存
     * 该方法用于在数据库中更新商品的库存和销售数量，当商品参与优惠活动时调用
     * 它从参数中解析出商品的库存变化量，并在数据库中应用这些变化
     *
     * @param param 包含更新信息的参数映射，其中包含commonId, goodsId和goodsNum
     * @return ResponseResult 表示操作成功的响应结果
     */
    @Override
    public ResponseResult updateProductVoucherGoodsStock(Map<String, Object> param) {
        // 从参数映射中提取商品的通用ID和库存变化量，并转换为Integer类型
        Integer commonId = Integer.valueOf(param.get("commonId").toString());
        Integer goodsId = Integer.valueOf(param.get("goodsId").toString());
        Integer goodsNum = Integer.valueOf(param.get("goodsNum").toString());

        // 根据通用ID查询商品基本信息
        GoodsCommonEntity entity = baseMapper.selectById(commonId);

        // 更新商品的总库存和销售数量
        int commonStock = Math.max(entity.getCommonStock() - goodsNum, 0);
        int commonSalenum = entity.getCommonSalenum() == null ? goodsNum : entity.getCommonSalenum() + goodsNum;
        baseMapper.update(null, new LambdaUpdateWrapper<GoodsCommonEntity>()
                .eq(GoodsCommonEntity::getCommonId, entity.getCommonId())
                .set(GoodsCommonEntity::getCommonStock, commonStock)
                .set(GoodsCommonEntity::getCommonSalenum, commonSalenum));

        // 根据商品ID查询商品详细信息
        GoodsBaseEntity baseEntity = goodsBaseMapper.selectOne(new LambdaUpdateWrapper<GoodsBaseEntity>()
                .eq(GoodsBaseEntity::getGoodsId, goodsId));
        // 根据goodsId查询具体商品信息
        GoodsStockEntity stockEntity = goodsStockService.getOne(new LambdaUpdateWrapper<GoodsStockEntity>()
                .eq(GoodsStockEntity::getGoodsId, goodsId));
        // 更新商品的实际库存和销售数量
        int goodsStock = Math.max(stockEntity.getGoodsStock() - goodsNum, 0);
        int goodsalenum = baseEntity.getGoodsSalenum() == null ? goodsNum : baseEntity.getGoodsSalenum() + goodsNum;
        goodsBaseMapper.update(baseEntity, new LambdaUpdateWrapper<GoodsBaseEntity>()
                .eq(GoodsBaseEntity::getGoodsId, goodsId)
                .set(GoodsBaseEntity::getGoodsSalenum, goodsalenum));
        goodsStockService.getBaseMapper().update(stockEntity, new LambdaUpdateWrapper<GoodsStockEntity>()
                .eq(GoodsStockEntity::getGoodsId, goodsId)
                .set(GoodsStockEntity::getGoodsStock, goodsStock));

        // 返回操作成功的响应结果
        return ResponseResult.success();
    }

    /**
     * 查询商品规格信息
     *
     * @param param 包含商品ID和规格值的参数对象
     * @return 包含商品规格信息的响应结果
     */
    @Override
    public ResponseResult queryGoodsSpec(GoodsDetailsDTO param) {
        // 将商品通用ID转换为Integer类型
        Integer commonId = Integer.valueOf(param.getCommonId());
        // 获取规格值列表
        List<Map> specValues = param.getSpecValues();

        // 初始化用户ID变量
        String userId = null;
        // 根据Token获取用户信息
        UserInfoVO userinfo = ParseTokenUtils.getUserInfoByTokenNoIsExist(param.getToken());
        if (null != userinfo) {
            // 如果用户信息不为空，提取用户ID
            userId = userinfo.getUserId();
        }
        // 获取会话时间
        Date sessionTime = param.getSessionTime();

        // 根据通用ID查询商品基础信息列表
        List<ManageGdsBaseAddResultVO> gdsBases = goodsBaseMapper.selectByCommonId(commonId);
        if (null == gdsBases || gdsBases.isEmpty()) {
            // 如果没有找到对应的商品，返回失败的响应结果
            return ResponseResult.fail(0, "没有此商品");
        }
        int goodsId = 0;
        //遍历当前商品的所有规格
        for (ManageGdsBaseAddResultVO record : gdsBases) {
            if (StringUtils.isNotEmpty(record.getSpecValue())) {
                int flag = 0;
                JSONArray array = JSONObject.parseArray(record.getSpecValue());
                List<GoodBaseSpecValuesJsonBeanVO> tempSpecValues = new ArrayList<>();
                //解析当前商品规格 的规格名和规格值
                for (int index = 0; index < array.size(); index++) {
                    String jsonObject = array.getString(index);
                    GoodBaseSpecValuesJsonBeanVO temp = JSONObject.parseObject(jsonObject, GoodBaseSpecValuesJsonBeanVO.class);
                    tempSpecValues.add(temp);
                }
                //遍历校验用户选中的规格 与 库中查出的哪个 规格名、值匹配，返回对应的规格价格
                for (int i = 0; i < specValues.size(); i++) {
                    Map mapSpecValues = specValues.get(i);
                    String recordTempString = JSONObject.toJSONString(mapSpecValues);
                    GoodBaseSpecValuesJsonBeanVO oldSpecValues = JSONObject.parseObject(recordTempString, GoodBaseSpecValuesJsonBeanVO.class);
                    for (GoodBaseSpecValuesJsonBeanVO newSpecValues : tempSpecValues) {
                        if (oldSpecValues.getParamName().trim().equals(newSpecValues.getParamName().trim()) &&
                                oldSpecValues.getParamValue().trim().equals(newSpecValues.getParamValue().trim())) {
                            flag++;
                        }
                    }

                }
                if (specValues.size() == flag) {
                    goodsId = record.getGoodsId();
                }
            }
        }
        GdsBaseDetailResultVO goodsBases = goodsBaseMapper.selectByGoodsId(goodsId);
        RedisUtil.set(HouConfig.GOODS_STOCK + goodsBases.getGoodsId(), String.valueOf(goodsBases.getGoodsStock()));
        goodsBases.setCommodityPrice(goodsBases.getGoodsPrice());

        //查询图片
        List<GdbMerchantGoodsImagesVO> goodsImages = goodsImagesMapper.selectByCommonIdImg(goodsId);
        goodsBases.setGoodsImages(goodsImages);

        // 查询门店商品库存
        if (null != param.getShopGateId()) {
            Integer shopGateGoodsStock = baseMapper.queryGoodsGateStock(goodsBases.getGoodsId(), param.getShopGateId().toString());
            if (shopGateGoodsStock != null) {
                goodsBases.setGoodsStock(shopGateGoodsStock);
            } else {
                goodsBases.setGoodsStock(0);
            }
        }
        // 活动
        ActivityGoodsDetailDTO activityGoodsDetail = dealActivity2(goodsBases.getGoodsId(), goodsBases.getGoodsStock(),
                goodsBases.getGoodsMaxSale(), commonId, goodsBases.getCommonIsXian(), userId, sessionTime);

        if (null != activityGoodsDetail && !activityGoodsDetail.getActivityStatus().equals(0)) {
            goodsBases.setActivityStatus(activityGoodsDetail.getActivityStatus());
            goodsBases.setActivityGoodsDetailDTO(activityGoodsDetail);
            goodsBases.setCurrentCartQuantity(activityGoodsDetail.getCurrentCartQuantity());
            if (!activityGoodsDetail.getActivityStatus().equals(4)) {
                goodsBases.setActivityPrice(activityGoodsDetail.getActivityPrice());
                goodsBases.setGoodsMaxSale(activityGoodsDetail.getGoodsUpperLimit());
                goodsBases.setGoodsMinSale(activityGoodsDetail.getGoodsLowerLimit());
            }
            //满减赠折活动展示活动内容ZZZ
            if (activityGoodsDetail.getActivityStatus().equals(5)) {
                String promotionContent = "";
                List<ManjianzengzhePromotionContentQueryResultVO> promotionContents = activityServiceClient.selectReductionPolicyPromotionContent(commonId);
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
                        goodsBases.setCommonIsXian(6);
                        goodsBases.setActivityStatus(6);
                        goodsBases.setPromotionType("满折扣");
                    }
                    if ("满减".equals(goodsBases.getPromotionType()) && hasGiveaway == 1) {
                        goodsBases.setPromotionType("满减赠");
                    } else if ("满赠".equals(goodsBases.getPromotionType()) && !mzFlag) {
                        goodsBases.setPromotionType("满减赠");
                    }
                    dealPromotionContent(r);

                    promotionContent = promotionContent + r.getPromotionContent() + "\n";

                }
                goodsBases.setPromotionContent(promotionContent);


            }
        }
        if (StringUtils.isNotEmpty(goodsBases.getSpecValue())) {
            List<GoodBaseSpecValuesJsonBeanVO> specValuesJsonBeans = new ArrayList<>();
            JSONArray specValuesJsonBeanArray = com.alibaba.fastjson.JSONObject.parseArray(goodsBases.getSpecValue());
            for (int index = 0; index < specValuesJsonBeanArray.size(); index++) {
                com.alibaba.fastjson.JSONObject obj = specValuesJsonBeanArray.getJSONObject(index);
                GoodBaseSpecValuesJsonBeanVO temp = JSONObject.parseObject(obj.toJSONString(), GoodBaseSpecValuesJsonBeanVO.class);
                specValuesJsonBeans.add(temp);
            }
            goodsBases.setSpecValueList(specValuesJsonBeans);
        }
        return ResponseResult.success(goodsBases);
    }

    @Override
    public ResponseResult productVoucherQueryGoodsSpec(GoodsDetailsDTO param) {
        // 商品通用ID
        Integer commonId = Integer.valueOf(param.getCommonId());
        // 商品规格值列表
        List<Map> specValues = param.getSpecValues();
        // 根据通用ID查询商品基础信息
        List<ManageGdsBaseAddResultVO> gdsBases = goodsBaseMapper.selectByCommonId(commonId);
        if (null == gdsBases || gdsBases.isEmpty()) {
            return ResponseResult.fail(0, "没有此商品");
        }
        // 商品ID
        int goodsId = 0;
        // 遍历商品的所有规格
        for (ManageGdsBaseAddResultVO record : gdsBases) {
            if (StringUtils.isNotEmpty(record.getSpecValue())) {
                int flag = 0;
                JSONArray array = JSONObject.parseArray(record.getSpecValue());
                List<GoodBaseSpecValuesJsonBeanVO> tempSpecValues = new ArrayList<>();
                // 解析商品规格的规格名和规格值
                for (int index = 0; index < array.size(); index++) {
                    String jsonObject = array.getString(index);
                    GoodBaseSpecValuesJsonBeanVO temp = JSONObject.parseObject(jsonObject, GoodBaseSpecValuesJsonBeanVO.class);
                    tempSpecValues.add(temp);
                }
                // 校验用户选中的规格与数据库中的规格是否匹配，返回对应的规格价格
                for (int i = 0; i < specValues.size(); i++) {
                    Map mapSpecValues = specValues.get(i);
                    String recordTempString = JSONObject.toJSONString(mapSpecValues);
                    GoodBaseSpecValuesJsonBeanVO oldSpecValues = JSONObject.parseObject(recordTempString, GoodBaseSpecValuesJsonBeanVO.class);
                    for (GoodBaseSpecValuesJsonBeanVO newSpecValues : tempSpecValues) {
                        if (oldSpecValues.getParamName().trim().equals(newSpecValues.getParamName().trim()) &&
                                oldSpecValues.getParamValue().trim().equals(newSpecValues.getParamValue().trim())) {
                            flag++;
                        }
                    }
                }
                // 如果用户选择的规格与数据库中的规格完全匹配
                if (specValues.size() == flag) {
                    goodsId = record.getGoodsId();
                }
            }
        }
        // 根据商品ID查询商品详细信息
        GdsBaseDetailResultVO goodsBases = goodsBaseMapper.selectByGoodsId(goodsId);
        // 设置商品价格
        goodsBases.setCommodityPrice(goodsBases.getGoodsPrice());
        // 查询商品图片
        List<GdbMerchantGoodsImagesVO> goodsImages = goodsImagesMapper.selectByCommonIdImg(goodsId);
        goodsBases.setGoodsImages(goodsImages);
        // 处理商品规格信息
        if (StringUtils.isNotEmpty(goodsBases.getSpecValue())) {
            List<GoodBaseSpecValuesJsonBeanVO> specValuesJsonBeans = new ArrayList<>();
            JSONArray specValuesJsonBeanArray = com.alibaba.fastjson.JSONObject.parseArray(goodsBases.getSpecValue());
            for (int index = 0; index < specValuesJsonBeanArray.size(); index++) {
                com.alibaba.fastjson.JSONObject obj = specValuesJsonBeanArray.getJSONObject(index);
                GoodBaseSpecValuesJsonBeanVO temp = JSONObject.parseObject(obj.toJSONString(), GoodBaseSpecValuesJsonBeanVO.class);
                specValuesJsonBeans.add(temp);
            }
            goodsBases.setSpecValueList(specValuesJsonBeans);
        }
        // 返回商品详细信息
        return ResponseResult.success(goodsBases);
    }

    /**
     * 根据参数查询积分商品列表
     *
     * @param param 查询参数对象，包含分页和过滤条件
     * @return 返回积分商品列表的响应结果
     */
    @Override
    public ResponseResult queryIntegralGoodsListFilter(ListPageQueryParamDTO param) {
        // 初始化商品分类和菜单变量
        GoodsCatSearchChildVO goodsCatSearchChild = null;
        List<WebCatMenuVO> allMenu = null;
        List<Integer> catIds = null;

        // 判断分类ID是否为空，如果不为空则尝试从缓存或数据库获取分类信息
        if (null != param.getCatId()) {
            // 尝试从缓存中获取分类信息
            String cacheCatIds = RedisUtil
                    .get(Constants.CACHE_GOODS_WEB_GOODS_LIST_HEADER_PREFIX + "_" + param.getCatId());
            if (null != cacheCatIds && !cacheCatIds.isEmpty()) {
                goodsCatSearchChild = JsonUtils.string2Obj(cacheCatIds, GoodsCatSearchChildVO.class);
                catIds = goodsCatSearchChild.getCatIds();
            } else {
                // 如果缓存中没有，则查询所有分类并构建分类列表
                allMenu = goodsCatMapper.selectAllCat();
                catIds = getMenuList(allMenu, param.getCatId());
                goodsCatSearchChild = new GoodsCatSearchChildVO();
                goodsCatSearchChild.setAllMenu(allMenu);
                goodsCatSearchChild.setCatIds(catIds);
                // 将分类信息存入缓存
                RedisUtil.set(Constants.CACHE_GOODS_WEB_GOODS_LIST_HEADER_PREFIX + "_" + param.getCatId(),
                        JsonUtils.toString(goodsCatSearchChild), RedisUtil.EXRP_MONTH);
            }

            // 更新查询参数，包括分类ID列表
            if (!CollectionUtils.isEmpty(catIds)) {
                catIds.add(param.getCatId());
                param.setCatIds(catIds);
                param.setCatId(0);
            }
        }

        // 查询商品列表
        IPage<GdsListQueryResultVO> page = null;
        try {
            // 将查询参数对象转换为Map，以便分页查询
            Map data = ParseMapUtils.beanToMap(param);
            page = baseMapper.selectByWebFilter(new Query<GdsListQueryResultVO>().getPage(data), param);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        // 添加热搜词
        getHotSearchWords(param);

        // 返回分页后的商品列表
        return ResponseResult.success(PageUtils.getPage(page));
    }

    @Override
    public ResponseResult queryShopIndex(ShopIndexDTO param) {
        // 根据shopId查询该店铺的所有商品信息
        List<ListQueryResultVO> gdsList = baseMapper.selectByShopIndex(param);
        // 遍历商品列表，处理每个商品的促销状态和价格信息
        for (ListQueryResultVO gdsListQueryResult : gdsList) {
            // 将商品ID字符串转换为整数类型
            gdsListQueryResult.setGoodsIdOne(Integer.parseInt(gdsListQueryResult.getGoodsId()));

            // 获取商品的促销状态
            Integer commonIsXian = gdsListQueryResult.getCommonIsXian();
            // 根据商品ID、通用ID和促销状态查询商品的促销详情
            ActivityGoodsDetailDTO activityGoodsDetail = dealActivity(gdsListQueryResult.getGoodsIdOne(), gdsListQueryResult.getCommonId(), commonIsXian);

            // 如果商品有参与促销活动，则设置相应的促销状态和详情信息
            if (null != activityGoodsDetail && !activityGoodsDetail.getActivityStatus().equals(CommonType.promotionStatus.NOT_PARTICIPATING.getCode())) {
                gdsListQueryResult.setActivityStatus(activityGoodsDetail.getActivityStatus());
                gdsListQueryResult.setActivityGoodsDetail(activityGoodsDetail);
                gdsListQueryResult.setCommonIsXian(activityGoodsDetail.getActivityStatus());
                gdsListQueryResult.setCommonPrice(activityGoodsDetail.getActivityPrice());
                // 如果促销类型为满减免，则设置特殊的促销价格
                if (activityGoodsDetail.getActivityStatus().equals(CommonType.promotionStatus.FULL_REDUCTION_AND_FREE_DISCOUNT.getCode())) {
                    gdsListQueryResult.setCommonPrice(activityGoodsDetail.getMjzzPrice());
                }
            } else {
                // 如果商品未参与任何促销活动，则设置默认的促销状态和详情信息
                gdsListQueryResult.setActivityStatus(CommonType.promotionStatus.NOT_PARTICIPATING.getCode());
                gdsListQueryResult.setActivityGoodsDetail(new ActivityGoodsDetailDTO());
                gdsListQueryResult.setCommonIsXian(CommonType.promotionStatus.NOT_PARTICIPATING.getCode());
            }

        }
        // 调用服务客户端查询店铺首页信息
        ResponseResult shopResult = merchantServiceClient.shopIndex(param.getShopId());

        // 如果店铺首页信息为空，则返回失败结果
        if (shopResult.getData() == null) {
            return ResponseResult.fail(BizCodeEnume.NO_STORE_INFORMATION.getMsg());
        }
        // 将店铺首页信息转换为指定的对象类型
        ShopIndexVO shopIndex = JSON.parseObject(JSON.toJSONString(shopResult.getData()), ShopIndexVO.class);

        // 设置店铺收藏状态为已收藏
        shopIndex.setIsEnshrine(CommonType.commonStatus.YES.getCode());
        // 查询该店铺的商品总数
        Integer shopSumNum = Math.toIntExact(baseMapper.selectCount(new LambdaQueryWrapper<GoodsCommonEntity>()
                .eq(GoodsCommonEntity::getShopId, param.getShopId())
                .eq(GoodsCommonEntity::getShopStatus, CommonType.shopStatus.STORE_OPEN_SUCCESS.getCode())
                .eq(GoodsCommonEntity::getCommonVerify, CommonType.productReview.PASS.getCode())
                .eq(GoodsCommonEntity::getCommonState, CommonType.commonSpuState.GOODS_NORMAL.getCode())));

        // 处理店铺轮播图信息
        dealShopSlide(shopIndex);

        // 如果店铺的图片信息为空，则使用默认图片进行替换
        if (shopIndex.getShopImage() == null || "".equals(shopIndex.getShopImage()) || shopIndex.getShopLogo() == null || "".equals(shopIndex.getShopLogo()) || shopIndex.getWapShopLogo() == null || "".equals(shopIndex.getWapShopLogo())) {
            String configValue = baseServiceClient.selectConfigValueFeign(CommonType.configKey.SHOP_LOGO_WRITING.getDesc());

            if (shopIndex.getShopImage() == null || "".equals(shopIndex.getShopImage())) {
                shopIndex.setShopImage(configValue);
            }
            if (shopIndex.getShopLogo() == null || "".equals(shopIndex.getShopLogo())) {
                shopIndex.setShopLogo(configValue);
            }
            if ((shopIndex.getWapShopLogo() == null || "".equals(shopIndex.getWapShopLogo())) && shopIndex.getShopLogo() != null) {
                shopIndex.setWapShopLogo(configValue);
            }
            if (shopIndex.getWapShopLogo() == null || "".equals(shopIndex.getWapShopLogo())) {
                shopIndex.setWapShopLogo(configValue);
            }
            if (shopIndex.getShopSlide() == null || "".equals(shopIndex.getShopSlide())) {
                shopIndex.setShopSlide(configValue);
            }
        }
        // 设置店铺商品总数和商品列表信息
        shopIndex.setShopSumNum(shopSumNum);
        shopIndex.setGdsList(gdsList);
        // 返回查询到的店铺首页信息
        return ResponseResult.success(shopIndex);
    }


    /**
     * 根据商品通用ID获取商品详情
     *
     * @param commonId 商品通用ID
     * @return 返回商品详情的响应结果
     */
    @Override
    public ResponseResult getDetailByCommonId(Integer commonId) {
        GoodsCommonEntity entity = baseMapper.selectById(commonId);
        return ResponseResult.success(entity);
    }

    /**
     * 更新商品的收藏次数
     *
     * @param commonId 商品通用ID
     * @return 返回更新收藏次数后的响应结果
     */
    @Override
    public ResponseResult updateCommonCollect(String commonId) {
        GoodsCommonEntity commonEntity = baseMapper.selectById(commonId);
        if (commonEntity.getCommonCollect() > 0) {
            baseMapper.update(commonEntity, new LambdaUpdateWrapper<GoodsCommonEntity>().eq(GoodsCommonEntity::getCommonId, commonId)
                    .set(GoodsCommonEntity::getCommonId, commonEntity.getCommonCollect() - 1));
        }
        return ResponseResult.success();
    }

    /**
     * 更新库存减少信息
     *
     * @param param 包含销售总结DTO参数
     * @return 返回更新后的响应结果
     */
    @Override
    public ResponseResult updateStockReduce(WapGoodsSaleSumDTO param) {
        GoodsCommonEntity goodsCommonEntity = baseMapper.selectById(param.getCommonId());
        baseMapper.update(null, new LambdaUpdateWrapper<GoodsCommonEntity>()
                .set(GoodsCommonEntity::getCommonStock, goodsCommonEntity.getCommonStock() + param.getGoodsNum())
                .eq(GoodsCommonEntity::getCommonId, goodsCommonEntity.getCommonId()));

        if (param.getGoodsType().intValue() == 1) {
            UserInfoVO userinfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
            Map<String, Object> map = new HashMap<>();
            map.put("userId", userinfo.getUserId());
            map.put("shopId", param.getTreasureOrderShopId());
            map.put("numberOfGifts", -1);

            userServiceClient.updateTreasureByMap(map);
        }
        return ResponseResult.success();
    }

    /**
     * 更新商品评论数量
     * <p>
     * 通过调用此方法，根据提供的商品ID（commonId）来更新数据库中对应商品的评论数量（commonEvaluate）
     *
     * @param map 包含商品ID信息的映射表，用于识别需要更新评论数量的商品
     * @return ResponseResult 表示操作结果，成功或失败
     */
    @Override
    public ResponseResult updateCommentCount(Map<String, Object> map) {
        // 根据商品ID查询商品信息
        GoodsCommonEntity entity = baseMapper.selectById(Integer.valueOf(map.get("commonId").toString()));
        // 检查商品是否存在
        if (null != entity) {
            // 更新商品的评论数量，将其增加1
            Integer count = baseMapper.update(entity, new LambdaUpdateWrapper<GoodsCommonEntity>()
                    .eq(GoodsCommonEntity::getCommonId, entity.getCommonId())
                    .set(GoodsCommonEntity::getCommonEvaluate, entity.getCommonEvaluate() + 1));
            // 检查更新操作是否成功
            if (count > 0) {
                // 更新成功，返回成功结果
                return ResponseResult.success();
            }
        }

        // 更新失败，返回失败结果
        return ResponseResult.fail(BizCodeEnume.UPDATE_ERROR.getCode(), BizCodeEnume.UPDATE_ERROR.getMsg());
    }


    private void dealShopSlide(ShopIndexVO shopBase) {
        // 处理轮播信息
        String[] shopSlideurlArr = null;
        String shopSlide = shopBase.getMobileShopSlide();
        String mobileShopSlideurl = shopBase.getMobileShopSlideurl();

        if (null == shopSlide || "".equals(shopSlide)) {
            shopSlide = shopBase.getShopSlide();
            mobileShopSlideurl = shopBase.getShopSlideurl();
        }
        if (null != shopSlide && !"".equals(shopSlide)) {
            if (mobileShopSlideurl != null && mobileShopSlideurl.length() > 0) {
                shopSlideurlArr = mobileShopSlideurl.split(",", -1);
            }

            String[] shopSlideArr = shopSlide.split(",", -1);
            List<ShopSlideUrlVO> shopSlideurlList = Lists.newArrayList();
            Integer length = 0;
            if (null != shopSlideurlArr && shopSlideurlArr.length > 0) {
                for (String shopSlideurl : shopSlideurlArr) {
                    ShopSlideUrlVO slideUrlList = new ShopSlideUrlVO();
                    if (!"https:\\".equals(shopSlideurl) && !"http://".equals(shopSlideurl)) {

                        if (shopSlideurl.contains("goodsId") && !shopSlideurl.contains("type=goods")) {
                            String[] gidStrings = shopSlideurl.split("goodsId=");
                            int goodsId = Integer.parseInt(gidStrings[gidStrings.length - 1]);
                            CommonAndGoodsVO commonIdAndGoodsId = new CommonAndGoodsVO();
                            GoodsBaseEntity baseEntity = goodsBaseMapper.selectById(goodsId);
                            Integer commonId = baseEntity.getCommonId();
                            commonIdAndGoodsId.setGoodsId(goodsId);
                            commonIdAndGoodsId.setCommonId(commonId);
                            // 跳转商品
                            slideUrlList.setOpType(2);
                            slideUrlList.setCommonIdAndGoodsId(commonIdAndGoodsId);
                        } else if (shopSlideurl.contains("shopId")) {
                            String[] gidStrings = shopSlideurl.split("shopId=");
                            int shopId = Integer.parseInt(gidStrings[gidStrings.length - 1]);
                            // 跳转店铺
                            slideUrlList.setOpType(4);
                            slideUrlList.setShopId(shopId);
                        } else if (shopSlideurl.contains("catId")) {
                            String[] gidStrings = shopSlideurl.split("catId=");
                            int catId = Integer.parseInt(gidStrings[gidStrings.length - 1]);
                            // 跳转店铺
                            slideUrlList.setOpType(6);
                            slideUrlList.setCatId(catId);
                        } else if (shopSlideurl.contains("images.oss")) {
                            // 跳转url
                            slideUrlList.setOpType(3);
                            slideUrlList.setUrl(shopSlideurl);
                        } else {
                            String[] gidStrings = shopSlideurl.split("gid=");
                            if (shopSlideurl.contains("gid") && !shopSlideurl.contains("type") && null != gidStrings) {

                                int goodsId = Integer.parseInt(gidStrings[gidStrings.length - 1]);
                                CommonAndGoodsVO commonIdAndGoodsId = new CommonAndGoodsVO();
                                GoodsBaseEntity baseEntity = goodsBaseMapper.selectById(goodsId);
                                Integer commonId = baseEntity.getCommonId();
                                commonIdAndGoodsId.setGoodsId(goodsId);
                                commonIdAndGoodsId.setCommonId(commonId);
                                // 跳转商品
                                slideUrlList.setOpType(2);
                                slideUrlList.setCommonIdAndGoodsId(commonIdAndGoodsId);
                            } else {
                                if (shopSlideurl.contains("gid=") && shopSlideurl.contains("type=goods")) {
                                    String[] amp = shopSlideurl.split("&amp;");
                                    String gid = "";
                                    Integer goodsId = null;
                                    for (String str : amp) {
                                        if (str.contains("gid=")) {
                                            gid = str;
                                        }
                                    }
                                    if (!"".equals(gid)) {
                                        goodsId = Integer.parseInt(gid.split("=")[1]);
                                        CommonAndGoodsVO commonIdAndGoodsId = new CommonAndGoodsVO();
                                        GoodsBaseEntity baseEntity = goodsBaseMapper.selectById(goodsId);
                                        Integer commonId = baseEntity.getCommonId();
                                        commonIdAndGoodsId.setGoodsId(goodsId);
                                        commonIdAndGoodsId.setCommonId(commonId);
                                        // 跳转商品
                                        slideUrlList.setOpType(2);
                                        slideUrlList.setCommonIdAndGoodsId(commonIdAndGoodsId);
                                    } else {
                                        slideUrlList.setOpType(7);
                                    }

                                } else if (shopSlideurl.contains("type") && !shopSlideurl.contains("gid=")) {
                                    // 跳转活动页
                                    slideUrlList.setOpType(8);
                                    slideUrlList.setActivityPage(shopSlideurl);
                                } else {
                                    slideUrlList.setOpType(7);
                                }

                            }
                        }

                    } else {
                        slideUrlList.setOpType(7);
                    }
                    shopSlideurlList.add(slideUrlList);
                }
                length = shopSlideArr.length;
            } else {
                length = 0;
            }

            List<ShopSlideUrlVO> slideurlList = Lists.newArrayList();
            if (length.equals(shopSlideurlList.size())) {
                for (int i = 0; i < shopSlideurlList.size(); i++) {
                    if (!"".equals(shopSlideArr[i])) {
                        shopSlideurlList.get(i).setShopSlideUrl(shopSlideArr[i]);
                        slideurlList.add(shopSlideurlList.get(i));
                    }
                }
            }

            shopBase.setShopSlideurlList(slideurlList);

        }
    }

    /**
     * 获取热搜词汇并更新数据库
     * 该方法旨在处理热搜词汇的获取和更新，根据传入的查询参数，
     * 判断词汇是否已存在于数据库，如果存在则更新搜索次数，否则插入新词汇。
     *
     * @param param 查询参数对象，包含需要查询的热搜词汇的通用名称等信息
     */
    private void getHotSearchWords(ListPageQueryParamDTO param) {
        // 获取需要查询的热搜词汇的通用名称
        String commonName = param.getCommonName();
        // 检查通用名称是否非空，避免不必要的数据库操作
        if (null != commonName && !commonName.equals("")) {
            // 根据通用名称查询数据库中是否存在对应的热搜词汇
            GdbMerchantSearchWordVO searchWord = baseMapper.selectBySearchName(commonName);
            // 创建一个记录对象，用于后续可能的数据库插入操作
            GdbMerchantSearchWordVO record = new GdbMerchantSearchWordVO();
            // 判断查询结果是否非空，避免不必要的数据库操作
            if (null != searchWord && !searchWord.equals("")) {
                // 如果词汇已存在，增加其搜索次数
                searchWord.setSearchNums(searchWord.getSearchNums() + 1);
                // 更新数据库中该词汇的搜索次数
                baseMapper.updateByPrimaryKeySelective(searchWord);
            } else {
                // 如果词汇不存在，设置记录对象的属性值，准备插入新词汇
                record.setSearchKeyword(commonName);
                // 设置词汇的拼音索引，便于后续的检索
                record.setSearchCharIndex(PinyinUtils.ToPinyin(commonName));
                // 设置初始搜索次数为1
                record.setSearchNums(1);
                // 插入新词汇到数据库
                baseMapper.insertSelective(record);
            }
        }
    }


    /**
     * 获取商品分类所有的子分类
     *
     * @param allMenu 商品分类菜单列表
     * @param catId   当前分类的ID
     * @return 所有子分类的ID列表
     */
    public List<Integer> getMenuList(List<WebCatMenuVO> allMenu, Integer catId) {
        List<Integer> allChildCatIds = Lists.newArrayList();

        if (!CollectionUtils.isEmpty(allMenu)) {
            for (WebCatMenuVO catMenu : allMenu) {
                if (catMenu.getCatId().equals(catId)) {
                    allChildCatIds.add(catId);
                    getChild(allMenu, catMenu.getCatId(), allChildCatIds);
                }

            }
        }
        return allChildCatIds;

    }

    /**
     * 递归查找子菜单
     *
     * @param allMenu        商品分类菜单列表
     * @param parentId       父菜单ID
     * @param allChildCatIds 所有子分类的ID列表
     * @return 所有子分类的ID列表，如果为空则返回null
     */
    private List<Integer> getChild(List<WebCatMenuVO> allMenu, Integer parentId, List<Integer> allChildCatIds) {

        List<Integer> childList = Lists.newArrayList();
        for (WebCatMenuVO catMenu : allMenu) {
            if (catMenu.getCatParentId().equals(parentId)) {
                allChildCatIds.add(catMenu.getCatId());
                childList.add(catMenu.getCatId());
                getChild(allMenu, catMenu.getCatId(), allChildCatIds);
            }
        }

        if (CollectionUtils.isEmpty(childList)) {
            return null;
        }

        return allChildCatIds;
    }

    /**
     * 处理商品参与的活动
     *
     * @param goodsId      商品ID
     * @param commonId     活动ID
     * @param commonIsXian 商品参与的活动类型
     * @return 处理后的活动商品详情
     */
    @Override
    public ActivityGoodsDetailDTO dealActivity(Integer goodsId, Integer commonId, Integer commonIsXian) {
        // 活动商品详情
        ActivityGoodsDetailDTO activityGoodsDetail = null;
        // 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣
        if (!commonIsXian.equals(CommonType.promotionStatus.NOT_PARTICIPATING.getCode())) {
            activityGoodsDetail = new ActivityGoodsDetailDTO();
            if (commonIsXian.equals(CommonType.promotionStatus.SECKILL.getCode())) {
                // 秒杀
                commonIsXian = dealSeckillActivity(goodsId, commonIsXian, activityGoodsDetail);
            } else if (commonIsXian.equals(CommonType.promotionStatus.DISCOUNT.getCode())) {
                // 限时折扣
                commonIsXian = dealDiscountActivity(goodsId, null, commonIsXian, activityGoodsDetail);
            } else if (commonIsXian.equals(CommonType.promotionStatus.COLLAGE.getCode())) {
                // 拼团
                commonIsXian = dealPintuanActivity(commonId, commonIsXian, goodsId, activityGoodsDetail);
            } else if (commonIsXian.equals(CommonType.promotionStatus.FULL_REDUCTION_AND_FREE_DISCOUNT.getCode())) {
                commonIsXian = dealManjianzengzheActivity(commonId, null, commonIsXian, goodsId, activityGoodsDetail);
            }
            activityGoodsDetail.setActivityStatus(commonIsXian);
        }

        return activityGoodsDetail;
    }


    /**
     * 处理秒杀活动
     * 根据商品ID和活动状态，获取并更新秒杀活动详情
     *
     * @param goodsId             商品ID
     * @param commonIsXian        活动状态标识
     * @param activityGoodsDetail 秒杀活动商品详情DTO
     * @return 更新后的活动状态标识
     */
    private Integer dealSeckillActivity(Integer goodsId, Integer commonIsXian, ActivityGoodsDetailDTO activityGoodsDetail) {
        // 创建映射以存储查询参数
        Map<String, Object> map = new HashMap<>();
        // 向查询参数中添加商品ID
        map.put("goodsId", goodsId);
        // 通过服务客户端查询秒杀商品信息
        AppSeckillGoodsVO seckillGoods = activityServiceClient.selectByGoodsId(map);
        // 如果找到了对应的秒杀商品信息
        if (null != seckillGoods) {
            // 比较当前时间与秒杀活动结束时间
            if (DateUtils.compareDate2(seckillGoods.getGoodsEndTime())) {
                // 计算剩余时间
                Long overplusTime = DateUtils.getSecondByMinusDate(new Date(), seckillGoods.getGoodsEndTime());

                // 设置活动商品购买数量
                activityGoodsDetail.setGoodsBuyCount(seckillGoods.getSeckillNum() != null ? seckillGoods.getSeckillNum() : 0);
                // 设置剩余时间
                activityGoodsDetail.setOverplusTime(overplusTime);
                // 设置活动价格
                activityGoodsDetail.setActivityPrice(seckillGoods.getSeckillPrice());
            } else {
                // 如果活动已结束，更新状态标识
                commonIsXian = 0;
            }
        } else {
            // 如果没有找到秒杀商品信息，更新状态标识
            commonIsXian = 0;
        }
        // 返回更新后的活动状态标识
        return commonIsXian;
    }

    /**
     * 查询商品订单参数
     * 根据商品ID查询商品订单参数信息
     *
     * @param goodsId 商品ID
     * @return 包含商品订单参数信息的响应结果
     */
    @Override
    public ResponseResult goodsOrderParam(String goodsId) {
        // 调用基础Mapper查询商品订单参数
        Map<String, Object> map = baseMapper.goodsOrderParam(goodsId);
        // 返回成功响应结果，携带查询到的商品订单参数信息
        return ResponseResult.success(map);
    }

    /**
     * 根据商品销量查询
     * 根据用户权限和店铺ID查询商品销售相关数据
     *
     * @param param 用户权限令牌DTO
     * @return 包含商品销售相关数据的响应结果
     */
    @Override
    public ResponseResult queryByGoodsSalesum(UserTokenBeanDTO param) {
        // 判断用户权限
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());
        // 创建用于存储查询结果的VO对象
        ManageIndexVO result = new ManageIndexVO();
        // 如果用户有权限
        if (userPermissionsResult.getFlag()) {
            // 获取用户对应的店铺ID
            Integer shopId = Integer.valueOf(userPermissionsResult.getShopId());

            // 查询已通过审核的商品数量
            Integer verify = goodsCommonMapper.selectVerify(shopId);
            // 查询正在出售的商品数量
            Integer saleSum = goodsCommonMapper.selectSaleing(shopId);
            // 查询待审核的商品数量
            Integer waitVerify = goodsCommonMapper.selectWaitVerify(shopId);
            // 初始化仓库中的商品数量
            Integer storeHouse = 0;
            // 创建查询DTO
            ManageGdsNameQueryDTO param1 = new ManageGdsNameQueryDTO();
            param1.setShopId(shopId.toString());
            // 查询违规商品数量
            Integer num1 = goodsCommonMapper.selectViolationsGoodsCount(param1);
            // 查询已下架商品数量
            Integer num2 = goodsCommonMapper.selectStoreGoodsCount(param1);
            // 查询待审核的商品数量
            Integer num3 = goodsCommonMapper.selectVerifyGoodsCount(param1);
            // 查询审核失败的商品数量
            Integer num4 = goodsCommonMapper.selectVerifyFailGoodsCount(param1);
            // 查询待上架经销商品数量
            Integer num5 = goodsCommonMapper.queryIsPriceUpdateCount(param1);
            // 计算仓库中的商品总数
            storeHouse = num1 + num2 + num3 + num4 + num5;
            // 查询违规下架的商品数量
            Integer irregularities = goodsCommonMapper.selectIrregularities(shopId);

            // 将查询结果设置到VO对象中
            result.setIrregularities(irregularities);
            result.setSaleSum(saleSum);
            result.setStoreHouse(storeHouse);
            result.setVerify(verify);
            result.setWaitVerify(waitVerify);

        } else {
            // 如果用户没有权限，返回失败响应结果
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        }
        // 返回成功响应结果，携带查询结果
        return ResponseResult.success(result);
    }


    /**
     * 根据商品名称查询商品信息
     *
     * @param param 查询参数对象，包含商品名称、店铺ID等信息
     * @return 包含分页查询结果的响应对象
     */
    @Override
    public ResponseResult queryByGoodsName(ManageGdsNameQueryDTO param) {
        // 从令牌中解析用户信息
        UserInfoVO userInfoVO = ParseTokenUtils.getUserInfoByToken(param.getToken());

        // 如果用户信息存在
        if (userInfoVO != null) {
            // 如果查询参数中的供应商店ID为空，则将当前用户的店铺ID设置为查询条件
            if (null == param.getSupplyShopId()) {
                param.setShopId(userInfoVO.getShopId());
            }
            // 分页查询数据
            Map data = null;
            try {
                // 将查询参数对象转换为Map
                data = ParseMapUtils.beanToMap(param);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            // 执行分页查询
            IPage<ManageGdsNameQueryVO> page = baseMapper.selectByGdsName(new Query<ManageGdsNameQueryVO>().getPage(data), param);
            List<ManageGdsNameQueryVO> goods = page.getRecords();

            // 处理查询结果，为每个商品查询其优惠券信息
            for (ManageGdsNameQueryVO good : goods) {
                List<VoucherMessageVO> voucherMes = goodsCommonMapper.selectVoucherMes(good.getCommonId());
                if (CollectionUtils.isEmpty(voucherMes)) {
                    voucherMes = Lists.newArrayList();
                }
                good.setVoucherMes(voucherMes);
                // 如果商品的上架状态未设置，默认设置为上架
                if (null == good.getSellEdit()) {
                    good.setSellEdit(1);
                }
            }
            // 返回分页查询结果
            return ResponseResult.success(PageUtils.getPage(page));
        } else {
            // 如果用户信息不存在，返回权限不足的错误
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        }
    }

    /**
     * 根据商品通用ID删除商品信息
     *
     * @param param 删除参数对象，包含商品通用ID列表和操作令牌
     * @return 响应对象，表示删除操作的结果
     */
    @Override
    @Transactional
    public ResponseResult deleteByCommonId(ManageCommonIdsDTO param) {
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 判断操作权限
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());

        // 如果有权限
        if (userPermissionsResult.getFlag()) {
            // 准备待删除的ID列表
            List<Integer> list = new ArrayList<>();
            List<Integer> commonIds = param.getCommonIds();

            // 执行删除操作
            goodsCommonMapper.updateCommonDel(commonIds);
            goodsBaseMapper.updateCommonDel(commonIds);
            goodsCommonDetailMapper.deleteByPrimaryKey(commonIds);
            goodsImagesMapper.deleteByCommonid(commonIds);
            // 删除 经销同步记录
            baseMapper.deleteDealerPutawayRecord(commonIds);
            // 删除京东经销商品同步记录
            goodsJingdongDropshippingMapper.deleteGoodsRecord(commonIds);
            // 处理供应商商品的下架和ES更新
            List<Integer> supperCommonids = new ArrayList<>();
            for (Integer SuppercommonId : commonIds) {
                // 判断当前商品是否为供应商商品，如果是，则下架对应商家的商品并更新ES
                List<Integer> dealerCommonIds = goodsCommonMapper.queryIsSupperGoods(SuppercommonId);
                if (null != dealerCommonIds && dealerCommonIds.size() > 0) {
                    goodsCommonMapper.updateOffline(dealerCommonIds);
                    supperCommonids.addAll(dealerCommonIds);
                }
            }

            // 删除相关活动商品信息
            NoneGoodsUpdateDTO nguParam = new NoneGoodsUpdateDTO();
            nguParam.setCommonIdList(commonIds);
            activityNoneGoodsMapper.deleteNoneGoods(nguParam);
            // 删除商品在redis中对应的库存信息
            List<Integer> goodsIds = goodsBaseMapper.queryGoodsByCommonIds(commonIds);
            for (Integer goodsId : goodsIds) {
                RedisUtil.remove(HouConfig.GOODS_STOCK + goodsId);
            }

            // 通过MQ更新ES的商品数据
            Map<String, Object> map = new HashMap<>();
            list.addAll(supperCommonids);
            list.addAll(param.getCommonIds());
            map.put("commonIdS", list);
            // 2表示删除操作
            map.put("type", 2);
            MqSendUtils.mqSendGoods(map);
            // 增加版本号
            asyncVersionService.addGoodsVersionToList(commonIds, param.getToken());
            // 返回成功响应
            return ResponseResult.success();
        } else {
            // 如果无权限，返回权限不足的错误
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        }
    }

    /**
     * 查询商品的锁定状态
     *
     * @param param 查询参数对象，包含商品通用ID
     * @return 包含商品锁定状态的响应对象
     */
    @Override
    public ResponseResult queryGoodsLockedStatus(GoodsDetailsDTO param) {
        // 查询并返回商品的锁定状态
        Integer commonIsLock = goodsCommonMapper.selectGoodsLockedStatus(param.getCommonId());
        return ResponseResult.success(commonIsLock);
    }

    /**
     * 查询可选商品列表
     *
     * @return 包含可选商品列表的响应结果
     */
    @Override
    public ResponseResult selectplusgoods() {
        List<PcConfigGoodsVO> pcConfigGoodsParams = baseMapper.pcConfigGoods();
        return ResponseResult.success(pcConfigGoodsParams);
    }

    /**
     * 查询仓库中的商品列表
     *
     * @param param 查询参数，包含商品信息和用户认证信息
     * @return 包含仓库商品列表的响应结果
     * @throws IllegalAccessException 如果访问权限检查失败
     */
    @Override
    public ResponseResult queryStoreHouse(ManageGdsNameQueryDTO param) throws IllegalAccessException {
        // 权限判断
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());

        if (userPermissionsResult.getFlag()) {
            param.setShopId(userPermissionsResult.getShopId());
            Map data = ParseMapUtils.beanToMap(param);
            IPage<ManageGdsNameQueryVO> page = baseMapper.selectStoreGoods(new Query<ManageGdsNameQueryVO>().getPage(data),
                    param);

            return ResponseResult.success(PageUtils.getPage(page));
        } else {
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        }

    }

    /**
     * 查询待审核的商品列表
     *
     * @param param 查询参数，包含商品信息和用户认证信息
     * @return 包含待审核商品列表的响应结果
     * @throws IllegalAccessException 如果访问权限检查失败
     */
    @Override
    public ResponseResult queryVerifyGoods(ManageGdsNameQueryDTO param) throws IllegalAccessException {
        // 权限判断
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());

        if (userPermissionsResult.getFlag()) {
            param.setShopId(userPermissionsResult.getShopId());
            Map data = ParseMapUtils.beanToMap(param);
            IPage<ManageGdsNameQueryVO> page = baseMapper.selectVerifyGoods(new Query<ManageGdsNameQueryVO>().getPage(data),
                    param);
            return ResponseResult.success(PageUtils.getPage(page));
        } else {
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        }

    }

    /**
     * 查询审核未通过的商品列表
     *
     * @param param 查询参数，包含商品信息和用户认证信息
     * @return 包含审核未通过商品列表的响应结果
     * @throws IllegalAccessException 如果访问权限检查失败
     */
    @Override
    public ResponseResult queryVerifyFailGoods(ManageGdsNameQueryDTO param) throws IllegalAccessException {
        // 权限判断
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());

        if (userPermissionsResult.getFlag()) {
            param.setShopId(userPermissionsResult.getShopId());
            Map data = ParseMapUtils.beanToMap(param);
            IPage<ManageGdsNameQueryVO> page = baseMapper.selectVerifyFailGoods(new Query<ManageGdsNameQueryVO>().getPage(data),
                    param);

            return ResponseResult.success(PageUtils.getPage(page));
        } else {
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        }

    }


    /**
     * 查询违规商品列表
     *
     * @param param 查询参数对象，包含商品信息和分页信息
     * @return 包含违规商品列表的响应结果
     * @throws IllegalAccessException 如果访问检查失败
     */
    @Override
    public ResponseResult queryViolationsGoods(ManageGdsNameQueryDTO param) throws IllegalAccessException {

        // 权限判断，检查用户是否具有商品管理权限
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());

        if (userPermissionsResult.getFlag()) {
            // 如果用户有权限，设置店铺ID并记录通用上架时间
            param.setShopId(userPermissionsResult.getShopId());
            Date commonSellTime = new Date();
            param.setCommonSellTime(commonSellTime);

            // 将查询参数对象转换为Map
            Map data = ParseMapUtils.beanToMap(param);

            // 执行查询，获取违规商品列表
            IPage<ManageGdsNameQueryVO> page = baseMapper.selectViolationsGoods(new Query<ManageGdsNameQueryVO>().getPage(data),
                    param);

            // 返回成功响应，包含分页商品数据
            return ResponseResult.success(PageUtils.getPage(page));
        } else {
            // 如果用户没有权限，返回权限不足的错误响应
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        }
    }

    /**
     * 根据运输模板ID查询商品列表
     *
     * @param param 查询参数对象，包含运输模板ID和分页信息
     * @return 包含商品列表的响应结果
     * @throws IllegalAccessException 如果访问检查失败
     */
    @Override
    public ResponseResult queryGoodsByTemplateId(TransportTemplateDetailDeleteDTO param) throws IllegalAccessException {
        // 权限判断，检查用户是否具有商品管理权限
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());

        // 设置店铺ID
        param.setShopId(userPermissionsResult.getShopId());

        // 将查询参数对象转换为Map
        Map data = ParseMapUtils.beanToMap(param);

        // 执行查询，获取对应运输模板ID的商品列表
        IPage<ManageTransportGoodsVO> page = baseMapper.selectByTemplateId(new Query<ManageTransportGoodsVO>().getPage(data),
                param);

        // 返回成功响应，包含分页商品数据
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 根据运输区域ID查询商品列表
     *
     * @param param 查询参数对象，包含运输区域ID和分页信息
     * @return 包含商品列表的响应结果
     * @throws IllegalAccessException 如果访问检查失败
     */
    @Override
    public ResponseResult queryGoodsByTransportAreaId(TransportTemplateDetailDeleteDTO param) throws IllegalAccessException {
        // 权限判断，检查用户是否具有商品管理权限
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());

        // 设置店铺ID
        param.setShopId(userPermissionsResult.getShopId());

        // 将查询参数对象转换为Map
        Map data = ParseMapUtils.beanToMap(param);

        // 执行查询，获取对应运输区域ID的商品列表
        IPage<ManageTransportGoodsVO> page = baseMapper.selectByTransportAreaId(new Query<ManageTransportGoodsVO>().getPage(data),
                param);

        // 返回成功响应，包含分页商品数据
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 更新商品通用ID列表的IsXian2字段
     *
     * @param commonIdList 商品通用ID列表
     */
    @Override
    public void updateCommonIsXian2(List<Integer> commonIdList) {
        // 调用持久层方法更新IsXian2字段
        goodsCommonMapper.updateCommonIsXian2(commonIdList);
    }

    /**
     * 更新商品的“是否显示”状态
     *
     * @param param 包含“commonIsXian”和“commonId”的映射
     */
    @Override
    public void updateCommonIsXian(Map<String, Object> param) {
        Integer commonIsXian = (Integer) param.get("commonIsXian");
        Integer commonId = (Integer) param.get("commonId");
        goodsCommonMapper.updateCommonIsXian(commonIsXian, commonId);
    }

    /**
     * 查询促销商品列表
     *
     * @param param 查询参数对象
     * @return 包含商品列表和总数的响应结果
     */
    @Override
    public ResponseResult queryGoodsList(GoodsListQueryDTO param) {
        PromotionGoodsVO result = new PromotionGoodsVO();
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
                logger.info("queryGoodsList - 开始从SecurityContext获取用户信息");
                UserInfoVO userInfo = SecurityContextUtils.getCurrentUser();
                logger.info("queryGoodsList - 获取用户信息成功: {}, shopId: {}, userId: {}", 
                    userInfo != null ? "成功" : "null",
                    userInfo != null ? userInfo.getShopId() : "null",
                    userInfo != null ? userInfo.getUserId() : "null");
                
                if (userInfo == null) {
                    logger.error("queryGoodsList - 用户信息为null，返回权限不足");
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
                logger.info("queryGoodsList - UserPermissionsResult构建成功，shopId: {}, userId: {}", 
                    userPermissionsResult.getShopId(), userPermissionsResult.getUserId());
            } catch (Exception e) {
                logger.error("queryGoodsList - 从SecurityContext获取用户信息失败: {}", e.getMessage(), e);
                return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
            }
        }

        if (userPermissionsResult.getFlag()) {
            param.setShopId(userPermissionsResult.getShopId());
            param.setPage((param.getPage() - 1) * param.getLimit());

            List<PromotionGoodsListVO> goodsList = goodsCommonMapper.selectPromotionGoodsList(param);
            Integer totalCount = goodsCommonMapper.selectPromotionGoodsListCount(param);
            totalCount = totalCount == null ? 0 : totalCount;
            result.setGoodsList(goodsList);
            result.setTotalCount(totalCount);
        } else {
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        }

        return ResponseResult.success(result);
    }

    /**
     * 查询活动商品
     *
     * @param param 查询参数对象
     * @return 包含活动商品列表的响应结果
     * @throws IllegalAccessException 当转换参数对象到Map失败时抛出异常
     */
    @Override
    public ResponseResult queryactivityGoods(ManageGdsNameQueryDTO param) throws IllegalAccessException {
        // 权限判断
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());
        if (userPermissionsResult.getFlag()) {
            if (null == param.getSupplyShopId()) {
                param.setShopId(userPermissionsResult.getShopId());
            }
            // 查询出售中的商品
            Map data = ParseMapUtils.beanToMap(param);
            IPage<ManageGdsNameQueryVO> page = baseMapper.selectactivityGoods(new Query<ManageGdsNameQueryVO>().getPage(data),
                    param);

            for (ManageGdsNameQueryVO good : page.getRecords()) {
                List<VoucherMessageVO> voucherMes = goodsCommonMapper.selectVoucherMes(good.getCommonId());
                if (CollectionUtils.isEmpty(voucherMes)) {
                    voucherMes = Lists.newArrayList();
                }
                good.setVoucherMes(voucherMes);
                if (null == good.getSellEdit()) {
                    good.setSellEdit(1);
                }
            }
            return ResponseResult.success(PageUtils.getPage(page));
        } else {
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        }

    }

    /**
     * 查询管理空间的所有商品信息
     *
     * @param param 查询参数对象
     * @return 包含所有商品信息的响应结果
     */
    @Override
    public ResponseResult queryManageSpaceAllGoods(PlatFormAllGoodsQueryDTO param) {
        GoodsCatSearchChildVO goodsCatSearchChild = null;
        List<WebCatMenuVO> allMenu = null;
        Map map = null;
        List<Integer> catIds = null;
        try {
            map = ParseMapUtils.beanToMap(param);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        // 判断分类ID是否为空，如果不为空则尝试从缓存或数据库获取分类信息
        if (null != param.getCatId()) {
            // 尝试从缓存中获取分类信息
            String cacheCatIds = RedisUtil
                    .get(Constants.CACHE_GOODS_WEB_GOODS_LIST_HEADER_PREFIX + "_" + param.getCatId());
            if (null != cacheCatIds && !cacheCatIds.isEmpty()) {
                goodsCatSearchChild = JsonUtils.string2Obj(cacheCatIds, GoodsCatSearchChildVO.class);
                catIds = goodsCatSearchChild.getCatIds();
            } else {
                // 如果缓存中没有，则查询所有分类并构建分类列表
                allMenu = goodsCatMapper.selectAllCat();
                catIds = getMenuList(allMenu, param.getCatId());
                goodsCatSearchChild = new GoodsCatSearchChildVO();
                goodsCatSearchChild.setAllMenu(allMenu);
                goodsCatSearchChild.setCatIds(catIds);
                // 将分类信息存入缓存
                RedisUtil.set(Constants.CACHE_GOODS_WEB_GOODS_LIST_HEADER_PREFIX + "_" + param.getCatId(),
                        JsonUtils.toString(goodsCatSearchChild), RedisUtil.EXRP_MONTH);
            }

            // 更新查询参数，包括分类ID列表
            if (!CollectionUtils.isEmpty(catIds)) {
                catIds.add(param.getCatId());
                param.setCatIds(catIds);
                param.setCatId(0);
            }
        }
        IPage<PlatFormAllGoodsVO> iPage = goodsCommonMapper.selectByAllGoodsToSpace(new Query<PlatFormAllGoodsVO>().getPage(map), param);
        List<PlatFormAllGoodsVO> records = iPage.getRecords();
        if (!CollectionUtils.isEmpty(records)) {
            for (PlatFormAllGoodsVO good : records) {
                List<String> noneNameList = goodsCommonMapper.selectNoneName(Integer.valueOf(good.getGoodsId()));
                good.setNoneNameList(noneNameList);

                // 计算价格
                ResponseResult<PointsCashRatioVo> pointsCashRatio = baseServiceClient.getPointsCashRatio();
                if (pointsCashRatio.getCode() == 1) {
                    Double pointsRate = good.getPointsRate();
                    PointsCashRatioVo ratio = pointsCashRatio.getData();
                    if (Objects.nonNull(pointsRate)) {
                        PointsRatioUtil.PointsCashRatioRes pointsRatio = PointsRatioUtil.getPointsRatio(good.getCommonPrice(), pointsRate);
                        good.setCashPrice(pointsRatio.getCashPrice());
                    }
                }
            }
        }
        return ResponseResult.success(PageUtils.getPage(iPage));
    }

    /**
     * 根据类型查询商品数量
     *
     * @param map 包含token和查询类型的映射
     * @return 包含查询结果的响应对象
     */
    @Override
    public ResponseResult goodsCount(Map<String, Object> map) {
        String token = (String) map.get("token");
        UserInfoVO info = ParseTokenUtils.getPlatformUser(token);
        if (info != null) {
            try {
                // 类型:1、全部，2、当日，3、本周，4、本月，5、年度
                Integer type = (Integer) map.get("type");
                Integer goodsCount = 0;
                if (type == 1) {
                    goodsCount = baseMapper.queryAllGoodsCount();
                } else if (type == 2) {
                    goodsCount = baseMapper.queryTodayGoodsCount();
                } else if (type == 3) {
                    goodsCount = baseMapper.queryWeekGoodsCount();
                } else if (type == 4) {
                    goodsCount = baseMapper.queryMonthGoodsCount();
                } else {
                    goodsCount = baseMapper.queryYearGoodsCount();
                }
                return ResponseResult.success(goodsCount);
            } catch (Exception e) {
                return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(), BizCodeEnume.SYSTEM_ERROR.getMsg());
            }
        } else {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
    }

    /**
     * 查询当日商品数量
     *
     * @param map 包含token的映射
     * @return 包含查询结果的响应对象
     */
    @Override
    public ResponseResult goodsTodayNumber(Map<String, Object> map) {
        String token = (String) map.get("token");
        UserInfoVO info = ParseTokenUtils.getPlatformUser(token);
        if (info != null) {
            try {
                Integer goodsCount = baseMapper.queryTodayGoodsCount();
                return ResponseResult.success(goodsCount);
            } catch (Exception e) {
                return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(), BizCodeEnume.SYSTEM_ERROR.getMsg());
            }
        } else {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
    }

    /**
     * 查询所有商品信息
     *
     * @param param 查询参数
     * @return 包含查询结果的响应对象
     * @throws IllegalAccessException 当访问权限不足时抛出异常
     */
    @Override
    public ResponseResult queryAllGoods(PlatFormAllGoodsDTO param) throws IllegalAccessException {
        if (param.getCatId() != null) {
            List<Integer> catIds = goodsCatMapper.getAllSonLevelCat(param.getCatId());
            param.setCatIdList(catIds);
        }
        //查询所有商品
        Map data = ParseMapUtils.beanToMap(param);
        IPage<PlatFormAllGoodsVO> page = baseMapper.selectByAllGoods(new Query<PlatFormAllGoodsVO>().getPage(data),
                param);
        for (PlatFormAllGoodsVO good : page.getRecords()) {
            //查询当前商品所上上架的专区
            List<String> noneNameList = goodsCommonMapper.selectNoneName(good.getCommonId());
            good.setNoneNameList(noneNameList);

            List<GoodsStockVO> stockEntityList = new ArrayList<>();
            //查询当前商品不同规格下的库存
            stockEntityList = goodsCommonMapper.selectStocks(good.getCommonId());
            good.setGoodsStockEntityList(stockEntityList);

            // 1 是代发商品
            if (1 == good.getDealerGoodsFlag()) {
                // 查询代发商品 所同步的供应商商品是否在售
                Integer supplierGoodsStart = goodsCommonMapper.querySupplierGoodsStart(good.getCommonId());
                if (0 < supplierGoodsStart) {
                    // 商品 不在售 所有经销商品不能 上架
                    good.setGoodsDealerDesc("商品在供应商端未在售,经销商品不能上架!");
                }
            }
        }


        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 查询代销商品列表
     *
     * @param param 查询参数
     * @return 包含查询结果的响应对象
     * @throws IllegalAccessException 当访问权限不足时抛出异常
     */
    @Override
    public ResponseResult queryDealerGoods(ManageGdsNameQueryDTO param) throws IllegalAccessException {
        Map data = ParseMapUtils.beanToMap(param);
        IPage<ManageGdsNameQueryVO> page = baseMapper.queryDealerGoods(new Query<ManageGdsNameQueryVO>().getPage(data),
                param);
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 更新积分商品信息
     *
     * @param param 积分商品的参数信息，包含商品详情及操作所需的令牌等
     * @return 更新结果，包含操作结果及商品信息
     */
    @Override
    public ResponseResult updateIntegralGoods(IntegralCommonDTO param) {
        // 解析令牌获取当前用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        if (null != userInfo) {
            // 设置店铺ID，此处示例为平台店铺，故ID为0
            Integer shopId = 0;
            // 设置店铺名称为平台店铺
            param.setShopName("平台店铺");
            // 将店铺ID设置到参数中
            param.setShopId(shopId.toString());
            // 设置商品的通用立方体积为0
            param.setCommonCubage(new BigDecimal(0));
            // 获取需要更新的商品ID
            Integer commonId = param.getCommonId();

            // 如果商品状态为空或为0，则设置状态为0
            if (param.getCommonState() == null || param.getCommonState() == 0) {
                param.setCommonState(0);
            }
            // 根据是否有视频来判断商品主图类型
            if (StringUtils.isNotEmpty(param.getCommonVideo())) {
                // 如果有视频，设置类型为2
                param.setType(2);
            }
            // 获取商品规格信息
            List<BaseAddParamDTO> gdsBases = param.getGdsBases();
            // 初始化待添加的商品规格列表
            List<BaseAddParamDTO> addGoodsBases = Lists.newArrayList();
            // 初始化待更新的商品规格列表
            List<BaseAddParamDTO> updateGoodsBases = Lists.newArrayList();
            // 初始化待更新的商品状态列表
            List<Integer> updateGoodsStatus = Lists.newArrayList();
            // 初始化商品主图变量
            String image = null;
            // 如果商品图片列表不为空，获取第一张图片作为主图
            if (!CollectionUtils.isEmpty(param.getGoodsImages())) {
                image = param.getGoodsImages().get(0).getImagesImage();
                // 将主图设置到参数中
                param.setCommonImage(image);
            }
            if (!CollectionUtils.isEmpty(gdsBases)) {
                for (BaseAddParamDTO gdsBase : gdsBases) {
                    gdsBase.setGoodsPromotionTips(param.getCommonPromotionTips());
                    if (null == param.getBrandId()) {
                        gdsBase.setBrandId(0);
                    }
                    if (param.getCommonState().equals(1)) {
                        gdsBase.setGoodsIsShelves(1);
                    } else {
                        gdsBase.setGoodsIsShelves(2);
                    }
                    gdsBase.setGoodsName(param.getCommonName());
                    gdsBase.setGoodsImage(image);
                    gdsBase.setGoodsIsRecommend(param.getCommonIsRecommend());
                    gdsBase.setCommonId(commonId);
                    gdsBase.setShopId(shopId.toString());
                    List<GoodBaseSpecValuesVO> specValues = gdsBase.getSpecValues();
                    String goodsSpec = null;
                    for (GoodBaseSpecValuesVO specValue : specValues) {
                        if (goodsSpec == null) {
                            goodsSpec = specValue.getParamValue();
                            continue;
                        }
                        goodsSpec = goodsSpec + "+" + specValue.getParamValue();
                    }
                    gdsBase.setGoodsSpec(goodsSpec);
                    if (null == gdsBase.getGoodsId()) {
                        addGoodsBases.add(gdsBase);
                    } else {
                        updateGoodsStatus.add(gdsBase.getGoodsId());
                        updateGoodsBases.add(gdsBase);
                    }
                }
            } else {
                return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());
            }
            param.setCommonUpdateBy(Integer.parseInt(userInfo.getUserId()));
            param.setCommonUpdateTime(new Date());

            // 修改状态为审核中
            param.setCommonVerify(1);
            param.setSpecParamJson(JSONObject.toJSONString(param.getSpecParam()));
            // 插入数据到公共信息表,并获取common_id
            baseMapper.updateGdsCommon(param);
            // 更新商品状态
            baseMapper.updateGoodsIsShelves(updateGoodsStatus, commonId);

            // 添加goods_base
            if (!CollectionUtils.isEmpty(addGoodsBases)) {
                for (BaseAddParamDTO addGoodsBase : addGoodsBases) {
                    baseMapper.updateGdsBasesBatch2Obj(addGoodsBase);
                }
            }
            if (!CollectionUtils.isEmpty(updateGoodsBases)) {
                for (BaseAddParamDTO record : updateGoodsBases) {
                    if (null == record.getGoodsMaxSale() || 0 == record.getGoodsMaxSale()) {
                        record.setGoodsMaxSale(record.getGoodsStock());
                    }
                    baseMapper.updateGdsBasesBatch2Obj(record);
                    //将数据更新到商品库存表
                    GoodsStockEntity entity = goodsStockService.getOne(new LambdaUpdateWrapper<GoodsStockEntity>()
                            .eq(GoodsStockEntity::getGoodsId, record.getGoodsId())
                            .eq(GoodsStockEntity::getCommonId, commonId));
                    GoodsStockEntity goodsStockEntity = new GoodsStockEntity();
                    goodsStockEntity.setId(entity.getId());
                    goodsStockEntity.setGoodsId(record.getGoodsId());
                    goodsStockEntity.setCommonId(commonId);
                    goodsStockEntity.setGoodsStock(record.getGoodsStock());
                    goodsStockEntity.setGoodsAlarm(record.getGoodsAlarm());
                    goodsStockEntity.setUpdateTime(new Date());
                    goodsStockEntity.setUpdateUser(userInfo.getUserId());
                    goodsStockService.updateById(goodsStockEntity);
                }
            }
            // 添加商品详情
            GoodsCommonDetailDTO goodsCommonDetail = param.getGoodsCommonDetail();
            if (null != goodsCommonDetail) {

                GoodsCommonDetailDTO selectByPrimaryKey = baseMapper.selectCommonDetailByPrimaryKey(commonId);
                goodsCommonDetail.setCommonId(commonId);
                if (null != selectByPrimaryKey) {
                    baseMapper.updateByPrimaryKey(goodsCommonDetail);
                } else {
                    baseMapper.insertSelective2(goodsCommonDetail);
                }

            }
            // 添加商品图片
            List<GoodsImagesDTO> goodsImages = param.getGoodsImages();
            List<GoodsImagesDTO> addImages = new ArrayList<GoodsImagesDTO>();
            List<GoodsImagesDTO> updateImages = new ArrayList<GoodsImagesDTO>();
            List<Integer> removeGoodsImage = Lists.newArrayList();
            if (!CollectionUtils.isEmpty(goodsImages)) {
                for (GoodsImagesDTO goodsImage : goodsImages) {
                    goodsImage.setCommonId(param.getCommonId());
                    goodsImage.setGoodsId(gdsBases.get(0).getGoodsId());
                    goodsImage.setShopId(shopId.toString());
                    if (null == goodsImage.getImagesId() || "".equals(goodsImage.getImagesId().toString())) {
                        addImages.add(goodsImage);
                    } else {
                        removeGoodsImage.add(goodsImage.getImagesId());
                        updateImages.add(goodsImage);
                    }
                }
                if (!CollectionUtils.isEmpty(removeGoodsImage)) {
                    baseMapper.deleteByGoodsImageIds(removeGoodsImage, commonId);
                }
                if (updateImages.size() > 0) {
                    baseMapper.updateBatch(goodsImages);
                } else {
                    baseMapper.deleteByCommonId2(commonId);
                }
                if (addImages.size() > 0) {
                    baseMapper.insertGoodsImage(addImages);
                }
            }
            return ResponseResult.success();
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    @Override
    public ResponseResult goodsQueryDetail(Map<String, Object> map) {
        // 从令牌中解析用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(map.get("token").toString());
        if (null != userInfo) {
            // 初始化返回结果对象
            ManageIntegraCommonVO commonResult = new ManageIntegraCommonVO();
            // 从参数中获取商品ID
            Integer commonId = Integer.valueOf(map.get("commonId").toString());
            // 初始化店铺ID，目前固定为0
            Integer shopId = 0;

            // 根据商品ID查询商品基本信息
            GoodsCommonEntity common = baseMapper.selectOne(new LambdaQueryWrapper<GoodsCommonEntity>()
                    .eq(GoodsCommonEntity::getShopStatus, CommonType.shopStatus.STORE_OPEN_SUCCESS.getCode())
                    .eq(GoodsCommonEntity::getCommonId, map.get("commonId").toString()));

            // 处理商品规格参数
            if (StringUtils.isNotEmpty(common.getSpecParam())) {
                List<SpecParamJsonBeanDTO> list = new ArrayList<>();
                JSONArray jsonSpecParam = JSONObject.parseArray(common.getSpecParam());
                for (int index = 0; index < jsonSpecParam.size(); index++) {
                    JSONObject obj = jsonSpecParam.getJSONObject(index);
                    SpecParamJsonBeanDTO temp = JSONObject.parseObject(obj.toJSONString(), SpecParamJsonBeanDTO.class);
                    list.add(temp);
                }
                common.setSpecParamList(list);
            }

            // 设置运费模板和销售区域信息
            ManageTransportTemplateVO transportTemplate = baseMapper.selectTransportTemplateById(common.getTransportTemplate());
            ManageTransportAreaVO transportAreas = baseMapper.selectTransportAreaByIdArea(common.getTransportAreaId());
            common.setTransportTemplates(transportTemplate);
            common.setTransportAreas(transportAreas);

            // 查询店铺自定义分类信息
            List<ManageGdsBaseInfoDTO> gdsBases = baseMapper.selectByCommonId3(commonId);
            // 查询商品详情信息
            GoodsCommonDetailDTO goodsCommonDetail = baseMapper.selectCommonDetailByPrimaryKey(commonId);
            // 查询商品图片信息
            List<GoodsImagesDTO> goodsImages = baseMapper.selectByCommonId4(commonId, shopId);

            // 设置返回结果
            commonResult.setCommonResult(common);
            commonResult.setGdsBases(gdsBases);
            commonResult.setGoodsCommonDetail(goodsCommonDetail);
            if (CollectionUtils.isEmpty(goodsImages)) {
                GoodsImagesDTO goodsImage = new GoodsImagesDTO();
                goodsImage.setImagesImage(common.getCommonImage());
                goodsImage.setImagesDisplayorder(0);
                goodsImage.setShopId(shopId.toString());
                goodsImage.setImagesIsDefault(1);
                goodsImages.add(goodsImage);
            }
            commonResult.setGoodsImages(goodsImages);
            // 返回成功结果
            return ResponseResult.success(commonResult);
        }
        // 返回令牌非法结果
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    @Override
    public ResponseResult addIntegralGoods(ManageGdsCommonPublishDTO param) {
        // 从令牌中解析用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        if (null != userInfo) {
            // 初始化用户权限信息
            UserPermissionsVO userPermissionsResult = new UserPermissionsVO();
            userPermissionsResult.setShopId(0);
            userPermissionsResult.setUserId("");

            // 初始化商品ID和店铺ID
            Integer commonId = null;
            Integer shopId = userPermissionsResult.getShopId();
            // 设置参数中的店铺ID和更新信息
            param.setShopId(shopId.toString());
            param.setCommonUpdateBy(userInfo.getUserId());
            param.setCommonUpdateTime(new Date());

            // 设置默认参数值
            // 商品上架状态
            param.setCommonState(1);
            // 商品重量
            param.setCommonCubage(new BigDecimal(0));
            param.setGoodsId("0");
            // 审核通过状态
            param.setCommonVerify(1);
            // 库存数量
            param.setCommonStock(0);
            // 设置添加时间和上架时间
            param.setCommonAddTime(new Date());
            param.setCommonSellTime(new Date());
            // 积分商品标志，0表示是积分商品
            param.setIntegralGoods(0);
            // 购买限制，0表示不限购
            short commonLimit = 0;
            param.setCommonLimit(commonLimit);

            // 处理商品图片
            String image = null;
            if (!CollectionUtils.isEmpty(param.getGoodsImages())) {
                image = param.getGoodsImages().get(0).getImagesImage();
                param.setCommonImage(image);
            }
            // 获取店铺区域id
            param.setDistrictId(0);
            param.setShopName("平台店铺");
            //不是自营商品
            param.setShopSelfSupport(0);
            // 判断主图类型
            if (StringUtils.isNotEmpty(param.getCommonVideo())) {
                param.setType(2);
            }
            //商铺状态 开店成功
            param.setShopStatus(3);
            param.setCommonSpecValue("");
            // 将规格 和 规格值保存到数据库 规格跟着商品走
            param.setSpecParamJson(JSON.toJSONString(param.getSpecParam()));
            // 添加数据到公共信息表,并获取common_id
            baseMapper.newInsertGdsCommonObj(param);

            commonId = param.getCommonId();
            // 获取goods_base,赋值commomid
            Integer goodsId = null;
            List<ManageGdsBaseAddPublishDTO> gdsBases = param.getGdsBases();
            for (ManageGdsBaseAddPublishDTO gdsBase : gdsBases) {
                gdsBase.setGoodsPromotionTips(param.getCommonPromotionTips());
                gdsBase.setCommonId(commonId);
                gdsBase.setShopId(0 + "");
                gdsBase.setShopName("平台店铺");
                gdsBase.setGoodsIsRecommend(param.getCommonIsRecommend());
                gdsBase.setGoodsImage(image);
                if (gdsBase.getBrandId() == null) {
                    gdsBase.setBrandId(0);
                }
                if (gdsBase.getGoodsAlarm() == null) {
                    gdsBase.setGoodsAlarm(0);
                }
                if (gdsBase.getGoodsMaxSale() == null) {
                    gdsBase.setGoodsMaxSale(0);
                }

                BaseAddParamDTO manageGdsBaseAddParam = new BaseAddParamDTO();
                manageGdsBaseAddParam.setSpecId(gdsBase.getSpecId());
                manageGdsBaseAddParam.setTypeId(gdsBase.getTypeId());
                if (StringUtils.isNotBlank(gdsBase.getGoodsSpec())) {
                    manageGdsBaseAddParam.setGoodsName("平台店铺" + "(" + gdsBase.getGoodsSpec() + ")");
                } else {
                    manageGdsBaseAddParam.setGoodsName(gdsBase.getGoodsName());
                }
                //带查询
                manageGdsBaseAddParam.setGoodsId(gdsBase.getGoodsId());
                manageGdsBaseAddParam.setCommonId(gdsBase.getCommonId());
                manageGdsBaseAddParam.setShopId(0 + "");
                manageGdsBaseAddParam.setShopName("平台店铺");
                manageGdsBaseAddParam.setGoodsPromotionTips(gdsBase.getGoodsPromotionTips());
                manageGdsBaseAddParam.setCatId(gdsBase.getCatId());
                manageGdsBaseAddParam.setGoodsSpecValue(gdsBase.getGoodsSpecValue());
                manageGdsBaseAddParam.setGoodsPrice(gdsBase.getGoodsPrice());
                manageGdsBaseAddParam.setGoodsMarketPrice(gdsBase.getGoodsMarketPrice());
                manageGdsBaseAddParam.setGoodsStock(gdsBase.getGoodsStock());
                manageGdsBaseAddParam.setGoodsAlarm(gdsBase.getGoodsAlarm());
                manageGdsBaseAddParam.setGoodsCode(gdsBase.getGoodsCode());
                manageGdsBaseAddParam.setGoodsBarcode(gdsBase.getGoodsBarcode());
                manageGdsBaseAddParam.setGoodsIsRecommend(gdsBase.getGoodsIsRecommend());
                manageGdsBaseAddParam.setGoodsImage(gdsBase.getGoodsImage());
                //商品上架
                manageGdsBaseAddParam.setGoodsIsShelves(1);
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
                //积分商品所需的积分
                manageGdsBaseAddParam.setNeedIntegral(gdsBase.getNeedIntegral());
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
                goodsId = manageGdsBaseAddParam.getGoodsId();
                gdsBase.setGoodsId(goodsId);
                //将数据更新到商品库存表
                GoodsStockEntity goodsStockEntity = new GoodsStockEntity();
                goodsStockEntity.setGoodsId(goodsId);
                goodsStockEntity.setCommonId(commonId);
                goodsStockEntity.setGoodsStock(gdsBase.getGoodsStock());
                goodsStockEntity.setGoodsAlarm(gdsBase.getGoodsAlarm());
                goodsStockEntity.setCreateTime(new Date());
                goodsStockEntity.setCreateUser(userInfo.getUserId());
                goodsStockService.save(goodsStockEntity);
                logger.info("商品goods_stock表更新成功");
            }
            // 添加商品详情
            GdbMerchantGoodsCommonDetailDTO goodsCommonDetail = param.getGoodsCommonDetail();
            if (null != goodsCommonDetail) {
                goodsCommonDetail.setCommonId(commonId);
                baseMapper.insertSelective3(goodsCommonDetail);
            }
            // 添加商品图片
            List<GdbMerchantGuiGeGoodsImagesDTO> goodsImages = param.getGoodsImages();
            if (!CollectionUtils.isEmpty(goodsImages)) {
                for (GdbMerchantGuiGeGoodsImagesDTO goodsImage : goodsImages) {

                    if (goodsImage.getImagesDisplayorder() == null) {
                        goodsImage.setImagesDisplayorder(0);
                    }
                    if (goodsImage.getImagesIsDefault() == null) {
                        goodsImage.setImagesIsDefault(1);
                    }
                    goodsImage.setShopId(shopId);
                    goodsImage.setCommonId(commonId);
                    goodsImage.setGoodsId(goodsId);
                }

            } else {
                GdbMerchantGuiGeGoodsImagesDTO goodsImage = new GdbMerchantGuiGeGoodsImagesDTO();
                if (goodsImage.getImagesDisplayorder() == null) {
                    goodsImage.setImagesDisplayorder(0);
                }
                if (goodsImage.getImagesIsDefault() == null) {
                    goodsImage.setImagesIsDefault(1);
                }
                goodsImage.setShopId(shopId);
                goodsImage.setCommonId(commonId);
                goodsImage.setGoodsId(goodsId);
                goodsImages.add(goodsImage);
            }
            if (!goodsImages.isEmpty()) {
                for (GdbMerchantGuiGeGoodsImagesDTO recordImage : goodsImages) {
                    baseMapper.insertGoodsImages(recordImage);
                }
            }
            // 更新商品规格
            newUpdateCommonSpec(commonId, gdsBases);

            return ResponseResult.success();
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());
    }

    /**
     * 查询审核数量
     * 该方法用于查询待审核的商品数量和店铺数量
     *
     * @param map 用于存储查询结果的映射对象
     * @return 包含审核数量信息的响应结果
     */
    @Override
    public ResponseResult queryVerifyNum(Map<String, Object> map) {
        //查询审核中商品
        Integer goodsNum = baseMapper.selectByGoodsCount();
        //查询审核中店铺
        Integer shopNum = baseMapper.selectByShopCount();
        map.put("goodsNum", goodsNum);
        map.put("shopNum", shopNum);
        return ResponseResult.success(map);
    }

    /**
     * 批量更新商品状态
     * 通过映射更新商品的通用状态
     *
     * @param map 包含要更新的商品状态信息的映射对象
     * @return 更新成功的响应结果，失败则返回错误代码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult updateGoodsCommonByMap(Map<String, Object> map) {
        Integer count = baseMapper.updateGoodsCommonByMap(map);
        if (count > 0) {
            return ResponseResult.success();
        }
        return ResponseResult.fail(BizCodeEnume.UPDATE_ERROR);
    }

    /**
     * 根据店铺ID查询通用ID列表
     * 该方法用于获取特定店铺的所有通用ID
     *
     * @param shopId 店铺ID
     * @return 店铺的通用ID列表
     */
    @Override
    public List<Integer> queryCommonIdByShopId(Integer shopId) {
        List<Integer> commonIds = baseMapper.queryCommonIdByShopId(shopId);
        return commonIds;
    }

    /**
     * 更新通用状态
     * 通过DTO参数更新通用状态信息
     *
     * @param commonParam 包含要更新的通用状态信息的DTO对象
     * @return 更新成功的响应结果，失败则返回错误代码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult updateCommonState(CommonShopStateDTO commonParam) {
        Integer count = baseMapper.updateCommonState(commonParam);
        if (count > 0) {
            return ResponseResult.success();
        }
        return ResponseResult.fail(BizCodeEnume.UPDATE_ERROR);
    }


    /**
     * 更新商品的通用规格信息
     *
     * @param commonId      通用规格ID
     * @param addGoodsBases 添加商品基础信息列表
     */
    private void newUpdateCommonSpec(Integer commonId, List<ManageGdsBaseAddPublishDTO> addGoodsBases) {
        // 存储通用规格的价值观对象列表
        List<CommonSpecValueVO> commonSpecValues = Lists.newArrayList();
        // 初始化通用库存
        Integer commonStock = 0;
        // 准备更新商品通用规格的参数对象
        UpdateGdsSpecParamDTO updateGdsSpecParam = new UpdateGdsSpecParamDTO();

        // 遍历商品基础信息列表，组装通用规格的价值观对象
        for (ManageGdsBaseAddPublishDTO goodsBase : addGoodsBases) {
            // 创建基础添加参数对象并设置相关属性
            BaseAddParamDTO manageGdsBaseAddParam = new BaseAddParamDTO();
            manageGdsBaseAddParam.setGoodsSpec(goodsBase.getGoodsName());
            manageGdsBaseAddParam.setSpecId(goodsBase.getSpecId());
            manageGdsBaseAddParam.setTypeId(goodsBase.getTypeId());

            // 创建通用规格的价值观对象并设置ID和商品ID，累加库存
            CommonSpecValueVO value = new CommonSpecValueVO();
            value.setCommonId(commonId);
            value.setGoodsId(goodsBase.getGoodsId());
            commonStock = commonStock + goodsBase.getGoodsStock();
            // 添加到列表中
            commonSpecValues.add(value);
        }
        // 从列表中获取第一个商品基础信息，用于接下来的参数设置
        ManageGdsBaseAddPublishDTO manageGdsBase = addGoodsBases.get(0);
        // 设置更新规格参数对象的属性值
        updateGdsSpecParam.setCommonSpecValue(JSON.toJSONString(commonSpecValues));
        updateGdsSpecParam.setCommonStock(commonStock);
        updateGdsSpecParam.setGoodsId(manageGdsBase.getGoodsId().toString());
        updateGdsSpecParam.setCommonPrice(manageGdsBase.getGoodsPrice());
        updateGdsSpecParam.setCommonMarketPrice(manageGdsBase.getGoodsMarketPrice());
        updateGdsSpecParam.setCommonImage(manageGdsBase.getGoodsImage());
        updateGdsSpecParam.setCommonAlarm(manageGdsBase.getGoodsAlarm());
        updateGdsSpecParam.setCommonCubage(manageGdsBase.getGoodsCubage());
        updateGdsSpecParam.setCommonId(commonId);
        updateGdsSpecParam.setCommonBabyPrice(manageGdsBase.getGoodsBabyPrice());
        updateGdsSpecParam.setTypeId(manageGdsBase.getTypeId());

        // 调用数据库操作方法更新商品规格信息
        baseMapper.updateGdsSpec(updateGdsSpecParam);
    }

    /**
     * 按月查询统计数据
     *
     * @param map 包含查询参数的Map，其中包含token
     * @return 包含查询结果的响应对象
     */
    @Override
    public ResponseResult queryByMonth(Map<String, Object> map) {
        // 从参数Map中获取token字符串
        String token = (String) map.get("token");
        // 解析token获取用户信息
        UserInfoVO info = ParseTokenUtils.getPlatformUser(token);
        if (info != null) {
            // 准备存储每月统计数据的列表
            List<ShopGroupingStatisticsVO> goodsGroupingList = new ArrayList<>();
            // 获取当前年份的字符串
            String year = DateUtils.getYear();
            // 遍历每个月，查询统计数据
            for (int i = 1; i <= 12; i++) {
                ShopGroupingStatisticsVO shopGroupingStatistics = new ShopGroupingStatisticsVO();
                // 获取某年某月的开始和结束时间
                Map<String, String> stringStringMap = DateUtils.scheduleTimeNday(Integer.valueOf(year), i);
                // 查询该时间段内的商品数量
                Integer num = baseMapper.queryGoodsGrouping2(stringStringMap);
                // 设置统计数据对象的数值和时间标签
                shopGroupingStatistics.setNum(num);
                shopGroupingStatistics.setTime(DateUtils.dateGeShiHua(stringStringMap.get("startTimeStr"), "yyyy年MM月"));
                // 添加到结果列表中
                goodsGroupingList.add(shopGroupingStatistics);
            }
            // 返回成功响应，携带查询结果列表
            return ResponseResult.success(goodsGroupingList);
        } else {
            // 返回失败响应，表示token无效
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
    }

    /**
     * 商品上架功能
     * 当商品为批量上架时，调用商品上架批处理方法
     * 当商品为单个时，查询商品信息并进行上架操作
     *
     * @param goodsUserInfo 商品上架信息
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult putAway(DealerGoodsUserInfoDTO goodsUserInfo) {
        ResponseResult resultResp;
        try {
            // 判断是否为批量上架
            if (!CollectionUtils.isEmpty(goodsUserInfo.getCommonIdList())) {
                return goodsPutawayBatch(goodsUserInfo);
            }

            // 查询单个商品信息
            ManageGdsCommonResultVO supplyGoodsCommon = goodsCommonMapper.selectPutawayGoodsInfo(goodsUserInfo.getCommonId());
            // 获取用户信息
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(goodsUserInfo.getToken());
            // 根据店铺ID查询店铺基础信息
            ManageShopBaseQueryResultVO shopBaseQueryResult = goodsCommonMapper.selectByShopId2(userInfo.getShopId());

            // 查询商品配置信息
            BaseGoodsConfigVO goodsConfig = goodsCommonMapper.selectDelayGoodsConfig(supplyGoodsCommon.getCommonId());
            // 执行商品上架操作
            resultResp = addDealerGoodsBatch(userInfo, supplyGoodsCommon, goodsConfig, shopBaseQueryResult, CommonType.supplierGoodsStatus.PUTAWAY.getCode());
            return resultResp;
        } catch (Exception e) {
            // 异常回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(), BizCodeEnume.SYSTEM_ERROR.getMsg());
        }
    }

    /**
     * 经销商下单购买商品
     * 根据订单商品列表，逐个校验商品是否存在，不存在则插入，存在则更新库存
     *
     * @param map 包含订单商品信息和用户Token的Map
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult dealer(Map<String, String> map) {
        logger.info("map{}", JSON.toJSONString(map));
        ResponseResult resultResp;
        try {
            // 解析订单商品列表
            List<DealerOrderGoodsVO> orderGoodsList = JSON.parseArray(map.get("dealerOrderGoodsList"), DealerOrderGoodsVO.class);
            // 获取用户信息
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(map.get("token"));
            // 根据店铺ID查询店铺基础信息
            ManageShopBaseQueryResultVO shopBaseQueryResult = baseMapper.selectByShopId2(userInfo.getShopId());

            // 构建商品ID列表，用于查询购买记录
            List<Integer> supplyGoodsIdList = orderGoodsList.stream().map(DealerOrderGoodsVO::getGoodsId).collect(Collectors.toList());
            DealerGoodsVO dealerGoods = new DealerGoodsVO(userInfo.getShopId(), CommonType.putawayState.NORMAL.getCode(), CommonType.dealerType.BUY.getCode(), supplyGoodsIdList);
            // 查询购买记录
            List<DealerPutawayRecordVO> putawayRecordList = baseMapper.queryBuyRecordList(dealerGoods);
            Map<Integer, DealerPutawayRecordVO> putawayRecordMap = putawayRecordList.stream().collect(Collectors.toMap(DealerPutawayRecordVO::getSupplyGoodsId, Function.identity()));

            // 遍历订单商品列表，进行插入或更新操作
            for (DealerOrderGoodsVO orderGoods : orderGoodsList) {
                // 获取商品基础信息
                GdsBaseDetailResultVO supplyGoodsBase = goodsBaseMapper.selectByGoodsId(orderGoods.getGoodsId());
                // 查询商品配置信息
                BaseGoodsConfigVO goodsConfig = baseMapper.selectDelayGoodsConfig(supplyGoodsBase.getCommonId());

                DealerPutawayRecordVO putawayRecord = putawayRecordMap.get(orderGoods.getGoodsId());
                if (null != putawayRecord) {
                    // 更新商品库存
                    supplyGoodsBase.setGoodsStock(orderGoods.getOrderGoodsNum());
                    resultResp = updateDealerGoods(putawayRecord, supplyGoodsBase, goodsConfig, CommonType.dealerType.BUY.getCode());
                } else {
                    // 查询是否存在其他规格商品的购买记录
                    putawayRecord = new DealerPutawayRecordVO(supplyGoodsBase.getCommonId(), null, userInfo.getShopId(), supplyGoodsBase.getShopId(), CommonType.putawayState.NORMAL.getCode(), CommonType.dealerType.BUY.getCode());
                    putawayRecord = baseMapper.queryPutawayRecord(putawayRecord);
                    if (null != putawayRecord) {
                        // 更新商品库存
                        supplyGoodsBase.setGoodsStock(orderGoods.getOrderGoodsNum());
                        resultResp = addDealerGoods(putawayRecord, userInfo, supplyGoodsBase, goodsConfig, CommonType.dealerType.BUY.getCode());
                    } else {
                        // 新增商品
                        supplyGoodsBase.setGoodsStock(orderGoods.getOrderGoodsNum());
                        resultResp = addDealerGoods(userInfo, supplyGoodsBase, shopBaseQueryResult, goodsConfig, CommonType.dealerType.BUY.getCode());
                        if (resultResp.getCode() == 1) {
                            putawayRecord = (DealerPutawayRecordVO) resultResp.getData();
                        }
                    }
                }
                // 新增上架记录
                if (resultResp.getCode() == 1) {
                    DealerPutawayRecordVO addrecord = new DealerPutawayRecordVO(supplyGoodsBase, userInfo, putawayRecord, shopBaseQueryResult);
                    // 标记为立即购买
                    addrecord.setDealerType(2);
                    goodsCommonMapper.insertputawayRecord(addrecord);
                }
            }
            return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
        } catch (Exception e) {
            logger.error(e.getMessage());
            // 异常回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
        }
    }

    /** 更新商品基本信息 */
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult updateDealerGoods(DealerPutawayRecordVO record, GdsBaseDetailResultVO supplyGoodsBase, BaseGoodsConfigVO goodsConfig, Integer dealerType) {
        try {
            Integer goodsEdit = goodsEditCheck(goodsConfig);
            // 1.更新商品状态
            GdbMerchantGoodsCommonVO updateGoods = new GdbMerchantGoodsCommonVO(record.getDealerCommonId(), dealerType.intValue() == CommonType.dealerType.PUT_AWAY.getCode() ? 1 : 0, supplyGoodsBase.getGoodsStock(), goodsEdit, null);
            goodsCommonMapper.updateDealerGoods(updateGoods);
            // 调用持久层方法更新商品基本信息
            // 2.更新商品状态
            ManageGdsBaseAddParamvDTO gdsBaseAddParam = new ManageGdsBaseAddParamvDTO(record.getDealerGoodsId(), supplyGoodsBase.getCommonId(), supplyGoodsBase.getGoodsStock(), dealerType.intValue() == CommonType.dealerType.PUT_AWAY.getCode() ? 1 : 2, dealerType);
            goodsBaseMapper.updateDealerGoodsBase(gdsBaseAddParam);
            goodsBaseMapper.updateDealerGoodsStock(gdsBaseAddParam);
            // 返回操作成功的结果
            return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
        } catch (Exception e) {
            // 记录异常信息
            logger.error(e.getMessage());
            // 设置当前事务需要回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            // 返回操作失败的结果
            return ResponseResult.success(BizCodeEnume.OPERATION_FAILED);
        }
    }

    /**
     * 为商户新增商品
     *
     * @param record          商品上架记录
     * @param userInfo        用户信息
     * @param supplyGoodsBase 供货商商品信息
     * @param goodsConfig     商品配置
     * @param dealerType      经销商类型
     * @return 操作结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult addDealerGoods(DealerPutawayRecordVO record, UserInfoVO userInfo, GdsBaseDetailResultVO supplyGoodsBase, BaseGoodsConfigVO goodsConfig, Integer dealerType) {
        try {
            // 检查商品编辑权限
            Integer goodsEdit = goodsEditCheck(goodsConfig);
            // 新增goodsBase
            GoodsBaseVO goodsBase = goodsBaseMapper.queryByGoodsId(supplyGoodsBase.getGoodsId());
            // 设置商品所属店铺信息
            goodsBase.setShopId(userInfo.getShopId());
            goodsBase.setShopName(record.getDealerShopName());
            goodsBase.setCommonId(record.getDealerCommonId());
            // 设置供应商信息
            goodsBase.setSupplierShopId(goodsBase.getShopId());
            goodsBase.setSupplierGoodsId(goodsBase.getGoodsId());
            // 设置操作用户信息
            goodsBase.setCreateUser(userInfo.getUserName());
            goodsBase.setUpdateUser(userInfo.getUserName());
            goodsBase.setCreateTime(new Date());
            goodsBase.setUpdateTime(new Date());
            // 设置商品库存
            goodsBase.setGoodsStock(supplyGoodsBase.getGoodsStock());
            // 根据经销商类型设置商品上架状态
            goodsBase.setGoodsIsShelves(dealerType.intValue() == CommonType.dealerType.PUT_AWAY.getCode() ? 1 : 2);
            // 插入新的商品基本信息
            goodsBase.setGoodsId(null);
            goodsBaseMapper.insertDealerGdsBases(goodsBase);
            // 记录商品ID
            record.setDealerGoodsId(goodsBase.getGoodsId());
            // 查询当前商品规格信息
            Map<String, String> commonSpec = goodsCommonMapper.queryCommonSpecByCommonId(record.getDealerCommonId());
            if (null != commonSpec) {
                // 处理规格信息
                String commonSpecValue = commonSpec.get("commonSpecValue");
                String specParam = commonSpec.get("specParam");
                // 解析并更新commonSpecValue
                List<GoodCommonSpecValueJsonBeanVO> goodCommonSpecValueJsonBeans = JSON.parseArray(commonSpecValue, GoodCommonSpecValueJsonBeanVO.class);
                goodCommonSpecValueJsonBeans.add(new GoodCommonSpecValueJsonBeanVO(record.getDealerCommonId(), goodsBase.getGoodsId()));
                commonSpecValue = JSON.toJSONString(goodCommonSpecValueJsonBeans);

                // 处理specParam
                List<GoodSpecValuesJsonBeanVO> goodSpecValues = JSON.parseArray(specParam, GoodSpecValuesJsonBeanVO.class);
                List<GoodBaseSpecValuesJsonBeanVO> goodBaseSpecValuesJsonBeanList = JSON.parseArray(goodsBase.getSpecValue(), GoodBaseSpecValuesJsonBeanVO.class);
                // 合并规格信息
                for (GoodBaseSpecValuesJsonBeanVO goodBaseSpecValuesJsonBean : goodBaseSpecValuesJsonBeanList) {
                    Boolean bool = true;
                    for (GoodSpecValuesJsonBeanVO goodSpecValue : goodSpecValues) {
                        if (goodBaseSpecValuesJsonBean.getParamName().equals(goodSpecValue.getParamName())) {
                            List<String> paramNameValues = goodSpecValue.getParamNameValues();
                            for (int i = 0; i < paramNameValues.size(); i++) {
                                if (!paramNameValues.get(i).equals(goodBaseSpecValuesJsonBean.getParamValue())) {
                                    paramNameValues.add(goodBaseSpecValuesJsonBean.getParamValue());
                                }
                            }
                            goodSpecValue.setParamNameValues(paramNameValues);
                            bool = false;
                        }
                    }
                    if (bool) {
                        GoodSpecValuesJsonBeanVO newGoodsSpec = new GoodSpecValuesJsonBeanVO();
                        newGoodsSpec.setParamName(goodBaseSpecValuesJsonBean.getParamName());
                        List<String> paramNameValues = new ArrayList<>();
                        paramNameValues.add(goodBaseSpecValuesJsonBean.getParamValue());
                        newGoodsSpec.setParamNameValues(paramNameValues);
                        newGoodsSpec.setSort(1);
                        goodSpecValues.add(newGoodsSpec);
                    }
                }
                specParam = JSON.toJSONString(goodSpecValues);
                // 更新商品规格信息
                GdbMerchantGoodsCommonVO goodsCommon = new GdbMerchantGoodsCommonVO(record.getDealerCommonId(), dealerType.intValue() == CommonType.dealerType.PUT_AWAY.getCode() ? 1 : 0, supplyGoodsBase.getGoodsStock(), goodsEdit, specParam, commonSpecValue);
                goodsCommonMapper.updateDealerGoods(goodsCommon);
            }
            // 返回操作成功的结果
            return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
        } catch (Exception e) {
            // 记录异常信息
            logger.error(e.getMessage());
            // 设置当前事务需要回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            // 返回操作失败的结果
            return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
        }
    }

    /**
     * 添加经销商商品信息
     *
     * @param userInfo            用户信息，包含店铺ID等
     * @param supplyGoodsBase     供货商商品基本信息
     * @param shopBaseQueryResult 店铺基础查询结果，包含店铺名称等
     * @param goodsConfig         商品配置信息
     * @param dealerType          经销商类型
     * @return 操作结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult addDealerGoods(UserInfoVO userInfo, GdsBaseDetailResultVO supplyGoodsBase, ManageShopBaseQueryResultVO shopBaseQueryResult, BaseGoodsConfigVO goodsConfig, Integer dealerType) {
        try {
            // 检查商品编辑权限
            Integer goodsEdit = goodsEditCheck(goodsConfig);

            // 通过供货商商品公共ID选择商品公共信息
            GdbMerchantGoodsCommonVO goodsCommon = goodsCommonMapper.selectsupplyGoodsCommonInfo(supplyGoodsBase.getCommonId());

            // 保存原店铺ID
            String commonShopId = goodsCommon.getShopId();

            // 更新商品公共信息中的店铺ID和名称
            goodsCommon.setShopId(userInfo.getShopId());
            goodsCommon.setShopName(shopBaseQueryResult.getShopName());

            // 设置供货商店铺ID
            goodsCommon.setSupplyShopId(commonShopId);

            // 初始化商品收藏、评价和销量
            goodsCommon.setCommonCollect(0);
            goodsCommon.setCommonEvaluate(0);
            goodsCommon.setCommonSalenum(0);

            // 更新商品所在地区ID
            goodsCommon.setDistrictId(shopBaseQueryResult.getDistrictId());

            // 设置商品父ID和配送时间
            goodsCommon.setCommonParentId(goodsCommon.getCommonId());
            goodsCommon.setDeliveryTime(goodsCommon.getDeliveryTime());

            // 处理配送区域信息
            if (goodsCommon.getTransportAreaId() > 0) {
                GdbMerchantTransportAreaVO transportArea = goodsCommonMapper.selectTransportArea(goodsCommon.getTransportAreaId());

                // 重置配送区域ID并关联到当前店铺ID
                transportArea.setId(null);
                transportArea.setShopId(userInfo.getShopId());

                // 插入配送区域信息
                goodsCommonMapper.insertTransportArea(transportArea);

                // 更新商品配送区域ID
                goodsCommon.setTransportAreaId(transportArea.getId());
            }

            // 处理配送模板信息
            if (goodsCommon.getTransportTemplate() > 0) {
                GdbMerchantTransportTemplateVO transportTemplate = goodsCommonMapper.selectTransportTemplate(goodsCommon.getTransportTemplate());

                // 重置配送模板ID并关联到当前店铺ID
                transportTemplate.setId(null);
                transportTemplate.setShopId(userInfo.getShopId());

                // 插入配送模板信息
                goodsCommonMapper.insertTransportTemplate(transportTemplate);

                // 查询并处理配送规则
                List<GdbMerchantTransportRuleVO> transportRules = goodsCommonMapper.selectTransportRule(transportTemplate.getId());
                if (!CollectionUtils.isEmpty(transportRules)) {
                    for (GdbMerchantTransportRuleVO transportRule : transportRules) {
                        // 重置配送规则ID并关联到新的配送模板ID
                        transportRule.setId(null);
                        transportRule.setTransportTemplateId(transportTemplate.getId());
                    }

                    // 插入配送规则
                    goodsCommonMapper.insertTransportRule(transportRules);
                }

                // 更新商品配送模板ID
                goodsCommon.setTransportTemplate(transportTemplate.getId());
            }
            goodsCommon.setCommonUpdateBy(userInfo.getUserId().toString());
            goodsCommon.setOrderOperator(userInfo.getUserName());
            goodsCommon.setCreateUser(userInfo.getUserName());
            goodsCommon.setCommonClick(0);
            goodsCommon.setGoodsType(1);
            goodsCommon.setCommonDistributorFlag(goodsEdit);
            if (dealerType.intValue() == CommonType.dealerType.PUT_AWAY.getCode()) {
                goodsCommon.setDealerGoodsFlag(1);
                goodsCommon.setCommonState(1);
            } else {
                goodsCommon.setDealerGoodsFlag(2);
                goodsCommon.setCommonState(0);
            }
            goodsCommon.setCommonStock(supplyGoodsBase.getGoodsStock());
            goodsCommon.setCommonAddTime(new Date());
            goodsCommon.setCommonSellTime(new Date());
            goodsCommon.setCommonUpdateTime(new Date());
            goodsCommon.setOrderOperatorTime(new Date());
            goodsCommon.setUpdateTime(new Date());
            goodsCommon.setCommonId(null);
            goodsCommon.setCommonVerify(10);
            goodsCommonMapper.insertGoodsCommon(goodsCommon);
            logger.info("添加goodsCommon成功");

            // 添加goodsbase
            GoodsBaseVO goodsBase = goodsBaseMapper.queryByGoodsId(supplyGoodsBase.getGoodsId());
            goodsBase.setGoodsId(null);
            goodsBase.setShopId(userInfo.getShopId());
            goodsBase.setShopName(shopBaseQueryResult.getShopName());
            goodsBase.setCommonId(goodsCommon.getCommonId());
            goodsBase.setSupplierShopId(supplyGoodsBase.getShopId().toString());
            goodsBase.setSupplierGoodsId(supplyGoodsBase.getGoodsId());
            goodsBase.setCreateUser(userInfo.getUserName());
            goodsBase.setUpdateUser(userInfo.getUserName());
            goodsBase.setGoodsStock(supplyGoodsBase.getGoodsStock());
            goodsBase.setCreateTime(new Date());
            goodsBase.setUpdateTime(new Date());
            goodsBase.setGoodsIsShelves(1);
            goodsBaseMapper.insertDealerGdsBases(goodsBase);
            logger.info("添加商品详情成功");

            // 更新规格信息
            List<CommonSpecValueVO> commonSpecValues = Lists.newArrayList();
            CommonSpecValueVO value = new CommonSpecValueVO(goodsBase.getGoodsId(), goodsCommon.getCommonId());
            commonSpecValues.add(value);

            UpdateGdsSpecParamVO updateGdsSpecParam = new UpdateGdsSpecParamVO();
            updateGdsSpecParam.setGoodsId(goodsBase.getGoodsId().toString());
            updateGdsSpecParam.setCommonId(goodsCommon.getCommonId());
            updateGdsSpecParam.setCommonSpecValue(JSON.toJSONString(commonSpecValues));

            List<GoodBaseSpecValuesJsonBeanVO> goodBaseSpecValuesJsonBeanList = JSON.parseArray(goodsBase.getSpecValue(), GoodBaseSpecValuesJsonBeanVO.class);
            List<SpecParamJsonBeanVO> specParamJsonBeanList = new ArrayList<>();
            Map<String, SpecParamJsonBeanVO> specParamMap = new HashMap<>(16);

            for (GoodBaseSpecValuesJsonBeanVO goodsSpecParam : goodBaseSpecValuesJsonBeanList) {
                SpecParamJsonBeanVO specParamJsonBean;
                List<String> paramNameValues;

                if (specParamMap.containsKey(goodsSpecParam.getParamName())) {
                    specParamJsonBean = specParamMap.get(goodsSpecParam.getParamName());
                    paramNameValues = specParamJsonBean.getParamNameValues();
                    paramNameValues.add(goodsBase.getGoodsSpec());
                    specParamJsonBean.setParamNameValues(paramNameValues);
                } else {
                    paramNameValues = new ArrayList<>();
                    paramNameValues.add(goodsBase.getGoodsSpec());
                    specParamJsonBean = new SpecParamJsonBeanVO(goodsSpecParam.getParamName(), goodsSpecParam.getSort(), paramNameValues);
                }
                specParamMap.put(goodsSpecParam.getParamName(), specParamJsonBean);
            }
            for (String specTemp : specParamMap.keySet()) {
                specParamJsonBeanList.add(specParamMap.get(specTemp));
            }

            updateGdsSpecParam.setSpecParam(JSON.toJSONString(specParamJsonBeanList));
            goodsCommonMapper.updateGdsSpec2(updateGdsSpecParam);
            logger.info("更新goodsCommon成功");

            // 添加商品详情
            GdbMerchantGoodsCommonDetailVO commonDetail = goodsCommonDetailMapper.selectByPrimaryKey(supplyGoodsBase.getCommonId());
            if (null != commonDetail) {
                commonDetail.setCommonId(goodsCommon.getCommonId());
                goodsCommonDetailMapper.insertSelective2(commonDetail);
                logger.info("添加商品详情成功");
            }
            // 添加商品图片
            List<GdbMerchantGoodsImagesVO> goodsImages = goodsImagesMapper.selectByCommonId2(supplyGoodsBase.getCommonId());
            if (!CollectionUtils.isEmpty(goodsImages)) {
                for (GdbMerchantGoodsImagesVO goodsImage : goodsImages) {
                    goodsImage.setCommonId(goodsCommon.getCommonId());
                    goodsImage.setGoodsId(goodsBase.getGoodsId());
                    goodsImage.setShopId(userInfo.getShopId());
                }
                goodsImagesMapper.insertBatch2(goodsImages);
                logger.info("添加商品图片成功");
            }
            DealerPutawayRecordVO record = new DealerPutawayRecordVO(goodsBase.getCommonId(), goodsBase.getGoodsId(), goodsBase.getShopId(), goodsBase.getShopName());
            return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL.getMsg(), record);
        } catch (Exception e) {
            logger.error(e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
        }
    }

    /**
     * 商品批量上架
     *
     * @param goodsUserInfo 经销商商品用户信息DTO，包含商品ID列表和用户token等信息
     * @return ResponseResult包含操作结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult goodsPutawayBatch(DealerGoodsUserInfoDTO goodsUserInfo) {
        // 初始化返回结果为成功
        ResponseResult resultResp = ResponseResult.success();
        try {
            // 一键上架商品，根据商品通用ID列表查询商品信息
            List<ManageGdsCommonResultVO> commonResults = goodsCommonMapper.queryCommonInfoList(goodsUserInfo.getCommonIdList());
            // 通过Token解析获取当前用户信息
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(goodsUserInfo.getToken());
            // 根据用户店铺ID查询店铺基本信息
            ManageShopBaseQueryResultVO shopBaseQueryResult = goodsCommonMapper.selectByShopId2(userInfo.getShopId());
            // 遍历查询到的商品信息列表
            for (ManageGdsCommonResultVO supplyGoodsCommon : commonResults) {
                // 根据商品通用ID查询商品配置信息
                BaseGoodsConfigVO goodsConfig = goodsCommonMapper.selectDelayGoodsConfig(supplyGoodsCommon.getCommonId());
                // 调用批量新增商品方法，将商品状态设置为上架
                resultResp = addDealerGoodsBatch(userInfo, supplyGoodsCommon, goodsConfig, shopBaseQueryResult, CommonType.supplierGoodsStatus.PUTAWAY.getCode());
            }
            // 返回成功结果
            return resultResp;
        } catch (Exception e) {
            // 捕获异常，设置当前事务需要回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            // 返回失败结果，系统错误
            return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(), BizCodeEnume.SYSTEM_ERROR.getMsg());
        }
    }

    /**
     * 批量添加经销商商品
     *
     * @param userInfo            用户信息，包含店铺ID和用户名等
     * @param supplyGoodsCommon   供货商商品基本信息
     * @param goodsConfig         商品基础配置信息
     * @param shopBaseQueryResult 店铺基础查询结果信息
     * @param dealerType          经销商类型
     * @return 返回操作结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult addDealerGoodsBatch(UserInfoVO userInfo, ManageGdsCommonResultVO supplyGoodsCommon, BaseGoodsConfigVO goodsConfig, ManageShopBaseQueryResultVO shopBaseQueryResult, Integer dealerType) {
        try {
            // 查询是否存在其他规格商品的经销记录
            DealerPutawayRecordVO putawayRecord = new DealerPutawayRecordVO(supplyGoodsCommon.getCommonId(), null, userInfo.getShopId(), supplyGoodsCommon.getShopId(), CommonType.putawayState.NORMAL.getCode(), dealerType);
            putawayRecord = baseMapper.queryPutawayRecord(putawayRecord);
            if (null != putawayRecord) {
                return ResponseResult.fail(BizCodeEnume.GOODS_DEALER);
            }

            // 检查商品编辑权限
            Integer goodsEdit = goodsEditCheck(goodsConfig);
            // 查询供货商商品基本信息
            GdbMerchantGoodsCommonVO goodsCommon = goodsCommonMapper.selectsupplyGoodsCommonInfo(supplyGoodsCommon.getCommonId());
            // 保留供货商店铺ID
            String commonShopId = goodsCommon.getShopId();
            goodsCommon.setSupplyShopId(commonShopId);
            // 更新商品所属店铺信息
            goodsCommon.setShopId(userInfo.getShopId());
            goodsCommon.setShopName(shopBaseQueryResult.getShopName());
            // 初始化商品统计信息
            goodsCommon.setCommonCollect(0);
            goodsCommon.setCommonEvaluate(0);
            goodsCommon.setCommonSalenum(0);
            // 设置地区信息
            goodsCommon.setDistrictId(shopBaseQueryResult.getDistrictId());
            // 初始化商品父ID
            goodsCommon.setCommonParentId(goodsCommon.getCommonId());
            // 设置更新者和操作员信息
            goodsCommon.setCommonUpdateBy(userInfo.getUserId().toString());
            goodsCommon.setOrderOperator(userInfo.getUserName());
            goodsCommon.setCreateUser(userInfo.getUserName());
            // 初始化点击量和商品类型
            goodsCommon.setCommonClick(0);
            goodsCommon.setGoodsType(1);
            // 根据编辑权限设置分销标志
            goodsCommon.setCommonDistributorFlag(goodsEdit);
            // 根据经销商类型设置商品状态
            if (dealerType.intValue() == CommonType.supplierGoodsStatus.PUTAWAY.getCode()) {
                goodsCommon.setDealerGoodsFlag(1);
                goodsCommon.setCommonState(1);
            } else {
                goodsCommon.setDealerGoodsFlag(2);
                goodsCommon.setCommonState(0);
            }
            // 保留供货商库存信息
            goodsCommon.setCommonStock(supplyGoodsCommon.getCommonStock());
            // 设置时间信息
            goodsCommon.setCommonAddTime(new Date());
            goodsCommon.setCommonSellTime(new Date());
            goodsCommon.setCommonUpdateTime(new Date());
            goodsCommon.setOrderOperatorTime(new Date());
            goodsCommon.setUpdateTime(new Date());
            // 重置商品ID和审核状态
            goodsCommon.setCommonId(null);
            goodsCommon.setCommonVerify(10);
            // 插入商品基本信息
            goodsCommonMapper.insertGoodsCommon(goodsCommon);

            // 添加商品基础信息
            List<GoodsBaseVO> goodsBaseList = goodsBaseMapper.queryGoodsCommonId(supplyGoodsCommon.getCommonId(), null);
            Integer supplyGoodsId;
            for (GoodsBaseVO goodsBase : goodsBaseList) {
                supplyGoodsId = goodsBase.getGoodsId();
                goodsBase.setGoodsId(null);
                goodsBase.setSupplierShopId(commonShopId);
                goodsBase.setSupplierGoodsId(supplyGoodsId);
                goodsBase.setShopId(userInfo.getShopId());
                goodsBase.setShopName(shopBaseQueryResult.getShopName());
                goodsBase.setCommonId(goodsCommon.getCommonId());
                goodsBase.setCreateUser(userInfo.getUserName());
                goodsBase.setUpdateUser(userInfo.getUserName());
                goodsBase.setGoodsStock(goodsBase.getGoodsStock());
                goodsBase.setCreateTime(new Date());
                goodsBase.setUpdateTime(new Date());
                // 根据经销商类型设置上架状态
                if (dealerType.intValue() == CommonType.supplierGoodsStatus.PUTAWAY.getCode()) {
                    goodsBase.setGoodsIsShelves(1);
                } else {
                    goodsBase.setGoodsIsShelves(2);
                }
                goodsBaseMapper.insertGoodsBase(goodsBase);
                Integer goodsId = baseMapper.getByGoodsId();
                //将数据更新到商品库存表
                GoodsStockEntity goodsStockEntity = new GoodsStockEntity();
                goodsStockEntity.setGoodsId(goodsId);
                goodsStockEntity.setCommonId(goodsBase.getCommonId());
                goodsStockEntity.setGoodsStock(goodsBase.getGoodsStock());
                goodsStockEntity.setGoodsAlarm(goodsBase.getGoodsAlarm());
                goodsStockEntity.setCreateTime(new Date());
                goodsStockEntity.setCreateUser(userInfo.getUserId());
                goodsStockService.save(goodsStockEntity);
                // 添加商品图片信息
                List<GdbMerchantGoodsImagesVO> goodsImages = goodsImagesMapper.selectByCommonIdImg(supplyGoodsId);
                if (!CollectionUtils.isEmpty(goodsImages)) {
                    for (GdbMerchantGoodsImagesVO goodsImage : goodsImages) {
                        goodsImage.setCommonId(goodsCommon.getCommonId());
                        goodsImage.setGoodsId(goodsId);
                        goodsImage.setShopId(userInfo.getShopId());
                    }
                    goodsImagesMapper.insertBatch2(goodsImages);
                }
            }

            // 获取最新商品基础信息
            goodsBaseList = goodsBaseMapper.queryGoodsCommonId(goodsCommon.getCommonId(), null);
            List<CommonSpecValueVO> commonSpecValues = Lists.newArrayList();
            for (GoodsBaseVO goodsBase : goodsBaseList) {
                CommonSpecValueVO value = new CommonSpecValueVO(goodsBase.getGoodsId(), goodsCommon.getCommonId());
                commonSpecValues.add(value);
            }

            // 更新商品规格信息
            UpdateGdsSpecParamVO updateGdsSpecParam = new UpdateGdsSpecParamVO();
            updateGdsSpecParam.setGoodsId(goodsBaseList.get(0).getGoodsId().toString());
            updateGdsSpecParam.setCommonId(goodsCommon.getCommonId());
            updateGdsSpecParam.setCommonSpecValue(JSON.toJSONString(commonSpecValues));
            goodsCommonMapper.updateGdsSpec2(updateGdsSpecParam);

            // 添加商品详情信息
            GdbMerchantGoodsCommonDetailVO commonDetail = goodsCommonDetailMapper.selectByPrimaryKey(supplyGoodsCommon.getCommonId());
            if (null != commonDetail) {
                commonDetail.setCommonId(goodsCommon.getCommonId());
                goodsCommonDetailMapper.insertSelective2(commonDetail);
            }

            // 记录上架操作
            DealerPutawayRecordVO record = new DealerPutawayRecordVO(supplyGoodsCommon, goodsCommon, userInfo, shopBaseQueryResult);
            record.setIsPriceUpdate(2);
            goodsCommonMapper.insertputawayRecord(record);
            return ResponseResult.success();
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 商品内容更新校验
     *
     * @param goodsConfig
     * @return
     */
    private Integer goodsEditCheck(BaseGoodsConfigVO goodsConfig) {
        Integer goodsEdit;
        if (goodsConfig.getSellEdit().intValue() == 2) {
            goodsEdit = 2;
        } else {
            if (goodsConfig.getSellEditPrice().intValue() == 1) {
                goodsEdit = 0;
            } else {
                goodsEdit = 1;
            }
        }
        return goodsEdit;
    }

    /**
     * 查询优惠券列表
     *
     * @param param 查询参数，包含用户信息和优惠券相关查询条件
     * @return 包含优惠券详情的响应结果
     */
    @Override
    public ResponseResult queryCouponList(UserVcBaseQueryDTO param) {
        // 查询优惠券限定的商品类别列表
        List<Integer> goodsCatLimitList = goodsCommonMapper.queryCouponList(param);

        List<WapCommonBaseUserFootVO> goodsList = null;
        // 如果商品类别列表不为空
        if (!CollectionUtils.isEmpty(goodsCatLimitList)) {
            // 根据商品类别列表查询商品详情
            goodsList = goodsCommonMapper.selectByCommonIds4(goodsCatLimitList);
        }
        // 如果商品列表为空，初始化为空列表
        if (CollectionUtils.isEmpty(goodsList)) {
            goodsList = Lists.newArrayList();
        }
        // 处理活动价格
        for (WapCommonBaseUserFootVO gdsListQueryResult : goodsList) {
            Integer commonIsXian = gdsListQueryResult.getCommonIsXian();
            // 获取商品的活动详情
            ActivityGoodsDetailDTO activityGoodsDetail = dealActivity(
                    Integer.parseInt(gdsListQueryResult.getGoodsId()), gdsListQueryResult.getCommonId(), commonIsXian);

            // 如果活动详情存在且活动状态不为0
            if (null != activityGoodsDetail && !activityGoodsDetail.equals("")
                    && activityGoodsDetail.getActivityStatus() != 0) {
                // 设置商品的活动状态、活动详情、是否限时和活动价格
                gdsListQueryResult.setActivityStatus(activityGoodsDetail.getActivityStatus());
                gdsListQueryResult.setActivityGoodsDetail(activityGoodsDetail);
                gdsListQueryResult.setCommonIsXian(activityGoodsDetail.getActivityStatus());
                gdsListQueryResult.setCommonPrice(activityGoodsDetail.getActivityPrice());
                // 如果活动状态为5，设置商品价格为秒杀折后价格
                if (activityGoodsDetail.getActivityStatus() == 5) {
                    gdsListQueryResult.setCommonPrice(activityGoodsDetail.getMjzzPrice());
                }
            } else {
                // 如果没有活动或者活动状态为0，初始化相关字段
                gdsListQueryResult.setActivityStatus(0);
                gdsListQueryResult.setActivityGoodsDetail(new ActivityGoodsDetailDTO());
                gdsListQueryResult.setCommonIsXian(0);
            }
        }
        // 返回商品列表的响应结果
        return ResponseResult.success(goodsList);
    }

    /**
     * 查询红包列表
     *
     * @param param 查询参数，包括用户信息和红包模板ID等
     * @return 包含红包详情的响应结果
     */
    @Override
    public ResponseResult queryRedPacketList(UserAvailableRedpacketQueryDTO param) {
        // 根据参数查询红包模板详情
        HouRedpacketTemplateVO redpacketTemplate = goodsCommonMapper.selectByRedpacketId(param);

        List<WapHallGoodsQueryVO> goodsList = null;
        // 检查模板是否存在且为单个红包类型
        if (null != redpacketTemplate && redpacketTemplate.getManyRedpacketType().equals(1)) {
            String goodsCatLimit = redpacketTemplate.getGoodsCatLimit();
            // 如果红包限定的商品类别不为空
            if (null != goodsCatLimit && !goodsCatLimit.equals("")) {
                // 将商品类别字符串转换为列表
                List<Integer> goodsCatLimitList = JsonUtils.StringToList2(goodsCatLimit);

                // 根据商品类别列表查询商品详情
                goodsList = goodsBaseMapper.selectByGoodsIds(goodsCatLimitList);
            }
        }
        // 如果商品列表为空，初始化为空列表
        if (CollectionUtils.isEmpty(goodsList)) {
            goodsList = Lists.newArrayList();
        }
        // 处理活动价格
        for (WapHallGoodsQueryVO gdsListQueryResult : goodsList) {
            Integer commonIsXian = gdsListQueryResult.getCommonIsXian();
            // 获取商品的活动详情
            ActivityGoodsDetailDTO activityGoodsDetail = dealActivity(gdsListQueryResult.getGoodsId(),
                    gdsListQueryResult.getCommonId(), commonIsXian);

            // 如果活动详情存在且活动状态不为0
            if (null != activityGoodsDetail && activityGoodsDetail.getActivityStatus() != 0) {
                // 设置商品的活动状态、活动详情、是否限时和活动价格
                gdsListQueryResult.setActivityStatus(activityGoodsDetail.getActivityStatus());
                gdsListQueryResult.setActivityGoodsDetail(activityGoodsDetail);
                gdsListQueryResult.setCommonIsXian(activityGoodsDetail.getActivityStatus());
                gdsListQueryResult.setCommonPrice(activityGoodsDetail.getActivityPrice());
                // 如果活动状态为5，设置商品价格为秒杀折后价格
                if (activityGoodsDetail.getActivityStatus() == 5) {
                    gdsListQueryResult.setCommonPrice(activityGoodsDetail.getMjzzPrice());
                }
            } else {
                // 如果没有活动或者活动状态为0，初始化相关字段
                gdsListQueryResult.setActivityStatus(0);
                gdsListQueryResult.setActivityGoodsDetail(new ActivityGoodsDetailDTO());
                gdsListQueryResult.setCommonIsXian(0);
            }
        }
        // 返回商品列表的响应结果
        return ResponseResult.success(goodsList);
    }

    /**
     * 分页查询积分商品
     *
     * @param param 分页查询参数，包括分类ID等
     * @return 包含积分商品分页结果的响应结果
     */
    @Override
    public ResponseResult queryIntegralAll(ListPageQueryParamDTO param) {
        try {
            // 如果分类ID不为空，获取其所有下级分类ID
            if (null != param.getCatId()) {
                GoodsCatSearchChildVO goodsCatSearchChild = null;
                List<WebCatMenuVO> allMenu = null;
                List<Integer> catIds = null;
                // 从缓存中获取分类ID列表
                String cacheCatIds = RedisUtil.get(Constants.CACHE_GOODS_WAP_GOODS_LIST_PREFIX + "_" + param.getCatId());

                // 如果缓存存在
                if (null != cacheCatIds && !cacheCatIds.equals("")) {
                    goodsCatSearchChild = JsonUtils.string2Obj(cacheCatIds, GoodsCatSearchChildVO.class);
                    catIds = goodsCatSearchChild.getCatIds();
                } else {
                    // 查询所有下级分类
                    allMenu = goodsCatMapper.selectAllCat();
                    catIds = getMenuList(allMenu, param.getCatId());

                    goodsCatSearchChild = new GoodsCatSearchChildVO();
                    goodsCatSearchChild.setAllMenu(allMenu);
                    goodsCatSearchChild.setCatIds(catIds);
                    // 将分类ID列表存入缓存
                    RedisUtil.set(Constants.CACHE_GOODS_WAP_GOODS_LIST_PREFIX + "_" + param.getCatId(),
                            JsonUtils.toString(goodsCatSearchChild), RedisUtil.EXRP_MONTH);
                }
                // 将当前分类ID和下级分类ID列表添加到查询参数中
                if (!CollectionUtils.isEmpty(catIds)) {
                    catIds.add(param.getCatId());
                    param.setCatIds(catIds);
                }
            }
            // 将查询参数对象转换为Map
            Map data = ParseMapUtils.beanToMap(param);
            // 执行分页查询积分商品
            IPage<IntegralGoodsResultVO> page = baseMapper.queryIntegralAll(new Query<IntegralGoodsResultVO>().getPage(data), param);
            // 返回分页查询结果的响应
            return ResponseResult.success(PageUtils.getPage(page));
        } catch (Exception e) {
            // 异常处理
            e.printStackTrace();
        }
        // 查询失败，返回系统错误的响应
        return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(), BizCodeEnume.SYSTEM_ERROR.getMsg());
    }

    @Override
    public ResponseResult platformLockUpdate(PlatFormGoodsStateDTO param) {
        // 通过令牌解析平台用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        // 确保用户信息非空
        if (null != userInfo) {
            // 初始化用于传递给消息队列的数据映射
            Map<String, Object> map = new HashMap<>();
            param.setCommonIdList(param.getCommonList().stream().map(CommonIdandTypesDTO::getCommonId).collect(Collectors.toList()));
            // 初始化商品ID列表
            List<Integer> list = new ArrayList<>();
            // 更新商品锁定状态
            baseMapper.updateGoodsLock(param);
            // 根据是否屏蔽商品设置操作类型
            if (null != param.getCommonIsScreen() && CommonType.commonSpuState.GOODS_LOCK.getCode().equals(param.getCommonIsScreen())) {
                // 设置为删除操作类型（屏蔽）
                map.put("type", CommonType.operateStatus.DEL.getCode());
            } else {
                // 设置为添加操作类型（不屏蔽）
                map.put("type", CommonType.operateStatus.ADD.getCode());
            }
            // 获取并设置商品ID列表
            list = param.getCommonIdList();
            map.put("commonIdS", list);
            // 通过消息队列更新ES中的商品数据
            MqSendUtils.mqSendGoods(map);
            return ResponseResult.success();
        }
        // 令牌非法时返回错误结果
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    @Override
    public ResponseResult platformScreenUpdate(PlatFormGoodsStateDTO param) {
        // 通过令牌解析平台用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        // 确保用户信息非空
        if (null != userInfo) {
            // 初始化用于传递给消息队列的数据映射
            Map<String, Object> map = new HashMap<>();
            param.setCommonIdList(param.getCommonList().stream().map(CommonIdandTypesDTO::getCommonId).collect(Collectors.toList()));
            // 更新商品屏蔽状态
            baseMapper.updateGoodsScreen(param);
            // 根据是否屏蔽商品设置操作类型
            if (null != param.getCommonIsScreen() && CommonType.commonSpuState.GOODS_SHIELD.getCode().equals(param.getCommonIsScreen())) {
                // 设置为删除操作类型（屏蔽）
                map.put("type", CommonType.operateStatus.DEL.getCode());
            } else {
                // 设置为添加操作类型（不屏蔽）
                map.put("type", CommonType.operateStatus.ADD.getCode());
            }
            // 排除称重商品后获取商品ID列表
            List<Integer> list = goodsCommonMapper.selectList(new LambdaQueryWrapper<GoodsCommonEntity>()
                            .in(GoodsCommonEntity::getCommonId, param.getCommonIdList())
                            .ne(GoodsCommonEntity::getVirtualGoods, 3))
                    .stream().map(GoodsCommonEntity::getCommonId).collect(Collectors.toList());
            // 设置商品ID列表到消息队列数据映射
            map.put("commonIdS", list);
            // 通过消息队列更新ES中的商品数据
            MqSendUtils.mqSendGoods(map);
            return ResponseResult.success();
        }
        // 令牌非法时返回错误结果
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    @Override
    public ResponseResult platformViolationsUpdate(PlatFormGoodsStateDTO param) {
        // 通过令牌解析平台用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        // 初始化用于传递给消息队列的数据映射
        Map<String, Object> map = new HashMap<>();
        if (null != userInfo) {
            // 设置操作人姓名
            param.setOrderOperator(userInfo.getUserRealname());
            // 设置操作时间格式化器
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // 操作时间
            String finishTime = dateFormat.format(new Date());
            param.setOrderOperatorTime(finishTime);
            // 审核时间
            param.setCommonVerifyTime(new Date());
            // 更新商品审核状态
            List<CommonIdandTypesDTO> commonlist = param.getCommonList() != null ? param.getCommonList() : new ArrayList<>();
            List<Integer> commonIdList = new ArrayList<>();
            List<Integer> supplierCommonIdList = new ArrayList<>();
            for (CommonIdandTypesDTO commonInfo : commonlist) {
                //修改审核状态
                if (commonInfo.getGoodsType().equals(1) || commonInfo.getGoodsType().equals(3)) {
                    commonIdList.add(commonInfo.getCommonId());
                } else {
                    supplierCommonIdList.add(commonInfo.getCommonId());
                }
            }
            param.setCommonIdList(commonIdList);
            param.setSupplierCommonIdList(supplierCommonIdList);
            //普通商品
            if (!commonIdList.isEmpty()) {
                this.baseMapper.updateGoodsState(param);
            }

            //供应商商品
            if (!supplierCommonIdList.isEmpty()) {
                this.baseMapper.updateSupplierGoodsState(param);
            }
            // 通过消息队列更新ES中的商品数据
            map.put("commonIdS", commonIdList);
            // 根据商品状态进行不同操作
            if (null != param.getCommonState()) {
                // 上架操作
                if (param.getCommonState().equals(CommonType.goodsIsShelves.GOODS_ON_THE_SHELF.getCode())) {
                    // 添加操作类型
                    map.put("type", CommonType.operateStatus.ADD.getCode());
                    if (!commonIdList.isEmpty()) {
                        goodsBaseMapper.updateGoodsBaseStateList(param);
                    }
                    if (!supplierCommonIdList.isEmpty()) {
                        goodsBaseMapper.updateSpplierGoodsBaseStateList(param);
                    }
                }
                // 下架操作
                if (param.getCommonState().equals(CommonType.goodsIsShelves.GOODS_OFF_THE_SHELF.getCode())) {
                    // 删除操作类型
                    map.put("type", CommonType.operateStatus.DEL.getCode());
                    // 设置商品状态为下架
                    param.setCommonState(CommonType.commonSpuState.GOODS_OFF_SHELF.getCode());
                    if (!commonIdList.isEmpty()) {
                        goodsBaseMapper.updateGoodsBaseStateList(param);
                    }
                    if (!supplierCommonIdList.isEmpty()) {
                        SupplierGoodsIsShelvesDTO dto = new SupplierGoodsIsShelvesDTO();
                        dto.setToken(param.getToken());
                        dto.setCommonIds(supplierCommonIdList);
                        supplierServiceClient.setSkuOfflineByPlatform(dto);
                    }
                }
                // 禁售操作
                if (param.getCommonState().equals(CommonType.commonSpuState.GOODS_ILLEGAL.getCode())) {
                    // 删除操作类型
                    map.put("type", CommonType.operateStatus.DEL.getCode());
                    if (!supplierCommonIdList.isEmpty()) {
                        SupplierGoodsIsShelvesDTO dto = new SupplierGoodsIsShelvesDTO();
                        dto.setCommonIds(supplierCommonIdList);
                        supplierServiceClient.setViolationByPlatform(dto);
                    }
                }
            }
            // 审核操作
            if (null != param.getCommonVerify()) {
                // 审核通过
                if (1 == param.getCommonVerify()) {
                    // 添加操作类型
                    map.put("type", CommonType.operateStatus.ADD.getCode());
                    //供应商商品
                    if (!supplierCommonIdList.isEmpty()) {
                        updateSupplierGoods(supplierCommonIdList);
                    }
                    //普通商品
                    if (!commonIdList.isEmpty()) {
                        asyncVersionService.addGoodsVersionToList(commonIdList, param.getToken());
                    }
                    //供应商商品
                    if (!supplierCommonIdList.isEmpty()) {
                        SupplierGoodsIsShelvesDTO dto = new SupplierGoodsIsShelvesDTO();
                        dto.setCommonIds(supplierCommonIdList);
                        dto.setToken(param.getToken());
                        supplierServiceClient.addGoodsVersionToList(dto);
                    }
                // 审核未通过
                } else {
                    // 删除操作类型
                    map.put("type", CommonType.operateStatus.DEL.getCode());
                }
            }
            if (!commonIdList.isEmpty()) {
                // 发送消息队列消息
                MqSendUtils.mqSendGoods(map);
            }

            return ResponseResult.success();
        }
        // 令牌非法时返回错误结果
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 审核通过后 如果是供应商商品需要将供应商商品数据同步更新到商家
     */
    public void updateSupplierGoods(List<Integer> supplierCommonIdList) {
        for (Integer supplierCommonId : supplierCommonIdList) {
            // 查询供应商商品CommonBase信息
            GoodsCommonEntity merchantGoodsCommon = this.baseMapper.querySupplierCommonBaseInfo(supplierCommonId);
            // 是代发商品 审核供应商商品是 才会同步商品信息到 经销商
            if (CommonType.supplierDealerPutawayStart.DROPSHIPPING.getCode().equals(merchantGoodsCommon.getIssuedOnBehalf())) {
                // 查询供应商商品GoodsBase信息
                List<GoodsBaseEntity> merchantGoodsBaseEntities = goodsBaseMapper.querySupplierGoodsBaseInfo(supplierCommonId);
                // 查询供应商商品详情
                String commonBody = goodsBaseMapper.querySupplierCommonBodyInfo(supplierCommonId);
                // 查询供应商goodsImages 信息
                // 查询是否有商家代发当前商品
                List<SupplierDealerPutawayRecordVO> supplierDealerPutawayRecordVOList = this.baseMapper.queryDealerCommonIdList(supplierCommonId);
                if (null != supplierDealerPutawayRecordVOList && !supplierDealerPutawayRecordVOList.isEmpty()) {
                    // 更新 goods_common
                    // 使用Stream 获取commonId
                    Set<Integer> dealerCommonIdList = supplierDealerPutawayRecordVOList.stream()
                            .map(SupplierDealerPutawayRecordVO::getDealerCommonId)
                            .distinct()
                            .collect(Collectors.toSet());
                    this.baseMapper.updateDealerGoodsCommon(dealerCommonIdList, merchantGoodsCommon);
                    // 更新 goods_common_detail
                    this.baseMapper.updateDealerCommonBody(dealerCommonIdList, commonBody);

                    // 更新 goods_base
                    for (SupplierDealerPutawayRecordVO supplierDealerPutawayRecordVO : supplierDealerPutawayRecordVOList) {
                        for (GoodsBaseEntity GoodsBaseEntity : merchantGoodsBaseEntities) {
                            if (supplierDealerPutawayRecordVO.getSupplyGoodsId().equals(GoodsBaseEntity.getGoodsId())) {
                                // 更新 goods_base
                                goodsBaseMapper.updateDealerGoods(supplierDealerPutawayRecordVO.getDealerGoodsId(), GoodsBaseEntity);
                                // 查询供应商商品图片信息
                                List<String> suuplierGoodsImage = goodsImagesMapper.querySupplierGoodsImage(GoodsBaseEntity.getGoodsId());
                                // 查询经销商品图片信息
                                List<GoodsImagesEntity> dealerGoodsImage = goodsImagesMapper.queryDealerGoodsImage(supplierDealerPutawayRecordVO.getDealerGoodsId());
                                // 更新 merchant_goods_images
                                if (null != suuplierGoodsImage && !suuplierGoodsImage.isEmpty()
                                        && null != dealerGoodsImage && !dealerGoodsImage.isEmpty()
                                ) {
                                    Integer suuplierGoodsSize = suuplierGoodsImage.size();
                                    Integer imageIndex = 0;
                                    for (GoodsImagesEntity GoodsImagesEntity : dealerGoodsImage) {
                                        if (imageIndex < suuplierGoodsSize) {
                                            GoodsImagesEntity.setImagesImage(suuplierGoodsImage.get(imageIndex));
                                        } else {
                                            GoodsImagesEntity.setImagesImage(suuplierGoodsImage.get(suuplierGoodsSize - 1));
                                        }
                                        goodsImagesMapper.updateDealerGoodsImage(GoodsImagesEntity);
                                        imageIndex++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public ResponseResult platformQueryDetail(ManageGdsNameQueryParamDTO param) {
        // 通过令牌解析平台用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        if (userInfo != null) {
            // 获取商品公共ID
            Integer commonId = param.getCommonId();
            if (commonId == null) {
                // 如果公共ID为空，返回业务异常
                return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION);
            }
            // 根据公共ID查询商品公共信息
            ManageGdsCommonVO common = baseMapper.selectByCommonIdInfo(commonId);
            if (common == null) {
                // 如果未找到公共信息，返回业务异常
                return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION);
            }

            // 查询商品的所有规格
            List<ManageGdsBaseAddResultVO> goodsBaseList = goodsBaseMapper.selectByCommonId2(commonId);
            String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.STORAGE_SWITCH.getDesc());
            if (!StringUtils.isEmpty(value) && "1".equals(value)) {
                Integer storageGoodsStock = storageGoodsBaseMapper.getAllSpecGoodsStorageStock(commonId);
                common.setStorageGoodsStock(storageGoodsStock);
            } else {
                common.setStorageGoodsStock(0);
            }

            // 查询并设置运费模板和售卖区域信息
            ResponseResult transportTemplate = merchantServiceClient.selectTemplateById(common.getTransportTemplate());
            ManageTransportTemplateVO transportTemplates = JSON.parseObject(JSON.toJSONString(transportTemplate.getData()), ManageTransportTemplateVO.class);

            ResponseResult areas = merchantServiceClient.selectAreaById(common.getTransportAreaId());
            ManageTransportAreaVO transportAreas = JSON.parseObject(JSON.toJSONString(areas.getData()), ManageTransportAreaVO.class);
            common.setTransportTemplates(transportTemplates);
            common.setTransportAreas(transportAreas);

            // 处理店铺自定义分类
            List<ManageGoodsSelfCatVO> shopSelfCats = null;
            if (common.getShopCatId() != null && !common.getShopCatId().equals("")) {
                shopSelfCats = baseMapper.selectByShopCatId(JsonUtils.StringToList(common.getShopCatId()));
            }
            common.setShopSelfCat(shopSelfCats);

            // 查询商品详细介绍和图片组
            GoodsCommonDetailEntity goodsCommonDetail = goodsCommonDetailMapper.selectById(commonId);
            List<GoodsImagesEntity> goodsImages = goodsImagesMapper.selectList(
                    new LambdaQueryWrapper<GoodsImagesEntity>()
                            .eq(GoodsImagesEntity::getCommonId, commonId)
                            .eq(GoodsImagesEntity::getShopId, param.getShopId())
            );

            // 将查询到的所有信息封装到Map中并返回成功结果
            Map<String, Object> map = new HashMap<>();
            map.put("common", common);
            map.put("goodsBaseList", goodsBaseList);
            map.put("goodsCommonDetail", goodsCommonDetail);
            map.put("goodsImages", goodsImages);
            return ResponseResult.success(map);
        }
        // 如果令牌非法，返回业务异常
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    @Override
    public ResponseResult queryGateCommonsList(GateQueryCommonsDTO param) {
        // 通过token解析获取用户信息
        try {
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
            // 如果用户信息为空，返回权限不足错误
            if (userInfo == null) {
                return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
            }

            // 设置店铺ID
            param.setShopId(Integer.parseInt(userInfo.getShopId()));
            // 初始化分页对象
            Page<GateQueryCommonsVO> page = new Page<>(param.getPage(), param.getLimit());
            // 查询闸机通用列表
            IPage<GateQueryCommonsVO> commonsResults = baseMapper.queryGateCommonsList(page, param);

            // 遍历结果，计算每个商品的库存总和
            commonsResults.getRecords().forEach(commomResult -> {
                Integer shopStock = 0;
                // 查询闸机商品基础信息列表
                List<GateGoodsBaseVO> GateGoodsBases = baseMapper.queryGateGoodsBasePoList(userInfo.getShopGateId(), commomResult.getCommonId());
                if (null != GateGoodsBases) {
                    // 收集所有商品的库存数量
                    List<Integer> integerList = GateGoodsBases.stream().map(GateGoodsBaseVO::getShopGateGoodsStock).collect(Collectors.toList());
                    // 计算库存总和
                    shopStock = integerList.stream().reduce(Integer::sum).orElse(0);
                }
                // 设置计算得到的库存总和
                commomResult.setShopGateGoodsStock(shopStock);
            });
            // 返回成功结果，包含分页信息
            return ResponseResult.success(PageUtils.getPage(commonsResults));
        } catch (Exception e) {
            // 异常处理，返回服务器错误
            return ResponseResult.fail(500, e.getMessage());
        }
    }

    /**
     * 查询商品规格列表
     *
     * @param param 查询参数，包括商品ID和分页信息等
     * @return 包含商品规格列表的响应结果
     */
    @Override
    public ResponseResult queryGoodsSpecList(ManageCommonIdsDTO param) {
        // 检查令牌有效性
        String userInfoStr = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + param.getToken());

        if (null == userInfoStr) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        try {
            // 初始化商品规格列表
            List<ManageGoodsBaseListVO> goodsBases = new ArrayList<ManageGoodsBaseListVO>();
            // 用于封装商品信息的临时对象
            GateGoodsBaseVO gdbGateGoodsBasePo = null;
            // 解析用户信息
            UserInfoDTO dto = JSON.parseObject(userInfoStr, UserInfoDTO.class);
            UserInfoVO userInfo = dto.getUser();
            // 查询商品规格列表
            goodsBases = baseMapper.queryGoodsSpecList(param.getCommonId());
            // 遍历商品规格列表，设置库存信息
            for (ManageGoodsBaseListVO goodsBase : goodsBases) {
                gdbGateGoodsBasePo = new GateGoodsBaseVO();
                gdbGateGoodsBasePo.setCommonId(goodsBase.getCommonId());
                gdbGateGoodsBasePo.setGoodsId(goodsBase.getGoodsId());
                gdbGateGoodsBasePo.setShopGateId(Integer.valueOf(userInfo.getShopId()));
                goodsBase.setGoodsSpec(goodsBase.getGoodsSpec());
                // 查询并设置当前商品在门店的库存
                Integer shopGateGoodsStock = baseMapper.queryGoodsGateStock(goodsBase.getGoodsId(), userInfo.getShopGateId());
                goodsBase.setShopGateGoodsStock(shopGateGoodsStock != null ? shopGateGoodsStock : 0);
            }
            return ResponseResult.success(goodsBases);
        } catch (Exception e) {
            return ResponseResult.fail(500, e.getMessage());
        }
    }

    /**
     * 更新门店商品库存
     *
     * @param param 包含门店商品库存更新信息的参数对象
     * @return 包含更新结果的响应对象
     */
    @Override
    @Transactional
    public ResponseResult updateGateShopStock(GateUpShopGoodsStockDTO param) {
        // 检查令牌有效性
        String userInfoStr = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + param.getToken());
        if (null == userInfoStr) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 检查参数有效性
        if (param.getCommonId() == null) {
            return ResponseResult.fail(2, "commonId不能为null");
        }
        if (param.getGoodsId() == null) {
            return ResponseResult.fail(2, "goodsId不能为空");
        }
        GateGoodsBaseVO gdbGateGoodsBasePo = new GateGoodsBaseVO();
        Integer integer = null;
        // 解析用户信息
        UserInfoDTO dto = JSON.parseObject(userInfoStr, UserInfoDTO.class);
        UserInfoVO userInfo = dto.getUser();
        // 设置商品门店库存信息
        gdbGateGoodsBasePo.setShopId(Integer.valueOf(userInfo.getShopId()));
        gdbGateGoodsBasePo.setShopGateId(Integer.valueOf(userInfo.getShopGateId()));
        gdbGateGoodsBasePo.setGoodsId(param.getGoodsId());
        gdbGateGoodsBasePo.setCommonId(param.getCommonId());
        gdbGateGoodsBasePo.setShopGateGoodsStock(param.getGoodsShopGateStock());
        // 检查是否已设置过门店库存
        Integer count = baseMapper.queryGateGoodsCount(gdbGateGoodsBasePo);
        if (count > 0) {
            integer = this.baseMapper.updateGateGoodsBase(gdbGateGoodsBasePo);
            if (integer > 0) {
                return ResponseResult.success("库存设置成功", integer);
            }
        } else {
            // 未设置过库存，直接插入新的库存信息
            integer = this.baseMapper.insertGateGoodsBase(gdbGateGoodsBasePo);
            if (integer > 0) {
                return ResponseResult.success("库存设置成功", integer);
            }

        }
        return ResponseResult.fail();
    }

    /**
     * 检查经销商是否有权经销指定商品
     *
     * @param dto 经销商商品用户信息DTO，包含商品ID、令牌等信息
     * @return 响应结果，包含成功或失败及原因
     */
    @Override
    public ResponseResult checkDealer(DealerGoodsUserInfoDTO dto) {
        // 通过令牌获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(dto.getToken());
        // 获取店铺ID
        String shopId = userInfo.getShopId();
        // 查询要经销的商品
        GdsBaseDetailResultVO supplyGoodsBase = null;
        if (null == dto.getCommonId()) {
            supplyGoodsBase = goodsBaseMapper.selectByGoodsId(dto.getGoodsId());
            dto.setCommonId(supplyGoodsBase.getCommonId());
        }
        // 判断是否是自己的商品
        Integer count = goodsBaseMapper.isMeGoods(shopId, dto.getCommonId());
        if (null != count && count >= 1) {
            return ResponseResult.fail(BizCodeEnume.GOODS_NOT_WORKING);
        }
        // 判断是否需要申请经销商
        BaseGoodsConfigVO manageGdsGoodsConfig = baseMapper.selectDelayGoodsConfig(dto.getCommonId());
        DealerApplyInfoVO dealerApplyInfo;
        if (manageGdsGoodsConfig.getSellCheck().intValue() == 1) {
            dto.setSellDeliver(manageGdsGoodsConfig.getSellDeliver());
            ResponseResult resultResp = queryGoodsUser(dto);
            if (resultResp.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
                return resultResp;
            } else {
                dealerApplyInfo = JSON.parseObject(JSON.toJSONString(resultResp.getData()), DealerApplyInfoVO.class);
            }

            return ResponseResult.success(supplyGoodsBase);

        } else {
            //根据commonid查询出对应供应商的goodsid和shopid
            List<Integer> list = baseMapper.selectShopIdAndGoodsId(dto.getCommonId());
            if (null != list && list.size() > 0) {
                //判读该商家是否已经同步过商品
                Integer countSys = Math.toIntExact(goodsBaseMapper.selectCount(new LambdaQueryWrapper<GoodsBaseEntity>()
                        .eq(GoodsBaseEntity::getShopId, shopId)
                        .eq(GoodsBaseEntity::getSupplierGoodsId, list.get(0))));
                if (null != countSys && countSys >= 1) {
                    return ResponseResult.fail(BizCodeEnume.PRODUCT_CANNOT_BE_SYNCHRONIZED_REPEATEDLY);
                }
            } else {
                return ResponseResult.fail(BizCodeEnume.INTERNAL_ERROR);
            }

            return ResponseResult.success(supplyGoodsBase);
        }
    }

    /**
     * 查询经销商用户信息
     *
     * @param dto 经销商商品用户信息DTO，包含商品ID、令牌等信息
     * @return 响应结果，包含成功或失败及原因
     */
    @Override
    public ResponseResult queryGoodsUser(DealerGoodsUserInfoDTO dto) {
        // 通过令牌获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(dto.getToken());
        if (null == userInfo) {
            return ResponseResult.fail(BizCodeEnume.USER_DOES_NOT_EXIST);
        } else if (null == userInfo.getShopType() || userInfo.getShopType().intValue() != 1) {
            return ResponseResult.fail(BizCodeEnume.USER_IS_NOT_BUSINESS);
        }
        Integer supplyShopId;
        Integer supplyGoodsId = null;
        if (null != dto.getCommonId()) {
            GoodsCommonEntity goodsCommon = baseMapper.selectById(dto.getCommonId());
            supplyShopId = goodsCommon.getShopId();
        } else {
            GdsBaseDetailResultVO goodsBase = goodsBaseMapper.selectByGoodsId(dto.getGoodsId());
            dto.setCommonId(goodsBase.getCommonId());
            supplyShopId = goodsBase.getShopId();
            supplyGoodsId = goodsBase.getGoodsId();
        }
        dto.setSupplierShopId(supplyShopId);
        dto.setDealerShopId(userInfo.getShopId());
        dto.setDealerStatus(CommonType.dealerStatus.AUTH_PASS.getCode());
        DealerApplyInfoVO dealerApplyInfo = baseServiceClient.queryDealerApplyByShop(dto);
        if (null == dealerApplyInfo) {
            return ResponseResult.fail(BizCodeEnume.NOT_APPLYING_FOR_A_DISTRIBUTOR);
        } else {
            if (dto.getSellDeliver().equals(CommonType.dealerType.PUT_AWAY.getCode())) {
                // 校验商品是否已经销
                DealerPutawayRecordVO record = new DealerPutawayRecordVO(dto.getCommonId(), supplyGoodsId, userInfo.getShopId(), supplyShopId, CommonType.putawayState.NORMAL.getCode(), CommonType.dealerType.PUT_AWAY.getCode());
                record = baseMapper.queryPutawayRecord(record);

                if (record == null) {
                    return ResponseResult.success(dealerApplyInfo);
                } else {
                    return ResponseResult.fail(BizCodeEnume.GOODS_DEALER);
                }
            }
            return ResponseResult.success(dealerApplyInfo);
        }
    }

    @Override
    public ResponseResult queryGoodsDetailByBusiness(ManageCommonIdsDTO dto) {
        // 根据商家需求查询商品详情
        // 通过商品通用ID查询商品基本信息
        GoodsCommonEntity goodsCommon = goodsCommonMapper.selectById(dto.getCommonId());
        // 若商品信息不存在，则返回错误结果
        if (null == goodsCommon) {
            return ResponseResult.fail(BizCodeEnume.GOODS_NOT_EXIST);
        }
        // 查询运费规则信息
        // 根据商品关联的运费区域ID查询运费规则
        ManageTransportAreaVO transportArea = baseMapper.selectByPrimaryKey(goodsCommon.getTransportAreaId());
        // 创建供货商商品详情对象
        SupplyGoodsDetailVO supplyGoodsDetail = new SupplyGoodsDetailVO(goodsCommon, transportArea.getName());
        // 查询不同规格的商品信息
        List<GoodsBaseEntity> goodsSpecList = goodsBaseMapper.selectList(new LambdaQueryWrapper<GoodsBaseEntity>()
                .eq(GoodsBaseEntity::getCommonId, goodsCommon.getCommonId()));
        // 查询商品图片信息
        List<GoodsImagesEntity> goodsImages = goodsImagesMapper.selectList(new LambdaQueryWrapper<GoodsImagesEntity>()
                .eq(GoodsImagesEntity::getCommonId, goodsCommon.getCommonId()));
        // 若图片信息为空，则使用商品默认图片创建新的图片信息对象并添加到列表
        if (CollectionUtils.isEmpty(goodsImages)) {
            GoodsImagesEntity goodsImage = new GoodsImagesEntity();
            goodsImage.setImagesImage(goodsCommon.getCommonImage());
            goodsImage.setImagesDisplayorder(0);
            goodsImage.setImagesIsDefault(1);
            goodsImages.add(goodsImage);
        }
        // 查询商品详细信息
        GoodsCommonDetailEntity detail = goodsCommonDetailMapper.selectById(goodsCommon.getCommonId());
        // 日志输出商品详细信息
        logger.info("detail{}", detail);

        // 设置商品详情信息
        supplyGoodsDetail.setCommonBody(detail.getCommonBody());
        supplyGoodsDetail.setGoodsSpecList(goodsSpecList);
        supplyGoodsDetail.setGoodsImages(goodsImages);

        // 返回查询到的商品详情结果
        return ResponseResult.success(supplyGoodsDetail);
    }

    @Override
    public ResponseResult queryGoodsListByBusiness(WebGoodsListQueryDTO dto) throws IllegalAccessException {
        // 初始化变量
        GoodsCatSearchChildVO goodsCatSearchChild;
        List<WebCatMenuVO> allMenu;
        List<Integer> catIds;

        // 获取登录用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(dto.getToken());
        // 用户不存在时返回错误结果
        if (null == userInfo) {
            return ResponseResult.fail(BizCodeEnume.USER_DOES_NOT_EXIST);
        }

        // 根据分类ID从缓存中获取商品分类信息
        if (null != dto.getCatId()) {
            String cacheCatIds = RedisUtil.get(Constants.CACHE_GOODS_WEB_GOODS_LIST_HEADER_PREFIX + "_" + dto.getCatId());
            // 如果缓存中存在该分类ID的信息，则直接使用
            if (null != cacheCatIds && !cacheCatIds.equals("")) {
                goodsCatSearchChild = JsonUtils.string2Obj(cacheCatIds, GoodsCatSearchChildVO.class);
                catIds = goodsCatSearchChild.getCatIds();
            } else {
                // 如果缓存中没有信息，则从数据库中获取
                allMenu = goodsCatMapper.selectAllCat();
                catIds = getMenuList(allMenu, dto.getCatId());
                goodsCatSearchChild = new GoodsCatSearchChildVO();
                goodsCatSearchChild.setAllMenu(allMenu);
                goodsCatSearchChild.setCatIds(catIds);
                // 更新缓存
                RedisUtil.set(Constants.CACHE_GOODS_WEB_GOODS_LIST_HEADER_PREFIX + "_" + dto.getCatId(), JsonUtils.toString(goodsCatSearchChild), RedisUtil.EXRP_MONTH);
            }
            // 添加当前分类ID到列表，并设置查询参数
            if (!CollectionUtils.isEmpty(catIds)) {
                catIds.add(dto.getCatId());
                dto.setCatIds(catIds);
                dto.setCatId(0);
            }
        }
        // 设置店铺ID为登录用户的店铺ID
        dto.setShopId(Integer.valueOf(userInfo.getShopId()));
        // 将查询参数转换为Map
        Map data = ParseMapUtils.beanToMap(dto);
        // 查询商品列表
        IPage<GdsListQueryResultVO> page = baseMapper.selectSupplyWebFilter(new Query<GdsListQueryResultVO>().getPage(data), dto);

        // 如果查询到商品，则处理商品操作类型
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            // 获取所有商品店铺信息
            List<Integer> goodsShopIdList = page.getRecords().stream().map(GdsListQueryResultVO::getShopId).distinct().collect(Collectors.toList());
            // 查询登录用户已申请经销商成功的店铺id
            List<BaseDealerApplyInfoVO> dealerApplyInfoList = goodsCatMapper.queryDealerApplyListByShop(userInfo.getShopId(), goodsShopIdList);
            goodsShopIdList = dealerApplyInfoList.stream().map(BaseDealerApplyInfoVO::getSupplierShopId).distinct().collect(Collectors.toList());
            // 遍历商品列表，设置操作类型
            for (GdsListQueryResultVO goods : page.getRecords()) {
                // 商品已经下架
                if (null != goods.getPutawayId()) {
                    goods.setOperateType(4);
                } else {
                    // 商品无需申请经销商
                    if (null == goods.getSellCheck() || goods.getSellCheck().intValue() == 2) {
                        goods.setOperateType(null == goods.getSellDeliver() ? 1 : goods.getSellDeliver());
                    } else {
                        // 需要申请经销商
                        if (!goodsShopIdList.contains(goods.getShopId())) {
                            goods.setOperateType(CommonType.goodsOperateType.APPLY.getCode());
                        } else {
                            goods.setOperateType(goods.getSellDeliver());
                        }
                    }
                }
            }
        }
        // 返回查询结果
        return ResponseResult.success(PageUtils.getPage(page));
    }


    @Override
    public ResponseResult seckillCheckGoods(ManageSeckillBaseUpdateDTO param) {
        // 检查参数是否非空
        if (param != null) {
            String seKillHourStr = "";
            // 当秒杀小时和秒杀开始日期均非空时，确定秒杀的具体时间
            if (null != param.getSeckillHour() && null != param.getSeckillStartDate()) {
                // 根据秒杀小时设置具体的时间字符串
                switch (param.getSeckillHour()) {
                    case 1:
                        seKillHourStr = "08:00:00";
                        break;
                    case 2:
                        seKillHourStr = "12:00:00";
                        break;
                    case 3:
                        seKillHourStr = "16:00:00";
                        break;
                    case 4:
                        seKillHourStr = "20:00:00";
                        break;
                    case 5:
                        seKillHourStr = "00:00:00";
                        break;
                    default:
                        seKillHourStr = "00:00:00";
                }
            }
            // 格式化日期并结合时间字符串生成秒杀开始的完整时间
            SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
            String seckillStartTime = simpleDateFormat2.format(param.getSeckillStartDate()) + " " + seKillHourStr;
            // 获取秒杀商品列表
            List<SeckillGoodsListDTO> seckillGoodsList = param.getSeckillGoodsList();
            // 检查秒杀商品列表是否非空且含有商品
            if (seckillGoodsList != null && seckillGoodsList.size() > 0) {
                // 遍历秒杀商品列表，检查每个商品是否参与了其他互斥活动
                for (SeckillGoodsListDTO goodsList : seckillGoodsList) {
                    // 查看是否参加其他类型活动（integer不为null是没参加其他活动）
                    Integer integer = baseMapper.selectCommonIsXian(goodsList.getCommonId());
                    // 查看参加同类型活动商品是否冲突（为null时没冲突）
                    List<AppSeckillGoodsVO> seckillGoodsResults = null;
                    // 根据秒杀ID是否为空，选择不同的查询方法
                    if (param.getSeckillId() != null) {
                        seckillGoodsResults = baseMapper.selectSeckillGoodsId1(goodsList.getGoodsId(), seckillStartTime, param.getSeckillId());
                    } else {
                        seckillGoodsResults = baseMapper.selectSeckillGoodsId(goodsList.getGoodsId(), seckillStartTime);
                    }
                    // 如果商品参加了其他活动，或者在相同时间参与了同类型的活动，则返回失败
                    if (integer == 0 || (seckillGoodsResults != null && seckillGoodsResults.size() > 0)) {
                        return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), "同一活动时间段内，部分活动商品已参加了其他互斥的营销活动，请调整商品或活动时间");
                    }
                }
                // 当所有商品都没有冲突时，返回成功信息
                return ResponseResult.success("无 互斥活动");
            }
        }
        // 当输入参数为空或商品列表为空时，返回默认成功响应
        return ResponseResult.success();
    }

    @Override
    public ResponseResult selecPlustGoodsByPage(Map<String, Object> map) {
        // 查询平台最低会员折扣
        String memberdiscount = baseMapper.selectmemberdiscount();
        BigDecimal bigDecimal = new BigDecimal(memberdiscount);

        // 获取token并解析用户信息
        String token = String.valueOf(map.get("token"));
        UserInfoVO userInfoVO = ParseTokenUtils.getUserInfoByToken(token);
        if (userInfoVO == null) {
            // 如果用户信息为空，返回TOKEN无效错误
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 将店铺ID放入map
        map.put("shopId", userInfoVO.getShopId());
        // 根据条件分页查询增强版商品信息
        IPage<ManageGdsNameQueryVO> page = baseMapper.selectByGdsNameByPlus(new Query<ManageGdsNameQueryVO>().getPage(map),
                map);

        // 查询最低折扣
        String mindiscount = mindiscount();

        // 遍历查询到的商品信息
        for (ManageGdsNameQueryVO good : page.getRecords()) {
            // 查询商品相关的优惠券信息
            List<VoucherMessageVO> voucherMes = baseMapper.selectVoucherMes(good.getCommonId());
            // 查询商品基础信息列表
            List<ManageGoodsBaseListVO> goodsBases = goodsBaseMapper.selectByGoodsList(good.getCommonId());
            good.setGoodsList(goodsBases);
            // 查询商品的最小库存和价格
            Map map1 = baseMapper.selectByGdsNamemin(good.getCommonId());
            good.setCommonStock(Integer.valueOf(String.valueOf(map1.get("goodsStock"))));
            good.setCommonPrice(String.valueOf(map1.get("goodsPrice")));

            // 根据最低折扣计算折扣价格
            if (mindiscount != null) {
                String goodsPrice = String.valueOf(map1.get("goodsPrice"));
                BigDecimal price = new BigDecimal(goodsPrice);
                BigDecimal discount = new BigDecimal(mindiscount);
                price = price.multiply(bigDecimal).multiply(discount);
                price = price.setScale(2, RoundingMode.HALF_UP);
                good.setDiscountPrice(String.valueOf(price));
            }

            // 如果优惠券信息为空，初始化为空列表
            if (CollectionUtils.isEmpty(voucherMes)) {
                voucherMes = Lists.newArrayList();
            }
            good.setVoucherMes(voucherMes);
            // 如果售卖状态为空，设置为1（上架）
            if (null == good.getSellEdit()) {
                good.setSellEdit(1);
            }
        }

        // 返回分页查询结果
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 删除加货商品信息
     *
     * @param map 包含商品信息和用户认证令牌的映射
     * @return 如果删除成功，返回操作成功的响应结果，否则返回操作失败的响应结果
     */
    @Override
    public ResponseResult deletePlusGoods(Map<String, Object> map) {
        // 从map中获取认证令牌
        String token = String.valueOf(map.get("token"));
        // 从Redis中获取用户信息字符串
        String userInfoStr = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + token);
        // 如果用户信息为空或字符串为空，则返回令牌无效的响应结果
        if (userInfoStr == null || userInfoStr == "") {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 将map中的'plusGoods'字段设置为0，表示删除加货商品
        map.put("plusGoods", 0);
        // 调用baseMapper的updatePlusGoods方法更新加货商品信息，并获取更新行数
        Integer integer = baseMapper.updatePlusGoods(map);
        // 如果更新行数大于0，则返回操作成功的响应结果，否则返回操作失败的响应结果
        if (integer > 0) {
            return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
        }
        return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
    }

    /**
     * 分页查询产品优惠券商品信息
     *
     * @param map 包含查询条件和用户认证令牌的映射
     * @return 返回查询结果的响应，包括分页的产品优惠券商品信息
     */
    @Override
    public ResponseResult selecProductVoucherGoodsByPage(Map<String, Object> map) {
        // 从map中获取认证令牌
        String token = String.valueOf(map.get("token"));
        // 解析令牌获取用户信息
        UserInfoVO userInfoVO = ParseTokenUtils.getUserInfoByToken(token);
        // 如果用户信息为空，则返回令牌无效的响应结果
        if (userInfoVO == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 将map中的'shopId'字段设置为用户信息中的shopId
        map.put("shopId", userInfoVO.getShopId());
        // 调用baseMapper的selecProductVoucherGoodsByPage方法进行分页查询，并获取分页结果
        IPage<ManageGdsNameQueryVO> page = baseMapper.selecProductVoucherGoodsByPage(new Query<ManageGdsNameQueryVO>().getPage(map),
                map);
        // 将分页结果包装成PageUtils的Page对象，并返回操作成功的响应结果
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 根据商品优惠券详情VO列表查询数据库中的商品优惠券信息
     * 此方法重写了父类或接口中的同名方法，目的是根据提供的商品优惠券详情VO列表
     * 从数据库中检索相对应的商品优惠券信息
     *
     * @param productVoucherGoodsVOS 商品优惠券详情VO列表，用于作为查询条件
     * @return 返回从数据库中查询到的商品优惠券详情VO列表
     */
    @Override
    public List<ProductVoucherGoodsVO> selecProductVoucherGoods(List<ProductVoucherGoodsVO> productVoucherGoodsVOS) {
        // 调用基础映射器的方法，从数据库中查询商品优惠券信息
        List<ProductVoucherGoodsVO> productVoucherGoodsVOList = baseMapper.selecProductVoucherGoods(productVoucherGoodsVOS);
        for (ProductVoucherGoodsVO productVoucherGoodsVO : productVoucherGoodsVOList) {
            // 查询默认商品规格库存
            GdsBaseDetailResultVO goodsBases = baseMapper.selectByGoodsId(productVoucherGoodsVO.getGoodsId().toString());
            productVoucherGoodsVO.setGoodsStock(goodsBases.getGoodsStock());
            productVoucherGoodsVO.setGoodsPrice(goodsBases.getGoodsPrice());
            //封装商品当前规格信息
            if (StringUtils.isNotEmpty(goodsBases.getSpecValue())) {
                List<GoodBaseSpecValuesJsonBeanVO> specValuesJsonBeans = new ArrayList<>();
                JSONArray specValuesJsonBeanArray = com.alibaba.fastjson.JSONObject.parseArray(goodsBases.getSpecValue());
                for (int index = 0; index < specValuesJsonBeanArray.size(); index++) {
                    com.alibaba.fastjson.JSONObject obj = specValuesJsonBeanArray.getJSONObject(index);
                    GoodBaseSpecValuesJsonBeanVO temp = JSONObject.parseObject(obj.toJSONString(), GoodBaseSpecValuesJsonBeanVO.class);
                    specValuesJsonBeans.add(temp);
                }
                productVoucherGoodsVO.setSpecValueList(specValuesJsonBeans);
            }
            // 封装商品所有规格信息
            if (StringUtils.isNotEmpty(productVoucherGoodsVO.getSpecParam())) {
                List<SpecParamJsonBeanVO> list = new ArrayList<>();
                JSONArray jsonSpecParam = com.alibaba.fastjson.JSONObject.parseArray(productVoucherGoodsVO.getSpecParam());
                for (int index = 0; index < jsonSpecParam.size(); index++) {
                    com.alibaba.fastjson.JSONObject obj = jsonSpecParam.getJSONObject(index);
                    SpecParamJsonBeanVO temp = JSONObject.parseObject(obj.toJSONString(), SpecParamJsonBeanVO.class);
                    list.add(temp);
                }
                productVoucherGoodsVO.setSpecParamList(list);
            }
        }
        // 返回查询结果
        return productVoucherGoodsVOList;
    }

    /**
     * 查询赠品列表
     *
     * @param param 查询参数对象，包含赠品信息和用户令牌
     * @return 如果权限验证通过，返回赠品列表；否则返回权限不足错误
     * @throws IllegalAccessException 如果访问权限检查失败
     */
    @Override
    public ResponseResult queryGiveawayList(ManjianzengzheListQueryDTO param) throws IllegalAccessException {
        // 权限判断
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());
        if (userPermissionsResult.getFlag()) {
            param.setShopId(userPermissionsResult.getShopId());
            Map data = ParseMapUtils.beanToMap(param);
            IPage<GoodsCommonEntity> page = baseMapper.queryGiveawayList(new Query<GoodsCommonEntity>().getPage(data),
                    param);
            return ResponseResult.success(PageUtils.getPage(page));
        } else {
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
    }

    /**
     * 折扣活动商品检查
     *
     * @param param 折扣活动参数对象，包含活动信息和商品列表
     * @return 如果存在互斥活动，返回错误信息；否则返回成功信息
     */
    @Override
    public ResponseResult discountCheckGoods(DiscountBaseAddParamDTO param) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 预热时间
        if (null == param.getPreStartHour()) {
            param.setPreStartHour(0);
        }
        Calendar cal = Calendar.getInstance();
        Date fdate = param.getDiscountStartTime();
        cal.setTime(fdate);
        cal.add(Calendar.HOUR, -param.getPreStartHour());
        fdate = cal.getTime();
        String format = sdf.format(fdate);
        List<DiscountGoodsListDTO> discountGoodsList = param.getDiscountGoodsList();
        if (discountGoodsList != null && discountGoodsList.size() > 0) {
            for (DiscountGoodsListDTO goodsList : discountGoodsList) {
                // 查看是否参加其他类型活动(integer不为null是没参加其他活动)
                Integer integer = Math.toIntExact(baseMapper.selectCount(new LambdaQueryWrapper<GoodsCommonEntity>()
                        .eq(GoodsCommonEntity::getCommonId, goodsList.getCommonId())
                        .in(GoodsCommonEntity::getCommonIsXian, 0, 3)));
                // 查看参加同类型活动商品是否冲突(为null时没冲突)
                List<DiscountGoodsListVO> goodsListResults = null;
                if (param.getDiscountId() != null) {
                    goodsListResults =
                            baseMapper.selectDiscountGoodsId1(goodsList.getGoodsId(), format, param.getDiscountId());
                } else {
                    goodsListResults =
                            baseMapper.selectDiscountGoodsId(goodsList.getGoodsId(), format);
                }
                if (integer == 0 || (goodsListResults != null && goodsListResults.size() > 0)) {
                    return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), "同一活动时间段内，部分活动商品已参加了其他互斥的营销活动，请调整商品或活动时间");
                }
            }
            return ResponseResult.success(CommonStatusEnum.SUCCESS.getCode(), "无互斥活动");
        }
        return ResponseResult.success(CommonStatusEnum.SUCCESS.getCode(), "无互斥活动");
    }

    /**
     * 根据拼团查询商品列表
     * 商城平台管理后台接口，需要登录权限验证
     *
     * @param param 查询参数对象，包含商品信息
     * @return 返回商品列表
     */
    @Override
    public ResponseResult queryGoodsListByPintuan(Map<String, Object> param) {
        // 统一使用commons的SecurityContextUtils获取当前用户（JWT过滤器已验证token）
        UserInfoVO userInfo = null;
        try {
            userInfo = com.houjiang.commons.util.SecurityContextUtils.getCurrentUser();
        } catch (Exception e) {
            // 用户未登录，返回权限不足错误
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        }
        
        // 如果用户信息为空，返回权限不足错误
        if (userInfo == null) {
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        }
        
        // 根据用户信息获取组织权限（shopId）
        if (userInfo.getShopId() != null && !userInfo.getShopId().isEmpty()) {
            try {
                Integer shopId = Integer.valueOf(userInfo.getShopId());
                param.put("shopId", shopId);
            } catch (Exception e) {
                // shopId格式错误，继续执行（不设置shopId过滤）
            }
        }
        
        // 执行查询
        IPage<GoodsCommonEntity> page = baseMapper.selectPromotionGoodsList2(new Query<GoodsCommonEntity>().getPage(param),
                param);
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 查询商品价格是否需要更新
     *
     * @param param 查询参数对象，包含商品信息和用户令牌
     * @return 如果权限验证通过，返回需要更新价格的商品列表；否则返回权限不足错误
     * @throws IllegalAccessException 如果访问权限检查失败
     */
    @Override
    public ResponseResult queryIsPriceUpdate(ManageGdsNameQueryDTO param) throws IllegalAccessException {
        // 权限判断
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());

        if (userPermissionsResult.getFlag()) {
            param.setShopId(userPermissionsResult.getShopId());

            Map data = ParseMapUtils.beanToMap(param);
            IPage<ManageGdsNameQueryVO> page = baseMapper.queryIsPriceUpdate(new Query<ManageGdsNameQueryVO>().getPage(data),
                    param);


            return ResponseResult.success(PageUtils.getPage(page));
        } else {
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
    }

    /**
     * 查询待发布商品列表
     *
     * @param param 查询参数对象，包含商品信息和用户令牌
     * @return 如果权限验证通过，返回待发布商品列表；否则返回权限不足错误
     * @throws IllegalAccessException 如果访问权限检查失败
     */
    @Override
    public ResponseResult queryWaitRelease(ManageGdsNameQueryDTO param) throws IllegalAccessException {
        // 权限判断
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());

        if (userPermissionsResult.getFlag()) {
            param.setShopId(userPermissionsResult.getShopId());
            Date commonSellTime = new Date();
            param.setCommonSellTime(commonSellTime);
            Map data = ParseMapUtils.beanToMap(param);
            IPage<GoodsCommonEntity> page = baseMapper.selectWaitRelease(new Query<GoodsCommonEntity>().getPage(data),
                    param);

            return ResponseResult.success(PageUtils.getPage(page));
        } else {
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
    }

    /**
     * 将商品设置为在线状态
     *
     * @param param 参数对象，包含要上架的商品ID列表和用户令牌
     * @return 如果权限验证通过，返回成功；否则返回权限不足错误
     */
    @Override
    public ResponseResult setGoodsOnline(ManageCommonIdsDTO param) {
        // 权限判断
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());

        if (userPermissionsResult.getFlag()) {
            // 商品上架
            baseMapper.updateOnline(param.getCommonIds());
            goodsBaseMapper.updateOnline(param.getCommonIds());
            //通过mq 更新es的商品数据
            Map<String, Object> map = new HashMap<>();
            List<Integer> list = new ArrayList<>();
            list = param.getCommonIds();
            map.put("commonIdS", list);
            // 1 是添加  2是删除
            map.put("type", 1);
            MqSendUtils.mqSendGoods(map);
        } else {
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
        return ResponseResult.success();
    }

    /**
     * 根据规格ID查询商品
     * 分页查询与指定规格ID相关的商品信息
     *
     * @param param 查询参数，包含分页信息和规格ID
     * @return 响应结果，包含分页查询到的商品信息
     */
    @Override
    public ResponseResult checkGoodsBySpecId(Map<String, Object> param) {

        IPage<GoodsCommonEntity> page = baseMapper.selectGoodsBySpecId(new Query<GoodsCommonEntity>().getPage(param),
                param);
        return ResponseResult.success(PageUtils.getPage(page));
    }


    /**
     * 商品下架功能
     * 通过权限校验后，将指定的商品下架，并处理相关联的供应商商品
     *
     * @param param 商品下架请求参数，包含商品ID和用户权限令牌
     * @return 响应结果，下架操作是否成功
     */
    @Override
    public ResponseResult setGoodsOffline(ManageCommonIdsDTO param) {
        // 权限判断
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());

        if (userPermissionsResult.getFlag()) {
            Map<String, Object> map = new HashMap<>();
            List<Integer> list = new ArrayList<>();

            // 商品下架
            goodsCommonMapper.update(null, new LambdaUpdateWrapper<GoodsCommonEntity>()
                    .set(GoodsCommonEntity::getCommonState, 0)
                    .in(GoodsCommonEntity::getCommonId, param.getCommonIds()));
            goodsBaseMapper.update(null, new LambdaUpdateWrapper<GoodsBaseEntity>()
                    .set(GoodsBaseEntity::getGoodsIsShelves, 2)
                    .eq(GoodsBaseEntity::getGoodsIsShelves, 1)
                    .in(GoodsBaseEntity::getCommonId, param.getCommonIds()));
            List<Integer> commonIds = param.getCommonIds();
            for (Integer commonId : commonIds) {
                //判断当前是否是供应商商品 如果是 下级商家对应的商品，并更新es
                List<Integer> dealerCommonIds = goodsCommonMapper.queryIsSupperGoods(commonId);
                if (null != dealerCommonIds && dealerCommonIds.size() > 0) {
                    //当前商品是供应商商品 所有商家商品下架 更新es
                    goodsCommonMapper.updateOffline(dealerCommonIds);
                    list.addAll(dealerCommonIds);
                }
            }
            list.addAll(param.getCommonIds());
            //通过mq 更新es的商品数据
            map.put("commonIdS", list);
            // 1 是添加  2是删除
            map.put("type", 2);
            MqSendUtils.mqSendGoods(map);
            return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
        } else {
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
    }

    /**
     * 计算最低折扣
     * 查询套餐表，计算所有套餐中最低的折扣
     *
     * @return 最低折扣值，如果没有可用折扣则返回null
     */
    public String mindiscount() {
        //查询套餐表
        List<PlusThaliEntityVO> plusThaliEntities = this.baseMapper.selectthaliList();
        List returnList = new ArrayList<>();
        if (plusThaliEntities != null && plusThaliEntities.size() > 0) {
            for (PlusThaliEntityVO plusThaliEntity : plusThaliEntities) {

                String thaliEquity = plusThaliEntity.getThaliEquity();
                String[] split = thaliEquity.split(",");
                List<Integer> appIdList = Stream.of(split).map(Integer::parseInt).collect(Collectors.toList());
                plusThaliEntity.setThaliEquityList(appIdList);

                List<PlusEquityEntityVO> selectplusequitys = this.baseMapper.selectplusequitys(appIdList);
                if (selectplusequitys != null && selectplusequitys.size() > 0) {
                    for (PlusEquityEntityVO selectplusequity : selectplusequitys) {
                        if (selectplusequity.getEquityType() == 1) {
                            String equityValue = selectplusequity.getEquityValue();
                            BigDecimal bigDecimal1 = new BigDecimal(equityValue);
                            returnList.add(bigDecimal1);
                        }
                    }
                }
            }
        }
        if (returnList != null && returnList.size() > 0) {
            return String.valueOf(Collections.min(returnList));
        }
        return null;
    }

    /**
     * 更新指定的商品为加号商品
     *
     * @param map 包含商品信息和用户验证信息的映射
     * @return 如果更新成功，返回操作成功的响应结果；否则，返回操作失败的响应结果
     */
    @Override
    public ResponseResult updatePlusGoods(Map<String, Object> map) {
        // 从map中获取token
        String token = String.valueOf(map.get("token"));
        // 根据token从Redis中获取用户信息
        String userInfoStr = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + token);
        // 如果用户信息为空或为空字符串，则返回TOKEN无效的错误
        if (userInfoStr == null || userInfoStr == "") {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 将map中的plusGoods标记为1，表示加号商品
        map.put("plusGoods", 1);
        // 调用baseMapper的updatePlusGoods方法更新商品信息
        Integer integer = this.baseMapper.updatePlusGoods(map);
        // 如果更新数量大于0，则返回操作成功的响应结果；否则，返回操作失败的响应结果
        if (integer > 0) {
            return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
        }
        return ResponseResult.success(BizCodeEnume.OPERATION_FAILED);
    }

    /**
     * 检查商品名称是否重复
     *
     * @param param 包含商品名称查询参数的对象
     * @return 返回检查结果，2表示重复，3表示不重复
     */
    @Override
    public ResponseResult checkGoodsName(CheckNameQueryParamDTO param) {
        // 默认标志值为2，表示重复
        Integer flag = 2;
        // 如果param中的commonId不为空，则查询对应的商品通用名称
        if (null != param.getCommonId()) {
            String commonName = this.baseMapper.queryCommonName(param.getCommonId());
            // 如果查询到的通用名称不为空且与待检查名称相同，则设置标志为3，表示不重复
            if (null != commonName && commonName.equals(param.getCheckName())) {
                flag = 3;
                return ResponseResult.success(flag);
            }
        }
        // 调用baseMapper的selectCommonName方法查询名称重复性
        Integer checkName = this.baseMapper.selectCommonName(param);
        // 如果查询结果为空，则设置标志为3，表示不重复
        if (null == checkName) {
            flag = 3;
        }
        // 返回检查结果
        return ResponseResult.success(flag);
    }

    @Override
    public ResponseResult livequeryByGoodsName(ManageGdsNameQueryDTO param) {
        // 权限判断
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());

        if (!userPermissionsResult.getFlag()) {
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        }
        if (null == param.getSupplyShopId()) {
            param.setShopId(userPermissionsResult.getShopId());
        }
        List<Integer> integers = this.baseMapper.queryLiveCommons2(userPermissionsResult.getShopId());
        // 查询出售中的商品
        Page<ManageGdsNameQueryVO> page = new Page<>(param.getPage(), param.getLimit());
        IPage<ManageGdsNameQueryVO> goods = this.baseMapper.selectByGdsName2(page, param, integers);

        for (ManageGdsNameQueryVO good : goods.getRecords()) {
            List<VoucherMessageVO> voucherMes = this.baseMapper.selectVoucherMes(good.getCommonId());
            if (CollectionUtils.isEmpty(voucherMes)) {
                voucherMes = Lists.newArrayList();
            }
            good.setVoucherMes(voucherMes);
        }
        return ResponseResult.success(PageUtils.getPage(goods));
    }

    /**
     * 商品分类授权校验
     *
     * @param passShopCatList
     * @param goodsCatList
     * @return
     */
    private Boolean queryGoodsCat(List<ManageGoodsSelfCatVO> passShopCatList, List<ShopGoodsCatVO> goodsCatList) {
        List<Integer> passCatIdList = passShopCatList.stream().map(ManageGoodsSelfCatVO::getShopGoodsCatId).collect(Collectors.toList());
        List<Integer> goodsCatIdList = goodsCatList.stream().map(ShopGoodsCatVO::getShopGoodsCatId).collect(Collectors.toList());
        // 校验商品的分类id是否有申请记录
        List<Integer> passCatList = goodsCatIdList.stream().filter(item -> passCatIdList.contains(item)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(passCatList)) {
            // 校验商品的父类id是否有申请记录
            List<Integer> goodsParentCatIdList = goodsCatList.stream().map(ShopGoodsCatVO::getParentId).distinct().collect(Collectors.toList());
            passCatList = goodsParentCatIdList.stream().filter(item -> passCatIdList.contains(item)).collect(Collectors.toList());
        }
        if (!CollectionUtils.isEmpty(passCatList)) {
            return true;
        }
        return false;
    }

    @Override
    public ResponseResult shopListQueryByShopFilter(ListPageQueryParamDTO param) {
        // 初始化变量用于存储查询到的店铺类型
        Integer selectshoptype = 0;
        // 如果提供了店铺ID，则查询该店铺的类型
        if (param.getShopId() != null) {
            selectshoptype = this.baseMapper.selectshoptype(param.getShopId());
        }
        // 只有当店铺类型不是特定类型时，才执行以下逻辑
        if (selectshoptype != 3) {
            // 如果提供了分类ID，则根据该分类ID查询相关店铺
            if (null != param.getCatId()) {
                // 根据分类ID获取商品分类信息
                WebCatMenuVO webCatMenu = goodsCatMapper.queryByCatId(param.getCatId());
                // 用于存储分类ID列表
                List<Integer> catIdList = new ArrayList<>();
                // 如果找到了对应的分类信息
                if (webCatMenu != null) {
                    // 如果点击的是一级分类
                    if (webCatMenu.getCatLevel() != null && webCatMenu.getCatLevel() == 1) {
                        // 查询下一级分类
                        List<WebCatMenuVO> nextCatIds = goodsCatMapper.selectByCatParentId2(webCatMenu.getCatId());
                        // 如果存在下一级分类
                        if (nextCatIds != null && !nextCatIds.isEmpty()) {
                            // 遍历下一级分类，查询下下级分类并添加到列表
                            for (WebCatMenuVO nextCatId : nextCatIds) {
                                List<WebCatMenuVO> nextCatIds1 = goodsCatMapper.selectByCatParentId2(nextCatId.getCatId());
                                if (nextCatIds1 != null && !nextCatIds1.isEmpty()) {
                                    List<Integer> catIds = catIds(nextCatIds1);
                                    catIdList.addAll(catIds);
                                }
                            }
                            // 清除原有分类ID并设置新的分类ID列表
                            param.setCatId(null);
                            param.setCatIds(catIdList);
                        }
                    } else if (webCatMenu.getCatLevel() != null && webCatMenu.getCatLevel() == 2) {
                        // 如果点击的是二级分类，逻辑类似一级分类但少一次循环
                        List<WebCatMenuVO> nextCatIds1 = goodsCatMapper.selectByCatParentId2(webCatMenu.getCatId());
                        if (nextCatIds1 != null && !nextCatIds1.isEmpty()) {
                            List<Integer> catIds = catIds(nextCatIds1);
                            param.setCatId(null);
                            param.setCatIds(catIds);
                        }
                    }
                }
            }
            // 将参数对象转换为Map，便于后续处理
            Map data = null;
            try {
                data = ParseMapUtils.beanToMap(param);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            // 根据过滤条件查询商品列表，并进行分页
            IPage<GdsListQueryResultVO> page = this.baseMapper.selectByFilter(new Query<GdsListQueryResultVO>().getPage(data), param);
            // 处理查询结果
            List<GdsListQueryResultVO> records = page.getRecords();
            for (GdsListQueryResultVO gdsListQueryResult : records) {
                // 解析商品ID
                gdsListQueryResult.setGoodsIdOne(Integer.parseInt(gdsListQueryResult.getGoodsId()));
                // 特殊条件下的商品ID重设
                if (gdsListQueryResult.getGoodsIdOne() == 1) {
                    List<Integer> goodsIds = goodsBaseMapper.selectGoodsId(gdsListQueryResult.getCommonId());
                    if (!CollectionUtils.isEmpty(goodsIds)) {
                        gdsListQueryResult.setGoodsIdOne(goodsIds.get(0));
                    }
                }

                // 重新设置商品ID
                gdsListQueryResult.setGoodsIdOne(Integer.parseInt(gdsListQueryResult.getGoodsId()));

                // 获取商品的活动状态
                Integer commonIsXian = gdsListQueryResult.getCommonIsXian();
                // 检查并处理商品活动详情
                ActivityGoodsDetailDTO activityGoodsDetail = dealActivity(gdsListQueryResult.getGoodsIdOne(),
                        gdsListQueryResult.getCommonId(), commonIsXian);

                // 根据活动状态更新商品信息
                if (null != activityGoodsDetail && activityGoodsDetail.getActivityStatus() != 0) {
                    gdsListQueryResult.setActivityStatus(activityGoodsDetail.getActivityStatus());
                    gdsListQueryResult.setActivityGoodsDetailDTO(activityGoodsDetail);
                    gdsListQueryResult.setCommonIsXian(activityGoodsDetail.getActivityStatus());
                    // 如果商品在活动中，设置特殊价格
                    if (activityGoodsDetail.getActivityStatus() == 5) {
                        gdsListQueryResult.setCommonPrice(activityGoodsDetail.getMjzzPrice());
                    } else if (null != activityGoodsDetail.getActivityPrice()) {
                        gdsListQueryResult.setCommonPrice(activityGoodsDetail.getActivityPrice());
                    }
                } else {
                    // 如果商品不在活动中，重置活动状态和相关商品信息
                    gdsListQueryResult.setActivityStatus(0);
                    gdsListQueryResult.setActivityGoodsDetailDTO(new ActivityGoodsDetailDTO());
                    gdsListQueryResult.setCommonIsXian(0);
                }
            }
            // 更新分页记录
            page.setRecords(records);
            // 返回处理后的分页数据
            return ResponseResult.success(PageUtils.getPage(page));
        } else {
            //品牌店铺
            if (param.getCatId() != null) {
                param.setCatIdstring(String.valueOf(param.getCatId()));
            }
            Map data = null;
            try {
                data = ParseMapUtils.beanToMap(param);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            IPage<GdsListQueryResultVO> page = this.baseMapper.selectShopByFilter(new Query<GdsListQueryResultVO>().getPage(data), param);
            List<GdsListQueryResultVO> records = page.getRecords();
            for (GdsListQueryResultVO gdsListQueryResult : records) {
                gdsListQueryResult.setGoodsIdOne(Integer.parseInt(gdsListQueryResult.getGoodsId()));
                if (gdsListQueryResult.getGoodsIdOne() == 1) {
                    List<Integer> goodsIds = goodsBaseMapper.selectGoodsId(gdsListQueryResult.getCommonId());
                    if (!CollectionUtils.isEmpty(goodsIds)) {
                        gdsListQueryResult.setGoodsIdOne(goodsIds.get(0));
                    }
                }

                gdsListQueryResult.setGoodsIdOne(Integer.parseInt(gdsListQueryResult.getGoodsId()));

                Integer commonIsXian = gdsListQueryResult.getCommonIsXian();
                // 活动
                ActivityGoodsDetailDTO activityGoodsDetail = dealActivity(gdsListQueryResult.getGoodsIdOne(),
                        gdsListQueryResult.getCommonId(), commonIsXian);

                if (null != activityGoodsDetail && activityGoodsDetail.getActivityStatus() != 0) {
                    gdsListQueryResult.setActivityStatus(activityGoodsDetail.getActivityStatus());
                    gdsListQueryResult.setActivityGoodsDetailDTO(activityGoodsDetail);
                    gdsListQueryResult.setCommonIsXian(activityGoodsDetail.getActivityStatus());
                    if (activityGoodsDetail.getActivityStatus() == 5) {
                        gdsListQueryResult.setCommonPrice(activityGoodsDetail.getMjzzPrice());
                    }
                } else {
                    gdsListQueryResult.setActivityStatus(0);
                    gdsListQueryResult.setActivityGoodsDetailDTO(new ActivityGoodsDetailDTO());
                    gdsListQueryResult.setCommonIsXian(0);
                }
            }
            page.setRecords(records);
            return ResponseResult.success(PageUtils.getPage(page));
        }

    }

    /**
     * 根据参数查询商品信息
     *
     * @param param 查询参数，包含分页和筛选条件
     * @return 返回查询到的商品信息分页结果
     */
    @Override
    public ResponseResult queryGoods(Map<String, Object> param) {
        IPage<GoodsInfoDTO> goodsInfo = this.baseMapper.queryGoodsInfo2(new Query<GoodsInfoDTO>().getPage(param),
                param);
        return ResponseResult.success(PageUtils.getPage(goodsInfo));
    }

    /**
     * 根据过滤条件查询商品
     *
     * @param param 商品查询参数，包含页码和限制数量
     * @return 返回查询到的商品分页结果
     * @throws IllegalAccessException 当访问规则不匹配时抛出异常
     */
    @Override
    public ResponseResult queryByFilter(ShopIndexQueryParamDTO param) throws IllegalAccessException {
        Page<ListQueryResultVO> page = new Page<>(param.getPage(), param.getLimit());
        IPage<ListQueryResultVO> gdsList = this.baseMapper.selectByShopFilter(page, param);
        for (ListQueryResultVO gdsListQueryResult : gdsList.getRecords()) {
            // 设置商品ID
            gdsListQueryResult.setGoodsIdOne(Integer.parseInt(gdsListQueryResult.getGoodsId()));

            Integer commonIsXian = gdsListQueryResult.getCommonIsXian();
            // 处理活动商品详情
            ActivityGoodsDetailDTO activityGoodsDetail = dealActivity(gdsListQueryResult.getGoodsIdOne(),
                    gdsListQueryResult.getCommonId(), commonIsXian);

            // 根据活动状态设置商品属性
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
        }
        return ResponseResult.success(gdsList);
    }

    /**
     * 查询店铺首页信息
     *
     * @param param 查询参数对象，包含店铺ID等信息
     * @return 返回包含店铺首页信息的响应结果
     * @throws IllegalAccessException 当访问权限不足时抛出异常
     */
    @Override
    public ResponseResult queryShopIndexquery(ShopIndexQueryParamDTO param) throws IllegalAccessException {
        // 根据shopId查询所有商品
        Map data = ParseMapUtils.beanToMap(param);
        IPage<ListQueryResultVO> gdsList = this.baseMapper.selectByShopIndex2(new Query<ListQueryResultVO>().getPage(data),
                param);
        // 处理每个商品的信息
        for (ListQueryResultVO gdsListQueryResult : gdsList.getRecords()) {
            // 将字符串的goodsId转换为整型
            gdsListQueryResult.setGoodsIdOne(Integer.parseInt(gdsListQueryResult.getGoodsId()));

            // 查询商品的活动信息
            Integer commonIsXian = gdsListQueryResult.getCommonIsXian();
            ActivityGoodsDetailDTO activityGoodsDetail = dealActivity(gdsListQueryResult.getGoodsIdOne(),
                    gdsListQueryResult.getCommonId(), commonIsXian);

            // 根据活动信息设置商品的状态和价格
            if (activityGoodsDetail != null && activityGoodsDetail.getActivityStatus() != 0) {
                gdsListQueryResult.setActivityStatus(activityGoodsDetail.getActivityStatus());
                gdsListQueryResult.setActivityGoodsDetail(activityGoodsDetail);
                gdsListQueryResult.setCommonIsXian(activityGoodsDetail.getActivityStatus());
                gdsListQueryResult.setCommonPrice(activityGoodsDetail.getActivityPrice());
                if (activityGoodsDetail.getActivityStatus() == 5) {
                    gdsListQueryResult.setCommonPrice(activityGoodsDetail.getMjzzPrice());
                }
            } else {
                // 如果没有活动信息，则设置默认状态和价格
                gdsListQueryResult.setActivityStatus(0);
                gdsListQueryResult.setActivityGoodsDetail(new ActivityGoodsDetailDTO());
                gdsListQueryResult.setCommonIsXian(0);
            }
        }
        // 查询首页信息
        ShopIndexVO shopIndex = this.baseMapper.selectByParam(param.getShopId());
        if (shopIndex == null) {
            return ResponseResult.fail(2, "无店铺信息");
        }
        // 设置店铺收藏状态
        shopIndex.setIsEnshrine(1);
        // 查询店铺商品总数
        Integer shopSumNum = goodsCommonMapper.selectShopSum(param.getShopId());

        // 处理轮播信息
        dealShopSlide(shopIndex);

        String configValue = null;
        // 如果店铺的图片信息为空，则从配置中获取默认图片
        if (shopIndex.getShopImage() == null || shopIndex.getShopImage().equals("") || shopIndex.getShopLogo() == null
                || shopIndex.getShopLogo().equals("") || shopIndex.getWapShopLogo() == null
                || shopIndex.getWapShopLogo().equals("")) {
            configValue = baseServiceClient.selectConfigValueFeign("shop_default_config");
            ShopConfigDefaultValueVO shopConfig = JsonUtils.string2Obj(configValue, ShopConfigDefaultValueVO.class);

            if (shopIndex.getShopImage() == null || shopIndex.getShopImage().equals("")) {
                shopIndex.setShopImage(shopConfig.getShopImage());
            }
            if (shopIndex.getShopLogo() == null || shopIndex.getShopLogo().equals("")) {
                shopIndex.setShopLogo(shopConfig.getShopLogo());
            }
            if ((shopIndex.getWapShopLogo() == null || shopIndex.getWapShopLogo().equals(""))
                    && shopIndex.getShopLogo() != null) {
                String shopConfigValue = baseServiceClient.selectConfigValueFeign("logo_copywriting_ico_shop");
                shopIndex.setWapShopLogo(shopConfigValue);
            }

            if (shopIndex.getShopSlide() == null || shopIndex.getShopSlide().equals("")) {
                shopIndex.setShopSlide(shopConfig.getShopSlide());
            }
        }
        // 设置店铺商品总数和商品列表
        shopIndex.setShopSumNum(shopSumNum);
        shopIndex.setGdsList(gdsList.getRecords());
        // 返回店铺首页信息
        return ResponseResult.success(shopIndex);
    }

    @Override
    public ResponseResult zipLocalDiskImage(byte[] sourceZipFileBytes, List<String> commonList) throws IOException {
        // 定义所需的变量和资源
        String zipEntryName = null;
        ZipEntry singleZipEntry = null;
        ZipInputStream sourceZipZis = null;
        BufferedInputStream sourceZipBis = null;
        ByteArrayInputStream sourceZipBais = null;
        Map<String, byte[]> targetFilesFolderMap = null;

        try {
            // 初始化解压过程所需的变量和对象
            targetFilesFolderMap = new HashMap<String, byte[]>();
            sourceZipBais = new ByteArrayInputStream(sourceZipFileBytes);
            sourceZipBis = new BufferedInputStream(sourceZipBais);
            sourceZipZis = new ZipInputStream(sourceZipBis, Charset.forName("GBK"));

            // 循环读取压缩包中的每一个条目并将其解压缩至Map中
            while ((singleZipEntry = sourceZipZis.getNextEntry()) != null) {
                zipEntryName = singleZipEntry.getName();
                String var = zipEntryName.substring(zipEntryName.indexOf("/"), zipEntryName.length());
                if (!"/".equals(var)) {
                    targetFilesFolderMap.put(zipEntryName, IOUtils.toByteArray(sourceZipZis));
                }
            }

        } catch (Exception e) {
            // 异常处理：打印异常堆栈信息
            e.printStackTrace();
        } finally {
            // 关闭资源
            if (null != sourceZipZis) {
                sourceZipZis.close();
            }
            if (null != sourceZipBis) {
                sourceZipBis.close();
            }
            if (null != sourceZipBais) {
                sourceZipBais.close();
            }
        }
        if (targetFilesFolderMap == null || targetFilesFolderMap.size() == 0) {
            return ResponseResult.success();
        }
        // 将解压后的文件上传至OSS，并返回上传结果
        return ResponseResult.success(importUploadOSS(targetFilesFolderMap, commonList));
    }

    /**
     * 批量插入商品信息
     *
     * @param file              上传的文件
     * @param token             用户身份令牌
     * @param transportAreaId   物流区域ID
     * @param transportTemplate 物流模板ID
     * @return 返回插入结果的响应
     * @throws Exception 可能抛出的异常
     */
    @Override
    public ResponseResult insertGoodsBatch(MultipartFile file, String token, Integer transportAreaId, Integer transportTemplate) throws Exception {
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(token);
        if (!userPermissionsResult.getFlag()) {
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        }
        // 获取文件原始名称
        String fileName = file.getOriginalFilename();
        // 获取文件扩展名的位置
        int lastIndex = fileName.lastIndexOf(".");
        // 获取上传文件的扩展名
        String extensionName = fileName.substring(lastIndex, fileName.length());
        // 判断需要用xls，还是xlsx
        boolean isExcels = true;
        if (extensionName.equals(".xls")) {
            isExcels = false;
        }
        // 解析excel数据
        Map<String, Object> datas = readExcel(file.getInputStream(), isExcels, userPermissionsResult.getShopId());
        // 用于返回的结果集合
        Map<String, Object> returnMap = new HashMap<String, Object>();
        if (null != datas) {
            // 创建商品批量插入参数对象
            GoodsBatchCommonVO param = new GoodsBatchCommonVO();
            param.setTransportAreaId(transportAreaId);
            param.setTransportTemplate(transportTemplate);
            param.setToken(token);
            // 调用导入Excel数据的方法
            Map<String, Object> result = importExcelData(datas, param);
            // 判断结果是否为空
            if (null == result) {
                return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
            }
            // 说明数据有错误
            if (null != result && !result.isEmpty() && null != result.get("errorMsg")) {
                // 处理错误信息
                Map errorMap = (Map) result.get("errorMsg");
                if (!errorMap.isEmpty()) {
                    // 生成唯一标识并存储错误信息
                    UUID uuid = UUID.randomUUID();
                    RedisUtil.set(uuid.toString(), JSON.toJSONString(result), 60 * 60 * 24);
                    returnMap.put("errorMsg", uuid.toString());
                }
            }
            // 处理成功数据
            if (null != result && !result.isEmpty() && null != result.get("commonIds")) {
                returnMap.put("success", result.get("commonIds"));
            }
            // 返回解压状态
            returnMap.put("isUploadUnzip", datas.get("isUploadUnzip"));

        } else {
            // 解析数据错误
            return ResponseResult.fail(0, "解析excel 数据错误");
        }
        // 返回处理结果
        return ResponseResult.success(returnMap);
    }

    /**
     * 导入Excel数据
     *
     * @param dataParam 导入的数据参数
     * @param param     商品批量插入参数
     * @return 返回导入结果的地图
     */
    @Override
    public Map<String, Object> importExcelData(Map<String, Object> dataParam, GoodsBatchCommonVO param) {
        // 处理数据
        Map<String, Object> result = new HashMap<>();
        List<ImportGoodsDataBeanVO> importData = new ArrayList<>();
        List<Integer> commonIds = new ArrayList<Integer>();
        Map<String, ImportGoodsDataBeanVO> importGoodsDataBeans = (Map<String, ImportGoodsDataBeanVO>) dataParam
                .get("goodsCommonMap");
        ImportGoodsDataBeanVO record = null;
        Map<String, Object> seccussMap = new HashMap<String, Object>();
        for (String key : importGoodsDataBeans.keySet()) {
            // 取出SPU信息
            record = importGoodsDataBeans.get(key);
            if (null != record.getGdbMerchantGoodsCommon()) {
                // 保存数据并获取commonId
                Integer commonId = saveData(record, param);
                if (null != commonId) {
                    commonIds.add(commonId);
                } else {
                    importData.add(record);
                }
            }
        }
        // 组织返回结果
        result.put("commonIds", commonIds);
        result.put("errorMsg", dataParam.get("errorMsg"));
        if (!importData.isEmpty()) {
            result.put("importData", importData);
        }
        return result;
    }


    /**
     * 更新秒杀商品的库存信息
     *
     * @param commonIdList 商品的公共ID列表
     */
    @Override
    public void updateGiveawayStock(List<Integer> commonIdList) {
        for (Integer commonId : commonIdList) {
            // 根据公共ID获取商品基本信息
            GoodsCommonEntity entity = this.baseMapper.selectById(commonId);
            // 查询与公共ID关联的所有商品详情
            List<GoodsStockEntity> list = goodsStockService.getBaseMapper().selectList(new LambdaQueryWrapper<GoodsStockEntity>()
                    .eq(GoodsStockEntity::getCommonId, commonId));
            GoodsStockEntity baseEntity = new GoodsStockEntity();
            GoodsBaseEntity skuEntity = new GoodsBaseEntity();
            // 寻找是否有库存大于0的商品详情
            for (GoodsStockEntity base : list) {
                if (base.getGoodsStock() > 0) {
                    baseEntity = base;
                    skuEntity = goodsBaseMapper.selectById(base.getGoodsId());
                    break;
                }
            }
            // 秒杀活动每次减少的库存数量
            int num = 1;
            // 确保商品的公共库存不会为负数
            Integer commonStock = entity.getCommonStock() > 0 ? entity.getCommonStock() : 0;
            // 确保商品的详细库存不会为负数
            Integer goodsStock = baseEntity.getGoodsStock() > 0 ? baseEntity.getGoodsStock() : 0;
            // 如果商品详情中有库存大于0的，则更新库存信息
            if (baseEntity.getGoodsStock() > 0) {
                if ((entity.getCommonStock() - num) >= 0) {
                    commonStock = entity.getCommonStock() - num;
                }
                if ((baseEntity.getGoodsStock() - num) >= 0) {
                    goodsStock = baseEntity.getGoodsStock() - num;
                }
            }
            RedisUtil.remove(HouConfig.GOODS_STOCK + baseEntity.getGoodsId());
            // 更新SPU库存
            this.baseMapper.update(null, new LambdaUpdateWrapper<GoodsCommonEntity>()
                    .eq(GoodsCommonEntity::getCommonId, entity.getCommonId())
                    .set(GoodsCommonEntity::getCommonSalenum, entity.getCommonSalenum() + num)
                    .set(GoodsCommonEntity::getCommonStock, commonStock));
            // 更新SKU库存
            goodsBaseMapper.update(skuEntity, new LambdaUpdateWrapper<GoodsBaseEntity>()
                    .eq(GoodsBaseEntity::getGoodsId, skuEntity.getGoodsId())
                    .set(GoodsBaseEntity::getGoodsSalenum, skuEntity.getGoodsSalenum() + num));
            goodsStockService.getBaseMapper().update(baseEntity, new LambdaUpdateWrapper<GoodsStockEntity>()
                    .eq(GoodsStockEntity::getGoodsId, baseEntity.getGoodsId())
                    .set(GoodsStockEntity::getGoodsStock, goodsStock));
        }
    }

    /**
     * 查询待发布的商品
     *
     * @param param 查询参数
     * @return 包含待发布商品信息的响应结果
     */
    @Override
    public ResponseResult queryToBeReleased(ManageGdsNameQueryParamDTO param) {
        // 从令牌中解析店铺用户信息
        ResponseResult rr = ParseTokenUtils.getShopUserInfo(param.getToken());
        if (rr.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return rr;
        }
        // 将解析得到的用户信息转换为UserInfoVO对象
        UserInfoVO ui = JSON.parseObject(JSON.toJSONString(rr.getData()), UserInfoVO.class);
        // 设置查询参数中的店铺ID
        param.setShopId(ui.getShopId());
        // 分页查询等待审核的商品
        Page<ManageGdsNameVO> page = new Page<>(param.getPage(), param.getLimit());
        IPage<ManageGdsNameVO> goods = this.baseMapper.queryToBeReleased(page, param);
        // 返回分页后的商品信息
        return ResponseResult.success(PageUtils.getPage(goods));
    }

    /**
     * 定时发布商品--立即发布
     * 该方法通过接收一组商品标识，将这些商品的状态更改为上架状态，实现商品的立即发布
     *
     * @param param 包含了token和需要发布的商品标识的参数对象
     * @return 如果商品状态更新成功，则返回成功响应；否则返回失败响应
     */
    @Override
    public ResponseResult fixedTimeGoodsPublishNow(ManageCommonIdsDTO param) {
        // 解析令牌获取店铺用户信息
        ResponseResult rr = ParseTokenUtils.getShopUserInfo(param.getToken());
        if (rr.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            // 如果解析令牌失败，则直接返回失败的响应
            return rr;
        }

        // 标记用于记录商品状态更新是否成功的变量
        Boolean commonFlag = false;
        Boolean goodsFlag = false;

        // 遍历所有需要发布的商品标识
        for (Integer commonId : param.getCommonIds()) {
            // 更改商品状态
            commonFlag = this.baseMapper.update(null, new LambdaUpdateWrapper<GoodsCommonEntity>()
                    .set(GoodsCommonEntity::getCommonState, CommonType.commonStatus.YES.getCode())
                    .eq(GoodsCommonEntity::getCommonId, commonId)) > 0;

            // 更改商品上架状态
            goodsFlag = goodsBaseMapper.update(null, new LambdaUpdateWrapper<GoodsBaseEntity>()
                    .set(GoodsBaseEntity::getGoodsIsShelves, CommonType.goodsIsShelves.GOODS_ON_THE_SHELF.getCode())
                    .eq(GoodsBaseEntity::getCommonId, commonId)) > 0;
        }

        // 如果所有商品的状态更新都成功，则返回成功响应；否则返回失败响应
        if (commonFlag && goodsFlag) {
            return ResponseResult.success();
        }
        return ResponseResult.fail();
    }


    /**
     * 放入仓库商品--立即审核
     *
     * @param param 包含商品ID和令牌的参数对象
     * @return 审核操作的结果
     */
    @Override
    public ResponseResult putIntoWarehouseGoodsReviewingNow(ManageCommonIdsDTO param) {
        // 解析令牌获取店铺用户信息
        ResponseResult rr = ParseTokenUtils.getShopUserInfo(param.getToken());
        // 如果解析失败，则直接返回错误结果
        if (rr.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return rr;
        }

        // 标记用于记录商品状态和上架状态更改是否成功
        Boolean commonFlag = false;
        Boolean goodsFlag = false;

        // 遍历商品ID列表，逐个更改商品状态和上架状态
        for (Integer commonId : param.getCommonIds()) {
            // 更改商品状态为审核中
            commonFlag = this.baseMapper.update(null, new LambdaUpdateWrapper<GoodsCommonEntity>()
                    .set(GoodsCommonEntity::getCommonState, CommonType.commonStatus.YES.getCode())
                    .set(GoodsCommonEntity::getCommonVerify, CommonType.productReview.UNDER_REVIEW.getCode())
                    .eq(GoodsCommonEntity::getCommonId, commonId)) > 0;

            // 更改上架状态为已上架
            goodsFlag = goodsBaseMapper.update(null, new LambdaUpdateWrapper<GoodsBaseEntity>()
                    .set(GoodsBaseEntity::getGoodsIsShelves, CommonType.goodsIsShelves.GOODS_ON_THE_SHELF.getCode())
                    .eq(GoodsBaseEntity::getCommonId, commonId)) > 0;
        }

        // 如果所有商品的状态和上架状态都更改成功，则返回成功结果，否则返回失败结果
        if (commonFlag && goodsFlag) {
            return ResponseResult.success();
        }
        return ResponseResult.fail();
    }

    /**
     * 分页查询平台礼包商品
     *
     * @param param 包含查询参数和分页信息的对象
     * @return 分页查询结果
     */
    @Override
    public ResponseResult selectPlatformGiftGoods(GiftGoodsDTO param) {
        // 创建分页对象并设置分页参数
        Page<GiftGoodsVO> page = new Page<>(param.getPage(), param.getLimit());

        // 执行分页查询操作
        IPage<GiftGoodsVO> goods = this.baseMapper.selectPlatformGiftGoods(page, param);

        // 将查询结果转换为分页格式并返回成功结果
        return ResponseResult.success(PageUtils.getPage(goods));
    }


    /**
     * 添加平台礼包商品
     *
     * @param param 商品参数，包含商品信息和设置信息
     * @return 返回操作结果
     */
    @Override
    public ResponseResult addPlatformGiftGoods(GiftGoodsDTO param) {
        // 查询用户服务设置
        ResponseResult rr = userServiceClient.vkQuerySetting();
        VkQuerySettingDTO dto = JSON.parseObject(JSON.toJSONString(rr.getData()), VkQuerySettingDTO.class);
        // 判断是否开启购买礼包商品功能
        if (dto.getApplyType() == 1) {
            return ResponseResult.fail(BizCodeEnume.NO_OPEN_BUY_GIFT_GOODS);
        }
        // 设置固定奖励和礼品商品标识
        param.setFixedRewards(dto.getFixedRewards());
        param.setIsGiftProducts(1);
        baseMapper.updatePlatformGiftGoods(param);
        return ResponseResult.success();
    }

    /**
     * 移除平台礼包商品
     *
     * @param param 商品参数，用于更新商品信息
     * @return 返回操作结果
     */
    @Override
    public ResponseResult removePlatformGiftGoods(GiftGoodsDTO param) {
        // 清除固定奖励和礼品商品标识
        param.setFixedRewards(BigDecimal.ZERO);
        param.setIsGiftProducts(0);
        baseMapper.updatePlatformGiftGoods(param);
        return ResponseResult.success();
    }

    /**
     * 查询平台商品规格列表
     *
     * @param param 查询参数，包含商品ID列表
     * @return 返回商品规格列表
     */
    @Override
    public ResponseResult queryPlatformGoodsSpecList(ManageCommonIdsDTO param) {
        List<ManageGoodsBaseListVO> goodsBases = goodsBaseMapper.selectByGoodsList(param.getCommonId());
        return ResponseResult.success(goodsBases);
    }

    /**
     * 保存商品数据
     * 该方法通过事务处理一系列商品数据保存操作，包括商品基本信息、图片、详情等
     *
     * @param record 商品数据传输对象，包含商品的各种详细信息
     * @param param  附加参数对象，包含运输区域ID等信息
     * @return 返回保存后的商品ID
     */
    @Transactional
    public Integer saveData(ImportGoodsDataBeanVO record, GoodsBatchCommonVO param) {
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        Integer commonId = null;
        // 导入SPU基本信息
        GdbMerchantGoodsCommonVO recordGoodsCommon = record.getGdbMerchantGoodsCommon();
        recordGoodsCommon.setTransportAreaId(param.getTransportAreaId());
        recordGoodsCommon.setTransportTemplate(param.getTransportTemplate());
        recordGoodsCommon.setCommonProperty("[]");
        recordGoodsCommon.setVirtualGoods(1);
        this.baseMapper.importDataGoodsCommon(recordGoodsCommon);
        commonId = recordGoodsCommon.getCommonId();


        // 处理商品详情图片链接
        String commonDetailImage = record.getImageName();
        if (StringUtils.isNotEmpty(commonDetailImage)) {
            String[] commonDetailImages = commonDetailImage.split("\\;");
            StringBuffer sb = new StringBuffer();
            for (String tempImagePath : commonDetailImages) {
                if (StringUtils.isNotEmpty(tempImagePath)) {
                    String temp = "<p><img class=\"wscnph\" src=\"".concat(tempImagePath).concat("\" />");
                    sb.append(temp);
                }
            }
            GoodsCommonDetailEntity entity = new GoodsCommonDetailEntity();
            entity.setCommonId(commonId);
            entity.setCommonBody(sb.toString());
            goodsCommonDetailMapper.insert(entity);
        }
        Integer commonStock = 0;
        List<Map<String, Object>> commonSpecValues = new ArrayList<>();
        List<GdbMerchantGoodsBaseVO> recordGoodsBases = record.getGdbMerchantGoodsBases();
        if (null != recordGoodsBases && !recordGoodsBases.isEmpty()) {
            for (GdbMerchantGoodsBaseVO base : recordGoodsBases) {
                base.setCommonId(commonId);
                base.setShopId(Integer.valueOf(recordGoodsCommon.getShopId()));
                base.setShopName(recordGoodsCommon.getShopName());
                base.setIsUse(1);
                base.setCatId(recordGoodsCommon.getCatId());
                base.setGoodsAlarm(100);
                base.setGoodsMaxSale(base.getGoodsStock());
                base.setGoodsImage(base.getGoodsImages().get(0).getImagesImage());
                commonStock += base.getGoodsStock();
                goodsBaseMapper.insertSelective(base);
                Integer goodsId = this.baseMapper.getByGoodsId();
                //将数据更新到商品库存表
                GoodsStockEntity goodsStockEntity = new GoodsStockEntity();
                goodsStockEntity.setGoodsId(goodsId);
                goodsStockEntity.setCommonId(commonId);
                goodsStockEntity.setGoodsStock(base.getGoodsStock());
                goodsStockEntity.setGoodsAlarm(base.getGoodsAlarm());
                goodsStockEntity.setCreateTime(new Date());
                goodsStockEntity.setCreateUser(userInfo.getUserId());
                goodsStockService.save(goodsStockEntity);
                Map<String, Object> specValue = new HashMap<>();
                specValue.put("commonId", commonId);
                specValue.put("goodsId", goodsId);
                commonSpecValues.add(specValue);
                // 插入SKU商品主图
                List<GdbMerchantGoodsImagesVO> imageRecords = base.getGoodsImages();
                if (null != imageRecords && !imageRecords.isEmpty()) {
                    for (GdbMerchantGoodsImagesVO imageRecord : imageRecords) {
                        // 文件名不为空且图片是链接
                        if (StringUtils.isNotEmpty(imageRecord.getImagesImage())) {
                            imageRecord.setCommonId(commonId);
                            imageRecord.setShopId(recordGoodsCommon.getShopId());
                            imageRecord.setGoodsId(base.getGoodsId());
                            goodsImagesMapper.insertSelective(imageRecord);
                        }
                    }
                }
            }
        }
        if (null != recordGoodsBases && recordGoodsBases.size() > 0) {
            this.baseMapper.update(null, new LambdaUpdateWrapper<GoodsCommonEntity>()
                    .set(GoodsCommonEntity::getCommonStock, commonStock)
                    .set(GoodsCommonEntity::getCommonSpecValue, JSON.toJSONString(commonSpecValues))
                    .set(GoodsCommonEntity::getGoodsId, recordGoodsBases.get(0).getGoodsId())
                    .eq(GoodsCommonEntity::getCommonId, commonId));
        }
        return commonId;
    }

    /**
     * 读取Excel文件
     * 根据Excel文件类型创建相应的工作簿对象，然后进行Excel数据解析
     *
     * @param is       文件输入流
     * @param isExcels 是否为Excel文件
     * @return 解析后的Excel数据Map
     * @throws Exception 解析过程中可能抛出的异常
     */
    public Map<String, Object> readExcel(InputStream is, boolean isExcels, String shopId) throws Exception {
        Workbook wookbook = null;
        if (isExcels) {
            wookbook = new XSSFWorkbook(is);
        } else {
            wookbook = new HSSFWorkbook(is);
        }
        return analysisExcel(wookbook, shopId);
    }


    /**
     * wookbook 直接解析成数据
     *
     * @param wookbook
     * @return
     */
    private Map<String, Object> analysisExcel(Workbook wookbook, String shopId) {
        if (null != wookbook) {
            boolean flag = false;
            Map<String, Object> resultMap = new HashMap<>();
            // 将goodscommon的数据存储
            Map<String, ImportGoodsDataBeanVO> goodsCommonMap = new HashMap<String, ImportGoodsDataBeanVO>();
            Sheet sheet = wookbook.getSheetAt(0);
            int rows = sheet.getPhysicalNumberOfRows();
            Map<Integer, List<String>> errorMsg = new HashMap<Integer, List<String>>();
            // 相同编码 对应的行号
            Map<String, List<Integer>> codeNumberRow = new HashMap<>();
            // 用于记录 不相同的规格的 编码，最后统一删除spu
            List<String> errorSpec = new ArrayList<String>();

            for (int i = 1; i < rows; i++) {
                logger.info("begin analys Excel row number =" + i);
                Row row = sheet.getRow(i);
                if (null == row || row.getPhysicalNumberOfCells() == 0) {
                    continue;
                }

                Map<String, String> importMap = new HashMap<String, String>();
                // 商品名称
                String goodsName = "";
                // 默认每一行都有错误，如果没有错误 errorMsg 长度为0
                List<String> errorRow = new ArrayList<>();
                if (row != null) {

                    // 取值编码 存储到 goodsCommonMap里面
                    Cell codeNumberCell = row.getCell(0);
                    String codeNumber = ExcelsUtil.getValue(codeNumberCell);
                    // 编码为空 退出当前循环
                    if (StringUtils.isEmpty(codeNumber)) {
                        logger.info("编码为空");
                        continue;
                    }
                    List<Integer> codeNumberRows = null;
                    if (codeNumberRow.isEmpty() || null == codeNumberRow.get(codeNumber)
                            || codeNumberRow.get(codeNumber).isEmpty()) {
                        codeNumberRows = new ArrayList<Integer>();
                    } else {
                        codeNumberRows = codeNumberRow.get(codeNumber);
                    }
                    codeNumberRows.add(i);
                    codeNumberRow.put(codeNumber, codeNumberRows);
                    // 第一列的编码作为key值，key值不存在，保存
                    if (!goodsCommonMap.containsKey(codeNumber)) {
                        goodsCommonMap.put(codeNumber, new ImportGoodsDataBeanVO());
                    }
                    ImportGoodsDataBeanVO importGoodsData = goodsCommonMap.get(codeNumber);

                    // 组装spu
                    GdbMerchantGoodsCommonVO commonRecord = new GdbMerchantGoodsCommonVO();
                    commonRecord.setAfterService("[\"极速退款\",\"增值速开\",\"正品保证\",\"售后保障\"]");
                    // shopid
                    String shopName = this.baseMapper.selectShopName(Integer.valueOf(shopId));
                    commonRecord.setShopId(shopId);
                    commonRecord.setShopName(shopName);

                    // 商品名称
                    Cell goodsNameCell = row.getCell(1);
                    goodsName = ExcelsUtil.getValue(goodsNameCell);
                    if (StringUtils.isNotEmpty(goodsName)) {
                        if (goodsName.length() <= 300) {
                            commonRecord.setCommonName(goodsName);
                        } else {
                            errorRow.add("商品名称;商品名称长度不能超过300个字符");
                            goodsName = "";
                        }
                    } else {
                        errorRow.add("商品名称;商品名称不能为空");
                    }

                    // 解析三级类目
                    Cell threeCatIdCell = row.getCell(4);
                    String threeCatId = ExcelsUtil.getValue(threeCatIdCell);
                    Map<String, Object> catMap = new HashMap<>();
                    if (StringUtils.isNotEmpty(threeCatId) && MathUtil.checkNumber(threeCatId)) {
                        catMap = getCatName(Integer.parseInt(threeCatId));
                        if (null != catMap.get("catName")) {
                            commonRecord.setCatId(Integer.parseInt(threeCatId));
                            commonRecord.setCatName((String) catMap.get("catName"));
                        } else {
                            // 三级类目有错误的
                            errorRow.add("三级类目;三级类目有误");
                        }
                    } else {
                        errorRow.add("三级类目;三级类目不能为空");
                    }

                    // 规格类型
                    Cell specParamCell = row.getCell(10);
                    String specParamStr = ExcelsUtil.getValue(specParamCell);
                    if (StringUtils.isNotEmpty(specParamStr)) {
                        String[] specParams = null;
                        if (specParamStr.indexOf("+") >= 0) {
                            specParams = specParamStr.split("\\+");
                        } else {
                            specParams = new String[1];
                            specParams[0] = specParamStr;
                        }
                        // 开始循环获取属性
                        int sort = 1;
                        List<SpecParamJsonBeanVO> specParamJsonBeans = new ArrayList<>();
                        for (String specParam : specParams) {
                            // 是否是公有属性
                            SpecParamJsonBeanVO jsonBean = new SpecParamJsonBeanVO();
                            jsonBean.setParamName(specParam);
                            jsonBean.setSort(sort);
                            specParamJsonBeans.add(jsonBean);
                            sort++;
                        }
                        if (!specParamJsonBeans.isEmpty()) {
                            commonRecord.setSpecParam(JSON.toJSONString(specParamJsonBeans));
                        }

                        // 处理spu成本价格
                        Cell commonCostCell = row.getCell(6);
                        String commonCostStr = ExcelsUtil.getValue(commonCostCell);
                        if (StringUtils.isNotEmpty(commonCostStr)) {
                            if (MathUtil.checkNumber(commonCostStr)) {
                                BigDecimal commonPrice = new BigDecimal(commonCostStr);
                                commonRecord.setCommonCostPrice(commonPrice);
                            } else {
                                errorRow.add("成本价（供货价）;成本价（供货价）格式不正确");
                            }
                        } else {
                            errorRow.add("成本价（供货价）;成本价（供货价）不能为空");
                        }

                        // 处理spu商品价
                        Cell commonPriceCell = row.getCell(7);
                        String commonPriceStr = ExcelsUtil.getValue(commonPriceCell);
                        if (StringUtils.isNotEmpty(commonPriceStr)) {
                            if (MathUtil.checkNumber(commonPriceStr)) {
                                BigDecimal commonPrice = new BigDecimal(commonPriceStr);
                                commonRecord.setCommonPrice(commonPrice);
                            } else {
                                errorRow.add("商品价 格式不正确");
                            }
                        } else {
                            errorRow.add("商品价 不能为空");
                        }
                        // 处理spu市场价
                        Cell commonMarketPriceCell = row.getCell(8);
                        String commonMarketPriceCellStr = ExcelsUtil.getValue(commonMarketPriceCell);
                        if (StringUtils.isNotEmpty(commonMarketPriceCellStr)) {
                            if (MathUtil.checkNumber(commonMarketPriceCellStr)) {
                                BigDecimal commonPrice = new BigDecimal(commonMarketPriceCellStr);
                                commonRecord.setCommonMarketPrice(commonPrice);
                            } else {
                                errorRow.add("市场价 格式不正确");
                            }
                        } else {
                            errorRow.add("市场价 不能为空");
                        }
                        //品牌id
                        commonRecord.setBrandId(0);


                        // 处理sku商品图片
                        Cell goodsImagesCell1 = row.getCell(12);
                        String goodsImages = ExcelsUtil.getValue(goodsImagesCell1);
                        if (StringUtils.isNotEmpty(goodsImages)) {
                            // 设置主图，默认第一张图片
                            commonRecord.setCommonImage(goodsImages);
                        } else {
                            errorRow.add("商品图片1;商品图片1不能为空");
                        }
                        // 这里是商品详情页 也是需要保存到oss上面的 商品详情页 图片内容
                        Cell commonDetailCell1 = row.getCell(17);
                        if (null != commonDetailCell1 && StringUtils.isNotEmpty(ExcelsUtil.getValue(commonDetailCell1))) {
                            importMap.put("6", ExcelsUtil.getValue(commonDetailCell1));
                        }
                        Cell commonDetailCell2 = row.getCell(18);
                        if (null != commonDetailCell2 && StringUtils.isNotEmpty(ExcelsUtil.getValue(commonDetailCell2))) {
                            importMap.put("7", ExcelsUtil.getValue(commonDetailCell2));
                        }
                        Cell commonDetailCell3 = row.getCell(19);
                        if (null != commonDetailCell3 && StringUtils.isNotEmpty(ExcelsUtil.getValue(commonDetailCell3))) {
                            importMap.put("8", ExcelsUtil.getValue(commonDetailCell3));
                        }
                        Cell commonDetailCell4 = row.getCell(20);
                        if (null != commonDetailCell4 && StringUtils.isNotEmpty(ExcelsUtil.getValue(commonDetailCell4))) {
                            importMap.put("9", ExcelsUtil.getValue(commonDetailCell4));
                        }
                        Cell commonDetailCell5 = row.getCell(21);
                        if (null != commonDetailCell5 && StringUtils.isNotEmpty(ExcelsUtil.getValue(commonDetailCell5))) {
                            importMap.put("10", ExcelsUtil.getValue(commonDetailCell5));
                        }
                        List<Cell> commonDetailCells = new ArrayList<>();
                        commonDetailCells.add(commonDetailCell1);
                        commonDetailCells.add(commonDetailCell2);
                        commonDetailCells.add(commonDetailCell3);
                        commonDetailCells.add(commonDetailCell4);
                        commonDetailCells.add(commonDetailCell5);
                        Map<String, String> commonDetailMap = setGdbMerchantGoodsCommonDetailMap(commonDetailCells);
                        String imageHttpUrl = commonDetailMap.get("httpUrl");


                        // http商详链接
                        if (StringUtils.isNotEmpty(imageHttpUrl)) {
                            importGoodsData.setImageName(imageHttpUrl);
                        }
                    } else {
                        errorRow.add("规格类型;规格类型不能为空");
                    }

                    // 处理sku
                    GdbMerchantGoodsBaseVO recordGoodsBase = new GdbMerchantGoodsBaseVO();
                    // 处理sku商品图片
                    Cell goodsImagesCell1 = row.getCell(12);
                    if (null != goodsImagesCell1 && StringUtils.isNotEmpty(ExcelsUtil.getValue(goodsImagesCell1))) {
                        importMap.put("1", ExcelsUtil.getValue(goodsImagesCell1));
                    }
                    Cell goodsImagesCell2 = row.getCell(13);
                    if (null != goodsImagesCell2 && StringUtils.isNotEmpty(ExcelsUtil.getValue(goodsImagesCell2))) {
                        importMap.put("2", ExcelsUtil.getValue(goodsImagesCell2));
                    }
                    Cell goodsImagesCell3 = row.getCell(14);
                    if (null != goodsImagesCell3 && StringUtils.isNotEmpty(ExcelsUtil.getValue(goodsImagesCell3))) {
                        importMap.put("3", ExcelsUtil.getValue(goodsImagesCell3));
                    }
                    Cell goodsImagesCell4 = row.getCell(15);
                    if (null != goodsImagesCell4 && StringUtils.isNotEmpty(ExcelsUtil.getValue(goodsImagesCell4))) {
                        importMap.put("4", ExcelsUtil.getValue(goodsImagesCell4));
                    }
                    Cell goodsImagesCell5 = row.getCell(16);
                    if (null != goodsImagesCell5 && StringUtils.isNotEmpty(ExcelsUtil.getValue(goodsImagesCell5))) {
                        importMap.put("5", ExcelsUtil.getValue(goodsImagesCell5));
                    }
                    List<Cell> goodsImageCells = new ArrayList<>();
                    goodsImageCells.add(goodsImagesCell1);
                    goodsImageCells.add(goodsImagesCell2);
                    goodsImageCells.add(goodsImagesCell3);
                    goodsImageCells.add(goodsImagesCell4);
                    goodsImageCells.add(goodsImagesCell5);
                    List<GdbMerchantGoodsImagesVO> goodsImages = setGoodsImages(goodsImageCells);

                    if (!goodsImages.isEmpty()) {
                        recordGoodsBase.setGoodsImages(goodsImages);
                    }

                    if (StringUtils.isNotEmpty(goodsName)) {
                        recordGoodsBase.setGoodsName(goodsName);
                    }

                    // spu 错误
                    if (errorRow.isEmpty()) {
                        if (null == importGoodsData.getGdbMerchantGoodsCommon()) {
                            importGoodsData.setGdbMerchantGoodsCommon(commonRecord);
                        }

                        if (!importMap.isEmpty()) {
                            flag = true;
                            if (importGoodsData.getGdbMerchantGoodsCommon().getImportNumber() == null) {
                                List<Map<String, String>> imageNameList = new ArrayList<>();
                                imageNameList.add(importMap);
                                importGoodsData.getGdbMerchantGoodsCommon().setImportNumber(JSONObject.toJSONString(imageNameList));
                            } else {
                                List<Map<String, String>> oldImageNameList = JsonUtils.string2Obj(importGoodsData.getGdbMerchantGoodsCommon().getImportNumber(), List.class, Map.class);
                                oldImageNameList.add(importMap);
                                importGoodsData.getGdbMerchantGoodsCommon().setImportNumber(JSONObject.toJSONString(oldImageNameList));
                            }
                        }
                    }


                    // 商家编号
                    Cell merchantCodeCell = row.getCell(2);
                    String merchantCodeStr = ExcelsUtil.getValue(merchantCodeCell);
                    if (StringUtils.isNotEmpty(merchantCodeStr)) {
                        recordGoodsBase.setMerchantCode(merchantCodeStr);
                    }
                    // 商品条型码
                    Cell commodityBarTypeCodeCell = row.getCell(3);
                    String commodityBarTypeCodeCellStr = ExcelsUtil.getValue(commodityBarTypeCodeCell);
                    if (StringUtils.isNotEmpty(commodityBarTypeCodeCellStr)) {
                        recordGoodsBase.setCommodityBarTypeCode(commodityBarTypeCodeCellStr);
                    }
                    // 商品重量(kg) 6
                    Cell goodsCubageCell = row.getCell(5);
                    String goodsCubageStr = ExcelsUtil.getValue(goodsCubageCell);
                    if (StringUtils.isNotEmpty(goodsCubageStr)) {
                        if (MathUtil.checkNumber(goodsCubageStr)) {
                            BigDecimal goodsCubage = new BigDecimal(goodsCubageStr);
                            recordGoodsBase.setGoodsCubage(goodsCubage);
                        } else {
                            errorRow.add("商品重量;商品重量格式不正确");
                        }

                    } else {
                        errorRow.add("商品重量;商品重量不能为空");
                    }
                    // 体积（m3） 7
                    recordGoodsBase.setGoodsVolume(BigDecimal.ZERO);


                    // 成本价（供货价） 9
                    Cell goodsCostPriceCell = row.getCell(6);
                    String goodsCostPriceStr = ExcelsUtil.getValue(goodsCostPriceCell);
                    if (StringUtils.isNotEmpty(goodsCostPriceStr)) {
                        if (MathUtil.checkNumber(goodsCostPriceStr)) {
                            BigDecimal goodsCostPrice = new BigDecimal(goodsCostPriceStr);
                            recordGoodsBase.setGoodsCostPrice(goodsCostPrice);
                        } else {
                            errorRow.add("成本价（供货价）;成本价（供货价）格式不正确");
                        }
                    } else {
                        errorRow.add("成本价（供货价）;成本价（供货价）不能为空");
                    }
                    // sku市场价格
                    BigDecimal goodsMarketPrice = BigDecimal.ZERO;
                    // 商品价格（直播价）
                    BigDecimal goodsPrice = BigDecimal.ZERO;
                    // 商品价格（直播价） 10
                    Cell goodsPriceCell = row.getCell(7);
                    String goodsPriceStr = ExcelsUtil.getValue(goodsPriceCell);
                    if (StringUtils.isNotEmpty(goodsPriceStr)) {
                        if (MathUtil.checkNumber(goodsPriceStr)) {
                            goodsPrice = new BigDecimal(goodsPriceStr);
                            recordGoodsBase.setGoodsPrice(goodsPrice);
                        } else {
                            errorRow.add("商品价格（直播价）;成本价（供货价）格式不正确");
                        }

                    } else {
                        errorRow.add("商品价格（直播价）;成本价（供货价）不能为空");
                    }
                    // 市场价（销售价） 9
                    Cell goodsMarketPriceCell = row.getCell(8);
                    String goodsMarketPriceStr = ExcelsUtil.getValue(goodsMarketPriceCell);
                    if (StringUtils.isNotEmpty(goodsMarketPriceStr)) {
                        if (MathUtil.checkNumber(goodsMarketPriceStr)) {
                            goodsMarketPrice = new BigDecimal(goodsMarketPriceStr);
                            recordGoodsBase.setGoodsMarketPrice(goodsMarketPrice);
                        } else {
                            errorRow.add("市场价（销售价）;市场价（销售价）格式不正确");
                        }

                    } else {
                        errorRow.add("市场价（销售价）;市场价（销售价）不能为空");
                    }

                    // 商品库存 10
                    Cell goodsStockCell = row.getCell(9);
                    String goodsStockStr = ExcelsUtil.getValue(goodsStockCell);
                    if (StringUtils.isNotEmpty(goodsStockStr)) {
                        if (MathUtil.checkNumber(goodsStockStr)) {
                            recordGoodsBase.setGoodsStock(Integer.parseInt(goodsStockStr));
                        } else {
                            errorRow.add("商品库存;商品库存 格式不正确");
                        }
                    } else {
                        errorRow.add("商品库存;商品库存不能为空");
                    }
                    // 销售规格值 12
                    Cell goodsSpecCell = row.getCell(11);
                    String goodsSpecStr = ExcelsUtil.getValue(goodsSpecCell);
                    if (StringUtils.isNotEmpty(goodsSpecStr)) {
                        recordGoodsBase.setGoodsSpec(goodsSpecStr);
                        if (null != importGoodsData.getGdbMerchantGoodsCommon()) {
                            // 开始拼接 spec_value
                            String specParamJson = importGoodsData.getGdbMerchantGoodsCommon().getSpecParam();
                            if (StringUtils.isNotEmpty(specParamJson)) {
                                Map<String, String> tempMap = getSpecValueMap(goodsSpecStr, specParamJson);
                                if (null != tempMap.get("error") && StringUtils.isNotEmpty(tempMap.get("error"))) {
                                    errorRow.add("销售规格值;销售规格和规格类型数量不一致");
                                    errorSpec.add(codeNumber);
                                } else {
                                    if (StringUtils.isNotEmpty(tempMap.get("specParamJsonObject"))) {
                                        importGoodsData.getGdbMerchantGoodsCommon()
                                                .setSpecParam(tempMap.get("specParamJsonObject"));
                                    }
                                    if (StringUtils.isNotEmpty(tempMap.get("goodBaseSpecValuesJsonBean"))) {
                                        recordGoodsBase.setSpecValue(tempMap.get("goodBaseSpecValuesJsonBean"));
                                    }
                                }
                            }
                        }
                    } else {
                        errorRow.add("销售规格值;销售规格值不能为空");
                    }

                    // 刚开始，goodsbase 为null,需要创建设置
                    if (null == importGoodsData.getGdbMerchantGoodsBases()) {
                        importGoodsData.setGdbMerchantGoodsBases(new ArrayList<GdbMerchantGoodsBaseVO>());
                    }
                    // sku 里面不加入
                    if (errorRow.isEmpty() && !errorSpec.contains(codeNumber)) {
                        List<GdbMerchantGoodsBaseVO> gdbMerchantGoodsBase = importGoodsData.getGdbMerchantGoodsBases();
                        gdbMerchantGoodsBase.add(recordGoodsBase);
                        importGoodsData.setGdbMerchantGoodsBases(gdbMerchantGoodsBase);
                        goodsCommonMap.put(codeNumber, importGoodsData);
                    }
                }
                if (!errorRow.isEmpty()) {
                    errorMsg.put(i, errorRow);
                    logger.info("row number =" + i + JSON.toJSONString(errorRow));
                }
            }
            // 删除规格 数量 不匹配已经加入到map里面的
            for (String number : errorSpec) {
                List<Integer> errorNumberRows = codeNumberRow.get(number);
                if (null != errorNumberRows && !errorNumberRows.isEmpty()) {
                    for (Integer errorNumberRow : errorNumberRows) {
                        if (null != errorMsg.get(errorNumberRow)) {
                            List<String> msg = errorMsg.get(errorNumberRow);
                            if (null == msg || msg.isEmpty()) {
                                msg = new ArrayList<String>();
                            }
                            msg.add("规格类型;规格类型不正确");
                            errorMsg.put(errorNumberRow, msg);
                        } else {
                            List<String> msg = errorMsg.get(errorNumberRow);
                            if (null == msg || msg.isEmpty()) {
                                msg = new ArrayList<String>();
                            }
                            msg.add("规格类型;规格类型不正确");
                            errorMsg.put(errorNumberRow, msg);
                        }
                    }
                }
                goodsCommonMap.remove(number);
            }

            // 根据错误信息删除所有错误的spu
            errorMsg.forEach((key, value) -> {
                Integer rowNumber = (Integer) key;
                Row errorRow = sheet.getRow(rowNumber);
                if (null != errorRow && errorRow.getPhysicalNumberOfCells() != 0 && null != errorRow.getCell(0)) {
                    String errorCodeNumber = ExcelsUtil.getValue(errorRow.getCell(0));
                    if (StringUtils.isNotEmpty(errorCodeNumber) && goodsCommonMap.containsKey(errorCodeNumber)) {
                        goodsCommonMap.remove(errorCodeNumber);
                    }
                }
            });
            resultMap.put("goodsCommonMap", goodsCommonMap);
            resultMap.put("errorMsg", errorMsg);
            resultMap.put("isUploadUnzip", flag);
            return resultMap;
        }
        return null;
    }

    /**
     * 拼接common表中的 spec_param 和 base 表中的 spec_value
     *
     * @param goodsSpecStr  商品规格字符串，各规格值之间用+连接
     * @param specParamJson 规格参数的JSON数组字符串
     * @return 返回一个Map，包含处理结果和可能的错误信息
     */
    public Map<String, String> getSpecValueMap(String goodsSpecStr, String specParamJson) {
        // 初始化结果Map
        Map<String, String> result = new HashMap<>();
        // 将商品规格字符串按+分割成数组
        String[] goodsSpeces = goodsSpecStr.split("\\+");
        // 解析规格参数的JSON字符串为JSONArray
        JSONArray specParamJsonObject = (JSONArray) JSON.parse(specParamJson);
        // 初始化记录列表，用于存储规格参数及其值
        List<GoodBaseSpecValuesJsonBeanVO> records = new ArrayList<>();
        // 初始化处理后的规格参数列表
        List<SpecParamJsonBeanVO> resultSpecParamJsonBean = new ArrayList<>();

        // 检查规格参数数量与规格值数量是否匹配
        if (specParamJsonObject.size() != goodsSpeces.length) {
            result.put("error", "规格和规格值数量不相等");
            return result;
        }

        // 遍历规格参数和规格值，进行拼接处理
        for (int index = 0; index < goodsSpeces.length; index++) {
            // 初始化规格名称值列表
            List<String> paramNameValues = null;
            // 获取当前规格参数的JSON对象
            JSON specParamJSON = (JSON) specParamJsonObject.get(index);
            // 将JSON对象解析为规格参数对象
            SpecParamJsonBeanVO specParam = JSON.parseObject(specParamJSON.toJSONString(), SpecParamJsonBeanVO.class);

            // 检查并处理规格参数的名称值列表
            if (null != specParam.getParamNameValues()) {
                paramNameValues = specParam.getParamNameValues();
            } else {
                paramNameValues = new ArrayList<>();
            }
            // 当前规格值
            String goodsSpec = goodsSpeces[index];
            // 如果规格名称值列表中不存在当前规格值，则添加
            if (!paramNameValues.contains(goodsSpec)) {
                paramNameValues.add(goodsSpec);
            }
            // 更新规格参数对象的名称值列表
            specParam.setParamNameValues(paramNameValues);
            // 将处理后的规格参数对象添加到结果列表
            resultSpecParamJsonBean.add(specParam);

            // 初始化规格参数值记录对象
            GoodBaseSpecValuesJsonBeanVO record = new GoodBaseSpecValuesJsonBeanVO();
            // 根据规格参数对象的属性值设置记录对象的属性
            if (StringUtils.isNotEmpty(specParam.getParamName())) {
                record.setParamName(specParam.getParamName());
            }
            if (null != specParam.getIsCommon()) {
                record.setIsCommon(specParam.getIsCommon());
            }
            if (null != specParam.getRefSpecId()) {
                record.setRefSpecId(specParam.getRefSpecId());
            }
            // 设置记录对象的规格值
            record.setParamValue(goodsSpec);
            // 将记录对象添加到记录列表
            records.add(record);
        }

        // 将处理后的规格参数列表和记录列表转换为JSON字符串，并放入结果Map
        result.put("specParamJsonObject", JSON.toJSONString(resultSpecParamJsonBean));
        result.put("goodBaseSpecValuesJsonBean", JSON.toJSONString(records));
        // 返回结果Map
        return result;
    }


    /**
     * 设置商品详情页
     * 该方法旨在处理商品详情页的信息展示问题，通过解析给定的cell列表，区分图片链接和图片名称，
     * 并将其整理到一个Map中，以便后续在商品详情页上进行展示。
     *
     * @param cells 一个包含商品信息的cell列表，每个cell包含一段路径，可能是图片的http链接或图片名称。
     * @return 返回一个Map，包含两个键值对，"httpUrl"键对应所有图片的http链接串联成的字符串，
     * "imageName"键对应所有图片名称串联成的字符串。
     */
    public Map<String, String> setGdbMerchantGoodsCommonDetailMap(List<Cell> cells) {
        // 初始化结果Map
        Map<String, String> result = new HashMap<String, String>();
        // 用于存储所有http链接的字符串
        String httpUrl = "";
        // 用于存储所有图片名称的字符串
        String imageName = "";
        // 检查输入的cell列表是否为空
        if (null != cells && !cells.isEmpty()) {
            // 遍历cell列表
            for (Cell cell : cells) {
                // 获取cell中的路径信息
                String path = ExcelsUtil.getValue(cell);
                // 判断路径是否为http或https链接
                if (path.startsWith("http://") || path.startsWith("https://")) {
                    // 是http链接，添加到httpUrl中
                    httpUrl += path + ";";
                } else {
                    // 非http链接，认为是图片名称，添加到imageName中
                    imageName += path + ";";
                }
            }
        }
        // 将整理好的http链接和图片名称分别存入结果Map
        result.put("httpUrl", httpUrl);
        result.put("imageName", imageName);
        // 返回结果Map
        return result;
    }


    /**
     * 设置商品图片 在商品详情页 图片列表
     * <p>
     * 通过解析Excel表格中的图片路径信息，设置商品图片列表
     * 主要功能包括初始化图片存储列表、遍历Excel单元格信息、
     * 设置并返回商品图片列表
     *
     * @param cells Excel表格中的单元格列表，包含图片路径信息
     * @return 返回初始化的商品图片列表，若输入为空则返回null
     */
    public List<GdbMerchantGoodsImagesVO> setGoodsImages(List<Cell> cells) {
        // 判断输入的Excel单元格列表是否非空且不为空
        if (null != cells && !cells.isEmpty()) {
            // 初始化商品图片列表
            List<GdbMerchantGoodsImagesVO> goodsImages = new ArrayList<>();
            // 遍历Excel表格中的每个单元格
            for (Cell cell : cells) {
                // 获取单元格中的图片路径信息
                String path = ExcelsUtil.getValue(cell);
                // 初始化商品图片实体对象
                GdbMerchantGoodsImagesVO recordImage = new GdbMerchantGoodsImagesVO();
                // 设置图片显示顺序
                recordImage.setImagesDisplayorder(0);
                // 设置图片默认状态
                recordImage.setImagesIsDefault(1);
                // 设置图片路径
                recordImage.setImagesImage(path);
                // 将初始化的商品图片实体对象添加到商品图片列表中
                goodsImages.add(recordImage);
            }
            // 返回初始化的商品图片列表
            return goodsImages;
        }
        // 若输入的Excel单元格列表为空，则返回null
        return null;
    }

    /**
     * 导入zip文件进行OSS上传
     *
     * @param mapByte 包含文件名和对应内容的字节映射
     * @return 上传结果映射，包含commonID和对应的OSS图片URL
     */
    public Map<String, Map<String, Object>> importUploadOSS(Map<String, byte[]> mapByte, List<String> commonList) {

        // 初始化返回映射
        Map<String, Map<String, Object>> mapOut = new HashMap<>();
        // 遍历上传图片
        Map<String, Object> imagesMap = new HashMap<>();
        for (String key : mapByte.keySet()) {
            // 提取文件名
            String photoName = key.substring(key.indexOf("/") + 1);
            OssUploadUpdateParamDTO ossUploadUpdateParam = new OssUploadUpdateParamDTO();
            ossUploadUpdateParam.setBase64Str(Base64.getEncoder().encodeToString(mapByte.get(key)));
            ossUploadUpdateParam.setStreamName(photoName);
            ResponseResult resp = uploadToOss(ossUploadUpdateParam);
            // 获取上传后的图片URL
            String ossImageUrl = resp.getData().toString();
            // 记录上传结果
            imagesMap.put(photoName, ossImageUrl);
        }
        // 遍历入参的CommonList获取每个commonID
        for (int i = 0; i < commonList.size(); i++) {
            String commonIdStr = commonList.get(i);
            Integer commonId = Integer.valueOf(commonIdStr);
            // 通过commonID查询数据库获取导入编号
            String imageName = this.baseMapper.selectById(commonId).getImportNumber();
            // 初始化当前commonID的参数映射
            Map<String, Object> paramMap = new HashMap<>();
            // 检查imageName是否非空
            if (org.apache.commons.lang.StringUtils.isNotEmpty(imageName)) {
                // 将imageName解析成图片名称列表
                List<Map<String, String>> oldImageNameList = JsonUtils.string2Obj(imageName, List.class, Map.class);
                // 初始化图片名称列表
                LinkedHashSet<String> listString = new LinkedHashSet<>();
                // 遍历图片名称Map转换成List
                for (Map<String, String> stringStringMap : oldImageNameList) {
                    for (String key : stringStringMap.keySet()) {
                        listString.add(stringStringMap.get(key));
                    }
                }
                // 遍历图片名称列表
                for (String pictureName : listString) {
                    // 记录上传结果
                    paramMap.put(pictureName, imagesMap.get(pictureName));
                }
            }
            // 将当前commonID的上传结果添加到返回映射
            mapOut.put(String.valueOf(commonId), paramMap);
        }
        // 调用数据库操作更新图片信息
        this.updateImagesCommon(mapOut);
        // 返回上传结果映射
        return mapOut;
    }


    /**
     * 业务代码操作
     *
     * @param map
     */
    @Transactional
    public void updateImagesCommon(Map<String, Map<String, Object>> map) {

        if (map != null) {
            Map<String, Object> mapList = new HashMap<>();
            for (String commonId : map.keySet()) {
                Map<String, Object> varMap = map.get(commonId);
                for (String varKey : varMap.keySet()) {
                    // 根据commonId  图片名称 修改图片 URL
                    //更新 goods_common 表
                    this.baseMapper.update(null, new LambdaUpdateWrapper<GoodsCommonEntity>()
                            .eq(GoodsCommonEntity::getCommonId, commonId)
                            .eq(GoodsCommonEntity::getCommonImage, varKey)
                            .set(GoodsCommonEntity::getCommonImage, varMap.get(varKey)));
                    //更新 goods_base 表
                    goodsBaseMapper.update(null, new LambdaUpdateWrapper<GoodsBaseEntity>()
                            .eq(GoodsBaseEntity::getCommonId, commonId)
                            .eq(GoodsBaseEntity::getGoodsImage, varKey)
                            .set(GoodsBaseEntity::getGoodsImage, varMap.get(varKey)));

                    goodsImagesMapper.update(null, new LambdaUpdateWrapper<GoodsImagesEntity>()
                            .eq(GoodsImagesEntity::getCommonId, commonId)
                            .eq(GoodsImagesEntity::getImagesImage, varKey)
                            .set(GoodsImagesEntity::getImagesImage, varMap.get(varKey)));
                }

                // 通过commonID查询数据库获取导入编号
                String imageName = this.baseMapper.selectById(commonId).getImportNumber();
                if (StringUtils.isNotEmpty(imageName)) {
                    // 将imageName解析成图片名称列表
                    List<Map<String, String>> oldImageNameList = JsonUtils.string2Obj(imageName, List.class, Map.class);
                    for (Map<String, String> stringStringMap : oldImageNameList) {
                        for (String key : stringStringMap.keySet()) {
                            if ("6".equals(key) || "7".equals(key) || "8".equals(key) || "9".equals(key) || "10".equals(key)) {
                                logger.info("详情图操作");
                                if (org.springframework.util.StringUtils.isEmpty(mapList.get(commonId))) {
                                    mapList.put(commonId, "<p><img class=\"wscnph\" src=\"".concat(varMap.get(stringStringMap.get(key)).toString()).concat("\" />"));
                                } else {
                                    //取出已经存在的url值
                                    String imagesUrl = mapList.get(commonId).toString();
                                    mapList.put(commonId, imagesUrl.concat("<p><img class=\"wscnph\" src=\"").concat(varMap.get(stringStringMap.get(key)).toString()).concat("\" />"));
                                }
                            }
                        }
                    }
                }
            }
            if (mapList.size() > 0) {
                //遍历map
                for (String key : mapList.keySet()) {
                    Map<String, Object> mapOut = new HashMap<>();
                    logger.info("Key: " + key + " Value: " + mapList.get(key));
                    Integer count = 0;

                    //查询goods_common_detail是否已经存在数据
                    count = Math.toIntExact(goodsCommonDetailMapper.selectCount(new LambdaQueryWrapper<GoodsCommonDetailEntity>()
                            .eq(GoodsCommonDetailEntity::getCommonId, Integer.valueOf(key))));

                    mapOut.put("common_id", Integer.valueOf(key));
                    mapOut.put("imageUrl", mapList.get(key));
                    if (count > 0) {
                        //更新goods_common_detail表
                        goodsCommonDetailMapper.update(null, new LambdaUpdateWrapper<GoodsCommonDetailEntity>()
                                .set(GoodsCommonDetailEntity::getCommonBody, mapList.get(key).toString())
                                .eq(GoodsCommonDetailEntity::getCommonId, Integer.valueOf(key)));
                    } else {
                        //插入goods_common_detail表
                        GoodsCommonDetailEntity entity = new GoodsCommonDetailEntity();
                        entity.setCommonId(Integer.valueOf(key));
                        entity.setCommonBody(mapList.get(key).toString());
                        goodsCommonDetailMapper.insert(entity);
                    }

                }

            }
        }

    }

    /**
     * 根据三级类目 获取common 表里面的 cat_name
     *
     * @param threadCatId
     * @return
     */
    public Map<String, Object> getCatName(Integer threadCatId) {
        Map<String, Object> result = new HashMap<>();
        GoodsCatEntity threedCat = goodsCatMapper.selectById(threadCatId);
        if (null != threedCat) {
            result.put("threedCat", threedCat);
            GoodsCatEntity sendCat = goodsCatMapper.selectById(threedCat.getCatParentId());
            if (null != sendCat) {
                result.put("sendCatId", sendCat.getCatId());
                GoodsCatEntity firstCat = goodsCatMapper.selectById(sendCat.getCatParentId());
                if (null != firstCat) {
                    result.put("catName",
                            firstCat.getCatName() + ">" + sendCat.getCatName() + ">" + threedCat.getCatName());
                }
            }
        }
        return result;
    }

    /**
     * 将文件上传到阿里云OSS
     *
     * @param param 包含上传所需信息的参数对象
     * @return 上传操作的结果
     */
    public ResponseResult uploadToOss(OssUploadUpdateParamDTO param) {
        // 调用第三方服务客户端的Base64编码上传方法
        return thirdPartyServiceClient.uploadToOssBase64(param);
    }

    /**
     * 提取商品分类ID列表
     *
     * @param WebCatMenus 商品分类菜单列表
     * @return 商品分类ID列表
     */
    public List<Integer> catIds(List<WebCatMenuVO> WebCatMenus) {
        List<Integer> catIds = new ArrayList<>();
        for (WebCatMenuVO webCatMenu : WebCatMenus) {
            catIds.add(webCatMenu.getCatId());
        }
        return catIds;
    }

    /**
     * 根据商品ID查询商品详情
     *
     * @param commonId 商品ID
     * @return 商品详情信息
     */
    @Override
    public GdbMerchantGoodsCommonVO queryProductVoucherGoods(Integer commonId) {
        // 根据商品ID查询商品信息
        GdbMerchantGoodsCommonVO goodsCommonVO = this.baseMapper.selectsupplyGoodsCommonInfo(commonId);
        // 返回查询到的商品信息
        return goodsCommonVO;
    }

    /**
     * 根据通用名称查询相似商品的ID列表
     *
     * @param commonName 商品的通用名称，用于模糊查询
     * @return 返回包含商品ID的列表，这些商品的名称包含提供的通用名称
     * <p>
     * 该方法通过调用baseMapper的queryLikeGoodsName方法，实现对商品名称的模糊查询，
     * 返回所有名称中包含指定通用名称的商品的ID列表
     */
    @Override
    public List<Integer> queryLikeGoodsName(String commonName) {
        List<Integer> commonIds = this.baseMapper.queryLikeGoodsName(commonName);
        return commonIds;
    }

    /**
     * 批量修改商品物流信息
     *
     * @param param
     * @return
     */
    @Override
    public ResponseResult updateGoodsTransportInfo(UpdateGoodsTransportInfoDTO param) {
        int count = 0;
        for (Integer commonId : param.getCommonIdList()) {
            count += baseMapper.update(null, new LambdaUpdateWrapper<GoodsCommonEntity>()
                    .set(null != param.getTransportTemplate(), GoodsCommonEntity::getTransportTemplate, param.getTransportTemplate())
                    .set(null != param.getTransportAreaId(), GoodsCommonEntity::getTransportAreaId, param.getTransportAreaId())
                    .set(null != param.getInternationalTransportAreaId(), GoodsCommonEntity::getInternationalTransportArea, param.getInternationalTransportAreaId())
                    .set(null != param.getInternationalTransportTemplate(), GoodsCommonEntity::getInternationalTransportTemplate, param.getInternationalTransportTemplate())
                    .eq(GoodsCommonEntity::getCommonId, commonId));
        }
        return count > 0 ? ResponseResult.success() : ResponseResult.fail();
    }

    @Override
    public ResponseResult oneClickDropShipping(OneClickProductListingDTO oneClickProductListingDTO) {
        ResponseResult rr = ParseTokenUtils.getShopUserInfo(oneClickProductListingDTO.getToken());
        // 如果获取用户信息失败，则直接返回错误信息
        if (rr.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return rr;
        }
        UserInfoVO ui = JSON.parseObject(JSON.toJSONString(rr.getData()), UserInfoVO.class);
        Integer shopId = Integer.valueOf(ui.getShopId());
        String shopName = this.baseMapper.selectShopNameByShopId(shopId);
        List<String> goodsIdListNew = new ArrayList<>();
        //通过spuId查询是否够存在商品
        for (String spuId : oneClickProductListingDTO.getSpuIdList()) {
            goodsIdListNew.add(spuId);
            //封装查询SPU商品详情请求参数
            List<String> goodsInfo = new ArrayList<>();
            //spuId
            goodsInfo.add(spuId);
            OpenSpuInfoQueryDTO goodsDto = new OpenSpuInfoQueryDTO();
            goodsDto.setSpuIdList(goodsInfo);
            //根据spuId查询商品详情即Spu的商品信息
            ResponseResult result = yiYaTongService.getSpuBySpuIdsGoodsList(goodsDto);
            //转换为VO
            ProductSelectionCenteGoodsVO vo = JSON.parseObject(JSON.toJSONString(result.getData()), ProductSelectionCenteGoodsVO.class);

            LambdaQueryWrapper<SupplierGoodsYiyatongEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(SupplierGoodsYiyatongEntity::getSpuId, spuId);
            lambdaQueryWrapper.eq(SupplierGoodsYiyatongEntity::getIsDelete, 0);
            lambdaQueryWrapper.eq(SupplierGoodsYiyatongEntity::getShopId, shopId);
            List<SupplierGoodsYiyatongEntity> commonEntities = supplierGoodsYiyatongService.list(lambdaQueryWrapper);
            //查询是否代发过
            if (!CollectionUtils.isEmpty(commonEntities)) {
                //更新商品信息
                updateYiYaTongGoods(commonEntities, shopId, shopName, vo);

            } else {
                //新增商品想你想
                addYiYaTongGoods(ui, shopId, shopName, vo);
            }
        }

        if (goodsIdListNew.isEmpty()) {
            return ResponseResult.success(1, "全部不能代发", 3);
        } else if (goodsIdListNew.size() < oneClickProductListingDTO.getSpuIdList().size()) {
            return ResponseResult.success(1, "部分代发成功", 2);
        } else {
            return ResponseResult.success(1, "全部代发成功", 1);
        }
    }

    private ResponseResult updateYiYaTongGoods(List<SupplierGoodsYiyatongEntity> commonEntities, Integer shopId, String shopName, ProductSelectionCenteGoodsVO vo) {
        for (SupplierGoodsYiyatongEntity supplierGoodsYiyatongEntity : commonEntities) {
            GoodsCommonEntity commonEntity = this.baseMapper.selectById(supplierGoodsYiyatongEntity.getCommonId());
            if (null != commonEntity) {
                //商品名
                commonEntity.setCommonName(vo.getGoodsName());
                //商品详情图
                commonEntity.setCommonImage(vo.getGoodsImage());
                commonEntity.setCommonAddTime(new Date());
                //商品重量
                commonEntity.setCommonCubage(vo.getCommonCubage());
                commonEntity.setDealerGoodsFlag(1);
                commonEntity.setCommonPrice(new BigDecimal("0"));
                if (vo.getPropertys().size() > 0) {
                    commonEntity.setSpecParam(convertSpecParam(vo.getPropertys()));
                }
                commonEntity.setSupplierSource(2);
                //库存
                commonEntity.setCommonStock(vo.getGoodsStock());
                commonEntity.setShopId(shopId);
                commonEntity.setShopName(shopName);
                //更新商品
                this.baseMapper.updateById(commonEntity);

                goodsCommonDetailMapper.update(null, new LambdaUpdateWrapper<GoodsCommonDetailEntity>()
                        .eq(GoodsCommonDetailEntity::getCommonId, supplierGoodsYiyatongEntity.getCommonId())
                        .set(GoodsCommonDetailEntity::getCommonBody, vo.getCommonBody()));

                //封装查询SKU规格信息请求参数
                OpenSkuInfoBySpuIdDTO spuIdDTO = new OpenSkuInfoBySpuIdDTO();
                spuIdDTO.setSpuId(vo.getSupplierCommonId());
                //请求查询SKU规格信息解决
                ResponseResult skuResult = yiYaTongService.listAllSkuBySpuId(spuIdDTO);

                List<OpenSkuInfoBySpuIdVO> skuList = JSON.parseArray(JSON.toJSONString(skuResult.getData()), OpenSkuInfoBySpuIdVO.class);
                if (skuList.size() > 0) {
                    List<GoodBaseSpecValuesVO> specValuesList = new ArrayList<>();
                    commonEntity.setCommonPrice(new BigDecimal(skuList.get(0).getBasePrice()));
                    for (OpenSkuInfoBySpuIdVO sku : skuList) {
                        GoodsBaseEntity goodsBaseEntity = goodsBaseMapper.selectById(supplierGoodsYiyatongEntity.getGoodsId());
                        if (null != goodsBaseEntity) {
                            //商品名
                            goodsBaseEntity.setGoodsName(vo.getGoodsName());
                            //市场价
                            goodsBaseEntity.setGoodsMarketPrice(new BigDecimal(sku.getOfficialDistriPrice()));
                            //分销价
                            goodsBaseEntity.setGoodsPrice(new BigDecimal(sku.getBasePrice()));
                            //商品logo
                            goodsBaseEntity.setGoodsImage(sku.getSkuPicUrl());
                            goodsBaseEntity.setCommonId(commonEntity.getCommonId());
                            goodsBaseEntity.setShopId(shopId);
                            goodsBaseEntity.setShopName(shopName);

                            //建议零售价
                            goodsBaseEntity.setMsrp(sku.getSuggestPrice());
                            //起购数量
                            goodsBaseEntity.setGoodsMinSale(sku.getBuyStartQty());

                            //规格
                            if (sku.getSkuPropertyList().size() > 0) {
                                String jsonArray2 = convertSkuSpecValue(sku.getSkuPropertyList());
                                goodsBaseEntity.setSpecValue(jsonArray2);
                                goodsBaseEntity.setGoodsSpec(sku.getSkuPropertyList().get(0).getSpecValueName());
                                for (SkuSpecVO p : sku.getSkuPropertyList()) {
                                    GoodBaseSpecValuesVO dto = new GoodBaseSpecValuesVO();
                                    dto.setParamName(p.getSpecName());
                                    dto.setParamValue(p.getSpecValueName());
                                    dto.setSort(1);
                                    specValuesList.add(dto);
                                }

                            }
                            //更新商品sku
                            goodsBaseMapper.updateById(goodsBaseEntity);
                        }

                        Integer goodsId = goodsBaseEntity.getGoodsId();

                        GoodsStockEntity goodsStockEntity = goodsStockService.getOne(new LambdaQueryWrapper<GoodsStockEntity>()
                                .eq(GoodsStockEntity::getGoodsId, goodsId)
                                .eq(GoodsStockEntity::getCommonId, commonEntity.getCommonId()));
                        if (null != goodsStockEntity) {
                            goodsStockEntity.setGoodsStock(Integer.valueOf(sku.getStock()));
                            goodsStockEntity.setCreateTime(new Date());
                            goodsStockService.updateById(goodsStockEntity);
                        }
                    }
                    if (specValuesList.size() > 0) {
                        //规格转换
                        convertSpecParam2(specValuesList, commonEntity);
                    }
                }

            }
        }

        return ResponseResult.success();
    }

    private ResponseResult addYiYaTongGoods(UserInfoVO ui, Integer shopId, String shopName, ProductSelectionCenteGoodsVO vo) {
        GoodsCommonEntity commonEntity = new GoodsCommonEntity();
        commonEntity.setShopId(shopId);
        commonEntity.setShopName(shopName);
        //商品名
        commonEntity.setCommonName(vo.getGoodsName());
        //商品详情图
        commonEntity.setCommonImage(vo.getGoodsImage());
        commonEntity.setCommonAddTime(new Date());
        //商品重量
        commonEntity.setCommonCubage(vo.getCommonCubage());
        //
        commonEntity.setCommonVerify(2);
        //
        commonEntity.setDealerGoodsFlag(1);
        commonEntity.setCommonPrice(new BigDecimal("0"));
        commonEntity.setSupplierSource(2);
        //库存
        commonEntity.setCommonStock(vo.getGoodsStock());

        //新增商品
        this.save(commonEntity);
        Integer commonId = commonEntity.getCommonId();

        GoodsCommonDetailEntity commonDetailEntity = new GoodsCommonDetailEntity();
        commonDetailEntity.setCommonId(commonId);
        commonDetailEntity.setCommonBody(vo.getCommonBody());
        commonDetailEntity.setTs(new Date());
        goodsCommonDetailMapper.insert(commonDetailEntity);

        //封装查询SKU规格信息请求参数
        OpenSkuInfoBySpuIdDTO spuIdDTO = new OpenSkuInfoBySpuIdDTO();
        spuIdDTO.setSpuId(vo.getSupplierCommonId());
        //请求查询SKU规格信息解决
        ResponseResult skuResult = yiYaTongService.listAllSkuBySpuId(spuIdDTO);

        List<OpenSkuInfoBySpuIdVO> skuList = JSON.parseArray(JSON.toJSONString(skuResult.getData()), OpenSkuInfoBySpuIdVO.class);
        if (skuList.size() > 0) {
            List<GoodBaseSpecValuesVO> specValuesList = new ArrayList<>();
            commonEntity.setCommonPrice(new BigDecimal(skuList.get(0).getBasePrice()));
            for (OpenSkuInfoBySpuIdVO sku : skuList) {
                GoodsBaseEntity goodsBaseEntity = new GoodsBaseEntity();
                goodsBaseEntity.setShopId(Integer.valueOf(shopId));
                goodsBaseEntity.setShopName(shopName);
                //商品名
                goodsBaseEntity.setGoodsName(vo.getGoodsName());
                //市场价
                goodsBaseEntity.setGoodsMarketPrice(new BigDecimal(sku.getOfficialDistriPrice()));
                //分销价
                goodsBaseEntity.setGoodsPrice(new BigDecimal(sku.getBasePrice()));
                //商品logo
                goodsBaseEntity.setGoodsImage(sku.getSkuPicUrl());
                goodsBaseEntity.setCommonId(commonEntity.getCommonId());
                //建议零售价
                goodsBaseEntity.setMsrp(sku.getSuggestPrice());
                //起购数量
                goodsBaseEntity.setGoodsMinSale(sku.getBuyStartQty());
                goodsBaseEntity.setGoodsIsShelves(2);
                goodsBaseEntity.setGoodsCubage(vo.getCommonCubage());
                //规格
                if (sku.getSkuPropertyList().size() > 0) {
                    String jsonArray2 = convertSkuSpecValue(sku.getSkuPropertyList());
                    goodsBaseEntity.setSpecValue(jsonArray2);
                    goodsBaseEntity.setGoodsSpec(sku.getSkuPropertyList().get(0).getSpecValueName());
                    for (SkuSpecVO p : sku.getSkuPropertyList()) {
                        GoodBaseSpecValuesVO dto = new GoodBaseSpecValuesVO();
                        dto.setParamName(p.getSpecName());
                        dto.setParamValue(p.getSpecValueName());
                        dto.setSort(1);
                        specValuesList.add(dto);
                    }
                }
                //新增商品sku
                goodsBaseMapper.insert(goodsBaseEntity);
                Integer goodsId = goodsBaseEntity.getGoodsId();
                String goodsCode = "HOU";
                goodsCode = goodsCode + goodsId + UUIDUtil.getRandom(4);
                goodsBaseEntity.setGoodsCode(goodsCode);
                goodsBaseMapper.updateById(goodsBaseEntity);
                commonEntity.setGoodsId(goodsId.toString());
                this.baseMapper.updateById(commonEntity);
                //代发记录
                SupplierGoodsYiyatongEntity supplierGoodsYiyatongEntity = new SupplierGoodsYiyatongEntity();
                supplierGoodsYiyatongEntity.setCommonId(commonId);
                supplierGoodsYiyatongEntity.setSpuId(vo.getSupplierCommonId());
                supplierGoodsYiyatongEntity.setGoodsId(goodsId);
                supplierGoodsYiyatongEntity.setShopId(shopId);
                supplierGoodsYiyatongEntity.setSkuId(sku.getSkuId());
                supplierGoodsYiyatongEntity.setCatId(vo.getCatId());
                supplierGoodsYiyatongService.save(supplierGoodsYiyatongEntity);


                GoodsStockEntity goodsStockEntity = new GoodsStockEntity();
                goodsStockEntity.setCommonId(commonEntity.getCommonId());
                goodsStockEntity.setGoodsId(goodsId);
                goodsStockEntity.setGoodsStock(Integer.valueOf(sku.getStock()));
                goodsStockEntity.setCreateTime(new Date());
                goodsStockEntity.setCreateUser(ui.getUserId());
                goodsStockService.save(goodsStockEntity);

            }
            if (specValuesList.size() > 0) {
                //规格转换
                convertSpecParam2(specValuesList, commonEntity);
            }


        }
        return ResponseResult.success();
    }

    /**
     * 贻亚通商品SPU规格转换
     *
     * @param specValuesList
     * @return
     */

    private void convertSpecParam2(List<GoodBaseSpecValuesVO> specValuesList, GoodsCommonEntity commonEntity) {
        List<SpecParamJsonBeanDTO> result = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        for (GoodBaseSpecValuesVO specValuesVO : specValuesList) {

            if (null != map.get(specValuesVO.getParamName())) {
                List<String> paramNameValues = JSON.parseArray(map.get(specValuesVO.getParamName()), String.class);
                paramNameValues.add(specValuesVO.getParamValue());
                map.put(specValuesVO.getParamName(), JSON.toJSONString(paramNameValues));
            } else {
                List<String> paramNameValues = new ArrayList<>();
                paramNameValues.add(specValuesVO.getParamValue());
                map.put(specValuesVO.getParamName(), JSON.toJSONString(paramNameValues));
            }
        }
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            List<String> paramNameValues = JSON.parseArray(value, String.class);
            SpecParamJsonBeanDTO dto = new SpecParamJsonBeanDTO();
            dto.setParamName(key);
            dto.setIsCommon(false);
            dto.setSort(1);
            dto.setParamNameValues(paramNameValues);
            result.add(dto);

        }
        String specParam = JSONObject.toJSONString(result);
        //规格
        commonEntity.setSpecParam(specParam);
        //更新商品
        this.baseMapper.updateById(commonEntity);
    }


    /**
     * 贻亚通商品SPU规格转换
     *
     * @param list
     * @return
     */

    private String convertSpecParam(List<PropertysVO> list) {
        List<SpecParamJsonBeanDTO> result = new ArrayList<>();
        for (PropertysVO p : list) {
            SpecParamJsonBeanDTO dto = new SpecParamJsonBeanDTO();
            dto.setIsCommon(false);
            dto.setParamName(p.getPropertyName());
            List<String> paramNameValues = new ArrayList<>();
            if (p.getItems().size() > 0) {
                for (int i = 0; i < p.getItems().size(); i++) {
                    paramNameValues.add(p.getItems().get(i).getPropertyValueName());
                }
            }
            dto.setParamNameValues(paramNameValues);
            dto.setSort(1);
            result.add(dto);
        }

        String specParam = JSONObject.toJSONString(result);
        return specParam;
    }

    /**
     * 贻亚通商品SKU规格转换
     *
     * @param list
     * @return
     */
    private String convertSkuSpecValue(List<SkuSpecVO> list) {
        List<GoodBaseSpecValuesVO> result = new ArrayList<>();
        for (SkuSpecVO p : list) {
            GoodBaseSpecValuesVO dto = new GoodBaseSpecValuesVO();
            dto.setParamName(p.getSpecName());
            dto.setParamValue(p.getSpecValueName());
            dto.setSort(1);
            result.add(dto);
        }

        String specParam = JSONObject.toJSONString(result);
        return specParam;
    }

    @Override
    public ResponseResult msgCallBack(MsgCallBackDTO msgCallBackDTO) {
        logger.info("-----msgCallBackDTO:" + JSONObject.toJSONString(msgCallBackDTO));
        ResponseResult result = ResponseResult.success();
        String enumNum = msgCallBackDTO.getEnumNum();
        String content = msgCallBackDTO.getContent();
        try {
            switch (enumNum) {
                //订单状态变更通知
                case "1":
                case "9"://关联的选品库商品spu被移入选品库
                //订单支付成功通知
                case "20":
                //订单商品修改发货信息
                case "21"://
                    result = ResponseResult.success();
                    break;
                case "2":
                    // 订单商品发货通知
                    result = shipmentsYiYaTongGoodsBySpuId(content);
                    break;
                case "3":
                    // 售后订单状态变更通知
                    result = returnYiYaTongGoodsBySpuId(content);
                    break;
                //商品基础信息或商品状态变更通知
                case "4":
                //商品规格删除通知
                case "6":
                //商品规格变更通知
                case "7":
                //选品库商品spu被加入企业商品库
                case "10":
                    result = updateYiYaTongGoodsBySpuId(content);
                    break;
                //商品删除通知
                case "5":
                case "8"://关联的选品库商品spu被移出选品库
                case "11"://选品库商品spu被移出企业商品库
                    result = deleteYiYaTongGoodsBySpuId(content);
                    break;
                case "12":
                    // 企业商品库的商品sku分销价变更/企业商品库的商品sku市场价变更/企业商品库的商品sku销售指导价变更
                    result = updateYiYaTongGoodsPriceBySpuId(content);
                    break;
                case "13":
                    // 商品上下架 status：0上架；1下架
                    result = updateYiYaTongGoodsStatusBySpuId(content);
                    break;
                default:
                    result = ResponseResult.success();
                    break;
            }

            YiyatongMsgCallbackEntity msgCallbackEntity = new YiyatongMsgCallbackEntity();
            msgCallbackEntity.setMessageId(msgCallBackDTO.getMessageId());
            msgCallbackEntity.setEnumNum(msgCallBackDTO.getEnumNum());
            msgCallbackEntity.setAppKey(msgCallBackDTO.getAppKey());
            msgCallbackEntity.setContent(msgCallBackDTO.getContent());
            msgCallbackEntity.setSign(msgCallBackDTO.getSign());
            msgCallbackEntity.setCreateTime(DateUtils.getNowDateToString());
            if (result.getCode() == CommonStatusEnum.SUCCESS.getCode()) {
                msgCallbackEntity.setIsUse(1);
            } else {
                msgCallbackEntity.setIsUse(0);
            }
            msgCallbackMapper.insert(msgCallbackEntity);


        } catch (Exception e) {
            result = ResponseResult.fail("Internal server error: " + e.getMessage());
        }

        return result;
    }


    public ResponseResult deleteYiYaTongGoodsBySpuId(String content) {

        List<SpuIdDTO> list = JSONObject.parseArray(content, SpuIdDTO.class);
        for (SpuIdDTO spuIdDTO : list) {
            List<SupplierGoodsYiyatongEntity> goodsYiyatongEntities = supplierGoodsYiyatongService.list(new LambdaQueryWrapper<SupplierGoodsYiyatongEntity>()
                    .eq(SupplierGoodsYiyatongEntity::getSpuId, spuIdDTO.getSpuId())
                    .eq(SupplierGoodsYiyatongEntity::getIsDelete, 0));


            Integer commonId = goodsYiyatongEntities.get(0).getCommonId();

            ManageCommonIdsDTO dto = new ManageCommonIdsDTO();

            List<Integer> commonIds = new ArrayList<>();
            commonIds.add(commonId);
            dto.setCommonIds(commonIds);
            //删除商品
            deleteByCommonId(dto);
        }
        return ResponseResult.success();
    }

    public ResponseResult updateYiYaTongGoodsBySpuId(String content) {
        ResponseResult result = ResponseResult.success();
        List<SpuIdAndSkuIdDTO> list = JSONObject.parseArray(content, SpuIdAndSkuIdDTO.class);

        Map<Integer, String> map = new HashMap<>();
        for (SpuIdAndSkuIdDTO spuIdDTO : list) {
            LambdaQueryWrapper<SupplierGoodsYiyatongEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(SupplierGoodsYiyatongEntity::getSpuId, spuIdDTO.getSpuId());
            lambdaQueryWrapper.eq(SupplierGoodsYiyatongEntity::getIsDelete, 0);
            lambdaQueryWrapper.groupBy(SupplierGoodsYiyatongEntity::getShopId);
            List<SupplierGoodsYiyatongEntity> commonEntities = supplierGoodsYiyatongService.list(lambdaQueryWrapper);
            for (SupplierGoodsYiyatongEntity commonEntity : commonEntities) {
                if (null != map.get(commonEntity.getShopId())) {
                    List<String> spuIdList = JSON.parseArray(map.get(commonEntity.getShopId()), String.class);
                    spuIdList.add(spuIdDTO.getSpuId());
                    map.put(commonEntity.getShopId(), JSON.toJSONString(spuIdList));
                } else {
                    List<String> spuIdList = new ArrayList<>();
                    spuIdList.add(spuIdDTO.getSpuId());
                    map.put(commonEntity.getShopId(), JSON.toJSONString(spuIdList));
                }
            }
        }
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            Integer key = entry.getKey();
            String value = entry.getValue();
            List<String> spuIdList = JSON.parseArray(value, String.class);
            OneClickProductListingDTO dto = new OneClickProductListingDTO();

            String token = RedisUtil.get(HouConfig.HOU_SHOP_TOKEN_SHOP + key);

            dto.setToken(token);
            dto.setSpuIdList(spuIdList);

            result = oneClickDropShipping(dto);
        }
        return result;
    }

    public ResponseResult updateYiYaTongGoodsPriceBySpuId(String content) {
        ResponseResult result = ResponseResult.success();
        List<SpuIdAndPriceDTO> list = JSONObject.parseArray(content, SpuIdAndPriceDTO.class);
        Map<Integer, String> map = new HashMap<>();
        for (SpuIdAndPriceDTO spuIdDTO : list) {
            LambdaQueryWrapper<SupplierGoodsYiyatongEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(SupplierGoodsYiyatongEntity::getSpuId, spuIdDTO.getSpuId());
            lambdaQueryWrapper.eq(SupplierGoodsYiyatongEntity::getIsDelete, 0);
            lambdaQueryWrapper.groupBy(SupplierGoodsYiyatongEntity::getShopId);
            List<SupplierGoodsYiyatongEntity> commonEntities = supplierGoodsYiyatongService.list(lambdaQueryWrapper);
            for (SupplierGoodsYiyatongEntity commonEntity : commonEntities) {
                if (null != map.get(commonEntity.getShopId())) {
                    List<String> spuIdList = JSON.parseArray(map.get(commonEntity.getShopId()), String.class);
                    spuIdList.add(spuIdDTO.getSpuId());
                    map.put(commonEntity.getShopId(), JSON.toJSONString(spuIdList));
                } else {
                    List<String> spuIdList = new ArrayList<>();
                    spuIdList.add(spuIdDTO.getSpuId());
                    map.put(commonEntity.getShopId(), JSON.toJSONString(spuIdList));
                }
            }
        }
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            Integer key = entry.getKey();
            String value = entry.getValue();
            List<String> spuIdList = JSON.parseArray(value, String.class);
            OneClickProductListingDTO dto = new OneClickProductListingDTO();
            String token = RedisUtil.get(HouConfig.HOU_SHOP_TOKEN_SHOP + key);
            dto.setToken(token);
            dto.setSpuIdList(spuIdList);

            result = oneClickDropShipping(dto);
        }
        return result;
    }

    public ResponseResult updateYiYaTongGoodsStatusBySpuId(String content) {
        List<SpuIdAndStatusDTO> list = JSONObject.parseArray(content, SpuIdAndStatusDTO.class);
        for (SpuIdAndStatusDTO spuIdDTO : list) {
            List<SupplierGoodsYiyatongEntity> goodsYiyatongEntities = supplierGoodsYiyatongService.list(new LambdaQueryWrapper<SupplierGoodsYiyatongEntity>()
                    .eq(SupplierGoodsYiyatongEntity::getSpuId, spuIdDTO.getSpuId())
                    .eq(SupplierGoodsYiyatongEntity::getIsDelete, 0));


            Integer commonId = goodsYiyatongEntities.get(0).getCommonId();
            Integer shopId = goodsYiyatongEntities.get(0).getShopId();
            GoodsIsShelvesDTO goodsIsShelvesDTO = new GoodsIsShelvesDTO();
            List<Integer> commonIds = new ArrayList<>();
            commonIds.add(commonId);
            String token = RedisUtil.get(HouConfig.HOU_SHOP_TOKEN_SHOP + shopId);
            goodsIsShelvesDTO.setToken(token);
            goodsIsShelvesDTO.setCommonIds(commonIds);
            //上架
            if (spuIdDTO.getStatus().equals("1")) {
                setGoodsSkuOnline(goodsIsShelvesDTO);
            }
            //下架
            if (spuIdDTO.getStatus().equals("0")) {
                setGoodsSkuOffline(goodsIsShelvesDTO);
            }

        }

        return ResponseResult.success();
    }

    public ResponseResult setGoodsSkuOffline(GoodsIsShelvesDTO param) {
        // 如果没有选择需要下架的商品，则返回错误信息
        if (param.getCommonIds().isEmpty()) {
            return ResponseResult.fail(BizCodeEnume.NO_CHOICE_GOODS);
        }
        // 初始化商品ID列表和信息映射表
        Map<String, Object> map = new HashMap<>();
        // 遍历所有需要下架的商品ID
        for (Integer commonId : param.getCommonIds()) {
            // 查询该商品ID下所有SKU的状态
            List<GoodsBaseVO> goods = goodsBaseMapper.selectByOffOrOnline(1, commonId);
            // 遍历SKU，将商品ID加入列表，并将SKU状态设置为下架
            for (GoodsBaseVO good : goods) {
                Integer goodsId = good.getGoodsId();
                // 商品下架操作
                goodsBaseMapper.updateSkuIsShelves(2, goodsId);
                // 校验商品的所有SKU是否都已下架
                Integer baseCount = goodsBaseMapper.selectCommonIsShelves(commonId);
                Integer shelvesCount = goodsBaseMapper.selectGoodsIsShelves(2, commonId);
                if (baseCount.equals(shelvesCount)) {
                    // 如果是，则将商品本身状态也设置为下架
                    this.baseMapper.updateOffline(param.getCommonIds());
                }
            }
        }
        // 准备发送到MQ的信息，包括下架的商品ID列表和操作类型
        //通过mq 更新es的商品数据
        map.put("commonIdS", param.getCommonIds());
        // 1 是添加  2是删除
        map.put("type", CommonType.operateStatus.DEL.getCode());
        // 发送信息到MQ，用于更新ES中的商品数据
        MqSendUtils.mqSendGoods(map);
        // 返回操作成功的结果
        return ResponseResult.success();
    }


    /**
     * 商品上架接口
     *
     * @param param 包含商品ID和其他必要信息的请求参数
     * @return 返回操作结果
     */
    public ResponseResult setGoodsSkuOnline(GoodsIsShelvesDTO param) {
        // 通过token获取用户信息，用于后续操作权限验证
        ResponseResult rr = ParseTokenUtils.getShopUserInfo(param.getToken());
        // 如果获取用户信息失败，则直接返回失败结果
        if (rr.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return rr;
        }
        UserInfoVO ui = JSON.parseObject(JSON.toJSONString(rr.getData()), UserInfoVO.class);
        // 检查是否选择了需要上架的商品，如果没有选择则返回错误
        if (param.getCommonIds().isEmpty()) {
            return ResponseResult.fail(BizCodeEnume.NO_CHOICE_GOODS);
        }
        ManageShopBaseVO shopBaseQueryResult = merchantServiceClient.selectGdsShopBaseByShopId(Integer.valueOf(ui.getShopId()));
        // 遍历所有选中的商品ID，进行上架操作
        for (Integer commonId : param.getCommonIds()) {
            // 根据商品状态和商品ID查询需要上架的商品详情
            List<GoodsBaseVO> goods = goodsBaseMapper.selectByOffOrOnline(
                    CommonType.goodsIsShelves.GOODS_OFF_THE_SHELF.getCode(), commonId);
            // 初始化商品ID列表和操作类型信息，用于后续发送MQ消息
            Map<String, Object> map = new HashMap<>();
            // 遍历商品详情，检查并处理每个商品
            for (GoodsBaseVO good : goods) {
                if (!shopBaseQueryResult.getShopSelfSupport().equals("true") && !shopBaseQueryResult.getCatIdList().contains(good.getCatId())) {
                    throw new DefinedException(BizCodeEnume.SHOP_BUSINESS_CATEGORY_DISCREPANCY);
                }
                if (good.getCatId() == null) {
                    throw new DefinedException(BizCodeEnume.LACK_OF_CLASSIFICATION);
                }
                // 初始化查询商品询价信息的参数
                Integer goodsId = good.getGoodsId();
                // 如果商品库存为0，则返回错误，无法上架
                if (good.getGoodsStock() <= 0) {
                    throw new DefinedException(BizCodeEnume.GOODS_STOCK_IS_ZERO);
                }
                // 更新商品的上架状态
                goodsBaseMapper.updateSkuIsShelves(CommonType.goodsIsShelves.GOODS_ON_THE_SHELF.getCode(), goodsId);
                // 检查所有SKU是否都已上架，如果是，则更新商品的上架状态
                Integer baseCount = goodsBaseMapper.selectCommonIsShelves(commonId);
                Integer shelvesCount = goodsBaseMapper.selectGoodsIsShelves(CommonType.goodsIsShelves.GOODS_ON_THE_SHELF.getCode(), commonId);
                if (baseCount.equals(shelvesCount)) {
                    this.baseMapper.updateOnline(param.getCommonIds());
                }
            }
            // 通过MQ发送更新商品信息到ES的消息，用于实时索引更新
            map.put("commonIdS", param.getCommonIds());
            // 1 是添加  2是删除
            map.put("type", CommonType.operateStatus.ADD.getCode());
            MqSendUtils.mqSendGoods(map);
        }
        // 所有商品上架成功，返回成功结果
        return ResponseResult.success();
    }

    public ResponseResult shipmentsYiYaTongGoodsBySpuId(String content) {
        List<ShipmentsYiYaTongDTO> list = JSONObject.parseArray(content, ShipmentsYiYaTongDTO.class);
        ShipmentsListDTO shipmentsListDTO = new ShipmentsListDTO();
        shipmentsListDTO.setShipmentsGoodsDTOList(list);
        ResponseResult result = orderServiceClient.yiyatongShipments(shipmentsListDTO);

        return result;
    }

    public ResponseResult returnYiYaTongGoodsBySpuId(String content) {
        List<ReturnYiYaTongInfoDTO> list = JSONObject.parseArray(content, ReturnYiYaTongInfoDTO.class);
        ReturnListDTO returnListDTO = new ReturnListDTO();
        returnListDTO.setReturnList(list);
        ResponseResult result = orderServiceClient.yiyatongReturn(returnListDTO);

        return result;
    }

    @Override
    public ResponseResult updateYiyatongGoodsPrice(UpdateSupperGoodsPriceDTO param) {
        // 通过token获取用户信息，用于验证权限和获取店铺ID。
        ResponseResult rr = ParseTokenUtils.getShopUserInfo(param.getToken());
        // 如果获取用户信息失败，则直接返回错误结果。
        if (rr.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return rr;
        }
        // 从供应商同步过来的商品 改价
        List<SupperGoodsPriceDTO> supperGoodsPriceList = param.getData();
        for (SupperGoodsPriceDTO supperGoodsPrice : supperGoodsPriceList) {
            supperGoodsPrice.setCatId(param.getCatId());
            //修改goodsBase 价格
            goodsBaseMapper.updateGoodsPrice(supperGoodsPrice);
        }
        GoodsCommonEntity entity = this.baseMapper.selectById(supperGoodsPriceList.get(0).getCommonId());
        entity.setCommonPrice(supperGoodsPriceList.get(0).getGoodsPrice());
        entity.setCatId(param.getCatId());
        String catName = goodsCatMapper.getAllCatName(param.getCatId());
        entity.setCatName(catName);
        this.baseMapper.updateById(entity);
        return ResponseResult.success();
    }

    @Override
    public ResponseResult updateSupperGoodsStart(Map<String, Object> param) {
        // 通过token获取用户信息，用于验证权限和获取店铺ID。
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.get("token").toString());
        ManageShopBaseVO shopBaseQueryResult = merchantServiceClient.selectGdsShopBaseByShopId(Integer.valueOf(ui.getShopId()));
        List<Integer> commonIds = (List<Integer>) param.get("commonIds");
        if (CollUtil.isEmpty(commonIds)) {
            List<GoodsCommonEntity> goodsCommonEntities = baseMapper.selectList(new LambdaQueryWrapper<GoodsCommonEntity>()
                    .eq(GoodsCommonEntity::getSupplierSource, CommonType.SupplierSource.JING_DONG.getCode())
                    .eq(GoodsCommonEntity::getCommonState, 0));
            commonIds = goodsCommonEntities.stream().map(GoodsCommonEntity::getCommonId).collect(Collectors.toList());
        }
        if (null != commonIds && !commonIds.isEmpty()) {
            for (Integer commonId : commonIds) {
                GoodsCommonEntity goods = this.baseMapper.selectOne(new LambdaQueryWrapper<GoodsCommonEntity>()
                        .eq(GoodsCommonEntity::getCommonId, commonId));
                if (goods.getSupplierSource() != 2) {
                    if (!shopBaseQueryResult.getShopSelfSupport().equals("true") && !shopBaseQueryResult.getCatIdList().contains(goods.getCatId())) {
                        return ResponseResult.fail(BizCodeEnume.SHOP_BUSINESS_CATEGORY_DISCREPANCY);
                    }
                    // 查询代发商品 所同步的供应商商品是否在售
                    Integer supplierGoodsStart = this.baseMapper.querySupplierGoodsStart(commonId);
                    if (0 < supplierGoodsStart && goods.getSupplierSource() != 2) {
                        // 商品 不在售 所有经销商品不能 上架
                        return ResponseResult.fail(2, "有商品在供应商端未在售，商品不能上架!");
                    }
                } else {
                    if (null == goods.getCatId() || goods.getCatId() == 0) {
                        return ResponseResult.fail(2, "上架商品中含有未分配平台分类的商品，请先分配!");
                    }
                }

            }
        }
        this.baseMapper.updateSupperGoodsStart(commonIds);
        goodsBaseMapper.updateSupperGoodsBaseStart(commonIds);
        Map<String, Object> map = new HashMap<>();
        //通过mq 更新es的商品数据
        map.put("commonIdS", commonIds);
        // 1 是添加  2是删除
        map.put("type", CommonType.operateStatus.ADD.getCode());
        // 发送信息到MQ，用于更新ES中的商品数据
        MqSendUtils.mqSendGoods(map);
        return ResponseResult.success();
    }

    @Override
    public ResponseResult queryDealerProducts(ManageGdsNameQueryParamDTO param) {
        // 通过token获取用户信息，用于验证权限和获取店铺ID。
        ResponseResult rr = ParseTokenUtils.getShopUserInfo(param.getToken());
        // 如果获取用户信息失败，则直接返回错误结果。
        if (rr.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return rr;
        }
        // 从用户信息中提取店铺ID，用于后续的商品查询。
        UserInfoVO ui = JSON.parseObject(JSON.toJSONString(rr.getData()), UserInfoVO.class);
        param.setShopId(ui.getShopId());
        if (StringUtils.isNotBlank(param.getCatId())) {
            // 根据分类ID获取商品分类信息
            WebCatMenuVO webCatMenu = goodsCatMapper.queryByCatId(Integer.valueOf(param.getCatId()));
            // 用于存储分类ID列表
            List<Integer> catIdList = new ArrayList<>();
            // 如果找到了对应的分类信息
            if (webCatMenu != null) {
                // 如果点击的是一级分类
                if (webCatMenu.getCatLevel() != null && webCatMenu.getCatLevel() == 1) {
                    // 查询下一级分类
                    List<WebCatMenuVO> nextCatIds = goodsCatMapper.selectByCatParentId2(webCatMenu.getCatId());
                    // 如果存在下一级分类
                    if (nextCatIds != null && !nextCatIds.isEmpty()) {
                        // 遍历下一级分类，查询下下级分类并添加到列表
                        for (WebCatMenuVO nextCatId : nextCatIds) {
                            List<WebCatMenuVO> nextCatIds1 = goodsCatMapper.selectByCatParentId2(nextCatId.getCatId());
                            if (nextCatIds1 != null && !nextCatIds1.isEmpty()) {
                                List<Integer> catIds = catIds(nextCatIds1);
                                catIdList.addAll(catIds);
                            }
                        }
                        // 清除原有分类ID并设置新的分类ID列表
                        param.setCatId(null);
                        param.setCatIds(catIdList);
                    }
                } else if (webCatMenu.getCatLevel() != null && webCatMenu.getCatLevel() == 2) {
                    // 如果点击的是二级分类，逻辑类似一级分类但少一次循环
                    List<WebCatMenuVO> nextCatIds1 = goodsCatMapper.selectByCatParentId2(webCatMenu.getCatId());
                    if (nextCatIds1 != null && !nextCatIds1.isEmpty()) {
                        List<Integer> catIds = catIds(nextCatIds1);
                        param.setCatId(null);
                        param.setCatIds(catIds);
                    }
                }
            }
        }
        // 设置分页参数，准备查询商品信息。
        // 分页查询放入仓库的商品
        Page<ManageGdsNameVO> page = new Page<>(param.getPage(), param.getLimit());
        // 执行查询，获取仓库中的商品列表。
        IPage<ManageGdsNameVO> goods = this.baseMapper.queryDealerProducts(page, param);
        List<ManageGdsNameVO> records = goods.getRecords();
        for (ManageGdsNameVO record : records) {
            // 查询供应商商品状态
            Integer supplierGoodsNum = this.baseMapper.querySupplierGoodsNum(record.getCommonId());
            if (supplierGoodsNum != 0) {
                record.setDetealToSell(true);
            } else {
                record.setDetealToSell(false);
            }
            //commonid总库存
            Integer commonStock = this.baseMapper.selectAllGooodsStock(record.getCommonId());
            record.setCommonStock(commonStock);
        }
        // 构造查询结果，包含分页信息和商品列表。
        return ResponseResult.success(PageUtils.getPage(goods));
    }

    @Override
    public ResponseResult queryDealerProductsYiyatong(ManageGdsNameQueryParamDTO param) {
        // 通过token获取用户信息，用于验证权限和获取店铺ID。
        ResponseResult rr = ParseTokenUtils.getShopUserInfo(param.getToken());
        // 如果获取用户信息失败，则直接返回错误结果。
        if (rr.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return rr;
        }
        // 从用户信息中提取店铺ID，用于后续的商品查询。
        UserInfoVO ui = JSON.parseObject(JSON.toJSONString(rr.getData()), UserInfoVO.class);
        param.setShopId(ui.getShopId());
        // 设置分页参数，准备查询商品信息。
        // 分页查询放入仓库的商品
        Page<ManageGdsNameVO> page = new Page<>(param.getPage(), param.getLimit());
        // 执行查询，获取仓库中的商品列表。
        IPage<ManageGdsNameVO> goods = this.baseMapper.queryDealerProductsYiyatong(page, param);
        List<ManageGdsNameVO> records = goods.getRecords();
        for (ManageGdsNameVO record : records) {
            // 查询供应商商品状态
            record.setDetealToSell(true);
            //commonid总库存
            Integer commonStock = this.baseMapper.selectAllGooodsStock(record.getCommonId());
            record.setCommonStock(commonStock);
            List<GoodscCatVO> catVOList = goodsCatMapper.selectCatList(record.getCatId());
            record.setCatList(catVOList);
        }
        // 构造查询结果，包含分页信息和商品列表。
        return ResponseResult.success(PageUtils.getPage(goods));
    }

    @Override
    public ResponseResult queryGoodsSpecListByYiYaTong(ManageGdsNameQueryParamDTO param) {
        // 通过token获取用户信息，用于验证权限和获取店铺ID。
        ResponseResult rr = ParseTokenUtils.getShopUserInfo(param.getToken());
        // 如果获取用户信息失败，则直接返回错误结果。
        if (rr.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return rr;
        }
        // 从用户信息中提取店铺ID，用于后续的商品查询。
        UserInfoVO ui = JSON.parseObject(JSON.toJSONString(rr.getData()), UserInfoVO.class);
        param.setShopId(ui.getShopId());

        List<SupplierGoodsSpecVO> list = this.baseMapper.queryGoodsSpecListByYiYaTong(param.getCommonId());
        for (SupplierGoodsSpecVO vo : list) {
            List<GoodscCatVO> catVOList = goodsCatMapper.selectCatList(vo.getCatId());
            vo.setCatList(catVOList);
            if (vo.getSpecValue() != null) {
                StringBuilder stringBuilder = new StringBuilder();
                JSONArray jsonOrgData = JSONObject.parseArray(vo.getSpecValue());
                List<GoodBaseSpecValuesVO> result = jsonOrgData.toJavaList(GoodBaseSpecValuesVO.class);
                for (GoodBaseSpecValuesVO specValuesVO : result) {
                    stringBuilder.append(specValuesVO.getParamValue() + ",");

                }
                int lastCommaIndex = stringBuilder.lastIndexOf(",");
                stringBuilder.deleteCharAt(lastCommaIndex);
                vo.setSpecValue(stringBuilder.toString());
            }
        }
        // 构造查询结果，包含分页信息和商品列表。
        return ResponseResult.success(list);
    }


    @Override
    public ResponseResult batchPriceAdjustment(BatchPriceAdjustmentDTO param) {
        Integer adjustmentType = param.getAdjustmentType();
        Integer type = param.getType();
        String proportion = param.getProportion();
        //上涨
        List<GoodsCommonEntity> commonEntities = this.baseMapper.selectList(new LambdaQueryWrapper<GoodsCommonEntity>()
                .eq(GoodsCommonEntity::getSupplierSource, 2)
                .in(GoodsCommonEntity::getCommonId, param.getCommonIds()));
        if (commonEntities.size() > 0) {
            for (GoodsCommonEntity commonEntity : commonEntities) {
                //按比例
                if (adjustmentType == 1) {
                    BigDecimal ratio = new BigDecimal(proportion).divide(BigDecimal.valueOf(100));
                    List<Map<String, Object>> supplierGoodsYiyatongEntities = this.baseMapper.selectYiYaTongGoodsId(commonEntity.getCommonId(), commonEntity.getShopId());
                    if (supplierGoodsYiyatongEntities.size() > 0) {
                        for (Map<String, Object> map : supplierGoodsYiyatongEntities) {
                            String goodsId = map.get("goodsId").toString();
                            BigDecimal goodsPrice = new BigDecimal(map.get("goodsPrice").toString());
                            //上涨
                            if (type == 1) {
                                goodsPrice = goodsPrice.add(goodsPrice.multiply(ratio));
                                goodsBaseMapper.update(null, new LambdaUpdateWrapper<GoodsBaseEntity>()
                                        .eq(GoodsBaseEntity::getGoodsId, Integer.valueOf(goodsId))
                                        .set(GoodsBaseEntity::getGoodsPrice, goodsPrice));
                            } else {
                                //下调
                                goodsPrice = goodsPrice.multiply(ratio);
                                goodsBaseMapper.update(null, new LambdaUpdateWrapper<GoodsBaseEntity>()
                                        .eq(GoodsBaseEntity::getGoodsId, Integer.valueOf(goodsId))
                                        .set(GoodsBaseEntity::getGoodsPrice, goodsPrice));


                            }
                            commonEntity.setCommonPrice(goodsPrice);
                        }
                    }
                } else {
                    //按固定金额
                    BigDecimal ratio = new BigDecimal(proportion);
                    //上涨
                    if (type == 1) {
                        List<Map<String, Object>> supplierGoodsYiyatongEntities = this.baseMapper.selectYiYaTongGoodsId(commonEntity.getCommonId(), commonEntity.getShopId());
                        if (supplierGoodsYiyatongEntities.size() > 0) {
                            for (Map<String, Object> map : supplierGoodsYiyatongEntities) {
                                String goodsId = map.get("goodsId").toString();
                                BigDecimal goodsPrice = new BigDecimal(map.get("goodsPrice").toString());
                                goodsPrice = goodsPrice.add(ratio);
                                goodsBaseMapper.update(null, new LambdaUpdateWrapper<GoodsBaseEntity>()
                                        .eq(GoodsBaseEntity::getGoodsId, Integer.valueOf(goodsId))
                                        .set(GoodsBaseEntity::getGoodsPrice, goodsPrice));


                            }
                        }
                    } else {
                        //下调
                        //判断是否有商品协议价低于下调价
                        List<Map<String, Object>> goodsList = this.baseMapper.selectYiYaTongGoodsIdByRatio(commonEntity.getCommonId(), ratio, commonEntity.getShopId());
                        if (goodsList.size() > 0) {
                            StringBuilder sb = new StringBuilder();
                            for (Map<String, Object> map : goodsList) {
                                sb.append(map.get("goodsName").toString() + ",");
                            }
                            return ResponseResult.fail("调价失败，商品名称:" + sb.toString() + "低于协议价!");
                        }

                        List<Map<String, Object>> supplierGoodsYiyatongEntities = this.baseMapper.selectYiYaTongGoodsId(commonEntity.getCommonId(), commonEntity.getShopId());
                        if (supplierGoodsYiyatongEntities.size() > 0) {
                            for (Map<String, Object> map : supplierGoodsYiyatongEntities) {
                                String goodsId = map.get("goodsId").toString();
                                BigDecimal goodsPrice = new BigDecimal(map.get("goodsPrice").toString());
                                goodsPrice = goodsPrice.subtract(ratio);
                                goodsBaseMapper.update(null, new LambdaUpdateWrapper<GoodsBaseEntity>()
                                        .eq(GoodsBaseEntity::getGoodsId, Integer.valueOf(goodsId))
                                        .set(GoodsBaseEntity::getGoodsPrice, goodsPrice));
                            }
                        }

                    }

                }
                //更新commonPrice
                List<Map<String, Object>> supplierGoodsYiyatongEntities = this.baseMapper.selectYiYaTongGoodsId(commonEntity.getCommonId(), commonEntity.getShopId());
                commonEntity.setCommonPrice(new BigDecimal(supplierGoodsYiyatongEntities.get(0).get("goodsPrice").toString()));

            }

        }
        return ResponseResult.success();
    }

    @Override
    public ResponseResult batchEdit(BatchPriceAdjustmentDTO param) {
        Integer adjustmentType = param.getAdjustmentType();
        Integer type = param.getType();
        String proportion = param.getProportion();
        Integer catId = param.getCatId();
        //上涨
        List<GoodsCommonEntity> commonEntities = this.baseMapper.selectList(new LambdaQueryWrapper<GoodsCommonEntity>()
                .eq(GoodsCommonEntity::getSupplierSource, 2)
                .in(GoodsCommonEntity::getCommonId, param.getCommonIds()));
        if (commonEntities.size() > 0) {
            for (GoodsCommonEntity commonEntity : commonEntities) {
                //按比例
                if (adjustmentType == 1) {
                    BigDecimal ratio = new BigDecimal(proportion).divide(BigDecimal.valueOf(100));
                    List<Map<String, Object>> supplierGoodsYiyatongEntities = this.baseMapper.selectYiYaTongGoodsId(commonEntity.getCommonId(), commonEntity.getShopId());
                    if (supplierGoodsYiyatongEntities.size() > 0) {
                        for (Map<String, Object> map : supplierGoodsYiyatongEntities) {
                            String goodsId = map.get("goodsId").toString();
                            BigDecimal goodsPrice = new BigDecimal(map.get("goodsPrice").toString());
                            goodsPrice = goodsPrice.add(goodsPrice.multiply(ratio));
                            goodsBaseMapper.update(null, new LambdaUpdateWrapper<GoodsBaseEntity>()
                                    .eq(GoodsBaseEntity::getGoodsId, Integer.valueOf(goodsId))
                                    .set(GoodsBaseEntity::getGoodsPrice, goodsPrice)
                                    .set(GoodsBaseEntity::getCatId, catId));
                        }
                    }
                } else {
                    //按固定金额
                    BigDecimal ratio = new BigDecimal(proportion);
                    List<Map<String, Object>> supplierGoodsYiyatongEntities = this.baseMapper.selectYiYaTongGoodsId(commonEntity.getCommonId(), commonEntity.getShopId());
                    if (supplierGoodsYiyatongEntities.size() > 0) {
                        for (Map<String, Object> map : supplierGoodsYiyatongEntities) {
                            String goodsId = map.get("goodsId").toString();
                            BigDecimal goodsPrice = new BigDecimal(map.get("goodsPrice").toString());
                            goodsPrice = goodsPrice.add(ratio);
                            goodsBaseMapper.update(null, new LambdaUpdateWrapper<GoodsBaseEntity>()
                                    .eq(GoodsBaseEntity::getGoodsId, Integer.valueOf(goodsId))
                                    .set(GoodsBaseEntity::getGoodsPrice, goodsPrice)
                                    .set(GoodsBaseEntity::getCatId, catId));


                        }
                    }
                }


                //更新commonPrice
                List<Map<String, Object>> supplierGoodsYiyatongEntities = this.baseMapper.selectYiYaTongGoodsId(commonEntity.getCommonId(), commonEntity.getShopId());
                commonEntity.setCommonPrice(new BigDecimal(supplierGoodsYiyatongEntities.get(0).get("goodsPrice").toString()));
                commonEntity.setCatId(catId);
                String catName = goodsCatMapper.getAllCatName(catId);
                commonEntity.setCatName(catName);
                this.baseMapper.updateById(commonEntity);
            }

        }
        return ResponseResult.success();
    }

    /**
     * 待上架的经销商品  查询商品价格
     *
     * @param param
     * @return
     */
    @Override
    public ResponseResult querySupperGoodsPrice(Map<String, Object> param) {
        // 通过token获取用户信息，用于验证权限和获取店铺ID。
        ResponseResult rr = ParseTokenUtils.getShopUserInfo(param.get("token").toString());
        // 如果获取用户信息失败，则直接返回错误结果。
        if (rr.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return rr;
        }
        //根据commonid 查询供应商商品的商品规格价格
        List<Map<String, Object>> result = this.baseMapper.querySupperGoodsPrice(param.get("commonId").toString());
        return ResponseResult.success(result);
    }

    @Override
    public ResponseResult queryYiYaTongGoodsPrice(Map<String, Object> param) {
        // 通过token获取用户信息，用于验证权限和获取店铺ID。
        ResponseResult rr = ParseTokenUtils.getShopUserInfo(param.get("token").toString());
        // 如果获取用户信息失败，则直接返回错误结果。
        if (rr.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return rr;
        }
        //根据commonid 查询供应商商品的商品规格价格
        List<Map<String, Object>> result = this.baseMapper.queryYiYaTongGoodsPrice(param.get("commonId").toString());
        return ResponseResult.success(result);
    }

    /**
     * 待上架的经销商品  修改商品价格
     *
     * @param param
     * @return
     */
    @Override
    public ResponseResult updateSupperGoodsPrice(UpdateSupperGoodsPriceDTO param) {
        // 通过token获取用户信息，用于验证权限和获取店铺ID。
        ResponseResult rr = ParseTokenUtils.getShopUserInfo(param.getToken());
        // 如果获取用户信息失败，则直接返回错误结果。
        if (rr.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return rr;
        }
        // 从供应商同步过来的商品 改价
        List<SupperGoodsPriceDTO> supperGoodsPriceList = param.getData();
        for (SupperGoodsPriceDTO supperGoodsPrice : supperGoodsPriceList) {
            //修改goodsBase 价格
            goodsBaseMapper.updateGoodsPrice(supperGoodsPrice);
            //修改goodsCommon 价格
            goodsCommonMapper.updateGoodsPrice(supperGoodsPrice);
        }
        return ResponseResult.success();
    }

    /**
     * 根据入住分类查询商品分类
     *
     * @param param
     * @return
     */
    @Override
    public ResponseResult supplierQueryByJoin(ManageItemCatQueryDTO param) {
        // 通过token获取用户信息
        ResponseResult rr = ParseTokenUtils.getShopUserInfo(param.getToken());
        // 如果获取用户信息失败，则直接返回错误信息
        if (rr.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return rr;
        }
        // 解析用户信息
        UserInfoVO ui = JSON.parseObject(JSON.toJSONString(rr.getData()), UserInfoVO.class);
        // 获取商家ID
        Integer shopId = Integer.valueOf(ui.getShopId());
        // 设置查询的商家ID
        param.setShopId(shopId);

        // 根据分类级别查询相应的商品分类
        Integer catLevel = param.getCatLevel();
        // 查询一级分类
        if (CommonType.level.ONE.getCode().equals(catLevel)) {
            // 先尝试查询商家加入的一级分类
            // 查询商家入职的分类
            List<ManageCatVO> catResults = goodsCatMapper.querySupplierLevelCatByShopId(param);
            // 如果没有查询结果，则查询商家自定义的一级分类
            if (null == catResults || catResults.isEmpty()) {
                catResults = goodsCatMapper.queryLevelCatBySelfShop(param);
            }
            // 返回查询结果
            return ResponseResult.success(catResults);
            // 查询二级分类
        } else if (CommonType.level.TWO.getCode().equals(catLevel)) {
            // 先尝试查询商家加入的二级分类
            // 查询商家入职的分类
            List<ManageCatVO> catResults = goodsCatMapper.querySupplierLevelCatByShopId(param);
            // 如果没有查询结果，则查询商家自定义的二级分类
            if (null == catResults || catResults.isEmpty()) {
                catResults = goodsCatMapper.queryLevelCatBySelfShop(param);
            }
            // 返回查询结果
            return ResponseResult.success(catResults);
            // 查询三级分类
        } else if (CommonType.level.THREE.getCode().equals(catLevel)) {
            // 先尝试查询商家加入的三级分类
            // 查询商家入职的分类
            List<ManageCatVO> catResults = goodsCatMapper.querySupplierLevelCatByShopId(param);
            // 如果没有查询结果，则查询商家自定义的三级分类
            if (null == catResults || catResults.isEmpty()) {
                catResults = goodsCatMapper.queryLevelCatBySelfShop(param);
            }
            // 返回查询结果
            return ResponseResult.success(catResults);
        }
        // 如果分类级别不是一级、二级、三级，则返回成功但无数据的结果
        return ResponseResult.success();
    }

    /**
     * 查询所有供应商商品信息。
     *
     * @param platFormAllGoodsQuery 查询条件对象，包含分类ID、分页信息等。
     *                              - catId: 商品分类ID，用于查询子分类。
     *                              - 其他字段：根据业务需求动态扩展。
     * @return ResponseResult 包含分页结果的商品信息列表。
     * - 成功时返回商品信息的分页数据。
     * - 失败时返回错误信息。
     */
    @Override
    public ResponseResult queryAllSupplierGoods(PlatFormAllGoodsDTO platFormAllGoodsQuery) {
        try {
            // 如果查询条件中包含分类ID，则获取该分类的所有子分类ID并设置到查询条件中
            if (platFormAllGoodsQuery.getCatId() != null) {
                List<Integer> catIds = goodsCatMapper.getAllSonLevelCat(platFormAllGoodsQuery.getCatId());
                platFormAllGoodsQuery.setCatIdList(catIds);
            }

            // 将查询条件对象转换为Map，便于后续处理
            Map data = ParseMapUtils.beanToMap(platFormAllGoodsQuery);

            // 根据查询条件和分页信息查询商品列表
            IPage<PlatFormAllGoodsVO> page = this.baseMapper.selectByAllSupplierGoods(new Query<PlatFormAllGoodsVO>().getPage(data), platFormAllGoodsQuery);
            List<PlatFormAllGoodsVO> records = page.getRecords();

            // 遍历商品列表，为每个商品查询库存信息
            for (PlatFormAllGoodsVO record : records) {
                if (record.getGoodsType() == 1) {
                    // 查询普通商品的库存信息
                    List<GoodsStockVO> stockEntityList = this.baseMapper.selectStocks(record.getCommonId());
                    record.setGoodsStockEntityList(stockEntityList);
                } else {
                    // 查询供应商商品的库存信息
                    List<GoodsStockVO> stockEntityList = this.baseMapper.selectSupplierStocks(record.getCommonId());
                    record.setGoodsStockEntityList(stockEntityList);
                }
            }

            // 返回查询成功的分页结果
            return ResponseResult.success(PageUtils.getPage(page));
        } catch (IllegalAccessException e) {
            // 打印异常堆栈信息
            e.printStackTrace();
        }

        // 如果出现异常，返回失败结果，并给出未知异常的错误信息
        return ResponseResult.fail(BizCodeEnume.UNKNOW_EXCEPTION.getMsg());
    }

    /**
     * 查询供应商的配送区域ID列表。
     *
     * @param goodsIds 商品ID列表，用于查询对应的配送区域ID。
     * @return List<Integer> 配送区域ID列表。
     */
    @Override
    public List<Integer> selectSupplierTransportAreaId(List<Integer> goodsIds) {
        return baseMapper.selectSupplierTransportAreaId(goodsIds);
    }

    /**
     * 根据商品通用ID查询商品详细信息。
     *
     * @param commonId 商品通用ID，用于唯一标识商品。
     * @return SelfSupportIndexGoodsVO 商品详细信息对象。
     */
    @Override
    public SelfSupportIndexGoodsVO selectByCommonId(Integer commonId) {
        SelfSupportIndexGoodsVO result = this.baseMapper.selectByCommonIdFeign(commonId);
        return result;
    }

    /**
     * 更新CommonIsXian3信息
     * 该方法用于更新数据库中与CommonIsXian3相关的记录
     *
     * @param param 包含更新信息的DTO对象
     */
    @Override
    public void updateCommonIsXian3(CommonIsXianUpdateDTO param) {
        this.baseMapper.updateCommonIsXian3(param);
    }

    /**
     * 查询满减赠者信息
     * 通过commonId查询满减赠者的相关信息
     *
     * @param commonId 用于查询的公共ID
     * @return 包含满减赠者信息的Map对象
     */
    @Override
    public Map<String, Object> selectManjianzengzheMessage(Integer commonId) {
        Map<String, Object> result = this.baseMapper.selectManjianzengzheMessage(commonId);
        return result;
    }

    /**
     * 通过CommonId查询商品详情信息
     * 使用CommonId作为参数，查询并返回商品详情的复杂对象
     *
     * @param commonId 商品的公共ID
     * @return 商品详情的查询结果对象
     */
    @Override
    public GdsDetailsCommonQueryResultVO selectByCommonIdFeign(Integer commonId) {
        GdsDetailsCommonQueryResultVO result = this.baseMapper.selectByCommonIdFeign2(commonId);
        return result;
    }

    /**
     * 查询首页推荐商品数量
     * 根据推荐参数查询满足条件的商品数量，用于网页首页推荐
     *
     * @param recommendParam 包含查询条件的参数对象
     * @return 满足条件的商品数量
     */
    @Override
    public Integer selectWebShopIndexCount(WebIndexRecommendQueryParamVO recommendParam) {
        Integer result = this.baseMapper.selectWebShopIndexCount(recommendParam);
        return result;
    }

    /**
     * 检查店铺自提功能
     * 通过传递的参数检查店铺是否支持自提功能
     *
     * @param param 包含检查条件的Map对象
     * @return 表示自提功能是否可用的整数值
     */
    @Override
    public Integer checkShopSelf(Map<String, Object> param) {
        Integer result = this.baseMapper.checkShopSelf(param);
        return result;
    }

    /**
     * 更新店铺状态
     * 根据传递的参数更新店铺的状态信息
     *
     * @param param 包含更新信息的DTO对象
     */
    @Override
    public void updateShopState(CommonShopStateDTO param) {
        this.baseMapper.updateShopState(param);
    }

    /**
     * 更新店铺状态（简化版）
     * 仅通过店铺ID更新店铺状态信息
     *
     * @param shopId 店铺的唯一标识ID
     */
    @Override
    public void updateShopState1(Integer shopId) {
        this.baseMapper.updateShopState1(shopId);
    }

    /**
     * 查询小程序店铺商品列表
     * 根据店铺列表查询结果对象查询并返回小程序中店铺商品的列表
     *
     * @param record 店铺列表查询结果对象，包含查询条件
     * @return 包含店铺商品信息的Map对象列表
     */
    @Override
    public List<Map<String, Object>> selectUniappQueryShopGoods(ShopListQueryResultVO record) {
        List<Map<String, Object>> result = this.baseMapper.selectUniappQueryShopGoods(record);
        return result;
    }

    /**
     * 通过店铺ID查询商品列表（版本3）
     * 使用店铺ID作为参数，查询并返回满足条件的商品列表，该方法是多个版本中的一个
     *
     * @param shopId 店铺的唯一标识ID
     * @return 包含商品信息的查询结果对象列表
     */
    @Override
    public List<GdsListQueryResultVO> selectByShopId3(Integer shopId) {
        List<GdsListQueryResultVO> result = this.baseMapper.selectByShopIdFeign(shopId);
        return result;
    }

    /**
     * 根据店铺ID选择店铺总信息
     *
     * @param shopId 店铺ID
     * @return 店铺总信息的整数表示
     */
    @Override
    public Integer selectShopSum(Integer shopId) {
        Integer result = this.baseMapper.selectShopSum(shopId);
        return result;
    }

    /**
     * 根据通用ID查询商品类别ID
     *
     * @param commonId 通用ID
     * @return 商品类别ID
     */
    @Override
    public String queryGoodsCatId(String commonId) {
        String result = this.baseMapper.queryGoodsCatId(commonId);
        return result;
    }

    /**
     * 更新商品的通用信息根据店铺ID
     *
     * @param shopId 店铺ID
     */
    @Override
    public void updateGoodsCommon(String shopId) {
        this.baseMapper.updateGoodsCommon(shopId);
    }

    /**
     * 更新商品的分销状态
     *
     * @param distributorFlag 分销标志
     * @param commonId        商品通用ID
     */
    @Override
    public void updateGoodsFxStatus(Integer distributorFlag, Integer commonId) {
        this.baseMapper.updateGoodsFxStatus(distributorFlag, commonId);
    }

    /**
     * 根据店铺ID选择店铺商品数量
     *
     * @param shopId 店铺ID
     * @return 店铺商品数量的整数表示
     */
    @Override
    public Integer selectShopGoodsCount(Integer shopId) {
        Integer result = this.baseMapper.selectShopGoodsCount(shopId);
        return result;
    }

    /**
     * 根据店铺ID选择店铺出售中的商品数量
     *
     * @param shopId 店铺ID
     * @return 店铺出售中商品数量的整数表示
     */
    @Override
    public Integer selectShopSellGoodsCount(Integer shopId) {
        Integer result = this.baseMapper.selectShopSellGoodsCount(shopId);
        return result;
    }

    /**
     * 通过Feign客户端调用，根据通用ID选择运输区域ID
     *
     * @param commonId 通用ID
     * @return 运输区域ID
     */
    @Override
    public Integer selectTransportAreaIdFeign(Integer commonId) {
        Integer result = this.baseMapper.selectTransportAreaIdFeign(commonId);
        return result;
    }

    /**
     * 查询商品使用的 售国际卖区域模板
     *
     * @param commonId
     * @return
     */
    @Override
    public Integer selectInternationalTransportAreaIdFeign(Integer commonId) {
        Integer result = this.baseMapper.selectInternationalTransportAreaIdFeign(commonId);
        return result;
    }

    /**
     * 根据商品ID列表查询物流信息
     *
     * @param goodsIdList 商品ID列表
     * @return 物流查询结果列表
     */
    @Override
    public List<WapShippingQueryResultVO> selectShipping2(List<Integer> goodsIdList) {
        List<WapShippingQueryResultVO> result = this.baseMapper.selectShipping2(goodsIdList);

        ResponseResult<PointsCashRatioVo> cashRatio = baseServiceClient.getPointsCashRatio();

        if (cashRatio.getCode() == 1) {
            result.forEach(item -> {
                Double pointsRate = item.getPointsRate();
                if (Objects.nonNull(pointsRate)) {
                    PointsCashRatioVo ratioVo = cashRatio.getData();
                    PointsRatioUtil.PointsCashRatioRes pointsRatio = PointsRatioUtil.getPointsRatio(item.getGoodsPrice(), pointsRate);
                    item.setCashPrice(pointsRatio.getCashPrice());
                }
            });
        }

        return result;
    }

    /**
     * 根据商品ID选择物流信息
     *
     * @param goodsId 商品ID
     * @return 物流信息DTO
     */
    @Override
    public ShippingInfoDTO selectShippingInfo(Integer goodsId) {
        ShippingInfoDTO result = this.baseMapper.selectShippingInfo(goodsId);
        return result;
    }

    /**
     * 查询FX规格商品ID
     *
     * @param commonId 通用ID
     * @return 商品ID
     */
    @Override
    public Integer queryFxGuigeGoodsId(Integer commonId) {
        Integer result = this.baseMapper.queryFxGuigeGoodsId(commonId);
        return result;
    }

    /**
     * 查询虚拟商品信息
     *
     * @param goodsId 商品ID
     * @return 虚拟商品ID
     */
    @Override
    public Integer queryVirtualGoods(Integer goodsId) {
        Integer result = this.baseMapper.queryVirtualGoods(goodsId);
        return result;
    }

    /**
     * 根据通用ID选择商品名称
     *
     * @param commonId 通用ID
     * @return 商品名称
     */
    @Override
    public String selectCommonName(Integer commonId) {
        String result = this.baseMapper.selectCommonNameFeign(commonId);
        return result;
    }

    /**
     * 查询商品详细信息
     *
     * @param commonId 通用ID
     * @return 商品详细信息对象
     */
    @Override
    public BaseWxLiveGoodsVO queryCommonDetail(Integer commonId) {
        BaseWxLiveGoodsVO result = this.baseMapper.queryCommonDetail(commonId);
        return result;
    }

    /**
     * 根据分类的通用名称选择分类的ID
     * <p>
     * 此方法覆盖了基础映射器中的相应方法，以提供根据分类的通用名称来获取其ID的功能
     * 它主要用于在较大的应用程序中实现特定的业务逻辑，即通过分类的通用名称快速找到对应的ID
     *
     * @param commonName 分类的通用名称，用于查询对应的分类ID
     * @return 返回查询到的分类ID如果未找到匹配的记录，则返回null
     */
    @Override
    public Integer selectCatIdByCommonName(String commonName) {
        Integer result = this.baseMapper.selectCatIdByCommonName(commonName);
        return result;
    }

    /**
     * 分页查询自营店铺商品
     *
     * @param param 查询参数，包含分页信息和查询条件
     * @return 返回分页查询结果
     */
    @Override
    public ResponseResult selectSelfSupportShopGoods(GiftGoodsDTO param) {
        //分页查询平台礼包商品
        Page<GiftGoodsVO> page = new Page<>(param.getPage(), param.getLimit());
        IPage<GiftGoodsVO> goods = this.baseMapper.selectSelfSupportShopGoods(page, param);
        return ResponseResult.success(PageUtils.getPage(goods));
    }

    /**
     * 商品智能推荐
     * 用户未登录 查询平台在售商品 按销量 评论排序
     * 用户登录按  历史下单商品、游览、收藏、加购、商品所属分类查询商品
     *
     * @param pageHelper
     * @return
     */
    @Override
    public ResponseResult intelligentRecommendationGoods(PageHelper pageHelper) {
        List<WapCommonBaseUserFootVO> resultList = new ArrayList<>();
        Integer goodsMaxNum = 10;
        if (RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + pageHelper.getToken())) {
            UserInfoVO info = ParseTokenUtils.getPlatformUser(pageHelper.getToken());
            // 判断是否是第一页
            if (pageHelper.getPage().equals(1)) {
                // 是第一页 则根据 历史下单商品、游览、收藏、加购、商品所属分类查询商品
                // 根据用户id查询用户今天 下单商品、游览、收藏、加购、商品所属分类 及商品id
                List<IntelligentRecommendationGoodsCatVO> goodsCatVOList = this.baseMapper.queryIntelligentRecommendationGoodsCat(info.getUserId());
                if (null != goodsCatVOList && goodsCatVOList.size() > 0) {
                    // 按 catId 分组
                    Map<Integer, List<IntelligentRecommendationGoodsCatVO>> groupedByCatId = goodsCatVOList.stream()
                            .collect(Collectors.groupingBy(IntelligentRecommendationGoodsCatVO::getCatId));
                    if (groupedByCatId.size() <= 5) {
                        goodsMaxNum = goodsMaxNum * groupedByCatId.size();
                    } else {
                        goodsMaxNum = goodsMaxNum * 5;
                    }
                    System.out.println("goodsMaxNum-------" + goodsMaxNum);
                    // 有就根据查询的商品数量 及商品 分类id 计算每个分类下要查询多少商品 默认先查询100 条
                    List<IntelligentRecommendationGoodsCatVO> catQueryGoodsNum = new ArrayList<>();
                    // 1. 先计算总数量
                    int totalCount = groupedByCatId.values().stream()
                            .mapToInt(List::size)
                            .sum();
                    // 2. 按比例分配
                    groupedByCatId.forEach((catId, commonIdList) -> {
                        IntelligentRecommendationGoodsCatVO vo = new IntelligentRecommendationGoodsCatVO();
                        vo.setCatId(catId);
                        // 计算比例：(当前分类数量 / 总数量) × goodsMaxNum
                        BigDecimal ratio = new BigDecimal(commonIdList.size())
                                .divide(new BigDecimal(totalCount), 4, RoundingMode.HALF_UP);
                        int allocatedNum = ratio.multiply(new BigDecimal(10))
                                .setScale(0, RoundingMode.HALF_UP)
                                .intValue();
                        vo.setQueryNum(allocatedNum);
                        catQueryGoodsNum.add(vo);
                    });
                    // 循环遍历查询商品数据
                    for (IntelligentRecommendationGoodsCatVO recommendationGoodsCatVO : catQueryGoodsNum) {
                        List<WapCommonBaseUserFootVO> catQueryGoodsList = this.baseMapper.queryCatQueryGoodsNum(recommendationGoodsCatVO);
                        if (null != catQueryGoodsList && catQueryGoodsList.size() > 0) {
                            resultList.addAll(catQueryGoodsList);
                        }
                    }
                    // 满足goodsMaxNum 商品数据打乱返回前端
                    System.out.println("resultList.size()-------" + resultList.size());
                    if (resultList.size() >= goodsMaxNum) {
                        Collections.shuffle(resultList);
                        return ResponseResult.success(resultList);
                    } else {
                        // 不足goodsMaxNum 条就查询在售线上商品 补足100条 数据打乱返回前端
                        Integer goodsNum = goodsMaxNum - resultList.size();
                        Page<WapCommonBaseUserFootVO> page = new Page<>(pageHelper.getPage(), goodsNum);
                        Page<WapCommonBaseUserFootVO> resultPage = this.baseMapper.queryIntelligentRecommendationAllGoods(page, resultList);
                        List<WapCommonBaseUserFootVO> records = resultPage.getRecords();
                        if (null != records && records.size() > 0) {
                            resultList.addAll(records);
                            Collections.shuffle(resultList);
                        }
                        return ResponseResult.success(resultList);
                    }
                } else {
                    // 如果没有 直接查询在售线上商品
                    Page<WapCommonBaseUserFootVO> page = new Page<>(pageHelper.getPage(), pageHelper.getLimit());
                    Page<WapCommonBaseUserFootVO> resultPage = this.baseMapper.queryIntelligentRecommendationAllGoods(page, null);
                    List<WapCommonBaseUserFootVO> records = resultPage.getRecords();
                    if (null != records && records.size() > 0) {
                        resultList = records;
                    }
                }
            } else {
                // 不是第一页 就查询在售商品
                Page<WapCommonBaseUserFootVO> page = new Page<>(pageHelper.getPage(), pageHelper.getLimit());
                Page<WapCommonBaseUserFootVO> resultPage = this.baseMapper.queryIntelligentRecommendationAllGoods(page, null);
                List<WapCommonBaseUserFootVO> records = resultPage.getRecords();
                if (null != records && records.size() > 0) {
                    resultList = records;
                }
            }
        } else {
            // 用户未登录
            Page<WapCommonBaseUserFootVO> page = new Page<>(pageHelper.getPage(), pageHelper.getLimit());
            Page<WapCommonBaseUserFootVO> resultPage = this.baseMapper.queryIntelligentRecommendationAllGoods(page, null);
            List<WapCommonBaseUserFootVO> records = resultPage.getRecords();
            if (null != records && records.size() > 0) {
                resultList = records;
            }
        }
        return ResponseResult.success(resultList);
    }

    /**
     * 减少商品库存增加商品销量
     *
     * @param goodsDetails
     * @return
     */
    @Override
    public ResponseResult updateSubStockAndAddSaleNum(String goodsDetails) {
        // 解析商品详情JSON字符串，得到商品销售汇总数据列表
        List<WapGoodsSaleSumDTO> wapGood = JSON.parseArray(goodsDetails, WapGoodsSaleSumDTO.class);
        // 遍历每个商品的销售汇总数据，进行库存更新和销售数量增加
        List<Integer> goodsIds = new ArrayList<>();
        for (WapGoodsSaleSumDTO wapGoodsSaleSumUpdate : wapGood) {
            goodsIds.add(wapGoodsSaleSumUpdate.getGoodsId());
            // 根据goodsId查询具体商品信息
            GoodsStockEntity goodsStockEntity = goodsStockService.getOne(new LambdaUpdateWrapper<GoodsStockEntity>()
                    .eq(GoodsStockEntity::getGoodsId, wapGoodsSaleSumUpdate.getGoodsId()));
            // 根据commonId查询通用商品信息
            GoodsCommonEntity entity = this.baseMapper.selectById(goodsStockEntity.getCommonId());
            // 获取商品数量，如果为null则默认为0
            int num = wapGoodsSaleSumUpdate.getGoodsNum() == null ? 0 : Integer.parseInt(wapGoodsSaleSumUpdate.getGoodsNum());
            // 获取通用库存，如果小于等于0则默认为0
            Integer commonStock = entity.getCommonStock() > 0 ? entity.getCommonStock() : 0;
            // 获取商品库存，如果小于等于0则默认为0
            Integer goodsStock = goodsStockEntity.getGoodsStock() > 0 ? goodsStockEntity.getGoodsStock() : 0;
            // 如果商品未被询价或商品有库存，则可以进行库存更新
            if (goodsStockEntity.getGoodsStock() > 0) {
                // 更新通用库存，如果更新后的库存大于等于0
                if ((entity.getCommonStock() - num) >= 0) {
                    commonStock = entity.getCommonStock() - num;
                }
                // 更新商品库存，如果更新后的库存大于等于0
                if ((goodsStockEntity.getGoodsStock() - num) >= 0) {
                    goodsStock = goodsStockEntity.getGoodsStock() - num;
                }
            }

            // 更新通用商品的库存信息
            this.baseMapper.update(null, new LambdaUpdateWrapper<GoodsCommonEntity>()
                    .eq(GoodsCommonEntity::getCommonId, entity.getCommonId())
                    .set(GoodsCommonEntity::getCommonStock, commonStock));
            // 更新商品库存表中的库存信息
            goodsStockEntity.setGoodsStock(goodsStock);
            goodsStockEntity.setUpdateTime(new Date());
            goodsStockService.updateById(goodsStockEntity);

        }
        Map<String, Object> esMap = new HashMap<>();
        // 更新es
        esMap.put("goodsIdS", goodsIds);
        // 1 是添加  2是删除
        esMap.put("type", 1);
        // 发送消息到MQ，用于更新ES中的商品销售数量
        MqSendUtils.mqSendGoodsByEs(esMap);
        // 更新操作成功，返回成功结果
        return ResponseResult.success();
    }

    /**
     * 批量导入商品
     *
     * @param file              文件
     * @param transportAreaId   运费区域id
     * @param transportTemplate 运费模板id
     * @return 结果
     */
    @Override
    public ResponseResult importGoodsBatch(MultipartFile file, String token, Integer transportAreaId, Integer transportTemplate) {
        // 通过token获取用户信息
        ResponseResult rr = ParseTokenUtils.getShopUserInfo(token);
        // 如果获取用户信息失败，则直接返回错误信息
        if (rr.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return rr;
        }
        // 解析用户信息
        UserInfoVO ui = JSON.parseObject(JSON.toJSONString(rr.getData()), UserInfoVO.class);
        // 解析Excel文件
        try {
            readMoreSkuExcel(file.getInputStream(), ui, transportAreaId, transportTemplate, token);
        } catch (Exception e) {
            logger.error("解析Excel文件失败：{}", e.getMessage());
            if (e instanceof DefinedException definedException) {
                return ResponseResult.fail(definedException.getMessageCode(), definedException.getDetailMessage());
            }
            return ResponseResult.fail(BizCodeEnume.FILE_FORMAT_WRONG);
        }
        return ResponseResult.success();
    }

    /**
     * 从Excel文件中读取数据。
     *
     * @param is                Excel文件的输入流。
     * @param userInfo          用户信息，用于鉴权或记录操作人。
     * @param transportAreaId   物流区域ID，用于指定物流相关的区域信息。
     * @param transportTemplate 物流模板ID，用于指定物流操作的模板。
     * @param token             认证令牌，用于用户身份验证。
     * @throws IOException 如果读取Excel文件发生错误。
     */
    private void readMoreSkuExcel(InputStream is, UserInfoVO userInfo, Integer transportAreaId,
                                  Integer transportTemplate, String token) throws IOException {
        // 尝试根据输入流创建Workbook对象，用于读取Excel内容
        Workbook workbook = WorkbookFactory.create(is);
        Map<String, Object> map = new HashMap<>();
        List<Integer> commonIdList = new ArrayList<>();
        List<Integer> goodsIdList = new ArrayList<>();
        Integer shopId = Integer.valueOf(userInfo.getShopId());
        UserTokenBeanDTO dto = new UserTokenBeanDTO();
        dto.setToken(token);
        //抽离出来减少查询的次数
        List<Integer> joinCat = merchantServiceClient.queryJoinCat(shopId);
        WebShopListQueryEsDTO webShopListQueryResult = merchantServiceClient.selectByShopId2(shopId);
        String shopName = webShopListQueryResult.getShopName();
        ManageShopVideoDTO shopImageSpaceDTO = new ManageShopVideoDTO();
        shopImageSpaceDTO.setShopId(String.valueOf(shopId));
        List<ManageShopVideoDTO> uploadBaseList = userServiceClient.selectImageSpaceList(shopImageSpaceDTO);
        Map<String, String> imageSpaceUrlMap = new HashMap<>();
        for (ManageShopVideoDTO uploadBaseEntity : uploadBaseList) {
            String uploadName = uploadBaseEntity.getUploadName();
            int dotIndex = uploadName.lastIndexOf(".");
            if (dotIndex > 0) {
                uploadName = uploadName.substring(0, dotIndex);
            }
            imageSpaceUrlMap.put(uploadName, uploadBaseEntity.getUploadPath());
        }
        if (null != workbook) {
            Sheet sheet = workbook.getSheetAt(0);
            int rows = sheet.getPhysicalNumberOfRows();
            // 获取所有合并区域
            List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
            // 将goodsCommon的数据存储
            logger.info("开始检查数据的合法");
            String no = "";
            Integer commonId = null;
            List<Map<String, Integer>> specList = new ArrayList<>();
            Integer catId = 0;
            String catName;
            GoodsCommonEntity commonEntity = new GoodsCommonEntity();
            String goodsName = "";
            int stock = 0;
            Integer brandId = 0;
            String brandName = "";
            String commonDetailsBody = "";
            List<String> specValueList;
            String[] specParams = new String[0];
            for (int i = 2; i < rows; i++) {
                Map<String, Integer> specMap = new HashMap<>();
                logger.info("begin analys Excel row number =" + i);
                Row row = sheet.getRow(i);
                if (null == row) {
                    continue;
                }
                // 取值编码 存储到 goodsCommonMap里面
                Cell codeNumberCell = row.getCell(0);
                String codeNumber = ExcelsUtil.getValue(sheet, mergedRegions, codeNumberCell);
                // 编码为空 退出当前循环
                if (StringUtils.isEmpty(codeNumber)) {
                    continue;
                }
                boolean isAlikeFlag = codeNumber.equals(no);
                if (!isAlikeFlag) {
                    no = codeNumber;
                    specList = new ArrayList<>();
                    stock = 0;
                    commonEntity = new GoodsCommonEntity();
                    setGoodsCommon(commonEntity, brandId, brandName, shopId, shopName, webShopListQueryResult, transportTemplate, transportAreaId);

                    // 商品名称
                    Cell goodsNameCell = row.getCell(1);
                    goodsName = ExcelsUtil.getValue(sheet, mergedRegions, goodsNameCell);
                    if (StringUtils.isEmpty(goodsName)) {
                        throw new DefinedException("第" + (i + 1) + "行商品名称不能为空");
                    }
                    commonEntity.setCommonName(goodsName);
                    // 解析三级类目
                    Cell threeCatIdCell = row.getCell(2);
                    String threeCatId = ExcelsUtil.getValue(sheet, mergedRegions, threeCatIdCell);
                    catId = (int) Double.parseDouble(threeCatId);
                    Map<String, Object> catMap;
                    if (StringUtils.isEmpty(threeCatId) || checkNumber(threeCatId)) {
                        throw new DefinedException("第" + (i + 1) + "行分类id不能为空，且必须为正整数");
                    }
                    catMap = getCatName(catId);
                    if (null == catMap.get("catName")) {
                        // 三级类目有错误的
                        throw new DefinedException("第" + (i + 1) + "行分类id有误");
                    }
                    System.out.println(joinCat);
                    if (!joinCat.contains(catId) && webShopListQueryResult.getShopSelfSupport().equals("false")) {
                        // 三级类目有错误的
                        throw new DefinedException("第" + (i + 1) + "行分类id需为店铺所绑定的经营类目");
                    }
                    commonEntity.setCatId(catId);
                    catName = (String) catMap.get("catName");
                    commonEntity.setCatName(catName);
                    // 规格名
                    Cell specNameCell = row.getCell(3);
                    String specParamStr = ExcelsUtil.getValue(sheet, mergedRegions, specNameCell);
                    if (StringUtils.isEmpty(specParamStr)) {
                        throw new DefinedException("第" + (i + 1) + "行商品规格名不能为空");
                    }
                    // 规格值
                    Cell specValueCell = row.getCell(4);
                    String specValueStr = ExcelsUtil.getValue(sheet, mergedRegions, specValueCell);
                    if (StringUtils.isEmpty(specValueStr)) {
                        throw new DefinedException("第" + (i + 1) + "行商品规格值不能为空");
                    }
                    specValueList = List.of(specValueStr.split(","));
                    if (specParamStr.contains("+")) {
                        specParams = specParamStr.split("\\+");
                    } else {
                        specParams = new String[1];
                        specParams[0] = specParamStr;
                    }
                    // 开始循环获取属性
                    List<SpecParamJsonBeanDTO> specParamJsonBeans = new ArrayList<>();
                    for (int j = 0; j < specParams.length; j++) {
                        SpecParamJsonBeanDTO jsonBean = new SpecParamJsonBeanDTO();
                        jsonBean.setParamName(specParams[j]);
                        jsonBean.setSort(j);
                        jsonBean.setIsCommon(false);
                        jsonBean.setRefSpecId(null);
                        String specValueHaveAdd = specValueList.get(j);
                        List<String> paramNameValues = List.of(specValueHaveAdd.split("\\+"));
                        jsonBean.setParamNameValues(paramNameValues);
                        specParamJsonBeans.add(jsonBean);
                    }
                    commonEntity.setSpecParam(JSON.toJSONString(specParamJsonBeans));
                }

                // 商品sku参数组装
                GoodsBaseEntity baseEntity = setGoodsBaseEntity(shopId, shopName, catId, brandId);

                // 组合规格值
                Cell specValueCell = row.getCell(5);
                String specValueStr = ExcelsUtil.getValue(specValueCell);
                if (StringUtils.isEmpty(specValueStr)) {
                    throw new DefinedException("第" + (i + 1) + "行组合规格值不能为空");
                }
                List<GoodBaseSpecValuesVO> specValues = new ArrayList<>();
                String[] specParamValues = specValueStr.split("\\+");
                for (int j = 0; j < specParams.length; j++) {
                    GoodBaseSpecValuesVO specValuesVO = new GoodBaseSpecValuesVO();
                    specValuesVO.setParamName(specParams[j]);
                    specValuesVO.setSort(1);
                    specValuesVO.setParamValue(specParamValues[j]);
                    specValues.add(specValuesVO);
                }
                baseEntity.setGoodsSpec(specValueStr);
                baseEntity.setGoodsName(brandName + " " + goodsName + " " + specValueStr);
                baseEntity.setSpecValue(JSON.toJSONString(specValues));
                // 处理商品市场价格
                Cell goodsMarketPriceCell = row.getCell(6);
                String goodsMarketPriceStr = ExcelsUtil.getValue(goodsMarketPriceCell);
                if (!StringUtils.isEmpty(goodsMarketPriceStr)) {
                    BigDecimal goodsMarketPrice = new BigDecimal(goodsMarketPriceStr);
                    baseEntity.setGoodsMarketPrice(goodsMarketPrice);
                }
                // 处理商品价格
                Cell goodsPriceCell = row.getCell(7);
                String goodsPriceStr = ExcelsUtil.getValue(goodsPriceCell);
                if (StringUtils.isEmpty(goodsPriceStr)) {
                    throw new DefinedException("第" + (i + 1) + "行价格不能为空");
                }
                if (checkNumber(goodsPriceStr)) {
                    throw new DefinedException("第" + (i + 1) + "行价格必须为数字字符");
                }
                BigDecimal goodsPrice = new BigDecimal(goodsPriceStr);
                baseEntity.setGoodsPrice(goodsPrice);
                commonEntity.setCommonPrice(goodsPrice);
                // 商品重量(g)
                Cell goodsCubageCell = row.getCell(8);
                String goodsCubageStr = ExcelsUtil.getValue(goodsCubageCell);
                if (!StringUtils.isEmpty(goodsCubageStr)) {
                    if (checkNumber(goodsCubageStr)) {
                        throw new DefinedException("第" + (i + 1) + "行商品重量必须为数字字符");
                    }
                }
                BigDecimal goodsCubage = StringUtils.isEmpty(goodsCubageStr) ? BigDecimal.ZERO : new BigDecimal(goodsCubageStr);
                baseEntity.setGoodsCubage(goodsCubage);
                commonEntity.setCommonCubage(goodsCubage);
                // 商品库存
                Cell goodsStockCell = row.getCell(9);
                String goodsStockStr = ExcelsUtil.getValue(goodsStockCell);
                if (StringUtils.isEmpty(goodsStockStr)) {
                    throw new DefinedException("第" + (i + 1) + "行商品库存不能为空");
                }
                if (checkNumber(goodsStockStr)) {
                    throw new DefinedException("第" + (i + 1) + "行商品库存必须为数字字符");
                }
                int goodsStock = (int) Double.parseDouble(goodsStockStr);
                stock += goodsStock;
                // 商品图片
                Cell imageOneCell = row.getCell(10);
                String imageOneStr = ExcelsUtil.getValue(imageOneCell);
                if (StringUtils.isEmpty(imageOneStr)) {
                    throw new DefinedException("第" + (i + 1) + "行商品主图不能为空");
                }
                List<String> goodsImageCells = new ArrayList<>();
                baseEntity.setGoodsImage(getImageUrl(imageSpaceUrlMap, imageOneStr));
                commonEntity.setCommonImage(getImageUrl(imageSpaceUrlMap, imageOneStr));
                goodsImageCells.add(getImageUrl(imageSpaceUrlMap, imageOneStr));
                Cell imageTwoCell = row.getCell(11);
                String imageTwoStr = ExcelsUtil.getValue(imageTwoCell);
                if (!StringUtils.isEmpty(imageTwoStr)) {
                    goodsImageCells.add(getImageUrl(imageSpaceUrlMap, imageTwoStr));
                }
                Cell imageThreeCell = row.getCell(12);
                String imageThreeStr = ExcelsUtil.getValue(imageThreeCell);
                if (!StringUtils.isEmpty(imageThreeStr)) {
                    goodsImageCells.add(getImageUrl(imageSpaceUrlMap, imageThreeStr));
                }
                Cell imageFourCell = row.getCell(13);
                String imageFourStr = ExcelsUtil.getValue(imageFourCell);
                if (!StringUtils.isEmpty(imageFourStr)) {
                    goodsImageCells.add(getImageUrl(imageSpaceUrlMap, imageFourStr));
                }
                Cell imageFiveCell = row.getCell(14);
                String imageFiveStr = ExcelsUtil.getValue(imageFiveCell);
                if (!StringUtils.isEmpty(imageFiveStr)) {
                    goodsImageCells.add(getImageUrl(imageSpaceUrlMap, imageFiveStr));
                }
                goodsImageCells = goodsImageCells.stream().filter(item -> !"".equals(item)).collect(Collectors.toList());
                if (goodsImageCells.isEmpty()) {
                    throw new DefinedException("请正确填写第" + (i + 1) + "行的商品图片");
                }
                // 添加spu
                if (!isAlikeFlag) {
                    goodsCommonMapper.insert(commonEntity);
                    commonId = commonEntity.getCommonId();
                    commonIdList.add(commonId);
                    GoodsCommonDetailEntity detailEntity = new GoodsCommonDetailEntity();
                    detailEntity.setCommonId(commonId);
                    detailEntity.setCommonBody(commonDetailsBody);
                    goodsCommonDetailMapper.insert(detailEntity);
                }
                // 添加sku
                baseEntity.setCommonId(commonId);
                goodsBaseMapper.insert(baseEntity);
                Integer goodsId = baseEntity.getGoodsId();
                specMap.put("commonId", commonId);
                specMap.put("goodsId", goodsId);
                goodsIdList.add(goodsId);
                specList.add(specMap);
                // 更新spu库存,规格值
                goodsCommonMapper.update(null, new LambdaUpdateWrapper<GoodsCommonEntity>()
                        .set(GoodsCommonEntity::getCommonStock, stock)
                        .set(GoodsCommonEntity::getCommonSpecValue, JSON.toJSONString(specList))
                        .set(GoodsCommonEntity::getSpecParam, commonEntity.getSpecParam())
                        .set(!isAlikeFlag, GoodsCommonEntity::getGoodsId, goodsId)
                        .eq(GoodsCommonEntity::getCommonId, commonId));
                // 更新sku订货编码
                goodsBaseMapper.update(null, new LambdaUpdateWrapper<GoodsBaseEntity>()
                        .set(GoodsBaseEntity::getGoodsCode, "YL" + goodsId + UUIDUtil.getRandom(4))
                        .eq(GoodsBaseEntity::getGoodsId, goodsId));
                // 更新sku图片
                for (String imageUrl : goodsImageCells) {
                    GoodsImagesEntity imagesEntity = new GoodsImagesEntity();
                    imagesEntity.setShopId(shopId);
                    imagesEntity.setGoodsId(goodsId);
                    imagesEntity.setCommonId(commonId);
                    imagesEntity.setImagesImage(imageUrl);
                    goodsImagesMapper.insert(imagesEntity);
                }
                // 更新sku库存
                GoodsStockEntity goodsStockEntity = new GoodsStockEntity();
                goodsStockEntity.setGoodsId(goodsId);
                goodsStockEntity.setCommonId(commonId);
                goodsStockEntity.setGoodsStock(stock);
                goodsStockEntity.setCreateUser(userInfo.getUserId());
                goodsStockEntity.setCreateTime(DateUtils.getNowDateToDf());
                goodsStockEntity.setUpdateUser(userInfo.getUserId());
                goodsStockEntity.setUpdateTime(DateUtils.getNowDateToDf());
                goodsStockService.save(goodsStockEntity);
            }
            workbook.close();
        }
        if (webShopListQueryResult.getShopSelfSupport().equals("true")) {
            // 通过MQ发送更新后的商品数据到ES
            map.put("goodsIdS", goodsIdList);
            // 创建商品版本信息
            map.put("type", CommonType.operateStatus.ADD.getCode());
            MqSendUtils.mqSendGoodsByEs(map);
            asyncVersionService.addGoodsVersionToList(commonIdList, token);
        }
    }

    /**
     * 校验是否是数字
     */
    public static boolean checkNumber(String number) {
        String reg = "\\d+(\\.\\d+)?";
        return !number.matches(reg);
    }

    private String getImageUrl(Map<String, String> imageSpaceUrlMap, String imageUrl) {
        if (imageSpaceUrlMap.get(imageUrl) != null) {
            return imageSpaceUrlMap.get(imageUrl);
        } else {
            if (imageUrl.startsWith("http")) {
                return imageUrl;
            }
            if (UnicodeUtils.isChinese(imageUrl)) {
                String encoded = URLEncoder.encode(imageUrl, StandardCharsets.UTF_8);
                if (imageSpaceUrlMap.get(encoded) != null) {
                    return imageSpaceUrlMap.get(encoded);
                }
            }
            return "";
        }
    }

    private void setGoodsCommon(GoodsCommonEntity commonEntity, Integer brandId, String brandName, Integer shopId, String shopName,
                                WebShopListQueryEsDTO webShopListQueryResult, Integer transportTemplate, Integer transportAreaId) {
        List<String> afterServiceList = Arrays.asList("极速退款", "增值速开", "正品保证", "售后保障");
        commonEntity.setBrandId(brandId);
        commonEntity.setBrandName(brandName);
        commonEntity.setShopId(shopId);
        commonEntity.setShopName(shopName);
        commonEntity.setGoodsId("0");
        commonEntity.setCommonState(1);
        if (webShopListQueryResult != null && webShopListQueryResult.getShopSelfSupport().equals("true")) {
            commonEntity.setShopSelfSupport(1);
            commonEntity.setCommonVerify(CommonType.productReview.PASS.getCode());
        } else {
            commonEntity.setShopSelfSupport(0);
            commonEntity.setCommonVerify(CommonType.productReview.UNDER_REVIEW.getCode());
        }
        commonEntity.setShopStatus(3);
        commonEntity.setCommonAddTime(DateUtils.getNowDateToDf());
        commonEntity.setCommonVerifyTime(DateUtils.getNowDateToDf());
        commonEntity.setCommonUpdateTime(DateUtils.getNowDateToDf());
        commonEntity.setCommonSellTime(DateUtils.getNowDateToDf());
        commonEntity.setCommonCostPrice(BigDecimal.ZERO);
        commonEntity.setCommonMarketPrice(BigDecimal.ZERO);
        // 运费模板id
        commonEntity.setTransportTemplate(transportTemplate);
        // 售卖区域id
        commonEntity.setTransportAreaId(transportAreaId);
    }

    private GoodsBaseEntity setGoodsBaseEntity(Integer shopId, String shopName, Integer catId, Integer brandId) {
        GoodsBaseEntity baseEntity = new GoodsBaseEntity();
        baseEntity.setShopId(shopId);
        baseEntity.setShopName(shopName);
        baseEntity.setCatId(catId);
        baseEntity.setBrandId(brandId);
        baseEntity.setGoodsMarketPrice(BigDecimal.ZERO);
        baseEntity.setGoodsCostPrice(BigDecimal.ZERO);
        baseEntity.setGoodsShopGateStock(0);
        baseEntity.setGoodsIsRecommend(1);
        return baseEntity;
    }

    /**
     * 查询商品的规格
     *
     * @param commonId
     * @return
     */
    @Override
    public String queryGoodsSpecStr(Integer commonId) {
        String goodsSpecStr = null;
        List<String> goodsSpecList = goodsBaseMapper.queryGoodsSpecStr(commonId);
        for (String s : goodsSpecList) {
            if (null == goodsSpecStr) {
                goodsSpecStr = s;
            } else {
                goodsSpecStr = goodsSpecStr + "," + s;
            }
        }

        return goodsSpecStr;
    }

    @Override
    public ResponseResult platFormQueryDealerGoods(PlatFormAllGoodsDTO platFormAllGoodsDTO) {
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(platFormAllGoodsDTO.getToken());
        if (null == userInfo) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        Map data = null;
        try {
            data = ParseMapUtils.beanToMap(platFormAllGoodsDTO);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        IPage<QueryDealerGoodsVO> page = new Page<>();
        if(platFormAllGoodsDTO.getSupplierSource() == CommonType.SupplierSource.JING_DONG.getCode()){
            // 查询京东代发 商品信息
            page = goodsJingdongDropshippingMapper.platFormQueryDealerGoods(new Query<QueryDealerGoodsVO>().getPage(data),platFormAllGoodsDTO);
            for (QueryDealerGoodsVO record : page.getRecords()) {
                Integer num = goodsJingdongDropshippingMapper.getDealerGoodsNum(record.getSupplyCommonId());
                record.setDealerGoodsNum(num);
            }
        }else if(platFormAllGoodsDTO.getSupplierSource() == CommonType.SupplierSource.YI_YA_TONG.getCode()){
            // 查询怡亚通代发 商品信息
            page = supplierGoodsYiyatongMapper.platFormQueryDealerGoods(new Query<QueryDealerGoodsVO>().getPage(data),platFormAllGoodsDTO);
            for (QueryDealerGoodsVO record : page.getRecords()) {
                Integer num = supplierGoodsYiyatongMapper.getDealerGoodsNum(record.getSupplyCommonId());
                record.setDealerGoodsNum(num);
            }
        }else{
            // 查询平台供应商代发 商品信息
            page = goodsDealerPutawayRecordMapper.platFormQueryDealerGoods(new Query<QueryDealerGoodsVO>().getPage(data),platFormAllGoodsDTO);
            for (QueryDealerGoodsVO record : page.getRecords()) {
                Integer num = goodsDealerPutawayRecordMapper.getDealerGoodsNum(record.getSupplyCommonId());
                record.setDealerGoodsNum(num);
            }
        }
        return ResponseResult.success(PageUtils.getPage(page));
    }

    @Override
    public ResponseResult platFormQueryDealerShop(PlatFormAllGoodsDTO platFormAllGoodsDTO) {
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(platFormAllGoodsDTO.getToken());
        if (null == userInfo) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        Map data = null;
        try {
            data = ParseMapUtils.beanToMap(platFormAllGoodsDTO);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        IPage<QueryDealerGoodsVO> page = new Page<>();
        if(platFormAllGoodsDTO.getSupplierSource() == CommonType.SupplierSource.JING_DONG.getCode()){
            // 查询京东代发 商品信息
            page = goodsJingdongDropshippingMapper.platFormQueryDealerShop(new Query<QueryDealerGoodsVO>().getPage(data),platFormAllGoodsDTO);

        }else if(platFormAllGoodsDTO.getSupplierSource() == CommonType.SupplierSource.YI_YA_TONG.getCode()){
            // 查询怡亚通代发 商品信息
            page = supplierGoodsYiyatongMapper.platFormQueryDealerShop(new Query<QueryDealerGoodsVO>().getPage(data),platFormAllGoodsDTO);
        }else{
            // 查询平台供应商代发 商品信息
            page = goodsDealerPutawayRecordMapper.platFormQueryDealerShop(new Query<QueryDealerGoodsVO>().getPage(data),platFormAllGoodsDTO);
        }
        return ResponseResult.success(PageUtils.getPage(page));
    }
}

