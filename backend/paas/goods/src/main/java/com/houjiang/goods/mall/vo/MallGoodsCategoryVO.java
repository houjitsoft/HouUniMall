package com.houjiang.goods.mall.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 商品分类 VO
 * @author Amos
 */
@Data
public class MallGoodsCategoryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String name;
    private Integer pid;
    private Integer level;
    private String image;
    private Integer sort;
    private Integer isShow;
    private Integer createTime;

    /** 子级 */
    private List<MallGoodsCategoryVO> children;
}
