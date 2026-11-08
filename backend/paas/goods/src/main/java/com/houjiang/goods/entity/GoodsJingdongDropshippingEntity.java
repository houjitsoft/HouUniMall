package com.houjiang.goods.entity;



import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


/**
 * 京东商品代发记录(GoodsJingdongDropshipping)表实体类
 *
 * @author Amos
 * @since 2025-05-29 14:07:48
 */
@TableName("goods_jingdong_dropshipping")
@Data
public class GoodsJingdongDropshippingEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;
        
     /**
     * 商品commonId
     */        
    private Integer commonId;
        
     /**
     * 京东商品商品spuId
     */        
    private String spuId;
        
     /**
     * 商品goodsId
     */        
    private Integer goodsId;
        
     /**
     * 京东商品商品skuId
     */        
    private String skuId;
        
     /**
     * 代发店铺id
     */        
    private Integer shopId;
        
     /**
     * 是否删除0:否,1:是
     */        
    private Integer isDelete;
    
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

