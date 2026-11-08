package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 商品服务Feign降级处理类
/**
 * 
 * @description 当商品服务不可用时提供降级响应
 * @author Amos
 * @date 2024-12-20
 */
@Component
public class GoodsServiceFeignFallback implements GoodsServiceFeign {

    @Override
    public ResponseResult queryUserFootprint(Map<String, Object> params) {
        return ResponseResult.fail(503, "goods service unavailable");
    }

    @Override
    public ResponseResult deleteUserFootprint(Map<String, Object> params) {
        return ResponseResult.fail(503, "goods service unavailable");
    }

    @Override
    public ResponseResult queryImFootprint(Map<String, Object> params) {
        return ResponseResult.fail(503, "goods service unavailable");
    }

    @Override
    public ResponseResult queryTransportArea(Map<String, Object> param) {
        return ResponseResult.fail(503, "goods service unavailable");
    }

    @Override
    public ResponseResult queryGoodsFilter(Map<String, Object> param) {
        return ResponseResult.fail(503, "goods service unavailable");
    }

    @Override
    public ResponseResult queryGoodsList(Map<String, Object> param) {
        return ResponseResult.fail("商品服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getGoodsDetail(Long id) {
        return ResponseResult.fail(404, "接口未实现：查询商品详情接口尚未在goods服务中实现");
    }

    @Override
    public ResponseResult createGoods(Map<String, Object> goodsData) {
        return ResponseResult.fail(404, "接口未实现：创建商品接口尚未在goods服务中实现");
    }

    @Override
    public ResponseResult updateGoods(Map<String, Object> goodsData) {
        return ResponseResult.fail(404, "接口未实现：更新商品接口尚未在goods服务中实现");
    }

    @Override
    public ResponseResult deleteGoods(Map<String, Object> param) {
        return ResponseResult.fail(404, "接口未实现：删除商品接口尚未在goods服务中实现");
    }

    @Override
    public ResponseResult updateGoodsStatus(Map<String, Object> param) {
        return ResponseResult.fail(404, "接口未实现：更新商品状态接口尚未在goods服务中实现");
    }

    @Override
    public ResponseResult updateGoodsStock(Map<String, Object> param) {
        return ResponseResult.fail(404, "接口未实现：更新商品库存接口尚未在goods服务中实现");
    }

    @Override
    public ResponseResult queryCategoryList(Map<String, Object> param) {
        return ResponseResult.fail("商品服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult createCategory(Map<String, Object> categoryData) {
        return ResponseResult.fail(404, "接口未实现：创建分类接口尚未在goods服务中实现");
    }

    @Override
    public ResponseResult updateCategory(Map<String, Object> categoryData) {
        return ResponseResult.fail(404, "接口未实现：更新分类接口尚未在goods服务中实现");
    }

    @Override
    public ResponseResult deleteCategory(Map<String, Object> param) {
        return ResponseResult.fail(404, "接口未实现：删除分类接口尚未在goods服务中实现");
    }

    @Override
    public ResponseResult getCategoryDistribution() {
        return ResponseResult.fail(404, "接口未实现：分类分布统计接口尚未在goods服务中实现");
    }

    @Override
    public ResponseResult getCategoryTree() {
        return ResponseResult.fail("商品服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult queryBrandList(Map<String, Object> param) {
        return ResponseResult.fail(404, "接口未实现：查询品牌列表接口尚未在goods服务中实现");
    }

    @Override
    public ResponseResult createBrand(Map<String, Object> brandData) {
        return ResponseResult.fail(404, "接口未实现：创建品牌接口尚未在goods服务中实现");
    }

    @Override
    public ResponseResult updateBrand(Map<String, Object> brandData) {
        return ResponseResult.fail(404, "接口未实现：更新品牌接口尚未在goods服务中实现");
    }

    @Override
    public ResponseResult deleteBrand(Map<String, Object> param) {
        return ResponseResult.fail(404, "接口未实现：删除品牌接口尚未在goods服务中实现");
    }

    @Override
    public ResponseResult querySpecList(Map<String, Object> param) {
        return ResponseResult.fail(404, "接口未实现：查询规格列表接口尚未在goods服务中实现");
    }

    @Override
    public ResponseResult createSpec(Map<String, Object> specData) {
        return ResponseResult.fail(404, "接口未实现：创建规格接口尚未在goods服务中实现");
    }

    @Override
    public ResponseResult updateSpec(Map<String, Object> specData) {
        return ResponseResult.fail(404, "接口未实现：更新规格接口尚未在goods服务中实现");
    }

    @Override
    public ResponseResult deleteSpec(Map<String, Object> param) {
        return ResponseResult.fail(404, "接口未实现：删除规格接口尚未在goods服务中实现");
    }

    @Override
    public ResponseResult queryAttributeList(Map<String, Object> param) {
        return ResponseResult.fail(404, "接口未实现：查询属性列表接口尚未在goods服务中实现");
    }

    @Override
    public ResponseResult createAttribute(Map<String, Object> attributeData) {
        return ResponseResult.fail(404, "接口未实现：创建属性接口尚未在goods服务中实现");
    }

    @Override
    public ResponseResult updateAttribute(Map<String, Object> attributeData) {
        return ResponseResult.fail(404, "接口未实现：更新属性接口尚未在goods服务中实现");
    }

    @Override
    public ResponseResult deleteAttribute(Map<String, Object> param) {
        return ResponseResult.fail(404, "接口未实现：删除属性接口尚未在goods服务中实现");
    }
    
    @Override
    public ResponseResult getGoodsStatistics() {
        Map<String, Object> empty = new java.util.LinkedHashMap<>();
        empty.put("totalGoods", 0);
        empty.put("onSaleGoods", 0);
        empty.put("offSaleGoods", 0);
        empty.put("lowStockGoods", 0);
        empty.put("topGoods", java.util.Collections.emptyList());
        return ResponseResult.success(empty);
    }
    
    @Override
    public ResponseResult queryAllBrands() {
        return ResponseResult.fail("商品服务暂时不可用，请稍后重试");
    }
    
    /** =============== 品牌类型管理 =============== */
    
    @Override
    public ResponseResult queryBrandTypeList(Map<String, Object> param) {
        return ResponseResult.fail("商品服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult createBrandType(Map<String, Object> brandTypeData) {
        return ResponseResult.fail(404, "接口未实现：创建品牌类型接口尚未在goods服务中实现");
    }
    
    @Override
    public ResponseResult updateBrandType(Map<String, Object> brandTypeData) {
        return ResponseResult.fail(404, "接口未实现：更新品牌类型接口尚未在goods服务中实现");
    }
    
    @Override
    public ResponseResult deleteBrandType(Map<String, Object> param) {
        return ResponseResult.fail(404, "接口未实现：删除品牌类型接口尚未在goods服务中实现");
    }
    
    /** =============== 属性类型管理 =============== */
    
    @Override
    public ResponseResult queryAttributeTypePage(Map<String, Object> param) {
        return ResponseResult.fail("商品服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult queryAttributeTypeList() {
        return ResponseResult.fail("商品服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult createAttributeType(Map<String, Object> attributeTypeData) {
        return ResponseResult.fail(404, "接口未实现：创建属性类型接口尚未在goods服务中实现");
    }
    
    @Override
    public ResponseResult updateAttributeType(Map<String, Object> attributeTypeData) {
        return ResponseResult.fail(404, "接口未实现：更新属性类型接口尚未在goods服务中实现");
    }
    
    @Override
    public ResponseResult deleteAttributeType(Map<String, Object> param) {
        return ResponseResult.fail(404, "接口未实现：删除属性类型接口尚未在goods服务中实现");
    }
    
    /** =============== 属性组管理 =============== */
    
    @Override
    public ResponseResult queryAttributeGroupList(Map<String, Object> param) {
        return ResponseResult.fail("商品服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult createAttributeGroup(Map<String, Object> attributeGroupData) {
        return ResponseResult.fail(404, "接口未实现：创建属性组接口尚未在goods服务中实现");
    }
    
    @Override
    public ResponseResult updateAttributeGroup(Map<String, Object> attributeGroupData) {
        return ResponseResult.fail(404, "接口未实现：更新属性组接口尚未在goods服务中实现");
    }
    
    @Override
    public ResponseResult deleteAttributeGroup(Map<String, Object> param) {
        return ResponseResult.fail(404, "接口未实现：删除属性组接口尚未在goods服务中实现");
    }
    
    // =============== 库存管理降级 ===============
    
    /** 库存统计 */
    @Override
    public ResponseResult getInventoryStatistics(Map<String, Object> param) {
        return ResponseResult.fail("库存服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getInventoryList(Map<String, Object> param) {
        return ResponseResult.fail("库存服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getInventoryFlow(Map<String, Object> param) {
        return ResponseResult.fail("库存服务暂时不可用，请稍后重试");
    }
    
    /** 库存预警 */
    @Override
    public ResponseResult getInventoryAlertStats(Map<String, Object> param) {
        return ResponseResult.fail("库存服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getInventoryAlertList(Map<String, Object> param) {
        return ResponseResult.fail("库存服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult adjustInventoryAlert(Map<String, Object> param) {
        return ResponseResult.fail("库存服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult createPurchaseRequest(Map<String, Object> param) {
        return ResponseResult.fail("库存服务暂时不可用，请稍后重试");
    }
    
    /** 库存盘点 */
    @Override
    public ResponseResult getInventoryCountList(Map<String, Object> param) {
        return ResponseResult.fail("库存服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult createInventoryCount(Map<String, Object> param) {
        return ResponseResult.fail("库存服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getInventoryCountDetail(Map<String, Object> param) {
        return ResponseResult.fail("库存服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult startInventoryCount(Map<String, Object> param) {
        return ResponseResult.fail("库存服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult completeInventoryCount(Map<String, Object> param) {
        return ResponseResult.fail("库存服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult cancelInventoryCount(Map<String, Object> param) {
        return ResponseResult.fail("库存服务暂时不可用，请稍后重试");
    }
    
    /** 出库管理 */
    @Override
    public ResponseResult getOutboundList(Map<String, Object> param) {
        return ResponseResult.fail("库存服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult createOutbound(Map<String, Object> param) {
        return ResponseResult.fail("库存服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getOutboundDetail(Map<String, Object> param) {
        return ResponseResult.fail("库存服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult approveOutbound(Map<String, Object> param) {
        return ResponseResult.fail("库存服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult confirmOutbound(Map<String, Object> param) {
        return ResponseResult.fail("库存服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult cancelOutbound(Map<String, Object> param) {
        return ResponseResult.fail("库存服务暂时不可用，请稍后重试");
    }
    
    /** 入库管理 */
    @Override
    public ResponseResult getInboundList(Map<String, Object> param) {
        return ResponseResult.fail("库存服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult createInbound(Map<String, Object> param) {
        return ResponseResult.fail("库存服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getInboundDetail(Map<String, Object> param) {
        return ResponseResult.fail("库存服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult approveInbound(Map<String, Object> param) {
        return ResponseResult.fail("库存服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult confirmInbound(Map<String, Object> param) {
        return ResponseResult.fail("库存服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult cancelInbound(Map<String, Object> param) {
        return ResponseResult.fail("库存服务暂时不可用，请稍后重试");
    }
    
    // =============== 数据统计降级 ===============
    
    /** 统计报表 */
    @Override
    public ResponseResult getStatisticsOverview(Map<String, Object> param) {
        return ResponseResult.fail("统计服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getStatisticsDetail(Map<String, Object> param) {
        return ResponseResult.fail("统计服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getStatisticsTrend(Map<String, Object> param) {
        return ResponseResult.fail("统计服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult exportStatistics(Map<String, Object> param) {
        return ResponseResult.fail("统计服务暂时不可用，请稍后重试");
    }
    
    /** 销售统计 */
    @Override
    public ResponseResult getSalesStatistics(Map<String, Object> param) {
        return ResponseResult.fail("统计服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getSalesTrend(Map<String, Object> param) {
        return ResponseResult.fail("统计服务暂时不可用，请稍后重试");
    }
    
    /** 财务统计 */
    @Override
    public ResponseResult getFinanceStatistics(Map<String, Object> param) {
        return ResponseResult.fail("统计服务暂时不可用，请稍后重试");
    }
    
    /** 库存统计 */
    @Override
    public ResponseResult getInventoryStatisticsOverview(Map<String, Object> param) {
        return ResponseResult.fail("统计服务暂时不可用，请稍后重试");
    }
    
    /** 客户统计 */
    @Override
    public ResponseResult getCustomerStatistics(Map<String, Object> param) {
        return ResponseResult.fail("统计服务暂时不可用，请稍后重试");
    }
    
    /** 商品统计 */
    @Override
    public ResponseResult getGoodsStatisticsOverview(Map<String, Object> param) {
        return ResponseResult.fail("统计服务暂时不可用，请稍后重试");
    }
    
    // =============== 售后服务降级 ===============
    
    /** 服务设置 */
    @Override
    public ResponseResult getServicePolicy(Map<String, Object> param) {
        return ResponseResult.fail("售后服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult saveServicePolicy(Map<String, Object> param) {
        return ResponseResult.fail("售后服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getServiceReasonList(Map<String, Object> param) {
        return ResponseResult.fail("售后服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult saveServiceReason(Map<String, Object> param) {
        return ResponseResult.fail("售后服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult deleteServiceReason(Map<String, Object> param) {
        return ResponseResult.fail("售后服务暂时不可用，请稍后重试");
    }
    
    /** 服务统计 */
    @Override
    public ResponseResult getServiceStatisticsOverview(Map<String, Object> param) {
        return ResponseResult.fail("售后服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getServiceTypeStatistics(Map<String, Object> param) {
        return ResponseResult.fail("售后服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getServiceSatisfactionStatistics(Map<String, Object> param) {
        return ResponseResult.fail("售后服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getServiceStatisticsDetail(Map<String, Object> param) {
        return ResponseResult.fail("售后服务暂时不可用，请稍后重试");
    }
    
    /** 服务记录 */
    @Override
    public ResponseResult getServiceRecordList(Map<String, Object> param) {
        return ResponseResult.fail("售后服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getServiceRecordDetail(Map<String, Object> param) {
        return ResponseResult.fail("售后服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getServiceRecordTimeline(Map<String, Object> param) {
        return ResponseResult.fail("售后服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getServiceRecordReview(Map<String, Object> param) {
        return ResponseResult.fail("售后服务暂时不可用，请稍后重试");
    }
    
    /** 服务工单 */
    @Override
    public ResponseResult getServiceTicketList(Map<String, Object> param) {
        return ResponseResult.fail("售后服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getServiceTicketDetail(Map<String, Object> param) {
        return ResponseResult.fail("售后服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult acceptServiceTicket(Map<String, Object> param) {
        return ResponseResult.fail("售后服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult processServiceTicket(Map<String, Object> param) {
        return ResponseResult.fail("售后服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult completeServiceTicket(Map<String, Object> param) {
        return ResponseResult.fail("售后服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult cancelServiceTicket(Map<String, Object> param) {
        return ResponseResult.fail("售后服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getServiceTicketCount(Map<String, Object> param) {
        return ResponseResult.fail("售后服务暂时不可用，请稍后重试");
    }

    /** ================= Mall 平台运营补齐 Fallback ================= */

    @Override
    public ResponseResult querySkuList(Map<String, Object> param) {
        return ResponseResult.fail(503, "商品服务暂时不可用：SKU 列表查询降级");
    }

    @Override
    public ResponseResult getCommonDetail(Integer commonId) {
        return ResponseResult.fail(503, "商品服务暂时不可用：SPU 详情查询降级");
    }

    @Override
    public ResponseResult batchUpdateGoodsPrice(Map<String, Object> param) {
        return ResponseResult.fail(503, "商品服务暂时不可用：批量调价降级");
    }

    @Override
    public ResponseResult updateGoodsBaseByMap(Map<String, Object> param) {
        return ResponseResult.fail(503, "商品服务暂时不可用：商品基础信息更新降级");
    }

    @Override
    public ResponseResult updateGoodsBaseState(Map<String, Object> param) {
        return ResponseResult.fail(503, "商品服务暂时不可用：商品状态批量更新降级");
    }

    @Override
    public ResponseResult queryByTypeId(Map<String, Object> param) {
        return ResponseResult.fail(503, "商品服务暂时不可用：按类型查询商品降级");
    }

    @Override
    public ResponseResult insertOssPhotoPort(Map<String, Object> param) {
        return ResponseResult.fail(503, "商品服务暂时不可用：OSS 图片入库降级");
    }

    @Override
    public ResponseResult selectCommodityDetails(Map<String, Object> param) {
        return ResponseResult.fail(503, "商品服务暂时不可用：商品详情查询降级");
    }

    @Override
    public ResponseResult selectCommodityDetailsExp(Map<String, Object> param) {
        return ResponseResult.fail(503, "商品服务暂时不可用：商品详情导出降级");
    }

    @Override
    public ResponseResult ossUpload(Map<String, Object> param) {
        return ResponseResult.fail(503, "商品服务暂时不可用：OSS 上传降级");
    }

    /** ============== Mall 商品中心降级 ============== */
    @Override public ResponseResult mallGoodsList(Map<String, Object> param) { return fail("Mall 商品列表"); }
    @Override public ResponseResult mallGoodsDetail(Integer id, String lang) { return fail("Mall 商品详情"); }
    @Override public ResponseResult mallGoodsI18nList(Integer commonId) { return fail("Mall 商品多语言列表"); }
    @Override public ResponseResult mallGoodsI18nSave(Integer commonId, Map<String, Object> body) { return fail("Mall 商品多语言保存"); }
    @Override public ResponseResult mallGoodsI18nSavePost(Map<String, Object> body) { return fail("Mall 商品多语言保存"); }
    @Override public ResponseResult mallGoodsI18nPublished(Integer commonId, String lang) { return fail("Mall 商品多语言已发布"); }
    @Override public ResponseResult mallGoodsSet(Map<String, Object> param) { return fail("Mall 商品保存"); }
    @Override public ResponseResult mallGoodsAudit(Map<String, Object> param) { return fail("Mall 商品审核"); }
    @Override public ResponseResult mallGoodsReAudit(Map<String, Object> param) { return fail("Mall 商品重审"); }
    @Override public ResponseResult mallGoodsDel(Map<String, Object> param) { return fail("Mall 商品删除"); }
    @Override public ResponseResult mallGoodsChangeStatus(Map<String, Object> param) { return fail("Mall 商品上下架"); }
    @Override public ResponseResult mallGoodsChangeRecommend(Map<String, Object> param) { return fail("Mall 商品推荐"); }
    @Override public ResponseResult mallCategoryList(String name) { return fail("Mall 分类列表"); }
    @Override public ResponseResult mallCategoryAll() { return fail("Mall 分类平铺"); }
    @Override public ResponseResult mallCategoryDetail(Integer id) { return fail("Mall 分类详情"); }
    @Override public ResponseResult mallCategoryAdd(Map<String, Object> param) { return fail("Mall 分类新增"); }
    @Override public ResponseResult mallCategoryEdit(Map<String, Object> param) { return fail("Mall 分类编辑"); }
    @Override public ResponseResult mallCategoryDel(Map<String, Object> param) { return fail("Mall 分类删除"); }
    @Override public ResponseResult mallCategoryStatus(Map<String, Object> param) { return fail("Mall 分类状态"); }
    @Override public ResponseResult mallCommentList(Map<String, Object> param) { return fail("Mall 评论列表"); }
    @Override public ResponseResult mallCommentSetIsShow(Map<String, Object> param) { return fail("Mall 评论状态"); }
    @Override public ResponseResult mallCommentReply(Map<String, Object> param) { return fail("Mall 评论回复"); }
    @Override public ResponseResult mallCommentDel(Map<String, Object> param) { return fail("Mall 评论删除"); }
    @Override public ResponseResult searchGoodsData(Map<String, Object> param) { return fail("商品搜索"); }
    @Override public ResponseResult queryCatFilter(Map<String, Object> param) { return fail("分类筛选"); }

    private ResponseResult fail(String name) {
        return ResponseResult.fail(503, "商品服务暂时不可用：" + name + "降级");
    }
}
