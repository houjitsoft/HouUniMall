package com.houjiang.goods.vo;

import lombok.Data;

import java.util.List;

/**
 * @ClassName ManageSpecValueResultEditVO
 * @Author Amos
 * @Date 2023/6/27 14:39
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageSpecValueResultEditVO {

    /**
     * 规格名称值id
     */
    private String specValueId;

    /**
     * 规格值名称
     */
    private String specValueName;

    /**
     * 分类id
     */
    private String catId;

    /**
     * 规格类型 多规格
     */
    private String  specId;
    /**
     * 规格名称
     */
    private  String specName;

    /**
     * 规格信息
     */
    private List<ManageSpecValueResultSolVO> specValueNames;
}
