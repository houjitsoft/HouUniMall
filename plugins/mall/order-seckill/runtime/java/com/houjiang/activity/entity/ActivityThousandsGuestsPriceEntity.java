package com.houjiang.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 千客千价价格策略配置
 * @TableName activity_thousands_guests_price
 * @author Amos
 */
@TableName(value = "hou_addon_activity_thousands_guests_price")
@Data
public class ActivityThousandsGuestsPriceEntity implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * SKU ID，可为空，空表示对整个SPU生效
     */
    private Long skuId;

    /**
     * 人群类型：0-全部用户，1-指定用户，2-会员等级，3-标签
     */
    private Integer segmentType;

    /**
     * 人群值：userId / 等级编码 / 标签编码
     */
    private String segmentValue;

    /**
     * 价格类型：1-指定价格，2-折扣百分比，3-减金额
     */
    private Integer priceType;

    /**
     * 价格值：
     * - priceType=1：指定价格
     * - priceType=2：折扣百分比（如 95 表示95折）
     * - priceType=3：减金额
     */
    private BigDecimal priceValue;

    /**
     * 优先级，越大优先级越高
     */
    private Integer priority;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 生效开始时间
     */
    private Date startTime;

    /**
     * 生效结束时间
     */
    private Date endTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 备注
     */
    private String remark;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
