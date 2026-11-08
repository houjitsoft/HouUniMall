package com.houjiang.goods.vo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;


/**
 * 京东商品代发记录
 *
 * @author Amos
 * @since 2025-05-29 14:07:48
 */
@Data
public class GoodsJingdongDropshippingVO implements Serializable {

     /**
     * 商品commonId
     */        
    private Integer commonId;
        
     /**
     * 京东商品商品spuId
     */        
    private String spuId;

    private String goodsName;

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
    
}

