package com.houjiang.base.dto.fileimport;

import lombok.Data;

@Data
public class DraftFileRequest {
    private String fileName;
    private String fileHash;
    private String fileUrl;
    private Long fileSize;
    private String contentType;
    private String aiRawJson;
    private String editedFieldsJson;
    private String status;
}
