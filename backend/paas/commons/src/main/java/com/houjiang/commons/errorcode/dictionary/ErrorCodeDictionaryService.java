package com.houjiang.commons.errorcode.dictionary;

import com.houjiang.commons.errorcode.ErrorCategory;
import com.houjiang.commons.errorcode.ErrorCodeDef;
import com.houjiang.commons.errorcode.ErrorCodeDictionaryItem;
import com.houjiang.commons.errorcode.ErrorCodeFrontendItem;
import com.houjiang.commons.errorcode.ErrorCodes;
import com.houjiang.commons.errorcode.ErrorDomain;
import com.houjiang.commons.errorcode.ErrorLevel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 错误码字典服务（筛选 / 导出 / 前端子集）。
 *
 * <p>解决的问题（PRD P0-4）：码表必须可枚举、可导出，且导出的字段与运行时枚举一致 —— 单一真源。
 *
 * <p><b>为什么放在 errorcode 模块而不是 commons</b>：架构设计 §6.1 把本类放在
 * {@code paas-commons}，但 bff-web <b>不依赖 commons</b>（pom 里没有，且刻意不引以避免拖进
 * MyBatis/Redis/Seata/RocketMQ 等一堆传递依赖），而字典接口 {@code /web/system/error-codes}
 * 又必须在 bff-web 暴露。因此本类下沉到零依赖的 errorcode 模块，由 commons 与 bff-web 共同使用。
 *
 * <p>筛选项：code / domain / level / category / status / q（名称或文案模糊）。
 *
 * @author Amos
 * @since v1.2.0
 */
public final class ErrorCodeDictionaryService {

    /** 字典版本号，随枚举结构变更递增。 */
    public static final String VERSION = "v1.2.0";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ErrorCodeDictionaryService() {
        // 工具类禁止实例化
    }

    /**
     * 按条件筛选错误码（全量字段）。
     *
     * @param code     精确码值，&lt;=0 表示不过滤
     * @param domain   域代号（如 SYSTEM），null/空表示不过滤
     * @param level    级别名（如 ERROR），null/空表示不过滤
     * @param category 类别名（如 AUTH），null/空表示不过滤
     * @param status   状态名（如 ACTIVE），null/空表示不过滤
     * @param q        名称或用户文案模糊匹配，null/空表示不过滤
     * @return 字典项列表
     */
    public static List<ErrorCodeDictionaryItem> list(int code, String domain, String level,
                                                     String category, String status, String q) {
        List<ErrorCodeDef> source;
        if (code > 0) {
            ErrorCodeDef def = ErrorCodes.of(code);
            source = def == null ? new ArrayList<>() : List.of(def);
        } else if (domain != null && !domain.isBlank()) {
            source = ErrorCodes.byDomain(ErrorDomain.ofId(parseDomainId(domain)));
        } else if (level != null && !level.isBlank()) {
            source = ErrorCodes.byLevel(ErrorLevel.valueOf(level.trim().toUpperCase(Locale.ROOT)));
        } else if (category != null && !category.isBlank()) {
            source = ErrorCodes.byCategory(ErrorCategory.valueOf(category.trim().toUpperCase(Locale.ROOT)));
        } else {
            source = ErrorCodes.all();
        }

        String statusFilter = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        String keyword = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);

        List<ErrorCodeDictionaryItem> items = new ArrayList<>();
        for (ErrorCodeDef def : source) {
            if (!statusFilter.isEmpty() && !def.meta().status().name().equals(statusFilter)) {
                continue;
            }
            if (!keyword.isEmpty()) {
                String haystack = (def.name() + " " + def.meta().userMessage()).toLowerCase(Locale.ROOT);
                if (!haystack.contains(keyword)) {
                    continue;
                }
            }
            items.add(ErrorCodeDictionaryItem.from(def));
        }
        return items;
    }

    /**
     * 把域代号（或域号字符串）解析成域号。
     *
     * @param domain 域代号如 SYSTEM，或数字如 20
     * @return 域号
     */
    private static int parseDomainId(String domain) {
        String value = domain.trim();
        if (value.matches("\\d+")) {
            return Integer.parseInt(value);
        }
        for (ErrorDomain item : ErrorDomain.values()) {
            if (item.code().equalsIgnoreCase(value) || item.name().equalsIgnoreCase(value)) {
                return item.domainId();
            }
        }
        throw new IllegalArgumentException("未知的域代号：" + domain);
    }

    /**
     * 裁剪为前端子集（不含 devMessage / troubleshooting / ownerModule）。
     *
     * @param items 全量字典项
     * @return 前端项列表
     */
    public static List<ErrorCodeFrontendItem> toFrontend(List<ErrorCodeDictionaryItem> items) {
        List<ErrorCodeFrontendItem> result = new ArrayList<>();
        for (ErrorCodeDictionaryItem item : items) {
            result.add(item.toFrontend());
        }
        return result;
    }

    /**
     * 组装字典接口的响应 data 节点。
     *
     * @param items   字典项
     * @param version 字典版本
     * @return data 节点（version / generatedAt / total / items）
     */
    public static Map<String, Object> wrap(List<?> items, String version) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("version", version == null || version.isBlank() ? VERSION : version);
        data.put("generatedAt", LocalDateTime.now().format(FORMATTER));
        data.put("total", items.size());
        data.put("items", items);
        return data;
    }

    /**
     * 导出为 CSV（含表头）。
     *
     * @param items 字典项
     * @return CSV 文本
     */
    public static String toCsv(List<ErrorCodeDictionaryItem> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("code,name,domain,domainId,ownerModule,userMessage,devMessage,level,retryable,"
                + "uiAction,category,status,httpSemantic,exposeToUser,troubleshooting,legacyCodes,since,remark\n");
        for (ErrorCodeDictionaryItem item : items) {
            sb.append(item.getCode()).append(',')
                    .append(escape(item.getName())).append(',')
                    .append(escape(item.getDomain())).append(',')
                    .append(item.getDomainId()).append(',')
                    .append(escape(item.getOwnerModule())).append(',')
                    .append(escape(item.getUserMessage())).append(',')
                    .append(escape(item.getDevMessage())).append(',')
                    .append(escape(item.getLevel())).append(',')
                    .append(item.isRetryable()).append(',')
                    .append(escape(item.getUiAction())).append(',')
                    .append(escape(item.getCategory())).append(',')
                    .append(escape(item.getStatus())).append(',')
                    .append(escape(item.getHttpSemantic())).append(',')
                    .append(item.isExposeToUser()).append(',')
                    .append(escape(item.getTroubleshooting())).append(',')
                    .append(escape(joinLegacy(item.getLegacyCodes()))).append(',')
                    .append(escape(item.getSince())).append(',')
                    .append(escape(item.getRemark()))
                    .append('\n');
        }
        return sb.toString();
    }

    /**
     * CSV 字段转义：含逗号/引号/换行时用双引号包裹，内部引号翻倍。
     *
     * @param value 原始值
     * @return 转义后的值
     */
    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        String text = value.replace("\r", " ").replace("\n", " ");
        if (text.contains(",") || text.contains("\"") || text.contains(";")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    /**
     * @param codes 旧码数组
     * @return 以分号连接的字符串
     */
    private static String joinLegacy(int[] codes) {
        if (codes == null || codes.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < codes.length; i++) {
            if (i > 0) {
                sb.append(';');
            }
            sb.append(codes[i]);
        }
        return sb.toString();
    }
}
