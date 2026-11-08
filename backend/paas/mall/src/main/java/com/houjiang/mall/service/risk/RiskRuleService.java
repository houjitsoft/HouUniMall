package com.houjiang.mall.service.risk;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.risk.RiskRule;
import com.houjiang.mall.mapper.risk.RiskRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 风控规则服务类
 * 
 * @author Amos
 */
@Service
@RequiredArgsConstructor
public class RiskRuleService extends ServiceImpl<RiskRuleMapper, RiskRule> {

    /**
     * 分页查询风控规则
     */
    public IPage<RiskRule> pageList(Map<String, Object> params) {
        Integer pageNum = (Integer) params.getOrDefault("pageNum", 1);
        Integer pageSize = (Integer) params.getOrDefault("pageSize", 10);
        String ruleName = (String) params.get("ruleName");
        Integer ruleType = params.get("ruleType") != null ? Integer.parseInt(params.get("ruleType").toString()) : null;
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;
        Long shopId = params.get("shopId") != null ? Long.parseLong(params.get("shopId").toString()) : null;

        LambdaQueryWrapper<RiskRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(ruleName), RiskRule::getRuleName, ruleName)
               .eq(ruleType != null, RiskRule::getRuleType, ruleType)
               .eq(status != null, RiskRule::getStatus, status)
               .eq(shopId != null, RiskRule::getShopId, shopId)
               .orderByDesc(RiskRule::getCreateTime);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 获取启用的规则列表
     */
    public List<RiskRule> listEnabled(Long shopId) {
        LambdaQueryWrapper<RiskRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RiskRule::getStatus, 1)
               .eq(shopId != null, RiskRule::getShopId, shopId);
        return list(wrapper);
    }

    /**
     * 创建风控规则
     */
    public Boolean createRule(RiskRule rule) {
        rule.setCreateTime(LocalDateTime.now());
        rule.setUpdateTime(LocalDateTime.now());
        return save(rule);
    }

    /**
     * 更新风控规则
     */
    public Boolean updateRule(RiskRule rule) {
        rule.setUpdateTime(LocalDateTime.now());
        return updateById(rule);
    }

    /**
     * 更新规则状态
     */
    public Boolean updateStatus(Long id, Integer status) {
        RiskRule rule = new RiskRule();
        rule.setId(id);
        rule.setStatus(status);
        rule.setUpdateTime(LocalDateTime.now());
        return updateById(rule);
    }
}
