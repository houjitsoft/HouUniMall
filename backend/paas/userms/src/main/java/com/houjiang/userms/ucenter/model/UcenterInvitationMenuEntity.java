package com.houjiang.userms.ucenter.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("hou_ucenter_invitation_menu")
public class UcenterInvitationMenuEntity {
    private Long invitationId;
    private Long menuId;
}
