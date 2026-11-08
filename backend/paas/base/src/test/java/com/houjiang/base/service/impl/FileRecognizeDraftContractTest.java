package com.houjiang.base.service.impl;

import com.houjiang.base.dto.fileimport.DraftCreateRequest;
import com.houjiang.base.dto.fileimport.DraftFileRequest;
import com.houjiang.base.dto.fileimport.DraftPreviewResponse;
import com.houjiang.base.dto.fileimport.ImportItemUpdateRequest;
import com.houjiang.base.mapper.FileRecognizeDraftMapper;
import com.houjiang.commons.util.ResponseResult;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
class FileRecognizeDraftContractTest {

    @Test
    void rejectsBlankDescriptionBeforePersistence() {
        FakeDraftMapper mapper = new FakeDraftMapper();
        FileRecognizeDraftServiceImpl service = new FileRecognizeDraftServiceImpl(mapper);

        DraftCreateRequest request = new DraftCreateRequest();
        request.setSystemKey("crm");
        request.setDescription("   ");
        request.setUserId("42");

        ResponseResult result = service.createOrUpdate(request, file());

        assertEquals(422, result.getCode());
        assertEquals(0, mapper.insertDraftCalls);
    }

    @Test
    void trimsDescriptionAndReturnsStableDraftForDuplicateFile() {
        FakeDraftMapper mapper = new FakeDraftMapper();
        mapper.draft = Map.of(
                "id", 10L,
                "draftNo", "draft-1",
                "userId", "42",
                "orgId", "8",
                "systemKey", "crm",
                "description", "客户资料",
                "status", "recognizing");
        mapper.file = Map.of(
                "id", 20L,
                "fileName", "客户资料.pdf",
                "fileHash", "sha256-1",
                "status", "recognized");
        FileRecognizeDraftServiceImpl service = new FileRecognizeDraftServiceImpl(mapper);

        DraftCreateRequest request = new DraftCreateRequest();
        request.setDraftNo("draft-1");
        request.setSystemKey("crm");
        request.setDescription("  客户资料  ");
        request.setUserId("42");
        request.setOrgId("8");

        ResponseResult result = service.createOrUpdate(request, file());

        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        Map<?, ?> data = (Map<?, ?>) result.getData();
        assertEquals("draft-1", data.get("draftNo"));
        assertEquals("客户资料", data.get("description"));
        assertEquals(20L, data.get("itemId"));
        assertEquals(0, mapper.insertFileCalls);
    }

    @Test
    void resumesLatestOwnedDraftAndPrefersEditedFieldsOverAiFields() {
        FakeDraftMapper mapper = new FakeDraftMapper();
        mapper.draft = Map.of(
                "id", 10L, "draftNo", "draft-1", "userId", "42", "orgId", "8",
                "systemKey", "crm", "description", "客户资料", "status", "preview");
        mapper.items = List.of(Map.of(
                "itemId", 20L, "fileName", "客户资料.pdf", "fileUrl", "https://oss/file.pdf",
                "objectKey", "customer", "functionKey", "customer", "status", "recognized",
                "aiRawJson", "{\"summary\":\"识别到客户名称\",\"aiProvider\":\"OpenAI\",\"aiModel\":\"gpt-5\"}",
                "aiFieldsJson", "{\"name\":\"AI客户\"}",
                "editedFieldsJson", "{\"name\":\"用户修改后的客户\"}"));
        FileRecognizeDraftServiceImpl service = new FileRecognizeDraftServiceImpl(mapper);

        ResponseResult result = service.resume("42", "8");

        assertEquals(1, result.getCode());
        DraftPreviewResponse preview = (DraftPreviewResponse) result.getData();
        assertEquals("draft-1", preview.getDraftNo());
        Map<?, ?> fields = (Map<?, ?>) preview.getItems().getFirst().get("fields");
        assertEquals("用户修改后的客户", fields.get("name"));
        assertEquals("识别到客户名称", preview.getItems().getFirst().get("summary"));
        assertEquals("gpt-5", preview.getItems().getFirst().get("aiModel"));
    }

    @Test
    void returnsSuccessfulEmptyResponseWhenNoDraftCanBeResumed() {
        FakeDraftMapper mapper = new FakeDraftMapper();
        FileRecognizeDraftServiceImpl service = new FileRecognizeDraftServiceImpl(mapper);

        ResponseResult result = service.resume("42", "8");

        assertEquals(1, result.getCode());
        assertNull(result.getData());
    }

    @Test
    void deniesAnotherUserAndRejectsUnknownEditedField() {
        FakeDraftMapper mapper = new FakeDraftMapper();
        mapper.draft = Map.of(
                "id", 10L, "draftNo", "draft-1", "userId", "99", "orgId", "8",
                "systemKey", "crm", "description", "客户资料", "status", "preview");
        FileRecognizeDraftServiceImpl service = new FileRecognizeDraftServiceImpl(mapper);

        assertEquals(403, service.findDraft("draft-1", "42", "8").getCode());

        mapper.itemContext = Map.of(
                "itemId", 20L, "userId", "42", "orgId", "8", "systemKey", "crm");
        ImportItemUpdateRequest update = new ImportItemUpdateRequest();
        update.setItemId(20L);
        update.setFunctionKey("customer");
        update.setObjectKey("customer");
        update.setFields(Map.of("dropTable", "never"));
        ResponseResult invalid = service.updateItem(update, "42", "8");

        assertEquals(422, invalid.getCode());
        assertEquals(0, mapper.updateItemCalls);
    }

    @Test
    void storesAiPayloadSeparatelyWithoutOverwritingEditedFields() {
        FakeDraftMapper mapper = new FakeDraftMapper();
        mapper.draft = Map.of(
                "id", 10L, "draftNo", "draft-1", "userId", "42", "orgId", "8",
                "systemKey", "crm", "description", "客户资料", "status", "recognizing");
        mapper.itemContext = Map.of(
                "itemId", 20L, "draftId", 10L, "draftNo", "draft-1", "userId", "42",
                "orgId", "8", "systemKey", "crm");
        FileRecognizeDraftServiceImpl service = new FileRecognizeDraftServiceImpl(mapper);
        Map<String, Object> recognition = Map.of(
                "systemKey", "crm", "functionKey", "customer", "objectKey", "customer",
                "fields", Map.of("name", "AI客户"), "summary", "识别完成",
                "needsManualTarget", false);

        ResponseResult saved = service.saveRecognition("draft-1", 20L, recognition, "42", "8");

        assertEquals(1, saved.getCode());
        assertEquals(1, mapper.saveRecognitionCalls);
        assertEquals("{\"name\":\"AI客户\"}", mapper.savedAiFieldsJson);
        assertTrue(mapper.savedAiRawJson.contains("识别完成"));
        assertEquals(null, mapper.savedEditedFieldsJson);
    }

    @Test
    void preparesOnlyValidatedStoredFieldsAndRecordsAttachmentOnSuccess() {
        FakeDraftMapper mapper = new FakeDraftMapper();
        mapper.confirmItem = Map.ofEntries(
                Map.entry("itemId", 20L), Map.entry("draftId", 10L), Map.entry("draftNo", "draft-1"),
                Map.entry("userId", "42"), Map.entry("orgId", "8"), Map.entry("systemKey", "pm"),
                Map.entry("functionKey", "project"), Map.entry("objectKey", "project"),
                Map.entry("fileName", "项目资料.pdf"), Map.entry("fileUrl", "https://files/project.pdf"),
                Map.entry("fileSize", 200L), Map.entry("contentType", "application/pdf"),
                Map.entry("aiFieldsJson", "{\"number\":\"P1\",\"name\":\"AI项目\"}"),
                Map.entry("editedFieldsJson", "{\"number\":\"P1\",\"name\":\"用户项目\"}"));
        FileRecognizeDraftServiceImpl service = new FileRecognizeDraftServiceImpl(mapper);

        ResponseResult prepared = service.prepareConfirm(20L, "42", "8");

        assertEquals(1, prepared.getCode());
        Map<?, ?> data = (Map<?, ?>) prepared.getData();
        assertEquals("pm.project", data.get("adapterKey"));
        assertEquals("用户项目", ((Map<?, ?>) data.get("fields")).get("name"));
        assertEquals("https://files/project.pdf", ((Map<?, ?>) data.get("attachment")).get("fileUrl"));

        ResponseResult completed = service.completeItem(20L, "success", "88", "/pm/project/detail/88",
                "https://files/project.pdf", null, "42", "8");
        assertEquals(1, completed.getCode());
        assertEquals(1, mapper.completeItemCalls);
        assertEquals(1, mapper.saveAttachmentCalls);
    }

    @Test
    void prepareConfirmRejectsMissingRequiredStoredField() {
        FakeDraftMapper mapper = new FakeDraftMapper();
        mapper.confirmItem = Map.ofEntries(
                Map.entry("itemId", 20L), Map.entry("draftId", 10L), Map.entry("draftNo", "draft-1"),
                Map.entry("userId", "42"), Map.entry("orgId", "8"), Map.entry("systemKey", "pm"),
                Map.entry("functionKey", "project"), Map.entry("objectKey", "project"),
                Map.entry("fileName", "项目资料.pdf"), Map.entry("fileUrl", "https://files/project.pdf"),
                Map.entry("aiFieldsJson", "{}"), Map.entry("editedFieldsJson", "{\"name\":\"缺编号项目\"}"));
        FileRecognizeDraftServiceImpl service = new FileRecognizeDraftServiceImpl(mapper);

        assertEquals(422, service.prepareConfirm(20L, "42", "8").getCode());
    }

    @Test
    void successfulCompletionRejectsMissingOriginalAttachmentBeforeUpdatingResult() {
        FakeDraftMapper mapper = new FakeDraftMapper();
        mapper.confirmItem = Map.ofEntries(
                Map.entry("itemId", 20L), Map.entry("draftId", 10L), Map.entry("draftNo", "draft-1"),
                Map.entry("userId", "42"), Map.entry("orgId", "8"), Map.entry("systemKey", "pm"),
                Map.entry("functionKey", "project"), Map.entry("objectKey", "project"),
                Map.entry("fileName", "项目资料.pdf"));
        FileRecognizeDraftServiceImpl service = new FileRecognizeDraftServiceImpl(mapper);

        ResponseResult completed = service.completeItem(20L, "success", "88", "/pm/project/detail/88",
                null, null, "42", "8");

        assertEquals(422, completed.getCode());
        assertEquals(0, mapper.completeItemCalls);
        assertEquals(0, mapper.saveAttachmentCalls);
    }

    private DraftFileRequest file() {
        DraftFileRequest file = new DraftFileRequest();
        file.setFileName("客户资料.pdf");
        file.setFileHash("sha256-1");
        file.setFileUrl("https://oss.example/customer.pdf");
        file.setFileSize(128L);
        file.setContentType("application/pdf");
        file.setAiRawJson("{\"objectKey\":\"crm.customer\"}");
        file.setEditedFieldsJson("{\"name\":\"厚匠科技\"}");
        file.setStatus("recognized");
        return file;
    }

    private static class FakeDraftMapper implements FileRecognizeDraftMapper {
        private Map<String, Object> draft;
        private Map<String, Object> file;
        private Map<String, Object> itemContext;
        private Map<String, Object> confirmItem;
        private List<Map<String, Object>> items = List.of();
        private int insertDraftCalls;
        private int insertFileCalls;
        private int updateItemCalls;
        private int saveRecognitionCalls;
        private String savedAiRawJson;
        private String savedAiFieldsJson;
        private String savedEditedFieldsJson;
        private int completeItemCalls;
        private int saveAttachmentCalls;

        @Override public Map<String, Object> findDraftByNo(String draftNo) { return draft; }
        @Override public Map<String, Object> findFileByDraftAndHash(Long draftId, String fileHash) { return file; }
        @Override public long insertDraft(Map<String, Object> values) { insertDraftCalls++; return 10L; }
        @Override public long insertFile(Map<String, Object> values) { insertFileCalls++; return 20L; }
        @Override public void updateDraft(Long id, String systemKey, String description, String status) { }
        @Override public Map<String, Object> findLatestOpenDraft(String userId, String orgId) { return draft; }
        @Override public List<Map<String, Object>> findItemsByDraftId(Long draftId) { return items; }
        @Override public Map<String, Object> findItemContext(Long itemId) { return itemContext; }
        @Override public void updateItem(Long itemId, String functionKey, String objectKey, String editedFieldsJson) {
            updateItemCalls++;
        }
        @Override public void saveRecognition(Long draftId, Long itemId, String functionKey, String objectKey,
                                    String aiRawJson, String aiFieldsJson, String status, String errorMessage) {
            saveRecognitionCalls++;
            savedAiRawJson = aiRawJson;
            savedAiFieldsJson = aiFieldsJson;
        }
        @Override public Map<String, Object> findConfirmItem(Long itemId) { return confirmItem; }
        @Override public void completeItem(Long draftId, Long itemId, String status, String businessRecordId,
                                 String detailUrl, String errorMessage) { completeItemCalls++; }
        @Override public void saveAttachment(Long draftId, Long itemId, String objectKey, String businessRecordId,
                                   String fileName, String fileUrl, String status, String errorMessage) {
            saveAttachmentCalls++;
        }
    }
}
