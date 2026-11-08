package com.houjiang.mall.service.distribution;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.distribution.CommissionRule;
import com.houjiang.mall.mapper.distribution.CommissionRuleMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 佣金规则服务类
 * 
 * @author Amos
 */
@Service
public class CommissionRuleService extends ServiceImpl<CommissionRuleMapper, CommissionRule> {

    /**
     * 获取店铺的佣金规则列表
     *
     * @param shopId 店铺ID
     * @return 规则列表
     */
    public List<CommissionRule> listByShop(Long shopId) {
        LambdaQueryWrapper<CommissionRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommissionRule::getShopId, shopId);
        wrapper.eq(CommissionRule::getStatus, 1);
        wrapper.orderByDesc(CommissionRule::getPriority);
        return this.list(wrapper);
    }

    /**
     * 创建佣金规则
     *
     * @param rule 规则信息
     * @return 是否成功
     */
    public boolean createRule(CommissionRule rule) {
        rule.setStatus(1);
        rule.setCreateTime(LocalDateTime.now());
        rule.setUpdateTime(LocalDateTime.now());
        return this.save(rule);
    }

    /**
     * 获取适用的佣金规则
     *
     * @param shopId 店铺ID
     * @param categoryId 分类ID
     * @param goodsId 商品ID
     * @return 最优规则
     */
    public CommissionRule getApplicableRule(Long shopId, Long categoryId, Long goodsId) {
        // 优先商品规则（goodsId 为空时跳过，避免 related_id IS NULL 误命中）
        if (goodsId != null) {
            LambdaQueryWrapper<CommissionRule> goodsWrapper = new LambdaQueryWrapper<>();
            goodsWrapper.eq(CommissionRule::getShopId, shopId);
            goodsWrapper.eq(CommissionRule::getType, 3);
            goodsWrapper.eq(CommissionRule::getRelatedId, goodsId);
            goodsWrapper.eq(CommissionRule::getStatus, 1);
            goodsWrapper.last("limit 1");
            CommissionRule goodsRule = this.getOne(goodsWrapper);
            if (goodsRule != null) {
                return goodsRule;
            }
        }

        // 其次分类规则
        if (categoryId != null) {
            LambdaQueryWrapper<CommissionRule> categoryWrapper = new LambdaQueryWrapper<>();
            categoryWrapper.eq(CommissionRule::getShopId, shopId);
            categoryWrapper.eq(CommissionRule::getType, 2);
            categoryWrapper.eq(CommissionRule::getRelatedId, categoryId);
            categoryWrapper.eq(CommissionRule::getStatus, 1);
            categoryWrapper.last("limit 1");
            CommissionRule categoryRule = this.getOne(categoryWrapper);
            if (categoryRule != null) {
                return categoryRule;
            }
        }

        // 最后全店规则
        LambdaQueryWrapper<CommissionRule> shopWrapper = new LambdaQueryWrapper<>();
        shopWrapper.eq(CommissionRule::getShopId, shopId);
        shopWrapper.eq(CommissionRule::getType, 1);
        shopWrapper.eq(CommissionRule::getStatus, 1);
        shopWrapper.last("limit 1");
        return this.getOne(shopWrapper);
    }
}
