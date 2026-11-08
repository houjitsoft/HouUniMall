package com.houjiang.userms.ucenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.userms.ucenter.model.UcenterInvitationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UcenterInvitationMapper extends BaseMapper<UcenterInvitationEntity> {
    @Select("SELECT COUNT(*) FROM hou_ucenter_invitation WHERE invitee_user_id=#{userId} AND status='PENDING' AND expires_at>NOW()")
    long countPendingByInvitee(@Param("userId") Long userId);

    @Select("SELECT * FROM hou_ucenter_invitation WHERE id=#{id} FOR UPDATE")
    UcenterInvitationEntity findByIdForUpdate(@Param("id") Long id);

    @Select("SELECT * FROM hou_ucenter_invitation WHERE space_id=#{spaceId} AND invitee_user_id=#{inviteeUserId} AND status='PENDING' LIMIT 1 FOR UPDATE")
    UcenterInvitationEntity findPendingForUpdate(@Param("spaceId") Long spaceId,
                                                  @Param("inviteeUserId") Long inviteeUserId);

    @Select("SELECT * FROM hou_ucenter_invitation WHERE invitee_user_id=#{userId} AND status='PENDING' ORDER BY create_time DESC")
    List<UcenterInvitationEntity> findPendingByInvitee(@Param("userId") Long userId);
}
