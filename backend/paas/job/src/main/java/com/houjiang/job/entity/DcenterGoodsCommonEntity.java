package com.houjiang.job.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 数据大屏商品sku表
 * @TableName ${database.prefix.datav}hou_datav_goods_common
 * @author Amos
 */
@TableName(value ="${database.prefix.datav}hou_datav_goods_common")
@Data
public class DcenterGoodsCommonEntity implements Serializable {
    /**
     * 商品id
     */
    @TableId(type = IdType.AUTO)
    private Integer commonId;

    /**
     * 商品名称
     */
    private String commonName;

    /**
     * 店铺状态-3：开店成功 2:待审核付款 1:待审核资料  0:关闭
     */
    private Integer shopStatus;

    /**
     * 商品销量
     */
    private Integer commonSalenum;

    /**
     * 商品主图
     */
    private String commonImage;

    /**
     * 商品状态 0下架，1正常，10违规（禁售）,2店铺下架商品下架,3商品删除
     */
    private Integer commonState;

    /**
     * 商品审核 1通过，0未通过，10审核中
     */
    private Integer commonVerify;

    /**
     * 商品添加时间
     */
    private Date commonAddTime;

    /**
     * 商品收藏量
     */
    private Integer commonCollect;

    /**
     * 商品评论数
     */
    private Integer commonEvaluate;

    /**
     * 商品是否屏蔽：1-否；2-是
     */
    private Integer commonIsScreen;

    /**
     * 积分商品 0是 1否
     */
    private Integer integralGoods;

    /**
     * 店铺id
     */
    private Integer shopId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}