package com.houjiang.job.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 数据大屏店铺表
 * @TableName ${database.prefix.datav}hou_datav_shop_base
 * @author Amos
 */
@TableName(value ="${database.prefix.datav}hou_datav_shop_base")
@Data
public class DcenterShopBaseEntity implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 开店时间
     */
    private Date shopCreateTime;

    /**
     * 店铺logo
     */
    private String shopLogo;

    /**
     * 店铺状态-3：开店成功 2:待审核付款 1:待审核资料  0:关闭
     */
    private Integer shopStatus;

    /**
     * 店铺类型: 1-卖家店铺; 2:供应商店铺
     */
    private Integer shopType;

    /**
     * 用户id
     */
    private String userId;
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}