package com.houjiang.activity.dto;


import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

/**
 * @author Amos
 */
@Data
public class SecondKillGoodsDTO extends PageHelper {
    /**
     * 限时秒杀商品 id
     */
    private int seckillGoodsId;
    /**
     *限时活动编号
     */
    private int seckillId;
    /**
     *商品名称
     */
    private String goodsName;
    /**
     *商品原价
     */
    private double goodsPrice;
    /**
     *限时秒杀价格
     */
    private double seckillPrice;
    /**
     *商品图片
     */
    private String goodsImage;
    /**
     * skuId
     */
    private int goodsId;
    /**
     *commonid
     */
    private Integer commonId;
    /**
     *满减赠折活动id
     */
    private Integer goodsUpperLimit;
    /**
     *秒杀成功数
     */
    private Integer seckillNum;


}
