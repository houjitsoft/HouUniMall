package com.houjiang.activity.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
 /**
 * @ClassName
 * @Description  限时秒杀商品表
 * @Author Amos
 * @Date 2023-05-25 13:58:28
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_addon_activity_seckill_goods")
@Data
public class ActivitySeckillGoodsEntity  implements Serializable {
    /**
     * 限时秒杀商品表
     */
    @TableId(type = IdType.AUTO)
    private Integer seckillGoodsId;
    /**
     * 限时活动编号
     */
    private Integer seckillId;
    /**
     * 活动名称
     */
    private String seckillName;
    /**
     * 商品编号
     */
    private Integer goodsId;

    private Integer commonId;
    /**
     * 店铺编号
     */
    private Integer shopId;
    /**
     * 商品名称
     */
    private String goodsName;
    /**
     * 商品原价
     */
    private Double goodsPrice;
    /**
     * 限时秒杀价格
     */
    private Double seckillPrice;
    /**
     * 商品图片
     */
    private String goodsImage;
    /**
     * 开始时间
     */
    private Date goodsStartTime;
    /**
     * 结束时间
     */
    private Date goodsEndTime;
    /**
     * 状态:0-未开始;1-已开始;2-已结束；3-已失效
     */
    private Integer seckillGoodsState;
    /**
     * 限购数量
     */
    private Integer goodsUpperLimit;
    /**
     * 秒杀库存数量
     */
    private Integer seckillNum;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

