package com.houjiang.userms.ucenter.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("hou_ucenter_permission_audit")
public class UcenterPermissionAuditEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long spaceId;
    private Long actorUserId;
    private Long targetMemberId;
    private UcenterPermissionEvent eventType;
    private String beforePermissions;
    private String afterPermissions;
    private String correlationId;
    private String result;
    private String clientIp;
    private String userAgent;
    private LocalDateTime createTime;
}
