package com.houjiang.commons.util.excel;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class PoiModel {

    private String content;

    private String oldContent;

    private int rowIndex;

    private int cellIndex;

}
