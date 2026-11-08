package com.houjiang.commons.vo.base;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 微信直播商品表
 * @TableName base_wx_live_goods
 * @author Amos
 */
@Data
public class BaseWxLiveGoodsVO implements Serializable {
    /**
     * 
     */
    private Integer id;

    /**
     * 商品id
     */
    private Integer commonId;

    /**
     * 规格id
     */
    private Integer goodsId;

    /**
     * 小程序审核后返回的goodsId
     */
    private Integer wxGoodsId;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 店铺名
     */
    private String shopName;

    /**
     * 商品名称
     */
    private String commonName;

    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;

    /**
     * 商品库存
     */
    private Integer commonStock;

    /**
     * 商品图片
     */
    private String commonImage;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 操作 0无 1添加 2删除
     */
    private Integer operation;

    /**
     * 审核状态 1 通过 2 审核中 3 不通过  4待平台审核  5待微信审核
     */
    private Integer isverify;

    /**
     * 审核原因
     */
    private String cause;

    private static final long serialVersionUID = 1L;
}