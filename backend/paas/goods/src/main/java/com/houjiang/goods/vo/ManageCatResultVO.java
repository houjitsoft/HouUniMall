package com.houjiang.goods.vo;

import lombok.Data;

/**
 * @ClassName ManageCatResultVO
 * @Description 商家管理分类查询结果
 * @Author Amos
 * @Date 2023/8/10 19:13
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageCatResultVO {

    /**
     * 分类id
     */
    private Integer catId;

    /**
     * 分类名称
     */
    private String catName;

    /**
     * 父分类id
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
