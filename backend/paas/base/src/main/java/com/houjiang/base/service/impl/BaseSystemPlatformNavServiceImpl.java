package com.houjiang.base.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.dto.BaseSystemPlatformNavDTO;
import com.houjiang.base.entity.BaseSystemPlatformNavEntity;
import com.houjiang.base.mapper.BaseSystemPlatformNavMapper;
import com.houjiang.base.service.BaseSystemPlatformNavService;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 页面导航表(BaseSystemPlatformNav)
 *
 * @since 2023-03-28 17:29:45
 * @author Amos
 */
@Service
public class BaseSystemPlatformNavServiceImpl extends ServiceImpl<BaseSystemPlatformNavMapper, BaseSystemPlatformNavEntity>
        implements BaseSystemPlatformNavService {


    /**
     * 更新平台导航信息。
     * 根据传入的导航数据对象，如果导航ID为空，则插入新导航；否则更新现有导航。
     * 需要验证用户token的有效性。
     *
     * @param baseSystemPlatformNavDto 导航数据对象，包含导航相关信息。
     * @return 操作结果，成功或失败。
     */
    @Override
    public ResponseResult updatePlatformNav(BaseSystemPlatformNavDTO baseSystemPlatformNavDto) {
        // 通过token获取用户信息，用于权限验证
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(baseSystemPlatformNavDto.getToken());
        if (null != userInfo) {
            // 如果导航ID为空，则插入新导航
            if (null == baseSystemPlatformNavDto.getNavId()) {
                this.baseMapper.insertPlatformNav(baseSystemPlatformNavDto);
            } else {
                // 否则更新现有导航
                this.baseMapper.updatePlatformNav(baseSystemPlatformNavDto);
            }
            // 操作成功返回
            return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
        }
        // token验证失败返回
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 删除平台导航信息。
     * 根据传入的导航ID删除对应的导航项。
     * 需要验证用户token的有效性。
     *
     * @param baseSystemPlatformNavDto 导航数据对象，包含待删除导航的ID。
     * @return 操作结果，成功或失败。
     */
    @Override
    public ResponseResult deletePlatformNav(BaseSystemPlatformNavDTO baseSystemPlatformNavDto) {
        // 通过token获取用户信息，用于权限验证
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(baseSystemPlatformNavDto.getToken());
        if (null != userInfo) {
            // 根据导航ID删除导航项
            this.baseMapper.deleteById(baseSystemPlatformNavDto.getNavId());
            // 操作成功返回
            return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
        }
        // token验证失败返回
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 根据ID查询平台导航信息。
     * 根据传入的导航ID查询对应的导航项详情。
     * 需要验证用户token的有效性。
     *
     * @param baseSystemPlatformNavDto 导航数据对象，包含查询导航的ID。
     * @return 导航详情，成功；失败返回空。
     */
    @Override
    public ResponseResult querySystemPlatformNavById(BaseSystemPlatformNavDTO baseSystemPlatformNavDto) {
        // 通过token获取用户信息，用于权限验证
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(baseSystemPlatformNavDto.getToken());
        if (null != userInfo) {
            // 根据导航ID查询导航项详情
            BaseSystemPlatformNavEntity baseSystemPlatformNaventity = this.baseMapper.selectById(baseSystemPlatformNavDto.getNavId());
            // 返回查询结果
            return ResponseResult.success(baseSystemPlatformNaventity);
        }
        // token验证失败返回
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 查询平台导航列表。
     * 根据传入的属性查询平台导航列表，并按显示顺序升序排列。
     * 需要验证用户token的有效性。
     *
     * @param baseSystemPlatformNavDto 导航数据对象，包含查询条件。
     * @return 导航列表，成功；失败返回空。
     */
    @Override
    public ResponseResult querySystemPlatformNavList(BaseSystemPlatformNavDTO baseSystemPlatformNavDto) {
        // 通过token获取用户信息，用于权限验证
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(baseSystemPlatformNavDto.getToken());
        if (null != userInfo) {
            // 根据属性查询导航列表，并按显示顺序升序排列
            List<BaseSystemPlatformNavEntity> baseSystemPlatformNavEntities = this.baseMapper.selectList(new LambdaQueryWrapper<BaseSystemPlatformNavEntity>()
                                                                                                 .eq(BaseSystemPlatformNavEntity::getNavProperty, baseSystemPlatformNavDto.getNavProperty())
                                                                                                 .orderByAsc(BaseSystemPlatformNavEntity::getNavDisplayorder));
            // 返回查询结果
            return ResponseResult.success(baseSystemPlatformNavEntities);
        }
        // token验证失败返回
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }


}

