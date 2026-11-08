package com.houjiang.merchant.service.impl.mall;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallGather;
import com.houjiang.merchant.common.entity.mall.MallGatherGoods;
import com.houjiang.merchant.common.entity.mall.MallShop;
import com.houjiang.merchant.common.entity.mall.MallShopAccountLog;
import com.houjiang.merchant.common.enums.DeliveryEnum;
import com.houjiang.merchant.common.enums.GatherEnum;
import com.houjiang.merchant.common.enums.GoodsEnum;
import com.houjiang.merchant.common.enums.ShopAccountEnum;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.mall.MallGatherGoodsMapper;
import com.houjiang.merchant.common.mapper.mall.MallGatherMapper;
import com.houjiang.merchant.common.mapper.mall.MallShopMapper;
import com.houjiang.merchant.common.util.*;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.*;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.gather.MallGatherCreateValidate;
import com.houjiang.merchant.validate.gather.MallGatherSearchValidate;
import com.houjiang.merchant.vo.gather.MallGatherCreateVo;
import com.houjiang.merchant.vo.gather.MallGatherDetailVo;
import com.houjiang.merchant.vo.gather.MallGatherListedVo;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import com.houjiang.merchant.common.validation.Assert;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
 * 商品采集实现类
 *
 * @author Amos
 */
@Service
public class MallGatherServiceImpl implements IMallGatherService {

    @Resource
    MallGatherMapper mallGatherMapper;
    @Resource
    MallShopMapper mallShopMapper;

    @Resource
    MallGatherGoodsMapper mallGatherGoodsMapper;
    @Resource
    IMallShopCategoryService iMallShopCategoryService;
    @Resource
    IMallGoodsCategoryService iMallGoodsCategoryService;
    @Resource
    IMallGatherGoodsService iMallGatherGoodsService;
    @Resource
    IMallShopAccountLogService iMallShopAccountLogService;
    @Resource
    IMallShopService iMallShopService;
    @Resource
    DataSourceTransactionManager transactionManager;
    @Resource
    TransactionDefinition transactionDefinition;


    /**
     * 商品采集列表
     *
     * @param pageValidate   分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<MallShopJobListedVo>
     * @author Amos
     */
    @Override
    public PageResult<MallGatherListedVo> list(PageValidate pageValidate, MallGatherSearchValidate searchValidate) {
        Integer page = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();

        QueryWrapper<MallGather> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("delete_time", 0).orderByDesc(Arrays.asList("id"));

        mallGatherMapper.setSearch(queryWrapper, searchValidate, new String[]{
                "=:shopId@shop_id:int"
        });

        IPage<MallGather> iPage = mallGatherMapper.selectPage(new Page<>(page, limit), queryWrapper);

        List<MallGatherListedVo> list = new LinkedList<>();
        for (MallGather item : iPage.getRecords()) {
            MallGatherListedVo vo = new MallGatherListedVo();
            BeanUtils.copyProperties(item, vo);
            vo.setCreateTime(TimeUtils.timestampToDate(item.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(item.getUpdateTime()));
            vo.setLinks(JSONArray.parseArray(item.getLinks()));
            vo.setShopCategory(iMallShopCategoryService.getById(item.getShopCategoryId()));
            vo.setCategory(iMallGoodsCategoryService.detail(item.getCategoryId()));
            list.add(vo);
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list);
    }

    /**
     * 商品采集详情
     *
     * @param id 主键参数
     * @return MallShopJob
     * @author Amos
     */
    @Override
    public MallGatherDetailVo detail(Integer id) {
        MallGather model = mallGatherMapper.selectOne(
                new QueryWrapper<MallGather>()
                        .eq("id", id).eq("delete_time", 0)
                        .last("limit 1"));

        Assert.notNull(model, "数据不存在");

        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能查看其他店铺的商品采集信息");
        }

        MallGatherDetailVo vo = new MallGatherDetailVo();
        BeanUtils.copyProperties(model, vo);
        vo.setCreateTime(TimeUtils.timestampToDate(model.getCreateTime()));
        vo.setUpdateTime(TimeUtils.timestampToDate(model.getUpdateTime()));
        vo.setLinks(JSONArray.parseArray(model.getLinks()));
        vo.setGoodsDealNo(iMallGatherGoodsService.goodsDealNo(model.getId()));
        vo.setGoodsDealYes(iMallGatherGoodsService.goodsDealYes(model.getId()));
        vo.setProgress(0);

        return vo;
    }

    /**
     * 商品采集新增
     *
     * @param createValidate 参数
     * @author Amos
     */
    @Override
    @Transactional
    public MallGatherCreateVo add(MallGatherCreateValidate createValidate) {
        if (createValidate.getLinks().size() == 0) {
            throw new OperateException("至少提交一条商品链接");
        }
        MallGather model = new MallGather();
        model.setCreateTime(System.currentTimeMillis() / 1000);
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        model.setShopId(createValidate.getShopId());
        model.setLinks(JSONArray.toJSONString(createValidate.getLinks()));
        model.setCategoryId(createValidate.getCategoryId());
        model.setShopCategoryId(createValidate.getShopCategoryId());
        model.setType(createValidate.getType());
        model.setNums(createValidate.getLinks().size());
        mallGatherMapper.insert(model);

        MallGatherCreateVo vo = new MallGatherCreateVo();
        vo.setId(model.getId());
        return vo;
    }

    /**
     * 商品采集删除
     *
     * @param id 主键ID
     * @author Amos
     */
    @Override
    public void del(Integer id) {
        MallGather model = mallGatherMapper.selectOne(
                new QueryWrapper<MallGather>()
                        .eq("id", id).eq("delete_time", 0)
                        .last("limit 1"));

        Assert.notNull(model, "数据不存在!");
        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能删除其他店铺的采集信息");
        }

        model.setDeleteTime(System.currentTimeMillis() / 1000);
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        mallGatherMapper.updateById(model);
    }

    @Override
    public void deal(Integer id) {
        MallGather model = mallGatherMapper.selectOne(
                new QueryWrapper<MallGather>()
                        .eq("id", id).eq("delete_time", 0)
                        .last("limit 1"));

        Assert.notNull(model, "数据不存在");

        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能执行其他店铺的商品采集信息");
        }
        String apikey = ConfigUtils.get("gather", "key", "");
        Map<String, String> gatherPrices = ConfigUtils.get("gather_price");
        Integer gatherMoney = 0;
        Integer itemIndex = 0;

        JSONArray links = JSONArray.parseArray(model.getLinks());
        if (links.size() == 0) {
            throw new OperateException("该商品链接没有内容，无法完成采集，请检查链接是否正确");
        }
        List<MallGatherGoods> insertMallGatherGoods = new ArrayList<>();
        links.forEach(item -> {
            MallGatherGoods gatherGoods = new MallGatherGoods();
            gatherGoods.setShopId(model.getShopId());
            gatherGoods.setGatherId(model.getId());
            gatherGoods.setStatus(0);
            gatherGoods.setDeal(0);
            gatherGoods.setLink(item.toString());
            gatherGoods.setContent("{}");
            JSONObject gather = new JSONObject();
            gather.put("gather", "[]");
            gather.put("fail", "");
            gatherGoods.setGather(JSONObject.toJSONString(gather));
            gatherGoods.setChannel(GatherEnum.CHANNEL_NULL.getCode());
            gatherGoods.setCreateTime(System.currentTimeMillis() / 1000);
            gatherGoods.setUpdateTime(System.currentTimeMillis() / 1000);
            mallGatherGoodsMapper.insert(gatherGoods);
            insertMallGatherGoods.add(gatherGoods);
        });

        for (MallGatherGoods item : insertMallGatherGoods) {
            Boolean isLast = (itemIndex == insertMallGatherGoods.size() - 1);

            // 开启事务
            TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);
            try {
                if (UrlUtils.isValidUrl(item.getLink()) == false) {
                    throw new OperateException("未知采集链接域名");
                }
                Integer channel = 0;
                Integer gatherPrice = 0;
                String queryUrl = "";
                Map<String, String> parse = UrlUtils.parseUrl(item.getLink());
                Map<String, String> pathInfo = UrlUtils.pathInfo(item.getLink());
                if (UrlUtils.isValidUrlAndDomain(item.getLink(), "taobao")) {
                    gatherPrice = Integer.valueOf(gatherPrices.get("tb"));
                    channel = GatherEnum.CHANNEL_TB.getCode();
                    Map<String, String> query = UrlUtils.parseQueryString(parse.get("query").toString());
                    queryUrl = "/taobao/detail?apikey=" + apikey + "&itemid=" + query.getOrDefault("id", "0");
                } else if (UrlUtils.isValidUrlAndDomain(item.getLink(), "tmall")) {
                    gatherPrice = Integer.valueOf(gatherPrices.get("tm"));
                    channel = GatherEnum.CHANNEL_TM.getCode();
                    Map<String, String> query = UrlUtils.parseQueryString(parse.get("query").toString());
                    queryUrl = "/tmall/detail?apikey=" + apikey + "&itemid=" + query.getOrDefault("id", "0");
                } else if (UrlUtils.isValidUrlAndDomain(item.getLink(), "jd")) {
                    gatherPrice = Integer.valueOf(gatherPrices.get("jd"));
                    channel = GatherEnum.CHANNEL_JD.getCode();
                    queryUrl = "/jd/detail?apikey=" + apikey + "&itemid=" + pathInfo.getOrDefault("filename", "0");
                } else if (UrlUtils.isValidUrlAndDomain(item.getLink(), "1688")) {
                    gatherPrice = Integer.valueOf(gatherPrices.get("ali_1688"));
                    channel = GatherEnum.CHANNEL_1688.getCode();
                    queryUrl = "/alibaba/detail?apikey=" + apikey + "&itemid=" + pathInfo.getOrDefault("filename", "0");
                } else {
                    throw new OperateException("未知渠道");
                }

                MallShop mallShop = this.iMallShopService.getById(model.getShopId());
                if (mallShop.getGatherMoney() < gatherPrice) {
                    throw new OperateException("商户采集余额不足");
                }

                gatherMoney = gatherPrice + gatherMoney;

                // 开始采集
                JSONObject httpParams = new JSONObject();
                httpParams.put("timeout", 1000000);
                String content = HttpUtils.sendPost("https://api09.99api.com" + queryUrl, JSONObject.toJSONString(httpParams));
                JSONObject contentJSON = JSONObject.parse(content);

                if (contentJSON == null) {
                    throw new OperateException("采集错误");
                }

                String retCode = contentJSON.getString("retcode");
                String retData = contentJSON.getString("data");
                if (StringUtils.isNull(retCode)) {
                    throw new OperateException("采集错误");
                }
                //都是失败
                if (retCode.equals("0000") == false) {
                    if (retCode.equals("4016") == true) {
                        throw new OperateException("平台接口余额不足");
                    } else {
                        throw new OperateException("采集错误:" + retCode + retData);
                    }
                }
                JSONObject goodsData = this.getGoodsData(content, model, channel);

                item.setStatus(1);
                item.setContent(goodsData.toJSONString());
                JSONObject itemGather = JSONObject.parseObject(item.getGather());
                itemGather.put("gather", content);
                item.setUpdateTime(System.currentTimeMillis() / 1000);
                item.setGather(JSONObject.toJSONString(itemGather));
                item.setChannel(channel);
                mallGatherGoodsMapper.updateById(item);

                model.setSuccess(model.getSuccess() + 1);
                model.setStatus(2);
                model.setGatherMoney(gatherMoney);
                model.setUpdateTime(System.currentTimeMillis() / 1000);
                mallGatherMapper.updateById(model);

                mallShop.setGatherMoney(mallShop.getGatherMoney() - gatherPrice);
                mallShop.setUpdateTime(System.currentTimeMillis() / 1000);
                mallShopMapper.updateById(mallShop);

                // 账户明细记录
                MallShopAccountLog accountLog = new MallShopAccountLog();
                accountLog.setShopId(model.getShopId());
                accountLog.setChangeObject(ShopAccountEnum.CHANGE_OBJECT_SHOP_GATHER_MONEY.getCode());
                accountLog.setChangeType(ShopAccountEnum.SHOP_GATHER_MONEY_GATHER_GOODS.getCode());
                accountLog.setFromId(0);
                accountLog.setFromSn("");
                accountLog.setChangeAmount(new BigDecimal(gatherPrice));
                accountLog.setParams("[]");
                accountLog.setAccountStatus(2);
                iMallShopAccountLogService.createAccountLog(accountLog);


                transactionManager.commit(transactionStatus);
            } catch (OperateException e) {
                transactionManager.rollback(transactionStatus);
                item.setStatus(0);
                JSONObject itemGather = JSONObject.parseObject(item.getGather());
                itemGather.put("fail", e.getMsg());
                item.setUpdateTime(System.currentTimeMillis() / 1000);
                item.setGather(JSONObject.toJSONString(itemGather));
                mallGatherGoodsMapper.updateById(item);

                model.setFailed(model.getFailed() + 1);
                mallGatherMapper.updateById(model);
                if (isLast.equals(true)) {
                    model.setStatus(2);
                    mallGatherMapper.updateById(model);
                }
                throw new OperateException(e.getMsg());
            }
        }
    }

    /**
     * 统一采集格式
     *
     * @param content
     * @param channel
     * @return
     */
    private JSONObject getGoodsData(String content, MallGather gather, Integer channel) {
        JSONObject contentJSON = JSONObject.parse(content);
        JSONObject data = new JSONObject();
        data.put("goods_image", new JSONArray());
        data.put("sku_name_list", new JSONArray());
        data.put("sku", new JSONArray());
        data.put("category_ids", new JSONArray() {{
            add(gather.getCategoryId());
        }});
        data.put("shop_category_id", gather.getShopCategoryId());
        data.put("is_express", 1);
        data.put("express_type", GoodsEnum.GOODS_EXPRESS_TYPE_FREE.getCode());
        data.put("video_is", 0);
        data.put("goods_send_mode", new JSONArray() {{
            add(DeliveryEnum.DELIVERY_TYPE_EXPRESS.getCode());
        }});
        data.put("virtual_sale_num", 0);
        data.put("stock_warning", 0);
        data.put("virtual_visit_num", 0);
        data.put("sort", 0);
        data.put("status", 0);
        data.put("shop_recommend", 0);
        data.put("code", ToolUtils.randomString(8));

        JSONObject contentData = contentJSON.getJSONObject("data");
        if (channel.equals(GatherEnum.CHANNEL_TB.getCode()) || channel.equals(GatherEnum.CHANNEL_TM.getCode())) {
            JSONObject item = contentData.getJSONObject("item");
            JSONArray sku = item.getJSONArray("sku");
            Integer skyCount = sku.size();
            data.put("name", item.getString("title"));
            data.put("descript", "");
            data.put("sku_type", skyCount > 1 ? GoodsEnum.SKU_TYPE_MORE.getCode() : GoodsEnum.SKU_TYPE_SINGLE.getCode());
            data.put("content", item.getString("desc"));
            JSONArray itemImages = item.getJSONArray("images");
            JSONArray goodsImage = new JSONArray();
            JSONArray image = new JSONArray();
            if (itemImages.size() > 0) {
                Integer imageIndex = 0;
                itemImages.forEach(img -> {
                    String httpStr = ToolUtils.checkHttp(img.toString());
                    if (imageIndex > 0) {
                        image.add(httpStr);
                    }
                    goodsImage.add(httpStr);
                });
            }
            data.put("goods_image", goodsImage);
            data.put("image", image);
            if (sku.size() > 0) {
                JSONArray dataSku = new JSONArray();
                for (Object skuItem : sku) {
                    JSONObject skuInfo = JSONObject.parseObject(JSONObject.toJSONString(skuItem));
                    if (skuInfo.getLong("skuId") > 0L) {
                        JSONArray skuValueArr = new JSONArray();
                        if (data.getInteger("sku_type").equals(GoodsEnum.SKU_TYPE_MORE.getCode())) {
                            for (String skuNameItem : skuInfo.getString("skuName").split(";")) {
                                String[] skuNameItemArr = skuNameItem.split("--");
                                skuValueArr.add(skuNameItemArr[0]);
                            }
                        }

                        JSONObject skuJSON = new JSONObject();
                        skuJSON.put("id", skuInfo.getLong("skuId"));
                        skuJSON.put("image", skuInfo.get("image"));
                        skuJSON.put("sku_value_arr", skuValueArr);
                        skuJSON.put("price", skuInfo.getBigDecimal("price"));
                        skuJSON.put("line_price", 0);
                        skuJSON.put("market_price", 0);
                        skuJSON.put("stock", StringUtils.isNull(skuInfo.getInteger("quantity")) ? 0 : skuInfo.getInteger("quantity"));
                        skuJSON.put("weight", 0);
                        skuJSON.put("volume", 0);
                        skuJSON.put("code", "");
                        dataSku.add(skuJSON);
                    }
                }
                data.put("sku", dataSku);
            }
            if (data.getInteger("sku_type").equals(GoodsEnum.SKU_TYPE_MORE.getCode())) {
                JSONArray skuNameListJArray = new JSONArray();
                JSONArray props = item.getJSONArray("props");
                if (props.size() > 0) {
                    for (Object propsItem : props) {
                        JSONObject prop = JSONObject.parseObject(JSONObject.toJSONString(propsItem));
                        JSONObject propJSON = new JSONObject();
                        propJSON.put("name", prop.getString("name"));
                        propJSON.put("has_image", prop.getString("hasImage") == "True" ? 1 : 0);

                        JSONArray item2Arr = new JSONArray();
                        for (Object item2 : prop.getJSONArray("values")) {
                            JSONObject item2JSON = JSONObject.parseObject(JSONObject.toJSONString(item2));
                            JSONObject valJSON = new JSONObject();
                            valJSON.put("value", item2JSON.getString("name"));
                            valJSON.put("image", StringUtils.isNull(item2JSON.getString("image")) ? "" : item2JSON.getString("image"));
                            item2Arr.add(valJSON);
                        }
                        propJSON.put("value", item2Arr);
                        skuNameListJArray.add(propJSON);
                    }
                }
                data.put("sku_name_list", skuNameListJArray);
            }
        }

        //采集jd
        if (channel.equals(GatherEnum.CHANNEL_JD.getCode())) {
            JSONObject item = contentData.getJSONObject("item");
            JSONArray sku = item.getJSONArray("sku");
            Integer skyCount = sku.size();
            data.put("name", item.getString("name"));
            data.put("descript", "");
            data.put("sku_type", skyCount > 1 ? GoodsEnum.SKU_TYPE_MORE.getCode() : GoodsEnum.SKU_TYPE_SINGLE.getCode());
            data.put("content", item.getString("desc"));
            JSONArray itemImages = item.getJSONArray("images");
            JSONArray goodsImage = new JSONArray();
            JSONArray image = new JSONArray();
            if (itemImages.size() > 0) {
                Integer imageIndex = 0;
                itemImages.forEach(img -> {
                    String httpStr = ToolUtils.checkHttp(img.toString());
                    if (imageIndex > 0) {
                        image.add(httpStr);
                    }
                    goodsImage.add(httpStr);
                });
            }
            data.put("goods_image", goodsImage);
            data.put("image", image);
            if (sku.size() > 0) {
                JSONArray dataSku = new JSONArray();
                for (Object skuItem : sku) {
                    JSONObject skuInfo = JSONObject.parseObject(JSONObject.toJSONString(skuItem));
                    JSONArray skuValueArr = new JSONArray();
                    if (data.getInteger("sku_type").equals(GoodsEnum.SKU_TYPE_MORE.getCode())) {
                        JSONObject saleProp = item.getJSONObject("saleProp");
                        saleProp.forEach((key, val) -> {
                            skuValueArr.add(skuInfo.get(key));
                        });
                    }

                    JSONObject skuJSON = new JSONObject();
                    skuJSON.put("id", "");
                    skuJSON.put("image", "");
                    skuJSON.put("sku_value_arr", skuValueArr);
                    skuJSON.put("price", StringUtils.isNull(skuInfo.getBigDecimal("price")) ? item.getBigDecimal("price") : skuInfo.getBigDecimal("price"));
                    skuJSON.put("line_price", 0);
                    skuJSON.put("market_price", 0);
                    skuJSON.put("stock", StringUtils.isNull(skuInfo.getInteger("stockState")) ? 0 : skuInfo.getInteger("stockState"));
                    skuJSON.put("weight", 0);
                    skuJSON.put("volume", 0);
                    skuJSON.put("code", "");
                    dataSku.add(skuJSON);
                }
                data.put("sku", dataSku);
            }
            if (data.getInteger("sku_type").equals(GoodsEnum.SKU_TYPE_MORE.getCode())) {
                JSONArray skuNameListJArray = new JSONArray();
                JSONObject props = item.getJSONObject("saleProp");
                JSONObject skuProps = item.getJSONObject("skuProps");
                props.forEach((key, val) -> {
                    JSONObject propJSON = new JSONObject();
                    propJSON.put("name", val);
                    propJSON.put("has_image", 0);
                    JSONArray item2Arr = new JSONArray();
                    skuProps.getJSONArray(key).forEach((skuPropsV) ->{
                        JSONObject item2Json = new JSONObject();
                        item2Json.put("value", skuPropsV);
                        item2Json.put("image", "");
                        item2Arr.add(item2Json);
                    });
                    propJSON.put("value", item2Arr);
                    skuNameListJArray.add(propJSON);
                });
                data.put("sku_name_list", skuNameListJArray);
            }
        }

        if (channel.equals(GatherEnum.CHANNEL_1688.getCode())) {
            JSONObject sku = contentData.getJSONObject("skuMap");
            data.put("name", contentData.getString("title"));
            data.put("descript", "");
            data.put("sku_type", sku.size() > 1 ? GoodsEnum.SKU_TYPE_MORE.getCode() : GoodsEnum.SKU_TYPE_SINGLE.getCode());
            data.put("content", contentData.getString("content"));

            JSONArray itemImages = contentData.getJSONArray("images");
            JSONArray goodsImage = new JSONArray();
            JSONArray image = new JSONArray();
            if (itemImages.size() > 0) {
                Integer imageIndex = 0;
                itemImages.forEach(img -> {
                    String httpStr = ToolUtils.checkHttp(img.toString());
                    if (imageIndex > 0) {
                        image.add(httpStr);
                    }
                    goodsImage.add(httpStr);
                });
            }
            data.put("goods_image", goodsImage);
            data.put("image", image);

            JSONArray dataSku = new JSONArray();
            sku.forEach((skuKey, skuInfoObj) -> {
                JSONObject skuInfo = JSONObject.parseObject(JSONObject.toJSONString(skuInfoObj));
                JSONObject skuJSON = new JSONObject();
                if (data.getInteger("sku_type").equals(GoodsEnum.SKU_TYPE_MORE.getCode())) {
                    skuJSON.put("sku_value_arr", skuKey.split(";"));
                } else {
                    skuJSON.put("sku_value_arr", new JSONArray());
                }
                skuJSON.put("id", "");
                skuJSON.put("image", "");
                skuJSON.put("price", StringUtils.isNull(skuInfo.getBigDecimal("discountPrice")) ? new BigDecimal(0) : skuInfo.getBigDecimal("price"));
                skuJSON.put("line_price", 0);
                skuJSON.put("market_price", 0);
                skuJSON.put("stock", StringUtils.isNull(skuInfo.getInteger("canBookCount")) ? 0 : skuInfo.getInteger("canBookCount"));
                skuJSON.put("weight", 0);
                skuJSON.put("volume", 0);
                skuJSON.put("code", "");
                dataSku.add(skuJSON);
            });
            data.put("sku", dataSku);
        }
        if (data.getInteger("sku_type").equals(GoodsEnum.SKU_TYPE_MORE.getCode())) {
            JSONArray skuProps = contentData.getJSONArray("skuProps");
            if (skuProps != null) {
                JSONArray skuNameListJArray = new JSONArray();
                for (Object skuPropsObj : skuProps) {
                    JSONObject skuPropsJson = JSONObject.parseObject(JSONObject.toJSONString(skuPropsObj));
                    Integer hasImage = 0;
                    JSONObject itemJSON = new JSONObject();
                    itemJSON.put("name", skuPropsJson.getString("prop"));
                    JSONArray itemValue = skuPropsJson.getJSONArray("value");
                    JSONArray valueArr = new JSONArray();
                    for (Object itemValueObj : itemValue) {
                        JSONObject itemValueJSON = JSONObject.parseObject(JSONObject.toJSONString(itemValueObj));
                        JSONObject valJson = new JSONObject();
                        valJson.put("value", itemValueJSON.getString("name"));
                        valJson.put("image", StringUtils.isNull(itemValueJSON.getString("imageUrl")) ? "" : itemValueJSON.getString("imageUrl"));
                        if (StringUtils.isNotNull(itemValueJSON.getString("imageUrl"))) {
                            hasImage = 1;
                        }
                        valueArr.add(valJson);
                    }
                    itemJSON.put("has_image", hasImage);
                    itemJSON.put("value", valueArr);
                    skuNameListJArray.add(itemJSON);
                }
                data.put("sku_name_list", skuNameListJArray);
            }
        }

        return data;
    }
}
