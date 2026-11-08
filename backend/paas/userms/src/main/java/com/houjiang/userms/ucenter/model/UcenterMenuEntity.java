package com.houjiang.userms.ucenter.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("hou_ucenter_menu")
public class UcenterMenuEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private String parentCode;
    private String nameKey;
    private String routePath;
    private String icon;
    private Integer sort;
    private Boolean mandatory;
    private Boolean ownerOnly;
    @TableField("is_show")
    private Boolean show;
    @TableField("is_enabled")
    private Boolean enabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
