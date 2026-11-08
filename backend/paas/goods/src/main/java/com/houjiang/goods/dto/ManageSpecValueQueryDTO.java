package com.houjiang.goods.dto;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

/**
 * @ClassName ManageSpecValueQueryDTO
 * @Description
 * @Author Amos
 * @Date 2023/8/10 19:05
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageSpecValueQueryDTO extends UserTokenBeanDTO {

    /**
     * 分类id列表
     */
    private String catIds;

    /**
     * 店铺id
     */
    private String shopId;

    /**
     * 分类id
     */
    private Integer catId;

    /**
     * 类型id
     */
    private Integer typeId;

    /**
     * 规格类型 多规格
     */
    private String specId;
}
