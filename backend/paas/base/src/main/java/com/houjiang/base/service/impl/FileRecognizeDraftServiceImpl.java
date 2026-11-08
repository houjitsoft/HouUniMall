package com.houjiang.base.service.impl;

import com.alibaba.fastjson.JSON;
import com.houjiang.base.dto.fileimport.DraftCreateRequest;
import com.houjiang.base.dto.fileimport.DraftFileRequest;
import com.houjiang.base.dto.fileimport.DraftPreviewResponse;
import com.houjiang.base.dto.fileimport.ImportItemUpdateRequest;
import com.houjiang.base.mapper.FileRecognizeDraftMapper;
import com.houjiang.base.service.FileRecognizeCatalog;
import com.houjiang.base.service.FileRecognizeDraftService;
import com.houjiang.commons.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileRecognizeDraftServiceImpl implements FileRecognizeDraftService {
    private final FileRecognizeDraftMapper mapper;
    private final FileRecognizeCatalog catalog = new FileRecognizeCatalog();

    @Override
    public ResponseResult createOrUpdate(DraftCreateRequest request, DraftFileRequest file) {
        if (request == null || blank(request.getSystemKey())) {
            return ResponseResult.fail(422, "请选择业务系统");
        }
        String description = request.getDescription() == null ? "" : request.getDescription().trim();
        if (description.isEmpty()) {
            return ResponseResult.fail(422, "描述不能为空");
        }
        if (description.length() > 500) {
            return ResponseResult.fail(422, "描述不能超过500字");
        }
        if (file == null || blank(file.getFileHash()) || blank(file.getFileName())) {
            return ResponseResult.fail(422, "文件信息不完整");
        }

        String draftNo = blank(request.getDraftNo()) ? "AIR-" + UUID.randomUUID() : request.getDraftNo().trim();
        Map<String, Object> draft = mapper.findDraftByNo(draftNo);
        long draftId;
        if (draft == null) {
            Map<String, Object> values = new LinkedHashMap<>();
            values.put("draftNo", draftNo);
            values.put("userId", request.getUserId());
            values.put("orgId", request.getOrgId());
            values.put("systemKey", request.getSystemKey().trim());
            values.put("description", description);
            values.put("status", "recognizing");
            draftId = mapper.insertDraft(values);
        } else {
            if (!same(request.getUserId(), draft.get("userId")) || !same(request.getOrgId(), draft.get("orgId"))) {
                return ResponseResult.fail(403, "无权访问该识别草稿");
            }
            draftId = number(draft.get("id"));
            mapper.updateDraft(draftId, request.getSystemKey().trim(), description, "recognizing");
        }

        Map<String, Object> storedFile = mapper.findFileByDraftAndHash(draftId, file.getFileHash());
        long itemId;
        if (storedFile == null) {
            Map<String, Object> values = new LinkedHashMap<>();
            values.put("draftId", draftId);
            values.put("fileName", file.getFileName());
            values.put("fileHash", file.getFileHash());
            values.put("fileUrl", file.getFileUrl());
            values.put("fileSize", file.getFileSize());
            values.put("contentType", file.getContentType());
            values.put("aiRawJson", file.getAiRawJson());
            values.put("editedFieldsJson", file.getEditedFieldsJson());
            values.put("status", blank(file.getStatus()) ? "uploaded" : file.getStatus());
            itemId = mapper.insertFile(values);
        } else {
            itemId = number(storedFile.get("id"));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("draftNo", draftNo);
        result.put("draftId", draftId);
        result.put("itemId", itemId);
        result.put("systemKey", request.getSystemKey().trim());
        result.put("description", description);
        result.put("status", storedFile == null ? "uploaded" : storedFile.get("status"));
        return ResponseResult.success(result);
    }

    @Override
    public ResponseResult resume(String userId, String orgId) {
        if (blank(userId)) return ResponseResult.fail(401, "请先登录");
        Map<String, Object> draft = mapper.findLatestOpenDraft(userId, orgId);
        if (draft == null) return ResponseResult.success();
        return ResponseResult.success(toPreview(draft));
    }

    @Override
    public ResponseResult findDraft(String draftNo, String userId, String orgId) {
        if (blank(userId)) return ResponseResult.fail(401, "请先登录");
        Map<String, Object> draft = mapper.findDraftByNo(draftNo);
        if (draft == null) return ResponseResult.fail(404, "识别草稿不存在");
        if (!same(userId, draft.get("userId")) || !same(orgId, draft.get("orgId"))) {
            return ResponseResult.fail(403, "无权访问该识别草稿");
        }
        return ResponseResult.success(toPreview(draft));
    }

    @Override
    public ResponseResult updateItem(ImportItemUpdateRequest request, String userId, String orgId) {
        if (request == null || request.getItemId() == null) {
            return ResponseResult.fail(422, "识别项不能为空");
        }
        Map<String, Object> context = mapper.findItemContext(request.getItemId());
        if (context == null) return ResponseResult.fail(404, "识别项不存在");
        if (!same(userId, context.get("userId")) || !same(orgId, context.get("orgId"))) {
            return ResponseResult.fail(403, "无权修改该识别项");
        }
        String systemKey = String.valueOf(context.get("systemKey"));
        var entry = catalog.find(systemKey, request.getObjectKey());
        if (entry.isEmpty() || !entry.get().functionKey().equals(request.getFunctionKey())) {
            return ResponseResult.fail(422, "目标功能或业务对象不在允许目录中");
        }
        ResponseResult<Map<String, Object>> validation = catalog.validateWritableFields(
                systemKey, request.getObjectKey(), request.getFields());
        if (validation.getCode() != 1) return validation;
        mapper.updateItem(request.getItemId(), request.getFunctionKey(), request.getObjectKey(),
                JSON.toJSONString(validation.getData()));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("itemId", request.getItemId());
        result.put("functionKey", request.getFunctionKey());
        result.put("objectKey", request.getObjectKey());
        result.put("fields", validation.getData());
        result.put("validation", catalog.validate(systemKey, request.getObjectKey(), validation.getData()));
        return ResponseResult.success(result);
    }

    @Override
    public ResponseResult saveRecognition(String draftNo, Long itemId, Map<String, Object> recognition,
                                          String userId, String orgId) {
        if (blank(draftNo) || itemId == null || recognition == null) {
            return ResponseResult.fail(422, "识别结果信息不完整");
        }
        Map<String, Object> draft = mapper.findDraftByNo(draftNo);
        Map<String, Object> context = mapper.findItemContext(itemId);
        if (draft == null || context == null || !draftNo.equals(String.valueOf(context.get("draftNo")))) {
            return ResponseResult.fail(404, "识别草稿或文件不存在");
        }
        if (!same(userId, draft.get("userId")) || !same(orgId, draft.get("orgId"))) {
            return ResponseResult.fail(403, "无权保存该识别结果");
        }
        String systemKey = String.valueOf(draft.get("systemKey"));
        boolean manual = Boolean.TRUE.equals(recognition.get("needsManualTarget"));
        String functionKey = manual ? null : text(recognition.get("functionKey"));
        String objectKey = manual ? null : text(recognition.get("objectKey"));
        Map<String, Object> fields = map(recognition.get("fields"));
        String error = null;
        if (!manual) {
            var entry = catalog.find(systemKey, objectKey);
            if (entry.isEmpty() || !entry.get().functionKey().equals(functionKey)) {
                return ResponseResult.fail(422, "AI 识别对象不在允许目录中");
            }
            ResponseResult<Map<String, Object>> allowed = catalog.validateWritableFields(systemKey, objectKey, fields);
            if (allowed.getCode() != 1) return allowed;
            fields = allowed.getData();
        } else {
            Object errors = recognition.get("validationErrors");
            error = errors == null ? "需要人工选择目标" : String.valueOf(errors);
        }
        mapper.saveRecognition(number(draft.get("id")), itemId, functionKey, objectKey,
                JSON.toJSONString(recognition), JSON.toJSONString(fields),
                manual ? "needs_manual_target" : "recognized", error);
        return ResponseResult.success(Map.of("draftNo", draftNo, "itemId", itemId,
                "status", manual ? "needs_manual_target" : "recognized"));
    }

    @Override
    public ResponseResult prepareConfirm(Long itemId, String userId, String orgId) {
        if (itemId == null) return ResponseResult.fail(422, "识别项不能为空");
        Map<String, Object> stored = mapper.findConfirmItem(itemId);
        if (stored == null) return ResponseResult.fail(404, "识别项不存在");
        if (!same(userId, stored.get("userId")) || !same(orgId, stored.get("orgId"))) {
            return ResponseResult.fail(403, "无权确认该识别项");
        }
        String systemKey = text(stored.get("systemKey"));
        String objectKey = text(stored.get("objectKey"));
        String functionKey = text(stored.get("functionKey"));
        var entry = catalog.find(systemKey, objectKey);
        if (entry.isEmpty() || !entry.get().functionKey().equals(functionKey)) {
            return ResponseResult.fail(422, "请先选择有效的目标功能和业务对象");
        }
        Map<String, Object> fields = parseJsonMap(hasText(stored.get("editedFieldsJson"))
                ? stored.get("editedFieldsJson") : stored.get("aiFieldsJson"));
        ResponseResult<Map<String, Object>> validation = catalog.validate(systemKey, objectKey, fields);
        if (validation.getCode() != 1) return validation;
        Map<String, Object> attachment = new LinkedHashMap<>();
        attachment.put("fileName", stored.get("fileName"));
        attachment.put("fileUrl", stored.get("fileUrl"));
        attachment.put("fileSize", stored.get("fileSize"));
        attachment.put("contentType", stored.get("contentType"));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("itemId", itemId);
        result.put("draftId", stored.get("draftId"));
        result.put("draftNo", stored.get("draftNo"));
        result.put("systemKey", systemKey);
        result.put("functionKey", functionKey);
        result.put("objectKey", objectKey);
        result.put("adapterKey", entry.get().adapterKey());
        result.put("targetTable", entry.get().targetTable());
        result.put("detailPath", entry.get().detailPath());
        result.put("fields", validation.getData());
        result.put("attachment", attachment);
        return ResponseResult.success(result);
    }

    @Override
    public ResponseResult completeItem(Long itemId, String status, String businessRecordId, String detailUrl,
                                       String attachmentUrl, String errorMessage, String userId, String orgId) {
        if (itemId == null || !("success".equals(status) || "failed".equals(status))) {
            return ResponseResult.fail(422, "入库结果状态无效");
        }
        Map<String, Object> stored = mapper.findConfirmItem(itemId);
        if (stored == null) return ResponseResult.fail(404, "识别项不存在");
        if (!same(userId, stored.get("userId")) || !same(orgId, stored.get("orgId"))) {
            return ResponseResult.fail(403, "无权更新该识别项");
        }
        Long draftId = number(stored.get("draftId"));
        String originalUrl = text(stored.get("fileUrl"));
        if ("success".equals(status) && blank(originalUrl)) {
            return ResponseResult.fail(422, "原始附件地址不存在");
        }
        mapper.completeItem(draftId, itemId, status, businessRecordId, detailUrl, errorMessage);
        if ("success".equals(status)) {
            mapper.saveAttachment(draftId, itemId, text(stored.get("objectKey")), businessRecordId,
                    text(stored.get("fileName")), originalUrl, "bound", null);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("itemId", itemId);
        result.put("status", status);
        result.put("businessRecordId", businessRecordId);
        result.put("detailUrl", detailUrl);
        result.put("attachmentUrl", "success".equals(status) ? stored.get("fileUrl") : null);
        result.put("errorMessage", errorMessage);
        return ResponseResult.success(result);
    }

    private DraftPreviewResponse toPreview(Map<String, Object> draft) {
        DraftPreviewResponse preview = new DraftPreviewResponse();
        preview.setDraftNo(String.valueOf(draft.get("draftNo")));
        preview.setSystemKey(String.valueOf(draft.get("systemKey")));
        preview.setDescription(String.valueOf(draft.get("description")));
        preview.setStatus(String.valueOf(draft.get("status")));
        String systemKey = String.valueOf(draft.get("systemKey"));
        List<Map<String, Object>> items = mapper.findItemsByDraftId(number(draft.get("id"))).stream()
                .map(item -> toPreviewItem(item, systemKey)).toList();
        preview.setItems(items);
        return preview;
    }

    private Map<String, Object> toPreviewItem(Map<String, Object> stored, String systemKey) {
        Map<String, Object> item = new LinkedHashMap<>(stored);
        Object edited = stored.get("editedFieldsJson");
        Object ai = stored.get("aiFieldsJson");
        item.put("fields", parseJsonMap(hasText(edited) ? edited : ai));
        Map<String, Object> aiMetadata = parseJsonMap(stored.get("aiRawJson"));
        for (String key : List.of("summary", "source", "aiProvider", "aiModel", "recognitionNote", "validationErrors")) {
            if (aiMetadata.containsKey(key)) item.put(key, aiMetadata.get(key));
        }
        Object objectKey = stored.get("objectKey");
        if (objectKey != null) {
            catalog.find(systemKey, String.valueOf(objectKey)).ifPresent(entry -> {
                item.put("targetTable", entry.targetTable());
                item.put("fieldSchema", entry.fields());
            });
        }
        item.remove("aiFieldsJson");
        item.remove("editedFieldsJson");
        item.remove("aiRawJson");
        return item;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonMap(Object value) {
        if (!hasText(value)) return new LinkedHashMap<>();
        try {
            Map<String, Object> parsed = JSON.parseObject(String.valueOf(value), LinkedHashMap.class);
            return parsed == null ? new LinkedHashMap<>() : parsed;
        } catch (Exception ignored) {
            return new LinkedHashMap<>();
        }
    }

    private boolean hasText(Object value) {
        return value != null && !String.valueOf(value).isBlank();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        if (!(value instanceof Map<?, ?> source)) return new LinkedHashMap<>();
        Map<String, Object> result = new LinkedHashMap<>();
        source.forEach((key, fieldValue) -> result.put(String.valueOf(key), fieldValue));
        return result;
    }

    private String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static boolean same(String expected, Object actual) {
        String left = expected == null ? "" : expected;
        String right = actual == null ? "" : String.valueOf(actual);
        return left.equals(right);
    }

    private static long number(Object value) {
        if (value instanceof Number n) return n.longValue();
        return Long.parseLong(String.valueOf(value));
    }
}
