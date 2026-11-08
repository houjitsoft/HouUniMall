package com.houjiang.bffweb.service.fileimport;

import com.houjiang.bffweb.service.FileImportAdapterResult;

import java.util.Map;
import java.util.Set;

/**
 * One explicit, controlled bridge from recognized fields to a business service.
 */
public interface FileImportBusinessAdapter {
    Set<String> keys();

    FileImportAdapterResult importItem(String key, Map<String, Object> fields,
                                       Map<String, Object> attachment);
}
