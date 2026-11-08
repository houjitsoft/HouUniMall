package com.houjiang.base.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Dashboard 用户偏好查询 VO
 *
 * @author Amos
 * @since 2026-05-13
 */
@Data
public class BaseDashboardPrefVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 用户ID */
    private String userId;

    /** 栅格列数 */
    private Integer cols;

    /** 自动刷新间隔(秒)，0 关闭 */
    private Integer refreshInterval;

    /** 版本号 */
    private Integer version;

    /** 明细 */
    private List<Item> items;

    /**
     * 页面区块（2026-09-21 新增）
     * 老数据没有记录时返回空数组，前端按注册表补默认值
     */
    private List<Section> sections;

    @Data
    public static class Section implements Serializable {
        private static final long serialVersionUID = 1L;
        private String id;
        private Integer visible;
        private Integer sort;
        /** 区块私有配置对象，由 configJson 反序列化而来 */
        private Map<String, Object> config;
    }

    @Data
    public static class Item implements Serializable {
        private static final long serialVersionUID = 1L;
        private String moduleId;
        private Integer visible;
        private Integer sort;
        private Integer x;
        private Integer y;
        private Integer w;
        private Integer h;
    }
}
