package com.houjiang.bffweb.service.fileimport;

import com.houjiang.bffweb.feign.PaymentServiceFeign;
import com.houjiang.bffweb.service.FileImportAdapterResult;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component
public class PaymentFileImportAdapter implements FileImportBusinessAdapter {
    private static final Set<String> KEYS = Set.of("payment.merchantQual", "payment.riskBlacklist");
    private static final Set<String> MERCHANT_FIELDS = Set.of(
            "terminal", "channel", "qualName", "env", "appId", "mchId",
            "notifyUrl", "returnUrl", "remark");
    private static final Set<String> TERMINALS = Set.of(
            "WX_MINI", "ALI_MINI", "BD_MINI", "TT_MINI", "QQ_MINI", "360_MINI", "KS_MINI",
            "LARK_MINI", "JD_MINI", "XHS_MINI", "APP", "HW", "QUICKAPP", "HARMONY", "WAP", "PC");
    private static final Set<String> CHANNELS = Set.of(
            "WECHAT", "ALIPAY", "UNIONPAY", "APPLE", "GOOGLE", "PAYPAL", "STRIPE", "HUAWEI");

    private final PaymentServiceFeign payment;

    public PaymentFileImportAdapter(PaymentServiceFeign payment) {
        this.payment = payment;
    }

    @Override
    public Set<String> keys() {
        return KEYS;
    }

    @Override
    public FileImportAdapterResult importItem(String key, Map<String, Object> fields,
                                              Map<String, Object> attachment) {
        if (!KEYS.contains(key)) return FileImportAdapterResult.fail(422, "不支持的支付智能入库对象");
        if ("payment.riskBlacklist".equals(key)) {
            // The current payment add API unconditionally writes status=1, so it is unsafe for AI import.
            return FileImportAdapterResult.fail(422, "该对象需要人工在支付中心新增");
        }
        Map<String, Object> source = fields == null ? Map.of() : fields;
        Map<String, Object> safe = new LinkedHashMap<>();
        for (String name : MERCHANT_FIELDS) {
            if (source.containsKey(name)) safe.put(name, source.get(name));
        }
        safe.putIfAbsent("env", "S");
        try {
            safe.put("terminal", enumValue(safe.get("terminal"), TERMINALS, "终端"));
            safe.put("channel", enumValue(safe.get("channel"), CHANNELS, "支付渠道"));
            safe.put("env", enumValue(safe.get("env"), Set.of("S", "P"), "环境"));
            if (safe.get("qualName") == null || String.valueOf(safe.get("qualName")).isBlank()) {
                throw new IllegalArgumentException("资质名称不能为空");
            }
        } catch (IllegalArgumentException ex) {
            return FileImportAdapterResult.fail(422, ex.getMessage());
        }
        safe.put("status", 0);
        ResponseResult<?> response = payment.merchantQualImport(safe);
        if (response == null) return FileImportAdapterResult.fail(503, "支付服务未返回结果");
        if (response.getCode() != 1) return FileImportAdapterResult.fail(response.getCode(), response.getMessage());
        String recordId = recordId(response.getData());
        if (recordId == null) return FileImportAdapterResult.fail(502, "支付服务成功响应缺少业务记录标识");
        return new FileImportAdapterResult(1, response.getMessage(), recordId,
                "/payment/merchant-qual", fileUrl(attachment));
    }

    private String recordId(Object data) {
        if (data instanceof Map<?, ?> values && values.get("id") != null) {
            return String.valueOf(values.get("id"));
        }
        if (data instanceof Number || data instanceof CharSequence && !String.valueOf(data).isBlank()) {
            return String.valueOf(data);
        }
        return null;
    }

    private String fileUrl(Map<String, Object> attachment) {
        Object value = attachment == null ? null : attachment.get("fileUrl");
        return value == null ? "" : String.valueOf(value);
    }

    private String enumValue(Object value, Set<String> allowed, String label) {
        String normalized = value == null ? "" : String.valueOf(value).trim().toUpperCase(java.util.Locale.ROOT);
        if (!allowed.contains(normalized)) throw new IllegalArgumentException(label + "不在允许范围内");
        return normalized;
    }
}
