package com.houjiang.base.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * Dashboard 用户偏好「页面区块」DTO
 *
 * <p>config 是区块私有配置（各区块字段不同），这里不做强类型约束，
 * 原样存取 JSON，合法性由前端 sanitize 兜底，后端只负责存得住、取得出。
 *
 * @author Amos
 * @since 2026-09-21
 */
@Data
public class BaseDashboardPrefSectionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 区块标识，如 hero/stats/quick/todo/trend/widgets */
    private String id;

    /** 是否显示：1显示 0隐藏 */
    private Integer visible;

    /** 排序 */
    private Integer sort;

    /** 区块私有配置，如 {"cards":["order"]} / {"days":30} */
    private Map<String, Object> config;
}
