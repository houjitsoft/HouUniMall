package com.houjiang.base.dto.fileimport;

import java.util.List;

/**
 * 服务端受控的智能入库对象。物理表名只来自本目录，不接受 AI 或前端传入。
 */
public record FileRecognizeCatalogEntry(
        String systemKey,
        String systemName,
        String functionKey,
        String functionName,
        String objectKey,
        String objectName,
        String targetTable,
        String adapterKey,
        String detailPath,
        String attachmentStrategy,
        List<FieldDefinition> fields
) {
    public FileRecognizeCatalogEntry {
        fields = List.copyOf(fields);
    }

    public record FieldDefinition(String name, String label, String type, boolean required) {
    }
}
