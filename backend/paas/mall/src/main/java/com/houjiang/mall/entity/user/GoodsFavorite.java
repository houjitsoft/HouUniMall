package com.houjiang.mall.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品收藏实体类
 * 表名: hou_mall_goods_favorite
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_goods_favorite")
public class GoodsFavorite implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 商品名称(冗余)
     */
    private String goodsName;

    /**
     * 商品图片(冗余)
     */
    private String goodsImage;

    /**
     * 商品价格(冗余)
     */
    private BigDecimal goodsPrice;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 店铺名称(冗余)
     */
    private String shopName;

    /**
     * 收藏类型 1-商品收藏 2-店铺收藏
     */
    private Integer favoriteType;

    /**
     * 是否有效 0-失效 1-有效
     */
    private Integer isValid;

    /**
     * 失效原因
     */
    private String invalidReason;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    @TableLogic
    private Integer deleted;
}
