package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "goods", contextId = "goodsServiceFeign", url = "${feign.goods.url:}",
        path = "/", fallback = GoodsServiceFeignFallback.class)

/**
 * @author Amos
 */
public interface GoodsServiceFeign {

    @PostMapping("/goods/userFootprint/query")
    ResponseResult queryUserFootprint(@RequestBody Map<String, Object> params);

    @PostMapping("/goods/userFootprint/delete")
    ResponseResult deleteUserFootprint(@RequestBody Map<String, Object> params);

    @PostMapping("/goods/userFootprint/im/queryFootPrint")
    ResponseResult queryImFootprint(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/common/goods/query/list")
    ResponseResult queryGoodsList(@RequestBody Map<String, Object> param);

    /** 查询商品可配送区域（PC/H5 统一 BFF 入口）。 */
    @PostMapping("/goods/base/transport/area/query")
    ResponseResult queryTransportArea(@RequestBody Map<String, Object> param);

    /** 前台商品筛选（含积分商品筛选），兼容商城 PC 端旧接口。 */
    @PostMapping("/goods/common/integral/query/filter")
    ResponseResult queryGoodsFilter(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/goods/goods/{id}")
    ResponseResult getGoodsDetail(@PathVariable("id") Long id);
    
    /**
     * TODO 补充方法说明
/**
     * @param goodsData TODO
     * @return TODO
     */
    @PostMapping("/goods/goods/add")
    ResponseResult createGoods(@RequestBody Map<String, Object> goodsData);
    
    /**
     * TODO 补充方法说明
/**
     * @param goodsData TODO
     * @return TODO
     */
    @PostMapping("/goods/goods/update")
    ResponseResult updateGoods(@RequestBody Map<String, Object> goodsData);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/goods/delete")
    ResponseResult deleteGoods(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/goods/status")
    ResponseResult updateGoodsStatus(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/goods/stock")
    ResponseResult updateGoodsStock(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/cat/category/query")
    ResponseResult queryCategoryList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param categoryData TODO
     * @return TODO
     */
    @PostMapping("/goods/category/add")
    ResponseResult createCategory(@RequestBody Map<String, Object> categoryData);
    
    /**
     * TODO 补充方法说明
/**
     * @param categoryData TODO
     * @return TODO
     */
    @PostMapping("/goods/category/update")
    ResponseResult updateCategory(@RequestBody Map<String, Object> categoryData);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/category/delete")
    ResponseResult deleteCategory(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/goods/category/distribution")
    ResponseResult getCategoryDistribution();
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/goods/cat/category/tree")
    ResponseResult getCategoryTree();
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/brand/query")
    ResponseResult queryBrandList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param brandData TODO
     * @return TODO
     */
    @PostMapping("/goods/brand/add")
    ResponseResult createBrand(@RequestBody Map<String, Object> brandData);
    
    /**
     * TODO 补充方法说明
/**
     * @param brandData TODO
     * @return TODO
     */
    @PostMapping("/goods/brand/update")
    ResponseResult updateBrand(@RequestBody Map<String, Object> brandData);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/brand/delete")
    ResponseResult deleteBrand(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/spec/query")
    ResponseResult querySpecList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param specData TODO
     * @return TODO
     */
    @PostMapping("/goods/spec/add")
    ResponseResult createSpec(@RequestBody Map<String, Object> specData);
    
    /**
     * TODO 补充方法说明
/**
     * @param specData TODO
     * @return TODO
     */
    @PostMapping("/goods/spec/update")
    ResponseResult updateSpec(@RequestBody Map<String, Object> specData);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/spec/delete")
    ResponseResult deleteSpec(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/attribute/query")
    ResponseResult queryAttributeList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param attributeData TODO
     * @return TODO
     */
    @PostMapping("/goods/attribute/add")
    ResponseResult createAttribute(@RequestBody Map<String, Object> attributeData);
    
    /**
     * TODO 补充方法说明
/**
     * @param attributeData TODO
     * @return TODO
     */
    @PostMapping("/goods/attribute/update")
    ResponseResult updateAttribute(@RequestBody Map<String, Object> attributeData);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/attribute/delete")
    ResponseResult deleteAttribute(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/goods/common/statistics")
    ResponseResult getGoodsStatistics();

    // ================= 品牌管理补充接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/goods/brand/queryList")
    ResponseResult queryAllBrands();

    // ================= 品牌类型管理接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/brandtype/list")
    ResponseResult queryBrandTypeList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param brandTypeData TODO
     * @return TODO
     */
    @PostMapping("/goods/brandtype/create")
    ResponseResult createBrandType(@RequestBody Map<String, Object> brandTypeData);
    
    /**
     * TODO 补充方法说明
/**
     * @param brandTypeData TODO
     * @return TODO
     */
    @PostMapping("/goods/brandtype/update")
    ResponseResult updateBrandType(@RequestBody Map<String, Object> brandTypeData);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/brandtype/delete")
    ResponseResult deleteBrandType(@RequestBody Map<String, Object> param);

    // ================= 属性类型管理接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/attributetype/page")
    ResponseResult queryAttributeTypePage(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/goods/attributetype/list")
    ResponseResult queryAttributeTypeList();
    
    /**
     * TODO 补充方法说明
/**
     * @param attributeTypeData TODO
     * @return TODO
     */
    @PostMapping("/goods/attributetype/create")
    ResponseResult createAttributeType(@RequestBody Map<String, Object> attributeTypeData);
    
    /**
     * TODO 补充方法说明
/**
     * @param attributeTypeData TODO
     * @return TODO
     */
    @PostMapping("/goods/attributetype/update")
    ResponseResult updateAttributeType(@RequestBody Map<String, Object> attributeTypeData);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/attributetype/delete")
    ResponseResult deleteAttributeType(@RequestBody Map<String, Object> param);

    // ================= 属性组管理接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/attributegroup/list")
    ResponseResult queryAttributeGroupList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param attributeGroupData TODO
     * @return TODO
     */
    @PostMapping("/goods/attributegroup/create")
    ResponseResult createAttributeGroup(@RequestBody Map<String, Object> attributeGroupData);
    
    /**
     * TODO 补充方法说明
/**
     * @param attributeGroupData TODO
     * @return TODO
     */
    @PostMapping("/goods/attributegroup/update")
    ResponseResult updateAttributeGroup(@RequestBody Map<String, Object> attributeGroupData);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/attributegroup/delete")
    ResponseResult deleteAttributeGroup(@RequestBody Map<String, Object> param);

    // ================= 库存管理接口 =================
    
    // 库存统计
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/inventory/statistics/overview")
    ResponseResult getInventoryStatistics(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/inventory/statistics/list")
    ResponseResult getInventoryList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/inventory/statistics/flow")
    ResponseResult getInventoryFlow(@RequestBody Map<String, Object> param);
    
    // 库存预警
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/inventory/alert/overview")
    ResponseResult getInventoryAlertStats(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/inventory/alert/list")
    ResponseResult getInventoryAlertList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/inventory/alert/adjust")
    ResponseResult adjustInventoryAlert(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/inventory/alert/purchase")
    ResponseResult createPurchaseRequest(@RequestBody Map<String, Object> param);
    
    // 库存盘点
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/inventory/count/list")
    ResponseResult getInventoryCountList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/inventory/count/create")
    ResponseResult createInventoryCount(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/inventory/count/detail")
    ResponseResult getInventoryCountDetail(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/inventory/count/start")
    ResponseResult startInventoryCount(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/inventory/count/complete")
    ResponseResult completeInventoryCount(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/inventory/count/cancel")
    ResponseResult cancelInventoryCount(@RequestBody Map<String, Object> param);
    
    // 出库管理
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/inventory/outbound/list")
    ResponseResult getOutboundList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/inventory/outbound/create")
    ResponseResult createOutbound(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/inventory/outbound/detail")
    ResponseResult getOutboundDetail(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/inventory/outbound/approve")
    ResponseResult approveOutbound(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/inventory/outbound/confirm")
    ResponseResult confirmOutbound(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/inventory/outbound/cancel")
    ResponseResult cancelOutbound(@RequestBody Map<String, Object> param);
    
    // 入库管理
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/inventory/inbound/list")
    ResponseResult getInboundList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/inventory/inbound/create")
    ResponseResult createInbound(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/inventory/inbound/detail")
    ResponseResult getInboundDetail(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/inventory/inbound/approve")
    ResponseResult approveInbound(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/inventory/inbound/confirm")
    ResponseResult confirmInbound(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/inventory/inbound/cancel")
    ResponseResult cancelInbound(@RequestBody Map<String, Object> param);
    
    // ================= 数据统计接口 =================
    
    // 统计报表
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/statistics/report/overview")
    ResponseResult getStatisticsOverview(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/statistics/report/detail")
    ResponseResult getStatisticsDetail(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/statistics/report/trend")
    ResponseResult getStatisticsTrend(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/statistics/report/export")
    ResponseResult exportStatistics(@RequestBody Map<String, Object> param);
    
    // 销售统计
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/statistics/sales/overview")
    ResponseResult getSalesStatistics(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/statistics/sales/trend")
    ResponseResult getSalesTrend(@RequestBody Map<String, Object> param);
    
    // 财务统计
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/statistics/finance/overview")
    ResponseResult getFinanceStatistics(@RequestBody Map<String, Object> param);
    
    // 库存统计
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/statistics/inventory/overview")
    ResponseResult getInventoryStatisticsOverview(@RequestBody Map<String, Object> param);
    
    // 客户统计
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/statistics/customer/overview")
    ResponseResult getCustomerStatistics(@RequestBody Map<String, Object> param);
    
    // 商品统计
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/statistics/goods/overview")
    ResponseResult getGoodsStatisticsOverview(@RequestBody Map<String, Object> param);

    // ================= 售后服务接口 =================
    
    // 服务设置
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/service/policy/get")
    ResponseResult getServicePolicy(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/service/policy/save")
    ResponseResult saveServicePolicy(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/service/reason/list")
    ResponseResult getServiceReasonList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/service/reason/save")
    ResponseResult saveServiceReason(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/service/reason/delete")
    ResponseResult deleteServiceReason(@RequestBody Map<String, Object> param);
    
    // 服务统计
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/service/statistics/overview")
    ResponseResult getServiceStatisticsOverview(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/service/statistics/type")
    ResponseResult getServiceTypeStatistics(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/service/statistics/satisfaction")
    ResponseResult getServiceSatisfactionStatistics(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/service/statistics/detail")
    ResponseResult getServiceStatisticsDetail(@RequestBody Map<String, Object> param);
    
    // 服务记录
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/service/record/list")
    ResponseResult getServiceRecordList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/service/record/detail")
    ResponseResult getServiceRecordDetail(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/service/record/timeline")
    ResponseResult getServiceRecordTimeline(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/service/record/review")
    ResponseResult getServiceRecordReview(@RequestBody Map<String, Object> param);
    
    // 服务工单
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/service/ticket/list")
    ResponseResult getServiceTicketList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/service/ticket/detail")
    ResponseResult getServiceTicketDetail(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/service/ticket/accept")
    ResponseResult acceptServiceTicket(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/service/ticket/process")
    ResponseResult processServiceTicket(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/service/ticket/complete")
    ResponseResult completeServiceTicket(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/service/ticket/cancel")
    ResponseResult cancelServiceTicket(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/service/ticket/count")
    ResponseResult getServiceTicketCount(@RequestBody Map<String, Object> param);

    // ================= Mall 平台运营补齐：对接 paas/goods GoodsBaseController =================

    /** 商品(SKU)列表查询：对接 GoodsBaseController#queryByGoodsBaseList */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/base/goods/list/query")
    ResponseResult querySkuList(@RequestBody Map<String, Object> param);

    /** 商品 SPU 详情：对接 GoodsBaseController#getGoodsCommonDetails (GET) */
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/goods/base/get/cart/getGoodsCommonDetails")
    ResponseResult getCommonDetail(@RequestParam("commonId") Integer commonId);

    /** 批量调整商品价格：对接 GoodsBaseController#batchUpdateGoodsPrice */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/base/price/batchUpdate")
    ResponseResult batchUpdateGoodsPrice(@RequestBody Map<String, Object> param);

    /** 通用更新商品基础信息：对接 GoodsBaseController#updateGoodsBaseByMap */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/base/updateGoodsBase")
    ResponseResult updateGoodsBaseByMap(@RequestBody Map<String, Object> param);

    /** 批量更新商品状态：对接 GoodsBaseController#updateGoodsBaseState */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/base/updateGoodsBaseState")
    ResponseResult updateGoodsBaseState(@RequestBody Map<String, Object> param);

    /** 按类型ID查询商品：对接 GoodsBaseController#queryByTypeId */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/base/typeid/query")
    ResponseResult queryByTypeId(@RequestBody Map<String, Object> param);

    /** OSS 图片记录入库：对接 GoodsBaseController#insertOssPhotoPort */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/base/OSS/insertOssPhotoPort")
    ResponseResult insertOssPhotoPort(@RequestBody Map<String, Object> param);

    /** 商品详情查询(管理后台)：对接 GoodsBaseController#selectCommodityDetails */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/base/goods/selectDetails")
    ResponseResult selectCommodityDetails(@RequestBody Map<String, Object> param);

    /** 商品详情导出：对接 GoodsBaseController#selectCommodityDetailsExp */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/base/goods/selectDetailsExp")
    ResponseResult selectCommodityDetailsExp(@RequestBody Map<String, Object> param);

    /** OSS 上传：对接 GoodsBaseController#uploadToOss */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/goods/base/oss/upload")
    ResponseResult ossUpload(@RequestBody Map<String, Object> param);

    // ============== Mall 商城商品中心（新版 hou_mall_goods_*） ==============

    /** Mall 商品列表 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/goods/lists")
    ResponseResult mallGoodsList(@RequestBody Map<String, Object> param);

    /** Mall 商品详情 */
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/goods/detail")
    ResponseResult mallGoodsDetail(@RequestParam("id") Integer id,
                                   @RequestParam(value = "lang", required = false) String lang);

    /** 商品多语言：列表 */
    @GetMapping("/mall/goods/i18n/{commonId}")
    ResponseResult mallGoodsI18nList(@PathVariable("commonId") Integer commonId);

    /** 商品多语言：保存 */
    @PutMapping("/mall/goods/i18n/{commonId}")
    ResponseResult mallGoodsI18nSave(@PathVariable("commonId") Integer commonId,
                                     @RequestBody Map<String, Object> body);

    /** 商品多语言：POST 保存（兼容无 PUT 的前端） */
    @PostMapping("/mall/goods/i18n/save")
    ResponseResult mallGoodsI18nSavePost(@RequestBody Map<String, Object> body);

    /** 商品多语言：已发布单语种 */
    @GetMapping("/mall/goods/i18n/{commonId}/published")
    ResponseResult mallGoodsI18nPublished(@PathVariable("commonId") Integer commonId,
                                          @RequestParam("lang") String lang);

    /** Mall 商品编辑/新增 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/goods/set")
    ResponseResult mallGoodsSet(@RequestBody Map<String, Object> param);

    /** Mall 商品审核 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/goods/audit")
    ResponseResult mallGoodsAudit(@RequestBody Map<String, Object> param);

    /** Mall 商品重审 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/goods/re_audit")
    ResponseResult mallGoodsReAudit(@RequestBody Map<String, Object> param);

    /** Mall 商品删除 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/goods/del")
    ResponseResult mallGoodsDel(@RequestBody Map<String, Object> param);

    /** Mall 商品上下架 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/goods/change_status")
    ResponseResult mallGoodsChangeStatus(@RequestBody Map<String, Object> param);

    /** Mall 商品店内推荐 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/goods/change_recommend")
    ResponseResult mallGoodsChangeRecommend(@RequestBody Map<String, Object> param);

    /** Mall 分类树 */
    /**
     * TODO 补充方法说明
/**
     * @param "name" TODO
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/mall/goods/category/lists")
    ResponseResult mallCategoryList(@RequestParam(value = "name", required = false) String name);

    /** Mall 分类平铺 */
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/goods/category/all")
    ResponseResult mallCategoryAll();

    /** Mall 分类详情 */
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/goods/category/detail")
    ResponseResult mallCategoryDetail(@RequestParam("id") Integer id);

    /** Mall 分类新增 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/goods/category/add")
    ResponseResult mallCategoryAdd(@RequestBody Map<String, Object> param);

    /** Mall 分类编辑 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/goods/category/edit")
    ResponseResult mallCategoryEdit(@RequestBody Map<String, Object> param);

    /** Mall 分类删除 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/goods/category/del")
    ResponseResult mallCategoryDel(@RequestBody Map<String, Object> param);

    /** Mall 分类显示切换 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/goods/category/status")
    ResponseResult mallCategoryStatus(@RequestBody Map<String, Object> param);

    /** Mall 评论列表 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/goods/comment/lists")
    ResponseResult mallCommentList(@RequestBody Map<String, Object> param);

    /** Mall 评论显示切换 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/goods/comment/set_is_show")
    ResponseResult mallCommentSetIsShow(@RequestBody Map<String, Object> param);

    /** Mall 评论回复 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/goods/comment/reply")
    ResponseResult mallCommentReply(@RequestBody Map<String, Object> param);

    /** Mall 评论删除 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/goods/comment/del")
    ResponseResult mallCommentDel(@RequestBody Map<String, Object> param);

    /**
     * 前台商品搜索/列表（H5 商品列表页、搜索结果页共用）。
     *
     * <p>老 uniapp 直接调 goods 的 /es/searchGoodsData，迁到 BFF 后这条路径没补，
     * 前端拿到 20001 就渲染成空列表。这里按原路径与入参透传，不改前端契约。
     */
    @PostMapping("/es/searchGoodsData")
    ResponseResult searchGoodsData(@RequestBody Map<String, Object> param);

    /** 前台分类/品牌/属性筛选项（商品列表页左侧筛选面板与分类条）。 */
    @PostMapping("/goods/cat/query/filter")
    ResponseResult queryCatFilter(@RequestBody Map<String, Object> param);
}
