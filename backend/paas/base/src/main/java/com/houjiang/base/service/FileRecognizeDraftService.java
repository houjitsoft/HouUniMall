package com.houjiang.base.service;

import com.houjiang.base.dto.fileimport.DraftCreateRequest;
import com.houjiang.base.dto.fileimport.DraftFileRequest;
import com.houjiang.base.dto.fileimport.ImportItemUpdateRequest;
import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

public interface FileRecognizeDraftService {
    ResponseResult createOrUpdate(DraftCreateRequest request, DraftFileRequest file);
    ResponseResult resume(String userId, String orgId);
    ResponseResult findDraft(String draftNo, String userId, String orgId);
    ResponseResult updateItem(ImportItemUpdateRequest request, String userId, String orgId);
    ResponseResult saveRecognition(String draftNo, Long itemId, Map<String, Object> recognition,
                                   String userId, String orgId);
    ResponseResult prepareConfirm(Long itemId, String userId, String orgId);
    ResponseResult completeItem(Long itemId, String status, String businessRecordId, String detailUrl,
                                String attachmentUrl, String errorMessage, String userId, String orgId);
}
