package com.houjiang.base.service;

import com.houjiang.base.dto.fileimport.FileRecognizeCatalogEntry;
import com.houjiang.base.dto.fileimport.FileRecognizeCatalogEntry.FieldDefinition;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public final class FileRecognizeCatalog {
    private static final List<FileRecognizeCatalogEntry> ENTRIES = List.of(
            entry("mall", "商城", "goods", "商品管理", "goods", "商品", "hou_goods_base", "/mall/goods/detail/",
                    field("goodsName", "商品名称", "string", true), field("catId", "商品分类", "long", false),
                    field("goodsPrice", "销售价", "decimal", false), field("goodsMarketPrice", "市场价", "decimal", false),
                    field("goodsCode", "商品编码", "string", false), field("goodsImage", "商品图片", "url", false),
                    field("goodsCostPrice", "成本价", "decimal", false), field("dw", "单位", "string", false),
                    field("moq", "起订量", "integer", false)),
            entry("mall", "商城", "customer", "客户管理", "customer", "商城客户", "hou_mall_customer", "/mall/customer/detail/",
                    field("name", "客户名称", "string", true), field("phone", "手机号", "string", false),
                    field("email", "邮箱", "string", false), field("province", "省", "string", false),
                    field("city", "市", "string", false), field("district", "区县", "string", false),
                    field("address", "详细地址", "string", false), field("source", "客户来源", "string", false),
                    field("remark", "备注", "text", false)),
            entry("crm", "CRM", "customer", "客户管理", "customer", "CRM客户", "hou_crm_customer", "/crm/customer/detail/",
                    field("name", "客户名称", "string", true), field("mobile", "手机号", "string", false),
                    field("telephone", "电话", "string", false), field("detailAddress", "详细地址", "string", false),
                    field("remark", "备注", "text", false)),
            entry("crm", "CRM", "business", "商机管理", "business", "商机", "hou_crm_business", "/crm/business/detail/",
                    field("name", "商机名称", "string", true), field("customerId", "客户", "long", false),
                    field("money", "预计金额", "decimal", false), field("dealTime", "预计成交时间", "datetime", false),
                    field("remark", "备注", "text", false)),
            entry("mes", "智能制造", "material", "物料管理", "material", "物料", "hou_mes_material", "/mes/material/detail/",
                    field("materialCode", "物料编码", "string", true), field("materialName", "物料名称", "string", true),
                    field("specification", "规格", "string", false), field("unit", "单位", "string", false),
                    field("materialType", "物料类型", "string", false), field("purchasePrice", "采购价", "decimal", false),
                    field("stockQuantity", "库存数量", "decimal", false), field("remark", "备注", "text", false)),
            entry("mes", "智能制造", "production", "生产管理", "production", "生产计划", "hou_mes_production", "/mes/production/detail/",
                    field("planCode", "计划编号", "string", true), field("productionBatch", "生产批次", "string", false),
                    field("goodsName", "产品名称", "string", true), field("goodsCode", "产品编码", "string", false),
                    field("number", "数量", "decimal", false), field("deliveryTime", "交付时间", "datetime", false),
                    field("unitPrice", "单价", "decimal", false), field("allMoney", "总金额", "decimal", false),
                    field("remark", "备注", "text", false)),
            entry("pm", "项目管理", "project", "项目管理", "project", "项目", "hou_pm_project", "/pm/project/detail/",
                    field("number", "项目编号", "string", true), field("name", "项目名称", "string", true),
                    field("category", "项目分类", "string", false), field("priority", "优先级", "string", false),
                    field("customerName", "客户名称", "string", false), field("price", "项目金额", "decimal", false),
                    field("content", "项目内容", "text", false), field("remark", "备注", "text", false)),
            entry("pm", "项目管理", "task", "任务管理", "task", "项目任务", "hou_pm_task", "/pm/task/detail/",
                    field("taskNo", "任务编号", "string", true), field("name", "任务名称", "string", true),
                    field("projectId", "所属项目", "long", false), field("content", "任务内容", "text", false),
                    field("progress", "进度", "integer", false), field("status", "状态", "string", false),
                    field("level", "优先级", "string", false), field("startTime", "开始时间", "datetime", false),
                    field("endTime", "结束时间", "datetime", false)),
            entry("erp", "ERP", "goods", "商品资料", "goods", "ERP商品", "hou_erp_goods", "/erp/goods/detail/",
                    field("goodsCode", "商品编码", "string", true), field("goodsName", "商品名称", "string", true),
                    field("goodsBarcode", "条码", "string", false), field("goodsSpecificationId", "规格", "long", false),
                    field("marketSellPrice", "零售价", "decimal", false), field("wholesalePrice", "批发价", "decimal", false),
                    field("purchase", "采购价", "decimal", false), field("goodsImage", "商品图片", "url", false),
                    field("content", "商品详情", "text", false), field("inventory", "库存", "decimal", false),
                    field("note", "备注", "text", false)),
            entry("erp", "ERP", "purchase", "采购管理", "purchase", "采购单", "hou_erp_purchase", "/erp/purchase/detail/",
                    field("purchaseCode", "采购单号", "string", true), field("purchaseTime", "采购时间", "datetime", true),
                    field("suppliersId", "供应商", "long", false), field("money", "应付金额", "decimal", false),
                    field("realMoney", "实付金额", "decimal", false), field("amount", "采购数量", "decimal", false),
                    field("remark", "备注", "text", false)),
            entry("finance", "财务中心", "income", "收入管理", "income", "收入记录",
                    "hou_finance_bookkeeping_income_log", "/finance/income",
                    field("amount", "金额", "decimal", true), field("datatime", "发生日期", "date", true),
                    field("subjectsId", "科目", "long", false), field("typeId", "收入类型", "long", false),
                    field("desc", "描述", "text", false), field("contractId", "合同", "long", false),
                    field("projectId", "项目", "long", false), field("supplierId", "供应商", "long", false),
                    field("proofimage", "凭证图片", "url", false)),
            entry("finance", "财务中心", "spending", "支出管理", "spending", "支出记录",
                    "hou_finance_bookkeeping_spending_log", "/finance/spending",
                    field("amount", "金额", "decimal", true), field("datatime", "发生日期", "date", true),
                    field("subjectsId", "科目", "long", false), field("typeId", "支出类型", "long", false),
                    field("desc", "描述", "text", false), field("contractId", "合同", "long", false),
                    field("projectId", "项目", "long", false), field("supplierId", "供应商", "long", false),
                    field("proofimage", "凭证图片", "url", false)),
            entry("finance", "财务中心", "invoice", "发票管理", "invoice", "发票",
                    "hou_finance_invoice", "/finance/invoice",
                    field("direction", "发票方向", "string", true),
                    field("invoiceNo", "发票号码", "string", true), field("invoiceType", "发票类型", "integer", false),
                    field("title", "发票抬头", "string", true), field("taxNo", "税号", "string", false),
                    field("amount", "含税金额", "decimal", true), field("taxAmount", "税额", "decimal", false),
                    field("issueDate", "开票日期", "date", false), field("customerName", "客户名称", "string", false),
                    field("remark", "备注", "text", false)),
            entry("supplier", "供应商", "goods", "商品管理", "goods", "供应商商品",
                    "hou_supplier_goods_base", "/supplier/product",
                    field("goodsName", "商品名称", "string", true), field("goodsCode", "商品编码", "string", true),
                    field("categoryId", "商品分类", "long", false), field("brandId", "品牌", "long", false),
                    field("price", "销售价", "decimal", false), field("marketPrice", "市场价", "decimal", false),
                    field("unit", "单位", "string", false), field("specification", "规格", "string", false),
                    field("description", "商品描述", "text", false)),
            entry("supplier", "供应商", "shop", "店铺管理", "shop", "供应商店铺",
                    "hou_supplier_shop_base", "/supplier/list",
                    field("shopName", "店铺名称", "string", true), field("userMobile", "联系人手机号", "string", true),
                    field("shopProvince", "省", "string", false), field("shopCity", "市", "string", false),
                    field("shopArea", "区县", "string", false), field("productClassIds", "经营类目", "list", false),
                    field("shopLevelId", "店铺等级", "long", false)),
            entry("payment", "支付中心", "merchantQual", "商户资质管理", "merchantQual", "商户资质",
                    "hou_pay_merchant_qual", "/payment/merchant-qual",
                    field("terminal", "终端", "string", true), field("channel", "支付渠道", "string", true),
                    field("qualName", "资质名称", "string", true), field("env", "环境", "string", false),
                    field("appId", "平台应用ID", "string", false), field("mchId", "渠道商户号", "string", false),
                    field("notifyUrl", "异步回调地址", "url", false), field("returnUrl", "同步跳转地址", "url", false),
                    field("remark", "备注", "text", false)),
            entry("payment", "支付中心", "riskBlacklist", "风控名单管理", "riskBlacklist", "风控黑白名单",
                    "hou_pay_risk_blacklist", "/payment/risk/black-list",
                    field("listType", "名单类型", "integer", true), field("targetType", "目标类型", "integer", true),
                    field("targetValue", "目标值", "string", true), field("bizScope", "业务作用域", "integer", false),
                    field("reason", "加入原因", "text", false), field("riskLevel", "风险等级", "integer", false),
                    field("effectiveTime", "生效时间", "datetime", false), field("expireTime", "失效时间", "datetime", false)),
            entry("oa", "OA办公", "contract", "合同管理", "contract", "合同",
                    "hou_oa_contract", "/oa/contract/list",
                    field("contractName", "合同名称", "string", true), field("contractNumber", "合同编号", "string", false),
                    field("customerName", "客户名称", "string", false), field("customerContact", "客户联系人", "string", false),
                    field("customerPhone", "客户电话", "string", false), field("customerEmail", "客户邮箱", "string", false),
                    field("partyAName", "甲方名称", "string", false), field("partyBName", "乙方名称", "string", false),
                    field("contractAmount", "合同金额", "decimal", false), field("categoryId", "合同分类", "long", false),
                    field("categoryName", "分类名称", "string", false), field("contractContent", "合同内容", "text", false),
                    field("paymentMethod", "付款方式", "string", false)),
            entry("oa", "OA办公", "document", "文档管理", "document", "文档",
                    "hou_oa_document", "/oa/document/library",
                    field("title", "标题", "string", true), field("category", "分类", "string", false),
                    field("description", "描述", "text", false), field("content", "内容", "text", false),
                    field("tags", "标签", "list", false)),
            entry("oa", "OA办公", "notice", "公告管理", "notice", "公告通知",
                    "notice", "/oa/notice/list",
                    field("noticeTitle", "公告标题", "string", true), field("noticeType", "公告类型", "string", false),
                    field("noticeLevel", "重要级别", "string", false), field("noticeContent", "公告内容", "text", true),
                    field("targetType", "目标类型", "string", false), field("targetDepartmentIds", "目标部门", "list", false),
                    field("targetEmployeeIds", "目标员工", "list", false),
                    field("effectiveStartTime", "生效开始时间", "datetime", false),
                    field("effectiveEndTime", "生效结束时间", "datetime", false),
                    field("keywords", "关键词", "string", false)),
            entry("geo", "GEO", "product", "品牌产品管理", "product", "品牌产品",
                    "hou_geo_brand_product", "/geo/settings/products",
                    field("name", "产品名称", "string", true), field("description", "产品描述", "text", false),
                    field("category", "产品分类", "string", false), field("websiteUrl", "产品网址", "url", false),
                    field("targetMarket", "目标市场", "string", false), field("valueProposition", "价值主张", "text", false)),
            entry("geo", "GEO", "competitor", "竞品管理", "competitor", "竞品",
                    "hou_geo_competitor", "/geo/settings/competitors",
                    field("name", "竞品名称", "string", true), field("websiteUrl", "竞品网址", "url", false),
                    field("description", "竞品描述", "text", false), field("strengths", "优势", "text", false),
                    field("weaknesses", "劣势", "text", false)),
            entry("geo", "GEO", "store", "门店管理", "store", "门店",
                    "hou_geo_local_store", "/geo/local",
                    field("name", "门店名称", "string", true), field("address", "门店地址", "string", true),
                    field("phone", "联系电话", "string", false), field("website", "网站", "url", false),
                    field("latitude", "纬度", "decimal", false), field("longitude", "经度", "decimal", false),
                    field("categories", "主营类目", "list", false), field("openingHours", "营业时间", "list", false)),
            entry("aicode", "AICode", "source", "代码源管理", "source", "代码源",
                    "aicode_code_source", "/aicode/sources",
                    field("name", "代码源名称", "string", true), field("repositoryUrl", "仓库地址", "url", true),
                    field("defaultBranch", "默认分支", "string", false), field("executionMode", "执行模式", "string", false),
                    field("credentialRef", "凭证引用", "string", false)),
            entry("aicode", "AICode", "task", "开发任务管理", "task", "开发任务",
                    "aicode_task", "/aicode/workbench",
                    field("requirement", "开发需求", "text", true), field("baseBranch", "基准分支", "string", false))
    );

    public List<FileRecognizeCatalogEntry> entries() {
        return ENTRIES;
    }

    public List<FileRecognizeCatalogEntry> entriesForSystem(String systemKey) {
        if (systemKey == null) return List.of();
        return ENTRIES.stream().filter(entry -> systemKey.equals(entry.systemKey())).toList();
    }

    public Optional<FileRecognizeCatalogEntry> find(String systemKey, String objectKey) {
        return ENTRIES.stream().filter(entry -> entry.systemKey().equals(systemKey)
                && entry.objectKey().equals(objectKey)).findFirst();
    }

    public ResponseResult<Map<String, Object>> validate(String systemKey, String objectKey, Map<String, Object> values) {
        ResponseResult<Map<String, Object>> writable = validateWritableFields(systemKey, objectKey, values);
        if (writable.getCode() != 1) return writable;
        Map<String, Object> safeValues = writable.getData();
        FileRecognizeCatalogEntry entry = find(systemKey, objectKey).orElseThrow();
        for (FieldDefinition field : entry.fields()) {
            if (field.required() && blankValue(safeValues.get(field.name()))) {
                return ResponseResult.fail(422, "缺少必填字段：" + field.name());
            }
        }
        return ResponseResult.success(new LinkedHashMap<>(safeValues));
    }

    public ResponseResult<Map<String, Object>> validateWritableFields(String systemKey, String objectKey,
                                                                       Map<String, Object> values) {
        Optional<FileRecognizeCatalogEntry> found = find(systemKey, objectKey);
        if (found.isEmpty()) {
            return ResponseResult.fail(422, "目标业务对象不在允许目录中");
        }
        Map<String, Object> safeValues = values == null ? Map.of() : values;
        Set<String> allowed = found.get().fields().stream().map(FieldDefinition::name).collect(Collectors.toSet());
        for (String fieldName : safeValues.keySet()) {
            if (!allowed.contains(fieldName)) {
                return ResponseResult.fail(422, "字段不允许写入：" + fieldName);
            }
        }
        return ResponseResult.success(new LinkedHashMap<>(safeValues));
    }

    public Map<String, Object> promptSchema(String systemKey) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("systemKey", systemKey);
        List<Map<String, Object>> objects = entriesForSystem(systemKey).stream().map(entry -> {
            Map<String, Object> object = new LinkedHashMap<>();
            object.put("key", entry.systemKey() + "." + entry.objectKey());
            object.put("functionKey", entry.functionKey());
            object.put("objectKey", entry.objectKey());
            object.put("objectName", entry.objectName());
            object.put("fields", entry.fields());
            return Map.copyOf(object);
        }).toList();
        schema.put("objects", objects);
        return Map.copyOf(schema);
    }

    private static boolean blankValue(Object value) {
        return value == null || value instanceof String text && text.isBlank();
    }

    private static FileRecognizeCatalogEntry entry(String systemKey, String systemName,
                                                    String functionKey, String functionName,
                                                    String objectKey, String objectName,
                                                    String table, String detailPath,
                                                    FieldDefinition... fields) {
        return new FileRecognizeCatalogEntry(systemKey, systemName, functionKey, functionName,
                objectKey, objectName, table, systemKey + "." + objectKey, detailPath,
                "domain-or-import-record", List.of(fields));
    }

    private static FieldDefinition field(String name, String label, String type, boolean required) {
        return new FieldDefinition(name, label, type, required);
    }
}
