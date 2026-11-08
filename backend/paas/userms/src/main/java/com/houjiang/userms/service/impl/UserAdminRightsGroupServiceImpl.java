package com.houjiang.userms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.userms.dto.AdminRightsGroupDTO;
import com.houjiang.userms.entity.UserAdminBaseEntity;
import com.houjiang.userms.entity.UserAdminRightsGroupEntity;
import com.houjiang.userms.mapper.UserAdminBaseMapper;
import com.houjiang.userms.mapper.UserAdminRightsGroupMapper;
import com.houjiang.userms.service.UserAdminRightsGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理服务实现类，主要负责管理员权限组的增删改查操作。
 * @author Amos
 */
@Service
public class UserAdminRightsGroupServiceImpl extends ServiceImpl<UserAdminRightsGroupMapper, UserAdminRightsGroupEntity>
    implements UserAdminRightsGroupService{

    @Autowired
    private UserAdminBaseMapper userAdminBaseMapper;

    /**
     * 获取员工信息及权限组列表。
     * @param paraMap 请求参数映射表，包含token信息。
     * @return 员工信息及权限组列表。
     */
    @Override
    public ResponseResult getStaffInfo(Map<String, String> paraMap) {
        // 通过token解析获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(paraMap.get("token"));
        if (null != userInfo) {
            // 初始化返回数据的映射
            Map<String, Object> returnMap = new HashMap<String, Object>();
            // 查询所有权限组信息
            // 获取所有权限组信息
            List<UserAdminRightsGroupEntity> listRightSGroup = this.baseMapper.selectList(new LambdaQueryWrapper<UserAdminRightsGroupEntity>());
            // 将权限组信息放入返回映射中
            returnMap.put("listRightSGroup", listRightSGroup);
            // 返回成功结果，包含权限组信息
            return ResponseResult.success(returnMap);
        }
        // 如果用户信息为空，返回令牌非法的错误结果
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);

    }

    /**
     * 删除权限组信息。
     * @param queryGroupInfo 权限组信息DTO，包含token和权限组ID。
     * @return 删除操作结果。
     */
    @Override
    public ResponseResult deleteGroupInfo(AdminRightsGroupDTO queryGroupInfo) {
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(queryGroupInfo.getToken());
        if (null != userInfo) {
            // 判断当前权限组是否被使用
            Long count = userAdminBaseMapper.selectCount(new LambdaQueryWrapper<UserAdminBaseEntity>()
                    .eq(UserAdminBaseEntity::getRightsGroupId, queryGroupInfo.getRightsGroupId().intValue()));
            if (count == 0) {
                // 未被使用，可删除
                this.baseMapper.deleteById(queryGroupInfo.getRightsGroupId());
                return ResponseResult.success(BizCodeEnume.DELETE_SUCCESS);
            }
            // 权限组正在使用中，无法删除
            return ResponseResult.fail(BizCodeEnume.GROUPING_IS_RELATED);
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 更新权限组信息。
     * @param queryGroupInfo 权限组信息DTO，包含token和更新后的权限组信息。
     * @return 更新操作结果。
     */
    @Override
    public ResponseResult updateGroupInfo(AdminRightsGroupDTO queryGroupInfo) {
        // 通过解析Token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(queryGroupInfo.getToken());
        // 如果用户信息不为空，则更新权限组信息并返回成功响应
        if (null != userInfo) {
            // 更新权限组信息
            this.baseMapper.updateGroupInfo(queryGroupInfo);
            return ResponseResult.success();
        }
        // 如果用户信息为空，则返回Token非法的错误响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);

    }

    /**
     * 根据权限组ID查询权限组信息。
     * @param queryGroupInfo 权限组信息DTO，包含token和权限组ID。
     * @return 查询到的权限组信息。
     */
    @Override
    public ResponseResult queryGroupInfo(AdminRightsGroupDTO queryGroupInfo) {
        // 通过解析Token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(queryGroupInfo.getToken());
        if (null != userInfo) {
            // 根据权限组ID查询权限组信息
            // 根据ID查询权限组信息
            UserAdminRightsGroupEntity adminRightsGroupEntity = this.baseMapper.selectById(queryGroupInfo.getRightsGroupId());
            // 返回查询到的权限组信息
            return ResponseResult.success(adminRightsGroupEntity);
        }
        // 如果用户信息为空，表示Token无效，返回失败响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);

    }

    /**
     * 查询权限组列表。
     * @param map 请求参数映射表，包含token信息。
     * @return 权限组列表。
     */
    @Override
    public ResponseResult queryGroupList(Map<String, Object> map) {
        // 通过token解析获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(map.get("token").toString());
        if (null != userInfo) {
            // 查询所有权限组信息
            // 查询所有权限组信息
            List<UserAdminRightsGroupEntity> list = this.baseMapper.selectList(new QueryWrapper<>());
            // 返回查询到的所有权限组信息
            return ResponseResult.success(list);
        }
        // 如果用户信息为空，表示token非法，返回失败响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);

    }

    /**
     * 添加权限组信息。
     * @param queryGroupInfo 权限组信息DTO，包含token和待添加的权限组信息。
     * @return 添加操作结果。
     */
    @Override
    public ResponseResult addGroupInfo(AdminRightsGroupDTO queryGroupInfo) {
        // 通过解析Token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(queryGroupInfo.getToken());
        // 如果用户信息不为空，则进行后续操作
        if (null != userInfo) {
            // 添加权限组信息到数据库
            // 添加权限组信息
            this.baseMapper.addGroupInfo(queryGroupInfo);
            // 返回操作成功的响应结果
            return ResponseResult.success();
        }
        // 如果用户信息为空，表示Token可能无效或过期，返回操作失败的响应结果
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);

    }
}
