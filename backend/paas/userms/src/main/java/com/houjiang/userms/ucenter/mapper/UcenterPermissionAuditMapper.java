package com.houjiang.userms.ucenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.userms.ucenter.model.UcenterPermissionAuditEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UcenterPermissionAuditMapper extends BaseMapper<UcenterPermissionAuditEntity> {
    @Select("SELECT * FROM hou_ucenter_permission_audit WHERE space_id=#{spaceId} ORDER BY create_time DESC, id DESC LIMIT #{limit}")
    List<UcenterPermissionAuditEntity> findRecent(@Param("spaceId") Long spaceId, @Param("limit") int limit);
}
