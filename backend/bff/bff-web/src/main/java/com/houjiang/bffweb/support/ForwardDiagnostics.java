package com.houjiang.bffweb.support;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * 转发诊断记录（P0 502 排查抓手）。
 *
 * <p><b>为什么需要它</b>：BFF 跑在 X99（Windows）上，日志拿不到，一次 502 只能靠猜——
 * 「连接被拒」「读超时」「上游返回错误码」三种完全不同的故障，在客户端看到的都是同一个 502。
 * 没有这个抓手，每排查一次就要花几小时反推。
 *
 * <p><b>记录什么</b>：
 * <ul>
 *   <li>{@code RESOLVED}——地址解析的<b>实际结果</b>与来源（{@code config} / {@code nacos} / {@code default}）。
 *       即使成功也记，否则无法回答「它到底走了哪条路」。</li>
 *   <li>{@code FAILURE}——转发抛异常时的 URL、异常类型与消息。</li>
 * </ul>
 *
 * <p><b>刻意不做什么</b>：
 * <ul>
 *   <li><b>不落盘</b>：内存有界队列，进程重启即清空。诊断数据是排障用的瞬时快照，
 *       落盘反而要处理清理、权限、磁盘写满这些新问题。</li>
 *   <li><b>不记请求体</b>：只记 URL 与异常，请求体可能含令牌与租户数据。</li>
 *   <li><b>URL 里的敏感参数一律打码</b>：{@code token} / {@code sign} 等凭据类 query
 *       会被替换成 {@code ***}（见 {@link #redact(String)}）。诊断接口虽然受
 *       {@code X-Admin-Token} 保护，但打码是第二道防线——日志、截图、转发给别人看的时候
 *       都会带上这个 URL，不能指望每个人都能守住鉴权。</li>
 * </ul>
 *
 * @author Amos
 * @since M4 补丁（502 排查）
 */
@Component
public class ForwardDiagnostics {

    /**
     * 队列上限（有界，防止高频转发把内存吃光）。
     */
    public static final int CAPACITY = 200;

    /**
     * 默认返回的条数。
     */
    public static final int DEFAULT_LIMIT = 20;

    /**
     * 条目类型：地址解析结果（成功也记）。
     */
    public static final String KIND_RESOLVED = "RESOLVED";

    /**
     * 条目类型：转发失败。
     */
    public static final String KIND_FAILURE = "FAILURE";

    /**
     * 需要打码的 query 参数名（小写比较）。
     */
    private static final Set<String> SENSITIVE_PARAMS = Set.of(
            "token", "access_token", "accesstoken", "sign", "signature", "sig",
            "password", "pwd", "secret", "authorization", "auth", "kid", "code");

    /**
     * URL 截断长度：超长 query（Range、大列表）不值得全记。
     */
    private static final int URL_MAX = 512;

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Deque<Entry> entries = new ArrayDeque<>();

    /**
     * 记录一次地址解析结果。
     *
     * @param url    实际选中的地址
     * @param source 来源（{@code config} / {@code nacos} / {@code default}）
     */
    public synchronized void recordResolved(String url, String source) {
        append(new Entry(now(), KIND_RESOLVED, source, redact(url), null, null));
    }

    /**
     * 记录一次转发失败。
     *
     * @param url    实际请求的地址（会被打码）
     * @param source 地址来源
     * @param error  异常，可为 null
     */
    public synchronized void recordFailure(String url, String source, Throwable error) {
        String errType = error == null ? null : error.getClass().getName();
        String errMsg = error == null ? null : error.getMessage();
        append(new Entry(now(), KIND_FAILURE, source, redact(url), errType, truncate(errMsg, 300)));
    }

    /**
     * 取最近的记录（不限类型），按时间从旧到新。
     *
     * @param limit 最多返回条数，小于 1 时按 {@link #DEFAULT_LIMIT} 处理
     * @return 不可变快照
     */
    public synchronized List<Entry> recent(int limit) {
        return tail(entries, limit, null);
    }

    /**
     * 取最近的失败记录，按时间从旧到新。
     *
     * @param limit 最多返回条数，小于 1 时按 {@link #DEFAULT_LIMIT} 处理
     * @return 不可变快照
     */
    public synchronized List<Entry> recentFailures(int limit) {
        return tail(entries, limit, KIND_FAILURE);
    }

    /**
     * 取最近的地址解析记录（用来确认「到底走了哪条路」）。
     *
     * @param limit 最多返回条数，小于 1 时按 {@link #DEFAULT_LIMIT} 处理
     * @return 不可变快照
     */
    public synchronized List<Entry> recentResolved(int limit) {
        return tail(entries, limit, KIND_RESOLVED);
    }

    /**
     * 清空（排障后复位用）。
     */
    public synchronized void clear() {
        entries.clear();
    }

    /**
     * 当前队列大小（单测与自检用）。
     *
     * @return 条目数
     */
    public synchronized int size() {
        return entries.size();
    }

    private void append(Entry entry) {
        entries.addLast(entry);
        while (entries.size() > CAPACITY) {
            entries.removeFirst();
        }
    }

    private static List<Entry> tail(Deque<Entry> source, int limit, String kind) {
        int max = limit >= 1 ? limit : DEFAULT_LIMIT;
        List<Entry> matched = new ArrayList<>();
        for (Entry entry : source) {
            if (kind == null || kind.equals(entry.kind())) {
                matched.add(entry);
            }
        }
        int from = Math.max(0, matched.size() - max);
        return Collections.unmodifiableList(new ArrayList<>(matched.subList(from, matched.size())));
    }

    private static String now() {
        return LocalDateTime.now().format(TS);
    }

    /**
     * 给 URL 里的敏感 query 参数打码，并截断超长 URL。
     *
     * <p>打码而不是整个抹掉 query：排障时「哪个插件、哪个版本」正是最需要的信息，
     * 全抹了等于把抓手又变成黑盒。只替换凭据类参数的值，参数名保留，
     * 同时保留一个 {@code ***} 让人一眼看出这里被处理过。
     *
     * @param url 原始 URL，可为 null
     * @return 打码后的 URL
     */
    public static String redact(String url) {
        if (url == null || url.isBlank()) {
            return url;
        }
        String result = url.length() > URL_MAX ? url.substring(0, URL_MAX) + "..." : url;
        int q = result.indexOf('?');
        if (q < 0) {
            return result;
        }
        String base = result.substring(0, q + 1);
        String[] pairs = result.substring(q + 1).split("&", -1);
        StringBuilder out = new StringBuilder(base);
        for (int i = 0; i < pairs.length; i++) {
            if (i > 0) {
                out.append('&');
            }
            String pair = pairs[i];
            int eq = pair.indexOf('=');
            if (eq < 0) {
                out.append(pair);
                continue;
            }
            String name = pair.substring(0, eq);
            if (SENSITIVE_PARAMS.contains(name.toLowerCase(Locale.ROOT))) {
                out.append(name).append("=***");
            } else {
                out.append(pair);
            }
        }
        return out.toString();
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() > max ? value.substring(0, max) + "..." : value;
    }

    /**
     * 一条诊断记录。
     *
     * @param at      发生时间（{@code yyyy-MM-dd HH:mm:ss}）
     * @param kind    {@link #KIND_RESOLVED} 或 {@link #KIND_FAILURE}
     * @param source  地址来源，失败条目也有（来自当次解析）
     * @param url     已打码的 URL
     * @param errType 异常类名，仅失败条目有
     * @param errMsg  异常消息，仅失败条目有
     */
    public record Entry(String at, String kind, String source, String url, String errType, String errMsg) {
    }
}
