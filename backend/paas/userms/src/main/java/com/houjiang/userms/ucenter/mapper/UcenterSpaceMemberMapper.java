package com.houjiang.userms.ucenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.userms.ucenter.model.UcenterSpaceMemberEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UcenterSpaceMemberMapper extends BaseMapper<UcenterSpaceMemberEntity> {
    @Select("SELECT * FROM hou_ucenter_space_member WHERE user_id=#{userId} AND status='ACTIVE' LIMIT 1 FOR UPDATE")
    UcenterSpaceMemberEntity findActiveByUserIdForUpdate(@Param("userId") Long userId);

    @Select("SELECT * FROM hou_ucenter_space_member WHERE user_id=#{userId} AND status='ACTIVE' LIMIT 1")
    UcenterSpaceMemberEntity findActiveByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM hou_ucenter_space_member WHERE id=#{memberId} AND status='ACTIVE' FOR UPDATE")
    UcenterSpaceMemberEntity findActiveByIdForUpdate(@Param("memberId") Long memberId);

    @Select("SELECT * FROM hou_ucenter_space_member WHERE space_id=#{spaceId} AND status='ACTIVE' ORDER BY role='OWNER' DESC, join_time")
    List<UcenterSpaceMemberEntity> findActiveBySpaceId(@Param("spaceId") Long spaceId);

    @Select("SELECT COUNT(*) FROM hou_ucenter_space_member WHERE space_id=#{spaceId} AND status='ACTIVE' AND user_id<>#{userId}")
    long countOtherActiveMembers(@Param("spaceId") Long spaceId, @Param("userId") Long userId);

    @Update("UPDATE hou_ucenter_space_member SET status='LEFT', leave_time=NOW(), update_time=NOW() WHERE id=#{memberId} AND user_id=#{userId} AND status='ACTIVE'")
    int markLeft(@Param("memberId") Long memberId, @Param("userId") Long userId);

    @Update("UPDATE hou_ucenter_space_member SET role=#{role}, update_time=NOW() WHERE id=#{memberId} AND status='ACTIVE'")
    int updateRole(@Param("memberId") Long memberId, @Param("role") String role);
}
