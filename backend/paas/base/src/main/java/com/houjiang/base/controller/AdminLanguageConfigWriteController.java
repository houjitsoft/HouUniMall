package com.houjiang.base.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.houjiang.base.entity.BaseSysConfigsEntity;
import com.houjiang.base.mapper.BaseSysConfigsMapper;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 按配置键写入系统配置。已有键只更新值，没有键时插入。
 * 语言配置走这条路径，避免只更新已有行时新键写不进去。
 */
@RestController
@RequestMapping("base/sysConfig")
public class AdminLanguageConfigWriteController {

    @Autowired
    private BaseSysConfigsMapper baseSysConfigsMapper;

    @Autowired
    private RedisCache redisCache;

    @PostMapping("/upsertConfigValue")
    public ResponseResult upsertConfigValue(@RequestBody Map<String, Object> map) {
        if (map == null || map.get("configKey") == null || map.get("configKey").toString().isBlank()) {
            return ResponseResult.fail(BizCodeEnume.KEY_VALUE_CANNOT_BE_EMPTY);
        }
        String configKey = map.get("configKey").toString().trim();
        String configValue = map.get("configValue") == null ? "" : map.get("configValue").toString();
        BaseSysConfigsEntity existing = baseSysConfigsMapper.selectOne(new LambdaQueryWrapper<BaseSysConfigsEntity>()
                .eq(BaseSysConfigsEntity::getConfigKey, configKey));
        if (existing == null) {
            BaseSysConfigsEntity entity = new BaseSysConfigsEntity();
            entity.setConfigKey(configKey);
            entity.setConfigValue(configValue);
            entity.setConfigType(text(map.get("configType")));
            entity.setConfigComment(text(map.get("configComment")));
            entity.setOpType("");
            entity.setOpValue("");
            entity.setConfigEnable(1);
            entity.setConfigSort(0);
            entity.setTerminalType(1);
            baseSysConfigsMapper.insert(entity);
        } else {
            baseSysConfigsMapper.update(null, new LambdaUpdateWrapper<BaseSysConfigsEntity>()
                    .eq(BaseSysConfigsEntity::getConfigKey, configKey)
                    .set(BaseSysConfigsEntity::getConfigValue, configValue));
        }
        if (redisCache != null) {
            redisCache.deleteObject(configKey);
        }
        return ResponseResult.success();
    }

    private static String text(Object value) {
        return value == null ? "" : value.toString();
    }
}
