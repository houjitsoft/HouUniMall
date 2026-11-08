package com.houjiang.goods.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 仓库中商品信息表(StorageGoodsBase)表实体类
 *
 * @author Amos
 * @since 2024-09-19 14:06:57
 */
@Data
@TableName("storage_goods_base")
public class StorageGoodsBaseEntity implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Integer storageGoodsId;
    
    /**
     * 店铺id
     */
    private Integer shopId;
    
    /**
     * 商品skuid
     */
    private Integer goodsId;
    
    /**
     * 商品spuid
     */
    private Integer commonId;
    
    /**
     * 仓库商品库存
     */
    private Integer storageStock;
    
    /**
     * 仓库id
     */
    private Integer storageId;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 修改时间
     */
    private Date updateTime;

}
