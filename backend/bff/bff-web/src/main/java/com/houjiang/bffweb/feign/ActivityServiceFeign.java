package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 活动服务Feign客户端
/**
 * 
 * @description 调用paas中台活动服务(activity)的接口
 * @author Amos
 * @date 2024-12-29
 */
@FeignClient(name = "order", url = "${feign.order.url:}", path = "/plugin/activity", fallback = ActivityServiceFeignFallback.class, contextId = "activityServiceFeign")
public interface ActivityServiceFeign {

    // ================= 活动管理接口 =================
    
    /**
     * 获取活动统计数据
     */
    @PostMapping("/manage/statistics")
    ResponseResult getActivityStatistics(@RequestParam(required = false) Map<String, Object> param);
    
    /**
     * 分页查询活动列表
     */
    @PostMapping("/manage/list")
    ResponseResult queryActivityList(@RequestBody Map<String, Object> param);
    
    /**
     * 查询活动详情
     */
    @PostMapping("/manage/detail")
    ResponseResult getActivityDetail(@RequestBody Map<String, Object> param);
    
    /**
     * 创建活动
     */
    @PostMapping("/manage/create")
    ResponseResult createActivity(@RequestBody Map<String, Object> activityData);
    
    /**
     * 更新活动
     */
    @PostMapping("/manage/update")
    ResponseResult updateActivity(@RequestBody Map<String, Object> activityData);
    
    /**
     * 删除活动
     */
    @PostMapping("/manage/delete")
    ResponseResult deleteActivity(@RequestBody Map<String, Object> param);
    
    /**
     * 暂停活动
     */
    @PostMapping("/manage/pause")
    ResponseResult pauseActivity(@RequestBody Map<String, Object> param);
    
    /**
     * 恢复活动
     */
    @PostMapping("/manage/resume")
    ResponseResult resumeActivity(@RequestBody Map<String, Object> param);
    
    /**
     * 复制活动
     */
    @PostMapping("/manage/copy")
    ResponseResult copyActivity(@RequestBody Map<String, Object> param);
    
    /**
     * 获取活动类型列表
     */
    @GetMapping("/manage/types")
    ResponseResult getActivityTypes();
    
    /**
     * 批量操作活动状态
     */
    @PostMapping("/manage/batch/status")
    ResponseResult batchUpdateStatus(@RequestBody Map<String, Object> param);

    // ================= 组合商品接口 =================

    /**
     * 分页查询组合商品列表
     */
    @PostMapping("/combination/query")
    ResponseResult queryCombinationList(@RequestBody Map<String, Object> param);

    /**
     * 查询组合商品详情
     */
    @PostMapping("/combination/detail")
    ResponseResult getCombinationDetail(@RequestBody Map<String, Object> param);

    /**
     * 新增组合商品
     */
    @PostMapping("/combination/add")
    ResponseResult addCombination(@RequestBody Map<String, Object> combinationData);

    /**
     * 更新组合商品
     */
    @PostMapping("/combination/update")
    ResponseResult updateCombination(@RequestBody Map<String, Object> combinationData);

    /**
     * 删除组合商品
     */
    @PostMapping("/combination/delete")
    ResponseResult deleteCombination(@RequestBody Map<String, Object> param);

    /**
     * 启用组合商品
     */
    @PostMapping("/combination/enable")
    ResponseResult enableCombination(@RequestBody Map<String, Object> param);

    // ================= 限时折扣接口 =================

    /**
     * 分页查询限时折扣列表
     */
    @PostMapping("/discount/query")
    ResponseResult queryDiscountList(@RequestBody Map<String, Object> param);

    /**
     * 查询限时折扣详情
     */
    @PostMapping("/discount/detail")
    ResponseResult getDiscountDetail(@RequestBody Map<String, Object> param);

    /**
     * 新增限时折扣
     */
    @PostMapping("/discount/add")
    ResponseResult addDiscount(@RequestBody Map<String, Object> discountData);

    /**
     * 更新限时折扣
     */
    @PostMapping("/discount/update")
    ResponseResult updateDiscount(@RequestBody Map<String, Object> discountData);

    /**
     * 删除限时折扣
     */
    @PostMapping("/discount/delete")
    ResponseResult deleteDiscount(@RequestBody Map<String, Object> param);

    /**
     * 启动限时折扣
     */
    @PostMapping("/discount/start")
    ResponseResult startDiscount(@RequestBody Map<String, Object> param);

    /**
     * 查询折扣商品列表
     */
    @PostMapping("/discount/goods/query")
    ResponseResult queryDiscountGoods(@RequestBody Map<String, Object> param);

    // ================= 惊喜红包接口 =================

    /**
     * 分页查询红包模板列表
     */
    @PostMapping("/redpacketTemplate/page")
    ResponseResult queryRedpacketTemplatePage(@RequestBody Map<String, Object> param);

    /**
     * 创建红包模板
     */
    @PostMapping("/redpacketTemplate/platform/insert")
    ResponseResult createRedpacketTemplate(@RequestBody Map<String, Object> param);

    /**
     * 更新红包模板
     */
    @PostMapping("/redpacketTemplate/platform/update")
    ResponseResult updateRedpacketTemplate(@RequestBody Map<String, Object> param);

    /**
     * 查询红包模板详情
     */
    @PostMapping("/redpacketTemplate/platform/detail")
    ResponseResult getRedpacketTemplateDetail(@RequestBody Map<String, Object> param);

    /**
     * 查询红包发放明细列表
     */
    @PostMapping("/redpacketBase/platform/detailList")
    ResponseResult queryRedpacketDetailList(@RequestBody Map<String, Object> param);

    /**
     * 查询红包领取用户列表
     */
    @PostMapping("/redpacketBase/platform/userList")
    ResponseResult queryRedpacketUserList(@RequestBody Map<String, Object> param);

    /**
     * 用户根据订单金额查询可用红包
     */
    @PostMapping("/redpacketBase/selectByDeposit")
    ResponseResult selectRedpacketByDeposit(@RequestBody Map<String, Object> param);

    /**
     * 用户领取红包
     */
    @PostMapping("/redpacketBase/receive")
    ResponseResult receiveRedpacket(@RequestBody Map<String, Object> param);

    // ================= 幸运大转盘接口 =================

    /**
     * 查询幸运大转盘活动列表
     */
    @PostMapping("/spinwin/query/list")
    ResponseResult querySpinwinActivityList(@RequestBody Map<String, Object> param);

    /**
     * 查询幸运大转盘活动详情
     */
    @PostMapping("/spinwin/query/detail")
    ResponseResult getSpinwinActivityDetail(@RequestBody Map<String, Object> param);

    /**
     * 新增或更新幸运大转盘活动
     */
    @PostMapping("/spinwin/update")
    ResponseResult updateSpinwinActivity(@RequestBody Map<String, Object> param);

    /**
     * 删除幸运大转盘活动
     */
    @PostMapping("/spinwin/delete")
    ResponseResult deleteSpinwinActivity(@RequestBody Map<String, Object> param);

    /**
     * 更新抽奖限制
     */
    @PostMapping("/spinwin/limit/update")
    ResponseResult updateSpinwinLimit(@RequestBody Map<String, Object> param);

    /**
     * 查询抽奖限制
     */
    @PostMapping("/spinwin/limit/query")
    ResponseResult querySpinwinLimit(@RequestBody Map<String, Object> param);

    /**
     * 查询用户抽奖次数
     */
    @PostMapping("/spinwin/draw/times/query")
    ResponseResult querySpinwinDrawTimes(@RequestBody Map<String, Object> param);

    /**
     * 查询活动说明
     */
    @PostMapping("/spinwin/explain/query")
    ResponseResult querySpinwinActivityExplain(@RequestBody Map<String, Object> param);

    /**
     * 用户抽奖
     */
    @PostMapping("/spinwin/lottery/draw/update")
    ResponseResult drawSpinwinLottery(@RequestBody Map<String, Object> param);

    // ================= 限时秒杀接口 =================

    /**
     * 查询店铺秒杀活动列表
     */
    @PostMapping("/activitySeckillBase/query/list")
    ResponseResult queryShopSeckillBaseList(@RequestBody Map<String, Object> param);

    /**
     * 查询店铺秒杀活动详情
     */
    @PostMapping("/activitySeckillBase/base/query")
    ResponseResult queryShopSeckillBase(@RequestBody Map<String, Object> param);

    /**
     * 创建或更新店铺秒杀活动
     */
    @PostMapping("/activitySeckillBase/add")
    ResponseResult addShopSeckillBase(@RequestBody Map<String, Object> param);

    /**
     * 删除店铺秒杀活动
     */
    @PostMapping("/activitySeckillBase/delete")
    ResponseResult deleteShopSeckillBase(@RequestBody Map<String, Object> param);

    /**
     * 查询限时秒杀商品列表
     */
    @PostMapping("/seckillGoods/querySecondKillGoods")
    ResponseResult querySecondKillGoods(@RequestBody Map<String, Object> param);

    /**
     * 创建或更新限时秒杀商品
     */
    @PostMapping("/seckillGoods/add")
    ResponseResult addShopSeckillGoods(@RequestBody Map<String, Object> param);

    /**
     * 删除限时秒杀商品
     */
    @PostMapping("/seckillGoods/delete")
    ResponseResult deleteShopSeckillGoods(@RequestBody Map<String, Object> param);

    /**
     * 查询秒杀活动商品（管理列表）
     */
    @PostMapping("/seckillGoods/query")
    ResponseResult queryShopSeckillGoods(@RequestBody Map<String, Object> param);

    /**
     * 查询秒杀活动规格列表
     */
    @PostMapping("/seckillGoods/spec/query")
    ResponseResult querySeckillGoodsSpecList(@RequestBody Map<String, Object> param);

    // ================= 满减满折（满减赠折）接口 =================

    /**
     * 查询满减赠折活动列表
     */
    @PostMapping("/reductionPolicyGoods/base/query/list")
    ResponseResult queryReductionPolicyBaseList(@RequestBody Map<String, Object> param);

    /**
     * 新增满减赠折活动
     */
    @PostMapping("/reductionPolicyGoods/base/add")
    ResponseResult addManjianzengzheBase(@RequestBody Map<String, Object> param);

    /**
     * 失效满减赠折活动
     */
    @PostMapping("/reductionPolicyGoods/base/invalidate")
    ResponseResult invalidateManjianzengzheBase(@RequestBody Map<String, Object> param);

    /**
     * 删除满减赠折活动
     */
    @PostMapping("/reductionPolicyGoods/base/delete")
    ResponseResult deleteManjianzengzheBase(@RequestBody Map<String, Object> param);

    /**
     * 新增活动商品
     */
    @PostMapping("/reductionPolicyGoods/base/goods/add")
    ResponseResult addReductionGoods(@RequestBody Map<String, Object> param);

    /**
     * 删除活动商品
     */
    @PostMapping("/reductionPolicyGoods/base/goods/delete")
    ResponseResult deleteReductionGoods(@RequestBody Map<String, Object> param);

    /**
     * 查看满减赠折活动详情（商品列表）
     */
    @PostMapping("/reductionPolicyGoods/base/goods/query/list")
    ResponseResult queryReductionGoodsDetailList(@RequestBody Map<String, Object> param);

    // ================= 店铺优惠券接口 =================

    /**
     * 平台查询店铺优惠券列表
     */
    @PostMapping("/voucher/template/platform/list")
    ResponseResult salesPromotionVoucherList(@RequestBody Map<String, Object> param);

    /**
     * 平台查询店铺优惠券详情
     */
    @PostMapping("/voucher/template/platform/detail")
    ResponseResult salesPromotionVoucherDetail(@RequestBody Map<String, Object> param);

    /**
     * 平台新增店铺优惠券
     */
    @PostMapping("/voucher/template/insert")
    ResponseResult insertVoucherTemp(@RequestBody Map<String, Object> param);

    /**
     * 平台修改店铺优惠券
     */
    @PostMapping("/voucher/template/platform/update")
    ResponseResult salesPromotionVoucherUpdate(@RequestBody Map<String, Object> param);

    /**
     * 平台删除店铺优惠券
     */
    @PostMapping("/voucher/template/platform/delete")
    ResponseResult deleteSalesPromotionVoucher(@RequestBody Map<String, Object> param);

    /**
     * 用户领取优惠券
     */
    @PostMapping("/voucherBase/insertvcbase")
    ResponseResult insertVcBase(@RequestBody Map<String, Object> param);

    /**
     * 用户查询未使用优惠券
     */
    @PostMapping("/voucherBase/selectvcbase")
    ResponseResult selectVcBase(@RequestBody Map<String, Object> param);

    /**
     * 根据店铺查询代金券信息
     */
    @PostMapping("/voucherBase/selectVctempByShopId")
    ResponseResult selectVcTempbByShopId(@RequestBody Map<String, Object> param);

    /**
     * 查询优惠券商品列表
     */
    @PostMapping("/voucherBase/voucher/query/list")
    ResponseResult queryFullReduceVcList(@RequestBody Map<String, Object> param);

    // ================= 千客千价接口 =================

    /**
     * 分页查询千客千价配置列表
     */
    @PostMapping("/thousandsGuestsPrice/config/page")
    ResponseResult queryThousandsGuestsPricePage(@RequestBody Map<String, Object> param);

    /**
     * 查询千客千价配置详情
     */
    @PostMapping("/thousandsGuestsPrice/config/detail")
    ResponseResult getThousandsGuestsPriceDetail(@RequestBody Map<String, Object> param);

    /**
     * 新增或更新千客千价配置
     */
    @PostMapping("/thousandsGuestsPrice/config/save")
    ResponseResult saveThousandsGuestsPriceConfig(@RequestBody Map<String, Object> param);

    /**
     * 删除千客千价配置
     */
    @PostMapping("/thousandsGuestsPrice/config/delete")
    ResponseResult deleteThousandsGuestsPriceConfig(@RequestBody Map<String, Object> param);

    /**
     * 更新千客千价配置状态
     */
    @PostMapping("/thousandsGuestsPrice/config/status/update")
    ResponseResult updateThousandsGuestsPriceStatus(@RequestBody Map<String, Object> param);

    /**
     * 查询用户千客千价个性化价格
     */
    @PostMapping("/thousandsGuestsPrice/price/query")
    ResponseResult queryThousandsGuestsPrice(@RequestBody Map<String, Object> param);

    // ================= 直播账号管理接口 =================

    /**
     * 分页查询直播账号
     */
    @PostMapping("/liveAccount/page")
    ResponseResult queryLiveAccountPage(@RequestBody Map<String, Object> param);

    /**
     * 直播账号详情
     */
    @PostMapping("/liveAccount/detail")
    ResponseResult getLiveAccountDetail(@RequestBody Map<String, Object> param);

    /**
     * 新增或更新直播账号
     */
    @PostMapping("/liveAccount/save")
    ResponseResult saveLiveAccount(@RequestBody Map<String, Object> param);

    /**
     * 更新直播账号状态
     */
    @PostMapping("/liveAccount/status/update")
    ResponseResult updateLiveAccountStatus(@RequestBody Map<String, Object> param);

    /**
     * 删除直播账号
     */
    @PostMapping("/liveAccount/delete")
    ResponseResult deleteLiveAccount(@RequestBody Map<String, Object> param);

    // ================= 直播间 & 互动接口 =================

    /**
     * 分页查询直播间
     */
    @PostMapping("/liveRoom/page")
    ResponseResult queryLiveRoomPage(@RequestBody Map<String, Object> param);

    /**
     * 直播间详情
     */
    @PostMapping("/liveRoom/detail")
    ResponseResult getLiveRoomDetail(@RequestBody Map<String, Object> param);

    /**
     * 新增或更新直播间
     */
    @PostMapping("/liveRoom/save")
    ResponseResult saveLiveRoom(@RequestBody Map<String, Object> param);

    /**
     * 更新直播间状态
     */
    @PostMapping("/liveRoom/status/update")
    ResponseResult updateLiveRoomStatus(@RequestBody Map<String, Object> param);

    /**
     * 删除直播间
     */
    @PostMapping("/liveRoom/delete")
    ResponseResult deleteLiveRoom(@RequestBody Map<String, Object> param);

    /**
     * 发送直播互动消息
     */
    @PostMapping("/liveRoom/message/send")
    ResponseResult sendLiveMessage(@RequestBody Map<String, Object> param);

    /**
     * 查询直播间消息列表
     */
    @PostMapping("/liveRoom/message/list")
    ResponseResult queryLiveMessageList(@RequestBody Map<String, Object> param);

    // ================= 直播商品审核接口 =================

    /**
     * 分页查询直播商品审核记录
     */
    @PostMapping("/liveGoodsAudit/page")
    ResponseResult queryLiveGoodsAuditPage(@RequestBody Map<String, Object> param);

    /**
     * 直播商品审核详情
     */
    @PostMapping("/liveGoodsAudit/detail")
    ResponseResult getLiveGoodsAuditDetail(@RequestBody Map<String, Object> param);

    /**
     * 新增/更新直播商品审核记录
     */
    @PostMapping("/liveGoodsAudit/save")
    ResponseResult saveLiveGoodsAudit(@RequestBody Map<String, Object> param);

    /**
     * 删除直播商品审核记录
     */
    @PostMapping("/liveGoodsAudit/delete")
    ResponseResult deleteLiveGoodsAudit(@RequestBody Map<String, Object> param);

    // ================= 积分商城工具接口 =================

    /**
     * 分页查询积分商城工具配置列表
     */
    @PostMapping("/pointsMallTool/page")
    ResponseResult queryPointsMallToolPage(@RequestBody Map<String, Object> param);

    /**
     * 查询积分商城工具配置详情
     */
    @PostMapping("/pointsMallTool/detail")
    ResponseResult getPointsMallToolDetail(@RequestBody Map<String, Object> param);

    /**
     * 新增或更新积分商城工具配置
     */
    @PostMapping("/pointsMallTool/save")
    ResponseResult savePointsMallTool(@RequestBody Map<String, Object> param);

    /**
     * 删除积分商城工具配置
     */
    @PostMapping("/pointsMallTool/delete")
    ResponseResult deletePointsMallTool(@RequestBody Map<String, Object> param);

    /**
     * 更新积分商城工具启用状态
     */
    @PostMapping("/pointsMallTool/status/update")
    ResponseResult updatePointsMallToolStatus(@RequestBody Map<String, Object> param);

    // ================= 红包管理接口 =================

    /**
     * 分页查询红包列表
     */
    @PostMapping("/redpacket/query")
    ResponseResult queryRedpacketList(@RequestBody Map<String, Object> param);

    /**
     * 新增红包
     */
    @PostMapping("/redpacket/add")
    ResponseResult addRedpacket(@RequestBody Map<String, Object> param);

    /**
     * 更新红包
     */
    @PostMapping("/redpacket/update")
    ResponseResult updateRedpacket(@RequestBody Map<String, Object> param);

    /**
     * 删除红包
     */
    @PostMapping("/redpacket/delete")
    ResponseResult deleteRedpacket(@RequestBody Map<String, Object> param);

    // ================= 积分商城商品接口 =================

    /**
     * 分页查询积分商城商品列表
     */
    @PostMapping("/pointsMallGoods/query")
    ResponseResult queryPointsMallGoodsList(@RequestBody Map<String, Object> param);

    /**
     * 新增积分商城商品
     */
    @PostMapping("/pointsMallGoods/add")
    ResponseResult addPointsMallGoods(@RequestBody Map<String, Object> param);

    /**
     * 更新积分商城商品
     */
    @PostMapping("/pointsMallGoods/update")
    ResponseResult updatePointsMallGoods(@RequestBody Map<String, Object> param);

    /**
     * 删除积分商城商品
     */
    @PostMapping("/pointsMallGoods/delete")
    ResponseResult deletePointsMallGoods(@RequestBody Map<String, Object> param);

    /**
     * 更新积分商城商品状态
     */
    @PostMapping("/pointsMallGoods/status/update")
    ResponseResult updatePointsMallGoodsStatus(@RequestBody Map<String, Object> param);

    // ================= 直播管理接口 =================

    /**
     * 分页查询直播列表
     */
    @PostMapping("/live/query")
    ResponseResult queryLiveList(@RequestBody Map<String, Object> param);

    /**
     * 新增直播
     */
    @PostMapping("/live/add")
    ResponseResult addLive(@RequestBody Map<String, Object> param);

    /**
     * 更新直播
     */
    @PostMapping("/live/update")
    ResponseResult updateLive(@RequestBody Map<String, Object> param);

    /**
     * 删除直播
     */
    @PostMapping("/live/delete")
    ResponseResult deleteLive(@RequestBody Map<String, Object> param);

    /**
     * 开始直播
     */
    @PostMapping("/live/start")
    ResponseResult startLive(@RequestBody Map<String, Object> param);

    /**
     * 结束直播
     */
    @PostMapping("/live/end")
    ResponseResult endLive(@RequestBody Map<String, Object> param);
    
    // ================= 营销活动管理接口 =================
    
    // 营销设置
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/marketing/setting/get")
    ResponseResult getMarketingSettings(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/marketing/setting/save")
    ResponseResult saveMarketingSettings(@RequestBody Map<String, Object> param);
    
    // 营销统计
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/marketing/statistics/overview")
    ResponseResult getMarketingStatisticsOverview(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/marketing/statistics/ranking")
    ResponseResult getMarketingActivityRanking(@RequestBody Map<String, Object> param);
    
    // 会员等级
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/marketing/member-level/list")
    ResponseResult getMemberLevelList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/marketing/member-level/save")
    ResponseResult saveMemberLevel(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/marketing/member-level/delete")
    ResponseResult deleteMemberLevel(@RequestBody Map<String, Object> param);
    
    // 积分管理
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/marketing/points/rule/list")
    ResponseResult getPointsRuleList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/marketing/points/rule/save")
    ResponseResult savePointsRule(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/marketing/points/rule/delete")
    ResponseResult deletePointsRule(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/marketing/points/statistics")
    ResponseResult getPointsStatistics(@RequestBody Map<String, Object> param);
    
    // 促销活动
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/marketing/promotion/list")
    ResponseResult getPromotionList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/marketing/promotion/detail")
    ResponseResult getPromotionDetail(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/marketing/promotion/save")
    ResponseResult savePromotion(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/marketing/promotion/delete")
    ResponseResult deletePromotion(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/marketing/promotion/toggle-status")
    ResponseResult togglePromotionStatus(@RequestBody Map<String, Object> param);
    
    // 优惠券管理
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/marketing/coupon/list")
    ResponseResult getMarketingCouponList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/marketing/coupon/save")
    ResponseResult saveMarketingCoupon(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/marketing/coupon/delete")
    ResponseResult deleteMarketingCoupon(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/marketing/coupon/issue")
    ResponseResult issueMarketingCoupon(@RequestBody Map<String, Object> param);

    // =============== 秒杀活动管理 ===============

    /**
     * 获取秒杀活动列表
     */
    @PostMapping("/marketing/seckill/list")
    ResponseResult getMarketingSeckillList(@RequestBody Map<String, Object> param);

    /**
     * 获取秒杀活动详情
     */
    @PostMapping("/marketing/seckill/detail")
    ResponseResult getMarketingSeckillDetail(@RequestBody Map<String, Object> param);

    /**
     * 审核秒杀活动
     */
    @PostMapping("/marketing/seckill/audit")
    ResponseResult auditMarketingSeckill(@RequestBody Map<String, Object> param);

    /**
     * 结束秒杀活动
     */
    @PostMapping("/marketing/seckill/end")
    ResponseResult endMarketingSeckill(@RequestBody Map<String, Object> param);

    // =============== 拼团活动管理 ===============

    /**
     * 获取拼团活动列表
     */
    @PostMapping("/marketing/group-buy/list")
    ResponseResult getMarketingGroupBuyList(@RequestBody Map<String, Object> param);

    /**
     * 获取拼团活动详情
     */
    @PostMapping("/marketing/group-buy/detail")
    ResponseResult getMarketingGroupBuyDetail(@RequestBody Map<String, Object> param);

    /**
     * 审核拼团活动
     */
    @PostMapping("/marketing/group-buy/audit")
    ResponseResult auditMarketingGroupBuy(@RequestBody Map<String, Object> param);

    /**
     * 结束拼团活动
     */
    @PostMapping("/marketing/group-buy/end")
    ResponseResult endMarketingGroupBuy(@RequestBody Map<String, Object> param);
}
