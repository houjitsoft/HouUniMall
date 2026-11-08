package com.houjiang.mall.entity.auth;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * Mall 管理员-角色关联
 * @author Amos
 */
@Data
@TableName("hou_mall_admin_role")
public class MallAdminRole implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer adminId;

    private Integer roleId;
}
