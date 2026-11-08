package com.houjiang.bffweb.service;

import com.houjiang.bffweb.feign.CrmServiceFeign;
import com.houjiang.bffweb.feign.MallServiceFeign;
import com.houjiang.bffweb.feign.MesServiceFeign;
import com.houjiang.bffweb.feign.PmServiceFeign;
import com.houjiang.bffweb.service.fileimport.FileImportBusinessAdapter;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class FileImportAdapterRegistry {
    private static final Map<String, Set<String>> ALLOWED_FIELDS = Map.ofEntries(
            Map.entry("mall.goods", Set.of("goodsName", "catId", "goodsPrice", "goodsMarketPrice", "goodsCode", "goodsImage", "goodsCostPrice", "dw", "moq")),
            Map.entry("mall.customer", Set.of("name", "phone", "email", "province", "city", "district", "address", "source", "remark")),
            Map.entry("crm.customer", Set.of("name", "mobile", "telephone", "detailAddress", "remark")),
            Map.entry("crm.business", Set.of("name", "customerId", "money", "dealTime", "remark")),
            Map.entry("mes.material", Set.of("materialCode", "materialName", "specification", "unit", "materialType", "purchasePrice", "stockQuantity", "remark")),
            Map.entry("mes.production", Set.of("planCode", "productionBatch", "goodsName", "goodsCode", "number", "deliveryTime", "unitPrice", "allMoney", "remark")),
            Map.entry("pm.project", Set.of("number", "name", "category", "priority", "customerName", "price", "content", "remark")),
            Map.entry("pm.task", Set.of("taskNo", "name", "projectId", "content", "progress", "status", "level", "startTime", "endTime")),
            Map.entry("erp.goods", Set.of("goodsCode", "goodsName", "goodsBarcode", "goodsSpecificationId", "marketSellPrice", "wholesalePrice", "purchase", "goodsImage", "content", "inventory", "note")),
            Map.entry("erp.purchase", Set.of("purchaseCode", "purchaseTime", "suppliersId", "money", "realMoney", "amount", "remark"))
    );
    private static final Map<String, String> DETAIL_PATHS = Map.ofEntries(
            Map.entry("mall.goods", "/mall/goods/detail/"), Map.entry("mall.customer", "/mall/customer/detail/"),
            Map.entry("crm.customer", "/crm/customer/detail/"), Map.entry("crm.business", "/crm/business/detail/"),
            Map.entry("mes.material", "/mes/material/detail/"), Map.entry("mes.production", "/mes/production/detail/"),
            Map.entry("pm.project", "/pm/project/detail/"), Map.entry("pm.task", "/pm/task/detail/"),
            Map.entry("erp.goods", "/erp/goods/detail/"), Map.entry("erp.purchase", "/erp/purchase/detail/")
    );

    private final MallServiceFeign mallServiceFeign;
    private final CrmServiceFeign crmServiceFeign;
    private final MesServiceFeign mesServiceFeign;
    private final PmServiceFeign pmServiceFeign;
    private final PluginApiClient pluginApiClient;
    private final Map<String, FileImportBusinessAdapter> businessAdapters;

    /** Kept for focused legacy tests and callers that only need the original ten adapters. */
    public FileImportAdapterRegistry(MallServiceFeign mallServiceFeign, CrmServiceFeign crmServiceFeign,
                                     MesServiceFeign mesServiceFeign, PmServiceFeign pmServiceFeign,
                                     PluginApiClient pluginApiClient) {
        this(mallServiceFeign, crmServiceFeign, mesServiceFeign, pmServiceFeign, pluginApiClient, List.of());
    }

    @Autowired
    public FileImportAdapterRegistry(MallServiceFeign mallServiceFeign, CrmServiceFeign crmServiceFeign,
                                     MesServiceFeign mesServiceFeign, PmServiceFeign pmServiceFeign,
                                     PluginApiClient pluginApiClient,
                                     List<FileImportBusinessAdapter> adapters) {
        this.mallServiceFeign = mallServiceFeign;
        this.crmServiceFeign = crmServiceFeign;
        this.mesServiceFeign = mesServiceFeign;
        this.pmServiceFeign = pmServiceFeign;
        this.pluginApiClient = pluginApiClient;
        this.businessAdapters = register(adapters);
    }

    public Set<String> supportedKeys() {
        Set<String> keys = new java.util.LinkedHashSet<>(ALLOWED_FIELDS.keySet());
        keys.addAll(businessAdapters.keySet());
        return Set.copyOf(keys);
    }

    public FileImportAdapterResult importItem(Map<String, Object> item, Map<String, Object> attachment) {
        String key = text(item == null ? null : item.get("adapterKey"));
        FileImportBusinessAdapter businessAdapter = businessAdapters.get(key);
        if (businessAdapter != null) {
            return businessAdapter.importItem(key, map(item.get("fields")), map(attachment));
        }
        if (!ALLOWED_FIELDS.containsKey(key)) {
            return FileImportAdapterResult.fail(422, "不支持的智能入库对象");
        }
        Map<String, Object> fields = filterFields(key, map(item.get("fields")));
        attachment = map(attachment);
        if ("pm.project".equals(key)) {
            fields.put("attachments", List.of(Map.of(
                    "name", text(attachment.get("fileName")),
                    "url", text(attachment.get("fileUrl")),
                    "size", attachment.getOrDefault("fileSize", 0),
                    "type", text(attachment.get("contentType")))));
        }
        ResponseResult response = invoke(key, fields);
        if (response == null) return FileImportAdapterResult.fail(503, "业务服务未返回结果");
        if (response.getCode() != 1) {
            return FileImportAdapterResult.fail(response.getCode(), response.getMessage());
        }
        String recordId = extractRecordId(response.getData(), fields, key);
        String detailUrl = recordId == null ? DETAIL_PATHS.get(key) : DETAIL_PATHS.get(key) + recordId;
        return new FileImportAdapterResult(1, response.getMessage(), recordId, detailUrl,
                text(attachment.get("fileUrl")));
    }

    private ResponseResult invoke(String key, Map<String, Object> fields) {
        return switch (key) {
            case "mall.goods" -> mallServiceFeign.addGoodsForImport(fields);
            case "mall.customer" -> mallServiceFeign.saveCustomer(fields);
            case "crm.customer" -> crmServiceFeign.addCustomer(fields);
            case "crm.business" -> crmServiceFeign.addBusiness(fields);
            case "mes.material" -> mesServiceFeign.createMaterial(fields);
            case "mes.production" -> mesServiceFeign.createProductionPlan(fields);
            case "pm.project" -> pmServiceFeign.createProject(fields);
            case "pm.task" -> pmServiceFeign.createTask(fields);
            case "erp.goods" -> pluginApiClient.post("erp", "/goods", fields);
            case "erp.purchase" -> pluginApiClient.post("erp", "/purchase", fields);
            default -> ResponseResult.fail(422, "不支持的智能入库对象");
        };
    }

    private Map<String, Object> filterFields(String key, Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String name : ALLOWED_FIELDS.get(key)) {
            if (source.containsKey(name)) result.put(name, source.get(name));
        }
        return result;
    }

    private String extractRecordId(Object data, Map<String, Object> fields, String key) {
        if (data instanceof Map<?, ?> values) {
            for (String name : List.of("id", "goodsId", "customerId", "businessId", "materialId",
                    "productionId", "projectId", "taskId", "purchaseId", "purchaseCode")) {
                Object value = values.get(name);
                if (value != null && !String.valueOf(value).isBlank()) return String.valueOf(value);
            }
        }
        for (String name : identityFields(key)) {
            Object value = fields.get(name);
            if (value != null && !String.valueOf(value).isBlank()) return String.valueOf(value);
        }
        return null;
    }

    private List<String> identityFields(String key) {
        return switch (key) {
            case "mall.goods", "erp.goods" -> List.of("goodsCode", "goodsName");
            case "mall.customer" -> List.of("phone", "name");
            case "crm.customer" -> List.of("mobile", "name");
            case "crm.business" -> List.of("name");
            case "mes.material" -> List.of("materialCode");
            case "mes.production" -> List.of("planCode");
            case "pm.project" -> List.of("number");
            case "pm.task" -> List.of("taskNo", "name");
            case "erp.purchase" -> List.of("purchaseCode");
            default -> new ArrayList<>();
        };
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> ? (Map<String, Object>) value : Map.of();
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private Map<String, FileImportBusinessAdapter> register(List<FileImportBusinessAdapter> adapters) {
        Map<String, FileImportBusinessAdapter> registered = new LinkedHashMap<>();
        if (adapters == null) return Map.of();
        for (FileImportBusinessAdapter adapter : adapters) {
            if (adapter == null || adapter.keys() == null) {
                throw new IllegalStateException("智能入库适配器及 keys 不能为空");
            }
            for (String key : adapter.keys()) {
                if (key == null || key.isBlank()) {
                    throw new IllegalStateException("智能入库适配器 key 不能为空");
                }
                if (ALLOWED_FIELDS.containsKey(key) || registered.putIfAbsent(key, adapter) != null) {
                    throw new IllegalStateException("智能入库适配器 key 重复: " + key);
                }
            }
        }
        return java.util.Collections.unmodifiableMap(registered);
    }
}
