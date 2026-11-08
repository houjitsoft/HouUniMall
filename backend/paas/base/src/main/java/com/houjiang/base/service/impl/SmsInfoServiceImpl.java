package com.houjiang.base.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.dto.BaseSmsConfigDTO;
import com.houjiang.base.entity.SmsInfoEntity;
import com.houjiang.base.mapper.SmsInfoMapper;
import com.houjiang.base.service.SmsInfoService;
import com.houjiang.base.security.BaseOrganizationResolver;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseMapUtils;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
* @author Amos
*/
@Service
public class SmsInfoServiceImpl extends ServiceImpl<SmsInfoMapper, SmsInfoEntity>
implements SmsInfoService {

    private static final Logger LOG = LoggerFactory.getLogger(SmsInfoServiceImpl.class);

    @Autowired
    private SmsInfoMapper smsInfoMapper;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private BaseOrganizationResolver organizationResolver;

    /**
     * 短信配置全量列表的进程内 TTL 缓存。
     * 用于替代 BFF 层 base 服务 readTimeout=60000 的兜底——通过缓存让重复查询命中内存。
     * 短信配置变更频次极低（一般只在初始化或换服务商时修改），TTL 设为 5 分钟。
     * 写入路径（{@link #updateSmsConfig}）成功后调用 {@link #invalidateSmsListCache()} 立即清空。
     */
    private static final long SMS_LIST_CACHE_TTL_MS = 5 * 60 * 1000L;
    private static final ConcurrentHashMap<String, CacheEntry<String>> smsListCache = new ConcurrentHashMap<>();
    private static final String SMS_LIST_CACHE_KEY = "all";

    /**
     * 通用 TTL 缓存条目
     */
    private static final class CacheEntry<T> {
        final T value;
        final long expireAt;
        CacheEntry(T value, long ttlMs) {
            this.value = value;
            this.expireAt = System.currentTimeMillis() + ttlMs;
        }
        boolean isExpired() {
            return System.currentTimeMillis() >= expireAt;
        }
    }

    /**
     * 写入路径触发：清空短信全量列表缓存。
     * 粒度选择全清（key 单一 "all"），写后立即可见。
     */
    private void invalidateSmsListCache() {
        smsListCache.clear();
    }


    /**
     * 查询并返回加密后的短信配置信息。
     * 主要用于前端展示，将敏感信息加密处理。
     *
     * @return 短信配置信息列表，包含各种模板的详细信息。
     */
    @Override
    public ResponseResult selectSMSInfoEncryption() throws IllegalAccessException {
        String orgId = currentOrgId();
        if (orgId == null) return ResponseResult.fail(403, "当前组织未确定或无权访问短信配置");
        String cacheKey = cacheKey(orgId);
        // 1. 进程内缓存命中则直接返回（TTL 5 分钟，写后由 invalidateSmsListCache 清掉）
        CacheEntry<String> cached = smsListCache.get(cacheKey);
        if (cached != null) {
            if (!cached.isExpired()) {
                return ResponseResult.success(
                        CommonType.initializationStatus.SUCCESS.getCode(),
                        BizCodeEnume.SMS_SUCCESS.getMsg(),
                        cached.value);
            }
            smsListCache.remove(cacheKey);
        }

        List<Map<String, Object>> smsInfList = new ArrayList<>();
        // 创建查询包装器
        QueryWrapper<SmsInfoEntity> queryWrapper = new QueryWrapper<>();
        List<SmsInfoEntity> smsList = this.baseMapper.selectList(new QueryWrapper<SmsInfoEntity>()
                .and(w -> w.eq("org_id", orgId).or().apply("FIND_IN_SET({0}, org_id)", orgId)));
        for (SmsInfoEntity smsInfoEntity : smsList) {
            String encodedString = null;
            List<Map<String, Object>> list = new ArrayList<>();
            if(null != smsInfoEntity ){
                // 将查询到的SmsInfoEntity对象转换为Map，方便处理
                //接收返回信息
                Map<String, Object> result = ParseMapUtils.beanToMap(smsInfoEntity);
                // 初始化标志位，表示查询到配置信息
                //返回给前端的标识 0 初始化成功但无配置信息  1已经存在配置信息并返回  2初始化失败
                Integer flag = 1;
                result.put("flag", flag);
                // 注意：以下模板相关字段已从数据库中删除，相关代码已注释
                // 短信模板现在统一通过消息模板系统管理（hou_base_message_template表）
                // if(!smsInfoEntity.getSmsType().equals(CommonType.smsType.UNIMALL.getDesc())) {
                //     // 模板相关代码已移除，请使用消息模板系统
                // }

                // 将模板列表添加到结果中
                result.put("templateList", list);
                // 将短信配置信息存入Redis
                redisCache.setCacheObject(cacheKey(orgId) + "_" + smsInfoEntity.getSmsType(), JSON.toJSON(result).toString());
                smsInfList.add(result);
            }
        }
        // 对结果进行编码
        String encodedString = Base64.getEncoder().encodeToString(JSONObject.toJSONString(smsInfList).getBytes());

        // 2. 写入进程内缓存
        smsListCache.put(cacheKey, new CacheEntry<>(encodedString, SMS_LIST_CACHE_TTL_MS));

        // 返回包含编码后短信配置信息的成功响应
        //oss配置库存在配置信息并返回给前端做展示
        return ResponseResult.success(CommonType.initializationStatus.SUCCESS.getCode(), BizCodeEnume.SMS_SUCCESS.getMsg(), encodedString);
    }
    /**
     * 更新短信配置信息。
     *
     * @param baseSmsConfigReq 包含短信配置信息的请求对象。
     * @return 返回操作结果，成功或失败。
     */
    @Override
    public ResponseResult updateSmsConfig(BaseSmsConfigDTO baseSmsConfigReq) {
        String orgId = currentOrgId();
        if (orgId == null) return ResponseResult.fail(403, "当前组织未确定或无权修改短信配置");
        // 检查请求对象是否为空
        if (null != baseSmsConfigReq) {
            SmsInfoEntity smsInfoEntity = null;
            
            // 优先根据ID查询（如果提供了ID）
            if (baseSmsConfigReq.getId() != null) {
                smsInfoEntity = this.baseMapper.selectOne(new LambdaQueryWrapper<SmsInfoEntity>()
                        .eq(SmsInfoEntity::getId, baseSmsConfigReq.getId())
                        .and(w -> w.eq(SmsInfoEntity::getOrgId, orgId)
                                .or().apply("FIND_IN_SET({0}, org_id)", orgId)));
                LOG.info("=== 根据ID查询短信配置: ID={}, 结果: {}", baseSmsConfigReq.getId(), smsInfoEntity != null ? "找到" : "未找到");
            }
            
            // 如果根据ID没找到，且提供了smsType，则根据smsType查询
            if (smsInfoEntity == null && baseSmsConfigReq.getSmsType() != null) {
                smsInfoEntity = this.baseMapper.selectOne(new LambdaQueryWrapper<SmsInfoEntity>()
                        .eq(SmsInfoEntity::getSmsType, baseSmsConfigReq.getSmsType())
                        .and(w -> w.eq(SmsInfoEntity::getOrgId, orgId)
                                .or().apply("FIND_IN_SET({0}, org_id)", orgId))
                        // 只查询未删除的记录
                        .eq(SmsInfoEntity::getSmsIsactive, "01"));
                LOG.info("=== 根据smsType查询短信配置: type={}, 结果: {}", baseSmsConfigReq.getSmsType(), smsInfoEntity != null ? "找到" : "未找到");
            }

            // 当前没有短信配置信息时，创建新的配置项并初始化属性
            if (smsInfoEntity == null) {
                LOG.info("=== 插入新的短信配置: {}", baseSmsConfigReq.getSmsType());
                smsInfoEntity = new SmsInfoEntity();
                // 从请求对象复制属性到新的配置实体
                BeanUtils.copyProperties(baseSmsConfigReq, smsInfoEntity);
                smsInfoEntity.setOrgId(orgId);
                // 设置短信配置的状态为启用，并设置激活状态为正常
                smsInfoEntity.setSmsStatus(CommonType.smsEnableDisableStatus.STARTUSING.getCode().toString());
                smsInfoEntity.setSmsIsactive(CommonType.pseudoDeleteIdentification.NORMAL.getCode().toString());
                // 插入新的短信配置到数据库
                this.baseMapper.insert(smsInfoEntity);
                // 将新的短信配置信息存储到Redis（使用smsType作为key的一部分）
                if (smsInfoEntity.getSmsType() != null) {
                    redisCache.deleteObject(cacheKey(orgId) + "_" + smsInfoEntity.getSmsType());
                }
            } else {
                LOG.info("=== 更新现有的短信配置: {} (ID: {})", baseSmsConfigReq.getSmsType(), smsInfoEntity.getId());
                // 当前已有短信配置信息时，更新现有的配置项
                // 保留原来的ID
                Long originalId = smsInfoEntity.getId();
                BeanUtils.copyProperties(baseSmsConfigReq, smsInfoEntity);
                smsInfoEntity.setOrgId(orgId);
                // 确保ID不被覆盖
                smsInfoEntity.setId(originalId);
                // 根据ID更新短信配置信息
                this.baseMapper.updateById(smsInfoEntity);
                // 删除Redis中的短信配置信息，以触发下次从数据库重新加载
                if (smsInfoEntity.getSmsType() != null) {
                    redisCache.deleteObject(cacheKey(orgId) + "_" + smsInfoEntity.getSmsType());
                }
            }
            
            // 如果当前配置被设置为启用状态，则禁用其他所有配置
            if("01".equals(smsInfoEntity.getSmsStatus())){
                baseMapper.update(null, new LambdaUpdateWrapper<SmsInfoEntity>()
                        .set(SmsInfoEntity::getSmsStatus, "00")
                        .ne(SmsInfoEntity::getId, smsInfoEntity.getId())
                        .and(w -> w.eq(SmsInfoEntity::getOrgId, orgId)
                                .or().apply("FIND_IN_SET({0}, org_id)", orgId))
                        // 只更新未删除的记录
                        .eq(SmsInfoEntity::getSmsIsactive, "01"));
            }
            
            // 将模板ID保存到Redis（因为数据库表中没有smsGeneral字段）
            if (baseSmsConfigReq.getSmsGeneral() != null && !baseSmsConfigReq.getSmsGeneral().trim().isEmpty()) {
                if (smsInfoEntity.getSmsType() != null) {
                    String templateIdKey = cacheKey(orgId) + "_TEMPLATE_ID_" + smsInfoEntity.getSmsType();
                    redisCache.setCacheObject(templateIdKey, baseSmsConfigReq.getSmsGeneral().trim());
                    LOG.info("=== 模板ID已保存到Redis: key={}, value={}", templateIdKey, baseSmsConfigReq.getSmsGeneral().trim());
                }
            }
            
            // 写入路径触发：清空进程内全量列表缓存，保证下次查询读到最新配置
            invalidateSmsListCache();

            // 操作成功返回
            return ResponseResult.success();
        }
        // 请求对象为空，操作失败返回
        return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION);
    }
    /**
     * 查询开启的sms配置
     * @return 开启的sms配置
     */
    @Override
    public String querySmsStates() {
        String orgId = currentOrgId();
        if (orgId == null) return null;
        try {
            // 使用LambdaQueryWrapper构造查询条件，查询smsStatus为"01"的记录
            // 兼容 smsIsactive = "01" 或 "1" 的情况
            SmsInfoEntity entity = this.baseMapper.selectOne(new LambdaQueryWrapper<SmsInfoEntity>()
                    .eq(SmsInfoEntity::getSmsStatus, "01")
                    .and(w -> w.eq(SmsInfoEntity::getOrgId, orgId)
                            .or().apply("FIND_IN_SET({0}, org_id)", orgId))
                    .and(wrapper -> wrapper
                        .eq(SmsInfoEntity::getSmsIsactive, "01")
                        .or()
                        .eq(SmsInfoEntity::getSmsIsactive, "1")
                    )
                    .last("LIMIT 1"));
            
            // 检查查询结果是否为空
            if (entity == null) {
                LOG.warn("=== 未找到启用的短信配置（smsStatus=01 且 smsIsactive=01或1）");
                return null;
            }
            
            // 返回查询结果的smsType字段
            LOG.info("=== 找到启用的短信配置: smsType={}, smsStatus={}, smsIsactive={}", 
                entity.getSmsType(), entity.getSmsStatus(), entity.getSmsIsactive());
            return entity.getSmsType();
        } catch (Exception e) {
            // 检查是否是表不存在的错误
            if (e.getMessage() != null && e.getMessage().contains("doesn't exist")) {
                LOG.error("=== 短信配置表不存在，请先创建 hou_base_sms_info 表: {}", e.getMessage());
            } else {
                LOG.error("=== 查询短信状态异常: {}", e.getMessage(), e);
            }
            return null;
        }
    }

    @Override
    public ResponseResult<Map<String, Object>> selectSMSInfoByType(String type) {
        String orgId = currentOrgId();
        if (orgId == null) return ResponseResult.<Map<String, Object>>fail(403, "当前组织未确定或无权访问短信配置");
        try {
            // 根据类型查询短信配置
            // 兼容 smsIsactive = "01" 或 "1" 的情况
            SmsInfoEntity entity = this.baseMapper.selectOne(new LambdaQueryWrapper<SmsInfoEntity>()
                    .eq(SmsInfoEntity::getSmsType, type)
                    .and(w -> w.eq(SmsInfoEntity::getOrgId, orgId)
                            .or().apply("FIND_IN_SET({0}, org_id)", orgId))
                    .and(wrapper -> wrapper
                        .eq(SmsInfoEntity::getSmsIsactive, "01")
                        .or()
                        .eq(SmsInfoEntity::getSmsIsactive, "1")
                    // 只查询未删除的记录
                    ));
            
            // 检查查询结果是否为空
            if (entity == null) {
                LOG.warn("=== 未找到类型为 {} 的短信配置（smsIsactive=01或1）", type);
                return ResponseResult.<Map<String, Object>>fail(CommonStatusEnum.FAIL.getCode(), "未找到对应的短信配置信息");
            }
            
            LOG.info("=== 找到类型为 {} 的短信配置: smsStatus={}, smsIsactive={}", 
                type, entity.getSmsStatus(), entity.getSmsIsactive());
            
            // 将实体转换为Map
            Map<String, Object> map = ParseMapUtils.beanToMap(entity);
            
            // 从Redis读取模板ID（因为数据库表中没有smsGeneral字段）
            String templateIdKey = cacheKey(orgId) + "_TEMPLATE_ID_" + type;
            String templateId = redisCache.getCacheObject(templateIdKey);
            if (templateId != null && !templateId.trim().isEmpty()) {
                map.put("smsGeneral", templateId.trim());
                LOG.info("=== 从Redis读取模板ID: key={}, value={}", templateIdKey, templateId.trim());
            } else {
                LOG.warn("=== Redis中未找到模板ID: key={}", templateIdKey);
            }
            
            return ResponseResult.success(map);
            
        } catch (IllegalAccessException e) {
            LOG.error("=== 转换短信配置对象失败: {}", e.getMessage(), e);
            return ResponseResult.<Map<String, Object>>fail(CommonStatusEnum.FAIL.getCode(), "系统异常，请联系客服");
        } catch (Exception e) {
            // 检查是否是表不存在的错误
            if (e.getMessage() != null && e.getMessage().contains("doesn't exist")) {
                LOG.error("=== 短信配置表不存在，请先创建 hou_base_sms_info 表: {}", e.getMessage());
                return ResponseResult.<Map<String, Object>>fail(CommonStatusEnum.FAIL.getCode(), "短信配置表不存在，请联系管理员创建数据库表");
            } else {
                LOG.error("=== 查询短信配置异常: {}", e.getMessage(), e);
                return ResponseResult.<Map<String, Object>>fail(CommonStatusEnum.FAIL.getCode(), "系统异常，请联系客服");
            }
        }
    }

    private String currentOrgId() {
        return organizationResolver == null ? null : organizationResolver.resolveCurrentOrgId();
    }

    private String cacheKey(String orgId) {
        return SMS_LIST_CACHE_KEY + ":" + orgId;
    }

}
