package com.houjiang.base.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.dto.BaseSensitiveConfigDTO;
import com.houjiang.base.entity.BaseSensitiveConfigEntity;
import com.houjiang.base.mapper.BaseSensitiveConfigMapper;
import com.houjiang.base.service.BaseSensitiveConfigService;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.config.RedisCache;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * @ClassName BaseSensitiveConfigServiceImpl
 * @Description  敏感词配置
 * @Author Amos
 * @Date 2024-01-30 16:48:22
 * @Version 1.0
 * @author Amos
 */
@Service
public class BaseSensitiveConfigServiceImpl extends ServiceImpl<BaseSensitiveConfigMapper, BaseSensitiveConfigEntity>
        implements BaseSensitiveConfigService {

    @org.springframework.beans.factory.annotation.Autowired
    private RedisCache redisCache;

    /**
     * 根据传入的配置参数查询敏感配置信息。
     *
     * @param param 敏感配置的DTO对象，用于传递查询条件。
     * @return 返回查询到的敏感配置信息。
     */
    @Override
    public ResponseResult querySensitiveConfig(BaseSensitiveConfigDTO param) {
        // 通过baseMapper查询唯一的敏感配置实体
        BaseSensitiveConfigEntity baseSensitiveConfigEntity = this.baseMapper.selectOne(null);
        String str = JSONObject.toJSONString(baseSensitiveConfigEntity);
        // 读侧 SensitiveCheckAspect 用 RedisUtil(Jedis 字符串)读取，保持读写序列化一致，
        // 避免 redisCache 写入多包一层引号导致 fastjson 解析失败
        RedisUtil.set(HouConfig.HOU_SENSITIVE_CONFIG, str);
        // 返回查询结果作为响应
        return ResponseResult.success(baseSensitiveConfigEntity);
    }

    /**
     * 更新敏感配置信息。
     *
     * @param param 敏感配置的DTO对象，包含需要更新的配置信息。
     * @return 返回更新操作的结果。
     */
    @Override
    public ResponseResult updateSensitiveConfig(BaseSensitiveConfigDTO param) {
        // 创建一个新的敏感配置实体
        BaseSensitiveConfigEntity entity = new BaseSensitiveConfigEntity();
        // 将DTO对象的属性复制到实体对象
        BeanUtils.copyProperties(param, entity);
        // 通过baseMapper根据ID更新敏感配置实体
        this.baseMapper.updateById(entity);
        // 将更新后的敏感配置实体转换为JSON字符串，并存储到Redis中
        // 配置更新到 redis（与读侧 RedisUtil 保持一致的字符串写入）
        String str = JSONObject.toJSONString(entity);
        RedisUtil.set(HouConfig.HOU_SENSITIVE_CONFIG, str);
        // 返回更新成功的响应
        return  ResponseResult.success();
    }
}
