package com.houjiang.mall.entity.auth;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;

/**
 * Mall 角色菜单关联实体类
 * 对应表: hou_mall_system_role_menu
 *
 * @author Amos
 */
@Data
@TableName("hou_mall_system_role_menu")
public class MallSystemRoleMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 角色ID */
    private Integer roleId;

    /** 菜单ID */
    private Integer menuId;
}
