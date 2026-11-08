package com.houjiang.base.dto.fileimport;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class ImportItemUpdateRequest {
    private Long itemId;
    private String functionKey;
    private String objectKey;
    private Map<String, Object> fields = new LinkedHashMap<>();
}
