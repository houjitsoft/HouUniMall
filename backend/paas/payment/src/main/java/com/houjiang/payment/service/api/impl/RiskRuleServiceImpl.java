package com.houjiang.payment.service.api.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.payment.dto.risk.RiskRuleCreateRequest;
import com.houjiang.payment.dto.risk.RiskRuleListRequest;
import com.houjiang.payment.dto.risk.RiskRuleUpdateRequest;
import com.houjiang.payment.entity.PayRiskRuleEntity;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.mapper.PayRiskRuleMapper;
import com.houjiang.payment.service.api.RiskRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 风控规则 CRUD 服务实现
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskRuleServiceImpl implements RiskRuleService {

    private final PayRiskRuleMapper ruleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayRiskRuleEntity create(RiskRuleCreateRequest req, String operatorId) {
        log.info("[risk/rule/create] ruleNo={}, ruleType={}, action={}", req.getRuleNo(), req.getRuleType(), req.getAction());
        // 1. 唯一校验
        Long cnt = ruleMapper.selectCount(new LambdaQueryWrapper<PayRiskRuleEntity>()
                .eq(PayRiskRuleEntity::getRuleNo, req.getRuleNo()));
        if (cnt != null && cnt > 0) {
            throw PaymentException.bizError("规则编号已存在: " + req.getRuleNo());
        }

        PayRiskRuleEntity entity = new PayRiskRuleEntity();
        entity.setRuleNo(req.getRuleNo());
        entity.setRuleName(req.getRuleName());
        entity.setRuleType(req.getRuleType());
        entity.setBizType(req.getBizType() == null ? 0 : req.getBizType());
        entity.setScope(req.getScope());
        entity.setScopeValue(req.getScopeValue());
        entity.setTriggerField(req.getTriggerField());
        entity.setOperator(StrUtil.blankToDefault(req.getOperator(), ">="));
        entity.setThresholdValue(req.getThresholdValue());
        entity.setTimeWindow(req.getTimeWindow() == null ? 0 : req.getTimeWindow());
        entity.setAction(req.getAction());
        entity.setPriority(req.getPriority() == null ? 100 : req.getPriority());
        entity.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        entity.setEffectiveTime(req.getEffectiveTime());
        entity.setExpireTime(req.getExpireTime());
        entity.setRemark(req.getRemark());
        entity.setCreatorId(operatorId);
        entity.setUpdaterId(operatorId);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        ruleMapper.insert(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(RiskRuleUpdateRequest req, String operatorId) {
        LambdaUpdateWrapper<PayRiskRuleEntity> uw = new LambdaUpdateWrapper<PayRiskRuleEntity>()
                .eq(PayRiskRuleEntity::getRuleNo, req.getRuleNo())
                .set(PayRiskRuleEntity::getUpdaterId, operatorId)
                .set(PayRiskRuleEntity::getUpdateTime, new Date());
        if (StrUtil.isNotBlank(req.getRuleName()))    uw.set(PayRiskRuleEntity::getRuleName, req.getRuleName());
        if (req.getRuleType() != null)                uw.set(PayRiskRuleEntity::getRuleType, req.getRuleType());
        if (req.getBizType() != null)                 uw.set(PayRiskRuleEntity::getBizType, req.getBizType());
        if (req.getScope() != null)                   uw.set(PayRiskRuleEntity::getScope, req.getScope());
        if (req.getScopeValue() != null)              uw.set(PayRiskRuleEntity::getScopeValue, req.getScopeValue());
        if (StrUtil.isNotBlank(req.getTriggerField())) uw.set(PayRiskRuleEntity::getTriggerField, req.getTriggerField());
        if (StrUtil.isNotBlank(req.getOperator()))    uw.set(PayRiskRuleEntity::getOperator, req.getOperator());
        if (req.getThresholdValue() != null)          uw.set(PayRiskRuleEntity::getThresholdValue, req.getThresholdValue());
        if (req.getTimeWindow() != null)              uw.set(PayRiskRuleEntity::getTimeWindow, req.getTimeWindow());
        if (req.getAction() != null)                  uw.set(PayRiskRuleEntity::getAction, req.getAction());
        if (req.getPriority() != null)                uw.set(PayRiskRuleEntity::getPriority, req.getPriority());
        if (req.getStatus() != null)                  uw.set(PayRiskRuleEntity::getStatus, req.getStatus());
        if (req.getEffectiveTime() != null)           uw.set(PayRiskRuleEntity::getEffectiveTime, req.getEffectiveTime());
        if (req.getExpireTime() != null)              uw.set(PayRiskRuleEntity::getExpireTime, req.getExpireTime());
        if (req.getRemark() != null)                  uw.set(PayRiskRuleEntity::getRemark, req.getRemark());
        int rows = ruleMapper.update(null, uw);
        if (rows == 0) throw PaymentException.bizError("规则不存在或已被删除");
        return true;
    }

    @Override
    public boolean changeStatus(String ruleNo, Integer status, String operatorId) {
        if (status == null || (status != 0 && status != 1)) {
            throw PaymentException.bizError("状态值非法，0-禁用 1-启用");
        }
        int rows = ruleMapper.update(null, new LambdaUpdateWrapper<PayRiskRuleEntity>()
                .eq(PayRiskRuleEntity::getRuleNo, ruleNo)
                .set(PayRiskRuleEntity::getStatus, status)
                .set(PayRiskRuleEntity::getUpdaterId, operatorId)
                .set(PayRiskRuleEntity::getUpdateTime, new Date()));
        return rows > 0;
    }

    @Override
    public boolean delete(String ruleNo, String operatorId) {
        PayRiskRuleEntity e = ruleMapper.selectOne(new LambdaQueryWrapper<PayRiskRuleEntity>()
                .eq(PayRiskRuleEntity::getRuleNo, ruleNo));
        if (e == null) throw PaymentException.bizError("规则不存在");
        return ruleMapper.deleteById(e.getId()) > 0;
    }

    @Override
    public IPage<PayRiskRuleEntity> list(RiskRuleListRequest req) {
        Page<PayRiskRuleEntity> page = new Page<>(
                req.getPageNum() == null ? 1 : req.getPageNum(),
                req.getPageSize() == null ? 20 : req.getPageSize());
        LambdaQueryWrapper<PayRiskRuleEntity> qw = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(req.getRuleNo()))    qw.like(PayRiskRuleEntity::getRuleNo, req.getRuleNo());
        if (StrUtil.isNotBlank(req.getRuleName()))  qw.like(PayRiskRuleEntity::getRuleName, req.getRuleName());
        if (req.getRuleType() != null)              qw.eq(PayRiskRuleEntity::getRuleType, req.getRuleType());
        if (req.getBizType() != null)               qw.eq(PayRiskRuleEntity::getBizType, req.getBizType());
        if (req.getScope() != null)                 qw.eq(PayRiskRuleEntity::getScope, req.getScope());
        if (StrUtil.isNotBlank(req.getScopeValue())) qw.eq(PayRiskRuleEntity::getScopeValue, req.getScopeValue());
        if (req.getStatus() != null)                qw.eq(PayRiskRuleEntity::getStatus, req.getStatus());
        qw.orderByAsc(PayRiskRuleEntity::getPriority).orderByDesc(PayRiskRuleEntity::getId);
        return ruleMapper.selectPage(page, qw);
    }

    @Override
    public PayRiskRuleEntity detail(String ruleNo) {
        return ruleMapper.selectOne(new LambdaQueryWrapper<PayRiskRuleEntity>()
                .eq(PayRiskRuleEntity::getRuleNo, ruleNo));
    }
}
