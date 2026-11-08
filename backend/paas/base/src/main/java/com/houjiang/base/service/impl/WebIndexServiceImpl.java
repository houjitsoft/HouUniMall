package com.houjiang.base.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.jfinal.weixin.sdk.utils.HttpUtils;
import com.houjiang.base.dto.*;
import com.houjiang.base.entity.BaseFloorAdpageEntity;
import com.houjiang.base.feign.BusinessClient;
import com.houjiang.base.feign.GoodsClient;
import com.houjiang.base.feign.ImClient;
import com.houjiang.base.mapper.*;
import com.houjiang.base.service.WebIndexService;
import com.houjiang.base.vo.*;
import com.houjiang.base.vo.index.*;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.constant.RedisConstants;
import com.houjiang.commons.dto.goods.CatIdsDTO;
import com.houjiang.commons.dto.goods.WebGoodsCatQueryDTO;
import com.houjiang.commons.dto.goods.WebGoodsListQueryDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.json.JSONSerializer;
import com.houjiang.commons.util.json.JsonUtils;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.RedisPrefixUtils;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.excel.Constants;
import com.houjiang.commons.vo.base.WabSystemConfigHallVO;
import com.houjiang.commons.vo.goods.GoodsCatVO;
import com.houjiang.commons.vo.goods.GoodsIdsVO;
import com.houjiang.commons.vo.goods.SecordNameListVO;
import com.houjiang.commons.vo.goodscat.GoodsCatSearchChildVO;
import com.houjiang.commons.vo.goodscat.WebCatMenuVO;
import com.houjiang.commons.vo.shop.PcConfigShopVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.commons.config.RedisCache;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @ClassName WebIndexServiceImpl
 * @Description pc首页装修
 * @Author Amos
 * @Date 2023/5/24 9:18
 * @Version 1.0
 * @author Amos
 */
@Service
@Component
public class WebIndexServiceImpl implements WebIndexService {

    @Value("${spring.cloud.nacos.config.server-addr}")
    private String address;

    @Value("${spring.cloud.nacos.config.username}")
    private String username;

    @Value("${spring.cloud.nacos.config.password}")
    private String password;

    @Value("${spring.cloud.nacos.config.namespace}")
    private String namespace;

    @Autowired
    private BaseSysConfigsMapper baseSysConfigsMapper;
    @Autowired
    private BaseArticleGroupMapper baseArticleGroupMapper;
    @Autowired
    private BaseArticleBaseMapper baseArticleBaseMapper;
    @Autowired
    private WebIndexMapper webIndexMapper;
    @Autowired
    private BaseFloorAdpageMapper baseFloorAdpageMapper;
    @Autowired
    private BaseSystemPlatformNavMapper baseSystemPlatformNavMapper;
    @Autowired
    private BasePcIndexConfigMapper basePcIndexConfigMapper;
    @Autowired
    private BasePcIndexConfigServiceImpl basePcIndexConfigService;
    @Autowired
    private ImClient imClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private BusinessClient businessClient;

    @Autowired
    private RedisCache redisCache;

    /**
     * 查询所有广告页面布局信息
     * 该方法通过调用数据库查询获取所有商家广告页面的布局信息，并进一步处理这些信息，包括解析JSON字符串、查询关联数据等，以组装成最终的业务对象。
     *
     * @return ResponseResult 包含所有广告页面布局信息的响应结果对象
     */
    @Override
    public ResponseResult queryAllAdpageFloor() {
        // 根据商家类型查询广告页面布局实体列表
        List<BaseFloorAdpageEntity> entityList = baseFloorAdpageMapper.selectList(new LambdaQueryWrapper<BaseFloorAdpageEntity>()
                .eq(BaseFloorAdpageEntity::getFloorType, CommonType.storeType.SELLER_STORE.getCode()));

        /**
         * 封装数据
         */
        List<MerchantFloorAdpageVO> allFloorAdpages = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityList)) {
            allFloorAdpages = entityList.stream().map(entity -> {
                MerchantFloorAdpageVO catResult = new MerchantFloorAdpageVO();

                BeanUtils.copyProperties(entity, catResult);
                return catResult;
            }).collect(Collectors.toList());
        }

        // 遍历所有广告页面布局，进一步处理每个页面的详细信息
        for (MerchantFloorAdpageVO allFloorAdpage : allFloorAdpages) {
            // 解析楼层名称JSON，获取具体楼层名称
            WebFloorAdpageNameDTO floorAdpageNameNew = JsonUtils.string2Obj(allFloorAdpage.getFloorAdpageName(),
                    WebFloorAdpageNameDTO.class);
            if (floorAdpageNameNew == null || !StringUtils.isNotBlank(floorAdpageNameNew.getCatName())) {
                Integer parsedCatId = floorAdpageNameNew == null ? 0 : floorAdpageNameNew.getCatId();
                floorAdpageNameNew = new WebFloorAdpageNameDTO();
                floorAdpageNameNew.setCatId(parsedCatId == null ? 0 : parsedCatId);
                floorAdpageNameNew.setCatName(StringUtils.defaultIfBlank(allFloorAdpage.getFloorAdpageName(), "商品分类"));
            }
            allFloorAdpage.setFloorAdpageNameNew(floorAdpageNameNew);

            // 根据广告轮播图ID列表查询对应的广告信息
            List<Integer> goodsIdList2 = JsonUtils.StringToList2(allFloorAdpage.getLeftAdpageBanner());
            List<GoodsIdsQueryListVO> adpageBannerList = webIndexMapper.selectByGoodIds(goodsIdList2);
            allFloorAdpage.setAdpageBannerList(adpageBannerList);

            // 解析左侧导航栏JSON，获取导航项列表
            List<PlatformLeftAdpageNavDTO> leftAdpageNavList = JsonUtils.string2Obj(allFloorAdpage.getLeftAdpageNav(),
                    List.class, PlatformLeftAdpageNavDTO.class);
            for (PlatformLeftAdpageNavDTO platformLeftAdpageNav : leftAdpageNavList) {
                // 根据导航链接处理逻辑，查询并设置相应的名称信息（商品、店铺、分类）
                String leftAdpageNavUrl = String.valueOf(platformLeftAdpageNav.getLeftAdpageNavUrl());
                if (StringUtils.isNotEmpty(leftAdpageNavUrl)) {
                    Map urlMap = JSON.parseObject(leftAdpageNavUrl, Map.class);
                    if (platformLeftAdpageNav.getOpType() != null) {
                        // 封装商品信息
                        if (platformLeftAdpageNav.getOpType().equals(2) && null != urlMap.get("commonId")) {
                            String commonId = String.valueOf(urlMap.get("commonId"));
                            if (StringUtils.isNotEmpty(commonId)) {
                                String commonName = goodsClient.selectCommonName(Integer.valueOf(commonId));
                                platformLeftAdpageNav.setCommonName(commonName);
                            }
                            // 封装店铺信息
                        } else if (platformLeftAdpageNav.getOpType().equals(4) && null != urlMap.get("shopId")) {
                            String shopId = String.valueOf(urlMap.get("shopId"));
                            if (StringUtils.isNotEmpty(shopId)) {
                                String shopName = businessClient.selectByShopName(Integer.valueOf(shopId));
                                platformLeftAdpageNav.setShopName(shopName);
                            }
                            // 封装分类信息
                        } else if (platformLeftAdpageNav.getOpType().equals(6) && null != urlMap.get("catId")) {
                            String catId = String.valueOf(urlMap.get("catId"));
                            if (StringUtils.isNotEmpty(catId)) {
                                String catName = goodsClient.selectByCatNameString(Integer.valueOf(catId));
                                platformLeftAdpageNav.setCatName(catName);
                            }
                        }
                    }
                }
            }
            allFloorAdpage.setLeftAdpageNavList(leftAdpageNavList);

            // 解析右侧热门推荐JSON，获取热门推荐信息
            PlatformRightAdpageNavDTO rightRec = JsonUtils.string2Obj(allFloorAdpage.getRightHotRecommend(),
                    PlatformRightAdpageNavDTO.class);
            allFloorAdpage.setRightRec(rightRec);

            // 根据右侧热门推荐商品ID列表，查询并设置热门推荐商品信息
            if (StringUtils.isNotEmpty(rightRec.getRightAdpageNavGoodsId())) {
                List<Integer> rightAdpageNavGoodsIds = JsonUtils.StringToList2(rightRec.getRightAdpageNavGoodsId());

                List<GoodsIdsVO> rightRecGoodsList = new ArrayList<>();
                MobConfigGoodsVO query = new MobConfigGoodsVO();
                List<WebCatMenuVO> allMenu = null;
                // 获取所有的子分类
                if (null == allMenu) {
                    allMenu = goodsClient.selectAllCat2();
                }
                List<Integer> catIds = getMenuList(allMenu, floorAdpageNameNew.getCatId());
                query.setCatIds(catIds);
                query.setGoodsIdList(rightAdpageNavGoodsIds);
                query.setGoodsNum(8 - rightRecGoodsList.size());
                List<Integer> goodsVarList = query.getGoodsIdList();
                for (Integer goodsId : goodsVarList) {
                    GoodsIdsVO param = goodsClient.newSelectWebFloorMobConfig(goodsId);
                    if (param != null) {
                        rightRecGoodsList.add(param);
                    }
                }
                allFloorAdpage.setRightRecGoodsList(rightRecGoodsList);
            }

            // 解析右侧分类广告JSON，获取右侧分类广告信息
            allFloorAdpage.setRightAdpageNavList(JsonUtils.string2Obj(allFloorAdpage.getRightAdpageNav(), List.class,
                    WebFloorAdpageNameVO.class));
        }
        return ResponseResult.success(allFloorAdpages);
    }

    /**
     * 获取指定分类及其子分类的ID列表。
     *
     * @param allMenu 所有菜单项的列表，包含分类信息。
     * @param catId   指定的分类ID。
     * @return 指定分类及其子分类的ID列表。
     */
    public List<Integer> getMenuList(List<WebCatMenuVO> allMenu, Integer catId) {
        // 初始化用于存储所有子分类ID的列表
        List<Integer> allChildCatIds = Lists.newArrayList();

        // 检查输入的菜单列表是否为空
        if (!org.springframework.util.CollectionUtils.isEmpty(allMenu)) {
            // 遍历所有菜单项，寻找指定分类及其子分类
            for (WebCatMenuVO catMenu : allMenu) {
                // 当找到指定分类时，将其ID添加到列表中，并递归获取其子分类
                if (catMenu.getCatId().equals(catId)) {
                    allChildCatIds.add(catId);
                    getChild(allMenu, catMenu.getCatId(), allChildCatIds);
                }

            }
        }
        // 返回所有子分类ID的列表
        return allChildCatIds;

    }

    /**
     * 递归获取指定父分类的所有子分类ID。
     *
     * @param allMenu        所有菜单项的列表，包含分类信息。
     * @param parentId       指定的父分类ID。
     * @param allChildCatIds 用于存储所有子分类ID的列表。
     * @return 子分类ID列表，如果没有子分类则返回null。
     */
    private List<Integer> getChild(List<WebCatMenuVO> allMenu, Integer parentId, List<Integer> allChildCatIds) {

        // 初始化用于存储当前层级子分类ID的列表
        List<Integer> childList = Lists.newArrayList();
        // 遍历所有菜单项，寻找指定父分类的子分类
        for (WebCatMenuVO catMenu : allMenu) {
            // 当找到指定父分类的子分类时，将其ID添加到列表中，并递归获取其子分类
            if (catMenu.getCatParentId().equals(parentId)) {
                allChildCatIds.add(catMenu.getCatId());
                childList.add(catMenu.getCatId());
                getChild(allMenu, catMenu.getCatId(), allChildCatIds);
            }
        }
        // 如果当前层级没有子分类，则返回null，表示无需继续递归
        if (org.springframework.util.CollectionUtils.isEmpty(childList)) {
            return null;
        }
        // 否则，返回所有子分类ID的列表
        return allChildCatIds;
    }


    /**
     * 查询首页信息
     *
     * @param integralType 积分类型，用于筛选商品分类中的积分商品
     * @return 首页信息的响应结果
     */
    @Override
    public ResponseResult queryIndex(Integer integralType) {
        WebIndexQueryVO result = new WebIndexQueryVO();

        // 查询平台导航
        // 查询首页导航
        List<WebSystemPlatformNavVO> platformNav = baseSystemPlatformNavMapper.selectPlatformNav(CommonType.ShopType.COMMON_SHOP.getCode());

        // 查询首页轮播图配置
        // 轮播
        WebSystemConfigQueryDTO param = new WebSystemConfigQueryDTO();
        param.setConfigType("index_slider");
        List<WebSystemConfigQueryVO> indexSlides = baseSysConfigsMapper.selectByParam(param);

        // 查询首页轮播图下的广告配置
        // 轮播下广告
        WebSystemConfigQueryDTO adParam = new WebSystemConfigQueryDTO();
        adParam.setConfigType("pc_index_ad");
        List<WebSystemConfigQueryVO> indexSlidesAds = baseSysConfigsMapper.selectByParam(adParam);

        // 查询一级商品分类
        // 查询一级分类
        WebGoodsCatQueryDTO catQueryParam = new WebGoodsCatQueryDTO();
        catQueryParam.setCatId(0);
        if (null == integralType) {
            catQueryParam.setIsIntegralGoods(2);
        } else {
            catQueryParam.setIsIntegralGoods(integralType);
        }
        ResponseResult goodsCatData = goodsClient.selectByParam(catQueryParam);
        List<GoodsCatVO> goodsCats = JSON.parseArray(JSON.toJSONString(goodsCatData.getData()),GoodsCatVO.class);
        // 为每个一级分类查询其二级子分类
        for (GoodsCatVO goodsCatResult : goodsCats) {
            List<SecordNameListVO> secordNameList = goodsClient.selectByCatParentId(goodsCatResult.getCatId());
            goodsCatResult.setSecordNameList(secordNameList);
        }

        // 查询热搜词
        List<WabSystemConfigHallVO> slides = baseSysConfigsMapper.selectByConfigTypesearch("search");
        String hotWordsStr = slides.get(0).getConfigValue();
        List<String> hotWords = JsonUtils.StringToList3(hotWordsStr);

        // 查询疯狂快报文章组
        WebIndexArticleGroupVO articleGroup = null;
        List<WebIndexArticleGroupVO> articleGroups = baseArticleGroupMapper.selectAllGroup();
        for (WebIndexArticleGroupVO articleGroup1 : articleGroups) {
            if (1 == articleGroup1.getArticleGroupSort()) {
                articleGroup = baseArticleGroupMapper.selectByArticleGroupId(articleGroup1.getArticleGroupId());
                List<WebArticleBaseNameVO> articleBase = baseArticleBaseMapper.selectByGroupId(articleGroup.getArticleGroupId(), 7);
                articleGroup.setArticletitle2(articleBase);
                break;
            }
        }

        // 组装查询结果
        result.setPlatformNav(platformNav);
        result.setIndexSlides(indexSlides);
        result.setGoodsCats(goodsCats);
        result.setHotWords(hotWords);
        result.setArticleGroup(articleGroup);
        result.setIndexSlidesAds(indexSlidesAds);
        return ResponseResult.success(result);
    }

    /**
     * 查询PC端首页配置信息
     * 从Redis中获取配置信息，如果存在则直接返回，如果不存在则从数据库中查询并存入Redis
     *
     * @return 返回配置信息的结果对象
     */
    @Override
    public ResponseResult selectPcIndexConfigAll() {
        // 初始化结果列表
        List<PcConfigResultVO> result = new ArrayList<>();
        try {
            // 尝试从Redis中获取配置信息
            String pcConfig = (String) redisCache.getCacheObject(RedisPrefixUtils.pcIndexConfig2);
            if (pcConfig != null) {
                // 从Redis中解析出配置信息
                result = JsonUtils.string2Obj(pcConfig, List.class, PcConfigResultVO.class);
                if (!CollectionUtils.isEmpty(result)) {
                    // 对配置中的商品信息进行补充查询
                    for (PcConfigResultVO pcConfigResult : result) {
                        if (pcConfigResult.getGoods() != null && pcConfigResult.getGoods().size() > 0) {
                            List<PcConfigGoodsVO> goods = new ArrayList<>();
                            for (PcConfigGoodsVO good : pcConfigResult.getGoods()) {
                                PcConfigGoodsVO pcConfigGoodsParam = webIndexMapper.pcConfigGoods(good.getCommonId());
                                if (pcConfigGoodsParam != null) {
                                    goods.add(pcConfigGoodsParam);
                                }
                            }
                            pcConfigResult.getGoods().clear();
                            pcConfigResult.getGoods().addAll(goods);
                        }
                    }
                    return ResponseResult.success(result);
                } else {
                    // 如果解析出的配置信息为空，则清除Redis中的相关配置信息
                    redisCache.deleteObject(RedisPrefixUtils.pcIndexConfig);
                    redisCache.deleteObject(RedisPrefixUtils.pcIndexConfig2);
                }
            }
            // 从数据库中查询全部配置信息
            GdbPcIndexConfigVO pcIndexConfig = new GdbPcIndexConfigVO();
            //查询所有数据 解析
            List<GdbPcIndexConfigVO> gdbPcIndexConfigs = basePcIndexConfigMapper.selectPcIndexConfig(pcIndexConfig);
            if (null != gdbPcIndexConfigs && gdbPcIndexConfigs.size() > 0) {
                for (GdbPcIndexConfigVO gdbPcIndexConfig : gdbPcIndexConfigs) {
                    //用与存放 商品 店铺数据
                    PcConfigResultVO pcConfigResult = new PcConfigResultVO();
                    pcConfigResult.setPageType(gdbPcIndexConfig.getPageType());
                    pcConfigResult.setPageName(gdbPcIndexConfig.getPageName());
                    pcConfigResult.setPageTitle(gdbPcIndexConfig.getPageTitle());
                    pcConfigResult.setPageTwoTitle(gdbPcIndexConfig.getPageTwoTitle());
                    pcConfigResult.setPageId(gdbPcIndexConfig.getId());
                    pcConfigResult.setPageStart(gdbPcIndexConfig.getPageStart());
                    // 获取页面数据
                    String data = gdbPcIndexConfig.getPageData();
                    if (null != data && "" != data) {
                        GdbPcIndexConfigDataVO gdbPcIndexConfigData = JSON.parseObject(data, GdbPcIndexConfigDataVO.class);
                        List<Integer> ids = gdbPcIndexConfigData.getData();
                        //商品
                        if (null != gdbPcIndexConfigData.getType() && 1 == gdbPcIndexConfigData.getType()) {
                            //存放商品信息
                            List<PcConfigGoodsVO> goods = new ArrayList<>();
                            if (null != ids) {
                                for (Integer id : ids) {
                                    //根据common 查询商品信息
                                    PcConfigGoodsVO pcConfigGoodsParam = webIndexMapper.pcConfigGoods(id);
                                    if (null != pcConfigGoodsParam) {
                                        goods.add(pcConfigGoodsParam);
                                    }
                                }
                                pcConfigResult.setGoods(goods);
                            }
                        //店铺
                        } else if (null != gdbPcIndexConfigData.getType() && 2 == gdbPcIndexConfigData.getType()) {
                            //存放店铺信息
                            List<PcConfigShopVO> shops = new ArrayList<>();
                            if (null != ids) {
                                for (Integer id : ids) {
                                    //根据shopid
                                    PcConfigShopVO pcConfigShopParam = businessClient.pcConfigShop(id);
                                    if (null != pcConfigShopParam) {
                                        if (StringUtils.isEmpty(pcConfigShopParam.getCommonImage())) {
                                            String icoShop = baseSysConfigsMapper.selectConfigValue("logo_copywriting_ico_shop");
                                            pcConfigShopParam.setCommonImage(icoShop);
                                        }
                                        shops.add(pcConfigShopParam);
                                    }
                                }
                                pcConfigResult.setShops(shops);
                            }
                        //分类
                        } else if (null != gdbPcIndexConfigData.getType() && 3 == gdbPcIndexConfigData.getType()) {
                            PcConfigCatVO pcConfigCatParam = new PcConfigCatVO();
                            pcConfigCatParam.setCatImage(gdbPcIndexConfigData.getImages());
                            if (null != ids) {
                                for (Integer id : ids) {
                                    pcConfigCatParam.setCatUrl(String.valueOf(id));
                                }
                            }
                            pcConfigResult.setCat(pcConfigCatParam);
                        }
                    }
                    result.add(pcConfigResult);
                }
                if (null != result) {
                    redisCache.setCacheObject(RedisPrefixUtils.pcIndexConfig2, JSONSerializer.serialize(result));
                    //存入redis
                    redisCache.expire(RedisPrefixUtils.pcIndexConfig2, 60 * 60 * 24, TimeUnit.SECONDS);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.success(result);
    }

    /**
     * 根据商品分类ID查询商品列表
     *
     * @param catId     商品分类ID
     * @param goodsType 商品类型
     * @param token     用户令牌
     * @return 返回查询结果
     */
    @Override
    public ResponseResult queryByGoodsCatId(Integer catId, Integer goodsType, String token) {
        // 初始化商品分类查询结果
        GoodsCatSearchChildVO goodsCatSearchChild = null;
        List<WebCatMenuVO> allMenu = null;
        List<Integer> catIds = null;
        if (catId != null) {
            // 尝试从Redis中获取商品分类信息
            String cacheCatIds = (String) redisCache.getCacheObject(Constants.CACHE_GOODS_WEB_GOODS_LIST_HEADER_PREFIX + "_" + catId);
            if (cacheCatIds != null && !cacheCatIds.equals("")) {
                goodsCatSearchChild = JsonUtils.string2Obj(cacheCatIds, GoodsCatSearchChildVO.class);
                catIds = goodsCatSearchChild.getCatIds();
            } else {
                // 从数据库中查询所有下级分类
                allMenu = goodsClient.selectAllCat2();
                catIds = getMenuList(allMenu, catId);
                goodsCatSearchChild = new GoodsCatSearchChildVO();
                goodsCatSearchChild.setAllMenu(allMenu);
                goodsCatSearchChild.setCatIds(catIds);
                // 将查询结果存入Redis
                redisCache.setCacheObject(Constants.CACHE_GOODS_WEB_GOODS_LIST_HEADER_PREFIX + "_" + catId,
                        JsonUtils.toString(goodsCatSearchChild));
                redisCache.expire(Constants.CACHE_GOODS_WEB_GOODS_LIST_HEADER_PREFIX + "_" + catId, RedisUtil.EXRP_MONTH, TimeUnit.SECONDS);
            }
            // 添加当前分类ID到列表中
            catIds.add(catId);
        }


        CatIdsDTO catIdsDTO= new CatIdsDTO();
        catIdsDTO.setCatIdList(catIds);
        catIdsDTO.setGoodsType(goodsType);

        // 根据分类ID列表查询商品信息
        List<GoodsIdsQueryVO> results = goodsClient.selectByCatIds(catIdsDTO);
        // 根据用户令牌获取用户信息
        UserInfoVO userInfo = null;
        if (StringUtils.isNotBlank(token)) {
            userInfo = ParseTokenUtils.getUserInfoByToken(token);
        }
        if (null != userInfo && null != goodsType && 2 == goodsType) {
            for (GoodsIdsQueryVO goods : results) {
                goods.setOperateType2(goods);
            }
        }
        return ResponseResult.success(results);
    }

    /**
     * 根据广告页面楼层ID查询对应的商品信息。
     *
     * @param map 包含商品ID列表和用户信息的映射表。
     * @return 返回查询到的商品信息列表。
     */
    @Override
    public ResponseResult queryAdpageFloorGoods(Map<String, Object> map) {
        // 尝试从映射表中获取用户信息
        UserInfoVO userInfo = null;
        Object tokenObj = map.get("token");
        if (null != tokenObj) {
            userInfo = ParseTokenUtils.getUserInfoByToken(tokenObj.toString());
        }

        // 初始化商品ID列表和商品信息列表
        List<Integer> goodsIds = new ArrayList<>();
        List<GoodsIdsQueryVO> goodsList = new ArrayList<>();

        // 如果用户信息存在
        if (null != userInfo) {
            // 尝试从映射表中获取商品ID列表
            if (null != map.get("goodsIds")) {
                goodsIds = (List<Integer>) map.get("goodsIds");
                goodsList = webIndexMapper.selectGoodsByGoodIds(goodsIds);


                // 如果没有经销商申请信息，为所有商品设置操作类型为查看
                for (GoodsIdsQueryVO goods : goodsList) {
                    goods.setOperateType2(goods);
                }
            }

        } else {
            // 如果用户信息不存在，但仍存在商品ID列表
            if (null != map.get("goodsIds")) {
                goodsIds = (List<Integer>) map.get("goodsIds");
                goodsList = webIndexMapper.selectGoodsByGoodIds(goodsIds);
            }
        }

        // 返回查询结果
        return ResponseResult.success(goodsList);
    }

    /**
     * 根据分类父ID和店铺ID查询商品分类。
     *
     * @param catParentId 商品分类的父ID。
     * @param shopId      店铺ID。
     * @return 返回查询到的商品分类结果列表。
     */
    @Override
    public ResponseResult queryItemCat(Integer catParentId, Integer shopId) {
        List<GoodCatsResultVO> result = new ArrayList<>();

        // 如果没有指定店铺ID
        if (shopId == null) {
            // 从缓存中获取分类结果，此处注释掉了实际的缓存操作

            // 如果缓存中不存在，从数据库中查询分类
            // 获取所有子分类ID
            List<Integer> catIds = null;
            WebGoodsCatQueryDTO param = getChildCatList(catIds, catParentId);

            ResponseResult goodsCatData = goodsClient.selectByParam(param);
            List<GoodsCatVO> goodsCats = JSON.parseArray(JSON.toJSONString(goodsCatData.getData()),GoodsCatVO.class);


            // 如果父ID不为0，说明需要查询二级分类下的三级分类
            if (catParentId != 0) {
                for (GoodsCatVO goodsCat : goodsCats) {
                    GoodCatsResultVO cats = new GoodCatsResultVO();

                    Integer catId = goodsCat.getCatId();
                    param = getChildCatList(catIds, catId);

                    // 查询三级分类
                    ResponseResult goodsCatResult = goodsClient.selectByParam(param);
                    List<GoodsCatVO> threeGoodsCats = JSON.parseArray(JSON.toJSONString(goodsCatResult.getData()),GoodsCatVO.class);

                    cats.setSecordCatId(goodsCat.getCatId());
                    cats.setGdsCats(threeGoodsCats);
                    cats.setCatPic(goodsCat.getCatPic());
                    cats.setSecordCatName(goodsCat.getCatName());

                    result.add(cats);
                }
            } else {
                GoodCatsResultVO cats = new GoodCatsResultVO();
                cats.setGdsCats(goodsCats);
                result.add(cats);
            }

            result = result.stream()
                    .filter(parent -> parent.getGdsCats() != null && !parent.getGdsCats().isEmpty())
                    .toList();

            // 将查询结果存入缓存，此处注释掉了实际的缓存操作
        } else {
            // 根据店铺ID查询自定义分类
            if (catParentId == 0) {
                List<GoodsCatVO> goodsCatResults = businessClient.selectByShopId1(catParentId, shopId);
                GoodCatsResultVO cats = new GoodCatsResultVO();
                cats.setGdsCats(goodsCatResults);
                result.add(cats);
            } else {
                List<GoodsCatVO> goodsCatResults = businessClient.selectByShopId2(catParentId, shopId);
                GoodCatsResultVO cats = new GoodCatsResultVO();
                cats.setGdsCats(goodsCatResults);
                result.add(cats);
            }
        }

        // 返回查询结果
        return ResponseResult.success(result);
    }

    /**
     * 根据分类ID获取子分类列表。
     *
     * @param catIds 存储所有分类ID的列表。
     * @param catId  当前分类的ID。
     * @return 包含子分类ID列表的查询DTO。
     */
    private WebGoodsCatQueryDTO getChildCatList(List<Integer> catIds, Integer catId) {
        // 从缓存中尝试获取子分类信息
        GoodsCatSearchChildVO goodsCatSearchChild;
        List<WebCatMenuVO> allMenu;
        if (null != catId) {
            // 从缓存中获取
            String cacheCatIds = (String) redisCache.getCacheObject(Constants.CACHE_GOODS_WEB_GOODS_LIST_HEADER_PREFIX + "_" + catId);
            // 查询所有下级分类
            allMenu = goodsClient.selectAllCat2();
            if (null != cacheCatIds && !cacheCatIds.equals("")) {
                goodsCatSearchChild = JsonUtils.string2Obj(cacheCatIds, GoodsCatSearchChildVO.class);
                catIds = goodsCatSearchChild.getCatIds();
            } else {
                allMenu = goodsClient.selectAllCat2();
                catIds = getMenuList(allMenu, catId);
                goodsCatSearchChild = new GoodsCatSearchChildVO();
                goodsCatSearchChild.setAllMenu(allMenu);
                goodsCatSearchChild.setCatIds(catIds);
                // 缓存子分类信息
                redisCache.setCacheObject(Constants.CACHE_GOODS_WEB_GOODS_LIST_HEADER_PREFIX + "_" + catId,
                        JsonUtils.toString(goodsCatSearchChild));
                redisCache.expire(Constants.CACHE_GOODS_WEB_GOODS_LIST_HEADER_PREFIX + "_" + catId, RedisUtil.EXRP_MONTH, TimeUnit.SECONDS);
            }
        }

        WebGoodsCatQueryDTO param = new WebGoodsCatQueryDTO();
        param.setCatIdList(catIds);
        param.setCatId(catId);
        return param;
    }

    /**
     * 查询平台导航信息。
     *
     * @return 包含导航信息的结果对象。
     */
    @Override
    public ResponseResult queryPlatformNav() {
        WebPlatformNavResultVO result = new WebPlatformNavResultVO();
        // 查询首页导航
        List<WebSystemPlatformNavVO> platformNav = baseSystemPlatformNavMapper.selectPlatformNav(CommonType.ShopType.COMMON_SHOP.getCode());

        // 热搜词
        List<WabSystemConfigHallVO> slides = baseSysConfigsMapper.selectByConfigTypesearch("search");
        String hotWordsStr = slides.get(0).getConfigValue();
        List<String> hotWords = JsonUtils.StringToList3(hotWordsStr);
        result.setHotWords(hotWords);
        result.setPlatformNav(platformNav);
        return ResponseResult.success(result);
    }

    /**
     * 查询商品列表头部的筛选条件。
     *
     * @param param 查询参数DTO。
     * @return 包含筛选条件的结果对象。
     */
    @Override
    public ResponseResult queryGoodsListHeaderFilter(WebGoodsListQueryDTO param) {
        List<WebCatMenuVO> allMenu = null;
        GoodsCatSearchChildVO goodsCatSearchChild = null;
        List<SecordNameListVO> nextCatIds = null;

        Integer catId = param.getCatId();

        List<Integer> catIds = null;

        if (null != param.getCatId()) {

            // 从缓存中获取子分类信息
            String cacheCatIds = (String) redisCache
                    .getCacheObject(Constants.CACHE_GOODS_WEB_GOODS_LIST_HEADER_PREFIX + "_" + param.getCatId());
            // 查询所有下级分类
            if (null != cacheCatIds && !cacheCatIds.equals("")) {
                goodsCatSearchChild = JsonUtils.string2Obj(cacheCatIds, GoodsCatSearchChildVO.class);
                catIds = goodsCatSearchChild.getCatIds();
                allMenu = goodsCatSearchChild.getAllMenu();
            } else {
                allMenu = goodsClient.selectAllCat2();
                catIds = getMenuList(allMenu, param.getCatId());

                goodsCatSearchChild = new GoodsCatSearchChildVO();
                goodsCatSearchChild.setAllMenu(allMenu);
                goodsCatSearchChild.setCatIds(catIds);
                // 缓存子分类信息
                redisCache.setCacheObject(Constants.CACHE_GOODS_WEB_GOODS_LIST_HEADER_PREFIX + "_" + param.getCatId(),
                        JsonUtils.toString(goodsCatSearchChild));
                redisCache.expire(Constants.CACHE_GOODS_WEB_GOODS_LIST_HEADER_PREFIX + "_" + param.getCatId(), RedisUtil.EXRP_MONTH, TimeUnit.SECONDS);
            }

            if (!org.springframework.util.CollectionUtils.isEmpty(catIds)) {
                catIds.add(param.getCatId());
                param.setCatIds(catIds);
                param.setCatId(0);
            }
        }

        // 获取分类列表
        List<WebCatMenuVO> goodsCatsList = getMenuList4(allMenu, catId);

        // 背景图
        WebSystemConfigQueryDTO adParam = new WebSystemConfigQueryDTO();
        adParam.setConfigType("PCGoodsPage");
        List<WebSystemConfigQueryVO> pcGoodsPage = baseSysConfigsMapper.selectByParam(adParam);

        // 推荐商品
        if (null == catId || catId.equals("")) {
            catId = goodsClient.selectCatIdByCommonName(param.getCommonName());
            param.setCatId(catId);
        }
        List<GoodsIdsQueryVO> recommendGoodsList = webIndexMapper.selectRecGoods(param);
        if (org.springframework.util.CollectionUtils.isEmpty(recommendGoodsList) || recommendGoodsList.size() != 4) {
            param.setCatId(null);
            recommendGoodsList = webIndexMapper.selectRecGoods(param);
        }

        GoodsListHeaderResultVO result = new GoodsListHeaderResultVO();
        result.setGoodsCatsList(goodsCatsList != null ? goodsCatsList : new ArrayList<WebCatMenuVO>());
        result.setNextCatIds(nextCatIds != null ? nextCatIds : new ArrayList<SecordNameListVO>());
        result.setRecommendGoodsList(
                recommendGoodsList != null ? recommendGoodsList : new ArrayList<GoodsIdsQueryVO>());
        result.setPcGoodsPage(pcGoodsPage != null ? pcGoodsPage : new ArrayList<WebSystemConfigQueryVO>());
        return ResponseResult.success(result);
    }

    /**
     * 根据配置键获取图片配置URL。
     *
     * @param configKey 配置的键。
     * @return 包含图片URL的结果对象。
     */
    @Override
    public ResponseResult getImageConfigUrl(String configKey) {
        if (configKey == null || "".equals(configKey)) {
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.KEY_VALUE_CANNOT_BE_EMPTY.getMsg());
        }
        String imageValue = baseSysConfigsMapper.getImageConfigUrl(configKey);

        return ResponseResult.success(imageValue);
    }

    /**
     * 获取商品分类所有的子分类
     *
     * @param allMenu
     * @param catId
     * @return
     */
    public List<WebCatMenuVO> getMenuList4(List<WebCatMenuVO> allMenu, Integer catId) {
        List<WebCatMenuVO> goodsCatsList = Lists.newArrayList();

        if (!org.springframework.util.CollectionUtils.isEmpty(allMenu)) {
            for (WebCatMenuVO catMenu : allMenu) {
                if (catMenu.getCatId().equals(catId)) {
                    goodsCatsList.add(catMenu);
                    if (!catMenu.getCatParentId().equals(0)) {
                        goodsCatsList = getChild4(allMenu, catMenu.getCatParentId(), goodsCatsList);
                    }
                    break;
                }

            }
        }
        return goodsCatsList;

    }

    /**
     * 找出子菜单
     *
     * @param allMenu
     * @param parentId
     * @param goodsCatsList
     * @return
     */
    private List<WebCatMenuVO> getChild4(List<WebCatMenuVO> allMenu, Integer parentId, List<WebCatMenuVO> goodsCatsList) {

        for (WebCatMenuVO catMenu : allMenu) {
            if (catMenu.getCatId().equals(parentId)) {
                goodsCatsList.add(0, catMenu);
                parentId = catMenu.getCatParentId();
                if (!parentId.equals(0)) {
                    goodsCatsList = getChild4(allMenu, parentId, goodsCatsList);
                }
            }
        }

        return goodsCatsList;
    }

    /**
     * 查询底部帮助信息。
     * 该方法用于获取网站底部的帮助信息，包括文章组和网站主题色。
     *
     * @return ResponseResult 包含底部帮助信息的响应结果。
     */
    @Override
    public ResponseResult queryButtomHelp() {
        Map<String, Object> restultMap = new HashMap<>();
        // 查询所有文章组
        // 查询所有的组
        List<WebIndexArticleGroupVO> articleGroups = baseArticleGroupMapper.selectAllGroup();
        // 查询网站主题色
        String rr = baseSysConfigsMapper.selectBaseWebConfig(CommonType.configKey.WEB_COLOR.getDesc());
        restultMap.put("webColor", rr);

        for (WebIndexArticleGroupVO articleGroup : articleGroups) {
            // 根据文章组ID查询文章信息
            List<WebArticleBaseNameVO> articletitle = baseArticleBaseMapper.selectByGroupId(articleGroup.getArticleGroupId(), null);
            if (articletitle != null && articletitle.size() > 0) {
                for (WebArticleBaseNameVO webArticleBaseName2 : articletitle) {
                    if (StringUtils.isNotEmpty(webArticleBaseName2.getArticleUrl())) {
                        webArticleBaseName2.setArticleGroupId(3);
                    }
                }
            }
            articleGroup.setArticletitle2(articletitle);
        }
        restultMap.put("articleGroups", articleGroups);

        return ResponseResult.success(restultMap);
    }

    /**
     * 查询PC端首页配置数据
     * 该方法主要用于获取PC端首页的配置数据，包括各种模块的展示数据和广告信息等。通过从Redis中获取缓存数据来提高响应速度，
     * 如果缓存中不存在则从数据库中查询并缓存起来。对于登录用户，还会根据用户信息进行一些个性化数据的处理。
     *
     * @param map 请求参数的映射表，其中可能包含用户的token信息用于获取用户特定的配置数据。
     * @return 返回首页配置数据的结果对象，包含查询成功与否的状态和数据。
     */
    @Override
    public ResponseResult newQueryPcIndex(Map<String, Object> map) {
        // 用于存储首页配置数据的列表
        //用于存放首页数据 排序后 返回前端
        List<GdbPcIndexVO> pcIndexData = new ArrayList<>();
        try {
            // 构建Redis中存储的键名，可能包含用户的ID信息
            String pcIndexConfig2 = RedisPrefixUtils.pcIndexConfig;
            // 用于存储会员折扣信息
            //会员折扣 - plus折扣
            String memberDiscount = null;
            // 用于存储PLUS会员状态
            Integer plusMember = null;
            // 如果请求中包含token信息，则尝试获取用户信息以进行个性化处理
            Object tokenObj = map.get("token");
            if (tokenObj != null && !tokenObj.toString().equals("")) {
                // 从Redis中根据token获取用户信息
                String userId = null;
                String userInfoStr = (String) redisCache.getCacheObject(RedisConstants.USER_TOKEN_USER + tokenObj.toString());
                if (StrUtil.isNotEmpty(userInfoStr)) {
                    UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(tokenObj.toString());
                    if (userInfo != null) {
                        // 处理用户ID和会员信息
                        userId = userInfo.getUserId();
                        memberDiscount = String.valueOf(userInfo.getPlusDiscount());
                        plusMember = userInfo.getPlusMember();
                    }
                }
                // 更新Redis键名以包含用户ID
                pcIndexConfig2 = pcIndexConfig2 + "_" + userId;
            }
            // 尝试从Redis中获取首页配置数据
            String pcConfig = (String) redisCache.getCacheObject(pcIndexConfig2);
            if (StrUtil.isNotBlank(pcConfig)) {
                // 如果缓存中存在数据，则直接返回
                pcIndexData = JSON.parseObject(pcConfig, new TypeReference<List<GdbPcIndexVO>>() {
                });
                return ResponseResult.success(pcIndexData);
            }
            // 从数据库中查询首页配置数据
            // 查询 gdb_pc_index_config 数据 pageType 1 天天特价 2 特色专题  3 为你推荐 4家居馆 7 智能推荐
            Map<Integer, List<PcConfigResultVO>> pcConfigResults = basePcIndexConfigService.querygdbPcIndexConfig();
            // 处理数据库查询结果，封装成首页配置数据的格式
            if (pcConfigResults != null) {
                for (Map.Entry<Integer, List<PcConfigResultVO>> entry : pcConfigResults.entrySet()) {
                    // 封装每个模块的数据
                    GdbPcIndexVO gdbPcIndexDTO = new GdbPcIndexVO();
                    List<PcConfigResultVO> pcConfigResultList = entry.getValue();
                    // 设置会员折扣信息
                    if (memberDiscount != null && !memberDiscount.equals("null")) {
                        gdbPcIndexDTO.setMemberDiscount(memberDiscount);
                    }
                    // 设置PLUS会员状态
                    gdbPcIndexDTO.setPlusMember(plusMember);
                    // 设置模块数据
                    gdbPcIndexDTO.setData(pcConfigResultList);
                    // 设置模块类型
                    gdbPcIndexDTO.setPageType(pcConfigResultList.stream().map(PcConfigResultVO::getPageType).collect(Collectors.toList()).get(0));
                    // 设置模块排序
                    gdbPcIndexDTO.setPageSort(pcConfigResultList.stream().map(PcConfigResultVO::getPageSort).collect(Collectors.toList()).get(0));
                    // 添加到结果列表
                    pcIndexData.add(gdbPcIndexDTO);
                }
            }
            // 查询楼层广告信息
            Map<Integer, List<MerchantFloorAdpageVO>> integerListMap = queryGdbMerchantFloorAdpage();
            // 处理楼层广告信息，同样封装成首页配置数据的格式
            for (Map.Entry<Integer, List<MerchantFloorAdpageVO>> entry : integerListMap.entrySet()) {
                List<MerchantFloorAdpageVO> gdbMerchantFloorAdpageList = entry.getValue();
                // 单楼层广告处理
                if (5 == entry.getKey()) {
                    for (MerchantFloorAdpageVO gdbMerchantFloorAdpage : gdbMerchantFloorAdpageList) {
                        GdbPcIndexVO gdbPcIndexDTO = new GdbPcIndexVO();
                        // 设置会员折扣信息
                        if (memberDiscount != null && !memberDiscount.equals("null")) {
                            gdbPcIndexDTO.setMemberDiscount(memberDiscount);
                        }
                        // 设置PLUS会员状态
                        gdbPcIndexDTO.setPlusMember(plusMember);
                        // 设置模块数据
                        gdbPcIndexDTO.setData(Lists.newArrayList(gdbMerchantFloorAdpage));
                        // 设置模块类型
                        gdbPcIndexDTO.setPageType(gdbMerchantFloorAdpage.getPageType());
                        // 设置模块排序
                        gdbPcIndexDTO.setPageSort(gdbMerchantFloorAdpage.getFloorAdpageOrder());
                        // 设置楼层ID
                        gdbPcIndexDTO.setFloorAdpageId(gdbMerchantFloorAdpage.getFloorAdpageId());
                        pcIndexData.add(gdbPcIndexDTO);
                    }
                } else {
                    // 多楼层广告处理
                    GdbPcIndexVO gdbPcIndexDTO = new GdbPcIndexVO();
                    // 设置会员折扣信息
                    gdbPcIndexDTO.setMemberDiscount(memberDiscount);
                    // 设置PLUS会员状态
                    gdbPcIndexDTO.setPlusMember(plusMember);
                    // 设置模块数据
                    gdbPcIndexDTO.setData(gdbMerchantFloorAdpageList);
                    // 设置模块类型
                    gdbPcIndexDTO.setPageType(gdbMerchantFloorAdpageList.stream().map(MerchantFloorAdpageVO::getPageType).collect(Collectors.toList()).get(0));
                    // 设置模块排序
                    gdbPcIndexDTO.setPageSort(gdbMerchantFloorAdpageList.stream().map(MerchantFloorAdpageVO::getFloorAdpageOrder).collect(Collectors.toList()).get(0));
                    pcIndexData.add(gdbPcIndexDTO);
                }
            }
            // 对首页配置数据进行排序
            List<GdbPcIndexVO> collect = pcIndexData.stream().sorted(Comparator.comparing(GdbPcIndexVO::getPageSort)).collect(Collectors.toList());
            // 将处理后的首页配置数据缓存到Redis中
            redisCache.setCacheObject(pcIndexConfig2, JSONSerializer.serialize(collect));
            redisCache.expire(pcIndexConfig2, 60 * 60 * 24, TimeUnit.SECONDS);
            // 返回处理后的首页配置数据
            return ResponseResult.success(collect);
        } catch (Exception e) {
            // 处理异常情况
            e.printStackTrace();
        }
        // 如果出现异常，则返回错误信息
        return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(), BizCodeEnume.SYSTEM_ERROR.getMsg());
    }


    /**
     * 更新PC端首页楼层状态
     *
     * @param map 包含楼层ID和楼层状态的Map
     * @return 返回操作结果，成功更新返回true
     */
    @Override
    public ResponseResult pcIndexFloorUpdate(Map<String, Object> map) {
        //  楼层 id
        Object floorAdpageIdObj = map.get("floorAdpageId");
        if (floorAdpageIdObj == null) {
            return ResponseResult.error("floorAdpageId参数不能为空");
        }
        Integer floorAdpageId = Integer.valueOf(floorAdpageIdObj.toString());
        //  1 开启 2 关闭
        Object floorStatusObj = map.get("floorStatus");
        if (floorStatusObj == null) {
            return ResponseResult.error("floorStatus参数不能为空");
        }
        Integer floorStatus = Integer.valueOf(floorStatusObj.toString());
        baseFloorAdpageMapper.pcIndexFloorUpdate(floorAdpageId, floorStatus);
        //删除redis缓存
        redisCache.deleteObject(RedisPrefixUtils.pcIndexConfig);
        redisCache.deleteObject(RedisPrefixUtils.pcIndexConfig2);
        return ResponseResult.success();
    }

    /**
     * 更新PC端首页配置状态
     *
     * @param map 包含页面类型和页面状态的Map
     * @return 返回操作结果，成功更新返回true
     */
    @Override
    public ResponseResult pcIndexConfigUpdate(Map<String, Object> map) {
        // 1 天天特价 2 特色专题  3 为你推荐 4 楼层
        Object pageTypeObj = map.get("pageType");
        if (pageTypeObj == null) {
            return ResponseResult.error("pageType参数不能为空");
        }
        Integer pageType = Integer.valueOf(pageTypeObj.toString());
        //  1 开启 2 关闭
        Object pageStartObj = map.get("pageStart");
        if (pageStartObj == null) {
            return ResponseResult.error("pageStart参数不能为空");
        }
        Integer pageStart = Integer.valueOf(pageStartObj.toString());

        baseFloorAdpageMapper.pcIndexConfigUpdate(pageType, pageStart);
        //删除redis缓存
        redisCache.deleteObject(RedisPrefixUtils.pcIndexConfig);
        redisCache.deleteObject(RedisPrefixUtils.pcIndexConfig2);
        return ResponseResult.success();
    }


    /**
     * 从GDB查询商家楼层广告页信息
     *
     * @return 商家楼层广告页信息的映射，键为页面类型，值为该类型下的广告页列表
     */
    public Map<Integer, List<MerchantFloorAdpageVO>> queryGdbMerchantFloorAdpage() {
        // 根据商家类型代码查询基础广告页信息
        List<BaseFloorAdpageEntity> entityList = baseFloorAdpageMapper.selectList(new LambdaQueryWrapper<BaseFloorAdpageEntity>()
                .eq(BaseFloorAdpageEntity::getFloorType, CommonType.storeType.SELLER_STORE.getCode()));

        /**
         * 封装数据
         */
        List<MerchantFloorAdpageVO> allFloorAdpages = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityList)) {
            allFloorAdpages = entityList.stream().map(entity -> {
                MerchantFloorAdpageVO catResult = new MerchantFloorAdpageVO();
                BeanUtils.copyProperties(entity, catResult);
                return catResult;
            }).collect(Collectors.toList());
        }

        // 遍历广告页列表，填充额外信息
        for (MerchantFloorAdpageVO allFloorAdpage : allFloorAdpages) {
            // 获取楼层名称
            WebFloorAdpageNameDTO floorAdpageNameNew = JsonUtils.string2Obj(allFloorAdpage.getFloorAdpageName(),
                    WebFloorAdpageNameDTO.class);
            if (floorAdpageNameNew == null || !StringUtils.isNotBlank(floorAdpageNameNew.getCatName())) {
                Integer parsedCatId = floorAdpageNameNew == null ? 0 : floorAdpageNameNew.getCatId();
                floorAdpageNameNew = new WebFloorAdpageNameDTO();
                floorAdpageNameNew.setCatId(parsedCatId == null ? 0 : parsedCatId);
                floorAdpageNameNew.setCatName(StringUtils.defaultIfBlank(allFloorAdpage.getFloorAdpageName(), "商品分类"));
            }
            allFloorAdpage.setFloorAdpageNameNew(floorAdpageNameNew);
            // 获取轮播图信息
            List<Integer> goodsIdList2 = JsonUtils.StringToList2(allFloorAdpage.getLeftAdpageBanner());
            List<GoodsIdsQueryListVO> adpageBannerList = webIndexMapper.selectByGoodIds(goodsIdList2);
            allFloorAdpage.setAdpageBannerList(adpageBannerList);
            // 获取左侧导航信息
            List<PlatformLeftAdpageNavDTO> leftAdpageNavList = JsonUtils.string2Obj(allFloorAdpage.getLeftAdpageNav(),
                    List.class, PlatformLeftAdpageNavDTO.class);
            for (PlatformLeftAdpageNavDTO platformLeftAdpageNav : leftAdpageNavList) {
                String leftAdpageNavUrl = String.valueOf(platformLeftAdpageNav.getLeftAdpageNavUrl());
                if (StringUtils.isNotEmpty(leftAdpageNavUrl)) {
                    Map urlMap = JSON.parseObject(leftAdpageNavUrl, Map.class);
                    if (platformLeftAdpageNav.getOpType() != null) {
                        // 根据操作类型填充具体信息
                        // 封装商品信息
                        if (platformLeftAdpageNav.getOpType().equals(2) && null != urlMap.get("commonId")) {
                            String commonId = String.valueOf(urlMap.get("commonId"));
                            if (StringUtils.isNotEmpty(commonId)) {
                                String commonName = goodsClient.selectCommonName(Integer.valueOf(commonId));
                                platformLeftAdpageNav.setCommonName(commonName);
                            }
                            // 封装店铺信息
                        } else if (platformLeftAdpageNav.getOpType().equals(4) && null != urlMap.get("shopId")) {
                            String shopId = String.valueOf(urlMap.get("shopId"));
                            if (StringUtils.isNotEmpty(shopId)) {
                                String shopName = businessClient.selectByShopName(Integer.valueOf(shopId));
                                platformLeftAdpageNav.setShopName(shopName);
                            }
                            // 封装分类信息
                        } else if (platformLeftAdpageNav.getOpType().equals(6) && null != urlMap.get("catId")) {
                            String catId = String.valueOf(urlMap.get("catId"));
                            if (StringUtils.isNotEmpty(catId)) {
                                String catName = goodsClient.selectByCatNameString(Integer.valueOf(catId));
                                platformLeftAdpageNav.setCatName(catName);
                            }
                        }
                    }
                }
            }
            allFloorAdpage.setLeftAdpageNavList(leftAdpageNavList);
            // 获取右侧热门推荐信息
            PlatformRightAdpageNavDTO rightRec = JsonUtils.string2Obj(allFloorAdpage.getRightHotRecommend(),
                    PlatformRightAdpageNavDTO.class);
            allFloorAdpage.setRightRec(rightRec);
            // 设置热门推荐商品列表
            if (StringUtils.isNotEmpty(rightRec.getRightAdpageNavGoodsId())) {
                List<Integer> rightAdpageNavGoodsIds = JsonUtils.StringToList2(rightRec.getRightAdpageNavGoodsId());
                List<GoodsIdsVO> rightRecGoodsList = new ArrayList<>();
                MobConfigGoodsVO query = new MobConfigGoodsVO();
                List<WebCatMenuVO> allMenu = null;
                // 获取所有的子分类
                if (null == allMenu) {
                    allMenu = goodsClient.selectAllCat2();
                }
                List<Integer> catIds = getMenuList(allMenu, floorAdpageNameNew.getCatId());
                query.setCatIds(catIds);
                query.setGoodsIdList(rightAdpageNavGoodsIds);
                query.setGoodsNum(8 - rightRecGoodsList.size());
                List<Integer> goodsVarList = query.getGoodsIdList();
                for (Integer goodsId : goodsVarList) {
                    GoodsIdsVO param = goodsClient.newSelectWebFloorMobConfig(goodsId);
                    if (param != null) {
                        rightRecGoodsList.add(param);
                    }
                }
                allFloorAdpage.setRightRecGoodsList(rightRecGoodsList);
            }
            // 获取右侧分类广告信息
            allFloorAdpage.setRightAdpageNavList(JsonUtils.string2Obj(allFloorAdpage.getRightAdpageNav(), List.class,
                    WebFloorAdpageNameVO.class));
        }
        // 根据页面类型对广告页列表进行分组
        Map<Integer, List<MerchantFloorAdpageVO>> collect = allFloorAdpages.stream().collect(Collectors.groupingBy(MerchantFloorAdpageVO::getPageType));
        return collect;
    }

    /**
     * 检测应用状态
     *
     * @return 包含各个应用状态的列表
     */
    @Override
    public ResponseResult detectingApplicationStatus() {
        String token = getAccessToken();
        List<Map<String, Object>> list = new ArrayList<>();
        detectingPort(token, list, "mall-dcenter", "数据中台");
        detectingPort(token, list, "webpos", "门店收银");
        detectingPort(token, list, "mall-rider", "骑手配送");
        detectingPort(token, list, "im", "IM客服");
        detectingPort(token, list, "mall-ai", "Ai大模型");
        detectingPort(token, list, "mall-cms", "企业官网");
        return ResponseResult.success(list);
    }


    /**
     * nacos获取service
     *
     * @return
     */
    private String getNacosService(String accessToken, String serviceName, String groupName, long current, long size) {
        Map<String, String> mapService = new HashMap<>();
        mapService.put("accessToken", accessToken);
        mapService.put("hasIpCount", "true");
        mapService.put("withInstances", "false");
        mapService.put("serviceNameParam", serviceName);
        mapService.put("clusterName", "DEFAULT");
        mapService.put("groupNameParam", groupName);
        mapService.put("pageSize", String.valueOf(size));
        mapService.put("pageNo", String.valueOf(current));
        mapService.put("namespaceId", namespace);
        return HttpUtils.get("http://" + address + "/nacos/v1/ns/catalog/services", mapService);
    }

    /**
     * nacos获取accessToken
     *
     * @return
     */
    private String getAccessToken() {
        if (redisCache.hasKey("nacos_accessToken")) {
            return (String) redisCache.getCacheObject("nacos_accessToken");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("username", username);
        map.put("password", password);
        String result = HttpUtil.post("http://" + address + "/nacos/v1/auth/users/login", map);
        JSONObject jsonObject = JSONObject.parseObject(result);
        String accessToken = jsonObject.getString("accessToken");
        redisCache.setCacheObject("nacos_accessToken", accessToken);
        redisCache.expire("nacos_accessToken", RedisUtil.EXRP_HOUR, java.util.concurrent.TimeUnit.SECONDS);
        return accessToken;
    }

    /**
     * 检测端口
     *
     * @param list        结果
     * @param serviceName 要检测的服务
     * @param key         key
     */
    private void detectingPort(String token, List<Map<String, Object>> list, String serviceName, String key) {
        String sercice = getNacosService(token, serviceName, "DEFAULT_GROUP", 1, 30);
        Map result = JSON.parseObject(sercice, Map.class);
        Map<String, Object> map = new HashMap<>();
        map.put("configKey", key);
        System.out.println(result);
        if ((Integer) result.get("count") > 0) {
            List<Map<String, Object>> serviceList = (List) result.get("serviceList");
            for (Map<String, Object> object : serviceList) {
                if ((Integer) object.get("healthyInstanceCount") > 0) {
                    map.put("configStatus", true);
                    break;
                } else {
                    map.put("configStatus", false);
                }
            }
        } else {
            map.put("configStatus", false);
        }
        list.add(map);
    }

    /**
     * 检测IM客服模块的状态
     * 此方法用于检查IM客服模块是否正在运行，通过解析应用状态配置来判断
     *
     * @param config IM客服的配置信息，用于查询IM客服的具体配置状态
     * @return 返回IM客服配置的结果，如果IM客服模块未运行，则返回成功状态
     */
    @Override
    public ResponseResult detectingImStatus(String config) {
        // 首页只依赖 IM 的公开客服配置；IM/DB 抖动时快速软降级，避免前端 30s 超时红报错。
        try {
            ResponseResult result = imClient.queryImConfig(config);
            if (result != null && result.getCode() == 1) {
                return result;
            }
            return ResponseResult.success();
        } catch (Exception ex) {
            return ResponseResult.success();
        }
    }

    /**
     * 检测IM客服模块的状态
     * 此方法用于检查IM客服模块是否正在运行，通过解析应用状态配置来判断
     *
     * @return 返回IM客服配置的结果，如果IM客服模块未运行，则返回成功状态
     */
    @Override
    public ResponseResult detectingImAppKey() {
        // 标志变量，用于记录IM客服模块是否正在运行
        boolean flag = checkAppServerStart("IM客服");
        if (!flag) {
            return ResponseResult.success();
        }

        // 如果IM客服模块正在运行，查询并返回IM客服的配置信息
        ResponseResult result = imClient.queryImKeySecret();
        return result;
    }

    /**
     * 检测Im应用中心状态 及查询用户是否有未读消息
     *
     * @param token
     * @return
     */
    @Override
    public ResponseResult detectingImChatHistoryRead(String token) {
        // 标志变量，用于记录IM客服模块是否正在运行
        boolean flag = checkAppServerStart("IM客服");
        if (!flag) {
            return ResponseResult.success();
        }
        // 根据token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(token);
        // 若用户信息为空，则返回token无效的结果
        if (null == userInfo) {
            return ResponseResult.success();
        }
        // 如果IM客服模块正在运行，根据商城用户id 查询是否有消息未读
        ResponseResult result = imClient.queryImChatHistoryRead(userInfo.getUserId());
        return result;
    }

    /**
     * 检查应用服务器是否已启动
     * 该方法通过检测应用的整体状态，来判断特定的应用服务器是否已经启动
     *
     * @param serverName 服务器名称，用于指定要检查的服务器
     * @return 返回一个Boolean值，表示服务器是否已启动如果服务器已启动，返回true；否则返回false
     */
    public Boolean checkAppServerStart(String serverName) {
        // 检测应用的整体状态，获取包含各种配置状态的响应结果
        ResponseResult res = detectingApplicationStatus();

        // 将响应结果中的数据转换为Map列表，以便进一步处理
        List<Map> list = JSON.parseArray(JSON.toJSONString(res.getData()), Map.class);

        // 标志变量，用于记录IM客服模块是否正在运行
        boolean flag = false;

        // 遍历Map列表，寻找IM客服的配置状态
        for (Map map : list) {
            // 当找到IM客服的配置项时，更新标志变量，并停止遍历
            if (serverName.equals(map.get("configKey"))) {
                flag = (Boolean) map.get("configStatus");
                break;
            }
        }
        // 返回IM客服模块的运行状态
        return flag;
    }
}
