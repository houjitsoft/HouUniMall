package com.houjiang.goods.dto;


import lombok.Data;

import java.util.List;

/**
 * 规格
 * @author Amos
 */
@Data
public class ManageSpecValueResultEditDTO {

    /**
     * 规格名称值id
     */
    private String specValueId;

    /**
     * 规格名称值名称
     */
    private String specValueName;

    /**
     * 分类id
     */
    private String catId;

    /**
     * 规格id
     */
    private String specId;

    /**
     * 规格名称
     */
    private String specName;

    /**
     * 规格值名字集合
     */
    private List<ManageSpecValueResultSolDTO> specValueNames;

}
