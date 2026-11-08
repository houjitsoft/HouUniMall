package com.houjiang.plugin.publish.state;

import lombok.Getter;

/**
 * 插件商品状态（{@code hou_market_plugin.status}）。
 *
 * <p>只声明本方案用到的两个取值：0=draft、2=published。
 * published 必须是 2，因为现有 {@code PluginMarketService.STATUS_PUBLISHED} 已固化为 2。
 * 现有实体注释里的 1=审核中 / 3=已下架 / 4=已拒绝 属于市场侧历史语义，
 * 本方案的上下架统一落在<b>版本状态</b>上，商品侧只关心「是否已上架」。
 *
 * @author Amos
 * @since M1
 */
@Getter
public enum PluginProductStatus {

    /**
     * 草稿：还没有任何已发布版本。
     */
    DRAFT(0, "草稿"),

    /**
     * 已上架：至少有一个 published 版本，可在市场列表出现。
     */
    PUBLISHED(2, "已上架");

    /**
     * 入库值。
     */
    private final int code;

    /**
     * 中文描述。
     */
    private final String description;

    PluginProductStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 按入库值解析枚举，未知值按草稿处理（兼容历史脏数据，不做硬失败）。
     *
     * @param code 入库值
     * @return 枚举实例，未知时为 {@link #DRAFT}
     */
    public static PluginProductStatus fromCode(Integer code) {
        if (code == null) {
            return DRAFT;
        }
        for (PluginProductStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return DRAFT;
    }
}
