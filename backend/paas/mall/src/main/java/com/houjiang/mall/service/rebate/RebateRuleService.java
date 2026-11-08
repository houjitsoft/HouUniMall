package com.houjiang.mall.service.rebate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.rebate.RebateRule;
import com.houjiang.mall.mapper.rebate.RebateRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 返利规则服务类
 * 
 * @author Amos
 */
@Service
@RequiredArgsConstructor
public class RebateRuleService extends ServiceImpl<RebateRuleMapper, RebateRule> {

    /**
     * 分页查询返利规则
     * 
     * @param params 查询参数
     * @return 分页数据
     */
    public IPage<RebateRule> pageList(Map<String, Object> params) {
        Integer pageNum = (Integer) params.getOrDefault("pageNum", 1);
        Integer pageSize = (Integer) params.getOrDefault("pageSize", 10);
        String ruleName = (String) params.get("ruleName");
        Integer rebateType = params.get("rebateType") != null ? Integer.parseInt(params.get("rebateType").toString()) : null;
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;
        Long shopId = params.get("shopId") != null ? Long.parseLong(params.get("shopId").toString()) : null;

        LambdaQueryWrapper<RebateRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(ruleName), RebateRule::getRuleName, ruleName)
               .eq(rebateType != null, RebateRule::getRebateType, rebateType)
               .eq(status != null, RebateRule::getStatus, status)
               .eq(shopId != null, RebateRule::getShopId, shopId)
               .orderByDesc(RebateRule::getCreateTime);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 创建返利规则
     * 
     * @param rule 规则信息
     * @return 是否成功
     */
    public Boolean createRule(RebateRule rule) {
        rule.setCreateTime(LocalDateTime.now());
        rule.setUpdateTime(LocalDateTime.now());
        return save(rule);
    }

    /**
     * 更新返利规则
     * 
     * @param rule 规则信息
     * @return 是否成功
     */
    public Boolean updateRule(RebateRule rule) {
        rule.setUpdateTime(LocalDateTime.now());
        return updateById(rule);
    }

    /**
     * 更新规则状态
     * 
     * @param id 规则ID
     * @param status 状态
     * @return 是否成功
     */
    public Boolean updateStatus(Long id, Integer status) {
        RebateRule rule = new RebateRule();
        rule.setId(id);
        rule.setStatus(status);
        rule.setUpdateTime(LocalDateTime.now());
        return updateById(rule);
    }
}
