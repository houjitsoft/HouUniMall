package com.houjiang.base.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.dto.BaseWebConfigDTO;
import com.houjiang.base.entity.BaseWebConfigEntity;
import com.houjiang.base.mapper.BaseWebConfigMapper;
import com.houjiang.base.service.BaseWebConfigService;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.config.RedisCache;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 网站配置接口实现
 * @author Amos
 */
@Service
public class BaseWebConfigServiceImpl extends ServiceImpl<BaseWebConfigMapper, BaseWebConfigEntity>
    implements BaseWebConfigService{

    @org.springframework.beans.factory.annotation.Autowired
    private RedisCache redisCache;

    /**
     * 编辑配置信息。
     * 通过接收配置信息的DTO对象，实现配置的新增或更新操作。首先检查token的有效性，然后根据配置键是否存在于数据库中，
     * 决定是新增配置还是更新已有的配置。该方法覆盖了父类中的同名方法。
     *
     * @param param 包含配置信息的DTO对象，包括配置键、配置值和token等信息。
     * @return 返回操作结果，如果token无效，则返回失败响应，否则返回成功响应。
     */
    @Override
    public ResponseResult editConfig(BaseWebConfigDTO param) {
        // 检查token是否存在于Redis中，以验证有效性
        //判断token是否过期
        boolean isExist = redisCache.hasKey(HouConfig.HOU_PLATFORM_TOKEN_USER + param.getToken());
        if (!isExist) {
            // 如果token无效，返回相应的错误码
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
        }

        // 根据配置键查询数据库中是否存在对应的配置项
        //查询key是否存在，存在修改，否则新增
        BaseWebConfigEntity baseWebConfigEntity = baseMapper.selectById(param.getConfigKey());
        if(baseWebConfigEntity == null){
            // 如果配置项不存在，创建新的配置项并插入到数据库
            BaseWebConfigEntity webConfigDO = new BaseWebConfigEntity();
            BeanUtils.copyProperties(param,webConfigDO);
            webConfigDO.setConfigEnable(CommonType.configEnable.AVAILABLE.getCode().toString());
            baseMapper.insert(webConfigDO);
            return ResponseResult.success();
        }
        // 如果配置项已存在，更新配置项的值
        baseWebConfigEntity.setConfigValue(param.getConfigValue());
        baseMapper.updateById(baseWebConfigEntity);
        return ResponseResult.success();
    }
}




