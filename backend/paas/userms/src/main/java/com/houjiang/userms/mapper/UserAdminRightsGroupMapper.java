package com.houjiang.userms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.userms.dto.AdminRightsGroupDTO;
import com.houjiang.userms.entity.UserAdminRightsGroupEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Entity com.houjiang.userms.entity.UserAdminRightsGroup
 * @author Amos
 */
@Mapper
public interface UserAdminRightsGroupMapper extends BaseMapper<UserAdminRightsGroupEntity> {

    /**
     * 更新组信息
     *
     * @param queryGroupInfo
     */
    void updateGroupInfo(AdminRightsGroupDTO queryGroupInfo);


    /**
     * 添加组信息
     *
     * @param queryGroupInfo
     */
    void addGroupInfo(AdminRightsGroupDTO queryGroupInfo);
}




