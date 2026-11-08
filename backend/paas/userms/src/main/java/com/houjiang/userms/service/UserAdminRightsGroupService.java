package com.houjiang.userms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.dto.AdminRightsGroupDTO;
import com.houjiang.userms.entity.UserAdminRightsGroupEntity;

import java.util.Map;

/**
 * @author Amos
 */
public interface UserAdminRightsGroupService extends IService<UserAdminRightsGroupEntity> {

    /**
     * 新增用户时查询权限组
     *
     * @param paraMap
     * @return
     */
    ResponseResult getStaffInfo(Map<String, String> paraMap);

    /**
     * 删除 平台权限组
     *
     * @param queryGroupInfo
     * @return
     */
    ResponseResult deleteGroupInfo(AdminRightsGroupDTO queryGroupInfo);

    /**
     * 修改平台权限组
     *
     * @param queryGroupInfo
     * @return
     */
    ResponseResult updateGroupInfo(AdminRightsGroupDTO queryGroupInfo);

    /**
     * 查询平台权限组详情
     *
     * @param queryGroupInfo
     * @return
     */
    ResponseResult queryGroupInfo(AdminRightsGroupDTO queryGroupInfo);


    /**
     * 查询平台权限组
     *
     * @param map
     * @return
     */
    ResponseResult queryGroupList(Map<String, Object> map);


    /**
     * 新增 平台权限组
     *
     * @param queryGroupInfo
     * @return
     */
    ResponseResult addGroupInfo(AdminRightsGroupDTO queryGroupInfo);
}
