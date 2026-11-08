package com.houjiang.base.service;

import com.houjiang.base.dto.fileimport.FileRecognizeCatalogEntry;
import com.houjiang.commons.util.ResponseResult;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileRecognizeCatalogTest {

    private final FileRecognizeCatalog catalog = new FileRecognizeCatalog();

    @Test
    void exposesOnlyTheTwentyFiveControlledBusinessObjects() {
        Set<String> keys = catalog.entries().stream()
                .map(entry -> entry.systemKey() + "." + entry.objectKey())
                .collect(Collectors.toSet());

        assertEquals(Set.of(
                "mall.goods", "mall.customer",
                "crm.customer", "crm.business",
                "mes.material", "mes.production",
                "pm.project", "pm.task",
                "erp.goods", "erp.purchase",
                "finance.income", "finance.spending", "finance.invoice",
                "supplier.goods", "supplier.shop",
                "payment.merchantQual", "payment.riskBlacklist",
                "oa.contract", "oa.document", "oa.notice",
                "geo.product", "geo.competitor", "geo.store",
                "aicode.source", "aicode.task"
        ), keys);
        assertEquals(25, catalog.entries().size());
        assertTrue(catalog.find("crm", "customer").isPresent());
        assertTrue(catalog.find("crm", "not-a-real-object").isEmpty());
    }

    @Test
    void exposesExactWhitelistsAndMetadataForTheFifteenNewObjects() {
        Map<String, List<String>> expectedFields = Map.ofEntries(
                Map.entry("finance.income", List.of("amount*", "datatime*", "subjectsId", "typeId", "desc",
                        "contractId", "projectId", "supplierId", "proofimage")),
                Map.entry("finance.spending", List.of("amount*", "datatime*", "subjectsId", "typeId", "desc",
                        "contractId", "projectId", "supplierId", "proofimage")),
                Map.entry("finance.invoice", List.of("direction*", "invoiceNo*", "invoiceType", "title*", "taxNo", "amount*",
                        "taxAmount", "issueDate", "customerName", "remark")),
                Map.entry("supplier.goods", List.of("goodsName*", "goodsCode*", "categoryId", "brandId", "price",
                        "marketPrice", "unit", "specification", "description")),
                Map.entry("supplier.shop", List.of("shopName*", "userMobile*", "shopProvince", "shopCity",
                        "shopArea", "productClassIds", "shopLevelId")),
                Map.entry("payment.merchantQual", List.of("terminal*", "channel*", "qualName*", "env", "appId",
                        "mchId", "notifyUrl", "returnUrl", "remark")),
                Map.entry("payment.riskBlacklist", List.of("listType*", "targetType*", "targetValue*", "bizScope",
                        "reason", "riskLevel", "effectiveTime", "expireTime")),
                Map.entry("oa.contract", List.of("contractName*", "contractNumber", "customerName", "customerContact",
                        "customerPhone", "customerEmail", "partyAName", "partyBName", "contractAmount", "categoryId",
                        "categoryName", "contractContent", "paymentMethod")),
                Map.entry("oa.document", List.of("title*", "category", "description", "content", "tags")),
                Map.entry("oa.notice", List.of("noticeTitle*", "noticeType", "noticeLevel", "noticeContent*",
                        "targetType", "targetDepartmentIds", "targetEmployeeIds", "effectiveStartTime",
                        "effectiveEndTime", "keywords")),
                Map.entry("geo.product", List.of("name*", "description", "category", "websiteUrl", "targetMarket",
                        "valueProposition")),
                Map.entry("geo.competitor", List.of("name*", "websiteUrl", "description", "strengths", "weaknesses")),
                Map.entry("geo.store", List.of("name*", "address*", "phone", "website", "latitude", "longitude",
                        "categories", "openingHours")),
                Map.entry("aicode.source", List.of("name*", "repositoryUrl*", "defaultBranch", "executionMode",
                        "credentialRef")),
                Map.entry("aicode.task", List.of("requirement*", "baseBranch"))
        );
        Map<String, String> expectedSystemNames = Map.of(
                "finance", "财务中心",
                "supplier", "供应商",
                "payment", "支付中心",
                "oa", "OA办公",
                "geo", "GEO",
                "aicode", "AICode"
        );

        expectedFields.forEach((key, fields) -> {
            String[] keyParts = key.split("\\.", 2);
            FileRecognizeCatalogEntry entry = catalog.find(keyParts[0], keyParts[1]).orElseThrow();
            assertEquals(expectedSystemNames.get(keyParts[0]), entry.systemName(), key);
            assertEquals(key, entry.adapterKey(), key);
            assertEquals("domain-or-import-record", entry.attachmentStrategy(), key);
            assertEquals(fields, entry.fields().stream()
                    .map(field -> field.name() + (field.required() ? "*" : ""))
                    .toList(), key);
        });
    }

    @Test
    void rejectsSensitivePaymentAndSourceCredentialFieldsWith422() {
        for (String forbidden : List.of("appSecret", "apiKey", "certs", "status")) {
            ResponseResult<?> result = catalog.validateWritableFields(
                    "payment", "merchantQual", Map.of(forbidden, "plaintext-secret"));
            assertEquals(422, result.getCode(), forbidden);
            assertTrue(result.getMessage().contains(forbidden), forbidden);
        }

        for (String forbidden : List.of("password", "repositoryPassword", "apiKey", "accessToken", "secretKey",
                "privateKey", "credentialValue")) {
            ResponseResult<?> result = catalog.validateWritableFields(
                    "aicode", "source", Map.of(forbidden, "plaintext-secret"));
            assertEquals(422, result.getCode(), forbidden);
            assertTrue(result.getMessage().contains(forbidden), forbidden);
        }
    }

    @Test
    void catalogEntriesAreImmutable() {
        List<FileRecognizeCatalogEntry> entries = catalog.entries();

        org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class,
                () -> entries.add(entries.getFirst()));
        org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class,
                () -> entries.getFirst().fields().clear());
    }

    @Test
    void rejectsUnknownObjectUnknownFieldAndMissingRequiredFieldWith422() {
        ResponseResult<?> unknownObject = catalog.validate("crm", "made-up", Map.of());
        assertEquals(422, unknownObject.getCode());

        Map<String, Object> unknownFieldValues = new LinkedHashMap<>();
        unknownFieldValues.put("name", "华东客户");
        unknownFieldValues.put("dropTable", "never");
        ResponseResult<?> unknownField = catalog.validate("crm", "customer", unknownFieldValues);
        assertEquals(422, unknownField.getCode());
        assertTrue(unknownField.getMessage().contains("dropTable"));

        ResponseResult<?> missingRequired = catalog.validate("pm", "project", Map.of("remark", "资料导入"));
        assertEquals(422, missingRequired.getCode());
        assertTrue(missingRequired.getMessage().contains("number"));
        assertFalse(missingRequired.getMessage().contains("SQL"));

        ResponseResult<?> missingInvoiceDirection = catalog.validate("finance", "invoice", Map.of(
                "invoiceNo", "FP-1", "title", "抬头", "amount", 100));
        assertEquals(422, missingInvoiceDirection.getCode());
        assertTrue(missingInvoiceDirection.getMessage().contains("direction"));
    }
}
