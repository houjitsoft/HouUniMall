package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.util.ResponseResult;
import com.houjiang.bffweb.feign.ActivityServiceFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 活动管理Web端BFF适配器
/**
 * 只做请求转发，不包含业务逻辑
 * 
 * @author Amos
 * @since 2024-12-29
 */
@RestController
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "houcloud.bff.activity.enabled", havingValue = "true")
@RequestMapping("/web/activity")
public class WebActivityController {

    @Autowired
    private ActivityServiceFeign activityServiceFeign;

    // ================= 活动管理接口 =================

    /**
     * 获取活动统计数据
     */
    @PostMapping("/manage/statistics")
    public ResponseResult getStatistics() {
        return activityServiceFeign.getActivityStatistics(null);
    }

    /**
     * 分页查询活动列表
     */
    @PostMapping("/manage/list")
    public ResponseResult getActivityList(@RequestBody Map<String, Object> queryParams) {
        return activityServiceFeign.queryActivityList(queryParams);
    }

    /**
     * 查询活动详情
     */
    @PostMapping("/manage/detail")
    public ResponseResult getActivityDetail(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.getActivityDetail(param);
    }

    /**
     * 创建活动
     */
    @PostMapping("/manage/create")
    public ResponseResult createActivity(@RequestBody Map<String, Object> activityData) {
        return activityServiceFeign.createActivity(activityData);
    }

    /**
     * 更新活动
     */
    @PostMapping("/manage/update")
    public ResponseResult updateActivity(@RequestBody Map<String, Object> activityData) {
        return activityServiceFeign.updateActivity(activityData);
    }

    /**
     * 删除活动
     */
    @PostMapping("/manage/delete")
    public ResponseResult deleteActivity(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.deleteActivity(param);
    }

    /**
     * 暂停活动
     */
    @PostMapping("/manage/pause")
    public ResponseResult pauseActivity(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.pauseActivity(param);
    }

    /**
     * 恢复活动
     */
    @PostMapping("/manage/resume")
    public ResponseResult resumeActivity(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.resumeActivity(param);
    }

    /**
     * 复制活动
     */
    @PostMapping("/manage/copy")
    public ResponseResult copyActivity(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.copyActivity(param);
    }

    /**
     * 获取活动类型列表
     */
    @GetMapping("/manage/types")
    public ResponseResult getActivityTypes() {
        return activityServiceFeign.getActivityTypes();
    }

    /**
     * 批量操作活动状态
     */
    @PostMapping("/manage/batch/status")
    public ResponseResult batchUpdateStatus(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.batchUpdateStatus(param);
    }

    // ================= 组合商品接口 =================

    /**
     * 分页查询组合商品列表
     */
    @PostMapping("/combination/query")
    public ResponseResult queryCombinationList(@RequestBody Map<String, Object> queryParams) {
        return activityServiceFeign.queryCombinationList(queryParams);
    }

    /**
     * 查询组合商品详情
     */
    @PostMapping("/combination/detail")
    public ResponseResult getCombinationDetail(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.getCombinationDetail(param);
    }

    /**
     * 新增组合商品
     */
    @PostMapping("/combination/add")
    public ResponseResult addCombination(@RequestBody Map<String, Object> combinationData) {
        return activityServiceFeign.addCombination(combinationData);
    }

    /**
     * 更新组合商品
     */
    @PostMapping("/combination/update")
    public ResponseResult updateCombination(@RequestBody Map<String, Object> combinationData) {
        return activityServiceFeign.updateCombination(combinationData);
    }

    /**
     * 删除组合商品
     */
    @PostMapping("/combination/delete")
    public ResponseResult deleteCombination(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.deleteCombination(param);
    }

    /**
     * 启用组合商品
     */
    @PostMapping("/combination/enable")
    public ResponseResult enableCombination(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.enableCombination(param);
    }

    // ================= 限时折扣接口 =================

    /**
     * 分页查询限时折扣列表
     */
    @PostMapping("/discount/query")
    public ResponseResult queryDiscountList(@RequestBody Map<String, Object> queryParams) {
        return activityServiceFeign.queryDiscountList(queryParams);
    }

    /**
     * 查询限时折扣详情
     */
    @PostMapping("/discount/detail")
    public ResponseResult getDiscountDetail(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.getDiscountDetail(param);
    }

    /**
     * 新增限时折扣
     */
    @PostMapping("/discount/add")
    public ResponseResult addDiscount(@RequestBody Map<String, Object> discountData) {
        return activityServiceFeign.addDiscount(discountData);
    }

    /**
     * 更新限时折扣
     */
    @PostMapping("/discount/update")
    public ResponseResult updateDiscount(@RequestBody Map<String, Object> discountData) {
        return activityServiceFeign.updateDiscount(discountData);
    }

    /**
     * 删除限时折扣
     */
    @PostMapping("/discount/delete")
    public ResponseResult deleteDiscount(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.deleteDiscount(param);
    }

    /**
     * 启动限时折扣
     */
    @PostMapping("/discount/start")
    public ResponseResult startDiscount(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.startDiscount(param);
    }

    /**
     * 查询折扣商品列表
     */
    @PostMapping("/discount/goods/query")
    public ResponseResult queryDiscountGoods(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.queryDiscountGoods(param);
    }

    // ================= 惊喜红包接口 =================

    /**
     * 分页查询红包列表
     */
    @PostMapping("/redpacket/query")
    public ResponseResult queryRedpacketList(@RequestBody Map<String, Object> queryParams) {
        return activityServiceFeign.queryRedpacketList(queryParams);
    }

    /**
     * 新增红包
     */
    @PostMapping("/redpacket/add")
    public ResponseResult addRedpacket(@RequestBody Map<String, Object> redpacketData) {
        return activityServiceFeign.addRedpacket(redpacketData);
    }

    /**
     * 更新红包
     */
    @PostMapping("/redpacket/update")
    public ResponseResult updateRedpacket(@RequestBody Map<String, Object> redpacketData) {
        return activityServiceFeign.updateRedpacket(redpacketData);
    }

    /**
     * 删除红包
     */
    @PostMapping("/redpacket/delete")
    public ResponseResult deleteRedpacket(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.deleteRedpacket(param);
    }

    /**
     * 分页查询红包模板列表
     */
    @PostMapping("/redpacket/template/page")
    public ResponseResult queryRedpacketTemplatePage(@RequestBody Map<String, Object> queryParams) {
        return activityServiceFeign.queryRedpacketTemplatePage(queryParams);
    }

    /**
     * 创建红包模板
     */
    @PostMapping("/redpacket/template/create")
    public ResponseResult createRedpacketTemplate(@RequestBody Map<String, Object> templateData) {
        return activityServiceFeign.createRedpacketTemplate(templateData);
    }

    /**
     * 更新红包模板
     */
    @PostMapping("/redpacket/template/update")
    public ResponseResult updateRedpacketTemplate(@RequestBody Map<String, Object> templateData) {
        return activityServiceFeign.updateRedpacketTemplate(templateData);
    }

    /**
     * 查询红包模板详情
     */
    @PostMapping("/redpacket/template/detail")
    public ResponseResult getRedpacketTemplateDetail(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.getRedpacketTemplateDetail(param);
    }

    /**
     * 查询红包发放明细列表
     */
    @PostMapping("/redpacket/base/detailList")
    public ResponseResult queryRedpacketDetailList(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.queryRedpacketDetailList(param);
    }

    /**
     * 查询红包领取用户列表
     */
    @PostMapping("/redpacket/base/userList")
    public ResponseResult queryRedpacketUserList(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.queryRedpacketUserList(param);
    }

    /**
     * 用户根据订单金额查询可用红包
     */
    @PostMapping("/redpacket/base/selectByDeposit")
    public ResponseResult selectRedpacketByDeposit(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.selectRedpacketByDeposit(param);
    }

    /**
     * 用户领取红包
     */
    @PostMapping("/redpacket/base/receive")
    public ResponseResult receiveRedpacket(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.receiveRedpacket(param);
    }

    // ================= 幸运大转盘接口 =================

    /**
     * 查询幸运大转盘活动列表
     */
    @PostMapping("/spinwin/list")
    public ResponseResult querySpinwinActivityList(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.querySpinwinActivityList(param);
    }

    /**
     * 查询幸运大转盘活动详情
     */
    @PostMapping("/spinwin/detail")
    public ResponseResult getSpinwinActivityDetail(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.getSpinwinActivityDetail(param);
    }

    /**
     * 新增或更新幸运大转盘活动
     */
    @PostMapping("/spinwin/update")
    public ResponseResult updateSpinwinActivity(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.updateSpinwinActivity(param);
    }

    /**
     * 删除幸运大转盘活动
     */
    @PostMapping("/spinwin/delete")
    public ResponseResult deleteSpinwinActivity(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.deleteSpinwinActivity(param);
    }

    /**
     * 更新抽奖限制
     */
    @PostMapping("/spinwin/limit/update")
    public ResponseResult updateSpinwinLimit(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.updateSpinwinLimit(param);
    }

    /**
     * 查询抽奖限制
     */
    @PostMapping("/spinwin/limit/query")
    public ResponseResult querySpinwinLimit(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.querySpinwinLimit(param);
    }

    /**
     * 查询用户抽奖次数
     */
    @PostMapping("/spinwin/draw/times")
    public ResponseResult querySpinwinDrawTimes(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.querySpinwinDrawTimes(param);
    }

    /**
     * 查询活动说明
     */
    @PostMapping("/spinwin/activity/explain")
    public ResponseResult querySpinwinActivityExplain(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.querySpinwinActivityExplain(param);
    }

    /**
     * 用户抽奖
     */
    @PostMapping("/spinwin/lottery/draw")
    public ResponseResult drawSpinwinLottery(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.drawSpinwinLottery(param);
    }

    // ================= 限时秒杀接口 =================

    /**
     * 查询店铺秒杀活动列表
     */
    @PostMapping("/seckill/base/list")
    public ResponseResult queryShopSeckillBaseList(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.queryShopSeckillBaseList(param);
    }

    /**
     * 查询店铺秒杀活动详情
     */
    @PostMapping("/seckill/base/detail")
    public ResponseResult queryShopSeckillBase(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.queryShopSeckillBase(param);
    }

    /**
     * 创建或更新店铺秒杀活动
     */
    @PostMapping("/seckill/base/save")
    public ResponseResult addShopSeckillBase(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.addShopSeckillBase(param);
    }

    /**
     * 删除店铺秒杀活动
     */
    @PostMapping("/seckill/base/delete")
    public ResponseResult deleteShopSeckillBase(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.deleteShopSeckillBase(param);
    }

    /**
     * 查询限时秒杀商品列表
     */
    @PostMapping("/seckill/goods/list")
    public ResponseResult querySecondKillGoods(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.querySecondKillGoods(param);
    }

    /**
     * 创建或更新限时秒杀商品
     */
    @PostMapping("/seckill/goods/save")
    public ResponseResult addShopSeckillGoods(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.addShopSeckillGoods(param);
    }

    /**
     * 删除限时秒杀商品
     */
    @PostMapping("/seckill/goods/delete")
    public ResponseResult deleteShopSeckillGoods(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.deleteShopSeckillGoods(param);
    }

    /**
     * 查询秒杀活动商品（管理列表）
     */
    @PostMapping("/seckill/goods/query")
    public ResponseResult queryShopSeckillGoods(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.queryShopSeckillGoods(param);
    }

    /**
     * 查询秒杀活动规格列表
     */
    @PostMapping("/seckill/goods/spec/list")
    public ResponseResult querySeckillGoodsSpecList(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.querySeckillGoodsSpecList(param);
    }

    // ================= 满减满折（满减赠折）接口 =================

    /**
     * 查询满减赠折活动列表
     */
    @PostMapping("/reduction/base/list")
    public ResponseResult queryReductionPolicyBaseList(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.queryReductionPolicyBaseList(param);
    }

    /**
     * 新增满减赠折活动
     */
    @PostMapping("/reduction/base/add")
    public ResponseResult addManjianzengzheBase(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.addManjianzengzheBase(param);
    }

    /**
     * 失效满减赠折活动
     */
    @PostMapping("/reduction/base/invalidate")
    public ResponseResult invalidateManjianzengzheBase(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.invalidateManjianzengzheBase(param);
    }

    /**
     * 删除满减赠折活动
     */
    @PostMapping("/reduction/base/delete")
    public ResponseResult deleteManjianzengzheBase(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.deleteManjianzengzheBase(param);
    }

    /**
     * 新增活动商品
     */
    @PostMapping("/reduction/goods/add")
    public ResponseResult addReductionGoods(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.addReductionGoods(param);
    }

    /**
     * 删除活动商品
     */
    @PostMapping("/reduction/goods/delete")
    public ResponseResult deleteReductionGoods(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.deleteReductionGoods(param);
    }

    /**
     * 查看满减赠折活动详情（商品列表）
     */
    @PostMapping("/reduction/goods/list")
    public ResponseResult queryReductionGoodsDetailList(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.queryReductionGoodsDetailList(param);
    }

    // ================= 店铺优惠券接口 =================

    /**
     * 平台查询店铺优惠券列表
     */
    @PostMapping("/voucher/platform/list")
    public ResponseResult salesPromotionVoucherList(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.salesPromotionVoucherList(param);
    }

    /**
     * 平台查询店铺优惠券详情
     */
    @PostMapping("/voucher/platform/detail")
    public ResponseResult salesPromotionVoucherDetail(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.salesPromotionVoucherDetail(param);
    }

    /**
     * 平台新增店铺优惠券
     */
    @PostMapping("/voucher/platform/add")
    public ResponseResult insertVoucherTemp(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.insertVoucherTemp(param);
    }

    /**
     * 平台修改店铺优惠券
     */
    @PostMapping("/voucher/platform/update")
    public ResponseResult salesPromotionVoucherUpdate(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.salesPromotionVoucherUpdate(param);
    }

    /**
     * 平台删除店铺优惠券
     */
    @PostMapping("/voucher/platform/delete")
    public ResponseResult deleteSalesPromotionVoucher(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.deleteSalesPromotionVoucher(param);
    }

    /**
     * 用户领取优惠券
     */
    @PostMapping("/voucher/user/receive")
    public ResponseResult insertVcBase(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.insertVcBase(param);
    }

    /**
     * 用户查询未使用优惠券
     */
    @PostMapping("/voucher/user/list")
    public ResponseResult selectVcBase(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.selectVcBase(param);
    }

    /**
     * 根据店铺查询代金券信息
     */
    @PostMapping("/voucher/shop/list")
    public ResponseResult selectVcTempbByShopId(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.selectVcTempbByShopId(param);
    }

    /**
     * 查询优惠券商品列表
     */
    @PostMapping("/voucher/goods/list")
    public ResponseResult queryFullReduceVcList(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.queryFullReduceVcList(param);
    }

    // ================= 千客千价接口 =================

    /**
     * 分页查询千客千价配置列表
     */
    @PostMapping("/thousandsPrice/config/page")
    public ResponseResult queryThousandsGuestsPricePage(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.queryThousandsGuestsPricePage(param);
    }

    /**
     * 查询千客千价配置详情
     */
    @PostMapping("/thousandsPrice/config/detail")
    public ResponseResult getThousandsGuestsPriceDetail(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.getThousandsGuestsPriceDetail(param);
    }

    /**
     * 新增或更新千客千价配置
     */
    @PostMapping("/thousandsPrice/config/save")
    public ResponseResult saveThousandsGuestsPriceConfig(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.saveThousandsGuestsPriceConfig(param);
    }

    /**
     * 删除千客千价配置
     */
    @PostMapping("/thousandsPrice/config/delete")
    public ResponseResult deleteThousandsGuestsPriceConfig(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.deleteThousandsGuestsPriceConfig(param);
    }

    /**
     * 更新千客千价配置状态
     */
    @PostMapping("/thousandsPrice/config/status/update")
    public ResponseResult updateThousandsGuestsPriceStatus(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.updateThousandsGuestsPriceStatus(param);
    }

    /**
     * 查询用户千客千价个性化价格
     */
    @PostMapping("/thousandsPrice/price/query")
    public ResponseResult queryThousandsGuestsPrice(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.queryThousandsGuestsPrice(param);
    }

    // ================= 直播账号管理接口 =================

    /**
     * 分页查询直播账号
     */
    @PostMapping("/liveAccount/page")
    public ResponseResult queryLiveAccountPage(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.queryLiveAccountPage(param);
    }

    /**
     * 直播账号详情
     */
    @PostMapping("/liveAccount/detail")
    public ResponseResult getLiveAccountDetail(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.getLiveAccountDetail(param);
    }

    /**
     * 新增或更新直播账号
     */
    @PostMapping("/liveAccount/save")
    public ResponseResult saveLiveAccount(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.saveLiveAccount(param);
    }

    /**
     * 更新直播账号状态
     */
    @PostMapping("/liveAccount/status/update")
    public ResponseResult updateLiveAccountStatus(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.updateLiveAccountStatus(param);
    }

    /**
     * 删除直播账号
     */
    @PostMapping("/liveAccount/delete")
    public ResponseResult deleteLiveAccount(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.deleteLiveAccount(param);
    }

    // ================= 直播间 & 互动接口 =================

    /**
     * 分页查询直播间
     */
    @PostMapping("/liveRoom/page")
    public ResponseResult queryLiveRoomPage(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.queryLiveRoomPage(param);
    }

    /**
     * 直播间详情
     */
    @PostMapping("/liveRoom/detail")
    public ResponseResult getLiveRoomDetail(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.getLiveRoomDetail(param);
    }

    /**
     * 新增或更新直播间
     */
    @PostMapping("/liveRoom/save")
    public ResponseResult saveLiveRoom(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.saveLiveRoom(param);
    }

    /**
     * 更新直播间状态
     */
    @PostMapping("/liveRoom/status/update")
    public ResponseResult updateLiveRoomStatus(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.updateLiveRoomStatus(param);
    }

    /**
     * 删除直播间
     */
    @PostMapping("/liveRoom/delete")
    public ResponseResult deleteLiveRoom(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.deleteLiveRoom(param);
    }

    /**
     * 发送直播互动消息（弹幕/点赞/礼物）
     */
    @PostMapping("/liveRoom/message/send")
    public ResponseResult sendLiveMessage(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.sendLiveMessage(param);
    }

    /**
     * 查询直播间消息列表
     */
    @PostMapping("/liveRoom/message/list")
    public ResponseResult queryLiveMessageList(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.queryLiveMessageList(param);
    }

    // ================= 直播商品审核接口 =================

    /**
     * 分页查询直播商品审核记录
     */
    @PostMapping("/liveGoodsAudit/page")
    public ResponseResult queryLiveGoodsAuditPage(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.queryLiveGoodsAuditPage(param);
    }

    /**
     * 直播商品审核详情
     */
    @PostMapping("/liveGoodsAudit/detail")
    public ResponseResult getLiveGoodsAuditDetail(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.getLiveGoodsAuditDetail(param);
    }

    /**
     * 新增/更新直播商品审核记录
     */
    @PostMapping("/liveGoodsAudit/save")
    public ResponseResult saveLiveGoodsAudit(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.saveLiveGoodsAudit(param);
    }

    /**
     * 删除直播商品审核记录
     */
    @PostMapping("/liveGoodsAudit/delete")
    public ResponseResult deleteLiveGoodsAudit(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.deleteLiveGoodsAudit(param);
    }

    // ================= 积分商城工具接口 =================

    /**
     * 分页查询积分商城工具配置列表
     */
    @PostMapping("/pointsMallTool/page")
    public ResponseResult queryPointsMallToolPage(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.queryPointsMallToolPage(param);
    }

    /**
     * 查询积分商城工具配置详情
     */
    @PostMapping("/pointsMallTool/detail")
    public ResponseResult getPointsMallToolDetail(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.getPointsMallToolDetail(param);
    }

    /**
     * 新增或更新积分商城工具配置
     */
    @PostMapping("/pointsMallTool/save")
    public ResponseResult savePointsMallTool(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.savePointsMallTool(param);
    }

    /**
     * 删除积分商城工具配置
     */
    @PostMapping("/pointsMallTool/delete")
    public ResponseResult deletePointsMallTool(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.deletePointsMallTool(param);
    }

    /**
     * 更新积分商城工具启用状态
     */
    @PostMapping("/pointsMallTool/status/update")
    public ResponseResult updatePointsMallToolStatus(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.updatePointsMallToolStatus(param);
    }

    // ================= 积分商城接口 =================

    /**
     * 分页查询积分商城商品列表
     */
    @PostMapping("/pointsmall/query")
    public ResponseResult queryPointsMallGoodsList(@RequestBody Map<String, Object> queryParams) {
        return activityServiceFeign.queryPointsMallGoodsList(queryParams);
    }

    /**
     * 新增积分商城商品
     */
    @PostMapping("/pointsmall/add")
    public ResponseResult addPointsMallGoods(@RequestBody Map<String, Object> goodsData) {
        return activityServiceFeign.addPointsMallGoods(goodsData);
    }

    /**
     * 更新积分商城商品
     */
    @PostMapping("/pointsmall/update")
    public ResponseResult updatePointsMallGoods(@RequestBody Map<String, Object> goodsData) {
        return activityServiceFeign.updatePointsMallGoods(goodsData);
    }

    /**
     * 删除积分商城商品
     */
    @PostMapping("/pointsmall/delete")
    public ResponseResult deletePointsMallGoods(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.deletePointsMallGoods(param);
    }

    /**
     * 更新积分商城商品状态
     */
    @PostMapping("/pointsmall/status")
    public ResponseResult updatePointsMallGoodsStatus(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.updatePointsMallGoodsStatus(param);
    }

    // ================= 直播管理接口 =================

    /**
     * 分页查询直播列表
     */
    @PostMapping("/live/query")
    public ResponseResult queryLiveList(@RequestBody Map<String, Object> queryParams) {
        return activityServiceFeign.queryLiveList(queryParams);
    }

    /**
     * 新增直播
     */
    @PostMapping("/live/add")
    public ResponseResult addLive(@RequestBody Map<String, Object> liveData) {
        return activityServiceFeign.addLive(liveData);
    }

    /**
     * 更新直播
     */
    @PostMapping("/live/update")
    public ResponseResult updateLive(@RequestBody Map<String, Object> liveData) {
        return activityServiceFeign.updateLive(liveData);
    }

    /**
     * 删除直播
     */
    @PostMapping("/live/delete")
    public ResponseResult deleteLive(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.deleteLive(param);
    }

    /**
     * 开始直播
     */
    @PostMapping("/live/start")
    public ResponseResult startLive(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.startLive(param);
    }

    /**
     * 结束直播
     */
    @PostMapping("/live/end")
    public ResponseResult endLive(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.endLive(param);
    }
}
