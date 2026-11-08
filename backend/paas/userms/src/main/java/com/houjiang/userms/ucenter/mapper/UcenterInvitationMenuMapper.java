package com.houjiang.userms.ucenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.userms.ucenter.model.UcenterInvitationMenuEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UcenterInvitationMenuMapper extends BaseMapper<UcenterInvitationMenuEntity> {
    @Select("SELECT menu_id FROM hou_ucenter_invitation_menu WHERE invitation_id=#{invitationId}")
    List<Long> findMenuIds(@Param("invitationId") Long invitationId);

    @Delete("DELETE FROM hou_ucenter_invitation_menu WHERE invitation_id=#{invitationId}")
    int deleteByInvitationId(@Param("invitationId") Long invitationId);
}
