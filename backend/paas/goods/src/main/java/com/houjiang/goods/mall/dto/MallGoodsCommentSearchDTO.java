package com.houjiang.goods.mall.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 商品评论搜索参数
 * @author Amos
 */
@Data
public class MallGoodsCommentSearchDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer pageNo = 1;
    private Integer pageSize = 15;

    private Integer goodsId;
    private Integer shopId;
    private Integer userId;
    private Integer score;
    private Integer isShow;
    private String goodsName;
}
