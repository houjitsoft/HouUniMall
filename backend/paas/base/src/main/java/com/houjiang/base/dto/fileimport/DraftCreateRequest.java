package com.houjiang.base.dto.fileimport;

import lombok.Data;

@Data
public class DraftCreateRequest {
    private String draftNo;
    private String systemKey;
    private String description;
    private String userId;
    private String orgId;
}
