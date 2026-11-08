package com.houjiang.commons.util.wx;

import lombok.Data;

/**
 * @author Amos
 * @version 1.0.0
 * @ClassName NewWxMaterialResult.java
 * @Description NewWxMaterialResult
 * @createTime 2021年08月19日 11:57:00
 */
@Data
public class NewWxMaterialResult {

    private String type;
    private String mediaId;
    private String thumbMediaId;
    private Integer createdAt;
    private Integer[] item;

}
