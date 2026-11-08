package com.houjiang.logging.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ELK（Elasticsearch）日志查询配置。
 *
 * <p>查询侧性能约束：强制时间范围、限制分页深度与单页大小、设置 ES 查询超时。</p>
 * @author Amos
 */
@Data
@Component
@ConfigurationProperties(prefix = "houcloud.log.elk")
public class ElkLogProperties {

    /** 是否启用 ELK 查询（关闭时系统/运行错误页仅展示审计库数据） */
    private boolean enabled = true;

    /** ES 地址，如 http://127.0.0.1:9200 */
    private String uris = "http://127.0.0.1:9200";

    private String username = "elastic";

    private String password = "";

    /** 逗号分隔的 index pattern，如 filebeat-*,logs-* */
    private String indexPatterns = "filebeat-*,logs-*,logstash-*,*-logs-*";

    /** 允许的最大查询时间跨度（天） */
    private int maxRangeDays = 7;

    /** 未传时间范围时的默认查询跨度（小时） */
    private int defaultRangeHours = 24;

    /** 单页最大条数 */
    private int maxPageSize = 100;

    /** from 偏移上限，避免深分页拖垮 ES */
    private int maxFromOffset = 5000;

    /** ES 查询超时，如 5s */
    private String queryTimeout = "5s";

    /** track_total_hits 上限 */
    private int trackTotalHitsCap = 10000;
}
