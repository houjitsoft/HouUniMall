package com.houjiang.userms.ucenter.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("hou_ucenter_member_menu")
public class UcenterMemberMenuEntity {
    private Long memberId;
    private Long menuId;
    private Long grantedBy;
    private LocalDateTime createTime;
}
