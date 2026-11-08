package com.houjiang.goods.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.houjiang.goods.dto.GoodsEntityDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 美食中国表
 * @TableName goods_food_exhibition_hall
 * @author Amos
 */
@TableName(value ="goods_food_exhibition_hall")
@Data
public class GoodsFoodExhibitionHallEntity implements Serializable {
    /**
     * 展馆ID
     */
    @TableId(type = IdType.AUTO)
    private Integer exhibitionHallId;

    /**
     * 展馆名称
     */
    private String exhibitionHallName;

    /**
     * 展馆主图
     */
    private String exhibitionHallImage;

    /**
     * 展馆logo
     */
    private String exhibitionHallLogo;

    /**
     * wap端logo
     */
    private String exhibitionHallLogoWap;

    /**
     * 展馆图文描述
     */
    private String exhibitionHallDesc1;

    /**
     * 
     */
    private String exhibitionHallDesc2;

    /**
     * 
     */
    private String exhibitionHallDesc3;

    /**
     * 展馆商品ID
     */
    private String goodsId;

    /**
     * 创建时间
     */
    private Date publishTime;

    /**
     * 展馆状态：1正常 0关闭，暂时不用，便于以后扩充
     */
    private Boolean exhibitionHallStatus;

    /**
     * 商品信息
     */
    @TableField(exist = false)
    private List<GoodsEntityDTO> goodsList;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}