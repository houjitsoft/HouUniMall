package com.houjiang.base.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.entity.BaseKd100ConfigEntity;
import com.houjiang.base.mapper.BaseKd100ConfigMapper;
import com.houjiang.base.service.BaseKd100ConfigService;
import com.houjiang.commons.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 快递100配置Service实现类
 * @author Amos
 */
@Service
@RequiredArgsConstructor
public class BaseKd100ConfigServiceImpl extends ServiceImpl<BaseKd100ConfigMapper, BaseKd100ConfigEntity>
        implements BaseKd100ConfigService {

    /**
     * 查询快递100配置
     */
    @Override
    public ResponseResult queryConfig(Map<String, Object> param) {
        BaseKd100ConfigEntity config = this.baseMapper.selectOne(null);
        return ResponseResult.success(config);
    }

    /**
     * 更新快递100配置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult updateConfig(Map<String, Object> param) {
        this.baseMapper.updateConfig(param);
        return ResponseResult.success();
    }
}
