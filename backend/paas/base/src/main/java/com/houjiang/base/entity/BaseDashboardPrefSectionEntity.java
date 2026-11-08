package com.houjiang.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * Dashboard 用户偏好「页面区块」表实体（每个区块一行）
 *
 * <p>2026-09-21：首页六大区块（hero / stats / quick / todo / trend / widgets）
 * 的显隐、顺序与各自配置原先写死在前端，现在落到这张表里，用户改完能跨设备记住。
 *
 * @author Amos
 * @since 2026-09-21
 */
@TableName("hou_base_dashboard_pref_section")
@Data
public class BaseDashboardPrefSectionEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 主表ID */
    private Long prefId;

    /** 区块标识，如 hero/stats/quick/todo/trend/widgets */
    private String sectionId;

    /** 是否显示：1显示 0隐藏 */
    private Integer visible;

    /** 排序 */
    private Integer sort;

    /** 区块私有配置 JSON，如 {"cards":["order","revenueToday"]} */
    private String configJson;
}
