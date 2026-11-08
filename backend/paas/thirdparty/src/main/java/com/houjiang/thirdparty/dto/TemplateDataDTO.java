package com.houjiang.thirdparty.dto;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class TemplateDataDTO {
    private String value;

    public TemplateDataDTO(String value) {
        this.value = value;
    }
}
