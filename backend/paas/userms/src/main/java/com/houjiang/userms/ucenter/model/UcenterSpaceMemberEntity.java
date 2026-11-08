package com.houjiang.userms.ucenter.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("hou_ucenter_space_member")
public class UcenterSpaceMemberEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long spaceId;
    private Long userId;
    private UcenterRole role;
    private String status;
    private Long joinedBy;
    private LocalDateTime joinTime;
    private LocalDateTime leaveTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
