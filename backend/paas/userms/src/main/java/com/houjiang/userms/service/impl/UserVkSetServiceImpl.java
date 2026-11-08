package com.houjiang.userms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.dto.VkQuerySettingDTO;
import com.houjiang.userms.entity.UserVkSetEntity;
import com.houjiang.userms.mapper.UserVkSetMapper;
import com.houjiang.userms.service.UserVkSetService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 用户微客设置服务实现类
 *
 * @ClassName UserVkSetServiceImpl 用户微客设置的业务逻辑实现
 * @Author Amos
 * @Date 2023/7/14 10:22
 * @Version 1.0
 * @author Amos
 */
@Service
public class UserVkSetServiceImpl extends ServiceImpl<UserVkSetMapper, UserVkSetEntity>
        implements UserVkSetService {

    /**
     * 启用或更新微客设置
     *
     * @param dto 微客设置的DTO对象，包含需要设置的参数
     * @return 操作结果，成功返回操作成功的响应结果，失败返回相应的错误信息
     */
    @Override
    public ResponseResult enableVk(VkQuerySettingDTO dto) {
        // 查询当前微客设置是否存在
        UserVkSetEntity result = this.baseMapper.selectOne(new LambdaQueryWrapper<>());
        if (null == result) {
            // 如果不存在，则插入新的微客设置
            this.baseMapper.insertEnableVk(dto);
        } else {
            // 如果存在，则更新现有的微客设置
            this.baseMapper.updateEnableVk(dto);
        }
        // 查询分佣设置是否存在
        Map<String, Object> commissionSettingResult = this.baseMapper.commissionSetting();
        if (null == commissionSettingResult) {
            // 如果分佣设置不存在，则插入新的分佣设置
            this.baseMapper.insertVk(dto);
        } else {
            // 如果分佣设置存在，则更新现有的分佣设置
            this.baseMapper.updateVk(dto);
        }
        // 返回操作成功的响应结果
        return ResponseResult.success();
    }

    /**
     * 添加或更新微客查询设置
     *
     * @param param 微客查询设置的DTO对象，包含需要设置的参数
     * @return 操作结果，成功返回操作成功的响应结果，失败返回相应的错误信息
     */
    @Override
    public ResponseResult addOrUpdateVkQuerySetting(VkQuerySettingDTO param) {
        // 查询当前微客查询设置是否存在
        UserVkSetEntity result = this.baseMapper.selectOne(new LambdaQueryWrapper<>());
        if (null == result) {
            // 如果不存在，则插入新的微客查询设置
            this.baseMapper.insertSetting(param);
        } else {
            // 如果存在，则更新现有的微客查询设置
            this.baseMapper.updateSetting(param);
        }
        // 返回操作成功的响应结果
        return ResponseResult.success();
    }

    /**
     * 查询微客设置
     *
     * @return 微客设置的详细信息，以响应结果的形式返回
     */
    @Override
    public ResponseResult vkQuerySetting() {
        // 查询微客设置
        UserVkSetEntity result = this.baseMapper.selectOne(new LambdaQueryWrapper<>());
        // 返回查询结果
        return ResponseResult.success(result);
    }
}
