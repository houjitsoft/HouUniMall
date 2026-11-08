package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.ActivityServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 商城营销活动Controller
/**
 * 提供优惠券、促销活动、积分管理、会员等级、营销统计、营销设置等功能
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/web/mall/marketing")
public class MallMarketingController {
    
    @Autowired
    private ActivityServiceFeign activityServiceFeign;

    // =============== 营销设置 ===============
    
    /**
     * 获取营销设置
     */
    @PostMapping("/setting/get")
    public ResponseResult getSettings(@RequestBody Map<String, Object> params) {
        try {
            return activityServiceFeign.getMarketingSettings(params);
        } catch (Exception e) {
            log.error("获取营销设置失败", e);
            return ResponseResult.fail(500, "获取营销设置失败");
        }
    }
    
    /**
     * 保存营销设置
     */
    @PostMapping("/setting/save")
    public ResponseResult saveSettings(@RequestBody Map<String, Object> params) {
        try {
            return activityServiceFeign.saveMarketingSettings(params);
        } catch (Exception e) {
            log.error("保存营销设置失败", e);
            return ResponseResult.fail(500, "保存营销设置失败");
        }
    }

    // =============== 营销统计 ===============
    
    /**
     * 营销统计概览
     */
    @PostMapping("/statistics/overview")
    public ResponseResult getStatisticsOverview(@RequestBody Map<String, Object> params) {
        try {
            return activityServiceFeign.getMarketingStatisticsOverview(params);
        } catch (Exception e) {
            log.error("获取营销统计概览失败", e);
            return ResponseResult.fail(500, "获取营销统计概览失败");
        }
    }
    
    /**
     * 营销活动排行
     */
    @PostMapping("/statistics/ranking")
    public ResponseResult getActivityRanking(@RequestBody Map<String, Object> params) {
        try {
            return activityServiceFeign.getMarketingActivityRanking(params);
        } catch (Exception e) {
            log.error("获取营销活动排行失败", e);
            return ResponseResult.fail(500, "获取营销活动排行失败");
        }
    }

    // =============== 会员等级 ===============
    
    /**
     * 获取会员等级列表
     */
    @PostMapping("/member-level/list")
    public ResponseResult getMemberLevelList(@RequestBody Map<String, Object> params) {
        try {
            return activityServiceFeign.getMemberLevelList(params);
        } catch (Exception e) {
            log.error("获取会员等级列表失败", e);
            return ResponseResult.fail(500, "获取会员等级列表失败");
        }
    }

    /**
     * 保存会员等级
     */
    @PostMapping("/member-level/save")
    public ResponseResult saveMemberLevel(@RequestBody Map<String, Object> params) {
        try {
            return activityServiceFeign.saveMemberLevel(params);
        } catch (Exception e) {
            log.error("保存会员等级失败", e);
            return ResponseResult.fail(500, "保存会员等级失败");
        }
    }
    
    /**
     * 删除会员等级
     */
    @PostMapping("/member-level/delete")
    public ResponseResult deleteMemberLevel(@RequestBody Map<String, Object> params) {
        try {
            return activityServiceFeign.deleteMemberLevel(params);
        } catch (Exception e) {
            log.error("删除会员等级失败", e);
            return ResponseResult.fail(500, "删除会员等级失败");
        }
    }

    // =============== 积分管理 ===============
    
    /**
     * 获取积分规则列表
     */
    @PostMapping("/points/rule/list")
    public ResponseResult getPointsRuleList(@RequestBody Map<String, Object> params) {
        try {
            return activityServiceFeign.getPointsRuleList(params);
        } catch (Exception e) {
            log.error("获取积分规则列表失败", e);
            return ResponseResult.fail(500, "获取积分规则列表失败");
        }
    }
    
    /**
     * 保存积分规则
     */
    @PostMapping("/points/rule/save")
    public ResponseResult savePointsRule(@RequestBody Map<String, Object> params) {
        try {
            return activityServiceFeign.savePointsRule(params);
        } catch (Exception e) {
            log.error("保存积分规则失败", e);
            return ResponseResult.fail(500, "保存积分规则失败");
        }
    }
    
    /**
     * 删除积分规则
     */
    @PostMapping("/points/rule/delete")
    public ResponseResult deletePointsRule(@RequestBody Map<String, Object> params) {
        try {
            return activityServiceFeign.deletePointsRule(params);
        } catch (Exception e) {
            log.error("删除积分规则失败", e);
            return ResponseResult.fail(500, "删除积分规则失败");
        }
    }
    
    /**
     * 获取积分统计
     */
    @PostMapping("/points/statistics")
    public ResponseResult getPointsStatistics(@RequestBody Map<String, Object> params) {
        try {
            return activityServiceFeign.getPointsStatistics(params);
        } catch (Exception e) {
            log.error("获取积分统计失败", e);
            return ResponseResult.fail(500, "获取积分统计失败");
        }
    }

    // =============== 促销活动 ===============

    /**
     * 获取促销活动列表
     */
    @PostMapping("/promotion/list")
    public ResponseResult getPromotionList(@RequestBody Map<String, Object> params) {
        try {
            return activityServiceFeign.getPromotionList(params);
        } catch (Exception e) {
            log.error("获取促销活动列表失败", e);
            return ResponseResult.fail(500, "获取促销活动列表失败");
        }
    }
    
    /**
     * 获取促销活动详情
     */
    @PostMapping("/promotion/detail")
    public ResponseResult getPromotionDetail(@RequestBody Map<String, Object> params) {
        try {
            return activityServiceFeign.getPromotionDetail(params);
        } catch (Exception e) {
            log.error("获取促销活动详情失败", e);
            return ResponseResult.fail(500, "获取促销活动详情失败");
        }
    }

    /**
     * 保存促销活动
     */
    @PostMapping("/promotion/save")
    public ResponseResult savePromotion(@RequestBody Map<String, Object> params) {
        try {
            return activityServiceFeign.savePromotion(params);
        } catch (Exception e) {
            log.error("保存促销活动失败", e);
            return ResponseResult.fail(500, "保存促销活动失败");
        }
    }
    
    /**
     * 删除促销活动
     */
    @PostMapping("/promotion/delete")
    public ResponseResult deletePromotion(@RequestBody Map<String, Object> params) {
        try {
            return activityServiceFeign.deletePromotion(params);
        } catch (Exception e) {
            log.error("删除促销活动失败", e);
            return ResponseResult.fail(500, "删除促销活动失败");
        }
    }
    
    /**
     * 启用/停用促销活动
     */
    @PostMapping("/promotion/toggle-status")
    public ResponseResult togglePromotionStatus(@RequestBody Map<String, Object> params) {
        try {
            return activityServiceFeign.togglePromotionStatus(params);
        } catch (Exception e) {
            log.error("启用/停用促销活动失败", e);
            return ResponseResult.fail(500, "启用/停用促销活动失败");
        }
    }

    // =============== 优惠券管理 ===============

    /**
     * 获取优惠券列表
     */
    @PostMapping("/coupon/list")
    public ResponseResult getCouponList(@RequestBody Map<String, Object> params) {
        try {
            return activityServiceFeign.getMarketingCouponList(params);
        } catch (Exception e) {
            log.error("获取优惠券列表失败", e);
            return ResponseResult.fail(500, "获取优惠券列表失败");
        }
    }

    /**
     * 保存优惠券
     */
    @PostMapping("/coupon/save")
    public ResponseResult saveCoupon(@RequestBody Map<String, Object> params) {
        try {
            return activityServiceFeign.saveMarketingCoupon(params);
        } catch (Exception e) {
            log.error("保存优惠券失败", e);
            return ResponseResult.fail(500, "保存优惠券失败");
        }
    }
    
    /**
     * 删除优惠券
     */
    @PostMapping("/coupon/delete")
    public ResponseResult deleteCoupon(@RequestBody Map<String, Object> params) {
        try {
            return activityServiceFeign.deleteMarketingCoupon(params);
        } catch (Exception e) {
            log.error("删除优惠券失败", e);
            return ResponseResult.fail(500, "删除优惠券失败");
        }
    }
    
    /**
     * 发放优惠券
     */
    @PostMapping("/coupon/issue")
    public ResponseResult issueCoupon(@RequestBody Map<String, Object> params) {
        try {
            return activityServiceFeign.issueMarketingCoupon(params);
        } catch (Exception e) {
            log.error("发放优惠券失败", e);
            return ResponseResult.fail(500, "发放优惠券失败");
        }
    }

    // =============== 秒杀活动管理 ===============

    /**
     * 获取秒杀活动列表（分页）
/**
     * @param params 查询参数，支持 page/limit/status/keyword/shopId
     * @return 秒杀活动分页数据
     */
    @PostMapping("/seckill/list")
    public ResponseResult getSeckillList(@RequestBody Map<String, Object> params) {
        try {
            return activityServiceFeign.getMarketingSeckillList(params);
        } catch (Exception e) {
            log.error("获取秒杀活动列表失败", e);
            return ResponseResult.fail(500, "获取秒杀活动列表失败");
        }
    }

    /**
     * 获取秒杀活动详情
/**
     * @param params 包含 seckillId 的参数
     * @return 秒杀活动详情
     */
    @PostMapping("/seckill/detail")
    public ResponseResult getSeckillDetail(@RequestBody Map<String, Object> params) {
        try {
            return activityServiceFeign.getMarketingSeckillDetail(params);
        } catch (Exception e) {
            log.error("获取秒杀活动详情失败", e);
            return ResponseResult.fail(500, "获取秒杀活动详情失败");
        }
    }

    /**
     * 审核秒杀活动
/**
     * @param params 包含 seckillId 和 auditStatus(1-通过, 2-拒绝) 的参数
     * @return 审核结果
     */
    @PostMapping("/seckill/audit")
    public ResponseResult auditSeckill(@RequestBody Map<String, Object> params) {
        try {
            return activityServiceFeign.auditMarketingSeckill(params);
        } catch (Exception e) {
            log.error("审核秒杀活动失败", e);
            return ResponseResult.fail(500, "审核秒杀活动失败");
        }
    }

    /**
     * 结束秒杀活动
/**
     * @param params 包含 seckillId 的参数
     * @return 操作结果
     */
    @PostMapping("/seckill/end")
    public ResponseResult endSeckill(@RequestBody Map<String, Object> params) {
        try {
            return activityServiceFeign.endMarketingSeckill(params);
        } catch (Exception e) {
            log.error("结束秒杀活动失败", e);
            return ResponseResult.fail(500, "结束秒杀活动失败");
        }
    }

    // =============== 拼团活动管理 ===============

    /**
     * 获取拼团活动列表（分页）
/**
     * @param params 查询参数，支持 page/limit/status/keyword/shopId
     * @return 拼团活动分页数据
     */
    @PostMapping("/group-buy/list")
    public ResponseResult getGroupBuyList(@RequestBody Map<String, Object> params) {
        try {
            return activityServiceFeign.getMarketingGroupBuyList(params);
        } catch (Exception e) {
            log.error("获取拼团活动列表失败", e);
            return ResponseResult.fail(500, "获取拼团活动列表失败");
        }
    }

    /**
     * 获取拼团活动详情
/**
     * @param params 包含 pintuanId 的参数
     * @return 拼团活动详情
     */
    @PostMapping("/group-buy/detail")
    public ResponseResult getGroupBuyDetail(@RequestBody Map<String, Object> params) {
        try {
            return activityServiceFeign.getMarketingGroupBuyDetail(params);
        } catch (Exception e) {
            log.error("获取拼团活动详情失败", e);
            return ResponseResult.fail(500, "获取拼团活动详情失败");
        }
    }

    /**
     * 审核拼团活动
/**
     * @param params 包含 pintuanId 和 auditStatus(1-通过, 2-拒绝) 的参数
     * @return 审核结果
     */
    @PostMapping("/group-buy/audit")
    public ResponseResult auditGroupBuy(@RequestBody Map<String, Object> params) {
        try {
            return activityServiceFeign.auditMarketingGroupBuy(params);
        } catch (Exception e) {
            log.error("审核拼团活动失败", e);
            return ResponseResult.fail(500, "审核拼团活动失败");
        }
    }

    /**
     * 结束拼团活动
/**
     * @param params 包含 pintuanId 的参数
     * @return 操作结果
     */
    @PostMapping("/group-buy/end")
    public ResponseResult endGroupBuy(@RequestBody Map<String, Object> params) {
        try {
            return activityServiceFeign.endMarketingGroupBuy(params);
        } catch (Exception e) {
            log.error("结束拼团活动失败", e);
            return ResponseResult.fail(500, "结束拼团活动失败");
        }
    }
}
