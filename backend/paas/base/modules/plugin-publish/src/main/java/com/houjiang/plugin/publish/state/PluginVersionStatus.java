package com.houjiang.plugin.publish.state;

import lombok.Getter;

/**
 * 插件版本状态（{@code hou_market_plugin_version.status}）。
 *
 * <p>取值严格沿用方案 §3.2 与现有代码固化语义，<b>不另起枚举值</b>：
 * published 必须是 1，因为现有 {@code PluginMarketService.VERSION_APPROVED} 只认 1，
 * 一旦改成其它值，已上架版本会从这个市场的查询里整体消失。
 *
 * @author Amos
 * @since M1
 */
@Getter
public enum PluginVersionStatus {

    /**
     * 草稿：已上传制品，未提交审核。
     */
    DRAFT(0, "草稿"),

    /**
     * 已发布：审核通过，可下载安装。
     */
    PUBLISHED(1, "已发布"),

    /**
     * 待审核：已提交，等平台运营审核。
     */
    PENDING(3, "待审核"),

    /**
     * 审核驳回：可修改后重新提交（同一 version 不再接受新制品）。
     */
    REJECTED(4, "已驳回"),

    /**
     * 已下架：曾发布过，被运营/发布者下架。
     */
    OFFLINE(5, "已下架");

    /**
     * 入库值。
     */
    private final int code;

    /**
     * 中文描述。
     */
    private final String description;

    PluginVersionStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 按入库值解析枚举。
     *
     * @param code 入库值
     * @return 枚举实例
     * @throws IllegalArgumentException 未知取值时抛出
     */
    public static PluginVersionStatus fromCode(Integer code) {
        if (code == null) {
            throw new IllegalArgumentException("版本状态不能为空");
        }
        for (PluginVersionStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的插件版本状态：" + code);
    }

    /**
     * @return 是否为「不可删除、不可被同 code+version 覆盖」的终态
     *         （published / offline：已安装租户必须能永远回滚到这些版本）
     */
    public boolean isImmutable() {
        return this == PUBLISHED || this == OFFLINE;
    }
}
