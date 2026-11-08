package com.houjiang.goods.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.houjiang.commons.util.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 商品店铺分类版本(GoodsShopCatVersion)表实体类
 *
 * @author Amos
 * @since 2025-05-28 17:25:13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("goods_shop_cat_version")
public class GoodsShopCatVersionEntity implements Serializable {

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    
    /**
     * 版本id
     */
    @TableId(type = IdType.AUTO)
    private Integer goodsShopCatVerId;
    
    /**
     * 版本id
     */
    private Integer verId;
    
    /**
     * 商品id
     */
    private Integer commonId;
    
    /**
     * 商品名称
     */
    private String commonName;
    
    /**
     * 商品数据
     */
    private String verBody;
    
    /**
     * 店铺id
     */
    private Integer shopId;
    
    /**
     * 操作人id
     */
    private String operateUserId;
    
    /**
     * 操作人名称
     */
    private String operateUserName;
    
    /**
     * 操作时间
     */
    private Date operateTime;
    
    /**
     * 版本号
     */
    private String versionNo;

    public GoodsShopCatVersionEntity(Integer verId, Integer commonId, String commonName, String verBody, Integer shopId, String operateUserId, String operateUserName, String versionNo) {
        this.verId = verId;
        this.commonId = commonId;
        this.commonName = commonName;
        this.verBody = verBody;
        this.shopId = shopId;
        this.operateUserId = operateUserId;
        this.operateUserName = operateUserName;
        this.versionNo = versionNo;
        this.operateTime = DateUtils.getNowDateToDf();
    }
}
