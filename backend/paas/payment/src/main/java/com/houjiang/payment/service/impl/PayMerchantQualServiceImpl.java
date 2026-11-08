package com.houjiang.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.payment.dto.merchantQual.MerchantQualQueryRequest;
import com.houjiang.payment.dto.merchantQual.MerchantQualSaveRequest;
import com.houjiang.payment.dto.merchantQual.MerchantQualVO;
import com.houjiang.payment.entity.PayMerchantQualCertEntity;
import com.houjiang.payment.entity.PayMerchantQualEntity;
import com.houjiang.payment.mapper.PayMerchantQualCertMapper;
import com.houjiang.payment.mapper.PayMerchantQualMapper;
import com.houjiang.payment.service.PayMerchantQualService;
import com.houjiang.payment.util.AesPayUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商户资质 Service 实现
 *
 * @author Amos
 * @since 2026-05-13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayMerchantQualServiceImpl implements PayMerchantQualService {

    private static final String SENSITIVE_MASK = "******";

    private final PayMerchantQualMapper qualMapper;
    private final PayMerchantQualCertMapper certMapper;

    @Override
    public IPage<MerchantQualVO> page(MerchantQualQueryRequest req) {
        LambdaQueryWrapper<PayMerchantQualEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(req.getTerminal())) {
            wrapper.eq(PayMerchantQualEntity::getTerminal, req.getTerminal());
        }
        if (StringUtils.hasText(req.getChannel())) {
            wrapper.eq(PayMerchantQualEntity::getChannel, req.getChannel());
        }
        if (StringUtils.hasText(req.getEnv())) {
            wrapper.eq(PayMerchantQualEntity::getEnv, req.getEnv());
        }
        if (req.getStatus() != null) {
            wrapper.eq(PayMerchantQualEntity::getStatus, req.getStatus());
        }
        if (StringUtils.hasText(req.getQualName())) {
            wrapper.like(PayMerchantQualEntity::getQualName, req.getQualName());
        }
        wrapper.orderByDesc(PayMerchantQualEntity::getUpdateTime);

        Page<PayMerchantQualEntity> page = new Page<>(
                req.getPageNum() == null ? 1 : req.getPageNum(),
                req.getPageSize() == null ? 10 : req.getPageSize());
        IPage<PayMerchantQualEntity> result = qualMapper.selectPage(page, wrapper);

        List<MerchantQualVO> records = result.getRecords().stream()
                .map(e -> toVO(e, false))
                .collect(Collectors.toList());

        Page<MerchantQualVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public MerchantQualVO detail(Long id, boolean plainSensitive) {
        PayMerchantQualEntity e = qualMapper.selectById(id);
        if (e == null) {
            return null;
        }
        MerchantQualVO vo = toVO(e, plainSensitive);
        // 关联证书
        List<PayMerchantQualCertEntity> certs = certMapper.selectList(
                new LambdaQueryWrapper<PayMerchantQualCertEntity>()
                        .eq(PayMerchantQualCertEntity::getQualId, id)
                        .orderByAsc(PayMerchantQualCertEntity::getId));
        if (certs != null && !certs.isEmpty()) {
            vo.setCerts(certs.stream().map(c -> {
                MerchantQualSaveRequest.CertItem item = new MerchantQualSaveRequest.CertItem();
                item.setId(c.getId());
                item.setCertType(c.getCertType());
                item.setCertPath(c.getCertPath());
                item.setCertSerialNo(c.getCertSerialNo());
                return item;
            }).collect(Collectors.toList()));
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(MerchantQualSaveRequest req) {
        // 必填校验
        if (!StringUtils.hasText(req.getTerminal()) || !StringUtils.hasText(req.getChannel())
                || !StringUtils.hasText(req.getQualName()) || !StringUtils.hasText(req.getEnv())) {
            throw new IllegalArgumentException("terminal/channel/env/qualName 必填");
        }
        Date now = new Date();
        PayMerchantQualEntity entity = new PayMerchantQualEntity();
        BeanUtils.copyProperties(req, entity, "certs", "appSecret", "apiKey");
        // 敏感字段 AES 加密
        if (StringUtils.hasText(req.getAppSecret()) && !SENSITIVE_MASK.equals(req.getAppSecret())) {
            entity.setAppSecret(AesPayUtil.encrypt(req.getAppSecret()));
        }
        if (StringUtils.hasText(req.getApiKey()) && !SENSITIVE_MASK.equals(req.getApiKey())) {
            entity.setApiKey(AesPayUtil.encrypt(req.getApiKey()));
        }
        entity.setUpdateTime(now);
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        if (entity.getTenantId() == null) {
            entity.setTenantId(0L);
        }

        if (req.getId() == null) {
            entity.setCreateTime(now);
            qualMapper.insert(entity);
        } else {
            // 编辑保留旧的密文（前端回显 ******，未修改时 entity.appSecret 为 null）
            if (!StringUtils.hasText(entity.getAppSecret())) {
                entity.setAppSecret(null);
            }
            if (!StringUtils.hasText(entity.getApiKey())) {
                entity.setApiKey(null);
            }
            qualMapper.updateById(entity);
        }

        // 证书全量替换
        if (req.getCerts() != null) {
            certMapper.delete(new LambdaQueryWrapper<PayMerchantQualCertEntity>()
                    .eq(PayMerchantQualCertEntity::getQualId, entity.getId()));
            List<PayMerchantQualCertEntity> certs = new ArrayList<>();
            for (MerchantQualSaveRequest.CertItem item : req.getCerts()) {
                if (!StringUtils.hasText(item.getCertType()) || !StringUtils.hasText(item.getCertPath())) {
                    continue;
                }
                PayMerchantQualCertEntity c = new PayMerchantQualCertEntity();
                c.setQualId(entity.getId());
                c.setCertType(item.getCertType());
                c.setCertPath(item.getCertPath());
                c.setCertSerialNo(item.getCertSerialNo());
                c.setCreateTime(now);
                certs.add(c);
            }
            for (PayMerchantQualCertEntity c : certs) {
                certMapper.insert(c);
            }
        }
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long importDisabled(MerchantQualSaveRequest req, Long tenantId, String creatorId) {
        if (req == null || tenantId == null || tenantId <= 0 || !StringUtils.hasText(creatorId)) {
            throw new IllegalArgumentException("组织和创建人不能为空");
        }
        if (!StringUtils.hasText(req.getTerminal()) || !StringUtils.hasText(req.getChannel())
                || !StringUtils.hasText(req.getQualName()) || !StringUtils.hasText(req.getEnv())) {
            throw new IllegalArgumentException("terminal/channel/env/qualName 必填");
        }
        Date now = new Date();
        PayMerchantQualEntity entity = new PayMerchantQualEntity();
        BeanUtils.copyProperties(req, entity, "id", "certs", "appSecret", "apiKey",
                "apiPublicKey", "extConfig", "status");
        entity.setTenantId(tenantId);
        entity.setStatus(0);
        entity.setCreateBy(creatorId);
        entity.setUpdateBy(creatorId);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        qualMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public boolean toggleStatus(Long id, Integer status) {
        if (id == null || status == null) {
            return false;
        }
        PayMerchantQualEntity e = new PayMerchantQualEntity();
        e.setId(id);
        e.setStatus(status);
        e.setUpdateTime(new Date());
        return qualMapper.updateById(e) > 0;
    }

    @Override
    public boolean delete(Long id) {
        return qualMapper.deleteById(id) > 0;
    }

    @Override
    public PayMerchantQualEntity getByTerminalChannel(String terminal, String channel, String env) {
        if (!StringUtils.hasText(terminal) || !StringUtils.hasText(channel)) {
            return null;
        }
        String envFinal = StringUtils.hasText(env) ? env : "P";
        PayMerchantQualEntity entity = qualMapper.selectOne(new LambdaQueryWrapper<PayMerchantQualEntity>()
                .eq(PayMerchantQualEntity::getTerminal, terminal)
                .eq(PayMerchantQualEntity::getChannel, channel)
                .eq(PayMerchantQualEntity::getEnv, envFinal)
                .eq(PayMerchantQualEntity::getStatus, 1)
                .last("LIMIT 1"));
        if (entity == null) {
            return null;
        }
        // 解密敏感字段，返回内存对象（不回写库）
        entity.setAppSecret(AesPayUtil.decrypt(entity.getAppSecret()));
        entity.setApiKey(AesPayUtil.decrypt(entity.getApiKey()));
        return entity;
    }

    /**
     * Entity → VO，敏感字段按需脱敏/解密
     */
    private MerchantQualVO toVO(PayMerchantQualEntity e, boolean plainSensitive) {
        MerchantQualVO vo = new MerchantQualVO();
        BeanUtils.copyProperties(e, vo);
        if (plainSensitive) {
            vo.setAppSecret(AesPayUtil.decrypt(e.getAppSecret()));
            vo.setApiKey(AesPayUtil.decrypt(e.getApiKey()));
        } else {
            vo.setAppSecret(StringUtils.hasText(e.getAppSecret()) ? SENSITIVE_MASK : null);
            vo.setApiKey(StringUtils.hasText(e.getApiKey()) ? SENSITIVE_MASK : null);
        }
        return vo;
    }
}
