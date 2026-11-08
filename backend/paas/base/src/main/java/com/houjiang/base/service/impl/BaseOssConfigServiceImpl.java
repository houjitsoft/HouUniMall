package com.houjiang.base.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.dto.BaseOssConfigDTO;
import com.houjiang.base.entity.BaseModularizationFlieEntity;
import com.houjiang.base.entity.BaseOssConfigEntity;
import com.houjiang.base.mapper.BaseModularizationFlieMapper;
import com.houjiang.base.mapper.BaseOssConfigMapper;
import com.houjiang.base.service.BaseOssConfigService;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName BaseOssConfigServiceImpl
 * @Description OSS
 * @Author Amos
 * @Date 2023/6/21 16:43
 * @Version 1.0
 * @author Amos
 */
@Service
public class BaseOssConfigServiceImpl extends ServiceImpl<BaseOssConfigMapper, BaseOssConfigEntity> implements BaseOssConfigService {

    @Autowired
    private BaseOssConfigMapper baseOssConfigMapper;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private BaseModularizationFlieMapper modularizationFlieMapper;

/**
 * 通过Feign方式查询OSS配置信息
 * @param ossType OSS类型
 * @return 查询结果
 */
@Override
public ResponseResult selectOSSInfoFeign(String ossType) {
    // 初始化OSS配置实体
    //接收返回信息
    BaseOssConfigEntity ossConfigEntity = new BaseOssConfigEntity();
    // 从数据库中查询OSS配置信息
    //redis 中不存在 查询数据库 存入redis 返回
    ossConfigEntity = baseOssConfigMapper.selectOSSInfo(ossType);
    if (ossConfigEntity != null) {
        // 将OSS配置信息存入Redis
        redisCache.setCacheObject(HouConfig.OSS_CONFIG, JSONObject.toJSONString(ossConfigEntity));
        // 返回成功结果，包含OSS配置信息
        return ResponseResult.success(CommonType.initializationStatus.SUCCESS.getCode(), BizCodeEnume.OSS_SUCCESS.getMsg(), ossConfigEntity);
    } else {
        // 返回失败结果
        return ResponseResult.fail(CommonType.initializationStatus.FAIL.getCode(), CommonType.initializationStatus.FAIL.getDesc());
    }
}

/**
 * 查询OSS配置信息
 * @param param 查询参数，包含token和ossType
 * @return 查询结果
 */
@Override
public ResponseResult selectOSSInfo(Map<String, Object> param) {
    // 从token中获取用户信息
    String token = param.get("token").toString();
    UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(token);
    if (userInfo != null) {
        // 初始化OSS配置实体
        //接收返回信息
        BaseOssConfigEntity ossConfigEntity = new BaseOssConfigEntity();
        // 默认OSS类型为阿里云，从参数中获取实际类型
        String ossType = param.get("ossType") == null ? CommonType.ossType.ALIYUN.getDesc() : (String) param.get("ossType");
        // 从数据库中查询OSS配置信息
        //redis 中不存在 查询数据库 存入redis 返回
        ossConfigEntity = baseOssConfigMapper.selectOSSInfo(ossType);
        if (ossConfigEntity != null) {
            // 返回成功结果，包含OSS配置信息
            return ResponseResult.success(CommonType.initializationStatus.SUCCESS.getCode(), BizCodeEnume.OSS_SUCCESS.getMsg(), ossConfigEntity);
        } else {
            // 返回失败结果
            return ResponseResult.fail(CommonType.initializationStatus.FAIL.getCode(), CommonType.initializationStatus.FAIL.getDesc());
        }
    }
    // 用户信息验证失败，返回失败结果
    return ResponseResult.fail(CommonStatusEnum.TOKEN_ERROR.getCode(), CommonStatusEnum.TOKEN_ERROR.getValue());
}



    /**
     * 更新OSS配置信息。
     * 通过接收BaseOssConfigDTO对象来更新OSS配置，并在更新后刷新Redis中的配置缓存。
     * @param baseOssConfigReq 包含OSS配置信息的请求对象。
     * @return 返回操作结果，成功或失败。
     */
    @Override
    public ResponseResult updateOssConfig(BaseOssConfigDTO baseOssConfigReq) {
        // 校验token是否有效
        if (null != baseOssConfigReq && null != baseOssConfigReq.getToken()) {
            // 校验token
            String token = baseOssConfigReq.getToken();
            UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(token);
            if (null != userInfo) {
                if(baseOssConfigReq.getOssAccessKeyId().contains("*")
                   ||  baseOssConfigReq.getOssAccessKeySecret().contains("*")
                ){
                    return ResponseResult.fail(BizCodeEnume.REVISED_AGAIN);
                }
                // 更新OSS配置
                //修moss 配置
                this.baseMapper.updateOssConfig(baseOssConfigReq);
                // 从Redis中移除OSS配置缓存
                redisCache.deleteObject(HouConfig.OSS_CONFIG);
                // 重新从数据库中获取最新的OSS配置并更新到Redis中
                BaseOssConfigEntity entity = this.baseMapper.selectOne(new LambdaQueryWrapper<BaseOssConfigEntity>()
                        .eq(BaseOssConfigEntity::getOssStates, "01"));
                redisCache.setCacheObject(HouConfig.OSS_CONFIG, JSON.toJSONString(entity));
                return ResponseResult.success();
            }
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
        }
        return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION);
    }


    /**
     * 更新OSS状态。
     * 根据传入的参数更新指定OSS配置的状态，并相应地更新其他配置的状态。
     * @param param 包含OSS状态更新信息的请求对象。
     * @return 返回操作结果，成功或失败。
     */
    @Override
    public ResponseResult updateOssStates(BaseOssConfigDTO param) {
        // 校验token是否有效
        if (null != param && null != param.getToken()) {
            // 校验token
            String token = param.getToken();
            UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(token);
            if (null != userInfo) {
                // 更新指定OSS配置的状态
                this.baseMapper.update(null, new LambdaUpdateWrapper<BaseOssConfigEntity>()
                        .set(BaseOssConfigEntity::getOssStates, param.getOssStates())
                        .eq(BaseOssConfigEntity::getOssId, param.getOssId()));
                // 启用某个配置时，将其余配置全部禁用（保持只有一个启用）
                // 禁用时，其余配置保持原状，不做任何改动
                if ("01".equals(param.getOssStates())) {
                    this.baseMapper.update(null, new LambdaUpdateWrapper<BaseOssConfigEntity>()
                            .set(BaseOssConfigEntity::getOssStates, "00")
                            .ne(BaseOssConfigEntity::getOssId, param.getOssId()));
                }
                // 从Redis中移除OSS配置缓存
                redisCache.deleteObject(HouConfig.OSS_CONFIG);
                // 重新从数据库中获取最新的OSS配置并更新到Redis中
                BaseOssConfigEntity entity = this.baseMapper.selectOne(new LambdaQueryWrapper<BaseOssConfigEntity>()
                        .eq(BaseOssConfigEntity::getOssStates, "01"));
                redisCache.setCacheObject(HouConfig.OSS_CONFIG, JSON.toJSONString(entity));
                return ResponseResult.success();
            }
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
        }
        return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION);
    }

    /**
     * 查询当前启用的OSS类型。
     * @return 返回当前启用的OSS类型的字符串。
     */
    @Override
    public String queryOssStates() {
        BaseOssConfigEntity entity = this.baseMapper.selectOne(new LambdaQueryWrapper<BaseOssConfigEntity>()
                .eq(BaseOssConfigEntity::getOssStates, "01"));
        return entity.getOssType();
    }

    /**
     * 查询所有的OSS配置信息。
     * @return 返回包含所有OSS配置信息的列表。
     */
    @Override
    public ResponseResult queryAllOssConfig() {
        List<BaseOssConfigEntity> list = this.baseMapper.selectList(new QueryWrapper<>());
        for (BaseOssConfigEntity baseOssConfigEntity : list) {
            baseOssConfigEntity.setOssAccessKeyId(maskMiddleChars(baseOssConfigEntity.getOssAccessKeyId()));
            baseOssConfigEntity.setOssAccessKeySecret(maskMiddleChars(baseOssConfigEntity.getOssAccessKeySecret()));
        }
        return ResponseResult.success(list);
    }

    @Override
    public ResponseResult queryStorageMonitor() {
        List<BaseOssConfigEntity> configs = this.baseMapper.selectList(new QueryWrapper<>());
        int enabledCount = (int) configs.stream()
                .filter(item -> "01".equals(item.getOssStates()))
                .count();
        BaseOssConfigEntity active = this.baseMapper.selectOne(new LambdaQueryWrapper<BaseOssConfigEntity>()
                .eq(BaseOssConfigEntity::getOssStates, "01")
                .last("LIMIT 1"));

        long totalFiles = modularizationFlieMapper.selectCount(null);
        long imageFiles = modularizationFlieMapper.selectCount(new LambdaQueryWrapper<BaseModularizationFlieEntity>()
                .eq(BaseModularizationFlieEntity::getFileType, 1));
        long videoFiles = modularizationFlieMapper.selectCount(new LambdaQueryWrapper<BaseModularizationFlieEntity>()
                .eq(BaseModularizationFlieEntity::getFileType, 2));

        Map<String, Object> activeConfig = null;
        if (active != null) {
            activeConfig = new LinkedHashMap<>();
            activeConfig.put("ossType", active.getOssType());
            activeConfig.put("ossBacketName", active.getOssBacketName());
            activeConfig.put("ossFolder", active.getOssFolder());
            activeConfig.put("ossEndpoint", active.getOssEndpoint());
            activeConfig.put("ossRegion", active.getOssRegion());
            activeConfig.put("ossUpdateDate", active.getOssUpdateDate());
        }

        List<Map<String, Object>> configSummary = configs.stream().map(item -> {
            Map<String, Object> row = new HashMap<>();
            row.put("ossId", item.getOssId());
            row.put("ossType", item.getOssType());
            row.put("ossStates", item.getOssStates());
            row.put("ossBacketName", item.getOssBacketName());
            row.put("ossEndpoint", item.getOssEndpoint());
            row.put("ossFolder", item.getOssFolder());
            row.put("ossUpdateDate", item.getOssUpdateDate());
            return row;
        }).toList();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalConfigs", configs.size());
        data.put("enabledConfigs", enabledCount);
        data.put("activeOssType", active != null ? active.getOssType() : null);
        data.put("activeConfig", activeConfig);
        data.put("configs", configSummary);
        data.put("fileStats", Map.of(
                "total", totalFiles,
                "image", imageFiles,
                "video", videoFiles
        ));
        return ResponseResult.success(data);
    }

    /**
     * 用来隐藏字符串中间部分。
     * 字符串长度不足 head+tail 时直接返回全星号隐葵。
     * @param code 原始字符串
     * @return 处理后的字符串
     */
    public static String maskMiddleChars(String code) {
        if (code == null || code.isEmpty()) {
            return code;
        }
        int head  = 3;
        int tail = 4;
        // 计算原始字符串的长度
        int length = code.length();
        // 如果字符串太短，全部隐葵
        if (length <= head + tail) {
            StringBuilder masked = new StringBuilder();
            for (int i = 0; i < length; i++) {
                masked.append('*');
            }
            return masked.toString();
        }
        // 计算需要被星号替换的字符数量
        int maskedLength = length - head - tail;
        // 构建新的字符串
        StringBuilder maskedCode = new StringBuilder();
        // 添加头部未被替换的字符
        maskedCode.append(code.substring(0, head));
        // 添加星号
        for (int i = 0; i < maskedLength; i++) {
            maskedCode.append('*');
        }
        // 添加尾部未被替换的字符
        maskedCode.append(code.substring(length - tail));
        return maskedCode.toString();
    }
}
