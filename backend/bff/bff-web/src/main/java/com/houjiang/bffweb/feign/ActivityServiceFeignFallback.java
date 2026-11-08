package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 活动服务Feign降级处理类
/**
 * 
 * @description 当活动服务不可用时提供降级响应
 * @author Amos
 * @date 2024-12-29
 */
@Component
public class ActivityServiceFeignFallback implements ActivityServiceFeign {

    @Override
    public ResponseResult getActivityStatistics(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult queryActivityList(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getActivityDetail(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult createActivity(Map<String, Object> activityData) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateActivity(Map<String, Object> activityData) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteActivity(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult pauseActivity(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult resumeActivity(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult copyActivity(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getActivityTypes() {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult batchUpdateStatus(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult queryCombinationList(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getCombinationDetail(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addCombination(Map<String, Object> combinationData) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateCombination(Map<String, Object> combinationData) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteCombination(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult enableCombination(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult queryDiscountList(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getDiscountDetail(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addDiscount(Map<String, Object> discountData) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateDiscount(Map<String, Object> discountData) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteDiscount(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult startDiscount(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult queryDiscountGoods(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult queryRedpacketTemplatePage(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult createRedpacketTemplate(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateRedpacketTemplate(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getRedpacketTemplateDetail(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult queryRedpacketDetailList(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult queryRedpacketUserList(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult selectRedpacketByDeposit(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult receiveRedpacket(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult querySpinwinActivityList(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getSpinwinActivityDetail(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateSpinwinActivity(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteSpinwinActivity(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateSpinwinLimit(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult querySpinwinLimit(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult querySpinwinDrawTimes(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult querySpinwinActivityExplain(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult drawSpinwinLottery(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult queryShopSeckillBaseList(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult queryShopSeckillBase(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addShopSeckillBase(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteShopSeckillBase(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult querySecondKillGoods(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addShopSeckillGoods(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteShopSeckillGoods(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult queryShopSeckillGoods(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult querySeckillGoodsSpecList(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult queryReductionPolicyBaseList(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addManjianzengzheBase(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult invalidateManjianzengzheBase(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteManjianzengzheBase(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addReductionGoods(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteReductionGoods(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult queryReductionGoodsDetailList(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult salesPromotionVoucherList(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult salesPromotionVoucherDetail(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult insertVoucherTemp(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult salesPromotionVoucherUpdate(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteSalesPromotionVoucher(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult insertVcBase(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult selectVcBase(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult selectVcTempbByShopId(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult queryFullReduceVcList(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult queryThousandsGuestsPricePage(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getThousandsGuestsPriceDetail(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult saveThousandsGuestsPriceConfig(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteThousandsGuestsPriceConfig(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateThousandsGuestsPriceStatus(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult queryThousandsGuestsPrice(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult queryLiveAccountPage(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getLiveAccountDetail(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult saveLiveAccount(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateLiveAccountStatus(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult queryLiveRoomPage(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getLiveRoomDetail(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult saveLiveRoom(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateLiveRoomStatus(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult sendLiveMessage(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult queryLiveMessageList(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult queryLiveGoodsAuditPage(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getLiveGoodsAuditDetail(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult saveLiveGoodsAudit(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteLiveGoodsAudit(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteLiveAccount(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult queryPointsMallToolPage(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getPointsMallToolDetail(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult savePointsMallTool(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deletePointsMallTool(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updatePointsMallToolStatus(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult queryRedpacketList(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addRedpacket(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateRedpacket(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteRedpacket(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult queryPointsMallGoodsList(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addPointsMallGoods(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updatePointsMallGoods(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deletePointsMallGoods(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updatePointsMallGoodsStatus(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult queryLiveList(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addLive(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateLive(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteLive(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult startLive(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult endLive(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteLiveRoom(Map<String, Object> param) {
        return ResponseResult.error("活动服务暂时不可用，请稍后重试");
    }
    
    /** =============== 营销活动管理降级 =============== */
    
    @Override
    public ResponseResult getMarketingSettings(Map<String, Object> param) {
        return ResponseResult.error("营销服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult saveMarketingSettings(Map<String, Object> param) {
        return ResponseResult.error("营销服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getMarketingStatisticsOverview(Map<String, Object> param) {
        return ResponseResult.error("营销服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getMarketingActivityRanking(Map<String, Object> param) {
        return ResponseResult.error("营销服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getMemberLevelList(Map<String, Object> param) {
        return ResponseResult.error("营销服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult saveMemberLevel(Map<String, Object> param) {
        return ResponseResult.error("营销服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult deleteMemberLevel(Map<String, Object> param) {
        return ResponseResult.error("营销服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getPointsRuleList(Map<String, Object> param) {
        return ResponseResult.error("营销服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult savePointsRule(Map<String, Object> param) {
        return ResponseResult.error("营销服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult deletePointsRule(Map<String, Object> param) {
        return ResponseResult.error("营销服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getPointsStatistics(Map<String, Object> param) {
        return ResponseResult.error("营销服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getPromotionList(Map<String, Object> param) {
        return ResponseResult.error("营销服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getPromotionDetail(Map<String, Object> param) {
        return ResponseResult.error("营销服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult savePromotion(Map<String, Object> param) {
        return ResponseResult.error("营销服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult deletePromotion(Map<String, Object> param) {
        return ResponseResult.error("营销服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult togglePromotionStatus(Map<String, Object> param) {
        return ResponseResult.error("营销服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getMarketingCouponList(Map<String, Object> param) {
        return ResponseResult.error("营销服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult saveMarketingCoupon(Map<String, Object> param) {
        return ResponseResult.error("营销服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult deleteMarketingCoupon(Map<String, Object> param) {
        return ResponseResult.error("营销服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult issueMarketingCoupon(Map<String, Object> param) {
        return ResponseResult.error("营销服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getMarketingSeckillList(Map<String, Object> param) {
        return ResponseResult.error("营销服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getMarketingSeckillDetail(Map<String, Object> param) {
        return ResponseResult.error("营销服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult auditMarketingSeckill(Map<String, Object> param) {
        return ResponseResult.error("营销服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult endMarketingSeckill(Map<String, Object> param) {
        return ResponseResult.error("营销服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getMarketingGroupBuyList(Map<String, Object> param) {
        return ResponseResult.error("营销服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getMarketingGroupBuyDetail(Map<String, Object> param) {
        return ResponseResult.error("营销服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult auditMarketingGroupBuy(Map<String, Object> param) {
        return ResponseResult.error("营销服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult endMarketingGroupBuy(Map<String, Object> param) {
        return ResponseResult.error("营销服务暂时不可用，请稍后重试");
    }

}