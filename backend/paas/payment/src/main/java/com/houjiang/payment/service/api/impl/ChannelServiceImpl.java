package com.houjiang.payment.service.api.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.util.PayCredentialCryptoUtil;
import com.houjiang.payment.dto.channel.ChannelListRequest;
import com.houjiang.payment.dto.channel.ChannelSaveRequest;
import com.houjiang.payment.entity.PayChannelEntity;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.mapper.PayChannelMapper;
import com.houjiang.payment.service.api.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 渠道配置 CRUD 服务实现
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelServiceImpl implements ChannelService {

    private final PayChannelMapper channelMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(ChannelSaveRequest req, String operatorId) {
        log.info("[channel/save] id={}, channelCode={}, provider={}", req.getId(), req.getChannelCode(), req.getProvider());
        if (req.getId() == null) {
            // 新增
            Long cnt = channelMapper.selectCount(new LambdaQueryWrapper<PayChannelEntity>()
                    .eq(PayChannelEntity::getChannelCode, req.getChannelCode()));
            if (cnt != null && cnt > 0) {
                throw PaymentException.bizError("渠道编码已存在: " + req.getChannelCode());
            }
            PayChannelEntity e = new PayChannelEntity();
            e.setChannelCode(req.getChannelCode());
            e.setChannelName(req.getChannelName());
            e.setProvider(req.getProvider());
            e.setPayMethod(req.getPayMethod());
            e.setPriority(req.getPriority() == null ? 100 : req.getPriority());
            e.setWeight(req.getWeight() == null ? 1 : req.getWeight());
            e.setDailyLimit(req.getDailyLimit());
            e.setSingleLimit(req.getSingleLimit());
            e.setSingleMin(req.getSingleMin());
            e.setFeeRate(req.getFeeRate());
            e.setFallbackCodes(req.getFallbackCodes());
            e.setHealthScore(100);
            e.setTotalCount(0L);
            e.setSuccessCount(0L);
            e.setCircuitStatus(0);
            e.setStatus(req.getStatus() == null ? 1 : req.getStatus());
            e.setMerchantNo(req.getMerchantNo());
            e.setConfigJson(req.getConfigJson());
            e.setRemark(req.getRemark());
            e.setCreatorId(operatorId);
            e.setUpdaterId(operatorId);
            e.setCreateTime(new Date());
            e.setUpdateTime(new Date());
            channelMapper.insert(e);
            return e.getId();
        }
        // 更新
        LambdaUpdateWrapper<PayChannelEntity> uw = new LambdaUpdateWrapper<PayChannelEntity>()
                .eq(PayChannelEntity::getId, req.getId())
                .set(PayChannelEntity::getUpdaterId, operatorId)
                .set(PayChannelEntity::getUpdateTime, new Date());
        if (StrUtil.isNotBlank(req.getChannelName())) uw.set(PayChannelEntity::getChannelName, req.getChannelName());
        if (StrUtil.isNotBlank(req.getProvider()))    uw.set(PayChannelEntity::getProvider, req.getProvider());
        if (StrUtil.isNotBlank(req.getPayMethod()))   uw.set(PayChannelEntity::getPayMethod, req.getPayMethod());
        if (req.getPriority() != null)                uw.set(PayChannelEntity::getPriority, req.getPriority());
        if (req.getWeight() != null)                  uw.set(PayChannelEntity::getWeight, req.getWeight());
        if (req.getDailyLimit() != null)              uw.set(PayChannelEntity::getDailyLimit, req.getDailyLimit());
        if (req.getSingleLimit() != null)             uw.set(PayChannelEntity::getSingleLimit, req.getSingleLimit());
        if (req.getSingleMin() != null)               uw.set(PayChannelEntity::getSingleMin, req.getSingleMin());
        if (req.getFeeRate() != null)                 uw.set(PayChannelEntity::getFeeRate, req.getFeeRate());
        if (req.getFallbackCodes() != null)           uw.set(PayChannelEntity::getFallbackCodes, req.getFallbackCodes());
        if (req.getStatus() != null)                  uw.set(PayChannelEntity::getStatus, req.getStatus());
        if (req.getMerchantNo() != null)              uw.set(PayChannelEntity::getMerchantNo, req.getMerchantNo());
        if (req.getConfigJson() != null)              uw.set(PayChannelEntity::getConfigJson, req.getConfigJson());
        if (req.getRemark() != null)                  uw.set(PayChannelEntity::getRemark, req.getRemark());
        int rows = channelMapper.update(null, uw);
        if (rows == 0) throw PaymentException.bizError("渠道不存在或已被删除");
        return req.getId();
    }

    @Override
    public boolean changeStatus(Long id, Integer status, String operatorId) {
        if (status == null || (status != 0 && status != 1)) {
            throw PaymentException.bizError("状态值非法，0-停用 1-启用");
        }
        int rows = channelMapper.update(null, new LambdaUpdateWrapper<PayChannelEntity>()
                .eq(PayChannelEntity::getId, id)
                .set(PayChannelEntity::getStatus, status)
                .set(PayChannelEntity::getUpdaterId, operatorId)
                .set(PayChannelEntity::getUpdateTime, new Date()));
        return rows > 0;
    }

    @Override
    public boolean delete(Long id) {
        return channelMapper.deleteById(id) > 0;
    }

    @Override
    public IPage<PayChannelEntity> page(ChannelListRequest req) {
        Page<PayChannelEntity> page = new Page<>(
                req.getPageNo() == null ? 1 : req.getPageNo(),
                req.getPageSize() == null ? 10 : req.getPageSize());
        LambdaQueryWrapper<PayChannelEntity> qw = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(req.getChannelCode())) qw.like(PayChannelEntity::getChannelCode, req.getChannelCode());
        if (StrUtil.isNotBlank(req.getProvider()))    qw.eq(PayChannelEntity::getProvider, req.getProvider());
        if (StrUtil.isNotBlank(req.getPayMethod()))   qw.eq(PayChannelEntity::getPayMethod, req.getPayMethod());
        if (req.getStatus() != null)                  qw.eq(PayChannelEntity::getStatus, req.getStatus());
        if (StrUtil.isNotBlank(req.getMerchantNo()))  qw.eq(PayChannelEntity::getMerchantNo, req.getMerchantNo());
        qw.orderByAsc(PayChannelEntity::getPriority).orderByDesc(PayChannelEntity::getId);
        return channelMapper.selectPage(page, qw);
    }

    @Override
    public PayChannelEntity detail(Long id) {
        return channelMapper.selectById(id);
    }

    @Override
    public PayChannelEntity getByCode(String channelCode) {
        return channelMapper.selectOne(new LambdaQueryWrapper<PayChannelEntity>()
                .eq(PayChannelEntity::getChannelCode, channelCode));
    }

    /**
     * 保存凭证：configMap 的敏感字段 AES 加密后作为 configJson 落库
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveCredential(ChannelSaveRequest req, Map<String, Object> configMap, String operatorId) {
        log.info("[channel/saveCredential] id={}, channelCode={}, fields={}",
                req.getId(), req.getChannelCode(),
                configMap == null ? 0 : configMap.size());
        // 加密敏感字段
        Map<String, Object> mergedConfig = configMap == null
                ? new HashMap<>() : new HashMap<>(configMap);
        if (req.getId() != null) {
            PayChannelEntity existing = channelMapper.selectById(req.getId());
            if (existing == null) throw PaymentException.bizError("Channel does not exist");
            Map<String, Object> stored = PayCredentialCryptoUtil.decryptConfigJson(existing.getConfigJson());
            stored.forEach((key, value) -> {
                Object incoming = mergedConfig.get(key);
                if (!mergedConfig.containsKey(key)
                        || (PayCredentialCryptoUtil.isSensitive(key)
                        && (incoming == null || StrUtil.isBlank(String.valueOf(incoming))
                        || "***".equals(incoming)))) {
                    mergedConfig.put(key, value);
                }
            });
        }
        String encryptedJson = PayCredentialCryptoUtil.encryptConfigJson(mergedConfig);
        req.setConfigJson(encryptedJson);
        return save(req, operatorId);
    }

    /**
     * 返回解密后的 configMap，仅供管理员编辑使用
     */
    @Override
    public Map<String, Object> getCredentialDecrypted(Long id) {
        PayChannelEntity e = channelMapper.selectById(id);
        if (e == null) throw PaymentException.bizError("渠道不存在");
        Map<String, Object> result = new HashMap<>();
        result.put("id", e.getId());
        result.put("channelCode", e.getChannelCode());
        result.put("channelName", e.getChannelName());
        result.put("provider", e.getProvider());
        result.put("status", e.getStatus());
        result.put("merchantNo", e.getMerchantNo());
        result.put("remark", e.getRemark());
        Map<String, Object> config = PayCredentialCryptoUtil.decryptConfigJson(e.getConfigJson());
        config.replaceAll((key, value) -> PayCredentialCryptoUtil.isSensitive(key) ? "***" : value);
        result.put("configMap", config);
        return result;
    }

    /**
     * 分页列表，configJson 脱敏（敏感字段 → ***）
     */
    @Override
    public IPage<PayChannelEntity> pageDesensitized(ChannelListRequest req) {
        IPage<PayChannelEntity> p = page(req);
        List<PayChannelEntity> records = p.getRecords();
        if (records != null) {
            for (PayChannelEntity e : records) {
                if (e.getConfigJson() != null) {
                    e.setConfigJson(PayCredentialCryptoUtil.desensitizeConfigJson(e.getConfigJson()));
                }
            }
        }
        return p;
    }

    /**
     * 测试连通：检查凭证是否完备，以及返回可识别的状态供前端展示
     * 详细的 SDK 调用下放 Adapter，本处仅验证必填字段是否齐备
     */
    @Override
    public Map<String, Object> testConnect(Long id) {
        PayChannelEntity e = channelMapper.selectById(id);
        if (e == null) throw PaymentException.bizError("渠道不存在");
        Map<String, Object> cfg = PayCredentialCryptoUtil.decryptConfigJson(e.getConfigJson());
        Map<String, Object> result = new HashMap<>();
        result.put("channelCode", e.getChannelCode());
        result.put("channelName", e.getChannelName());
        // 必填字段按渠道区分
        String code = e.getChannelCode() == null ? "" : e.getChannelCode();
        String[] required;
        switch (code) {
            case "WECHAT_V3":
                required = new String[]{"appId", "mchId", "apiV3Key", "certSerialNo", "privateKey"};
                break;
            case "ALIPAY":
            case "ALI_CREDIT":
                required = new String[]{"appId", "privateKey", "alipayPublicKey"};
                break;
            case "YSF_PAY":
                required = new String[]{"merchId", "signKey"};
                break;
            case "FME_PAY":
                required = new String[]{"partnerId", "appKey", "appSecret"};
                break;
            default:
                required = new String[0];
        }
        for (String key : required) {
            Object v = cfg.get(key);
            if (v == null || String.valueOf(v).isEmpty()) {
                result.put("success", false);
                result.put("errorCode", "CHANNEL_NOT_CONFIGURED");
                result.put("message", "渠道凭证不完整，缺少必填项: " + key);
                return result;
            }
        }
        // 凭证齐备，本阶段未实际调用上游 SDK（需商户号/证书才能走通）
        result.put("success", true);
        result.put("message", "凭证完整，待商户号验证后的上游 SDK 实调");
        return result;
    }

    /** 公开字段白名单：仅这些字段会下发给 C 端 */
    private static final Set<String> PUBLIC_CONFIG_FIELDS = new HashSet<>(java.util.Arrays.asList(
            "appId", "mchId", "merchId", "partnerId", "appKey",
            "certSerialNo", "sandbox", "notifyUrl", "jumpUrl",
            "icon", "displayName", "sort", "sceneType"
    ));

    /**
     * 公开版渠道配置：剔除敏感字段，仅返回启用渠道
     */
    @Override
    public List<Map<String, Object>> listEnabledPublic(String payMethod) {
        LambdaQueryWrapper<PayChannelEntity> qw = new LambdaQueryWrapper<PayChannelEntity>()
                .eq(PayChannelEntity::getStatus, 1);
        if (StrUtil.isNotBlank(payMethod)) {
            qw.eq(PayChannelEntity::getPayMethod, payMethod);
        }
        qw.orderByAsc(PayChannelEntity::getPriority).orderByDesc(PayChannelEntity::getId);
        List<PayChannelEntity> records = channelMapper.selectList(qw);
        if (records == null || records.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return records.stream().map(e -> {
            Map<String, Object> item = new HashMap<>();
            item.put("channelCode", e.getChannelCode());
            item.put("channelName", e.getChannelName());
            item.put("provider", e.getProvider());
            item.put("payMethod", e.getPayMethod());
            item.put("merchantNo", e.getMerchantNo());
            item.put("priority", e.getPriority());
            item.put("weight", e.getWeight());
            item.put("singleMin", e.getSingleMin());
            item.put("singleLimit", e.getSingleLimit());
            item.put("feeRate", e.getFeeRate());
            // 仅暴露公开字段，剔除敏感字段
            Map<String, Object> publicCfg = new HashMap<>();
            try {
                Map<String, Object> raw = PayCredentialCryptoUtil.decryptConfigJson(e.getConfigJson());
                if (raw != null) {
                    for (Map.Entry<String, Object> en : raw.entrySet()) {
                        if (PayCredentialCryptoUtil.isSensitive(en.getKey())) {
                            continue;
                        }
                        if (PUBLIC_CONFIG_FIELDS.contains(en.getKey())) {
                            publicCfg.put(en.getKey(), en.getValue());
                        }
                    }
                }
            } catch (Exception ex) {
                log.warn("[channel/listEnabledPublic] parse configJson failed, channelCode={}", e.getChannelCode(), ex);
            }
            item.put("config", publicCfg);
            return item;
        }).collect(Collectors.toList());
    }
}
