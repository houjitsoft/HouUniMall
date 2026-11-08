package com.houjiang.userms.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * PM 用户映射同步 Mapper。
 *
 * PM 用户表与 userms 共用业务库；组织关系仍以 hou_user_organization 为准，
 * 这里仅维护 PM 模块所需的内部用户身份和兼容查询缓存。
 */
@Mapper
public interface PmUserSyncMapper {

    @Select("SELECT id FROM hou_pm_user WHERE user_id = #{userId} LIMIT 1")
    Integer selectPmUserIdByUserId(@Param("userId") String userId);

    @Insert("INSERT INTO hou_pm_user (user_id, org_id) VALUES (#{userId}, #{orgIds})")
    int insertPmUser(@Param("userId") String userId, @Param("orgIds") String orgIds);

    @Update("UPDATE hou_pm_user SET org_id = #{orgIds} WHERE user_id = #{userId}")
    int updatePmUserOrgIds(@Param("userId") String userId, @Param("orgIds") String orgIds);
}
