package com.houjiang.payment.service.api.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.payment.dto.channel.RouteListRequest;
import com.houjiang.payment.dto.channel.RouteSaveRequest;
import com.houjiang.payment.entity.PayChannelRouteEntity;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.mapper.PayChannelRouteMapper;
import com.houjiang.payment.service.api.ChannelRouteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 路由规则 CRUD 服务实现
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelRouteServiceImpl implements ChannelRouteService {

    private final PayChannelRouteMapper routeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(RouteSaveRequest req, String operatorId) {
        log.info("[route/save] id={}, routeNo={}, bizType={}, strategy={}", req.getId(), req.getRouteNo(), req.getBizType(), req.getStrategy());
        if (req.getId() == null) {
            Long cnt = routeMapper.selectCount(new LambdaQueryWrapper<PayChannelRouteEntity>()
                    .eq(PayChannelRouteEntity::getRouteNo, req.getRouteNo()));
            if (cnt != null && cnt > 0) {
                throw PaymentException.bizError("规则编号已存在: " + req.getRouteNo());
            }
            PayChannelRouteEntity e = new PayChannelRouteEntity();
            e.setRouteNo(req.getRouteNo());
            e.setRouteName(req.getRouteName());
            e.setBizType(req.getBizType());
            e.setPayMethod(req.getPayMethod());
            e.setTerminal(req.getTerminal());
            e.setMerchantNo(req.getMerchantNo());
            e.setAmountMin(req.getAmountMin());
            e.setAmountMax(req.getAmountMax());
            e.setChannelCodes(req.getChannelCodes());
            e.setStrategy(StrUtil.blankToDefault(req.getStrategy(), "WEIGHT"));
            e.setPriority(req.getPriority() == null ? 100 : req.getPriority());
            e.setStatus(req.getStatus() == null ? 1 : req.getStatus());
            e.setEffectiveTime(req.getEffectiveTime());
            e.setExpireTime(req.getExpireTime());
            e.setRemark(req.getRemark());
            e.setCreatorId(operatorId);
            e.setUpdaterId(operatorId);
            e.setCreateTime(new Date());
            e.setUpdateTime(new Date());
            routeMapper.insert(e);
            return e.getId();
        }
        LambdaUpdateWrapper<PayChannelRouteEntity> uw = new LambdaUpdateWrapper<PayChannelRouteEntity>()
                .eq(PayChannelRouteEntity::getId, req.getId())
                .set(PayChannelRouteEntity::getUpdaterId, operatorId)
                .set(PayChannelRouteEntity::getUpdateTime, new Date());
        if (StrUtil.isNotBlank(req.getRouteName()))   uw.set(PayChannelRouteEntity::getRouteName, req.getRouteName());
        if (req.getBizType() != null)                 uw.set(PayChannelRouteEntity::getBizType, req.getBizType());
        if (req.getPayMethod() != null)               uw.set(PayChannelRouteEntity::getPayMethod, req.getPayMethod());
        if (req.getTerminal() != null)                uw.set(PayChannelRouteEntity::getTerminal, req.getTerminal());
        if (req.getMerchantNo() != null)              uw.set(PayChannelRouteEntity::getMerchantNo, req.getMerchantNo());
        if (req.getAmountMin() != null)               uw.set(PayChannelRouteEntity::getAmountMin, req.getAmountMin());
        if (req.getAmountMax() != null)               uw.set(PayChannelRouteEntity::getAmountMax, req.getAmountMax());
        if (StrUtil.isNotBlank(req.getChannelCodes())) uw.set(PayChannelRouteEntity::getChannelCodes, req.getChannelCodes());
        if (StrUtil.isNotBlank(req.getStrategy()))    uw.set(PayChannelRouteEntity::getStrategy, req.getStrategy());
        if (req.getPriority() != null)                uw.set(PayChannelRouteEntity::getPriority, req.getPriority());
        if (req.getStatus() != null)                  uw.set(PayChannelRouteEntity::getStatus, req.getStatus());
        if (req.getEffectiveTime() != null)           uw.set(PayChannelRouteEntity::getEffectiveTime, req.getEffectiveTime());
        if (req.getExpireTime() != null)              uw.set(PayChannelRouteEntity::getExpireTime, req.getExpireTime());
        if (req.getRemark() != null)                  uw.set(PayChannelRouteEntity::getRemark, req.getRemark());
        int rows = routeMapper.update(null, uw);
        if (rows == 0) throw PaymentException.bizError("规则不存在或已被删除");
        return req.getId();
    }

    @Override
    public boolean changeStatus(Long id, Integer status, String operatorId) {
        if (status == null || (status != 0 && status != 1)) {
            throw PaymentException.bizError("状态值非法，0-停用 1-启用");
        }
        int rows = routeMapper.update(null, new LambdaUpdateWrapper<PayChannelRouteEntity>()
                .eq(PayChannelRouteEntity::getId, id)
                .set(PayChannelRouteEntity::getStatus, status)
                .set(PayChannelRouteEntity::getUpdaterId, operatorId)
                .set(PayChannelRouteEntity::getUpdateTime, new Date()));
        return rows > 0;
    }

    @Override
    public boolean delete(Long id) {
        return routeMapper.deleteById(id) > 0;
    }

    @Override
    public IPage<PayChannelRouteEntity> page(RouteListRequest req) {
        Page<PayChannelRouteEntity> page = new Page<>(
                req.getPageNo() == null ? 1 : req.getPageNo(),
                req.getPageSize() == null ? 10 : req.getPageSize());
        LambdaQueryWrapper<PayChannelRouteEntity> qw = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(req.getRouteNo()))     qw.like(PayChannelRouteEntity::getRouteNo, req.getRouteNo());
        if (req.getBizType() != null)                 qw.eq(PayChannelRouteEntity::getBizType, req.getBizType());
        if (StrUtil.isNotBlank(req.getPayMethod()))   qw.eq(PayChannelRouteEntity::getPayMethod, req.getPayMethod());
        if (StrUtil.isNotBlank(req.getTerminal()))    qw.eq(PayChannelRouteEntity::getTerminal, req.getTerminal());
        if (req.getStatus() != null)                  qw.eq(PayChannelRouteEntity::getStatus, req.getStatus());
        qw.orderByAsc(PayChannelRouteEntity::getPriority).orderByDesc(PayChannelRouteEntity::getId);
        return routeMapper.selectPage(page, qw);
    }

    @Override
    public PayChannelRouteEntity detail(Long id) {
        return routeMapper.selectById(id);
    }
}
