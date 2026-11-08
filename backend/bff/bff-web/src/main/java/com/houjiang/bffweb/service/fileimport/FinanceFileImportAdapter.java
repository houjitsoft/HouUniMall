package com.houjiang.bffweb.service.fileimport;

import com.houjiang.bffweb.feign.FinanceServiceFeign;
import com.houjiang.bffweb.service.FileImportAdapterResult;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class FinanceFileImportAdapter implements FileImportBusinessAdapter {
    private static final Set<String> KEYS = Set.of(
            "finance.income", "finance.spending", "finance.invoice");
    private static final Set<String> BOOKKEEPING_FIELDS = Set.of(
            "amount", "datatime", "subjectsId", "typeId", "desc", "contractId",
            "projectId", "supplierId", "proofimage");
    private static final Set<String> INVOICE_FIELDS = Set.of(
            "direction", "invoiceNo", "invoiceType", "title", "taxNo", "amount", "taxAmount",
            "issueDate", "customerName", "remark");
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");

    private final FinanceServiceFeign finance;

    public FinanceFileImportAdapter(FinanceServiceFeign finance) {
        this.finance = finance;
    }

    @Override
    public Set<String> keys() {
        return KEYS;
    }

    @Override
    public FileImportAdapterResult importItem(String key, Map<String, Object> fields,
                                              Map<String, Object> attachment) {
        if (!KEYS.contains(key)) return FileImportAdapterResult.fail(422, "不支持的财务智能入库对象");
        Map<String, Object> source = fields == null ? Map.of() : fields;
        Map<String, Object> safe;
        try {
            safe = "finance.invoice".equals(key) ? invoicePayload(source) : bookkeepingPayload(source, attachment);
        } catch (IllegalArgumentException ex) {
            return FileImportAdapterResult.fail(422, ex.getMessage());
        }
        ResponseResult<?> response = switch (key) {
            case "finance.income" -> finance.importIncome(safe);
            case "finance.spending" -> finance.importSpending(safe);
            case "finance.invoice" -> finance.createInvoice(safe);
            default -> null;
        };
        if (response == null) return FileImportAdapterResult.fail(503, "财务服务未返回结果");
        if (response.getCode() != 1) return FileImportAdapterResult.fail(response.getCode(), response.getMessage());
        String recordId = extractId(response.getData());
        if (recordId == null) return FileImportAdapterResult.fail(502, "财务服务成功响应缺少业务记录标识");
        return new FileImportAdapterResult(1, response.getMessage(), recordId, detailPath(key), fileUrl(attachment));
    }

    private Map<String, Object> bookkeepingPayload(Map<String, Object> source, Map<String, Object> attachment) {
        Map<String, Object> result = filter(source, BOOKKEEPING_FIELDS);
        result.put("amount", decimal(result.get("amount"), "金额", true));
        if (result.containsKey("datatime")) result.put("datatime", epochSeconds(result.get("datatime")));
        String originalFile = fileUrl(attachment);
        if (!originalFile.isBlank()) result.put("proofimage", originalFile);
        result.put("status", "unaudited");
        return result;
    }

    private Map<String, Object> invoicePayload(Map<String, Object> source) {
        Map<String, Object> allowed = filter(source, INVOICE_FIELDS);
        Map<String, Object> result = new LinkedHashMap<>();
        String direction = text(allowed.get("direction")).toLowerCase(java.util.Locale.ROOT);
        if (!Set.of("input", "output").contains(direction)) {
            throw new IllegalArgumentException("发票方向必须为 input 或 output");
        }
        result.put("direction", direction);
        copy(allowed, result, "invoiceNo", "remark");
        result.put("amount", decimal(allowed.get("amount"), "含税金额", true));
        if (allowed.containsKey("taxAmount")) {
            result.put("taxAmount", decimal(allowed.get("taxAmount"), "税额", false));
        }
        Object invoiceType = integer(allowed.get("invoiceType"));
        if (invoiceType != null) result.put("invoiceType", invoiceType);
        Object title = allowed.get("title");
        result.put("customerName", present(title) ? title : allowed.get("customerName"));
        if (allowed.containsKey("issueDate")) result.put("invoiceDate", date(allowed.get("issueDate")));
        if (present(allowed.get("taxNo"))) result.put("invoiceContent", "税号：" + allowed.get("taxNo"));
        // FinanceInvoice.status is integer: 0 pending, 1 issued, 2 cancelled.
        result.put("status", 0);
        return result;
    }

    private Map<String, Object> filter(Map<String, Object> source, Set<String> allowed) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String field : allowed) {
            if (source.containsKey(field)) result.put(field, source.get(field));
        }
        return result;
    }

    private void copy(Map<String, Object> source, Map<String, Object> target, String... names) {
        for (String name : names) if (source.containsKey(name)) target.put(name, source.get(name));
    }

    private Integer epochSeconds(Object value) {
        if (value instanceof Number number) return checkedEpoch(number.longValue());
        String text = value == null ? "" : String.valueOf(value).trim();
        if (text.isEmpty()) throw new IllegalArgumentException("发生日期不能为空");
        try {
            return checkedEpoch(Long.parseLong(text));
        } catch (NumberFormatException ignored) {
            // Continue with the catalog's ISO date/datetime forms.
        }
        try {
            return checkedEpoch(LocalDate.parse(text).atStartOfDay(BUSINESS_ZONE).toEpochSecond());
        } catch (DateTimeParseException ignored) {
            try {
                return checkedEpoch(LocalDateTime.parse(text).atZone(BUSINESS_ZONE).toEpochSecond());
            } catch (DateTimeParseException ignoredAgain) {
                try {
                    return checkedEpoch(Instant.parse(text).getEpochSecond());
                } catch (DateTimeParseException ex) {
                    throw new IllegalArgumentException("发生日期格式不正确");
                }
            }
        }
    }

    private Integer integer(Object value) {
        if (value == null || String.valueOf(value).isBlank()) return null;
        try {
            BigDecimal parsed = new BigDecimal(String.valueOf(value).trim()).stripTrailingZeros();
            int result = parsed.intValueExact();
            if (result < 1 || result > 3) throw new ArithmeticException();
            return result;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("发票类型必须为整数");
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("发票类型必须为 1、2 或 3");
        }
    }

    private Integer checkedEpoch(long value) {
        long seconds = Math.abs(value) >= 10_000_000_000L ? value / 1000L : value;
        try {
            return Math.toIntExact(seconds);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("发生日期超出允许范围");
        }
    }

    private BigDecimal decimal(Object value, String label, boolean required) {
        if (!present(value)) {
            if (required) throw new IllegalArgumentException(label + "不能为空");
            return null;
        }
        try {
            BigDecimal parsed = new BigDecimal(String.valueOf(value).trim());
            if (parsed.signum() < 0) throw new IllegalArgumentException(label + "不能为负数");
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + "格式不正确");
        }
    }

    private LocalDate date(Object value) {
        if (!present(value)) throw new IllegalArgumentException("开票日期不能为空");
        try {
            return LocalDate.parse(String.valueOf(value).trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("开票日期格式不正确");
        }
    }

    private String extractId(Object data) {
        if (data instanceof Map<?, ?> values) {
            for (String name : List.of("id", "invoiceId")) {
                Object value = values.get(name);
                if (present(value)) return String.valueOf(value);
            }
        }
        if (data instanceof Number || data instanceof CharSequence && present(data)) return String.valueOf(data);
        return null;
    }

    private String detailPath(String key) {
        return switch (key) {
            case "finance.income" -> "/finance/income";
            case "finance.spending" -> "/finance/spending";
            case "finance.invoice" -> "/finance/invoice";
            default -> "/finance";
        };
    }

    private String fileUrl(Map<String, Object> attachment) {
        Object value = attachment == null ? null : attachment.get("fileUrl");
        return value == null ? "" : String.valueOf(value);
    }

    private boolean present(Object value) {
        return value != null && !String.valueOf(value).isBlank();
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
