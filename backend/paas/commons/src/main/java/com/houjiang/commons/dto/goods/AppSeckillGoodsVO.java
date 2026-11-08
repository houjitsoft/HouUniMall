package com.houjiang.commons.dto.goods;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品
 * @author Amos
 */
@Data
public class AppSeckillGoodsVO {

    /**
     * 限时秒杀价格
     */
    private BigDecimal seckillPrice;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date goodsStartTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date goodsEndTime;

    /**
     * 剩余时间
     */
    private Long overplusTime;

    /**
     * 购买下限，0为不限制
     */
    private Integer goodsLowerLimit;

    /**
     * 购买上限
     */
    private Integer goodsUpperLimit;

    /**
     * 秒杀成功数
     */
    private Integer seckillNum;

    /**
     * 商品id
     */
    private Integer seckillGoodsId;

    /**
     * 商品id
     */
    private Integer commonId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;

    /**
     * 商品图片
     */
    private String goodsImage;

    /**
     * 规格名
     */
    private String goodsSpec;

    /**
     * 规格名
     */
    private String specValueName;

    /**
     * 秒杀id
     */
    private Integer seckillId;

    /**
     * 商品库存
     */
    private Integer goodsStock;

    /**
     * 商品名称
     */
    private String commonName;

    /**
     * 是否参加了活动 0不参加,1砍价,2秒杀,3限时折扣,4拼团,5满减赠折
     */
    private Integer commonIsXian;

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startDateTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endDateTime;

    /**
     * 店铺id
     */
    private Integer shopId;



}
