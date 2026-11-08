package com.houjiang.base.dto.fileimport;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class DraftPreviewResponse {
    private String draftNo;
    private String systemKey;
    private String description;
    private String status;
    private List<Map<String, Object>> items = new ArrayList<>();
}
