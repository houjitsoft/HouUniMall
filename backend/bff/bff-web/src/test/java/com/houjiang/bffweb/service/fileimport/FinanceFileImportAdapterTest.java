package com.houjiang.bffweb.service.fileimport;

import com.houjiang.bffweb.feign.FinanceServiceFeign;
import com.houjiang.bffweb.service.FileImportAdapterResult;
import com.houjiang.bffweb.util.ResponseResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class FinanceFileImportAdapterTest {

    @Test
    void supportsOnlyTheThreeControlledFinanceObjects() throws ReflectiveOperationException {
        FileImportBusinessAdapter adapter = adapter(mock(FinanceServiceFeign.class));

        assertEquals(Set.of("finance.income", "finance.spending", "finance.invoice"), adapter.keys());
        assertEquals(422, adapter.importItem("finance.unknown", Map.of(), attachment()).code());
    }

    @Test
    void forcesUnauditedStatusAndUsesOriginalFileAsBookkeepingProof() throws ReflectiveOperationException {
        FinanceServiceFeign finance = mock(FinanceServiceFeign.class);
        finance = mock(FinanceServiceFeign.class, invocation -> switch (invocation.getMethod().getName()) {
            case "importIncome" -> ResponseResult.success(Map.of("id", 101));
            case "importSpending" -> ResponseResult.success(Map.of("id", 102));
            default -> null;
        });
        FileImportBusinessAdapter adapter = adapter(finance);
        Map<String, Object> recognized = new LinkedHashMap<>();
        recognized.put("amount", "125.50");
        recognized.put("datatime", "2026-09-10");
        recognized.put("status", "audited");
        recognized.put("proofimage", "https://attacker.invalid/fake.jpg");
        recognized.put("orgId", "other-tenant");

        FileImportAdapterResult income = adapter.importItem("finance.income", recognized, attachment());
        FileImportAdapterResult spending = adapter.importItem("finance.spending", recognized, attachment());

        assertEquals(1, income.code());
        assertEquals(1, spending.code());
        assertEquals("101", income.businessRecordId());
        assertEquals("102", spending.businessRecordId());
        Map<String, Object> safeIncome = invocationPayload(finance, "importIncome");
        Map<String, Object> safeSpending = invocationPayload(finance, "importSpending");
        for (Map<String, Object> payload : List.of(safeIncome, safeSpending)) {
            assertEquals("unaudited", payload.get("status"));
            assertEquals("https://files/source.pdf", payload.get("proofimage"));
            assertEquals(new java.math.BigDecimal("125.50"), payload.get("amount"));
            assertTrue(payload.get("datatime") instanceof Integer);
            assertFalse(payload.containsKey("orgId"));
        }
    }

    @Test
    void mapsCatalogInvoiceFieldsToTheRealFinanceContractAndKeepsItPending() throws ReflectiveOperationException {
        FinanceServiceFeign finance = mock(FinanceServiceFeign.class);
        when(finance.createInvoice(any())).thenReturn(ResponseResult.success(Map.of("id", 77L)));
        FileImportBusinessAdapter adapter = adapter(finance);

        FileImportAdapterResult result = adapter.importItem("finance.invoice", Map.ofEntries(
                Map.entry("invoiceNo", "FP-2026-001"),
                Map.entry("direction", "input"),
                Map.entry("invoiceType", "2"),
                Map.entry("title", "后匠科技有限公司"),
                Map.entry("taxNo", "91310000TEST"),
                Map.entry("amount", "100.00"),
                Map.entry("taxAmount", "6.00"),
                Map.entry("issueDate", "2026-09-10"),
                Map.entry("customerName", "不得覆盖抬头"),
                Map.entry("status", 2),
                Map.entry("orgId", "other-tenant")), attachment());

        assertEquals(1, result.code());
        assertEquals("77", result.businessRecordId());
        assertEquals("/finance/invoice", result.detailUrl());
        ArgumentCaptor<Map<String, Object>> payload = ArgumentCaptor.forClass(Map.class);
        verify(finance).createInvoice(payload.capture());
        assertEquals("后匠科技有限公司", payload.getValue().get("customerName"));
        assertEquals(java.time.LocalDate.of(2026, 9, 10), payload.getValue().get("invoiceDate"));
        assertEquals(2, payload.getValue().get("invoiceType"));
        assertEquals(0, payload.getValue().get("status"));
        assertEquals("input", payload.getValue().get("direction"));
        assertTrue(payload.getValue().get("amount") instanceof java.math.BigDecimal);
        assertTrue(payload.getValue().get("taxAmount") instanceof java.math.BigDecimal);
        assertTrue(payload.getValue().get("invoiceDate") instanceof java.time.LocalDate);
        assertFalse(payload.getValue().containsKey("title"));
        assertFalse(payload.getValue().containsKey("issueDate"));
        assertFalse(payload.getValue().containsKey("orgId"));
    }

    @Test
    void rejectsInvalidFinanceTypesBeforeCallingTheDomainService() throws ReflectiveOperationException {
        for (Map<String, Object> fields : List.of(
                invoice("sideways", "100", "2026-09-10", "1"),
                invoice("output", "not-money", "2026-09-10", "1"),
                invoice("output", "100", "not-a-date", "1"),
                invoice("output", "100", "2026-09-10", "1.5"))) {
            FinanceServiceFeign finance = mock(FinanceServiceFeign.class);
            FileImportAdapterResult result = adapter(finance).importItem("finance.invoice", fields, attachment());
            assertEquals(422, result.code(), fields.toString());
            verifyNoInteractions(finance);
        }
    }

    @Test
    void acceptsMillisecondsTimestampWithoutOverflow() throws ReflectiveOperationException {
        FinanceServiceFeign finance = mock(FinanceServiceFeign.class, invocation ->
                "importIncome".equals(invocation.getMethod().getName())
                        ? ResponseResult.success(Map.of("id", 103)) : null);
        FileImportAdapterResult result = adapter(finance).importItem("finance.income",
                Map.of("amount", 1, "datatime", 1788969600000L), attachment());
        assertEquals(1, result.code());
        assertEquals(1788969600, invocationPayload(finance, "importIncome").get("datatime"));
    }

    @Test
    void rejectsEveryIsoDateFormOutsideSigned32BitEpochSeconds() throws ReflectiveOperationException {
        for (String date : List.of(
                "2100-01-01",
                "2100-01-01T00:00:00",
                "2100-01-01T00:00:00Z")) {
            FinanceServiceFeign finance = mock(FinanceServiceFeign.class);

            FileImportAdapterResult result = adapter(finance).importItem("finance.income",
                    Map.of("amount", 1, "datatime", date), attachment());

            assertEquals(422, result.code(), date);
            assertEquals("发生日期超出允许范围", result.message(), date);
            verifyNoInteractions(finance);
        }
    }

    @Test
    void rejectsInvalidBookkeepingAmountsBeforeCallingTheDomainService() throws ReflectiveOperationException {
        for (String key : List.of("finance.income", "finance.spending")) {
            for (Object amount : List.of("not-money", "-0.01")) {
                FinanceServiceFeign finance = mock(FinanceServiceFeign.class);
                FileImportAdapterResult result = adapter(finance).importItem(key,
                        Map.of("amount", amount, "datatime", "2026-09-10"), attachment());
                assertEquals(422, result.code(), key + ": " + amount);
                verifyNoInteractions(finance);
            }
        }
    }

    private Map<String, Object> invoice(String direction, String amount, String date, String type) {
        return Map.of("direction", direction, "invoiceNo", "FP-1", "title", "抬头",
                "amount", amount, "issueDate", date, "invoiceType", type);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> invocationPayload(FinanceServiceFeign finance, String methodName) {
        return (Map<String, Object>) org.mockito.Mockito.mockingDetails(finance).getInvocations().stream()
                .filter(call -> methodName.equals(call.getMethod().getName())).findFirst().orElseThrow().getArgument(0);
    }

    private FileImportBusinessAdapter adapter(FinanceServiceFeign finance) throws ReflectiveOperationException {
        Class<?> type = Class.forName("com.houjiang.bffweb.service.fileimport.FinanceFileImportAdapter");
        return (FileImportBusinessAdapter) type.getConstructor(FinanceServiceFeign.class).newInstance(finance);
    }

    private Map<String, Object> attachment() {
        return Map.of("fileName", "source.pdf", "fileUrl", "https://files/source.pdf",
                "fileSize", 123L, "contentType", "application/pdf");
    }
}
