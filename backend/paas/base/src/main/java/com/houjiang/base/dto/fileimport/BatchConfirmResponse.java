package com.houjiang.base.dto.fileimport;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class BatchConfirmResponse {
    private String draftNo;
    private int successCount;
    private int failedCount;
    private List<Map<String, Object>> items = new ArrayList<>();
}
