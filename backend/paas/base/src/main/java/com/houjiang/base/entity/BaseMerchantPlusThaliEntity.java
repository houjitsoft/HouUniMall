package com.houjiang.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.houjiang.commons.dto.userms.PlusEquityDTO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * plus会员套餐表
 * @TableName base_merchant_plus_thali
 * @author Amos
 */
@TableName(value ="hou_base_merchant_plus_thali")
@Data
public class BaseMerchantPlusThaliEntity implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer thaliId;

    /**
     * 套餐类型 1 月卡 2 季卡 3 年卡
     */
    private Integer thaliType;

    /**
     * 套餐价格
     */
    private BigDecimal thaliPrice;

    /**
     * 套餐权益id
     */
    private String thaliEquity;

    /**
     * 是否推荐 0 不推荐 1 推荐
     */
    private Integer thaliRecommend;

    /**
     * 套餐状态   0 关闭  1  启用
     */
    private Integer thaliStatus;

    /**
     * 更新时间
     */
    private Date thaliUpdatetime;

    /**
     * 原价
     */
    private BigDecimal thaliOldprice;

    /**
     * 套餐权益集合
     */
    @TableField(exist = false)
    private List thaliEquityList;

    /**
     * 会员权益
     */
    @TableField(exist = false)
    private List<PlusEquityDTO> plusEquitys;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}