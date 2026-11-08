package com.houjiang.commons.dto.goods;


import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

/**
 * 分类
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ManageSpecValueInfoDTO extends UserTokenBeanDTO {

    /**
     * 分类id
     */
    private String catIds;

    /**
     * 店铺id
     */
    private String shopId;

    /**
     * 分类id
     */
    private String catId;

    /**
     * 类型id
     */
    private Integer typeId;

    /**
     * 所属规格id
     */
    private String specId;


    /**
     * 规格名称
     */
    private String specName;

    /**
     * 规格值名字集合
     */
    private List specValueNames;


    /**
     * 规格名称值id
     */
    private String specValueId;

}
