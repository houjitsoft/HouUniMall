package com.houjiang.goods.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.houjiang.commons.dto.goods.*;
import com.houjiang.commons.dto.sensitive.BaseSensitiveDTO;
import com.houjiang.commons.dto.shop.*;
import com.houjiang.commons.util.json.JsonUtils;
import com.houjiang.commons.util.*;
import com.houjiang.commons.vo.base.PointsCashRatioVo;
import com.houjiang.commons.vo.goods.GoodsBrandVO;
import com.houjiang.commons.vo.sensitive.BaseSensitiveVO;
import com.houjiang.commons.vo.shop.ArticleVO;
import com.houjiang.commons.vo.shop.ShopGoodsCatVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.goods.entity.GoodsCommonEntity;
import com.houjiang.goods.feign.BaseServiceClient;
import com.houjiang.goods.feign.MerchantServiceClient;
import com.houjiang.goods.mapper.GoodsBaseMapper;
import com.houjiang.goods.mapper.GoodsCommonMapper;
import com.houjiang.goods.service.*;
import com.houjiang.goods.util.es.ESUtils;
import com.houjiang.commons.vo.goods.SearchVO;
import org.springframework.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Amos
 */
@Service
public class ElasticSearchServiceImpl implements ElasticSearchService {
    @Autowired
    private GoodsBaseMapper goodsBaseMapper;

    @Autowired
    private GoodsCommonMapper goodsCommonMapper;

    @Autowired
    private MerchantServiceClient merchantServiceClient;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private BaseServiceClient baseServiceClient;

    @Autowired
    private GoodsCommonService goodsCommonService;

    @Autowired
    private GoodsCatService goodsCatService;

    @Autowired
    private GoodsAttributeGroupService goodsAttributeGroupService;

    @Autowired
    private GoodsBrandService goodsBrandService;

    private static Pattern pattern = Pattern.compile("([^:;]+):([^;]+);");

    /**
     * 通过CommonId查询商品列表信息
     *
     * @param commonId 商品的通用ID，用于在数据库中定位特定商品。
     * @return 返回GoodsListQueryEsDTO对象，包含查询到的商品列表信息。
     */
    @Override
    public GoodsListQueryEsDTO selectByCommonId(Integer commonId) {
        return goodsBaseMapper.selectByCommonIdEs(commonId);
    }

    /**
     * 通过GoodsId查询商品列表信息
     *
     * @param goodsId 商品的唯一ID，用于在数据库中定位特定商品。
     * @return 返回GoodsListQueryEsDTO对象，包含查询到的商品列表信息。
     */
    @Override
    public GoodsListQueryEsDTO selectByGoodsId(Integer goodsId) {
        return goodsBaseMapper.selectByGoodsIdEs(goodsId);
    }

    /**
     * 通过CommonId查询店铺ID
     *
     * @param commonId 商品的通用ID，用于在数据库中定位特定商品并获取其所属店铺ID。
     * @return 返回GoodsListQueryEsDTO对象，包含查询到的店铺ID信息。
     */
    @Override
    public GoodsListQueryEsDTO selectShopIdByCommonId(Integer commonId) {
        return goodsCommonMapper.selectShopIdByCommonId(commonId);
    }

    /**
     * 查询所有店铺信息
     *
     * @return 返回一个包含所有店铺信息的List<ArticleShopDTO>对象列表。
     */
    @Override
    public List<ArticleShopDTO> selectAllShop() {
        return merchantServiceClient.selectAllShop();
    }

    /**
     * 根据店铺ID查询店铺信息。
     *
     * @param shopId 店铺ID。
     * @return 店铺信息列表，包含导航信息、商品列表和商品总数。
     */
    @Override
    public List<WebShopListQueryEsDTO> selectByShopId(Integer shopId) {
        WebShopListQueryEsDTO webShopListQueryResult = null;
        List<WebShopListQueryEsDTO> shopList = new ArrayList<>();
        // 通过店铺ID查询店铺基本信息
        webShopListQueryResult = merchantServiceClient.selectByShopId2(shopId);
        if (null != webShopListQueryResult) {
            // 设置店铺默认值，如轮播图、Logo等
            // 获取店铺默认值
            getShopDefaultValue2(webShopListQueryResult);
            // 查询店铺商品列表
            // 查询店铺商品
            List<GoodsListQueryEsDTO> commonList = goodsCommonMapper.selectByShopId3(shopId);
            webShopListQueryResult.setCommonList(commonList);
            // 查询店铺商品总数
            // 查询店铺商品数量
            Integer shopGoodsSum = goodsCommonMapper.selectShopSum(shopId);
            // 设置店铺商品总数
            webShopListQueryResult.setShopGoodsSum(shopGoodsSum);
            // 将处理后的店铺信息添加到列表中
            shopList.add(webShopListQueryResult);
        }
        return shopList;
    }

    /**
     * 设置店铺的默认值，如轮播图、Logo等。
     *
     * @param shopListQueryResult 店铺信息DTO，包含需要设置默认值的字段。
     */
    public void getShopDefaultValue2(WebShopListQueryEsDTO shopListQueryResult) {
        String configValue = null;
        ShopConfigDefaultValueDTO shopConfig = null;
        // 如果店铺轮播图不为空且不为空字符串，则取第一个轮播图；否则，从默认配置中获取并设置
        if (null != shopListQueryResult.getShopSlide() && !shopListQueryResult.getShopSlide().equals("")) {
            shopListQueryResult.setShopSlide(shopListQueryResult.getShopSlide().split(",", -1)[0]);
        } else {
            configValue = baseServiceClient.selectConfigValueFeign("shop_default_config");
            shopConfig = JsonUtils.string2Obj(configValue, ShopConfigDefaultValueDTO.class);
            shopListQueryResult.setShopSlide(shopConfig.getShopSlide().split(",", -1)[0]);
        }

        // 如果店铺Logo为空或为空字符串，则从默认配置中获取并设置
        if (shopListQueryResult.getShopLogo() == null || shopListQueryResult.getShopLogo().equals("")) {
            if (shopConfig == null) {
                configValue = baseServiceClient.selectConfigValueFeign("shop_default_config");
                shopConfig = JsonUtils.string2Obj(configValue, ShopConfigDefaultValueDTO.class);
            }
            shopListQueryResult.setShopLogo(shopConfig.getShopLogo());
        }
    }

    /**
     * 根据H5店铺ID查询店铺信息。
     *
     * @param shopId H5店铺ID。
     * @return 查询到的店铺信息。
     */
    @Override
    public ShopListQueryDTO selectByH5ShopId(Integer shopId) {
        // 通过H5店铺ID查询店铺信息
        ShopListQueryDTO shopListQueryDto = merchantServiceClient.selectByH5ShopId(shopId);
        if (shopListQueryDto != null) {
            // 设置店铺的默认轮播图，如果店铺未设置，则使用默认配置中的轮播图
            // 获取店铺默认值
            String configValue = null;
            ShopConfigDefaultValueDTO shopConfig = null;
            if (shopListQueryDto.getShopSlide() != null && !shopListQueryDto.getShopSlide().isEmpty()) {
                shopListQueryDto.setShopSlide(shopListQueryDto.getShopSlide().split(",", -1)[0]);
            } else {
                configValue = baseServiceClient.selectConfigValueFeign("shop_default_config");
                shopConfig = JsonUtils.string2Obj(configValue, ShopConfigDefaultValueDTO.class);
                shopListQueryDto.setShopSlide(shopConfig.getShopSlide().split(",", -1)[0]);
            }

            // 设置店铺的默认Logo，如果店铺未设置，则使用默认配置中的Logo
            if (shopListQueryDto.getShopLogo() == null || shopListQueryDto.getShopLogo().isEmpty()) {
                if (shopConfig == null) {
                    configValue = baseServiceClient.selectConfigValueFeign("shop_default_config");
                    shopConfig = JsonUtils.string2Obj(configValue, ShopConfigDefaultValueDTO.class);
                }
                shopListQueryDto.setShopLogo(shopConfig.getShopLogo());
            }

            // 查询店铺的商品信息并设置到店铺信息中
            List<Map<String, Object>> maps = goodsCommonMapper.selectuniappQueryShopGoods(shopListQueryDto);
            shopListQueryDto.setGoods(maps);
        }
        return shopListQueryDto;
    }

    /**
     * 商品搜索
     *
     * @return 商品列表
     * @throws IOException 异常
     */
    @Override
    public ResponseResult<SearchVO> searchGoodsData(ListPageQueryParamDTO param) throws IOException {
        SearchVO searchVO = new SearchVO();
        //查询plus是否开启
        Integer selectplus = (Integer) baseServiceClient.queryMerchantPlusRule().getData();
        // 从token中解析用户信息，用于后续的PLUS会员优惠逻辑
        String token = param.getToken();
        Integer plusMember = null;
        BigDecimal plusDiscount = null;
        if (token != null) {
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByTokenNoIsExist(token);
            if (userInfo != null) {
                plusMember = userInfo.getPlusMember();
                plusDiscount = userInfo.getPlusDiscount();
            }
        }
        ShopGoodsCatVO goodsCat = new ShopGoodsCatVO();
        if (param.getCatId() != null) {
            // 根据分类id查询该分类下所有下级分类id
            List<Integer> catIds = goodsCatService.queryAllSubordinateCatIdByCatId(param.getCatId());
            param.setCatIds(catIds);
            // 根据商品类别ID查询商品类别信息
            ResponseResult catResult = goodsCatService.queryCatByCatId(param.getCatId());
            goodsCat = JSON.parseObject(JSON.toJSONString(catResult.getData()), ShopGoodsCatVO.class);
        }
        // 添加热搜词
        setHotSearchWords(param.getCommonName());
        // 根据商品属性获取的commonId集合
        if (param.getCatId() == null || (goodsCat.getCatLevel() != null && goodsCat.getCatLevel() == 3)) {
            setCommonIdByAttrName(param);
        }
        // es查询商品数据
        List<ArticleVO> list = ESUtils.searchGoodsData(param, goodsCat);
        if (null == list) {
            return ResponseResult.success(searchVO);
        }
        // 查询后再分页，排序
        List<ArticleVO> result = getGoodsList(list, param);
        // 使用stream流提取出commonId、catId、brandId集合
        List<Integer> commonIdList = extractIds(list, ArticleVO::getCommonId);
        List<Integer> catIdList = extractIds(list, ArticleVO::getCatId);
        List<Integer> brandIdList = extractIds(list, ArticleVO::getBrandId);
        // 设置商品会员价
        for (ArticleVO article : result) {
            article.setPlusMember(plusMember);
            article.setPlusDiscount(plusDiscount);
        }
        searchVO.setKeyword(param.getCommonName());
        // 设置查询结果 nav品牌 nav分类 line品牌 line分类 attrs商品属性
        setSearchVO(searchVO, param, brandIdList, catIdList, goodsCat, commonIdList);
        searchVO.setType(1);
        searchVO.setSelectplus(selectplus);
        searchVO.setGoodsList(result);
        searchVO.setGoodsCount(list.size());
        return ResponseResult.success(searchVO);
    }

    /**
     * 设置搜索视图对象（SearchVO）的属性
     * 此方法根据提供的参数和列表初始化SearchVO对象的各个属性，包括品牌、分类和属性等信息
     *
     * @param searchVO     搜索视图对象，用于存储搜索相关的数据
     * @param param        列表页面查询参数DTO，包含查询相关的参数
     * @param brandIdList  品牌ID列表，用于获取品牌信息
     * @param catIdList    分类ID列表，用于获取分类信息
     * @param goodsCat     商铺商品分类视图对象，包含分类的详细信息
     * @param commonIdList 通用属性ID列表，用于获取商品属性信息
     */
    private void setSearchVO(SearchVO searchVO, ListPageQueryParamDTO param, List<Integer> brandIdList,
                             List<Integer> catIdList, ShopGoodsCatVO goodsCat, List<Integer> commonIdList) {
        // 根据参数中的品牌ID列表和传入的品牌ID列表，设置品牌信息
        if (param.getBrandIdList() == null || param.getBrandIdList().isEmpty()) {
            if (!brandIdList.isEmpty()) {
                List<GoodsBrandVO> goodsBrandList = goodsBrandService.selectGoodsBrandList(brandIdList);
                searchVO.setLineBrands(goodsBrandList);
            }
        } else {
            List<GoodsBrandVO> goodsBrandList = goodsBrandService.selectGoodsBrandList(param.getBrandIdList());
            searchVO.setNavBrands(goodsBrandList);
        }

        // 根据分类ID列表和参数中的分类ID，设置销售分类信息
        if (!catIdList.isEmpty() && param.getCatId() == null) {
            searchVO.setLineSaleCategories(goodsCatService.selectLineSaleCategoriesByThreeCat(catIdList));
        } else if (goodsCat.getCatLevel() != null && goodsCat.getCatLevel() != 3) {
            searchVO.setLineSaleCategories(goodsCatService.selectLineSaleCategoriesByCatId(goodsCat.getCatId()));
        }

        // 根据参数中的分类ID和积分商品标志，设置导航销售分类信息
        if (param.getCatId() != null) {
            Integer isIntegralCat = param.getIntegralGoods() == null || param.getIntegralGoods() == 1 ? 0 : 1;
            searchVO.setNavSaleCategories(goodsCatService.selectNavSaleCategories(param.getCatId(), isIntegralCat));
        }

        // 根据通用属性ID列表，设置商品属性信息
        if (!commonIdList.isEmpty() && (param.getCatId() == null || (goodsCat.getCatLevel() != null && goodsCat.getCatLevel() == 3))) {
            GoodsAttrDTO dto = new GoodsAttrDTO(commonIdList, param.getAttrName(), new ArrayList<>());
            searchVO.setAttrs(goodsAttributeGroupService.selectGoodsAttributeGroupList(dto));
        } else {
            // 如果没有通用属性，则设置一个空的通用属性列表
            searchVO.setAttrs(new ArrayList<>());
        }
    }

    /**
     * 根据属性名称设置通用ID
     * 该方法用于解析参数中的属性名称字符串，提取其中的键值对，
     * 并根据这些键值对查询对应的通用ID列表，然后将该列表设置到参数对象中
     *
     * @param param 包含属性名称的查询参数对象，属性名称应包含键值对信息
     */
    private void setCommonIdByAttrName(ListPageQueryParamDTO param) {
        // 检查属性名称是否为空
        if (!StringUtils.isEmpty(param.getAttrName()) && !param.getAttrName().equals(";")) {
            // 用于存储键值对的 Map
            List<Map<String, Object>> attrList = new ArrayList<>();
            // 创建匹配器
            Matcher matcher = pattern.matcher(param.getAttrName());
            // 查找并提取所有的键值对
            while (matcher.find()) {
                // 用于存储键值对的 Map
                Map<String, Object> keyValuePairs = new HashMap<>();
                String key = matcher.group(1).trim();
                String value = matcher.group(2).trim();
                keyValuePairs.put("key", key);
                List<String> valueList = Arrays.asList(value.split(","));
                keyValuePairs.put("valueList", valueList);
                attrList.add(keyValuePairs);
            }
            // 根据提取的键值对查询通用ID列表
            List<Integer> commonIds = goodsAttributeGroupService.selectCommonIdListByAttrs(attrList);
            // 将查询到的通用ID列表设置到参数对象中
            param.setCommonIdList(commonIds);
        }
    }

    /**
     * 从给定的列表中提取每个元素的ID
     * 此方法使用了泛型和函数式接口，以提高代码的灵活性和可重用性
     *
     * @param list      包含任意类型元素的列表
     * @param extractor 函数式接口实例，用于从列表的每个元素中提取ID
     * @param <T>       列表中元素的类型
     * @return 包含所有提取出的ID的列表
     */
    private <T> List<Integer> extractIds(List<T> list, Function<T, Integer> extractor) {
        // 使用Java 8的Stream API对列表进行流式处理
        // map操作应用于列表的每个元素，使用extractor提取ID
        // collect操作将所有提取的ID收集到一个新的列表中
        return list.stream()
                .map(extractor)
                .collect(Collectors.toList());
    }

    /**
     * 根据指定的条件和分页参数获取并处理商品列表。
     * 该方法首先根据特定规则对输入列表进行排序，然后执行分页操作，最后应用额外的排序。
     *
     * @param list                   输入的商品列表，包含多个 ArticleVO 对象。
     * @param webGoodsListQueryParam 分页查询参数，包含页码和每页显示的数量。
     * @return 处理后的商品列表，包含分页和排序后的结果。
     */
    private List<ArticleVO> getGoodsList(List<ArticleVO> list, ListPageQueryParamDTO webGoodsListQueryParam) {
        List<ArticleVO> result = new ArrayList<>();
        if (list.isEmpty()) {
            return result;
        }

        List<GoodsCommonEntity> goodsList = goodsCommonService.list(new LambdaQueryWrapper<GoodsCommonEntity>()
                .in(GoodsCommonEntity::getGoodsId, list.stream().map(ArticleVO::getCommonId).toList()));
        Map<Integer, GoodsCommonEntity> goodsCommonMap = goodsList.stream().collect(Collectors.toMap(GoodsCommonEntity::getCommonId, Function.identity(), (oldVal, newVal) -> newVal));

        ResponseResult<PointsCashRatioVo> pointsCashRatio = baseServiceClient.getPointsCashRatio();

        for (ArticleVO vo : list) {
            if (pointsCashRatio.getCode() == 1) {

                GoodsCommonEntity goodsCommon = goodsCommonMap.get(vo.getCommonId());
                // ES 索引与库不一致时（商品已删除/重建索引未同步）这里会取到 null，
                // 原写法直接 .getPointsRate() 抛 NPE，被全局兜底成 20010，
                // 表现是「商品列表整页打不开」而不是少一个商品。跳过即可。
                if (goodsCommon == null) {
                    continue;
                }
                Double pointsRate = goodsCommon.getPointsRate();
                if (Objects.nonNull(pointsRate)) {
                    BigDecimal commonPrice = goodsCommon.getCommonPrice();
                    PointsCashRatioVo ratioVo = pointsCashRatio.getData();
                    PointsRatioUtil.PointsCashRatioRes ratio = PointsRatioUtil.getPointsRatio(commonPrice, pointsRate);
                    vo.setCashPrice(ratio.getCashPrice());
                }
            }
        }

        // 第一步：按 getGoodsListFirstShow 和 levelId 排序
        list.sort(Comparator.comparing(ArticleVO::getGoodsListFirstShow)
                .thenComparingInt(ArticleVO::getLevelId).reversed());

        // 第二步：对列表进行分页
        int page = webGoodsListQueryParam.getPage() == null ? 0 : webGoodsListQueryParam.getPage() - 1;
        int startIndex = page * webGoodsListQueryParam.getLimit();
        int endIndex = Math.min(startIndex + webGoodsListQueryParam.getLimit(), list.size());
        result.addAll(list.subList(startIndex, endIndex));

        // 第三步：根据订单类型应用额外排序
        applyAdditionalSorting(result, webGoodsListQueryParam);

        return result;
    }

    /**
     * 根据查询参数中的排序类型对文章列表应用额外的排序逻辑。
     * 该方法旨在根据特定条件对文章列表进行排序，而不影响其原始逻辑。
     *
     * @param result                 文章列表，将对其进行排序。
     * @param webGoodsListQueryParam 包含额外排序条件的查询参数对象。
     */
    private void applyAdditionalSorting(List<ArticleVO> result, ListPageQueryParamDTO webGoodsListQueryParam) {
        if (webGoodsListQueryParam.getOrderType() == null) {
            return;
        }

        switch (webGoodsListQueryParam.getOrderType()) {
            // 假设 5 是评论排序
            case 5:
                if (webGoodsListQueryParam.getCommentOrder() != null && webGoodsListQueryParam.getCommentOrder() != 0) {
                    sortComments(result, webGoodsListQueryParam.getCommentOrder());
                }
                break;
            // 假设 1 是价格排序
            case 1:
                if (webGoodsListQueryParam.getPriceOrder() != null && webGoodsListQueryParam.getPriceOrder() != 0) {
                    sortPrices(result, webGoodsListQueryParam.getPriceOrder());
                }
                break;
            // 假设 2 是销售数量排序
            case 2:
                if (webGoodsListQueryParam.getSaleNum() != null && webGoodsListQueryParam.getSaleNum() != 0) {
                    sortSales(result, webGoodsListQueryParam.getSaleNum());
                }
                break;
            default:
                // 不进行额外排序
                break;
        }
    }

    /**
     * 根据评论的热度对文章列表进行排序
     *
     * @param result       文章列表，包含多个ArticleVO对象
     * @param commentOrder 排序方式，1代表降序，其他值代表升序
     */
    private void sortComments(List<ArticleVO> result, int commentOrder) {
        // 根据commentOrder的值决定排序方式
        if (commentOrder == 1) {
            // 若commentOrder为1，则按照评论热度降序排序
            result.sort(Comparator.comparingInt(ArticleVO::getCommonEvaluate).reversed());
        } else {
            // 否则，按照评论热度升序排序
            result.sort(Comparator.comparingInt(ArticleVO::getCommonEvaluate));
        }
    }

    /**
     * 根据价格对商品列表进行排序
     * <p>
     * 此方法旨在对商品列表按照价格进行升序或降序排序，具体排序方式由参数priceOrder决定
     *
     * @param result     商品列表，包含多个ArticleVO对象
     * @param priceOrder 价格排序标志，1代表降序排序，非1则进行升序排序
     */
    private void sortPrices(List<ArticleVO> result, int priceOrder) {
        // 根据priceOrder的值决定排序方式
        if (priceOrder == 1) {
            // 如果priceOrder为1，按照商品价格降序排序
            result.sort(Comparator.comparing(ArticleVO::getCommonPrice).reversed());
        } else {
            // 如果priceOrder不为1，按照商品价格升序排序
            result.sort(Comparator.comparing(ArticleVO::getCommonPrice));
        }
    }

    /**
     * 根据销量对文章进行排序
     *
     * @param result    文章列表，经过查询后未排序的结果
     * @param saleOrder 销量排序的顺序，1表示降序，非1表示升序
     */
    private void sortSales(List<ArticleVO> result, int saleOrder) {
        // 根据saleOrder的值决定排序方式，1为降序，其他为升序
        if (saleOrder == 1) {
            // 使用销量进行降序排序
            result.sort(Comparator.comparingInt(ArticleVO::getCommonSalenum).reversed());
        } else {
            // 使用销量进行升序排序
            result.sort(Comparator.comparingInt(ArticleVO::getCommonSalenum));
        }
    }

    /**
     * 根据查询参数从ES中搜索商品数据，并结合用户信息（如PLUS会员状态）进行处理和返回。
     *
     * @param webGoodsListQueryParam 搜索商品的查询参数，包含token等信息。
     * @return 返回搜索结果，包括商品列表、PLUS状态等信息。
     */
    @Override
    public ResponseResult pcSeachGoodsData(WebGoodsListQueryDTO webGoodsListQueryParam) {
        // 查询商家PLUS规则，以确定PLUS状态
        //查询plus是否开启
        Integer selectplus = (Integer) baseServiceClient.queryMerchantPlusRule().getData();
        Map<String, Object> resultMap = null;
        List<ArticleDTO> result = new ArrayList<>();
        try {
            resultMap = new HashMap<>();
            // 从token中解析用户信息，用于后续的PLUS会员优惠逻辑
            String token = webGoodsListQueryParam.getToken();
            Integer plusMember = null;
            BigDecimal plusDiscount = null;
            if (token != null) {
                UserInfoVO userInfo = ParseTokenUtils.getUserInfoByTokenNoIsExist(token);
                if (userInfo != null) {
                    plusMember = userInfo.getPlusMember();
                    plusDiscount = userInfo.getPlusDiscount();
                }
            }
            // 从ES中执行商品数据搜索
            //从es中查询数据
            Map<String, Object> map = ESUtils.pcSeachGoodsData(webGoodsListQueryParam);
            // 设置热搜词
            // 添加热搜词
            setHotSearchWords(webGoodsListQueryParam.getCommonName());
            ArrayList<ArticleDTO> list = (ArrayList<ArticleDTO>) map.get("list");
            if (null != list && !list.isEmpty()) {
                for (ArticleDTO articleDto : list) {
                    articleDto.setPlusMember(plusMember);
                    articleDto.setPlusDiscount(plusDiscount);
                    result.add(articleDto);
                }
                // 删除已处理的商品ID，用于后续的无效数据清理
                // 对搜索结果进行排序
                if (!result.isEmpty()) {
                    // 按照 getGoodsListFirstShow 值从大到小排序
                    result.sort((o1, o2) -> {
                        // 首先比较 getGoodsListFirstShow 值，从大到小排列
                        int compareFirstShow = o2.getGoodsListFirstShow().compareTo(o1.getGoodsListFirstShow());
                        if (compareFirstShow != 0) {
                            return compareFirstShow;
                        } else {
                            // 如果 getGoodsListFirstShow 值相同，则比较 levelId 的大小
                            return Integer.compare(o1.getLevelId(), o2.getLevelId());
                        }
                    });
                }
                // 构建最终返回的结果Map
                resultMap.put("list", result);
                resultMap.put("plusStatus", selectplus);
                resultMap.put("totalCount", map.get("count"));
                resultMap.put("goodsBrandCatParam", map.get("goodsBrandCatParam"));
                resultMap.put("gdbGoodsAttParams", map.get("gdbGoodsAttParams"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 如果结果为空，则初始化为空List和相关默认值
        if (CollectionUtils.isEmpty(result)) {
            result = Lists.newArrayList();
            resultMap.put("list", result);
            resultMap.put("plusStatus", selectplus);
            resultMap.put("totalCount", 0);
            resultMap.put("goodsBrandCatParam", null);
            resultMap.put("goodsAttributeResults", null);
        }
        // 返回处理后的搜索结果
        return ResponseResult.success(resultMap);
    }

    /**
     * 根据查询参数搜索店铺数据。
     *
     * @param webGoodsListQueryParam 查询参数，包含店铺搜索的条件。
     * @return 返回搜索结果，包括店铺列表和是否开启PLUS状态。
     */
    @Override
    public ResponseResult pcSeachShopData(WebShopListQueryDTO webGoodsListQueryParam) {
        // 查询是否开启了PLUS规则
        //查询plus是否开启
        Integer selectplus = (Integer) baseServiceClient.queryMerchantPlusRule().getData();
        Map<String, Object> resultMap = null;
        try {
            resultMap = new HashMap<>();
            List<WebShopListQueryEsDTO> result = new ArrayList<>();
            List<WebShopListQueryEsDTO> webShopListQueryResults = null;

            // 从查询参数中获取token，用于识别PLUS会员信息
            String token = webGoodsListQueryParam.getToken();
            Integer plusMember = null;
            BigDecimal plusDiscount = null;
            if (token != null) {
                UserInfoVO userInfo = ParseTokenUtils.getUserInfoByTokenNoIsExist(token);
                if (userInfo != null) {
                    plusMember = userInfo.getPlusMember();
                    plusDiscount = userInfo.getPlusDiscount();
                }
            }

            List<Integer> list2 = new ArrayList<>();

            // 从ES中查询店铺数据
            //从es中查询数据
            Map<String, Object> map = ESUtils.pcSeachShopData(webGoodsListQueryParam);
            ArrayList<ArticleShopDTO> list = (ArrayList<ArticleShopDTO>) map.get("list");
            if (null != list && !list.isEmpty()) {
                for (ArticleShopDTO articleShopDto : list) {
                    // 尝试从Redis中获取店铺详细信息，如果不存在则从数据库查询并存入Redis
                    //根据前缀+CommonId从redis 中查询，如果存在将结果添加到返回值中，如果redis中不存在就从mysql中查询，将结果存入redis和返回值中
                    String jsonString = RedisUtil.get("unimall_pc_shop_shopId_" + articleShopDto.getShopId().toString());
                    if (null == jsonString) {
                        webShopListQueryResults = this.selectByShopId(articleShopDto.getShopId());
                        if (null == webShopListQueryResults || webShopListQueryResults.size() == 0) {
                            list2.add(articleShopDto.getShopId());
                            continue;
                        }
                        webShopListQueryResults.get(0).setShopName(articleShopDto.getShopName());
                        webShopListQueryResults.get(0).setPlusMember(plusMember);
                        webShopListQueryResults.get(0).setPlusDiscount(plusDiscount);
                        result.add(webShopListQueryResults.get(0));
                        //1天后过期
                        RedisUtil.set("unimall_pc_shop_shopId_" + articleShopDto.getShopId(), JsonUtils.toString(webShopListQueryResults.get(0)), 60 * 60 * 24);
                    } else {
                        WebShopListQueryEsDTO webShopListQueryResult = objectMapper.readValue(jsonString, WebShopListQueryEsDTO.class);
                        webShopListQueryResult.setShopName(articleShopDto.getShopName());
                        webShopListQueryResult.setPlusMember(plusMember);
                        webShopListQueryResult.setPlusDiscount(plusDiscount);
                        result.add(webShopListQueryResult);
                    }
                }
                // 如果有店铺信息需要从ES中删除，标记为list2
                if (0 != list2.size()) {
                    ESUtils.deleteShop(list2);
                }
                // 根据getGoodsListFirstShow值对搜索结果进行排序
                // 按照 getGoodsListFirstShow 值从大到小排序
                result.sort((o1, o2) -> {
                    // 首先比较 getGoodsListFirstShow 值，从大到小排列
                    int compareFirstShow = o2.getShopListFirstShow().compareTo(o1.getShopListFirstShow());
                    if (compareFirstShow != 0) {
                        return compareFirstShow;
                    } else {
                        // 如果 getGoodsListFirstShow 值相同，则比较 levelId 的大小
                        return Integer.compare(o1.getLevelId(), o2.getLevelId());
                    }
                });
                resultMap.put("list", result);
                resultMap.put("plusStatus", selectplus);
                resultMap.put("totalCount", map.get("count"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.success(resultMap);
    }

    /**
     * 设置热门搜索词。根据传入的通用名（commonName），查询是否存在对应的热门搜索词。如果存在，则增加其搜索次数；
     * 如果不存在，则创建一个新的热门搜索词。该方法通过调用基础服务客户端（baseServiceClient）来实现对热门搜索词的查询、
     * 更新和添加操作。
     *
     * @param commonName 通用名，用于查询或创建热门搜索词。
     */
    private void setHotSearchWords(String commonName) {
        // 检查传入的commonName是否为空或空字符串
        if (null != commonName && !commonName.equals("")) {
            // 查询与commonName匹配的热门搜索词
            GoodsMerchantSearchWordDTO searchWord = baseServiceClient.queryHotSearchWords(commonName);
            // 初始化一个新的热门搜索词对象
            GoodsMerchantSearchWordDTO record = new GoodsMerchantSearchWordDTO();
            // 检查查询到的热门搜索词是否存在且不为空字符串
            if (null != searchWord && !searchWord.equals("")) {
                // 如果存在，则增加其搜索次数，并更新到后台系统
                searchWord.setSearchNums(searchWord.getSearchNums() + 1);
                baseServiceClient.updateHotSearchWords(searchWord);
            } else {
                // 如果不存在，则创建一个新的热门搜索词，并设置其相关属性，包括搜索关键词、拼音索引和搜索次数
                record.setSearchKeyword(commonName);
                record.setSearchCharIndex(PinyinUtils.ToPinyin(commonName));
                record.setSearchNums(1);
                // 将新创建的热门搜索词添加到后台系统
                baseServiceClient.addHotSearchWords(record);
            }
        }
    }

    /**
     * 创建商品索引。
     *
     * @return 返回操作结果。
     */
    @Override
    public ResponseResult createGoodsIndex() {
        return ESUtils.setMappings();
    }

    /**
     * 创建店铺索引。
     *
     * @return 返回操作结果。
     */
    @Override
    public ResponseResult createShopIndex() {
        return ESUtils.setShopMappings();
    }

    /**
     * 创建敏感信息索引。
     *
     * @return 返回操作结果。
     */
    @Override
    public ResponseResult createSensitiveIndex() {
        return ESUtils.createSensitiveIndex();
    }

    /**
     * 删除商品索引。
     *
     * @return 返回操作结果。
     */
    @Override
    public ResponseResult deleteGoodsIndex() {
        return ESUtils.deleteGoodsIndex();
    }

    /**
     * 删除敏感信息索引。
     *
     * @return 返回操作结果。
     */
    @Override
    public ResponseResult deleteSensitiveIndex() {
        return ESUtils.deleteSensitiveIndex();
    }

    /**
     * 删除店铺索引。
     *
     * @return 返回操作结果。
     */
    @Override
    public ResponseResult deleteShopIndex() {
        return ESUtils.deleteShopIndex();
    }

    /**
     * 导入数据到ES。
     * 从数据库中获取所有文章信息，并处理其属性内容，然后导入到ES中。
     *
     * @return 返回操作结果。
     */
    @Override
    public ResponseResult importData() {
        ResponseResult result = null;
        // 每页大小
        int pageSize = 1000;
        // 当前页码
        int page = 1;
        // 总记录数
        int totalRecords = goodsBaseMapper.selectAllCount();
        // 总页数
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);

        for (int i = 0; i < totalPages; i++) {
            // 计算偏移量
            int offset = (page - 1) * pageSize;
            // 分页查询数据
            List<ArticleDTO> articleDTOS = goodsBaseMapper.selectAll(offset, pageSize);
            // 将处理后的文章信息列表导入到ES中
            result = ESUtils.importData(articleDTOS);
            // 增加页码
            page++;
        }
        return result;
    }

    /**
     * 导入店铺数据到ES搜索引擎。
     * 从业务客户端获取所有店铺数据，然后使用ESUtils将这些数据导入到ES中。
     *
     * @return 返回操作结果，包括成功与否等信息。
     */
    @Override
    public ResponseResult importShopData() {
        List<ArticleShopDTO> articleShopDto = merchantServiceClient.selectAllShop();
        return ESUtils.importShopData(articleShopDto);
    }

    /**
     * 导入敏感数据到ES搜索引擎。
     * 从基础服务客户端获取所有敏感数据，然后使用ESUtils将这些数据导入到ES中。
     *
     * @return 返回操作结果，包括成功与否等信息。
     */
    @Override
    public ResponseResult importSensitiveData() {
        List<BaseSensitiveVO> baseSensitiveVOList = baseServiceClient.selectAllSensitive();
        return ESUtils.importSensitiveData(baseSensitiveVOList);
    }

    /**
     * 删除所有商品数据 from ES搜索引擎。
     * 该方法用于清除ES中的所有商品数据，不接受任何参数。
     *
     * @return 返回操作结果，包括成功与否等信息。
     */
    @Override
    public ResponseResult deleteGoodsData() {
        return ESUtils.deleteAll();
    }

    /**
     * 删除所有店铺数据 from ES搜索引擎。
     * 该方法用于清除ES中的所有店铺数据，不接受任何参数。
     *
     * @return 返回操作结果，包括成功与否等信息。
     */
    @Override
    public ResponseResult deleteShopData() {
        return ESUtils.deleteShopAll();
    }

    /**
     * 删除所有敏感数据 from ES搜索引擎。
     * 该方法用于清除ES中的所有敏感数据，不接受任何参数。
     *
     * @return 返回操作结果，包括成功与否等信息。
     */
    @Override
    public ResponseResult deleteSensitiveData() {
        return ESUtils.deleteSensitiveAll();
    }

    /**
     * 根据前端查询参数，从ES中搜索商品数据，并结合Redis缓存策略，优化查询性能。
     *
     * @param webGoodsListQueryParam 前端传来的查询参数，包含商品的检索条件。
     * @return 返回搜索结果，包括商品列表、总数量等信息。
     */
    @Override
    public ResponseResult h5SeachGoodsData(WebGoodsListQueryDTO webGoodsListQueryParam) {
        Map<String, Object> resultMap = null;
        try {
            resultMap = new HashMap<>();
            List<GoodsListQueryEsDTO> result = new ArrayList<>();
            GoodsListQueryEsDTO gdsListQueryResult = null;
            List<Integer> list2 = new ArrayList<>();

            // 从ES中查询商品搜索数据
            //从es中查询数据
            Map<String, Object> map = ESUtils.h5SeachGoodsData(webGoodsListQueryParam);
            // 设置热搜词
            // 添加热搜词
            setHotSearchWords(webGoodsListQueryParam.getCommonName());
            List<ArticleDTO> list = (List<ArticleDTO>) map.get("list");
            if (null != list && !list.isEmpty()) {
                for (ArticleDTO articleDto : list) {
                    // 获取商品信息，如果缓存不存在，则从ES中查询并存入缓存
                    gdsListQueryResult = this.selectByCommonId(articleDto.getCommonId());
                    if (null == gdsListQueryResult) {
                        list2.add(articleDto.getCommonId());
                        continue;
                    }
                    gdsListQueryResult.setCommonNameOrignial(gdsListQueryResult.getCommonName());
                    //替换高亮商品名称
                    gdsListQueryResult.setCommonName(articleDto.getCommonName());

                    ActivityGoodsDetailDTO activityGoodsDetailDTO = goodsCommonService.dealActivity(Integer.valueOf(gdsListQueryResult.getGoodsId()), gdsListQueryResult.getCommonId(), gdsListQueryResult.getCommonIsXian());
                    if (null != activityGoodsDetailDTO && activityGoodsDetailDTO.getActivityPrice() != null) {
                        gdsListQueryResult.setCommonPrice(activityGoodsDetailDTO.getActivityPrice());
                    }
                    result.add(gdsListQueryResult);
                }
                // 对于ES中不存在的商品ID，进行删除操作
                if (!list2.isEmpty()) {
                    ESUtils.delete(list2);
                }
                // 组装搜索结果的响应信息
                resultMap.put("list", result);
                resultMap.put("totalCount", map.get("count"));
                resultMap.put("goodsBrandCatParam", map.get("goodsBrandCatParam"));
                resultMap.put("goodsAttributeResults", map.get("gdbGoodsAttParams"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 返回处理后的搜索结果
        return ResponseResult.success(resultMap);
    }

    /**
     * 根据查询参数搜索H5店铺数据。
     *
     * @param shopListQueryParam 查询参数，包含店铺的搜索条件。
     * @return 返回搜索结果，包括店铺列表和总数。
     */
    @Override
    public ResponseResult h5SeachShopData(ShopListQueryDTO shopListQueryParam) {
        Map<String, Object> resultMap = null;
        try {
            resultMap = new HashMap<>();
            List<ShopListQueryDTO> result = new ArrayList<>();
            ShopListQueryDTO shopListQueryDto = null;
            List<Integer> list2 = new ArrayList<>();

            // 从ES中查询店铺数据
            //从es中查询数据
            Map<String, Object> map = ESUtils.h5SeachShopData(shopListQueryParam);
            ArrayList<ArticleShopDTO> list = (ArrayList<ArticleShopDTO>) map.get("list");
            if (null != list && !list.isEmpty()) {
                for (ArticleShopDTO articleShopDto : list) {
                    // 如果Redis中不存在，则从数据库中查询并存入Redis
                    shopListQueryDto = this.selectByH5ShopId(articleShopDto.getShopId());
                    if (null == shopListQueryDto) {
                        list2.add(articleShopDto.getShopId());
                        continue;
                    }
                    shopListQueryDto.setShopName(articleShopDto.getShopName());
                    result.add(shopListQueryDto);
                    //1天后过期
                    RedisUtil.set("unimall_h5_shop_shopId_" + articleShopDto.getShopId(), JsonUtils.toString(shopListQueryDto), 60 * 60 * 24);
                }
                // 如果有店铺在数据库中不存在，则从ES中删除这些店铺
                if (!list2.isEmpty()) {
                    ESUtils.deleteShop(list2);
                }
                // 将搜索结果和总数放入结果映射中
                resultMap.put("list", result);
                resultMap.put("totalCount", map.get("count"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 返回搜索结果
        return ResponseResult.success(resultMap);
    }

    /**
     * 根据查询条件检索品牌属性数据。
     *
     * @param webGoodsListQueryParam 查询条件对象，包含品牌属性的检索参数。
     * @return 返回检索结果，包含品牌属性的数据。
     */
    @Override
    public ResponseResult querySeachBrandAttributeData(WebGoodsListQueryDTO webGoodsListQueryParam) {
        // 使用ESUtils查询品牌属性数据
        Map<String, Object> seachBrandAttributeMap = ESUtils.querySeachBrandAttributeData(webGoodsListQueryParam);
        // 返回查询结果
        return ResponseResult.success(seachBrandAttributeMap);
    }

    /**
     * 添加敏感信息列表。
     *
     * @param list 包含待添加敏感信息的列表。
     * @return 返回操作结果，表示添加是否成功。
     */
    @Override
    public ResponseResult addSensitiveList(List<BaseSensitiveVO> list) {
        // 使用ESUtils添加敏感信息列表
        ResponseResult result = ESUtils.addOrUpdateSensitive(list);
        return result;
    }

    /**
     * 删除敏感信息列表。
     *
     * @param list 包含待删除敏感信息ID的列表。
     * @return 返回操作结果，表示删除是否成功。
     */
    @Override
    public ResponseResult deletSensitiveList(List<Integer> list) {
        // 使用ESUtils删除敏感信息列表
        ResponseResult result = ESUtils.deleteSensitive(list);
        return result;
    }

    /**
     * 在 Elasticsearch 中搜索敏感信息。
     *
     * @param baseSensitiveDTO 查询条件对象，包含搜索敏感信息的参数。
     * @return 返回搜索结果，如果发生异常，则返回失败结果。
     */
    @Override
    public ResponseResult esSeachSensitive(BaseSensitiveDTO baseSensitiveDTO) {
        try {
            // 使用ESUtils进行敏感信息搜索
            ResponseResult result = ESUtils.esSeachSensitive(baseSensitiveDTO);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 如果发生异常，返回失败结果
        return ResponseResult.fail();
    }
}
