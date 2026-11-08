package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

/**
 * Mall商城服务Feign客户端
/**
 * <p>
 * 生产同机部署建议配置 {@code feign.mall.url=http://127.0.0.1:8220}，
 * 避免 Nacos 存在不可达 mall 实例时登录/业务接口随机超时或 11006。
 * @author Amos
 */
@FeignClient(name = "mall", contextId = "mallServiceFeign", url = "${feign.mall.url:}",
        fallback = MallServiceFeignFallback.class)
public interface MallServiceFeign {

    // ================= Dashboard聚合 =================

    /**
     * 获取商城概览统计数据
     */
    // ================= 商品/订单代理（MALL_ADMIN_TOKEN，见 paas/mall） =================

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/goods/list")
    ResponseResult mallAdminGoodsList(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/mall/goods/category/tree")
    ResponseResult mallAdminGoodsCategoryTree(@RequestBody(required = false) Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/order/manage/list")
    ResponseResult mallAdminOrderManageList(@RequestBody Map<String, Object> params);

    /** 后台订单详情：mall 侧做 order 详情契约适配（2026-10-31 后台订单详情打通）。 */
    @PostMapping("/mall/order/manage/detail")
    ResponseResult mallAdminOrderManageDetail(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/mall/dashboard/overview")
    ResponseResult getDashboardOverview();

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/mall/dashboard/workbench")
    ResponseResult getDashboardWorkbench();

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/dashboard/check-version")
    ResponseResult getDashboardCheckVersion(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/dashboard/check-legal")
    ResponseResult getDashboardCheckLegal(@RequestBody Map<String, Object> params);

    /**
     * 获取实时监控数据
     */
    @PostMapping("/mall/dashboard/realtime")
    ResponseResult getDashboardRealtime();

    /**
     * 获取销售趋势数据
     */
    @PostMapping("/mall/dashboard/sales-trend")
    ResponseResult getDashboardSalesTrend(@RequestBody Map<String, Object> params);

    /**
     * 获取商品佣金排行
     */
    @PostMapping("/mall/dashboard/goods-rank")
    ResponseResult getDashboardGoodsRank(@RequestBody Map<String, Object> params);

    /**
     * 获取快捷操作待处理数据
     */
    @PostMapping("/mall/dashboard/quick-actions")
    ResponseResult getDashboardQuickActions();

    // ================= 客户管理 =================

    /**
     * 分页查询客户列表
     */
    @PostMapping("/mall/customer/list")
    ResponseResult getCustomerList(@RequestBody Map<String, Object> params);

    /**
     * 获取客户详情
     */
    @PostMapping("/mall/customer/detail")
    ResponseResult getCustomerDetail(@RequestBody Map<String, Object> params);

    /**
     * 保存或更新客户
     */
    @PostMapping("/mall/customer/save")
    ResponseResult saveCustomer(@RequestBody Map<String, Object> params);

    /**
     * 删除客户
     */
    @PostMapping("/mall/customer/delete")
    ResponseResult deleteCustomer(@RequestBody Map<String, Object> params);

    /**
     * 获取客户分组列表
     */
    @PostMapping("/mall/customer/group/list")
    ResponseResult getCustomerGroupList();

    /**
     * 保存客户分组
     */
    @PostMapping("/mall/customer/group/save")
    ResponseResult saveCustomerGroup(@RequestBody Map<String, Object> params);

    /**
     * 删除客户分组
     */
    @PostMapping("/mall/customer/group/delete")
    ResponseResult deleteCustomerGroup(@RequestBody Map<String, Object> params);

    /**
     * 获取客户标签列表
     */
    @PostMapping("/mall/customer/tag/list")
    ResponseResult getCustomerTagList();

    /**
     * 保存客户标签
     */
    @PostMapping("/mall/customer/tag/save")
    ResponseResult saveCustomerTag(@RequestBody Map<String, Object> params);

    /**
     * 删除客户标签
     */
    @PostMapping("/mall/customer/tag/delete")
    ResponseResult deleteCustomerTag(@RequestBody Map<String, Object> params);

    /**
     * 分页查询客户跟进记录
     */
    @PostMapping("/mall/customer/follow/list")
    ResponseResult getCustomerFollowList(@RequestBody Map<String, Object> params);

    /**
     * 添加客户跟进记录
     */
    @PostMapping("/mall/customer/follow/add")
    ResponseResult addCustomerFollow(@RequestBody Map<String, Object> params);

    /**
     * 删除客户跟进记录
     */
    @PostMapping("/mall/customer/follow/delete")
    ResponseResult deleteCustomerFollow(@RequestBody Map<String, Object> params);

    /**
     * 获取客户统计数据
     */
    @PostMapping("/mall/customer/statistics")
    ResponseResult getCustomerStatistics(@RequestBody Map<String, Object> params);

    // ================= 积分商城 =================

    /** 积分商品列表 */
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/points-tool/list")
    ResponseResult getPointsToolList(@RequestBody Map<String, Object> params);

    /** 积分商品详情 */
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/points-tool/detail")
    ResponseResult getPointsToolDetail(@RequestBody Map<String, Object> params);

    /** 创建积分商品 */
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/points-tool/create")
    ResponseResult createPointsTool(@RequestBody Map<String, Object> params);

    /** 更新积分商品 */
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/points-tool/update")
    ResponseResult updatePointsTool(@RequestBody Map<String, Object> params);

    /** 删除积分商品 */
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/points-tool/delete")
    ResponseResult deletePointsTool(@RequestBody Map<String, Object> params);

    /** 切换积分商品状态 */
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/points-tool/toggle-status")
    ResponseResult togglePointsToolStatus(@RequestBody Map<String, Object> params);

    // ================= 直播互动 =================

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/live/list")
    ResponseResult getLiveList(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/live/detail")
    ResponseResult getLiveDetail(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/live/create")
    ResponseResult createLive(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/live/update")
    ResponseResult updateLive(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/live/delete")
    ResponseResult deleteLive(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/live/start")
    ResponseResult startLive(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/live/end")
    ResponseResult endLive(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/live/product/audit/list")
    ResponseResult getLiveProductAuditList(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/live/product/audit/detail")
    ResponseResult getLiveProductAuditDetail(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/live/product/audit/submit")
    ResponseResult submitLiveProductAudit(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/live/account/list")
    ResponseResult getLiveAccountList(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/live/account/detail")
    ResponseResult getLiveAccountDetail(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/live/account/create")
    ResponseResult createLiveAccount(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/live/account/update")
    ResponseResult updateLiveAccount(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/live/account/delete")
    ResponseResult deleteLiveAccount(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/live/account/toggle-status")
    ResponseResult toggleLiveAccountStatus(@RequestBody Map<String, Object> params);

    // ================= 发现内容 - 话题分类 =================

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/content/topic/category/list")
    ResponseResult getTopicCategoryList(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/content/topic/category/detail")
    ResponseResult getTopicCategoryDetail(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/content/topic/category/create")
    ResponseResult createTopicCategory(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/content/topic/category/update")
    ResponseResult updateTopicCategory(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/content/topic/category/delete")
    ResponseResult deleteTopicCategory(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/content/topic/category/toggle-status")
    ResponseResult toggleTopicCategoryStatus(@RequestBody Map<String, Object> params);

    // ================= 发现内容 - 选题 =================

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/content/selection/list")
    ResponseResult getSelectionList(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/content/selection/detail")
    ResponseResult getSelectionDetail(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/content/selection/create")
    ResponseResult createSelection(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/content/selection/update")
    ResponseResult updateSelection(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/content/selection/delete")
    ResponseResult deleteSelection(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/content/selection/publish")
    ResponseResult publishSelection(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/content/selection/offline")
    ResponseResult offlineSelection(@RequestBody Map<String, Object> params);

    // ================= 发现内容 - 心得 =================

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/content/mood/list")
    ResponseResult getMoodList(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/content/mood/detail")
    ResponseResult getMoodDetail(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/content/mood/audit")
    ResponseResult auditMood(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/content/mood/delete")
    ResponseResult deleteMood(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/content/mood/offline")
    ResponseResult offlineMood(@RequestBody Map<String, Object> params);

    // ================= 发现内容 - 评论 =================

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/content/review/list")
    ResponseResult getReviewList(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/content/review/detail")
    ResponseResult getReviewDetail(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/content/review/audit")
    ResponseResult auditReview(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/content/review/delete")
    ResponseResult deleteReview(@RequestBody Map<String, Object> params);

    // ================= 分销模块 - 分销员 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/distribution/distributor/list")
    ResponseResult getDistributorList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/distribution/distributor/detail")
    ResponseResult getDistributorDetail(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param distributor TODO
     * @return TODO
     */
    @PostMapping("/mall/distribution/distributor/apply")
    ResponseResult applyDistributor(@RequestBody Map<String, Object> distributor);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/distribution/distributor/audit")
    ResponseResult auditDistributor(@RequestBody Map<String, Object> param);

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/distribution/distributor/status")
    ResponseResult updateDistributorStatus(@RequestBody Map<String, Object> param);

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/distribution/distributor/update")
    ResponseResult updateDistributor(@RequestBody Map<String, Object> param);

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/distribution/distributor/delete")
    ResponseResult deleteDistributor(@RequestBody Map<String, Object> param);

    // ================= 分销模块 - 分销等级 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/distribution/level/list")
    ResponseResult getDistributorLevelList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/distribution/level/all")
    ResponseResult getAllDistributorLevels(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param level TODO
     * @return TODO
     */
    @PostMapping("/mall/distribution/level/create")
    ResponseResult createDistributorLevel(@RequestBody Map<String, Object> level);
    
    /**
     * TODO 补充方法说明
/**
     * @param level TODO
     * @return TODO
     */
    @PostMapping("/mall/distribution/level/update")
    ResponseResult updateDistributorLevel(@RequestBody Map<String, Object> level);

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/distribution/level/delete")
    ResponseResult deleteDistributorLevel(@RequestBody Map<String, Object> param);

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/distribution/level/detail")
    ResponseResult getDistributorLevelDetail(@RequestBody Map<String, Object> param);

    // ================= 分销模块 - 佣金记录 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/distribution/commission/list")
    ResponseResult getCommissionRecordList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/distribution/commission/detail")
    ResponseResult getCommissionRecordDetail(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/distribution/commission/settle")
    ResponseResult settleCommission(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/distribution/commission/batchSettle")
    ResponseResult batchSettleCommission(@RequestBody Map<String, Object> param);

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/distribution/commission/stats")
    ResponseResult getCommissionStats(@RequestBody Map<String, Object> param);

    // ================= 分销模块 - 佣金规则 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/distribution/rule/list")
    ResponseResult getCommissionRuleList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param rule TODO
     * @return TODO
     */
    @PostMapping("/mall/distribution/rule/create")
    ResponseResult createCommissionRule(@RequestBody Map<String, Object> rule);
    
    /**
     * TODO 补充方法说明
/**
     * @param rule TODO
     * @return TODO
     */
    @PostMapping("/mall/distribution/rule/update")
    ResponseResult updateCommissionRule(@RequestBody Map<String, Object> rule);

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/distribution/rule/delete")
    ResponseResult deleteCommissionRule(@RequestBody Map<String, Object> param);

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/distribution/rule/detail")
    ResponseResult getCommissionRuleDetail(@RequestBody Map<String, Object> param);

    // ================= 分销模块 - 分销订单 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/distribution/order/list")
    ResponseResult getDistributionOrderList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/distribution/order/detail")
    ResponseResult getDistributionOrderDetail(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/distribution/order/status")
    ResponseResult updateDistributionOrderStatus(@RequestBody Map<String, Object> param);

    // ================= 分销模块 - 分销商品 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/distribution/goods/list")
    ResponseResult getDistributionGoodsList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/distribution/goods/detail")
    ResponseResult getDistributionGoodsDetail(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param goods TODO
     * @return TODO
     */
    @PostMapping("/mall/distribution/goods/create")
    ResponseResult createDistributionGoods(@RequestBody Map<String, Object> goods);
    
    /**
     * TODO 补充方法说明
/**
     * @param goods TODO
     * @return TODO
     */
    @PostMapping("/mall/distribution/goods/update")
    ResponseResult updateDistributionGoods(@RequestBody Map<String, Object> goods);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/distribution/goods/status")
    ResponseResult updateDistributionGoodsStatus(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/distribution/goods/delete")
    ResponseResult deleteDistributionGoods(@RequestBody Map<String, Object> param);

    // ================= 返利模块 - 返利规则 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/rebate/rule/list")
    ResponseResult getRebateRuleList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/rebate/rule/detail/{id}")
    ResponseResult getRebateRuleDetail(@PathVariable("id") Long id);
    
    /**
     * TODO 补充方法说明
/**
     * @param rule TODO
     * @return TODO
     */
    @PostMapping("/rebate/rule/create")
    ResponseResult createRebateRule(@RequestBody Map<String, Object> rule);
    
    /**
     * TODO 补充方法说明
/**
     * @param rule TODO
     * @return TODO
     */
    @PostMapping("/rebate/rule/update")
    ResponseResult updateRebateRule(@RequestBody Map<String, Object> rule);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/rebate/rule/status")
    ResponseResult updateRebateRuleStatus(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/rebate/rule/delete/{id}")
    ResponseResult deleteRebateRule(@PathVariable("id") Long id);

    // ================= 返利模块 - 返利记录 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/rebate/record/list")
    ResponseResult getRebateRecordList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/rebate/record/detail/{id}")
    ResponseResult getRebateRecordDetail(@PathVariable("id") Long id);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/rebate/record/settle/{id}")
    ResponseResult settleRebateRecord(@PathVariable("id") Long id);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/rebate/record/batchSettle")
    ResponseResult batchSettleRebateRecord(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/rebate/record/cancel")
    ResponseResult cancelRebateRecord(@RequestBody Map<String, Object> param);

    // ================= 返利模块 - 返利调整 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/rebate/adjustment/list")
    ResponseResult getRebateAdjustmentList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param adjustment TODO
     * @return TODO
     */
    @PostMapping("/rebate/adjustment/create")
    ResponseResult createRebateAdjustment(@RequestBody Map<String, Object> adjustment);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/rebate/adjustment/audit")
    ResponseResult auditRebateAdjustment(@RequestBody Map<String, Object> param);

    // ================= 返利模块 - 返利发放 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/rebate/payment/list")
    ResponseResult getRebatePaymentList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param payment TODO
     * @return TODO
     */
    @PostMapping("/rebate/payment/create")
    ResponseResult createRebatePayment(@RequestBody Map<String, Object> payment);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/rebate/payment/execute/{id}")
    ResponseResult executeRebatePayment(@PathVariable("id") Long id);

    // ================= 仓储模块 - 库存管理 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/storage/stock/list")
    ResponseResult getStockList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/storage/stock/detail/{id}")
    ResponseResult getStockDetail(@PathVariable("id") Long id);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/storage/stock/warehouse/{warehouseId}")
    ResponseResult getStocksByWarehouse(@PathVariable("warehouseId") Long warehouseId);
    
    /**
     * TODO 补充方法说明
/**
     * @param "warehouseId" TODO
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/storage/stock/warning")
    ResponseResult getWarningStocks(@RequestParam(value = "warehouseId", required = false) Long warehouseId);
    
    /**
     * TODO 补充方法说明
/**
     * @param stock TODO
     * @return TODO
     */
    @PostMapping("/storage/stock/create")
    ResponseResult createStock(@RequestBody Map<String, Object> stock);
    
    /**
     * TODO 补充方法说明
/**
     * @param stock TODO
     * @return TODO
     */
    @PostMapping("/storage/stock/update")
    ResponseResult updateStock(@RequestBody Map<String, Object> stock);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/storage/stock/increase")
    ResponseResult increaseStock(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/storage/stock/decrease")
    ResponseResult decreaseStock(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/storage/stock/lock")
    ResponseResult lockStock(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/storage/stock/unlock")
    ResponseResult unlockStock(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/storage/stock/delete/{id}")
    ResponseResult deleteStock(@PathVariable("id") Long id);

    // ================= 仓储模块 - 仓库管理 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/storage/warehouse/list")
    ResponseResult getWarehouseList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param "shopId" TODO
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/storage/warehouse/enabled")
    ResponseResult getEnabledWarehouses(@RequestParam(value = "shopId", required = false) Long shopId);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/storage/warehouse/detail/{id}")
    ResponseResult getWarehouseDetail(@PathVariable("id") Long id);
    
    /**
     * TODO 补充方法说明
/**
     * @param warehouse TODO
     * @return TODO
     */
    @PostMapping("/storage/warehouse/create")
    ResponseResult createWarehouse(@RequestBody Map<String, Object> warehouse);
    
    /**
     * TODO 补充方法说明
/**
     * @param warehouse TODO
     * @return TODO
     */
    @PostMapping("/storage/warehouse/update")
    ResponseResult updateWarehouse(@RequestBody Map<String, Object> warehouse);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/storage/warehouse/setDefault")
    ResponseResult setDefaultWarehouse(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/storage/warehouse/delete/{id}")
    ResponseResult deleteWarehouse(@PathVariable("id") Long id);

    // ================= 仓储模块 - 入库管理 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/storage/inbound/list")
    ResponseResult getStockInboundList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/storage/inbound/detail/{id}")
    ResponseResult getStockInboundDetail(@PathVariable("id") Long id);
    
    /**
     * TODO 补充方法说明
/**
     * @param inbound TODO
     * @return TODO
     */
    @PostMapping("/storage/inbound/create")
    ResponseResult createStockInbound(@RequestBody Map<String, Object> inbound);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/storage/inbound/confirm/{id}")
    ResponseResult confirmStockInbound(@PathVariable("id") Long id);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/storage/inbound/cancel/{id}")
    ResponseResult cancelStockInbound(@PathVariable("id") Long id);

    // ================= 仓储模块 - 出库管理 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/storage/outbound/list")
    ResponseResult getStockOutboundList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/storage/outbound/detail/{id}")
    ResponseResult getStockOutboundDetail(@PathVariable("id") Long id);
    
    /**
     * TODO 补充方法说明
/**
     * @param outbound TODO
     * @return TODO
     */
    @PostMapping("/storage/outbound/create")
    ResponseResult createStockOutbound(@RequestBody Map<String, Object> outbound);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/storage/outbound/confirm/{id}")
    ResponseResult confirmStockOutbound(@PathVariable("id") Long id);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/storage/outbound/cancel/{id}")
    ResponseResult cancelStockOutbound(@PathVariable("id") Long id);

    // ================= 仓储模块 - 库存调拨 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/storage/transfer/list")
    ResponseResult getStockTransferList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/storage/transfer/detail/{id}")
    ResponseResult getStockTransferDetail(@PathVariable("id") Long id);
    
    /**
     * TODO 补充方法说明
/**
     * @param transfer TODO
     * @return TODO
     */
    @PostMapping("/storage/transfer/create")
    ResponseResult createStockTransfer(@RequestBody Map<String, Object> transfer);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/storage/transfer/audit")
    ResponseResult auditStockTransfer(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/storage/transfer/complete/{id}")
    ResponseResult completeStockTransfer(@PathVariable("id") Long id);

    // ================= 仓储模块 - 库存盘点 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/storage/inventory/list")
    ResponseResult getStockInventoryList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/storage/inventory/detail/{id}")
    ResponseResult getStockInventoryDetail(@PathVariable("id") Long id);
    
    /**
     * TODO 补充方法说明
/**
     * @param inventory TODO
     * @return TODO
     */
    @PostMapping("/storage/inventory/create")
    ResponseResult createStockInventory(@RequestBody Map<String, Object> inventory);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/storage/inventory/start/{id}")
    ResponseResult startStockInventory(@PathVariable("id") Long id);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/storage/inventory/submit")
    ResponseResult submitStockInventory(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/storage/inventory/audit")
    ResponseResult auditStockInventory(@RequestBody Map<String, Object> param);

    // ================= 风控模块 - 预警规则 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/risk/rule/list")
    ResponseResult getRiskRuleList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param "shopId" TODO
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/risk/rule/enabled")
    ResponseResult getEnabledRiskRules(@RequestParam(value = "shopId", required = false) Long shopId);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/risk/rule/detail/{id}")
    ResponseResult getRiskRuleDetail(@PathVariable("id") Long id);
    
    /**
     * TODO 补充方法说明
/**
     * @param rule TODO
     * @return TODO
     */
    @PostMapping("/risk/rule/create")
    ResponseResult createRiskRule(@RequestBody Map<String, Object> rule);
    
    /**
     * TODO 补充方法说明
/**
     * @param rule TODO
     * @return TODO
     */
    @PostMapping("/risk/rule/update")
    ResponseResult updateRiskRule(@RequestBody Map<String, Object> rule);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/risk/rule/status")
    ResponseResult updateRiskRuleStatus(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/risk/rule/delete/{id}")
    ResponseResult deleteRiskRule(@PathVariable("id") Long id);

    // ================= 风控模块 - 实时监控 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/risk/monitor/list")
    ResponseResult getRiskMonitorList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/risk/monitor/detail/{id}")
    ResponseResult getRiskMonitorDetail(@PathVariable("id") Long id);
    
    /**
     * TODO 补充方法说明
/**
     * @param "shopId" TODO
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/risk/monitor/statistics")
    ResponseResult getRiskMonitorStatistics(@RequestParam(value = "shopId", required = false) Long shopId);
    
    /**
     * TODO 补充方法说明
/**
     * @param monitor TODO
     * @return TODO
     */
    @PostMapping("/risk/monitor/save")
    ResponseResult saveRiskMonitor(@RequestBody Map<String, Object> monitor);

    // ================= 风控模块 - 预警处理 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/risk/alert/list")
    ResponseResult getRiskAlertList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/risk/alert/detail/{id}")
    ResponseResult getRiskAlertDetail(@PathVariable("id") Long id);
    
    /**
     * TODO 补充方法说明
/**
     * @param "shopId" TODO
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/risk/alert/pendingCount")
    ResponseResult getRiskAlertPendingCount(@RequestParam(value = "shopId", required = false) Long shopId);
    
    /**
     * TODO 补充方法说明
/**
     * @param alert TODO
     * @return TODO
     */
    @PostMapping("/risk/alert/create")
    ResponseResult createRiskAlert(@RequestBody Map<String, Object> alert);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/risk/alert/handle")
    ResponseResult handleRiskAlert(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/risk/alert/ignore")
    ResponseResult ignoreRiskAlert(@RequestBody Map<String, Object> param);

    // ================= 首页模块 - 首页聚合数据 =================
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/home/data")
    ResponseResult getHomeData();

    // ================= 首页模块 - Banner轮播图 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param "position" TODO
     * @param "home" TODO
     * @return TODO
     */
    @GetMapping("/mall/home/banner/list")
    ResponseResult getHomeBanners(@RequestParam(value = "position", defaultValue = "home") String position,
                                   @RequestParam(value = "limit", defaultValue = "5") Integer limit);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/home/banner/page")
    ResponseResult getBannerPage(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param banner TODO
     * @return TODO
     */
    @PostMapping("/mall/home/banner/add")
    ResponseResult addBanner(@RequestBody Map<String, Object> banner);
    
    /**
     * TODO 补充方法说明
/**
     * @param banner TODO
     * @return TODO
     */
    @PostMapping("/mall/home/banner/update")
    ResponseResult updateBanner(@RequestBody Map<String, Object> banner);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/mall/home/banner/delete/{id}")
    ResponseResult deleteBanner(@PathVariable("id") Long id);

    // ================= 首页模块 - 公告快报 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param "type" TODO
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/mall/home/notice/list")
    ResponseResult getHomeNotices(@RequestParam(value = "type", required = false) Integer type,
                                   @RequestParam(value = "limit", defaultValue = "5") Integer limit);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/home/notice/page")
    ResponseResult getNoticePage(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param notice TODO
     * @return TODO
     */
    @PostMapping("/mall/home/notice/add")
    ResponseResult addNotice(@RequestBody Map<String, Object> notice);
    
    /**
     * TODO 补充方法说明
/**
     * @param notice TODO
     * @return TODO
     */
    @PostMapping("/mall/home/notice/update")
    ResponseResult updateNotice(@RequestBody Map<String, Object> notice);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/mall/home/notice/delete/{id}")
    ResponseResult deleteNotice(@PathVariable("id") Long id);

    // ================= 首页模块 - 推荐商品 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param "type" TODO
     * @param "1" TODO
     * @return TODO
     */
    @GetMapping("/mall/home/recommend/list")
    ResponseResult getHomeRecommendGoods(@RequestParam(value = "type", defaultValue = "1") Integer type,
                                          @RequestParam(value = "position", defaultValue = "home") String position,
                                          @RequestParam(value = "limit", defaultValue = "10") Integer limit);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/home/recommend/page")
    ResponseResult getRecommendGoodsPage(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/home/recommend/{id}")
    ResponseResult getRecommendGoodsById(@PathVariable("id") Long id);
    
    /**
     * TODO 补充方法说明
/**
     * @param goods TODO
     * @return TODO
     */
    @PostMapping("/mall/home/recommend/add")
    ResponseResult addRecommendGoods(@RequestBody Map<String, Object> goods);
    
    /**
     * TODO 补充方法说明
/**
     * @param goods TODO
     * @return TODO
     */
    @PostMapping("/mall/home/recommend/update")
    ResponseResult updateRecommendGoods(@RequestBody Map<String, Object> goods);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/mall/home/recommend/delete/{id}")
    ResponseResult deleteRecommendGoods(@PathVariable("id") Long id);
    
    /**
     * TODO 补充方法说明
/**
     * @param ids TODO
     * @return TODO
     */
    @PostMapping("/mall/home/recommend/batch/delete")
    ResponseResult batchDeleteRecommendGoods(@RequestBody java.util.List<Long> ids);

    // ================= 首页模块 - 楼层配置 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param "shopId" TODO
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/mall/home/floor/enabled")
    ResponseResult getEnabledFloors(@RequestParam(value = "shopId", required = false) Long shopId);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/home/floor/type/{floorType}")
    ResponseResult getFloorsByType(@PathVariable("floorType") String floorType,
                                    @RequestParam(value = "shopId", required = false) Long shopId);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/home/floor/page")
    ResponseResult getFloorPage(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/home/floor/{id}")
    ResponseResult getFloorById(@PathVariable("id") Long id);
    
    /**
     * TODO 补充方法说明
/**
     * @param floor TODO
     * @return TODO
     */
    @PostMapping("/mall/home/floor/add")
    ResponseResult addFloor(@RequestBody Map<String, Object> floor);
    
    /**
     * TODO 补充方法说明
/**
     * @param floor TODO
     * @return TODO
     */
    @PostMapping("/mall/home/floor/update")
    ResponseResult updateFloor(@RequestBody Map<String, Object> floor);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/mall/home/floor/delete/{id}")
    ResponseResult deleteFloor(@PathVariable("id") Long id);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/home/floor/sort")
    ResponseResult updateFloorSort(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/home/floor/status")
    ResponseResult updateFloorStatus(@RequestBody Map<String, Object> param);

    // ================= 店铺管理 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/shop/list")
    ResponseResult getShopList(@RequestBody Map<String, Object> param);

    /** 真正的门店主数据列表（商家一对多门店）。 */
    @PostMapping("/mall/store/list")
    ResponseResult getPhysicalStoreList(@RequestBody Map<String, Object> param);

    /** 门店表单使用的商家选项。 */
    @PostMapping("/mall/store/merchant-options")
    ResponseResult getPhysicalStoreMerchantOptions();

    /** 门店开通商家权限时使用的已有账号选项。 */
    @PostMapping("/mall/store/account-options")
    ResponseResult getPhysicalStoreAccountOptions(@RequestBody Map<String, Object> param);

    /** 在指定商家下新增门店。 */
    @PostMapping("/mall/store/create")
    ResponseResult createPhysicalStore(@RequestBody Map<String, Object> param);

    /** 更新门店营业、骑手配送或 POS 开关。 */
    @PostMapping("/mall/store/toggle")
    ResponseResult togglePhysicalStore(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/shop/detail/{id}")
    ResponseResult getShopDetail(@PathVariable("id") Long id);
    
    /**
     * TODO 补充方法说明
/**
     * @param shop TODO
     * @return TODO
     */
    @PostMapping("/mall/shop/create")
    ResponseResult createShop(@RequestBody Map<String, Object> shop);
    
    /**
     * TODO 补充方法说明
/**
     * @param shop TODO
     * @return TODO
     */
    @PostMapping("/mall/shop/update")
    ResponseResult updateShop(@RequestBody Map<String, Object> shop);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @DeleteMapping("/mall/shop/delete/{id}")
    ResponseResult deleteShop(@PathVariable("id") Long id);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/mall/shop/level/list")
    ResponseResult getShopLevelList();
    
    /**
     * TODO 补充方法说明
/**
     * @param level TODO
     * @return TODO
     */
    @PostMapping("/mall/shop/level/create")
    ResponseResult createShopLevel(@RequestBody Map<String, Object> level);
    
    /**
     * TODO 补充方法说明
/**
     * @param level TODO
     * @return TODO
     */
    @PostMapping("/mall/shop/level/update")
    ResponseResult updateShopLevel(@RequestBody Map<String, Object> level);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/shop/entry/list")
    ResponseResult getMerchantEntryList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/shop/entry/audit")
    ResponseResult auditMerchantEntry(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/shop/inquiry/list")
    ResponseResult getInquiryList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param inquiry TODO
     * @return TODO
     */
    @PostMapping("/mall/shop/inquiry/quote")
    ResponseResult quoteInquiry(@RequestBody Map<String, Object> inquiry);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/shop/cod/list")
    ResponseResult getCashOnDeliveryList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/shop/invoice/list")
    ResponseResult getInvoiceList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param invoice TODO
     * @return TODO
     */
    @PostMapping("/mall/shop/invoice/issue")
    ResponseResult issueInvoice(@RequestBody Map<String, Object> invoice);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/shop/deposit/list")
    ResponseResult getQualityDepositList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/shop/deposit/refund")
    ResponseResult refundQualityDeposit(@RequestBody Map<String, Object> param);
    
    /** 更新店销状态 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/shop/update-status")
    ResponseResult updateShopStatus(@RequestBody Map<String, Object> param);
    
    /** 获取店销概览统计 */
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/mall/shop/overview")
    ResponseResult getShopOverview();
    
    // ================= 经营类目 =================
    
    /** 分页查询经营类目列表 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/shop/category/list")
    ResponseResult getShopCategoryList(@RequestBody Map<String, Object> param);
    
    /** 获取经营类目详情 */
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/mall/shop/category/detail/{id}")
    ResponseResult getShopCategoryDetail(@PathVariable("id") Long id);
    
    /** 新增经营类目 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/shop/category/create")
    ResponseResult createShopCategory(@RequestBody Map<String, Object> param);
    
    /** 编辑经营类目 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/shop/category/update")
    ResponseResult updateShopCategory(@RequestBody Map<String, Object> param);
    
    /** 删除经营类目 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/shop/category/delete")
    ResponseResult deleteShopCategory(@RequestBody Map<String, Object> param);
    
    /** 更改经营类目状态 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/shop/category/status")
    ResponseResult updateShopCategoryStatus(@RequestBody Map<String, Object> param);
    
    /** 获取启用经营类目列表 */
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/mall/shop/category/enabled")
    ResponseResult getEnabledShopCategoryList();
    
    // ================= 商家菜单权限 =================
    
    /** 获取商家菜单树形列表 */
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/mall/shop/menu/list")
    ResponseResult getShopMenuList();
    
    /** 获取菜单详情 */
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/mall/shop/menu/detail/{id}")
    ResponseResult getShopMenuDetail(@PathVariable("id") Long id);
    
    /** 新增商家菜单 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/shop/menu/create")
    ResponseResult createShopMenu(@RequestBody Map<String, Object> param);
    
    /** 编辑商家菜单 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/shop/menu/update")
    ResponseResult updateShopMenu(@RequestBody Map<String, Object> param);
    
    /** 删除商家菜单 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/shop/menu/delete")
    ResponseResult deleteShopMenu(@RequestBody Map<String, Object> param);
    
    /** 更改商家菜单状态 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/shop/menu/status")
    ResponseResult updateShopMenuStatus(@RequestBody Map<String, Object> param);
    
    /** 获取启用菜单列表（父级选择用） */
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/mall/shop/menu/enabled")
    ResponseResult getEnabledShopMenuList();

    /** 获取财务概览统计 */
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/mall/finance/overview")
    ResponseResult getFinanceOverview();

    @PostMapping("/mall/finance/center")
    ResponseResult getMerchantFinanceCenter();

    @PostMapping("/mall/finance/refund/stats")
    ResponseResult getRefundStats(@RequestBody Map<String, Object> param);

    @PostMapping("/mall/finance/refund/list")
    ResponseResult getRefundList(@RequestBody Map<String, Object> param);

    @PostMapping("/mall/finance/refund/retry")
    ResponseResult retryRefund(@RequestBody Map<String, Object> param);

    @PostMapping("/mall/finance/shop/balance/list")
    ResponseResult getShopBalanceList(@RequestBody Map<String, Object> param);

    @PostMapping("/mall/finance/user/wallet/list")
    ResponseResult getUserWalletList(@RequestBody Map<String, Object> param);

    @PostMapping("/mall/finance/user/recharge/list")
    ResponseResult getUserRechargeList(@RequestBody Map<String, Object> param);

    /** 分页查询提现列表 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/finance/withdraw/list")
    ResponseResult getWithdrawList(@RequestBody Map<String, Object> param);

    /** 获取提现详情 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/finance/withdraw/detail")
    ResponseResult getWithdrawDetail(@RequestBody Map<String, Object> param);

    /** 提现审核 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/finance/withdraw/audit")
    ResponseResult auditWithdraw(@RequestBody Map<String, Object> param);

    /** 获取提现统计 */
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/mall/finance/withdraw/stats")
    ResponseResult getWithdrawStats(@RequestBody Map<String, Object> param);

    // ================= 财务管理 - 收入 =================
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/finance/income/list")
    ResponseResult getFinanceIncomeList(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/finance/income/detail")
    ResponseResult getFinanceIncomeDetail(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/finance/income/create")
    ResponseResult createFinanceIncome(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/finance/income/confirm")
    ResponseResult confirmFinanceIncome(@RequestBody Map<String, Object> params);

    // ================= 财务管理 - 支出 =================
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/finance/expense/list")
    ResponseResult getFinanceExpenseList(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/finance/expense/detail")
    ResponseResult getFinanceExpenseDetail(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/finance/expense/create")
    ResponseResult createFinanceExpense(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/finance/expense/confirm")
    ResponseResult confirmFinanceExpense(@RequestBody Map<String, Object> params);

    // ================= 财务管理 - 结算 =================
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/finance/settlement/list")
    ResponseResult getFinanceSettlementList(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/finance/settlement/detail")
    ResponseResult getFinanceSettlementDetail(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/finance/settlement/create")
    ResponseResult createFinanceSettlement(@RequestBody Map<String, Object> params);
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/finance/settlement/settle")
    ResponseResult settleFinanceSettlement(@RequestBody Map<String, Object> params);

    /** 佣金提现 */
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/mall/distribution/withdraw")
    ResponseResult withdrawCommission(@RequestBody Map<String, Object> param);

    // ================= dcenter 商家管理后台 - 商品管理 =================

    /**
     * 获取商品列表
     */
    @PostMapping("/mall/shop/goods/list")
    ResponseResult getGoodsList(@RequestBody Map<String, Object> params);

    /**
     * 获取商品详情
     */
    @PostMapping("/mall/shop/goods/detail")
    ResponseResult getGoodsDetail(@RequestBody Map<String, Object> params);

    /**
     * 保存商品
     */
    @PostMapping("/mall/shop/goods/save")
    ResponseResult saveGoods(@RequestBody Map<String, Object> params);

    /** 智能入库使用现有商品新增接口。 */
    @PostMapping("/mall/shop/goods/add")
    ResponseResult addGoodsForImport(@RequestBody Map<String, Object> params);

    /**
     * 删除商品
     */
    @PostMapping("/mall/shop/goods/delete")
    ResponseResult deleteGoods(@RequestBody Map<String, Object> params);

    /**
     * 批量删除商品
     */
    @PostMapping("/mall/shop/goods/batchDelete")
    ResponseResult batchDeleteGoods(@RequestBody Map<String, Object> params);

    /**
     * 批量上架商品
     */
    @PostMapping("/mall/shop/goods/batchPutOnSale")
    ResponseResult batchPutOnSale(@RequestBody Map<String, Object> params);

    /**
     * 批量下架商品
     */
    @PostMapping("/mall/shop/goods/batchPutOffSale")
    ResponseResult batchPutOffSale(@RequestBody Map<String, Object> params);

    /**
     * 更新商品状态
     */
    @PostMapping("/mall/shop/goods/updateStatus")
    ResponseResult updateGoodsStatus(@RequestBody Map<String, Object> params);

    // ================= dcenter 商家管理后台 - 商品评价 =================

    /**
     * 获取评价列表
     */
    @PostMapping("/mall/shop/comment/list")
    ResponseResult getCommentList(@RequestBody Map<String, Object> params);

    /**
     * 审核评价
     */
    @PostMapping("/mall/shop/comment/audit")
    ResponseResult auditComment(@RequestBody Map<String, Object> params);

    /**
     * 回复评价
     */
    @PostMapping("/mall/shop/comment/reply")
    ResponseResult replyComment(@RequestBody Map<String, Object> params);

    // ================= dcenter 商家管理后台 - 会员管理 =================

    /**
     * 获取会员列表
     */
    @PostMapping("/mall/user/member/list")
    ResponseResult getMemberList(@RequestBody Map<String, Object> params);

    /**
     * 获取会员详情
     */
    @PostMapping("/mall/user/member/detail")
    ResponseResult getMemberDetail(@RequestBody Map<String, Object> params);

    /**
     * 更新会员等级
     */
    @PostMapping("/mall/user/member/updateLevel")
    ResponseResult updateMemberLevel(@RequestBody Map<String, Object> params);

    // ================= dcenter 商家管理后台 - 店铺装修 =================

    /**
     * 获取装修页面列表
     */
    @PostMapping("/mall/decoration/page/list")
    ResponseResult getDecoratePageList(@RequestBody Map<String, Object> params,
                                       @RequestHeader(value = "Authorization", required = false) String authorization);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @param "Authorization" TODO
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/mall/decoration/page/detail")
    ResponseResult getDecoratePageDetail(@RequestBody Map<String, Object> params,
                                         @RequestHeader(value = "Authorization", required = false) String authorization);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @param "Authorization" TODO
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/mall/decoration/page/save")
    ResponseResult saveDecoratePage(@RequestBody Map<String, Object> params,
                                    @RequestHeader(value = "Authorization", required = false) String authorization);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @param "Authorization" TODO
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/mall/decoration/page/delete")
    ResponseResult deleteDecoratePage(@RequestBody Map<String, Object> params,
                                      @RequestHeader(value = "Authorization", required = false) String authorization);

    /**
     * 移动端页面模板库。
     *
     * @param authorization 管理员 token
     * @return 模板列表
     */
    @PostMapping("/mall/decoration/page/template/list")
    ResponseResult getDecoratePageTemplates(
            @RequestHeader(value = "Authorization", required = false) String authorization);

    /**
     * 读取装修页面当前套用的模板 code。
     *
     * @param params { pageId }
     * @param authorization 管理员 token
     * @return templateCode
     */
    @PostMapping("/mall/decoration/page/template/current")
    ResponseResult getDecoratePageTemplate(@RequestBody Map<String, Object> params,
                                           @RequestHeader(value = "Authorization", required = false) String authorization);

    /**
     * 为装修页面套用模板。
     *
     * @param params { pageId, templateCode }
     * @param authorization 管理员 token
     * @return 实际生效的 code
     */
    @PostMapping("/mall/decoration/page/template/apply")
    ResponseResult applyDecoratePageTemplate(@RequestBody Map<String, Object> params,
                                             @RequestHeader(value = "Authorization", required = false) String authorization);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @param "Authorization" TODO
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/mall/decoration/page/home/list")
    ResponseResult getHomePageList(@RequestBody Map<String, Object> params,
                                   @RequestHeader(value = "Authorization", required = false) String authorization);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @param "Authorization" TODO
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/mall/decoration/page/setHome")
    ResponseResult setHomePage(@RequestBody Map<String, Object> params,
                               @RequestHeader(value = "Authorization", required = false) String authorization);

    // ================= dcenter 商家管理后台 - 文件管理 =================

    /**
     * 获取文件分类列表
     */
    // ================= 平台管理后台素材库（like-admin /file/* 兼容） =================

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @param "Authorization" TODO
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/mall/admin/album/listCate")
    ResponseResult adminAlbumListCate(@RequestParam Map<String, Object> params,
                                      @RequestHeader(value = "Authorization", required = false) String authorization);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @param "Authorization" TODO
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/mall/admin/album/lists")
    ResponseResult adminAlbumLists(@RequestParam Map<String, Object> params,
                                     @RequestHeader(value = "Authorization", required = false) String authorization);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @param "Authorization" TODO
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/mall/admin/album/addCate")
    ResponseResult adminAlbumAddCate(@RequestBody Map<String, Object> params,
                                       @RequestHeader(value = "Authorization", required = false) String authorization);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @param "Authorization" TODO
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/mall/admin/album/editCate")
    ResponseResult adminAlbumEditCate(@RequestBody Map<String, Object> params,
                                        @RequestHeader(value = "Authorization", required = false) String authorization);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @param "Authorization" TODO
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/mall/admin/album/delCate")
    ResponseResult adminAlbumDelCate(@RequestBody Map<String, Object> params,
                                       @RequestHeader(value = "Authorization", required = false) String authorization);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @param "Authorization" TODO
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/mall/admin/album/delete")
    ResponseResult adminAlbumDelete(@RequestBody Map<String, Object> params,
                                      @RequestHeader(value = "Authorization", required = false) String authorization);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @param "Authorization" TODO
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/mall/admin/album/move")
    ResponseResult adminAlbumMove(@RequestBody Map<String, Object> params,
                                    @RequestHeader(value = "Authorization", required = false) String authorization);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @param "Authorization" TODO
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/mall/admin/album/rename")
    ResponseResult adminAlbumRename(@RequestBody Map<String, Object> params,
                                      @RequestHeader(value = "Authorization", required = false) String authorization);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @param "Authorization" TODO
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/mall/admin/album/record")
    ResponseResult adminAlbumRecord(@RequestBody Map<String, Object> params,
                                    @RequestHeader(value = "Authorization", required = false) String authorization);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/mall/file/cate/list")
    ResponseResult getFileCategoryList(@RequestBody Map<String, Object> params);

    /**
     * 保存文件分类
     */
    @PostMapping("/mall/file/cate/save")
    ResponseResult saveFileCategory(@RequestBody Map<String, Object> params);

    /**
     * 删除文件分类
     */
    @PostMapping("/mall/file/cate/delete")
    ResponseResult deleteFileCategory(@RequestBody Map<String, Object> params);

    /**
     * 获取文件列表
     */
    @PostMapping("/mall/file/list")
    ResponseResult getFileList(@RequestBody Map<String, Object> params);

    /**
     * 上传文件
     */
    @PostMapping("/mall/file/upload")
    ResponseResult uploadFile(@RequestBody Map<String, Object> params);

    /**
     * 删除文件
     */
    @PostMapping("/mall/file/delete")
    ResponseResult deleteFile(@RequestBody Map<String, Object> params);

    /**
     * 移动文件
     */
    @PostMapping("/mall/file/move")
    ResponseResult moveFile(@RequestBody Map<String, Object> params);

    /**
     * 重命名文件
     */
    @PostMapping("/mall/file/rename")
    ResponseResult renameFile(@RequestBody Map<String, Object> params);

    // ================= dcenter 商家管理后台 - 系统公告 =================

    /**
     * 获取公告列表
     */
    @PostMapping("/mall/notice/announcement/list")
    ResponseResult getAnnouncementList(@RequestBody Map<String, Object> params);

    /**
     * 获取最新公告
     */
    @PostMapping("/mall/notice/announcement/latest")
    ResponseResult getLatestAnnouncement(@RequestBody Map<String, Object> params);

    /**
     * 获取公告详情
     */
    @PostMapping("/mall/notice/announcement/detail")
    ResponseResult getAnnouncementDetail(@RequestBody Map<String, Object> params);

    /**
     * 保存公告（新增或编辑）
     */
    @PostMapping("/mall/notice/announcement/save")
    ResponseResult saveAnnouncement(@RequestBody Map<String, Object> params);

    /**
     * 删除公告
     */
    @PostMapping("/mall/notice/announcement/delete")
    ResponseResult deleteAnnouncement(@RequestBody Map<String, Object> params);

    // ================= dcenter 商家管理后台 - 客服配置 =================

    /**
     * 获取客服配置
     */
    @PostMapping("/mall/service/config/get")
    ResponseResult getServiceConfig(@RequestBody Map<String, Object> params);

    /**
     * 保存客服配置
     */
    @PostMapping("/mall/service/config/save")
    ResponseResult saveServiceConfig(@RequestBody Map<String, Object> params);

    // ================= dcenter 商家管理后台 - 消息通知设置 =================

    /**
     * 获取消息通知设置列表
     */
    @PostMapping("/mall/message/list")
    ResponseResult getMessageSettingList(@RequestBody Map<String, Object> params);

    /**
     * 获取消息通知设置详情
     */
    @PostMapping("/mall/message/detail")
    ResponseResult getMessageSettingDetail(@RequestBody Map<String, Object> params);

    /**
     * 保存消息通知设置
     */
    @PostMapping("/mall/message/save")
    ResponseResult saveMessageSetting(@RequestBody Map<String, Object> params);

    /**
     * 删除消息通知设置
     */
    @PostMapping("/mall/message/delete")
    ResponseResult deleteMessageSetting(@RequestBody Map<String, Object> params);

    // ================= dcenter 商家管理后台 - 集采业务 =================

    /**
     * 获取集采列表
     */
    @PostMapping("/mall/gather/list")
    ResponseResult getGatherList(@RequestBody Map<String, Object> params);

    /**
     * 获取集采详情
     */
    @PostMapping("/mall/gather/detail")
    ResponseResult getGatherDetail(@RequestBody Map<String, Object> params);

    /**
     * 保存集采配置
     */
    @PostMapping("/mall/gather/save")
    ResponseResult saveGather(@RequestBody Map<String, Object> params);

    /**
     * 删除集采配置
     */
    @PostMapping("/mall/gather/delete")
    ResponseResult deleteGather(@RequestBody Map<String, Object> params);

    // ================= 装修系统 - tabbar、风格、页面数据 =================

    /**
     * 获取底部导航详情
     */
    @PostMapping("/mall/decoration/tabbar/detail")
    ResponseResult getTabbarDetail(@RequestBody Map<String, Object> params,
                                 @RequestHeader(value = "Authorization", required = false) String authorization);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @param "Authorization" TODO
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/mall/decoration/tabbar/save")
    ResponseResult saveTabbar(@RequestBody Map<String, Object> params,
                              @RequestHeader(value = "Authorization", required = false) String authorization);

    /**
     * 获取商城风格主题
     */
    @PostMapping("/mall/config/getMallStyle")
    ResponseResult getMallStyle(@RequestBody Map<String, Object> params,
                                @RequestHeader(value = "Authorization", required = false) String authorization);

    /**
     * 获取商城风格主题（likeshop 路径）
     */
    @GetMapping("/mall/config/get_mall_style")
    ResponseResult getMallStyleConfig(@RequestHeader(value = "Authorization", required = false) String authorization);

    /**
     * 保存商城风格主题
     */
    @PostMapping("/mall/config/setMallStyle")
    ResponseResult setMallStyle(@RequestBody Map<String, Object> params,
                                @RequestHeader(value = "Authorization", required = false) String authorization);

    /**
     * 保存商城风格主题（likeshop 路径）
     */
    @PostMapping("/mall/config/set_mall_style")
    ResponseResult setMallStyleConfig(@RequestBody Map<String, Object> params,
                                      @RequestHeader(value = "Authorization", required = false) String authorization);

    /**
     * 新增装修页面
     */
    @PostMapping("/mall/decoration/page/add")
    ResponseResult addDecoratePage(@RequestBody Map<String, Object> params,
                                   @RequestHeader(value = "Authorization", required = false) String authorization);

    /**
     * 获取装修用文章数据
     */
    @PostMapping("/mall/decoration/data/article")
    ResponseResult getDecorateArticleData(@RequestBody Map<String, Object> params,
                                          @RequestHeader(value = "Authorization", required = false) String authorization);

    // ===================== 前台首页数据（读 hou_base_decorate_page 表） =====================

    /**
     * 获取首页装修数据（前台 H5/小程序）
     */
    @PostMapping("/mall/front/index/data")
    ResponseResult getFrontIndexData(@RequestBody Map<String, Object> params);

    /**
     * 获取装修页面数据（按 type）
     */
    @PostMapping("/mall/front/index/decorate")
    ResponseResult getFrontDecorate(@RequestBody Map<String, Object> params);

    /**
     * 获取商城公共配置（tabbar、主题、登录等）
     */
    @PostMapping("/mall/front/index/config")
    ResponseResult getFrontIndexConfig();

    /**
     * 获取首页商品分类列表
     */
    @PostMapping("/mall/front/index/category")
    ResponseResult getFrontIndexCategory();

    /**
     * 前台热门搜索
     */
    @PostMapping("/mall/front/index/hotSearch")
    ResponseResult getFrontHotSearch();

    // ===================== 前台店铺（读 hou_merchant_mall_shop 表） =====================

    /**
     * 前台店铺分页列表（page/limit/keyword）
     */
    @PostMapping("/mall/front/shop/list")
    ResponseResult frontShopList(@RequestBody Map<String, Object> params);

    /**
     * 前台店铺详情
     */
    @GetMapping("/mall/front/shop/detail")
    ResponseResult frontShopDetail(@RequestParam("id") Long id);

    /**
     * 前台店铺商品经营分类
     */
    @GetMapping("/mall/front/shop/category")
    ResponseResult frontShopCategory(@RequestParam("id") Long id);

    /**
     * 前台商品评论分页列表（goods_id、page、limit）
     */
    @PostMapping("/mall/front/comment/list")
    ResponseResult frontCommentList(@RequestBody Map<String, Object> params);

    // ================= setting/dict_type 字典类型 =================

    /** 字典类型全部 */
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/setting/dict_type/all")
    ResponseResult allDictType();

    /** 字典类型列表（分页） */
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @GetMapping("/mall/setting/dict_type/lists")
    ResponseResult listsDictType(@RequestParam Map<String, String> params);

    /** 字典类型详情 */
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/setting/dict_type/detail")
    ResponseResult detailDictType(@RequestParam("id") Integer id);

    /** 字典类型新增 */
    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/mall/setting/dict_type/add")
    ResponseResult addDictType(@RequestBody Map<String, Object> body);

    /** 字典类型编辑 */
    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/mall/setting/dict_type/edit")
    ResponseResult editDictType(@RequestBody Map<String, Object> body);

    /** 字典类型删除 */
    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/mall/setting/dict_type/delete")
    ResponseResult delDictType(@RequestBody Map<String, Object> body);

    // ================= setting/dict_data 字典数据 =================

    /** 字典数据全部（按 typeId 过滤） */
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @GetMapping("/mall/setting/dict_data/all")
    ResponseResult allDictData(@RequestParam Map<String, String> params);

    /** 字典数据列表（分页） */
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @GetMapping("/mall/setting/dict_data/lists")
    ResponseResult listsDictData(@RequestParam Map<String, String> params);

    /** 字典数据详情 */
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/setting/dict_data/detail")
    ResponseResult detailDictData(@RequestParam("id") Integer id);

    /** 字典数据新增 */
    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/mall/setting/dict_data/add")
    ResponseResult addDictData(@RequestBody Map<String, Object> body);

    /** 字典数据编辑 */
    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/mall/setting/dict_data/edit")
    ResponseResult editDictData(@RequestBody Map<String, Object> body);

    /** 字典数据删除 */
    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/mall/setting/dict_data/delete")
    ResponseResult delDictData(@RequestBody Map<String, Object> body);

    // ================= setting/pay_config 支付配置 =================

    /** 支付配置列表 */
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/setting/pay_config/lists")
    ResponseResult listsPayConfig();

    /** 支付配置详情 */
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/setting/pay_config/detail")
    ResponseResult detailPayConfig(@RequestParam("id") Integer id);

    /** 支付配置编辑 */
    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/mall/setting/pay_config/edit")
    ResponseResult editPayConfig(@RequestBody Map<String, Object> body);

    // ================= setting/pay_way 支付方式（B1-4）=================

    /** 支付方式列表（按场景分组） */
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/setting/pay_way/lists")
    ResponseResult listsPayWay();

    /** 支付方式批量编辑 */
    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/mall/setting/pay_way/edit")
    ResponseResult editPayWay(@RequestBody Map<String, Object> body);

    // ================= setting/storage 存储配置（B1-5）=================

    /** 存储引擎列表 */
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/setting/storage/lists")
    ResponseResult listsStorage();

    /** 存储引擎详情 */
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/setting/storage/detail")
    ResponseResult detailStorage(@RequestParam("engine") String engine);

    /** 存储引擎编辑 */
    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/mall/setting/storage/setup")
    ResponseResult setupStorage(@RequestBody Map<String, Object> body);

    /** 存储引擎切换 */
    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/mall/setting/storage/change")
    ResponseResult changeStorage(@RequestBody Map<String, Object> body);

    // ================= setting/sms 短信配置（B1-6）=================

    /** 短信引擎列表 */
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/setting/sms/lists")
    ResponseResult listsSms();

    /** 短信引擎详情 */
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/setting/sms/detail")
    ResponseResult detailSms(@RequestParam("type") String type);

    /** 短信引擎编辑 */
    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/mall/setting/sms/edit")
    ResponseResult editSms(@RequestBody Map<String, Object> body);

    // ================= setting/notice 通知设置（B1-7）=================

    /** 通知设置列表 */
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/setting/notice/lists")
    ResponseResult listsNotice(@RequestParam("recipient") Integer recipient);

    /** 通知设置详情 */
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/setting/notice/detail")
    ResponseResult detailNotice(@RequestParam("id") Integer id);

    /** 通知设置保存 */
    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/mall/setting/notice/edit")
    ResponseResult editNotice(@RequestBody Map<String, Object> body);

    // ================= setting/order 订单设置（B1-8）=================

    /** 交易设置详情 */
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/setting/order/transaction")
    ResponseResult getOrderTransaction();

    /** 交易设置保存 */
    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/mall/setting/order/transaction/save")
    ResponseResult saveOrderTransaction(@RequestBody Map<String, Object> body);

    /** 结算设置详情 */
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/setting/order/settlement")
    ResponseResult getOrderSettlement();

    /** 结算设置保存 */
    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/mall/setting/order/settlement/save")
    ResponseResult saveOrderSettlement(@RequestBody Map<String, Object> body);

    // ================= setting/search 热门搜索（B1-9）=================

    /** 热搜详情 */
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/setting/search/lists")
    ResponseResult listsSearch();

    /** 热搜编辑 */
    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/mall/setting/search/edit")
    ResponseResult editSearch(@RequestBody Map<String, Object> body);

    // ================= setting/user 用户设置（B1-10）=================

    /** 用户基础设置详情 */
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/setting/user/lists")
    ResponseResult listsUser();

    /** 用户基础设置保存 */
    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/mall/setting/user/edit")
    ResponseResult editUser(@RequestBody Map<String, Object> body);

    /** 登录注册设置详情 */
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/setting/user/login/lists")
    ResponseResult listsUserLogin();

    /** 登录注册设置保存 */
    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/mall/setting/user/login/edit")
    ResponseResult editUserLogin(@RequestBody Map<String, Object> body);

    // ================= setting/website 网站设置（B1-11）=================

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/setting/website/lists")
    ResponseResult listsWebsite();

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/mall/setting/website/edit")
    ResponseResult editWebsite(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/setting/website/copyright/lists")
    ResponseResult listsWebsiteCopyright();

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/mall/setting/website/copyright/edit")
    ResponseResult editWebsiteCopyright(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/setting/website/agreement/lists")
    ResponseResult listsWebsiteAgreement();

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/mall/setting/website/agreement/edit")
    ResponseResult editWebsiteAgreement(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/setting/website/statistics/lists")
    ResponseResult listsWebsiteStatistics();

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/mall/setting/website/statistics/edit")
    ResponseResult editWebsiteStatistics(@RequestBody Map<String, Object> body);

    // ================= 管理员认证 =================

    /**
     * Mall 管理员登录
     */
    @PostMapping("/mall/auth/login")
    ResponseResult mallLogin(@RequestBody Map<String, Object> param);

    /**
     * 获取 Mall 管理员信息
     */
    @PostMapping("/mall/auth/userinfo")
    ResponseResult mallGetUserInfo(@RequestHeader("Authorization") String token);

    /**
     * 获取 Mall 管理员菜单
     */
    @PostMapping("/mall/auth/menus")
    ResponseResult mallGetMenus(@RequestHeader("Authorization") String token);

    /**
     * Mall 管理员退出登录
     */
    @PostMapping("/mall/auth/logout")
    ResponseResult mallLogout(@RequestHeader("Authorization") String token);

    // ================= Mall 平台后台权限管理（hou_mall_*） =================

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/mall/perms/menu/list")
    ResponseResult mallPermMenuList(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/mall/perms/menu/all")
    ResponseResult mallPermMenuAll(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/mall/perms/menu/tree")
    ResponseResult mallPermMenuTree(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/perms/menu/detail")
    ResponseResult mallPermMenuDetail(@RequestHeader("Authorization") String token, @RequestParam("id") Integer id);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/mall/perms/menu/add")
    ResponseResult mallPermMenuAdd(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/mall/perms/menu/edit")
    ResponseResult mallPermMenuEdit(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/mall/perms/menu/delete")
    ResponseResult mallPermMenuDelete(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/perms/role/all")
    ResponseResult mallPermRoleAll(@RequestHeader("Authorization") String token);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/perms/role/lists")
    ResponseResult mallPermRoleLists(@RequestHeader("Authorization") String token, @RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/perms/role/detail")
    ResponseResult mallPermRoleDetail(@RequestHeader("Authorization") String token, @RequestParam("id") Integer id);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/mall/perms/role/add")
    ResponseResult mallPermRoleAdd(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/mall/perms/role/edit")
    ResponseResult mallPermRoleEdit(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/mall/perms/role/delete")
    ResponseResult mallPermRoleDelete(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/perms/admin/all")
    ResponseResult mallPermAdminAll(@RequestHeader("Authorization") String token);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/perms/admin/lists")
    ResponseResult mallPermAdminLists(@RequestHeader("Authorization") String token, @RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/perms/admin/detail")
    ResponseResult mallPermAdminDetail(@RequestHeader("Authorization") String token, @RequestParam("id") Integer id);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/mall/perms/admin/add")
    ResponseResult mallPermAdminAdd(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/mall/perms/admin/edit")
    ResponseResult mallPermAdminEdit(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/mall/perms/admin/delete")
    ResponseResult mallPermAdminDelete(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> params);

    // ================= Mall 平台后台组织管理（hou_mall_dept / hou_mall_jobs） =================

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/org/dept/all")
    ResponseResult mallOrgDeptAll(@RequestHeader("Authorization") String token);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/org/dept/lists")
    ResponseResult mallOrgDeptLists(@RequestHeader("Authorization") String token, @RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/org/dept/detail")
    ResponseResult mallOrgDeptDetail(@RequestHeader("Authorization") String token, @RequestParam("id") Integer id);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/mall/org/dept/add")
    ResponseResult mallOrgDeptAdd(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/mall/org/dept/edit")
    ResponseResult mallOrgDeptEdit(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/mall/org/dept/delete")
    ResponseResult mallOrgDeptDelete(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/org/jobs/all")
    ResponseResult mallOrgJobsAll(@RequestHeader("Authorization") String token, @RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/org/jobs/lists")
    ResponseResult mallOrgJobsLists(@RequestHeader("Authorization") String token, @RequestParam Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/mall/org/jobs/detail")
    ResponseResult mallOrgJobsDetail(@RequestHeader("Authorization") String token, @RequestParam("id") Integer id);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/mall/org/jobs/add")
    ResponseResult mallOrgJobsAdd(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/mall/org/jobs/edit")
    ResponseResult mallOrgJobsEdit(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/mall/org/jobs/delete")
    ResponseResult mallOrgJobsDelete(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> params);

    /**
     * H5 首页装修数据（新版后台装修 → H5 老结构）。
     * 前台公开接口：/mall/front/** 在 mall 侧已 permitAll。
     */
    @GetMapping("/mall/front/index/legacyHome")
    ResponseResult frontLegacyHomeGet();

    @PostMapping("/mall/front/index/legacyHome")
    ResponseResult frontLegacyHomePost();
}
