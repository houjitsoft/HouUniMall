package com.houjiang.merchant.service.impl.mall;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.merchant.common.config.GlobalConfig;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.distribution.DistributionLevel;
import com.houjiang.merchant.common.entity.mall.*;
import com.houjiang.merchant.common.enums.AuditEnum;
import com.houjiang.merchant.common.enums.DeliveryEnum;
import com.houjiang.merchant.common.enums.GoodsEnum;
import com.houjiang.merchant.common.enums.YesNoEnum;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.distribution.DistributionLevelMapper;
import com.houjiang.merchant.common.mapper.mall.*;
import com.houjiang.merchant.common.util.*;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.*;
import com.houjiang.merchant.service.ms.IMsService;
import com.houjiang.merchant.service.pt.IPtService;
import com.houjiang.merchant.service.user.IUserLevelService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.*;
import com.houjiang.merchant.vo.distribution.DistributionLevelSimpleVo;
import com.houjiang.merchant.vo.distribution.MallGoodsDistributionSimpleVo;
import com.houjiang.merchant.vo.mall.*;
import com.houjiang.merchant.vo.user.DiscountUserListedVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.houjiang.merchant.common.validation.Assert;
import org.springframework.util.ObjectUtils;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * 商家账号实现类
 */
@Service
public class MallGoodsServiceImpl implements IMallGoodsService {

    @Resource
    MallGoodsDistributionMapper mallGoodsDistributionMapper;
    @Resource
    DistributionLevelMapper distributionLevelMapper;
    @Resource
    MallGoodsMapper mallGoodsMapper;
    @Resource
    MallGoodsImageMapper mallGoodsImageMapper;
    @Resource
    MallGoodsCategoryIndexMapper mallGoodsCategoryIndexMapper;

    @Resource
    MallGoodsCategoryMapper mallGoodsCategoryMapper;

    @Resource
    MallShopGoodsCategoryMapper mallShopGoodsCategoryMapper;

    @Resource
    MallGoodsSkuNameMapper goodsSkuNameMapper;
    @Resource
    MallGoodsSkuValueMapper goodsSkuValueMapper;
    @Resource
    MallGoodsSkuMapper goodsSkuMapper;
    @Resource
    MallGoodsDiscountMapper mallGoodsDiscountMapper;
    @Resource
    IMallShopService mallShopService;
    @Resource
    IMallGoodsCategoryIndexService iMallGoodsCategoryIndexService;
    @Resource
    IMallGoodsCategoryService categoryService;
    @Resource
    IMallShopGoodsCategoryService iMallShopGoodsCategoryService;
    @Resource
    IMallGoodsImageService iMallGoodsImageService;
    @Resource
    IMallExpressTplService iMallExpressTplService;
    @Resource
    IMallGoodsSkuService iMallGoodsSkuService;
    @Resource
    IPtService iPtService;
    @Resource
    IMsService iMsService;
    @Resource
    IMallGoodsDiscountService iMallGoodsDiscountService;
    @Resource
    IMallGoodsDistributionService iMallGoodsDistributionService;
    @Resource
    IUserLevelService iUserLevelService;

    @Resource
    MallGoodsSkuNameMapper mallGoodsSkuNameMapper;

    @Resource
    MallCartMapper mallCartMapper;

    @Resource
    MallGoodsSkuValueMapper mallGoodsSkuValueMapper;

    @Resource
    MallGoodsSkuMapper mallGoodsSkuMapper;

    @Override
    public JSONArray getGoodsSaleRank(Integer num, Integer shopId) {
        List<Map<String, Object>> datalist = mallGoodsMapper.selectWithCast(shopId, num);
        JSONArray ret = new JSONArray();
        for (Map<String, Object> item : datalist) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", item.get("name"));
            jsonObject.put("id", item.get("id"));
            jsonObject.put("sale_money", item.get("sale_money"));
            //jsonObject.put("sale_rank", item.get("sale_rank"));
            jsonObject.put("image", UrlUtils.toShopAbsoluteUrl(item.get("image").toString()));
            ret.add(jsonObject);
        }
        return ret;
    }

    @Override
    public MallGoods getById(Integer id) {
        return mallGoodsMapper.selectOne(new QueryWrapper<MallGoods>().eq("id", id).eq("delete_time", 0).eq("shop_id", MerchantThreadLocal.getShopId()));
    }

    @Override
    public Boolean checkShopGoodsCategoryHas(Integer shopGoodsCategoryId) {
        MallGoods mallGoods = mallGoodsMapper.selectOne(new QueryWrapper<MallGoods>().eq("shop_category_id", shopGoodsCategoryId).eq("delete_time", 0).last("limit 1"));
        return StringUtils.isNull(mallGoods) ? false : true;
    }

    @Override
    public Long getShopGoodsCategoryCountsCateId(Integer shopId, Integer shopCategoryId) {
        Long count = mallGoodsMapper.selectCount(new QueryWrapper<MallGoods>().eq("shop_id", shopId).eq("shop_category_id", shopCategoryId).eq("delete_time", 0));
        return count;
    }


    /**
     * 商品列表
     *
     * @param pageValidate   分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<MallGoodsListedVo>
     * @author Amos
     */
    @Override
    public PageResult<MallGoodsListedVo> list(PageValidate pageValidate, MallGoodsSearchValidate searchValidate) {
        Integer page = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();

        QueryWrapper<MallGoods> queryWrapper = new QueryWrapper<MallGoods>();
        queryWrapper.orderByDesc(Arrays.asList("sort", "id"));
        if (StringUtils.isNotNull(searchValidate.getShopId())) {
            queryWrapper.eq("shop_id", searchValidate.getShopId());
        }

        if (StringUtils.isNotEmpty(searchValidate.getName())) {
            queryWrapper.like("name", searchValidate.getName());
        }

        if (StringUtils.isNotNull(searchValidate.getCategory_id())) {
            List<Integer> goodsIds = iMallGoodsCategoryIndexService.getGoodsIdsByCid(searchValidate.getCategory_id());
            if (goodsIds.size() > 0) {
                queryWrapper.in("id", goodsIds);
            } else {
                queryWrapper.eq("id", -1);
            }
        }

        if (StringUtils.isNotNull(searchValidate.getShop_category_id())) {
            queryWrapper.eq("shop_category_id", searchValidate.getShop_category_id());
        }

        if (StringUtils.isNotNull(searchValidate.getIs_discount())) {
            queryWrapper.eq("is_discount", searchValidate.getIs_discount());
        }

        if (StringUtils.isNotNull(searchValidate.getIs_distribution())) {
            queryWrapper.eq("is_distribution", searchValidate.getIs_distribution());
        }

        QueryWrapper<MallGoods> countAuditQueryWrapper = queryWrapper.clone();
        countAuditQueryWrapper = this.searchTypeAttr(countAuditQueryWrapper, "audit");

        QueryWrapper<MallGoods> countAuditRefuseQueryWrapper = queryWrapper.clone();
        countAuditRefuseQueryWrapper = this.searchTypeAttr(countAuditRefuseQueryWrapper, "audit_refuse");

        QueryWrapper<MallGoods> countNotSaleQueryWrapper = queryWrapper.clone();
        countNotSaleQueryWrapper = this.searchTypeAttr(countNotSaleQueryWrapper, "not_sale");

        QueryWrapper<MallGoods> countSaleQueryWrapper = queryWrapper.clone();
        countSaleQueryWrapper = this.searchTypeAttr(countSaleQueryWrapper, "sale");

        QueryWrapper<MallGoods> countStockWarningQueryWrapper = queryWrapper.clone();
        countStockWarningQueryWrapper = this.searchTypeAttr(countStockWarningQueryWrapper, "stock_warning");

        if (StringUtils.isNotEmpty(searchValidate.getType())) {
            queryWrapper = this.searchTypeAttr(queryWrapper, searchValidate.getType());
        }

        Map<String, Object> extend = new HashMap<>();
        extend.put("count_audit", mallGoodsMapper.selectCount(countAuditQueryWrapper));
        extend.put("count_audit_refuse", mallGoodsMapper.selectCount(countAuditRefuseQueryWrapper));
        extend.put("count_not_sale", mallGoodsMapper.selectCount(countNotSaleQueryWrapper));
        extend.put("count_sale", mallGoodsMapper.selectCount(countSaleQueryWrapper));
        extend.put("count_stock_warning", mallGoodsMapper.selectCount(countStockWarningQueryWrapper));
        IPage<MallGoods> iPage = mallGoodsMapper.selectPage(new Page<>(page, limit), queryWrapper);
        List<MallGoodsListedVo> list = new LinkedList<>();
        for (MallGoods item : iPage.getRecords()) {
            MallGoodsListedVo vo = new MallGoodsListedVo();
            BeanUtils.copyProperties(item, vo);
            vo.setImage(UrlUtils.toShopAbsoluteUrl(item.getImage()));
            vo.setCreateTime(TimeUtils.timestampToDate(item.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(item.getUpdateTime()));
            vo.setVideoUri(UrlUtils.toShopAbsoluteUrl(item.getVideoUri()));
            vo.setAllSaleNum(item.getSaleNum() + item.getVirtualSaleNum());
            vo.setAllVisitNum(item.getVisitNum() + item.getVirtualVisitNum());
            vo.setAuditStatusText(AuditEnum.getStatusDesc(item.getAuditStatus()));
            vo.setCanAudit(this.getCanAuditAttr(item));
            vo.setCanDelete(this.getCanDeleteAttr(item));
            vo.setCanReAudit(this.getCanReAuditAttr(item));
            vo.setCanSale(this.getCanSaleAttr(item));
            vo.setCategoryIds(iMallGoodsCategoryIndexService.getCateIdsByGoodsId(item.getId()));
            vo.setCategoryIdsText(categoryService.getCateNamesByGoodsId(item.getId()));
            vo.setExpressTypeText(this.getExpressTypeTextAttr(item));
            vo.setGoodsSendMode(this.getGoodsSendModeAttr(item));
            vo.setGoodsSendModeText(this.getGoodsSendModeTextAttr(item));
            vo.setPriceText(this.getPriceTextAttr(item));
            vo.setSkuTypeText(this.getSkuTypeTextAttr(item));
            vo.setStatusText(this.getStatusTextAttr(item));
            vo.setVideoFromText(this.getVideoFromTextAttr(item));
            vo.setPt(iPtService.getGoodsPt(item.getId()));
            vo.setMs(iMsService.getGoodsMs(item.getId()));
            vo.setSku(iMallGoodsSkuService.getListByGoods(item.getId()));
            list.add(vo);
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list, extend);
    }

    /**
     * 商品详情
     *
     * @param id 主键参数
     * @return MallGoods
     * @author Amos
     */
    @Override
    public MallGoodsDetailVo detail(Integer id) {
        MallGoods model = mallGoodsMapper.selectOne(
                new QueryWrapper<MallGoods>()
                        .eq("id", id)
                        .eq("shop_id", MerchantThreadLocal.getShopId())
                        .eq("delete_time", 0)
                        .last("limit 1"));

        Assert.notNull(model, "商品不存在");

        MallGoodsDetailVo vo = new MallGoodsDetailVo();
        BeanUtils.copyProperties(model, vo);
        vo.setImage(UrlUtils.toShopAbsoluteUrl(model.getImage()));
        vo.setCreateTime(TimeUtils.timestampToDate(model.getCreateTime()));
        vo.setUpdateTime(TimeUtils.timestampToDate(model.getUpdateTime()));
        vo.setVideoCover(UrlUtils.toShopAbsoluteUrl(model.getVideoCover()));
        vo.setVideoUri(UrlUtils.toShopAbsoluteUrl(model.getVideoUri()));
        vo.setAllSaleNum(model.getSaleNum() + model.getVirtualSaleNum());
        vo.setAllVisitNum(model.getVisitNum() + model.getVirtualVisitNum());
        vo.setAuditStatusText(AuditEnum.getStatusDesc(model.getAuditStatus()));
        vo.setCanAudit(this.getCanAuditAttr(model));
        vo.setCanDelete(this.getCanDeleteAttr(model));
        vo.setCanReAudit(this.getCanReAuditAttr(model));
        vo.setCanSale(this.getCanSaleAttr(model));
        vo.setCategoryIds(iMallGoodsCategoryIndexService.getCateIdsByGoodsId(model.getId()));
        vo.setCategoryIdsText(categoryService.getCateNamesByGoodsId(model.getId()));
        vo.setExpressTypeText(this.getExpressTypeTextAttr(model));
        vo.setGoodsSendMode(this.getGoodsSendModeAttr(model));
        vo.setGoodsSendModeText(this.getGoodsSendModeTextAttr(model));
        vo.setPriceText(this.getPriceTextAttr(model));
        vo.setSkuTypeText(this.getSkuTypeTextAttr(model));
        vo.setStatusText(this.getStatusTextAttr(model));
        vo.setVideoFromText(this.getVideoFromTextAttr(model));
        vo.setPt(iPtService.getGoodsPt(model.getId()));
        vo.setMs(iMsService.getGoodsMs(model.getId()));
        vo.setGoodsImage(iMallGoodsImageService.getListByGoodsId(model.getId()));
        vo.setSkuNameList(iMallGoodsSkuService.getSkuNameList(model.getId()));
        vo.setSku(iMallGoodsSkuService.getListByGoods(model.getId()));

        // 转绝对路径
        String domain = RequestUtils.uri();
        String engine = ConfigUtils.get("storage", "default", "local");
        if (!engine.equals("") && !engine.equals("local")) {
            Map<String, String> mapStorage = ConfigUtils.getMap("storage", engine);
            domain = mapStorage.getOrDefault("domain", RequestUtils.uri());
        } else {
            domain = domain + "/" + GlobalConfig.shopPublicPrefix + "/";
        }
        String content = model.getContent().replaceAll("##domain##", domain);
        vo.setContent(content);

        return vo;
    }

    /**
     * 商品新增
     *
     * @param createValidate 参数
     * @author Amos
     */
    @Override
    @Transactional
    public Integer add(MallGoodsCreateValidate createValidate) {
        // 笛卡尔积规格
        createValidate.setSku(this.__cartesianSpec(createValidate));
        // 验证商品数据
        this.__checkAddGoodsData(createValidate);

        String domain = RequestUtils.uri();
        String engine = ConfigUtils.get("storage", "default", "local");
        if (!engine.equals("local")) {
            Map<String, String> mapStorage = ConfigUtils.getMap("storage", engine);
            domain = mapStorage.getOrDefault("domain", RequestUtils.uri());
        } else {
            domain = domain + "/merchant/uploads";
        }
        String content = createValidate.getContent().replaceAll(domain, "##domain##");

        // 验证商品数据
        MallGoods model = new MallGoods();
        model.setCreateTime(System.currentTimeMillis() / 1000);
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        model.setShopId(createValidate.getShopId());
        model.setName(createValidate.getName());
        model.setCode(createValidate.getCode());
        model.setDescript(createValidate.getDescript());
        model.setCategoryIds(StringUtils.join(createValidate.getCategoryIds(), ","));
        model.setShopCategoryId(createValidate.getShopCategoryId());
        model.setImage(UrlUtils.toShopRelativeUrl(createValidate.getGoodsImage().get(0)));
        model.setVideoIs(createValidate.getVideoIs());
        model.setVideoFrom(createValidate.getVideoFrom());
        model.setVideoCover(UrlUtils.toShopRelativeUrl(createValidate.getVideoCover()));
        model.setVideoUri(createValidate.getVideoUri());
        model.setSkuType(createValidate.getSkuType());
        model.setIsExpress(1);
        model.setExpressType(createValidate.getExpressType());
        model.setExpressTplId(createValidate.getExpressTplId());
        model.setStockWarning(createValidate.getStockWarning());
        model.setContent(createValidate.getContent());
//        model.setSaleMoney(createValidate.getSaleMoney());
        model.setSort(createValidate.getSort());
        model.setStatus(createValidate.getStatus());
        model.setAuditStatus(this.getCreateUpdateAuditStatus(createValidate.getShopId(), null));
//        model.setAuditContent(createValidate.getAuditContent());
        model.setShopRecommend(YesNoEnum.NO.getCode());
        model.setIsRecycle(YesNoEnum.NO.getCode());
        model.setContent(content);
        model.setIsDiscount(YesNoEnum.NO.getCode());
        model.setDiscountType(GoodsEnum.DISCOUNT_TYPE_DEFAULT.getCode());
        model.setIsDistribution(YesNoEnum.NO.getCode());
        model.setDistributionType(GoodsEnum.DISTRIBUTION_TYPE_DEFAULT.getCode());
        model.setTotalStock(0);
        model.setMinPrice(new BigDecimal(0));
        model.setMaxPrice(new BigDecimal(0));
        model.setMinLinePrice(new BigDecimal(0));
        model.setMaxLinePrice(new BigDecimal(0));
        mallGoodsMapper.insert(model);

        // 添加商品规格
        Map<String, Object> goodsSkuData = this.__addGoodsSpec(model.getId(), createValidate);
        model.setMinPrice((BigDecimal) goodsSkuData.get("minPrice"));
        model.setMaxPrice((BigDecimal) goodsSkuData.get("maxPrice"));
        model.setMinLinePrice((BigDecimal) goodsSkuData.get("minLineationPrice"));
        model.setMaxLinePrice((BigDecimal) goodsSkuData.get("maxLineationPrice"));
        model.setTotalStock((Integer) goodsSkuData.get("totalStock"));
        mallGoodsMapper.updateById(model);

        // 添加商品图片
        for (String image : createValidate.getGoodsImage()) {
            MallGoodsImage goodsImage = new MallGoodsImage();
            goodsImage.setGoodsId(model.getId());
            goodsImage.setImage(UrlUtils.toShopRelativeUrl(image));
            goodsImage.setCreateTime(System.currentTimeMillis() / 1000);
            goodsImage.setUpdateTime(System.currentTimeMillis() / 1000);
            mallGoodsImageMapper.insert(goodsImage);
        }

        // 添加商品分类
        for (Integer categoryId : createValidate.getCategoryIds()) {
            MallGoodsCategoryIndex goodsCategoryIndex = new MallGoodsCategoryIndex();
            goodsCategoryIndex.setGoodsId(model.getId());
            goodsCategoryIndex.setCategoryId(categoryId);
            goodsCategoryIndex.setCreateTime(System.currentTimeMillis() / 1000);
            goodsCategoryIndex.setUpdateTime(System.currentTimeMillis() / 1000);
            mallGoodsCategoryIndexMapper.insert(goodsCategoryIndex);
        }

        //更新在售商品的数量
        mallShopService.updateOnlineGoods(createValidate.getShopId());
        return model.getId();
    }

    public MallGoods _create_update_data(MallGoodsUpdateValidate updateValidate, List<MallGoodsSkuListedVo> sku) {
        MallGoods data = new MallGoods();
        String domain = RequestUtils.uri();
        String engine = ConfigUtils.get("storage", "default", "local");
        if (!engine.equals("local")) {
            Map<String, String> mapStorage = ConfigUtils.getMap("storage", engine);
            domain = mapStorage.getOrDefault("domain", RequestUtils.uri());
        } else {
            domain = domain + "/merchant/uploads";
        }
        String content = updateValidate.getContent().replaceAll(domain, "##domain##");

        data.setId(updateValidate.getId());
        data.setName(updateValidate.getName());
        data.setCode(updateValidate.getCode());
        data.setDescript(updateValidate.getDescript());
        data.setCategoryIds(StringUtils.join(updateValidate.getCategoryIds(), ","));
        data.setImage(UrlUtils.toShopRelativeUrl(updateValidate.getGoodsImage().get(0)));

        data.setVideoIs(updateValidate.getVideoIs());
        data.setVideoFrom(updateValidate.getVideoFrom());
        data.setVideoCover(UrlUtils.toShopRelativeUrl(updateValidate.getVideoCover()));
        data.setVideoUri(UrlUtils.toShopRelativeUrl(updateValidate.getVideoUri()));

        data.setSkuType(updateValidate.getSkuType());

        data.setIsExpress(1);
        data.setExpressType(updateValidate.getExpressType());
        data.setExpressTplId(updateValidate.getExpressTplId());
        data.setExpressMoney(updateValidate.getExpressMoney());

        data.setTotalStock(updateValidate.getSku().stream().mapToInt(MallGoodsSkuVo::getStock).sum());
        data.setStockWarning(updateValidate.getStockWarning());

        data.setMinPrice(updateValidate.getSku()
                .stream()
                .map(item -> item.getPrice())
                .filter(price -> price != null)
                .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO));

        data.setMaxPrice(updateValidate.getSku()
                .stream()
                .map(item -> item.getPrice())
                .filter(price -> price != null)
                .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO));


        data.setMinLinePrice(updateValidate.getSku()
                .stream()
                .map(item -> item.getLinePrice())
                .filter(linePrice -> linePrice != null)
                .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
        data.setMaxLinePrice(updateValidate.getSku()
                .stream()
                .map(item -> item.getLinePrice())
                .filter(linePrice -> linePrice != null)
                .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
//
        data.setVirtualSaleNum(updateValidate.getVirtualSaleNum());
        data.setVirtualVisitNum(updateValidate.getVirtualVisitNum());
        data.setSort(updateValidate.getSort());
        data.setStatus(updateValidate.getStatus());
        data.setContent(content);

        data.setShopId(updateValidate.getShopId());
        data.setShopRecommend(updateValidate.getShopRecommend());
        data.setShopCategoryId(updateValidate.getShopCategoryId());

        data.setIsDiscount(YesNoEnum.NO.getCode());
        data.setDiscountType(GoodsEnum.DISCOUNT_TYPE_DEFAULT.getCode());

        data.setIsDistribution(YesNoEnum.NO.getCode());
        data.setDistributionType(GoodsEnum.DISTRIBUTION_TYPE_DEFAULT.getCode());

        data.setUpdateTime(System.currentTimeMillis() / 1000);
        return data;
    }


    /**
     * 商品编辑
     *
     * @param updateValidate 参数
     * @author Amos
     */
    @Override
    @Transactional
    public String edit(MallGoodsUpdateValidate updateValidate) {
        String msg = "成功";
        MallGoods model = mallGoodsMapper.selectOne(
                new QueryWrapper<MallGoods>()
                        .eq("id", updateValidate.getId())
                        .eq("shop_id", MerchantThreadLocal.getShopId())
                        .eq("delete_time", 0)
                        .last("limit 1"));


        MallGoods oldGoods = new MallGoods();

        BeanUtils.copyProperties(model, oldGoods);

        List<MallGoodsSkuListedVo> sku = new ArrayList<>();
        if (model != null) {
            sku = this.iMallGoodsSkuService.getListByGoods(model.getId());
        }
        MallGoods data = this._create_update_data(updateValidate, sku);
        data.setAuditStatus(this.getCreateUpdateAuditStatus(model.getShopId(), model));
        // 旧的规格类型
        Integer oldSkuType = model.getSkuType();
        boolean skuTypeHasChange = oldSkuType.intValue() != updateValidate.getSkuType().intValue();
        boolean checkSkuHasUpdate = skuTypeHasChange || checkSkuHasUpdate(sku, oldGoods, updateValidate);

        if (!checkSkuHasUpdate) {
            data.setIsDiscount(oldGoods.getIsDiscount());
            data.setDiscountType(oldGoods.getDiscountType());
            data.setIsDistribution(oldGoods.getIsDistribution());
            data.setDistributionType(oldGoods.getDistributionType());
        } else {


            if (oldGoods.getIsDistribution().intValue() == 1) {
                msg = "该商品属于分销商品，请重新设置分销信息";
            }

        }

        mallGoodsMapper.updateById(data);

        // 添加商品图片
        mallGoodsImageMapper.delete(new QueryWrapper<MallGoodsImage>().eq("goods_id", model.getId()));
        for (String image : updateValidate.getGoodsImage()) {
            MallGoodsImage goodsImage = new MallGoodsImage();
            goodsImage.setGoodsId(model.getId());
            goodsImage.setImage(UrlUtils.toShopRelativeUrl(image));
            goodsImage.setCreateTime(System.currentTimeMillis() / 1000);
            goodsImage.setUpdateTime(System.currentTimeMillis() / 1000);
            mallGoodsImageMapper.insert(goodsImage);
        }

        // 添加商品分类
        mallGoodsCategoryIndexMapper.delete(new QueryWrapper<MallGoodsCategoryIndex>().eq("goods_id", model.getId()));
        for (Integer categoryId : updateValidate.getCategoryIds()) {
            MallGoodsCategoryIndex goodsCategoryIndex = new MallGoodsCategoryIndex();
            goodsCategoryIndex.setGoodsId(model.getId());
            goodsCategoryIndex.setCategoryId(categoryId);
            goodsCategoryIndex.setCreateTime(System.currentTimeMillis() / 1000);
            goodsCategoryIndex.setUpdateTime(System.currentTimeMillis() / 1000);
            mallGoodsCategoryIndexMapper.insert(goodsCategoryIndex);
        }


        // 处理商品规格
        mallGoodsSkuNameMapper.delete(new QueryWrapper<MallGoodsSkuName>().eq("goods_id", model.getId()));

        mallGoodsSkuValueMapper.delete(new QueryWrapper<MallGoodsSkuValue>().eq("goods_id", model.getId()));

        updateValidate = this.saveSkuNameAndValue(updateValidate, model.getId());

        List<Integer> skuNewIds = new ArrayList<>();
        boolean isMore = updateValidate.getSkuType().equals(GoodsEnum.SKU_TYPE_MORE.getCode());
        for (MallGoodsSkuVo tempSku : updateValidate.getSku()) {
            MallGoodsSku skuModel;
            if (tempSku.getId() != null && !skuTypeHasChange) {
                skuModel = mallGoodsSkuMapper.selectById(tempSku.getId());
                skuModel.setGoodsId(model.getId());
                skuModel.setImage(UrlUtils.toShopRelativeUrl(tempSku.getImage()));
                skuModel.setSkuValueIds(isMore ? this.getSkuValueIds(tempSku, updateValidate.getSkuNameList()).toJSONString() : new JSONArray().toJSONString());
                skuModel.setSkuValueArr(isMore ? String.valueOf(new JSONArray(tempSku.getSkuValueArr())) : new JSONArray().toJSONString());
                skuModel.setPrice(tempSku.getPrice() != null ? tempSku.getPrice() : new BigDecimal("0"));
                skuModel.setLinePrice(tempSku.getLinePrice() != null ? tempSku.getLinePrice() : new BigDecimal("0"));
                skuModel.setMarketPrice(tempSku.getMarketPrice() != null ? tempSku.getMarketPrice() : new BigDecimal("0"));
                skuModel.setStock(tempSku.getStock() != null ? tempSku.getStock() : 0);
                skuModel.setWeight(tempSku.getWeight() != null ? tempSku.getWeight() : new BigDecimal("0"));
                skuModel.setVolume(tempSku.getVolume() != null ? tempSku.getVolume() : new BigDecimal("0"));
                skuModel.setCode(tempSku.getCode());
                skuModel.setShopId(MerchantThreadLocal.getShopId());
                this.mallGoodsSkuMapper.updateById(skuModel);
            } else {
                skuModel = new MallGoodsSku();
                skuModel.setGoodsId(model.getId());
                skuModel.setImage(UrlUtils.toShopRelativeUrl(tempSku.getImage()));
                skuModel.setSkuValueIds(isMore ? this.getSkuValueIds(tempSku, updateValidate.getSkuNameList()).toJSONString() : new JSONArray().toJSONString());
                skuModel.setSkuValueArr(isMore ? String.valueOf(new JSONArray(tempSku.getSkuValueArr())) : new JSONArray().toJSONString());
                skuModel.setPrice(tempSku.getPrice() == null || tempSku.getPrice().toString() == "null" ?  new BigDecimal("0") : tempSku.getPrice());
                skuModel.setLinePrice(tempSku.getLinePrice() == null || tempSku.getLinePrice().toString() == "null" ?  new BigDecimal("0") : tempSku.getLinePrice());
                skuModel.setMarketPrice(tempSku.getMarketPrice() == null || tempSku.getMarketPrice().toString() == "null" ?  new BigDecimal("0") : tempSku.getMarketPrice());
                skuModel.setStock(tempSku.getStock() != null ? tempSku.getStock() : 0);
                skuModel.setWeight(tempSku.getWeight() == null || tempSku.getWeight().toString() == "null" ?  new BigDecimal("0") : tempSku.getWeight());
                skuModel.setVolume(tempSku.getVolume() == null || tempSku.getVolume().toString() == "null" ?  new BigDecimal("0") : tempSku.getVolume());
                skuModel.setCode(tempSku.getCode());
                skuModel.setShopId(MerchantThreadLocal.getShopId());
                this.mallGoodsSkuMapper.insert(skuModel);
            }
            skuNewIds.add(skuModel.getId());
        }

        mallGoodsSkuMapper.delete(new QueryWrapper<MallGoodsSku>().eq("goods_id", model.getId()).notIn("id", skuNewIds));

        mallCartMapper.delete(new QueryWrapper<MallCart>().eq("goods_id", model.getId()).notIn("goods_sku_id", skuNewIds));


        // 折扣&分销 重置
        iMallGoodsDiscountService.delGoodsSet(updateValidate.getId());
        iMallGoodsDistributionService.delGoodsSet(updateValidate.getId());
        //更新在售商品的数量
        mallShopService.updateOnlineGoods(updateValidate.getShopId());

        return msg;
    }

    private JSONArray getSkuValueIds(MallGoodsSkuVo tempSku, List<SkuNameListVo> skuNameList) {
        JSONArray ids = new JSONArray();

        // 对sku_value_arr中的每个值进行MD5加密
        List<String> skuValueArr = tempSku.getSkuValueArr();
        List<String> md5SkuValueArr = new ArrayList<>();

        for (String value : skuValueArr) {
            md5SkuValueArr.add(getMd5(value));
        }

        // 更新sku中的sku_value_arr为MD5值
//        tempSku.setSkuValueArr(md5SkuValueArr);

        // 遍历sku_name_list查找匹配的ID
        for (SkuNameListVo skuName : skuNameList) {
            List<SpecListVo> values = skuName.getValue();

            for (SpecListVo value : values) {
                String valueStr = value.getValue();
                String md5Value = getMd5(valueStr);

                if (md5SkuValueArr.contains(md5Value)) {
                    Integer id = value.getId();
                    if (id != null) {
                        ids.add(id);
                    }
                    // 找到匹配项后跳出内层循环，继续外层循环
                    break;
                }
            }
        }
        return ids;
    }

    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    private MallGoodsUpdateValidate saveSkuNameAndValue(MallGoodsUpdateValidate updateValidate, Integer id) {

        Integer skuType = updateValidate.getSkuType();

        if (GoodsEnum.SKU_TYPE_MORE.getCode() == skuType.intValue()) {
            List<SkuNameListVo> skuNameList =updateValidate.getSkuNameList();
            for (int i = 0 ; i < skuNameList.size() ; i++) {
                SkuNameListVo skuName = skuNameList.get(i);
                // 规格名
                MallGoodsSkuName skuNameModel = new MallGoodsSkuName();
                skuNameModel.setGoodsId(id);
                skuNameModel.setName(skuName.getName());
                mallGoodsSkuNameMapper.insert(skuNameModel);
                skuName.setId(skuNameModel.getId());
                // 规格值
                List<SpecListVo> values = skuName.getValue();
                for (int j = 0; j < values.size(); j++ ) {
                    SpecListVo value = values.get(j);
                    MallGoodsSkuValue skuValueModel = new MallGoodsSkuValue();
                    skuValueModel.setGoodsId(id);
                    skuValueModel.setSkuNameId(skuNameModel.getId());
                    skuValueModel.setValue(value.getValue());
                    mallGoodsSkuValueMapper.insert(skuValueModel);
                    value.setId(skuValueModel.getId());
                    values.set(j, value);
                }
                skuName.setValue(values);
                skuNameList.set(i, skuName);
            }
            updateValidate.setSkuNameList(skuNameList);
        } else if (GoodsEnum.SKU_TYPE_SINGLE.equals(skuType)) {
            List<MallGoodsSkuVo> skuList = updateValidate.getSku();
            if (skuList != null && !skuList.isEmpty()) {
                List<MallGoodsSkuVo> singleSku = new ArrayList<>();
                singleSku.add(skuList.get(0));
                updateValidate.setSku(singleSku);
            }
        }

        return updateValidate;
    }


    private static String getMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean checkSkuHasUpdate(List<MallGoodsSkuListedVo> sku, MallGoods goods, MallGoodsUpdateValidate updateValidate) {

        if (!goods.getSkuType().equals(updateValidate.getSkuType())) {
            return true;
        }

        if (updateValidate.getSkuType().equals(GoodsEnum.SKU_TYPE_SINGLE.getCode())) {

            return sku.get(0).getPrice().compareTo(updateValidate.getSku().get(0).getPrice()) != 0;

        } else if (updateValidate.getSkuType().equals(GoodsEnum.SKU_TYPE_MORE.getCode())) {
            Map<String, Map<String, BigDecimal>> oldSkus = new HashMap<>();
            Map<String, Map<String, BigDecimal>> newSkus = new HashMap<>();

            for (MallGoodsSkuListedVo tempSku : sku) {
                oldSkus.put(getMd5(tempSku.getSkuValueArr().toJSONString()),
                        new HashMap<String, BigDecimal>() {{
                            put("price", tempSku.getPrice());
                        }});
            }

            for (MallGoodsSkuVo tempSku : updateValidate.getSku()) {
                newSkus.put(getMd5(new JSONArray(tempSku.getSkuValueArr()).toJSONString()),
                        new HashMap<String, BigDecimal>() {{
                            put("price", tempSku.getPrice());
                        }});
            }

            if (oldSkus.size() != newSkus.size()) {
                return true;
            }


            for (Map.Entry<String, Map<String, BigDecimal>> entry : oldSkus.entrySet()) {
                if (!newSkus.containsKey(entry.getKey())) {
                    return true;
                }
                if (entry.getValue().get("price").compareTo(newSkus.get(entry.getKey()).get("price")) != 0) {
                    return true;
                }
            }


        } else {
            return true;
        }

        return false;
    }

    /**
     * 商品删除
     *
     * @param id 主键ID
     * @author Amos
     */
    @Override
    @Transactional
    public void del(Integer id) {
        this.checkGoods(id, "delete");
        MallGoods model = mallGoodsMapper.selectOne(
                new QueryWrapper<MallGoods>()
                        .eq("id", id)
                        .eq("delete_time", 0)
                        .last("limit 1"));

        Assert.notNull(model, "数据不存在!");
        mallGoodsMapper.delete(new QueryWrapper<MallGoods>().eq("id", id));
    }

    @Override
    public MallGoodsOtherListVo otherList() {
        MallGoodsOtherListVo vo = new MallGoodsOtherListVo();
        vo.setVideoFromArr(GoodsEnum.getVideoFromTextArr2());
        vo.setStatusList(GoodsEnum.getGoodsStatusTextArr2());
        vo.setSkuTypeList(GoodsEnum.getSkuTypeTextArr2());
        vo.setExpressTypeList(GoodsEnum.getGoodsExpressTypeTextArr2());
        vo.setGoodsSendModeList(ConfigUtils.deliveryConfigText());
        vo.setExpressList(iMallExpressTplService.getListByShopId(MerchantThreadLocal.getShopId()));

        vo.setCategoryList(categoryService.list(new MallGoodsCategorySearchValidate()));
        vo.setShopGoodsCategory(iMallShopGoodsCategoryService.otherList(MerchantThreadLocal.getShopId()));
        return vo;
    }

    @Override
    @Transactional
    public void status(MallShopGoodsStatusValidate statusValidate) {
        MallGoods model = this.getById(statusValidate.getId());
        if (StringUtils.isNull(model)) {
            throw new OperateException("找不到该商品信息");
        }
        model.setStatus(statusValidate.getStatus());
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        mallGoodsMapper.updateById(model);
    }

    @Override
    public MallCouponGoodsDetailVo getCouponGoods(Integer id) {
        MallGoodsDetailVo detail = this.detail(id);
        MallCouponGoodsDetailVo vo = new MallCouponGoodsDetailVo();
        BeanUtils.copyProperties(detail, vo);
        return vo;
    }

    @Override
    @Transactional
    public void discountSet(MallShopGoodsDiscountSetValidate setValidate) {
        if (setValidate.getIds().size() <= 0) {
            throw new OperateException("请选择商品");
        }

        MallGoods mallGoods = new MallGoods();
        mallGoods.setIsDiscount(setValidate.getIsDiscount());
        mallGoods.setDiscountType(GoodsEnum.DISCOUNT_TYPE_DEFAULT.getCode());
        mallGoods.setUpdateTime(System.currentTimeMillis() / 1000);

        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.in("id", setValidate.getIds());
        queryWrapper.eq("delete_time", 0);

        mallGoodsMapper.update(mallGoods, queryWrapper);

        iMallGoodsDiscountService.delGoodsArraySet(setValidate.getIds());
    }

    @Override
    public MallGoodsDiscountDetailVo discountDetail(Integer id) {
        MallGoodsDetailVo detail = this.detail(id);
        MallGoodsDiscountDetailVo vo = new MallGoodsDiscountDetailVo();
        BeanUtils.copyProperties(detail, vo);
        vo.setUserLevel(iUserLevelService.listsAForGoodsDiscountDetail(detail));
        return vo;
    }

    @Override
    @Transactional
    public void discountSave(MallShopGoodsDiscountSaveValidate setValidate) {
        MallGoods goods = this.getById(setValidate.getId());
        Assert.notNull(goods, "找不到该商品");

        //删除旧的配置
        iMallGoodsDiscountService.delGoodsSet(goods.getId());

        goods.setIsDiscount(setValidate.getIsDiscount());
        goods.setDiscountType(setValidate.getDiscountType());
        goods.setUpdateTime(System.currentTimeMillis() / 1000);
        mallGoodsMapper.updateById(goods);

        if (setValidate.getUserLevel().size() > 0) {
            for (DiscountUserListedVo userLevel : setValidate.getUserLevel()) {
                for (MallGoodsUserLevelDiscountVo goodsDiscount : userLevel.getDiscountList()){
                    MallGoodsDiscount discount = new MallGoodsDiscount();
                    discount.setGoodsId(setValidate.getId());
                    discount.setGoodsSkuId(goodsDiscount.getGoodsSkuId());
                    discount.setUserLevelId(userLevel.getId());
                    discount.setAloneDiscount(setValidate.getDiscountType().equals(GoodsEnum.DISCOUNT_TYPE_ALONE_DISCOUNT.getCode()) == true ? StringUtils.isNotNull(goodsDiscount.getAloneDiscount()) ? goodsDiscount.getAloneDiscount()  : new BigDecimal(0) : new BigDecimal(0));
                    discount.setAlonePrice(setValidate.getDiscountType().equals(GoodsEnum.DISCOUNT_TYPE_ALONE_PRICE.getCode()) == true ? StringUtils.isNotNull(goodsDiscount.getAlonePrice()) ? goodsDiscount.getAlonePrice()  : new BigDecimal(0) : new BigDecimal(0));
                    discount.setCreateTime(System.currentTimeMillis() / 1000);
                    discount.setUpdateTime(System.currentTimeMillis() / 1000);
                    mallGoodsDiscountMapper.insert(discount);
                }
            }
        }
    }

    @Override
    @Transactional
    public void distributionSet(MallShopGoodsDistributionSetValidate setValidate) {
        MallGoods mallGoods = new MallGoods();
        mallGoods.setIsDistribution(setValidate.getIsDistribution());
        mallGoods.setDistributionType(GoodsEnum.DISTRIBUTION_TYPE_DEFAULT.getCode());
        mallGoods.setUpdateTime(System.currentTimeMillis() / 1000);
        mallGoodsMapper.update(mallGoods, new QueryWrapper<MallGoods>().in("id", setValidate.getIds()));

        iMallGoodsDistributionService.delGoodsArraySet(setValidate.getIds());
    }

    @Override
    public MallGoodsDistributionDetailVo distributionDetail(Integer id) {
        MallGoods model = mallGoodsMapper.selectOne(
                new QueryWrapper<MallGoods>()
                        .eq("id", id)
                        .last("limit 1"));

        Assert.notNull(model, "数据不存在");

        MallGoodsDistributionDetailVo vo = new MallGoodsDistributionDetailVo();
        vo.setCode(model.getCode());
        vo.setName(model.getName());
        vo.setId(model.getId());
        vo.setImage(model.getImage());
        vo.setIsDistribution(model.getIsDistribution());
        vo.setDistributionType(model.getDistributionType());

        List<MallGoodsSku> mallGoodsSkus = goodsSkuMapper.selectList(new QueryWrapper<MallGoodsSku>().eq("delete_time", 0).eq("shop_id", model.getShopId()).eq("goods_id", model.getId()));

        List<MallGoodsDistributionSkuVo> mallGoodsSkuVos = new ArrayList<>();
        mallGoodsSkus.forEach(item-> {
            MallGoodsDistributionSkuVo skuVo = new MallGoodsDistributionSkuVo();
            BeanUtils.copyProperties(item, skuVo);
            skuVo.setLinePrice(item.getLinePrice().toString());
            skuVo.setMarketPrice(item.getMarketPrice().toString());
            skuVo.setPrice(item.getPrice().toString());
            skuVo.setSkuValueArr(JSONArray.parse(item.getSkuValueArr()));
            skuVo.setSkuValueIds(JSONArray.parse(item.getSkuValueIds()));
            skuVo.setWeight(item.getWeight().toString());
            skuVo.setVolume(item.getVolume().toString());
            skuVo.setImage(UrlUtils.toAdminAbsoluteUrl(item.getImage()));
            mallGoodsSkuVos.add(skuVo);
        });
        vo.setSku(mallGoodsSkuVos);

        vo.setImage(UrlUtils.toAdminAbsoluteUrl(model.getImage()));

        List<DistributionLevel> levels = distributionLevelMapper.selectList(new QueryWrapper<DistributionLevel>().eq("delete_time", 0).orderByAsc("level"));

        List<DistributionLevelSimpleVo> distributionLevelSimpleVos = new ArrayList<>();
        levels.forEach(item -> {
            DistributionLevelSimpleVo distributionLevelSimpleVo = new DistributionLevelSimpleVo();
            distributionLevelSimpleVo.setId(item.getId());
            distributionLevelSimpleVo.setLevel(item.getLevel());
            distributionLevelSimpleVo.setDescription(item.getDescription());
            distributionLevelSimpleVo.setName(item.getName());
            distributionLevelSimpleVo.setOneRatio(item.getOneRatio() != null ? item.getOneRatio().toString() : "");
            distributionLevelSimpleVo.setIsDefault(item.getIsDefault());
            distributionLevelSimpleVo.setSelfRatio(item.getSelfRatio() != null ? item.getSelfRatio().toString() : "");
            distributionLevelSimpleVo.setTwoRatio(item.getTwoRatio() != null ? item.getTwoRatio().toString() : "");
            distributionLevelSimpleVos.add(distributionLevelSimpleVo);
            List<MallGoodsDistribution> mallGoodsDistributionList = mallGoodsDistributionMapper.selectList(new QueryWrapper<MallGoodsDistribution>().eq("delete_time", 0).eq("goods_id", model.getId()).eq("distribution_level_id", item.getId()));
            List<MallGoodsDistributionSimpleVo> simpleVos = new ArrayList<>();
            if (mallGoodsDistributionList != null && mallGoodsDistributionList.size() > 0) {
                mallGoodsDistributionList.forEach(tempItem -> {

                    MallGoodsDistributionSimpleVo simpleVo = new MallGoodsDistributionSimpleVo();
                    simpleVo.setGoodsId(tempItem.getGoodsId());
                    simpleVo.setGoodsSkuId(tempItem.getGoodsSkuId());
                    simpleVo.setDistributionLevelId(tempItem.getDistributionLevelId());
                    simpleVo.setAloneSelfRatio(tempItem.getAloneSelfRatio() != null ? tempItem.getAloneSelfRatio().toString() : "");
                    simpleVo.setAloneSelfMoney(tempItem.getAloneSelfMoney() != null ? tempItem.getAloneSelfMoney().setScale(2, RoundingMode.HALF_UP).toString() : "");
                    simpleVo.setAloneOneRatio(tempItem.getAloneOneRatio() != null ? tempItem.getAloneOneRatio().toString() : "");
                    simpleVo.setAloneOneMoney(tempItem.getAloneOneMoney() != null ? tempItem.getAloneOneMoney().setScale(2, RoundingMode.HALF_UP).toString() : "");
                    simpleVo.setAloneTwoRatio(tempItem.getAloneTwoRatio() != null ? tempItem.getAloneTwoRatio().toString() : "");
                    simpleVo.setAloneTwoMoney(tempItem.getAloneTwoMoney() != null ? tempItem.getAloneTwoMoney().setScale(2, RoundingMode.HALF_UP).toString() : "");
                    simpleVo.setGoodsSkuId(tempItem.getGoodsSkuId());

                    MallGoodsSku sku = goodsSkuMapper.selectOne(new QueryWrapper<MallGoodsSku>().eq("delete_time", 0).eq("id", tempItem.getGoodsSkuId()));

                    if (sku != null) {
                        simpleVo.setSkuValueArr(JSONArray.parse(sku.getSkuValueArr()));
                        simpleVo.setPrice(sku.getPrice() != null ? sku.getPrice().toString() : "0");
                    }

                    simpleVos.add(simpleVo);

                });
            } else {
                MallGoodsDistributionSimpleVo simpleVo = new MallGoodsDistributionSimpleVo();

                mallGoodsSkus.forEach(sku-> {
                    simpleVo.setGoodsId(model.getId());
                    simpleVo.setGoodsSkuId(sku.getId());
                    simpleVo.setAloneOneRatio("");
                    simpleVo.setAloneOneMoney("");
                    simpleVo.setDistributionLevelId(item.getId());
                    simpleVo.setAloneSelfRatio("");
                    simpleVo.setAloneSelfMoney("");
                    simpleVo.setAloneTwoRatio("");
                    simpleVo.setAloneTwoMoney("");
                    simpleVo.setSkuValueArr(JSONArray.parse(sku.getSkuValueArr()));
                    simpleVo.setPrice(sku.getPrice() != null ? sku.getPrice().toString() : "0");
                    simpleVos.add(simpleVo);
                });



            }
            distributionLevelSimpleVo.setDistributionList(simpleVos);

        });

        vo.setDistributionLevel(distributionLevelSimpleVos);

        return vo;
    }

    @Override
    @Transactional
    public void distributionSave(MallShopGoodsDistributionSaveValidate setValidate) {
        iMallGoodsDistributionService.delGoodsSet(setValidate.getId());

        MallGoods mallGoods = new MallGoods();
        mallGoods.setIsDistribution(setValidate.getIsDistribution());
        mallGoods.setDistributionType(setValidate.getDistributionType());
        mallGoods.setUpdateTime(System.currentTimeMillis() / 1000);
        mallGoodsMapper.update(mallGoods, new QueryWrapper<MallGoods>().eq("id", setValidate.getId()));

        if (setValidate.getDistributionLevel().size() > 0) {
            if (setValidate.getDistributionLevel().size() > 0) {
                for (DistributionLevelSimpleVo distributionLevel : setValidate.getDistributionLevel()) {
                    for (MallGoodsDistributionSimpleVo goodsDistribution : distributionLevel.getDistributionList()) {
                        MallGoodsDistribution temp = new MallGoodsDistribution();
                        MallGoodsSku skus = iMallGoodsSkuService.getById(goodsDistribution.getGoodsSkuId());
                        if (StringUtils.isNull(skus)) {
                            throw new OperateException("折扣信息商品错误");
                        }
                        temp.setGoodsId(setValidate.getId());
                        temp.setGoodsSkuId(goodsDistribution.getGoodsSkuId());
                        temp.setDistributionLevelId(distributionLevel.getId());
                        temp.setAloneSelfRatio(new BigDecimal(0));
                        temp.setAloneOneRatio(new BigDecimal(0));
                        temp.setAloneTwoRatio(new BigDecimal(0));
                        temp.setAloneSelfMoney(new BigDecimal(0));
                        temp.setAloneOneMoney(new BigDecimal(0));
                        temp.setAloneTwoMoney(new BigDecimal(0));
                        temp.setCreateTime(System.currentTimeMillis() / 1000);
                        temp.setUpdateTime(System.currentTimeMillis() / 1000);

                        if (setValidate.getDistributionType().equals(GoodsEnum.DISCOUNT_TYPE_ALONE_DISCOUNT.getCode())) {
                            BigDecimal aloneSelfRatio = new BigDecimal(goodsDistribution.getAloneSelfRatio());
                            BigDecimal aloneOneRatio = new BigDecimal(goodsDistribution.getAloneOneRatio());
                            BigDecimal aloneTwoRatio = new BigDecimal(goodsDistribution.getAloneTwoRatio());
                            if (aloneSelfRatio.add(aloneOneRatio).add(aloneTwoRatio).compareTo(new BigDecimal(100)) > 0) {
                                throw new OperateException("单独佣金比例之和不能超过100");
                            }
                            temp.setAloneSelfRatio(aloneSelfRatio);
                            temp.setAloneOneRatio(aloneOneRatio);
                            temp.setAloneTwoRatio(aloneTwoRatio);
                        }

                        if (setValidate.getDistributionType().equals(GoodsEnum.DISCOUNT_TYPE_ALONE_PRICE.getCode())) {
                            BigDecimal aloneSelfMoney = new BigDecimal(goodsDistribution.getAloneSelfMoney());
                            BigDecimal aloneOneMoney = new BigDecimal(goodsDistribution.getAloneOneMoney());
                            BigDecimal aloneTwoMoney = new BigDecimal(goodsDistribution.getAloneTwoMoney());

                            if (aloneSelfMoney.add(aloneOneMoney).add(aloneTwoMoney).compareTo(skus.getPrice()) > 0) {
                                throw new OperateException("单独佣金 佣金之和不能超过售卖价格");
                            }
                            temp.setAloneSelfMoney(aloneSelfMoney);
                            temp.setAloneOneMoney(aloneOneMoney);
                            temp.setAloneTwoMoney(aloneTwoMoney);
                        }
                        mallGoodsDistributionMapper.insert(temp);
                    }
                }
            } else {
                throw new OperateException("佣金信息错误");
            }
        } else {
            throw new OperateException("等级信息错误");
        }
        //mallGoodsDistributionMapper.insert();
    }


    /**
     * 可审核
     *
     * @param mallGoods
     * @return
     */
    private Integer getCanAuditAttr(MallGoods mallGoods) {
        return mallGoods.getAuditStatus().equals(AuditEnum.WAIT.getCode()) ? 1 : 0;
    }

    /**
     * 可违规重审
     *
     * @param mallGoods
     * @return
     */
    private Integer getCanReAuditAttr(MallGoods mallGoods) {
        return mallGoods.getAuditStatus().equals(AuditEnum.PASS.getCode()) ? 1 : 0;
    }

    /**
     * 可删除
     *
     * @param mallGoods
     * @return
     */
    private Integer getCanDeleteAttr(MallGoods mallGoods) {
        return 1;
    }

    /**
     * 可销售
     */
    private Integer getCanSaleAttr(MallGoods mallGoods) {
        return mallGoods.getStatus().equals(GoodsEnum.GOODS_STATUS_UP.getCode()) && mallGoods.getAuditStatus().equals(AuditEnum.PASS.getCode()) && mallGoods.getShopId().equals(MerchantThreadLocal.getShopId()) ? 1 : 0;
    }

    /**
     * 运费模板类型文本
     *
     * @param mallGoods
     * @return
     */
    private String getExpressTypeTextAttr(MallGoods mallGoods) {
        return GoodsEnum.getGoodsExpressTypeTextArr(mallGoods.getExpressType());
    }

    private List<Integer> getGoodsSendModeAttr(MallGoods mallGoods) {
        List<Integer> arr = new ArrayList<Integer>();
        if (mallGoods.getIsExpress().equals(1)) {
            arr.add(DeliveryEnum.DELIVERY_TYPE_EXPRESS.getCode());
        }
        return arr;
    }

    /**
     * 配送模式文本
     *
     * @param mallGoods
     * @return
     */
    private String getGoodsSendModeTextAttr(MallGoods mallGoods) {
        List<String> arr = new ArrayList<String>();
        if (mallGoods.getIsExpress().equals(1)) {
            arr.add(ConfigUtils.get("mall_delivery_type", "express_name", "快递发货"));
        }
        return StringUtils.join(arr, ",");
    }

    /**
     * price_text 价格
     *
     * @param mallGoods
     * @return
     */
    private String getPriceTextAttr(MallGoods mallGoods) {
        Integer skuType = mallGoods.getSkuType();
        if (skuType.equals(GoodsEnum.SKU_TYPE_SINGLE.getCode())) {
            return mallGoods.getMinPrice().toString();
        }
        if (skuType.equals(GoodsEnum.SKU_TYPE_MORE.getCode())) {
            if (mallGoods.getMinPrice().compareTo(mallGoods.getMaxPrice()) == 0) {
                return mallGoods.getMinPrice().toString();
            } else {
                return mallGoods.getMinPrice().toString() + " ~ " + mallGoods.getMaxPrice().toString();
            }
        }
        return "";
    }

    /**
     * 规格类型
     *
     * @param mallGoods
     * @return
     */
    private String getSkuTypeTextAttr(MallGoods mallGoods) {
        return GoodsEnum.getSkuTypeTextArr(mallGoods.getSkuType());
    }

    /**
     * 状态
     *
     * @param mallGoods
     * @return
     */
    private String getStatusTextAttr(MallGoods mallGoods) {
        return GoodsEnum.getGoodsStatusTextArr(mallGoods.getStatus());
    }

    /**
     * 视频来源
     *
     * @return
     */
    private String getVideoFromTextAttr(MallGoods mallGoods) {
        return GoodsEnum.getVideoFromTextArr(mallGoods.getVideoFrom());
    }

    private QueryWrapper<MallGoods> searchTypeAttr(QueryWrapper<MallGoods> queryWrapper, String type) {
        QueryWrapper<MallGoods> cpQueryWrapper = queryWrapper;
        // 销售中
        if (type.equals("sale")) {
            cpQueryWrapper.eq("audit_status", AuditEnum.PASS.getCode());
            cpQueryWrapper.eq("status", GoodsEnum.GOODS_STATUS_UP.getCode());
        }

        // 仓库中
        if (type.equals("not_sale")) {
            cpQueryWrapper.eq("status", GoodsEnum.GOODS_STATUS_DOWN.getCode());
        }
        // 待审核
        if (type.equals("audit")) {
            cpQueryWrapper.eq("audit_status", AuditEnum.WAIT.getCode());
        }
        // 审核未通过
        if (type.equals("audit_refuse")) {
            cpQueryWrapper.eq("audit_status", AuditEnum.FAIL.getCode());
        }
        // 库存预警
        if (type.equals("stock_warning")) {
            cpQueryWrapper.eq("status", GoodsEnum.GOODS_STATUS_UP.getCode());
            cpQueryWrapper.gt("stock_warning", 0);
            cpQueryWrapper.gt("total_stock", 0);
            cpQueryWrapper.apply("`total_stock` < `stock_warning`");

        }
        return cpQueryWrapper;
    }

    /**
     * 检查商品数据
     *
     * @param goodsCreateValidate 参数
     * @author Amos
     */
    private void __checkAddGoodsData(MallGoodsCreateValidate goodsCreateValidate) {
        // 验证商品名称
        MallGoods goodsName = mallGoodsMapper.selectOne(
                new QueryWrapper<MallGoods>()
                        .eq("delete_time", 0)
                        .eq("shop_id", goodsCreateValidate.getShopId())
                        .eq("name", goodsCreateValidate.getName()).last("limit 1"));

        Assert.isNull(goodsName, "商品名称已存在");

        // 验证商品编码
        MallGoods goodsCode = mallGoodsMapper.selectOne(
                new QueryWrapper<MallGoods>()
                        .eq("delete_time", 0)
                        .eq("shop_id", goodsCreateValidate.getShopId())
                        .eq("code", goodsCreateValidate.getCode()).last("limit 1"));

        Assert.isNull(goodsCode, "商品编码已存在");

        // 验证商品分类
        List<MallGoodsCategory> goodsCategoryList = mallGoodsCategoryMapper.selectBatchIds(goodsCreateValidate.getCategoryIds());
        if (goodsCategoryList.size() != goodsCreateValidate.getCategoryIds().size()) {
            throw new OperateException("平台分类信息错误，请刷新分类");
        }

        MallShopGoodsCategory shopGoodsCategory = mallShopGoodsCategoryMapper.selectOne(new QueryWrapper<MallShopGoodsCategory>().eq("id", goodsCreateValidate.getShopCategoryId()).eq("delete_time", 0).eq("shop_id", goodsCreateValidate.getShopId()));

        if (StringUtils.isNull(shopGoodsCategory)) {
            throw new OperateException("商家分类信息错误，请刷新分类");
        }

        // 验证商品SKU
        List<MallGoodsSkuVo> serviceSpecValueList = goodsCreateValidate.getSku();
        for (MallGoodsSkuVo goodsSkuVo : serviceSpecValueList) {
            if (StringUtils.isNull(goodsSkuVo.getPrice()) || goodsSkuVo.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new OperateException("销售价格不能少于0");
            }
            if (StringUtils.isNull(goodsSkuVo.getStock()) || goodsSkuVo.getStock() < 0) {
                throw new OperateException("库存数不能少于0");
            }
        }
    }

    /**
     * 处理商品规格(创建)
     *
     * @param goodsCreateValidate 参数
     * @return List<GoodsSkuVo>
     * @author Amos
     */
    private List<MallGoodsSkuVo> __cartesianSpec(MallGoodsCreateValidate goodsCreateValidate) {
        List<SkuNameListVo> specValue = goodsCreateValidate.getSkuNameList();
        List<MallGoodsSkuVo> specValueList = goodsCreateValidate.getSku();
        
        // 如果只有一个SKU，直接返回
        if (specValueList.size() == 1) {
            return specValueList;
        }
        
        // 构建规格值的笛卡尔乘积
        List<List<String>> specLists = new ArrayList<>();
        
        // 从sku_name_list中提取所有规格值
        for (SkuNameListVo specValueVo : specValue) {
            List<SpecListVo> specList = specValueVo.getValue();
            List<String> specName = new ArrayList<>();
            for (SpecListVo specListVo : specList) {
                specName.add(specListVo.getValue());
            }
            specLists.add(specName);
        }
        
        // 生成笛卡尔乘积
        List<List<String>> cartesianProduct = generateCartesianProduct(specLists);
        
        // 修正SKU数据
        List<MallGoodsSkuVo> correctedSkuList = new ArrayList<>();
        
        for (int i = 0; i < cartesianProduct.size() && i < specValueList.size(); i++) {
            MallGoodsSkuVo sku = specValueList.get(i);
            List<String> combination = cartesianProduct.get(i);
            
            // 修正sku_value_arr为正确的规格值组合
            sku.setSkuValueArr(combination);
            
            // 生成ids（用于前端识别）
            StringBuilder idsBuilder = new StringBuilder();
            for (int j = 0; j < combination.size(); j++) {
                if (j > 0) idsBuilder.append(",");
                // 找到当前规格值在对应规格维度中的索引
                String value = combination.get(j);
                List<String> specDimension = specLists.get(j);
                int index = specDimension.indexOf(value);
                idsBuilder.append(index);
            }
            sku.setIds(idsBuilder.toString());
            
            correctedSkuList.add(sku);
        }
        
        // 如果前端传入的SKU数量不足，补充缺失的组合
        for (int i = specValueList.size(); i < cartesianProduct.size(); i++) {
            List<String> combination = cartesianProduct.get(i);
            
            // 创建新的SKU，使用第一个SKU的默认值
            MallGoodsSkuVo newSku = new MallGoodsSkuVo();
            if (!specValueList.isEmpty()) {
                MallGoodsSkuVo template = specValueList.get(0);
                newSku.setPrice(template.getPrice());
                newSku.setLinePrice(template.getLinePrice());
                newSku.setMarketPrice(template.getMarketPrice());
                newSku.setStock(template.getStock());
                newSku.setVolume(template.getVolume());
                newSku.setWeight(template.getWeight());
                newSku.setCode(template.getCode());
                newSku.setImage(template.getImage());
            }
            
            newSku.setSkuValueArr(combination);
            
            // 生成ids
            StringBuilder idsBuilder = new StringBuilder();
            for (int j = 0; j < combination.size(); j++) {
                if (j > 0) idsBuilder.append(",");
                String value = combination.get(j);
                List<String> specDimension = specLists.get(j);
                int index = specDimension.indexOf(value);
                idsBuilder.append(index);
            }
            newSku.setIds(idsBuilder.toString());
            
            correctedSkuList.add(newSku);
        }
        
        return correctedSkuList;
    }

    /**
     * 生成笛卡尔乘积
     */
    private List<List<String>> generateCartesianProduct(List<List<String>> specLists) {
        if (specLists.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<List<String>> result = new ArrayList<>();
        
        // 初始化第一个维度
        for (String value : specLists.get(0)) {
            List<String> combination = new ArrayList<>();
            combination.add(value);
            result.add(combination);
        }
        
        // 逐个维度进行笛卡尔乘积
        for (int i = 1; i < specLists.size(); i++) {
            List<String> currentDimension = specLists.get(i);
            List<List<String>> newResult = new ArrayList<>();
            
            for (List<String> existingCombination : result) {
                for (String value : currentDimension) {
                    List<String> newCombination = new ArrayList<>(existingCombination);
                    newCombination.add(value);
                    newResult.add(newCombination);
                }
            }
            
            result = newResult;
        }
        
        return result;
    }

    /**
     * 添加商品规格
     *
     * @param goodsId             商品ID
     * @param goodsCreateValidate 参数
     * @return Map<Object>
     * @author Amos
     */
    private Map<String, Object> __addGoodsSpec(Integer goodsId, MallGoodsCreateValidate goodsCreateValidate) {
        Integer specType = goodsCreateValidate.getSkuType();
        Map<String, Object> goodsSkuData = new LinkedHashMap<>();

        for (MallGoodsSkuVo goodSkuVo : goodsCreateValidate.getSku()) {
            if (goodSkuVo.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new OperateException("销售价格不能少于等于0");
            }
            if (goodSkuVo.getStock() < 0) {
                throw new OperateException("库存数量不能少于0");
            }
        }

        //单规格
        if (0 == specType) {
            //规格项
            MallGoodsSku goodsSku = new MallGoodsSku();
            MallGoodsSkuVo cvSku = goodsCreateValidate.getSku().get(0);
            BeanUtils.copyProperties(cvSku, goodsSku);
            goodsSku.setId(null);
            goodsSku.setShopId(goodsCreateValidate.getShopId());
            goodsSku.setGoodsId(goodsId);
            goodsSku.setSkuValueIds(new JSONArray().toJSONString());
            goodsSku.setSkuValueArr(new JSONArray().toJSONString());
            goodsSku.setImage(UrlUtils.toShopRelativeUrl(goodsSku.getImage()));
            goodsSku.setCreateTime(System.currentTimeMillis() / 1000);
            goodsSku.setUpdateTime(System.currentTimeMillis() / 1000);
            goodsSku.setLinePrice(StringUtils.isNull(cvSku.getLinePrice()) ? new BigDecimal(0) : cvSku.getLinePrice());
            goodsSku.setMarketPrice(StringUtils.isNull(cvSku.getMarketPrice()) ? new BigDecimal(0) : cvSku.getMarketPrice());
            goodsSku.setVolume(StringUtils.isNull(cvSku.getVolume()) ? new BigDecimal(0) : cvSku.getVolume());
            goodsSku.setWeight(StringUtils.isNull(cvSku.getWeight()) ? new BigDecimal(0) : cvSku.getWeight());
            goodsSkuMapper.insert(goodsSku);

            goodsSkuData.put("minPrice", goodsSku.getPrice());
            goodsSkuData.put("maxPrice", goodsSku.getPrice());
            goodsSkuData.put("minLineationPrice", goodsSku.getLinePrice());
            goodsSkuData.put("maxLineationPrice", goodsSku.getLinePrice());
            goodsSkuData.put("totalStock", goodsSku.getStock());

        } else {
            List<SkuNameListVo> specValue = goodsCreateValidate.getSkuNameList();
            Map<String, Integer> specListData = new LinkedHashMap<>();
            for (SkuNameListVo specValueVo : specValue) {
                MallGoodsSkuName goodsSkuName = new MallGoodsSkuName();
                goodsSkuName.setGoodsId(goodsId);
                goodsSkuName.setName(specValueVo.getName());
                goodsSkuName.setCreateTime(System.currentTimeMillis() / 1000);
                goodsSkuName.setUpdateTime(System.currentTimeMillis() / 1000);
                goodsSkuNameMapper.insert(goodsSkuName);
                List<SpecListVo> specList = specValueVo.getValue();
                for (SpecListVo specListVo : specList) {
                    MallGoodsSkuValue goodsSkuValue = new MallGoodsSkuValue();
                    goodsSkuValue.setGoodsId(goodsId);
                    goodsSkuValue.setSkuNameId(goodsSkuName.getId());
                    goodsSkuValue.setValue(specListVo.getValue());
                    goodsSkuValue.setCreateTime(System.currentTimeMillis() / 1000);
                    goodsSkuValue.setUpdateTime(System.currentTimeMillis() / 1000);
                    goodsSkuValueMapper.insert(goodsSkuValue);
                    specListData.put(specListVo.getValue(), goodsSkuValue.getId());
                }
            }
            List<MallGoodsSkuVo> serviceSpecValueList = goodsCreateValidate.getSku();

            List<BigDecimal> priceList = new LinkedList<>();
            List<BigDecimal> lineationPrice = new LinkedList<>();
            Integer totalStock = 0;
            for (MallGoodsSkuVo goodsSkuVo : serviceSpecValueList) {
                //String skuValueArr = goodsSkuVo.getSkuValueArr();
                List<String> skuValueArrList = goodsSkuVo.getSkuValueArr();

                JSONArray skuIds = new JSONArray();
                for (String skuName : skuValueArrList) {
                    Integer skuNameId = specListData.get(skuName);
                    if (skuNameId != null) {
                        skuIds.add(skuNameId);
                    }
//                    if (skuIds.toString().equals(""+"")) {
//                        skuIds = new StringBuilder(skuNameId.toString());
//                    } else {
//                        skuIds.append(",").append(skuNameId.toString());
//                    }
                }

                //goodsSkuVo.setSkuValueIds(JSONArray.toJSONString(skuValueArrList));
                goodsSkuVo.setGoodsId(goodsId);
                MallGoodsSku goodsSku = new MallGoodsSku();
                BeanUtils.copyProperties(goodsSkuVo, goodsSku);
                goodsSku.setId(null);
                goodsSku.setImage(UrlUtils.toShopRelativeUrl(goodsSkuVo.getImage()));
                goodsSku.setShopId(goodsCreateValidate.getShopId());
                goodsSku.setSkuValueArr(JSONArray.toJSONString(goodsSkuVo.getSkuValueArr()));
                goodsSku.setLinePrice(StringUtils.isNull(goodsSkuVo.getLinePrice()) ? new BigDecimal(0) : goodsSkuVo.getLinePrice());
                goodsSku.setMarketPrice(StringUtils.isNull(goodsSkuVo.getMarketPrice()) ? new BigDecimal(0) : goodsSkuVo.getMarketPrice());
                goodsSku.setCreateTime(System.currentTimeMillis() / 1000);
                goodsSku.setUpdateTime(System.currentTimeMillis() / 1000);
                goodsSku.setVolume(goodsSkuVo.getVolume());
                goodsSku.setWeight(goodsSkuVo.getWeight());
                goodsSku.setSkuValueIds(JSONArray.toJSONString(skuIds));
                goodsSkuMapper.insert(goodsSku);
                priceList.add(goodsSkuVo.getPrice());
                lineationPrice.add(StringUtils.isNull(goodsSkuVo.getLinePrice()) ? new BigDecimal(0) : goodsSkuVo.getLinePrice());
                totalStock += goodsSkuVo.getStock();
            }
            goodsSkuData.put("minPrice", Collections.min(priceList));
            goodsSkuData.put("maxPrice", Collections.max(priceList));
            goodsSkuData.put("minLineationPrice", Collections.min(lineationPrice));
            goodsSkuData.put("maxLineationPrice", Collections.max(lineationPrice));
            goodsSkuData.put("totalStock", totalStock);
        }
        return goodsSkuData;
    }

    /**
     * get_create_update_audit_status
     * 返回是否需要审核的字段
     */
    private Integer getCreateUpdateAuditStatus(Integer shopId, MallGoods goods) {
        MallShop shop = mallShopService.getById(shopId);
        if (shop.getGoodsAudit().equals(YesNoEnum.NO.getCode())) {
            return AuditEnum.PASS.getCode();
        }

        if (StringUtils.isNull(goods)) {
            return AuditEnum.WAIT.getCode();
        }

        // 已通过 则不需要在审核 否则都需要审核
        if (goods.getAuditStatus().equals(YesNoEnum.YES.getCode())) {
            return AuditEnum.PASS.getCode();
        } else {
            return AuditEnum.WAIT.getCode();
        }
    }

    /**
     * 编辑商品规格
     *
     * @param oldGoodsSpec        旧商品规格
     * @param goodsUpdateValidate 商品参数
     * @author Amos
     */
    private Map<String, Object> __editGoodsSpec(Integer oldGoodsSpec, MallGoodsUpdateValidate goodsUpdateValidate) {
        Map<String, Object> goodsSkuData = new LinkedHashMap<>();
        List<BigDecimal> priceList = new LinkedList<>();
        List<BigDecimal> lineationPrice = new LinkedList<>();
        Integer totalStock = 0;

        for (MallGoodsSkuVo goodSkuVo : goodsUpdateValidate.getSku()) {
            if (goodSkuVo.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new OperateException("销售价格不能少于等于0");
            }

            if (ObjectUtils.isEmpty(goodSkuVo.getLinePrice())) {
                goodSkuVo.setLinePrice(BigDecimal.ZERO);
            } else {
                if (goodSkuVo.getLinePrice().compareTo(BigDecimal.ZERO) < 0) {
                    throw new OperateException("划线价格不能少于0");
                }
            }

            if (ObjectUtils.isEmpty(goodSkuVo.getMarketPrice())) {
                goodSkuVo.setMarketPrice(BigDecimal.ZERO);
            } else {
                if (goodSkuVo.getMarketPrice().compareTo(BigDecimal.ZERO) < 0) {
                    throw new OperateException("市场价格不能少于0");
                }
            }

            if (ObjectUtils.isEmpty(goodSkuVo.getStock()) || goodSkuVo.getStock() < 0) {
                throw new OperateException("库存数量不能少于0");
            }

            if (ObjectUtils.isEmpty(goodSkuVo.getWeight())) {
                goodSkuVo.setWeight(new BigDecimal(0));
            } else {
                if (goodSkuVo.getWeight().compareTo(BigDecimal.ZERO) < 0) {
                    throw new OperateException("重量值不能少于0");
                }
            }

            if (ObjectUtils.isEmpty(goodSkuVo.getVolume())) {
                goodSkuVo.setVolume(new BigDecimal(0));
            } else {
                if (goodSkuVo.getVolume().compareTo(BigDecimal.ZERO) < 0) {
                    throw new OperateException("体积值不能少于0");
                }
            }

            if (ObjectUtils.isEmpty(goodSkuVo.getCode())) {
                goodSkuVo.setCode("");
            }
        }

        // 单规格数据保存
        if (0 == goodsUpdateValidate.getSkuType()) {
            if (0 == oldGoodsSpec) {
                // 单规格 =》单规格
                MallGoodsSku goodsSku = goodsSkuMapper.selectOne(new QueryWrapper<MallGoodsSku>().eq("goods_id", goodsUpdateValidate.getId()).eq("delete_time", 0));
                MallGoodsSkuVo cvSku = goodsUpdateValidate.getSku().get(0);
                BeanUtils.copyProperties(cvSku, goodsSku);
                goodsSku.setGoodsId(goodsUpdateValidate.getId());
                //goodsSku.setSkuValueArr("默认");
                goodsSku.setSkuValueArr(new JSONArray().toJSONString());
                goodsSku.setImage(UrlUtils.toShopRelativeUrl(cvSku.getImage()));
                goodsSku.setVolume(cvSku.getVolume());
                goodsSku.setWeight(cvSku.getWeight());
                goodsSkuMapper.updateById(goodsSku);

                priceList.add(goodsSku.getPrice());
                lineationPrice.add(goodsSku.getLinePrice());
                totalStock = goodsSku.getStock();
            } else {
                // 多规格 =》单规格
                Map<String, Object> whereMap = new LinkedHashMap<>();
                whereMap.put("goods_id", goodsUpdateValidate.getId());
                goodsSkuNameMapper.deleteByMap(whereMap);
                goodsSkuValueMapper.deleteByMap(whereMap);

                MallGoodsSku sku = new MallGoodsSku();
                sku.setDeleteTime(System.currentTimeMillis() / 1000);
                goodsSkuMapper.update(sku, new QueryWrapper<MallGoodsSku>().eq("goods_id", goodsUpdateValidate.getId()));
                // 规格项
                MallGoodsSku goodsSku = new MallGoodsSku();
                MallGoodsSkuVo cvSku = goodsUpdateValidate.getSku().get(0);
                BeanUtils.copyProperties(cvSku, goodsSku);
                goodsSku.setGoodsId(goodsUpdateValidate.getId());
                //goodsSku.setSkuValueIds(goodsSkuValue.getId().toString());
                goodsSku.setShopId(goodsUpdateValidate.getShopId());
                goodsSku.setSkuValueIds(new JSONArray().toJSONString());
                goodsSku.setSkuValueArr(new JSONArray().toJSONString());
                goodsSku.setImage(UrlUtils.toShopRelativeUrl(goodsSku.getImage()));
                goodsSku.setCreateTime(System.currentTimeMillis() / 1000);
                goodsSku.setUpdateTime(System.currentTimeMillis() / 1000);
                goodsSku.setLinePrice(StringUtils.isNull(cvSku.getLinePrice()) ? new BigDecimal(0) : cvSku.getLinePrice());
                goodsSku.setMarketPrice(StringUtils.isNull(cvSku.getMarketPrice()) ? new BigDecimal(0) : cvSku.getMarketPrice());
                goodsSku.setVolume(cvSku.getVolume());
                goodsSku.setWeight(cvSku.getWeight());
                goodsSkuMapper.insert(goodsSku);

                priceList.add(goodsSku.getPrice());
                lineationPrice.add(goodsSku.getLinePrice());
                totalStock = goodsSku.getStock();
            }
        }

        // 多规格数据保存
        if (0 != goodsUpdateValidate.getSkuType()) {
            // 多规格 => 多规格
            Map<String, Object> whereMap = new LinkedHashMap<>();
            whereMap.put("goods_id", goodsUpdateValidate.getId());
            goodsSkuNameMapper.deleteByMap(whereMap);
            goodsSkuValueMapper.deleteByMap(whereMap);
            //goodsSkuMapper.deleteByMap(whereMap);
            MallGoodsSku sku = new MallGoodsSku();
            sku.setDeleteTime(System.currentTimeMillis() / 1000);
            goodsSkuMapper.update(sku, new QueryWrapper<MallGoodsSku>().eq("goods_id", goodsUpdateValidate.getId()));
            goodsUpdateValidate.setSku(this.__cartesianSpec(goodsUpdateValidate));
            // 添加商品规格
            List<SkuNameListVo> specValue = goodsUpdateValidate.getSkuNameList();
            Map<String, Integer> specListData = new LinkedHashMap<>();
            for (SkuNameListVo specValueVo : specValue) {
                MallGoodsSkuName goodsSkuName = new MallGoodsSkuName();
                goodsSkuName.setGoodsId(goodsUpdateValidate.getId());
                goodsSkuName.setName(specValueVo.getName());
                goodsSkuName.setCreateTime(System.currentTimeMillis() / 1000);
                goodsSkuName.setUpdateTime(System.currentTimeMillis() / 1000);
                goodsSkuNameMapper.insert(goodsSkuName);
                List<SpecListVo> specList = specValueVo.getValue();
                for (SpecListVo specListVo : specList) {
                    MallGoodsSkuValue goodsSkuValue = new MallGoodsSkuValue();
                    goodsSkuValue.setGoodsId(goodsUpdateValidate.getId());
                    goodsSkuValue.setSkuNameId(goodsSkuName.getId());
                    goodsSkuValue.setValue(specListVo.getValue());
                    goodsSkuValue.setCreateTime(System.currentTimeMillis() / 1000);
                    goodsSkuValue.setUpdateTime(System.currentTimeMillis() / 1000);
                    goodsSkuValueMapper.insert(goodsSkuValue);
                    specListData.put(specListVo.getValue(), goodsSkuValue.getId());
                }
            }
            List<MallGoodsSkuVo> serviceSpecValueList = goodsUpdateValidate.getSku();

            for (MallGoodsSkuVo goodsSkuVo : serviceSpecValueList) {
                //String skuValueArr = goodsSkuVo.getSkuValueArr();
                List<String> skuValueArrList = goodsSkuVo.getSkuValueArr();
                JSONArray skuIds = new JSONArray();
                for (String skuName : skuValueArrList) {
                    Integer skuNameId = specListData.get(skuName);
                    skuIds.add(skuNameId.toString());
                }
                goodsSkuVo.setGoodsId(goodsUpdateValidate.getId());
                MallGoodsSku goodsSku = new MallGoodsSku();
                BeanUtils.copyProperties(goodsSkuVo, goodsSku);
                goodsSku.setId(null);
                goodsSku.setImage(UrlUtils.toShopRelativeUrl(goodsSkuVo.getImage()));
                goodsSku.setShopId(goodsUpdateValidate.getShopId());
                goodsSku.setSkuValueArr(JSONArray.toJSONString(goodsSkuVo.getSkuValueArr()));
                goodsSku.setLinePrice(StringUtils.isNull(goodsSkuVo.getLinePrice()) ? new BigDecimal(0) : goodsSkuVo.getLinePrice());
                goodsSku.setMarketPrice(StringUtils.isNull(goodsSkuVo.getMarketPrice()) ? new BigDecimal(0) : goodsSkuVo.getMarketPrice());
                goodsSku.setCreateTime(System.currentTimeMillis() / 1000);
                goodsSku.setUpdateTime(System.currentTimeMillis() / 1000);
                goodsSku.setVolume(goodsSkuVo.getVolume());
                goodsSku.setWeight(goodsSkuVo.getWeight());
                goodsSku.setSkuValueIds(JSONArray.toJSONString(skuIds));
                goodsSkuMapper.insert(goodsSku);
                priceList.add(goodsSkuVo.getPrice());
                lineationPrice.add(StringUtils.isNull(goodsSkuVo.getLinePrice()) ? new BigDecimal(0) : goodsSkuVo.getLinePrice());
                totalStock += goodsSkuVo.getStock();
            }
            goodsSkuData.put("minPrice", Collections.min(priceList));
            goodsSkuData.put("maxPrice", Collections.max(priceList));
            goodsSkuData.put("minLineationPrice", Collections.min(lineationPrice));
            goodsSkuData.put("maxLineationPrice", Collections.max(lineationPrice));
            goodsSkuData.put("totalStock", totalStock);
        }

        goodsSkuData.put("maxPrice", Collections.max(priceList));
        goodsSkuData.put("minPrice", Collections.min(priceList));
        goodsSkuData.put("minLineationPrice", Collections.min(lineationPrice));
        goodsSkuData.put("maxLineationPrice", Collections.max(lineationPrice));
        goodsSkuData.put("totalStock", totalStock);
        return goodsSkuData;
    }

    /**
     * 处理商品规格(更新)
     *
     * @param goodsUpdateValidate 参数
     * @return List<GoodsSkuVo>
     * @author Amos
     */
    public List<MallGoodsSkuVo> __cartesianSpec(MallGoodsUpdateValidate goodsUpdateValidate) {
        List<SkuNameListVo> specValue = goodsUpdateValidate.getSkuNameList();
        List<List<String>> specLists = new ArrayList<>();
        List<MallGoodsSkuVo> specValueList = goodsUpdateValidate.getSku();
        if (specValueList.size() == 1) {
            return specValueList;
        }

        return specValueList;
    }

    /**
     * 商品是否可以编辑
     *
     * @param goodsId
     * @param type    业务类型，edit, delete
     * @return
     */
    private void checkGoods(Integer goodsId, String type) {
        MallGoods goods = mallGoodsMapper.selectById(goodsId);
        String action = type.equals("edit") ? "编辑" : "删除";
        if (StringUtils.isNull(goods)) {
            throw new OperateException("找不到该商品无法" + action);
        }
        // 检测秒杀活动
        Boolean msGoodsHas = iMsService.goodsHas(goodsId, null);
        if (msGoodsHas.equals(true)) {
            throw new OperateException("商品已参与秒杀活动，不能" + action);
        }

        // 拼团活动
        Boolean ptGoodsHas = iPtService.goodsHas(goodsId, null);
        if (ptGoodsHas.equals(true)) {
            throw new OperateException("商品已参与拼团活动，不能" + action);
        }
    }
}
