package com.houjiang.goods.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 商品库存表
 * @TableName goods_stock
 * @author Amos
 */
@Data
public class MerchantGoodsStockVO {
    /**
     * 
     */
    private Integer id;

    /**
     * goodsId
     */
    private Integer goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * commonId
     */
    private Integer commonId;

    /**
     * 商品库存
     */
    private Integer goodsStock;

    /**
     * 库存预警值
     */
    private Integer goodsAlarm;

    /**
     * 创建者
     */
    private String createUser;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改人
     */
    private String updateUser;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 仓库名称
     */
    private String storageName;

    /**
     * 商品规格
     */
    private String goodsSpec;

    /**
     * 商品图片
     */
    private String goodsImage;

    /**
     * 单位
     */
    private String dw;

    /**
     * 总库存
     */
    private Integer totalStorageStock;

    /**
     * 仓库集合
     */
    private List<StorageGoodsBaseVO> storageGoodsList;

}