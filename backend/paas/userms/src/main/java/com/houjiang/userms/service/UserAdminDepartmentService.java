package com.houjiang.userms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.dto.AdminDepartmentDTO;
import com.houjiang.userms.entity.UserAdminDepartmentEntity;

import java.util.Map;

/**
 * @author Amos
 */
public interface UserAdminDepartmentService extends IService<UserAdminDepartmentEntity> {

    /**
     * 删除 平台部门
     *
     * @param adminDepartmentDto
     * @return
     */
    ResponseResult deleteDepartment(AdminDepartmentDTO adminDepartmentDto);

    /**
     * 查询平台所有部门
     *
     * @param map
     * @return
     */
    ResponseResult queryDepartmentAll(Map<String, Object> map);


    /**
     * 新增 或 修改 平台部门
     *
     * @param adminDepartmentDto
     * @return
     */
    ResponseResult addDepartment(AdminDepartmentDTO adminDepartmentDto);
}
