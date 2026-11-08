package com.houjiang.userms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.userms.dto.AdminDepartmentDTO;
import com.houjiang.userms.entity.UserAdminDepartmentEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Entity com.houjiang.userms.entity.UserAdminDepartment
 * @author Amos
 */
@Mapper
public interface UserAdminDepartmentMapper extends BaseMapper<UserAdminDepartmentEntity> {

    /**
     * 查询用户信息
     *
     * @param id
     * @return
     */
    Integer selectDepartmentIsUser(@Param("id") Integer id);


    /**
     * 新增
     *
     * @param adminDepartmentDto
     */
    void addDepartment(@Param("param") AdminDepartmentDTO adminDepartmentDto);
}




