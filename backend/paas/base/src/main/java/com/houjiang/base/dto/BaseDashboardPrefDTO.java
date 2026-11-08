package com.houjiang.base.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Dashboard 用户偏好保存/查询 DTO
 *
 * @author Amos
 * @since 2026-05-13
 */
@Data
public class BaseDashboardPrefDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 栅格列数 */
    private Integer cols;

    /** 自动刷新间隔(秒)，0 表示关闭 */
    private Integer refreshInterval;

    /** Widget 明细列表 */
    private List<BaseDashboardPrefItemDTO> items;

    /**
     * 页面区块列表（2026-09-21 新增）
     * 老客户端不传该字段时后端按「不变更」处理，避免把已有区块配置冲掉
     */
    private List<BaseDashboardPrefSectionDTO> sections;
}
