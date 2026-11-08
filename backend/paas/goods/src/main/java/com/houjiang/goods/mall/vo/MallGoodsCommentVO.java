package com.houjiang.goods.mall.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 商品评论列表 VO
 * @author Amos
 */
@Data
public class MallGoodsCommentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer createTime;
    private Integer shopId;
    private Integer userId;
    private Integer orderId;
    private Integer goodsId;
    private Integer goodsSkuId;
    private Integer score;
    private String content;
    private Integer isShow;
    private Integer isReply;
    private String replyContent;
    private Integer describeScore;
    private Integer serviceScore;
    private Integer logisticsScore;
    private Integer hasImages;
    private List<String> imageList;

    private String goodsName;
    private String goodsImage;
}
