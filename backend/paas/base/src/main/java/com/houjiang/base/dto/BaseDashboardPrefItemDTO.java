package com.houjiang.base.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Dashboard 用户偏好明细 DTO
 *
 * @author Amos
 * @since 2026-05-13
 */
@Data
public class BaseDashboardPrefItemDTO implements Serializable {
    private static final long serialVersionUID = 1L;

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

    /** 栅格宽度 */
    private Integer w;

    /** 栅格高度 */
    private Integer h;
}
