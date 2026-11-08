package com.houjiang.payment.service.api.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.payment.dto.risk.BlacklistAddRequest;
import com.houjiang.payment.dto.risk.BlacklistListRequest;
import com.houjiang.payment.entity.PayRiskBlacklistEntity;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.mapper.PayRiskBlacklistMapper;
import com.houjiang.payment.service.api.RiskBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 风控黑白名单服务实现
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskBlacklistServiceImpl implements RiskBlacklistService {

    private final PayRiskBlacklistMapper blacklistMapper;

    @Override
    public PayRiskBlacklistEntity add(BlacklistAddRequest req, String operatorId) {
        log.info("[risk/blacklist/add] listType={}, targetType={}, targetValue={}",
                req.getListType(), req.getTargetType(), req.getTargetValue());
        PayRiskBlacklistEntity e = new PayRiskBlacklistEntity();
        e.setListType(req.getListType());
        e.setTargetType(req.getTargetType());
        e.setTargetValue(req.getTargetValue());
        e.setBizScope(req.getBizScope() == null ? 0 : req.getBizScope());
        e.setReason(req.getReason());
        e.setRiskLevel(req.getRiskLevel() == null ? 1 : req.getRiskLevel());
        e.setEffectiveTime(req.getEffectiveTime());
        e.setExpireTime(req.getExpireTime());
        e.setStatus(1);
        e.setOperatorId(operatorId);
        e.setCreateTime(new Date());
        e.setUpdateTime(new Date());
        blacklistMapper.insert(e);
        return e;
    }

    @Override
    public boolean remove(Long id, String operatorId) {
        if (id == null) throw PaymentException.bizError("ID 不能为空");
        return blacklistMapper.deleteById(id) > 0;
    }

    @Override
    public IPage<PayRiskBlacklistEntity> list(BlacklistListRequest req) {
        Page<PayRiskBlacklistEntity> page = new Page<>(
                req.getPageNum() == null ? 1 : req.getPageNum(),
                req.getPageSize() == null ? 20 : req.getPageSize());
        LambdaQueryWrapper<PayRiskBlacklistEntity> qw = new LambdaQueryWrapper<>();
        if (req.getListType() != null)                   qw.eq(PayRiskBlacklistEntity::getListType, req.getListType());
        if (req.getTargetType() != null)                 qw.eq(PayRiskBlacklistEntity::getTargetType, req.getTargetType());
        if (StrUtil.isNotBlank(req.getTargetValue()))    qw.like(PayRiskBlacklistEntity::getTargetValue, req.getTargetValue());
        if (req.getBizScope() != null)                   qw.eq(PayRiskBlacklistEntity::getBizScope, req.getBizScope());
        if (req.getStatus() != null)                     qw.eq(PayRiskBlacklistEntity::getStatus, req.getStatus());
        qw.orderByDesc(PayRiskBlacklistEntity::getId);
        return blacklistMapper.selectPage(page, qw);
    }

    @Override
    public PayRiskBlacklistEntity matchTarget(Integer listType, Integer targetType, String targetValue, Integer bizScope) {
        if (listType == null || targetType == null || StrUtil.isBlank(targetValue)) return null;
        Date now = new Date();
        LambdaQueryWrapper<PayRiskBlacklistEntity> qw = new LambdaQueryWrapper<PayRiskBlacklistEntity>()
                .eq(PayRiskBlacklistEntity::getListType, listType)
                .eq(PayRiskBlacklistEntity::getTargetType, targetType)
                .eq(PayRiskBlacklistEntity::getTargetValue, targetValue)
                .eq(PayRiskBlacklistEntity::getStatus, 1)
                .and(w -> w.isNull(PayRiskBlacklistEntity::getEffectiveTime)
                           .or().le(PayRiskBlacklistEntity::getEffectiveTime, now))
                .and(w -> w.isNull(PayRiskBlacklistEntity::getExpireTime)
                           .or().ge(PayRiskBlacklistEntity::getExpireTime, now))
                .and(w -> w.eq(PayRiskBlacklistEntity::getBizScope, 0)
                           .or().eq(PayRiskBlacklistEntity::getBizScope, bizScope == null ? 0 : bizScope))
                .last("LIMIT 1");
        return blacklistMapper.selectOne(qw);
    }
}
