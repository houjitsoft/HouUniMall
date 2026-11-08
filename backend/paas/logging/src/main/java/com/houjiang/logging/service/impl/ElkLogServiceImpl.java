package com.houjiang.logging.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.houjiang.logging.config.ElkLogProperties;
import com.houjiang.logging.dto.ElkLogQueryDTO;
import com.houjiang.logging.service.ElkLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.houjiang.commons.http.OutboundHttpClientFactory;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 通过 Elasticsearch _search API 查询运行日志。
 *
 * <p>性能策略：强制时间范围、限制 from 深度、列表不拉取 stack_trace、聚合设 size 上限。</p>
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "houcloud.log.elk.enabled", havingValue = "true", matchIfMissing = true)
public class ElkLogServiceImpl implements ElkLogService {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter ES_DT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final ElkLogProperties props;
    private final RestTemplate restTemplate = OutboundHttpClientFactory.restTemplate(5000, 5000);

    @Override
    public boolean isEnabled() {
        return props.isEnabled() && StringUtils.hasText(props.getUris());
    }

    @Override
    public Map<String, Object> queryPage(ElkLogQueryDTO dto, long current, long size) {
        Map<String, Object> empty = new LinkedHashMap<>();
        empty.put("records", List.of());
        empty.put("total", 0L);
        empty.put("current", current);
        empty.put("size", size);
        empty.put("elkEnabled", false);

        if (!isEnabled()) {
            return empty;
        }

        long pageSize = Math.min(Math.max(size, 1), props.getMaxPageSize());
        long from = (Math.max(current, 1) - 1) * pageSize;
        if (from > props.getMaxFromOffset()) {
            throw new IllegalArgumentException("分页过深，请缩小时间范围或使用关键字过滤（最大偏移 " + props.getMaxFromOffset() + "）");
        }

        TimeRange range = resolveRange(dto.getStartTime(), dto.getEndTime());
        JSONObject body = buildSearchBody(dto, range, pageSize, from);

        JSONObject result = executeSearch(body);
        if (result == null) {
            empty.put("elkEnabled", true);
            empty.put("elkError", "Elasticsearch 查询失败，请检查连接或索引配置");
            return empty;
        }

        JSONObject hits = result.getJSONObject("hits");
        long total = 0;
        if (hits != null) {
            Object totalObj = hits.get("total");
            if (totalObj instanceof JSONObject) {
                total = ((JSONObject) totalObj).getLongValue("value");
            } else if (totalObj instanceof Number) {
                total = ((Number) totalObj).longValue();
            }
        }

        List<Map<String, Object>> records = new ArrayList<>();
        JSONArray hitArr = hits != null ? hits.getJSONArray("hits") : null;
        if (hitArr != null) {
            for (int i = 0; i < hitArr.size(); i++) {
                JSONObject hit = hitArr.getJSONObject(i);
                records.add(mapHit(hit, false));
            }
        }

        Map<String, Object> page = new LinkedHashMap<>();
        page.put("records", records);
        page.put("total", total);
        page.put("current", current);
        page.put("size", pageSize);
        page.put("elkEnabled", true);
        page.put("hasMore", from + records.size() < total);
        return page;
    }

    @Override
    public Map<String, Long> levelStats(String startTime, String endTime) {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("DEBUG", 0L);
        stats.put("INFO", 0L);
        stats.put("WARN", 0L);
        stats.put("ERROR", 0L);
        stats.put("FATAL", 0L);

        if (!isEnabled()) {
            return stats;
        }

        TimeRange range = resolveRange(startTime, endTime);
        JSONObject body = new JSONObject();
        body.put("size", 0);
        body.put("timeout", props.getQueryTimeout());
        body.put("track_total_hits", false);
        body.put("query", rangeQuery(range, null, null, null, null));

        JSONObject aggs = new JSONObject();
        JSONObject levels = new JSONObject();
        levels.put("terms", new JSONObject().fluentPut("field", "log.level.keyword").fluentPut("size", 10));
        aggs.put("by_level", levels);
        JSONObject levels2 = new JSONObject();
        levels2.put("terms", new JSONObject().fluentPut("field", "level.keyword").fluentPut("size", 10));
        aggs.put("by_level2", levels2);
        body.put("aggs", aggs);

        JSONObject result = executeSearch(body);
        if (result == null) {
            return stats;
        }
        mergeAggBuckets(stats, result.getJSONObject("aggregations"), "by_level");
        mergeAggBuckets(stats, result.getJSONObject("aggregations"), "by_level2");
        return stats;
    }

    @Override
    public long countTodayErrors() {
        if (!isEnabled()) {
            return 0;
        }
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        ElkLogQueryDTO dto = new ElkLogQueryDTO();
        dto.setStartTime(start.format(DT));
        dto.setEndTime(end.format(DT));
        dto.setLevel("ERROR");
        Map<String, Object> page = queryPage(dto, 1, 1);
        Object total = page.get("total");
        return total instanceof Number ? ((Number) total).longValue() : 0;
    }

    @Override
    public long countTodayTotal() {
        if (!isEnabled()) {
            return 0;
        }
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        TimeRange range = new TimeRange(start, end);
        JSONObject body = new JSONObject();
        body.put("size", 0);
        body.put("timeout", props.getQueryTimeout());
        body.put("track_total_hits", props.getTrackTotalHitsCap());
        body.put("query", rangeQuery(range, null, null, null, null));
        JSONObject result = executeSearch(body);
        if (result == null) {
            return 0;
        }
        JSONObject hits = result.getJSONObject("hits");
        if (hits == null) {
            return 0;
        }
        Object totalObj = hits.get("total");
        if (totalObj instanceof JSONObject) {
            return ((JSONObject) totalObj).getLongValue("value");
        }
        return totalObj instanceof Number ? ((Number) totalObj).longValue() : 0;
    }

    @Override
    public List<Map<String, Object>> topRuntimeErrors(int limit, String startTime, String endTime) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (!isEnabled()) {
            return list;
        }
        int lim = Math.min(Math.max(limit, 1), 50);
        TimeRange range = resolveRange(startTime, endTime);

        JSONObject body = new JSONObject();
        body.put("size", 0);
        body.put("timeout", props.getQueryTimeout());
        body.put("track_total_hits", false);

        ElkLogQueryDTO errFilter = new ElkLogQueryDTO();
        errFilter.setLevel("ERROR");
        body.put("query", rangeQuery(range, errFilter, null, null, null));

        JSONObject aggs = new JSONObject();
        JSONObject terms = new JSONObject();
        terms.put("terms", new JSONObject()
                .fluentPut("field", "message.keyword")
                .fluentPut("size", lim)
                .fluentPut("order", new JSONObject().fluentPut("_count", "desc")));
        aggs.put("top_errors", terms);
        body.put("aggs", aggs);

        JSONObject result = executeSearch(body);
        if (result == null) {
            return list;
        }
        JSONObject aggregations = result.getJSONObject("aggregations");
        if (aggregations == null) {
            return list;
        }
        JSONObject top = aggregations.getJSONObject("top_errors");
        JSONArray buckets = top != null ? top.getJSONArray("buckets") : null;
        if (buckets == null) {
            return list;
        }
        for (int i = 0; i < buckets.size(); i++) {
            JSONObject b = buckets.getJSONObject(i);
            Map<String, Object> item = new HashMap<>();
            String key = b.getString("key");
            item.put("error", key != null && key.length() > 200 ? key.substring(0, 200) + "..." : key);
            item.put("errorType", key);
            item.put("count", b.getLongValue("doc_count"));
            list.add(item);
        }
        return list;
    }

    private void mergeAggBuckets(Map<String, Long> stats, JSONObject aggs, String key) {
        if (aggs == null) {
            return;
        }
        JSONObject by = aggs.getJSONObject(key);
        JSONArray buckets = by != null ? by.getJSONArray("buckets") : null;
        if (buckets == null) {
            return;
        }
        for (int i = 0; i < buckets.size(); i++) {
            JSONObject b = buckets.getJSONObject(i);
            String lvl = b.getString("key");
            if (!StringUtils.hasText(lvl)) {
                continue;
            }
            String up = lvl.toUpperCase();
            stats.merge(up, b.getLongValue("doc_count"), Long::sum);
        }
    }

    private JSONObject buildSearchBody(ElkLogQueryDTO dto, TimeRange range, long size, long from) {
        JSONObject body = new JSONObject();
        body.put("from", from);
        body.put("size", size);
        body.put("timeout", props.getQueryTimeout());
        body.put("track_total_hits", props.getTrackTotalHitsCap());
        body.put("sort", JSONArray.parseArray("[{\"@timestamp\":{\"order\":\"desc\"}}]"));
        body.put("_source", JSONArray.parseArray(
                "[\"@timestamp\",\"message\",\"log.level\",\"level\",\"logger_name\",\"log.logger\","
                        + "\"thread_name\",\"service.name\",\"app\",\"application\",\"host.name\","
                        + "\"error.message\",\"error.type\"]"));
        body.put("query", rangeQuery(range, dto, dto.getLevel(), dto.getService(), dto.getKeyword()));
        return body;
    }

    private JSONObject rangeQuery(TimeRange range, ElkLogQueryDTO dto, String level,
                                  String service, String keyword) {
        JSONArray filter = new JSONArray();
        JSONObject rangeQ = new JSONObject();
        JSONObject ts = new JSONObject();
        ts.put("gte", toEsTime(range.start));
        ts.put("lte", toEsTime(range.end));
        rangeQ.put("range", new JSONObject().fluentPut("@timestamp", ts));
        filter.add(rangeQ);

        if (StringUtils.hasText(level)) {
            JSONArray should = new JSONArray();
            String lv = level.toUpperCase();
            should.add(termQuery("log.level.keyword", lv));
            should.add(termQuery("level.keyword", lv));
            should.add(termQuery("log.level", lv));
            should.add(termQuery("level", lv));
            filter.add(new JSONObject().fluentPut("bool",
                    new JSONObject().fluentPut("should", should).fluentPut("minimum_should_match", 1)));
        }

        if (StringUtils.hasText(service)) {
            JSONArray should = new JSONArray();
            should.add(wildcardQuery("service.name.keyword", "*" + service + "*"));
            should.add(wildcardQuery("app.keyword", "*" + service + "*"));
            should.add(wildcardQuery("application.keyword", "*" + service + "*"));
            filter.add(new JSONObject().fluentPut("bool",
                    new JSONObject().fluentPut("should", should).fluentPut("minimum_should_match", 1)));
        }

        if (dto != null && StringUtils.hasText(dto.getLogger())) {
            JSONArray should = new JSONArray();
            should.add(wildcardQuery("logger_name.keyword", "*" + dto.getLogger() + "*"));
            should.add(wildcardQuery("log.logger.keyword", "*" + dto.getLogger() + "*"));
            filter.add(new JSONObject().fluentPut("bool",
                    new JSONObject().fluentPut("should", should).fluentPut("minimum_should_match", 1)));
        }

        JSONObject bool = new JSONObject();
        bool.put("filter", filter);

        if (StringUtils.hasText(keyword)) {
            JSONObject qs = new JSONObject();
            qs.put("query_string", new JSONObject()
                    .fluentPut("query", "*" + escapeQuery(keyword) + "*")
                    .fluentPut("fields", JSONArray.parseArray("[\"message\",\"error.message\",\"logger_name\",\"log.logger\"]"))
                    .fluentPut("analyze_wildcard", true)
                    .fluentPut("default_operator", "AND"));
            JSONArray must = new JSONArray();
            must.add(qs);
            bool.put("must", must);
        }

        return new JSONObject().fluentPut("bool", bool);
    }

    private static JSONObject termQuery(String field, String value) {
        return new JSONObject().fluentPut("term", new JSONObject().fluentPut(field, value));
    }

    private static JSONObject wildcardQuery(String field, String value) {
        return new JSONObject().fluentPut("wildcard", new JSONObject().fluentPut(field, value));
    }

    private static String escapeQuery(String keyword) {
        return keyword.replace("\\", "\\\\").replace("*", "\\*").replace("?", "\\?");
    }

    private Map<String, Object> mapHit(JSONObject hit, boolean includeStack) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", hit.getString("_id"));
        row.put("index", hit.getString("_index"));
        row.put("source", "elk");

        JSONObject src = hit.getJSONObject("_source");
        if (src == null) {
            return row;
        }

        row.put("timestamp", firstNonBlank(src, "@timestamp", "timestamp"));
        row.put("level", firstNonBlank(src, "log.level", "level"));
        row.put("message", truncate(firstNonBlank(src, "message", "error.message"), 500));
        row.put("logger", firstNonBlank(src, "logger_name", "log.logger"));
        row.put("thread", firstNonBlank(src, "thread_name", "thread"));
        row.put("service", firstNonBlank(src, "service.name", "app", "application"));
        row.put("hostName", firstNonBlank(src, "host.name", "host"));
        row.put("applicationName", firstNonBlank(src, "application", "app", "service.name"));
        row.put("exceptionClass", firstNonBlank(src, "error.type", "exception"));

        if (includeStack) {
            row.put("stackTrace", firstNonBlank(src, "error.stack_trace", "stack_trace"));
        }
        return row;
    }

    private static String firstNonBlank(JSONObject src, String... keys) {
        for (String k : keys) {
            String v = src.getString(k);
            if (StringUtils.hasText(v)) {
                return v;
            }
        }
        return "";
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    private JSONObject executeSearch(JSONObject body) {
        try {
            String index = props.getIndexPatterns().trim();
            String url = trimSlash(props.getUris()) + "/" + index + "/_search";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (StringUtils.hasText(props.getUsername())) {
                String auth = props.getUsername() + ":" + (props.getPassword() != null ? props.getPassword() : "");
                headers.set("Authorization", "Basic " + Base64.getEncoder()
                        .encodeToString(auth.getBytes(StandardCharsets.UTF_8)));
            }

            HttpEntity<String> entity = new HttpEntity<>(body.toJSONString(), headers);
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                log.warn("[ELK] 非 2xx 响应 status={}", resp.getStatusCode());
                return null;
            }
            return JSON.parseObject(resp.getBody());
        } catch (Exception e) {
            log.error("[ELK] 查询失败: {}", e.getMessage());
            return null;
        }
    }

    private TimeRange resolveRange(String startTime, String endTime) {
        LocalDateTime end;
        LocalDateTime start;
        if (StringUtils.hasText(startTime) && StringUtils.hasText(endTime)) {
            start = LocalDateTime.parse(startTime, DT);
            end = LocalDateTime.parse(endTime, DT);
        } else {
            end = LocalDateTime.now();
            start = end.minusHours(props.getDefaultRangeHours());
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("结束时间必须大于开始时间");
        }
        if (start.plusDays(props.getMaxRangeDays()).isBefore(end)) {
            throw new IllegalArgumentException("查询时间范围不能超过 " + props.getMaxRangeDays() + " 天");
        }
        return new TimeRange(start, end);
    }

    private static String toEsTime(LocalDateTime time) {
        return time.atZone(ZoneId.systemDefault()).toInstant().toString();
    }

    private static String trimSlash(String uris) {
        if (uris == null) {
            return "";
        }
        return uris.endsWith("/") ? uris.substring(0, uris.length() - 1) : uris;
    }

    private record TimeRange(LocalDateTime start, LocalDateTime end) {
    }
}
