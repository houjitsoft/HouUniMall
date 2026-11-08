package com.houjiang.base.mapper;

import java.util.Map;
import java.util.List;

public interface FileRecognizeDraftMapper {
    Map<String, Object> findDraftByNo(String draftNo);
    Map<String, Object> findFileByDraftAndHash(Long draftId, String fileHash);
    long insertDraft(Map<String, Object> values);
    long insertFile(Map<String, Object> values);
    void updateDraft(Long id, String systemKey, String description, String status);
    Map<String, Object> findLatestOpenDraft(String userId, String orgId);
    List<Map<String, Object>> findItemsByDraftId(Long draftId);
    Map<String, Object> findItemContext(Long itemId);
    void updateItem(Long itemId, String functionKey, String objectKey, String editedFieldsJson);
    void saveRecognition(Long draftId, Long itemId, String functionKey, String objectKey,
                         String aiRawJson, String aiFieldsJson, String status, String errorMessage);
    Map<String, Object> findConfirmItem(Long itemId);
    void completeItem(Long draftId, Long itemId, String status, String businessRecordId,
                      String detailUrl, String errorMessage);
    void saveAttachment(Long draftId, Long itemId, String objectKey, String businessRecordId,
                        String fileName, String fileUrl, String status, String errorMessage);
}
