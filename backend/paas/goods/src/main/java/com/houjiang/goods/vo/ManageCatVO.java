package com.houjiang.goods.vo;

import com.houjiang.commons.dto.PageUserTokenBeanDTO;
import lombok.Data;

/**
 * @ClassName ManageCatVO
 * @Description 商品分类
 * @Author Amos
 * @Date 2023/6/28 9:44
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageCatVO extends PageUserTokenBeanDTO {
    private Integer catId;

    /**
     *  分类名称
     */
    private String catName;

    /**
     * 父类
     */
    private Integer catParentId;

    /**
     * 分类图片
     */
    private String catPic;

    /**
     * 类型id
     */
    private Integer typeId;

    /**
     * 是否允许虚拟
     */
    private Integer catIsVirtual;
}
