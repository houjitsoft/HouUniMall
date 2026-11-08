package com.houjiang.goods.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * Mall 商品评论实体
 * 表：hou_mall_goods_comment
 * @author Amos
 */
@Data
@TableName("hou_mall_goods_comment")
public class MallGoodsComment implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer createTime;
    private Integer updateTime;
    private Integer deleteTime;

    private Integer shopId;
    private Integer userId;
    private Integer orderId;
    private Integer orderGoodsId;
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
    private String images;
}
