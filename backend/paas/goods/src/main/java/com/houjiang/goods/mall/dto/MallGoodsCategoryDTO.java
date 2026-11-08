package com.houjiang.goods.mall.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 商品分类增改参数
 * @author Amos
 */
@Data
public class MallGoodsCategoryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String name;
    private Integer pid;
    private Integer level;
    private String image;
    private Integer sort;
    private Integer isShow;
}
