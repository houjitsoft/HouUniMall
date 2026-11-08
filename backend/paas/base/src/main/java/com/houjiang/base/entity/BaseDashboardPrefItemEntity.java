package com.houjiang.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * Dashboard 用户偏好明细表实体（每个 Widget 一行）
 *
 * @author Amos
 * @since 2026-05-13
 */
@TableName("hou_base_dashboard_pref_item")
@Data
public class BaseDashboardPrefItemEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 主表ID */
    private Long prefId;

    /** 模块标识，如 oa/payment/contract/erp */
    private String moduleId;

    /** 是否显示：1显示 0隐藏 */
    private Integer visible;

    /** 排序 */
    private Integer sort;

    /** 栅格 X */
    private Integer x;

    /** 栅格 Y */
    private Integer y;

    /** 栅格 宽度 */
    private Integer w;

    /** 栅格 高度 */
    private Integer h;
}
