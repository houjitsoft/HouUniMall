package com.houjiang.mall.entity.auth;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;

/**
 * Mall 系统角色实体类
 * 对应表: hou_mall_system_role
 *
 * @author Amos
 */
@Data
@TableName("hou_mall_system_role")
public class MallSystemRole implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 角色ID */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 角色名称 */
    private String name;

    /** 角色描述 */
    @TableField("`desc`")
    private String desc;

    /** 排序 */
    private Integer sort;

    /** 创建时间（时间戳） */
    private Integer createTime;

    /** 更新时间（时间戳） */
    private Integer updateTime;

    /** 删除时间（时间戳，0=未删除） */
    private Integer deleteTime;
}
