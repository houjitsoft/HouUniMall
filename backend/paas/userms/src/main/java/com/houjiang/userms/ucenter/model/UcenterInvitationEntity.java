package com.houjiang.userms.ucenter.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("hou_ucenter_invitation")
public class UcenterInvitationEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long spaceId;
    private Long inviterUserId;
    private Long inviteeUserId;
    private String maskedMobile;
    private String tokenDigest;
    private UcenterInvitationStatus status;
    private LocalDateTime expiresAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
