package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.MallStyleDefaults;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Mall商城服务Feign降级处理
/**
 * 
 * @author Amos
 */
@Component
public class MallServiceFeignFallback implements MallServiceFeign {

    private ResponseResult fallbackResult() {
        return ResponseResult.error("Mall服务暂时不可用，请稍后重试");
    }

    /** ================= Dashboard聚合 ================= */

    @Override
    public ResponseResult mallAdminGoodsList(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult mallAdminGoodsCategoryTree(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult mallAdminOrderManageList(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult mallAdminOrderManageDetail(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getDashboardOverview() {
        return fallbackResult();
    }

    @Override
    public ResponseResult getDashboardWorkbench() {
        return fallbackResult();
    }

    @Override
    public ResponseResult getDashboardCheckVersion(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getDashboardCheckLegal(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getDashboardRealtime() {
        return fallbackResult();
    }

    @Override
    public ResponseResult getDashboardSalesTrend(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getDashboardGoodsRank(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getDashboardQuickActions() {
        return fallbackResult();
    }

    /** ================= 客户管理 ================= */

    @Override
    public ResponseResult getCustomerList(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getCustomerDetail(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult saveCustomer(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult deleteCustomer(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getCustomerGroupList() {
        return fallbackResult();
    }

    @Override
    public ResponseResult saveCustomerGroup(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult deleteCustomerGroup(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getCustomerTagList() {
        return fallbackResult();
    }

    @Override
    public ResponseResult saveCustomerTag(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult deleteCustomerTag(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getCustomerFollowList(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult addCustomerFollow(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult deleteCustomerFollow(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getCustomerStatistics(Map<String, Object> params) {
        return fallbackResult();
    }

    /** ================= 积分商城 ================= */

    @Override
    public ResponseResult getPointsToolList(Map<String, Object> params) { return fallbackResult(); }
    @Override
    public ResponseResult getPointsToolDetail(Map<String, Object> params) { return fallbackResult(); }
    @Override
    public ResponseResult createPointsTool(Map<String, Object> params) { return fallbackResult(); }
    @Override
    public ResponseResult updatePointsTool(Map<String, Object> params) { return fallbackResult(); }
    @Override
    public ResponseResult deletePointsTool(Map<String, Object> params) { return fallbackResult(); }
    @Override
    public ResponseResult togglePointsToolStatus(Map<String, Object> params) { return fallbackResult(); }

    /** ================= 直播互动 ================= */

    @Override public ResponseResult getLiveList(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult getLiveDetail(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult createLive(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult updateLive(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult deleteLive(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult startLive(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult endLive(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult getLiveProductAuditList(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult getLiveProductAuditDetail(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult submitLiveProductAudit(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult getLiveAccountList(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult getLiveAccountDetail(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult createLiveAccount(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult updateLiveAccount(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult deleteLiveAccount(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult toggleLiveAccountStatus(Map<String, Object> p) { return fallbackResult(); }

    /** ================= 发现内容 - 话题分类 ================= */

    @Override public ResponseResult getTopicCategoryList(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult getTopicCategoryDetail(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult createTopicCategory(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult updateTopicCategory(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult deleteTopicCategory(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult toggleTopicCategoryStatus(Map<String, Object> p) { return fallbackResult(); }

    /** ================= 发现内容 - 选题 ================= */

    @Override public ResponseResult getSelectionList(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult getSelectionDetail(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult createSelection(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult updateSelection(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult deleteSelection(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult publishSelection(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult offlineSelection(Map<String, Object> p) { return fallbackResult(); }

    /** ================= 发现内容 - 心得 ================= */

    @Override public ResponseResult getMoodList(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult getMoodDetail(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult auditMood(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult deleteMood(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult offlineMood(Map<String, Object> p) { return fallbackResult(); }

    /** ================= 发现内容 - 评论 ================= */

    @Override public ResponseResult getReviewList(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult getReviewDetail(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult auditReview(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult deleteReview(Map<String, Object> p) { return fallbackResult(); }

    /** ================= 财务管理 - 收入 ================= */

    @Override public ResponseResult getFinanceIncomeList(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult getFinanceIncomeDetail(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult createFinanceIncome(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult confirmFinanceIncome(Map<String, Object> p) { return fallbackResult(); }

    /** ================= 财务管理 - 支出 ================= */

    @Override public ResponseResult getFinanceExpenseList(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult getFinanceExpenseDetail(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult createFinanceExpense(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult confirmFinanceExpense(Map<String, Object> p) { return fallbackResult(); }

    /** ================= 财务管理 - 结算 ================= */

    @Override public ResponseResult getFinanceSettlementList(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult getFinanceSettlementDetail(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult createFinanceSettlement(Map<String, Object> p) { return fallbackResult(); }
    @Override public ResponseResult settleFinanceSettlement(Map<String, Object> p) { return fallbackResult(); }

    /** ================= 分销模块 - 分销员 ================= */
    
    @Override
    public ResponseResult getDistributorList(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getDistributorDetail(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult applyDistributor(Map<String, Object> distributor) {
        return fallbackResult();
    }

    @Override
    public ResponseResult auditDistributor(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult updateDistributorStatus(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult updateDistributor(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult deleteDistributor(Map<String, Object> param) {
        return fallbackResult();
    }

    /** ================= 分销模块 - 分销等级 ================= */

    @Override
    public ResponseResult getDistributorLevelList(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getAllDistributorLevels(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult createDistributorLevel(Map<String, Object> level) {
        return fallbackResult();
    }

    @Override
    public ResponseResult updateDistributorLevel(Map<String, Object> level) {
        return fallbackResult();
    }

    @Override
    public ResponseResult deleteDistributorLevel(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getDistributorLevelDetail(Map<String, Object> param) {
        return fallbackResult();
    }

    /** ================= 分销模块 - 佣金记录 ================= */

    @Override
    public ResponseResult getCommissionRecordList(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getCommissionRecordDetail(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult settleCommission(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult batchSettleCommission(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getCommissionStats(Map<String, Object> param) {
        return fallbackResult();
    }

    /** ================= 分销模块 - 佣金规则 ================= */

    @Override
    public ResponseResult getCommissionRuleList(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult createCommissionRule(Map<String, Object> rule) {
        return fallbackResult();
    }

    @Override
    public ResponseResult updateCommissionRule(Map<String, Object> rule) {
        return fallbackResult();
    }

    @Override
    public ResponseResult deleteCommissionRule(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getCommissionRuleDetail(Map<String, Object> param) {
        return fallbackResult();
    }

    /** ================= 分销模块 - 分销订单 ================= */

    @Override
    public ResponseResult getDistributionOrderList(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getDistributionOrderDetail(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult updateDistributionOrderStatus(Map<String, Object> param) {
        return fallbackResult();
    }

    /** ================= 分销模块 - 分销商品 ================= */

    @Override
    public ResponseResult getDistributionGoodsList(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getDistributionGoodsDetail(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult createDistributionGoods(Map<String, Object> goods) {
        return fallbackResult();
    }

    @Override
    public ResponseResult updateDistributionGoods(Map<String, Object> goods) {
        return fallbackResult();
    }

    @Override
    public ResponseResult updateDistributionGoodsStatus(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult deleteDistributionGoods(Map<String, Object> param) {
        return fallbackResult();
    }


    /** ================= 返利模块 - 返利规则 ================= */

    @Override
    public ResponseResult getRebateRuleList(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getRebateRuleDetail(Long id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult createRebateRule(Map<String, Object> rule) {
        return fallbackResult();
    }

    @Override
    public ResponseResult updateRebateRule(Map<String, Object> rule) {
        return fallbackResult();
    }

    @Override
    public ResponseResult updateRebateRuleStatus(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult deleteRebateRule(Long id) {
        return fallbackResult();
    }

    /** ================= 返利模块 - 返利记录 ================= */

    @Override
    public ResponseResult getRebateRecordList(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getRebateRecordDetail(Long id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult settleRebateRecord(Long id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult batchSettleRebateRecord(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult cancelRebateRecord(Map<String, Object> param) {
        return fallbackResult();
    }

    /** ================= 返利模块 - 返利调整 ================= */

    @Override
    public ResponseResult getRebateAdjustmentList(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult createRebateAdjustment(Map<String, Object> adjustment) {
        return fallbackResult();
    }

    @Override
    public ResponseResult auditRebateAdjustment(Map<String, Object> param) {
        return fallbackResult();
    }

    /** ================= 返利模块 - 返利发放 ================= */

    @Override
    public ResponseResult getRebatePaymentList(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult createRebatePayment(Map<String, Object> payment) {
        return fallbackResult();
    }

    @Override
    public ResponseResult executeRebatePayment(Long id) {
        return fallbackResult();
    }

    /** ================= 仓储模块 - 库存管理 ================= */

    @Override
    public ResponseResult getStockList(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getStockDetail(Long id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getStocksByWarehouse(Long warehouseId) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getWarningStocks(Long warehouseId) {
        return fallbackResult();
    }

    @Override
    public ResponseResult createStock(Map<String, Object> stock) {
        return fallbackResult();
    }

    @Override
    public ResponseResult updateStock(Map<String, Object> stock) {
        return fallbackResult();
    }

    @Override
    public ResponseResult increaseStock(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult decreaseStock(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult lockStock(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult unlockStock(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult deleteStock(Long id) {
        return fallbackResult();
    }

    /** ================= 仓储模块 - 仓库管理 ================= */

    @Override
    public ResponseResult getWarehouseList(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getEnabledWarehouses(Long shopId) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getWarehouseDetail(Long id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult createWarehouse(Map<String, Object> warehouse) {
        return fallbackResult();
    }

    @Override
    public ResponseResult updateWarehouse(Map<String, Object> warehouse) {
        return fallbackResult();
    }

    @Override
    public ResponseResult setDefaultWarehouse(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult deleteWarehouse(Long id) {
        return fallbackResult();
    }

    /** ================= 仓储模块 - 入库管理 ================= */

    @Override
    public ResponseResult getStockInboundList(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getStockInboundDetail(Long id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult createStockInbound(Map<String, Object> inbound) {
        return fallbackResult();
    }

    @Override
    public ResponseResult confirmStockInbound(Long id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult cancelStockInbound(Long id) {
        return fallbackResult();
    }

    /** ================= 仓储模块 - 出库管理 ================= */

    @Override
    public ResponseResult getStockOutboundList(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getStockOutboundDetail(Long id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult createStockOutbound(Map<String, Object> outbound) {
        return fallbackResult();
    }

    @Override
    public ResponseResult confirmStockOutbound(Long id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult cancelStockOutbound(Long id) {
        return fallbackResult();
    }

    /** ================= 仓储模块 - 库存调拨 ================= */

    @Override
    public ResponseResult getStockTransferList(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getStockTransferDetail(Long id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult createStockTransfer(Map<String, Object> transfer) {
        return fallbackResult();
    }

    @Override
    public ResponseResult auditStockTransfer(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult completeStockTransfer(Long id) {
        return fallbackResult();
    }

    /** ================= 仓储模块 - 库存盘点 ================= */

    @Override
    public ResponseResult getStockInventoryList(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getStockInventoryDetail(Long id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult createStockInventory(Map<String, Object> inventory) {
        return fallbackResult();
    }

    @Override
    public ResponseResult startStockInventory(Long id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult submitStockInventory(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult auditStockInventory(Map<String, Object> param) {
        return fallbackResult();
    }

    /** ================= 风控模块 - 预警规则 ================= */

    @Override
    public ResponseResult getRiskRuleList(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getEnabledRiskRules(Long shopId) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getRiskRuleDetail(Long id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult createRiskRule(Map<String, Object> rule) {
        return fallbackResult();
    }

    @Override
    public ResponseResult updateRiskRule(Map<String, Object> rule) {
        return fallbackResult();
    }

    @Override
    public ResponseResult updateRiskRuleStatus(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult deleteRiskRule(Long id) {
        return fallbackResult();
    }

    /** ================= 风控模块 - 实时监控 ================= */

    @Override
    public ResponseResult getRiskMonitorList(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getRiskMonitorDetail(Long id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getRiskMonitorStatistics(Long shopId) {
        return fallbackResult();
    }

    @Override
    public ResponseResult saveRiskMonitor(Map<String, Object> monitor) {
        return fallbackResult();
    }

    /** ================= 风控模块 - 预警处理 ================= */

    @Override
    public ResponseResult getRiskAlertList(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getRiskAlertDetail(Long id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getRiskAlertPendingCount(Long shopId) {
        return fallbackResult();
    }

    @Override
    public ResponseResult createRiskAlert(Map<String, Object> alert) {
        return fallbackResult();
    }

    @Override
    public ResponseResult handleRiskAlert(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult ignoreRiskAlert(Map<String, Object> param) {
        return fallbackResult();
    }

    /** ================= 首页模块 - 首页聚合数据 ================= */

    @Override
    public ResponseResult getHomeData() {
        return fallbackResult();
    }

    /** ================= 首页模块 - Banner轮播图 ================= */

    @Override
    public ResponseResult getHomeBanners(String position, Integer limit) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getBannerPage(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult addBanner(Map<String, Object> banner) {
        return fallbackResult();
    }

    @Override
    public ResponseResult updateBanner(Map<String, Object> banner) {
        return fallbackResult();
    }

    @Override
    public ResponseResult deleteBanner(Long id) {
        return fallbackResult();
    }

    /** ================= 首页模块 - 公告快报 ================= */

    @Override
    public ResponseResult getHomeNotices(Integer type, Integer limit) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getNoticePage(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult addNotice(Map<String, Object> notice) {
        return fallbackResult();
    }

    @Override
    public ResponseResult updateNotice(Map<String, Object> notice) {
        return fallbackResult();
    }

    @Override
    public ResponseResult deleteNotice(Long id) {
        return fallbackResult();
    }

    /** ================= 首页模块 - 推荐商品 ================= */

    @Override
    public ResponseResult getHomeRecommendGoods(Integer type, String position, Integer limit) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getRecommendGoodsPage(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getRecommendGoodsById(Long id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult addRecommendGoods(Map<String, Object> goods) {
        return fallbackResult();
    }

    @Override
    public ResponseResult updateRecommendGoods(Map<String, Object> goods) {
        return fallbackResult();
    }

    @Override
    public ResponseResult deleteRecommendGoods(Long id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult batchDeleteRecommendGoods(java.util.List<Long> ids) {
        return fallbackResult();
    }

    /** ================= 首页模块 - 楼层配置 ================= */

    @Override
    public ResponseResult getEnabledFloors(@RequestParam(value = "shopId", required = false) Long shopId) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getFloorsByType(String floorType, Long shopId) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getFloorPage(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getFloorById(Long id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult addFloor(Map<String, Object> floor) {
        return fallbackResult();
    }

    @Override
    public ResponseResult updateFloor(Map<String, Object> floor) {
        return fallbackResult();
    }

    @Override
    public ResponseResult deleteFloor(Long id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult updateFloorSort(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult updateFloorStatus(Map<String, Object> param) {
        return fallbackResult();
    }

    /** ================= 店铺管理 ================= */

    @Override
    public ResponseResult getShopList(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getPhysicalStoreList(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getPhysicalStoreMerchantOptions() {
        return fallbackResult();
    }

    @Override
    public ResponseResult getPhysicalStoreAccountOptions(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult createPhysicalStore(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult togglePhysicalStore(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getShopDetail(Long id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult createShop(Map<String, Object> shop) {
        return fallbackResult();
    }

    @Override
    public ResponseResult updateShop(Map<String, Object> shop) {
        return fallbackResult();
    }

    @Override
    public ResponseResult deleteShop(Long id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getShopLevelList() {
        return fallbackResult();
    }

    @Override
    public ResponseResult createShopLevel(Map<String, Object> level) {
        return fallbackResult();
    }

    @Override
    public ResponseResult updateShopLevel(Map<String, Object> level) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getMerchantEntryList(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult auditMerchantEntry(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getInquiryList(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult quoteInquiry(Map<String, Object> inquiry) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getCashOnDeliveryList(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getInvoiceList(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult issueInvoice(Map<String, Object> invoice) {
        return fallbackResult();
    }

    /** ================= 店铺模块 - 质量保证金 ================= */

    @Override
    public ResponseResult getQualityDepositList(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult refundQualityDeposit(Map<String, Object> param) {
        return fallbackResult();
    }

    /** ================= 店铺模块 - 状态和概览 ================= */

    @Override
    public ResponseResult updateShopStatus(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getShopOverview() {
        return fallbackResult();
    }

    /** ================= 财务管理 - 概览 ================= */

    @Override
    public ResponseResult getFinanceOverview() {
        return fallbackResult();
    }

    @Override public ResponseResult getMerchantFinanceCenter() { return fallbackResult(); }
    @Override public ResponseResult getRefundStats(Map<String, Object> param) { return fallbackResult(); }
    @Override public ResponseResult getRefundList(Map<String, Object> param) { return fallbackResult(); }
    @Override public ResponseResult retryRefund(Map<String, Object> param) { return fallbackResult(); }
    @Override public ResponseResult getShopBalanceList(Map<String, Object> param) { return fallbackResult(); }
    @Override public ResponseResult getUserWalletList(Map<String, Object> param) { return fallbackResult(); }
    @Override public ResponseResult getUserRechargeList(Map<String, Object> param) { return fallbackResult(); }

    /** ================= 财务管理 - 提现 ================= */

    @Override
    public ResponseResult getWithdrawList(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getWithdrawDetail(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult auditWithdraw(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getWithdrawStats(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult withdrawCommission(Map<String, Object> param) {
        return fallbackResult();
    }

    /** ================= dcenter 商家管理后台 - 商品管理 ================= */

    @Override
    public ResponseResult getGoodsList(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getGoodsDetail(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult saveGoods(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult addGoodsForImport(Map<String, Object> params) {
        return ResponseResult.fail(503, "商城商品服务暂时不可用");
    }

    @Override
    public ResponseResult deleteGoods(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult batchDeleteGoods(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult batchPutOnSale(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult batchPutOffSale(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult updateGoodsStatus(Map<String, Object> params) {
        return fallbackResult();
    }

    /** ================= dcenter 商家管理后台 - 商品评价 ================= */

    @Override
    public ResponseResult getCommentList(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult auditComment(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult replyComment(Map<String, Object> params) {
        return fallbackResult();
    }

    /** ================= dcenter 商家管理后台 - 会员管理 ================= */

    @Override
    public ResponseResult getMemberList(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getMemberDetail(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult updateMemberLevel(Map<String, Object> params) {
        return fallbackResult();
    }

    /** ================= dcenter 商家管理后台 - 店铺装修 ================= */

    @Override
    public ResponseResult getDecoratePageList(Map<String, Object> params, String authorization) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getDecoratePageDetail(Map<String, Object> params, String authorization) {
        return fallbackResult();
    }

    @Override
    public ResponseResult saveDecoratePage(Map<String, Object> params, String authorization) {
        return fallbackResult();
    }

    @Override
    public ResponseResult deleteDecoratePage(Map<String, Object> params, String authorization) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getDecoratePageTemplates(String authorization) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getDecoratePageTemplate(Map<String, Object> params, String authorization) {
        return fallbackResult();
    }

    @Override
    public ResponseResult applyDecoratePageTemplate(Map<String, Object> params, String authorization) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getHomePageList(Map<String, Object> params, String authorization) {
        return fallbackResult();
    }

    @Override
    public ResponseResult setHomePage(Map<String, Object> params, String authorization) {
        return fallbackResult();
    }

    /** ================= 平台管理后台素材库 ================= */

    @Override
    public ResponseResult adminAlbumListCate(Map<String, Object> params, String authorization) {
        return fallbackResult();
    }

    @Override
    public ResponseResult adminAlbumLists(Map<String, Object> params, String authorization) {
        return fallbackResult();
    }

    @Override
    public ResponseResult adminAlbumAddCate(Map<String, Object> params, String authorization) {
        return fallbackResult();
    }

    @Override
    public ResponseResult adminAlbumEditCate(Map<String, Object> params, String authorization) {
        return fallbackResult();
    }

    @Override
    public ResponseResult adminAlbumDelCate(Map<String, Object> params, String authorization) {
        return fallbackResult();
    }

    @Override
    public ResponseResult adminAlbumDelete(Map<String, Object> params, String authorization) {
        return fallbackResult();
    }

    @Override
    public ResponseResult adminAlbumMove(Map<String, Object> params, String authorization) {
        return fallbackResult();
    }

    @Override
    public ResponseResult adminAlbumRename(Map<String, Object> params, String authorization) {
        return fallbackResult();
    }

    @Override
    public ResponseResult adminAlbumRecord(Map<String, Object> params, String authorization) {
        return fallbackResult();
    }

    /** ================= dcenter 商家管理后台 - 文件管理 ================= */

    @Override
    public ResponseResult getFileCategoryList(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult saveFileCategory(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult deleteFileCategory(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getFileList(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult uploadFile(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult deleteFile(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult moveFile(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult renameFile(Map<String, Object> params) {
        return fallbackResult();
    }

    /** ================= dcenter 商家管理后台 - 系统公告 ================= */

    @Override
    public ResponseResult getAnnouncementList(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getLatestAnnouncement(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getAnnouncementDetail(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult saveAnnouncement(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult deleteAnnouncement(Map<String, Object> params) {
        return fallbackResult();
    }

    /** ================= dcenter 商家管理后台 - 客服配置 ================= */

    @Override
    public ResponseResult getServiceConfig(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult saveServiceConfig(Map<String, Object> params) {
        return fallbackResult();
    }

    /** ================= dcenter 商家管理后台 - 消息通知设置 ================= */

    @Override
    public ResponseResult getMessageSettingList(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getMessageSettingDetail(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult saveMessageSetting(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult deleteMessageSetting(Map<String, Object> params) {
        return fallbackResult();
    }

    /** ================= dcenter 商家管理后台 - 集采业务 ================= */

    @Override
    public ResponseResult getGatherList(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getGatherDetail(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult saveGather(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult deleteGather(Map<String, Object> params) {
        return fallbackResult();
    }

    /** ================= 装修系统 ================= */

    @Override
    public ResponseResult getTabbarDetail(Map<String, Object> params, String authorization) {
        return fallbackResult();
    }

    @Override
    public ResponseResult saveTabbar(Map<String, Object> params, String authorization) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getMallStyle(Map<String, Object> params, String authorization) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getMallStyleConfig(String authorization) {
        return ResponseResult.success(MallStyleDefaults.all());
    }

    @Override
    public ResponseResult setMallStyle(Map<String, Object> params, String authorization) {
        return fallbackResult();
    }

    @Override
    public ResponseResult setMallStyleConfig(Map<String, Object> params, String authorization) {
        return fallbackResult();
    }

    @Override
    public ResponseResult addDecoratePage(Map<String, Object> params, String authorization) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getDecorateArticleData(Map<String, Object> params, String authorization) {
        return fallbackResult();
    }

    /** ================= 店铺菜单 - 启用列表 ================= */

    @Override
    public ResponseResult getEnabledShopMenuList() {
        return fallbackResult();
    }

    /** ================= 店铺商品分类 ================= */

    @Override
    public ResponseResult getShopCategoryList(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getShopCategoryDetail(Long id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult createShopCategory(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult updateShopCategory(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult deleteShopCategory(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult updateShopCategoryStatus(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getEnabledShopCategoryList() {
        return fallbackResult();
    }

    /** ================= 店铺商品菜单 ================= */

    @Override
    public ResponseResult getShopMenuList() {
        return fallbackResult();
    }

    @Override
    public ResponseResult getShopMenuDetail(Long id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult createShopMenu(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult updateShopMenu(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult deleteShopMenu(Map<String, Object> param) {
        return fallbackResult();
    }

    @Override
    public ResponseResult updateShopMenuStatus(Map<String, Object> param) {
        return fallbackResult();
    }

    /** ================= 前台首页数据 ================= */

    @Override
    public ResponseResult getFrontIndexData(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getFrontDecorate(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getFrontIndexConfig() {
        return fallbackResult();
    }

    @Override
    public ResponseResult getFrontIndexCategory() {
        return fallbackResult();
    }

    @Override
    public ResponseResult getFrontHotSearch() {
        return fallbackResult();
    }

    @Override
    public ResponseResult frontShopList(Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult frontShopDetail(Long id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult frontShopCategory(Long id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult frontCommentList(Map<String, Object> params) {
        return fallbackResult();
    }

    /** ================= setting/dict_type 字典类型 ================= */

    @Override
    public ResponseResult allDictType() {
        return fallbackResult();
    }

    @Override
    public ResponseResult listsDictType(Map<String, String> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult detailDictType(Integer id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult addDictType(Map<String, Object> body) {
        return fallbackResult();
    }

    @Override
    public ResponseResult editDictType(Map<String, Object> body) {
        return fallbackResult();
    }

    @Override
    public ResponseResult delDictType(Map<String, Object> body) {
        return fallbackResult();
    }

    /** ================= setting/dict_data 字典数据 ================= */

    @Override
    public ResponseResult allDictData(Map<String, String> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult listsDictData(Map<String, String> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult detailDictData(Integer id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult addDictData(Map<String, Object> body) {
        return fallbackResult();
    }

    @Override
    public ResponseResult editDictData(Map<String, Object> body) {
        return fallbackResult();
    }

    @Override
    public ResponseResult delDictData(Map<String, Object> body) {
        return fallbackResult();
    }

    /** ================= setting/pay_config 支付配置 ================= */

    @Override
    public ResponseResult listsPayConfig() {
        return fallbackResult();
    }

    @Override
    public ResponseResult detailPayConfig(Integer id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult editPayConfig(Map<String, Object> body) {
        return fallbackResult();
    }

    /** ================= setting/pay_way 支付方式（B1-4）================= */

    @Override
    public ResponseResult listsPayWay() {
        return fallbackResult();
    }

    @Override
    public ResponseResult editPayWay(Map<String, Object> body) {
        return fallbackResult();
    }

    /** ================= setting/storage 存储配置（B1-5）================= */

    @Override
    public ResponseResult listsStorage() {
        return fallbackResult();
    }

    @Override
    public ResponseResult detailStorage(String engine) {
        return fallbackResult();
    }

    @Override
    public ResponseResult setupStorage(Map<String, Object> body) {
        return fallbackResult();
    }

    @Override
    public ResponseResult changeStorage(Map<String, Object> body) {
        return fallbackResult();
    }

    /** ================= setting/sms 短信配置（B1-6）================= */

    @Override
    public ResponseResult listsSms() {
        return fallbackResult();
    }

    @Override
    public ResponseResult detailSms(String type) {
        return fallbackResult();
    }

    @Override
    public ResponseResult editSms(Map<String, Object> body) {
        return fallbackResult();
    }

    /** ================= setting/notice 通知设置（B1-7）================= */

    @Override
    public ResponseResult listsNotice(Integer recipient) {
        return fallbackResult();
    }

    @Override
    public ResponseResult detailNotice(Integer id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult editNotice(Map<String, Object> body) {
        return fallbackResult();
    }

    /** ================= setting/order 订单设置（B1-8）================= */

    @Override
    public ResponseResult getOrderTransaction() {
        return fallbackResult();
    }

    @Override
    public ResponseResult saveOrderTransaction(Map<String, Object> body) {
        return fallbackResult();
    }

    @Override
    public ResponseResult getOrderSettlement() {
        return fallbackResult();
    }

    @Override
    public ResponseResult saveOrderSettlement(Map<String, Object> body) {
        return fallbackResult();
    }

    /** ================= setting/search 热门搜索（B1-9）================= */

    @Override
    public ResponseResult listsSearch() {
        return fallbackResult();
    }

    @Override
    public ResponseResult editSearch(Map<String, Object> body) {
        return fallbackResult();
    }

    /** ================= setting/user 用户设置（B1-10）================= */

    @Override
    public ResponseResult listsUser() {
        return fallbackResult();
    }

    @Override
    public ResponseResult editUser(Map<String, Object> body) {
        return fallbackResult();
    }

    @Override
    public ResponseResult listsUserLogin() {
        return fallbackResult();
    }

    @Override
    public ResponseResult editUserLogin(Map<String, Object> body) {
        return fallbackResult();
    }

    /** ================= setting/website 网站设置（B1-11）================= */

    @Override
    public ResponseResult listsWebsite() {
        return fallbackResult();
    }

    @Override
    public ResponseResult editWebsite(Map<String, Object> body) {
        return fallbackResult();
    }

    @Override
    public ResponseResult listsWebsiteCopyright() {
        return fallbackResult();
    }

    @Override
    public ResponseResult editWebsiteCopyright(Map<String, Object> body) {
        return fallbackResult();
    }

    @Override
    public ResponseResult listsWebsiteAgreement() {
        return fallbackResult();
    }

    @Override
    public ResponseResult editWebsiteAgreement(Map<String, Object> body) {
        return fallbackResult();
    }

    @Override
    public ResponseResult listsWebsiteStatistics() {
        return fallbackResult();
    }

    @Override
    public ResponseResult editWebsiteStatistics(Map<String, Object> body) {
        return fallbackResult();
    }

    /** ================= 管理员认证 ================= */

    @Override
    public ResponseResult mallLogin(Map<String, Object> param) {
        return ResponseResult.error("Mall认证服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult mallGetUserInfo(String token) {
        return ResponseResult.error("Mall认证服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult mallGetMenus(String token) {
        return ResponseResult.error("Mall认证服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult mallLogout(String token) {
        return ResponseResult.error("Mall认证服务暂时不可用，请稍后重试");
    }

  @Override
    public ResponseResult mallPermMenuList(String token, Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult mallPermMenuAll(String token, Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult mallPermMenuTree(String token, Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult mallPermMenuDetail(String token, Integer id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult mallPermMenuAdd(String token, Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult mallPermMenuEdit(String token, Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult mallPermMenuDelete(String token, Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult mallPermRoleAll(String token) {
        return fallbackResult();
    }

    @Override
    public ResponseResult mallPermRoleLists(String token, Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult mallPermRoleDetail(String token, Integer id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult mallPermRoleAdd(String token, Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult mallPermRoleEdit(String token, Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult mallPermRoleDelete(String token, Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult mallPermAdminAll(String token) {
        return fallbackResult();
    }

    @Override
    public ResponseResult mallPermAdminLists(String token, Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult mallPermAdminDetail(String token, Integer id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult mallPermAdminAdd(String token, Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult mallPermAdminEdit(String token, Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult mallPermAdminDelete(String token, Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult mallOrgDeptAll(String token) {
        return fallbackResult();
    }

    @Override
    public ResponseResult mallOrgDeptLists(String token, Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult mallOrgDeptDetail(String token, Integer id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult mallOrgDeptAdd(String token, Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult mallOrgDeptEdit(String token, Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult mallOrgDeptDelete(String token, Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult mallOrgJobsAll(String token, Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult mallOrgJobsLists(String token, Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult mallOrgJobsDetail(String token, Integer id) {
        return fallbackResult();
    }

    @Override
    public ResponseResult mallOrgJobsAdd(String token, Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult mallOrgJobsEdit(String token, Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult mallOrgJobsDelete(String token, Map<String, Object> params) {
        return fallbackResult();
    }

    @Override
    public ResponseResult frontLegacyHomeGet() {
        return fallbackResult();
    }

    @Override
    public ResponseResult frontLegacyHomePost() {
        return fallbackResult();
    }
}
