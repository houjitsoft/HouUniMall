package com.houjiang.mall.model.market;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 目录页「公开基线」快照（M5 缓存对象）。
 *
 * <p><b>为什么单独建这个类</b>：缓存里只能放<b>与租户无关</b>的公开数据——
 * 安装态（{@code hou_addon_plugin_install}）与授权（{@code hou_market_plugin_license}）
 * 必须按 {@code instanceId} 现算，一旦进缓存，缓存规模会随「站点数 × 插件数 × 分页」指数膨胀，
 * 而且任何一次安装 / 购买都要失效全站缓存。
 *
 * <p>因此列表缓存只装「商品元数据 + 分页计数」，请求进来后再叠加租户维度字段。
 *
 * @author Amos
 * @since M5
 */
@Data
public class CatalogBaseline {

    /**
     * 公开基线列表（租户字段为空，由调用方叠加）。
     */
    private List<MarketPluginItem> items = new ArrayList<>();

    /**
     * 总条数。
     */
    private long total;

    /**
     * 总页数。
     */
    private long pages;
}
