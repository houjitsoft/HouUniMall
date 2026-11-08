package com.houjiang.base.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.dto.BaseSmsConfigDTO;
import com.houjiang.base.entity.BaseSmsInfoEntity;
import com.houjiang.base.mapper.BaseSmsInfoMapper;
import com.houjiang.base.service.BaseSmsInfoService;
import com.houjiang.base.security.BaseOrganizationResolver;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class BaseSmsInfoServiceImpl extends ServiceImpl<BaseSmsInfoMapper, BaseSmsInfoEntity>
    implements BaseSmsInfoService{

    @org.springframework.beans.factory.annotation.Autowired
    private RedisCache redisCache;

    @org.springframework.beans.factory.annotation.Autowired
    private BaseOrganizationResolver organizationResolver;

    /**
     * 查询短信配置信息，并将配置信息存入Redis
     * @return 返回查询结果，包含短信配置信息
     */
    @Override
    public ResponseResult selectSMSInfo2() {
        String orgId = currentOrgId();
        if (orgId == null) return ResponseResult.error("当前组织未确定或无权访问短信配置");
        List<BaseSmsInfoEntity> smsInfList = new ArrayList<>();
        // 初始化存储模板信息的列表
        List<Map<String,Object>> list = new ArrayList<>();
        // 查询短信配置信息
        //接收返回信息
        BaseSmsInfoEntity entity= this.baseMapper.selectOne(new LambdaQueryWrapper<BaseSmsInfoEntity>()
                .and(w -> w.eq(BaseSmsInfoEntity::getOrgId, orgId)
                        .or().apply("FIND_IN_SET({0}, org_id)", orgId)));
        if(null != entity ){
            // 设置标记，表示配置已更新
            Integer flag = 1;
            // 将短信配置信息存入Redis
            redisCache.setCacheObject(cacheKey(orgId), JSON.toJSON(entity).toString());
            entity.setFlag(flag);
            // 注意：以下模板相关字段已从数据库中删除，相关代码已注释
            // 短信模板现在统一通过消息模板系统管理（hou_base_message_template表）
            // 模板相关代码已移除，请使用消息模板系统
            // 将模板列表添加到结果中
            entity.setTemplateList(list);
            // 将短信配置信息存入Redis
            redisCache.setCacheObject(cacheKey(orgId) + "_" + entity.getSmsType(), JSON.toJSON(entity).toString());
            smsInfList.add(entity);
        }
        // 返回查询结果
        //oss配置库存在配置信息并返回给前端做展示
        return ResponseResult.success( "SMS配置信息存在并返回成功",smsInfList);
    }

    /**
     * 更新短信配置信息
     * @param baseSmsConfigReq 包含更新信息的请求对象
     * @return 返回操作结果，成功或失败
     */
    @Override
    public ResponseResult updateSmsConfig(BaseSmsConfigDTO baseSmsConfigReq) {
        String orgId = currentOrgId();
        if (orgId == null) return ResponseResult.fail(403, "当前组织未确定或无权修改短信配置");
        // 校验token，确认用户身份
        if (null != baseSmsConfigReq && null != baseSmsConfigReq.getToken()) {
            // 校验token
            String token = baseSmsConfigReq.getToken();
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token)==null? ParseTokenUtils.getPlatformUser(token):ParseTokenUtils.getUserInfoByToken(token);
            if (null != userInfo) {
                // 查询当前的短信配置信息
                BaseSmsInfoEntity smsInfoEntity = this.baseMapper.selectOne(new QueryWrapper<BaseSmsInfoEntity>()
                        .and(w -> w.eq("org_id", orgId)
                                .or().apply("FIND_IN_SET({0}, org_id)", orgId)));
                if (smsInfoEntity == null) {
                    // 如果尚未存在配置信息，则创建新的配置项
                    smsInfoEntity = new BaseSmsInfoEntity();
                    BeanUtils.copyProperties(baseSmsConfigReq, smsInfoEntity);
                    smsInfoEntity.setOrgId(orgId);
                    // 设置配置状态为启用
                    smsInfoEntity.setSmsStatus(CommonType.smsEnableDisableStatus.STARTUSING.getCode().toString());
                    smsInfoEntity.setSmsIsactive(CommonType.pseudoDeleteIdentification.NORMAL.getCode().toString());
                    // 插入新的短信配置信息
                    //oss配置库没有配置信息就初始化数据
                    this.baseMapper.insert(smsInfoEntity);
                    redisCache.setCacheObject(cacheKey(orgId), JSON.toJSONString(smsInfoEntity));
                } else {
                    // 如果已经存在配置信息，则更新配置项
                    // 修改sms 配置
                    BeanUtils.copyProperties(baseSmsConfigReq, smsInfoEntity);
                    smsInfoEntity.setOrgId(orgId);
                    this.baseMapper.updateById(smsInfoEntity);
                    // 清除Redis中的旧配置信息，并写入新配置信息
                    redisCache.deleteObject(cacheKey(orgId));
                    redisCache.setCacheObject(cacheKey(orgId), JSON.toJSONString(smsInfoEntity));
                }
                if(smsInfoEntity.getSmsStatus().equals("01")){
                    baseMapper.update(null, new LambdaUpdateWrapper<BaseSmsInfoEntity>()
                            .set(BaseSmsInfoEntity::getSmsStatus, "00")
                            .ne(BaseSmsInfoEntity::getId, smsInfoEntity.getId())
                            .and(w -> w.eq(BaseSmsInfoEntity::getOrgId, orgId)
                                    .or().apply("FIND_IN_SET({0}, org_id)", orgId)));
                }
                return ResponseResult.success();
            }
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
        }
        return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION);
    }

    /**
     * 更新短信配置信息
     * @param baseSmsConfigReq 包含更新信息的参数Map
     * @return 返回操作结果，成功或失败
     */
    @Override
    public ResponseResult updateSMS(BaseSmsConfigDTO baseSmsConfigReq) {
        String orgId = currentOrgId();
        if (orgId == null) return ResponseResult.fail(403, "当前组织未确定或无权修改短信配置");
        // 检查请求对象是否为空
        if (null != baseSmsConfigReq) {
            // 根据查询条件获取当前的短信配置信息
            BaseSmsInfoEntity smsInfoEntity = null;
            // 如果提供了ID，则根据ID查询
            if (baseSmsConfigReq.getId() != null) {
                smsInfoEntity = this.baseMapper.selectOne(new LambdaQueryWrapper<BaseSmsInfoEntity>()
                        .eq(BaseSmsInfoEntity::getId, baseSmsConfigReq.getId())
                        .and(w -> w.eq(BaseSmsInfoEntity::getOrgId, orgId)
                                .or().apply("FIND_IN_SET({0}, org_id)", orgId)));
            }
            // 如果根据ID没找到，且提供了smsType，则根据smsType查询
            if (smsInfoEntity == null && baseSmsConfigReq.getSmsType() != null) {
                smsInfoEntity = this.baseMapper.selectOne(new LambdaQueryWrapper<BaseSmsInfoEntity>()
                        .eq(BaseSmsInfoEntity::getSmsType, baseSmsConfigReq.getSmsType())
                        .and(w -> w.eq(BaseSmsInfoEntity::getOrgId, orgId)
                                .or().apply("FIND_IN_SET({0}, org_id)", orgId))
                        .eq(BaseSmsInfoEntity::getSmsIsactive, "01"));
            }

            // 当前没有短信配置信息时，创建新的配置项并初始化属性
            if (smsInfoEntity == null) {
                smsInfoEntity = new BaseSmsInfoEntity();
                // 从请求对象复制属性到新的配置实体
                BeanUtils.copyProperties(baseSmsConfigReq, smsInfoEntity);
                smsInfoEntity.setOrgId(orgId);
                // 设置短信配置的状态为启用，并设置激活状态为正常
                smsInfoEntity.setSmsStatus(CommonType.smsEnableDisableStatus.STARTUSING.getCode().toString());
                smsInfoEntity.setSmsIsactive(CommonType.pseudoDeleteIdentification.NORMAL.getCode().toString());
                // 插入新的短信配置到数据库
                this.baseMapper.insert(smsInfoEntity);
                // 将新的短信配置信息存储到Redis
                redisCache.setCacheObject(cacheKey(orgId), JSON.toJSONString(smsInfoEntity));
            } else {
                // 当前已有短信配置信息时，更新现有的配置项
                BeanUtils.copyProperties(baseSmsConfigReq, smsInfoEntity);
                smsInfoEntity.setOrgId(orgId);
                // 根据ID更新短信配置信息
                this.baseMapper.updateById(smsInfoEntity);
                // 删除Redis中的短信配置信息，以触发下次从数据库重新加载
                redisCache.deleteObject(cacheKey(orgId));
            }
            if(smsInfoEntity.getSmsStatus().equals("01")){
                    baseMapper.update(null, new LambdaUpdateWrapper<BaseSmsInfoEntity>()
                            .set(BaseSmsInfoEntity::getSmsStatus, "00")
                            .ne(BaseSmsInfoEntity::getId, smsInfoEntity.getId())
                            .and(w -> w.eq(BaseSmsInfoEntity::getOrgId, orgId)
                                    .or().apply("FIND_IN_SET({0}, org_id)", orgId)));
            }
            redisCache.deleteObject(cacheKey(orgId));
            // 操作成功返回
            return ResponseResult.success();
        }
        // 请求对象为空，操作失败返回
        return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION);
    }

    private String currentOrgId() {
        return organizationResolver == null ? null : organizationResolver.resolveCurrentOrgId();
    }

    private String cacheKey(String orgId) {
        return HouConfig.SMS_CONFIG + ":" + orgId;
    }
}
