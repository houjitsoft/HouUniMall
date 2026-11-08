package com.houjiang.userms.ucenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.userms.ucenter.model.UcenterSpaceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UcenterSpaceMapper extends BaseMapper<UcenterSpaceEntity> {
    @Select("SELECT * FROM hou_ucenter_space WHERE id=#{id} FOR UPDATE")
    UcenterSpaceEntity findByIdForUpdate(@Param("id") Long id);

    @Update("UPDATE hou_ucenter_space SET version=version+1, update_time=NOW() WHERE id=#{id} AND status=1")
    int incrementVersion(@Param("id") Long id);

    @Update("UPDATE hou_ucenter_space SET status=0, close_time=NOW(), update_time=NOW() WHERE id=#{id} AND owner_user_id=#{ownerUserId} AND status=1")
    int closeActiveSpace(@Param("id") Long id, @Param("ownerUserId") Long ownerUserId);

    @Update("UPDATE hou_ucenter_space SET owner_user_id=#{newOwnerUserId}, version=version+1, update_time=NOW() WHERE id=#{id} AND owner_user_id=#{oldOwnerUserId} AND status=1")
    int transferOwner(@Param("id") Long id, @Param("oldOwnerUserId") Long oldOwnerUserId,
                      @Param("newOwnerUserId") Long newOwnerUserId);
}
