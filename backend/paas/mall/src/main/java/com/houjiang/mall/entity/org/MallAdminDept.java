package com.houjiang.mall.entity.org;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
@TableName("hou_mall_admin_dept")
public class MallAdminDept implements Serializable {

    private Integer adminId;

    private Integer deptId;
}
